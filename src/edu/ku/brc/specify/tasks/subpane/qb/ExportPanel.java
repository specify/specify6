/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
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
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.tools.ireportspecify.MainFrameSpecify;
import edu.ku.brc.ui.CustomDialog;
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
	protected JButton showIPTSQLBtn;
	protected JLabel status;
	protected JProgressBar prog;
	protected long rowCount = 0;
	protected int mapUpdating = -1;
	protected int stupid = -1;
	protected javax.swing.SwingWorker<Object, Object> updater = null;
	protected javax.swing.SwingWorker<Object, Object> dumper = null;
	
	protected final List<SpExportSchemaMapping> maps;
	
	public ExportPanel(List<SpExportSchemaMapping> maps)
	{
		super();
		this.maps = maps;
		createUI();
	}
	
	
	/**
	 * 
	 */
	public void createUI()
	{
    	buildTableModel();
		mapsDisplay = new JTable(mapsModel);
		mapsDisplay.setPreferredScrollableViewportSize(mapsDisplay.getPreferredSize());
    	setLayout(new BorderLayout());
    	//System.out.println();
//    	for (int r = 0; r < mapsDisplay.getRowCount(); r++)
//    	{
//    		for (int c = 0; c < mapsDisplay.getColumnCount(); c++)
//    		{
//    			System.out.print(mapsDisplay.getValueAt(r, c));
//    		}
//    		System.out.println();
//    	}
    	JScrollPane sp = new JScrollPane(mapsDisplay);
    	PanelBuilder tblpb = new PanelBuilder(new FormLayout("2dlu, f:p:g, 2dlu", "5dlu, f:p:g, 5dlu"));
    	//sp.setBorder(new EmptyBorder(7,7,7,7));
    	CellConstraints cc = new CellConstraints();
    	tblpb.add(sp, cc.xy(2, 2));
    	
    	add(tblpb.getPanel(), BorderLayout.CENTER);
    	exportToDbTblBtn = new JButton(UIRegistry.getResourceString("ExportPanel.ExportToDBTbl"));
    	//exportToDbTblBtn.setEnabled(false);
    	exportToDbTblBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int row = mapsDisplay.getSelectedRow();
				if (row != -1)
				{
					mapUpdating = row;
					stupid = 1;
					SpExportSchemaMapping map = maps.get(row);
					SpQuery q = map.getMappings().iterator().next()
						.getQueryField().getQuery();
					Vector<QBDataSourceListenerIFace> ls = new Vector<QBDataSourceListenerIFace>();
					ls.add(ExportPanel.this);
					exportToDbTblBtn.setEnabled(false);
					exportToTabDelimBtn.setEnabled(false);
					updater = QueryBldrPane.exportToTable(q, ls);
				}
				else 
				{
					UIRegistry.showLocalizedMsg("ExportPanel.PleaseMakeASelection");
				}
			}
    	});
    	
    	this.exportToTabDelimBtn = new JButton(UIRegistry.getResourceString("ExportPanel.ExportTabDelimTxt"));
    	this.exportToTabDelimBtn.setToolTipText(UIRegistry.getResourceString("ExportPanel.ExportTabDelimTxtHint"));
    	this.exportToTabDelimBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int row = mapsDisplay.getSelectedRow();
				if (row != -1)
				{
					mapUpdating = row;
					stupid = 0;
					SpExportSchemaMapping map = maps.get(row);
					SpQuery q = map.getMappings().iterator().next()
						.getQueryField().getQuery();
					Vector<QBDataSourceListenerIFace> ls = new Vector<QBDataSourceListenerIFace>();
					ls.add(ExportPanel.this);
					File file = new File(UIRegistry.getDefaultWorkingPath() + File.separator + q.getName() + ".txt");
					exportToDbTblBtn.setEnabled(false);
					exportToTabDelimBtn.setEnabled(false);
					dumper = ExportToMySQLDB.exportRowsToTabDelimitedText(file, null, ExportToMySQLDB.fixTblNameForMySQL(q.getName()), ls);
					dumper.execute();
				}
				else 
				{
					UIRegistry.showLocalizedMsg("ExportPanel.PleaseMakeASelection");
				}
			}
    	});

    	showIPTSQLBtn = new JButton(UIRegistry.getResourceString("ExportPanel.ShowSQLBtn"));
    	showIPTSQLBtn.setToolTipText(UIRegistry.getResourceString("ExportPanel.ShowSQLBtnTT"));
    	showIPTSQLBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int row = mapsDisplay.getSelectedRow();
				if (row != -1)
				{
					SpExportSchemaMapping map = maps.get(row);
					String iptSQL = ExportToMySQLDB.getSelectForIPTDBSrc(ExportToMySQLDB.fixTblNameForMySQL(map.getMappingName()));
					JTextArea ta = new JTextArea(iptSQL);
					ta.setLineWrap(true);
					ta.setColumns(60);
					ta.setRows(10);
					ta.selectAll();
					JScrollPane sp = new JScrollPane(ta);
					CustomDialog cd = new CustomDialog((Frame )UIRegistry.getTopWindow(), UIRegistry.getResourceString("ExportPanel.SQLTitle"), true, sp);
					UIHelper.centerAndShow(cd);
				}
				else 
				{
					UIRegistry.showLocalizedMsg("ExportPanel.PleaseMakeASelection");
				}
			}
    	});
    	
    	PanelBuilder pbb = new PanelBuilder(new FormLayout("2dlu, f:p:g, p, 2dlu, p, 2dlu, p, 2dlu", "p, p"));
    	status = new JLabel(UIRegistry.getResourceString("ExportPanel.InitialStatus")); 
    	status.setFont(status.getFont().deriveFont(Font.ITALIC));
    	Dimension pref = status.getPreferredSize();
    	pref.setSize(Math.max(250, pref.getWidth()), pref.getHeight());
    	status.setPreferredSize(pref);
    	pbb.add(status, cc.xy(2, 1));
    	pbb.add(this.showIPTSQLBtn, cc.xy(3, 1));
    	pbb.add(exportToTabDelimBtn, cc.xy(7, 1));
    	pbb.add(exportToDbTblBtn, cc.xy(5, 1));
    	
    	prog = new JProgressBar();
    	pbb.add(prog, cc.xyw(2, 2, 4));
    	add(pbb.getPanel(), BorderLayout.SOUTH);
	}
	
	/**
	 * 
	 */
	protected void buildTableModel()
	{
		Vector<Vector<String>> data = new Vector<Vector<String>>();
		for (SpExportSchemaMapping map : maps)
		{
			Vector<String> row = new Vector<String>(2);
			row.add(map.getMappingName());
			row.add(map.getTimestampExported() != null ? map.getTimestampExported().toString() : 
				UIRegistry.getResourceString("ExportPanel.MappingCacheNeedsBuilding"));
			data.add(row);
		}
		Vector<String> headers = new Vector<String>();
		headers.add(UIRegistry.getResourceString("ExportPanel.MappingTitle"));
		headers.add(UIRegistry.getResourceString("ExportPanel.MappingExportTimeTitle"));

		mapsModel = new DefaultTableModel(data, headers);
	}

	/**
	 * @return
	 */
	public boolean close()
	{
		if (updater != null || dumper != null)
		{
			boolean result = UIRegistry.displayConfirmLocalized("ExportPanel.CancelExportTitle", "ExportPanel.CancelConfirmMsg", "OK", "Cancel", JOptionPane.QUESTION_MESSAGE);
			if (result)
			{
				if (updater != null)
				{
					updater.cancel(true);
				}
				else
				{
					dumper.cancel(true);
				}
			}
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#currentRow(int)
	 */
	@Override
	public void currentRow(final long currentRow)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				//progDlg.setProcess(currentRow);
				prog.setValue((int )currentRow);
				System.out.println(currentRow);
			}
		});
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#done(int)
	 */
	@Override
	public void done(long rows)
	{
		if (stupid == 0 && mapUpdating != -1)
		{
			System.out.println("done exporting " + rowCount);
			
			status.setText(String.format(UIRegistry.getResourceString("ExportLabel.UpdateDone"), rowCount));
			if (updater != null)
			{
				refreshUpdatedMapDisplay(mapUpdating);
			}
			prog.setValue(0);
			this.exportToDbTblBtn.setEnabled(true);
			this.exportToTabDelimBtn.setEnabled(true);
			mapUpdating = -1;
			updater = null;
			dumper = null;
		}
		stupid--;
	}

	/**
	 * 
	 */
	protected void refreshUpdatedMapDisplay(int mapToRefresh)
	{
		SpExportSchemaMapping map = maps.get(mapToRefresh);
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
        	session.refresh(map);
			map.forceLoad();
			map.getMappings().iterator().next().getQueryField().getQuery().forceLoad();
       }
        finally
        {
        	session.close();
        }
        this.mapsModel.setValueAt(map.getTimestampExported().toString(), mapToRefresh, 1);
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
				status.setText(UIRegistry.getResourceString("ExportPanel.UpdatingCache")); 
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
				status.setText("ExportPanel.DataRetrieved"); 
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
				status.setText(UIRegistry.getResourceString("ExportPanel.RetrievingData")); 
			}
		});
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#rowCount(int)
	 */
	@Override
	public void rowCount(final long rowCount)
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
			{
				//progDlg.getProcessProgress().setIndeterminate(false);
				//progDlg.setProcess(0, rowCount);
				prog.setIndeterminate(false);
				prog.setMinimum(0);
				prog.setMaximum((int )rowCount - 1);
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
