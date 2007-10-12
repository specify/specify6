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
/**
 * 
 */
package edu.ku.brc.ui.db;

import edu.ku.brc.ui.UIHelper;

/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 */
public class DatabaseConnectionProperties
{
    //protected String driverClassName = "";
    protected String driverType = "";
    //protected String dialectClassName = "";
    //protected String databaseName = "";
    protected String host = "";
    //protected String connectionString = "";
    protected String userName = "";
    protected String password = "";
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
