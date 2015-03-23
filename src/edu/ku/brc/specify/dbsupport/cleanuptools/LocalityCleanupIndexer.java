/* Copyright (C) 2015, University of Kansas Center for Research
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

import static edu.ku.brc.ui.UIRegistry.getAppDataDir;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

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
import org.apache.lucene.queryparser.classic.QueryParser;
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
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.config.Scriptlet;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.GeoCoordDetail;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.LocalityDetail;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.Earth;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 27, 2012
 *
 */
public class LocalityCleanupIndexer
{
    private static final Logger  log = Logger.getLogger(LocalityCleanupIndexer.class);
    
    private File          FILE_INDEX_DIR;
    
    private IndexReader   reader;
    private IndexSearcher searcher;
    private Analyzer      analyzer;
    
    private IndexWriter   writer;
    private QueryParser   parser;
    
    private Scriptlet     scriptlet   = new Scriptlet();
    
    private Integer       currLocId   = null;
    private CustomDialog  dlg         = null;
    private ProgressDialog prgDlg     = null;
    
    private boolean       isQuitting   = false;
    private boolean       foundNothing = true;
    
    /**
     * 
     */
    public LocalityCleanupIndexer()
    {
        super();
        
        FILE_INDEX_DIR = new File(getAppDataDir() + File.separator + "locality-index");
    }

    /**
     * @return the isQuitting
     */
    public boolean isQuitting()
    {
        return isQuitting;
    }

    /**
     * @param prgDlg
     */
    public void setProgressDlg(final ProgressDialog prgDlg)
    {
        this.prgDlg = prgDlg;
    }

    /**
     * @return didn't find any matches at all
     */
    public boolean hasFoundNothing()
    {
        return foundNothing;
    }

