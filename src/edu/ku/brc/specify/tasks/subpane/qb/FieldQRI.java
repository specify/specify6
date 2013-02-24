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
package edu.ku.brc.specify.tasks.subpane.qb;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class FieldQRI extends BaseQRI
{
    protected TableQRI    table = null;
    protected DBFieldInfo fi;
    
    /**
     * @param table
     * @param fi
     */
    public FieldQRI(final TableQRI table, final DBFieldInfo fi)
    {
        super(null);
        
        this.table = table;
        this.fi    = fi;
        
        if (fi != null)
        {
            title    = fi.getTitle();
            //buildValues();
        }
        iconName = "BlankIcon";
    }
        
    /**
     * @return the fieldInfo.
     */
    public DBFieldInfo getFieldInfo()
    {
        return fi;
    }
    
    /**
     * @return the name of the field.
     */
    public String getFieldName()
    {
        return fi.getName();
    }
    
    /**
     * @return the formatter for the field.
     */
    public UIFieldFormatterIFace getFormatter()
    {
        if (fi != null)
            return fi.getFormatter();
        return null;
    }
    
    /**
     * @return the tableInfo.
     */
    public DBTableInfo getTableInfo()
    {
        if (fi != null)
            return fi.getTableInfo();
        return null;
    }
    
    /**
     * @param forWhereClause
     * @param forSchemaExport
     * @return
     */
    protected boolean addPartialDateColumn(final boolean forWhereClause, 
    		final boolean forSchemaExport)
    {
    	return !forWhereClause && getFieldInfo() != null && getFieldInfo().isPartialDate();
    }
    
    /**
     * @param ta
     * @param forWhereClause
     * @param forSchemaExport
     * @return sql/hql specification for this field.
     */
    public String getSQLFldSpec(final TableAbbreviator ta, final boolean forWhereClause, 
    		final boolean forSchemaExport, final String formatName)
    {
        String result = ta.getAbbreviation(table.getTableTree()) + "." + getFieldName();
        if (addPartialDateColumn(forWhereClause, forSchemaExport))
        {
            String precName = getFieldInfo().getDatePrecisionName();
            result += ", " + ta.getAbbreviation(table.getTableTree()) + "." + precName;
        }
        else if (getDataClass().equals(java.sql.Timestamp.class))
        {
        	//XXX Portability: MySql Specific??
        	//necessary because timeStamp criteria can't currently be entered to nano-precision. 
        	result = "DATE(" + result + ")";
        }
        return result;
    }
        
    /**
     * @param ta
     * @param forSchemaExport
     * @param negate
     * @return the null or not null condition for the field
     */
    public String getNullCondition(final TableAbbreviator ta, final boolean forSchemaExport, final boolean negate, final String formatName)
    {
        return getSQLFldSpec(ta, true, forSchemaExport, formatName) + (negate ? " is not " : " is ") + "null";
    }
    
    /**
     * @return the data type for the field
     */
    public Class<?> getDataClass()
    {
        if (fi != null)
            return fi.getDataClass();
        return String.class;
    }

    /**
     * @param table the table to set
     */
    public void setTable(TableQRI table)
    {
        this.table = table;
    }

    /**
     * @return the table
     */
    public TableQRI getTable()
    {
        return table;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.BaseQRI#getTableTree()
     */
    @Override
    public TableTree getTableTree()
    {
        if (table != null)
        {
            return table.getTableTree();
        }
        return null;
    }
    
    
    /**
     * @return true if the schema field represented by this object has been hidden.
     */
    public boolean isFieldHidden()
    {
        return getFieldInfo().isHidden();
    }
    
    /**
     * @return name for use in getStringId()
     */
    protected String getFieldNameForStringId()
    {
    	return getFieldName();
    }
    
    /**
     * @return a string identifier unique to this field within the query that is independent of the field's title.
     */
    public String getStringId()
    {
        return getTableTree().getPathFromRootAsString() + "." + getTableInfo().getName() + "." + getFieldNameForStringId();
    }
}
