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

import java.util.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import edu.ku.brc.ui.forms.FormDataObjIFace;



/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "institution")
public class Institution extends DataModelObjBase implements java.io.Serializable 
{

    // Fields    

     protected Long       institutionId;
     protected String     name;
     protected String     title;
     protected String     abbrev;
     protected String     uri;
     protected String     iconURI;
     protected String     ipr;
     protected String     copyright;
     protected String     termsOfUse;
     protected String     disclaimer;
     protected String     remarks;
     protected Set<Agent> technicalContacts;
     protected Set<Agent> contentContacts;


    // Constructors

    /** default constructor */
    public Institution() {
    }
    
    /** constructor with id */
    public Institution(Long institutionId) {
        this.institutionId = institutionId;
    }
   
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        institutionId     = null;
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
        technicalContacts = new HashSet<Agent>();
        contentContacts   = new HashSet<Agent>();
    }
    
    /**
     * @return the institutionId
     */
    @Id
    @GeneratedValue
    @Column(name = "InstitutionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getInstitutionId()
    {
        return institutionId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Long getId()
    {
        return institutionId;
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
    public Set<Agent> getContentContacts()
    {
        return contentContacts;
    }

    /**
     * @return the copyright
     */
    @Lob
    @Column(name = "Copyright", unique = false, nullable = true, insertable = true, updatable = true, length = 16000)
    public String getCopyright()
    {
        return copyright;
    }

    /**
     * @return the disclaimer
     */
    @Lob
    @Column(name = "Disclaimer", unique = false, nullable = true, insertable = true, updatable = true, length = 16000)
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
    @Column(name = "Ipr", unique = false, nullable = true, insertable = true, updatable = true, length = 16000)
    public String getIpr()
    {
        return ipr;
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
    @Column(name = "Remarks", unique = false, nullable = true, insertable = true, updatable = true, length=16000)
    public String getRemarks()
    {
        return remarks;
    }

    /**
     * @return the technicalContacts
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "instTechContact")
    public Set<Agent> getTechnicalContacts()
    {
        return technicalContacts;
    }

    /**
     * For the Data
     * @return the termsOfUse
     */
    @Lob
    @Column(name = "TermsOfUse", unique = false, nullable = true, insertable = true, updatable = true, length=16000)
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
    public void setInstitutionId(Long institutionId)
    {
        this.institutionId = institutionId;
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

    @Override
    @Transient
    public String getIdentityTitle()
    {
        if (name!=null)
        {
            return name;
        }
        
        return super.getIdentityTitle();
    }

    @Override
    public void addReference(FormDataObjIFace ref, String refType)
    {

        super.addReference(ref, refType);
    }

    @Override
    public void removeReference(FormDataObjIFace ref, String refType)
    {
        super.removeReference(ref, refType);
    }


}