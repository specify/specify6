/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.dbsupport;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import edu.ku.brc.helpers.XMLHelper;

/**
 * Abstract class for setting application context. It is designed that each application should implement its own.<br>
 * <br>
 * CONTEXT_STATUS is passed back and has the following meaning:<br>
 * <UL>
 * <LI>OK - The context was set correctly
 * <LI>Error - there was an error setting the context
 * <LI>Ignore - The context was not "reset" to a different value and caller should act as if the call didn't happen.
 * (Basbically a user action caused it to be abort, but it was OK)
 * <LI>Initial - This should never be passed outside to the caller, it is intended as a start up state for the object.
 * </UL>
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 24, 2009
 *
 */
public abstract class SchemaUpdateService
{
    public static final String factoryName = "edu.ku.brc.af.core.db.SchmeaUpdateService"; //$NON-NLS-1$
    
    public enum SchemaUpdateType {Success, SuccessAppVer, SuccessSilent, Error, NotNeeded}
    
    public enum CONTEXT_STATUS {OK, Error, Ignore, Initial}
    
    protected Vector<String> errMsgList = new Vector<String>();

    protected static SchemaUpdateService instance = null;
    
    protected boolean appCanUpdateSchema = true; 
    
    /**
     * 
     */
    public SchemaUpdateService()
    {
        super();
    }

    /**
     * @return the errMsgList
     */
    public Vector<String> getErrMsgList()
    {
        return errMsgList;
    }
    
    /**
     * Check to see if the DB Schema needs to be updated
     * @param versionNumber the current version number of the application
     * @param username the user logging in
     * @return
     */
    public abstract SchemaUpdateType updateSchema(String versionNumber, String username);
    
    /**
     * @param value
     * 
     * 
     */
    public void setAppCanUpdateSchema(boolean value) {
    	this.appCanUpdateSchema = value;
    }
    
    /**
     * @return
     */
    public boolean getAppCanUpdateSchema() {
    	return this.appCanUpdateSchema;
    }
    
    /**
     * @return a string with the version number for the database schema
     */
    public abstract String getDBSchemaVersionFromXML();
    
    /**
     * @param conn
     * @param fileName
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static boolean createDBTablesFromSQLFile(final Connection conn, final String fileName) throws Exception
    {
        File outFile = XMLHelper.getConfigDir(fileName);
        if (outFile != null && outFile.exists())
        {
            StringBuilder sb      = new StringBuilder();
            Statement     stmt    = conn.createStatement();
            List<?>       list    = FileUtils.readLines(outFile);
            
            for (String line : (List<String>)list)
            {
                String tLine = line.trim();
                sb.append(tLine);
                
                if (tLine.endsWith(";"))
                {
                    System.out.println(sb.toString());
                    stmt.executeUpdate(sb.toString());
                    sb.setLength(0);
                }
            }
            stmt.close();
            return true;
        }
        return false;
    }
    
    
    /**
     * Returns the instance of the AppContextMgr.
     * @return the instance of the AppContextMgr.
     */
    public static SchemaUpdateService getInstance()
    {
        if (instance != null)
        {
            return instance;
            
        }
        // else
        String factoryNameStr = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (factoryNameStr != null) 
        {
            try 
            {
                instance = (SchemaUpdateService)Class.forName(factoryNameStr).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaUpdateService.class, e);
                InternalError error = new InternalError("Can't instantiate AppContextMgr factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }
    
}