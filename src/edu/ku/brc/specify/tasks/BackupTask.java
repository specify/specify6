/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks;

import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 29, 2008
 *
 */
public class BackupTask extends BaseTask
{
    protected edu.ku.brc.af.tasks.BackupTask baseBackupTask;
    
    /**
     * 
     */
    public BackupTask()
    {
        super("BackupTask", UIRegistry.getResourceString("BackupTask.TITLE"));
        this.iconName = "Backup";
        
        baseBackupTask = new edu.ku.brc.af.tasks.BackupTask();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return getSplashPane();
    }

    /**
     * @return
     */
    public SubPaneIFace getSplashPane()
    {
        return baseBackupTask.getSplashPane();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        return baseBackupTask.getPermEditorPanel();
    }
    
    
    /**
     * @return the permissions array
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {true, true, true, true},
                                {false, false, false, false},
                                {false, false, false, false}};
    }
}
