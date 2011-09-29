package edu.ku.brc.services.geolocate.prototype;

import java.util.EventObject;

@SuppressWarnings("serial")
public class UncertaintyCircleChangeEvent extends EventObject {
	private long uncertaintyRadiusInMeters;
	public UncertaintyCircleChangeEvent(Object source, long uncertaintyRadiusInMeters) {
		super(source);
		this.uncertaintyRadiusInMeters = uncertaintyRadiusInMeters;
	}
	public long getUncertaintyRadiusInMeters() {
		return uncertaintyRadiusInMeters;
	}
}
