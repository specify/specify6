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

import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.makeTableHeadersCentered;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
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

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.CustomDialog;

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

    private Vector<ISOItem>      isoList;
    private JTable               table;
    private ISOTableModel        model;
    
    protected ArrayList<JLabel>  labels   = new ArrayList<JLabel>();
    protected ArrayList<JLabel>  codes    = new ArrayList<JLabel>();
    protected ArrayList<JButton> backBtns = new ArrayList<JButton>();
    
    //protected HashMap<GeoRankType, ISOItem> levelMap = new HashMap<GeoRankType, ISOItem>();
    
    protected GeoRankType currentLevel    = GeoRankType.eContinent;
    protected JButton     selectRowBtn;
    
    /**
     * @param dialog
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public ISOCodeListDlg(final Dialog dialog, final int whichBtns) throws HeadlessException
    {
        super(dialog, "ISO Codes", true, whichBtns, null); // I18N
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        if (CustomDialog.OK_BTN == this.whichBtns)
        {
            setCancelLabel(getResourceString("CLOSE"));            
        }
        super.createUI();
        
        CellConstraints cc = new CellConstraints();

        ActionListener al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                clearLevels((JButton)e.getSource());
            }
        };
        
        selectRowBtn = new JButton("Select");
        selectRowBtn.addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                tableRowChoosen();
            }
        });
        
        JLabel hdrTitle1 = createI18NLabel("Geography", JLabel.CENTER);
        JLabel hdrTitle2 = createI18NLabel("ISO Code", JLabel.CENTER);
        Font font = hdrTitle1.getFont().deriveFont(Font.BOLD);
        hdrTitle1.setFont(font);
        hdrTitle2.setFont(font);
        
        PanelBuilder pbc = new PanelBuilder(new FormLayout("f:p:g,12px,p,12px,p", "p,4px,p,4px,p,4px,p,4px,p"));
        pbc.add(hdrTitle1,   cc.xy(1, 1));
        pbc.add(hdrTitle2, cc.xy(3, 1));
        //pbc.add(" ",   cc.xy(5, 1));
        int index = 3;
        for (int i=0;i<3;i++)
        {
            JLabel lbl = new JLabel();
            labels.add(lbl);
            
            JLabel cdLbl = new JLabel();
            codes.add(cdLbl);
            cdLbl.setHorizontalAlignment(SwingConstants.CENTER);
            
            JButton btn = new JButton("^");
            backBtns.add(btn);
            btn.addActionListener(al);
            pbc.add(lbl,   cc.xy(1, index));
            pbc.add(cdLbl, cc.xy(3, index));
            pbc.add(btn,   cc.xy(5, index));
            
            lbl.setBackground(new Color(250, 250, 250));
            cdLbl.setBackground(lbl.getBackground());
            lbl.setOpaque(true);
            cdLbl.setOpaque(true);
            btn.setEnabled(false);
            index += 2;
        }

        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "p,8px,f:p:g,4px,p"));
        
        isoList = new Vector<ISOItem>();
        //model   = new ISOTableModel();
        table   = new JTable();
        makeTableHeadersCentered(table, false);
        
        TableCellRenderer tcr = table.getDefaultRenderer(String.class);
        DefaultTableCellRenderer dtcr = (DefaultTableCellRenderer) tcr;
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);

        PanelBuilder pb2 = new PanelBuilder(new FormLayout("c:p:g", "p"));
        pb2.add(pbc.getPanel(), cc.xy(1,1));
        //pb2.getPanel().setBackground(Color.BLUE);
        //pb2.setOpaque(true);

        int y = 1;
        pb.add(pb2.getPanel(), cc.xy(1,1)); y += 2;
        //pb.add(new JLabel("ISO Codes"), cc.xy(1,y)); y += 2;   // I18N
        pb.add(createScrollPane(table), cc.xy(1,y)); y += 2;

        pb.setDefaultDialogBorder();
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        pack();
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    tableRowChoosen();
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
                    tableRowChoosen();
                }
            }
        });
        
        fillContinent();
    }
    
    /**
     *
     */
    private void fillTable(final ISOItem item)
    {
        switch (currentLevel)
        {
            case eContinent:
                fillContriesWithContinent(item);
                break;
                
            case eCountry:
                fillStatesWithCountry(item);
                break;
                
            case eState:
                fillCountiesWithState(item);
                break;
        }
    }
    
    /**
     *
     */
    private void tableRowChoosen()
    {
        int selectedIndex = table.getSelectedRow();
        if (selectedIndex > -1)
        {
            ISOItem item = isoList.get(selectedIndex);
            fillTable(item);
        }
    }
    
    private void clearLevels(final JButton btn)
    {
        int index = backBtns.indexOf(btn);
        for (int i=backBtns.size()-1;i >= index;i--)
        {
            labels.get(i).setText("");
            codes.get(i).setText("");
            backBtns.get(i).setEnabled(false);
        }
        
        switch (index)
        {
            case 0:
                fillContinent();
                return;
                
            case 1:
                currentLevel = GeoRankType.eContinent;
                break;

            case 2:
                currentLevel = GeoRankType.eCountry;
                break;

            case 3:
                currentLevel = GeoRankType.eState;
                break;
        }
        fillTable(new ISOItem(null, codes.get(index-1).getText()));
    }
    
    /**
     *
     */
    private void fillTable(final String whereStr, final int codeLen)
    {
        String extra = codeLen > -1 ? String.format("AND LENGTH(ISOCode) = %d", codeLen) : "";
        String sql   = String.format("SELECT DISTINCT asciiname, ISOCode FROM geoname WHERE %s %s ORDER BY asciiname", whereStr, extra);
        isoList.clear();
        System.out.println(sql);
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        for (Object[] row : rows)
        {
            String cont = row[0].toString();
            if (!cont.endsWith("Ocean") || (!cont.startsWith("North") && !cont.startsWith("South")))
            {
                isoList.add(new ISOItem(row[0].toString(), row[1].toString()));
            }
        }
        //model.fireTableDataChanged();
        //model.fireTableStructureChanged();
       table.setModel(model = new ISOTableModel());
    }
    
    /**
     *
     */
    private void fillContinent()
    {
        currentLevel = GeoRankType.eContinent;
        fillTable("fcode = 'CONT' OR fcode = 'OCN'", 2);
    }
    
    /**
     *
     */
    private void fillContriesWithContinent(final ISOItem item)
    {
        if (item.title != null) 
        {
            labels.get(0).setText(item.title);
        }
        codes.get(0).setText(item.code);
        backBtns.get(0).setEnabled(true);
        String sql = String.format("select name,iso_alpha2 from countryinfo WHERE continent = '%s' ORDER BY name;", item.code);
        isoList.clear();
        System.out.println(sql);
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        for (Object[] row : rows)
        {
            isoList.add(new ISOItem(row[0].toString(), row[1].toString()));
        }
        currentLevel = GeoRankType.eCountry;
        table.setModel(model = new ISOTableModel());
    }
    
    /**
     *
     */
    private void fillStatesWithCountry(final ISOItem item)
    {
        // SELECT asciiname, latitude, longitude, country, admin1 as StateCode FROM geoname WHERE fcode = 'ADM1' AND country = 'US' ORDER BY name
        if (item.title != null) 
        {
            labels.get(1).setText(item.title);
        }
        codes.get(1).setText(item.code);
        backBtns.get(1).setEnabled(true);
        String sql = String.format("fcode = 'ADM1' AND country = '%s'", item.code);
        currentLevel = GeoRankType.eState;
        fillTable(sql, 4);
    }
    
    /**
     *
     */
    private void fillCountiesWithState(final ISOItem item)
    {
        //SELECT asciiname AS CountyName, latitude, longitude, country, admin1 as StateCode FROM geoname WHERE admin1 = 'IA' AND fcode = 'ADM2' ORDER BY name
        if (item.title != null) 
        {
            labels.get(2).setText(item.title);
        }
        codes.get(2).setText(item.code);
        backBtns.get(2).setEnabled(true);
        String sql = String.format("fcode = 'ADM2' AND admin1 = '%s' AND country = '%s'", item.code.substring(2), codes.get(1).getText());
        currentLevel = GeoRankType.eCounty;
        fillTable(sql, -1);
    }
    
    
    //-----------------------------------------------------------
    //--
    //-----------------------------------------------------------
    private class ISOItem
    {
        public String title;
        public String code;
        
        public ISOItem(final String title, final String code)
        {
            this.title = title;
            this.code = code;
        }
    }
    
    //-----------------------------------------------------------
    //--
    //-----------------------------------------------------------
    private class ISOTableModel extends DefaultTableModel
    {

        @Override
        public String getColumnName(int column)
        {
            if (column == 0)
            {
                switch (currentLevel)
                {
                    case eContinent:
                        return "Continents";
                        
                    case eCountry:
                        return "Countries";
                        
                    case eState:
                        return "States";
                        
                    case eCounty:
                        return "Couties";
                }
            }
            return "ISO Code"; // I18N
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
            ISOItem item = isoList.get(row);
            return column == 0 ? item.title : item.code;
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
