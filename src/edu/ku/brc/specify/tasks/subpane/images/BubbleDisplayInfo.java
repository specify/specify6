/* Copyright (C) 2012, University of Kansas Center for Research
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

import edu.ku.brc.af.core.db.DBTableIdMgr;
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
public class BubbleDisplayInfo
{
    protected int    colTblId;
    protected String columnName;
    protected String label;
    protected UIFieldFormatterIFace formatter;
    protected DBTableInfo tableInfo;
    
    
    /**
     * @param colTblId
     * @param columnName
     * @param label
     * @param formatter
     */
    public BubbleDisplayInfo(int    colTblId, 
                            String columnName, 
                            String label,
                            UIFieldFormatterIFace formatter,
                            DBTableInfo tableInfo)
    {
        super();
        this.colTblId  = colTblId;
        this.columnName = columnName;
        this.label     = label;
        this.formatter = formatter;
        this.tableInfo = tableInfo;
    }
    
    /**
     * @param colTblId
     * @param columnName
     * @param label
     */
    public BubbleDisplayInfo(int colTblId, String columnName, String label)
    {
        this(colTblId, columnName, label, null, null);
        this.tableInfo = DBTableIdMgr.getInstance().getInfoById(colTblId);
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
        return columnName;
    }
    /**
     * @param columnName the columnName to set
     */
    public void setColumnName(String columnName)
    {
        this.columnName = columnName;
    }
    /**
     * @return the label
     */
    public String getLabel()
    {
        return label;
    }
    /**
     * @param label the label to set
     */
    public void setLabel(String label)
    {
        this.label = label;
    }
    /**
     * @return the formatter
     */
    public UIFieldFormatterIFace getFormatter()
    {
        return formatter;
    }
    /**
     * @param formatter the formatter to set
     */
    public void setFormatter(UIFieldFormatterIFace formatter)
    {
        this.formatter = formatter;
    }

    /**
     * @return the tableInfo
     */
    public DBTableInfo getTableInfo()
    {
        return tableInfo;
    }
}
