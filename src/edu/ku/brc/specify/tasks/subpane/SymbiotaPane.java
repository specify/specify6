/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.FileUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace.PartialDateEnum;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;
import edu.ku.brc.specify.datamodel.SpSymbiotaInstance;
import edu.ku.brc.specify.plugins.morphbank.DarwinCoreArchive;
import edu.ku.brc.specify.tasks.StartUpTask;
import edu.ku.brc.specify.tasks.SymbiotaTask;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace;
import edu.ku.brc.specify.tools.export.ExportPanel;
import edu.ku.brc.specify.tools.export.ExportToMySQLDB;
import edu.ku.brc.specify.tools.export.MappingUpdateStatus;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
@SuppressWarnings("serial")
public class SymbiotaPane extends BaseSubPane implements QBDataSourceListenerIFace
{
    //private static final Logger log  = Logger.getLogger(SymbiotaPane.class);
    
    protected static String SymbiotaViewSetName = "SystemSetup";
	protected static String SymbiotaViewName = "SymbiotaPane";
    
	protected enum OverallStatuses{Good, CacheNotCreated, CacheNeedsRebuild, DataToPush, DataToPull};
    protected enum OverallStatusAlert{GREEN, YELLOW, RED};

//    protected ViewIFace formView = null;
//    protected Viewable form = null;
//    protected Hashtable<String, Object> dataMap = new Hashtable<String, Object>();

	protected JLabel instanceName;
    protected JLabel overallStatus;
    protected JLabel instanceMapping;
    protected JLabel description;
    protected JLabel key;
	protected JLabel lastSendToSymLbl;
    protected JLabel lastSendToSym;
    protected JLabel lastGetFromSymLbl;
    protected JLabel lastGetFromSym;
    protected JLabel dbCacheStatusLbl;
    protected JLabel dbCacheStatus;
    protected JLabel dbCacheCreatedLbl;
    protected JLabel dbCacheCreated;
    protected JLabel unsentTotalChangesLbl;
    protected JLabel unsentTotalChanges;
    protected JLabel unsentNewOrEditedRecsLbl;
    protected JLabel unsentNewOrEditedRecs;
    protected JLabel unsentDelRecsLbl;
    protected JLabel unsentDelRecs;
    

    protected JPanel mainStatsPane;
    protected JPanel introPane;
    protected JPanel activePane = null;
    
    protected JPanel statsPanel;
    protected JPanel cmdPanel;
    protected JButton pushBtn;
    protected JButton archiveBtn;
    protected JButton pullBtn;
    
    protected SymbiotaTask symTask;
    protected AtomicReference<MappingUpdateStatus> mapStatus = new AtomicReference<MappingUpdateStatus>(null);
    protected boolean useCache = true;
    
    protected List<Integer> deletedRecsForCurrentUpdate = new ArrayList<Integer>();
    protected Set<Integer> newOrChangedRecsForCurrentUpdate = new HashSet<Integer>();
    
    protected AtomicBoolean updateCacheAfterStatusUpdate = new AtomicBoolean(false);
    protected AtomicBoolean buildArchiveAfterCacheUpdate = new AtomicBoolean(false);
    protected AtomicBoolean sendAfterArchiveBuild = new AtomicBoolean(false);
    protected AtomicReference<String> archiveFileName = new AtomicReference<String>(null);
    
    protected ProgressDialog progDlg = null;
    
    protected List<JLabel> loadingIcons = new ArrayList<JLabel>();
    
    protected String progDlgTitleKey = "SymbiotaPane.SENDING_DLG";
    
    protected AtomicReference<SymWorker<MappingUpdateStatus, Object>> statsGetter = new AtomicReference<SymWorker<MappingUpdateStatus, Object>>(null);
    protected AtomicReference<SymWorker<?, Object>> activeWorker = new AtomicReference<SymWorker<?, Object>>(null);
    
    
	/**
	 * @param name
	 * @param task
	 */
	public SymbiotaPane(final String name, final Taskable task) {
        super(name, task);
        symTask = (SymbiotaTask)task;
//        formView = AppContextMgr.getInstance().getView(SymbiotaViewSetName, SymbiotaViewName);
//        if (formView != null)
//        {
//            form = ViewFactory.createFormView(null, formView, null, dataMap, MultiView.NO_OPTIONS, null);
//
//        } else
//        {
//            log.error("Couldn't load form with name ["+SymbiotaViewSetName+"] Id ["+SymbiotaViewName+"]");
//        }
		UsageTracker.incrUsageCount(symTask.getName() + "." + "CreateStartPane");
        createUI();
	}
	
	protected JPanel buildInfoPanel() {
        PanelBuilder tpb = new PanelBuilder(new FormLayout("f:p:g", "p, 3dlu, p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,5dlu"));
        CellConstraints cc = new CellConstraints();
        
		//Icon icon = new ImageIcon("/home/timo/Downloads/3D_hand.gif");
		//JLabel iconLbl = new JLabel(icon);
		//loadingIcons.add(iconLbl);
        tpb.add(tpb.getComponentFactory().createSeparator(UIRegistry.getResourceString("SymbiotaPane.SymbiotaInstance"), 
        		SwingConstants.LEFT), cc.xy(1, 1));
        tpb.add(getStatPane(UIHelper.createLabel(UIRegistry.getResourceString("SymbiotaTask.InstanceName")),
        		instanceName, /*iconLbl*/null),  
        		cc.xy(1, 3));
        tpb.add(getStatPane(UIHelper.createLabel(UIRegistry.getResourceString("SymbiotaPane.OverallStatusLbl")),
        		overallStatus, null),
        		cc.xy(1, 5));
        
        tpb.add(getStatPane(UIHelper.createLabel(UIRegistry.getResourceString("SymbiotaPane.InstanceDescription")),  
        		description, null),  
        		cc.xy(1, 7));
        tpb.add(getStatPane(UIHelper.createLabel(UIRegistry.getResourceString("SymbiotaPane.InstanceKey")),
        		key, null),  
        		cc.xy(1, 9));
        tpb.add(getStatPane(UIHelper.createLabel(UIRegistry.getResourceString("SymbiotaPane.MappingName")),
        		instanceMapping, null),  
        		cc.xy(1, 11));
		
        return tpb.getPanel();
	}
	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#getHelpTarget()
	 */
	@Override
	public String getHelpTarget() {
		if (activePane == mainStatsPane) {
			return "connector_info";
		} else {
			return super.getHelpTarget();
		}
	}

