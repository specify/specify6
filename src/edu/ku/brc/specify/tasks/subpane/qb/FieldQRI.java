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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.HashSet;
import java.util.Set;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
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
    
    protected final static String[] forbiddenFieldTexts = {"attachment.scopetype", "attachment.scopeid", "attachment.tableid"};
    protected final static Set<String> forbiddenFields = new HashSet<String>(forbiddenFieldTexts.length);
    static {
    	for (int f = 0; f < forbiddenFieldTexts.length; f++)
    	{
    		forbiddenFields.add(forbiddenFieldTexts[f]);
    	}
    }
    	
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
            if (forbiddenFields.contains((fi.getTableInfo().getName() + "." + fi.getName()).toLowerCase()))
            {
            	fi.setHidden(true);
            }
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
        if (table != null)
            return table.getTableInfo();
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

    protected boolean addTableNumColumn(final boolean forWhereClause, final boolean forSchemaExport, boolean formatAuditIds) {
        boolean result = false;
        if (formatAuditIds && !forWhereClause) {
            DBFieldInfo fi = getFieldInfo();
            if (fi != null) {
                String fldName = fi.getName();
                result =  fi.getTableInfo().getName().equalsIgnoreCase("SpAuditLog")
                    && (fldName.equalsIgnoreCase("RecordId") || fldName.equalsIgnoreCase("ParentRecordId"));
            }
        }
        return result;
    }

    protected boolean addAuditValColumns(final boolean forWhereClause, final boolean forSchemaExport, boolean formatAuditIds) {
        boolean result = false;
        if (formatAuditIds && !forWhereClause) {
            DBFieldInfo fi = getFieldInfo();
            if (fi != null) {
                String fldName = fi.getName();
                result = fi.getTableInfo().getName().equalsIgnoreCase("SpAuditLogField")
                        && (fldName.equalsIgnoreCase("NewValue") || fldName.equalsIgnoreCase("OldValue"));
            }
        }
        return result;
    }

    protected boolean addAuditFldNameColumns(final boolean forWhereClause, final boolean forSchemaExport, boolean formatAuditIds) {
        boolean result = false;
        if (formatAuditIds && !forWhereClause) {
            DBFieldInfo fi = getFieldInfo();
            if (fi != null) {
                String fldName = fi.getName();
                result = fi.getTableInfo().getName().equalsIgnoreCase("SpAuditLogField")
                        && fldName.equalsIgnoreCase("FieldName");
            }
        }
        return result;
    }

    /**
     * @param ta
     * @param forWhereClause
     * @param forSchemaExport
     * @return sql/hql specification for this field.
     */
    public String getSQLFldSpec(final TableAbbreviator ta, final boolean forWhereClause, 
    		final boolean forSchemaExport, final String formatName, boolean formatAuditIds) {
        String result = ta.getAbbreviation(table.getTableTree()) + "." + getFieldName();
        if (addPartialDateColumn(forWhereClause, forSchemaExport)) {
            String precName = getFieldInfo().getDatePrecisionName();
            result += ", " + ta.getAbbreviation(table.getTableTree()) + "." + precName;
        } else if (addTableNumColumn(forWhereClause, forSchemaExport, formatAuditIds)) {
            String fldName = getFieldInfo().getName();
            String tblNumFld = fldName.equalsIgnoreCase("RecordId") ? "tableNum" : "parentTableNum";
            result += ", " + ta.getAbbreviation(table.getTableTree()) + "." + tblNumFld;
        } else if (addAuditValColumns(forWhereClause, forSchemaExport, formatAuditIds)) {
           result += ", " + ta.getAbbreviation(table.getTableTree().getParent()) + ".tableNum"
                   + ", " + ta.getAbbreviation(table.getTableTree()) + ".fieldName";
        } else if (this instanceof RelQRI) {

        } else if (getDataClass().equals(java.sql.Timestamp.class) && forWhereClause) {
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
        return getSQLFldSpec(ta, true, forSchemaExport, formatName, false) + (negate ? " is not " : " is ") + "null";
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
