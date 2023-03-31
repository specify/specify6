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
package edu.ku.brc.specify.tasks.subpane.wb.schema;

import edu.ku.brc.af.core.db.DBFieldInfo;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * This class basically is a wrapper for DBTableIdMgr with a few enhancments to simplify workbench uploading.
 */
public class Field implements Comparable<Field>
{
    /**
     * The name of the field.
     */
    protected String                 name;
    /**
     * String specification of the field's data type.
     */
    protected String                 type;
    /**
     * A pointer to the table containing this field.
     */
    protected Table                  table;
    /**
     * The underlying FieldInfo.
     */
    protected DBFieldInfo fieldInfo;
    /**
     * The column index of the field in Table.
     * (Not currently necessary?)
     */
    protected int                    columnIndex = -1;
    
    /**
     * True if the field must be non-null. (Necessary for foreign keys when DBFieldInfo is null).
     */
    protected boolean required = false;

    /**
     * True if field represents a foreign key.
     */
    protected boolean foreignKey = false;
    
    /**
     * @return the columnIndex
     */
    public final int getColumnIndex()
    {
        return columnIndex;
    }

    /**
     * @param columnIndex the columnIndex to set
     */
    public final void setColumnIndex(int columnIndex)
    {
        this.columnIndex = columnIndex;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Field fld)
    {
        int tc = table.compareTo(fld.table);
        if (tc != 0) { return tc; }
        return name.compareToIgnoreCase(fld.name);
    }

    
    /* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) 
	{
		if (!(obj instanceof Field))
		{
			return false;
		}
		return compareTo((Field )obj) == 0;
	}

	/**
     * @return the name
     */
    public final String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public final void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the type
     */
    public final String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public final void setType(String type)
    {
        this.type = type;
    }

    /**
     * @param name
     * @param type
     * @param required
     * @param foreignKey
     */
    public Field(String name, String type, boolean required, boolean foreignKey)
    {
        super();
        this.name = name;
        this.type = type;
        this.required = required;
        this.foreignKey = foreignKey;
        this.fieldInfo = null;
    }

    /**
     * @param name
     * @param type
     */
    public Field(String name, String type)
    {
        super();
        this.name = name;
        this.type = type;
        this.fieldInfo = null;
    }

    public Field(String name, String type, boolean foreignKey)
    {
        super();
        this.name = name;
        this.type = type;
        this.foreignKey = foreignKey;
        this.fieldInfo = null;
    }

    
    /**
     * @param fieldInfo
     */
    public Field(final DBFieldInfo fieldInfo)
    {
        super();
        this.fieldInfo = fieldInfo;
        this.name = fieldInfo.getName();
        this.type = fieldInfo.getType();
    }

    /**
     * @param field
     */
    public Field(final Field field)
    {
        super();
        this.fieldInfo = field.fieldInfo; // NOT copying fieldinfo
        this.name = field.getName();
        this.type = field.getType();
    }

    /**
     * @return the table
     */
    public final Table getTable()
    {
        return table;
    }

    /**
     * @param table the table to set
     */
    public final void setTable(Table table)
    {
        this.table = table;
    }

    /**
     * @return the fieldInfo
     */
    public final DBFieldInfo getFieldInfo()
    {
        return fieldInfo;
    }

    /**
     * @return the required
     */
    public boolean isRequired()
    {
        return required;
    }

    /**
     * @return the foreignKey
     */
    public boolean isForeignKey()
    {
        return foreignKey;
    }
}
