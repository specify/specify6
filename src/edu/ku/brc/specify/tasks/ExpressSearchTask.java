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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.ExpressResultsTableInfo;
import edu.ku.brc.af.core.ExpressSearchResults;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.ExpressSearchIndexerPane;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.helpers.HTTPGetter;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPane;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.db.JAutoCompTextField;
import edu.ku.brc.ui.db.PickListDBAdapterFactory;
/**
 * This task will enable the user to index the database and preform express searches. This is where the Express Search starts.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class ExpressSearchTask extends BaseTask implements CommandListener, ExpressSearchIndexerPane.ExpressSearchIndexerListener
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(ExpressSearchTask.class);

    public static final String EXPRESSSEARCH      = "Express_Search";
    public static final String CHECK_INDEXER_PATH = "CheckIndexerPath";
    
    // Static Data Memebers
    protected static WeakReference<TableInfoWeakRef> tableInfo   = null;
    protected static ExpressSearchTask               instance    = null;
    protected static final String                    LAST_SEARCH = "lastsearch"; 
    
    // Data Members
    protected Analyzer                     analyzer       = new StandardAnalyzer();
    protected File                         lucenePath     = null;
    protected JAutoCompTextField           searchText;
    protected JButton                      searchBtn;
    protected Color                        textBGColor    = null;
    protected Color                        badSearchColor = new Color(255,235,235);


    /**
     * Deafult Constructor.
     */
    public ExpressSearchTask()
    {
        super(EXPRESSSEARCH, getResourceString(EXPRESSSEARCH));
        icon = IconManager.getIcon("Search", IconManager.IconSize.Std16);

        lucenePath = getIndexDirPath(); // must be initialized here
        
        CommandDispatcher.register(APP_CMD_TYPE, this);
        CommandDispatcher.register(EXPRESSSEARCH, this);
        
        instance = this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
        }
    }

    /**
     * Helper function to return the path to the express search directory.
     * @return return the path to the express search directory
     */
    public static File getIndexDirPath()
    {
        File                 path       = null;
        SpecifyAppContextMgr appContext = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        if (appContext != null)
        {
            String luceneLocPref = AppPreferences.getLocalPrefs().get("ui.misc.luceneLocation", UICacheManager.getDefaultWorkingPath());
            AppPreferences.getLocalPrefs().put("ui.misc.luceneLocation", luceneLocPref);
            
            path = new File(luceneLocPref + File.separator + appContext.getDatabaseName() + File.separator + "index-dir");
            if (!path.exists())
            {
                if (!path.mkdirs())
                {
                    String msg = "unable to create directory [" + path.getAbsolutePath() + "]";
                    log.error(msg);
                    throw new RuntimeException(msg);
                }
            }
        }
        return path;
    }
    
    /**
     * Returns whether there are file sin the lucence directory.
     * @return whether there are file sin the lucence directory.
     */
    public static boolean doesIndexExist()
    {
        File path = getIndexDirPath();
        if (path != null)
        {
            try
            {
                return IndexReader.indexExists(FSDirectory.getDirectory(getIndexDirPath(), false));
            } catch (IOException ex)
            {
            }
        }
        return false;
    }
    
    /**
     * Returns the HashMap of ExpressResultsTableInfo items mapped By Name.
     * @return the HashMap of ExpressResultsTableInfo items mapped By Name.
     */
    public static TableInfoWeakRef getTableInfoHashMaps()
    {
        TableInfoWeakRef tableInfoWR = null;
        
        if (tableInfo != null)
        {
            tableInfoWR = tableInfo.get();
        }
        
        if (tableInfoWR == null)
        {
            tableInfoWR = intializeTableInfo();
            tableInfo = new WeakReference<TableInfoWeakRef>(tableInfoWR);
        }
        return tableInfoWR;
    }
    
    /**
     * Returns the TableInfo object by Name.
     * @return the TableInfo object by Name.
     */
    public static ExpressResultsTableInfo getTableInfoByName(final String name)
    {
        return getTableInfoHashMaps().getTables().get(name);
    }
    
    /**
     * Returns the HashMap of ExpressResultsTableInfo items mapped By Name.
     * @return the HashMap of ExpressResultsTableInfo items mapped By Name.
     */
    public static Hashtable<String, ExpressResultsTableInfo> getTableInfoHash()
    {
        return getTableInfoHashMaps().getTables();
    }
    
    /**
     * Returns the Hash for Mapping Id to TableInfo.
     * @return the Hash for Mapping Id to TableInfo.
     */
    protected static Hashtable<String, ExpressResultsTableInfo> getIdToTableInfoHash()
    {
        return getTableInfoHashMaps().getIdToTableInfoHash();
    }
    
    /**
     * Returns the Hash for Mapping by Join Id to TableInfo.
     * @return the Hash for Mapping by Join Id to TableInfo.
     */
    protected static Hashtable<String, List<ExpressResultsTableInfo>> getJoinIdToTableInfoHash()
    {
        return getTableInfoHashMaps().getJoinIdToTableInfoHash();
    }

    /**
     * Collects information about all the tables that will be processed for any search.
     * @param tableItems the list of Elements to be processed
     * @param tables the table info hash
     */
    protected static void intializeTableInfo(final List tableItems, 
                                             final Hashtable<String, ExpressResultsTableInfo> tables,
                                             final Hashtable<String, ExpressResultsTableInfo> byIdHash,
                                             Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash,
                                             final boolean isExpressSearch)
    {
        for ( Iterator iter = tableItems.iterator(); iter.hasNext(); )
        {
            Element                 tableElement = (Element)iter.next();
            ExpressResultsTableInfo ti           = new ExpressResultsTableInfo(tableElement, ExpressResultsTableInfo.LOAD_TYPE.Both, isExpressSearch);
            if (byIdHash.get(ti.getId()) == null)
            {
                byIdHash.put(ti.getId(), ti);
                
                if (tables.get(ti.getName()) == null)
                {
                    tables.put(ti.getName(), ti);
                    
                    if (!ti.isIndexed())
                    {
                        ExpressResultsTableInfo.JoinColInfo joinCols[] = ti.getJoins();
                        if (joinCols != null)
                        {
                            for (ExpressResultsTableInfo.JoinColInfo jci :  joinCols)
                            {
                                List<ExpressResultsTableInfo> list = joinIdToTableInfoHash.get(jci.getJoinTableId());
                                if (list == null)
                                {
                                    list = new ArrayList<ExpressResultsTableInfo>();
                                    joinIdToTableInfoHash.put(jci.getJoinTableId(), list);
                                    //log.debug("Adding JOin Table ID["+jci.getJoinTableId()+"]");
                                }
                                list.add(ti);
                            }
                        }
                    }
                    
                } else
                {
                    log.error("Duplicate express Search name["+ti.getName()+"]");
                }

            } else
            {
                log.error("Duplicate Search Id["+ti.getId()+"]");
            }
        } 
    }

    /**
     * Collects information about all the tables that will be processed for any search.
     * @return hash of named ExpressResultsTableInfo
     */
    protected static TableInfoWeakRef intializeTableInfo()
    {
        Hashtable<String, ExpressResultsTableInfo>       tables                = new Hashtable<String, ExpressResultsTableInfo>();
        
        Hashtable<String, ExpressResultsTableInfo>       idToTableInfoHash     = new Hashtable<String, ExpressResultsTableInfo>();
        Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash = new Hashtable<String, List<ExpressResultsTableInfo>>();
        
        try
        {
            Element esDOM = AppContextMgr.getInstance().getResourceAsDOM("SearchConfig"); // Describes the definitions of the full text search
            
            intializeTableInfo(esDOM.selectNodes("/searches/express/table"), tables, idToTableInfoHash, joinIdToTableInfoHash, true);
            
            intializeTableInfo(esDOM.selectNodes("/searches/generic/table"), tables, idToTableInfoHash, joinIdToTableInfoHash, false);

                
        } catch (Exception ex)
        {
            log.error(ex);
        }
        
        // This is sort of bad because it assumes the Task has already been created
        // It really shoud be in nearly all cases, but I can't absolutely guareentee it
        return instance.new TableInfoWeakRef(tables, idToTableInfoHash, joinIdToTableInfoHash);
    }

    /**
     * Check to see of the index has been run and then enables the express search controls.
     *
     */
    public void checkForIndexer()
    {
        boolean exists = doesIndexExist();
        
        //log.debug(lucenePath.getAbsoluteFile() + " has index " + (lucenePath.list().length > 0));
        
        searchBtn.setEnabled(exists);
        searchText.setEnabled(exists);
    }

    /**
     * Displays the config pane for the express search.
     *
     */
    public void showIndexerPane()
    {
        ExpressSearchIndexerPane expressSearchIndexerPane = new ExpressSearchIndexerPane(this, this, ExpressSearchTask.getIndexDirPath());
        SubPaneMgr.getInstance().addPane(expressSearchIndexerPane);
    }

    /**
     * Performs the express search and returns the results.
     */
    protected void doQuery()
    {
        searchText.setBackground(textBGColor);
        
        String searchTerm = searchText.getText();
        if (isNotEmpty(searchTerm))
        {
            ExpressSearchResultsPane expressSearchPane = new ExpressSearchResultsPane(searchTerm, this);
            if (doQuery(lucenePath, analyzer, searchText, badSearchColor, expressSearchPane))
            {
                SubPaneMgr.getInstance().addPane(expressSearchPane);
            } else
            {
                UICacheManager.displayLocalizedStatusBarText("NoExpressSearchResults");
            }
            AppPreferences.getLocalPrefs().put(LAST_SEARCH, searchTerm);
        }
    }


    /**
     * Performs the express search and returns the results to the ExpressSearchResultsPaneIFace/
     * @param lucenePath the path to lucene
     * @param analyzer the analyzer to use for the indexer
     * @param searchText the Text Control that contains the search string
     * @param badSearchColor the color to set the control if no results
     * @param tables ExpressResultsTableInfo hash
     * @param esrPane the pane that the results will be set into
     * @return true if results were found, false if not results
     */
    public static boolean doQuery(final File       lucenePath,
                                  final Analyzer   analyzer,
                                  final JTextField searchText,
                                  final Color      badSearchColor,
                                  final ExpressSearchResultsPaneIFace esrPane)
    {
        return doQuery(lucenePath, analyzer, searchText, null, badSearchColor, esrPane);
    }

    /**
     * Performs the express search and returns the results to the ExpressSearchResultsPaneIFace/
     * @param lucenePath the path to lucene
     * @param analyzer the analyzer to use for the indexer
     * @param searchTextStr the string to use as the search
     * @param tables ExpressResultsTableInfo hash
     * @param esrPane the pane that the results will be set into
     * @return true if results were found, false if not results
     */
    public static boolean doQuery(final File       lucenePath,
                                  final Analyzer   analyzer,
                                  final String     searchTextStr,
                                  final ExpressSearchResultsPaneIFace esrPane)
    {
        return doQuery(lucenePath, analyzer, null, searchTextStr, null, esrPane);
    }

    /**
     * Performs the express search and returns the results to the ExpressSearchResultsPaneIFace.
     * If the control is null then it will use the string.
     *
     * @param lucenePath the path to lucene
     * @param analyzer the analyzer to use for the indexer
     * @param searchText the Text Control that contains the search string (can be null)
     * @param searchTextStr the Text Control that contains the search string (can be null)
     * @param badSearchColor the color to set the control if no results (can be null if searchText is null)
     * @param tables ExpressResultsTableInfo hash
     * @param esrPane the pane that the results will be set into
     * @return true if results were found, false if not results
     */
    public static boolean doQuery(final File       lucenePath,
                                  final Analyzer   analyzer,
                                  final JTextField searchText,
                                  final String     searchTextStr,
                                  final Color      badSearchColor,
                                  final ExpressSearchResultsPaneIFace esrPane)
    {
        String searchTerm;
        if (searchText != null)
        {
            searchTerm = searchText.getText();
        } else
        {
            searchTerm = searchTextStr;
        }

        boolean hasResults = false;
        if (searchTerm != null && searchTerm.length() > 0)
        {
            if (true)
            {
                hasResults = exeQueryLocal(lucenePath, analyzer, searchTerm, esrPane);
            } else
            {
                hasResults = exeQueryRemote(lucenePath, analyzer, searchTerm, esrPane);
            }
        }
        
        if (!hasResults)
        {
            if (searchText != null)
            {
                if (badSearchColor != null)
                {
                    searchText.setBackground(badSearchColor);
                }
                searchText.setSelectionStart(0);
                searchText.setSelectionEnd(searchText.getText().length());
                searchText.getToolkit().beep();
            }
        }
        
        return hasResults;
    }
    
    
    protected static byte[]      bytes   = new byte[LittleEndianConsts.LONG_SIZE];
    protected static int         offset  = -1;
    protected static InputStream iStream = null;
    
    protected static long getLong() throws IOException
    {
        iStream.read(bytes);
        return LittleEndian.getLong(bytes, 0);
    }

    /**
     * Traverses the individual result record ifs and maps them into the result tables.
     * @param searchIdStr the ID of the Express Search definition
     * @param recId the record Id
     * @param idToTableInfoMap the TableInfo mapped by ID
     * @param joinIdToTableInfoMap the TableInfo mapped by Join ID
     * @param resultsMap the primary result tables
     * @param resultsForJoinsMap the related results table
     */
    public static boolean collectResults(final int    docInx,
                                         final String searchIdStr,
                                         final long   recId,
                                         final Hashtable<String, ExpressResultsTableInfo>       idToTableInfoMap,
                                         final Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoMap,
                                         final Hashtable<String, ExpressSearchResults>          resultsMap,
                                         final Hashtable<String, ExpressSearchResults>          resultsForJoinsMap)
    {
        boolean usingHitsCache = false;
        
        ExpressResultsTableInfo tblInfo;
        ExpressSearchResults    results = resultsMap.get(searchIdStr);
        if (results != null)
        {
            tblInfo = results.getTableInfo();
            
        } else
        {
            tblInfo = idToTableInfoMap.get(searchIdStr);
            if (tblInfo == null)
            {
                throw new RuntimeException("Bad id from search["+searchIdStr+"]");
            }
            results = new ExpressSearchResults(searchIdStr, null, tblInfo);
            resultsMap.put(searchIdStr, results);
        }

        if (tblInfo.isUseHitsCache())
        {
            results.addIndex(docInx);
            usingHitsCache = true;

        } else
        {        
            results.add(recId);
        }
        
        log.debug("Find any Joins for TableID ["+tblInfo.getTableId()+"]");
        List<ExpressResultsTableInfo> list = joinIdToTableInfoMap.get(tblInfo.getTableId());
        if (list != null)
        {
            for (ExpressResultsTableInfo erti : list)
            {
                log.debug("Checking up["+tblInfo.getTableId()+"]");
                results = resultsForJoinsMap.get(erti.getId());//tblInfo.getTableId());
                if (results == null)
                {
                    Integer joinColTableId = null;
                    ExpressResultsTableInfo.JoinColInfo joinCols[] = erti.getJoins();
                    if (joinCols != null)
                    {
                        for (ExpressResultsTableInfo.JoinColInfo jci :  joinCols)
                        {
                            if (tblInfo.getTableId().equals(jci.getJoinTableId()))
                            {
                                joinColTableId = jci.getJoinTableIdAsInt();
                                break;
                            }
                        }
                    }
                    if (joinColTableId == null)
                    {
                        throw new RuntimeException("Shouldn't have got here!");
                    }
                    log.debug("ExpressSearchResults erti.getId()["+erti.getId()+"] joinColTableId["+joinColTableId+"]");
                    results = new ExpressSearchResults(erti.getId(), joinColTableId, erti);
                    resultsForJoinsMap.put(erti.getId(), results);
                }
                results.add(recId);
            }
        }
        return usingHitsCache;
    }
    
    
    /**
     * Traverses through the results and adds to the panel to be displayed.
     * @param resultsMap the primary result tables
     * @param resultsForJoinsMap the related results table
     */
    protected static void displayResults(final ExpressSearchResultsPaneIFace           esrPane,
                                         final Hashtable<String, ExpressSearchResults> resultsMap,
                                         final Hashtable<String, ExpressSearchResults> resultsForJoinsMap,
                                         final Hits                                    hits)
    {
        // For Debug Only
        if (true)
        {
            for (Enumeration<ExpressSearchResults> e=resultsMap.elements();e.hasMoreElements();)
            {
                ExpressSearchResults results = e.nextElement();
                if (results.getRecIds().size() > 0)
                {
                    System.err.println("\n\n------------------------------------");
                    System.err.println("------------------------------------");
                    System.err.println("Search Id "+results.getTableInfo().getId()+" Table Id "+results.getTableInfo().getTableId());
                    System.err.println("------------------------------------");
                    for (Long l : results.getRecIds())
                    {
                        System.err.print(l+" ");
                    }
                    System.err.println();
                }
            }
    
            for (Enumeration<ExpressSearchResults> e=resultsForJoinsMap.elements();e.hasMoreElements();)
            {
                ExpressSearchResults results = e.nextElement();
                if (results.getRecIds().size() > 0)
                {
                    System.err.println("\n\n------------------------------------");
                    System.err.println("------------------------------------");
                    System.err.println("Search Id "+results.getTableInfo().getId() + 
                                       " Table Id "+results.getTableInfo().getTableId() + 
                                       " Column Name "+results.getJoinColTableId());
                    System.err.println("------------------------------------");
                    for (Long l : results.getRecIds())
                    {
                        System.err.print(l+" ");
                    }
                    System.err.println();
                }
            }
        }
        
        for (Enumeration<ExpressSearchResults> e=resultsMap.elements();e.hasMoreElements();)
        {
            ExpressSearchResults results = e.nextElement();
            if (results.getRecIds().size() > 0)//|| tableInfo.getNumIndexes() > 0)
            {
                esrPane.addSearchResults(results, hits);
                results.getRecIds().clear();
            }
        }
        resultsMap.clear();
        
        for (Enumeration<ExpressSearchResults> e=resultsForJoinsMap.elements();e.hasMoreElements();)
        {
            ExpressSearchResults results = e.nextElement();
            if (results.getRecIds().size() > 0)//|| tableInfo.getNumIndexes() > 0)
            {
                esrPane.addSearchResults(results, hits);
                results.getRecIds().clear();
            }
        }
        resultsForJoinsMap.clear();

    }
    
    /**
     * Executes a Remote query against the Servlet (proof of concept).
     * @param lucenePath the Path to the Lucene Directory
     * @param analyzer the analyzer to use
     * @param searchTextStr the search string to be searched
     * @param esrPane the desintation panel of the results
     * @return true if OK
     */
    public static boolean exeQueryRemote(final File      lucenePath,
                                        final Analyzer   analyzer,
                                        final String     searchTextStr,
                                        final ExpressSearchResultsPaneIFace esrPane)
    {
        
        HTTPGetter getter = new HTTPGetter();
        //System.out.println(new String(getter.doHTTPRequest("http://localhost:8080/sample/hello")));
        try
        {
            String encoded = StringEscapeUtils.escapeHtml(searchTextStr);
            iStream = getter.beginHTTPRequest("http://localhost:8080/sample/hello?q="+encoded+"&db="+SpecifyAppContextMgr.getInstance().getDatabaseName());
            
            long hits = getLong();
            log.debug("Hits: "+hits);
            
            if (hits > 0)
            {
                Hashtable<String, ExpressResultsTableInfo>       idToTableInfoMap     = getIdToTableInfoHash();
                Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoMap = getJoinIdToTableInfoHash();
                
                Hashtable<String, ExpressSearchResults>          resultsMap           = new Hashtable<String, ExpressSearchResults>();
                Hashtable<String, ExpressSearchResults>          resultsForJoinsMap   = new Hashtable<String, ExpressSearchResults>();
                
                long numTables = getLong();
                
                for (long i=0;i<numTables;i++)
                {
                    long   searchId  = getLong();
                    String searchIdStr = Long.toString(searchId);
                    @SuppressWarnings("unused")
                    long   useFloat = getLong();
                    long   numIds   = getLong();
                    //System.out.println("Table ["+tableId+"] useFloat["+useFloat+"] Num Ids["+numIds+"]");
                    
                   for (long inx=0;inx<numIds;inx++)
                    {
                        long recId = getLong();
                        collectResults(0, searchIdStr, recId, idToTableInfoMap, joinIdToTableInfoMap, resultsMap, resultsForJoinsMap);
                    }
                }
                
                displayResults(esrPane, resultsMap, resultsForJoinsMap, null);
                
                return true;
            }

            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * Executes a Local query directly against the Lucene index that is locally on disk.
     * @param lucenePath the Path to the Lucene Directory
     * @param analyzer the analyzer to use
     * @param searchTextStr the search string to be searched
     * @param esrPane the desintation panel of the results
     * @return true if OK
     */
    public static boolean exeQueryLocal(final File       lucenePath,
                                        final Analyzer   analyzer,
                                        final String     searchTextStr,
                                        final ExpressSearchResultsPaneIFace esrPane)
    {
        //Hashtable<String, ExpressResultsTableInfo>       tables               = getTableInfoHash();
        Hashtable<String, ExpressResultsTableInfo>       idToTableInfoMap     = getIdToTableInfoHash();
        Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoMap = getJoinIdToTableInfoHash();
        
        IndexSearcher searcher = null;
        
        try
        {
            // XXX sorting didn't work for some reason

            // Sort sort =  new Sort("table");
            // Sort sort2 =  new Sort(new SortField[] {new SortField("table", SortField.INT, true)});

            searcher = new IndexSearcher(FSDirectory.getDirectory(lucenePath, false));

            Query query;

            // Implicit AND
            QueryParser parser = new QueryParser("contents", analyzer);
            //parser.setOperator(QueryParser.DEFAULT_OPERATOR_AND);
            query = parser.parse(searchTextStr);
            //System.out.println(query.toString());

            Hits hits = searcher.search(query);
            
            if (hits.length() == 0)
            {
                log.debug("No Hits for ["+searchTextStr+"]["+query.toString()+"]");
                return false;
            }

            log.debug(hits.length()+" Hits for ["+searchTextStr+"]["+query.toString()+"]");

            Hashtable<String, ExpressSearchResults> resultsMap         = new Hashtable<String, ExpressSearchResults>();
            Hashtable<String, ExpressSearchResults> resultsForJoinsMap = new Hashtable<String, ExpressSearchResults>();
            
            int cntUseHitsCache = 0;
            // can be sped up if I figure out how to sort it
            for (int i=0;i<hits.length();i++)
            {
                Document doc = hits.doc(i);
                if (collectResults(i, doc.get("sid"), Long.parseLong(doc.get("id")), idToTableInfoMap, joinIdToTableInfoMap, resultsMap, resultsForJoinsMap))
                {
                    cntUseHitsCache++;
                } 
            }
            
            displayResults(esrPane, resultsMap, resultsForJoinsMap, hits);
            
            searcher.close();
            
            return true;

        } catch (ParseException ex)
        {
            JOptionPane.showMessageDialog(UICacheManager.get(UICacheManager.FRAME), getResourceString("BadQuery"), getResourceString("BadQueryTitle"), JOptionPane.ERROR_MESSAGE);
            log.error(ex);

        } catch (IOException ex)
        {
            // XXX Change message
            JOptionPane.showMessageDialog(UICacheManager.get(UICacheManager.FRAME), getResourceString("BadQuery"), getResourceString("BadQueryTitle"), JOptionPane.ERROR_MESSAGE);
            log.error(ex);
        }
        
        if (searcher != null)
        {
            try
            {
                searcher.close();
                
            } catch (IOException ex)
            {
                log.error(ex);
            }
        }
        return false;
    }

    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return new SimpleDescPane(name, this, "This is the Express Search Pane");
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getNavBoxes()
     */
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

        RecordSetTask rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);

        extendedNavBoxes.addAll(rsTask.getNavBoxes());

        return extendedNavBoxes;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();

        // Create Search Panel
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        JPanel     searchPanel = new JPanel(gridbag);
        JLabel     spacer      = new JLabel(" ");

        searchBtn = new JButton(getResourceString("Search"));
        searchBtn.setToolTipText(getResourceString("ExpressSearchTT"));

        //searchText  = new JTextField("[19510707 TO 19510711]", 10);//"beanii"
        //searchText  = new JTextField("beanii", 15);
                
        searchText = new JAutoCompTextField(15, PickListDBAdapterFactory.getInstance().create("ExpressSearch", true));
        searchText.setAskBeforeSave(false);
        
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        //searchText.setText("2004-IZ-121");
        //searchText.setText("platostomus");
        searchText.setText(localPrefs.get(LAST_SEARCH, ""));
        
        //searchText  = searchTextAutoComp.getTnew JTextField("2004-IZ-121", 15);

        //searchText  = new JTextField(10);
        textBGColor = searchText.getBackground();

        searchText.setMinimumSize(new Dimension(50, searchText.getPreferredSize().height));

        ActionListener doQuery = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doQuery();
            }
        };

        searchBtn.addActionListener(doQuery);
        searchText.addActionListener(doQuery);
        searchText.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                if (searchText.getBackground() != textBGColor)
                {
                    searchText.setBackground(textBGColor);
                }
            }


        });


        c.weightx = 1.0;
        gridbag.setConstraints(spacer, c);
        searchPanel.add(spacer);

        c.weightx = 0.0;
        gridbag.setConstraints(searchText, c);
        searchPanel.add(searchText);

        searchPanel.add(spacer);

        gridbag.setConstraints(searchBtn, c);
        searchPanel.add(searchBtn);

        list.add(new ToolBarItemDesc(searchPanel));

        checkForIndexer();

        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        return new Vector<MenuItemDesc>();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getTaskClass()
     */
    @Override
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
    //----------------------------------------------------------------
    //-- CommandListener Interface
    //----------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(APP_CMD_TYPE))
        {
            if (cmdAction.isAction(APP_RESTART_ACT))
            {
                lucenePath = getIndexDirPath(); // must be initialized here (again)
                checkForIndexer();
            }
            
        } else if (cmdAction.isType(EXPRESSSEARCH))
        {
            if (cmdAction.isAction(CHECK_INDEXER_PATH))
            {
                lucenePath = getIndexDirPath(); // must be initialized here (again)
                checkForIndexer();
            }  
        }
    }
    
    //------------------------------------------------
    //-- ExpressSearchIndexerListener
    //------------------------------------------------

   /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.ExpressSearchIndexerPane.ExpressSearchIndexerListener#doneIndexing()
     */
    public void doneIndexing()
    {
        checkForIndexer();
    }
    
    //------------------------------------------------
    //-- TableInfoWeakRef Inner Class
    //------------------------------------------------
    class TableInfoWeakRef
    {
        Hashtable<String, ExpressResultsTableInfo>       tables                = new Hashtable<String, ExpressResultsTableInfo>();
        
        Hashtable<String, ExpressResultsTableInfo>       idToTableInfoHash     = new Hashtable<String, ExpressResultsTableInfo>();
        Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash = new Hashtable<String, List<ExpressResultsTableInfo>>();
        
        public TableInfoWeakRef(Hashtable<String, ExpressResultsTableInfo> tables, Hashtable<String, ExpressResultsTableInfo> idToTableInfoHash, Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash)
        {
            super();
            this.tables = tables;
            this.idToTableInfoHash = idToTableInfoHash;
            this.joinIdToTableInfoHash = joinIdToTableInfoHash;
        }

        public Hashtable<String, ExpressResultsTableInfo> getIdToTableInfoHash()
        {
            return idToTableInfoHash;
        }

        public Hashtable<String, List<ExpressResultsTableInfo>> getJoinIdToTableInfoHash()
        {
            return joinIdToTableInfoHash;
        }

        public Hashtable<String, ExpressResultsTableInfo> getTables()
        {
            return tables;
        }

    }

}
