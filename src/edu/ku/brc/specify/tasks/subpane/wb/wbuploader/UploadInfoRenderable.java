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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.SortedSet;
import java.util.Vector;

import edu.ku.brc.af.core.expresssearch.TableNameRendererIFace;

public class UploadInfoRenderable implements TableNameRendererIFace, Comparable<UploadInfoRenderable>
{
    protected Class<?> tblClass;
    protected String title;
    protected Integer createdCnt;
    protected Integer updatedCnt;
    protected boolean showCnt = false;
    protected boolean includeCreatedCntinTitle = false;
    UploadedRecordInfo dummy = new UploadedRecordInfo(null, null, -1, null, true, null, null); 
    protected Vector<UploadTable> myTables;
    
    protected void refresh()
    {
        updatedCnt = 0;
        createdCnt = 0;
    	for (UploadTable ut : myTables)
        {
            SortedSet<UploadedRecordInfo> ups = ut.getUploadedRecs();
            int updates = ups.headSet(dummy).size();
        	createdCnt += updates;
        	updatedCnt += ups.size() - updates;
        }

    }
    
    public UploadInfoRenderable(final UploadTable ut)
    {
        this.tblClass = ut.getTblClass();
        this.title = ut.getTable().getTableInfo().getTitle();
        this.createdCnt = 0;
        this.updatedCnt = 0;
        myTables = new Vector<UploadTable>();
        myTables.add(ut);
    }
    
    public void addTable(final UploadTable ut)
    {
        myTables.add(ut);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.TableNameRendererIFace#getIconName()
     */
    public String getIconName()
    {
        return tblClass.getSimpleName().toLowerCase();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.TableNameRendererIFace#getTitle()
     */
    public String getTitle()
    {
        if (!showCnt || includeCreatedCntinTitle)
        {
            return title;
        }
        StringBuilder count = new StringBuilder(" (");
        count.append(createdCnt);
        count.append(" ");
        if (createdCnt == 1)
        {
            count.append(getResourceString("WB_OBJECT_CREATED"));
        }
        else
        {
            count.append(getResourceString("WB_OBJECTS_CREATED"));
        }
        count.append(")");
        return title + count.toString();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(UploadInfoRenderable obj)
    {
        return title.compareTo(obj.getTitle());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!obj.getClass().equals(UploadInfoRenderable.class))
        {
            return false;
        }
        return compareTo((UploadInfoRenderable)obj) == 0;
    }
    
    /**
     * @param showCnt the showCreatedCnt to set
     */
    public void setShowCnt(boolean showCnt)
    {
        this.showCnt = showCnt;
    }
    
    public String getTableName()
    {
        return tblClass.getSimpleName();
    }

    /**
     * @return the createdCnt
     */
    public Integer getCreatedCnt()
    {
        return createdCnt;
    }
    
    
    /**
	 * @return the updatedCnt
	 */
	public Integer getUpdatedCnt() 
	{
		return updatedCnt;
	}

	/**
     * sets Cnts to 0 and sets showCnts to false.
     */
    public void reset()
    {
        createdCnt = 0;
        updatedCnt = 0;
        setShowCnt(false);
    }
}
