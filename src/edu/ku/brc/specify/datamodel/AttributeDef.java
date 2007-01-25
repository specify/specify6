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

import javax.persistence.CascadeType;
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


import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * @author rods
 *
 */
@Entity
@Table(name = "attributedef")
public class AttributeDef extends DataModelObjBase implements java.io.Serializable {

    // Fields

     protected Long attributeDefId;
     protected Short tableType;
     protected String fieldName;
     protected Short dataType;
     protected CollectionObjDef collectionObjDef;
     protected PrepType prepType;
     protected Set<CollectingEventAttr> collectingEventAttrs;
     protected Set<PreparationAttr> preparationAttrs;
     protected Set<CollectionObjectAttr> collectionObjectAttrs;

    // Constructors

    /** default constructor */
    public AttributeDef()
    {
        //
    }

    /** constructor with id */
    public AttributeDef(Long attributeDefId) {
        this.attributeDefId = attributeDefId;
    }




    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        attributeDefId = null;
        tableType = null;
        fieldName = null;
        dataType = null;
        collectionObjDef = null;
        prepType = null;
        collectingEventAttrs = new HashSet<CollectingEventAttr>();
        preparationAttrs = new HashSet<PreparationAttr>();
        collectionObjectAttrs = new HashSet<CollectionObjectAttr>();
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "AttributeDefID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getAttributeDefId() {
        return this.attributeDefId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.attributeDefId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return AttributeDef.class;
    }

    public void setAttributeDefId(Long attributeDefId) {
        this.attributeDefId = attributeDefId;
    }

    /**
     *
     */
    @Column(name = "TableType", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getTableType() {
        return this.tableType;
    }

    public void setTableType(Short tableType) {
        this.tableType = tableType;
    }

    /**
     *
     */
    @Column(name = "FieldName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     *
     */
    @Column(name = "DataType", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getDataType() {
        return this.dataType;
    }

    public void setDataType(Short dataType) {
        this.dataType = dataType;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionObjDefID", unique = false, nullable = false, insertable = true, updatable = true)
    public CollectionObjDef getCollectionObjDef() {
        return this.collectionObjDef;
    }

    public void setCollectionObjDef(CollectionObjDef collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PrepTypeID", unique = false, nullable = true, insertable = true, updatable = true)
    public PrepType getPrepType() {
        return this.prepType;
    }

    public void setPrepType(PrepType prepType) {
        this.prepType = prepType;
    }

    /**
     *
     */
    @OneToMany(cascade = { CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "definition")
    public Set<CollectingEventAttr> getCollectingEventAttrs() {
        return this.collectingEventAttrs;
    }

    public void setCollectingEventAttrs(Set<CollectingEventAttr> collectingEventAttrs) {
        this.collectingEventAttrs = collectingEventAttrs;
    }

    /**
     *
     */
    @OneToMany(cascade = { CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "definition")
    public Set<PreparationAttr> getPreparationAttrs() {
        return this.preparationAttrs;
    }

    public void setPreparationAttrs(Set<PreparationAttr> preparationAttrs) {
        this.preparationAttrs = preparationAttrs;
    }

    /**
     *
     */
    @OneToMany(cascade = { CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "definition")
    public Set<CollectionObjectAttr> getCollectionObjectAttrs() {
        return this.collectionObjectAttrs;
    }

    public void setCollectionObjectAttrs(Set<CollectionObjectAttr> collectionObjectAttrs) {
        this.collectionObjectAttrs = collectionObjectAttrs;
    }


    // Add Methods

    public void addCollectingEventAttrs(final CollectingEventAttr collectingEventAttr)
    {
        this.collectingEventAttrs.add(collectingEventAttr);
        collectingEventAttr.setDefinition(this);
    }

    public void addPreparationAttrs(final PreparationAttr preparationAttrArg)
    {
        this.preparationAttrs.add(preparationAttrArg);
        preparationAttrArg.setDefinition(this);
    }

    public void addCollectionObjectAttrs(final CollectionObjectAttr collectionObjectAttr)
    {
        this.collectionObjectAttrs.add(collectionObjectAttr);
        collectionObjectAttr.setDefinition(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeCollectingEventAttrs(final CollectingEventAttr collectingEventAttr)
    {
        this.collectingEventAttrs.remove(collectingEventAttr);
        collectingEventAttr.setCollectingEvent(null);
    }

    public void removePreparationAttrs(final PreparationAttr preparationAttrArg)
    {
        this.preparationAttrs.remove(preparationAttrArg);
        preparationAttrArg.setPreparation(null);
    }

    public void removeCollectionObjectAttrs(final CollectionObjectAttr collectionObjectAttr)
    {
        this.collectionObjectAttrs.remove(collectionObjectAttr);
        collectionObjectAttr.setCollectionObject(null);
    }

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 16;
    }
}
