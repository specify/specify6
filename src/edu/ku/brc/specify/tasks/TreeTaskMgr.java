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

import java.util.Vector;

import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.tasks.BaseTask;
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
    private static TreeTaskMgr instance = new TreeTaskMgr();
    
    private Vector<BaseTreeTask<?, ?, ?>> treeTasks = new Vector<BaseTreeTask<?,?,?>>();
    
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
        for (BaseTreeTask<?,?,?> treeTask : treeTasks)
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(treeTask.getTreeClass().getName());
            
            if (treeTask.getTreeEditAction() != null)
            {
                NavBoxItemIFace nb = BaseTask.makeDnDNavBtn(treeNB, ti.getTitle(), treeTask.getTreeClass().getSimpleName(), null, null, null, false, false);
                RolloverCommand roc = (RolloverCommand)nb;
                roc.addActionListener(treeTask.getTreeEditAction());
                roc.setToolTip(getResourceString("TASK.SHRTDESC." + treeTask.getTreeClass().getSimpleName()));
                treeNB.add(nb);
            }
            
            if (treeTask.getTreeDefEditAction() != null)
            {
                NavBoxItemIFace nb  = BaseTask.makeDnDNavBtn(treeDefNB, ti.getTitle(), treeTask.getTreeClass().getSimpleName(), null, null, null, false, false);
                RolloverCommand roc = (RolloverCommand)nb;
                roc.addActionListener(treeTask.getTreeDefEditAction());
                roc.setToolTip(getResourceString("TASK.SHRTDESC." + treeTask.getTreeDefClass().getSimpleName()));
                treeDefNB.add(nb);
            }
            
            if (treeTask.getTreeUnlockAction() != null)
            {
                NavBoxItemIFace nb  = BaseTask.makeDnDNavBtn(treeDefNB, ti.getTitle(), treeTask.getTreeClass().getSimpleName(), null, null, null, false, false);
                RolloverCommand roc = (RolloverCommand)nb;
                roc.addActionListener(treeTask.getTreeUnlockAction());
                roc.setToolTip(getResourceString("TASK.UNLOCK." + treeTask.getTreeClass().getSimpleName()));
                unlockNB.add(nb);
            }
        }
    }
    
}
