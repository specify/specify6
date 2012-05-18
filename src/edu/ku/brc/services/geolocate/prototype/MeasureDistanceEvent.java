package edu.ku.brc.services.geolocate.prototype;

import java.util.EventObject;
import java.util.List;

import org.jdesktop.swingx.mapviewer.GeoPosition;

@SuppressWarnings("serial")
public class MeasureDistanceEvent extends EventObject {
	private List<GeoPosition> segments;
	public MeasureDistanceEvent(Object source, List<GeoPosition> segments) {
		super(source);
		this.segments = segments;
	}
	public List<GeoPosition> getSegments() {
		return segments;
	}
}
