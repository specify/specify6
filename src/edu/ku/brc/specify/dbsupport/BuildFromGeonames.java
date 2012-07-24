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
package edu.ku.brc.specify.dbsupport;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.hibernate.Session;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.BackupServiceFactory;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;

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
public class BuildFromGeonames
{
    private static final Logger  log = Logger.getLogger(BuildFromGeonames.class);
    private final String GEONAME_SQL = "SELECT Name FROM geography WHERE GeographyID = ";  

    private GeographyTreeDef           geoDef;
    private Timestamp                  now;
    private String                     insertSQL = null;
    private Agent                      createdByAgent;
    private ProgressFrame              frame;
    private boolean                    areNodesChanged = false;
    
    private boolean                    buildGeoNamesOnly;
    
    private String                     itUsername;
    private String                     itPassword;
    private Connection                 readConn = null;
    private Connection                 updateConn;
    private PreparedStatement          pStmt    = null;
    
    private ArrayList<Object>          rowData = new ArrayList<Object>();
    
    
    private Hashtable<String, String>  continentNameFromCode        = new Hashtable<String, String>();
    private Hashtable<String, String>  countryNameFromCode          = new Hashtable<String, String>();
    private Hashtable<String, String>  stateNameFromCode            = new Hashtable<String, String>();
    private Hashtable<String, String>  countryToISO3                = new Hashtable<String, String>();
    
    private Hashtable<String, String>  continentCodeFromCountryCode = new Hashtable<String, String>();
    private Hashtable<String, String>  countryCodeFromStateCode     = new Hashtable<String, String>();
    private Hashtable<String, Integer> contToIdHash                 = new Hashtable<String, Integer>();
    
    private Hashtable<String, String>  countryCodeFromName          = new Hashtable<String, String>();
    private Hashtable<String, String>  stateCodeFromName            = new Hashtable<String, String>();
    
    private Hashtable<String, Hashtable<String, Integer>> countryStateCodeToIdHash = new Hashtable<String, Hashtable<String, Integer>>();
    
    private Hashtable<String, Integer> countryCodeToIdHash    = new Hashtable<String, Integer>();
    
    private int countryTotal;
    private int countryCount;
    
    //-------------------------------------------------
    // Lucene Members
    //-------------------------------------------------
    private TableWriter  tblWriter        = null;
    private boolean      doStopProcessing = false;
    private boolean      doSkipCountry    = false;
    //private File         srcCodeFilesDir  = null;
    //private File         baseDir;
    private File         FILE_INDEX_DIR;
    
    private IndexReader  reader;
    private Searcher     searcher;
    private Analyzer     analyzer;
    
    private IndexWriter  writer;
    
    // For Processing User's Geo Tree
    private String[] keys = {"country", "state", "county"};
    
    private QueryParser parser;
    
    // Fix Geo UI
    private boolean doUpdateName = false;
    private boolean doMerge      = false;
    private boolean doAddISOCode = true;
    private Integer mergeToGeoId = null;
    
    private boolean[] doAllCountries;
    private boolean[] doInvCountry;
    private Integer   doIndvCountryId = null;
    
    private Vector<Integer> countryIds = new Vector<Integer>();
    private Vector<Pair<String, Integer>> countryInfo = new Vector<Pair<String, Integer>>();

    
    /**
     * Constructor.
     * @param geoDef
     * @param nowStr
     * @param createdByAgent
     * @param itUsername
     * @param itPassword
     * @param frame
     */
    public BuildFromGeonames(final GeographyTreeDef   geoDef, 
                             final Timestamp          now,
                             final Agent              createdByAgent,
                             final String             itUsername,
                             final String             itPassword,
                             final boolean            buildGeoNamesOnly,
                             final ProgressFrame      frame)
    {
        super();
        this.geoDef            = geoDef;
        this.now               = now;
        this.createdByAgent    = createdByAgent;
        this.itUsername        = itUsername;
        this.itPassword        = itPassword;
        this.buildGeoNamesOnly = buildGeoNamesOnly;
        this.frame             = frame;
        
        insertSQL = "INSERT INTO geography (Name, RankID, ParentID, IsAccepted, IsCurrent, GeographyTreeDefID, GeographyTreeDefItemID, " +
                    "CreatedByAgentID, CentroidLat, CentroidLon, Abbrev, GeographyCode, " +
                    "TimestampCreated, TimestampModified, Version) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        FILE_INDEX_DIR = new File(UIRegistry.getAppDataDir() + File.separator + "genames-index");
    }
    
    /**
     * Builds the root Geography record (node).
     * @param session the current session
     * @return the root
     */
    public Geography buildEarth(final Session session)
    {
        try
        {
         // setup the root Geography record (planet Earth)
            Geography earth = new Geography();
            earth.initialize();
            earth.setName("Earth");
            earth.setFullName("Earth");
            earth.setNodeNumber(1);
            earth.setHighestChildNodeNumber(1);
            earth.setRankId(0);
            earth.setDefinition(geoDef);
            
            GeographyTreeDefItem defItem = geoDef.getDefItemByRank(0);
            
            earth.setDefinitionItem(defItem);
            defItem.getTreeEntries().add(earth);
            
            session.saveOrUpdate(earth);
            
            return earth;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            
        }
        return null;
    }
    
    /**
     * 
     */
    private void connectToDB()
    {
        DBConnection currDBConn = DBConnection.getInstance();
        if (updateConn == null)
        {
            updateConn = currDBConn.createConnection();
        }
        readConn = currDBConn.createConnection();
    }

