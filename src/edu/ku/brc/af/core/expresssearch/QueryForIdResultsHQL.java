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

import org.apache.commons.lang.StringUtils;

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
    
    protected String                overrideSQL  = null;
    protected List<ERTICaptionInfo> captions     = null; 
    protected boolean               isExpanded   = false;
    
    @SuppressWarnings("unchecked")
    public QueryForIdResultsHQL(final SearchTableConfig searchTableConfig,
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
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getRecordSetColumnInx()
     */
    //@Override
    public int getRecordSetColumnInx()
    {
        return 0;
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
        return searchTableConfig.getSQL(searchTermArg, false, ids);
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
        return searchTableConfig.getTableInfo().getClassObj().getSimpleName();
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
                                                          "", 
                                                          i+1);
            try
            {
                caption.setColClass(Class.forName(dfc.getFieldInfo().getType()));
                
            } catch (ClassNotFoundException ex)
            {
                ex.printStackTrace();
            }
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
}
