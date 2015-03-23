/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.services.geolocate.ui;

import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.services.geolocate.client.GeoLocate;
import edu.ku.brc.services.geolocate.client.GeographicPoint;
import edu.ku.brc.services.geolocate.client.GeorefResult;
import edu.ku.brc.services.geolocate.client.GeorefResultSet;
import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.services.mapping.LatLonPoint;
import edu.ku.brc.services.mapping.LocalityMapper.MapperListener;
import edu.ku.brc.specify.ui.ClickAndGoSelectListener;
import edu.ku.brc.specify.ui.WorldWindPanel;
import edu.ku.brc.ui.BiColorTableCellRenderer;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.markers.BasicMarker;
// ZZZ import gov.nasa.worldwind.view.OrbitView;

/**
 * A UI panel for use in displaying the results of a GEOLocate web service query.
 * 
 * @author jstewart
 * @author rods
 * 
 * @code_status Alpha
 */
/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 12, 2009
 *
 */
public class GeoLocateResultsDisplay extends JPanel implements MapperListener, SelectListener
{
    protected static final int MAP_WIDTH  = 500;
    protected static final int MAP_HEIGHT = 500;

    protected ResultsTableModel tableModel;
    protected JTable            resultsTable;
    
    protected JLabel            mapLabel;
    
    protected JTextField        localityStringField;
    protected JTextField        countyField;
    protected JTextField        stateField;
    protected JTextField        countryField;
    
    protected JButton           acceptBtn     = null;
    
    protected WorldWindPanel    wwPanel       = null;
    protected GeorefResult      userDefGeoRef = null;
    protected Position          lastClickPos  = null;
    
