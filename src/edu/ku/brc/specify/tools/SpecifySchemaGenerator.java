/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tools;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.ui.UIRegistry;


/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class SpecifySchemaGenerator
{
    protected static final Logger log = Logger.getLogger(SpecifySchemaGenerator.class);
            
    protected DBConnection dbConn;
    
    public SpecifySchemaGenerator()
    {
        // do nothing
    }
    
    /*
    public synchronized void generateSchema(String hostname, String databaseName) throws SQLException
    {
        String dbDriver  = "com.mysql.jdbc.Driver";
        String dbDialect = "org.hibernate.dialect.MySQLDialect";
        String connStr   = "jdbc:mysql://" + hostname + "/";
        String user      = "rods";
        String passwd    = "rods";

        dbConn = DBConnection.createInstance(dbDriver, dbDialect, databaseName, connStr, user, passwd);

        dropAndCreateDB(databaseName);
        doGenSchema(dbDriver,dbDialect,hostname,databaseName,user,passwd);
    }*/
    
    /**
     * This Drops (or deletes) the database and creates a new one by generating the schema.
     * @param dbdriverInfo the driver info
     * @param hostname the host name ('localhost')
     * @param databaseName the database name
     * @param userName the username
     * @param password the password
     * @throws SQLException
     */
    public synchronized void generateSchema(final DatabaseDriverInfo dbdriverInfo, 
                                            final String hostname,
                                            final String databaseName,
                                            final String userName,
                                            final String password) throws SQLException
    {
        boolean isDerby = dbdriverInfo.getName().equals("Derby");
        
        // Get the Create OR the Open String
        // Note: Derby local databases have a different connection string for creating verses opening.
        // So we need to get the Create string if it has one (Derby) or the open string if it doesn't
        // Also notice that Derby wants a database name for the initial connection and the others do not
        String connectionStr = dbdriverInfo.getConnectionCreateOpenStr(hostname, isDerby ? databaseName : "");
        
        // For derby the Connection String includes the "create" indicator
        // so we must first drop (delete) the Derby Database
        if (isDerby)
        {
            dropDerbyDatabase(databaseName);
        }

        // Now connect to other databases and "create" the Derby database
        dbConn = DBConnection.createInstance(dbdriverInfo.getDriverClassName(), dbdriverInfo.getDialectClassName(), databaseName, connectionStr, userName, password);

        // Once connected drop the non-Derby databases
        if (!isDerby)
        {
            dropAndCreateDB(dbConn, databaseName);
            
            connectionStr = dbdriverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostname, databaseName);
        }
        
        // Generate the schema
        doGenSchema(dbdriverInfo,
                    connectionStr,
                    hostname,
                    databaseName,
                    userName,
                    password);
        
        // Now switch the Connection String back to Open from Create.
        if (isDerby)
        {
            dbConn.close(); // Derby Only
            
            connectionStr = dbdriverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostname, databaseName);
            
            dbConn = DBConnection.createInstance(dbdriverInfo.getDriverClassName(), dbdriverInfo.getDialectClassName(), databaseName, connectionStr, userName, password);
        }

    }

    /**
     * Drops (deletes the directory) for a local Derby Database.
     * @param databaseName the database name (which is really the directory name).
     */
    protected void dropDerbyDatabase(final String databaseName)
    {
        String derbyDatabasePath = UIRegistry.getJavaDBPath();
        if (StringUtils.isNotEmpty(derbyDatabasePath))
        {
            if (new File(derbyDatabasePath).exists())
            {
                File derbyDBDir = new File(derbyDatabasePath + File.separator + databaseName);
                if (derbyDBDir.exists())
                {
                    try
                    {
                        FileUtils.deleteDirectory(derbyDBDir);
                        return;
                        
                    } catch (IOException ex)
                    {
                        log.error(ex);
                    }
                    throw new RuntimeException("Error deleting directory["+derbyDBDir.getAbsolutePath()+"]");
                }
            }
        } else
        {
            throw new RuntimeException("JavaDB Path System property was null or empty and can't be!");
        }
    }
    
    /**
     * Drop the database via it's connection.
     * @param dbConnection the connection 
     * @param dbName the database name
     * @throws SQLException
     */
    protected void dropAndCreateDB(final DBConnection dbConnection, final String dbName) throws SQLException
    {
        Connection connection = dbConnection.createConnection();
        Statement  stmt       = connection.createStatement();
        try
        {
            log.info("Dropping database "+dbName);
            
            stmt.execute("drop database "+ dbName);
            
            log.info("Dropped database "+dbName);
            
            stmt.close();
            
        } catch (SQLException ex)
        {
            log.info("Database ["+dbName+"] didn't exist.");
        }

        stmt = connection.createStatement();
        
        log.info("Creating database "+dbName);
        
        stmt.execute("create database "+ dbName);
        
        log.info("Created database "+dbName);
        
        stmt.close();
        connection.close();
    }
    
