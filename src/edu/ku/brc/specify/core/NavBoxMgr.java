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
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

public class NavBoxMgr extends JPanel
{
    // Static Data Memebers
    private static NavBoxMgr instance = new NavBoxMgr();
    
    // Data Memebers
    private List<NavBoxIFace>   list   = Collections.synchronizedList(new ArrayList<NavBoxIFace>());
    private NavBoxLayoutManager layout = new NavBoxLayoutManager(5, 5);
     
    /**
     * Protected Default Constructor
     *
     */
    protected NavBoxMgr()
    {
       setLayout(layout);
       setBackground(Color.WHITE); // PREF ??
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
     * Registers a SubPane's NavBoxes into the Manager
     * @param pane the pane to 
     */
    public void register(Taskable task)
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
     * Registers a SubPane's NavBoxes into the Manager
     * @param pane the pane to 
     */
    public void unregister(Taskable task)
    {
        // for now just clear everything
        layout.removeAll();
        this.removeAll();
        list.clear();

        repaint();
    }
    
    /**
     * 
     * @param name
     * @return
     */
    protected boolean exists(String name)
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
     * 
     * @param box
     */
    public void addBox(NavBoxIFace box)
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
        }
    }    
    
}
