/**
 * 
 */
package edu.ku.brc.specify.tools.export;


import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import edu.ku.brc.helpers.UIFileFilter;
import edu.ku.brc.specify.tasks.StartUpTask;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.WebStoreAttachmentMgr;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.util.FileUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.ProcessListUtil;
import edu.ku.brc.af.ui.ProcessListUtil.PROC_STATUS;
import edu.ku.brc.af.ui.ProcessListUtil.ProcessListener;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.SpExportSchemaItemMapping;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.tasks.ExportMappingTask;
import edu.ku.brc.specify.tasks.QueryTask;
import edu.ku.brc.specify.tasks.subpane.qb.ERTICaptionInfoQB;
import edu.ku.brc.specify.tasks.subpane.qb.HQLSpecs;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSource;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace;
import edu.ku.brc.specify.tasks.subpane.qb.QueryBldrPane;
import edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanel;
import edu.ku.brc.specify.tasks.subpane.qb.QueryParameterPanel;
import edu.ku.brc.specify.tasks.subpane.qb.TableQRI;
import edu.ku.brc.specify.tasks.subpane.qb.TableTree;
import edu.ku.brc.specify.tools.ireportspecify.MainFrameSpecify;
import edu.ku.brc.specify.tools.webportal.BuildSearchIndex2;
import edu.ku.brc.specify.ui.AppBase;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.AttachmentUtils;


/**
 * @author timo
 *
 */
@SuppressWarnings("serial")
public class ExportPanel extends JPanel implements QBDataSourceListenerIFace
{
    protected static final Logger                            log            = Logger.getLogger(ExportPanel.class);

    protected static final String EXPORT_TEXT_PATH = "ExportPanel.TabDelimExportPath";
    protected static final String EXPORT_WEBPORTAL_PATH = "ExportPanelExportWebPortalPath";
    protected static final long maxExportRowCount = 100000;
    
    protected boolean exportIsThreaded = true;
    
    protected JTable mapsDisplay;
	protected DefaultTableModel mapsModel;
	protected JButton quitBtn;
	protected JButton exportToDbTblBtn;
	protected JButton exportToTabDelimBtn;
	protected JButton setupWebPortalBtn;
	protected JButton showIPTSQLBtn;
	protected JButton helpBtn;
	protected JLabel status;
	protected JProgressBar prog;
	protected JPanel progPane;
	
	protected String itUserName = null;
	protected String itPw = null;
	
	protected long rowCount = 0;
	protected long rowsExported = 0;
	protected long cacheRowCount = 0;
	protected AtomicInteger mapUpdating = new AtomicInteger(-1);
	protected int stupid = -1;
	protected javax.swing.SwingWorker<Object, Object> updater = null;
	protected javax.swing.SwingWorker<Object, Object> dumper = null;
	
	
	protected final List<SpExportSchemaMapping> maps;
	protected final List<Pair<SpExportSchemaMapping, Long>> updateStats;
	
	protected final Boolean useBulkLoad = AppPreferences.getLocalPrefs().getBoolean("ExportPanel.UseBulkLoad", false);
	protected final String bulkFileDir = AppPreferences.getLocalPrefs().get("ExportPanel.BulkFileDir", AppPreferences.getLocalPrefs().getDirPath());
	
	
	/**
	 * @param maps
	 */
	public ExportPanel(List<SpExportSchemaMapping> maps)
	{
		super();
		this.maps = maps;
		this.updateStats = new ArrayList<Pair<SpExportSchemaMapping, Long>>();
		for (SpExportSchemaMapping map : maps)
		{
			updateStats.add(new Pair<SpExportSchemaMapping, Long>(map, -2L));
		}
        StartUpTask.configureAttachmentManager();
		createUI();
		startStatusCalcs();
	}
	
	/**
	 * Starts threads to calculate status of maps
	 */
	protected void startStatusCalcs()
	{
		int row = 0;
		for (SpExportSchemaMapping map : maps)
		{
			if (!rebuildForRow(row))
			{
				getMappingStatus(map);
			} else 
			{
				updateStats.get(row).setSecond(-1L);
			}
			row++;
		}
	}
	
	/**
	 * @param mappingName
	 * @return
	 */
	public static String getCacheTableName(String mappingName)
	{
		//return  ExportToMySQLDB.fixTblNameForMySQL(mappingName + AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionName());
		return  ExportToMySQLDB.fixTblNameForMySQL(mappingName);
	}
	
	/**
	 * 
	 */
	protected void updateUIAfterMapSelection()
	{
		updateBtnStates();
	}
	
