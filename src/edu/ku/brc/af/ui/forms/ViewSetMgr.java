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
package edu.ku.brc.af.ui.forms;

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

import edu.ku.brc.af.ui.forms.persist.FormDevHelper;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewSet;
import edu.ku.brc.af.ui.forms.persist.ViewSetIFace;
import edu.ku.brc.exceptions.ConfigurationException;

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
    protected String                     name;
    protected Hashtable<String, ViewSetIFace> viewsHash      = new Hashtable<String, ViewSetIFace>();
    protected File                       contextDir     = null;
    protected boolean                    registryExists = false;
    
    /**
     * Constructor with File as a dir point to ViewSet.
     * @param contextDir the path to the viewset
     */
    public ViewSetMgr(final String name, final File contextDir)
    {
        this.name = name;
        
        init(contextDir, true);
    }
    
    /**
     * Constructor with File as a dir point to ViewSet.
     *
     * @param contextDir the path to the viewset
     * @param emptyIsOK true means it is ok if it doesn't exist, false means it MUST exist
     */
    public ViewSetMgr(final String name, final File contextDir, final boolean emptyIsOK)
    {
        this.name = name;
        init(contextDir, emptyIsOK);
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Adds a new ViewSet to the registry
     * @param typeStr the type of viewset
     * @param nameArg the name
     * @param title the title
     * @param fileName the file name (no path)
     * @param i18NResourceName the resource file name
     */
    public void addViewSetDef(final String typeStr, 
                              final String nameArg, 
                              final String title, 
                              final String fileName,
                              final String i18NResourceName)
    {
        if (!isViewSetNameInUse(nameArg))
        {
            ViewSet viewSet = new ViewSet(ViewSet.parseType(typeStr), nameArg, title, fileName, contextDir);
            viewSet.setI18NResourceName(i18NResourceName);
            
            viewsHash.put(viewSet.getName(), viewSet);
            
        } else
        {
            throw new RuntimeException("ViewSet Name["+nameArg+"] is in use.");
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
        for (Enumeration<ViewSetIFace> e=viewsHash.elements();e.hasMoreElements();)
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
    public ViewIFace getView(final String viewSetName, final String viewName)
    {
        ViewSetIFace viewSet = null;
        if (StringUtils.isNotEmpty(viewSetName))
        {
            viewSet = viewsHash.get(viewSetName);
            
        } else
        {
            // Go searching for it.
            for (ViewSetIFace vs : viewsHash.values())
            {
                for (ViewIFace view : vs.getViews().values())
                {
                    if (view.getName().equals(viewName))
                    {
                        return view;
                    }
                }
            }
        }
        
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
    public ViewSetIFace getViewSet(final String viewSetName)
    {
        if (viewSetName == null)
        {
            List<ViewSetIFace> userVS = getUserViewSets();
            if (userVS.size() == 1)
            {
                return userVS.get(0);
                
            }
            // else
            log.error("User ViewSets:");
            for (ViewSetIFace vs : userVS)
            {
                log.error(vs.getName());
            }
            FormDevHelper.showFormDevError("There are multiple User ViewSets so I don't know which one to choose for the default.");
            return null;
        }
        return viewsHash.get(viewSetName);        
    }
   
    /**
     * Returns a list of all the ViewSets
     * @return a list of all the ViewSets
     */
    public List<ViewSetIFace> getViewSets()
    {
        return Collections.list(viewsHash.elements());
    }
    
    /**
     * Returns a list of all the non-System ViewSets.
     * @return a list of all the non-System ViewSets
     */
    public List<ViewSetIFace> getUserViewSets()
    {
        List<ViewSetIFace> list = new ArrayList<ViewSetIFace>();
        
        for (Enumeration<ViewSetIFace> e=viewsHash.elements();e.hasMoreElements();)
        {
            ViewSetIFace vs = e.nextElement();
            if (vs.getType() == ViewSetIFace.Type.User)
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
                String  fName       = getAttr(fileElement, "name", null);
                if (!isViewSetNameInUse(fName))
                {
                    String typeStr     = getAttr(fileElement, "type", "system");
                    String title       = getAttr(fileElement, "title", null);
                    String fileName    = getAttr(fileElement, "file", null);
                    
                    // these can go away once we validate the XML
                    if (StringUtils.isEmpty(typeStr))
                    {
                        FormDevHelper.appendFormDevError("ViewSet type cannot be null!");
                        return;
                    }
                    if (StringUtils.isEmpty(title))
                    {
                        FormDevHelper.appendFormDevError("ViewSet title cannot be null!");
                        return;
                    }                       
                    if (StringUtils.isEmpty(fileName))
                    {
                        FormDevHelper.appendFormDevError("ViewSet file cannot be null!");
                        return;
                    }
                    // else
                    File viewSetFile = new File(contextDir.getAbsoluteFile() + File.separator + fileName);
                    if (!viewSetFile.exists())
                    {
                        FormDevHelper.appendFormDevError("ViewSet file cannot be found at["+viewSetFile.getAbsolutePath()+"]");
                        return;
                    }
                    
                    ViewSet viewSet = new ViewSet(ViewSet.parseType(typeStr), fName, title, fileName, contextDir);
                    viewsHash.put(viewSet.getName(), viewSet);
                    
                } else
                {
                    String msg = "ViewSet Name["+fName+"] is in use.";
                    log.error(msg);
                    FormDevHelper.appendFormDevError(msg);
                }
            }
        } else
        {
            String msg = "The root element for the document was null!";
            log.error(msg);
            FormDevHelper.appendFormDevError(msg);
        } 
    }

    /**
     * Reads the Form Registry. The forms are loaded when needed and only one ViewSet can be the "core" ViewSet which is where most of the forms
     * reside. This could also be thought of as the "default" set of forms.
     * 
     * @param contextDirArg the directory in which load the view sets
     */
    protected void init(final File contextDirArg, final boolean emptyIsOK)
    {
        this.contextDir     = contextDirArg;
        this.registryExists = false;
        
        if (contextDirArg != null)
        { 
            log.debug(contextDirArg.getAbsoluteFile());
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
                    log.error(ex);
                    ex.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ViewSetMgr.class, ex);
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
            for (Enumeration<ViewSetIFace> e = viewsHash.elements(); e.hasMoreElements();)
            {
                ViewSetIFace viewSet = e.nextElement();
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
