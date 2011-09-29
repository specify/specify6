package edu.ku.brc.services.geolocate.prototype;

import java.util.EventListener;

public interface MapPointerMoveListener extends EventListener {
	void mapPointerMoved(MapPointerMoveEvent evt);

}
