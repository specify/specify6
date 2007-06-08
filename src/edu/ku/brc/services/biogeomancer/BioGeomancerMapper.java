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

package edu.ku.brc.services.biogeomancer;


import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.SimpleCircleIcon;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.services.MapGrabber;

/**
 * This was for the SPNHC Demo. This class really needs to be derived from a generic class like LocalityMapper
 *
 * @code_status Unknown (auto-generated)
 *
 * @author rods
 *
 */
public class BioGeomancerMapper
{
    private static final Logger log = Logger.getLogger(BioGeomancerMapper.class);
    protected List<BGMData>     bgmDatas;
    protected BGMData           currentLoc;
    protected List<String>      labels;
    protected List<Point>       markerLocations;
    protected List<Rectangle>   boxLocations;
    protected MapGrabber        mapGrabber;
    
    // the actual bounds of the BioGeomancer data points
    protected double            minLat;
    protected double            maxLat;
    protected double            minLong;
    protected double            maxLong;
    
    // the bounds of the map after including calculating the best size
    protected double            mapMinLat;
    protected double            mapMaxLat;
    protected double            mapMinLong;
    protected double            mapMaxLong;
    protected double            pixelPerLatRatio;
    protected double            pixelPerLongRatio;
    protected Integer           maxMapWidth;
    protected Integer           maxMapHeight;
    protected int               mapWidth;
    protected int               mapHeight;
    protected int               mostRecentPaintedX;
    protected int               mostRecentPaintedY;

    // an icon for the little marker dots
    // this icon will get repainted in each location
    protected SimpleCircleIcon  marker;
    
    protected Color             labelColor;
    
    // the cached information
    protected Icon              mapIcon;
    protected Icon              overlayIcon = null;
    protected boolean           cacheValid;

    public BioGeomancerMapper()
    {
        minLat = -90;
        minLong = -180;
        maxLat = 90;
        maxLong = 180;
        this.bgmDatas = new Vector<BGMData>();

        this.labels = new Vector<String>();
        this.markerLocations = new Vector<Point>();
        this.boxLocations = new Vector<Rectangle>();
        this.mapGrabber = new MapGrabber();
        labelColor = Color.BLACK;
        cacheValid = false;
        marker = new SimpleCircleIcon(8,Color.BLACK);
    }

    public void addBGMDataAndLabel(double px, double py, double lx, double ly, double rx, double ry, String label)
    {
        BGMData bgmData = new BGMData(px, py, lx, ly, rx, ry);
        bgmDatas.add(bgmData);
        recalculateBoundingBox();

        labels.add(label);
        Point iconLoc = determinePixelCoordsOfBGMData(bgmData);
        markerLocations.add(iconLoc);
        Rectangle rect = determinePixelRectOfBGMData(bgmData);
        boxLocations.add(rect);
    }

    /**
     * @param maxMapHeight
     *            The maxMapHeight to set.
     */
    public void setMaxMapHeight(Integer maxMapHeight)
    {
        this.maxMapHeight = maxMapHeight;
    }

    /**
     * @return Returns the maxMapWidth.
     */
    public Integer getMaxMapWidth()
    {
        return maxMapWidth;
    }

    /**
     * @param maxMapWidth
     *            The maxMapWidth to set.
     */
    public void setMaxMapWidth(Integer maxMapWidth)
    {
        this.maxMapWidth = maxMapWidth;
    }

    private Pair<Double, Double> getLatLong(BGMData loc)
    {
        Double lat1 = loc.getLatitude1();
        Double long1 = loc.getLongitude1();
        return new Pair<Double, Double>(lat1,long1);
    }

    protected void recalculateBoundingBox()
    {
        cacheValid = false;
        if( bgmDatas.isEmpty() )
        {
            minLat = -90;
            minLong = -180;
            maxLat = 90;
            maxLong = 180;
            mapMinLat = -90;
            mapMinLong = -180;
            mapMaxLat = 90;
            mapMaxLong = 180;
            return;
        }

        // setup the minimums so they are guaranteed to be changed
        minLong = 180;
        maxLong = -180;
        minLat = 90;
        maxLat = -90;
        for( BGMData loc : bgmDatas )
        {
            Pair<Double, Double> latLong = getLatLong(loc);
            if( latLong.first < minLat )
            {
                minLat = latLong.first;
            }
            if( latLong.second < minLong )
            {
                minLong = latLong.second;
            }
            if( latLong.first > maxLat )
            {
                maxLat = latLong.first;
            }
            if( latLong.second > maxLong )
            {
                maxLong = latLong.second;
            }
        }
        createBoundingBoxBufferRegion();
        
        // XXX
        // TODO: ensure the resulting bounding box is valid by panning left/right/up/down and shrinking
        // to make sure it's within (-90,-180,90,180)
    }

