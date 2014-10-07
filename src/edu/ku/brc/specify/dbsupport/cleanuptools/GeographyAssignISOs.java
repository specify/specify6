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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
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
    private final static String        GEONAME_SQL                  = "SELECT Name FROM geography WHERE GeographyID = ";  
    private final static String        GEONAME_LOOKUP_CONTINENT_SQL = "SELECT geonameid, ISOCode FROM geoname WHERE LOWER(asciiname) = ?";  
    private final static String        GEONAME_LOOKUP_COUNTRY_SQL   = "SELECT geonameid, iso_alpha2 FROM countryinfo WHERE LOWER(name) = ?";  
    private final static String        GEONAME_LOOKUP_STATE_SQL     = "SELECT geonameid, ISOCode FROM geoname WHERE fcode = 'ADM1' AND LOWER(asciiname) = ? AND country = ?";  
    private final static String        GEONAME_LOOKUP_COUNTY_SQL    = "SELECT geonameid, ISOCode FROM geoname WHERE fcode = 'ADM2' AND (LOWER(asciiname) = ? OR LOWER(asciiname) = ?) AND country = ? AND admin1 = ?";  
    public  final static String        GEONAMES_INDEX_DATE_PREF     = "GEONAMES_INDEX_DATE_PREF";
    public  final static String        GEONAMES_INDEX_NUMDOCS       = "GEONAMES_INDEX_NUMDOCS";

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
    
    private TableWriter  tblWriter        = null;
    private boolean      doStopProcessing = false;
    private boolean      doSkipCountry    = false;
    //private boolean      areNodesChanged  = false;
    
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
    
    private boolean[] doAllCountries;
    private boolean[] doInvCountry;
    private Integer   doIndvCountryId = null;
    
    private Vector<Integer> countryIds = new Vector<Integer>();
    private Vector<GeoSearchResultsItem> countryInfo = new Vector<GeoSearchResultsItem>();
    private Vector<GeoSearchResultsItem> luceneResults = new Vector<GeoSearchResultsItem>();
    
    private Integer foundGeonameId = null;

    
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

        luceneSearch = new GeoCleanupFuzzySearch(geoDef);
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
    
//    private boolean checkUniqueness(final int rankId)
//    {
//        String sql = adjustSQL(String.format("SELECT CNT, NM FROM (SELECT COUNT(Name) CNT, Name NM FROM geography WHERE RankID = %d AND GeographyTreeDefID = GEOTREEDEFID GROUP BY Name) T1 WHERE CNT > 1 ORDER BY NM ASC", rankId));
//    	Vector<Object[]> items = BasicSQLUtils.query(sql);
//    	if (items != null && items.size() > 0)
//    	{
//        	sql = adjustSQL(String.format("SELECT Name FROM geographytreedefitem WHERE RankID = %d AND GeographyTreeDefID = GEOTREEDEFID", rankId));
//        	String rankName = BasicSQLUtils.querySingleObj(sql);
//    		UIRegistry.displayInfoMsgDlg("There are " + rankName);
//    		//return false;
//    	}
//    	return true;
//    }
    
