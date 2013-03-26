/* Copyright (C) 2009, University of Kansas Center for Research
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.thumbnails.Thumbnailer;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "attachment")
@org.hibernate.annotations.Table(appliesTo="attachment", indexes =
    {   @Index (name="TitleIDX", columnNames={"Title"}),
        @Index (name="DateImagedIDX", columnNames={"DateImaged"}),
        @Index (name="AttchScopeIDIDX", columnNames={"ScopeID"}),
        @Index (name="AttchScopeTypeIDX", columnNames={"ScopeType"}),
        @Index (name="AttchmentGuidIDX", columnNames={"GUID"}),
    })
public class Attachment extends DataModelObjBase implements Serializable
{
    private static final HashMap<Integer, Byte> tblIdToScopeType = createTblScoprMapping();
    
    public static final byte COLLECTION_SCOPE = 0;
    public static final byte DISCIPLINE_SCOPE = 1;
    public static final byte DIVISION_SCOPE   = 2;
    public static final byte GLOBAL_SCOPE     = 3;
    
    protected Integer                 attachmentId;
    protected String                  mimeType;
    protected String                  origFilename;
    protected String                  title;
    protected String                  license;
    protected String                  copyrightHolder;
    protected String                  copyrightDate;
    protected String                  credit;
    protected String                  dateImaged;
        
    protected Calendar                fileCreatedDate;
    protected String                  remarks;
    protected String                  attachmentLocation;
    protected String                  guid;
    protected Byte                    tableID;
    protected Byte                    visibility;
    protected SpecifyUser             visibilitySetBy;
    protected Set<AttachmentMetadata> metadata;
    protected Set<AttachmentTag>      tags;
    protected AttachmentImageAttribute   attachmentImageAttribute;
    
    protected Integer scopeID;
    protected Byte    scopeType;
    
    // transient field
    protected boolean                 storeFile;
    
    // data model classes that can have Attachments
    protected Set<AccessionAttachment>               accessionAttachments;
    protected Set<AgentAttachment>                   agentAttachments;
    protected Set<BorrowAttachment>                  borrowAttachments;
    protected Set<CollectingEventAttachment>         collectingEventAttachments;
    protected Set<CollectionObjectAttachment>        collectionObjectAttachments;
    protected Set<ConservDescriptionAttachment>      conservDescriptionAttachments;
    protected Set<ConservEventAttachment>            conservEventAttachments;
    protected Set<DNASequenceAttachment>             dnaSequenceAttachments;
    protected Set<DNASequencingRunAttachment>        dnaSequencingRunAttachments;
    protected Set<FieldNotebookAttachment>           fieldNotebookAttachments;
    protected Set<FieldNotebookPageAttachment>       fieldNotebookPageAttachments;
    protected Set<FieldNotebookPageSetAttachment>    fieldNotebookPageSetAttachments;
    protected Set<GiftAttachment>                    giftAttachments;
    protected Set<LoanAttachment>                    loanAttachments;
    protected Set<LocalityAttachment>                localityAttachments;
    protected Set<PermitAttachment>                  permitAttachments;
    protected Set<PreparationAttachment>             preparationAttachments;
    protected Set<ReferenceWorkAttachment>           referenceWorkAttachments;
    protected Set<RepositoryAgreementAttachment>     repositoryAgreementAttachments;
    protected Set<TaxonAttachment>                   taxonAttachments;
    
    /** default constructor */
    public Attachment()
    {
        // do nothing
    }

    /** constructor with id */
    public Attachment(Integer attachmentId)
    {
        this.attachmentId = attachmentId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        attachmentId       = null;
        mimeType           = null;
        origFilename       = null;
        fileCreatedDate    = null;
        remarks            = null;
        attachmentLocation = null;
        title              = null;
        license            = null;
        copyrightHolder    = null;
        credit             = null;
        copyrightDate      = null;
        dateImaged         = null;
        metadata           = new HashSet<AttachmentMetadata>();
        tags               = new HashSet<AttachmentTag>();
        
        scopeID            = null;
        scopeType          = null;
        
        storeFile          = false;
        
        guid               = null;
        
        accessionAttachments           = new HashSet<AccessionAttachment>();
        agentAttachments               = new HashSet<AgentAttachment>();
        borrowAttachments              = new HashSet<BorrowAttachment>();
        collectionObjectAttachments    = new HashSet<CollectionObjectAttachment>();
        collectingEventAttachments     = new HashSet<CollectingEventAttachment>();
        conservDescriptionAttachments  = new HashSet<ConservDescriptionAttachment>();
        conservEventAttachments        = new HashSet<ConservEventAttachment>();
        dnaSequenceAttachments         = new HashSet<DNASequenceAttachment>();
        dnaSequencingRunAttachments    = new HashSet<DNASequencingRunAttachment>();
        giftAttachments                = new HashSet<GiftAttachment>();
        loanAttachments                = new HashSet<LoanAttachment>();
        localityAttachments            = new HashSet<LocalityAttachment>();
        permitAttachments              = new HashSet<PermitAttachment>();
        preparationAttachments         = new HashSet<PreparationAttachment>();
        repositoryAgreementAttachments = new HashSet<RepositoryAgreementAttachment>();
        referenceWorkAttachments       = new HashSet<ReferenceWorkAttachment>();
        taxonAttachments               = new HashSet<TaxonAttachment>();
        
        hasGUIDField = true;
        setGUID();
    }

    @Id
    @GeneratedValue
    @Column(name = "AttachmentID")
    public Integer getAttachmentId()
    {
        return this.attachmentId;
    }

    public void setAttachmentId(Integer attachmentId)
    {
        this.attachmentId = attachmentId;
    }
    
    @Transient
    @Override
    public Integer getId()
    {
        return attachmentId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Attachment.class;
    }

    @Column(name = "MimeType", length = 64)
    public String getMimeType()
    {
        return this.mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    @Column(name = "OrigFilename", nullable = false, length = 128)
    public String getOrigFilename()
    {
        return this.origFilename;
    }

    public void setOrigFilename(String origFilename)
    {
//        if ((origFilename != null && origFilename.equals(this.origFilename)) || (origFilename == null && this.origFilename == null))
//        {
//            // nothing is being changed
//            return;
//        }
        
        this.origFilename = origFilename;

//        // if there isn't a set storage yet, set one
//        if (origFilename != null && this.attachmentLocation == null)
//        {
//            // set the attachmentLocation field
//            AttachmentUtils.getAttachmentManager().setStorageLocationIntoAttachment(this);
//        }
//        
        // if a MIME type isn't already set, try to determine it
        if (this.mimeType == null && this.origFilename != null)
        {
            this.mimeType = AttachmentUtils.getMimeType(origFilename);
        }
    }

    @Column(name = "Title", length = 64)
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @Column(name = "License", length = 64)
    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    @Column(name = "CopyrightDate", length = 64)
    public String getCopyrightDate()
    {
        return copyrightDate;
    }

    public void setCopyrightDate(String copyrightDate)
    {
        this.copyrightDate = copyrightDate;
    }

    @Column(name = "CopyrightHolder", length = 64)
    public String getCopyrightHolder()
    {
        return copyrightHolder;
    }

    public void setCopyrightHolder(String copyrightHolder)
    {
        this.copyrightHolder = copyrightHolder;
    }

    @Column(name = "Credit", length = 64)
    public String getCredit()
    {
        return credit;
    }

    public void setCredit(String credit)
    {
        this.credit = credit;
    }

    @Column(name = "DateImaged", length = 64)
    public String getDateImaged()
    {
        return dateImaged;
    }

    public void setDateImaged(String dateImaged)
    {
        this.dateImaged = dateImaged;
    }

	@Temporal(TemporalType.DATE)
    @Column(name = "FileCreatedDate")
    public Calendar getFileCreatedDate()
    {
        return this.fileCreatedDate;
    }

    public void setFileCreatedDate(Calendar fileCreatedDate)
    {
        this.fileCreatedDate = fileCreatedDate;
    }

    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks()
    {
        return this.remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    @Column(name = "AttachmentLocation", length = 128)
    public String getAttachmentLocation()
    {
        return this.attachmentLocation;
    }

    public void setAttachmentLocation(String attachmentLocation)
    {
        this.attachmentLocation = attachmentLocation;
    }

    @Column(name = "Visibility")
    public Byte getVisibility() 
    {
        return this.visibility;
    }
    
    public void setVisibility(Byte visibility) 
    {
        this.visibility = visibility;
    }   

    /**
     * @return the tableID
     */
    @Column(name = "TableID", unique = false, nullable = false, insertable = true, updatable = true)
    public Byte getTableID()
    {
        return tableID;
    }

    /**
     * @param tableID the tableID to set
     */
    public void setTableID(Byte tableID)
    {
        this.tableID = tableID;
    }

    /**
     * @param tableID the tableID to set
     */
    public void setTableId(int tableIDArg)
    {
        this.tableID = (byte)tableIDArg;
        
        scopeType = tblIdToScopeType.get(tableIDArg);
        if (scopeType == null)
        {
            UIRegistry.showError(String.format("Attachment TableID was set to %d, an unknown attachment Owner!", tableIDArg));
            scopeType = GLOBAL_SCOPE;
        } else
        {
            AppContextMgr acm = AppContextMgr.getInstance();
            switch (scopeType)
            {
                case COLLECTION_SCOPE:
                    scopeID = acm.getClassObject(Collection.class).getId();
                    break;
                    
                case DISCIPLINE_SCOPE:
                    scopeID = acm.getClassObject(Discipline.class).getId();
                    break;
                    
                case DIVISION_SCOPE:
                    scopeID = acm.getClassObject(Division.class).getId();
                    break;
                    
                default:
                    scopeID = null;
                    break;
            }
        }
    }
    
    /**
     * @return
     */
    private static HashMap<Integer, Byte>  createTblScoprMapping()
    {
        int[] ids = {
            Accession.getClassTableId(), Attachment.GLOBAL_SCOPE,
            Agent.getClassTableId(), Attachment.DIVISION_SCOPE,
            Borrow.getClassTableId(),  Attachment.COLLECTION_SCOPE,
            CollectionObject.getClassTableId(),  Attachment.COLLECTION_SCOPE,
            CollectingEvent.getClassTableId(),  Attachment.DISCIPLINE_SCOPE,
            ConservDescription.getClassTableId(), Attachment.DIVISION_SCOPE,
            ConservEvent.getClassTableId(), Attachment.COLLECTION_SCOPE,
            DNASequence.getClassTableId(), Attachment.COLLECTION_SCOPE,
            DNASequencingRun.getClassTableId(), Attachment.COLLECTION_SCOPE,
            FieldNotebook.getClassTableId(),  Attachment.DISCIPLINE_SCOPE,
            FieldNotebookPage.getClassTableId(), Attachment.DISCIPLINE_SCOPE,
            FieldNotebookPageSet.getClassTableId(),  Attachment.DISCIPLINE_SCOPE,
            Gift.getClassTableId(), Attachment.DIVISION_SCOPE,
            Loan.getClassTableId(), Attachment.DIVISION_SCOPE,
            Locality.getClassTableId(),  Attachment.DISCIPLINE_SCOPE,
            Permit.getClassTableId(), Attachment.GLOBAL_SCOPE,
            Preparation.getClassTableId(), Attachment.COLLECTION_SCOPE,
            ReferenceWork.getClassTableId(), Attachment.GLOBAL_SCOPE,
            RepositoryAgreement.getClassTableId(), Attachment.DIVISION_SCOPE,
            Taxon.getClassTableId(), Attachment.DISCIPLINE_SCOPE,
        };
        HashMap<Integer, Byte> map = new HashMap<Integer, Byte>();
        for (int i=0;i<ids.length;i++)
        {
            map.put(ids[i], (byte)ids[i+1]);
            i++;
        }
        return map;
    }

    /**
     *
     */
    @Column(name = "GUID", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
    
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "VisibilitySetByID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpecifyUser getVisibilitySetBy() {
        return this.visibilitySetBy;
    }
    
    public void setVisibilitySetBy(SpecifyUser visibilitySetBy) {
        this.visibilitySetBy = visibilitySetBy;
    }

    @OneToMany(fetch=FetchType.EAGER, mappedBy = "attachment")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<AttachmentMetadata> getMetadata()
    {
        return this.metadata;
    }

    public void setMetadata(Set<AttachmentMetadata> metadata)
    {
        this.metadata = metadata;
    }
    
    @OneToMany(fetch=FetchType.EAGER, mappedBy = "attachment")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<AttachmentTag> getTags()
    {
        return tags;
    }

    public void setTags(Set<AttachmentTag> tags)
    {
        this.tags = tags;
    }


	@OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<AccessionAttachment> getAccessionAttachments()
    {
        return accessionAttachments;
    }

    public void setAccessionAttachments(Set<AccessionAttachment> accessionAttachments)
    {
        this.accessionAttachments = accessionAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<AgentAttachment> getAgentAttachments()
    {
        return agentAttachments;
    }

    public void setAgentAttachments(Set<AgentAttachment> agentAttachments)
    {
        this.agentAttachments = agentAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<CollectingEventAttachment> getCollectingEventAttachments()
    {
        return collectingEventAttachments;
    }

    public void setCollectingEventAttachments(Set<CollectingEventAttachment> collectingEventAttachments)
    {
        this.collectingEventAttachments = collectingEventAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<CollectionObjectAttachment> getCollectionObjectAttachments()
    {
        return collectionObjectAttachments;
    }

    public void setCollectionObjectAttachments(Set<CollectionObjectAttachment> collectionObjectAttachments)
    {
        this.collectionObjectAttachments = collectionObjectAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<ConservDescriptionAttachment> getConservDescriptionAttachments()
    {
        return conservDescriptionAttachments;
    }

    public void setConservDescriptionAttachments(Set<ConservDescriptionAttachment> conservDescriptionAttachments)
    {
        this.conservDescriptionAttachments = conservDescriptionAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<ConservEventAttachment> getConservEventAttachments()
    {
        return conservEventAttachments;
    }

    public void setConservEventAttachments(Set<ConservEventAttachment> conservEventAttachments)
    {
        this.conservEventAttachments = conservEventAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<DNASequenceAttachment> getDnaSequenceAttachments()
    {
        return dnaSequenceAttachments;
    }

    public void setDnaSequenceAttachments(Set<DNASequenceAttachment> dnaSequenceAttachments)
    {
        this.dnaSequenceAttachments = dnaSequenceAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<DNASequencingRunAttachment> getDnaSequencingRunAttachments()
    {
        return dnaSequencingRunAttachments;
    }

    public void setDnaSequencingRunAttachments(Set<DNASequencingRunAttachment> dnaSequencingRunAttachments)
    {
        this.dnaSequencingRunAttachments = dnaSequencingRunAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<FieldNotebookAttachment> getFieldNotebookAttachments()
    {
        return fieldNotebookAttachments;
    }

    public void setFieldNotebookAttachments(Set<FieldNotebookAttachment> fieldNotebookAttachments)
    {
        this.fieldNotebookAttachments = fieldNotebookAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<FieldNotebookPageAttachment> getFieldNotebookPageAttachments()
    {
        return fieldNotebookPageAttachments;
    }

    public void setFieldNotebookPageAttachments(Set<FieldNotebookPageAttachment> fieldNotebookPageAttachments)
    {
        this.fieldNotebookPageAttachments = fieldNotebookPageAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<FieldNotebookPageSetAttachment> getFieldNotebookPageSetAttachments()
    {
        return fieldNotebookPageSetAttachments;
    }

    public void setFieldNotebookPageSetAttachments(Set<FieldNotebookPageSetAttachment> fieldNotebookPageSetAttachments)
    {
        this.fieldNotebookPageSetAttachments = fieldNotebookPageSetAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<LoanAttachment> getLoanAttachments()
    {
        return loanAttachments;
    }

    public void setLoanAttachments(Set<LoanAttachment> loanAttachments)
    {
        this.loanAttachments = loanAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<LocalityAttachment> getLocalityAttachments()
    {
        return localityAttachments;
    }

    public void setLocalityAttachments(Set<LocalityAttachment> localityAttachments)
    {
        this.localityAttachments = localityAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<PreparationAttachment> getPreparationAttachments()
    {
        return preparationAttachments;
    }

    public void setPreparationAttachments(Set<PreparationAttachment> preparationAttachments)
    {
        this.preparationAttachments = preparationAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<PermitAttachment> getPermitAttachments()
    {
        return permitAttachments;
    }

    public void setPermitAttachments(Set<PermitAttachment> permitAttachments)
    {
        this.permitAttachments = permitAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<RepositoryAgreementAttachment> getRepositoryAgreementAttachments()
    {
        return repositoryAgreementAttachments;
    }

    public void setRepositoryAgreementAttachments(Set<RepositoryAgreementAttachment> repositoryAgreementAttachments)
    {
        this.repositoryAgreementAttachments = repositoryAgreementAttachments;
    }

    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<TaxonAttachment> getTaxonAttachments()
    {
        return taxonAttachments;
    }

    public void setTaxonAttachments(Set<TaxonAttachment> taxonAttachments)
    {
        this.taxonAttachments = taxonAttachments;
    }

    /**
     * @return the borrowAttachments
     */
    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<BorrowAttachment> getBorrowAttachments()
    {
        return borrowAttachments;
    }

    /**
     * @param borrowAttachments the borrowAttachments to set
     */
    public void setBorrowAttachments(Set<BorrowAttachment> borrowAttachments)
    {
        this.borrowAttachments = borrowAttachments;
    }

    /**
     * @return the giftAttachments
     */
    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
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

    /**
     * @return the referenceWorkAttachments
     */
    @OneToMany(mappedBy = "attachment")
    @Cascade( {CascadeType.ALL} )
    public Set<ReferenceWorkAttachment> getReferenceWorkAttachments()
    {
        return referenceWorkAttachments;
    }

    /**
     * @param referenceWorkAttachments the referenceWorkAttachments to set
     */
    public void setReferenceWorkAttachments(Set<ReferenceWorkAttachment> referenceWorkAttachments)
    {
        this.referenceWorkAttachments = referenceWorkAttachments;
    }

    /**
     * @return
     */
    @ManyToOne(cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.LAZY)
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @JoinColumn(name = "AttachmentImageAttributeID", unique = false, nullable = true, insertable = true, updatable = true)
    public AttachmentImageAttribute getAttachmentImageAttribute() 
    {
        return this.attachmentImageAttribute;
    }

    /**
     * @param attachmentImageAttribute
     */
    public void setAttachmentImageAttribute(AttachmentImageAttribute attachmentImageAttribute) 
    {
        this.attachmentImageAttribute = attachmentImageAttribute;
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
        return 41;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        String filename = getOrigFilename();
        if (filename != null)
        {
            // We have to do this in a system independent way b/c the file may have come from a Windows system but is being viewed on a Linux system (or vice versa)
            // Using File.getName() only works for files with names that look like the filenames on the running VMs platform.
            int lastWinSepIndex = filename.lastIndexOf('\\');
            int lastUnixSepIndex = filename.lastIndexOf('/');
            int lastIndex = Math.max(lastWinSepIndex, lastUnixSepIndex);
            if (lastIndex != -1)
            {
                filename = filename.substring(lastIndex+1);
            }
        }
        
        return title + ": " + filename;
    }

    @Transient
    public boolean isStoreFile()
    {
        return storeFile;
    }

    public void setStoreFile(boolean storeFile)
    {
        this.storeFile = storeFile;
    }

    /**
     * @throws IOException
     */
    public void storeFile(final boolean doDisplayErrors) throws IOException
    {
        // Copy the attachment file to the file storage system
        Thumbnailer            thumbnailGen  = AttachmentUtils.getThumbnailer();
        AttachmentManagerIface attachmentMgr = AttachmentUtils.getAttachmentManager();
        
        //String fName = StringUtils.replace(origFilename, "darwin\\", "darwin2\\");
        //File                   origFile      = new File(fName);//origFilename);
        File                   origFile      = new File(origFilename);
        File                   thumbFile     = null;
        
        try
        {
            thumbFile = File.createTempFile("sp6_thumb_", null);
            thumbFile.deleteOnExit();
            thumbnailGen.generateThumbnail(origFilename, thumbFile.getAbsolutePath(), false);
        }
        catch (IOException e)
        {
            thumbFile = null;
        }
        
        try
        {
            attachmentMgr.storeAttachmentFile(this, origFile, thumbFile);
            
        } catch (IOException ex)
        {
            if (doDisplayErrors)
            {
                AppPreferences prefs = AppPreferences.getGlobalPrefs().getBoolean("USE_GLOBAL_PREFS", false) ? 
                                            AppPreferences.getGlobalPrefs() : AppPreferences.getLocalPrefs();
                boolean useFilePath = prefs.getBoolean("attachment.use_path", true);
                String  msgKey      = "ATTCH_NOT_SAVED_REPOS" + (useFilePath ? "" : "_WEB");
                String  errMsg      = ex.getMessage();
                UIRegistry.showLocalizedError(msgKey, origFilename, StringUtils.isNotEmpty(errMsg) ? errMsg : "");
                return;
            }
            throw ex;
            
        } finally
        {
            this.storeFile = false;
        }
    }
    
    /**
     * @return the scopeID
     */
    @Column(name = "ScopeID", nullable = true)
    public Integer getScopeID()
    {
        return scopeID;
    }

    /**
     * @param scopeID the scopeID to set
     */
    public void setScopeID(Integer scopeID)
    {
        this.scopeID = scopeID;
    }

    /**
     * @return the scopeType
     */
    @Column(name = "ScopeType", nullable = true)
    public Byte getScopeType()
    {
        return scopeType;
    }

    /**
     * @param scopeType the scopeType to set
     */
    public void setScopeType(Byte scopeType)
    {
        this.scopeType = scopeType;
    }

    /**
     * @param scopeType the scopeType to set
     */
    public void setScopeType(Integer scopeType)
    {
        this.scopeType = scopeType != null ? scopeType.byteValue() : null;
    }

    /**
     * @param objAttachment
     * @return
     */
    public static String getIdentityTitle(final ObjectAttachmentIFace<?> objAttachment)
    {
         if (objAttachment != null)
         {
             Attachment attachment = objAttachment.getAttachment();
             if (attachment != null)
             {
                 String title = attachment.getTitle();
                 if (StringUtils.isNotEmpty(title))
                 {
                     return title;
                 }
                 
                 String fileName = attachment.getOrigFilename();
                 fileName = FilenameUtils.getName(fileName);
                 if (StringUtils.isNotEmpty(fileName))
                 {
                     return fileName;
                 }
             }
         }
         
         Integer id = ((FormDataObjIFace)objAttachment).getId();
         
         return id != null ? id.toString() : "N/A";
    }
}
