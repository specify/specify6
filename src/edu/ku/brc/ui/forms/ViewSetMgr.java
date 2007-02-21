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

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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

import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.forms.persist.ViewSet;

/**
 * Reads a registry xml file in a directory that can contain one or more ViewSets. <br>
 * Each registry entry is a ViewSet (and a file) and a ViewSet contains one or more Views.<br>
 * 
 * @code_status Complete
 *
 * @author rods
 *
 */
public class ViewSetMgr
{
    // Statics
    private static final String  REGISTRY_FILENAME  = "viewset_registry.xml"; 
    private static final Logger  log       = Logger.getLogger(ViewSetMgr.class);
    
    private static SAXReader saxReader  = null;
    
    // Data Members
    protected Hashtable<String, ViewSet> viewsHash      = new Hashtable<String, ViewSet>();
    protected File                       contextDir     = null;
    protected boolean                    registryExists = false;
    
    /**
     * Constructor.
     *
     */
    public ViewSetMgr()
    {
        // do nothing
    }
    
    /**
     * Constructor with DOM.
     *
     * @param rootDOM the root of the DOM
     */
    public ViewSetMgr(Element rootDOM)
    {
        init(rootDOM);
    }
    
    /**
     * Constructor with File as a dir point to ViewSet.
     * @param contextDir the path to the viewset
     */
    public ViewSetMgr(final File contextDir)
    {
        init(contextDir, true);
    }
    
    /**
     * Constructor with File as a dir point to ViewSet.
     *
     * @param contextDir the path to the viewset
     * @param emptyIsOK true means it is ok if it doesn't exist, false means it MUST exist
     */
    public ViewSetMgr(final File contextDir, final boolean emptyIsOK)
    {
        init(contextDir, emptyIsOK);
    }
    
    /**
     * Adds a new ViewSet to the registry
     * @param typeStr the type of viewset
     * @param name the name
     * @param title the title
     * @param fileName the file name (no path)
     */
    public void addViewSetDef(final String typeStr, final String name, final String title, final String fileName)
    {
        if (!isViewSetNameInUse(name))
        {
            ViewSet viewSet = new ViewSet(ViewSet.parseType(typeStr), name, title, fileName, contextDir);
            viewsHash.put(viewSet.getName(), viewSet);
            
        } else
        {
            throw new RuntimeException("ViewSet Name["+name+"] is in use.");
        }
    }

    /**
     * Adds a ViewSet that is already built.
     * @param viewSet the viewset
     */
    public void addViewSet(final ViewSet viewSet)
    {
        viewsHash.put(viewSet.getName(), viewSet);
    }
    
    /**
     * Clear and cleans all the view sets
     *
     */
    public void clearAll()
    {
        reset();
        viewsHash.clear();
    }
    
    /**
     * Clears all the ViewSets so they are reloaded, but it doesn't re-read the config file.
     */
    public void reset()
    {
        for (Enumeration<ViewSet> e=viewsHash.elements();e.hasMoreElements();)
        {
            e.nextElement().cleanUp();           
        }
    }
    
    /**
     * Checks all the view "sets" to see if the name has already been used.
     * @param viewSetName name of set of views (file of views)
     * @return returns whether a a "set" of views have been read. A "set" of views come from a single file.
     */
    public boolean isViewSetNameInUse(final String viewSetName)
    {
        return viewsHash.get(viewSetName) != null;
    }
    
    /**
     * Checks to see a View Set and View's Name is already in use.
     * @param viewSetName the name of a view set
     * @param viewName the name of the view
     * @return return true if the ViewSet/ViewId has been registered
     */
    public boolean isViewInUse(final String viewSetName, final String viewName)
    {
         return getView(viewSetName, viewName) != null;
    }
    
    /**
     * Gets a View by ViewSet name and View Name.
     * @param viewSetName the view set name of the view (can be null or "default" for the current ViewSet)
     * @param viewName the name of the view
     * @return the FormView from a view set by id 
     */
    public View getView(final String viewSetName, final String viewName)
    {
        ViewSet viewSet = viewsHash.get(viewSetName);
        if (viewSet != null)
        {
            return viewSet.getView(viewName);
        }
        return null; 
    }
    
    /**
     * Returns a viewset by name.
     * @param viewSetName the name of the view set
     * @return ViewSet containing the hashtable of FormViews for this view set (this is not a copy)
     */
    public ViewSet getViewSet(final String viewSetName)
    {
        if (viewSetName == null)
        {
            List<ViewSet> userVS = getUserViewSets();
            if (userVS.size() == 1)
            {
                return userVS.get(0);
                
            }
            // else
            log.error("User ViewSets:");
            for (ViewSet vs : userVS)
            {
                log.error(vs.getName());
            }
            throw new RuntimeException("There are multiple User ViewSets so I don't know which one to choose for the default.");
        }
        return viewsHash.get(viewSetName);        
    }
   
