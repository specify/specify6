/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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

import edu.ku.brc.util.Orderable;

/**
 * @author Administrator
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "dnasequencingrun")
public class DNASequencingRun extends CollectionMember 
	implements AttachmentOwnerIFace<DNASequencingRunAttachment>, Orderable
{
	protected Integer dnaSequencingRunId;
	protected Integer ordinal;
	protected String readDirection;
	protected Calendar runDate;
	protected Boolean pcrCocktailPrimer;
	protected String pcrForwardPrimerCode;
	protected String pcrReversePrimerCode;
	protected String pcrPrimerSequence5_3;
	protected String pcrPrimerName;
	protected String sequencePrimerCode;
	protected String sequencePrimerName;
	protected String sequencePrimerSequence5_3;
	protected Boolean sequenceCocktailPrimer;
	protected String traceFileName;
	protected String scoreFileName;
	protected String remarks;
	
	protected Float number1;
	protected Float number2;
	protected Float number3;
	protected String text1;
	protected String text2;
	protected String text3;
	protected Boolean yesNo1;
	protected Boolean yesNo2;
	protected Boolean yesNo3;
	
	protected DNASequence dnaSequence;
	protected Set<DNASequencingRunAttachment> attachments;
	protected Set<DNASequencingRunCitation> citations;
		
    /**
	 * @return the dnaSequencingRunId
	 */
    @Id
    @GeneratedValue
    @Column(name = "DNASequencingRunID", unique = false, nullable = false, insertable = true, updatable = true)
	public Integer getDnaSequencingRunId()
	{
		return dnaSequencingRunId;
	}


	/**
	 * @return the ordinal
	 */
    @Column(name = "Ordinal", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getOrdinal()
	{
		return ordinal;
	}




	/**
	 * @return the readDirection
	 */
    @Column(name = "ReadDirection", unique = false,nullable = true, insertable = true, updatable = true, length = 16)
	public String getReadDirection()
	{
		return readDirection;
	}




	/**
	 * @return the runDate
	 */
    @Temporal(TemporalType.DATE)
    @Column(name = "RunDate", unique = false, nullable = true, insertable = true, updatable = true)
	public Calendar getRunDate()
	{
		return runDate;
	}




	/**
	 * @return the pcrCocktailPrimer
	 */
    @Column(name = "PCRCocktailPrimer", unique = false, nullable = true, insertable = true, updatable = true)
	public Boolean getPcrCocktailPrimer()
	{
		return pcrCocktailPrimer;
	}




	/**
	 * @return the pcrForwardPrimerCode
	 */
    @Column(name = "PCRForwardPrimerCode", unique = false,nullable = true, insertable = true, updatable = true, length = 32)
	public String getPcrForwardPrimerCode()
	{
		return pcrForwardPrimerCode;
	}




	/**
	 * @return the pcrReversePrimerCode
	 */
    @Column(name = "PCRReversePrimerCode", unique = false,nullable = true, insertable = true, updatable = true, length = 32)
	public String getPcrReversePrimerCode()
	{
		return pcrReversePrimerCode;
	}




	/**
	 * @return the pcrPrimerSequence5_3
	 */
    @Column(name = "PCRPrimerSequence5_3", unique = false,nullable = true, insertable = true, updatable = true, length = 64)
	public String getPcrPrimerSequence5_3()
	{
		return pcrPrimerSequence5_3;
	}




	/**
	 * @return the pcrPrimerName
	 */
    @Column(name = "PCRPrimerName", unique = false,nullable = true, insertable = true, updatable = true, length = 32)
	public String getPcrPrimerName()
	{
		return pcrPrimerName;
	}




	/**
	 * @return the sequencePrimerCode
	 */
    @Column(name = "SequencePrimerCode", unique = false,nullable = true, insertable = true, updatable = true, length = 32)
	public String getSequencePrimerCode()
	{
		return sequencePrimerCode;
	}




	/**
	 * @return the sequencePrimerName
	 */
    @Column(name = "SequencePrimerName", unique = false,nullable = true, insertable = true, updatable = true, length = 32)
	public String getSequencePrimerName()
	{
		return sequencePrimerName;
	}




	/**
	 * @return the sequencePrimerSequence5_3
	 */
    @Column(name = "SequencePrimerSequence5_3", unique = false,nullable = true, insertable = true, updatable = true, length = 64)
	public String getSequencePrimerSequence5_3()
	{
		return sequencePrimerSequence5_3;
	}




	/**
	 * @return the sequenceCocktailPrimer
	 */
    @Column(name = "SequenceCocktailPrimer", unique = false, nullable = true, insertable = true, updatable = true)
	public Boolean getSequenceCocktailPrimer()
	{
		return sequenceCocktailPrimer;
	}




	/**
	 * @return the traceFileName
	 */
    @Column(name = "TraceFileName", unique = false,nullable = true, insertable = true, updatable = true, length = 32)
	public String getTraceFileName()
	{
		return traceFileName;
	}




	/**
	 * @return the scoreFileName
	 */
    @Column(name = "ScoreFileName", unique = false,nullable = true, insertable = true, updatable = true, length = 32)
	public String getScoreFileName()
	{
		return scoreFileName;
	}




	/**
	 * @return the remarks
	 */
	@Lob
    @Column(name = "Remarks", unique = false,nullable = true, insertable = true, updatable = true, length = 4096)
	public String getRemarks()
	{
		return remarks;
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
	 * @return the number2
	 */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
	public Float getNumber2()
	{
		return number2;
	}




	/**
	 * @return the number3
	 */
    @Column(name = "Number3", unique = false, nullable = true, insertable = true, updatable = true)
	public Float getNumber3()
	{
		return number3;
	}




	/**
	 * @return the text1
	 */
    @Column(name = "Text1", unique = false,nullable = true, insertable = true, updatable = true, length = 32)
	public String getText1()
	{
		return text1;
	}




	/**
	 * @return the text2
	 */
    @Column(name = "Text2", unique = false,nullable = true, insertable = true, updatable = true, length = 32)
	public String getText2()
	{
		return text2;
	}




	/**
	 * @return the text3
	 */
    @Column(name = "Text3", unique = false,nullable = true, insertable = true, updatable = true, length = 64)
	public String getText3()
	{
		return text3;
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
	 * @return the yesNo2
	 */
    @Column(name = "YesNo2", unique = false, nullable = true, insertable = true, updatable = true)
	public Boolean getYesNo2()
	{
		return yesNo2;
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
	 * @return the dnaSequence
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DNASequenceID", unique = false, nullable = false, insertable = true, updatable = true)
	public DNASequence getDnaSequence()
	{
		return dnaSequence;
	}

	/**
	 * @return the citations
	 */
    @OneToMany(mappedBy = "sequencingRun")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
	public Set<DNASequencingRunCitation> getCitations()
	{
		return citations;
	}

    @OneToMany(mappedBy = "dnaSequencingRun")
    @Cascade( {CascadeType.ALL} )
    @OrderBy("ordinal ASC")
    public Set<DNASequencingRunAttachment> getAttachments()
    {
        return attachments;
    }

	/**
     * @return the Table ID for the class.
     */
	@Transient
    public static int getClassTableId()
    {
    	return 88;
    }
    
    
	/**
	 * @param dnaSequencingRunId the dnaSequencingRunId to set
	 */
	public void setDnaSequencingRunId(Integer dnaSequencingRunId)
	{
		this.dnaSequencingRunId = dnaSequencingRunId;
	}




	/**
	 * @param ordinal the ordinal to set
	 */
	public void setOrdinal(Integer ordinal)
	{
		this.ordinal = ordinal;
	}




	/**
	 * @param readDirection the readDirection to set
	 */
	public void setReadDirection(String readDirection)
	{
		this.readDirection = readDirection;
	}




	/**
	 * @param runDate the runDate to set
	 */
	public void setRunDate(Calendar runDate)
	{
		this.runDate = runDate;
	}




	/**
	 * @param pcrCocktailPrimer the pcrCocktailPrimer to set
	 */
	public void setPcrCocktailPrimer(Boolean pcrCocktailPrimer)
	{
		this.pcrCocktailPrimer = pcrCocktailPrimer;
	}




	/**
	 * @param pcrForwardPrimerCode the pcrForwardPrimerCode to set
	 */
	public void setPcrForwardPrimerCode(String pcrForwardPrimerCode)
	{
		this.pcrForwardPrimerCode = pcrForwardPrimerCode;
	}




	/**
	 * @param pcrReversePrimerCode the pcrReversePrimerCode to set
	 */
	public void setPcrReversePrimerCode(String pcrReversePrimerCode)
	{
		this.pcrReversePrimerCode = pcrReversePrimerCode;
	}




	/**
	 * @param pcrPrimerSequence5_3 the pcrPrimerSequence5_3 to set
	 */
	public void setPcrPrimerSequence5_3(String pcrPrimerSequence5_3)
	{
		this.pcrPrimerSequence5_3 = pcrPrimerSequence5_3;
	}




	/**
	 * @param pcrPrimerName the pcrPrimerName to set
	 */
	public void setPcrPrimerName(String pcrPrimerName)
	{
		this.pcrPrimerName = pcrPrimerName;
	}




	/**
	 * @param sequencePrimerCode the sequencePrimerCode to set
	 */
	public void setSequencePrimerCode(String sequencePrimerCode)
	{
		this.sequencePrimerCode = sequencePrimerCode;
	}




	/**
	 * @param sequencePrimerName the sequencePrimerName to set
	 */
	public void setSequencePrimerName(String sequencePrimerName)
	{
		this.sequencePrimerName = sequencePrimerName;
	}




	/**
	 * @param sequencePrimerSequence5_3 the sequencePrimerSequence5_3 to set
	 */
	public void setSequencePrimerSequence5_3(String sequencePrimerSequence5_3)
	{
		this.sequencePrimerSequence5_3 = sequencePrimerSequence5_3;
	}




	/**
	 * @param sequenceCocktailPrimer the sequenceCocktailPrimer to set
	 */
	public void setSequenceCocktailPrimer(Boolean sequenceCocktailPrimer)
	{
		this.sequenceCocktailPrimer = sequenceCocktailPrimer;
	}




	/**
	 * @param traceFileName the traceFileName to set
	 */
	public void setTraceFileName(String traceFileName)
	{
		this.traceFileName = traceFileName;
	}




	/**
	 * @param scoreFileName the scoreFileName to set
	 */
	public void setScoreFileName(String scoreFileName)
	{
		this.scoreFileName = scoreFileName;
	}




	/**
	 * @param remarks the remarks to set
	 */
	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}




	/**
	 * @param number1 the number1 to set
	 */
	public void setNumber1(Float number1)
	{
		this.number1 = number1;
	}




	/**
	 * @param number2 the number2 to set
	 */
	public void setNumber2(Float number2)
	{
		this.number2 = number2;
	}




	/**
	 * @param number3 the number3 to set
	 */
	public void setNumber3(Float number3)
	{
		this.number3 = number3;
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
	 * @param dnaSequence the dnaSequence to set
	 */
	public void setDnaSequence(DNASequence dnaSequence)
	{
		this.dnaSequence = dnaSequence;
	}




	/**
	 * @param citations the citations to set
	 */
	public void setCitations(Set<DNASequencingRunCitation> citations)
	{
		this.citations = citations;
	}




	/**
     * @param attachments the attachments to set
     */
    public void setAttachments(Set<DNASequencingRunAttachment> attachments)
    {
        this.attachments = attachments;
    }

	//---------------------------------------------------------------
	//Orderable implementation
	//---------------------------------------------------------------

    
    /* (non-Javadoc)
	 * @see edu.ku.brc.util.Orderable#getOrderIndex()
	 */
	@Override
	@Transient
	public int getOrderIndex()
	{
		return ordinal;
	}


	/* (non-Javadoc)
	 * @see edu.ku.brc.util.Orderable#setOrderIndex(int)
	 */
	@Override
	public void setOrderIndex(int order)
	{
		ordinal = order;
	}
    
	//---------------------------------------------------------------
	//AttachmentOwnerIFace implementation
	//---------------------------------------------------------------
	

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
	 */
	@Override
	@Transient
	public Set<DNASequencingRunAttachment> getAttachmentReferences()
	{
		return attachments;
	}

	//---------------------------------------------------------------
	//DataModelObjBase override
	//---------------------------------------------------------------
	
	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
	 */
	@Override
	@Transient
	public Class<?> getDataClass()
	{
		return DNASequencingRun.class;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
	 */
	@Override
	@Transient
	public Integer getId()
	{
		return dnaSequencingRunId;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
	 */
	@Override
	@Transient
	public int getTableId()
	{
		return getClassTableId();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
	 */
	@Override
	public void initialize()
	{
		dnaSequencingRunId = null;
		ordinal = null;
		readDirection = null;
		runDate = null;
		pcrCocktailPrimer = null;
		pcrForwardPrimerCode = null;
		pcrReversePrimerCode = null;
		pcrPrimerSequence5_3 = null;
		pcrPrimerName = null;
		sequencePrimerCode = null;
		sequencePrimerName = null;
		sequencePrimerSequence5_3 = null;
		sequenceCocktailPrimer = null;
		traceFileName = null;
		scoreFileName = null;
		remarks = null;

		number1 = null;
		number2 = null;
		number3 = null;
		text1 = null;
		text2 = null;
		text3 = null;
		yesNo1 = null;
		yesNo2 = null;
		yesNo3 = null;
		
		dnaSequence = null;
		attachments = new TreeSet<DNASequencingRunAttachment>();
		citations = new HashSet<DNASequencingRunCitation>();
	}
	
}
