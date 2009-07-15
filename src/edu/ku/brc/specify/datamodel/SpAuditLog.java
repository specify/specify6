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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spauditlog")
public class SpAuditLog extends DataModelObjBase implements java.io.Serializable 
{
    public enum ACTION {Insert, Update, Remove};
    // Fields

    protected Integer           spAuditLogId;
    protected Short             tableNum;
    protected Integer           recordId;
    protected Short             parentTableNum;
    protected Integer           parentRecordId;
    protected Integer           recordVersion;
    protected Byte              action;
    
    protected Set<SpAuditLogField> fields;

    // Constructors

    /** default constructor */
    public SpAuditLog() 
    {
        //
    }

    /** constructor with id */
    public SpAuditLog(Integer AuditLogId) 
    {
        this.spAuditLogId = AuditLogId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        spAuditLogId    = null;
        tableNum        = null;
        recordId        = null;
        parentTableNum  = null;
        parentRecordId  = null;
        recordVersion   = null;
        action          = null;
        
        fields          = new HashSet<SpAuditLogField>();
    }
    // End Initializer

    // Property accessors

    /**
     *  PrimaryKey
     */
    @Id
    @GeneratedValue
    @Column(name = "SpAuditLogID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpAuditLogId() 
    {
        return this.spAuditLogId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.spAuditLogId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return SpAuditLog.class;
    }

    public void setSpAuditLogId(Integer AuditLogId) 
    {
        this.spAuditLogId = AuditLogId;
    }

    /**
     *
     */
    @Column(name = "TableNum", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getTableNum() 
    {
        return this.tableNum;
    }

    public void setTableNum(Short tableNum) 
    {
        this.tableNum = tableNum;
    }

    
    /**
     * @return the recordVersion
     */
    @Column(name = "RecordVersion", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getRecordVersion()
    {
        return recordVersion;
    }

    /**
     * @param recordVersion the recordVersion to set
     */
    public void setRecordVersion(Integer recordVersion)
    {
        this.recordVersion = recordVersion;
    }

    /**
	 * @return the recordId
	 */
    @Column(name = "RecordId", unique = false, nullable = true, insertable = true, updatable = true)
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
     * @return the parentTableNum
     */
    @Column(name = "ParentTableNum", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getParentTableNum()
    {
        return parentTableNum;
    }

    /**
     * @param parentTableNum the parentTableNum to set
     */
    public void setParentTableNum(Short parentTableNum)
    {
        this.parentTableNum = parentTableNum;
    }

    /**
     * @return the parentRecordId
     */
    @Column(name = "ParentRecordId", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getParentRecordId()
    {
        return parentRecordId;
    }

    /**
     * @param parentRecordId the parentRecordId to set
     */
    public void setParentRecordId(Integer parentRecordId)
    {
        this.parentRecordId = parentRecordId;
    }

    /**
     * @return the action
     */
    @Column(name = "Action", unique = false, nullable = false, insertable = true, updatable = true)
    public Byte getAction()
    {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(Byte action)
    {
        this.action = action;
    }

    /**
     * @return the fields
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spAuditLog")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpAuditLogField> getFields()
    {
        return fields;
    }

    /**
     * @param fields the fields to set
     */
    public void setFields(Set<SpAuditLogField> fields)
    {
        this.fields = fields;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(tableNum);
        sb.append(" / ");
        sb.append(action);

        return sb.toString();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
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
        return 530;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Transient
    @Override
    public boolean isChangeNotifier()
    {
        return false;
    }

}
