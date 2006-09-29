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





/**

 */
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
    public WorkbenchTemplateMappingItem() {
    }
    
    /** constructor with id */
    public WorkbenchTemplateMappingItem(Long workbenchTemplateMappingItemId) {
        this.workbenchTemplateMappingItemId = workbenchTemplateMappingItemId;
    }
   
    // Initializer
    public void initialize()
    {
        throw new RuntimeException("Meg need to implement me!");

    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getWorkbenchTemplateMappingItemId() {
        return this.workbenchTemplateMappingItemId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.workbenchTemplateMappingItemId;
    }
    
    public void setWorkbenchTemplateMappingItemId(Long workbenchTemplateMappingItemId) {
        this.workbenchTemplateMappingItemId = workbenchTemplateMappingItemId;
    }

    /**
     * 
     */
    public String getTablename() {
        return this.tablename;
    }
    
    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    /**
     * 
     */
    public Integer getTableid() {
        return this.tableid;
    }
    
    public void setTableid(Integer tableid) {
        this.tableid = tableid;
    }

    /**
     * 
     */
    public String getFieldname() {
        return this.fieldname;
    }
    
    public void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }

    /**
     * 
     */
    public String getCaption() {
        return this.caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * 
     */
    public Integer getVieworder() {
        return this.vieworder;
    }
    
    public void setVieworder(Integer vieworder) {
        this.vieworder = vieworder;
    }

    /**
     * 
     */
    public String getDatatype() {
        return this.datatype;
    }
    
    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    /**
     * 
     */
    public WorkbenchTemplate getWorkbenchTemplates() {
        return this.workbenchTemplates;
    }
    
    public void setWorkbenchTemplates(WorkbenchTemplate workbenchTemplates) {
        this.workbenchTemplates = workbenchTemplates;
    }




}
