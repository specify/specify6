/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.util.Orderable;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "groupperson", uniqueConstraints = { 
        @UniqueConstraint(columnNames = { "OrderNumber", "GroupID" }) 
       })
public class GroupPerson extends DataModelObjBase implements java.io.Serializable,
                                                             Orderable,
                                                             Comparable<GroupPerson>,
                                                             Cloneable
                                                             
{

    // Fields    

     protected Integer groupPersonId;
     protected Short   orderNumber;
     protected String  remarks;
     protected Agent   group;
     protected Agent   member;
     
     protected Division division;


    // Constructors

    /** default constructor */
    public GroupPerson() {
        //
    }
    
    /** constructor with id */
    public GroupPerson(Integer groupPersonId) {
        this.groupPersonId = groupPersonId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        groupPersonId = null;
        orderNumber   = null;
        remarks       = null;
        group         = null;
        member        = null;
        
        division = AppContextMgr.getInstance().hasContext() ? AppContextMgr.getInstance().getClassObject(Division.class) : null;
        
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "GroupPersonID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getGroupPersonId() 
    {
        return this.groupPersonId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.groupPersonId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return GroupPerson.class;
    }
    
    public void setGroupPersonId(Integer groupPersonId) 
    {
        this.groupPersonId = groupPersonId;
    }

    /**
     * 
     */
    @Column(name = "OrderNumber", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getOrderNumber() 
    {
        return this.orderNumber;
    }
    
    public void setOrderNumber(Short orderNumber) 
    {
        this.orderNumber = orderNumber;
    }

    /**
     * 
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() 
    {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) 
    {
        this.remarks = remarks;
    }

    /**
     * AgentID of group
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "GroupID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getGroup() 
    {
        return this.group;
    }
    
    public void setGroup(Agent agentByGroup) 
    {
        this.group = agentByGroup;
    }

    /**
     * AgentID of member (member must be of type Person)
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "MemberID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getMember() 
    {
        return this.member;
    }
    
    public void setMember(Agent agentByMember) 
    {
        this.member = agentByMember;
    }
    
    /**
     * @return the division
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DivisionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Division getDivision()
    {
        return division;
    }

    /**
     * @param division the division to set
     */
    public void setDivision(Division division)
    {
        this.division = division;
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
        return 49;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Orderable#getOrderIndex()
     */
    @Transient
    public int getOrderIndex()
    {
        return orderNumber;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Orderable#setOrderIndex(int)
     */
    public void setOrderIndex(int order)
    {
        orderNumber = (short)order;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Agent.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return member != null ? member.getId() : group != null ? group.getId() : null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        String name = "";
        if (member != null)
        {
            name = member.getIdentityTitle();
        }

        if (StringUtils.isNotEmpty(name))
        {
            return name;
        }
        
        return super.getIdentityTitle();
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(GroupPerson obj)
    {
        return orderNumber != null && obj != null && obj.orderNumber != null ? orderNumber.compareTo(obj.orderNumber) : 0;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        GroupPerson obj = (GroupPerson)super.clone();
        obj.init();
        obj.setGroupPersonId(null);
        return obj;
    }
}
