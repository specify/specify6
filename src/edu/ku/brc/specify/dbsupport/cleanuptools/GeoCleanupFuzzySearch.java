/* Copyright (C) 2012, University of Kansas Center for Research
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
import static edu.ku.brc.ui.UIHelper.centerAndShow;
import static edu.ku.brc.ui.UIRegistry.getAppDataDir;
import static edu.ku.brc.ui.UIRegistry.showError;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

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
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.dbsupport.BuildFromGeonames;
import edu.ku.brc.ui.ProgressFrame;
/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 21, 2014
 *
 */
public class GeoCleanupFuzzySearch
{
    private static final Logger  log = Logger.getLogger(GeoCleanupFuzzySearch.class);
    
    public final static Pattern kSpecialCharsPattern = Pattern.compile("[,.;!?(){}\\[\\]<>%\\-+*&$@\\=\\/]"); //store it somewhere so 

    
    private static String[] replaceNames = new String[] {"Republic of Korea", "South Korea", 
                                                         "Russian", "Russia", 
                                                         "United States of America", "United States",
                                                         "Portuguese", "Portugal",
                                                         };
    
    private static String[] removeStrs = new String[] {
        "Islamic Republic", "Republics", "Republic", "Islamic", "Independent",
        "Federal", "Democratic", "Federation", "Commonwealth", 
        "Principality", "Federative", "Plurinational", "Socialist",  
        "Co operative", "Sultanate", "People's", "Territory", "Hashemite", "Country",
    };

    private static String[] replaceStrs = new String[] {"State of ", "Union of ", "Kingdom of ", "Arab ", 
                                                        " of ", "of ", " the ", "the ", " The ", "The ", };

    private static boolean isDoingTesting = false;
    
    public  final static String        GEONAMES_INDEX_DATE_PREF     = "GEONAMES_INDEX_DATE_PREF";
    public  final static String        GEONAMES_INDEX_NUMDOCS       = "GEONAMES_INDEX_NUMDOCS";
    
    private File         FILE_INDEX_DIR;
    
    private HashMap<String, String> countryLookupMap = new HashMap<String, String>();
    private HashMap<String, String> stateLookupMap   = new HashMap<String, String>();
    
    
    private Connection                 readConn = null;
    
    private static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
    private IndexReader   reader;
    private IndexSearcher searcher;
    private IndexWriter   writer;
    private QueryParser   parser;
    
    private GeographyTreeDef           geoDef;
    private ProgressFrame              frame;
    private boolean                    areNodesChanged = false;
    
    private ArrayList<Object>          rowData = new ArrayList<Object>();
    private StateCountryContXRef       stCntXRef;
    
    /**
     * @param geoDef
     * @param frame
     */
    public GeoCleanupFuzzySearch(final GeographyTreeDef geoDef)
    {
        super();
        
        readConn = DBConnection.getInstance().createConnection();
        
//        String roundTrip;
//        try
//        {
//            String altNames = BasicSQLUtils.querySingleObj("select alternatenames from geoname where geonameId = 921929");
//            byte[] utf8Bytes = altNames.getBytes();
//            roundTrip = new String(utf8Bytes, "UTF8");
//            System.out.println("roundTrip = " + roundTrip);
//            roundTrip = new String(utf8Bytes);
//            System.out.println("roundTrip = " + roundTrip);
//            
//            utf8Bytes = altNames.getBytes("UTF8");
//            roundTrip = new String(utf8Bytes, "UTF8");
//            System.out.println("roundTrip = " + roundTrip);
//            roundTrip = new String(utf8Bytes);
//            System.out.println("roundTrip = " + roundTrip);
//        } catch (UnsupportedEncodingException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        //dbConn.close();
        
        //
        if (false) // debug
        {
            HashSet<String> namesSet = new HashSet<String>();
            Vector<Object> nmRows = BasicSQLUtils.querySingleCol("select asciiname from geoname where fcode LIKE '%PCL%' ORDER BY asciiname");
            for (Object nm : nmRows)
            {
                String newName = stripExtrasFromName((String)nm);
                System.out.println("["+newName+"]\t\t["+nm+"]");
                namesSet.add(newName);
            }
    
            nmRows = BasicSQLUtils.querySingleCol("SELECT Name FROM geography WHERE RankID = 200 ORDER BY Name");
            for (Object nm : nmRows)
            {
                String newName = stripExtrasFromName((String)nm);
                if (!namesSet.contains(newName))
                {
                    System.out.println("Not Found: ["+newName+"]\t\t["+nm+"]");
                }
            }
        }
        
        Vector<Object[]> rows = BasicSQLUtils.query("SELECT iso_alpha2, name FROM countryinfo");
        for (Object[] row : rows)
        {
            countryLookupMap.put(row[0].toString(), row[1].toString());
        }
        
        rows = BasicSQLUtils.query("SELECT asciiname, country, admin1 FROM geoname WHERE fcode = 'ADM1'");
        for (Object[] row : rows)
        {
            stateLookupMap.put(row[1].toString()+"_"+row[2].toString(), row[0].toString());
        }

        this.geoDef = geoDef;
        
        // ZZZ For Release
        if (isDoingTesting)
        {
            FILE_INDEX_DIR = new File("/Users/rods/Downloads/lucene/geonames-index"); // Debug Only
        } else
        {
            String dirPath = getAppDataDir() + File.separator + "geonames-index";
            FILE_INDEX_DIR = new File(dirPath);
        }
     }    

