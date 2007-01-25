
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Orderable;
import edu.ku.brc.util.thumbnails.Thumbnailer;

@Entity
@Table(name = "attachments")
public class Attachment extends DataModelObjBase implements Serializable, Orderable
{
    private Long                    attachmentID;
    private String                  mimeType;
    private String                  origFilename;
    private Calendar                fileCreatedDate;
    private Integer                 ordinal;
    private String                  remarks;
    private String                  attachmentLocation;
    protected Integer visibility;
    protected String visibilitySetBy;
    private Set<AttachmentMetadata> metadata;
    private Agent                   agent;
    private CollectionObject        collectionObject;
    private CollectingEvent         collectingEvent;
    private Loan                    loan;
    private Locality                locality;
    private Permit                  permit;
    private Preparation             preparation;
    private Taxon                   taxon;
    private Accession accession;
    private RepositoryAgreement repositoryAgreement;
    /** default constructor */
    public Attachment()
    {
        // do nothing
    }

    /** constructor with id */
    public Attachment(Long attachmentID)
    {
        this.attachmentID = attachmentID;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        attachmentID = null;
        mimeType = null;
        origFilename = null;
        fileCreatedDate = null;
        ordinal = null;
        remarks = null;
        attachmentLocation = null;
        metadata = new HashSet<AttachmentMetadata>();
        agent = null;
        collectionObject = null;
        collectingEvent = null;
        loan = null;
        locality = null;
        permit = null;
        preparation = null;
        taxon = null;
    }

