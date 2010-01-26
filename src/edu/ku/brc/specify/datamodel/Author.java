/* Copyright (C) 2009, University of Kansas Center for Research
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
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.util.Orderable;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "author", uniqueConstraints = { @UniqueConstraint(columnNames = { "OrderNumber", "ReferenceWorkID" }) })
public class Author extends DataModelObjBase implements java.io.Serializable,
                                                        Orderable,
                                                        Comparable<Author>
{

    // Fields    

     protected Integer       authorId;
     protected Short         orderNumber;
     protected String        remarks;
     protected ReferenceWork referenceWork;
     protected Agent         agent;


    // Constructors

    /** default constructor */
    public Author() {
        //
    }
    
    /** constructor with id */
    public Author(Integer authorId) {
        this.authorId = authorId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        authorId = null;
        orderNumber = null;
        remarks = null;
        referenceWork = null;
        agent = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "AuthorID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getAuthorId() {
        return this.authorId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.authorId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Author.class;
    }
    
    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
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
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * Reference record the Agent authored
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ReferenceWorkID", unique = false, nullable = false, insertable = true, updatable = true)
    public ReferenceWork getReferenceWork() {
        return this.referenceWork;
    }
    
    public void setReferenceWork(ReferenceWork referenceWork) {
        this.referenceWork = referenceWork;
    }

    /**
     *      * Agent record representing the Author
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }
    
    @Override
    @Transient
    public String getIdentityTitle()
    {
        String name = "";
        if (agent != null)
        {
            name = agent.getIdentityTitle();
        }

        if (StringUtils.isNotEmpty(name))
        {
            return name;
        }
        
        return super.getIdentityTitle();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return ReferenceWork.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return referenceWork != null ? referenceWork.getId() : null;
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
        return 17;
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
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Author obj)
    {
        return orderNumber != null && obj != null && obj.orderNumber != null ? orderNumber.compareTo(obj.orderNumber) : 0;
    }



}
