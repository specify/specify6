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

import org.apache.log4j.Logger;

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
    protected static final Logger  log = Logger.getLogger(RawData.class);
            
    private static HashMap<Integer, DataIndexType> map = new HashMap<Integer, DataIndexType>();
    
    // The order matches AnalysisBase
    public enum DataIndexType {
        eCollector_num, eCatalogue_number, eGenus, eSpecies, eSubspecies, eCollector_name, eLocality, 
        eLatitude, eLongitude, eYear, eMonth, eDay, eCountry, eState_province, eCounty, 
        eFamily, eMax_altitude, eMin_altitude, eInstitution_code, eCollection_code, eAuthor, eStartDate,
    }
    
    private static String[] dataColumnNames = {
        "fieldnumber", "catalognumber", "genus1", "species1", "subspecies1", "collectorname", "localityname", 
        "latitude1", "longitude1", "year", "month", "day", "country", "state", "county", 
        "family1", "maxaltitude", "minaltitude", "institutioncode", "collectioncode", "author", "startdate",
    };
    
    private static HashMap<String, DataIndexType> dataColInxHash = new HashMap<String, DataIndexType>();
    
    // Non-Static data members
    private HashMap<DataIndexType, Object>  values = new HashMap<DataIndexType, Object>();

    // Static initialization
    static
    {
        for (DataIndexType dit : DataIndexType.values())
        {
            map.put(dit.ordinal(), dit);
            dataColInxHash.put(dataColumnNames[dit.ordinal()], dit);
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
     * @param colName
     * @return
     */
    public Object getData(final String colName)
    {
        DataIndexType dit = dataColInxHash.get(colName);
        if (dit != null)
        {
            return getData(dit);
        }
        
        log.error("Couldn't find column for name["+colName+"]");
        return null;
    }
    
    /**
     * @param colName
     * @return
     */
    public static Integer getIndex(final String colName)
    {
        DataIndexType dit = dataColInxHash.get(colName);
        if (dit != null)
        {
            return dit.ordinal();
        }  
        return null;
    }

    /**
     * @param dit
     * @return standard column name for the enum
     */
    public static String getColumnName(final DataIndexType dit)
    {
        return dataColumnNames[dit.ordinal()];
    }

    /**
     * @param colName
     * @return
     */
    public static DataIndexType getDataIndexType(final String colName)
    {
        return dataColInxHash.get(colName);
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
