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

import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "collectionobjectcitation")
@org.hibernate.annotations.Table(appliesTo="collectionobjectcitation", indexes =
    {   
        @Index (name="COCITColMemIDX", columnNames={"CollectionMemberID"})
    })
public class CollectionObjectCitation extends CollectionMember implements java.io.Serializable
{

    // Fields    

     protected Integer          collectionObjectCitationId;
     protected String           remarks;
     protected Boolean          isFigured;
     protected ReferenceWork    referenceWork;
     protected CollectionObject collectionObject;
     
     protected String 			plateNumber;
     protected String			figureNumber;
     protected String		    pageNumber;


    // Constructors

    /** default constructor */
    public CollectionObjectCitation() {
        //
    }
    
    /** constructor with id */
    public CollectionObjectCitation(Integer collectionObjectCitationId) {
        this.collectionObjectCitationId = collectionObjectCitationId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        collectionObjectCitationId = null;
        remarks = null;
        referenceWork = null;
        collectionObject = null;
        plateNumber = null;
        figureNumber = null;
        pageNumber = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "CollectionObjectCitationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getCollectionObjectCitationId() {
        return this.collectionObjectCitationId;
    }

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
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.collectionObjectCitationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CollectionObjectCitation.class;
    }
    
    public void setCollectionObjectCitationId(Integer collectionObjectCitationId) {
        this.collectionObjectCitationId = collectionObjectCitationId;
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

    /**
     *      * The associated reference
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
     *      * Biological Object cited
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionObjectID", unique = false, nullable = false, insertable = true, updatable = true)
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return CollectionObject.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return collectionObject != null ? collectionObject.getId() : null;
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
        return 29;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
    	CollectionObjectCitation obj = (CollectionObjectCitation) super.clone();
    	obj.setCollectionObjectCitationId(null);
    	obj.setCollectionObject(null);
    	return obj;
    }

}
