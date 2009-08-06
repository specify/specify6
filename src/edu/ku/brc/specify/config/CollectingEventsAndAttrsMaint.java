/**
 * 
 */
package edu.ku.brc.specify.config;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttribute;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttribute;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.PreparationAttribute;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;

/**
 * @author timo
 * @author rods
 *
 * duplicateCollectingEvents() ensures that for 'embedded collecting event' collections, the effective relationship between
 * CollectingEvent AND CollectionObject is one-to-one.
 * 
 * Any CollectingEvents that have N > 1 related CollectionObjects are cloned N times to create a distinct CollectingEvent 
 * for each related CollectionObject.
 * 
 */
public class CollectingEventsAndAttrsMaint
{
    private static String CNT = "CNT";
    
    protected static final Logger  log = Logger.getLogger(CollectingEventsAndAttrsMaint.class);
    
    protected Connection               connection;
    protected DataProviderSessionIFace session;
    
    
    /**
     * 
     */
    public CollectingEventsAndAttrsMaint()
    {
        try
        {
            connection = DBConnection.getInstance().createConnection();
            session    = DataProviderFactory.getInstance().createSession();
        
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * Cleans up connections.
     */
    public void shutdown()
    {
        try
        {
            connection.close();
            session.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param collectionId
     * @return
     */
    protected int getCountForMaint(final int collectionId)
    {
        int count = 0;
        boolean isEmbeddedCE = AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent();
        if (isEmbeddedCE)
        {
            String sql = "SELECT SUM(cnt) FROM (SELECT CollectingEventID, count(*) AS cnt FROM collectionobject WHERE " +
                         "CollectingEventID IS NOT NULL AND CollectionMemberID = " + collectionId + " GROUP BY CollectingEventID) T1 WHERE cnt > 1";
            count += BasicSQLUtils.getCountAsInt(connection, sql);
        }
        
        String sql = "SELECT COUNT(*) FROM (SELECT CollectionObjectAttributeID, count(*) AS cnt FROM collectionobject c WHERE " + 
                     "CollectionObjectAttributeID IS NOT NULL AND CollectionMemberId = " + collectionId + " GROUP BY CollectionObjectAttributeID) T1 WHERE cnt > 1";
        count += BasicSQLUtils.getCountAsInt(connection, sql);
        
         sql = "SELECT * FROM (SELECT PreparationAttributeID, count(*) AS cnt FROM preparation WHERE " +
               "PreparationAttributeID IS NOT NULL AND CollectionMemberId = " + collectionId + " GROUP BY PreparationAttributeID) T1 WHERE cnt > 1";
        count += BasicSQLUtils.getCountAsInt(connection, sql);
        
        return count;
    }
    
    protected int getCECountForMaint()
    {
        String sql = "SELECT COUNT(*) FROM (SELECT CollectingEventAttributeID, count(*) AS cnt FROM collectingevent WHERE " + 
                     "CollectingEventAttributeID IS NOT NULL GROUP BY CollectingEventAttributeID) T1 WHERE cnt > 1";
        return BasicSQLUtils.getCountAsInt(connection, sql);
    }
    
    /**
     * @param collectionId
     * @return
     */
    protected Vector<Object[]> getCollectingEventsWithManyCollectionObjects(final int collectionId)
    {
        
        String sql = "SELECT * FROM (SELECT CollectingEventID, count(*) AS cnt FROM collectionobject c WHERE " +
                     "CollectingEventID IS NOT NULL AND CollectionMemberID = " + collectionId + " GROUP BY CollectingEventID) T1 WHERE cnt > 1";
        return BasicSQLUtils.query(connection, sql);
        
    }
    
    /**
     * @param collectionId id of an 'embedded collecting event' collection.
     * @throws Exception
     */
    protected int duplicateCollectingEvents(final int collectionId)
    {
        int cnt = 0;
        for (Object[] ce : getCollectingEventsWithManyCollectionObjects(collectionId))
        {
            cnt += duplicateCollectingEvent(ce[0]);
        }
        return cnt;
    }
    
    /**
     * @param ceid id for a collecting event with many (> 1) collection objects
     * @throws Exception
     */
    protected int duplicateCollectingEvent(final Object ceid)
    {
        int cnt = 0;
        Vector<Object[]> cos = BasicSQLUtils.query(connection, "SELECT CollectionObjectId FROM collectionobject WHERE CollectingEventID = " + ceid);
        if (cos != null && cos.size() > 0)
        {
            try
            {
                session.beginTransaction();
                CollectingEvent ce = session.get(CollectingEvent.class, (Integer )ceid);
                for (int co = 1; co < cos.size(); co++)
                {
                    CollectingEvent ceClone = (CollectingEvent)ce.clone();
                    session.saveOrUpdate(ceClone);
                    CollectionObject coObj = session.get(CollectionObject.class, (Integer )cos.get(co)[0]);
                    coObj.setCollectingEvent(ceClone);
                    session.saveOrUpdate(coObj);
                    cnt++;
                }
                session.commit();
                
            } catch (Exception ex)
            {
                session.rollback();
                log.error(ex);
                
            }
        }
        return cnt;
    }
    
    /**
     * @throws Exception
     * 
     * Duplicates collecting events for all 'embedded collecting event' collections in the database. 
     */
    public void performMaint()
    {
        final ArrayList<Integer> collectionsIds = new ArrayList<Integer>(16);
        for (Object[] row : BasicSQLUtils.query("SELECT CollectionID FROM collection WHERE IsEmbeddedCollectingEvent = TRUE"))
        {
            collectionsIds.add((Integer)row[0]);
        }
        
        if (collectionsIds.size() == 0)
        {
            return;
        }
        
        int totCnt = getCECountForMaint();
        for (Integer id : collectionsIds)
        {
            totCnt += getCountForMaint(id);  // CollectionID
        }
        
        if (totCnt == 0)
        {
            return;
        }
         
        final int             totalCnt  = totCnt;
        final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("PERFORMING_MAINT"), 24);

        glassPane.setProgress(0);
        
        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                int count = 0;
                count += fixDupColEveAttrs();
                firePropertyChange(CNT, count, (int)( (100.0 * totalCnt) / count));
                
                for (Integer id : collectionsIds)
                {
                    count += duplicateCollectingEvents(id);
                    firePropertyChange(CNT, count, (int)( (100.0 * totalCnt) / count));
                    
                    count += fixDupPrepAttrs(id);
                    firePropertyChange(CNT, count, (int)( (100.0 * totalCnt) / count));
                    
                    count += fixDupColObjAttrs(id);
                    firePropertyChange(CNT, count, (int)( (100.0 * totalCnt) / count));
                }
                return null;
            }
            @Override
            protected void done()
            {
                super.done();
                
                glassPane.setProgress(100);
                
                UIRegistry.clearSimpleGlassPaneMsg();
            }
        };
        
        worker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (CNT.equals(evt.getPropertyName())) 
                        {
                            int value = (Integer)evt.getNewValue();
                            
                            if (value < 100)
                            {
                                glassPane.setProgress((Integer)evt.getNewValue());
                            } else
                            {
                                glassPane.setProgress(100);
                            }
                        }
                    }
                });
        
        worker.execute();
    }
    
    /**
     * @param collectionId
     */
    protected int fixDupColObjAttrs(final int collectionId)
    {
        int count = 0;
        String sql = "SELECT * FROM (SELECT CollectionObjectAttributeID, count(*) AS cnt FROM collectionobject c WHERE " +
        		     " CollectionObjectAttributeID IS NOT NULL AND c.CollectionMemberId = " + collectionId + " GROUP BY CollectionObjectAttributeID) T1 WHERE cnt > 1";
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        if (rows != null)
        {
            for (Object[] row : rows)
            {
                try
                {
                    int id = (Integer)row[0];
                    CollectionObjectAttribute colObjAttr = session.get(CollectionObjectAttribute.class, id);
                    Set<CollectionObject> cos = colObjAttr.getCollectionObjects();
                    
                    if (colObjAttr != null)
                    {
                        int cnt = 0;
                        for (CollectionObject co : cos)
                        {
                            if (cnt > 0)
                            {
                                try
                                {
                                    CollectionObjectAttribute colObjAttribute = (CollectionObjectAttribute)colObjAttr.clone();
                                    
                                    CollectionObject colObj = session.get(CollectionObject.class, co.getCollectionObjectId());
                                    colObj.setCollectionObjectAttribute(colObjAttribute);
                                    colObjAttribute.getCollectionObjects().add(colObj);
                                    session.beginTransaction();
                                    session.saveOrUpdate(colObjAttribute);
                                    session.saveOrUpdate(co);
                                    session.commit();
                                    count++;
                                    
                                } catch (Exception ex1)
                                {
                                    ex1.printStackTrace();
                                    session.rollback();
                                }
                            }
                            cnt++;
                        }
                    } else
                    {
                        log.error("CollectionObjectAttribute is: "+colObjAttr);
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                   
                }
            }
        }
        return count;
    }
    
    /**
     * @param collectionId
     */
    protected int fixDupColEveAttrs()
    {
        int count = 0;
        String sql = "SELECT * FROM (SELECT CollectingEventAttributeID, count(*) AS cnt FROM collectingevent c WHERE " +
                     "CollectingEventAttributeID IS NOT NULL GROUP BY CollectingEventAttributeID) T1 WHERE cnt > 1";
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        if (rows != null)
        {
            for (Object[] row : rows)
            {
                try
                {
                    int id = (Integer)row[0];
                    CollectingEventAttribute attrOwner = session.get(CollectingEventAttribute.class, id);
                    Set<CollectingEvent> set = attrOwner.getCollectingEvents();
                    
                    if (attrOwner != null)
                    {
                        int cnt = 0;
                        for (CollectingEvent obj : set)
                        {
                            if (cnt > 0)
                            {
                                try
                                {
                                    CollectingEventAttribute newAttr = (CollectingEventAttribute)attrOwner.clone();
                                    
                                    CollectingEvent owner = session.get(CollectingEvent.class, obj.getCollectingEventId());
                                    owner.setCollectingEventAttribute(newAttr);
                                    newAttr.getCollectingEvents().add(owner);
                                    session.beginTransaction();
                                    session.saveOrUpdate(newAttr);
                                    session.saveOrUpdate(obj);
                                    session.commit();
                                    count++;
                                    
                                } catch (Exception ex1)
                                {
                                    ex1.printStackTrace();
                                    session.rollback();
                                }
                            }
                            cnt++;
                        }
                    } else
                    {
                        log.error("CollectingEventAttribute is: "+attrOwner);
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                   
                }
            }
        }
        return count;
    }
    
    protected int fixDupPrepAttrs(final int collectionId)
    {
        int count = 0;
        String sql = "SELECT * FROM (SELECT PreparationAttributeID, count(*) AS cnt FROM preparation p WHERE " +
                     "PreparationAttributeID IS NOT NULL AND p.CollectionMemberId = " + collectionId + " GROUP BY PreparationAttributeID) T1 WHERE cnt > 1";
        Vector<Object[]> rows = BasicSQLUtils.query(connection, sql);
        if (rows != null)
        {
            for (Object[] row : rows)
            {
                try
                {
                    int id = (Integer)row[0];
                    PreparationAttribute attrOwner = session.get(PreparationAttribute.class, id);
                    Set<Preparation> set = attrOwner.getPreparations();
                    
                    if (attrOwner != null)
                    {
                        int cnt = 0;
                        for (Preparation obj : set)
                        {
                            if (cnt > 0)
                            {
                                try
                                {
                                    PreparationAttribute newAttr = (PreparationAttribute)attrOwner.clone();
                                    
                                    Preparation owner = session.get(Preparation.class, obj.getPreparationId());
                                    owner.setPreparationAttribute(newAttr);
                                    newAttr.getPreparations().add(owner);
                                    session.beginTransaction();
                                    session.saveOrUpdate(newAttr);
                                    session.saveOrUpdate(obj);
                                    session.commit();
                                    count++;

                                } catch (Exception ex1)
                                {
                                    ex1.printStackTrace();
                                    session.rollback();
                                }
                            }
                            cnt++;
                        }
                    } else
                    {
                        log.error("PreparationAttribute is: "+attrOwner);
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                   
                }
            }
        }
        return count;
    }

}
