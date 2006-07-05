/* Filename:    $RCSfile: UIFieldFormatterMgr.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/04/06 16:52:27 $
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.prefs.PrefsCache;

/**
 * The Format Manager; reads in all the formats from XML
 * @author rods
 *
 */
public class UIFieldFormatterMgr
{
    public enum FieldType {numeric, alphanumeric, alpha, separator}

    private static final Logger log = Logger.getLogger(UIFieldFormatterMgr.class);
    protected static UIFieldFormatterMgr instance = new UIFieldFormatterMgr();

    protected Hashtable<String, Formatter> hash = new Hashtable<String, Formatter>();


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
    public static Formatter getFormatter(final String name)
    {
        return instance.hash.get(name);

    }

    /**
     * Loads the formats from the config file
     *
     */
    public void load()
    {

        try
        {
            Element root  = XMLHelper.readDOMFromConfigDir("uiformatters.xml");
            if (root != null)
            {
                List formats = root.selectNodes("/formats/format");
                for (Object fObj : formats)
                {
                    Element formatElement = (Element)fObj;

                    String  name   = formatElement.attributeValue("name");
                    String  fType  = formatElement.attributeValue("type");

                    List                 fieldsList = formatElement.selectNodes("field");
                    List<FormatterField> fields     = new ArrayList<FormatterField>();

                    for (Object fldObj : fieldsList)
                    {
                        Element fldElement = (Element)fldObj;

                        int       size  = XMLHelper.getAttr(fldElement, "size", 1);
                        String    value = fldElement.attributeValue("value");
                        String    typeStr = fldElement.attributeValue("type");
                        FieldType type = null;
                        try
                        {
                            type  = FieldType.valueOf(typeStr);
                        } catch (Exception ex)
                        {
                            log.error("["+typeStr+"]"+ex.toString());
                        }
                        fields.add(new FormatterField(type, size, value));
                    }

                    boolean isDate = StringUtils.isNotEmpty(fType) && fType.equals("date");
                    Formatter formatter = new Formatter(name, isDate, fields);
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
    protected void addFieldsForDate(UIFieldFormatterMgr.Formatter formatter)
    {
        SimpleDateFormat scrDateFormat = PrefsCache.getSimpleDateFormat("ui", "formatting", "scrdateformat");

        String formatStr = scrDateFormat.toPattern();
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
                    UIFieldFormatterMgr.FormatterField f = new UIFieldFormatterMgr.FormatterField(UIFieldFormatterMgr.FieldType.numeric, 2, s.toUpperCase());
                    formatter.getFields().add(f);
                    currChar = c;

                } else if (c == 'y')
                {
                    int start = i;
                    while (i < formatStr.length() && formatStr.charAt(i) == 'y')
                    {
                        i++;
                    }
                    UIFieldFormatterMgr.FormatterField f;
                    if (i - start > 2)
                    {
                        f = new UIFieldFormatterMgr.FormatterField(UIFieldFormatterMgr.FieldType.numeric, 4, "YYYY");
                    } else
                    {
                        f = new UIFieldFormatterMgr.FormatterField(UIFieldFormatterMgr.FieldType.numeric, 2, "YY");
                    }
                    formatter.getFields().add(f);
                    currChar = c;
                    i--;
                } else
                {
                    String s = "";
                    s += c;
                    UIFieldFormatterMgr.FormatterField f = new UIFieldFormatterMgr.FormatterField(UIFieldFormatterMgr.FieldType.separator, 1, s);
                    formatter.getFields().add(f);
                }
            }
        }
    }

    //---------------------------------------------------------
    // Inner Classes
    //---------------------------------------------------------

    public class Formatter
    {
        protected String name;
        protected boolean isDate;
        protected List<FormatterField> fields;

        public Formatter(String name, boolean isDate, List<FormatterField> fields)
        {
            this.name   = name;
            this.isDate = isDate;
            this.fields = fields;
        }

        public List<FormatterField> getFields()
        {
            return fields;
        }

        public String getName()
        {
            return name;
        }

        public boolean isDate()
        {
            return isDate;
        }


    }

    public class FormatterField
    {
        protected FieldType type;
        protected int       size;
        protected String    value;

        public FormatterField(FieldType type, int size, String value)
        {
            super();
            // TODO Auto-generated constructor stub
            this.type = type;
            this.size = size;
            this.value = value;
        }

        public int getSize()
        {
            return size;
        }

        public FieldType getType()
        {
            return type;
        }

        public String getValue()
        {
            return value;
        }

    }

}
