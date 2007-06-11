/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.services.mapping;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.jdesktop.animation.timing.Cycle;
import org.jdesktop.animation.timing.Envelope;
import org.jdesktop.animation.timing.TimingController;
import org.jdesktop.animation.timing.TimingTarget;

import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.SimpleCircleIcon;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.services.MapGrabber;

/**
 * Maps a collection of <code>Locality</code> objects.
 *
 * @author jstewart
 * @code_status Beta
 */
public class LocalityMapper implements TimingTarget
{
	/** Logger for all messages emitted from this class. */
	protected static final Logger log = Logger.getLogger(LocalityMapper.class);
    
	/** List of {@link MapLocationIFace} objects to be mapped. */
	protected List<MapLocationIFace> mapLocations;
	/** A member of {@link #mapLocations} to be considered the 'current' item. */
	protected MapLocationIFace  currentLoc;
	/** Labels to apply to the members of {@link #mapLocations}. */
	protected List<String>		labels;
	/** Pixel locations of markers to apply to members of {@link #mapLocations}. */
	protected List<Point>		markerLocations;
	/** Smallest latitude in the set of {@link MapLocationIFace}s. */
	protected double			minLat;
	/** Largest latitude in the set of {@link MapLocationIFace}s. */
	protected double			maxLat;
	/** Smallest longitude in the set of {@link MapLocationIFace}s. */
	protected double			minLong;
	/** Largest longitude in the set of {@link MapLocationIFace}s. */
	protected double			maxLong;
	/**locationslatitude in the set of {@link MapLocationIFace}s after adding a 5% buffer region. */
	protected double			mapMinLat;
	/** Largest latitude in the set of {@link MapLocationIFace}s after adding a 5% buffer region. */
	protected double			mapMaxLat;
	/** Smallest longitude in the set of {@link MapLocationIFace}s after adding a 5% buffer region. */
	protected double			mapMinLong;
	/** Largest longitude in the set of {@link MapLocationIFace}s after adding a 5% buffer region. */
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
    protected double            minAspectRatio;
    /** The maximum aspect ratio of the returned map (as a float length/width) */
    protected double            maxAspectRatio;
    /** Indicator as to whether the minimum and maximum aspect ratios are enforced. */
    protected boolean           enforceAspectRatios;
	/** The most recent screen coordinate used for painting the map. */
	protected int				mostRecentPaintedX;
	/** The most recent screen coordinate used for painting the map. */
	protected int				mostRecentPaintedY;

	/** Icon to use for painting locations on the map. */
	protected SimpleCircleIcon	marker;
	/** Icon to use for painting the 'current' location on the map. */
	protected SimpleCircleIcon	currentLocMarker;
	/** Toggle switch for enabling/disabling arrow painting between map locations. */
	protected boolean			showArrows;
	/** Toggle switch for enabling/disabling arrow animating between map locations. */
	protected boolean			showArrowAnimations	= true;
	/** Toggle switch for enabling/disabling label display near map locations. */
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
	/** Start location of the current animation. */
	protected MapLocationIFace			animStartLoc;
	/** End location of the current animation. */
	protected MapLocationIFace			animEndLoc;
	/** An <code>Icon</code> of the map. */
	protected Icon				mapIcon;
    /** ??? Rod? ???*/
    protected Icon              overlayIcon = null;
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
        