	protected JPanel buildStatsPanel() {
        PanelBuilder bpb = new PanelBuilder(new FormLayout("f:p:g","p, 3dlu, p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu"));
        CellConstraints cc = new CellConstraints();
        		
        bpb.add(bpb.getComponentFactory().createSeparator("Current Status", SwingConstants.LEFT), cc.xy(1, 1));
        
		bpb.add(getStatPane(lastSendToSymLbl, lastSendToSym, null), cc.xy(1,3));
		//bpb.add(getStatPane(lastGetFromSymLbl, lastGetFromSym, null), cc.xy(1,5));
		bpb.add(getStatPane(dbCacheStatusLbl, dbCacheStatus, null), cc.xy(1,5));
		bpb.add(getStatPane(dbCacheCreatedLbl, dbCacheCreated, null), cc.xy(1,7));
		bpb.add(getStatPane(unsentTotalChangesLbl, unsentTotalChanges, null), cc.xy(1,9));
		bpb.add(getStatPane(unsentNewOrEditedRecsLbl, unsentNewOrEditedRecs, null), cc.xy(1,11));
		bpb.add(getStatPane(unsentDelRecsLbl, unsentDelRecs, null), cc.xy(1,13));
		
		return bpb.getPanel();
	}
	

	/**
	 * 
	 */
//	protected void initDataMap() {
//		dataMap.put("instanceName", "?");
//		dataMap.put("overallStatus", "?");
//		dataMap.put("instanceMapping", "?");
//		dataMap.put("description", "?");
//		dataMap.put("key", "?");
//		dataMap.put("lastSendToSym", "?");
//		dataMap.put("lastGetFromSym", "?");
//		dataMap.put("dbCacheStatus", "?");
//		dataMap.put("dbCacheCreated", "?");
//		dataMap.put("unsentTotalChanges", "?");
//		dataMap.put("unsentNewOrEditedRecs", "?");
//		dataMap.put("unsentDelRecs", "?");
//	}
	
	protected void clearSelection() {
		SwingUtilities.invokeLater(new Runnable() {

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
			    instanceName.setText("?");
			    overallStatus.setText("?");
			    instanceMapping.setText("?");
			    description.setText("?");
			    key.setText("?");
			    lastSendToSym.setText("?");
			    lastGetFromSym.setText("?");
			    dbCacheStatus.setText("?");
			    dbCacheCreated.setText("?");
			    unsentTotalChanges.setText("?");
			    unsentNewOrEditedRecs.setText("?");
			    unsentDelRecs.setText("?");
			    
//			    initDataMap();
//			    form.setDataIntoUI();
			}
			
		});
	}
	
	/**
	 * 
	 */
