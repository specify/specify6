package edu.ku.brc.specify.tasks.services;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
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

import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.SimpleCircleIcon;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.services.MapGrabber;

/**
 * Maps a grouping of <code>Locality</code> objects.
 *
 * @author jstewart
 * @code_status Complete
 */
public class LocalityMapper implements TimingTarget
{
	/** Logger for all messages emitted from this class. */
	protected static final Logger       log					= Logger.getLogger(LocalityMapper.class);
	/** List of <code>Locality</code> objects to be mapped. */
	protected List<Locality>	localities;
	/** A member of <code>localities</code> to be considered the 'current' item. */
	protected Locality			currentLoc;
	/** Labels to apply to the members of <code>localities</code>. */
	protected List<String>		labels;
	/** Pixel locations of markers to apply to members of <code>localities</code>. */
	protected List<Point>		markerLocations;
	/** <code>MapGrabber</code> that provides the basic map images. */
	protected MapGrabber		mapGrabber;
	/** Smallest latitude in the set of localities. */
	protected double			minLat;
	/** Largest latitude in the set of localities. */
	protected double			maxLat;
	/** Smallest longitude in the set of localities. */
	protected double			minLong;
	/** Largest longitude in the set of localities. */
	protected double			maxLong;
	/** Smallest latitude in the set of localities after adding a 5% buffer region. */
	protected double			mapMinLat;
	/** Largest latitude in the set of localities after adding a 5% buffer region. */
	protected double			mapMaxLat;
	/** Smallest longitude in the set of localities after adding a 5% buffer region. */
	protected double			mapMinLong;
	/** Largest longitude in the set of localities after adding a 5% buffer region. */
	protected double			mapMaxLong;
	/** Range of latitude covered by the map. */
	protected double			mapLatRange;
	/** Range of longitude convered by the map. */
	protected double			mapLongRange;
	/** Ratio of image pixels to latitude degrees. */
	protected double			pixelPerLatRatio;
	/** Ratio of image pixels to longitude degrees. */
	protected double			pixelPerLongRatio;
	/** Maximum width of retrieved maps. */
	protected Integer			maxMapWidth;
	/** Maximum height of retrieved maps. */
	protected Integer			maxMapHeight;
	/** Actual width of a retrieved map. */
	protected int				mapWidth;
	/** Actual height of a retrieved map. */
	protected int				mapHeight;
    /** The minimum aspect ratio of the returned map (as a float length/width) */
    protected double             minAspectRatio;
    /** The maximum aspect ratio of the returned map (as a float length/width) */
    protected double             maxAspectRatio;
    /** Indicator as to whether the minimum and maximum aspect ratios are enforced. */
    protected boolean enforceAspectRatios;
	/** The most recent screen coordinate used for painting the map. */
	protected int				mostRecentPaintedX;
	/** The most recent screen coordinate used for painting the map. */
	protected int				mostRecentPaintedY;

	/** Icon to use for painting localities on the map. */
	protected SimpleCircleIcon	marker;
	/** Icon to use for painting the 'current' locality on the map. */
	protected SimpleCircleIcon	currentLocMarker;
	/** Toggle switch for enabling/disabling arrow painting between localities. */
	protected boolean			showArrows;
	/** Toggle switch for enabling/disabling arrow animating between localities. */
	protected boolean			showArrowAnimations	= true;
	/** Toggle switch for enabling/disabling label display near localities. */
	protected boolean			showLabels;
	/** Color of the arrows. */
	protected Color				arrowColor;
	/** Color of the labels. */
	protected Color				labelColor;
	/** Indicates if an animation is currently in progress. */
	protected boolean			animationInProgress	= false;
	/** Percentage of animation that is completed. */
	protected float				percent;
	/** Animation manager. */
	protected TimingController	animator;
	/** Start locality of the current animation. */
	protected Locality			animStartLoc;
	/** End locality of the current animation. */
	protected Locality			animEndLoc;
	/** An <code>Icon</code> of the map. */
	protected Icon				mapIcon;
    /** ??? Rod? ???*/
    protected Icon             overlayIcon = null;
	/** Indicator as to whether the currently cached map image is still valid. */
	protected boolean			cacheValid;


