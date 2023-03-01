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

import java.util.*;
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

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.tasks.InteractionsTask;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "exchangeout")
@org.hibernate.annotations.Table(appliesTo="exchangeout", indexes =
    {   @Index (name="ExchangeOutdateIDX", columnNames={"ExchangeDate"}),
		@Index (name="DescriptionOfMaterialIDX2", columnNames={"DescriptionOfMaterial"}),
        @Index (name="ExchangeOutNumberIDX", columnNames={"ExchangeOutNumber"})
    })
@SuppressWarnings("serial")
public class ExchangeOut extends DataModelObjBase implements java.io.Serializable, OneToManyProviderIFace, AttachmentOwnerIFace<ExchangeOutAttachment> {
    private static final Logger log = Logger.getLogger(ExchangeOut.class);
    // Fields

    protected Integer         exchangeOutId;
    protected String          exchangeOutNumber;
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
    protected Agent           agentSentTo;
    protected Agent           agentCatalogedBy;
    protected Set<Shipment>   shipments;
    protected Division        division;
    protected Set<ExchangeOutPrep> exchangeOutPreps;
    protected Deaccession            deaccession;
    protected Set<ExchangeOutAttachment> exchangeOutAttachments;


    // Constructors

    /** default constructor */
    public ExchangeOut() {
        //
    }
    
    /** constructor with id */
    public ExchangeOut(Integer exchangeOutId) {
        this.exchangeOutId = exchangeOutId;
    }
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        exchangeOutId    = null;
        exchangeOutNumber = null;
        exchangeDate     = null;
        quantityExchanged = null;
        descriptionOfMaterial = null;
        srcGeography     = null;
        srcTaxonomy      = null;
        remarks          = null;
        contents         = null;
        text1            = null;
        text2            = null;
        number1          = null;
        number2          = null;
        yesNo1           = null;
        yesNo2           = null;
        addressOfRecord  = null;
        agentSentTo      = null;
        agentCatalogedBy = null;
        shipments        = new HashSet<Shipment>();
        exchangeOutPreps = new HashSet<ExchangeOutPrep>();
        exchangeOutAttachments = new HashSet<>();

        division         = null;
        deaccession = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "ExchangeOutID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getExchangeOutId()
    {
        return this.exchangeOutId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.exchangeOutId;
    }

    /**
     *      * Invoice number
     */
    @Column(name = "ExchangeOutNumber", unique = false, nullable = false, insertable = true, updatable = true, length = 50)
    public String getExchangeOutNumber() {
        return this.exchangeOutNumber;
    }
    
    public void setExchangeOutNumber(String exchangeOutNumber) {
        this.exchangeOutNumber = exchangeOutNumber;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return ExchangeOut.class;
    }
    
    public void setExchangeOutId(Integer exchangeOutId)
    {
        this.exchangeOutId = exchangeOutId;
    }

