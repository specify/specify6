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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import edu.ku.brc.specify.config.DisciplineType;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
//@org.hibernate.annotations.Proxy(lazy = false)
@Table(name="discipline")
@org.hibernate.annotations.Table(appliesTo="discipline", indexes =
    {   @Index (name="DisciplineNameIDX", columnNames={"Name"})
    })
public class Discipline extends DataModelObjBase implements java.io.Serializable, Comparable<Discipline>
{

    protected static Discipline currentDiscipline = null;
    
    // Fields

    protected Integer                   disciplineId;
    protected String                    name;
    protected String                    title;
    protected DataType                  dataType;
    protected Set<Collection>           collections;
    protected Set<AttributeDef>         attributeDefs;
    protected GeographyTreeDef          geographyTreeDef;
    protected GeologicTimePeriodTreeDef geologicTimePeriodTreeDef;
    protected StorageTreeDef            storageTreeDef;
    protected TaxonTreeDef              taxonTreeDef;
    protected LithoStratTreeDef         lithoStratTreeDef;
    protected Set<Locality>             localities;
    protected Set<SpAppResourceDir>     spAppResourceDirs;
    //protected Set<UserPermission>       userPermissions;
    protected Set<Agent>                agents;
    protected Division                  division;
    
    protected Set<DeterminationStatus>  determinationStatuss;
    
    protected Set<SpLocaleContainer>    spLocaleContainers;
     

    // Constructors

    /** default constructor */
    public Discipline() {
        //
    }

    /** constructor with id */
    public Discipline(Integer disciplineId) {
        this.disciplineId = disciplineId;
    }

    /**
     * @return
     */
    public static Discipline getCurrentDiscipline()
    {
        return currentDiscipline;
    }

    /**
     * @param currentDiscipline
     */
    public static void setCurrentDiscipline(Discipline discipline)
    {
        Discipline.currentDiscipline = discipline;
    }
    
    /**
     * Returns true if the the disciplineType matches the current one
     * @param disciplineArg the one in question
     * @return true if the the disciplineType matches the current one
     */
    public static boolean isCurrentDiscipline(final DisciplineType.STD_DISCIPLINES disciplineArg)
    {
        if (Discipline.currentDiscipline != null)
        {
            String disciplineName = Discipline.currentDiscipline.getName();
            return StringUtils.isNotEmpty(disciplineName) && disciplineName.equals(disciplineArg.toString());
        }
        return false;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        disciplineId          = null;
        name                  = null;
        title                 = null;
        dataType              = null;
        //userPermissions       = null;
        collections           = new HashSet<Collection>();
        attributeDefs         = new HashSet<AttributeDef>();
        geographyTreeDef      = null;
        geologicTimePeriodTreeDef = null;
        storageTreeDef       = null;
        taxonTreeDef          = null;
        lithoStratTreeDef     = null;
        determinationStatuss  = new HashSet<DeterminationStatus>();
        localities            = new HashSet<Locality>();
        spAppResourceDirs     = new HashSet<SpAppResourceDir>();
        spLocaleContainers    = new HashSet<SpLocaleContainer>();
        agents                = new HashSet<Agent>();
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name="DisciplineID")
    public Integer getDisciplineId() {
        return this.disciplineId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.disciplineId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Discipline.class;
    }

    public void setDisciplineId(Integer disciplineId) {
        this.disciplineId = disciplineId;
    }

    /**
     *
     */
    @Column(name="Title", length=64)
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
    *
    */
    @Column(name="Name", length=64)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     *
     */
    @ManyToOne
    @JoinColumn(name="DataTypeID", nullable=false)
    public DataType getDataType() {
        return this.dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    /**
     *
     */
    @OneToMany(mappedBy="discipline")
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
    @OneToMany(mappedBy="discipline")
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
    @Cascade( {CascadeType.ALL} )
    @JoinColumn(name="GeographyTreeDefID", nullable=false)
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
    @Cascade( {CascadeType.ALL} )
    @JoinColumn(name="GeologicTimePeriodTreeDefID", nullable=false)
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
    @Cascade( {CascadeType.ALL} )
    @JoinColumn(name="StorageTreeDefID")
    public StorageTreeDef getStorageTreeDef() {
        return this.storageTreeDef;
    }

    public void setStorageTreeDef(StorageTreeDef storageTreeDef) {
        this.storageTreeDef = storageTreeDef;
    }

    /**
     *      * @hibernate.one-to-one
     */
    @OneToOne
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
    @Cascade( {CascadeType.ALL} )
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
    @OneToMany(mappedBy="discipline")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<Locality> getLocalities() 
    {
        return this.localities;
    }

    public void setLocalities(Set<Locality> localities) 
    {
        this.localities = localities;
    } 

    /**
     * @return the determinationStatuss
     */
    @OneToMany(mappedBy="discipline")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<DeterminationStatus> getDeterminationStatuss()
    {
        return determinationStatuss;
    }

    /**
     * @param determinationStatuss the determinationStatuss to set
     */
    public void setDeterminationStatuss(Set<DeterminationStatus> determinationStatuss)
    {
        this.determinationStatuss = determinationStatuss;
    }
    
    @OneToMany(mappedBy="discipline")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<SpAppResourceDir> getSpAppResourceDirs()
    {
        return spAppResourceDirs;
    }

    public void setSpAppResourceDirs(Set<SpAppResourceDir> spAppResourceDirs)
    {
        this.spAppResourceDirs = spAppResourceDirs;
    }
    
//    @OneToMany(mappedBy="discipline")
//    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
//    public Set<UserPermission> getUserPermissions() 
//    {
//        return this.userPermissions;
//    }
//    
//    public void setUserPermissions(Set<UserPermission> userPermissions) 
//    {
//        this.userPermissions = userPermissions;
//    }
    

    /**
     * @return the localeContainers
     */
    @OneToMany(mappedBy="discipline")
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
     * @return the agents
     */
    @ManyToMany(mappedBy="disciplines")
    @Cascade( {CascadeType.SAVE_UPDATE} )
    public Set<Agent> getAgents()
    {
        return agents;
    }

    /**
     * @param agents the agents to set
     */
    public void setAgents(Set<Agent> agents)
    {
        this.agents = agents;
    }

    /**
     * @return the division
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DivisionID", nullable = false)
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
        //buffer.append("name").append("='").append(getName()).append("' ");
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
        return name != null ? name : super.getIdentityTitle();
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
    public int compareTo(Discipline obj)
    {
        if (title != null && obj != null && StringUtils.isNotEmpty(obj.title))
        {
            return title.compareTo(obj.title);
        }
        
        if (name != null && obj != null && StringUtils.isNotEmpty(obj.name))
        {
            return name.compareTo(obj.name);
        }
        // else
        return timestampCreated.compareTo(obj.timestampCreated);
    }
    
    /**
     * @param defName
     * @return TreeDef from table named defName.
     * 
     * This is needed by the uploader and the query builder.
     */
    @Transient
    public TreeDefIface<?,?,?> getTreeDef(final String defName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        Method getter = Discipline.class.getMethod("get" + defName, (Class<?>[])null);
        return (TreeDefIface<?,?,?>)getter.invoke(this,  (Object[])null);
    }

}
