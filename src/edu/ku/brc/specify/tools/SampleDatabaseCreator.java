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
import org.hibernate.Session;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.XMLHelper;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class SampleDatabaseCreator
{
    protected static final Logger log = Logger.getLogger(SampleDatabaseCreator.class);
    
    protected String dbName;
    protected Session session;
    
    public SampleDatabaseCreator(String dbName)
    {
        this.dbName = dbName;

        String dbDriver = "com.mysql.jdbc.Driver";
        String dbDialect = "org.hibernate.dialect.MySQLDialect";
        String connStr = "jdbc:mysql://localhost/"+dbName;
        String user = "rods";
        String passwd = "rods";
        
        DBConnection dbConn = DBConnection.getInstance();

        dbConn.setDriver(dbDriver);
        dbConn.setDialect(dbDialect);
        dbConn.setDatabaseName(dbName);
        dbConn.setConnectionStr(connStr);
        dbConn.setUsernamePassword(user, passwd);

        Connection connection = dbConn.createConnection();
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (SQLException ex)
            {
                // do nothing
            }
        }
    }
    
    public void buildSample(boolean create) throws Exception
    {
        // do nothing for now
        if(create)
        {
            createDB(dbName);
        }
        
        this.session = HibernateUtil.getCurrentSession();
    }
    
    public void createDB(String databaseName) throws Exception
    {
        Connection connection = DBConnection.getInstance().createConnection();
        Statement stmt = connection.createStatement();
        try
        {
            log.info("Dropping database "+databaseName);
            boolean rv = stmt.execute("drop database "+ databaseName);
            if (!rv)
            {
                //throw new RuntimeException("Couldn't drop database "+databaseName);
            }
            log.info("Dropped database "+databaseName);
            
        } catch (SQLException ex)
        {
            log.info("Database ["+databaseName+"] didn't exist.");
        }

        stmt = connection.createStatement();
        boolean rv = stmt.execute("create database "+ databaseName);
        if (!rv)
        {
            //throw new RuntimeException("Couldn't create database "+databaseName);
        }
        log.info("Created database "+databaseName);
        
        stmt.close();
        connection.close();
        
        writeHibPropFile(databaseName);
        doGenSchema();
    }
    

    protected static void writeHibPropFile(final String databaseName)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("hibernate.dialect=org.hibernate.dialect.MySQLDialect\n");
        sb.append("hibernate.connection.driver_class=com.mysql.jdbc.Driver\n");
        sb.append("hibernate.connection.url=jdbc:mysql://localhost/"+databaseName+"\n");
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

    protected static void doGenSchema() throws Exception
    {
        log.info("Starting up ANT for genschema task.");

        // Create a new project, and perform some default initialization
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

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        SampleDatabaseCreator sampleDbCreator = new SampleDatabaseCreator("Sample");
        sampleDbCreator.buildSample(true);
    }

}
