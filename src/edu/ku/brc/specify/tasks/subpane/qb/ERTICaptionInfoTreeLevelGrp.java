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
    protected boolean                                useCache     = true;
 
    //XXX stuff to test sorting in 
//  Collections.sort(cache, new Comparator<Vector<Object>>() {
    //
//                    /* (non-Javadoc)
//                     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
//                     */
//                    @Override
//                    public int compare(Vector<Object> o1, Vector<Object> o2)
//                    {
//                        String c1 = (String )o1.get(1);
//                        String c2 = (String )o2.get(1);
//                        if (c1 == null && c2 == null)
//                        {
//                            return 0;
//                        }
//                        if (c1 == null)
//                        {
//                            return -1;
//                        }
//                        if (c2 == null)
//                        {
//                            return 1;
//                        }
//                        return c1.compareTo(c2);
//                    }
//                    
//                });

    
    
    public Object processValue(final Object value, final int rankIdx)
    {
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
        //XXX implement caching...
        return currentRanks[rankIdx];
    }

    protected Pair<Integer, String> getAncestorInfo(final Object ancestor)
    {
        if (ancestor == null)
        {
            return new Pair<Integer, String>(null, null);
        }
        Object[] info = (Object[] )ancestor;
        return new Pair<Integer, String>((Integer )info[1], (String )info[0]);
    }
    
    protected void newValue(final Object value)
    {
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
        //System.out.println("ERTICaptionInfoTreeGrp.newValue " + value);
    }

    public ERTICaptionInfoTreeLevelGrp(final Class<?> treeClass, final int treeDefId, final String alias)
    {
        this.treeClass = treeClass;
        this.treeDefId = treeDefId;
        this.alias = alias;
        this.members = new Vector<ERTICaptionInfoTreeLevel>();
    }
    
    protected String getNodeNameFld()
    {
        return "name";
    }
    
    protected String getNodeTblName()
    {
        return treeClass.getSimpleName().toLowerCase();
    }
    
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
    
    public ERTICaptionInfoTreeLevel addRank(final TreeLevelQRI rank, final String colName, final String lbl, final String id)
    {
        if (!(treeDefId == rank.getTreeDefId() && treeClass.equals(rank.getTreeDataClass()) && alias.equals(rank.getTableAlias())))
        {
            return null;
        }
        ERTICaptionInfoTreeLevel result = new ERTICaptionInfoTreeLevel(colName, lbl, 0, id, this, rank.getRankId());
        members.add(result);
        return result;
    }
    
    public void setUp()
    {
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

    }
}
