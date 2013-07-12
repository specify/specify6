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

import java.io.File;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Properties;
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * Clone does NOT clone the spAppResourceDirs hashSet, but it DOES clone the spAppResourceDatas HashSet
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spappresource")
@org.hibernate.annotations.Table(appliesTo="spappresource", indexes =
    {   @Index (name="SpAppResNameIDX", columnNames={"Name"}),
        @Index (name="SpAppResMimeTypeIDX", columnNames={"MimeType"})
    })
public class SpAppResource extends DataModelObjBase implements java.io.Serializable, AppResourceIFace, Cloneable, Comparable<SpAppResource>
{
    private static final Logger  log = Logger.getLogger(SpAppResource.class);
    
    // Fields    

     protected Integer                   spAppResourceId;
     protected Short                     level;
     protected String                    name;
     protected String                    description;
     protected String                    mimeType;
     protected String                    metaData;
     protected Integer                   ownerPermissionLevel;
     protected Integer                   groupPermissionLevel;
     protected Integer                   allPermissionLevel;
     protected Set<SpAppResourceData>    spAppResourceDatas;
     protected SpAppResourceDir          spAppResourceDir;
     protected Set<SpReport>             spReports;
     protected SpecifyUser               specifyUser;
     protected SpPrincipal               group;
     
     // Non Persisted Fields
     protected String                    fileName     = null;
     protected Properties                metaDataHash = null;

    // Constructors

    /** default constructor */
    public SpAppResource() 
    {
        //
    }
    
