/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.sql.Timestamp;
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

/**
 * @author timbo
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spexportschemamapping")
@org.hibernate.annotations.Table(appliesTo="spexportschemamapping")
public class SpExportSchemaMapping extends DataModelObjBase
{
    protected Integer							spExportSchemaMappingId;
	protected SpExportSchema					spExportSchema;
	protected String                            mappingName;
	protected String                            description;
	protected Timestamp							timestampExported;
	protected Set<SpExportSchemaItemMapping>	mappings;

	/**
	 * Default constructor.
	 */
	public SpExportSchemaMapping()
	{
		
	}
	
    @Id
    @GeneratedValue
    @Column(name = "SpExportSchemaMappingID", unique = false, nullable = false, insertable = true, updatable = true)
	public Integer getSpExportSchemaMappingId()
	{
		return spExportSchemaMappingId;
	}
	/**
	 * @param spExportSchemaMappingId the spExportSchemaMappingId to set
	 */
	public void setSpExportSchemaMappingId(Integer spExportSchemaMappingId)
	{
		this.spExportSchemaMappingId = spExportSchemaMappingId;
	}
	
	
	/**
	 * @return the timeLastExported
	 */
	   @Column(name = "TimeStampExported")
	public Timestamp getTimestampExported()
	{
		return timestampExported;
	}

	/**
	 * @param timeLastExported the timeLastExported to set
	 */
	public void setTimestampExported(Timestamp timestampExported)
	{
		this.timestampExported = timestampExported;
	}

	/**
	 * @return the exportSchemaItem
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ExportSchemaID", unique = false, nullable = false, insertable = true, updatable = true)
	public SpExportSchema getSpExportSchema()
	{
		return spExportSchema;
	}
	/**
	 * @param spExportSchema the spExportSchema to set
	 */
	public void setSpExportSchema(SpExportSchema spExportSchema)
	{
		this.spExportSchema = spExportSchema;
	}
	
    /**
     * @return the mappings.
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "exportSchemaMapping")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.DELETE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpExportSchemaItemMapping> getMappings()
    {
    	return mappings;
    }
    /**
     * @param mappings the mappings to set.
     */
    public void setMappings(Set<SpExportSchemaItemMapping> mappings)
    {
    	this.mappings = mappings;
    }

    /**
     * @return the mappingName
     */
    @Column(name = "MappingName", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getMappingName()
    {
        return mappingName;
    }

    /**
     * @param mappingName
     */
    public void setMappingName(String mappingName)
    {
    	this.mappingName = mappingName;
    }
    
    /**
     * @return the description
     */
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description
     */
    public void setDescription(String description)
    {
    	this.description = description;
    }
	
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
	 */
	@Override
	@Transient
	public Class<?> getDataClass()
	{
		return SpExportSchemaMapping.class;	
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
	 */
	@Override
    @Transient
	public Integer getId()
	{
		return spExportSchemaMappingId;
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
        return 528;
    }

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
	 */
	@Override
	public void initialize()
	{
		super.init();
		spExportSchemaMappingId = null;
		spExportSchema = null;
		mappingName = null;
		description = null;
		timestampExported = null;
		mappings = new HashSet<SpExportSchemaItemMapping>();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
	 */
	@Override
	public void forceLoad()
	{
		super.forceLoad();
		this.getSpExportSchema();
		for (SpExportSchemaItemMapping map : getMappings())
		{
			map.getExportSchemaItem();
		}
	}

	
//	/**
//	 * @param mapping
//	 * 
//	 * Adds mapping to the mappings for this SchemaMapping.
//	 * Removes any existing mappings to mapping's ExportSchemaItem. 
//	 */
//	public void addMapping(SpExportSchemaItemMapping mapping)
//	{
//		if (mapping.getExportSchemaItem() != null)
//		{
//			Vector<SpExportSchemaItemMapping> toRemove = new Vector<SpExportSchemaItemMapping>();
//			for (SpExportSchemaItemMapping existingMapping : mappings)
//			{
//				if (existingMapping.getExportSchemaItem().getId().equals(mapping.getExportSchemaItem().getId()))
//				{
//					toRemove.add(existingMapping);
//				}
//			}
//			for (SpExportSchemaItemMapping obsoleteMapping : toRemove)
//			{
//				obsoleteMapping.setExportSchemaMapping(null);
//				mappings.remove(obsoleteMapping);
//			}
//		}
//		mapping.setExportSchemaMapping(this);
//		mappings.add(mapping);
//	}
}
