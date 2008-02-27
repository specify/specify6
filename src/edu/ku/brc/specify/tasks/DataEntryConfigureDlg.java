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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.ui.AddRemoveEditPanel;
import edu.ku.brc.ui.CustomDialog;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Feb 27, 2008
 *
 */
public class DataEntryConfigureDlg extends CustomDialog
{
    private static final Logger log = Logger.getLogger(DataEntryConfigureDlg.class);
    
    protected DataEntryTask task;
    
    protected Vector<DataEntryView> stdViews       = null;
    protected Vector<DataEntryView> miscViews      = null;
    
    protected ViewsOrderPanel       stdPanel;
    protected ViewsOrderPanel       miscPanel;
    
    protected AddRemoveEditPanel    stdAREPanel;
    protected AddRemoveEditPanel    miscAREPanel;
    
    protected boolean               hasChanged = false;

    
    public DataEntryConfigureDlg(final DataEntryTask task)
    {
        super((Frame)getTopWindow(), getResourceString("Configure"), true, null);
        
        this.task = task;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        try
        {
            XStream xstream = new XStream();
            
            DataEntryXML.config(xstream);
            DataEntryView.config(xstream);
            
            DataEntryXML dataEntryXML = (DataEntryXML)xstream.fromXML(AppContextMgr.getInstance().getResourceAsXML("DataEntryTaskInit")); // Describes the definitions of the full text search);
            stdViews  = dataEntryXML.getStd();
            miscViews = dataEntryXML.getMisc();
            
            Collections.sort(stdViews);
            
            stdPanel  = new ViewsOrderPanel("Standard", stdViews);
            miscPanel = new ViewsOrderPanel("Misc", miscViews);
            
            CellConstraints cc     = new CellConstraints();

            PanelBuilder mainPB = new PanelBuilder(new FormLayout("p,10px,p", "p"));
            mainPB.add(stdPanel,  cc.xy(1,1));
            mainPB.add(miscPanel, cc.xy(3,1));

            mainPB.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            contentPanel = mainPB.getPanel();
            mainPanel.add(contentPanel, BorderLayout.CENTER);
            
            pack();
            
        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
        }
    }
    
    protected void setHasChanged(final boolean hasChangedArg)
    {
        hasChanged = hasChangedArg;
        getOkBtn().setEnabled(hasChanged);
    }
    
    protected void enableBtns(final JList list)
    {
       // int selectedIndex = list.getSelectedIndex();
        
    }
    
    protected void addItem(final JList list)
    {
        //int index = list.getSelectedIndex();
        setHasChanged(true);
    }

    protected void removeItem(final JList list)
    {
        int index = list.getSelectedIndex();
        list.remove(index);
        setHasChanged(true);
        list.repaint();
    }

    protected void editItem(final JList list)
    {
        //int index = list.getSelectedIndex();
    }

    
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
        public ViewsOrderPanel(final String titleKey, final Vector<DataEntryView> views)
        {
            
            PanelBuilder    outer = new PanelBuilder(new FormLayout("f:p:g,2px,p", "p,2px,f:p:g,2px,p"), this);
            CellConstraints cc      = new CellConstraints();
            
            orderModel = new DefaultListModel();
            for (DataEntryView dev : views)
            {
                orderModel.addElement(dev);
            }
            orderList = new JList(orderModel);
            orderList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                //@Override
                public void valueChanged(ListSelectionEvent e)
                {
                    updateEnabledState();
                }
            });
            
            outer.add(new JLabel(getResourceString(titleKey)), cc.xy(1,1));
            JScrollPane sp = new JScrollPane(orderList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            outer.add(sp, cc.xy(1, 3));
            
            orderUpBtn = createIconBtn("ReorderUp", "ES_RES_MOVE_UP", new ActionListener()
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
            orderDwnBtn = createIconBtn("ReorderDown", "ES_RES_MOVE_DOWN", new ActionListener()
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
            }, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    editItem(orderList);
                }
            });
            
            PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
            upDownPanel.add(orderUpBtn,       cc.xy(1, 2));
            upDownPanel.add(orderDwnBtn,      cc.xy(1, 4));
            
            outer.add(upDownPanel.getPanel(), cc.xy(3, 3));
            outer.add(arePanel, cc.xy(1, 5));
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
