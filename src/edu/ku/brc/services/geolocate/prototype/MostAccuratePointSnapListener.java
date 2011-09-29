package edu.ku.brc.services.geolocate.prototype;

import java.util.EventListener;

public interface MostAccuratePointSnapListener extends EventListener {
	void mostAccuratePointSnapped(MapPointerMoveEvent evt);
}
