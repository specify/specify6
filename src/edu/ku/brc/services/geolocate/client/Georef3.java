
package edu.ku.brc.services.geolocate.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Georef3 element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="Georef3">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="vLocality" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="vGeography" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="HwyX" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *           &lt;element name="FindWaterbody" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "vLocality",
    "vGeography",
    "hwyX",
    "findWaterbody"
})
@XmlRootElement(name = "Georef3")
public class Georef3 {

    @XmlElement(namespace = "http://www.museum.tulane.edu/webservices/")
    protected String vLocality;
    @XmlElement(namespace = "http://www.museum.tulane.edu/webservices/")
    protected String vGeography;
    @XmlElement(name = "HwyX", namespace = "http://www.museum.tulane.edu/webservices/")
    protected boolean hwyX;
    @XmlElement(name = "FindWaterbody", namespace = "http://www.museum.tulane.edu/webservices/")
    protected boolean findWaterbody;

    /**
     * Gets the value of the vLocality property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVLocality() {
        return vLocality;
    }

    /**
     * Sets the value of the vLocality property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVLocality(String value) {
        this.vLocality = value;
    }

    /**
     * Gets the value of the vGeography property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVGeography() {
        return vGeography;
    }

    /**
     * Sets the value of the vGeography property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVGeography(String value) {
        this.vGeography = value;
    }

    /**
     * Gets the value of the hwyX property.
     * 
     */
    public boolean isHwyX() {
        return hwyX;
    }

    /**
     * Sets the value of the hwyX property.
     * 
     */
    public void setHwyX(boolean value) {
        this.hwyX = value;
    }

    /**
     * Gets the value of the findWaterbody property.
     * 
     */
    public boolean isFindWaterbody() {
        return findWaterbody;
    }

    /**
     * Sets the value of the findWaterbody property.
     * 
     */
    public void setFindWaterbody(boolean value) {
        this.findWaterbody = value;
    }

}
