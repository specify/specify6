
package edu.ku.brc.services.geolocate.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SnapPointToNearestFoundWaterBody2 element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="SnapPointToNearestFoundWaterBody2">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="Country" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="State" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="County" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="LocalityString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="WGS84Latitude" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *           &lt;element name="WGS84Longitude" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "country",
    "state",
    "county",
    "localityString",
    "wgs84Latitude",
    "wgs84Longitude"
})
@XmlRootElement(name = "SnapPointToNearestFoundWaterBody2")
public class SnapPointToNearestFoundWaterBody2 {

    @XmlElement(name = "Country", namespace = "http://www.museum.tulane.edu/webservices/")
    protected String country;
    @XmlElement(name = "State", namespace = "http://www.museum.tulane.edu/webservices/")
    protected String state;
    @XmlElement(name = "County", namespace = "http://www.museum.tulane.edu/webservices/")
    protected String county;
    @XmlElement(name = "LocalityString", namespace = "http://www.museum.tulane.edu/webservices/")
    protected String localityString;
    @XmlElement(name = "WGS84Latitude", namespace = "http://www.museum.tulane.edu/webservices/")
    protected double wgs84Latitude;
    @XmlElement(name = "WGS84Longitude", namespace = "http://www.museum.tulane.edu/webservices/")
    protected double wgs84Longitude;

    /**
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountry(String value) {
        this.country = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setState(String value) {
        this.state = value;
    }

    /**
     * Gets the value of the county property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCounty() {
        return county;
    }

    /**
     * Sets the value of the county property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCounty(String value) {
        this.county = value;
    }

    /**
     * Gets the value of the localityString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalityString() {
        return localityString;
    }

    /**
     * Sets the value of the localityString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalityString(String value) {
        this.localityString = value;
    }

    /**
     * Gets the value of the wgs84Latitude property.
     * 
     */
    public double getWGS84Latitude() {
        return wgs84Latitude;
    }

    /**
     * Sets the value of the wgs84Latitude property.
     * 
     */
    public void setWGS84Latitude(double value) {
        this.wgs84Latitude = value;
    }

    /**
     * Gets the value of the wgs84Longitude property.
     * 
     */
    public double getWGS84Longitude() {
        return wgs84Longitude;
    }

    /**
     * Sets the value of the wgs84Longitude property.
     * 
     */
    public void setWGS84Longitude(double value) {
        this.wgs84Longitude = value;
    }

}
