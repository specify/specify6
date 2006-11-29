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
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Preparation;
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


    protected Hashtable<String, SwitchFormatter> formatHash      = new Hashtable<String, SwitchFormatter>();
    protected Hashtable<Class, SwitchFormatter>  formatClassHash = new Hashtable<Class, SwitchFormatter>();
    protected Hashtable<String, Aggregator>      aggHash         = new Hashtable<String, Aggregator>();
    protected Hashtable<Class, Aggregator>       aggClassHash    = new Hashtable<Class, Aggregator>();
    protected Object[]                           args            = new Object[2]; // start with two slots
    
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

                    String name       = formatElement.attributeValue("name");
                    String className  = formatElement.attributeValue("class");
                    String format     = formatElement.attributeValue("format");
                    boolean isDefault = XMLHelper.getAttr(formatElement, "default", true);
                    
                    Class dataClass = null;
                    if (StringUtils.isNotEmpty(className))
                    {
                        try
                        {
                            dataClass = Class.forName(className);
                        } catch (Exception ex)
                        {
                            log.error("Couldn't load class ["+className+"]");
                        }
                    } else
                    {
                        log.error("Class name ["+className+"] is empty and can't be. Skipping.");
                        continue;
                    }
                    
                    Element switchElement = (Element)formatElement.selectObject("switch");
                    if (switchElement != null)
                    {
                        boolean  isSingle     = getAttr(switchElement, "single", false);
                        String   switchField  = getAttr(switchElement, "field", null);
                        
                        SwitchFormatter switchFormatter = new SwitchFormatter(name, isSingle, isDefault, dataClass, switchField);
                        
                        if (formatHash.get(name) == null)
                        {
                            formatHash.put(name, switchFormatter);
    
                        } else
                        {
                            throw new RuntimeException("Duplicate formatter name["+name+"]");
                        }
                        
                        SwitchFormatter sf = formatClassHash.get(dataClass);
                        if (sf == null || isDefault)
                        {
                            formatClassHash.put(dataClass, switchFormatter);
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
                            switchFormatter.add(new DataFieldFormat(name, dataClass, isDefault, format, valueStr, fields));
                        }
                    } else
                    {
                        log.error("No switch element! ["+name+"]"); // not needed once we start using a DTD/Schema
                    }
                }
                
                for ( Object aggObj : root.selectNodes("/formatters/aggregators/aggregator"))
                {
                    Element aggElement = (Element)aggObj;

                    String name       = aggElement.attributeValue("name");
                    String  dataClassName = XMLHelper.getAttr(aggElement, "class", null);
                    String separator  = aggElement.attributeValue("separator");
                    String countStr   = aggElement.attributeValue("count");
                    String ending     = aggElement.attributeValue("ending");
                    String format     = aggElement.attributeValue("format");
                    boolean isDefault = XMLHelper.getAttr(aggElement, "default", true);
                    
                    Integer count = StringUtils.isNotEmpty(countStr) && StringUtils.isNumeric(countStr) ? Integer.parseInt(countStr) : null;
                    
                    Class dataClass = null;
                    if (StringUtils.isNotEmpty(dataClassName))
                    {
                        try
                        {
                            dataClass = Class.forName(dataClassName);
                        } catch (Exception ex)
                        {
                            log.error("Couldn't load class ["+dataClassName+"]");
                        }
                    } else
                    {
                        log.error("Class name ["+dataClassName+"] is empty and can't be. Skipping.");
                        continue;
                    }
                    
                    // TODO check for duplicates!
                    aggHash.put(name, new Aggregator(name, dataClass, isDefault, separator, count, ending, format));
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
            return getDataFormatter(dataObj, switcherFormatter);
            
        } else
        {
            log.error("Couldn't find a switchable name ["+formatName+"]");
        }

        return null;
    }

    /**
     * Format a data object using a named formatter.
     * @param dataObj the data object for which fields will be formatted for it
     * @param switcherFormatter the switch formatter
     * @return the string result of the format
     */
    protected DataFieldFormat getDataFormatter(final Object dataObj, final SwitchFormatter switcherFormatter)
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
            log.error("Couldn't find a switchable data formatter for ["+switcherFormatter.getName()+"] field["+switcherFormatter.getFieldName()+"] value["+value+"]");
        }
        return dff;
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
            DataObjectGettable getter = DataObjectGettableFactory.get(format.getDataClass().getName(), 
                                                                      "edu.ku.brc.ui.forms.DataGetterForObj");
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
                                String sep = field.getSep();
                                if (sep != null)
                                {
                                    strBuf.append(sep);
                                }
                                //log.debug("["+value+"]["+format.getFormat()+"]");
                                args[0] = value;
                                Formatter formatter = new Formatter();
                                formatter.format(field.getFormat(), args);
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
            DataObjectGettable getter = DataObjectGettableFactory.get(format.getDataClass().getName(), 
                                                                      "edu.ku.brc.ui.forms.DataGetterForObj");
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
    protected String aggregateInternal(final Collection<?> items, final Aggregator agg)
    {
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
            log.error("Aggegrator was null.");
        }
        return null;
    }
    
    /**
     * Format a data object using a named formatter.
     * @param dataObj the data object for which fields will be formatted for it
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    public static String format(final Object dataObj, final String formatName)
    {
        SwitchFormatter sf = instance.formatHash.get(formatName);
        if (sf != null)
        {
            DataFieldFormat dff = instance.getDataFormatter(dataObj, sf);
            if (dff != null)
            {
                return instance.formatInternal(dff, dataObj);
                
            } else
            {
                log.error("Couldn't find DataFieldFormat for ["+sf.getName()+"] value["+dataObj+"]");
            }
        } else
        {
            log.error("Couldn't find SwitchFormatter for class ["+formatName+"]"); 
        }
        return null;
    }

    /**
     * Format a data object using a named formatter.
     * @param dataObj the data object for which fields will be formatted for it
     * @param dataClass the class for the data to be formatted
     * @return the string result of the format
     */
    public static String format(final Object dataObj, final Class dataClass)
    {
        SwitchFormatter sf = instance.formatClassHash.get(dataClass);
        if (sf != null)
        {
            DataFieldFormat dff = instance.getDataFormatter(dataObj, sf);
            if (dff != null)
            {
                return instance.formatInternal(dff, dataObj);
                
            } else
            {
                log.error("Couldn't find DataFieldFormat for ["+sf.getName()+"] value["+dataObj+"]");
            }
        } else
        {
            log.error("Couldn't find SwitchFormatter for class ["+dataClass.getName()+"]"); 
        }
        return null;
    }

    /**
     * Format a data object using a named formatter.
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
     * Aggregates all the items in a Collection into a string given a formatter.
     * @param items the collection of items
     * @param aggName the name of the aggregator to use
     * @return a string representing a collection of all the objects 
     */
    public static String aggregate(final Collection<?> items, final String aggName)
    {
        if (items != null && items.size() > 0)
        {
            Aggregator agg = instance.aggHash.get(aggName);
            if (agg != null)
            {
                return instance.aggregateInternal(items, agg);
                
            } else
            {
                log.error("Couldn't find Aggegrator ["+aggName+"]");
            }
            
        }
        // else
        return "";
    }
    
    /**
     * Aggregates all the items in a Collection into a string given a formatter.
     * @param items the collection of items
     * @param aggName the name of the aggregator to use
     * @return a string representing a collection of all the objects 
     */
    public static String aggregate(final Collection<?> items, final Class dataClass)
    {
        Aggregator defAgg = null;
        if (dataClass == Determination.class)
        {
            int x = 0;
            x++;
        }
        for (Enumeration<Aggregator> e=instance.aggHash.elements();e.hasMoreElements();)
        {
            Aggregator agg = e.nextElement();
            if (dataClass == agg.getDataClass())
            {
                if (agg.isDefault())
                {
                    defAgg = agg;
                    break;
                    
                } else if (defAgg == null)
                {
                    defAgg = agg;
                }
            }
        }
        
        if (defAgg != null)
        {
            return instance.aggregateInternal(items, defAgg);
            
        } else
        {
            log.error("Could find aggregator of class ["+dataClass.getCanonicalName()+"]");
        }
        return "";
    }

    //----------------------------------------------------------------
    // Inner Classes
    //----------------------------------------------------------------

    protected class SwitchFormatter
    {
        protected String          name;
        protected boolean         isSingle;
        protected boolean         isDefault;
        protected Class           dataClass;
        protected String          fieldName;
        protected DataFieldFormat single     = null;
        
        protected Hashtable<String, DataFieldFormat> formatsHashtable= null;
        
        public SwitchFormatter(final String  name, 
                               final boolean isSingle, 
                               final boolean isDefault, 
                               final Class   dataClass, 
                               final String  fieldName)
        {
            this.name      = name;
            this.isSingle  = isSingle;
            this.isDefault = isDefault;
            this.dataClass = dataClass;
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

        public String getName()
        {
            return name;
        }

        public String getFieldName()
        {
            return fieldName;
        }

        public boolean isSingle()
        {
            return isSingle;
        }

        public Class getDataClass()
        {
            return dataClass;
        }

        public boolean isDefault()
        {
            return isDefault;
        }

        public DataFieldFormat getSingle()
        {
            return single;
        }
    }
    
    /**
     * A field formatter
     */
    protected class DataFieldFormat
    {
        protected String      name;
        protected Class<?>    dataClass;
        protected boolean     isDefault;
        protected String      format;
        protected String      value;
        protected DataField[] fields;
        protected Class<?>    classObj;

        public DataFieldFormat(String name, Class<?> dataClass, boolean isDefault, String format, String value, DataField[] fields)
        {
            this.name       = name;
            this.dataClass  = dataClass;
            this.isDefault  = isDefault;
            this.format     = format;
            this.value      = value;
            this.fields     = fields;
        }

        public Class<?> getDataClass()
        {
            return dataClass;
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

        public boolean isDefault()
        {
            return isDefault;
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
    
    
    /**
     * @author rods
     *
     */
    protected class Aggregator
    {
        protected String          name;
        protected Class           dataClass;
        protected boolean         isDefault;
        protected String          separator;
        protected Integer         count      = null;
        protected String          ending;
        protected String          formatName;
        
        public Aggregator(String name, Class dataClass, boolean isDefault, String separator, Integer count, String ending, String formatName)
        {
            super();
            this.name = name;
            this.isDefault = isDefault;
            this.dataClass = dataClass;
            this.separator = separator;
            this.count = count;
            this.ending = ending;
            this.formatName = formatName;
        }

        public boolean isDefault()
        {
            return isDefault;
        }

        public Class getDataClass()
        {
            return dataClass;
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

}
