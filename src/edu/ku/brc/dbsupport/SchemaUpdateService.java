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
import java.sql.Connection;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

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
public abstract class SchemaUpdateService
{
    public static final String factoryName = "edu.ku.brc.af.core.db.SchmeaUpdateService"; //$NON-NLS-1$
    
    public enum CONTEXT_STATUS {OK, Error, Ignore, Initial}
    
    protected static SchemaUpdateService instance = null;
    
    /**
     * @return
     */
    protected DBConnection getITConnection()
    {
        JTextField     userNameTF = new JTextField(15);
        JPasswordField passwordTF = new JPasswordField();
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,p", "p,4px,p"));
        
        pb.add(UIHelper.createI18NFormLabel("username"), cc.xy(1, 1));
        pb.add(userNameTF, cc.xy(3, 1));
        
        pb.add(UIHelper.createI18NFormLabel("password"), cc.xy(1, 3));
        pb.add(passwordTF, cc.xy(3, 3));
        
        pb.setDefaultDialogBorder();
        
        CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), "", true, pb.getPanel());
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            DBConnection dbc    = DBConnection.getInstance();
            DBConnection dbConn = new DBConnection(userNameTF.getText(), new String(passwordTF.getPassword()),  dbc.getConnectionStr(), dbc.getDriver(), dbc.getDialect(), dbc.getDatabaseName());
            try
            {
                Connection conn = dbConn.createConnection();
                conn.close();
                
                dbConn.setServerName(dbc.getServerName());
                return dbConn;
                
            } catch (Exception ex)
            {
                
            }
        }
        return null;
    }
    
    /**
     * Returns a View by name, meaning a ViewSet name and a View name inside the ViewSet.
     * @param viewName the name of the view (cannot be null)
     * @return the view
     */
    public abstract boolean updateSchema();
    
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