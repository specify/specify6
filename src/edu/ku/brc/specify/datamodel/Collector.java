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

import java.sql.Timestamp;

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
import org.hibernate.annotations.Index;

import edu.ku.brc.util.Orderable;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "collector", uniqueConstraints = { @UniqueConstraint(columnNames = {"AgentID", "CollectingEventID"}) })
@org.hibernate.annotations.Table(appliesTo="collector", indexes =
    {   
        @Index (name="COLTRColMemIDX", columnNames={"CollectionMemberID"})
    })
public class Collector extends CollectionMember implements java.io.Serializable, 
                                                           Orderable, 
                                                           Comparable<Collector>,
                                                           Cloneable
{

    // Fields    

     protected Integer         collectorId;
     protected Integer         orderNumber;
     protected Boolean         isPrimary;
     protected String          remarks;
     protected CollectingEvent collectingEvent;
     protected Agent           agent;


    // Constructors

    /** default constructor */
    public Collector() 
    {
        //
    }
    
    /** constructor with id */
    public Collector(Integer collectorId) 
    {
        this.collectorId = collectorId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        collectorId     = null;
        orderNumber     = null;
        isPrimary       = true;
        remarks         = null;
        collectingEvent = null;
        agent           = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "CollectorID")
    public Integer getCollectorId() 
    {
        return this.collectorId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.collectorId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Collector.class;
    }
    
    public void setCollectorId(Integer collectorId) {
        this.collectorId = collectorId;
    }

    /**
     * 
     */
    @Column(name = "OrderNumber", nullable = false)
    public Integer getOrderNumber() {
        return this.orderNumber;
    }
    
    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    /**
     * @return the isPrimary
     */
    @Column(name = "IsPrimary", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getIsPrimary()
    {
        return isPrimary;
    }

    /**
     * @param isPrimary the isPrimary to set
     */
    public void setIsPrimary(Boolean isPrimary)
    {
        this.isPrimary = isPrimary;
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
     *      * The CollectingEvent the agent participated in
     */
    @ManyToOne
    @JoinColumn(name = "CollectingEventID", nullable = false)
    public CollectingEvent getCollectingEvent()
    {
        return this.collectingEvent;
    }
    
    public void setCollectingEvent(CollectingEvent collectingEvent) 
    {
        this.collectingEvent = collectingEvent;
    }

    /**
     *      * Link to Collector's record in Agent table
     */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "AgentID", nullable = false)
    public Agent getAgent()
    {
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
        return 30;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Orderable#getOrderIndex()
     */
    @Transient
    public int getOrderIndex()
    {
        return getOrderNumber();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Orderable#setOrderIndex(int)
     */
    public void setOrderIndex(int order)
    {
        setOrderNumber(order);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Collector obj)
    {
        return orderNumber.compareTo(obj.orderNumber);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Collector obj = (Collector)super.clone();
        obj.initialize();
        
        obj.remarks         = remarks;
        obj.collectingEvent = collectingEvent;
        obj.agent           = agent;
        obj.orderNumber     = orderNumber;
        
        obj.timestampCreated     = new Timestamp(System.currentTimeMillis());
        obj.timestampModified    = timestampCreated;
        
        return obj;
    }

}
