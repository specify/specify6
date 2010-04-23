/* Copyright (C) 2009, University of Kansas Center for Research
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

import java.awt.Frame;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

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
    
    public enum SchemaUpdateType {Success, SuccessSilent, Error, NotNeeded}
    
    public enum CONTEXT_STATUS {OK, Error, Ignore, Initial}
    
    protected Vector<String> errMsgList = new Vector<String>();

    protected static SchemaUpdateService instance = null;
    
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
     * Returns a View by name, meaning a ViewSet name and a View name inside the ViewSet.
     * @param viewName the name of the view (cannot be null)
     * @param versionNumber the current version number of the application
     * @return the view
     */
    public abstract SchemaUpdateType updateSchema(String versionNumber);
    
    
    /**
     * @return a string with the version number for the database schema
     */
    public abstract String getDBSchemaVersionFromXML();
    
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