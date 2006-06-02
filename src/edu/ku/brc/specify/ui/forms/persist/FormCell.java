/* Filename:    $RCSfile: FormCell.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
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
package edu.ku.brc.specify.ui.forms.persist;

import static org.apache.commons.lang.StringUtils.split;

import org.apache.commons.lang.StringUtils;

/**
 * This represents all the information about a cell in the form.
 * @author rods
 *
 */
public class FormCell
{
    public enum CellType {separator, field, label, subview, command, panel};

    // Required fields
    protected CellType type;
    protected String   id;
    protected String   name;
    protected boolean  ignoreSetGet       = false;
    protected boolean  changeListenerOnly = false;
    protected boolean  isMultiField       = false; // Meaning does it have a comma separating mulitple field

    protected String[] fieldNames     = null;

    protected int      colspan = 1;
    protected int      rowspan = 1;


    /**
     *
     */
    public FormCell()
    {

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

    public void setRowspan(int rowspan)
    {
        this.rowspan = rowspan;
    }

    public void setType(CellType type)
    {
        this.type = type;
    }

 }
