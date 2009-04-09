/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.net.InetAddress;

import javax.swing.JOptionPane;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.SpTaskSemaphore;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgrCallerIFace;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr.USER_ACTION;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class UploadLocker implements TaskSemaphoreMgrCallerIFace
{
    protected final boolean canIgnore;
    protected final boolean canOverride;
    protected final boolean doLock;
    protected final Taskable task;

    public UploadLocker()
    {
        canIgnore = false;
        canOverride = false;
        doLock = false;
        task = null;
    }

    public UploadLocker(boolean canOverride, boolean canUnlock, final Taskable task, final boolean doLock)
    {
    	this.canIgnore = canOverride; 
        this.canOverride = canUnlock;
        this.doLock = doLock;
        this.task = task;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.TaskSemaphoreMgrCallerIFace#resolveConflict(edu.ku.brc.specify.datamodel.SpTaskSemaphore, boolean, java.lang.String)
     */
    @Override
    public USER_ACTION resolveConflict(SpTaskSemaphore semaphore,
                                       boolean previouslyLocked,
                                       String prevLockBy)
    {
        try
        {
            SpecifyUser currentUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
            SpecifyUser lockUser =  semaphore.getOwner();
            boolean sameUser = currentUser.getId().equals(lockUser != null ? lockUser.getId() : null);
            
            String currMachineName = InetAddress.getLocalHost().toString();
            String lockMachineName = semaphore.getMachineName();
            boolean sameMachine = currMachineName.equals(lockMachineName);
            
            String msg;
            int      options;
            int      defBtn = 0;
            int      msgType;  
            Object[] optionLabels;
			msgType = JOptionPane.INFORMATION_MESSAGE;
			options = JOptionPane.YES_OPTION;
			optionLabels = new String[] { getResourceString("OK") };
			Collection currCollection = AppContextMgr.getInstance().getClassObject(Collection.class);
            if (sameUser && sameMachine)
			{
				if (task == null)
				{
					msg = String.format(UIRegistry.getResourceString("UploadLocker.LockedByYou"),
						currCollection.getCollectionName());
				}
				else
				{
					msg = String.format(UIRegistry.getResourceString("UploadLocker.TaskUnavailable"),
							task.getTitle());
				}
			} else
			{
				msg = String.format(UIRegistry.getResourceString("UploadLocker.LockedByUserMachine"),
						currCollection.getCollectionName(), lockUser.getIdentityTitle(), lockMachineName);
				if (task != null)
				{
					msg += String.format(UIRegistry.getResourceString("UploadLocker.TaskUnavailable"),
							task.getTitle(), Uploader.getLockTitle());
				}
			}
            
            msgType = JOptionPane.QUESTION_MESSAGE;
			if (!doLock)
			{
				if (canIgnore && canOverride)
				{
					//options = JOptionPane.YES_NO_CANCEL_OPTION;
					options = JOptionPane.YES_NO_OPTION;
					optionLabels = new String[] {
							getResourceString("UploadLocker.Exit"),
							//getResourceString("UploadLocker.Ignore"),
							getResourceString("UploadLocker.Remove") };
				} else if (canIgnore)
				{
					//options = JOptionPane.YES_NO_OPTION;
					options = JOptionPane.YES_OPTION;
					optionLabels = new String[] {
							getResourceString("UploadLocker.Exit"),
							//getResourceString("UploadLocker.Ignore") 
							};
				}
			} else if (doLock)
			{
				if (canOverride)

				{
					options = JOptionPane.YES_NO_OPTION;
					optionLabels = new String[] {
							getResourceString("UploadLocker.Exit"),
							getResourceString("UploadLocker.Override") };
				}
			}
            int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                    msg,
                    getResourceString("SpTaskSemaphore.IN_USE_TITLE"),  //$NON-NLS-1$
                    options,
                    msgType, null, optionLabels, defBtn);
            
            if (userChoice == JOptionPane.YES_OPTION)
            {
                return USER_ACTION.Cancel;
            }
            if (userChoice == JOptionPane.NO_OPTION)
            {
                //UIRegistry.displayInfoMsgDlgLocalized("UploadLocker.LockOverrideInfo");
            	//return USER_ACTION.OK;
            	return USER_ACTION.Override;
            }
            if (userChoice == JOptionPane.CANCEL_OPTION)
            {
                //unlock ---
                //UIRegistry.showError("Unlocking is under construction");
                //return USER_ACTION.Cancel;
            	return USER_ACTION.Override;
            }
            return USER_ACTION.Error;
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UploadLocker.class, e);
            e.printStackTrace();
            return USER_ACTION.Error;
        }
    }

}
