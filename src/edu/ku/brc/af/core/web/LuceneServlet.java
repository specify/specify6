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
package edu.ku.brc.af.core.web;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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

import edu.ku.brc.af.core.ExpressSearchIndexer;
import edu.ku.brc.af.core.ExpressSearchIndexer.ExpressSearchIndexerListener;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.ui.UIHelper;

/**
 * Needs a lot work. but this is the start. Also note that the DBConnection values are hard coded for now, they need to be passed across in the request.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 24, 2006
 *
 */
public class LuceneServlet extends HttpServlet implements ExpressSearchIndexerListener
{
    private static final Logger log = Logger.getLogger(LuceneServlet.class);
    
    protected Analyzer                     analyzer       = new StandardAnalyzer();
    protected File                         lucenePath     = null;
    protected String                       databaseName   = null;
    
    protected byte[]                       bytes      = new byte[64];
    protected OutputStream                 outStream  = null;
    
    protected Hashtable<Long, TableIdInfo> tableIdHash = new Hashtable<Long, TableIdInfo>();
    protected Stack<TableIdInfo>           recycler    = new Stack<TableIdInfo>();
    
    /**
     * 
     */
    public LuceneServlet()
    {

    }

    /**
     * writes a long into the response stream
     * @param val the long value
     * @throws IOException when it occurs
     */
    protected void putLong(long val) throws IOException
    {
        LittleEndian.putLong(bytes, val);
        outStream.write(bytes, 0, LittleEndianConsts.LONG_SIZE);
    }
    
