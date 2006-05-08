package edu.ku.brc.specify.tasks.services;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.ui.GraphicsUtils;
import edu.ku.brc.specify.ui.SimpleCircleIcon;
import edu.ku.brc.util.Pair;

public class LocalityMapper
{
	private static Log log = LogFactory.getLog(LocalityMapper.class);
	
	protected List<Locality> localities;
	protected List<String> labels;
	protected List<Point> markerLocations;
	protected MapGrabber mapGrabber;
	
	// the actual bounds of the collecting localities
	protected double minLat;
	protected double maxLat;
	protected double minLong;
	protected double maxLong;
	
	// the bounds of the map after including a 5% buffer region
	protected double mapMinLat;
	protected double mapMaxLat;
	protected double mapMinLong;
	protected double mapMaxLong;
	
	protected double mapLatRange;
	protected double mapLongRange;
	
	protected double pixelPerLatRatio;
	protected double pixelPerLongRatio;
	
	protected Integer mapWidth;
	protected Integer mapHeight;
	
	protected int mostRecentPaintedX;
	protected int mostRecentPaintedY;

	// an icon for the little marker dots
	// this icon will get repainted in each location
	protected SimpleCircleIcon marker;
	
	// some configuration of the image
	protected boolean showArrows;
	protected boolean showLabels;
	protected Color arrowColor;
	protected Color labelColor;
	
	// the cached information
	protected Icon mapIcon;
	protected boolean cacheValid;
	
	protected String exceptionText;
	
	public LocalityMapper()
	{
		minLat = -90;
		minLong = -180;
		maxLat = 90;
		maxLong = 180;
		
		this.localities = new Vector<Locality>();
		this.labels = new Vector<String>();
		this.markerLocations = new Vector<Point>();
		this.mapGrabber = new MapGrabber();
		
		showArrows = true;
		showLabels = true;
		
		labelColor = Color.BLACK;
		arrowColor = Color.BLACK;
		
		cacheValid = false;
		
		marker = new SimpleCircleIcon(8,Color.BLACK);
	}
	
	public LocalityMapper( List<Locality> localities )
	{
		this();
		this.localities.addAll(localities);
		for( int i = 0; i < localities.size(); ++i )
		{
			labels.add(null);
		}
		for( int i = 0; i < localities.size(); ++i )
		{
			markerLocations.add(null);
		}
		recalculateBoundingBox();
	}
	
	public LocalityMapper( List<Locality> localities, List<String> labels )
	{
		this();
		this.localities.addAll(localities);
		this.labels.addAll(labels);
		for( int i = 0; i < localities.size(); ++i )
		{
			markerLocations.add(null);
		}
		recalculateBoundingBox();
	}
	
	public void addLocalityAndLabel( Locality loc, String label )
	{
		localities.add(loc);
		labels.add(label);
		System.out.println("["+loc.getLatitude1()+"]["+label+"]");
		
		Point iconLoc = determinePixelCoordsOfLocality(loc);
		markerLocations.add(iconLoc);
		
		if( localities.size() == 1 )
		{
			recalculateBoundingBox();
		}
		
		// instead of recalculating the bounding box from scratch
		// just factor in how this new data point might change it
		Pair<Double,Double> latLong = getLatLong(loc);
		double lat = latLong.first;
		double lon = latLong.second;
		
		if( lat < minLat )
		{
			minLat = lat;
			cacheValid = false;
		}
		if( lat > maxLat )
		{
			maxLat = lat;
			cacheValid = false;
		}
		if( lon < minLong )
		{
			minLong = lon;
			cacheValid = false;
		}
		if( lon > maxLong )
		{
			maxLong = lon;
			cacheValid = false;
		}
		
		createBoundingBoxBufferRegion();
	}

	public void removeLocalityAndLabel( Locality loc )
	{
		int index = localities.indexOf(loc);
		localities.remove(index);
		labels.remove(index);
		markerLocations.remove(index);

		Pair<Double,Double> latLong = getLatLong(loc);
		double lat = latLong.first;
		double lon = latLong.second;
		
		if( minLat == lat || maxLat == lat || minLong == lon || maxLong == lon )
		{
			recalculateBoundingBox();
		}
	}
	
	/**
	 * @return Returns the arrowColor.
	 */
	public Color getArrowColor()
	{
		return arrowColor;
	}

