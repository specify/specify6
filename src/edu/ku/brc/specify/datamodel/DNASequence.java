/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
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
    {   @Index (name="DNASeqDateIDX", columnNames={"SeqDate"})
    })
public class DNASequence extends CollectionMember
{
    protected Integer  dnaSequenceId;
    protected String   geneName;
    protected String   geneSequence;
    protected String   pcrPrimerFwd;
    protected String   pcrPrimerRev;
    protected String   redirection;
    protected String   processIdentifier;
    protected Calendar seqDate;
    protected Calendar submissionDate;
    
    protected Agent    sequencer;
    protected String   ncbiNumber;
    
    protected Set<CollectionObject> collectionObjects;
    protected Set<DNASequenceAttachment> attachments;
    
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
        dnaSequenceId     = null;
        geneName          = null;
        geneSequence      = null;
        pcrPrimerFwd      = null;
        pcrPrimerRev      = null;
        redirection       = null;
        processIdentifier = null;
        seqDate           = null;
        submissionDate    = null;
        sequencer         = null;
        ncbiNumber        = null;
        collectionObjects = new HashSet<CollectionObject>();
        attachments       = new TreeSet<DNASequenceAttachment>();
    }

    /**
     * @param dnaSequenceId the dnaSequenceId to set
     */
    public void setDnaSequenceId(Integer dnaSequenceId)
    {
        this.dnaSequenceId = dnaSequenceId;
    }


    /**
     * @param geneName the geneName to set
     */
    public void setGeneName(String geneName)
    {
        this.geneName = geneName;
    }


    /**
     * @param geneSequence the geneSequence to set
     */
    public void setGeneSequence(String geneSequence)
    {
        this.geneSequence = geneSequence;
    }


    /**
     * @param pcrPrimerFwd the pcrPrimerFwd to set
     */
    public void setPcrPrimerFwd(String pcrPrimerFwd)
    {
        this.pcrPrimerFwd = pcrPrimerFwd;
    }


    /**
     * @param pcrPrimerRev the pcrPrimerRev to set
     */
    public void setPcrPrimerRev(String pcrPrimerRev)
    {
        this.pcrPrimerRev = pcrPrimerRev;
    }


    /**
     * @param redirection the redirection to set
     */
    public void setRedirection(String redirection)
    {
        this.redirection = redirection;
    }


    /**
     * @param processIdentifier the processIdentifier to set
     */
    public void setProcessIdentifier(String processIdentifier)
    {
        this.processIdentifier = processIdentifier;
    }


    /**
     * @param seqDate the seqDate to set
     */
    public void setSeqDate(Calendar seqDate)
    {
        this.seqDate = seqDate;
    }


    /**
     * @param submissionDate the submissionDate to set
     */
    public void setSubmissionDate(Calendar submissionDate)
    {
        this.submissionDate = submissionDate;
    }


    /**
     * @param sequencer the sequencer to set
     */
    public void setSequencer(Agent sequencer)
    {
        this.sequencer = sequencer;
    }


    /**
     * @param ncbiNumber the ncbiNumber to set
     */
    public void setNcbiNumber(String ncbiNumber)
    {
        this.ncbiNumber = ncbiNumber;
    }


    /**
     * @param collectionObjects the collectionObjects to set
     */
    public void setCollectionObjects(Set<CollectionObject> collectionObjects)
    {
        this.collectionObjects = collectionObjects;
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


    /**
     * @return the geneName
     */
    @Column(name = "GeneName", unique = false, nullable = false, insertable = true, updatable = true, length = 32)
    public String getGeneName()
    {
        return geneName;
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
     * @return the pcrPrimerFwd
     */
    @Column(name = "PcrPrimerFwd", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getPcrPrimerFwd()
    {
        return pcrPrimerFwd;
    }


    /**
     * @return the pcrPrimerRev
     */
    @Column(name = "PcrPrimerRev", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getPcrPrimerRev()
    {
        return pcrPrimerRev;
    }


    /**
     * @return the redirection
     */
    @Column(name = "Redirection", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getRedirection()
    {
        return redirection;
    }


    /**
     * @return the processIdentifier
     */
    @Column(name = "ProcessIdentifier", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getProcessIdentifier()
    {
        return processIdentifier;
    }


    /**
     * @return the seqDate
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "SeqDate", unique = false, nullable = false, insertable = true, updatable = true)
    public Calendar getSeqDate()
    {
        return seqDate;
    }


    /**
     * @return the subMissionDate
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "SubmissionDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getSubmissionDate()
    {
        return submissionDate;
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
     * @return the ncbiNumber
     */
    @Column(name = "NcbiNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getNcbiNumber()
    {
        return ncbiNumber;
    }


    /**
     * @return the collectionObjects
     */
    @OneToMany(mappedBy = "dnaSequence")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Set<CollectionObject> getCollectionObjects()
    {
        return collectionObjects;
    }
    
    @OneToMany(mappedBy = "dnaSequence")
    @Cascade( {CascadeType.ALL} )
    public Set<DNASequenceAttachment> getAttachments()
    {
        return attachments;
    }


    public void setAttachments(Set<DNASequenceAttachment> attachments)
    {
        this.attachments = attachments;
    }

    //---------------------------------------------------------------------------
    // Overrides DataModelObjBase
    //---------------------------------------------------------------------------

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
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 121;
    }

}