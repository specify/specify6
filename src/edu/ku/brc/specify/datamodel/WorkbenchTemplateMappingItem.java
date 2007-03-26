/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
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
 * Items are sorted by ViewOrder
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@Table(name = "workbenchtemplatemappingitem")
@org.hibernate.annotations.Proxy(lazy = false)
public class WorkbenchTemplateMappingItem extends DataModelObjBase implements java.io.Serializable, Comparable<WorkbenchTemplateMappingItem>
{

    // Fields

    protected Long              workbenchTemplateMappingItemId;
    protected String            tableName;
    protected Integer           srcTableId;
    protected String            fieldName;
    protected String            caption;
    protected Short             viewOrder;             // The Current View Order
    protected Short             origImportColumnIndex; // The index from the imported data file
    protected String            dataType;
    protected Short             fieldLength;            // the length of the data from the specify Schema, usually for strings.
    protected WorkbenchTemplate workbenchTemplate;
    protected Boolean           isExportableToContent;
    protected Boolean           isIncludedInTitle;

    // UI Layout extras
    protected String            metaData;
    protected Short             xCoord;
    protected Short             yCoord;
    protected Boolean           carryForward;

    // Constructors

    /** default constructor */
    public WorkbenchTemplateMappingItem()
    {
        //
    }

    /** constructor with id */
    public WorkbenchTemplateMappingItem(Long workbenchTemplateMappingItemId)
    {
        this.workbenchTemplateMappingItemId = workbenchTemplateMappingItemId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        workbenchTemplateMappingItemId = null;
        tableName = null;
        srcTableId = null;
        fieldName = null;
        caption = null;
        viewOrder = null;
        origImportColumnIndex = null;
        dataType = null;
        fieldLength = -1;
        workbenchTemplate = null;
        metaData = null;
        xCoord = -1;
        yCoord = -1;
        carryForward          = false;
        isExportableToContent = true;
        isIncludedInTitle     = false;

    }

    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "WorkbenchTemplateMappingItemID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getWorkbenchTemplateMappingItemId()
    {
        return this.workbenchTemplateMappingItemId;
    }

    /**
     * Generic Getter for the ID Property.
     * 
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.workbenchTemplateMappingItemId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return WorkbenchTemplateMappingItem.class;
    }

    public void setWorkbenchTemplateMappingItemId(Long workbenchTemplateMappingItemId)
    {
        this.workbenchTemplateMappingItemId = workbenchTemplateMappingItemId;
    }

    /**
     * 
     */
    @Column(name = "TableName", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getTableName()
    {
        return this.tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    /**
     * 
     */
    @Column(name = "TableId", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public Integer getSrcTableId()
    {
        return this.srcTableId;
    }

    public void setSrcTableId(Integer srcTableId)
    {
        this.srcTableId = srcTableId;
    }

    /**
     * 
     */
    @Column(name = "FieldName", unique = false, nullable = true, insertable = true, updatable = true)
    public String getFieldName()
    {
        return this.fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * 
     */
    @Column(name = "Caption", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getCaption()
    {
        return this.caption;
    }

    public void setCaption(String caption)
    {
        this.caption = caption;
    }

    /**
     * 
     */
    @Column(name = "ViewOrder", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getViewOrder()
    {
        return this.viewOrder;
    }

    public void setViewOrder(Short viewOrder)
    {
        this.viewOrder = viewOrder;
    }

    /**
     * 
     */
    @Column(name = "DataColumnIndex", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getOrigImportColumnIndex()
    {
        return this.origImportColumnIndex;
    }

    public void setOrigImportColumnIndex(Short dataColumnIndex)
    {
        this.origImportColumnIndex = dataColumnIndex;
    }

    /**
     * 
     */
    @Column(name = "DataType", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getDataType()
    {
        return this.dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    /**
     * @return the fieldLength
     */
    @Column(name = "FieldLength", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getFieldLength()
    {
        return fieldLength;
    }

    /**
     * @param fieldLength the fieldLength to set
     */
    public void setFieldLength(Short dataLength)
    {
        this.fieldLength = dataLength;
    }


    @Column(name = "MetaData", length=128, unique = false, nullable = true, insertable = true, updatable = true)
    public String getMetaData()
    {
        return metaData;
    }

    public void setMetaData(String metaData)
    {
        this.metaData = metaData;
    }

    @Column(name = "XCoord", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getXCoord()
    {
        return xCoord;
    }

    public void setXCoord(Short coord)
    {
        xCoord = coord;
    }

    @Column(name = "YCoord", unique = false, nullable = true, insertable = true, updatable = true)
   public Short getYCoord()
    {
        return yCoord;
    }

    public void setYCoord(Short coord)
    {
        yCoord = coord;
    }

    @Column(name="CarryForward",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getCarryForward()
    {
        return carryForward;
    }

    public void setCarryForward(Boolean carryForward)
    {
        this.carryForward = carryForward;
    }

    @Column(name="IsExportableToContent",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getIsExportableToContent()
    {
        return isExportableToContent;
    }

    public void setIsExportableToContent(Boolean isExportableToContent)
    {
        this.isExportableToContent = isExportableToContent;
    }

    @Column(name="IsIncludedInTitle",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getIsIncludedInTitle()
    {
        return isIncludedInTitle;
    }

    public void setIsIncludedInTitle(Boolean isIncludedInTitle)
    {
        this.isIncludedInTitle = isIncludedInTitle;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "WorkbenchTemplateID", unique = false, nullable = false, insertable = true, updatable = true)
    public WorkbenchTemplate getWorkbenchTemplate()
    {
        return this.workbenchTemplate;
    }

    public void setWorkbenchTemplate(WorkbenchTemplate workbenchTemplate)
    {
        this.workbenchTemplate = workbenchTemplate;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(WorkbenchTemplateMappingItem obj)
    {
        return viewOrder.compareTo(obj.viewOrder);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return caption != null ? caption : fieldName;
    }

    /*
     * (non-Javadoc)
     * 
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
        return 82;
    }

}
