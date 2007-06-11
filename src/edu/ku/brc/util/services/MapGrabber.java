/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.util.services;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.FileCache;

/**
 * Grabs a Map from a web-based mapping service.
 *
 * @author jstewart
 * @code_status Complete
 */
public class MapGrabber
{
	/** Logger for all log messages emitted from this class. */
	private static final Logger log = Logger.getLogger(MapGrabber.class);

	/** HttpClient used for grabbing maps using HTTP. */
	protected HttpClient httpClient;

	// setup some default values
	// TODO: remove these from any final versions

//	// Aimee's server
//	protected String host = "129.237.201.104";
//	protected String defaultPathAndParams = "/cgi-bin/ogc.cgi/bmortho?version=1.1.1&service=WMS&Request=GetMap&format=image/gif&styles=default&srs=epsg:4326";
//	protected String layers = "bmng";

	// NASA server
	/** The map server host. */
	protected String host = "mapus.jpl.nasa.gov";
	/** URL path and parameters for the map service. */
	protected String defaultPathAndParams = "/browse.cgi?request=GetMap&srs=EPSG:4326&format=image/png&styles=visual";
	/** Value of the map service layers parameter. */
	protected String layers = "global_mosaic";

	/** Southernmost latitude of interest. */
	protected double minLat = -90;
	/** Westernmost longitude of interest. */
	protected double minLong = -180;
	/** Northernmost latitude of interest. */
	protected double maxLat = 90;
	/** Easternmost longitude of interest. */
	protected double maxLong = 180;
	/** Maximum height of returned map (in pixels). */
	protected Integer height = null;
	/** Maximum width of returned map (in pixels). */
	protected Integer width = null;
	/** Maximum height of returned map if caller doesn't specify one. */
	protected int defaultHeight = 2048;
	/** Maximum width of returned map if caller doesn't specify one. */
	protected int defaultWidth = 2048;

	/** FileCache for caching retrieved maps. */
	protected static FileCache imageCache = UIRegistry.getLongTermFileCache();

	/**
	 * Constructs a <code>MapGrabber</code> using all default parameters.
	 */
	public MapGrabber()
	{
		httpClient = new HttpClient();
	}

	/**
	 * Returns the mapping host.
	 * 
	 * @see #setHost(String)
	 * @return the host
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Sets the mapping host.
	 * 
	 * @see #getHost()
	 * @param host the host
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * Returns the layers string used in the map request to the server.
	 * 
	 * @see #setLayers(String)
	 * @return the layers string
	 */
	public String getLayers()
	{
		return layers;
	}

	/**
	 * Sets the layers string used in the map request to the server.
	 * 
	 * @see #getLayers()
	 * @param layers the layers string
	 */
	public void setLayers(String layers)
	{
		this.layers = layers;
	}

	/**
	 * Returns the max map height.
	 * 
	 * @see #setHeight(Integer)
	 * @return the max map height
	 */
	public Integer getHeight()
	{
		return height;
	}

	/**
	 * Sets the max map height the grabber should return.
	 * 
	 * @see #getHeight()
	 * @param height the max height
	 */
	public void setHeight(Integer height)
	{
        Integer ht = height;
		if( ht != null && ht > 2048 )
		{
            ht = 2048;
		}
		this.height = ht;
	}

	/**
	 * Returns the max map width.
	 * 
	 * @see #setWidth(Integer)
	 * @return the max map width
	 */
	public Integer getWidth()
	{
		return width;
	}

	/**
	 * Sets the max map width the grabber should return.
	 * 
	 * @see #getWidth()
	 * @param width the max map width
	 */
	public void setWidth(Integer width)
	{
        Integer wt = width;
		if( wt != null && wt.intValue() > 2048 )
		{
            wt = 2048;
		}
		this.width = wt;
	}

	/**
	 * Returns the northernmost latitude of the map.
	 * 
	 * @see #setMaxLat(double)
	 * @return the Easternmore latitude
	 */
	public double getMaxLat()
	{
		return maxLat;
	}

	/**
	 * Sets the northernmost latitude on the map.
	 * 
	 * @see #getMaxLat()
	 * @param maxLat the Easternmost latitude
	 */
	public void setMaxLat(double maxLat)
	{
		this.maxLat = maxLat;
	}

	/**
	 * Returns the easternmost latitude of the map.
	 * 
	 * @see #setMaxLong(double)
	 * @return the Northernmost latitude
	 */
	public double getMaxLong()
	{
		return maxLong;
	}

	/**
	 * Sets the easternmost latitude of the map.
	 * 
	 * @see #getMaxLong()
	 * @param maxLong the Northernmost latitude
	 */
	public void setMaxLong(double maxLong)
	{
		this.maxLong = maxLong;
	}

	/**
	 * Returns the southernmost latitude of the map.
	 * 
	 * @see #setMinLat(double)
	 * @return the Westernmost latitude
	 */
	public double getMinLat()
	{
		return minLat;
	}

