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
import javax.persistence.Lob;
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
    protected String				  viewDescription;
    protected String                  imageType;
    protected String				  text1;
    protected String                  text2;
    protected Double                  number1;
    protected Double                  number2;
    protected Boolean                 yesNo1;
    protected Boolean                 yesNo2;
    protected String				  remarks;
    
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
	 * @return the number1
	 */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
	public Double getNumber1() 
    {
		return number1;
	}

	/**
	 * @param number1 the number1 to set
	 */
	public void setNumber1(Double number1) 
	{
		this.number1 = number1;
	}

	/**
	 * @return the number2
	 */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
	public Double getNumber2() 
    {
		return number2;
	}

	/**
	 * @param number2 the number2 to set
	 */
	public void setNumber2(Double number2) 
	{
		this.number2 = number2;
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
	 * @return the viewDescription
	 */
    @Column(name = "ViewDescription", length = 80)
    public String getViewDescription() 
	{
		return viewDescription;
	}

	/**
	 * @param viewDescription the viewDescription to set
	 */
	public void setViewDescription(String viewDescription) 
	{
		this.viewDescription = viewDescription;
	}

	/**
	 * @return the imageType
	 */
    @Column(name = "ImageType", length = 80)
	public String getImageType() 
	{
		return imageType;
	}

	/**
	 * @param imageType the imageType to set
	 */
	public void setImageType(String imageType) 
	{
		this.imageType = imageType;
	}

	
	/**
	 * @return the text1
	 */
    @Column(name = "Text1", length = 200)
	public String getText1() 
	{
		return text1;
	}

	/**
	 * @param text1 the text1 to set
	 */
	public void setText1(String text1) 
	{
		this.text1 = text1;
	}

	/**
	 * @return the text2
	 */
    @Column(name = "Text2", length = 200)
	public String getText2() 
	{
		return text2;
	}

	/**
	 * @param text2 the text2 to set
	 */
	public void setText2(String text2) 
	{
		this.text2 = text2;
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
	 * @param yesNo1 the yesNo1 to set
	 */
	public void setYesNo1(Boolean yesNo1) 
	{
		this.yesNo1 = yesNo1;
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
	 * @param yesNo2 the yesNo2 to set
	 */
	public void setYesNo2(Boolean yesNo2) 
	{
		this.yesNo2 = yesNo2;
	}

	/**
	 * @return the remarks
	 */
    @Lob
    @Column(name = "Remarks", length = 4096)
	public String getRemarks() 
	{
		return remarks;
	}

	/**
	 * @param remarks the remarks to set
	 */
	public void setRemarks(String remarks) 
	{
		this.remarks = remarks;
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
