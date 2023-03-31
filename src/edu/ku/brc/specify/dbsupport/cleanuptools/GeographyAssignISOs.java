/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import static edu.ku.brc.specify.conversion.BasicSQLUtils.getCountAsInt;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.queryForInts;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.queryForRow;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.querySingleObj;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.update;
import static edu.ku.brc.ui.UIHelper.centerAndShow;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.createFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createList;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIRegistry.getAppDataDir;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.showError;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.remove;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.Version;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;

/**
 * Can't use PrepareStatment because of MySQL boolean bit issue.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Aug 16, 2009
 *
 */
public class GeographyAssignISOs
{
    private static final Logger  log = Logger.getLogger(GeographyAssignISOs.class);
    private final static String GEONAME_SQL                  = "SELECT Name FROM geography WHERE GeographyID = ";  
    private final static String GEONAME_LOOKUP_CONTINENT_SQL = "SELECT geonameid, ISOCode FROM geoname WHERE LOWER(asciiname) = ?";  
    private final static String GEONAME_LOOKUP_COUNTRY_SQL   = "SELECT geonameid, iso_alpha2 FROM countryinfo WHERE LOWER(name) = ?";  
    private final static String GEONAME_LOOKUP_STATE_SQL     = "SELECT geonameid, ISOCode FROM geoname WHERE fcode = 'ADM1' AND LOWER(asciiname) = ? AND country = ?";  
    private final static String GEONAME_LOOKUP_COUNTY_SQL    = "SELECT geonameid, ISOCode FROM geoname WHERE fcode = 'ADM2' AND (LOWER(asciiname) = ? OR LOWER(asciiname) = ?) AND country = ? AND admin1 = ?";  
    public  final static String GEONAMES_INDEX_DATE_PREF     = "GEONAMES_INDEX_DATE_PREF";
    public  final static String GEONAMES_INDEX_NUMDOCS       = "GEONAMES_INDEX_NUMDOCS";
    
    protected enum LuceneSearchResultsType {eNotFound, eFound, eMatch}
    
    protected enum ProcessingState {eInitialPass, eAllCountriesPass, eStartIndvCountryPass, eIndvCountryPass, eProcessingDone}


    //private GeographyTreeDef           geoDef;
    private Agent                      createdByAgent;
    private ProgressFrame              frame;
    
    private Connection                 readConn = null;
    private Connection                 updateConn;
    
    private PreparedStatement          lookupContinentStmt = null;
    private PreparedStatement          lookupCountryStmt   = null;
    private PreparedStatement          lookupStateStmt     = null;
    private PreparedStatement          lookupCountyStmt    = null;
    
    private int                        geographyTotal;
    private int                        totalUpdated;
    private int                        totalMerged;
    private int                        processedCount;
    private boolean                    blockStatsUpdates = false;
    
    // Auto processing of ISOCodes
    private ProcessingState            processingPhase = ProcessingState.eInitialPass;
    private Vector<Integer>            usaIds          = new Vector<Integer>();
    private int                        earthId         = 0;
    
    //-------------------------------------------------
    // UI
    //-------------------------------------------------
    private JCheckBox                   continentsCBX;
    private JCheckBox                   countriesCBX;
    private JCheckBox                   stateCBX;
    private JCheckBox                   countiesCBX;
    
    private JLabel                      spCountriesLbl;
    private JComboBox<?>                spCountriesCmbx;
    private JCheckBox                   spStatesCBX;
    private JCheckBox                   spCountiesCBX;


    private JRadioButton               allCountriesRB;
    private JRadioButton               singleCountryRB;
    private ButtonGroup                btnGroup;
    
    private JLabel                     countriesTotalLabel;
    private JLabel                     statesTotalLabel;
    
    
    private TableWriter                tblWriter        = null;
    private boolean                    doStopProcessing = false;
    private boolean                    doSkipCountry    = false;
    private TreeSet<String>            blankGeoNameParents = new TreeSet<String>();
    
    private StateCountryContXRef stCntXRef;
    
    private GeoCleanupFuzzySearch luceneSearch;
    
    // For Processing User's Geo Tree
    private Integer[] keys   = {100,         200,       300,     400};
    private String[]  values = {"continent", "country", "state", "county"};
    private HashMap<Integer, String> rankToNameMap = new HashMap<Integer, String>();
    
    // Fix Geo UI
    private boolean doUpdateName = false;
    private boolean doMerge      = false;
    private boolean doAddISOCode = true;
    private String  isoCodeStr   = "";
    private Integer mergeToGeoId = null;
    
    private GeoSearchResultsItem selectedSearchItem = null;
    
    private boolean[] doAllCountries;
    private boolean[] doInvCountry;
    private Integer   doIndvCountryId = null;
    
    private Vector<Integer>              countryIds    = new Vector<Integer>();
    private Vector<GeoSearchResultsItem> countryInfo   = new Vector<GeoSearchResultsItem>();
    private Vector<GeoSearchResultsItem> luceneResults = new Vector<GeoSearchResultsItem>();
    private Vector<GeoSearchResultsItem> badRankIDs    = new Vector<GeoSearchResultsItem>();
    
    private String    fullWriterPath;
    
    //private Integer foundGeonameId = null;

    
    /**
     * Constructor.
     * @param geoDef
     * @param nowStr
     * @param createdByAgent
     * @param itUsername
     * @param itPassword
     */
    public GeographyAssignISOs(final GeographyTreeDef   geoDef, 
                               final Agent              createdByAgent)
    {
        super();
        //this.geoDef            = geoDef;
        this.createdByAgent    = createdByAgent;
        
        for (int i=0;i<keys.length;i++)
        {
            rankToNameMap.put(keys[i], values[i]);
        }

        luceneSearch   = new GeoCleanupFuzzySearch(geoDef);
        fullWriterPath = getAppDataDir() + File.separator + "geo_report.html";
    }
    
