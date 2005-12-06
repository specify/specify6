/* Filename:    $RCSfile: LabelsTask.java,v $
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

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.specify.core.subpane.LabelsPane;
import edu.ku.brc.specify.core.subpane.SimpleDescPane;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import edu.ku.brc.specify.ui.UICacheManager;

/**
 * A task to manage Labels and response to Label Commands
 * 
 * @author rods
 * 
 */
public class LabelsTask extends BaseTask
{

    /**
     * 
     *
     */
    public LabelsTask()
    {
        super(getResourceString("Labels"));
        
        // Temporary
        NavBox navBox = new NavBox(name);
        navBox.add(NavBox.createBtn("Fish Label Example", name, IconManager.IconSize.Std16, new DisplayAction("fish_label.jrxml")));
        navBox.add(NavBox.createBtn(name, name, IconManager.IconSize.Std16));
        navBoxes.addElement(navBox);
    }
    
    /**
     * Performs a command (to cfreate a label)
     * @param name the XML file name for the label
     */
    public void doCommand(final String name)
    {
        LabelsPane labelsPane = new LabelsPane(getResourceString("Labels"), this);
        UICacheManager.addSubPane(labelsPane);
        
        labelsPane.createReport(name);

    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return new SimpleDescPane(name, this, "Welcome to Specify's Label Maker");
    }
    
    //-------------------------------------------------------
    // Plugin Interface
    //-------------------------------------------------------
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        ToolBarDropDownBtn btn = createToolbarButton(name, "labels.gif", "labels_hint");   
        
        list.add(new ToolBarItemDesc(btn.getCompleteComp()));
        return list;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        return list;
        
    }
    
    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------
 
     /**
     * 
     * @author rods
     *
     */
    class DisplayAction implements ActionListener 
    {
        private String   name;
        
        public DisplayAction(final String name)
        {
            this.name = name;
        }
        
        public void actionPerformed(ActionEvent e) 
        {
            doCommand(name);
        }
    }
}
