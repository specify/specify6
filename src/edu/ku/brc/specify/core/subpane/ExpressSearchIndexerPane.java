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


import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

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

import edu.ku.brc.specify.core.ExpressSearchTask;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.ui.UICacheManager;

/**
 * A pane enables the user to see (and control) the indexing process for express search.<BR>
 * NOTE: This creates the index cache locality, it doesn't support is being on the network.
 * 
 * @author rods
 *
 */
public class ExpressSearchIndexerPane extends BaseSubPane implements Runnable
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

    /**
     * Default Constructor
     *
     */
    public ExpressSearchIndexerPane(final ExpressSearchTask task)
    {
        super(getResourceString("IndexerPane"), task);
         
        lucenePath = ExpressSearchTask.getIndexDirPath();
        
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,2dlu,p", "c:p,5dlu,c:p,5dlu,c:p,5dlu,c:p"));
        CellConstraints cc         = new CellConstraints();

        removeAll();
        progressBar.setIndeterminate(false);

        indvLabel         = new JLabel("", JLabel.RIGHT);
        globalLabel       = new JLabel(getResourceString("Indexing_Descr"), JLabel.RIGHT);
        globalProgressBar = new JProgressBar();
        
        cancelBtn = new JButton(getResourceString("CancelIndexing"));
        closeBtn  = new JButton(getResourceString("Close"));
        closeBtn.setVisible(false);
        
        builder.add(indvLabel, cc.xy(1,1));
        builder.add(progressBar, cc.xy(3,1));
        
        builder.add(globalLabel, cc.xy(1,3));
        builder.add(globalProgressBar, cc.xy(3,3));
        builder.add(cancelBtn, cc.xywh(1,5,3,1));
        builder.add(closeBtn, cc.xywh(1,7,3,1));
        
        PanelBuilder    builder2    = new PanelBuilder(new FormLayout("center:p:g", "center:p:g"));
        builder2.add(builder.getPanel(), cc.xy(1,1));
   
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
     * Performs a query and then indexes all the results for each orw and column
     * @param writer the lucene writer
     * @param sqlStr the SQL to be executed
     * @param tableId the table ID 
     * @param fields the column positions in the results of those items to be indexed
     * @return the time it took in milliseconds
     */
    public long indexQuery(final IndexWriter writer, final String sqlStr, int tableId, int[] fields)
    {
        Connection dbConnection = DBConnection.getInstance().getConnection();
        Statement  dbStatement = null;
        
        long begin = 0;
        
        progressBar.setIndeterminate(true);
        
        try
        {
            if (dbConnection != null)
            {
                dbStatement = dbConnection.createStatement();
                
                log.info("SQL ["+sqlStr+"]");
                ResultSet rs = dbStatement.executeQuery(sqlStr);
                
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
                        progressBar.setValue(rowCnt++);
                        Document doc = new Document();
                        doc.add(Field.Keyword("id", rs.getObject(fields[0]).toString()));
                        doc.add(Field.UnIndexed("table", Integer.toString(tableId)));
                        
                        int cnt = 0;
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
            log.error("Error in run["+sqlStr+"]", ex);      
        } catch (Exception ex)
        {
            //ex.printStackTrace();
            log.error("Error in run["+sqlStr+"]", ex);           
        }
        long end = new Date().getTime();

        long delta = end - begin;
        log.info("Time to index (" + delta + " ms)");
        return delta;

        
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
                int    id    = Integer.parseInt(tableElement.attributeValue("id"));
                String title = tableElement.attributeValue("title");
                
                Element indexElement = (Element)tableElement.selectSingleNode("index");
                String  sqlStr       = indexElement.selectSingleNode("sql").getText();
                                
                List colItems = indexElement.selectNodes("cols/col");
                int[] cols = new int[colItems.size()];
                for (int i=0;i<colItems.size();i++)
                {
                    Element colElement = (Element)colItems.get(i);
                    cols[i] = Integer.parseInt(colElement.getTextTrim());
               }
               log.info("Indexing: "+title+"  Id: "+id);
               indvLabel.setText(title);
               deltaTime += indexQuery(writer, sqlStr, id, cols);
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
                System.out.println(fileNames[i]);
                File file = new File(lucenePath.getAbsoluteFile()+File.separator+fileNames[i]);
                file.delete();
            }
            lucenePath.delete();
            
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
 }
