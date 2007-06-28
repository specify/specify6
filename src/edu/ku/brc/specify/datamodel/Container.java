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

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "container")
public class Container extends DataModelObjBase implements java.io.Serializable {

    // Fields

     protected Long containerId;
     protected Long collectionObjectId;
     protected Short type;
     protected String name;
     protected String description;
     protected Integer number;
     protected Set<ContainerItem> items;
     protected CollectionObject container;
     protected Location location;


    // Constructors

    /** default constructor */
    public Container() {
        //
    }

    /** constructor with id */
    public Container(Long containerId) {
        this.containerId = containerId;
    }




    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        containerId = null;
        collectionObjectId = null;
        type = null;
        name = null;
        description = null;
        number = null;
        items = new HashSet<ContainerItem>();
        container = null;
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
    public Long getContainerId() {
        return this.containerId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
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

    public void setContainerId(Long containerId) {
        this.containerId = containerId;
    }

    /**
     *
     */
    @Column(name = "CollectionObjectID", unique = false, nullable = true, insertable = true, updatable = true)
    public Long getCollectionObjectId() {
        return this.collectionObjectId;
    }

    public void setCollectionObjectId(Long collectionObjectId) {
        this.collectionObjectId = collectionObjectId;
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
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "container")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<ContainerItem> getItems() {
        return this.items;
    }

    public void setItems(Set<ContainerItem> items) {
        this.items = items;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionObjectID", unique = false, nullable = true, insertable = false, updatable = false)
    public CollectionObject getContainer() {
        return this.container;
    }

    public void setContainer(CollectionObject container) {
        this.container = container;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "LocationID", unique = false, nullable = true, insertable = true, updatable = true)
    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }


    // Add Methods

    public void addItems(final ContainerItem item)
    {
        this.items.add(item);
        item.setContainer(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeItems(final ContainerItem item)
    {
        this.items.remove(item);
        item.setContainer(null);
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
        return 31;
    }

}
