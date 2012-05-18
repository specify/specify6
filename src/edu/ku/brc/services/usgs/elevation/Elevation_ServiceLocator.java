/**
 * Elevation_ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package edu.ku.brc.services.usgs.elevation;

public class Elevation_ServiceLocator extends org.apache.axis.client.Service implements edu.ku.brc.services.usgs.elevation.Elevation_Service {

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

    public Elevation_ServiceLocator() {
    }


    public Elevation_ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public Elevation_ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for Elevation_ServiceSoap
    private java.lang.String Elevation_ServiceSoap_address = "http://cumulus.cr.usgs.gov/XMLWebServices2/Elevation_Service.asmx";

    public java.lang.String getElevation_ServiceSoapAddress() {
        return Elevation_ServiceSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String Elevation_ServiceSoapWSDDServiceName = "Elevation_ServiceSoap";

    public java.lang.String getElevation_ServiceSoapWSDDServiceName() {
        return Elevation_ServiceSoapWSDDServiceName;
    }

    public void setElevation_ServiceSoapWSDDServiceName(java.lang.String name) {
        Elevation_ServiceSoapWSDDServiceName = name;
    }

    public edu.ku.brc.services.usgs.elevation.Elevation_ServiceSoap getElevation_ServiceSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(Elevation_ServiceSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getElevation_ServiceSoap(endpoint);
    }

    public edu.ku.brc.services.usgs.elevation.Elevation_ServiceSoap getElevation_ServiceSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.ku.brc.services.usgs.elevation.Elevation_ServiceSoapStub _stub = new edu.ku.brc.services.usgs.elevation.Elevation_ServiceSoapStub(portAddress, this);
            _stub.setPortName(getElevation_ServiceSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setElevation_ServiceSoapEndpointAddress(java.lang.String address) {
        Elevation_ServiceSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.ku.brc.services.usgs.elevation.Elevation_ServiceSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.ku.brc.services.usgs.elevation.Elevation_ServiceSoapStub _stub = new edu.ku.brc.services.usgs.elevation.Elevation_ServiceSoapStub(new java.net.URL(Elevation_ServiceSoap_address), this);
                _stub.setPortName(getElevation_ServiceSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("Elevation_ServiceSoap".equals(inputPortName)) {
            return getElevation_ServiceSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://gisdata.usgs.gov/XMLWebServices2/", "Elevation_Service");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://gisdata.usgs.gov/XMLWebServices2/", "Elevation_ServiceSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("Elevation_ServiceSoap".equals(portName)) {
            setElevation_ServiceSoapEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
