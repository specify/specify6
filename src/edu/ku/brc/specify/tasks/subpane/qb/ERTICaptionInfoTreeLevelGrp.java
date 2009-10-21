/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * A grouping of columns in a result-set that refer to levels in a common tree.
 * 
 * Node number(s) retrieved by initial db query are used to determine level names while results are
 * prepared for display. 
 * 
 * At most one query is required per row to determine values for all columns in a group. 
 */
public class ERTICaptionInfoTreeLevelGrp
{
    protected final Vector<ERTICaptionInfoTreeLevel> members;
    protected final Class<?>                         treeClass;
    protected final int                              treeDefId;
    protected final String                           alias;
    protected final LookupsCache                     lookupCache;
    protected final boolean                          useCache;
    
    protected static boolean                         useHibernate = false;
    
    protected QueryIFace                             query = null;
    protected Statement                              statement = null;
    protected String                                 querySQL = null;
    
    protected String[]                               currentRanks = null;
    protected Object                                 currentVal   = null;

    protected DataProviderSessionIFace               session = null;

    protected boolean                                isSetup      = false;
    

    /**
     * @param treeClass
     * @param treeDefId
     * @param alias
     */
    public ERTICaptionInfoTreeLevelGrp(final Class<?> treeClass, final int treeDefId, final String alias,
                                       final boolean useCache, final Integer cacheSize)
    {
        this.treeClass = treeClass;
        this.treeDefId = treeDefId;
        this.alias = alias;
        this.members = new Vector<ERTICaptionInfoTreeLevel>();
        this.useCache = useCache;
        if (useCache)
        {
            lookupCache = cacheSize == null ? new LookupsCache() : new LookupsCache(cacheSize);
        }
        else
        {
            lookupCache = null;
        }
    }

    /**
     * @param value
     * @param rankIdx
     * @return
     */
    public Object processValue(final Object value, final int rankIdx) throws SQLException
    {
        if (!isSetup)
        {
            throw new RuntimeException("ERTICaptionInfoTreeLevelGrp: " + treeClass.getName() + " group was not setup.");
        }
        
        if (value == null)
        {
            return null;
        }
        //XXX make sure equals() works...
        if (!value.equals(currentVal))
        {
            newValue(value);
            currentVal = value;
        }
        
        return currentRanks[rankIdx];
    }

    /**
     * @param ancestor
     * @return Pair of ancestor's rank and name.
     */
    protected Pair<Integer, String> getAncestorInfo(final Object ancestor)
    {
        if (ancestor == null)
        {
            return new Pair<Integer, String>(null, null);
        }
        Object[] info = (Object[] )ancestor;
        return new Pair<Integer, String>((Integer )info[1], (String )info[0]);
    }
    
    
    
    /**
     * @param value
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    protected void newValue(final Object value) throws SQLException
    {
        if (useCache)
        {
            String[] lookedUp = (String[] )lookupCache.lookupKey((Integer )value);
            if (lookedUp != null)
            {
                currentRanks = lookedUp;
                return;
            }
        }

        if (useCache)
        {
            currentRanks = new String[members.size()];
            for (int r = 0; r < currentRanks.length; r++)
            {
                currentRanks[r] = null;
            }
        }
        ResultSet ancestorRows = null;
        Iterator<Object> ancestors = null;
        Pair<Integer, String> ancestor = null;
        if (useHibernate)
        {
            query.setParameter("descendantArg", value);
        	ancestors = (Iterator<Object> )query.list().iterator();
        } else 
        {
        	String sql = setupQuery(value);
        	final ResultSet rows = statement.executeQuery(sql);
        	ancestorRows = rows;
        	ancestors = new Iterator<Object>() {

				@Override
				public boolean hasNext() 
				{
					try 
					{
						return rows.next();
					} catch (SQLException ex)
					{
						return false;
					}
				}

				@Override public Object next()
				{
					try
					{
						Object[] result = new Object[2];
						result[0] = rows.getString(1);
						result[1] = Integer.valueOf(rows.getString(2));
						return result;
					} catch (SQLException ex)
					{
						return null;
					}
				}

				@Override
				public void remove()
				{
					// ignore					
				}
        		
        	};

        }
        
    	ancestor = ancestors.hasNext() ? getAncestorInfo(ancestors.next()) : null;
        //members are ordered by rank
        for (ERTICaptionInfoTreeLevel member : members)
        {
            while (ancestor != null && ancestor.getFirst() < member.getRank())
            {
                ancestor = ancestors.hasNext() ? getAncestorInfo(ancestors.next()) : null;
            }
            if (ancestor != null && ancestor.getFirst() == member.getRank())
            {
                currentRanks[member.getRankIdx()] = ancestor.getSecond();
            }
            else
            {
                currentRanks[member.getRankIdx()] = null;
            }
        }
        if (ancestorRows != null)
        {
        	ancestorRows.close();
        }
        
        if (useCache)
        {
            lookupCache.addKey((Integer )value, currentRanks);
        }
    }

    protected String setupQuery(Object value)
    {
    	return querySQL.replace(":descendantArg", value.toString());
    }
    
    
    /**
     * @return the name of the 'name' field for nodes in the tree.
     */
    protected String getNodeNameFld()
    {
        return "name";
    }
    
