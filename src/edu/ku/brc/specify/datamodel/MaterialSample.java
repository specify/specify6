/**
 * 
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

/**
 * @author timo
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "materialsample")
@org.hibernate.annotations.Table(appliesTo="materialsample", indexes =
{   @Index (name="DesignationIDX", columnNames={"GGBNSampleDesignation"})
})
public class MaterialSample extends CollectionMember {
	protected Integer materialSampleId;
    protected String  guid;
    protected BigDecimal GGBN_concentration;
    protected String GGBN_concentrationUnit;
    protected String GGBN_materialSampleType;
    protected String GGBN_medium;
    protected String GGBN_purificationMethod;
    protected String GGBN_quality;
	protected Calendar GGBN_qualityCheckDate;
	protected Calendar extractionDate;
    protected String GGBN_qualityRemarks;
    protected BigDecimal GGBN_absorbanceRatio260_230;
    protected BigDecimal GGBN_absorbanceRatio260_280;
    protected String GGBN_absorbanceRatioMethod;
    protected BigDecimal GGBN_volume;
    protected String GGBN_volumeUnit;
    protected BigDecimal GGBN_weight;
    protected String GGBN_weightMethod;
    protected String GGBN_weightUnit;
    protected BigDecimal GGBN_sampleSize;
    protected String GGBN_sampleDesignation;
    
    protected String sraSampleID;
    protected String sraBioSampleID;
    protected String sraProjectID;
    protected String sraBioProjectID;

	protected String remarks;
	protected String text1;
	protected String text2;
	protected BigDecimal number1;
	protected BigDecimal number2;
	protected Integer integer1;
	protected Integer integer2;
	protected Boolean yesNo1;
	protected Boolean yesNo2;
	
	protected String reservedText3;
	protected String reservedText4;
	protected Integer reservedInteger3;
	protected Integer reservedInteger4;
	protected BigDecimal reservedNumber3;
	protected BigDecimal reservedNumber4;

    protected Preparation preparation;
    protected Agent extractor;
    protected Set<DNASequence> dnaSequences;

	/**
	 * 
	 */
	public MaterialSample() {
		// nothing TODO 
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
	 */
	@Override
	public void initialize() {
		super.init();
		materialSampleId = null;
        GGBN_concentration = null;
        GGBN_concentrationUnit = null;
        GGBN_materialSampleType = null;
        GGBN_medium = null;
        GGBN_purificationMethod = null;
        GGBN_quality = null;
        GGBN_qualityCheckDate = null;
        extractionDate = null;
        GGBN_qualityRemarks = null;
        GGBN_absorbanceRatio260_230 = null;
        GGBN_absorbanceRatio260_280 = null;
        GGBN_absorbanceRatioMethod = null;
        GGBN_volume = null;
        GGBN_volumeUnit = null;
        GGBN_weight = null;
        GGBN_weightUnit = null;
        GGBN_weightMethod = null;
        GGBN_sampleSize = null;
        GGBN_sampleDesignation = null;
 
        sraSampleID = null;
        sraBioSampleID = null;
        sraProjectID = null;
        sraBioProjectID = null;

		remarks = null;
		text1 = null;
		text2 = null;
		number1 = null;
		number2 = null;
		integer1 = null;
		integer2 = null;
		yesNo1 = null;
		yesNo2 = null;

		reservedText3 = null;
		reservedText4 = null;
		reservedNumber3 = null;
		reservedNumber4 = null;
		reservedInteger3 = null;
		reservedInteger4 = null;

        extractor = null;
		preparation = null;
        dnaSequences = new HashSet<DNASequence>();
	}


	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "ExtractorID", unique = false, nullable = true, insertable = true, updatable = true)
	public Agent getExtractor() {
		return extractor;
	}

	public void setExtractor(Agent extractor) {
		this.extractor = extractor;
	}

	/**
	 * @return the sraSampleID
	 */
    @Column(name = "SRASampleID", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getSraSampleID() {
		return sraSampleID;
	}

	/**
	 * @param sraSampleID the sraSampleID to set
	 */
	public void setSraSampleID(String sraSampleID) {
		this.sraSampleID = sraSampleID;
	}

	/**
	 * @return the sraBioSampleID
	 */
    @Column(name = "SRABioSampleID", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getSraBioSampleID() {
		return sraBioSampleID;
	}

	/**
	 * @param sraBioSampleID the sraBioSampleID to set
	 */
	public void setSraBioSampleID(String sraBioSampleID) {
		this.sraBioSampleID = sraBioSampleID;
	}

	/**
	 * @return the sraProjectID
	 */
    @Column(name = "SRAProjectID", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getSraProjectID() {
		return sraProjectID;
	}

	/**
	 * @param sraProjectID the sraProjectID to set
	 */
	public void setSraProjectID(String sraProjectID) {
		this.sraProjectID = sraProjectID;
	}

	/**
	 * @return the sraBioProjectID
	 */
    @Column(name = "SRABioProjectID", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getSraBioProjectID() {
		return sraBioProjectID;
	}

	/**
	 * @param sraBioProjectID the sraBioProjectID to set
	 */
	public void setSraBioProjectID(String sraBioProjectID) {
		this.sraBioProjectID = sraBioProjectID;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
	 */
	@Override
	@Transient
	public Integer getParentId() {
		return preparation == null ? null : preparation.getId();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
	 */
	@Override
	@Transient
	public Integer getParentTableId() {
		return Preparation.getClassTableId();
	}
	
    /**
	 * @return the materialSampleId
	 */
    @Id
    @GeneratedValue
    @Column(name = "MaterialSampleID", unique = false, nullable = false, insertable = true, updatable = true)
	public Integer getMaterialSampleId() {
		return materialSampleId;
	}

	/**
	 * @param materialSampleId the materialSampleId to set
	 */
	public void setMaterialSampleId(Integer materialSampleId) {
		this.materialSampleId = materialSampleId;
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
 * @return the gGBN_sampleDesignation
 */
   @Column(name = "GGBNSampleDesignation", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
   public String getGGBN_sampleDesignation() {
	   return GGBN_sampleDesignation;
   }

   /**
    * @param gGBN_sampleDesignation the gGBN_sampleDesignation to set
    */
   public void setGGBN_sampleDesignation(String gGBN_sampleDesignation) {
	   GGBN_sampleDesignation = gGBN_sampleDesignation;
   }

	/**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PreparationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Preparation getPreparation() {
        return this.preparation;
    }
    
    public void setPreparation(Preparation preparation) {
        this.preparation = preparation;
    }

    /**
     * @return the dnaSequences
     */
    @OneToMany(mappedBy = "materialSample")
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
	 * @return the gGBN_concentration
	 */
    @Column(name = "GGBNConcentration", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
	public BigDecimal getGGBN_concentration() {
		return GGBN_concentration;
	}

	/**
	 * @param gGBN_concentration the gGBN_concentration to set
	 */
	public void setGGBN_concentration(BigDecimal gGBN_concentration) {
		GGBN_concentration = gGBN_concentration;
	}

	/**
	 * @return the gGBN_materialSampleType
	 */
    @Column(name = "GGBNMaterialSampleType", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getGGBN_materialSampleType() {
		return GGBN_materialSampleType;
	}

	/**
	 * @param gGBN_materialSampleType the gGBN_materialSampleType to set
	 */
	public void setGGBN_materialSampleType(String gGBN_materialSampleType) {
		GGBN_materialSampleType = gGBN_materialSampleType;
	}

	/**
	 * @return the gGBN_medium
	 */
    @Column(name = "GGBNMedium", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getGGBN_medium() {
		return GGBN_medium;
	}

	/**
	 * @param gGBN_medium the gGBN_medium to set
	 */
	public void setGGBN_medium(String gGBN_medium) {
		GGBN_medium = gGBN_medium;
	}

	/**
	 * @return the gGBN_purificationMethod
	 */
    @Column(name = "GGBNPurificationMethod", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getGGBN_purificationMethod() {
		return GGBN_purificationMethod;
	}

	/**
	 * @param gGBN_purificationMethod the gGBN_purificationMethod to set
	 */
	public void setGGBN_purificationMethod(String gGBN_purificationMethod) {
		GGBN_purificationMethod = gGBN_purificationMethod;
	}

	/**
	 * @return the gGBN_quality
	 */
    @Column(name = "GGBNQuality", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getGGBN_quality() {
		return GGBN_quality;
	}

	/**
	 * @param gGBN_quality the gGBN_quality to set
	 */
	public void setGGBN_quality(String gGBN_quality) {
		GGBN_quality = gGBN_quality;
	}

	/**
	 * @return the gGBN_qualityCheckDate
	 */
    @Temporal(TemporalType.DATE)
    @Column(name = "ExtractionDate")
	public Calendar getExtractionDate() {
		return extractionDate;
	}

	/**
	 * @param extractionDate the extractionDate to set
	 */
	public void setExtractionDate(Calendar extractionDate) {
		this.extractionDate = extractionDate;
	}

	/**
	 * @return the gGBN_qualityCheckDate
	 */
	@Temporal(TemporalType.DATE)
	@Column(name = "GGBNQualityCheckDate")
	public Calendar getGGBN_qualityCheckDate() {
		return GGBN_qualityCheckDate;
	}

	/**
	 * @param gGBN_qualityCheckDate the gGBN_qualityCheckDate to set
	 */
	public void setGGBN_qualityCheckDate(Calendar gGBN_qualityCheckDate) {
		GGBN_qualityCheckDate = gGBN_qualityCheckDate;
	}

	/**
	 * @return the gGBN_qualityRemarks
	 */
    @Lob
    @Column(name = "GGBNQualityRemarks", length = 4096)
	public String getGGBN_qualityRemarks() {
		return GGBN_qualityRemarks;
	}

	/**
	 * @param gGBN_qualityRemarks the gGBN_qualityRemarks to set
	 */
	public void setGGBN_qualityRemarks(String gGBN_qualityRemarks) {
		GGBN_qualityRemarks = gGBN_qualityRemarks;
	}

	/**
	 * @return the gGBN_absorbanceRatio260_230
	 */
    @Column(name = "GGBNAbsorbanceRatio260_230", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
	public BigDecimal getGGBN_absorbanceRatio260_230() {
		return GGBN_absorbanceRatio260_230;
	}

	/**
	 * @param gGBN_absorbanceRatio260_230 the gGBN_absorbanceRatio260_230 to set
	 */
	public void setGGBN_absorbanceRatio260_230(BigDecimal gGBN_absorbanceRatio260_230) {
		GGBN_absorbanceRatio260_230 = gGBN_absorbanceRatio260_230;
	}

	/**
	 * @return the gGBN_absorbanceRation260_280
	 */
    @Column(name = "GGBNAbsorbanceRatio260_280", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
	public BigDecimal getGGBN_absorbanceRatio260_280() {
		return GGBN_absorbanceRatio260_280;
	}

	/**
	 * @param gGBN_absorbanceRation260_280 the gGBN_absorbanceRation260_280 to set
	 */
	public void setGGBN_absorbanceRatio260_280(BigDecimal gGBN_absorbanceRatio260_280) {
		GGBN_absorbanceRatio260_280 = gGBN_absorbanceRatio260_280;
	}

	/**
	 * @return the gGBN_concentrationUnit
	 */
    @Column(name = "GGBNConcentrationUnit", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getGGBN_concentrationUnit() {
		return GGBN_concentrationUnit;
	}

	/**
	 * @param gGBN_concentrationUnit the gGBN_concentrationUnit to set
	 */
	public void setGGBN_concentrationUnit(String gGBN_concentrationUnit) {
		GGBN_concentrationUnit = gGBN_concentrationUnit;
	}

	/**
	 * @return the gGBN_volume
	 */
    @Column(name = "GGBNVolume", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
	public BigDecimal getGGBN_volume() {
		return GGBN_volume;
	}

	/**
	 * @param gGBN_volume the gGBN_volume to set
	 */
	public void setGGBN_volume(BigDecimal gGBN_volume) {
		GGBN_volume = gGBN_volume;
	}

	/**
	 * @return the gGBN_volumeUnit
	 */
    @Column(name = "GGBNVolumeUnit", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getGGBN_volumeUnit() {
		return GGBN_volumeUnit;
	}

	/**
	 * @param gGBN_volumeUnit the gGBN_volumeUnit to set
	 */
	public void setGGBN_volumeUnit(String gGBN_volumeUnit) {
		GGBN_volumeUnit = gGBN_volumeUnit;
	}

	/**
	 * @return the gGBN_weight
	 */
    @Column(name = "GGBNWeight", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
	public BigDecimal getGGBN_weight() {
		return GGBN_weight;
	}

	/**
	 * @param gGBN_weight the gGBN_weight to set
	 */
	public void setGGBN_weight(BigDecimal gGBN_weight) {
		GGBN_weight = gGBN_weight;
	}

	/**
	 * @return the gGBN_weightUnit
	 */
    @Column(name = "GGBNWeightUnit", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getGGBN_weightUnit() {
		return GGBN_weightUnit;
	}

	/**
	 * @param gGBN_weightUnit the gGBN_weightUnit to set
	 */
	public void setGGBN_weightUnit(String gGBN_weightUnit) {
		GGBN_weightUnit = gGBN_weightUnit;
	}

	/**
	 * @return the gGBN_weightMethod
	 */
    @Column(name = "GGBNWeightMethod", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getGGBN_weightMethod() {
		return GGBN_weightMethod;
	}

	/**
	 * @param gGBN_weightUnit the gGBN_weightMethod to set
	 */
	public void setGGBN_weightMethod(String gGBN_weightMethod) {
		GGBN_weightMethod = gGBN_weightMethod;
	}
	/**
	 * @return the gGBN_absorbanceRatioMethod
	 */
    @Column(name = "GGBNRAbsorbanceRatioMethod", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getGGBN_absorbanceRatioMethod() {
		return GGBN_absorbanceRatioMethod;
	}

	/**
	 * @param gGBN_absorbanceRatioMethod the gGBN_absorbanceRatioMethod to set
	 */
	public void setGGBN_absorbanceRatioMethod(String gGBN_absorbanceRatioMethod) {
		GGBN_absorbanceRatioMethod = gGBN_absorbanceRatioMethod;
	}

	/**
	 * @return the gGBN_sampleSize
	 */
    @Column(name = "GGBNSampleSize", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
	public BigDecimal getGGBN_sampleSize() {
		return GGBN_sampleSize;
	}

	/**
	 * @param gGBN_sampleSize the gGBN_sampleSize to set
	 */
	public void setGGBN_sampleSize(BigDecimal gGBN_sampleSize) {
		GGBN_sampleSize = gGBN_sampleSize;
	}

	/**
	 * @return the remarks
	 */
    @Lob
    @Column(name = "Remarks", length = 4096)
	public String getRemarks() {
		return remarks;
	}

	/**
	 * @param remarks the remarks to set
	 */
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	/**
	 * @return the text1
	 */
    @Lob
    @Column(name = "Text1", length = 4096)
	public String getText1() {
		return text1;
	}

	/**
	 * @param text1 the text1 to set
	 */
	public void setText1(String text1) {
		this.text1 = text1;
	}

	/**
	 * @return the text2
	 */
    @Lob
    @Column(name = "Text2", length = 4096)
	public String getText2() {
		return text2;
	}

	/**
	 * @param text2 the text2 to set
	 */
	public void setText2(String text2) {
		this.text2 = text2;
	}

	/**
	 * @return the number1
	 */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
	public BigDecimal getNumber1() {
		return number1;
	}

	/**
	 * @param number1 the number1 to set
	 */
	public void setNumber1(BigDecimal number1) {
		this.number1 = number1;
	}

	/**
	 * @return the number2
	 */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
	public BigDecimal getNumber2() {
		return number2;
	}

	/**
	 * @param number2 the number2 to set
	 */
	public void setNumber2(BigDecimal number2) {
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
	 * @return the yesNo1
	 */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
	public Boolean getYesNo1() {
		return yesNo1;
	}

	/**
	 * @param yesNo1 the yesNo1 to set
	 */
	public void setYesNo1(Boolean yesNo1) {
		this.yesNo1 = yesNo1;
	}

	/**
	 * @return the yesNo2
	 */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
	public Boolean getYesNo2() {
		return yesNo2;
	}

	/**
	 * @param yesNo2 the yesNo2 to set
	 */
	public void setYesNo2(Boolean yesNo2) {
		this.yesNo2 = yesNo2;
	}

	/**
	 * @return the text1
	 */
    @Lob
    @Column(name = "ReservedText3", length = 4096)
	public String getReservedText3() {
		return reservedText3;
	}

	/**
	 * @param text3
	 */
	public void setReservedText3(String text3) {
		this.reservedText3 = text3;
	}

	/**
	 * @return 
	 */
    @Lob
    @Column(name = "ReservedText4", length = 4096)
	public String getReservedText4() {
		return reservedText4;
	}

	/**
	 * @param text4
	 */
	public void setReservedText4(String text4) {
		this.reservedText4 = text4;
	}

	/**
	 * @return 
	 */
    @Column(name = "ReservedNumber3", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
	public BigDecimal getReservedNumber3() {
		return reservedNumber3;
	}

	/**
	 * @param number3 
	 */
	public void setReservedNumber3(BigDecimal number3) {
		this.reservedNumber3 = number3;
	}

	/**
	 * @return 
	 */
    @Column(name = "ReservedNumber4", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
	public BigDecimal getReservedNumber4() {
		return reservedNumber4;
	}

	/**
	 * @param number4
	 */
	public void setReservedNumber4(BigDecimal number4) {
		this.reservedNumber4 = number4;
	}

	/**
	 * @return 
	 */
    @Column(name = "ReservedInteger3", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getReservedInteger3() {
		return reservedInteger3;
	}

	/**
	 * @param integer3
	 */
	public void setReservedInteger3(Integer integer3) {
		this.reservedInteger3 = integer3;
	}

	/**
	 * @return 	 
	 * */
    @Column(name = "ReservedInteger4", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getReservedInteger4() {
		return reservedInteger4;
	}

	/**
	 * @param integer4
	 * 	 
	 * */
	public void setReservedInteger4(Integer integer4) {
		this.reservedInteger4 = integer4;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
	 */
	@Override
	@Transient
	public Integer getId() {
		return materialSampleId;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
	 */
	@Override
	@Transient
	public int getTableId() {
		return MaterialSample.getClassTableId();
	}

    /**
     * @return the Table ID for the class.
     */
	@Transient
    public static int getClassTableId()
    {
        return 151;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
	 */
	@Override
	@Transient
	public Class<?> getDataClass() {
		return MaterialSample.class;
	}

}
