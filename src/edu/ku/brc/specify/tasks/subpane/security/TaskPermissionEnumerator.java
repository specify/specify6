/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.List;

import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.StartUpTask;
import edu.ku.brc.af.tasks.StatsTrackerTask;

/**
 * This class enumerates task related permissions associated with a principal
 * 
 * @author rods
 *
 */
public class TaskPermissionEnumerator extends PermissionEnumerator 
{

    protected List<SecurityOptionIFace> taskOptions = null;
    
	/**
	 * 
	 */
	public TaskPermissionEnumerator()
	{
	   super("Task", "ADMININFO_DESC");
	}
	
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEnumerator#getSecurityOptions()
     */
    @Override
    protected List<SecurityOptionIFace> getSecurityOptions()
    {
        if (taskOptions == null)
        {
            taskOptions = new ArrayList<SecurityOptionIFace>();
            
            for (Taskable task : TaskMgr.getInstance().getAllTasks())
            {
                if (!(StatsTrackerTask.class.isAssignableFrom(task.getTaskClass()) ||
                      StartUpTask.class.isAssignableFrom(task.getTaskClass())))
                {
                    taskOptions.add(task);
                }
            }
        }
        return taskOptions;
    }
    
}
