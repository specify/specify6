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
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;


import java.util.Date;




/**

 */
@Entity
@Table(name = "grouppersons", uniqueConstraints = { @UniqueConstraint(columnNames = { "OrderNumber", "GroupID" }) })
public class GroupPersons extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long groupPersonsId;
     protected Short orderNumber;
     protected String remarks;
     protected Agent group;
     protected Agent member;


    // Constructors

    /** default constructor */
    public GroupPersons() {
        //
    }
    
    /** constructor with id */
    public GroupPersons(Long groupPersonsId) {
        this.groupPersonsId = groupPersonsId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        groupPersonsId = null;
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
    @Column(name = "GroupPersonsID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getGroupPersonsId() {
        return this.groupPersonsId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.groupPersonsId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return GroupPersons.class;
    }
    
    public void setGroupPersonsId(Long groupPersonsId) {
        this.groupPersonsId = groupPersonsId;
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
    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
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
    @Cascade( { CascadeType.SAVE_UPDATE })
    @JoinColumn(name = "MemberID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getMember() {
        return this.member;
    }
    
    public void setMember(Agent agentByMember) {
        this.member = agentByMember;
    }





    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 49;
    }

}
