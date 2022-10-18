/* Copyright (C) 2022, Specify Collections Consortium
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

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

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

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "container")
@org.hibernate.annotations.Table(appliesTo="container", indexes =
    {   @Index (name="ContainerNameIDX", columnNames={"Name"}),
        @Index (name="ContainerMemIDX", columnNames={"CollectionMemberID"})
    })
public class Container extends CollectionMember implements java.io.Serializable,
                                                           Comparable<Container>
{

     // Fields

     protected Integer               containerId;
     protected Short                 type;
     protected String                name;
     protected String                description;
     protected Integer               number;
     protected Set<CollectionObject> collectionObjects;    // This should only ever hold a single ColObj
     protected Set<CollectionObject> collectionObjectKids;
     protected Storage               storage;

     // Tree
     protected Container             parent;
     protected Set<Container>        children;
     
     // Transient
     protected Vector<Container>     childrenList = null;

    // Constructors

    /** default " */
    public Container() 
    {
        //
    }

    /** constructor with id */
    public Container(Integer containerId) 
    {
        this.containerId = containerId;
        System.err.println("2constructor "+hashCode());
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        containerId            = null;
        type                   = null;
        name                   = null;
        description            = null;
        number                 = null;
        parent                 = null;
        collectionObjects      = new HashSet<CollectionObject>();
        collectionObjectKids = new HashSet<CollectionObject>();
        storage                = null;
        children               = new HashSet<Container>();
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "ContainerID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getContainerId() 
    {
        return this.containerId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.containerId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Container.class;
    }

    public void setContainerId(Integer containerId) 
    {
        this.containerId = containerId;
    }

    /**
     *
     */
    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getType() 
    {
        return this.type;
    }

    public void setType(Short type) 
    {
        this.type = type;
    }

    /**
     *
     */
    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 1024)
    public String getName() 
    {
        return this.name;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    /**
     *
     */
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 65536)
    public String getDescription() 
    {
        return this.description;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    /**
     *
     */
    @Column(name = "Number", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumber() 
    {
        return this.number;
    }

    public void setNumber(Integer number) 
    {
        this.number = number;
    }
    
    /**
     * @return
     */
    @Transient
    public CollectionObject getCollectionObject()
    {
        Set<CollectionObject> colObjsSet = getCollectionObjects();
        return colObjsSet != null && colObjsSet.size() > 0 ? colObjsSet.iterator().next() : null;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "container")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectionObject> getCollectionObjects() 
    {
        return this.collectionObjects;
    }

    public void setCollectionObjects(Set<CollectionObject> collectionObjects) 
    {
        this.collectionObjects = collectionObjects;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "containerOwner")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectionObject> getCollectionObjectKids() 
    {
        return this.collectionObjectKids;
    }

    public void setCollectionObjectKids(Set<CollectionObject> collectionObjectKids) 
    {
        this.collectionObjectKids = collectionObjectKids;
    }
    
    /**
     * @return the childrenList
     */
    @Transient
    public Vector<Container> getChildrenList()
    {
        if (childrenList == null)
        {
            childrenList = new Vector<Container>();
            childrenList.addAll(children);
        }
        return childrenList;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "StorageID", unique = false, nullable = true, insertable = true, updatable = true)
    public Storage getStorage() 
    {
        return this.storage;
    }

    public void setStorage(Storage storage) 
    {
        this.storage = storage;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentID")
    public Container getParent()
    {
        return this.parent;
    }

    public void setParent(Container parent)
    {
        this.parent = parent;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Container.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return parent != null ? parent.getId() : null;
    }

    
    @OneToMany(mappedBy = "parent")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<Container> getChildren()
    {
        return this.children;
    }

    public void setChildren(Set<Container> children)
    {
        this.children = children;
    }
    
    
    // Add Methods

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        try
        {
            getCollectionObjects().size();
            getCollectionObjectKids().size();
            getChildren().size();
            
        } catch (org.hibernate.LazyInitializationException ex)
        {
            // This is temporary for Release 6.3.00 - rods 05/22/11
            DataProviderSessionIFace tmpSession = null;
            try
            {
                tmpSession = DataProviderFactory.getInstance().createSession();
                tmpSession.attach(this);
                getCollectionObjects().size();
                getCollectionObjectKids().size();
                getChildren().size();
                    
            } catch (Exception exx)
            {
                ex.printStackTrace();
                
            } finally
            {
                if (tmpSession != null)
                {
                    tmpSession.close();
                }
            }
        }
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    @Override
    public String toString()
    {
        return StringUtils.isNotEmpty(name) ? name : "N/A";
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Container o)
    {
        if (name == null || o.name == null) return 0;
        
        return name.compareTo(o.name);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Transient
    @Override
    public String getIdentityTitle()
    {
        return StringUtils.isNotEmpty(name) ? name : super.getIdentityTitle();
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 31;
    }

}
