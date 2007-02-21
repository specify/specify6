/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui;

import java.io.File;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.ListCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 * This class provides a mapper from Objects to a String of representative text
 * for those instances as well as a facility to register other ObjectTextMapper instances.
 * This class is designed for use in UI renderers such as {@link ListCellRenderer}s and
 * {@link TreeCellRenderer}s.  It allows for developers to write 'plugins' that can
 * provide useful text representations of various classes of objects.
 *
 * @author jstewart
 */
public class RepresentativeTextFactory implements ObjectTextMapper
{
    /** a mapping from a Class to its registered {@link ObjectTextMapper}. */
    protected Hashtable<Class<?>, ObjectTextMapper> subMappers;

    /** the singleton instance of this class. */
    protected static RepresentativeTextFactory instance;
    
    /**
     * A protected constructor, expected to be used only inside the {@link #getInstance()}
     * method during its first execution.
     */
    protected RepresentativeTextFactory()
    {
    	// instantiate internal properites
        subMappers = new Hashtable<Class<?>, ObjectTextMapper>();
    }
    
    /**
     * Returns the singleton instance of a RepresentativeTextFactory.  Creation
     * of the singleton instance is followed immediately by its configuration
     * by a call to {@link #readMappingFile()}.  This requires the existance of the file
     * 'text_factory_mapping.xml' somewhere in the classpath.
     * 
     * @return the singleton instance of RepresentativeTextFactory
     */
    public synchronized static RepresentativeTextFactory getInstance()
    {
    	// check for prior existance of the singleton
        if (instance == null)
        {
        	// create a new instance and configure it
            instance = new RepresentativeTextFactory();
            try
            {
                instance.readMappingFile();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.println("An error occurred while configuring RepresentativeTextFactory.");
            }
        }
        
        return instance;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getMappedClasses()
     */
    public Class[] getMappedClasses()
    {
    	// this instance only handles FormDataObjIFace if no submappers are registered
    	// TODO: should this return the aggregate of all classes mapped by registered submappers?
    	return new Class[] {FormDataObjIFace.class};
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getString(java.lang.Object)
     */
    public String getString(Object o)
    {
        // if an ObjectTextMapper was registered to handle this class of object
        // let the registered object do the work
    	// TODO: improve this to work when a handler is registered for a superclass of
    	//       the passed in object
        ObjectTextMapper subMapper = subMappers.get(o.getClass());
        if (subMapper!=null)
        {
            return subMapper.getString(o);
        }
        
        // otherwise...
        // call getIdentityTitle, if the argument is an instance of FormDataObjIFace
        if (o instanceof FormDataObjIFace)
        {
            FormDataObjIFace formDataObj = (FormDataObjIFace)o;
            return formDataObj.getIdentityTitle();
        }
        
        // else
        // if all else fails, call toString(), which must be there
        return o.toString();
    }
    
    /**
     * Parse the mapping file, 'text_factory_mapping.xml', registering a submapper
     * for each SubMapper element found.  Register the found mappers for each class
     * that they themselves claim to handle, based on the return of their
     * {@link #getMappedClasses()} method.
     *
     * @throws Exception any error occurs during parsing or mapper instantiation
     */
    protected void readMappingFile() throws Exception
    {
    	// TODO: should we make the filename configurable?
        URL mappingFileURL = ClassLoader.getSystemResource("text_factory_mapping.xml");
        File mappingFile = new File(mappingFileURL.toURI());
        
        // build a DOM from the file
        Document mappingDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(mappingFile);
        
        // grab all <SubMapper> nodes
        NodeList subMaps = mappingDoc.getElementsByTagName("SubMapper");
        for (int i = 0; i < subMaps.getLength(); ++i )
        {
            Node entry = subMaps.item(i);
            // instantiate each class found
            String classname = entry.getAttributes().getNamedItem("class").getNodeValue();
            // register it as a submapper for each class it handles
            ObjectTextMapper subMapper = Class.forName(classname).asSubclass(ObjectTextMapper.class).newInstance();
            Class[] handledClasses = subMapper.getMappedClasses();
            for (Class<?> clazz: handledClasses)
            {
                if (clazz==null)
                {
                    // if this mapper claims to map no classes, why does it exist?
                	// skip this one
                    continue;
                }
                
                subMappers.put(clazz, subMapper);
            }
        }
    }
}
