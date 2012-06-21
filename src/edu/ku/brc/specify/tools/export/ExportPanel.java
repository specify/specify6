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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

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
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.SpExportSchemaItemMapping;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpQueryField;
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
import edu.ku.brc.specify.ui.AppBase;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconManager.IconSize;
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

    protected static final String EXPORT_TEXT_PATH = "ExportPanel.TabDelimExportPath";
    
    protected static final long maxExportRowCount = 100000;
    
    protected JTable mapsDisplay;
	protected DefaultTableModel mapsModel;
	protected JButton exportToDbTblBtn;
	protected JButton exportToTabDelimBtn;
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
	protected int mapUpdating = -1;
	protected int stupid = -1;
	protected javax.swing.SwingWorker<Object, Object> updater = null;
	protected javax.swing.SwingWorker<Object, Object> dumper = null;
	
	protected final List<SpExportSchemaMapping> maps;
	protected final List<Pair<SpExportSchemaMapping, Long>> updateStats;
	
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
			updateStats.add(new Pair<SpExportSchemaMapping, Long>(map, -1L));	
		}
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
			if (!rebuildForRow(row++))
			{
				getMappingStatus(map);
			}
		}
	}
	
	public static String getCacheTableName(String mappingName)
	{
		//return  ExportToMySQLDB.fixTblNameForMySQL(mappingName + AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionName());
		return  ExportToMySQLDB.fixTblNameForMySQL(mappingName);
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
					mapUpdating = row;
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
							exportToDbTblBtn.setEnabled(false);
							exportToTabDelimBtn.setEnabled(false);

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
					mapUpdating = row;
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
						SpExportSchemaMapping map = maps.get(row);
						String iptSQL = ExportToMySQLDB
								.getSelectForIPTDBSrc(getCacheTableName(map.getMappingName()));
						JTextArea ta = new JTextArea(iptSQL);
						ta.setLineWrap(true);
						ta.setColumns(60);
						ta.setRows(10);
						ta.selectAll();
						JScrollPane scrp = new JScrollPane(ta);
						CustomDialog cd = new CustomDialog((Frame) UIRegistry
								.getTopWindow(), UIRegistry
								.getResourceString("ExportPanel.SQLTitle"),
								true, scrp);
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
    	
        PanelBuilder pbb = new PanelBuilder(new FormLayout("2dlu, f:p:g, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu", "p, p, 7dlu"));
    	status = new JLabel(UIRegistry.getResourceString("ExportPanel.InitialStatus")); 
    	status.setFont(status.getFont().deriveFont(Font.ITALIC));
    	Dimension pref = status.getPreferredSize();
    	pref.setSize(Math.max(300, pref.getWidth()), pref.getHeight());
    	status.setPreferredSize(pref);
    	pbb.add(status, cc.xy(2, 1));
    	pbb.add(this.showIPTSQLBtn, cc.xy(3, 1));
    	pbb.add(exportToTabDelimBtn, cc.xy(7, 1));
    	pbb.add(exportToDbTblBtn, cc.xy(5, 1));
    	pbb.add(helpBtn, cc.xy(9, 1));
    	
    	progPane = new JPanel(new CardLayout());
    	progPane.add(new JPanel(), "blank");
    	prog = new JProgressBar();
    	progPane.add(prog, "prog");
    	pbb.add(progPane, cc.xyw(2, 2, 6));
    	//prog.setVisible(false);
    	add(pbb.getPanel(), BorderLayout.SOUTH);

        HelpMgr.setAppDefHelpId("schema_tool");

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
		if (mapUpdating != -1)
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
	
	/**
	 * @param map
	 * @return text description of update status for map.
	 */
	protected String getInitialMapStatusTextAndCheckExportBtnEnable(SpExportSchemaMapping map)
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
			row.add(getInitialMapStatusTextAndCheckExportBtnEnable(map));
			
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
        	if (getNumberColumnsInCache(map) - 1 != map.getMappings().size())
        	{
        		return true;
        	}
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
        QueryParameterPanel qpp = new QueryParameterPanel();
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
					
            		boolean rebuild = rebuildExistingTbl;
            		if (rebuildExistingTbl)
            		{
            			cacheRowCount = ucheck.getSecond() - rowsExported;
            		} else 
            		{
            			int m = 0;
            			while (maps.get(0) != theMapping) m++;
            			cacheRowCount = updateStats.get(m).getSecond();
            		}
            		boolean firstPass = true;
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
            			rowsExported += ExportToMySQLDB.exportToTable(loopConn, cols, src, exportQuery.getName(), dataSrcListeners, includeRecordIds, rebuild, !rebuildExistingTbl, 1, firstPass);
            			
            			
            			rebuild = false;
            			firstPass = false;
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
		
		unlock(mapUpdating);
		if (rows == -1 || (stupid == 0 && mapUpdating != -1))
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
						refreshUpdatedMapDisplay(mapUpdating);
					}
					((CardLayout )progPane.getLayout()).first(progPane);
					prog.setValue(0);
					exportToDbTblBtn.setEnabled(true);
					exportToTabDelimBtn.setEnabled(true);
					mapUpdating = -1;
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
			newMap.getSpExportSchema().forceLoad();
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
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
			{
				if (rowCount > 0 && prog.isIndeterminate())
				{
					prog.setIndeterminate(false);
					prog.setValue(0);
				}
				if (rowCount < cacheRowCount) 
				{
					status.setText(String.format(UIRegistry.getResourceString("ExportPanel.UpdatingCacheChunk"), rowsExported+1, rowsExported + rowCount, cacheRowCount));
				} else
				{
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
					/* (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run()
					{
						try
						{
							displayStatusForMap(map, get());
							exportToDbTblBtn.setEnabled(true);
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

        
        
        System.setProperty(AppContextMgr.factoryName,                   "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr //$NON-NLS-1$
        System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences //$NON-NLS-1$
        System.setProperty(AppPreferences.factoryGlobalName,            "edu.ku.brc.specify.config.AppPrefsGlobalDBIOIImpl");         // Needed by AppReferences //$NON-NLS-1$
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
                UIHelper.doLogin(usrPwdProvider, true, false, false, new SchemaExportLauncher(), Specify.getLargeIconName(), nameAndTitle, nameAndTitle, "SpecifyWhite32", "login"); // true
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
