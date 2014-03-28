package edu.ku.brc.specify.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.util.Orderable;

/**
 * @author timo
 *
 */
/**
 * @author timo
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "storageattachment")
@SuppressWarnings("serial")
public class StorageAttachment extends DataModelObjBase implements 
	ObjectAttachmentIFace<Storage>, 
	Orderable, 
	Serializable, 
	Comparable<StorageAttachment> {

    protected Integer    storageAttachmentId;
    protected Storage    storage;
    protected Attachment attachment;
    protected Integer    ordinal;
    protected String     remarks;

    /**
     * 
     */
    public StorageAttachment() {
    	// do nothing
    }
    
    /**
     * @param id
     */
    public StorageAttachment(Integer id) {
    	this.storageAttachmentId = id;
    }
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
	 */
	@Override
	public void initialize() {
		super.init();
		storageAttachmentId = null;
		storage = null;
		attachment = new Attachment();
		attachment.initialize();
		ordinal = null;
	}

	/**
	 * @return
	 */
	@Id
	@GeneratedValue
	@Column(name = "StorageAttachmentID")
	public Integer getStorageAttachmentId() {
		return storageAttachmentId;
	}

	/**
	 * @param storageAttachmentId
	 */
	public void setStorageAttachmentId(Integer storageAttachmentId) {
		this.storageAttachmentId = storageAttachmentId;
	}

	/**
	 * @return
	 */
	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "StorageID", nullable = false)
	public Storage getStorage() {
		return storage;
	}

	/**
	 * @param storage
	 */
	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.ku.brc.specify.datamodel.ObjectAttachmentIFace#getAttachment()
	 */
	@ManyToOne(cascade = {}, fetch = FetchType.EAGER)
	@Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
	@JoinColumn(name = "AttachmentID", nullable = false)
	@OrderBy("ordinal ASC")
	public Attachment getAttachment() {
		return attachment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.ku.brc.specify.datamodel.ObjectAttachmentIFace#setAttachment(edu.
	 * ku.brc.specify.datamodel.Attachment)
	 */
	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.ku.brc.specify.datamodel.ObjectAttachmentIFace#getOrdinal()
	 */
	@Column(name = "Ordinal", nullable = false)
	public Integer getOrdinal() {
		return ordinal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.ku.brc.specify.datamodel.ObjectAttachmentIFace#setOrdinal(java.lang
	 * .Integer)
	 */
	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.ku.brc.specify.datamodel.ObjectAttachmentIFace#getRemarks()
	 */
	@Lob
	@Column(name = "Remarks", length = 4096)
	public String getRemarks() {
		return remarks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.ku.brc.specify.datamodel.ObjectAttachmentIFace#setRemarks(java.lang
	 * .String)
	 */
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
	 */
	@Override
    @Transient
	public Integer getId() {
		return storageAttachmentId;
	}


	@Override
	@Transient
	public Class<?> getDataClass() {
		return StorageAttachment.class;
	}

	   /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Storage.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return storage != null ? storage.getId() : null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return Storage.getClassTableId();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.ObjectAttachmentIFace#getTableID()
     */
    @Override
    @Transient
    public int getTableID()
    {
        return Storage.getClassTableId();
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 119;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.ObjectAttachmentIFace#getObject()
     */
    @Transient
    public Storage getObject()
    {
        return getStorage();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.ObjectAttachmentIFace#setObject(edu.ku.brc.specify.datamodel.DataModelObjBase)
     */
    public void setObject(Storage object)
    {
        setStorage(object);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
         return Attachment.getIdentityTitle(this);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    @Override
    public String toString()
    {
        return Attachment.getIdentityTitle(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(StorageAttachment o)
    {
        return ((Integer)getOrderIndex()).compareTo(o.getOrderIndex());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.OrderableFormDataObj#getOrderIndex()
     */
    @Transient
    public int getOrderIndex()
    {
        return (this.ordinal != null) ? this.ordinal : 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.OrderableFormDataObj#setOrderIndex(int)
     */
    public void setOrderIndex(int ordinal)
    {
        this.ordinal = ordinal;
    }

}