//    protected void writeHibPropFile(final String dbDriver,
//    								final String dbDialect,
//    								final String hostname,
//    								final String databaseName,
//    								final String user,
//    								final String passwd) throws IOException
//    {
//        StringBuilder sb = new StringBuilder();
//        sb.append("hibernate.connection.driver_class="+dbDriver+"\n");
//        sb.append("hibernate.dialect="+dbDialect+"\n");
//        sb.append("hibernate.connection.url=jdbc:mysql://"+hostname+"/"+databaseName+"\n");
//        sb.append("hibernate.connection.username="+user+"\n");
//        sb.append("hibernate.connection.password="+passwd+"\n");
//        sb.append("hibernate.max_fetch_depth=3\n");
//        sb.append("hibernate.connection.pool_size=5\n");
//        sb.append("hibernate.bytecode.use_reflection_optimizer=true\n");
//
//        XMLHelper.setContents(new File("src" + File.separator + "hibernate.properties"), sb.toString());
//    }
    
    /**
     * Creates a properties object with the necessary args for generating the schema.
     * @param driverInfo the driver info to use
     * @param connectionStr the connection string for creating or opening a database
     * @param hostname the hostname (localhost)
     * @param databaseName the database name
     * @param user the username
     * @param passwd the password (clear text)
     * @return properties
     */
    protected Properties getHibernateProperties(final DatabaseDriverInfo driverInfo,
                                                final String connectionStr, // might be a create or an open connection string
                                                final String hostname,
                                                final String databaseName,
                                                final String user,
                                                final String passwd)
    {
        Properties props = new Properties();
        props.setProperty("hibernate.connection.driver_class", driverInfo.getDriverClassName());
        props.setProperty("hibernate.dialect",                 driverInfo.getDialectClassName());
        props.setProperty("hibernate.connection.url",          connectionStr);
        props.setProperty("hibernate.connection.username",     user);
        props.setProperty("hibernate.connection.password",     passwd);
        props.setProperty("hibernate.max_fetch_depth",         "3");
        props.setProperty("hibernate.connection.pool_size",    "5");
        props.setProperty("hibernate.format_sql",              "true");
        
        /*
        for (Object key : props.keySet())
        {
            //log.info(key+"="+props.getProperty((String)key));
            log.error(key+"="+props.getProperty((String)key));
        }
        */
        return props;
    }

    /**
     * Creates the Schema.
     * @param driverInfo the driver info to use
     * @param connectionStr the connection string for creating or opening a database
     * @param hostname the hostname (localhost)
     * @param databaseName the database name
     * @param user the username
     * @param passwd the password (clear text)
     */
    protected void doGenSchema(final DatabaseDriverInfo driverInfo,
                               final String connectionStr, // might be a create or an open connection string
                               final String hostname,
                               final String databaseName,
                               final String user,
                               final String passwd)
    {
//        // Let Apache Ant do all of the real work
//        Project project = new Project();
//        project.init();
//        project.setBasedir(".");
//        ProjectHelper.getProjectHelper().parse(project, new File("build.xml"));
//        project.executeTarget("genschema");
//
        // if we can get this stuff working, we can get rid of using Ant for this purpose
        Configuration hibCfg = new AnnotationConfiguration();
        hibCfg.setProperties(getHibernateProperties(driverInfo, connectionStr, hostname, databaseName, user, passwd));
        hibCfg.configure();
        
        SchemaExport schemaExporter = new SchemaExport(hibCfg);
        schemaExporter.setDelimiter(";");
        
        log.info("Generating schema");
        
        boolean printToScreen = false;
        boolean exportToDb    = true;
        boolean justDrop      = false;
        boolean justCreate    = true;
        log.info("Creating the DB schema");
        schemaExporter.execute(printToScreen, exportToDb, justDrop, justCreate);
        
        log.info("DB schema creation completed");
        
        // log the exceptions that occurred
        List<?> exceptions = schemaExporter.getExceptions();
        for (Object o: exceptions)
        {
            Exception e = (Exception)o;
            log.error(e.getMessage());
        }
    }
    
    public static void main(String[] args) throws SQLException
    {
        SpecifySchemaGenerator schemaGen = new SpecifySchemaGenerator();
        
        DatabaseDriverInfo dbdriverInfo = DatabaseDriverInfo.getDriver("MySQL");

        schemaGen.generateSchema(dbdriverInfo, "localhost", "testdb", "rods", "rods");
    }
}
