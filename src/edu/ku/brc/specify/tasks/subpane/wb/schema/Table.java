/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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

import java.util.Collection;
import java.util.TreeMap;
import java.util.Vector;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableInfo;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * A wrapper for DBTableIdMgr slightly enhanced for workbench uploads.
 */
public class Table implements Comparable<Table>
{
    /**
     * The name of the table.
     */
    protected String                 name;
    /**
     * The fields in the table.
     */
    protected TreeMap<String, Field> fields;
    /**
     * The relatinoships involving the table.
     */
    protected Vector<Relationship>   relationships;
    /**
     * The underlying TableInfo.
     */
    protected DBTableInfo tableInfo;
    /**
     * The primary key of the Table.
     */
    protected Field                  keyFld = null;
    /**
     * The schema containing this table.
     */
    protected DBSchema               schema;

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object tableObj)
    {
        if (tableObj == null)
            return false;

        if (this.getClass() != tableObj.getClass())
            return false;

        Table table = (Table) tableObj;
        return getName().equalsIgnoreCase(table.getName());
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Table tbl)
    {
        return name.compareToIgnoreCase(tbl.name);
    }

    /**
     * @return the fields
     */
    public final Collection<Field> getFields()
    {
        return fields.values();
    }

    /**
     * @return the name
     */
    public final String getName()
    {
        return name;
    }

    /**
     * @return the relationships
     */
    public final Vector<Relationship> getRelationships()
    {
        return relationships;
    }

    public Table(final DBSchema schema, final String name)
    {
        this.schema = schema;
        this.name = name;
        fields = new TreeMap<String, Field>();
        relationships = new Vector<Relationship>();
        tableInfo = null;
    }

    public Table(final DBSchema schema, final DBTableInfo tableInfo)
    {
        this.schema = schema;
        this.tableInfo = tableInfo;
        this.name = this.tableInfo.getShortClassName();
        fields = new TreeMap<String, Field>();
        for (DBFieldInfo fld : this.tableInfo.getFields())
        {
            addField(new Field(fld));
        }
        keyFld = new Field(this.tableInfo.getIdFieldName(), this.tableInfo.getIdType());
        addField(keyFld);

        relationships = new Vector<Relationship>();
    }

    public Table(final String name, final Table toCopy)
    {
        this.tableInfo = toCopy.tableInfo; // NOT copying tableInfo
        this.name = name;
        this.schema = toCopy.schema;
        fields = new TreeMap<String, Field>();
        for (Field copyFld : toCopy.getFields())
        {
            addField(new Field(copyFld));
        }

        if (toCopy.getKey() != null)
        {
            keyFld = getField(toCopy.getKey().getName());
        }

        relationships = new Vector<Relationship>();
    }

    /**
     * @param field
     * 
     * Adds field to this table.
     */
    public void addField(final Field field)
    {
        fields.put(field.getName().toLowerCase(), field);
        if (field.columnIndex == -1)
        {
            field.setColumnIndex(fields.size() - 1);
        }
        field.setTable(this);
    }

    /**
     * @param fldName
     * @return the field.
     */
    public Field getField(String fldName)
    {
        return fields.get(fldName.toLowerCase());
    }

    /**
     * @return the keyFld.
     */
    public Field getKey()
    {
        return keyFld;
    }

    /**
     * @return the tableInfo
     */
    public final DBTableInfo getTableInfo()
    {
        return tableInfo;
    }

    /**
     * @return the schema
     */
    public final DBSchema getSchema()
    {
        return schema;
    }
}
