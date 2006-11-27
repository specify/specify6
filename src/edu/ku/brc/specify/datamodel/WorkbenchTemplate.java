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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class WorkbenchTemplate extends DataModelObjBase implements java.io.Serializable 
{

    // Fields    

     protected Long                              workbenchTemplateId;
     protected String                            name;
     protected String                            remarks;
     protected Set<Workbench>                    workbenches;
     protected Set<WorkbenchTemplateMappingItem> workbenchTemplateMappingItems;


    // Constructors

    /** default constructor */
    public WorkbenchTemplate() {
    }
    
    /** constructor with id */
    public WorkbenchTemplate(Long workbenchTemplateId) 
    {
        this.workbenchTemplateId = workbenchTemplateId;
    }
   
    
    // Initializer
    public void initialize()
    {
        workbenchTemplateId = null;
        name = null;
        remarks = null;
        workbenches = new HashSet<Workbench>();
        workbenchTemplateMappingItems = new HashSet<WorkbenchTemplateMappingItem>();   
        
        timestampCreated = new Date();
        timestampModified = new Date();
        lastEditedBy = null;
    }
    
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getWorkbenchTemplateId() {
        return this.workbenchTemplateId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.workbenchTemplateId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class getDataClass()
    {
        return WorkbenchTemplate.class;
    }
    
    public void setWorkbenchTemplateId(Long workbenchTemplateId) {
        this.workbenchTemplateId = workbenchTemplateId;
    }

    /**
     * 
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     * 
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    public String getLastEditedBy()
    {
        return lastEditedBy;
    }

    public void setLastEditedBy(String lastEditedBy)
    {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     * 
     */
    public Set<Workbench> getWorkbenches() {
        return this.workbenches;
    }
    
    public void setWorkbenches(Set<Workbench> workbenches) {
        this.workbenches = workbenches;
    }

    /**
     * 
     */
    public Set<WorkbenchTemplateMappingItem> getWorkbenchTemplateMappingItems() 
    {
        return this.workbenchTemplateMappingItems;
    }
    
    public void setWorkbenchTemplateMappingItems(Set<WorkbenchTemplateMappingItem> workbenchTemplateMappingItems) 
    {
        this.workbenchTemplateMappingItems = workbenchTemplateMappingItems;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 81;
    }


}