    /**
     * 
     */
    private void connectToDB()
    {
        DBConnection currDBConn = DBConnection.getInstance();
        if (updateConn == null) updateConn = currDBConn.createConnection();
        if (readConn == null) readConn = currDBConn.createConnection();
        
        try
        {
            if (lookupContinentStmt == null && readConn != null)
            {
            	lookupContinentStmt = readConn.prepareStatement(GEONAME_LOOKUP_CONTINENT_SQL);
            }
            if (lookupCountryStmt == null && readConn != null)
            {
                lookupCountryStmt = readConn.prepareStatement(GEONAME_LOOKUP_COUNTRY_SQL);
            }
            if (lookupStateStmt == null && readConn != null)
            {
                lookupStateStmt = readConn.prepareStatement(GEONAME_LOOKUP_STATE_SQL);
            }
            if (lookupCountyStmt == null && readConn != null)
            {
                lookupCountyStmt = readConn.prepareStatement(GEONAME_LOOKUP_COUNTY_SQL);
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
    
    private int getUnitedStatesIndex(final Object[] titles)
    {
        Vector<Object> list = new Vector<Object>();
        Collections.addAll(list, titles);
        String[] names = {"United States", "USA", "U.S.A.", "United States of America"};
        for (String nm : names)
        {
            int index = list.indexOf(nm);
            if (index > -1)
            {
                return index;
            }
        }
        return -1;
    }

    /**
     * @return
     */
    @SuppressWarnings("rawtypes")
    public boolean buildAsync(final int earthID)
    {
        this.earthId = earthID;
        
        if (processingPhase == ProcessingState.eInitialPass)
        {
            processingPhase = ProcessingState.eAllCountriesPass;
            doAllCountries  = new boolean[] {true, false, false, false};
            doInvCountry    = new boolean[] {false, false, false, false};
            doIndvCountryId = null;
            
            // Check to see if it needs indexing.
            boolean shouldIndex = luceneSearch.shouldIndex();
            
            if (shouldIndex)
            {
                frame = new ProgressFrame("Building Geography Authority..."); // I18N
                frame.getCloseBtn().setVisible(false);
                frame.turnOffOverAll();
                frame.setDesc("Loading Geonames data..."); // I18N
                frame.pack();
                frame.setSize(450, frame.getBounds().height+10);
                UIHelper.centerAndShow(frame, 450, frame.getBounds().height+10);
    
                luceneSearch.startIndexingProcessAsync(earthId, frame, new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        frame.setVisible(false);
                        frame = null;
                        if (((Boolean)e.getSource()))
                        {
                            GeographyAssignISOs.this.startTraversal();
                        }
                    }
                });
                
            } else
            {
                String sql = "SELECT Name, geonameId, iso_alpha2 FROM countryinfo";
                for (Object[] row : query(sql))
                {
                    countryInfo.add(new GeoSearchResultsItem((String)row[0], (Integer)row[1], (String)row[2]));
                }
                startTraversal();
            }
        } else
        {
            if (processingPhase == ProcessingState.eStartIndvCountryPass)
            {
                String sql = "SELECT GeographyID FROM geography WHERE GeographyCode = 'US'";
                for (Integer recId : queryForInts(sql))
                {
                    usaIds.add(recId);
                }
                processingPhase = ProcessingState.eIndvCountryPass;
            }
            
            if (usaIds.size() > 0)
            {
                doAllCountries  = new boolean[] {true, false, false, false};
                doInvCountry    = new boolean[] {true, true, false, false};
                doIndvCountryId = usaIds.get(0);
                usaIds.remove(0);
                startTraversal();
            } else
            {
                processingPhase = ProcessingState.eProcessingDone;
                shutdown();
            }
        }

        return true;
    }
    
    /**
     * 
     */
    private void shutdown()
    {
        luceneSearch.doneSearching();
        
        tblWriter.close();
        
        if (tblWriter.hasLines())
        {
            try
            {
                AttachmentUtils.openFile(new File(fullWriterPath));
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        try
        {
            if (updateConn != DBConnection.getInstance()) updateConn.close();
            if (lookupCountryStmt != null) lookupCountryStmt.close();
            if (lookupStateStmt != null) lookupStateStmt.close();

        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        
        UIRegistry.clearSimpleGlassPaneMsg();
        String msg = totalUpdated == 0 ? "The selected geography records are up to date." : 
                                         String.format("Geography records updated: %d", totalUpdated);
//        if (doMerge)
//        {
//            msg += String.format("\nGeography records merged: %d", totalMerged);
//        }
       UIRegistry.writeTimedSimpleGlassPaneMsg(msg, 4000, true);

    }

    /**
     * @return
     */
    @SuppressWarnings("rawtypes")
    public boolean buildAsyncOrig(final int earthId)
    {
        String sql = adjustSQL("SELECT COUNT(*) FROM geography WHERE GeographyCode IS NOT NULL AND RankID = 100 AND GeographyTreeDefID = GEOTREEDEFID");
        int numContinentsWithNames = BasicSQLUtils.getCountAsInt(sql);
        
        continentsCBX = createCheckBox("All Continents"); // I18N

        CellConstraints cc  = new CellConstraints();
        PanelBuilder    pb1 = new PanelBuilder(new FormLayout("f:p:g", "p,4px,p,4px,p,8px"));
        countriesCBX  = createCheckBox("All Countries");
        stateCBX      = createCheckBox("All States");
        countiesCBX   = createCheckBox("All Counties");
        pb1.add(countriesCBX,  cc.xy(1, 1));
        pb1.add(stateCBX,      cc.xy(1, 3));
        //pb1.add(countiesCBX,   cc.xy(1, 5));
        
        allCountriesRB  = new JRadioButton("Choose the Geography level to be processed"); //L18N
        singleCountryRB = new JRadioButton("Choose an individual Country");
        btnGroup = new ButtonGroup();
        btnGroup.add(this.allCountriesRB);
        btnGroup.add(this.singleCountryRB);
        
        if (numContinentsWithNames == 0)
        {
            continentsCBX.setEnabled(false);
            continentsCBX.setSelected(true);
        }
        
        countriesCBX.setEnabled(true);
        stateCBX.setEnabled(false);
        countiesCBX.setEnabled(false);
        
        countryIds.clear();
        sql = "SELECT g.GeographyID, g.Name, g2.Name FROM geography g LEFT JOIN geography g2 ON g.ParentID = g2.GeographyID " +
        	  "WHERE g.Name IS NOT NULL && LENGTH(g.Name) > 0 AND g.RankID = 200 AND g.GeographyTreeDefID = GEOTREEDEFID ORDER BY g.Name";
        sql = adjustSQL(sql);
        
        Vector<Object[]> rows   = query(sql);
        Object[]         titles = new Object[rows.size()+1];
        int i = 0;
        titles[i++] = "None"; // I18N
        countryIds.add(-1);
        for (Object[] r : rows)
        {
            countryIds.add((Integer)r[0]);
            String countryStr = (String)r[1];
            String contStr    = (String)r[2];
            titles[i++] = countryStr != null ? (countryStr + " (" + contStr + ")") : countryStr;
        }
        
        
        PanelBuilder    pb2 = new PanelBuilder(new FormLayout("8px,p,2px,f:p:g", "p,4px,p,8px"));
        spCountriesLbl  = createFormLabel("Country");          // I18N
        spCountriesCmbx = createComboBox(titles);
        spStatesCBX     = createCheckBox("States (Required)"); // I18N
        spCountiesCBX   = createCheckBox("Counties");          // I18N
        
        pb2.add(spCountriesLbl, cc.xy(2, 1));
        pb2.add(spCountriesCmbx, cc.xy(4, 1));
        pb2.add(spStatesCBX,     cc.xyw(1, 3, 4));
        //pb2.add(spCountiesCBX,   cc.xyw(1, 5, 4));
        
        spCountriesCmbx.setSelectedIndex(0);
        
        spStatesCBX.setSelected(true);
        spStatesCBX.setEnabled(false);
        spCountiesCBX.setEnabled(false);

        String          rowDef = createDuplicateJGoodiesDef("p", "4px", 8);
        PanelBuilder    pb     = new PanelBuilder(new FormLayout("16px,f:p:g", rowDef));
        
        pb.addSeparator("Continents to be processed", cc.xyw(1, 1, 2));
        pb.add(continentsCBX, cc.xyw(1, 3, 2));
        
        pb.addSeparator("Countries to be processed", cc.xyw(1, 5, 2));
        pb.add(allCountriesRB, cc.xyw(1, 7, 2));
        pb.add(pb1.getPanel(), cc.xyw(2, 9, 1));
        
        pb.add(singleCountryRB, cc.xyw(1, 11, 2));
        pb.add(pb2.getPanel(),  cc.xyw(2, 13, 1));
        
        pb.add(createGeoStatsPanel(), cc.xyw(1, 15, 2));
        
        pb.setDefaultDialogBorder();
        final CustomDialog dlg = new CustomDialog((Frame)getTopWindow(), "ISO Code Processing", 
                                                  true, CustomDialog.OKCANCELHELP, pb.getPanel()); // I18N
        dlg.setHelpContext("GeoCleanUpLevelChooser");
       
        // Setup actions
        ChangeListener rbChangeListener = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                radioSelected(dlg);
            }
        };
        allCountriesRB.addChangeListener(rbChangeListener);
        singleCountryRB.addChangeListener(null);
        
        countriesCBX.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                boolean isSel = countriesCBX.isSelected();
                stateCBX.setEnabled(isSel);
                countiesCBX.setEnabled(false);
                if (!isSel)
                {
                    stateCBX.setSelected(false);
                    countiesCBX.setSelected(false);
                }
                calcGeoStats();
                dlg.getOkBtn().setEnabled(isSel || spCountriesCmbx.getSelectedIndex() > 0);
            }
        });
        
