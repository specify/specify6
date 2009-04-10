/* Copyright (C) 2009, University of Kansas Center for Research
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
 * This class is responsible for creating and caching objects that implment the DataGettabable interface.<br>
 * The cache is to support reuse by any number forms that will need it.
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class DataObjectGettableFactory
{
    // Static Data Members
    protected static final DataObjectGettableFactory instance  = new DataObjectGettableFactory();
    
    // Data Members
    protected Hashtable<String, Hashtable<String, DataObjectGettable>> hashTable = new Hashtable<String, Hashtable<String, DataObjectGettable>>();
    
    /**
     * Constructor of singleton
     */
    protected DataObjectGettableFactory()
    {
        // do nothing
    }
    
    /**
     * Creates a DataGettable from a class name and caches it
     * @param className
     * @param gettableClassName
     * @return the data gettable object
     */
    public static DataObjectGettable get(final String className, final String gettableClassName)
    {
        if (gettableClassName == null || gettableClassName.length() == 0) return null;
        if (className == null)
        {
            throw new RuntimeException("Class Name is null and can't be!");
        }
        
        Hashtable<String, DataObjectGettable> gettableHash = instance.hashTable.get(className);
        if (gettableHash == null)
        {
            gettableHash = new Hashtable<String, DataObjectGettable>();
            instance.hashTable.put(className, gettableHash);
        }
        
        DataObjectGettable dataGettable = gettableHash.get(gettableClassName);
        if (dataGettable == null)
        {
            try
            {
                dataGettable = (DataObjectGettable)Class.forName(gettableClassName).newInstance();
                gettableHash.put(gettableClassName, dataGettable);
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjectGettableFactory.class, ex);
                ex.printStackTrace(); // gettableClassName FIXME
            }
        }
        return dataGettable;
    }   

}
