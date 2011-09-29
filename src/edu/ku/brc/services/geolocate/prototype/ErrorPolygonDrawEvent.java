package edu.ku.brc.services.geolocate.prototype;

import java.util.EventObject;
import java.util.List;

import org.jdesktop.swingx.mapviewer.GeoPosition;

@SuppressWarnings("serial")
public class ErrorPolygonDrawEvent extends EventObject {
	private List<GeoPosition> vertices;
	public ErrorPolygonDrawEvent(Object source, List<GeoPosition> vertices) {
		super(source);
		this.vertices = vertices;
	}
	public List<GeoPosition> getVertices() {
		return vertices;
	}
}
