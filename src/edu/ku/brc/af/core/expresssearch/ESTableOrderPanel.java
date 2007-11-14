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
/**
 * 
 */
package edu.ku.brc.af.core.expresssearch;

import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.IconManager;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 11, 2007
 *
 */
public class ESTableOrderPanel extends JPanel
{
    protected SearchConfig config;
    
    // Table Ordering
    protected JList                                 orderTablesList;
    protected DefaultListModel                      orderTablesModel = new DefaultListModel();
    protected JButton                               orderUpBtn;
    protected JButton                               orderDwnBtn;
    
    protected Hashtable<SearchTableConfig, Boolean> usedHash           = new Hashtable<SearchTableConfig, Boolean>();
    protected Hashtable<String, RelatedQuery>       relatedQueriesHash = new Hashtable<String, RelatedQuery>();
    
    /**
     * 
     */
    public ESTableOrderPanel(final SearchConfig config)
    {
        this.config = config;
        
        PanelBuilder    outer = new PanelBuilder(new FormLayout("f:p:g,2px,p", "p,2px,f:p:g"), this);
        CellConstraints cc      = new CellConstraints();
        
        orderTablesList = new JList(orderTablesModel);
        TableNameRenderer nameRender = new TableNameRenderer(IconManager.IconSize.Std24);
        nameRender.setUseIcon("PlaceHolder");
        orderTablesList.setCellRenderer(nameRender);
        orderTablesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            //@Override
            public void valueChanged(ListSelectionEvent e)
            {
                updateEnabledState();
            }
            
        });
        
        outer.add(new JLabel(getResourceString("ES_ORDER_RET_RES")), cc.xy(1,1));
        outer.add(orderTablesList, cc.xy(1,3));
        
        orderUpBtn = createIconBtn("ReorderUp", "ES_RES_MOVE_UP", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                int inx = orderTablesList.getSelectedIndex();
                DisplayOrderingIFace item = (DisplayOrderingIFace)orderTablesModel.getElementAt(inx);
                DisplayOrderingIFace prev = (DisplayOrderingIFace)orderTablesModel.getElementAt(inx-1);
                
                orderTablesModel.remove(inx);
                orderTablesModel.insertElementAt(item, inx-1);
                orderTablesList.setSelectedIndex(inx-1);
                item.setDisplayOrder(inx-1);
                prev.setDisplayOrder(inx);
                updateEnabledState();
            }
        });
        orderDwnBtn = createIconBtn("ReorderDown", "ES_RES_MOVE_DOWN", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                int    inx      = orderTablesList.getSelectedIndex();
                DisplayOrderingIFace item = (DisplayOrderingIFace)orderTablesModel.getElementAt(inx);
                DisplayOrderingIFace nxt  = (DisplayOrderingIFace)orderTablesModel.getElementAt(inx+1);
               
                orderTablesModel.remove(inx);
                orderTablesModel.insertElementAt(item, inx+1);
                orderTablesList.setSelectedIndex(inx+1);
                item.setDisplayOrder(inx+1);
                nxt.setDisplayOrder(inx);
                updateEnabledState();
            }
        });
        
        PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
        upDownPanel.add(orderUpBtn,       cc.xy(1, 2));
        upDownPanel.add(orderDwnBtn,      cc.xy(1, 4));
        outer.add(upDownPanel.getPanel(), cc.xy(3, 3));
        
        outer.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }
    
    
    /**
     * Update the order up/down btns enabled state
     */
    protected void updateEnabledState()
    {
        int inx = orderTablesList.getSelectedIndex();
        orderUpBtn.setEnabled(inx > 0);
        orderDwnBtn.setEnabled(inx > -1 && inx < orderTablesModel.size()-1);
    }
    
    /**
     * Fills the Order List.
     * @param currSearchableList the current search list of fields
     */
    protected void loadOrderList(final Vector<SearchFieldConfig> currSearchableList)
    {
        Vector<TableNameRendererIFace> tblList = new Vector<TableNameRendererIFace>();
        
        int newOrdercnt = 1000;
        
        usedHash.clear();
        
        // Gather just the tables, see if they have an 'old' order
        // add them to the TableOrder hash with their old or new order
        for (SearchFieldConfig sfc : currSearchableList)
        {
            SearchTableConfig stc = sfc.getStc();
            
            if (stc.hasConfiguredSearchFields())
            {
                if (usedHash.get(stc) == null)
                {
                    if (stc.getDisplayOrder() == null)
                    {
                        // no old order so add one.
                        stc.setDisplayOrder(newOrdercnt++);
                        
                    } else if (stc.getDisplayOrder() == null)
                    {
                        stc.setDisplayOrder(newOrdercnt++);
                    }
                    tblList.add(stc);
                    usedHash.put(stc, true);
                }
            } else
            {
                stc.setDisplayOrder(null); 
            }
        }
        
        // Set all the Related Queries to 'Not In Use'
        // so we can figure out which ones are in use.
        for (RelatedQuery rq : config.getRelatedQueries())
        {
            rq.setInUse(false);
        }
        
        // Now for each Table that was configured, we need to add the related queries
        // We check to see if it already has an order, and then we add it to the has with the old or new order
        
        Hashtable<String, List<ExpressResultsTableInfo>> joinHash = ExpressSearchConfigCache.getJoinIdToTableInfoHash();
        
        Hashtable<String, ExpressResultsTableInfo> duplicateHash = new Hashtable<String, ExpressResultsTableInfo>();
        for (SearchFieldConfig sfc : currSearchableList)
        {
            SearchTableConfig stc = sfc.getStc();
            
            // First does it have any index fields selected to be searched?
            if (stc.hasConfiguredSearchFields())
            {
                // Now for the 'core' table find all the related searches
                List<ExpressResultsTableInfo> joinList = joinHash.get(Integer.toString(stc.getTableInfo().getTableId()));
                if (joinList != null)
                {
                    // Ok, now loop though all the related searches for this table
                    // so each erti refers to the Table in the STC
                    for (ExpressResultsTableInfo erti : joinList)
                    {
                        // Check to see if the related has already be added.
                        if (duplicateHash.get(erti.getId()) == null)
                        {
                            // Ok, now let's add the erti (related search)
                            RelatedQuery rq = relatedQueriesHash.get(erti.getId());
                            if (rq == null)
                            {
                                // Check to see if this has already been configured
                                // if it can't find it then it is suppose to create a new one
                                rq = config.findRelatedQuery(erti.getId(), true);
                                if (rq.getDisplayOrder().intValue() == Integer.MAX_VALUE)
                                {
                                    rq.setDisplayOrder(newOrdercnt++);
                                }
                                relatedQueriesHash.put(erti.getId(), rq); // Id is guaranteed to be unique
                                //System.out.println("* "+erti.getId() + " " + DBTableIdMgr.getInstance().getInfoById(Integer.parseInt(erti.getId())).getClassObj().getSimpleName());
                                
                            } else if (rq.getDisplayOrder() == null)
                            {
                                rq.setDisplayOrder(newOrdercnt++);
                            }
                            rq.setErti(erti);
                            rq.setInUse(true);
                            
                            duplicateHash.put(erti.getId(), erti);
                        }
                    }
                }
            }
        }
        
        // Now add in all the Related Queries
        for (RelatedQuery rq : config.getRelatedQueries())
        {
            if (rq.isInUse())
            {
                //System.out.println("* "+rq.getDisplayOrder()+ "  "+rq.getTitle());
                tblList.add(rq);
            } 
        }
        
        // Clear the model
        orderTablesModel.clear();
        
        // Sort the list containing both STC and Related Queries
        Collections.sort(tblList, new Comparator<TableNameRendererIFace>() {
            //@Override
            public int compare(TableNameRendererIFace o1, TableNameRendererIFace o2)
            {
                return ((DisplayOrderingIFace)o1).getDisplayOrder().compareTo(((DisplayOrderingIFace)o2).getDisplayOrder());
            }
        });
        
        // Now fill the model
        int i = 0;
        for (TableNameRendererIFace sr : tblList)
        {
            //System.out.println(((DisplayOrderingIFace)sr).getDisplayOrder()+ "  "+sr.getTitle());
            ((DisplayOrderingIFace)sr).setDisplayOrder(i++);
            orderTablesModel.addElement(sr);
        }
    }
    
    /**
     * 
     */
    protected void grabOrderInList()
    {
        
    }
    
    /**
     * Save the changes from the list of display items to be ordered.
     * @param currSearchableList
     */
    public void saveChanges(final Vector<SearchFieldConfig> currSearchableList)
    {
        // If they have made changes put have not readjusted the order
        // we can give it a default order by loading it into the UI before saving.
        loadOrderList(currSearchableList);
        
        /*
        TableOrderRoot root = tableOrderingService.getTableOrderRoot();
        root.getOrder().clear();
        
        for (int i=0;i<orderTablesModel.size();i++)
        {
            TableOrder tblOrder = (TableOrder)orderTablesModel.get(i);
            tblOrder.setOrder(i);
            root.getOrder().add(tblOrder);
        }*/
    }
}
