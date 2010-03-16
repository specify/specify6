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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spappresourcedir")
@org.hibernate.annotations.Table(appliesTo="spappresourcedir", indexes =
    {   @Index (name="SpAppResourceDirDispTypeIDX", columnNames={"DisciplineType"})
    })
public class SpAppResourceDir extends DataModelObjBase implements java.io.Serializable, Cloneable
{

    // Fields

     protected Integer            spAppResourceDirId;
     protected Collection         collection;
     protected Discipline         discipline;
     protected SpecifyUser        specifyUser;
     protected Set<SpAppResource> spPersistedAppResources;
     protected Set<SpViewSetObj>  spPersistedViewSets;
     protected String             userType;
     protected String             disciplineType;
     protected Boolean            isPersonal;
     
     // Transient Data Members
     protected Set<SpAppResource> spAppResources     = null;
     protected Set<SpViewSetObj>  spViewSets         = null;
     protected boolean            shouldInitialViews = true;
     
     protected String             title = null;

    // Constructors

    /** default constructor */
    public SpAppResourceDir() 
    {
        //
    }

    /** constructor with id */
    public SpAppResourceDir(Integer spAppResourceDirId) 
    {
        this.spAppResourceDirId = spAppResourceDirId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        spAppResourceDirId      = null;
        collection              = AppContextMgr.getInstance().getClassObject(Collection.class);
        discipline              = null;
        specifyUser             = null;
        
        spPersistedAppResources = new HashSet<SpAppResource>();
        spPersistedViewSets     = new HashSet<SpViewSetObj>();

        userType                = null;
        disciplineType          = null;
        
        spAppResources          = null;//new HashSet<AppResource>();
        spViewSets              = new HashSet<SpViewSetObj>();
        
        isPersonal              = false;
    }
    // End Initializer


    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "SpAppResourceDirID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpAppResourceDirId() 
    {
        return this.spAppResourceDirId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        if (collection != null)
        {
            collection.getCollectionName();
        }
        if (discipline != null)
        {
            discipline.getName();
        }
        if (specifyUser != null)
        {
            specifyUser.getName();
        }
        getSpPersistedAppResources();
        getSpPersistedViewSets();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Transient
    @Override
    public boolean isChangeNotifier()
    {
        return false;
    }
    
    /**
     * @param spAppResourceDirId
     */
    public void setSpAppResourceDirId(Integer spAppResourceDirId) 
    {
        this.spAppResourceDirId = spAppResourceDirId;
    }

    /**
     * @return
     */
    @Column(name = "DisciplineType", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getDisciplineType()
    {
        return disciplineType;
    }

    /**
     * @param disciplineType
     */
    public void setDisciplineType(String disciplineType)
    {
        this.disciplineType = disciplineType;
    }

    /**
     * @return
     */
    @Column(name = "UserType", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getUserType()
    {
        return userType;
    }

    /**
     * @param userType
     */
    public void setUserType(String userType)
    {
        this.userType = userType;
    }
    

    /**
     * @return the isPersonal
     */
    @Column(name = "IsPersonal", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getIsPersonal()
    {
        return isPersonal;
    }

    /**
     * @param isPersonal the isPersonal to set
     */
    public void setIsPersonal(Boolean isPersonal)
    {
        this.isPersonal = isPersonal;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceDefaultIFace#getCollection()
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Collection getCollection() {
        return this.collection;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceDefaultIFace#setCollection(edu.ku.brc.specify.datamodel.Collection)
     */
    public void setCollection(Collection collection) 
    {
        this.collection = collection;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceDefaultIFace#getDiscipline()
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DisciplineID", unique = false, nullable = true, insertable = true, updatable = true)
    public Discipline getDiscipline() 
    {
        return this.discipline;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceDefaultIFace#setDiscipline(edu.ku.brc.specify.datamodel.Discipline)
     */
    public void setDiscipline(Discipline discipline) 
    {
        this.discipline = discipline;
    }

    /**
     * @return
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpecifyUserID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpecifyUser getSpecifyUser() 
    {
        return this.specifyUser;
    }

    /**
     * @param specifyUser
     */
    public void setSpecifyUser(SpecifyUser specifyUser) 
    {
        this.specifyUser = specifyUser;
    }


    /**
     * @return
     */
    @OneToMany(mappedBy = "spAppResourceDir")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<SpAppResource> getSpPersistedAppResources() 
    {
        return this.spPersistedAppResources;
    }


    /**
     * @param persistedAppResources
     */
    public void setSpPersistedAppResources(Set<SpAppResource> persistedAppResources) 
    {
        this.spPersistedAppResources = persistedAppResources;
    }


    /**
     * @return
     */
    @OneToMany(mappedBy = "spAppResourceDir")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<SpViewSetObj> getSpPersistedViewSets() 
    {
        return this.spPersistedViewSets;
    }

    /**
     * @param persistedViewSets
     */
    public void setSpPersistedViewSets(Set<SpViewSetObj> persistedViewSets) 
    {
        this.spPersistedViewSets = persistedViewSets;
    }
    /**
     * @return
     */
    @Transient
    public Set<SpAppResource> getSpAppResources()
    {
        if (spAppResources == null)
        {
            spAppResources = new HashSet<SpAppResource>();
            spAppResources.addAll(getSpPersistedAppResources());
        }
        return spAppResources;
    }

    /**
     * @param spAppResources
     */
    public void setSpAppResources(Set<SpAppResource> spAppResources)
    {
        this.spAppResources = spAppResources;
    }

    /**
     * @return the list of Peristable and transient ViewSetObjs
     */
    @Transient
    public Set<SpViewSetObj> getSpViewSets()
    {
        if (spViewSets == null || shouldInitialViews)
        {
            spViewSets = new HashSet<SpViewSetObj>();
            spViewSets.addAll(getSpPersistedViewSets());
            shouldInitialViews = false;
        }
        return spViewSets;
    }

    /**
     * @param viewSets the set of ViewSets
     */
    public void setSpViewSets(Set<SpViewSetObj> viewSets)
    {
        this.spViewSets = viewSets;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the title
     */
    @Transient
    public String getTitle()
    {
        return title;
    }

    /**
     * @return a very descriptive unique identifier
     */
    @Transient
    public String getVerboseUniqueIdentifer()
    {
        if (collection == null && 
            specifyUser == null && 
            discipline == null && 
            disciplineType == null && 
            userType == null)
        {
            return "Common";
        }
        
        StringBuilder strBuf = new StringBuilder();
        strBuf.append(""+(collection != null ? collection.getCollectionName() : ""));
        strBuf.append(" "+(specifyUser != null ? specifyUser.getName() : ""));
        strBuf.append(" "+(discipline != null ? discipline.getType() : ""));
        strBuf.append(" "+(disciplineType != null ? disciplineType : ""));
        strBuf.append(" "+(userType != null ? userType : ""));
        strBuf.append(" "+isPersonal);
        return strBuf.toString(); 
    }
    
    /**
     * @param arName
     * @return
     */
    @Transient
    public SpAppResource getResourceByName(final String arName)
    {
        for (SpAppResource ar :  getSpAppResources())
        {
            if (ar.getName() != null && ar.getName().equals(arName))
            {
                return ar;
            }
        }
        return null;
    }

    /**
     * @return a unique identifier, the ID if it has one or build one if not.
     */
    @Transient
    public String getUniqueIdentifer()
    {
        return spAppResourceDirId == null ? getVerboseUniqueIdentifer() : spAppResourceDirId.toString();
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return title == null ? super.getIdentityTitle() : title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    public String toString()
    {
        return getIdentityTitle();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 516;
    }
    
    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.spAppResourceDirId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return SpAppResourceDir.class;
    }
    
    /**
     * @param appRes
     * @param checkPersistentOnly
     * @return true if this directory contains appRes.
     */
    public boolean containsResource(final SpAppResource appRes, final boolean checkPersistentOnly)
    {
        Set<SpAppResource> appResSet;
        if (checkPersistentOnly)
        {
            appResSet = getSpPersistedAppResources();
        }
        else
        {
            appResSet = getSpAppResources();
        }
        for (SpAppResource item : appResSet)
        {
            if (item.equals(appRes))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param appRes
     * 
     * Removes appRes from spPersistedAppResources and spAppResources sets.
     * 
     * @return true if resource was removed from both sets. 
     */
    public boolean removeResource(final SpAppResource appRes)
    {
        /* the removeResource(XXX) methods were added because, for reasons I am unable to determine, contrary
         * to the documentation, during calls to the remove method for the spPersistedAppResources and spAppResources the
         * SpAppResources.equals() method is NOT used to determine equality. This caused SpecifyAppContextMgr.removeResourceFromDir to
         * fail or throw an execption.
         */
        return removeResource(appRes, true) && removeResource(appRes, false);
    }
    
    /**
     * @param appRes
     * @param persistedOnly - if true remove from spPersistedAppResources else remove from spAppResources
     * @return true if appRes was removed from the specified set.
     */
    public boolean removeResource(final SpAppResource appRes, final boolean persistedOnly)
    {
        /* the removeResource(XXX) methods were added because, for reasons I am unable to determine, contrary
         * to the documentation, during calls to the remove method for the spPersistedAppResources and spAppResources the
         * SpAppResources.equals() method is NOT used to determine equality. This caused SpecifyAppContextMgr.removeResourceFromDir to
         * fail or throw an execption.
         */
        Set<SpAppResource> appResSet;
        if (persistedOnly)
        {
            appResSet = getSpPersistedAppResources();
        }
        else
        {
            appResSet = getSpAppResources();
        }
        for (SpAppResource item : appResSet)
        {
            if (item.equals(appRes))
            {
                return appResSet.remove(item);
            }
        }
        return false; 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        SpAppResourceDir dir = (SpAppResourceDir)super.clone();
        dir.spPersistedAppResources = new HashSet<SpAppResource>();
        dir.spPersistedViewSets     = new HashSet<SpViewSetObj>();
        dir.spAppResources          = new HashSet<SpAppResource>();
        dir.spViewSets              = new HashSet<SpViewSetObj>();
        
        for (SpAppResource ar : spPersistedAppResources)
        {
            dir.spPersistedAppResources.add(ar);
        }
        for (SpViewSetObj vso : spPersistedViewSets)
        {
            dir.spPersistedViewSets.add(vso);
        }
        
        for (SpAppResource ar : spAppResources)
        {
            dir.spAppResources.add(ar);
        }
        for (SpViewSetObj vso : spViewSets)
        {
            dir.spViewSets.add(vso);
        }
        return dir;
    }
    
    
}
