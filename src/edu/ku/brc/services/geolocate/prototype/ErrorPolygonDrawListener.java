package edu.ku.brc.services.geolocate.prototype;

import java.util.EventListener;

public interface ErrorPolygonDrawListener extends EventListener {
	void errorPolygonDrawn(ErrorPolygonDrawEvent evt);
}
