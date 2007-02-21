/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tools;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.helpers.XMLHelper;

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
    
    public synchronized void generateSchema(String hostname, String databaseName) throws SQLException
    {
        String dbDriver = "com.mysql.jdbc.Driver";
        String dbDialect = "org.hibernate.dialect.MySQLDialect";
        String connStr = "jdbc:mysql://" + hostname + "/";
        String user = "rods";
        String passwd = "rods";

        dbConn = DBConnection.createInstance(dbDriver, dbDialect, databaseName, connStr, user, passwd);

        dropAndCreateDB(databaseName);
        writeHibPropFile(dbDriver,dbDialect,hostname,databaseName,user,passwd);
        doGenSchema();
    }
    
    protected void dropAndCreateDB(final String dbName) throws SQLException
    {
        Connection connection = dbConn.createConnection();
        Statement stmt = connection.createStatement();
        try
        {
            log.info("Dropping database "+dbName);
            stmt.execute("drop database "+ dbName);
            log.info("Dropped database "+dbName);
            
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
    
    protected void writeHibPropFile(final String dbDriver,
    								final String dbDialect,
    								final String hostname,
    								final String databaseName,
    								final String user,
    								final String passwd)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("hibernate.connection.driver_class="+dbDriver+"\n");
        sb.append("hibernate.dialect="+dbDialect+"\n");
        sb.append("hibernate.connection.url=jdbc:mysql://"+hostname+"/"+databaseName+"\n");
        sb.append("hibernate.connection.username="+user+"\n");
        sb.append("hibernate.connection.password="+passwd+"\n");
        sb.append("hibernate.max_fetch_depth=3\n");
        sb.append("hibernate.connection.pool_size=5\n");
        sb.append("hibernate.bytecode.use_reflection_optimizer=true\n");

        try
        {
            XMLHelper.setContents(new File("src" + File.separator + "hibernate.properties"), sb.toString());

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    protected void doGenSchema()
    {
        // Let Apache Ant do all of the real work
        Project project = new Project();
		project.init();
		project.setBasedir(".");
		ProjectHelper.getProjectHelper().parse(project, new File("build.xml"));
		project.executeTarget("genschema");
        
// // if we can get this stuff working, we can get rid of using Ant for this
// purpose
//        Configuration hibCfg = new AnnotationConfiguration();
//        hibCfg.configure();
//        SchemaExport schemaExporter = new SchemaExport(hibCfg);
//        log.error("Generating schema");
//        schemaExporter.execute(false, true, false, false);
//        List<?> exceptions = schemaExporter.getExceptions();
//        for (Object o: exceptions)
//        {
//            Exception e = (Exception)o;
//            log.error(e.getMessage());
//        }
    }
    
    public static void main(String[] args) throws Exception
    {
        SpecifySchemaGenerator schemaGen = new SpecifySchemaGenerator();
        schemaGen.generateSchema("localhost", "junkorama");
    }
}
