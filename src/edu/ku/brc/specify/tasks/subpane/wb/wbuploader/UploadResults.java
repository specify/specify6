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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class UploadResults implements QueryForIdResultsIFace
{
    protected final UploadTable uploadTable;
    protected final UploadData uploadData;
    protected Vector<Integer> recIds = null;
    protected final List<ERTICaptionInfo> captions;
    protected final Color bannerColor = new Color(144, 30, 255); //rgb copied from QBQueryForIdResultsHQL
    
    /**
     * @param uploadTable
     * @param uploadData
     */
    public UploadResults(final UploadTable uploadTable, final UploadData uploadData)
    {
        this.uploadTable = uploadTable;
        this.uploadData = uploadData;
        this.captions = buildCaptions();
    }
    
    /**
     * @return List of columnInfo in order fields are present in wb dataset.
     */
    protected List<ERTICaptionInfo> buildCaptions()
    {
        //use uploadTable fields to make caption infos
        List<ERTICaptionInfo> result = new ArrayList<ERTICaptionInfo>();
        for (Vector<UploadField> flds : uploadTable.getUploadFields())
        {
            List<UploadField> orderedFields = new ArrayList<UploadField>();
            for (UploadField fld : flds)
            {
                if (fld.getIndex() != -1 && !(fld.getSequenceInt() > 0))
                {
                    orderedFields.add(fld);
                }
            }
            UploadRetriever.columnOrder(orderedFields);
            for (UploadField fld : orderedFields)
            {
                String title = fld.getField().getFieldInfo().getTitle();
                String fldName = uploadTable.getTable().getTableInfo().getName() + "."
                        + fld.getField().getFieldInfo().getName();
                result.add(new ERTICaptionInfo(fldName, title, true, null, 0));
            }
        }
        return result;        
    }
    
    /**
     * @return the total number records uploaded for uploadTable.
     */
    public int getUploadedRecCount()
    {
        return uploadTable.getUploadedRecs().size();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#cleanUp()
     */
    //@Override
    public void cleanUp()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#complete()
     */
    //@Override
    public void complete()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#enableEditing()
     */
    //@Override
    public boolean isEditingEnabled()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getBannerColor()
     */
    //@Override
    public Color getBannerColor()
    {
        return bannerColor;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getDescription()
     */
    //@Override
    public String getDescription()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getDisplayOrder()
     */
    //@Override
    public Integer getDisplayOrder()
    {
        return 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getIconName()
     */
    //@Override
    public String getIconName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getParams()
     */
    //@Override
    public List<Pair<String, Object>> getParams()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getQueryTask()
     */
    //@Override
    public Future<?> getQueryTask()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getRecIds()
     */
    //@Override
    public Vector<Integer> getRecIds()
    {
        if (recIds == null)
        {
            buildRecIds();
        }
        return recIds;
    }

    /**
     * Build recIds structure.
     */
    protected void buildRecIds()
    {
        recIds = new Vector<Integer>(uploadTable.getUploadedRecs().size());
        for (UploadedRecordInfo rec : uploadTable.getUploadedRecs())
        {
            recIds.add(rec.getKey());
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getSQL(java.lang.String, java.util.Vector)
     */
    //@Override
    public String getSQL(String searchTerm, Vector<Integer> ids)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getSearchTerm()
     */
    //@Override
    public String getSearchTerm()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getTableId()
     */
    //@Override
    public int getTableId()
    {
        return uploadTable.getTable().getTableInfo().getTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getTitle()
     */
    //@Override
    public String getTitle()
    {
        return uploadTable.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getVisibleCaptionInfo()
     */
    //@Override
    public List<ERTICaptionInfo> getVisibleCaptionInfo()
    {
        return captions;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isExpanded()
     */
    //@Override
    public boolean isExpanded()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isHQL()
     */
    //@Override
    public boolean isHQL()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isMultipleSelection()
     */
    //@Override
    public boolean isMultipleSelection()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#queryTaskDone(java.lang.Object)
     */
    //@Override
    public void queryTaskDone(Object results)
    {
        //do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#removeIds(java.util.List)
     */
    //@Override
    public void removeIds(List<Integer> ids)
    {
        //No. Don't do nothing.
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setMultipleSelection(boolean)
     */
    //@Override
    public void setMultipleSelection(boolean isMultiple)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setQueryTask(java.util.concurrent.Future)
     */
    //@Override
    public void setQueryTask(Future<?> queryTask)
    {
        //don't care.
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setSQL(java.lang.String)
     */
    //@Override
    public void setSQL(String sql)
    {
        // irrelevant
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#shouldInstallServices()
     */
    //@Override
    public boolean shouldInstallServices()
    {
        return true;
    }

    /**
     * @return the uploadTable
     */
    public UploadTable getUploadTable()
    {
        return uploadTable;
    }

    /**
     * @return the uploadData
     */
    public UploadData getUploadData()
    {
        return uploadData;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.QueryForIdResultsIFace#size()
     */
    @Override
    public int size()
    {
        return getRecIds().size();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#showProgress()
     */
    //@Override
    public boolean showProgress()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.QueryForIdResultsIFace#getMaxTableRows()
     */
    @Override
    public int getMaxTableRows()
    {
        return -1;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.QueryForIdResultsIFace#cacheFilled(java.util.Vector)
     */
    @Override
    public void cacheFilled(Vector<Vector<Object>> cache)
    {
        // TODO Auto-generated method stub
        
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.db.QueryForIdResultsIFace#isCount()
	 */
	@Override
	public boolean isCount() 
	{
		// Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.db.QueryForIdResultsIFace#setCount(boolean)
	 */
	@Override
	public void setCount(boolean value) 
	{
		// ignore
		
	}
    
    
}