//    private boolean isUniquenessOK()
//    {
//    	if (checkUniqueness(100))
//    	{
//    		if (checkUniqueness(200))
//    		{
//    			return true;
//    		}
//    	}
//    	return false;
//    }

    /**
     * @return
     */
    @SuppressWarnings("rawtypes")
    public boolean buildAsync(final int earthId)
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
        
        
        PanelBuilder    pb2 = new PanelBuilder(new FormLayout("8px,p,2px,f:p:g", "p,4px,p,4px,p,8px"));
        spCountriesLbl  = createFormLabel("Country");          // I18N
        spCountriesCmbx = createComboBox(titles);
        spStatesCBX     = createCheckBox("States (Required)"); // I18N
        spCountiesCBX   = createCheckBox("Counties");          // I18N
        
        pb2.add(spCountriesLbl, cc.xy(2, 1));
        pb2.add(spCountriesCmbx, cc.xy(4, 1));
        pb2.add(spStatesCBX,     cc.xyw(1, 3, 4));
        pb2.add(spCountiesCBX,   cc.xyw(1, 5, 4));
        
        spCountriesCmbx.setSelectedIndex(0);
        
        spStatesCBX.setSelected(true);
        spStatesCBX.setEnabled(false);
        spCountiesCBX.setEnabled(false);

        String          rowDef = createDuplicateJGoodiesDef("p", "4px", 7);
        PanelBuilder    pb     = new PanelBuilder(new FormLayout("16px,f:p:g", rowDef));
        
        pb.addSeparator("Continents to be processed", cc.xyw(1, 1, 2));
        pb.add(continentsCBX, cc.xyw(1, 3, 2));
        
        pb.addSeparator("Countries to be processed", cc.xyw(1, 5, 2));
        pb.add(allCountriesRB, cc.xyw(1, 7, 2));
        pb.add(pb1.getPanel(), cc.xyw(2, 9, 1));
        
        pb.add(singleCountryRB, cc.xyw(1, 11, 2));
        pb.add(pb2.getPanel(),  cc.xyw(2, 13, 1));
        
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
        singleCountryRB.addChangeListener(rbChangeListener);
        
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
        
        allCountriesRB.setSelected(true);
        
        dlg.createUI();
        dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // Must be called after 'createUI'
        dlg.getOkBtn().setEnabled(false);
        
        centerAndShow(dlg);
        if (dlg.isCancelled())
        {
            return false;
        }
        
        connectToDB();
        
        doAllCountries  = new boolean[] {countriesCBX.isSelected(), stateCBX.isSelected(), countiesCBX.isSelected(), false};
        doInvCountry    = new boolean[] {spCountriesCmbx.getSelectedIndex() > 0, spStatesCBX.isSelected(), spCountiesCBX.isSelected(), false};
        doIndvCountryId = doInvCountry[0] ? countryIds.get(spCountriesCmbx.getSelectedIndex()) : null;
               
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
    private boolean searchLuceneWithFuzzy(final int       level,
                                          final int       rankId,
                                          final String[]  parentNames,
                                          final int[]     parentRanks,
                                          final String[]  parentISOCodes) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<level+1;i++)
        {
            if (i > 0) sb.append(' ');
            sb.append(parentNames[i]);
        }
        log.debug("["+sb.toString()+"]");        //Query query = new FuzzyQuery(new Term("name", sb.toString()));

        Integer  geonameId = null;
        String   isoCode   = null;
        Document doc       = null;
        HashSet<Integer>     usedIds   = new HashSet<Integer>();
        TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
        try
        {
            Query q = new QueryParser(Version.LUCENE_47, "name", GeoCleanupFuzzySearch.getAnalyzer()).parse(sb.toString());
            luceneSearch.getSearcher().search(q, collector);
        } catch (ParseException e)
        {
            e.printStackTrace();
            return false;
        }
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        for (int i=0;i<hits.length;++i) 
        {
            int docId     = hits[i].doc;
            doc           = luceneSearch.getSearcher().doc(docId);
            System.out.println("Fuzzy: "+i+"  "+hits[i].score+"  ["+doc.get("name")+"][cntry: "+doc.get("country")+" st:"+doc.get("state")+" co:"+
                                doc.get("county")+"] rnk:"+doc.get("rankid")+" gnId: "+doc.get("geonmid"));
            int docRankId = Integer.parseInt(doc.get("rankid"));
            if (rankId == docRankId)
            {
                int geoId = Integer.parseInt(doc.get("geonmid"));
                if (!usedIds.contains(geoId))
                {
                    usedIds.add(geoId);
                    
                    String fullName;
                    
                    String country = doc.get("country");
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
                    } else
                    {
                        fullName = doc.get("name");
                    }
                    
                    isoCodeStr = doc.get("code");
                
                    if (geonameId == null)
                    {
                        geonameId = geoId;
                    }
                    luceneResults.add(new GeoSearchResultsItem(fullName, geonameId, isoCodeStr));
                }
            }
