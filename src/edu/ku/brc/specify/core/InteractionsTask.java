/* Filename:    $RCSfile: InteractionsTask.java,v $
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
package edu.ku.brc.specify.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.ku.brc.specify.core.subpane.SimpleDescPane;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.TaskPluginable;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import edu.ku.brc.specify.ui.UICacheManager;
import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

/**
 * 
 * @author rods
 * This task manages Loans, Gifts, Exchanges and provide actions and forms to do the interactions
 */
public class InteractionsTask extends BaseTask
{
    /**
     * Default Constructor
     *
     */
    public InteractionsTask()
    {
        super(getResourceString("Interactions"));
        
        // Temporary
        NavBox navBox = new NavBox(getResourceString("Actions"));
        navBox.add(NavBox.createBtn(getResourceString("New_Loan"),  name, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("New_Gifts"), name, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("New_Exchange"), name, IconManager.IconSize.Std16));
        navBoxes.addElement(navBox);
        
        String reports = getResourceString("Reports");
        navBox = new NavBox(reports);
        navBox.add(NavBox.createBtn(getResourceString("All_Overdue_Loans_Report"), reports, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("All_Open_Loans_Report"), reports, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("All_Loans_Report"), reports, IconManager.IconSize.Std16));
        navBoxes.addElement(navBox);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return new SimpleDescPane(name, this, "This is the Interactions Pane");
    }
    
    //-------------------------------------------------------
    // Plugin Interface
    //-------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        ToolBarDropDownBtn btn = createToolbarButton(name, "loans.gif", "interactions_hint");
        
        list.add(new ToolBarItemDesc(btn.getCompleteComp()));
        
        return list;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        return list;
        
    }
    

}
