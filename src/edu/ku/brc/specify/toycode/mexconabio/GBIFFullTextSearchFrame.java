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
 */
/**
 * 
 */
package edu.ku.brc.specify.toycode.mexconabio;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;
import com.jgoodies.looks.plastic.theme.SkyKrupp;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBInfoBase;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.SpecifyDataObjFieldFormatMgr;
import edu.ku.brc.specify.config.SpecifyWebLinkMgr;
import edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModel;
import edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModelRowInfo;
import edu.ku.brc.specify.tools.schemalocale.SchemaLocalizerFrame;
import edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 17, 2010
 *
 */
public class GBIFFullTextSearchFrame extends CustomDialog
{
    private Connection        dbConn = null;
    private JTextField        searchField;
    private JTable            table;
    private DataObjTableModel model;
    private JLabel            status;        
    
    // Lucene
    protected File         INDEX_DIR = new File("index-gbif2");
    protected IndexReader  reader;
    protected Searcher     searcher;
    protected Analyzer     analyzer;
    
    /**
     * @param title
     * @param contentPanel
     * @throws HeadlessException
     */
    public GBIFFullTextSearchFrame() throws HeadlessException
    {
        super((Frame)null, "GBIF Search", true, CustomDialog.CANCEL_BTN, null);
        
        analyzer = new StandardAnalyzer(Version.LUCENE_36);
        try
        {
            reader   = IndexReader.open(FSDirectory.open(INDEX_DIR), true);
            searcher = new IndexSearcher(reader);
            
        } catch (CorruptIndexException e)
        {
            e.printStackTrace();
            
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public void createDBConnection(final String server, 
                                   final String port, 
                                   final String dbName, 
                                   final String username, 
                                   final String pwd)
    {
        String connStr = "jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8&autoReconnect=true";
        try
        {
            dbConn = DriverManager.getConnection(String.format(connStr, server, port, dbName), username, pwd);
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomFrame#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        // 10029 AND Ipomoea
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout("p,10px,f:p:g,2px,p", "p,10px,f:p:g,4px,p")); //$NON-NLS-1$ //$NON-NLS-2$
        CellConstraints cc      = new CellConstraints();

        searchField = new JTextField();
        builder.add(UIHelper.createFormLabel("Search"), cc.xy(1,1));
        builder.add(searchField, cc.xy(3,1));
        
        JButton searchBtn = UIHelper.createButton("Search");
        builder.add(searchBtn, cc.xy(5,1));
        
        table = new JTable();
        builder.add(UIHelper.createScrollPane(table, true), cc.xyw(1, 3, 5));
        
        status = UIHelper.createLabel("");
        builder.add(status, cc.xyw(1, 5, 5));
        
        builder.setDefaultDialogBorder();
        
        contentPanel = builder.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        pack();
        
        searchField.addKeyListener(new KeyAdapter()
        {

            /* (non-Javadoc)
             * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
             */
            @Override
            public void keyPressed(KeyEvent e)
            {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    doSearch(searchField.getText());
                }
            }
            
        });
        
        searchField.setText("10029 AND Ipomoea");
        
        searchBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doSearch(searchField.getText());
            }
        });
    }
    
    private void doSearch(final String serachStr)
    {
        long startTime = System.currentTimeMillis();
        Query q;
        try
        {
            q = new QueryParser(Version.LUCENE_36, "contents", analyzer).parse(serachStr);
            int hitsPerPage = 1000;
            
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            
            if (hits.length > 0)
            {
                ArrayList<String> ids = new ArrayList<String>(hits.length);
                
                System.out.println("Found " + hits.length + " hits.");
                for(int i=0;i<hits.length;++i) 
                {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    System.out.println((i + 1) + ". " + d.get("id"));
                    
                    String id = d.get("id");
                    ids.add(id);
                }
                createAndFillModels(ids);
            } else
            {
                TableModel mdl = table.getModel();
                if (mdl instanceof DataObjTableModel)
                {
                    ((DataObjTableModel)mdl).clear();
                }
            }
            System.out.println(String.format("Time: %8.2f", (System.currentTimeMillis() - startTime) / 1000.0));
            
            String msg = String.format("Found %d items in %8.2f", hits.length, (System.currentTimeMillis() - startTime) / 1000.0);
            status.setText(msg);
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.cleanuptools.BaseCleanup#createAndFillModels()
     */
    protected void createAndFillModels(final List<String> ids)
    {
        
        StringBuilder sb = new StringBuilder();
        for (String id : ids)
        {
            if (sb.length() > 0) sb.append(',');
            sb.append(id);
        }
        
        final String inClause = sb.toString();
        
        final String[] colNames = {"Id", "Collector Number", "Institution Code", "Collection Code", "Catalog Number", 
                                    "Scientific Name", "Author", "Genus", "Species", 
                                    "Subspecies", "Latitude", "Longitude", "Lat Long Prec", 
                                    "Max altitude", "Min altitude", "Alt Precision", "Min Depth", 
                                    "Max Depth", "Depth Precision", "Continent Ocean", "Country", 
                                    "State", "County", "Collector Name", "Locality", 
                                    "Year", "Month", "Day"};
                            
        final Class<?> dataClasses[] = {Integer.class, String.class, String.class, String.class, 
                                        String.class, String.class, String.class, String.class, 
                                        String.class, String.class, String.class, String.class, 
                                        String.class, String.class, String.class, String.class, 
                                        String.class, String.class, String.class, String.class, 
                                        String.class, String.class, String.class, String.class, 
                                        String.class, String.class, String.class, String.class};

        
        model = new DataObjTableModel(dbConn, 100, null, false)
        {
            /* (non-Javadoc)
             * @see edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModel#buildSQL()
             */
            @Override
            protected String buildSQL()
            {
                String gSQL = String.format("SELECT id, collector_num, institution_code, collection_code, " +
                              "catalogue_number, scientific_name, author, genus, species, subspecies, latitude, longitude,  " +
                              "lat_long_precision, max_altitude, min_altitude, altitude_precision, min_depth, max_depth, depth_precision, " +
                              "continent_ocean, country, state_province, county, collector_name, " + 
                              "locality, year, month, day FROM raw WHERE id in (%s)", inClause);
                            
                tableInfo = new DBTableInfo(100, this.getClass().getName(), "raw", "id", "r");
                
                for (int i=0;i<colNames.length;i++)
                {
                    DBFieldInfo fi = new DBFieldInfo(tableInfo, colNames[i], dataClasses[i]);
                    fi.setTitle(colNames[i]);
                    colDefItems.add(fi);
                }
                numColumns = colNames.length;
                
                return gSQL;
            }

            /* (non-Javadoc)
             * @see edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModel#addAdditionalRows(java.util.ArrayList)
             */
            @Override
            protected void addAdditionalRows(final ArrayList<DBInfoBase> colDefItemsArg,
                                             final ArrayList<DataObjTableModelRowInfo> rowInfoListArg)
            {
            }
            
        };
        table.setModel(model);
        DefaultTableCellRenderer tcr = getTableCellRenderer(model.getRowInfoList());
        for (int i=0;i<model.getColumnCount();i++)
        {
            if (model.getColumnClass(i) != Boolean.class)
            {
                table.setDefaultRenderer(model.getColumnClass(i), tcr);
            }
        }
    }
    
    protected DefaultTableCellRenderer getTableCellRenderer(final List<DataObjTableModelRowInfo> rowInfoList)
    {
        final Color sameColor = new Color(0,128,0);
        
        return new DefaultTableCellRenderer() {
            @SuppressWarnings("unchecked")
            @Override
            public Component getTableCellRendererComponent(JTable tableArg,
                                                           Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus,
                                                           int row,
                                                           int column)
            {
                DataObjTableModelRowInfo rowInfo = rowInfoList.get(row);
                
                boolean doCenter = false;
                Object  val      = value;
                if (value instanceof Pair<?, ?>)
                {
                    Pair<Object, Object> pair = (Pair<Object, Object>)value;
                    val      = pair.first;
                    doCenter = true;
                }
                
                JLabel lbl = (JLabel)super.getTableCellRendererComponent(tableArg, val, isSelected, hasFocus, row, column);
                if (rowInfo.isMainRecord())
                {
                    lbl.setForeground(Color.WHITE);
                    lbl.setBackground(Color.BLUE.brighter().brighter());
                    
                } else
                {
                    lbl.setForeground(model.isSame(column) ? sameColor : Color.BLACK);
                    lbl.setBackground(Color.WHITE);
                }
                lbl.setHorizontalTextPosition(doCenter ? SwingConstants.CENTER : SwingConstants.LEFT);
                
                return lbl;
            }
        };
    }
    
    public void cleanup()
    {
        
        try
        {
            if (dbConn != null)
            {
                dbConn.close();
            }
            if (searcher != null)
            {
                searcher.close();
            }
            if (reader != null)
            {
                reader.close();
            }
            if (analyzer != null)
            {
                analyzer.close();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                
//              Set App Name, MUST be done very first thing!
                UIRegistry.setAppName("Specify");  //$NON-NLS-1$
                
                // Then set this
                IconManager.setApplicationClass(Specify.class);
                IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml")); //$NON-NLS-1$
                IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml")); //$NON-NLS-1$
                IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml")); //$NON-NLS-1$
                
                try
                {
                    UIHelper.OSTYPE osType = UIHelper.getOSType();
                    if (osType == UIHelper.OSTYPE.Windows )
                    {
                        //UIManager.setLookAndFeel(new WindowsLookAndFeel());
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        
                    } else if (osType == UIHelper.OSTYPE.Linux )
                    {
                        //UIManager.setLookAndFeel(new GTKLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
                        //PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
                        //PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        //PlasticLookAndFeel.setPlasticTheme(new ExperienceRoyale());
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                    }
                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerFrame.class, e);
                    e.printStackTrace();
                }
                
                System.setProperty(AppContextMgr.factoryName,          "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr //$NON-NLS-1$
                System.setProperty(SchemaI18NService.factoryName,      "edu.ku.brc.specify.config.SpecifySchemaI18NService");  // Needed for Localization and Schema //$NON-NLS-1$
                System.setProperty(UIFieldFormatterMgr.factoryName,    "edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr");    // Needed for CatalogNumbering //$NON-NLS-1$
                System.setProperty(WebLinkMgr.factoryName,             "edu.ku.brc.specify.config.SpecifyWebLinkMgr");         // Needed for WebLnkButton //$NON-NLS-1$
                System.setProperty(DataObjFieldFormatMgr.factoryName,   "edu.ku.brc.specify.config.SpecifyDataObjFieldFormatMgr");     // Needed for WebLnkButton //$NON-NLS-1$

                SpecifyDataObjFieldFormatMgr.setDoingLocal(true);
                SpecifyUIFieldFormatterMgr.setDoingLocal(true);
                SpecifyWebLinkMgr.setDoingLocal(true);
                
                GBIFFullTextSearchFrame dlg = new GBIFFullTextSearchFrame();
                dlg.createDBConnection("localhost", "3306", "gbif", "root", "root");
                dlg.setCancelLabel("Close");
                dlg.setSize(900,700);
                UIHelper.centerAndShow(dlg);
                dlg.cleanup();
                System.exit(0);
                
            }
        });

    }
    
}
