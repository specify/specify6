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
public class ViewSetObj  implements java.io.Serializable, AppResourceIFace 
{

    private static final Logger  log       = Logger.getLogger(ViewSetObj.class);
            
    // Fields    

     protected Long viewSetObjId;
     protected Short level;
     protected String name;
     protected String description;
     protected String metaData;
     protected Set<AppResourceData> appResourceDatas;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     private Set<AppResourceDefault> appResourceDefaults;

     // Non Persisted Fields
     protected String fileName = null;


    // Constructors

    /** default constructor */
    public ViewSetObj() {
    }
    
    /** constructor with id */
    public ViewSetObj(Long viewSetObjId) {
        this.viewSetObjId = viewSetObjId;
    }
   
    
    // Initializer
    public void initialize()
    {
        viewSetObjId = null;
        level = null;
        name = null;
        description = null;
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
    public Long getViewSetObjId() {
        return this.viewSetObjId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.viewSetObjId;
    }
    
    public void setViewSetObjId(Long viewSetObjId) {
        this.viewSetObjId = viewSetObjId;
    }

    /**
     * 
     */
    public Short getLevel() {
        return this.level;
    }
    
    public void setLevel(Short level) {
        this.level = level;
    }

    /**
     * 
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     */
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#getMimeType()
     */
    public String getMimeType() {
        return "text/xml";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#setMimeType(java.lang.String)
     */
    public void setMimeType(String mimeType) {
        throw new RuntimeException("Can't set MimeType");
    }
    
    /**
     * 
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     * 
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     *      
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    public String getMetaData()
    {
        return metaData;
    }

    public void setMetaData(String metaData)
    {
        this.metaData = metaData;
    }
    
    /**
     * 
     */
    public Set<AppResourceDefault> getAppResourceDefaults() {
        return this.appResourceDefaults;
    }
    
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
                ard.setViewSetObj(this);
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
        log.info("********* "+getFileName()+" size:"+appResourceDatas.size());
        
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
            
            
            if ((blobData != null && blobData.length() > 0) || 
                StringUtils.isNotEmpty(fileName))
            {
                String str;
                if (StringUtils.isNotEmpty(fileName))
                {
                    File file = new File(fileName);
                    str = XMLHelper.getContents(file);
                    timestampCreated  = new Date(file.lastModified());
                    timestampModified = timestampCreated;
                    
                } else
                {
                    str = new String(blobData.getBytes(1L, (int)blobData.length()));
                }
                
                if (str.length() > 0)
                {
                   return StringEscapeUtils.unescapeXml(str);
                }
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

}
