/* Filename:    $RCSfile: DataObjectSettableFactory.java,v $
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

import java.util.Hashtable;

/**
 * This class is responsible for creating and caching objects that implment the DataSettabable interface.<br>
 * The cache is to support reuse by any number forms that will need it.
 * 
 * @author rods
 *
 */
public class DataObjectSettableFactory
{
    // Static Data Members
    protected static final DataObjectSettableFactory instance  = new DataObjectSettableFactory();
    
    // Data Members
    protected Hashtable<String, Hashtable<String, DataObjectSettable>> hashTable = new Hashtable<String, Hashtable<String, DataObjectSettable>>();
    
    /**
     * Constructor of singleton
     */
    protected DataObjectSettableFactory()
    {
        
    }
    
    /**
     * Creates a DataSettable from a class name and caches it
     * 
     * @param className the name of the DataSettable implementation class
     * @return the data settable object
     */
    public static DataObjectSettable get(final String settableClassName, final String className)
    {
        if (className == null || className.length() == 0) return null;
        
        Hashtable<String, DataObjectSettable> settableHash = instance.hashTable.get(settableClassName);
        if (settableHash == null)
        {
            settableHash = new Hashtable<String, DataObjectSettable>();
            instance.hashTable.put(settableClassName, settableHash);
        }
        
        DataObjectSettable dataSettable = settableHash.get(className);
        if (dataSettable == null)
        {
            try
            {
                dataSettable = (DataObjectSettable)Class.forName(className).newInstance();
                settableHash.put(className, dataSettable);
                
            } catch (Exception ex)
            {
                ex.printStackTrace(); // XXX FIXME
            }
        }
        return dataSettable;
    }   

}
