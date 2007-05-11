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

import org.hibernate.annotations.Index;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@Table(name = "workbenchdataitem")
@org.hibernate.annotations.Table(appliesTo="workbenchdataitem", indexes =
    {   
        @Index (name="DataItemRowNumberIDX", columnNames={"rowNumber"}),
    })
@org.hibernate.annotations.Proxy(lazy = false)
public class WorkbenchDataItem implements java.io.Serializable, Comparable<WorkbenchDataItem>
{
    public static final int VAL_NONE   = 0;
    public static final int VAL_OK     = 1;
    public static final int VAL_ERROR  = 2;
        
    // Fields
    protected Long         workbenchDataItemId;
    protected String       cellData;
    protected Short        rowNumber;
    protected Short        validationStatus;
    protected WorkbenchRow workbenchRow;
    protected WorkbenchTemplateMappingItem workbenchTemplateMappingItem;

    // Constructors

    /** default constructor */
    public WorkbenchDataItem()
    {
        //
    }

    public WorkbenchDataItem(final WorkbenchRow workbenchRow, 
                             final WorkbenchTemplateMappingItem wbtmi,
                             final String cellData, 
                             final Short rowNumber)
    {
       initialize();
       this.cellData     = cellData;
       this.rowNumber    = rowNumber;
       this.workbenchRow = workbenchRow;
       this.workbenchTemplateMappingItem = wbtmi;
       workbenchRow.getWorkbenchDataItems().add(this);
    }


    /** constructor with id */
    public WorkbenchDataItem(Long workbenchDataItemId)
    {
        this.workbenchDataItemId = workbenchDataItemId;
    }

    // Initializer
    public void initialize()
    {
        workbenchDataItemId = null;
        cellData            = null;
        rowNumber           = null;
        validationStatus    = VAL_NONE;
        workbenchRow        = null;
        workbenchTemplateMappingItem = null;
    }

    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "WorkbenchDataItemID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getWorkbenchDataItemId()
    {
        return this.workbenchDataItemId;
    }

    /**
     * Generic Getter for the ID Property.
     * 
     * @returns ID Property.
     */
    @Transient
    public Long getId()
    {
        return this.workbenchDataItemId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    public Class<?> getDataClass()
    {
        return WorkbenchDataItem.class;
    }

    public void setWorkbenchDataItemId(Long workbenchDataItemId)
    {
        this.workbenchDataItemId = workbenchDataItemId;
    }

    public static int cellDataLength = 512;
    /**
     * 
     */
    @Column(name = "CellData", length=512, unique = false, nullable = true, insertable = true, updatable = true)
    public String getCellData()
    {
        return this.cellData;
    }

    public void setCellData(String cellData)
    {
        this.cellData = cellData;
    }

    /**
     * 
     */
    @Column(name = "RowNumber", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getRowNumber()
    {
    
        return this.rowNumber;
    }

    public void setRowNumber(Short rowNumber)
    {
        this.rowNumber = rowNumber;
    }

    /**
     * 
     */
    @Transient
    public Short getColumnNumber()
    {
        return getWorkbenchTemplateMappingItem().getViewOrder();
    }

    @Column(name = "ValidationStatus", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getValidationStatus()
    {
        return validationStatus;
    }

    public void setValidationStatus(Short validationStatus)
    {
        this.validationStatus = validationStatus;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "WorkbenchRowID", unique = false, nullable = false, insertable = true, updatable = true)
    public WorkbenchRow getWorkbenchRow()
    {
        return this.workbenchRow;
    }

    public void setWorkbenchRow(WorkbenchRow workbenchRow)
    {
        this.workbenchRow = workbenchRow;
    }
    
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "WorkbenchTemplateMappingItemID", nullable = false)
    public WorkbenchTemplateMappingItem getWorkbenchTemplateMappingItem()
    {
        return workbenchTemplateMappingItem;
    }

    public void setWorkbenchTemplateMappingItem(WorkbenchTemplateMappingItem workbenchTemplateMappingItem)
    {
        this.workbenchTemplateMappingItem = workbenchTemplateMappingItem;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(WorkbenchDataItem obj)
    {
        return getWorkbenchTemplateMappingItem().getViewOrder().compareTo(obj.getWorkbenchTemplateMappingItem().getViewOrder());
    }

	/**
	 * @return the cellDataLength
	 */
	public static int getCellDataLength()
	{
		return cellDataLength;
	}
}
