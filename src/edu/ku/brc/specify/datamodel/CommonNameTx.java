/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

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
public class CommonNameTx extends DataModelObjBase implements Serializable
{
    protected Integer            commonNameTxId;
    protected String             country;    // Java Two Character Code
    protected String             language;   // Java Two Character Code
    protected String             variant;    // Java Two Character Code
    protected String             name;
    protected Taxon              taxon;

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

}
