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
package edu.ku.brc.af.tasks.subpane;


import static edu.ku.brc.ui.UICacheManager.getResourceString;
import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.getString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ExpressResultsTableInfo;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.PairsMultipleQueryResultsHandler;
import edu.ku.brc.dbsupport.QueryResultsContainer;
import edu.ku.brc.dbsupport.QueryResultsDataObj;
import edu.ku.brc.dbsupport.QueryResultsListener;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.forms.persist.FormViewDef;

/**
 * A pane enables the user to see (and control) the indexing process for express search.<BR>
 * NOTE: This creates the index cache locality, it doesn't support is being on the network.
 *
 * For each table defined in search_config.xml they are parsed into a ExpressResultsTableInfo object
 * which is used for doing the indexing amd for doing the search results
 *
 * NOTE: XXX The indexing of the database needs to be abstracted out to be able to run "headless"<br>
 * That way we could run it on a server. The idea is that the UI would be accessed via a proxy
 * and the headless could be a "do nothing stub".
 *
 * We need to implement the the part where it only updates the tables that have been changed.
 * This is is extra work because we need to remove all the indexes out of Lucene for that table type.
 * 
 * @code_status Beta
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ExpressSearchIndexerPane extends BaseSubPane implements Runnable, QueryResultsListener
{
    // Static Data Members
    private static final Logger   log      = Logger.getLogger(ExpressSearchIndexerPane.class);
    private static final Analyzer analyzer = new StandardAnalyzer();//WhitespaceAnalyzer();

    // Data Members
    protected Thread       thread;
    protected File         lucenePath    = null;
    protected Element      esDOM         = null;
    protected JLabel       indvLabel;
    protected JLabel       globalLabel;
    protected JProgressBar globalProgressBar;
    protected long         termsIndexed  = 0;
    protected boolean      isCancelled   = false;
    protected JButton      cancelBtn;
    protected JButton      closeBtn;
    protected JCheckBox    forceChkbx;

    protected ImageIcon    checkIcon     = new ImageIcon(IconManager.getImagePath("check.gif"));  // Move to icons.xml
    protected ImageIcon    exclaimIcon   = new ImageIcon(IconManager.getImagePath("exclaim.gif"));
    protected ImageIcon    exclaimYWIcon = new ImageIcon(IconManager.getImagePath("exclaim_yellow.gif"));

    protected PairsMultipleQueryResultsHandler handler = null;

    protected Hashtable<String, Boolean>   outOfDateHash = new Hashtable<String, Boolean>();
    protected Hashtable<String, JLabel>    resultsLabels = new Hashtable<String, JLabel>();
    protected JPanel                       resultsPanel;
    protected Font                         captionFont   = null;
    protected JLabel                       explainLabel;
    protected boolean                      noIndexFile   = false;

    protected boolean                      doIndexForms  = false; // XXX Pref
    protected boolean                      doIndexLabels = false; // XXX Pref
    
    protected IndexWriter                  optWriter     = null;
    protected ExpressSearchIndexerListener esipListener = null;

    /**
     * Default Constructor.
     *
     * @param task the owning task
     * @param esipListener the ExpressSearchIndexerPaneListener
     * @param lucenePath the path to the Lucene index
     */
    public ExpressSearchIndexerPane(final Taskable task, final ExpressSearchIndexerListener esipListener, final File lucenePath)
    {
        super(getResourceString("IndexerPane"), task);

        this.lucenePath   = lucenePath;
        this.esipListener = esipListener;
        
        startCheckOutOfDateProcess(); // must be done before openingScreenInit

        openingScreenInit();

    }

    /**
     * @return check to see if luceen exist and whether it is empty
     */
    protected boolean isLuceneEmpty()
    {
        if (lucenePath.exists())
        {
            return lucenePath.list().length == 0;
        }
        return false;
    }

    /**
     * Read in all the tables that need to be checked
     *
     */
    protected void startCheckOutOfDateProcess()
    {
        Vector<QueryResultsContainer> list = new Vector<QueryResultsContainer>();

        noIndexFile = isLuceneEmpty();

        try
        {
            if (esDOM == null)
            {
                esDOM = AppContextMgr.getInstance().getResourceAsDOM("SearchConfig"); // Describes the definitions of the full text search
            }

            Hashtable<String, String> namesHash = new Hashtable<String, String>();

            List tables = esDOM.selectNodes("/searches/express/table/outofdate/table");
            for ( Iterator iter = tables.iterator(); iter.hasNext(); )
            {
                Element tableElement = (Element)iter.next();
                namesHash.put(tableElement.attributeValue("name"), tableElement.attributeValue("title"));
            }

            int numRowDefs = (namesHash.size() / 2) + namesHash.size() % 2;
            PanelBuilder    builder = new PanelBuilder(new FormLayout("p:g,2dlu,p:g,20px,p:g,2dlu,p:g", createDuplicateJGoodiesDef("p","5px", numRowDefs)));
            CellConstraints cc      = new CellConstraints();

            if (captionFont == null)
            {
                Font curFont = getFont();
                captionFont = new Font(curFont.getFontName(), Font.BOLD, 14);
            }
            
            Timestamp oldTimestamp = new Timestamp(new Date().getTime()-1000);

            // NOTE: Each database check is added with the table name 
            // from the outofdate/tables list within the search definitions
            int row = 1;
            int col = 1;
            for (Enumeration<String> e=namesHash.keys();e.hasMoreElements();)
            {
                String nameStr = e.nextElement();
                
                // Find the last Created Timestamp
                String sqlStr = "select TimestampCreated from "+nameStr+" order by TimestampCreated desc limit 0,1"; // TODO This needs to be per DB PLATFORM
                log.debug(sqlStr);
                QueryResultsContainer container = new QueryResultsContainer(sqlStr);
                container.add(new QueryResultsDataObj(nameStr));

                // Since the index doesn't exist fake like
                // each table has at least one out of date record
                container.add(noIndexFile ? new QueryResultsDataObj(oldTimestamp) : new QueryResultsDataObj(1,1));
                list.add(container);
                
                // Now find the last Modified Timestamp
                sqlStr = "select TimestampModified from "+nameStr+" order by TimestampModified desc limit 0,1"; // TODO This needs to be per DB PLATFORM
                log.debug(sqlStr);
                container = new QueryResultsContainer(sqlStr);
                container.add(new QueryResultsDataObj(nameStr));

                // Since the index doesn't exist fake like
                // each table has at least one out of date record
                container.add(noIndexFile ? new QueryResultsDataObj(oldTimestamp) : new QueryResultsDataObj(1,1));
                list.add(container);
                
                JLabel label = new JLabel(namesHash.get(nameStr)+":", JLabel.RIGHT);
                label.setFont(captionFont);

                builder.add(label, cc.xy(col,row));
                label = new JLabel(exclaimYWIcon);

                resultsLabels.put(nameStr, label);
                builder.add(label, cc.xy(col+2,row));
                
                col += 4;
                if (col > 7)
                {
                    col = 1;
                    row += 2;
                }
                
            }

            resultsPanel = builder.getPanel();

        } catch (Exception ex)
        {
            log.error(ex);
        }

        handler = new PairsMultipleQueryResultsHandler();
        handler.init(this, list);

        // We won't start it up because we no it doesn't exist
        // so there is no reason check everything
        if (!noIndexFile)
        {
            handler.startUp();
        }
    }

    /**
     *
     *
     */
    protected void openingScreenInit()
    {
        // Create Start up UI
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p:g,2dlu,p:g", createDuplicateJGoodiesDef("c:p", "5dlu", 8)));
        CellConstraints cc         = new CellConstraints();

        removeAll();

        NavBoxButton configureBtn = new NavBoxButton(getResourceString("Configure"), IconManager.getImage("Configure", IconManager.IconSize.Std32));
        NavBoxButton buildBtn     = new NavBoxButton(getResourceString("Build"), new ImageIcon(IconManager.getImagePath("build.gif")));
        
        forceChkbx = new JCheckBox(getResourceString("ForceLuceneUpdate"));
        forceChkbx.setEnabled(false); // doing this for now until we can fix it.
        forceChkbx.setSelected(true);
        
        configureBtn.setVerticalLayout(true);
        buildBtn.setVerticalLayout(true);

        JLabel label = new JLabel(getResourceString("ESIndexerCaption"), JLabel.CENTER);
        label.setFont(captionFont);

        int row = 1;
        builder.add(label, cc.xywh(1,row,3,1));
        row+=2;
        builder.add(new JSeparator(JSeparator.HORIZONTAL), cc.xywh(1,row,3,1));
        row+=2;
        builder.add(resultsPanel, cc.xywh(1,row,3,1));
        row+=2;
        builder.add(forceChkbx, cc.xywh(1,row, 3, 1));
        row+=2;
        builder.add(new JSeparator(JSeparator.HORIZONTAL), cc.xywh(1,row,3,1));
        row+=2;
        builder.add(configureBtn, cc.xy(1,row));
        builder.add(buildBtn, cc.xy(3,row));
        row+=2;

        explainLabel = new JLabel(getResourceString("UpdatingES"), JLabel.CENTER);
        //explainLabel.setFont(new Font(getFont().getFontName(), Font.ITALIC, getFont().getSize()));
        builder.add(explainLabel, cc.xywh(1,row,3,1));
        row+=2;

        //builder.getPanel().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        builder.getPanel().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(5,5,5,5)));
        PanelBuilder    builder2    = new PanelBuilder(new FormLayout("center:p:g", "center:p:g"));
        builder2.add(builder.getPanel(), cc.xy(1,1));

        builder2.getPanel().setBackground(Color.WHITE);
        add(builder2.getPanel(), BorderLayout.CENTER);

        configureBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(UICacheManager.get(UICacheManager.FRAME), "Sorry, not implemented yet.");
            }
        });

        buildBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                buildInit();
            }
        });

        // Since we have no index then go ahead and display the resullts
        if (noIndexFile)
        {
            allResultsBack();
        }

    }
    
    /**
     * Returns the analyzer.
     * @return the analyzer.
     */
    public static Analyzer getAnalyzer()
    {
        return analyzer;
    }
    
    /**
     * Create the Lucene directory if it doesn't exist. Then create a new index if the dir didn't exist or "create" is true.
     * @param create indicates whether the index should be created, this also can be used to delete and start over.
     * @return creates and returns a new IndexWriter.
     * @throws IOException
     */
    public static IndexWriter createIndexWriter(final File path, final boolean create) throws IOException
    {
        boolean     shouldBeCreated = path.exists();
        Directory   dir             = FSDirectory.getDirectory(path, true);
        IndexWriter writer          = new IndexWriter(dir, analyzer, create || shouldBeCreated);
        //writer.setMaxBufferedDocs(arg0);
        writer.setMaxMergeDocs(9999999);
        writer.setMergeFactor(UIHelper.getOSType() == UIHelper.OSTYPE.Windows ? 1000 : 100);
        //writer.mergeFactor   = 1000;
        //writer.maxMergeDocs  = 9999999;
        //writer.minMergeDocs  = 1000;
        return writer;
    }
    
    /**
     * @param rs
     * @param secondaryKey
     * @param objClass
     */
    public String indexValue(final Document doc,
                              final ResultSet rs,
                              final int index,
                              final String fieldName,
                              final String secondaryKey,
                              final Class objClass,
                              final DateFormat formatter) throws SQLException
    {
        String value = null;

        // There may be a better way to express this,
        // but this is very explicit as to whether it is indexed as a Keyword or not
        if (secondaryKey == null)
        {
            if (objClass == java.sql.Date.class)
            {
                Date date = rs.getDate(index);
                if (date != null)
                {
                    value = formatter.format(date);
                    if (fieldName == null)
                    {
                        doc.add(new Field(fieldName, value, Field.Store.YES, Field.Index.UN_TOKENIZED));
                        //doc.add(Field.Keyword(fieldName, value));
                    } else
                    {
                        doc.add(new Field("contents", value, Field.Store.NO, Field.Index.TOKENIZED));
                        //doc.add(Field.UnStored("contents", value));
                    }
                    //log.debug("["+fieldName+"]["+secondaryKey+"]["+value+"]");
                }

            } else
            {
                String str = rs.getString(index);
                if (isNotEmpty(str))
                {
                    value = str;
                    doc.add(new Field("contents", str, Field.Store.NO, Field.Index.TOKENIZED));
                    //doc.add(Field.UnStored("contents", str));
                }
            }

        } else
        {
            if (objClass == java.sql.Date.class)
            {
                Date date = rs.getDate(index);
                if (date != null)
                {
                    value = formatter.format(date);
                    if (fieldName == null)
                    {
                        doc.add(new Field(fieldName, value, Field.Store.YES, Field.Index.UN_TOKENIZED));
                        //doc.add(Field.Keyword(fieldName, value));
                    } else
                    {
                        doc.add(new Field("contents", value, Field.Store.NO, Field.Index.TOKENIZED));
                        //doc.add(Field.UnStored("contents", value));
                    }
                    //log.debug("["+fieldName+"]["+secondaryKey+"]["+value+"]");
                }

            } else
            {
                String str = rs.getString(index);
                if (isNotEmpty(str))
                {
                    value = str;
                    doc.add(new Field(secondaryKey, str, Field.Store.YES, Field.Index.UN_TOKENIZED));
                    //doc.add(Field.Keyword(secondaryKey, str));
                }
            }
        }
        //log.debug("["+fieldName+"]["+secondaryKey+"]["+value+"]");
        return value;
    }

    /**
     * Performs a query and then indexes all the results for each orw and column
     * @param writer the lucene writer
     * @param tableInfo info describing the table (hold the table ID)
     */
    public long indexQuery(final IndexWriter writer, ExpressResultsTableInfo tableInfo)
    {
        DateFormat formatter    = new SimpleDateFormat("yyyyMMdd");

        Connection dbConnection = DBConnection.getInstance().createConnection();
        Statement  dbStatement  = null;
        boolean    useHitsCache = tableInfo.isUseHitsCache();
        
        ExpressResultsTableInfo.ColInfo     colInfo[]     = tableInfo.getCols();
        ExpressResultsTableInfo.JoinColInfo joinColInfo[] = tableInfo.getJoins();

        StringBuilder strBuf = new StringBuilder(128);

        long begin = 0;

        progressBar.setIndeterminate(true);

        try
        {
            if (dbConnection != null)
            {
                dbStatement = dbConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

                log.debug("SQL ["+tableInfo.getBuildSql()+"]");

                ResultSet rs = dbStatement.executeQuery(tableInfo.getBuildSql());

                begin = new Date().getTime();

                double numRows = 0;
                int    trigger = -1;
                int    step    = 0;
                if (rs.last())
                {
                    numRows = rs.getRow();
                    progressBar.setMaximum((int)numRows);
                    progressBar.setValue(0);
                    progressBar.setIndeterminate(false);
                    progressBar.setString("0%");
                    progressBar.setStringPainted(true);
                    trigger = (int)(numRows * 0.02);
                    
                } else
                {
                    log.debug("Table["+tableInfo.getTitle()+"] is empty.");
                    //throw new RuntimeException("Can't go to last record.");
                }
                log.debug("Row ["+numRows+"] to index in ["+tableInfo.getTitle()+"].");

                if (rs.first())
                {
                    // First we create an array of Class so we know what each column's Object class is
                    ResultSetMetaData rsmd = rs.getMetaData();
                    Class[]  classes = new Class[rsmd.getColumnCount()+1];
                    classes[0] = null; // we do this so the "1" based columns match up with the list
                    for (int i=1;i<rsmd.getColumnCount();i++)
                    {
                        try
                        {
                            classes[i] = Class.forName(rsmd.getColumnClassName(i));
                            //log.debug(rsmd.getColumnName(i)+"  "+classes[i].getSimpleName());
                        } catch (Exception ex) {  }
                    }

                    String idStr = tableInfo.getId();

                    int rowCnt = 1;
                    do
                    {
                        if (step == trigger)
                        {
                            progressBar.setValue(rowCnt);
                            progressBar.setString((int)((rowCnt / numRows) * 100.0)+"%");
                            step = 0;
                        }

                        step++;
                        rowCnt++;
                        
                        Document doc = new Document();
                        doc.add(new Field("id", rs.getString(tableInfo.getIdColIndex()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                        doc.add(new Field("sid", idStr, Field.Store.YES, Field.Index.NO));
                        //doc.add(new Field("class", tableInfo.getName(), Field.Store.YES, Field.Index.NO));


                        int cnt = 0;
                        if (useHitsCache)
                        {
                            strBuf.setLength(0);
                            
                            for (ExpressResultsTableInfo.JoinColInfo jci : joinColInfo)
                            {
                                doc.add(new Field(jci.getJoinTableId(), rs.getString(jci.getPosition()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                            }
                            
                            for (int i=0;i<colInfo.length;i++)
                            {
                                ExpressResultsTableInfo.ColInfo ci = colInfo[i];
                                if (i > 0)
                                {
                                    int inx = ci.getPosition();
                                    String value = indexValue(doc, rs, inx,
                                                              rsmd.getColumnName(inx),
                                                              ci.getSecondaryKey(),
                                                              classes[inx],
                                                              formatter);
                                    if (value != null)
                                    {
                                        cnt++;
                                        termsIndexed++;
                                        strBuf.append(value);
                                        strBuf.append('\t');

                                    } else
                                    {
                                        strBuf.append(" \t");
                                    }

                                    if (isCancelled)
                                    {
                                        dbStatement.close();
                                        dbConnection.close();
                                        return 0;
                                    }
                                } else
                                {
                                    strBuf.append(" \t");
                                }
                            }

                            doc.add(new Field("data", strBuf.toString(), Field.Store.YES, Field.Index.NO));
                            //doc.add(Field.UnIndexed("data", strBuf.toString()));

                        } else
                        {
                            for (ExpressResultsTableInfo.JoinColInfo jci : joinColInfo)
                            {
                                //if (jci.getJoinTableId().equals("5"))
                                //{
                                //    System.out.println(jci.getJoinTableId()+" "+rs.getString(jci.getPosition()));
                                //}
                                doc.add(new Field(jci.getJoinTableId(), rs.getString(jci.getPosition()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                            }
                            
                            for (int i=0;i<colInfo.length;i++)
                            {
                                ExpressResultsTableInfo.ColInfo ci = colInfo[i];
                                int inx = ci.getPosition();

                                if (indexValue(doc, rs, inx, rsmd.getColumnName(inx), ci.getSecondaryKey(),
                                               classes[inx], formatter) != null)
                                {
                                    cnt++;
                                    termsIndexed++;
                                }

                                if (isCancelled)
                                {
                                    dbStatement.close();
                                    dbConnection.close();
                                    return 0;
                                }

                            }
                        }
                        if (cnt > 0)
                        {
                            writer.addDocument(doc);
                        }
                    } while(rs.next());
                    log.debug("done indexing");
                }
                progressBar.setString("");

                rs.close();
                dbStatement.close();
                dbConnection.close();
            } else
            {
                throw new RuntimeException("WHy are new Here?");
            }

        } catch (java.sql.SQLException ex)
        {
            //ex.printStackTrace();
            log.error("Error in run["+tableInfo.getBuildSql()+"]", ex);
            
        } catch (Exception ex)
        {
            //ex.printStackTrace();
            log.error("Error in run["+tableInfo.getBuildSql()+"]", ex);
        }
        long end = new Date().getTime();

        long delta = begin > 0 ? end - begin : 0;
        log.debug("Time to index (" + delta + " ms)");
        return delta;
    }

    /**
     * Creates UI for the build process and starts it up
     *
     */
    public void buildInit()
    {
        removeAll();

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,2dlu,p", createDuplicateJGoodiesDef("c:p", "5dlu", 6)));
        CellConstraints cc         = new CellConstraints();

        progressBar.setIndeterminate(false);

        indvLabel         = new JLabel("", JLabel.RIGHT);
        globalLabel       = new JLabel(getResourceString("Indexing_Descr"), JLabel.RIGHT);
        globalProgressBar = new JProgressBar();

        cancelBtn = new JButton(getResourceString("CancelIndexing"));
        closeBtn  = new JButton(getResourceString("Close"));
        closeBtn.setVisible(false);

        JLabel label = new JLabel(getResourceString("UpdatingES"), JLabel.CENTER);
        label.setFont(captionFont);

        int row = 1;
        builder.add(label, cc.xywh(1,row,3,1));
        row +=2;
        builder.add(new JSeparator(JSeparator.HORIZONTAL), cc.xywh(1,row,3,1));
        row += 2;
        builder.add(indvLabel, cc.xy(1,row));
        builder.add(progressBar, cc.xy(3,row));
        row += 2;
        builder.add(globalLabel, cc.xy(1,row));
        builder.add(globalProgressBar, cc.xy(3,row));
        row += 2;
        builder.add(cancelBtn, cc.xywh(1,row,3,1));
        row += 2;
        builder.add(closeBtn, cc.xywh(1,row,3,1));
        row += 2;

        builder.getPanel().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), BorderFactory.createEmptyBorder(5,5,5,5)));

        PanelBuilder builder2 = new PanelBuilder(new FormLayout("center:p:g", "center:p:g"));
        builder2.add(builder.getPanel(), cc.xy(1,1));

        builder2.getPanel().setBackground(Color.WHITE);
        add(builder2.getPanel(), BorderLayout.CENTER);

        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                cancelBtn.setEnabled(false);
                isCancelled = true;
                //stop();
            }
        });

        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                SubPaneMgr.getInstance().closeCurrent();
            }
        });

        start();
    }

    /**
     * @param writer
     * @param form
     */
    protected void indexViewForm(final IndexWriter writer, final FormViewDef form) throws IOException
    {
        /*
        // separator, field, label, subview
        for (FormRow row :  form.getRows())
        {
            for (FormCell cell : row.getCells())
            {
                if (cell instanceof FormCellLabel)
                {
                    termsIndexed++;
                    Document doc = new Document();
                    doc.add(Field.Keyword("id", Integer.toString(form.getId())));
                    doc.add(Field.Keyword("sid", "10000")); // XXX this is not right, look it up!

                    String formName = form.getName();
                    String label    = ((FormCellLabel)cell).getLabel();

                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append(formName);
                    strBuf.append('\t');
                    strBuf.append(cell.getType().toString());
                    strBuf.append('\t');
                    strBuf.append(label);
                    strBuf.append('\t');
                    strBuf.append(form.getDesc());
                    strBuf.append('\t');

                    doc.add(Field.UnStored("contents", form.getName()));
                    doc.add(Field.UnStored("contents", label));

                    doc.add(Field.UnIndexed("data", strBuf.toString()));

                    writer.addDocument(doc);
                }

            }
        }
        */
   }

    /**
     * @param writer
     * @param form
     */
    protected void indexViewTable(final IndexWriter writer, final FormViewDef form) throws IOException
    {
        /*
        for (FormColumn formCol : form.getColumns())
        {
            termsIndexed++;

            Document doc = new Document();
            doc.add(Field.Keyword("id", Integer.toString(form.getId())));
            doc.add(Field.Keyword("sid", "10000")); // XXX this is not right, look it up!

            String formName = form.getName();
            String label    = formCol.getLabel();

            StringBuffer strBuf = new StringBuffer();
            strBuf.append(formName);
            strBuf.append('\t');
            strBuf.append("col");
            strBuf.append('\t');
            strBuf.append(label);
            strBuf.append('\t');
            strBuf.append(form.getDesc());
            strBuf.append('\t');

            doc.add(Field.UnStored("contents", form.getName()));
            doc.add(Field.UnStored("contents", label));

            doc.add(Field.UnIndexed("data", strBuf.toString()));

            writer.addDocument(doc);

        }
        */
    }

    /**
     * Indexes all the fields and forms in the forms
     *
     */
    protected long indexForms(final IndexWriter writer) throws IOException
    {
        /*
        // Count up how many View we are going to process
        int cnt = 0;
        for (ViewSet viewSet : ViewSetMgrTests.getViewSets())
        {
            cnt += viewSet.getViews().size();
        }

        indvLabel.setVisible(true);
        progressBar.setMaximum(cnt);
        progressBar.setValue(0);
        progressBar.setIndeterminate(false);
        progressBar.setString("0%");
        progressBar.setStringPainted(true);

        long begin = new Date().getTime();

        cnt = 0;
        for (ViewSet viewSet : ViewSetMgrTests.getViewSets())
        {
            for (FormView formView : viewSet.getViews())
            {
                indvLabel.setText(formView.getViewSetName());

                if (formView.getType() == FormView.ViewType.form)
                {
                    indexViewForm(writer, (FormFormView)formView);

                } else if (formView.getType() == FormView.ViewType.table)
                {
                    indexViewTable(writer, (FormTableView)formView);

                } else if (formView.getType() == FormView.ViewType.field)
                {

                }
                progressBar.setValue(++cnt);
            }
        }
        long end = new Date().getTime();
        long delta = end - begin;
        log.debug("Time to index (" + delta + " ms)");

        progressBar.setString("100%");
        indvLabel.setText("");

        return delta;
        */
        return 0;
    }

    /**
     * Indexes all the fields and forms in the forms
     *
     */
    protected long indexLabels(final IndexWriter writer)
    {
        /*
        // TODO FIX ME! Indexing labels
        
        File resourceDir = null;//AppContextMgr.getInstance().getCurrentContext();
        File[] files = resourceDir.listFiles(new DiskFileFilter("jrxml"));

        indvLabel.setVisible(true);
        progressBar.setMaximum(files.length);
        progressBar.setValue(0);
        progressBar.setIndeterminate(false);
        progressBar.setString("0%");
        progressBar.setStringPainted(true);

        long begin = new Date().getTime();

        int cnt = 0;

        for (File file : files)
        {
            String fileName = file.getName();
            try
            {
                Element root = XMLHelper.readFileToDOM4J(file.getAbsoluteFile());
                String labelName = root.attributeValue("name");

                indvLabel.setText(labelName);

                //List textFields = root.selectNodes("/jasperReport/detail/band/textField");
                //for ( Iterator iter = textFields.iterator(); iter.hasNext(); )
                //{
                //    Element textField = (Element)iter.next();
                //}

                StringBuilder strBuf = new StringBuilder(128);
                List staticTexts = root.selectNodes("/jasperReport/detail/band/staticText/text");
                for ( Iterator iter = staticTexts.iterator(); iter.hasNext(); )
                {
                    Element text = (Element)iter.next();
                    String label = text.getTextTrim();

                    termsIndexed++;

                    Document doc = new Document();
                    doc.add(Field.Keyword("id", labelName));
                    doc.add(Field.Keyword("sid", "20000")); // XXX this is not right, look it up!

                    strBuf.append(fileName);
                    strBuf.append('\t');
                    strBuf.append(labelName);
                    strBuf.append('\t');
                    strBuf.append("Static");
                    strBuf.append('\t');
                    strBuf.append(label);
                    strBuf.append('\t');

                    doc.add(Field.UnStored("contents", fileName));
                    doc.add(Field.UnStored("contents", labelName));
                    doc.add(Field.UnStored("contents", label));
                    doc.add(Field.UnIndexed("data", strBuf.toString()));
                    strBuf.setLength(0);

                    writer.addDocument(doc);
               }

            } catch (Exception ex)
            {
                log.error(ex);
            }
            progressBar.setValue(++cnt);
        }
        long end = new Date().getTime();
        long delta = end - begin;
        log.debug("Time to index (" + delta + " ms)");

        progressBar.setString("100%");
        indvLabel.setText("");

        return delta;
        */
        return 0;
    }

    /**
     * Starts the index process, it reads all the desired SQL from an XML file "search_config.xml"
     * in the config directory. If the process is cancelled then the indexing files are removed.
     *
     */
    protected void index() throws IOException
    {
        IndexWriter writer    = null;
        long        deltaTime = 0;
        try
        {
            writer = createIndexWriter(lucenePath, true);
            
            if (esDOM == null)
            {
                esDOM = XMLHelper.readDOMFromConfigDir("search_config.xml");         // Describes the definitions of the full text search
            }

            List tables = esDOM.selectNodes("/searches/express/table");
            
            boolean doAll = forceChkbx.isSelected();

            int numOfCategories = (doIndexForms ? 1 : 0) + (doIndexLabels ? 1 : 0);
            
            // Count up how many will be updated by checking each out of date table name 
            // against the names in the outOfDateHash 
            if (doAll)
            {
                numOfCategories += tables.size();
                
            } else
            {
                for (Object obj : tables)
                {
                    Element                  tableElement = (Element)obj;
                    ExpressResultsTableInfo  tableInfo    = new ExpressResultsTableInfo(tableElement, ExpressResultsTableInfo.LOAD_TYPE.Building, true);
                    
                    // Each Table was checked and outOfDateHash contains the names of the tables that need updating
                    // usually there is just one table
                    for (String tableName : tableInfo.getOutOfDate().keySet())
                    {
                        if (outOfDateHash.get(tableName) != null && tableInfo.isIndexed() && isNotEmpty(tableInfo.getBuildSql()))
                        {
                            numOfCategories++;
                            break;
                        }
                    }
                }
            }

            globalProgressBar.setStringPainted(true);
            globalProgressBar.setMaximum(numOfCategories);
            globalProgressBar.setValue(0);
            globalProgressBar.setString("0%");
            int indexerCnt = 0;
            for (Object obj : tables)
            {
                Element                  tableElement = (Element)obj;
                ExpressResultsTableInfo  tableInfo    = new ExpressResultsTableInfo(tableElement, ExpressResultsTableInfo.LOAD_TYPE.Building, true);
                
                // Each Table was checked and outOfDateHash contains the names of the tables that need updating
                // usually there is just one table
                boolean needsUpdating = false;
                if (!doAll)
                {
                    for (String tableName : tableInfo.getOutOfDate().keySet())
                    {
                        if (outOfDateHash.get(tableName) != null)
                        {
                            needsUpdating = true;
                            break;
                        }
                    }
                }
                
                if ((doAll || needsUpdating) && tableInfo.isIndexed() && isNotEmpty(tableInfo.getBuildSql()))
                {
                    log.debug("Indexing: "+tableInfo.getTitle()+"  Id: "+tableInfo.getTableId());
                    indvLabel.setText(tableInfo.getTitle());
                    int id = Integer.parseInt(tableInfo.getTableId());
                    if (id < 10000)
                    {
                       deltaTime += indexQuery(writer, tableInfo);
                       log.debug("Delta Time from indexQuery: "+deltaTime);
                    }
                    if (isCancelled)
                    {
                       break;
                    }
                    globalProgressBar.setValue(++indexerCnt);

                    globalProgressBar.setString((int)((double)indexerCnt / (double)tables.size() * 100.0)+"%");
                    indvLabel.setText("");
                }
            }

            // These could be moved up into the loop above
            if (doIndexForms)
            {
                deltaTime += indexForms(writer);
                log.debug(deltaTime);
            }

            if (doIndexLabels)
            {
                deltaTime += indexLabels(writer);
                log.debug(deltaTime);
            }

        } catch (Exception ex)
        {
            log.error(ex);
        }

        // OK, we are done But did we complete or cancel?

        indvLabel.setHorizontalAlignment(JLabel.CENTER);
        globalLabel.setHorizontalAlignment(JLabel.CENTER);
        progressBar.setVisible(false);
        globalProgressBar.setVisible(false);
        cancelBtn.setVisible(false);

        if (isCancelled)
        {
            indvLabel.setVisible(false);
            globalLabel.setText(getResourceString("indexingWasCancelled"));

            if (writer != null)
            {
                writer.close();
            }

            // Create a new one and then close it
            String[] fileNames = lucenePath.list();
            for (int i=0;i<fileNames.length;i++)
            {
                //System.out.println(fileNames[i]);
                File file = new File(lucenePath.getAbsoluteFile()+File.separator+fileNames[i]);
                file.delete();
            }
            log.debug(lucenePath.delete() ? "deleted" : "not deleted");
            
            CommandDispatcher.dispatch(new CommandAction("Express_Search", "CheckIndexerPath", null));
            
        } else
        {
            long minutes = deltaTime / 60000L;
            long seconds = (deltaTime / 1000L) % 60L;
            indvLabel.setText(String.format(getResourceString("TermsIndexedInTime"), new Object[] {termsIndexed, minutes, seconds}));
            globalLabel.setText(getResourceString("OptimizingIndex"));
            log.debug(deltaTime);
            log.debug("Time to index all (" + (deltaTime / 1000.0) + " seconds)");
            
            optWriter = writer;
            
            final SwingWorker worker = new SwingWorker()
            {
                public Object construct()
                {
                    try
                    {
                        optWriter.optimize();
                        optWriter.close();
                        
                    } catch (Exception ex)
                    {
                        log.error(ex);
                    }
                    return null;
                }

                //Runs on the event-dispatching thread.
                public void finished()
                {
                    globalLabel.setText(getResourceString("doneIndexing"));
                    optWriter = null;
                    
                    closeBtn.setVisible(true);

                    if (esipListener != null)
                    {
                        esipListener.doneIndexing();
                    }
                    CommandDispatcher.dispatch(new CommandAction("Express_Search", "CheckIndexerPath", null));
                }
            };
            worker.start();

        }

    }

    /**
     * Starts the thread to make the SQL call
     *
     */
    public void start()
    {
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Stops the thread making the call
     *
     */
    public synchronized void stop()
    {
        if (thread != null)
        {
            thread.interrupt();
        }
        thread = null;
        notifyAll();
    }

    /**
     * Does indexing
     */
    public void run()
    {
        try
        {
            index();

        } catch (Exception ex)
        {
            log.error(ex);
        }

    }
    
    //--------------------------------------
    // QueryResultsListener
    //--------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QueryResultsListener#allResultsBack()
     */
    public synchronized void allResultsBack()
    {
        boolean allOK = true;
        
        //DateFormat                formatter    = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Timestamp lastModified = noIndexFile ? new Timestamp(0) : new Timestamp(lucenePath.lastModified());

        java.util.List<Object> list = handler.getDataObjects();
        
        for (int i=0;i<list.size();i++)
        {
            Object nameObj         = list.get(i++);
            Object createdDateObj  = list.get(i++);
            
            list.get(i++); // skip text label (should be same as nameObj
            Object modifiedDateObj = list.get(i);
            
            JLabel label = resultsLabels.get(getString(nameObj));
            if (label != null)
            {
                Timestamp createdTS  = (Timestamp)createdDateObj;
                Timestamp modifiedTS = (Timestamp)modifiedDateObj;
     
                if (createdDateObj != null && modifiedTS != null)
                {
                    createdTS = createdTS.getTime() > modifiedTS.getTime() ? createdTS : modifiedTS;
                            
                } else if (createdTS == null && modifiedTS != null)
                {
                    createdTS = modifiedTS;  // this should happen
                }
                
                if (createdTS != null)
                {
                    //log.debug("["+formatter.format(lastModified)+"]["+formatter.format(createdTS)+"] "+(lastModified.getTime() < createdTS.getTime()));
                    boolean outOfDate = lastModified.getTime() < createdTS.getTime();
                    label.setIcon(outOfDate ? exclaimIcon : checkIcon);
                    if (outOfDate)
                    {
                        allOK = false;
                        outOfDateHash.put(nameObj.toString(), outOfDate);
                        //System.out.println("["+nameObj.toString()+"]");
                    }
                    
                } else
                {
                    label.setIcon(checkIcon); // This means there are no records to indicate it is up to date
                }
                

            } else
            {
                log.error("Couldn't find label["+getString(nameObj)+"]");
            }
        }
        list.clear();

        explainLabel.setText(allOK ? getResourceString("ESNoRebuild") : getResourceString("ESIndexerExplain"));
        explainLabel.setForeground(allOK ? Color.GREEN.darker() : Color.RED.darker());
        explainLabel.invalidate();
        doLayout();
        repaint();

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QueryResultsListener#resultsInError(edu.ku.brc.specify.dbsupport.QueryResultsContainer)
     */
    public void resultsInError(final QueryResultsContainer qrc)
    {
        //JOptionPane.showMessageDialog(this, getResourceString("ERROR_CREATNG_BARCHART"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE); // XXX LOCALIZE

        //addCompletedComp(new JLabel(getResourceString("ERROR_CREATNG_BARCHART"), JLabel.CENTER));
    }
    
    //------------------------------------------------
    //-- ExpressSearchIndexerListener
    //------------------------------------------------

    public interface ExpressSearchIndexerListener
    {
        /**
         * Indicates the ExpressSearchIndex has completed.
         */
        public void doneIndexing();
        
    }
}
