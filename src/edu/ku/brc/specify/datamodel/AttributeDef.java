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

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

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

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author rods
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "attributedef")
public class AttributeDef extends DataModelObjBase implements java.io.Serializable {

    // Fields

     protected Integer                  attributeDefId;
     protected Short                    tableType;
     protected String                   fieldName;
     protected Short                    dataType;
     protected Discipline               discipline;
     protected PrepType                 prepType;
     protected Set<CollectingEventAttr> collectingEventAttrs;
     protected Set<PreparationAttr>     preparationAttrs;
     protected Set<CollectionObjectAttr> collectionObjectAttrs;

    // Constructors

    /** default constructor */
    public AttributeDef()
    {
        //
    }

    /** constructor with id */
    public AttributeDef(Integer attributeDefId) {
        this.attributeDefId = attributeDefId;
    }




    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        attributeDefId        = null;
        tableType             = null;
        fieldName             = null;
        dataType              = null;
        discipline            = AppContextMgr.getInstance().getClassObject(Discipline.class);
        prepType              = null;
        collectingEventAttrs  = new HashSet<CollectingEventAttr>();
        preparationAttrs      = new HashSet<PreparationAttr>();
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
    public Integer getAttributeDefId() {
        return this.attributeDefId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
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

    public void setAttributeDefId(Integer attributeDefId) {
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
    @JoinColumn(name = "DisciplineID", unique = false, nullable = false, insertable = true, updatable = true)
    public Discipline getDiscipline() {
        return this.discipline;
    }

    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
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
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
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
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
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
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        
        Vector<Object> ids = BasicSQLUtils.querySingleCol("SELECT AttributeDefID FROM collectingeventattr WHERE AttributeDefID = "+ attributeDefId);
        if (ids.size() == 1)
        {
            parentTblId = CollectingEventAttr.getClassTableId();
            return (Integer)ids.get(0);
        }
        
        ids = BasicSQLUtils.querySingleCol("SELECT AttributeDefID FROM preparationattr WHERE AttributeDefID = "+ attributeDefId);
        if (ids.size() == 1)
        {
            parentTblId = PreparationAttr.getClassTableId();
            return (Integer)ids.get(0);
        }
        
        ids = BasicSQLUtils.querySingleCol("SELECT AttributeDefID FROM collectionobjectattr WHERE AttributeDefID = "+ attributeDefId);
        if (ids.size() == 1)
        {
            parentTblId = CollectionObjectAttr.getClassTableId();
            return (Integer)ids.get(0);
        }
        parentTblId = null;
        return null;
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
        return 16;
    }
}
