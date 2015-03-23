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
package edu.ku.brc.af.ui.forms.formatters;

import java.util.Vector;


/**
 * Creates a formatter that doesn;t validate on length, but it does enforce it. 
 * It validates on a minimum value and a maximum value.
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: May 2, 2008
 *
 */
public class NumberMinMaxFormatter extends UIFieldFormatter
{

    /**
     * Creates a formatter that doesn;t validate on length, but it does enforce it. 
     * It validates on a minimum value and a maximum value.
     * @param dataClass class of the number it handles
     * @param len the maximum lengrth it handles length
     * @param minValue the min value
     * @param maxValue the max value
     */
    public NumberMinMaxFormatter(final Class<?> dataClass,
                           final int      len,
                           final Number   minValue,
                           final Number   maxValue)
    {
        super("Numeric", false, null, FormatterType.numeric, null, dataClass, false, false, null);

        this.minValue = minValue;
        this.maxValue = maxValue;

        UIFieldFormatterField field = new UIFieldFormatterField(UIFieldFormatterField.FieldType.numeric, len, "#", false, false);
        fields = new Vector<UIFieldFormatterField>(1);
        fields.add(field);
    }
}
