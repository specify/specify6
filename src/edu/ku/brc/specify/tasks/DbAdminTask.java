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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.specify.tasks.subpane.DbAdminPane;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToolBarDropDownBtn;

/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 * Created Date: Feb 5, 2007
 *
 */
public class DbAdminTask extends BaseTask
{
    private static final Logger log = Logger.getLogger(DbAdminTask.class);
    public static final String DB_ADMIN = "Db_Admin";
    public static final String NEW_USER = "Create_New_User";
    public static final String NEW_GROUP = "Create_New_Group";
    // Data Members
    protected Vector<NavBoxIFace> extendedNavBoxes = new Vector<NavBoxIFace>();
    protected NavBox              viewsNavBox      = null;
    protected SubPaneIFace        starterPane      = null;
    
    /**
     * 
     */
    public DbAdminTask()
    {
        super(DB_ADMIN, getResourceString(DB_ADMIN));
        CommandDispatcher.register(DB_ADMIN, this);
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
            
            // Temporary
             NavBox navBox = new NavBox(getResourceString("Actions"));
             navBox.add(NavBox.createBtn(getResourceString(NEW_USER),  name, IconManager.IconSize.Std16, new NavBoxAction(DB_ADMIN, NEW_USER)));
             navBoxes.addElement(navBox);
             navBox.add(NavBox.createBtn(getResourceString(NEW_GROUP),  name, IconManager.IconSize.Std16, new NavBoxAction(DB_ADMIN, NEW_GROUP)));
              navBoxes.addElement(navBox);
        }
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        //return new SimpleDescPane(title, this, "This is the Reports Pane");
        
        DbAdminPane dbAdminPane = new DbAdminPane(name, this);
        //dbAdminPane.setLabelText(getResourceString("db_admin_hint"));
        dbAdminPane.createUI();
        starterPane = dbAdminPane;
        return starterPane;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
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
        String iconName = name;
        String hint = getResourceString("db_admin_hint");
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
        log.debug("Processing Command ["+cmdAction.getType()+"]["+cmdAction.getAction()+"]");
        
        if (cmdAction.isType(DB_ADMIN))
        {
            processAdminCommands(cmdAction);
            
        } else if (cmdAction.isType(APP_CMD_TYPE) && cmdAction.isAction(APP_RESTART_ACT))
        {
            //viewsNavBox.clear();
            //initializeViewsNavBox();
        }
    }
    
    
    private void processAdminCommands(CommandAction cmdAction)
    {
        log.error("not implemented");
        // TODO Auto-generated method stub
        
    }
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }

}