    /**
     * Returns a list of all the ViewSets
     * @return a list of all the ViewSets
     */
    public List<ViewSet> getViewSets()
    {
        return Collections.list(viewsHash.elements());
    }
    
    /**
     * Returns a list of all the non-System ViewSets.
     * @return a list of all the non-System ViewSets
     */
    public List<ViewSet> getUserViewSets()
    {
        List<ViewSet> list = new ArrayList<ViewSet>();
        
        for (Enumeration<ViewSet> e=viewsHash.elements();e.hasMoreElements();)
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
     * Sets the directory location for the ViewSet
     * @param contextDir the directory location for the ViewSet
     */
    public void setContextDir(File contextDir)
    {
        this.contextDir = contextDir;
    }

    /**
     * Return the directory location for the ViewSet
     * @return the directory location for the ViewSet
     */
    public File getContextDir()
    {
        return contextDir;
    }
    
    /**
     * Returns whether the registry file was present in the directory.
     * @return whether the registry file was present in the directory.
     */
    public boolean isRegistryExists()
    {
        return registryExists;
    }

    /**
     * Reads the Form Registry. The forms are loaded when needed and onlu one ViewSet can be the "core" ViewSet which is where most of the forms
     * reside. This could also be thought of as the "default" set of forms.
     * 
     * @param contextDir the directory in which load the view sets
     */
    protected void init(final Element rootDOM)
    {
        if (rootDOM != null)
        {
            for ( Iterator<?> i = rootDOM.elementIterator( "file" ); i.hasNext(); ) 
            {
                Element fileElement = (Element) i.next();
                String  name        = getAttr(fileElement, "name", null);
                if (!isViewSetNameInUse(name))
                {
                    String typeStr   = getAttr(fileElement, "type", "system");
                    String title     = getAttr(fileElement, "title", null);
                    String fileName  = getAttr(fileElement, "file", null);
                    
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
                    }
                    // else
                    File viewSetFile = new File(contextDir.getAbsoluteFile() + File.separator + fileName);
                    if (!viewSetFile.exists())
                    {
                        throw new RuntimeException("ViewSet file cannot be found at["+viewSetFile.getAbsolutePath()+"]");
                    }
                    
                    ViewSet viewSet = new ViewSet(ViewSet.parseType(typeStr), name, title, fileName, contextDir);
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

    }

    /**
     * Reads the Form Registry. The forms are loaded when needed and onlu one ViewSet can be the "core" ViewSet which is where most of the forms
     * reside. This could also be thought of as the "default" set of forms.
     * 
     * @param contextDirArg the directory in which load the view sets
     */
    protected void init(final File contextDirArg, final boolean emptyIsOK)
    {
        this.contextDir = contextDirArg;
        registryExists = false;
        
        if (contextDirArg != null)
        { 
            File vsRegFile = new File(contextDirArg.getAbsoluteFile() + File.separator + REGISTRY_FILENAME);
            if (vsRegFile.exists())
            {
                registryExists = true;

                try
                {
                    org.dom4j.Document document = readFileToDOM4J(new FileInputStream(vsRegFile));
                    Element            root     = document.getRootElement();
                    
                    init(root);
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    log.error(ex);
                }
            } else if (!emptyIsOK)
            {
                throw new ConfigurationException(vsRegFile.getAbsoluteFile() + " doesn't exist.");
            }
        }
    }
    
    /**
     * Writes the viewset registry file.
     * @throws IOException on error
     */
    public void save() throws IOException
    {
        Writer output = null;
        try
        {
            output = new BufferedWriter(new FileWriter(contextDir.getAbsoluteFile() + File.separator + REGISTRY_FILENAME));
            output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            output.write("<files>\n");
            for (Enumeration<ViewSet> e = viewsHash.elements(); e.hasMoreElements();)
            {
                ViewSet viewSet = e.nextElement();
                output.write("      <file type=\""+
                                    viewSet.getType().toString()+"\" name=\""+
                                    viewSet.getName()+"\" title=\""+
                                    viewSet.getTitle()+"\" file=\""+
                                    viewSet.getFileName()+"\"/>\n");

            }
            output.write("</files>\n");

        } finally
        {
            // flush and close both "output" and its underlying FileWriter
            if (output != null)
            {
                output.flush();
                output.close();
            }
        }

    }
    
    /**
     * Reads a DOM from a string and returns the root element of the DOM
     * 
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
        /*if (doValidation)
        {
            saxReader.setValidation(true);
            saxReader.setFeature("http://apache.org/xml/features/validation/schema", true);
            saxReader.setFeature("http://xml.org/sax/features/validation", true);
            saxReader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", 
                               XMLHelper.getConfigDirPath("form.xsd"));
        }*/
        return saxReader.read( fileInputStream );
    }


    
}
