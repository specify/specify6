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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;

/**
 * A dialog used for Configuring the 'create' or new Queries.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Feb 28, 2008
 *
 */
public class QueryConfigureDlg extends CustomDialog
{
    protected QueryTask             task;
    
    protected Vector<SpQuery>       favQueries        = new Vector<SpQuery>();
    protected Vector<SpQuery>       otherQueries      = new Vector<SpQuery>();
    
    protected JButton               mvToOtherBtn;
    protected JButton               mvToFreqBtn;

    protected QueryOrderPanel       favPanel;
    protected QueryOrderPanel       otherPanel;
    
    protected boolean               hasChanged = false;
    
    /**
     * Creates the Dialog for configuring the queries
     * @param task the QueryTask
     */
    public QueryConfigureDlg(final QueryTask task)
    {
        super((Frame)getTopWindow(), getResourceString("QY_CONFIGURE_QUERIES"), true, OKCANCELHELP, null);
        
        this.task = task;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        HelpMgr.registerComponent(helpBtn, "QBSavedQueriesConfig"); 
        
        String sqlStr = "FROM SpQuery as sq Inner Join sq.specifyUser as user where user.specifyUserId = "+SpecifyUser.getCurrentUser().getSpecifyUserId() + " ORDER BY ordinal";

        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            List<?> rows = session.getDataList(sqlStr);
            
            // Add the non-favorite queries to the extra list
            for (Object row : rows)
            {
                Object[] objs = (Object[])row;
                SpQuery query = (SpQuery)objs[0];
                
                if (query.getIsFavorite())
                {
                    favQueries.add(query);
                } else
                {
                    otherQueries.add(query);
                }
            }
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        favPanel   = new QueryOrderPanel("QY_FAV_LIST", favQueries, true, true);
        otherPanel = new QueryOrderPanel("QY_OTHER_LIST", otherQueries, false, false);
        
        mvToOtherBtn = createIconBtn("Map", "QY_MOVE_TO_OTHER_TT", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                moveToOther();
            }
        });
        mvToFreqBtn = createIconBtn("Unmap", "QY_MOVE_TO_FAV_TT", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                moveToFav();
            }
        });
        
        CellConstraints cc     = new CellConstraints();
        
        PanelBuilder middlePanel = new PanelBuilder(new FormLayout("c:p:g", "p, 2px, p"));
        middlePanel.add(mvToOtherBtn, cc.xy(1, 1));
        middlePanel.add(mvToFreqBtn, cc.xy(1, 3));

        PanelBuilder middlePanel2 = new PanelBuilder(new FormLayout("c:p:g", "f:p:g, p, f:p:g"));
        middlePanel2.add(middlePanel.getPanel(), cc.xy(1, 2));

        PanelBuilder mainPB = new PanelBuilder(new FormLayout("f:p:g,5px,p,10px,f:p:g", "f:p:g"));
        mainPB.add(favPanel,  cc.xy(1,1));
        mainPB.add(middlePanel2.getPanel(),  cc.xy(3,1));
        mainPB.add(otherPanel, cc.xy(5,1));

        mainPB.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        contentPanel = mainPB.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        pack();
        
        Dimension size = getSize();
        size.width  = Math.max(size.width, 500);
        size.height = Math.max(size.height, 350);
        setSize(size);
    }
    
    /**
     * Moves query from freq list to extra
     */
    protected void moveToOther()
    {
        int inx = favPanel.getOrderList().getSelectedIndex();
        if (inx > -1)
        {
            Object name = favPanel.getOrderList().getSelectedValue();
            ((DefaultListModel)favPanel.getOrderList().getModel()).removeElement(name);
            ((DefaultListModel)otherPanel.getOrderList().getModel()).addElement(name);
            //pack();
        }
    }
    
    /**
     * 
     */
    protected void moveToFav()
    {
        int inx = otherPanel.getOrderList().getSelectedIndex();
        if (inx > -1)
        {
            Object name = otherPanel.getOrderList().getSelectedValue();
            ((DefaultListModel)otherPanel.getOrderList().getModel()).removeElement(name);
            ((DefaultListModel)favPanel.getOrderList().getModel()).addElement(name);
            //pack();
        }
    }
    
    /**
     * @param hasChangedArg
     */
    protected void setHasChanged(final boolean hasChangedArg)
    {
        hasChanged = hasChangedArg;
        getOkBtn().setEnabled(hasChanged);
    }
   
    /**
     * @return the freq quesries lis
     */
    public Vector<SpQuery> getFavQueries()
    {
        return favPanel.getQueries();
    }

    /**
     * @return the extra queries list
     */
    public Vector<SpQuery> getOtherQueries()
    {
        return otherPanel.getQueries();
    }

    /**
     * Removes an item from the list
     * @param list the list
     */
    protected void removeItem(final JList list)
    {
        int index = list.getSelectedIndex();
        if (index > -1)
        {
            ((DefaultListModel)list.getModel()).remove(index);
            setHasChanged(true);
            list.repaint();
        }
    }

    /**
     * @param list
     */
    protected void editItem(final JList list)
    {
    }

    /**
     * Item selected in either list.
     */
    protected void itemSelected()
    {
        mvToOtherBtn.setEnabled(!favPanel.getOrderList().isSelectionEmpty());
        mvToFreqBtn.setEnabled(!otherPanel.getOrderList().isSelectionEmpty());
    }
    
    //---------------------------------------------------------------------
    public class QueryOrderPanel extends JPanel
    {
        // Table Ordering
        protected JList                     orderList;
        protected DefaultListModel          orderModel;
        protected JButton                   orderUpBtn;
        protected JButton                   orderDwnBtn;
        
        /**
         * 
         */
        public QueryOrderPanel(final String titleKey, 
                               final Vector<SpQuery> queries,
                               final boolean orderOnLeft,
                               final boolean hideOrdering)
        {
            
            PanelBuilder    outer = new PanelBuilder(new FormLayout(orderOnLeft ? "p,2px,f:p:g" : "f:p:g,2px,p", "p,2px,f:p:g"), this);
            CellConstraints cc    = new CellConstraints();
            
            orderModel = new DefaultListModel();
            for (SpQuery q : queries)
            {
                orderModel.addElement(q);
            }
            orderList = new JList(orderModel);
            orderList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                //@Override
                public void valueChanged(ListSelectionEvent e)
                {
                    updateEnabledState();
                    itemSelected();
                }
            });
            
            orderUpBtn = createIconBtn("ReorderUp", "QY_MOVE_UP", new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    int inx = orderList.getSelectedIndex();
                    SpQuery item = (SpQuery)orderModel.getElementAt(inx);
                    
                    orderModel.remove(inx);
                    orderModel.insertElementAt(item, inx-1);
                    orderList.setSelectedIndex(inx-1);
                    updateEnabledState();
                }
            });
            orderDwnBtn = createIconBtn("ReorderDown", "QY_MOVE_DOWN", new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    int inx = orderList.getSelectedIndex();
                    SpQuery item = (SpQuery)orderModel.getElementAt(inx);
                   
                    orderModel.remove(inx);
                    orderModel.insertElementAt(item, inx+1);
                    orderList.setSelectedIndex(inx+1);
                    updateEnabledState();
                }
            });
            
            int col = orderOnLeft ? 3 : 1;
            outer.add(createLabel(getResourceString(titleKey), SwingConstants.CENTER), cc.xy(col, 1));
            JScrollPane sp = new JScrollPane(orderList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            outer.add(sp,                     cc.xy(col, 3));

            if (hideOrdering)
            {
                PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
                upDownPanel.add(orderUpBtn,       cc.xy(1, 2));
                upDownPanel.add(orderDwnBtn,      cc.xy(1, 4));
                outer.add(upDownPanel.getPanel(), cc.xy(orderOnLeft ? 1 : 3, 3));
            }
            
            outer.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        }
        
        
        /**
         * Update the order up/down btns enabled state
         */
        protected void updateEnabledState()
        {
            int inx = orderList.getSelectedIndex();
            orderUpBtn.setEnabled(inx > 0);
            orderDwnBtn.setEnabled(inx > -1 && inx < orderModel.size()-1);
        }


        /**
         * @return the orderList
         */
        public JList getOrderList()
        {
            return orderList;
        }

        /**
         * @return the orderModel
         */
        public Vector<SpQuery> getQueries()
        {
            Vector<SpQuery> queries = new Vector<SpQuery>();
            for (int i=0;i<orderModel.size();i++)
            {
                queries.add((SpQuery)orderModel.get(i));
            }
            return queries;
        }
    }
}
