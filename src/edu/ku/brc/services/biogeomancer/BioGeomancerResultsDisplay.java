package edu.ku.brc.services.biogeomancer;

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
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener;
import edu.ku.brc.ui.UIHelper;

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
    
    public BioGeomancerResultsDisplay()
    {
        String rowDef = UIHelper.createDuplicateJGoodiesDef("p", "2px", 13) + ",20px,p:g";
        setLayout(new FormLayout("p,10px,400px,10px,C:p:g", rowDef));

        CellConstraints cc = new CellConstraints();

        int rowIndex = 1;
        idField       = addRow(cc, getResourceString("ID"),        1, rowIndex);
        rowIndex+=2;
        countryField  = addRow(cc, getResourceString("Country"),   1, rowIndex);
        rowIndex+=2;
        adm1Field     = addRow(cc, getResourceString("Adm1"),      1, rowIndex);
        rowIndex+=2;
        adm2Field     = addRow(cc, getResourceString("Adm2"),      1, rowIndex);
        rowIndex+=2;
        localityField = addRow(cc, getResourceString("Locality"),  1, rowIndex);
        rowIndex+=2;

        countryBoundingBoxField       = addRow(cc, getResourceString("CountryBoundingBox"),       1, rowIndex);
        rowIndex+=2;
        matchedCountField             = addRow(cc, getResourceString("MatchedCount"),             1, rowIndex);
        rowIndex+=2;
        boundingBoxField              = addRow(cc, getResourceString("BoundingBox"),              1, rowIndex);
        rowIndex+=2;
        boundingBoxCentroidField      = addRow(cc, getResourceString("BoundingBoxCentroid"),      1, rowIndex);
        rowIndex+=2;
        centroidErrorRadiusField      = addRow(cc, getResourceString("CentroidErrorRadius"),      1, rowIndex);
        rowIndex+=2;
        centroidErrorRadiusUnitsField = addRow(cc, getResourceString("CentroidErrorRadiusUnits"), 1, rowIndex);
        rowIndex+=2;
        multiPointMatchField          = addRow(cc, getResourceString("MultiPointMatch"),          1, rowIndex);
        rowIndex+=2;
        weightedCentroidField         = addRow(cc, getResourceString("WeightedCentroid"),         1, rowIndex);
        rowIndex+=2;

        mapLabel = new JLabel();
        mapLabel.setText(getResourceString("LOADING_MAP"));
        add(mapLabel, cc.xywh(5,1,1,25));

        mapLabel.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));

        bgResultsTable = new JTable();
        bgResultsTable.setShowVerticalLines(false);
        bgResultsTable.setShowHorizontalLines(false);
        bgResultsTable.setRowSelectionAllowed(true);

        mapLabel.setText("");

        JScrollPane scrollPane = new JScrollPane(bgResultsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, cc.xywh(1,rowIndex, 5, 1));
        rowIndex+=2;
    }
    
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

    protected JTextField addRow(final CellConstraints cc,
                                final String labelStr,
                                final int column,
                                final int row,
                                int colSpan)
    {
        add(new JLabel(labelStr+":", SwingConstants.RIGHT), cc.xy(column,row));
        JTextField tf = createTextField();
        add(tf, cc.xywh(column+2,row, colSpan,1));
        return tf;
    }

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
    
    public void setBioGeomancerResultsData(String bgXmlResponse) throws Exception
    {
        mapLabel.setIcon(null);
        mapLabel.setText(getResourceString("LOADING_MAP"));
        
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
    
    public void addListSelectionListener( ListSelectionListener listener )
    {
        bgResultsTable.getSelectionModel().addListSelectionListener(listener);
    }
    
    public void removeListSelectionListener( ListSelectionListener listener )
    {
        bgResultsTable.getSelectionModel().removeListSelectionListener(listener);
    }
    
    public BioGeomancerResultStruct getSelectedResult()
    {
        int rowIndex = bgResultsTable.getSelectedRow();
        if (rowIndex < 0 || rowIndex > summary.results.length-1)
        {
            return null;
        }
        
        return summary.results[rowIndex];
    }
    
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

    public void exceptionOccurred(Exception e)
    {
        mapLabel.setText("Error while grabbing map");
    }

    public void mapReceived(Icon map)
    {
        mapLabel.setIcon(map);
    }
}
