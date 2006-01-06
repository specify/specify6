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

import java.io.FileInputStream;
import java.net.URL;
import java.util.*;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.ku.brc.specify.exceptions.ConfigurationException;
import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.ui.forms.persist.FormAltView;
import edu.ku.brc.specify.ui.forms.persist.FormCell;
import edu.ku.brc.specify.ui.forms.persist.FormCellSubView;
import edu.ku.brc.specify.ui.forms.persist.FormFormView;
import edu.ku.brc.specify.ui.forms.persist.FormRow;
import edu.ku.brc.specify.ui.forms.persist.FormView;
import edu.ku.brc.specify.ui.forms.persist.*;
import edu.ku.brc.specify.ui.forms.persist.ViewSet;

public class ViewMgr
{
    // Statics
    private final static Logger     log        = Logger.getLogger(ViewMgr.class);
    private static ViewMgr          instance   = new ViewMgr();
    private static SAXReader        saxReader  = null;    
    
    
    // Data Members
    private Hashtable<String, ViewSet> viewsHash = new Hashtable<String, ViewSet>();
    
    /**
     * protected Constructor
     *
     */
    protected ViewMgr()
    {
    }
    
    /**
     * This is used mostly for testing
     *
     */
    public static void clearAll()
    {
        instance.viewsHash.clear();
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
            saxReader= new SAXReader();
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
     * Loads a view file from a path
     * @param path the path to the view file
     * @throws Exception file io exceptions
     */
    public static void loadViewFile(final String path) throws Exception
    {
        loadViewFile(new FileInputStream(path));
    }
    
    /**
     * Loads a View file from an URL
     * @param url the url location
     * @throws Exception and errors
     */
    public static void loadViewFile(final URL url) throws Exception
    {
        loadViewFile(new FileInputStream(url.getFile()));
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
     * Checks to see a View Set and View's Id is already in use
     * @param viewSetName the name of a view set
     * @param viewId the id of an individual view
     * @return return true if the ViewSet/ViewId has been registered
     */
    public static boolean isViewInUse(final String viewSetName, final Integer viewId)
    {
         return getView(viewSetName, viewId) != null;
    }
    
    /**
     * Gets a FormView by set name and id
     * @param viewSetName the view set name of the view
     * @param viewId the id of the view
     * @return the FormView from a view set by id 
     */
    public static FormView getView(final String viewSetName, final Integer viewId)
    {
        ViewSet viewSet = instance.viewsHash.get(viewSetName);
        if (viewSet != null)
        {
            return viewSet.getById(viewId);
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
     * Validates the views and subview
     * @throws Exception XXX
     */
    public static void validate() throws Exception
    {
        for (Enumeration e=instance.viewsHash.keys();e.hasMoreElements();)
        {
            String viewSetName = (String)e.nextElement();
            
            ViewSet viewSet = instance.viewsHash.get(viewSetName);
             
           // Validate all the Alt Views and SubViews
            for (FormView view : viewSet.getViews())
            {
                for (FormAltView altView : view.getAltViews())
                {
                    if (viewSet.getById(altView.getId()) == null)
                    {
                        String msg = "View Set ["+viewSetName+"] ["+view.getId()+"] has invalid Alt View Id ["+altView.getId()+"]";
                        log.error(msg);
                        throw new ConfigurationException(msg);
                        
                    } else if (view.getId() == altView.getId())
                    {
                        String msg = "View Set ["+viewSetName+"] ["+view.getId()+"] cannot be its own AltView.";
                        log.error(msg);
                        throw new ConfigurationException(msg);
                    }
                }
                
                if (view.getType() == FormView.ViewType.form) // faster than instance of
                {
                    FormFormView formView = (FormFormView)view;
                    for (FormRow row : formView.getRows())
                    {
                        for (FormCell cell : row.getCells())
                        {
                            if (cell.getType() == FormCell.CellType.subview) // faster than instance of
                            {
                                FormCellSubView cellSV = (FormCellSubView)cell;
                                if (!isViewInUse(formView.getViewSetName(), cellSV.getId()))
                                {
                                    String msg = "View Set ["+viewSetName+"] ["+view.getId()+"] Cell SubView Id ["+cellSV.getId()+"] cannot be found.";
                                    log.error(msg);
                                    throw new ConfigurationException(msg);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
   
    /**
     * Load an XML View File from a stream if the ViewSet is not unique than it throws and exception
     * @param fileInputStream a file input stream to read the DOM4J from
     * @throws Exception on various errors
     */
    public static void loadViewFile(FileInputStream fileInputStream) throws Exception
    {
        org.dom4j.Document document = readFileToDOM4J(fileInputStream);
        Element            root     = document.getRootElement();
        if (root != null)
        {
            Vector<FormView> views = new Vector<FormView>(); // will eventually be moved to where it can be reused
            
            // Note this will check for the uniqueness of the view sets name
            // so we can assume the ViewSet is unique (throws an exception if not unique)
            String viewsName = FormViewFactory.getViews(root, views, true);
            
            ViewSet viewSet = new ViewSet(viewsName);
            viewSet.setViews(views);
            
            // Register all views for the view set
            instance.viewsHash.put(viewsName, viewSet);
                
        } else
        {
            String msg = "The root element for the document was null!";
            log.error(msg);
            throw new ConfigurationException(msg);
        }
        
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
