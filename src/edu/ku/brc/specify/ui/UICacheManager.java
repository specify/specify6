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

import java.awt.Component;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.exceptions.UIException;

public class UICacheManager
{
    // Static Data Members
    public static final String FRAME     = "frame";
    public static final String MENUBAR   = "menubar";
    public static final String TOOLBAR   = "toolbar";
    public static final String STATUSBAR = "statusbar";
    public static final String TOPFRAME  = "topframe";
    
    private static Log log               = LogFactory.getLog(UICacheManager.class);
    private static UICacheManager cmdMgr = new UICacheManager();
    
    // Data Members
    protected Hashtable<String, Component> components = new Hashtable<String, Component>();
    
    protected Hashtable<String, ActionChangedListener>         actionChangedListeners = new Hashtable<String, ActionChangedListener>();
    protected Hashtable<String, Hashtable<String, JComponent>> uiItems                = new Hashtable<String, Hashtable<String, JComponent>>();
    
    protected ResourceBundle resourceBundle = null;
    protected String         resourceName   = "resources";
    
    protected SubPaneMgr     subPaneMgr     = null;
    
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
     * @return the singleton instance
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
     * @param resourceName The reourceName to set.
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
     * Registers a uiComp into the applications
     * @param name the name of the UI component to be registered
     * @param uiComp the UI component to be registered
     */
    public void register(final String name, final Component uiComp)
    {
        if (uiComp != null)
        {
            if (components.get(name) == null)
            {
                components.put(name, uiComp);
            } else
            {
                throw new RuntimeException("Registering a uiComp with an existing name["+ name+"]");
            }
        } else
        {
            throw new NullPointerException("Trying to register a null UI Component!");
        }
    }
    
    /**
     * Unregisters a uiComp from the application
     * @param name the name of the UI component to be unregistered
     */
    public void unregister(final String name)
    {
        if (name != null)
        {
            if (components.get(name) != null)
            {
                components.remove(name);
            } else
            {
                throw new RuntimeException("Unregistering a uiComp that has been registered ["+name+"]");
            }
        } else
        {
            throw new NullPointerException("Trying to unregister with a null name!");
        }
    }

    /**
     * Returns a UI component by name
     * @param name the name of the component to be retrieved 
     * @return a UI component by name
     */
    public Component get(final String name)
    {
        return components.get(name);
    }
    
    /**
     * Displays a message in the status bar
     * @param text
     */
    public static void displayStatusBarText(final String text)
    {
        JTextField txtField = ((JTextField)UICacheManager.getInstance().get(STATUSBAR));
        assert txtField != null : "No statusbar has been created!";
       
        txtField.setText(text == null ? "" : text);
       
    }

    /**
     * Displays a message in the status bar
     * @param key the key of the string that is to appear in the status bar. The resource string will be looked up
     */
    public static void displayLocalizedStatusBarText(final String key)
    {
        if (key == null) throw new NullPointerException("Call to displayLocalizedStatusBarText cannot be null!");
        
        String localizedStr = UICacheManager.getInstance().getResourceStringInternal(key);
        assert localizedStr != null : "Localized String for key["+key+"]";
        
        displayStatusBarText(localizedStr);
        
        
    }
    
    /**
     * Convience method for adding a subpane to the subpane manager
     * @param subPane the sub pane to be added
     */
    public static void addSubPane(SubPaneIFace subPane)
    {
        UICacheManager.getInstance().getSubPaneMgr().addPane(subPane);
    }

    /**
     * 
     * @return the sub pane manager
     */
    public SubPaneMgr getSubPaneMgr()
    {
        return subPaneMgr;
    }

    /**
     * 
     * @param subPaneMgr
     */
    public void setSubPaneMgr(SubPaneMgr subPaneMgr)
    {
        this.subPaneMgr = subPaneMgr;
    }
    
    /**
     * repaints the top most frame
     *
     */
    public static void forceTopFrameRepaint()
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() 
            {
                JFrame frame = ((JFrame)UICacheManager.getInstance().get(TOPFRAME));
                assert frame != null : "The top frame has not been registered";
                frame.repaint();
            }
          });
    }


}
