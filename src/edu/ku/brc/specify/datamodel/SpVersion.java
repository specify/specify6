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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spversion")
public class SpVersion extends DataModelObjBase implements java.io.Serializable 
{

    // Fields

    protected Integer           spVersionId;
    protected String            appName;
    protected String            appVersion;
    protected String            schemaVersion;

    // Constructors

    /** default constructor */
    public SpVersion() 
    {
        //
    }

    /** constructor with id */
    public SpVersion(Integer VersionId) 
    {
        this.spVersionId = VersionId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        spVersionId    = null;
        appName        = null;
        appVersion     = null;
        schemaVersion  = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * PrimaryKey
     */
    @Id
    @GeneratedValue
    @Column(name = "SpVersionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpVersionId() 
    {
        return this.spVersionId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.spVersionId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return SpVersion.class;
    }

    public void setSpVersionId(Integer VersionId) 
    {
        this.spVersionId = VersionId;
    }

    /**
     *
     */
    @Column(name = "AppName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getAppName() 
    {
        return this.appName;
    }

    public void setAppName(String appName) 
    {
        this.appName = appName;
    }

    /**
     * @return the appVersion
     */
    @Column(name = "AppVersion", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getAppVersion()
    {
        return appVersion;
    }

    /**
     * @param appVersion the appVersion to set
     */
    public void setAppVersion(String appVersion)
    {
        this.appVersion = appVersion;
    }

    /**
     * @return the schemaVersion
     */
    @Column(name = "SchemaVersion", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getSchemaVersion()
    {
        return schemaVersion;
    }

    /**
     * @param schemaVersion the schemaVersion to set
     */
    public void setSchemaVersion(String schemaVersion)
    {
        this.schemaVersion = schemaVersion;
    }


    @Override
    @Transient
    public String getIdentityTitle()
    {
        StringBuilder sb = new StringBuilder();
        
        Address.append(sb, appName);

        if (sb.length() > 0)
        {
            return sb.toString();
        }
        return super.getIdentityTitle();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Transient
    @Override
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 529;
    }

}
