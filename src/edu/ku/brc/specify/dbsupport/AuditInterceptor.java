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

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import edu.ku.brc.af.core.ExpressResultsTableInfo;
import edu.ku.brc.af.core.ExpressSearchIndexer;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.ui.forms.FormDataObjIFace;


/**
 * This class watches for all Hibernate updates and modifies the Lucene index to make sure it always remains up to date.
 *
 * @code_status Alpha
 *
 * @author rods
 *
 */
public class AuditInterceptor  extends edu.ku.brc.dbsupport.AuditInterceptor
{
    private static final Logger log = Logger.getLogger(AuditInterceptor.class);
    
    protected enum IndexAction {New, Update, Delete}
    
    protected ExpressSearchIndexer     indexer        = null;
    protected Vector<FormDataObjIFace> newFormObjsList    = new Vector<FormDataObjIFace>();
    protected Vector<FormDataObjIFace> updateFormObjsList = new Vector<FormDataObjIFace>();
    protected Vector<FormDataObjIFace> removeFormObjsList = new Vector<FormDataObjIFace>();
    
    
    /**
     * 
     */
    public AuditInterceptor()
    {
        super();
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AuditInterceptor#onDelete(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public void onDelete(Object entity,
                         Serializable id,
                         Object[] state,
                         String[] propertyNames,
                         Type[] types)
    {
        log.info("onDelete "+entity);
        //updateIndex(entity, IndexAction.Delete);
        if (entity instanceof FormDataObjIFace)
        {
            FormDataObjIFace formObj = (FormDataObjIFace)entity;
            if (formObj.getId() == null)
            {
                removeFormObjsList.add(formObj);
                
            } else
            {
                updateIndex(formObj, IndexAction.Delete);        
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.hibernate.EmptyInterceptor#postFlush(java.util.Iterator)
     */
    public void postFlush(Iterator entities)
    {
        while (entities.hasNext())
        {
            Object entity = entities.next();
            if (entity instanceof FormDataObjIFace)
            {
                FormDataObjIFace formObj = (FormDataObjIFace)entity;
                if (newFormObjsList.contains(formObj))
                {
                    newFormObjsList.remove(formObj);
                    updateIndex(formObj, IndexAction.New);
                    System.err.println("postFlush"+formObj);
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AuditInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public boolean onFlushDirty(Object entity,
                                Serializable id,
                                Object[] currentState,
                                Object[] previousState,
                                String[] propertyNames,
                                Type[] types)
    {
        log.info("onFlushDirty "+entity);
        
        //updateIndex(entity, IndexAction.Update);
        
        if (entity instanceof FormDataObjIFace)
        {
            FormDataObjIFace formObj = (FormDataObjIFace)entity;
            if (formObj.getId() == null)
            {
                updateFormObjsList.add(formObj);
                
            } else
            {
                updateIndex(formObj, IndexAction.Update);        
            }
        }        
        
        return false; // Don't veto
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AuditInterceptor#onLoad(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public boolean onLoad(Object entity,
                          Serializable id,
                          Object[] state,
                          String[] propertyNames,
                          Type[] types)
    {
        //log.info("onLoad "+entity);
        return false; // Don't veto
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AuditInterceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public boolean onSave(Object entity,
                          Serializable id,
                          Object[] state,
                          String[] propertyNames,
                          Type[] types)
    {
        log.info("onSave "+entity);
        
        if (entity instanceof FormDataObjIFace)
        {
            FormDataObjIFace formObj = (FormDataObjIFace)entity;
            if (formObj.getId() == null)
            {
                newFormObjsList.add(formObj);
                
            } else
            {
                updateIndex(formObj, IndexAction.New);        
            }
        }
        
        
        return false; // Don't veto
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AuditInterceptor#afterTransactionCompletion(org.hibernate.Transaction)
     */
    @Override
    public void afterTransactionCompletion(Transaction tx)
    {
        //log.info("afterTransactionCompletion "+newFormObjsList.size());
        newFormObjsList.clear();
        updateFormObjsList.clear();
        newFormObjsList.clear();
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
        /*
        if (indexer == null)
        {
            indexer = new ExpressSearchIndexer(ExpressSearchTask.getIndexDirPath(), null);
        }
        
        IndexWriter writer = ExpressSearchIndexer.createIndexWriter(ExpressSearchTask.getIndexDirPath(), false); // false - do not force a new index to be created
        
        
        String updateSQLStr = tblInfo.getUpdateSql();
        if (updateSQLStr != null)
        {
            String sql = String.format(tblInfo.getUpdateSql(), new Object[] {formObj.getId()});
            
            log.info("["+sql+"]");
            
            indexer.indexQuery(0, writer, tblInfo, sql);
        } else
        {
            log.error("Update String was NUll for TableInfo["+tblInfo.getId()+"]");
        }

        writer.close();
        */
        
    }
    
    /**
     * Upates the Lucene Index information for the Form Data Object
     * @param formObj the data object
     * @param action the action to take
     * @return true on sucess
     */
    protected boolean updateIndex(final FormDataObjIFace formObj, final IndexAction action)
    {
        try
        {
            IndexReader   reader   = IndexReader.open(FSDirectory.getDirectory(ExpressSearchTask.getIndexDirPath(), false));
            IndexSearcher searcher = new IndexSearcher(reader);
            
            Hashtable<String, ExpressResultsTableInfo> tableInfoHash = ExpressSearchTask.getTableInfoHash();
            for (Enumeration<ExpressResultsTableInfo> e=tableInfoHash.elements();e.hasMoreElements();)
            {
                ExpressResultsTableInfo tblInfo = e.nextElement();
                
                if (tblInfo.isExpressSearch())
                {
                    if (formObj.getTableId() == Integer.parseInt(tblInfo.getTableId()))
                    {
                        if (action == IndexAction.New)
                        {
                            update(formObj, tblInfo);
                            
                        } else if (action == IndexAction.Update || action == IndexAction.Delete)
                        {
                            
                            Query query = new TermQuery(new Term("id", Long.toString(formObj.getId())));
                            Hits  hits  = searcher.search(query);
                            System.out.println("Hits: "+hits.length()+"  Query["+query.toString("contents")+"]");
                            for (int i=0;i<hits.length();i++)
                            {
                                Document doc = hits.doc(i);
                                String   sid = doc.get("sid");
                                System.out.println("sid: ["+sid+"]["+tblInfo.getId()+"] id["+doc.get("id")+"]"); 
                                if (sid != null && sid.equals(tblInfo.getId()))
                                {
                                    System.out.println("sid: ["+tblInfo.getTableId()+"] id["+tblInfo.getId()+"]");
                                    System.out.println("Removing["+hits.id(i)+"] "+sid);
                                    
                                    reader.deleteDocument(hits.id(i));
                                    
                                    if (action == IndexAction.Update)
                                    {
                                        update(formObj, tblInfo);
                                    }
                                }
                            }
                        }
                    }
                    
                    ExpressResultsTableInfo.JoinColInfo[] joinColInfo = tblInfo.getJoins();
                    if (joinColInfo != null)
                    {
                        for (ExpressResultsTableInfo.JoinColInfo jci : joinColInfo)
                        {
                            if (jci.getJoinTableIdAsInt() == formObj.getTableId())
                            {
                                Query joinQuery = new TermQuery(new Term(jci.getJoinTableId(), Long.toString(formObj.getId())));
                                Hits  joinHits  = searcher.search(joinQuery);
                                System.out.println("Hits: "+joinHits.length()+"  Query["+joinQuery.toString("contents")+"]");
                                for (int i=0;i<joinHits.length();i++)
                                {
                                    Document doc = joinHits.doc(i);
                                    String   sid = doc.get("sid");
                                    System.out.println("sid: ["+sid+"]["+tblInfo.getId()+"] id["+doc.get("id")+"]"); 
                                    if (sid != null && sid.equals(tblInfo.getId()))
                                    {
                                        System.out.println("sid: ["+tblInfo.getTableId()+"] id["+tblInfo.getId()+"]");
                                        System.out.println("Removing["+joinHits.id(i)+"] "+sid);
                                        
                                        //reader.deleteDocument(joinHits.id(i));
                                        
                                        if (action == IndexAction.Update)
                                        {
                                            update(formObj, tblInfo);
                                        }
                                    }
                                }
                                for (int i=0;i<joinHits.length();i++)
                                {
                                    Document doc = joinHits.doc(i);
                                    String   sid = doc.get("sid");
                                    if (sid != null && sid.equals(tblInfo.getId()))
                                    {
                                        reader.deleteDocument(joinHits.id(i));
                                    }
                                }

                            }
                        }
                    }
                }
            }
            
            searcher.close();
                
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * Upates the Lucene Index information for the Form Data Object
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
