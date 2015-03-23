/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms;

import java.util.Hashtable;

/**
 * This class is responsible for creating and caching objects that implment the DataSettabable interface.<br>
 * The cache is to support reuse by any number forms that will need it.
 *
 * @code_status Beta
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
        // do nothing
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjectSettableFactory.class, ex);
                System.err.println("Unable to create class["+className+"]");
                ex.printStackTrace(); // XXX FIXME
            }
        }
        return dataSettable;
    }   

}
