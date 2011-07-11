/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms.formatters;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.io.File;
import java.math.BigDecimal;
import java.security.AccessController;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.DataObjectGettable;
import edu.ku.brc.af.ui.forms.DataObjectGettableFactory;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.UIHelper;


/**
 * This class manages all the Data Object Formatters. A DataObjectFormatter is used to create a string representation 
 * of a data object. Much of the time this is a single field, but sometimes it is a concatenation of several fields.
 * 
 * Although this class behaves as a singleton, other free instances of this Mgr can be created to simulate caches of formatters.
 * These are used to isolate the changes made to field and data object formatters and aggregators on the respective dialogs
 * to the objects held by the respective managers. An object of this class holds clones of the formatters that are affected
 * by the changes made on the dialogs. Those changes are only committed if the object holder (the objects that called the
 * dialogs) choose to do so (e.g. when the user clicks SAVE on the Localizer Tool frame).
 *   
 * @author rods, ricardo
 *
 * @code_status Complete
 *
 * Created Date: Jan 17, 2007
 *
 */
public class DataObjFieldFormatMgr
{
    public static final String factoryName = "edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr";
    
    protected static final Logger log = Logger.getLogger(DataObjFieldFormatMgr.class);
    
    protected static DataObjFieldFormatMgr instance   = null;
    protected static boolean               doingLocal = false;

    protected boolean domFound = false;

    protected Hashtable<String,   DataObjSwitchFormatter> formatHash      = new Hashtable<String, DataObjSwitchFormatter>();
    protected Hashtable<Class<?>, DataObjSwitchFormatter> formatClassHash = new Hashtable<Class<?>, DataObjSwitchFormatter>();
    protected Hashtable<String,   DataObjAggregator>      aggHash         = new Hashtable<String, DataObjAggregator>();
    protected Hashtable<Class<?>, DataObjAggregator>      aggClassHash    = new Hashtable<Class<?>, DataObjAggregator>();
    protected Object[]                                    args            = new Object[2]; // start with two slots
    
    protected Hashtable<String, Class<?>>                 typeHash        = new Hashtable<String, Class<?>>();
    protected Hashtable<Class<?>, String>                 typeHashRevMap  = new Hashtable<Class<?>, String>(); // reverse mapping
    
    protected String                                      localFileName   = null;
    protected boolean                                     hasChanged      = false;
    protected AppContextMgr                               appContextMgr      = null;

    /**
     * Protected Constructor
     */
    protected DataObjFieldFormatMgr()
    {
        init();
        load();
    }
    
    /**
     * Constructor
     */
    public DataObjFieldFormatMgr(final AppContextMgr appContextMgr)
    {
        this.appContextMgr = appContextMgr;
        init();
        load();
    }
    
    /**
     * Resets the Mgr so it gets reloaded.
     */
    public void reset()
    {
        if (instance != null)
        {
            instance.save();
        }
        instance = null;
    }
    
    /**
     * 
     */
    private void init()
    {
        localFileName = "backstop"+File.separator+"dataobj_formatters.xml";
        
        Object[] initTypeData = {"string",     String.class, 
                                 "int",        Integer.class, 
                                 "long",       Long.class, 
                                 "float",      Float.class, 
                                 "double",     Double.class, 
                                 "boolean",    Boolean.class,
                                 "bigdecimal", BigDecimal.class,
                                 "short",      Short.class,
                                 "byte",       Byte.class};
        for (int i=0;i<initTypeData.length;i++)
        {
            typeHash.put((String)initTypeData[i], (Class<?>)initTypeData[i+1]);
            typeHashRevMap.put((Class<?>)initTypeData[i+1], (String)initTypeData[i]);
            i++;
        }
    }
    
    /**
     * @return the contextMgr
     */
    public AppContextMgr getAppContextMgr()
    {
        if (appContextMgr == null)
        {
            appContextMgr = AppContextMgr.getInstance();
        }
        return appContextMgr;
    }

    /**
     * @param contextMgr the contextMgr to set
     */
    public void setAppContextMgr(AppContextMgr appContextMgr)
    {
        this.appContextMgr = appContextMgr;
    }
    
    /**
     * @param cls the class
     * @return the string for the java type class
     */
    public String getStrForType(final Class<?> cls)
    {
        return typeHashRevMap.get(cls);
    }
    
