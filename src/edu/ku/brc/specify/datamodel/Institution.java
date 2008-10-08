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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;



/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
//@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "institution")
@org.hibernate.annotations.Table(appliesTo="institution", indexes =
    {   @Index (name="InstNameIDX", columnNames={"Name"})
    })
public class Institution extends UserGroupScope implements java.io.Serializable 
{

    // Fields    

     protected Integer       institutionId;
     protected String        name;
     protected String        title;
     protected String        abbrev;
     protected String        uri;
     protected String        iconURI;
     protected String        ipr;
     protected String        copyright;
     protected String        termsOfUse;
     protected String        disclaimer;
     protected String        remarks;
     protected String        description;
     protected String        license;
     protected Boolean       isServerBased;
     
     protected Address       address;
     protected Set<Agent>    technicalContacts;
     protected Set<Agent>    contentContacts;
     protected Set<Division> divisions;
     protected StorageTreeDef storageTreeDef;

    // Constructors

    /** default constructor */
    public Institution() 
    {
    }
    
    /** constructor with id */
    public Institution(Integer institutionId) 
    {
        super(institutionId);
    }
   
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        name              = null;
        title             = null;
        abbrev            = null;
        uri               = null;
        iconURI           = null;
        ipr               = null;
        copyright         = null;
        termsOfUse        = null;
        disclaimer        = null;
        remarks           = null;
        description       = null;
        license           = null;
        address           = null;
        isServerBased     = false;
        technicalContacts = new HashSet<Agent>();
        contentContacts   = new HashSet<Agent>();
        divisions         = new HashSet<Division>();
        storageTreeDef    = null;
    }
    
    /**
     * @return the institutionId
     */
    public Integer getInstitutionId()
    {
        return getUserGroupScopeId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return getUserGroupScopeId();
    }
    
    /**
     * @return the abbrev
     */
    @Column(name = "Abbrev", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getAbbrev()
    {
        return abbrev;
    }

    /**
     * @return the contentContacts
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "instContentContact")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Agent> getContentContacts()
    {
        return contentContacts;
    }

    /**
     * @return the copyright
     */
    @Lob
    @Column(name = "Copyright", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
    public String getCopyright()
    {
        return copyright;
    }

    /**
     * @return the disclaimer
     */
    @Lob
    @Column(name = "Disclaimer", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
    public String getDisclaimer()
    {
        return disclaimer;
    }

    /**
     * @return the iconURI
     */
    @Column(name = "IconURI", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getIconURI()
    {
        return iconURI;
    }

    /**
     * Intellectual Property.
     * @return the ipr
     */
    @Lob
    @Column(name = "Ipr", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
    public String getIpr()
    {
        return ipr;
    }

    /**
     * @return the license
     */
    @Lob
    @Column(name = "License", unique = false, nullable = true, insertable = true, updatable = true, length = 2048)
    public String getLicense()
    {
        return license;
    }

    /**
     * @param license the license to set
     */
    public void setLicense(String license)
    {
        this.license = license;
    }

    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getName()
    {
        return name;
    }

    /**
     * @return the remarks
     */
    @Lob
    @Column(name = "Remarks", unique = false, nullable = true, insertable = true, updatable = true, length=8192)
    public String getRemarks()
    {
        return remarks;
    }

    /**
     * @return the description
     */
    @Lob
    @Column(name = "Description", length = 8192)
    public String getDescription()
    {
        return description;
    }
    
    /**
     * @return the technicalContacts
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "instTechContact")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Agent> getTechnicalContacts()
    {
        return technicalContacts;
    }

    /**
     * For the Data
     * @return the termsOfUse
     */
    @Lob
    @Column(name = "TermsOfUse", unique = false, nullable = true, insertable = true, updatable = true, length=8192)
    public String getTermsOfUse()
    {
        return termsOfUse;
    }

    /**
     * @return the title
     */
    @Column(name = "Title", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getTitle()
    {
        return title;
    }

    /**
     * @return the uri
     */
    @Column(name = "Uri", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getUri()
    {
        return uri;
    }

    /**
     * @param abbrev the abbrev to set
     */
    @Column(name = "Abbrev", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public void setAbbrev(String abbrev)
    {
        this.abbrev = abbrev;
    }

    /**
     * @return the isServerBased
     */
    @Column(name = "IsServerBased", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getIsServerBased()
    {
        return isServerBased;
    }

    /**
     * @param contentContacts the contentContacts to set
     */
    public void setContentContacts(Set<Agent> contentContacts)
    {
        this.contentContacts = contentContacts;
    }

    /**
     * @param copyright the copyright to set
     */
    public void setCopyright(String copyright)
    {
        this.copyright = copyright;
    }

    /**
     * @param disclaimer the disclaimer to set
     */
    public void setDisclaimer(String disclaimer)
    {
        this.disclaimer = disclaimer;
    }

    /**
     * @param iconURI the iconURI to set
     */
    public void setIconURI(String iconURI)
    {
        this.iconURI = iconURI;
    }

    /**
     * @param institutionId the institutionId to set
     */
    public void setInstitutionId(Integer institutionId)
    {
    	setUserGroupScopeId(institutionId);
    }

    /**
     * @param ipr the ipr to set
     */
    public void setIpr(String ipr)
    {
        this.ipr = ipr;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @param technicalContacts the technicalContacts to set
     */
    public void setTechnicalContacts(Set<Agent> technicalContacts)
    {
        this.technicalContacts = technicalContacts;
    }

    /**
     * @param termsOfUse the termsOfUse to set
     */
    public void setTermsOfUse(String termsOfUse)
    {
        this.termsOfUse = termsOfUse;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri)
    {
        this.uri = uri;
    }

    /**
     * @param isServerBased the isServerBased to set
     */
    public void setIsServerBased(Boolean isServerBased)
    {
        this.isServerBased = isServerBased;
    }

    /**
     * @return the address
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    @JoinColumn(name = "AddressID", unique = false, nullable = true, insertable = true, updatable = true)
    public Address getAddress()
    {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(Address address)
    {
        this.address = address;
    }

    /**
     * @return the divisions
     */
    @OneToMany(cascade = { }, fetch = FetchType.LAZY, mappedBy = "institution")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Division> getDivisions()
    {
        return divisions;
    }

    /**
     * @param divisions the divisions to set
     */
    public void setDivisions(Set<Division> divisions)
    {
        this.divisions = divisions;
    }
    
    /**
     * 
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    @JoinColumn(name = "StorageTreeDefID")
    public StorageTreeDef getStorageTreeDef()
    {
        return this.storageTreeDef;
    }

    public void setStorageTreeDef(StorageTreeDef storageTreeDef)
    {
        this.storageTreeDef = storageTreeDef;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return Institution.class;
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
        return 94;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return StringUtils.isNotEmpty(name) ? name : super.getIdentityTitle();
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getIdentityTitle();
    }

}