//	protected void setSelectionToDataMap() {
//		SpSymbiotaInstance sym = symTask.getTheInstance();
//		if (sym != null) {
//			dataMap.put("instanceName", sym.getInstanceName());
//			dataMap.put("instanceMapping", sym.getSchemaMapping().getMappingName());
//			dataMap.put("description", sym.getDescription() == null ? "" : sym.getDescription());
//			dataMap.put("key", sym.getSymbiotaKey());
//			UIFieldFormatterIFace dateFormatter = UIFieldFormatterMgr.getInstance().getDateFormatter(PartialDateEnum.Full);
//			if (sym.getLastPush() != null) {
//				dataMap.put("lastSendToSym", dateFormatter.formatToUI(sym.getLastPush()).toString());
//			} else {
//				dataMap.put("lastSendToSym", UIRegistry.getResourceString("SymbiotaPane.Never"));
//			}
//			if (sym.getLastPull() != null) {
//				dataMap.put("lastGetFromSym", dateFormatter.formatToUI(sym.getLastPull()).toString());
//			} else {
//				dataMap.put("lastGetFromSym", UIRegistry.getResourceString("SymbiotaPane.Never"));
//			}
//		}
//	}
	
	/**
	 * 
	 */
	protected void setSelection() {
		SwingUtilities.invokeLater(new Runnable() {

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				SpSymbiotaInstance sym = symTask.getTheInstance();
				if (sym != null) {
					showPane(mainStatsPane);
					instanceName.setText(sym.getInstanceName());
					instanceMapping.setText(sym.getSchemaMapping().getMappingName());
					description.setText(sym.getDescription());
					key.setText(sym.getSymbiotaKey());
					UIFieldFormatterIFace dateFormatter = UIFieldFormatterMgr.getInstance().getDateFormatter(PartialDateEnum.Full);
					if (sym.getLastPush() != null) {
						lastSendToSym.setText(dateFormatter.formatToUI(sym.getLastPush()).toString());
					} else {
			    	lastSendToSym.setText(UIRegistry.getResourceString("SymbiotaPane.Never"));
					}
					if (sym.getLastPull() != null) {
						lastGetFromSym.setText(dateFormatter.formatToUI(sym.getLastPull()).toString());
					} else {
						lastGetFromSym.setText(UIRegistry.getResourceString("SymbiotaPane.Never"));
					}
					
//					setSelectionToDataMap();
//					form.setDataIntoUI();
					
				} else {
					showPane(introPane);
				}
			}
			
		});
	}

	
	/**
	 * 
	 */
	public void instanceSelected() {
		SwingUtilities.invokeLater(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				clearSelection();
				setSelection();
				for (JLabel l : loadingIcons) {
					l.setVisible(true);
				}
			}
		});
		getStats(false);			
	}
	
	/**
	 * @return
	 */
	protected String getArchiveFileName() {
		if (archiveFileName.get() != null) {
			return archiveFileName.get();
		} else {
			return getDefaultArchiveFileName();	
		}
	}
	
	/**
	 * @return
	 */
	protected String getDefaultArchiveFileName() {
		return UIRegistry.getAppDataDir() + File.separator + "SymbiotaDwcArchive.zip";
	}
	/**
	 * 
	 */
	protected void getStats(final boolean showProgress) {
		if (statsGetter.get() != null) {
			//System.out.println("cancelling active stats getter");
			statsGetter.get().setIgnoreResult(true);
			statsGetter.get().cancel(true); //true can cause exceptions --- which probably won't percolate up to the UI, and
										//probably won't cause any issues, at least for the statsGetter, updates are another story
			statsGetter.set(null);
		}
		
		SymWorker<MappingUpdateStatus, Object> worker = new SymWorker<MappingUpdateStatus, Object>("getStats") {

			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#doInBackground()
			 */
			@Override
			protected MappingUpdateStatus doInBackground() throws Exception {
				if (symTask.getSchemaMapping() != null) {
					return ExportPanel.retrieveMappingStatus(symTask.getSchemaMapping(), getOverrideTimestamp());
				} else {
					return null;
				}
			}

			/* (non-Javadoc)
			 * @see edu.ku.brc.specify.tasks.subpane.SymbiotaPane.SymWorker#doDone()
			 */
			@Override
			protected void doDone() {
				try {
					//System.out.println("Done getting stats");
					updateStats(get());
					if (getVerificationFromUserIfNecessary()) {
						if (updateCacheAfterStatusUpdate.get()) {
							updateCacheAfterStatusUpdate.set(false);
							updateAndBuildArchive();
						} 
					} else {
						updateCacheAfterStatusUpdate.set(false);
						buildArchiveAfterCacheUpdate.set(false);
						sendAfterArchiveBuild.set(false);
						hideProgDlg();
						instanceSelected();
						setUIEnabled(true);	
					}
				} catch (Exception ignore) {					
				}
			}			
		};	
		statsGetter.set(worker);
		worker.execute();
	}
	
	
	/**
	 * @return
	 */
	protected boolean getVerificationFromUserIfNecessary() {
		boolean result = true;
		OverallStatusAlert al = getOverallStatusAlertLevel(getOverallStatus(mapStatus.get(), symTask.getTheInstance()));
		if (sendAfterArchiveBuild.get() && al.equals(OverallStatusAlert.GREEN)) {
			boolean onTop = false;
			if (progDlg.isVisible() && progDlg.isAlwaysOnTop()) {
				progDlg.setAlwaysOnTop(false);
				onTop = true;
			}
			result = UIRegistry.displayConfirmLocalized("SymbiotaPane.ConfirmSend", "SymbiotaPane.ConfirmSendAllWhenNoneToSend",	 "Yes", "No", JOptionPane.QUESTION_MESSAGE);
			if (onTop) {
				progDlg.setAlwaysOnTop(true);
			}
		} 
		return result;
	}
	/**
	 * @param stats
	 */
	protected void updateStats(MappingUpdateStatus stats) {
		mapStatus.set(stats);
		if (mapStatus.get() != null) {
			updateStatsDisplay();
		}
	}
	
	/**
	 * @param instance
	 * @param status
	 * @return
	 */
	protected boolean needToSendAllRecs(SpSymbiotaInstance instance, MappingUpdateStatus status) {
		boolean neverPushed = instance.getLastPush() == null;
		boolean needsRebuild = status.isNeedsRebuild();
		boolean builtSinceLastPush = needsRebuild || 
				instance.getSchemaMapping().getTimestampExported() == null || 
				(neverPushed && instance.getSchemaMapping().getTimestampExported() != null) ||
				(!neverPushed && instance.getLastCacheBuild() != null && instance.getLastCacheBuild().after(instance.getLastPush()));
		return neverPushed || needsRebuild || builtSinceLastPush;
	}
	
	protected void updateStatsToDataMap() {
		
	}
	/**
	 * 
	 */
	protected void updateStatsDisplay() {
		SwingUtilities.invokeLater(new Runnable() {

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				SpSymbiotaInstance instance = symTask.getTheInstance();
				MappingUpdateStatus ms = mapStatus.get();
				updateOverallStatus(ms, instance);
				dbCacheStatus.setText(ms.isNeedsRebuild() ? UIRegistry.getResourceString("SymbiotaPane.CacheNeedsRebuild") 
						: UIRegistry.getResourceString("SymbiotaPane.CacheOK"));
				dbCacheCreated.setText(ms.isNeedsRebuild() || instance.getSchemaMapping().getTimestampExported() == null ? UIRegistry.getResourceString("SymbiotaPane.CacheNotCreated") 
						: UIFieldFormatterMgr.getInstance().getDateFormatter(PartialDateEnum.Full).formatToUI(instance.getSchemaMapping().getTimestampExported()).toString());
				boolean needToSendAll = needToSendAllRecs(instance, ms); 
				String totalRecsInCache = needToSendAll ? ms.isNeedsRebuild() ? "?" : String.valueOf(getTotalNumberOfRecsInCache(instance)) : "Aucune importance";
				unsentTotalChanges.setText(needToSendAll ? totalRecsInCache : String.valueOf(ms.getTotalRecsChanged()));
				unsentNewOrEditedRecs.setText(needToSendAll ? totalRecsInCache : String.valueOf(ms.getTotalRecsChanged() - ms.getRecsToDelete()));
				unsentDelRecs.setText(ms.isNeedsRebuild() ? "?" : String.valueOf(ms.getRecsToDelete()));
				for (JLabel l : loadingIcons) {
					l.setVisible(false);
				}
			}
			
		});
	}
	
	/**
	 * @param mapStatus
	 * @param instance
	 * @return
	 */
	protected OverallStatuses getOverallStatus(MappingUpdateStatus mapStatus, SpSymbiotaInstance instance) {
		OverallStatuses os = OverallStatuses.Good;
		if (instance.getSchemaMapping().getTimestampExported() == null) {
			os = OverallStatuses.CacheNotCreated;
		} else if (mapStatus.isNeedsRebuild()) {
			os = OverallStatuses.CacheNeedsRebuild;
		} else if (mapStatus.getTotalRecsChanged() > 0 || mapStatus.getRecsToDelete() > 0
				|| instance.getLastPush() == null || needToSendAllRecs(instance, mapStatus)) {
			os = OverallStatuses.DataToPush;
		} 
		return os;
	}
	
	/**
	 * @param os
	 * @return
	 */
	protected OverallStatusAlert getOverallStatusAlertLevel(OverallStatuses os) {
		OverallStatusAlert al = OverallStatusAlert.GREEN;
		if (os == OverallStatuses.CacheNotCreated || os == OverallStatuses.CacheNeedsRebuild) {
			al = OverallStatusAlert.RED;
		} else if (os == OverallStatuses.DataToPull || os == OverallStatuses.DataToPush) {
			al = OverallStatusAlert.YELLOW;
		}
		return al;
	}
	
	/**
	 * @param os
	 * @return
	 */
	protected String getOverallStatusText(OverallStatuses os) {
		String key = "SymbiotaPane.GeneralGood";
		if (os == OverallStatuses.CacheNotCreated) {
			key = "SymbiotaPane.CacheNotCreatedStatus";
		} else if (os == OverallStatuses.CacheNeedsRebuild) {
			key = "SymbiotaPane.CacheNeedsRebuildStatus";
		} else if (os == OverallStatuses.DataToPush) {
			key = "SymbiotaPane.UnsentChangesStatus";
		} else if (os == OverallStatuses.DataToPull) {
			key = "SymbiotaPane.ChangesAtSymbiotaStatus";
		}
		return UIRegistry.getResourceString(key);
	}
	
	/**
	 * @param mapStatus
	 * @param instance
	 */
	protected void updateOverallStatus(MappingUpdateStatus mapStatus, SpSymbiotaInstance instance) {
		overallStatus.setText(getOverallStatusText(mapStatus, instance));
	}
	
	/**
	 * @param mapStatus
	 * @param instance
	 * @return
	 */
	protected String getOverallStatusText(MappingUpdateStatus mapStatus, SpSymbiotaInstance instance) {
		OverallStatuses os = getOverallStatus(mapStatus, instance);
		OverallStatusAlert al = getOverallStatusAlertLevel(os);
		String statusText = getOverallStatusText(os);
		String fontColor = al == OverallStatusAlert.GREEN ? "blue"
				: al == OverallStatusAlert.RED ? "red" : "green";
		String html = "<html><font color=\"" + fontColor + "\">" + statusText + "</font></html>";
		return html;
	}
	
	/**
	 * @return
	 */
	protected int getTotalNumberOfRecsInCache(SpSymbiotaInstance instance) {
		int result = 0;
		String tbl = ExportToMySQLDB.fixTblNameForMySQL(instance.getSchemaMapping().getMappingName());
		if (BasicSQLUtils.doesTableExist(DBConnection.getInstance().getConnection(), tbl)) {
			result = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM " + tbl);
		}
		return result;
	}
	
	/**
	 * @param lbl
	 * @param val
	 * @return
	 */
	protected JPanel getStatPane(JLabel lbl, JLabel val, JLabel iconLbl) {
        PanelBuilder pb = new PanelBuilder(new FormLayout("20dlu, f:p:g", "p"));
		
		JPanel result = new JPanel(new BorderLayout());
		
		lbl.setText(lbl.getText() + ": ");
		result.add(lbl, BorderLayout.WEST);
		Font newFont = val.getFont();
		val.setFont(newFont.deriveFont(Font.BOLD));
		//val.getFont().
		result.add(val, BorderLayout.CENTER);
		if (iconLbl != null) {
			result.add(iconLbl, BorderLayout.EAST);
		}
		Rectangle b = result.getBounds();
		b.setSize(b.width, 30);
		result.setBounds(b);
		
		//return result;
		CellConstraints cc = new CellConstraints();
		pb.add(result, cc.xy(2, 1));
		return pb.getPanel();
	}
	
	/**
	 * @return
	 */
	protected Timestamp getOverrideTimestamp() {
		Timestamp overrideTimestamp = null;
		Timestamp cacheExported = symTask.getSchemaMapping().getTimestampExported();
		Timestamp symSent = symTask.getTheInstance().getLastPush() != null 
				? new Timestamp(symTask.getTheInstance().getLastPush().getTimeInMillis())
				: null;
		if (symSent != null && symSent.before(cacheExported)) {
			overrideTimestamp = symSent;
		}
		return overrideTimestamp;
	}
	
	/**
	 * 
	 */
	protected void updateCache() {
		setUpProgDlgForUpdate();
		SymWorker<Boolean, Object> worker = new SymWorker<Boolean, Object>("updateCache") {
			
			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#doInBackground()
			 */
			@Override
			protected Boolean doInBackground() throws Exception {
				boolean includeRecordIds = true;
				boolean useBulkLoad = false; 
				String bulkFileDir = null;
				QBDataSourceListenerIFace listener = SymbiotaPane.this; 
				Connection conn = DBConnection.getInstance().getConnection();
				final long cacheRowCount = mapStatus.get().getTotalRecsChanged() - mapStatus.get().getRecsToDelete();
				deletedRecsForCurrentUpdate.clear();
				newOrChangedRecsForCurrentUpdate.clear();
		        return ExportPanel.updateInBackground(includeRecordIds, useBulkLoad, bulkFileDir, 
		        		symTask.getSchemaMapping(), listener, conn, 
		        		cacheRowCount, getOverrideTimestamp());
			}

			/* (non-Javadoc)
			 * @see edu.ku.brc.specify.tasks.subpane.SymbiotaPane.SymWorker#doDone()
			 */
			@Override
			protected void doDone() {
				try {
					activeWorker.set(null);
					cacheUpdated(get());
					symTask.refreshInstance();
					getStats(false);
				} catch (Exception ignore) {					
				}
			}
			
		};	
		activeWorker.set(worker);
		worker.execute();
	}

	/**
	 * @param success
	 */
	protected void cacheUpdated(Boolean success) {
		if (success) {
			if (buildArchiveAfterCacheUpdate.get()) {
				buildDwCArchive();
				buildArchiveAfterCacheUpdate.set(false);
			}
		} else {
			UIRegistry.displayErrorDlg(getResourceString("SymbiotaPane.CacheUpdateErrorMsg"));
		}
	}
	
	/**
	 * 
	 */
	protected void createMainStatsPane() {
	    instanceName = UIHelper.createLabel("?");
	    overallStatus = UIHelper.createLabel("?");
	    instanceMapping = UIHelper.createLabel("?");
	    description = UIHelper.createLabel("?");
	    key = UIHelper.createLabel("?");
	    key.setVisible(!AppContextMgr.isSecurityOn() || symTask.getPermissions().canAdd());
	    
		lastSendToSymLbl = UIHelper.createI18NLabel("SymbiotaPane.LastSendDate");
		lastSendToSym = UIHelper.createLabel("?");
		lastGetFromSymLbl = UIHelper.createI18NLabel("SymbiotaPane.LastGetDate");
		lastGetFromSym = UIHelper.createLabel("?");
		dbCacheStatusLbl = UIHelper.createI18NLabel("SymbiotaPane.DBCacheStatus");
		dbCacheStatus = UIHelper.createLabel("?");
		dbCacheCreatedLbl = UIHelper.createI18NLabel("SymbiotaPane.DBCacheCreated");
		dbCacheCreated = UIHelper.createLabel("?");
		unsentTotalChangesLbl = UIHelper.createI18NLabel("SymbiotaPane.TotalChanges");
		unsentTotalChanges = UIHelper.createLabel("?");
		unsentNewOrEditedRecsLbl = UIHelper.createI18NLabel("SymbiotaPane.NewOrEditedRecs");
		unsentNewOrEditedRecs = UIHelper.createLabel("?");
		unsentDelRecsLbl = UIHelper.createI18NLabel("SymbiotaPane.DelRecs");
		unsentDelRecs = UIHelper.createLabel("?");
				
				
		cmdPanel = new JPanel(new BorderLayout());
		JPanel btnPane = new JPanel();
		btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.X_AXIS));
		pushBtn = UIHelper.createButton(UIRegistry.getResourceString("SymbiotaTask.SendToSymBtn"));
		pushBtn.setToolTipText(UIRegistry.getResourceString("SymbiotaTask.SendToSymBtnTT"));
		pushBtn.addActionListener(new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				symTask.sendToSym();
			}
			
		});
		archiveBtn = UIHelper.createButton(UIRegistry.getResourceString("SymbiotaTask.SendToArchiveBtn"));
		archiveBtn.setToolTipText(UIRegistry.getResourceString("SymbiotaTask.SendToArchiveBtnTT"));
		archiveBtn.addActionListener(new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				symTask.sendToArchive();
			}
			
		});
		archiveBtn.setVisible(false);
		
		pullBtn = UIHelper.createButton(UIRegistry.getResourceString("SymbiotaTask.GetFromSymBtn"));
		pullBtn.setToolTipText(UIRegistry.getResourceString("SymbiotaTask.GetFromSymBtnTT"));
		pullBtn.addActionListener(new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				symTask.getFromSym();
			}
			
		});
		if (!getButtonsPermission()) {
			pullBtn.setEnabled(false);
			pushBtn.setEnabled(false);
			archiveBtn.setEnabled(false);
			pullBtn.setToolTipText(UIRegistry.getResourceString("SymbiotaPane.NoPermissionForAction"));
			pushBtn.setToolTipText(UIRegistry.getResourceString("SymbiotaPane.NoPermissionForAction"));
			archiveBtn.setToolTipText(UIRegistry.getResourceString("SymbiotaPane.NoPermissionForAction"));
		}
		//btnPane.add(pullBtn);
		btnPane.add(pushBtn);
		btnPane.add(archiveBtn);
		cmdPanel.add(btnPane, BorderLayout.EAST);
		
		mainStatsPane = new JPanel(new BorderLayout());
		mainStatsPane.add(buildInfoPanel(), BorderLayout.NORTH);
		mainStatsPane.add(buildStatsPanel(), BorderLayout.CENTER);
		
		//mainStatsPane.add(form.getUIComponent(), BorderLayout.WEST);
		
		mainStatsPane.add(cmdPanel, BorderLayout.SOUTH);
		
	}
	
	/**
	 * @return
	 */
	protected boolean getButtonsPermission() {
		return !AppContextMgr.isSecurityOn() || (symTask != null && symTask.getPermissions().canModify());
	}
	
	/**
	 * 
	 */
	public void updateActionEnablement() {
		pushBtn.setEnabled(symTask.getTheInstance() != null && getButtonsPermission());
		pullBtn.setEnabled(symTask.getTheInstance() != null && getButtonsPermission());
		archiveBtn.setEnabled(symTask.getTheInstance() != null && getButtonsPermission());		
	}
	
	/**
	 * 
	 */
	protected void createIntroPane() {
		//JLabel text = UIHelper.createLabel("<html><p><p><b>Welcome to the Symbiota Task</b><p><p>blah...<p><p>Before you can use this task you need to create a schema mapping and create a new Symbiota connector.<p><p>blah blah blah...</html>");
		//introPane = new JPanel(new BorderLayout());
		//introPane.add(text, BorderLayout.NORTH);
	    introPane = (JPanel)StartUpTask.createFullImageSplashPanel(SymbiotaTask.SYMBIOTA_TITLE, symTask).getUIComponent();
	}
	
	/**
	 * @param toShow
	 */
	protected void showPane(JPanel toShow) {
		if (activePane != toShow) {
			final JPanel toRemove = activePane;
			activePane = toShow;
			SwingUtilities.invokeLater(new Runnable() {

				/* (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
				@Override
				public void run() {
					if (toRemove != null) {
						remove(toRemove);
					}
					add(activePane, BorderLayout.CENTER);
					if (getGraphics() != null) {
						update(getGraphics());
					}
				}
			});
		}
	}
	
	/**
	 * 
	 */
	protected void createUI() {
		removeAll();
		createMainStatsPane();
		createIntroPane();
		
		add(introPane, BorderLayout.CENTER);
		activePane = introPane;
	}
	
	/**
	 * 
	 */
	protected void setUpProgDlgForStatCheck() {
		//hideProgDlg();
        showProgDlg(getResourceString(progDlgTitleKey), getResourceString("SymbiotaPane.CheckingStats"), false);
	}
	
	/**
	 * 
	 */
	protected void setUpProgDlgForUpdate() {
		//hideProgDlg();
        showProgDlg(getResourceString(progDlgTitleKey), getResourceString("SymbiotaPane.UpdatingCache"), false);
	}
	
	/**
	 * 
	 */
	protected void setUpProgDlgForArchiveBuild() {
		//hideProgDlg();
        showProgDlg(getResourceString(progDlgTitleKey), getResourceString("SymbiotaPane.BuildingDwcArchive"), true);
	}

	/**
	 * 
	 */
	protected void setUpProgDlgForSend() {
		//hideProgDlg();
		boolean warn = needToSendAllRecs(symTask.getTheInstance(), mapStatus.get())
				||  mapStatus.get().getTotalRecsChanged() >= 5000
				|| (getOverallStatusAlertLevel(getOverallStatus(mapStatus.get(), symTask.getTheInstance())).equals(OverallStatusAlert.GREEN)
						&& getTotalNumberOfRecsInCache(symTask.getTheInstance()) >= 5000);
		String msgKey = warn 
				? "SymbiotaPane.PleaseAllowSendActionToComplete"
				: "SymbiotaPane.SENDING_DLG";
        showProgDlg(getResourceString(progDlgTitleKey), getResourceString(msgKey), true);
	}
	
	/**
	 * @param title
	 * @param desc
	 */
	protected void showProgDlg(final String title, final String desc, final boolean canCancel) {
		SwingUtilities.invokeLater(new Runnable() {

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				if (progDlg == null) {
		        	progDlg = new ProgressDialog(title, false, true);
		            progDlg.setResizable(false);
		            progDlg.getCloseBtn().setText(UIRegistry.getResourceString("CANCEL"));
		            progDlg.getCloseBtn().removeActionListener(progDlg.getCloseBtn().getActionListeners()[0]);
		            progDlg.getCloseBtn().addActionListener(new ActionListener() {

		            	/* (non-Javadoc)
		            	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		            	 */
		            	@Override
		            	public void actionPerformed(ActionEvent e) {
		            		//System.out.println("NO!");
		            		if (activeWorker.get() != null) {
		            			boolean cancelIt = true;
		            			if (activeWorker.get() != null && activeWorker.get().getTaskName().equals("sendToSym")) {
		            				//need to warn and get confirmation
		            				boolean onTop = false;
		            				if (progDlg.isVisible() && progDlg.isAlwaysOnTop()) {
		            					progDlg.setAlwaysOnTop(false);
		            					onTop = true;
		            				}
		            				cancelIt = UIRegistry.displayConfirmLocalized("SymbiotaPane.ConfirmPushCancelTitle", 
		            						"SymbiotaPane.ConfirmPushCancelMsg", "YES", "NO", JOptionPane.WARNING_MESSAGE);
		            				if (onTop) {
		            					progDlg.setAlwaysOnTop(true);
		            				}
		            			}
		            			if (cancelIt && activeWorker.get() != null) {
		            				activeWorker.get().setIgnoreResult(true);
		            				activeWorker.get().cancel(true); //true can cause exceptions --- which probably won't percolate up to the UI, and
		    											//probably won't cause any issues, at least for the statsGetter, updates are another story
		            				activeWorker.set(null);
		            				hideProgDlg();
		            			}
		    				}
		    			}
		            });
				}
		       progDlg.setTitle(title);
		       progDlg.setProcessPercent(false);
		       progDlg.getProcessProgress().setIndeterminate(true);
		       progDlg.getProcessProgress().setStringPainted(false);
		       progDlg.setDesc(desc);
		       progDlg.getCloseBtn().setEnabled(canCancel);
		       progDlg.getCloseBtn().setVisible(canCancel);
		       progDlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		       
		       //progDlg.setModal(false);
		       progDlg.setModal(true);

		       progDlg.setAlwaysOnTop(true);
		       UIHelper.centerAndShow(progDlg);
			}
		});
	}
	
	/**
	 * 
	 */
	protected void hideProgDlg() {
		if (progDlg != null && progDlg.isVisible()) {
//			SwingUtilities.invokeLater(new Runnable() {
//
//				/* (non-Javadoc)
//				 * @see java.lang.Runnable#run()
//				 */
//				@Override
//				public void run() {
			        progDlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			        progDlg.setVisible(false);
//			        progDlg = null;
//				}
//			});
		}
	}
	
	/**
	 * 
	 */
	public void startSend(String archiveFileName) {
		setUpProgDlgForStatCheck();
		//XXX make sure stats are up to the minute.
		//XXX but can't perfectly do that unless other users are forced out of collection?
		setUIEnabled(false);
		symTask.refreshInstance();
		updateCacheAfterStatusUpdate.set(true);
		buildArchiveAfterCacheUpdate.set(true);
		deletedRecsForCurrentUpdate.clear();
		newOrChangedRecsForCurrentUpdate.clear();
		this.archiveFileName.set(archiveFileName);
		sendAfterArchiveBuild.set(archiveFileName == null);

		progDlgTitleKey = archiveFileName == null ? "SymbiotaPane.SENDING_DLG" : "SymbiotaPane.ARCHIVING_DLG";
		getStats(true);
	}
	
	/**
	 * 
	 */
	protected void updateAndBuildArchive() {
		MappingUpdateStatus ms = mapStatus.get();
		if (ms.isNeedsRebuild()) {
			hideProgDlg();
			UIRegistry.displayInfoMsgDlgLocalized("SymbiotaPane.UseExporterAppToRebuildMsg", Object[].class.cast(null));
			setUIEnabled(true);
		} else {
			if (ms.getTotalRecsChanged() != 0) {
				updateCache();
			} else {
				buildDwCArchive();
			}
		}
	}
	
	/**
	 * @return
	 */
	protected String getIdSqlForSend() {
		if (useCache) {
			return "select " + ExportPanel.getCacheTableName(symTask.getSchemaMapping().getMappingName()) + 
					"id from " + ExportPanel.getCacheTableName(symTask.getSchemaMapping().getMappingName());
		} else {
			return null;
		}
	}
	
	protected void buildDwCArchive() {
		setUpProgDlgForArchiveBuild();
		
		SymWorker<Boolean, Object> worker = /**
		 * @author timo
		 *
		 */
		new SymWorker<Boolean, Object>("buildDwcArchive") {

			String outputFileName = getArchiveFileName();
			
			/**
			 * @return
			 */
			private Pair<Integer, Iterator<?>> getSizeAndIterator() {
				if (!needToSendAllRecs(symTask.getTheInstance(), mapStatus.get()) && newOrChangedRecsForCurrentUpdate.size() != 0) {
					return new Pair<Integer, Iterator<?>>(newOrChangedRecsForCurrentUpdate.size(), newOrChangedRecsForCurrentUpdate.iterator());
				} else {
					List<?> ids = BasicSQLUtils.querySingleCol(getIdSqlForSend());
					return new Pair<Integer, Iterator<?>>(ids.size(), ids.iterator());
				}
			}

			/**
			 * @param archiveName
			 * @param metaFild
			 * @param csvs
			 * @throws Exception
			 */
			protected void writeToArchiveFile(String archiveName, File metaFile, List<Pair<String, List<String>>> csvs) throws Exception {
		    	ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(new File(archiveName)));

		    	Charset utf8 = Charset.forName("utf8");
		    	
		    	zout.putNextEntry(new ZipEntry("meta.xml"));
		    	zout.write(FileUtils.readFileToString(metaFile, utf8).getBytes(utf8));
		    	zout.closeEntry();
		    	int size = 0;
		    	for (Pair<String, List<String>> csv : csvs) {
		    		size += csv.getSecond().size();
		    	}
		    	initProgRange(size);
		    	size = 0;
				for (Pair<String, List<String>> csv : csvs) {
			    	zout.putNextEntry(new ZipEntry(csv.getFirst()));
					for (String line : csv.getSecond()) {
						//System.out.println("    " + line);
						zout.write(line.getBytes(utf8));
						zout.write("\n".getBytes(utf8));
						setProgValue(++size);
					}
					zout.closeEntry();
				}
				zout.close();
			}

			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#doInBackground()
			 */
			@Override
			protected Boolean doInBackground() throws Exception {
				try {
					//File archiveMetaFile = new File("/home/timo/meta.xml");
					File archiveMetaFile = XMLHelper.getConfigDir("SymbiotaDwcMeta.xml");
					DarwinCoreArchive dwc = new DarwinCoreArchive(archiveMetaFile, symTask.getSchemaMapping().getId(), useCache);
					RecordSet rs = new RecordSet();
					rs.initialize();
					rs.setDbTableId(CollectionObject.getClassTableId());
					Pair<Integer, Iterator<?>> ids = getSizeAndIterator();
					Iterator<?> its = ids.getSecond();
					initProgRange(ids.getFirst());
					int rec = 0;
					while (its.hasNext()) {
						Integer id = (Integer)its.next();
						RecordSetItem rsi = new RecordSetItem();
						rsi.initialize();
						rsi.setRecordId(id);
						rs.getRecordSetItems().add(rsi);
						setProgValue(++rec);
					}
					setProgValue(0);
					List<Pair<String, List<String>>> csvs = dwc.getExportText(rs, progDlg != null ? progDlg.getProcessProgress() : null);
					writeToArchiveFile(outputFileName, archiveMetaFile, csvs);
				} catch (Exception e) {
		            UsageTracker.incrHandledUsageCount();
		            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SymbiotaPane.class, e);
		            e.printStackTrace();
				}
				return true;
			}

			/* (non-Javadoc)
			 * @see edu.ku.brc.specify.tasks.subpane.SymbiotaPane.SymWorker#doDone()
			 */
			@Override
			protected void doDone() {
				if (!ignoreResult.get()) {
					try {
						activeWorker.set(null);
						saveToFileDone(get(), outputFileName);
					} catch (Exception ignore) {
					}
				}
			}
			
		};
		activeWorker.set(worker);
		worker.execute();
	}
	
	
	
	/**
	 * @param fileName
	 */
	protected void sendToSym(final String fileName) {
		setUpProgDlgForSend();
		SymWorker<Pair<Boolean, String>, Object> worker = new SymWorker<Pair<Boolean, String>, Object>("sendToSym") {

			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#doInBackground()
			 */
			@Override
			protected Pair<Boolean, String> doInBackground() throws Exception {
				HttpClient httpClient = new HttpClient();
				PostMethod post = new PostMethod(symTask.getSymbiotaPostUrlForCurrentInstance());
				Pair<Boolean, String> result = new Pair<Boolean, String>(false, null);
								
				try {
				
					Part[] parts = {new StringPart("uploadtype", "6"), 
						new StringPart("key", symTask.getTheInstance().getSymbiotaKey()),
						new FilePart("uploadfile", fileName, new File(fileName)),
						new StringPart("importident", "1")/*, new StringPart("importimage", "1")*/};
					RequestEntity entity = new MultipartRequestEntity(parts, post.getParams());
					post.setRequestEntity(entity);

					//System.out.println("SKIPPING the POST!!!");
					int postStatus = /*200;*/ httpClient.executeMethod(post);
					//System.out.println("Status from Symbiota Post: " + postStatus);
					if (postStatus == 200) {
						byte[] responseBytes = post.getResponseBody();
						String response = responseBytes == null ? "" : new String(responseBytes);
						System.out.println("ResponseBody: " + response);
						if (!response.startsWith("FAILED") && !response.startsWith("ERROR")) {
							result.setFirst(true);
						}
						result.setSecond(response);
					} else {
						result.setSecond(String.format(UIRegistry.getResourceString("SymbiotaPane.BadPostStatus"), postStatus));
					}
					
				} catch (Exception ex) {
					//ex.printStackTrace();
					result.setSecond(ex.getLocalizedMessage());
				}
				return result;
			}

			/* (non-Javadoc)
			 * @see edu.ku.brc.specify.tasks.subpane.SymbiotaPane.SymWorker#doDone()
			 */
			@Override
			protected void doDone() {
				if (!ignoreResult.get()) {
					try {
						sendToSymDone(get());
					} catch (Exception ignore) {
					}
				}
			}
		};		
		activeWorker.set(worker);
		worker.execute();
	}
	/**
	 * @param success
	 * @param outputFileName
	 */
	protected void saveToFileDone(Boolean success, String outputFileName) {
		if (success && sendAfterArchiveBuild.get()) {
			sendAfterArchiveBuild.set(false);
			sendToSym(outputFileName);
		} else {
			hideProgDlg();
			if (success) {
				UIRegistry.displayInfoMsgDlgLocalized("SymbiotaPane.ArchiveFileCreated", outputFileName);
			} else {
				UIRegistry.displayErrorDlgLocalized("SymbiotaPane.ErrorCreatingArchive", Object[].class.cast(null));
			}
			instanceSelected();
			setUIEnabled(true);	
			
		}
	}
	
	/**
	 * @param result
	 */
	protected void sendToSymDone(Pair<Boolean, String> result) {
		hideProgDlg();
		activeWorker.set(null);
		if (result.getFirst()) {
			symTask.updateLastPushForInstance(symTask.getTheInstance());
			UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, "SymbiotaPane.SendSuccessDlgTitle", 
					"SymbiotaPane.SendSuccess", result.getSecond());
		} else {
			UIRegistry.showLocalizedError("SymbiotaPane.PushToSymFailed", result.getSecond());
		}
		instanceSelected();
		symTask.refreshInstance();
		setUIEnabled(true);		
	}
	
	/**
	 * @param val
	 */
	protected void setUIEnabled(boolean val) {
		//sendToSymBtn.setEnabled(val);
		//getFromSymBtn.setEnabled(val);
	}
	
	/**
	 * 
	 */
	public void getFromSym() {
		UIRegistry.showError("get from sym is not implemented");
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#currentRow(long)
	 */
	@Override
	public void currentRow(long currentRow) {
		setProgValue(Long.valueOf(currentRow).intValue());
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#rowCount(long)
	 */
	@Override
	public void rowCount(long rowCount) {
		//System.out.println(rowCount);
		initProgRange(Long.valueOf(rowCount).intValue());
	}

	/**
	 * @param max
	 */
	protected void initProgRange(int max) {
		if (max > 0 && progDlg != null && progDlg.isVisible()) {
			progDlg.setProcessPercent(false);
			progDlg.getProcessProgress().setStringPainted(false);
			progDlg.getProcessProgress().setIndeterminate(false);
			progDlg.getProcessProgress().setMinimum(0);
			progDlg.getProcessProgress().setMaximum(max);
			progDlg.getProcessProgress().setValue(0);
		}
	}
	
	/**
	 * @param val
	 */
	protected void setProgValue(int val) {
		if (progDlg != null && progDlg.isVisible()) {
			progDlg.getProcessProgress().setValue(val);			
		}
	}
	
	/**
	 * @param val
	 */
	protected void setProgIndeterminate(boolean val) {
		if (progDlg != null && progDlg.isVisible()) {
			progDlg.getProcessProgress().setStringPainted(false);
			progDlg.getProcessProgress().setIndeterminate(val);			
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#done(long)
	 */
	@Override
	public void done(long rows) {
		setProgValue(Long.valueOf(rows).intValue());
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#loading()
	 */
	@Override
	public void loading() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#loaded()
	 */
	@Override
	public void loaded() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#filling()
	 */
	@Override
	public void filling() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#isListeningClosely()
	 */
	@Override
	public boolean isListeningClosely() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#doTellAll()
	 */
	@Override
	public boolean doTellAll() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#deletedRecs(java.util.List)
	 */
	@Override
	public void deletedRecs(List<Integer> keysDeleted) {
		deletedRecsForCurrentUpdate.addAll(keysDeleted);
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#updatedRec(java.lang.Integer)
	 */
	@Override
	public void updatedRec(Integer key) {
		newOrChangedRecsForCurrentUpdate.add(key);
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#addedRec(java.lang.Integer)
	 */
	@Override
	public void addedRec(Integer key) {
		newOrChangedRecsForCurrentUpdate.add(key);
	}

	private abstract class SymWorker<T,V> extends javax.swing.SwingWorker<T,V> {
		protected AtomicBoolean ignoreResult = new AtomicBoolean(false);
		protected final String taskName;
		
		
		/**
		 * @param taskName
		 */
		public SymWorker(String taskName) {
			super();
			this.taskName = taskName;
		}

		/**
		 * @return
		 */
		public boolean isIgnoreResult() {
			return ignoreResult.get();
		}
		
		/**
		 * @param val
		 */
		public void setIgnoreResult(boolean val) {
			ignoreResult.set(val);
		}

		
		/**
		 * @return the taskName
		 */
		public String getTaskName() {
			return taskName;
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		protected void done() {
			super.done();
			if (!isIgnoreResult()) {
				doDone();
			}
		}
		
		/**
		 * 
		 */
		protected abstract void doDone();
		
	}

}
