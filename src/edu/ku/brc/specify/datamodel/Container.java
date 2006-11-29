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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class Container extends DataModelObjBase implements java.io.Serializable {

    // Fields

     protected Long containerId;
     protected Integer collectionObjectId;
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
    }

    /** constructor with id */
    public Container(Long containerId) {
        this.containerId = containerId;
    }




    // Initializer
    public void initialize()
    {
        containerId = null;
        collectionObjectId = null;
        type = null;
        name = null;
        description = null;
        number = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        items = new HashSet<ContainerItem>();
        container = null;
        location = null;
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    public Long getContainerId() {
        return this.containerId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.containerId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
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
    public Integer getCollectionObjectId() {
        return this.collectionObjectId;
    }

    public void setCollectionObjectId(Integer collectionObjectId) {
        this.collectionObjectId = collectionObjectId;
    }

    /**
     *
     */
    public Short getType() {
        return this.type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    /**
     *
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     */
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     */
    public Integer getNumber() {
        return this.number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    /**
     *
     */
    public Set<ContainerItem> getItems() {
        return this.items;
    }

    public void setItems(Set<ContainerItem> items) {
        this.items = items;
    }

    /**
     *
     */
    public CollectionObject getContainer() {
        return this.container;
    }

    public void setContainer(CollectionObject container) {
        this.container = container;
    }

    /**
     *
     */
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

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 31;
    }

}
