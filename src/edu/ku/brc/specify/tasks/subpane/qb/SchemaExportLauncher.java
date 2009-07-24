/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Window;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.DatabaseLoginListener;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.tasks.QueryTask;
import edu.ku.brc.specify.tools.ireportspecify.IReportLauncher;

/**
 * @author timo
 *
 */
public class SchemaExportLauncher implements DatabaseLoginListener
{
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
        AppContextMgr.CONTEXT_STATUS status = AppContextMgr.getInstance().setContext(databaseName, userName, true);
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
                System.exit(0);
            }
        
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
            //openIReportEditor();
        	openSchemaExporter();
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
    protected void openSchemaExporter() 
    {
//       SwingUtilities.invokeLater(new Runnable()
//        {
//            public void run()
//            {
                try
                {
                	TaskMgr.register(new QueryTask(), false);
                	DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    List<SpExportSchemaMapping> maps = null;
                    try
                    {
                    	maps = session.getDataList(SpExportSchemaMapping.class);
                    	Discipline disc = AppContextMgr.getInstance().getClassObject(Discipline.class);
                    	for (int m = maps.size() - 1; m >= 0; m--)
                    	{
                    		SpExportSchemaMapping map = maps.get(m);
                    		if (!map.getSpExportSchema().getDiscipline().getId().equals(disc.getId()))
                    		{
                    			maps.remove(m);
                    		}
                    		else
                    		{
                    			map.forceLoad();
                    			map.getMappings().iterator().next().getQueryField().getQuery().forceLoad();
                    		}
                    	}
                    }
                    catch (Exception ex)
                    {
                        UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QueryBldrPane.class, ex);
                        ex.printStackTrace();
                        System.exit(1);
                    }
                    finally
                    {
                    	session.close();
                    }
                    if (maps != null)
                    {
                    	if (maps.size() == 0)
                    	{
                            JOptionPane.showMessageDialog(null, getResourceString("SchemaExportLauncher.NoExportSchemaFound"),
                                    getResourceString("SchemaExportLauncher.NoExportSchemaFoundTitle"),
                                    JOptionPane.ERROR_MESSAGE);
                            System.exit(0);
                    	}
                    	ExportPanel ep = new ExportPanel(maps);
                    	JFrame frame = new JFrame();
                    	frame.setContentPane(ep);
                    	frame.setVisible(true);
//                    	CustomDialog cd = new CustomDialog((Frame )UIRegistry.getTopWindow(), 
//                    			getResourceString("SchemaExportLauncher.DlgTitle"), true,
//                    			ep);
//                    	UIHelper.centerAndShow(cd);
                    }
                	//System.exit(0);
                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IReportLauncher.class, e);
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
//            }
//        });
    }
}
