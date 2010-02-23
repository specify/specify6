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
package edu.ku.brc.specify.conversion;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.jtds.jdbc.ClobImpl;

import org.apache.log4j.Logger;

import com.mysql.jdbc.CommunicationsException;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * A set of basic utilities that used almost exclusively for converting old Database schemas to the new schema
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
public class BasicSQLUtils
{
    protected static final Logger           log               = Logger.getLogger(BasicSQLUtils.class);
    
    protected static TableWriter tblWriter = null;
    
    public static enum SERVERTYPE {MySQL, MS_SQLServer}
    public static SERVERTYPE myDestinationServerType = SERVERTYPE.MySQL;
    public static SERVERTYPE mySourceServerType = SERVERTYPE.MySQL;
    
    // These are the configuration Options for a View
    public static final int HIDE_ALL_ERRORS         =  0;  // Hhow no errors (Silent)
    public static final int SHOW_NAME_MAPPING_ERROR =  1;  // Show Errors when mapping from Old Name to New Name
    public static final int SHOW_VAL_MAPPING_ERROR  =  2;  // Show Errors when mapping from Old Name to New Name
    public static final int SHOW_NULL_FK            =  4;  // Show Error When a Foreign Key is Null and shouldn't be
    public static final int SHOW_FK_LOOKUP          =  8;  // Show Error when Foreign Key is not null but couldn't be mapped to a new value
    public static final int SHOW_NULL_PM            = 16;  // Show Error When a Primary Key is Null and shouldn't be
    public static final int SHOW_PM_LOOKUP          = 32;  // Show Error when Primary Key is not null but couldn't be mapped to a new value
    public static final int SHOW_COPY_TABLE         = 64;  // Show Errors during copy table
    public static final int SHOW_ALL                = SHOW_NAME_MAPPING_ERROR | SHOW_VAL_MAPPING_ERROR | SHOW_FK_LOOKUP | SHOW_NULL_FK | SHOW_NULL_PM | SHOW_PM_LOOKUP;
    
    protected static    int showErrors           = SHOW_ALL;
    
    
    protected static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static SimpleDateFormat dateFormatter     = new SimpleDateFormat("yyyy-MM-dd");
    protected static Calendar         calendar          = new GregorianCalendar();
    protected static Timestamp        now               = new Timestamp(System .currentTimeMillis());
    protected static String           nowStr            = dateTimeFormatter.format(now);
    
    protected static Map<String, String> ignoreMappingFieldNames = null;
    protected static Map<String, String> ignoreMappingFieldIDs   = null;
    protected static Map<String, String> oneToOneIDHash          = null;
    
    // A map used to map a New Column name to an object that can either get or convert the value.
    protected static Hashtable<String, BasicSQLUtilsMapValueIFace> columnValueMapper = new Hashtable<String, BasicSQLUtilsMapValueIFace>();
    
    protected static Connection    dbConn = null;  // (it may be shared so don't close)
    protected static ProgressFrame frame = null;
    protected static boolean       ignoreMySQLduplicates = true;
    protected static boolean       skipTrackExceptions   = false;
    
    protected static Pair<String, String> datePair = new Pair<String, String>();
    
    // Missing Mapping File
    protected static PrintWriter missingPW;
    
