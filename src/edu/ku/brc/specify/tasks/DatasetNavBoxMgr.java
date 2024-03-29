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

import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.ui.*;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.Trash;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: Jul 28, 2011
 *
 */
public class DatasetNavBoxMgr
{
    private static final Logger log = Logger.getLogger(DatasetNavBoxMgr.class);
    
    private final List<WeakReference<DataSetNavBox>> navBoxes =
        new LinkedList<WeakReference<DataSetNavBox>>();

    private final WorkbenchTask workbenchTask;
    
    public DatasetNavBoxMgr(WorkbenchTask workbenchTask)
    {
        this.workbenchTask = workbenchTask;
    }

    /**
     * @param obj
     * @return
     */
    protected boolean shouldAddToNavBox(final Workbench obj) {
    	String srcName = obj.getWorkbenchTemplate().getSrcFilePath();
    	return srcName == null || !srcName.startsWith("<<#spatch#>>");
    }

    //used by defunct SGR Task
    public NavBox createWorkbenchNavBox(String actionType) {
        return createWorkbenchNavBox(actionType, null, null);
    }

    public NavBox createWorkbenchNavBox(String actionType, List<?> list, ActionListener refresher) {
        DataSetNavBox navBox = null;
        
        navBox = new DataSetNavBox(getResourceString("WB_DATASETS"),false,true, actionType);
        if (refresher != null) {
            navBox.insert(NavBox.createBtnWithTT(getResourceString("WB_RefreshDatasets"), "Reload",
                    getResourceString("WB_REFRESH_DATASETS_TT"), IconManager.STD_ICON_SIZE,
                    refresher), false, false, 0);
        }
        for (Object obj : list) {
            if (shouldAddToNavBox((Workbench)obj)) {
                addWorkbenchToNavBox(navBox, (Workbench)obj);
            }
        }

        if (navBox != null) {
            navBoxes.add(new WeakReference<DataSetNavBox>(navBox));
        }

        return navBox;
    }
    

    public void addWorkbench(Workbench workbench) {
        if (shouldAddToNavBox(workbench)) {
        	for (WeakReference<DataSetNavBox> boxRef : navBoxes) if (boxRef.get() != null) {
        		addWorkbenchToNavBox(boxRef.get(), workbench);
        	}
        }
    }
    
    public void removeWorkbench(Workbench workbench)
    {
        for (WeakReference<DataSetNavBox> boxRef : navBoxes) if (boxRef.get() != null)
        {
            NavBox nb = boxRef.get();
            NavBoxItemIFace nbi = WorkbenchTask.getBoxByTitle(nb, workbench.getName());
            if (nbi == null)
            {
                log.error("couldn't find nbi for Workbench ["+workbench.getName()+"]");
                return;
            }
            
            if (nbi instanceof GhostActionable)
            {
                UIRegistry.getGlassPane().remove((GhostActionable)nbi);
                
                nb.remove(nbi);
                
                // XXX this is pathetic and needs to be made generic
                NavBoxMgr.getInstance().validate();
                NavBoxMgr.getInstance().invalidate();
                NavBoxMgr.getInstance().doLayout();
                NavBoxMgr.getInstance().repaint();
                UIRegistry.forceTopFrameRepaint();
            }
        }
    }
    
