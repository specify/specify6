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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.UIRegistry;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Polyline;
//ZZZ import gov.nasa.worldwind.util.GeometryMath;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 15, 2012
 *
 */
public class LocalityGeoBoundsChecker2
{
    public enum LookupType {eFound, eNotFound, eStateDifferent};
    
    private HashMap<String, StateInfo> statesHash          = new HashMap<String, StateInfo>();
    private HashMap<String, StateInfo> statesIdHash        = new HashMap<String, StateInfo>();
    private HashMap<String, StateInfo> codeToStateInfoHash = new HashMap<String, StateInfo>();
    
    private HashMap<String, String>    statesCodeHash   = new HashMap<String, String>();
    private StateInfo                  stateInfoFoundIn = null;
    private ArrayList<StateLookupInfo> items            = new ArrayList<StateLookupInfo>();
    
    private Connection connection;
    /**
     * 
     */
    public LocalityGeoBoundsChecker2(final Connection connection)
    {
        super();
        this.connection = connection;
        
        loadStateCodeHash();
    }
    
    /**
     * 
     */
    private void loadStateCodeHash()
    {
        String sql = "SELECT asciiname,admin1 FROM geoname WHERE fcode = 'ADM1' AND country = 'US'";
        for (Object[] row : BasicSQLUtils.query(sql))
        {
            statesCodeHash.put(row[0].toString(), row[1].toString());
        }
    }
    
