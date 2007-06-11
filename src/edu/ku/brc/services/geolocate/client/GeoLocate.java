/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.services.geolocate.client;

import java.util.List;

import edu.ku.brc.services.mapping.LocalityMapper;
import edu.ku.brc.services.mapping.SimpleMapLocation;
import edu.ku.brc.services.mapping.LocalityMapper.MapperListener;

public class GeoLocate
{
    protected static final int MAP_MAX_WIDTH  = 400;
    protected static final int MAP_MAX_HEIGHT = 250;

    public static GeorefResultSet getGeoLocateResults(String country, String state, String county, String localityString)
    {
        // Call Web Service Operation
        Geolocatesvc service = new Geolocatesvc();
        GeolocatesvcSoap port = service.getGeolocatesvcSoap();

        // initialize parameters
        boolean hwyX = false;
        boolean findWaterbody = false;
        
        // process result here
        GeorefResultSet resultSet = port.georef2(country, state, county, localityString, hwyX, findWaterbody);
        
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
