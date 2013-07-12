/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.specify.SpecifyUserTypes;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;

/**
 * This manages all the Tree actions for editing the tree, tree def, and for unlock the trees.
 * This enabled the BaseTreeTask to create NavBoxItems for each type of tree.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jan 26, 2009
 *
 */
public class TreeTaskMgr implements CommandListener
{
    private static final int SYNC_INTERVAL = 300; // 5 minutes
    
    private static TreeTaskMgr instance       = new TreeTaskMgr();
    private static Timer       lockCheckTimer = null;
    private static Hashtable<BaseTreeTask<?,?,?>, Vector<RolloverCommand>> unlockBtnHash  = new Hashtable<BaseTreeTask<?,?,?>, Vector<RolloverCommand>>();
    
    private Vector<BaseTreeTask<?, ?, ?>> treeTasks = new Vector<BaseTreeTask<?,?,?>>();
    
    
    static {
            
        lockCheckTimer = new Timer(true); // Daemon Thread
        lockCheckTimer.schedule(new TimerTask() {
            @Override
            public void run() 
            {
                checkLocks();
            }
        }, SYNC_INTERVAL*1000, SYNC_INTERVAL*1000);

        // Add shutdown hook to flush cached prefs on normal termination
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        if (lockCheckTimer != null)
                        {
                            lockCheckTimer.cancel();
                        }
                    }
                });
                return null;
            }
        });
    }
    /**
     * 
     */
    private TreeTaskMgr()
    {
        CommandDispatcher.register(BaseTask.APP_CMD_TYPE, this);
    }
    
    /**
     * @return the instance
     */
    public static TreeTaskMgr getInstance()
    {
        return instance;
    }
    
    
    /**
     * @param treeTask
     */
    public void add(final BaseTreeTask<?,?,?> treeTask)
    {
        treeTasks.add(treeTask);
    }
    
    /**
     * @param treeNB
     * @param treeDefNB
     * @param unlockNB
     */
    public void fillNavBoxes(final NavBox treeNB,
                             final NavBox treeDefNB,
                             final NavBox unlockNB,
                             final NavBox browseNB)
    {
        Collections.sort(treeTasks);
        
        for (BaseTreeTask<?,?,?> treeTask : treeTasks)
        {
            //System.err.println(treeTask);
            
            if (treeTask.isTreeOnByDefault())
            {
                DBTableInfo        treeTI    = DBTableIdMgr.getInstance().getByClassName(treeTask.getTreeClass().getName());
                PermissionSettings treePerms = treeTI.getPermissions();
                
                Action edtTreeAction    = treePerms != null && treePerms.canView() ? treeTask.getTreeEditAction() : null;
                Action edtTreeDefAction = SpecifyUser.isCurrentUserType(SpecifyUserTypes.UserType.Manager) ? treeTask.getTreeDefEditAction() : null;
                Action unlockTreeAction = treeTask.getTreeUnlockAction();
                Action browseTreeAction = treeTask.getTreeBrowseAction();
                
                Vector<RolloverCommand> rocs = null;
                if (edtTreeAction != null || edtTreeDefAction != null || unlockTreeAction != null)
                {
                    rocs = unlockBtnHash.get(treeTask);
                    if (rocs == null)
                    {
                        rocs = new Vector<RolloverCommand>();
                        unlockBtnHash.put(treeTask, rocs);
                    }
                } else
                {
                    continue;
                }
                
                if (edtTreeAction != null)
                {
                    NavBoxItemIFace nb  = BaseTask.makeDnDNavBtn(treeNB, treeTI.getTitle(), treeTask.getTreeClass().getSimpleName(), null, null, null, false, false);
                    RolloverCommand roc = (RolloverCommand)nb;
                    roc.addActionListener(edtTreeAction);
                    roc.setToolTip(getResourceString("TASK.SHRTDESC." + treeTask.getTreeClass().getSimpleName()));
                }
                
                if (edtTreeDefAction != null)
                {
                    NavBoxItemIFace nb  = BaseTask.makeDnDNavBtn(treeDefNB, treeTI.getTitle(), treeTask.getTreeClass().getSimpleName(), null, null, null, false, false);
                    RolloverCommand roc = (RolloverCommand)nb;
                    roc.addActionListener(edtTreeDefAction);
                    roc.setToolTip(getResourceString("TASK.SHRTDESC." + treeTask.getTreeDefClass().getSimpleName()));
                }
                
                if (unlockTreeAction != null)
                {
                    NavBoxItemIFace nb  = BaseTask.makeDnDNavBtn(unlockNB, treeTI.getTitle(), treeTask.getTreeClass().getSimpleName(), null, null, null, false, false);
                    RolloverCommand roc = (RolloverCommand)nb;
                    roc.addActionListener(unlockTreeAction);
                    roc.setToolTip(getResourceString("TASK.UNLOCK." + treeTask.getTreeClass().getSimpleName()));
                    rocs.add(roc);
                }
                
                if (browseTreeAction != null)
                {
                    NavBoxItemIFace nb  = BaseTask.makeDnDNavBtn(browseNB, treeTI.getTitle(), treeTask.getTreeClass().getSimpleName(), null, null, null, false, false);
                    RolloverCommand roc = (RolloverCommand)nb;
                    roc.addActionListener(browseTreeAction);
                    roc.setToolTip(getResourceString("TASK.BROWSE." + treeTask.getTreeClass().getSimpleName()));
                    //rocs.add(roc);
                }
            }
        }
    }
    
    /**
     * Checks the tree locks
     */
    public static void checkLocks()
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                checkLocksInternal();
            }
        });
    }
    
    /**
     * 
     */
    private static void checkLocksInternal()
    {
        synchronized (instance)
        {
            for (BaseTreeTask<?,?,?> treeTask : unlockBtnHash.keySet())
            {
                TreeDefIface<?,?,?> tdi = treeTask.getCurrentTreeDef();
                if (tdi != null)
                {
                    DBTableInfo treeDefTI = DBTableIdMgr.getInstance().getByClassName(tdi.getClass().getName());
                    
                    boolean isLocked = TaskSemaphoreMgr.isLockedOrInUse("def", treeDefTI.getClassObj().getSimpleName(), TaskSemaphoreMgr.SCOPE.Discipline);
                    
                    
                    Vector<RolloverCommand> rocs = unlockBtnHash.get(treeTask);
                    if (rocs != null)
                    {
                        String iconName = isLocked ? "Security" : treeTask.getTreeClass().getSimpleName();
                        ImageIcon icon = IconManager.getIcon(iconName, IconManager.STD_ICON_SIZE);
                        
                        for (RolloverCommand roc : rocs)
                        {
                            roc.setIcon(icon);
                        }
                    }
                }
            }            
        }
    }
    
    
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------


    /**
	 * @return a copy of the treeTasks
	 */
	public Vector<BaseTreeTask<?, ?, ?>> getTreeTasks()
	{
		return new Vector<BaseTreeTask<?,?,?>>(treeTasks);
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.af.ui.CommandListener#doCommand(edu.ku.brc.af.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdActionArg)
    {
        /*CommandAction cmdAction = cmdActionArg;
        if (cmdAction.isType(BaseTask.APP_CMD_TYPE) && 
           (cmdAction.isAction(BaseTask.APP_RESTART_ACT) || cmdAction.isAction(BaseTask.APP_START_ACT)))
        {
            treeNavBox.clear();
            treeDefNavBox.clear();
            unlockNavBox.clear();
            
            
            boolean skip = AppContextMgr.isSecurityOn() && !DBTableIdMgr.getInstance().getByShortClassName(treeClass.getSimpleName()).getPermissions().canView();
            
            log.debug(treeClass.getSimpleName()+"  skip "+skip);
            if (!skip) //if (isTreeOnByDefault())
            {
                TreeTaskMgr.getInstance().fillNavBoxes(treeNavBox, treeDefNavBox, unlockNavBox);
                for (NavBoxItemIFace nbi : treeNavBox.getItems())
                {
                    ((RolloverCommand)nbi.getUIComponent()).addDropDataFlavor(null);
                }
            } 
            
            treeNavBox.setVisible(treeNavBox.getComponentCount() > 0);
            treeDefNavBox.setVisible(treeDefNavBox.getComponentCount() > 0);
            unlockNavBox.setVisible(unlockNavBox.getComponentCount() > 0);

        }*/
    }
}
