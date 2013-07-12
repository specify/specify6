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
package edu.ku.brc.af.ui.db;


/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 */
public class DatabaseConnectionProperties
{
    protected String driverType = ""; //$NON-NLS-1$
    protected String host       = ""; //$NON-NLS-1$
    protected String userName   = ""; //$NON-NLS-1$
    protected String password   = ""; //$NON-NLS-1$
    
    /**
     * @param driverClassName
     * @param dialectClassName
     * @param databaseName
     * @param connectionString
     * @param userName
     * @param password
     */
    public DatabaseConnectionProperties( String driverType, String host, String userName, String password)
    {
        super();
        this.driverType = driverType;
        this.host = host;
        this.userName = userName;
        this.password = password;
    }

    /**
     * 
     */
    public DatabaseConnectionProperties()
    {

    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the userName
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    /**
     * @return the driverType
     */
    public String getDriverType()
    {
        return driverType;
    }

    /**
     * @param driverType the driverType to set
     */
    public void setDriverType(String driverType)
    {
        this.driverType = driverType;
    }

    /**
     * @return the host
     */
    public String getHost()
    {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host)
    {
        this.host = host;
    }

}