    /**
     * Respond to a GET request for the content produced by
     * this servlet.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are producing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doGet(final HttpServletRequest  request,
                      final HttpServletResponse response) throws IOException, ServletException 
      {

        response.setContentType("application/octet-stream");
        //response.setContentType("application/x-binary");
        
        if (request.getParameter("index") != null)
        {
            String databaseName = "accessions";
            if (!UIHelper.tryLogin("com.mysql.jdbc.Driver", 
                                  "org.hibernate.dialect.MySQLDialect", 
                                  databaseName, 
                                  "jdbc:mysql://localhost/"+databaseName, "rods", "rods"))
            {
                throw new RuntimeException("Couldn't login into ["+databaseName+"] "+DBConnection.getInstance().getErrorMsg());
            }
            ExpressSearchIndexer indexer = new ExpressSearchIndexer(lucenePath, null, true);
            indexer.start();
            
        } else
        {
            databaseName = request.getParameter("db");
            if (StringUtils.isNotEmpty(databaseName))
            {
                lucenePath   = new File("/Users/rods/.Specify/"+databaseName+"/index-dir");
                if (lucenePath.exists())
                {
                    outStream    = response.getOutputStream();
                    doQuery(request.getParameter("q"));
                    outStream = null;
                } else
                {
                    log.info("Dir index file doesn't exist.");
                    putLong(0);
                }
            } else
            {
                log.info("No database name specified.");
                putLong(0);
            }
            
        }
    }
    
    /**
     * Performs the express search and returns the results.
     */
    protected void doQuery(final String searchTerm)
    {
        if (searchTerm != null && searchTerm.length() > 0)
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
                query = parser.parse(searchTerm);
                
                //log.info(query.toString());

                Hits hits = searcher.search(query);
                
                putLong((long)hits.length());
                
                if (hits.length() == 0)
                {
                   

                } else
                {
                    //log.debug(hits.length()+" Hits for ["+searchTerm+"]["+query.toString()+"]");
    
                    // can be sped up if I figure out how to sort it
                    for (int i=0;i<hits.length();i++)
                    {
                        Document doc = hits.doc(i);
                        
                        long        tableId = Long.parseLong(doc.get("table"));
                        Long        tID     = new Long(tableId);
                        
                        TableIdInfo tii     = tableIdHash.get(tID);
                        if (tii == null)
                        {
                            if ( recycler.size() > 0)
                            {
                                tii = recycler.pop();
                                tii.reset();
                                
                            } else
                            {
                                tii = new TableIdInfo();
                            }
                            tii.setTableId(tableId);
                            tableIdHash.put(tID, tii);
                        }
                        tii.add(doc.get("id"));
                    }
                    
                    log.info("size "+tableIdHash.size());
                    
                    putLong((long)tableIdHash.size());
                    for (Enumeration<TableIdInfo> e=tableIdHash.elements();e.hasMoreElements();)
                    {
                        TableIdInfo tii = e.nextElement();
                        tii.output(outStream);
                        recycler.push(tii);
                    }
                    tableIdHash.clear();
                    outStream.flush();
                }

            } catch (ParseException ex)
            {
                //writer.println(ex.toString());
                ex.printStackTrace();
                //JOptionPane.showMessageDialog(UICacheManager.get(UICacheManager.FRAME), getResourceString("BadQuery"), getResourceString("BadQueryTitle"), JOptionPane.ERROR_MESSAGE);
                //log.error(ex);

            } catch (IOException ex)
            {
                //writer.println(ex.toString());
                ex.printStackTrace();
                // XXX Change message
                //JOptionPane.showMessageDialog(UICacheManager.get(UICacheManager.FRAME), getResourceString("BadQuery"), getResourceString("BadQueryTitle"), JOptionPane.ERROR_MESSAGE);
                //log.error(ex);
            }
        } else
        {
            //writer.println("Bad Search Term.");
        }
    }
    
    class TableIdInfo
    {
        protected long         tableId  = -1;
        protected boolean      useFloat = false;
        protected Vector<Long> ids      = new Vector<Long>(128);
        protected boolean      knowsType = false;
        
        public TableIdInfo()
        {
            
        }
        
        public void reset()
        {
            tableId = -1;
            useFloat = false;
            ids.clear();
            knowsType = false;
        }
        
        public void output(final OutputStream os) throws IOException
        {
            putLong(tableId);
            putLong(useFloat ? 1L : 0L);
            long numItems = (long)ids.size();
            putLong(numItems);
            for (int i=0;i<numItems;i++)
            {
                putLong(((Long)ids.get(i)).longValue());
            }
        }
        
        public void add(String idStr)
        {
            if (!knowsType)
            {
                useFloat  = idStr.indexOf(".") > -1;
                knowsType = true;
            }
            ids.add(Long.parseLong(idStr));
        }

        public Vector getIds()
        {
            return ids;
        }

        public long getTableId()
        {
            return tableId;
        }

        public void setTableId(long tableId)
        {
            this.tableId = tableId;
        }

        public boolean isUseFloat()
        {
            return useFloat;
        }
    }
    
    //------------------------------------------------------
    // ExpressSearchIndexerListener
    //------------------------------------------------------
    
    public void namedSections(List<String> sectionNames)
    {
        for (String section : sectionNames)
        {
            log.info(section);
        }
    }
    
    public void outOfDate(String name, boolean isOutOfDate)
    {
        log.info(name+" "+isOutOfDate);
    }
    
    public void outOfDateComplete(boolean allOK)
    {
        log.info(allOK); 
    }
    
    public void startedIndexing()
    {
        log.info("startedIndexing"); 
    }
    
    public void processing(double percentage)
    {
        log.info("processing "+percentage); 
    }
    
    public void endedIndexing(int minutes, int seconds)
    {
        log.info("endedIndexing "+minutes+" "+seconds); 
    }
    
    public void complete()
    {
        log.info("complete "); 
    }
    
    public void prepareSection(int section)
    {
        log.info("prepareSection "+section); 
    }
    
    public void startedSection(int section)
    {
        log.info("startedSection "+section); 
    }
    
    public void processingSection(int section, double percentage)
    {
        log.info("startedIndexing "+section+"  "+percentage); 
    }
    
    public void endedSection(int section)
    {
        log.info("endedSection "+section); 
    }
}
