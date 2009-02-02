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
package edu.ku.brc.af.ui.forms.formatters;

import java.math.BigDecimal;
import java.security.AccessController;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.db.AutoNumberIFace;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.UIRegistry;

/**
 * The Format Manager; reads in all the formats from XML
 * 
 * @code_status Beta L
 * @author rods, ricardo
 * 
 */
public class UIFieldFormatterMgr implements AppPrefsChangeListener
{
    public static final String                                 factoryName    = "edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr";

    private static final Logger                                log            = Logger.getLogger(UIFieldFormatterMgr.class);

    protected static UIFieldFormatterMgr                       instance    = null;
    protected static boolean                                   doingLocal    = false;

    protected boolean                                          hasChanged  = false;
    protected Hashtable<String, UIFieldFormatterIFace>         hash        = new Hashtable<String, UIFieldFormatterIFace>();
    protected Hashtable<Class<?>, Vector<UIFieldFormatterIFace>> classToListHash = new Hashtable<Class<?>, Vector<UIFieldFormatterIFace>>();

    /**
     * Protected Constructor
     */
    protected UIFieldFormatterMgr()
    {
        load();
    }

    /**
     * Copy constructor.
     * @param source
     */
    public UIFieldFormatterMgr(final UIFieldFormatterMgr source)
    {
        setHash(source.getHash());
    }

    /**
     * Copies the internal data structures.
     * @param source a format manager
     */
    public void copyFrom(final UIFieldFormatterMgr source)
    {
        this.hasChanged = source.hasChanged;
        setHash(source.getHash());
    }

