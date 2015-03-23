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
package edu.ku.brc.af.tasks;

import java.awt.Color;
import java.util.List;

import javax.swing.JLabel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 2, 2008
 *
 */
public class BackupTask extends BaseTask
{
    /**
     * 
     */
    public BackupTask()
    {
        super("BackupTask", UIRegistry.getResourceString("BackupTask.TITLE"));
        this.iconName = "Backup";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return null;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        /*if (AppContextMgr.isSecurityOn())
        {
            getPermissions(); // Gets the Permissions
            if (permissions != null && permissions.hasNoPerm())
            {
                return null;
            }
        }
        
        String menuDesc = "Specify.SYSTEM_MENU";
        
        menuItems = new Vector<MenuItemDesc>();
        
        if (permissions == null || permissions.canView())
        {
            String    menuTitle = "BackupTask.BK_MENU"; //$NON-NLS-1$
            String    mneu      = "BackupTask.BK_MNEU"; //$NON-NLS-1$
            String    desc      = "BackupTask.BK_DESC"; //$NON-NLS-1$
            JMenuItem mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    BackupServiceFactory.getInstance().doBackUp();
                }
            });
            MenuItemDesc bkMI = new MenuItemDesc(mi, menuDesc);
            bkMI.setSepPosition(MenuItemDesc.Position.Before);
            menuItems.add(bkMI);
        }
        
        if (permissions == null || permissions.canModify())
        {
            String    menuTitle = "BackupTask.RS_MENU"; //$NON-NLS-1$
            String    mneu      = "BackupTask.RS_MNEU"; //$NON-NLS-1$
            String    desc      = "BackupTask.RS_DESC"; //$NON-NLS-1$
            JMenuItem mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    SubPaneMgr.getInstance().aboutToShutdown();
                    SubPaneIFace splash = getSplashPane();
                    SubPaneMgr.getInstance().addPane(splash);
                    SubPaneMgr.getInstance().showPane(splash);
                    BackupServiceFactory.getInstance().doRestore();
                }
            });
            MenuItemDesc rsMI = new MenuItemDesc(mi, menuDesc);
            menuItems.add(rsMI);
        }
        return menuItems;
        */
        return null;
    }  
    
    
    /**
     * @return
     */
    public SubPaneIFace getSplashPane()
    {
        PanelBuilder    display = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "f:p:g,p,150px,f:p:g"));
        CellConstraints cc      = new CellConstraints();

        display.add(new JLabel(IconManager.getIcon("SpecifySplash")), cc.xy(2, 2));
        
        if (UIHelper.getOSType() != UIHelper.OSTYPE.MacOSX)
        {
            display.getPanel().setBackground(Color.WHITE);
        }
        
        return starterPane = new SimpleDescPane(title, this, display.getPanel());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        return new BasicPermisionPanel("BackupEditorPanel.TITLE", "BackupEditorPanel.BKUP", "BackupEditorPanel.RSTR", null, null);
    }

}
