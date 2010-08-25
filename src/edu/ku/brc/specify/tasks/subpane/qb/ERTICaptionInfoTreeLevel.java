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

import java.sql.SQLException;

import org.apache.log4j.Logger;


/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class ERTICaptionInfoTreeLevel extends ERTICaptionInfoQB
{
    protected static final Logger log = Logger.getLogger(ERTICaptionInfoTreeLevel.class);
    protected final ERTICaptionInfoTreeLevelGrp group;
    protected final int rank;
    protected final int fldIdx;
    protected int rankIdx;
    
    /**
     * @param colName
     * @param colLabel
     * @param posIndex
     * @param colStringId
     * @param group
     * @param rank
     */
    public ERTICaptionInfoTreeLevel(String  colName, 
                                    String  colLabel, 
                                    int     posIndex,
                                    String colStringId,
                                    final ERTICaptionInfoTreeLevelGrp group,
                                    final int rank)
    {
        this(colName, colLabel, posIndex, colStringId, group, rank, 0);
    }

    public ERTICaptionInfoTreeLevel(String  colName, 
            String  colLabel, 
            int     posIndex,
            String colStringId,
            final ERTICaptionInfoTreeLevelGrp group,
            final int rank, final int fldIdx)
    {
    	super(colName, colLabel, true, null, posIndex, colStringId, null, null);
    	this.group = group;
    	this.rank = rank;
    	this.fldIdx = fldIdx;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.ERTICaptionInfoQB#processValue(java.lang.Object)
     */
    @Override
    public Object processValue(Object value)
    {
        try
        {
        	return group.processValue(value, rankIdx, fldIdx);
        } catch (SQLException ex)
        {
        	log.error(ex);
        	return null;
        }
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