    /**
     * Increases the size of the current bounding box in order to create
     * a bit of a buffer (and ensure the bounding box isn't a single point).
     */
    protected void createBoundingBoxBufferRegion()
    {
        double minDegRange = .1;
        
        double latSpread = maxLat-minLat;
        if( latSpread < minDegRange )
        {
            // expand the range to at least be minDegRange degrees
            double diff = minDegRange - latSpread;
            latSpread = minDegRange;
            mapMinLat = minLat - diff/2;
            mapMaxLat = maxLat + diff/2;
        }
        else
        {
            // just add 5% to each side
            mapMinLat = minLat - (.05*latSpread);
            mapMaxLat = maxLat + (.05*latSpread);
        }
        
        double longSpread = maxLong-minLong;
        if( longSpread < minDegRange )
        {
            // expand the range to at least be minDegRange degrees
            double diff = minDegRange - longSpread;
            longSpread = minDegRange;
            mapMinLong = minLong - diff/2;
            mapMaxLong = maxLong + diff/2;
        }
        else
        {
            // just add 5% to each side
            mapMinLong = minLong - (.05*longSpread);
            mapMaxLong = maxLong + (.05*longSpread);
        }
    }
    
    /**
     * In order to ensure that we grab the largest map allowed, we need to expand either
     * then width or height to fill in the extra space.  So, we figure out which way the
     * map needs to be expanded, then adjust the bounding box to accomplish that.
     */
    protected void expandMapRegionToFillUsableSpace()
    {
        double degToPixelLat = (mapMaxLat - mapMinLat) / maxMapHeight;
        double degToPixelLon = (mapMaxLong - mapMinLong) / maxMapWidth;
        
        // we need a uniform deg/pixel ratio for both lat and long, so find the largest one and use it
        double uniformDegToPixel = Math.max(degToPixelLat, degToPixelLon);
        
        double correctedLatRange = uniformDegToPixel * maxMapHeight;
        double correctedLonRange = uniformDegToPixel * maxMapWidth;
        
        double latRangeDiff = correctedLatRange - (mapMaxLat - mapMinLat);
        double lonRangeDiff = correctedLonRange - (mapMaxLong - mapMinLong);
        
        // apply the corrections evenly
        mapMaxLat  += latRangeDiff / 2;
        mapMinLat  -= latRangeDiff / 2;
        mapMaxLong += lonRangeDiff / 2;
        mapMinLong -= lonRangeDiff / 2;
    }

    protected Image getMapFromService(final String host,
                                      final String defaultPathAndParams,
                                      final String layers,
                                      double minLatArg,
                                      double minLongArg,
                                      double maxLatArg,
                                      double maxLongArg)   throws HttpException, IOException
    {
        log.debug("Asking for map from service with following arguments...");
        log.debug("\tmin lat: " + minLatArg);
        log.debug("\tmin lon: " + minLongArg);
        log.debug("\tmax lat: " + maxLatArg);
        log.debug("\tmax lon: " + maxLongArg);
        log.debug("\twidth:   " + maxMapWidth);
        log.debug("\theight:  " + maxMapHeight);
        
        mapGrabber.setHost(host);
        mapGrabber.setDefaultPathAndParams(defaultPathAndParams);
        mapGrabber.setLayers(layers);
        
        mapGrabber.setMinLat(minLatArg);
        mapGrabber.setMaxLat(maxLatArg);
        mapGrabber.setMinLong(minLongArg);
        mapGrabber.setMaxLong(maxLongArg);

        mapGrabber.setHeight(maxMapHeight);
        mapGrabber.setWidth(maxMapWidth);
        return mapGrabber.getMap();
    }

    protected Point determinePixelCoordsOfBGMData(BGMData loc)
    {
        Pair<Double, Double> latLong = getLatLong(loc);
        double y = latLong.first-mapMinLat;
        double x = latLong.second-mapMinLong;
        y = mapHeight-y*pixelPerLatRatio;
        x = x*pixelPerLongRatio;
        return new Point((int) x,(int) y);
    }

    protected Rectangle determinePixelRectOfBGMData(BGMData loc)
    {
        double y = loc.lx-mapMinLat;
        double x = loc.ly-mapMinLong;
        double y2 = loc.rx-mapMinLat;
        double x2 = loc.ry-mapMinLong;
        y = mapHeight-y*pixelPerLatRatio;
        x = x*pixelPerLongRatio;
        y2 = mapHeight-y2*pixelPerLatRatio;
        x2 = x2*pixelPerLongRatio;
        return new Rectangle((int) x, (int) y, (int)(x2),(int) (y2));
    }

    protected boolean pointIsOnMapIcon(int x, int y)
    {
        if( mostRecentPaintedX>x||mostRecentPaintedX+mapWidth<x )
        {
            return false;
        }
        if( mostRecentPaintedY>y||mostRecentPaintedY+mapHeight<y )
        {
            return false;
        }
        return true;
    }

