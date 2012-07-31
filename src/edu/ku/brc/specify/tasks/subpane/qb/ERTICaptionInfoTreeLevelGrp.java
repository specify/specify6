/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Modifications Copyright (C) 2012 President and Fellows of Harvard College
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.util.Pair;

/**
 * @author timbo
 * @author Lawrence Chan lchan@indigocube.net (Harvard University Herbaria)
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
    private static final Logger      log               = Logger.getLogger(ERTICaptionInfoTreeLevelGrp.class);
    
    protected final Vector<ERTICaptionInfoTreeLevel> members;
    protected final Class<?>                         treeClass;
    protected final int                              treeDefId;
    protected final String                           alias;
    protected final Vector<String>				     fieldsToRetrieve;		
    protected final LookupsCache                     lookupCache;
    protected final boolean                          useCache;
    
    protected static boolean                         useHibernate = false;
    
    protected QueryIFace                             query = null;
    protected Statement                              statement = null;
    protected String                                 querySQL = null;
    
    //protected Object[]                               currentRanks = null;
    //protected AtomicReferenceArray<Object[]>         currentRanks = null;
    //protected Object                                 currentVal   = null;
    //protected final AtomicReference<Object>		     currentVal = new AtomicReference<Object>();
    protected DataProviderSessionIFace               session = null;

    protected boolean                                isSetup      = false;
    


    /**
     * @param treeClass
     * @param treeDefId
     * @param alias
     * @param useCache
     * @param cacheSize
     */
    public ERTICaptionInfoTreeLevelGrp(final Class<?> treeClass, final int treeDefId, final String alias,
            final boolean useCache, final Integer cacheSize)
    {
    	this.treeClass = treeClass;
    	this.treeDefId = treeDefId;
    	this.alias = alias;
    	this.fieldsToRetrieve = new Vector<String>();
    	this.members = new Vector<ERTICaptionInfoTreeLevel>();    	
    	this.useCache = useCache;
    	//this.useCache = false; //!!!!!!!!!!!!!!!!!!!!!!But already have the ancestorsCache so why bother???
    	if (this.useCache)
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
    public Object processValue(final Object value, final int rankIdx, final int fldIdx) throws SQLException
    {
        if (!isSetup)
        {
            throw new RuntimeException("ERTICaptionInfoTreeLevelGrp: " + treeClass.getName() + " group was not setup.");
        }
        
        if (value == null)
        {
            return null;
        }
        
//        Object val = currentVal.get();
//        if (!value.equals(val))
//        {
//            Object current = newValue(value);
//            if (useCache)
//            {
//            	lookupCache.addKey((Integer )value, current);
//            }
//            Object[] currentRank = (Object[])((Object[] )current)[rankIdx];
//            if (currentRank != null)
//            {
//            	return currentRank[fldIdx];
//            }
//            currentVal.set(value);
//            return null;
//        }
        
        //Object[] currentRank = (Object[] )currentRanks[rankIdx];
//        Object lookedUp = lookupCache.lookupKey((Integer )value);
//        if (lookedUp != null)
//        {
//        	Object[] currentRank =  (Object[])((Object[] )lookedUp)[rankIdx];
//        	//Object[] currentRank =  currentRanks.get(rankIdx);
//        	if (currentRank == null)
//        	{
//        		return null;
//        	}
//        	return currentRank[fldIdx];
//        } else
//        {
//            Object current = newValue(value);
//            if (useCache)
//            {
//            	lookupCache.addKey((Integer )value, current);
//            }
//            //currentVal.set(value);
//            Object[] currentRank = (Object[])((Object[] )current)[rankIdx];
//            if (currentRank != null)
//            {
//            	return currentRank[fldIdx];
//            }
//            //currentVal.set(value);
//            return null;
//        }
        
        Object current = useCache ? lookupCache.lookupKey((Integer )value) : null;
        if (current == null)
        {
            current = newValue(value);
            if (useCache)
            {
            	lookupCache.addKey((Integer )value, current);
            }
        }
        Object[] currentRank = (Object[])((Object[] )current)[rankIdx];
        if (currentRank != null)
        {
         	return currentRank[fldIdx];
        }
        return null;        
        
    }

    /**
     * @param ancestor
     * @return Pair of ancestor's rank and name.
     */
    protected Pair<Integer, String[]> getAncestorInfo(final Object ancestor)
    {
        if (ancestor == null)
        {
            return new Pair<Integer, String[]>(null, null);
        }
        Object[] info = (Object[] )ancestor;
        String[] vals = new String[info.length-1];
        for (int c = 0; c < vals.length; c++)
        {
        	vals[c] = info[c] == null ? null : info[c].toString();
        }
        return new Pair<Integer, String[]>((Integer )info[info.length - 1], vals);
    }
    
    
    
    /**
     * @param value
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    protected Object[] newValue(final Object value) throws SQLException
    {
    	Object[] currentRanks = null;
        //synchronized(currentRanks) 
        //{
        	if (useCache)
        	{
        		Object[] lookedUp = (Object[] )lookupCache.lookupKey((Integer )value);
        		//AtomicReferenceArray<Object[]> lookedUp = (AtomicReferenceArray<Object[]>)lookupCache.lookupKey((Integer )value);
        		if (lookedUp != null)
        		{
        			currentRanks = lookedUp;
        			return currentRanks;
        		}
        	} 
        	if (currentRanks == null)
        	{
        		currentRanks = new Object[members.size()];
        		//currentRanks = new AtomicReferenceArray<Object[]>(members.size());
        		for (int r = 0; r < members.size(); r++)
        		{
        			currentRanks[r] = null;
        			//currentRanks.set(r, null);
        		}
        	}
        //}
        ResultSet ancestorRows = null;
        Iterator<Object> ancestors = null;
        Pair<Integer, String[]> ancestor = null;
        Statement statement = null;
        if (useHibernate)
        {
            query.setParameter("descendantArg", value);
        	ancestors = (Iterator<Object> )query.list().iterator();
        } else 
        {
        	String sql = setupQuery(value);
        	statement = DBConnection.getInstance().getConnection().createStatement();
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
						int arraySize = fieldsToRetrieve == null ? 2 : fieldsToRetrieve.size() + 1;
						Object[] result = new Object[arraySize];
						for (int c = 0; c < arraySize-1; c++)
						{
							result[c] = rows.getString(c+1);
						}
						result[arraySize-1] = Integer.valueOf(rows.getString(arraySize));
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
        
        //XXX using a pair for ancestor will not work when fieldsToRetrieve is non null
    	ancestor = ancestors.hasNext() ? getAncestorInfo(ancestors.next()) : null;
        //members are ordered by rank
        for (ERTICaptionInfoTreeLevel member : members)
        {
            //if (ancestor != null) System.out.println(ancestor.getFirst() + " " + member.getRank());
        	while (ancestor != null && ancestor.getFirst() < member.getRank())
            {
                ancestor = ancestors.hasNext() ? getAncestorInfo(ancestors.next()) : null;
            }
            if (ancestor != null && ancestor.getFirst() == member.getRank())
            {
                currentRanks[member.getRankIdx()] = ancestor.getSecond();
            	//currentRanks.
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
        if (statement != null)
        {
        	statement.close();
        }
        
//        if (useCache)
//        {
//            lookupCache.addKey((Integer )value, currentRanks);
//        }
        return currentRanks;
    }

    /**
     * 
     * @author lchan
     * @param value
     * @return
     */
    protected String setupQuery(Object value)
    {
        String ancestorsIn = getAncestorsIn(treeClass, (Integer) value);
        
    	return querySQL.replace(":ancestorsIn", ancestorsIn);
    }
    
    
    /**
     * @return the list of fields that need to be retrieved.
     * by default this is just the name of the 'name' field for nodes in the tree.
     */
    protected String geFldsList()
    {
        if (fieldsToRetrieve == null)
        {
        	return "name";
        } else
        {
        	StringBuilder result = new StringBuilder();
        	for (String fld : fieldsToRetrieve)
        	{
        		if (result.length() > 0)
        		{
        			result.append(", ");
        		}
        		result.append(fld);
        	}
        	return result.toString();
        }
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
    public ERTICaptionInfoTreeLevel addRank(final TreeLevelQRI rank, final String colName, final String lbl, final String id, 
    		final String fldName)
    {
        if (isSetup)
        {
            throw new RuntimeException("ERTICaptionInfoTreeLevelGrp: tree for " + treeClass.getSimpleName() + " is already setup.");
        }
        
        if (!(treeDefId == rank.getTreeDefId() && treeClass.equals(rank.getTreeDataClass()) && alias.equals(rank.getTableAlias())))
        {
            return null;
        }
        int fldIdx = fieldsToRetrieve.indexOf(fldName);
        if (fldIdx == -1)
        {
        	fieldsToRetrieve.add(fldName);
        	fldIdx = fieldsToRetrieve.size() - 1;
        }
        ERTICaptionInfoTreeLevel result = new ERTICaptionInfoTreeLevel(colName, lbl, 0, id, this, rank.getRankId(), fldIdx);
        members.add(result);
        return result;
    }
    
    private final HashMap<Integer, String> ancestorsCache = new HashMap<Integer, String>();
    
    /**
     * Returns the ids of a node and its ancestors in the form of id1,
     * id2,...id3. It caches the results of the recursive ancestors because
     * Specify calls it for each result row/column.
     * 
     * @author lchan
     * @param clazz
     * @param nodeId
     * @return
     */
    private String getAncestorsIn(Class<?> clazz, Integer nodeId) {
        DataProviderSessionIFace mySession = DataProviderFactory.getInstance().createSession();
        try {

            String ancestorsIn;
            QueryIFace query = mySession.createQuery("select e.parent.id from "
                    + clazz.getName() + " e where e.id = :nodeId", false);
            query.setParameter("nodeId", nodeId);
            Integer parentId = (Integer) query.uniqueResult();
            Map<Integer, String> ancestorsCacheSync = Collections.synchronizedMap(ancestorsCache);
            if (ancestorsCacheSync.containsKey(parentId)) {
                ancestorsIn = ancestorsCacheSync.get(parentId);
            } else {
                ancestorsIn = getAncestorsInRecursive(clazz, nodeId, mySession);
                ancestorsCacheSync.put(parentId, ancestorsIn);
            }

            String in = nodeId + ", " + ancestorsIn;
            in = "in(" + in.substring(0, in.length() - 2)
                    + ")";

            return in;
        } finally {
            mySession.close();
        }
    }
    
    private String getAncestorsInRecursive(Class<?> clazz, Integer nodeId, DataProviderSessionIFace mySession) {
        QueryIFace query = mySession.createQuery("select e.parent.id from " + clazz.getName() + " e where e.id = :nodeId", false);
        query.setParameter("nodeId", nodeId);
        Integer parentId = (Integer) query.uniqueResult();
        if (parentId == null) {
            return "";
        } else {
            return parentId + ", " + getAncestorsInRecursive(clazz, parentId, mySession);
        }
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
        
        String[] currentRanks = new String[members.size()];
        
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
        		+ geFldsList()
        		+ ", rankId from "
        		+ getNodeTblName()
        		+ " where id :ancestorsIn and"
        		+ getNodeTreeFldName() + "=" + this.treeDefId + " and rankId in(" + rankStr + ") order by rankId ";
        	query = session.createQuery(ancestorSQL, true);

        }
        else {
        	//leave session open all the time for better performance
        	//session = DataProviderFactory.getInstance().createSession();

        	
        	// lchan: hardcoded because Specify uses a different ID field for each entity
        	String idField = treeClass.getSimpleName() + "Id";
        	
        	querySQL = "select "
        		+ geFldsList()
        		+ ", rankId from "
        		+ getNodeTblName().toLowerCase()
        		+ " where " + idField + " :ancestorsIn and "
        		+ getNodeTreeFldName() + "=" + this.treeDefId + " and rankId in(" + rankStr + ") order by rankId ";
        	Connection connection = DBConnection.getInstance().getConnection();
        	statement = connection.createStatement();
        }
        
    	isSetup = true;
    }
    
}
