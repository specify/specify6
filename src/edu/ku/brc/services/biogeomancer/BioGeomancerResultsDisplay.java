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
package edu.ku.brc.services.biogeomancer;

import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.services.mapping.LocalityMapper.MapperListener;
import edu.ku.brc.ui.UIHelper;

/**
 * A UI panel for use in displaying the results of a BioGeomancer Classic query.
 * 
 * @author jstewart
 * @code_status Beta
 */
public class BioGeomancerResultsDisplay extends JPanel implements MapperListener
{
    protected static final int MAP_WIDTH  = 400;
    protected static final int MAP_HEIGHT = 250;

    protected JPanel topPanel;
    protected JLabel mapLabel;
    protected JPanel resultsDetailsForm;
    protected JTable bgResultsTable;
    
    protected JTextField idField;
    protected JTextField countryField;
    protected JTextField adm1Field;
    protected JTextField adm2Field;
    protected JTextField localityField;
    protected JTextField countryBoundingBoxField;
    protected JTextField matchedCountField;
    protected JTextField boundingBoxField;
    protected JTextField boundingBoxCentroidField;
    protected JTextField centroidErrorRadiusField;
    protected JTextField centroidErrorRadiusUnitsField;
    protected JTextField multiPointMatchField;
    protected JTextField weightedCentroidField;
    
    protected BioGeomancerQuerySummaryStruct summary;
    
