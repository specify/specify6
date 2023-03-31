/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.tasks.subpane.JasperReportsCache;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
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
        super(REPORTS, getResourceString(REPORTS));
		
        JasperReportsCache.clearCache();
        
        iconName      = name;
        defaultFlavor = new DataFlavor(ReportsBaseTask.class, name);
        navMimeDefs     = new ArrayList<Pair<String,String>>(2);
        navMimeDefs.add(new Pair<String,String>(name, REPORTS_MIME));
        navMimeDefs.add(new Pair<String,String>("Labels", LABELS_MIME));
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
               
        RolloverCommand roc = (RolloverCommand)makeDnDNavBtn(actionNavBox, getResourceString("ReportRunner"), name, 
                getResourceString("RUN_REPORT_TT"), 
                new CommandAction(REPORTS, RUN_REPORT, SpReport.getClassTableId()), null, false, false);// true means make it draggable
        roc.addDropDataFlavor(runReportFlavor);
        
//        if (!AppContextMgr.isSecurityOn() || getPermissions().canAdd())
//        {
//            actionNavBox.add(NavBox.createBtnWithTT(getResourceString("ImportReport"), name,
//                getResourceString("IMPORT_REPORT_TT"), IconManager.STD_ICON_SIZE,
//                new ActionListener()
//                {
//                    /*
//                     * (non-Javadoc)
//                     * 
//                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//                     */
//                    // @Override
//                    public void actionPerformed(ActionEvent e)
//                    {
//                        //this is probably overkill, but doesn't seem to hurt anything and is not slow.
//                        ((SpecifyAppContextMgr) AppContextMgr.getInstance()).setContext(
//                                ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getDatabaseName(), 
//                                ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getUserName(), 
//                                true, false);
//                        
//                        CommandDispatcher.dispatch(new CommandAction(ReportsBaseTask.REPORTS,
//                                ReportsBaseTask.IMPORT, null));
//                    }
//                }));
//        }
        
        if (!AppContextMgr.isSecurityOn() || getPermissions().canModify() || getPermissions().canAdd())
        {
            actionNavBox.add(NavBox.createBtnWithTT(getResourceString("RefreshReports"), "Reload",
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
                    	String  alwaysAskPref = "ALWAYS.ASK.COLL"; //$NON-NLS-1$
                    	boolean askForColl    = AppPreferences.getRemote().getBoolean(alwaysAskPref, false);
                    	AppPreferences.getRemote().putBoolean(alwaysAskPref, false);
                    	
                        //this is probably overkill, but doesn't seem to hurt anything and is not slow.
                        ((SpecifyAppContextMgr) AppContextMgr.getInstance()).setContext(((SpecifyAppContextMgr)AppContextMgr.getInstance()).getDatabaseName(), 
                                ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getUserName(), 
                                true,    // startOver
                                false,   // is First Time
                                false);  // promptForCollection
                        
                        AppPreferences.getRemote().putBoolean(alwaysAskPref, askForColl);
                        
                        refreshCommands();
                    }
                }));
        }
    }

    /**
     * @return the initial pane
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return starterPane = StartUpTask.createFullImageSplashPanel(title, this);
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        toolbarItems = new Vector<ToolBarItemDesc>();
        String label = getResourceString(name);
        String hint = getResourceString("reports_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label, iconName, hint);

        toolbarItems.add(new ToolBarItemDesc(btn));
        return toolbarItems;
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
