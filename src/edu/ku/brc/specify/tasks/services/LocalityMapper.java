package edu.ku.brc.specify.tasks.services;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
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
import org.jdesktop.animation.timing.Cycle;
import org.jdesktop.animation.timing.Envelope;
import org.jdesktop.animation.timing.TimingController;
import org.jdesktop.animation.timing.TimingTarget;

import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.ui.GraphicsUtils;
import edu.ku.brc.specify.ui.SimpleCircleIcon;
import edu.ku.brc.util.Pair;

public class LocalityMapper implements TimingTarget
{
	private static Log			log					= LogFactory.getLog(LocalityMapper.class);
	protected List<Locality>	localities;
	protected Locality			currentLoc;
	protected List<String>		labels;
	protected List<Point>		markerLocations;
	protected MapGrabber		mapGrabber;
	// the actual bounds of the collecting localities
	protected double			minLat;
	protected double			maxLat;
	protected double			minLong;
	protected double			maxLong;
	// the bounds of the map after including a 5% buffer region
	protected double			mapMinLat;
	protected double			mapMaxLat;
	protected double			mapMinLong;
	protected double			mapMaxLong;
	protected double			mapLatRange;
	protected double			mapLongRange;
	protected double			pixelPerLatRatio;
	protected double			pixelPerLongRatio;
	protected Integer			maxMapWidth;
	protected Integer			maxMapHeight;
	protected int				mapWidth;
	protected int				mapHeight;
	protected int				mostRecentPaintedX;
	protected int				mostRecentPaintedY;

	// an icon for the little marker dots
	// this icon will get repainted in each location
	protected SimpleCircleIcon	marker;
	// this icon will be painted on whichever marker is considered current
	// by using a separate Icon for the current one, we can later decide
	// to not only customize the icon color for the current locality, but
	// also the entire Icon (using a diff shape, etc)
	protected SimpleCircleIcon	currentLocMarker;
	// some configuration of the image
	protected boolean			showArrows;
	protected boolean			showArrowAnimations	= true;
	protected boolean			showLabels;
	protected Color				arrowColor;
	protected Color				labelColor;
	protected boolean			animationInProgress	= false;
	protected float				percent;
	protected TimingController	animator;
	protected Locality			animStartLoc;
	protected Locality			animEndLoc;
	// the cached information
	protected Icon				mapIcon;
	protected boolean			cacheValid;

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
		currentLocMarker = new SimpleCircleIcon(8,Color.BLACK);
		
		// setup the animation Cycle
		int resolution = 0;
		int duration = 750;
		Cycle cycle = new Cycle(duration,resolution);
		
		// setup the animation Envelope
		double repeatCount = 1;
		int start = 0;
		Envelope.RepeatBehavior repeatBehavior = Envelope.RepeatBehavior.REVERSE;
		Envelope.EndBehavior endBehavior = Envelope.EndBehavior.HOLD;
		Envelope env = new Envelope(repeatCount,start,repeatBehavior,endBehavior);
		