        stateCBX.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                countiesCBX.setEnabled(stateCBX.isSelected());
                if (!stateCBX.isSelected())
                {
                    countiesCBX.setSelected(false);
                }
                calcGeoStats();
            }
        });
        
        // Special
        spCountriesCmbx.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                boolean isSel = spCountriesCmbx.getSelectedIndex() > 0;
                spStatesCBX.setSelected(isSel);
                spCountiesCBX.setEnabled(isSel);
                if (!isSel)
                {
                    spStatesCBX.setSelected(false);
                    spCountiesCBX.setSelected(false);
                }
                calcGeoStats();
                dlg.getOkBtn().setEnabled(isSel || countriesCBX.isSelected());

            }
        });
        
        spStatesCBX.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                spCountiesCBX.setEnabled(stateCBX.isSelected());
                calcGeoStats();
            }
        });
        
        allCountriesRB.setSelected(true);
        
        dlg.createUI();
        dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // Must be called after 'createUI'
        dlg.getOkBtn().setEnabled(false);
        
        // AUTO Don't show Dialog because it is automatically setting what to do 
        centerAndShow(dlg);
        
        if (dlg.isCancelled())
        {
            return false;
        }
        
        connectToDB();
        
        if (true) // AUTO 
        {
            doAllCountries  = new boolean[] {countriesCBX.isSelected(), stateCBX.isSelected(), countiesCBX.isSelected(), false};
            doInvCountry    = new boolean[] {spCountriesCmbx.getSelectedIndex() > 0, spStatesCBX.isSelected(), spCountiesCBX.isSelected(), false};
            doIndvCountryId = doInvCountry[0] ? countryIds.get(spCountriesCmbx.getSelectedIndex()) : null;
        } else
        {
            int indexOfUSA = getUnitedStatesIndex(titles);
            if (indexOfUSA == -1)
            {
                Vector<Object> nameList = new Vector<Object>();
                Collections.addAll(nameList, titles);
                JList list = createList(nameList);
                
                JScrollPane sp = createScrollPane(list);
                pb = new PanelBuilder(new FormLayout("f:p:g", "p,8px,f:p:g"));
                pb.add(createLabel("Select the United States"), cc.xy(1, 1));
                pb.add(sp, cc.xy(1, 3));
                pb.setDefaultDialogBorder();
                final CustomDialog askDlg = new CustomDialog((Frame)getTopWindow(), "Choose", 
                                                          true, CustomDialog.OKCANCELHELP, pb.getPanel()); // I18N
                dlg.setHelpContext("GeoCleanUpLevelChooser");
                centerAndShow(askDlg);
                if (!askDlg.isCancelled())
                {
                    indexOfUSA = list.getSelectedIndex();
                }
            }
            
            doAllCountries  = new boolean[] {true, false, false, false};
            doInvCountry    = new boolean[] {indexOfUSA > -1, true, false, false};
            doIndvCountryId = doInvCountry[0] ? countryIds.get(indexOfUSA) : null;
        }
        
        
        // Check to see if it needs indexing.
        boolean shouldIndex = luceneSearch.shouldIndex();
        
        if (shouldIndex)
        {
            frame = new ProgressFrame("Building Geography Authority..."); // I18N
            frame.getCloseBtn().setVisible(false);
            frame.turnOffOverAll();
            frame.setDesc("Loading Geonames data..."); // I18N
            frame.pack();
            frame.setSize(450, frame.getBounds().height+10);
            UIHelper.centerAndShow(frame, 450, frame.getBounds().height+10);

            luceneSearch.startIndexingProcessAsync(earthId, frame, new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent e)
                {
                    frame.setVisible(false);
                    frame = null;
                    if (((Boolean)e.getSource()))
                    {
                        GeographyAssignISOs.this.startTraversal();
                    }
                }
            });
            
        } else
        {
            sql = "SELECT Name, geonameId, iso_alpha2 FROM countryinfo";
            for (Object[] row : query(sql))
            {
                countryInfo.add(new GeoSearchResultsItem((String)row[0], (Integer)row[1], (String)row[2]));
            }
            startTraversal();
        }
        
        return true;
    }
    
    private JPanel createGeoStatsPanel()
    {
        CellConstraints cc  = new CellConstraints();

        PanelBuilder pb = new PanelBuilder(new FormLayout("p,2px,p,f:p:g,", "p,8px,p,4px,p"));
        countriesTotalLabel = UIHelper.createLabel("");
        statesTotalLabel  = UIHelper.createLabel("");
        
        pb.addSeparator("Unassigned Geography Counts", cc.xyw(1,1,4)); // I18N
        pb.add(createFormLabel("Countries"), cc.xy(1, 3));
        pb.add(createFormLabel("States"),    cc.xy(1, 5));
        pb.add(countriesTotalLabel, cc.xy(3, 3));
        pb.add(statesTotalLabel,    cc.xy(3, 5));
        return pb.getPanel();
    }
    
    private void calcGeoStats()
    {
        synchronized (this)
        {
            if (blockStatsUpdates) return;
            
            if (allCountriesRB.isSelected())
            {
                if (!countriesCBX.isSelected())
                {
                    countriesTotalLabel.setText("");
                    statesTotalLabel.setText("");
                    return;
                }
                if (!stateCBX.isSelected())
                {
                    statesTotalLabel.setText("");
                }
            } else {
                if (spCountriesCmbx.getSelectedIndex() < 1)
                {
                    countriesTotalLabel.setText("");
                    statesTotalLabel.setText("");
                    return;
                }
                if (!spStatesCBX.isSelected())
                {
                    statesTotalLabel.setText("");
                }
            }
            
            SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
            {
                Integer totalCountries = null;
                Integer totalStates    = null;
                
                @Override
                protected Object doInBackground() throws Exception
                {
                    boolean isAll  = allCountriesRB.isSelected();
                    int     selInx = spCountriesCmbx.getSelectedIndex();
                    
                    String base = "SELECT COUNT(*) FROM geography WHERE GeographyCode IS NULL";
                    
                    totalCountries = (!isAll && selInx < 1) ? null : getCountAsInt(base + " AND RankID = 200" + (isAll ? "" : " AND GeographyID = "+countryIds.get(selInx)));
                   
                    if ((isAll && stateCBX.isSelected()) || (!isAll && spStatesCBX.isSelected()))
                    {
                        String sql = base + " AND RankID = 300";
                        if (!isAll)
                        {
                            sql += " AND ParentID = " + countryIds.get(selInx);
                        }
                        totalStates = getCountAsInt(sql);
                    }
                    return null;
                }
    
                @Override
                protected void done()
                {
                    if (totalCountries != null)
                    {
                        countriesTotalLabel.setText(totalCountries.toString());
                    }
                    if (totalStates != null)
                    {
                        statesTotalLabel.setText(totalStates.toString());
                    }
                }
            };
            worker.execute();
        }
    }

    
    /**
     * @param sql
     * @return
     */
    private String adjustSQL(final String sql)
    {
        return QueryAdjusterForDomain.getInstance().adjustSQL(sql);
    }
    
    /**
     * @param dlg
     */
    private void radioSelected(final CustomDialog dlg)
    {
        blockStatsUpdates = true;
        
        boolean isAllCountries = this.allCountriesRB.isSelected();
       
        countriesCBX.setEnabled(isAllCountries);
        stateCBX.setEnabled(false);
        countiesCBX.setEnabled(false);
        
        countriesCBX.setSelected(false);
        stateCBX.setSelected(false);
        countiesCBX.setSelected(false);
        
        spCountriesLbl.setEnabled(!isAllCountries);
        spCountriesCmbx.setEnabled(!isAllCountries);
        spStatesCBX.setEnabled(false);
        spCountiesCBX.setEnabled(!isAllCountries && spCountriesCmbx.getSelectedIndex() > 0);
        
        spStatesCBX.setSelected(!isAllCountries && spCountriesCmbx.getSelectedIndex() > 0);
        spCountiesCBX.setSelected(!isAllCountries && spCountriesCmbx.getSelectedIndex() > 0);
        
        if (dlg.getOkBtn() != null)
        {
            dlg.getOkBtn().setEnabled(false);
        }
        
        blockStatsUpdates = false;
        calcGeoStats();
    }
    
    
    /**
     * @param desc
     */
