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
import java.util.Hashtable;
import java.util.Map;

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

public class ViewSet implements Comparable<ViewSet>
{
    private static final Logger  log = Logger.getLogger(ViewSet.class);
    private static boolean ALWAYS_LOAD = true; // XXX PREF

    public enum Type {System, User}


    protected Type                       type              = Type.User;
    protected String                     name              = null;
    protected String                     title             = null;
    protected String                     fileName          = null;
    protected File                       dirPath           = null;

    protected boolean                    hasLoadedViews    = false;
    protected Hashtable<String, View>    transientViews    = null;
    protected Hashtable<String, ViewDef> transientViewDefs = null;
    protected Hashtable<String, View>    views             = new Hashtable<String, View>();
    protected Hashtable<String, ViewDef> viewDefs          = new Hashtable<String, ViewDef>();

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
    public ViewSet(final Element rootDOM) throws Exception
    {
        loadDOM(rootDOM, true);
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

    /**
     * Cleans up intneral data.
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

    /**
     * Gets a view by name.
     * @param nameStr name of view to be retrieved
     * @return the view or null if it isn't found
     */
    public View getView(final String nameStr)
    {
        loadViews();

        return views.get(nameStr);
    }

    /**
     * Get all the views. It loads them if they have not been loaded yet.
     * @return the vector of all the view in the ViewSet
     */
    public Map<String, View> getViews()
    {
        loadViews();
        
        return views;
    }

    /**
     * Returns all the ViewDefs.
     * @return all the ViewDefs.
     */
    public Hashtable<String, ViewDef> getViewDefs()
    {
        return viewDefs;
    }

    /**
     * Gets the name.
     * @return the name of the viewset
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the type of ViewSet it is.
     * @return the type of ViewSet it is
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Sets the name.
     * @param name the name of the viewset
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * Returns the title.
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Returns file name (no path)
     * @return file name (no path)
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * Indicates that is contains the core set of forms that can be referred in other places with specifying the viewset name.
     * @return that is contains the core set of forms that can be referred in other places with specifying the viewset name
     */
    public boolean isSystem()
    {
        return type == Type.System;
    }
    
    /**
     * Adds a dynamic or transient View; which is a View that is not read from the database or a file.
     * @param view the in memory View
     */
    public void addTransientView(final View view)
    {
        if (transientViews == null)
        {
            transientViews = new Hashtable<String, View>();
            
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
    public void addTransientViewDef(final ViewDef viewDef)
    {
        if (transientViewDefs == null)
        {
            transientViewDefs = new Hashtable<String, ViewDef>();
            
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
    public void removeTransientView(final View view)
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
    public void removeTransientViewDef(final ViewDef viewDef)
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
     * @param rootDOM the root
     */
    protected void loadDOM(final Element rootDOM, final boolean doSetName) throws Exception
    {
        if (rootDOM != null)
        {
            viewDefs.clear();
            views.clear();

            // Do these first so the view can check their altViews against them            
            ViewLoader.getViewDefs(rootDOM, viewDefs);

            String viewsName = ViewLoader.getViews(rootDOM, views, viewDefs);
            if (doSetName)
            {
                name = viewsName;

            } else if (!viewsName.equals(name))
            {
                String msg = "The name in the registry doesn't match the name in the file!["+name+"]["+viewsName+"]";
                log.error(msg);
                throw new ConfigurationException(msg);
            }


        } else
        {
            String msg = "The root element for the document was null!";
            log.error(msg);
            throw new ConfigurationException(msg);
        }
        hasLoadedViews = true;
    }

    /**
     * Load an XML View File from a stream if the ViewSet is not unique than it throws and exception.
     * @param fileInputStream a file input stream to read the DOM4J from
     * @throws Exception on various errors
     */
    protected void loadViewFile(final FileInputStream fileInputStream) throws Exception
    {
        loadDOM(XMLHelper.readFileToDOM4J(fileInputStream), false);
    }

    /**
     * Comparator.
     * @param obj the obj to compare
     * @return 0,1,-1
     */
    public int compareTo(ViewSet obj)
    {
        return name.compareTo(obj.name);
    }
}
