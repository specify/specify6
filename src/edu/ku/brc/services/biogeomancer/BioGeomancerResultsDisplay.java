package edu.ku.brc.services.biogeomancer;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Dimension;

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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyKrupp;

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
        String rowDef = UIHelper.createDuplicateJGoodiesDef("p", "2px", 19);
        setLayout(new FormLayout("p,2px,p,10px,p,2px,C:p:g", rowDef));

        CellConstraints cc = new CellConstraints();

        int rowIndex = 3;
        idField       = addRow(cc, "ID",        1, rowIndex);
        rowIndex+=2;
        countryField  = addRow(cc, "Country",   1, rowIndex);
        rowIndex+=2;
        adm1Field     = addRow(cc, "Adm1",      1, rowIndex);
        rowIndex+=2;
        adm2Field     = addRow(cc, "Adm2",      1, rowIndex);
        rowIndex+=2;
        localityField = addRow(cc, "Locality",  1, rowIndex);
        rowIndex+=2;

        mapLabel = new JLabel();
        mapLabel.setText(getResourceString("LOADING_MAP"));
        add(mapLabel, cc.xywh(7,3,1,25));

        mapLabel.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));

        countryBoundingBoxField       = addRow(cc, "Country Bounding Box",        1, rowIndex);
        rowIndex+=2;
        matchedCountField             = addRow(cc, "Matched Count",               1, rowIndex);
        rowIndex+=2;
        boundingBoxField              = addRow(cc, "Bounding Box",                1, rowIndex);
        rowIndex+=2;
        boundingBoxCentroidField      = addRow(cc, "Bounding Box Centroid",       1, rowIndex);
        rowIndex+=2;
        centroidErrorRadiusField      = addRow(cc, "Centroid Error Radius",       1, rowIndex);
        rowIndex+=2;
        centroidErrorRadiusUnitsField = addRow(cc, "Centroid Error Radius Units", 1, rowIndex);
        rowIndex+=2;
        multiPointMatchField          = addRow(cc, "Multi Point Match",           1, rowIndex);
        rowIndex+=2;
        weightedCentroidField         = addRow(cc, "Weighted Centroid",           1, rowIndex);
        rowIndex+=2;

        bgResultsTable = new JTable();
        bgResultsTable.setShowVerticalLines(false);
        bgResultsTable.setShowHorizontalLines(false);
        bgResultsTable.setRowSelectionAllowed(true);

        mapLabel.setText("");

        JScrollPane scrollPane = new JScrollPane(bgResultsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, cc.xywh(1,rowIndex, 7, 1));
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
        JTextField dataLabel = new JTextField();
        dataLabel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        return dataLabel;
    }
    
    public void setBioGeomancerResultsData(String bgXmlResponse) throws Exception
    {
        summary = BioGeomancer.parseBioGeomancerResponse(bgXmlResponse);
        
        idField.setText(summary.id);
        countryField.setText(summary.country);
        adm1Field.setText(summary.adm1);
        adm2Field.setText(summary.adm2);
        localityField.setText(summary.localityStr);
        countryBoundingBoxField.setText(summary.countryBoundingBox);
        matchedCountField.setText(summary.matchedRecordCount);
        boundingBoxField.setText(summary.boundingBox);
        boundingBoxCentroidField.setText(summary.boundingBoxCentroid);
        centroidErrorRadiusField.setText(summary.boundingBoxCentroidErrorRadius);
        centroidErrorRadiusUnitsField.setText(summary.boundingBoxCentroidErrorRadiusUnits);
        multiPointMatchField.setText(summary.multiPointMatch);
        weightedCentroidField.setText(summary.weightedCentroid);
        
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

    public void exceptionOccurred(Exception e)
    {
        mapLabel.setText("Error while grabbing map");
    }

    public void mapReceived(Icon map)
    {
        mapLabel.setIcon(map);
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException
    {
        UIManager.setLookAndFeel(new PlasticLookAndFeel());
        PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());

        BioGeomancerResultsDisplay d = new BioGeomancerResultsDisplay();
        JFrame f = new JFrame();
        f.add(d);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        
        try
        {
            d.setBioGeomancerResultsData("<SOAP-ENV:Envelope xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\" xmlns:namesp2=\"http://namespaces.soaplite.com/perl\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><SOAP-ENV:Body><namesp1:text2latlongResponse xmlns:namesp1=\"http://130.132.27.130/GeoParse\"><return xsi:type=\"namesp2:returnType\" SOAP-ENC:arrayType=\"namesp2:responseSetType[1]\"><responseSet xsi:type=\"namesp2:responseSetType\"><summary xsi:type=\"namesp2:summaryType\"><queryId xsi:type=\"xsd:string\">39</queryId><queryCountry xsi:type=\"xsd:string\">USA</queryCountry><queryAdm1 xsi:type=\"xsd:string\">Kansas</queryAdm1><queryAdm2 xsi:type=\"xsd:string\">Cherokee</queryAdm2><queryString xsi:type=\"xsd:string\">Spring River</queryString><countryName xsi:type=\"xsd:string\">United States</countryName><countryBoundingBox xsi:type=\"xsd:string\">-180 -2. -15.5 90</countryBoundingBox><matchedRecordCount xsi:type=\"xsd:int\">1</matchedRecordCount><boundingBox xsi:type=\"xsd:string\">BOX(-94.7525 36.79194, -94.7525 36.79194)</boundingBox><boundingBoxCentroid xsi:type=\"xsd:string\">POINT(-94.7525 36.79194)</boundingBoxCentroid><boundingBoxCentroidErrorRadius xsi:type=\"xsd:float\">0.0</boundingBoxCentroidErrorRadius><boundingBoxCentroidErrorRadiusUnits xsi:type=\"xsd:string\">km</boundingBoxCentroidErrorRadiusUnits><multiPointMatch xsi:type=\"xsd:string\">MULTIPOINT(-94.7525 36.79194)</multiPointMatch><weightedCentroid xsi:type=\"xsd:string\">POINT(-94.7525 36.79194)</weightedCentroid></summary><records xsi:type=\"namesp2:recordsType\" SOAP-ENC:arrayType=\"namesp2:recordType[1]\"><record xsi:type=\"namesp2:recordType\"><adm2 xsi:type=\"xsd:string\">Cherokee</adm2><adm1 xsi:type=\"xsd:string\">KS</adm1><country xsi:type=\"xsd:string\">USA</country><featureType xsi:type=\"xsd:string\">stream</featureType><featureName xsi:type=\"xsd:string\">Spring River</featureName><gazetteerSource xsi:type=\"xsd:string\">GNIS</gazetteerSource><InterpretedCoordinates xsi:type=\"xsd:string\">POINT(-94.7525 36.79194)</InterpretedCoordinates><boundingBox xsi:type=\"xsd:string\">BOX(-94.7525 36.79194, -94.7525 36.79194)</boundingBox><sourceCoordinates xsi:type=\"xsd:string\">POINT(-94.7525 36.79194)</sourceCoordinates><InterpretedString xsi:type=\"xsd:string\">Spring River</InterpretedString></record></records></responseSet></return></namesp1:text2latlongResponse></SOAP-ENV:Body></SOAP-ENV:Envelope>");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
