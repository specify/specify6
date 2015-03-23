/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.af.ui.forms.persist;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * Class that manages all the View (forms) for a given view set (which is read from a single file).<br><br>
 * NOTE: ViewSets can have "transient" Views and ViewDefs. These are created dynamically in memory and are not persisted.

 * @code_status Beta
 **
 * @author rods
 */

public class ViewSet implements Comparable<ViewSetIFace>, ViewSetIFace
{
    private static final Logger  log = Logger.getLogger(ViewSet.class);
    private static boolean ALWAYS_LOAD = false; // XXX PREF

    protected Type                       type              = Type.User;
    protected String                     name              = null;
    protected String                     title             = null;
    protected String                     fileName          = null;
    protected File                       dirPath           = null;
    protected String                     i18NResourceName  = null;

    protected boolean                         isDiskBased       = true;
    protected boolean                         hasLoadedViews    = false;
    protected Hashtable<String, ViewIFace>    transientViews    = null;
    protected Hashtable<String, ViewDefIFace> transientViewDefs = null;
    protected Hashtable<String, ViewIFace>    views             = new Hashtable<String, ViewIFace>();
    protected Hashtable<String, ViewDefIFace> viewDefs          = new Hashtable<String, ViewDefIFace>();

    /**
     * Default Constructor.
     *
     */
    public ViewSet()
    {
        // do nothing
    }

    /**
     * Constructor from a DOM element.
     * @param rootDOM the DOM element
     */
    public ViewSet(final Element rootDOM,
                   final boolean doMapDefinitions) throws Exception
    {
        isDiskBased = false;
        loadDOM(rootDOM, true, doMapDefinitions);
    }


    /**
     * Constructor.
     * @param type indicates that is contains the core set of forms that
     *             can be referred in other places with specifying the viewset name
     * @param name name of view set
     * @param title human readable title (short description)
     * @param fileName the filename of the ViewSet
     * @param dirPath the directory path to the viewset
     * @param i18NResourceName the name of the resource file
      */
    public ViewSet(final Type type,
                   final String name,
                   final String title,
                   final String fileName,
                   final File   dirPath)
    {
        this.type     = type;
        this.name     = name;
        this.title    = title;
        this.fileName = fileName;
        this.dirPath  = dirPath;
        this.isDiskBased = true;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.persist.ViewSetIFace#isDiskBased()
     */
    @Override
    public boolean isDiskBased()
    {
        return isDiskBased;
    }

    /**
     * @param isDiskBased the isDiskBased to set
     */
    public void setDiskBased(boolean isDiskBased)
    {
        this.isDiskBased = isDiskBased;
    }

    /**
     * Parse for the type and converts it to the Enum.
     * @param typeStr the type
     * @return the enum of the type
     */
    public static Type parseType(final String typeStr)
    {
        if (typeStr.equalsIgnoreCase("user"))
        {
            return Type.User;
        }

        return Type.System;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        if (views != null)
        {
            //for (View fv : views)
            //{
            //    fv.cleanUp();
            //}
            views.clear();    
        }
        
        if (views != null)
        {
            viewDefs.clear();
        }
        
        hasLoadedViews = false;
    }

    /**
     * Loads the view from the file.
     */
    public void loadViews()
    {
        if ((ALWAYS_LOAD || !hasLoadedViews) && dirPath != null && fileName != null)
        {
            try
            {
                loadViewFile(new FileInputStream(new File(dirPath + File.separator + fileName)));

            } catch (FileNotFoundException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ViewSet.class, ex);
                log.error(ex);
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ViewSet.class, ex);
                log.error(ex);
                ex.printStackTrace();
            }
        }
        
        // Add any Transient Views back in after reloading the ViewSet
        if (transientViews != null)
        {
            for (String viewName : transientViews.keySet())
            {
                views.put(viewName, transientViews.get(viewName));
            }
        }
        
