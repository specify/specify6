package edu.ku.brc.services.geolocate.prototype;

import java.util.EventListener;

public interface UncertaintyCircleResizeListener extends EventListener {
	void uncertaintyCircleResized(UncertaintyCircleResizeEvent evt);
}
