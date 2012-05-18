package edu.ku.brc.services.geolocate.prototype;

import java.util.EventListener;

public interface MeasureDistanceListener extends EventListener {
	void distanceMeasured(MeasureDistanceEvent evt);
}
