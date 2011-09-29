package edu.ku.brc.services.geolocate.prototype;

import java.util.EventListener;

public interface MostAccuratePointReleaseListener extends EventListener {
	void mostAccuratePointReleased(MapPointerMoveEvent evt);
}
