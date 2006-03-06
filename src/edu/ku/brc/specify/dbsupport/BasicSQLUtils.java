/*
 * Filename:    $RCSfile: BasicSQLUtils.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.3 $
 * Date:        $Date: 2005/10/20 12:53:02 $
 *
 * This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.dbsupport;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.helpers.UIHelper;

/**
 * 
 * @author rods
 *
 */
public class BasicSQLUtils
{
    protected static Log              log               = LogFactory.getLog(BasicSQLUtils.class);
    protected static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static SimpleDateFormat dateFormatter     = new SimpleDateFormat("yyyy-MM-dd");
    protected static boolean          showMappingError  = true;
    
    protected static BasicSQLUtils basicSQLUtils = new  BasicSQLUtils();
    
    /**
     * Singleton 
     */
    protected  BasicSQLUtils()
    {
    }
    
    /**
     * Sets whether mapping column errors should be displayed
     * @param showMappingError true - shows erros, false does not
     */
    public static void setShowMappingError(boolean showMappingError)
    {
        BasicSQLUtils.showMappingError = showMappingError;
    }

    /**
     * Returns whether it should display mapping errors
     * @return whether it should display mapping errors
     */
    public static boolean isShowMappingError()
    {
        return showMappingError;
    }

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
            //log.info(cmdStr);
            return stmt.executeUpdate(cmdStr); 
            
        } catch (Exception ex) 
        {
            //e.printStackTrace();
            log.error(ex);
            log.error(cmdStr+"\n");
        }
        return -1;
    }
    
    /**
     * Deletes all the records from a table
     * @param tableName the name of the table
     * @return the return value from the SQL update statment (or -1 on an exception)
     */
    public static int deleteAllRecordsFromTable(final String tableName)
    {
        return deleteAllRecordsFromTable(DBConnection.getConnection(), tableName);
    }

    /**
     * Deletes all the records from a table
     * @param stmt Statement object to execute the SQL
     * @param tableName the name of the table
     * @return the return value from the SQL update statment (or -1 on an exception)
     */
    public static int deleteAllRecordsFromTable(final Connection connection, final String tableName)
    {
        try
        {
            int count = 0;
            Statement cntStmt = connection.createStatement();
            ResultSet rs      = cntStmt.executeQuery("select count(*) from "+tableName);
            if (rs.first())
            {
                count = rs.getInt(1);
            }
            rs.close();
            
            Statement stmt = connection.createStatement();
            int retVal = exeUpdateCmd(stmt, "delete from "+tableName);
            stmt.clearBatch();
            stmt.close();
            
            log.info("Deleted "+count+" records from "+tableName);
            
            return retVal;
            
        } catch (SQLException ex)
        {
            //e.printStackTrace();
            log.error(ex);
        }
        return -1;
    }
    
    /**
     * Removes all the records from all the tables from the current DBConnection
     */
    public static void cleanAllTables()
    {
        try
        {
            Connection connection = DBConnection.getConnection();
            
            cleanAllTables(connection);
            
            connection.close();
            
        } catch (SQLException ex)
        {
            //e.printStackTrace();
            log.error(ex);
        }
    }
    
    /**
     * Removes all the records from all the tables
     */
    public static void cleanAllTables(final Connection connection)
    {
        try
        {
            Statement  stmt = connection.createStatement();
            
            ResultSet rs = stmt.executeQuery("show tables");
            if (rs.first())
            {
                do
                {
                    String tableName = rs.getString(1);
                    //System.out.println("Deleting Records from "+tableName);
                    deleteAllRecordsFromTable(connection, tableName);
                } while (rs.next());
            }
            rs.close();
            
            stmt.clearBatch();
            stmt.close();
            
        } catch (SQLException ex)
        {
            //e.printStackTrace();
            log.error(ex);
        }
    }
    
    /**
     * Returns a valid String value for an Object, meaning it will put quotes around Strings and ate etc.
     * @param obj the object to convert
     * @return the string representation
     */
    public static String getStrValue(Object obj)
    {
        return getStrValue(obj, null);
    }

    
    /**
     * Returns a valid String value for an Object, meaning it will put quotes around Strings and ate etc.
     * @param obj the object to convert
     * @return the string representation
     */
    public static String getStrValue(final Object obj, final String newFieldType)
    {
        if (obj == null)
        {
            return "NULL";
            
        } else if (obj instanceof String)
        {
            String str = (String)obj;
            if (str.indexOf('"') > -1 || str.indexOf('\\') > -1)
            {
                str = StringEscapeUtils.escapeJava(str);
            }
            return '"'+str+'"';
            
        } else if (obj instanceof Integer)
        {
            if (newFieldType != null && newFieldType.indexOf("date") ==  0)
            {
                return '"'+dateFormatter.format(UIHelper.convertIntToDate((Integer)obj)) + '"';
            } else
            {
                return ((Integer)obj).toString();
            }
            
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
            return obj.toString();
        }
    }
    
   /**
     * Fills the list with all the names of the table
     * @param connection the connection
     * @param tableName the table name
     * @param list the list to be filled
     */
    public static void getFieldNamesFromSchema(final Connection connection, 
                                               final String tableName,
                                               final List<String> list)
    {
        try
        {
            Statement stmt = connection.createStatement();
            ResultSet rs   = stmt.executeQuery("describe "+tableName);
            while (rs.next()) 
            {      
                list.add(rs.getString(1));
            }
            rs.close();
            stmt.close();
            
        } catch (SQLException ex)
        {
            log.error(ex);
        }
    }

    /**
     * Fills the list with FieldMetaData objects for each field in the table
     * @param connection the connection
     * @param tableName the table name
     * @param fieldList the list to be filled with field/type objects (FieldMetaData)
     */
    public static void getFieldMetaDataFromSchema(final Connection          connection, 
                                                  final String              tableName,
                                                  final List<FieldMetaData> fieldList)
    {
        try
        {
            Statement stmt = connection.createStatement();
            ResultSet rs   = stmt.executeQuery("describe "+tableName);
            while (rs.next()) 
            {      
                fieldList.add(basicSQLUtils.new FieldMetaData(rs.getString(1), rs.getString(2)));
            }
            rs.close();
            stmt.close();
            
        } catch (SQLException ex)
        {
            log.error(ex);
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
     * @return true if successful
     */
    public static boolean copyTable(final Connection fromConn,
                                    final Connection toConn,
                                    final String     tableName,
                                    final Map<String, String> colNewToOldMap)
    {
        return copyTable(fromConn, toConn, "select * from " + tableName, tableName, tableName, colNewToOldMap);
    }

    /**
     * Copies a table to a new table of a different name (same schema) within the same DB Connection
     * @param conn a connection to copy from one table to another in the same database
     * @param fromTableName the table name its coming from
     * @param toTableName the table name it is going to
     * @param colNewToOldMap a map of new file names toold file names
     * @return true if successful
     */
    public static boolean copyTable(final Connection conn,
                                    final String     fromTableName,
                                    final String     toTableName,
                                    final Map<String, String> colNewToOldMap)
    {
        return copyTable(conn, conn, "select * from " + fromTableName, fromTableName, toTableName, colNewToOldMap);
    }

    /**
     * Copies from one connect/table to another connection/table.
     *  
     * @param fromConn DB Connection that the data is coming from
     * @param toConnDB Connection that the data is going to
     * @param fromTableName the table name its coming from
     * @param toTableName the table name it is going to
     * @param colNewToOldMap a map of new file names to old file names
     * @return true if successful
     */
    public static boolean copyTable(final Connection fromConn,
                                    final Connection toConn,
                                    final String     sqlStr,
                                    final String     fromTableName,
                                    final String     toTableName,
                                    final Map<String, String> colNewToOldMap)
    {
        String id = "";
        try
        {
            List<FieldMetaData> colMetaData = new ArrayList<FieldMetaData>();
            getFieldMetaDataFromSchema(toConn, toTableName, colMetaData);
            
            Statement stmt = fromConn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlStr);
            ResultSetMetaData rsmd = rs.getMetaData();
            Hashtable<String, Integer> fromHash = new Hashtable<String, Integer>();
            for (int i = 1; i <= rsmd.getColumnCount(); i++)
            {
                fromHash.put(rsmd.getColumnName(i), i);
            }
            // System.out.println("Num Cols: "+rsmd.getColumnCount());
            StringBuffer str = new StringBuffer();
            int count = 0;
            while (rs.next())
            {
                str.setLength(0);
                str.append("INSERT INTO " + toTableName + " VALUES (");
                
                id = rs.getString(1);
                for (int i = 0; i < colMetaData.size(); i++)
                {
                    FieldMetaData fieldMetaData = colMetaData.get(i);
                    String colName = fieldMetaData.getName();
                    
                    Integer index = fromHash.get(colName);
                    if (index == null && colNewToOldMap != null)
                    {
                        String mappedName = colNewToOldMap.get(colName);
                        if (mappedName != null)
                        {
                            index = fromHash.get(mappedName);
                            
                        } else if (showMappingError)
                        {
                            log.error("The name [" + colName + "] was not mapped.");
                        }
                    }
                    
                    if (index != null)
                    {
                        if (i > 0) str.append(", ");
                        Object dataObj = rs.getObject(index);
                        str.append(getStrValue(dataObj, fieldMetaData.getType()));
                        
                    } else
                    {
                        if (showMappingError)
                        {
                            log.error("For Table[" + fromTableName + "] Col Name[" + colMetaData.get(i).getName() + "] was not mapped");
                        }
                        if (i > 0) str.append(", ");
                        str.append("NULL");
                        
                        //rs.close();
                        //stmt.clearBatch();
                        //stmt.close();
                        //return false;
                    }

                }
                str.append(")");
                if (count % 1000 == 0) log.info(toTableName + " processed: " + count);
                Statement updateStatement = toConn.createStatement();
                int retVal = exeUpdateCmd(updateStatement, str.toString());
                updateStatement.clearBatch();
                updateStatement.close();
                if (retVal == -1)
                {
                    rs.close();
                    stmt.clearBatch();
                    stmt.close();
                    return false;
                }
                count++;
                // if (count == 1) break;
            }
            log.info(fromTableName + " processed " + count + " records.");

            rs.close();
            stmt.clearBatch();
            stmt.close();
            
        } catch (SQLException ex)
        {
            //e.printStackTrace();
            log.error(ex);
            log.error("ID: " + id);
        }
        return true;
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
    
    //-----------------------------------------------------------------------
    //-- Inner Classes
    //-----------------------------------------------------------------------
    public class FieldMetaData 
    {
        protected String name;
        protected String type;
        
        public FieldMetaData(String name, String type)
        {
            this.name = name;
            this.type = type;
        }

        public String getName()
        {
            return name;
        }

        public String getType()
        {
            return type;
        }
    }

}
