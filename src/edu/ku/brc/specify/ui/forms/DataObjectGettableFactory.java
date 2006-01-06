/* Filename:    $RCSfile: DataObjectGettableFactory.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui.forms;

import java.util.*;

/**
 * This class is responsible for creating and caching objects that implment the DataGettabable interface.<br>
 * The cache is to support reuse by any number forms that will need it.
 * 
 * @author rods
 *
 */
public class DataObjectGettableFactory
{
    // Static Data Members
    protected static DataObjectGettableFactory      instance  = new DataObjectGettableFactory();
    
    // Data Members
    protected Hashtable<String, Hashtable<String, DataObjectGettable>> hashTable = new Hashtable<String, Hashtable<String, DataObjectGettable>>();
    
    /**
     * Constructor of singleton
     */
    protected DataObjectGettableFactory()
    {
        
    }
    
    /**
     * Creates a DataGettable from a class name and caches it
     * 
     * @param className the name of the DataGettable implementation class
     * @return the data gettable object
     */
    public static DataObjectGettable get(final String gettableClassName, final String className)
    {
        if (className == null || className.length() == 0) return null;
        
        Hashtable<String, DataObjectGettable> gettableHash = instance.hashTable.get(gettableClassName);
        if (gettableHash == null)
        {
            gettableHash = new Hashtable<String, DataObjectGettable>();
            instance.hashTable.put(gettableClassName, gettableHash);
        }
        
        DataObjectGettable dataGettable = gettableHash.get(className);
        if (dataGettable == null)
        {
            try
            {
                dataGettable = (DataObjectGettable)Class.forName(className).newInstance();
                gettableHash.put(className, dataGettable);
                
            } catch (Exception ex)
            {
                ex.printStackTrace(); // XXX FIXME
            }
        }
        return dataGettable;
    }   

}
