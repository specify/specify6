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

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "containeritem")
public class ContainerItem extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long containerItemId;
     protected Container container;
     protected Set<CollectionObject> collectionObjects;


    // Constructors

    /** default constructor */
    public ContainerItem() {
        //
    }
    
    /** constructor with id */
    public ContainerItem(Long containerItemId) {
        this.containerItemId = containerItemId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        containerItemId = null;
        container = null;
        collectionObjects = new HashSet<CollectionObject>();
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "ContainerItemID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getContainerItemId() {
        return this.containerItemId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.containerItemId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return ContainerItem.class;
    }
    
    public void setContainerItemId(Long containerItemId) {
        this.containerItemId = containerItemId;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ContainerID", unique = false, nullable = false, insertable = true, updatable = true)
    public Container getContainer() {
        return this.container;
    }
    
    public void setContainer(Container container) {
        this.container = container;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "containerItem")
    public Set<CollectionObject> getCollectionObjects() {
        return this.collectionObjects;
    }
    
    public void setCollectionObjects(Set<CollectionObject> collectionObjects) {
        this.collectionObjects = collectionObjects;
    }





    // Add Methods

    public void addCollectionObjects(final CollectionObject collectionObject)
    {
        this.collectionObjects.add(collectionObject);
        collectionObject.setContainerItem(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeCollectionObjects(final CollectionObject collectionObject)
    {
        this.collectionObjects.remove(collectionObject);
        collectionObject.setContainerItem(null);
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
        return 32;
    }

}
