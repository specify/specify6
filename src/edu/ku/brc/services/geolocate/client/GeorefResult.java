
package edu.ku.brc.services.geolocate.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Georef_Result complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Georef_Result">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="WGS84Coordinate" type="{http://www.museum.tulane.edu/webservices/}GeographicPoint"/>
 *         &lt;element name="ParsePattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Georef_Result", propOrder = {
    "wgs84Coordinate",
    "parsePattern"
})
public class GeorefResult {

    @XmlElement(name = "WGS84Coordinate", namespace = "http://www.museum.tulane.edu/webservices/", required = true)
    protected GeographicPoint wgs84Coordinate;
    @XmlElement(name = "ParsePattern", namespace = "http://www.museum.tulane.edu/webservices/")
    protected String parsePattern;

    /**
     * Gets the value of the wgs84Coordinate property.
     * 
     * @return
     *     possible object is
     *     {@link GeographicPoint }
     *     
     */
    public GeographicPoint getWGS84Coordinate() {
        return wgs84Coordinate;
    }

    /**
     * Sets the value of the wgs84Coordinate property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeographicPoint }
     *     
     */
    public void setWGS84Coordinate(GeographicPoint value) {
        this.wgs84Coordinate = value;
    }

    /**
     * Gets the value of the parsePattern property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParsePattern() {
        return parsePattern;
    }

    /**
     * Sets the value of the parsePattern property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParsePattern(String value) {
        this.parsePattern = value;
    }

}
