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
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.helpers.XMLHelper;




/**

 */
public class AppResource extends DataModelObjBase implements java.io.Serializable, AppResourceIFace 
{
    private static final Logger  log       = Logger.getLogger(AppResource.class);
    
    // Fields    

     protected Long                    appResourceId;
     protected Short                   level;
     protected String                  name;
     protected String                  description;
     protected String                  mimeType;
     protected String                  metaData;
     protected Set<AppResourceData>    appResourceDatas;
     protected Set<AppResourceDefault> appResourceDefaults;
     
     // Non Persisted Fields
     protected String                  fileName = null;

    // Constructors

    /** default constructor */
    public AppResource() 
    {
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
        appResourceId = null;
        level = null;
        name = null;
        description = null;
        mimeType = null;
        metaData = null;
        timestampCreated = new Date();
        timestampModified = null;
        lastEditedBy = null;
        appResourceDefaults = new HashSet<AppResourceDefault>();
        appResourceDatas = new HashSet<AppResourceData>();
        
        fileName = null;
    }
    // End Initializer

    

    // Property accessors

    /**
     * 
     */
    public Long getAppResourceId() {
        return this.appResourceId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.appResourceId;
    }
    
    public void setAppResourceId(Long appResourceId) {
        this.appResourceId = appResourceId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#getLevel()
     */
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
    public String getMimeType() {
        return this.mimeType;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#setMimeType(java.lang.String)
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMetaData()
    {
        return metaData;
    }

    public void setMetaData(String metaData)
    {
        this.metaData = metaData;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#getAppResourceDefaults()
     */
    public Set<AppResourceDefault> getAppResourceDefaults() {
        return this.appResourceDefaults;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#setAppResourceDefaults(java.util.Set)
     */
    public void setAppResourceDefaults(Set<AppResourceDefault> appResourceDefaults) {
        this.appResourceDefaults = appResourceDefaults;
    }

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
        if (fileName != null)
        {
            throw new RuntimeException("Not implemented!");
        }
        
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

            ard.setData(Hibernate.createBlob(dataStr.getBytes()));


        } else if (appResourceDatas.size() > 0)
        {
            appResourceDatas.iterator().next().setData(null);
        }
        
        setAppResourceDatas(appResourceDatas); // Must call this to make sure it knows we changed it
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppResourceIFace#getDataAsString()
     */
    public String getDataAsString()
    {
        getAppResourceDatas(); // Must call this before accessing it as a local data member
        
        try
        {
            AppResourceData ard = null;
            Blob blobData = null;
            if (appResourceDatas.size() > 0)
            {
                ard = appResourceDatas.iterator().next();
                if (ard != null)
                {
                    blobData = ard.getData();
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
            
            if (str == null && blobData != null && blobData.length() > 0)
            {
                str = new String(blobData.getBytes(1L, (int)blobData.length()));
            }
            
            if (str != null && str.length() > 0)
            {
               return StringEscapeUtils.unescapeXml(str);
            }

        } catch (SQLException ex)
        {
            log.error(ex);
        }
        return null;
    }


    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 83;
    }



}
