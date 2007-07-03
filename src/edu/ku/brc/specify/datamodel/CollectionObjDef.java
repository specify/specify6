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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name="collectionobjdef")
public class CollectionObjDef extends DataModelObjBase implements java.io.Serializable 
{

    protected static CollectionObjDef currentCollectionObjDef = null;
    
    // Fields

    protected Long                      collectionObjDefId;
    protected String                    name;
    protected String                    discipline;
    protected DataType                  dataType;
    protected Set<Collection>           collection;
    protected SpecifyUser               specifyUser;
    protected Set<AttributeDef>         attributeDefs;
    protected GeographyTreeDef          geographyTreeDef;
    protected GeologicTimePeriodTreeDef geologicTimePeriodTreeDef;
    protected LocationTreeDef           locationTreeDef;
    protected TaxonTreeDef              taxonTreeDef;
    protected Set<Locality>             localities;
    protected Set<AppResourceDefault>   appResourceDefaults;
    protected Set<UserPermission>       userPermissions;
     

    // Constructors

    /** default constructor */
    public CollectionObjDef() {
        //
    }

    /** constructor with id */
    public CollectionObjDef(Long collectionObjDefId) {
        this.collectionObjDefId = collectionObjDefId;
    }

    public static CollectionObjDef getCurrentCollectionObjDef()
    {
        return currentCollectionObjDef;
    }

    public static void setCurrentCollectionObjDef(CollectionObjDef currentCollectionObjDef)
    {
        CollectionObjDef.currentCollectionObjDef = currentCollectionObjDef;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        collectionObjDefId = null;
        name = null;
        discipline = null;
        dataType = null;
        userPermissions = null;
        collection = new HashSet<Collection>();
        specifyUser = null;
        attributeDefs = new HashSet<AttributeDef>();
        geographyTreeDef = null;
        geologicTimePeriodTreeDef = null;
        locationTreeDef = null;
        taxonTreeDef = null;
        localities = new HashSet<Locality>();
        appResourceDefaults = new HashSet<AppResourceDefault>();
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name="CollectionObjDefID", unique=false, nullable=false, insertable=true, updatable=true)
    public Long getCollectionObjDefId() {
        return this.collectionObjDefId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.collectionObjDefId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CollectionObjDef.class;
    }

    public void setCollectionObjDefId(Long collectionObjDefId) {
        this.collectionObjDefId = collectionObjDefId;
    }

    /**
     *
     */
    @Column(name="Name", unique=false, nullable=true, insertable=true, updatable=true, length=64)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
    *
    */
    @Column(name="Discipline", unique=false, nullable=true, insertable=true, updatable=true, length=64)
    public String getDiscipline()
    {
        return discipline;
    }

    public void setDiscipline(String discipline)
    {
        this.discipline = discipline;
    }

    /**
     *
     */
    @ManyToOne
    @Cascade( {CascadeType.SAVE_UPDATE} )
    @JoinColumn(name="DataTypeID", unique=false, nullable=false, insertable=true, updatable=true)
    public DataType getDataType() {
        return this.dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    /**
     *
     */
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="collectionObjDef")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<Collection> getCollection() {
        return this.collection;
    }

    public void setCollection(Set<Collection> collection) {
        this.collection = collection;
    }

    /**
     *
     */
    @ManyToOne
    @Cascade( {CascadeType.SAVE_UPDATE} )
    @JoinColumn(name="SpecifyUserID", unique=false, nullable=false, insertable=true, updatable=true)
    public SpecifyUser getSpecifyUser() {
        return this.specifyUser;
    }

    public void setSpecifyUser(SpecifyUser specifyUser) {
        this.specifyUser = specifyUser;
    }

