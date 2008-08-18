/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.af.ui.forms.formatters;

import java.util.ArrayList;


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
        fields = new ArrayList<UIFieldFormatterField>(1);
        fields.add(field);
    }
}