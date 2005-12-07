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

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.util.*;
import java.util.Vector;

import edu.ku.brc.specify.core.DataEntryTask.DataEntryAction;
import edu.ku.brc.specify.core.subpane.SimpleDescPane;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.*;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;

import edu.ku.brc.specify.config.SpecifyConfig;
import edu.ku.brc.specify.core.DataEntryTask;
import edu.ku.brc.specify.core.InteractionsTask;
import edu.ku.brc.specify.core.LabelsTask;
import edu.ku.brc.specify.core.QueryTask;
import edu.ku.brc.specify.core.ReportsTask;
import edu.ku.brc.specify.core.StatsTask;
import edu.ku.brc.specify.dbsupport.*;
import edu.ku.brc.specify.datamodel.*;
import edu.ku.brc.specify.plugins.PluginMgr;
import edu.ku.brc.specify.ui.GenericFrame;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.MainPanel;
import edu.ku.brc.specify.ui.PropertyViewer;
import edu.ku.brc.specify.ui.ToolbarLayoutManager;
import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.dbsupport.*;
/**
 * 
 * @author rods
 *
 */
public class RecordSetTask extends BaseTask
{
    public static final String RECORD_SET = "Record_Set";
    
    /**
     * Default Constructor
     *
     */
    public RecordSetTask()
    {
        super(RECORD_SET, getResourceString(RECORD_SET));
        
        NavBox navBox = new NavBox(title);
        
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(RecordSet.class);
        List recordSets = criteria.list();
          
        for (Iterator iter=recordSets.iterator();iter.hasNext();)
        {
            RecordSet recordSet = (RecordSet)iter.next();
            navBox.add(NavBox.createBtn(recordSet.getName(), name, IconManager.IconSize.Std16));
        }          
        navBoxes.addElement(navBox);
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return new SimpleDescPane(name, this, "This is the Data Entry Pane");
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
        return RECORD_SET; // XXX Localize, Hmmm maybe not????
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        
        //ToolBarDropDownBtn btn = createToolbarButton(RECORD_SET,   "dataentry.gif",    "dataentry_hint");
       
        //list.add(new ToolBarItemDesc(btn.getCompleteComp()));
        
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
    

}
