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
package edu.ku.brc.ui.forms.formatters;

import java.math.BigDecimal;
import java.security.AccessController;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.dbsupport.AutoNumberIFace;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatter.FormatterType;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatter.PartialDateEnum;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterField.FieldType;

/**
 * The Format Manager; reads in all the formats from XM
 * @code_status Beta
 *L
 * @author rods
 *
 */
public class UIFieldFormatterMgr
{
    public static final String factoryName = "edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr";
    
    private static final Logger log = Logger.getLogger(UIFieldFormatterMgr.class);
    
    protected static UIFieldFormatterMgr instance = null;
    protected static boolean             doingLocal = false;

    protected Hashtable<String, UIFieldFormatterIFace> hash = new Hashtable<String, UIFieldFormatterIFace>();

    /**
     * Protected Constructor
     */
    protected UIFieldFormatterMgr()
    {
        load();
    }

    /**
     * Returns the instance to the singleton
     * @return  the instance to the singleton
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
        String factoryNameStr = AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (StringUtils.isNotEmpty(factoryNameStr)) 
        {
            try 
            {
                return instance = (UIFieldFormatterMgr)Class.forName(factoryNameStr).newInstance();
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate UIFieldFormatterMgr factory " + factoryNameStr);
                error.initCause(e);
                throw error;
            }
        }
        // should not happen
        throw new RuntimeException("Can't instantiate UIFieldFormatterMgr factory [" + factoryNameStr+"]");
    }

    /**
     * @param doingLocal the doingLocal to set
     */
    public static void setDoingLocal(boolean doingLocal)
    {
        UIFieldFormatterMgr.doingLocal = doingLocal;
    }
    
    /**
     * @return
     */
    public static List<UIFieldFormatterIFace> getFormatters()
    {
        Vector<UIFieldFormatterIFace> list = new Vector<UIFieldFormatterIFace>();
        for (UIFieldFormatterIFace fmt : instance.hash.values())
        {
            boolean isUIF = fmt instanceof UIFieldFormatter;
            if (!isUIF || ((UIFieldFormatter)fmt).getType() == UIFieldFormatter.FormatterType.generic)
            {
                list.add(fmt);
            }
        }
        return list;
    }

    /**
     * Returns a formatter by name
     * @param name the name of the format
     * @return return a formatter if it is there, returns null if it isn't
     */
    protected UIFieldFormatterIFace getFormatterInternal(final String name)
    {
        return StringUtils.isNotEmpty(name) ? hash.get(name) : null;
    }

    /**
     * Returns a formatter by name
     * @param name the name of the format
     * @return return a formatter if it is there, returns null if it isn't
     */
    public static UIFieldFormatterIFace getFormatter(final String name)
    {
        return getInstance().getFormatterInternal(name);

    }

