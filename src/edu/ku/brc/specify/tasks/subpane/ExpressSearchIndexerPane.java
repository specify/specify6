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
package edu.ku.brc.specify.tasks.subpane;


import static edu.ku.brc.helpers.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.helpers.UIHelper.getInt;
import static edu.ku.brc.helpers.UIHelper.getString;
import static edu.ku.brc.ui.UICacheManager.getResourceString;
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
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.PairsMultipleQueryResultsHandler;
import edu.ku.brc.dbsupport.QueryResultsContainer;
import edu.ku.brc.dbsupport.QueryResultsDataObj;
import edu.ku.brc.dbsupport.QueryResultsListener;
import edu.ku.brc.helpers.DiskFileFilter;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.tasks.ExpressResultsTableInfo;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UICacheManager;
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
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ExpressSearchIndexerPane extends BaseSubPane implements Runnable, QueryResultsListener
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(ExpressSearchIndexerPane.class);

    // Data Members
    protected Thread       thread;
    protected File         lucenePath    = null;
    protected Analyzer     analyzer      = new StandardAnalyzer();//WhitespaceAnalyzer();
    protected Element      esDOM         = null;
    protected JLabel       indvLabel;
    protected JLabel       globalLabel;
    protected JProgressBar globalProgressBar;
    protected long         termsIndexed  = 0;
    protected boolean      isCancelled   = false;
    protected JButton      cancelBtn;
    protected JButton      closeBtn;

    protected ImageIcon    checkIcon     = new ImageIcon(IconManager.getImagePath("check.gif"));  // Move to icons.xml
    protected ImageIcon    exclaimIcon   = new ImageIcon(IconManager.getImagePath("exclaim.gif"));
    protected ImageIcon    exclaimYWIcon = new ImageIcon(IconManager.getImagePath("exclaim_yellow.gif"));

    protected PairsMultipleQueryResultsHandler handler = null;

    protected Hashtable<String, JLabel> resultsLabels = new Hashtable<String, JLabel>();
    protected JPanel                    resultsPanel;
    protected Font                      captionFont   = null;
    protected JLabel                    explainLabel;
    protected boolean                   noIndexFile   = false;

    protected boolean                   doIndexForms  = false; // XXX Pref
    protected boolean                   doIndexLabels = false; // XXX Pref

    /**
     * Default Constructor
     *
     */
    public ExpressSearchIndexerPane(final ExpressSearchTask task)
    {
        super(getResourceString("IndexerPane"), task);

        lucenePath = ExpressSearchTask.getIndexDirPath();

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

        Date lastModified = noIndexFile ? new Date(0) : new Date(lucenePath.lastModified());

        try
        {
            if (esDOM == null)
            {
                esDOM = AppContextMgr.getInstance().getResourceAsDOM("SearchConfig"); // Describes the definitions of the full text search
            }

            Hashtable<String, String> namesHash = new Hashtable<String, String>();

            List tables = esDOM.selectNodes("/tables/table/outofdate/table");
            for ( Iterator iter = tables.iterator(); iter.hasNext(); )
            {
                Element tableElement = (Element)iter.next();
                namesHash.put(tableElement.attributeValue("name"), tableElement.attributeValue("title"));
            }

            PanelBuilder    builder = new PanelBuilder(new FormLayout("p:g,2dlu,p:g", createDuplicateJGoodiesDef("p","5px", namesHash.size())));
            CellConstraints cc      = new CellConstraints();

            if (captionFont == null)
            {
                Font curFont = getFont();
                captionFont = new Font(curFont.getFontName(), Font.BOLD, 14);
            }

            int row = 1;
            DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            for (Enumeration<String> e=namesHash.keys();e.hasMoreElements();)
            {
                String nameStr = e.nextElement();
                String sqlStr = "select count(*) from " + nameStr +" where datediff(TimeStampCreated, "+formatter.format(lastModified)+") > 0";
                log.debug(sqlStr);
                QueryResultsContainer container = new QueryResultsContainer(sqlStr);
                container.add(new QueryResultsDataObj(nameStr));

                // Since the index doesn't exist fake like
                // each table has at least one out of date record
                container.add(noIndexFile ? new QueryResultsDataObj(new Integer(1)) : new QueryResultsDataObj(1,1));
                list.add(container);

                JLabel label = new JLabel(namesHash.get(nameStr)+":", JLabel.RIGHT);
                label.setFont(captionFont);

                builder.add(label, cc.xy(1,row));
                label = new JLabel(exclaimYWIcon);

                resultsLabels.put(nameStr, label);
                builder.add(label, cc.xy(3,row));
                row += 2;
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
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p:g,2dlu,p:g", createDuplicateJGoodiesDef("c:p", "5dlu", 6)));
        CellConstraints cc         = new CellConstraints();

        removeAll();

        RolloverCommand configureBtn = new RolloverCommand(getResourceString("Configure"), IconManager.getImage("Configure", IconManager.IconSize.Std32));
        RolloverCommand buildBtn     = new RolloverCommand(getResourceString("Build"), new ImageIcon(IconManager.getImagePath("build.gif")));

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

        if (fieldName.equals("DateAccessioned"))
        {
            int x = 0;
            x++;
        }
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
                        doc.add(Field.Keyword(fieldName, value));
                    } else
                    {
                        doc.add(Field.UnStored("contents", value));
                    }
                    //log.debug("["+fieldName+"]["+secondaryKey+"]["+value+"]");
                }

            } else
            {
                String str = rs.getString(index);
                if (isNotEmpty(str))
                {
                    value = str;
                    doc.add(Field.UnStored("contents", str));
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
                        doc.add(Field.Keyword(fieldName, value));
                    } else
                    {
                        doc.add(Field.UnStored("contents", value));
                    }
                    //log.debug("["+fieldName+"]["+secondaryKey+"]["+value+"]");
                }

            } else
            {
                String str = rs.getString(index);
                if (isNotEmpty(str))
                {
                    value = str;
                    doc.add(Field.Keyword(secondaryKey, str));
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

        Connection dbConnection = DBConnection.getConnection();
        Statement  dbStatement  = null;

        int      tableId       = Integer.parseInt(tableInfo.getTableId());
        int[]    fields        = tableInfo.getCols();
        String[] secondaryKeys = tableInfo.getSecondaryKeys();
        boolean  useHitsCache  = tableInfo.isUseHitsCache();

        StringBuilder strBuf = new StringBuilder(128);

        long begin = 0;

        progressBar.setIndeterminate(true);

        try
        {
            if (dbConnection != null)
            {
                dbStatement = dbConnection.createStatement();

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

                    String tableIdStr = Integer.toString(tableId);

                    int rowCnt = 1;
                    do
                    {
                        if (step == trigger)
                        {
                            progressBar.setValue(rowCnt);
                            progressBar.setString((int)(((double)rowCnt / numRows) * 100.0)+"%");
                            step = 0;
                        }

                        step++;
                        rowCnt++;
                        Document doc = new Document();
                        doc.add(Field.UnIndexed("id", rs.getString(fields[0])));
                        doc.add(Field.UnIndexed("table", tableIdStr));

                        int cnt = 0;
                        if (useHitsCache)
                        {
                            strBuf.setLength(0);
                            for (int i=0;i<fields.length;i++)
                            {
                                Object valObj = rs.getObject(fields[i]);
                                if (valObj != null)
                                {
                                    if (i > 0)
                                    {
                                        int inx = fields[i];
                                        String value = indexValue(doc, rs, inx,
                                                                  rsmd.getColumnName(inx),
                                                                  secondaryKeys[i],
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
                                } else
                                {
                                    strBuf.append(" \t");
                                }
                            }

                            doc.add(Field.UnIndexed("data", strBuf.toString()));

                        } else
                        {
                            for (int i=1;i<fields.length;i++)
                            {
                                int inx = fields[i];
                                Object valObj = rs.getObject(inx);
                                if (valObj != null)
                                {
                                    if (indexValue(doc, rs, inx, rsmd.getColumnName(inx), secondaryKeys[i],
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
                        }
                        if (cnt > 0)
                        {
                            writer.addDocument(doc);
                        }
                    } while(rs.next());
                    log.debug("done indexing");
                }
                progressBar.setString("");

                dbStatement.close();
                dbConnection.close();
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

        long delta = end - begin;
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
                    doc.add(Field.Keyword("table", "10000"));

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

                /*List textFields = root.selectNodes("/jasperReport/detail/band/textField");
                for ( Iterator iter = textFields.iterator(); iter.hasNext(); )
                {
                    Element textField = (Element)iter.next();
                }*/

                StringBuilder strBuf = new StringBuilder(128);
                List staticTexts = root.selectNodes("/jasperReport/detail/band/staticText/text");
                for ( Iterator iter = staticTexts.iterator(); iter.hasNext(); )
                {
                    Element text = (Element)iter.next();
                    String label = text.getTextTrim();

                    termsIndexed++;

                    Document doc = new Document();
                    doc.add(Field.Keyword("id", labelName));
                    doc.add(Field.Keyword("table", "20000"));

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
    }

    /**
     * Starts the index process, it reads all the desired SQL from an XML file "search_config.xml"
     * in the config directory. If the process is cancelled then the indexing files are removed.
     *
     */
    public void index() throws IOException
    {

        Directory dir = FSDirectory.getDirectory(lucenePath, true);
        IndexWriter writer = new IndexWriter(dir, analyzer, true);
        writer.mergeFactor   = 1000;
        writer.maxMergeDocs  = 9999999;
        writer.minMergeDocs  = 1000;

        long deltaTime = 0;
        try
        {
            if (esDOM == null)
            {
                esDOM = XMLHelper.readDOMFromConfigDir("search_config.xml");         // Describes the definitions of the full text search
            }

            List tables = esDOM.selectNodes("/tables/table");

            int numOfCategories = tables.size() + (doIndexForms ? 1 : 0) + (doIndexLabels ? 1 : 0);

            globalProgressBar.setStringPainted(true);
            globalProgressBar.setMaximum(numOfCategories);
            globalProgressBar.setValue(0);
            globalProgressBar.setString("0%");
            int indexerCnt = 0;
            for ( Iterator iter = tables.iterator(); iter.hasNext(); )
            {
                Element tableElement = (Element)iter.next();
                ExpressResultsTableInfo tableInfo = new ExpressResultsTableInfo(tableElement, ExpressResultsTableInfo.LOAD_TYPE.Building);

                if (isNotEmpty(tableInfo.getBuildSql()))
                {
                    log.debug("Indexing: "+tableInfo.getTitle()+"  Id: "+tableInfo.getTableId());
                    indvLabel.setText(tableInfo.getTitle());
                    int id = Integer.parseInt(tableInfo.getTableId());
                    if (id < 10000)
                    {
                       deltaTime += indexQuery(writer, tableInfo);
                       log.debug(deltaTime);
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

            writer.close();

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

            indvLabel.setText(termsIndexed+ " terms indexed in "+(((double)deltaTime) / 1000.0) + " seconds");
            globalLabel.setText(getResourceString("doneIndexing"));
            log.debug(deltaTime);
            log.debug("Time to index all (" + (((double)deltaTime) / 1000.0) + " seconds)");
            writer.optimize();
            writer.close();
        }
        closeBtn.setVisible(true);

        ((ExpressSearchTask)task).checkForIndexer();

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
     * @see edu.ku.brc.specify.dbsupport.QueryResultsListener#allResultsBack()
     */
    public synchronized void allResultsBack()
    {
        boolean allOK = true;

        java.util.List<Object> list = handler.getDataObjects();
        for (int i=0;i<list.size();i++)
        {
            Object name   = list.get(i++);
            Object valObj = list.get(i);
            JLabel label  = resultsLabels.get(getString(name));
            if (label != null)
            {
                int num = getInt(valObj);
                label.setIcon(num == 0 ? checkIcon : exclaimIcon);
                if (num > 0)
                {
                    allOK = false;
                }
            } else
            {
                log.error("Couldn't find label["+getString(name)+"]");
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
     * @see edu.ku.brc.specify.dbsupport.QueryResultsListener#resultsInError(edu.ku.brc.specify.dbsupport.QueryResultsContainer)
     */
    public void resultsInError(final QueryResultsContainer qrc)
    {
        //JOptionPane.showMessageDialog(this, getResourceString("ERROR_CREATNG_BARCHART"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE); // XXX LOCALIZE

        //addCompletedComp(new JLabel(getResourceString("ERROR_CREATNG_BARCHART"), JLabel.CENTER));
    }

}
