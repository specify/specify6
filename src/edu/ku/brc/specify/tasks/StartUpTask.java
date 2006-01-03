/* Filename:    $RCSfile: StartUpTask.java,v $
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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.util.List;
import java.util.Vector;

import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.tasks.subpane.StatsPane;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;

/**
 * This task will enable the user to create queries, save them and execute them.
 * 
 * @author rods
 *
 */
public class StartUpTask extends BaseTask
{
    public static final String STARTUP = "Startup";
    
    protected Vector<ToolBarDropDownBtn> tbList = new Vector<ToolBarDropDownBtn>();
    
    
    /**
     * Default Constructor
     *
     */
    public StartUpTask()
    {
        super(STARTUP, getResourceString(STARTUP));
        
        icon = IconManager.getImage(STARTUP, IconManager.IconSize.Std16);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return new StatsPane(title, this, "startup_panel.xml", true, null);
    }
    
    //-------------------------------------------------------
    // Plugin Interface
    //-------------------------------------------------------
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        
        /*ToolBarDropDownBtn btn = createToolbarButton(name, "queryIt.gif", "search_hint");
        if (tbList.size() == 0)
        {
            tbList.add(btn);
        }
        list.add(new ToolBarItemDesc(btn.getCompleteComp()));
        */
        return list;
        
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        
        return list;
        
    }
    
}
