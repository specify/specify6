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
package edu.ku.brc.af.core;


import static org.apache.commons.lang.StringUtils.isNotEmpty;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.Element;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.PairsMultipleQueryResultsHandler;
import edu.ku.brc.dbsupport.QueryResultsContainer;
import edu.ku.brc.dbsupport.QueryResultsDataObj;
import edu.ku.brc.dbsupport.QueryResultsListener;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
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
 
 * @code_status Beta
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ExpressSearchIndexer implements Runnable, QueryResultsListener
{
    // Static Data Members
    private static final Logger   log      = Logger.getLogger(ExpressSearchIndexer.class);
    private static final Analyzer analyzer = new StandardAnalyzer();//WhitespaceAnalyzer();

    // Data Members
    protected Thread                    thread;
    protected File                      lucenePath        = null;
    protected Element                   esDOM             = null;
    protected double                    numRows            = 0;
    protected IndexWriter               optWriter          = null;
    protected DateFormat                formatter    = new SimpleDateFormat("yyyyMMdd");
    
    protected long                      termsIndexed      = 0;
    protected boolean                   isCancelled       = false;

    protected PairsMultipleQueryResultsHandler handler    = null;

    protected boolean                   noIndexFile       = false;

    protected boolean                   doIndexForms      = false; // XXX Pref
    protected boolean                   doIndexLabels     = false; // XXX Pref
    
    protected ExpressSearchIndexerListener listener       = null;

    /**
     * Constructor.
     * @param lucenePath the path to the lucene index
     * @param listener the listener for when it is done (can be null)
     */
    public ExpressSearchIndexer(final File lucenePath, final ExpressSearchIndexerListener listener)
    {
        this.listener = listener;
        
        this.lucenePath = lucenePath;

        startCheckOutOfDateProcess(); // must be done before openingScreenInit

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

            List tables = esDOM.selectNodes("/searches/express/table/outofdate/table");
            Hashtable<String, String> namesHash = new Hashtable<String, String>();
            List<String>              sectionNames = new ArrayList<String>(tables.size()+2);
            for ( Iterator iter = tables.iterator(); iter.hasNext(); )
            {
                Element tableElement = (Element)iter.next();
                String  sectionName  = tableElement.attributeValue("title"); 
                namesHash.put(tableElement.attributeValue("name"), sectionName);
                sectionNames.add(sectionName);
            }
            
            if (doIndexForms)
            {
                sectionNames.add("Forms"); // I18N
            }
            if (doIndexLabels)
            {
                sectionNames.add("Labels"); // I18N
            }  
            
            if (listener != null)
            {
               listener.namedSections(sectionNames); 
            }

            for (Enumeration<String> e=namesHash.keys();e.hasMoreElements();)
            {
                String nameStr = e.nextElement();
                String sqlStr = "select TimestampCreated from accession order by TimestampCreated desc limit 0,1"; // TODO This needs to be per DB PLATFORM
                log.debug(sqlStr);
                QueryResultsContainer container = new QueryResultsContainer(sqlStr);
                container.add(new QueryResultsDataObj(nameStr));

                // Since the index doesn't exist fake like
                // each table has at least one out of date record
                container.add(noIndexFile ? new QueryResultsDataObj(new Date(new Date().getTime()-1000)) : new QueryResultsDataObj(1,1));
                list.add(container);
                
                // Now find the last Modified Timestamp
                sqlStr = "select TimestampModified from "+nameStr+" order by TimestampModified desc limit 0,1"; // TODO This needs to be per DB PLATFORM
                log.info(sqlStr);
                container = new QueryResultsContainer(sqlStr);
                container.add(new QueryResultsDataObj(nameStr));

                // Since the index doesn't exist fake like
                // each table has at least one out of date record
                container.add(noIndexFile ? new QueryResultsDataObj(new Date(new Date().getTime()-1000)) : new QueryResultsDataObj(1,1));
                list.add(container);
            }

        } catch (Exception ex)
        {
            log.error(ex);
        }

        handler = new PairsMultipleQueryResultsHandler();
        handler.init(this, list);

        // We won't start it up because we know it doesn't exist
        // so there is no reason check everything
        if (!noIndexFile)
        {
            handler.startUp();
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
    public String indexValue(final Document   doc,
                              final ResultSet  rs,
                              final int        index,
                              final String     fieldName,
                              final String     secondaryKey,
                              final Class      objClass) throws SQLException
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
                    doc.add(new Field("contents", str, Field.Store.NO, Field.Index.TOKENIZED));
                    //doc.add(Field.UnStored("contents", str));
                }
            }
        }
        //log.debug("["+fieldName+"]["+secondaryKey+"]["+value+"]");
        return value;
    }

    /**
     * Performs a query and then indexes all the results for each orw and column
     * @param sectionIndex the section that is currently being processed
     * @param writer the lucene writer
     * @param tableInfo info describing the table (hold the table ID)
     * @param trigger the value that trips the notification of the listener
     * @param resultset the resultset
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public boolean indexQuery(final int                     sectionIndex,
                              final IndexWriter             writer, 
                              final ExpressResultsTableInfo tableInfo, 
                              final int                     trigger,
                              final ResultSet               resultset) throws SQLException, IOException
    {
        boolean    useHitsCache = tableInfo.isUseHitsCache();
        
        ExpressResultsTableInfo.ColInfo     colInfo[]     = tableInfo.getCols();
        ExpressResultsTableInfo.JoinColInfo joinColInfo[] = tableInfo.getJoins();

        StringBuilder strBuf = new StringBuilder(128);

        if (resultset.first())
        {
            // First we create an array of Class so we know what each column's Object class is
            ResultSetMetaData rsmd    = resultset.getMetaData();
            Class[]           classes = new Class[rsmd.getColumnCount()+1];
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

            int step = 0;
            double rowCnt = 1.0;
            do
            {
                if (step == trigger)
                {
                    if (listener != null)
                    {
                        listener.processingSection(sectionIndex, rowCnt / numRows);
                    }
                    step = 0;
                }

                step++;
                rowCnt++;
                Document doc = new Document();
                doc.add(new Field("id", resultset.getString(tableInfo.getIdColIndex()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("sid", idStr, Field.Store.YES, Field.Index.NO));
                doc.add(new Field("class", tableInfo.getName(), Field.Store.YES, Field.Index.NO));
                
                if (true)
                {
                    System.out.println("id: "+doc.get("id"));
                    System.out.println("sid: "+doc.get("sid"));
                    System.out.println("class: "+doc.get("class"));
                }

                int cnt = 0;
                if (useHitsCache)
                {
                    strBuf.setLength(0);
                    
                    for (ExpressResultsTableInfo.JoinColInfo jci : joinColInfo)
                    {
                        doc.add(new Field(jci.getJoinTableId(), resultset.getString(jci.getPosition()), Field.Store.YES, Field.Index.NO));
                    }

                    
                    for (int i=0;i<colInfo.length;i++)
                    {
                        ExpressResultsTableInfo.ColInfo ci = colInfo[i];
                        if (i > 0)
                        {
                            int inx = ci.getPosition();
                            String value = indexValue(doc, resultset, inx,
                                                      rsmd.getColumnName(inx),
                                                      ci.getSecondaryKey(),
                                                      classes[inx]);
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
                                return false;
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
                        doc.add(new Field(jci.getJoinTableId(), resultset.getString(jci.getPosition()), Field.Store.YES, Field.Index.NO));
                    }
                    
                    for (int i=0;i<colInfo.length;i++)
                    {
                        ExpressResultsTableInfo.ColInfo ci = colInfo[i];
                        int inx = ci.getPosition();

                        if (indexValue(doc, resultset, inx, rsmd.getColumnName(inx), ci.getSecondaryKey(), classes[inx]) != null)
                        {
                            cnt++;
                            termsIndexed++;
                        }

                        if (isCancelled)
                        {
                            return false;
                        }

                    }
                }
                
                if (cnt > 0)
                {
                    writer.addDocument(doc);
                }
                
            } while(resultset.next());
            log.debug("done indexing");
        }
        return true;
    }

    /**
     * Performs a query and then indexes all the results for each orw and column
     * @param sectionIndex the section that is currently being processed
     * @param writer the lucene writer
     * @param tableInfo info describing the table (hold the table ID)
     * @param sqlStr the SQL string for building the index
     */
    public long indexQuery(final int                     sectionIndex, 
                           final IndexWriter             writer, 
                           final ExpressResultsTableInfo tableInfo, 
                           final String                  sqlStr)
    {
        Connection dbConnection = DBConnection.getInstance().createConnection();
        Statement  dbStatement  = null;

        long begin = 0;

        if (listener != null)
        {
            listener.prepareSection(sectionIndex);
        }

        try
        {
            if (dbConnection != null)
            {
                dbStatement = dbConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

                log.debug("SQL ["+sqlStr+"]");

                ResultSet rs = dbStatement.executeQuery(sqlStr);

                begin = new Date().getTime();
                
                numRows = 0;
                int    trigger = -1;
                if (rs.last())
                {
                    numRows = rs.getRow();
                    if (listener != null)
                    {
                        listener.startedSection(sectionIndex);
                    }
                    trigger = (int)(numRows * 0.02);
                    
                } else
                {
                    log.debug("Table["+tableInfo.getTitle()+"] is empty.");
                    //throw new RuntimeException("Can't go to last record.");
                }
                log.debug("Row ["+numRows+"] to index in ["+tableInfo.getTitle()+"].");

                indexQuery(sectionIndex, writer, tableInfo,trigger, rs);

                rs.close();

                dbStatement.close();
                dbConnection.close();
                
            } else
            {
                throw new RuntimeException("Exception in indexQuery - Why are new Here?");
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
        
        if (listener != null)
        {
            listener.endedSection(sectionIndex);
        }

        long end = new Date().getTime();

        long delta = begin > 0 ? end - begin : 0;
        log.debug("Time to index (" + delta + " ms)");
        return delta;
    }

    /**
     * Performs a query for the entire table (or relationship) and then indexes all the results for each row and column.
     * @param sectionIndex the section that is currently being processed
     * @param writer the lucene writer
     * @param tableInfo info describing the table (hold the table ID)
     */
    public long indexQuery(final int sectionIndex, final IndexWriter writer, final ExpressResultsTableInfo tableInfo)
    {
        return indexQuery(sectionIndex, writer, tableInfo, tableInfo.getBuildSql());
    }

    /**
     * Creates UI for the build process and starts it up
     *
     */
    public void buildInit()
    {
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
            doc.add(Field.Keyword("table", "10000"));

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
        // TODO FIX ME! Indexing labels
        
        /*
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
    public void index() throws IOException
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
            List<ExpressResultsTableInfo> tableInfoList = new ArrayList<ExpressResultsTableInfo>(tables.size());
            for (Object obj : tables)
            {
                Element tableElement = (Element)obj;
                ExpressResultsTableInfo tableInfo = new ExpressResultsTableInfo(tableElement, ExpressResultsTableInfo.LOAD_TYPE.Building, true);
                if (isNotEmpty(tableInfo.getBuildSql()))
                {
                    tableInfoList.add(tableInfo);
                }
            }
            
            int numOfCategories = tableInfoList.size();
            
            if (listener != null)
            {
                listener.startedIndexing();
            }
            
            int indexerCnt = 0;
            for (ExpressResultsTableInfo tableInfo : tableInfoList)
            {
                log.debug("Indexing: "+tableInfo.getTitle()+"  Id: "+tableInfo.getTableId());
                int id = Integer.parseInt(tableInfo.getTableId());
                if (id < 10000)
                {
                   deltaTime += indexQuery(indexerCnt, writer, tableInfo);
                   log.debug("Delta Time from indexQuery: "+deltaTime);
                }
                if (isCancelled)
                {
                   break;
                }
                indexerCnt++;
                if (listener != null)
                {
                    listener.processing(((double)indexerCnt / (double)numOfCategories * 100.0));
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

        if (isCancelled)
        {
            if (writer != null)
            {
                writer.close();
            }
            
            //listener.endedIndexing(allOK, minutes, seconds)

            // Create a new one and then close it
            String[] fileNames = lucenePath.list();
            for (int i=0;i<fileNames.length;i++)
            {
                //System.out.println(fileNames[i]);
                File file = new File(lucenePath.getAbsoluteFile()+File.separator+fileNames[i]);
                file.delete();
            }
            log.debug(lucenePath.delete() ? "deleted" : "not deleted");

        } else
        {
            int minutes = (int)(deltaTime / 60000L);
            int seconds = (int)((deltaTime / 1000L) % 60L);
            
            if (listener != null)
            {
                listener.endedIndexing(minutes, seconds);
            }
            
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
                    optWriter = null;
                    if (listener != null)
                    {
                        listener.complete();
                    }
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
        
        Timestamp lastModified = noIndexFile ? new Timestamp(0) : new Timestamp(lucenePath.lastModified());

        if (listener != null)
        {
            java.util.List<Object> list = handler.getDataObjects();
            for (int i=0;i<list.size();i++)
            {
                Object nameObj         = list.get(i++);
                Object createdDateObj  = list.get(i++);
                
                list.get(i++); // skip text label (should be same as nameObj
                Object modifiedDateObj = list.get(i);
                
                Timestamp createdTS  = (Timestamp)createdDateObj;
                Timestamp modifiedTS = (Timestamp)modifiedDateObj;
     
                boolean isOld = false;
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
                    isOld = lastModified.getTime() < createdTS.getTime();
                    if (!isOld)
                    {
                        allOK = false;
                    }                    
                }

                listener.outOfDate(nameObj.toString(), !isOld);
            }
            list.clear();
            listener.outOfDateComplete(allOK);
        }

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QueryResultsListener#resultsInError(edu.ku.brc.specify.dbsupport.QueryResultsContainer)
     */
    public void resultsInError(final QueryResultsContainer qrc)
    {
        //JOptionPane.showMessageDialog(this, getResourceString("ERROR_CREATNG_BARCHART"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE); // XXX LOCALIZE

        //addCompletedComp(new JLabel(getResourceString("ERROR_CREATNG_BARCHART"), JLabel.CENTER));
    }
    
    public interface ExpressSearchIndexerListener
    {
        public void namedSections(List<String> sectionNames);
        public void outOfDate(String name, boolean isOutOfDate);
        public void outOfDateComplete(boolean allOK);
        
        public void startedIndexing();
        public void processing(double percentage);
        public void endedIndexing(int minutes, int seconds);
        public void complete();
        
        public void prepareSection(int section);
        public void startedSection(int section);
        public void processingSection(int section, double percentage);
        public void endedSection(int section);
    }

}
