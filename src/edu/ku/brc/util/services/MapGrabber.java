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

package edu.ku.brc.util.services;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.UICacheManager;
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
	protected Integer maxHeight = null;
	/** Maximum width of returned map (in pixels). */
	protected Integer maxWidth = null;
	/** Maximum height of returned map if caller doesn't specify one. */
	protected int defaultMaxHeight = 2048;
	/** Maximum width of returned map if caller doesn't specify one. */
	protected int defaultMaxWidth = 2048;

	/** FileCache for caching retrieved maps. */
	protected static FileCache imageCache = UICacheManager.getLongTermFileCache();

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
	 * @see #setMaxHeight(Integer)
	 * @return the max map height
	 */
	public Integer getMaxHeight()
	{
		return maxHeight;
	}

	/**
	 * Sets the max map height the grabber should return.
	 * 
	 * @see #getMaxHeight()
	 * @param height the max height
	 */
	public void setMaxHeight(Integer height)
	{
        Integer ht = height;
		if( ht != null && ht > 2048 )
		{
            ht = 2048;
		}
		this.maxHeight = ht;
	}

	/**
	 * Returns the max map width.
	 * 
	 * @see #setMaxWidth(Integer)
	 * @return the max map width
	 */
	public Integer getMaxWidth()
	{
		return maxWidth;
	}

	/**
	 * Sets the max map width the grabber should return.
	 * 
	 * @see #getMaxWidth()
	 * @param width the max map width
	 */
	public void setMaxWidth(Integer width)
	{
        Integer wt = width;
		if( wt != null && wt.intValue() > 2048 )
		{
            wt = 2048;
		}
		this.maxWidth = wt;
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
     * {@link #setMaxWidth(Integer)} and {@link #setMaxHeight(Integer)} followed
     * by a call to {@link #getMap()}.
     * 
     * @see #getMap()
     * @see #setMaxWidth(Integer)
     * @see #setMaxHeight(Integer)
     * 
     * @param width the max map width
     * @param height the max map height
     * @return a map image
     * @throws HttpException if network errors occur while grabbing map from service
     * @throws IOException if network errors occur while grabbing map from service
     */
    public Image getMap(Integer width,
						Integer height)
		throws HttpException, IOException
	{
		setMaxWidth(width);
		setMaxHeight(height);
		return getMap();
	}

    /**
     * Provides a convenience method.  Calling this method results in calls to
     * setters for the various fields associated with the parameters followed
     * by a call to {@link #getMap()}.
     * 
     * @see #getMap()
     * 
     * @param width the max map width
     * @param height the max map height
     * @param minLat the min map latitude
     * @param minLong the min map longitude
     * @param maxLat the max map latitude
     * @param maxLong the max map longitude
     * @return a map image
     * @throws HttpException if network errors occur while grabbing map from service
     * @throws IOException if network errors occur while grabbing map from service
     */
	public Image getMap(Integer width,
						Integer height,
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
		setMaxWidth(width);
		setMaxHeight(height);
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
	 * Calculates the appropriate width and height of a map (before grabbing it)
	 * based on the values of maxWidth, maxHeight, minLat, minLong, maxLat, and
	 * maxLong.  The values of maxHeight and maxWidth are modified appropriately.
	 */
	protected void calcWidthAndHeight()
	{
		double longSpread = maxLong - minLong;
		double latSpread = maxLat - minLat;

		boolean fatMap = longSpread > latSpread ? true : false;

		if( fatMap )
		{
			// calculate the height from max width
			maxHeight = (int)(latSpread/longSpread*maxWidth);
			return;
		}
		
		// calculate the width from the max height
		maxWidth = (int)(maxHeight*longSpread/latSpread);
		return;
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
		log.debug("maxWidth="+maxWidth+"\tmaxHeight="+maxHeight);
		double longSpread = maxLong - minLong;
		double latSpread = maxLat - minLat;
		log.debug("lat spread:  min="+minLat+"\tmax="+maxLat+"\trange="+latSpread);
		log.debug("long spread: min="+minLong+"\tmax="+maxLong+"\trange="+longSpread);
		if( maxWidth == null && maxHeight == null )
		{
			maxWidth = defaultMaxWidth;
			maxHeight = defaultMaxHeight;
		}
		calcWidthAndHeight();

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
		url.append(maxHeight);
		url.append("&width=");
		url.append(maxWidth);

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
				imageCache.cacheWebResource(urlStr);
				imageFile = imageCache.getCacheFile(urlStr);
			}
			image = Toolkit.getDefaultToolkit().getImage(imageFile.getAbsolutePath());
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
		}

		return image;
	}
}
