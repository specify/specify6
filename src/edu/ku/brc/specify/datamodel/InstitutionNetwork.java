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
/**
 * 
 */
package edu.ku.brc.specify.datamodel;

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

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 26, 2011
 *
 */
@Entity
//@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "institutionnetwork")
@org.hibernate.annotations.Table(appliesTo="institutionnetwork", indexes =
  {   @Index (name="InstNetworkNameIDX", columnNames={"Name"})
  })
public class InstitutionNetwork extends DataModelObjBase implements java.io.Serializable 
{
  // Fields    

   protected Integer         institutionNetworkId;
   protected String          name;
   protected String          altName;
   protected String          code;
   protected String          uri;
   protected String          iconURI;
   protected String          ipr;
   protected String          copyright;
   protected String          termsOfUse;
   protected String          disclaimer;
   protected String          remarks;
   protected String          description;
   protected String          license;
   
   
   protected Address         address;
   protected Set<Agent>      contacts;
   protected Set<Collection> collections;

  // Constructors

  /** default constructor */
  public InstitutionNetwork() 
  {
  }
  
  /** constructor with id */
  public InstitutionNetwork(Integer institutionNetworkId) 
  {
      super();
      this.institutionNetworkId = institutionNetworkId;
  }
 
  // Initializer
  @Override
  public void initialize()
  {
      super.init();
      
      institutionNetworkId = null;
      name              = null;
      altName           = null;
      code              = null;
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
      contacts          = new HashSet<Agent>();
      collections       = new HashSet<Collection>();
  }
  
  /**
   * @return the institutionNetworkId
   */
  @Id
  @GeneratedValue
  @Column(name = "InstitutionNetworkID", unique = false, nullable = false, insertable = true, updatable = true)
  public Integer getInstitutionNetworkId()
  {
      return institutionNetworkId;
  }

  /* (non-Javadoc)
   * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
   */
  @Override
  @Transient
  public Integer getId()
  {
      return institutionNetworkId;
  }
  
  /**
   * @return the code
   */
  @Column(name = "Code", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
  public String getCode()
  {
      return code;
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
   * @return the contacts
   */
  @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "instTechContact")
  @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
  public Set<Agent> getContacts()
  {
      return contacts;
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
   * @return the altName
   */
  @Column(name = "AltName", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
  public String getAltName()
  {
      return altName;
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
  @Column(name = "Code", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
  public void setCode(String code)
  {
      this.code = code;
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
   * @param institutionNetworkId the institutionNetworkId to set
   */
  public void setInstitutionNetworkId(Integer institutionNetworkId)
  {
      this.institutionNetworkId = institutionNetworkId;
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
   * @param contacts the contacts to set
   */
  public void setContacts(Set<Agent> contacts)
  {
      this.contacts = contacts;
  }

  /**
   * @param termsOfUse the termsOfUse to set
   */
  public void setTermsOfUse(String termsOfUse)
  {
      this.termsOfUse = termsOfUse;
  }

  /**
   * @param altName the altName to set
   */
  public void setAltName(String altName)
  {
      this.altName = altName;
  }

  /**
   * @param uri the uri to set
   */
  public void setUri(String uri)
  {
      this.uri = uri;
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
   * @return the collections
   */
  @OneToMany(cascade = { }, fetch = FetchType.LAZY, mappedBy = "institutionNetwork")
  @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
  public Set<Collection> getCollections()
  {
      return collections;
  }

  /**
   * @param collections the collections to set
   */
  public void setCollections(Set<Collection> collections)
  {
      this.collections = collections;
  }

  /* (non-Javadoc)
   * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
   */
  @Override
  @Transient
  public Class<?> getDataClass()
  {
      return InstitutionNetwork.class;
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
