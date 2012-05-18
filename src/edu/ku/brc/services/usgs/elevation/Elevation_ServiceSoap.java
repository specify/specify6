/**
 * Elevation_ServiceSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package edu.ku.brc.services.usgs.elevation;

public interface Elevation_ServiceSoap extends java.rmi.Remote {

    /**
     * Returns a value from a single elevation source for a given
     * latitude/longitude point
     */
    public edu.ku.brc.services.usgs.elevation.GetElevationResponseGetElevationResult getElevation(java.lang.String x_Value, java.lang.String y_Value, java.lang.String elevation_Units, java.lang.String source_Layer, java.lang.String elevation_Only) throws java.rmi.RemoteException;

    /**
     * Returns values from all elevation sources for a given latitude/longitude
     * point
     */
    public edu.ku.brc.services.usgs.elevation.GetAllElevationsResponseGetAllElevationsResult getAllElevations(java.lang.String x_Value, java.lang.String y_Value, java.lang.String elevation_Units) throws java.rmi.RemoteException;
}
