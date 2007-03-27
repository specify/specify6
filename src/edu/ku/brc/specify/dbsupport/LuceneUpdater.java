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

package edu.ku.brc.specify.dbsupport;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;

import edu.ku.brc.af.core.ExpressResultsTableInfo;
import edu.ku.brc.af.core.ExpressSearchIndexer;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 * 
 * This class is responsible for updating a "local" Lucene index. Local can mean on the same machine as the application or this
 * class may be used by a servelet to update the index locally on the remote machine. This is a singleton.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Oct 23, 2006
 *
 */
public class LuceneUpdater
{
    protected static final Logger       log       = Logger.getLogger(LuceneUpdater.class);
    protected static       LuceneUpdater instance = new LuceneUpdater();
    
    public enum IndexAction {New, Update, Delete}
    
    // Data Members
    protected ExpressSearchIndexer indexer = null;
    
    /**
     * Constructor.
     */
    protected LuceneUpdater()
    {
        
    }
    
    /**
     * Returns the singleton instance.
     * @return the singleton instance.
     */
    public static LuceneUpdater getInstance()
    {
        return instance;
    }

    
    /**
     * Creates a new lucene document for the database object.
     * @param formObj the object for which a new entry will be created.
     * @param tblInfo the TableInfo for the data object
     * @throws IOException
     */
    protected void update(final FormDataObjIFace        formObj, 
                          final ExpressResultsTableInfo tblInfo) throws IOException
    {
        if (indexer == null)
        {
            indexer = new ExpressSearchIndexer(ExpressSearchTask.getIndexDirPath(), null, false);
        }
        
        IndexWriter writer = ExpressSearchIndexer.createIndexWriter(ExpressSearchTask.getIndexDirPath(), false); // false - do not force a new index to be created
        
        String updateSQLStr = tblInfo.getUpdateSql(formObj.getTableId());
        if (updateSQLStr != null)
        {
            String sql = String.format(updateSQLStr, new Object[] {formObj.getId().toString()});
            
            log.info("["+sql+"]");
            
            indexer.indexQuery(0, writer, tblInfo, sql);
        } else
        {
            log.info("Update String was NUll for TableInfo["+tblInfo.getId() + "]");
        }

        writer.close();
        
    }
    
    /**
     * Upates the Lucene Index information for the Form Data Object.
     * @param formObj the data object
     * @param action the action to take
     * @return true on sucess
     */
    protected boolean updateIndex(final FormDataObjIFace formObj, final IndexAction action)
    {
        if (ExpressSearchTask.isStarted())
        {
            // Short Circut the Indexer by asking the object if it is indexable.
            if (!formObj.isIndexable())
            {
                return true;
            }
           
            try
            {
               
                if (ExpressSearchTask.doesIndexExist())
                {
                    File          lucenePath = ExpressSearchTask.getIndexDirPath();
                    IndexReader   reader     = IndexReader.open(FSDirectory.getDirectory(lucenePath, false));
                    IndexSearcher searcher   = new IndexSearcher(reader);
                    
                    Hashtable<String, ExpressResultsTableInfo> tableInfoHash = ExpressSearchTask.getTableInfoHash();
                    for (Enumeration<ExpressResultsTableInfo> e=tableInfoHash.elements();e.hasMoreElements();)
                    {
                        ExpressResultsTableInfo tblInfo = e.nextElement();
                        
                        if (tblInfo.isExpressSearch() && tblInfo.isIndexed())
                        {
                            //log.debug("["+formObj.getTableId()+"] ["+Integer.parseInt(tblInfo.getTableId())+"]"); 
                            if (formObj.getTableId() == Integer.parseInt(tblInfo.getTableId()))
                            {
                                //log.debug("TABLE ID: ["+formObj.getTableId()+"] "+action); 
                                if (action == IndexAction.New)
                                {
                                    reader.close();
                                    searcher.close();
                                    
                                    update(formObj, tblInfo);
                                    
                                    reader   = IndexReader.open(FSDirectory.getDirectory(ExpressSearchTask.getIndexDirPath(), false));
                                    searcher = new IndexSearcher(reader);
                                    
                                } else if (action == IndexAction.Update || action == IndexAction.Delete)
                                {
                                    
                                    Query query = new TermQuery(new Term("id", Long.toString(formObj.getId())));
                                    Hits  hits  = searcher.search(query);
                                    //log.debug("Hits: "+hits.length()+"  Query["+query.toString("contents")+"]");
                                    int updates = 0;
                                    for (int i=0;i<hits.length();i++)
                                    {
                                        Document doc = hits.doc(i);
                                        String   sid = doc.get("sid");
                                        //log.debug("sid: ["+sid+"]["+tblInfo.getId()+"] id["+doc.get("id")+"]"); 
                                        if (sid != null && sid.equals(tblInfo.getId()))
                                        {
                                            //log.debug("TBL ID["+tblInfo.getTableId()+"] id["+tblInfo.getId()+"]");
                                            //log.debug("Removing HitsID["+hits.id(i)+"] SID["+sid+"]");
                                            
                                            reader.deleteDocument(hits.id(i));
                                            
                                            updates++;
                                        }
                                    }
        
                                    if (updates > 0 && action == IndexAction.Update)
                                    {
                                        reader.close();
                                        searcher.close();
                                        
                                        update(formObj, tblInfo);
                                        reader   = IndexReader.open(FSDirectory.getDirectory(ExpressSearchTask.getIndexDirPath(), false));
                                        searcher = new IndexSearcher(reader);
                                    }
                                }
                            }
                        }
                    }
                    
                    searcher.close();
                    reader.close();
                }  
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        // else
        return false;
    }
    
    /**
     * Upates the Lucene Index information for the Form Data Object.
     * @param formObj the data object
     * @param action the action to take
     * @return true on sucess
     */
    protected boolean updateIndex(final Object entity, final IndexAction action)
    {
        if (entity instanceof FormDataObjIFace)
        {
            return updateIndex((FormDataObjIFace)entity, action);
        }
        return false;
    }
}