	/**
	 * @param arrowColor The arrowColor to set.
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
	 * @param dotColor The dotColor to set.
	 */
	public void setDotColor(Color dotColor)
	{
		marker.setColor(dotColor);
	}

	/**
	 * @return Returns the dotSize.
	 */
	public int getDotSize()
	{
		return marker.getSize();
	}

	/**
	 * @param dotSize The dotSize to set.
	 */
	public void setDotSize(int dotSize)
	{
		marker.setSize(dotSize);
	}

	/**
	 * @return Returns the labelColor.
	 */
	public Color getLabelColor()
	{
		return labelColor;
	}

	/**
	 * @param labelColor The labelColor to set.
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
	 * @param showArrows The showArrows to set.
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
	 * @param showLabels The showLabels to set.
	 */
	public void setShowLabels(boolean showLabels)
	{
		this.showLabels = showLabels;
	}

	public Integer getMapHeight()
	{
		return mapHeight;
	}
	
	public void setPreferredMapHeight(Integer mapHeight)
	{
		this.mapHeight = mapHeight;
	}

	public Integer getMapWidth()
	{
		return mapWidth;
	}

	public void setPreferredMapWidth(Integer mapWidth)
	{
		this.mapWidth = mapWidth;
	}

	public List<Point> getMarkerLocations() 
	{
		return markerLocations;
	}

	private Pair<Double, Double> getLatLong(Locality loc)
	{
		Double lat1 = loc.getLatitude1();
		Double lat2 = loc.getLatitude2();
		Double long1 = loc.getLongitude1();
		Double long2 = loc.getLongitude2();
		
		if( lat2 != null && long2 != null )
		{
			return centerOfBBox(lat1,lat2,long1,long2);
		}
		
		return new Pair<Double,Double>(lat1,long1);
	}
	
	private Pair<Double,Double> centerOfBBox(Double lat1, Double lat2, Double long1, Double long2)
	{
		Pair<Double,Double> center = new Pair<Double,Double>();
		center.first = (lat1+lat2)/2;
		center.second = (long1+long2)/2;
		return center;
	}

	protected double getLatLongRatio()
	{
		double longRange = maxLong - minLong;
		double latRange = maxLat - minLat;
		return latRange/longRange;
	}
	
	protected void recalculateBoundingBox()
	{
		cacheValid = false;
		
		if( localities.isEmpty() )
		{
			minLat = -90;
			minLong = -180;
			maxLat = 90;
			maxLong = 180;
			return;
		}

		// setup the minimums so they are guaranteed to be changed
		minLong = 180;
		maxLong = -180;
		minLat  = 90;
		maxLat  = -90;
		
		for( Locality loc: localities )
		{
			Pair<Double,Double> latLong = getLatLong(loc);
			if(latLong.first < minLat)
			{
				minLat = latLong.first;
			}
			if(latLong.second < minLong)
			{
				minLong = latLong.second;
			}
			if(latLong.first > maxLat)
			{
				maxLat = latLong.first;
			}
			if(latLong.second > maxLong)
			{
				maxLong = latLong.second;
			}
		}
		
		createBoundingBoxBufferRegion();
	}
	
	protected void createBoundingBoxBufferRegion()
	{
		// make sure we have a 5% buffer around the edge of the map
		double latSpread = maxLat - minLat;
		if( latSpread == 0 )
		{
			latSpread += 10;
		}
		double longSpread = maxLong - minLong;
		if( longSpread == 0 )
		{
			longSpread += 10;
		}

		double bufferFactor = .05;
		
		mapMinLat = Math.max(-90, minLat - latSpread*bufferFactor);
		mapMinLong = Math.max(-180, minLong - longSpread*bufferFactor);
		mapMaxLat = Math.min(90, maxLat + latSpread*bufferFactor);
		mapMaxLong = Math.min(180, maxLong + longSpread*bufferFactor);
	}
	
	protected Image getMapFromService(double minLat, double minLong,
			double maxLat, double maxLong) throws HttpException, IOException
	{
		mapGrabber.setMinLat(minLat);
		mapGrabber.setMaxLat(maxLat);
		mapGrabber.setMinLong(minLong);
		mapGrabber.setMaxLong(maxLong);

		mapGrabber.setPreferredHeight(mapHeight);
		mapGrabber.setPreferredWidth(mapWidth);
		
		return mapGrabber.getMap();
	}

