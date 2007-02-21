/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui;

import java.io.File;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.ListCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconManager.IconSize;

/**
 * This class provides a mapper from Objects to an {@link ImageIcon} that is representative
 * of those instances as well as a facility to register other {@link ObjectIconMapper} instances.
 * This class is designed for use in UI renderers such as {@link ListCellRenderer}s and
 * {@link TreeCellRenderer}s.  It allows for developers to write 'plugins' that can
 * provide useful icon representations of various classes of objects.  Also provided
 * is a facility to register a single {@link ImageIcon} to represent all instances of various classes.
 *
 * @author jstewart
 */
public class RepresentativeIconFactory implements ObjectIconMapper
{
	/** a mapping from a Class to its registered ImageIcon. */
	protected Hashtable<Class<?>, ImageIcon> classToIconMap;
    
	/** a mapping from a Class to its registered ObjectIconMapper instance. */
	protected Hashtable<Class<?>, ObjectIconMapper> subMappers;
    
    /** the singleton instance of this class. */
    protected static RepresentativeIconFactory instance;
    
    /**
     * A protected constructor, expected to be used only inside the {@link #getInstance()}
     * method during its first execution.
     */
    protected RepresentativeIconFactory()
    {
        classToIconMap = new Hashtable<Class<?>, ImageIcon>();
        subMappers = new Hashtable<Class<?>, ObjectIconMapper>();
    }
    
    /**
     * Returns the singleton instance of a RepresentativeIconFactory.  Creation
     * of the singleton instance is followed immediately by its configuration
     * by a call to  {@link #readMappingFile()}.  This requires the existance of the file
     * 'icon_factory_mapping.xml' somewhere in the classpath.
     * 
     * @return the singleton instance of RepresentativeIconFactory
     */
    public synchronized static RepresentativeIconFactory getInstance()
    {
    	// check for prior existance of the singleton
        if (instance == null)
        {
        	// create a new instance and configure it
            instance = new RepresentativeIconFactory();
            try
            {
                instance.readMappingFile();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.println("An error occurred while configuring RepresentativeIconFactory.");
            }
        }
        
        return instance;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getIcon(java.lang.Object)
     */
    public synchronized ImageIcon getIcon(Object o)
    {
    	// first see if a submapper handles this type of object
        ObjectIconMapper subMapper = subMappers.get(o.getClass());
        if (subMapper!=null)
        {
            return subMapper.getIcon(o);
        }
        
        // then see if there is a single Icon representing this class of object
        ImageIcon icon = classToIconMap.get(o.getClass());
        
        // this can be null, if and only if no hanlder or class icon was registered
        return icon;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getMappedClasses()
     */
    public Class[] getMappedClasses()
    {
    	// This class maps no classes on its own.  All mappings are through submappers.
    	// TODO: should this class not really implement ObjectIconMapper?
    	return new Class[] {};
    }
    
    /**
     * Parse the mapping file, 'icon_factory_mapping.xml', registering a submapper
     * for each SubMapper element found.  Register the found mappers for each class
     * that they themselves claim to handle, based on the return of their
     *  {@link #getMappedClasses()} method.  Each ClassIcon element is also located.  For each,
     * an ImageIcon is registered to represent all instances of the referenced class.
     *
     * @throws Exception any error occurs during parsing or mapper instantiation
     */
    protected void readMappingFile() throws Exception
    {
    	// TODO: should we make the filename configurable?
        URL mappingFileURL = ClassLoader.getSystemResource("icon_factory_mapping.xml");
        File mappingFile = new File(mappingFileURL.toURI());
        
        // build a DOM from the file
        Document mappingDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(mappingFile);
        
        // grab all ClassIcon nodes
        // they map classes to single ImageIcons
        NodeList mapEntries = mappingDoc.getElementsByTagName("ClassIcon");
        for (int i = 0; i < mapEntries.getLength(); ++i)
        {
            Node entry = mapEntries.item(i);
            String classname = entry.getAttributes().getNamedItem("class").getNodeValue();
            String iconname = entry.getAttributes().getNamedItem("iconname").getNodeValue();
            Class<?> clazz = Class.forName(classname);
            ImageIcon icon = IconManager.getIcon(iconname, IconSize.Std24);
            classToIconMap.put(clazz, icon);
        }
        
        // grab all SubMapper nodes
        NodeList subMaps = mappingDoc.getElementsByTagName("SubMapper");
        for (int i = 0; i < subMaps.getLength(); ++i )
        {
            Node entry = subMaps.item(i);
            // instantiate each class found
            String classname = entry.getAttributes().getNamedItem("class").getNodeValue();
            // register it as a submapper for each class it handles
            ObjectIconMapper subMapper = Class.forName(classname).asSubclass(ObjectIconMapper.class).newInstance();
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
