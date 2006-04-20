/* Filename:    $RCSfile: ViewMgr.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui.forms;

import static edu.ku.brc.specify.helpers.XMLHelper.getAttr;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.ku.brc.specify.exceptions.ConfigurationException;
import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.ui.forms.persist.View;
import edu.ku.brc.specify.ui.forms.persist.ViewSet;

/**
 * Reads the Form Registry. The forms are loaded when needed and onlu one ViewSet can be the "core" ViewSet which is where most of the forms
 * reside. This could also be thought of as the "default" set of forms.
 * @author rods
 *
 */
public class ViewMgr
{
    // Statics
    private final static Logger  log        = Logger.getLogger(ViewMgr.class);
    private static final ViewMgr instance;
    
    private static SAXReader saxReader  = null;
    
    static {
        instance   = new ViewMgr();
        instance.init();
    }
    
    
    // Data Members
    protected Hashtable<String, ViewSet> viewsHash   = new Hashtable<String, ViewSet>();
    protected ViewSet                    coreViewSet = null;
    
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
            org.dom4j.Document document = readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath("forms_registry.xml")));
            Element            root     = document.getRootElement();
            if (root != null)
            {
                for ( Iterator i = root.elementIterator( "file" ); i.hasNext(); ) 
                {
                    Element fileElement = (Element) i.next();
                    String  name        = getAttr(fileElement, "name", null);
                    if (!isViewSetNameInUse(name))
                    {
                        boolean  isCore = getAttr(fileElement, "core", false);
                        if (coreViewSet != null && isCore)
                        {
                            log.error("Ignoring 'core' attribute for view ["+name+"] because there is already one set to true.");
                            isCore = false;
                        }
                        
                        ViewSet viewSet = new ViewSet(name, getAttr(fileElement, "file", null), isCore);
                        if (isCore)
                        {
                            coreViewSet = viewSet;
                        }
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
     * This is used mostly for testing
     *
     */
    public static void clearAll()
    {
        for (Enumeration e=instance.viewsHash.elements();e.hasMoreElements();)
        {
            ((ViewSet)e.nextElement()).cleanUp();           
        }
        instance.viewsHash.clear();
    }
    
    /**
     * Clears all the ViewSets so they are reloaded, but it doesn't re-read the config file
     *
     */
    public static void reset()
    {
        for (Enumeration e=instance.viewsHash.elements();e.hasMoreElements();)
        {
            ((ViewSet)e.nextElement()).cleanUp();           
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
     * @param viewSetName the view set name of the view
     * @param viewName the name of the view
     * @return the FormView from a view set by id 
     */
    public static View getView(final String viewSetName, final String viewName)
    {
        ViewSet viewSet;
        
        if (viewSetName == null)
        {
            if (instance.coreViewSet != null)
            {
                viewSet = instance.coreViewSet;
            } else
            {
                log.error("Asking for 'core' ViewSet and one has not been defined!");
                viewSet = instance.viewsHash.get(viewSetName);
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
     * @return Returns a list of all the ViewSets
     */
    public static List<ViewSet> getViewSets()
    {
        return Collections.list(instance.viewsHash.elements());
    }
    
}
