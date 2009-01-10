/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
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

import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.thumbnails.Thumbnailer;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "attachment")
@org.hibernate.annotations.Table(appliesTo="attachment", indexes =
    {   @Index (name="TitleIDX", columnNames={"Title"}),
        @Index (name="DateImagedIDX", columnNames={"DateImaged"})
    })
public class Attachment extends DataModelObjBase implements Serializable
{
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
    protected Integer                 visibility;
    protected String                  visibilitySetBy;
    protected Set<AttachmentMetadata> metadata;
    protected Set<AttachmentTag>      tags;
    
    // transient field
    protected boolean                 storeFile;
    
    // data model classes that can have Attachments
    protected Set<AccessionAttachment>               accessionAttachments;
    protected Set<AgentAttachment>                   agentAttachments;
    protected Set<CollectingEventAttachment>         collectingEventAttachments;
    protected Set<CollectionObjectAttachment>        collectionObjectAttachments;
    protected Set<ConservDescriptionAttachment>      conservDescriptionAttachments;
    protected Set<ConservEventAttachment>            conservEventAttachments;
    protected Set<DNASequenceAttachment>             dnaSequenceAttachments;
    protected Set<FieldNotebookAttachment>           fieldNotebookAttachments;
    protected Set<FieldNotebookPageAttachment>       fieldNotebookPageAttachments;
    protected Set<FieldNotebookPageSetAttachment>    fieldNotebookPageSetAttachments;
    protected Set<LoanAttachment>                    loanAttachments;
    protected Set<LocalityAttachment>                localityAttachments;
    protected Set<PermitAttachment>                  permitAttachments;
    protected Set<PreparationAttachment>             preparationAttachments;
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
        metadata           = new HashSet<AttachmentMetadata>();
        tags               = new HashSet<AttachmentTag>();
        
        storeFile          = false;
        
        accessionAttachments           = new HashSet<AccessionAttachment>();
        agentAttachments               = new HashSet<AgentAttachment>();
        collectionObjectAttachments    = new HashSet<CollectionObjectAttachment>();
        collectingEventAttachments     = new HashSet<CollectingEventAttachment>();
        conservDescriptionAttachments  = new HashSet<ConservDescriptionAttachment>();
        conservEventAttachments        = new HashSet<ConservEventAttachment>();
        loanAttachments                = new HashSet<LoanAttachment>();
        localityAttachments            = new HashSet<LocalityAttachment>();
        permitAttachments              = new HashSet<PermitAttachment>();
        preparationAttachments         = new HashSet<PreparationAttachment>();
        repositoryAgreementAttachments = new HashSet<RepositoryAgreementAttachment>();
        taxonAttachments               = new HashSet<TaxonAttachment>();
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
    public Integer getVisibility() 
    {
        return this.visibility;
    }
    
    public void setVisibility(Integer visibility) 
    {
        this.visibility = visibility;
    }   

    @Column(name = "VisibilitySetBy", length = 50)
    public String getVisibilitySetBy() {
        return this.visibilitySetBy;
    }
    
    public void setVisibilitySetBy(String visibilitySetBy) {
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

    public void storeFile() throws IOException
    {
        // Copy the attachment file to the file storage system
        Thumbnailer thumbnailGen = AttachmentUtils.getThumbnailer();
        AttachmentManagerIface attachmentMgr = AttachmentUtils.getAttachmentManager();
        File origFile = new File(origFilename);
        File thumbFile = null;
        
        try
        {
            thumbFile = File.createTempFile("sp6_thumb_", null);
            thumbFile.deleteOnExit();
            thumbnailGen.generateThumbnail(origFilename, thumbFile.getAbsolutePath(), false);
        }
        catch (IOException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(Attachment.class, e);
            thumbFile = null;
        }
        attachmentMgr.storeAttachmentFile(this, origFile, thumbFile);
        
        this.storeFile = false;
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
