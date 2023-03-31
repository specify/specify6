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
@Table(name = "giftpreparation")
@org.hibernate.annotations.Table(appliesTo="giftpreparation", indexes =
    {   @Index (name="GiftPrepDspMemIDX", columnNames={"DisciplineID"})
    })
public class GiftPreparation extends DisciplineMember implements java.io.Serializable, PreparationHolderIFace, Comparable<GiftPreparation>
{

    // Fields    

    protected Integer                       giftPreparationId;
    protected Integer                       quantity;
    protected String                        descriptionOfMaterial;
    protected String                        outComments;          // Shipped Comments
    protected String                        inComments;           // Returned Comments
    protected String                        receivedComments;     // Received Comments
    protected Preparation                   preparation;
    protected Gift                          gift;
    protected String text1;
    protected String text2;
    protected String text3;
    protected String text4;
    protected String text5;

    // Constructors

    /** default constructor */
    public GiftPreparation() {
        //
    }
    
    /** constructor with id */
    public GiftPreparation(Integer giftPreparationId) 
    {
        this.giftPreparationId = giftPreparationId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        giftPreparationId = null;
        quantity = null;
        descriptionOfMaterial = null;
        outComments = null;
        inComments = null;
        receivedComments = null;
        preparation = null;
        gift = null;
        text1 = null;
        text2 = null;
        text3 = null;
        text4 = null;
        text5 = null;
    }
    // End Initializer

    /**
     * PrimaryKey
     */
    @Id
    @GeneratedValue
    @Column(name = "GiftPreparationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getGiftPreparationId() {
        return this.giftPreparationId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.giftPreparationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return GiftPreparation.class;
    }
    
    public void setGiftPreparationId(Integer giftPreparationId) {
        this.giftPreparationId = giftPreparationId;
    }

    /**
     * The total number of specimens  gifted (necessary for lots)
     */
    @Column(name = "Quantity", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getQuantity() 
    {
        return this.quantity == null ? 0 : this.quantity;
    }
    
    public void setQuantity(Integer quantity) 
    {
        this.quantity = quantity;
    }

    /**
     * Description of gifted material (intended to be used for non-cataloged items, i.e. when PreparationID is null)
     */
    @Column(name = "DescriptionOfMaterial", unique = false, nullable = true, insertable = true, updatable = true)
    public String getDescriptionOfMaterial() {
        return this.descriptionOfMaterial;
    }
    
    public void setDescriptionOfMaterial(String descriptionOfMaterial) {
        this.descriptionOfMaterial = descriptionOfMaterial;
    }

    /**
     * Comments on item when gifted
     */
    @Lob
    @Column(name = "OutComments", unique = false, nullable = true, insertable = true, updatable = true, length = 1024)
    public String getOutComments() {
        return this.outComments;
    }
    
    public void setOutComments(String outComments) {
        this.outComments = outComments;
    }

    /**
     * Comments on item when returned
     */
    @Lob
    @Column(name = "InComments", unique = false, nullable = true, insertable = true, updatable = true, length = 1024)
    public String getInComments() {
        return this.inComments;
    }
    
    public void setInComments(String inComments) {
        this.inComments = inComments;
    }

    /**
     * @return the receivedComments
     */
    @Lob
    @Column(name = "ReceivedComments", unique = false, nullable = true, insertable = true, updatable = true, length = 1024)
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.PreparationHolderIFace#getQuantityReturned()
     */
    @Transient
    public Integer getQuantityReturned() 
    {
        return 0;
    }
    
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PreparationID", unique = false, nullable = true, insertable = true, updatable = true)
    public Preparation getPreparation() {
        return this.preparation;
    }
    
    public void setPreparation(Preparation preparation) {
        this.preparation = preparation;
    }

    /**
     * Gift containing the Preparation
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "GiftID", unique = false, nullable = true, insertable = true, updatable = true)
    public Gift getGift() {
        return this.gift;
    }
    
    public void setGift(Gift gift) 
    {
        this.gift = gift;
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
    @Lob
    @Column(name = "Text3", length = 65535)
    public String getText3() {
        return this.text3;
    }
    
    public void setText3(String text3) {
        this.text3 = text3;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text4", length = 65535)
    public String getText4() {
        return this.text4;
    }
    
    public void setText4(String text4) {
        this.text4 = text4;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text5", length = 65535)
    public String getText5() {
        return this.text5;
    }
    
    public void setText5(String text5) {
        this.text5 = text5;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Gift.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return gift != null ? gift.getId() : null;
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
        return 132;
    }
    
    //----------------------------------------------------------------------
    //-- Comparable Interface
    //----------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(GiftPreparation obj)
    {
        return timestampCreated != null && obj != null && obj.timestampCreated != null ? timestampCreated.compareTo(obj.timestampCreated) : 0;
    }
}
