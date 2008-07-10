/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
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

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jul 9, 2008
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spexportschema")
@org.hibernate.annotations.Table(appliesTo="spexportschema")
public class SpExportSchema extends DataModelObjBase
{
    protected Integer spExportSchemaId;
    protected String  schemaName;
    protected String  schemaVersion;
    protected Set<SpExportSchemaItem> spExportSchemaItems;
    
    /**
     * 
     */
    public SpExportSchema()
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
        spExportSchemaId    = null;
        schemaName          = null;
        schemaVersion       = null;
        spExportSchemaItems = new HashSet<SpExportSchemaItem>();
    }

    /**
     * @param exportSchemaId the exportSchemaId to set
     */
    public void setSpExportSchemaId(Integer exportSchemaId)
    {
        this.spExportSchemaId = exportSchemaId;
    }

    /**
     * @param schemaName the schemaName to set
     */
    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    /**
     * @param schemaVersion the schemaVersion to set
     */
    public void setSchemaVersion(String schemaVersion)
    {
        this.schemaVersion = schemaVersion;
    }

    /**
     * @param items the items to set
     */
    public void setSpExportSchemaItems(Set<SpExportSchemaItem> items)
    {
        this.spExportSchemaItems = items;
    }
    
    
    /**
     * @return the exportSchemaId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpExportSchemaID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpExportSchemaId()
    {
        return spExportSchemaId;
    }

    /**
     * @return the schemaName
     */
    @Column(name = "SchemaName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getSchemaName()
    {
        return schemaName;
    }

    /**
     * @return the schemaVersion
     */
    @Column(name = "SchemaVersion", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getSchemaVersion()
    {
        return schemaVersion;
    }

    /**
     * @return the items
     */
    @OneToMany(mappedBy = "spExportSchema")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpExportSchemaItem> getSpExportSchemaItems()
    {
        return spExportSchemaItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpExportSchema.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spExportSchemaId;
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
        return 524;
    }
}
