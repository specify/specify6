/* Filename:    $RCSfile: ExpressSearchIndexerPane.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.core.subpane;


import static edu.ku.brc.specify.helpers.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.specify.helpers.UIHelper.getInt;
import static edu.ku.brc.specify.helpers.UIHelper.getString;
import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
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
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.border.EtchedBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import edu.ku.brc.specify.core.ExpressResultsTableInfo;
import edu.ku.brc.specify.core.ExpressSearchTask;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.dbsupport.PairsMultipleQueryResultsHandler;
import edu.ku.brc.specify.dbsupport.QueryResultsContainer;
import edu.ku.brc.specify.dbsupport.QueryResultsDataObj;
import edu.ku.brc.specify.dbsupport.QueryResultsListener;
import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.RolloverCommand;
import edu.ku.brc.specify.ui.UICacheManager;

/**
 * A pane enables the user to see (and control) the indexing process for express search.<BR>
 * NOTE: This creates the index cache locality, it doesn't support is being on the network.
 * 
 * @author rods
 *
 */
public class ExpressSearchIndexerPane extends BaseSubPane implements Runnable, QueryResultsListener
{
    // Static Data Members
    private static Log log = LogFactory.getLog(ExpressSearchIndexerPane.class);

    // Data Members
    protected Thread       thread;
    protected File         lucenePath = null;
    protected Element      esDOM;
    protected JLabel       indvLabel;
    protected JLabel       globalLabel;
    protected JProgressBar globalProgressBar;
    protected long         termsIndexed = 0;
    protected boolean      isCancelled  = false;
    protected JButton      cancelBtn;
    protected JButton      closeBtn;
    
    protected ImageIcon    checkIcon     = new ImageIcon(IconManager.getImagePath("check.gif"));
    protected ImageIcon    exclaimIcon   = new ImageIcon(IconManager.getImagePath("exclaim.gif"));
    protected ImageIcon    exclaimYWIcon = new ImageIcon(IconManager.getImagePath("exclaim_yellow.gif"));
    
    protected PairsMultipleQueryResultsHandler handler = null;
    
    protected Hashtable<String, JLabel> resultsLabels = new Hashtable<String, JLabel>();
    protected JPanel                    resultsPanel;
    protected Font                      captionFont = null;
    protected JLabel                    explainLabel;
    protected boolean                   noIndexFile = false;
    
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
            Element esDOM = XMLHelper.readDOMFromConfigDir("express_search.xml");         // Describes the definitions of the full text search
            
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
                log.info(sqlStr);
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
        
