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
 * @author rods
 *
 * @code_status Alpha
 *
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@Table(name = "workbenchdataitem")
public class WorkbenchDataItem extends DataModelObjBase implements java.io.Serializable, Comparable<WorkbenchDataItem>
{

    // Fields

    protected Long      workbenchDataItemId;
    protected String    cellData;
    protected Integer   rowNumber;
    protected Integer   columnNumber;
    protected Workbench workbench;

    // Constructors

    /** default constructor */
    public WorkbenchDataItem()
    {
        //
    }

    /** constructor with id */
    public WorkbenchDataItem(Long workbenchDataItemId)
    {
        this.workbenchDataItemId = workbenchDataItemId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        workbenchDataItemId = null;
        cellData = null;
        rowNumber = null;
        columnNumber = null;
        workbench = null;
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
    @Override
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
    @Override
    public Class<?> getDataClass()
    {
        return WorkbenchDataItem.class;
    }

    public void setWorkbenchDataItemId(Long workbenchDataItemId)
    {
        this.workbenchDataItemId = workbenchDataItemId;
    }

    /**
     * 
     */
    @Column(name = "CellData", length=255, unique = false, nullable = true, insertable = true, updatable = true)
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
    @Column(name = "RowNumber", length = 32, unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getRowNumber()
    {
        return this.rowNumber;
    }

    public void setRowNumber(Integer rowNumber)
    {
        this.rowNumber = rowNumber;
    }

    /**
     * 
     */
    @Column(name = "ColumnNumber", length = 32, unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getColumnNumber()
    {
        return this.columnNumber;
    }

    public void setColumnNumber(Integer columnNumber)
    {
        this.columnNumber = columnNumber;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "WorkbenchID", unique = false, nullable = false, insertable = true, updatable = true)
    public Workbench getWorkbench()
    {
        return this.workbench;
    }

    public void setWorkbench(Workbench workbench)
    {
        this.workbench = workbench;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(WorkbenchDataItem obj)
    {
        return columnNumber.compareTo(obj.columnNumber);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isIndexable()
     */
    @Transient
    @Override
    public boolean isIndexable()
    {
        return false;
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
        return 80;
    }

}
