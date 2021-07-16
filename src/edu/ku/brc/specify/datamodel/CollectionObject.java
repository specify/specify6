/* Copyright (C) 2020, Specify Collections Consortium
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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.AttributeProviderIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "collectionobject", uniqueConstraints = {
        @UniqueConstraint(columnNames={"CollectionID", "CatalogNumber"} ),
        @UniqueConstraint(columnNames={"CollectionID", "UniqueIdentifier"} )
        }
)
@org.hibernate.annotations.Table(appliesTo="collectionobject", indexes =
    {   @Index (name="FieldNumberIDX", columnNames={"FieldNumber"}),
        @Index (name="CatalogedDateIDX", columnNames={"CatalogedDate"}),
        @Index (name="CatalogNumberIDX", columnNames={"CatalogNumber"}),
        @Index (name="AltCatalogNumberIDX", columnNames= {"AltCatalogNumber"}),
        @Index (name="ColObjGuidIDX", columnNames={"GUID"}),
        @Index (name="COColMemIDX", columnNames={"CollectionmemberID"})
    })
@SuppressWarnings("serial")
public class CollectionObject extends CollectionMember implements AttachmentOwnerIFace<CollectionObjectAttachment>, 
                                                                  java.io.Serializable, 
                                                                  AttributeProviderIFace, 
                                                                  Comparable<CollectionObject>
{
    private static final Logger log = Logger.getLogger(CollectionObject.class);

    // Fields

    /* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#shouldForceLoadChildSet(java.lang.reflect.Method)
	 */
	@Override
	public int shouldForceLoadChildSet(Method getter) {
		if ("getProjects".equals(getter.getName())) {
			return 0;
		} else {
			return super.shouldForceLoadChildSet(getter);
		}
	}



	protected Integer                       collectionObjectId;
    protected String                        fieldNumber;
    protected String                        description;
    protected String                        projectNumber;
    protected Integer numberOfDuplicates;
    protected String                        text1;
    protected String                        text2;
    protected String                        text3;
    protected String                        text4;
    protected String                        text5;
    protected String                        text6;
    protected String                        text7;
    protected String                        text8;
    protected String                        reservedText;
    protected String					    reservedText2;
    protected String                        reservedText3;
    protected Float                         number1;
    protected Float                         number2;
    protected Integer					    integer1;
    protected Integer					    integer2;
    protected Integer					    reservedInteger3;
    protected Integer					    reservedInteger4;
    protected Boolean                       yesNo1;
    protected Boolean                       yesNo2;
    protected Boolean                       yesNo3;
    protected Boolean                       yesNo4;
    protected Boolean                       yesNo5;
    protected Boolean                       yesNo6;
    protected Integer                       countAmt;
    protected String                        remarks;
    protected String                        name;
    protected String                        modifier;
    protected Calendar                      catalogedDate;
    protected Byte                          catalogedDatePrecision;   // Accurate to Year, Month, Day
    protected String                        catalogedDateVerbatim;
    protected String                        guid;
    protected String uniqueIdentifier;
    protected String                        altCatalogNumber;
    protected Boolean                       deaccessioned;
    protected String                        catalogNumber;
    protected Calendar                      inventoryDate;
    protected Byte                          inventoryDatePrecision;   // Accurate to Year, Month, Day
    protected Calendar date1;
    protected Byte date1Precision;
    protected String                        objectCondition;
    protected String                        availability;
    protected String                        restrictions;
    protected String                        notifications;
    protected BigDecimal                    totalValue;
    protected Byte							sgrStatus;
    protected String						ocr;
    protected String embargoReason;
    protected Calendar embargoStartDate;
    protected Byte embargoStartDatePrecision;
    protected Calendar embargoReleaseDate;
    protected Byte embargoReleaseDatePrecision;
    protected Agent embargoAuthority;
    
    // Security
    protected Byte                          visibility;
    protected SpecifyUser                   visibilitySetBy;
    
    // Relationships
    protected CollectingEvent               collectingEvent;
    protected Set<CollectionObjectCitation> collectionObjectCitations;
    protected Set<Preparation>              preparations;
    protected Set<CollectionObjectProperty> collectionObjectProperties;
    protected Set<Determination>            determinations;
    protected Set<Project>                  projects;
    // protected Set<DeaccessionPreparation> deaccessionPreparations;
    protected Set<OtherIdentifier>          otherIdentifiers;
    protected Collection                    collection;
    protected Accession                     accession;
    protected Agent                         cataloger;
    protected Agent                         inventorizedBy;
    protected Agent agent1;
    protected Container                     container;        // The container it belongs to   (Associated with)
    protected Container                     containerOwner;   // The container it is a part of (Parent Container)
    protected Appraisal                     appraisal;
    protected CollectionObjectAttribute     collectionObjectAttribute; // Specify 5 Attributes table
    protected Set<CollectionObjectAttr>     collectionObjectAttrs;      // Generic Expandable Attributes
    protected Set<CollectionRelationship>   leftSideRels;
    protected Set<CollectionRelationship>   rightSideRels;
    protected PaleoContext                  paleoContext;
    protected Set<DNASequence>              dnaSequences;
    protected FieldNotebookPage             fieldNotebookPage;
    
    protected Set<ConservDescription>         conservDescriptions;
    protected Set<TreatmentEvent>             treatmentEvents;
    protected Set<CollectionObjectAttachment> collectionObjectAttachments;

    protected Set<ExsiccataItem>              exsiccataItems;
    protected Set<VoucherRelationship> voucherRelationships;
    
    // Constructors

    /** default constructor */
    public CollectionObject()
    {
        // do nothing
    }

    /** constructor with id */
    public CollectionObject(Integer collectionObjectId) 
    {
        this.collectionObjectId = collectionObjectId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        collectionObjectId    = null;
        fieldNumber           = null;
        description           = null;
        numberOfDuplicates = null;
        text1                 = null;
        text2                 = null;
        text3                 = null;
        text4                 = null;
        text5                 = null;
        text6                 = null;
        text7                 = null;
        text8                 = null;
        reservedText          = null;
        reservedText2         = null;
        reservedText3         = null;
        number1               = null;
        number2               = null;
        integer1			  = null;
        integer2			  = null;
        reservedInteger3	  = null;
        reservedInteger4	  = null;
        yesNo1                = null;
        yesNo2                = null;
        yesNo3                = null;
        yesNo4                = null;
        yesNo5                = null;
        yesNo6                = null;
        countAmt              = null;
        remarks               = null;
        name                  = null;
        modifier              = null;
        catalogedDate         = null;
        catalogedDateVerbatim = null;
        date1 = null;
        date1Precision = 1;
        guid                  = null;
        uniqueIdentifier = null;
        altCatalogNumber      = null;
        deaccessioned         = null;
        catalogNumber         = null;
        objectCondition       = null;
        availability          = null;
        restrictions          = null;
        notifications         = null;
        totalValue            = null;
        visibility            = null;
        visibilitySetBy       = null; 
        sgrStatus             = null;
        
        collectingEvent       = null;
        appraisal             = null;
        collectionObjectCitations = new HashSet<>();
        collectionObjectAttrs = new HashSet<>();
        preparations          = new HashSet<>();
        collectionObjectProperties = new HashSet<>();
        determinations        = new HashSet<>();
        projects              = new HashSet<>();
        //deaccessionPreparations = new HashSet<DeaccessionPreparation>();
        otherIdentifiers      = new HashSet<>();
        collection            = null;
        accession             = null;
        cataloger             = null;
        inventorizedBy        = null;
        agent1 = null;
        container             = null;
        containerOwner        = null;
        paleoContext          = null;
        dnaSequences          = new HashSet<>();
        fieldNotebookPage     = null;
        
        leftSideRels          = new HashSet<>();
        rightSideRels         = new HashSet<>();
        
        conservDescriptions         = new HashSet<>();
        treatmentEvents             = new HashSet<>();
        collectionObjectAttachments = new HashSet<>();
        
        exsiccataItems              = new HashSet<>();
        voucherRelationships = new HashSet<>();


        hasGUIDField = true;

        setGUID();
    }
    // End Initializer
    
    public void initForSearch()
    {
        collection = new Collection();
        collection.initialize();
        
        accession = new Accession();
        accession.initialize();
        
        cataloger  = new Agent();
        cataloger.initialize();
    }

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "CollectionObjectID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getCollectionObjectId() {
        return this.collectionObjectId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.collectionObjectId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CollectionObject.class;
    }

    public void setCollectionObjectId(Integer collectionObjectId) {
        this.collectionObjectId = collectionObjectId;
    }

    /**
     *      * BiologicalObject (Bird, Fish, etc)
     */
    @Column(name = "FieldNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getFieldNumber()
    {
        return this.fieldNumber;
    }

    public void setFieldNumber(String fieldNumber)
    {
        this.fieldNumber = fieldNumber;
    }

    /**
     * Image, Sound, Preparation, Container(Container Label?) - this was suppose to be in Preparation
     */
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 1000)
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Lob
    @Column(name = "EmbargoReason", length = 4096)
    public String getEmbargoReason() {
        return this.embargoReason;
    }

    public void setEmbargoReason(String embargoReason) {
        this.embargoReason = embargoReason;
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
    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text6", length = 65535)
    public String getText6() {
        return this.text6;
    }

    public void setText6(String text6) {
        this.text6 = text6;
    }
    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text7", length = 65535)
    public String getText7() {
        return this.text7;
    }

    public void setText7(String text7) {
        this.text7 = text7;
    }
    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text8", length = 65535)
    public String getText8() {
        return this.text8;
    }

    public void setText8(String text8) {
        this.text8 = text8;
    }
    /**
    *
    */
   @Column(name = "ReservedText", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
   public String getReservedText() {
       return this.reservedText;
   }

   public void setReservedText(String reservedText) {
       this.reservedText = reservedText;
   }

   
    /**
 * @return the reservedText2
 */
   @Column(name = "ReservedText2", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
public String getReservedText2() {
	return reservedText2;
}

/**
 * @param reservedText2 the reservedText2 to set
 */
public void setReservedText2(String reservedText2) {
	this.reservedText2 = reservedText2;
}

/**
 * @return the reservedText3
 */
@Column(name = "ReservedText3", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
public String getReservedText3() {
	return reservedText3;
}

/**
 * @param reservedText3 the reservedText3 to set
 */
public void setReservedText3(String reservedText3) {
	this.reservedText3 = reservedText3;
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

    @Column(name = "NumberOfDuplicates", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumberOfDuplicates() {
        return numberOfDuplicates;
    }

    public void setNumberOfDuplicates(Integer numberOfDuplicates) {
        this.numberOfDuplicates = numberOfDuplicates;
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
     * @return the yesNo3
     */
    @Column(name="YesNo3",unique=false,nullable=true,updatable=true,insertable=true)
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
     * @return the yesNo4
     */
    @Column(name="YesNo4",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo4()
    {
        return yesNo4;
    }

    /**
     * @param yesNo4 the yesNo4 to set
     */
    public void setYesNo4(Boolean yesNo4)
    {
        this.yesNo4 = yesNo4;
    }

    /**
     * @return the yesNo5
     */
    @Column(name="YesNo5",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo5()
    {
        return yesNo5;
    }

    /**
     * @param yesNo5 the yesNo5 to set
     */
    public void setYesNo5(Boolean yesNo5)
    {
        this.yesNo5 = yesNo5;
    }

    /**
     * @return the yesNo6
     */
    @Column(name="YesNo6",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo6()
    {
        return yesNo6;
    }

    /**
     * @param yesNo6 the yesNo6 to set
     */
    public void setYesNo6(Boolean yesNo6)
    {
        this.yesNo6 = yesNo6;
    }

    /**
     *
     */
    @Column(name = "CountAmt", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getCountAmt() {
        return this.countAmt;
    }

    public void setCountAmt(Integer countAmt) {
        this.countAmt = countAmt;
    }

    @Transient
    public Integer getTotalCountAmt() { return getPrepTotalCount(this.getId());}

    @Transient
    public Integer getActualTotalCountAmt() { return getPrepUndisposedTotalCount(this.getId()); }

    private static Integer getPrepTotalCount(Integer id) {
        Integer result = null;
        if (id != null) {
            result = BasicSQLUtils.getCountAsInt("select sum(countAmt) from preparation where collectionobjectid = " + id);
        }
        return result;
    }

    private static Integer getPrepUndisposedTotalCount(Integer id) {
        Integer result = null;
        if (id != null) {
            Vector<Object> prepIds = BasicSQLUtils.querySingleCol("select preparationid from preparation where collectionobjectid = " + id);
            int runningTotal = 0;
            boolean nonNull = false;
            for (Object prepId : prepIds) {
                Object[] prepObj = new Object[1];
                prepObj[0] = prepId;
                Object prepCnt = Preparation.computeActualCountAmt(prepObj);
                if (prepCnt != null) {
                    runningTotal = runningTotal + Integer.valueOf(prepCnt.toString());
                    nonNull = true;
                }
            }
            if (nonNull) {
                result = runningTotal;
            }
        }
        return result;
    }

    @Transient
    public static List<String> getQueryableTransientFields() {
        List<String> result = new ArrayList<>();
        result.add("ActualTotalCountAmt");
        result.add("TotalCountAmt");
        return result;
    }

    public static Object getQueryableTransientFieldValue(String fldName, Object[] vals) {
        if (fldName.equalsIgnoreCase("ActualTotalCountAmt")) {
            return getPrepUndisposedTotalCount((Integer)vals[0]);
        } else if (fldName.equalsIgnoreCase("TotalCountAmt")) {
            return getPrepTotalCount((Integer)vals[0]);
        } else {
            log.error("Unknown calculated field: " + fldName);
            return null;
        }
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
   @Column(name = "Text3", length = 4096)
   public String getText3() {
       return this.text3;
   }

   public void setText3(String text3) {
       this.text3 = text3;
   }

    /**
     *
     */
    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     */
    @Column(name = "Modifier", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getModifier() {
        return this.modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    /**
     *
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "CatalogedDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getCatalogedDate() {
        return this.catalogedDate;
    }

    public void setCatalogedDate(Calendar catalogedDate) {
        this.catalogedDate = catalogedDate;
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
    @Temporal(TemporalType.DATE)
    @Column(name = "EmbargoStartDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getEmbargoStartDate() {
        return embargoStartDate;
    }

    /**
     *
     * @param embargoStartDate
     */
    public void setEmbargoStartDate(Calendar embargoStartDate) {
        this.embargoStartDate = embargoStartDate;
    }

    /**
     *
     * @return
     */
    @Column(name = "EmbargoStartDatePrecision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getEmbargoStartDatePrecision() {
        return embargoStartDatePrecision;
    }

    /**
     *
     * @param embargoStartDatePrecision
     */
    public void setEmbargoStartDatePrecision(Byte embargoStartDatePrecision) {
        this.embargoStartDatePrecision = embargoStartDatePrecision;
    }

    /**
     *
     * @return
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "EmbargoReleaseDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getEmbargoReleaseDate() {
        return embargoReleaseDate;
    }

    /**
     *
     * @param embargoReleaseDate
     */
    public void setEmbargoReleaseDate(Calendar embargoReleaseDate) {
        this.embargoReleaseDate = embargoReleaseDate;
    }

    /**
     *
     * @return
     */
    @Column(name = "EmbargoReleaseDatePrecision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getEmbargoReleaseDatePrecision() {
        return embargoReleaseDatePrecision;
    }

    /**
     *
     * @param embargoReleaseDatePrecision
     */
    public void setEmbargoReleaseDatePrecision(Byte embargoReleaseDatePrecision) {
        this.embargoReleaseDatePrecision = embargoReleaseDatePrecision;
    }
    /**
     *
     */
    @Column(name = "CatalogedDateVerbatim", length = 32, unique = false, nullable = true, insertable = true, updatable = true)
    public String getCatalogedDateVerbatim() {
        return this.catalogedDateVerbatim;
    }

    public void setCatalogedDateVerbatim(String catalogedDateVerbatim) {
        this.catalogedDateVerbatim = catalogedDateVerbatim;
    }

    /**
     *
     */
    @Column(name = "GUID", unique = false, nullable = true, insertable = true, updatable = false, length = 128)
    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

   /**
     *
     */
    @Column(name = "UniqueIdentifier", unique = false, nullable = true, insertable = true, updatable = false, length = 128)
    public String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    /**
     *
     */
    @Column(name = "AltCatalogNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getAltCatalogNumber() 
    {
        return this.altCatalogNumber;
    }

    public void setAltCatalogNumber(String altCatalogNumber) 
    {
        this.altCatalogNumber = altCatalogNumber;
    }

    /**
     *
     */
    @Column(name = "Deaccessioned", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getDeaccessioned() {
        return this.deaccessioned;
    }

    public void setDeaccessioned(Boolean deaccessioned) {
        this.deaccessioned = deaccessioned;
    }

    /**
     *
     */
    @Column(name = "CatalogNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getCatalogNumber() {
        return this.catalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }
    
    /**
     * @return the catalogedDatePrecision
     */
    @Column(name = "CatalogedDatePrecision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getCatalogedDatePrecision()
    {
        return catalogedDatePrecision != null ? this.catalogedDatePrecision : (byte)UIFieldFormatterIFace.PartialDateEnum.Full.ordinal();
    }

    /**
     * @param catalogedDatePrecision the catalogedDatePrecision to set
     */
    public void setCatalogedDatePrecision(Byte catalogedDatePrecision)
    {
        this.catalogedDatePrecision = catalogedDatePrecision;
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
     * @return the inventoryDate
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "InventoryDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getInventoryDate()
    {
        return inventoryDate;
    }

    /**
     * @param inventoryDate the inventoryDate to set
     */
    public void setInventoryDate(Calendar inventoryDate)
    {
        this.inventoryDate = inventoryDate;
    }
    /**
     * @return the InventoryDatePrecision
     */
    @Column(name = "InventoryDatePrecision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getInventoryDatePrecision()
    {
        return inventoryDatePrecision != null ? this.inventoryDatePrecision : (byte)UIFieldFormatterIFace.PartialDateEnum.Full.ordinal();
    }

    /**
     * @param inventoryDatePrecision the inventoryDatePrecision to set
     */
    public void setInventoryDatePrecision(Byte inventoryDatePrecision)
    {
        this.inventoryDatePrecision = inventoryDatePrecision;
    }

    /**
     * @return the condition
     */
    @Column(name = "ObjectCondition", unique = false, nullable = true, insertable = true, updatable = true, length=64)
    public String getObjectCondition()
    {
        return objectCondition;
    }

    /**
     * @param condition the condition to set
     */
    public void setObjectCondition(String objectCondition)
    {
        this.objectCondition = objectCondition;
    }

    /**
     * @return the availability
     */
    @Column(name = "Availability", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getAvailability()
    {
        return availability;
    }

    /**
     * @param availability the availability to set
     */
    public void setAvailability(String availability)
    {
        this.availability = availability;
    }

    /**
     * @return the restrictions
     */
    @Column(name = "Restrictions", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getRestrictions()
    {
        return restrictions;
    }

    /**
     * @param restrictions the restrictions to set
     */
    public void setRestrictions(String restrictions)
    {
        this.restrictions = restrictions;
    }

    /**
     * @return the notifications
     */
    @Column(name = "Notifications", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getNotifications()
    {
        return notifications;
    }

    /**
     * @param notifications the notifications to set
     */
    public void setNotifications(String notifications)
    {
        this.notifications = notifications;
    }
    /**
     * @return the totalValue
     */
    @Column(name = "TotalValue", unique = false, nullable = true, insertable = true, updatable = true, precision = 12, scale = 2)
    public BigDecimal getTotalValue()
    {
        return totalValue;
    }

    /**
     * @param totalValue the totalValue to set
     */
    public void setTotalValue(BigDecimal totalValue)
    {
        this.totalValue = totalValue;
    }


    /**
     * @return the projectNumber
     */
    @Column(name = "ProjectNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getProjectNumber()
    {
        return projectNumber;
    }

    /**
     * @param projectNumber the projectNumber to set
     */
    public void setProjectNumber(String projectNumber)
    {
        this.projectNumber = projectNumber;
    }

    /**
     *      * Indicates whether this record can be viewed - by owner, by institution, or by all
     */
    @Column(name = "Visibility", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
    public Byte getVisibility() {
        return this.visibility;
    }
    
    public void setVisibility(Byte visibility) {
        this.visibility = visibility;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isRestrictable()
     */
    @Transient
    @Override
    public boolean isRestrictable()
    {
        return true;
    }   
    
    /**
     * @return the current determination
     */
    @Transient
    public Determination getCurrentDetermination() {
    	try { 
    		for (Determination d : determinations) {
    			if (d.getIsCurrent()) {
    				return d;
    			}
    		}
    	} catch (org.hibernate.LazyInitializationException lix) {
            if (this.getId() != null) {
            	DataProviderSessionIFace session = null;
            	try {
            		session = DataProviderFactory.getInstance().createSession();
            		String hql = "from Determination WHERE collectionObjectId = " + this.getId() + " and isCurrent = true";
            		return (Determination)session.getData(hql);
            	} finally {
            		if (session != null) {
            			session.close();
            		}
            	}
            }
    	}
    	return null;
    }

    @Transient
    public int getTotalCount() {
        int result = 0;
        for (Preparation p : getPreparations()) {
            result += p.getCountAmt() != null ? p.getCountAmt() : 0;
        }
        return result;
    }

    @Transient
    public int getTotalActualCount() {
        int result = 0;
        for (Preparation p : getPreparations()) {
            result += p.getActualCountAmt();
        }
        return result;
    }



    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "VisibilitySetByID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpecifyUser getVisibilitySetBy() {
        return this.visibilitySetBy;
    }
    
    public void setVisibilitySetBy(SpecifyUser visibilitySetBy) {
        this.visibilitySetBy = visibilitySetBy;
    }
    
    /**
     *      * BiologicalObject (Bird, Fish, etc)
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    //@Cascade( { CascadeType.MERGE, CascadeType.LOCK })
    @Cascade( { CascadeType.LOCK })
    @JoinColumn(name = "CollectingEventID", unique = false, nullable = true, insertable = true, updatable = true)
    public CollectingEvent getCollectingEvent() {
        return this.collectingEvent;
    }

    public void setCollectingEvent(CollectingEvent collectingEvent) {
        this.collectingEvent = collectingEvent;
    }

    /**
     * @return the appraisal
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    //@Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "AppraisalID", unique = false, nullable = true, insertable = true, updatable = true)
    public Appraisal getAppraisal()
    {
        return appraisal;
    }

    /**
     * @param appraisal the appraisal to set
     */
    public void setAppraisal(Appraisal appraisal)
    {
        this.appraisal = appraisal;
    }

    /**
     *
     */
    @ManyToOne(cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @JoinColumn(name = "CollectionObjectAttributeID", unique = false, nullable = true, insertable = true, updatable = true)
    public CollectionObjectAttribute getCollectionObjectAttribute() 
    {
        return this.collectionObjectAttribute;
    }

    public void setCollectionObjectAttribute(CollectionObjectAttribute colObjAttribute) 
    {
        this.collectionObjectAttribute = colObjAttribute;
    }

    /**
     *
     */
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "collectionObject")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<CollectionObjectCitation> getCollectionObjectCitations() {
        return this.collectionObjectCitations;
    }

    public void setCollectionObjectCitations(Set<CollectionObjectCitation> collectionObjectCitations) {
        this.collectionObjectCitations = collectionObjectCitations;
    }

    /**
     *
     */
    @Transient
    public Set<AttributeIFace> getAttrs() 
    {
        return new HashSet<AttributeIFace>(this.collectionObjectAttrs);
    }

    public void setAttrs(Set<AttributeIFace> collectionObjectAttrs) 
    {
        this.collectionObjectAttrs.clear();
        for (AttributeIFace a : collectionObjectAttrs)
        {
            if (a instanceof CollectionObjectAttr)
            {
                this.collectionObjectAttrs.add((CollectionObjectAttr)a);
            }
        }
    }

    /**
     * @return the collectionObjectAttrs
     */
    @OneToMany(targetEntity=CollectionObjectAttr.class,
            cascade = {}, fetch = FetchType.LAZY, mappedBy="collectionObject")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<CollectionObjectAttr> getCollectionObjectAttrs()
    {
        return collectionObjectAttrs;
    }

    /**
     * @param collectionObjectAttrs the collectionObjectAttrs to set
     */
    public void setCollectionObjectAttrs(Set<CollectionObjectAttr> collectionObjectAttrs)
    {
        this.collectionObjectAttrs = collectionObjectAttrs;
    }

    /**
     *
     */
    @OneToMany(mappedBy = "collectionObject")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Preparation> getPreparations() {
        return this.preparations;
    }

    public void setPreparations(Set<Preparation> preparations) {
        this.preparations = preparations;
    }

    /**
     *
     */
    @OneToMany(mappedBy = "collectionObject")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<CollectionObjectProperty> getCollectionObjectProperties() {
        return this.collectionObjectProperties;
    }

    public void setCollectionObjectProperties(Set<CollectionObjectProperty> collectionObjectProperties) {
        this.collectionObjectProperties = collectionObjectProperties;
    }
    /**
     *
     */
    @OneToMany(mappedBy = "collectionObject")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<VoucherRelationship> getVoucherRelationships() {
        return this.voucherRelationships;
    }

    public void setVoucherRelationships(Set<VoucherRelationship> voucherRelationships) {
        this.voucherRelationships = voucherRelationships;
    }

    /**
     *
     */
    @OneToMany(mappedBy = "collectionObject")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Determination> getDeterminations() 
    {
        return this.determinations;
    }

    public void setDeterminations(Set<Determination> determinations) 
    {
        this.determinations = determinations;
    }

    /**
     *
     */
    @ManyToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="collectionObjects")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Set<Project> getProjects() 
    {
        return this.projects;
    }

    public void setProjects(Set<Project> projects) 
    {
        this.projects = projects;
    }
    
    /**
     *
     */
    //@ManyToOne(cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.LAZY)
    //@org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.LOCK })

    @JoinColumn(name = "PaleoContextID", unique = false, nullable = true, insertable = true, updatable = true)
    public PaleoContext getPaleoContext()
    {
        return this.paleoContext;
    }

    public void setPaleoContext(PaleoContext paleoContext)
    {
        this.paleoContext = paleoContext;
    }


//    /**
//     *
//     */
//    public Set<DeaccessionPreparation> getDeaccessionPreparations() {
//        return this.deaccessionPreparations;
//    }
//
//    public void setDeaccessionPreparations(Set<DeaccessionPreparation> deaccessionPreparations) {
//        this.deaccessionPreparations = deaccessionPreparations;
//    }

    /**
     * @return the collectionObjects
     */
    @OneToMany(mappedBy = "collectionObject")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<DNASequence> getDnaSequences()
    {
        return dnaSequences;
    }
    
    /**
     * @param dnaSequences the dnaSequences to set
     */
    public void setDnaSequences(Set<DNASequence> dnaSequences)
    {
        this.dnaSequences = dnaSequences;
    }

    /**
     * @return the fieldNotebookPage
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "FieldNotebookPageID", unique = false, nullable = true, insertable = true, updatable = true)
    public FieldNotebookPage getFieldNotebookPage()
    {
        return fieldNotebookPage;
    }

    /**
     * @param fieldNotebookPage the fieldNotebookPage to set
     */
    public void setFieldNotebookPage(FieldNotebookPage fieldNotebookPage)
    {
        this.fieldNotebookPage = fieldNotebookPage;
    }

    /**
     * @return the treatmentEvents
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collectionObject")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<TreatmentEvent> getTreatmentEvents()
    {
        return treatmentEvents;
    }

    /**
     * @param treatmentEvents the treatmentEvents to set
     */
    public void setTreatmentEvents(Set<TreatmentEvent> treatmentEvents)
    {
        this.treatmentEvents = treatmentEvents;
    }

    /**
     *
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "collectionObject")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<OtherIdentifier> getOtherIdentifiers() {
        return this.otherIdentifiers;
    }

    public void setOtherIdentifiers(Set<OtherIdentifier> otherIdentifiers) {
        this.otherIdentifiers = otherIdentifiers;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Collection getCollection() {
        return this.collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AccessionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Accession getAccession() {
        return this.accession;
    }

    public void setAccession(Accession accession) {
        this.accession = accession;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CatalogerID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getCataloger() {
        return this.cataloger;
    }

    public void setCataloger(Agent cataloger) {
        this.cataloger = cataloger;
    }

    /**
     *
     * @return
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "Agent1ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent1() {
        return agent1;
    }

    /**
     *
     * @param agent1
     */
    public void setAgent1(Agent agent1) {
        this.agent1 = agent1;
    }

    /**
     *
     * @return
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "EmbargoAuthorityID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getEmbargoAuthority() {
        return embargoAuthority;
    }

    /**
     *
     * @param embargoAuthority
     */
    public void setEmbargoAuthority(Agent embargoAuthority) {
        this.embargoAuthority = embargoAuthority;
    }

    /**
    *
    */
   @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
   @JoinColumn(name = "InventorizedByID", unique = false, nullable = true, insertable = true, updatable = true)
   public Agent getInventorizedBy() {
       return this.inventorizedBy;
   }

   public void setInventorizedBy(Agent inventorizedBy) {
       this.inventorizedBy = inventorizedBy;
   }

    /**
     *      Container
     */
    @ManyToOne(cascade = { }, fetch = FetchType.LAZY)
    @Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "ContainerID", unique = false, nullable = true, insertable = true, updatable = true)
    public Container getContainer() {
        return this.container;
    }

    public void setContainer(final Container container) {
        this.container = container;
    }
    
    /**
     *      * Preparation, Container
     */
    @ManyToOne(cascade = { }, fetch = FetchType.LAZY)
    @Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "ContainerOwnerID", unique = false, nullable = true, insertable = true, updatable = true)
    public Container getContainerOwner() {
        return this.containerOwner;
    }

    public void setContainerOwner(final Container containerOwner) {
        this.containerOwner = containerOwner;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "leftSide")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<CollectionRelationship> getLeftSideRels() 
    {
        return this.leftSideRels;
    }
    
    public void setLeftSideRels(Set<CollectionRelationship> leftSideRels) 
    {
        this.leftSideRels = leftSideRels;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "rightSide")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<CollectionRelationship> getRightSideRels() 
    {
        return this.rightSideRels;
    }
    
    public void setRightSideRels(Set<CollectionRelationship> rightSideRels) 
    {
        this.rightSideRels = rightSideRels;
    }
    

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collectionObject")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<ConservDescription> getConservDescriptions()
    {
        return this.conservDescriptions;
    }

    public void setConservDescriptions(final Set<ConservDescription> conservDescriptions)
    {
        this.conservDescriptions = conservDescriptions;
    }

    @OneToMany(mappedBy = "collectionObject")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<CollectionObjectAttachment> getCollectionObjectAttachments()
    {
        return collectionObjectAttachments;
    }

    public void setCollectionObjectAttachments(Set<CollectionObjectAttachment> collectionObjectAttachments)
    {
        this.collectionObjectAttachments = collectionObjectAttachments;
    }

    @OneToMany(mappedBy = "collectionObject")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<ExsiccataItem> getExsiccataItems()
    {
        return exsiccataItems;
    }
    
    public void setExsiccataItems(Set<ExsiccataItem> exsiccataItems)
    {
        this.exsiccataItems = exsiccataItems;
    }
    
   //---------------------------------------------------------------------------
    // Overrides DataModelObjBase
    //---------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        if (StringUtils.isNotEmpty(catalogNumber))
        {
            UIFieldFormatterIFace fmt = AppContextMgr.getInstance().getFormatter("CollectionObject", "CatalogNumber");
            if (fmt != null)
            {
                return fmt.formatToUI(catalogNumber).toString();
            }
        }
        return fieldNumber != null ? fieldNumber : super.getIdentityTitle();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    @Override
    public String toString()
    {
        return getIdentityTitle();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Collection.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return collection != null ? collection.getId() : null;
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
        return 1;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
    @Transient
    public Set<CollectionObjectAttachment> getAttachmentReferences()
    {
        return collectionObjectAttachments;
    }

    
    /**
	 * @return the sgrStatus
	 */
    @Column(name = "SGRStatus", unique = false, nullable = true, insertable = true, updatable = true)
	public Byte getSgrStatus() 
	{
		return sgrStatus;
	}

	/**
	 * @param sgrStatus the sgrStatus to set
	 */
	public void setSgrStatus(Byte sgrStatus) 
	{
		this.sgrStatus = sgrStatus;
	}
	
	

    /**
	 * @return the ocr
	 */
    @Lob
    @Column(name = "OCR", length = 4096)
    public String getOcr() 
	{
		return ocr;
	}

	/**
	 * @param ocr the ocr to set
	 */
	public void setOcr(String ocr) 
	{
		this.ocr = ocr;
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        determinations.size();
        collectionObjectProperties.size();
        preparations.size();
        for (Preparation prep : preparations)
        {
            prep.forceLoad();
        }
        collectionObjectAttachments.size();
        collectionObjectCitations.size();
        if (collection != null)
        {
            collection.getId();
        }
        
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(OtherIdentifier.getClassTableId());
        if (ti != null && !ti.isHidden())
        {
            otherIdentifiers.size();
        }
        
        CollectingEvent ce = getCollectingEvent();
        if (ce != null)// && AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent())
        {
            ce.forceLoad(AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent());
        }
        
        //projects.size();
        for (Project prj : projects)
        {
        	//get something harmless, don't load collectionobjects
        	//fix for bug #9381 (probably relates to bugs 9380 and 9382)
        	//prj.getCollectionObjects().size();
        	
        	prj.getProjectName();
        }

        dnaSequences.size();
        for (DNASequence dnaS : dnaSequences) {
            dnaS.forceLoad();
        }

        conservDescriptions.size();
        for (ConservDescription cd : conservDescriptions) {
            cd.forceLoad();
        }
    }
    
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    /*@Override
    public Object clone() throws CloneNotSupportedException
    {
        CollectionObject obj = (CollectionObject)super.clone();
        obj.init();
        
        obj.collectionObjectId = null;
        obj.collectionObjectAttribute     = null;
        obj.collectionObjectAttrs         = new HashSet<CollectionObjectAttr>();
        obj.collectionObjectAttachments   = new HashSet<CollectionObjectAttachment>();
        
        for (Collector collector : collectors)
        {
            Collector newCollector = (Collector)collector.clone();
            newCollector.setCollectionObject(obj);
            obj.collectors.add(newCollector);
        }
        
        // Clone Attributes
        obj.collectionObjectAttribute    = collectionObjectAttribute != null ? (CollectionObjectAttribute)collectionObjectAttribute.clone() : null;
        obj.collectionObjectAttrs        = new HashSet<CollectionObjectAttr>();
        for (CollectionObjectAttr cea : collectionObjectAttrs)
        {
            obj.collectionObjectAttrs.add((CollectionObjectAttr)cea.clone());
        }
         
        return obj;
    }*/
    
    //----------------------------------------------------------------------
    //-- Comparable Interface
    //----------------------------------------------------------------------


    

	/* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CollectionObject obj)
    {
        if (obj == null) return 1;
        
        String myCatNum = catalogNumber == null ? "" : catalogNumber;
        String objCatNum = obj.catalogNumber == null ? "" : obj.catalogNumber;
        if (!"".equals(myCatNum) || !"".equals(objCatNum)) {
        	return myCatNum.compareTo(objCatNum);
        } else {
        	Timestamp myStamp = timestampCreated;
        	Timestamp objStamp = obj.timestampCreated;
        	if (myStamp != null && objStamp != null) {
        		return myStamp.compareTo(objStamp);
        	} else if (myStamp != null) {
        		return 1;
        	} else if (objStamp != null) {
        		return -1;
        	} else {
        		return 0;
        	}
        }
    }

}
