/**
 * GeolocatesvcLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package edu.ku.brc.services.geolocate.prototype.client;

import edu.ku.brc.af.prefs.AppPreferences;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;

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
    //private final static String GEOLOCATE_SERVICE_URL_DEFAULT = "http://www.museum.tulane.edu/webservices/geolocatesvcv2/geolocatesvc.asmx";
    //private final static String GEOLOCATE_SERVICE_URL_DEFAULT = "http://www.geo-locate.org/webservices/legacy/geolocatesvcv2/geolocatesvc.asmx";
    private final static String GEOLOCATE_SERVICE_URL_DEFAULT = "http://www.geo-locate.org/webservices/geolocatesvcv2/geolocatesvc.asmx";
    private final static String GEOLOCATE_SERVICE_URL_PREF = "GEOLOCATE_SERVICE_URL";
    //private final static String GEOLOCATE_NAMESPACE_URL_DEFAULT = "http://www.museum.tulane.edu/webservices/";
    //private final static String GEOLOCATE_NAMESPACE_URL_DEFAULT = "http://www.geo-locate.org/webservices/legacy/";
    private final static String GEOLOCATE_NAMESPACE_URL_DEFAULT = "http://geo-locate.org/webservices/";
    private final static String GEOLOCATE_NAMESPACE_URL_PREF = "GEOLOCATE_NAMESPACE_URL";
    //private final static String GEOLOCATE_WEB_URL_DEFAULT = "http://www.museum.tulane.edu/geolocate/web/WebGeoref.aspx?v=1";
    private final static String GEOLOCATE_WEB_URL_DEFAULT = "http://www.geo-locate.org/web/WebGeoref.aspx?v=1";
    private final static String GEOLOCATE_WEB_URL_PREF = "GEOLOCATE_WEB_URL";
    //private final static String GEOLOCATE_SOAP_URL_DEFAULT = "http://www.museum.tulane.edu/webservices/";
    //private final static String GEOLOCATE_SOAP_URL_DEFAULT = "http://www.geo-locate.org/webservices/legacy/";
    private final static String GEOLOCATE_SOAP_URL_DEFAULT = "http://geo-locate.org/webservices/";
    private final static String GEOLOCATE_SOAP_URL_PREF = "GEOLOCATE_SOAP_URL";
    private static String GEOLOCATE_SERVICE_URL = (AppPreferences.getRemote().get(GEOLOCATE_SERVICE_URL_PREF, GEOLOCATE_SERVICE_URL_DEFAULT));
    private static String GEOLOCATE_NAMESPACE_URL = (AppPreferences.getRemote().get(GEOLOCATE_NAMESPACE_URL_PREF, GEOLOCATE_NAMESPACE_URL_DEFAULT));
    private static String GEOLOCATE_WEB_URL = AppPreferences.getRemote().get(GEOLOCATE_WEB_URL_PREF, GEOLOCATE_WEB_URL_DEFAULT);
    private static String GEOLOCATE_SOAP_URL = AppPreferences.getRemote().get(GEOLOCATE_SOAP_URL_PREF, GEOLOCATE_SOAP_URL_DEFAULT);

    private static boolean[] redirectChecks = {false, false, false, false};

    public static String getGeoLocateServiceURL() {
        GEOLOCATE_SERVICE_URL =  getURL(GEOLOCATE_SERVICE_URL, GEOLOCATE_SERVICE_URL_PREF, 0);
        return GEOLOCATE_SERVICE_URL;
    }
    public static String getGeoLocateNameSpaceURL() {
        GEOLOCATE_NAMESPACE_URL = getURL(GEOLOCATE_NAMESPACE_URL, GEOLOCATE_NAMESPACE_URL_PREF, 1);
        return GEOLOCATE_NAMESPACE_URL;
    }
    public static String getGeoLocateWebURL() {
        GEOLOCATE_WEB_URL = getURL(GEOLOCATE_WEB_URL, GEOLOCATE_WEB_URL_PREF, 2);
        return GEOLOCATE_WEB_URL;
    }
    public static String getGeoLocateSoapURL() {
        GEOLOCATE_SOAP_URL = getURL(GEOLOCATE_SOAP_URL, GEOLOCATE_SOAP_URL_PREF, 3);
        return GEOLOCATE_SOAP_URL;
    }

    private static String getURL(String currentURL, String prefName, int redirectCheckIdx) {
        if (redirectChecks[redirectCheckIdx]) {
            return currentURL;
        } else {
            String newURL = checkForRedirect(currentURL);
            redirectChecks[redirectCheckIdx] = true;
            if (!newURL.equals(currentURL)) {
                AppPreferences.getRemote().put(prefName, newURL);
            }
            return newURL;
        }
    }

    /* we might need something like this to deal redirects for the geolocate services...*/
    private static String checkForRedirect(String url) {

        try {

            //String url = "http://www.twitter.com";
            //String url = GEOLOCATE_SERVICE_URL_DEFAULT;

            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setReadTimeout(3000);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "google.com");

            //System.out.println("Request URL ... " + url);

            boolean redirect = false;

            // normally, 3xx is redirect
            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }

            //System.out.println("Response Code ... " + status);

            if (redirect) {

                // get redirect url from "location" header field
                return conn.getHeaderField("Location");

                // get the cookie if need, for login
                //String cookies = conn.getHeaderField("Set-Cookie");

                // open the new connnection again
//                conn = (HttpURLConnection) new URL(newUrl).openConnection();
//                conn.setRequestProperty("Cookie", cookies);
//                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
//                conn.addRequestProperty("User-Agent", "Mozilla");
//                conn.addRequestProperty("Referer", "google.com");
//
//                System.out.println("Redirect to URL : " + newUrl);

            } else {
                return url;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return url;
        }

    } /**/

    private java.lang.String geolocatesvcSoap_address = getGeoLocateServiceURL();

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
        return new javax.xml.namespace.QName(GeolocatesvcLocator.getGeoLocateNameSpaceURL(), "geolocatesvc");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName(GeolocatesvcLocator.getGeoLocateNameSpaceURL(), "geolocatesvcSoap"));
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
