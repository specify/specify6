package edu.ku.brc.services.geolocate.prototype;

public class Ellipsoid {
	private String name;
	private double radius, flattening;
	
	public Ellipsoid(String name, double radius, double flattening)
	{
		this.setName(name);
		this.setRadius(radius);
		this.setFlattening(flattening);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getFlattening() {
		return flattening;
	}

	public void setFlattening(double flattening) {
		this.flattening = flattening;
	}
}