    /**
     * Copy constructor
     * @param source Source to copy from, usually the static instance
     */
    @SuppressWarnings("unchecked")
    public DataObjFieldFormatMgr(final DataObjFieldFormatMgr source)
    {
        formatHash      = (Hashtable<String,   DataObjSwitchFormatter>) source.getFormatHash().clone();
        formatClassHash = (Hashtable<Class<?>, DataObjSwitchFormatter>) source.getFormatClassHash().clone();
        aggHash         = (Hashtable<String,   DataObjAggregator>)      source.getAggHash().clone();
        aggClassHash    = (Hashtable<Class<?>, DataObjAggregator>)      source.getAggClassHash().clone();
    }
    
    /**
     * Transfer state of free manager instance to static instance
     */
    public void copyFrom(final DataObjFieldFormatMgr source)
    {
        this.hasChanged = source.hasChanged;
        setFormatHash(source.getFormatHash());
        setFormatClassHash(source.getFormatClassHash());
        setAggHash(source.getAggHash());
        setAggClassHash(source.getAggClassHash());
    }
    
    /**
     * @param localFileName the localFileName to set
     */
    public void setLocalFileName(String localFileName)
    {
        this.localFileName = localFileName;
    }

    /**
     * @return the localFileName
     */
    public String getLocalFileName()
    {
        return localFileName;
    }

    /**
     * @param doLocal the doLocal to set
     */
    public static void setDoingLocal(boolean doLocal)
    {
        DataObjFieldFormatMgr.doingLocal = doLocal;
    }

    /**
     * @return the DOM to process
     */
    protected Element getDOM() throws Exception
    {
        return XMLHelper.readDOMFromConfigDir("backstop/dataobj_formatters.xml"); //$NON-NLS-1$
    }
    
    /**
     * Resets the cache.
     */
    protected void resetInternal()
    {
        formatHash.clear();
        formatClassHash.clear();
        aggHash.clear();
        aggClassHash.clear();
    }

