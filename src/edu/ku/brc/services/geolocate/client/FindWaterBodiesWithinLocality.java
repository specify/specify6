/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
 * <p>Java class for FindWaterBodiesWithinLocality element declaration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;element name="FindWaterBodiesWithinLocality">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="LocalityDescription" type="{http://www.museum.tulane.edu/webservices/}LocalityDescription"/>
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
    "localityDescription" //$NON-NLS-1$
})
@XmlRootElement(name = "FindWaterBodiesWithinLocality") //$NON-NLS-1$
public class FindWaterBodiesWithinLocality {

    @XmlElement(name = "LocalityDescription", namespace = "http://www.museum.tulane.edu/webservices/", required = true) //$NON-NLS-1$ //$NON-NLS-2$
    protected LocalityDescription localityDescription;

    /**
     * Gets the value of the localityDescription property.
     * 
     * @return
     *     possible object is
     *     {@link LocalityDescription }
     *     
     */
    public LocalityDescription getLocalityDescription() {
        return localityDescription;
    }

    /**
     * Sets the value of the localityDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocalityDescription }
     *     
     */
    public void setLocalityDescription(LocalityDescription value) {
        this.localityDescription = value;
    }

}
