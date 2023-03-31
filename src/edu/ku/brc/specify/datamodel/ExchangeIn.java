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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "exchangein")
@org.hibernate.annotations.Table(appliesTo="exchangein", indexes =
    {   @Index (name="ExchangeDateIDX", columnNames={"ExchangeDate"}),
		@Index (name="DescriptionOfMaterialIDX", columnNames={"DescriptionOfMaterial"})
    })
@SuppressWarnings("serial")
public class ExchangeIn extends DataModelObjBase implements java.io.Serializable, AttachmentOwnerIFace<ExchangeInAttachment>  {

    // Fields    

    protected Integer         exchangeInId;
    protected String          exchangeInNumber;
    protected Calendar        exchangeDate;
    protected Short           quantityExchanged;
    protected String          descriptionOfMaterial;
    
    protected String          srcGeography;
    protected String          srcTaxonomy;
    
    protected String          remarks;
    protected String				  contents;
    protected String          text1;
    protected String          text2;
    protected BigDecimal           number1;
    protected BigDecimal           number2;
    protected Boolean         yesNo1;
    protected Boolean         yesNo2;

    protected AddressOfRecord addressOfRecord;
    protected Agent           agentReceivedFrom;
    protected Agent           agentCatalogedBy;
    protected Division        division;
    protected Set<ExchangeInPrep> exchangeInPreps;
    protected Set<ExchangeInAttachment> exchangeInAttachments;

    // Constructors

    /** default constructor */
    public ExchangeIn() {
        //
    }
    
    /** constructor with id */
    public ExchangeIn(Integer exchangeInId) {
        this.exchangeInId = exchangeInId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        exchangeInId = null;
        exchangeInNumber = null;
        exchangeDate = null;
        quantityExchanged = null;
        descriptionOfMaterial = null;
        srcGeography     = null;
        srcTaxonomy      = null;
        remarks = null;
        contents        = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        agentReceivedFrom = null;
        agentCatalogedBy  = null;
        addressOfRecord   = null;
        division          = null;
        exchangeInPreps = new HashSet<ExchangeInPrep>();
        exchangeInAttachments = new HashSet<>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "ExchangeInID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getExchangeInId() {
        return this.exchangeInId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.exchangeInId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return ExchangeIn.class;
    }
    
    public void setExchangeInId(Integer exchangeInId) {
        this.exchangeInId = exchangeInId;
    }

    /**
     *      * Invoice number
     */
    @Column(name = "ExchangeInNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getExchangeInNumber() {
        return this.exchangeInNumber;
    }
    
    public void setExchangeInNumber(String exchangeInNumber) {
        this.exchangeInNumber = exchangeInNumber;
    }

    /**
     *      * Date exchange was received
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "ExchangeDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getExchangeDate() {
        return this.exchangeDate;
    }
    
    public void setExchangeDate(Calendar exchangeDate) {
        this.exchangeDate = exchangeDate;
    }

    /**
     *      * Number of items received
     */
    @Column(name = "QuantityExchanged", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getQuantityExchanged() {
        return this.quantityExchanged;
    }
    
    public void setQuantityExchanged(Short quantityExchanged) {
        this.quantityExchanged = quantityExchanged;
    }

    /**
     * 
     */
    @Column(name = "DescriptionOfMaterial", unique = false, nullable = true, insertable = true, updatable = true, length = 120)
    public String getDescriptionOfMaterial() {
        return this.descriptionOfMaterial;
    }
    
    public void setDescriptionOfMaterial(String descriptionOfMaterial) {
        this.descriptionOfMaterial = descriptionOfMaterial;
    }

    /**
     * @return the srcGeography
     */
    @Column(name = "SrcGeography", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getSrcGeography()
    {
        return srcGeography;
    }

    /**
     * @param srcGeography the srcGeography to set
     */
    public void setSrcGeography(String srcGeography)
    {
        this.srcGeography = srcGeography;
    }

    /**
     * @return the srcTaxonomy
     */
    @Column(name = "SrcTaxonomy", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getSrcTaxonomy()
    {
        return srcTaxonomy;
    }

    /**
     * @param srcTaxonomy the srcTaxonomy to set
     */
    public void setSrcTaxonomy(String srcTaxonomy)
    {
        this.srcTaxonomy = srcTaxonomy;
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
     * 
     */
    @Lob
    @Column(name = "Contents", length = 4096)
    public String getContents() {
        return this.contents;
    }
    
    /**
     * @param contents
     */
    public void setContents(String contents) {
        this.contents = contents;
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
     *      * Agent ID of organization that sent material
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ReceivedFromOrganizationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getAgentReceivedFrom() {
        return this.agentReceivedFrom;
    }
    
    public void setAgentReceivedFrom(Agent agentReceivedFrom) {
        this.agentReceivedFrom = agentReceivedFrom;
    }

    /**
     *      * Agent ID of person recording the exchange
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CatalogedByID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getAgentCatalogedBy() {
        return this.agentCatalogedBy;
    }
    
    public void setAgentCatalogedBy(Agent agentCatalogedBy) {
        this.agentCatalogedBy = agentCatalogedBy;
    }

    /**
     * @return the addressOfRecord
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AddressOfRecordID", unique = false, nullable = true, insertable = true, updatable = true)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public AddressOfRecord getAddressOfRecord()
    {
        return addressOfRecord;
    }

    /**
     * @param addressOfRecord the addressOfRecord to set
     */
    public void setAddressOfRecord(AddressOfRecord addressOfRecord)
    {
        this.addressOfRecord = addressOfRecord;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "exchangeIn")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<ExchangeInPrep> getExchangeInPreps() {
        return this.exchangeInPreps;
    }
    
    public void setExchangeInPreps(Set<ExchangeInPrep> exchangeInPreps) {
        this.exchangeInPreps = exchangeInPreps;
    }
    
    @OneToMany(mappedBy = "exchangeIn")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<ExchangeInAttachment> getExchangeInAttachments()
    {
        return exchangeInAttachments;
    }

    public void setExchangeInAttachments(Set<ExchangeInAttachment> exchangeInAttachments)
    {
        this.exchangeInAttachments = exchangeInAttachments;
    }

    /**
     * @return the division
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DivisionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Division getDivision()
    {
        return division;
    }

    /**
     * @param division the division to set
     */
    public void setDivision(Division division)
    {
        this.division = division;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Division.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return division != null ? division.getId() : null;
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
        return 39;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentTableId()
     */
    @Override
    @Transient
    public int getAttachmentTableId()
    {
        return getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
    @Transient
    public Set<ExchangeInAttachment> getAttachmentReferences()
    {
        return exchangeInAttachments;
    }

}
