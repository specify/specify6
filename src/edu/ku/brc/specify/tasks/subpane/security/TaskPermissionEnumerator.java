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
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.StartUpTask;
import edu.ku.brc.af.tasks.StatsTrackerTask;
import edu.ku.brc.specify.SpecifyUserTypes;
import edu.ku.brc.specify.tasks.PermissionOptionPersist;

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
            if (true)
            {
                XStream xstream = new XStream();
                PermissionOptionPersist.config(xstream);
                //try
                //{
                    Hashtable<String, Hashtable<String, PermissionOptionPersist>> hash = new Hashtable<String, Hashtable<String, PermissionOptionPersist>>();
                    for (Taskable task : TaskMgr.getInstance().getAllTasks())
                    {
                        if (task.isPermissionsSettable())
                        {
                            String name = task.getName();
                            //System.out.println("Task: "+name);
                            
                            Hashtable<String, PermissionOptionPersist> hashItem = new Hashtable<String, PermissionOptionPersist>();
                            hash.put(name, hashItem);
                            int i = 0;
                            for (SpecifyUserTypes.UserType spUserType : SpecifyUserTypes.UserType.values())
                            {
                                String          typeName = spUserType.toString();
                                PermissionIFace perm     = task.getDefaultPermissions(typeName);
                                if (perm == null)
                                {
                                    perm = new PermissionSettings(false, false, false, false);
                                }
                                hashItem.put(typeName, new PermissionOptionPersist(name, typeName, perm));
                                i++;
                            }
                        }
                    }
                    
                    //FileUtils.writeStringToFile(new File("tasks_new.xml"), xstream.toXML(hash)); //$NON-NLS-1$
                    //System.out.println(xstream.toXML(config));
                    
                //} catch (IOException ex)
                //{
                //    ex.printStackTrace();
                //}
            }

            taskOptions = new ArrayList<SecurityOptionIFace>();
            
            for (Taskable task : TaskMgr.getInstance().getAllTasks())
            {
                if (task.isPermissionsSettable() &&
                        (!(StatsTrackerTask.class.isAssignableFrom(task.getTaskClass()) ||
                          StartUpTask.class.isAssignableFrom(task.getTaskClass()))))
                {
                    taskOptions.add(task);
                }
            }
        }
        return taskOptions;
    }
    
}