    /**
     * @return the analyzer
     */
    public static StandardAnalyzer getAnalyzer()
    {
        return analyzer;
    }

    /**
     * @return the parser
     */
    public QueryParser getParser()
    {
        return parser;
    }

    /**
     * @return the reader
     */
    public IndexReader getReader()
    {
        return reader;
    }

    /**
     * @return the searcher
     */
    public IndexSearcher getSearcher()
    {
        return searcher;
    }
    
    public boolean shouldIndex()
    {
        AppPreferences localPrefs            = AppPreferences.getLocalPrefs();
        boolean        needsIndexing         = true;
        Long           lastGeoNamesBuildTime = BuildFromGeonames.getLastGeonamesBuiltTime();
        Long           lastIndexBuild        = localPrefs != null ? localPrefs.getLong(GEONAMES_INDEX_DATE_PREF, null) : null;
        if (!initLuceneforReading() || lastIndexBuild == null || lastGeoNamesBuildTime == null || !lastIndexBuild.equals(lastGeoNamesBuildTime))
        {
            if (getReader() != null)
            {
                Integer numDocs = localPrefs != null ? localPrefs.getInt(GEONAMES_INDEX_NUMDOCS, null) : null;
                if (numDocs != null)
                {
                    //System.out.println(String.format("%d %d", getReader().numDocs(), numDocs));
                    needsIndexing = getReader().numDocs() != numDocs;
                }
            }
            doneSearching();
        } else
        {
            needsIndexing = false;
        }

        return needsIndexing;
    }

    /**
     * Close the index.
     * @throws java.io.IOException when exception closing
     */
//    public void closeIndex() throws IOException
//    {
//        writer.close();
//    }

    /**
     * @param name
     * @return
     */
    protected static String stripExtrasFromName(final String name)
    {
        String sName = name;
        for (int i=0;i<replaceNames.length;i+=2)
        {
            sName = StringUtils.replace(sName.trim(), replaceNames[i], replaceNames[i+1]);
        }
        for (String extraStr : removeStrs)
        {
            sName = StringUtils.remove(sName.trim(), extraStr);
        }
        for (String replStr : replaceStrs)
        {
            sName = StringUtils.replace(sName.trim(), replStr, " ");
        }
        sName = kSpecialCharsPattern.matcher(sName).replaceAll(" ").trim();

        while (sName.contains("  "))
        {
            sName = StringUtils.replace(sName.trim(), "  ", " ");
        }
        return sName;
    }
    
//    private String lookupStateName(final String countryISOCode, final String stateISOCode)
//    {
//        Vector<Object> names = BasicSQLUtils.querySingleCol(String.format("SELECT name FROM geoname WHERE country = '%s' AND admin1 = '%s'", countryISOCode, stateISOCode));
//        return names.size() > 0 ? names.get(0).toString() : "";
//    }

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
            //connectToDB();
            
            stmt = readConn.createStatement();
            
            int cnt;
            
            initLuceneForIndexing(true);
            
            //////////////////////
            // Continent
            //////////////////////
            cnt    = 0;
            String cntSQL = "SELECT COUNT(*) ";
            int    totCnt = getCountAsInt(cntSQL + "FROM continentcodes");
            if (frame != null) frame.setProcess(0, totCnt);
            
