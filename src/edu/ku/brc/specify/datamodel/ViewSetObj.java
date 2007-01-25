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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.io.File;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.helpers.XMLHelper;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "viewsetobj")
public class ViewSetObj extends DataModelObjBase implements java.io.Serializable, AppResourceIFace 
{

    private static final Logger  log       = Logger.getLogger(ViewSetObj.class);
            
    // Fields    

     protected Long                    viewSetObjId;
     protected Short                   level;
     protected String                  name;
     protected String                  description;
     protected String                  metaData;
     protected Set<AppResourceData>    appResourceDatas;
     protected Set<AppResourceDefault> appResourceDefaults;

     // Non Persisted Fields
     protected String                    fileName     = null;
     protected Hashtable<String, String> metaDataHash = null;


    // Constructors

    /** default constructor */
    public ViewSetObj() {
        //
    }
    
    /** constructor with id */
    public ViewSetObj(Long viewSetObjId) {
        this.viewSetObjId = viewSetObjId;
    }
   
    
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        viewSetObjId = null;
        level = null;
        name = null;
        description = null;
        metaData = null;
        appResourceDefaults = new HashSet<AppResourceDefault>();
        appResourceDatas = new HashSet<AppResourceData>();
        
        fileName = null;
    }
    // End Initializer

    

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "ViewSetObjID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getViewSetObjId() {
        return this.viewSetObjId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.viewSetObjId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return ViewSetObj.class;
    }
    
    public void setViewSetObjId(Long viewSetObjId) {
        this.viewSetObjId = viewSetObjId;
    }

    /**
     * 
     */
    @Column(name = "Level", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getLevel() {
        return this.level;
    }
    
    public void setLevel(Short level) {
        this.level = level;
    }

    /**
     * 
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     */
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true)
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#getMimeType()
     */
    @Transient
    public String getMimeType() {
        return "text/xml";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AppResourceIFace#setMimeType(java.lang.String)
     */
    public void setMimeType(String mimeType) {
        throw new RuntimeException("Can't set MimeType");
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
     * Builds meta data hash. 
     */
    protected void initMetaData()
    {
        if (metaDataHash == null)
        {
            metaDataHash = new Hashtable<String, String>(); 
        }
        
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
    
    /**
     * 
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY)
    @JoinTable(name = "appresdef_viewsetobj", joinColumns = { @JoinColumn(name = "ViewSetObjID", unique = false, nullable = false, insertable = true, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "AppResourceDefaultID", unique = false, nullable = false, insertable = true, updatable = false) })
    @Cascade( { CascadeType.SAVE_UPDATE })
    public Set<AppResourceDefault> getAppResourceDefaults() {
        return this.appResourceDefaults;
    }
    
    public void setAppResourceDefaults(Set<AppResourceDefault> appResourceDefaults) {
        this.appResourceDefaults = appResourceDefaults;
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
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "viewSetObj")
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
    @Transient
    public String getDataAsString()
    {
        log.debug("********* "+getFileName()+" size:"+appResourceDatas.size());
        
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
                    log.debug("Loading File["+fileName+"]");
                    
                    File file = new File(fileName);
                    str = XMLHelper.getContents(file);
                    timestampCreated  = new Date(file.lastModified());
                    timestampModified = timestampCreated;
                    
                } else
                {
                    str = new String(blobData == null ? null : blobData.getBytes(1L, (int)blobData.length()));
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


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 86;
    }

}
