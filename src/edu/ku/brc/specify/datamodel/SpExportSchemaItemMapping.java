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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.SpExportSchemaItem;
import edu.ku.brc.specify.datamodel.SpQueryField;

/**
 * @author timbo
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spexportschemaitemmapping")
@org.hibernate.annotations.Table(appliesTo="spexportschemaitemmapping")
public class SpExportSchemaItemMapping extends DataModelObjBase
{
    protected Integer               spExportSchemaItemMappingId;
    protected SpExportSchemaMapping	spExportSchemaMapping;
    protected SpExportSchemaItem    exportSchemaItem;
    protected SpQueryField			queryField;
    protected String                remarks;
    
    /**
     * Default constructor.
     */
    public SpExportSchemaItemMapping()
    {
    	//nothing
    }
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
	 */
	@Override
    @Transient
	public Class<?> getDataClass()
	{
		return SpExportSchemaItemMapping.class;
	}
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
	 */
	@Override
    @Transient
	public Integer getId()
	{
		return spExportSchemaItemMappingId;
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
        return 527;
    }
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
	 */
	@Override
	public void initialize()
	{
		super.init();
		spExportSchemaItemMappingId = null;
		spExportSchemaMapping = null;
		exportSchemaItem = null;
		queryField = null;
		remarks = null;
	}
	/**
	 * @return the spExportSchemaItemMappingId
	 */
    @Id
    @GeneratedValue
    @Column(name = "SpExportSchemaItemMappingID", unique = false, nullable = false, insertable = true, updatable = true)
	public Integer getSpExportSchemaItemMappingId()
	{
		return spExportSchemaItemMappingId;
	}
	/**
	 * @param spExportSchemaItemMappingId the spExportSchemaItemMappingId to set
	 */
	public void setSpExportSchemaItemMappingId(Integer spExportSchemaItemMappingId)
	{
		this.spExportSchemaItemMappingId = spExportSchemaItemMappingId;
	}
	/**
	 * @return the exportSchemaItem
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ExportSchemaItemID", unique = false, nullable = false, insertable = true, updatable = true)
	public SpExportSchemaItem getExportSchemaItem()
	{
		return exportSchemaItem;
	}
	/**
	 * @param exportSchemaItem the exportSchemaItem to set
	 */
	public void setExportSchemaItem(SpExportSchemaItem exportSchemaItem)
	{
		this.exportSchemaItem = exportSchemaItem;
	}
	/**
	 * @return the exportMapping
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpQueryFieldID", unique = false, nullable = false, insertable = true, updatable = true)
	public SpQueryField getQueryField()
	{
		return queryField;
	}
	/**
	 * @param queryField the queryField to set
	 */
	public void setQueryField(SpQueryField queryField)
	{
		this.queryField = queryField;
	}

    /**
     * @return the schema mapping
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.LOCK })
    @JoinColumn(name = "SpExportSchemaMappingID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpExportSchemaMapping getExportSchemaMapping()
    {
    	return this.spExportSchemaMapping;
    }
    
    /**
     * @param spExportSchemaMapping
     */
    public void setExportSchemaMapping(SpExportSchemaMapping spExportSchemaMapping)
    {
    	this.spExportSchemaMapping = spExportSchemaMapping;
    }

	/**
	 * @return the remarks
	 */
    @Column(name = "Remarks", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
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
    
    
}
