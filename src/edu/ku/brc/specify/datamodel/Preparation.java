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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.math.BigDecimal;

import javax.persistence.*;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.tasks.InteractionsProcessor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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
@Table(name="preparation", uniqueConstraints = {
        @UniqueConstraint(columnNames={"CollectionMemberID", "BarCode"} )
}
)

@org.hibernate.annotations.Table(appliesTo="preparation", indexes =
    {   @Index (name="PreparedDateIDX", columnNames={"preparedDate"}),
        @Index (name="PrepColMemIDX", columnNames={"CollectionMemberID"}),
    	@Index (name="PrepGuidIDX", columnNames={"GUID"}),
        @Index (name="PrepSampleNumIDX", columnNames={"SampleNumber"}),
            @Index (name="PrepBarCodeIDX", columnNames={"BarCode"})
    })
@SuppressWarnings("serial")
public class Preparation extends CollectionMember implements AttachmentOwnerIFace<PreparationAttachment>, 
                                                             AttributeProviderIFace, 
                                                             java.io.Serializable, 
                                                             Comparable<Preparation>,
                                                             Cloneable
{

    private static final Logger log = Logger.getLogger(Preparation.class);
    // Fields

    protected Integer                     preparationId;
    protected String                      text1;
    protected String                      text2;
    protected String					  text3;	
    protected String					  text4;	
    protected String					  text5;
    protected String                      text6;
    protected String                      text7;
    protected String                      text8;
    protected String                      text9;
    protected String                      text10;
    protected String                      text11;
    protected String                      text12;
    protected String                      text13;
    protected Integer                     countAmt;
    protected String                      storageLocation;
    protected String                      remarks;
    protected Calendar                    preparedDate;
    protected Byte                        preparedDatePrecision;   // Accurate to Year, Month, Day
    protected Calendar                    date1;
    protected Byte                        date1Precision;
    protected Calendar                    date2;
    protected Byte                        date2Precision;
    protected Calendar                    date3;
    protected Byte                        date3Precision;
    protected Calendar                    date4;
    protected Byte                        date4Precision;

    protected String                      status;
    protected String                      sampleNumber;
    protected String                      description;             // from Specify 5
    protected String                      guid;
    protected String barCode;
    
    protected BigDecimal                       number1;
    protected BigDecimal                       number2;
    protected Integer					  integer1;
    protected Integer					  integer2;
    protected Integer					  reservedInteger3;
    protected Integer					  reservedInteger4;
    protected Boolean                     yesNo1;
    protected Boolean                     yesNo2;
    protected Boolean                     yesNo3;
    
    protected Set<GiftPreparation>        giftPreparations;
    protected Set<LoanPreparation>        loanPreparations;
    protected Set<ConservDescription>     conservDescriptions;
    protected PrepType                    prepType;
    protected CollectionObject            collectionObject;
    protected Agent                       preparedByAgent;
    protected Storage                     storage;
    protected Storage alternateStorage;
    protected Set<DisposalPreparation> disposalPreparations;

    protected PreparationAttribute        preparationAttribute;    // Specify 5 Attributes table
    protected Set<PreparationAttr>        preparationAttrs;        // Generic Expandable Attributes
    protected Set<PreparationAttachment>  preparationAttachments;
    protected Set<PreparationProperty>    preparationProperties;
    
    protected Set<ExchangeInPrep>         exchangeInPreps;
    protected Set<ExchangeOutPrep>        exchangeOutPreps;
    
    protected Set<MaterialSample>         materialSamples;
    
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
        text1        = null;
        text2        = null;
        text3 = null;
        text4 = null;
        text5 = null;
        text6 = null;
        text7 = null;
        text8 = null;
        text9 = null;
        text10 = null;
        text11 = null;
        text12 = null;
        text13 = null;
        countAmt     = null;
        storageLocation = null;
        remarks      = null;
        preparedDate = null;
        preparedDatePrecision = 1;
        date1 = null;
        date1Precision = 1;
        date2 = null;
        date2Precision = 1;
        date3 = null;
        date3Precision = 1;
        date4 = null;
        date4Precision = 1;
        status       = null;
        sampleNumber = null;
        description  = null;
        guid         = null;
        barCode      = null;

        number1      = null;
        number2      = null;
        integer1			  = null;
        integer2			  = null;
        reservedInteger3	  = null;
        reservedInteger4	  = null;
        yesNo1       = null;
        yesNo2       = null;
        yesNo3       = null;
        
        giftPreparations = new HashSet<GiftPreparation>();
        loanPreparations = new HashSet<LoanPreparation>();
        prepType = null;
        collectionObject = null;
        preparedByAgent = null;
        storage = null;
        alternateStorage = null;
        disposalPreparations = new HashSet<DisposalPreparation>();
        conservDescriptions         = new HashSet<ConservDescription>();

        preparationAttribute   = null;
        preparationAttrs       = new HashSet<PreparationAttr>();
        preparationAttachments = new HashSet<PreparationAttachment>();
        preparationProperties  = new HashSet<PreparationProperty>();
        
        exchangeInPreps  = new HashSet<ExchangeInPrep>();
        exchangeOutPreps = new HashSet<ExchangeOutPrep>();

        materialSamples = new HashSet<MaterialSample>();
        
        hasGUIDField = true;
        setGUID();
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
     *
     * @return
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "Date1", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate1() {
        return date1;
    }

    /**
     *
     * @param date1
     */
    public void setDate1(Calendar date1) {
        this.date1 = date1;
    }

    /**
     *
     * @return
     */
    @Column(name = "Date1Precision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDate1Precision() {
        return date1Precision;
    }

    /**
     *
     * @param date1Precision
     */
    public void setDate1Precision(Byte date1Precision) {
        this.date1Precision = date1Precision;
    }
    /**
     *
     * @return
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "Date2", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate2() {
        return date2;
    }

    /**
     *
     * @param date2
     */
    public void setDate2(Calendar date2) {
        date2 = date2;
    }

    /**
     *
     * @return
     */
    @Column(name = "Date2Precision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDate2Precision() {
        return date2Precision;
    }

    /**
     *
     * @param date2Precision
     */
    public void setDate2Precision(Byte date2Precision) {
        date2Precision = date2Precision;
    }

    /**
     *
     * @return
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "Date3", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate3() {
        return date3;
    }

    /**
     *
     * @param date3
     */
    public void setDate3(Calendar date3) {
        date3 = date3;
    }

    /**
     *
     * @return
     */
    @Column(name = "Date3Precision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDate3Precision() {
        return date3Precision;
    }

    /**
     *
     * @param date3Precision
     */
    public void setDate3Precision(Byte date3Precision) {
        date3Precision = date3Precision;
    }

    /**
     *
     * @return
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "Date4", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate4() {
        return date4;
    }

    /**
     *
     * @param date4
     */
    public void setDate4(Calendar date4) {
        date4 = date4;
    }

    /**
     *
     * @return
     */
    @Column(name = "Date4Precision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDate4Precision() {
        return date4Precision;
    }

    /**
     *
     * @param date4Precision
     */
    public void setDate4Precision(Byte date4Precision) {
        date4Precision = date4Precision;
    }

    /**
     * @return the text6
     */
    @Lob
    @Column(name = "Text6", length = 65535)
    public String getText6() {
        return text6;
    }

    /**
     *
     * @param text6
     */
    public void setText6(String text6) {
        this.text6 = text6;
    }

    /**
     *
     * @return
     */
    @Lob
    @Column(name = "Text7", length = 65535)
    public String getText7() {
        return text7;
    }

    /**
     *
     * @param text7
     */
    public void setText7(String text7) {
        this.text7 = text7;
    }

    /**
     *
     * @return
     */
    @Lob
    @Column(name = "Text8", length = 65535)
    public String getText8() {
        return text8;
    }

    /**
     *
     * @param text8
     */
    public void setText8(String text8) {
        this.text8 = text8;
    }

    /**
     *
     * @return
     */
    @Lob
    @Column(name = "Text9", length = 65535)
    public String getText9() {
        return text9;
    }

    /**
     *
     * @param text9
     */
    public void setText9(String text9) {
        this.text9 = text9;
    }

    /**
     *
     * @return
     */
    @Lob
    @Column(name = "Text10", length = 65535)
    public String getText10() {
        return text10;
    }

    /**
     *
     * @param text10
     */
    public void setText10(String text10) {
        this.text10 = text10;
    }
    /**
     *
     * @return
     */
    @Lob
    @Column(name = "Text11", length = 65535)
    public String getText11() {
        return text11;
    }

    /**
     *
     * @param text11
     */
    public void setText11(String text11) {
        this.text11 = text11;
    }
    /**
     *
     * @return
     */
    @Column(name = "Text12", length = 128)
    public String getText12() {
        return text12;
    }

    /**
     *
     * @param text12
     */
    public void setText12(String text12) {
        this.text12 = text12;
    }
    /**
     *
     * @return
     */
    @Column(name = "Text13", length = 128)
    public String getText13() {
        return text13;
    }

    /**
     *
     * @param text13
     */
    public void setText13(String text13) {
        this.text13 = text13;
    }
    /**
     *
     */
    @OneToMany(mappedBy = "preparation")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<PreparationProperty> getPreparationProperties() {
        return this.preparationProperties;
    }

    public void setPreparationProperties(Set<PreparationProperty> preparationProperties) {
        this.preparationProperties = preparationProperties;
    }
    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "preparation")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<ConservDescription> getConservDescriptions()
    {
        return this.conservDescriptions;
    }

    public void setConservDescriptions(final Set<ConservDescription> conservDescriptions)
    {
        this.conservDescriptions = conservDescriptions;
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
	 * @return the text3
	 */
    @Lob
    @Column(name = "Text3", length = 65535)
	public String getText3() {
		return text3;
	}

	/**
	 * @param text3 the text3 to set
	 */
	public void setText3(String text3) {
		this.text3 = text3;
	}

	/**
	 * @return the text4
	 */
    @Lob
    @Column(name = "Text4", length = 65535)
	public String getText4() {
		return text4;
	}

	/**
	 * @param text4 the text4 to set
	 */
	public void setText4(String text4) {
		this.text4 = text4;
	}

	/**
	 * @return the text5
	 */
    @Lob
    @Column(name = "Text5", length = 65535)
	public String getText5() {
		return text5;
	}

	/**
	 * @param text5 the text5 to set
	 */
	public void setText5(String text5) {
		this.text5 = text5;
	}

	/**
     *      * The number of objects (specimens, slides, pieces) prepared
     */
    @Column(name = "CountAmt", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getCountAmt() 
    {
        return this.countAmt;
    }
    
    public void setCountAmt(Integer countAmt) 
    {
        this.countAmt = countAmt;
    }
    
    @Transient
    public int getLoanAvailable()
    {
        int cnt = this.countAmt != null ? this.countAmt : 0;
        return cnt - getLoanQuantityOut() - getDisposedQuantity();
    }

    @Transient
    public int getActualCountAmt() {
        int cnt = this.countAmt != null ? this.countAmt : 0;
        return cnt - getDisposedQuantity();
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
            int quantityResolved = lpo.getQuantityResolved() != null ? lpo.getQuantityResolved() : 0;
            
            stillOut += (quantityLoaned - quantityResolved);
        }
        return stillOut;
    }

    private boolean countGiftsAsDisposals = true;
    private boolean countExchangesAsDisposals = true;
    @Transient
    int getDisposedQuantity() {
        int deacced = 0;
        for (DisposalPreparation dp : getDisposalPreparations()) {
            deacced += dp.quantity != null ? dp.getQuantity() : 0;
        }
        if (countGiftsAsDisposals) {
            for (GiftPreparation gp : getGiftPreparations()) {
                deacced += gp.quantity != null ? gp.getQuantity() : 0;
            }
        }
        if (countExchangesAsDisposals) {
            for (ExchangeOutPrep ep : getExchangeOutPreps()) {
                deacced += ep.quantity != null ? ep.getQuantity() : 0;
            }
        }
        return deacced;
    }

    @OneToMany(mappedBy = "preparation")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<MaterialSample> getMaterialSamples() {
        return this.materialSamples;
    }

    public void setMaterialSamples(Set<MaterialSample> materialSamples) {
        this.materialSamples = materialSamples;
    }

    /**
     * @return
     */
    @Transient
    public Boolean getIsOnLoan()
    {
        if (isOnLoan == null)
        {
            Connection conn = null;
            Statement  stmt = null;
            try
            {
                conn = DBConnection.getInstance().createConnection();
                if (conn != null)
                {
                    stmt = conn.createStatement();
                    String sql = "SELECT p.CountAmt, lp.Quantity, lp.QuantityResolved, lp.QuantityReturned, lp.IsResolved FROM preparation p " +
                                 "INNER JOIN loanpreparation lp ON p.PreparationID = lp.PreparationID WHERE p.PreparationID = "+getId();
                    ResultSet rs = stmt.executeQuery(sql);
                    
                    int     totalOnLoan = 0;
                    Integer prepQty     = null;
                    boolean checkAllInteractions = false;

                    while (rs.next())
                    {
                        prepQty = rs.getObject(1) != null ? rs.getInt(1) : 0;
                        //System.err.println("\nprepQty "+prepQty);
                        
                        boolean isResolved = rs.getObject(5) != null ? rs.getBoolean(5) : false;
                        
                        int loanQty = rs.getObject(2) != null ? rs.getInt(2) : 0;
                        int qtyRes  = rs.getObject(3) != null ? rs.getInt(3) : 0;
                        //int qtyRtn  = rs.getObject(4) != null ? rs.getInt(4) : 0;
                        
                        //System.err.println("loanQty "+loanQty);
                        //System.err.println("qtyRes  "+qtyRes);
                        //System.err.println("qtyRtn  "+qtyRtn);
                        
                        if (isResolved && qtyRes != loanQty) // this shouldn't happen
                        {
                            qtyRes = loanQty;
                        }
                        
                        totalOnLoan += loanQty - qtyRes;
                    }
                    rs.close();
                    isOnLoan = totalOnLoan > 0;
                    if (!isOnLoan && checkAllInteractions) {
                        isOnLoan = !BasicSQLUtils.getCount("select count(*) from preparation p left join giftpreparation"
                                + " gp on gp.preparationid = p.preparationid left join disposalpreparation dp on dp.preparationid"
                                + " = p.preparationid left join exchangeoutprep ep on ep.preparationid = p.preparationid where p.preparationid = "
                                + getId() + " and (gp.quantity > 0 or dp.quantity > 0 or ep.quantity > 0)").equals(0);
                    }
                        

                    //System.err.println("totalOnLoan "+totalOnLoan);
                    //System.err.println("isOnLoan    "+isOnLoan);
                    
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
        return isOnLoan == null ? false : isOnLoan;
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
   @Column(name = "GUID", unique = false, nullable = true, insertable = true, updatable = false, length = 128)
   public String getGuid() {
       return this.guid;
   }

   /**
    * @param guid
    */
   public void setGuid(String guid) {
       this.guid = guid;
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

    @Column(name = "BarCode", unique = false, nullable = true, insertable = true, updatable = true, length = 256)
    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    /**
     * Image, Sound, Preparation, Container(Container Label?)
     */
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getDescription() 
    {
        return this.description;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    /**
     * @return the number1
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
    public BigDecimal getNumber1()
    {
        return number1;
    }

    /**
     * @param number1 the number1 to set
     */
    public void setNumber1(BigDecimal number1)
    {
        this.number1 = number1;
    }

    /**
     * @return the number2
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
    public BigDecimal getNumber2()
    {
        return number2;
    }

    /**
     * @param number2 the number2 to set
     */
    public void setNumber2(BigDecimal number2)
    {
        this.number2 = number2;
    }

	/**
	 * @return the integer1
	 */
    @Column(name = "Integer1", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getInteger1() {
		return integer1;
	}

	/**
	 * @param integer1 the integer1 to set
	 */
	public void setInteger1(Integer integer1) {
		this.integer1 = integer1;
	}

	/**
	 * @return the integer2
	 */
    @Column(name = "Integer2", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getInteger2() {
		return integer2;
	}

	/**
	 * @param integer2 the integer2 to set
	 */
	public void setInteger2(Integer integer2) {
		this.integer2 = integer2;
	}

	/**
	 * @return the reservedInteger3
	 */
    @Column(name = "ReservedInteger3", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getReservedInteger3() {
		return reservedInteger3;
	}

	/**
	 * @param reservedInteger3 the reservedInteger3 to set
	 */
	public void setReservedInteger3(Integer reservedInteger3) {
		this.reservedInteger3 = reservedInteger3;
	}

	/**
	 * @return the reservedInteger4
	 */
    @Column(name = "ReservedInteger4", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getReservedInteger4() {
		return reservedInteger4;
	}

	/**
	 * @param reservedInteger4 the reservedInteger4 to set
	 */
	public void setReservedInteger4(Integer reservedInteger4) {
		this.reservedInteger4 = reservedInteger4;
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
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "preparation")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<ExchangeInPrep> getExchangeInPreps() {
        return this.exchangeInPreps;
    }
    
    public void setExchangeInPreps(Set<ExchangeInPrep> exchangeInPreps) {
        this.exchangeInPreps = exchangeInPreps;
    }
    
    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "preparation")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<ExchangeOutPrep> getExchangeOutPreps() {
        return this.exchangeOutPreps;
    }
    
    public void setExchangeOutPreps(Set<ExchangeOutPrep> exchangeOutPreps) {
        this.exchangeOutPreps = exchangeOutPreps;
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

    /**
     *
     * @param storage
     */
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AlternateStorageID", unique = false, nullable = true, insertable = true, updatable = true)
    public Storage getAlternateStorage() {
        return alternateStorage;
    }

    /**
     *
     * @param alternateStorage
     */
    public void setAlternateStorage(Storage alternateStorage) {
        this.alternateStorage = alternateStorage;
    }

    /**
    *
    */
   @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "preparation")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
   public Set<DisposalPreparation> getDisposalPreparations() {
       return this.disposalPreparations;
   }

   public void setDisposalPreparations(Set<DisposalPreparation> disposalPreparations) {
       this.disposalPreparations = disposalPreparations;
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentTableId()
     */
    @Override
    @Transient
    public int getAttachmentTableId()
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
    @Override
    @Transient
    public Set<PreparationAttachment> getAttachmentReferences()
    {
        return preparationAttachments;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        preparationAttachments.size();
        preparationAttrs.size();
        preparationProperties.size();
        disposalPreparations.size();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Preparation obj = (Preparation)super.clone();
        
        obj.preparationId           = null;
        obj.loanPreparations        = new HashSet<LoanPreparation>();
        obj.collectionObject        = null;
        obj.disposalPreparations = new HashSet<DisposalPreparation>();
        obj.preparationAttachments  = new HashSet<PreparationAttachment>();
        obj.conservDescriptions = new HashSet<ConservDescription>();
        obj.preparationProperties = new HashSet<PreparationProperty>();
       
        // Clone Attributes
        obj.preparationAttribute    = preparationAttribute != null ? (PreparationAttribute)preparationAttribute.clone() : null;
        obj.preparationAttrs        = new HashSet<PreparationAttr>();
        for (PreparationAttr pa : preparationAttrs)
        {
            PreparationAttr newPA = (PreparationAttr)pa.clone();
            obj.preparationAttrs.add(newPA);
            newPA.setPreparation(obj);
        }
        for (PreparationProperty pp : preparationProperties) {
            PreparationProperty newPP = (PreparationProperty)pp.clone();
            obj.preparationProperties.add(newPP);
            newPP.setPreparation(obj);
        }
        obj.setGUID();
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
        return timestampCreated != null && obj != null && obj.timestampCreated != null ? timestampCreated.compareTo(obj.timestampCreated) : 0;
    }

    @Transient
    public static List<String> getQueryableTransientFields() {
        List<String> result = new ArrayList<>();
        result.add("ActualCountAmt");
        return result;
    }

    protected static Object computeActualCountAmt(Object[] vals) {
        boolean[] settings = {false, true, true, true};
        String sql = InteractionsProcessor.getAdjustedCountForPrepSQL("p.preparationid = " + vals[0], settings);
        Object[] amt = BasicSQLUtils.queryForRow(sql);
        return amt != null ? amt[1] : null;
    }

    public static Object getQueryableTransientFieldValue(String fldName, Object[] vals) {
        if (fldName.equalsIgnoreCase("ActualCountAmt")) {
            return computeActualCountAmt(vals);
        } else {
            log.error("Unknown calculated field: " + fldName);
            return null;
        }
    }
}
