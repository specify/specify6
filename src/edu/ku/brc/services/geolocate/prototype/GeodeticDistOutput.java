package edu.ku.brc.services.geolocate.prototype;

public class GeodeticDistOutput {
	
	private double nmDist; //Distance in nautical miles.
	private double faz; //Forward azimuth;
	private double baz; //Backward azimuth;
	
	public GeodeticDistOutput(double nmDist, double faz, double baz)
	{
		this.setNmDist(nmDist);
		this.setFaz(faz);
		this.setBaz(baz);
	}

	public double getNmDist() {
		return nmDist;
	}

	public void setNmDist(double nmDist) {
		this.nmDist = nmDist;
	}

	public double getFaz() {
		return faz;
	}

	public void setFaz(double faz) {
		this.faz = faz;
	}

	public double getBaz() {
		return baz;
	}

	public void setBaz(double baz) {
		this.baz = baz;
	}
}
