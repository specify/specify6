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
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconManager.IconSize;

/**
 * A factory for producing icons that visually represent data objects.
 * 
 * @code_status Alpha
 * @author jstewart
 */
public class RepresentativeIconFactory implements ObjectIconMapper
{
    protected Hashtable<Class<?>, ImageIcon> classToIconMap;
    protected Hashtable<Class<?>, ObjectIconMapper> subMappers;
    
    protected static RepresentativeIconFactory instance;
    
    protected RepresentativeIconFactory()
    {
        classToIconMap = new Hashtable<Class<?>, ImageIcon>();
        subMappers = new Hashtable<Class<?>, ObjectIconMapper>();
    }
    
    public synchronized static RepresentativeIconFactory getInstance()
    {
        if (instance == null)
        {
            instance = new RepresentativeIconFactory();
            try
            {
                instance.readMappingFile();
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return instance;
    }
    
    public synchronized ImageIcon getIcon(Object o)
    {
        ObjectIconMapper subMapper = subMappers.get(o.getClass());
        if (subMapper!=null)
        {
            return subMapper.getIcon(o);
        }
        
        ImageIcon icon = classToIconMap.get(o.getClass());
        return icon;
    }
    
    public Class[] getMappedClasses()
    {
        Class[] mappedClasses = new Class[4];
        mappedClasses[0] = Attachment.class;
        mappedClasses[1] = Determination.class;
        mappedClasses[2] = CollectionObject.class;
        mappedClasses[3] = Agent.class;
        return mappedClasses;
    }
    
    protected void readMappingFile() throws Exception
    {
        URL mappingFileURL = ClassLoader.getSystemResource("icon_factory_mapping.xml");
        File mappingFile = new File(mappingFileURL.toURI());
        Document mappingDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(mappingFile);
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
        
        NodeList subMaps = mappingDoc.getElementsByTagName("SubMapper");
        for (int i = 0; i < subMaps.getLength(); ++i )
        {
            Node entry = subMaps.item(i);
            String classname = entry.getAttributes().getNamedItem("class").getNodeValue();
            ObjectIconMapper subMapper = Class.forName(classname).asSubclass(ObjectIconMapper.class).newInstance();
            Class[] handledClasses = subMapper.getMappedClasses();
            for (Class<?> clazz: handledClasses)
            {
                if (clazz==null)
                {
                    // skip it
                    continue;
                }
                
                subMappers.put(clazz, subMapper);
            }
        }
    }
    
}
