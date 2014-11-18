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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
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
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "workbenchdataitem")
@org.hibernate.annotations.Table(appliesTo="workbenchdataitem", indexes =
    {   
        @Index (name="DataItemRowNumberIDX", columnNames={"rowNumber"})
    })
@SuppressWarnings("serial")
public class WorkbenchDataItem implements java.io.Serializable, Comparable<WorkbenchDataItem>
{
    public static final short VAL_NONE       		= 0;
    public static final short VAL_OK         		= 1;
    public static final short VAL_ERROR      		= 2;
    public static final short VAL_ERROR_EDIT 		= 3;
    public static final short VAL_NEW_DATA   		= 4;
    public static final short VAL_MULTIPLE_MATCH 	= 5;
    public static final short VAL_NOT_MATCHED 		= 6; //match not attempted, 
    													//most likely due to un-matched parent
    
    private static Integer maxWBCellLength = null;

    // Fields
    protected Integer      workbenchDataItemId;
    protected String       cellData;
    protected Short        rowNumber;
    protected Short        validationStatus;
    protected WorkbenchRow workbenchRow;
    protected WorkbenchTemplateMappingItem workbenchTemplateMappingItem;

    //Transient
    protected String	   statusText = null;
    protected boolean	   required = false;
    protected int		   editorValidationStatus = VAL_OK;	//the validation status is actually relative to factors
    														//outside the workbench such as picklist contents. It
    														//is easier to work with when transient
    
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
    public WorkbenchDataItem(Integer workbenchDataItemId)
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
    @Column(name = "WorkbenchDataItemID")
    public Integer getWorkbenchDataItemId()
    {    
        return this.workbenchDataItemId;
    }

    /**
     * Generic Getter for the ID Property.
     * 
     * @return ID Property
     */
    @Transient
    public Integer getId()
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

    public void setWorkbenchDataItemId(Integer workbenchDataItemId)
    {
        this.workbenchDataItemId = workbenchDataItemId;
    }

    /**
     * 
     */
    @Lob
    @Column(name = "CellData")
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
    @Column(name = "RowNumber")
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

    @Column(name = "ValidationStatus")
    public Short getValidationStatus()
    {
        return validationStatus;
    }

    public void setValidationStatus(Short validationStatus)
    {
        this.validationStatus = validationStatus;
    }
    
    /**
     * @return transient validation status
     */
    @Transient
    public int getEditorValidationStatus()
    {
    	return editorValidationStatus;
    }

    /**
     * @param editorValidationStatus the transient validation status to set
     */
    public void setEditorValidationStatus(int editorValidationStatus)
    {
    	this.editorValidationStatus = editorValidationStatus;
    }
    
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "WorkbenchRowID", nullable = false)
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
        if (getWorkbenchTemplateMappingItem() != null &&
            getWorkbenchTemplateMappingItem().getViewOrder() != null &&
            obj.getWorkbenchTemplateMappingItem() != null &&
            obj.getWorkbenchTemplateMappingItem().getViewOrder() != null)
        {
            return getWorkbenchTemplateMappingItem().getViewOrder().compareTo(obj.getWorkbenchTemplateMappingItem().getViewOrder());
        }
        return 0;
    }

    
	/**
     * @return the maxWBCellLength
     */
    public static Integer getMaxWBCellLength()
    {
        return maxWBCellLength;
    }

    /**
     * @param maxWBCellLength the maxWBCellLength to set
     */
    public static void setMaxWBCellLength(final Integer maxWBCellLen)
    {
        WorkbenchDataItem.maxWBCellLength = maxWBCellLen;
    }

	/**
	 * @return the statusText
	 */
	@Transient
	public String getStatusText()
	{
		return statusText;
	}
	
	/**
	 * @param statusText
	 */
	public void setStatusText(String statusText)
	{
		this.statusText = statusText;
	}

	/**
	 * @return the required
	 */
	@Transient
	public boolean isRequired()
	{
		return required;
	}

	/**
	 * @param required the required to set
	 */
	public void setRequired(boolean required)
	{
		this.required = required;
	}
	
	
}
