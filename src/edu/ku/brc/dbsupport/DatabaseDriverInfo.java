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
package edu.ku.brc.dbsupport;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * This class holds the necessary nformation for connecting to JDBC and Hibernate. There is a tstaic function for
 * readng an XML file that contains all the differently supported database drivers and there connection information.
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class DatabaseDriverInfo implements Comparable<DatabaseDriverInfo>
{
    public enum ConnectionType {Create, Open, Close, Opensys}
    
    private static final Logger log  = Logger.getLogger(DatabaseDriverInfo.class);
    
    protected static SoftReference<Vector<DatabaseDriverInfo>> driverList = null;
    
    protected String name;
    protected String driver;
    protected String dialect;
    
    protected Hashtable<ConnectionType, String> connectionFormats = new Hashtable<ConnectionType, String>();
    
    /**
     * Constructor.
     * @param name name of the driver (human readable)
     * @param driver the JDBC Driver Class name
     * @param dialect the Hibernate Dialect Class Name
     */
    public DatabaseDriverInfo(final String name, 
                              final String driver, 
                              final String dialect)
    {
        this.name = name;
        this.driver = driver;
        this.dialect = dialect;
        
    }
    
    /**
     * Adds a Connection Type format string, ignores empty or null values.
     * @param type the type of connection
     * @param connFormatStr the format string
     */
    public void addFormat(final ConnectionType type, final String connFormatStr)
    {
        //System.out.println(name+" "+type+" "+connFormatStr);
        if (type != null && StringUtils.isNotEmpty(connFormatStr))
        {
            connectionFormats.put(type, connFormatStr);
        }
    }
    
    /**
     * Returns the connection string might return null if connection type doesn't exist.
     * @param server the server (machine name or IP addr)
     * @param database the database name
     * @return the full connection string
     */
    public String getConnectionStr(final ConnectionType type, final String server, final String database)
    {
        String connStr = connectionFormats.get(type);
        if (connStr != null)
        {
            if (StringUtils.isNotEmpty(database))
            {
                connStr = connStr.replaceFirst("DATABASE", database); //$NON-NLS-1$
            }
            
            String dataDir = UIRegistry.getAppDataDir() + File.separator + "specify_data";
            connStr = connStr.replaceFirst("DATADIR",  dataDir); //$NON-NLS-1$
            
            return StringUtils.isNotEmpty(server) ? connStr.replaceFirst("SERVER", server) : connStr; //$NON-NLS-1$
        }
        return null;
    }
    /**
     * Returns the connection string might return null if connection type doesn't exist.
     * @param server the server (machine name or IP addr)
     * @param database the database name
     * @return the full connection string
     */
    public String getConnectionStr(final ConnectionType type, 
                                   final String server, 
                                   final String database, 
                                   final String username, 
                                   final String password, 
                                   final String serverType)
    {
        
        String connStr = connectionFormats.get(type);
        if (connStr != null)
        {
            if (serverType.equals("SQLServer")) //$NON-NLS-1$
            {
                if(StringUtils.isNotEmpty(database))
                {
                        connStr = connStr.replaceFirst("DATABASE",  database); //$NON-NLS-1$
                }
                else
                {
                    connStr = connStr.replaceFirst("DATABASE", ""); //$NON-NLS-1$ //$NON-NLS-2$
                }
                connStr = connStr.replaceFirst("USERNAME", username); //$NON-NLS-1$
                connStr = connStr.replaceFirst("PASSWORD", password); //$NON-NLS-1$
                return StringUtils.isNotEmpty(server) ? connStr.replaceFirst("SERVER", server): connStr; //$NON-NLS-1$
            }
            
            String dataDir = UIRegistry.getEmbeddedDBPath();
            if (dataDir != null)
            {
                dataDir = new File(dataDir).getAbsolutePath();
                
                log.debug(dataDir);
                System.err.println(dataDir);
            
                connStr = connStr.replaceFirst("DATADIR",  dataDir); //$NON-NLS-1$
            }
            
            connStr = connStr.replaceFirst("DATABASE", database); //$NON-NLS-1$
            connStr = connStr.replaceFirst("USERNAME", username); //$NON-NLS-1$
            connStr = connStr.replaceFirst("PASSWORD", password); //$NON-NLS-1$
            return StringUtils.isNotEmpty(server) ? connStr.replaceFirst("SERVER", server): connStr; //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Returns the Create connection string and if that doesn't exist then it returns the "Open" connection string which is the default.
     * @param server the server (machine name or IP addr)
     * @param database the database name
     * @return the full connection string
     */
    public String getConnectionCreateOpenStr(final String server, final String database, final String username, final String password, final String serverType)
    {
        String connStr = connectionFormats.get(ConnectionType.Create);
        if (connStr == null)
        {
            connStr = connectionFormats.get(ConnectionType.Open);
        }
        
        if (connStr != null)
        {
            if (serverType.equals("SQLServer")) //$NON-NLS-1$
            {
                if(StringUtils.isNotEmpty(database))
                {
                        connStr = connStr.replaceFirst("DATABASE", database); //$NON-NLS-1$
                }
                else
                {
                    connStr = connStr.replaceFirst("DATABASE", ""); //$NON-NLS-1$ //$NON-NLS-2$
                }
                connStr = connStr.replaceFirst("USERNAME", username); //$NON-NLS-1$
                connStr = connStr.replaceFirst("PASSWORD", password); //$NON-NLS-1$
                return StringUtils.isNotEmpty(server) ? connStr.replaceFirst("SERVER", server): connStr; //$NON-NLS-1$
            }
            
            String dataDir = UIRegistry.getAppDataDir() + File.separator + "specify_data";
            connStr = connStr.replaceFirst("DATADIR",  dataDir); //$NON-NLS-1$
            
            connStr = connStr.replaceFirst("DATABASE", database); //$NON-NLS-1$
            connStr = connStr.replaceFirst("USERNAME", username); //$NON-NLS-1$
            connStr = connStr.replaceFirst("PASSWORD", password); //$NON-NLS-1$
            return StringUtils.isNotEmpty(server) ? connStr.replaceFirst("SERVER", server): connStr; //$NON-NLS-1$
        }        
        return null;
    }
    

//    /**
//     * Returns the Create connection string and if that doesn't exist then it returns the "Open" connection string which is the default.
//     * @param server the server (machine name or IP addr)
//     * @param database the database name
//     * @return the full connection string
//     */
//    public String getConnectionCreateOpenStr(final String server, final String database)
//    {
//        String connStr = connectionFormats.get(ConnectionType.Create);
//        if (connStr == null)
//        {
//            connStr = connectionFormats.get(ConnectionType.Open);
//        }
//        
//        if (connStr != null)
//        {
//            connStr = connStr.replaceFirst("DATABASE", database);
//            return StringUtils.isNotEmpty(server) ? connStr.replaceFirst("SERVER", server) : connStr;
//        }
//        
//        return null;
//    }
    /**
     * Returns the dialect for Hibernate
     * @return the dialect for Hibernate
     */
    public String getDialectClassName()
    {
        return dialect;
    }
    
    /**
     * Returns the driver for JDBC
     * @return the driver for driver
     */
    public String getDriverClassName()
    {
        return driver;
    }
    
    /**
     * Returns the name
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return name;
    }

    /**
     * Comparable interface method
     * @param obj the objec to compare to
     * @return 0 if equals
     */
    public int compareTo(DatabaseDriverInfo obj)
    {
        return name.compareTo(obj.name);
    }
    
    //-------------------------------------------------------------------------------
    //-- Static Methods
    //-------------------------------------------------------------------------------
    
    /**
     * Returns a driver by name from the list of drivers.
     * @param drvName the name of the driver
     * @param dbDrivers the driver list
     * @return the driver info
     */
    public static DatabaseDriverInfo getDriver(final String drvName)
    {
        int inx = Collections.binarySearch(getDriversList(), new DatabaseDriverInfo(drvName, null, null));
        return inx > -1 ? getDriversList().get(inx) : null;
    }
    
    /**
     * Reads in the disciplines file (is loaded when the class is loaded).
     * @return Reads in the disciplines file (is loaded when the class is loaded).
     */
    public static Vector<DatabaseDriverInfo> getDriversList()
    {
        Vector<DatabaseDriverInfo> list = null;
        
        if (driverList != null)
        {
            list = driverList.get();
        }
        
        if (list == null)
        {
            driverList = new SoftReference<Vector<DatabaseDriverInfo>>(loadDatabaseDriverInfo());
        }
        
        return driverList.get();
    }
    
    /**
     * Reads the Form Registry. The forms are loaded when needed and onlu one ViewSet can be the "core" ViewSet which is where most of the forms
     * reside. This could also be thought of as the "default" set of forms.
     * @return the list of info objects
     */
    protected static Vector<DatabaseDriverInfo> loadDatabaseDriverInfo()
    {
        Vector<DatabaseDriverInfo> dbDrivers = new Vector<DatabaseDriverInfo>();
        try
        {
            Element root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath("dbdrivers.xml"))); //$NON-NLS-1$
            if (root != null)
            {
                Hashtable<String, String> hash = new Hashtable<String, String>();

                for ( Iterator<?> i = root.elementIterator( "db" ); i.hasNext(); )  //$NON-NLS-1$
                {
                    Element dbElement = (Element) i.next();
                    String  name      = getAttr(dbElement, "name", null); //$NON-NLS-1$
                    
                    if (hash.get(name) == null)
                    {
                        hash.put(name, name);
                        
                        String driver  = getAttr(dbElement, "driver", null); //$NON-NLS-1$
                        String dialect = getAttr(dbElement, "dialect", null); //$NON-NLS-1$
                        
                       // these can go away once we validate the XML
                        if (StringUtils.isEmpty(driver))
                        {
                            throw new RuntimeException("Driver cannot be null!"); //$NON-NLS-1$
                        }
                        if (StringUtils.isEmpty(driver))
                        {
                            throw new RuntimeException("Dialect cannot be null!"); //$NON-NLS-1$
                        }                       
                        
                        DatabaseDriverInfo drv = new DatabaseDriverInfo(name, driver, dialect);
                        
                        // Load up the Connection Types
                        for ( Iterator<?> connIter = dbElement.elementIterator( "connection" ); connIter.hasNext(); )  //$NON-NLS-1$
                        {
                            Element connElement = (Element) connIter.next();
                            String  typeStr     = getAttr(connElement, "type", null); //$NON-NLS-1$
                            String  connFormat  = connElement.getTextTrim();
                            ConnectionType type = ConnectionType.valueOf(StringUtils.capitalize(typeStr));
                            drv.addFormat(type, connFormat);
                        }
                        
                        if (drv.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, " ", " ", " ", " ", " ") == null) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                        {
                            log.error("Meg might've screwed up generating connection strings, contact her if you get this error"); //$NON-NLS-1$
                            throw new RuntimeException("Dialect ["+name+"] has no 'Open' connection type!"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        
                        dbDrivers.add(drv);
                        
                    } else
                    {
                        log.error("Database Driver Name["+name+"] is in use."); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            } else
            {
                String msg = "The root element for the document was null!"; //$NON-NLS-1$
                log.error(msg);
                throw new ConfigurationException(msg);
            } 
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DatabaseDriverInfo.class, ex);
            ex.printStackTrace();
            log.error(ex);
        }
        Collections.sort(dbDrivers);
        return dbDrivers;
    }
    
    /**
     * Locates a DatabaseDriverInfo object by name.
     * @param dbDrivers a list of drivers
     * @param name the name to look up
     * @return the info object
     */
    public static DatabaseDriverInfo getInfoByName(final Vector<DatabaseDriverInfo> dbDrivers, final String name)
    {
        int inx = Collections.binarySearch(dbDrivers, new DatabaseDriverInfo(name, null, null));
        return inx > -1 ? dbDrivers.get(inx) : null;
    }

}
