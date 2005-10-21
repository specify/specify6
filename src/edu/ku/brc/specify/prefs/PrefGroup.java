/* Filename:    $RCSfile: PrefGroup.java,v $
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

import java.util.ArrayList;
import java.util.Collection;


public class PrefGroup implements PrefGroupIFace
{
    private Collection<PrefSubGroup> subGroups = new ArrayList<PrefSubGroup>();
    private String name;
   

    public PrefGroup() 
    {
    }

    public String getName() 
    {
        return this.name;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public void addSubGroup(PrefSubGroup aSubGroup)
    {
        subGroups.add(aSubGroup);
    }
    
    public Collection<PrefSubGroup> getSubGroups()
    {
        return subGroups;
    }
    
    public PrefSubGroupIFace getSubGroupByName(String aName)
    {
        for (PrefSubGroup subGroup : subGroups)
        {
            if (subGroup.getName().equals(aName))
            {
                return (PrefSubGroupIFace)subGroup;
            }
        }
        return null;
    }
}
