/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.ui;

import java.util.Hashtable;

/**
 * Used to gather CollectionObject Information for Loans.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 28, 2009
 *
 */
public class ColObjInfo 
{
    protected String  taxonName;
    protected String  catNo;
    protected Integer colObjId;
    protected Hashtable<Integer, PrepInfo> preps = null;
    
    /**
     * @param colObjId
     * @param catNo
     * @param taxonName
     */
    public ColObjInfo(final Integer colObjId,
                      final String catNo,
                      final String taxonName)
    {
        super();
        this.colObjId  = colObjId;
        this.catNo     = catNo;
        this.taxonName = taxonName;
    }
    
    /**
     * @param pi
     */
    public void add(final PrepInfo pi)
    {
        if (preps == null)
        {
            preps = new Hashtable<Integer, PrepInfo>();
        }
        preps.put(pi.getPrepId(), pi);
    }
    
    /**
     * @param prepId
     * @return
     */
    public PrepInfo get(final Integer prepId)
    {
        return preps != null ? preps.get(prepId) : null;
    }
    
     
    /**
     * @return the catNo
     */
    public String getCatNo()
    {
        return catNo;
    }

    /**
     * @param catNo the catNo to set
     */
    public void setCatNo(String catNo)
    {
        this.catNo = catNo;
    }

    /**
     * @return the preps
     */
    public Hashtable<Integer, PrepInfo> getPreps()
    {
        return preps;
    }

    /**
     * @return the taxonName
     */
    public String getTaxonName()
    {
        return taxonName;
    }

    /**
     * @return the colObjId
     */
    public Integer getColObjId()
    {
        return colObjId;
    }
}
