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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.Criteria;

import edu.ku.brc.specify.core.subpane.SimpleDescPane;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.RolloverCommand;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.dnd.GhostActionable;
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
        
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize();super.initialize(); // sets isInitialized to false
            
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(RecordSet.class);
            List recordSets = criteria.list();
              
            NavBox navBox = new NavBox(title);
            
            for (Iterator iter=recordSets.iterator();iter.hasNext();)
            {
                RecordSet recordSet = (RecordSet)iter.next();
                
                NavBoxItemIFace nb = NavBox.createBtn(recordSet.getName(), name, IconManager.IconSize.Std16);
                navBox.add(nb);
                
                if (nb instanceof GhostActionable)
                {
                    GhostActionable ga = (GhostActionable)nb;
                    ga.createMouseDropAdapter();
                    ga.setData(recordSet);
                }
            }          
            navBoxes.addElement(navBox);
        }
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
}
