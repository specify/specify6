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
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.ui.db.*;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.dnd.DataActionEvent;
import edu.ku.brc.specify.ui.dnd.GhostActionable;
import edu.ku.brc.specify.ui.dnd.GhostActionableDropManager;
import edu.ku.brc.specify.ui.dnd.GhostMouseDropAdapter;

/**
 * A task to manage Labels and response to Label Commands
 * 
 * @author rods
 * 
 */
public class LabelsTask extends BaseTask
{
    public static final String LABELS = "Labels";
    
    public static final String DOLABELS_ACTION = "DoLabels";
    
    protected Vector<NavBoxIFace> extendedNavBoxes = new Vector<NavBoxIFace>(); 

    /**
     * 
     *
     */
    public LabelsTask()
    {
        super(LABELS, getResourceString(LABELS));
        CommandDispatcher.register(LABELS, this);
    }
    
    /**
     * Helper method for registering a NavBoxItem as a GhostMouseDropAdapter
     * @param list the list of NavBoxItems that will have this nbi registered as a drop zone
     * @param navBox the parent box for the nbi to be added to
     * @param navBoxItemDropZone the nbi in question
     */
    protected void addToNavBoxAndRegisterAsDroppable(java.util.List<NavBoxIFace> list, NavBox navBox, NavBoxItemIFace navBoxItemDropZone)
    {
        navBox.add(navBoxItemDropZone);
        for (NavBoxIFace nBox : list)
        {
            for (NavBoxItemIFace nbi : nBox.getItems())
            {
                if (nbi instanceof GhostActionable)
                {
                    GhostActionable       ga  = (GhostActionable)nbi;
                    GhostMouseDropAdapter gpa = ga.getMouseDropAdapter();  
                    gpa.addGhostDropListener(new GhostActionableDropManager(UICacheManager.getGlassPane(), navBoxItemDropZone.getUIComponent(), ga));
                }
            }
        }

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            NavBox navBox = new NavBox(name);  
            
            // Get all RecordSets and register them 
            RecordSetTask rst = (RecordSetTask)ContextMgr.getInstance().getTaskByClass(RecordSetTask.class);
            
            java.util.List<NavBoxIFace> list = rst.getNavBoxes();
            
            // Temporary these to come from a persistent store
            addToNavBoxAndRegisterAsDroppable(list, navBox, NavBox.createBtn("Fish Label Example", name, IconManager.IconSize.Std16, new DisplayAction("fish_label.jrxml", "Fish Label Example")));
            addToNavBoxAndRegisterAsDroppable(list, navBox, NavBox.createBtn("Lichens Label Example", name, IconManager.IconSize.Std16, new DisplayAction("lichens_label.jrxml", "Lichens Label Example")));

            navBoxes.addElement(navBox);
        }
        
    }

    
    /**
     * Performs a command (to cfreate a label)
     * @param name the XML file name for the label
     */
    public void doCommand(final String name, final String title, final Object data)
    {
        LabelsPane labelsPane = new LabelsPane(title, this);
        UICacheManager.addSubPane(labelsPane);
        RecordSet rs = null;
        if (data instanceof RecordSet)
        {
            rs = (RecordSet)data;
        }
        labelsPane.createReport(name, rs);

    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return new SimpleDescPane(name, this, "Welcome to Specify's Label Maker");
    }
    
    /**
     * Displays UI that asks the user to select a predefined label.
     * @return
     */
    protected String askForLabelName()
    {
        // XXX need to pass in table type
        ChooseRecordSetDlg dlg = new ChooseRecordSetDlg();
        dlg.setVisible(true);
        return "fish_label.jrxml";
    }
    
    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getNavBoxes()
     */
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();
        
        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);
        
        RecordSetTask rsTask = (RecordSetTask)ContextMgr.getInstance().getTaskByClass(RecordSetTask.class);
        
        extendedNavBoxes.addAll(rsTask.getNavBoxes());
        
        return extendedNavBoxes;
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
    
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.getAction().equals(DOLABELS_ACTION))
        {
            Object data = cmdAction.getData();
            if (data instanceof RecordSet)
            {
                String labelName = askForLabelName();
                if (labelName != null)
                {
                    doCommand(labelName, "Labels", data);
                }
            }
        }
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
        private String   title;
        private RecordSet recordSet = null;
        
        
        public DisplayAction(final String name, final String title)
        {
            this.name = name;
            this.title = title;
        }
        
        public void actionPerformed(ActionEvent e) 
        {
            Object data = null;
            if (e instanceof DataActionEvent)
            {
                data = ((DataActionEvent)e).getData();
            }
            doCommand(name, title, data);
        }
        
        public void setRecordSet(final RecordSet recordSet)
        {
            this.recordSet = recordSet;
        }
        
        public RecordSet getRecordSet()
        {
            return recordSet;
        }
    }
 
}
