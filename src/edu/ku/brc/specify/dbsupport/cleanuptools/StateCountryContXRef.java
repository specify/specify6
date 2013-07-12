/* Copyright (C) 2013, University of Kansas Center for Research
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 20, 2012
 *
 */
public class StateCountryContXRef
{
    private Connection conn;
    
    private Hashtable<String, String>  continentNameFromCode        = new Hashtable<String, String>();
    private Hashtable<String, Integer> contCodeToIdHash                 = new Hashtable<String, Integer>();
    
    private Hashtable<String, String>  continentCodeFromCountryCode = new Hashtable<String, String>();
    private Hashtable<String, String>  countryNameFromCode          = new Hashtable<String, String>();
    private Hashtable<String, String>  countryCodeFromName          = new Hashtable<String, String>();
    private Hashtable<String, Integer> countryCodeToIdHash          = new Hashtable<String, Integer>();
    
    private Hashtable<String, String>  stateNameFromCode            = new Hashtable<String, String>();
    private Hashtable<String, String>  stateCodeFromName            = new Hashtable<String, String>();
    private Hashtable<String, String>  countryCodeFromStateCode     = new Hashtable<String, String>();

    /**
     * @param conn
     */
    public StateCountryContXRef(Connection conn)
    {
        super();
        this.conn = conn;
    }
    
    /*
     * 
     */
    public boolean build()
    {
        Statement stmt = null;
        ResultSet rs   = null;
        try
        {
            // Continents
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT geonameId, name, ISOCode FROM geoname WHERE fcode = 'CONT'");
            while (rs.next())
            {
                int    id   = rs.getInt(1);
                String name = rs.getString(2);
                String code = rs.getString(3);
                continentNameFromCode.put(code, name);
                contCodeToIdHash.put(code, id);
            }
            rs.close();
            
            // Countries
            String sql = "SELECT gn.geonameId, ci.name, ci.iso_alpha2, continent FROM countryinfo ci INNER JOIN geoname gn ON ci.iso_alpha2 = gn.country WHERE gn.fcode = 'PCLI' ORDER BY ci.name";
            rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                int    id   = rs.getInt(1);
                String name = rs.getString(2);
                String code = rs.getString(3);
                
                countryCodeFromName.put(name, code);
                countryNameFromCode.put(code, name);
                countryCodeToIdHash.put(code, id);
                continentCodeFromCountryCode.put(code, rs.getString(4));
            }
            rs.close();
            
            // States
            sql = "SELECT geonameId, asciiname, latitude, longitude, country, admin1, ISOCode FROM geoname WHERE asciiname IS NOT NULL AND LENGTH(asciiname) > 0 AND fcode = 'ADM1'";
            rs     = stmt.executeQuery(sql);
            while (rs.next())
            {
                String nameStr      = rs.getString(2);
                String countryCode  = rs.getString(5);
                String stateCode    = rs.getString(6);
                String countryState = countryCode + "," + stateCode;
                
                String countryName = countryNameFromCode.get(countryCode);
                stateNameFromCode.put(countryState, nameStr);
                stateCodeFromName.put(countryName + ";" + nameStr, stateCode);
                countryCodeFromStateCode.put(stateCode, countryCode);
            }
            rs.close();
            
            stmt.close();
            return true;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {}
        }
        return false;
    }
    
    /**
     * @param name
     * @return
     */
    public String continentCodeToName(final String name)
    {
        return continentNameFromCode.get(name);
    }
    
    /**
     * @param code
     * @return
     */
    public Integer continentCodeToId(final String code)
    {
        return contCodeToIdHash.get(code);
    }
    
    /**
     * @param code
     * @return
     */
    public String countryCodeToContinentCode(final String code)
    {
        return continentCodeFromCountryCode.get(code);
    }
    
    /**
     * @param code
     * @return
     */
    public String countryCodeToName(final String code)
    {
        return countryNameFromCode.get(code);
    }
    
    /**
     * @param name
     * @return
     */
    public String countryNameToCode(final String name)
    {
        return name != null ? countryCodeFromName.get(name) : null;
    }
    
    /**
     * @param code
     * @return
     */
    public Integer countryCodeToId(final String code)
    {
        return countryCodeToIdHash.get(code);
    }
    
    /**
     * @param code
     * @return
     */
    public String stateCodeToName(final String code)
    {
        return stateNameFromCode.get(code);
    }
    
    /**
     * @param name
     * @return
     */
    public String stateNameToCode(final String name)
    {
        return stateCodeFromName.get(name);
    }
    
    /**
     * @param code
     * @return
     */
    public String stateCodeToCountryCode(final String code)
    {
        return countryCodeFromStateCode.get(code);
    }
}
