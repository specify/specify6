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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.AttributeProviderIFace;
import edu.ku.brc.dbsupport.DBConnection;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "preparation")
@org.hibernate.annotations.Table(appliesTo="preparation", indexes =
    {   @Index (name="PreparedDateIDX", columnNames={"preparedDate"}),
        @Index (name="PrepColMemIDX", columnNames={"CollectionMemberID"})
    })
public class Preparation extends CollectionMember implements AttachmentOwnerIFace<PreparationAttachment>, 
                                                             AttributeProviderIFace, 
                                                             java.io.Serializable, 
                                                             Comparable<Preparation>,
                                                             Cloneable
{

    // Fields    

    protected Integer                     preparationId;
    protected String                      text1;
    protected String                      text2;
    protected Integer                     count;
    protected String                      storageLocation;
    protected String                      remarks;
    protected Calendar                    preparedDate;
    protected Byte                        preparedDatePrecision;   // Accurate to Year, Month, Day
    protected String                      status;
    protected String                      sampleNumber;
    
    protected Float                       number1;
    protected Float                       number2;
    protected Boolean                     yesNo1;
    protected Boolean                     yesNo2;
    protected Boolean                     yesNo3;
    
    protected Set<GiftPreparation>        giftPreparations;
    protected Set<LoanPreparation>        loanPreparations;
    protected PrepType                    prepType;
    protected CollectionObject            collectionObject;
    protected Agent                       preparedByAgent;
    protected Storage                     storage;
    protected Set<DeaccessionPreparation> deaccessionPreparations;

    protected PreparationAttribute        preparationAttribute;    // Specify 5 Attributes table
    protected Set<PreparationAttr>        preparationAttrs;        // Generic Expandable Attributes
    protected Set<PreparationAttachment>  preparationAttachments;
    
    // Transient
    protected Boolean                     isOnLoan = null;
    
    // Constructors

    /** default constructor */
    public Preparation() 
    {
        // do nothing
    }
    
    /** constructor with id */
    public Preparation(Integer preparationId) 
    {
        this.preparationId = preparationId;
    }
   
    
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
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
        preparedDatePrecision = null;
        status = null;
        sampleNumber = null;
        
        number1      = null;
        number2      = null;
        yesNo1       = null;
        yesNo2       = null;
        yesNo3       = null;
        
        giftPreparations = new HashSet<GiftPreparation>();
        loanPreparations = new HashSet<LoanPreparation>();
        prepType = null;
        collectionObject = null;
        preparedByAgent = null;
        storage = null;
        deaccessionPreparations = new HashSet<DeaccessionPreparation>();
        
        preparationAttribute   = null;
        preparationAttrs       = new HashSet<PreparationAttr>();
        preparationAttachments = new HashSet<PreparationAttachment>();
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "PreparationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getPreparationId() {
        return this.preparationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
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
    
    public void setPreparationId(Integer preparationId) {
        this.preparationId = preparationId;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text1", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text2", length=300, unique = false, nullable = true, insertable = true, updatable = true)
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
    public int getLoanAvailable()
    {
        int cnt = this.count != null ? this.count : 0;
        return cnt - getLoanQuantityOut();
    }

    /**
     * calculates the number of preparations already loaned out.
     * @return the (calculated) number of preps out on loan.
     */
    @Transient
    public int getLoanQuantityOut()
    {
        int stillOut = 0;
        for (LoanPreparation lpo : getLoanPreparations())
        {
            int quantityLoaned   = lpo.getQuantity() != null ? lpo.getQuantity() : 0;
            int quantityReturned = lpo.getQuantityReturned() != null ? lpo.getQuantityReturned() : 0;
            
            stillOut += (quantityLoaned - quantityReturned);
        }
        return stillOut;
    }
    
    /**
     * @return
     */
    @Transient
    public Boolean getIsOnLoan()
    {
        if (isOnLoan == null)
        {
            Connection conn = DBConnection.getInstance().createConnection();
            Statement  stmt = null;
            try
            {
                conn = DBConnection.getInstance().createConnection();
                if (conn != null)
                {
                    stmt = conn.createStatement();
                    String sql = "SELECT p.Count, lp.Quantity, lp.QuantityResolved, lp.QuantityReturned, lp.IsResolved FROM preparation p " +
                                 "INNER JOIN loanpreparation lp ON p.PreparationID = lp.PreparationID WHERE p.PreparationID = "+getId();
                    ResultSet rs = stmt.executeQuery(sql);
                    
                    int     totalAvail = 0;
                    Integer prepQty    = null;
                    
                    while (rs.next())
                    {
                        prepQty = rs.getObject(1) != null ? rs.getInt(1) : 0;
                        System.err.print("\nprepQty "+prepQty);
                        
                        boolean isResolved = rs.getObject(5) != null ? rs.getBoolean(5) : false;
                        
                        int loanQty = rs.getObject(2) != null ? rs.getInt(2) : 0;
                        int qtyRes  = rs.getObject(3) != null ? rs.getInt(3) : 0;
                        int qtyRtn  = rs.getObject(4) != null ? rs.getInt(4) : 0;
                        
                        if (isResolved && qtyRes != loanQty) // this shouldn't happen
                        {
                            qtyRes = loanQty;
                        }
                        
                        System.err.println("  loanQty "+loanQty+"   qtyRtn "+qtyRtn+"   qtyRes "+qtyRes);
                        
                        totalAvail += qtyRes - loanQty;
                        System.err.println("totalAvail: "+totalAvail);
                    }
                    rs.close();
                    
                    if (prepQty == null)
                    {
                        return false;
                    }
                        
                    isOnLoan = totalAvail < prepQty;
                } else
                {
                    UsageTracker.incrNetworkUsageCount();
                }
                
            } catch (SQLException ex)
            {
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(Preparation.class, ex);
                UsageTracker.incrSQLUsageCount();
                ex.printStackTrace();
                
            } finally
            {
                if (stmt != null)
                {
                    try
                    {
                        stmt.close();
                    } catch (SQLException ex) {}
                }
                if (conn != null)
                {
                    try
                    {
                        conn.close();
                    } catch (SQLException ex) {}
                }
            }
        }
        return isOnLoan == null ? false : true;
    }
    
    /**
     * @param isOnLoan
     */
    public void setIsOnLoan(final Boolean isOnLoan)
    {
        this.isOnLoan = isOnLoan;
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
    @Temporal(TemporalType.DATE)
    @Column(name = "PreparedDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getPreparedDate() {
        return this.preparedDate;
    }
    
    public void setPreparedDate(Calendar preparedDate) {
        this.preparedDate = preparedDate;
    }

    /**
     * @return the preparedDatePrecision
     */
    @Column(name = "PreparedDatePrecision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getPreparedDatePrecision()
    {
        return this.preparedDatePrecision != null ? this.preparedDatePrecision : (byte)UIFieldFormatterIFace.PartialDateEnum.Full.ordinal();
    }

    /**
     * @param preparedDatePrecision the preparedDatePrecision to set
     */
    public void setPreparedDatePrecision(Byte preparedDatePrecision)
    {
        this.preparedDatePrecision = preparedDatePrecision;
    }

    /**
     * @return the status
     */
    @Column(name = "Status", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getStatus()
    {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * @return the sampleNumber
     */
    @Column(name = "SampleNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getSampleNumber()
    {
        return sampleNumber;
    }

    /**
     * @param sampleNumber the sampleNumber to set
     */
    public void setSampleNumber(String sampleNumber)
    {
        this.sampleNumber = sampleNumber;
    }

    /**
     * @return the number1
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber1()
    {
        return number1;
    }

    /**
     * @param number1 the number1 to set
     */
    public void setNumber1(Float number1)
    {
        this.number1 = number1;
    }

    /**
     * @return the number2
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber2()
    {
        return number2;
    }

    /**
     * @param number2 the number2 to set
     */
    public void setNumber2(Float number2)
    {
        this.number2 = number2;
    }

    /**
     * @return the yesNo1
     */
    @Column(name = "YesNo1", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo1()
    {
        return yesNo1;
    }

    /**
     * @param yesNo1 the yesNo1 to set
     */
    public void setYesNo1(Boolean yesNo1)
    {
        this.yesNo1 = yesNo1;
    }

    /**
     * @return the yesNo2
     */
    @Column(name = "YesNo2", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo2()
    {
        return yesNo2;
    }

    /**
     * @param yesNo2 the yesNo2 to set
     */
    public void setYesNo2(Boolean yesNo2)
    {
        this.yesNo2 = yesNo2;
    }

    /**
     * @return the yesNo3
     */
    @Column(name = "YesNo3", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getYesNo3()
    {
        return yesNo3;
    }

    /**
     * @param yesNo3 the yesNo3 to set
     */
    public void setYesNo3(Boolean yesNo3)
    {
        this.yesNo3 = yesNo3;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "preparation")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<LoanPreparation> getLoanPreparations() {
        return this.loanPreparations;
    }
    
    public void setLoanPreparations(Set<LoanPreparation> loanPreparations) {
        this.loanPreparations = loanPreparations;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "preparation")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<GiftPreparation> getGiftPreparations() {
        return this.giftPreparations;
    }
    
    public void setGiftPreparations(Set<GiftPreparation> giftPreparations) {
        this.giftPreparations = giftPreparations;
    }

   /**
     * @return the preparationAttrs
     */
    @OneToMany(targetEntity=PreparationAttr.class, cascade = {}, fetch = FetchType.LAZY, mappedBy="preparation")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<PreparationAttr> getPreparationAttrs()
    {
        return preparationAttrs;
    }

    /**
     * @param preparationAttrs the preparationAttrs to set
     */
    public void setPreparationAttrs(Set<PreparationAttr> preparationAttrs)
    {
        this.preparationAttrs = preparationAttrs;
    }

   /**
    *
    */
   @Transient
   public Set<AttributeIFace> getAttrs() 
   {
       return new HashSet<AttributeIFace>(this.preparationAttrs);
   }

   public void setAttrs(Set<AttributeIFace> preparationAttrs) 
   {
       this.preparationAttrs.clear();
       for (AttributeIFace a : preparationAttrs)
       {
           if (a instanceof PreparationAttr)
           {
               this.preparationAttrs.add((PreparationAttr)a);
           }
       }
   }
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
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
    @JoinColumn(name = "PreparedByID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getPreparedByAgent() {
        return this.preparedByAgent;
    }
    
    public void setPreparedByAgent(Agent preparedByAgent) {
        this.preparedByAgent = preparedByAgent;
    }

    //@OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "preparation")
    @OneToMany(mappedBy = "preparation")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<PreparationAttachment> getPreparationAttachments()
    {
        return preparationAttachments;
    }

    public void setPreparationAttachments(Set<PreparationAttachment> preparationAttachments)
    {
        this.preparationAttachments = preparationAttachments;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "StorageID", unique = false, nullable = true, insertable = true, updatable = true)
    public Storage getStorage() {
        return this.storage;
    }
    
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    /**
    *
    */
   @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "preparation")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
   public Set<DeaccessionPreparation> getDeaccessionPreparations() {
       return this.deaccessionPreparations;
   }

   public void setDeaccessionPreparations(Set<DeaccessionPreparation> deaccessionPreparations) {
       this.deaccessionPreparations = deaccessionPreparations;
   }
   
   /**
   *
   */
   @ManyToOne(cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.LAZY)
   @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
   @JoinColumn(name = "PreparationAttributeID", unique = false, nullable = true, insertable = true, updatable = true)
   public PreparationAttribute getPreparationAttribute() {
       return this.preparationAttribute;
   }

   public void setPreparationAttribute(PreparationAttribute preparationAttribute) {
       this.preparationAttribute = preparationAttribute;
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Transient
    public Set<PreparationAttachment> getAttachmentReferences()
    {
        return preparationAttachments;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Preparation obj = (Preparation)super.clone();
        obj.initialize();
        
        obj.text1        = text1;
        obj.text2        = text2;
        obj.count        = count;
        obj.storageLocation = storageLocation;
        obj.remarks      = remarks;
        obj.preparedDate = preparedDate;
        obj.status       = status;
        obj.sampleNumber = sampleNumber;
        
        obj.number1      = number1;
        obj.number2      = number2;
        obj.yesNo1       = yesNo1;
        obj.yesNo2       = yesNo2;
        obj.yesNo3       = yesNo2;
        obj.prepType     = prepType;
        obj.preparedByAgent = preparedByAgent;
        obj.collectionObject = collectionObject;
        
        // These are for documentation purposes (to know what isn't being cloned)
        
        //giftPreparations = new HashSet<GiftPreparation>();
        //loanPreparations = new HashSet<LoanPreparation>();
        //collectionObject = null;
        //storage = null;
        //deaccessionPreparations = new HashSet<DeaccessionPreparation>();
        //preparationAttributes  = null;
        //preparationAttrs       = new HashSet<PreparationAttr>();
        //preparationAttachments = new HashSet<PreparationAttachment>();
        
        return obj;
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
