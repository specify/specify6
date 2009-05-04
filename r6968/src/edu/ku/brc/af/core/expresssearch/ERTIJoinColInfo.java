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
package edu.ku.brc.af.core.expresssearch;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import org.dom4j.Element;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 10, 2007
 *
 */
public class ERTIJoinColInfo
{
    protected String  joinTableId;
    protected int     joinTableIdAsInt;
    protected String  colName;
    protected boolean isPrimary;
    
    // Transient
    private DBTableInfo tableInfo = null;
    
    /**
     * @param element
     */
    public ERTIJoinColInfo(final Element element)
    {
        joinTableId      = getAttr(element, "tableid", null); //$NON-NLS-1$
        joinTableIdAsInt = getAttr(element, "tableid", -1); //$NON-NLS-1$
        colName          = element.getTextTrim();
        isPrimary        = getAttr(element, "primary", false); //$NON-NLS-1$
    }

    public String getJoinTableId()
    {
        return joinTableId;
    }

    public int getJoinTableIdAsInt()
    {
        return joinTableIdAsInt;
    }

    public String getColName()
    {
        return colName;
    }

    public boolean isPrimary()
    {
        return isPrimary;
    }

    /**
     * @return the tableInfo
     */
    public DBTableInfo getTableInfo()
    {
        if (tableInfo == null)
        {
            tableInfo = DBTableIdMgr.getInstance().getInfoById(joinTableIdAsInt);
        }
        return tableInfo;
    }
    
    
}
