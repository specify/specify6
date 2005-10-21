/* Filename:    $RCSfile: Preference.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
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
package edu.ku.brc.specify.prefs;

import java.util.Enumeration;
import java.util.Hashtable;

import edu.ku.brc.specify.exceptions.UIException;


public class Preference implements PrefIFace 
{
    private Hashtable<String, PrefChangeListenerIFace> prefsChangedListeners = new Hashtable<String, PrefChangeListenerIFace>();
    
    private String name;
    private String value;
    private String valueType;

    public Preference() 
    {
    }
    
    public boolean isGroup()
    {
        return false;
    }

    public String getName() 
    {
        return this.name;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getValue() 
    {
        return this.value;
    }

    public void setValue(String value) 
    {
        this.value = value;
        preferncesHaveChanged();
    }

    public String getValueType() 
    {
        return this.valueType;
    }

    public void setValueType(String valueType) 
    {
        this.valueType = valueType;
    }
    
    /**
     * 
     * @param aName
     * @param aPCL
     * @throws UIException
     */
    public void registerPrefChangeListener(String aName, PrefChangeListenerIFace aPCL) throws UIException
    {
        if (prefsChangedListeners.containsKey(aName))
        {
           throw new UIException("PrefChangeListener with Name["+aName+"] has already been registered."); 
        }
        prefsChangedListeners.put(aName, aPCL);
    }
    
    /**
     * 
     * @param aName
     * @param aPCL
     * @throws UIException
     */
    public void unregisterPrefChangeListener(String aName, PrefChangeListenerIFace aPCL) throws UIException
    {
        PrefChangeListenerIFace acl = prefsChangedListeners.get(aName);
        if (acl == null)
        {
           throw new UIException("Couldn't find PrefChangeListener with Name["+aName+"]."); 
        }
        prefsChangedListeners.remove(acl);
    }
    
    /**
     * Notify all listeners that the prefs have changes
     *
     */
    public void preferncesHaveChanged()
    {
        for (Enumeration e=prefsChangedListeners.elements();e.hasMoreElements();)
        {
            ((PrefChangeListenerIFace)e.nextElement()).prefsChanged(this);
        }
    }    

}
