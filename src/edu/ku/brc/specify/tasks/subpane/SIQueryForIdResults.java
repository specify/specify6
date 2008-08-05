/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane;

import java.awt.Color;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

import edu.ku.brc.af.core.expresssearch.SearchTableConfig;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.ERTICaptionInfo;
import edu.ku.brc.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Apr 29, 2008
 *
 */
public class SIQueryForIdResults implements QueryForIdResultsIFace
{
    protected Vector<Pair<SearchTableConfig, String>> reasonList = new Vector<Pair<SearchTableConfig, String>>();
    protected Vector<Integer> recIds = new Vector<Integer>();
    
    /**
     * 
     */
    public SIQueryForIdResults()
    {
        recIds.add(0);
    }

    /**
     * @param stc
     * @param reason
     */
    public void addReason(SearchTableConfig stc, String reason)
    {
        reasonList.add(new Pair<SearchTableConfig, String>(stc, reason));
    }
    
    /**
     * @return the reasonList
     */
    public Vector<Pair<SearchTableConfig, String>> getReasonList()
    {
        return reasonList;
    }

    //----------------------------------------------------------------------------
    //-- QueryForIdResultsIFace
    //----------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#cleanUp()
     */
    public void cleanUp()
    {
        reasonList.clear();
        reasonList = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#enableEditing()
     */
    //@Override
    public boolean isEditingEnabled()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getBannerColor()
     */
    //@Override
    public Color getBannerColor()
    {
        return new Color(255, 0, 0);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getDescription()
     */
    //@Override
    public String getDescription()
    {
        return UIRegistry.getResourceString("SIQueryForIdResults.DESC");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getDisplayOrder()
     */
    //@Override
    public Integer getDisplayOrder()
    {
        return Integer.MAX_VALUE;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getIconName()
     */
    //@Override
    public String getIconName()
    {
        return "Search";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getParams()
     */
    //@Override
    public List<Pair<String, Object>> getParams()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getRecIds()
     */
    //@Override
    public Vector<Integer> getRecIds()
    {
        return recIds;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getSearchTerm()
     */
    //@Override
    public String getSearchTerm()
    {
        return "Warnings";
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
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getTableId()
     */
    //@Override
    public int getTableId()
    {
        return 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getTitle()
     */
    //@Override
    public String getTitle()
    {
        return UIRegistry.getResourceString("SIQueryForIdResults.SEARCH_WARNING");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getVisibleCaptionInfo()
     */
    //@Override
    public List<ERTICaptionInfo> getVisibleCaptionInfo()
    {
        List<ERTICaptionInfo> list = new Vector<ERTICaptionInfo>();
        list.add(new ERTICaptionInfo("Table", UIRegistry.getResourceString("SIQueryForIdResults.TABLE"), true, null, 0));
        list.add(new ERTICaptionInfo("Reason", UIRegistry.getResourceString("SIQueryForIdResults.REASON"), true, null, 1));
        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isExpanded()
     */
    //@Override
    public boolean isExpanded()
    {
        return true;
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
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#removeIds(java.util.List)
     */
    //@Override
    public void removeIds(List<Integer> ids)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setMultipleSelection(boolean)
     */
    //@Override
    public void setMultipleSelection(boolean isMultiple)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setSQL(java.lang.String)
     */
    //@Override
    public void setSQL(String sql)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#shouldInstallServices()
     */
    //@Override
    public boolean shouldInstallServices()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#complete()
     */
    //@Override
    public void complete()
    {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getQueryTask()
     */
    //@Override
    public Future<?> getQueryTask()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setQueryTask(java.util.concurrent.Future)
     */
    //@Override
    public void setQueryTask(Future<?> queryTask)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#queryTaskDone()
     */
    //@Override
    public void queryTaskDone(final Object results)
    {
        //nuthin
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setRecIds(java.util.Vector)
     */
    //@Override
    public void setRecIds(final Vector<Integer> ids)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#showProgress()
     */
    //@Override
    public boolean showProgress()
    {
        return true;
    }
    
    
}