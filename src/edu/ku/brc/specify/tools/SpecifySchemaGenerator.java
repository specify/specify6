/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tools;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;


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
    
    public synchronized void generateSchema(final DatabaseDriverInfo dbdriverInfo, 
                                            final String hostname,
                                            final String databaseName,
                                            final String userName,
                                            final String password) throws SQLException
    {
        
        String connectionStr = dbdriverInfo.getConnectionStr(hostname, "");

        dbConn = DBConnection.createInstance(dbdriverInfo.getDriverClassName(), dbdriverInfo.getDialectClassName(), databaseName, connectionStr, userName, password);

        if (!dbdriverInfo.getName().equals("Derby"))
        {
            dropAndCreateDB(databaseName);
        }
        
        doGenSchema(dbdriverInfo,
                    hostname,
                    databaseName,
                    userName,
                    password);
    }

    /**
     * @param dbName
     * @throws SQLException
     */
    protected void dropAndCreateDB(final String dbName) throws SQLException
    {
        Connection connection = dbConn.createConnection();
        Statement stmt = connection.createStatement();
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
     * @param hostname the hostname (localhost)
     * @param databaseName the database name
     * @param user the username
     * @param passwd the password (clear text)
     * @return properties
     */
    protected Properties getHibernateProperties(final DatabaseDriverInfo driverInfo,
                                                final String hostname,
                                                final String databaseName,
                                                final String user,
                                                final String passwd)
    {
        Properties props = new Properties();
        props.setProperty("hibernate.connection.driver_class", driverInfo.getDriverClassName());
        props.setProperty("hibernate.dialect",                 driverInfo.getDialectClassName());
        props.setProperty("hibernate.connection.url",          driverInfo.getConnectionStr(hostname, databaseName));
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
     * @param hostname the hostname (localhost)
     * @param databaseName the database name
     * @param user the username
     * @param passwd the password (clear text)
     */
    protected void doGenSchema(final DatabaseDriverInfo driverInfo,
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
        hibCfg.setProperties(getHibernateProperties(driverInfo, hostname, databaseName, user, passwd));
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