	protected Point determinePixelCoordsOfLocality( Locality loc )
	{
		Pair<Double,Double> latLong = getLatLong(loc);
		double y = latLong.first - mapMinLat;
		double x = latLong.second - mapMinLong;
		y = mapHeight - y * pixelPerLatRatio;
		x = x * pixelPerLongRatio;
		
		return new Point((int)x,(int)y);
	}
	
	protected boolean pointIsOnMapIcon(int x, int y)
	{
		if( mostRecentPaintedX > x || mostRecentPaintedX + mapWidth < x )
		{
			return false;
		}
		
		if( mostRecentPaintedY > y || mostRecentPaintedY + mapHeight < y )
		{
			return false;
		}
		
		return true;
	}
	
	public Pair<Double,Double> getLatLongForPointOnMapIcon(int x, int y)
	{
		if( !pointIsOnMapIcon(x, y) )
		{
			return null;
		}
		
		// calculate the latitude
		double lat=-1;
		int relativeY = y - mostRecentPaintedY;
		lat = mapMaxLat - relativeY / pixelPerLatRatio;
		
		// calculate the longitude
		double lon=-1;
		int relativeX = x - mostRecentPaintedX;
		lon = relativeX / pixelPerLongRatio + mapMinLong;
		
		return new Pair<Double,Double>(lat,lon);
	}
	
	public Icon getMap() throws HttpException, IOException
	{
		if( !cacheValid )
		{
			Image mapImage = getMapFromService(mapMinLat, mapMinLong, mapMaxLat, mapMaxLong);
			//Image mapImage = new ImageIcon("/home/rods/map.png").getImage();
			mapIcon = new ImageIcon(mapImage);
			cacheValid = true;

			mapWidth = mapIcon.getIconWidth();
			mapHeight = mapIcon.getIconHeight();

			mapLatRange = mapMaxLat - mapMinLat;
			mapLongRange = mapMaxLong - mapMinLong;

			pixelPerLatRatio = (double)mapHeight / (double)mapLatRange;
			pixelPerLongRatio = (double)mapWidth / (double)mapLongRange;

			for( int i = 0; i < localities.size(); ++i )
			{
				Locality loc = localities.get(i);
				Point iconLoc = determinePixelCoordsOfLocality(loc);
				markerLocations.set(i, iconLoc);
			}
		}
		
		Icon icon = new Icon()
		{
			public void paintIcon(Component c, Graphics g, int x, int y)
			{
				// log the x and y for the MouseMotionListener
				mostRecentPaintedX = x;
				mostRecentPaintedY = y;

				mapIcon.paintIcon(c, g, x, y);
				Point lastLoc = null;
				for( int i = 0; i < localities.size(); ++i )
				{
					Point markerLoc = markerLocations.get(i);
					String label = labels.get(i);

					if( markerLoc == null )
					{
						log.info("A marker location is null");
						continue;
					}
					if(!pointIsOnMapIcon(x+markerLoc.x, y+markerLoc.y))
					{
						log.info("A marker location is off the map");
						continue;
					}
					
					// TODO: draw an arrow from lastLoc to iconLoc
					if( showArrows && lastLoc != null )
					{
						int x1 = x + lastLoc.x;
						int y1 = y + lastLoc.y;
						int x2 = x + markerLoc.x;
						int y2 = y + markerLoc.y;
						Color origColor = g.getColor();
						g.setColor(arrowColor);
						GraphicsUtils.drawArrow((Graphics2D)g, x1, y1, x2, y2, 2);
						g.setColor(origColor);
					}
					
					marker.paintIcon(c, g, markerLoc.x+x, markerLoc.y+y);
					if( label != null )
					{
						Color origColor = g.getColor();
						g.setColor(labelColor);
						GraphicsUtils.drawCenteredString(label, g, markerLoc.x+x, markerLoc.y+y);
						g.setColor(origColor);
					}

					lastLoc = markerLoc;
				}
				
				if( exceptionText != null )
				{
					Color origColor = g.getColor();
					g.setColor(labelColor);
					GraphicsUtils.drawCenteredString(exceptionText, g, x+mapWidth/2, y+mapHeight/2);
					g.setColor(origColor);			
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
		
		// put the decorations on it
		return icon;
	}
}
