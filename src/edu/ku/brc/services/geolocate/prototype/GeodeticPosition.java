package edu.ku.brc.services.geolocate.prototype;

public class GeodeticPosition {
	private double dist; //Distance in meters.
	private double fBearing; //Forward bearing (from center to radial) in degrees.
	private double bBaring; //Backward bearing (from radial to center) in degrees.
	
	public GeodeticPosition(double dist, double fBearing, double bBaring)
	{
		this.setDist(dist);
		this.setFBearing(fBearing);
		this.setBBearing(bBaring);
	}

	public double getDist() {
		return dist;
	}

	public void setDist(double dist) {
		this.dist = dist;
	}

	public double getFBearing() {
		return fBearing;
	}

	public void setFBearing(double fBearing) {
		this.fBearing = fBearing;
	}

	public double getBBearing() {
		return bBaring;
	}

	public void setBBearing(double bBearing) {
		this.bBaring = bBearing;
	}
}
