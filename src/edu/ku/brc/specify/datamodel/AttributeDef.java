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


/**
 * @author rods
 *
 */
public class AttributeDef  implements java.io.Serializable {

    // Fields

     protected Long attributeDefId;
     protected Short tableType;
     protected String fieldName;
     protected Short dataType;
     protected CollectionObjDef collectionObjDef;
     protected PrepType prepType;
     protected Set<CollectingEventAttr> collectingEventAttrs;
     protected Set<PreparationAttr> preparationAttr;
     protected Set<CollectionObjectAttr> collectionObjectAttrs;
     protected Set<ExternalResourceAttr> externalResourcesAttrs;


    // Constructors

    /** default constructor */
    public AttributeDef() {
    }

    /** constructor with id */
    public AttributeDef(Long attributeDefId) {
        this.attributeDefId = attributeDefId;
    }




    // Initializer
    public void initialize()
    {
        attributeDefId = null;
        tableType = null;
        fieldName = null;
        dataType = null;
        collectionObjDef = null;
        prepType = null;
        collectingEventAttrs = new HashSet<CollectingEventAttr>();
        preparationAttr = new HashSet<PreparationAttr>();
        collectionObjectAttrs = new HashSet<CollectionObjectAttr>();
        externalResourcesAttrs = new HashSet<ExternalResourceAttr>();
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    public Long getAttributeDefId() {
        return this.attributeDefId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.attributeDefId;
    }

    public void setAttributeDefId(Long attributeDefId) {
        this.attributeDefId = attributeDefId;
    }

    /**
     *
     */
    public Short getTableType() {
        return this.tableType;
    }

    public void setTableType(Short tableType) {
        this.tableType = tableType;
    }

    /**
     *
     */
    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     *
     */
    public Short getDataType() {
        return this.dataType;
    }

    public void setDataType(Short dataType) {
        this.dataType = dataType;
    }

    /**
     *
     */
    public CollectionObjDef getCollectionObjDef() {
        return this.collectionObjDef;
    }

    public void setCollectionObjDef(CollectionObjDef collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }

    /**
     *
     */
    public PrepType getPrepType() {
        return this.prepType;
    }

    public void setPrepType(PrepType prepType) {
        this.prepType = prepType;
    }

    /**
     *
     */
    public Set<CollectingEventAttr> getCollectingEventAttrs() {
        return this.collectingEventAttrs;
    }

    public void setCollectingEventAttrs(Set<CollectingEventAttr> collectingEventAttrs) {
        this.collectingEventAttrs = collectingEventAttrs;
    }

    /**
     *
     */
    public Set getPreparationAttr() {
        return this.preparationAttr;
    }

    public void setPreparationAttr(Set<PreparationAttr> preparationAttr) {
        this.preparationAttr = preparationAttr;
    }

    /**
     *
     */
    public Set<CollectionObjectAttr> getCollectionObjectAttrs() {
        return this.collectionObjectAttrs;
    }

    public void setCollectionObjectAttrs(Set<CollectionObjectAttr> collectionObjectAttrs) {
        this.collectionObjectAttrs = collectionObjectAttrs;
    }

    /**
     *
     */
    public Set<ExternalResourceAttr> getexternalResourcesAttrs() {
        return this.externalResourcesAttrs;
    }

    public void setexternalResourcesAttrs(Set<ExternalResourceAttr> externalResourcesAttrs) {
        this.externalResourcesAttrs = externalResourcesAttrs;
    }





    // Add Methods

    public void addCollectingEventAttrs(final CollectingEventAttr collectingEventAttr)
    {
        this.collectingEventAttrs.add(collectingEventAttr);
        collectingEventAttr.setDefinition(this);
    }

    public void addPreparationAttrs(final PreparationAttr preparationAttr)
    {
        this.preparationAttr.add(preparationAttr);
        preparationAttr.setDefinition(this);
    }

    public void addCollectionObjectAttrs(final CollectionObjectAttr collectionObjectAttr)
    {
        this.collectionObjectAttrs.add(collectionObjectAttr);
        collectionObjectAttr.setDefinition(this);
    }

    public void addExternalResourcesAttrs(final ExternalResourceAttr externalResourceAttr)
    {
        this.externalResourcesAttrs.add(externalResourceAttr);
        externalResourceAttr.setDefinition(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeCollectingEventAttrs(final CollectingEventAttr collectingEventAttr)
    {
        this.collectingEventAttrs.remove(collectingEventAttr);
        collectingEventAttr.setCollectingEvent(null);
    }

    public void removePreparationAttrs(final PreparationAttr preparationAttr)
    {
        this.preparationAttr.remove(preparationAttr);
        preparationAttr.setPreparation(null);
    }

    public void removeCollectionObjectAttrs(final CollectionObjectAttr collectionObjectAttr)
    {
        this.collectionObjectAttrs.remove(collectionObjectAttr);
        collectionObjectAttr.setCollectionObject(null);
    }

    public void removeExternalResourcesAttrs(final ExternalResourceAttr externalResource)
    {
        this.externalResourcesAttrs.remove(externalResource);
        externalResource.setExternalResource(null);
    }

    // Delete Add Methods
}
