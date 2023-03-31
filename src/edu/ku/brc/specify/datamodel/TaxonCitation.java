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

import java.math.BigDecimal;

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

/**

 */
@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "taxoncitation")
public class TaxonCitation extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Integer taxonCitationId;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected BigDecimal number1;
     protected BigDecimal number2;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected ReferenceWork referenceWork;
     protected Taxon taxon;
    protected Boolean isFigured;
    protected String 			plateNumber;
    protected String			figureNumber;
    protected String		    pageNumber;


    // Constructors

    /** default constructor */
    public TaxonCitation() {
        //
    }
    
    /** constructor with id */
    public TaxonCitation(Integer taxonCitationId) {
        this.taxonCitationId = taxonCitationId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        taxonCitationId = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        referenceWork = null;
        taxon = null;
        isFigured = null;
        plateNumber = null;
        figureNumber = null;
        pageNumber = null;

    }
    // End Initializer

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

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "TaxonCitationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getTaxonCitationId() {
        return this.taxonCitationId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.taxonCitationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return TaxonCitation.class;
    }
    
    public void setTaxonCitationId(Integer taxonCitationId) {
        this.taxonCitationId = taxonCitationId;
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
     *      * User definable
     */
    @Lob
    @Column(name = "Text1", length = 65535)
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text2", length = 65535)
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 24, precision = 20, scale = 10)
    public BigDecimal getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(BigDecimal number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 24, precision = 20, scale = 10)
    public BigDecimal getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(BigDecimal number2) {
        this.number2 = number2;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *      * The ID of reference work that cites the taxon name
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
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "TaxonID", unique = false, nullable = false, insertable = true, updatable = true)
    public Taxon getTaxon() {
        return this.taxon;
    }
    
    public void setTaxon(Taxon taxon) {
        this.taxon = taxon;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Taxon.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return taxon != null ? taxon.getId() : null;
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
        return 75;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
    	TaxonCitation obj = (TaxonCitation) super.clone();
    	obj.setTaxonCitationId(null);
    	obj.setTaxon(null);
    	return obj;
    }

}
