/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.images;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Mar 12, 2013
 *
 */
public class BubbleDisplayInfo implements Comparable<BubbleDisplayInfo>
{
    private static int idCnt = 0;
    
    protected Integer     id;
    protected int         colTblId;
    protected DBTableInfo tableInfo;
    protected DBFieldInfo fieldInfo;
    
    /**
     * @param ti
     * @param fi
     */
    public BubbleDisplayInfo(final DBTableInfo ti, 
                             final DBFieldInfo fi)
    {
        this.id        = idCnt++;
        this.tableInfo = ti;
        this.fieldInfo = fi;
        this.colTblId  = ti.getTableId();
    }

    /**
     * @return the colTblId
     */
    public int getColTblId()
    {
        return colTblId;
    }
    /**
     * @param colTblId the colTblId to set
     */
    public void setColTblId(int colTblId)
    {
        this.colTblId = colTblId;
    }
    
    /**
     * @return the columnName
     */
    public String getColumnName()
    {
        return fieldInfo.getColumn();
    }
    
    /**
     * @return the columnName
     */
    public String getTitle()
    {
        return fieldInfo.getTitle();
    }
    
    /**
     * @return the tableInfo
     */
    public DBTableInfo getTableInfo()
    {
        return tableInfo;
    }

    /**
     * @return the fieldInfo
     */
    public DBFieldInfo getFieldInfo()
    {
        return fieldInfo;
    }
    
    /**
     * @return the formatter
     */
    public UIFieldFormatterIFace getFormatter()
    {
        return fieldInfo.getFormatter();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return id.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(BubbleDisplayInfo o)
    {
        return id.compareTo(o.id);
    }
}