//            int docId     = hits[i].doc;
//            doc           = luceneSearch.getSearcher().doc(docId);
//            System.out.println("Fuzzy: "+i+"  "+hits[i].score+"  "+doc.get("name"));
        }
        
        if (rankId == 400 && !doInvCountry[2])
        {
            return false;
        }
        
        foundGeonameId = geonameId;
        isoCodeStr     = isoCode;
        return isNotEmpty(isoCodeStr);
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
            pStmt = lookupCountryStmt;
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
                pStmt = lookupStateStmt;
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
        
        if (pStmt != null && searchText != null)
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
        foundGeonameId = geonameId;
        isoCodeStr      = isoCode;
        return geonameId != null;
    }
    
    /**
     * @param level
     * @param rankId
     * @param parentNames
     * @param parentRanks
     * @param parentISOCodes
     * @return
     */
//    private boolean searchLuceneWithTerms(final int       level,
//                                          final int       rankId,
//                                          final String[]  parentNames,
//                                          final int[]     parentRanks,
//                                          final String[]  parentISOCodes) throws Exception
//    {
//        String  searchText = null;
//        String  isoCode    = null;
//        Integer geonameId  = null;
//        
//        
//        // Ok, now check Lucence
//        if (geonameId == null)
//        {
//            Query  query;
//            boolean oldWay = false;
//            if (oldWay)
//            {
//                StringBuilder sb = new StringBuilder();
//                for (int i=0;i<level+1;i++)
//                {
//                    String name = i == 0 ? GeoCleanupFuzzySearch.stripExtrasFromName(parentNames[0]) : parentNames[i];
//                    if (name != null && parentRanks[i] > 100)
//                    {
//                        String keyStr = rankToNameMap.get(parentRanks[i]);
//                        if (keyStr != null)
//                        {
//                            if (sb.length() > 0) sb.append(" AND ");
//                            sb.append(String.format("%s:\"%s\"", keyStr, name));
//                        }
//                    }
//                }
//                searchText = sb.toString();
//                query      = luceneSearch.getParser().parse(searchText.replace('/', ' '));
//            } else
//            {
//                BooleanQuery boolQuery = new BooleanQuery();
//                for (int i=0;i<level+1;i++)
//                {
//                    String name = i == 0 ? GeoCleanupFuzzySearch.stripExtrasFromName(parentNames[0]) : parentNames[i];
//                    if (name != null && parentRanks[i] > 100)
//                    {
//                        String keyStr = rankToNameMap.get(parentRanks[i]);
//                        if (keyStr != null)
//                        {
//                            TermQuery t1 = new TermQuery(new Term(keyStr, name));
//                            boolQuery.add(t1, parentRanks[i] == 400 ? Occur.MUST : Occur.SHOULD);
//                        }
//                    }
//                }
//                query = boolQuery;
//                searchText = query.toString();
//            }
//            
//            if (isNotEmpty(searchText))
//            {
//                //for (int j=0;j<level;j++) System.out.print("  ");
//                log.debug("Searching for: " + query.toString());
//        
//                HashSet<Integer>     usedIds     = new HashSet<Integer>();
//                Document             doc         = null;
//                int                  hitsPerPage = 500;
//                TopScoreDocCollector collector   = TopScoreDocCollector.create(hitsPerPage, true);
//                luceneSearch.getSearcher().search(query, collector);
//                ScoreDoc[] hits = collector.topDocs().scoreDocs;
//                for (int i=0;i<hits.length;++i) 
//                {
//                    int docId     = hits[i].doc;
//                    doc           = luceneSearch.getSearcher().doc(docId);
//                    int docRankId = Integer.parseInt(doc.get("rankid"));
//                    
//                    System.out.println(String.format("%s -> rank: %d = %d", doc.get("name"), rankId, docRankId));
//                    if (rankId == docRankId)
//                    {
//                        int geoId = Integer.parseInt(doc.get("geonmid"));
//                        if (!usedIds.contains(geoId))
//                        {
//                            usedIds.add(geoId);
//                            isoCode   = doc.get("code");
//                            
//                            if (geonameId == null)
//                            {
//                                geonameId = geoId;
//                            }
//    
//                            StringBuilder sb = new StringBuilder();
//                            String[] names = {doc.get("country"), doc.get("state"), doc.get("county")};
//                            for (String nm : names)
//                            {
//                                if (nm != null)
//                                {
//                                    if (sb.length() > 0) sb.append(", ");
//                                    sb.append(nm);
//                                }
//                            }
//                            luceneResults.add(new GeoSearchResultsItem(sb.toString(), geonameId, isoCode));
//                        }
//                    }
//                }
//            }
//        }
//        foundGeonameId = geonameId;
//        isoCodeStr      = isoCode;
//        return geonameId != null;
//    }

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
        String nbsp       = "&nbsp;";

        // Check the database directly
        if (isEmpty(geoISOCode))
        {
            try
            {
                foundGeonameId = null;
                luceneResults.removeAllElements();
                
                boolean foundMatch = searchGeonameForMatch(level, rankId, parentNames, parentRanks, parentISOCodes);
                System.out.println(parentNames+"  foundMatch: "+foundMatch);
                if (!foundMatch)
                {
                    foundMatch = searchLuceneWithFuzzy(level, rankId, parentNames, parentRanks, parentISOCodes);
    
                    Integer geonameId = chooseGeo(geoId, parentNames[level], level, rankId, parentNames, parentRanks, foundGeonameId);
                    
                    if (doStopProcessing || (doSkipCountry && rankId > 199))
                    {
                        String oldName = querySingleObj(GEONAME_SQL + geoId);
                        tblWriter.log(parentNames[0], 
                                      parentNames[1] != null ? parentNames[1] : nbsp, 
                                      parentNames[2] != null ? parentNames[2] : nbsp, 
                                      oldName, nbsp, nbsp, "Skipped");
                        return;
                    }
        
                    if (geonameId != null)
                    {
                        parentISOCodes[level] = isoCodeStr;
                        updateGeography(geoId, geonameId, parentNames, isoCodeStr);
                    } else
                    {
                    	boolean didUpdate = false;
                    	if (isNotEmpty(this.isoCodeStr))
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
                    System.out.println(String.format("Matched [%s] [%s]", parentNames[level], isoCodeStr));
                    parentISOCodes[level] = isoCodeStr;
                    updateGeography(geoId, foundGeonameId, parentNames, isoCodeStr);
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
            String     wStr = rankId == 400 ? "GeographyCode IS NULL" : ""; 
            String    sql   = String.format("SELECT GeographyID, Name, RankID, GeographyCode FROM geography WHERE %s ParentID = %d ORDER BY RankID, Name", wStr, geoId);
            Statement stmt  = readConn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            while (rs.next())
            {
                int    childGeoId  = rs.getInt(1);    // Get Child's Id
                String name        = rs.getString(2); // Get Child's Name
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
        	boolean           isAlt   = (row == null && isNotEmpty(countryCode));
            String            name    = isAlt ? parentNames[0] : (String)row[0];
            String            isoCode = isAlt ? countryCode : (isNotEmpty(isoCodeStr) ? isoCodeStr : (String)row[1]);
            PreparedStatement pStmt   = null;
            try
            {
                autoCommit = updateConn.getAutoCommit();
                String pre  = "UPDATE geography SET ";
                String post = ", ModifiedByAgentID=?, TimestampModified=? WHERE GeographyID=?";
                int inx     = 2; 
                if (doUpdateName && doAddISOCode)
                {
                    pStmt = updateConn.prepareStatement(pre+"LOWER(Name)=?, GeographyCode=?"+post);
                    pStmt.setString(1, name.toLowerCase());
                    pStmt.setString(2, isoCode);
                    inx = 3;
                    
                } else if (doUpdateName)
                {
                    pStmt = updateConn.prepareStatement(pre+"LOWER(Name)=?"+post);
                    pStmt.setString(1, name.toLowerCase());
                    
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
                              final int[]     parentRanks,
                              final Integer   geonameId) throws SQLException
    {
        // Convert RankID to level
//        if (rankId > 200)
//        {
//            int levelFromRankId = (rankId / 100) - 1;
//            if (levelFromRankId != level)
//            {
//                String msg = String.format("The geography name '%s' appears to have the wrong rank\nYou may want to investigate.\nCurrent Rank is %d processed Rank is %d", nameStr, rankId, level * 100);
//                showError(msg);
//                return null;
//            }
//        }
        
        GeoChooserDlg dlg = new GeoChooserDlg(nameStr, rankId, level, parentNames, parentRanks, geonameId, 
                                              stCntXRef, countryInfo, doAllCountries, doInvCountry, 
                                              readConn, geographyTotal);
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
        this.isoCodeStr   = null;
        this.doAddISOCode = false;

        if (dlg.getBtnPressed() != QUIT_BTN) 
        {
            if (dlg.getBtnPressed() == SAVE_BTN)
            {
                doUpdateName = dlg.getUpdateNameCB().isSelected();
                doMerge      = dlg.getMergeCB() != null ? dlg.getMergeCB().isSelected() : false;
                doAddISOCode = dlg.getAddISOCodeCB().isSelected();
                isoCodeStr   = dlg.getSelectedISOValue();

                String selectedCounty = dlg.getSelectedListValue();
                if (doMerge && rankId == 400)
                {
                    int geoParentId = getCountAsInt(readConn, "SELECT ParentID FROM geography WHERE GeographyID = "+ geoId);
                    if (geoParentId != -1)
                    {
                        try
                        {
                            String cName = remove(selectedCounty, " County");
                            PreparedStatement pStmt = updateConn.prepareStatement("SELECT GeographyID FROM geography WHERE RankID = 400 AND GeographyID <> ? AND ParentID = ? AND (LOWER(Name) = ? OR LOWER(Name) = ?");
                            pStmt.setInt(1,    geoId);
                            pStmt.setInt(2,    geoParentId);
                            pStmt.setString(3, cName.toLowerCase());
                            pStmt.setString(4, selectedCounty.toLowerCase());
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
                parentRanks[level] = rankId;
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
    private void startTraversal()
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
        
        if (!luceneSearch.initLuceneforReading())
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
            String firstCol = continentsCBX.isSelected() ? "Continent / " : "";
            tblWriter.logHdr(firstCol+"Country", "State", "County", "Old Name", "New Name", "ISO Code", "Action");

            // KUFish - United States
            // Herps - United State 853, USA 1065
            // KUPlants 205
            
            totalUpdated = 0;
            totalMerged  = 0;
            
            //-------------------
            // Do Continent
            //-------------------
            String[] parentNames    = new String[3];
            int[]    parentRanks    = new int[3];
            String[] parentISOCodes = new String[3];
            geographyTotal = getCountAsInt(readConn, adjustSQL("SELECT COUNT(*) FROM geography WHERE GeographyTreeDefID = GEOTREEDEFID AND GeographyCode IS NULL"));
            String sql   = adjustSQL("SELECT GeographyID, Name, RankID, GeographyCode FROM geography WHERE GeographyTreeDefID = GEOTREEDEFID AND GeographyCode IS NULL AND RankID = 100 ORDER BY Name ASC");
            
            for (Object[] row : query(readConn, sql))
            {
                for (int i=0;i<parentNames.length;i++)
                {
                    parentNames[i] = null;
                    parentRanks[i] = -1;
                    parentISOCodes[i] = null;
                }
                
                int    geoId         = (Integer)row[0];
                String continentName = (String)row[1];
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
            }            
            
            if (!doStopProcessing)
            {
                //-------------------
                // Do Country
                //-------------------
                geographyTotal = getCountAsInt(readConn, "SELECT COUNT(*) FROM geography WHERE GeographyCode IS NULL AND RankID = 200");
                sql  = "SELECT GeographyID, Name, RankID, GeographyCode FROM geography WHERE ";
                sql += doIndvCountryId != null ? "(GeographyCode IS NULL OR GeographyID = %d) AND" : "";
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
                        System.out.println(countryName);
                        parentNames[0] = countryName;
                        parentRanks[0] = 200;
                        parentISOCodes[0] = isoCode;
                        findGeo(geoId, isoCode, 0, 200, parentNames, parentRanks, parentISOCodes, isIndvCountry);
                        if (doStopProcessing)
                        {
                            break;
                        }
                    }
                }
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
            luceneSearch.doneSearching();
            
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
