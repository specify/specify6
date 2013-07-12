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
package edu.ku.brc.specify.web;

import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Vector;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * May 12, 2009
 *
 */
public class WebMemberDAO
{
    public static final int WM_IS_NEW        = 1;
    public static final int WM_REFRESHING_DB = 2;
    public static final int WM_BUILDING_EXT  = 4; // building aggregated DB
    public static final int WM_AVAILABLE     = 8; 
    
    protected static SimpleDateFormat                       dateTimeFormatter      = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static Timestamp                              now                    = new Timestamp(System .currentTimeMillis());
    protected static String                                 nowStr                 = dateTimeFormatter.format(now);

    
    private Connection connection;
    
    /**
     * @param conn
     */
    public WebMemberDAO(final Connection conn)
    {
        super();
        connection = conn;
    }
    
    /**
     * @param serviceNumber
     * @return
     */
    public String getDBNameForServiceNum(final String serviceNumber)
    {
        Vector<Object[]> row = BasicSQLUtils.query(connection, "SELECT DatabaseName FROM webmember WHERE ServiceNumber = '"+serviceNumber+"'");
        if (row != null && row.size() > 0)
        {
            return row.get(0)[0].toString();
        }
        return null;
    }
    
    /**
     * @param name
     * @param dbName
     * @param serviceNumber
     * @return
     */
    public boolean addWebMember(final String name, 
                                final String dbName, 
                                final String serviceNumber)
    {
        String sql = "INSERT INTO webmember (TimestampCreated, Name, DatabaseName, ServiceNumber, Status) VALUES ('%s', '%s', '%s', '%s', %d)";
        String sqlStr = String.format(sql, nowStr, name, dbName, serviceNumber, WM_IS_NEW);
        return BasicSQLUtils.update(connection, sqlStr) == 1;
    }
    
    /**
     * @param dbName
     * @return
     */
    public int getStatusForDB(final String dbName)
    {
        Integer status = BasicSQLUtils.getCount(connection, "SELECT Status FROM webmember WHERE DatabaseName = '"+dbName+"'");
        return status == null ? 0 : status;
    }

    /**
     * @return
     */
    public boolean createTable()
    {
        boolean dropRV = BasicSQLUtils.update(connection, "DROP TABLE IF EXISTS `webmember`") == 1;
        System.out.println("********************** dropRV " +dropRV);
        
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `webmember` (\n");
        sb.append(" `WebMemberID` int(11) NOT NULL auto_increment,\n");
        sb.append(" `TimestampCreated` datetime NOT NULL,\n");
        sb.append(" `Name` varchar(64) default NULL,\n");
        sb.append(" `DatabaseName` varchar(64) default NULL,\n");
        sb.append(" `ServiceNumber` varchar(64) default NULL,\n");
        sb.append(" `Status` int(11) default NULL,\n");
        sb.append(" PRIMARY KEY  (`WebMemberID`)\n");
        sb.append(" ) ENGINE=InnoDB DEFAULT CHARSET=latin1;\n");
        
        System.out.println(sb.toString());
        return BasicSQLUtils.update(connection, sb.toString()) == 1;
    }
}