    private RolloverCommand addWorkbenchToNavBox(DataSetNavBox navBox, final Workbench workbench)
    {
        CommandAction cmd = new CommandAction(navBox.actionType, WorkbenchTask.SELECTED_WORKBENCH, 
                Workbench.getClassTableId());
        RecordSet     rs  = new RecordSet();
        rs.initialize();
        rs.set(workbench.getName(), Workbench.getClassTableId(), RecordSet.GLOBAL);

        rs.addItem(workbench.getWorkbenchId());
        cmd.setProperty("workbench", rs);
        CommandAction deleteCmd = null;
        //if (!AppContextMgr.isSecurityOn() || getPermissions().canDelete())
        if (workbenchTask.isPermitted())
        {
            deleteCmd = new CommandAction(WorkbenchTask.WORKBENCH, WorkbenchTask.DELETE_CMD_ACT, rs);
        }
        final RolloverCommand roc = 
            (RolloverCommand)edu.ku.brc.af.tasks.BaseTask.makeDnDNavBtn(
                    navBox, workbench.getName(), "DataSet16", cmd, 
                    deleteCmd, true, true);// true means make it draggable
        //if (!AppContextMgr.isSecurityOn() || getPermissions().canModify())
        if (workbenchTask.isPermitted())
        {
            roc.setToolTip(getResourceString("WB_CLICK_EDIT_DATA_TT"));
        }
        
        // Drag Flavors
        if (deleteCmd != null)
        {
            roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        }
        roc.addDragDataFlavor(WorkbenchTask.DATASET_FLAVOR);
        
        // Drop Flavors
        //if (!AppContextMgr.isSecurityOn() || getPermissions().canModify())
        if (workbenchTask.isPermitted())  
        {
            roc.addDropDataFlavor(new DataFlavor(Workbench.class, WorkbenchTask.EXPORT_DATA_FILE));
        }
        
        if (workbenchTask.canViewReports())
        {
            roc.addDropDataFlavor(new DataFlavor(Workbench.class, "Report"));
        }
        
        
        JPopupMenu popupMenu = new JPopupMenu();
        String menuTitle = "WB_EDIT_PROPS";
        String mneu = "WB_EDIT_PROPS_MNEU";
        UIHelper.createLocalizedMenuItem(popupMenu, menuTitle, mneu, null, true, new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                workbenchTask.editWorkbenchProps(roc);
                UsageTracker.incrUsageCount("WB.ShowWorkbenchProps");
            }
        });
        menuTitle = "WB_EDIT_DATASET_MAPPING";
        mneu = "WB_EDIT_DATASET_MAPPING_MNEU";
        UIHelper.createLocalizedMenuItem(popupMenu, menuTitle, mneu, null, true, new ActionListener() {
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent e)
            {
                Workbench wb = workbenchTask.getWorkbenchFromCmd(roc.getData(), "WorkbenchEditMapping");
                if (wb != null)
                {
                    UsageTracker.incrUsageCount("WB.EditMappings");
                    workbenchTask.editTemplate(wb.getWorkbenchTemplate());
                }
            }
        });
        menuTitle = "WB_HANDOFF";
        mneu = "WB_PASS_IT_ON";
        UIHelper.createLocalizedMenuItem(popupMenu, menuTitle, mneu, null, true, new ActionListener() {
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent e)
            {
                Workbench wb = workbenchTask.getWorkbenchFromCmd(roc.getData(), "WorkbenchEditMapping");
                if (wb != null)
                {
                    UsageTracker.incrUsageCount("WB.DataSetHandoff");
                    workbenchTask.changeUser(wb);
                }
            }
        });

        //if (!AppContextMgr.isSecurityOn() || getPermissions().canDelete())
        if (workbenchTask.isPermitted())
        {
            popupMenu.addSeparator();
            menuTitle = "Delete";
            mneu = "DELETE_MNEU";
            UIHelper.createLocalizedMenuItem(popupMenu, menuTitle, mneu, null, true,
                    new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            UsageTracker.incrUsageCount("WB.DeletedWorkbench");
                            Object cmdActionObj = roc.getData();
                            if (cmdActionObj != null && cmdActionObj instanceof CommandAction)
                            {
                                CommandAction subCmd = (CommandAction) cmdActionObj;
                                RecordSetIFace recordSet = (RecordSetIFace) subCmd
                                        .getProperty("workbench");
                                if (recordSet != null)
                                {
                                    workbenchTask.deleteWorkbench(recordSet);
                                }
                            }
                        }
                    });
        }
        roc.setPopupMenu(popupMenu);

        NavBox.refresh(navBox);
        
        return roc;
    }

    private static class DataSetNavBox extends NavBox
    {

        public final String actionType;

        public DataSetNavBox(String name, boolean collapsable, boolean scrollable, String actionType)
        {
            super(name, collapsable, scrollable);
            this.actionType = actionType;
        }

        @Override
        public Component insertSorted(NavBoxItemIFace item) {
            int insertionInx = 0;
            if (items.size() > 0) {
                if (items.get(0).getData() instanceof CommandAction && !((CommandAction)items.get(0).getData()).getType().equalsIgnoreCase("workbench")) {
                    insertionInx = -1;
                }
                do {
                    insertionInx++;
                } while (insertionInx < items.size() && item.compareTo(items.get(insertionInx)) >= 0);
            }
            return insert(item, true, false, insertionInx);
        }

    }
}
