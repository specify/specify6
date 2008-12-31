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
import edu.ku.brc.specify.datamodel.SpTaskSemaphore;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgrCaller;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr.USER_ACTION;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class UploadLocker implements TaskSemaphoreMgrCaller
{
    protected final boolean canOverride;
    protected final boolean canUnlock;

    public UploadLocker()
    {
        canOverride = false;
        canUnlock = false;
    }

    public UploadLocker(boolean canOverride, boolean canUnlock)
    {
        this.canOverride = canOverride;
        this.canUnlock = canUnlock;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.TaskSemaphoreMgrCaller#resolveConflict(edu.ku.brc.specify.datamodel.SpTaskSemaphore, boolean, java.lang.String)
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
            if (sameUser && sameMachine)
            {
                msg = "The task " + Uploader.getLockTitle() + " is currently locked by "
                    + " you. ";

            }
            else
            {
                msg = "The task " + Uploader.getLockTitle() + " is currently locked by "
                    + lockUser.getIdentityTitle() + " on " + lockMachineName;
            }
            System.out.println(msg);
            
            if (canOverride)
            {
                msgType = JOptionPane.QUESTION_MESSAGE;
                if (canUnlock) //ignoring canUnlock if canOverride is false
                {
                    options = JOptionPane.YES_NO_CANCEL_OPTION;
                    optionLabels = new String[] {
                        getResourceString("UploadLocker.Exit"), 
                        getResourceString("UploadLocker.Override"),
                        getResourceString("UploadLocker.Remove")
                    };   
                }
                else  
                {
                    options = JOptionPane.YES_NO_OPTION;
                    optionLabels = new String[] {
                            getResourceString("UploadLocker.Exit"),
                            getResourceString("UploadLocker.Override")
                    };                
                }
            }
            else
            {
                msgType = JOptionPane.INFORMATION_MESSAGE;
                options = JOptionPane.YES_OPTION;
                optionLabels = new String[] {
                        getResourceString("UploadLocker.Exit")
                };
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
                return USER_ACTION.OK;
            }
            if (userChoice == JOptionPane.CANCEL_OPTION)
            {
                //unlock ---
                UIRegistry.showError("Unlocking is under construction");
                return USER_ACTION.Cancel;
            }
            return USER_ACTION.Error;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return USER_ACTION.Error;
        }
    }

}