    /**
     * Returns a formatter by data class. Returns the "default" formatter and if no default
     * is set it returns the first one it finds.
     * @param clazz the class of the data that the formatter is used for.
     * @return return a formatter if it is there, returns null if it isn't
     */
    public UIFieldFormatterIFace getFormatterInternal(final Class<?> clazz)
    {
        UIFieldFormatterIFace formatter = null;
        for (Enumeration<UIFieldFormatterIFace> e=hash.elements();e.hasMoreElements();)
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
     * Returns a formatter by data class. Returns the "default" formatter and if no default
     * is set it returns the first one it finds.
     * @param clazz the class of the data that the formatter is used for.
     * @return return a formatter if it is there, returns null if it isn't
     */
    public static UIFieldFormatterIFace getFormatter(final Class<?> clazz)
    {
        return getInstance().getFormatterInternal(clazz);
    }
    
    /**
     * Returns a Date Formatter for a given type of Partial Date.
     * @param type the type of Partial Date formatter.
     * @return the formatter
     */
    public static UIFieldFormatterIFace getDateFormmater(UIFieldFormatter.PartialDateEnum type)
    {
        for (Enumeration<UIFieldFormatterIFace> e=getInstance().hash.elements();e.hasMoreElements();)
        {
            UIFieldFormatterIFace f = e.nextElement();
            //System.out.println("["+Date.class+"]["+f.getDataClass()+"] "+f.getPartialDateType());
            if ((Date.class == f.getDataClass() || Date.class == f.getDataClass())  && f.getPartialDateType() == type)
            {
                return f;
            }
        }
        return null;
    }
    
    /**
     * Returns a list of formatters that match the class, the default (if there is one) is at the beginning of the list.
     * @param isForPartial indicates to get Partial Date formatters
     * @return return a list of formatters that match the class
     */
    public static List<UIFieldFormatterIFace> getDateFormatterList(final boolean isForPartial)
    {
        Vector<UIFieldFormatterIFace> list = new Vector<UIFieldFormatterIFace>();
        for (Enumeration<UIFieldFormatterIFace> e=getInstance().hash.elements();e.hasMoreElements();)
        {
            UIFieldFormatterIFace f = e.nextElement();
            if (f.isDate())
            {
                boolean isPartial = f.getName().indexOf("Partial") > -1;
                if ((isForPartial && isPartial) || (!isForPartial && !isPartial))
                {
                    list.add(f);
                }
            }
        }
        return list;
    }
    
    /**
     * Returns a list of formatters that match the class, the default (if there is one) is at the beginning of the list.
     * @param clazz the class of the data that the formatter is used for.
     * @return return a list of formatters that match the class
     */
    public static List<UIFieldFormatterIFace> getFormatterList(final Class<?> clazz)
    {
        return getFormatterList(clazz, null);
    }

    /**
     * Returns a list of formatters that match the class, the default (if there is one) is at the beginning of the list.
     * @param clazz the class of the data that the formatter is used for.
     * @return return a list of formatters that match the class
     */
    public static List<UIFieldFormatterIFace> getFormatterList(final Class<?> clazz,
                                                               final String fieldName)
    {
        Vector<UIFieldFormatterIFace> list = new Vector<UIFieldFormatterIFace>();
        UIFieldFormatterIFace         defFormatter = null;
        for (Enumeration<UIFieldFormatterIFace> e=getInstance().hash.elements();e.hasMoreElements();)
        {
            UIFieldFormatterIFace f = e.nextElement();
            if (clazz == f.getDataClass() && 
                (fieldName == null || (fieldName.equals(f.getFieldName()) || fieldName.equals("*"))))
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
     * Gets a unique name for a formatter if it doesn't yet have one
     */
    private String getFormatterUniqueName(UIFieldFormatterIFace formatter)
    {
    	String name = formatter.getName();
 
    	if (name == null || name.equals(""))
    	{
    		// find a formatter name that doesn't yet exist in the hash
    		// name formation patter is <field name>.i where i is a counter
    		int i = 1;
    		Set<String> names = hash.keySet();
    		String prefix = formatter.getFieldName();
    		name = prefix + "." + Integer.toString(i);
    		while (names.contains((String) name))
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
    public void addFormatter(UIFieldFormatterIFace formatter)
    {
    	getFormatterUniqueName(formatter);
    	hash.put(formatter.getName(), formatter);
    }
    
    /**
     * Deletes a formatter from the 
     */
    public void removeFormatter(UIFieldFormatterIFace formatter)
    {
    	hash.remove(formatter.getName());
    }
    
    /**
     * Returns the DOM it is suppose to load the formatters from.
     * @return Returns the DOM it is suppose to load the formatters from.
     */
    protected Element getDOM() throws Exception
    {
        if (doingLocal)
        {
            return XMLHelper.readDOMFromConfigDir("backstop/uiformatters.xml");
        }

        AppResourceIFace escAppRes = AppContextMgr.getInstance().getResourceFromDir("Collection", "UIFormatters");
        if (escAppRes != null)
        {
            return AppContextMgr.getInstance().getResourceAsDOM(escAppRes);
           
        } else
        {
            // Get the default resource by name and copy it to a new User Area Resource
            AppResourceIFace newAppRes = AppContextMgr.getInstance().copyToDirAppRes("Collection", "UIFormatters");
            // Save it in the User Area
            AppContextMgr.getInstance().saveResource(newAppRes);
            return AppContextMgr.getInstance().getResourceAsDOM(newAppRes);
        }
    }
    
    /**
     * Loads the formats from the config file.
     *
     */
    public void load()
    {
        try
        {
            Element root  = getDOM();
            if (root != null)
            {
                List<?> formats = root.selectNodes("/formats/format");
                for (Object fObj : formats)
                {
                    Element formatElement = (Element)fObj;

                    String  name          = formatElement.attributeValue("name");
                    String  fType         = formatElement.attributeValue("type");
                    String  fieldName     = XMLHelper.getAttr(formatElement, "fieldname", "*");
                    String  dataClassName = formatElement.attributeValue("class");
                    int     precision     = XMLHelper.getAttr(formatElement, "precision", 12);
                    int     scale         = XMLHelper.getAttr(formatElement, "scale", 10);
                    boolean isDefault     = XMLHelper.getAttr(formatElement, "default", false);
                    boolean isSystem      = XMLHelper.getAttr(formatElement, "system", false);
                    
                    AutoNumberIFace autoNumberObj     = null;
                    Element         autoNumberElement = (Element)formatElement.selectSingleNode("autonumber");
                    if (autoNumberElement != null)
                    {
                        String autoNumberClassName = autoNumberElement.getTextTrim();
                        if (StringUtils.isNotEmpty(autoNumberClassName) && StringUtils.isNotEmpty(dataClassName) && StringUtils.isNotEmpty(fieldName))
                        {
                            autoNumberObj = createAutoNumber(autoNumberClassName, dataClassName, fieldName);

                        } else
                        {
                            throw new RuntimeException("The class cannot be empty for an external formatter! ["+name+"] or missing field name ["+fieldName+"] or missing data Class name ["+dataClassName+"]");
                        }
                    }
                    
                    Element external = (Element)formatElement.selectSingleNode("external");
                    if (external != null)
                    {
                        String externalClassName = external.getTextTrim();
                        if (StringUtils.isNotEmpty(externalClassName))
                        {
                            try 
                            {
                                UIFieldFormatterIFace formatter = Class.forName(externalClassName).asSubclass(UIFieldFormatterIFace.class).newInstance();
                                formatter.setName(name);
                                formatter.setAutoNumber(autoNumberObj);
                                formatter.setDefault(isDefault);
                                
                                hash.put(name, formatter);
                                
                            } catch (Exception ex)
                            {
                                log.error(ex);
                                ex.printStackTrace();
                            }
                        } else
                        {
                            throw new RuntimeException("The value cannot be empty for an external formatter! ["+name+"]");
                        }
                        
                    } else
                    {
                        List<?>              fieldsList         = formatElement.selectNodes("field");
                        List<UIFieldFormatterField> fields      = new ArrayList<UIFieldFormatterField>();
                        boolean              isInc              = false;
                        String               partialDateTypeStr = formatElement.attributeValue("partialdate");
                        
                        for (Object fldObj : fieldsList)
                        {
                            Element fldElement = (Element)fldObj;

                            int       size    = XMLHelper.getAttr(fldElement, "size", 1);
                            String    value   = fldElement.attributeValue("value");
                            String    typeStr = fldElement.attributeValue("type");
                            boolean   increm  = XMLHelper.getAttr(fldElement, "inc", false);
                            boolean   byYear  = false;
                            
                            UIFieldFormatterField.FieldType type = null;
                            try
                            {
                                type  = UIFieldFormatterField.FieldType.valueOf(typeStr);
                                
                            } catch (Exception ex)
                            {
                                log.error("["+typeStr+"]"+ex.toString());
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
                        UIFieldFormatter.FormatterType type = UIFieldFormatter.FormatterType.generic;
                        UIFieldFormatter.PartialDateEnum partialDateType = UIFieldFormatter.PartialDateEnum.None;
                        if (StringUtils.isNotEmpty(fType) && fType.equals("numeric"))
                        {
                            type = UIFieldFormatter.FormatterType.numeric;
                        } 
                        else if (StringUtils.isNotEmpty(fType) && fType.equals("date"))
                        {
                            type = UIFieldFormatter.FormatterType.date;
                            if (StringUtils.isNotEmpty(partialDateTypeStr))
                            {
                                partialDateType = UIFieldFormatter.PartialDateEnum.valueOf(partialDateTypeStr);
                            }
                            else
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
                                log.error("Couldn't load class ["+dataClassName+"] for ["+name+"]");
                            }
                            
                        } else if (StringUtils.isNotEmpty(fType) && fType.equals("date"))
                        {
                            dataClass = Date.class;
                        }

                        UIFieldFormatter formatter = new UIFieldFormatter(name, isSystem, fieldName, type, partialDateType, dataClass, isDefault, isInc, fields);
                        if (type == UIFieldFormatter.FormatterType.date && fields.size() == 0)
                        {
                            addFieldsForDate(formatter);
                            
                        } else if (type == UIFieldFormatter.FormatterType.numeric)
                        {
                            formatter.setPrecision(precision);
                            formatter.setScale(scale);
                            addFieldsForNumeric(formatter);
                        }

                        formatter.setAutoNumber(autoNumberObj);
                        hash.put(name, formatter);

                    }
                }
            } else
            {
                log.debug("Couldn't open uiformatters.xml");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
    }

    /**
     * Saves formatters
     * @param 
     */
    public void save() 
    {
		StringBuilder sb = new StringBuilder(1024);
    	
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<formats>\n");
    	Iterator<UIFieldFormatterIFace> it = hash.values().iterator();
    	while (it.hasNext()) 
    	{
    		it.next().toXML(sb);
    	}
		sb.append("\n</formats>\n");

        AppResourceIFace escAppRes = AppContextMgr.getInstance().getResourceFromDir("Collection", "UIFormatters");
        if (escAppRes != null)
        {
            escAppRes.setDataAsString(sb.toString());
            AppContextMgr.getInstance().saveResource(escAppRes);
           
        } else
        {
            AppContextMgr.getInstance().putResourceAsXML("UIFormatters", sb.toString());    
        }
    }
    
    /**
     * Factory that creates a new UIFieldFormatter from a formatting string
     * @param formattingString Formatting string that defines the formatter
     * @return The UIFieldFormatter corresponding to the formatting string
     * @throws UIFieldFormattingParsingException (if formatting string is invalid)
     */
    public static UIFieldFormatter factory(final String formattingString, DBFieldInfo fieldInfo) 
    	throws UIFieldFormatterParsingException 
    {
     	Class<?> clazz = fieldInfo.getTableInfo().getClassObj();
    	UIFieldFormatter fmt = new UIFieldFormatter(null, false, fieldInfo.getName(), 
    			FormatterType.generic, PartialDateEnum.None, clazz, false, false, null);
    	
    	AutoNumberIFace autoNumber = createAutoNumber("edu.ku.brc.dbsupport.AutoNumberGeneric", 
    												  clazz.getName(), fieldInfo.getName());
    	fmt.setAutoNumber(autoNumber);
    	
    	// separators and split pattern strings
    	Pattern splitPattern = Pattern.compile("([\\/\\-\\_ ])+");
    	Matcher matcher = splitPattern.matcher(formattingString);

        // Find all the separator matches and create individual fields by calling formatter field factory
    	UIFieldFormatterField field;
    	String fieldString = "";
    	int begin = 0;
    	while (matcher.find()) 
    	{
    		// create a field with what's before the current separator
    		fieldString = formattingString.substring(begin, matcher.start());
    		field = UIFieldFormatterField.factory(fieldString);
    		fmt.addField(field);
    		begin = matcher.end();

    		// create separator field
    		String value = matcher.group();
    		field = new UIFieldFormatterField(FieldType.separator, value.length(), value, false);
    		fmt.addField(field);
       	}
    	
    	// create last bit of formatter
		fieldString = formattingString.substring(begin);
		field = UIFieldFormatterField.factory(fieldString);
		fmt.addField(field);

		// TODO: find out if it is incrementer
		
    	return fmt;
    }

    /**
     * Creates and returns an autonumbering object for the formatter.
     * @param autoNumberClassName the class name to be instantiated
     * @param dataClassName the data class name (which the auto number will operate on
     * @param fieldName the field that will be incremented in the dataClassName object
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
            log.error(ex);
            ex.printStackTrace();
        }
        return autoNumberObj;
    }

    /**
     * Constructs a the fields for a date formatter if the user didn't specify them; it gets the fields
     * for the date from the dat preference
     * @param formatter the formatter to be augmented
     */
    protected void addFieldsForDate(final UIFieldFormatter formatter)
    {
        DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");

        UIFieldFormatter.PartialDateEnum partialType = formatter.getPartialDateType();
        
        StringBuilder newFormatStr = new StringBuilder();
        String        formatStr    = scrDateFormat.getSimpleDateFormat().toPattern();
        boolean       wasConsumed  = false;
        char          currChar     = ' ';
        
        for (int i=0;i<formatStr.length();i++)
        {
            char c = formatStr.charAt(i);
            if (c != currChar)
            {
                if (c == 'M') // make sure we consume them
                {
                    if (partialType == UIFieldFormatter.PartialDateEnum.Full || partialType == UIFieldFormatter.PartialDateEnum.Month)
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
                        f = new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric, 4, "YYYY", false);
                    } else
                    {
                        f = new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric, 2, "YY", false);
                    }
                    formatter.getFields().add(f);
                    currChar = c;
                    i--;
                    
                } else if (!wasConsumed)
                {
                    String s = "";
                    s += c;
                    UIFieldFormatterField f = new UIFieldFormatterField(UIFieldFormatterField.FieldType.separator, 1, s, false);
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
            formatter.setDateWrapper(new DateWrapper(new SimpleDateFormat(newFormatStr.toString())));
        }
    }
    

    /**
     * Constructs a the fields for a numeric formatter.
     * @param formatter the formatter to be augmented
     */
    protected void addFieldsForNumeric(final UIFieldFormatter formatter)
    {
        int len;
        Class<?> cls = formatter.getDataClass();
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
        } else if (cls == BigDecimal.class)
        {
            len = formatter.getPrecision() + formatter.getScale() + 1;
        } else
        {
            throw new RuntimeException("Missing case for numeric class ["+cls.getName()+"]");
        }
        StringBuilder sb = new StringBuilder(len);
        for (int i=0;i<len;i++)
        {
            sb.append(' ');
        }
        formatter.getFields().add(new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric, len, sb.toString(), false));
    }

    
    /*
    public static void test()
    {
        Properties props = new Properties();
        props.put("class", "edu.ku.brc.specify.datamodel.Accession");
        props.put("field", "number");
        UIFieldFormatterIFace  formatter = UIFieldFormatterMgr.getFormatter("AccessionNumber");
        AutoNumberGeneric generic   = new AutoNumberGeneric(props);
        System.out.println("New  Num["+formatter.toPattern()+"]");
        System.out.println("Next Num["+generic.getNextNumber(formatter, formatter.toPattern())+"]");
        
        props = new Properties();
        props.put("class", "edu.ku.brc.specify.datamodel.CollectionObject");
        props.put("field", "catalogNumber");
         formatter = UIFieldFormatterMgr.getFormatter("AccessionNumber");
        CollectionAutoNumber colAtuoNum   = new CollectionAutoNumber(props);
        System.out.println("New  Num["+formatter.toPattern()+"]");
        System.out.println("Next Num["+colAtuoNum.getNextNumber(formatter, formatter.toPattern())+"]");
    }
    */
}
