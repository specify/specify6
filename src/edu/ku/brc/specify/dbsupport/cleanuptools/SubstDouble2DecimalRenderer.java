/* Copyright (C) 2015, University of Kansas Center for Research
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

import java.math.RoundingMode;
import java.text.NumberFormat;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 13, 2012
 *
 */
public class SubstDouble2DecimalRenderer extends DefaultTableCellRenderer
{
    private Number       numVal;
    private NumberFormat numFmt;
    private String       zeroes = ".";

    public SubstDouble2DecimalRenderer(final int precision)
    {
        super();
        init(precision);
    }

    public SubstDouble2DecimalRenderer()
    {
        super();
        init(2);
    }

    /**
     * @param precision
     */
    private void init(final int precision)
    {
        setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        numFmt = NumberFormat.getNumberInstance();
        numFmt.setMinimumFractionDigits(precision);
        numFmt.setMaximumFractionDigits(precision);
        numFmt.setRoundingMode(RoundingMode.HALF_UP);
        for (int i = 0; i < precision; i++) zeroes += '0';
    }
    
    /**
     * @param val
     * @return
     */
    public Object formatValue(final Object val)
    {
        Object value = val;
        
        if ((value != null) && (value instanceof Number))
        {
            numVal = (Number) value;
            String str = numFmt.format(numVal.doubleValue());
            if (str.endsWith(zeroes))
            {
                str = str.substring(0, str.length() - zeroes.length());
            }
            value = str;
        } else
        {
            System.out.println("Not Num:"+(val != null ? val.getClass().getSimpleName() : "null"));
        }
        return value;
    }

    @Override
    public void setValue(final Object val)
    {
        super.setValue(formatValue(val));
    }
}
