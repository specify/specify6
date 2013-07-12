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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 26, 2008
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spfieldvaluedefault")
@org.hibernate.annotations.Table(appliesTo="spfieldvaluedefault", indexes =
        @Index (name="SpFieldValueDefaultColMemIDX", columnNames={"CollectionMemberID"})
    )
public class SpFieldValueDefault extends CollectionMember implements java.io.Serializable 
{
    // Fields    
    protected Integer                 spFieldValueDefaultId;
    protected String                  tableName;
    protected String                  fieldName;
    protected String                  strValue;
    protected Integer                 idValue;

    // Constructors

    /** default constructor */
    public SpFieldValueDefault()
    {
        // do nothing
    }
    
    /** constructor with id */
    public SpFieldValueDefault(Integer spFieldValueDefaultId) 
    {
        this.spFieldValueDefaultId = spFieldValueDefaultId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        spFieldValueDefaultId = null;
        tableName       = null;
        fieldName       = null;
        strValue        = null;
        idValue         = null;
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "SpFieldValueDefaultID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpFieldValueDefaultId() {
        return this.spFieldValueDefaultId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Integer getId()
    {
        return this.spFieldValueDefaultId;
    }
   
    public void setSpFieldValueDefaultId(Integer spFieldValueDefaultId) {
        this.spFieldValueDefaultId = spFieldValueDefaultId;
    }
    
    /**
     * @param tableIdent the tableIdent to set
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * @param strValue the strValue to set
     */
    public void setStrValue(String strValue)
    {
        this.strValue = strValue;
    }

    /**
     * @param idValue the idValue to set
     */
    public void setIdValue(Integer idValue)
    {
        this.idValue = idValue;
    }

    /**
     * @return the tableIdent
     */
    @Column(name = "TableName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getTableName()
    {
        return tableName;
    }

    /**
     * @return the fieldName
     */
    @Column(name = "FieldName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @return the strValue
     */
    @Column(name = "StrValue", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getStrValue()
    {
        return strValue;
    }

    /**
     * @return the idValue
     */
    @Column(name = "IdValue", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getIdValue()
    {
        return idValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return SpFieldValueDefault.class;
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
        return 520;
    }

}