	/**
	 * Sets the southernmost latitude of the map.
	 * 
	 * @see #getMinLat()
	 * @param minLat the Westernmost latitude
	 */
	public void setMinLat(double minLat)
	{
		this.minLat = minLat;
	}

	/**
	 * Returns the westernmost longitude of the map.
	 * 
	 * @see #setMinLong(double)
	 * @return the Southernmost longitude
	 */
	public double getMinLong()
	{
		return minLong;
	}

	/**
	 * Sets the westernmost longitude of the map.
	 * 
	 * @see #getMinLong()
	 * @param minLong the Southernmost longitude
	 */
	public void setMinLong(double minLong)
	{
		this.minLong = minLong;
	}

	/**
	 * Sets the default URL path and params for the mapping web service.
	 * 
	 * @param defaultPathAndParams the URL path and params
	 */
	public void setDefaultPathAndParams(String defaultPathAndParams)
    {
        this.defaultPathAndParams = defaultPathAndParams;
    }

    /**
     * Provides a convenience method.  Calling this method results in calls to
     * setters for the various fields associated with the parameters followed
     * by a call to {@link #getMap()}.
     * 
     * @see #getMap()
     * 
     * @param w the max map width
     * @param h the max map height
     * @param minLat the min map latitude
     * @param minLong the min map longitude
     * @param maxLat the max map latitude
     * @param maxLong the max map longitude
     * @return a map image
     * @throws HttpException if network errors occur while grabbing map from service
     * @throws IOException if network errors occur while grabbing map from service
     */
	public Image getMap(Integer w,
						Integer h,
						int minLatitude,
						int minLongitude,
						int maxLatitude,
						int maxLongitude)
		throws HttpException, IOException
	{
		setMinLat(minLatitude);
		setMinLong(minLongitude);
		setMaxLat(maxLatitude);
		setMaxLong(maxLongitude);
		setWidth(w);
		setHeight(h);
		return getMap();
	}

	/**
	 * Returns the ratio latitude range : longitude range.
	 * 
	 * @return the ratio
	 */
	protected double getLatLongRatio()
	{
		double longRange = maxLong - minLong;
		double latRange = maxLat - minLat;
		return (latRange/longRange);
	}

	/**
	 * Retrieves a map from the web service.
	 * 
	 * @return a map image
     * @throws HttpException if network errors occur while grabbing map from service
     * @throws IOException if network errors occur while grabbing map from service
	 */
	public Image getMap() throws HttpException, IOException
	{
		log.debug("Entering MapGrabber.getMap()");
		log.debug("width="+width+"\theight="+height);
		double longSpread = maxLong - minLong;
		double latSpread = maxLat - minLat;
		log.debug("lat spread:  min="+minLat+"\tmax="+maxLat+"\trange="+latSpread);
		log.debug("long spread: min="+minLong+"\tmax="+maxLong+"\trange="+longSpread);
		if( width == null && height == null )
		{
			width = defaultWidth;
			height = defaultHeight;
		}

		StringBuilder url = new StringBuilder("http://");
		url.append(host);
		url.append(defaultPathAndParams);

		// set layers
		url.append("&layers=");
		url.append(layers);

		// set bounding box
		url.append("&bbox=");
		url.append(minLong);
		url.append(",");
		url.append(minLat);
		url.append(",");
		url.append(maxLong);
		url.append(",");
		url.append(maxLat);

		// set size
		url.append("&height=");
		url.append(height);
		url.append("&width=");
		url.append(width);

		Image image;
		String urlStr = url.toString();
		if( imageCache != null )
		{
			// check the image cache
			log.info("Asking cache to grab map image: " + urlStr);
			File imageFile = imageCache.getCacheFile(urlStr);
			if( imageFile == null )
			{
				// the image wasn't in the cache
				// grab it again
				urlStr = imageCache.cacheWebResource(urlStr);
				imageFile = imageCache.getCacheFile(urlStr);
			}
			image = Toolkit.getDefaultToolkit().getImage(imageFile.getAbsolutePath());
            ImageIcon mapIcon = new ImageIcon(image);
            if (mapIcon.getIconHeight() < 0 || mapIcon.getIconWidth() < 0)
            {
                // since it was invalid, throw it out of the cache
                imageCache.clearItem(urlStr);
                throw new IOException("Mapping service failed to return a valid image.  Map request URL: " + urlStr);
            }
		}
		else
		{
			log.info("No image cache available.  Grabbing map internally.");
			GetMethod get = new GetMethod(urlStr);
			get.setFollowRedirects(true);
			int resultCode = httpClient.executeMethod(get);
			log.info("GET " + urlStr + " returned " + resultCode );
			log.info("Exiting MapGrabber.getMap()");
			byte[] data = get.getResponseBody();
			image = Toolkit.getDefaultToolkit().createImage(data);
            ImageIcon mapIcon = new ImageIcon(image);
            if (mapIcon.getIconHeight() < 0 || mapIcon.getIconWidth() < 0)
            {
                throw new IOException("Mapping service failed to return a valid image.  Map request URL: " + urlStr);
            }
		}

		return image;
	}
}
