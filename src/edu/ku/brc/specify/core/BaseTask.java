/* Filename:    $RCSfile: BaseTask.java,v $
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

import javax.swing.*;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.TaskPluginable;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import edu.ku.brc.specify.ui.UICacheManager;

/**
 * Abstract class to provide a base level of functionality for implementing a task.
 * It fully implements Taskable and partially implements TaskPluginable.
 * 
 * @author rods
 *
 */
public abstract class BaseTask implements Taskable, TaskPluginable
{
    // Data Members
    protected final String        name;
    protected final String        title;
    
    protected Vector<NavBoxIFace> navBoxes = new Vector<NavBoxIFace>(); 
    protected Icon                icon     = null;
    protected boolean             isInitialized = false;
    
    /**
     * Constructor
     * @param name the name of the task (already localized)
     */
    public BaseTask(final String name, final String title)
    {
        this.name = name;
        this.title = title;
        
        ContextMgr.getInstance().register(this);
    }
    
    /**
     * Remove self from ContextMgr
     */
    public void finalize()
    {
        ContextMgr.getInstance().unregister(this);
    }
    
    /**
     * 
     */
    public void initialize()
    {
        isInitialized = true;
    }
    
     /**
     * Helper
     * @param catName
     * @param imageName
     * @param hint
     * @return
     */
    protected ToolBarDropDownBtn createToolbarButton(final String catName,
                                                     final String imageName,
                                                     final String hint)
    {

        return createToolbarButton(catName, imageName, hint, null);
        
    }
    
    /**
     * Helper
     * @param catName
     * @param imageName
     * @param hint
     * @return
     */
    protected ToolBarDropDownBtn createToolbarButton(final String catName,
                                                     final String imageName,
                                                     final String hint,
                                                     final List<JComponent> menus)
    {
        String name = getResourceString(catName);
        
        icon = IconManager.getInstance().getIcon(catName, IconManager.IconSize.Std16);
        
        ToolBarDropDownBtn btn = new ToolBarDropDownBtn(name, IconManager.getInstance().getIcon(catName, IconManager.IconSize.Std24), JButton.BOTTOM, menus);
        btn.setStatusBarHintText(getResourceString(hint));
        
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                requestContext();
            }
        });
        return btn;
    }

    
    /**
     * Returns the initial pane for this task, may be a blank (empty) pane, but shouldn't null
     * @return Returns the initial pane for this task, may be a blank (empty) pane, but shouldn't null
     */
    public abstract SubPaneIFace getStarterPane();

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
        return navBoxes;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getIcon()
     */
    public Icon getIcon()
    {
        return icon;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#requestContext()
     */
    public void requestContext()
    {
        ContextMgr.getInstance().requestContext(this);
        
        UICacheManager.getInstance().getSubPaneMgr().addPane(getStarterPane());
    }
    
    //-------------------------------------------------------
    // Plugin Interface
    //-------------------------------------------------------
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public String getName()
    {
        return name;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getTitle()
     */
    public String getTitle()
    {
        return title;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public abstract List<ToolBarItemDesc> getToolBarItems();
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getMenuItems()
     */
    public abstract List<MenuItemDesc> getMenuItems();

}