	/**
	 * Constructs an instance using default parameters.
	 */
	public LocalityMapper()
	{
		minLat              =   -90;
        minLong             =  -180;
        maxLat              =    90;
        maxLong             =   180;
        minAspectRatio      =     1;
        maxAspectRatio      =     1;
        enforceAspectRatios = false;
        
        this.localities = new Vector<Locality>();
        this.labels = new Vector<String>();
        this.markerLocations = new Vector<Point>();
        this.mapGrabber = new MapGrabber();
        
        showArrows = true;
        showLabels = true;
        labelColor = Color.BLACK;
        arrowColor = Color.BLACK;
        cacheValid = false;
        
        marker = new SimpleCircleIcon(8, Color.BLACK);
        currentLocMarker = new SimpleCircleIcon(8, Color.BLACK);

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

	
	/**
	 * Constructs an instance to map the given set of localities.
	 * 
	 * @param localities a set of localities to map
	 */
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

	
	/**
	 * Constructs an instance to map the given localities and
	 * applying the given lables.
	 * 
	 * @param localities the localities to map
	 * @param labels the labels to display
	 */
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
	 * Attach another <code>TimingTarget</code> to the underlying animator.
	 * 
	 * @param target the new <code>TimingTarget</code>
	 */
	public void addTimingTarget(TimingTarget target)
	{
		animator.addTarget(target);
	}

	/**
	 * Returns the 'current' locality.
	 * 
	 * @see #setCurrentLoc(Locality)
	 * @return the 'current' locality
	 */
	public Locality getCurrentLoc()
	{
		return currentLoc;
	}

	/**
	 * Sets the 'current' locality.
	 * 
	 * @see #getCurrentLoc()
	 * @param currentLoc the 'current' locality
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

	
	/**
	 * Adds a locality and label to the set to be mapped/displayed.
	 * 
	 * @param loc the locality
	 * @param label the label
	 */
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

	
	/**
	 * Removes a locality (and associated label) from the set to be mapped/displayed. 
	 * 
	 * @param loc the locality
	 */
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
	 * Returns the arrowColor.
	 *
	 * @see #setArrowColor(Color)
	 * @return the arrowColor
	 */
	public Color getArrowColor()
	{
		return arrowColor;
	}

	/**
	 * Sets the arrowColor.
	 *
	 * @see #getArrowColor()
	 * @param arrowColor the arrowColor
	 */
	public void setArrowColor(Color arrowColor)
	{
		this.arrowColor = arrowColor;
	}

	/**
	 * Returns the mapHeight.
	 *
	 * @see #setMapHeight(int)
	 * @return the mapHeight
	 */
	public int getMapHeight()
	{
		return mapHeight;
	}

	/**
	 * Returns the mapWidth.
	 *
	 * @see #setMapWidth(int)
	 * @return the mapWidth
	 */
	public int getMapWidth()
	{
		return mapWidth;
	}

	/**
	 * Returns the labelColor.
	 *
	 * @see #setLabelColor(Color)
	 * @return the labelColor
	 */
	public Color getLabelColor()
	{
		return labelColor;
	}

	/**
	 * Sets the labelColor.
	 *
	 * @see #getLabelColor()
	 * @param labelColor the labelColor
	 */
	public void setLabelColor(Color labelColor)
	{
		this.labelColor = labelColor;
	}

	/**
	 * Returns the maxMapHeight.
	 *
	 * @see #setMaxMapHeight(Integer)
	 * @return the maxMapHeight
	 */
	public Integer getMaxMapHeight()
	{
		return maxMapHeight;
	}

	/**
	 * Sets the maxMapHeight.
	 *
	 * @see #getMaxMapHeight()
	 * @param maxMapHeight the maxMapHeight
	 */
	public void setMaxMapHeight(Integer maxMapHeight)
	{
		this.maxMapHeight = maxMapHeight;
	}

	/**
	 * Returns the maxMapWidth.
	 *
	 * @see #setMaxMapWidth(Integer)
	 * @return the maxMapWidth
	 */
	public Integer getMaxMapWidth()
	{
		return maxMapWidth;
	}

	/**
	 * Sets the maxMapWidth.
	 *
	 * @see #getMaxMapWidth()
	 * @param maxMapWidth the maxMapWidth
	 */
	public void setMaxMapWidth(Integer maxMapWidth)
	{
		this.maxMapWidth = maxMapWidth;
	}

	/**
     * Returns the maximum allowed aspect ratio of the returned maps.
     * 
     * @see #setMaxAspectRatio(float)
	 * @return the maximum allowed aspect ratio
	 */
	public double getMaxAspectRatio()
    {
        return maxAspectRatio;
    }

    /**
     * Sets the maximum allowed aspect ratio of the returned maps.  This limit is enforced
     * as much as possible, but under no circumstances will the returned map be too small to
     * contain the provided Localities, nor will the map have a longitude range greater than
     * 180 to -180 or a latitude range greater than 90 to -90.
     * 
     * @see #getMaxAspectRatio()
     * @param maxAspectRatio the maximum allowed aspect ratio
     */
    public void setMaxAspectRatio(double maxAspectRatio)
    {
        this.maxAspectRatio = maxAspectRatio;
    }

    /**
     * Returns the minimum allowed aspect ratio of the returned maps.
     * 
     * @see #setMinAspectRatio(float)
     * @return the minimum allowed aspect ratio
     */
    public double getMinAspectRatio()
    {
        return minAspectRatio;
    }

    /**
     * Sets the minimum allowed aspect ratio of the returned maps.  This limit is enforced
     * as much as possible, but under no circumstances will the returned map be too small to
     * contain the provided Localities, nor will the map have a longitude range greater than
     * 180 to -180 or a latitude range greater than 90 to -90.
     * 
     * @see #getMinAspectRatio()
     * @param maxAspectRatio the minimum allowed aspect ratio
     */
    public void setMinAspectRatio(double minAspectRatio)
    {
        this.minAspectRatio = minAspectRatio;
    }

    /**
     * Returns true if the aspect ratio limits are enforced.
     * 
     * @return true if the aspect ratio limits are enforced
     */
    public boolean isEnforceAspectRatios()
    {
        return enforceAspectRatios;
    }

    /**
     * Sets the value of {@link #enforceAspectRatios}.
     * 
     * @param enforceAspectRatios whether or not to enforce the aspect ratio limits
     */
    public void setEnforceAspectRatios(boolean enforceAspectRatios)
    {
        this.enforceAspectRatios = enforceAspectRatios;
    }

    /**
	 * Returns the showArrowAnimations.
	 *
	 * @see #setShowArrowAnimations(boolean)
	 * @return the showArrowAnimations
	 */
	public boolean isShowArrowAnimations()
	{
		return showArrowAnimations;
	}

	/**
	 * Sets the showArrowAnimations.
	 *
	 * @see #getShowArrowAnimations()
	 * @param showArrowAnimations the showArrowAnimations
	 */
	public void setShowArrowAnimations(boolean showArrowAnimations)
	{
		this.showArrowAnimations = showArrowAnimations;
	}

	/**
	 * Returns the showArrows.
	 *
	 * @see #setShowArrows(boolean)
	 * @return the showArrows
	 */
	public boolean isShowArrows()
	{
		return showArrows;
	}

	/**
	 * Sets the showArrows.
	 *
	 * @see #getShowArrows()
	 * @param showArrows the showArrows
	 */
	public void setShowArrows(boolean showArrows)
	{
		this.showArrows = showArrows;
	}

	/**
	 * Returns the showLabels.
	 *
	 * @see #setShowLabels(boolean)
	 * @return the showLabels
	 */
	public boolean isShowLabels()
	{
		return showLabels;
	}

	/**
	 * Sets the showLabels.
	 *
	 * @see #getShowLabels()
	 * @param showLabels the showLabels
	 */
	public void setShowLabels(boolean showLabels)
	{
		this.showLabels = showLabels;
	}

	/**
	 * Returns the color of the 'current' locality marker.
	 *
	 * @see #setCurrentLocColor(Color)
	 * @return the color
	 */
	public Color getCurrentLocColor()
	{
		return currentLocMarker.getColor();
	}

	/**
	 * Sets the color of the 'current' locality marker.
	 *
	 * @see #getCurrentLocColor()
	 * @param currentLocColor the color
	 */
	public void setCurrentLocColor(Color currentLocColor)
	{
		currentLocMarker.setColor(currentLocColor);
	}

	/**
	 * Gets the size of the locality marker dot.
	 *
	 * @see #setDotSize(int)
	 * @return the size
	 */
	public int getDotSize()
	{
		return marker.getSize();
	}
	
	/**
	 * Sets the size of the locality marker dot.
	 *
	 * @see #getDotSize()
	 * @param dotSize the size
	 */
	public void setDotSize(int dotSize)
	{
		marker.setSize(dotSize);
		currentLocMarker.setSize(dotSize);
	}

	/**
	 * Returns the color of the locality marker icon.
	 *
	 * @see edu.ku.brc.ui.SimpleCircleIcon#getColor()
	 * @see #setDotColor(Color)
	 * @return the color
	 */
	public Color getDotColor()
	{
		return marker.getColor();
	}

	/**
	 * Sets the color of the locality marker icon.
	 *
	 * @see edu.ku.brc.ui.SimpleCircleIcon#setColor(java.awt.Color)
	 * @see #getDotColor();
	 * @param color the color
	 */
	public void setDotColor(Color color)
	{
		marker.setColor(color);
	}

	/**
	 * Returns the pixel locations of the markers.
	 *
	 * @return the <code>List</code> of marker locations
	 */
	public List<Point> getMarkerLocations()
	{
		return markerLocations;
	}

	/**
	 * Zooms the current map by the given percentage.
	 *
	 * @param percentZoom the zoom ratio
	 */
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

	/**
	 * Pans the current map the given number of degrees in each direction.
	 *
	 * @param latChange the amount of pan in the N/S direction
	 * @param longChange the amount of pan in the E/W direction
	 */
	public void pan(double latChange, double longChange)
	{
        double latChg = latChange;
        double longChg = longChange;
		cacheValid = false;
		if( mapMinLat+latChange<-90 )
		{
            latChg = -90-mapMinLat;
		}
		if( mapMaxLat+latChange>90 )
		{
            latChg = 90-mapMaxLat;
		}
		if( mapMinLong+longChange<-180 )
		{
            longChg = -180-mapMinLong;
		}
		if( mapMaxLong+longChange>180 )
		{
            longChg = 180-mapMaxLong;
		}

		mapMinLat += latChg;
		mapMaxLat += latChg;
		mapMinLong += longChg;
		mapMaxLong += longChg;
	}

	/**
	 * Determines if the given map bounding box is valid.
	 *
	 * @param minimumLat min lat
	 * @param minimumLong min long
	 * @param maximumLat max lat
	 * @param maximumLong max long
	 * @return true if valid
	 */
	protected boolean boxIsValid(double minimumLat, double minimumLong, double maximumLat, double maximumLong)
	{
		if( -90<=minimumLat&&minimumLat<maximumLat&&maximumLat<=90 )
		{
			if( -180<=minimumLong&&minimumLong<maximumLong&&maximumLong<=180 )
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the map bounding box.
	 *
	 * @param minLat min lat
	 * @param minLong min long
	 * @param maxLat max lat
	 * @param maxLong max long
	 */
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

	/**
	 * Gets the lat/long of a locality.  If the locality specifies a bounding box,
	 * the center of the box is returned.
	 *
	 * @param loc the locality
	 * @return the lat/long
	 */
	private Pair<Double, Double> getLatLong(Locality loc)
	{
        Double lat1 = loc.getLatitude1().doubleValue();
        Double long1 = loc.getLongitude1().doubleValue();

        if( loc.getLatitude2() != null && loc.getLongitude2() != null )
		{
            Double lat2 = loc.getLatitude2().doubleValue();
            Double long2 = loc.getLongitude2().doubleValue();
			return centerOfBBox(lat1,lat2,long1,long2);
		}
		
        return new Pair<Double, Double>(lat1,long1);
	}

	/**
	 * Returns the center of the bounding box described by the parameters.
	 *
	 * @param lat1 a latitude
	 * @param lat2 a latitude
	 * @param long1 a longitude
	 * @param long2 a longitude
	 * @return the lat/long center of the bounding box
	 */
	private Pair<Double, Double> centerOfBBox(Double lat1, Double lat2, Double long1, Double long2)
	{
		Pair<Double, Double> center = new Pair<Double, Double>();
		center.first = (lat1+lat2)/2;
		center.second = (long1+long2)/2;
		return center;
	}

	/**
	 * Returns the lat/long ratio of the current map parameters.
	 *
	 * @return the ratio
	 */
	protected double getLatLongRatio()
	{
		double longRange = maxLong-minLong;
		double latRange = maxLat-minLat;
		return latRange/longRange;
	}

	/**
	 * Calculates the proper bounding box to enclose the current set of
	 * localities.
	 */
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
        if (enforceAspectRatios)
        {
            ensureMinMaxAspectRatio();
        }
	}

	/**
	 * Increases the size of the current bounding box in order to create
	 * a 5% size buffer.
	 */
	protected void createBoundingBoxBufferRegion()
	{
        // ensure at least a 5 degree range
		double latSpread = maxLat-minLat;
		if( latSpread<4 )
		{
			latSpread = 5;
		}
		double longSpread = maxLong-minLong;
		if( longSpread<4 )
		{
			longSpread = 5;
		}

        // make sure we have a 5% buffer around the edge of the map
		double bufferFactor = .05;
		mapMinLat = Math.max(-90,minLat-latSpread*bufferFactor);
		mapMinLong = Math.max(-180,minLong-longSpread*bufferFactor);
		mapMaxLat = Math.min(90,maxLat+latSpread*bufferFactor);
		mapMaxLong = Math.min(180,maxLong+longSpread*bufferFactor);
	}
    
    /**
     * Modifies the bounding box to ensure that the minimum and maximum
     * aspect ratio limits are met.
     */
    protected void ensureMinMaxAspectRatio()
    {
        double currentWidth = mapMaxLong - mapMinLong;
        double currentHeight = mapMaxLat - mapMinLat;
        double currentAspectRatio = currentWidth/currentHeight;
        
        if (currentAspectRatio < minAspectRatio)
        {
            // increase aspect ratio to the minimum
            // we do this by increasing the current width (if possible)
            double newWidth = minAspectRatio * currentHeight;
            double amtOfIncr = newWidth - currentWidth;
            mapMinLong = mapMinLong - .5*amtOfIncr;
            mapMaxLong = mapMaxLong + .5*amtOfIncr;
        }
        else if (currentAspectRatio > maxAspectRatio)
        {
            // decrease aspect ratio to the maximum
            // we do this by increasing the current height (if possible)
            double newHeight = currentWidth / maxAspectRatio;
            double amtOfIncr = newHeight - currentHeight;
            mapMinLat = mapMinLat - .5*amtOfIncr;
            mapMaxLat = mapMaxLat + .5*amtOfIncr;
        }
        
        // At this point we have an expanded map that meets the aspect ratio requirements.
        // However, the map might not fit inside the [-180,180] / [-90,90] max bounding box.
        // So, we'll do some panning around, then cropping
        
        // pan right if needed, then left if needed, then crop the width
        if (mapMinLong < -180)
        {
            double panRightAmt = -180 - mapMinLong;
            mapMinLong += panRightAmt;
            mapMaxLong += panRightAmt;
        }
        
        if (mapMaxLong > 180)
        {
            double panLeftAmt = mapMaxLong - 180;
            mapMinLong -= panLeftAmt;
            mapMaxLong -= panLeftAmt;
        }
        
        mapMinLong = Math.max(mapMinLong, -180);
        mapMaxLong = Math.min(mapMaxLong, 180);
        
        // pan up if needed, then down if needed, then crop the height
        if (mapMinLat < -90)
        {
            double panUpAmt = -90 - mapMinLat;
            mapMinLat += panUpAmt;
            mapMaxLat += panUpAmt;
        }
        
        if (mapMaxLat > 90)
        {
            double panDownAmt = mapMaxLat - 90;
            mapMinLat -= panDownAmt;
            mapMaxLat -= panDownAmt;
        }
        
        mapMinLat = Math.max(mapMinLat, -90);
        mapMaxLat = Math.min(mapMaxLat, 90);
        
        // Now we have a bounding box that fits within the maximum bounding box, contains
        // all the provided georeferences, and is as close to the ratio limits as possible
    }

	/**
	 * Configures the {@link MapGrabber} with the given parameters and requests
	 * a new map.
	 *
	 * @param host the map service host
	 * @param defaultPathAndParams the URL path and default parameters
	 * @param layers the map layers
	 * @param minLat min map lat
	 * @param minLong min map long
	 * @param maxLat max map lat
	 * @param maxLong max map long
	 * @return the map image
	 * @throws HttpException a network error occurred while grabbing a new map
	 * @throws IOException a network error occurred while grabbing a new map
	 */
	protected Image getMapFromService(final String host,
                                      final String defaultPathAndParams,
                                      final String layers,
                                      double miniLat,
                                      double miniLong,
                                      double maxiLat,
                                      double maxiLong)	throws HttpException, IOException
	{

        mapGrabber.setHost(host);
        mapGrabber.setDefaultPathAndParams(defaultPathAndParams);
        mapGrabber.setLayers(layers);

		mapGrabber.setMinLat(miniLat);
		mapGrabber.setMaxLat(maxiLat);
		mapGrabber.setMinLong(miniLong);
		mapGrabber.setMaxLong(maxiLong);

		mapGrabber.setMaxHeight(maxMapHeight);
		mapGrabber.setMaxWidth(maxMapWidth);
		return mapGrabber.getMap();
	}

	/**
	 * Finds the pixel location on the map of the given locality.
	 *
	 * @param loc the locality
	 * @return the pixel location
	 */
	protected Point determinePixelCoordsOfLocality(Locality loc)
	{
		Pair<Double, Double> latLong = getLatLong(loc);
		double y = latLong.first-mapMinLat;
		double x = latLong.second-mapMinLong;
		y = mapHeight-y*pixelPerLatRatio;
		x = x*pixelPerLongRatio;
		return new Point((int) x,(int) y);
	}

	/**
	 * Determines if a given pixel location falls on the map icon.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return true if (x,y) is on the map icon
	 */
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

	/**
	 * Returns the lat/long associated with the given pixel location.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the lat/long
	 */
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

	/**
	 * Starts a thread to grab a new map.  When the process succeeds
	 * or fails, <code>callback</code> will be notified.
	 *
	 * @param callback the object to notify when completed
	 */
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

	/**
	 * Grabs a new map from the web service and appropriately adorns it
	 * with labels and markers.
	 *
	 * @return a map image as an icon
	 * @throws HttpException a network error occurred while grabbing the map from the service
	 * @throws IOException a network error occurred while grabbing the map from the service
	 */
	protected Icon grabNewMap() throws HttpException, IOException
	{
		if( !cacheValid )
		{
            Image mapImage = getMapFromService("mapus.jpl.nasa.gov",
                    "/browse.cgi?request=GetMap&srs=EPSG:4326&format=image/png&styles=visual",
                    "global_mosaic",
                    mapMinLat, mapMinLong, mapMaxLat, mapMaxLong);

            Image overlayImage = getMapFromService("129.237.201.132",
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

			pixelPerLatRatio = mapHeight/mapLatRange;
			pixelPerLongRatio = mapWidth/mapLongRange;

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
//                overlayIcon.paintIcon(c, g, x, y);

				Point lastLoc = null;
				for( int i = 0; i<localities.size(); ++i )
				{
					Point markerLoc = markerLocations.get(i);
					String label = labels.get(i);
					boolean current = (currentLoc!=null)&&markerLoc.equals(currentLocPoint);

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
					// TODO: draw an arrow from lastLoc to iconLoc
					if( showArrows && lastLoc != null )
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
						GraphicsUtils.drawArrow(g,x1,y1,x2,y2,2,2);
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

	/**
	 * Sets the initial state of animation related fields.  This is
	 * called by the TimingController just before an animation begins.
	 *
	 * @see org.jdesktop.animation.timing.TimingTarget#begin()
	 */
	public void begin()
	{
		this.percent = 0;
		this.animationInProgress = true;
	}

	/**
	 * Sets the finalized state of animation related fields.  This is
	 * called by the TimingController just after an animation ends.
	 *
	 * @see org.jdesktop.animation.timing.TimingTarget#end()
	 */
	public void end()
	{
		this.animationInProgress = false;
	}

	/**
	 * Provides callback method for animation progress notifications.
	 *
	 * @see org.jdesktop.animation.timing.TimingTarget#timingEvent(long, long, float)
	 * @param cycleElapsedTime the total time in milliseconds elapsed in the current <code>Cycle</code>
	 * @param totalElapsedTime the total time in milliseconds elapsed since the start of the first cycle
	 * @param percentDone the fraction of completion between the start and end of the current cycle. Note that on reversing cycles (<code>Envelope.RepeatBehavior.REVERSE</code>) the fraction decreases from 1.0 to 0 on backwards-running cycles.
	 */
	public void timingEvent(long cycleElapsedTime, long totalElapsedTime, float percentDone)
	{
		this.percent = percentDone;
	}

	/**
	 * Defines requirements for objects that receive callbacks from a 
	 * <code>LocalityMapper</code> instance about the state of the map
	 * generation process.
	 * 
	 * @author jstewart
	 */
	public interface MapperListener
	{
		/**
		 * Signals that a map was successfully retrieved.
		 * 
		 * @param map the map
		 */
		public void mapReceived(Icon map);

		/**
		 * Signals that an exception occurred while grabbing map.
		 * 
		 * @param e the <code>Exception</code> that occurred during map grabbing operations
		 */
		public void exceptionOccurred(Exception e);
	}
}
