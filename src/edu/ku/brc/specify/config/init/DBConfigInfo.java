/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.config.init;

import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.specify.config.DisciplineType;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 17, 2008
 *
 */
public class DBConfigInfo
{
    protected DatabaseDriverInfo driverInfo;
    protected String hostName; 
    protected String dbName; 
    protected String username; 
    protected String password; 
    protected String firstName; 
    protected String lastName; 
    protected String email;
    protected DisciplineType disciplineType;
    protected String userType = "CollectionManager";
    
    protected String instName;
    protected String instTitle = null;
    
    protected String divName;
    protected String divTitle  = null;
    protected String divAbbrev = null;
    
    protected String collectionPrefix = null;
    protected String collectionName   = null;
    
    public DBConfigInfo(DatabaseDriverInfo driverInfo, 
                        String hostName, 
                        String dbName,
                        String username, 
                        String password, 
                        String firstName, 
                        String lastName, 
                        String email,
                        DisciplineType disciplineType, 
                        String instName, 
                        String divName)
    {
        super();
        this.driverInfo = driverInfo;
        this.hostName = hostName;
        this.dbName = dbName;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.disciplineType = disciplineType;
        this.instName = instName;
        this.divName = divName;
    }

    /**
     * @return the driverInfo
     */
    public DatabaseDriverInfo getDriverInfo()
    {
        return driverInfo;
    }

    /**
     * @return the hostName
     */
    public String getHostName()
    {
        return hostName;
    }

    /**
     * @return the dbName
     */
    public String getDbName()
    {
        return dbName;
    }

    /**
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @return the firstName
     */
    public String getFirstName()
    {
        return firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName()
    {
        return lastName;
    }

    /**
     * @return the email
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * @return the disciplineType
     */
    public DisciplineType getDiscipline()
    {
        return disciplineType;
    }

    /**
     * @return the instName
     */
    public String getInstName()
    {
        return instName;
    }

    /**
     * @return the divName
     */
    public String getDivName()
    {
        return divName;
    }

    /**
     * @return the userType
     */
    public String getUserType()
    {
        return userType;
    }

    /**
     * @return the divTitle
     */
    public String getDivTitle()
    {
        return divTitle;
    }

    /**
     * @return the divAbbrev
     */
    public String getDivAbbrev()
    {
        return divAbbrev;
    }

    /**
     * @return the collectionName
     */
    public String getCollectionName()
    {
        return collectionName;
    }
    

    /**
     * @return the collectionPrefix
     */
    public String getCollectionPrefix()
    {
        return collectionPrefix;
    }
    

    /**
     * @return the instTitle
     */
    public String getInstTitle()
    {
        return instTitle;
    }

    //---------------
    
    /**
     * @param divTitle the divTitle to set
     */
    public void setDivTitle(String divTitle)
    {
        this.divTitle = divTitle;
    }

    /**
     * @param divAbbrev the divAbbrev to set
     */
    public void setDivAbbrev(String divAbbrev)
    {
        this.divAbbrev = divAbbrev;
    }


    /**
     * @param collectionPrefix the collectionPrefix to set
     */
    public void setCollectionPrefix(String collectionPrefix)
    {
        this.collectionPrefix = collectionPrefix;
    }


    /**
     * @param collectionName the collectionName to set
     */
    public void setCollectionName(String collectionName)
    {
        this.collectionName = collectionName;
    }

    /**
     * @param instTitle the instTitle to set
     */
    public void setInstTitle(String instTitle)
    {
        this.instTitle = instTitle;
    }

    
    
}