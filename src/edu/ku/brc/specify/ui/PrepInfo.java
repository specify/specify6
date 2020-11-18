/* Copyright (C) 2020, Specify Collections Consortium
 *
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
    protected int     qtyAvailable;

    /**
     *
     * @param prepId
     * @param type
     * @param qtyPrep
     * @param qtyAvailable
     */
    public PrepInfo(Integer prepId,
                    int type,
                    int qtyPrep,
                    int qtyAvailable)
    {
        super();
        this.prepId      = prepId;
        this.type        = type;
        this.qtyPrep     = qtyPrep;
        this.qtyAvailable   = qtyAvailable;
    }

    /**
     * @return
     */
    public int getAvailable() {
         return qtyAvailable;
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

}

