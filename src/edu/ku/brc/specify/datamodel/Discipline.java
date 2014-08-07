/* Copyright (C) 2013, University of Kansas Center for Research
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
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

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.config.DisciplineType;

/**

 */
@Entity
//@org.hibernate.annotations.Proxy(lazy = false)
@Table(name="discipline")
@org.hibernate.annotations.Table(appliesTo="discipline", indexes =
    {   @Index (name="DisciplineNameIDX", columnNames={"Name"})
    })
@SuppressWarnings("serial")
public class Discipline extends UserGroupScope implements java.io.Serializable, Comparable<Discipline>
{
    // Fields
    protected String                    type;
    protected String                    name;
    protected DataType                  dataType;
    protected String                    regNumber;
    protected Set<Collection>           collections;
    protected Set<AttributeDef>         attributeDefs;
    protected GeographyTreeDef          geographyTreeDef;
    protected GeologicTimePeriodTreeDef geologicTimePeriodTreeDef;
    protected TaxonTreeDef              taxonTreeDef;
    protected LithoStratTreeDef         lithoStratTreeDef;
    protected String                     paleoContextChildTable;
    protected Boolean					 isPaleoContextEmbedded;
    //protected Set<Locality>             localities;
    //protected Set<SpAppResourceDir>     spAppResourceDirs;
    //protected Set<UserPermission>       userPermissions;
    protected Division                  division;
    
    //protected Set<DeterminationStatus>  determinationStatuss;
    
    protected Set<SpLocaleContainer>    spLocaleContainers;
    protected Set<SpExportSchema>       spExportSchemas;  // Zero or One
    protected Set<AutoNumberingScheme>  numberingSchemes;

    // Constructors

    /** default constructor */
    public Discipline() {
        //
    }

    /** constructor with id */
    public Discipline(Integer disciplineId) {
        super(disciplineId);
    }

    /**
     * Returns true if the the disciplineType matches the current one
     * @param disciplineArg the one in question
     * @return true if the the disciplineType matches the current one
     */
    public static boolean isCurrentDiscipline(final DisciplineType.STD_DISCIPLINES disciplineArg)
    {
        Discipline current = AppContextMgr.getInstance().getClassObject(Discipline.class);
        if (current != null)
        {
            String disciplineName = current.getType();
            return StringUtils.isNotEmpty(disciplineName) && disciplineName.equals(disciplineArg.toString());
        }
        return false;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        type                  = null;
        name                  = null;
        dataType              = null;
        regNumber             = null;
        
        //userPermissions       = null;
        collections           = new HashSet<Collection>();
        attributeDefs         = new HashSet<AttributeDef>();
        geographyTreeDef      = null;
        geologicTimePeriodTreeDef = null;
        taxonTreeDef          = null;
        lithoStratTreeDef     = null;
        paleoContextChildTable = "CollectingEvent";
        isPaleoContextEmbedded = false;
        //determinationStatuss  = new HashSet<DeterminationStatus>();
        //localities            = new HashSet<Locality>();
        //spAppResourceDirs     = new HashSet<SpAppResourceDir>();
        spLocaleContainers    = new HashSet<SpLocaleContainer>();
        spExportSchemas       = new HashSet<SpExportSchema>();
        numberingSchemes      = new HashSet<AutoNumberingScheme>();
     }
    // End Initializer

    // Property accessors

    /**
     *
     */
    public Integer getDisciplineId() {
        return getUserGroupScopeId();
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return getUserGroupScopeId();
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
    	setUserGroupScopeId(disciplineId);
    }

    /**
     *
     */
    @Column(name="Name", length=64)
    public String getName() {
        return this.name;
    }

    public void setName(String title) {
        this.name = title;
    }

