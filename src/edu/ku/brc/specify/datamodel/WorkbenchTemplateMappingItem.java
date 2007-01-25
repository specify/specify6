/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
@Table(name = "workbenchtemplatemappingitem")
public class WorkbenchTemplateMappingItem extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long workbenchTemplateMappingItemId;
     protected String tablename;
     protected Integer tableid;
     protected String fieldname;
     protected String caption;
     protected Integer vieworder;
     protected String datatype;
     protected WorkbenchTemplate workbenchTemplates;


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
        tablename = null;
        tableid = null;
        fieldname = null;
        caption = null;
        vieworder = null;
        datatype = null;
        workbenchTemplates = null;
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
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.workbenchTemplateMappingItemId;
    }

    /* (non-Javadoc)
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
    public String getTablename() {
        return this.tablename;
    }
    
    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    /**
     * 
     */
    @Column(name = "TableId", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public Integer getTableid() {
        return this.tableid;
    }
    
    public void setTableid(Integer tableid) {
        this.tableid = tableid;
    }

    /**
     * 
     */
    @Column(name = "FieldName", unique = false, nullable = true, insertable = true, updatable = true)
    public String getFieldname() {
        return this.fieldname;
    }
    
    public void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }

    /**
     * 
     */
    @Column(name = "Caption", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getCaption() {
        return this.caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * 
     */
    @Column(name = "ViewOrder", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getVieworder() {
        return this.vieworder;
    }
    
    public void setVieworder(Integer vieworder) {
        this.vieworder = vieworder;
    }

    /**
     * 
     */
    @Column(name = "DataType", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getDatatype() {
        return this.datatype;
    }
    
    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "WorkbenchTemplateID", unique = false, nullable = false, insertable = true, updatable = true)
    public WorkbenchTemplate getWorkbenchTemplates() {
        return this.workbenchTemplates;
    }
    
    public void setWorkbenchTemplates(WorkbenchTemplate workbenchTemplates) {
        this.workbenchTemplates = workbenchTemplates;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 82;
    }


}
