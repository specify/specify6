/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.ui.forms.persist;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;
import static edu.ku.brc.helpers.XMLHelper.xmlProps;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
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

    protected int      xCoord  = -1;
    protected int      yCoord  = -1;
    protected int      width   = -1;
    protected int      height  = -1;


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
            if (isNotEmpty(str))
            {
                return Integer.parseInt(str);
            }
        }
        return defVal;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getPropertyAsInteger(java.lang.String, Integer)
     */
    public Integer getPropertyAsInteger(final String nameStr, final Integer defVal)
    {
        if (properties != null)
        {
            String str = properties.getProperty(nameStr);
            if (isNotEmpty(str))
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
            if (isNotEmpty(str))
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
        if (properties == null)
        {
            properties = new Properties();
        }
        return properties;
    }
    
    /**
     * @return the xCoord
     */
    public int getXCoord()
    {
        return xCoord;
    }

    /**
     * @param coord the xCoord to set
     */
    public void setXCoord(int coord)
    {
        xCoord = coord;
    }

    /**
     * @return the yCoord
     */
    public int getYCoord()
    {
        return yCoord;
    }

    /**
     * @param coord the yCoord to set
     */
    public void setYCoord(int coord)
    {
        yCoord = coord;
    }

    /**
     * @return the height
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height)
    {
        this.height = height;
    }

    /**
     * @return the width
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width)
    {
        this.width = width;
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
        formCell.type       = type;
        formCell.id         = id;
        formCell.name       = name;
        formCell.isMultiField = isMultiField;
        formCell.ignoreSetGet       = ignoreSetGet;
        formCell.changeListenerOnly = changeListenerOnly;
        formCell.fieldNames         = fieldNames != null ? fieldNames.clone() : null;
        formCell.colspan    = colspan;
        formCell.rowspan    = colspan;
        formCell.properties = properties != null ? (Properties)properties.clone() : null;
        formCell.xCoord     = xCoord;
        formCell.yCoord     = yCoord;
        formCell.width      = width;
        formCell.height     = height;

        return formCell;
    }
    
    protected void toXMLAttrs(@SuppressWarnings("unused") final StringBuilder sb)
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#toXML(java.lang.StringBuffer)
     */
    public void toXML(StringBuilder sb)
    {
        sb.append("          <cell");
        
        xmlAttr(sb, "type", type.toString());
        
        xmlAttr(sb, "id", id);
        xmlAttr(sb, "name", name);
        
        if (changeListenerOnly) xmlAttr(sb, "changesonly", changeListenerOnly);

        this.toXMLAttrs(sb);
        
        if (colspan > 1)
        {
            xmlAttr(sb, "colspan", colspan);
        }
        
        if (rowspan > 1)
        {
            xmlAttr(sb, "rowspan", rowspan);
        }
        
        if (xCoord > -1)
        {
            xmlAttr(sb, "x",       xCoord);
            xmlAttr(sb, "y",       yCoord);
            xmlAttr(sb, "width",   width);
            xmlAttr(sb, "height",  height);
        }
        
        if (properties != null && properties.size() > 0)
        {
            sb.append(" initialize=\"");
            xmlProps(sb, properties);
            sb.append("\"");
        }
        
        sb.append("/>\n");
        
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return (isNotEmpty(name) ? name : id) + " (" + type + ")";
    }
 }
