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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
@Table(name = "collectionreltype")
public class CollectionRelType extends DataModelObjBase implements java.io.Serializable 
{
    protected Integer                     collectionRelTypeId;
    protected String                      name;
    protected String                      remarks;
    protected Set<CollectionRelationship> relationships;
    protected Collection                  leftSideCollection;
    protected Collection                  rightSideCollection;

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        collectionRelTypeId = null;
        name                = null;
        leftSideCollection  = null;
        rightSideCollection = null;
        remarks             = null;
    }
    
    /**
     * @return the collectionRelTypeId
     */
    @Id
    @GeneratedValue
    @Column(name = "CollectionRelTypeID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getCollectionRelTypeId()
    {
        return collectionRelTypeId;
    }

    /**
     * @param collectionRelTypeId the collectionRelTypeId to set
     */
    public void setCollectionRelTypeId(Integer collectionRelTypeId)
    {
        this.collectionRelTypeId = collectionRelTypeId;
    }

    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the remarks
     */
    @Column(name = "Remarks", unique = false, nullable = true, insertable = true, updatable = true, length = 4096)
    public String getRemarks()
    {
        return remarks;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    /**
     * @return the relationships
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collectionRelType")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectionRelationship> getRelationships()
    {
        return relationships;
    }

    /**
     * @param relationships the relationships to set
     */
    public void setRelationships(final Set<CollectionRelationship> relationships)
    {
        this.relationships = relationships;
    }

    /**
     * @return the leftSideCollection
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "LeftSideCollectionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Collection getLeftSideCollection()
    {
        return leftSideCollection;
    }

    /**
     * @param leftSideCollection the leftSideCollection to set
     */
    public void setLeftSideCollection(Collection leftSideCollection)
    {
        this.leftSideCollection = leftSideCollection;
    }

    /**
     * @return the rightSideCollection
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "RightSideCollectionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Collection getRightSideCollection()
    {
        return rightSideCollection;
    }

    /**
     * @param rightSideCollection the rightSideCollection to set
     */
    public void setRightSideCollection(Collection rightSideCollection)
    {
        this.rightSideCollection = rightSideCollection;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CollectionRelType.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return collectionRelTypeId;
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
    @Transient
    public static int getClassTableId()
    {
        return 98;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return name != null ? name : super.getIdentityTitle();
    }
}
