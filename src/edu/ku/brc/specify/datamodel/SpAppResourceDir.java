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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spappresourcedir")
@org.hibernate.annotations.Table(appliesTo="spappresourcedir", indexes =
    {   @Index (name="SpAppResourceDirDispTypeIDX", columnNames={"DisciplineType"})
    })
public class SpAppResourceDir extends DataModelObjBase implements java.io.Serializable
{

    // Fields

     protected Integer            spAppResourceDirId;
     protected Collection         collection;
     protected CollectionType     collectionType;
     protected SpecifyUser        specifyUser;
     protected Set<SpAppResource> spPersistedAppResources;
     protected Set<SpViewSetObj>  spPersistedViewSets;
     protected String             userType;
     protected String             disciplineType;
     
     // Transient Data Member
     protected Set<SpAppResource> spAppResources     = null;
     protected Set<SpViewSetObj>  spViewSets         = null;
     protected boolean            shouldInitialViews = true;

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
        collection              = null;
        collectionType          = null;
        specifyUser             = null;
        
        spPersistedAppResources = new HashSet<SpAppResource>();
        spPersistedViewSets     = new HashSet<SpViewSetObj>();

        userType                = null;
        disciplineType          = null;
        
        spAppResources          = null;//new HashSet<AppResource>();
        spViewSets              = new HashSet<SpViewSetObj>();
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
     * @see edu.ku.brc.specify.datamodel.AppResourceDefaultIFace#getCollectionType()
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionTypeID", unique = false, nullable = true, insertable = true, updatable = true)
    public CollectionType getCollectionType() 
    {
        return this.collectionType;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceDefaultIFace#setCollectionType(edu.ku.brc.specify.datamodel.CollectionType)
     */
    public void setCollectionType(CollectionType collectionType) 
    {
        this.collectionType = collectionType;
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
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spAppResourceDirs")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
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
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spAppResourceDirs")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
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
     * @return a very descriptive unique identifier
     */
    @Transient
    public String getVerboseUniqueIdentifer()
    {
        StringBuilder strBuf = new StringBuilder();
        strBuf.append(""+(collection != null ? collection.getCollectionName() : ""));
        strBuf.append(" "+(specifyUser != null ? specifyUser.getName() : ""));
        strBuf.append(" "+(collectionType != null ? collectionType.getName() : ""));
        strBuf.append(" "+(disciplineType != null ? disciplineType : ""));
        strBuf.append(" "+(userType != null ? userType : ""));
        return strBuf.toString(); 
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
    
    /**
     * @return the Table ID for the class.
     */
    /**
     * @return
     */
    public static int getClassTableId()
    {
        return 516;
    }
}
