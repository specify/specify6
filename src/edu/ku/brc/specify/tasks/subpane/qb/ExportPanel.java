/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.tools.ireportspecify.MainFrameSpecify;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
@SuppressWarnings("serial")
public class ExportPanel extends JPanel implements QBDataSourceListenerIFace
{
    protected static final Logger                            log            = Logger.getLogger(ExportPanel.class);

    protected JTable mapsDisplay;
	protected DefaultTableModel mapsModel;
	protected JButton exportToDbTblBtn;
	protected JButton exportToTabDelimBtn;
	protected JLabel status;
	protected JProgressBar prog;
	protected int rowCount = 0;
	
	
	protected final List<SpExportSchemaMapping> maps;
	
	public ExportPanel(List<SpExportSchemaMapping> maps)
	{
		super();
		this.maps = maps;
		createUI();
	}
	
	public void createUI()
	{
    	buildTableModel();
		mapsDisplay = new JTable(mapsModel);
    	setLayout(new BorderLayout());
    	System.out.println();
    	for (int r = 0; r < mapsDisplay.getRowCount(); r++)
    	{
    		for (int c = 0; c < mapsDisplay.getColumnCount(); c++)
    		{
    			System.out.print(mapsDisplay.getValueAt(r, c));
    		}
    		System.out.println();
    	}
    	JScrollPane sp = new JScrollPane(mapsDisplay);
    	add(sp, BorderLayout.CENTER);
    	JPanel btnPanel = new JPanel(new BorderLayout());
    	exportToDbTblBtn = new JButton(UIRegistry.getResourceString("ExportPanel.ExportToDBTbl"));
    	exportToDbTblBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int row = mapsDisplay.getSelectedRow();
				if (row != -1)
				{
					SpExportSchemaMapping map = maps.get(row);
					SpQuery q = map.getMappings().iterator().next()
						.getQueryField().getQuery();
					Vector<QBDataSourceListenerIFace> ls = new Vector<QBDataSourceListenerIFace>();
					ls.add(ExportPanel.this);
					QueryBldrPane.exportToTable(q, ls);
				}
			}
    	});
    	status = new JLabel("ready and waiting"); //XXX i18n
    	btnPanel.add(status, BorderLayout.WEST);
    	prog = new JProgressBar();
    	btnPanel.add(prog, BorderLayout.CENTER);
    	btnPanel.add(exportToDbTblBtn, BorderLayout.EAST);
    	
    	add(btnPanel, BorderLayout.SOUTH);
	}
	
	protected void buildTableModel()
	{
		Vector<Vector<String>> data = new Vector<Vector<String>>();
		for (SpExportSchemaMapping map : maps)
		{
			Vector<String> row = new Vector<String>(2);
			row.add(map.getMappingName());
			row.add(map.getTimestampExported() != null ? map.getTimestampExported().toString() : "needs to be built");
			data.add(row);
		}
		Vector<String> headers = new Vector<String>();
		headers.add(UIRegistry.getResourceString("ExportPanel.MappingTitle"));
		headers.add(UIRegistry.getResourceString("ExportPanel.MappingExportTimeTitle"));

		mapsModel = new DefaultTableModel(data, headers);
//		for (SpExportSchemaMapping map : maps)
//		{
//			Vector<Object> row = new Vector<Object>(2);
//			row.add(map.getMappingName());
//			row.add(map.getTimestampExported() != null ? map.getTimestampExported() : "needs to be built");
//			mapsModel.addRow(row);
//		}
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#currentRow(int)
	 */
	@Override
	public void currentRow(final int currentRow)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				//progDlg.setProcess(currentRow);
				prog.setValue(currentRow);
				System.out.println(currentRow);
			}
		});
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#done(int)
	 */
	@Override
	public void done(int rows)
	{
		System.out.println("done exporting " + rowCount);
		status.setText("exported " + rowCount); //XXX i18n
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#filling()
	 */
	@Override
	public void filling()
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
			{
				//progDlg.getProcessProgress().setIndeterminate(false);
				prog.setIndeterminate(false);
				System.out.println("filling");
				status.setText("filling"); //XXX i18n
			}
		});
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#loaded()
	 */
	@Override
	public void loaded()
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
			{
				//progDlg.getProcessProgress().setIndeterminate(false);
				prog.setIndeterminate(false);
				System.out.println("loaded");
				status.setText("loaded"); //XXX i18n
			}
		});
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#loading()
	 */
	@Override
	public void loading()
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
			{
				//progDlg.getProcessProgress().setIndeterminate(true);
				prog.setIndeterminate(true);
				System.out.println("loading");
				status.setText("loading"); //XXX i18n
			}
		});
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#rowCount(int)
	 */
	@Override
	public void rowCount(final int rowCount)
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
			{
				//progDlg.getProcessProgress().setIndeterminate(false);
				//progDlg.setProcess(0, rowCount);
				prog.setIndeterminate(false);
				prog.setMinimum(0);
				prog.setMaximum(rowCount);
				System.out.println(rowCount);
				ExportPanel.this.rowCount = rowCount;
			}
		});
	}

	
    public static void main(String[] args)
    {
        log.debug("********* Current ["+(new File(".").getAbsolutePath())+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // This is for Windows and Exe4J, turn the args into System Properties
        
        UIRegistry.setEmbeddedDBDir(UIRegistry.getDefaultEmbeddedDBPath()); // on the local machine
        
        for (String s : args)
        {
            String[] pairs = s.split("="); //$NON-NLS-1$
            if (pairs.length == 2)
            {
                if (pairs[0].startsWith("-D")) //$NON-NLS-1$
                {
                    System.setProperty(pairs[0].substring(2, pairs[0].length()), pairs[1]);
                } 
            } else
            {
                String symbol = pairs[0].substring(2, pairs[0].length());
                System.setProperty(symbol, symbol);
            }
        }
        
        // Now check the System Properties
        String appDir = System.getProperty("appdir");
        if (StringUtils.isNotEmpty(appDir))
        {
            UIRegistry.setDefaultWorkingPath(appDir);
        }
        
        String appdatadir = System.getProperty("appdatadir");
        if (StringUtils.isNotEmpty(appdatadir))
        {
            UIRegistry.setBaseAppDataDir(appdatadir);
        }
        
        String embeddeddbdir = System.getProperty("embeddeddbdir");
        if (StringUtils.isNotEmpty(embeddeddbdir))
        {
            UIRegistry.setEmbeddedDBDir(embeddeddbdir);
        }
        
        String mobile = System.getProperty("mobile");
        if (StringUtils.isNotEmpty(mobile))
        {
            UIRegistry.setEmbeddedDBDir(UIRegistry.getMobileEmbeddedDBPath());
        }

        // Set App Name, MUST be done very first thing!
        UIRegistry.setAppName("SchemaExporter");  //$NON-NLS-1$
        //UIRegistry.setAppName("Specify");  //$NON-NLS-1$
        
        // Then set this
        IconManager.setApplicationClass(Specify.class);
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml")); //$NON-NLS-1$

        
        
        System.setProperty(AppContextMgr.factoryName,                   "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr //$NON-NLS-1$
        System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences //$NON-NLS-1$
        System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");            // Needed By UIRegistry //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory", "edu.ku.brc.specify.ui.SpecifyDraggableRecordIdentiferFactory"); // Needed By the Form System //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.dbsupport.AuditInterceptor",     "edu.ku.brc.specify.dbsupport.AuditInterceptor");       // Needed By the Form System for updating Lucene and logging transactions //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.dbsupport.DataProvider",         "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.ui.db.PickListDBAdapterFactory", "edu.ku.brc.specify.ui.db.PickListDBAdapterFactory");   // Needed By the Auto Cosmplete UI //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty(CustomQueryFactory.factoryName,              "edu.ku.brc.specify.dbsupport.SpecifyCustomQueryFactory"); //$NON-NLS-1$
        System.setProperty(UIFieldFormatterMgr.factoryName,             "edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr");           // Needed for CatalogNumberign //$NON-NLS-1$
        System.setProperty(QueryAdjusterForDomain.factoryName,          "edu.ku.brc.specify.dbsupport.SpecifyQueryAdjusterForDomain"); // Needed for ExpressSearch //$NON-NLS-1$
        System.setProperty(SchemaI18NService.factoryName,               "edu.ku.brc.specify.config.SpecifySchemaI18NService");         // Needed for Localization and Schema //$NON-NLS-1$
        System.setProperty(WebLinkMgr.factoryName,                      "edu.ku.brc.specify.config.SpecifyWebLinkMgr");                // Needed for WebLnkButton //$NON-NLS-1$
        System.setProperty(SecurityMgr.factoryName,                     "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        System.setProperty(DBMSUserMgr.factoryName,                     "edu.ku.brc.dbsupport.MySQLDMBSUserMgr");
        System.setProperty(SchemaUpdateService.factoryName,             "edu.ku.brc.specify.dbsupport.SpecifySchemaUpdateService");   // needed for updating the schema
        
        final AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        adjustLocaleFromPrefs();
    	final String iRepPrefDir = localPrefs.getDirPath(); 
        int mark = iRepPrefDir.lastIndexOf(UIRegistry.getAppName(), iRepPrefDir.length());
        final String SpPrefDir = iRepPrefDir.substring(0, mark) + "Specify";
        HibernateUtil.setListener("post-commit-update", new edu.ku.brc.specify.dbsupport.PostUpdateEventListener()); //$NON-NLS-1$
        HibernateUtil.setListener("post-commit-insert", new edu.ku.brc.specify.dbsupport.PostInsertEventListener()); //$NON-NLS-1$
        HibernateUtil.setListener("post-commit-delete", new edu.ku.brc.specify.dbsupport.PostDeleteEventListener()); //$NON-NLS-1$
        
        SwingUtilities.invokeLater(new Runnable() {
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
          public void run()
            {
                
                try
                {
                    UIHelper.OSTYPE osType = UIHelper.getOSType();
                    if (osType == UIHelper.OSTYPE.Windows )
                    {
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        
                    } else if (osType == UIHelper.OSTYPE.Linux )
                    {
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                    }
                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, e);
                    log.error("Can't change L&F: ", e); //$NON-NLS-1$
                }
                
                DatabaseLoginPanel.MasterPasswordProviderIFace usrPwdProvider = new DatabaseLoginPanel.MasterPasswordProviderIFace()
                {
                    @Override
                    public boolean hasMasterUserAndPwdInfo(final String username, final String password)
                    {
                        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password))
                        {
                            UserAndMasterPasswordMgr.getInstance().setUsersUserName(username);
                            UserAndMasterPasswordMgr.getInstance().setUsersPassword(password);
                            boolean result = false;
                            try
                            {
                            	try
                            	{
                            		AppPreferences.getLocalPrefs().flush();
                            		AppPreferences.getLocalPrefs().setDirPath(SpPrefDir);
                            		AppPreferences.getLocalPrefs().setProperties(null);
                            		result = UserAndMasterPasswordMgr.getInstance().hasMasterUsernameAndPassword();
                            	}
                            	finally
                            	{
                            		AppPreferences.getLocalPrefs().flush();
                            		AppPreferences.getLocalPrefs().setDirPath(iRepPrefDir);
                            		AppPreferences.getLocalPrefs().setProperties(null);
                            	}
                            } catch (Exception e)
                            {
                            	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            	edu.ku.brc.exceptions.ExceptionTracker.getInstance()
    								.capture(MainFrameSpecify.class, e);
                            	result = false;
                            }
                            return result;
                        }
                        return false;
                    }
                    
                    @Override
                    public Pair<String, String> getUserNamePassword(final String username, final String password)
                    {
                        UserAndMasterPasswordMgr.getInstance().setUsersUserName(username);
                        UserAndMasterPasswordMgr.getInstance().setUsersPassword(password);
                        Pair<String, String> result = null;
                        try
                        {
                        	try
                        	{
                        		AppPreferences.getLocalPrefs().flush();
                        		AppPreferences.getLocalPrefs().setDirPath(SpPrefDir);
                        		AppPreferences.getLocalPrefs().setProperties(null);
                        		result = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
                        	}
                        	finally
                        	{
                        		AppPreferences.getLocalPrefs().flush();
                        		AppPreferences.getLocalPrefs().setDirPath(iRepPrefDir);
                        		AppPreferences.getLocalPrefs().setProperties(null);
                        	}
                        } catch (Exception e)
                        {
                        	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        	edu.ku.brc.exceptions.ExceptionTracker.getInstance()
								.capture(MainFrameSpecify.class, e);
                        	result = null;
                        }
                        return result;
                    }
                    @Override
                    public boolean editMasterInfo(final String username, final boolean askFroCredentials)
                    {
                        boolean result = false;
                    	try
                        {
                        	try
                        	{
                        		AppPreferences.getLocalPrefs().flush();
                        		AppPreferences.getLocalPrefs()
									.setDirPath(SpPrefDir);
                        		AppPreferences.getLocalPrefs().setProperties(null);
                        		result =  UserAndMasterPasswordMgr
									.getInstance()
									.editMasterInfo(username, askFroCredentials);
                        	} finally
                        	{
                        		AppPreferences.getLocalPrefs().flush();
                        		AppPreferences.getLocalPrefs().setDirPath(
									iRepPrefDir);
                        		AppPreferences.getLocalPrefs().setProperties(null);
                        	}
                        } catch (Exception e)
                        {
                        	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        	edu.ku.brc.exceptions.ExceptionTracker.getInstance()
								.capture(MainFrameSpecify.class, e);
                        	result = false;
                        }
                    	return result;
                   }
                };
                String nameAndTitle = "Schema Exporter"; // I18N
                UIRegistry.setRelease(true);
                UIHelper.doLogin(usrPwdProvider, false, false, new SchemaExportLauncher(), "Specify", nameAndTitle, nameAndTitle, "SpecifyWhite32", "login"); // true
																																	// means
																																	// do
																																	// auto
																																	// login
																																	// if
																																	// it
																																	// can,
																																	// second
																																	// bool
																																	// means
																																	// use
																																	// dialog
																																	// instead
																																	// of
																																	// frame
                
                localPrefs.load();
                
            }
        });

       
    }
    
    protected static void adjustLocaleFromPrefs()
    {
        String language = AppPreferences.getLocalPrefs().get("locale.lang", null); //$NON-NLS-1$
        if (language != null)
        {
            String country  = AppPreferences.getLocalPrefs().get("locale.country", null); //$NON-NLS-1$
            String variant  = AppPreferences.getLocalPrefs().get("locale.var",     null); //$NON-NLS-1$
            
            Locale prefLocale = new Locale(language, country, variant);
            
            Locale.setDefault(prefLocale);
            UIRegistry.setResourceLocale(prefLocale);
        }
        
        try
        {
            ResourceBundle.getBundle("resources", Locale.getDefault()); //$NON-NLS-1$
            
        } catch (MissingResourceException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, ex);
            Locale.setDefault(Locale.ENGLISH);
            UIRegistry.setResourceLocale(Locale.ENGLISH);
        }
        
    }
}
