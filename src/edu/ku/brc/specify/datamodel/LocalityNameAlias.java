/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Sep 12, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "localitynamealias")
@org.hibernate.annotations.Table(appliesTo="localitynamealias", indexes =
    {   @Index (name="LocalityNameAliasIDX", columnNames={"Name"})
    })
public class LocalityNameAlias extends CollectionMember
{
    
    protected Integer  localityNameAliasId;
    protected String   name;
    protected String   source;
    protected Locality locality;

    /**
     * 
     */
    public LocalityNameAlias()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        localityNameAliasId = null;
        name                = null;
        source              = null;
    }
    
    /**
     * @return the localityNameAliasId
     */
    @Id
    @GeneratedValue
    @Column(name = "LocalityNameAliasID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getLocalityNameAliasId()
    {
        return localityNameAliasId;
    }

    /**
     * @param localityNameAliasId the localityNameAliasId to set
     */
    public void setLocalityNameAliasId(Integer localityNameAliasId)
    {
        this.localityNameAliasId = localityNameAliasId;
    }

    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 255)
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @return the source
     */
    @Column(name = "Source", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getSource()
    {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source)
    {
        this.source = source;
    }

    /**
     * @return the locality
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LocalityID", unique = false, nullable = false, insertable = true, updatable = true)
    public Locality getLocality()
    {
        return locality;
    }

    /**
     * @param locality the locality to set
     */
    public void setLocality(Locality locality)
    {
        this.locality = locality;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return LocalityNameAlias.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return localityNameAliasId;
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
    @Transient
    public static int getClassTableId()
    {
        return 120;
    }

    @Override
    @Transient
    public String getIdentityTitle()
    {
        return StringUtils.isNotEmpty(name) ? name : super.getIdentityTitle();
    }


}
