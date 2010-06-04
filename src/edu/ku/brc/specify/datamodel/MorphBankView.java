/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * @author timo
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "morphbankview")
public class MorphBankView extends DataModelObjBase
{
	protected Integer morphBankViewId;
	protected Integer morphBankExternalViewId; //possibly pointing to morphbank's id for this view?
	protected String viewName; //for lookups
	
	protected String imagingTechnique;
	protected String imagingPreparationTechnique;
	protected String specimenPart;
	protected String viewAngle;
	protected String developmentState;
	protected String sex;
	protected String form;
	
	protected Set<AttachmentImageAttribute> attachmentImageAttributes;
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
	 */
	@Transient
	@Override
	public Class<?> getDataClass()
	{
		return MorphBankView.class;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
	 */
	@Transient
	@Override
	public Integer getId()
	{
		return getClassTableId();
	}

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 138;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
	 */
    @Transient
	@Override
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
		super.init();
		morphBankViewId = null;
		morphBankExternalViewId = null; 
		viewName = null; 
		
		imagingTechnique = null;
		imagingPreparationTechnique = null;
		specimenPart = null;
		viewAngle = null;
		developmentState = null;
		sex = null;
		form = null;
		
		attachmentImageAttributes = new HashSet<AttachmentImageAttribute>();
	}

	/**
	 * @return the morphBankViewId
	 */
    @Id
    @GeneratedValue
    @Column(name = "MorphBankViewID", unique = false, nullable = false, insertable = true, updatable = true)
	public Integer getMorphBankViewId()
	{
		return morphBankViewId;
	}

	/**
	 * @param morphBankViewId the morphBankViewId to set
	 */
	public void setMorphBankViewId(Integer morphBankViewId)
	{
		this.morphBankViewId = morphBankViewId;
	}

	/**
	 * @return the morphBankExternalViewId
	 */
    @Column(name = "MorphBankExternalViewID", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getMorphBankExternalViewId()
	{
		return morphBankExternalViewId;
	}

	/**
	 * @param morphBankExternalViewId the morphBankExternalViewId to set
	 */
	public void setMorphBankExternalViewId(Integer morphBankExternalViewId)
	{
		this.morphBankExternalViewId = morphBankExternalViewId;
	}

	/**
	 * @return the viewName
	 */
	@Column(name = "ViewName", length=128, unique = false, nullable = true, insertable = true, updatable = true)	
	public String getViewName()
	{
		return viewName;
	}

	/**
	 * @param viewName the viewName to set
	 */
	public void setViewName(String viewName)
	{
		this.viewName = viewName;
	}

	/**
	 * @return the imagingTechnique
	 */
	@Column(name = "ImagingTechnique", length=128, unique = false, nullable = true, insertable = true, updatable = true)	
	public String getImagingTechnique()
	{
		return imagingTechnique;
	}

	/**
	 * @param imagingTechnique the imagingTechnique to set
	 */
	public void setImagingTechnique(String imagingTechnique)
	{
		this.imagingTechnique = imagingTechnique;
	}

	/**
	 * @return the imagingPreparationTechnique
	 */
	@Column(name = "ImagingPreparationTechnique", length=128, unique = false, nullable = true, insertable = true, updatable = true)	
	public String getImagingPreparationTechnique()
	{
		return imagingPreparationTechnique;
	}

	/**
	 * @param imagingPreparationTechnique the imagingPreparationTechnique to set
	 */
	public void setImagingPreparationTechnique(String imagingPreparationTechnique)
	{
		this.imagingPreparationTechnique = imagingPreparationTechnique;
	}

	/**
	 * @return the specimenPart
	 */
	@Column(name = "SpecimenPart", length=128, unique = false, nullable = true, insertable = true, updatable = true)	
	public String getSpecimenPart()
	{
		return specimenPart;
	}

	/**
	 * @param specimenPart the specimenPart to set
	 */
	public void setSpecimenPart(String specimenPart)
	{
		this.specimenPart = specimenPart;
	}

	/**
	 * @return the viewAngle
	 */
	@Column(name = "ViewAngle", length=128, unique = false, nullable = true, insertable = true, updatable = true)	
	public String getViewAngle()
	{
		return viewAngle;
	}

	/**
	 * @param viewAngle the viewAngle to set
	 */
	public void setViewAngle(String viewAngle)
	{
		this.viewAngle = viewAngle;
	}

	/**
	 * @return the developmentState
	 */
	@Column(name = "DevelopmentState", length=128, unique = false, nullable = true, insertable = true, updatable = true)	
	public String getDevelopmentState()
	{
		return developmentState;
	}

	/**
	 * @param developmentState the developmentState to set
	 */
	public void setDevelopmentState(String developmentState)
	{
		this.developmentState = developmentState;
	}

	/**
	 * @return the sex
	 */
	@Column(name = "Sex", length=32, unique = false, nullable = true, insertable = true, updatable = true)	
	public String getSex()
	{
		return sex;
	}

	/**
	 * @param sex the sex to set
	 */
	public void setSex(String sex)
	{
		this.sex = sex;
	}

	/**
	 * @return the form
	 */
	@Column(name = "Form", length=128, unique = false, nullable = true, insertable = true, updatable = true)	
	public String getForm()
	{
		return form;
	}

	/**
	 * @param form the form to set
	 */
	public void setForm(String form)
	{
		this.form = form;
	}

	/**
	 * @return the attachments
	 */
    @OneToMany(mappedBy = "morphBankView")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
	public Set<AttachmentImageAttribute> getAttachmentImageAttributes()
	{
		return attachmentImageAttributes;
	}

	/**
	 * @param attachments the attachments to set
	 */
	public void setAttachmentImageAttributes(Set<AttachmentImageAttribute> attachmentImageAttributes)
	{
		this.attachmentImageAttributes = attachmentImageAttributes;
	}

	
}