    /**
     * Returns the instance to the singleton
     * 
     * @return the instance to the singleton
     */
    public static UIFieldFormatterMgr getInstance()
    {
        if (instance != null)
        {
            return instance;
        }

        if (StringUtils.isEmpty(factoryName))
        {
            return instance = new UIFieldFormatterMgr();
        }

        // else
        String factoryNameStr = AccessController
                .doPrivileged(new java.security.PrivilegedAction<String>()
                {
                    public String run()
                    {
                        return System.getProperty(factoryName);
                    }
                });

        if (StringUtils.isNotEmpty(factoryNameStr))
        {
            try
            {
                return instance = (UIFieldFormatterMgr) Class.forName(factoryNameStr).newInstance();

            } catch (Exception e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIFieldFormatterMgr.class, e);
                InternalError error = new InternalError("Can't instantiate UIFieldFormatterMgr factory " + factoryNameStr);
                error.initCause(e);
                throw error;
            }
        }
        // should not happen
        throw new RuntimeException("Can't instantiate UIFieldFormatterMgr factory ["+ factoryNameStr + "]");
    }

    /**
     * @param doingLocal the doingLocal to set
     */
    public static void setDoingLocal(boolean doingLocal)
    {
        UIFieldFormatterMgr.doingLocal = doingLocal;
    }

    /**
     * @return the formatters list
     */
    public List<UIFieldFormatterIFace> getFormatters()
    {
        Vector<UIFieldFormatterIFace> list = new Vector<UIFieldFormatterIFace>();
        for (UIFieldFormatterIFace fmt : hash.values())
        {
            boolean isUIF = fmt instanceof UIFieldFormatter;
            if (!isUIF || ((UIFieldFormatter) fmt).getType() == UIFieldFormatter.FormatterType.generic)
            {
                list.add(fmt);
            }
        }
        return list;
    }

    /**
     * Returns a formatter by name
     * 
     * @param name the name of the format
     * @return return a formatter if it is there, returns null if it isn't
     */
    protected UIFieldFormatterIFace getFormatterInternal(final String name)
    {
        return StringUtils.isNotEmpty(name) ? hash.get(name) : null;
    }

    /**
     * Returns a formatter by name
     * 
     * @param name the name of the format
     * @return return a formatter if it is there, returns null if it isn't
     */
    public UIFieldFormatterIFace getFormatter(final String name)
    {
        return getFormatterInternal(name);
    }

    /**
     * Returns a formatter by data class. Returns the "default" formatter and if
     * no default is set it returns the first one it finds.
     * 
     * @param clazz the class of the data that the formatter is used for.
     * @return return a formatter if it is there, returns null if it isn't
     */
    public UIFieldFormatterIFace getFormatterInternal(final Class<?> clazz)
    {
        UIFieldFormatterIFace formatter = null;
        for (Enumeration<UIFieldFormatterIFace> e = hash.elements(); e.hasMoreElements();)
        {
            UIFieldFormatterIFace f = e.nextElement();
            if (clazz == f.getDataClass())
            {
                if (f.isDefault())
                {
                    return f;
                }
                if (formatter == null)
                {
                    formatter = f;
                }
            }
        }
        return formatter;
    }

    /**
     * Returns a formatter by data class. Returns the "default" formatter and if
     * no default is set it returns the first one it finds.
     * 
     * @param clazz the class of the data that the formatter is used for.
     * @return return a formatter if it is there, returns null if it isn't
     */
    public UIFieldFormatterIFace getFormatter(final Class<?> clazz)
    {
        return getFormatterInternal(clazz);
    }

    /**
     * Returns a Date Formatter for a given type of Partial Date.
     * 
     * @param type the type of Partial Date formatter.
     * @return the formatter
     */
    public UIFieldFormatterIFace getDateFormmater(final UIFieldFormatter.PartialDateEnum type)
    {
        for (Enumeration<UIFieldFormatterIFace> e = hash.elements(); e.hasMoreElements();)
        {
            UIFieldFormatterIFace f = e.nextElement();
            // System.out.println("["+Date.class+"]["+f.getDataClass()+"]
            // "+f.getPartialDateType());
            if (Date.class == f.getDataClass() &&
                 f.getPartialDateType() == type)
            {
                return f;
            }
        }
        return null;
    }

    /**
     * Returns a list of formatters that match the class, the default (if there
     * is one) is at the beginning of the list.
     * 
     * @param isForPartial indicates to get Partial Date formatters
     * @return return a list of formatters that match the class
     */
    public List<UIFieldFormatterIFace> getDateFormatterList(final boolean isForPartial)
    {
        Vector<UIFieldFormatterIFace> list = new Vector<UIFieldFormatterIFace>();
        for (UIFieldFormatterIFace f : hash.values())
        {
            if (f.isDate())
            {
                boolean isPartial = f.getPartialDateType() == UIFieldFormatterIFace.PartialDateEnum.Month ||
                                    f.getPartialDateType() == UIFieldFormatterIFace.PartialDateEnum.Year;
                if (isForPartial == isPartial)
                {
                    list.add(f);
                }
            }
        }
        return list;
    }

    /**
     * Returns a list of formatters that match the class, the default (if there
     * is one) is at the beginning of the list.
     * 
     * @param clazz the class of the data that the formatter is used for.
     * @return return a list of formatters that match the class
     */
    public List<UIFieldFormatterIFace> getFormatterList(final Class<?> clazz)
    {
        return getFormatterList(clazz, null);
    }

    /**
     * Returns a list of formatters that match the class, the default (if there
     * is one) is at the beginning of the list.
     * 
     * @param clazz the class of the data that the formatter is used for.
     * @return return a list of formatters that match the class
     */
    public List<UIFieldFormatterIFace> getFormatterList(final Class<?> clazz,
                                                        final String fieldName)
    {
        if (fieldName == null)
        {
            Vector<UIFieldFormatterIFace> list = classToListHash.get(clazz);
            if (list != null)
            {
                return list;
            }
        }
        
        Vector<UIFieldFormatterIFace> list = new Vector<UIFieldFormatterIFace>();
        
        UIFieldFormatterIFace defFormatter = null;
        for (Enumeration<UIFieldFormatterIFace> e = hash.elements(); e.hasMoreElements();)
        {
            UIFieldFormatterIFace fmt = e.nextElement();
            //log.debug(fmt.getTitle()+" -  "+fmt.getDataClass().getSimpleName());
            if (clazz == fmt.getDataClass() && (fieldName == null || 
                (fieldName.equals(fmt.getFieldName()) || fieldName.equals("*"))))
            {
                if (fmt.isDefault() && defFormatter == null)
                {
                    defFormatter = fmt;
                } else
                {
                    list.add(fmt);
                }
            }
        }
        
        if (defFormatter != null)
        {
            list.insertElementAt(defFormatter, 0);
        }
        
        if (fieldName == null)
        {
            classToListHash.put(clazz, list);
        }
        
        return list;
    }
    
    /**
     * Clear the vectors from the class hash.
     */
    protected void cleanClassToListHash()
    {
        for (Class<?> key : classToListHash.keySet())
        {
            Vector<UIFieldFormatterIFace> list = classToListHash.get(key);
            list.clear();
        }
        classToListHash.clear();
    }

    /**
     * Gets a unique name for a formatter if it doesn't yet have one
     */
    private String getFormatterUniqueName(final UIFieldFormatterIFace formatter)
    {
        String name = formatter.getName();

        if (StringUtils.isEmpty(name))
        {
            // find a formatter name that doesn't yet exist in the hash
            // name formation patter is <field name>.i where i is a counter
            int         i      = 1;
            Set<String> names  = hash.keySet();
            String      prefix = formatter.getFieldName();
            
            name = prefix + "." + Integer.toString(i);
            while (names.contains(name))
            {
                name = prefix + "." + Integer.toString(++i);
            }
        }
        formatter.setName(name);
        return null;
    }

    /**
     * Adds a new formatter
     */
    public void addFormatter(final UIFieldFormatterIFace formatter)
    {
        getFormatterUniqueName(formatter);
        
        Vector<UIFieldFormatterIFace> list = classToListHash.get(formatter.getDataClass());
        if (list == null)
        {
            list = new Vector<UIFieldFormatterIFace>();
            classToListHash.put(formatter.getDataClass(), list);
        }
        
        list.add(formatter);
        hash.put(formatter.getName(), formatter);
    }

    /**
     * Deletes a formatter from the
     */
    public void removeFormatter(UIFieldFormatterIFace formatter)
    {
        hash.remove(formatter.getName());
        
        Vector<UIFieldFormatterIFace> list = classToListHash.get(formatter.getDataClass());
        if (list != null)
        {
            list.remove(formatter);
            classToListHash.put(formatter.getDataClass(), list);
        }
    }

    /**
     * Returns the DOM it is suppose to load the formatters from.
     * 
     * @return Returns the DOM it is suppose to load the formatters from.
     */
    protected Element getDOM() throws Exception
    {
        throw new RuntimeException("Not implemented.");
    }

    /**
     * Loads the formats from the config file.
     * 
     */
    public void load()
    {
        hash.clear();
        cleanClassToListHash();
        
        try
        {
            Element root = getDOM();
            if (root != null)
            {
                boolean hasDefault = false;
                List<?> formats = root.selectNodes("/formats/format");
                for (Object fObj : formats)
                {
                    Element formatElement = (Element) fObj;

                    String  name      = formatElement.attributeValue("name");
                    String  fType     = formatElement.attributeValue("type");
                    String  fieldName = XMLHelper.getAttr(formatElement, "fieldname", "*");
                    String  dataClassName = formatElement .attributeValue("class");
                    int     precision = XMLHelper.getAttr(formatElement, "precision", 12);
                    int     scale     = XMLHelper.getAttr(formatElement, "scale", 2);
                    boolean isDefault = XMLHelper.getAttr(formatElement, "default", false);
                    boolean isSystem  = XMLHelper.getAttr(formatElement, "system", false);

                    // Make sure we only have one default.
                    if (isDefault)
                    {
                        if (!hasDefault)
                        {
                            hasDefault = true;
                        } else
                        {
                            isDefault = false;
                        }
                    }
                    
                    AutoNumberIFace autoNumberObj = null;
                    Element autoNumberElement = (Element) formatElement.selectSingleNode("autonumber");
                    if (autoNumberElement != null)
                    {
                        String autoNumberClassName = autoNumberElement.getTextTrim();
                        if (StringUtils.isNotEmpty(autoNumberClassName) &&
                            StringUtils.isNotEmpty(dataClassName) &&
                            StringUtils.isNotEmpty(fieldName))
                        {
                            autoNumberObj = createAutoNumber(autoNumberClassName, dataClassName,fieldName);

                        } else
                        {
                            throw new RuntimeException(
                                    "The class cannot be empty for an external formatter! ["
                                            + name
                                            + "] or missing field name ["
                                            + fieldName
                                            + "] or missing data Class name ["
                                            + dataClassName + "]");
                        }
                    }
                    
                    
                    UIFieldFormatterIFace formatter = null;
                    
                    Element external = (Element) formatElement.selectSingleNode("external");
                    if (external != null)
                    {
                        String externalClassName = external.getTextTrim();
                        if (StringUtils.isNotEmpty(externalClassName))
                        {
                            try
                            {
                                formatter = Class.forName(externalClassName).asSubclass(UIFieldFormatterIFace.class).newInstance();
                                formatter.setName(name);
                                formatter.setAutoNumber(autoNumberObj);
                                formatter.setDefault(isDefault);
                                
                                hash.put(name, formatter);

                            } catch (Exception ex)
                            {
                                log.error(ex);
                                ex.printStackTrace();
                                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIFieldFormatterMgr.class, ex);
                            }
                        } else
                        {
                            throw new RuntimeException("The value cannot be empty for an external formatter! ["+ name + "]");
                        }
                    }
                    
                    List<?>                     fieldsList = formatElement.selectNodes("field");
                    List<UIFieldFormatterField> fields     = new ArrayList<UIFieldFormatterField>();
                    boolean                     isInc      = false;
                    String                      partialDateTypeStr = formatElement.attributeValue("partialdate");
                    for (Object fldObj : fieldsList)
                    {
                        Element fldElement = (Element) fldObj;

                        int     size    = XMLHelper.getAttr(fldElement, "size", 1);
                        String  value   = fldElement.attributeValue("value");
                        String  typeStr = fldElement.attributeValue("type");
                        boolean increm  = XMLHelper.getAttr(fldElement, "inc", false);
                        boolean byYear  = false;

                        UIFieldFormatterField.FieldType type = null;
                        try
                        {
                            type = UIFieldFormatterField.FieldType.valueOf(typeStr);

                        } catch (Exception ex)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIFieldFormatterMgr.class, ex);
                            log.error("[" + typeStr + "]" + ex.toString());
                        }

                        if (type == UIFieldFormatterField.FieldType.year)
                        {
                            size = 4;
                            byYear = XMLHelper.getAttr(fldElement, "byyear", false);
                        }

                        fields.add(new UIFieldFormatterField(type, size, value, increm, byYear));
                        if (increm)
                        {
                            isInc = true;
                        }
                    }

                    // set field type
                    UIFieldFormatter.FormatterType   type            = UIFieldFormatter.FormatterType.generic;
                    UIFieldFormatter.PartialDateEnum partialDateType = UIFieldFormatter.PartialDateEnum.None;
                    if (StringUtils.isNotEmpty(fType) && fType.equals("numeric"))
                    {
                        type = UIFieldFormatter.FormatterType.numeric;
                        
                    } else if (StringUtils.isNotEmpty(fType) && fType.equals("date"))
                    {
                        type = UIFieldFormatter.FormatterType.date;
                        if (StringUtils.isNotEmpty(partialDateTypeStr))
                        {
                            partialDateType = UIFieldFormatter.PartialDateEnum.valueOf(partialDateTypeStr);
                        } else
                        {
                            partialDateType = UIFieldFormatter.PartialDateEnum.Full;
                        }
                    }
                    
                    Class<?> dataClass = null;
                    if (StringUtils.isNotEmpty(dataClassName))
                    {
                        try
                        {
                            dataClass = Class.forName(dataClassName);
                        } catch (Exception ex)
                        {
                            log.error("Couldn't load class [" + dataClassName + "] for [" + name + "]");
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIFieldFormatterMgr.class, ex);
                        }

                    } else if (StringUtils.isNotEmpty(fType) && fType.equals("date"))
                    {
                        dataClass = Date.class;
                    }

                    if (formatter == null)
                    {
                        formatter = new UIFieldFormatter(name, isSystem, fieldName, type, partialDateType,
                                                         dataClass, isDefault, isInc, fields);
                        hash.put(name, formatter);
                    } else
                    {
                        formatter.setPartialDateType(partialDateType);
                    }
                    
                    if (formatter instanceof UIFieldFormatter)
                    {
                        UIFieldFormatter fmt = (UIFieldFormatter)formatter;
                        fmt.setType(type);
                        
                        if (type == UIFieldFormatter.FormatterType.date && fields.size() == 0)
                        {
                            addFieldsForDate(fmt);

                        } else if (type == UIFieldFormatter.FormatterType.numeric)
                        {
                            fmt.setPrecision(precision);
                            fmt.setScale(scale);
                            addFieldsForNumeric(fmt);
                        }
                    }

                    formatter.setAutoNumber(autoNumberObj);
                }
            } else
            {
                log.debug("Couldn't open uiformatters.xml");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIFieldFormatterMgr.class, ex);
            log.error(ex);
        }
    }

    /**
     * @param source
     */
    public void applyChanges(final UIFieldFormatterMgr source)
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
     * 
     */
    public void save()
    {
        StringBuilder sb = new StringBuilder(1024);

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<formats>\n");

        // sort formatters by name, then save them to db
        Vector<UIFieldFormatterIFace> formatVector = new Vector<UIFieldFormatterIFace>(instance.hash.values());
        Collections.sort(formatVector, new Comparator<UIFieldFormatterIFace>()
        {
            public int compare(UIFieldFormatterIFace o1, UIFieldFormatterIFace o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        for (UIFieldFormatterIFace format : formatVector)
        {
            format.toXML(sb);
        }
        sb.append("\n</formats>\n");

        saveXML(sb.toString());
    }

    /**
     * Persists the XML.
     * 
     * @param xml
     *            the xml to be persisted.
     */
    protected void saveXML(final String xml)
    {
        throw new RuntimeException("Not implemented.");
    }

    /**
     * @param fieldInfo
     * @return
     */
    public static UIFieldFormatterFactory getFormatFactory(DBFieldInfo fieldInfo)
    {
        if (String.class.getCanonicalName().equals(fieldInfo.getType()))
        {
            // text or string fields
            return new UITextFieldFormatterFactory(fieldInfo);
        }
        else if (Float.class.getCanonicalName().equals(fieldInfo.getType()))
        {
            // float fields
            return new UIFloatFieldFormatterFactory(fieldInfo);
        }
        else if (Integer.class.getCanonicalName().equals(fieldInfo.getType()))
        {
            // float fields
            return new UIIntegerFieldFormatterFactory(fieldInfo);
        }
        else
        {
            // unknown type
            return null;
        }
    }
    
    /**
     * Creates and returns an autonumbering object for the formatter.
     * 
     * @param autoNumberClassName the class name to be instantiated
     * @param dataClassName the data class name (which the auto number will operate on
     * @param fieldName  the field that will be incremented in the dataClassName object
     * @return the auto number object or null
     */
    protected static AutoNumberIFace createAutoNumber(final String autoNumberClassName, 
                                                      final String dataClassName,
                                                      final String fieldName)
    {
        AutoNumberIFace autoNumberObj = null;
        try
        {
            autoNumberObj = Class.forName(autoNumberClassName).asSubclass(AutoNumberIFace.class).newInstance();
            Properties props = new Properties();
            props.put("class", dataClassName);
            props.put("field", fieldName);
            autoNumberObj.setProperties(props);

        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIFieldFormatterMgr.class, ex);
            log.error(ex);
            ex.printStackTrace();
        }
        return autoNumberObj;
    }

    /**
     * Constructs a the fields for a date formatter if the user didn't specify
     * them; it gets the fields for the date from the dat preference
     * 
     * @param formatter
     *            the formatter to be augmented
     */
    protected void addFieldsForDate(final UIFieldFormatter formatter)
    {
        formatter.getFields().clear();
        
        String prefPostFix = "";
        
        UIFieldFormatter.PartialDateEnum partialType = formatter.getPartialDateType();
        if (partialType == UIFieldFormatter.PartialDateEnum.Month)
        {
            prefPostFix = "mon";
        } else if (partialType == UIFieldFormatter.PartialDateEnum.Year)
        {
            prefPostFix = "year";
        }
        
        DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat"+prefPostFix);

        StringBuilder newFormatStr = new StringBuilder();
        String        formatStr    = scrDateFormat.getSimpleDateFormat().toPattern();
        boolean       wasConsumed  = false;
        char          currChar     = ' ';

        for (int i = 0; i < formatStr.length(); i++)
        {
            char c = formatStr.charAt(i);
            if (c != currChar)
            {
                if (c == 'M') // make sure we consume them
                {
                    if (partialType == UIFieldFormatter.PartialDateEnum.Full
                            || partialType == UIFieldFormatter.PartialDateEnum.Month)
                    {
                        String s = "";
                        s += c;
                        s += c;
                        UIFieldFormatterField f = new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric, 2, s.toUpperCase(), false);
                        formatter.getFields().add(f);
                        currChar = c;
                        newFormatStr.append(c);
                        newFormatStr.append(c);

                    } else
                    {
                        wasConsumed = true;
                    }

                } else if (c == 'd')
                {
                    if (partialType == UIFieldFormatter.PartialDateEnum.Full)
                    {
                        String s = "";
                        s += c;
                        s += c;
                        UIFieldFormatterField f = new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric, 2, s.toUpperCase(), false);
                        formatter.getFields().add(f);
                        currChar = c;
                        newFormatStr.append(c);
                        newFormatStr.append(c);

                    } else
                    {
                        wasConsumed = true;
                    }

                } else if (c == 'y')
                {
                    int start = i;
                    while (i < formatStr.length() && formatStr.charAt(i) == 'y')
                    {
                        i++;
                        newFormatStr.append(c);
                    }
                    UIFieldFormatterField f;
                    if (i - start > 2)
                    {
                        f = new UIFieldFormatterField(
                                UIFieldFormatterField.FieldType.numeric, 4,
                                "YYYY", false);
                    } else
                    {
                        f = new UIFieldFormatterField(
                                UIFieldFormatterField.FieldType.numeric, 2,
                                "YY", false);
                    }
                    formatter.getFields().add(f);
                    currChar = c;
                    i--;

                } else if (!wasConsumed)
                {
                    String s = "";
                    s += c;
                    UIFieldFormatterField f = new UIFieldFormatterField(UIFieldFormatterField.FieldType.separator, 1, s,false);
                    formatter.getFields().add(f);
                    newFormatStr.append(c);

                } else
                {
                    wasConsumed = false;
                }
            }
        } // for

        if (partialType == UIFieldFormatter.PartialDateEnum.Full)
        {
            formatter.setDateWrapper(scrDateFormat);
        } else
        {
            scrDateFormat.setSimpleDateFormat(new SimpleDateFormat(newFormatStr.toString()));
            formatter.setDateWrapper(scrDateFormat);
        }
    }

    /**
     * Constructs a the fields for a numeric formatter.
     * 
     * @param formatter
     *            the formatter to be augmented
     */
    protected void addFieldsForNumeric(final UIFieldFormatter formatter)
    {
        int len;
        Class<?> cls = formatter.getDataClass();
        if (cls == BigDecimal.class)
        {
            len = formatter.getPrecision() + formatter.getScale() + 1;
        } else

        {
            if (cls == Long.class)
            {
                len = Long.toString(Long.MAX_VALUE).length();
            } else if (cls == Integer.class)
            {
                len = Integer.toString(Integer.MAX_VALUE).length();
            } else if (cls == Short.class)
            {
                len = Short.toString(Short.MAX_VALUE).length();
            } else if (cls == Byte.class)
            {
                len = Byte.toString(Byte.MAX_VALUE).length();
            } else if (cls == Double.class)
            {
                len = String.format("%f", Double.MAX_VALUE).length();
            } else if (cls == Float.class)
            {
                len = String.format("%f", Float.MAX_VALUE).length();

            } else
            {
                throw new RuntimeException("Missing case for numeric class ["+ cls.getName() + "]");
            }
            len = Math.min(len, 10);
        }
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
        {
            sb.append(' ');
        }
        formatter.getFields().add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric, len, sb.toString(), false));
    }

    /**
     * @param isAutoNumber
     * @param fieldType
     * @param length
     * @return
     */
    public static String getFormatterPattern(final boolean isAutoNumber,
            final UIFieldFormatterField.FieldType fieldType, final int length)
    {
        char defChar = 'A';
        if (fieldType != null)
        {
            switch (fieldType)
            {
            case numeric:
                defChar = 'N';
                break;

            case alphanumeric:
                defChar = 'A';
                break;

            case alpha:
                defChar = 'a';
                break;

            case separator:
                defChar = '#';
                break;

            case year:
                defChar = 'Y';
                break;

            case anychar:
                defChar = ' '; // we don't need to localize this
                break;

            default:
                defChar = 'X';
                break;
            }
        } else if (!isAutoNumber)
        {
            throw new RuntimeException(
                    "Can't have a null fieldType and not be autonumbered");
        }

        char pChar;
        if (isAutoNumber)
        {
            pChar = getAutoNumberPatternChar();

        } else if (fieldType != UIFieldFormatterField.FieldType.anychar)
        {
            String key = "UIFieldFormatterMgr." + fieldType.toString();
            String charPattern = UIRegistry.getResourceString(key);
            pChar = charPattern.length() > 0 ? charPattern.charAt(0) : defChar;
        } else
        {
            pChar = defChar;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            sb.append(pChar);
        }
        return sb.toString();
    }

    /**
     * @return
     */
    public Hashtable<String, UIFieldFormatterIFace> getHash()
    {
        return hash;
    }

    /**
     * @param hash
     */
    public void setHash(Hashtable<String, UIFieldFormatterIFace> hash)
    {
        this.hash = hash;
    }
    
    /**
     * 
     */
    public void reloadDateFormatter()
    {
        for (UIFieldFormatterIFace fmt : hash.values())
        {
            if (fmt.getDataClass() == Date.class && fmt instanceof UIFieldFormatter)
            {
                addFieldsForDate((UIFieldFormatter)fmt);
            }
        }
    }

    /**
     * @return the localized char used to represent autonumbered.
     */
    public static char getAutoNumberPatternChar()
    {
        String key = "UIFieldFormatterMgr.autonumber";
        String charPattern = UIRegistry.getResourceString(key);
        return charPattern.length() > 0 ? charPattern.charAt(0) : '#';
    }

    //-------------------------------------------------
    // AppPrefsChangeListener
    //-------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsChangeListener#preferenceChange(edu.ku.brc.af.prefs.AppPrefsChangeEvent)
     */
    public void preferenceChange(AppPrefsChangeEvent evt)
    {
        if (evt.getKey().equals("ui.formatting.scrdateformat")) //$NON-NLS-1$
        {
            reloadDateFormatter();
        }
    }

    
    /*
     * public static void test() { Properties props = new Properties();
     * props.put("class", "edu.ku.brc.specify.datamodel.Accession");
     * props.put("field", "number"); UIFieldFormatterIFace formatter =
     * UIFieldFormatterMgr.getFormatter("AccessionNumber"); AutoNumberGeneric
     * generic = new AutoNumberGeneric(props); System.out.println("New
     * Num["+formatter.toPattern()+"]"); System.out.println("Next
     * Num["+generic.getNextNumber(formatter, formatter.toPattern())+"]");
     * 
     * props = new Properties(); props.put("class",
     * "edu.ku.brc.specify.datamodel.CollectionObject"); props.put("field",
     * "catalogNumber"); formatter =
     * UIFieldFormatterMgr.getFormatter("AccessionNumber"); CollectionAutoNumber
     * colAtuoNum = new CollectionAutoNumber(props); System.out.println("New
     * Num["+formatter.toPattern()+"]"); System.out.println("Next
     * Num["+colAtuoNum.getNextNumber(formatter, formatter.toPattern())+"]"); }
     */
}
