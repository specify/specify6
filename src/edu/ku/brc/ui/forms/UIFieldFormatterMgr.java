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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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
import edu.ku.brc.dbsupport.DBConnection;
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
     * Returns a formatter by data class. Returns the "default" formatter and if no default
     * is set it returns the first one it finds.
     * @param clazz the class of the data tghat the formatter is used for.
     * @return return a formatter if it is there, returns null if it isn't
     */
    public static Formatter getFormatter(final Class clazz)
    {
        Formatter formatter = null;
        for (Enumeration<Formatter> e=instance.hash.elements();e.hasMoreElements();)
        {
            Formatter f = e.nextElement();
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
    public static List<Formatter> getFormatterList(final Class clazz)
    {
        Vector<Formatter> list = new Vector<Formatter>();
        Formatter defFormatter = null;
        for (Enumeration<Formatter> e=instance.hash.elements();e.hasMoreElements();)
        {
            Formatter f = e.nextElement();
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
                    List<FormatterField> fields     = new ArrayList<FormatterField>();
                    boolean              isInc      = false;
                    
                    for (Object fldObj : fieldsList)
                    {
                        Element fldElement = (Element)fldObj;

                        int       size    = XMLHelper.getAttr(fldElement, "size", 1);
                        String    value   = fldElement.attributeValue("value");
                        String    typeStr = fldElement.attributeValue("type");
                        boolean   increm  = XMLHelper.getAttr(fldElement, "inc", false);
                        FieldType type = null;
                        try
                        {
                            type  = FieldType.valueOf(typeStr);
                        } catch (Exception ex)
                        {
                            log.error("["+typeStr+"]"+ex.toString());
                        }
                        fields.add(new FormatterField(type, size, value, increm));
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
                        log.error("Couldn't load class ["+dataClassName+"]");
                    }

                    boolean   isDate    = StringUtils.isNotEmpty(fType) && fType.equals("date");
                    Formatter formatter = new Formatter(name, isDate, dataClass, isDefault, isInc, fields);
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
                    UIFieldFormatterMgr.FormatterField f = new UIFieldFormatterMgr.FormatterField(UIFieldFormatterMgr.FieldType.numeric, 2, s.toUpperCase(), false);
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
                        f = new UIFieldFormatterMgr.FormatterField(UIFieldFormatterMgr.FieldType.numeric, 4, "YYYY", false);
                    } else
                    {
                        f = new UIFieldFormatterMgr.FormatterField(UIFieldFormatterMgr.FieldType.numeric, 2, "YY", false);
                    }
                    formatter.getFields().add(f);
                    currChar = c;
                    i--;
                } else
                {
                    String s = "";
                    s += c;
                    UIFieldFormatterMgr.FormatterField f = new UIFieldFormatterMgr.FormatterField(UIFieldFormatterMgr.FieldType.separator, 1, s, false);
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
        protected String               name;
        protected Class                dataClass;
        protected boolean              isDate;
        protected boolean              isDefault;
        protected List<FormatterField> fields;
        protected boolean              isIncrementer;

        public Formatter(final String  name, 
                         final boolean isDate, 
                         final Class   dataClass,
                         final boolean isDefault,
                         final boolean isIncrementer,
                         final List<FormatterField> fields)
        {
            this.name      = name;
            this.dataClass = dataClass;
            this.isDate    = isDate;
            this.isDefault = isDefault;
            this.fields    = fields;
            this.isIncrementer = isIncrementer;
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

        public Class getDataClass()
        {
            return dataClass;
        }

        public boolean isDefault()
        {
            return isDefault;
        }

        public boolean isIncrementer()
        {
            return isIncrementer;
        }

        public void setIncrementer(boolean isIncrementer)
        {
            this.isIncrementer = isIncrementer;
        }
        
        /**
         * This is work in progress.
         * @return the next formatted ID
         */
        public String getNextId()
        {
            // For Demo
            try
            {
                Connection conn = DBConnection.getInstance().createConnection();
                Statement  stmt = conn.createStatement();
                // MySQL should use Hibernate
                ResultSet  rs   = stmt.executeQuery("select "+name+" from "+dataClass.getSimpleName()+" order by "+name+" desc limit 0,1");
                if (rs.first())
                {
                    String numStr      = rs.getString(1);
                    int    offsetStart = 1;
                    int    offsetEnd   = numStr.length();
                    for (FormatterField ff : fields)
                    {
                        if (!ff.isIncrementer())
                        {
                            offsetStart += ff.getSize();
                        } else
                        {
                            offsetEnd = offsetStart + ff.getSize();
                            break;
                        }
                    }
                    int num = Integer.parseInt(numStr.substring(offsetStart, offsetEnd));
                    num++;
                    return String.format("2006-%03d", new Object[] {num});
                    
                } else
                {
                    return "2006-001";
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            return null;
        }
    }

    public class FormatterField
    {
        protected FieldType type;
        protected int       size;
        protected String    value;
        protected boolean   incrementer;
        
        public FormatterField(FieldType type, int size, String value, boolean incrementer)
        {
            super();
            // TODO Auto-generated constructor stub
            this.type = type;
            this.size = size;
            this.value = value;
            this.incrementer = incrementer;
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

        public boolean isIncrementer()
        {
            return incrementer;
        }

    }

}
