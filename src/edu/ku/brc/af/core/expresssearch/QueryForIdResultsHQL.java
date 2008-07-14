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
package edu.ku.brc.af.core.expresssearch;

import java.awt.Color;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.db.ERTICaptionInfo;
import edu.ku.brc.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 14, 2007
 *
 */
public class QueryForIdResultsHQL implements QueryForIdResultsIFace
{
    protected SearchTableConfig     searchTableConfig;
    protected Color                 bannerColor;
    protected Vector<Integer>       recIds       = new Vector<Integer>();
    protected String                searchTerm;
    
    protected String                overrideSQL           = null;
    protected List<ERTICaptionInfo> captions              = null; 
    protected boolean               isExpanded            = false;
    protected boolean               shouldInstallServices = true;
    protected RecordSetIFace        recordSet;
    protected boolean               isMultipleSelection   = true;
    protected boolean               isEditable            = false;
    
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public QueryForIdResultsHQL(final SearchTableConfig searchTableConfig,
                                final Color             bannerColor,
                                final String            searchTerm,
                                final List<?>           listOfIds)
    {
        this.searchTableConfig = searchTableConfig;
        this.bannerColor       = bannerColor;
        this.searchTerm        = searchTerm;
        this.recordSet         = null;
        
        for (Integer id : (List<Integer>)listOfIds)
        {
            recIds.add(id);
        }
    }

    /**
     * @param searchTableConfig
     * @param bannerColor
     * @param recordSet
     */
    public QueryForIdResultsHQL(final SearchTableConfig searchTableConfig,
                                final Color             bannerColor,
                                final RecordSetIFace    recordSet)
    {
        this.searchTableConfig = searchTableConfig;
        this.bannerColor       = bannerColor;
        this.searchTerm        = ""; //$NON-NLS-1$
        this.recordSet         = recordSet;

        for (RecordSetItemIFace item : recordSet.getOrderedItems())
        {
            recIds.add(item.getRecordId());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#isHQL()
     */
    //@Override
    public boolean isHQL()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#cleanUp()
     */
    //@Override
    public void cleanUp()
    {
        searchTableConfig = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getBannerColor()
     */
    //@Override
    public Color getBannerColor()
    {
        return bannerColor;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getDisplayOrder()
     */
    //@Override
    public Integer getDisplayOrder()
    {
        return searchTableConfig.getDisplayOrder();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getIconName()
     */
    //@Override
    public String getIconName()
    {
        return searchTableConfig.getIconName();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getRecIds()
     */
    //@Override
    public Vector<Integer> getRecIds()
    {
        return recIds;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getSQL(java.lang.String, java.util.Vector)
     */
    //@Override
    public String getSQL(String searchTermArg, Vector<Integer> ids)
    {
        if (StringUtils.isNotEmpty(overrideSQL))
        {
            return overrideSQL;
        }
        ESTermParser.parse(searchTermArg, false);
        return searchTableConfig.getSQL(ESTermParser.getFields(), false, ids == null && recIds != null ? recIds : ids, true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getSearchTerm()
     */
    //@Override
    public String getSearchTerm()
    {
        return searchTerm;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getTableId()
     */
    //@Override
    public int getTableId()
    {
        return searchTableConfig.getTableInfo().getTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getTitle()
     */
    //@Override
    public String getTitle()
    {
        return searchTableConfig.getTableInfo().getTitle();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getVisibleCaptionInfo()
     */
    //@Override
    public List<ERTICaptionInfo> getVisibleCaptionInfo()
    {
        if (captions == null)
        {
            buildCaptions();
        }
        return captions;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#isExpanded()
     */
    public boolean isExpanded()
    {
        return isExpanded;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#shouldInstallServices()
     */
    public boolean shouldInstallServices()
    {
        return shouldInstallServices;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#enableEditing()
     */
    public boolean isEditingEnabled()
    {
        return isEditable;
    }
    
    /**
     * @param isEditable the isEditable to set
     */
    public void setEditable(boolean isEditable)
    {
        this.isEditable = isEditable;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#removeIds(java.util.List)
     */
    public void removeIds(List<Integer> ids)
    {
        CommandAction cmd = new CommandAction("Record_Set", "DELETEITEMS", new Object[] {recordSet, ids});
        CommandDispatcher.dispatch(cmd);
    }
    
    /**
     * @param shouldInstallServices the shouldInstallServices to set
     */
    public void setShouldInstallServices(boolean shouldInstallServices)
    {
        this.shouldInstallServices = shouldInstallServices;
    }

    /**
     * @param isExpanded the isExpanded to set
     */
    public void setExpanded(boolean isExpanded)
    {
        this.isExpanded = isExpanded;
    }

    /**
     * 
     */
    protected void buildCaptions()
    {
        captions = new Vector<ERTICaptionInfo>();
        int i = 0;
        for (DisplayFieldConfig dfc : searchTableConfig.getDisplayFields())
        {
            ERTICaptionInfo caption = new ERTICaptionInfo(dfc.getFieldInfo().getColumn(), 
                                                          dfc.getFieldInfo().getTitle(), 
                                                          true,
                                                          dfc.getFieldInfo().getFormatter(), 
                                                          i+1);
            caption.setColClass(dfc.getFieldInfo().getDataClass());
                
            captions.add(caption);
            i++;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#setSQL(java.lang.String)
     */
    public void setSQL(String sql)
    {
        overrideSQL = sql;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getDescription()
     */
    public String getDescription()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isMultipleSelection()
     */
    public boolean isMultipleSelection()
    {
        return isMultipleSelection;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setMultipleSelection(boolean)
     */
    public void setMultipleSelection(boolean isMultiple)
    {
        isMultipleSelection = isMultiple;
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
        //nothing to do here.
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#queryTaskDone()
     */
    //@Override
    public void queryTaskDone(final Object results)
    {
        //nuthin
        
    }

    
    
}
