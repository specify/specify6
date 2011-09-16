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
package edu.ku.brc.specify.tools.ireportspecify;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import it.businesslogic.ireport.gui.MainFrame;

import java.awt.Window;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.DatabaseLoginListener;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class IReportLauncher implements DatabaseLoginListener
{
    //iReport MainFrame
    protected static MainFrameSpecify iReportMainFrame   = null;

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#cancelled()
     */
    public void cancelled()
    {
        System.exit(0);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#loggedIn(java.awt.Window, java.lang.String, java.lang.String)
     */
    public void loggedIn(Window window, String databaseName, String userName)
    {
        //System.out.println("Yup. You logged in.");

        //snatched from Specify.restartApp...
        AppPreferences.shutdownRemotePrefs();
        
        if (window != null)
        {
            window.setVisible(false);
        }
        
        //moved here because context needs to be set before loading prefs, we need to know the SpecifyUser
        AppContextMgr.CONTEXT_STATUS status = AppContextMgr.getInstance().setContext(databaseName, userName, true, true, true);
       // AppContextMgr.getInstance().
        SpecifyAppPrefs.initialPrefs();
        
        if (status == AppContextMgr.CONTEXT_STATUS.OK)
        {
            if (AppContextMgr.getInstance().getClassObject(Discipline.class) == null)
            {
                return;
            }
            
            int disciplineeId = AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId();
            SchemaI18NService.getInstance().loadWithLocale(SpLocaleContainer.CORE_SCHEMA, disciplineeId, DBTableIdMgr.getInstance(), Locale.getDefault());
        } else if (status == AppContextMgr.CONTEXT_STATUS.Error)
        {
            if (AppContextMgr.getInstance().getClassObject(Collection.class) == null)
            {
                
                // TODO This is really bad because there is a Database Login with no Specify login
                JOptionPane.showMessageDialog(null, 
                                              getResourceString("Specify.LOGIN_USER_MISMATCH"),  //$NON-NLS-1$
                                              getResourceString("Specify.LOGIN_USER_MISMATCH_TITLE"),  //$NON-NLS-1$
                                              JOptionPane.ERROR_MESSAGE);
            }
            System.exit(0);
        }
        //...end specify.restartApp snatch
        
        boolean canOpen = true;
        if (AppContextMgr.isSecurityOn())
        {
            PermissionIFace permissions = SecurityMgr.getInstance().getPermission("Task.Reports");
            canOpen = permissions.canView();
        }
        if (canOpen)
        {
            openIReportEditor();
        }
        else
        {
            JOptionPane.showMessageDialog(null, getResourceString("IReportLauncher.PERMISSION_DENIED"),
                        getResourceString("IReportLauncher.PERMISSION_DENIED_TITLE"),
                        JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    
    /**
     * 
     */
    protected void openIReportEditor() 
    {
       SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    if (iReportMainFrame == null)
                    {
                        MainFrame.reportClassLoader.rescanLibDirectory();
                        Thread.currentThread().setContextClassLoader(MainFrame.reportClassLoader);
                        iReportMainFrame = new MainFrameSpecify(MainFrameSpecify.getDefaultArgs(), false, false);
                    }
                    iReportMainFrame.refreshSpJRConnections();
                    iReportMainFrame.setVisible(true);
                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IReportLauncher.class, e);
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        });
    }
        
}

