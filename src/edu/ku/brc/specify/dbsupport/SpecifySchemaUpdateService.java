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
package edu.ku.brc.specify.dbsupport;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.dom4j.Element;
import org.jfree.util.Log;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * May 18, 2009
 *
 */
public class SpecifySchemaUpdateService extends SchemaUpdateService
{

    /**
     * 
     */
    public SpecifySchemaUpdateService()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SchemaUpdateService#updateSchema(java.lang.String)
     */
    @Override
    public boolean updateSchema(final String versionNumber)
    {
        String  dbVersion = null;
        
        Element root;
        try
        {
            root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath("schema_version.xml")));//$NON-NLS-1$
            if (root != null)
            {
                Element dbElement = (Element)root;//.selectObject("version");
                if (dbElement != null)
                {
                    dbVersion = dbElement.getTextTrim();
                }
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            
        } catch (Exception e)
        {
            e.printStackTrace();
        } 
        
        
        DBConnection dbConn = DBConnection.getInstance();
        if (dbConn != null)
        {
            DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
            if (dbMgr.connect(dbConn.getUserName(), dbConn.getPassword(), dbConn.getServerName(), dbConn.getDatabaseName()))
            {
                // Here checks to see if this is the first ever
                boolean doUpdate      = false;
                boolean doInsert      = false;
                String  appVersion    = null;
                String  schemaVersion = null;
                Integer spverId       = null;
                Integer versionNum    = 1;
                
                if (dbMgr.doesDBHaveTable("spversion"))
                {
                    Vector<Object[]> rows = BasicSQLUtils.query(dbConn.getConnection(), "SELECT AppVersion, SchemaVersion, SpVersionID, Version FROM spversion");
                    if (rows.size() == 1)
                    {
                        Object[] row  = (Object[])rows.get(0);
                        appVersion    = row[0].toString();
                        schemaVersion = row[1].toString();
                        spverId       = (Integer)row[2];
                        versionNum    = (Integer)row[3];
                        
                        if (appVersion == null && schemaVersion == null)
                        {
                            doUpdate = true;
                            
                        } else if (appVersion.equals(versionNumber) || (schemaVersion.equals(dbVersion) && dbVersion != null))
                        {
                            doUpdate = true;
                        }
                        
                    }
                } else
                {
                    doInsert = true;
                }
                
                SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Timestamp        now               = new Timestamp(System .currentTimeMillis());

                try
                {
                    if (doUpdate || doInsert)
                    {
                        Pair<String, String> usrPwd = getITConnection();
                        if (dbConn != null)
                        {
                            DBConnection dbc = DBConnection.getInstance();
    
                            SpecifySchemaGenerator.updateSchema(DatabaseDriverInfo.getDriver(dbc.getDriver()), dbc.getServerName(), dbc.getDatabaseName(), usrPwd.first,usrPwd.second);
                        }
                        
                        if (doInsert || (appVersion == null && schemaVersion == null))
                        {
                            String sql = "INSERT INTO spversion (AppName, AppVersion, SchemaVersion, TimestampCreated, TimestampModified, Version) VALUES('Specify', '"+versionNumber+"', '"+dbVersion+"', '" + 
                                                                 dateTimeFormatter.format(now) + "', '" + dateTimeFormatter.format(now) + "', "+versionNum+")";
                            System.err.println(sql);
                            BasicSQLUtils.update(dbConn.getConnection(), sql);
                            
                        } else if (appVersion.equals(versionNumber) || (schemaVersion.equals(dbVersion) && dbVersion != null))
                        {
                            versionNum++;
                            String sql = "UPDATE spversion SET AppVersion='"+versionNumber+"', SchemaVersion='"+dbVersion+"', Version="+versionNum+" WHERE SpVersionID = "+ spverId;
                            System.err.println(sql);
                            BasicSQLUtils.update(dbConn.getConnection(), sql);
                        }
                        return true;
                    }
                    
                } catch (SQLException e)
                {
                    e.printStackTrace();
                    
                } finally
                {
                    dbMgr.close();
                }
            }
        }
        return false;
    }

}
