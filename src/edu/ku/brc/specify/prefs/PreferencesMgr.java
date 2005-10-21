/* Filename:    $RCSfile: PreferencesIFace.java,v $
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

import java.util.*;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.exceptions.UIException;

/**
 * Call Groups no matter of the level must have a unique Name
 * @author rods
 *
 */
public class PreferencesMgr implements PreferencesIFace
{
    private static Log            log      = LogFactory.getLog(PreferencesMgr.class);
    private static PreferencesMgr prefsMgr = new PreferencesMgr();
    
    private Hashtable<String, PrefGroupIFace> allGroups = new Hashtable<String, PrefGroupIFace>();

    private PrefGroupIFace topLevelGroup = null;
    /**
     * Default private constructor for singleton
     *
     */
    private PreferencesMgr()
    {
        topLevelGroup = new PrefGroup("Root"); 
    }
    
    /**
     * 
     * @return
     */
    public static PreferencesMgr getInstance()
    {
        return prefsMgr;
    }
    
    /**
     * Adds a main level Group of preferences
     * @param aGroup
     */
    public void addRootGroup(PrefGroupIFace aGroup)
    {
        if (allGroups.get(aGroup.getName()) == null)
        {
            
        } else
        {
            String msg = "Group by Name["+aGroup.getName()+"] already exists.";
            log.error(msg);
            throw new UIException(msg);
        }
    }
    
    /**
     * 
     * @param aName name of group
     * @return group by name
     */
    public PrefGroupIFace getGroup(String aName)
    {
        return allGroups.get(aName);
    }
    
    //----------------------------------------------------------------------------
    //-- PreferencesIFace Interface
    //----------------------------------------------------------------------------
    public PrefIFace getPref(String aName)
    {
        return null;
    }
    
    public boolean setPref(PrefIFace aPref)
    {
        return false;
    }

    
}
