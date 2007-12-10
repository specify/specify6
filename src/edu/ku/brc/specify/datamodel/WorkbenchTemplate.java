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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "workbenchtemplate")
public class WorkbenchTemplate extends DataModelObjBase implements java.io.Serializable, Comparable<WorkbenchTemplate>
{

     // Fields    

     protected Integer                           workbenchTemplateId;
     protected String                            name;
     protected String                            remarks;
     protected Set<Workbench>                    workbenches;
     protected Set<WorkbenchTemplateMappingItem> workbenchTemplateMappingItems;
     protected SpecifyUser                       specifyUser;
     protected String                            srcFilePath;

    // Constructors

    /** default constructor */
    public WorkbenchTemplate() 
    {
        //
    }
    
    /** constructor with id */
    public WorkbenchTemplate(Integer workbenchTemplateId) 
    {
        this.workbenchTemplateId = workbenchTemplateId;
    }
   
    
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        workbenchTemplateId = null;
        name                = null;
        srcFilePath         = null;
        remarks             = null;
        workbenches         = new HashSet<Workbench>();
        workbenchTemplateMappingItems = new HashSet<WorkbenchTemplateMappingItem>();
        specifyUser         = null;
    }
    
    // End Initializer
    
    /**
     * Assumes it is connected to a Session and forces all the data to be loaded. 
     */
    public void forceLoad()
    {
        for (WorkbenchTemplateMappingItem item : getWorkbenchTemplateMappingItems())
        {
            item.getFieldName();
        }
    }

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "WorkbenchTemplateID")
    public Integer getWorkbenchTemplateId() {
        return this.workbenchTemplateId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
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
    
    public void setWorkbenchTemplateId(Integer workbenchTemplateId) {
        this.workbenchTemplateId = workbenchTemplateId;
    }

    /**
     * 
     */
    @Column(name = "Name", length = 64)
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the path to the original File.
     * @return the path to the original File.
     */
    @Column(name = "SrcFilePath", length = 255)
    public String getSrcFilePath()
    {
        return srcFilePath;
    }

    /**
     * Sets the path to the original file.
     * @param srcFilePath the path.
     */
    public void setSrcFilePath(String srcFilePath)
    {
        this.srcFilePath = srcFilePath;
    }
    
    /**
     * 
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    @OneToMany(mappedBy = "workbenchTemplate")
    public Set<Workbench> getWorkbenches() {
        return this.workbenches;
    }
    
    public void setWorkbenches(Set<Workbench> workbenches) {
        this.workbenches = workbenches;
    }

    /**
     * 
     */

    @OneToMany(mappedBy = "workbenchTemplate")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<WorkbenchTemplateMappingItem> getWorkbenchTemplateMappingItems() 
    {
        return this.workbenchTemplateMappingItems;
    }
    
    public void setWorkbenchTemplateMappingItems(Set<WorkbenchTemplateMappingItem> workbenchTemplateMappingItems) 
    {
        this.workbenchTemplateMappingItems = workbenchTemplateMappingItems;
    }
    
    /**
     * 
     */
    @ManyToOne
    @JoinColumn(name = "SpecifyUserID", nullable = false)
    public SpecifyUser getSpecifyUser() {
        return this.specifyUser;
    }
    
    public void setSpecifyUser(SpecifyUser owner) {
        this.specifyUser = owner;
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(WorkbenchTemplate obj)
    {
        return name.compareTo(obj.name);
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
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 81;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    { 
        return StringUtils.isNotEmpty(name) ? name : super.getIdentityTitle();
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        WorkbenchTemplate wbt = (WorkbenchTemplate)super.clone();
        wbt.workbenchTemplateId = null;
        wbt.name        = name;
        wbt.remarks     = remarks;
        wbt.specifyUser = specifyUser;
        wbt.srcFilePath = srcFilePath;
        
        wbt.workbenches                   = new HashSet<Workbench>();
        wbt.workbenchTemplateMappingItems = new HashSet<WorkbenchTemplateMappingItem>();
        
        for (WorkbenchTemplateMappingItem item : workbenchTemplateMappingItems)
        {
            WorkbenchTemplateMappingItem newItem = (WorkbenchTemplateMappingItem)item.clone();
            newItem.setWorkbenchTemplate(wbt);
            wbt.workbenchTemplateMappingItems.add(newItem);
        }
        
        return wbt;

    }

}
