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
package edu.ku.brc.specify.core;

import java.util.List;
import java.util.Vector;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.core.subpane.SQLQueryPane;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.TaskPluginable;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

public class QueryTask extends BaseTask
{
    public QueryTask()
    {
        super(getResourceString("Search"));
        
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
     * 
     * @param sqlStr
     */
    public void createAndExecute(final String sqlStr)
    {
        SQLQueryPane sqlPane = new SQLQueryPane(name, this, true);
        UICacheManager.getInstance().getSubPaneMgr().addPane(sqlPane);
        sqlPane.setSQLStr(sqlStr);
        sqlPane.doQuery();
    }
    
    /**
     * 
     */
    public SubPaneIFace getStarterPane()
    {
        return new SQLQueryPane(name, this, false);
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
        
        ToolBarDropDownBtn btn = createToolbarButton(name, "queryIt.gif", "search_hint");
        
        // Create Search Panel
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        
        JPanel     searchPanel = new JPanel(gridbag);
        JLabel     spacer      = new JLabel(" ");
        JTextField searchText  = new JTextField(10);
        JButton    searchBtn   = new JButton(name);
        
        searchText.setMinimumSize(new Dimension(50, searchText.getPreferredSize().height));
        
        c.weightx = 1.0;
        gridbag.setConstraints(spacer, c);
        searchPanel.add(spacer);
        
        c.weightx = 0.0;
        gridbag.setConstraints(searchText, c);
        searchPanel.add(searchText);
        
        searchPanel.add(spacer);
        
        gridbag.setConstraints(searchBtn, c);
        searchPanel.add(searchBtn);
        
        list.add(new ToolBarItemDesc(btn.getCompleteComp()));
        list.add(new ToolBarItemDesc(searchPanel));
        
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
