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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "groupperson", uniqueConstraints = { @UniqueConstraint(columnNames = { "OrderNumber", "GroupID" }) })
public class GroupPerson extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long groupPersonId;
     protected Short orderNumber;
     protected String remarks;
     protected Agent group;
     protected Agent member;


    // Constructors

    /** default constructor */
    public GroupPerson() {
        //
    }
    
    /** constructor with id */
    public GroupPerson(Long groupPersonId) {
        this.groupPersonId = groupPersonId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        groupPersonId = null;
        orderNumber = null;
        remarks = null;
        group = null;
        member = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "GroupPersonID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getGroupPersonId() {
        return this.groupPersonId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
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
    
    public void setGroupPersonId(Long groupPersonId) {
        this.groupPersonId = groupPersonId;
    }

    /**
     * 
     */
    @Column(name = "OrderNumber", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getOrderNumber() {
        return this.orderNumber;
    }
    
    public void setOrderNumber(Short orderNumber) {
        this.orderNumber = orderNumber;
    }

    /**
     * 
     */
    @Lob
    @Column(name="Remarks", unique=false, nullable=true, updatable=true, insertable=true)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * AgentID of group
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "GroupID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getGroup() {
        return this.group;
    }
    
    public void setGroup(Agent agentByGroup) {
        this.group = agentByGroup;
    }

    /**
     *      * AgentID of member (member must be of type Person)
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "MemberID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getMember() {
        return this.member;
    }
    
    public void setMember(Agent agentByMember) {
        this.member = agentByMember;
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

}
