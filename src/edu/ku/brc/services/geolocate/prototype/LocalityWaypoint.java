package edu.ku.brc.services.geolocate.prototype;

import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;

public class LocalityWaypoint extends Waypoint {

    private Locality locality;
    private LocalityWaypoint correctedWaypoint;
    private boolean corrected; 

    public LocalityWaypoint(Locality locality) {
        this.locality = locality;
       correctedWaypoint = null;
    }

    public Locality getLocality() {
        return locality;
    }

    public void setLocality(Locality locality) {
        this.locality = locality;
    }

    public boolean isCorrected() {
        return corrected;
    }

    public void setCorrected(boolean corrected) {
        this.corrected = corrected;
    }

    public LocalityWaypoint getCorrectedWaypoint() {
        return correctedWaypoint;
    }

    public void setCorrectedWaypoint(LocalityWaypoint correctedWaypoint) {
        this.correctedWaypoint = correctedWaypoint;
    }

    @Override
    public GeoPosition getPosition() {
        return new GeoPosition(locality.getLatitude(), locality.getLongitude());
    }
    
    @Override
    public void setPosition(GeoPosition coordinate) {
    	super.setPosition(coordinate);
    	locality.setLatitude(coordinate.getLatitude());
    	locality.setLongitude(coordinate.getLongitude());
    }
    
    @Override
    public Object clone() {
    	Locality clonedLocality = new Locality();
    	clonedLocality.setCorrectionStatus(this.locality.getCorrectionStatus());
    	clonedLocality.setCountry(locality.getCountry());
    	clonedLocality.setState(locality.getState());
    	clonedLocality.setCounty(locality.getCounty());
    	clonedLocality.setLocality(locality.getLocality());
    	clonedLocality.setErrorPolygon(locality.getErrorPolygon());
    	clonedLocality.setLocalityId(locality.getLocalityId());
    	clonedLocality.setLatitude(locality.getLatitude());
    	clonedLocality.setLongitude(locality.getLongitude());
    	clonedLocality.setMultipleResults(locality.getMultipleResults());
    	clonedLocality.setPrecision(locality.getPrecision());
    	clonedLocality.setUncertaintyMeters(locality.getUncertaintyMeters());
    	
    	boolean clonedCorrected = corrected;
    	
    	LocalityWaypoint clonedCorrectedWayPoint = null;
    	if (correctedWaypoint != null)
    		clonedCorrectedWayPoint = (LocalityWaypoint) correctedWaypoint.clone(); //Recursive.
    	
    	LocalityWaypoint clone = new LocalityWaypoint(clonedLocality);
    	clone.setCorrected(clonedCorrected);
    	clone.setCorrectedWaypoint(clonedCorrectedWayPoint);
    	
    	return (Object) clone;
    }
}
