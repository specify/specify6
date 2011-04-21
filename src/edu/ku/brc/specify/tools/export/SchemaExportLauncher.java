/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

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
import edu.ku.brc.specify.tasks.subpane.qb.QueryBldrPane;
import edu.ku.brc.specify.tools.ireportspecify.IReportLauncher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

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
        AppContextMgr.CONTEXT_STATUS status = AppContextMgr.getInstance().setContext(databaseName, userName, true, true);
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
            PermissionIFace permissions = SecurityMgr.getInstance().getPermission("Task.ExportMappingTask");
            canOpen = permissions.canView();
        }
        if (canOpen)
        {
        	openSchemaExporter();
        }
        else
        {
            JOptionPane.showMessageDialog(null, getResourceString("SchemaExportLauncher.PERMISSION_DENIED"),
                        getResourceString("SchemaExportLauncher.PERMISSION_DENIED_TITLE"),
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
                    	//XXX CollectionScoping Issue!!! Collection coll = AppContextMgr.getInstance().getClassObject(Collection.class);
                    	for (int m = maps.size() - 1; m >= 0; m--)
                    	{
                    		SpExportSchemaMapping map = maps.get(m);
                    		if (!map.getSpExportSchema().getDiscipline().getId().equals(disc.getId()))
                        	//XXX CollectionScoping Issue!!! if (!map.getCollectionMemberId().equals(coll.getId()))
                    		{
                    			maps.remove(m);
                    		}
                    		else
                    		{
                    			map.forceLoad();
                    			map.getMappings().iterator().next().getQueryField().getQuery().forceLoad();
                    			map.getSpExportSchema().forceLoad();
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
                    	final ExportPanel ep = new ExportPanel(maps);
                    	final JFrame frame = new JFrame();
                    	frame.addWindowListener(new WindowListener() {

							/* (non-Javadoc)
							 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
							 */
							@Override
							public void windowActivated(WindowEvent arg0)
							{
								// TODO Auto-generated method stub
								
							}

							/* (non-Javadoc)
							 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
							 */
							@Override
							public void windowClosed(WindowEvent arg0)
							{
								System.exit(0);
							}

							/* (non-Javadoc)
							 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
							 */
							@Override
							public void windowClosing(WindowEvent arg0)
							{
								if (ep.close())
								{
									frame.setVisible(false);
									System.exit(0);
								}
							}

							/* (non-Javadoc)
							 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
							 */
							@Override
							public void windowDeactivated(WindowEvent arg0)
							{
								// TODO Auto-generated method stub
								
							}

							/* (non-Javadoc)
							 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
							 */
							@Override
							public void windowDeiconified(WindowEvent arg0)
							{
								// TODO Auto-generated method stub
								
							}

							/* (non-Javadoc)
							 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
							 */
							@Override
							public void windowIconified(WindowEvent arg0)
							{
								// TODO Auto-generated method stub
								
							}

							/* (non-Javadoc)
							 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
							 */
							@Override
							public void windowOpened(WindowEvent arg0)
							{
								// TODO Auto-generated method stub
								
							}
                    		
                    	});
                    	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    	frame.setTitle(UIRegistry.getResourceString("SchemaExportLauncher.DlgTitle"));
                    	frame.setContentPane(ep);
                    	frame.pack();
                    	frame.setIconImage(IconManager.getImage(IconManager.makeIconName("SpecifyWhite32")).getImage());
                    	UIHelper.centerAndShow(frame);
                    }
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
