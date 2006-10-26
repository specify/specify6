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
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.ExpressSearchIndexerPane;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.helpers.HTTPGetter;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPane;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.db.JAutoCompTextField;
import edu.ku.brc.ui.db.PickListDBAdapter;
/**
 * This task will enable the user to index there database and preform express searches
 
 * @code_status Complete
 **
 * @author rods
 *
 */
public class ExpressSearchTask extends BaseTask implements CommandListener, ExpressSearchIndexerPane.ExpressSearchIndexerListener
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(ExpressSearchTask.class);

    public static final String EXPRESSSEARCH = "Express_Search";

    // Data Members
    protected Analyzer                     analyzer       = new StandardAnalyzer();
    protected File                         lucenePath     = null;
    protected JAutoCompTextField           searchText;
    protected JButton                      searchBtn;
    protected Color                        textBGColor    = null;
    protected Color                        badSearchColor = new Color(255,235,235);

    protected static WeakReference<Hashtable<String, ExpressResultsTableInfo>> tableHash = null;

    /**
     * Deafult Constructor.
     */
    public ExpressSearchTask()
    {
        super(EXPRESSSEARCH, getResourceString(EXPRESSSEARCH));
        icon = IconManager.getIcon("Search", IconManager.IconSize.Std16);

        lucenePath = getIndexDirPath(); // must be initialized here
        
        CommandDispatcher.register("App", this);
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
     * Returns the HashMap of ExpressResultsTableInfo items.
     * @return the HashMap of ExpressResultsTableInfo items
     */
    public static Hashtable<String, ExpressResultsTableInfo> getTableInfoHash()
    {

        Hashtable<String, ExpressResultsTableInfo> tableInfoHashMap = null;
        if (tableHash != null)
        {
            tableInfoHashMap = tableHash.get();
        }
        
        if (tableInfoHashMap == null)
        {
            tableInfoHashMap = intializeTableInfo();
            tableHash = new WeakReference<Hashtable<String, ExpressResultsTableInfo>>(tableInfoHashMap);
        }
        return tableInfoHashMap;
    }

    /**
     * Helper function to return the path to the express search directory.
     * @return return the path to the express search directory
     */
    public static File getIndexDirPath()
    {
        String subName = "";
        SpecifyAppContextMgr appContext = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        CollectionObjDef colObjDef = CollectionObjDef.getCurrentCollectionObjDef();
        if (colObjDef != null)
        {
            subName = colObjDef.getName();
        } else
        {
            subName = appContext.getDatabaseName();
        }
        
        File path = new File(UICacheManager.getDefaultWorkingPath()+File.separator+subName+File.separator+"index-dir");
        if (!path.exists())
        {
            if (!path.mkdirs())
            {
                String msg = "unable to create directory [" + path.getAbsolutePath() + "]";
                log.error(msg);
                throw new RuntimeException(msg);
            }
        }
        return path;
    }

    /**
     * Collects information about all the tables that will be processed for any search.
     * @param tableItems the list of Elements to be processed
     * @param tables the table info hash
     */
    protected static void intializeTableInfo(final List tableItems, 
                                             final Hashtable<String, ExpressResultsTableInfo> tables,
                                             final Hashtable<String, ExpressResultsTableInfo> byIdHash,
                                             final boolean isExpressSearch)
    {
        for ( Iterator iter = tableItems.iterator(); iter.hasNext(); )
        {
            Element                 tableElement = (Element)iter.next();
            ExpressResultsTableInfo tableInfo    = new ExpressResultsTableInfo(tableElement, ExpressResultsTableInfo.LOAD_TYPE.Both, isExpressSearch);
            if (byIdHash.get(tableInfo.getId()) == null)
            {
                byIdHash.put(tableInfo.getId(), tableInfo);
                
                if (tables.get(tableInfo.getName()) == null)
                {
                    tables.put(tableInfo.getName(), tableInfo);

                } else
                {
                    log.error("Duplicate express Search name["+tableInfo.getName()+"]");
                }

            } else
            {
                log.error("Duplicate Search Id["+tableInfo.getId()+"]");
            }
        } 
    }

    /**
     * Collects information about all the tables that will be processed for any search.
     * @return hash of named ExpressResultsTableInfo
     */
    protected static Hashtable<String, ExpressResultsTableInfo> intializeTableInfo()
    {
        Hashtable<String, ExpressResultsTableInfo> tables   = new Hashtable<String, ExpressResultsTableInfo>();
        Hashtable<String, ExpressResultsTableInfo> byIdHash = new Hashtable<String, ExpressResultsTableInfo>();
        try
        {
            Element esDOM = AppContextMgr.getInstance().getResourceAsDOM("SearchConfig"); // Describes the definitions of the full text search
            
            intializeTableInfo(esDOM.selectNodes("/searches/express/table"), tables, byIdHash, true);
            
            intializeTableInfo(esDOM.selectNodes("/searches/generic/table"), tables, byIdHash, false);
                
        } catch (Exception ex)
        {
            log.error(ex);
        }
        byIdHash.clear();
        
        return tables;
    }

    /**
     * Check to see of the index has been run and then enables the express search controls.
     *
     */
    public void checkForIndexer()
    {
        boolean exists = lucenePath.exists() && lucenePath.list().length > 0;
        
        log.debug(lucenePath.getAbsoluteFile() + " has index " + (lucenePath.list().length > 0));
        
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
            if (doQuery(lucenePath, analyzer, searchText, badSearchColor, getTableInfoHash(), expressSearchPane))
            {
                SubPaneMgr.getInstance().addPane(expressSearchPane);
            } else
            {
                UICacheManager.displayLocalizedStatusBarText("NoExpressSearchResults");
            }
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
                                  final Hashtable<String, ExpressResultsTableInfo> tables,
                                  final ExpressSearchResultsPaneIFace esrPane)
    {
        return doQuery(lucenePath, analyzer, searchText, null, badSearchColor, tables, esrPane);
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
                                  final Hashtable<String, ExpressResultsTableInfo> tables,
                                  final ExpressSearchResultsPaneIFace esrPane)
    {
        return doQuery(lucenePath, analyzer, null, searchTextStr, null, tables, esrPane);
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
                                  final Hashtable<String, ExpressResultsTableInfo> tables,
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
                hasResults = exeQueryLocal(lucenePath, analyzer, searchTerm, tables, esrPane);
            } else
            {
                hasResults = exeQueryRemote(lucenePath, analyzer, searchTerm, tables, esrPane);
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
     * @param lucenePath
     * @param analyzer
     * @param searchTextStr
     * @param tables
     * @param esrPane
     * @return
     */
    public static boolean exeQueryRemote(final File      lucenePath,
                                        final Analyzer   analyzer,
                                        final String     searchTextStr,
                                        final Hashtable<String, ExpressResultsTableInfo> tables,
                                        final ExpressSearchResultsPaneIFace esrPane)
    {
        
        HTTPGetter getter = new HTTPGetter();
        //System.out.println(new String(getter.doHTTPRequest("http://localhost:8080/sample/hello")));
        
        try
        {
            String encoded = StringEscapeUtils.escapeHtml(searchTextStr);
            iStream = getter.beginHTTPRequest("http://localhost:8080/sample/hello?q="+encoded+"&db="+SpecifyAppContextMgr.getInstance().getDatabaseName());
            
            long hits = getLong();
            //System.out.println("Hits: "+hits);
            
            if (hits > 0)
            {
                
                // "tables" maps by name so create a hash for mapping by ID
                Hashtable<String, ExpressResultsTableInfo> idToTableInfoMap = new Hashtable<String, ExpressResultsTableInfo>();

                for (Enumeration<ExpressResultsTableInfo> e=tables.elements();e.hasMoreElements();)
                {
                    ExpressResultsTableInfo ti = e.nextElement();
                    if (ti.isExpressSearch())
                    {
                        idToTableInfoMap.put(ti.getId(), ti);
                    }
                }
                
                long numTables = getLong();
                //System.out.println("Num of Tables: "+numTables);
                
                for (long i=0;i<numTables;i++)
                {
                    long tableId  = getLong();
                    @SuppressWarnings("unused")
                    long useFloat = getLong();
                    long numIds   = getLong();
                    //System.out.println("Table ["+tableId+"] useFloat["+useFloat+"] Num Ids["+numIds+"]");
                    
                    ExpressResultsTableInfo tableInfo = idToTableInfoMap.get(Long.toString(tableId));
                    Vector<Integer> recIds = tableInfo.getRecIds();
                    
                    for (long inx=0;inx<numIds;inx++)
                    {
                        long recId = getLong();
                        recIds.add((int)recId);
                        //System.out.print(recId + ", ");
                    }
                    //System.out.println();
                }
                
                for (Enumeration<ExpressResultsTableInfo> e=tables.elements();e.hasMoreElements();)
                {
                    ExpressResultsTableInfo tableInfo = e.nextElement();
                    if (tableInfo.getRecIds().size() > 0 || tableInfo.getNumIndexes() > 0)
                    {
                        esrPane.addSearchResults(tableInfo, null);
                        tableInfo.getRecIds().clear();
                    }
                }
                return true;
            }

            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    
    /**
     * @param lucenePath
     * @param analyzer
     * @param searchTextStr
     * @param tables
     * @param esrPane
     * @return
     */
    public static boolean exeQueryLocal(final File      lucenePath,
                                        final Analyzer   analyzer,
                                        final String     searchTextStr,
                                        final Hashtable<String, ExpressResultsTableInfo> tables,
                                        final ExpressSearchResultsPaneIFace esrPane)
    {

        try
        {
            // XXX sorting didn't work for some reason

            // Sort sort =  new Sort("table");
            // Sort sort2 =  new Sort(new SortField[] {new SortField("table", SortField.INT, true)});

            IndexSearcher searcher = new IndexSearcher(FSDirectory.getDirectory(lucenePath, false));

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

            // "tables" maps by name so create a hash for mapping by ID
            Hashtable<String, ExpressResultsTableInfo> idToTableInfoMap = new Hashtable<String, ExpressResultsTableInfo>();

            for (Enumeration<ExpressResultsTableInfo> e=tables.elements();e.hasMoreElements();)
            {
                ExpressResultsTableInfo ti = e.nextElement();
                if (ti.isExpressSearch())
                {
                	idToTableInfoMap.put(ti.getId(), ti);
                }                   
            }

            log.debug(hits.length()+" Hits for ["+searchTextStr+"]["+query.toString()+"]");

            boolean useFloat = false;

            int cntUseHitsCache = 0;
            // can be sped up if I figure out how to sort it
            for (int i=0;i<hits.length();i++)
            {
                Document                doc         = hits.doc(i);
                String                  searchIdStr = doc.get("sid");
                ExpressResultsTableInfo tableInfo   = idToTableInfoMap.get(searchIdStr);
                if (tableInfo == null)
                {
                    throw new RuntimeException("Bad id from search["+searchIdStr+"]");
                }

                if (tableInfo.isUseHitsCache())
                {
                    tableInfo.addIndex(i);
                    cntUseHitsCache++;

                } else
                {
                    try
                    {
                        if (useFloat)
                        {
                            tableInfo.getRecIds().add((int)(Float.parseFloat(doc.get("id"))));
                        } else
                        {
                            tableInfo.getRecIds().add((Integer.parseInt(doc.get("id"))));
                        }
                    } catch (java.lang.NumberFormatException e)
                    {
                        useFloat = true;
                        tableInfo.getRecIds().add((int)(Float.parseFloat(doc.get("id"))));
                    }
                }
            }

            for (Enumeration<ExpressResultsTableInfo> e=tables.elements();e.hasMoreElements();)
            {
                ExpressResultsTableInfo tableInfo = e.nextElement();
                if (tableInfo.getRecIds().size() > 0 || tableInfo.getNumIndexes() > 0)
                {
                    esrPane.addSearchResults(tableInfo, hits);
                    tableInfo.getRecIds().clear();
                }
            }

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

        searchBtn   = new JButton(getResourceString("Search"));
        searchBtn.setToolTipText(getResourceString("ExpressSearchTT"));

        //searchText  = new JTextField("[19510707 TO 19510711]", 10);//"beanii"
        //searchText  = new JTextField("beanii", 15);
                
        searchText = new JAutoCompTextField(15, new PickListDBAdapter("ExpressSearch", true));
        searchText.setAskBeforeSave(false);
        
        searchText.setText("2004-IZ-121");
        
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
     * @see edu.ku.brc.af.core.Taskable#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        return new Vector<MenuItemDesc>();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getTaskClass()
     */
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
    //----------------------------------------------------------------
    //-- CommandListener Interface
    //----------------------------------------------------------------
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.getType().equals("App") && cmdAction.getAction().equals("Restart"))
        {
            lucenePath = getIndexDirPath(); // must be initialized here (again)
            
            checkForIndexer();
        }

    }
    
    //------------------------------------------------
    //-- ExpressSearchIndexerListener
    //------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchIndexerPane.ExpressSearchIndexerListener#doneIndexing()
     */
    public void doneIndexing()
    {
        checkForIndexer();
    }

}
