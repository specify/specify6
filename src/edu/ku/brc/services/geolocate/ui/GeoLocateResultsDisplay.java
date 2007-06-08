/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.services.geolocate.ui;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.services.geolocate.client.GeoLocate;
import edu.ku.brc.services.geolocate.client.GeorefResult;
import edu.ku.brc.services.geolocate.client.GeorefResultSet;
import edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener;
import edu.ku.brc.ui.UIHelper;

/**
 * A UI panel for use in displaying the results of a GEOLocate web service query.
 * 
 * @author jstewart
 * @code_status Alpha
 */
public class GeoLocateResultsDisplay extends JPanel implements MapperListener
{
    protected static final int MAP_WIDTH  = 400;
    protected static final int MAP_HEIGHT = 250;

    protected ResultsTableModel tableModel;
    protected JTable resultsTable;
    
    protected JLabel mapLabel;
    
    protected JTextField localityStringField;
    protected JTextField countyField;
    protected JTextField stateField;
    protected JTextField countryField;
    
    public GeoLocateResultsDisplay()
    {
        super();
        
        setLayout(new FormLayout("p,10px,400px,10px,C:p:g", "p,2px,p,2px,p,2px,p,10px,p:g"));
        
        CellConstraints cc = new CellConstraints();
        
        // add the query fields to the display
        int rowIndex = 1;
        localityStringField = addRow(cc, getResourceString("LocalityString"),      1, rowIndex);
        rowIndex+=2;
        countyField         = addRow(cc, getResourceString("County"), 1, rowIndex);
        rowIndex+=2;
        stateField          = addRow(cc, getResourceString("State"),    1, rowIndex);
        rowIndex+=2;
        countryField        = addRow(cc, getResourceString("Country"),    1, rowIndex);
        rowIndex+=2;

        // add the JLabel to show the map
        mapLabel = new JLabel();
        mapLabel.setText(getResourceString("LOADING_MAP"));
        mapLabel.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        add(mapLabel, cc.xywh(5,1,1,9));

        // add the results table
        tableModel = new ResultsTableModel();
        resultsTable = new JTable(tableModel);
        resultsTable.setShowVerticalLines(false);
        resultsTable.setShowHorizontalLines(false);
        resultsTable.setRowSelectionAllowed(true);

        // add a cell renderer that uses the tooltip to show the text of the "parse pattern" column in case
        // it is too long to show and gets truncated by the standard cell renderer
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof String)
                {
                    ((JLabel)c).setToolTipText((String)value);
                }
                return c;
            }
        };
        resultsTable.getColumnModel().getColumn(3).setCellRenderer(cellRenderer);
        
        JScrollPane scrollPane = new JScrollPane(resultsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, cc.xywh(1,rowIndex, 3, 1));
        rowIndex+=2;
    }
    
    public void setGeoLocateQueryAndResults(String localityString, String county, String state, String country, GeorefResultSet georefResults)
    {
        localityStringField.setText(localityString);
        localityStringField.setCaretPosition(0);
        countyField.setText(county);
        countyField.setCaretPosition(0);
        stateField.setText(state);
        stateField.setCaretPosition(0);
        countryField.setText(country);
        countryField.setCaretPosition(0);
        
        tableModel.setResultSet(georefResults.getResultSet());
        
        mapLabel.setText(getResourceString("LOADING_MAP"));
        GeoLocate.getMapOfGeographicPoints(georefResults.getResultSet(), GeoLocateResultsDisplay.this);

        // set the table height to at most 10 rows
        Dimension size = resultsTable.getPreferredScrollableViewportSize();
        size.height = Math.min(size.height, resultsTable.getRowHeight()*10);
        resultsTable.setPreferredScrollableViewportSize(size);
        UIHelper.calcColumnWidths(resultsTable);
    }
    
    /**
     * Returns the selected result.
     * 
     * @return the selected result
     */
    public GeorefResult getSelectedResult()
    {
        int rowIndex = resultsTable.getSelectedRow();
        if (rowIndex < 0 || rowIndex > tableModel.getRowCount())
        {
            return null;
        }
        
        return tableModel.getResult(rowIndex);
    }
    
    /**
     * Selects the result with the given index in the results list.
     * 
     * @param index the index of the result to select
     */
    public void setSelectedResult(int index)
    {
        if (index < 0 || index > resultsTable.getRowCount()-1)
        {
            resultsTable.clearSelection();
        }
        else
        {
            resultsTable.setRowSelectionInterval(index, index);
            int colCount = resultsTable.getColumnCount();
            resultsTable.setColumnSelectionInterval(0, colCount-1);
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener#exceptionOccurred(java.lang.Exception)
     */
    public void exceptionOccurred(Exception e)
    {
        mapLabel.setText("Error while grabbing map"); // XXX i18n
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener#mapReceived(javax.swing.Icon)
     */
    public void mapReceived(Icon map)
    {
        mapLabel.setText(null);
        mapLabel.setIcon(map);
        repaint();
    }
    
    /**
     * Adds a new row to this object's content area.
     * 
     * @param cc the cell constraints of the new row
     * @param labelStr the text label for the new row
     * @param column the starting column number for the new row's UI
     * @param row the row number of the new row
     * @return the {@link JTextField} added to the new row
     */
    protected JTextField addRow(final CellConstraints cc,
                                final String labelStr,
                                final int column,
                                final int row)
    {
        add(new JLabel(labelStr+":", SwingConstants.RIGHT), cc.xy(column,row));
        JTextField tf = createTextField();
        tf.setEditable(false);
        add(tf, cc.xy(column+2,row));
        return tf;
    }

    /**
     * Creates a {@link JTextField} customized for use in this UI widget.
     * 
     * @return a {@link JTextField}
     */
    protected JTextField createTextField()
    {
        JTextField tf     = new JTextField();
        Insets     insets = tf.getBorder().getBorderInsets(tf);
        tf.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.bottom));
        tf.setForeground(Color.BLACK);
        tf.setBackground(Color.WHITE);
        tf.setEditable(false);
        return tf;
    }
    
    protected class ResultsTableModel extends AbstractTableModel
    {
        protected List<GeorefResult> results;
        
        public void setResultSet(List<GeorefResult> results)
        {
            this.results = results;
            fireTableDataChanged();
        }
        
        public GeorefResult getResult(int index)
        {
            return results.get(index);
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            switch (columnIndex)
            {
                case 0:
                {
                    return Integer.class;
                }
                case 1:
                case 2:
                {
                    return Double.class;
                }
                case 3:
                {
                    return String.class;
                }
            }
            return null;
        }

        @Override
        public String getColumnName(int column)
        {
            switch (column)
            {
                case 0:
                {
                    return getResourceString("Number");
                }
                case 1:
                {
                    return getResourceString("Latitude");
                }
                case 2:
                {
                    return getResourceString("Longitude");
                }
                case 3:
                {
                    return getResourceString("ParsePattern");
                }
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }

        public int getColumnCount()
        {
            return 4;
        }

        public int getRowCount()
        {
            return (results == null) ? 0 : results.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex)
        {
            GeorefResult res = results.get(rowIndex);
            switch (columnIndex)
            {
                case 0:
                {
                    return rowIndex+1;
                }
                case 1:
                {
                    return res.getWGS84Coordinate().getLatitude();
                }
                case 2:
                {
                    return res.getWGS84Coordinate().getLongitude();
                }
                case 3:
                {
                    return res.getParsePattern();
                }
            }
            return null;
        }
        
    }
    
    public static void main(String[] args)
    {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        GeorefResultSet resSet = GeoLocate.getGeoLocateResults("US", "LA", "Tangipahoa Parish", "Tangipahoa R. approx. 1 mi. E of Independence, LA on bridge of Highway 40");
        
        GeoLocateResultsDisplay geoLocateResultsDisplay = new GeoLocateResultsDisplay();
        geoLocateResultsDisplay.setGeoLocateQueryAndResults("Tangipahoa R. approx. 1 mi. E of Independence, LA on bridge of Highway 40", "Tangipahoa Parish", "LA", "US", resSet);
        f.add(geoLocateResultsDisplay);
        
        f.pack();
        
        f.setVisible(true);
    }
}