    /**
     * @param earthId
     * @return
     */
    public boolean build(final int earthId)
    {
        return buildInternal(earthId);
    }

    /**
     * @param earthId
     * @return
     */
    public boolean buildAsync(final int earthId, final ChangeListener cl)
    {
        final JCheckBox    countriesCBX = UIHelper.createCheckBox("All Countries");
        final JCheckBox    stateCBX     = UIHelper.createCheckBox("All States");
        final JCheckBox    countiesCBX  = UIHelper.createCheckBox("All Counties");
        
        countriesCBX.setSelected(true);
        countriesCBX.setEnabled(true);
        stateCBX.setEnabled(false);
        countiesCBX.setEnabled(false);
        
        countryIds.clear();
        String sql = "SELECT g.GeographyID, g.Name, g2.Name FROM geography g LEFT JOIN geography g2 ON g.ParentID = g2.GeographyID WHERE g.Name IS NOT NULL && LENGTH(g.Name) > 0 AND g.RankID = 200 ORDER BY g.Name";
        Vector<Object[]> rows   = BasicSQLUtils.query(sql);
        Object[]         titles = new Object[rows.size()+1];
        int i = 0;
        titles[i++] = "None";
        countryIds.add(-1);
        for (Object[] r : rows)
        {
            countryIds.add((Integer)r[0]);
            String countryStr = (String)r[1];
            String contStr    = (String)r[2];
            titles[i++] = countryStr != null ? (countryStr + " (" + contStr + ")") : countryStr;
        }
        final JComboBox spCountriesCmbx = UIHelper.createComboBox(titles);
        final JCheckBox spStatesCBX     = UIHelper.createCheckBox("States (Always)");
        final JCheckBox spCountiesCBX   = UIHelper.createCheckBox("Counties");
        
        spCountriesCmbx.setSelectedIndex(0);
        
        spStatesCBX.setSelected(false);
        spStatesCBX.setEnabled(false);
        spCountiesCBX.setEnabled(false);

        Object[] comps = new Object[] {"Choose the Geographies to be processed", countriesCBX, stateCBX, countiesCBX, 
                                       "Choose an individual Country", spCountriesCmbx, spStatesCBX, spCountiesCBX};
        String          rowDef = UIHelper.createDuplicateJGoodiesDef("p", "4px", comps.length);
        CellConstraints cc     = new CellConstraints();
        PanelBuilder    pb     = new PanelBuilder(new FormLayout("p,2px,f:p:g", rowDef));

        pb.setDefaultDialogBorder();
        final CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), "Geographies To Be Processed", true, pb.getPanel()); 

        i = 1;
        for (Object c : comps)
        {
            if (c instanceof String)
            {
                pb.addSeparator((String)c, cc.xyw(1, i, 3));
            } else if (c instanceof JComboBox)
            {
                pb.add(UIHelper.createFormLabel("Country"), cc.xy(1, i));
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
        
        UIHelper.centerAndShow(dlg);
        if (dlg.isCancelled())
        {
            return false;
        }
        
        doAllCountries  = new boolean[] {countriesCBX.isSelected(), stateCBX.isSelected(), countiesCBX.isSelected(), false};
        doInvCountry    = new boolean[] {spCountriesCmbx.getSelectedIndex() > 0, spStatesCBX.isSelected(), spCountiesCBX.isSelected(), false};
        doIndvCountryId = doInvCountry[0] ? countryIds.get(spCountriesCmbx.getSelectedIndex()) : null;
        
        if (buildGeoNamesOnly)
        {
            UIHelper.centerAndShow(frame);
            
            SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>()
            {
                @Override
                protected Boolean doInBackground() throws Exception
                {
                    setProgressDesc("Creating geography reference...");
                    
                    if (loadGeoNamesDB())
                    {
                        setProgressDesc("Creating searchable index...");
                        return buildInternal(earthId);
                    }
                    return false;
                }
                @Override
                protected void done()
                {
                    super.done();
                    
                    frame.setVisible(false);
                    
                    startTraversal();
                    
                    cl.stateChanged(new ChangeEvent(BuildFromGeonames.this));
                }
            };
            worker.execute();
            return true;
        }
        return false;
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
        
        String[] extras = new String[] {"Republic ", "Islamic ", "Independent ",
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
    private boolean buildInternal(final int earthId)
    {
        Statement    stmt       = null;
        try
        {
            connectToDB();
            
            pStmt = updateConn.prepareStatement(insertSQL);
            
            stmt = readConn.createStatement();
            
            Integer count = BasicSQLUtils.getCount(readConn, "SELECT COUNT(*) FROM geoname");
            if (frame != null)
            {
                frame.setProcess(0, count);
                setProgressDesc("Creating Geography...");
            }
            
            Hashtable<String, String> continentCodeFromName = new Hashtable<String, String>();
            ResultSet rs = stmt.executeQuery("SELECT code, name from continentCodes");
            while (rs.next())
            {
                continentNameFromCode.put(rs.getString(1), rs.getString(2));
                continentCodeFromName.put(rs.getString(2), rs.getString(1));
            }
            rs.close();
            
            setProgressDesc("Creating Continents..."); // I18N
            
            int cnt = 0;
            
            initLuceneForIndexing(true);
            
            //////////////////////
            // Continent
            //////////////////////
            String sqlStr = "SELECT g.geonameId, cc.name, g.latitude, g.longitude, cc.code FROM geoname g Inner Join continentCodes cc ON g.name = cc.name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                if (frame != null)
                {
                    frame.setProcess(cnt);
                }
                
                if (buildInsert(rs, 100, earthId) && !buildGeoNamesOnly)
                {
                    pStmt.executeUpdate();
                    Integer newId = BasicSQLUtils.getInsertedId(pStmt);
                    contToIdHash.put(rs.getString(5), newId);
                }
                
                cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    frame.setProcess(cnt);
                }
            }
            rs.close();
            
            //////////////////////
            // Countries
            //////////////////////
            
            setProgressDesc("Creating Countries...");
            
            // First map all Countries to Continents
            rs = stmt.executeQuery("SELECT iso_alpha2, iso_alpha3, continent FROM countryinfo ORDER BY continent, iso_alpha2");
            while (rs.next())
            {
                String countryCode     = rs.getString(1);
                String countryCodeISO3 = rs.getString(2);
                String continentCode   = rs.getString(3);
                countryStateCodeToIdHash.put(countryCode, new Hashtable<String, Integer>());
                countryToISO3.put(countryCode, countryCodeISO3);
                continentCodeFromCountryCode.put(countryCode, continentCode);
            }
            rs.close();
            
            // Create an Countries that referenced in the geoname table
            rs = stmt.executeQuery("SELECT geonameId, name, iso_alpha2, continent FROM countryinfo ORDER BY continent, iso_alpha2");
            while (rs.next())
            {
                String countryCode = rs.getString(3);
                if (buildGeoNamesOnly || BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM geography WHERE RankID = 200 AND Abbrev = '"+countryCode+"'") == 0)
                {
                    int    geonameId     = rs.getInt(1);
                    String countryName   = rs.getString(2);
                    String continentCode = rs.getString(4);
                    
                    log.debug("1 Adding country["+countryName+"] "+countryCode);
                    addDoc(geonameId, countryName, countryName, null, null, 200, countryCode, countryCode);
                    
                    countryNameFromCode.put(countryCode, countryName);
                    countryCodeFromName.put(countryName, countryCode);
                    //System.out.println(String.format("2[%s] [%s]", countryName, countryCode));
                    
                    if (!buildGeoNamesOnly)
                    {
                        prepareCountry(countryName, countryCode, continentCode, 200);
                        
                        if (pStmt.executeUpdate() == 1)
                        {
                            Integer newId = BasicSQLUtils.getInsertedId(pStmt);
                            countryCodeToIdHash.put(countryCode, newId);
                        }
                    }
                }
            }
            rs.close();

            // Now create all the countries in the geoname table
            sqlStr = "SELECT geonameId, asciiname, latitude, longitude, country FROM geoname WHERE fcode = 'PCLI' ORDER BY name";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                String countryName = rs.getString(2);
                String countryCode = rs.getString(5);
                
                if (countryNameFromCode.get(countryCode) == null)
                {
                    log.debug("2 Adding country["+countryName+"] "+countryCode);
                    countryNameFromCode.put(countryCode, countryName);
                    countryCodeFromName.put(countryName, countryCode);
    
                    if (buildInsert(rs, 200, earthId) && !buildGeoNamesOnly)
                    {
                        pStmt.executeUpdate();
                        Integer newId = BasicSQLUtils.getInsertedId(pStmt);
                        countryCodeToIdHash.put(rs.getString(5), newId);
                    }
                }                
                cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    frame.setProcess(cnt);
                }
            }
            rs.close();
            
            setProgressDesc("Creating States...");
            
            //////////////////////
            // States
            //////////////////////
            sqlStr = "SELECT geonameId, asciiname, latitude, longitude, country, admin1, ISOCode FROM geoname WHERE asciiname IS NOT NULL AND LENGTH(asciiname) > 0 AND fcode = 'ADM1' ORDER BY asciiname";
            rs     = stmt.executeQuery(sqlStr);
            while (rs.next())
            {
                if (buildInsert(rs, 300, earthId))
                {
                    String nameStr      = rs.getString(2);
                    String countryCode  = rs.getString(5);
                    String stateCode    = rs.getString(6);
                    String countryState = countryCode + "," + stateCode;
                    
                    String countryName = countryNameFromCode.get(countryCode);
                    stateNameFromCode.put(countryState, nameStr);
                    stateCodeFromName.put(countryName + ";" + nameStr, stateCode);
                    countryCodeFromStateCode.put(stateCode, countryCode);
                    
                    //System.out.println(nameStr);
                    
                    if (!buildGeoNamesOnly)
                    {
                        pStmt.executeUpdate();
                        Integer newId = BasicSQLUtils.getInsertedId(pStmt);
                        Hashtable<String, Integer> stateToIdHash = countryStateCodeToIdHash.get(countryCode);
                        if (stateToIdHash != null)
                        {
                            stateToIdHash.put(stateCode, newId);
                        } else
                        {
                            log.error("****** Error - No State for code ["+stateCode+"]  Country: "+countryCode+"   Name: "+nameStr);
                        }
                    }
                }
                
                cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    frame.setProcess(cnt);
                }

            }
            rs.close();
            
            setProgressDesc("Creating Counties...");
            
            //////////////////////
            // County
            //////////////////////
            sqlStr = "SELECT geonameId, asciiname, latitude, longitude, country, admin1, admin2, ISOCode FROM geoname WHERE fcode = 'ADM2' ORDER BY asciiname";
            rs = stmt.executeQuery(sqlStr);
            while (rs.next())
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
                
                if (buildInsert(rowData, 400, earthId) && !buildGeoNamesOnly)
                {
                    pStmt.executeUpdate();
                }
                
                cnt++;
                if (frame != null && cnt % 100 == 0)
                {
                    frame.setProcess(cnt);
                }
            }
            rs.close();
            
            if (frame != null)
            {
                frame.setProcess(count);
            }
            
            doneIndexing();
            
            initLuceneforReading();

            
            return true;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildFromGeonames.class, ex);

            try
            {
                updateConn.rollback();
                
            } catch (Exception exr)
            {
                exr.printStackTrace();
            }
        } finally
        {
            try
            {
                if (stmt != null)
                {
                    stmt.close();
                }
                if (readConn != null)
                {
                    readConn.close();
                    readConn = null;
                }
                if (pStmt != null)
                {
                    pStmt.close();
                    pStmt = null;
                }
                if (updateConn != DBConnection.getInstance())
                {
                    updateConn.close();
                    updateConn = null;
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        
        return false;
    }
    
    /**
     * 
     */
    private void buildISOCodes()
    {
        try
        {
            DBConnection currDBConn = DBConnection.getInstance();
            String       dbName     = currDBConn.getDatabaseName();
            DBMSUserMgr  dbMgr      = DBMSUserMgr.getInstance();
            if (dbMgr != null)
            {
                if (dbMgr.connectToDBMS(itUsername, itPassword, currDBConn.getServerName()))
                {
                    Connection conn = dbMgr.getConnection();
                    Statement  stmt = null;
                    ResultSet  rs   = null;
                    PreparedStatement pStmt = null;
                    try
                    {
                        conn.setCatalog(dbName);
                        
                        boolean isFieldOK = true;
                        if (!dbMgr.doesFieldExistInTable("geoname", "ISOCode"))
                        {
                            if (BasicSQLUtils.update(conn, "ALTER TABLE geoname ADD COLUMN ISOCode VARCHAR(24) DEFAULT NULL") == 0)
                            {
                                isFieldOK = false;
                            }
                        }
                        
                        if (isFieldOK)
                        {
                            String sql = "SELECT g.geonameId, g.fcode, g.country, g.admin1, g.admin2 FROM geoname g " +
                                         "ORDER BY g.country ASC, g.fcode DESC, g.admin1 ASC, g.admin2 ASC";
                            pStmt = conn.prepareStatement("UPDATE geoname SET ISOCode=? WHERE geonameId = ?");
                            stmt  = conn.createStatement();
                            rs    = stmt.executeQuery(sql);
                            
                            boolean hasCountry = false;
                            boolean isOK       = true;
                            StringBuilder sb = new StringBuilder();
                            while (rs.next() && isOK)
                            {
                                String fcode = rs.getString(2);
                                if (!hasCountry)
                                {
                                    hasCountry = fcode.equals("PCLI");
                                }
                                
                                if (hasCountry)
                                {
                                    String country = rs.getString(3);
                                    if (StringUtils.isEmpty(country))
                                    {
                                        continue;
                                    }
                                    
                                    String state   = rs.getString(4);
                                    String county  = rs.getString(5);
                                    
                                    sb.setLength(0);
                                    if (StringUtils.isNotEmpty(country))
                                    {
                                        sb.append(country);
                                        if (!fcode.equals("PCLI") && StringUtils.isNotEmpty(state))
                                        {
                                            sb.append(state);
                                            if (StringUtils.isNotEmpty(county))
                                            {
                                                sb.append(county);
                                            }
                                        }
                                        pStmt.setString(1, sb.length() > 24 ? sb.substring(0, 24) : sb.toString());
                                        pStmt.setInt(2, rs.getInt(1));
                                        isOK = pStmt.executeUpdate() == 1;
                                    }
                               }
                            }
                        } else
                        {
                            UIRegistry.showLocalizedError("ERROR_ERR_GEODB", dbName);
                        }
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    } finally
                    {
                        try
                        {
                            if (rs != null) rs.close();
                            if (stmt != null) stmt.close();
                            if (pStmt != null) pStmt.close();

                        } catch (Exception ex) {}
                    }
                } else
                {
                    UIRegistry.showLocalizedError("ERROR_LOGIN_GEODB", dbName);
                }
                dbMgr.close();
                
            } else
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildFromGeonames.class, new Exception("Couldn't create DBMSMgr"));
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildFromGeonames.class, ex);
        }

    }
    
    /**
     * @param rs
     * @param rankId
     * @param earthId
     * @return
     * @throws SQLException
     */
    private boolean buildInsert(final ResultSet rs, 
                                final int       rankId,
                                final int       earthId) throws SQLException
    {
        rowData.clear();
        for (int i=0;i<rs.getMetaData().getColumnCount();i++)
        {
            rowData.add(rs.getObject(i+1));
        }
        return buildInsert(rowData, rankId, earthId);
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
                           final String countryCode)
    {
        Document doc = new Document();
        doc.add(new Field("name", stripExtrasFromName(fullName),  Field.Store.YES, Field.Index.ANALYZED));
        if (rankId == 200)
        {
            countryInfo.add(new Pair<String, Integer>(countryName, geonmId));
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
        
        if (rankId > 199 && StringUtils.isNotEmpty(countryCode))
        {
            String iso3 = countryToISO3.get(countryCode);
            if (StringUtils.isNotEmpty(iso3))
            {
                doc.add(new Field("iso3", iso3, Field.Store.NO, Field.Index.NOT_ANALYZED));
            }
        }
        
        try
        {
            //System.out.println(String.format("%s [%s]", nameStr, fullISOStr));
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
    private boolean buildInsert(final List<Object> row, 
                                final int          rankId,
                                final int          earthId) throws SQLException
    {
        
        GeographyTreeDefItem item         = geoDef.getDefItemByRank(rankId);
        int                  geoDefItemId = item.getId();
        
        int    geonameId    = (Integer)row.get(0);
        String nameStr      = row.get(1).toString().trim();
        
        Integer parentId    = null;
        String  abbrev      = null;
        String  isoCode     = null;
        String  countryCode = null;
        
        String countryName  = null;
        String stateName    = null;
        String countyName   = null;
        
        if (rankId == 100) // Continents
        {
            parentId = earthId;
            abbrev   = row.get(4).toString();
            isoCode  = abbrev;
            
        } else if (rankId == 200) // Country
        {
            countryName = nameStr;
            countryCode = row.get(4).toString();
            String continentCode = continentCodeFromCountryCode.get(countryCode);
            
            abbrev  = countryCode;
            isoCode = countryCode;
            
            if (continentCode != null)
            {
                parentId = contToIdHash.get(continentCode);
                if (parentId == null && !buildGeoNamesOnly)
                {
                    log.error("No Continent Id for Name  continentCode["+continentCode+"]   Country["+nameStr+"]");
                }
            } else
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
            countryCode = row.get(4).toString();
            abbrev      = row.get(5).toString();
            isoCode     = row.get(6).toString();
            
            parentId = countryCodeToIdHash.get(countryCode);
            if (parentId == null && !buildGeoNamesOnly)
            {
                log.error("No Parent Id for CountryCode:["+countryCode+"]["+nameStr+"]");
            }
            countryName = countryNameFromCode.get(countryCode);
            
        } else if (rankId == 400) // County
        {
            countyName       = nameStr;
            String stateCode = row.get(5).toString();
            countryCode      = row.get(4).toString();
            
            abbrev  = ""; // There is no abbreviation for county
            isoCode = row.get(7) != null ? row.get(7).toString() : null;
            
            if (StringUtils.isNotEmpty(countryCode))
            {
                Hashtable<String, Integer> stateToIdHash = countryStateCodeToIdHash.get(countryCode);
                if (stateToIdHash != null)
                {
                    parentId = stateToIdHash.get(stateCode);
                    if (parentId == null && !buildGeoNamesOnly)
                    {
                        log.error("No State Id for CC["+countryCode+"]  stateCode["+stateCode+"] County["+nameStr+"]");
                    }
                } else if (!buildGeoNamesOnly)
                {
                    log.error("No State Hash for CC["+countryCode+"]  State["+stateCode+"]  Name: "+row.get(1));
                }
                
                countryName = countryNameFromCode.get(countryCode);
                stateName   = stateNameFromCode.get(countryCode + ","+stateCode);
            }
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

        boolean status = false;
        if (parentId != null)
        {
            Double lat = row.get(2) != null ? ((BigDecimal)row.get(2)).doubleValue() : null;
            Double lon = row.get(3) != null ? ((BigDecimal)row.get(3)).doubleValue() : null;
            
            pStmt.setString(1,  nameStr);
            pStmt.setInt(2,     rankId);
            pStmt.setInt(3,     parentId);
            pStmt.setBoolean(4, true);
            pStmt.setBoolean(5, true);
            pStmt.setInt(6,     geoDef.getId());
            pStmt.setInt(7,     geoDefItemId);
            pStmt.setInt(8,     createdByAgent == null ? 1 : createdByAgent.getId());
            pStmt.setBigDecimal(9, lat > -181 ? new BigDecimal(lat) : null); // Lat
            pStmt.setBigDecimal(10, lon > -181 ? new BigDecimal(lon) : null); // Lon
            
            pStmt.setString(11, StringUtils.isNotEmpty(abbrev) ? abbrev : null); // Abbrev
            
            if (StringUtils.isNotEmpty(isoCode))
            {
                if (isoCode.length() > 8) // For schema 1.7
                {
                    isoCode = isoCode.substring(0, 8);
                }
                pStmt.setString(12, isoCode);
            } else
            {
                log.debug(String.format("%s does not have an ISO Code.", nameStr));
                pStmt.setObject(12, null);
            }
            pStmt.setTimestamp(13, now);
            pStmt.setTimestamp(14, now);
            pStmt.setInt(15, 0);
            status = true;
        }
        
        if (rankId == 200 && isoCode != null && countryNameFromCode.get(isoCode) != null)
        {
            System.out.println("Skipping "+countryName);
            return status;
        }
        
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
                        countryCode);
        
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
                        countryCode);
                
                addDoc(geonameId, 
                        fullName.toString(), 
                        countryName, 
                        stateName, 
                        "St " + countyName.substring(6), 
                        rankId,
                        isoCode,
                        countryCode);
            }
        }
        return status;
    }
    
    /**
     * @param nameStr
     * @param countryCode
     * @param continentCode
     * @param rankId
     * @throws SQLException 
     */
    private void prepareCountry(final String nameStr, 
                               final String countryCode, 
                               final String continentCode, 
                               final int rankId) throws SQLException
    {
        GeographyTreeDefItem item         = geoDef.getDefItemByRank(rankId);
        int                  geoDefItemId = item.getId();
        
        Integer parentId = null;
        
        parentId = contToIdHash.get(continentCode);
        if (parentId == null)
        {
            log.error("No Continent Id for continentCode["+continentCode+"]   Country["+nameStr+"]");
        }
    
        if (parentId != null || rankId == 100)
        {
            pStmt.setString(1, nameStr);
            pStmt.setInt(2, rankId);
            pStmt.setInt(3, parentId);
            pStmt.setBoolean(4, true);
            pStmt.setBoolean(5, true);
            pStmt.setInt(6, geoDef.getId());
            pStmt.setInt(7, geoDefItemId);
            pStmt.setInt(8, createdByAgent == null ? 1 : createdByAgent.getId());
            pStmt.setBigDecimal(9, null); // Lat
            pStmt.setBigDecimal(10, null); // Lon
            
            pStmt.setString(11, countryCode); // Abbrev
            pStmt.setString(12, countryCode); // Abbrev
            pStmt.setTimestamp(13, now);
            pStmt.setTimestamp(14, now);
            pStmt.setInt(15, 0);
        } else
        {
            log.error("parentId is NULL ["+continentCode+"]");
        }
    }
    
    /**
     * Unzips and loads the SQL backup of the geonames database needed for building the full geography tree.
     * @return true if build correctly.
     */
    public boolean loadGeoNamesDB()
    {
        try
        {
            DBConnection currDBConn = DBConnection.getInstance();
            String       dbName     = currDBConn.getDatabaseName();
            DBMSUserMgr.DBSTATUS status = DBMSUserMgr.checkForDB(dbName, currDBConn.getServerName(), itUsername, itPassword);
            
            if (status == DBMSUserMgr.DBSTATUS.missingDB)
            {
                DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
                
                if (dbMgr != null)
                {
                    if (dbMgr.connectToDBMS(itUsername, itPassword, currDBConn.getServerName()))
                    {
                        if (!dbMgr.createDatabase(dbName))
                        {
                            UIRegistry.showLocalizedError("ERROR_CRE_GEODB", dbName);
                        }
                    } else
                    {
                        UIRegistry.showLocalizedError("ERROR_LOGIN_GEODB", dbName);
                    }
                    
                } else
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildFromGeonames.class, new Exception("Couldn't create DBMSMgr"));
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildFromGeonames.class, ex);
        }
        
        File file = new File(XMLHelper.getConfigDirPath("geonames.sql.zip"));
        if (file.exists())
        {
            BackupServiceFactory bsf = BackupServiceFactory.getInstance();
            bsf.setUsernamePassword(itUsername, itPassword);
            
            String dbName = DBConnection.getInstance().getDatabaseName();
            boolean status = bsf.doRestoreBulkDataInBackground(dbName, null, file.getAbsolutePath(), null, null, null, true, false); // true - does it asynchronously, 
            
            buildISOCodes();
            
            // Clear IT Username and Password
            bsf.setUsernamePassword(null, null);
            
            return status;
        }
        return false;
    }
    
    //--------------------------------------------------------------------------------------------------------------------------------
    //-- Lucene
    //--------------------------------------------------------------------------------------------------------------------------------
    
    /**
     * 
     */
    public void initLuceneforReading()
    {
        try
        {
            reader = IndexReader.open(FSDirectory.open(FILE_INDEX_DIR), true);
            
        } catch (CorruptIndexException e)
        {
            e.printStackTrace();
            
        } catch (IOException e)
        {
            e.printStackTrace();
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
        analyzer = new StandardAnalyzer(Version.LUCENE_30, new HashSet<Object>());
        parser   = new QueryParser(Version.LUCENE_30, "name", analyzer);
    }

    /**
     * @param doDeleteIndex
     */
    public void initLuceneForIndexing(final boolean doDeleteIndex)
    {
        try
        {
            if (doDeleteIndex && FILE_INDEX_DIR.exists())
            {
                FileUtils.deleteDirectory(FILE_INDEX_DIR);
            }
            
            if (!FILE_INDEX_DIR.mkdirs())
            {
                // error
            }
            
            Analyzer indexAnalyzer = new StandardAnalyzer(Version.LUCENE_30);
            
            writer = new IndexWriter(FSDirectory.open(FILE_INDEX_DIR), indexAnalyzer, true, IndexWriter.MaxFieldLength.LIMITED);
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
            writer.optimize();
            writer.close();
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
            searcher.close();
            reader.clone();
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
        if (rankId == 400)
        {
            parentNames[2] = StringUtils.remove(parentNames[2], " County");
        }
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<level+1;i++)
        {
            String name = i == 0 ? stripExtrasFromName(parentNames[0]) : parentNames[i];
            if (i > 0) sb.append(" AND ");
            sb.append(String.format("%s:\"%s\"", keys[i], name));
        }
        
        try
        {
            String searchText = sb.toString();
            Query  query      = parser.parse(searchText);
            for (int j=0;j<level;j++) System.out.print("  ");
            log.debug("Searching for: " + query.toString());
    
            Integer              geonameId   = null;
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

                geonameId = chooseGeo(geoId, parentNames[level], level, rankId, parentNames, geonameId);
                
                if (doStopProcessing || (doSkipCountry && rankId > 199))
                {
                    return;
                }
                
                if (geonameId != null)
                {
                    updateGeography(geoId, geonameId, parentNames);
                } else
                {
                    String oldName = BasicSQLUtils.querySingleObj(GEONAME_SQL + geoId);
                    String nbsp = "&nbsp;";
                    tblWriter.log(parentNames[0], 
                                  parentNames[1] != null ? parentNames[1] : nbsp, 
                                  parentNames[2] != null ? parentNames[2] : nbsp, 
                                  oldName, nbsp, nbsp, "Skipped");
                    //System.out.println(String.format("No Match [%s]", parentNames[level]));
                    return;
                }
            } else
            {
                updateGeography(geoId, geonameId, parentNames);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        boolean doDrillDown = doAllCountries[level+1] || (isIndvCountry && doInvCountry[level+1]);
        
        if (doDrillDown) // don't go down further than County level
        {
            String    sql  = "SELECT GeographyID, Name, RankID FROM geography WHERE ParentID = " + geoId;
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
                                 final String[] parentNames)
    {
        boolean autoCommit = true;
        
        String   oldName   = BasicSQLUtils.querySingleObj(GEONAME_SQL + geoId);
        String   sql       = "SELECT asciiname, ISOCode FROM geoname WHERE geonameId = " + geonameId;
        Object[] row       = BasicSQLUtils.queryForRow(sql);
        if (row != null)
        {
            String            name    = (String)row[0];
            String            isoCode = (String)row[1];
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
                }
                
                if (mergeToGeoId != null && doMerge)
                {
                    sql = String .format("UPDATE locality SET GeographyID = %d WHERE GeographyID = %d", mergeToGeoId, geoId); 
                    if (BasicSQLUtils.update(sql) > 0)
                    {
                        sql = "DELETE FROM geography WHERE GeographyID = " + geoId;
                        if (BasicSQLUtils.update(sql) != 1)
                        {
                            log.error("Unable to delete geo id "+geoId);
                        } else
                        {
                            areNodesChanged = true;
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
                
                try
                {
                    pStmt.close();
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
    private Integer chooseGeo(final int       geoId,
                              final String    nameStr,
                              final int       level,
                              final int       rankId,
                              final String[]  parentNames,
                              final Integer   geonameId) throws SQLException
    {
        mergeToGeoId = null;
        
        String whereStr = "";
        switch (rankId)
        {
            case 200:
                whereStr = String.format("WHERE fcode = 'PCLI'");
                break;
                
            case 300: {
                String countryCode = countryCodeFromName.get(parentNames[0]);
                whereStr = String.format("WHERE fcode = 'ADM1' AND country = '%s'", countryCode);
            } break;
                
            case 400: {// County
                String names     = parentNames[0] + ";" + parentNames[1];
                String stateCode =  stateCodeFromName.get(names);
                if (stateCode != null)
                {
                    whereStr = String.format("WHERE fcode = 'ADM2' AND admin1 = '%s'", stateCode);
                } else
                {
                    System.err.println("Missing state code["+names+"]");
                }
            } break;
        }
        
        whereStr += " ORDER BY asciiname";
        
        char   firstChar = nameStr.charAt(0);
        String twoChars  = nameStr.substring(0, 2);
        String title     = "";
        switch (rankId)
        {
            case 200:   // Country
                break;
                
            case 300:   // State 
                title = parentNames[0];
                break;
                
            case 400: { // County
                title = String.format("%s; %s", parentNames[0], parentNames[1]);
            } break;
        }
        
        boolean doStatesOrCounties = doAllCountries[1] || doAllCountries[2] || doInvCountry[1] || doInvCountry[2];
        int btnOpts = doStatesOrCounties ? CustomDialog.OKCANCELAPPLYHELP : CustomDialog.OKCANCELHELP;
        PanelBuilder       pb  = new PanelBuilder(new FormLayout("f:p:g", "p,2px,p,10px,f:p:g,8px,p,4px,p,4px,p,10px,p"));
        final CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), "Choose", true, btnOpts, pb.getPanel());
        dlg.setCloseOnApplyClk(true);
        dlg.setCloseOnHelpClk(true);
        
        int  inx  = -1; // geonameId
        int  inx1 = -1; // first letter
        int  inx2 = -1; // first two letters
        int  i   = 0;
        DefaultListModel dlm = new DefaultListModel();
        final JList list = new JList(dlm);
        Vector<Pair<String, Integer>> coInfoList;

        if (rankId > 200)
        {
            Statement stmt = readConn.createStatement();
            coInfoList = new Vector<Pair<String, Integer>>();
            
            ResultSet rs   = stmt.executeQuery("SELECT geonameId, asciiname FROM geoname " + whereStr);
            while (rs.next())
            {
                String name = rs.getString(2);
                if (StringUtils.isNotEmpty(name))
                {
                    coInfoList.add(new Pair<String, Integer>(name, rs.getInt(1)));
                }
            }
            rs.close();
            stmt.close();
        } else
        {
            coInfoList = countryInfo;
        }
        
        Collections.sort(coInfoList, new Comparator<Pair<String, Integer>>()
        {
            @Override
            public int compare(Pair<String, Integer> p1, Pair<String, Integer> p2)
            {
                return p1.first.compareTo(p2.first);
            }
        });
        
        for (Pair<String, Integer> p : coInfoList)
        {
            String name = p.first;
            char   fc  = name.charAt(0);
            String cmp = name.length() > 1 ? name.substring(0, 2) : null;
            dlm.addElement(name);
            
            if (inx == -1 && geonameId != null && p.second == geonameId)
            {
                inx = i;
            } else if (inx1 == -1 && fc == firstChar)
            {
                inx1 = i;
            }
            if (inx2 == -1 && cmp != null && cmp.equals(twoChars))
            {
                inx2 = i;
            }
                
            i++;
        }
        
        list.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e) 
            {
                if (e.getClickCount() == 2)
                {
                    dlg.getOkBtn().doClick();
                }
            }
        });
        
        final JCheckBox updateNameCB = UIHelper.createCheckBox("Update Name");
        final JCheckBox mergeCB      = UIHelper.createCheckBox("Merge Geographies");
        final JCheckBox addISOCodeCB = UIHelper.createCheckBox("Add ISO Code");

        updateNameCB.setSelected(true);
        mergeCB.setSelected(true);
        addISOCodeCB.setSelected(true);
        
        updateNameCB.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                mergeCB.setEnabled(updateNameCB.isSelected() && rankId == 400);
                if (!updateNameCB.isSelected())
                {
                    mergeCB.setSelected(false);
                }
            }
        });
        CellConstraints cc = new CellConstraints();
        JScrollPane  sb  = UIHelper.createScrollPane(list, true);
        JLabel       lb1 = UIHelper.createLabel(title, SwingConstants.CENTER);
        JLabel       lb2 = UIHelper.createLabel(nameStr, SwingConstants.CENTER);
        
        if (rankId != 200) pb.add(lb1, cc.xy(1, 1));
        pb.add(lb2, cc.xy(1, 3));
        pb.add(sb,  cc.xy(1, 5));
        pb.add(updateNameCB,  cc.xy(1, 7));
        pb.add(mergeCB,       cc.xy(1, 9));
        pb.add(addISOCodeCB,  cc.xy(1, 11));
        
        if (doAllCountries[0])
        {
            pb.add(UIHelper.createLabel(String.format("Progress %d / %d", countryCount, countryTotal)),  cc.xy(1, 13));
        }
        pb.setDefaultDialogBorder();
        
        mergeCB.setEnabled(rankId == 400);
        mergeCB.setSelected(rankId == 400);
        
        lb2.setBackground(Color.WHITE);
        lb2.setOpaque(true);
        
        int selInx = inx > -1 ? inx : (inx2 > -1 ? inx2 : inx1);
        list.setSelectedIndex(selInx);
        list.ensureIndexIsVisible(selInx);
        
        dlg.setOkLabel(UIRegistry.getResourceString("Save"));
        dlg.setCancelLabel(UIRegistry.getResourceString("Skip"));
        dlg.setHelpLabel(UIRegistry.getResourceString("Quit"));
        
        // Optional Depending on States / Countries
        if (doStatesOrCounties)
        {
            dlg.setApplyLabel("Next Country");
        }
        
        UIHelper.centerAndShow(dlg);
        if (!dlg.isCancelled())
        {
            doUpdateName = updateNameCB.isSelected();
            doMerge      = mergeCB.isSelected();
            doAddISOCode = addISOCodeCB.isSelected();

            if (dlg.getBtnPressed() == CustomDialog.OK_BTN)
            {
                String selectedCounty = (String)list.getSelectedValue();
                if (doMerge && rankId == 400)
                {
                    int geoParentId = BasicSQLUtils.getCountAsInt(readConn, "SELECT ParentID FROM geography WHERE GeographyID = "+ geoId);
                    if (geoParentId != -1)
                    {
                        String cName = StringUtils.remove(selectedCounty, " County");
                        String sql = String.format("SELECT GeographyID FROM geography WHERE RankID = 400 AND GeographyID <> %d AND ParentID = %d AND (Name = \"%s\" OR Name = \"%s\")", 
                                geoId, geoParentId, cName, selectedCounty);
                        //log.debug(sql);
                        mergeToGeoId = BasicSQLUtils.getCount(readConn, sql);
                    }
                }
                parentNames[level] = selectedCounty;
                return coInfoList.get(list.getSelectedIndex()).second;
                
            } else if (dlg.getBtnPressed() == CustomDialog.APPLY_BTN)
            {
                doSkipCountry = true;
                return null;
            }
            doStopProcessing = true;
        }
        return null;
    }
    
    /**
     * 
     */
    /**
     * 
     */
    public void startTraversal()
    {
        initLuceneforReading();

        connectToDB();
        
        HashMap<String, String> countryMappings = new HashMap<String, String>();
        //countryMappings.put("US", "United States");
        //countryMappings.put("USA", "United States");
        
        try
        {
            String fullPath = UIRegistry.getAppDataDir() + File.separator + "geo_report.html";
            tblWriter       = new TableWriter(fullPath, "Update Report");
            tblWriter.startTable();
            tblWriter.logHdr("Country", "State", "County", "Old Name", "New Name", "ISO Code", "Action");

            String[] parentNames = new String[3];
            // KUFish - United States
            // Herps - United State 853, USA 1065
            // KUPlants 205
            
            countryCount = 0;
            countryTotal = BasicSQLUtils.getCountAsInt(readConn, "SELECT COUNT(*) FROM geography WHERE RankID = 200");
            String   sql   = "SELECT GeographyID, Name FROM geography WHERE RankID = 200 ORDER BY Name ASC";// AND GeographyID = 205";
            for (Object[] row : BasicSQLUtils.query(readConn, sql))
            {
                doSkipCountry = false;
                
                for (int i=0;i<parentNames.length;i++) parentNames[i] = null;
                
                String countryName = (String)row[1];
                countryName = countryMappings.get(countryName);
                if (countryName == null)
                {
                    countryName = (String)row[1];
                }
                
                int geoId = (Integer)row[0];
                //System.out.println(countryName+"  "+geoId+"  "+doInvCountry[0]+"  "+doIndvCountryId);
                boolean isIndvCountry = doIndvCountryId != null && doIndvCountryId == geoId;
                if (doAllCountries[0] || (doInvCountry[0] && isIndvCountry))
                {
                    //System.out.println(countryName);
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
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