    /**
    *
    */
    @Column(name="Type", length=64)
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name="DataTypeID", nullable=false)
    public DataType getDataType() {
        return this.dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }
    
    /**
     * @return the isRegistered
     */
    @Column(name = "RegNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public String getRegNumber()
    {
        return regNumber;
    }

    /**
     * @param isRegistered the isRegistered to set
     */
    public void setRegNumber(String regNumber)
    {
        this.regNumber = regNumber;
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
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    @JoinColumn(name="GeographyTreeDefID", nullable=false)
    public GeographyTreeDef getGeographyTreeDef() 
    {
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
     * @return the isPaleoContextEmbedded
     */
    @Column(name = "IsPaleoContextEmbedded", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getIsPaleoContextEmbedded()
    {
        return isPaleoContextEmbedded;
    }

    /**
     * @param isPaleoContextEmbedded the isPaleoContextEmbedded to set
     */
    public void setIsPaleoContextEmbedded(Boolean isPaleoContextEmbedded)
    {
        this.isPaleoContextEmbedded = isPaleoContextEmbedded;
    }

    /**
     * @return
     */
    @Column(name = "PaleoContextChildTable", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getPaleoContextChildTable() {
    	return paleoContextChildTable;
    }
    
    /**
     * @param paleoContextChildTable
     */
    public void setPaleoContextChildTable(String paleoContextChildTable) {
    	this.paleoContextChildTable = paleoContextChildTable;
    }

    /*
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
    */
    
//    /**
//     * @return the determinationStatuss
//     */
//    @OneToMany(mappedBy="discipline")
//    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
//    public Set<DeterminationStatus> getDeterminationStatuss()
//    {
//        return determinationStatuss;
//    }

    /**
     * @param determinationStatuss the determinationStatuss to set
     */
//    public void setDeterminationStatuss(Set<DeterminationStatus> determinationStatuss)
//    {
//        this.determinationStatuss = determinationStatuss;
//    }
    
    /*@OneToMany(mappedBy="discipline")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<SpAppResourceDir> getSpAppResourceDirs()
    {
        return spAppResourceDirs;
    }

    public void setSpAppResourceDirs(Set<SpAppResourceDir> spAppResourceDirs)
    {
        this.spAppResourceDirs = spAppResourceDirs;
    }*/
    
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
     * @return the division
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DivisionID", nullable = false)
    public Division getDivision()
    {
        return division;
    }

    /**
     * @return the spExportSchemas
     */
    @OneToMany(mappedBy="discipline")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<SpExportSchema> getSpExportSchemas()
    {
        return spExportSchemas;
    }

    /**
     * @param spExportSchemas the spExportSchemas to set
     */
    public void setSpExportSchemas(Set<SpExportSchema> spExportSchemas)
    {
        this.spExportSchemas = spExportSchemas;
    }
    
    /**
     * @return the numberingSchemes
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY)
    @JoinTable(name = "autonumsch_dsp", 
            joinColumns = { @JoinColumn(name = "DisciplineID", unique = false, nullable = false, insertable = true, updatable = false) }, 
            inverseJoinColumns = { @JoinColumn(name = "AutoNumberingSchemeID", unique = false, nullable = false, insertable = true, updatable = false) })
    public Set<AutoNumberingScheme> getNumberingSchemes()
    {
        return numberingSchemes;
    }

    /**
     * @param numberingSchemes the numberingSchemes to set
     */
    public void setNumberingSchemes(Set<AutoNumberingScheme> numberingSchemes)
    {
        this.numberingSchemes = numberingSchemes;
    }

    /**
     * @param division the division to set
     */
    public void setDivision(Division division)
    {
        this.division = division;
    }
    
	/**
     * Asks the Object to force load and child object. This must be done within a Session. 
     */
    @Override
    public void forceLoad()
    {
        //getDeterminationStatuss().size(); // make sure they are loaded
        
        for (AutoNumberingScheme ans : numberingSchemes) // Force Load of Numbering Schemes
        {
            ans.getTableNumber();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        /*StringBuffer buffer = new StringBuffer(128);

        buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
        //buffer.append("name").append("='").append(getName()).append("' ");
        buffer.append("]");

        return buffer.toString();*/
        return StringUtils.isNotEmpty(name) ? name :  type;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Division.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return division != null ? division.getId() : null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return toString();
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
        if (name != null && obj != null && StringUtils.isNotEmpty(obj.name))
        {
            return name.compareTo(obj.name);
        }
        
        if (type != null && obj != null && StringUtils.isNotEmpty(obj.type))
        {
            return type.compareTo(obj.type);
        }
        // else
        return timestampCreated != null && obj != null && obj.timestampCreated != null ? timestampCreated.compareTo(obj.timestampCreated) : 0;
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
