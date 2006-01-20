/* Filename:    $RCSfile: PrefsToolbar.java,v $
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

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.ui.RolloverCommand;
import edu.ku.brc.specify.ui.ToolbarLayoutManager;
import edu.ku.brc.specify.ui.UICacheManager;

/**
 * This class simply reads all the prefs and constructs a toolbar with the various icons.
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class PrefsToolbar extends JPanel
{
    private static Log log = LogFactory.getLog(PrefsToolbar.class);
    
    public static final String NAME        = "name";
    public static final String TITLE       = "title";
    public static final String PANEL_CLASS = "panelClass";
    public static final String ICON_PATH   = "iconPath";
           
    protected Preferences                appsNode = UICacheManager.getAppPrefs();
    protected PrefMainPanel              mainPanel;
    protected int                        iconSize     = 24;  // XXX PREF (Possible???)
    
    /**
     * COnstructor
     */
    /**
     * Constructor with the main panel so the icon know how to show their pane
     * 
     * @param mainPanel the main pane that houses all the preference panes
     */
    public PrefsToolbar(final PrefMainPanel mainPanel)
    {
        super(new ToolbarLayoutManager(2,5));
        
        this.mainPanel = mainPanel;
        
        init();
        
    }

    /**
     * Initializes the toolbar with all the icon from all the diffrent groups or sections
     */
    protected void init()
    {
        Preferences appPrefs = UICacheManager.getAppPrefs();
        if (appPrefs == null)
        {
            throw new RuntimeException("The root pref name has not been set.");
        }
        
        try
        {
            // First Get Main Categories
            String[] childrenNames = appPrefs.childrenNames();
            for (String sectionName : childrenNames)
            {
                Preferences section = appPrefs.node(sectionName);
                String  title = sectionName;
                if (title != null)
                {
                    boolean isAppPref = section.getBoolean("isApp", false);
                    if (isAppPref)
                    {
                        loadSectionPrefs(section);
                    }
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Loads a Section or grouping of Prefs
     * @param parentPref the parent pref which is the groups or section
     */
    protected void loadSectionPrefs(final Preferences parentPref)
    {
        try
        {
            String[] childrenNames = parentPref.childrenNames();
            for (String childName : childrenNames)
            {
                Preferences pref  = parentPref.node(childName);
                
                String title      = pref.get(TITLE, null);
                String panelClass = pref.get(PANEL_CLASS, null);
                String iconPath   = pref.get(ICON_PATH, null);
                
                if (title != null && panelClass != null && iconPath != null)
                {
                    ImageIcon icon = new ImageIcon(new URL(iconPath));
                    if (icon.getIconWidth() > iconSize || icon.getIconHeight() > iconSize)
                    {
                        icon = new ImageIcon(icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));  
                    }
                    if (icon == null)
                    {
                        log.error("Icon was created - path["+iconPath+"]");
                    }
                    
                    RolloverCommand btn = new RolloverCommand(getResourceString(title), icon);
                    btn.setOpaque(false);
                    btn.setVerticalLayout(true);
                    
                    try
                    {
                        Class panelClassObj = Class.forName(panelClass);
                        Component comp = (Component)panelClassObj.newInstance(); 
                        mainPanel.addPanel(title, comp);
                        
                        add(btn.getUIComponent());
                        
                    } catch (Exception ex)
                    {
                        log.error(ex); // XXX FIXME
                    }
                    btn.addActionListener(new ShowAction(title)); 
                }
            }
            
        } catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Show a panel by name
     * @param panelName the name of the panel to be shown
     */
    protected void showPanel(final String panelName)
    {
        mainPanel.showPanel(panelName);
    }


    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------
    
 
    /**
     * 
     * Command for showing a pref pane
     *
     */
    class ShowAction implements ActionListener 
    {
        private String panelName;
        
        public ShowAction(final String panelName)
        {
            this.panelName = panelName;
        }
        public void actionPerformed(ActionEvent e) 
        {
            showPanel(panelName);
        }
    }

}
