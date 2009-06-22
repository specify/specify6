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

import java.sql.SQLException;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.specify.tools.SpecifySchemaGenerator;

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
        /*DBConnection dbConn = DBConnection.getInstance();//getITConnection();
        if (dbConn != null)
        {
            DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
            if (dbMgr.connect(dbConn.getUserName(), dbConn.getPassword(), dbConn.getServerName(), dbConn.getDatabaseName()))
            {
                // Here checks to see if this is the first ever
                if (!dbMgr.doesDBHaveTable("spversion"))
                {
                    DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver(dbConn.getDriverName());
                    try
                    {
                        dbConn = getITConnection();
                        if (dbConn != null)
                        {
                            SpecifySchemaGenerator.generateSchema(driverInfo, dbConn.getServerName(), dbConn.getDatabaseName(), dbConn.getUserName(), dbConn.getPassword());
                        }
                        
                    } catch (SQLException ex)
                    {
                        ex.printStackTrace();
                    }
                }
                dbMgr.close();
            }
        }*/
        return false;
    }

}
