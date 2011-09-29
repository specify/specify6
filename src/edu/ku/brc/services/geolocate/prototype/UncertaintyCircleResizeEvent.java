package edu.ku.brc.services.geolocate.prototype;

import java.util.EventObject;

@SuppressWarnings("serial")
public class UncertaintyCircleResizeEvent extends EventObject {
	private LocalityWaypoint centerWayPoint;
	public UncertaintyCircleResizeEvent(Object source, LocalityWaypoint centerWayPoint) {
		super(source);
		this.centerWayPoint = centerWayPoint;
	}
	public LocalityWaypoint getCenterWayPoint() {
		return centerWayPoint;
	}
}
