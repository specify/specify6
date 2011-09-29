package edu.ku.brc.services.geolocate.prototype;

import java.util.EventListener;

public interface UncertaintyCircleChangeListener extends EventListener {
	void uncertaintyCircleChanged(UncertaintyCircleChangeEvent evt);
}