        RolloverCommand configureBtn = new RolloverCommand(getResourceString("Configure"), IconManager.getInstance().getIcon("Configure", IconManager.IconSize.Std32));
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
     * Performs a query and then indexes all the results for each orw and column
     * @param writer the lucene writer
     * @param sqlStr the SQL to be executed
     * @param tableId the table ID 
     * @param fields the column positions in the results of those items to be indexed
     * @return the time it took in milliseconds
     */
    public long indexQuery(final IndexWriter writer,ExpressResultsTableInfo tableInfo)
    {
        Connection dbConnection = DBConnection.getInstance().getConnection();
        Statement  dbStatement = null;
        
        int     tableId      = Integer.parseInt(tableInfo.getTableId());
        int[]   fields       = tableInfo.getCols();
        boolean useHitsCache = tableInfo.isUseHitsCache();
        
        StringBuffer strBuf = new StringBuffer();
        
        long begin = 0;
        
        progressBar.setIndeterminate(true);
        
        try
        {
            if (dbConnection != null)
            {
                dbStatement = dbConnection.createStatement();
                
                log.info("SQL ["+tableInfo.getBuildSql()+"]");
                
                ResultSet rs = dbStatement.executeQuery(tableInfo.getBuildSql());
                
                begin = new Date().getTime();
                
                if (rs.last())
                {
                    progressBar.setMaximum(rs.getRow());
                    progressBar.setValue(0);
                    progressBar.setIndeterminate(false);
                }
                
                if (rs.first())
                {
                    int rowCnt = 1;
                    do
                    {
                        if (rowCnt % 50 == 0)
                        {
                            progressBar.setValue(rowCnt);
                        }
                        rowCnt++;
                        Document doc = new Document();
                        doc.add(Field.Keyword("id", rs.getObject(fields[0]).toString()));
                        doc.add(Field.UnIndexed("table", Integer.toString(tableId)));
                        
                        int cnt = 0;
                        if (useHitsCache)
                        {
                            strBuf.setLength(0);
                            for (int i=0;i<fields.length;i++)
                            {
                                Object valObj = rs.getObject(fields[i]);
                                if (valObj != null)
                                {
                                    String valStr =  valObj.toString();
                                    if (valStr.length() > 0)
                                    {          
                                        termsIndexed++;
                                        cnt++;
                                        if (i > 0)
                                        {
                                            doc.add(Field.UnStored("contents", valStr));
                                        }
                                        strBuf.append(valStr);
                                        strBuf.append('\t');
                                         
                                        if (isCancelled)
                                        {
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
                                Object valObj = rs.getObject(fields[i]);
                                if (valObj != null)
                                {
                                    String valStr =  valObj.toString();
                                    if (valStr.length() > 0)
                                    {
                                        termsIndexed++;
                                        cnt++;
                                        doc.add(Field.UnStored("contents", valStr));
                                        
                                        if (isCancelled)
                                        {
                                            dbStatement.close();
                                            dbConnection.close();
                                            return 0;
                                        }
                                   }
                                }
                            }
                        }
                        if (cnt > 0)
                        {
                            writer.addDocument(doc);                            
                        }
                    } while(rs.next());
                    log.info("done indexing");
                }

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
        log.info("Time to index (" + delta + " ms)");
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
                UICacheManager.getInstance().getSubPaneMgr().closeCurrent();
            }
        });
        
        start();
    }
    
    /**
     * Starts the index process, it reads all the desired SQL from an XML file "express_search.xml"
     * in the config directory. If the process is cancelled then the indexing files are removed.
     *
     */
    public void index() throws IOException
    {
 
        
        Directory dir = FSDirectory.getDirectory(lucenePath, true);
        IndexWriter writer = new IndexWriter(dir, new StandardAnalyzer(), true);
        writer.mergeFactor   = 1000;
        writer.maxMergeDocs  = 9999999;
        writer.minMergeDocs  = 1000;
        
        long deltaTime = 0;
        try
        {
            Element esDOM = XMLHelper.readDOMFromConfigDir("express_search.xml");         // Describes the definitions of the full text search
            
            List tables = esDOM.selectNodes("/tables/table");
            globalProgressBar.setMaximum(tables.size());
            globalProgressBar.setValue(0);
            int indexerCnt = 0;
            for ( Iterator iter = tables.iterator(); iter.hasNext(); ) 
            {
                Element tableElement = (Element)iter.next();
                ExpressResultsTableInfo tableInfo = new ExpressResultsTableInfo(tableElement, ExpressResultsTableInfo.LOAD_TYPE.Building);

               log.info("Indexing: "+tableInfo.getTitle()+"  Id: "+tableInfo.getTableId());
               indvLabel.setText(tableInfo.getTitle());
               deltaTime += indexQuery(writer, tableInfo);
               if (isCancelled)
               {
                   break;
               }
               globalProgressBar.setValue(++indexerCnt);
               indvLabel.setText("");
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
            log.info(lucenePath.delete() ? "deleted" : "not deleted");
            
        } else
        {
            
            indvLabel.setText(termsIndexed+ " terms indexed in "+(((double)deltaTime) / 1000.0) + " seconds");
            globalLabel.setText(getResourceString("doneIndexing"));
            log.info("Time to index all (" + (((double)deltaTime) / 1000.0) + " seconds)");
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
                log.info("Couldn't find label["+getString(name)+"]");
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
