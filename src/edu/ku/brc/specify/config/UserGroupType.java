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

package edu.ku.brc.specify.config;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.io.FileInputStream;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.helpers.XMLHelper;


/**
 * This class manages all the available User Types.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Mar 2, 2007
 *
 */
public class UserGroupType implements Comparable<UserGroupType>
{
    private static final Logger  log = Logger.getLogger(UserGroupType.class);
    
    // Static SoftReference Data Members
    protected static SoftReference<Vector<UserGroupType>>            userTypeList  = null;
    protected static SoftReference<Hashtable<String, UserGroupType>> userTypeHash  = null;
    
    // Data Members
    protected String  name;
    
    public UserGroupType(final String name)
    {
        this.name  = name;
    }

    public String getName()
    {
        return name;
    }

    public String toString()
    {
        return name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(UserGroupType obj)
    {
        return name.compareTo(obj.name);
    }
    
    //-------------------------------------------------------------------------------
    //-- Static Methods
    //-------------------------------------------------------------------------------
    
    /**
     * Returns a UserType by name.
     * @param name the name of the UserType
     * @return a UserType by name.
     */
    public static UserGroupType getUserType(final String name)
    {
        UserGroupType UserType = getUserTypeHash().get(name);
        if (UserType == null)
        {
            log.error("Couldn't locate UserType["+name+"]");
        }
        return UserType;
    }
    

    /**
     * Returns a UserType by name.
     * @param name the name of the UserType
     * @return a UserType by name.
     */
    public static UserGroupType getByName(final String name)
    {
        for (UserGroupType UserType : getUserTypeList())
        {
            if (name.equals(UserType.getName()))
            {
                return UserType;
            }
        }
        return null;
    }
    
    /**
     * Reads in the user types file (is loaded when the class is loaded).
     */
    public static Vector<UserGroupType> getUserTypeList()
    {
        Vector<UserGroupType> list = null;
        
        if (userTypeList != null)
        {
            list = userTypeList.get();
        }
        
        if (list == null)
        {
            userTypeList = new SoftReference<Vector<UserGroupType>>(loadUserTypeList());
        }
        
        return userTypeList.get();
    }
    
    /**
     * Reads in the user types file (is loaded when the class is loaded).
     */
    protected static Vector<UserGroupType> loadUserTypeList()
    {
        Vector<UserGroupType> list = new Vector<UserGroupType>();
        try
        {
            Element root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath("usertypes.xml")));
            if (root != null)
            {
                for ( Iterator<?> i = root.elementIterator( "UserType" ); i.hasNext(); )
                {
                    Element disciplineNode = (Element) i.next();

                    String  name      = getAttr(disciplineNode, "name", null);
                    UserGroupType UserType = new UserGroupType(name);
                    list.add(UserType);
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
        
        Collections.sort(list);
        return list;
    }

    /**
     * Reads in the user types file (is loaded when the class is loaded).
     */
    public static Hashtable<String, UserGroupType> getUserTypeHash()
    {
        Hashtable<String, UserGroupType> hash = null;
        
        if (userTypeHash != null)
        {
            hash = userTypeHash.get();
        }
        
        if (hash == null)
        {
        	userTypeHash = new SoftReference<Hashtable<String, UserGroupType>>(loadUserTypeHash());
        }
        
        return userTypeHash.get();
    }
    

    /**
     * Reads in the user types file (is loaded when the class is loaded).
     */
    protected static Hashtable<String, UserGroupType> loadUserTypeHash()
    {
        Hashtable<String, UserGroupType> dispHash = new Hashtable<String, UserGroupType>();
        for (UserGroupType UserType : getUserTypeList())
        {
            dispHash.put(UserType.getName(), UserType);
        }
        return dispHash;
    }
}
