/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import static edu.ku.brc.ui.UIHelper.createI18NButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jun 29, 2010
 *
 */
public class AgentCleanupListDlg extends CustomDialog
{
    protected enum ItemStatusType {eOK, eProcessed, eRelated}
    
    protected String[]             headers     = new String[] {"Include", "Name", "Count"};
    protected JTable               table;
    protected FindItemTableModel   model;
    protected Vector<FindItemInfo> itemsList;
    
    protected JButton              selectAllBtn;
    protected JButton              deselectAllBtn;
    
    protected DBTableInfo          tblInfo     = null;
    protected String               title       = null;
    protected String               topMsg      = null;
    
    /**
     * @param frame
     * @param title
     * @throws HeadlessException
     */
    public AgentCleanupListDlg(final String title, 
                               final String topMsg,
                               final Vector<FindItemInfo> itemsList) throws HeadlessException
    {
        super((Frame)getTopWindow(), getResourceString("CLNUP.FNDTITLE"), true, OKCANCEL, null);
        this.title  = title;
        this.topMsg = topMsg;
        this.itemsList = itemsList;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        setCancelLabel(getResourceString("CLOSE"));
        setOkLabel(getResourceString("CLNUP_AGT_CHOOSE"));
        
        super.createUI();
        
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder btnPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g,p,f:p:g", "p")); //$NON-NLS-1$ //$NON-NLS-2$
        selectAllBtn   = createI18NButton("SELECTALL"); //$NON-NLS-1$
        deselectAllBtn = createI18NButton("DESELECTALL"); //$NON-NLS-1$
        btnPB.add(selectAllBtn,   cc.xy(2, 1));
        btnPB.add(deselectAllBtn, cc.xy(4, 1));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", (topMsg != null ? "p,2px," : "") + "p,2px,p,2px,p"));
        
        model = new FindItemTableModel();
        table  = new JTable(model);
        UIHelper.makeTableHeadersCentered(table, false);
        
        TableCellRenderer tcr = table.getDefaultRenderer(Integer.class);
        DefaultTableCellRenderer dtcr = (DefaultTableCellRenderer) tcr;
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        
        int y = 1;
        if (topMsg != null)
        {
            pb.add(createLabel(topMsg, SwingConstants.CENTER), cc.xyw(1,y,3)); y += 2;
        }
        pb.add(createScrollPane(table), cc.xyw(1,y,3)); y += 2;
        pb.add(btnPB.getPanel(),        cc.xyw(1,y,3));

        pb.setDefaultDialogBorder();
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        pack();
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    //updateBtnUI();
                }
            }
        });
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                
                if (e.getClickCount() == 2)
                {
                    //getOkBtn().setEnabled(true);
                    //getOkBtn().doClick();
                }
            }
        });
        
        selectAllBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doSetSelection(true);
                getOkBtn().setEnabled(true);
            }
        });
        deselectAllBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doSetSelection(false);
                getOkBtn().setEnabled(false);
            }
        });
    }
    
    /**
     * @param selected
     */
    private void doSetSelection(final boolean selected)
    {
        for (FindItemInfo fii : itemsList)
        {
            fii.setIncluded(selected);
        }
        model.fireTableDataChanged();
    }
    
    /**
     * 
     */
    private void updateBtnUI()
    {
        //boolean enable = table.getSelectedRowCount() > 0;
        //selectAllBtn.setEnabled(table.getSelectedRowCount() != table.getRowCount());
        //deselectAllBtn.setEnabled(enable);
        boolean enabled = false;
        for (FindItemInfo fii : itemsList)
        {
            if (fii.isIncluded())
            {
                enabled = true;
                break;
            }
        }
        getOkBtn().setEnabled(enabled);
    }
    
    /**
     * @return
     */
    public int[] getSelectedIndexes()
    {
        ArrayList<Integer> selectedIndexes = new ArrayList<Integer>(500);
        int inx = 0;
        for (FindItemInfo fii : itemsList)
        {
            if (fii.isIncluded())
            {
                selectedIndexes.add(inx);
            }
            inx++;
        }
        
        if (selectedIndexes.size() == 0)
        {
            return new int[0];
        }
        
        int[] indexes = new int[selectedIndexes.size()];
        inx = 0;
        for (Integer index : selectedIndexes)
        {
            indexes[inx++] = index;
        }        
        return indexes;
    }
    
    //----------------------------------------------------------
    class FindItemTableModel extends DefaultTableModel
    {

        @Override
        public String getColumnName(int column)
        {
            return headers[column];
        }

        @Override
        public int getColumnCount()
        {
            return headers.length;
        }

        @Override
        public int getRowCount()
        {
            return itemsList.size();
        }

        @Override
        public void setValueAt(final Object aValue, final int row, final int column)
        {
            if (column == 0)
            {
                FindItemInfo fii = itemsList.get(row);
                fii.setIncluded((Boolean)aValue);
                updateBtnUI();
            }
        }

        @Override
        public Object getValueAt(int row, int column)
        {
            FindItemInfo fii = itemsList.get(row);
            if (column == 0) return fii.isIncluded();
            
            return column == 1 ? fii.getValue() : fii.getCount() + 1;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex == 0) return Boolean.class;
            
            return columnIndex == 1 ? String.class : Integer.class;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return column == 0;
        }
        
    }
}
