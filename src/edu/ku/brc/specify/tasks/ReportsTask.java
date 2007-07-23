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

import java.awt.datatransfer.DataFlavor;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToolBarDropDownBtn;

/**
 * This task will enable a user to create, and view reports.
 *
 * @code_status Alpha
 *
 * @author rods
 *
 */
public class ReportsTask extends ReportsBaseTask
{

    /**
     * Constructor.
     */
    public ReportsTask()
    {
        super();
        
        name          = "Reports";
        title         = getResourceString(name);
        defaultFlavor = new DataFlavor(ReportsBaseTask.class, name);
        mimeType      = REPORTS_MIME;

        setIcon(this.name);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.ReportsBaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        String taskName = cmdAction.getPropertyAsString("task name");
        if (StringUtils.isEmpty(taskName) || taskName.equals(getName()))
        {
            super.doCommand(cmdAction);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.ReportsBaseTask#preInitialize()
     */
    @Override
    public void preInitialize()
    {
        super.preInitialize();
        
        actionNavBox.add(NavBox.createBtnWithTT(getResourceString("Create_New_Report"), name, getResourceString("CREATE_REPORT_TT"), IconManager.IconSize.Std16, null));
        actionNavBox.add(NavBox.createBtnWithTT(getResourceString("ReportEditor"),  "EditIcon", getResourceString("EDIT_REPORT_TT"), IconManager.IconSize.Std16, new NavBoxAction(name, OPEN_EDITOR))); // I18N
   }

    /**
     * @return the initial pane
     */
    public SubPaneIFace getStarterPane()
    {
        return starterPane = new SimpleDescPane(title, this, "This is the Reports Pane");
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
        String hint = getResourceString("reports_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label, iconName, hint);


        list.add(new ToolBarItemDesc(btn));
        return list;
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
}