    /**
     * Constructor.  Creates all of the internal UI contents.
     */
    public BioGeomancerResultsDisplay()
    {
        String rowDef = UIHelper.createDuplicateJGoodiesDef("p", "2px", 13) + ",20px,p:g"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        setLayout(new FormLayout("p,10px,400px,10px,C:p:g", rowDef)); //$NON-NLS-1$

        CellConstraints cc = new CellConstraints();

        int rowIndex = 1;
        idField       = addRow(cc, getResourceString("BioGeomancerResultsDisplay.ID"),        1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        countryField  = addRow(cc, getResourceString("BioGeomancerResultsDisplay.COUNTRY"),   1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        adm1Field     = addRow(cc, getResourceString("BioGeomancerResultsDisplay.ADM1"),      1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        adm2Field     = addRow(cc, getResourceString("BioGeomancerResultsDisplay.ADM2"),      1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        localityField = addRow(cc, getResourceString("BioGeomancerResultsDisplay.LOCALITY"),  1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;

        countryBoundingBoxField       = addRow(cc, getResourceString("BioGeomancerResultsDisplay.COUNTRY_BOUNDING_BOX"),       1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        matchedCountField             = addRow(cc, getResourceString("BioGeomancerResultsDisplay.MATCHED_CNT"),             1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        boundingBoxField              = addRow(cc, getResourceString("BioGeomancerResultsDisplay.BOUNDING_BOX"),              1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        boundingBoxCentroidField      = addRow(cc, getResourceString("BioGeomancerResultsDisplay.BOUNDING_BOX_CENTROID"),      1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        centroidErrorRadiusField      = addRow(cc, getResourceString("BioGeomancerResultsDisplay.CENTROID_ERROR_RADIUS"),      1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        centroidErrorRadiusUnitsField = addRow(cc, getResourceString("BioGeomancerResultsDisplay.CENTROID_ERROR_RADIUS_UNITS"), 1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        multiPointMatchField          = addRow(cc, getResourceString("BioGeomancerResultsDisplay.MULTI_POINT_MATCH"),          1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;
        weightedCentroidField         = addRow(cc, getResourceString("BioGeomancerResultsDisplay.WEIGHTED_CENTROID"),         1, rowIndex); //$NON-NLS-1$
        rowIndex+=2;

        mapLabel = createLabel(getResourceString("BioGeomancerResultsDisplay.LOADING_MAP")); //$NON-NLS-1$
        add(mapLabel, cc.xywh(5,1,1,25));

        mapLabel.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));

        bgResultsTable = new JTable();
        bgResultsTable.setShowVerticalLines(false);
        bgResultsTable.setShowHorizontalLines(false);
        bgResultsTable.setRowSelectionAllowed(true);

        mapLabel.setText(""); //$NON-NLS-1$

        JScrollPane scrollPane = new JScrollPane(bgResultsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, cc.xywh(1,rowIndex, 5, 1));
        rowIndex+=2;
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
    
    /**
     * Parses the XML reply from BioGeomancer Classic, creating a {@link BioGeomancerQuerySummaryStruct}
     * holding the corresponding data.  The data is then displayed in the appropriate UI widgets.
     * 
     * @param bgXmlResponse
     * @throws Exception
     */
    public void setBioGeomancerResultsData(String bgXmlResponse) throws Exception
    {
        mapLabel.setIcon(null);
        mapLabel.setText(getResourceString("BioGeomancerResultsDisplay.LOADING_MAP")); //$NON-NLS-1$
        
        summary = BioGeomancer.parseBioGeomancerResponse(bgXmlResponse);
        
        idField.setText(summary.id);
        idField.setCaretPosition(0);
        countryField.setText(summary.country);
        countryField.setCaretPosition(0);
        adm1Field.setText(summary.adm1);
        adm1Field.setCaretPosition(0);
        adm2Field.setText(summary.adm2);
        adm2Field.setCaretPosition(0);
        localityField.setText(summary.localityStr);
        localityField.setCaretPosition(0);
        countryBoundingBoxField.setText(summary.countryBoundingBox);
        countryBoundingBoxField.setCaretPosition(0);
        matchedCountField.setText(summary.matchedRecordCount);
        matchedCountField.setCaretPosition(0);
        boundingBoxField.setText(summary.boundingBox);
        boundingBoxField.setCaretPosition(0);
        boundingBoxCentroidField.setText(summary.boundingBoxCentroid);
        boundingBoxCentroidField.setCaretPosition(0);
        centroidErrorRadiusField.setText(summary.boundingBoxCentroidErrorRadius);
        centroidErrorRadiusField.setCaretPosition(0);
        centroidErrorRadiusUnitsField.setText(summary.boundingBoxCentroidErrorRadiusUnits);
        centroidErrorRadiusUnitsField.setCaretPosition(0);
        multiPointMatchField.setText(summary.multiPointMatch);
        multiPointMatchField.setCaretPosition(0);
        weightedCentroidField.setText(summary.weightedCentroid);
        weightedCentroidField.setCaretPosition(0);
        
        bgResultsTable.setModel(new BioGeomancerResultsTableModel(summary.results));
        UIHelper.calcColumnWidths(bgResultsTable);
        
        BioGeomancer.getMapOfQuerySummary(summary, this);
        repaint();
    }
    
    /**
     * Adds a list selection listener to the results listing.
     * 
     * @param listener the listener to add
     */
    public void addListSelectionListener( ListSelectionListener listener )
    {
        bgResultsTable.getSelectionModel().addListSelectionListener(listener);
    }
    
    /**
     * Removes the given {@link ListSelectionListener} (if it was previously added).
     * 
     * @param listener the listener to remove
     */
    public void removeListSelectionListener( ListSelectionListener listener )
    {
        bgResultsTable.getSelectionModel().removeListSelectionListener(listener);
    }
    
    /**
     * Returns the selected result.
     * 
     * @return the selected result
     */
    public BioGeomancerResultStruct getSelectedResult()
    {
        int rowIndex = bgResultsTable.getSelectedRow();
        if (rowIndex < 0 || rowIndex > summary.results.length-1)
        {
            return null;
        }
        
        return summary.results[rowIndex];
    }
    
    /**
     * Selects the result with the given index in the results list.
     * 
     * @param index the index of the result to select
     */
    public void setSelectedResult(int index)
    {
        if (index < 0 || index > bgResultsTable.getRowCount()-1)
        {
            bgResultsTable.clearSelection();
        }
        else
        {
            bgResultsTable.setRowSelectionInterval(index, index);
            int colCount = bgResultsTable.getColumnCount();
            bgResultsTable.setColumnSelectionInterval(0, colCount-1);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener#exceptionOccurred(java.lang.Exception)
     */
    public void exceptionOccurred(Exception e)
    {
        // blame this on the map service
        String errorMsg = getResourceString("BioGeomancerResultsDisplay.WB_MAP_SERVICE_CONNECTION_FAILURE"); //$NON-NLS-1$
        mapLabel.setText(errorMsg);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener#mapReceived(javax.swing.Icon)
     */
    public void mapReceived(Icon map)
    {
        mapLabel.setIcon(map);
    }
}
