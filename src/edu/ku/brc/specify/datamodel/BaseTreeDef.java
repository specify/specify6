/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Transient;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.SpecifyUserTypes.UserType;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgrCallerIFace;
import edu.ku.brc.specify.dbsupport.TreeDefStatusMgr;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr.USER_ACTION;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable;
import edu.ku.brc.specify.treeutils.FullNameRebuilder;
import edu.ku.brc.specify.treeutils.NodeNumberer;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public abstract class BaseTreeDef<N extends Treeable<N,D,I>,
                                  D extends TreeDefIface<N,D,I>,
                                  I extends TreeDefItemIface<N,D,I>> extends DataModelObjBase 
                                  implements TreeDefIface<N,D,I>
{
    private static final Logger     log = Logger.getLogger(BaseTreeDef.class);
    
    protected transient DataProviderSessionIFace nodeUpdateSession = null;
    protected transient QueryIFace nodeQ = null;
    protected transient QueryIFace highestNodeQ = null;
    protected transient QueryIFace childrenQ = null;
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#nodeNumbersAreCurrent()
     */
    public boolean getNodeNumbersAreUpToDate()
    {
        return TreeDefStatusMgr.isNodeNumbersAreUpToDate(this);
    }
    
    /**
     * @param rootObj
     * 
     * Builds the queries used during the node number update process.
     */
    protected void buildQueries(final DataModelObjBase rootObj)
    {
        String hql = "select n." + UploadTable.deCapitalize(rootObj.getDataClass().getSimpleName()) + "Id from " 
            + rootObj.getDataClass().getSimpleName() + " n where parentID=:parent";
        childrenQ = this.nodeUpdateSession.createQuery(hql, false);

        hql = "update " + rootObj.getDataClass().getSimpleName() 
            + " set nodeNumber=:node where " + UploadTable.deCapitalize(rootObj.getDataClass().getSimpleName()) + "Id=:id";
        nodeQ = this.nodeUpdateSession.createQuery(hql, false);
        
        hql = "update " + rootObj.getDataClass().getSimpleName() 
            + " set highestChildNodeNumber=:node where " 
            + UploadTable.deCapitalize(rootObj.getDataClass().getSimpleName()) + "ID=:id";
        highestNodeQ = this.nodeUpdateSession.createQuery(hql, false);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#updateAllNodes()
     */
    @SuppressWarnings("unchecked")
    public void updateAllNodesOld(final DataModelObjBase rootObj) throws Exception
    {
        nodeUpdateSession = DataProviderFactory.getInstance().createSession();
        N root = (N)rootObj;
        if (root == null)
        {
            I rootDefItem = getDefItemByRank(0);
            nodeUpdateSession.attach(rootDefItem);
            root = rootDefItem.getTreeEntries().iterator().next();
            nodeUpdateSession.evict(rootDefItem);
        }
        try
        {
            buildQueries((DataModelObjBase )root);
            //But there could be thousands and thousands of records affected within the transaction.
            //SQLServer accessed via ADO would blow up for large transactions  ???
            nodeUpdateSession.beginTransaction();
            
            writeNodeNumber(root.getTreeId(), 1);
            int highestChild = updateAllNodes2(root.getTreeId(), 1);
            writeHighestChildNodeNumber(root.getTreeId(), highestChild);
            
            nodeUpdateSession.commit();
            TreeDefStatusMgr.setNodeNumbersAreUpToDate(this, true);
        }
        catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseTreeDef.class, ex);
            nodeUpdateSession.rollback();
        }
        finally
        {
            nodeUpdateSession.close();
            childrenQ = null;
            nodeQ = null;
            highestNodeQ = null;
            nodeUpdateSession = null;
        }
    }

    public N getTreeRootNode()
    {
        I rootDefItem = getDefItemByRank(0);
        nodeUpdateSession.attach(rootDefItem);
        N result = rootDefItem.getTreeEntries().iterator().next();
        nodeUpdateSession.evict(rootDefItem);
        return result;
    }
    
    /**
     * @param rootId
     * @param rootNodeNumber
     * @return the highestChildNodeNumber for rootId
     * @throws Exception
     * 
     * Recursively walks the tree and numbers nodes.
     */
    protected Integer updateAllNodes2(final Integer rootId, final int rootNodeNumber) throws Exception
    {
        List<?> children = getChildren(rootId);
        int nodeNumber = rootNodeNumber;
        for (Object childId : children)
        {
            writeNodeNumber(childId, nodeNumber+1);
            nodeNumber = updateAllNodes2((Integer)childId, nodeNumber+1);
            writeHighestChildNodeNumber(childId, nodeNumber);
        }
        return nodeNumber;
    }

    /**
     * @param nodeId
     * @return children of parent with nodeId
     */
    protected List<?> getChildren(final int nodeId)
    {
        childrenQ.setParameter("parent", nodeId);
        return childrenQ.list();
    }
    
    /**
     * @param childId
     * @param nodeNumber
     * 
     * Sets the nodeNumber for the item with key childId.
     */
    protected void writeNodeNumber(final Object childId, final Integer nodeNumber)
    {
        nodeQ.setParameter("node", nodeNumber);
        nodeQ.setParameter("id", childId);
        nodeQ.executeUpdate();
    }

    /**
     * @param childId
     * @param nodeNumber
     * 
     * Sets the highestChildNodeNumber for the item with key childId.
     */
    protected void writeHighestChildNodeNumber(final Object childId, final Integer nodeNumber)
    {
        highestNodeQ.setParameter("node", nodeNumber);
        highestNodeQ.setParameter("id", childId);
        highestNodeQ.executeUpdate();
    }

        
    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#getDoNodeUpdates()
     */
    public boolean getDoNodeNumberUpdates()
    {
        return TreeDefStatusMgr.isDoNodeNumberUpdates(this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#setDoNodeUpdates(boolean)
     */
    public void setDoNodeNumberUpdates(final boolean arg)
    {
        TreeDefStatusMgr.setDoNodeNumberUpdates(this, arg);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#isUploadInProgress()
     */
    public boolean isUploadInProgress()
    {
        return TreeDefStatusMgr.isUploadInProgress(this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#setUploadInProgress(boolean)
     */
    public void setUploadInProgress(final boolean arg)
    {
        TreeDefStatusMgr.setUploadInProgress(this, arg);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#setNodeNumbersAreUpToDate(boolean)
     * 
     * locks or unlocks a nodeNumbersAreuptodate semaphore.
     */
    @Override
    public void setNodeNumbersAreUpToDate(final boolean arg) 
    {
    	TreeDefStatusMgr.setNodeNumbersAreUpToDate(this, arg);
//        if (nodeNumbersAreUpToDate == null || !nodeNumbersAreUpToDate.equals(arg))
//        {
//            boolean canSwitch;
//            //seems like a lock is the best way to persist the out-of-date state
//            if (!arg)
//            {
//                TaskSemaphoreMgr.USER_ACTION action = TaskSemaphoreMgr.lock(getNodeNumberUptoDateLockTitle(), getNodeNumberUptoDateLockName(), null, TaskSemaphoreMgr.SCOPE.Discipline, canOverrideLock());
//                canSwitch = action == TaskSemaphoreMgr.USER_ACTION.OK;        
//            }
//            else
//            {
//                if (!TaskSemaphoreMgr.isLocked(getNodeNumberUptoDateLockTitle(), getNodeNumberUptoDateLockName(), TaskSemaphoreMgr.SCOPE.Discipline))
//                {
//                    canSwitch = true;
//                }
//                else
//                {
//                    canSwitch = TaskSemaphoreMgr.unlock(getNodeNumberUptoDateLockTitle(), 
//                            getNodeNumberUptoDateLockName(), TaskSemaphoreMgr.SCOPE.Discipline);
//                }
//            }
//            if (canSwitch)
//            {
//                nodeNumbersAreUpToDate = arg;
//            }
//        }
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.util.Nameable#getName()
     */
    //@Override
    public String getName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Nameable#setName(java.lang.String)
     */
    //@Override
    public void setName(String name)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    public Class<?> getDataClass()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    public Integer getId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
     */
    @Override
    public int getTableId()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#getStandardLevels()
     */
    //@Override
    public List<TreeDefItemStandardEntry> getStandardLevels()
    {
        return new LinkedList<TreeDefItemStandardEntry>();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#getRankIncrement()
     */
    //@Override
    public int getRankIncrement()
    {
        return 1000; //plenty of space for inserts?
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#updateAllFullNames(edu.ku.brc.specify.datamodel.DataModelObjBase)
     */
    @Override
    @SuppressWarnings("unchecked")
	public void updateAllFullNames(DataModelObjBase rootObj, DataProviderSessionIFace session,
			int minRank) throws Exception 
	{
        final FullNameRebuilder<N,D,I> renamer = new FullNameRebuilder<N,D,I>((D )this, session, minRank);
        final JStatusBar nStatusBar = UIRegistry.getStatusBar();
        if (nStatusBar != null)
        {
            nStatusBar.setProgressRange(renamer.getProgressName(), 0, 100);
        }
        
        renamer.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if ("progress".equals(evt.getPropertyName())) 
                        {
                            if (nStatusBar != null)
                            {
                                nStatusBar.setValue(renamer.getProgressName(), (Integer )evt.getNewValue());
                            }
                        }
                    }
                });

        boolean ok = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).displayAgentsLoggedInDlg("BaseTreeDef.TREE_UPDATE_DENIED_TITLE", "BaseTreeDef.OTHER_USERS");
        if (!ok)
        {
            return;
        }
            
        //useGlassPane avoids issues when simpleglasspane is already displayed. no help for normal glass pane yet.
        boolean useGlassPane = !UIRegistry.isShowingGlassPane() && nStatusBar != null;
        try
        {
            if (useGlassPane)
            {
                UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("BaseTreeDef.UPDATING_FULLNAMES", getName()), 24);
            }
            else if (nStatusBar != null)
            {
                UIRegistry.displayLocalizedStatusBarText("BaseTreeDef.UPDATING_FULLNAMES", getName());
            }
            renamer.execute();
            renamer.get();
        }
        catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseTreeDef.class, ex);
            log.error(ex);
            UIRegistry.showLocalizedError("BaseTreeDef.UnableToRename");
            return;
        }
        finally
        {
            if (useGlassPane)
            {
                UIRegistry.clearSimpleGlassPaneMsg();
            }
            else if (nStatusBar != null)
            {
                UIRegistry.displayStatusBarText("");
            }
            if (nStatusBar != null)
            {
                nStatusBar.setProgressDone(renamer.getProgressName());
            }
        }
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#updateAllNodes(edu.ku.brc.specify.datamodel.DataModelObjBase)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void updateAllNodeNumbers(final DataModelObjBase rootObj, final boolean useProgDlg) throws Exception
    {    	
    	final NodeNumberer<N,D,I> nodeNumberer = new NodeNumberer<N,D,I>((D )this);
        final JStatusBar nStatusBar = useProgDlg ? null : UIRegistry.getStatusBar();        
        final ProgressDialog progDlg = nStatusBar != null ? null :
            new ProgressDialog(UIRegistry.getResourceString("BaseTreeDef.UPDATING_TREE_DLG"), false, false);
        if (nStatusBar != null)
        {
            nStatusBar.setProgressRange(nodeNumberer.getProgressName(), 0, 100);
        }
        else
        {
            progDlg.setModal(true);
            progDlg.setProcess(0,100);
            progDlg.setProcessPercent(true);
            progDlg.setDesc(String.format(UIRegistry.getResourceString("BaseTreeDef.UPDATING_TREE"), getName()));
            nodeNumberer.setProgDlg(progDlg);
        }
        
        nodeNumberer.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if ("progress".equals(evt.getPropertyName())) 
                        {
                            if (nStatusBar != null)
                            {
                                nStatusBar.setValue(nodeNumberer.getProgressName(), (Integer )evt.getNewValue());
                            }
                            else
                            {
                                progDlg.setProcess((Integer )evt.getNewValue());
                            }
                        }
                    }
                });

        boolean ok = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).displayAgentsLoggedInDlg("BaseTreeDef.TREE_UPDATE_DENIED_TITLE", "BaseTreeDef.OTHER_USERS");
        if (!ok)
        {
            return;
        }

        setRenumberingNodes(true);
        setNodeNumbersAreUpToDate(false);
        
        if (!TreeDefStatusMgr.isRenumberingNodes(this) || TreeDefStatusMgr.isNodeNumbersAreUpToDate(this))
        {
            //locking issues will hopefully have been made apparent to user during the preceding setXXX calls. 
            UIRegistry.showLocalizedError("BaseTreeDef.UnableToUpdate");
            setRenumberingNodes(false);
            return;
        }
            
        //useGlassPane avoids issues when simpleglasspane is already displayed. no help for normal glass pane yet.
        boolean useGlassPane = !UIRegistry.isShowingGlassPane() && nStatusBar != null;
    	
        if (!TreeDefStatusMgr.lockTree(this, new TaskSemaphoreMgrCallerIFace(){
    	    @Override
    		public TaskSemaphoreMgr.USER_ACTION resolveConflict(SpTaskSemaphore semaphore, 
                    boolean previouslyLocked,
                    String prevLockBy)
    	    {
    	    	boolean okay = UIRegistry.displayConfirm(UIRegistry.getResourceString("BaseTreeDef.TreeLockMsgTitle"), 
    	    				String.format(UIRegistry.getResourceString("BaseTreeDef.TreeLockMsg"), 
    	    						DBTableIdMgr.getInstance().getByClassName(getNodeClass().getName()).getTitle(),
    	    						prevLockBy), 
    	    				UIRegistry.getResourceString("BaseTreeDef.RemoveLock"), 
    	    				UIRegistry.getResourceString("CANCEL"), JOptionPane.WARNING_MESSAGE);
    	    	if (okay)
    	    	{
    	    		return USER_ACTION.Override;
    	    	}
    	    	return USER_ACTION.Error;
    	    }
    		
    	}))
    	{
    		//hopefully lock problems will already have been reported 
    		return; 
    	}
        try
        {
            if (useGlassPane)
            {
                UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("BaseTreeDef.UPDATING_TREE", getName()), 24);
            }
            else if (nStatusBar != null)
            {
                UIRegistry.displayLocalizedStatusBarText("BaseTreeDef.UPDATING_TREE", getName());
            }
            nodeNumberer.execute();
            if (progDlg != null)
            {
                UIHelper.centerAndShow(progDlg);
            }
            setNodeNumbersAreUpToDate(nodeNumberer.get());
        }
        catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseTreeDef.class, ex);
            log.error(ex);
            UIRegistry.showLocalizedError("BaseTreeDef.UnableToUpdate");
            return;
        }
        finally
        {
            setRenumberingNodes(false);
            if (!TreeDefStatusMgr.unlockTree(this))
            {
            	//hopefully problems will already have been reported 
            }
            if (useGlassPane)
            {
                UIRegistry.clearSimpleGlassPaneMsg();
            }
            else if (nStatusBar != null)
            {
                UIRegistry.displayStatusBarText("");
            }
            if (nStatusBar != null)
            {
                nStatusBar.setProgressDone(nodeNumberer.getProgressName());
            }
            else
            {
                progDlg.processDone();
                progDlg.setVisible(false);
                progDlg.dispose();
            }
        }
    }

    /**
     * @param arg the val to set.
     * 
     * Locks or unlocks a "numbering nodes" semaphore.
     */
    public void setRenumberingNodes(boolean arg) 
    {
    	TreeDefStatusMgr.setRenumberingNodes(this, arg);
    }
        
    /**
     * @return true if current user is a manager.
     */
    protected boolean canOverrideLock()
    {
        return SpecifyUser.isCurrentUserType(UserType.Manager);
    }
    
    /**
     * @return true if tree node numbers are up-to date.
     * @throws Exception
     * 
     * Checks to see if a NodeNumbersOutOfDate lock is set. If not returns true.
     * If so, then if user has permission to update the tree, an option is to update or exit specify is presented. If update
     * is selected and succeeds, true is returned. 
     */
    public boolean checkNodeNumbersUpToDate() throws Exception
    {
        boolean result;
        if (TreeDefStatusMgr.isNodeNumbersAreUpToDate(this))
        {
             result = true;
        }
        else
        {
            if (userCanUpdateTree())
            {
                PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, f:p:g, 5dlu", "5dlu, f:p:g, 2dlu, f:p:g, 5dlu"));
                pb.add(new JLabel(String.format(UIRegistry.getResourceString("BaseTreeDef.TREE_UPDATE_REQUIRED1"), getName())), new CellConstraints().xy(2, 2));
                pb.add(new JLabel(UIRegistry.getResourceString("BaseTreeDef.TREE_UPDATE_REQUIRED2")), new CellConstraints().xy(2, 4));
                
                CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(),
                        UIRegistry.getResourceString("BaseTreeDef.TREE_UPDATE_REQUIRED_TITLE"),
                        true,
                        CustomDialog.OKCANCELHELP,
                        pb.getPanel());
                dlg.setCancelLabel(UIRegistry.getResourceString("SpecifyAppContextMgr.EXIT"));
                UIHelper.centerAndShow(dlg);
                if (dlg.getBtnPressed() == CustomDialog.OK_BTN)
                {
                    updateAllNodeNumbers(null, false);
                    result = TreeDefStatusMgr.isNodeNumbersAreUpToDate(this);                    
                }
                else
                {
                    result = false;
                }
            }
            else
            {
                PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, f:p:g, 5dlu", "5dlu, f:p:g, 2dlu, f:p:g, 5dlu"));
                pb.add(new JLabel(String.format(UIRegistry.getResourceString("BaseTreeDef.TREE_UPDATE_REQUIRED1"), getName())), new CellConstraints().xy(2, 2));
                pb.add(new JLabel(UIRegistry.getResourceString("BaseTreeDef.NO_TREE_UPDATE_PERMISSION")), new CellConstraints().xy(2, 4));
                
                CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(),
                        UIRegistry.getResourceString("BaseTreeDef.TREE_UPDATE_REQUIRED_TITLE"),
                        true,
                        CustomDialog.OKHELP,
                        pb.getPanel());
                UIHelper.centerAndShow(dlg);
                result = false;               
           }
        }
        return result;
    }
    
    /**
     * @return true if user has permission to edit tree def.
     */
    protected boolean userCanUpdateTree()
    {
        if (!AppContextMgr.isSecurityOn())
        {
            return true;
        }
        //XXX Need to see if User is in Managers group????? (Something to do with Manager/Full User/Limited User kluge???)

        return DBTableIdMgr.getInstance().getByClassName(getClass().getName()).getPermissions().canModify();
    }
    
    /**
     * @return true if no node numbering lock exists, or if user can override lock.
     */
    public boolean checkNodeRenumberingLock()
    {
    	if (TreeDefStatusMgr.isRenumberingNodes(this))
    	{
    		if (canOverrideLock())
    		{
    			boolean ok = UIRegistry.displayConfirm(UIRegistry.getResourceString("BaseTreeDef.IsNumberingWarnTitle"),	 
    					String.format(UIRegistry.getResourceString("BaseTreeDef.NumberingKillMsg"), 
	    						DBTableIdMgr.getInstance().getByClassName(getNodeClass().getName()).getTitle()), 
	    						UIRegistry.getResourceString("BaseTreeDef.RemoveLock"), 
	    						UIRegistry.getResourceString("CANCEL"), 
	    						JOptionPane.WARNING_MESSAGE);
    			if (ok)
    			{
    				TreeDefStatusMgr.setRenumberingNodes(this, false);
    			}    
    		}
    	}
    	return !TreeDefStatusMgr.isRenumberingNodes(this);
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.TreeDefIface#isSynonymySupported()
	 */
	@Override
	@Transient
	public boolean isSynonymySupported() 
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.TreeDefIface#isRequiredLevel(int)
	 */
	@Override
	@Transient
	public boolean isRequiredLevel(int levelRank)
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
	 */
	@Override
	public void initialize()
	{
		// TODO Auto-generated method stub
		
	}    
	
    
}
