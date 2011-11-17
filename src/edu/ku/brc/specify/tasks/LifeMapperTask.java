/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.SecurityOption;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.tasks.subpane.LifeMapperPane;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 28, 2011
 *
 */
public class LifeMapperTask extends BaseTask
{
    //private static final Logger log  = Logger.getLogger(VisualQueryTask.class);
    
    private static final String  LIFEMAPPER        = "Lifemapper";
    private static final String  LIFEMAP_TITLE     = "LIFEMAP_TITLE";
    private static final String  LIFEMAP_SECURITY  = "LIFEMAPEDIT";
    
    // Data Members
    protected LifeMapperPane          lmPane           = null;
    protected NavBox                  actionNavBox     = null;

    protected Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    protected ToolBarDropDownBtn      toolBarBtn       = null;

    
    /**
     * 
     */
    public LifeMapperTask()
    {
        super(LIFEMAPPER, UIRegistry.getResourceString(LIFEMAP_TITLE));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    @Override
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            extendedNavBoxes.clear();
            
            //navBoxes.add(actionNavBox);
        }
        isShowDefault = true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#preInitialize()
     */
    @Override
    public void preInitialize()
    {
        CommandDispatcher.register(LIFEMAPPER, this);

        // Create and add the Actions NavBox first so it is at the top at the top
        actionNavBox = new NavBox(getResourceString("Actions"));
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        UsageTracker.incrUsageCount("LM.OpenLifemapper");
        //starterPane = null; // for debug
        if (starterPane == null || lmPane.hasSizeChanged())
        {
            if (lmPane != null)
            {
                lmPane.shutdown();
            }
            starterPane = lmPane = new LifeMapperPane(name, this);
        }
        return starterPane;
    }
    
    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getNavBoxes()
     */
    @Override
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

        return extendedNavBoxes;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        String label    = getResourceString(LIFEMAPPER);
        String hint     = getResourceString(LIFEMAPPER);
        toolBarBtn      = createToolbarButton(label, iconName, hint);
        
        toolbarItems = new Vector<ToolBarItemDesc>();
        toolbarItems.add(new ToolBarItemDesc(toolBarBtn));
        return toolbarItems;
    }
    
    //-------------------------------------------------------
    // SecurityOption Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getAdditionalSecurityOptions()
     */
    @Override
    public List<SecurityOptionIFace> getAdditionalSecurityOptions()
    {
        List<SecurityOptionIFace> list = new ArrayList<SecurityOptionIFace>();
        
        SecurityOption secOpt = new SecurityOption(LIFEMAP_SECURITY, 
                                                    getResourceString("LIFEMAP_TITLE"), 
                                                    securityPrefix,
                                                    new BasicPermisionPanel(LIFEMAP_TITLE, 
                                                                            "LIFEMAP_VIEW", 
                                                                            "LIFEMAP_EDIT"));
        addPerms(secOpt, new boolean[][] 
                   {{true, true, true, false},
                   {false, false, false, false},
                   {false, false, false, false},
                   {false, false, false, false}});
        
        list.add(secOpt);

        return list;
    }
    
    //-------------------------------------------------------
    // BaseTask
    //-------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        menuItems = new Vector<MenuItemDesc>();
        /*
        final String DATA_MENU = "Specify.DATA_MENU";
        
        SecurityMgr secMgr = SecurityMgr.getInstance();
        
        MenuItemDesc mid;
        JMenuItem mi;
        String    menuDesc = getResourceString(LIFEMAP_MENU);

        String securityName = buildTaskPermissionName(LIFEMAP_SECURITY);
        if (!AppContextMgr.isSecurityOn() || 
            (secMgr.getPermission(securityName) != null && 
             !secMgr.getPermission(securityName).hasNoPerm()))
        {
            mi       = UIHelper.createLocalizedMenuItem(LIFEMAP_MENU, LIFEMAP_MNU, LIFEMAP_TITLE, true, null); 
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            LifeMapperTask.this.requestContext();
                        }
                    });
                }
            });
            mid = new MenuItemDesc(mi, DATA_MENU);
            mid.setPosition(MenuItemDesc.Position.Bottom, menuDesc);
            menuItems.add(mid);
        }
        */
        return menuItems;
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------
    
    /**
     * Processes all Commands of type RECORD_SET.
     * @param cmdAction the command to be processed
     */
    protected void processRecordSetCommands(final CommandAction cmdAction)
    {
        if (cmdAction.isAction("Display"))
        {
            if (cmdAction.getData() instanceof RecordSetIFace)
            {
                RecordSetIFace rs = (RecordSetIFace)cmdAction.getData();
                if (rs.getDbTableId() == Taxon.getClassTableId())
                {
                    // Loop through all the RS ids and get the names
                    //BasicSQLUtils.querySingleObj("SELECT)
                    //((LifeMapperPane)getStarterPane()).doSearchGenusSpecies(searchStr)
                }
            } else if (cmdAction.getData() instanceof Pair<?,?>)
            {
                @SuppressWarnings("unchecked")
                Pair<Taxon, RecordSet> p = (Pair<Taxon, RecordSet>)cmdAction.getData();
                getStarterPane(); // make sure it is loaded
                lmPane.resetWWPanel();
                lmPane.setDoResetWWPanel(false);
                lmPane.doSearchGenusSpecies(p.first.getFullName(), true);
                requestContext();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        super.doCommand(cmdAction);
        
        if (cmdAction.isType(LIFEMAPPER))
        {
            processRecordSetCommands(cmdAction);
        }
    }

}
