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
package edu.ku.brc.specify.plugins.sgr;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 27, 2010
 *
 */
public class GroupingColObjData
{
    private Integer grpId;
    private String  collectorNumber;
    private String  genus;
    private String  year;
    private String  mon;
    private Integer cnt;
    
    private List<Integer> rawIds = new ArrayList<Integer>();

    /**
     * @param grpId
     * @param collectorNumber
     * @param genus
     * @param year
     * @param mon
     * @param cnt
     */
    public GroupingColObjData(final Integer grpId, 
                              final String collectorNumber, 
                              final String genus, 
                              final String year,
                              final String mon, 
                              final Integer cnt)
    {
        super();
        this.grpId = grpId;
        this.collectorNumber = collectorNumber;
        this.genus = genus;
        this.year = year;
        this.mon = mon;
        this.cnt = cnt;
    }

    /**
     * @param id
     */
    public void addRawId(final int id)
    {
        rawIds.add(id);
    }
    
    /**
     * @return the rawIds
     */
    public List<Integer> getRawIds()
    {
        return rawIds;
    }

    /**
     * @return the grpId
     */
    public Integer getGrpId()
    {
        return grpId;
    }

    /**
     * @param grpId the grpId to set
     */
    public void setGrpId(Integer grpId)
    {
        this.grpId = grpId;
    }

    /**
     * @return the collectorNumber
     */
    public String getCollectorNumber()
    {
        return collectorNumber;
    }

    /**
     * @param collectorNumber the collectorNumber to set
     */
    public void setCollectorNumber(String collectorNumber)
    {
        this.collectorNumber = collectorNumber;
    }

    /**
     * @return the genus
     */
    public String getGenus()
    {
        return genus;
    }

    /**
     * @param genus the genus to set
     */
    public void setGenus(String genus)
    {
        this.genus = genus;
    }

    /**
     * @return the year
     */
    public String getYear()
    {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(String year)
    {
        this.year = year;
    }

    /**
     * @return the mon
     */
    public String getMon()
    {
        return mon;
    }

    /**
     * @param mon the mon to set
     */
    public void setMon(String mon)
    {
        this.mon = mon;
    }

    /**
     * @return the cnt
     */
    public Integer getCnt()
    {
        return cnt;
    }

    /**
     * @param cnt the cnt to set
     */
    public void setCnt(Integer cnt)
    {
        this.cnt = cnt;
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder("[");
        for (Field f : this.getClass().getDeclaredFields())
        {
            if (!isStaticField(f))
            {
                try
                {
                    b.append(f.getName() + "=" + f.get(this) + " ");
                } catch (IllegalAccessException e)
                {
                    // pass, don't print
                }
            }
        }
        b.append(']');
        return b.toString();
    }


    private boolean isStaticField(Field f)
    {
        return Modifier.isStatic(f.getModifiers());
    }

    
}
