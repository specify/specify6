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


/**
 * @author timo
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "workbenchrowexportedrelationship")
public class WorkbenchRowExportedRelationship extends DataModelObjBase
{

	protected Integer workbenchRowExportedRelationshipId;
	protected WorkbenchRow workbenchRow;
	protected String tableName;
	protected String relationshipName;
	protected Integer recordId;
	protected Integer sequence;
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
	 */
	@Transient
	@Override
	public Class<?> getDataClass()
	{
		return WorkbenchRowExportedRelationship.class;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
	 */
	@Transient
	@Override
	public Integer getId()
	{
		return workbenchRowExportedRelationshipId;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
	 */
	@Transient
	@Override
	public int getTableId()
	{
		return getClassTableId();	
	}

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 126;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
	 */
	@Override
	public void initialize()
	{
        super.init();
        workbenchRowExportedRelationshipId = null;
        workbenchRow = null;
        tableName = null;
        relationshipName = null;
        recordId = null;
        sequence = null;
	}

	/**
	 * @return the workbenchRowExportedRelationshipID
	 */
    @Id
    @GeneratedValue
    @Column(name = "WorkbenchRowExportedRelationshipID")
	public Integer getWorkbenchRowExportedRelationshipId()
	{
		return workbenchRowExportedRelationshipId;
	}

	/**
	 * @param workbenchRowExportedRelationshipID the workbenchRowExportedRelationshipID to set
	 */
	public void setWorkbenchRowExportedRelationshipId(
			Integer workbenchRowExportedRelationshipId)
	{
		this.workbenchRowExportedRelationshipId = workbenchRowExportedRelationshipId;
	}

	/**
	 * @return the workbenchRow
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER) 
    @JoinColumn(name = "WorkbenchRowID", nullable = false)
	public WorkbenchRow getWorkbenchRow()
	{
		return workbenchRow;
	}

	/**
	 * @param workbenchRow the workbenchRow to set
	 */
	public void setWorkbenchRow(WorkbenchRow workbenchRow)
	{
		this.workbenchRow = workbenchRow;
	}

	/**
	 * @return the tableName
	 */
    @Column(name = "TableName", length=120)
	public String getTableName()
	{
		return tableName;
	}

	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	/**
	 * @return the relationshipName
	 */
    @Column(name = "RelationshipName", length=120)
	public String getRelationshipName()
	{
		return relationshipName;
	}

	/**
	 * @param relationshipName the relationshipName to set
	 */
	public void setRelationshipName(String relationshipName)
	{
		this.relationshipName = relationshipName;
	}

	/**
	 * @return the recordId
	 */
    @Column(name = "RecordID")
	public Integer getRecordId()
	{
		return recordId;
	}

	/**
	 * @param recordId the recordId to set
	 */
	public void setRecordId(Integer recordId)
	{
		this.recordId = recordId;
	}

	/**
	 * @return the sequence
	 */
    @Column(name = "Sequence")
	public Integer getSequence()
	{
		return sequence;
	}

	/**
	 * @param sequence the sequence to set
	 */
	public void setSequence(Integer sequence)
	{
		this.sequence = sequence;
	}

	
}
