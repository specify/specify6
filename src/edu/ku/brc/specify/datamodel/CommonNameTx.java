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

import java.io.Serializable;
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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 4, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "commonnametx")
@org.hibernate.annotations.Table(appliesTo="commonnametx", indexes =
    {   @Index (name="CommonNameTxNameIDX", columnNames={"Name"}),
        @Index (name="CommonNameTxCountryIDX", columnNames={"Country"})
    })
public class CommonNameTx extends DataModelObjBase implements Serializable, Cloneable
{
    protected Integer					commonNameTxId;
	protected String					country;		// Java Two Character
														// Code
	protected String					language;		// Java Two Character
														// Code
	protected String					variant;		// Java Two Character
														// Code
	protected String					name;
	protected String					author;
	protected Taxon						taxon;
	protected Set<CommonNameTxCitation>	citations;

    public CommonNameTx()
    {
        super();
    }

    public CommonNameTx(Integer commonNameId)
    {
        super();
        this.commonNameTxId = commonNameId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        commonNameTxId = null;
        name         = null;
        country      = null;
        language     = null;
        variant      = null;
        taxon        = null;
        citations    = new HashSet<CommonNameTxCitation>();
    }

    @Id
    @GeneratedValue
    @Column(name = "CommonNameTxID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getCommonNameTxId()
    {
        return commonNameTxId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return this.commonNameTxId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CommonNameTx.class;
    }

    public void setCommonNameTxId(Integer commonNameId)
    {
        this.commonNameTxId = commonNameId;
    }

    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * @return the language
     */
    @Column(name = "Language", unique = false, nullable = true, insertable = true, updatable = true, length = 2)
    public String getLanguage()
    {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * @return the country
     */
    @Column(name = "Country", unique = false, nullable = true, insertable = true, updatable = true, length = 2)
    public String getCountry()
    {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country)
    {
        this.country = country;
    }

    /**
     * @return the variant
     */
    @Column(name = "Variant", unique = false, nullable = true, insertable = true, updatable = true, length = 2)
    public String getVariant()
    {
        return variant;
    }

    /**
     * @param variant the variant to set
     */
    public void setVariant(String variant)
    {
        this.variant = variant;
    }

    /**
     * @return the taxon
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "TaxonID", unique = false, nullable = false, insertable = true, updatable = true)
    public Taxon getTaxon()
    {
        return taxon;
    }

    /**
     * @param taxon the taxon to set
     */
    public void setTaxon(Taxon taxon)
    {
        this.taxon = taxon;
    }

    
    /**
	 * @return the author
	 */
    @Column(name = "Author", length = 128)
	public String getAuthor()
	{
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author)
	{
		this.author = author;
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Taxon.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return taxon != null ? taxon.getId() : null;
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
        return 106;
    }

    @Override
    @Transient
    public String getIdentityTitle()
    {
        return this.name != null ? this.name : super.getIdentityTitle();
    }

	/**
	 * @return the citations
	 */
    @OneToMany(mappedBy = "commonNameTx")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
	public Set<CommonNameTxCitation> getCitations()
	{
		return citations;
	}

	/**
	 * @param citations the citations to set
	 */
	public void setCitations(Set<CommonNameTxCitation> citations)
	{
		this.citations = citations;
	}


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        CommonNameTx obj = (CommonNameTx)super.clone();
        
        obj.commonNameTxId = null;
        obj.taxon = null;
        obj.citations    = new HashSet<CommonNameTxCitation>(); 
        
        return obj;
    }
}
