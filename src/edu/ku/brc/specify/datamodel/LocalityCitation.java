/* Copyright (C) 2022, Specify Collections Consortium
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
package edu.ku.brc.specify.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "localitycitation", uniqueConstraints = { 
        @UniqueConstraint(columnNames = { "ReferenceWorkID", "LocalityID" }) 
        })
@org.hibernate.annotations.Table(appliesTo="localitycitation", indexes =
    {   @Index (name="LocCitDspMemIDX", columnNames={"DisciplineID"})
    })
public class LocalityCitation extends DisciplineMember implements java.io.Serializable, Cloneable
{

    // Fields    

     protected Integer       localityCitationId;
     protected String        remarks;
     protected ReferenceWork referenceWork;
     protected Locality      locality;
    protected Boolean isFigured;
    protected String 			plateNumber;
    protected String			figureNumber;
    protected String		    pageNumber;


    // Constructors

    /** default constructor */
    public LocalityCitation() {
        //
    }
    
    /** constructor with id */
    public LocalityCitation(Integer localityCitationId) {
        this.localityCitationId = localityCitationId;
    }
   

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        localityCitationId = null;
        remarks = null;
        referenceWork = null;
        locality = null;
        isFigured = null;
        plateNumber = null;
        figureNumber = null;
        pageNumber = null;
    }
    // End Initializer

    // Property accessors
    /**
     * @return the isFigured
     */
    @Column(name = "IsFigured", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsFigured()
    {
        return isFigured;
    }

    /**
     * @param isFigured the isFigured to set
     */
    public void setIsFigured(Boolean isFigured)
    {
        this.isFigured = isFigured;
    }

    /**
     * @return the plateNumber
     */
    @Column(name = "PlateNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getPlateNumber() {
        return plateNumber;
    }

    /**
     * @param plateNumber the plateNumber to set
     */
    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    /**
     * @return the figureNumber
     */
    @Column(name = "FigureNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getFigureNumber() {
        return figureNumber;
    }

    /**
     * @param figureNumber the figureNumber to set
     */
    public void setFigureNumber(String figureNumber) {
        this.figureNumber = figureNumber;
    }

    /**
     * @return the pageNumber
     */
    @Column(name = "PageNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getPageNumber() {
        return pageNumber;
    }

    /**
     * @param pageNumber the pageNumber to set
     */
    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "LocalityCitationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getLocalityCitationId() {
        return this.localityCitationId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.localityCitationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return LocalityCitation.class;
    }
    
    public void setLocalityCitationId(Integer localityCitationId) {
        this.localityCitationId = localityCitationId;
    }

    /**
     * 
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        LocalityCitation lc = (LocalityCitation)super.clone();
        localityCitationId = null;
        
        return lc;
    }

    /**
     *      * ID of work citing locality
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ReferenceWorkID", unique = false, nullable = false, insertable = true, updatable = true)
    public ReferenceWork getReferenceWork() {
        return this.referenceWork;
    }
    
    public void setReferenceWork(ReferenceWork referenceWork) {
        this.referenceWork = referenceWork;
    }

    /**
     *      * ID of locality cited
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LocalityID", unique = false, nullable = false, insertable = true, updatable = true)
    public Locality getLocality() {
        return this.locality;
    }
    
    public void setLocality(Locality locality) {
        this.locality = locality;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Locality.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return locality != null ? locality.getId() : null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 57;
    }

}
