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
package edu.ku.brc.af.core.expresssearch;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableInfo;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 23, 2007
 *
 */
public class TableFieldPair implements Comparable<TableFieldPair>
{
    protected DBTableInfo tableinfo;
    protected DBFieldInfo fieldInfo;
    protected boolean     isInUse    = false;
    
    public TableFieldPair(final DBTableInfo tableinfo, final DBFieldInfo fieldInfo)
    {
        super();
        this.tableinfo = tableinfo;
        this.fieldInfo = fieldInfo;
    }

    public boolean isInUse()
    {
        return isInUse;
    }

    public void setInUse(boolean isMapped)
    {
        this.isInUse = isMapped;
    }

    public DBFieldInfo getFieldInfo()
    {
        return fieldInfo;
    }

    public DBTableInfo getTableinfo()
    {
        return tableinfo;
    }
    
    /**
     * Return the title for the Field.
     * @return the title for the Field.
     */
    public String getTitle()
    {
        return fieldInfo.getColumn();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return fieldInfo.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(TableFieldPair obj)
    {
        return fieldInfo.toString().compareTo(obj.fieldInfo.toString());
    }

}
