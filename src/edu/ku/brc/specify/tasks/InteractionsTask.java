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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.core.ContextMgr;
import edu.ku.brc.specify.core.NavBox;
import edu.ku.brc.specify.core.NavBoxIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.tasks.subpane.SimpleDescPane;
import edu.ku.brc.specify.ui.CommandAction;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import edu.ku.brc.specify.ui.forms.persist.View;

/**
 * This task manages Loans, Gifts, Exchanges and provide actions and forms to do the interactions
 *
 * @author rods
 *
 */
public class InteractionsTask extends BaseTask
{
    private static final Logger log = Logger.getLogger(InteractionsTask.class);

    public static final String INTERACTIONS = "Interactions";

    // Data Members
    protected Vector<NavBoxIFace> extendedNavBoxes = new Vector<NavBoxIFace>();

   /**
     * Default Constructor
     *
     */
    public InteractionsTask()
    {
        super(INTERACTIONS, getResourceString("Interactions"));

        // Temporary
        NavBox navBox = new NavBox(getResourceString("Actions"));
        navBox.add(NavBox.createBtn(getResourceString("Accession"),  "Interactions", IconManager.IconSize.Std16,
                new CreateViewAction(this, null, "Accession", "Edit", Accession.class)));
        navBox.add(NavBox.createBtn(getResourceString("New_Loan"),  name, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("New_Gifts"), name, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("New_Exchange"), name, IconManager.IconSize.Std16));
        navBoxes.addElement(navBox);

        navBox = new NavBox(getResourceString(ReportsTask.REPORTS));
        navBox.add(NavBox.createBtn(getResourceString("All_Overdue_Loans_Report"), ReportsTask.REPORTS, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("All_Open_Loans_Report"), ReportsTask.REPORTS, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("All_Loans_Report"), ReportsTask.REPORTS, IconManager.IconSize.Std16));
        navBoxes.addElement(navBox);
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getNavBoxes()
     */
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

        RecordSetTask rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);

        extendedNavBoxes.addAll(rsTask.getNavBoxes());

        return extendedNavBoxes;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return new SimpleDescPane(title, this, "Please select an Interaction");
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

        list.add(new ToolBarItemDesc(btn));

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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getTaskClass()
     */
    public Class getTaskClass()
    {
        return this.getClass();
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.getAction().equals("NewInteraction"))
        {
            if (cmdAction.getData() instanceof RecordSet)
            {
                addSubPaneToMgr(DataEntryTask.createFormFor(this, name, (RecordSet)cmdAction.getData()));

            } else if (cmdAction.getData() instanceof Object[])
            {
                Object[] dataList = (Object[])cmdAction.getData();
                if (dataList.length != 3)
                {
                    View   view = (View)dataList[0];
                    String mode = (String)dataList[1];
                    String idStr = (String)dataList[2];
                    DataEntryTask.openView(this, view, mode, idStr);

                } else
                {
                    log.error("The Edit Command was sent with an object Array that was not 3 components!");
                }
            } else
            {
                log.error("The Edit Command was sent that didn't have data that was a RecordSet or an Object Array");
            }
        }
    }

    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------


}
