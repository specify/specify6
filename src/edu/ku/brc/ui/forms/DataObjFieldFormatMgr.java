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

import java.util.Collection;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.ui.UIHelper;


/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class DataObjFieldFormatMgr
{
    protected static final Logger log = Logger.getLogger(DataObjFieldFormatMgr.class);
    protected static DataObjFieldFormatMgr  instance = new DataObjFieldFormatMgr();


    protected Hashtable<String, SwitchFormatter> formatHash = new Hashtable<String, SwitchFormatter>();
    protected Hashtable<String, Aggregator>      aggHash    = new Hashtable<String, Aggregator>();
    protected Object[]                           args       = new Object[2]; // start with two slots
    
    protected Hashtable<String, Class<?>>        typeHash   = new Hashtable<String, Class<?>>();
    
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
                    
                    Element switchElement = (Element)formatElement.selectObject("switch");
                    if (switchElement != null)
                    {
                        boolean  isSingle     = getAttr(switchElement, "single", false);
                        String   switchField  = getAttr(switchElement, "field", null);
                        
                        SwitchFormatter switchFormatter = new SwitchFormatter(isSingle, switchField);
                        
                        if (formatHash.get(name) == null)
                        {
                            formatHash.put(name, switchFormatter);
    
                        } else
                        {
                            throw new RuntimeException("Duplicate formatter name["+name+"]");
                        }
                        
                        List<?> fieldsElements = switchElement.selectNodes("fields");
                        for (Object fieldsObj : fieldsElements)
                        {
                            Element fieldsElement = (Element)fieldsObj;
                            String   valueStr  = getAttr(fieldsElement, "value", null);
                            
                            List<?> fldList = fieldsElement.selectNodes("field");
                            DataField[] fields = new DataField[fldList.size()];
                            int inx = 0;
                            for (Object fldObj : fldList)
                            {
                                Element  fieldElement  = (Element)fldObj;
                                String   fieldName     = fieldElement.getTextTrim();
                                String   dataTypeStr   = getAttr(fieldElement, "type", "string");
                                String   formatStr     = getAttr(fieldElement, "format", null);
                                String   sepStr        = getAttr(fieldElement, "sep", null);
                                String   formatterName = getAttr(fieldElement, "formatter", null);
                                
                                Class<?> classObj      = typeHash.get(dataTypeStr);
                                if (classObj == null)
                                {
                                    log.error("Couldn't map standard type["+dataTypeStr+"]");
                                }
                                fields[inx] = new DataField(fieldName, classObj, formatStr, sepStr, formatterName);
                                inx++;
                            }
                            switchFormatter.add(new DataFieldFormat(name, className, format, valueStr, fields));
                        }
                    }
                }
                
                for ( Object aggObj : root.selectNodes("/formatters/aggregators/aggregator"))
                {
                    Element aggElement = (Element)aggObj;

                    String name      = aggElement.attributeValue("name");
                    String separator = aggElement.attributeValue("separator");
                    String countStr  = aggElement.attributeValue("count");
                    String ending    = aggElement.attributeValue("ending");
                    String format    = aggElement.attributeValue("format");
                    
                    Integer count = StringUtils.isNotEmpty(countStr) && StringUtils.isNumeric(countStr) ? Integer.parseInt(countStr) : null;
                    
                    // TODO check for duplicates!
                    aggHash.put(name, new Aggregator(name, separator, count, ending, format));
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
     * Format a data object using a named formatter.
     * @param dataObj the data object for which fields will be formatted for it
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    protected DataFieldFormat getDataFormatter(final Object dataObj, final String formatName)
    {
        SwitchFormatter switcherFormatter = formatHash.get(formatName);
        if (switcherFormatter != null)
        {
            if (switcherFormatter.isSingle)
            {
                return switcherFormatter.getFormatterForValue(null); // null is ignored
            }
    
            DataObjectGettable getter = DataObjectGettableFactory.get(dataObj.getClass().getName(), "edu.ku.brc.ui.forms.DataGetterForObj");
    
            Object[]        values = UIHelper.getFieldValues(new String[] {switcherFormatter.getFieldName()}, dataObj, getter);
            String          value  = values[0] != null ? values[0].toString() : "null";
            DataFieldFormat dff    = switcherFormatter.getFormatterForValue(value);
            if (dff == null)
            {
                log.error("Couldn't find a switchable data formatter for ["+formatName+"] field["+switcherFormatter.getFieldName()+"] value["+value+"]");
            }
            return dff;
            
        } else
        {
            log.error("Couldn't find a switchable name ["+formatName+"]");
        }
        return null;
    }

    /**
     * Format a data object using a named formatter
     * @param dataObj the data object for which fields will be formatted for it
     * @return the string result of the format
     */
    protected String formatInternal(final DataFieldFormat format, final Object dataObj)
    {
        if (format != null)
        {
            // XXX FIXME this shouldn't be hard coded here
            DataObjectGettable getter = DataObjectGettableFactory.get(format.getClassName(), "edu.ku.brc.ui.forms.DataGetterForObj");
            if (getter != null)
            {
                StringBuilder strBuf = new StringBuilder(128);
                for (DataField field : format.getFields())
                {
                    Object[] values = UIHelper.getFieldValues(new String[]{field.getName()}, dataObj, getter);
                    
                    
                    Object value = values != null ? values[0] : null;//getter.getFieldValue(dataObj, field.getName());
                    if (value != null)
                    {
                        if (field.getFormmatterName() != null )
                        {
                            String fmtStr = formatInternal(getDataFormatter(value, field.getFormmatterName()), value);
                            if (fmtStr != null)
                            {
                                strBuf.append(fmtStr);
                            }
                            
                        } else if (value.getClass() == field.getType())
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
     * @return the string result of the format
     */
    protected String formatInternal(final DataFieldFormat format, final Object[] dataObjs)
    {
        if (format != null)
        {
            // XXX FIXME this shouldn't be hard coded here
            DataObjectGettable getter = DataObjectGettableFactory.get(format.getClassName(), "edu.ku.brc.ui.forms.DataGetterForObj");
            if (getter != null)
            {
                StringBuilder strBuf = new StringBuilder(128);
                
                if (dataObjs.length == format.getFields().length)
                {
                    int inx = 0;
                    for (DataField field : format.getFields())
                    {
                        Object value = dataObjs[inx++];
                        if (value != null)
                        {
                            
                            if (field.getFormmatterName() != null )
                            {
                                String fmtStr = formatInternal(getDataFormatter(value, field.getFormmatterName()), value);
                                if (fmtStr != null)
                                {
                                    strBuf.append(fmtStr);
                                }
                                
                            } else if (value.getClass() == field.getType())
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
     * Aggregates all the items in a Collection into a string given a formatter 
     * @param items the collection of items
     * @param aggName the name of the aggregator to use
     * @return a string representing a collection of all the objects 
     */
    public String aggregateInternal(final Collection<?> items, final String aggName)
    {
        Aggregator agg = aggHash.get(aggName);
        if (agg != null)
        {
            StringBuilder aggStr = new StringBuilder(128);
            
            int count = 0;
            for (Object obj : items)
            {
                if (obj != null)
                {
                    if (count > 0)
                    {
                        aggStr.append(agg.getSeparator());
                    }
                    aggStr.append(formatInternal(instance.getDataFormatter(obj, agg.getFormatName()), obj));
                    
                    if (agg.getCount() != null && count < agg.getCount())
                    {
                        aggStr.append(agg.getEnding());
                        break;
                    }
                }
                count++;
            }
            return aggStr.toString();
            
        } else
        {
            log.error("Couldn't find Aggegrator ["+aggName+"]");
        }
        return null;
    }
    
    /**
     * Format a data object using a named formatter
     * @param dataObj the data object for which fields will be formatted for it
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    public static String format(final Object dataObj, final String formatName)
    {
        return instance.formatInternal(instance.getDataFormatter(dataObj, formatName), dataObj);
    }

    /**
     * Format a data object using a named formatter
     * @param dataObjs the array data objects for which fields will be formatted for it
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    public static String format(final Object[] dataObjs, final String formatName)
    {
        throw new RuntimeException("OK, I am used, so come and fix me up!");
        //return instance.formatInternal(dataObjs, formatName);
    }
    
    /**
     * Aggregates all the items in a Collection into a string given a formatter 
     * @param items the collection of items
     * @param aggName the name of the aggregator to use
     * @return a string representing a collection of all the objects 
     */
    public static String aggregate(final Collection<?> items, final String aggName)
    {
        if (items != null && items.size() > 0)
        {
            return instance.aggregateInternal(items, aggName);
            
        } else
        {
            return "";
        }
    }


    //----------------------------------------------------------------
    // Inner Classes
    //----------------------------------------------------------------

    protected class SwitchFormatter
    {
        protected boolean         isSingle;
        protected String          fieldName;
        protected DataFieldFormat single     = null;
        
        protected Hashtable<String, DataFieldFormat> formatsHashtable= null;
        
        public SwitchFormatter(final boolean isSingle, final String fieldName)
        {
            this.isSingle  = isSingle;
            this.fieldName = fieldName;
        }
        
        public void add(final DataFieldFormat dff)
        {
            if (isSingle)
            {
                single = dff;
                
            } else
            {
                if (formatsHashtable == null)
                {
                    formatsHashtable = new Hashtable<String, DataFieldFormat>();
                }
                
                if (StringUtils.isNotEmpty(dff.getValue()))
                {
                    formatsHashtable.put(dff.getValue(), dff);
                    
                } else
                {
                    log.error("Data formatter's 'value' attribute is empty for ["+dff.getName()+"]");
                }
            }
        }
        
        public DataFieldFormat getFormatterForValue(final String value)
        {
            if (isSingle)
            {
                return single;
                
            }
            return formatsHashtable.get(value);
        }

        public String getFieldName()
        {
            return fieldName;
        }

        public boolean isSingle()
        {
            return isSingle;
        }

        public DataFieldFormat getSingle()
        {
            return single;
        }
    }
    
    protected class Aggregator
    {
        protected String          name;
        protected String          separator;
        protected Integer         count      = null;
        protected String          ending;
        protected String          formatName;
        
        public Aggregator(String name, String separator, Integer count, String ending, String formatName)
        {
            super();
            this.name = name;
            this.separator = separator;
            this.count = count;
            this.ending = ending;
            this.formatName = formatName;
        }

        public Integer getCount()
        {
            return count;
        }

        public String getEnding()
        {
            return ending;
        }

        public String getFormatName()
        {
            return formatName;
        }

        public String getName()
        {
            return name;
        }

        public String getSeparator()
        {
            return separator;
        }
    }
    
    /**
     * A field formatter
     */
    protected class DataFieldFormat
    {
        protected String      name;
        protected String      className;
        protected String      format;
        protected String      value;
        protected DataField[] fields;
        protected Class<?>    classObj;

        public DataFieldFormat(String name, String className, String format, String value, DataField[] fields)
        {
            this.name       = name;
            this.className  = className;
            this.format     = format;
            this.value     = value;
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

        public String getValue()
        {
            return value;
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

    /**
     * A individual part of the formatter.
     *
     */
    protected class DataField
    {
        protected String   name;
        protected Class<?> type;
        protected String   format;
        protected String   sep;
        protected String   formmatterName;
        
        public DataField(String name, Class<?> type, String format, String sep, String formmatterName)
        {
            super();
            
            this.name = name;
            this.type = type;
            this.format = format;
            this.sep = sep;
            this.formmatterName = formmatterName;
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
        public String getFormmatterName()
        {
            return formmatterName;
        }
        
    }

}
