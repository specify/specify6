/* Copyright (C) 2015, University of Kansas Center for Research
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
