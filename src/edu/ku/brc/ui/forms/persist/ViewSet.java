/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.ui.forms.persist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.helpers.XMLHelper;

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

    protected boolean                    hasLoadedViews    = false;
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
    protected void loadViews()
    {
        if ((ALWAYS_LOAD || !hasLoadedViews) && dirPath != null && fileName != null)
        {
            try
            {
                loadViewFile(new FileInputStream(new File(dirPath + File.separator + fileName)));

            } catch (FileNotFoundException ex)
            {
                log.error(ex);
                
            } catch (Exception ex)
            {
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
    public ViewIFace getView(final String nameStr)
    {
        loadViews();

        return views.get(nameStr);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getViews()
     */
    public Hashtable<String, ViewIFace> getViews()
    {
        loadViews();
        
        return views;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getViewDefs()
     */
    public Hashtable<String, ViewDefIFace> getViewDefs()
    {
        return viewDefs;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getType()
     */
    public Type getType()
    {
        return type;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#setName(java.lang.String)
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getTitle()
     */
    public String getTitle()
    {
        return title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#getFileName()
     */
    public String getFileName()
    {
        return fileName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#isSystem()
     */
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
            throw new RuntimeException("Transient View Name ["+viewDef.getName()+"] is already being used!");
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

            String viewsName = ViewLoader.getViews(rootDOM, views, altViewsViewDefName);
            if (doSetName)
            {
                name = viewsName;

            } else if (!viewsName.equals(name))
            {
                String msg = "The name in the registry doesn't match the name in the file!["+name+"]["+viewsName+"]";
                log.error(msg);
                throw new ConfigurationException(msg);
            }
            
            // Do these first so the view can check their altViews against them 
            ViewLoader.getViewDefs(rootDOM, viewDefs, views, doMapDefinitions);

            verifyViewsAndViewDefs(altViewsViewDefName);
            
            
        } else
        {
            String msg = "The root element for the document was null!";
            log.error(msg);
            throw new ConfigurationException(msg);
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
                if (av.getViewDef() == null)
                {
                    String viewDefName = altViewsViewDefName.get(av);
                    if (StringUtils.isNotEmpty(viewDefName))
                    {
                        ViewDefIFace referredToViewDef = viewDefs.get(viewDefName);
                        if (referredToViewDef != null)
                        {
                            av.setViewDef(referredToViewDef);
                        } else
                        {
                            log.error("AltView referrs to a non-existent view with name["+av.getViewDefName()+"]");
                        }
                        
                    } else
                    {
                        log.error("Couldn't find the ViewDef Name for the AltView!");
                    }
                }
            }
        }
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#toXML(java.lang.StringBuffer)
     */
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
            if (viewDef instanceof FormViewDef)
            {
                if (((FormViewDef)viewDef).getDefinitionName() != null && ((FormViewDef)viewDef).getDefinitionName().equals("Collectors"))
                {
                    int x = 0;
                    x++;
                }
            }
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
        loadDOM(XMLHelper.readFileToDOM4J(fileInputStream), false, true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewSetIFace#compareTo(edu.ku.brc.ui.forms.persist.ViewSet)
     */
    public int compareTo(ViewSetIFace obj)
    {
        return name.compareTo(obj.getName());
    }
}
