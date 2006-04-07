/* Filename:    $RCSfile: DataObjFieldFormatMgr.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/04/05 19:59:54 $
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

import java.util.Formatter;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.ku.brc.specify.helpers.XMLHelper;

/**
 * @author rods
 *
 */
public class DataObjFieldFormatMgr
{
    protected static Log log = LogFactory.getLog(DataObjFieldFormatMgr.class);
    protected static DataObjFieldFormatMgr  instance = new DataObjFieldFormatMgr();
    
    
    protected Hashtable<String, DataFieldFormat> hash = new Hashtable<String, DataFieldFormat>();
    protected Object[]                           args = new Object[2]; // start with two slots  
    
    /**
     * Protected Constructor
     */
    protected DataObjFieldFormatMgr()
    {
        load();
    }
    
    /**
     * Loads formats from config file
     *
     */
    public void load()
    {
        String fileName = "dataobj_formatters.xml";
        try
        {
            Element root  = XMLHelper.readDOMFromConfigDir(fileName);
            if (root != null)
            {
                List formatters = root.selectNodes("/formatters/format");
                for ( Object formatObj : formatters) 
                {
                    Element formatElement = (Element)formatObj;
                    
                    String name      = formatElement.attributeValue("name");
                    String className = formatElement.attributeValue("class");
                    String format    = formatElement.attributeValue("format");
                    
                    List fields = formatElement.selectNodes("fields/field");
                    String[] fieldNames = new String[fields.size()];
                    int inx = 0;
                    for (Object fieldObj : fields) 
                    {
                        fieldNames[inx++] = ((Element)fieldObj).getTextTrim();
                    }
                    
                    if (hash.get(name) == null)
                    {
                        hash.put(name, new DataFieldFormat(name, className, format, fieldNames));
                        
                    } else
                    {
                        throw new RuntimeException("Duplicate formatter name["+name+"]");
                    }
                }
            } else
            {
                log.debug("Couldn't open ["+fileName+"]");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
    }
    
    /**
     * Format a data object using a named formatter
     * @param dataObj the data object for which fields will be formatted for it
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    protected String formatInternal(final Object dataObj, final String formatName)
    {
        DataFieldFormat format = hash.get(formatName);
        if (format != null)
        {
            DataObjectGettable getter = DataObjectGettableFactory.get(format.getClassName(), "edu.ku.brc.specify.ui.forms.DataGetterForObj");
            if (getter != null)
            {
                String[] fieldsNames = format.getFieldNames();
                if (fieldsNames.length > args.length)
                {
                    args = new Object[fieldsNames.length];
                }
                int inx = 0;
                for (String fieldName : fieldsNames)
                {
                    args[inx++] = getter.getFieldValue(dataObj, fieldName);
                }
                Formatter formatter = new Formatter();
                formatter.format(format.getFormat(), args);
                
                // clear any references to data
                for (int i=0;i<fieldsNames.length;i++)
                {
                    args[i] = null;
                }
                return formatter.toString();
            }
        }
        return "";
    }
    
    /**
     * Format a data object using a named formatter
     * @param dataObj the data object for which fields will be formatted for it
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    public static String format(final Object dataObj, final String formatName)
    {
        return instance.formatInternal(dataObj, formatName);
    }
    

    //----------------------------------------------------------------
    // Inner Classes
    //----------------------------------------------------------------
    
    protected class DataFieldFormat
    {
        protected String   name;
        protected String   className;
        protected String   format;
        protected String[] fieldNames;
        protected Class    classObj;
        
        public DataFieldFormat(String name, String className, String format, String[] fieldNames)
        {
            this.name = name;
            this.className = className;
            this.format = format;
            this.fieldNames = fieldNames;
            
            try
            {
                classObj = Class.forName(className);
                
            } catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }

        public String getClassName()
        {
            return className;
        }

        public String[] getFieldNames()
        {
            return fieldNames;
        }

        public String getFormat()
        {
            return format;
        }

        public String getName()
        {
            return name;
        }

        public Class getClassObj()
        {
            return classObj;
        }
        
        
        
    }

}
