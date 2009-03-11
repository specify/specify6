/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.StaleObjectException;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpTaskSemaphore;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.UIRegistry;

/**
 * This class is used to lock functionality that is shared across the network. For example, two users should never be
 * able to edit the Taxon tree at the same time. This enables a user to lock or own the task. This also enables users
 * to override the lock in case of a network failure or the application somehow crashing or being stopped by a 
 * machine shutting down.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jul 16, 2008
 *
 */
public class TaskSemaphoreMgr
{
    private static final Logger log = Logger.getLogger(TaskSemaphoreMgr.class);
    
    public enum SCOPE       {Global, Discipline, Collection}
    public enum USER_ACTION {OK, Error, Cancel, ViewMode, Override}
    
    private static boolean previouslyLocked = false;
    private static String prevLockedBy = null;
    
    /**
     * Check to see if the lock is set on the task semaphore.
     * @param title The human (localized) title of the task 
     * @param name the unique name
     * @param scope the scope of the lock
     * @return true if it is locked.
     */
    public static boolean isLocked(final String title, 
                                   final String name, 
                                   final SCOPE  scope)
    {
        Discipline discipline = scope == SCOPE.Discipline ? AppContextMgr.getInstance().getClassObject(Discipline.class) : null;
        Collection collection = scope == SCOPE.Collection ? AppContextMgr.getInstance().getClassObject(Collection.class) : null;
       
        Connection connection = DBConnection.getInstance().createConnection();
        if (connection != null)
        {
            Statement  stmt = null;
            ResultSet  rs   = null;
            try
            {
                String sql = buildSQL(name, scope, discipline, collection);
                //log.debug(sql);
                
                stmt = connection.createStatement();
                rs = stmt.executeQuery(sql);
                if (rs != null && rs.next())
                {
                    return rs.getBoolean(1);
                }
                return false;
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, ex);
                //log.error(ex);
            } finally 
            {
                try
                {
                    if (rs != null)
                    {
                        rs.close();
                    }
                    if (stmt != null)
                    {
                        stmt.close();
                    }
                } catch (Exception ex) {}
            }
        }
        return false;
        
