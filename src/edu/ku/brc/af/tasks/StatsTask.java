/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.af.tasks.subpane.StatsPane;
import edu.ku.brc.stats.StatsMgr;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIRegistry;
/**
 * The StatsTask is responsible gettiong and displaying all various different kinds of stats
 * (needs better comments)
 * 
 * @code_status Code Freeze 
 *
 * @author rods
 *
 */
public class StatsTask extends BaseTask
{
    // Static Data Members
    public static final String STATISTICS = "Statistics"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(StatsTask.class);

    protected static final String DISPLAY   = "display"; //$NON-NLS-1$
    protected static final String BAR_CHART = "bar chart"; //$NON-NLS-1$
    protected static final String PIE_CHART = "pie chart"; //$NON-NLS-1$
    protected static final String TABLE     = "table"; //$NON-NLS-1$
    protected static final String FORM      = "form"; //$NON-NLS-1$

    // Data Members
     protected Element panelDOM;

    /**
     * Constructor that creates a Statistics Tasks
     *
     */
    public StatsTask()
    {
        super(STATISTICS, getResourceString(STATISTICS));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            try
            {
                panelDOM = AppContextMgr.getInstance().getResourceAsDOM("StatisticsPanel");   // contains a description of the NavBoxes //$NON-NLS-1$
                if (panelDOM == null)
                {
                    log.error("Couldn't load StatisticsPanel"); //$NON-NLS-1$
                    return;
                }
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(StatsTask.class, ex);
                log.error("Couldn't load `StatisticsPanel` " +ex); //$NON-NLS-1$
            }
    
            
            StatsMgr.getDOM(); // This loads the resource name
            
            boolean hasResBundle = false;
            if (StringUtils.isNotEmpty(StatsMgr.getResourceName()))
            {
                hasResBundle = UIRegistry.loadAndPushResourceBundle(StatsMgr.getResourceName()) != null;
            }
            
            try
            {
                // Process the NavBox Panel and create all the commands
                // XXX This needs to be made generic so everyone can use it
                //
                List<?> boxes = panelDOM.selectNodes("/boxes/box"); //$NON-NLS-1$
                for ( Iterator<?> iter = boxes.iterator(); iter.hasNext(); )
                {
                    Element box = (Element) iter.next();
                    NavBox navBox = new NavBox(UIRegistry.getResourceString(box.attributeValue("title"))); //$NON-NLS-1$
        
                    List<?> items = box.selectNodes("item"); //$NON-NLS-1$
                    for ( Iterator<?> iter2 = items.iterator(); iter2.hasNext(); )
                    {
                        Element item = (Element) iter2.next();
                        String boxName  = item.attributeValue("name"); //$NON-NLS-1$
                        String boxTitle = UIRegistry.getResourceString(item.attributeValue("title")); //$NON-NLS-1$
                        String type     = item.attributeValue("type"); //$NON-NLS-1$
                        ActionListener action = null;
                        if (type.toLowerCase().equals(PIE_CHART))
                        {
                            type = "Pie_Chart"; //$NON-NLS-1$
                            action = new DisplayAction(boxName);
        
                        } else if (type.toLowerCase().equals(BAR_CHART))
                        {
                            type = "Bar_Chart"; //$NON-NLS-1$
                            action = new DisplayAction(boxName);
                        }
        
                        String tooltip = StatsMgr.getTooltipForStat(boxName);
                        navBox.add(NavBox.createBtnWithTT(boxTitle, type, tooltip, IconManager.STD_ICON_SIZE, action));
                   }
                   navBoxes.add(navBox);
                }
                
            } finally
            {
                if (hasResBundle)
                {
                    UIRegistry.popResourceBundle();
                }
            }
        }
        isShowDefault = true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        if (starterPane == null)
        {
            starterPane = new StatsPane(title, this, "StatsSummaryPanel", true, null, null); //$NON-NLS-1$
        }
        return starterPane;
    }

    /**
     * Looks up statName and creates the appropriate SubPane
     * @param statName the name of the stat to be displayed
     */
    public void createStatPane(final String statName)
    {
        // Create stat pane return a non-null panel for charts and null for non-charts
        // Of coarse, it could pass back nul if a chart was missed named
        // but error would be shown inside the StatsMgr for that case
        JPanel panel = StatsMgr.createStatPane(statName);
        if (panel != null)
        {
            SimpleDescPane pane = new SimpleDescPane(title, this, panel);
            addSubPaneToMgr(pane);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        return new BasicPermisionPanel(STATISTICS, "ENABLE", null, null, null);
    }
    
    //-------------------------------------------------------
    // BaseTask Taskable Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        toolbarItems = new Vector<ToolBarItemDesc>();
        String label = getResourceString(STATISTICS);
        String hint  = getResourceString("stats_hint"); //$NON-NLS-1$
        ToolBarDropDownBtn      btn  = createToolbarButton(label, iconName, hint);

        toolbarItems.add(new ToolBarItemDesc(btn));
        return toolbarItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getTaskClass()
     */
    @Override
    public Class<? extends StatsTask> getTaskClass()
    {
        return this.getClass();
    }


    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------

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
     *
     * @author rods
     *
     */
    class DisplayAction implements ActionListener
    {
        private String   statName;

        public DisplayAction(final String statName)
        {
            this.statName = statName;
        }

        public void actionPerformed(ActionEvent e)
        {
            UsageTracker.incrUsageCount("ST."+statName);
            createStatPane(statName);
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
