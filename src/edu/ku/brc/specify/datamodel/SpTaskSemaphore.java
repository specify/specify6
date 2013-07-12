/* Copyright (C) 2013, University of Kansas Center for Research
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

import java.sql.Timestamp;

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

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "sptasksemaphore")
@SuppressWarnings("serial")
public class SpTaskSemaphore extends DataModelObjBase implements java.io.Serializable 
{

    // Fields

    protected Integer           spTaskSemaphoreId;
    protected String            taskName;
    protected String            context;
    protected String            machineName;
    protected Byte              scope;
    protected SpecifyUser       owner; 
    protected Discipline        discipline;
    protected Collection        collection;
    protected Integer			usageCount;

    protected Boolean           isLocked;
    protected Timestamp         lockedTime;

    // Constructors

    /** default constructor */
    public SpTaskSemaphore() 
    {
        //
    }

    /** constructor with id */
    public SpTaskSemaphore(Integer taskSemaphoreId) 
    {
        this.spTaskSemaphoreId = taskSemaphoreId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        spTaskSemaphoreId = null;
        taskName        = null;
        context         = null;
        machineName     = null;
        owner           = null;
        isLocked        = null;
        lockedTime      = null;
        discipline      = null;
        collection      = null;
        usageCount		= null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * PrimaryKey
     */
    @Id
    @GeneratedValue
    @Column(name = "TaskSemaphoreID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpTaskSemaphoreId() 
    {
        return this.spTaskSemaphoreId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.spTaskSemaphoreId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return SpTaskSemaphore.class;
    }

    public void setSpTaskSemaphoreId(Integer taskSemaphoreId) 
    {
        this.spTaskSemaphoreId = taskSemaphoreId;
    }

    /**
     *
     */
    @Column(name = "TaskName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getTaskName() 
    {
        return this.taskName;
    }

    public void setTaskName(String taskName) 
    {
        this.taskName = taskName;
    }

    
    /**
	 * @return the usageCount
	 */
    @Column(name = "UsageCount", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getUsageCount()
	{
		return usageCount;
	}

	/**
	 * @param usageCount the usageCount to set
	 */
	public void setUsageCount(Integer usageCount)
	{
		this.usageCount = usageCount;
	}

	/**
     * @return the isLocked
     */
    @Column(name = "IsLocked", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsLocked()
    {
        return isLocked == null ? false : isLocked;
    }

    /**
     * @param isLocked the isLocked to set
     */
    public void setIsLocked(Boolean isLocked)
    {
        this.isLocked = isLocked;
    }

    /**
     * @return the lockedTime
     */
    @Column(name = "LockedTime", unique = false, nullable = true, insertable = true, updatable = true)
    public Timestamp getLockedTime()
    {
        return lockedTime;
    }

    /**
     * @param lockedTime the lockedTime to set
     */
    public void setLockedTime(Timestamp lockedTime)
    {
        this.lockedTime = lockedTime;
    }

    /**
     * @return the context
     */
    @Column(name = "Context", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getContext()
    {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(String context)
    {
        this.context = context;
    }

    /**
     * @return the machineName
     */
    @Column(name = "MachineName", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getMachineName()
    {
        return machineName;
    }

    /**
     * @param machineName the machineName to set
     */
    public void setMachineName(String machineName)
    {
        this.machineName = machineName;
    }

    /**
     * @return the scope
     */
    @Column(name = "Scope", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getScope()
    {
        return scope;
    }

    /**
     * @param scope the scope to set
     */
    public void setScope(Byte scope)
    {
        this.scope = scope;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "OwnerID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpecifyUser getOwner() {
        return this.owner;
    }

    public void setOwner(SpecifyUser owner) {
        this.owner = owner;
    }

    /**
     * @return the discipline
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DisciplineID", unique = false, nullable = true, insertable = true, updatable = true)
    public Discipline getDiscipline()
    {
        return discipline;
    }

    /**
     * @param discipline the discipline to set
     */
    public void setDiscipline(Discipline discipline)
    {
        this.discipline = discipline;
    }

    /**
     * @return the collection
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Collection getCollection()
    {
        return collection;
    }

    /**
     * @param collection the collection to set
     */
    public void setCollection(Collection collection)
    {
        this.collection = collection;
    }

    @Override
    @Transient
    public String getIdentityTitle()
    {
        StringBuilder sb = new StringBuilder();
        
        Address.append(sb, taskName);
        Address.append(sb, owner.toString());
        Address.append(sb, isLocked.toString());
        Address.append(sb, lockedTime.toString());

        if (sb.length() > 0)
        {
            return sb.toString();
        }
        return super.getIdentityTitle();
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Transient
    @Override
    public boolean isChangeNotifier()
    {
        return false;
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 526;
    }

}
