/* Filename:    $RCSfile: UICacheManager.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.2 $
 * Date:        $Date: 2005/10/20 12:53:02 $
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
package edu.ku.brc.specify.ui;

import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.exceptions.UIException;

public class UICacheManager
{
    private static Log log               = LogFactory.getLog(UICacheManager.class);
    private static UICacheManager cmdMgr = new UICacheManager();
    
    private Hashtable<String, ActionChangedListener>         actionChangedListeners = new Hashtable<String, ActionChangedListener>();
    private Hashtable<String, Hashtable<String, JComponent>> uiItems                = new Hashtable<String, Hashtable<String, JComponent>>();
    
    private ResourceBundle resourceBundle = null;
    private String         resourceName   = "resources";
    
    private JTextField     statusBarTextField  = null;
    /**
     * Default private constructor for singleton
     *
     */
    private UICacheManager()
    {
        if (resourceBundle == null)
        {
            try {
                // Get the resource bundle for the default locale
                resourceBundle = ResourceBundle.getBundle(resourceName);
            
            } catch (MissingResourceException ex) {
                log.error("Couldn't find Resource Bundle Name["+resourceName+"]", ex);
            }
        }
    }
    
    /**
     * 
     * @return
     */
    public static UICacheManager getInstance()
    {
        return cmdMgr;
    }
    
    /**
     * 
     * @return the current ResourceBundle
     */
    public static ResourceBundle getResourceBundleInternal()
    {
        return UICacheManager.getInstance().getResourceBundle();
    }
    
   
    /**
     * Register an action change listener
     * @param aName
     * @param aACL
     * @throws UIException
     */
    public void registerActionChangedListener(String aName, ActionChangedListener aACL) throws UIException
    {
        if (actionChangedListeners.containsKey(aName))
        {
           throw new UIException("ActionChangedListener with Name["+aName+"] has already been registered."); 
        }
        actionChangedListeners.put(aName, aACL);
    }
    
    /**
     * 
     * @param aName
     * @param aACL
     * @throws UIException
     */
    public void unregisterActionChangedListener(String aName, ActionChangedListener aACL) throws UIException
    {
        ActionChangedListener acl = actionChangedListeners.get(aName);
        if (acl == null)
        {
           throw new UIException("Couldn't find ActionChangedListener with Name["+aName+"]."); 
        }
        actionChangedListeners.remove(acl);
    }
    
    /**
     * 
     * @param aCategory
     * @param aName
     * @param aUIComp
     * @throws UIException
     */
    public void registerUI(String aCategory, String aName, JComponent aUIComp) throws UIException
    {
        Hashtable<String, JComponent> compsHash = uiItems.get(aCategory);
        if (compsHash == null)
        {
            compsHash = new Hashtable<String, JComponent>();
            uiItems.put(aCategory, compsHash);
        }
        if (compsHash.containsKey(aName))
        {
           throw new UIException("UI component with Name["+aName+"] has already been registered to ["+aCategory+"]."); 
        }
        compsHash.put(aName, aUIComp);
    }
    
    /**
     * 
     * @param aCategory
     * @param aName
     * @param aUIComp
     * @throws UIException
     */
    public void unregisterUI(String aCategory, String aName, JComponent aUIComp) throws UIException
    {
        Hashtable<String, JComponent> compsHash = uiItems.get(aCategory);
        if (compsHash == null)
        {
            throw new UIException("Couldn't find UI Category with Name["+aCategory+"]."); 
        }
        JComponent comp = compsHash.get(aName);
        if (comp == null)
        {
           throw new UIException("Couldn't find UI component with Name["+aName+"]."); 
        }
        compsHash.remove(comp);
    }
    
    public ResourceBundle getResourceBundle()
    {
        return resourceBundle;
    }

    /**
     * @return Returns the reourceName.
     */
    public String getResourceName()
    {
        return resourceName;
    }

    /**
     * @param reourceName The reourceName to set.
     */
    public void setResourceName(String resourceName)
    {
        this.resourceName = resourceName;
    }
    
    public String getResourceStringInternal(String aKey)
    {
        try {
            return resourceBundle.getString(aKey);
        } catch (MissingResourceException ex) {
            log.error("Couldn't find key["+aKey+"] in resource bundle.");
            return aKey;
        }
    }

    public static String getResourceString(String aKey)
    {
        return getInstance().getResourceStringInternal(aKey);
    }

    /**
     * @return Returns the statusBarTextField.
     */
    public JTextField getStatusBarTextField()
    {
        return statusBarTextField;
    }

    /**
     * @param statusBarTextField The statusBarTextField to set.
     */
    public void setStatusBarTextField(JTextField statusBarTextField)
    {
        this.statusBarTextField = statusBarTextField;
    }


}