    /**
     *
     */
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="collectionObjDef")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<AttributeDef> getAttributeDefs() {
        return this.attributeDefs;
    }

    public void setAttributeDefs(Set<AttributeDef> attributeDefs) {
        this.attributeDefs = attributeDefs;
    }

    /**
     *
     */
    @ManyToOne
    @Cascade( {CascadeType.SAVE_UPDATE} )
    @JoinColumn(name="GeographyTreeDefID", unique=false, nullable=true, insertable=true, updatable=true)
    public GeographyTreeDef getGeographyTreeDef() {
        return this.geographyTreeDef;
    }

    public void setGeographyTreeDef(GeographyTreeDef geographyTreeDef) {
        this.geographyTreeDef = geographyTreeDef;
    }

    /**
     *
     */
    @ManyToOne
    @Cascade( {CascadeType.SAVE_UPDATE} )
    @JoinColumn(name="GeologicTimePeriodTreeDefID", unique=false, nullable=false, insertable=true, updatable=true)
    public GeologicTimePeriodTreeDef getGeologicTimePeriodTreeDef() {
        return this.geologicTimePeriodTreeDef;
    }

    public void setGeologicTimePeriodTreeDef(GeologicTimePeriodTreeDef geologicTimePeriodTreeDef) {
        this.geologicTimePeriodTreeDef = geologicTimePeriodTreeDef;
    }

    /**
     *
     */
    @ManyToOne
    @Cascade( {CascadeType.SAVE_UPDATE} )
    @JoinColumn(name="LocationTreeDefID", unique=false, nullable=true, insertable=true, updatable=true)
    public LocationTreeDef getLocationTreeDef() {
        return this.locationTreeDef;
    }

    public void setLocationTreeDef(LocationTreeDef locationTreeDef) {
        this.locationTreeDef = locationTreeDef;
    }

    /**
     *      * @hibernate.one-to-one
     */
    @OneToOne
    @JoinColumn(name="TaxonTreeDefID")
    public TaxonTreeDef getTaxonTreeDef() {
        return this.taxonTreeDef;
    }

    public void setTaxonTreeDef(TaxonTreeDef taxonTreeDef) {
        this.taxonTreeDef = taxonTreeDef;
    }

    /**
     *
     */
    @ManyToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="collectionObjDefs")
    @Cascade( {CascadeType.SAVE_UPDATE} )
    public Set<Locality> getLocalities() {
        return this.localities;
    }

    public void setLocalities(Set<Locality> localities) {
        this.localities = localities;
    } 

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="collectionObjDef")
    public Set<AppResourceDefault> getAppResourceDefaults()
    {
        return appResourceDefaults;
    }

    public void setAppResourceDefaults(Set<AppResourceDefault> appResourceDefaults)
    {
        this.appResourceDefaults = appResourceDefaults;
    }
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="collectionObjDef")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<UserPermission> getUserPermissions() 
    {
        return this.userPermissions;
    }
    
    public void setUserPermissions(Set<UserPermission> userPermissions) 
    {
        this.userPermissions = userPermissions;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer(128);

        buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
        buffer.append("name").append("='").append(getName()).append("' ");
        buffer.append("]");

        return buffer.toString();
    }




    // Add Methods

    public void addCollection(final Collection collectionArg)
    {
        this.collection.add(collectionArg);
        collectionArg.setCollectionObjDef(this);
    }

    public void addAttributeDefs(final AttributeDef attributeDef)
    {
        this.attributeDefs.add(attributeDef);
        attributeDef.setCollectionObjDef(this);
    }

    public void addLocalities(final Locality localitiesArg)
    {
        this.localities.add(localitiesArg);
        localitiesArg.getCollectionObjDefs().add(this);
    }
    
    public void addUserPermission(final UserPermission userPermission)
    {
        this.userPermissions.add(userPermission);
        userPermission.setCollectionObjDef(this);
    }
    // Done Add Methods

    // Delete Methods

    public void removeCollection(final Collection collectionArg)
    {
        this.collection.remove(collectionArg);
        collectionArg.setCollectionObjDef(null);
    }

    public void removeAttributeDefs(final AttributeDef attributeDef)
    {
        this.attributeDefs.remove(attributeDef);
        attributeDef.setCollectionObjDef(null);
    }
    public void removeLocalities(final Locality localitiesArg)
    {
        this.localities.remove(localitiesArg);
        localitiesArg.getCollectionObjDefs().remove(this);
    }
    public void removeUserPermission(final UserPermission userPermission)
    {
        this.userPermissions.remove(userPermission);
        userPermission.setCollectionObjDef(null);
    }
    // Delete Add Methods
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        if (name != null) return name;
        return super.getIdentityTitle();
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
        return 26;
    }
}
