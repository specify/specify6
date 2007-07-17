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

import edu.ku.brc.ui.forms.FormDataObjIFace;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name="collectiontype")
public class CollectionType extends DataModelObjBase implements java.io.Serializable 
{

    protected static CollectionType currentCollectionType = null;
    
    // Fields

    protected Long                      collectionTypeId;
    protected String                    name;
    protected String                    discipline;
    protected DataType                  dataType;
    protected Set<Collection>           collections;
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
    public CollectionType() {
        //
    }

    /** constructor with id */
    public CollectionType(Long collectionTypeId) {
        this.collectionTypeId = collectionTypeId;
    }

    public static CollectionType getCurrentCollectionType()
    {
        return currentCollectionType;
    }

    public static void setCurrentCollectionType(CollectionType currentCollectionType)
    {
        CollectionType.currentCollectionType = currentCollectionType;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        collectionTypeId = null;
        name = null;
        discipline = null;
        dataType = null;
        userPermissions = null;
        collections = new HashSet<Collection>();
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
    @Column(name="CollectionTypeID", unique=false, nullable=false, insertable=true, updatable=true)
    public Long getCollectionTypeId() {
        return this.collectionTypeId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.collectionTypeId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CollectionType.class;
    }

    public void setCollectionTypeId(Long collectionTypeId) {
        this.collectionTypeId = collectionTypeId;
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
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="collectionType")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<Collection> getCollections() {
        return this.collections;
    }

    public void setCollections(Set<Collection> collections) {
        this.collections = collections;
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
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="collectionType")
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
    @OneToOne(cascade={})
    @Cascade( {CascadeType.ALL} )
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
    @ManyToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="collectionTypes")
    @Cascade( {CascadeType.SAVE_UPDATE} )
    public Set<Locality> getLocalities() {
        return this.localities;
    }

    public void setLocalities(Set<Locality> localities) {
        this.localities = localities;
    } 

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="collectionType")
    public Set<AppResourceDefault> getAppResourceDefaults()
    {
        return appResourceDefaults;
    }

    public void setAppResourceDefaults(Set<AppResourceDefault> appResourceDefaults)
    {
        this.appResourceDefaults = appResourceDefaults;
    }
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="collectionType")
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
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#addReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void addReference(FormDataObjIFace ref, String refType)
    {
        if (ref instanceof Collection)
        {
            this.collections.add((Collection)ref);
            ((Collection)ref).setCollectionType(this);
            
        } else if (ref instanceof AttributeDef)
        {
            this.attributeDefs.add((AttributeDef)ref);
            ((AttributeDef)ref).setCollectionType(this);

        } else if (ref instanceof Locality)
        {
            this.localities.add((Locality)ref);
            ((Locality)ref).getCollectionTypes().add(this);
            
        } else if (ref instanceof UserPermission)
        {
            userPermissions.add((UserPermission)ref);
            ((UserPermission)ref).setCollectionType(this);

        } else
        {
            throw new RuntimeException("Adding Object ["+ref.getClass().getSimpleName()+"] and the refType is null.");
        }
    }
    // Done Add Methods

    // Delete Methods

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#removeReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void removeReference(FormDataObjIFace ref, String refType)
    {
        if (ref instanceof Collection)
        {
            collections.remove(ref);
            ((Collection)ref).setCollectionType(null);
                
        } else if (ref instanceof AttributeDef)
        {
            attributeDefs.remove(ref);
            ((AttributeDef)ref).setCollectionType(null);
            
        } else if (ref instanceof Locality)
        {
            localities.remove(ref);
            ((Locality)ref).setCollectionTypes(null);
            
        } else if (ref instanceof UserPermission)
        {
            this.userPermissions.remove(ref);
            ((UserPermission)ref).setCollectionType(null);
            
        } else
        {
            throw new RuntimeException("Removing Object ["+ref.getClass().getSimpleName()+"] and the refType is null.");
        }
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