    /**
     *      * Date exchange was sent
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "ExchangeDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getExchangeDate() 
    {
        return this.exchangeDate;
    }
    
    public void setExchangeDate(Calendar exchangeDate) 
    {
        this.exchangeDate = exchangeDate;
    }

    /**
     *      * Number of items sent
     */
    @Column(name = "QuantityExchanged", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getQuantityExchanged() 
    {
        return this.quantityExchanged;
    }
    
    public void setQuantityExchanged(Short quantityExchanged) 
    {
        this.quantityExchanged = quantityExchanged;
    }

    /**
     * 
     */
    @Column(name = "DescriptionOfMaterial", unique = false, nullable = true, insertable = true, updatable = true, length = 120)
    public String getDescriptionOfMaterial() 
    {
        return this.descriptionOfMaterial;
    }
    
    public void setDescriptionOfMaterial(String descriptionOfMaterial) 
    {
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
    public String getRemarks()
    {
        return this.remarks;
    }

    public void setRemarks(String remarks)
    {
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
    public String getText1()
    {
        return this.text1;
    }

    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text2", length = 65535)
    public String getText2()
    {
        return this.text2;
    }

    public void setText2(String text2)
    {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 24, precision = 20, scale = 10)
    public BigDecimal getNumber1()
    {
        return this.number1;
    }

    public void setNumber1(BigDecimal number1)
    {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 24, precision = 20, scale = 10)
    public BigDecimal getNumber2()
    {
        return this.number2;
    }

    public void setNumber2(BigDecimal number2)
    {
        this.number2 = number2;
    }

    /**
     * User definable
     */
    @Column(name = "YesNo1", unique = false, nullable = true, updatable = true, insertable = true)
    public Boolean getYesNo1()
    {
        return this.yesNo1;
    }

    public void setYesNo1(Boolean yesNo1)
    {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo2()
    {
        return this.yesNo2;
    }

    public void setYesNo2(Boolean yesNo2)
    {
        this.yesNo2 = yesNo2;
    }

    /**
     *      * Agent ID of organization material was sent to
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SentToOrganizationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getAgentSentTo()
    {
        return this.agentSentTo;
    }

    public void setAgentSentTo(Agent agentSentTo)
    {
        this.agentSentTo = agentSentTo;
    }

    /**
     *      * Agent ID of person who recorded  the exchange
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CatalogedByID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getAgentCatalogedBy()
    {
        return this.agentCatalogedBy;
    }

    public void setAgentCatalogedBy(Agent agentCatalogedBy)
    {
        this.agentCatalogedBy = agentCatalogedBy;
    }

    /**
     *      * Shipment information for the exchange
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "exchangeOut")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Shipment> getShipments()
    {
        return this.shipments;
    }

    public void setShipments(Set<Shipment> shipments)
    {
        this.shipments = shipments;
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
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "exchangeOut")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<ExchangeOutPrep> getExchangeOutPreps() {
        return this.exchangeOutPreps;
    }
    
    public void setExchangeOutPreps(Set<ExchangeOutPrep> exchangeOutPreps) {
        this.exchangeOutPreps = exchangeOutPreps;
    }
    
    @OneToMany(mappedBy = "exchangeOut")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<ExchangeOutAttachment> getExchangeOutAttachments()
    {
        return exchangeOutAttachments;
    }

    public void setExchangeOutAttachments(Set<ExchangeOutAttachment> exchangeOutAttachments)
    {
        this.exchangeOutAttachments = exchangeOutAttachments;
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

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.LOCK })
    @JoinColumn(name = "DeaccessionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Deaccession getDeaccession() {
        return deaccession;
    }

    public void setDeaccession(Deaccession deaccession) {
        this.deaccession = deaccession;
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

    @Override
    @Transient
    public Set<? extends PreparationHolderIFace> getPreparationHolders() {
        return getExchangeOutPreps();
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 40;
    }

    @Transient
    public Integer getTotalPreps() {
        return countContents(false);
    }

    @Transient
    public Integer getTotalItems() {
        return countContents(true);
    }

    protected Integer countContents(Boolean countQuantity) {
        if (getId() == null) {
            return null;
        } else {
            return BasicSQLUtils.getCountAsInt(getCountContentsSql(countQuantity, getId()));
        }
    }

    protected static String getCountContentsSql(boolean countQuantity, int id) {
        return InteractionsTask.getCountContentsSql(countQuantity, false, id, getClassTableId());
    }

    @Transient
    public static List<String> getQueryableTransientFields() {
        List<String> result = new ArrayList<>();
        result.add("TotalPreps");
        result.add("TotalItems");
        return result;
    }

    public static Object getQueryableTransientFieldValue(String fldName, Object[] vals) {
        if (vals == null || vals[0] == null) {
            return null;
        } else if (fldName.equalsIgnoreCase("TotalPreps")) {
            return BasicSQLUtils.getCountAsInt(getCountContentsSql(false, (Integer)vals[0]));
        } else if (fldName.equalsIgnoreCase("TotalItems")) {
            return BasicSQLUtils.getCountAsInt(getCountContentsSql(true, (Integer)vals[0]));
        } else {
            log.error("Unknown calculated field: " + fldName);
            return null;
        }
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
    public Set<ExchangeOutAttachment> getAttachmentReferences()
    {
        return exchangeOutAttachments;
    }
}
