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
package edu.ku.brc.af.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.util.List;
import java.util.Vector;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.plugins.MenuItemDesc;
import edu.ku.brc.af.plugins.ToolBarItemDesc;
import edu.ku.brc.af.tasks.subpane.StatsPane;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToolBarDropDownBtn;

/**
 * This task reads an xml definition describing the content and the layout of statistics for the
 * startup page of the application.
 
 * @code_status Complete
 **
 * @author rods
 *
 */
public class StartUpTask extends BaseTask
{
    public static final String STARTUP = "Startup";

    protected Vector<ToolBarDropDownBtn> tbList = new Vector<ToolBarDropDownBtn>();
    protected SubPaneIFace blankPanel = null;

    // XXX Demo Only
    StatsPane statPane;


    /**
     * Default Constructor
     *
     */
    public StartUpTask()
    {
        super(STARTUP, getResourceString(STARTUP));

        icon = IconManager.getImage(STARTUP, IconManager.IconSize.Std16);
    }

    /**
     * Creates the StartUP Statistics pane and removes the blank pane.
     */
    public void createStartUpStatPanel()
    {
        StatsPane statPane = new StatsPane(title, this, "startup_panel.xml", true, null);
        SubPaneMgr.getInstance().removePane(blankPanel);
        SubPaneMgr.getInstance().addPane(statPane);
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
    public SubPaneIFace getStarterPane()
    {
        //blankPanel = new SimpleDescPane("", this, "");
        //System.out.println(blankPanel);
        //return blankPanel;

        StatsPane statPane = new StatsPane(title, this, "startup_panel.xml", true, null);
        return statPane;
    }

    //-------------------------------------------------------
    // Plugin Interface
    //-------------------------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.plugins2.TaskPluginable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();

        /*ToolBarDropDownBtn btn = createToolbarButton(name, "queryIt.gif", "search_hint");
        if (tbList.size() == 0)
        {
            tbList.add(btn);
        }
        list.add(new ToolBarItemDesc(btn));
        */
        return list;

    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.plugins2.TaskPluginable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();

        return list;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.plugins2.TaskPluginable#getTaskClass()
     */
    public Class getTaskClass()
    {
        return this.getClass();
    }

}
