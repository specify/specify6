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

import static edu.ku.brc.specify.conversion.BasicSQLUtils.getCount;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getCountAsInt;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.queryForRow;
import static edu.ku.brc.ui.UIRegistry.getAppDataDir;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static org.apache.commons.lang.StringUtils.countMatches;
import static org.apache.commons.lang.StringUtils.deleteWhitespace;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AgentGeography;
import edu.ku.brc.specify.datamodel.AgentSpecialty;
import edu.ku.brc.specify.datamodel.AgentVariant;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 27, 2012
 *
 */
public class AgentCleanupIndexer extends LuceneHelperBase
{
    private static final Logger  log = Logger.getLogger(AgentCleanupIndexer.class);
    
    private Scriptlet      scriptlet   = new Scriptlet();
    
    private Integer        currAgentID = null;
    private CustomDialog   dlg         = null;
    private ProgressDialog prgDlg      = null;
    
    private boolean        isQuitting  = false;
    
    /**
     * 
     */
    public AgentCleanupIndexer()
    {
        super();
        FILE_INDEX_DIR = new File(getAppDataDir() + File.separator + "agents-index");
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
     * @return the prgDlg
     */
    public ProgressDialog getPrgDlg()
    {
        return prgDlg;
    }

    /**
     * @return
     */
    public boolean hasMoreAgents()
    {
        String sql = "SELECT AgentID FROM agent WHERE DivisionID = DIVID ";
        if (currAgentID != null)
        {
            sql += String.format("AND AgentID > %d ", currAgentID);
        }
        sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql) + "ORDER BY AgentID ASC LIMIT 0,1";
        System.out.println(sql);
        
        currAgentID = getCount(sql);
        
        //System.out.println("currAgentID: "+currAgentID);
        return currAgentID != null;
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
            
            analyzer = new StandardAnalyzer(Version.LUCENE_36, new HashSet<Object>());
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
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
    private String ifnull(final String str)
    {
        return str == null ? "" : str;
    }
    
    /**
     * 
     */
    public FindItemInfo getNextAgent()
    {
        if (parser == null)
        {
            initLuceneforReading("full");
        }
        
        if (!hasMoreAgents())
        {
            return null;
        }
        
        FindItemInfo fii = null;
        
        boolean cont = true;
        while (cont)
        {
            Integer prevAgentId = currAgentID;
            // Do 20 at a time
            String sql = String.format("SELECT LastName, FirstName, MiddleInitial, AgentID FROM agent " +
            		                   "WHERE SpecifyUserID IS NULL AND LastName IS NOT NULL AND AgentID >= %d AND DivisionID = DIVID LIMIT 0, 20", currAgentID);
            sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
            System.out.println(sql);
            
            Vector<Object[]> rows = query(sql);
            if (rows == null || rows.size() < 2)
            {
                isQuitting = true;
                return null;
            }
            
            for (Object[] row : rows)
            {
                String  lastNm   = (String)row[0];
                String  firstNm  = (String)row[1];
                String  midNm    = (String)row[2];
                currAgentID      = (Integer)row[3];
                
                //System.out.println("last["+lastNm+"]  first["+firstNm+"]  mid["+midNm+"]  full["+fullName+"]");
                String[] nms = FirstLastVerifier.parseName(lastNm.toString());
                if (nms != null)
                {
                    String last  = nms[0];
                    String first = nms.length > 1 ? nms[1] : null;
                    if (first == null && isNotEmpty(firstNm))
                    {
                        first = firstNm;
                    }
                    
                    //System.out.println("last["+lastNm+"]  first["+firstNm+"]  mid["+midNm+"]");
                    fii = new FindItemInfo(currAgentID, scriptlet.buildNameString(firstNm, lastNm, midNm));
                    
                    StringBuilder sb = new StringBuilder();
                    if (isNotEmpty(last))
                    {
                        sb.append("last:");
                        sb.append(last);
                        sb.append("^4"); // Boost 4 times
                    }
                    
                    if (isNotEmpty(first))
                    {
                        sb.append(" "); 
                        boolean ok = first.length() > 1;
                        if (ok) sb.append("AND (first:"); 
                        sb.append(first);
                        sb.append("~0.6"); 
                        if (ok) 
                        {
                            sb.append(" OR first:");
                            sb.append(first.charAt(0));
                            sb.append("~0.4)");
                        }
                    }
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
                        System.out.println("Hits: "+(hits != null ? hits.length : 0));
                        
                        docList.clear();
                        for (int i=0;i<hits.length;++i) 
                        {
                            System.out.println("doc: "+i+" scrore: "+hits[i].score+"  "+searcher.doc(hits[i].doc).get("full"));
                            //if (hits[i].score > 1.0)
                            {
                                int docId     = hits[i].doc;
                                doc           = searcher.doc(docId);
                                
                                lastNm            = doc.get("last");
                                int numCommas     = countMatches(lastNm, ",");
                                int numSemiColons = countMatches(lastNm, ";");
                                int recId         = Integer.parseInt(doc.get("id"));
                                if (numSemiColons > 0 || numCommas > 0 || recId == currAgentID)
                                {
                                    System.out.println(numSemiColons+"  "+numCommas+"  "+(recId == currAgentID)+"  "+recId);
                                    continue;
                                }
                                docList.add(new Pair<Document, Float>(doc, (Float)hits[i].score));
                            }
                        }
                        
                        System.out.println("docList.size(): "+docList.size());
                        if (docList.size() > 0)
                        {
                            //System.out.println("\n--------------------------------------------------------------");
                            //System.out.println(String.format("[%s] ->[%s][%s]", lastNm.toString(), p.first, (p.second != null ? p.second : "null")));
                            
                            int i = 0;
                            for (Pair<Document, Float> pp : docList)
                            {
                                if (pp.second > 1.0)
                                {
                                    String idStr = pp.first.get("id");
                                    int dupId = Integer.parseInt(idStr);
                                    if (dupId != currAgentID)
                                    {
                                        fii.addDuplicate(dupId);
                                        System.out.println(String.format("%d - %5.3f - %s", i, pp.second, pp.first.get("full")));
                                        i++;
                                    }
                                }
                            }
                            
                            if (i > 0)
                            {
                                int btn = chooseAgentsToMergeNew(fii);
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
                } else
                {
                    //System.out.println(String.format("Group [%s] ", name.toString()));
                }
            } // for loop
            
            if (prevAgentId == currAgentID)
            {
                int numRemaining = getCountAsInt("SELECT COUNT(*) FROM agent WHERE AgentID > "+currAgentID);
                if (numRemaining == 0)
                {
                    return null;
                }
                currAgentID = getCountAsInt("SELECT AgentID FROM agent WHERE AgentID > "+currAgentID+" ORDER BY AgentID ASC LIMIT 0,1");
            }
        } // while loop
        return fii;
    }
    
    /**
     * @param args
     * @return
     */
    private String buildAddr(String...strs)
    {
        StringBuilder sb = new StringBuilder();
        for (String s : strs)
        {
            if (isNotEmpty(s))
            {
                if (sb.length() > 0) sb.append(", ");
                sb.append(s);
            }
        }
        return sb.toString();
    }
    
    /**
     * @param strs
     * @return
     */
    private int fieldCnt(String...strs)
    {
        int cnt = 0;
        for (String s : strs)
        {
            if (isNotEmpty(deleteWhitespace(s)))
            {
                cnt++;
            }
        }
        return cnt;
    }
    
    /**
     * @param agentId
     * @param isInto
     * @return
     */
    private ModelItem createModelItem(final Integer agentId, final boolean isInto)
    {
        String sql = "SELECT LastName, FirstName, MiddleInitial FROM agent WHERE AgentID = " + agentId;
        Object[] row = queryForRow(sql);
        String lastName   = (String)row[0];
        String firstName  = (String)row[1];
        String midInitial = (String)row[2];
        
        int    maxCnt  = 0;
        String address = null;
        sql = "SELECT IsPrimary, City, State, Country FROM address WHERE (City IS NOT NULL OR State IS NOT NULL OR Country IS NOT NULL) AND AgentID = " + agentId;
        for (Object[] r : query(sql))
        {
            Boolean isPrimary = (Boolean)r[0];
            String  city      = (String)r[1];
            String  state     = (String)r[2];
            String  country   = (String)r[3];
            
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
        }
        return new ModelItem(isInto, false, scriptlet.buildNameString(firstName, lastName, midInitial), lastName, firstName, midInitial, address, agentId);
    }
    
    /**
     * @param fii
     * @return
     */
    private int chooseAgentsToMergeNew(final FindItemInfo fii)
    {
        if (true)
        {
            
            MultipleRecordCleanupDlg mrcDlg = null;
            try
            {
                MultipleRecordComparer mrc = new MultipleRecordComparer(fii, 
                                                                        Agent.getClassTableId(), 
                                                                        Address.getClassTableId(), 
                                                                        AgentVariant.getClassTableId(),
                                                                        AgentSpecialty.getClassTableId(),
                                                                        AgentGeography.getClassTableId());
                mrc.setSingleRowIncluded(false, false, false, false);
                //mrc.addDisplayColumn("Agent's Name");
                mrc.addDisplayColumn("LastName");
                mrc.addDisplayColumn("FirstName");
                mrc.addDisplayColumn("MiddleInitial");
                
                mrc.loadData();
                
                
                mrcDlg = new MultipleRecordCleanupDlg(mrc, "Agent Cleanup");
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
        Vector<ModelItem> items = new Vector<AgentCleanupIndexer.ModelItem>();
        
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
     * @param fii
     * @return
     */
    /*private int chooseAgentsToMerge(final FindItemInfo fii)
    {
        Vector<ModelItem> items = new Vector<AgentCleanupIndexer.ModelItem>();
        
        ModelItem primary = createModelItem(fii.getId(), true);
        items.add(primary);
        //System.out.println("\nfii: "+fii.getId());
        
        for (Integer agentId : fii.getDuplicateIds())
        {
            items.add(createModelItem(agentId, false));
            //System.out.println("  "+agentId);
        }
        Collections.sort(items);
        items.remove(primary);
        items.insertElementAt(primary, 0);
        
        JTable table = new JTable(new FindItemTableModel(items));
        UIHelper.makeTableHeadersCentered(table, false);
        UIHelper.calcColumnWidths(table, 10, 300);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        JScrollPane     sp = UIHelper.createScrollPane(table, true);
        
        pb.add(sp, cc.xy(1,1));
        pb.setDefaultDialogBorder();
        
        String fmt = "SELECT COUNT(*) FROM agent WHERE SpecifyUserID IS NULL AND LastName IS NOT NULL AND AgentID %s %d AND DivisionID = DIVID";
        fmt = QueryAdjusterForDomain.getInstance().adjustSQL(fmt);

        int cntLeft = getCountAsInt(String.format(fmt, "<", currAgentID));
        int cntDone = getCountAsInt(String.format(fmt, ">=", currAgentID));
        String dlgTitle = getFormattedResStr("CLNUP_AG_CHOOSE_TITLE", cntLeft, cntDone);
        
        dlg = new CustomDialog((Frame)getTopWindow(), dlgTitle, true, CustomDialog.OKCANCELAPPLY, pb.getPanel());
        dlg.setOkLabel(getResourceString("CLNUP_MERGE"));
        dlg.setApplyLabel(getResourceString("SKIP"));
        dlg.setCancelLabel(getResourceString("QUIT"));
        dlg.setCloseOnApplyClk(true);
        dlg.createUI();
        dlg.pack();
        UIHelper.centerAndShow(dlg, 800, dlg.getSize().height);
        
        return dlg.getBtnPressed();
    }*/
    
    /**
     * 
     */
    public int buildIndex()
    {
        initLuceneForIndexing(true);

        int cnt = 0;
        Connection dbConn = DBConnection.getInstance().getConnection();
        try
        {
            Statement stmt = dbConn.createStatement();
            String sql = "SELECT LastName, FirstName, MiddleInitial, CONCAT(LastName, ' ', IFNULL(FirstName, ''),  ' ', IFNULL(MiddleInitial, '')), AgentID FROM agent WHERE DivisionID = DIVID";
            sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
            System.out.println(sql);
            
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                String last  = ifnull(rs.getString(1));
                String first = ifnull(rs.getString(2));
                String mid   = ifnull(rs.getString(3));
                String full  = ifnull(rs.getString(4));
                String idStr = ifnull(rs.getString(5));
                
                String[] nms = FirstLastVerifier.parseName(last.toString());
                if (nms != null)
                {
                    last = nms[0];
                    if (isEmpty(first) && nms.length > 1 && isNotEmpty(nms[1]))
                    {
                        first = nms[1];
                    }
                }
                //System.out.println("last["+last+"]  first["+first+"]  mid["+mid+"]  full["+full+"]");
                
                Document doc = new Document();
                doc.add(new Field("last",  last,  Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("first", first, Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("mid",   mid,   Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("full",  full,  Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("id",    idStr, Field.Store.YES, Field.Index.NOT_ANALYZED));
                //System.out.println("last["+last+"]  first["+first+"]  mid["+mid+"]  full["+full+"]");
                
                try
                {
                    writer.addDocument(doc);
                    cnt++;
                } catch (CorruptIndexException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
            writer.close();
            System.out.println("Added "+cnt+" documents.");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return cnt;
    }
    
    /**
     * 
     */
    /*public void process()
    {
        //initLuceneforReading();
        
        String sql = "SELECT LastName, FirstName, MiddleInitial, CONCAT(IFNULL(LastName, ''), ' ', IFNULL(FirstName, ''),  ' ', IFNULL(MiddleInitial, '')) FROM agent WHERE AgentID = 6944";
        Object[] row = queryForRow(sql);
        
        String last  = (String)row[0];
        String first = (String)row[1];
        String mid   = (String)row[2];
        String full  = (String)row[3];
        
        StringBuilder sb = new StringBuilder();
        if (isNotEmpty(last))
        {
            sb.append(last);
            sb.append("^4"); // Boost 4 times
        }
        
        if (isNotEmpty(first))
        {
            sb.append(" "); 
            boolean ok = first.length() > 1;
            if (ok) sb.append("AND ("); 
            sb.append(first);
            sb.append("~0.6"); 
            if (ok) 
            {
                sb.append(" OR ");
                sb.append(first.charAt(0));
                sb.append("~0.4)");
            }
        }
        System.out.println(sb.toString());
        
        String queryString = sb.toString();
        
        Query query;
        try
        {
            query = parser.parse(queryString);
            log.debug("Searching for: " + query.toString());

            //Integer              agentId   = null;
            Document             doc         = null;
            //boolean              found       = false;
            
            int                  hitsPerPage = 500;
            TopScoreDocCollector collector   = TopScoreDocCollector.create(hitsPerPage, true);
            searcher.search(query, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            for (int i=0;i<hits.length;++i) 
            {
                int docId     = hits[i].doc;
                doc           = searcher.doc(docId);
                System.out.println(String.format("%d - %5.3f - %s", i, hits[i].score, doc.get("full")));
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/

    /**
     * 
     */
    public void shtudown()
    {
        try
        {
            if (searcher != null) searcher.close();
            if (analyzer != null) analyzer.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
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
        int     agentId;
        

        
        /**
         * @param isMergedInto
         * @param isMergedFrom
         * @param title
         * @param last
         * @param first
         * @param mid
         * @param address
         * @param agentId
         */
        public ModelItem(boolean isMergedInto, boolean isMergedFrom, String title, String last,
                String first, String mid, String address, int agentId)
        {
            super();
            this.isMergedInto = isMergedInto;
            this.isMergedFrom = isMergedFrom;
            this.title        = title != null ? title : "N/A";
            this.last         = getDisplayStr(last);
            this.first        = getDisplayStr(first);
            this.mid          = getDisplayStr(mid);
            this.address      = address;
            this.agentId      = agentId;
        }

        private String getDisplayStr(final String str)
        {
            if (isEmpty(str))
            {
                return " ";
            }
            
            String s = deleteWhitespace(str);
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
        private String[] headers = {"Merge Into", "Merge From", "Full Name", "Last", "First", "Middle", "Location"};
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
                if (isNotEmpty(mi.address)) locCnt++;
            }
            hasLocation = locCnt > 0;
        }

        @Override
        public String getColumnName(int column)
        {
            return headers[column];
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
