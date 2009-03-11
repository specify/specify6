/*
     * Copyright (C) 2009  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
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

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
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
public class TreeTaskMgr
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
                             final NavBox unlockNB)
    {
        Collections.sort(treeTasks);
        
        for (BaseTreeTask<?,?,?> treeTask : treeTasks)
        {
            if (treeTask.isTreeOnByDefault())
            {
                DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(treeTask.getTreeClass().getName());
                
                Vector<RolloverCommand> rocs = unlockBtnHash.get(treeTask);
                if (rocs == null)
                {
                    rocs = new Vector<RolloverCommand>();
                    unlockBtnHash.put(treeTask, rocs);
                }
                
                if (treeTask.getTreeEditAction() != null)
                {
                    NavBoxItemIFace nb = BaseTask.makeDnDNavBtn(treeNB, ti.getTitle(), treeTask.getTreeClass().getSimpleName(), null, null, null, false, false);
                    RolloverCommand roc = (RolloverCommand)nb;
                    roc.addActionListener(treeTask.getTreeEditAction());
                    roc.setToolTip(getResourceString("TASK.SHRTDESC." + treeTask.getTreeClass().getSimpleName()));
                    treeNB.add(nb);
                    //rocs.add(roc);
                }
                
                if (treeTask.getTreeDefEditAction() != null)
                {
                    NavBoxItemIFace nb  = BaseTask.makeDnDNavBtn(treeDefNB, ti.getTitle(), treeTask.getTreeClass().getSimpleName(), null, null, null, false, false);
                    RolloverCommand roc = (RolloverCommand)nb;
                    roc.addActionListener(treeTask.getTreeDefEditAction());
                    roc.setToolTip(getResourceString("TASK.SHRTDESC." + treeTask.getTreeDefClass().getSimpleName()));
                    treeDefNB.add(nb);
                    //rocs.add(roc);
                }
                
                if (treeTask.getTreeUnlockAction() != null)
                {
                    NavBoxItemIFace nb  = BaseTask.makeDnDNavBtn(treeDefNB, ti.getTitle(), treeTask.getTreeClass().getSimpleName(), null, null, null, false, false);
                    RolloverCommand roc = (RolloverCommand)nb;
                    roc.addActionListener(treeTask.getTreeUnlockAction());
                    roc.setToolTip(getResourceString("TASK.UNLOCK." + treeTask.getTreeClass().getSimpleName()));
                    unlockNB.add(nb);
                    
                    rocs.add(roc);
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
                    
                    boolean isLocked = TaskSemaphoreMgr.isLocked("def", treeDefTI.getClassObj().getSimpleName(), TaskSemaphoreMgr.SCOPE.Discipline);
                    
                    
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
}
