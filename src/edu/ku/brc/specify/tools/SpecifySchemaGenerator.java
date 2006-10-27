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
    
    public synchronized void generateSchema(String hostname, String databaseName) throws Exception
    {
        String dbDriver = "com.mysql.jdbc.Driver";
        String dbDialect = "org.hibernate.dialect.MySQLDialect";
        String connStr = "jdbc:mysql://" + hostname + "/";
        String user = "rods";
        String passwd = "rods";

        dbConn = DBConnection.createInstance(dbDriver, dbDialect, databaseName, connStr, user, passwd);

        dropAndCreateDB(databaseName);
        writeHibPropFile(databaseName);
        doGenSchema();
    }
    
    protected void dropAndCreateDB(final String dbName) throws Exception
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
    
    protected void writeHibPropFile(final String dbName)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("hibernate.dialect=org.hibernate.dialect.MySQLDialect\n");
        sb.append("hibernate.connection.driver_class=com.mysql.jdbc.Driver\n");
        sb.append("hibernate.connection.url=jdbc:mysql://localhost/"+dbName+"\n");
        sb.append("hibernate.connection.username=rods\n");
        sb.append("hibernate.connection.password=rods\n");
        sb.append("hibernate.max_fetch_depth=3\n");
        sb.append("hibernate.connection.pool_size=5\n");
        sb.append("hibernate.cglib.use_reflection_optimizer=true\n");

        try
        {
            XMLHelper.setContents(new File("src" + File.separator + "hibernate.properties"), sb.toString());

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    protected void doGenSchema() throws Exception
    {
        // Let Apache Ant do all of the real work
        Project project = new Project();
        try
        {
            project.init();
            project.setBasedir(".");

            ProjectHelper.getProjectHelper().parse(project, new File("build.xml"));

            project.executeTarget("genschema");

        } catch (BuildException e)
        {
            throw new Exception(e);
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        SpecifySchemaGenerator schemaGen = new SpecifySchemaGenerator();
        schemaGen.generateSchema("localhost", "junkorama");
    }
}
