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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.tasks.subpane.LabelsPane;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.util.Pair;

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
        navMimeDefs     = new ArrayList<Pair<String,String>>(2);
        navMimeDefs.add(new Pair<String,String>(name, REPORTS_MIME));
        navMimeDefs.add(new Pair<String,String>("Labels", LABELS_MIME));

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
        
//        actionNavBox.add(NavBox.createBtnWithTT(getResourceString("Create_New_Report"), name, 
//                getResourceString("CREATE_REPORT_TT"), IconManager.STD_ICON_SIZE, 
//                new ActionListener() {
//
//                    /* (non-Javadoc)
//                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//                     */
//                    //@Override
//                    public void actionPerformed(ActionEvent e)
//                    {
//                        System.out.println("You clicked 'Create New Report'");
//                        CommandAction cmd = new CommandAction(REPORTS, OPEN_EDITOR, SpReport.getClassTableId());
//                        cmd.setProperty("newwizard", "true");
//                        CommandDispatcher.dispatch(cmd);
//                    }
//        }));
                
//        RolloverCommand roc = (RolloverCommand)makeDnDNavBtn(actionNavBox, getResourceString("ReportEditor"), "EditIcon", 
//                getResourceString("EDIT_REPORT_TT"), 
//                new CommandAction(REPORTS, OPEN_EDITOR, SpReport.getClassTableId()), null, true, false);// true means make it draggable
//        roc.addDropDataFlavor(spReportFlavor);
//        roc.addDragDataFlavor(new DataFlavor(SpReport.class, OPEN_EDITOR));
       
        RolloverCommand roc = (RolloverCommand)makeDnDNavBtn(actionNavBox, getResourceString("ReportRunner"), name, 
                getResourceString("RUN_REPORT_TT"), 
                new CommandAction(REPORTS, RUN_REPORT, SpReport.getClassTableId()), null, true, false);// true means make it draggable
        roc.addDropDataFlavor(runReportFlavor);
        roc.addDragDataFlavor(runReportFlavor);   
        
        actionNavBox.add(NavBox.createBtnWithTT(getResourceString("RefreshReports"), name,
                getResourceString("REFRESH_REPORT_TT"), IconManager.STD_ICON_SIZE,
                new ActionListener()
                {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                     */
                    // @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        //this is probably overkill, but doesn't seem to hurt anything and is not slow.
                        AppContextMgr.getInstance().setContext(((SpecifyAppContextMgr)AppContextMgr.getInstance()).getDatabaseName(), 
                                ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getUserName(), 
                                false);
                        
                        refreshCommands();
                    }
                }));
        
        actionNavBox.add(NavBox.createBtnWithTT(getResourceString("ImportReport"), name,
                getResourceString("IMPORT_REPORT_TT"), IconManager.STD_ICON_SIZE,
                new ActionListener()
                {
                    /*
                     * (non-Javadoc)
                     * 
                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                     */
                    // @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        //this is probably overkill, but doesn't seem to hurt anything and is not slow.
                        AppContextMgr.getInstance().setContext(((SpecifyAppContextMgr)AppContextMgr.getInstance()).getUserName(), 
                                ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getDatabaseName(), 
                                false);
                        
                        CommandDispatcher.dispatch(new CommandAction(ReportsBaseTask.REPORTS,
                                ReportsBaseTask.IMPORT, null));
                    }
                }));
    }

    /**
     * @return the initial pane
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        //starterPane = new SimpleDescPane(name, this, "Welcome to Specify's Label Maker");
        LabelsPane labelsPane = new LabelsPane(name, this, null);
        labelsPane.setLabelText("This is the Reports Pane"); // XXX I18N
        starterPane = labelsPane;
        return starterPane;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
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
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        return list;

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    @Override
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
}