    /**
     * Loads formats from config file
     *
     */
    public void load()
    {
        resetInternal();
        
        try
        {
            Element root  = getDOM();
            
            if (root != null)
            {
                domFound = true;
                List<?> formatters = root.selectNodes("/formatters/format");
                for ( Object formatObj : formatters)
                {
                    Element formatElement = (Element)formatObj;

                    String name       = formatElement.attributeValue("name");
                    String title      = formatElement.attributeValue("title");
                    String className  = formatElement.attributeValue("class");
                    String format     = formatElement.attributeValue("format");
                    boolean isDefault = XMLHelper.getAttr(formatElement, "default", false);
                    
                    if (StringUtils.isEmpty(title))
                    {
                        title = name;
                    }
                    
                    Class<?> dataClass = null;
                    if (StringUtils.isNotEmpty(className))
                    {
                        try
                        {
                            dataClass = Class.forName(className);
                        } catch (Exception ex)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjFieldFormatMgr.class, ex);
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
                        
                        DataObjSwitchFormatter switchFormatter = new DataObjSwitchFormatter(name, title, isSingle, isDefault, dataClass, switchField);
                        
                        if (formatHash.get(name) == null)
                        {
                            formatHash.put(name, switchFormatter);
    
                        } else
                        {
                            throw new RuntimeException("Duplicate formatter name["+name+"]");
                        }
                        
                        DataObjSwitchFormatter sf = formatClassHash.get(dataClass);
                        if (sf == null || isDefault)
                        {
                            if (isDefault)
                            {
                                DataObjSwitchFormatter curDO = formatClassHash.get(dataClass);
                                if (curDO != null && curDO.isDefault())
                                {
                                    throw new RuntimeException("There are two default DataObjectFormatters current ["+curDO.getName()+"] adding["+sf.getName()+"] for class "+dataClass.getSimpleName());
                                }
                            }
                            formatClassHash.put(dataClass, switchFormatter);
                        }
                        
                        Element external = (Element)switchElement.selectSingleNode("external");
                        if (external != null)
                        {
                            String externalClassName = getAttr(external, "class", (String)null);
                            if (StringUtils.isNotEmpty(externalClassName))
                            {
                                Properties props = new Properties();
                                
                                List<?> paramElements = external.selectNodes("param");
                                for (Object param : paramElements)
                                {
                                    String nameStr = getAttr((Element)param, "name", null);
                                    String val     = ((Element)param).getTextTrim();
                                    if (StringUtils.isNotEmpty(nameStr) && StringUtils.isNotEmpty(val))
                                    {
                                        props.put(nameStr, val);
                                    }
                                }
                                try 
                                {
                                    DataObjDataFieldFormatIFace fmt = Class.forName(externalClassName).asSubclass(DataObjDataFieldFormatIFace.class).newInstance();
                                    fmt.init(name, props);
                                    switchFormatter.add(fmt);
                                    
                                } catch (Exception ex)
                                {
                                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjFieldFormatMgr.class, ex);
                                    log.error(ex);
                                    ex.printStackTrace();
                                }
                            } else
                            {
                                throw new RuntimeException("The 'class' attribute cannot be empty for an external formatter! ["+name+"]");
                            }
                        } else
                        {
                            DBTableInfo tableInfo = switchFormatter.getTableInfo();
                            
                            List<?> fieldsElements = switchElement.selectNodes("fields");
                            for (Object fieldsObj : fieldsElements)
                            {
                                Element fieldsElement = (Element)fieldsObj;
                                String  valueStr      = getAttr(fieldsElement, "value", null);
                                
                                List<?> fldList = fieldsElement.selectNodes("field");
                                DataObjDataField[] fields = new DataObjDataField[fldList.size()];
                                int inx = 0;
                                for (Object fldObj : fldList)
                                {
                                    Element  fieldElement  = (Element)fldObj;
                                    String   fieldName     = fieldElement.getTextTrim();
                                    String   dataTypeStr   = getAttr(fieldElement, "type",      null);
                                    String   formatStr     = getAttr(fieldElement, "format",    null);
                                    String   sepStr        = getAttr(fieldElement, "sep",       null);
                                    String   formatterName = getAttr(fieldElement, "formatter", null);
                                    String   uifieldformatter = getAttr(fieldElement, "uifieldformatter", null);
                                    
                                    DBFieldInfo        fieldInfo = tableInfo.getFieldByName(fieldName);
                                    DBRelationshipInfo relInfo   = fieldInfo == null ? tableInfo.getRelationshipByName(fieldName) : null;
                                    
                                    Class<?> classObj;
                                    if (dataTypeStr == null)
                                    {
                                        if (fieldInfo != null)
                                        {
                                            classObj = fieldInfo.getDataClass();
                                        } else
                                        {
                                            classObj = String.class;
                                        }
                                    } else
                                    {
                                        classObj = typeHash.get(dataTypeStr);
                                    }
                                    
                                    if (classObj == null)
                                    {
                                        log.error("Couldn't map standard type["+dataTypeStr+"]");
                                    }
                                    fields[inx] = new DataObjDataField(fieldName, classObj, formatStr, sepStr, formatterName, uifieldformatter);
                                    fields[inx].setDbInfo(tableInfo, fieldInfo, relInfo, true);
                                    
                                    inx++;
                                }
                                switchFormatter.add(new DataObjDataFieldFormat(name, dataClass, isDefault, format, valueStr, fields));
                            }
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
                    String title      = aggElement.attributeValue("title");
                    String separator  = aggElement.attributeValue("separator");
                    String countStr   = aggElement.attributeValue("count");
                    String ending     = aggElement.attributeValue("ending");
                    String format     = aggElement.attributeValue("format");
                    String ordFldName = XMLHelper.getAttr(aggElement, "orderfieldname", null);
                    boolean isDefault = XMLHelper.getAttr(aggElement, "default", true);
                    String dataClassName = XMLHelper.getAttr(aggElement, "class", null);
                    
                    Integer count = StringUtils.isNotEmpty(countStr) && StringUtils.isNumeric(countStr) ? Integer.parseInt(countStr) : null;
                    
                    Class<?> dataClass = null;
                    if (StringUtils.isNotEmpty(dataClassName))
                    {
                        try
                        {
                            dataClass = Class.forName(dataClassName);
                        } catch (Exception ex)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjFieldFormatMgr.class, ex);
                            log.error("Couldn't load class ["+dataClassName+"]");
                        }
                    } else
                    {
                        log.error("Class name ["+dataClassName+"] is empty and can't be. Skipping.");
                        continue;
                    }
                    
                    // TODO check for duplicates!
                    aggHash.put(name, new DataObjAggregator(name, title, dataClass, isDefault, separator, count, ending, format, ordFldName));
                }
                    
            } else
            {
                log.debug("Couldn't get resource [DataObjFormatters]");
            }
            
