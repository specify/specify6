/* Filename:    $RCSfile: NavBoxMgr.java,v $
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
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import edu.ku.brc.specify.exceptions.ConfigurationException;

/**
 * @author rods
 *
 * A singleton that manages a list of NavBoxIFace items. The NavBoxIFace are layed out using a manager 
 * which typically lays them out in a vertical fashion. A Taskable object vends one or more NavBoxes.
 */
public class NavBoxMgr extends JPanel
{
    // Static Data Members
    private static NavBoxMgr instance = new NavBoxMgr();
    
    // Data Members
    private List<NavBoxIFace>   list   = Collections.synchronizedList(new ArrayList<NavBoxIFace>());
    private NavBoxLayoutManager layout = new NavBoxLayoutManager(5, 5);
     
    /**
     * Protected Default Constructor for the singleton
     *
     */
    protected NavBoxMgr()
    {
       setLayout(layout);
       setBackground(Color.WHITE); // XXX PREF ??
    }
    
    /**
     * Returns instance of MavBoxMgr
     * @return returns a NavBoxMgr singleton instance
     */
    public static NavBoxMgr getInstance()
    {
        return instance;
    }
    
    /**
     * Registers a Task's NavBoxes into the Manager
     * @param task a task to be managed, this means we ask the task for the list of NavBoxes and then does a layout
     */
    public void register(final Taskable task)
    {
        List<NavBoxIFace> list = task.getNavBoxes();
        if (list != null)
        {
            for (NavBoxIFace box : list)
            {
                addBox(box);
                box.getUIComponent().invalidate();
                box.getUIComponent().doLayout();
            }
        }
        doLayout();
        repaint();
    }
    
    /**
     * Registers a Task's NavBoxes into the Manager
     * @param pane the pane to 
     */
    public void unregister(final Taskable task)
    {
        // for now just clear everything
        layout.removeAll();
        this.removeAll();
        list.clear();

        repaint();
    }
    
    /**
     * Returns whether a box with a unique name has already been registered
     * @param name the name of the box
     * @return Returns whether a box with a unique name has already been registered
     */
    protected boolean exists(final String name)
    {
        for (NavBoxIFace box : list)
        {
            if (box.getName().equals(name))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Adds a box to the manager (all adds are 'appends' at the moment)
     * @param box the box to be added
     */
    public void addBox(final NavBoxIFace box)
    {
        if (box == null)
        {
            throw new NullPointerException("Null pane when adding to NavBoxMgr");
        }
        
        if (!exists(box.getName()))
        {
            list.add(box); 
            add(box.getUIComponent());
            layout.addLayoutComponent(null, box.getUIComponent());
            invalidate();
            doLayout();
        } else
        {
            throw new ConfigurationException("Adding a new NavBox with duplicate name["+box.getName()+"]");
        }
    }    
    
}
