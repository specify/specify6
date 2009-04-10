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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

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
    protected Integer           collectionRelationshipId;
    protected CollectionRelType collectionRelType;
    protected CollectionObject  leftSide;
    protected CollectionObject  rightSide;
    protected String            text1;
    protected String            text2;
    
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
        text1     = null;
        text2     = null;
        leftSide  = null;
        rightSide = null;
    }


    /**
     * @return the collectionRelationshipId
     */
    @Id
    @GeneratedValue
    @Column(name = "CollectionRelationshipID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getCollectionRelationshipId()
    {
        return collectionRelationshipId;
    }

    /**
     * @param collectionRelationshipId the collectionRelationshipId to set
     */
    public void setCollectionRelationshipId(Integer collectionRelationshipId)
    {
        this.collectionRelationshipId = collectionRelationshipId;
    }

    /**
     * @return the text1
     */
    @Column(name = "Text1", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getText1()
    {
        return text1;
    }

    /**
     * @param text1 the text1 to set
     */
    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    /**
     * @return the text2
     */
    @Column(name = "Text2", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getText2()
    {
        return text2;
    }

    /**
     * @param text2 the text2 to set
     */
    public void setText2(String text2)
    {
        this.text2 = text2;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
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
    @JoinColumn(name = "LeftSideCollectionID", unique = false, nullable = false, insertable = true, updatable = true)
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
    @JoinColumn(name = "RightSideCollectionID", unique = false, nullable = false, insertable = true, updatable = true)
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
    public Integer getId()
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