//    private void setProgressDesc(final String desc)
//    {
//        if (frame != null)
//        {
//            SwingUtilities.invokeLater(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    frame.setDesc(desc);
//                }
//            });
//        }
//    }
    
  
    /**
     * @param theRankId
     * @param parentNames
     * @param parentRanks
     * @return
     */
    private String getParentNameWithRank(final int theRankId, final String[] parentNames, final int[] parentRanks)
    {
        int i = 0;
        while (parentRanks[i] > 0)
        {
            if (parentRanks[i] == theRankId)
            {
                return parentNames[i];
            }
            i++;
        }
        return null;
    }
    
    /**
     * @param level
     * @param rankId
     * @param parentNames
     * @param parentRanks
     * @param parentISOCodes
     * @return
     */
    private LuceneSearchResultsType searchLuceneWithFuzzy(final int       level,
                                                          final int       rankId,
                                                          final String[]  parentNames,
                                                          final int[]     parentRanks,
                                                          final String[]  parentISOCodes) throws IOException
    {
        luceneResults.removeAllElements();
        
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<level+1;i++)
        {
            if (i > 0) sb.append(' ');
            sb.append(parentNames[i]);
        }
        //log.debug("["+sb.toString()+"]");        //Query query = new FuzzyQuery(new Term("name", sb.toString()));

        String   isoCode   = null;
        Document doc       = null;
        HashSet<Integer>     usedIds   = new HashSet<Integer>();
        TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
        String searchStr = "";
        try
        {
            System.out.println("searchStr["+searchStr+"]");
            searchStr = GeoCleanupFuzzySearch.stripExtrasFromName(sb.toString());
            if (isEmpty(searchStr)) 
            {
                String parentName = level == 0 ? "Earth" : parentNames[level];
                blankGeoNameParents.add(parentName);
                return LuceneSearchResultsType.eNotFound;
            }
            System.out.println("searchStr["+searchStr+"]");
            Query q = new QueryParser(Version.LUCENE_47, "name", GeoCleanupFuzzySearch.getAnalyzer()).parse(searchStr);
            luceneSearch.getSearcher().search(q, collector);
        } catch (ParseException e)
        {
            e.printStackTrace();
            return LuceneSearchResultsType.eNotFound;
        }
        
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        for (int i=0;i<hits.length;++i) 
        {
            int docId     = hits[i].doc;
            doc           = luceneSearch.getSearcher().doc(docId);
            //System.out.println("Fuzzy: "+i+"  "+hits[i].score+"  ["+doc.get("name")+"][cntry: "+doc.get("country")+" st:"+doc.get("state")+" co:"+
            //                    doc.get("county")+"] rnk:"+doc.get("rankid")+" gnId: "+doc.get("geonmid"));
            int docRankId = Integer.parseInt(doc.get("rankid"));
            if (rankId == docRankId)
            {
                int    geoId    = Integer.parseInt(doc.get("geonmid"));
                String fullName = doc.get("name");
                isoCode         = doc.get("code");
                
                String country = doc.get("country");
                if (i == 0 && 
                    ((isNotEmpty(fullName) && fullName.equals(searchStr)) || 
                    ( rankId == 200 && isNotEmpty(country) && country.equals(searchStr))))
                {
                    selectedSearchItem = new GeoSearchResultsItem(fullName, geoId, isoCode);
                    return LuceneSearchResultsType.eMatch;
                }
                
                if (!usedIds.contains(geoId))
                {
                    usedIds.add(geoId);
                    
                    String state   = doc.get("state");
                    String county  = doc.get("county");
                    
                    if (isNotEmpty(country) || isNotEmpty(country) || isNotEmpty(country))
                    {
                        sb = new StringBuilder();
                        String[] names = {country, state, county};
                        for (String nm : names)
                        {
                            if (nm != null)
                            {
                                if (sb.length() > 0) sb.append(", ");
                                sb.append(nm);
                            }
                        }
                        fullName = sb.toString();
                    }
                    
                    luceneResults.add(new GeoSearchResultsItem(fullName, geoId, isoCode));
                }
            }
//            int docId     = hits[i].doc;
//            doc           = luceneSearch.getSearcher().doc(docId);
//            System.out.println("Fuzzy: "+i+"  "+hits[i].score+"  "+doc.get("name"));
        }
        
        if (rankId == 400 && !doInvCountry[2])
        {
            return LuceneSearchResultsType.eNotFound;
        }
        
        boolean hasItems = luceneResults.size() > 0;
        if (hasItems)
        {
            selectedSearchItem = luceneResults.get(0);
        }
        return hasItems ? LuceneSearchResultsType.eFound : LuceneSearchResultsType.eNotFound;
    }
                                          
    /**
     * @param level
     * @param rankId
     * @param parentNames
     * @param parentRanks
     * @param parentISOCodes
     * @return
     */
    private boolean searchGeonameForMatch(final int       level,
                                          final int       rankId,
                                          final String[]  parentNames,
                                          final int[]     parentRanks,
                                          final String[]  parentISOCodes) throws Exception
    {
        String  searchText = null;
        String  isoCode    = null;
        Integer geonameId  = null;
        
        PreparedStatement pStmt   = null;
        if (rankId == 100)
        {
            pStmt      = lookupContinentStmt;
            searchText = parentNames[0];

        } else if (rankId == 200)
        {
            pStmt      = lookupCountryStmt;
            searchText = getParentNameWithRank(200, parentNames, parentRanks);

        } else if (rankId == 300)
        {
            isoCode = parentISOCodes[level-1];
            if (isoCode == null)
            {
                String countryName = getParentNameWithRank(200, parentNames, parentRanks);
                lookupCountryStmt.setString(1, countryName.toLowerCase());
                ResultSet rs = lookupCountryStmt.executeQuery();
                if (rs.next())
                {
                    geonameId = rs.getInt(1);
                    isoCode   = rs.getString(2);
                }
                rs.close();
            }
            
            if (isNotEmpty(isoCode))
            {
                searchText = getParentNameWithRank(300, parentNames, parentRanks);
                pStmt      = lookupStateStmt;
                lookupStateStmt.setString(2, isoCode);
            }
        } else if (rankId == 400)
        {
            searchText = getParentNameWithRank(400, parentNames, parentRanks);
            if (setupCountyStmt(searchText, parentNames, parentRanks, lookupCountyStmt))
            {
                pStmt = lookupCountyStmt;
            }
        }
        
        if (isNotEmpty(searchText))
        {
            if (pStmt != null)
            {
                pStmt.setString(1, searchText.toLowerCase());
                ResultSet rs = pStmt.executeQuery();
                if (rs.next())
                {
                    geonameId = rs.getInt(1);
                    isoCode   = rs.getString(2);
                }
                rs.close();
            }
        } else
        {
            String parentName = level == 0 ? "Earth" : parentNames[level];
            blankGeoNameParents.add(parentName);
            return false;
        }
        
        if (geonameId == null && rankId == 300)
        {
            lookupCountryStmt.setString(1, searchText.toLowerCase());
            ResultSet rs = lookupCountryStmt.executeQuery();
            if (rs.next())
            {
                geonameId = rs.getInt(1);
                isoCode   = rs.getString(2);
            }
            rs.close();
        }

        boolean found = geonameId != null;
        if (found)
        {
            selectedSearchItem = new GeoSearchResultsItem(searchText, geonameId, isoCode);
        }
        return found;
    }
    
    /**
     * @param geoId
     * @param level
     * @param rankId
     * @param parentNames
     * @param parentRanks
     * @param parentISOCodes
     * @param isIndvCountry
     * @throws SQLException
     */
    private void findGeo(final int       geoId, 
                         final String    geoISOCode,
                         final int       level,
                         final int       rankId,
                         final String[]  parentNames,
                         final int[]     parentRanks,
                         final String[]  parentISOCodes,
                         final boolean   isIndvCountry) throws SQLException
    {
        String nbsp = "&nbsp;";
        
        processedCount++;

        // Check the database directly
        if (isEmpty(geoISOCode))
        {
            try
            {
                luceneResults.removeAllElements();
                
                boolean foundMatch = searchGeonameForMatch(level, rankId, parentNames, parentRanks, parentISOCodes); // will set selectedSearchItem
                if (!foundMatch)
                {
                    LuceneSearchResultsType resType = searchLuceneWithFuzzy(level, rankId, parentNames, parentRanks, parentISOCodes); // will set selectedSearchItem
    
                    if (resType != LuceneSearchResultsType.eMatch)
                    {
                        chooseGeo(geoId, parentNames[level], level, rankId, parentNames, parentRanks);
                        
                        if (doStopProcessing || (doSkipCountry && rankId > 199))
                        {
                            String oldName = querySingleObj(GEONAME_SQL + geoId);
                            tblWriter.log(parentNames[0], 
                                          parentNames[1] != null ? parentNames[1] : nbsp, 
                                          //parentNames[2] != null ? parentNames[2] : nbsp, // Counties 
                                          doStopProcessing || doSkipCountry ? nbsp : oldName, nbsp, nbsp, "Skipped");
                            if (rankId > 200)
                            {
                                doSkipCountry = false;
                                return;
                            }
                        }
                    }
        
                    if (selectedSearchItem != null)
                    {
                        this.doAddISOCode     = true;
                        parentISOCodes[level] = selectedSearchItem.isoCode;
                        updateGeography(geoId, selectedSearchItem.geonameId, selectedSearchItem.name, selectedSearchItem.isoCode, parentNames);
                    } else if (this.doAddISOCode)
                    {
                        parentISOCodes[level] = this.isoCodeStr;
                        updateGeography(geoId, -1, null, this.isoCodeStr, parentNames);
                    }
                } else
                {
                    this.doAddISOCode     = true;
                    parentISOCodes[level] = selectedSearchItem.isoCode;
                    updateGeography(geoId, selectedSearchItem.geonameId, selectedSearchItem.name, selectedSearchItem.isoCode, parentNames);
                }
               
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        
        boolean doDrillDown = doAllCountries[level+1] || (isIndvCountry && doInvCountry[level+1]);
        
        if (doDrillDown) // don't go down further than County level
        {
            //String    sql  = "SELECT GeographyID, Name, RankID FROM geography WHERE GeographyCode IS NULL AND ParentID = " + geoId + " ORDER BY RankID, Name";
            String    wStr  = rankId == 400 ? "GeographyCode IS NULL" : ""; 
            String    sql   = String.format("SELECT GeographyID, Name, RankID, GeographyCode FROM geography WHERE %s ParentID = %d ORDER BY RankID, Name", wStr, geoId);
            Statement stmt  = readConn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            while (rs.next())
            {
                String name = rs.getString(2); // Get Child's Name
                if (isNotEmpty(name))
                {
                    int    childGeoId  = rs.getInt(1);    // Get Child's Id
                    int    childRankId = rs.getInt(3);    // Get Child's RankID
                    String isoCode     = rs.getString(4); // Get Child's ISO Code
                    
                    for (int ii=level+1;ii<parentNames.length;ii++)
                    {
                        parentNames[ii] = null;
                        parentRanks[ii] = -1;
                    }
                    
                    parentNames[level+1] = name;
                    parentRanks[level+1] = childRankId;
                    findGeo(childGeoId, isoCode, level+1, childRankId, parentNames, parentRanks, parentISOCodes, isIndvCountry);
                    if (doStopProcessing)
                    {
                        return;
                    }
                    
                    if (doSkipCountry)
                    {
                        doSkipCountry = rankId > 199;
                        if (doSkipCountry) return;
                    }
                } else
                {
                    blankGeoNameParents.add(parentNames[level]);
                }
            }
            rs.close();
            stmt.close();
        }
    }
    
    private boolean setupCountyStmt(final String countyName,
                                    final String[] parentNames, 
                                    final int[] parentRanks, 
                                    final PreparedStatement pStmt) throws SQLException
    {
        String countryName = getParentNameWithRank(200, parentNames, parentRanks);
        if (isNotEmpty(countryName))
        {
            String countryCode = querySingleObj(String.format("SELECT iso_alpha2 FROM countryinfo WHERE LOWER(name) = '%s'", countryName.toLowerCase()));
            if (isNotEmpty(countryCode))
            {
                String stateName = getParentNameWithRank(300, parentNames, parentRanks);
                if (isNotEmpty(stateName))
                {
                    String stateCode = querySingleObj(String.format("SELECT ISOCode FROM geoname WHERE LOWER(country) = '%s' AND LOWER(asciiname) = '%s'", countryCode.toLowerCase(), stateName.toLowerCase()));
                    if (isNotEmpty(stateCode))
                    {
                        if (stateCode.length() == 4)
                        {
                            stateCode = stateCode.substring(2);
                        }
                        if (isNotEmpty(countyName))
                        {
                            String lwrCounty = countyName.toLowerCase();
                            pStmt.setString(2, lwrCounty.contains("county") ? lwrCounty : (lwrCounty + " county"));
                            pStmt.setString(3, countryCode);
                            pStmt.setString(4, stateCode);
                            return true;
                        }                        
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * @param geoId
     * @param geonameId
     * @param rankId
     */
    private void updateGeography(final int      geoId, 
                                 final int      geonameId,
                                 final String   newGeoName,
                                 final String   newISOCode,
                                 final String[] parentNames)
    {
        boolean autoCommit = true;
        
        String   oldName   = querySingleObj(GEONAME_SQL + geoId);
        String   sql       = "SELECT asciiname, ISOCode FROM geoname WHERE geonameId = " + geonameId;
        Object[] row       = geonameId != -1 ? queryForRow(sql) : null;
        
        if (row == null && !this.doAddISOCode)
        {
            return;
        }
        
        if (row != null || newISOCode != null)
        {
            String name    = this.doUpdateName || row == null ? newGeoName : (String)row[0];
            String isoCode = this.doAddISOCode && isNotEmpty(newISOCode) ? newISOCode : (String)row[1];
            
            PreparedStatement pStmt   = null;
            try
            {
                autoCommit = updateConn.getAutoCommit();
                String pre  = "UPDATE geography SET ";
                String post = ", ModifiedByAgentID=?, TimestampModified=? WHERE GeographyID=?";
                int inx     = 2; 
                if (this.doUpdateName && this.doAddISOCode)
                {
                    pStmt = updateConn.prepareStatement(pre+"Name=?, GeographyCode=?"+post);
                    pStmt.setString(1, name);
                    pStmt.setString(2, isoCode);
                    inx = 3;
                    
                } else if (this.doUpdateName)
                {
                    pStmt = updateConn.prepareStatement(pre+"Name=? "+post);
                    pStmt.setString(1, name.toLowerCase());
                    
                } else if (this.doAddISOCode)
                {
                    pStmt = updateConn.prepareStatement(pre+"GeographyCode=? "+post);
                    pStmt.setString(1, isoCode);
                }
                
                if (pStmt != null)
                {
                    pStmt.setInt(inx,  createdByAgent.getId());
                    pStmt.setTimestamp(inx+1, new Timestamp(Calendar.getInstance().getTime().getTime()));
                    pStmt.setInt(inx+2,  geoId);
        
                    boolean isOK = true;
                    int rv = pStmt.executeUpdate();
                    if (rv != 1)
                    {
                        log.error("Error updating "+name);
                        isOK = false;
                    } else
                    {
                        //areNodesChanged = true; // Global indication that at least one node was updated.
                        totalUpdated++;
                    }
                    
                    if (mergeToGeoId != null && doMerge)
                    {
                        sql = String .format("UPDATE locality SET GeographyID = %d WHERE GeographyID = %d", mergeToGeoId, geoId); 
                        if (update(sql) > 0)
                        {
                            sql = "DELETE FROM geography WHERE GeographyID = " + geoId;
                            if (update(sql) != 1)
                            {
                                log.error("Unable to delete geo id "+geoId);
                            } else
                            {
                                //areNodesChanged = true;
                                totalMerged++;
                            }
                        } else
                        {
                            log.error(String.format("Unable to update localities from geo id %d to %d ", geoId, mergeToGeoId));
                        }
                    }
        
                    if (!autoCommit)
                    {
                        updateConn.commit();
                    }
        
                    String nbsp = "&nbsp;";
                    tblWriter.log(parentNames[0] != null ? parentNames[0] : oldName, 
                                  parentNames[1] != null ? parentNames[1] : nbsp, 
                                  //parentNames[2] != null ? parentNames[2] : nbsp, // Counties 
                                  doUpdateName ? oldName : nbsp, 
                                  doUpdateName ? name : nbsp,
                                  doAddISOCode ? isoCode : nbsp, 
                                  isOK ? "Updated" : "Error Updating.");
                }
                
            } catch (SQLException e)
            {
                e.printStackTrace();
                
                if (!autoCommit)
                {
                    try
                    {
                        updateConn.rollback();
                    } catch (SQLException e1)
                    {
                        e1.printStackTrace();
                    }
                }
    
            } finally
            {
                // These need to be reset here, 
                // because this method is called sometimes automatically 
                doUpdateName       = false;
                doMerge            = false;
                doAddISOCode       = true;
                selectedSearchItem = null;
                
                try
                {
                    if (pStmt != null) pStmt.close();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        } else
        {
            log.error("Couldn't find record: "+sql);
        }
    }
    
    /**
     * @param geoId
     * @param nameStr
     * @param level
     * @param rankId
     * @param parentNames
     * @return
     * @throws SQLException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void chooseGeo(final int       geoId,
                           final String    nameStr,
                           final int       level,
                           final int       rankId,   // the RankID it is currently working on
                           final String[]  parentNames,
                           final int[]     parentRanks) throws SQLException
    {
        // Convert RankID to level
//        if (rankId > 100)
//        {
//            int levelFromRankId = (rankId / 100) - 2;
//            if (levelFromRankId != level)
//            {
//                badRankIDs.add(new GeoSearchResultsItem(nameStr, rankId, level * 100));
//            }
//        }
        
        Integer geonameId = selectedSearchItem != null ? selectedSearchItem.geonameId : null;
        GeoChooserDlg dlg = new GeoChooserDlg(nameStr, rankId, level, parentNames, parentRanks, geonameId, 
                                              stCntXRef, countryInfo, doAllCountries, doInvCountry, 
                                              readConn, processedCount, geographyTotal);
        if (luceneResults.size() > 0)
        {
            dlg.setCoInfoList(luceneResults); // this will force the dialog to use the results from Lucene
        }
        
        //int SKIP_BTN = CustomDialog.CANCEL_BTN;
        int SAVE_BTN = CustomDialog.OK_BTN;
        int NXTC_BTN = CustomDialog.CANCEL_BTN;
        int QUIT_BTN = CustomDialog.HELP_BTN;
        
        dlg.createUI();
        dlg.pack();
        centerAndShow(dlg);
        
        //dlg.dispose();
        this.doAddISOCode       = false;
        this.selectedSearchItem = null;

        if (dlg.getBtnPressed() != QUIT_BTN) 
        {
            if (dlg.getBtnPressed() == SAVE_BTN)
            {
                doUpdateName       = dlg.getUpdateNameCB().isSelected();
                doMerge            = dlg.getMergeCB() != null ? dlg.getMergeCB().isSelected() : false;
                doAddISOCode       = dlg.getAddISOCodeCB().isSelected();
                selectedSearchItem = dlg.getSelectedGeoSearchItem();
                
                this.isoCodeStr = dlg.getISOCodeFromTextField();
                if (this.isoCodeStr != null && selectedSearchItem != null && !this.isoCodeStr.equals(selectedSearchItem.isoCode))
                {
                    selectedSearchItem.isoCode = this.isoCodeStr;
                }

                String selectedGeoName = selectedSearchItem != null ? selectedSearchItem.name : null;
                if (doMerge && rankId == 400 && selectedGeoName != null)
                {
                    int geoParentId = getCountAsInt(readConn, "SELECT ParentID FROM geography WHERE GeographyID = "+ geoId);
                    if (geoParentId != -1)
                    {
                        try
                        {
                            String cName = remove(selectedGeoName, " County");
                            PreparedStatement pStmt = updateConn.prepareStatement("SELECT GeographyID FROM geography WHERE RankID = 400 AND GeographyID <> ? AND ParentID = ? AND (LOWER(Name) = ? OR LOWER(Name) = ?");
                            pStmt.setInt(1,    geoId);
                            pStmt.setInt(2,    geoParentId);
                            pStmt.setString(3, cName.toLowerCase());
                            pStmt.setString(4, selectedGeoName.toLowerCase());
                            ResultSet rs = pStmt.executeQuery();
                            
                            if (rs.first())
                            {
                                mergeToGeoId = rs.getInt(1);
                            }
                            rs.close();
                            pStmt.close();
                            
                        } catch (SQLException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
                parentNames[level] = selectedGeoName;
                parentRanks[level] = rankId;
                
                return;
            } 
            
            if (dlg.getBtnPressed() == NXTC_BTN)
            {
                doSkipCountry = true;
            }
        } else
        {
            doStopProcessing = true;
        }
    }
                              
    /**
     * 
     */
    private void startTraversal()
    {
        UIRegistry.writeSimpleGlassPaneMsg("Processing geography...", 24); // I18N

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                startTraversalInternal();
            }
        });
    }
    
    /**
     * 
     */
    private void startTraversalInternal()
    {
        log.debug("Phase: "+processingPhase+" Id:"+doIndvCountryId);
        connectToDB();

        if (stCntXRef == null)
        {
            stCntXRef = new StateCountryContXRef(readConn);
            boolean isOK = stCntXRef.build();
            if (!isOK)
            {
                showError("There was an error building the Geography cross-refernce.");
                return;
            }
        }
        
        if (!luceneSearch.initLuceneforReading())
        {
            showError("The geography index is missing!");
            return;
        }
        
        HashMap<String, String> countryMappings = new HashMap<String, String>();
        try
        {
            processedCount = 0;
            String[] parentNames    = new String[3];
            int[]    parentRanks    = new int[3];
            String[] parentISOCodes = new String[3];

            // Keep code here in case we switch back to using the dialog
            if (processingPhase == ProcessingState.eAllCountriesPass) // represents first time through
            {
                tblWriter       = new TableWriter(fullWriterPath, "Geography ISO Code Report");
                tblWriter.startTable();
                //String firstCol = continentsCBX.isSelected() ? "Continent / " : "";
                //tblWriter.logHdr(firstCol+"Country", "State", "County", "Old Name", "New Name", "ISO Code", "Action"); // for when we do counties
                tblWriter.logHdr("Continent / Country", "State", "Old Name", "New Name", "ISO Code", "Action");
    
                // KUFish - United States
                // Herps - United State 853, USA 1065
                // KUPlants 205
                
                totalUpdated   = 0;
                totalMerged    = 0;
                
                //------------------------------------------------------
                // Do Continents
                // Only do Continents on the first time through
                //------------------------------------------------------
                geographyTotal = getCountAsInt(readConn, adjustSQL("SELECT COUNT(*) FROM geography WHERE GeographyTreeDefID = GEOTREEDEFID AND GeographyCode IS NULL"));
                String sql     = adjustSQL("SELECT GeographyID, Name, RankID, GeographyCode FROM geography WHERE GeographyTreeDefID = GEOTREEDEFID AND GeographyCode IS NULL AND RankID = 100 ORDER BY Name ASC");
                
                for (Object[] row : query(readConn, sql))
                {
                    for (int i=0;i<parentNames.length;i++)
                    {
                        parentNames[i] = null;
                        parentRanks[i] = -1;
                        parentISOCodes[i] = null;
                    }
                    
                    String continentName = (String)row[1];
                    if (isNotEmpty(continentName))
                    {
                        int    geoId         = (Integer)row[0];
                        int    rankId        = (Integer)row[2];                
                        String isoCode       = (String)row[3];                
        
                        parentNames[0]    = continentName;
                        parentRanks[0]    = rankId;
                        parentISOCodes[0] = isoCode;
                        
                        findGeo(geoId, isoCode, 0, 100, parentNames, parentRanks, parentISOCodes, false);
                        if (doStopProcessing)
                        {
                            break;
                        }
                    } else
                    {
                        blankGeoNameParents.add("Earth");
                    }
                }
            }
            
            // Check to see this the user decided to quit
            // This code works for both all Counreies and an individual country
            if (!doStopProcessing)
            {
                //-------------------
                // Do Country
                //-------------------
                int countryCount = getCountAsInt(readConn, "SELECT COUNT(*) FROM geography WHERE GeographyCode IS NULL AND RankID = 200");
                int statesCount  = getCountAsInt(readConn, "SELECT COUNT(*) FROM geography g1 INNER JOIN geography g2 ON g1.GeographyID = g2.ParentID WHERE g1.RankID = 200 AND g2.GeographyCode IS NULL");
                geographyTotal   = countryCount + statesCount;
                
                String sql;
                sql  = "SELECT GeographyID, Name, RankID, GeographyCode FROM geography WHERE ";
                sql += doIndvCountryId != null ? "GeographyID = %d AND" : "";
                sql += " RankID = 200 ORDER BY Name ASC";
                
                if (doIndvCountryId != null)
                {
                    sql = String.format(sql,  doIndvCountryId);
                }
                
                for (Object[] row : query(readConn, sql))
                {
                    doSkipCountry = false;
                    
                    for (int i=0;i<parentNames.length;i++)
                    {
                        parentNames[i]    = null;
                        parentRanks[i]    = -1;
                        parentISOCodes[i] = null;
                    }
                    
                    int    geoId       = (Integer)row[0];
                    String countryName = (String)row[1];
                    //int    rankID      = (Integer)row[2]; 
                    String isoCode     = (String)row[3];
                    
                    countryName = countryMappings.get(countryName.toLowerCase());
                    if (countryName == null)
                    {
                        countryName = (String)row[1];
                    }
                    
                    boolean isIndvCountry = doIndvCountryId != null && doIndvCountryId == geoId;
                    //System.out.println(countryName+"  "+geoId+"   doInvCountry[0]: "+doInvCountry[0]+"  doIndvCountryId: "+doIndvCountryId+"  isIndvCountry: "+isIndvCountry);
                    if (doAllCountries[0] || (doInvCountry[0] && isIndvCountry))
                    {
                        //System.out.println(countryName);
                        if (isNotEmpty(countryName))
                        {
                            parentNames[0]    = countryName;
                            parentRanks[0]    = 200;
                            parentISOCodes[0] = isoCode;
                            findGeo(geoId, isoCode, 0, 200, parentNames, parentRanks, parentISOCodes, isIndvCountry);
                            if (doStopProcessing)
                            {
                                break;
                            }
                        }
                    }
                }
            }
            
            doIndvCountryId = null;

            if (doStopProcessing)
            {
                processingPhase = ProcessingState.eProcessingDone;
                
            } else if (processingPhase == ProcessingState.eAllCountriesPass)
            {
                processingPhase = ProcessingState.eStartIndvCountryPass;
                
            } else if (processingPhase == ProcessingState.eIndvCountryPass)
            {
                if (usaIds.size() == 0)
                {
                    processingPhase = ProcessingState.eProcessingDone;
                }
            }
            
            if (processingPhase != ProcessingState.eProcessingDone)
            {
                buildAsync(this.earthId);
                return;
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            if (processingPhase == ProcessingState.eProcessingDone)
            {
                tblWriter.endTable(); // end the current HTML table

                if (badRankIDs.size() > 0)
                {
                    tblWriter.println("<BR><BR><h2>Geography Records with incorrect RankIDs</h2>");
                    tblWriter.startTable();
                    tblWriter.logHdr("Geography Name", "Incorrect Rank", "Correct Rank");
                    for (GeoSearchResultsItem item : badRankIDs)
                    {
                        String goodLevel = rankToNameMap.get(item.goodRankId);
                        String badLevel  = rankToNameMap.get(item.currentRankId);
                        tblWriter.log(item.name, 
                                      goodLevel != null ? goodLevel : Integer.toString(item.goodRankId), 
                                      badLevel != null ? badLevel : Integer.toString(item.currentRankId));
                    }
                    tblWriter.endTable();
                }
                if (blankGeoNameParents.size() > 0)
                {
                    tblWriter.println("<BR><BR><h2>Geography Records that have children with a blank names.</h2>");
                    tblWriter.println("<p>The name may not be blank in the database but once all the special characters are removed it may become blank. For example, if the name had just a question mark.</p>");
                    tblWriter.startTable();
                    tblWriter.logHdr("Geography Parent Name");
                    for (String parentName : blankGeoNameParents)
                    {
                        tblWriter.logTDCls("", parentName);
                    }
                    tblWriter.endTable();
                }
                
                shutdown();
            }
        }
    }
}
