/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.awt.Color;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;

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
    protected boolean               isMultipleSelection  = true;

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
    public void add(final String idStr)
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
    public void addIndex(final int index)
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
    public void setSearchTerm(final String searchTerm)
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
    @Override
    public Vector<Integer> getRecIds()
    {
        return recIds;
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
            
            String critiera = (tableInfo.isFieldNameOnlyForSQL() ? StringUtils.substringAfterLast(joinIdName, ".") : joinIdName) //$NON-NLS-1$
                              + " in (" + idsStr.toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            
            //System.out.println("["+critiera+"]");
            sqlStr = String.format(tableInfo.getViewSql(), new Object[] {joinIdName, critiera});
            //System.out.println("["+sqlStr+"]");
            sqlStr = QueryAdjusterForDomain.getInstance().adjustSQL(sqlStr);
            
        } else
        {
            String vsql = getTableInfo().getViewSql();
            sqlStr = idsStr.length() > 0 ? vsql.replace("%s", idsStr.toString()) : vsql; //$NON-NLS-1$
        }
        return sqlStr;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace#setSQL(java.lang.String)
     */
    public void setSQL(final String sql)
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
    public boolean isEditingEnabled()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#removeIds(java.util.List)
     */
    public void removeIds(final List<Integer> ids)
    {
        // no op
    }

    /**
     * @param isExpanded the isExpanded to set
     */
    public void setExpanded(final boolean isExpanded)
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
        //do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#queryTaskDone()
     */
    //@Override
    public void queryTaskDone(final Object results)
    {
        // nuthin
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#showProgress()
     */
    //@Override
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
