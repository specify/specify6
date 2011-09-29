package edu.ku.brc.services.geolocate.prototype;

import java.util.EventObject;
import org.jdesktop.swingx.mapviewer.GeoPosition;

@SuppressWarnings("serial")
public class MapPointerMoveEvent extends EventObject {
	private GeoPosition location;
	
	public MapPointerMoveEvent(Object source, GeoPosition location) {
		super(source);
		this.location = location;
	}
	
	public GeoPosition getLocation() {
		return location;
	}

}
