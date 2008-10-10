/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

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
    
    protected QueryIFace                             query = null;

    protected String[]                               currentRanks = null;
    protected Object                                 currentVal   = null;

    protected DataProviderSessionIFace               session = null;

    protected LinkedList<Pair<Integer, String[]>>    lookupTbl    = null;
    protected static final int                       lookupSize   = 20;
    protected static final boolean                   useCache     = true;
 
    protected boolean                                isSetup      = false;
    

    /**
     * @param treeClass
     * @param treeDefId
     * @param alias
     */
    public ERTICaptionInfoTreeLevelGrp(final Class<?> treeClass, final int treeDefId, final String alias)
    {
        this.treeClass = treeClass;
        this.treeDefId = treeDefId;
        this.alias = alias;
        this.members = new Vector<ERTICaptionInfoTreeLevel>();
        if (useCache)
        {
            lookupTbl = new LinkedList<Pair<Integer, String[]>>();
        }
    }

    /**
     * @param value
     * @param rankIdx
     * @return
     */
    public Object processValue(final Object value, final int rankIdx)
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
     * @return the ranks for the value.
     * 
     * Checks to see if value is in the cache of looked up values
     * and returns it's ranks or null if it is not found.
     */
    protected String[] lookupValue(final Object value)
    {
        for (Pair<Integer, String[]> val : lookupTbl)
        {
            if (val.getFirst().equals(value))
            {
                return val.getSecond();
            }
        }
        return null;
    }
    
    /**
     * @param key
     * @param value
     */
    protected void addToCache(final Integer key, final String[] value)
    {
        if (lookupTbl.size() == lookupSize)
        {
            lookupTbl.remove();
        }
        lookupTbl.add(new Pair<Integer, String[]>(key, value));
    }
    
    protected void newValue(final Object value)
    {
        if (useCache)
        {
            String[] lookedUp = lookupValue(value);
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
        
        query.setParameter("descendantArg", value);
        Iterator<?> ancestors = query.list().iterator();
        Pair<Integer, String> ancestor = ancestors.hasNext() ? getAncestorInfo(ancestors.next()) : null;
        
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
        
        if (useCache)
        {
            addToCache((Integer )value, currentRanks);
        }
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
    public void setUp()
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

        isSetup = true;
    }
    
}
