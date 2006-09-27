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

package edu.ku.brc.specify.extras;


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
import org.jdesktop.animation.timing.Cycle;
import org.jdesktop.animation.timing.Envelope;
import org.jdesktop.animation.timing.TimingController;
import org.jdesktop.animation.timing.TimingTarget;

import edu.ku.brc.specify.tasks.services.MapGrabber;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.SimpleCircleIcon;
import edu.ku.brc.util.Pair;

/**
 * This was for the SPNHC Demo. This class really needs to be derived from a generic class like LocalityMapper
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class BioGeoMancerMapper implements TimingTarget
{
    private static final Logger          log                 = Logger.getLogger(BioGeoMancerMapper.class);
    protected List<BGMData>    bgmDatas;
    protected BGMData          currentLoc;
    protected List<String>      labels;
    protected List<Point>       markerLocations;
    protected List<Rectangle>   boxLocations;
    protected MapGrabber        mapGrabber;
    // the actual bounds of the collecting bgmDatas
    protected double            minLat;
    protected double            maxLat;
    protected double            minLong;
    protected double            maxLong;
    // the bounds of the map after including a 5% buffer region
    protected double            mapMinLat;
    protected double            mapMaxLat;
    protected double            mapMinLong;
    protected double            mapMaxLong;
    protected double            mapLatRange;
    protected double            mapLongRange;
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
    // this icon will be painted on whichever marker is considered current
    // by using a separate Icon for the current one, we can later decide
    // to not only customize the icon color for the current BGMData, but
    // also the entire Icon (using a diff shape, etc)
    protected SimpleCircleIcon  currentLocMarker;
    // some configuration of the image
    protected boolean           showArrows;
    protected boolean           showArrowAnimations = true;
    protected boolean           showLabels;
    protected Color             arrowColor;
    protected Color             labelColor;
    protected boolean           animationInProgress = false;
    protected float             percent;
    protected TimingController  animator;
    protected BGMData          animStartLoc;
    protected BGMData          animEndLoc;
    // the cached information
    protected Icon              mapIcon;
    protected Icon             overlayIcon = null;
    protected boolean           cacheValid;

    public BioGeoMancerMapper()
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
        showArrows = true;
        showLabels = true;
        labelColor = Color.BLACK;
        arrowColor = Color.BLACK;
        cacheValid = false;
        marker = new SimpleCircleIcon(8,Color.BLACK);
        currentLocMarker = new SimpleCircleIcon(8,Color.BLACK);
        // setup the animation Cycle
        int resolution = 0;
        int duration = 1000;
        Cycle cycle = new Cycle(duration,resolution);
        // setup the animation Envelope
        double repeatCount = 1;
        int start = 0;
        Envelope.RepeatBehavior repeatBehavior = Envelope.RepeatBehavior.REVERSE;
        Envelope.EndBehavior endBehavior = Envelope.EndBehavior.HOLD;
        Envelope env = new Envelope(repeatCount,start,repeatBehavior,endBehavior);
        // setup the TimingController (the animation controller)
        animator = new TimingController(cycle,env,this);
    }

    /**
     * @return Returns the animator.
     */
    public TimingController getAnimator()
    {
        return animator;
    }


    public void addBGMDataAndLabel(double px, double py, double lx, double ly, double rx, double ry, String label)
    {
        BGMData bgmData = new BGMData(px, py, lx, ly, rx, ry);
        bgmDatas.add(bgmData);
        labels.add(label);
        //System.out.println("["+loc.getLatitude1()+"]["+label+"]");
        Point iconLoc = determinePixelCoordsOfBGMData(bgmData);
        markerLocations.add(iconLoc);
        Rectangle rect = determinePixelRectOfBGMData(bgmData);
        boxLocations.add(rect);
        if( bgmDatas.size()==1 )
        {
            recalculateBoundingBox();
        }
        // instead of recalculating the bounding box from scratch
        // just factor in how this new data point might change it
        Pair<Double, Double> latLong = getLatLong(bgmData);
        double lat = latLong.first;
        double lon = latLong.second;
        if( lat<minLat )
        {
            minLat = lat;
            cacheValid = false;
        }
        if( lat>maxLat )
        {
            maxLat = lat;
            cacheValid = false;
        }
        if( lon<minLong )
        {
            minLong = lon;
            cacheValid = false;
        }
        if( lon>maxLong )
        {
            maxLong = lon;
            cacheValid = false;
        }
        createBoundingBoxBufferRegion();
    }

    /**
     * @return Returns the arrowColor.
     */
    public Color getArrowColor()
    {
        return arrowColor;
    }

    /**
     * @param arrowColor
     *            The arrowColor to set.
     */
    public void setArrowColor(Color arrowColor)
    {
        this.arrowColor = arrowColor;
    }

    /**
     * @return Returns the dotColor.
     */
    public Color getDotColor()
    {
        return marker.getColor();
    }

    /**
     * @param dotColor
     *            The dotColor to set.
     */
    public void setDotColor(Color dotColor)
    {
        marker.setColor(dotColor);
    }

    /**
     * @return Returns the currentLocColor.
     */
    public Color getCurrentLocColor()
    {
        return currentLocMarker.getColor();
    }

    /**
     * @param currentLocColor
     *            The currentLocColor to set.
     */
    public void setCurrentLocColor(Color currentLocColor)
    {
        currentLocMarker.setColor(currentLocColor);
    }

    /**
     * @return the showArrowAnimations
     */
    public boolean getShowArrowAnimations()
    {
        return showArrowAnimations;
    }

    /**
     * @param showArrowAnimations
     *            the showArrowAnimations to set
     */
    public void setShowArrowAnimations(boolean showArrowAnimations)
    {
        this.showArrowAnimations = showArrowAnimations;
    }

    /**
     * @return Returns the dotSize.
     */
    public int getDotSize()
    {
        return marker.getSize();
    }

    /**
     * @param dotSize
     *            The dotSize to set.
     */
    public void setDotSize(int dotSize)
    {
        marker.setSize(dotSize);
        currentLocMarker.setSize(dotSize);
    }

    /**
     * @return Returns the labelColor.
     */
    public Color getLabelColor()
    {
        return labelColor;
    }

    /**
     * @param labelColor
     *            The labelColor to set.
     */
    public void setLabelColor(Color labelColor)
    {
        this.labelColor = labelColor;
    }

    /**
     * @return Returns the showArrows.
     */
    public boolean getShowArrows()
    {
        return showArrows;
    }

    /**
     * @param showArrows
     *            The showArrows to set.
     */
    public void setShowArrows(boolean showArrows)
    {
        this.showArrows = showArrows;
    }

    /**
     * @return Returns the showLabels.
     */
    public boolean getShowLabels()
    {
        return showLabels;
    }

    /**
     * @param showLabels
     *            The showLabels to set.
     */
    public void setShowLabels(boolean showLabels)
    {
        this.showLabels = showLabels;
    }

    /**
     * @return Returns the maxMapHeight.
     */
    public Integer getMaxMapHeight()
    {
        return maxMapHeight;
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

    /**
     * @return Returns the mapHeight.
     */
    public int getMapHeight()
    {
        return mapHeight;
    }

    /**
     * @return Returns the mapWidth.
     */
    public int getMapWidth()
    {
        return mapWidth;
    }

    public List<Point> getMarkerLocations()
    {
        return markerLocations;
    }

    public void zoom(float percentZoom)
    {
        if( percentZoom==1||percentZoom<=0 )
        {
            // don't waste any time
            return;
        }
        cacheValid = false;
        double longRangeChange = mapLongRange*1/percentZoom;
        double longChange = .5*(mapLongRange-longRangeChange);
        mapMinLong += longChange;
        mapMaxLong -= longChange;
        double latRangeChange = mapLatRange*1/percentZoom;
        double latChange = .5*(mapLatRange-latRangeChange);
        mapMinLat += latChange;
        mapMaxLat -= latChange;
    }

    public void pan(double latChange, double longChange)
    {
        cacheValid = false;
        if( mapMinLat+latChange<-90 )
        {
            latChange = -90-mapMinLat;
        }
        if( mapMaxLat+latChange>90 )
        {
            latChange = 90-mapMaxLat;
        }
        if( mapMinLong+longChange<-180 )
        {
            longChange = -180-mapMinLong;
        }
        if( mapMaxLong+longChange>180 )
        {
            longChange = 180-mapMaxLong;
        }

        mapMinLat += latChange;
        mapMaxLat += latChange;
        mapMinLong += longChange;
        mapMaxLong += longChange;
    }

    protected boolean boxIsValid(double minLat, double minLong, double maxLat, double maxLong)
    {
        if( -90<=minLat&&minLat<maxLat&&maxLat<=90 )
        {
            if( -180<=minLong&&minLong<maxLong&&maxLong<=180 )
            {
                return true;
            }
        }
        return false;
    }

    public void setBoundingBox(double minLat, double minLong, double maxLat, double maxLong)
    {
        if( !boxIsValid(minLat,minLong,maxLat,maxLong) )
        {
            throw new IllegalArgumentException("Arguments define invalid bounding box");
        }

        cacheValid = false;
        mapMinLat = minLat;
        mapMinLong = minLong;
        mapMaxLat = maxLat;
        mapMaxLong = maxLong;
    }

    private Pair<Double, Double> getLatLong(BGMData loc)
    {
        Double lat1 = loc.getLatitude1();
        Double long1 = loc.getLongitude1();
        return new Pair<Double, Double>(lat1,long1);
    }

    @SuppressWarnings("unused")
    private Pair<Double, Double> centerOfBBox(Double lat1, Double lat2, Double long1, Double long2)
    {
        Pair<Double, Double> center = new Pair<Double, Double>();
        center.first = (lat1+lat2)/2;
        center.second = (long1+long2)/2;
        return center;
    }

    protected double getLatLongRatio()
    {
        double longRange = maxLong-minLong;
        double latRange = maxLat-minLat;
        return latRange/longRange;
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
            if( latLong.first<minLat )
            {
                minLat = latLong.first;
            }
            if( latLong.second<minLong )
            {
                minLong = latLong.second;
            }
            if( latLong.first>maxLat )
            {
                maxLat = latLong.first;
            }
            if( latLong.second>maxLong )
            {
                maxLong = latLong.second;
            }
        }
        createBoundingBoxBufferRegion();
    }

    protected void createBoundingBoxBufferRegion()
    {
        // make sure we have a 5% buffer around the edge of the map
        double latSpread = maxLat-minLat;
        if( latSpread==0 )
        {
            latSpread += 5;
        }
        double longSpread = maxLong-minLong;
        if( longSpread==0 )
        {
            longSpread += 5;
        }

        double bufferFactor = .20;
        mapMinLat = Math.max(-90,minLat-latSpread*bufferFactor);
        mapMinLong = Math.max(-180,minLong-longSpread*bufferFactor);
        mapMaxLat = Math.min(90,maxLat+latSpread*bufferFactor);
        mapMaxLong = Math.min(180,maxLong+longSpread*bufferFactor);
    }

    protected Image getMapFromService(final String host,
                                      final String defaultPathAndParams,
                                      final String layers,
                                      double minLat,
                                      double minLong,
                                      double maxLat,
                                      double maxLong)   throws HttpException, IOException
    {

        mapGrabber.setHost(host);
        mapGrabber.setDefaultPathAndParams(defaultPathAndParams);
        mapGrabber.setLayers(layers);

        mapGrabber.setMinLat(minLat);
        mapGrabber.setMaxLat(maxLat);
        mapGrabber.setMinLong(minLong);
        mapGrabber.setMaxLong(maxLong);

        mapGrabber.setMaxHeight(maxMapHeight);
        mapGrabber.setMaxWidth(maxMapWidth);
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

    public Pair<Double, Double> getLatLongForPointOnMapIcon(int x, int y)
    {
        if( !pointIsOnMapIcon(x,y) )
        {
            return null;
        }
        // calculate the latitude
        double lat = -1;
        int relativeY = y-mostRecentPaintedY;
        lat = mapMaxLat-relativeY/pixelPerLatRatio;
        // calculate the longitude
        double lon = -1;
        int relativeX = x-mostRecentPaintedX;
        lon = relativeX/pixelPerLongRatio+mapMinLong;
        return new Pair<Double, Double>(lat,lon);
    }

    public void getMap(final MapperListener callback)
    {
        Thread mapGrabberThread = new Thread("Mapper Grabber")
        {
            public void run()
            {
                try
                {
                    Icon map = grabNewMap();
                    callback.mapReceived(map);
                }
                catch( Exception e )
                {
                    callback.exceptionOccurred(e);
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

            Image overlayImage = getMapFromService("129.237.201.104",
                    "/cgi-bin/ogc.cgi/specify?service=WMS&request=GetMap&srs=EPSG:4326&version=1.3.1&format=image/png&transparent=true",
                    "states,rivers",
                    mapMinLat, mapMinLong, mapMaxLat, mapMaxLong);

            mapIcon     = new ImageIcon(mapImage);
            overlayIcon = new ImageIcon(overlayImage);
            cacheValid  = true;

            mapWidth = mapIcon.getIconWidth();
            mapHeight = mapIcon.getIconHeight();

            mapLatRange = mapMaxLat-mapMinLat;
            mapLongRange = mapMaxLong-mapMinLong;

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

    public void begin()
    {
        this.animationInProgress = true;
    }

    public void end()
    {
        this.animationInProgress = false;
    }

    public void timingEvent(long arg0, long arg1, float percentDone)
    {
        this.percent = percentDone;
    }

    // -----------------------------------------------------------------
    // Inner Class / Interface
    // -----------------------------------------------------------------
    public interface MapperListener
    {
        public void mapReceived(Icon map);

        public void exceptionOccurred(Exception e);
    }

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
