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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.DateWrapper;

/**
 * The Format Manager; reads in all the formats from XM
 * @code_status Beta
 *L
 * @author rods
 *
 */
public class UIFieldFormatterMgr
{
    

    private static final Logger log = Logger.getLogger(UIFieldFormatterMgr.class);
    protected static UIFieldFormatterMgr instance = new UIFieldFormatterMgr();

    protected Hashtable<String, UIFieldFormatter> hash = new Hashtable<String, UIFieldFormatter>();


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
        return instance;
    }

    /**
     * Returns a formatter by name
     * @param name the name of the format
     * @return return a formatter if it is there, returns null if it isn't
     */
    public static UIFieldFormatter getFormatter(final String name)
    {
        return instance.hash.get(name);

    }

    /**
     * Returns a formatter by data class. Returns the "default" formatter and if no default
     * is set it returns the first one it finds.
     * @param clazz the class of the data tghat the formatter is used for.
     * @return return a formatter if it is there, returns null if it isn't
     */
    public static UIFieldFormatter getFormatter(final Class clazz)
    {
        UIFieldFormatter formatter = null;
        for (Enumeration<UIFieldFormatter> e=instance.hash.elements();e.hasMoreElements();)
        {
            UIFieldFormatter f = e.nextElement();
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
     * Returns a list of formatters that match the class, the default (if there is one) is at the beginning of the list.
     * @param clazz the class of the data tghat the formatter is used for.
     * @return return a list of formatters that match the class
     */
    public static List<UIFieldFormatter> getFormatterList(final Class clazz)
    {
        Vector<UIFieldFormatter> list = new Vector<UIFieldFormatter>();
        UIFieldFormatter defFormatter = null;
        for (Enumeration<UIFieldFormatter> e=instance.hash.elements();e.hasMoreElements();)
        {
            UIFieldFormatter f = e.nextElement();
            if (clazz == f.getDataClass())
            {
                if (f.isDefault())
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
     * Loads the formats from the config file.
     *
     */
    public void load()
    {
        try
        {
            Element root  = AppContextMgr.getInstance().getResourceAsDOM("UIFormatters");
            if (root != null)
            {
                List<?> formats = root.selectNodes("/formats/format");
                for (Object fObj : formats)
                {
                    Element formatElement = (Element)fObj;

                    String  name          = formatElement.attributeValue("name");
                    String  fType         = formatElement.attributeValue("type");
                    String  dataClassName = formatElement.attributeValue("class");
                    boolean isDefault     = XMLHelper.getAttr(formatElement, "default", true);

                    List<?>              fieldsList = formatElement.selectNodes("field");
                    List<UIFieldFormatterField> fields     = new ArrayList<UIFieldFormatterField>();
                    boolean              isInc      = false;
                    
                    for (Object fldObj : fieldsList)
                    {
                        Element fldElement = (Element)fldObj;

                        int       size    = XMLHelper.getAttr(fldElement, "size", 1);
                        String    value   = fldElement.attributeValue("value");
                        String    typeStr = fldElement.attributeValue("type");
                        boolean   increm  = XMLHelper.getAttr(fldElement, "inc", false);
                        UIFieldFormatterField.FieldType type = null;
                        try
                        {
                            type  = UIFieldFormatterField.FieldType.valueOf(typeStr);
                        } catch (Exception ex)
                        {
                            log.error("["+typeStr+"]"+ex.toString());
                        }
                        fields.add(new UIFieldFormatterField(type, size, value, increm));
                        if (increm)
                        {
                            isInc = true;
                        }
                    }
                    
                    Class dataClass = null;
                    try
                    {
                        dataClass = Class.forName(dataClassName);
                    } catch (Exception ex)
                    {
                        log.error("Couldn't load class ["+dataClassName+"] for ["+name+"]");
                    }

                    boolean   isDate    = StringUtils.isNotEmpty(fType) && fType.equals("date");
                    UIFieldFormatter formatter = new UIFieldFormatter(name, isDate, dataClass, isDefault, isInc, fields);
                    if (isDate && fields.size() == 0)
                    {
                        addFieldsForDate(formatter);
                    }

                    hash.put(name, formatter);

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
     * Constructs a the fields for a date formatter if the user didn't specify them; it gets the fields
     * for the date from the dat preference
     * @param formatter the formatter to be augmented
     */
    protected void addFieldsForDate(UIFieldFormatter formatter)
    {
        DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");

        String formatStr = scrDateFormat.getSimpleDateFormat().toPattern();
        char currChar = ' ';
        for (int i=0;i<formatStr.length();i++)
        {
            char c = formatStr.charAt(i);
            if (c != currChar)
            {
                if (c == 'M' || c == 'd')
                {
                    String s = "";
                    s += c;
                    s += c;
                    UIFieldFormatterField f = new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric, 2, s.toUpperCase(), false);
                    formatter.getFields().add(f);
                    currChar = c;

                } else if (c == 'y')
                {
                    int start = i;
                    while (i < formatStr.length() && formatStr.charAt(i) == 'y')
                    {
                        i++;
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
                } else
                {
                    String s = "";
                    s += c;
                    UIFieldFormatterField f = new UIFieldFormatterField(UIFieldFormatterField.FieldType.separator, 1, s, false);
                    formatter.getFields().add(f);
                }
            }
        }
    }

    //---------------------------------------------------------
    // Inner Classes
    //---------------------------------------------------------


}