    /**
     * 
     */
    public void load()
    {
        File file = new File(XMLHelper.getConfigDirPath("st99_d00a.dat"));
        try
        {
            List<String> lines = (List<String>)FileUtils.readLines(file);
            for (int i=0;i<lines.size();i++)
            {
                String polygonId = StringUtils.remove(StringUtils.deleteWhitespace(lines.get(i++)), '"');
                String stateId   = StringUtils.remove(StringUtils.deleteWhitespace(lines.get(i++)), '"');
                String stateNm   = StringUtils.remove(StringUtils.deleteWhitespace(lines.get(i++)), '"');
                i += 2;
                
                StateInfo si = statesHash.get(stateNm);
                if (si == null)
                {
                    String code = statesCodeHash.get(stateNm);
                    si = new StateInfo(stateNm, code, stateId);
                    statesHash.put(stateNm, si);
                    codeToStateInfoHash.put(code, si);
                }
                statesIdHash.put(polygonId, si);
            }
            
            for (String polygonId : statesIdHash.keySet())
            {
                System.out.println(String.format("%s - %s", polygonId, statesIdHash.get(polygonId).name));
            }
            
            file = new File(XMLHelper.getConfigDirPath("st99_d00.dat"));
            FileInputStream fis = new FileInputStream(file);
            BufferedReader bufRdr = new BufferedReader(new InputStreamReader(fis));
            
            StateInfo           si     = null;
            ArrayList<Position> points = null;
            
            String s;
            while ((s = bufRdr.readLine()) != null) 
            { 
                String[] tokens = StringUtils.split(s, ' ');
                if (tokens.length == 3)
                {
                    //System.out.println(tokens[0]);
                    /*if (points != null)
                    {
                        Polyline polygon = new Polyline(); 
                        polygon.setPositions(points);
                        si.addPolygon(polygon);
                    }*/
                    
                    si     = statesIdHash.get(tokens[0]);
                    points = new ArrayList<Position>();

                    //double lat = Double.valueOf(tokens[1]);
                    //double lon = Double.valueOf(tokens[2]);
                    //points.add(Position.fromDegrees(lat, lon, 0));
                    
                } else if (tokens.length == 2)
                {
                    double lat = Double.valueOf(tokens[1]);
                    double lon = Double.valueOf(tokens[0]);
                    points.add(Position.fromDegrees(lat, lon, 0));
                    
                } else if (si != null)
                {
                    Polyline polygon = new Polyline(); 
                    polygon.setPositions(points);
                    si.addPolygon(polygon);
                    si = null;
                }
            }
            bufRdr.close();
            fis.close();
            
            for (StateInfo stateInfo : statesHash.values())
            {
                System.out.println(String.format("%s - %d", stateInfo.name, stateInfo.polygons.size()));
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * @param stateInfo
     * @param lat
     * @param lon
     * @return
     */
    private boolean isInState(final StateInfo stateInfo, final double lat, final double lon)
    {
        Position pos = Position.fromDegrees(lat, lon, 0.0);
        for (Polyline polygon : stateInfo.polygons)
        {
// ZZZ            
//            if (GeometryMath.isLocationInside(pos, polygon.getPositions()))
//            {
//                return true;
//            }
        }
        return false;
    }
    
    /**
     * @param lat
     * @param lon
     * @param stateCode
     * @return
     */
    public LookupType lookup(final double lat, final double lon, final String stateCode)
    {
        stateInfoFoundIn = null;
        
        System.out.println(String.format("%8.4f, %8.4f  (%s)", lat, lon, stateCode));
        
        if (StringUtils.isNotEmpty(stateCode))
        {
            StateInfo stateInfo = codeToStateInfoHash.get(stateCode);
            if (stateInfo != null)
            {
                if (isInState(stateInfo, lat, lon))
                {
                    stateInfoFoundIn = stateInfo;
                    return LookupType.eFound;
                }
            }
        }
        
        for (StateInfo si : statesHash.values())
        {
            if (stateCode == null || !stateCode.equals(si.code))
            {
                if (isInState(si, lat, lon))
                {
                    stateInfoFoundIn = si;
                    return LookupType.eStateDifferent;
                }
            }
        }
        return LookupType.eNotFound;
    }
    
    /**
     * @return the stateInfoFoundIn
     */
    public StateInfo getStateInfoFoundIn()
    {
        return stateInfoFoundIn;
    }
    
    /**
     * 
     */
    public void processLocalities()
    {
        load();
        
        String sql = "SELECT HighestChildNodeNumber, NodeNumber FROM geography WHERE GeographyCode = 'US' AND GeographyTreeDefID = GEOTREEDEFID";
        sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
        System.out.println(sql);
        
        Object[] rowObjs = BasicSQLUtils.queryForRow(sql);
        if (rowObjs == null)
        {
            UIRegistry.showError("Couldn't locate US geography.");
            return;
        }
        int highNode = (Integer)rowObjs[0];
        int nodeNum  = (Integer)rowObjs[1];
        
        //highNode = 2737;
        //nodeNum  = 1065;
        
        sql = String.format("SELECT l.Latitude1, l.Longitude1, g.GeographyCode, RankID, ParentID, l.LocalityID, g.GeographyID FROM locality l " +
              "INNER JOIN geography g ON l.GeographyID = g.GeographyID " +
              "WHERE Latitude1 IS NOT NULL AND Longitude1 IS NOT NULL AND DisciplineID = DSPLNID AND NodeNumber >= %d AND NodeNumber <= %d " +
              "ORDER BY LocalityID", nodeNum, highNode);
        
        sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
        System.out.println(sql);

        Statement stmt = null;
        try
        {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                double lat       = rs.getDouble(1);
                double lon       = rs.getDouble(2);
                String stateCode = rs.getString(3);
                int    rankId    = rs.getInt(4);
                int    parentId  = rs.getInt(5);
                int    locId     = rs.getInt(6);
                int    geoId     = rs.getInt(7);
                
                while (rankId > 300)
                {
                    sql = "SELECT RankID, GeographyCode, ParentID FROM geography WHERE GeographyID = " + parentId;
                    rowObjs = BasicSQLUtils.queryForRow(sql);
                    if (rowObjs != null)
                    {
                        geoId     = parentId;
                        rankId    = (Integer)rowObjs[0];
                        stateCode = (String)rowObjs[1];
                        parentId  = (Integer)rowObjs[2];
                    } else
                    {
                        break;
                    }
                }
                if (stateCode != null && stateCode.length() == 4)
                {
                    stateCode = stateCode.substring(2, 4);
                }
                LookupType status = lookup(lat, lon, stateCode);
                if (status != LookupType.eFound)
                {
                    items.add(new StateLookupInfo(locId, geoId, stateInfoFoundIn, status));
                }
            }
            rs.close();
            
            //int i = 0;
            for (StateLookupInfo sli : items)
            {
                //System.out.println(String.format("%d - %d %d  %s", i++, sli.localityId, sli.geographyId, sli.status.toString()));
                sql = "SELECT l.LocalityName, g.FullName, l.Latitude1, l.Longitude1 FROM locality l " +
                      "INNER JOIN geography g ON l.GeographyID = g.GeographyID " +
                      "WHERE LocalityID = "+ sli.localityId;
                Object[] row = BasicSQLUtils.queryForRow(sql);
                System.out.println(String.format("%s, %s, %s, %s  (%d, %d)", row[0].toString(), row[1].toString(), row[2].toString(), row[3].toString(), sli.localityId, sli.geographyId));
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                stmt.close();
            } catch (SQLException e) {}
        }
    }
    

    //---------------------------------------------------------------------
    //
    //---------------------------------------------------------------------
    
    class StateLookupInfo
    {
        int       localityId;
        int       geographyId;
        StateInfo stateInfo;
        LookupType status;
        
        /**
         * @param localityId
         * @param geographyId
         * @param stateInfo
         * @param status
         */
        public StateLookupInfo(int localityId, int geographyId, StateInfo stateInfo,
                LookupType status)
        {
            super();
            this.localityId = localityId;
            this.geographyId = geographyId;
            this.stateInfo = stateInfo;
            this.status = status;
        }
        /**
         * @return the localityId
         */
        public int getLocalityId()
        {
            return localityId;
        }
        /**
         * @return the geographyId
         */
        public int getGeographyId()
        {
            return geographyId;
        }
        /**
         * @return the stateInfo
         */
        public StateInfo getStateInfo()
        {
            return stateInfo;
        }
        /**
         * @return the status
         */
        public LookupType getStatus()
        {
            return status;
        }
    }
    
    class StateInfo
    {
        String              name;
        String              id;
        String              code;
        ArrayList<Polyline> polygons       = new ArrayList<Polyline>();
        HashSet<String>     polygonIdsHash = new HashSet<String>();
        
        /**
         * @param code
         */
        public StateInfo(final String name, final String code, final String id)
        {
            super();
            this.name = name;
            this.code = code;
            this.id   = id;
        }
        
        public void addId(final String id)
        {
            polygonIdsHash.add(id);
        }
        
        /*public void addPoint(final double lat, final double lon)
        {
            polygons.add(Position.fromDegrees(lat, lon, 0));
        }*/
        
        public void addPolygon(final Polyline p)
        {
            polygons.add(p);
        }
        
        /**
         * @param lat
         * @param lon
         * @return
         */
        public boolean contains(final double lat, final double lon)
        {
            Position pos = Position.fromDegrees(lat, lon, 0.0);
            for (Polyline p : polygons)
            {
// ZZZ                
//                if (GeometryMath.isLocationInside(pos, p.getPositions()))
//                {
//                    System.out.println("Found: "+ name);
//                    return true;
//                }
            }
            return false;
        }
    }

    /**
     * @param args
     */
    /*public static void main(String[] args)
    {
        LocalityGeoBoundsChecker2 lgbc = new LocalityGeoBoundsChecker2();
        lgbc.load();
        
        // 38.9717� N, 95.2350� W
        double lat = 38.9717;
        double lon = -95.2350;
        lgbc.getState(lat, lon);
        System.out.println("Done.");
    }*/

}
