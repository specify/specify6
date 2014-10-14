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
import edu.ku.brc.ui.UIHelper;

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
    protected enum GeoRankType {eEarth, eContinent, eCountry, eState, eCounty}

    private final boolean        isIncludingCounties = false;
    private final String         blankValue = "        ";
    private Vector<GeoSearchResultsItem>      isoList;
    private JTable               table;
    
    private ArrayList<JLabel>    labels       = new ArrayList<JLabel>();
    private ArrayList<JLabel>    codes        = new ArrayList<JLabel>();
    
    private JButton              backBtn;
    private JButton              nextBtn;
    
    private GeoRankType          currentLevel;
    
    /**
     * @param dialog
     * @param whichBtns
     * @throws HeadlessException
     */
    public ISOCodeListDlg(final Dialog dialog, final int whichBtns) throws HeadlessException
    {
        super(dialog, "ISO Codes", true, OKCANCELHELP, null); // I18N
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        setHelpContext("GeoCleanUpFindISOCode");
        setCancelLabel(getResourceString("CLOSE"));            
        setOkLabel(getResourceString("CLNUP_GEO_CHOOSE_ISO")); 
        
        super.createUI();
        
        CellConstraints cc = new CellConstraints();

        ActionListener al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                clearLevels();
            }
        };
        backBtn = UIHelper.createI18NButton("Back");
        backBtn.addActionListener(al);
        backBtn.setEnabled(false);

        al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              int selectedIndex = table.getSelectedRow();
              if (selectedIndex > -1)
              {
                  currentLevel = GeoRankType.values()[currentLevel.ordinal() + 1];
                  
                  GeoSearchResultsItem item = isoList.get(selectedIndex);
                  fillTableFromItem(item);
              }
            }
        };
        nextBtn = UIHelper.createI18NButton("Next");
        nextBtn.addActionListener(al);
        nextBtn.setEnabled(false);
        
        JLabel hdrTitle1 = createI18NLabel("Geography", JLabel.CENTER); // I18N
        JLabel hdrTitle2 = createI18NLabel("ISO Code", JLabel.CENTER);
        Font font = hdrTitle1.getFont().deriveFont(Font.BOLD);
        hdrTitle1.setFont(font);
        hdrTitle2.setFont(font);
        
        PanelBuilder pbc = new PanelBuilder(new FormLayout("f:p:g,12px,p,12px,p", "p,4px,p,4px,p,4px,p,4px,p"));
        pbc.add(hdrTitle1, cc.xy(1, 1));
        pbc.add(hdrTitle2, cc.xy(3, 1));
        
        int numRows = isIncludingCounties ? 3 : 2;
        int index   = 3;
        
        for (int i=0;i<numRows;i++)
        {
            JLabel lbl = new JLabel(blankValue);
            labels.add(lbl);
            
            JLabel cdLbl = new JLabel(blankValue);
            codes.add(cdLbl);
            cdLbl.setHorizontalAlignment(SwingConstants.CENTER);
            
            pbc.add(lbl,   cc.xy(1, index));
            pbc.add(cdLbl, cc.xy(3, index));
            
            lbl.setBackground(new Color(250, 250, 250));
            cdLbl.setBackground(lbl.getBackground());
            lbl.setOpaque(true);
            cdLbl.setOpaque(true);
            index += 2;
        }
        
        PanelBuilder pbck = new PanelBuilder(new FormLayout("p", "f:p:g,p,10px,p,f:p:g"));
        pbck.add(backBtn,   cc.xy(1, 2));
        pbck.add(nextBtn,   cc.xy(1, 4));

        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,4px,p", "p,8px,p,2px,f:p:g,4px,p"));
        
        isoList = new Vector<GeoSearchResultsItem>();
        table   = new JTable();
        makeTableHeadersCentered(table, false);
        
        TableCellRenderer tcr = table.getDefaultRenderer(String.class);
        DefaultTableCellRenderer dtcr = (DefaultTableCellRenderer) tcr;
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);

        PanelBuilder pb2 = new PanelBuilder(new FormLayout("c:p:g", "p"));
        pb2.add(pbc.getPanel(), cc.xy(1,1));

        int y = 1;
        pb.add(pb2.getPanel(), cc.xy(1,1)); y += 2;
        pb.add(new JLabel("Click on an item in the list:"), cc.xy(1,y)); y += 2;   // I18N
        pb.add(createScrollPane(table), cc.xy(1,y)); y += 2;
        
        pb.add(pbck.getPanel(), cc.rchw(1, 3, 7, 1));

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
                    getOkBtn().doClick();
                }
            }
        });
        
        currentLevel = GeoRankType.eEarth;
        fillTableFromItem(null); // eEarth doesn't need an item
    }

    /**
     * @return the geonameId
     */
    public GeoSearchResultsItem getSelectedItem()
    {
        return table.getSelectedRow() > -1 ? isoList.get(table.getSelectedRow()) : null;
    }

    /**
     *
     */
    private void fillTableFromItem(final GeoSearchResultsItem item)
    {
        switch (currentLevel)
        {
            case eEarth:
                fillContinent();
                break;
                
            case eContinent:
                fillContriesWithContinent(item);
                break;
                
            case eCountry:
                fillStatesWithCountry(item);
                break;
                
            case eState:
                if (isIncludingCounties)
                {
                    fillCountiesWithState(item);
                }
                break;
                
            case eCounty:
                break;
        }
    }
    
    /**
     *
     */
    private void tableRowChoosen()
    {
        nextBtn.setEnabled(table.getSelectedRow() > -1 && currentLevel.ordinal() < GeoRankType.eCountry.ordinal());
    }
    
    private void clearLevels()
    {
        currentLevel = GeoRankType.values()[currentLevel.ordinal() - 1];
        
        int    index   = currentLevel.ordinal();
        String newCode = currentLevel == GeoRankType.eEarth ? null : codes.get(index-1).getText();
        
        for (int i=labels.size()-1;i >= index;i--)
        {
            labels.get(i).setText(blankValue);
            codes.get(i).setText(blankValue);
        }
        
        fillTableFromItem(new GeoSearchResultsItem(null, null, newCode));
        
        backBtn.setEnabled(index > 0);
    }
    
    /**
     *
     */
    private void fillTable(final String whereStr, final int codeLen)
    {
        String extra = codeLen > -1 ? String.format("AND LENGTH(ISOCode) = %d", codeLen) : "";
        String sql   = String.format("SELECT DISTINCT asciiname, ISOCode,geonameId FROM geoname WHERE %s %s ORDER BY asciiname", whereStr, extra);
        isoList.clear();
        //System.out.println(sql);
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        for (Object[] row : rows)
        {
            String cont = row[0].toString();
            if (!cont.endsWith("Ocean") || (!cont.startsWith("North") && !cont.startsWith("South")))
            {
                isoList.add(new GeoSearchResultsItem(row[0].toString(), (Integer)row[2], row[1].toString()));
            }
        }
        table.setModel(new ISOTableModel());
    }
    
    /**
     *
     */
    private void fillContinent()
    {
        fillTable("fcode = 'CONT' OR fcode = 'OCN'", 2);
    }

    
    /**
     * @param item
     */
    private void fillContriesWithContinent(final GeoSearchResultsItem item)
    {
        if (item.name != null) 
        {
            labels.get(0).setText(item.name);
        }
        codes.get(0).setText(item.isoCode);
        backBtn.setEnabled(true);
        String sql = String.format("SELECT name,iso_alpha2,geonameId from countryinfo WHERE continent = '%s' ORDER BY name;", item.isoCode);
        isoList.clear();
        //System.out.println(sql);
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        for (Object[] row : rows)
        {
            isoList.add(new GeoSearchResultsItem(row[0].toString(), (Integer)row[2], row[1].toString()));
        }
        table.setModel(new ISOTableModel());
    }
    
    /**
     *
     */
    private void fillStatesWithCountry(final GeoSearchResultsItem item)
    {
        // SELECT asciiname, latitude, longitude, country, admin1 as StateCode FROM geoname WHERE fcode = 'ADM1' AND country = 'US' ORDER BY name
        if (item.name != null) 
        {
            labels.get(1).setText(item.name);
        }
        codes.get(1).setText(item.isoCode);
        backBtn.setEnabled(true);
        String sql = String.format("fcode = 'ADM1' AND country = '%s'", item.isoCode);
        fillTable(sql, 4);
    }
    
    /**
     *
     */
    private void fillCountiesWithState(final GeoSearchResultsItem item)
    {
        //SELECT asciiname AS CountyName, latitude, longitude, country, admin1 as StateCode FROM geoname WHERE admin1 = 'IA' AND fcode = 'ADM2' ORDER BY name
        if (item.name != null) 
        {
            labels.get(2).setText(item.name);
        }
        codes.get(2).setText(item.isoCode);
        backBtn.setEnabled(true);
        String sql = String.format("fcode = 'ADM2' AND admin1 = '%s' AND country = '%s'", item.isoCode.substring(2), codes.get(1).getText());
        fillTable(sql, -1);
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
                        return "Counties";
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
            GeoSearchResultsItem item = isoList.get(row);
            return column == 0 ? item.name : item.isoCode;
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
