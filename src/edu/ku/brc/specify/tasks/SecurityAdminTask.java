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

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.specify.tasks.subpane.SecurityAdminPane;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;

/**
 * 
 * @author megkumin & Ricardo
 *
 */
public class SecurityAdminTask extends BaseTask
{
    private static final Logger log            = Logger.getLogger(SecurityAdminTask.class);
    public static final  String SECURITY_ADMIN = "Security";
    protected SubPaneIFace      starterPane    = null;

    public SecurityAdminTask()
    {
    	super(SECURITY_ADMIN, getResourceString(SECURITY_ADMIN));
    	CommandDispatcher.register(SECURITY_ADMIN, this);
    	CommandDispatcher.register(APP_CMD_TYPE, this);    
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
        JMenuItem mi = UIHelper.createMenuItem("Security Tools", "T", "", true, null); // I18N
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                SecurityAdminTask.this.requestContext();
            }
        });
        list.add(new MenuItemDesc(mi, "AdvMenu/SystemMenu"));
        return list;

    }  
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        String label = getResourceString(name);
        String iconName = "SystemSetup"; // temporary
        String hint = getResourceString("SECURITY_ADMIN_HINT");
        ToolBarDropDownBtn btn = createToolbarButton(label, iconName, hint);
        list.add(new ToolBarItemDesc(btn));
        return list;
    } 
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @SuppressWarnings("unchecked") 
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(SECURITY_ADMIN))
        {
            processAdminCommands(cmdAction);     
        } 
    }
   
    private void processAdminCommands(CommandAction cmdAction)
    {
        log.error("not implemented");        
    }
}
