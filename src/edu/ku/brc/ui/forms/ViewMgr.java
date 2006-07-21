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
package edu.ku.brc.ui.forms;

import static edu.ku.brc.specify.helpers.XMLHelper.getAttr;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.ku.brc.specify.exceptions.ConfigurationException;
import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.forms.persist.ViewSet;

/**
 * Reads the Form Registry. The forms are loaded when needed and onlu one ViewSet can be the "core" ViewSet which is where most of the forms
 * reside. This could also be thought of as the "default" set of forms.
 * @author rods
 *
 */
public class ViewMgr
{
    // Statics
    public  static final String  defaultViewSetName     = "Default";
    
    private static final Logger  log        = Logger.getLogger(ViewMgr.class);
    private static final ViewMgr instance;
    
    private static SAXReader saxReader  = null;
    
    static {
        instance   = new ViewMgr();
        instance.init();
    }
    
    
    // Data Members
    protected Hashtable<String, ViewSet> viewsHash      = new Hashtable<String, ViewSet>();
    protected ViewSet                    defaultViewSet = null;
    
    /**
     * protected Constructor
     *
     */
    protected ViewMgr()
    {
    }
    
    /**
     * Reads the Form Registry. The forms are loaded when needed and onlu one ViewSet can be the "core" ViewSet which is where most of the forms
     * reside. This could also be thought of as the "default" set of forms.
     */
    protected void init()
    {
        try
        {
            org.dom4j.Document document = readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath("viewset_registry.xml")));
            Element            root     = document.getRootElement();
            if (root != null)
            {
                for ( Iterator i = root.elementIterator( "file" ); i.hasNext(); ) 
                {
                    Element fileElement = (Element) i.next();
                    String  name        = getAttr(fileElement, "name", null);
                    if (!isViewSetNameInUse(name))
                    {
                        String typeStr   = getAttr(fileElement, "type", "system");
                        String title     = getAttr(fileElement, "title", null);
                        String fileName  = getAttr(fileElement, "file", null);
                        String databases = getAttr(fileElement, "databases", "");
                        String users     = getAttr(fileElement, "users", "");
                        
                        // these can go away once we validate the XML
                        if (StringUtils.isEmpty(typeStr))
                        {
                            throw new RuntimeException("ViewSet type cannot be null!");
                        }
                        if (StringUtils.isEmpty(title))
                        {
                            throw new RuntimeException("ViewSet title cannot be null!");
                        }                       
                        if (StringUtils.isEmpty(fileName))
                        {
                            throw new RuntimeException("ViewSet file cannot be null!");
                        } else
                        {
                            String path = XMLHelper.getConfigDirPath(fileName);
                            File   file = new File(path);
                            if (file == null || !file.exists())
                            {
                                throw new RuntimeException("ViewSet file cannot be found at["+path+"]");
                            }
                        }
                        
                        ViewSet viewSet = new ViewSet(ViewSet.parseType(typeStr), name, title, fileName, databases, users);
                        viewsHash.put(viewSet.getName(), viewSet);
                        
                    } else
                    {
                        log.error("ViewSet Name["+name+"] is in use.");
                    }
                }
            } else
            {
                String msg = "The root element for the document was null!";
                log.error(msg);
                throw new ConfigurationException(msg);
            } 
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
    }
    
    /**
     * Sets a created viewset as the "default" view set
     * @param name the name of the viewset to be identified as the "default" 
     */
    public static void setAsDefaultViewSet(final String name)
    {
        ViewSet viewSet = instance.viewsHash.get(name);
        if (viewSet == null)
        {
            throw new RuntimeException("Couldn't find viewSet["+name+"] to make it the default.");
        }
        instance.defaultViewSet = viewSet;
    }
    
    /**
     * This is used mostly for testing
     *
     */
    public static void clearAll()
    {
        reset();
        instance.viewsHash.clear();
    }
    
    /**
     * Clears all the ViewSets so they are reloaded, but it doesn't re-read the config file
     *
     */
    public static void reset()
    {
        for (Enumeration<ViewSet> e=instance.viewsHash.elements();e.hasMoreElements();)
        {
            e.nextElement().cleanUp();           
        }
    }
    
    /**
     * Reads a DOM from a string and returns the root element of the DOM
     * @param fileInputStream
     * @return returns a document from a DOM file input stream
     */
    public static org.dom4j.Document readFileToDOM4J(final FileInputStream fileInputStream) throws Exception
    {
        if (saxReader == null)
        {
            saxReader = new SAXReader();
        }
        
        boolean doValidation = false;
        saxReader.setValidation(doValidation);
        if (doValidation)
        {
            saxReader.setValidation(true);
            saxReader.setFeature("http://apache.org/xml/features/validation/schema", true);
            saxReader.setFeature("http://xml.org/sax/features/validation", true);
            saxReader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", 
                               XMLHelper.getConfigDirPath("form.xsd"));
        }
        return saxReader.read( fileInputStream );
    }

    /**
     * Checks all the view "sets" to see if the name has already been used
     * @param viewSetName name of set of views (file of views)
     * @return returns whether a a "set" of views have been read. A "set" of views come from a single file.
     */
    public static boolean isViewSetNameInUse(final String viewSetName)
    {
        return instance.viewsHash.get(viewSetName) != null;
    }
    
    /**
     * Checks to see a View Set and View's Name is already in use
     * @param viewSetName the name of a view set
     * @param viewName the name of the view
     * @return return true if the ViewSet/ViewId has been registered
     */
    public static boolean isViewInUse(final String viewSetName, final String viewName)
    {
         return getView(viewSetName, viewName) != null;
    }
    
    /**
     * Gets a View by ViewSet name and View Name
     * @param viewSetName the view set name of the view (can be null or "default" for the current ViewSet)
     * @param viewName the name of the view
     * @return the FormView from a view set by id 
     */
    public static View getView(final String viewSetName, final String viewName)
    {
        ViewSet viewSet;
        
        if (viewSetName == null || viewSetName.equals(defaultViewSetName))
        {
            if (instance.defaultViewSet != null)
            {
                viewSet = instance.defaultViewSet;
                
            } else
            {
                String msg = "Asking for the 'default' ViewSet and one has not been set!";
                log.error(msg);
                throw new RuntimeException(msg);
            }
        } else
        {
            viewSet = instance.viewsHash.get(viewSetName);
        }
        
        if (viewSet != null)
        {
            return viewSet.getView(viewName);
        }
        return null; 
    }
    
    /**
     * Returns a viewset by name
     * @param viewSetName the name of the view set
     * @return ViewSet containing the hashtable of FormViews for this view set (this is not a copy)
     */
    public static ViewSet getViews(final String viewSetName)
    {
        return instance.viewsHash.get(viewSetName);        
    }
   
    /**
     * Returns a list of all the ViewSets
     * @return a list of all the ViewSets
     */
    public static List<ViewSet> getViewSets()
    {
        return Collections.list(instance.viewsHash.elements());
    }
    
    /**
     * Returns a list of all the non-System ViewSets
     * @return a list of all the non-System ViewSets
     */
    public static List<ViewSet> getUserViewSets()
    {
        List<ViewSet> list = new ArrayList<ViewSet>();
        
        for (Enumeration<ViewSet> e=instance.viewsHash.elements();e.hasMoreElements();)
        {
            ViewSet vs = e.nextElement();
            if (vs.getType() == ViewSet.Type.User)
            {
                list.add(vs);
            }
        }

        return list;
    }
    
    /**
     * Returns a list of all the non-System ViewSets that are for this user and database
     * @param username the username to be checked
     * @param database the database
     * @return a list of all the non-System ViewSets that are for this user and database
     */
    public static List<ViewSet> getViewSetsForUserAndDatabase(final String username, final String database)
    {
        List<ViewSet> list = new ArrayList<ViewSet>();
        
        for (Enumeration<ViewSet> e=instance.viewsHash.elements();e.hasMoreElements();)
        {
            ViewSet vs = e.nextElement();
            if (vs.getType() == ViewSet.Type.User)
            {
                boolean userOK = StringUtils.isNotEmpty(username) && Collections.binarySearch(vs.getUsers(), username) > -1;
                if (!userOK && vs.getUsers().size() == 1 && vs.getUsers().get(0).equalsIgnoreCase("all"))
                {
                    userOK = true;
                }
                //log.debug("["+vs.getUsers()+"]["+username+"]["+vs.getDatabases()+"]["+database+"]");
                if (userOK && StringUtils.isNotEmpty(database) && Collections.binarySearch(vs.getDatabases(), database) > -1)
                {
                    list.add(vs);
                }
            }
        }

        return list;
    }
    
}
