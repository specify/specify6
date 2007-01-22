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

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class RepresentativeTextFactory implements ObjectTextMapper
{
    protected Hashtable<Class<?>, ObjectTextMapper> subMappers;

    protected static RepresentativeTextFactory instance;
    
    protected RepresentativeTextFactory()
    {
        subMappers = new Hashtable<Class<?>, ObjectTextMapper>();
    }
    public synchronized static RepresentativeTextFactory getInstance()
    {
        if (instance == null)
        {
            instance = new RepresentativeTextFactory();
            try
            {
                //instance.readMappingFile();
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return instance;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getMappedClasses()
     */
    public Class[] getMappedClasses()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getString(java.lang.Object)
     */
    public String getString(Object o)
    {
        // if a class was registered to handle this class of object
        // let the registered handler do the work
        ObjectTextMapper subMapper = subMappers.get(o.getClass());
        if (subMapper!=null)
        {
            return subMapper.getString(o);
        }
        
        // otherwise...
        // call getIdentityTitle, if possible
        if (o instanceof FormDataObjIFace)
        {
            FormDataObjIFace formDataObj = (FormDataObjIFace)o;
            return formDataObj.getIdentityTitle();
        }
        // else
        // if all else fails, call toString(), which must be there
        return o.toString();
    }
    
    protected void readMappingFile() throws Exception
    {
        URL mappingFileURL = ClassLoader.getSystemResource("text_factory_mapping.xml");
        File mappingFile = new File(mappingFileURL.toURI());
        Document mappingDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(mappingFile);
        
        NodeList subMaps = mappingDoc.getElementsByTagName("SubMapper");
        for (int i = 0; i < subMaps.getLength(); ++i )
        {
            Node entry = subMaps.item(i);
            String classname = entry.getAttributes().getNamedItem("class").getNodeValue();
            ObjectTextMapper subMapper = Class.forName(classname).asSubclass(ObjectTextMapper.class).newInstance();
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
