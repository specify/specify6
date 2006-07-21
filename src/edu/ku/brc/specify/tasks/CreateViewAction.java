/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.tasks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.helpers.UIHelper;

/**
 * @author rods
 *
 */
public class CreateViewAction implements ActionListener
{
    private Taskable task;
    private String   viewSetName;
    private String   viewName; 
    private String   mode;
    private Class    newDataObjClass;
    
    public CreateViewAction(final Taskable task, 
                            final String   viewSetName, 
                            final String   viewName, 
                            final String   mode, 
                            final Class    newDataObjClass)
    {
        this.task = task;
        this.viewSetName = viewSetName;
        this.viewName = viewName;
        this.mode = mode;
        this.newDataObjClass = newDataObjClass;
    }
    public void actionPerformed(ActionEvent e)
    {
        
        DataEntryTask.openView(task, viewSetName, viewName, mode, UIHelper.createAndNewDataObj(newDataObjClass), true);
    }
}
