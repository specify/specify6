/**
 * GeolocatesvcLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package edu.ku.brc.services.geolocate.prototype.client;

public class GeolocatesvcLocator extends org.apache.axis.client.Service implements edu.ku.brc.services.geolocate.prototype.client.Geolocatesvc {

    public GeolocatesvcLocator() {
    }


    public GeolocatesvcLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GeolocatesvcLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for geolocatesvcSoap
    private java.lang.String geolocatesvcSoap_address = "http://www.museum.tulane.edu/webservices/geolocatesvcv2/geolocatesvc.asmx";

    public java.lang.String getgeolocatesvcSoapAddress() {
        return geolocatesvcSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String geolocatesvcSoapWSDDServiceName = "geolocatesvcSoap";

    public java.lang.String getgeolocatesvcSoapWSDDServiceName() {
        return geolocatesvcSoapWSDDServiceName;
    }

    public void setgeolocatesvcSoapWSDDServiceName(java.lang.String name) {
        geolocatesvcSoapWSDDServiceName = name;
    }

    public edu.ku.brc.services.geolocate.prototype.client.GeolocatesvcSoap getgeolocatesvcSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(geolocatesvcSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getgeolocatesvcSoap(endpoint);
    }

    public edu.ku.brc.services.geolocate.prototype.client.GeolocatesvcSoap getgeolocatesvcSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.ku.brc.services.geolocate.prototype.client.GeolocatesvcSoapStub _stub = new edu.ku.brc.services.geolocate.prototype.client.GeolocatesvcSoapStub(portAddress, this);
            _stub.setPortName(getgeolocatesvcSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setgeolocatesvcSoapEndpointAddress(java.lang.String address) {
        geolocatesvcSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.ku.brc.services.geolocate.prototype.client.GeolocatesvcSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.ku.brc.services.geolocate.prototype.client.GeolocatesvcSoapStub _stub = new edu.ku.brc.services.geolocate.prototype.client.GeolocatesvcSoapStub(new java.net.URL(geolocatesvcSoap_address), this);
                _stub.setPortName(getgeolocatesvcSoapWSDDServiceName());
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
        if ("geolocatesvcSoap".equals(inputPortName)) {
            return getgeolocatesvcSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "geolocatesvc");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "geolocatesvcSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("geolocatesvcSoap".equals(portName)) {
            setgeolocatesvcSoapEndpointAddress(address);
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
