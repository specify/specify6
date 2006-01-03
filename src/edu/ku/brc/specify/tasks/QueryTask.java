/* Filename:    $RCSfile: QueryTask.java,v $
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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;

import edu.ku.brc.specify.core.NavBox;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.tasks.subpane.SQLQueryPane;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import edu.ku.brc.specify.ui.UICacheManager;

/**
 * This task will enable the user to create queries, save them and execute them.
 * 
 * @author rods
 *
 */
public class QueryTask extends BaseTask
{
    public static final String QUERY = "Search";
    
    protected Vector<ToolBarDropDownBtn> tbList = new Vector<ToolBarDropDownBtn>();
    protected Vector<JComponent>          menus  = new Vector<JComponent>();
    
    
    /**
     * Default Constructor
     *
     */
    public QueryTask()
    {
        super(QUERY, getResourceString(QUERY));
        
        // XXX Localize
        // Temporary
        NavBox navBox = new NavBox(name);
        navBox.add(NavBox.createBtn(getResourceString("New"), name, IconManager.IconSize.Std16, new QueryAction("select * from picklist where picklist_id = 3")));
        navBox.add(NavBox.createBtn(getResourceString("Advanced"), name, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("Specimen"), name, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("Taxonomic"), name, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("Geographic"), name, IconManager.IconSize.Std16));
        navBoxes.addElement(navBox);
        
        navBox = new NavBox(getResourceString("Saved_Searches"));
        navBox.add(NavBox.createBtn("Species Counts", name, IconManager.IconSize.Std16, new QueryAction("select SpeciesName,count(tx) as SpeciesCount from (select determination.TaxonNameId, taxonname.TaxonNameID as tx, taxonname.TaxonName as SpeciesName from taxonname,determination where determination.TaxonNameId = taxonname.taxonnameid) as newTable group by tx order by SpeciesCount DESC;")));
        navBox.add(NavBox.createBtn("Picklist", name, IconManager.IconSize.Std16));
        navBoxes.addElement(navBox);
    }
    
    /**
     * CReates pane and executes a query
     * @param sqlStr SQL to be executed
     */
    public void createAndExecute(final String sqlStr)
    {
        SQLQueryPane sqlPane = new SQLQueryPane(name, this, false, false);//true, true);
        UICacheManager.getInstance().getSubPaneMgr().addPane(sqlPane);
        sqlPane.setSQLStr(sqlStr);
        sqlPane.doQuery();
        
        // XXX Example Code
        // This is an example of how to add menu items to a menu in a Toolbar Button
        /*
        if (menus != null && menus.size() == 0)
        {
            ToolBarDropDownBtn tb = tbList.elementAt(0);
            menus.add(new JMenuItem("Hello"));
            tb.propertyChange(null);
        }*/
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return new SQLQueryPane(name, this, false, false);
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
        
        ToolBarDropDownBtn btn = createToolbarButton(name, "queryIt.gif", "search_hint", menus);
        if (tbList.size() == 0)
        {
            tbList.add(btn);
        }
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
    class QueryAction implements ActionListener 
    {
        private String queryStr;
        
        public QueryAction(final String queryStr)
        {
            this.queryStr = queryStr;
        }
        public void actionPerformed(ActionEvent e) 
        {
            createAndExecute(queryStr);
        }
    }
    

}
