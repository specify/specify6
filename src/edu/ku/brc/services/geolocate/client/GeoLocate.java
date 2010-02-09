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
package edu.ku.brc.services.geolocate.client;

import java.util.List;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.services.mapping.LocalityMapper;
import edu.ku.brc.services.mapping.SimpleMapLocation;
import edu.ku.brc.services.mapping.LocalityMapper.MapperListener;

public class GeoLocate
{
    private static final String GL_HYWX    = "GEOLocate.HYWX";
    private static final String GL_WTRBODY = "GEOLocate.WATERBODY";

    protected static final int MAP_MAX_WIDTH  = 400;
    protected static final int MAP_MAX_HEIGHT = 250;

    public static GeorefResultSet getGeoLocateResults(String country, String state, String county, String localityString)
    {
        // Call Web Service Operation
        Geolocatesvc service = new Geolocatesvc();
        GeolocatesvcSoap port = service.getGeolocatesvcSoap();

        // initialize parameters
        boolean hwyX          = AppPreferences.getRemote().getBoolean(GL_HYWX, false);
        boolean findWaterbody = AppPreferences.getRemote().getBoolean(GL_WTRBODY, false);
        
        // process result here
        GeorefResultSet resultSet = port.georef2(country == null ? "" : country, 
                                                 state   == null ? "" : state, 
                                                 county  == null ? "" : county, 
                                                 localityString, hwyX, findWaterbody);
        
        return resultSet;
    }
    
    /**
     * Grabs a map of the given a list of {@link GeographicPoint}s.
     * 
     * @param points a list of {@link GeographicPoint}s to map
     * @param callback the class to notify after the map grabbing is complete
     */
    public static void getMapOfGeographicPoints(List<GeorefResult> points, MapperListener callback)
    {
        LocalityMapper mapper = new LocalityMapper();
        mapper.setShowArrows(false);
        mapper.setMaxMapWidth(MAP_MAX_WIDTH);
        mapper.setMaxMapHeight(MAP_MAX_HEIGHT);

        for (int index = 0; index < points.size(); ++index)
        {
            GeographicPoint point = points.get(index).getWGS84Coordinate();
            
            double lon = point.getLongitude();
            double lat = point.getLatitude();
            
            // create a Locality record to pass to the mapper
            SimpleMapLocation tmpLoc = new SimpleMapLocation(lat,lon,null,null);
            
            mapper.addLocationAndLabel(tmpLoc, Integer.toString(index+1));
        }
        
        // get the map, notifying the callback when done
        mapper.getMap(callback);
    }
}
