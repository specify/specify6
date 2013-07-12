/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.config.init;

import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.specify.SpecifyUserTypes;
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
    protected String userType = SpecifyUserTypes.UserType.Manager.toString();
    
    protected String instName;
    protected String instTitle = null;
    
    protected String divName;
    protected String divTitle  = null;
    protected String divAbbrev = null;
    
    protected String collectionPrefix = null;
    protected String collectionName   = null;
    
    public DBConfigInfo(final DatabaseDriverInfo driverInfo, 
                        final String hostName, 
                        final String dbName,
                        final String username, 
                        final String password, 
                        final String firstName, 
                        final String lastName, 
                        final String email,
                        final DisciplineType disciplineType, 
                        final String instName, 
                        final String divName)
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
