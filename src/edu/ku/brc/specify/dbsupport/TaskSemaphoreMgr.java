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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.net.InetAddress;
import java.sql.Timestamp;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
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
    public enum SCOPE {Global, Discipline, Collection}
    
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
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            Discipline discipline = scope == SCOPE.Discipline ? AppContextMgr.getInstance().getClassObject(Discipline.class) : null;
            Collection collection = scope == SCOPE.Collection ? AppContextMgr.getInstance().getClassObject(Collection.class) : null;

            SpTaskSemaphore semaphore = getSemaphore(session, name, scope, discipline, collection);
            
            if (semaphore == null)
            {
                //throw new RuntimeException("lock ["+title+"] didn't exist");
                return false;
            }
            return semaphore.getIsLocked();
            
        } catch (Exception ex)
        {
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
     * Locks the semaphore.
     * @param title The human (localized) title of the task 
     * @param name the unique name
     * @param context
     * @param scope the scope of the lock
     * @param allowOverride allows the user to override the lock
     * @return
     */
    public static boolean lock(final String title, 
                               final String name, 
                               final String context,
                               final SCOPE  scope,
                               final boolean allowOverride)
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
                    semaphore = null;
                }
                
                if (semaphore == null || previouslyLocked)
                {
                    if (semaphore == null)
                    {
                        String msg = UIRegistry.getLocalizedMessage("SpTaskSemaphore.IN_USE", title);//$NON-NLS-1$
                        Object[] options = { getResourceString("SpTaskSemaphore.TRYAGAIN"),  //$NON-NLS-1$
                                getResourceString("NO")  //$NON-NLS-1$
                              };
                        int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                                     msg,
                                                                     getResourceString("SpTaskSemaphore.IN_USE_TITLE"),  //$NON-NLS-1$
                                                                     JOptionPane.YES_NO_CANCEL_OPTION,
                                                                     JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                        if (userChoice == JOptionPane.NO_OPTION)
                        {
                            return false;
                        }
                        
                    } else
                    {
                        // Check to see if we have the same user on the same machine.
                        SpecifyUser user            = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
                        String      currMachineName = InetAddress.getLocalHost().toString();
                        String      dbMachineName   = semaphore.getMachineName();
                        if (StringUtils.isNotEmpty(dbMachineName) && StringUtils.isNotEmpty(currMachineName) && currMachineName.equals(dbMachineName) &&
                            semaphore.getOwner() != null && user != null && user.getId().equals(semaphore.getOwner().getId()))
                        {
                            String msg = UIRegistry.getLocalizedMessage("SpTaskSemaphore.IN_USE_BY_YOU", title);//$NON-NLS-1$
                            Object[] options = { getResourceString("SpTaskSemaphore.OVERRIDE"),  //$NON-NLS-1$
                                                 getResourceString("NO")  //$NON-NLS-1$
                                  };
                            int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                                         msg,
                                                                         getResourceString("SpTaskSemaphore.IN_USE_TITLE"),  //$NON-NLS-1$
                                                                         JOptionPane.YES_NO_CANCEL_OPTION,
                                                                         JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                            return userChoice == JOptionPane.YES_OPTION;
                            
                        }
                        
                        String userStr = prevLockedBy != null ? prevLockedBy : semaphore.getOwner().getIdentityTitle();
                        String msg = UIRegistry.getLocalizedMessage("SpTaskSemaphore.IN_USE_OV", title, userStr, semaphore.getLockedTime().toString());
                        
                        int      options;
                        int      defBtn;
                        Object[] optionLabels;
                        if (allowOverride)
                        {
                            defBtn = 2;
                            options = JOptionPane.YES_NO_CANCEL_OPTION;
                            optionLabels = new String[] { getResourceString("SpTaskSemaphore.OVERRIDE"),  //$NON-NLS-1$
                                    getResourceString("NO"),  //$NON-NLS-1$
                                    getResourceString("SpTaskSemaphore.TRYAGAIN")//$NON-NLS-1$
                                    };
                        } else
                        {
                            defBtn = 0;
                            options = JOptionPane.YES_NO_OPTION;
                            optionLabels = new String[] {
                                    getResourceString("NO"),  //$NON-NLS-1$
                                    getResourceString("SpTaskSemaphore.TRYAGAIN"), //$NON-NLS-1$
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
                                try
                                {
                                    return setLock(session, name, context, scope, true, true) != null;
                                
                                } catch (StaleObjectException ex)
                                {
                                    return false;
                                }
                            }
                            return false;
                                
                        } else if (userChoice == JOptionPane.NO_OPTION && options == JOptionPane.YES_NO_CANCEL_OPTION)
                        {
                            return false;
                        }
                    }
                } else
                {
                    return true;
                }
                
                count++;
                
            } while (true);
            
        } catch (Exception ex)
        {
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
        String sql = buildSQL(name, scope, discipline, collection);
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
                throw new RuntimeException("Trying to unlock when already unlocked!");
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
            // error
            throw new RuntimeException("No lock!");
        }
        
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
            
        return semaphore;
    }
    
}