    /**
     * 
     */
    public void initLuceneforReading()
    {
        try
        {
            reader = IndexReader.open(FSDirectory.open(FILE_INDEX_DIR));
            
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
        analyzer = new StandardAnalyzer(Version.LUCENE_47, CharArraySet.EMPTY_SET);
        parser   = new QueryParser(Version.LUCENE_47, "loc", analyzer);
    }
    
    /**
     * @return
     */
    public boolean hasMoreLocalities()
    {
        String sql = "SELECT LocalityID FROM locality WHERE DisciplineID = DSPLNID ";
        if (currLocId != null)
        {
            sql += String.format("AND LocalityID > %d ", currLocId);
        }
        sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql) + "ORDER BY LocalityID ASC LIMIT 0,1";
        System.out.println(sql);
        
        currLocId = BasicSQLUtils.getCount(sql);
        
        System.out.println("currLocalityID: "+currLocId);
        return currLocId != null;
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
            
            analyzer = new StandardAnalyzer(Version.LUCENE_47, CharArraySet.EMPTY_SET);
            IndexWriterConfig config        = new IndexWriterConfig(Version.LUCENE_47, analyzer);
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
     * @param str
     * @return
     */
    /*private String ifnull(final String str)
    {
        return str == null ? "" : str;
    }*/
    
    /**
     * @param strs
     * @return
     */
    /*private int indexOfLargest(final String...strs)
    {
        int inx = -1;
        int max = 0;
        for (int i=0;i<strs.length;i++)
        {
            if (strs[i].length() > max)
            {
                max = strs[i].length();
                inx = i;
            }
        }
        return inx;
    } */
    
    /**
     * @param locId
     * @return
     */
    private Pair<BigDecimal, BigDecimal> getLatLon(final int locId)
    {
        BigDecimal mainLat = null;  
        BigDecimal mainLon = null;  
        Object[] rowData = BasicSQLUtils.queryForRow("SELECT Latitude1, Longitude1 FROM locality WHERE LocalityID = " + locId);
        if (rowData != null)
        {
            mainLat = (BigDecimal)rowData[0];
            mainLon = (BigDecimal)rowData[1];
        }
        return new Pair<BigDecimal, BigDecimal>(mainLat, mainLon);
    }
    
    /**
     * 
     */
    public FindItemInfo getNextLocality()
    {
        foundNothing = true; // start by saying we haven't found any matches
        
        if (parser == null)
        {
            initLuceneforReading();
        }
        
        if (!hasMoreLocalities())
        {
            isQuitting = true;
            return null;
        }
        
        FindItemInfo fii = null;
        
        boolean cont = true;
        while (cont)
        {
            // Do 20 at a time
            String sql = String.format("SELECT LocalityName, FullName, LocalityID FROM locality l LEFT JOIN geography g ON l.GeographyID = g.GeographyID " +
            		                   "WHERE LocalityName IS NOT NULL AND LocalityID >= %d AND DisciplineID = DSPLNID LIMIT 0, 20", currLocId);
            sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
            System.out.println(sql);
            
            Vector<Object[]> rows = BasicSQLUtils.query(sql);
            if (rows == null || rows.size() == 0)
            {
                isQuitting = true;
                return null;
            }
            
            for (Object[] row : rows)
            {
                String  localityName = (String)row[0];
                String  geoName      = fixGeo((String)row[1]);
                currLocId = (Integer)row[2];
                
                fii = new FindItemInfo(currLocId, localityName);
                
                Pair<BigDecimal, BigDecimal> mainLatLon = getLatLon(currLocId);
                boolean isMainLatLon = (mainLatLon.first != null && mainLatLon.first.doubleValue() != 0.0 && 
                                        mainLatLon.second != null && mainLatLon.second.doubleValue() != 0.0);
                    
                
                StringBuilder sb = new StringBuilder();
                sb.append("loc:");
                sb.append(localityName);
                sb.append("^4 AND geo:"); // Boost 4 times
                sb.append(geoName);

                System.out.println(sb.toString());
                
                String queryString = sb.toString();
                
                Vector<Pair<Document, Float>> docList = new Vector<Pair<Document, Float>>();
                try
                {
                    Query query = parser.parse(queryString);
                    log.debug("Searching for: " + query.toString());
                    
                    Document             doc         = null;
                    int                  hitsPerPage = 10;
                    TopScoreDocCollector collector   = TopScoreDocCollector.create(hitsPerPage, true);
                    searcher.search(query, collector);
                    ScoreDoc[] hits = collector.topDocs().scoreDocs;
                    
                    docList.clear();
                    for (int i=0;i<hits.length;++i) 
                    {
                        if (hits[i].score > 1.0)
                        {
                            int docId = hits[i].doc;
                            doc       = searcher.doc(docId);
                            
                            int recId = Integer.parseInt(doc.get("id"));
                            if (currLocId != recId)
                            {
                                docList.add(new Pair<Document, Float>(doc, (Float)hits[i].score));
                            }
                        }
                    }
                    
                    if (docList.size() > 0)
                    {
                        //System.out.println("\n--------------------------------------------------------------");
                        //System.out.println(String.format("[%s] ->[%s][%s]", lastNm.toString(), p.first, (p.second != null ? p.second : "null")));
                        
                        int dupAddedCnt = 0;
                        for (Pair<Document, Float> pp : docList)
                        {
                            if (pp.second > 1.0)
                            {
                                String idStr = pp.first.get("id");
                                int    dupId = Integer.parseInt(idStr);
                                if (dupId != currLocId)
                                {
                                    Pair<BigDecimal, BigDecimal> dupLatLon = getLatLon(dupId);
                                    boolean isDupLatLon = (dupLatLon.first != null && dupLatLon.first.doubleValue() != 0.0 && 
                                                           dupLatLon.second != null && dupLatLon.second.doubleValue() != 0.0);
                                    boolean isOKToAdd = true;
                                    if (isMainLatLon && isDupLatLon)
                                    {
                                        
                                        LatLon mainLL       = LatLon.fromDegrees(mainLatLon.first.doubleValue(), mainLatLon.second.doubleValue());
                                        LatLon dupLL        = LatLon.fromDegrees(dupLatLon.first.doubleValue(), dupLatLon.second.doubleValue());
                                        double distInMeters = LatLon.ellipsoidalDistance(mainLL, dupLL, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS);
                                        System.out.println(String.format("%8.5f, %8.5f", distInMeters, 0.0));
                                        
                                        isOKToAdd = distInMeters < 50.0;
                                    }

                                    if (isOKToAdd)
                                    {
                                        fii.addDuplicate(dupId);
                                        System.out.println(String.format("%d - %5.3f - %s", dupAddedCnt, pp.second, pp.first.get("full")));
                                        dupAddedCnt++;
                                    }
                                }
                            }
                        }
                        
                        if (dupAddedCnt > 0)
                        {
                            foundNothing = false;
                            int btn = chooseLocalitiesToMerge(fii);
                            if (btn == CustomDialog.OK_BTN)
                            {
                                return fii;
                            }
                            
                            if (btn == CustomDialog.CANCEL_BTN)
                            {
                                isQuitting = true;
                                return null;
                            }
                        }
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            } // for loop
            
            if (!hasMoreLocalities())
            {
                isQuitting = true;
                return null;
            }
        } // while loop
        return fii;
    }
    
    /**
     * @param strs
     * @return
     */
    /*private int fieldCnt(String...strs)
    {
        int cnt = 0;
        for (String s : strs)
        {
            if (StringUtils.isNotEmpty(StringUtils.deleteWhitespace(s)))
            {
                cnt++;
            }
        }
        return cnt;
    }*/
    
    /**
     * @param localityId
     * @param isInto
     * @return
     */
    private ModelItem createModelItem(final Integer localityId, final boolean isInto)
    {
        String sql = "SELECT l.LocalityName, g.FullName, l.LocalityID FROM locality l LEFT JOIN geography g ON l.GeographyID = g.GeographyID WHERE LocalityID = " + localityId;
        Object[] row = BasicSQLUtils.queryForRow(sql);
        String locName  = (String)row[0];
        String geoName  = (String)row[1];
        
        String address = null;
        /*int    maxCnt  = 0;
        sql = "SELECT IsPrimary, City, State, Country FROM address WHERE (City IS NOT NULL OR State IS NOT NULL OR Country IS NOT NULL) AND LocalityID = " + localityId;
        for (Object[] r : BasicSQLUtils.query(sql))
        {
            Boolean isPrimary = (Boolean)r[0];
            String  city      = (String)r[2];
            String  state     = (String)r[3];
            String  country   = (String)r[4];
            
            if (isPrimary != null && isPrimary)
            {
                address = buildAddr(city, state, country);
                break;
            }
            int cnt = fieldCnt(city, state, country);
            if (cnt > maxCnt)
            {
                address = buildAddr(city, state, country);
            }
        }*/
        return new ModelItem(isInto, false, scriptlet.buildNameString(geoName, locName, ""), locName, geoName, "", address, localityId);
    }
    
    /**
     * @param fii
     * @return
     */
    private int chooseLocalitiesToMerge(final FindItemInfo fii)
    {
        if (true)
        {
            
            MultipleRecordCleanupDlg mrcDlg = null;
            try
            {
                MultipleRecordComparer mrc = new MultipleRecordComparer(fii, 
                                                                        Locality.getClassTableId(), 
                                                                        LocalityDetail.getClassTableId(), 
                                                                        GeoCoordDetail.getClassTableId());
                mrc.setSingleRowIncluded(true, true);
                //mrc.addDisplayColumn("FullName", "Full Name", "CONCAT(LocalityName, '-') AS FullName");
                mrc.addDisplayColumn("LocalityName");
                
                mrc.loadData();
                
                mrcDlg = new MultipleRecordCleanupDlg(mrc, "Locality Cleanup");
                mrcDlg.createUI();
                if (!mrcDlg.isSingle())
                {
                    mrcDlg.pack();
                    mrcDlg.setSize(800, 500);
                    UIHelper.centerWindow(mrcDlg);
                    mrcDlg.toFront();
                    if (prgDlg != null) prgDlg.toBack();
                    mrcDlg.setVisible(true);
                    if (prgDlg != null) prgDlg.toFront();
                    //isContinuing = !mrcDlg.isCancelled();
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            
            return mrcDlg.getBtnPressed();
        }
        Vector<ModelItem> items = new Vector<LocalityCleanupIndexer.ModelItem>();
        
        ModelItem primary = createModelItem(fii.getId(), true);
        items.add(primary);
        System.out.println("\nfii: "+fii.getId());
        
        for (Integer localityId : fii.getDuplicateIds())
        {
            items.add(createModelItem(localityId, false));
            System.out.println("  "+localityId);
        }
        Collections.sort(items);
        items.remove(primary);
        items.insertElementAt(primary, 0);
        
        JTable table = new JTable(new FindItemTableModel(items));
        UIHelper.makeTableHeadersCentered(table, false);
        //UIHelper.setVisibleRowCount(table,  10);
        UIHelper.calcColumnWidths(table, 10, 300);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        JScrollPane     sp = UIHelper.createScrollPane(table, true);
        
        pb.add(sp, cc.xy(1,1));
        pb.setDefaultDialogBorder();
        
        dlg = new CustomDialog((Frame)getTopWindow(), getResourceString("CLNUP_LOC_CHOOSE_TITLE"), true, CustomDialog.OKCANCELAPPLY, pb.getPanel());
        dlg.setOkLabel(getResourceString("CLNUP_MERGE"));
        dlg.setApplyLabel(getResourceString("SKIP"));
        dlg.setCancelLabel(getResourceString("QUIT"));
        dlg.setCloseOnApplyClk(true);
        dlg.createUI();
        dlg.pack();
        UIHelper.centerAndShow(dlg, 800, dlg.getSize().height);
        
        return dlg.getBtnPressed();
    }

    /**
     * @param locStr
     * @return
     */
    protected static String fixUp(final String locStr)
    {
        if (locStr != null)
        {
            String str = StringUtils.replace(locStr, "miles", "mi");
            str = str.replaceAll("[^a-zA-Z0-9.\\s]", "");
            return str;
        }
        return locStr;
    }
    
    /**
     * @param locStr
     * @return
     */
    protected static String fixGeo(final String locStr)
    {
        if (locStr != null)
        {
            String str = locStr.replaceAll("[^a-zA-Z0-9.\\s]", "");
            str = StringUtils.replace(str, " ", "_");
            return str;
        }
        return locStr;
    }
    
    /**
     * @param localityStr
     * @return
     */
    protected static String getMiles(final String localityStr)
    {
        String locStr = localityStr.toString();
        if (StringUtils.contains(locStr, " mi") || StringUtils.contains(locStr, " km"))
        {
            String[] tokens = StringUtils.split(locStr, ' ');
            for (int i=0;i<tokens.length;i++)
            {
                if (tokens[i].equals("mi") || tokens[i].equals("km"))
                {
                    
                    if (i > 0 && StringUtils.isNumeric(tokens[i-1]))
                    {
                        return tokens[i-1];
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 
     */
    public int buildIndex()// throws CorruptIndexException, LockObtainFailedException, IOException, ParseException
    {
        initLuceneForIndexing(true);
        
        int cnt = 0;
        Connection conn = DBConnection.getInstance().getConnection();
        try
        {
            Statement stmt = conn.createStatement();
            String sql = "SELECT LocalityName, FullName, CONCAT(LocalityName, ' ', IFNULL(FullName, '')), l.LocalityID FROM locality l LEFT JOIN geography g ON l.GeographyID = g.GeographyID WHERE DisciplineID = DSPLNID AND LocalityName IS NOT NULL";
            sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
            System.out.println(sql);
            
            String cntSQL = QueryAdjusterForDomain.getInstance().adjustSQL("SELECT COUNT(*) FROM locality WHERE DisciplineID = DSPLNID");
            System.out.println(cntSQL);
            int tot = BasicSQLUtils.getCountAsInt(cntSQL);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                String locName  = fixUp(rs.getString(1));
                String geoName  = fixGeo(rs.getString(2));
                String full     = rs.getString(3);
                String idStr    = rs.getString(4);
                //String miles    = getMiles(locName);
                
                Document doc = new Document();
                doc.add(new Field("loc",  locName,  Field.Store.YES, Field.Index.ANALYZED));
                if (StringUtils.isNotEmpty(geoName)) doc.add(new Field("geo",  geoName, Field.Store.YES, Field.Index.ANALYZED));
                //if (StringUtils.isNotEmpty(miles)) doc.add(new Field("mi",  miles, Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("full", full, Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("id",   idStr, Field.Store.YES, Field.Index.NOT_ANALYZED));
                
                try
                {
                    writer.addDocument(doc);
                } catch (CorruptIndexException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                
                cnt++;
                if (cnt % 100 == 0)
                {
                    System.out.println(String.format("%d / %d", cnt, tot));
                }
            }
            writer.close();
            rs.close();
            
            /*
            initLuceneforReading();
            
            sql = "SELECT CONCAT(LocalityName, ' ', IFNULL(FullName, '')), FullName, l.LocalityID FROM locality l LEFT JOIN geography g ON l.GeographyID = g.GeographyID WHERE LocalityName IS NOT NULL";
            //sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
            //System.out.println(sql);
            cnt = 0;
            PrintWriter pw = new PrintWriter("loc.txt");
            rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                String locName  = fixUp(rs.getString(1));
                String geoName  = fixGeo(rs.getString(2));
                String miles    = getMiles(locName);
                
                //System.out.println(locName+"  "+rs.getString(2));
                
                if (StringUtils.deleteWhitespace(locName).length() == 0) continue;
                
                String idStr = rs.getString(3);
                
                if (geoName != null && (geoName.length() == 0 || StringUtils.deleteWhitespace(geoName).length() == 0))
                {
                    geoName = null;
                }
                
                //String qStr = locName;// + (miles != null ? (" AND mi:"+miles) : "");
                String qStr = locName + (geoName != null ? (" AND geo:'"+geoName+"'") : "");
                Query q = new QueryParser(Version.LUCENE_36, "full", analyzer).parse(qStr);

                int hitsPerPage = 10;
                TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
                searcher.search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                boolean first = true;
                for (int i = 0; i < hits.length; ++i)
                {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    
                    if (!idStr.equals(d.get("id")))
                    {
                        String docMiles = getMiles(d.get("loc")); 
                        if (miles != null)
                        if ((docMiles == null && miles == null) || (miles != null && docMiles != null && miles.equals(docMiles)))
                        {
                            if (first)
                            {
                                pw.println("\nFound " + hits.length + " hits. ["+locName+"] mi: "+(miles != null ? miles : ""));
                                first = false;
                            }
                            pw.println((i + 1) + ". " + d.get("full")+"  "+hits[i].score);
                        }
                    }
                }
                cnt++;
                if (cnt % 100 == 0)
                {
                    System.out.println(String.format("%d / %d", cnt, tot));
                }
                if (cnt > 1000) break;
            }
            rs.close();
            stmt.close();
            pw.close();*/
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return cnt;
    }
    
    /**
     * 
     */
    public void shtudown()
    {
//        try
//        {
//            if (searcher != null) searcher.close();
            if (analyzer != null) analyzer.close();
//        } catch (IOException e)
//        {
//            e.printStackTrace();
//        }
    }
    
    //----------------------------------------------------------------------
    class ModelItem implements Comparable<ModelItem>
    {
        boolean isMergedInto;
        boolean isMergedFrom;
        String  title;
        String  last;
        String  first;
        String  mid;
        String  address;
        int     localityId;
        

        
        /**
         * @param isMergedInto
         * @param isMergedFrom
         * @param title
         * @param last
         * @param first
         * @param mid
         * @param address
         * @param localityId
         */
        public ModelItem(boolean isMergedInto, boolean isMergedFrom, String title, String last,
                         String first, String mid, String address, int localityId)
        {
            super();
            this.isMergedInto = isMergedInto;
            this.isMergedFrom = isMergedFrom;
            this.title        = title != null ? title : "N/A";
            this.last         = getDisplayStr(last);
            this.first        = getDisplayStr(first);
            this.mid          = getDisplayStr(mid);
            this.address      = address;
            this.localityId      = localityId;
        }

        private String getDisplayStr(final String str)
        {
            if (StringUtils.isEmpty(str))
            {
                return " ";
            }
            
            String s = StringUtils.deleteWhitespace(str);
            if (s.length() == 0)
            {
                return "(whitespace)";
            }
            return str;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(ModelItem mi)
        {
            return title.compareTo(mi.title);
        }
        
    }
    
    //----------------------------------------------------------------------
    class FindItemTableModel extends DefaultTableModel
    {
        private String[] headers = {"MERGE_INTO", "MERGE_FROM", "MERGE_FULLNM", "MERGE_LAST", "MERGE_FIRST", "MERGE_MID", "MERGE_LOC"};
        private Vector<ModelItem> items;
        private boolean hasLocation;
        
        /**
         * 
         */
        public FindItemTableModel(final Vector<ModelItem> list)
        {
            super();
            items = list;
            int locCnt = 0;
            for (ModelItem mi : items)
            {
                if (StringUtils.isNotEmpty(mi.address)) locCnt++;
            }
            hasLocation = locCnt > 0;
        }

        @Override
        public String getColumnName(int column)
        {
            return getResourceString("CLNUP_"+headers[column]);
        }

        @Override
        public int getColumnCount()
        {
            return headers.length - (hasLocation ? 0 : 1);
        }

        @Override
        public int getRowCount()
        {
            return items != null ? items.size() : 0;
        }

        @Override
        public Object getValueAt(int row, int column)
        {
            ModelItem item = items.get(row);
            switch (column)
            {
                case 0 : return item.isMergedInto;
                case 1 : return item.isMergedFrom;
                case 2 : return item.title;
                case 3 : return item.last;
                case 4 : return item.first;
                case 5 : return item.mid;
                case 6 : return item.address;
            }
            return "";
        }

        @Override
        public Class<?> getColumnClass(int column)
        {
            return column < 2 ? Boolean.class : String.class;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return column < 2;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object aValue, int row, int column)
        {
            Boolean value = (Boolean)aValue;
            if (value)
            {
                if (column == 0)
                {
                    for (int i=0;i<items.size();i++)
                    {
                        items.get(i).isMergedInto = false;
                    }
                    ModelItem item = items.get(row);
                    item.isMergedFrom = false;
                    item.isMergedInto = true;
                    
                } else if (column == 1)
                {
                    ModelItem item = items.get(row);
                    if (item.isMergedInto)
                    {
                        item.isMergedInto = false;
                    }
                    item.isMergedFrom = true;
                }
                
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        fireTableDataChanged();
                    }
                });
            } else
            {
                ModelItem item = items.get(row);
                if (column == 0)
                {
                    item.isMergedInto = false;
                } else
                {
                    item.isMergedFrom = false;
                }
            }
            
            //System.out.println();
            int cntInto = 0;
            int cntFrom = 0;
            for (int i=0;i<items.size();i++)
            {
                ModelItem item = items.get(i);
                cntInto += item.isMergedInto ? 1 : 0;
                cntFrom += item.isMergedFrom ? 1 : 0;
                //System.out.println(String.format("%d %d  %s %s", cntInto, cntFrom, item.isMergedInto ? "Y" : "N", item.isMergedFrom ? "Y" : "N"));
            }
            dlg.getOkBtn().setEnabled(cntInto == 1 && cntFrom > 0 && cntFrom < items.size());
        }
    }

}
