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

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
//@org.hibernate.annotations.Proxy(lazy = false)
@Table(name="collectiontype")
@org.hibernate.annotations.Table(appliesTo="collectiontype", indexes =
    {   @Index (name="ColTypeNameIDX", columnNames={"Name"}),
        @Index (name="DisciplineIDX", columnNames={"Discipline"})
    })
public class CollectionType extends DataModelObjBase implements java.io.Serializable, Comparable<CollectionType>
{

    protected static CollectionType currentCollectionType = null;
    
    // Fields

    protected Integer                   collectionTypeId;
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
    protected LithoStratTreeDef         lithoStratTreeDef;
    protected Set<Locality>             localities;
    protected Set<SpAppResourceDir>     spAppResourceDirs;
    protected Set<UserPermission>       userPermissions;
    protected Division                  division;
    
    protected Set<SpLocaleContainer>    spLocaleContainers;
     

    // Constructors

    /** default constructor */
    public CollectionType() {
        //
    }

    /** constructor with id */
    public CollectionType(Integer collectionTypeId) {
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
        lithoStratTreeDef = null;
        localities = new HashSet<Locality>();
        spAppResourceDirs = new HashSet<SpAppResourceDir>();
        spLocaleContainers    = new HashSet<SpLocaleContainer>();
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name="CollectionTypeID", unique=false, nullable=false, insertable=true, updatable=true)
    public Integer getCollectionTypeId() {
        return this.collectionTypeId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
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

    public void setCollectionTypeId(Integer collectionTypeId) {
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
    @ManyToOne( fetch = FetchType.LAZY )
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL })
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
    @ManyToOne( fetch = FetchType.LAZY )
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL })
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
    @ManyToOne( fetch = FetchType.LAZY )
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL })
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
     *      * @hibernate.many-to-one
     */
    @ManyToOne( fetch = FetchType.LAZY )
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL })
    @JoinColumn(name="LithoStratTreeDefID")
    public LithoStratTreeDef getLithoStratTreeDef() {
        return this.lithoStratTreeDef;
    }

    public void setLithoStratTreeDef(LithoStratTreeDef lithoStratTreeDef) {
        this.lithoStratTreeDef = lithoStratTreeDef;
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
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpAppResourceDir> getSpAppResourceDirs()
    {
        return spAppResourceDirs;
    }

    public void setSpAppResourceDirs(Set<SpAppResourceDir> spAppResourceDirs)
    {
        this.spAppResourceDirs = spAppResourceDirs;
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
    

    /**
     * @return the localeContainers
     */
    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="collectionType")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<SpLocaleContainer> getSpLocaleContainers()
    {
        return spLocaleContainers;
    }

    /**
     * @param localeContainers the localeContainers to set
     */
    public void setSpLocaleContainers(Set<SpLocaleContainer> spLocaleContainers)
    {
        this.spLocaleContainers = spLocaleContainers;
    }

    /**
     * @return the division
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DivisionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Division getDivision()
    {
        return division;
    }

    /**
     * @param division the division to set
     */
    public void setDivision(Division division)
    {
        this.division = division;
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
    
    //----------------------------------------------------------------------
    //-- Comparable Interface
    //----------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CollectionType obj)
    {
        if (name != null && obj != null && StringUtils.isNotEmpty(obj.name))
        {
            return name.compareTo(obj.name);
        }
        if (discipline != null && obj != null && StringUtils.isNotEmpty(obj.discipline))
        {
            return discipline.compareTo(obj.discipline);
        }
        // else
        return timestampCreated.compareTo(obj.timestampCreated);
    }
}
