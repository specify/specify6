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
package edu.ku.brc.services.mapping;

import edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace;

/**
 * A simple implementation of {@link MapLocationIFace}.
 * 
 * @author jstewart
 * @code_status Beta
 */
public class SimpleMapLocation implements MapLocationIFace
{
    protected Double lat1;
    protected Double long1;
    protected Double lat2;
    protected Double long2;
    
    /**
     * @param lat1
     * @param long1
     * @param lat2
     * @param long2
     */
    public SimpleMapLocation(Double lat1, Double long1, Double lat2, Double long2)
    {
        super();
        this.lat1 = lat1;
        this.long1 = long1;
        this.lat2 = lat2;
        this.long2 = long2;
    }

    public Double getLat1()
    {
        return lat1;
    }

    public Double getLat2()
    {
        return lat2;
    }

    public Double getLong1()
    {
        return long1;
    }

    public Double getLong2()
    {
        return long2;
    }
}
