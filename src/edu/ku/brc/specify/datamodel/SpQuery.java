/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 17, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spquery")
@org.hibernate.annotations.Table(appliesTo="spquery", indexes =
    {   @Index (name="SpQueryNameIDX", columnNames={"Name"})
    })
public class SpQuery extends DataModelObjBase
{
    protected Integer           spQueryId;
    protected String            name;
    protected String            contextName;
    protected Short             contextTableId;
    protected String            sqlStr;
    protected Boolean           isFavorite; //whether or not this query goes on the 'short list'
    protected boolean           named = true;
    
    protected Set<SpQueryField> fields;
    protected Set<SpReport>     reports;
    protected SpecifyUser       specifyUser;

 
    /**
     * 
     */
    public SpQuery()
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        spQueryId        = null;
        name             = null;
        contextName      = null;
        contextTableId   = null;
        sqlStr           = null;
        named            = true;
        fields           = new HashSet<SpQueryField>();
        reports          = new HashSet<SpReport>();
        specifyUser      = null;
        isFavorite       = null;
    }
    
    
    /**
     * @param spQueryId the spQueryId to set
     */
    public void setSpQueryId(Integer spQueryId)
    {
        this.spQueryId = spQueryId;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param contextName the contextName to set
     */
    public void setContextName(String contextName)
    {
        this.contextName = contextName;
    }

    /**
     * @param contextTableId the contextTableId to set
     */
    public void setContextTableId(Short contextTableId)
    {
        this.contextTableId = contextTableId;
    }

    /**
     * @param sql the sql to set
     */
    public void setSqlStr(String sqlStr)
    {
        this.sqlStr = sqlStr;
    }

    /**
     * @param tables the fields to set
     */
    public void setFields(Set<SpQueryField> fields)
    {
        this.fields = fields;
    }
    
    public void setSpecifyUser(SpecifyUser owner)
    {
        this.specifyUser = owner;
    }
    
    public void setReports(Set<SpReport> reports)
    {
        this.reports = reports;
    }

    /**
     * @return the spQueryId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpQueryID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpQueryId()
    {
        return spQueryId;
    }

    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName()
    {
        return name;
    }

    /**
     * @return the contextName
     */
    @Column(name = "ContextName", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getContextName()
    {
        return contextName;
    }

    /**
     * @return the contextTableId
     */
    @Column(name = "ContextTableId", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getContextTableId()
    {
        return contextTableId;
    }

    /**
     * @return the sql
     */
    @Column(name = "SqlStr", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getSqlStr()
    {
        return sqlStr;
    }

    /**
     * @return the fields
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "query")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpQueryField> getFields()
    {
        return fields;
    }

    /**
     * @return the reports
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "query")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpReport> getReports()
    {
        return reports;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpecifyUserID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpecifyUser getSpecifyUser() 
    {
        return this.specifyUser;
    }
    
    //----------------------------------------------------------------------
    //-- DataModelObjBase
    //----------------------------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpQuery.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spQueryId;
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
        return 517;
    }

    /**
     * @return the named
     */
    @Transient
    public boolean isNamed()
    {
        return named;
    }

    /**
     * @param named the named to set
     */
    public void setNamed(boolean named)
    {
        this.named = named;
    }

    /**
     * @return the isFavorite
     */
    @Column(name = "IsFavorite", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsFavorite()
    {
        return isFavorite;
    }

    /**
     * @param isFavorite the isFavorite to set
     */
    public void setIsFavorite(Boolean isFavorite)
    {
        this.isFavorite = isFavorite;
    }
    
    
}
