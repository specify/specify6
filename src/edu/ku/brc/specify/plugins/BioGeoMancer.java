/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */package edu.ku.brc.specify.plugins;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.extras.BGMRecordTableModel;
import edu.ku.brc.specify.extras.BioGeoMancerMapper;
import edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener;
import edu.ku.brc.specify.tasks.services.biogeomancer.BioGeomancerQuerySummary;
import edu.ku.brc.specify.tasks.services.biogeomancer.BioGeomancerResult;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIPluginable;

/**
 * BioGeoMancer plugin For SPNHC Demo.
 
 * @code_status Alpha
 **
 * @author rods
 *
 */
public class BioGeoMancer extends JPanel implements GetSetValueIFace, UIPluginable, PropertyChangeListener, MapperListener
{
    private static final Logger log = Logger.getLogger(BioGeoMancer.class);
    
    protected static Exception    exception = null; 
    
    protected boolean             doingCache = false;

    protected static final int BGM_WIDTH  = 400;
    protected static final int BGM_HEIGHT = 250;

    protected JTextField          latitude;
    protected JTextField          longitude;
    protected Locality            locality;
    protected JButton             infoBtn    = null;
    protected JProgressBar        progress   = null;
    protected JTable              bgResultsTable;

    protected JFrame              frame      = null;
    protected JButton             okBtn      = null;

    protected JLabel             label      = new JLabel();

    protected BioGeoMancerMapper bioGeoMancerMapper = new BioGeoMancerMapper();
    
    protected int numResults = 0;

    /**
     * Constructor.
     */
    public BioGeoMancer()
    {
        // nothing
    }


