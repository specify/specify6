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

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.helpers.XMLHelper;




/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "appresource")
public class AppResource extends DataModelObjBase implements java.io.Serializable, AppResourceIFace 
{
    //private static final Logger  log       = Logger.getLogger(AppResource.class);
    
    // Fields    

     protected Long                    appResourceId;
     protected Short                   level;
     protected String                  name;
     protected String                  description;
     protected String                  mimeType;
     protected String                  metaData;
     protected Integer                 ownerPermissionLevel;
     protected Integer                 groupPermissionLevel;
     protected Integer                 allPermissionLevel;
     protected Set<AppResourceData>    appResourceDatas;
     protected Set<AppResourceDefault> appResourceDefaults;
     protected SpecifyUser             specifyUser;
     protected UserGroup               group;
     
     // Non Persisted Fields
     protected String                    fileName     = null;
     protected Hashtable<String, String> metaDataHash = null;

    // Constructors

    /** default constructor */
    public AppResource() 
    {
        //
    }
    
    /** constructor with id */
    public AppResource(Long appResourceId) 
    {
        this.appResourceId = appResourceId;
    }
   
    
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        appResourceId       = null;
        level               = null;
        name                = null;
        description         = null;
        mimeType            = null;
        metaData            = null;
        ownerPermissionLevel = null;
        groupPermissionLevel = null;
        allPermissionLevel = null;
        appResourceDefaults = new HashSet<AppResourceDefault>();
        appResourceDatas    = new HashSet<AppResourceData>();
        specifyUser = null;
        group = null;       
        fileName = null;
    }
    // End Initializer

    

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "AppResourceID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getAppResourceId() {
        return this.appResourceId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.appResourceId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return AppResource.class;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isIndexable()
     */
    @Transient
    @Override
    public boolean isIndexable()
    {
        return false;
    }
    
    public void setAppResourceId(Long appResourceId) {
        this.appResourceId = appResourceId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#getLevel()
     */
    @Column(name = "Level", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getLevel() {
        return this.level;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#setLevel(java.lang.Short)
     */
    public void setLevel(Short level) {
        this.level = level;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#getName()
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName() {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#getDescription()
     */
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true)
    public String getDescription() {
        return this.description;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#getMimeType()
     */
    @Column(name = "MimeType", unique = false, nullable = true, insertable = true, updatable = true)
    public String getMimeType() {
        return this.mimeType;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#setMimeType(java.lang.String)
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#getMetaData()
     */
    @Column(name = "MetaData", unique = false, nullable = true, insertable = true, updatable = true)
    public String getMetaData()
    {
        return metaData;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#getMetaData(java.lang.String)
     */
    public String getMetaData(final String attr)
    {
        initMetaData();
        
        return metaDataHash != null ? metaDataHash.get(attr) : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#setMetaData(java.lang.String)
     */
    public void setMetaData(String metaData)
    {
        if (StringUtils.isNotEmpty(this.metaData) && metaDataHash != null)
        {
            metaDataHash.clear();
        }
        this.metaData = metaData;
    }
    
    /**
     * 
     */
    @Column(name = "OwnerPermissionLevel", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getOwnerPermissionLevel() {
        return this.ownerPermissionLevel;
    }
    
    public void setOwnerPermissionLevel(Integer ownerPermissionLevel) {
        this.ownerPermissionLevel = ownerPermissionLevel;
    }  
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#getMetaDataMap()
     */
    @Transient
    public Map<String, String> getMetaDataMap()
    {
        initMetaData();
        
        return metaDataHash;
    }

    /**
     * 
     */
    @Column(name = "GroupPermissionLevel", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getGroupPermissionLevel() {
        return this.groupPermissionLevel;
    }
    
    public void setGroupPermissionLevel(Integer groupPermissionLevel) {
        this.groupPermissionLevel = groupPermissionLevel;
    }

    /**
     * 
     */
    @Column(name = "AllPermissionLevel", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getAllPermissionLevel() {
        return this.allPermissionLevel;
    }
    
    public void setAllPermissionLevel(Integer allPermissionLevel) {
        this.allPermissionLevel = allPermissionLevel;
    }

    /**
     * Builds meta data hash. 
     */
    protected void initMetaData()
    {
        if (metaDataHash == null)
        {
            metaDataHash = new Hashtable<String, String>(); 
            
            if (StringUtils.isNotEmpty(metaData))
            {
                for (String pair : metaData.split(";"))
                {
                    if (StringUtils.isNotEmpty(pair))
                    {
                        String[] tokens = pair.split("=");
                        if (tokens != null && tokens.length == 2)
                        {
                            metaDataHash.put(tokens[0], tokens[1]);
                        }
                    }
                }
            }
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#getAppResourceDefaults()
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY)
    @JoinTable(name = "appresdef_appres", joinColumns = { @JoinColumn(name = "AppResourceID", unique = false, nullable = false, insertable = true, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "AppResourceDefaultID", unique = false, nullable = false, insertable = true, updatable = false) })
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<AppResourceDefault> getAppResourceDefaults() {
        return this.appResourceDefaults;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#setAppResourceDefaults(java.util.Set)
     */
    public void setAppResourceDefaults(Set<AppResourceDefault> appResourceDefaults) {
        this.appResourceDefaults = appResourceDefaults;
    }
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpecifyUserID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpecifyUser getSpecifyUser() {
        return this.specifyUser;
    }
    /**
     * 
     */
    public void setSpecifyUser(SpecifyUser owner) {
        this.specifyUser = owner;
    }
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "UserGroupID", unique = false, nullable = true, insertable = true, updatable = true)
    public UserGroup getGroup() {
        return this.group;
    }
    
    public void setGroup(UserGroup group) {
        this.group = group;
    }
    @Transient
    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    
    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "appResource")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AppResourceData> getAppResourceDatas() {
        return appResourceDatas;
    }
    
    public void setAppResourceDatas(Set<AppResourceData> appResourceDatas) {
        this.appResourceDatas = appResourceDatas;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#setDataAsString(java.lang.String)
     */
    public void setDataAsString(final String dataStr)
    {
        //if (fileName != null)
        //{
        //    //throw new RuntimeException("Not implemented!");
        //}
        
        if (StringUtils.isNotEmpty(dataStr))
        {
            AppResourceData ard;
            if (appResourceDatas.size() == 0)
            {
                ard = new AppResourceData();
                ard.initialize();
                ard.setAppResource(this);
                appResourceDatas.add(ard);
                
            } else
            {
                ard = appResourceDatas.iterator().next();
            }

            ard.setData(dataStr.getBytes());


        } else if (appResourceDatas.size() > 0)
        {
            appResourceDatas.iterator().next().setData(null);
        }
        
        //setAppResourceDatas(appResourceDatas); // Must call this to make sure it knows we changed it
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#getDataAsString()
     */
    @Transient
    public String getDataAsString()
    {
        getAppResourceDatas(); // Must call this before accessing it as a local data member
        
        AppResourceData ard = null;
        if (appResourceDatas.size() > 0)
        {
            ard = appResourceDatas.iterator().next();
            if (ard != null)
            {
                return new String(ard.getData());
            }
        }
        
        String str = null;
        if (StringUtils.isNotEmpty(fileName))
        {
            File file = new File(fileName);
            str = XMLHelper.getContents(file);
            timestampCreated  = new Date(file.lastModified());
            timestampModified = timestampCreated;
        }

        if (str != null && str.length() > 0)
        {
           return StringEscapeUtils.unescapeXml(str);
        }

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
        return 83;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return description;
    }

}
