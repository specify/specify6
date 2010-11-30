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
package edu.ku.brc.specify.ui;

import edu.ku.brc.specify.datamodel.LoanPreparation;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Nov 29, 2010
 *
 */
public class LoanReturnInfo
{
    private LoanPreparation lpo;
    private boolean         isResolved;
    private String          remarks;
    private int             returnedQty;
    private int             resolvedQty;
    
    /**
     * @param lpo
     * @param remarks
     * @param returnedQty
     * @param resolvedQty
     * @param isResolved
     */
    public LoanReturnInfo(final LoanPreparation lpo, 
                          final String          remarks, 
                          final int             returnedQty, 
                          final int             resolvedQty,
                          final boolean         isResolved)
    {
        super();
        this.lpo         = lpo;
        this.remarks     = remarks;
        this.returnedQty = returnedQty;
        this.resolvedQty = resolvedQty;
        this.isResolved  = isResolved;
    }
    
    /**
     * @return
     */
    public LoanPreparation getLoanPreparation()
    {
        return lpo;
    }
    
    /**
     * @return
     */
    public String getRemarks()
    {
        return remarks;
    }
    
    public int getReturnedQty()
    {
        return returnedQty;
    }
    
    public int getResolvedQty()
    {
        return resolvedQty;
    }
    
    /**
     * @return the isResolved
     */
    public boolean isResolved()
    {
        return isResolved;
    }
    
}

