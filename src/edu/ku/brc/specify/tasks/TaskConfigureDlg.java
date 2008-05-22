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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
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

import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.AddRemoveEditPanel;
import edu.ku.brc.ui.CustomDialog;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Feb 27, 2008
 *
 */
public class TaskConfigureDlg extends CustomDialog
{
    //private static final Logger log = Logger.getLogger(DataEntryConfigureDlg.class);
    
    protected boolean                includeAddPanels = false;
    protected String                 helpContext;
    protected String                 stdTitleKey;
    protected String                 miscTitleKey;
    protected String                 mvRightTTKey;
    protected String                 mvLeftTTKey;
    
    protected Vector<TaskConfigItemIFace> stdViews       = null;
    protected Vector<TaskConfigItemIFace> miscViews      = null;
    protected Vector<TaskConfigItemIFace> removedItems   = null;
    
    protected JButton                mvToMiscBtn;
    protected JButton                mvToStdBtn;

    
    protected ItemsOrderPanel        stdPanel;
    protected ItemsOrderPanel        miscPanel;
    
    protected boolean                hasChanged = false;
    
    /**
     * @param stdList the left side list
     * @param miscList the right side list
     * @param includeAddPanels whether to include the add,delete,edit panels
     * @param helpContext the context for the help button
     * @param titleKey the locale title key
     * @param stdTitle the locale title key for the left side list
     * @param miscTitle the locale title key for the right side list
     * @param mvRightTTKey the locale title key for the tooltip for the move right btn
     * @param mvLeftTTKey the locale title key for the tooltip for the move right btn
     */
    public TaskConfigureDlg(final Vector<TaskConfigItemIFace> stdList, 
                                   final Vector<TaskConfigItemIFace> miscList,
                                   final boolean includeAddPanels,
                                   final String helpContext,
                                   final String titleKey,
                                   final String stdTitle,
                                   final String miscTitle,
                                   final String mvRightTTKey,
                                   final String mvLeftTTKey)
    {
        super((Frame)getTopWindow(), getResourceString(titleKey), true, OKCANCELHELP, null);
        
        this.stdViews      = stdList;
        this.miscViews     = miscList;
        this.includeAddPanels = includeAddPanels;
        this.helpContext   = helpContext;
        this.stdTitleKey   = stdTitle;
        this.miscTitleKey  = miscTitle;
        this.mvRightTTKey  = mvRightTTKey;
        this.mvLeftTTKey   = mvLeftTTKey;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        HelpMgr.registerComponent(helpBtn, helpContext); 
        
        stdPanel  = new ItemsOrderPanel(stdTitleKey,  stdViews,  true,  includeAddPanels);
        miscPanel = new ItemsOrderPanel(miscTitleKey, miscViews, false, includeAddPanels);
        
        mvToMiscBtn = createIconBtn("Map", mvRightTTKey, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                moveToMisc();
            }
        });
        mvToStdBtn = createIconBtn("Unmap", mvLeftTTKey, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                moveToStd();
            }
        });
        
        CellConstraints cc     = new CellConstraints();
        
        PanelBuilder middlePanel = new PanelBuilder(new FormLayout("c:p:g", "p, 2px, p"));
        middlePanel.add(mvToMiscBtn, cc.xy(1, 1));
        middlePanel.add(mvToStdBtn, cc.xy(1, 3));

        PanelBuilder middlePanel2 = new PanelBuilder(new FormLayout("c:p:g", "f:p:g, p, f:p:g"));
        middlePanel2.add(middlePanel.getPanel(), cc.xy(1, 2));

        PanelBuilder mainPB = new PanelBuilder(new FormLayout("p:g,5px,p,10px,p:g", "f:p:g"));
        mainPB.add(stdPanel,  cc.xy(1,1));
        mainPB.add(middlePanel2.getPanel(),  cc.xy(3,1));
        mainPB.add(miscPanel, cc.xy(5,1));

        mainPB.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        contentPanel = mainPB.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        pack();
        
        Dimension size = getSize();
        size.width  = Math.max(size.width, 500);
        size.height = Math.max(size.height, 350);
        setSize(size);
        
        stdPanel.getOrderList().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                if (e.getClickCount() == 2)
                {
                    moveToMisc();
                }
            }
            
        });
        
        miscPanel.getOrderList().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                if (e.getClickCount() == 2)
                {
                    moveToStd();
                }
            }
            
        });
    }
    
    /**
     * 
     */
    protected void moveToMisc()
    {
        int inx = stdPanel.getOrderList().getSelectedIndex();
        if (inx > -1)
        {
            Object dev = stdPanel.getOrderList().getSelectedValue();
            ((DefaultListModel)stdPanel.getOrderList().getModel()).removeElement(dev);
            ((DefaultListModel)miscPanel.getOrderList().getModel()).addElement(dev);
            //pack();
        }
    }
    
    /**
     * 
     */
    protected void moveToStd()
    {
        int inx = miscPanel.getOrderList().getSelectedIndex();
        if (inx > -1)
        {
            Object dev = miscPanel.getOrderList().getSelectedValue();
            ((DefaultListModel)miscPanel.getOrderList().getModel()).removeElement(dev);
            ((DefaultListModel)stdPanel.getOrderList().getModel()).addElement(dev);
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
     * @param list
     */
    protected void addItem(final JList list, @SuppressWarnings("unused")
                                             final Vector<TaskConfigItemIFace> itemList)
    {

    }
    
    /**
     * @return the removedItems
     */
    public Vector<TaskConfigItemIFace> getRemovedItems()
    {
        return removedItems;
    }

    /**
     * Remove item from list.
     * @param list the list to be removed from.
     */
    protected void removeItem(final JList list)
    {
        int index = list.getSelectedIndex();
        if (index > -1)
        {
            TaskConfigItemIFace item = (TaskConfigItemIFace)((DefaultListModel)list.getModel()).get(index);
            if (item != null)
            {
                if (removedItems == null)
                {
                    removedItems = new Vector<TaskConfigItemIFace>();
                }
                
                removedItems.add(item);
                ((DefaultListModel)list.getModel()).remove(index);
                setHasChanged(true);
                list.repaint();
            }
        }
    }

    /**
     * @param list
     */
    protected void editItem(final JList list)
    {
    }

    /**
     * item selected in either list.
     */
    protected void itemSelected()
    {
        mvToMiscBtn.setEnabled(!stdPanel.getOrderList().isSelectionEmpty());
        mvToStdBtn.setEnabled(!miscPanel.getOrderList().isSelectionEmpty());
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        stdPanel.doOrderItems();
        miscPanel.doOrderItems();
        
        super.okButtonPressed();
    }

    //---------------------------------------------------------------------
    public class ItemsOrderPanel extends JPanel
    {
        // Table Ordering
        protected JList                                 orderList;
        protected DefaultListModel                      orderModel;
        protected JButton                               orderUpBtn;
        protected JButton                               orderDwnBtn;
        
        protected AddRemoveEditPanel                    arePanel = null;
        protected Vector<TaskConfigItemIFace>           items;
        protected Vector<TaskConfigItemIFace>           hiddenItems = new Vector<TaskConfigItemIFace>();
        
        /**
         * @param titleKey
         * @param items
         * @param orderOnLeft
         * @param includeAREPanel
         */
        public ItemsOrderPanel(final String titleKey, 
                               final Vector<TaskConfigItemIFace> items,
                               final boolean orderOnLeft,
                               final boolean includeAREPanel)
        {
            
            PanelBuilder    outer = new PanelBuilder(new FormLayout(orderOnLeft ? "p,2px,f:p:g" : "f:p:g,2px,p", 
                                                       "p,2px,f:p:g" + (includeAREPanel ? ",2px,p" : "")), this);
            CellConstraints cc    = new CellConstraints();
            
            this.items = items;
            
            Collections.sort(items);
            orderModel = new DefaultListModel();
            for (TaskConfigItemIFace item : items)
            {
                if (item.isVisible())
                {
                    orderModel.addElement(item);
                } else
                {
                    hiddenItems.add(item);
                }
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
            
            orderUpBtn = createIconBtn("ReorderUp", "TCGD_MOVE_UP", new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    int inx = orderList.getSelectedIndex();
                    TaskConfigItemIFace item = (TaskConfigItemIFace)orderModel.getElementAt(inx);
                    
                    orderModel.remove(inx);
                    orderModel.insertElementAt(item, inx-1);
                    orderList.setSelectedIndex(inx-1);
                    updateEnabledState();
                }
            });
            orderDwnBtn = createIconBtn("ReorderDown", "TCGD_MOVE_DOWN", new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    int inx = orderList.getSelectedIndex();
                    TaskConfigItemIFace item = (TaskConfigItemIFace)orderModel.getElementAt(inx);
                   
                    orderModel.remove(inx);
                    orderModel.insertElementAt(item, inx+1);
                    orderList.setSelectedIndex(inx+1);
                    updateEnabledState();
                }
            });
            
            if (includeAREPanel)
            {
                arePanel = new AddRemoveEditPanel(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        addItem(orderList, items);
                    }
                }, new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        removeItem(orderList);
                    }
                }, null);
                arePanel.getAddBtn().setEnabled(true);
            }
            
            PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
            upDownPanel.add(orderUpBtn,       cc.xy(1, 2));
            upDownPanel.add(orderDwnBtn,      cc.xy(1, 4));
            
            int col = orderOnLeft ? 3 : 1;
            outer.add(createLabel(getResourceString(titleKey), SwingConstants.CENTER), cc.xy(col, 1));
            JScrollPane sp = new JScrollPane(orderList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            outer.add(sp,                     cc.xy(col, 3));
            
            if (arePanel != null)
            {
                outer.add(arePanel, cc.xy(col, 5));
            }
            
            outer.add(upDownPanel.getPanel(), cc.xy(orderOnLeft ? 1 : 3, 3));
            
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
            if (arePanel != null)
            {
                arePanel.setEnabled(inx > -1);
                arePanel.getAddBtn().setEnabled(true);
            }
        }
        
        /**
         * Sets the order in the items.
         */
        public void doOrderItems()
        {
            // clear the List
            items.clear();
            
            // Fill it back in and order the items
            int order;
            for (order=0;order<orderModel.size();order++)
            {
                TaskConfigItemIFace item = (TaskConfigItemIFace)orderModel.get(order);
                item.setOrder(order);
                items.add(item);
            }
            
            // Add in the Hidden items
            for (TaskConfigItemIFace item : hiddenItems)
            {
                item.setOrder(order++);
                items.add(item);
            }
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
        protected DefaultListModel getOrderModel()
        {
            return orderModel;
        }
    }
}