    /**
     * @return the table name for the tree.
     */
    protected String getNodeTblName()
    {
        return treeClass.getSimpleName().toLowerCase();
    }
    
    /**
     * @return the field that stores the tree definition for the tree.
     */
    protected String getNodeTreeFldName() 
    {
        return treeClass.getSimpleName().toLowerCase() + "TreeDefId";
    }
    
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable
    {
        if (session != null)
        {
            session.close();
        }
        if (statement != null)
        {
        	statement.close();
        }
    }

    /**
     * @return the treeDefId
     */
    public int getTreeDefId()
    {
        return treeDefId;
    }

    /**
     * @return the treeClass
     */
    public Class<?> getTreeClass()
    {
        return treeClass;
    }
    
    /**
     * @return the alias
     */
    public String getAlias()
    {
        return alias;
    }
    
    /**
     * @param rank
     * @param colName
     * @param lbl
     * @param id
     * @return a ERTICaptionInfoTreeLevel object for the column.
     * 
     * Adds a member to the group and returns a CaptionInfo for the rank's column in the result-set.
     */
    public ERTICaptionInfoTreeLevel addRank(final TreeLevelQRI rank, final String colName, final String lbl, final String id)
    {
        if (isSetup)
        {
            throw new RuntimeException("ERTICaptionInfoTreeLevelGrp: tree for " + treeClass.getSimpleName() + " is already setup.");
        }
        
        if (!(treeDefId == rank.getTreeDefId() && treeClass.equals(rank.getTreeDataClass()) && alias.equals(rank.getTableAlias())))
        {
            return null;
        }
        ERTICaptionInfoTreeLevel result = new ERTICaptionInfoTreeLevel(colName, lbl, 0, id, this, rank.getRankId());
        members.add(result);
        return result;
    }
    
    /**
     * Prepares group to process a result set.
     * MUST be called after all members have been added to the group.
     */
    public void setUp() throws SQLException
    {
        if (isSetup)
        {
            throw new RuntimeException("ERTICaptionInfoTreeLevelGrp: tree for " + treeClass.getSimpleName() + " is already setup.");
        }
        
        Collections.sort(members, new Comparator<ERTICaptionInfoTreeLevel>() {

            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(ERTICaptionInfoTreeLevel o1, ERTICaptionInfoTreeLevel o2)
            {
                if (o1.getRank() < o2.getRank())
                {
                    return -1;
                }
                if (o1.getRank() == o2.getRank())
                {
                    return 0;
                }
                return 1;
            }
            
        });
        
        currentRanks = new String[members.size()];
        
        int m = 0;
        String rankStr = "";
        for (ERTICaptionInfoTreeLevel member : members)
        {
            member.setRankIdx(m);
            currentRanks[m++] = null;
            if (!StringUtils.isEmpty(rankStr))
            {
                rankStr += ",";
            }
            rankStr += member.getRank();
        }
        
        if (useHibernate)
        {
        	//leave session open all the time for better performance
        	session = DataProviderFactory.getInstance().createSession();

        	String ancestorSQL = "select "
        		+ getNodeNameFld()
        		+ ", rankId from "
        		+ getNodeTblName()
        		+ " where "
        		+ ":descendantArg between nodenumber and highestchildnodenumber and "
        		+ getNodeTreeFldName() + "=" + this.treeDefId + " and rankId in(" + rankStr + ") order by rankId ";
        	query = session.createQuery(ancestorSQL, true);

        }
        else 
        {
        	querySQL = "select "
        		+ getNodeNameFld()
        		+ ", rankId from "
        		+ getNodeTblName().toLowerCase()
        		+ " where "
        		+ ":descendantArg between nodenumber and highestchildnodenumber and "
        		+ getNodeTreeFldName() + "=" + this.treeDefId + " and rankId in(" + rankStr + ") order by rankId ";
        	Connection connection = DBConnection.getInstance().getConnection();
        	statement = connection.createStatement();
        }
        
    	isSetup = true;
    }
    
}
