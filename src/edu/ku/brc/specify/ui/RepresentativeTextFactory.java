/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.ui;

import java.io.File;
import java.util.Hashtable;

import javax.swing.ListCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.helpers.XMLHelper;

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
    private static final Logger log = Logger.getLogger(RepresentativeTextFactory.class);
    
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RepresentativeTextFactory.class, e);
                e.printStackTrace();
                log.error("An error occurred while configuring RepresentativeTextFactory.");
            }
        }
        
        return instance;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getMappedClasses()
     */
    public Class<?>[] getMappedClasses()
    {
    	// this instance only handles FormDataObjIFace if no submappers are registered
    	// TODO: should this return the aggregate of all classes mapped by registered submappers?
    	return new Class<?>[] {FormDataObjIFace.class};
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getString(java.lang.Object)
     */
    public String getString(Object o)
    {
        // first see if a submapper handles this type of object
        ObjectTextMapper subMapper = subMappers.get(o.getClass());
        if (subMapper!=null)
        {
            return subMapper.getString(o);
        }

        // try one more thing in case the submapper handles an interface or superclass
        for (Class<?> clazz: subMappers.keySet())
        {
            if (clazz.isAssignableFrom(o.getClass()))
            {
                return subMappers.get(clazz).getString(o);
            }
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
        File mappingFile = XMLHelper.getConfigDir("text_factory_mapping.xml");
        
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
            Class<?>[] handledClasses = subMapper.getMappedClasses();
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
