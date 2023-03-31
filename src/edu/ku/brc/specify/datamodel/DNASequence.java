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
import java.util.TreeSet;
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
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 16, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "dnasequence")
@org.hibernate.annotations.Table(appliesTo="dnasequence", indexes =
    {   @Index (name="GenBankAccIDX", columnNames={"GenBankAccessionNumber"}),
        @Index (name="BOLDBarcodeIDX", columnNames={"BOLDBarcodeID"}),
        @Index (name="BOLDSampleIDX", columnNames=("BOLDSampleID"))
    })
public class DNASequence extends CollectionMember implements AttachmentOwnerIFace<DNASequenceAttachment>
{
	protected Integer						dnaSequenceId;
	protected String						moleculeType;
	protected String						targetMarker;
	protected String						boldBarcodeId;
	protected String						genbankAccessionNumber;
	protected String						boldSampleId;
	protected String						boldTranslationMatrix;
	protected String	                    geneSequence;
	protected String						remarks;
	protected String						text1;
	protected String						text2;
	protected String						text3;
	protected Boolean						yesNo1;
	protected Boolean						yesNo2;
	protected Boolean						yesNo3;
	protected Integer						totalResidues;
	protected Integer						compA;
	protected Integer						compG;
	protected Integer						compC;
	protected Integer						compT;
	protected Integer						ambiguousResidues;
	protected BigDecimal						    number1;
	protected BigDecimal						    number2;
	protected BigDecimal						    number3;
	protected Calendar						boldLastUpdateDate;
	protected Calendar sequenceDate;
	protected Byte                          sequenceDatePrecision;   // Accurate to Year, Month, Day
	protected Calendar extractionDate;
	protected Byte                          extractionDatePrecision;   // Accurate to Year, Month, Day
    protected Agent	extractor;
    protected Agent							sequencer;
	protected Set<Extractor>	extractors;
	protected Set<PcrPerson>							pcrPersons;
	protected CollectionObject				collectionObject;
	protected MaterialSample				materialSample;
    protected Set<DNASequenceAttachment>    attachments;
	protected Set<DNASequencingRun>			dnaSequencingRuns;
    
    /**
     * 
     */
    public DNASequence()
    {
        // no op
    }
 

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();

