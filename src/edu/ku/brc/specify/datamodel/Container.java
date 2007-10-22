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
import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "container")
@org.hibernate.annotations.Table(appliesTo="container", indexes =
    {   @Index (name="ContainerNameIDX", columnNames={"Name"})
    })
public class Container extends CollectionMember implements java.io.Serializable {

    // Fields

     protected Integer               containerId;
     protected Short                 type;
     protected String                name;
     protected String                description;
     protected Integer               number;
     protected Container             parent;
     protected Set<CollectionObject> collectionObjects;
     protected Set<CollectionObject> collectionObjectOwners;
     protected Location              location;


    // Constructors

    /** default constructor */
    public Container() {
        //
    }

    /** constructor with id */
    public Container(Integer containerId) {
        this.containerId = containerId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        containerId = null;
        type = null;
        name = null;
        description = null;
        number = null;
        parent = null;
        collectionObjects = new HashSet<CollectionObject>();
        collectionObjectOwners = new HashSet<CollectionObject>();
        location = null;
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "ContainerID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getContainerId() {
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

    public void setContainerId(Integer containerId) {
        this.containerId = containerId;
    }

    /**
     *
     */
    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getType() {
        return this.type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    /**
     *
     */
    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     */
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true)
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     */
    @Column(name = "Number", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumber() {
        return this.number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    /**
     * @return the parent
     */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "ParentID")
    public Container getParent()
    {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(Container parent)
    {
        this.parent = parent;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "container")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectionObject> getCollectionObjects() {
        return this.collectionObjects;
    }

    public void setCollectionObjects(Set<CollectionObject> collectionObjects) {
        this.collectionObjects = collectionObjects;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "containerOwner")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectionObject> getCollectionObjectOwners() {
        return this.collectionObjectOwners;
    }

    public void setCollectionObjectOwners(Set<CollectionObject> collectionObjectOwners) {
        this.collectionObjectOwners = collectionObjectOwners;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LocationID", unique = false, nullable = true, insertable = true, updatable = true)
    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }


    // Add Methods

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
        return 31;
    }

}
