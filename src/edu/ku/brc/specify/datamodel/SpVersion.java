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

import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

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
    
    protected Boolean           isDBClosed;
    protected String            dbClosedBy;

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
        
        isDBClosed     = null;
        dbClosedBy     = null;
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


    /**
     * @return the isDBClosed
     */
    @Column(name = "IsDBClosed", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsDBClosed()
    {
        return isDBClosed != null && isDBClosed;
    }

    /**
     * @param isDBClosed the isDBClosed to set
     */
    public void setIsDBClosed(Boolean isDBClosed)
    {
        this.isDBClosed = isDBClosed;
    }

    /**
     * @return the dbClosedBy
     */
    @Column(name = "DbClosedBy", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getDbClosedBy()
    {
        return dbClosedBy;
    }

    /**
     * @param dbClosedBy the dbClosedBy to set
     */
    public void setDbClosedBy(String dbClosedBy)
    {
        this.dbClosedBy = dbClosedBy;
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
    
    /**
     * @param conn
     * @param appVerNum
     * @param dbVersion
     * @return
     */
    private static boolean createInitialRecordInternal(final Connection conn, final String appVerNum, final String dbVersion)
    {
        // Create Version Record
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Timestamp        now               = new Timestamp(System.currentTimeMillis());

        String sql = "INSERT INTO spversion (AppName, AppVersion, SchemaVersion, TimestampCreated, TimestampModified, Version) VALUES('Specify', '"+appVerNum+"', '"+dbVersion+"', '" + 
                     dateTimeFormatter.format(now) + "', '" + dateTimeFormatter.format(now) + "', 1)";
        
        return BasicSQLUtils.update(conn, sql) == 1;
    }
    
    /**
     * @param conn
     * @param appVerNum
     * @param dbVersion
     * @return
     */
    public static boolean createInitialRecord(final Connection conn, final String appVerNum, final String dbVersion)
    {
        int numRecords = BasicSQLUtils.getNumRecords(conn, "spversion");
        if (numRecords == 0)
        {
            return createInitialRecordInternal(conn, appVerNum, dbVersion);
        }
        
        int recVerNum = 1;
        int spverId   = 1;
        Vector<Object[]> row = BasicSQLUtils.query(conn, "SELECT SpVersionID, Version FROM spversion ORDER BY SpVersionID ASC");
        if (row != null && row.size() > 0)
        {
            Object[] r = row.get(0);
            spverId   = (Integer)r[0];
            recVerNum = ((Integer)r[1]) + 1;
            BasicSQLUtils.update(conn, "DELETE FROM spversion WHERE SpVersionID > " + spverId);
        } else
        {
            return createInitialRecordInternal(conn, appVerNum, dbVersion); // fail safe, should never happen
        }
        
        return updateRecord(conn, appVerNum, dbVersion, recVerNum, spverId);
    }

    /**
     * @param conn
     * @param appVerNum
     * @param dbVersion
     * @param recVerNum
     * @param spverId
     * @return
     */
    public static boolean updateRecord(final Connection conn, final String appVerNum, final String dbVersion, final int recVerNum, final int spverId)
    {
        // Create Version Record

        String sql = "UPDATE spversion SET AppVersion='"+appVerNum+"', SchemaVersion='"+dbVersion+"', Version="+recVerNum+" WHERE SpVersionID = "+ spverId;
        
        return BasicSQLUtils.update(conn, sql) == 1;
        
    }

    /**
     * The original release of 6.3.00 had a bug that set the SchemaVersion version to the AppVersion number.
     * @param conn the current connection
     * @return true on success
     */
    public static boolean fixSchemaNumber(final DBConnection dbConn)
    {
        DBMSUserMgr mgr  = DBMSUserMgr.getInstance();
        Connection  conn = dbConn.createConnection();
        mgr.setConnection(conn);
        if (mgr.doesDBHaveTable("spversion"))
        {
            String sql = "UPDATE spversion SET SchemaVersion='1.5' WHERE SchemaVersion ='6.3.00'";
            return BasicSQLUtils.update(conn, sql) == 1;
        }
        return true;
    }

}
