package edu.ku.brc.specify.tasks.services;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.util.FileCache;

public class MapGrabber
{
	private static Log log = LogFactory.getLog(MapGrabber.class);
	// setup some default values
	// TODO: remove these from any final versions
	
	protected HttpClient httpClient;
	
//	// Aimee's server
//	protected String host = "129.237.201.104";
//	protected String defaultPathAndParams = "/cgi-bin/ogc.cgi/bmortho?version=1.1.1&service=WMS&Request=GetMap&format=image/gif&styles=default&srs=epsg:4326";
//	protected String layers = "bmng";

	// NASA server
	protected String host = "mapus.jpl.nasa.gov";
	protected String defaultPathAndParams = "/wms.cgi?request=GetMap&srs=EPSG:4326&format=image/png&styles=visual";
	protected String layers = "global_mosaic";
	
	protected double minLat = -90;
	protected double minLong = -180;
	protected double maxLat = 90;
	protected double maxLong = 180;
	protected Integer maxHeight = null;
	protected Integer maxWidth = null;
	protected int defaultMaxHeight = 2048;
	protected int defaultMaxWidth = 2048;

	protected static FileCache imageCache = UICacheManager.getLongTermFileCache();
	
	public MapGrabber()
	{
		httpClient = new HttpClient();
	}
	
	/**
	 * @return Returns the host.
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * @param host The host to set.
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * @return Returns the layers.
	 */
	public String getLayers()
	{
		return layers;
	}

	/**
	 * @param layers The layers to set.
	 */
	public void setLayers(String layers)
	{
		this.layers = layers;
	}
	
	/**
	 * @return Returns the maxHeight.
	 */
	public Integer getMaxHeight()
	{
		return maxHeight;
	}

	/**
	 * @param maxHeight The maxHeight to set.
	 */
	public void setMaxHeight(Integer height)
	{
		if( height != null && height > 2048 )
		{
			height = 2048;
		}
		this.maxHeight = height;
	}

	/**
	 * @return Returns the maxWidth.
	 */
	public Integer getMaxWidth()
	{
		return maxWidth;
	}

	/**
	 * @param maxWidth The maxWidth to set.
	 */
	public void setMaxWidth(Integer width)
	{
		if( width != null && width.intValue() > 2048 )
		{
			width = 2048;
		}
		this.maxWidth = width;
	}
	
	/**
	 * @return Returns the maxLat.
	 */
	public double getMaxLat()
	{
		return maxLat;
	}

	/**
	 * @param maxLat The maxLat to set.
	 */
	public void setMaxLat(double maxLat)
	{
		this.maxLat = maxLat;
	}

	/**
	 * @return Returns the maxLong.
	 */
	public double getMaxLong()
	{
		return maxLong;
	}

	/**
	 * @param maxLong The maxLong to set.
	 */
	public void setMaxLong(double maxLong)
	{
		this.maxLong = maxLong;
	}

	/**
	 * @return Returns the minLat.
	 */
	public double getMinLat()
	{
		return minLat;
	}

	/**
	 * @param minLat The minLat to set.
	 */
	public void setMinLat(double minLat)
	{
		this.minLat = minLat;
	}

	/**
	 * @return Returns the minLong.
	 */
	public double getMinLong()
	{
		return minLong;
	}

	/**
	 * @param minLong The minLong to set.
	 */
	public void setMinLong(double minLong)
	{
		this.minLong = minLong;
	}

	public Image getMap(Integer width,
						Integer height)
		throws HttpException, IOException
	{
		setMaxWidth(width);
		setMaxHeight(height);
		return getMap();
	}

	public Image getMap(Integer width,
						Integer height,
						int minLat,
						int minLong,
						int maxLat,
						int maxLong)
		throws HttpException, IOException
	{
		setMinLat(minLat);
		setMinLong(minLong);
		setMaxLat(maxLat);
		setMaxLong(maxLong);
		setMaxWidth(width);
		setMaxHeight(height);
		return getMap();
	}
	
	protected double getLatLongRatio()
	{
		double longRange = maxLong - minLong;
		double latRange = maxLat - minLat;
		return (double)(latRange/longRange);
	}
	
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
		else
		{
			// calculate the width from the max height
			maxWidth = (int)(maxHeight*longSpread/latSpread);
			return;
		}
	}
	
	public Image getMap() throws HttpException, IOException
	{
		log.info("Entering MapGrabber.getMap()");
		log.info("maxWidth="+maxWidth+"\tmaxHeight="+maxHeight);
		double longSpread = maxLong - minLong;
		double latSpread = maxLat - minLat;
		log.info("lat spread:  min="+minLat+"\tmax="+maxLat+"\trange="+latSpread);
		log.info("long spread: min="+minLong+"\tmax="+maxLong+"\trange="+longSpread);
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
		if( imageCache != null )
		{
			// check the image cache
			String key = url.toString();
			log.info("Asking cache to grab map image: " + url.toString());
			File imageFile = imageCache.getCacheFile(key);
			if( imageFile == null )
			{
				// the image wasn't in the cache
				// grab it again
				imageCache.cacheWebResource(key,url.toString());
				imageFile = imageCache.getCacheFile(key);
			}
			image = Toolkit.getDefaultToolkit().getImage(imageFile.getAbsolutePath());
		}
		else
		{
			log.info("No image cache available.  Grabbing map internally.");
			GetMethod get = new GetMethod(url.toString());
			get.setFollowRedirects(true);
			int resultCode = httpClient.executeMethod(get);
			log.info("GET " + url.toString() + " returned " + resultCode );
			log.info("Exiting MapGrabber.getMap()");
			byte[] data = get.getResponseBody();
			image = Toolkit.getDefaultToolkit().createImage(data);			
		}
		
		return image;
	}
}
