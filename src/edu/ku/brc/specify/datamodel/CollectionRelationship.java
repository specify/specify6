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
/**
 * 
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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 12, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "collectionrelationship")
public class CollectionRelationship extends DataModelObjBase implements java.io.Serializable 
{
    protected Long              collectionRelationshipId;
    protected CollectionRelType collectionRelType;
    protected CollectionObject        leftSide;
    protected CollectionObject        rightSide;
    
    public CollectionRelationship()
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        collectionRelationshipId = null;
        collectionRelType = null;
        leftSide = null;
        rightSide = null;
    }


    /**
     * @return the collectionRelationshipId
     */
    @Id
    @GeneratedValue
    @Column(name = "CollectionRelationshipID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getCollectionRelationshipId()
    {
        return collectionRelationshipId;
    }

    /**
     * @param collectionRelationshipId the collectionRelationshipId to set
     */
    public void setCollectionRelationshipId(Long collectionRelationshipId)
    {
        this.collectionRelationshipId = collectionRelationshipId;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "CollectionRelTypeID", unique = false, nullable = true, insertable = true, updatable = true)
    public CollectionRelType getCollectionRelType() {
        return this.collectionRelType;
    }

    public void setCollectionRelType(CollectionRelType collectionRelType) {
        this.collectionRelType = collectionRelType;
    }
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LSCollectionID", unique = false, nullable = false, insertable = true, updatable = true)
    public CollectionObject getLeftSide() {
        return this.leftSide;
    }
    
    public void setLeftSide(CollectionObject leftSide) {
        this.leftSide = leftSide;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "RSCollectionID", unique = false, nullable = false, insertable = true, updatable = true)
    public CollectionObject getRightSide() {
        return this.rightSide;
    }
    
    public void setRightSide(CollectionObject rightSide) {
        this.rightSide = rightSide;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CollectionRelationship.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Long getId()
    {
        return collectionRelationshipId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
     */
    @Transient
    @Override
    public int getTableId()
    {
        return 99;
    }
}
