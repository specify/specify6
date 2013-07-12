/* Copyright (C) 2013, University of Kansas Center for Research
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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Georef_Result_Set complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Georef_Result_Set">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="NumResults" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ExectutionTimems" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="ResultSet" type="{http://www.museum.tulane.edu/webservices/}Georef_Result" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Georef_Result_Set", propOrder = { //$NON-NLS-1$
    "numResults", //$NON-NLS-1$
    "exectutionTimems", //$NON-NLS-1$
    "resultSet" //$NON-NLS-1$
})
public class GeorefResultSet {

    @XmlElement(name = "NumResults", namespace = "http://www.museum.tulane.edu/webservices/") //$NON-NLS-1$ //$NON-NLS-2$
    protected int numResults;
    @XmlElement(name = "ExectutionTimems", namespace = "http://www.museum.tulane.edu/webservices/") //$NON-NLS-1$ //$NON-NLS-2$
    protected double exectutionTimems;
    @XmlElement(name = "ResultSet", namespace = "http://www.museum.tulane.edu/webservices/", required = true) //$NON-NLS-1$ //$NON-NLS-2$
    protected List<GeorefResult> resultSet;

    /**
     * Gets the value of the numResults property.
     * 
     */
    public int getNumResults() {
        return numResults;
    }

    /**
     * Sets the value of the numResults property.
     * 
     */
    public void setNumResults(int value) {
        this.numResults = value;
    }

    /**
     * Gets the value of the exectutionTimems property.
     * 
     */
    public double getExectutionTimems() {
        return exectutionTimems;
    }

    /**
     * Sets the value of the exectutionTimems property.
     * 
     */
    public void setExectutionTimems(double value) {
        this.exectutionTimems = value;
    }

    /**
     * Gets the value of the resultSet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resultSet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResultSet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GeorefResult }
     * 
     * 
     */
    public List<GeorefResult> getResultSet() {
        if (resultSet == null) {
            resultSet = new ArrayList<GeorefResult>();
        }
        return this.resultSet;
    }

}
