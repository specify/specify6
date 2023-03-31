/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.services.geolocate.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Georef2 element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="Georef2">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="Country" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="State" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="County" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="LocalityString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "", propOrder = { //$NON-NLS-1$
    "country", //$NON-NLS-1$
    "state", //$NON-NLS-1$
    "county", //$NON-NLS-1$
    "localityString", //$NON-NLS-1$
    "hwyX", //$NON-NLS-1$
    "findWaterbody" //$NON-NLS-1$
})
@XmlRootElement(name = "Georef2") //$NON-NLS-1$
public class Georef2 {

    @XmlElement(name = "Country", namespace = "http://www.museum.tulane.edu/webservices/") //$NON-NLS-1$ //$NON-NLS-2$
    protected String country;
    @XmlElement(name = "State", namespace = "http://www.museum.tulane.edu/webservices/") //$NON-NLS-1$ //$NON-NLS-2$
    protected String state;
    @XmlElement(name = "County", namespace = "http://www.museum.tulane.edu/webservices/") //$NON-NLS-1$ //$NON-NLS-2$
    protected String county;
    @XmlElement(name = "LocalityString", namespace = "http://www.museum.tulane.edu/webservices/") //$NON-NLS-1$ //$NON-NLS-2$
    protected String localityString;
    @XmlElement(name = "HwyX", namespace = "http://www.museum.tulane.edu/webservices/") //$NON-NLS-1$ //$NON-NLS-2$
    protected boolean hwyX;
    @XmlElement(name = "FindWaterbody", namespace = "http://www.museum.tulane.edu/webservices/") //$NON-NLS-1$ //$NON-NLS-2$
    protected boolean findWaterbody;

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