    /**
     * Constructor.
     */
    public GeoLocateResultsDisplay()
    {
        super();
        
        setLayout(new FormLayout("p,10px,400px,10px,f:p:g", "p,2px,p,2px,p,2px,p,10px,f:p:g")); //$NON-NLS-1$ //$NON-NLS-2$
        
        CellConstraints cc = new CellConstraints();
        
        // add the query fields to the display
        int rowIndex = 1;
        localityStringField = addRow(cc, getResourceString("GeoLocateResultsDisplay.LOCALITY_DESC"),      1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        countyField         = addRow(cc, getResourceString("GeoLocateResultsDisplay.COUNTY"), 1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        stateField          = addRow(cc, getResourceString("GeoLocateResultsDisplay.STATE"),    1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        countryField        = addRow(cc, getResourceString("GeoLocateResultsDisplay.COUNTRY"),    1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;

        // add the JLabel to show the map
        mapLabel = createLabel(getResourceString("GeoLocateResultsDisplay.LOADING_MAP")); //$NON-NLS-1$
        mapLabel.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        
        boolean useWorldWind = AppPreferences.getLocalPrefs().getBoolean("USE.WORLDWIND", false);
        if (!useWorldWind)
        {
            add(mapLabel, cc.xywh(5,1,1,9));
            
        } else
        {
            wwPanel = new WorldWindPanel();
            wwPanel.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
            wwPanel.getWorld().addSelectListener(new ClickAndGoSelectListener(wwPanel.getWorld(), MarkerLayer.class));
            wwPanel.getWorld().addSelectListener(this);
            
            wwPanel.getWorld().getInputHandler().addMouseListener(new MouseAdapter()
            {
                /* (non-Javadoc)
                 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
                 */
                @Override
                public void mouseClicked(final MouseEvent e)
                {
                    super.mouseClicked(e);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            Position pos = wwPanel.getWorld().getCurrentPosition();
                            if (!pos.equals(lastClickPos))
                            {
                                if (userDefGeoRef == null)
                                {
                                    addUserDefinedMarker();
                                } else 
                                {
                                    repositionUserDefMarker();
                                }
                            }
                        }
                    });
                }
            });
            
            add(wwPanel, cc.xywh(5,1,1,9));
        }

        // add the results table
        tableModel   = new ResultsTableModel();
        resultsTable = new JTable(tableModel);
        resultsTable.setShowVerticalLines(false);
        resultsTable.setShowHorizontalLines(false);
        resultsTable.setRowSelectionAllowed(true);
        resultsTable.setDefaultRenderer(String.class, new BiColorTableCellRenderer(false));
        
        resultsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    if (acceptBtn != null)
                    {
                        //System.out.println(resultsTable.getSelectedRowCount());
                        acceptBtn.setEnabled(resultsTable.getSelectedRowCount() > 0);
                    }
                }
            }
        });
        
        if (wwPanel != null)
        {
            resultsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    wwPanel.flyToMarker(resultsTable.getSelectedRow());
                }
            });
        }

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
    
    /**
     * @param acceptBtn the acceptBtn to set
     */
    public void setAcceptBtn(JButton acceptBtn)
    {
        this.acceptBtn = acceptBtn;
    }

    /**
     * 
     */
    private void addUserDefinedMarker()
    {
        Position        pos = wwPanel.getWorld().getCurrentPosition();
        GeographicPoint pnt = new GeographicPoint();
        pnt.setLatitude(pos.getLatitude().getDegrees());
        pnt.setLongitude(pos.getLongitude().getDegrees());
        
        // Create User defined point/marker
        userDefGeoRef = new GeorefResult();
        userDefGeoRef.setWGS84Coordinate(pnt);
        userDefGeoRef.setParsePattern(getResourceString("GeoLocateResultsDisplay.USRDEF")); // XXX I18N
        tableModel.add(userDefGeoRef);
        
        // Auto select the User Defined row
        int lastRow = tableModel.getRowCount() - 1;
        resultsTable.getSelectionModel().setSelectionInterval(lastRow, lastRow);
        resultsTable.repaint();
        
        wwPanel.placeMarkers(tableModel.getPoints(), null);
    }
    
    /**
     * 
     */
    private void repositionUserDefMarker()
    {
        Position        pos = wwPanel.getWorld().getCurrentPosition();
        GeographicPoint pnt = userDefGeoRef.getWGS84Coordinate();
        pnt.setLatitude(pos.getLatitude().getDegrees());
        pnt.setLongitude(pos.getLongitude().getDegrees());
        
        tableModel.fireTableCellUpdated(tableModel.getRowCount()-1, 1);
        tableModel.fireTableCellUpdated(tableModel.getRowCount()-1, 2);
        
        wwPanel.placeMarkers(tableModel.getPoints(), null);
        wwPanel.getWorld().repaint();
        
        int lastRow = tableModel.getRowCount() - 1;
        resultsTable.getSelectionModel().setSelectionInterval(lastRow, lastRow);
        resultsTable.repaint();
    }
    
    /**
     * @param localityString
     * @param county
     * @param state
     * @param country
     * @param georefResults
     */
    public void setGeoLocateQueryAndResults(String localityString, 
                                            String county, 
                                            String state, 
                                            String country, 
                                            GeorefResultSet georefResults)
    {
        userDefGeoRef = null;
        lastClickPos  = null;
        localityStringField.setText(localityString);
        localityStringField.setCaretPosition(0);
        countyField.setText(county);
        countyField.setCaretPosition(0);
        stateField.setText(state);
        stateField.setCaretPosition(0);
        countryField.setText(country);
        countryField.setCaretPosition(0);
        
        tableModel.setResultSet(georefResults.getResultSet());
        
        if (wwPanel != null)
        {
            ArrayList<LatLonPlacemarkIFace> pnts = new ArrayList<LatLonPlacemarkIFace>(georefResults.getResultSet().size());
            for (GeorefResult grr : georefResults.getResultSet())
            {
                pnts.add(new LatLonPoint(grr.getWGS84Coordinate().getLatitude(), grr.getWGS84Coordinate().getLongitude()));
            }
            wwPanel.placeMarkers(pnts, 0);
            
        } else
        {
            mapLabel.setText(getResourceString("GeoLocateResultsDisplay.LOADING_MAP")); //$NON-NLS-1$
        }
        //GeoLocate.getMapOfGeographicPoints(georefResults.getResultSet(), GeoLocateResultsDisplay.this);
        
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
        if (mapLabel != null) mapLabel.setText(getResourceString("GeoLocateResultsDisplay.ERROR_GETTING_MAP")); //$NON-NLS-1$
        JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setErrorMessage(getResourceString("GeoLocateResultsDisplay.ERROR_GETTING_MAP"), e); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener#mapReceived(javax.swing.Icon)
     */
    public void mapReceived(Icon map)
    {
        JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setText(""); //$NON-NLS-1$
        mapLabel.setText(null);
        mapLabel.setIcon(map);
        repaint();
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.worldwind.event.SelectListener#selected(gov.nasa.worldwind.event.SelectEvent)
     */
    @Override
    public void selected(final SelectEvent event)
    {
        if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
        {
            // This is a left click
            if (event.hasObjects() && event.getTopPickedObject().hasPosition())
            {
                lastClickPos = wwPanel.getWorld().getCurrentPosition();
                
                // There is a picked object with a position
// ZZZ                 
//                if (wwPanel.getWorld().getView() instanceof OrbitView)
//                {
//                    if (event.getTopObject().getClass().equals(BasicMarker.class))
//                    {
//                        int inx = wwPanel.getMarkers().indexOf(event.getTopObject());
//                        if (inx > -1)
//                        {
//                            resultsTable.setRowSelectionInterval(inx, inx);
//                        }
//                    } else if (event.getTopObject().getClass().equals(GlobeAnnotation.class))
//                    {
//                        int inx = wwPanel.getAnnotations().indexOf(event.getTopObject());
//                        if (inx > -1)
//                        {
//                            resultsTable.setRowSelectionInterval(inx, inx);
//                        }
//                    }
//                }
            }
        }
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
        add(createI18NFormLabel(labelStr), cc.xy(column,row)); //$NON-NLS-1$
        JTextField tf = createTextField();
        tf.setEditable(false);
        add(tf, cc.xy(column+2,row));
        return tf;
    }
    
    /**
     * Cleans up the panel.
     */
    public void shutdown()
    {
        if (wwPanel != null)
        {
            wwPanel.shutdown();
        }
    }

    /**
     * Creates a {@link JTextField} customized for use in this UI widget.
     * 
     * @return a {@link JTextField}
     */
    protected JTextField createTextField()
    {
        JTextField tf     = UIHelper.createTextField();
        Insets     insets = tf.getBorder().getBorderInsets(tf);
        tf.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.bottom));
        tf.setForeground(Color.BLACK);
        tf.setBackground(Color.WHITE);
        tf.setEditable(false);
        return tf;
    }
    
    //-----------------------------------------------------------------
    //
    //-----------------------------------------------------------------
    protected class ResultsTableModel extends AbstractTableModel
    {
        protected List<GeorefResult> results;
        
        public void setResultSet(List<GeorefResult> results)
        {
            this.results = results;
            fireTableDataChanged();
        }
        
        /**
         * @param grr
         */
        public void add(final GeorefResult grr)
        {
            results.add(grr);
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    fireTableDataChanged();
                }
            });
        }
        
        /**
         * @param index
         * @return
         */
        public GeorefResult getResult(int index)
        {
            return results.get(index);
        }
        
        /**
         * @return the results
         */
        public List<GeorefResult> getResults()
        {
            return results;
        }
        
        /**
         * @return
         */
        public List<LatLonPlacemarkIFace> getPoints()
        {
            ArrayList<LatLonPlacemarkIFace> pnts = new ArrayList<LatLonPlacemarkIFace>(results.size());
            
            for (GeorefResult grr : results)
            {
                pnts.add(new LatLonPoint(grr.getWGS84Coordinate().getLatitude(), grr.getWGS84Coordinate().getLongitude()));
            }
            return pnts;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
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

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            switch (column)
            {
                case 0:
                {
                    return getResourceString("GeoLocateResultsDisplay.NUMBER"); //$NON-NLS-1$
                }
                case 1:
                {
                    return getResourceString("GeoLocateResultsDisplay.LATITUDE"); //$NON-NLS-1$
                }
                case 2:
                {
                    return getResourceString("GeoLocateResultsDisplay.LONGITUDE"); //$NON-NLS-1$
                }
                case 3:
                {
                    return getResourceString("GeoLocateResultsDisplay.PARSE_PATTERN"); //$NON-NLS-1$
                }
            }
            return null;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return 4;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return (results == null) ? 0 : results.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        @Override
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
}