        dnaSequenceId = null;
        moleculeType = null;
        targetMarker = null;
        boldBarcodeId = null;
        genbankAccessionNumber = null;
        boldSampleId = null;
        boldTranslationMatrix = null;
        geneSequence = null;
        remarks = null;
        text1 = null;
        text2 = null;
        text3 = null;
        yesNo1 = null;
        yesNo2 = null;
        yesNo3 = null;
        totalResidues = null;
        compA = null;
        compG = null;
        compC = null;
        compT = null;
        ambiguousResidues = null;
        number1 = null;
        number2 = null;
        number3 = null;
        boldLastUpdateDate = null;
        sequenceDate = null;
		sequenceDatePrecision = 1;
		extractionDate = null;
		extractionDatePrecision = 1;
		extractor = null;
		sequencer = null;
		extractors = new HashSet<Extractor>();
        pcrPersons = new HashSet<PcrPerson>();
        collectionObject = null;
        materialSample = null;
        attachments = new TreeSet<DNASequenceAttachment>();
        dnaSequencingRuns = new HashSet<DNASequencingRun>();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
		for (DNASequencingRun dnar : dnaSequencingRuns) {
			dnar.forceLoad();
		}
        attachments.size();
    }

    /**
     * @return the moleculeType
     */
    @Column(name = "MoleculeType", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getMoleculeType()
    {
    	return this.moleculeType;
    }
    
    
    /**
	 * @return the targetMarker
	 */
    @Column(name = "TargetMarker", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
	public String getTargetMarker()
	{
		return targetMarker;
	}


	/**
	 * @return the boldBarcodeId
	 */
    @Column(name = "BOLDBarcodeID", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
	public String getBoldBarcodeId()
	{
		return boldBarcodeId;
	}


	/**
	 * @return the genbankAccessionNumber
	 */
    @Column(name = "GenBankAccessionNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
	public String getGenbankAccessionNumber()
	{
		return genbankAccessionNumber;
	}


	/**
	 * @return the boldSampleId
	 */
    @Column(name = "BOLDSampleID", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
	public String getBoldSampleId()
	{
		return boldSampleId;
	}


	/**
	 * @return the boldTranslationMatrix
	 */
    @Column(name = "BOLDTranslationMatrix", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getBoldTranslationMatrix()
	{
		return boldTranslationMatrix;
	}

	/**
	 * @return the geneSequence
	 */
    @Lob
    @Column(name = "GeneSequence", unique = false, nullable = true, insertable = true, updatable = true, length = 2048)
	public String getGeneSequence()
	{
		return geneSequence;
	}

	/**
	 * @return the remarks
	 */
    @Lob
    @Column(name = "Remarks", unique = false, nullable = true, insertable = true, updatable = true, length = 4096)
	public String getRemarks()
	{
		return remarks;
	}


	/**
	 * @return the text1
	 */
    @Column(name = "Text1", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
	public String getText1()
	{
		return text1;
	}


	/**
	 * @return the text2
	 */
    @Column(name = "Text2", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
	public String getText2()
	{
		return text2;
	}


	/**
	 * @return the text3
	 */
    @Column(name = "Text3", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getText3()
	{
		return text3;
	}


	/**
	 * @return the yesNo1
	 */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
	public Boolean getYesNo1()
	{
		return yesNo1;
	}


	/**
	 * @return the yesNo2
	 */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
	public Boolean getYesNo2()
	{
		return yesNo2;
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
	 * @return the totalResidues
	 */
    @Column(name = "TotalResidues", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getTotalResidues()
	{
		return totalResidues;
	}


	/**
	 * @return the compA
	 */
    @Column(name = "CompA", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getCompA()
	{
		return compA;
	}


	/**
	 * @return the compG
	 */
    @Column(name = "CompG", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getCompG()
	{
		return compG;
	}


	/**
	 * @return the compC
	 */
    @Column(name = "CompC", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getCompC()
	{
		return compC;
	}


	/**
	 * @return the compT
	 */
    @Column(name = "compT", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getCompT()
	{
		return compT;
	}


	/**
	 * @return the ambiguousResidues
	 */
    @Column(name = "AmbiguousResidues", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getAmbiguousResidues()
	{
		return ambiguousResidues;
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
	 * @return the number2
	 */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
	public BigDecimal getNumber2()
	{
		return number2;
	}


	/**
	 * @return the number3
	 */
    @Column(name = "Number3", unique = false, nullable = true, insertable = true, updatable = true, precision = 20, scale = 10)
	public BigDecimal getNumber3()
	{
		return number3;
	}


	/**
	 * @return the boldLastUpdateDate
	 */
    @Temporal(TemporalType.DATE)
    @Column(name = "BOLDLastUpdateDate", unique = false, nullable = true, insertable = true, updatable = true)
	public Calendar getBoldLastUpdateDate()
	{
		return boldLastUpdateDate;
	}

    /**
	 * @param targetMarker the targetMarker to set
	 */
	public void setTargetMarker(String targetMarker) 
	{
		this.targetMarker = targetMarker;
	}


	/**
	 * @param boldBarcodeId the boldBarcodeId to set
	 */
	public void setBoldBarcodeId(String boldBarcodeId) 
	{
		this.boldBarcodeId = boldBarcodeId;
	}


	/**
	 * @param genbankAccessionNumber the genbankAccessionNumber to set
	 */
	public void setGenbankAccessionNumber(String genbankAccessionNumber) 
	{
		this.genbankAccessionNumber = genbankAccessionNumber;
	}


	/**
	 * @param boldSampleId the boldSampleId to set
	 */
	public void setBoldSampleId(String boldSampleId) 
	{
		this.boldSampleId = boldSampleId;
	}


	/**
	 * @param boldTranslationMatrix the boldTranslationMatrix to set
	 */
	public void setBoldTranslationMatrix(String boldTranslationMatrix) 
	{
		this.boldTranslationMatrix = boldTranslationMatrix;
	}

	/**
	 * @param geneSequence the geneSequence to set
	 */
	public void setGeneSequence(String geneSequence) 
	{
		this.geneSequence = geneSequence;
	}

	
	/**
	 * @param remarks the remarks to set
	 */
	public void setRemarks(String remarks) 
	{
		this.remarks = remarks;
	}


	/**
	 * @param text1 the text1 to set
	 */
	public void setText1(String text1) 
	{
		this.text1 = text1;
	}


	/**
	 * @param text2 the text2 to set
	 */
	public void setText2(String text2) 
	{
		this.text2 = text2;
	}


	/**
	 * @param text3 the text3 to set
	 */
	public void setText3(String text3) 
	{
		this.text3 = text3;
	}


	/**
	 * @param yesNo1 the yesNo1 to set
	 */
	public void setYesNo1(Boolean yesNo1) 
	{
		this.yesNo1 = yesNo1;
	}


	/**
	 * @param yesNo2 the yesNo2 to set
	 */
	public void setYesNo2(Boolean yesNo2) 
	{
		this.yesNo2 = yesNo2;
	}


	/**
	 * @param yesNo3 the yesNo3 to set
	 */
	public void setYesNo3(Boolean yesNo3) 
	{
		this.yesNo3 = yesNo3;
	}


	/**
	 * @param totalResidues the totalResidues to set
	 */
	public void setTotalResidues(Integer totalResidues) 
	{
		this.totalResidues = totalResidues;
	}


	/**
	 * @param compA the compA to set
	 */
	public void setCompA(Integer compA) 
	{
		this.compA = compA;
	}


	/**
	 * @param compG the compG to set
	 */
	public void setCompG(Integer compG) 
	{
		this.compG = compG;
	}


	/**
	 * @param compC the compC to set
	 */
	public void setCompC(Integer compC) 
	{
		this.compC = compC;
	}


	/**
	 * @param compT the compT to set
	 */
	public void setCompT(Integer compT) 
	{
		this.compT = compT;
	}


	/**
	 * @param ambiguousResidues the ambiguousResidues to set
	 */
	public void setAmbiguousResidues(Integer ambiguousResidues) 
	{
		this.ambiguousResidues = ambiguousResidues;
	}


	/**
	 * @param number1 the number1 to set
	 */
	public void setNumber1(BigDecimal number1) 
	{
		this.number1 = number1;
	}


	/**
	 * @param number2 the number2 to set
	 */
	public void setNumber2(BigDecimal number2) 
	{
		this.number2 = number2;
	}


	/**
	 * @param number3 the number3 to set
	 */
	public void setNumber3(BigDecimal number3) 
	{
		this.number3 = number3;
	}


	/**
	 * @param boldLastUpdateDate the boldLastUpdateDate to set
	 */
	public void setBoldLastUpdateDate(Calendar boldLastUpdateDate) 
	{
		this.boldLastUpdateDate = boldLastUpdateDate;
	}


	/**
     * @param moleculeType the moleculeType to set
     */
    public void setMoleculeType(String moleculeType)
    {
    	this.moleculeType = moleculeType;
    }
        
    /**
     * @param dnaSequenceId the dnaSequenceId to set
     */
    public void setDnaSequenceId(Integer dnaSequenceId)
    {
        this.dnaSequenceId = dnaSequenceId;
    }


    /**
     * @param attachments the attachments to set
     */
    public void setAttachments(Set<DNASequenceAttachment> attachments)
    {
        this.attachments = attachments;
    }

    /**
     * @param collectionObject the collectionObject to set
     */
    public void setCollectionObject(CollectionObject collectionObject)
    {
        this.collectionObject = collectionObject;
    }

    /**
     * @param materialSample
     */
    public void setMaterialSample(MaterialSample materialSample)
    {
        this.materialSample = materialSample;
    }

    /**
     * @return the dnaSequenceId
     */
    @Id
    @GeneratedValue
    @Column(name = "DnaSequenceID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getDnaSequenceId()
    {
        return dnaSequenceId;
    }

	@Temporal(TemporalType.DATE)
	@Column(name = "SequenceDate", unique = false, nullable = true, insertable = true, updatable = true)
	public Calendar getSequenceDate() {
		return sequenceDate;
	}

	public void setSequenceDate(Calendar sequenceDate) {
		this.sequenceDate = sequenceDate;
	}

	@Column(name = "SequenceDatePrecision", unique = false, nullable = true, insertable = true, updatable = true)
	public Byte getSequenceDatePrecision() {
		return sequenceDatePrecision;
	}

	public void setSequenceDatePrecision(Byte sequenceDatePrecision) {
		this.sequenceDatePrecision = sequenceDatePrecision;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "ExtractionDate", unique = false, nullable = true, insertable = true, updatable = true)
	public Calendar getExtractionDate() {
		return extractionDate;
	}

	public void setExtractionDate(Calendar extractionDate) {
		this.extractionDate = extractionDate;
	}

	@Column(name = "ExtractionDatePrecision", unique = false, nullable = true, insertable = true, updatable = true)
	public Byte getExtractionDatePrecision() {
		return extractionDatePrecision;
	}

	public void setExtractionDatePrecision(Byte extractionDatePrecision) {
		this.extractionDatePrecision = extractionDatePrecision;
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
	 * @return the sequencer
	 */
	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "AgentID", unique = false, nullable = true, insertable = true, updatable = true)
	public Agent getSequencer()
	{
		return sequencer;
	}

	/**
	 * @param sequencer the sequencer to set
	 */
	public void setSequencer(Agent sequencer)
	{
		this.sequencer = sequencer;
	}


	@OneToMany(mappedBy = "dnaSequence")
	@Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
	@OrderBy("orderNumber ASC")
	public Set<Extractor> getExtractors() {
		return extractors;
	}

	public void setExtractors(Set<Extractor> extractors) {
		this.extractors = extractors;
	}

	@OneToMany(mappedBy = "dnaSequence")
	@Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
	@OrderBy("orderNumber ASC")
	public Set<PcrPerson> getPcrPersons() {
		return pcrPersons;
	}

	public void setPcrPersons(Set<PcrPerson> pcrPersons) {
		this.pcrPersons = pcrPersons;
	}

    /**
     * @return the collectionObject
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionObjectID", unique = false, nullable = true, insertable = true, updatable = true)
    public CollectionObject getCollectionObject()
    {
        return collectionObject;
    }

    /**
     * @return the materialSample
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "MaterialSampleID", unique = false, nullable = true, insertable = true, updatable = true)
    public MaterialSample getMaterialSample()
    {
        return materialSample;
    }

    /**
	 * @return the dnaSequencingRuns
	 */
    @OneToMany(mappedBy = "dnaSequence")
    @Cascade( {CascadeType.ALL} )
    @OrderBy("ordinal ASC")
	public Set<DNASequencingRun> getDnaSequencingRuns()
	{
		return dnaSequencingRuns;
	}


	/**
	 * @param dnaSequencingRuns the dnaSequencingRuns to set
	 */
	public void setDnaSequencingRuns(Set<DNASequencingRun> dnaSequencingRuns)
	{
		this.dnaSequencingRuns = dnaSequencingRuns;
	}
    
	
    @OneToMany(mappedBy = "dnaSequence")
    @Cascade( {CascadeType.ALL} )
    @OrderBy("ordinal ASC")
    public Set<DNASequenceAttachment> getAttachments()
    {
        return attachments;
    }

    //---------------------------------------------------------------
    //AttachmentOwnerIFace implementation
    //---------------------------------------------------------------
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
    @Transient
    public Set<DNASequenceAttachment> getAttachmentReferences()
    {
        return attachments;
    }

    //---------------------------------------------------------------------------
    // Overrides DataModelObjBase
    //---------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        //The CO/Prep parent reporting method used here and in getParentId() should be safe as long as this
		//methods continues to be used only by the logging module
    	return materialSample != null ? MaterialSample.getClassTableId() : CollectionObject.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return materialSample != null ? materialSample.getId() :
				(collectionObject != null ? collectionObject.getId() : null);
    }
    
	/* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return dnaSequenceId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return DNASequence.class;
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
        return 121;
    }

}
