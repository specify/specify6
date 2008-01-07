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

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.ui.db.QueryForIdResultsIFace;


/**
 * This class is used for collecting all the record primary keys for a given Express Definition.
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
public class QueryForIdResultsSQL implements QueryForIdResultsIFace
{
    protected String                  searchId;
    protected Integer                 joinColTableId;
    protected ExpressResultsTableInfo tableInfo;
    protected Vector<Integer>         recIds       = new Vector<Integer>();
    protected Vector<Integer>         indexes      = new Vector<Integer>();
    protected String                  searchTerm   = null;
    protected Integer                 displayOrder = null;
    
    protected String                  overrideSQL  = null;
    protected boolean                 isExpanded   = false;
    
    protected Hashtable<Integer, Boolean> duplicateRecId = new Hashtable<Integer, Boolean>();
    

    /**
     * Constructs the Results.
     * @param searchId The Express Search definition ID
     * @param joinColTableId the Join's Table ID (may be null)
     * @param tableInfo the table info 
     */
    public QueryForIdResultsSQL(final String                  searchId, 
                                final Integer                 joinColTableId, 
                                final ExpressResultsTableInfo tableInfo,
                                final Integer                 displayOrder,
                                final String                  searchTerm)
    {
        super();
        this.searchId          = searchId;
        this.joinColTableId    = joinColTableId;
        this.tableInfo         = tableInfo;
        this.displayOrder      = displayOrder;
        this.searchTerm        = searchTerm;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#isHQL()
     */
    //@Override
    public boolean isHQL()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getTitle()
     */
    public String getTitle()
    {
        return tableInfo.getTitle();
    }
    
    /**
     * @return the displayOrder
     */
    public Integer getDisplayOrder()
    {
        return displayOrder;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#cleanUp()
     */
    public void cleanUp()
    {
        tableInfo = null;
        
        recIds.clear();
        recIds = null;
        
        duplicateRecId.clear();
    }
    
    /**
     * @param id
     */
    public void add(final Integer id)
    {
        if (duplicateRecId.get(id) == null)
        {
            duplicateRecId.put(id, true);
            recIds.add(id);
        }
    }

    /**
     * @param ids
     */
    public void add(final List<?> ids)
    {
        recIds.ensureCapacity(ids.size());
        for (Object obj : ids)
        {
            recIds.add((Integer)obj);
        }
    }

    /**
     * @param idStr
     */
    public void add(String idStr)
    {
        add(Integer.parseInt(idStr)); 
    }
    

    /**
     * Returns the number of indexes.
     * @return the number of indexes
     */
    public int getNumIndexes()
    {
        return indexes.size();
    }

    /**
     * Adds an index.
     * @param index the index to add
     */
    public void addIndex(int index)
    {
        indexes.add(index);
    }

    public String getSearchId()
    {
        return searchId;
    }

    public Integer getJoinColTableId()
    {
        return joinColTableId;
    }

    /**
     * @return the searchTerm
     */
    public String getSearchTerm()
    {
        return searchTerm;
    }

    /**
     * @param searchTerm the searchTerm to set
     */
    public void setSearchTerm(String searchTerm)
    {
        this.searchTerm = searchTerm;
    }

    public ExpressResultsTableInfo getTableInfo()
    {
        return tableInfo;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.ExpressSearchResultsIFace#getRecIds()
     */
    public Vector<Integer> getRecIds()
    {
        return recIds;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.ExpressSearchResultsIFace#getTableId()
     */
    //@Override
    public int getTableId()
    {
        return Integer.parseInt(tableInfo.getTableId());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.ExpressSearchResultsIFace#getIconName()
     */
    //@Override
    public String getIconName()
    {
        return tableInfo.getIconName();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getVisibleCaptionInfo()
     */
    //@Override
    public List<ERTICaptionInfo> getVisibleCaptionInfo()
    {
        return tableInfo.getVisibleCaptionInfo();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.ExpressSearchResultsIFace#getBannerColor()
     */
    //@Override
    public Color getBannerColor()
    {
        return tableInfo.getColor();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.ExpressSearchResultsIFace#getSQL(java.lang.String, java.util.Vector)
     */
    //@Override
    public String getSQL(final String searchTermArg, 
                         final Vector<Integer> ids)
    {
        if (StringUtils.isNotEmpty(overrideSQL))
        {
            return overrideSQL;
        }
        
        Vector<Integer> tempIds = ids == null ? recIds : ids;
        // else
        StringBuilder idsStr = new StringBuilder(recIds.size()*8);
        for (int i=0;i<tempIds.size();i++)
        {
            if (i > 0) idsStr.append(',');
            idsStr.append(tempIds.elementAt(i).toString());
        }
        
        String sqlStr;
        if (getJoinColTableId() != null)
        {
            String joinIdName     = null;
            for (ERTIJoinColInfo jci : tableInfo.getJoins())
            {
                if (joinColTableId == jci.getJoinTableIdAsInt())
                {
                    joinIdName = jci.getColName();
                }
            }
            if (joinIdName == null)
            {
                int x = 0;
                x++;
            }
            
            String critiera = (tableInfo.isFieldNameOnlyForSQL() ? StringUtils.substringAfterLast(joinIdName, ".") : joinIdName)
                              + " in (" + idsStr.toString() + ")";
            
            //System.out.println("["+critiera+"]");
            sqlStr = String.format(tableInfo.getViewSql(), new Object[] {joinIdName, critiera});
            //System.out.println("["+sqlStr+"]");
            sqlStr = ExpressSearchSQLAdjuster.getInstance().adjustSQL(sqlStr);
        } else
        {
            String vsql = getTableInfo().getViewSql();
            sqlStr = idsStr.length() > 0 ? vsql.replace("%s", idsStr.toString()) : vsql;
        }
        return sqlStr;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#setSQL(java.lang.String)
     */
    public void setSQL(String sql)
    {
        overrideSQL = sql;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isExpanded()
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
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#enableEditing()
     */
    public boolean enableEditing()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#removeIds(java.util.List)
     */
    public void removeIds(List<Integer> ids)
    {
        // no op
    }

    /**
     * @param isExpanded the isExpanded to set
     */
    public void setExpanded(boolean isExpanded)
    {
        this.isExpanded = isExpanded;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#getDescription()
     */
    public String getDescription()
    {
        return tableInfo != null ? tableInfo.getDescription() : null;
    }
    
}
