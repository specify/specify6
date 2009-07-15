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
@Table(name = "spauditlogfield")
public class SpAuditLogField extends DataModelObjBase implements java.io.Serializable 
{

    // Fields

    protected Integer           spAuditLogFieldId;
    protected String            fieldName;
    protected String            oldValue;
    protected String            newValue;
    protected SpAuditLog        spAuditLog;
    

    // Constructors

    /** default constructor */
    public SpAuditLogField() 
    {
        //
    }

    /** constructor with id */
    public SpAuditLogField(Integer AuditLogFieldId) 
    {
        this.spAuditLogFieldId = AuditLogFieldId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        spAuditLogFieldId = null;
        fieldName         = null;
        oldValue          = null;
        newValue          = null;
    }
    // End Initializer

    // Property accessors

    /**
     *  PrimaryKey
     */
    @Id
    @GeneratedValue
    @Column(name = "SpAuditLogFieldID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpAuditLogFieldId() 
    {
        return this.spAuditLogFieldId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.spAuditLogFieldId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return SpAuditLogField.class;
    }

    public void setSpAuditLogFieldId(Integer AuditLogFieldId) 
    {
        this.spAuditLogFieldId = AuditLogFieldId;
    }

    /**
     * @return the fieldName
     */
    @Column(name = "FieldName", unique = false, nullable = false, insertable = true, updatable = true, length = 32)
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @param desc the desc to set
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * @return the oldValue
     */
    @Column(name = "OldValue", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getOldValue()
    {
        return oldValue;
    }

    /**
     * @param oldValue the oldValue to set
     */
    public void setOldValue(String oldValue)
    {
        this.oldValue = oldValue;
    }

    /**
     * @return the newValue
     */
    @Column(name = "NewValue", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getNewValue()
    {
        return newValue;
    }

    /**
     * @param newValue the newValue to set
     */
    public void setNewValue(String newValue)
    {
        this.newValue = newValue;
    }

    /**
     * @return the spAuditLog
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpAuditLogID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpAuditLog getSpAuditLog()
    {
        return spAuditLog;
    }

    /**
     * @param spAuditLog the spAuditLog to set
     */
    public void setSpAuditLog(SpAuditLog spAuditLog)
    {
        this.spAuditLog = spAuditLog;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return fieldName;
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
        return 531;
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
