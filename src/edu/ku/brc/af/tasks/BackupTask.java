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
/**
 * 
 */
package edu.ku.brc.af.tasks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.db.MySQLBackupService;
import edu.ku.brc.ui.UIHelper;

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
        super("", "");
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return null;
    }

    private void doRestore()
    {
    }

    private void doBackup()
    {
        MySQLBackupService backupService = new MySQLBackupService();
        backupService.doBackUp(null);
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        menuItems = new Vector<MenuItemDesc>();
        
        String menuTitle = "BackupTask.BK_MENU"; //$NON-NLS-1$
        String mneu      = "BackupTask.BK_MNEU"; //$NON-NLS-1$
        String desc      = "BackupTask.BK_DESC"; //$NON-NLS-1$
        JMenuItem mi     = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null); // I18N
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                BackupTask.this.doBackup();
            }
        });
        String menuDesc = "AdvMenu/SystemMenu";
        menuItems.add(new MenuItemDesc(mi, menuDesc ));
        return menuItems;

    }  

}
