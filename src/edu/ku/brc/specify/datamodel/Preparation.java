/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.dbsupport.AttributeIFace;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "preparation")
public class Preparation extends DataModelObjBase implements java.io.Serializable, Comparable<Preparation>
{

    // Fields    

    protected Long                        preparationId;
    protected String                      text1;
    protected String                      text2;
    protected Integer                     count;
    protected String                      storageLocation;
    protected String                      remarks;
    protected Calendar                    preparedDate;
    protected Set<LoanPhysicalObject>     loanPhysicalObjects;
    protected Set<AttributeIFace>         attrs;
    protected PrepType                    prepType;
    protected CollectionObject            collectionObject;
    protected Agent                       preparedByAgent;
    protected Location                    location;
    protected Set<Attachment>             attachments;
    protected Set<DeaccessionPreparation> deaccessionPreparations;
    protected PreparationAttributes       preparationAttributes;


    // Constructors

    /** default constructor */
    public Preparation() {
        //
        // do nothing
    }
    
    /** constructor with id */
    public Preparation(Long preparationId) {
        this.preparationId = preparationId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        preparationId = null;
        text1 = null;
        text2 = null;
        count = null;
        storageLocation = null;
        remarks = null;
        preparedDate = null;
        loanPhysicalObjects = new HashSet<LoanPhysicalObject>();
        attrs = new HashSet<AttributeIFace>();
        prepType = null;
        collectionObject = null;
        preparedByAgent = null;
        location = null;
        attachments = new HashSet<Attachment>();
        deaccessionPreparations = new HashSet<DeaccessionPreparation>();
        preparationAttributes = null;

    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "PreparationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getPreparationId() {
        return this.preparationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Long getId()
    {
        return this.preparationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Preparation.class;
    }
    
    public void setPreparationId(Long preparationId) {
        this.preparationId = preparationId;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text1", length=255, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text2", length=255, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * The number of objects (specimens, slides, pieces) prepared
     */
    @Column(name = "Count", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
    public Integer getCount() 
    {
        return this.count;
    }
    
    public void setCount(Integer count) 
    {
        this.count = count;
    }
    
    @Transient
    public int getAvailable()
    {
        int cnt = this.count != null ? this.count : 0;
        return cnt - getQuantityOut();
    }

    @Transient
    public int getQuantityOut()
    {
        int stillOut = 0;
        for (LoanPhysicalObject lpo : getLoanPhysicalObjects())
        {
            int quantityLoaned   = lpo.getQuantity() != null ? lpo.getQuantity() : 0;
            int quantityReturned = lpo.getQuantityReturned() != null ? lpo.getQuantityReturned() : 0;
            
            stillOut += (quantityLoaned - quantityReturned);
        }
        return stillOut;
    }

    /**
     * 
     */
    @Column(name = "StorageLocation", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getStorageLocation() {
        return this.storageLocation;
    }
    
    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    /**
     * 
     */
    @Lob
    @Column(name="Remarks", unique=false, nullable=true, updatable=true, insertable=true)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "PreparedDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getPreparedDate() {
        return this.preparedDate;
    }
    
    public void setPreparedDate(Calendar preparedDate) {
        this.preparedDate = preparedDate;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "preparation")
    public Set<LoanPhysicalObject> getLoanPhysicalObjects() {
        return this.loanPhysicalObjects;
    }
    
    public void setLoanPhysicalObjects(Set<LoanPhysicalObject> loanPhysicalObjects) {
        this.loanPhysicalObjects = loanPhysicalObjects;
    }

    /**
     * 
     */
    @OneToMany(targetEntity=PreparationAttr.class,
            cascade = {}, fetch = FetchType.LAZY, mappedBy="preparation")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AttributeIFace> getAttrs() {
        return this.attrs;
    }
    
    public void setAttrs(Set<AttributeIFace> attrs) {
        this.attrs = attrs;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "PrepTypeID", unique = false, nullable = false, insertable = true, updatable = true)
    public PrepType getPrepType() {
        return this.prepType;
    }
    
    public void setPrepType(PrepType prepType) {
        this.prepType = prepType;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionObjectID", unique = false, nullable = false, insertable = true, updatable = true)
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "PreparedByID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getPreparedByAgent() {
        return this.preparedByAgent;
    }
    
    public void setPreparedByAgent(Agent preparedByAgent) {
        this.preparedByAgent = preparedByAgent;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "preparation")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "LocationID", unique = false, nullable = true, insertable = true, updatable = true)
    public Location getLocation() {
        return this.location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
    *
    */
   @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "preparation")
   public Set<DeaccessionPreparation> getDeaccessionPreparations() {
       return this.deaccessionPreparations;
   }

   public void setDeaccessionPreparations(Set<DeaccessionPreparation> deaccessionPreparations) {
       this.deaccessionPreparations = deaccessionPreparations;
   }
   
   /**
   *
   */
   @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
   @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
   @JoinColumn(name = "PreparationAttributesID", unique = false, nullable = true, insertable = true, updatable = true)
   public PreparationAttributes getPreparationAttributes() {
       return this.preparationAttributes;
   }

   public void setPreparationAttributes(PreparationAttributes preparationAttributes) {
       this.preparationAttributes = preparationAttributes;
   }

    // Add Methods

    public void addLoanPhysicalObjects(final LoanPhysicalObject loanPhysicalObject)
    {
        this.loanPhysicalObjects.add(loanPhysicalObject);
        loanPhysicalObject.setPreparation(this);
    }

    public void addAttrs(final PreparationAttr attr)
    {
        this.attrs.add(attr);
        attr.setPreparation(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeLoanPhysicalObjects(final LoanPhysicalObject loanPhysicalObject)
    {
        this.loanPhysicalObjects.remove(loanPhysicalObject);
        loanPhysicalObject.setPreparation(null);
    }

    public void removeAttrs(final PreparationAttr attr)
    {
        this.attrs.remove(attr);
        attr.setPreparation(null);
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
        return 63;
    }

    @Override
    @Transient
    public String getIdentityTitle()
    {
        PrepType pt = this.getPrepType();
        if (pt != null && StringUtils.isNotEmpty(pt.getName()))
        {
          String prepTypeStr = pt.getName();
          return prepTypeStr + (collectionObject != null ?  (": " + collectionObject.getIdentityTitle()) : "");
        } 
        // else
        return "Prepration " + getPreparationId();
    }
    

    //----------------------------------------------------------------------
    //-- Comparable Interface
    //----------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Preparation obj)
    {
        if (prepType != null && obj != null && StringUtils.isNotEmpty(prepType.name) && StringUtils.isNotEmpty(obj.prepType.name))
        {
            return prepType.name.toLowerCase().compareTo(obj.prepType.name.toLowerCase());
        }
        // else
        return timestampCreated.compareTo(obj.timestampCreated);
    }

}
