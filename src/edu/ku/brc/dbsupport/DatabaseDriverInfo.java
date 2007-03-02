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
package edu.ku.brc.dbsupport;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.helpers.XMLHelper;

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
    private static final Logger log  = Logger.getLogger(DatabaseDriverInfo.class);
    
    protected static WeakReference<Vector<DatabaseDriverInfo>> driverList = null;
    
    protected String name;
    protected String driver;
    protected String dialect;
    protected String connectionFormat;
    
    /**
     * Constructor.
     * @param name name of the driver (human readable)
     * @param driver the JDBC Driver Class name
     * @param dialect the Hibernate Dialect Class Name
     * @param connectionFormat the connection format string
     */
    public DatabaseDriverInfo(String name, String driver, String dialect, String connectionFormat)
    {
        this.name = name;
        this.driver = driver;
        this.dialect = dialect;
        this.connectionFormat = connectionFormat;
    }
    
    /**
     * Returns the connection string.
     * @param server the server (machine name or IP addr)
     * @param database the database name
     * @return the full connection string
     */
    public String getConnectionStr(final String server, final String database)
    {
        //if (StringUtils.isEmpty(database))
        //{
        //    return null;
        //}
        
        String connStr = connectionFormat.replaceFirst("DATABASE", database);
        return StringUtils.isNotEmpty(server) ? connStr.replaceFirst("SERVER", server) : connStr;
    }

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
    public static  DatabaseDriverInfo getDriver(final String drvName)
    {
        int inx = Collections.binarySearch(getDriversList(), new DatabaseDriverInfo(drvName, null, null, null));
        return inx == -1 ? null : getDriversList().get(inx);
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
            driverList = new WeakReference<Vector<DatabaseDriverInfo>>(loadDatabaseDriverInfo());
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
            Element root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath("dbdrivers.xml")));
            if (root != null)
            {
                Hashtable<String, String> hash = new Hashtable<String, String>();

                for ( Iterator i = root.elementIterator( "db" ); i.hasNext(); ) 
                {
                    Element dbElement = (Element) i.next();
                    String  name        = getAttr(dbElement, "name", null);
                    if (hash.get(name) == null)
                    {
                        hash.put(name, name);
                        
                        String driver  = getAttr(dbElement, "driver", null);
                        String dialect = getAttr(dbElement, "dialect", null);
                        String connStr = dbElement.element("connection").getTextTrim();
                        
                       // these can go away once we validate the XML
                        if (StringUtils.isEmpty(driver))
                        {
                            throw new RuntimeException("Driver cannot be null!");
                        }
                        if (StringUtils.isEmpty(driver))
                        {
                            throw new RuntimeException("Dialect cannot be null!");
                        }                       
                        if (StringUtils.isEmpty(connStr))
                        {
                            throw new RuntimeException("Connection cannot be null!");
                        }                       
                        DatabaseDriverInfo drv = new DatabaseDriverInfo(name, driver, dialect, connStr);
                        dbDrivers.add(drv);
                        
                    } else
                    {
                        log.error("Database Driver Name["+name+"] is in use.");
                    }
                }
            } else
            {
                String msg = "The root element for the document was null!";
                log.error(msg);
                throw new ConfigurationException(msg);
            } 
        } catch (Exception ex)
        {
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
        int inx = Collections.binarySearch(dbDrivers, new DatabaseDriverInfo(name, null, null, null));
        return inx > -1 ? dbDrivers.get(inx) : null;
    }

}