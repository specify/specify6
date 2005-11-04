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

import edu.ku.brc.specify.ui.forms.persist.*;

import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import edu.ku.brc.specify.exceptions.ConfigurationException;

public class ViewMgr
{
    // Statics
    private final static Logger log        = Logger.getLogger(ViewMgr.class);
    private static ViewMgr      instance   = new ViewMgr();
    private static SAXReader    saxReader  = null;
    
    
    // Data Members
    private Hashtable<String, ViewSet> viewsHash = new Hashtable<String, ViewSet>();
    
    private FormViewFactory  formViewFactory = FormViewFactory.getInstance();
    
    /**
     * Private Constructor
     *
     */
    private ViewMgr()
    {
    }
    
    /**
     * 
     * @return the singleton for the ViewMgr
     */
    public static ViewMgr getInstance()
    {
        return instance;
    }
    
    /**
     * This is used mostly for testing
     *
     */
    public void clearAll()
    {
        viewsHash.clear();
    }
    
     /**
     * 
     * @param aFile
     * @return
     */
    public static org.dom4j.Document readFileToDOM4J(FileInputStream aFileInputStream) throws Exception
    {
        if (saxReader == null)
        {
            saxReader= new SAXReader();
        }
        
        saxReader.setValidation(true);
        saxReader.setFeature("http://apache.org/xml/features/validation/schema", true);
        saxReader.setFeature("http://xml.org/sax/features/validation", true);
        saxReader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", 
                           (FormViewFactory.class.getResource("../form.xsd")).getPath());
        
        return saxReader.read( aFileInputStream );
    }

    /**
     * 
     * @param aPath
     * @throws Exception
     */
    public void loadViewFile(String aPath) throws Exception
    {
        loadViewFile(new FileInputStream(aPath));
    }
    
    /**
     * 
     * @param aURL
     * @throws Exception
     */
    public void loadViewFile(URL aURL) throws Exception
    {
        loadViewFile(new FileInputStream(aURL.getFile()));
    }
    
    /**
     * Checks all the view "sets" to see if the name has already been used
     * @param aViewSetName name of set of views (file of views)
     * @return returns whether a a "set" of views have been read. A "set" of views come from a single file.
     */
    public boolean isViewSetNameInUse(String aViewSetName)
    {
        return viewsHash.get(aViewSetName) != null;
    }
    
    /**
     * Checks to see a View Set and View's Id is already in use
     * @param aViewSetName the name of a view set
     * @param aViewId the id of an individual view
     * @return return true if the ViewSet/ViewId has been registered
     */
    public boolean isViewInUse(String aViewSetName, Integer aViewId)
    {
         return getView(aViewSetName, aViewId) != null;
    }
    
    /**
     * 
     * @param aViewSetName the view set name of the view
     * @param aViewId the id of the view
     * @return the FormView from a view set by id 
     */
    public FormView getView(String aViewSetName, Integer aViewId)
    {
        ViewSet viewSet = viewsHash.get(aViewSetName);
        if (viewSet != null)
        {
            return viewSet.getById(aViewId);
        }
        return null; 
    }
    
    /**
     * 
     * @param aViewSetName the name of the view set
     * @return ViewSet containing the hashtable of FormViews for this view set (this is not a copy)
     */
    public ViewSet getViews(String aViewSetName)
    {
        return viewsHash.get(aViewSetName);        
    }

    
    /**
     * 
     * @param aViewSetName
     * @param aList
     * @throws Exception
     */
    public void validate() throws Exception
    {
        ViewMgr viewMgr = ViewMgr.getInstance();
        
        for (Enumeration e=viewsHash.keys();e.hasMoreElements();)
        {
            String viewSetName = (String)e.nextElement();
            
            ViewSet viewSet = viewsHash.get(viewSetName);
             
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
                                if (!viewMgr.isViewInUse(formView.getViewSetName(), cellSV.getId()))
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
     * @param aFIS a file input stream to read the DOM4J from
     * @throws Exception on various errors
     */
    public void loadViewFile(FileInputStream aFIS) throws Exception
    {
        org.dom4j.Document document = readFileToDOM4J(aFIS);
        Element            root     = document.getRootElement();
        if (root != null)
        {
            Vector<FormView> views = new Vector<FormView>(); // will eventually be moved to where it can be reused
            
            // Note this will check for the uniqueness of the view sets name
            // so we can assume the ViewSet is unique (throws an exception if not unique)
            String viewsName = formViewFactory.getViews(root, views, true);
            
            ViewSet viewSet = new ViewSet(viewsName);
            viewSet.setViews(views);
            
            // Register all views for the view set
            viewsHash.put(viewsName, viewSet);
                
        } else
        {
            String msg = "The root element for the document was null!";
            log.error(msg);
            throw new ConfigurationException(msg);
        }
        
    }

}
