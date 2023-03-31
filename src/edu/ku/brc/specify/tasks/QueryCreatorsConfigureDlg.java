/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

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
@SuppressWarnings("serial")
public class QueryCreatorsConfigureDlg extends CustomDialog
{
    //private static final Logger log = Logger.getLogger(DataEntryConfigureDlg.class);
    
    protected QueryTask             task;
    
    protected List<String>        freqQueries       = null;
    protected List<String>        extraQueries      = null;
    protected List<String>        stdQueries      = null;
    
    protected JButton               mvToExtraBtn;
    protected JButton               mvToFreqBtn;

    
    protected QueryOrderPanel       freqPanel;
    protected QueryOrderPanel       extraPanel;
    
    protected boolean               hasChanged = false;
    
    protected Hashtable<String, String> reverseNameHash = new Hashtable<String, String>();
    protected Hashtable<String, String> nameHash        = new Hashtable<String, String>();
    

    /**
     * Creates the Dialog for configuring the queries
     * @param task the QueryTask
     * @param freqQueries the frequently used queries
     * @param extraQueries the extra queries
     * @param stdQueries the master list
     */
    public QueryCreatorsConfigureDlg(final QueryTask task,
                               final List<String> freqQueries,
                               final List<String> extraQueries,
                               final List<String> stdQueries)
    {
        super((Frame)getTopWindow(), getResourceString("QY_CONFIGURE_CREATORS"), true, OKCANCELHELP, null);
        
        this.task = task;
        
        this.freqQueries  = freqQueries;
        this.extraQueries = extraQueries;
        this.stdQueries   = stdQueries;
        
        for (String sName : stdQueries)
        {
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(sName);
            nameHash.put(sName, tableInfo.getTitle());
            reverseNameHash.put(tableInfo.getTitle(), sName);
        }
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        HelpMgr.registerComponent(helpBtn, "QBNewQueryConfig");
        
        freqPanel  = new QueryOrderPanel("QY_QUICK_LIST", freqQueries, true);
        extraPanel = new QueryOrderPanel("QY_EXTRA_LIST",     extraQueries, false);
        
        mvToExtraBtn = createIconBtn("Map", "QY_MOVE_TO_EXTRA_TT", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                moveToExtra();
            }
        });
        mvToFreqBtn = createIconBtn("Unmap", "QY_MOVE_TO_FREQ", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                moveToFreq();
            }
        });
        
        CellConstraints cc     = new CellConstraints();
        
        PanelBuilder middlePanel = new PanelBuilder(new FormLayout("c:p:g", "p, 2px, p"));
        middlePanel.add(mvToExtraBtn, cc.xy(1, 1));
        middlePanel.add(mvToFreqBtn, cc.xy(1, 3));

        PanelBuilder middlePanel2 = new PanelBuilder(new FormLayout("c:p:g", "f:p:g, p, f:p:g"));
        middlePanel2.add(middlePanel.getPanel(), cc.xy(1, 2));

        PanelBuilder mainPB = new PanelBuilder(new FormLayout("p:g,5px,p,10px,p:g", "f:p:g"));
        mainPB.add(freqPanel,  cc.xy(1,1));
        mainPB.add(middlePanel2.getPanel(),  cc.xy(3,1));
        mainPB.add(extraPanel, cc.xy(5,1));

        mainPB.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        contentPanel = mainPB.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        

        Dimension size = getSize();
        size.width  = Math.max(size.width, 500);
        size.height = Math.max(size.height, 350);
        setPreferredSize(size);
        pack();
    }
    
    /**
     * MOves query from freq list to extra
     */
    protected void moveToExtra()
    {
        int inx = freqPanel.getOrderList().getSelectedIndex();
        if (inx > -1)
        {
            String name = freqPanel.getOrderList().getSelectedValue();
            ((DefaultListModel<String> )freqPanel.getOrderList().getModel()).removeElement(name);
            ((DefaultListModel<String> )extraPanel.getOrderList().getModel()).addElement(name);
        }
    }
    
    /**
     * 
     */
    protected void moveToFreq()
    {
        int inx = extraPanel.getOrderList().getSelectedIndex();
        if (inx > -1)
        {
            String name = extraPanel.getOrderList().getSelectedValue();
            ((DefaultListModel<String>) extraPanel.getOrderList().getModel()).removeElement(name);
            ((DefaultListModel<String>) freqPanel.getOrderList().getModel()).addElement(name);
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
     * Adds new items to list
     * @param list the list
     */
    protected void addItem(final JList<String> list)
    {
        Hashtable<String, Object> hash = new Hashtable<String, Object>();
        for (String sName : freqPanel.getNames())
        {
            hash.put(sName, sName);
        }
        
        for (String sName : extraPanel.getNames())
        {
            if (sName != null) {
            	hash.put(sName, sName);
            }
        }
        
        List<String>              uniqueList    = new Vector<String>();
        Hashtable<String, String> newAvailViews = new Hashtable<String, String>();
        for (String stdName : stdQueries)
        {
            if (hash.get(stdName) == null)
            {
                hash.put(stdName, stdName);
                uniqueList.add(nameHash.get(stdName));
                newAvailViews.put(stdName, stdName);
            }
        }
        
        if (uniqueList.size() == 0)
        {
            JOptionPane.showMessageDialog(this, getResourceString("QY_NONE_QUERIES_AVAIL"), 
                    getResourceString("QY_NONE_QUERIES_AVAIL_TITLE"), JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        ToggleButtonChooserDlg<String> dlg = new ToggleButtonChooserDlg<String>(this,
                "QY_AVAIL_QUERIES_CONFIG", uniqueList);
        
        dlg.setUseScrollPane(true);
        UIHelper.centerAndShow(dlg);
        
        if (!dlg.isCancelled())
        {
            ListModel<String> model = list.getModel();
            
            for (String name : dlg.getSelectedObjects())
            {
                ((DefaultListModel<String>) model).addElement(name);
            }
        }
        setHasChanged(true);
    }

    /**
     * @return the freq quesries lis
     */
    public List<String> getFreqQueries()
    {
        return freqPanel.getNames();
    }

    /**
     * @return the extra queries list
     */
    public List<String> getExtraQueries()
    {
        return extraPanel.getNames();
    }

    /**
     * Removes an item from the list
     * @param list the list
     */
    protected void removeItem(final JList<String> list)
    {
        int index = list.getSelectedIndex();
        if (index > -1)
        {
        	((DefaultListModel<String>)list.getModel()).remove(index);
            setHasChanged(true);
            list.repaint();
        }
    }

    /**
     * @param list
     */
    protected void editItem(final JList<String> list)
    {
    }

    /**
     * Item selected in either list.
     */
    protected void itemSelected()
    {
        mvToExtraBtn.setEnabled(!freqPanel.getOrderList().isSelectionEmpty());
        mvToFreqBtn.setEnabled(!extraPanel.getOrderList().isSelectionEmpty());
    }
    
    //---------------------------------------------------------------------
    public class QueryOrderPanel extends JPanel
    {
        // Table Ordering
        protected JList<String>             orderList;
        protected DefaultListModel<String>  orderModel;
        protected JButton                   orderUpBtn;
        protected JButton                   orderDwnBtn;
        
        protected EditDeleteAddPanel        edaPanel;
        
        
        /**
         * 
         */
        public QueryOrderPanel(final String titleKey, 
                               final List<String> names,
                               final boolean orderOnLeft)
        {
            
            PanelBuilder    outer = new PanelBuilder(new FormLayout(orderOnLeft ? "p,2px,f:p:g" : "f:p:g,2px,p", "p,2px,f:p:g,2px,p"), this);
            CellConstraints cc    = new CellConstraints();
            
            orderModel = new DefaultListModel<String>();
            for (String shortClassName : names)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(shortClassName);
                if (tableInfo == null)
                {
                    throw new RuntimeException("The Query Named can't be found in table info["+shortClassName+"]");
                }
                orderModel.addElement(tableInfo.getTitle());
                
            }
            orderList = new JList<String>(orderModel);
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
                    String item = (String)orderModel.getElementAt(inx);
                    
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
                    String item = (String)orderModel.getElementAt(inx);
                   
                    orderModel.remove(inx);
                    orderModel.insertElementAt(item, inx+1);
                    orderList.setSelectedIndex(inx+1);
                    updateEnabledState();
                }
            });
            
            edaPanel = new EditDeleteAddPanel(null, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    removeItem(orderList);
                }
                }, 
                new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        addItem(orderList);
                    }
                });
            
            PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
            upDownPanel.add(orderUpBtn,       cc.xy(1, 2));
            upDownPanel.add(orderDwnBtn,      cc.xy(1, 4));
            
            int col = orderOnLeft ? 3 : 1;
            outer.add(createLabel(getResourceString(titleKey), SwingConstants.CENTER), cc.xy(col, 1));
            JScrollPane sp = UIHelper.createScrollPane(orderList);
            outer.add(sp,                     cc.xy(col, 3));
            outer.add(edaPanel,               cc.xy(col, 5));
            
            outer.add(upDownPanel.getPanel(), cc.xy(orderOnLeft ? 1 : 3, 3));
            
            outer.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            
            edaPanel.getAddBtn().setEnabled(true);
        }
        
        
        /**
         * Update the order up/down btns enabled state
         */
        protected void updateEnabledState()
        {
            int inx = orderList.getSelectedIndex();
            orderUpBtn.setEnabled(inx > 0);
            orderDwnBtn.setEnabled(inx > -1 && inx < orderModel.size()-1);
            edaPanel.setEnabled(inx > -1);
            edaPanel.getAddBtn().setEnabled(true);
        }


        /**
         * @return the orderList
         */
        public JList<String> getOrderList()
        {
            return orderList;
        }

        /**
         * @return the orderModel
         */
        public Vector<String> getNames()
        {
            Vector<String> names = new Vector<String>();
            for (int i=0;i<orderModel.size();i++)
            {
                names.add(reverseNameHash.get(orderModel.get(i)));
            }
            return names;
        }
    }
}