		// setup the TimingController (the animation controller)
		animator = new TimingController(cycle,env,this);
		animator.setAcceleration(0.45f);
		animator.setDeceleration(0.45f);
	}

	public LocalityMapper(List<Locality> localities)
	{
		this();
		this.localities.addAll(localities);
		for( int i = 0; i<localities.size(); ++i )
		{
			labels.add(null);
		}
		for( int i = 0; i<localities.size(); ++i )
		{
			markerLocations.add(null);
		}
		recalculateBoundingBox();
	}

	public LocalityMapper(List<Locality> localities, List<String> labels)
	{
		this();
		this.localities.addAll(localities);
		this.labels.addAll(labels);
		for( int i = 0; i<localities.size(); ++i )
		{
			markerLocations.add(null);
		}
		recalculateBoundingBox();
	}

	/**
	 * @return Returns the animator.
	 */
	public TimingController getAnimator()
	{
		return animator;
	}

	/**
	 * @return Returns the currentLoc.
	 */
	public Locality getCurrentLoc()
	{
		return currentLoc;
	}

	/**
	 * @param currentLoc
	 *            The currentLoc to set.
	 */
	public void setCurrentLoc(Locality currentLoc)
	{
		if( this.showArrowAnimations )
		{
			this.animStartLoc = this.currentLoc;
			this.animEndLoc = currentLoc;

			// normalize the arrow speed by calculating the appropriate cycle duration
			// find the longest distance an arrow might have to cover
			double mapDiagDist = Math.sqrt(Math.pow(mapWidth,2)+Math.pow(mapHeight,2));
			
			// set the arrow speed to cover the longest possible route in 2 seconds
			// dist unit is pixels/millisec
			double arrowSpeed = mapDiagDist / 2000;

			// calculate the length of the arrow to animate
			int startIndex = localities.indexOf(animStartLoc);
			int endIndex = localities.indexOf(animEndLoc);
			if( startIndex!=-1&&endIndex!=-1 )
			{
				Point startPoint = markerLocations.get(startIndex);
				Point endPoint = markerLocations.get(endIndex);
				double arrowLength = GraphicsUtils.distance(startPoint, endPoint);
				int duration = (int)(arrowLength/arrowSpeed);
				animator.getCycle().setDuration(duration);
				
				// normalize the acceleration to be 0->full_speed in 500 ms
				// deceleration is the same (full_speed->0 in 500 ms)
				if( duration <= 1000 )
				{
					animator.setAcceleration(0.5f);
					animator.setDeceleration(0.5f);
				}
				else
				{
					float acc = 500/duration;
					animator.setAcceleration(acc);
					animator.setDeceleration(acc);
				}
			}
			
			animator.start();
		}
		this.currentLoc = currentLoc;
	}

	public void addLocalityAndLabel(Locality loc, String label)
	{
		localities.add(loc);
		labels.add(label);
		System.out.println("["+loc.getLatitude1()+"]["+label+"]");
		Point iconLoc = determinePixelCoordsOfLocality(loc);
		markerLocations.add(iconLoc);
		if( localities.size()==1 )
		{
			recalculateBoundingBox();
		}
		// instead of recalculating the bounding box from scratch
		// just factor in how this new data point might change it
		Pair<Double, Double> latLong = getLatLong(loc);
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

	public void removeLocalityAndLabel(Locality loc)
	{
		int index = localities.indexOf(loc);
		localities.remove(index);
		labels.remove(index);
		markerLocations.remove(index);

		Pair<Double, Double> latLong = getLatLong(loc);
		double lat = latLong.first;
		double lon = latLong.second;
		if( minLat==lat||maxLat==lat||minLong==lon||maxLong==lon )
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

	private Pair<Double, Double> getLatLong(Locality loc)
	{
		Double lat1 = loc.getLatitude1();
		Double lat2 = loc.getLatitude2();
		Double long1 = loc.getLongitude1();
		Double long2 = loc.getLongitude2();
		if( lat2!=null&&long2!=null )
		{
			return centerOfBBox(lat1,lat2,long1,long2);
		}
		return new Pair<Double, Double>(lat1,long1);
	}

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
		if( localities.isEmpty() )
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
		for( Locality loc : localities )
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

		double bufferFactor = .05;
		mapMinLat = Math.max(-90,minLat-latSpread*bufferFactor);
		mapMinLong = Math.max(-180,minLong-longSpread*bufferFactor);
		mapMaxLat = Math.min(90,maxLat+latSpread*bufferFactor);
		mapMaxLong = Math.min(180,maxLong+longSpread*bufferFactor);
	}

	protected Image getMapFromService(double minLat, double minLong, double maxLat, double maxLong)	throws HttpException,
																									IOException
	{
		mapGrabber.setMinLat(minLat);
		mapGrabber.setMaxLat(maxLat);
		mapGrabber.setMinLong(minLong);
		mapGrabber.setMaxLong(maxLong);

		mapGrabber.setMaxHeight(maxMapHeight);
		mapGrabber.setMaxWidth(maxMapWidth);
		return mapGrabber.getMap();
	}

	protected Point determinePixelCoordsOfLocality(Locality loc)
	{
		Pair<Double, Double> latLong = getLatLong(loc);
		double y = latLong.first-mapMinLat;
		double x = latLong.second-mapMinLong;
		y = mapHeight-y*pixelPerLatRatio;
		x = x*pixelPerLongRatio;
		return new Point((int) x,(int) y);
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
			Image mapImage = getMapFromService(mapMinLat,mapMinLong,mapMaxLat,mapMaxLong);
			// Image mapImage = new ImageIcon("/home/rods/map.png").getImage();
			mapIcon = new ImageIcon(mapImage);
			cacheValid = true;

			mapWidth = mapIcon.getIconWidth();
			mapHeight = mapIcon.getIconHeight();

			mapLatRange = mapMaxLat-mapMinLat;
			mapLongRange = mapMaxLong-mapMinLong;

			pixelPerLatRatio = (double) mapHeight/(double) mapLatRange;
			pixelPerLongRatio = (double) mapWidth/(double) mapLongRange;

			for( int i = 0; i<localities.size(); ++i )
			{
				Locality loc = localities.get(i);
				Point iconLoc = determinePixelCoordsOfLocality(loc);
				markerLocations.set(i,iconLoc);
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
				Point currentLocPoint = null;
				if( currentLoc!=null )
				{
					currentLocPoint = determinePixelCoordsOfLocality(currentLoc);
				}

				mapIcon.paintIcon(c,g,x,y);
				Point lastLoc = null;
				for( int i = 0; i<localities.size(); ++i )
				{
					Point markerLoc = markerLocations.get(i);
					String label = labels.get(i);
					boolean current = (currentLoc!=null)&&markerLoc.equals(currentLocPoint);

					if( markerLoc==null )
					{
						log.info("A marker location is null");
						continue;
					}
					if( !pointIsOnMapIcon(x+markerLoc.x,y+markerLoc.y) )
					{
						log.info("A marker location is off the map");
						continue;
					}
					// TODO: draw an arrow from lastLoc to iconLoc
					if( showArrows && lastLoc!=null )
					{
						int x1 = x+lastLoc.x;
						int y1 = y+lastLoc.y;
						int x2 = x+markerLoc.x;
						int y2 = y+markerLoc.y;
						Color origColor = g.getColor();
						if( current && !animationInProgress )
						{
							g.setColor(getCurrentLocColor());
						}
						else
						{
							g.setColor(arrowColor);
						}
						GraphicsUtils.drawArrow((Graphics2D) g,x1,y1,x2,y2,2,2);
						g.setColor(origColor);
					}
					if( current )
					{
						currentLocMarker.paintIcon(c,g,markerLoc.x+x,markerLoc.y+y);
					}
					else
					{
						marker.paintIcon(c,g,markerLoc.x+x,markerLoc.y+y);
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

					lastLoc = markerLoc;
				}
				if( showArrowAnimations&&animationInProgress )
				{
					int startIndex = localities.indexOf(animStartLoc);
					int endIndex = localities.indexOf(animEndLoc);
					if( startIndex!=-1&&endIndex!=-1 )
					{
						Point startPoint = markerLocations.get(startIndex);
						Point endPoint = markerLocations.get(endIndex);
						Point arrowEnd = GraphicsUtils.getPointAlongLine(startPoint,endPoint,
								percent);
						Color orig = g.getColor();
						g.setColor(getCurrentLocColor());
						GraphicsUtils.drawArrow(g,startPoint.x+x,startPoint.y+y,arrowEnd.x+x,arrowEnd.y+y,
								5,3);
						g.setColor(orig);
					}
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
		this.percent = 0;
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
}
