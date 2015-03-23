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
package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

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
@Table(name = "gift")
@org.hibernate.annotations.Table(appliesTo="gift", indexes =
    {   @Index (name="GiftNumberIDX", columnNames={"GiftNumber"}),
        @Index (name="GiftDateIDX", columnNames={"GiftDate"})
    })
@SuppressWarnings("serial")
public class Gift extends DisciplineMember implements java.io.Serializable, PreparationsProviderIFace, AttachmentOwnerIFace<GiftAttachment>
{
    // Fields    
    protected Integer                 giftId;
    protected String                  giftNumber;
    protected Calendar                giftDate;
    
    protected String                  receivedComments;
    protected String                  specialConditions;
    protected Boolean                 isFinancialResponsibility;
    protected String                  purposeOfGift;
    protected Calendar                dateReceived;
    
    protected String                  srcGeography;
    protected String                  srcTaxonomy;
    
    protected String                  remarks;
    protected String				  contents;
    protected String                  text1;
    protected String                  text2;
    protected Float                   number1;
    protected Float                   number2;
    protected Boolean                 yesNo1;
    protected Boolean                 yesNo2;
    
    protected AddressOfRecord         addressOfRecord;
    protected Set<GiftAgent>          giftAgents;
    protected Set<GiftAttachment>     giftAttachments;
    protected Set<GiftPreparation>    giftPreparations;
    protected Set<Shipment>           shipments;
    
    protected Division                division;


    // Constructors

    /** default constructor */
    public Gift()
    {
        // do nothing
    }
    
    /** constructor with id */
    public Gift(Integer giftId) {
        this.giftId = giftId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        giftId          = null;
        giftNumber      = null;
        giftDate        = null;

        receivedComments    = null;
        specialConditions   = null;
        isFinancialResponsibility = null;
        purposeOfGift       = null;
        dateReceived        = null;
        
        srcGeography        = null;
        srcTaxonomy         = null;
        
        remarks         = null;
        contents        = null;
        text1           = null;
        text2           = null;
        number1         = null;
        number2         = null;

        yesNo1          = null;
        yesNo2          = null;
        giftAgents      = new HashSet<GiftAgent>();

        giftPreparations = new HashSet<GiftPreparation>();
        shipments        = new HashSet<Shipment>();
        
        division        = null;
        addressOfRecord = null;
        giftAttachments =  new HashSet<GiftAttachment>();

    }
    // End Initializer

    /**
     *      * PrimaryKey
     */
    @Id
    @GeneratedValue
    @Column(name = "GiftID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getGiftId() {
        return this.giftId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Integer getId()
    {
        return this.giftId;
    }
   
    public void setGiftId(Integer giftId) {
        this.giftId = giftId;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Gift.class;
    }
    /**
     *      * Invoice number
     */
    @Column(name = "GiftNumber", unique = false, nullable = false, insertable = true, updatable = true, length = 50)
    public String getGiftNumber() {
        return this.giftNumber;
    }
    
    public void setGiftNumber(String giftNumber) {
        this.giftNumber = giftNumber;
    }

    /**
     *      * Date the Gift was created.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "GiftDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getGiftDate() {
        return this.giftDate;
    }
    
    public void setGiftDate(Calendar giftDate) {
        this.giftDate = giftDate;
    }

    /**
     * @return the srcGeography
     */
    @Column(name = "SrcGeography", unique = false, nullable = true, insertable = true, updatable = true, length = 500)
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
    @Column(name = "SrcTaxonomy", unique = false, nullable = true, insertable = true, updatable = true, length = 500)
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
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo1", unique = false, nullable = true, updatable = true, insertable = true)
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo2", unique = false, nullable = true, updatable = true, insertable = true)
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     * @return the receivedComments
     */
    @Column(name = "ReceivedComments", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getReceivedComments()
    {
        return receivedComments;
    }

    /**
     * @param receivedComments the receivedComments to set
     */
    public void setReceivedComments(String receivedComments)
    {
        this.receivedComments = receivedComments;
    }

    /**
     * @return the specialConditions
     */
    @Lob
    @Column(name = "SpecialConditions", unique = false, nullable = true, insertable = true, updatable = true, length = 2048)
    public String getSpecialConditions()
    {
        return specialConditions;
    }

    /**
     * @param specialConditions the specialConditions to set
     */
    public void setSpecialConditions(String specialConditions)
    {
        this.specialConditions = specialConditions;
    }

    /**
     * @return the isFinancialResponsibility
     */
    @Column(name = "IsFinancialResponsibility", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsFinancialResponsibility()
    {
        return isFinancialResponsibility;
    }

    /**
     * @param isFinancialResponsibility the isFinancialResponsibility to set
     */
    public void setIsFinancialResponsibility(Boolean isFinancialResponsibility)
    {
        this.isFinancialResponsibility = isFinancialResponsibility;
    }


    /**
     * @return the purposeOfGift
     */
    @Column(name = "PurposeOfGift", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getPurposeOfGift()
    {
        return purposeOfGift;
    }

    /**
     * @param purposeOfGift the purposeOfGift to set
     */
    public void setPurposeOfGift(String purposeOfGift)
    {
        this.purposeOfGift = purposeOfGift;
    }

    /**
     * @return the dateReceived
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateReceived", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateReceived()
    {
        return dateReceived;
    }

    /**
     * @param dateReceived the dateReceived to set
     */
    public void setDateReceived(Calendar dateReceived)
    {
        this.dateReceived = dateReceived;
    }

    /**
     * @return the division
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DivisionID", unique = false, nullable = true, insertable = true, updatable = true)
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
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "gift")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<GiftAgent> getGiftAgents() {
        return this.giftAgents;
    }
    
    public void setGiftAgents(Set<GiftAgent> giftAgents) {
        this.giftAgents = giftAgents;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "gift")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<GiftPreparation> getGiftPreparations() {
        return this.giftPreparations;
    }
    
    public void setGiftPreparations(Set<GiftPreparation> giftPreparations) {
        this.giftPreparations = giftPreparations;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "gift")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Shipment> getShipments() {
        return this.shipments;
    }
    
    public void setShipments(Set<Shipment> shipments) {
        this.shipments = shipments;
    }
    
    /**
     * @return the giftAttachments
     */
    @OneToMany(mappedBy = "gift")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<GiftAttachment> getGiftAttachments()
    {
        return giftAttachments;
    }

    /**
     * @param giftAttachments the giftAttachments to set
     */
    public void setGiftAttachments(Set<GiftAttachment> giftAttachments)
    {
        this.giftAttachments = giftAttachments;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.PreparationsProviderIFace#getPreparations()
     */
    @Override
    @Transient
    public Set<PreparationHolderIFace> getPreparations()
    {
        HashSet<PreparationHolderIFace> set = new HashSet<PreparationHolderIFace>();
        for (GiftPreparation gp : giftPreparations)
        {
            set.add(gp);
        }
        return set;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        for (GiftAgent a : giftAgents)
        {
            a.forceLoad();
        }
        for (Shipment s : shipments)
        {
            s.forceLoad();
        }
        for (GiftPreparation giftPrep : giftPreparations)
        {
            giftPrep.forceLoad();
        }
        for (GiftAttachment giftAtt : giftAttachments)
        {
            giftAtt.forceLoad();
        }
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
    public Set<GiftAttachment> getAttachmentReferences()
    {
        return giftAttachments;
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 131;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return giftNumber != null ? giftNumber : super.getIdentityTitle();
    }


}
