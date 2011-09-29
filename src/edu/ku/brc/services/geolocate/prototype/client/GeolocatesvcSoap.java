/**
 * GeolocatesvcSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package edu.ku.brc.services.geolocate.prototype.client;

public interface GeolocatesvcSoap extends java.rmi.Remote {

    /**
     * returns the names of all waterbodies found with a locality
     * description. <br>*U.S. localities only, county required*
     */
    public java.lang.String[] findWaterBodiesWithinLocality(edu.ku.brc.services.geolocate.prototype.client.LocalityDescription localityDescription) throws java.rmi.RemoteException;

    /**
     * Georeferences a locality description. returns a 'Georef_Result_Set'
     * given a 'LocalityDescription' and boolean georeferencing options.
     * <br>Language key refers to an integer representing different languages
     * libraries. Will default to basic english (0) if invalid key is provided.
     * <br>*GLOBAL*
     */
    public edu.ku.brc.services.geolocate.prototype.client.Georef_Result_Set georef(edu.ku.brc.services.geolocate.prototype.client.LocalityDescription localityDescription, boolean hwyX, boolean findWaterbody, boolean restrictToLowestAdm, boolean doUncert, boolean doPoly, boolean displacePoly, boolean polyAsLinkID, int languageKey) throws java.rmi.RemoteException;

    /**
     * Georeferences a locality description. returns a 'Georef_Result_Set'
     * given Country, State, County, LocalityString and boolean georeferencing
     * options.  <br><b>Use this one if you are unsure of which to use.</b>
     * <br>Language key refers to an integer representing different languages
     * libraries. Will default to basic english (languagekey=0) if invalid
     * key is provided. <br>*GLOBAL*
     */
    public edu.ku.brc.services.geolocate.prototype.client.Georef_Result_Set georef2(java.lang.String country, java.lang.String state, java.lang.String county, java.lang.String localityString, boolean hwyX, boolean findWaterbody, boolean restrictToLowestAdm, boolean doUncert, boolean doPoly, boolean displacePoly, boolean polyAsLinkID, int languageKey) throws java.rmi.RemoteException;

    /**
     * Georeferences a locality description. returns a 'Georef_Result_Set'
     * given vLocality, vGeorgraphy and boolean georeferencing options. 
     * VLocality and VGeogrpahy are fields specific to BioGeomancer. <br>Language
     * key refers to an integer representing different languages libraries.
     * Will default to basic english (0) if invalid key is provided <br>*North
     * American Localities Only*
     */
    public edu.ku.brc.services.geolocate.prototype.client.Georef_Result_Set georef3(java.lang.String vLocality, java.lang.String vGeography, boolean hwyX, boolean findWaterbody, boolean restrictToLowestAdm, boolean doUncert, boolean doPoly, boolean displacePoly, boolean polyAsLinkID, int languageKey) throws java.rmi.RemoteException;

    /**
     * Snaps given point to nearest water body found from given locality
     * description. <br>*U.S. localities only, county required*
     */
    public void snapPointToNearestFoundWaterBody(edu.ku.brc.services.geolocate.prototype.client.LocalityDescription localityDescription, edu.ku.brc.services.geolocate.prototype.client.holders.GeographicPointHolder WGS84Coordinate) throws java.rmi.RemoteException;

    /**
     * Snaps given point to nearest water body found from given locality
     * description terms. <br>*U.S. localities only, county required*
     */
    public edu.ku.brc.services.geolocate.prototype.client.GeographicPoint snapPointToNearestFoundWaterBody2(java.lang.String country, java.lang.String state, java.lang.String county, java.lang.String localityString, double WGS84Latitude, double WGS84Longitude) throws java.rmi.RemoteException;

    /**
     * Returns an uncertainty polygon given the unique id used to
     * generate it.
     */
    public java.lang.String calcUncertaintyPoly(java.lang.String polyGenerationKey) throws java.rmi.RemoteException;

    /**
     * Georeferences a locality description. returns a 'Georef_Result_Set'
     * given Country, State, County, LocalityString and boolean georeferencing
     * options.  Also adds results from Biogeomancer to the mix. May take
     * a long time to get results back from BG. <br>Language key refers to
     * an integer representing different languages libraries. Will default
     * to basic english (languagekey=0) if invalid key is provided. <br>*GLOBAL*
     */
    public edu.ku.brc.services.geolocate.prototype.client.Georef_Result_Set georef2PlusBG(java.lang.String country, java.lang.String state, java.lang.String county, java.lang.String localityString, boolean hwyX, boolean findWaterbody, boolean restrictToLowestAdm, boolean doUncert, boolean doPoly, boolean displacePoly, boolean polyAsLinkID, int languageKey) throws java.rmi.RemoteException;
}