        this.mapLocations     = new Vector<MapLocationIFace>();
        this.labels           = new Vector<String>();
        this.markerLocations  = new Vector<Point>();
        
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
	 * Constructs an instance to map the given set of locations.
	 * 
	 * @param locations a set of locations to map
	 */
	public LocalityMapper(List<MapLocationIFace> locations)
	{
		this();
		this.mapLocations.addAll(locations);
		for( int i = 0; i<locations.size(); ++i )
		{
			labels.add(null);
		}
		for( int i = 0; i<locations.size(); ++i )
		{
			markerLocations.add(null);
		}
        cacheValid = false;
	}

	
	/**
	 * Constructs an instance to map the given locations and
	 * applying the given lables.
	 * 
	 * @param locations the locations to map
	 * @param labels the labels to display
	 */
	public LocalityMapper(List<MapLocationIFace> locations, List<String> labels)
	{
		this();

        // check the sizes
        if (locations.size() != labels.size())
        {
            throw new IllegalArgumentException("Locations and labels list must be the same size");
        }

		this.mapLocations.addAll(mapLocations);
		this.labels.addAll(labels);
		for( int i = 0; i<mapLocations.size(); ++i )
		{
			markerLocations.add(null);
		}
        cacheValid = false;
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
	public MapLocationIFace getCurrentLoc()
	{
		return currentLoc;
	}

	/**
	 * Sets the 'current' locality.
	 * 
	 * @see #getCurrentLoc()
	 * @param currentLoc the 'current' locality
	 */
	public void setCurrentLoc(MapLocationIFace currentLoc)
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
			int startIndex = mapLocations.indexOf(animStartLoc);
			int endIndex = mapLocations.indexOf(animEndLoc);
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
	 * Adds a location and label to the set to be mapped/displayed.
	 * 
	 * @param loc the location
	 * @param label the label
	 */
	public void addLocationAndLabel(MapLocationIFace loc, String label)
	{
		mapLocations.add(loc);
		labels.add(label);
        markerLocations.add(null);
        cacheValid = false;
	}
	
	/**
	 * Removes a location (and associated label) from the set to be mapped/displayed. 
	 * 
	 * @param loc the location
	 */
	public void removeLocationAndLabel(MapLocationIFace loc)
	{
		int index = mapLocations.indexOf(loc);
		mapLocations.remove(index);
		labels.remove(index);
		markerLocations.remove(index);
        cacheValid = false;
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
        cacheValid = false;
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
        cacheValid = false;
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
     * contain the provided {@link MapLocationIFace}s, nor will the map have a longitude range greater than
     * 180 to -180 or a latitude range greater than 90 to -90.
     * 
     * @see #getMaxAspectRatio()
     * @param maxAspectRatio the maximum allowed aspect ratio
     */
    public void setMaxAspectRatio(double maxAspectRatio)
    {
        this.maxAspectRatio = maxAspectRatio;
        if (this.enforceAspectRatios)
        {
            cacheValid = false;
        }
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
     * contain the provided {@link MapLocationIFace}s, nor will the map have a longitude range greater than
     * 180 to -180 or a latitude range greater than 90 to -90.
     * 
     * @see #getMinAspectRatio()
     * @param maxAspectRatio the minimum allowed aspect ratio
     */
    public void setMinAspectRatio(double minAspectRatio)
    {
        this.minAspectRatio = minAspectRatio;
        if (this.enforceAspectRatios)
        {
            cacheValid = false;
        }
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
        
        if (this.enforceAspectRatios)
        {
            cacheValid = false;
        }
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
		double longRangeChange = mapLongRange*1/percentZoom;
		double longChange = .5*(mapLongRange-longRangeChange);
		mapMinLong += longChange;
		mapMaxLong -= longChange;
		double latRangeChange = mapLatRange*1/percentZoom;
		double latChange = .5*(mapLatRange-latRangeChange);
		mapMinLat += latChange;
		mapMaxLat -= latChange;
        cacheValid = false;
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
        cacheValid = false;
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
		if( -90 <= minimumLat && minimumLat < maximumLat && maximumLat <= 90 )
		{
			if( -180 <= minimumLong && minimumLong < maximumLong && maximumLong <= 180 )
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the lat/long of a locality.  If the locality specifies a bounding box,
	 * the center of the box is returned.
	 *
	 * @param loc the locality
	 * @return the lat/long
	 */
	private Pair<Double, Double> getLatLong(MapLocationIFace loc)
	{
        Double lat1 = loc.getLat1();
        Double long1 = loc.getLong1();
        Double lat2 = loc.getLat2();
        Double long2 = loc.getLong2();

        if( lat2 != null && long2 != null )
		{
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
	 * {@link MapLocationIFace}s.
	 */
	protected void recalculateBoundingBox()
	{
		if( mapLocations.isEmpty() )
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
		for( MapLocationIFace loc : mapLocations )
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
        
        expandMapRegionToFillUsableSpace();
        
        if (enforceAspectRatios)
        {
            ensureMinMaxAspectRatio();
        }
	}

	/**
	 * Increases the size of the current bounding box in order to create
	 * a bit of a buffer (and ensure the bounding box isn't a single point).
	 */
    protected void createBoundingBoxBufferRegion()
    {
        double minSpread = .025;
        double latSpread = maxLat-minLat;
        if( latSpread < minSpread )
        {
            // expand the range to at least be .5 degrees
            double diff = minSpread - latSpread;
            latSpread = minSpread;
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
        if( longSpread < minSpread )
        {
            // expand the range to at least be .5 degrees
            double diff = minSpread - longSpread;
            longSpread = minSpread;
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
                                      double maxiLong,
                                      int height,
                                      int width)	throws HttpException, IOException
	{
	    MapGrabber mapGrabber = new MapGrabber();
        mapGrabber.setHost(host);
        mapGrabber.setDefaultPathAndParams(defaultPathAndParams);
        mapGrabber.setLayers(layers);

		mapGrabber.setMinLat(miniLat);
		mapGrabber.setMaxLat(maxiLat);
		mapGrabber.setMinLong(miniLong);
		mapGrabber.setMaxLong(maxiLong);

		mapGrabber.setHeight(height);
		mapGrabber.setWidth(width);
		return mapGrabber.getMap();
	}

	/**
	 * Finds the pixel location on the map of the given map location.
	 *
	 * @param loc the map location
	 * @return the pixel location
	 */
	protected Point determinePixelCoordsOfMapLocationIFace(MapLocationIFace loc)
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
		Thread mapGrabberThread = new Thread("Map Grabber")
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
        recalculateBoundingBox();
        
		if( !cacheValid )
		{
            Image mapImage = getMapFromService("mapus.jpl.nasa.gov",
                    "/wms.cgi?request=GetMap&srs=EPSG:4326&format=image/png&styles=visual",
                    "global_mosaic",
                    mapMinLat, mapMinLong, mapMaxLat, mapMaxLong, maxMapHeight, maxMapWidth);

            Image overlayImage = getMapFromService("129.237.201.132",
                    //"/cgi-bin/ogc.cgi/specify?service=WMS&request=GetMap&srs=EPSG:4326&version=1.3.1&format=image/png&transparent=true",
                    "/cgi-bin/mapserv?map=/var/www/maps/specify.map&service=WMS&request=GetMap&srs=EPSG:4326&version=1.3.1&format=image/png&transparent=true",
                    "states,rivers",
                    mapMinLat, mapMinLong, mapMaxLat, mapMaxLong, maxMapHeight, maxMapWidth);

			mapIcon     = new ImageIcon(mapImage);
            overlayIcon = new ImageIcon(overlayImage);
			cacheValid  = true;

			mapWidth = mapIcon.getIconWidth();
			mapHeight = mapIcon.getIconHeight();
            
            if (mapWidth < 0 || mapHeight < 0)
            {
                throw new IOException("Request for map failed.  Received map has negative width or height.");
            }

			mapLatRange = mapMaxLat-mapMinLat;
			mapLongRange = mapMaxLong-mapMinLong;

			pixelPerLatRatio = mapHeight/mapLatRange;
			pixelPerLongRatio = mapWidth/mapLongRange;

			for( int i = 0; i<mapLocations.size(); ++i )
			{
				MapLocationIFace loc = mapLocations.get(i);
				Point iconLoc = determinePixelCoordsOfMapLocationIFace(loc);
				markerLocations.set(i,iconLoc);
			}
            
            cacheValid = true;
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
					currentLocPoint = determinePixelCoordsOfMapLocationIFace(currentLoc);
				}

				mapIcon.paintIcon(c,g,x,y);
                overlayIcon.paintIcon(c, g, x, y);

				Point lastLoc = null;
				for( int i = 0; i<mapLocations.size(); ++i )
				{
					Point markerLoc = markerLocations.get(i);
					String label = labels.get(i);
					boolean current = (currentLoc!=null)&&markerLoc.equals(currentLocPoint);

					if( markerLoc == null )
					{
						log.error("A marker location is null");
						continue;
					}
					if( !pointIsOnMapIcon(x+markerLoc.x, y+markerLoc.y) )
					{
						log.error("A marker location is off the map");
						continue;
					}
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
				if( showArrowAnimations && animationInProgress )
				{
					int startIndex = mapLocations.indexOf(animStartLoc);
					int endIndex = mapLocations.indexOf(animEndLoc);
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
     * @code_status Complete
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
    
    /**
     * This interface defines the minimal information retrievable from any object that can
     * be mapped by a {@link LocalityMapper}.
     * 
     * @author jstewart
     * @code_status Complete
     */
    public static interface MapLocationIFace
    {
        /**
         * Returns the latitude of the point or the 'upper-left corner' of the bounding box.
         * 
         * @return the latitude of the point or the 'upper-left corner' of the bounding box
         */
        public Double getLat1();
        
        /**
         * Returns the longitude of the point or the 'upper-left corner' of the bounding box.
         * 
         * @return the longitude of the point or the 'upper-left corner' of the bounding box
         */
        public Double getLong1();
        
        /**
         * Returns the latitude of the 'lower-right corner' of the bounding box.
         * 
         * @return the latitude of the 'lower-right corner' of the bounding box
         */
        public Double getLat2();
        
        /**
         * Returns the longitude of the 'lower-right corner' of the bounding box.
         * 
         * @return the longitude of the 'lower-right corner' of the bounding box
         */
        public Double getLong2();
        
    }

    
    // I commented out this code so it doesn't get compiled in.  However, if I ever need to come back and retest this class, I'll need this again.
    public static void main(String[] args)
    {
        // some test points
//        SimpleMapLocation l1 = new SimpleMapLocation(38.877,-94.871,null,null);
//        SimpleMapLocation l2 = new SimpleMapLocation(38.875,-94.875,null,null);
//        SimpleMapLocation l3 = new SimpleMapLocation(38.879,-94.877,null,null);
//        SimpleMapLocation l4 = new SimpleMapLocation(38.871,-94.879,null,null);

        SimpleMapLocation l1 = new SimpleMapLocation(38.662,-95.574,null,null);
//        SimpleMapLocation l2 = new SimpleMapLocation(38.875,-94.875,null,null);
//        SimpleMapLocation l3 = new SimpleMapLocation(38.879,-94.877,null,null);
//        SimpleMapLocation l4 = new SimpleMapLocation(38.871,-94.879,null,null);

        LocalityMapper lm = new LocalityMapper();
        lm.addLocationAndLabel(l1, null);
//        lm.addLocationAndLabel(l2, null);
//        lm.addLocationAndLabel(l3, null);
//        lm.addLocationAndLabel(l4, null);
        lm.setShowArrows(false);
        lm.setDotColor(Color.YELLOW);
     
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JLabel mapLabel = new JLabel();
        int w = 400;
        int h = 250;
        Dimension dimension = new Dimension(w,h);
        mapLabel.setSize(dimension);
        mapLabel.setPreferredSize(dimension);
        f.add(mapLabel);
        
        lm.setMaxMapWidth(w);
        lm.setMaxMapHeight(h);

        MapperListener l = new MapperListener()
        {
            public void exceptionOccurred(Exception e)
            {
                final StringWriter result = new StringWriter();
                final PrintWriter printWriter = new PrintWriter(result);
                e.printStackTrace(printWriter);

                mapLabel.setText("<html><pre> " + result.toString() + "</pre></html>");
            }
            public void mapReceived(Icon map)
            {
                mapLabel.setIcon(map);
            }
        };
        
        f.pack();

        lm.getMap(l);
        
        f.setVisible(true);
    }
}
