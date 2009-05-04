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

/**
 * Used to gather Preparation information for Loans
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 28, 2009
 *
 */
public class PrepInfo
{
    protected Integer prepId;
    protected int     type;
    protected int     qtyPrep;
    protected int     qtyLoaned;
    protected int     qtyResolved;
    
    /**
     * @param prepId
     * @param qtyLoaned
     * @param qtyResolved
     * @param qtyAvail
     */
    public PrepInfo(Integer prepId, 
                    int type, 
                    int qtyPrep, 
                    int qtyLoaned, 
                    int qtyResolved)
    {
        super();
        this.prepId      = prepId;
        this.type        = type;
        this.qtyPrep     = qtyPrep;
        this.qtyLoaned   = qtyLoaned;
        this.qtyResolved = qtyResolved;
    }

    public void add(final int qtyLoanedArg, 
                    final int qtyResolvedArg)
    {
        this.qtyLoaned += qtyLoanedArg;
        this.qtyResolved += qtyResolvedArg;
    }
    
    /**
     * @return
     */
    public int getAvailable()
    {
        return qtyPrep - qtyLoaned + qtyResolved;
    }
    
    /**
     * @return the prepId
     */
    public Integer getPrepId()
    {
        return prepId;
    }

    /**
     * @return the type
     */
    public int getType()
    {
        return type;
    }

    /**
     * @return the qtyPrep
     */
    public int getQtyPrep()
    {
        return qtyPrep;
    }

    /**
     * @return the qtyLoaned
     */
    public int getQtyLoaned()
    {
        return qtyLoaned;
    }

    /**
     * @return the qtyResolved
     */
    public int getQtyResolved()
    {
        return qtyResolved;
    }

}

