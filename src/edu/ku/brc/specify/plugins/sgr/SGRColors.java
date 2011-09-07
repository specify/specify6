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
package edu.ku.brc.specify.plugins.sgr;

import java.awt.Color;

/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: May 23, 2011
 *
 */
public class SGRColors
{
    public static Color colorForScore(float score, float maxScore)
    {
        return colorForScore(score, maxScore, null);
    }
   
    public static Color colorForScore(float score, float maxScore, Float fieldContribution)
    {
        float x = (float) (Math.log(score/maxScore + 1) / Math.log(2));
        float h = 130 + 240*(x-1);
        float s = 0.2f;
        if (fieldContribution != null)
            s += 0.3f * Math.log(fieldContribution + 1); 
        return Color.getHSBColor(h/360, (float) Math.min(1.0, s), 0.9f);
    }
}
