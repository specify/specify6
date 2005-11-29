/* Filename:    $RCSfile: MainPanel.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.core;

import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.core.subpane.*;

/**
 * 
 * @author rods
 * Status: Work In Progress
 *
 */
public class ContextMgr
{
    // Static Data Members
    private static ContextMgr instance = new ContextMgr();
    
    // Data Members
    protected Taskable         currentContext = null;
    protected Vector<Taskable> tasks          = new Vector<Taskable>();
    
    /**
     * Protected Constructor of Singleton
     *
     */
    protected ContextMgr()
    {
        
    }
    
    /**
     * Returns singleton
     * @return returns singleton of Context Manager
     */ 
    public static ContextMgr getInstance()
    {
        return instance;
    }
    
    /**
     * Request for a change in context
     * @param the task requesting the context
     */
    public void requestContext(Taskable task)
    {
        if (currentContext != null)
        {
            NavBoxMgr.getInstance().unregister(currentContext);
        }
        
        NavBoxMgr.getInstance().register(task);
        currentContext = task;
 
    }
    
    public void register(Taskable task)
    {
        tasks.addElement(task);
    }
    
    public void unregister(Taskable task)
    {
        tasks.removeElement(task);
    }
    
}
