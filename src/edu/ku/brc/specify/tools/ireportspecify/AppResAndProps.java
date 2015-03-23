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
package edu.ku.brc.specify.tools.ireportspecify;

import edu.ku.brc.af.core.AppResourceIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Cheap class for tracking AppResource and "other stuff" associated with SpReports
 */
public class AppResAndProps
{
    private final AppResourceIFace appRes;
    private final Object repeats;
    /**
     * @param appRes
     * @param repeats
     */
    public AppResAndProps(AppResourceIFace appRes, Object repeats)
    {
        super();
        this.appRes = appRes;
        this.repeats = repeats;
    }
    /**
     * @return the appRes
     */
    public AppResourceIFace getAppRes()
    {
        return appRes;
    }
    /**
     * @return the repeats
     */
    public Object getRepeats()
    {
        return repeats;
    }
}
