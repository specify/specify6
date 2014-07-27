/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

/**
 * @author timo
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spsymbiotainstance")
@org.hibernate.annotations.Table(appliesTo="spsymbiotainstance", indexes =
    {   
        @Index (name="SPSYMINSTColMemIDX", columnNames={"CollectionMemberID"})
    })
public class SpSymbiotaInstance extends CollectionMember {
    protected static final Logger log = Logger.getLogger(SpSymbiotaInstance.class);

    protected Integer spSymbiotaInstanceId;
    protected String symbiotaKey;
    protected String instanceName;
    protected String description;
    protected String remarks;
    protected Calendar lastPush;
    protected Calendar lastPull;
    protected Calendar lastCacheBuild;
    
    protected SpExportSchemaMapping schemaMapping; 
    
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
	 */
	@Override
	public void initialize() {
		super.init();
		spSymbiotaInstanceId = null;
		symbiotaKey = null;
		instanceName = null;
		description = null;
		remarks = null;
		lastPush = null;
		lastPull = null;
		lastCacheBuild = null;
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
	 */
	@Override
	@Transient
	public Integer getId() {
		return getSpSymbiotaInstanceId();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
	 */
	@Override
	@Transient
	public int getTableId() {
		return getClassTableId();
	}

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId() {
        return 533;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
	 */
	@Override
	@Transient
	public Class<?> getDataClass() {
		return SpSymbiotaInstance.class;
	}

	/**
	 * @return the spSymbiotaInstanceId
	 */
    @Id
    @GeneratedValue
    @Column(name = "SpSymbiotaInstanceID", unique = false, nullable = false, insertable = true, updatable = true)
	public Integer getSpSymbiotaInstanceId() {
		return spSymbiotaInstanceId;
	}

	/**
	 * @param spSymbiotaInstanceId the spSymbiotaInstanceId to set
	 */
	public void setSpSymbiotaInstanceId(Integer spSymbiotaInstanceId) {
		this.spSymbiotaInstanceId = spSymbiotaInstanceId;
	}

	/**
	 * @return the symbiotaKey
	 */
    @Column(name = "SymbiotaKey", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
	public String getSymbiotaKey() {
		return symbiotaKey;
	}

	/**
	 * @param symbiotaKey the symbiotaKey to set
	 */
	public void setSymbiotaKey(String symbiotaKey) {
		this.symbiotaKey = symbiotaKey;
	}

	/**
	 * @return the instanceName
	 */
    @Column(name = "InstanceName", unique = false, nullable = true, insertable = true, updatable = true, length = 256)
	public String getInstanceName() {
		return instanceName;
	}

	/**
	 * @param instanceName the instanceName to set
	 */
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	/**
	 * @return the description
	 */
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 256)
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return
	 */
	@Lob
	@Column(name = "Remarks")
	public String getRemarks() {
		return this.remarks;
	}

	/**
	 * @param remarks
	 */
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	/**
	 * @return the lastPush
	 */
    @Column(name = "LastPush", nullable = true)
	public Calendar getLastPush() {
		return lastPush;
	}

	/**
	 * @param lastPush the lastPush to set
	 */
	public void setLastPush(Calendar lastPush) {
		this.lastPush = lastPush;
	}

	/**
	 * @return the lastPull
	 */
    @Column(name = "LastPull", nullable = true)
	public Calendar getLastPull() {
		return lastPull;
	}

	/**
	 * @param lastPull the lastPull to set
	 */
	public void setLastPull(Calendar lastPull) {
		this.lastPull = lastPull;
	}

	/**
	 * @return the lastPush
	 */
    @Column(name = "LastCacheBuild", nullable = true)
	public Calendar getLastCacheBuild() {
		return lastCacheBuild;
	}

	/**
	 * @param lastCacheBuild the lastCacheBuild to set
	 */
	public void setLastCacheBuild(Calendar lastCacheBuild) {
		this.lastCacheBuild = lastCacheBuild;
	}

	/**
	 * @return the schemaMapping
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.LOCK })
    @JoinColumn(name = "SchemaMappingID", unique = false, nullable = true, insertable = true, updatable = true)
	public SpExportSchemaMapping getSchemaMapping() {
		return schemaMapping;
	}

	/**
	 * @param schemaMapping the schemaMapping to set
	 */
	public void setSchemaMapping(SpExportSchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
	}


	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
	 */
	@Override
	public void forceLoad() {
		super.forceLoad();
		schemaMapping.forceLoad();
	}


	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
	 */
	@Override
	public String toString() {
		return getInstanceName();
	}

	
	
}