    static
    {
        try
        {
            missingPW = new PrintWriter("missing.txt");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Singleton
     */
    protected  BasicSQLUtils()
    {
        
    }

    public static int getShowErrors()
    {
        return showErrors;
    }

    public static void setShowErrors(final int showErrors)
    {
        BasicSQLUtils.showErrors = showErrors;
    }

    /**
     * @param skipTrackExceptions the skipTrackExceptions to set
     */
    public static void setSkipTrackExceptions(boolean skipTrackExceptions)
    {
        BasicSQLUtils.skipTrackExceptions = skipTrackExceptions;
    }
    
    /**
     * @return the skipTrackExceptions
     */
    public static boolean isSkipTrackExceptions()
    {
        return skipTrackExceptions;
    }
    
    /**
     * Sets the SQL connection
     * @param connection the SQL Connection
     */
    public static void setDBConnection(final Connection connection)
    {
        dbConn = connection;
    }

    /**
     * Sets a UI feedback frame.
     * @param frame the frame
     */
    public static void setFrame(final ProgressFrame frame)
    {
        BasicSQLUtils.frame = frame;
    }
    
    /**
     * Sets min to max.
     * @param min min
     * @param max max
     */
    public static void setProcess(final int min, final int max)
    {
        if (frame != null)
        {
            frame.setProcess(min, max);
        }
    }
    
    /**
     * Sets the value.
     * @param value the value
     */
    public static void setProcess(final int value)
    {
        if (frame != null)
        {
            frame.setProcess(value);
        }
    }
    
    
    public static TableWriter getTblWriter() 
    {
        return tblWriter;
    }

    public static void setTblWriter(TableWriter tblWriter) 
    {
        BasicSQLUtils.tblWriter = tblWriter;
    }

    /**
     * @param oneToOneIDHash the oneToOneIDHash to set
     */
    public static void setOneToOneIDHash(Map<String, String> oneToOneIDHash)
    {
        BasicSQLUtils.oneToOneIDHash = oneToOneIDHash;
    }
    
    public static void clearValueMapper()
    {
        columnValueMapper.clear();
    }
    
    public static void addToValueMapper(final String newFieldName, final BasicSQLUtilsMapValueIFace mapper)
    {
        columnValueMapper.put(newFieldName, mapper);
    }

    /**
     * Creates or clears and fills a list
     * @param fieldNames the list of names, can be null then the list is cleared and nulled out
     * @param ignoreMap the map to be be crated or cleared and nulled
     * @return the same map or a newly created one
     */
    protected static Map<String, String> configureIgnoreMap(final String[] fieldNames, final Map<String, String> ignoreMap)
    {
        Map<String, String> ignoreMapLocal = ignoreMap;
        if (fieldNames == null)
        {
            if (ignoreMapLocal != null)
            {
                ignoreMapLocal.clear();
                ignoreMapLocal = null;
            }
        } else
        {
            if (ignoreMapLocal == null)
            {
                ignoreMapLocal  = UIHelper.createMap();
            } else
            {
                ignoreMapLocal.clear();
            }
            //log.info("Ignore these Field Names when mapping:");
            for (String name : fieldNames)
            {
                ignoreMapLocal.put(name, "X");
                //log.info(name);
            }
        }
        return ignoreMapLocal;
    }

    /**
     * Sets a list of field names to ignore when mapping database tables from new names to old names
     * @param fieldNames the list of names to ignore
     */
    public static void setFieldsToIgnoreWhenMappingNames(final String[] fieldNames)
    {
        ignoreMappingFieldNames = configureIgnoreMap(fieldNames, ignoreMappingFieldNames);
    }

    /**
     * @return whether there are any ignore Fields
     */
    public static boolean hasIgnoreFields()
    {
        return ignoreMappingFieldNames != null;
    }
    /**
     * Sets a list of field names to ignore when mapping IDs
     * @param fieldNames the list of names to ignore
     */
    public static void setFieldsToIgnoreWhenMappingIDs(final String[] fieldNames)
    {
        ignoreMappingFieldIDs = configureIgnoreMap(fieldNames, ignoreMappingFieldIDs);
    }

//    /**
//     * Executes an SQL Update command
//     * @param stmt Statement object to execute the SQL
//     * @param cmdStr the SQL string to execute
//     * @return the return code from the executeUpdate call
//     */
//    public static int exeUpdateCmd(Connection conn, String cmdStr)
//    {
//        try
//        {   log.debug("---- exeUpdateCmd (PS)" + cmdStr);
//            PreparedStatement pstmt = conn.prepareStatement(cmdStr);
//            
//            return pstmt.executeUpdate();
//
//        } 
////        catch (java.sql.SQLException ex)
////        {
////            //e.printStackTrace();
////            ex.getMessage()
////            log.error(ex.getStackTrace().toString());
////            log.error(cmdStr+"\n");
////            ex.printStackTrace();
////            throw new RuntimeException(ex);      
////        }
//        catch (Exception ex)
//        {
//            //TODO: Problem encountered with the CUPaleo database when converting the AccessionAgent 
//            //We (Rod?) need to go in an create a hashtable that
//            if (ex instanceof MySQLIntegrityConstraintViolationException)
//            {
//                log.error("ignoring a record because it makes a MySQLIntegrityConstraintViolation: " + ex.getStackTrace().toString() );
//                return 0;
//            }
//            log.error(ex.getStackTrace().toString());
//            log.error(cmdStr+"\n");
//            ex.printStackTrace();
//            throw new RuntimeException(ex);
//        }
//        //return -1;
//    }
    
    /**
     * Executes an SQL Update command
     * @param stmt Statement object to execute the SQL
     * @param cmdStr the SQL string to execute
     * @return the return code from the executeUpdate call
     */
    public static int exeUpdateCmd(Statement stmt, String cmdStr)
    {

        try
        {
            //log.debug("---- exeUpdateCmd" + cmdStr);
            stmt.setEscapeProcessing(true);
            return stmt.executeUpdate(cmdStr);
        } 
        catch (Exception ex)
        {
            //TODO: Problem encountered with the CUPaleo database when converting the AccessionAgent 
            //We (Rod?) need to go in an create a hashtable that
            if ((ex instanceof MySQLIntegrityConstraintViolationException)&&(cmdStr.contains("INSERT INTO accessionagent")))
            {
                log.error("ignoring a record because it makes a MySQLIntegrityConstraintViolation: " + ex.getStackTrace().toString() );
                log.error(cmdStr+"\n");
                ex.printStackTrace();
                return 0;
            }
            else if (cmdStr.contains("INSERT INTO accessionagent"))
            {
                log.error("ignoring a record because it makes a uncatchable SQL Exception: " + ex.getStackTrace().toString() );
                log.error(cmdStr+"\n");
                ex.printStackTrace();
                return 0;  
            }
            else 
            {
                //e.printStackTrace();
                log.error(ex.getMessage());
                log.error(cmdStr+"\n");
                ex.printStackTrace();
                //ex.getStackTrace().
                throw new RuntimeException(ex);  
            }
        }
        //return -1;
    }
    
    /**
     * Returns the ID of the record that was just inserted.
     * @param stmt the insert statement
     * @return null on error, or the ID
     */
    public static Integer getInsertedId(final Statement stmt)
    {
        try
        {
            ResultSet resultSet = stmt.getGeneratedKeys(); 
    
            if ( resultSet != null && resultSet.next() ) 
            { 
                return resultSet.getInt(1); 
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * Executes an SQL Update command
     * @param stmt Statement object to execute the SQL
     * @param cmdStr the SQL string to execute
     * @return the return code from the executeUpdate call
     */
//    public static int exeUpdateCmd(Statement stmt, String cmdStr)
//    {
//        log.debug("exeUpdateCmd" + cmdStr);
//        if (cmdStr.equals("SET FOREIGN_KEY_CHECKS = 0")
//                && (myDestinationServerType != myDestinationServerType.MS_SQLServer))
//        {
//            try
//            {
//                // log.info(cmdStr);
//                return stmt.executeUpdate(cmdStr);
//
//            } catch (Exception ex)
//            {
//                // e.printStackTrace();
//                log.error(ex.getStackTrace().toString());
//                log.error(cmdStr + "\n");
//                ex.printStackTrace();
//                throw new RuntimeException(ex);
//            }
//        } else try
//        {
//            return removeForeignKeyConstraints(stmt.getConnection());
//        } catch (SQLException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return -1;
//    }

    /**
     * Deletes all the records from a table
     * @param tableName the name of the table
     * @param currentServerType server type
     * @return the return value from the SQL update statment (or -1 on an exception)
     */
    public static int deleteAllRecordsFromTable(final String    tableName,
                                                final SERVERTYPE currentServerType)
    {
        int count = 0;

        try
        {
            Connection connection = dbConn != null ? dbConn : DBConnection.getInstance().createConnection();

            count = deleteAllRecordsFromTable(connection, tableName,currentServerType);

            if (dbConn == null)
            {
                connection.close();
            }

        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        return count;
   }
    
    /**
     * @param sql
     * @return
     */
    public static Integer getCount(final String sql)
    {
        return getCount(dbConn != null ? dbConn : DBConnection.getInstance().getConnection(), sql);
    }
    
    /**
     * @param sql
     * @return
     */
    public static int getCountAsInt(final String sql)
    {
        return getCountAsInt(dbConn != null ? dbConn : DBConnection.getInstance().getConnection(), sql);
    }
    
    /**
     * @param conn
     * @param sql
     * @return
     */
    public static int getCountAsInt(final Connection conn, final String sql)
    {
        Integer cnt = getCount(conn, sql);
        return cnt == null ? 0 : cnt;
    }
    
    /**
     * @param sql
     * @return
     */
    public static Integer getCount(final Connection connection, final String sql)
    {
        Integer   count = null;
        Statement stmt  = null;
        try
        {
            //log.debug(sql);
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next())
            {
                count = rs.getInt(1);
            }
            rs.close();
            
            //log.debug(count+" - "+sql);

        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                    
                } catch (Exception ex) {}
            }
        }

        return count;
    }

    /**
     * @param sql
     * @return
     */
    public static Vector<Object[]> query(final String sql)
    {
        return query(null, sql, false);
    }

    /**
     * @param sql
     * @return
     */
    public static Vector<Object[]> query(final Connection conn, final String sql)
    {
        return query(conn, sql, false);
    }

    /**
     * @param conn
     * @param sql
     * @param includeHeaderRow
     * @return
     */
    public static Vector<Object[]> query(final Connection conn, final String sql, final boolean includeHeaderRow)
    {
        Vector<Object[]> list = new Vector<Object[]>();
        Statement stmt = null;
        
        Connection connection    = null;
        boolean    doCloseConn   = false;
        boolean    doSkipConnSet = false;
        boolean    isStale       = true;
        int        tries         = 0;
        
        while (isStale && tries < 3)
        {
            try
            {
                if (!doSkipConnSet)
                {
                    if (conn != null)
                    {
                        connection = conn;
                        
                    } else if (dbConn != null)
                    {
                        connection = dbConn;
                    } else
                    {
                        connection = DBConnection.getInstance().createConnection();
                        doCloseConn = true;
                    }
                }
    
                tries++;
                stmt = connection.createStatement();
                ResultSet         rs       = stmt.executeQuery(sql);
                ResultSetMetaData metaData = rs.getMetaData();
                int               numCols  = metaData.getColumnCount();
                if (includeHeaderRow)
                {
                    Object[] colData = new Object[numCols];
                    list.add(colData);
                    for (int i=0;i<numCols;i++)
                    {
                        colData[i] = metaData.getColumnName(i+1);
                    } 
                }
                while (rs.next())
                {
                    Object[] colData = new Object[numCols];
                    list.add(colData);
                    for (int i=0;i<numCols;i++)
                    {
                        colData[i] = rs.getObject(i+1);
                    }
                }
                rs.close();
                
                isStale = false;
    
            } catch (CommunicationsException ex)
            {
                connection = DBConnection.getInstance().createConnection();
                doCloseConn   = true;
                doSkipConnSet = true;
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
                
                if (!skipTrackExceptions)
                {
                    edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
                }
                
            } finally
            {
                if (stmt != null)
                {
                    try
                    {
                        stmt.close();
                    } catch (Exception ex) {}
                }
            }
            
            if (!isStale && connection != null && doCloseConn)
            {
                try
                {
                    connection.close();
                } catch (Exception ex) {}
            }
        }

        return list;
    }

    /**
     * @param sql
     * @return
     */
    public static Vector<Object> querySingleCol(final String sql)
    {
        return querySingleCol(null, sql);
    }

    /**
     * @param conn 
     * @param sql
     * @return
     */
    public static Vector<Object> querySingleCol(final Connection conn, final String sql)
    {
        Vector<Object> list = new Vector<Object>();
        Statement stmt = null;
        
        Connection connection    = null;
        boolean    doCloseConn   = false;
        boolean    doSkipConnSet = false;
        boolean    isStale       = true;
        int        tries         = 0;
        
        while (isStale && tries < 3)
        {
            try
            {
                if (!doSkipConnSet)
                {
                    if (conn != null)
                    {
                        connection = conn;
                        
                    } else if (dbConn != null)
                    {
                        connection = dbConn;
                    } else
                    {
                        connection = DBConnection.getInstance().createConnection();
                        doCloseConn = true;
                    }
                }
    
                tries++;
                stmt = connection.createStatement();
                ResultSet         rs       = stmt.executeQuery(sql);
                ResultSetMetaData metaData = rs.getMetaData();
                int               numCols  = metaData.getColumnCount();
                
                if (numCols > 1)
                {
                    log.warn("Query has "+numCols+" columns and should only have one.");
                }

                while (rs.next())
                {
                    list.add(rs.getObject(1));
                }
                rs.close();
                
                isStale = false;
    
            } catch (CommunicationsException ex)
            {
                connection = DBConnection.getInstance().createConnection();
                doCloseConn   = true;
                doSkipConnSet = true;
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
                
                if (!skipTrackExceptions)
                {
                    edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
                }
                
            } finally
            {
                if (stmt != null)
                {
                    try
                    {
                        stmt.close();
                    } catch (Exception ex) {}
                }
            }
            
            if (!isStale && connection != null && doCloseConn)
            {
                try
                {
                    connection.close();
                } catch (Exception ex) {}
            }
        }

        return list;
    }

    /**
     * @param sql
     * @return
     */
    public static <T> T querySingleObj(final String sql)
    {
        return querySingleObj(null, sql);
    }

    /**
     * @param conn 
     * @param sql
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T querySingleObj(final Connection conn, final String sql)
    {
        Vector<Object> list = querySingleCol(conn, sql);
        if (list.size() > 1)
        {
            log.warn("The query ["+sql+"] returned more than one object.");
        }
        return (T)list.get(0);
    }

    /**
     * @param sql
     * @return
     */
    public static Vector<Integer> queryForInts(final String sql)
    {
        return queryForInts(null, sql);
    }

    /**
     * @param conn 
     * @param sql
     * @return
     */
    public static Vector<Integer> queryForInts(final Connection conn, final String sql)
    {
        Vector<Integer> list = new Vector<Integer>();
        Statement stmt = null;
        
        Connection connection    = null;
        boolean    doCloseConn   = false;
        boolean    doSkipConnSet = false;
        boolean    isStale       = true;
        int        tries         = 0;
        
        while (isStale && tries < 3)
        {
            try
            {
                if (!doSkipConnSet)
                {
                    if (conn != null)
                    {
                        connection = conn;
                        
                    } else if (dbConn != null)
                    {
                        connection = dbConn;
                    } else
                    {
                        connection = DBConnection.getInstance().createConnection();
                        doCloseConn = true;
                    }
                }
    
                tries++;
                stmt = connection.createStatement();
                ResultSet         rs       = stmt.executeQuery(sql);
                ResultSetMetaData metaData = rs.getMetaData();
                int               numCols  = metaData.getColumnCount();
                
                if (numCols > 1)
                {
                    log.warn("Query has "+numCols+" columns and should only have one.");
                }

                while (rs.next())
                {
                    list.add(rs.getInt(1));
                }
                rs.close();
                
                isStale = false;
    
            } catch (CommunicationsException ex)
            {
                connection = DBConnection.getInstance().createConnection();
                doCloseConn   = true;
                doSkipConnSet = true;
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
                
                if (!skipTrackExceptions)
                {
                    edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
                }
                
            } finally
            {
                if (stmt != null)
                {
                    try
                    {
                        stmt.close();
                    } catch (Exception ex) {}
                }
            }
            
            if (!isStale && connection != null && doCloseConn)
            {
                try
                {
                    connection.close();
                } catch (Exception ex) {}
            }
        }

        return list;
    }
    
    /**
     * @param sql
     * @return
     */
    public static int update(final String sql)
    {
        return update(null, sql);
    }

    /**
     * @param conn
     * @param sql
     * @return
     */
    public static int update(final Connection conn, final String sql)
    {
        Statement stmt = null;
        try
        {
            Connection connection = conn != null ? conn : (dbConn != null ? dbConn : DBConnection.getInstance().getConnection());

            stmt = connection.createStatement();
            return stmt.executeUpdate(sql);

        } catch (SQLException ex)
        {
            
            if (!skipTrackExceptions)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
            }
            
        } finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                } catch (Exception ex) {}
            }
        }