            String sqlStr = "SELECT geonameId, name, code from continentcodes";
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


            sqlStr = "SELECT c.geonameId, c.Name, Latitude, Longitude, iso_alpha2, iso_alpha3, alternatenames " + post;
            //System.out.println(sqlStr);
            rs = stmt.executeQuery(sqlStr);
            while (rs.next() && isOK)
            {
                String countryCode = rs.getString(5);
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
            
            setProgressDesc("Preparing States...");  // I18N
            
            //////////////////////
            // States
            //////////////////////
            cnt    = 0;
            post   = "FROM geoname WHERE asciiname IS NOT NULL AND LENGTH(asciiname) > 0 AND fcode = 'ADM1' ORDER BY asciiname";
            totCnt = getCountAsInt(cntSQL + post);
            inc    = totCnt / 20;
            if (frame != null) frame.setProcess(0, 100);

            sqlStr = "SELECT geonameId, asciiname, latitude, longitude, country, admin1, ISOCode, alternatenames ";
            rs     = stmt.executeQuery(sqlStr + post);
            while (rs.next())
            {
                isOK = buildDoc(rs, 300, earthId);
                cnt++;
                if (frame != null && cnt % inc == 0) frame.setProcess(cnt / 20);
            }
            rs.close();
            
            if (!isOK) return false;
            
            setProgressDesc("Preparing Counties...");  // I18N
            
            //////////////////////
            // County
            //////////////////////
            cnt    = 0;
            post   = "FROM geoname WHERE fcode = 'ADM2' ORDER BY asciiname";
            totCnt = getCountAsInt(cntSQL + post);
            inc    = totCnt / 20;
            if (frame != null) frame.setProcess(0, 100);
            
            sqlStr = "SELECT geonameId, asciiname, latitude, longitude, country, admin1, admin2, ISOCode, alternatenames ";
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
        String alternames   = row.get(row.size()-1).toString();
        String[] altNames   = StringUtils.isNotEmpty(alternames) ? StringUtils.split(alternames, ',') : null;
        
        if (rankId == 100) // Continents
        {
            isoCode  = row.get(4).toString();
            
        } else if (rankId == 200) // Country
        {
            countryName = nameStr;
            countryCode = row.get(4).toString();
            isoCode3    = row.get(5).toString(); 
//            String continentCode = stCntXRef.countryCodeToContinentCode(countryCode);
            
            isoCode = countryCode;
            
//            if (continentCode == null)
//            {
//                StringBuilder sb = new StringBuilder("No Continent Code ["+continentCode+"]:\n");
//                for (int i=0;i<row.size();i++)
//                {
//                    sb.append(i+" - "+row.get(i)+"\n");
//                }
//                log.error(sb.toString());
//            }
            
        } else if (rankId == 300) // State
        {
            stateName   = nameStr;
            //System.out.println(geonameId+"  "+stateName);
            countryCode = row.get(4).toString();
            isoCode     = row.get(6).toString();
            countryName = stCntXRef.countryCodeToName(countryCode);
            
        } else if (rankId == 400) // County
        {
//            for (int i=1;i<row.size();i++)
//            {
//                System.out.println(i+" "+row.get(i));
//            }
            
            countryName = countryLookupMap.get(row.get(4).toString());
            //stateName   = lookupStateName(row.get(4).toString(), row.get(5).toString());
            stateName   = stateLookupMap.get(row.get(4).toString()+"_"+row.get(5).toString());
            
//            if (stateName != null && stateName.equalsIgnoreCase("Iowa"))// && countyName != null && countyName.equalsIgnoreCase("Fayette Country"))
//            {
//                System.out.println("["+countryName+"]["+stateName+"]["+countyName+"]");
//            }

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
        
        String fullNameString = fullName.toString().trim();

        
        // Lucene Index
        status = addDoc(geonameId, 
                        fullNameString, 
                        countryName, 
                        stateName, 
                        countyName, 
                        rankId,
                        isoCode,
                        countryCode,
                        isoCode3);
        
        if (altNames != null)
        {
            //int cnt = 0;
            for (String altName : altNames)
            {
//                switch (rankId)
//                {
//                    case 200: countryName = altName;
//                    break;
//                    case 300: stateName = altName;
//                    break;
//                    case 400: countyName = altName;
//                    break;
//                }
//
//                if (countryName != null && countryName.equalsIgnoreCase("costa rica"))
//                {
//                    System.out.println("["+countryName+"]["+stateName+"]["+countyName+"]");
//                }
//                if (stateName != null && stateName.equalsIgnoreCase("Iowa"))// && countyName != null && countyName.equalsIgnoreCase("Fayette Country"))
//                {
//                    System.out.println("["+countryName+"]["+stateName+"]["+countyName+"]");
//                }
                String docName = altName.contains(fullNameString) ? altName : (altName + " " + fullNameString);
                status = addDoc(geonameId,
                        docName,
                        countryName,
                        stateName,
                        countyName,
                        rankId,
                        isoCode,
                        countryCode,
                        isoCode3);
                //cnt++;
                //if (cnt > 10) break;
            }
        }
        
        if (StringUtils.isNotEmpty(countyName))
        {
            String lwCty = countyName.toLowerCase();
            if (lwCty.startsWith("saint ") && lwCty.length() > 7)
            {
                addDoc(geonameId, 
                        fullNameString, 
                        countryName, 
                        stateName, 
                        "St. " + countyName.substring(6), 
                        rankId,
                        isoCode,
                        countryCode,
                        null);
                
                addDoc(geonameId, 
                        fullNameString, 
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
        String strippedName = stripExtrasFromName(fullName);
        doc.add(new TextField("name", strippedName, Store.YES));
//        if (rankId == 200)
//        {
//            countryInfo.add(new Triple<String, Integer, String>(countryName, geonmId, countryCode));
//            //System.out.println(">> "+stripExtrasFromName(fullName)+" ["+countryName+"]");
//        }
        
        if (countryName != null)
        {
            doc.add(new TextField("country", countryName, Field.Store.YES));
            //doc.add(new TextField("country", replace(countryName.toLowerCase(), "-", " "), Field.Store.NO));
        }
        if (stateName != null)
        {
            doc.add(new TextField("state", stateName, Field.Store.YES));
            //doc.add(new TextField("state", replace(stateName.toLowerCase(), "-", " "), Field.Store.NO));
        }
        if (countyName != null)
        {
            String cName = countyName;//StringUtils.remove(countyName.toLowerCase(), " County");
            //cName = replace(cName, "-", " ");
            doc.add(new TextField("county", cName, Field.Store.YES));
        }
        doc.add(new TextField("rankid",  Integer.toString(rankId),  Field.Store.YES));
        doc.add(new TextField("geonmid", Integer.toString(geonmId), Field.Store.YES));
        doc.add(new StringField("code",  fullISOStr != null ? fullISOStr : "", Field.Store.YES));
        
        if (isoCode3 != null) doc.add(new TextField("code3",    isoCode3, Field.Store.NO));
        
        try
        {
            //System.out.println(String.format("%s [%s]", nameStr, fullISOStr));
            //System.out.println(String.format("%d - %s (%s)", geonmId, fullName, fullISOStr));
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
     * 
     */
    public void doneIndexing()
    {
        try
        {
            System.out.println("Number of Lucene Doc: "+writer.numDocs());
            
            if (!isDoingTesting)
            {
                AppPreferences.getLocalPrefs().putInt(GEONAMES_INDEX_NUMDOCS, writer.numDocs());
                
                Long lastGeoNamesBuildTime = BuildFromGeonames.getLastGeonamesBuiltTime();
                if (lastGeoNamesBuildTime != null)
                {
                    AppPreferences.getLocalPrefs().putLong(GEONAMES_INDEX_DATE_PREF, lastGeoNamesBuildTime);
                }
            }
            //writer.commit();
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
    * @param earthId
    * @return true if ok
    */
   private boolean startIndexingProcessSync(final int earthId, final ProgressFrame frame)
   {
       this.frame = frame;
       
        setProgressDesc("Build Geography Names cross-reference..."); // I18N
        stCntXRef = new StateCountryContXRef(readConn);
        boolean isOK = stCntXRef.build();
        if (isOK)
        {
            setProgressDesc("Creating searchable index..."); // I18N
            isOK = buildLuceneIndex(earthId);
        }
        return isOK;
   }

     /**
     * @param earthId
     * @param cl
     */
    public void startIndexingProcessAsync(final int earthId, final ProgressFrame frame, final ChangeListener cl)
    {
        this.frame = frame;
        
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
                
                // NOTE: need to check here that everything built OK
                cl.stateChanged(new ChangeEvent((Boolean)isOK));
            }
        };
        worker.execute();
    }
       
    /**
     * 
     */
    public void doneSearching()
    {
        try
        {
            if (analyzer != null) analyzer.close();
            if (reader != null) reader.close();
            
            analyzer = null;
            searcher = null;
            reader   = null;
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        areNodesChanged = false;
        if (areNodesChanged && geoDef != null)
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
    
    //--------------------------------------------------------------------------------------------------------------------------------
    //-- Lucene Searching
    //--------------------------------------------------------------------------------------------------------------------------------
    
    /**
     * @return false if index doesn't exist
     */
    public boolean initLuceneforReading()
    {
        if (!FILE_INDEX_DIR.exists()) return false;
        
        try
        {
            reader = DirectoryReader.open(FSDirectory.open(FILE_INDEX_DIR));
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
            
            Analyzer          indexAnalyzer = new StandardAnalyzer(Version.LUCENE_47);
            IndexWriterConfig config        = new IndexWriterConfig(Version.LUCENE_47, indexAnalyzer);
            writer = new IndexWriter(FSDirectory.open(FILE_INDEX_DIR), config);
            //writer.prepareCommit();
            
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
    
    public static void main(String[] args) throws IOException
    {
        
        //For Debug
        String       connectStr = "jdbc:mysql://localhost/testfish";
        String       username   = "root";
        String       password   = "root";
        DBConnection dbConn;

        // Debug        
        dbConn = DBConnection.getInstance();
        dbConn.setConnectionStr(connectStr);
        dbConn.setDatabaseName("stats");
        dbConn.setUsernamePassword(username, password);
        dbConn.setDriver("com.mysql.jdbc.Driver");
        
        boolean doBuildIndex = false;

        //String indexLocation = "/Users/rods/Downloads/lucene/geonames-index";
        String indexLocation = "/Users/rods/Documents/Specify/geonames-index";

        GeoCleanupFuzzySearch indexer = null;
        try
        {
            indexer = new GeoCleanupFuzzySearch(null);
            if (doBuildIndex)
            {
                indexer.startIndexingProcessSync(1, null);
            }

        } catch (Exception ex)
        {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }

        // ===================================================
        // after adding, we always have to call the
        // closeIndex, otherwise the index is not created
        // ===================================================
        // indexer.closeIndex();

        // =========================================================
        // Now search
        // =========================================================
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation)));
        IndexSearcher searcher = new IndexSearcher(reader);

        
        boolean doFuzzy = false;
        boolean doTerm = false;
        boolean doParse = true;
        
        if (doFuzzy)
        {
            System.out.println("-------------------------- Fuzzy -----------------------");
            String[] searchStrs = { 
                    "Comoro Islands", 
                    "Solomon", 
                    "united states iowa",
                    "germany brandenburg", 
                    "bulgaria sofia", 
                    "costa rica alajuela",
                    "costa rica cartago", 
                    "costa rica alajuela", 
                    "canada newfoundland",
                    "mexico campeche", 
                    "australia ashmore and cartier islands", 
                    "fiji lau",
                    "fiji lomaiviti", 
                    "guam agana", 
                    "germany Lower Saxony",
                    "germany Saxony",
                    "germany Sachsen Anhalt", 
                    "germany Sachsen-Anhalt", 
                    "germany Land Sachsen-Anhalt", 
                    "united states iowa,Fayette",
                    "united states iowa Fayette County",
                    "Argentina Buenos Aires",
                    "buenos aires argentina ",
                    };

            for (String searchText : searchStrs)
            {
                try
                {
                    Query query = new FuzzyQuery(new Term("name", searchText));

                    TopDocs docs = searcher.search(query, 10);
                    ScoreDoc[] hits = docs.scoreDocs;
                    System.out.println(searchText + " -> Hits " + hits.length + " hits ["+query+"]");
                    for (int i = 0; i < hits.length; ++i)
                    {
                        int docId = hits[i].doc;
                        Document d = searcher.doc(docId);
                        System.out.println((i + 1) + ". " + d.get("name") + " score="
                                + hits[i].score);
                    }
                } catch (Exception e)
                {
                    System.out.println("Error searching  " + searchText + " : " + e.getMessage());
                }
            }
        }
        
        if (doTerm)
        {
            System.out.println("-------------------------- Terms -----------------------");
            String[] searchStrs = { 
                    "Comoro Islands", 
                    "Solomon", 
                    "united states,iowa",
                    "germany,brandenburg", 
                    "bulgaria,sofia", 
                    "costa rica,alajuela",
                    "costa rica,cartago", 
                    "costa rica,alajuela", 
                    "canada,newfoundland",
                    "mexico,campeche", 
                    "australia,ashmore and cartier islands", 
                    "fiji,lau",
                    "fiji,lomaiviti", 
                    "guam,agana", 
                    "germany,Lower Saxony", 
                    "germany,Saxony",
                    "germany,Sachsen Anhalt", 
                    "germany,Sachsen-Anhalt", 
                    "germany,Land Sachsen-Anhalt", 
                    "united states,iowa,Fayette",
                    "united states,iowa,Fayette County",
                    "argentina,buenos aires",
                    "Argentina,Buenos Aires",
                    };

            for (String searchText : searchStrs)
            {
                try
                {
                    String[] tokens = StringUtils.split(searchText, ',');
                    BooleanQuery query = new BooleanQuery();
                    
                    TermQuery t1 = new TermQuery(new Term("country", tokens[0]));
                    t1.setBoost(0.2f);
                    query.add(t1, Occur.SHOULD);

                    if (tokens.length > 1)
                    {
                        TermQuery t2 = new TermQuery(new Term("state", tokens[1]));
                        t2.setBoost(0.4f);
                        query.add(t2, Occur.SHOULD);
                    }

                    if (tokens.length > 2)
                    {
                        TermQuery t3 = new TermQuery(new Term("county", tokens[2]));
                        t3.setBoost(0.8f);
                        query.add(t3, Occur.MUST);
                    }

                    TopDocs docs = searcher.search(query, 20);
                    ScoreDoc[] hits = docs.scoreDocs;
                    System.out.println(searchText + " -> Hits " + hits.length + " hits ["+query+"]");
                    for (int i = 0; i < hits.length; ++i)
                    {
                        int docId = hits[i].doc;
                        Document d = searcher.doc(docId);
                        System.out.println((i + 1) + ". " + d.get("name") + " score="
                                + hits[i].score);
                    }
                } catch (Exception e)
                {
                    System.out.println("Error searching  " + searchText + " : " + e.getMessage());
                }
            }
        }

        if (doParse)
        {
            System.out.println("-------------------------- Parsing -----------------------");
            String[] searchStrs = {
                    "Comoro Islands",
                    "Bahamas Elbow Bank",
//                    "Solomon",
//                    "united states iowa",
//                    "germany brandenburg",
//                    "bulgaria sofia",
//                    "costa rica alajuela",
//                    "costa rica cartago",
//                    "costa rica alajuela",
//                    "canada newfoundland",
//                    "mexico campeche",
//                    "australia ashmore and cartier islands",
//                    "fiji lau",
//                    "fiji lomaiviti",
//                    "guam agana",
//                    "germany Lower Saxony",
//                    "germany Saxony",
//                    "germany Sachsen Anhalt",
//                    "germany Sachsen-Anhalt",
//                    "germany Land Sachsen-Anhalt",
//                    "united states iowa,Fayette",
//                    "united states iowa Fayette County",
//                    "Argentina Buenos Aires",
//                    "buenos aires argentina "
            };

            for (String searchText : searchStrs)
            {
                try
                {
                    TopScoreDocCollector collector = TopScoreDocCollector.create(5, true);
                    Query q = new QueryParser(Version.LUCENE_47, "name", analyzer).parse(searchText);
                    searcher.search(q, collector);
                    ScoreDoc[] hits = collector.topDocs().scoreDocs;
                    if (hits != null)
                    {
                        System.out.println(searchText + " -> Hits " + hits.length + " hits.");
    
                        // System.out.println("For: ["+seatchText+"] Found " + hits.length + " hits.");
                        for (int i = 0; i < hits.length; ++i)
                        {
                            int docId = hits[i].doc;
                            Document d = searcher.doc(docId);
                            if (d != null)
                            {
                                System.out.println((i + 1) + ". " + d.get("name") + " score="
                                        + hits[i].score);
                            } else
                            {
                                System.err.println("Doc was null searching  " + searchText);
                            }
                        }
                    } else
                    {
                        System.err.println("Hits was null searching  " + searchText);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                    System.err.println("Error searching  " + searchText + " : " + e.getMessage());
                }
            }
        }

    }

}
