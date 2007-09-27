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
public class FormCell implements Comparable<FormCellIFace>, Cloneable, FormCellIFace
{
    

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

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getColspan()
     */
    public int getColspan()
    {
        return colspan;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getId()
     */
    public String getIdent()
    {
        return id;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getRowspan()
     */
    public int getRowspan()
    {
        return rowspan;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getType()
     */
    public CellType getType()
    {
        return type;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#isIgnoreSetGet()
     */
    public boolean isIgnoreSetGet()
    {
        return ignoreSetGet;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#isChangeListenerOnly()
     */
    public boolean isChangeListenerOnly()
    {
        return changeListenerOnly;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#isMultiField()
     */
    public boolean isMultiField()
    {
        return isMultiField;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getFieldNames()
     */
    public String[] getFieldNames()
    {
        return fieldNames;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setChangeListenerOnly(boolean)
     */
    public void setChangeListenerOnly(boolean changeListenerOnly)
    {
        this.changeListenerOnly = changeListenerOnly;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setColspan(int)
     */
    public void setColspan(int colspan)
    {
        this.colspan = colspan;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setFieldNames(java.lang.String[])
     */
    public void setFieldNames(String[] fieldNames)
    {
        this.fieldNames = fieldNames;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setIgnoreSetGet(boolean)
     */
    public void setIgnoreSetGet(boolean ignoreSetGet)
    {
        this.ignoreSetGet = ignoreSetGet;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setMultiField(boolean)
     */
    public void setMultiField(boolean isMultiField)
    {
        this.isMultiField = isMultiField;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setId(java.lang.String)
     */
    public void setIdent(String id)
    {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setRowspan(int)
     */
    public void setRowspan(int rowspan)
    {
        this.rowspan = rowspan;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setType(edu.ku.brc.ui.forms.persist.FormCell.CellType)
     */
    public void setType(CellType type)
    {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#compareTo(edu.ku.brc.ui.forms.persist.FormCell)
     */
    public int compareTo(FormCellIFace obj)
    {
        if (obj == null || obj.getIdent() == null)
        {
            return 0;
        }
        return id.compareTo(obj.getIdent());
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setProperties(java.util.Properties)
     */
    public void setProperties(final Properties properties)
    {
        this.properties = properties;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#addProperty(java.lang.String, java.lang.String)
     */
    public void addProperty(final String nameStr, final String value)
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        properties.put(nameStr, value);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getProperty(java.lang.String)
     */
    public String getProperty(final String nameStr)
    {
        if (properties != null)
        {
            return properties.getProperty(nameStr);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getPropertyAsInt(java.lang.String, int)
     */
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

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getPropertyAsBoolean(java.lang.String, boolean)
     */
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

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getProperties()
     */
    public Properties getProperties()
    {
        return properties;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#clone()
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
