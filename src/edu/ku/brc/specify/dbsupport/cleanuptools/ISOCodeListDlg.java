/* Copyright (C) 2012, University of Kansas Center for Research
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

import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.HeadlessException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 18, 2014
 *
 */
public class ISOCodeListDlg extends CustomDialog
{
    protected enum GeoRankType {eContinent, eCountry, eState, eCounty}

    private JList<String> geoList;
    private JTable        table;
    private Vector<Pair<String, String>> isoList;
    private TableModel    model;
    
    protected JComboBox<String> continentCBX;
    protected JComboBox<String> countryCBX;
    protected JComboBox<String> stateCBX;
    
    /**
     * @param dialog
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public ISOCodeListDlg(final Dialog dialog) throws HeadlessException
    {
        super(dialog, "ISO Codes", true, OKCANCEL, null);
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
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,4px,p,8px,f:p:g"));
        
        isoList = new Vector<Pair<String, String>>();
        model   = new ISOTableModel();
        table   = new JTable(model);
        UIHelper.makeTableHeadersCentered(table, false);
        
        TableCellRenderer tcr = table.getDefaultRenderer(Integer.class);
        DefaultTableCellRenderer dtcr = (DefaultTableCellRenderer) tcr;
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        
        continentCBX = new JComboBox<String>();
        countryCBX   = new JComboBox<String>();
        stateCBX     = new JComboBox<String>();
        
        countryCBX.setEnabled(false);
        stateCBX.setEnabled(false);
        
        int y = 1;
        pb.add(new JLabel("ISO Codes"), cc.xy(1,y)); y += 2;
        pb.add(createScrollPane(table), cc.xyw(1,y,3)); y += 2;

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
        
        continentCBX.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
            }
        });

        countryCBX.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
            }
        });
        
        stateCBX.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
            }
        });
    }
    
    private void fillContinent()
    {
        Vector<String> dataList = new Vector<String>();
        isoList.clear();
        String sql = "";
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        for (Object[] row : rows)
        {
            isoList.add(new Pair<String, String>(row[0].toString(), row[1].toString()));
            dataList.add(row[0].toString());
        }
        continentCBX.setModel(new DefaultComboBoxModel<String>(dataList));
    }
    
    private void fillTableWithCountries(final String isoCode)
    {
        
    }
    
    private void fillTableWithStates(final String isoCode)
    {
        
    }
    
    private class ISOTableModel extends DefaultTableModel
    {

        @Override
        public String getColumnName(int column)
        {
            return column == 0 ? "Country" : "ISO Code";
        }

        @Override
        public int getColumnCount()
        {
            return 2;
        }

        @Override
        public int getRowCount()
        {
            return isoList.size();
        }

        @Override
        public void setValueAt(final Object aValue, final int row, final int column)
        {
        }

        @Override
        public Object getValueAt(int row, int column)
        {
            Pair<String, String> pair = isoList.get(row);
            return column == 0 ? pair.first : pair.second;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }
        
    }
}