        return -1;
    }

    /**
     * Deletes all the records from a table
     * @param connection connection to the DB
     * @param tableName the name of the table
     * @return the return value from the SQL update statement (or -1 on an exception)
     */
    public static int deleteAllRecordsFromTable(final Connection connection, 
                                                final String     tableName,
                                                final SERVERTYPE currentServerType)
    {
        try
        {
            if (doesTableExist(connection, tableName))
            {
                Integer count = getCount(connection, "SELECT COUNT(*) FROM " + tableName);
                if (count == null || count == 0)
                {
                    return 0;
                }
                
                Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                if (currentServerType != SERVERTYPE.MS_SQLServer)
                {
                    removeForeignKeyConstraints(stmt.getConnection(), currentServerType);
                }
                int retVal = exeUpdateCmd(stmt, "delete from "+tableName);
                stmt.clearBatch();
                stmt.close();
    
                log.info("Deleted "+count+" records from "+tableName);
    
                return retVal;
    
            }
        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
            log.error(ex);
            ex.printStackTrace();

        }
        return 0;
    }
    
    /**
     * @param connection
     * @param tableName
     * @return
     */
    public static boolean doesTableExist(final Connection connection, 
                                         final String tableName)
    {
        try
        {
            DatabaseMetaData mdm = connection.getMetaData();
            ResultSet        rs  = mdm.getColumns(connection.getCatalog(), connection.getCatalog(), tableName, null);
            if (rs.next())
            {
                rs.close();
                return true;
            }
            rs.close();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Removes all the records from all the tables from the current DBConnection
     */
    public static void cleanAllTables(SERVERTYPE currentServerType)
    {
        try
        {
            Connection connection = dbConn != null ? dbConn : DBConnection.getInstance().createConnection();

            cleanAllTables(connection, currentServerType);

            if (dbConn == null)
            {
                connection.close();
            }

        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
            //e.printStackTrace();
            log.error(ex);
        }
    }
    
    public static String getDatabaseName(final Connection connection)
    {
       // List<String> names = new Vector<String>();
        String databaseName = null;
        try
        {
            //Statement  stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            
            //ResultSet rs1 = stmt.executeQuery("select name from sysfiles");
            
            //if (rs1.first())
            //{
                databaseName = connection.getCatalog();
                //log.debug("GETTING db NAME: " + databaseName);
                return  databaseName;
            //}

        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
            log.error(ex);
            ex.printStackTrace();
        }
        return  databaseName;
    }

    /**
     * Removes all the records from all the tables
     */
    public static void cleanAllTables(final Connection connection,
                                      SERVERTYPE currentServerType)
    {
        try
        {
            Statement  stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);

            ResultSet rs = stmt.executeQuery("show tables");
            if (rs.first())
            {
                do
                {
                    String tableName = rs.getString(1);
                    //System.out.println("Deleting Records from "+tableName);
                    deleteAllRecordsFromTable(connection, tableName,currentServerType);
                } while (rs.next());
            }
            rs.close();

            stmt.clearBatch();
            stmt.close();

        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
            log.error(ex);
            ex.printStackTrace();
        }
    }

    /**
     * Returns a valid String value for an Object, meaning it will put quotes around Strings and ate etc.
     * @param obj the object to convert
     * @return the string representation
     */
    public static String getStrValue(final Object obj)
    {
        return getStrValue(obj, null);
    }


    /**
     * Returns a valid String value for an Boolean, meaning convert true to 1 and false to 0
     * @param Boolean value to convert
     * @return the string representation
     */
    public static String getStrValue(final Boolean val)
    {
        return Integer.toString(val == false? 0 : 1);
        //return getStrValue(obj, null);
    }
    
    /**
     * Escaped and delimited string for use in SQL, 
     * using appropriate delimiter for DestinationServerType
     * @param str
     * @return escaped and delimited string for use in SQL, 
     * using appropriate delimiter for DestinationServerType
     */
    public static String getEscapedSQLStrExpr(final String str)
    {
        if (str != null)
        {
        	String delimiter = "'";
        	/*if (myDestinationServerType == SERVERTYPE.MS_SQLServer
        			|| myDestinationServerType == SERVERTYPE.MySQL)
        	{
        		//possibly for other dbms some other encloser would be required
        		log.info("setting string delimiter to \"'\" for ServerType " + myDestinationServerType);
        	}*/
        	return delimiter + escapeStringLiterals(str, delimiter) + delimiter;
        }
        return null;
	}
    
    /**
     * Escapes the single quote so it can be part of the data without causing an exception.
     * @param str the string to be escaped
     * @return escaped version of str 
     * 
     * Delimiter is assumed to be "'"
     */
    public static String escapeStringLiterals(String str)
    {
    	//" can't be used to enclose strings in where clauses for SQLServer and postgres
    	//' works for MySQL, SQLServer, and postgres
    	return escapeStringLiterals(str, "'");
    }
    
    // MEG NEEDS TO FIX THIS!!!!!!! IT IS NOT CORRECT, BUT I WANTED TO MOVE ON
    /**
     * @param str string to escape
     * @param enclosingChar character used to delimit the string
     * @return string with bad characters escaped 
     */
    public static String escapeStringLiterals(String str, String enclosingChar)
    {
//        if (s.indexOf("\r\n")>= 0)
//                {
//            log.error("slash r slash n: newline encountered");
//                }
//
//        for(int i = 0; i < s.length(); i++)
//        {
//            char c = s.charAt(i);
//            //Character c1 = (Character)c;
//            if (Character.isWhitespace(c))
//            {
//                log.error("Character is whitespace");
//            }
//            
//            log.error("Char: " + c );
//            log.error("Unicode: " + Character.getNumericValue(c));
//        }
//        if (s.indexOf("\r")>= 0)
//        {
//            log.error("return encountered");
//        }
//                
       // log.debug("escaping string literal:" + s);
        if (str != null)
        {
            String s = str;
            if (s.indexOf("\\") >= 0)
            {
                // s = s.replaceAll("\\","\\\\\\");
                // s = s.replaceAll("\\", "\\\\");
                if (myDestinationServerType != SERVERTYPE.MS_SQLServer)
                {
                    s = s.replaceAll("\\\\", "\\\\\\\\");
                    //log.debug("escaping !M$ backslash:" + s);
                }
                // s = StringEscapeUtils.escapeJava(s);
               // log.debug("backslash:" + s);
            }
            if (enclosingChar.equals("\"") && s.indexOf("\"") >= 0)
            {
                s = s.replaceAll("\"", "\\\"\"");
                // s = s.replaceAll("\"","\\\"");
                //log.debug("escaped double quotes:" + s);
            }
            if (enclosingChar.equals("\'") && s.indexOf("\'") >= 0)
            {
                //if (myDestinationServerType == SERVERTYPE.MS_SQLServer)
                {
                    s = s.replaceAll("\'", "\'\'");
                }
                // s = s.replaceAll("\'","\\\'\'");
                // s = s.replaceAll("\'","\\\'");
                // log.debug("single quotes:" + s);
            }
            return s;
        }
        return null;
    }
    
    /**
     * @param obj
     * @return
     */
    public static java.sql.Date getDateObj(final Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        
        if (obj instanceof Integer)
        {
            getPartialDate(obj, datePair);
            
            try
            {
                Date d = dateFormatter.parse(datePair.first);
                if (d != null)
                {
                    return new java.sql.Date(d.getTime());
                }
                return null;
                
            } catch (ParseException e)
            {
                e.printStackTrace();
            }
            return null;
                
        } else if (obj instanceof Date)
        {
            return new java.sql.Date(((Date)obj).getTime());
        }
        return null;
    }

    /**
     * Returns a valid String value for an Object, meaning it will put quotes around Strings and ate
     * etc.
     * @param obj the object to convert
     * @return the string representation
     */
    public static String getStrValue(final Object obj, final String newFieldType)
    {
        if (obj == null)
        {
            return "NULL";

        } 
        else if (obj instanceof net.sourceforge.jtds.jdbc.ClobImpl )
        {
            //log.debug("instance of Clob");
            String str = "";
            ClobImpl clob = (ClobImpl)obj;
            //log.debug("tyring to get clob");
            try
            {
                str =  clob.getSubString(1, (int) clob.length());
//                str = escapeStringLiterals(str);
//                return '"'+str+'"';
                return getEscapedSQLStrExpr(str);
            } catch (SQLException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
                log.error("error occurred trying to get string from clob for SQL Server driver");
                // TODO Auto-generated catch block
                ex.printStackTrace();
                System.exit(0);
            }
            //return getStrValue(obj, null);    
            return obj.toString();
        }
        else if (obj instanceof String)
        {
            String str = (String)obj;
//            if (str.indexOf('"') > -1 || str.indexOf('\\') > -1 || str.indexOf('\'') > -1)
//            {
//                str = escapeStringLiterals(str);
//            }
//            return '"'+str+'"';
            	return getEscapedSQLStrExpr(str);
//            String str = (String)obj;
//            if (str.indexOf('"') > -1 || str.indexOf('\\') > -1)
//            {
//                log.debug("ran into char worth escaping" + str);
//                str = StringEscapeUtils.escapeSql(str);
//                log.debug("ran into char worth escaping (escaped for SQL)" + str);
//                //str = StringEscapeUtils.escapeJava(str);
//                //log.debug("ran into char worth escaping (escaped for java)" + str);
//            }
//            return '"'+str+'"';

        } else if (obj instanceof Integer)
        {
            if (newFieldType != null)
            {
                if (newFieldType.toLowerCase().indexOf("date") ==  0)
                {
                	getPartialDate(obj, datePair);
                    return datePair.first;

                }
                //Meg dropped the (1) from the newFieldType check, field metadata didn't include the (1) values
                else if (newFieldType.equalsIgnoreCase("bit") || newFieldType.equalsIgnoreCase("tinyint") || newFieldType.equalsIgnoreCase("boolean"))
                //else if (newFieldType.equalsIgnoreCase("bit(1)") || newFieldType.equalsIgnoreCase("tinyint(1)"))
                {
                    int val = ((Integer)obj).intValue();
                    return Integer.toString(val == 0 ? 0 : 1);
                }
                ////Meg dropped the (1) from the newFieldType check, field metadata didn't include the (1) values
                //else if (newFieldType.equalsIgnoreCase("bit") || newFieldType.equalsIgnoreCase("tinyint"))
                else if (newFieldType.equalsIgnoreCase("bit(1)") || newFieldType.equalsIgnoreCase("tinyint(1)"))
                {
                    int val = ((Integer)obj).intValue();
                    return Integer.toString(val == 0 ? 0 : 1);
                }
                return ((Integer)obj).toString();
            }
            return ((Integer)obj).toString();

        } else if (obj instanceof Date)
        {
            return '"'+dateTimeFormatter.format((Date)obj) + '"';

        } else if (obj instanceof Float)
        {
            return ((Float)obj).toString();

        } else if (obj instanceof Double)
        {
            return ((Double)obj).toString();

        } else if (obj instanceof Character)
        {
            return '"'+((Character)obj).toString()+'"';
        } else
        {
            //log.debug("not sure the type");
            return obj.toString();
        }
    }
//    /**
//     * Returns a valid String value for an Object, meaning it will put quotes around Strings and ate etc.
//     * @param obj the object to convert
//     * @return the string representation
//     */
//    public static String getStrValue2(final Object obj, final String newFieldType)
//    {
//        if (obj == null)
//        {
//            return "NULL";
//
//        } else if (obj instanceof String)
//        {
//            String str = (String)obj;
//            if (str.indexOf('"') > -1 || str.indexOf('\\') > -1 || str.indexOf('\'') > -1 )
//            {
//                str = StringEscapeUtils.escapeJava(str);
//            }
//            return '\''+str+'\'';
//
//        } else if (obj instanceof Integer)
//        {
//            if (newFieldType != null)
//            {
//                if (newFieldType.indexOf("date") ==  0)
//                {
//                    Date dateObj = UIHelper.convertIntToDate((Integer)obj);                   
//                    return dateObj == null ? "NULL" : '\''+dateFormatter.format(dateObj) + '\'';
//
//                }
//                else if (newFieldType.equalsIgnoreCase("bit(1)") || newFieldType.equalsIgnoreCase("tinyint(1)"))
//                {
//                    int val = ((Integer)obj).intValue();
//                    return Integer.toString(val == 0? 0 : 1);
//                }
//                return ((Integer)obj).toString();
//            }
//            return ((Integer)obj).toString();
//
//        } else if (obj instanceof Date)
//        {
//            return '\''+dateTimeFormatter.format((Date)obj) + '\'';
//
//        } else if (obj instanceof Float)
//        {
//            return ((Float)obj).toString();
//
//        } else if (obj instanceof Double)
//        {
//            return ((Double)obj).toString();
//
//        } else if (obj instanceof Character)
//        {
//            return '\''+((Character)obj).toString()+'\'';
//        } else
//        {
//            return obj.toString();
//        }
//    }

    public static List<String> getFieldNamesFromSchema(final Connection connection,
                                                       final String     tableName)
    {
        try
        {
            ArrayList<String> fields = new ArrayList<String>();
            
            DatabaseMetaData mdm = connection.getMetaData();
            ResultSet        rs  = mdm.getColumns(connection.getCatalog(), connection.getCatalog(), tableName, null);
            while (rs.next())
            {
                fields.add(rs.getString("COLUMN_NAME"));
            }
            rs.close();
            return fields;
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static List<String> getFieldNamesFromSchema(final Connection connection,
                                                       final String tableName,
                                                       final List<String> fields)
    {
        try
        {
            DatabaseMetaData mdm = connection.getMetaData();
            ResultSet        rs  = mdm.getColumns(connection.getCatalog(), connection.getCatalog(),
                    tableName, null);
            while (rs.next())
            {
                fields.add(rs.getString("COLUMN_NAME"));
            }
            rs.close();
            return fields;

        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }


    /**
     * @param connection
     * @return
     */
    public static List<String> getTableNames(final Connection connection)
    {
        try
        {
            ArrayList<String> fields = new ArrayList<String>();

            DatabaseMetaData mdm = connection.getMetaData();
            ResultSet        rs  = mdm.getTables(connection.getCatalog(), connection.getCatalog(), null, new String[] {"TABLE"});
            while (rs.next())
            {
                /*System.out.println("-------- " + rs.getString("TABLE_NAME")+" ----------");
                for (int i=1;i<=rs.getMetaData().getColumnCount();i++)
                {
                    System.out.println(rs.getMetaData().getColumnName(i)+"="+rs.getObject(i));

                }*/
                fields.add(rs.getString("TABLE_NAME"));
            }
            rs.close();
            return fields;

        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }


    /**
     * @param connection
     * @param tableName
     * @return
     */
    public static List<FieldMetaData> getFieldMetaDataFromSchema(final Connection connection,
                                                                 final String     tableName)
    {
        try
        {
            ArrayList<FieldMetaData> fields = new ArrayList<FieldMetaData>();
            
            DatabaseMetaData mdm = connection.getMetaData();
            ResultSet        rs  = mdm.getColumns(connection.getCatalog(), connection.getCatalog(), tableName, null);
            while (rs.next())
            {
                /*System.out.println("-------- " + rs.getString("COLUMN_NAME")+" ----------");
                for (int i=1;i<=rs.getMetaData().getColumnCount();i++)
                {
                    System.out.println(rs.getMetaData().getColumnName(i)+"="+rs.getObject(i));

                }*/
                
                String typeStr = rs.getString("TYPE_NAME");
                FieldMetaData fmd = new FieldMetaData(rs.getString("COLUMN_NAME"), 
                                                      typeStr, 
                                                      typeStr.startsWith("DATE"), 
                                                      false);
                fmd.setSqlType(rs.getInt("DATA_TYPE"));
                fields.add(fmd);
            }
            rs.close();
            return fields;
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    
    
    /**
     * @param connection
     * @param tableName
     * @return
     */
    public static Map<String, FieldMetaData> getFieldMetaDataFromSchemaHash(final Connection connection,
                                                                            final String     tableName)
    {
        Map<String, FieldMetaData> fieldMetaDataMap = new Hashtable<String, FieldMetaData>();
        for (FieldMetaData fmd : getFieldMetaDataFromSchema(connection, tableName))
        {
            fieldMetaDataMap.put(fmd.getName(), fmd);
        }
        return fieldMetaDataMap;
    }


    /**
     * Converts an integer time in the form of YYYYMMDD to the proper Date
     * @param iDate the int to be converted
     * @return the date object
     * @return the verbatimDate strin that holds old invalid verbatim dates
     */
    public static Date convertIntToDate(final int iDate, final StringBuilder verbatimDate)
    {
        calendar.clear();

        int year  = iDate / 20000;
        if (year > 1600)
        {
            int tmp   = (iDate - (year * 20000));
            int month = tmp / 100;
            int day   = (tmp - (month * 100));

            if (month == 0 || day == 0)
            {
                verbatimDate.setLength(0);
                verbatimDate.append(Integer.toString(iDate));
                if (month == 0)
                {
                    month = 7;
                }
                if (day == 0)
                {
                    day = 1;
                }
            }

            calendar.set(year, month-1, day);
        } else
        {
            calendar.setTimeInMillis(0);
        }

        return calendar.getTime();
    }
    
    /**
     * @param msg
     */
    protected static void writeErrLog(final String msg)
    { 
    	if (tblWriter != null)
        {
        	tblWriter.logError(msg);
        	tblWriter.flush();
        	
        } else
        {
        	missingPW.println(msg);
        	missingPW.flush();
        }
    }

    /**
     * Copies a table from one DB to another
     */
    /**
     * Copies a a table with the same name from one DB to another
     * @param fromConn the "from" DB
     * @param toConn the "to" DB
     * @param tableName the table name to be copied
     * @param colNewToOldMap a map of new file names toold file names
     * @param verbatimDateMapper a map from the new Vertbatim Date Field to the new date column name it is associated with
     * @return true if successful
     */
    public static boolean copyTable(final Connection fromConn,
                                    final Connection toConn,
                                    final String     tableName,
                                    final Map<String, String> colNewToOldMap,
                                    final Map<String, String> verbatimDateMapper,                                   
                                    final SERVERTYPE sourceServerType,
                                    final SERVERTYPE destServerType)
    {
        return copyTable(fromConn, toConn, "select * from " + tableName, tableName, tableName, colNewToOldMap, verbatimDateMapper,sourceServerType,destServerType);
    }
    /**
     * Copies a table to a new table of a different name (same schema) within the same DB Connection
     * @param conn a connection to copy from one table to another in the same database
     * @param fromTableName the table name its coming from
     * @param toTableName the table name it is going to
     * @param colNewToOldMap a map of new file names toold file names
     * @param verbatimDateMapper a map from the new Vertbatim Date Field to the new date column name it is associated with
     * @return true if successful
     */
    public static boolean copyTable(final Connection fromConn,
                                    final Connection toConn,
                                    final String     fromTableName,
                                    final String     toTableName,
                                    final Map<String, String> colNewToOldMap,
                                    final Map<String, String> verbatimDateMapper,                                   
                                    final SERVERTYPE sourceServerType,
                                    final SERVERTYPE destServerType)
    {
        return copyTable(fromConn, toConn, "select * from " + fromTableName, fromTableName, toTableName, colNewToOldMap, verbatimDateMapper,sourceServerType,destServerType);
    }
    
    /**
     * Copies a table to a new table of a different name (same schema) within the same DB Connection
     * @param conn a connection to copy from one table to another in the same database
     * @param fromTableName the table name its coming from
     * @param toTableName the table name it is going to
     * @param colNewToOldMap a map of new file names toold file names
     * @param verbatimDateMapper a map from the new Vertbatim Date Field to the new date column name it is associated with
     * @return true if successful
     */
    public static boolean copyTable(final Connection conn,
                                    final String     fromTableName,
                                    final String     toTableName,
                                    final Map<String, String> colNewToOldMap,
                                    final Map<String, String> verbatimDateMapper,                                   
                                    final SERVERTYPE sourceServerType,
                                    final SERVERTYPE destServerType)
    {
        return copyTable(conn, conn, "select * from " + fromTableName, fromTableName, toTableName, colNewToOldMap, verbatimDateMapper,sourceServerType,destServerType);
    }

    /**
     * Copies from one connection/table to another connection/table. Sets the order by clause to be the first field in the
     * "from" field list.
     *
     * @param fromConn DB Connection that the data is coming from
     * @param toConn Connection that the data is going to
     * @param sql the SQL to be executed
     * @param fromTableName the table name its coming from
     * @param toTableName the table name it is going to
     * @param colNewToOldMap a map of new file names to old file names
     * @param verbatimDateMapper a map from the new Vertbatim Date Field to the new date column name it is associated with
     * @return true if successful
     */
    public static boolean copyTable(final Connection fromConn,
                                    final Connection toConn,
                                    final String     sql,
                                    final String     fromTableName,
                                    final String     toTableName,
                                    final Map<String, String> colNewToOldMap,
                                    final Map<String, String> verbatimDateMapper,                                   
                                    final SERVERTYPE sourceServerType,
                                    final SERVERTYPE destServerType)
    {
        String sqlStr = sql == null ? "SELECT * FROM " + fromTableName : sql;
        
        return copyTable(fromConn,toConn,sqlStr,fromTableName,toTableName,colNewToOldMap,verbatimDateMapper,null,sourceServerType,destServerType);
    }

    /**
     * @param fromConn
     * @param toConn
     * @param sql
     * @param fromTableName
     * @param toTableName
     * @param colNewToOldMap
     * @param verbatimDateMapper
     * @param newColDefValues
     * @param sourceServerType
     * @param destServerType
     * @return
     */
    public static boolean copyTable(final Connection fromConn,
                                    final Connection toConn,
                                    final String     sql,
                                    final String     fromTableName,
                                    final String     toTableName,
                                    final Map<String, String> colNewToOldMap,
                                    final Map<String, String> verbatimDateMapper,
                                    final Map<String, String> newColDefValues,
                                    final SERVERTYPE sourceServerType,
                                    final SERVERTYPE destServerType)
    {
        return copyTable(fromConn, toConn, sql, null, fromTableName, toTableName, colNewToOldMap, verbatimDateMapper, newColDefValues, sourceServerType, destServerType);
    }

    /**
     * @param fromConn
     * @param toConn
     * @param sql
     * @param fromTableName
     * @param toTableName
     * @param colNewToOldMap
     * @param verbatimDateMapper
     * @param newColDefValues
     * @param sourceServerType
     * @param destServerType
     * @return
     */
    public static boolean copyTable(final Connection fromConn,
                                    final Connection toConn,
                                    final String     sql,
                                    final String     countSQL,
                                    final String     fromTableName,
                                    final String     toTableName,
                                    final Map<String, String> colNewToOldMap,
                                    final Map<String, String> verbatimDateMapper,
                                    final Map<String, String> newColDefValues,
                                    final SERVERTYPE sourceServerType,
                                    final SERVERTYPE destServerType)
    {
        //Timestamp now = new Timestamp(System.currentTimeMillis());

        IdMapperMgr idMapperMgr = IdMapperMgr.getInstance();

        if (frame != null)
        {
            frame.setDesc("Copying Table "+fromTableName);
        }
        log.info("Copying Table "+fromTableName);

        List<String> fromFieldNameList = getFieldNamesFromSchema(fromConn, fromTableName);

        String sqlStr = sql + " ORDER BY " +  fromTableName + "." + fromFieldNameList.get(0);
        log.debug(sqlStr);
        
        int numRecs;
        if (countSQL == null)
        {
            numRecs = getNumRecords(fromConn, fromTableName);
        } else
        {
            numRecs = getCountAsInt(fromConn, countSQL);
        }
        setProcess(0, numRecs);
        
        DBTableInfo tblInfo         = DBTableIdMgr.getInstance().getInfoByTableName(toTableName);
        Statement   updateStatement = null;
        String      id              = "";
        try
        {
            
            updateStatement = toConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            if (BasicSQLUtils.myDestinationServerType != BasicSQLUtils.SERVERTYPE.MS_SQLServer) 
            {
                BasicSQLUtils.removeForeignKeyConstraints(toConn, BasicSQLUtils.myDestinationServerType);
            
            }
            
            //HashMap<String, Integer> newDBFieldHash   = new HashMap<String, Integer>();
            List<FieldMetaData>      newFieldMetaData = getFieldMetaDataFromSchema(toConn, toTableName);
            //int inx = 1;
            //for (FieldMetaData fmd : newFieldMetaData)
            //{
            //    newDBFieldHash.put(fmd.getName(), inx++);
            //}

            Statement         stmt = fromConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            //System.out.println(sqlStr);
            ResultSet         rs   = stmt.executeQuery(sqlStr);
            ResultSetMetaData rsmd = rs.getMetaData();
            
            Vector<Integer> dateColumns = new Vector<Integer>();
            
            //System.out.println(toTableName);
            Hashtable<String, Integer> fromHash = new Hashtable<String, Integer>();
            for (int i = 1; i <= rsmd.getColumnCount(); i++)
            {
            	String colName = rsmd.getColumnName(i);
                
                fromHash.put(colName, i);
                //System.out.println(rsmd.getColumnName(i)+" -> "+i);
                
                if (rsmd.getColumnType(i) == java.sql.Types.DATE || 
                    colName.toLowerCase().endsWith("date") || 
                    colName.toLowerCase().startsWith("date"))
                {
                	//System.out.println("Date: "+rsmd.getColumnName(i)+" -> "+i);
                	dateColumns.add(i);
                }
            }
            
            Hashtable<String, String> oldNameToNewNameHash = new Hashtable<String, String>();
            if (colNewToOldMap != null)
            {
	            for (String newName : colNewToOldMap.keySet())
	            {
	            	String oldName = colNewToOldMap.get(newName);
	            	System.out.println("Mapping oldName["+(oldName == null ? newName : oldName) + " -> "+ newName);
	            	
	            	oldNameToNewNameHash.put(oldName == null ? newName : oldName, newName);
	            }
            }
            
            // System.out.println("Num Cols: "+rsmd.getColumnCount());

            Map<String, String>               vertbatimDateMap = UIHelper.createMap();
            Map<String, Pair<String, String>> dateMap          = new Hashtable<String, Pair<String, String>>();
            
            String insertSQL = null;

            // Get the columns that have dates in case we get a TimestampCreated date that is null
            // and then we can go looking for an older date to try to figure it out
            Integer timestampModifiedInx = fromHash.get("TimestampModified");
            Integer timestampCreatedInx  = fromHash.get("TimestampCreated");
            boolean isAccessionTable     = fromTableName.equals("accession");

            StringBuilder verbatimDateStr = new StringBuilder(1024);
            StringBuffer  str             = new StringBuffer(1024);
            int           count           = 0;
            while (rs.next())
            {
            	boolean skipRecord = false;
            	
                // Start by going through the resultset and converting all dates from Integers
                // to real dates and keep the verbatium date information if it is a partial date
                for (int i : dateColumns)
                {
                    String  oldColName  = rsmd.getColumnName(i);
                    Integer oldColIndex = fromHash.get(oldColName);

                    if (oldColIndex == null)
                    {
                        log.error("Couldn't find new column for old column for date for Table[" + fromTableName + "] Col Name[" + newFieldMetaData.get(i).getName() + "]");
                        continue;
                    }

                    if (oldColIndex > newFieldMetaData.size())
                    {
                    	continue;
                    }
                    
                    String newColName = colNewToOldMap != null ? oldNameToNewNameHash.get(oldColName) : null;
                    if (newColName == null)
                    {
                    	newColName = oldColName;
                    }
                    
                    Object dataObj = rs.getObject(i);
                    
                    if (dataObj instanceof Integer)
                    {
                    	Pair<String, String> datep = new Pair<String, String>();
                    	getPartialDate((Integer)dataObj, datep);
                        dateMap.put(newColName, datep);

                        if (verbatimDateMapper != null)
                        {
	                        if (verbatimDateStr.length() > 0)
	                        {
	                            vertbatimDateMap.put(newColName, dataObj.toString());
	                        } else
	                        {
	                            log.error("No Verbatim Date Mapper  for Table[" + fromTableName + "] Col Name[" + newFieldMetaData.get(i).getName() + "]");
	                        }
                        }
                    }
                }
                
                // OK here we make sure that both the created dated ad modified date are not null
                // and we copy the date if one has a value and the other does not.
                Date timestampCreatedCached  = now;
                Date timestampModifiedCached = now;
                
                if (timestampModifiedInx != null && timestampCreatedInx != null)
                {
                    timestampModifiedCached = rs.getDate(timestampModifiedInx);
                    timestampCreatedCached  = rs.getDate(timestampCreatedInx);
                    if (timestampModifiedCached == null && timestampCreatedCached == null)
                    {
                        timestampCreatedCached  = Calendar.getInstance().getTime();
                        timestampModifiedCached = Calendar.getInstance().getTime();
                        
                    } else if (timestampModifiedCached == null && timestampCreatedCached != null)
                    {
                        timestampModifiedCached = new Date(timestampCreatedCached.getTime());
                    } else
                    {
                        timestampCreatedCached = timestampModifiedCached != null ? new Date(timestampModifiedCached.getTime()) : new Date();
                    }
                } else 
                {
                    
                    if (timestampModifiedInx != null)
                    {
                        timestampModifiedCached = rs.getDate(timestampModifiedInx);
                        if (timestampModifiedCached == null)
                        {
                            timestampModifiedCached = now;
                        }
                    }
                        
                    if (timestampCreatedInx != null)
                    {
                        timestampCreatedCached = rs.getDate(timestampCreatedInx);
                        if (timestampCreatedCached == null)
                        {
                            timestampCreatedCached = now;
                        }
                    }
                }
                    
                str.setLength(0);
                if (insertSQL == null)
                {
                    StringBuffer fieldList = new StringBuffer();
                    fieldList.append("( ");
                    for (int i = 0; i< newFieldMetaData.size();i++)
                    {
                        if ((i > 0) &&  (i < newFieldMetaData.size()))
                        {
                            fieldList.append(", ");
                        }
                        String newFieldName = newFieldMetaData.get(i).getName();
                        fieldList.append(newFieldName + " ");
                    }
                    fieldList.append(")");
                    
                    
                    str.append("INSERT INTO " + toTableName + " "+ fieldList+ " VALUES (");
                    
                    insertSQL = str.toString();
                    
                    log.debug(str);
                } else
                {
                    str.append(insertSQL);
                }
                
                id = rs.getString(1);

                // For each column in the new DB table...
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    FieldMetaData newFldMetaData   = newFieldMetaData.get(i);
                    String        newColName       = newFldMetaData.getName();
                    String        oldMappedColName = null;
                    
                    //System.out.println("["+newFieldName.getName()+"]");

                    // Get the Old Column Index from the New Name
                   // String  oldName     = colNewToOldMap != null ? colNewToOldMap.get(newColName) : newColName;
                    Integer columnIndex = fromHash.get(newColName);
                    
                    if (columnIndex == null && colNewToOldMap != null)
                    {
                        oldMappedColName = colNewToOldMap.get(newColName);
                        if (oldMappedColName != null)
                        {
                            columnIndex = fromHash.get(oldMappedColName);

                        } else if (isOptionOn(SHOW_NAME_MAPPING_ERROR) &&
                                   (ignoreMappingFieldNames == null || 
                                    ignoreMappingFieldNames.get(newColName) == null))
                        {
                            String msg = "No Map for table ["+fromTableName+"] from New Name[" + newColName + "] to Old Name["+oldMappedColName+"]";
                            log.error(msg);
                            
                            writeErrLog(msg);
                            
                        }
                    } else
                    {
                        oldMappedColName = newColName;
                    }
                    
                    //System.out.println("new["+newColName+"]  old["+oldMappedColName+"]");

                    if (columnIndex != null)
                    {
                        if (i > 0) str.append(", ");
                        Object dataObj = rs.getObject(columnIndex);
                        
                        if (idMapperMgr != null && oldMappedColName != null && oldMappedColName.endsWith("ID"))
                        {
                            IdMapperIFace idMapper = idMapperMgr.get(fromTableName, oldMappedColName);
                            if (idMapper != null)
                            {
                                int showNullOption     = SHOW_NULL_FK;
                                int showFkLookUpOption = SHOW_FK_LOOKUP;
                                                               
                                int oldPrimaryKeyId = rs.getInt(columnIndex);
                                if (oldMappedColName.equalsIgnoreCase(fromTableName+"id"))
                                {
                                    showNullOption     = SHOW_NULL_PM;
                                    showFkLookUpOption = SHOW_PM_LOOKUP;
                                } 
                                    
                                // if the value was null, getInt() returns 0
                                // use wasNull() to distinguish real 0 from a null return
                                if (rs.wasNull())
                                {
                                    dataObj = null;
                                    
                                    if (isOptionOn(showNullOption))
                                    {
                                        
                                    	String msg = "Unable to Map "+(showNullOption == SHOW_NULL_FK ? "Foreign" : "Primary")+" Key Id[NULL] old Name["+oldMappedColName+"]   colInx["+columnIndex+"]   newColName["+newColName+"]";
                                        log.error(msg);
                                        writeErrLog(msg);
                                        skipRecord = true;
                                    }
                                }
                                else
                                {
                                    dataObj = idMapper.get(oldPrimaryKeyId);
                                    
                                    if (dataObj == null && isOptionOn(showFkLookUpOption))
                                    {
                                        String msg = "Unable to Map Primary Id["+oldPrimaryKeyId+"] old Name["+oldMappedColName+"] table["+fromTableName+"]";
                                        log.error(msg);
                                        writeErrLog(msg);
                                        skipRecord = true;
                                    }
                                }
                            } else
                            {
                                if (isOptionOn(SHOW_NAME_MAPPING_ERROR) && 
                                        (ignoreMappingFieldIDs == null || ignoreMappingFieldIDs.get(oldMappedColName) == null))
                                {
                                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                                    // XXX Temporary fix so it doesn't hide other errors
                                    // Josh has promised his first born if he doesn't fix this!
                                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                                    if (!oldMappedColName.equals("RankID"))
                                    {
                                        //idMapperMgr.dumpKeys();
                                        String msg = "No ID Map for ["+fromTableName+"] Old Column Name["+oldMappedColName+"]";
                                        log.error(msg);
                                        writeErrLog(msg);
                                        skipRecord = true;
                                    }
                                }
                            }
                        }

                        // First check to see if it is null
                        if (dataObj == null)
                        {
                            if (newFldMetaData.getName().equals("TimestampCreated"))
                            {
                                if (timestampCreatedInx != null)
                                {
                                    if (isAccessionTable)
                                    {
                                        Date date = UIHelper.convertIntToDate(rs.getInt(fromHash.get("DateAccessioned")));
                                        str.append(date != null ? getStrValue(date) : getStrValue(timestampCreatedCached, newFldMetaData.getType()));
    
                                    } else
                                    {
                                        str.append(getStrValue(timestampCreatedCached, newFldMetaData.getType()));
                                    }
    
                                } else
                                {
                                    str.append(getStrValue(timestampCreatedCached, newFldMetaData.getType()));
                                }
    
                            } else if (newFldMetaData.getName().equals("TimestampModified"))
                            {
                                if (timestampModifiedInx != null)
                                {
                                    if (isAccessionTable)
                                    {
                                        Date date = UIHelper.convertIntToDate(rs.getInt(fromHash.get("DateAccessioned")));
                                        str.append(date != null ? getStrValue(date) : getStrValue(timestampCreatedCached, newFldMetaData.getType()));
    
                                    } else
                                    {
                                        str.append(getStrValue(timestampModifiedCached, newFldMetaData.getType()));
                                    }
                                } else
                                {
                                    str.append(getStrValue(timestampModifiedCached, newFldMetaData.getType()));
                                }
                            } else
                            {
                                str.append("NULL");
                            }
                                

                        } else if (dataObj instanceof Integer && 
                                  (newFldMetaData.getSqlType() == java.sql.Types.DATE ||
                                   newColName.toLowerCase().endsWith("date") || 
                                   newColName.toLowerCase().startsWith("date")))
                        {
                        	Pair<String, String> datePr = dateMap.get(newColName);
                        	if (datePr != null)
                        	{
                        		str.append(datePr.first);
                        		
                        	} else if (verbatimDateMapper != null)
                        	{
	                            // First check to see if the current column name is that of the verbatim field
	                            // it will return the new schema's date field name that this verbatim field is associated with
	                            String dateFieldName = verbatimDateMapper.get(newColName); // from verbatim to associated date field
	                            if (dateFieldName != null)
	                            {
	                                str.append(getStrValue(vertbatimDateMap.get(newColName)));
	
	                            } else
	                            {
	                                str.append(getStrValue(dateMap.get(newColName)));
	                            }
                        	} else
                        	{
                        		str.append("NULL");
                        	}

                        } else if (dataObj instanceof Number)
                        {
                            DBFieldInfo fi   = tblInfo.getFieldByColumnName(newColName);
                            String      type = newFldMetaData.getType().toLowerCase().startsWith("tiny") ? fi.getType() : newFldMetaData.getType();
                            str.append(getStrValue(dataObj, type));
                            
                        } else 
                        {
                            str.append(getStrValue(dataObj, newFldMetaData.getType()));
                        }

                    } else if (newColName.endsWith("Version"))
                    {
                        if (i > 0) str.append(", ");
                        str.append("0");
                        
                    } else if (newColName.endsWith("DatePrecision"))
                    {
                    	if (i > 0) str.append(", ");
                    	
                    	String cName = newColName.substring(0, newColName.length()-9);
                    	Pair<String, String> datePr = dateMap.get(cName);
                    	if (datePr != null)
                    	{
                    		str.append(datePr.second);
                    	} else
                    	{
                    		str.append("NULL");
                    	}
                    	
                    } else if (idMapperMgr != null && newColName.endsWith("ID") && oneToOneIDHash != null && oneToOneIDHash.get(newColName) != null)
                    {
                        
                        IdMapperIFace idMapper = idMapperMgr.get(toTableName, newColName);
                        if (idMapper != null)
                        {
                            idMapper.setShowLogErrors(false);
                            Integer newPrimaryId = idMapper.get(Integer.parseInt(id));
                            if (newPrimaryId != null)
                            {
                                if (i > 0) str.append(", ");
                                str.append(newPrimaryId);
                            } else
                            {
                                if (i > 0) str.append(", ");
                                str.append("NULL");
                                
                                if (isOptionOn(SHOW_VAL_MAPPING_ERROR))
                                {
                                    String msg = "For Table[" + fromTableName + "] mapping new Column Name[" + newColName + "] ID["+id+"] was not mapped";
                                    log.error(msg);
                                    writeErrLog(msg);
                                    skipRecord = true;
                                }
                            }
                        }
                        
                    } else // there was no old column that maps to this new column
                    {
                        String newColValue = null;
                        if (newColDefValues != null)
                        {
                            newColValue = newColDefValues.get(newColName);
                        }
                        
                        if (newColValue == null)
                        {
                            newColValue = "NULL";
                            //System.out.println("ignoreMappingFieldNames" + ignoreMappingFieldNames);
                            //System.out.println("ignoreMappingFieldNames.get(colName)" + ignoreMappingFieldNames.get(colName));
                            if (isOptionOn(SHOW_NAME_MAPPING_ERROR) &&
                                (ignoreMappingFieldNames == null || ignoreMappingFieldNames.get(newColName) == null))
                            {
                                String msg = "For Table[" + fromTableName + "] mapping new Column Name[" + newColName + "] was not mapped";
                                log.error(msg);
                                writeErrLog(msg);
                                skipRecord = true;
                            }
                        }
                        if (i > 0) str.append(", ");
                        
                        BasicSQLUtilsMapValueIFace valueMapper = columnValueMapper.get(newFldMetaData.getName());
                        if (valueMapper != null)
                        {
                            newColValue = valueMapper.mapValue(newColValue);
                        }
                        
                        str.append(newColValue);
                    }

                }
                
                str.append(")");
                if (frame != null)
                {
                    if (count % 500 == 0)
                    {
                        frame.setProcess(count);
                    }
                    
                } else
                {
                    if (count % 2000 == 0)
                    {
                        log.info(toTableName + " processed: " + count);
                    }                        
                }
                
                //setQuotedIdentifierOFFForSQLServer(toConn, BasicSQLUtils.myDestinationServerType);
                //exeUpdateCmd(updateStatement, "SET FOREIGN_KEY_CHECKS = 0");
                //if (str.toString().toLowerCase().contains("insert into locality"))
                //{
                    //log.debug(str.toString());
                //}

                //String str2 = "SET QUOTED_IDENTIFIER ON";
                //log.debug("executing: " + str);
                //updateStatement.execute(str2);
               // updateStatement.close();
                if (!skipRecord)
                {
                    if (isOptionOn(SHOW_COPY_TABLE))
                    {
                        log.debug("executing: " + str);
                    }
	                int retVal = exeUpdateCmd(updateStatement, str.toString());
	                if (retVal == -1)
	                {
	                    rs.close();
	                    stmt.clearBatch();
	                    stmt.close();
	                    return false;
	                }
                }
                count++;
                // if (count == 1) break;
            }
            
            if (frame != null)
            {
                frame.setProcess(count);
               
            } else
            {
                log.info(fromTableName + " processed " + count + " records.");         
            }            

            rs.close();
            stmt.clearBatch();
            stmt.close();

        } catch (SQLException ex)
        {
        	ex.printStackTrace();
        	
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
            //e.printStackTrace();
            log.error(sqlStr);
            log.error(ex);
            log.error("ID: " + id);
        } finally
        {
            try
            {
                updateStatement.clearBatch();
                updateStatement.close();
                
            } catch (SQLException ex)
            {
                
            }
        }
        BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(null);//meg added
        return true;
    }
    
    /**
     * @param newFieldName
     * @param type
     * @param data
     * @param sb
     */
    public static  void fixTimestamps(final String newFieldName, 
                                      final String type, 
                                      final Object data, 
                                      final StringBuilder sb)
    {
        if (newFieldName.equals("TimestampModified") || newFieldName.equals("TimestampModified"))
        {
            if (getStrValue(data, type).toString().toLowerCase().equals("null"))
            {
                sb.append("'" + nowStr + "'");
            } else
            {
                sb.append(getStrValue(data, type));
            }
        } else
        {
            sb.append(getStrValue(data, type));
        }
    }
    
    /**
     * @param data
     * @param newFieldType
     * @param datePair
     */
    public static void getPartialDate(final Object data, 
                                      final Pair<String, String> datePairArg)
    {
        getPartialDate(data, datePairArg, true);
    }
    
    /**
     * @param data
     * @param newFieldType
     * @param datePair
     */
    public static void getPartialDate(final Object data, 
                                      final Pair<String, String> datePairArg,
                                      final boolean includeQuotes)
    {
        datePairArg.first  = "NULL";
        datePairArg.second = "NULL";
        
        if (data != null)
        {
            if (((Integer)data) > 0)
            {
                // 012345678     012345678
                // 20051314      19800307
                Date   dateObj = null;
                String dateStr = ((Integer)data).toString();
                if (dateStr.length() == 8)
                {
                    //System.out.println("["+dateStr+"]["+data+"]");//["+(dateStr.length() >)+"]");
                    int fndInx  = dateStr.substring(4, 8).indexOf("00");
                    if (fndInx > -1)
                    {
                        if (fndInx == 0)
                        {
                            dateStr = dateStr.substring(0, 4) + "0101";
                            dateObj = UIHelper.convertIntToDate(Integer.parseInt(dateStr)); 
                            datePairArg.second = "3";
                            
                        } else if (fndInx == 2)
                        {
                            dateStr = dateStr.substring(0, 6) + "01";
                            dateObj = UIHelper.convertIntToDate(Integer.parseInt(dateStr)); 
                            datePairArg.second = "2";
                            
                        } else
                        {
                            dateObj = UIHelper.convertIntToDate((Integer)data);
                            datePairArg.second = "1";
                        }
                    } else
                    {
                        dateObj = UIHelper.convertIntToDate((Integer)data); 
                        datePairArg.second = "1";
                    }
                    datePairArg.first = dateObj == null ? "NULL" : (includeQuotes ? "\"" : "") + dateFormatter.format(dateObj) + (includeQuotes ? "\"" : "");
                    
                } else 
                {
                    log.error("Partial Date was't 8 digits! ["+dateStr+"]");
                }
            } else
            {
                datePairArg.first  = "NULL";
                datePairArg.second = "1";
            }
        }
    }

    
    /**
     * Takes a list of names and creates a string with the names comma separated
     * @param list the list of names (or field names)
     * @return the string of comma separated names
     */
    public static String buildSelectFieldList(final List<String> list, final String tableName)
    {
        StringBuffer str = new StringBuffer();
        for (int i=0;i<list.size();i++)
        {
            if (i > 0) str.append(", ");

            if (tableName != null)
            {
                str.append(tableName);
                str.append('.');
            }

            str.append(list.get(i));
        }
        return str.toString();
    }

    /**
     * Takes a list of names and creates a string with the names comma separated
     * @param list the list of names (or field names)
     * @return the string of comma separated names
     */
    public static String buildSelectFieldMetaDataList(final List<FieldMetaData> list, final String tableName)
    {
        StringBuffer str = new StringBuffer();
        for (int i=0;i<list.size();i++)
        {
            if (i > 0) str.append(", ");

            if (tableName != null)
            {
                str.append(tableName);
                str.append('.');
            }

            str.append(list.get(i).getName());
        }
        return str.toString();
    }

    /**
     * Creates a mapping of the new name to the old name
     * @param pairs array of pairs of names
     * @return the map object
     */
    public static Map<String, String> createFieldNameMap(String[] pairs)
    {
        Map<String, String> map = new Hashtable<String, String>();

        for (int i=0;i<pairs.length;i++)
        {
            map.put(pairs[i], pairs[i+1]);
            i++;
        }
        return map;
    }

    /**
     * Returns the number of records in a table
     * @param connection db connection
     * @param tableName the name of the table
     * @return the number of records in a table
     */
    public static int getNumRecords(final Connection connection, final String tableName)
    {
        try
        {
            Integer   count = 0;
            Statement cntStmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rs      = cntStmt.executeQuery("select count(*) from "+tableName);
            if (rs.first())
            {
                count = rs.getInt(1);
                if (count == null)
                {
                    return -1;
                }
            }
            rs.close();
            cntStmt.close();

            return count;

        } catch (SQLException ex)
        {
            log.error(ex);
        	ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
        }
        return -1;
    }
    
    /**
     * Returns a count of the records query by SQL passed in.
     * @param sql the SQL with a 'count(?)' 
     * @return the number of records or zero
     */
    public static int getNumRecords(final String sql)
    {
        log.debug(sql);
        
        Connection conn    = DBConnection.getInstance().createConnection();
        try
        {
            int count = getNumRecords(sql, conn);
            conn.close();
            return count;
            
        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
            ex.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Returns a count of the records query by SQL passed in.
     * @param sql the SQL with a 'count(?)' 
     * @return the number of records or zero
     */
    public static int getNumRecords(final String sql, final Connection conn)
    {
        Statement  cntStmt = null;
        try
        {
            int count = 0;
            
            
            if (conn != null)
            {
                cntStmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                if (cntStmt != null)
                {
                    ResultSet rs      = cntStmt.executeQuery(sql);
                    if (rs.first())
                    {
                        count = rs.getInt(1);
                    }
                    rs.close();
                }
                    cntStmt.close();
            }
            return count;

        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
            log.error(ex);
            
        } finally
        {
            try
            {
                if (cntStmt != null)
                {
                    cntStmt.close();
                }
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
        return 0;
    }
    
    

    /**
     * Returns the last ID that was inserted into the database
     * @param connection db connection
     * @param tableName the name of the table
     * @param idColName primary key column name
     * @return the last ID that was inserted into the database
     */
    public static int getHighestId(final Connection connection, final String idColName, final String tableName)
    {
        try
        {
            Statement cntStmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            ResultSet rs      = cntStmt.executeQuery("select "+idColName+" from "+tableName+" order by "+idColName+" asc");
            int id = 0;
            if (rs.last())
            {
                id = rs.getInt(1);
            } else
            {
                id = 1;
            }
            rs.close();
            cntStmt.close();

            return id;

        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
            log.error(ex);
        }
        return -1;
    }

    /**
     * @param opt
     * @return
     */
    public static boolean isOptionOn(final int opt)
    {
        return (showErrors & opt) == opt;
    }
    
    /**
     * @param mySQLFormatedStr
     * @param currentServerType
     * @return
     */
    public static String getServerTypeSpecificSQL(final String mySQLFormatedStr, final SERVERTYPE currentServerType)
    {
        String mySQLFormatedString = mySQLFormatedStr;
         if (currentServerType == SERVERTYPE.MS_SQLServer)
        {
            mySQLFormatedString = stripSingleQuotes(mySQLFormatedString);
            mySQLFormatedString = stripEngineCharSet(mySQLFormatedString);
            mySQLFormatedString = stripIntSize(mySQLFormatedString);
        }
        return mySQLFormatedString;
    }
    
    /**
     * @param str
     * @return
     */
    private static String stripSingleQuotes(final String str)
    {
        return str.replace("`", "");
    }
    
    /**
     * @param strArg
     * @return
     */
    private static String stripEngineCharSet(final String strArg)
    {
        String str = strArg;
        str = str.replaceAll("ENGINE=InnoDB", "");
        str = str.replaceAll("DEFAULT CHARSET=latin1", "");
        return str;
    }
    
    /**
     * @param str
     * @return
     */
    private static String stripIntSize(final String str)
    {
        return str.replaceAll("\\(11\\)", "");  
    }
    
    /**
     * @param name
     * @param currentServerType
     * @return
     */
    public static String createIndexFieldStatment(final String name, final SERVERTYPE currentServerType) 
    {
        if (currentServerType == SERVERTYPE.MS_SQLServer)
        {
            return "create INDEX INX_"+name+" ON " + name + " (NewID)";
        }
        else if (currentServerType == SERVERTYPE.MySQL)
        {
            return "alter table "+name+" add index INX_"+name+" (NewID)";
        }
        return "alter table "+name+" add index INX_"+name+" (NewID)";
    }
    
    /**
     * @param connection
     * @param tableName
     * @param currentServerType
     */
    public static void setIdentityInsertONCommandForSQLServer(final Connection connection, 
                                                              final String tableName,
                                                              final SERVERTYPE currentServerType) 
    {
        setIdentityInserCommandForSQLServer(connection, tableName, "ON", currentServerType); 
    }
    
    /**
     * @param connection
     * @param tableName
     * @param currentServerType
     */
    public static void setIdentityInsertOFFCommandForSQLServer(final Connection connection, 
                                                               final String tableName,
                                                               final SERVERTYPE currentServerType) 
    {
        setIdentityInserCommandForSQLServer(connection, tableName, "OFF", currentServerType); 
    }   
    
    /**
     * @param connection
     * @param tableName
     * @param mySwitch
     * @param currentServerType
     */
    public static void setIdentityInserCommandForSQLServer(final Connection connection,
                                                           final String tableName,
                                                           final String mySwitch,
                                                           final SERVERTYPE currentServerType)
    {
        //REQUIRED FOR SQL SERVER IN ORDER TO PROGRAMMATICALLY SET DEFAULT VALUES
        if (currentServerType == SERVERTYPE.MS_SQLServer)
        {
            try
            {
                Statement cntStmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                String str = "SET IDENTITY_INSERT " + tableName + " " + mySwitch;
                cntStmt.execute(str);
                str = "SET QUOTED_IDENTIFIER OFF";
                cntStmt.execute(str);
                cntStmt.close();

            } catch (SQLException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
                log.error(ex);
                ex.printStackTrace();
                System.exit(0);
            }
        }
    }
    
//    public static void setQuotedIdentifierOFFForSQLServer(Connection connection,
//                                                           SERVERTYPE currentServerType)
//    {
//        //      REQUIRED FOR SQL SERVER IN ORDER TO PROGRAMMATICALLY SET DEFAULT VALUES
//        if (currentServerType == SERVERTYPE.MS_SQLServer)
//        {
//            try
//            {
//                Statement cntStmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
//
//                String str = "SET QUOTED_IDENTIFIER OFF";
//                log.debug("executing: " + str);
//                cntStmt.execute(str);
//                cntStmt.close();
//
//            } catch (SQLException ex)
//            {
//                log.error(ex);
//                ex.printStackTrace();
//                System.exit(0);
//            }
//        }
//    }
    public static void removeForeignKeyConstraints(final Connection connection,
                                                   final String tableName,
                                                   final SERVERTYPE currentServerType)
    {
        try
        {
            if (currentServerType == SERVERTYPE.MS_SQLServer)
            {
                Statement cntStmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                String str = "ALTER TABLE " + tableName + " NOCHECK CONSTRAINT ALL";
                cntStmt.execute(str);
                cntStmt.close();
                    
            } else
            {
                Statement cntStmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                String str = "SET FOREIGN_KEY_CHECKS = 0";
                cntStmt.execute(str);
                cntStmt.close();

            }
        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
            log.error("Error encountered trying to turn off foreign key constraints on database");
            log.error(ex);
        }  
    }
    
    /**
     * @param connection
     * @param currentServerType
     */
    public static void removeForeignKeyConstraints(final Connection connection,
                                                   final SERVERTYPE currentServerType)
    {
        try
        {
            if (currentServerType == SERVERTYPE.MS_SQLServer) 
            {
                List<String> myTables = getTableNames(connection);       
                for (Iterator<String> i = myTables.iterator( ); i.hasNext( ); ) 
                {
                    String s = i.next( );
    
                    Statement cntStmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                    String str = "ALTER TABLE "+s+" NOCHECK CONSTRAINT ALL";
                    cntStmt.execute(str);
                    cntStmt.close();
                }
            }
            else
            {
                Statement cntStmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                String str = "SET FOREIGN_KEY_CHECKS = 0";
                cntStmt.execute(str);
                cntStmt.close();
            }
        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BasicSQLUtils.class, ex);
            log.error(ex);
        } 
    }
}
