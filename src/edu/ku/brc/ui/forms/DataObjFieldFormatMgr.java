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

import java.util.Formatter;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.helpers.XMLHelper;

/*
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class DataObjFieldFormatMgr
{
    protected static final Logger log = Logger.getLogger(DataObjFieldFormatMgr.class);
    protected static DataObjFieldFormatMgr  instance = new DataObjFieldFormatMgr();


    protected Hashtable<String, DataFieldFormat> hash     = new Hashtable<String, DataFieldFormat>();
    protected Object[]                           args     = new Object[2]; // start with two slots
    protected Hashtable<String, Class<?>>           typeHash = new Hashtable<String, Class<?>>();

    protected StringBuilder                      strBuf   = new StringBuilder(128);
    /**
     * Protected Constructor
     */
    protected DataObjFieldFormatMgr()
    {
        Object[] initTypeData = {"string", String.class, "int", Integer.class, "float", Float.class, "double", Double.class, "boolean", Boolean.class};
        for (int i=0;i<initTypeData.length;i++)
        {
            typeHash.put((String)initTypeData[i], (Class)initTypeData[i+1]);
            i++;
        }
        load();

    }

    /**
     * Loads formats from config file
     *
     */
    public void load()
    {
        try
        {
            Element root  = AppContextMgr.getInstance().getResourceAsDOM("DataObjFormatters");
            if (root != null)
            {
                List<?> formatters = root.selectNodes("/formatters/format");
                for ( Object formatObj : formatters)
                {
                    Element formatElement = (Element)formatObj;

                    String name      = formatElement.attributeValue("name");
                    String className = formatElement.attributeValue("class");
                    String format    = formatElement.attributeValue("format");

                    List<?> fieldsElements = formatElement.selectNodes("fields/field");
                    DataField[] fields = new DataField[fieldsElements.size()];
                    int inx = 0;
                    for (Object fieldObj : fieldsElements)
                    {
                        Element fieldElement = (Element)fieldObj;
                        String  fieldName    = fieldElement.getTextTrim();
                        String  dataTypeStr  = XMLHelper.getAttr(fieldElement, "type", "string");
                        String  formatStr    = XMLHelper.getAttr(fieldElement, "format", null);
                        String  sepStr       = XMLHelper.getAttr(fieldElement, "sep", null);
                        Class<?>   classObj     = typeHash.get(dataTypeStr);
                        if (classObj == null)
                        {
                            log.error("Couldn't map standard type["+dataTypeStr+"]");
                        }
                        fields[inx] = new DataField(fieldName, classObj, formatStr, sepStr);
                        inx++;
                    }

                    if (hash.get(name) == null)
                    {
                        hash.put(name, new DataFieldFormat(name, className, format, fields));

                    } else
                    {
                        throw new RuntimeException("Duplicate formatter name["+name+"]");
                    }
                }
            } else
            {
                log.debug("Couldn't get resource [DataObjFormatters]");
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
            // XXX FIXME this shouldn't be hard coded here
            DataObjectGettable getter = DataObjectGettableFactory.get(format.getClassName(), "edu.ku.brc.ui.forms.DataGetterForObj");
            if (getter != null)
            {
                strBuf.setLength(0);
                for (DataField field : format.getFields())
                {
                    Object value = getter.getFieldValue(dataObj, field.getName());
                    if (value != null)
                    {
                        if (value.getClass() == field.getType())
                        {
                            // When format is null then it is a string
                            if (field.getType() == String.class &&
                                (field.getFormat() == null || format.equals("%s")))
                            {
                                if (field.getSep() != null)
                                {
                                    strBuf.append(field.getSep());
                                }
                                strBuf.append(value.toString());
                            } else
                            {
                                args[0] = value;
                                Formatter formatter = new Formatter();
                                formatter.format(format.getFormat(), args);
                                strBuf.append(formatter.toString());
                                args[0] = null;
                            }
                        } else
                        {
                            log.error("Mismatch of types data retrieved as class["+value.getClass().getSimpleName()+"] and the format requires ["+field.getType().getSimpleName()+"]");
                        }
                    }
                }
                return strBuf.toString();
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
    protected String formatInternal(final Object[] dataObjs, final String formatName)
    {
        DataFieldFormat format = hash.get(formatName);
        if (format != null)
        {
            // XXX FIXME this shouldn't be hard coded here
            DataObjectGettable getter = DataObjectGettableFactory.get(format.getClassName(), "edu.ku.brc.ui.forms.DataGetterForObj");
            if (getter != null)
            {
                if (dataObjs.length == format.getFields().length)
                {
                    strBuf.setLength(0);
                    int inx = 0;
                    for (DataField field : format.getFields())
                    {
                        Object value = dataObjs[inx++];
                        if (value != null)
                        {
                            if (value.getClass() == field.getType())
                            {
                                // When format is null then it is a string
                                if (field.getType() == String.class &&
                                    (field.getFormat() == null || format.equals("%s")))
                                {
                                    if (field.getSep() != null)
                                    {
                                        strBuf.append(field.getSep());
                                    }
                                    strBuf.append(value.toString());
                                } else
                                {
                                    args[0] = value;
                                    Formatter formatter = new Formatter();
                                    formatter.format(format.getFormat(), args);
                                    strBuf.append(formatter.toString());
                                    args[0] = null;
                                }
                            } else
                            {
                                log.error("Mismatch of types data retrieved as class["+value.getClass().getSimpleName()+"] and the format requires ["+field.getType().getSimpleName()+"]");
                            }
                        }
                    }
                } else
                {
                    log.error("Data Array sent to formatter is not the same length ["+dataObjs.length+"] as the formatter ["+format.getFields().length+"]");
                }
                return strBuf.toString();
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

    /**
     * Format a data object using a named formatter
     * @param dataObjs the array data object for which fields will be formatted for it
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    public static String format(final Object[] dataObjs, final String formatName)
    {
        return instance.formatInternal(dataObjs, formatName);
    }


    //----------------------------------------------------------------
    // Inner Classes
    //----------------------------------------------------------------

    protected class DataFieldFormat
    {
        protected String   name;
        protected String   className;
        protected String   format;
        protected DataField[] fields;
        protected Class<?>    classObj;

        public DataFieldFormat(String name, String className, String format, DataField[] fields)
        {
            this.name       = name;
            this.className  = className;
            this.format     = format;
            this.fields     = fields;

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

        public DataField[] getFields()
        {
            return fields;
        }

        public String getFormat()
        {
            return format;
        }

        public String getName()
        {
            return name;
        }

        public Class<?> getClassObj()
        {
            return classObj;
        }
    }

    protected class DataField
    {
        protected String name;
        protected Class<?>  type;
        protected String format;
        protected String sep;
        public DataField(String name, Class<?> type, String format, String sep)
        {
            super();
            // TODO Auto-generated constructor stub
            this.name = name;
            this.type = type;
            this.format = format;
            this.sep = sep;
        }
        public String getFormat()
        {
            return format;
        }
        public String getName()
        {
            return name;
        }
        public String getSep()
        {
            return sep;
        }
        public Class<?> getType()
        {
            return type;
        }

    }


}
