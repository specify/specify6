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
package edu.ku.brc.ui.forms.persist;

import static org.apache.commons.lang.StringUtils.split;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * This represents all the information about a cell in the form
 * @code_status Beta
 *.
 * @author rods
 *
 */
public class FormCell implements Comparable<FormCell>, Cloneable
{
    public enum CellType {separator, field, label, statictext, subview, command, panel, statusbar, iconview}

    // Required fields
    protected CellType type;
    protected String   id;
    protected String   name;
    protected boolean  ignoreSetGet       = false;
    protected boolean  changeListenerOnly = false;
    protected boolean  isMultiField       = false; // Meaning does it have a comma separating multiple field

    protected String[] fieldNames     = null;

    protected int      colspan = 1;
    protected int      rowspan = 1;


    protected Properties properties = null;

    /**
     *
     */
    public FormCell()
    {
        // do nothing
    }

    /**
     * Constructor
     * @param type type of cell
     * @param id the unique id
     * @param name the name
     */
    public FormCell(final CellType type, final String id, final String name)
    {
        this.type = type;
        this.id   = id;
        this.name = name;
        this.isMultiField = name.indexOf(',') > -1;
        //if (isMultiField)
        //{
        if (StringUtils.isNotBlank(name))
        {
            fieldNames = split(StringUtils.deleteWhitespace(name), ",");
        }
        //} else
       // {
        //    fieldNames = new String[1];
        //    fieldNames[0] = name;
        //}

    }

    /**
     * Constructor
     * @param type type of cell
     * @param id the unique id
     * @param name the name
     * @param colspan the number of columns to span
     * @param rowspan the number of rows to span
     */
    public FormCell(final CellType type,
                    final String   id,
                    final String   name,
                    final int      colspan,
                    final int      rowspan)
    {
        this(type, id, name);

        this.colspan = colspan;
        this.rowspan = rowspan;
    }

    public int getColspan()
    {
        return colspan;
    }

    public String getName()
    {
        return name;
    }

    public String getId()
    {
        return id;
    }

    public int getRowspan()
    {
        return rowspan;
    }

    public CellType getType()
    {
        return type;
    }

    public boolean isIgnoreSetGet()
    {
        return ignoreSetGet;
    }

    public boolean isChangeListenerOnly()
    {
        return changeListenerOnly;
    }

    public boolean isMultiField()
    {
        return isMultiField;
    }

    public String[] getFieldNames()
    {
        return fieldNames;
    }

    public void setChangeListenerOnly(boolean changeListenerOnly)
    {
        this.changeListenerOnly = changeListenerOnly;
    }

    public void setColspan(int colspan)
    {
        this.colspan = colspan;
    }

    public void setFieldNames(String[] fieldNames)
    {
        this.fieldNames = fieldNames;
    }

    public void setIgnoreSetGet(boolean ignoreSetGet)
    {
        this.ignoreSetGet = ignoreSetGet;
    }

    public void setMultiField(boolean isMultiField)
    {
        this.isMultiField = isMultiField;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setRowspan(int rowspan)
    {
        this.rowspan = rowspan;
    }

    public void setType(CellType type)
    {
        this.type = type;
    }

    public int compareTo(FormCell obj)
    {
        if (obj == null || obj.id == null)
        {
            return 0;
        }
        return id.compareTo(obj.id);
    }
    
    public void setProperties(final Properties properties)
    {
        this.properties = properties;
    }

    public void addProperty(final String nameStr, final String value)
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        properties.put(nameStr, value);
    }

    public String getProperty(final String nameStr)
    {
        if (properties != null)
        {
            return properties.getProperty(nameStr);
        }
        return null;
    }

    public int getPropertyAsInt(final String nameStr, final int defVal)
    {
        if (properties != null)
        {
            String str = properties.getProperty(nameStr);
            if (StringUtils.isNotEmpty(str))
            {
                return Integer.parseInt(str);
            }
        }
        return defVal;
    }

    public boolean getPropertyAsBoolean(final String nameStr, final boolean defVal)
    {
        if (properties != null)
        {
            String str = properties.getProperty(nameStr);
            if (StringUtils.isNotEmpty(str))
            {
                return str.equalsIgnoreCase("true");
            }
        }
        return defVal;
    }

    public Properties getProperties()
    {
        return properties;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        FormCell formCell = (FormCell)super.clone();
        formCell.type = type;
        formCell.id   = id;
        formCell.name = name;
        formCell.isMultiField = isMultiField;
        formCell.ignoreSetGet       = ignoreSetGet;
        formCell.changeListenerOnly = changeListenerOnly;
        formCell.fieldNames         = fieldNames != null ? fieldNames.clone() : null;
        formCell.colspan = colspan;
        formCell.rowspan = colspan;
        formCell.properties = properties != null ? (Properties)properties.clone() : null;

        return formCell;

    }

 }
