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


/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class ERTICaptionInfoTreeLevel extends ERTICaptionInfoQB
{
    protected final ERTICaptionInfoTreeLevelGrp group;
    protected final int rank;
    protected int rankIdx;
    
    public ERTICaptionInfoTreeLevel(String  colName, 
                                    String  colLabel, 
                                    int     posIndex,
                                    String colStringId,
                                    final ERTICaptionInfoTreeLevelGrp group,
                                    final int rank)
    {
        super(colName, colLabel, true, null, posIndex, colStringId, null);
        this.group = group;
        this.rank = rank;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.ERTICaptionInfoQB#processValue(java.lang.Object)
     */
    @Override
    public Object processValue(Object value)
    {
        // TODO Auto-generated method stub
        return group.processValue(value, rankIdx);
    }

    /**
     * @return the rank
     */
    public int getRank()
    {
        return rank;
    }

    /**
     * @return the rankIdx
     */
    public int getRankIdx()
    {
        return rankIdx;
    }
    
    /**
     * @param value the rankIdx to set
     */
    public void setRankIdx(int value)
    {
        rankIdx = value;
    }
    
}