        if (transientViewDefs != null)
        {
            for (String viewName : transientViewDefs.keySet())
            {
                viewDefs.put(viewName, transientViewDefs.get(viewName));
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getView(java.lang.String)
     */
    @Override
    public ViewIFace getView(final String nameStr)
    {
        loadViews();

        return views.get(nameStr);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getViews()
     */
    @Override
    public Hashtable<String, ViewIFace> getViews()
    {
        loadViews();
        
        return views;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getViewDefs()
     */
    @Override
    public Hashtable<String, ViewDefIFace> getViewDefs()
    {
        return viewDefs;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getName()
     */
    @Override
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getType()
     */
    @Override
    public Type getType()
    {
        return type;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#setName(java.lang.String)
     */
    @Override
    public void setName(final String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getFileName()
     */
    @Override
    public String getFileName()
    {
        return fileName;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.persist.ViewSetIFace#getI18NResourceName()
     */
    @Override
    public String getI18NResourceName()
    {
        return i18NResourceName;
    }

    /**
     * @param resourceName the i18NResourceName to set
     */
    public void setI18NResourceName(String resourceName)
    {
        i18NResourceName = resourceName;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#isSystem()
     */
    @Override
    public boolean isSystem()
    {
        return type == Type.System;
    }
    
    /**
     * Adds a dynamic or transient View; which is a View that is not read from the database or a file.
     * @param view the in memory View
     */
    public void addTransientView(final ViewIFace view)
    {
        if (transientViews == null)
        {
            transientViews = new Hashtable<String, ViewIFace>();
            
        } else if (transientViews.get(view.getName()) != null)
        {
            throw new RuntimeException("Transient View Name ["+view.getName()+"] is already being used!");
        }
        
        transientViews.put(view.getName(), view);
        views.put(view.getName(), view);
    }

    /**
     * Adds a dynamic or transient ViewDef; which is a View that is not read from the database or a file.
     * @param viewDef the in memory ViewDef
     */
    public void addTransientViewDef(final ViewDefIFace viewDef)
    {
        if (transientViewDefs == null)
        {
            transientViewDefs = new Hashtable<String, ViewDefIFace>();
            
        } else if (transientViews.get(viewDef.getName()) != null)
        {
            String msg = "Transient View Name ["+viewDef.getName()+"] is already being used!";
            log.error(msg);
            FormDevHelper.appendFormDevError(msg);
            return;
        }
        
        transientViewDefs.put(viewDef.getName(), viewDef);
        viewDefs.put(viewDef.getName(), viewDef);
    }

    /**
     * Adds a dynamic or transient View; which is a View that is not read from the database or a file.
     * @param view the in memory View
     */
    public void removeTransientView(final ViewIFace view)
    {
        if (transientViews != null)
        {
            transientViews.remove(view.getName());
            views.remove(view.getName());
            view.cleanUp();
        }
        
    }

    /**
     * Adds a dynamic or transient ViewDef; which is a View that is not read from the database or a file.
     * @param viewDef the in memory ViewDef
     */
    public void removeTransientViewDef(final ViewDefIFace viewDef)
    {
        if (transientViewDefs != null)
        {
            transientViewDefs.remove(viewDef.getName());
            viewDefs.remove(viewDef.getName());
            viewDef.cleanUp();
        }
    }

    /**
     * Loads the ViewSet from a DOM element.
     * @param rootDOM
     * @param doSetName
     * @param doMapDefinitions tells it to map and clone the definitions for formtables (use false for the FormEditor)
     * @throws Exception
     */
    protected void loadDOM(final Element rootDOM, 
                           final boolean doSetName,
                           final boolean doMapDefinitions) throws Exception
    {
        if (rootDOM != null)
        {
            viewDefs.clear();
            views.clear();
            
            Hashtable<AltViewIFace, String> altViewsViewDefName = new Hashtable<AltViewIFace, String>();
            
            i18NResourceName = getAttr(rootDOM, "i18nresname", null);

            String viewsName = ViewLoader.getViews(rootDOM, views, altViewsViewDefName);
            if (viewsName != null)
            {
                if (doSetName)
                {
                    name = viewsName;
    
                } else if (!viewsName.equals(name))
                {
                    String msg = "The name in the registry doesn't match the name in the file!["+name+"]["+viewsName+"]";
                    log.error(msg);
                    FormDevHelper.appendFormDevError(msg);
                    return;
                }
                
                boolean hasResBundleName = StringUtils.isNotEmpty(i18NResourceName);
                if (hasResBundleName)
                {
                    UIRegistry.loadAndPushResourceBundle(i18NResourceName);
                }
                
                try
                {
                    // Do these first so the view can check their altViews against them 
                    ViewLoader.getViewDefs(rootDOM, viewDefs, views, doMapDefinitions);
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ViewSet.class, ex);
                    
                } finally
                {
                    if (hasResBundleName)
                    {
                        UIRegistry.popResourceBundle();
                    }
                }
    
                verifyViewsAndViewDefs(altViewsViewDefName);
            }
            
        } else
        {
            String msg = "The root element for the document was null!";
            log.error(msg);
            FormDevHelper.appendFormDevError(msg);
            hasLoadedViews = false;
            return;
        }
        hasLoadedViews = true;
    }
    
    /**
     * This verifies that a view refers to a valid ViewDef. It also sets the ViewDef object into the AltView.
     * @param views the hash of views
     * @param viewDefs the hash of viewdefs
     */
    protected void verifyViewsAndViewDefs(final Hashtable<AltViewIFace, String> altViewsViewDefName)
    {
        // Need to get the Viewdefs and put them into the AltView
        
        for (ViewIFace view : views.values())
        {
            for (AltViewIFace av : view.getAltViews())
            {
                String viewDefName = altViewsViewDefName.get(av);
                if (av.getViewDef() == null)
                {
                    if (StringUtils.isNotEmpty(viewDefName))
                    {
                        ViewDefIFace referredToViewDef = viewDefs.get(viewDefName);
                        if (referredToViewDef != null)
                        {
                            av.setViewDef(referredToViewDef);
                        } else
                        {
                            String msg = "ViewSet["+name+"] View["+view+"] AltView ["+av.getName()+"] refers to a non-existent ViewDef with name["+av.getViewDefName()+"]";
                            log.error(msg);
                            FormDevHelper.appendFormDevError(msg);
                        }
                        
                    } else
                    {
                        String msg = "ViewSet["+name+"] Couldn't find the ViewDef Name for the AltView!";
                        log.error(msg);
                        FormDevHelper.appendFormDevError(msg);
                    }
                }
            }
        }
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#toXML(java.lang.StringBuffer)
     */
    @Override
    public void toXML(final StringBuilder sb)
    {
        sb.append("<viewset name=\""+name+"\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        
        sb.append("  <views>\n");
        Vector<ViewIFace> viewsList = new Vector<ViewIFace>(views.values());
        Collections.sort(viewsList);
        for (ViewIFace view : viewsList)
        {
            view.toXML(sb);
        }
        sb.append("  </views>\n");
        
        sb.append("  <viewdefs>\n");
        Vector<ViewDefIFace> viewDefsList = new Vector<ViewDefIFace>(viewDefs.values());
        Collections.sort(viewDefsList);
        for (ViewDefIFace viewDef : viewDefsList)
        {
            /*if (viewDef instanceof FormViewDef)
            {
                if (((FormViewDef)viewDef).getDefinitionName() != null && ((FormViewDef)viewDef).getDefinitionName().equals("Collectors"))
                {
                    int x = 0;
                    x++;
                }
            }*/
            viewDef.toXML(sb);
        }
        sb.append("  </viewdefs>\n");
        sb.append("</viewset>\n");
    }

    /**
     * Load an XML View File from a stream if the ViewSet is not unique than it throws and exception.
     * @param fileInputStream a file input stream to read the DOM4J from
     * @throws Exception on various errors
     */
    protected void loadViewFile(final FileInputStream fileInputStream) throws Exception
    {
        isDiskBased = true;
        loadDOM(XMLHelper.readFileToDOM4J(fileInputStream), false, true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#compareTo(edu.ku.brc.ui.forms.persist.ViewSet)
     */
    @Override
    public int compareTo(ViewSetIFace obj)
    {
        return name.compareTo(obj.getName());
    }
}
