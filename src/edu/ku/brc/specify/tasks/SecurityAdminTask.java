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

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.specify.tasks.subpane.security.SecurityAdminPane;
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
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        
        // check whether user can see the security admin panel
        // other permissions will be checked when the panel is created 
        if (!SecurityMgr.getInstance().checkPermission("Task." + SECURITY_ADMIN, BasicSpPermission.view)) //$NON-NLS-1$
        {
        	return list;
        }

        // else
        String menuTitle = "SecurityAdminTask.SECURITY_TOOLS_MENU"; //$NON-NLS-1$
        String mneu      = "SecurityAdminTask.SECURITY_TOOLS_MNEU"; //$NON-NLS-1$
        String desc      = "SecurityAdminTask.SECURITY_TOOLS_DESC"; //$NON-NLS-1$
        JMenuItem mi     = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null); // I18N
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                SecurityAdminTask.this.requestContext();
            }
        });
        String menuDesc = "AdvMenu/SystemMenu";
        list.add(new MenuItemDesc(mi, menuDesc ));
        return list;

    }  
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        return list;
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
   
    private void processAdminCommands(CommandAction cmdAction)
    {
        log.error("not implemented");         //$NON-NLS-1$
    }
}