    /**
     * Creates a Dialog (non-modl) that will display detail information.
     * for the object in the text field.
     * 
     * @param domStr DOM String to be parsed
     */
    public void createInfoFrame(@SuppressWarnings("unused") final String domStr)
    {
        try
        {
            frame = new JFrame();
            JPanel p = new JPanel(new BorderLayout());
            p.add(createBGMPanel(XMLHelper.readFileToDOM4J(new File("biogeomancer.xml")), null, true), BorderLayout.CENTER);

            frame.setContentPane(p);
            frame.setVisible(true);
            frame.setLocation(0,0);
            frame.setSize(frame.getPreferredSize());

            //progress.setIndeterminate(true);
            //progress.setValue(50);
            progress.setVisible(false);

            ImageIcon icon = IconManager.getIcon("BioGeoMancer", IconManager.IconSize.Std16);
            if (icon != null)
            {
                frame.setIconImage(icon.getImage());
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see java.awt.Component#requestFocus()
     */
    @Override
    public void requestFocus()
    {
        longitude.requestFocus();
    }

    /**
     * Called when data retrieval is complete.
     * @param dom null when in error, not null when data was returned
     */
    protected void setDataIntoframe(@SuppressWarnings("unused") final Element dom)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                progress.setIndeterminate(false);
                progress.setVisible(false);
                //frame.setData(getter.getDom());
            }
        });
    }
    
    /**
     * Returns the number of results retrieved from BioGeomancer.
     * 
     * @return the number of BioGeomancer result candidates
     */
    public int getResultsCount()
    {
        return numResults;
    }
    
    /**
     * Process the DOM element.
     * @param element element
     * @param name element
     * @return the data
     */
    protected static String getData(final Element element, final String name)
    {
        Element node = (Element)element.selectSingleNode(name);
        if (node != null)
        {
            String data = node.getTextTrim();
            int inx = data.indexOf("(");
            if (inx != -1)
            {
                int einx = data.indexOf(")");
                return data.substring(inx+1, einx);
            }
            return data;
        }
        
        // else
        // Although the name may not have been found it could be because no results came back
        log.debug("****** ["+name+"] was not found.");
        return "";
    }

    /**
     * Create data label.
     * @param element element
     * @param name name
     * @return a JComponent
     */
    protected JComponent createDataLabel(final Element element, final String name)
    {
        try
        {
            String data = getData(element, name);
            JTextField dataLabel = new JTextField(data);
            dataLabel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
            return dataLabel;

        } catch (Exception ex)
        {
            log.warn("Couldn't find value for["+name+"]");
        }
        return new JLabel("", SwingConstants.LEFT);
    }

    /**
     * @param element
     * @param builder
     * @param cc
     * @param labelArg
     * @param name
     * @param column
     * @param row
     */
    protected void addRow(final Element element,
                          final PanelBuilder builder,
                          final CellConstraints cc,
                          final String labelArg,
                          final String name,
                          final int column,
                          final int row)
    {
        builder.add(new JLabel(labelArg+":", SwingConstants.RIGHT), cc.xy(column,row));
        builder.add(createDataLabel(element, name), cc.xy(column+2,row));
    }

    /**
     * @param element
     * @param builder
     * @param cc
     * @param labelArg
     * @param name
     * @param column
     * @param row
     * @param colSpan
     */
    protected void addRow(final Element element,
            final PanelBuilder builder,
            final CellConstraints cc,
            final String labelArg,
            final String name,
            final int column,
            final int row,
            int colSpan)
    {
        builder.add(new JLabel(labelArg+":", SwingConstants.RIGHT), cc.xy(column,row));
        builder.add(createDataLabel(element, name), cc.xywh(column+2,row, colSpan,1));
    }

    /**
     * This method inspects the response received from a BG call and determines
     * the number of possible results given.
     * 
     * @param bioGeomancerResponseString the response from the BG service
     * @return the number of possible results found in the given response
     * @throws Exception if parsing the response string fails
     */
    public static int getResultsCount(final String bioGeomancerResponseString) throws Exception
    {
        Element responseAsXml = XMLHelper.readStrToDOM4J(bioGeomancerResponseString);
        List<?> records = responseAsXml.selectNodes("//record");
        return (records != null) ? records.size() : 0;
    }

    /**
     * Processes the document that was returned.
     * @param root the root DOM node of the document
     * @return a panel with the results
     */
    @SuppressWarnings("null")
    public JPanel createBGMPanel(final Element root, final Window window, final boolean createBtns)
    {
        String rowDef = UIHelper.createDuplicateJGoodiesDef("p", "2px", 19);
        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p,10px,p,2px,C:p:g", rowDef));

        Element summary = (Element)root.selectSingleNode("//summary");
        if (summary != null)
        {
           CellConstraints cc = new CellConstraints();
           builder.addSeparator("Biogeomancer Results", cc.xywh(1, 1, 7, 1));

           addRow(summary, builder, cc, "ID",       "queryId",      1, 3);
           addRow(summary, builder, cc, "Country",  "queryCountry", 1, 5);
           addRow(summary, builder, cc, "Adm1",     "queryAdm1",    1, 7);
           addRow(summary, builder, cc, "Adm2",     "queryAdm2",    1, 9);
           addRow(summary, builder, cc, "Locality", "queryString",  1, 11);

           label.setText(getResourceString("LOADING_MAP"));
           builder.add(label, cc.xywh(7,3,1,25));
           //builder.add(new JScrollPane(label, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), cc.xywh(7,3,1,25));

           label.setPreferredSize(new Dimension(BGM_WIDTH, BGM_HEIGHT));
           
           JButton closeBtn = null;
           if (createBtns)
           {
               okBtn = new JButton(getResourceString("Accept"));
               okBtn.setEnabled(false);
               closeBtn = new JButton(getResourceString("Close"));
           }
           
           int rowInx = 13;
           String [] dataNames  = {"countryBoundingBox",   "matchedRecordCount", "boundingBox",  "boundingBoxCentroid",   "boundingBoxCentroidErrorRadius", "boundingBoxCentroidErrorRadiusUnits", "multiPointMatch",  "weightedCentroid"};
           String [] dataLabels = {"Country Bounding Box", "Matched Count",      "Bounding Box", "Bounding Box Centroid", "Centroid Error Radius",          "Centroid Error Radius Units",         "Multi Point Match", "Weighted Centroid"};
           for (int i=0;i<dataNames.length;i++)
           {
               addRow(summary, builder, cc, dataLabels[i], dataNames[i], 1, rowInx);
               rowInx += 2;
           }

           List<String[]> rowData = new ArrayList<String[]>();
           List<?> records = root.selectNodes("//record");
           numResults = (records != null) ? records.size() : 0;
           if (records != null && records.size() > 0)
           {
               String[] elementNames = {"country", "adm1", "adm2", "featureName", "featureType", "gazetteerSource", "InterpretedCoordinates", "offsetVector", "boundingBox", "InterpretedString"};
               int cnt = 0;
               for ( Object obj : records)
               {
                   cnt++;
                   Element rec = (Element)obj;
                   String[] row = new String[11];

                   row[0] = Integer.toString(cnt);
                   for (int i=0;i<elementNames.length;i++)
                   {
                       //log.debug("["+elementNames[i]+"]");
                       row[i+1] = getData(rec, elementNames[i]);

                       log.debug("["+elementNames[i]+"]["+row[i]+"]");
                   }

                   if (true)
                   {
                       String[] coords = StringUtils.split(getData(rec, "InterpretedCoordinates"));
                       double lon = Double.parseDouble(coords[0]);
                       double lat = Double.parseDouble(coords[1]);

                       String s = getData(rec, "boundingBox");
                       if (StringUtils.isNotEmpty(s))
                       {
                           String[] boxList = StringUtils.split(s.replace(',', ' '));
                           double[] box = new double[4];
                           for (int i=0;i<boxList.length;i++)
                           {
                               box[i] = Double.parseDouble(boxList[i]);
                           }
                           bioGeoMancerMapper.addBGMDataAndLabel(lat, lon, box[1], box[0], box[3], box[2], Integer.toString(cnt));
                           bioGeoMancerMapper.setMaxMapWidth(BGM_WIDTH);
                           bioGeoMancerMapper.setMaxMapHeight(BGM_HEIGHT);
                       }
                   }
                   rowData.add(row);
               }
               bioGeoMancerMapper.getMap(this);
           }

           BGMRecordTableModel bgmTableModel = new BGMRecordTableModel(rowData);
           bgResultsTable = new JTable(bgmTableModel);
           bgResultsTable.setShowVerticalLines(false);
           bgResultsTable.setShowHorizontalLines(false);
           bgResultsTable.setRowSelectionAllowed(true);

           if (createBtns)
           {
               bgResultsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
               {
                   public void valueChanged(ListSelectionEvent e)
                   {
                       if (!e.getValueIsAdjusting() && okBtn != null && bgResultsTable != null)
                       {
                           okBtn.setEnabled(bgResultsTable.getSelectedRowCount() > 0);
                       }
                   }
               });
               
               bgResultsTable.addMouseListener(new MouseAdapter() {
                   @Override
                   public void mouseClicked(MouseEvent e)
                   {
                       if (e.getClickCount() == 2)
                       {
                           okBtn.doClick(); //emulate button click
                       }
                   }
               });
           }
           
           
           if (bgmTableModel.getRowCount() == 0)
           {
               label.setText("");
               
           } else if (bgmTableModel.getRowCount() == 1)
           {
               bgResultsTable.setRowSelectionInterval(0, 0);
               
               if (createBtns)
               {
                   okBtn.setEnabled(true);
               }
           }
           

           JScrollPane scrollPane = new JScrollPane(bgResultsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
           builder.add(scrollPane, cc.xywh(1,rowInx, 7, 1));
           rowInx += 2;
           

           //okBtn.addActionListener(this);
           if (getRootPane() != null)
           {
               getRootPane().setDefaultButton(okBtn);
           }

           PanelBuilder btnBar = new PanelBuilder(new FormLayout("50px,f:p:g,r:p,10px", "p"));
           progress = new JProgressBar(0, 100);
           progress.setVisible(false);
           
           if (createBtns)
           {
               btnBar.add(progress, cc.xy(1,1));
               btnBar.add(ButtonBarFactory.buildOKCancelBar(okBtn, closeBtn), cc.xy(3,1));
               builder.add(btnBar.getPanel(), cc.xywh(3,rowInx, 5, 1));

               closeBtn.addActionListener( new ActionListener() {
                   public void actionPerformed(ActionEvent e)
                   {
                       if (window!=null)
                       {
                           window.setVisible(false);
                           window.dispose();
                       }
                       else
                       {
                           frame.setVisible(false);
                           frame.dispose();
                           frame = null;
                       }
                   }
               });

               okBtn.addActionListener( new ActionListener() {
                   public void actionPerformed(ActionEvent e)
                   {

                       String lonLatStr = (String)bgResultsTable.getModel().getValueAt(bgResultsTable.getSelectedRow(), 7);
                       if (StringUtils.isNotEmpty(lonLatStr))
                       {
                           String[] coords = StringUtils.split(lonLatStr);
                           longitude.setText(coords[0]);
                           latitude.setText(coords[1]);
                       }
                       if (window!=null)
                       {
                           window.setVisible(false);
                           window.dispose();
                       }
                       else
                       {
                           frame.setVisible(false);
                           frame.dispose();
                           frame = null;
                       }
                   }
               });
           }

        }
        calcColumnWidths(bgResultsTable);
        return builder.getPanel();
    }

    
    
    /**
     * Calculates and sets the each column to it preferred size.
     * @param table the table to fix ups
     */
    protected void calcColumnWidths(JTable table)
    {
        JTableHeader header = table.getTableHeader();

        TableCellRenderer defaultHeaderRenderer = null;

        if (header != null)
        {
            defaultHeaderRenderer = header.getDefaultRenderer();
        }

        TableColumnModel columns = table.getColumnModel();
        TableModel data = table.getModel();

        int margin = columns.getColumnMargin(); // only JDK1.3

        int rowCount = data.getRowCount();

        int totalWidth = 0;

        for (int i = columns.getColumnCount() - 1; i >= 0; --i)
        {
            TableColumn column = columns.getColumn(i);

            int columnIndex = column.getModelIndex();

            int width = -1;

            TableCellRenderer h = column.getHeaderRenderer();

            if (h == null)
                h = defaultHeaderRenderer;

            if (h != null) // Not explicitly impossible
            {
                Component c = h.getTableCellRendererComponent
                       (table, column.getHeaderValue(),
                        false, false, -1, i);

                width = c.getPreferredSize().width;
            }

            for (int row = rowCount - 1; row >= 0; --row)
            {
                TableCellRenderer r = table.getCellRenderer(row, i);

                Component c = r.getTableCellRendererComponent
                   (table,
                    data.getValueAt(row, columnIndex),
                    false, false, row, i);

                    width = Math.max(width, c.getPreferredSize().width+10); // adding an arbitray 10 pixels to make it look nicer
            }

            if (width >= 0)
            {
                column.setPreferredWidth(width + margin); // <1.3: without margin
            }
            else
            {
                // ???
            }

            totalWidth += column.getPreferredWidth();
        }

        // If you like; This does not make sense for two many columns!
        Dimension size = table.getPreferredScrollableViewportSize();
        //if (totalWidth > size.width)
        {
            size.height = Math.min(size.height, table.getRowHeight()*10);
            size.width  = totalWidth;
            table.setPreferredScrollableViewportSize(size);
        }

    }


    //--------------------------------------------------------
    //-- UIPluginable
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#initialize(java.util.Properties, boolean)
     */
    public void initialize(final Properties properties, final boolean isViewMode)
    {

        PanelBuilder builder = new PanelBuilder(new FormLayout("p,1px,p,1px,p,1px,p", "p"), this);

        CellConstraints cc = new CellConstraints();
        longitude = new JTextField(10);
        longitude.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        
        builder.add(longitude, cc.xy(1,1));

        builder.add(new JLabel("/", SwingConstants.CENTER), cc.xy(3,1));

        //builder.add(new JLabel("Latitude:", JLabel.RIGHT), cc.xy(1,3));
        latitude = new JTextField(10);
        latitude.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        
        builder.add(latitude, cc.xy(5,1));

        ColorWrapper viewFieldColor = AppPrefsCache.getColorWrapper("ui", "formatting", "viewfieldcolor");
        if (viewFieldColor != null)
        {
            longitude.setBackground(viewFieldColor.getColor());
            latitude.setBackground(viewFieldColor.getColor());
        }

        infoBtn = new JButton(IconManager.getIcon("BioGeoMancer", IconManager.IconSize.Std16));
        infoBtn.setFocusable(false);
        infoBtn.setMargin(new Insets(2,2,2,2));
        //infoBtn.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        builder.add(infoBtn, cc.xy(7,1));

        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        infoBtn.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        //createInfoFrame();
                        getBioGeoMancerData();
                    }
                });
    }

    protected String getGeo(final Geography g, final int rankId)
    {
        Geography geo = g;
        while (geo != null && geo.getRankId() != rankId)
        {
            geo = geo.getParent();
        }
        if (geo != null)
        {
            return geo.getName();
        }
        return "";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }

    protected void getBioGeoMancerData()
    {
        final SwingWorker worker = new SwingWorker()
        {
            String domStr = "";

            @Override
            public Object construct()
            {
                Geography geo = locality.getGeography();

                if (!doingCache)
                {
                    domStr = getBioGeoMancerResponse(locality.getLocalityId().toString(), getGeo(geo, 200), getGeo(geo, 300), getGeo(geo, 400), locality.getLocalityName());
                    
                } else
                {
                    try
                    {
                        StringBuilder strBuf = new StringBuilder(521);
                        BufferedReader in = new BufferedReader(new FileReader("biogeomancer.xml"));
                        String str;
                        while ((str = in.readLine()) != null) {
                            strBuf.append(str);
                        }
                        in.close();
                        domStr = strBuf.toString();
                    }
                    catch (IOException e)
                    {
                        // ignore?
                    }
                }
                return null;
            }

            //Runs on the event-dispatching thread.
            @Override
            public void finished()
            {
                createInfoFrame(domStr);
            }
        };
        worker.start();
    }
    
    
    //--------------------------------------------------------
    //-- Utility Methods
    //--------------------------------------------------------

    /**
     * Sends request to BioGeoMancer.
     * @param id id
     * @param country country
     * @param adm1 country
     * @param adm2 adm2
     * @param localityArg locality
     * @return returns the response body content
     */
    public static String getBioGeoMancerResponse(final String id,
                                                 final String country,
                                                 final String adm1,
                                                 final String adm2,
                                                 final String localityArg)
    {
        // clear out the exception from any previous calls to this method
        exception = null;
        
        String retVal = null;
        
        try
        {
            HttpClient httpClient = new HttpClient();
            PostMethod postMethod = new PostMethod("http://130.132.27.130/cgi-bin/bgm-0.2/batch_test.pl");
            StringBuilder strBuf = new StringBuilder(128);
            strBuf.append("\""+ id + "\",");
            strBuf.append("\""+ country + "\",");
            strBuf.append("\""+ adm1 + "\",");
            strBuf.append("\""+ (adm2 != null ? adm2 : "") + "\",");
            strBuf.append("\""+ localityArg + "\"\r\n");

            NameValuePair[] postData = {
                //new NameValuePair("batchtext", "\"12931\",\"Mexico\",\"Veracruz\",\"\",\"12 km NW of Catemaco\"\r\n"),
                new NameValuePair("batchtext", strBuf.toString()),
                new NameValuePair("format", "xml") };

            //the 2.0 beta1 version has a
            // PostMethod.setRequestBody(NameValuePair[])
            //method, as addParameters is deprecated
            postMethod.addParameters(postData);

            String responseBody = "";

            httpClient.executeMethod(postMethod);
            responseBody = postMethod.getResponseBodyAsString();

            /*
            Writer output = new BufferedWriter(new FileWriter("biogeomancer.xml"));
            output.write(responseBody);
            output.flush();
            output.close();
    */
            
            //release the connection used by the method
            postMethod.releaseConnection();

            retVal = responseBody;
            
        } catch (Exception ex)
        {
            exception = ex;
        }
        return retVal;
    }
    
    public static BioGeomancerQuerySummary getBioGeomancerResponses(final String id,
                                                         final String country,
                                                         final String adm1,
                                                         final String adm2,
                                                         final String localityString)
                                                        throws Exception
    {
        String responseStr = getBioGeoMancerResponse(id, country, adm1, adm2, localityString);
        return parseBioGeomancerResponse(responseStr);
    }
    
    public static BioGeomancerQuerySummary parseBioGeomancerResponse(String bgResponse) throws Exception
    {
        // read the string into a DOM
        Element root = XMLHelper.readStrToDOM4J(bgResponse);
        Element summary = (Element)root.selectSingleNode("//summary");
        if (summary == null)
        {
            throw new Exception("BioGeomancer response is missing required data");
        }

        BioGeomancerQuerySummary querySummary = new BioGeomancerQuerySummary();

        // get all of the data from the summary section
        querySummary.id                                  = getData(summary,"queryId");
        querySummary.country                             = getData(summary,"queryCountry");
        querySummary.adm1                                = getData(summary,"queryAdm1");
        querySummary.adm2                                = getData(summary,"queryAdm2");
        querySummary.localityStr                         = getData(summary,"queryString");
        querySummary.countryBoundingBox                  = getData(summary,"countryBoundingBox");
        querySummary.matchedRecordCount                  = getData(summary,"matchedRecordCount");
        querySummary.boundingBox                         = getData(summary,"boundingBox");
        querySummary.boundingBoxCentroid                 = getData(summary,"boundingBoxCentroid");
        querySummary.boundingBoxCentroidErrorRadius      = getData(summary,"boundingBoxCentroidErrorRadius");
        querySummary.boundingBoxCentroidErrorRadiusUnits = getData(summary,"boundingBoxCentroidErrorRadiusUnits");
        querySummary.multiPointMatch                     = getData(summary,"multiPointMatch");
        querySummary.weightedCentroid                    = getData(summary,"weightedCentroid");

        // get each of the results records
        List<?> records = root.selectNodes("//record");
        BioGeomancerResult[] results = new BioGeomancerResult[records.size()];
        int index = 0;
        for (Object o: records)
        {
            Element record = (Element)o;
            BioGeomancerResult result = new BioGeomancerResult();
            result.country     = getData(record, "country");
            result.adm1        = getData(record, "adm1");
            result.adm2        = getData(record, "adm2");
            result.featureName = getData(record, "featureName");
            result.featureType = getData(record, "featureType");
            result.gazetteer   = getData(record, "gazetteerSource");
            result.coordinates = getData(record, "InterpretedCoordinates");
            result.offset      = getData(record, "offsetVector");
            result.boundingBox = getData(record, "boundingBox");
            result.locality    = getData(record, "InterpretedString");
            results[index++] = result;
        }
        querySummary.results = results;

        return querySummary;
    }
    
    public static void getMapOfBioGeomancerResults(String bgResponse, MapperListener callback) throws Exception
    {
        BioGeomancerQuerySummary summary = parseBioGeomancerResponse(bgResponse);
        getMapOfQuerySummary(summary, callback);
    }

    public static void getMapOfQuerySummary(BioGeomancerQuerySummary querySummary, MapperListener callback)
    {
        BioGeoMancerMapper bioGeoMancerMapper = new BioGeoMancerMapper();

        for (int i = 0; i < querySummary.results.length; ++i)
        {
            BioGeomancerResult result = querySummary.results[i];
            String[] coords = StringUtils.split(result.coordinates);
            double lon = Double.parseDouble(coords[0]);
            double lat = Double.parseDouble(coords[1]);
            
            String bbox = result.boundingBox;
            if (StringUtils.isNotEmpty(bbox))
            {
                String[] boxList = StringUtils.split(bbox.replace(',', ' '));
                double[] box = new double[4];
                for (int j = 0; j < boxList.length; ++j)
                {
                    box[j] = Double.parseDouble(boxList[j]);
                }
                bioGeoMancerMapper.addBGMDataAndLabel(lat, lon, box[1], box[0], box[3], box[2], Integer.toString(i+1));
                bioGeoMancerMapper.setMaxMapWidth(BGM_WIDTH);
                bioGeoMancerMapper.setMaxMapHeight(BGM_HEIGHT);
            }
        }
        bioGeoMancerMapper.getMap(callback);
    }
    
    public static Exception getException()
    {
        return exception;
    }

    //--------------------------------------------------------
    //-- GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        if (value != null && value instanceof Locality)
        {
            locality = (Locality)value;
            latitude.setText(locality.getLatitude1() != null ? locality.getLatitude1().toString() : "");
            longitude.setText(locality.getLongitude1() != null ? locality.getLongitude1().toString() : "");
        } else
        {
            latitude.setText("");
            longitude.setText("");

        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        BigDecimal newLat = null;
        BigDecimal newLon = null;
        
        String latText = latitude.getText();
        String lonText = longitude.getText();
        
        if (StringUtils.isNotEmpty(latText) && StringUtils.isNotEmpty(lonText))
        {
            
            try
            {
                newLat = new BigDecimal(latText);
                newLon = new BigDecimal(lonText);
            }
            catch (NumberFormatException nfe)
            {
                log.error("Could not parse georeference string as a decimal number", nfe);
                newLat = null;
                newLon = null;
            }
            
            log.debug("latitude:  " + newLat);
            log.debug("longitude: " + newLon);
            
            if (newLat != null && newLon != null)
            {
                locality.setLatitude1(newLat);
                locality.setLongitude1(newLon);
            }
        }
        
        return locality;
    }


    /* (non-Javadoc)
	 * @see edu.ku.brc.ui.UIPluginable#setCellName(java.lang.String)
	 */
	public void setCellName(String cellName) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.UIPluginable#setChangeListener(javax.swing.event.ChangeListener)
	 */
	public void setChangeListener(ChangeListener listener) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.UIPluginable#setIsDisplayOnly(boolean)
	 */
	public void setIsDisplayOnly(boolean isDisplayOnly) {
		// TODO Auto-generated method stub
		
    } 
    
    //--------------------------------------------------------
    // PropertyChangeListener
    //--------------------------------------------------------

    public void propertyChange(PropertyChangeEvent evt)
    {
        frame = null;
    }

    //--------------------------------------------------------
    // MapperListener
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.extras.BioGeoMancerMapper.MapperListener#mapReceived(javax.swing.Icon)
     */
    public void mapReceived(Icon map)
    {
        label.setIcon(map);
        label.invalidate();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.extras.BioGeoMancerMapper.MapperListener#exceptionOccurred(java.lang.Exception)
     */
    public void exceptionOccurred(Exception e)
    {
        label.setIcon(null);
    }
}
