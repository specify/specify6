/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author timo
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "attachmentimageattribute")
public class AttachmentImageAttribute extends DataModelObjBase 
{
	protected Integer                 attachmentImageAttributeId;
    protected Integer				  height;
    protected Integer                 width;
    protected Double                  resolution;
    protected Double                  magnification;
    protected String                  creativeCommons;
    protected MorphBankView           morphBankView;
    protected Set<Attachment>		  attachments;
	
    protected Timestamp timestampLastSend; //time last send to morphbank was completed 
	protected Timestamp timestampLastUpdateCheck; //time mb was last checked for updates
	//morph bank's ids . (Integer is problably ok?) . (probably only really need the mbImageId?)
	protected Integer mbImageId; 
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
	 */
	@Override
	@Transient
	public Class<?> getDataClass() 
	{
        return AttachmentImageAttribute.class;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
	 */
	@Override
	@Transient
	public Integer getId() 
	{
		return attachmentImageAttributeId;
	}

    @Id
    @GeneratedValue
    @Column(name = "AttachmentImageAttributeID")
    public Integer getAttachmentImageAttributeId()
    {
        return this.attachmentImageAttributeId;
    }

    public void setAttachmentImageAttributeId(Integer attachmentImageAttributeId)
    {
        this.attachmentImageAttributeId = attachmentImageAttributeId;
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
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 139;
    }
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
	 */
	@Override
	public void initialize() 
	{
        super.init();
        attachmentImageAttributeId = null;
        height             = null;
        width              = null;
        resolution         = null;
        magnification      = null;
        creativeCommons    = null;
        morphBankView      = null;
        attachments        = new HashSet<Attachment>();
    	timestampLastSend = null; 
    	timestampLastUpdateCheck = null; 
    	mbImageId = null; 
	}
    /**
	 * @return the height
	 */
    @Column(name = "Height", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
	public Integer getHeight()
	{
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(Integer height)
	{
		this.height = height;
	}

	/**
	 * @return the width
	 */
    @Column(name = "Width", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
	public Integer getWidth()
	{
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(Integer width)
	{
		this.width = width;
	}

	/**
	 * @return the resolution
	 */
    @Column(name = "Resolution", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
	public Double getResolution()
	{
		return resolution;
	}

	/**
	 * @param resolution the resolution to set
	 */
	public void setResolution(Double resolution)
	{
		this.resolution = resolution;
	}

	/**
	 * @return the magnification
	 */
    @Column(name = "Magnification", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
	public Double getMagnification()
	{
		return magnification;
	}

	/**
	 * @param magnification the magnification to set
	 */
	public void setMagnification(Double magnification)
	{
		this.magnification = magnification;
	}

	/**
	 * @return the creativeCommons
	 */
    @Column(name = "CreativeCommons", length = 128)
	public String getCreativeCommons()
	{
		return creativeCommons;
	}

	/**
	 * @param creativeCommons the creativeCommons to set
	 */
	public void setCreativeCommons(String creativeCommons)
	{
		this.creativeCommons = creativeCommons;
	}

    /**
     * @return the attachments
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "attachmentImageAttribute")
    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    /**
     * @param attachments
     */
    public void setAttachments(Set<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    /**
     * @return the single attachment
     */
    @Transient
    public Attachment getAttachment()
    {
    	if (attachments == null || attachments.size() == 0)
    	{
    		return null;
    	}
    	
    	return attachments.iterator().next();
    }
    
    /**
	 * @return the morphBankView
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "MorphBankViewID", nullable = true)
	public MorphBankView getMorphBankView() 
	{
		return morphBankView;
	}

	/**
	 * @param morphBankView the morphBankView to set
	 */
	public void setMorphBankView(MorphBankView morphBankView) 
	{
		this.morphBankView = morphBankView;
	}

	/**
	 * @return the mbImageId
	 */
    @Column(name = "MBImageID", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
	public Integer getMbImageId()
	{
		return mbImageId;
	}

	/**
	 * @param mbImageId the mbImageId to set
	 */
	public void setMbImageId(Integer mbImageId)
	{
		this.mbImageId = mbImageId;
	}

	/**
	 * @return the lastSendTimestamp
	 */
    @Column(name = "TimestampLastSend")
	public Timestamp getTimestampLastSend()
	{
		return timestampLastSend;
	}

	/**
	 * @param lastSendTimestamp the lastSendTimestamp to set
	 */
	public void setTimestampLastSend(Timestamp lastSendTimestamp)
	{
		this.timestampLastSend = lastSendTimestamp;
	}

	/**
	 * @return the lastUpdateCheckTimestamp
	 */
    @Column(name = "TimestampLastUpdateCheck")
	public Timestamp getTimestampLastUpdateCheck()
	{
		return timestampLastUpdateCheck;
	}

	/**
	 * @param lastUpdateCheckTimestamp the lastUpdateCheckTimestamp to set
	 */
	public void setTimestampLastUpdateCheck(Timestamp lastUpdateCheckTimestamp)
	{
		this.timestampLastUpdateCheck = lastUpdateCheckTimestamp;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
	 */
	@Override
	public void forceLoad() 
	{
		super.forceLoad();
		for (Attachment att : getAttachments())
		{
			att.getId(); 
		}
	}
	
	

}
