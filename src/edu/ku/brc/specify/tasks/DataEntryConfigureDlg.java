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
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.AddRemoveEditPanel;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.persist.ViewIFace;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Feb 27, 2008
 *
 */
public class DataEntryConfigureDlg extends CustomDialog
{
    //private static final Logger log = Logger.getLogger(DataEntryConfigureDlg.class);
    
    protected DataEntryTask task;
    
    protected Vector<DataEntryView> stdViews       = null;
    protected Vector<DataEntryView> miscViews      = null;
    
    protected JButton               mvToMiscBtn;
    protected JButton               mvToStdBtn;

    
    protected ViewsOrderPanel       stdPanel;
    protected ViewsOrderPanel       miscPanel;
    
    protected boolean               hasChanged = false;
    
    /**
     * @param task
     */
    public DataEntryConfigureDlg(final DataEntryTask task)
    {
        super((Frame)getTopWindow(), getResourceString("DET_CONFIGURE_VIEWS"), true, OKCANCELHELP, null);
        
        this.task = task;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        HelpMgr.registerComponent(helpBtn, "DataEntryConfigure"); 
        
        stdViews  = task.getStdViews();
        miscViews = task.getMiscViews();
        
        stdPanel  = new ViewsOrderPanel("DET_STANDARD", stdViews, true);
        miscPanel = new ViewsOrderPanel("DET_MISC", miscViews, false);
        
        mvToMiscBtn = createIconBtn("Map", "DET_MOVE_TO_MISC_TT", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                moveToMisc();
            }
        });
        mvToStdBtn = createIconBtn("Unmap", "DET_MOVE_TO_STD_TT", new ActionListener()
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
    protected void addItem(final JList list)
    {
        Hashtable<String, Object> hash = new Hashtable<String, Object>();
        ListModel model = stdPanel.getOrderModel();
        for (int i=0;i<model.getSize();i++)
        {
            DataEntryView dev = (DataEntryView)model.getElementAt(i);
            hash.put(dev.getView(), dev);
        }
        
        model = miscPanel.getOrderModel();
        for (int i=0;i<model.getSize();i++)
        {
            DataEntryView dev = (DataEntryView)model.getElementAt(i);
            hash.put(dev.getView(), dev);
        }
        
        List<String>                 uniqueList    = new Vector<String>();
        List<ViewIFace>              views         = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getAllViews();
        Hashtable<String, ViewIFace> newAvailViews = new Hashtable<String, ViewIFace>();
        for (ViewIFace view : views)
        {
            if (hash.get(view.getName()) == null)
            {
                hash.put(view.getName(), view);
                uniqueList.add(view.getObjTitle());
                newAvailViews.put(view.getObjTitle(), view);
            }
        }
        
        if (uniqueList.size() == 0)
        {
            JOptionPane.showMessageDialog(this, getResourceString("DET_DEV_NONE_AVAIL"), 
                    getResourceString("DET_DEV_NONE_AVAIL_TITLE"), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        ToggleButtonChooserDlg<String> dlg = new ToggleButtonChooserDlg<String>((Frame)UIRegistry.getTopWindow(),
                getResourceString("DET_AVAIL_VIEWS"), uniqueList);
        
        dlg.setUseScrollPane(true);
        UIHelper.centerAndShow(dlg);
        
        if (!dlg.isCancelled())
        {
            model = list.getModel();
            
            for (String name : dlg.getSelectedObjects())
            {
                ViewIFace     view = newAvailViews.get(name);
                DBTableInfo   ti   = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                DataEntryView dev  = new DataEntryView(view.getObjTitle(), view.getName(), 
                                                       ti != null ? ti.getName() : null, view.getObjTitle(), true);
                
                ((DefaultListModel)model).addElement(dev);
            }
            //pack();
        }
        setHasChanged(true);
    }
    
    /**
     * @param panel
     * @return
     */
    protected Vector<DataEntryView> getViews(ViewsOrderPanel panel)
    {
        Vector<DataEntryView> list = new Vector<DataEntryView>();
        for (int i=0;i<panel.getOrderModel().getSize();i++)
        {
            list.add((DataEntryView)panel.getOrderModel().getElementAt(i));
        }
        return list;
    }

    /**
     * @return
     */
    public Vector<DataEntryView> getStdViews()
    {
        return getViews(stdPanel);
    }

    /**
     * @return
     */
    public Vector<DataEntryView> getMiscViews()
    {
        return getViews(miscPanel);
    }

    /**
     * @param list
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
     * item selected in either list.
     */
    protected void itemSelected()
    {
        mvToMiscBtn.setEnabled(!stdPanel.getOrderList().isSelectionEmpty());
        mvToStdBtn.setEnabled(!miscPanel.getOrderList().isSelectionEmpty());
    }
    
    //---------------------------------------------------------------------
    public class ViewsOrderPanel extends JPanel
    {
        // Table Ordering
        protected JList                                 orderList;
        protected DefaultListModel                      orderModel;
        protected JButton                               orderUpBtn;
        protected JButton                               orderDwnBtn;
        
        protected AddRemoveEditPanel                    arePanel;
        
        /**
         * 
         */
        public ViewsOrderPanel(final String titleKey, 
                               final Vector<DataEntryView> views,
                               final boolean orderOnLeft)
        {
            
            PanelBuilder    outer = new PanelBuilder(new FormLayout(orderOnLeft ? "p,2px,f:p:g" : "f:p:g,2px,p", "p,2px,f:p:g,2px,p"), this);
            CellConstraints cc    = new CellConstraints();
            
            orderModel = new DefaultListModel();
            for (DataEntryView dev : views)
            {
                if (dev.isSideBar())
                {
                    orderModel.addElement(dev);
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
            
            orderUpBtn = createIconBtn("ReorderUp", "DET_MOVE_UP", new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    int inx = orderList.getSelectedIndex();
                    DataEntryView item = (DataEntryView)orderModel.getElementAt(inx);
                    
                    orderModel.remove(inx);
                    orderModel.insertElementAt(item, inx-1);
                    orderList.setSelectedIndex(inx-1);
                    updateEnabledState();
                }
            });
            orderDwnBtn = createIconBtn("ReorderDown", "DET_MOVE_DOWN", new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    int inx = orderList.getSelectedIndex();
                    DataEntryView item = (DataEntryView)orderModel.getElementAt(inx);
                   
                    orderModel.remove(inx);
                    orderModel.insertElementAt(item, inx+1);
                    orderList.setSelectedIndex(inx+1);
                    updateEnabledState();
                }
            });
            
            arePanel = new AddRemoveEditPanel(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    addItem(orderList);
                }
            }, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    removeItem(orderList);
                }
            }, null);
            
            PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
            upDownPanel.add(orderUpBtn,       cc.xy(1, 2));
            upDownPanel.add(orderDwnBtn,      cc.xy(1, 4));
            
            int col = orderOnLeft ? 3 : 1;
            outer.add(createLabel(getResourceString(titleKey), SwingConstants.CENTER), cc.xy(col, 1));
            JScrollPane sp = new JScrollPane(orderList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            outer.add(sp,                     cc.xy(col, 3));
            outer.add(arePanel,               cc.xy(col, 5));
            
            outer.add(upDownPanel.getPanel(), cc.xy(orderOnLeft ? 1 : 3, 3));
            
            outer.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            
            arePanel.getAddBtn().setEnabled(true);
        }
        
        
        /**
         * Update the order up/down btns enabled state
         */
        protected void updateEnabledState()
        {
            int inx = orderList.getSelectedIndex();
            orderUpBtn.setEnabled(inx > 0);
            orderDwnBtn.setEnabled(inx > -1 && inx < orderModel.size()-1);
            arePanel.setEnabled(inx > -1);
            arePanel.getAddBtn().setEnabled(true);
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
        public DefaultListModel getOrderModel()
        {
            return orderModel;
        }
    }
}
