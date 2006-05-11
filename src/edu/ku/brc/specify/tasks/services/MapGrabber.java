package edu.ku.brc.specify.tasks.services;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MapGrabber
{
	private static Log log = LogFactory.getLog(MapGrabber.class);
	// setup some default values
	// TODO: remove these from any final versions

	protected HttpClient httpClient;
	//protected String host = "129.237.201.104";
    protected String host = "mapus.jpl.nasa.gov";
    //protected String layers = "bmng";
    protected String layers = "global_mosaic";
	protected double minLat = -90;
	protected double minLong = -180;
	protected double maxLat = 90;
	protected double maxLong = 180;
	protected Integer height = null;
	protected Integer width = null;
	protected int defaultHeight = 300;
	protected int defaultWidth = 500;

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
	 * @return Returns the height.
	 */
	public Integer getHeight()
	{
		return height;
	}

	/**
	 * @param height The height to set.
	 */
	public void setPreferredHeight(Integer height)
	{
		this.height = height;
	}

	/**
	 * @return Returns the width.
	 */
	public Integer getWidth()
	{
		return width;
	}

	/**
	 * @param width The width to set.
	 */
	public void setPreferredWidth(Integer width)
	{
		this.width = width;
	}

	/**
	 * @return Returns the defaultHeight.
	 */
	public int getDefaultHeight()
	{
		return defaultHeight;
	}

	/**
	 * @param defaultHeight The defaultHeight to set.
	 */
	public void setDefaultHeight(int defaultHeight)
	{
		this.defaultHeight = defaultHeight;
	}

	/**
	 * @return Returns the defaultWidth.
	 */
	public int getDefaultWidth()
	{
		return defaultWidth;
	}

	/**
	 * @param defaultWidth The defaultWidth to set.
	 */
	public void setDefaultWidth(int defaultWidth)
	{
		this.defaultWidth = defaultWidth;
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
		setPreferredWidth(width);
		setPreferredHeight(height);
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
		setPreferredWidth(width);
		setPreferredHeight(height);
		return getMap();
	}

	protected double getLatLongRatio()
	{
		double longRange = maxLong - minLong;
		double latRange = maxLat - minLat;
		return (double)(latRange/longRange);
	}

	protected void calcHeight()
	{
		height = (int)(width * getLatLongRatio());
	}

	protected void calcWidth()
	{
		width = (int)(height / getLatLongRatio());
	}

	public Image getMap() throws HttpException, IOException
	{
		if( width == null && height == null )
		{
			width = defaultWidth;
			height = defaultHeight;
		}
		else if( width == null )
		{
			calcWidth();
		}
		else if( height == null )
		{
			calcHeight();
		}

		if( width > 2048 )
		{
			width = 2048;
			calcHeight();
		}

		if( height > 2048 )
		{
			height = 2048;
			calcWidth();
		}

		StringBuilder url = new StringBuilder("http://");
		url.append(host);
        url.append("/browse.cgi?wms_server=wms.cgi&srs=EPSG:4326&format=image/jpeg&styles=visual&layers=");
		//url.append("/cgi-bin/ogc.cgi/bmortho?version=1.1.1&service=WMS&Request=GetMap&format=image/gif&styles=default&srs=epsg:4326&layers=");
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

		GetMethod get = new GetMethod(url.toString());
		get.setFollowRedirects(true);
		int resultCode = httpClient.executeMethod(get);
		log.info("GET " + url.toString() + " returned " + resultCode );
		return Toolkit.getDefaultToolkit().createImage(get.getResponseBody());
	}
}