    public void getMap(final MapperListener callback)
    {
        Thread mapGrabberThread = new Thread("Mapper Grabber")
        {
            @Override
            public void run()
            {
                try
                {
                    Icon map = grabNewMap();
                    
                    if (callback != null)
                    {
                        callback.mapReceived(map);
                    }
                }
                catch( Exception e )
                {
                    if (callback != null)
                    {
                        callback.exceptionOccurred(e);
                    }
                }
            }
        };
        mapGrabberThread.setDaemon(true);
        mapGrabberThread.start();
    }

    protected Icon grabNewMap() throws HttpException, IOException
    {
        if( !cacheValid )
        {
            Image mapImage = getMapFromService("mapus.jpl.nasa.gov",
                    "/wms.cgi?request=GetMap&srs=EPSG:4326&format=image/png&styles=visual",
                    "global_mosaic",
                    mapMinLat, mapMinLong, mapMaxLat, mapMaxLong);

            Image overlayImage = getMapFromService("129.237.201.132",
                    //"/cgi-bin/ogc.cgi/specify?service=WMS&request=GetMap&srs=EPSG:4326&version=1.3.1&format=image/png&transparent=true",
                    "/cgi-bin/mapserv?map=/var/www/maps/specify.map&service=WMS&request=GetMap&srs=EPSG:4326&version=1.3.1&format=image/png&transparent=true",
                    "states,rivers",
                    mapMinLat, mapMinLong, mapMaxLat, mapMaxLong);

            mapIcon     = new ImageIcon(mapImage);
            overlayIcon = new ImageIcon(overlayImage);
            cacheValid  = true;

            mapWidth = mapIcon.getIconWidth();
            mapHeight = mapIcon.getIconHeight();

            double mapLatRange = mapMaxLat-mapMinLat;
            double mapLongRange = mapMaxLong-mapMinLong;

            pixelPerLatRatio = mapHeight / mapLatRange;
            pixelPerLongRatio = mapWidth / mapLongRange;

            for( int i = 0; i<bgmDatas.size(); ++i )
            {
                BGMData loc = bgmDatas.get(i);
                Point iconLoc = determinePixelCoordsOfBGMData(loc);
                markerLocations.set(i,iconLoc);
                Rectangle rect = determinePixelRectOfBGMData(loc);
                boxLocations.set(i,rect);
            }
        }
        Icon icon = new Icon()
        {
            @SuppressWarnings("synthetic-access")
            public void paintIcon(Component c, Graphics g, int x, int y)
            {
                // this helps keep the labels inside the map
                g.setClip(x,y,mapWidth,mapHeight);
                // log the x and y for the MouseMotionListener
                mostRecentPaintedX = x;
                mostRecentPaintedY = y;

                mapIcon.paintIcon(c,g,x,y);
                overlayIcon.paintIcon(c, g, x, y);

                //Point lastLoc = null;
                for( int i = 0; i<bgmDatas.size(); ++i )
                {
                    Point markerLoc = markerLocations.get(i);
                    Rectangle boxLoc = boxLocations.get(i);
                    String label = labels.get(i);

                    if( markerLoc==null )
                    {
                        log.error("A marker location is null");
                        continue;
                    }
                    if( !pointIsOnMapIcon(x+markerLoc.x,y+markerLoc.y) )
                    {
                        log.error("A marker location is off the map");
                        continue;
                    }
                    marker.paintIcon(c,g,markerLoc.x+x,markerLoc.y+y);

                    if (boxLoc != null)
                    {
                        //g.drawRect(boxLoc.x+x, boxLoc.y+x, boxLoc.width+x, boxLoc.height+y);
                    }

                    if( label!=null )
                    {
                        Color origColor = g.getColor();
                        FontMetrics fm = g.getFontMetrics();
                        int length = fm.stringWidth(label);
                        g.setColor(Color.WHITE);
                        g.fillRect(markerLoc.x+x-(length/2),markerLoc.y+y-(fm.getHeight()/2),
                                length,fm.getHeight());
                        g.setColor(labelColor);
                        GraphicsUtils.drawCenteredString(label,g,markerLoc.x+x,markerLoc.y+y);
                        g.setColor(origColor);
                    }

                    //lastLoc = markerLoc;
                }
            }

            public int getIconWidth()
            {
                return mapWidth;
            }

            public int getIconHeight()
            {
                return mapHeight;
            }
        };

        return icon;
    }

    // -----------------------------------------------------------------
    // Inner Class / Interface
    // -----------------------------------------------------------------
    public class BGMData
    {
        public double px;
        public double py;
        public double lx;
        public double ly;
        public double rx;
        public double ry;
        
        public BGMData(double px, double py, double lx, double ly, double rx, double ry)
        {
            super();
            // TODO Auto-generated constructor stub
            this.px = px;
            this.py = py;
            this.lx = lx;
            this.ly = ly;
            this.rx = rx;
            this.ry = ry;
        }
        
        public Double getLatitude1()
        {
            return px;
        }

        public Double getLongitude1()
        {
            return py;
        }
    }
}