        /*
        // This is not used right NOW!
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
 
            SpTaskSemaphore semaphore = getSemaphore(session, name, scope, discipline, collection);
            
            if (semaphore == null)
            {
                //throw new RuntimeException("lock ["+title+"] didn't exist");
                return false;
            }
            return semaphore.getIsLocked();
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, ex);
            ex.printStackTrace();
            //log.error(ex);
            
        } finally 
        {
             if (session != null)
             {
                 session.close();
             }
        }
        throw new RuntimeException("Error checking lock ["+title+"]");   
        */
    }

    /**
     * Find the semaphore for a task and return it..
     * @param title The human (localized) title of the task 
     * @param name the unique name
     * @param scope the scope of the lock
     * @return the semaphore or null if no matching semaphore exists.
     */
    protected static SpTaskSemaphore getLockInfo(final String title, 
                                                 final String name, 
                                                 final SCOPE  scope)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            Discipline discipline = scope == SCOPE.Discipline ? AppContextMgr.getInstance().getClassObject(Discipline.class) : null;
            Collection collection = scope == SCOPE.Collection ? AppContextMgr.getInstance().getClassObject(Collection.class) : null;

            SpTaskSemaphore semaphore = getSemaphore(session, name, scope, discipline, collection);
            
            return semaphore;
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, ex);
            ex.printStackTrace();
            //log.error(ex);
            
        } finally 
        {
             if (session != null)
             {
                 session.close();
             }
        }
        throw new RuntimeException("Error getting lock info ["+title+"]");   
    }

    /**
     * @param title
     * @param name
     * @param scope
     * @return
     */
    public static boolean askUserToUnlock(final String title, 
                                           final String name, 
                                           final SCOPE  scope)
    {
        SpecifyUser user      = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        Discipline discipline = scope == SCOPE.Discipline ? AppContextMgr.getInstance().getClassObject(Discipline.class) : null;
        Collection collection = scope == SCOPE.Collection ? AppContextMgr.getInstance().getClassObject(Collection.class) : null;

        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            SpTaskSemaphore semaphore = null;
            try
            {
                semaphore = getSemaphore(session, name, scope, discipline, collection);
            
            } catch (StaleObjectException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, ex);
                semaphore = null;
            }
            
            if (semaphore == null)
            {
                // can't be unlocked at this time.
                // or it isn't locked
                
            } else
            {
                if (!semaphore.getIsLocked())
                {
                    return false;
                }
                
                // Check to see if we have the same user on the same machine.
                String currMachineName = InetAddress.getLocalHost().toString();
                String dbMachineName   = semaphore.getMachineName();
                
                if (StringUtils.isNotEmpty(dbMachineName) && 
                    StringUtils.isNotEmpty(currMachineName) && 
                    currMachineName.equals(dbMachineName) &&
                    semaphore.getOwner() != null && 
                    user != null && 
                    user.getId().equals(semaphore.getOwner().getId()))
                {
                    // In use by this user
                    
                    int      options      = JOptionPane.YES_NO_OPTION;
                    Object[] optionLabels = new String[] { getResourceString("SpTaskSemaphore.OVERRIDE"),  //$NON-NLS-1$
                                                           getResourceString("CANCEL")//$NON-NLS-1$
                                                         };
                    int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                            getLocalizedMessage("SpTaskSemaphore.IN_USE_BY_YOU_UNLK", title, title),
                            getResourceString("SpTaskSemaphore.IN_USE_TITLE"),  //$NON-NLS-1$
                            options,
                            JOptionPane.QUESTION_MESSAGE, null, optionLabels, 1);
                    
                    return userChoice == JOptionPane.YES_OPTION;
                }
                
                String userStr = prevLockedBy != null ? prevLockedBy : semaphore.getOwner().getIdentityTitle();
                String msg = UIRegistry.getLocalizedMessage("SpTaskSemaphore.IN_USE_OV_UNLK", title, userStr, semaphore.getLockedTime().toString());
                
                int      options;
                Object[] optionLabels;
                    options = JOptionPane.YES_NO_OPTION;
                    optionLabels = new String[] { getResourceString("SpTaskSemaphore.OVERRIDE"),  //$NON-NLS-1$
                                                  getResourceString("CANCEL"),  //$NON-NLS-1$
                                                };
                
                int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                             msg,
                                                             getResourceString("SpTaskSemaphore.IN_USE_TITLE"),  //$NON-NLS-1$
                                                             options,
                                                             JOptionPane.QUESTION_MESSAGE, null, optionLabels, 1);
                return userChoice == JOptionPane.YES_OPTION;
            }

                
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, ex);
            ex.printStackTrace();
            //log.error(ex);
            
        } finally 
        {
             if (session != null)
             {
                 session.close();
             }
        }

        return false;
    }
    
    /**
     * Unlocks the semaphore.
     * @param title The human (localized) title of the task 
     * @param name the unique name
     * @param scope the scope of the lock
     * @return true if it was unlocked
     */
    public static boolean unlock(final String title, 
                                 final String name, 
                                 final SCOPE  scope)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            int     count = 0;
            boolean lockWasRemoved = false;
            do {
                try
                {
                    lockWasRemoved = setLock(session, name, null, scope, false, false) != null;
                    if (lockWasRemoved)
                    {
                        break;
                    }
                } catch (StaleObjectException ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, ex);
                    lockWasRemoved = false;
                }
                
                count++;
                
            } while (count < 3);
            
            if (lockWasRemoved)
            {
                return true;
            }
            throw new RuntimeException("Couldn't unlock.");
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, ex);
            ex.printStackTrace();
            //log.error(ex);
            
        } finally 
        {
             if (session != null)
             {
                 session.close();
             }
        }
        return false;
    }
    
    /**
     * @param title
     * @param name
     * @param context
     * @param scope
     * @param allViewMode
     * @return
     */
    public static USER_ACTION lock(final String title, 
                                   final String name, 
                                   final String context,
                                   final SCOPE  scope,
                                   final boolean allViewMode)
    {
        return lock(title, name, context, scope, allViewMode, null);
    }
    
    /**
     * Locks the semaphore.
     * @param title The human (localized) title of the task 
     * @param name the unique name
     * @param context
     * @param scope the scope of the lock
     * @param allViewMode allows it to ask the user about 'View Only'
     * @return
     */
    public static USER_ACTION lock(final String title, 
                                   final String name, 
                                   final String context,
                                   final SCOPE  scope,
                                   final boolean allViewMode,
                                   final TaskSemaphoreMgrCallerIFace caller)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            int count = 0;
            do {
                
                SpTaskSemaphore semaphore = null;
                try
                {
                    semaphore = setLock(session, name, context, scope, true, false);
                
                } catch (StaleObjectException ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, ex);
                    semaphore = null;
                }
                
                if (semaphore == null || previouslyLocked)
                {
                    if (caller != null)
                    {
                        return caller.resolveConflict(semaphore, previouslyLocked, prevLockedBy);
                    }
                    
                    if (semaphore == null)
                    {
                        String msg = UIRegistry.getLocalizedMessage("SpTaskSemaphore.IN_USE", title);//$NON-NLS-1$
                        Object[] options = { getResourceString("SpTaskSemaphore.TRYAGAIN"),  //$NON-NLS-1$
                                             getResourceString("CANCEL")  //$NON-NLS-1$
                                           };
                        int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                                     msg,
                                                                     getResourceString("SpTaskSemaphore.IN_USE_TITLE"),  //$NON-NLS-1$
                                                                     JOptionPane.YES_NO_OPTION,
                                                                     JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                        if (userChoice == JOptionPane.NO_OPTION)
                        {
                            return USER_ACTION.Cancel;
                        }
                        
                    } else
                    {
                        // Check to see if we have the same user on the same machine.
                        SpecifyUser user            = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
                        String      currMachineName = InetAddress.getLocalHost().toString();
                        String      dbMachineName   = semaphore.getMachineName();
                        
                        //System.err.println("["+dbMachineName+"]["+currMachineName+"]["+user.getId()+"]["+semaphore.getOwner().getId()+"]");
                        if (StringUtils.isNotEmpty(dbMachineName) && StringUtils.isNotEmpty(currMachineName) && currMachineName.equals(dbMachineName) &&
                            semaphore.getOwner() != null && user != null && user.getId().equals(semaphore.getOwner().getId()))
                        {
                            if (allViewMode)
                            {
                                int      options      = JOptionPane.YES_NO_OPTION;
                                Object[] optionLabels = new String[] { getResourceString("SpTaskSemaphore.VIEWMODE"),  //$NON-NLS-1$
                                                                       getResourceString("CANCEL")//$NON-NLS-1$
                                                                     };
                                int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                        getLocalizedMessage("SpTaskSemaphore.IN_USE_BY_YOU", title),
                                        getResourceString("SpTaskSemaphore.IN_USE_TITLE"),  //$NON-NLS-1$
                                        options,
                                        JOptionPane.QUESTION_MESSAGE, null, optionLabels, 0);
                                
                                return userChoice == JOptionPane.NO_OPTION ? USER_ACTION.Cancel : USER_ACTION.ViewMode; // CHECKED
                            }
                            
                            int      options      = JOptionPane.OK_OPTION;
                            Object[] optionLabels = new String[] { getResourceString("OK")//$NON-NLS-1$
                                                                 };
                            JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                    getLocalizedMessage("SpTaskSemaphore.IN_USE_BY_YOU", title),
                                    getResourceString("SpTaskSemaphore.IN_USE_TITLE"),  //$NON-NLS-1$
                                    options,
                                    JOptionPane.QUESTION_MESSAGE, null, optionLabels, 0);
                            
                            return USER_ACTION.Cancel;
                        }
                        
                        String userStr = prevLockedBy != null ? prevLockedBy : semaphore.getOwner().getIdentityTitle();
                        String msg = UIRegistry.getLocalizedMessage("SpTaskSemaphore.IN_USE_OV", title, userStr, semaphore.getLockedTime().toString());
                        
                        int      options;
                        int      defBtn;
                        Object[] optionLabels;
                        if (allViewMode)
                        {
                            defBtn = 2;
                            options = JOptionPane.YES_NO_CANCEL_OPTION;
                            optionLabels = new String[] { getResourceString("SpTaskSemaphore.VIEWMODE"),  //$NON-NLS-1$
                                                          getResourceString("SpTaskSemaphore.TRYAGAIN"),  //$NON-NLS-1$
                                                          getResourceString("CANCEL")//$NON-NLS-1$
                                                        };
                        } else
                        {
                            defBtn = 0;
                            options = JOptionPane.YES_NO_OPTION;
                            optionLabels = new String[] {
                                    getResourceString("SpTaskSemaphore.TRYAGAIN"), //$NON-NLS-1$
                                    getResourceString("CANCEL"),  //$NON-NLS-1$
                                    };   
                        }
                        
                        int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                                     msg,
                                                                     getResourceString("SpTaskSemaphore.IN_USE_TITLE"),  //$NON-NLS-1$
                                                                     options,
                                                                     JOptionPane.QUESTION_MESSAGE, null, optionLabels, defBtn);
                        if (userChoice == JOptionPane.YES_OPTION)
                        {
                            if (options == JOptionPane.YES_NO_CANCEL_OPTION)
                            {
                                return USER_ACTION.ViewMode; // CHECKED
                            }
                            // this means try again
                            
                        } else if (userChoice == JOptionPane.NO_OPTION)
                        {
                            if (options == JOptionPane.YES_NO_OPTION)
                            {
                                return USER_ACTION.Cancel;
                            }
                         // CHECKED
                            
                        } else if (userChoice == JOptionPane.CANCEL_OPTION)
                        {
                            return USER_ACTION.Cancel; // CHECKED
                        }
                    }
                } else
                {
                    return USER_ACTION.OK;
                }
                
                count++;
                
            } while (true);
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, ex);
            ex.printStackTrace();
            //log.error(ex);
            
        } finally 
        {
             if (session != null)
             {
                 session.close();
             }
        }
        return USER_ACTION.Error;
    }
    
    /**
     * Builds the SQL string needed for checking the semaphore.
     * @param name the unique name
     * @param scope the scope of the lock
     * @param specifyUser
     * @param discipline
     * @param collection
     * @return
     */
    private static String buildHQL(final String name, 
                                   final SCOPE  scope,
                                   final Discipline discipline,
                                   final Collection collection)
    {
        StringBuilder joins = new StringBuilder();
        StringBuilder where = new StringBuilder();
        
        if (discipline != null)
        {
            where.append(" AND d.disciplineId = ");
            where.append(discipline.getId());
            joins.append("INNER JOIN ts.discipline d ");
            
        } else if (collection != null)
        {
            where.append(" AND ts.collectionId = ");
            where.append(collection.getId());
            joins.append("INNER JOIN ts.collection c ");
        }
        
        //where.append(" AND spu.specifyUserId = ");
        //where.append(specifyUser.getId());
        //joins.append("INNER JOIN ts.owner spu ");
        
        StringBuilder sb = new StringBuilder("FROM SpTaskSemaphore ts ");
        sb.append(joins);
        String wStr = String.format("WHERE taskName = '%s' AND scope = %d ", 
                                     name, scope.ordinal());
        sb.append(wStr);
        sb.append(where);
        
        return sb.toString();
    }
    
    /**
     * Builds the SQL string needed for checking the semaphore.
     * @param name the unique name
     * @param scope the scope of the lock
     * @param specifyUser
     * @param discipline
     * @param collection
     * @return
     */
    private static String buildSQL(final String name, 
                                   final SCOPE  scope,
                                   final Discipline discipline,
                                   final Collection collection)
    {
        StringBuilder joins = new StringBuilder();
        StringBuilder where = new StringBuilder();
        
        if (discipline != null)
        {
            where.append(" AND d.DisciplineID = ");
            where.append(discipline.getId());
            joins.append("INNER JOIN discipline d ON d.DisciplineID = ts.DisciplineID ");
            
        } else if (collection != null)
        {
            where.append(" AND ts.CollectionID = ");
            where.append(collection.getId());
            joins.append("INNER JOIN collection c ON c.CollectionID = ts.CollectionID ");
        }
        
        //where.append(" AND spu.specifyUserId = ");
        //where.append(specifyUser.getId());
        //joins.append("INNER JOIN ts.owner spu ");
        
        StringBuilder sb = new StringBuilder("SELECT IsLocked FROM sptasksemaphore ts ");
        sb.append(joins);
        String wStr = String.format("WHERE TaskName = '%s' AND Scope = %d ", 
                                     name, scope.ordinal());
        sb.append(wStr);
        sb.append(where);
        
        return sb.toString();
    }
    
    /**
     * Gets the semaphore object from the database.
     * @param session
     * @param name the unique name
     * @param scope the scope of the lock
     * @param specifyUser
     * @param discipline
     * @param collection
     * @return
     * @throws Exception
     */
    private static SpTaskSemaphore getSemaphore(final DataProviderSessionIFace session,
                                                final String name, 
                                                final SCOPE  scope,
                                                final Discipline discipline,
                                                final Collection collection) throws Exception
    {
        String sql = buildHQL(name, scope, discipline, collection);
        //System.err.println(sql);
        //Object[] cols = (Object[])session.getData(sql);
        Object data = session.getData(sql);
        if (data instanceof SpTaskSemaphore)
        {
            return (SpTaskSemaphore )data;
        }
        Object[] cols = (Object[] )data;
        return cols != null && cols.length > 0 ? (SpTaskSemaphore)cols[0] : null;
    }
    
    /**
     * @param name
     * @param scope
     * @return
     */
    public static boolean doesOwnSemaphore(final String name, 
                                           final SCOPE  scope)
    {
        SpecifyUser user = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        String sqlStr = String.format("SELECT count(*) FROM sptasksemaphore WHERE TaskName = '%s' AND Scope = %d AND OwnerID = %d AND IsLocked <> 0", 
                                      name, scope.ordinal(), user.getId());
        //System.err.println(sqlStr+"  ["+BasicSQLUtils.getCount(sqlStr)+"]");
        return BasicSQLUtils.getCount(sqlStr) > 0;
    }
    
    /**
     * Gets the semaphore and set the lock to true.
     * @param session
     * @param name the unique name
     * @param context 
     * @param scope the scope of the lock
     * @param doLock
     * @param doOverride
     * @return
     * @throws Exception
     */
    private static SpTaskSemaphore setLock(final DataProviderSessionIFace session,
                                           final String name, 
                                           final String context,
                                           final SCOPE  scope,
                                           final boolean doLock,
                                           final boolean doOverride) throws Exception
    {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        SpecifyUser user      = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        Discipline discipline = scope == SCOPE.Discipline ? AppContextMgr.getInstance().getClassObject(Discipline.class) : null;
        Collection collection = scope == SCOPE.Collection ? AppContextMgr.getInstance().getClassObject(Collection.class) : null;
        
        // Get our own copies of the Global Objects.
        user       = user       != null ? session.getData(SpecifyUser.class, "id", user.getId(), DataProviderSessionIFace.CompareType.Equals) : null;
        discipline = discipline != null ? session.getData(Discipline.class, "id", discipline.getId(), DataProviderSessionIFace.CompareType.Equals) : null;
        collection = collection != null ? session.getData(Collection.class, "id", collection.getId(), DataProviderSessionIFace.CompareType.Equals) : null;

        SpTaskSemaphore semaphore = getSemaphore(session, name, scope, discipline, collection);
        
        if (semaphore != null)
        {
            if (doLock)
            {
                if (semaphore.getIsLocked() && !doOverride)
                {
                    previouslyLocked = true;
                    prevLockedBy     = semaphore.getOwner().getAgents().iterator().next().getIdentityTitle();
                    return semaphore;
                }
            } else if (!semaphore.getIsLocked())
            {
                //throw new RuntimeException("Trying to unlock when already unlocked!");
                System.err.println("Trying to unlock when already unlocked!");
            }
            
            previouslyLocked = semaphore.getIsLocked();
            
        } else if (doLock)
        {
            semaphore = new SpTaskSemaphore();
            semaphore.initialize();
            semaphore.setTaskName(name);
            semaphore.setTimestampCreated(now);
            //user.addReference(semaphore, "taskSemaphores");
            semaphore.setOwner(user);
            previouslyLocked = false;
            
        } else
        {
            // Changing to just an error message for Bug 6478
            // I think it was a timing issue. I wasn't able to
            // reproduce it.
            log.error("Try to unlock when there is no lock.");
            // error
            //throw new RuntimeException("No lock!");
        }
        
        //if (semaphore != null)
        {
            semaphore.setIsLocked(doLock);
            semaphore.setContext(context);
            String machineName = InetAddress.getLocalHost().toString();
            machineName =  StringUtils.isNotEmpty(machineName) ? machineName.substring(0, Math.min(64, machineName.length())) : null;
            semaphore.setMachineName(doLock ? machineName : null);
            semaphore.setScope(new Byte((byte)scope.ordinal()));
            semaphore.setLockedTime(now);
            semaphore.setTimestampModified(now);
            semaphore.setDiscipline(discipline);
            semaphore.setCollection(collection);
            semaphore.setOwner(user);
            
            session.beginTransaction();
            session.saveOrUpdate(semaphore);
            session.commit();
        }
            
        return semaphore;
    }
    
}
