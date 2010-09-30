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
package edu.ku.brc.specify.plugins.sgr;

import java.util.HashMap;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 27, 2010
 *
 */
public class RawData
{
    private static HashMap<Integer, DataIndexType> map = new HashMap<Integer, DataIndexType>();
    
    public enum DataIndexType {
        eCollector_num, eInstitution_code, eCollection_code, eCatalogue_number, eAuthor, eFamily, eGenus, eSpecies, eSubspecies, 
        eLatitude, eLongitude, eMax_altitude, eMin_altitude, eCountry, eState_province, eCounty, eCollector_name, eLocality, eYear, eMonth, eDay
    }
    
    private HashMap<DataIndexType, Object>  values = new HashMap<DataIndexType, Object>();

    static
    {
        for (DataIndexType dit : DataIndexType.values())
        {
            map.put(dit.ordinal(), dit);
        }
    }
    
    /**
     * 
     */
    public RawData()
    {
        super();
    };
    
    /**
     * 
     */
    public void clear()
    {
        values.clear();
    }

    /**
     * @param index
     * @param value
     */
    public void setData(final DataIndexType index, final Object value)
    {
        values.put(index, value);
    }
    
    /**
     * @param index
     * @return
     */
    public Object getData(final DataIndexType index)
    {
        return values.get(index);
    }
    
    /**
     * @param index
     * @param value
     */
    public void setData(final int index, final Object value)
    {
        values.put(map.get(index), value);
    }
    
    /**
     * @param index
     * @return
     */
    public Object getData(final int index)
    {
        return values.get(map.get(index));
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (DataIndexType dt : DataIndexType.values())
        {
            if (sb.length() > 0) sb.append(", ");
            sb.append(dt.toString()+" -> "+getData(dt));
        }
        return sb.toString();
    }
}
