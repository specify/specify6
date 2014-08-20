/* Copyright (C) 2013, University of Kansas Center for Research
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

import static edu.ku.brc.specify.conversion.BasicSQLUtils.getCountAsInt;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.queryForRow;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.querySingleObj;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.update;
import static edu.ku.brc.ui.UIHelper.centerAndShow;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.createFormLabel;
import static edu.ku.brc.ui.UIRegistry.getAppDataDir;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.showError;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.dbsupport.BuildFromGeonames;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Triple;

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
    private final static String        GEONAME_SQL                  = "SELECT Name FROM geography WHERE GeographyID = ";  
    private final static String        GEONAME_LOOKUP_CONTINENT_SQL = "SELECT geonameid, ISOCode FROM geoname WHERE name = ?";  
    private final static String        GEONAME_LOOKUP_COUNTRY_SQL   = "SELECT geonameid, iso_alpha2 FROM countryinfo WHERE name = ?";  
    private final static String        GEONAME_LOOKUP_STATE_SQL     = "SELECT geonameid, ISOCode FROM geoname WHERE fcode = 'ADM1' AND name = ? AND country = ?";  
    public  final static String        GEONAMES_INDEX_DATE_PREF     = "GEONAMES_INDEX_DATE_PREF";
    public  final static String        GEONAMES_INDEX_NUMDOCS       = "GEONAMES_INDEX_NUMDOCS";

    private GeographyTreeDef           geoDef;
    private Agent                      createdByAgent;
    private ProgressFrame              frame;
    private boolean                    areNodesChanged = false;
    
    private Connection                 readConn = null;
    private Connection                 updateConn;
    
    private PreparedStatement          lookupContinentStmt = null;
    private PreparedStatement          lookupCountryStmt   = null;
    private PreparedStatement          lookupStateStmt     = null;
    
    private ArrayList<Object>          rowData = new ArrayList<Object>();
    
    private int                        countryTotal;
    private int                        countryCount;
    private int                        totalUpdated;
    private int                        totalMerged;
    
    
    //-------------------------------------------------
    // Lucene Members
    //-------------------------------------------------
    private TableWriter  tblWriter        = null;
    private boolean      doStopProcessing = false;
    private boolean      doSkipCountry    = false;
    private File         FILE_INDEX_DIR;
    
    private StateCountryContXRef stCntXRef;
    
    private IndexReader   reader;
    private IndexSearcher searcher;
    private Analyzer      analyzer;
    
    private IndexWriter  writer;
    
    // For Processing User's Geo Tree
    private String[] keys = {"country", "state", "county"};
    
    private QueryParser parser;
    
    // Fix Geo UI
    private boolean doUpdateName = false;
    private boolean doMerge      = false;
    private boolean doAddISOCode = true;
    private String  isoCodeStr   = "";
    private Integer mergeToGeoId = null;
    
    private boolean[] doAllCountries;
    private boolean[] doInvCountry;
    private Integer   doIndvCountryId = null;
    
    private Vector<Integer> countryIds = new Vector<Integer>();
    private Vector<Triple<String, Integer, String>> countryInfo = new Vector<Triple<String, Integer, String>>();

    
    /**
     * Constructor.
     * @param geoDef
     * @param nowStr
     * @param createdByAgent
     * @param itUsername
     * @param itPassword
     * @param frame
     */
    public GeographyAssignISOs(final GeographyTreeDef   geoDef, 
                               final Agent              createdByAgent,
                               final ProgressFrame      frame)
    {
        super();
        this.geoDef            = geoDef;
        this.createdByAgent    = createdByAgent;
        this.frame             = frame;
        
        FILE_INDEX_DIR = new File(getAppDataDir() + File.separator + "genames-index");
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
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
    
    private boolean checkUniqueness(final int rankId)
    {
//        String sql = QueryAdjusterForDomain.getInstance().adjustSQL(String.format("SELECT CNT, NM FROM (SELECT COUNT(Name) CNT, Name NM FROM geography WHERE RankID = %d AND GeographyTreeDefID = GEOTREEDEFID GROUP BY Name) T1 WHERE CNT > 1 ORDER BY NM ASC", rankId));
//    	Vector<Object[]> items = BasicSQLUtils.query(sql);
//    	if (items != null && items.size() > 0)
//    	{
//        	sql = QueryAdjusterForDomain.getInstance().adjustSQL(String.format("SELECT Name FROM geographytreedefitem WHERE RankID = %d AND GeographyTreeDefID = GEOTREEDEFID", rankId));
//        	String rankName = BasicSQLUtils.querySingleObj(sql);
//    		UIRegistry.displayInfoMsgDlg("There are " + rankName);
//    		//return false;
//    	}
    	return true;
    }
    
    private boolean isUniquenessOK()
    {
    	if (checkUniqueness(100))
    	{
    		if (checkUniqueness(200))
    		{
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * @return
     */
    @SuppressWarnings("rawtypes")
    public boolean buildAsync(final int earthId, final ChangeListener cl)
    {
    	if (!isUniquenessOK())
    	{
            if (cl != null) cl.stateChanged(new ChangeEvent(GeographyAssignISOs.this));
    		return false;
    	}
    	
        final JCheckBox    continentsCBX = createCheckBox("All Continents"); // I18N
        final JCheckBox    countriesCBX  = createCheckBox("All Countries");
        final JCheckBox    stateCBX      = createCheckBox("All States");
        final JCheckBox    countiesCBX   = createCheckBox("All Counties");
        
        continentsCBX.setEnabled(false);
        continentsCBX.setSelected(true);
        
        countriesCBX.setEnabled(true);
        stateCBX.setEnabled(false);
        countiesCBX.setEnabled(false);
        
        countryIds.clear();
        String sql = "SELECT g.GeographyID, g.Name, g2.Name FROM geography g LEFT JOIN geography g2 ON g.ParentID = g2.GeographyID " +
        	         "WHERE g.Name IS NOT NULL && LENGTH(g.Name) > 0 AND g.RankID = 200 AND g.GeographyTreeDefID = GEOTREEDEFID ORDER BY g.Name";
        sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
        
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
        final JComboBox spCountriesCmbx = createComboBox(titles);
        final JCheckBox spStatesCBX     = createCheckBox("States (Always)"); // I18N
        final JCheckBox spCountiesCBX   = createCheckBox("Counties");        // I18N
        
        spCountriesCmbx.setSelectedIndex(0);
        
        spStatesCBX.setSelected(false);
        spStatesCBX.setEnabled(false);
        spCountiesCBX.setEnabled(false);

        Object[] comps = new Object[] {"Continents to be processed", continentsCBX, // I18N
        		                       "Choose the Geographies to be processed", countriesCBX, stateCBX, countiesCBX, // I18N
                                       "Choose an individual Country", spCountriesCmbx, spStatesCBX, spCountiesCBX};// I18N
        String          rowDef = createDuplicateJGoodiesDef("p", "4px", comps.length);
        CellConstraints cc     = new CellConstraints();
        PanelBuilder    pb     = new PanelBuilder(new FormLayout("p,2px,f:p:g", rowDef));

        pb.setDefaultDialogBorder();
        final CustomDialog dlg = new CustomDialog((Frame)getTopWindow(), "Geographies To Be Processed", true, pb.getPanel()); // I18N

        i = 1;
        for (Object c : comps)
        {
            if (c instanceof String)
            {
                pb.addSeparator((String)c, cc.xyw(1, i, 3));
            } else if (c instanceof JComboBox)
            {
                pb.add(createFormLabel("Country"), cc.xy(1, i));// I18N
                pb.add((JComboBox)c, cc.xy(3, i));
            } else
            {
                pb.add((JComponent)c, cc.xyw(1, i, 3));
            }
            i += 2;
        }
        
        // Setup actions
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
                dlg.getOkBtn().setEnabled(isSel || countriesCBX.isSelected());

            }
        });
        
        spStatesCBX.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                spCountiesCBX.setEnabled(stateCBX.isSelected());
            }
        });
        
        centerAndShow(dlg);
        if (dlg.isCancelled())
        {
            if (cl != null) cl.stateChanged(new ChangeEvent(GeographyAssignISOs.this));
            return false;
        }
        
        connectToDB();
        
        doAllCountries  = new boolean[] {countriesCBX.isSelected(), stateCBX.isSelected(), countiesCBX.isSelected(), false};
        doInvCountry    = new boolean[] {spCountriesCmbx.getSelectedIndex() > 0, spStatesCBX.isSelected(), spCountiesCBX.isSelected(), false};
        doIndvCountryId = doInvCountry[0] ? countryIds.get(spCountriesCmbx.getSelectedIndex()) : null;
        
        
        // Check to see if it needs indexing.
        boolean shouldIndex = true;
        
        Long lastGeoNamesBuildTime = BuildFromGeonames.getLastGeonamesBuiltTime();
        if (lastGeoNamesBuildTime != null)
        {
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            Long lastIndexBuild = localPrefs != null ? localPrefs.getLong(GEONAMES_INDEX_DATE_PREF, null) : null;
            if (lastIndexBuild != null && lastIndexBuild.equals(lastGeoNamesBuildTime))
            {
                if (initLuceneforReading())
                {
                    if (reader != null)
                    {
                        Integer numDocs = localPrefs != null ? localPrefs.getInt(GEONAMES_INDEX_NUMDOCS, null) : null;
                        if (numDocs != null)
                        {
                            System.out.println(String.format("%d %d", reader.numDocs(), numDocs));
                            shouldIndex = reader.numDocs() != numDocs;
                        }
                    }
                    doneSearching();
                }
            }
        } else
        {
            showError("Specify cannot proceed the geonames table is missing."); // shouldn't happen
            if (cl != null) cl.stateChanged(new ChangeEvent(GeographyAssignISOs.this));
            return false;
        }

        if (shouldIndex)
        {
            startIndexingProcessAsync(earthId, cl);
            
        } else
        {
            sql = "SELECT Name, geonameId, iso_alpha2 FROM countryinfo";
            for (Object[] row : query(sql))
            {
                countryInfo.add(new Triple<String, Integer, String>((String)row[0], (Integer)row[1], (String)row[2]));
            }
            startTraversal();
            if (cl != null) cl.stateChanged(new ChangeEvent(GeographyAssignISOs.this));
        }
        
        return true;
    }
    
    /**
     * @param earthId
     * @param cl
     */
    private void startIndexingProcessAsync(final int earthId, final ChangeListener cl)
    {
        centerAndShow(frame);
        
        SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>()
        {
            boolean isOK = true;
            @Override
            protected Boolean doInBackground() throws Exception
            {
                setProgressDesc("Build Geography Names cross-reference...");  // I18N
                stCntXRef = new StateCountryContXRef(readConn);
                isOK = stCntXRef.build();
                if (isOK)
                {
                    setProgressDesc("Creating searchable index...");  // I18N
                    isOK =  buildLuceneIndex(earthId);
                }
                return isOK;
            }
            @Override
            protected void done()
            {
                super.done();
                
                frame.setVisible(false);
                
                // NOTE: need to check here that everything built OK
                if (isOK)
                {
                    startTraversal();
                    cl.stateChanged(new ChangeEvent(GeographyAssignISOs.this));
                }
            }
        };
        worker.execute();
    }
    
    /**
     * @param desc
     */
    private void setProgressDesc(final String desc)
    {
        if (frame != null)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    frame.setDesc(desc);
                }
            });
        }
    }
    
    /**
     * @param name
     * @return
     */
    private String stripExtrasFromName(final String name)
    {
        
        String[] extras = new String[] {"Islamic Republic ", "Republic ", "Islamic ", "Independent ",
                                       "Federal ", "Democratic ", "Federation ", "Commonwealth ", 
                                       "Principality ", "Federative", "Plurinational ", "Socialist ", };
        
        String sName = StringUtils.replace(name, "State of ", " ");
        sName = StringUtils.replace(sName, "Union of ", " ");
        sName = StringUtils.replace(sName, "Kingdom of ", " ");
        sName = StringUtils.replace(sName, " of ", " ");
        sName = StringUtils.replace(sName, " the ", " ");
        
        for (String extra : extras)
        {
            sName = StringUtils.remove(sName, extra);
        }
        return sName;
    }
    
    /**
     * Builds the Geography tree from the geonames table.
     * @param earthId the id of the root.
     * @return true on success
     */
    private boolean buildLuceneIndex(final int earthId)
    {
        boolean   isOK           = true;
        boolean   doCloseIndexer = true;
        Statement stmt           = null;
        try
        {
            connectToDB();
            
            stmt = readConn.createStatement();
            
            int cnt;
            
            initLuceneForIndexing(true);
            
            //////////////////////
            // Continent
            //////////////////////
            cnt    = 0;
            String cntSQL = "SELECT COUNT(*) ";
            int    totCnt = getCountAsInt(cntSQL + "FROM continentCodes");
            if (frame != null) frame.setProcess(0, totCnt);
            
            String sqlStr = "SELECT geonameId, name, code from continentCodes";
            ResultSet rs = stmt.executeQuery(sqlStr);
            while (rs.next() && isOK)
            {
                isOK = addDoc(rs.getInt(1), 
                              rs.getString(2),
                              "", "", "",
                              100,
                              rs.getString(3),
                              "",
                              "");
                cnt++;
                if (frame != null) frame.setProcess(cnt);
            }
            rs.close();
            
            if (!isOK) return false;
            
            //////////////////////
            // Create an Countries that referenced in the geoname table
            //////////////////////
            /*cnt    = 0;
            post   = "FROM countryinfo ORDER BY continent, iso_alpha2";
            totCnt = getCountAsInt(cntSQL + post);
            inc    = totCnt / 20;
            
            rs = stmt.executeQuery("SELECT geonameId, name, iso_alpha2, continent " + post);
            while (rs.next() && isOK)
            {
                int    geonameId     = rs.getInt(1);
                String countryName   = rs.getString(2);
                String countryCode   = rs.getString(3);
                
                //log.debug("1 Adding country["+countryName+"] "+countryCode);
                isOK = addDoc(geonameId, countryName, countryName, null, null, 200, countryCode, countryCode);
                if (frame != null && cnt % inc == 0) frame.setProcess(cnt);
            }
            rs.close();*/

            // Now create all the countries in the geoname table
            cnt     = 0;
            String post = "FROM countryinfo c INNER JOIN geoname g ON g.geonameId = c.geonameId";
            totCnt  = getCountAsInt(cntSQL + post);
            int inc = totCnt / 20;
            if (frame != null) frame.setProcess(0, 100);


            sqlStr = "SELECT c.geonameId, c.Name, Latitude, Longitude, iso_alpha2, iso_alpha3 " + post;
            System.out.println(sqlStr);
            rs = stmt.executeQuery(sqlStr);
            while (rs.next() && isOK)
            {
                String countryCode = rs.getString(5);
                String countryName = rs.getString(2);
                System.out.println(countryName);
//                if (countryName.equals("Islamic Republic of Afghanistan"))
//                {
//                    System.out.println(countryName);
//                }
                
                if (stCntXRef.countryCodeToName(countryCode) == null)
                {
                    log.error("Error - Unknown country code["+countryCode+"]");
                }   
                
                isOK = buildDoc(rs, 200, earthId);
                
                cnt++;
                if (frame != null && (cnt % inc) == 0) frame.setProcess(cnt / 20);
            }
            rs.close();
            
            if (!isOK) return false;
            
            setProgressDesc("Creating States...");  // I18N
            
            //////////////////////
            // States
            //////////////////////
            cnt    = 0;
            post   = "FROM geoname WHERE asciiname IS NOT NULL AND LENGTH(asciiname) > 0 AND fcode = 'ADM1' ORDER BY asciiname";
            totCnt = getCountAsInt(cntSQL + post);
            inc    = totCnt / 20;
            if (frame != null) frame.setProcess(0, 100);

            sqlStr = "SELECT geonameId, asciiname, latitude, longitude, country, admin1, ISOCode ";
            rs     = stmt.executeQuery(sqlStr + post);
            while (rs.next())
            {
                isOK = buildDoc(rs, 300, earthId);
                cnt++;
                if (frame != null && cnt % inc == 0) frame.setProcess(cnt / 20);
            }
            rs.close();
            
            if (!isOK) return false;
            
            setProgressDesc("Creating Counties...");  // I18N
            
            //////////////////////
            // County
            //////////////////////
            cnt    = 0;
            post   = "FROM geoname WHERE fcode = 'ADM2' ORDER BY asciiname";
            totCnt = getCountAsInt(cntSQL + post);
            inc    = totCnt / 20;
            if (frame != null) frame.setProcess(0, 100);
            
            sqlStr = "SELECT geonameId, asciiname, latitude, longitude, country, admin1, admin2, ISOCode ";
            rs = stmt.executeQuery(sqlStr + post);
            while (rs.next() && isOK)
            {
                rowData.clear();
                rowData.add((Integer)rs.getInt(1)); //             (0)
                
                rowData.add(rs.getString(2));       //             (1)
                
                rowData.add(rs.getBigDecimal(3));   // Lat         (2)
                rowData.add(rs.getBigDecimal(4));   // Lon         (3)
                rowData.add(rs.getString(5));       // CountryCode (4)
                rowData.add(rs.getString(6));       // StateCode   (5)
                rowData.add(rs.getString(7));       // CountyCode  (6)
                rowData.add(rs.getString(8));       // ISOCode     (7)
                
                isOK = buildDocInsert(rowData, 400, earthId);
                
                cnt++;
                if (frame != null && cnt % inc == 0) frame.setProcess(cnt / 20);
            }
            rs.close();
            
            if (!isOK) return false;
            
            doneIndexing();
            doCloseIndexer = false;
            
            return true;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GeographyAssignISOs.class, ex);

        } finally
        {
            if (!isOK)
            {
                showError("There was an error indexing geographies.");
            }
            
            if (doCloseIndexer)
            {
                try
                {
                    if (stmt != null) stmt.close();
                } catch (Exception ex) {}
            }
        }
        
        return false;
    }
    
    /**
     * @param rs
     * @param rankId
     * @param earthId
     * @return
     * @throws SQLException
     */
    private boolean buildDoc(final ResultSet rs, 
                                final int       rankId,
                                final int       earthId) throws SQLException
    {
        rowData.clear();
        for (int i=0;i<rs.getMetaData().getColumnCount();i++)
        {
            rowData.add(rs.getObject(i+1));
        }
        return buildDocInsert(rowData, rankId, earthId);
    }
    
    /**
     * @param fullName
     * @param countryName
     * @param stateName
     * @param countyName
     * @param rankId
     * @param fullISOStr
     * @param countryCode
     */
    private boolean addDoc(final int    geonmId,
                           final String fullName, 
                           final String countryName, 
                           final String stateName, 
                           final String countyName, 
                           final int    rankId,
                           final String fullISOStr,
                           final String countryCode,
                           final String isoCode3) // Countries Only
    {
        Document doc = new Document();
        doc.add(new Field("name", stripExtrasFromName(fullName),  Field.Store.YES, Field.Index.ANALYZED));
        if (rankId == 200)
        {
            countryInfo.add(new Triple<String, Integer, String>(countryName, geonmId, countryCode));
            System.out.println(">> "+stripExtrasFromName(fullName)+" ["+countryName+"]");
        }
        
        if (countryName != null)
        {
            doc.add(new Field("country", countryName, Field.Store.NO, Field.Index.ANALYZED));
        }
        if (stateName != null)
        {
            doc.add(new Field("state", stateName, Field.Store.NO, Field.Index.ANALYZED));
        }
        if (countyName != null)
        {
            String cName = StringUtils.remove(countyName, " County");
            doc.add(new Field("county", cName, Field.Store.NO, Field.Index.ANALYZED));
        }
        doc.add(new Field("rankid",  Integer.toString(rankId),  Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("geonmid", Integer.toString(geonmId), Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("code",    fullISOStr != null ? fullISOStr : "", Field.Store.YES, Field.Index.NOT_ANALYZED));
        
        if (isoCode3 != null) doc.add(new Field("code3",    isoCode3, Field.Store.NO, Field.Index.ANALYZED));

        
        try
        {
            //System.out.println(String.format("%s [%s]", nameStr, fullISOStr));
        	System.out.println(String.format("%d - %s (%s)", geonmId, fullName, fullISOStr));
            writer.addDocument(doc);
            return true;
        } catch (CorruptIndexException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    
    /**
     * @param row
     * @param rankId
     * @param earthId
     * @return
     * @throws SQLException
     */
    private boolean buildDocInsert(final List<Object> row, 
                                final int             rankId,
                                final int             earthId) throws SQLException
    {
        
        int    geonameId    = (Integer)row.get(0);
        String nameStr      = row.get(1).toString().trim();
        
        String  isoCode     = null;
        String  isoCode3    = null;
        String  countryCode = null;
        
        String countryName  = null;
        String stateName    = null;
        String countyName   = null;
        
        if (rankId == 100) // Continents
        {
            isoCode  = row.get(4).toString();
            
        } else if (rankId == 200) // Country
        {
            countryName = nameStr;
            countryCode = row.get(4).toString();
            isoCode3    = row.get(5).toString(); 
            String continentCode = stCntXRef.countryCodeToContinentCode(countryCode);
            
            isoCode = countryCode;
            
            if (continentCode == null)
            {
                StringBuilder sb = new StringBuilder("No Continent Code ["+continentCode+"]:\n");
                for (int i=0;i<row.size();i++)
                {
                    sb.append(i+" - "+row.get(i)+"\n");
                }
                log.error(sb.toString());
            }
            
        } else if (rankId == 300) // State
        {
            stateName   = nameStr;
            System.out.println(geonameId+"  "+stateName);
            countryCode = row.get(4).toString();
            isoCode     = row.get(6).toString();
            countryName = stCntXRef.countryCodeToName(countryCode);
            
        } else if (rankId == 400) // County
        {
            countyName  = nameStr;
            countryCode = row.get(4).toString();
            isoCode     = row.get(7) != null ? row.get(7).toString() : null;
        }
        
        if (nameStr.length() > 64)
        {
            log.error("Name["+nameStr+" is too long "+nameStr.length() + "truncating.");
            nameStr = nameStr.substring(0, 64);
        }
        
        if (StringUtils.isNotEmpty(isoCode) && isoCode.length() > 24) // Schema 1.8
        {
            isoCode = isoCode.substring(0, 24);
        }

        boolean status = true;
        /*if (rankId == 200 && isoCode != null && stCntXRef.countryCodeToName(isoCode) == null)
        {
            System.out.println("Skipping ["+countryName+"]  ISO Code["+isoCode+"]");
            return false;
        }*/
        
        // Prepare Data for Indexing
        StringBuilder fullName = new StringBuilder();
        if (StringUtils.isNotEmpty(countyName))
        {
            fullName.append(countyName);
        }
        if (StringUtils.isNotEmpty(stateName))
        {
            fullName.append(" ");
            fullName.append(stateName);
        }
        if (StringUtils.isNotEmpty(countryName))
        {
            fullName.append(" ");
            fullName.append(countryName);
        }
        
        // Lucene Index
        status = addDoc(geonameId, 
                        fullName.toString(), 
                        countryName, 
                        stateName, 
                        countyName, 
                        rankId,
                        isoCode,
                        countryCode,
                        isoCode3);
        
        if (StringUtils.isNotEmpty(countyName))
        {
            String lwCty = countyName.toLowerCase();
            if (lwCty.startsWith("saint ") && lwCty.length() > 7)
            {
                addDoc(geonameId, 
                        fullName.toString(), 
                        countryName, 
                        stateName, 
                        "St. " + countyName.substring(6), 
                        rankId,
                        isoCode,
                        countryCode,
                        null);
                
                addDoc(geonameId, 
                        fullName.toString(), 
                        countryName, 
                        stateName, 
                        "St " + countyName.substring(6), 
                        rankId,
                        isoCode,
                        countryCode,
                        null);
            }
        }
        return status;
    }
    
    //--------------------------------------------------------------------------------------------------------------------------------
    //-- Lucene
    //--------------------------------------------------------------------------------------------------------------------------------
    
    /**
     * @return false if index doesn't exist
     */
    public boolean initLuceneforReading()
    {
        if (!FILE_INDEX_DIR.exists()) return false;
        
        try
        {
            reader = IndexReader.open(FSDirectory.open(FILE_INDEX_DIR));
            System.out.println("Num Docs: "+reader.numDocs());
            
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        Set<?>          stdStopWords = StandardAnalyzer.STOP_WORDS_SET;
        HashSet<Object> stopWords    = new HashSet<Object>(stdStopWords); 
        stopWords.remove("will");
        
        /*for (Object o : stopWords)
        {
            System.out.print(o.toString()+' ');
        }
        System.out.println();*/
        
        searcher = new IndexSearcher(reader);
        analyzer = new StandardAnalyzer(Version.LUCENE_47, CharArraySet.EMPTY_SET);
        parser   = new QueryParser(Version.LUCENE_47, "name", analyzer);
        
        return true;
    }

    /**
     * @param doDeleteIndex
     */
    public void initLuceneForIndexing(final boolean doDeleteIndex)
    {
        try
        {
        	if (writer != null)
        	{
      			writer = null;
        	}
            if (doDeleteIndex && FILE_INDEX_DIR.exists())
            {
                FileUtils.deleteDirectory(FILE_INDEX_DIR);
            }
            
            if (!FILE_INDEX_DIR.mkdirs())
            {
                // error
            }
            
            Analyzer          indexAnalyzer = new StandardAnalyzer(Version.LUCENE_36);
            IndexWriterConfig config        = new IndexWriterConfig(Version.LUCENE_36, indexAnalyzer);
            writer = new IndexWriter(FSDirectory.open(FILE_INDEX_DIR), config);
            
            log.debug("Indexing to directory '" + FILE_INDEX_DIR + "'...");
            
        } catch (CorruptIndexException e)
        {
            e.printStackTrace();
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     */
    public void doneIndexing()
    {
        try
        {
            System.out.println(writer.numDocs());
            AppPreferences.getLocalPrefs().putInt(GEONAMES_INDEX_NUMDOCS, writer.numDocs());
            
            Long lastGeoNamesBuildTime = BuildFromGeonames.getLastGeonamesBuiltTime();
            if (lastGeoNamesBuildTime != null)
            {
                AppPreferences.getLocalPrefs().putLong(GEONAMES_INDEX_DATE_PREF, lastGeoNamesBuildTime);
            }
            writer.close();
            writer = null;
            
        } catch (CorruptIndexException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     */
    public void doneSearching()
    {
        try
        {
            analyzer.close();
            //searcher.close();
            reader.close();
            
            analyzer = null;
            searcher = null;
            reader   = null;
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        areNodesChanged = false;
        if (areNodesChanged)
        {
            try
            {
                geoDef.setNodeNumbersAreUpToDate(false);
                geoDef.checkNodeNumbersUpToDate(true);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * @param geoId
     * @param level
     * @param rankId
     * @param parentNames
     * @throws SQLException
     */
    private void findGeo(final int       geoId, 
                         final int       level,
                         final int       rankId,
                         final String[]  parentNames,
                         final boolean   isIndvCountry) throws SQLException
    {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<level+1;i++)
        {
            String name = i == 0 ? stripExtrasFromName(parentNames[0]) : parentNames[i];
            if (i > 0) sb.append(" AND ");
            sb.append(String.format("%s:\"%s\"", keys[i], name));
        }
        
        /*if (rankId == 200 && parentNames[0].length() == 3)
        {
            sb.insert(0, '(');
            sb.append(") OR code3:");
            sb.append(parentNames[0]);
        }*/
        String searchText = sb.toString();

        // Check the database directly
        try
        {
            String            countryCode = null;
            PreparedStatement pStmt       = null;
            if (rankId == 100)
            {
                pStmt      = lookupContinentStmt;
                searchText = parentNames[0];

            } else if (rankId == 200)
            {
                pStmt = lookupCountryStmt;
                searchText = parentNames[0];

            } else if (rankId == 300)
            {
                lookupCountryStmt.setString(1, parentNames[0]);
                ResultSet rs = lookupCountryStmt.executeQuery();
                if (rs.next())
                {
                    countryCode = rs.getString(2);
                }
                rs.close();
                
                if (StringUtils.isNotEmpty(countryCode))
                {
                    searchText = parentNames[1];
                    pStmt = lookupStateStmt;
                    lookupStateStmt.setString(2, countryCode);
                }
            }
            
            Integer geonameId   = null;
            if (pStmt != null)
            {
                pStmt.setString(1, searchText);
                ResultSet rs = pStmt.executeQuery();
                if (rs.next())
                {
                    geonameId   = rs.getInt(1);
                    countryCode = rs.getString(2);
                }
                rs.close();
            }
            
            if (geonameId == null && rankId == 300)
            {
                lookupCountryStmt.setString(1, searchText);
                ResultSet rs = lookupCountryStmt.executeQuery();
                if (rs.next())
                {
                    geonameId = rs.getInt(1);
                }
                rs.close();
            }
            
            // Ok, now check Lucence
            if (geonameId == null)
            {
                Query  query      = parser.parse(searchText.replace('/', ' '));
                for (int j=0;j<level;j++) System.out.print("  ");
                log.debug("Searching for: " + query.toString());
        
                Document             doc         = null;
                boolean              found       = false;
                int                  hitsPerPage = 500;
                TopScoreDocCollector collector   = TopScoreDocCollector.create(hitsPerPage, true);
                searcher.search(query, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;
                for (int i=0;i<hits.length;++i) 
                {
                    int docId     = hits[i].doc;
                    doc           = searcher.doc(docId);
                    int docRankId = Integer.parseInt(doc.get("rankid"));
                    
                    if (rankId == docRankId)
                    {
                        geonameId = Integer.parseInt(doc.get("geonmid"));
                        found = true;
                        break;
                    }
                }
                
                if (!found)
                {
                    sb.setLength(0);
                    for (int i=0;i<level+1;i++)
                    {
                        if (i > 0) sb.append(' ');
                        sb.append(parentNames[i]);
                    }
                    query = new FuzzyQuery(new Term("name", sb.toString()));
            
                    geonameId   = null;
                    doc         = null;
                    found       = false;
                    collector   = TopScoreDocCollector.create(10, true);
                    searcher.search(query, collector);
                    hits = collector.topDocs().scoreDocs;
                    for (int i=0;i<hits.length;++i) 
                    {
                        if (i == 0)
                        {
                            int docId     = hits[0].doc;
                            doc           = searcher.doc(docId);
                            int docRankId = Integer.parseInt(doc.get("rankid"));
                            if (rankId == docRankId)
                            {
                                geonameId = Integer.parseInt(doc.get("geonmid"));
                            }
                        }
                        int docId     = hits[i].doc;
                        doc           = searcher.doc(docId);
                        System.out.println("Fuzzy: "+i+"  "+hits[i].score+"  "+doc.get("name"));
                    }
                    
                    if (rankId == 400 && !doInvCountry[2])
                    {
                    	return;
                    }
    
                    geonameId = chooseGeo(geoId, parentNames[level], level, rankId, parentNames, geonameId);
                    
                    if (doStopProcessing || (doSkipCountry && rankId > 199))
                    {
                        return;
                    }
                }
                
                if (geonameId != null)
                {
                    updateGeography(geoId, geonameId, parentNames, isoCodeStr);
                } else
                {
                	boolean didUpdate = false;
                	if (StringUtils.isNotEmpty(this.isoCodeStr))
                	{
                		String ic = BasicSQLUtils.querySingleObj("SELECT GeographyCode FROM geography WHERE GeographyID  = "+geoId);
                		if (ic == null || !ic.equals(this.isoCodeStr))
                		{
                			String updateStr = String.format("UPDATE geography SET GeographyCode='%s' WHERE GeographyID = %d", this.isoCodeStr, geoId);
                			BasicSQLUtils.update(updateStr);
                			didUpdate = true;
                		}
                	}

                	if (!didUpdate)
                	{
	                    String oldName = querySingleObj(GEONAME_SQL + geoId);
	                    String nbsp = "&nbsp;";
	                    tblWriter.log(parentNames[0], 
	                                  parentNames[1] != null ? parentNames[1] : nbsp, 
	                                  parentNames[2] != null ? parentNames[2] : nbsp, 
	                                  oldName, nbsp, nbsp, "Skipped");
	                    //System.out.println(String.format("No Match [%s]", parentNames[level]));
	                    return;
                	}
                }
            } else
            {
                updateGeography(geoId, geonameId, parentNames, countryCode);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        boolean doDrillDown = doAllCountries[level+1] || (isIndvCountry && doInvCountry[level+1]);
        
        if (doDrillDown) // don't go down further than County level
        {
            String    sql  = "SELECT GeographyID, Name, RankID FROM geography WHERE GeographyCode IS NULL AND ParentID = " + geoId + " ORDER BY RankID, Name";
            Statement stmt = readConn.createStatement();
            ResultSet rs   = stmt.executeQuery(sql);
            while (rs.next())
            {
                int    childGeoId  = rs.getInt(1);    // Get Child's Id
                String name        = rs.getString(2); // Get Child's Name
                int    childRankId = rs.getInt(3);    // Get Child's RankID
                
                for (int ii=level+1;ii<parentNames.length;ii++) parentNames[ii] = null;
                
                parentNames[level+1] = name;
                findGeo(childGeoId, level+1, childRankId, parentNames, isIndvCountry);
                if (doStopProcessing)
                {
                    return;
                } else if (doSkipCountry)
                {
                    doSkipCountry = rankId > 199;
                    if (doSkipCountry) return;
                }
            }
            rs.close();
            stmt.close();
        }
    }
    

    
    /**
     * Get full ISO code from Lucene Index.
     * @param geonameId
     * @return string with full geo ISO
     */
    /*private String getISOCode(final int geonameId)
    {
        try
        {
            String               geonameIdStr = Integer.toString(geonameId);
            Query                query        = parser.parse("geonmid:"+geonameIdStr);
            int                  hitsPerPage  = 500;
            TopScoreDocCollector collector    = TopScoreDocCollector.create(hitsPerPage, true);
            
            searcher.search(query, collector);
            for (ScoreDoc scoreDoc : collector.topDocs().scoreDocs) 
            {
                int      docId = scoreDoc.doc;
                Document doc   = searcher.doc(docId);
                if (geonameIdStr.equals(doc.get("geonmid")))
                {
                    return doc.get("code");
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }*/
    
    /**
     * @param geoId
     * @param geonameId
     * @param rankId
     */
    private void updateGeography(final int      geoId, 
                                 final int      geonameId,
                                 final String[] parentNames,
                                 final String   countryCode)
    {
        boolean autoCommit = true;
        
        String   oldName   = querySingleObj(GEONAME_SQL + geoId);
        String   sql       = "SELECT asciiname, ISOCode FROM geoname WHERE geonameId = " + geonameId;
        Object[] row       = queryForRow(sql);
        if (row != null || countryCode != null)
        {
        	boolean           isAlt   = (row == null && StringUtils.isNotEmpty(countryCode));
            String            name    = isAlt ? parentNames[0] : (String)row[0];
            String            isoCode = isAlt ? countryCode : (StringUtils.isNotEmpty(isoCodeStr) ? isoCodeStr : (String)row[1]);
            PreparedStatement pStmt   = null;
            try
            {
                autoCommit = updateConn.getAutoCommit();
                String pre  = "UPDATE geography SET ";
                String post = ", ModifiedByAgentID=?, TimestampModified=? WHERE GeographyID=?";
                int inx     = 2; 
                if (doUpdateName && doAddISOCode)
                {
                    pStmt = updateConn.prepareStatement(pre+"Name=?, GeographyCode=?"+post);
                    pStmt.setString(1, name);
                    pStmt.setString(2, isoCode);
                    inx = 3;
                    
                } else if (doUpdateName)
                {
                    pStmt = updateConn.prepareStatement(pre+"Name=?"+post);
                    pStmt.setString(1, name);
                    
                } else if (doAddISOCode)
                {
                    pStmt = updateConn.prepareStatement(pre+"GeographyCode=?"+post);
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
                        areNodesChanged = true; // Global indication that at least one node was updated.
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
                                areNodesChanged = true;
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
                    tblWriter.log(parentNames[0], 
                                  parentNames[1] != null ? parentNames[1] : nbsp, 
                                  parentNames[2] != null ? parentNames[2] : nbsp, 
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
                doUpdateName = false;
                doMerge      = false;
                doAddISOCode = true;
                isoCodeStr   = null;
                
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
    private Integer chooseGeo(final int       geoId,
                              final String    nameStr,
                              final int       level,
                              final int       rankId,
                              final String[]  parentNames,
                              final Integer   geonameId) throws SQLException
    {
        mergeToGeoId = null;
        
        GeoChooserDlg dlg = new GeoChooserDlg(nameStr, rankId, level, parentNames, geonameId, 
                                              stCntXRef, countryInfo, doAllCountries, doInvCountry, 
                                              readConn, countryCount, countryTotal);
        
        //int SKIP_BTN = CustomDialog.CANCEL_BTN;
        int SAVE_BTN = CustomDialog.OK_BTN;
        int NXTC_BTN = CustomDialog.APPLY_BTN;
        int QUIT_BTN = CustomDialog.HELP_BTN;
        
        dlg.createUI();
        dlg.pack();
        centerAndShow(dlg);
        
        //dlg.dispose();

        if (dlg.getBtnPressed() != QUIT_BTN) 
        {
            doUpdateName = dlg.getUpdateNameCB().isSelected();
            doMerge      = dlg.getMergeCB().isSelected();
            doAddISOCode = dlg.getAddISOCodeCB().isSelected();
            isoCodeStr   = dlg.getSelectedISOValue();

            if (dlg.getBtnPressed() == SAVE_BTN)
            {
                String selectedCounty = dlg.getSelectedListValue();
                if (doMerge && rankId == 400)
                {
                    int geoParentId = getCountAsInt(readConn, "SELECT ParentID FROM geography WHERE GeographyID = "+ geoId);
                    if (geoParentId != -1)
                    {
                        try
                        {
                            String cName = StringUtils.remove(selectedCounty, " County");
                            PreparedStatement pStmt = updateConn.prepareStatement("SELECT GeographyID FROM geography WHERE RankID = 400 AND GeographyID <> ? AND ParentID = ? AND (Name = ? OR Name = ?");
                            pStmt.setInt(1,    geoId);
                            pStmt.setInt(2,    geoParentId);
                            pStmt.setString(3, cName);
                            pStmt.setString(4, selectedCounty);
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
                parentNames[level] = selectedCounty;
                Integer idFromList = dlg.getSelectedId();
                Integer lookupId   = dlg.getLookupId();
                
                if (idFromList == null)
                {
                    return lookupId;
                }
                return idFromList;
                
            } else if (dlg.getBtnPressed() == NXTC_BTN)
            {
                doSkipCountry = true;
            }
        } else
        {
            doStopProcessing = true;
        }
        return null;
    }
                              
    /**
     * 
     */
    public void startTraversal()
    {
        UIRegistry.writeSimpleGlassPaneMsg("Processing geography...", 24);

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
        
        if (!initLuceneforReading())
        {
            showError("The geography index is missing!");
            return;
        }
        
        HashMap<String, String> countryMappings = new HashMap<String, String>();
        try
        {
            String fullPath = getAppDataDir() + File.separator + "geo_report.html";
            tblWriter       = new TableWriter(fullPath, "Geography ISO Code Report");
            tblWriter.startTable();
            tblWriter.logHdr("Country", "State", "County", "Old Name", "New Name", "ISO Code", "Action");

            String[] parentNames = new String[3];
            // KUFish - United States
            // Herps - United State 853, USA 1065
            // KUPlants 205
            
            totalUpdated = 0;
            totalMerged  = 0;
            
            //-------------------
            // Do Continent
            //-------------------
            countryCount = 0;
            countryTotal = getCountAsInt(readConn, "SELECT COUNT(*) FROM geography WHERE GeographyCode IS NULL AND RankID = 100");
            String sql   = "SELECT GeographyID, Name FROM geography WHERE GeographyCode IS NULL AND RankID = 100 ORDER BY Name ASC";
            for (Object[] row : query(readConn, sql))
            {
                for (int i=0;i<parentNames.length;i++) parentNames[i] = null;
                
                String continentName = (String)row[1];
//                continentName = countryMappings.get(continentName);
//                if (continentName == null)
//                {
//                    continentName = (String)row[1];
//                }
                
                int geoId = (Integer)row[0];
                System.out.println(continentName+"  "+geoId);

                parentNames[0] = continentName;
                findGeo((Integer)row[0], 0, 100, parentNames, false);
                if (doStopProcessing)
                {
                    break;
                }
                countryCount++;
            }            
            
            
            //-------------------
            // Do Country
            //-------------------
            countryCount = 0;
            countryTotal = getCountAsInt(readConn, "SELECT COUNT(*) FROM geography WHERE GeographyCode IS NULL AND RankID = 200");
            sql  = "SELECT GeographyID, Name FROM geography WHERE ";
            sql += doIndvCountryId != null ? "(GeographyCode IS NULL OR GeographyID = %d)" : "GeographyCode IS NULL";
            sql += " AND RankID = 200 ORDER BY Name ASC";
            
            if (doIndvCountryId != null)
            {
                sql = String.format(sql,  doIndvCountryId);
            }
            
            for (Object[] row : query(readConn, sql))
            {
                doSkipCountry = false;
                
                for (int i=0;i<parentNames.length;i++) parentNames[i] = null;
                
                String countryName = (String)row[1];
                countryName = countryMappings.get(countryName);
                if (countryName == null)
                {
                    countryName = (String)row[1];
                }
                
                int     geoId         = (Integer)row[0];
                boolean isIndvCountry = doIndvCountryId != null && doIndvCountryId == geoId;
                System.out.println(countryName+"  "+geoId+"   doInvCountry[0]: "+doInvCountry[0]+"  doIndvCountryId: "+doIndvCountryId+"  isIndvCountry: "+isIndvCountry);
                if (doAllCountries[0] || (doInvCountry[0] && isIndvCountry))
                {
                    System.out.println(countryName);
                    parentNames[0] = countryName;
                    findGeo((Integer)row[0], 0, 200, parentNames, isIndvCountry);
                    if (doStopProcessing)
                    {
                        break;
                    }
                }
                countryCount++;
            }
            tblWriter.endTable();
            tblWriter.close();
            
            if (tblWriter.hasLines())
            {
                AttachmentUtils.openFile(new File(fullPath));
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            doneSearching();
            
            try
            {
                if (readConn != null) readConn.close();
                if (updateConn != DBConnection.getInstance()) updateConn.close();
                if (lookupCountryStmt != null) lookupCountryStmt.close();
                if (lookupStateStmt != null) lookupStateStmt.close();

            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
            
            UIRegistry.clearSimpleGlassPaneMsg();
            String msg = String.format("Geography records updated: %d", totalUpdated);
            if (doMerge)
            {
                msg += String.format("\nGeography records merged: %d", totalMerged);
            }
            UIRegistry.showLocalizedMsg("INFORMATION", msg);
        }
    }
}
