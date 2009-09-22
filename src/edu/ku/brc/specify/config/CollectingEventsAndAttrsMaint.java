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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttribute;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttribute;
import edu.ku.brc.specify.datamodel.Institution;
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
    
    protected FixUpWorker              fixUpWorker = null;
    
    
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
    
    /**
     * @return
     */
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
    protected Vector<Object> getCollectingEventsWithManyCollectionObjects(final int collectionId)
    {
        String sql = "SELECT * FROM (SELECT CollectingEventID, count(*) AS cnt FROM collectionobject c WHERE " +
                     "CollectingEventID IS NOT NULL AND CollectionMemberID = " + collectionId + " GROUP BY CollectingEventID) T1 WHERE cnt > 1";
        return BasicSQLUtils.querySingleCol(connection, sql);
        
    }
    
    protected int getCollectingEventsWithManyCollectionObjectsCount(final int collectionId)
    {
        String sql = "SELECT SUM(CNT) FROM (SELECT CollectingEventID, count(*) AS cnt FROM collectionobject c WHERE " +
                     "CollectingEventID IS NOT NULL AND CollectionMemberID = " + collectionId + " GROUP BY CollectingEventID) T1 WHERE cnt > 1";
        return BasicSQLUtils.getCountAsInt(connection, sql);
        
    }
    
    /**
     * @throws Exception
     * 
     * Duplicates collecting events for all 'embedded collecting event' collections in the database. 
     */
    public void performMaint()
    {
        
        // First Check Prefs
        String convPrefName = "INST.CONVERTED";
        
        AppPreferences remotePrefs = AppPreferences.getRemote();
        if (remotePrefs.getBoolean(convPrefName, false))
        {
            return;
        }
        
        // Skip converted databases
        Institution institution = AppContextMgr.getInstance().getClassObject(Institution.class);
        String      remarks     = institution.getRemarks();
        if (StringUtils.isNotEmpty(remarks) && remarks.equals("Sp5Converted")) 
        {
            try
            {
                institution.setRemarks(null);
                session.beginTransaction();
                session.saveOrUpdate(institution);
                session.commit();
                
                AppContextMgr.getInstance().setClassObject(Institution.class, institution);
                remotePrefs.getBoolean(convPrefName, true);
                return;
                
            } catch (Exception ex1)
            {
                ex1.printStackTrace();
                session.rollback();
                UIRegistry.showError("Database mainteneance could not be performed.\nTry again later.\nSpecify must exit now.");
                DBConnection.shutdown();
                HibernateUtil.shutdown();
                System.exit(0);
            }
        }
        
        final ArrayList<Integer> collectionsIds = new ArrayList<Integer>(16);
        for (Object[] row : BasicSQLUtils.query("SELECT CollectionID FROM collection WHERE IsEmbeddedCollectingEvent = TRUE"))
        {
            collectionsIds.add((Integer)row[0]);
        }
        
        if (collectionsIds.size() == 0)
        {
            return;
        }
        
        int totCnt = 0;
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
        
        fixUpWorker = new FixUpWorker()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                int count = 0;
                glassPane.setText("Fixing Collecting Event Attributes..."); // I18N
                count += fixDupColEveAttrs();
                firePropertyChange(CNT, count, (int)( (100.0 * count) / totalCnt));
                
                for (Integer id : collectionsIds)
                {
                    glassPane.setText("Fixing Preparation Atttributes...");
                    count += fixDupPrepAttrs(id);
                    firePropertyChange(CNT, count, (int)( (100.0 * count) / totalCnt));
                    
                    glassPane.setText("Fixing Collection Object Atttributes...");
                    count += fixDupColObjAttrs(id);
                    firePropertyChange(CNT, count, (int)( (100.0 * count) / totalCnt));
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
        
        fixUpWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (CNT.equals(evt.getPropertyName())) 
                        {
                            int value = (Integer)evt.getNewValue();
                            if (value < 100)
                            {
                                glassPane.setProgress(value);
                            } else
                            {
                                glassPane.setProgress(100);
                            }
                        }
                    }
                });
        
        fixUpWorker.execute();
    }
    
    /**
     * @param collectionId
     */
    protected int fixDupColObjAttrs(final int collectionId)
    {
        int count    = 0;
        int localCnt = 0;
        String sql = "SELECT * FROM (SELECT CollectionObjectAttributeID, count(*) AS cnt FROM collectionobject c WHERE " +
        		     " CollectionObjectAttributeID IS NOT NULL AND c.CollectionMemberId = " + collectionId + " GROUP BY CollectionObjectAttributeID) T1 WHERE cnt > 1";
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        if (rows != null)
        {
            for (Object[] row : rows)
            {
                localCnt++;
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
                                    session.evict(colObj);
                                    count++;
                                    
                                } catch (Exception ex1)
                                {
                                    ex1.printStackTrace();
                                    session.rollback();
                                }
                            }
                            cnt++;
                        }
                        session.evict(colObjAttr);
                        
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
        int count    = 0;
        int localCnt = 0;
        String sql = "SELECT * FROM (SELECT CollectingEventAttributeID, count(*) AS cnt FROM collectingevent c WHERE " +
                     "CollectingEventAttributeID IS NOT NULL GROUP BY CollectingEventAttributeID) T1 WHERE cnt > 1";
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        if (rows != null)
        {
            for (Object[] row : rows)
            {
                fixUpWorker.doFirePropertyChange(CNT, -1, (int)(localCnt / (double)rows.size()));
                localCnt++;
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
                                    
                                    session.evict(owner);
                                    count++;
                                    
                                } catch (Exception ex1)
                                {
                                    ex1.printStackTrace();
                                    session.rollback();
                                }
                            }
                            cnt++;
                        }
                        session.evict(attrOwner);
                        
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
        int count    = 0;
        int localCnt = 0;
        String sql = "SELECT * FROM (SELECT PreparationAttributeID, count(*) AS cnt FROM preparation p WHERE " +
                     "PreparationAttributeID IS NOT NULL AND p.CollectionMemberId = " + collectionId + " GROUP BY PreparationAttributeID) T1 WHERE cnt > 1";
        Vector<Object[]> rows = BasicSQLUtils.query(connection, sql);
        if (rows != null)
        {
            for (Object[] row : rows)
            {
                fixUpWorker.doFirePropertyChange(CNT, -1, (int)(localCnt / (double)rows.size()));
                localCnt++;
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
                                    session.evict(owner);
                                    count++;

                                } catch (Exception ex1)
                                {
                                    ex1.printStackTrace();
                                    session.rollback();
                                }
                            }
                            cnt++;
                        }
                        session.evict(attrOwner);
                        
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

    
    abstract class FixUpWorker extends SwingWorker<Integer, Integer>
    {
        public void doFirePropertyChange(final String propName, final Object oldVal, final Object newVal)
        {
            firePropertyChange(propName, oldVal, newVal);
        }
    }
}
