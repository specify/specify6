/* This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.MasterPasswordMgr;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.specify.tasks.subpane.security.SecurityAdminPane;
import edu.ku.brc.specify.tasks.subpane.security.SecuritySummaryDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIHelper;

/**
 * 
 * @author megkumin & Ricardo
 *
 */
public class SecurityAdminTask extends BaseTask
{
    private static final Logger log            = Logger.getLogger(SecurityAdminTask.class);
    public static final  String SECURITY_ADMIN = "SecurityAdmin"; //$NON-NLS-1$
    protected SubPaneIFace      starterPane    = null;

    public SecurityAdminTask()
    {
    	super(SECURITY_ADMIN, getResourceString(SECURITY_ADMIN));
    	CommandDispatcher.register(SECURITY_ADMIN, this);
    	iconName = "Security";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
    	SecurityAdminPane userGroupAdminPane = new SecurityAdminPane(name, this);
    	userGroupAdminPane.createMainControlUI();
        starterPane = userGroupAdminPane;
        return starterPane;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        menuItems = new Vector<MenuItemDesc>();
        
        // show security summary menu item
    	// no need to check security as everyone can see their own summary... besides it's read only
        String menuTitle = "SecurityAdminTask.SHOW_SECURITY_SUMMARY_MENU"; //$NON-NLS-1$
        String mneu      = "SecurityAdminTask.SHOW_SECURITY_SUMMARY_MNEU"; //$NON-NLS-1$
        String desc      = "SecurityAdminTask.SHOW_SECURITY_SUMMARY_DESC"; //$NON-NLS-1$
        JMenuItem mi     = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null); // I18N
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
            	SecuritySummaryDlg dlg = new SecuritySummaryDlg(null);
            	dlg.setVisible(true);
            }
        });
        String menuDesc = "Specify.SYSTEM_MENU/Specify.COLSETUP_MENU";
        
        MenuItemDesc showSummaryMenuDesc = new MenuItemDesc(mi, menuDesc);
        showSummaryMenuDesc.setPosition(MenuItemDesc.Position.After, getResourceString("SecurityAdminTask.SECURITY_TOOLS_MENU"));

        // check whether user can see the security admin panel
        // other permissions will be checked when the panel is created 
        // XXX RELEASE
        if (!UIHelper.isSecurityOn() || SecurityMgr.getInstance().checkPermission("Task." + SECURITY_ADMIN, BasicSpPermission.view)) //$NON-NLS-1$
        {
            // security tools menu item
            menuTitle = "SecurityAdminTask.SECURITY_TOOLS_MENU"; //$NON-NLS-1$
            mneu      = "SecurityAdminTask.SECURITY_TOOLS_MNEU"; //$NON-NLS-1$
            desc      = "SecurityAdminTask.SECURITY_TOOLS_DESC"; //$NON-NLS-1$
            mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null); // I18N
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    SecurityAdminTask.this.requestContext();
                }
            });
            MenuItemDesc mid = new MenuItemDesc(mi, menuDesc);
            mid.setPosition(MenuItemDesc.Position.After, getResourceString("SystemSetupTask.COLL_CONFIG"));
            mid.setSepPosition(MenuItemDesc.Position.After);
            
            menuItems.add(mid);
            menuItems.add(showSummaryMenuDesc);
        }
        
        menuTitle = "SecurityAdminTask.MASTER_PWD_MENU"; //$NON-NLS-1$
        mneu      = "SecurityAdminTask.MASTER_PWD_MNEU"; //$NON-NLS-1$
        desc      = "SecurityAdminTask.MASTER_PWD_DESC"; //$NON-NLS-1$
        mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null); // I18N
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                MasterPasswordMgr.getInstance().editMasterInfo();
            }
        });
        
        MenuItemDesc mid = new MenuItemDesc(mi, "HELP");
        menuItems.add(mid);
        
        return menuItems;
    }
    
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        // XXX RELEASE
        String user = System.getProperty("user.name");
        if (user.startsWith("rod"))
        {
            toolbarItems = new Vector<ToolBarItemDesc>();
            toolbarItems.add(new ToolBarItemDesc(createToolbarButton("Security", iconName, "")));
        }

        return toolbarItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @SuppressWarnings("unchecked")  //$NON-NLS-1$
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(SECURITY_ADMIN))
        {
            processAdminCommands(cmdAction);     
        } 
    }
   
    private void processAdminCommands(@SuppressWarnings("unused") CommandAction cmdAction)
    {
        log.error("not implemented");         //$NON-NLS-1$
    }
}
