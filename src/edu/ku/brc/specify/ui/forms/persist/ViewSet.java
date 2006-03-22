/* Filename:    $RCSfile: ViewSet.java,v $
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
package edu.ku.brc.specify.ui.forms.persist;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Vector;

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
    private final static Logger  log = Logger.getLogger(ViewSet.class);

    private static FormView comparable = new FormView();
    
    private String           name     = null;
    private String           fileName = null;
    private Vector<FormView> views    = null;
    
    /**
     * Default Constructor
     *
     */
    public ViewSet()
    {
    }


    /**
     * Constructor 
     * @param name name of view set
     * @param fileName the filename it came from
     * @param views the list of views
     */
    public ViewSet(final String name, final String fileName)
    {
        this.name     = name;
        this.fileName = fileName;
    }

    /**
     * Cleans up intneral data 
     */
    public void cleanUp()
    {
        if (views != null)
        {
            for (FormView fv : views)
            {
                fv.cleanUp();
            }
            views.clear();
            views = null; // will force it to be reloaded.
        }
    }
    
    /**
     * Added a form to the view set
     * @param formView the form to be added
     */
    public void add(final FormView formView)
    {
        loadViews();
        views.add(formView);
    }
    
    /**
     * Loads the view from the file
     */
    protected void loadViews()
    {
        if (views == null)
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
            }
        }
    }
    
    /**
     * Gets form
     * @param id id of form to be trieved
     * @return the form or null if it isn't found 
     */
    public FormView getForm(final Integer id)
    {
        loadViews();

        comparable.setId(id);
        int inx = Collections.binarySearch(views, comparable);  
        return inx > -1 ? views.elementAt(inx) : null;
    }

    /**
     * Get the views. It loads them if they have not been loaded yet.
     * @return the vector of all the view in the ViewSet
     */
    public Vector<FormView> getViews()
    {
        loadViews();
        return views;
    }

    /**
     * Sets the Views
     * @param views the vector of new views
     */
    public void setViews(final Vector<FormView> views)
    {
        this.views = views;
        
        Collections.sort(views);
    }

    /**
     * Gets the name
     * @return the name of the viewset
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name
     * @param name the name of the viewset
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * Load an XML View File from a stream if the ViewSet is not unique than it throws and exception
     * @param fileInputStream a file input stream to read the DOM4J from
     * @throws Exception on various errors
     */
    protected void loadViewFile(final FileInputStream fileInputStream) throws Exception
    {
        Element root = XMLHelper.readFileToDOM4J(fileInputStream);
        if (root != null)
        {
            Vector<FormView> newViews = new Vector<FormView>(); // will eventually be moved to where it can be reused
            
            // Note this will check for the uniqueness of the ViewSet's name
            // so we can assume the ViewSet is unique (throws an exception if not unique)
            String viewsName = FormViewFactory.getViews(root, newViews, true);
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
