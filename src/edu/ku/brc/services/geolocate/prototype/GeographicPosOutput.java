package edu.ku.brc.services.geolocate.prototype;

public class GeographicPosOutput {
	private double gLat; //Geographic latitude.
	private double gLon; //Geographic Longitude.
	private double az; //Azimuth.
	
	public GeographicPosOutput(double gLat, double gLon, double az)
	{
		this.setGLat(gLat);
		this.setGLon(gLon);
		this.setAz(az);
	}

	public double getGLat() {
		return gLat;
	}

	public void setGLat(double gLat) {
		this.gLat = gLat;
	}

	public double getGLon() {
		return gLon;
	}

	public void setGLon(double gLon) {
		this.gLon = gLon;
	}

	public double getAz() {
		return az;
	}

	public void setAz(double az) {
		this.az = az;
	}

}