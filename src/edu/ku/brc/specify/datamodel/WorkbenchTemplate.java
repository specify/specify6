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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.HashSet;
import java.util.Set;

/**

 */
@Entity
@Table(name = "workbenchtemplate")
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
        //
    }
    
    /** constructor with id */
    public WorkbenchTemplate(Long workbenchTemplateId) 
    {
        this.workbenchTemplateId = workbenchTemplateId;
    }
   
    
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        workbenchTemplateId = null;
        name = null;
        remarks = null;
        workbenches = new HashSet<Workbench>();
        workbenchTemplateMappingItems = new HashSet<WorkbenchTemplateMappingItem>();   
    }
    
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "WorkbenchTemplateID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getWorkbenchTemplateId() {
        return this.workbenchTemplateId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.workbenchTemplateId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return WorkbenchTemplate.class;
    }
    
    public void setWorkbenchTemplateId(Long workbenchTemplateId) {
        this.workbenchTemplateId = workbenchTemplateId;
    }

    /**
     * 
     */
    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     */
    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "workbenchTemplate")
    public Set<Workbench> getWorkbenches() {
        return this.workbenches;
    }
    
    public void setWorkbenches(Set<Workbench> workbenches) {
        this.workbenches = workbenches;
    }

    /**
     * 
     */

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "workbenchTemplates")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<WorkbenchTemplateMappingItem> getWorkbenchTemplateMappingItems() 
    {
        return this.workbenchTemplateMappingItems;
    }
    
    public void setWorkbenchTemplateMappingItems(Set<WorkbenchTemplateMappingItem> workbenchTemplateMappingItems) 
    {
        this.workbenchTemplateMappingItems = workbenchTemplateMappingItems;
    }

    public void addWorkbenchDataItem(Workbench workbench)
    {
        workbenches.add(workbench);
        workbench.setWorkbenchTemplate(this);
        //item.set
    }
    public void addWorkbenches(final Workbench workbench)
    {
        this.workbenches.add(workbench);
        workbench.setWorkbenchTemplate(this);
    }   
    /**
     * @param workbench - 
     * void
     */
    public void removeWorkbenchDataItem(Workbench workbench)
    {
        this.workbenches.remove(workbench);
        workbench.setWorkbenchTemplate(null);
    } 
    
    /**
     * @param workbench - 
     * void
     */
    public void removeWorkbench(final Workbench workbench)
    {
        this.workbenches.remove(workbench);
        workbench.setWorkbenchTemplate(null);
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 81;
    }
    
    @Override
    @Transient
    public String getIdentityTitle()
    { 
        if(name!=null)return name;
        return super.getIdentityTitle();
    }

}
