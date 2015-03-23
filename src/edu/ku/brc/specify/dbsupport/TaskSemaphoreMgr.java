/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.dbsupport;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
       
        Connection connection = DBConnection.getInstance().getConnection();
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
     * Checks IsLocked and UsageCount for the specified semaphore and returns 
     * true if it is locked or the UsageCount is non-null and non-zero.
     * 
     * @param title
     * @param name
     * @param scope
     * @return true if UsageCount is > 0.
     */
    public static boolean isLockedOrInUse(final String title, 
            final String name, 
            final SCOPE  scope)
    {
        Discipline discipline = scope == SCOPE.Discipline ? AppContextMgr.getInstance().getClassObject(Discipline.class) : null;
        Collection collection = scope == SCOPE.Collection ? AppContextMgr.getInstance().getClassObject(Collection.class) : null;
       
        Connection connection = DBConnection.getInstance().getConnection();
        if (connection != null)
        {
            Statement  stmt = null;
            ResultSet  rs   = null;
            try
            {
                String sql = buildSQL(name, scope, discipline, collection, "IsLocked, UsageCount");
                //log.debug(sql);
                
                stmt = connection.createStatement();
                rs = stmt.executeQuery(sql);
                if (rs != null && rs.next())
                {
                	Integer count = rs.getInt(2);
                	return rs.getBoolean(1) || (count == null ? false : count > 0);
                }
                return false;
                
            } catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException ex)
            {
                UIRegistry.showLocalizedMsg("TIMEOUT_ERR");
                
            } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException ex)
            {
                UIRegistry.showLocalizedMsg("TIMEOUT_ERR");
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, ex);
                log.error(ex);
                
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
//                    if (connection != null)
//                    {
//                        connection.close();
//                    }
                } catch (Exception ex) 
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, ex);
                    log.error(ex);
                }
            }
        }
        return false;
    }
    /**
     * Find the semaphore for a task and return it..
     * @param title The human (localized) title of the task 
     * @param name the unique name
     * @param scope the scope of the lock
     * @return the semaphore or null if no matching semaphore exists.
     */
    public static SpTaskSemaphore getLockInfo(final String title, 
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
            
            if (semaphore != null && semaphore.getOwner() != null && semaphore.getOwner().getAgents() != null)
            {
                semaphore.getOwner().getAgents().size(); // force Load
            }
            
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
                if (!semaphore.getIsLocked() && !(semaphore.getUsageCount() != null && semaphore.getUsageCount() > 0))
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
                String msg = UIRegistry.getLocalizedMessage("SpTaskSemaphore.IN_USE_OV_UNLK", title, userStr, 
                		semaphore.getLockedTime() == null ? "?" : semaphore.getLockedTime().toString());
                
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
                    lockWasRemoved = setLock(session, name, null, scope, false, false, false) != null;
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
            //throw new RuntimeException("Couldn't unlock.");
            return false;
            
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
        return lock(title, name, context, scope, allViewMode, null, false);
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
                                   final TaskSemaphoreMgrCallerIFace caller,
                                   final boolean checkUsage)
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
                    semaphore = setLock(session, name, context, scope, true, false, checkUsage);
                
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
                        String msgKey = allViewMode ? "SpTaskSemaphore.IN_USE_OV" : "SpTaskSemaphore.IN_USE";
                        String msg = UIRegistry.getLocalizedMessage(msgKey, title, userStr, 
                        		semaphore.getLockedTime() != null ? semaphore.getLockedTime().toString() : "");
                        
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
     * @param title
     * @param name
     * @param scope
     * @return true if successful.
     * 
     * Adds 1 to the usage count for the specified semaphore.
     */
    public static boolean incrementUsageCount(final String title, final String name, final SCOPE  scope)
    {
    	return updateUsageCount(title, name, scope, 1);
    }
    
    /**
     * @param title
     * @param name
     * @param scope
     * @return true if successful
     * 
     * Subtracts 1 from the usage count for the specified semaphore.
     */
    public static boolean decrementUsageCount(final String title, final String name, final SCOPE  scope)
    {
    	return updateUsageCount(title, name, scope, -1);
    }

    /**
     * @param title
     * @param name
     * @param scope
     * @return true if successful.
     * 
     * sets usage count for the specified semaphore to null.
     */
    public static boolean clearUsageCount(final String title, final String name, final SCOPE  scope)
    {
    	return updateUsageCount(title, name, scope, null);
    }

    /**
     * @param title
     * @param name
     * @param scope
     * @param increment
     * @return true if usage count is successfully incremented.
     * 
     * Adds increment (can be a negative number) to specified semaphore.
     * 
     */
    private static boolean updateUsageCount(final String title, final String name, final SCOPE  scope, final Integer increment)
    {
    	if (isLocked(title, name, scope))
    	{
    		return false;
    	}
    	
    	boolean result = false;
    	boolean inTransaction = false;
    	DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
    	try
    	{
            Timestamp now = new Timestamp(System.currentTimeMillis());
            
            SpecifyUser user      = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
            Discipline discipline = scope == SCOPE.Discipline ? AppContextMgr.getInstance().getClassObject(Discipline.class) : null;
            Collection collection = scope == SCOPE.Collection ? AppContextMgr.getInstance().getClassObject(Collection.class) : null;
            
            // Get our own copies of the Global Objects.
            user       = user       != null ? session.getData(SpecifyUser.class, "id", user.getId(), DataProviderSessionIFace.CompareType.Equals) : null;
            discipline = discipline != null ? session.getData(Discipline.class, "id", discipline.getId(), DataProviderSessionIFace.CompareType.Equals) : null;
            collection = collection != null ? session.getData(Collection.class, "id", collection.getId(), DataProviderSessionIFace.CompareType.Equals) : null;
    		session.beginTransaction();
    		inTransaction = true;
            SpTaskSemaphore semaphore = getSemaphore(session, name, scope, discipline, collection);
            if (semaphore == null)
            {
            	semaphore = new SpTaskSemaphore();
                semaphore = new SpTaskSemaphore();
                semaphore.initialize();
                semaphore.setTaskName(name);
                semaphore.setTimestampCreated(now);
                semaphore.setOwner(user);
                semaphore.setScope((byte )scope.ordinal());
                semaphore.setDiscipline(discipline);
                semaphore.setCollection(collection);
            }
    		Integer count = semaphore.getUsageCount() == null ? 0 : semaphore.getUsageCount();
    		if (increment == null)
    		{
    			semaphore.setUsageCount(null);
    		}
    		else
    		{
     			Integer newCount = new Integer(count + increment);
    			if (newCount.intValue() < 0)
    			{
    				log.error("attempt to set usage count for " + name + "to " + newCount);
    				newCount = 0;
    			}
    			semaphore.setUsageCount(newCount);
    		}
    		session.saveOrUpdate(semaphore);
    		session.commit();
    		result = true;
    	}
    	catch (Exception ex)
    	{
            if (inTransaction)
            {
            	session.rollback();
            }
    		edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskSemaphoreMgr.class, ex);
            ex.printStackTrace();
    	}
    	finally
    	{
    		session.close();
    	}
    	return result;
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
        return buildSQL(name, scope, discipline, collection, "IsLocked");
    }

    /**
    * Builds the SQL string needed for checking the semaphore.
     * @param name
     * @param scope
     * @param discipline
     * @param collection
     * @param fldsToSelect 
     * @return
     */
    private static String buildSQL(final String name, 
            final SCOPE  scope,
            final Discipline discipline,
            final Collection collection,
            final String fldsToSelect)
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
        
        StringBuilder sb = new StringBuilder("SELECT " + fldsToSelect + " FROM sptasksemaphore ts ");
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
                                           final boolean doOverride,
                                           final boolean checkUsage) throws Exception
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
            boolean locked = semaphore.getIsLocked() || 
        		(checkUsage && semaphore.getUsageCount() != null && semaphore.getUsageCount() > 0); 
            if (doLock)
            {
            	if (locked && !doOverride)
                {
                    previouslyLocked = true;
                    if (semaphore.getOwner() != null && 
                        semaphore.getOwner().getAgents() != null &&
                        semaphore.getOwner().getAgents().size() > 0)
                    {
                        prevLockedBy = semaphore.getOwner().getAgents().iterator().next().getIdentityTitle();
                    } else
                    {
                        prevLockedBy = null;
                    }
                    return semaphore;
                }
            } else if (!semaphore.getIsLocked())
            {
                //throw new RuntimeException("Trying to unlock when already unlocked!");
                log.error("Trying to unlock when already unlocked!");
            }
            
            previouslyLocked = locked;
            
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
            return null; 
            // error
            //throw new RuntimeException("No lock!");
        }
        
        //if (semaphore != null)
        {
            semaphore.setIsLocked(doLock);
            semaphore.setContext(context);
            String machineName = "";
            try
            {
            	machineName = InetAddress.getLocalHost().toString();
            } catch (UnknownHostException ex)
            {
            	//no internet connection. ignore.
            }
            
            machineName =  StringUtils.isNotEmpty(machineName) ? machineName.substring(0, Math.min(64, machineName.length())) : null;
            semaphore.setMachineName(doLock ? machineName : null);
            semaphore.setScope(new Byte((byte)scope.ordinal()));
            semaphore.setLockedTime(now);
            semaphore.setUsageCount(null);
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
