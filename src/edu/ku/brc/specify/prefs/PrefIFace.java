/* Filename:    $RCSfile: PrefIFace.java,v $
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

import java.util.Set;

import edu.ku.brc.specify.exceptions.UIException;

public interface PrefIFace
{
    /**
     * 
     * @return name of group
     */
    public String getName();
    
    /**
     * 
     * @return whether it is a group or not
     */
    public boolean isGroup();
    
    /**
     * 
     * @param aName
     * @param aPCL
     * @throws UIException
     */
    public void registerPrefChangeListener(String aName, PrefChangeListener aPCL) throws UIException;
    
    /**
     * 
     * @param aName
     * @param aPCL
     * @throws UIException
     */
    public void unregisterPrefChangeListener(String aName, PrefChangeListener aPCL) throws UIException;
   
    /**
     * 
     * @return
     */
    public PrefGroupIFace getGroup();

}