            // This needs to be refactored so we don't have to do this here
            // I think it is because 'load' is being called from the constructor.
            if (instance == null)
            {
                instance = this;
            }
            // now that all formats have been loaded, set table/field/formatter info\
            // must be executed after the instance is set
            for ( DataObjSwitchFormatter format : instance.formatHash.values() )
            {
                format.setTableAndFieldInfo();
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjFieldFormatMgr.class, ex);
            ex.printStackTrace();
            log.error(ex);
        }
    }
    
    /**
     * Copies the internal data structures from the source to this object. But only if they have changed.
     * @param source the source of the changes
     */
    public void applyChanges(final DataObjFieldFormatMgr source)
    {
        if (source.hasChanged)
        {
            copyFrom(source);
            save();
            
        } else
        {
            log.debug("Not saved = No Changes");
        }
    }
    
    /**
     * Saves formatters.
     */
    public void save() 
    {
        DataObjFieldFormatMgr localInstance = DataObjFieldFormatMgr.getInstance();
        
        // can only save the static instance
        //if (localInstance.getLocalFileName() == null)
        //{
        //    return;
        //}
        
        StringBuilder sb = new StringBuilder(1024);
        
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        
        // data obj formatters
        sb.append("<formatters>\n");

        Vector<DataObjSwitchFormatter> formatVector = new Vector<DataObjSwitchFormatter>(localInstance.getFormatHash().values());
        Collections.sort(formatVector, new Comparator<DataObjSwitchFormatter>()
        {
            public int compare(DataObjSwitchFormatter o1, DataObjSwitchFormatter o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        for (DataObjSwitchFormatter format : formatVector)
        {
            format.toXML(sb);
        }

        // aggregators
        sb.append("  <aggregators>\n");

        Vector<DataObjAggregator> aggVector = new Vector<DataObjAggregator>(localInstance.getAggHash().values());
        Collections.sort(aggVector, new Comparator<DataObjAggregator>()
        {
            public int compare(DataObjAggregator o1, DataObjAggregator o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (DataObjAggregator agg : aggVector)
        {
            agg.toXML(sb);
        }
        
        sb.append("  </aggregators>\n");
        sb.append("\n\n</formatters>\n");
        
        log.debug(sb.toString());
        
        saveXML(sb.toString());
    }
    
    /**
     * Persists the XML.
     * @param xml the xml string.
     */
    protected void saveXML(final String xml)
    {
        // save resource back to database
        if (doingLocal)
        {
            File outFile = XMLHelper.getConfigDir(instance.getLocalFileName());
            try
            {
                FileUtils.writeStringToFile(outFile, xml);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjFieldFormatMgr.class, ex);
            }
        }
    }
    
    /**
     * Adds a new formatter
     */
    public void addFormatter(DataObjSwitchFormatter formatter)
    {
        formatHash.put(formatter.getName(), formatter);
        formatClassHash.put(formatter.getDataClass(), formatter);
        hasChanged = true;
    }
    
    /**
     * Deletes a formatter from the 
     */
    public void removeFormatter(DataObjSwitchFormatter formatter)
    {
        formatHash.remove(formatter.getName());
        formatClassHash.remove(formatter.getName());
        hasChanged = true;
    }
    
    /**
     * Returns a data formatter.
     * @param formatName the name
     * @return the formatter
     */
    public DataObjSwitchFormatter getFormatter(final String formatName)
    {
        return formatHash.get(formatName);
    }

    /**
     * Returns all the formatters as a Collection
     * @return all the formatters
     */
    public Collection<DataObjSwitchFormatter> getFormatters()
    {
        return formatHash.values();
    }

    /**
     * Format a data object using a named formatter.
     * @param dataObj the data object for which fields will be formatted for it
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    private DataObjDataFieldFormatIFace getDataFormatter(final Object dataObj, final String formatName)
    {
        DataObjSwitchFormatter switcherFormatter = formatHash.get(formatName);
        if (switcherFormatter != null)
        {
            return switcherFormatter.getDataFormatter(dataObj);
            
        }
        return null;
    }
    
    /**
     * Format a data object using a named formatter.
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    public DataObjSwitchFormatter getDataFormatter(final String formatName)
    {
        return formatHash.get(formatName);
    }
    
    /**
     * Returns a list of formatters that match the class, the default (if there is one) is at the beginning of the list.
     * @param clazz the class of the data that the formatter is used for.
     * @return return a list of formatters that match the class
     */
    public List<DataObjSwitchFormatter> getFormatterList(final Class<?> clazz)
    {
        Vector<DataObjSwitchFormatter> list = new Vector<DataObjSwitchFormatter>();
        DataObjSwitchFormatter         defFormatter = null;
        
        for (Enumeration<DataObjSwitchFormatter> e=formatHash.elements();e.hasMoreElements();)
        {
            DataObjSwitchFormatter f = e.nextElement();
            if (clazz == f.getDataClass())
            {
                if (f.isDefault() && defFormatter == null)
                {
                    defFormatter = f;
                } else
                {
                    list.add(f);
                }
            }
        }
        if (defFormatter != null)
        {
            list.insertElementAt(defFormatter, 0);
        }
        return list;
    }
    
    /**
     * Tries to get the value and if it gets a lazy exception .
     * @param fieldNames
     * @param dataObj
     * @param getter
     * @return
     */
    private Object[] getFieldValues(final String[] fieldNames,
                                    final Object dataObj,
                                    final DataObjectGettable getter)
    {
        Object[] values = null;
        try
        {
            values = UIHelper.getFieldValues(fieldNames, dataObj, getter);
            
        } catch (org.hibernate.LazyInitializationException hbex) // XXX this Exception should be made generic
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                session.attach(dataObj);
                
                values = UIHelper.getFieldValues(fieldNames, dataObj, getter);
                
            } catch (Exception ex)
            {
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
        return values;
    }

    /**
     * Format a data object using a named formatter
     * @param dataObj the data object for which fields will be formatted for it
     * @return the string result of the format
     */
    protected String formatInternal(final DataObjDataFieldFormatIFace format, final Object dataObj)
    {
        String restricted = FormHelper.checkForRestrictedValue(dataObj);
        if (restricted != null)
        {
            return restricted;
        }
        
        if (format != null)
        {
            if (format.isDirectFormatter())
            {
                return format.format(dataObj);
            }
            
            DataObjectGettable getter = DataObjectGettableFactory.get(format.getDataClass().getName(), 
                                                                      FormHelper.DATA_OBJ_GETTER);
            if (getter != null)
            {
                StringBuilder strBuf = new StringBuilder(128);
                for (DataObjDataField field : format.getFields())
                {
                    Class<?> fieldClass = field.getType();
                    
                    Object[] values = getFieldValues(new String[]{field.getName()}, dataObj, getter);
                    Object   value  = values != null ? values[0] : null;
                    
                    // NOTE: if the field was a Date or Calendar object it has already been reformatted to a String
                    // so we change the fieldClass to string so everything works out.
                    if (fieldClass == Date.class || fieldClass == Calendar.class)
                    {
                        fieldClass = String.class;
                    }
                    
                    if (value != null)
                    {
                        if (AppContextMgr.isSecurityOn() && value instanceof FormDataObjIFace)
                        {
                            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(value.getClass().getSimpleName());
                            if (tblInfo != null)
                            {
                                PermissionSettings perm = tblInfo.getPermissions();
                                if (perm != null)
                                {
                                    if (!perm.canView())
                                    {
                                        return "";
                                    }
                                }
                            }
                        }
                        
                        if (field.getDataObjFormatterName() != null )
                        {
                            String fmtStr = formatInternal(getDataFormatter(value, field.getDataObjFormatterName()), value);
                            if (fmtStr != null)
                            {
                                strBuf.append(fmtStr);
                            }
                            
                        } else if (field.getUiFieldFormatterName() != null )
                        {
                            UIFieldFormatterIFace fmt = UIFieldFormatterMgr.getInstance().getFormatter(field.getUiFieldFormatterName());
                            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(dataObj.getClass().getSimpleName());
                            if (tblInfo != null)
                            {
                                DBFieldInfo fi = tblInfo.getFieldByName(field.getName());
                                if (fi != null && fi.getFormatter() != null)
                                {
                                    fmt = fi.getFormatter();
                                }
                            }
                            
                            if (fmt != null)
                            {
                                strBuf.append(fmt.formatToUI(value));
                            } else
                            {
                                strBuf.append(value);
                            }
                            
                        } else if (value.getClass() == fieldClass)
                        {
                            // When format is null then it is a string
                            if (fieldClass == String.class && (field.getFormat() == null || format.equals("%s")))
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
                                //log.debug("["+value+"]["+format+"]");
                                if (field.getFormat() != null)
                                {
                                    args[0] = value;
                                    Formatter formatter = new Formatter();
                                    formatter.format(field.getFormat(), args);
                                    strBuf.append(formatter.toString());
                                    args[0] = null;
                                    
                                } else
                                {
                                    strBuf.append(value.toString());
                                }
                            }
                        } else
                        {
                            log.error("Mismatch of types data retrieved as class["+(value != null ? value.getClass().getSimpleName() : "N/A")+
                                    "] and the format requires ["+(field != null ? (fieldClass != null ? fieldClass.getSimpleName() : "N/A 2") : "N/A")+"]");
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
    protected String formatInternal(final DataObjDataFieldFormat format, final Object[] dataObjs)
    {
        if (format != null)
        {
            DataObjectGettable getter = DataObjectGettableFactory.get(format.getDataClass().getName(), 
                                                                      FormHelper.DATA_OBJ_GETTER);
            if (getter != null)
            {
                StringBuilder strBuf = new StringBuilder(128);
                
                if (dataObjs.length == format.getFields().length)
                {
                    int inx = 0;
                    for (DataObjDataField field : format.getFields())
                    {
                        Object value = dataObjs[inx++];
                        if (value != null)
                        {
                            if (field.getDataObjFormatterName() != null )
                            {
                                String fmtStr = formatInternal(getDataFormatter(value, field.getDataObjFormatterName()), value);
                                if (fmtStr != null)
                                {
                                    strBuf.append(fmtStr);
                                }
                                
                            } else if (field.getUiFieldFormatterName() != null )
                            {
                                UIFieldFormatterIFace fmt = UIFieldFormatterMgr.getInstance().getFormatter(field.getUiFieldFormatterName());
                                if (fmt != null)
                                {
                                    strBuf.append(fmt.formatToUI(value));
                                } else
                                {
                                    strBuf.append(value);
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
    @SuppressWarnings("unchecked")
    protected String aggregateInternal(final Collection<?> items, final DataObjAggregator agg)
    {
        if (agg != null)
        {
            StringBuilder aggStr = new StringBuilder(128);
            
            Collection<?> itemsAsCol = items;
//            if (StringUtils.isNotBlank(agg.getOrderFieldName()))
//            {
//            	try
//            	{
//            		String orderFld = agg.getOrderFieldName();
//            		String methodName = "get" + orderFld.substring(0,1).toUpperCase() + orderFld.substring(1);
//            		final Method orderFldGetter = agg.getDataClass().getMethod(methodName, (Class<?>[])null);
//            		if (Comparable.class.isAssignableFrom(orderFldGetter.getReturnType()))
//            		{
//            			List<Object> itemsList = new Vector<Object>();
//            			itemsList.addAll(itemsAsCol);
//            			Comparator<Object> comp = new Comparator<Object>() {
//
//            				@Override
//            				public int compare(Object o1, Object o2) {
//            					if (agg.getDataClass().isAssignableFrom(o1.getClass()) && agg.getDataClass().isAssignableFrom(o2.getClass()))
//            					{
//            						try
//            						{
//            							Comparable<Object> f1 = (Comparable<Object>)orderFldGetter.invoke(agg.getDataClass().cast(o1), (Object[])null);
//            							Comparable<Object> f2 = (Comparable<Object>)orderFldGetter.invoke(agg.getDataClass().cast(o2), (Object[])null);
//            							if (f1 != null)
//            							{
//            								return f1.compareTo(f2);
//            							}
//            							else if (f2 != null)
//            							{
//            								return -1;
//            							} 
//            							else
//            							{
//            								return 0;
//            							}
//            						}  catch (InvocationTargetException tex)
//            		            	{
//            		            		return 0;
//            		            	}catch (IllegalAccessException acex)
//            		            	{
//            		            		return 0;
//            		            	}catch (IllegalArgumentException arex)
//            		            	{
//            		            		return 0;
//            		            	}
//            					}
//            					return 0;
//            				}
//            			};
//            			Collections.sort(itemsList, comp);
//            			itemsAsCol = itemsList;
//            		}
//            	} catch (NoSuchMethodException mex)
//            	{
//            		//sorting was a bad idea
//            	} catch (SecurityException sex)
//            	{
//            		//sorting was a bad idea
//            	}            
//            }
            /*if (items != null && items.size() > 0)
            {
                if (items.iterator().next() instanceof Comparable<?>)
                {
                    List<Comparable<Object>> itemsList = new ArrayList<Comparable<Object>>();
                    for (Object obj : items)
                    {
                        itemsList.add((Comparable<Object>)obj);
                    }
                    Collections.sort(itemsList);
                    itemsAsCol = itemsList;
                }
            }*/
            
            int count = 0;
            for (Object obj : itemsAsCol)
            {
                if (obj != null)
                {
                    // only add a separator after the first element
                    if (count > 0)
                    {
                        aggStr.append(agg.getSeparator());
                    }
                    
                    if (agg.useIdentity() && obj instanceof FormDataObjIFace)
                    {
                        aggStr.append(((FormDataObjIFace)obj).getIdentityTitle());
                        
                    } else
                    {
                        aggStr.append(formatInternal(getInstance().getDataFormatter(obj, agg.getFormatName()), obj));
                    }
                    
                    //System.out.println(aggStr.toString());
                    
                    int aggCount = (agg.getCount() != null)? agg.getCount() : 0;
                    if (aggCount > 0 && count >= aggCount - 1)
                    {
                        // add the ending string at the end of the aggregated string and quit loop
                        String endingStr = agg.getEnding();
                        if (StringUtils.isNotEmpty(endingStr))
                        {
                            aggStr.append(endingStr);
                        }
                        break;
                    }
                }
                count++;
            }
            return aggStr.toString();
            
        }
        // else
        log.error("Aggegrator was null.");
        return null;
    }
    
    /**
     * Format a data object using a named formatter.
     * @param dataObj the data object for which fields will be formatted for it
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    public String format(final Object dataObj, final String formatName)
    {
        if (domFound && StringUtils.isNotEmpty(formatName))
        {
            DataObjSwitchFormatter sf = formatHash.get(formatName);
            if (sf != null)
            {
                DataObjDataFieldFormatIFace dff = sf.getDataFormatter(dataObj);
                if (dff != null)
                {
                    return formatInternal(dff, dataObj);
                    
                }
                // else
                log.error("Couldn't find DataObjDataFieldFormat for ["+sf.getName()+"] value["+dataObj+"]");
            } else
            {
                log.error("Couldn't find DataObjSwitchFormatter for class ["+formatName+"]"); 
            }
        }
        return null;
    }

    /**
     * Format a data object using a named formatter.
     * @param dataObj the data object for which fields will be formatted for it
     * @param dataClass the class for the data to be formatted
     * @return the string result of the format
     */
    public String format(final Object dataObj, final Class<?> dataClass)
    {
        if (dataObj == null)
        {
            return "";
        }
        
        if (domFound)
        {
            DataObjSwitchFormatter sf = formatClassHash.get(dataClass);
            if (sf != null)
            {
                DataObjDataFieldFormatIFace dff = sf.getDataFormatter(dataObj);
                if (dff != null)
                {
                    return formatInternal(dff, dataObj);
                }
                // else
                log.error("Couldn't find DataObjDataFieldFormat for ["+sf.getName()+"] value["+dataObj+"]");
            }
            // else
            log.error("Couldn't find DataObjSwitchFormatter for class ["+dataClass.getName()+"]");
        }
        return null;
    }

    /**
     * Format a data object using a named formatter.
     * @param dataObjs the array data objects for which fields will be formatted for it
     * @param formatName the name of the formatter to use
     * @return the string result of the format
     */
    public static String format(@SuppressWarnings("unused")final Object[] dataObjs, @SuppressWarnings("unused") final String formatName)
    {
        throw new RuntimeException("OK, I am used, so come and fix me up!");
        //return instance.formatInternal(dataObjs, formatName);
    }
    
    /**
     * @param aggName
     * @return
     */
    public DataObjAggregator getAggregator(final String aggName)
    {
        return aggHash.get(aggName);
    }
    
    /**
     * Aggregates all the items in a Collection into a string given a formatter.
     * @param items the collection of items
     * @param aggName the name of the aggregator to use
     * @return a string representing a collection of all the objects 
     */
    public String aggregate(final Collection<?> items, final String aggName)
    {
        if (domFound)
        {
            if (items != null && items.size() > 0)
            {
                DataObjAggregator agg = aggHash.get(aggName);
                if (agg != null)
                {
                    return aggregateInternal(items, agg);
                    
                }
                // else
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
    public String aggregate(final Collection<?> items, final Class<?> dataClass)
    {
        if (!domFound || items == null || dataClass == null)
        {
            return "";
        }
        
        DataObjAggregator defAgg = null;
        for (Enumeration<DataObjAggregator> e=aggHash.elements();e.hasMoreElements();)
        {
            DataObjAggregator agg = e.nextElement();
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
            return aggregateInternal(items, defAgg);
            
        }
        // else
        log.error("Could find aggregator of class ["+dataClass.getCanonicalName()+"]");
        return "";
    }
    
    /**
     * Returns a list of aggregators that match the class, the default (if there is one) is at the beginning of the list.
     * @param clazz the class of the data that the aggregator is used for.
     * @return return a list of aggregators that match the class
     */
    public List<DataObjAggregator> getAggregatorList(final Class<?> clazz)
    {
        Vector<DataObjAggregator> list = new Vector<DataObjAggregator>();
        DataObjAggregator defFormatter = null;
        
        for (Enumeration<DataObjAggregator> e=aggHash.elements();e.hasMoreElements();)
        {
            DataObjAggregator f = e.nextElement();
            if (clazz == f.getDataClass())
            {
                if (f.isDefault() && defFormatter == null)
                {
                    defFormatter = f;
                } else
                {
                    list.add(f);
                }
            }
        }
        if (defFormatter != null)
        {
            list.insertElementAt(defFormatter, 0);
        }
        return list;
    }

    /**
     * Generic method that creates a unique name for an object in a hash if it doesn't yet have one
     * @param <T>
     * @param prefix
     * @param separator
     * @param names
     * @return
     */
    protected static <T> String getUniqueName(final String prefix, final String separator, final Set<String> names)
    {
        // find a name that doesn't yet exist in the hash
        // name formation patter is prefix.i, where i is a counter
        int i = 1;
        String name = prefix + separator + Integer.toString(i);
        while (names.contains(name))
        {
            name = prefix + separator + Integer.toString(++i);
        }
        return name;
    }

    /**
     * Adds a new aggregator.
     * @param aggregator the aggregator to add
     */
    public void addAggregator(final DataObjAggregator aggregator)
    {
        aggHash.put(aggregator.getName(), aggregator);
        aggClassHash.put(aggregator.getDataClass(), aggregator);
        hasChanged = true;
    }
    
    /**
     * Deletes a aggregator from the hashes
     * @param aggregator the aggregator to remove
     */
    public void removeAggregator(final DataObjAggregator aggregator)
    {
        aggHash.remove(aggregator.getName());
        aggClassHash.remove(aggregator.getName());
        hasChanged = true;
    }
    
    /**
     * Returns the instance to the singleton
     * @return  the instance to the singleton
     */
    public static DataObjFieldFormatMgr getInstance()
    {
        if (instance != null)
        {
            return instance;
        }
        
        if (StringUtils.isEmpty(factoryName))
        {
            instance = new DataObjFieldFormatMgr();
            
        } else
        {
            
            // else
            String factoryNameStr = AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {
                    public String run() {
                        return System.getProperty(factoryName);
                        }
                    });
                
            if (StringUtils.isNotEmpty(factoryNameStr)) 
            {
                try 
                {
                    instance = (DataObjFieldFormatMgr)Class.forName(factoryNameStr).newInstance();
                     
                } catch (Exception e) 
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjFieldFormatMgr.class, e);
                    InternalError error = new InternalError("Can't instantiate DataObjFieldFormatMgr factory " + factoryNameStr);
                    error.initCause(e);
                    throw error;
                }
            }
            
            if (instance == null)
            {
                instance = new DataObjFieldFormatMgr();
            }
        }
        
        // now that all formats have been loaded, set table/field/formatter info\
        // must be executed after the instance is set
        for ( DataObjSwitchFormatter format : instance.formatHash.values() )
        {
            format.setTableAndFieldInfo();
        }

        return instance;
    }

    public Hashtable<String, DataObjSwitchFormatter> getFormatHash()
    {
        return formatHash;
    }

    public void setFormatHash(Hashtable<String, DataObjSwitchFormatter> formatHash)
    {
        this.formatHash = formatHash;
    }

    public Hashtable<Class<?>, DataObjSwitchFormatter> getFormatClassHash()
    {
        return formatClassHash;
    }

    public void setFormatClassHash(
            Hashtable<Class<?>, DataObjSwitchFormatter> formatClassHash)
    {
        this.formatClassHash = formatClassHash;
    }

    public Hashtable<String, DataObjAggregator> getAggHash()
    {
        return aggHash;
    }

    public void setAggHash(Hashtable<String, DataObjAggregator> aggHash)
    {
        this.aggHash = aggHash;
    }

    public Hashtable<Class<?>, DataObjAggregator> getAggClassHash()
    {
        return aggClassHash;
    }

    public void setAggClassHash(Hashtable<Class<?>, DataObjAggregator> aggClassHash)
    {
        this.aggClassHash = aggClassHash;
    }
    
    /**
     * Sets DataObjFieldFormatMgr instance to null. 
     * Next getInstance() call will force complete rebuild. 
     * 
     * Not sure if synchronization helps if getInstance() is not synchronized.
     * However, risk of concurrent access is minimal, because this is only called 
     * after Schema localization changes, which can only be performed when no other tasks are open.
     * 
     */
    public synchronized static void clear()
    {
    	instance = null;
    }
}