	/**
	 * 
	 */
	protected void updateBtnStates()
	{
		//boolean upToDate = isUpToDateForRow(selectedIdx);
		int selectedIdx = mapsDisplay.getSelectedRow();
		boolean isCheckingStatus = isCheckingStatusForRow(selectedIdx);
		boolean isBuilt = isBuiltForRow(selectedIdx);
		boolean notUpdating = mapUpdating.get() == -1;
		//System.out.println("updateUIAfterMapSelection: Row: " + selectedIdx + ", isBuilt: " + isBuilt + ", updating: " + !notUpdating);
		this.showIPTSQLBtn.setEnabled(notUpdating && isBuilt);
		this.exportToTabDelimBtn.setEnabled(notUpdating && isBuilt);
		this.setupWebPortalBtn.setEnabled(notUpdating && isBuilt);
		this.exportToDbTblBtn.setEnabled(notUpdating && !isCheckingStatus);
		
	}
	/**
	 * 
	 */
	public void createUI()
	{
    	buildTableModel();
		mapsDisplay = new JTable(mapsModel) {

			/* (non-Javadoc)
			 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
			 */
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
			
		};
		mapsDisplay.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			/* (non-Javadoc)
			 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
			 */
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting())
				{
					SwingUtilities.invokeLater(new Runnable(){

						/* (non-Javadoc)
						 * @see java.lang.Runnable#run()
						 */
						@Override
						public void run() {
							updateUIAfterMapSelection();
						}
					
					});
				}
				
			}
			
		});
		mapsDisplay.getTableHeader().setReorderingAllowed(false);
		mapsDisplay.setPreferredScrollableViewportSize(mapsDisplay.getPreferredSize());
		mapsDisplay.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mapsDisplay.getSelectionModel().setSelectionInterval(0, 0);
    	setLayout(new BorderLayout());
    	JScrollPane sp = new JScrollPane(mapsDisplay);
    	PanelBuilder tblpb = new PanelBuilder(new FormLayout("2dlu, f:p:g, 2dlu", "5dlu, f:p:g, 5dlu"));
    	CellConstraints cc = new CellConstraints();
    	tblpb.add(sp, cc.xy(2, 2));
    	
    	add(tblpb.getPanel(), BorderLayout.CENTER);
    	exportToDbTblBtn = UIHelper.createButton(UIRegistry.getResourceString("ExportPanel.ExportToDBTbl"));
    	exportToDbTblBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int row = mapsDisplay.getSelectedRow();
				if (row != -1)
				{
					mapUpdating.set(row);
					stupid = 1;
					SpExportSchemaMapping map = maps.get(row);
					boolean reBuildIt = false;
					if (isUpToDateForRow(row)) 
					{
						reBuildIt = UIRegistry.displayConfirmLocalized("ExportPanel.ConfirmForceRebuildTitle", "ExportPanel.ConfirmForceRebuildMsg", 
								"ExportPanel.RebuildBtn", "Cancel", JOptionPane.QUESTION_MESSAGE);
						if (!reBuildIt) {
							return;
						}
					}
					if (checkLock(map)) 
					{
						updater = exportToTable(map, rebuildForRow(row) || reBuildIt);
						if (updater != null) 
						{
							updater.execute();
							SwingUtilities.invokeLater(new Runnable() 
							{

								/*
								 * (non-Javadoc)
								 * 
								 * @see java.lang.Runnable#run()
								 */
								@Override
								public void run() 
								{
									updateBtnStates();
									((CardLayout) progPane.getLayout())
											.last(progPane);
								}

							});
						}
					}
				}
				else 
				{
					UIRegistry.showLocalizedMsg("ExportPanel.PleaseMakeASelection");
				}
			}
    	});

    	setupWebPortalBtn = UIHelper.createButton(UIRegistry.getResourceString("ExportPanel.SetupWebPortalBtnTitle"));
		setupWebPortalBtn.setToolTipText(UIRegistry.getResourceString("ExportPanel.SetupWebPortalBtnHint"));
    	setupWebPortalBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int row = mapsDisplay.getSelectedRow();
				if (row != -1) {
					mapUpdating.set(row);
					stupid = 1;
					SpExportSchemaMapping map = maps.get(row);
					if (rebuildForRow(row)) {
						// The button probably won't be enabled in this case but...
						UIRegistry
								.displayInfoMsgDlg(getResourceString("ExportPanel.NoWebSetupForCacheNeedsRebuild"));
					} else if (!isUpToDateForRow(row)) {
						// The button probably won't be enabled in this case but...
						UIRegistry
								.displayInfoMsgDlg(getResourceString("ExportPanel.NoWebSetupForCacheNotUpToDate"));
					} else if (checkLock(map)) {
						AppPreferences localPrefs =  AppPreferences.getLocalPrefs();
						String defPath = localPrefs.get(EXPORT_WEBPORTAL_PATH, null);
						JFileChooser save = defPath == null ? new JFileChooser() :
							new JFileChooser(new File(defPath));
						save.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        save.setFileFilter(new UIFileFilter("zip"));
						int result = save.showSaveDialog(null);
	    				if (result != JFileChooser.APPROVE_OPTION)
						{	
							return;
						}
	    				//localPrefs.put(EXPORT_WEBPORTAL_PATH, save.getCurrentDirectory().getPath());
	    				localPrefs.put(EXPORT_WEBPORTAL_PATH, save.getSelectedFile().getPath());

                        String zipFile = save.getSelectedFile().getPath();
                        if (!zipFile.toLowerCase().endsWith(".zip"))
                        {
                             zipFile += ".zip";
                        }
                        final BuildSearchIndex2 bsi = new BuildSearchIndex2(
                                maps.get(row),
                                zipFile,
                                getCollectionName(),
                                getAttachmentURL());

			        	try {
			        		bsi.connect();
			        	} catch (SQLException sqex) {
			                UsageTracker.incrHandledUsageCount();
			                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExportPanel.class, sqex);	
			                return;
			        	}
			        	new javax.swing.SwingWorker<Boolean, Object>() {

			        		Boolean success = false;
							/* (non-Javadoc)
							 * @see javax.swing.SwingWorker#doInBackground()
							 */
							@Override
							protected Boolean doInBackground() throws Exception {
								success = bsi.index(ExportPanel.this);
								return success;
							}

							/* (non-Javadoc)
							 * @see javax.swing.SwingWorker#done()
							 */
							@Override
							protected void done() {
								if (!success)
								{
									String msg = getResourceString("ExportPanel.SetupWebFailMsg");
									UIRegistry.displayErrorDlg(msg);
									ExportPanel.this.done(-1);
								} else
								{
									ExportPanel.this.done(1);
								}
							}
			        		
			        	}.execute();
			        	
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								((CardLayout) progPane.getLayout())
										.last(progPane);
							}

						});
					}
				} else {
					UIRegistry
							.showLocalizedMsg("ExportPanel.PleaseMakeASelection");
				}
			}
		});
    	//disabling for jar release for WB RecordSet-to-Dataset fix.
    	//setupWebPortalBtn.setVisible(false);

    	this.exportToTabDelimBtn = UIHelper.createButton(UIRegistry.getResourceString("ExportPanel.ExportTabDelimTxt"));
    	this.exportToTabDelimBtn.setToolTipText(UIRegistry.getResourceString("ExportPanel.ExportTabDelimTxtHint"));
    	this.exportToTabDelimBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int row = mapsDisplay.getSelectedRow();
				if (row != -1)
				{
					if (!isBuiltForRow(row))
					{
						UIRegistry.displayInfoMsgDlgLocalized("ExportPanel.CacheNotCreated");
						return;
					}
					AppPreferences localPrefs =  AppPreferences.getLocalPrefs();
					String defPath = localPrefs.get(EXPORT_TEXT_PATH, null);
					JFileChooser save = defPath == null ? new JFileChooser() :
						new JFileChooser(new File(defPath));
					int result = save.showSaveDialog(null);
    				if (result != JFileChooser.APPROVE_OPTION)
					{	
						return;
					}
    				localPrefs.put(EXPORT_TEXT_PATH, save.getCurrentDirectory().getPath());
					mapUpdating.set(row);
					stupid = 0;
					SpExportSchemaMapping map = maps.get(row);
					SpQuery q = map.getMappings().iterator().next()
						.getQueryField().getQuery();
					Vector<QBDataSourceListenerIFace> ls = new Vector<QBDataSourceListenerIFace>();
					ls.add(ExportPanel.this);
					//File file = new File(UIRegistry.getDefaultWorkingPath() + File.separator + q.getName() + ".txt");
					File file = save.getSelectedFile();
					exportToDbTblBtn.setEnabled(false);
					exportToTabDelimBtn.setEnabled(false);
					dumper = ExportToMySQLDB.exportRowsToTabDelimitedText(file, null, getCacheTableName(q.getName()), ls);
					SwingUtilities.invokeLater(new Runnable() {

						/* (non-Javadoc)
						 * @see java.lang.Runnable#run()
						 */
						@Override
						public void run()
						{
							((CardLayout )progPane.getLayout()).last(progPane);
						}
						
					});
					dumper.execute();
				}
				else 
				{
					UIRegistry.showLocalizedMsg("ExportPanel.PleaseMakeASelection");
				}
			}
    	});

    	showIPTSQLBtn = UIHelper.createButton(UIRegistry.getResourceString("ExportPanel.ShowSQLBtn"));
    	showIPTSQLBtn.setToolTipText(UIRegistry.getResourceString("ExportPanel.ShowSQLBtnTT"));
    	showIPTSQLBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int row = mapsDisplay.getSelectedRow();
				if (row != -1)
				{
					if (!isBuiltForRow(row))
					{
						UIRegistry.displayInfoMsgDlgLocalized("ExportPanel.NoSQLForRebuild");
					}
					else
					{
					    JPanel panel = new JPanel(new BorderLayout());
					    
						SpExportSchemaMapping map = maps.get(row);
						String iptSQL = ExportToMySQLDB
								.getSelectForIPTDBSrc(getCacheTableName(map.getMappingName()));
						JTextArea ta = new JTextArea(iptSQL);
						ta.setLineWrap(true);
						ta.setColumns(60);
						ta.setRows(10);
						ta.selectAll();
						JScrollPane scrp = new JScrollPane(ta);
						panel.add(scrp, BorderLayout.CENTER);
						panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
						CustomDialog cd = new CustomDialog((Frame) UIRegistry
								.getTopWindow(), UIRegistry
								.getResourceString("ExportPanel.SQLTitle"),
								true, CustomDialog.OK_BTN, panel);
						cd.setOkLabel(UIRegistry.getResourceString("CLOSE"));
						UIHelper.centerAndShow(cd);
					}
				}
				else 
				{
					UIRegistry.showLocalizedMsg("ExportPanel.PleaseMakeASelection");
				}
			}
    	});
    	
        helpBtn = createButton(getResourceString("HELP"));
        HelpMgr.registerComponent(helpBtn, "schema_tool");
    	
        PanelBuilder btnPB = new PanelBuilder(new FormLayout("p,f:p:g,4px,p,8px,p,8px,p,8px,p,8px,p", "p"));
        quitBtn = UIHelper.createButton("Quit");
        
        btnPB.add(helpBtn,             cc.xy(1, 1));
        btnPB.add(showIPTSQLBtn,       cc.xy(4, 1));
        btnPB.add(exportToTabDelimBtn, cc.xy(6, 1));
        btnPB.add(exportToDbTblBtn,    cc.xy(8, 1));
        btnPB.add(setupWebPortalBtn,   cc.xy(10, 1));
        btnPB.add(quitBtn,             cc.xy(12, 1));
        
    	status = new JLabel(UIRegistry.getResourceString("ExportPanel.InitialStatus")); 
    	status.setFont(status.getFont().deriveFont(Font.ITALIC));
    	Dimension pref = status.getPreferredSize();
    	pref.setSize(Math.max(300, pref.getWidth()), pref.getHeight());
    	status.setPreferredSize(pref);
    	
    	
    	progPane = new JPanel(new CardLayout());
    	progPane.add(new JPanel(), "blank");
    	
    	prog = new JProgressBar();
    	progPane.add(prog, "prog");
    	
    	
        PanelBuilder pbb = new PanelBuilder(new FormLayout("f:p:g,8px,f:p:g", "p, 8px, p"));
        pbb.add(status,           cc.xy(1, 1));
        pbb.add(progPane,         cc.xy(3, 1));
        pbb.add(btnPB.getPanel(), cc.xyw(1, 3, 3));
    	
    	//prog.setVisible(false);
    	add(pbb.getPanel(), BorderLayout.SOUTH);

    	setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    	
        HelpMgr.setAppDefHelpId("schema_tool");
        
        quitBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        shutdown();
                    }
                });
            }
        });
	}
	
    /**
     * 
     */
    private void shutdown()
    {
        helpBtn.setEnabled(false);
        showIPTSQLBtn.setEnabled(false);
        exportToTabDelimBtn.setEnabled(false);
        exportToDbTblBtn.setEnabled(false);
        setupWebPortalBtn.setEnabled(false);
        quitBtn.setEnabled(false);
        
        // Need for proper UI feedback
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                doQuit();
            }
        });
    }

    private String getAttachmentURL() {
        AttachmentManagerIface attachmentMgr = AttachmentUtils.getAttachmentManager();
        if (attachmentMgr instanceof WebStoreAttachmentMgr) {
            return ((WebStoreAttachmentMgr)attachmentMgr).getServerURL();
        } else {
            return "";
        }
    }

    private String getCollectionName() {
        String collectionName = null;

        try {
            DataProviderSessionIFace session = null;
            SpecifyUser currentUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
            if (currentUser != null) {
                session = DataProviderFactory.getInstance().createSession();

                SpecifyUser user = session.getData(SpecifyUser.class, "id", currentUser.getId(), DataProviderSessionIFace.CompareType.Equals);
                collectionName = user.getLoginCollectionName();
                user.setLoginOutTime(new Timestamp(System.currentTimeMillis()));

                try {
                    session.beginTransaction();
                    session.saveOrUpdate(user);
                    session.commit();

                } catch (Exception ex) {
                    log.error(ex);

                } finally {
                    if (session != null) {
                        session.close();
                    }
                }
            }

        } catch (Exception ex) {
            log.error(ex);
        }

        return collectionName;
    }
    /**
     * 
     */
    private void doQuit()
    {
        try
        {
            DataProviderSessionIFace session     = null;
            SpecifyUser              currentUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
            if (currentUser != null)
            {
                session = DataProviderFactory.getInstance().createSession();
                
                SpecifyUser user = session.getData(SpecifyUser.class, "id", currentUser.getId(), DataProviderSessionIFace.CompareType.Equals);
                user.setIsLoggedIn(false);
                user.setLoginDisciplineName(null);
                user.setLoginCollectionName(null);
                user.setLoginOutTime(new Timestamp(System.currentTimeMillis()));
                
                try
                {
                    session.beginTransaction();
                    session.saveOrUpdate(user);
                    session.commit();
                    
                } catch (Exception ex)
                {
                    log.error(ex);
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
            }
            
        } catch (Exception ex)
        {
            log.error(ex);
        }
        DataProviderFactory.getInstance().shutdown();
        DBConnection.shutdown();
        DBConnection.shutdownFinalConnection(true, false); // true means System.exit
        
        System.exit(1);
    }
    
	/**
	 * @param map
	 * @return true if map lock status is ok.
	 */
	protected boolean checkLock(SpExportSchemaMapping map)
	{
		boolean result = ExportMappingTask.checkMappingLock(map);
		return result;
	}
	
	/**
	 * @param mapUpdatingArg
	 * 
	 * Unlocks tasksemaphore for map
	 */
	protected void unlock(int mapUpdatingArg)
	{
		if (mapUpdating.get() != -1)
		{
			ExportMappingTask.unlockMapping(maps.get(mapUpdatingArg));
		}
	}
	
	/**
	 * @param row
	 * @return true if the cache for the map displayed at row needs rebuilding.
	 */
	protected boolean rebuildForRow(int row)
	{
		return mapsModel.getValueAt(row, 2).toString().equals(UIRegistry.getResourceString("ExportPanel.MappingCacheNeedsBuilding"));
	}
	
	/**
	 * @param row
	 * @return true if the cache for mapping at row has ever been built
	 */
	protected boolean isBuiltForRow(int row)
	{
		return !mapsModel.getValueAt(row, 1).toString().equals(UIRegistry.getResourceString("ExportPanel.Never"));
	}
	
	protected boolean isUpToDateForRow(int row)
	{
		return mapsModel.getValueAt(row, 2).toString().equals(UIRegistry.getResourceString("ExportPanel.Uptodate"));
	}
	
	protected boolean isCheckingStatusForRow(int row)
	{
		return mapsModel.getValueAt(row, 2).toString().equals(UIRegistry.getResourceString("ExportPanel.CalculatingStatus"));
	}
	/**
	 * @param map
	 * @return text description of update status for map.
	 */
	protected String getInitialMapStatusText(SpExportSchemaMapping map)
	{
		if (needsToBeRebuilt(map))
		{
			return UIRegistry.getResourceString("ExportPanel.MappingCacheNeedsBuilding");
		}
		else
		{
			SwingUtilities.invokeLater(new Runnable() {

				/* (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				@Override
				public void run() {
					exportToDbTblBtn.setEnabled(false);
				}
				
			});
			return UIRegistry.getResourceString("ExportPanel.CalculatingStatus");
		}
	}
	
	/**
	 * @param map
	 * @param stats
	 * 
	 * Updates status column for map in maps display
	 */
	protected void displayStatusForMap(SpExportSchemaMapping map, MappingUpdateStatus stats)
	{
		String statsText;
		if (stats == null)
		{
			statsText = UIRegistry.getResourceString("ExportPanel.NeedsUpdating");
		}
		else if (stats.getTotalRecsChanged() == 0)
		{
			statsText = UIRegistry.getResourceString("ExportPanel.Uptodate");
		}
		else
		{
			statsText = String.format(getResourceString("ExportPanel.OutOfDateRecs"), stats.getTotalRecsChanged());
		}
		int row = 0;
		while (maps.get(row) != map) row++;
		updateStats.get(row).setSecond(stats.getTotalRecsChanged());
		mapsModel.setValueAt(statsText, row, 2);
	}
	
	/**
	 * 
	 */
	protected void buildTableModel()
	{
		Vector<Vector<String>> data = new Vector<Vector<String>>();
		for (SpExportSchemaMapping map : maps)
		{
			Vector<String> row = new Vector<String>(3);
			row.add(map.getMappingName());
			row.add(map.getTimestampExported() != null ? map.getTimestampExported().toString() : 
				UIRegistry.getResourceString("ExportPanel.Never"));
			row.add(getInitialMapStatusText(map));
			
			data.add(row);
		}
		Vector<String> headers = new Vector<String>();
		headers.add(UIRegistry.getResourceString("ExportPanel.MappingTitle"));
		headers.add(UIRegistry.getResourceString("ExportPanel.MappingExportTimeTitle"));
		headers.add(UIRegistry.getResourceString("ExportPanel.Status"));
		mapsModel = new DefaultTableModel(data, headers);	
	}

	/**
	 * @return
	 */
	public boolean close()
	{
		if (updater != null || dumper != null)
		{
			boolean result = UIRegistry.displayConfirmLocalized("ExportPanel.CancelExportTitle", "ExportPanel.CancelConfirmMsg", "YES", "NO", JOptionPane.QUESTION_MESSAGE);
			if (result)
			{
				if (updater != null && !updater.isCancelled() && !updater.isDone())
				{
					updater.cancel(true);
				}
				else if (dumper != null && !dumper.isCancelled() && !dumper.isDone())
				{
					dumper.cancel(true);
				}
			}
			return result;
		}
		return true;
	}
	
	/**
	 * @param map
	 * @return the number of columns in the cache table for map
	 */
	protected int getNumberColumnsInCache(SpExportSchemaMapping map)
	{
		try
		{
			Connection conn = DBConnection.getInstance().createConnection();
			Statement stmt = conn.createStatement();
			try
			{
				//XXX "limit" keyword may be mySQL-specific 
				ResultSet rs = stmt.executeQuery("select * from " + getCacheTableName(map.getMappingName()) + " limit 1");
				int result = rs.getMetaData().getColumnCount();
				rs.close();
				return result;
			} finally
			{
				stmt.close();
				conn.close();
			}
		} catch (Exception ex)
		{
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExportPanel.class, ex);
            //UIRegistry.getStatusBar().setErrorMessage(ex.getLocalizedMessage(), ex);
            throw new RuntimeException(ex);
		}
	}
	
	/**
	 * @param map
	 * @return true if cache table for map needs to be rebuilt
	 */
	protected boolean needsToBeRebuilt(SpExportSchemaMapping map)
	{
        if (map.getTimestampExported() != null)
        {
        	int numberOfColumnsMapped = 0;
        	for (SpExportSchemaItemMapping im : map.getMappings())
        	{
        		if (im.getTimestampCreated().compareTo(map.getTimestampExported()) > 0)
        		{
        			return true;
        		}
        		if (im.getTimestampModified() != null && im.getTimestampModified().compareTo(map.getTimestampExported()) > 0)
        		{
        			return true;
        		}
        		if (im.getQueryField().getIsDisplay())
        		{
        			numberOfColumnsMapped++;
        		}
        	}
        	if (getNumberColumnsInCache(map) - 1 != numberOfColumnsMapped)
        	{
        		return true;
        	}
        	SpQuery q = map.getMappings().iterator().next().getQueryField().getQuery();
    		if (q.getTimestampCreated().compareTo(map.getTimestampExported()) > 0)
    		{
    			return true;
    		}
    		if (q.getTimestampModified() != null && q.getTimestampModified().compareTo(map.getTimestampExported()) > 0)
    		{
    			return true;
    		}
        	for (SpQueryField qf : q.getFields())
        	{
        		if (qf.getTimestampCreated().compareTo(map.getTimestampExported()) > 0)
        		{
        			return true;
        		}
        		if (qf.getTimestampModified() != null && qf.getTimestampModified().compareTo(map.getTimestampExported()) > 0)
        		{
        			return true;
        		}
        	}
        	return false;
        }
        return true;
	}
	
	/**
	 * @param theMapping
	 * @param includeRecordIds
	 * @param getColInfo
	 * @return
	 */
	protected List<Specs> getSpecs(SpExportSchemaMapping theMapping, boolean includeRecordIds,
			boolean getColInfo, boolean rebuildExistingTbl)
	{
        UsageTracker.incrUsageCount("SchemaExport.ExportToTable");
        QueryTask qt = (QueryTask )ContextMgr.getTaskByClass(QueryTask.class);
        final TableTree tblTree;
        final Hashtable<String, TableTree> ttHash;
        if (qt != null)
        {
            Pair<TableTree, Hashtable<String, TableTree>> trees = qt.getTableTrees();
            tblTree = trees.getFirst();
            ttHash = trees.getSecond();
        }
        else
        {
            log.error("Cound not find the Query task when exporting mapping");
            //blow up
            throw new RuntimeException("Cound not find the Query task when exporting mapping");
        }
        TableQRI rootQRI = null;
		final Vector<QBDataSourceListenerIFace> dataSrcListeners = new Vector<QBDataSourceListenerIFace>();
		dataSrcListeners.add(this);
		SpQuery exportQuery = theMapping.getMappings().iterator().next().getQueryField().getQuery();
        int cId = exportQuery.getContextTableId();
        for (TableTree tt : ttHash.values())
        {
            if (cId == tt.getTableInfo().getTableId())
            {
                rootQRI = tt.getTableQRI();
                break;
            }
        }
        QueryParameterPanel qpp = new QueryParameterPanel(){

			@Override
			public boolean isPromptMode() {
				return false;
			}

			@Override
			public boolean isForSchemaExport() {
				return true;
			} 
        	
        };
        qpp.setQuery(exportQuery, tblTree, ttHash, false);
        Vector<QueryFieldPanel> qfps = QueryBldrPane.getQueryFieldPanelsForMapping(qpp, exportQuery.getFields(), tblTree, ttHash,
        		null, theMapping, null, null);

        HQLSpecs sql = null;
        String uniquenessHQL = null;
        HQLSpecs uniquenessSql = null;
        try
        {
            //the hql generated by this call will only select records that have changed
        	sql = QueryBldrPane.buildHQL(rootQRI, !includeRecordIds, qfps, tblTree, null, 
            		exportQuery.getSearchSynonymy() == null ? false : exportQuery.getSearchSynonymy(),
            				true, rebuildExistingTbl ? null : theMapping.getTimestampExported());
            //get the hql to use when determining if the export mapping query still returns unique col ids.
        	if (theMapping.getTimestampExported() != null && !rebuildExistingTbl)
        	{
        		uniquenessSql = QueryBldrPane.buildHQL(rootQRI, !includeRecordIds, qfps, tblTree, null, 
            		exportQuery.getSearchSynonymy() == null ? false : exportQuery.getSearchSynonymy(),
            				true, null);
        		uniquenessHQL = uniquenessSql.getHql();
        	}
        	else
        	{
        		uniquenessHQL = sql.getHql();
        	}
        }
        catch (Exception ex)
        {
            UsageTracker.incrHandledUsageCount();
            ex.printStackTrace();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QueryBldrPane.class, ex);
            UIRegistry.getStatusBar().setErrorMessage(ex.getLocalizedMessage(), ex);
            return null;
        }
        List<ERTICaptionInfoQB> cols = getColInfo ?
        	QueryBldrPane.getColumnInfo(qfps, false, rootQRI.getTableInfo(), true) : null;
        
        List<Specs> result = new ArrayList<Specs>();
        result.add(new Specs(sql, cols, uniquenessHQL, uniquenessSql));
        return result;
	}
	
	protected String adjustPathForWindows(String path)
	{
		String result = path;
		if (UIHelper.isWindows()) 
		{
			//result = result.replaceAll("\", "\\");
			result = Pattern.compile("\\\\").matcher(result).replaceAll(Matcher.quoteReplacement("\\\\"));
		}
		return result;
	}
	
    protected javax.swing.SwingWorker<Object, Object> exportToTable(final SpExportSchemaMapping theMapping, final boolean rebuildExistingTbl)
    {
        UsageTracker.incrUsageCount("SchemaExport.ExportToTable");
		final SpQuery exportQuery = theMapping.getMappings().iterator().next().getQueryField().getQuery();
		final Vector<QBDataSourceListenerIFace> dataSrcListeners = new Vector<QBDataSourceListenerIFace>();
		dataSrcListeners.add(this);
        
        Pair<String, String> it = null;
		if (rebuildExistingTbl)
		{
				it = DatabaseLoginPanel.getITUsernamePwd();
				if (it == null)
				{
					return null;
				}
		}
        
		rowsExported = 0;
		final boolean includeRecordIds = true;
        List<Specs> specs = getSpecs(theMapping, includeRecordIds, true, rebuildExistingTbl);
        if (specs == null)
        {
        	return null;
        }
        
        final List<ERTICaptionInfoQB> cols = specs.get(0).getCols();
        final HQLSpecs hql = specs.get(0).getSpecs();
        final String uniquenessHql = specs.get(0).getUniquenessHQL();
        final HQLSpecs uniquenessSpecs = specs.get(0).getUniquenessSpecs();
        
        //XXX need progress report for data acquisition step too.
                
        final DBConnection itDbConn = !rebuildExistingTbl ? null : getItDBConnection(it); 
        final Connection conn = !rebuildExistingTbl  ? DBConnection.getInstance().getConnection()
        		: itDbConn.getConnection();        
        javax.swing.SwingWorker<Object, Object> worker = new javax.swing.SwingWorker<Object, Object>()  {
        	private Exception killer = null;
        	private boolean success = false;
        	

			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#doInBackground()
			 */
			@Override
			protected Object doInBackground() throws Exception
			{
				try
				{
            		Pair<Boolean, Long> ucheck = QueryBldrPane.checkUniqueRecIds(uniquenessHql, uniquenessSpecs.getArgs());
					if (!ucheck.getFirst())
            		{
            			SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run()
							{
		            			UIRegistry.displayErrorDlg(UIRegistry.getResourceString("ExportPanel.DUPLICATE_KEYS_EXPORT"));
							}
            			});
            			return null;
            		}
					
            		if (rebuildExistingTbl)
            		{
            			cacheRowCount = ucheck.getSecond() - rowsExported;
            		} else
            		{
            			int m = 0;
            			while (maps.get(m) != theMapping) m++;
            			cacheRowCount = updateStats.get(m).getSecond();
            		}
					
            		BasicSQLUtils.update("update spexportschemamapping set TimestampExported = null where SpExportSchemaMappingID = " + theMapping.getId());
            		
            		boolean rebuild = rebuildExistingTbl;
            		boolean firstPass = true;
        			String actualTblName = ExportToMySQLDB.fixTblNameForMySQL(exportQuery.getName());
            		String bulkFilePath = useBulkLoad ? bulkFileDir + File.separator + actualTblName : null;
            		Connection loopConn = conn;
            		/* debug aid
            		ArrayList<Pair<Long, Double>> stats = new ArrayList<Pair<Long,Double>>(1000);
            		for (int i = 0; i < 1000; i++)
            		{
            			stats.add(new Pair<Long, Double>(-1L, -1.0));
            		}
            		*/
            		while (rowsExported < cacheRowCount)
            		{
            	        //long startTime = System.nanoTime();
            			QBDataSource src = new QBDataSource(hql.getHql(), hql.getArgs(), hql
            	                .getSortElements(), cols,
            	                includeRecordIds);
            	        for (QBDataSourceListenerIFace l : dataSrcListeners)
            	        {
            	        	src.addListener(l);
            	        }

            	        src.setFirstResult(rowsExported);
            	        src.setMaxResults(ExportPanel.maxExportRowCount);
            			src.startDataAcquisition();
            			loading();
            			
            			//XXX Assuming specimen-based export - 1 for baseTableId.
            			rowsExported += ExportToMySQLDB.exportToTable(loopConn, cols, src, exportQuery.getName(), dataSrcListeners, includeRecordIds, rebuild, 
            					!rebuildExistingTbl, 1, firstPass, bulkFilePath);
            			
            			rebuild = false;
            			firstPass = false;
            		}
            		if (useBulkLoad)
            		{
            			Statement bulkLoadStmt = loopConn.createStatement(); //May need work to ensure the loopConn has permission to execute "load data" 
            			try
        				{
        					//XXX if this fails, there's no need to roll back right?
            				bulkLoadStmt.executeUpdate("set character_set_database='utf8'");
            				String bulkFileSql = "load data local infile '" + adjustPathForWindows(bulkFilePath) + 
        							"'into table " + actualTblName + " fields terminated by '\\t' optionally enclosed by '\\''"; 
        					bulkLoadStmt.executeUpdate(bulkFileSql);
        					FileUtils.delete(new File(bulkFilePath));
        					//fileLoaded = true;
        				} finally
        				{
        					//leave the file on disk in case of bulkLoad failure.
        					
        					bulkLoadStmt.close();
        				}
            		}
        			
					boolean transOpen = false;
					DataProviderSessionIFace theSession = DataProviderFactory.getInstance().createSession();;
			        try
			        {
			        	SpExportSchemaMapping mergedMap = theSession.merge(theMapping);
			        	mergedMap.setTimestampExported(new Timestamp(System.currentTimeMillis()));
			        	theSession.beginTransaction();
			        	transOpen = true;
			        	theSession.saveOrUpdate(mergedMap);
			        	theSession.commit();
			        	transOpen = false;
			        }
			        catch (Exception ex)
			        {
			        	//UIRegistry.displayStatusBarErrMsg(getResourceString("QB_DBEXPORT_ERROR_SETTING_EXPORT_TIMESTAMP"));
			        	if (transOpen)
			        	{
			        		theSession.rollback();
			        	}
			        	throw ex;
			        }
			        finally
			        {
			        	theSession.close();
			        }
					success = true;
				}
				catch (Exception ex)
				{
					killer = ex;
				}
				return null;
			}

			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#done()
			 */
			@Override
			protected void done()
			{
				if (success)
				{
					stupid = 0;
					for (QBDataSourceListenerIFace listener : dataSrcListeners)
					{
						listener.done(rowsExported);
					}
				}
				else
				{
					String msg = getResourceString("ExportPanel.UpdateFailMsg");
					if (killer != null)
					{
						killer.printStackTrace();
						msg += " Error: " + killer.getClass().getSimpleName();
						if (StringUtils.isNotBlank(killer.getLocalizedMessage()))
						{
							msg += " (" + killer.getLocalizedMessage() + ")";
						}
					}
					else
					{
						msg += ".";
					}
					UIRegistry.displayErrorDlg(msg);
					for (QBDataSourceListenerIFace l : dataSrcListeners)
					{
						l.done(-1);
					}
				}
				if (itDbConn != null)
				{
					itDbConn.close();
				}
			}
        	
        };
        
        return worker;
    }

    /**
     * @param it
     * @return a DBConnection created with the it username and password pair
     */
    protected DBConnection getItDBConnection(Pair<String, String> it)
    {
    	DBConnection dbc    = DBConnection.getInstance();
        return DBConnection.createInstance(dbc.getDriver(), 
                                                          dbc.getDialect(), 
                                                          dbc.getDatabaseName(), 
                                                          dbc.getConnectionStr(), 
                                                          it.getFirst(), 
                                                          it.getSecond());
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
				prog.setValue((int )currentRow);
				//System.out.println(currentRow);
			}
		});
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#isListeningClosely()
	 */
	@Override
	public boolean isListeningClosely() 
	{
		return !exportIsThreaded;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#done(int)
	 */
	@Override
	public void done(long rows)
	{
		if (rows != -1 && rowsExported + rows < cacheRowCount)
		{
			prog.setValue(0);
			return;
		}
		
		unlock(mapUpdating.get());
		if (rows == -1 || (stupid == 0 && mapUpdating.get() != -1))
		{
			final long frows = rows;
			
			SwingUtilities.invokeLater(new Runnable() {

				/* (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				@Override
				public void run()
				{
					if (updater != null)
					{
						if (frows != -1)
						{
							UIRegistry.displayInfoMsgDlgLocalized("ExportPanel.UpdateSuccess");
							status.setText(UIRegistry.getResourceString("ExportPanel.CacheUpdated"));
						}
						else
						{
							UIRegistry.displayInfoMsgDlgLocalized("ExportPanel.UpdateFailMsg");
							status.setText(UIRegistry.getResourceString("ExportPanel.UpdateFail"));
						}
					}
					else
					{
						if (frows != -1)
						{
							status.setText(String.format(UIRegistry.getResourceString("ExportPanel.ExportDone"), rowCount));
						}
						else
						{
							UIRegistry.displayInfoMsgDlgLocalized("ExportPanel.ExportFailMsg");
							status.setText(UIRegistry.getResourceString("ExportPanel.ExportFail"));
						}
					}
					if (updater != null && frows != -1)
					{
						refreshUpdatedMapDisplay(mapUpdating.get());
					}
					((CardLayout )progPane.getLayout()).first(progPane);
					prog.setValue(0);
					mapUpdating.set(-1);
					updateBtnStates();
					updater = null;
					dumper = null;
				}
			});
			
		}
		stupid--;
	}

	/**
	 * 
	 */
	protected void refreshUpdatedMapDisplay(int mapToRefresh)
	{
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
        	SpExportSchemaMapping newMap = session.get(SpExportSchemaMapping.class,	maps.get(mapToRefresh).getId());
			newMap.forceLoad();
			newMap.getMappings().iterator().next().getQueryField().getQuery().forceLoad();
			if (newMap.getSpExportSchema() != null)
			{
				newMap.getSpExportSchema().forceLoad();
			}
	        maps.set(mapToRefresh, newMap);
       }
        finally
        {
        	session.close();
        }
        mapsModel.setValueAt(maps.get(mapToRefresh).getTimestampExported().toString(), mapToRefresh, 1);
        mapsModel.setValueAt(UIRegistry.getResourceString("ExportPanel.Uptodate"), mapToRefresh, 2);
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#filling()
	 */
	@Override
	public void filling()
	{
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (rowCount > 0 && prog.isIndeterminate()) {
                    prog.setIndeterminate(false);
                    prog.setValue(0);
                }
                if (rowCount >= 0 && rowCount < cacheRowCount) {
                    status.setText(String.format(UIRegistry.getResourceString("ExportPanel.UpdatingCacheChunk"), rowsExported + 1, rowsExported + rowCount, cacheRowCount));
                } else {
                    status.setText(UIRegistry.getResourceString("ExportPanel.UpdatingCache"));
                }
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
				//System.out.println("loaded");
				status.setText(UIRegistry.getResourceString("ExportPanel.DataRetrieved")); 
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
				prog.setIndeterminate(true);
				//System.out.println("loading");
				status.setText(UIRegistry.getResourceString("ExportPanel.RetrievingData")); 
			}
		});
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#rowCount(int)
	 */
	@Override
	public void rowCount(final long rowCountArg)
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
			{
				if (rowCountArg > 0)
				{
					prog.setIndeterminate(false);
					prog.setMinimum(0);
					prog.setMaximum((int )rowCountArg - 1);
				}
				ExportPanel.this.rowCount = rowCountArg;
			}
		});
	}

	protected void getMappingStatus(final SpExportSchemaMapping map)
	{
		javax.swing.SwingWorker<MappingUpdateStatus, Object> worker = new javax.swing.SwingWorker<MappingUpdateStatus, Object>() {

			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#doInBackground()
			 */
			@Override
			protected MappingUpdateStatus doInBackground() throws Exception
			{
				try
				{
					Connection conn = DBConnection.getInstance().createConnection();
					Statement stmt = conn.createStatement();
					try
					{
						MappingUpdateStatus result = null;
						String tbl = getCacheTableName(map.getMappingName());
						String keyFld = tbl + "Id";
						SpQuery q = map.getMappings().iterator().next().getQueryField().getQuery();
						DBTableInfo rootTbl = DBTableIdMgr.getInstance().getInfoById(q.getContextTableId());
						String spTbl = rootTbl.getName();
						String spKeyFld = rootTbl.getIdColumnName();
						String sql = "select count(*) from " + tbl + " where " + keyFld 
							+ " not in(select " + spKeyFld + " from " + spTbl;
						
						//XXX Collection Scoping Issue???
						if (rootTbl.getFieldByName("collectionMemberId") != null)
						{
							sql += " where CollectionMemberId = " 
								+ AppContextMgr.getInstance().getClassObject(Collection.class).getId();
						}
						
						sql += ")";
						int deletedRecs = BasicSQLUtils.getCountAsInt(conn, sql);
						int otherRecs = 0; 
						
						HQLSpecs hql = getSpecs(map, true, false, false).get(0).getSpecs();
						DataProviderSessionIFace theSession = DataProviderFactory.getInstance().createSession();
				        try
				        {
				        	QueryIFace query = theSession.createQuery(hql.getHql(), false);
			                if (hql.getArgs() != null)
			                {
			                    for (Pair<String, Object> param : hql.getArgs())
			                    {
			                    	query.setParameter(param.getFirst(), param.getSecond());
			                    }
			                }
			                otherRecs = query.list().size();
				        } finally
				        {
				        	theSession.close();
				        }
				        result = new MappingUpdateStatus(deletedRecs, 0, 0, deletedRecs + otherRecs);
						return result;
					} finally
					{
						stmt.close();
						conn.close();
					}
				} catch (Exception ex)
				{
		            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
		            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExportPanel.class, ex);
		            throw new RuntimeException(ex);
				}
			}

			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#done()
			 */
			@Override
			protected void done()
			{
				super.done();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						try
						{
							displayStatusForMap(map, get());
							boolean enable = true;
							for (Pair<SpExportSchemaMapping, Long> sm : updateStats)
							{
								if (sm.getSecond() == -2L) 
								{
									enable = false;
									break;
								}
							}
							exportToDbTblBtn.setEnabled(enable);
						} catch (Exception ex)
						{
				            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
				            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExportPanel.class, ex);
				            throw new RuntimeException(ex);
						}
					}
				});
			}
			
		};
		worker.execute();
	}
	
	
	public class MappingUpdateStatus
	{
		protected final long recsToDelete;
		protected final long recsUpdated;
		protected final long recsAdded;
		protected final long totalRecsChanged;
		/**
		 * @param recsToDelete
		 * @param recsUpdated
		 * @param recsAdded
		 * @param totalRecsChanged
		 */
		public MappingUpdateStatus(long recsToDelete, long recsUpdated,
				long recsAdded, long totalRecsChanged)
		{
			super();
			this.recsToDelete = recsToDelete;
			this.recsUpdated = recsUpdated;
			this.recsAdded = recsAdded;
			this.totalRecsChanged = totalRecsChanged;
		}
		/**
		 * @return the recsToDelete
		 */
		public long getRecsToDelete()
		{
			return recsToDelete;
		}
		/**
		 * @return the recsUpdated
		 */
		public long getRecsUpdated()
		{
			return recsUpdated;
		}
		/**
		 * @return the recsAdded
		 */
		public long getRecsAdded()
		{
			return recsAdded;
		}
		/**
		 * @return the totalRecsChanged
		 */
		public long getTotalRecsChanged()
		{
			return totalRecsChanged;
		}
		
		
	}
	
	private static void startUp()
	{
        if (UIRegistry.isEmbedded())
        {
            ProcessListUtil.checkForMySQLProcesses(new ProcessListener()
            {
                @Override
                public void done(PROC_STATUS status) // called on the UI thread
                {
                    if (status == PROC_STATUS.eOK || status == PROC_STATUS.eFoundAndKilled)
                    {
                        startupContinuing(); // On UI Thread
                    }
                }
            });
        } else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    startupContinuing();
                }
            });
        }
    }
    
    /**
     * 
     */
    private static void startupContinuing() // needs to be called on the UI Thread
    {
        final AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        adjustLocaleFromPrefs();
        final String iRepPrefDir = localPrefs.getDirPath(); 
        int mark = iRepPrefDir.lastIndexOf(UIRegistry.getAppName(), iRepPrefDir.length());
        final String SpPrefDir = iRepPrefDir.substring(0, mark) + "Specify";

        DatabaseLoginPanel.MasterPasswordProviderIFace usrPwdProvider = new DatabaseLoginPanel.MasterPasswordProviderIFace()
        {
            @Override
            public boolean hasMasterUserAndPwdInfo(final String username, final String password, final String dbName)
            {
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password))
                {
                    UserAndMasterPasswordMgr.getInstance().set(username, password, dbName);
                    
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
            public Pair<String, String> getUserNamePassword(final String username, final String password, final String dbName)
            {
                UserAndMasterPasswordMgr.getInstance().set(username, password, dbName);
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
            public boolean editMasterInfo(final String username, final String dbName, final boolean askFroCredentials)
            {
                boolean result = false;
                try
                {
                    try
                    {
                        AppPreferences.getLocalPrefs().flush();
                        AppPreferences.getLocalPrefs().setDirPath(SpPrefDir);
                        AppPreferences.getLocalPrefs().setProperties(null);
                        result =  UserAndMasterPasswordMgr.getInstance().editMasterInfo(username, dbName, askFroCredentials);
                    } finally
                    {
                        AppPreferences.getLocalPrefs().flush();
                        AppPreferences.getLocalPrefs().setDirPath(iRepPrefDir);
                        AppPreferences.getLocalPrefs().setProperties(null);
                    }
                } catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, e);
                    result = false;
                }
                return result;
           }
        };
        String nameAndTitle = UIRegistry.getResourceString("SchemaExportLauncher.DlgTitle"); // I18N
        UIRegistry.setRelease(true);
        UIHelper.doLogin(usrPwdProvider, true, false, false, new SchemaExportLauncher(), Specify.getLargeIconName(), nameAndTitle, nameAndTitle, "SpecifyWhite32", "login"); 
        
        localPrefs.load();

	}
	
    public static void main(String[] args)
    {
        log.debug("********* Current ["+(new File(".").getAbsolutePath())+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // This is for Windows and Exe4J, turn the args into System Properties
 
        // Set App Name, MUST be done very first thing!
        //UIRegistry.setAppName("SchemaExporter");  //$NON-NLS-1$
        UIRegistry.setAppName("Specify");  //$NON-NLS-1$
        
        AppBase.processArgs(args);
        AppBase.setupTeeForStdErrStdOut(true, false);

        
        // Then set this
        IconManager.setApplicationClass(Specify.class);
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml")); //$NON-NLS-1$
        
        Specify.setUpSystemProperties();
        
        HibernateUtil.setListener("post-commit-update", new edu.ku.brc.specify.dbsupport.PostUpdateEventListener()); //$NON-NLS-1$
        HibernateUtil.setListener("post-commit-insert", new edu.ku.brc.specify.dbsupport.PostInsertEventListener()); //$NON-NLS-1$
        HibernateUtil.setListener("post-commit-delete", new edu.ku.brc.specify.dbsupport.PostDeleteEventListener()); //$NON-NLS-1$
 
        ImageIcon helpIcon = IconManager.getIcon(Specify.getIconName(),IconSize.Std16); //$NON-NLS-1$
        HelpMgr.initializeHelp("SpecifyHelp", helpIcon.getImage()); //$NON-NLS-1$

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
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExportPanel.class, e);
                    log.error("Can't change L&F: ", e); //$NON-NLS-1$
                }
                
                startUp(); 
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
    
    private class Specs 
    {
    	protected final HQLSpecs specs; 
    	protected final List<ERTICaptionInfoQB> cols;
    	protected final String uniquenessHQL;
    	protected final HQLSpecs uniquenessSpecs;
    	
    	public Specs(HQLSpecs specs, List<ERTICaptionInfoQB> cols, String uniquenessHQL, HQLSpecs uniquenessSpecs)
    	{
    		this.specs = specs;
    		this.cols = cols;
    		this.uniquenessHQL = uniquenessHQL;
    		this.uniquenessSpecs = uniquenessSpecs;
    	}

		public HQLSpecs getSpecs()
		{
			return specs;
		}

		public List<ERTICaptionInfoQB> getCols()
		{
			return cols;
		}

		public String getUniquenessHQL()
		{
			return uniquenessHQL;
		}
    	
    	public HQLSpecs getUniquenessSpecs()
    	{
    		return uniquenessSpecs == null ? specs : uniquenessSpecs;
    	}
    }
}