    /** constructor with id */
    public SpAppResource(Integer spAppResourceId) 
    {
        this.spAppResourceId = spAppResourceId;
    }
   
    
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        spAppResourceId      = null;
        level                = null;
        name                 = null;
        description          = null;
        mimeType             = null;
        metaData             = null;
        ownerPermissionLevel = null;
        groupPermissionLevel = null;
        allPermissionLevel   = null;
        spAppResourceDir     = null;
        spAppResourceDatas   = new HashSet<SpAppResourceData>();
        spReports            = new HashSet<SpReport>();
        specifyUser          = null;
        group                = null;       
        fileName             = null;
    }
    // End Initializer

    

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "SpAppResourceID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpAppResourceId() {
        return this.spAppResourceId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.spAppResourceId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return SpAppResource.class;
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
    
    public void setSpAppResourceId(Integer spAppResourceId) {
        this.spAppResourceId = spAppResourceId;
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
        
        return metaDataHash != null ? metaDataHash.getProperty(attr) : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#setMetaData(java.lang.String)
     */
    public void setMetaData(String metaData)
    {
        metaDataHash = null;
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
    public Properties getMetaDataMap()
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
            metaDataHash = new Properties(); 
            
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


    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "appResource")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpReport> getSpReports()
    {
        return spReports;
    }

    public void setSpReports(Set<SpReport> spReports)
    {
        this.spReports = spReports;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#getSpAppResourceDirs()
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpAppResourceDirID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpAppResourceDir getSpAppResourceDir() 
    {
        return this.spAppResourceDir;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#setSpAppResourceDirs(java.util.Set)
     */
    public void setSpAppResourceDir(SpAppResourceDir spAppResourceDir) 
    {
        this.spAppResourceDir = spAppResourceDir;
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
    @JoinColumn(name = "SpPrincipalID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpPrincipal getGroup() {
        return this.group;
    }
    
    public void setGroup(SpPrincipal group) {
        this.group = group;
    }
    
    /**
     * @return
     */
    @Transient
    public String getFileName()
    {
        return fileName == null ? name : fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    
    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spAppResource")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<SpAppResourceData> getSpAppResourceDatas() 
    {
        return spAppResourceDatas;
    }
    
    public void setSpAppResourceDatas(Set<SpAppResourceData> spAppResourceDatas) 
    {
        this.spAppResourceDatas = spAppResourceDatas;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#setDataAsString(java.lang.String)
     */
    public void setDataAsString(final String dataStr)
    {
        DataProviderSessionIFace session = null;
        try
        {
            if (getId() != null)
            {
                session = DataProviderFactory.getInstance().createSession();
                session.attach(this);
            }
            
            setDataStr(dataStr);
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpAppResource.class, ex);
           log.error(ex);
           ex.printStackTrace();
           
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        //setSpAppResourceDatas(spAppResourceDatas); // Must call this to make sure it knows we changed it
    }
    
    /**
     * Sets a string as the data and does NOT save it to the database.
     * @param dataStr the string data
     */
    public void setDataStr(final String dataStr)
    {
        if (StringUtils.isNotEmpty(dataStr))
        {
            SpAppResourceData ard;
            if (spAppResourceDatas.size() == 0)
            {
                ard = new SpAppResourceData();
                ard.initialize();
                ard.setSpAppResource(this);
                spAppResourceDatas.add(ard);
                
            } else
            {
                ard = spAppResourceDatas.iterator().next();
            }

            ard.setData(dataStr.getBytes());

        } else if (spAppResourceDatas.size() > 0)
        {
            spAppResourceDatas.iterator().next().setData(null);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#getDataAsString()
     */
    @Transient
    public String getDataAsString()
    {
        return getDataAsString(null);
    }

    /**
     * Gets the contents with a possible existing session.
     * @param sessionArg the current session or null.
     * @return the contents as a string
     */
    @Transient
    public String getDataAsString(final DataProviderSessionIFace sessionArg)
    {
        SpAppResourceData        appResData = null;
        DataProviderSessionIFace session    = null;
        try
        {
            if (spAppResourceId != null)
            {
                session = sessionArg != null ? sessionArg : DataProviderFactory.getInstance().createSession();
                session.attach(this);
            }
            
            if (spAppResourceDatas.size() > 0)
            {
                appResData = spAppResourceDatas.iterator().next();
                if (appResData != null)
                {
                    return new String(appResData.getData());
                }
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpAppResource.class, ex);
           log.error(ex);
           ex.printStackTrace();
           
        } finally
        {
            if (sessionArg == null && session != null)
            {
                session.close();
            }
        }
        
        String fileNameToOpen = fileName;
        boolean doesFileExist = false;
        if (StringUtils.isNotEmpty(fileNameToOpen))
        {
            File file = new File(fileNameToOpen);
            if (!file.exists())
            {
                String fName = FilenameUtils.getName(fileNameToOpen);
                String path = FilenameUtils.getFullPathNoEndSeparator(fileNameToOpen);
                path = path.substring(0, FilenameUtils.indexOfLastSeparator(path));
                
                fileNameToOpen = path + File.separator + fName;
                
                doesFileExist = (new File(fileNameToOpen)).exists();
            } else
            {
                doesFileExist = true; 
            }
        }
            
        String str  = null;
        if (doesFileExist)
        {
            File file = new File(fileNameToOpen);
            str = XMLHelper.getContents(file);
            timestampCreated  = new Timestamp(file.lastModified());
            //timestampModified = timestampCreated;
        } else
        {
            UIRegistry.showError("The file in the app_resources.xml ["+fileName+"] is missing.");
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
        return 514;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return description;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        SpAppResource obj = (SpAppResource)super.clone();
        
        obj.spAppResourceId      = null;
        obj.spAppResourceDatas   = new HashSet<SpAppResourceData>();
        
        return obj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTimestampModified()
     */
    @Override
    @Transient
    public Timestamp getTimestampModified()
    {
        if (spAppResourceId == null && StringUtils.isNotEmpty(fileName))
        {
            return new Timestamp(new File(fileName).lastModified());
        }
        return super.getTimestampModified();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!getClass().equals(obj.getClass()))
        {
            return false;
        }
        SpAppResource app = (SpAppResource)obj;
        if (!(getId() != null && app.getId() != null))
        {
            return false;
        }
        int id1 = getId();
        int id2 = app.getId();
        return id1 == id2;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(SpAppResource o)
    {
        return name.compareToIgnoreCase(o.name);
    }
    
    
}
