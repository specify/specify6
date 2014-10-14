/* Copyright (C) 2012, University of Kansas Center for Research
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
package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 22, 2014
 *
 */
public class GeoSearchResultsItem
{
    public String  name;
    public Integer geonameId;
    public String  isoCode;
    
    // These are for capturing Rank Errors
    public int currentRankId;  // Bad RankId
    public int goodRankId;     // Should be RankId
    

    public GeoSearchResultsItem(final String name)
    {
        this(name, null, null);
    }

    public GeoSearchResultsItem(final String name, final int currentRankId, final int goodRankId)
    {
        this(name, null, null);
        
        this.currentRankId = currentRankId;
        this.goodRankId    = goodRankId;
    }

    /**
     * @param name
     * @param recId
     * @param isoCode
     */
    public GeoSearchResultsItem(final String name, final Integer geonameId, final String isoCode)
    {
        super();
        this.name          = name;
        this.geonameId     = geonameId;
        this.isoCode       = isoCode;
        
        this.currentRankId = 0;
        this.goodRankId    = 0;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return name;
    }
}