    @Id
    @GeneratedValue
    @Column(name = "AttachmentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getAttachmentID()
    {
        return this.attachmentID;
    }

    public void setAttachmentID(Long attachmentID)
    {
        this.attachmentID = attachmentID;
    }
    
    @Transient
    @Override
    public Long getId()
    {
        return attachmentID;
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

    @Column(name = "MimeType", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getMimeType()
    {
        return this.mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    @Column(name = "OrigFilename", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getOrigFilename()
    {
        return this.origFilename;
    }

    public void setOrigFilename(String origFilename)
    {
        this.origFilename = origFilename;

        // for newly created attachments, setup the attachmentLocation field
        if (this.attachmentID == null && origFilename != null)
        {
            // set the attachmentLocation field
            AttachmentUtils.getAttachmentManager().setStorageLocationIntoAttachment(this);
            this.mimeType = AttachmentUtils.getMimeType(origFilename);
        }
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "FileCreatedDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getFileCreatedDate()
    {
        return this.fileCreatedDate;
    }

    public void setFileCreatedDate(Calendar fileCreatedDate)
    {
        this.fileCreatedDate = fileCreatedDate;
    }

    @Column(name = "Ordinal", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal)
    {
        this.ordinal = ordinal;
    }

    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getRemarks()
    {
        return this.remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    @Column(name = "AttachmentLocation", unique = false, nullable = true, insertable = true, updatable = true)
    public String getAttachmentLocation()
    {
        return this.attachmentLocation;
    }

    public void setAttachmentLocation(String attachmentLocation)
    {
        this.attachmentLocation = attachmentLocation;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "attachment")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AttachmentMetadata> getMetadata()
    {
        return this.metadata;
    }

    public void setMetadata(Set<AttachmentMetadata> metadata)
    {
        this.metadata = metadata;
    }
    
    public void addAttachmentMetadata(AttachmentMetadata meta)
    {
        this.metadata.add(meta);
        meta.setAttachment(this);
    }
    
    public void removeAttachmentMetadata(AttachmentMetadata meta)
    {
        this.metadata.remove(meta);
        meta.setAttachment(null);
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent()
    {
        return agent;
    }

    public void setAgent(Agent agent)
    {
        this.agent = agent;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectingEventID", unique = false, nullable = true, insertable = true, updatable = true)
    public CollectingEvent getCollectingEvent()
    {
        return collectingEvent;
    }

    public void setCollectingEvent(CollectingEvent collectingEvent)
    {
        this.collectingEvent = collectingEvent;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionObjectID", unique = false, nullable = true, insertable = true, updatable = true)
    public CollectionObject getCollectionObject()
    {
        return collectionObject;
    }

    public void setCollectionObject(CollectionObject collectionObject)
    {
        this.collectionObject = collectionObject;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanID", unique = false, nullable = true, insertable = true, updatable = true)
    public Loan getLoan()
    {
        return loan;
    }

    public void setLoan(Loan loan)
    {
        this.loan = loan;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LocalityID", unique = false, nullable = true, insertable = true, updatable = true)
    public Locality getLocality()
    {
        return locality;
    }

    public void setLocality(Locality locality)
    {
        this.locality = locality;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PermitID", unique = false, nullable = true, insertable = true, updatable = true)
    public Permit getPermit()
    {
        return permit;
    }

    public void setPermit(Permit permit)
    {
        this.permit = permit;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PreparationID", unique = false, nullable = true, insertable = true, updatable = true)
    public Preparation getPreparation()
    {
        return preparation;
    }

    public void setPreparation(Preparation preparation)
    {
        this.preparation = preparation;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "TaxonID", unique = false, nullable = true, insertable = true, updatable = true)
    public Taxon getTaxon()
    {
        return taxon;
    }

    public void setTaxon(Taxon taxon)
    {
        this.taxon = taxon;
    }
    /**
     *      * Indicates whether this record can be viewed - by owner, by instituion, or by all
     */
    @Column(name = "Visibility", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
    public Integer getVisibility() 
    {
        return this.visibility;
    }
    
    public void setVisibility(Integer visibility) 
    {
        this.visibility = visibility;
    }   
    
    /**
     * 
     */
    @Column(name = "VisibilitySetBy", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getVisibilitySetBy() 
    {
        return this.visibilitySetBy;
    }
    
    public void setVisibilitySetBy(String visibilitySetBy) 
    {
        this.visibilitySetBy = visibilitySetBy;
    }   
    @Override
    public void addReference(FormDataObjIFace ref, String refType)
    {
        if (ref instanceof AttachmentMetadata)
        {
            addAttachmentMetadata((AttachmentMetadata)ref);
            return;
        }
        super.addReference(ref, refType);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.OrderableFormDataObj#getOrderIndex()
     */
    @Transient
    public int getOrderIndex()
    {
        if (ordinal != null) { return this.ordinal; }
        return 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.OrderableFormDataObj#setOrderIndex(int)
     */
    public void setOrderIndex(int ordinal)
    {
        this.ordinal = ordinal;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
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
        if (getOrigFilename() != null)
        {
            File f = new File(getOrigFilename());
            return f.getName();
        }
        return super.getIdentityTitle();
    }

    @Override
    public void onDelete()
    {
        // TODO Delete the attachment file from the file storage system
    }

    @Override
    public void onSave()
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
            thumbnailGen.generateThumbnail(origFilename, thumbFile.getAbsolutePath());
        }
        catch (IOException e)
        {
            // unable to create thumbnail
            thumbFile = null;
        }
        
        try
        {
            attachmentMgr.storeAttachmentFile(this, origFile, thumbFile);
        }
        catch (IOException e)
        {
            // exception while saving copying attachments to storage system
            e.printStackTrace();
        }
    }

    /**
     * @return the accession
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AccessionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Accession getAccession()
    {
        return this.accession;
    }

    /**
     * @param accession the accession to set
     */
    public void setAccession(Accession accession)
    {
        this.accession = accession;
    }

    /**
     * @return the repositoryAgreement
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "RepositoryAgreementID", unique = false, nullable = true, insertable = true, updatable = true)
    public RepositoryAgreement getRepositoryAgreement()
    {
        return this.repositoryAgreement;
    }

    /**
     * @param repositoryAgreement the repositoryAgreement to set
     */
    public void setRepositoryAgreement(RepositoryAgreement repositoryAgreement)
    {
        this.repositoryAgreement = repositoryAgreement;
    }
    @Override
    public void onUpdate()
    {
        // TODO Possibly update the attachment file in the file storage system
    }
}
