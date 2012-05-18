/**
 * Elevation_Service.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package edu.ku.brc.services.usgs.elevation;

public interface Elevation_Service extends javax.xml.rpc.Service {

/**
 * The Elevation Query Web Service returns the elevation in feet or
 * meters for a specific latitutde/longitude (WGS 1984) point from the
 * USGS Seamless Elevation datasets hosted at <a href="http://eros.usgs.gov/">EROS</a>.
 * The elevation values returned default to the best-available data source
 * available at the specified point.  Alternately, this service may return
 * the value from a specified data source, or from all data sources.
 * If unable to find data at the requested point, this service returns
 * an extremely large, negative value (-1.79769313486231E+308).  View
 * the detailed <a href="/XMLWebServices2/Elevation_Service_Methods.php">Elevation
 * Service Methods</a> description for more information on the methods
 * and parameters used in this service.  Visit <a href="http://gisdata.usgs.gov/">http://gisdata.usgs.gov/</a>
 * to view other EROS Web Services.
 */
    public java.lang.String getElevation_ServiceSoapAddress();

    public edu.ku.brc.services.usgs.elevation.Elevation_ServiceSoap getElevation_ServiceSoap() throws javax.xml.rpc.ServiceException;

    public edu.ku.brc.services.usgs.elevation.Elevation_ServiceSoap getElevation_ServiceSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
