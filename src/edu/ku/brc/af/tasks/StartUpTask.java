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
package edu.ku.brc.af.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.subpane.StatsPane;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.ToolBarDropDownBtn;

/**
 * This task reads an xml definition describing the content and the layout of statistics for the
 * startup page of the application.
 
 * @code_status Complete
 **
 * @author rods
 *
 */
/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class StartUpTask extends BaseTask
{
    public static final String STARTUP = "Startup"; //$NON-NLS-1$

    protected Vector<ToolBarDropDownBtn> tbList     = new Vector<ToolBarDropDownBtn>();
    protected SubPaneIFace               blankPanel = null;

    StatsPane statPane;

    /**
     * Default Constructor
     *
     */
    public StartUpTask()
    {
        super(STARTUP, getResourceString(STARTUP));
    }
    
    /**
     * @return
     */
    public JPanel createSplashPanel()
    {
        return null;
    }

    /**
     * Creates the StartUP Statistics pane and removes the blank pane.
     */
    public void createStartUpStatPanel()
    {
        StatsPane pane = new StatsPane(title, this, "StartUpPanel", true, null, createSplashPanel()); //$NON-NLS-1$
        SubPaneMgr.getInstance().removePane(blankPanel);
        SubPaneMgr.getInstance().addPane(pane);
        blankPanel = null;
    }

    /**
     * Returns the blank SubPane or null.
     * @return the blank SubPane or null
     */
    public SubPaneIFace getBlankPane()
    {
        return blankPanel;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        for (SubPaneIFace sb : SubPaneMgr.getInstance().getSubPanes())
        {
            if (sb.getTask() == this)
            {
                return sb;
            }
        }
        
        return new StatsPane(title, this, "StartUpPanel", true, null, createSplashPanel()); //$NON-NLS-1$
    }

    //-------------------------------------------------------
    // BaseTask (Taskable Interface)
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        return new Vector<ToolBarItemDesc>();

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        return new Vector<MenuItemDesc>();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getTaskClass()
     */
    @Override
    public Class<? extends StartUpTask> getTaskClass()
    {
        return this.getClass();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doProcessAppCommands(edu.ku.brc.ui.CommandAction)
     */
    @Override
    protected void doProcessAppCommands(CommandAction cmdAction)
    {
        super.doProcessAppCommands(cmdAction);
        
        if (cmdAction.isAction(APP_RESTART_ACT))
        {
            starterPane = null; // should have already been removed
        }
    }
    
    /**
     * @return the permissions array
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {true, true, true, true},
                                {true, true, false, false},
                                {true, false, false, false}};
    }
}
