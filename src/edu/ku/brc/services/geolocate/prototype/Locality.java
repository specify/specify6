package edu.ku.brc.services.geolocate.prototype;

public class Locality {
	private String locality;
    private String country;
    private String state;
    private String county;
    private double longitude;
    private double latitude;
    private String correctionStatus;
    private String precision;
    private int score;
    private String errorPolygon;
    private String uncertaintyMeters;
    private String multipleResults;
    private String localityId;

    public String getCorrectionStatus() {
        return correctionStatus;
    }

    public void setCorrectionStatus(String correctionStatus) {
        this.correctionStatus = correctionStatus;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getErrorPolygon() {
        return errorPolygon;
    }

    public void setErrorPolygon(String errorPolygon) {
        this.errorPolygon = errorPolygon;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getLocalityId() {
        return localityId;
    }

    public void setLocalityId(String localityId) {
        this.localityId = localityId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getMultipleResults() {
        return multipleResults;
    }

    public void setMultipleResults(String multipleResults) {
        this.multipleResults = multipleResults;
    }

    public String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

	public String getUncertaintyMeters() {
		return uncertaintyMeters;
	}

	public void setUncertaintyMeters(String uncertaintyMeters) {
		this.uncertaintyMeters = uncertaintyMeters;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
}
