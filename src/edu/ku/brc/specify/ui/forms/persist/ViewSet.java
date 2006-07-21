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
package edu.ku.brc.specify.ui.forms.persist;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.specify.exceptions.ConfigurationException;
import edu.ku.brc.specify.helpers.XMLHelper;

/**
 * Class that manages all the forms for a given view set (which is read from a single file)
 *
 * @author Rod Spears <rods@ku.edu>
 */

public class ViewSet
{
    private static final Logger  log = Logger.getLogger(ViewSet.class);
    private static boolean ALWAYS_LOAD = true; // XXX PREF

    public enum Type {System, User}
    
    protected Type             type      = Type.User;
    protected String           name      = null;
    protected String           title     = null;
    protected String           fileName  = null;
    protected List<String>     databases = new ArrayList<String>();
    protected List<String>     users     = new ArrayList<String>();
    
    protected Hashtable<String, View>    views    = null;
    protected Hashtable<String, ViewDef> viewDefs = new Hashtable<String, ViewDef>();
    
    /**
     * Default Constructor.
     *
     */
    public ViewSet()
    {
    }


    /**
     * Constructor. 
     * @param type indicates that is contains the core set of forms that 
     *             can be referred in other places with specifying the viewset name
     * @param name name of view set
     * @param title human readable title (short description)
     * @param fileName the filename of the ViewSet
     * @param databases the databases it can be used with
     * @param users the users that can access it
     */
    public ViewSet(final Type type, 
                   final String name, 
                   final String title, 
                   final String fileName,
                   final String databases,
                   final String users)
    {
        this.type     = type;
        this.name     = name;
        this.title    = title;
        this.fileName = fileName;
        
        String[] items = StringUtils.split(databases, ",");
        Collections.addAll(this.databases, items);
        
        items = StringUtils.split(users, ",");
        Collections.addAll(this.users, items);
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
            views = null; // will force it to be reloaded.
        }
    }
    
    /**
     * Loads the view from the file.
     */
    protected void loadViews()
    {
        if ( ALWAYS_LOAD || views == null)
        {
            try
            {
                loadViewFile(new FileInputStream(XMLHelper.getConfigDirPath(fileName)));
                
            } catch (FileNotFoundException ex)
            {
                log.error(ex);
            } catch (Exception ex)
            {
                log.error(ex);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Gets a view by name.
     * @param name name of view to be retrieved
     * @return the view or null if it isn't found 
     */
    public View getView(final String name)
    {
        loadViews();

        return views.get(name);
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
     * Sets the Views.
     * @param views the vector of new views
     */
    public void setViews(final Hashtable<String, View> views)
    {
        this.views = views;
    }

    /**
     * Sets the ViewDefs.
     * @param viewDefs the vector of new views
     */
    public void setViewDefs(final Hashtable<String, ViewDef> viewDefs)
    {
        this.viewDefs = viewDefs;
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
     * Indicates that is contains the core set of forms that can be referred in other places with specifying the viewset name.
     * @return that is contains the core set of forms that can be referred in other places with specifying the viewset name
     */
    public boolean isSystem()
    {
        return type == Type.System;
    }

    /**
     * Returns a list of databases that the view works with.
     * @return a list of databases that the view works with
     */
    public List<String> getDatabases()
    {
        return databases;
    }

    /**
     * Returns a list of users that can use this form.
     * @return a list of users that can use this form
     */
    public List<String> getUsers()
    {
        return users;
    }

    /**
     * Load an XML View File from a stream if the ViewSet is not unique than it throws and exception.
     * @param fileInputStream a file input stream to read the DOM4J from
     * @throws Exception on various errors
     */
    protected void loadViewFile(final FileInputStream fileInputStream) throws Exception
    {
        Element root = XMLHelper.readFileToDOM4J(fileInputStream);
        if (root != null)
        {
            // Do these first so the view can check their altViews against them
            Hashtable<String, ViewDef> newViewDefs = new Hashtable<String, ViewDef>(); // will eventually be moved to where it can be reused
            ViewLoader.getViewDefs(root, newViewDefs);
            setViewDefs(newViewDefs);
                

            Hashtable<String, View> newViews = new Hashtable<String, View>(); // will eventually be moved to where it can be reused
            
            String viewsName = ViewLoader.getViews(root, newViews, newViewDefs);
            if (!viewsName.equals(name))
            {
                String msg = "The name in the registry doesn't match the name in the file!["+name+"]["+viewsName+"]";
                log.error(msg);
                throw new ConfigurationException(msg);
            }
            setViews(newViews);
            
        } else
        {
            String msg = "The root element for the document was null!";
            log.error(msg);
            throw new ConfigurationException(msg);
        }
    }
}
