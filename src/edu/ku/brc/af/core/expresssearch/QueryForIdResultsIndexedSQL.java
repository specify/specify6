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

package edu.ku.brc.af.core.expresssearch;

import java.awt.Color;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.ui.ESTermParser;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.util.Pair;


/**
 * This class is used for collecting all the record primary keys for a given Express Definition.
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
public class QueryForIdResultsIndexedSQL implements QueryForIdResultsIFace
{
    protected SearchTableConfig       searchTableConfig = null;
    protected Color                   bannerColor;

    protected String                  searchId;
    protected Vector<Integer>         recIds       = new Vector<Integer>();
    protected String                  searchTerm   = null;
    protected Integer                 displayOrder = null;
    protected List<ERTICaptionInfo>   captions     = null; 
    
    protected String                  overrideSQL  = null;
    protected boolean                 isExpanded   = false;
    
    protected Hashtable<Integer, Boolean> duplicateRecId = new Hashtable<Integer, Boolean>();
    protected boolean               isMultipleSelection  = true;

    /**
     * @param searchTableConfig
     * @param bannerColor
     * @param searchTerm
     * @param listOfIds
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public QueryForIdResultsIndexedSQL(final SearchTableConfig searchTableConfig,
                                       final Color             bannerColor,
                                       final String            searchTerm,
                                       final List<?>           listOfIds)
    {
        this.searchTableConfig = searchTableConfig;
        this.bannerColor       = bannerColor;
        this.searchTerm        = searchTerm;

        for (Integer id : (List<Integer>)listOfIds)
        {
            recIds.add(id);
        }
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#isHQL()
     */
    @Override
    public boolean isHQL()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getTitle()
     */
    @Override
    public String getTitle()
    {
        return searchTableConfig.getTableInfo().getTitle();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.QueryForIdResultsIFace#getDisplayOrder()
     */
    @Override
    public Integer getDisplayOrder()
    {
        return searchTableConfig.getDisplayOrder();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        searchTableConfig = null;
        
        if (recIds != null)
        {
            recIds.clear();
            recIds = null;
        }
        
        if (duplicateRecId != null)
        {
            duplicateRecId.clear();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.QueryForIdResultsIFace#getSearchTerm()
     */
    @Override
    public String getSearchTerm()
    {
        return searchTerm;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.ExpressSearchResultsIFace#getRecIds()
     */
    @Override
    public Vector<Integer> getRecIds()
    {
        return recIds;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.ExpressSearchResultsIFace#getTableId()
     */
    @Override
    public int getTableId()
    {
        return searchTableConfig.getTableInfo().getTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.ExpressSearchResultsIFace#getIconName()
     */
    @Override
    public String getIconName()
    {
        return searchTableConfig.getTableInfo().getName();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getVisibleCaptionInfo()
     */
    @Override
    public List<ERTICaptionInfo> getVisibleCaptionInfo()
    {
        if (captions == null)
        {
            buildCaptions();
        }
        return captions;
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
     * @see edu.ku.brc.af.core.ExpressSearchResultsIFace#getBannerColor()
     */
    @Override
    public Color getBannerColor()
    {
        return bannerColor;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.ExpressSearchResultsIFace#getSQL(java.lang.String, java.util.Vector)
     */
    @Override
    public String getSQL(final String searchTermArg, 
                         final Vector<Integer> ids)
    {
        if (StringUtils.isNotEmpty(overrideSQL))
        {
            return overrideSQL;
        }
        
        ESTermParser.getInstance().parse(searchTermArg, false);
        String sql = searchTableConfig.getSQL(ESTermParser.getInstance().getFields(), false, ids == null && recIds != null ? recIds : ids, false);
        //System.err.println(sql);
        return sql;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#setSQL(java.lang.String)
     */
    @Override
    public void setSQL(final String sql)
    {
        overrideSQL = sql;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isExpanded()
     */
    @Override
    public boolean isExpanded()
    {
        return isExpanded;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#shouldInstallServices()
     */
    @Override
    public boolean shouldInstallServices()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#enableEditing()
     */
    @Override
    public boolean isEditingEnabled()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#removeIds(java.util.List)
     */
    @Override
    public void removeIds(final List<Integer> ids)
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getDescription()
     */
    @Override
    public String getDescription()
    {
        return searchTableConfig.getTableInfo().getDescription();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isMultipleSelection()
     */
    @Override
    public boolean isMultipleSelection()
    {
        return isMultipleSelection;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setMultipleSelection(boolean)
     */
    @Override
    public void setMultipleSelection(boolean isMultiple)
    {
        isMultipleSelection = isMultiple;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getParams()
     */
    @Override
    public List<Pair<String, Object>> getParams()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#complete()
     */
    @Override
    public void complete()
    {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getQueryTask()
     */
    @Override
    public Future<?> getQueryTask()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setQueryTask(java.util.concurrent.Future)
     */
    @Override
    public void setQueryTask(Future<?> queryTask)
    {
        //do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#queryTaskDone()
     */
    @Override
    public void queryTaskDone(final Object results)
    {
        // nuthin
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.QueryForIdResultsIFace#size()
     */
    @Override
    public int size()
    {
        return recIds.size();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#showProgress()
     */
    @Override
    public boolean showProgress()
    {
        return true;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.QueryForIdResultsIFace#getMaxTableRows()
     */
    @Override
    public int getMaxTableRows()
    {
        // TODO Auto-generated method stub
        return -1;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.db.QueryForIdResultsIFace#cacheFilled(java.util.Vector)
     */
    @Override
    public void cacheFilled(Vector<Vector<Object>> cache)
    {
        // nothing to do
    }
    
    
}
