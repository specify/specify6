/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.tasks.subpane.wb;


import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import edu.ku.brc.af.core.*;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.af.ui.forms.ResultSetControllerListener;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.StaleObjectException;
import edu.ku.brc.helpers.ImageFilter;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.services.biogeomancer.GeoCoordBGMProvider;
import edu.ku.brc.services.biogeomancer.GeoCoordDataIFace;
import edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace;
import edu.ku.brc.services.biogeomancer.GeoCoordServiceProviderIFace;
import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.services.mapping.LocalityMapper;
import edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace;
import edu.ku.brc.services.mapping.LocalityMapper.MapperListener;
import edu.ku.brc.services.mapping.SimpleMapLocation;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.*;
import edu.ku.brc.specify.dbsupport.RecordTypeCodeBuilder;
import edu.ku.brc.specify.dbsupport.TypeCode;
import edu.ku.brc.specify.rstools.ExportFileConfigurationFactory;
import edu.ku.brc.specify.rstools.ExportToFile;
import edu.ku.brc.specify.rstools.GoogleEarthExporter;
import edu.ku.brc.specify.rstools.WorkbenchRowPlacemarkWrapper;
import edu.ku.brc.specify.tasks.*;
import edu.ku.brc.specify.tasks.subpane.ESResultsSubPane;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.*;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.specify.ui.LengthInputVerifier;
import edu.ku.brc.specify.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.ui.*;
import edu.ku.brc.ui.ToggleButtonChooserPanel.Type;
import edu.ku.brc.ui.tmanfe.SearchReplacePanel;
import edu.ku.brc.ui.tmanfe.SpreadSheet;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.GeoRefConverter.GeoRefFormat;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.Pair;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jdesktop.swingx.decorator.*;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.table.TableColumnExt;
import sun.swing.table.DefaultTableCellHeaderRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.table.*;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.ConnectException;
import java.util.*;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static edu.ku.brc.ui.UIHelper.*;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

/**
 * Main class that handles the editing of Workbench data. It creates both a spreasheet and a form pane for editing the data.
 * 
 * @author rods, jstewart
 *
 * @code_status Beta
 *
 * Created Date: Mar 6, 2007
 *
 */
@SuppressWarnings("serial")
public class WorkbenchPaneSS extends BaseSubPane
{
    private static boolean          debugging = true;
    protected static final Logger     log = Logger.getLogger(WorkbenchPaneSS.class);
    
    final public static String wbAutoValidatePrefName = "WB.AutoValidatePref";
    final public static String wbAutoMatchPrefName = "WB.AutoMatchPref";
    final public static String wbAutoEditCheckPrefName = "WB.AutoEditCheckPref";
    
    public enum PanelType {Spreadsheet, Form}
    
    protected SearchReplacePanel    findPanel              = null;
    protected SpreadSheet           spreadSheet;
    protected Workbench             workbench;
    protected GridTableModel        model;
    protected TableColumnExt        imageColExt;
    protected String[]              columns;
    protected Integer[]             columnMaxWidths;  //the maximum characters allowable for the mapped fields 
    protected Vector<WorkbenchTemplateMappingItem> headers = new Vector<WorkbenchTemplateMappingItem>();
    protected boolean               hasChanged             = false;
    protected boolean               blockChanges           = false;
    protected RecordSet             recordSet              = null;
    
    protected JButton               saveBtn                = null;
    protected JButton               deleteRowsBtn          = null;
    protected JButton               clearCellsBtn          = null;
    protected JButton               addRowsBtn             = null;
    protected JButton               carryForwardBtn        = null;
    protected JButton               toggleImageFrameBtn    = null;
    protected JButton               showMapBtn             = null;
    protected JButton               controlPropsBtn        = null;
    protected JButton               exportKmlBtn           = null;
    protected JButton               geoRefToolBtn          = null;
    protected JButton               convertGeoRefFormatBtn = null;
    protected JButton               exportExcelCsvBtn      = null;
    protected JButton               uploadDatasetBtn       = null;
    protected JButton				showHideUploadToolBtn  = null;
    protected UploadToolPanel	    uploadToolPanel        = null;
    protected JButton               importImagesBtn        = null;

    //updateables only...
    protected JButton				revertBtn              = null;
    
    
    protected DropDownButtonStateful ssFormSwitcher        = null;  
    protected List<JButton>         selectionSensitiveButtons  = new Vector<JButton>();
    
    protected int                   currentRow                 = 0;
    protected FormPaneWrapper       formPane;
    protected ResultSetController   resultsetController;
    
    protected CardLayout            cardLayout                 = null;
    protected JPanel                mainPanel;
    protected PanelType             currentPanelType           = PanelType.Spreadsheet;
    
    protected JSplitPane            uploadPane                 = null; 
    
    protected JPanel                controllerPane;
    protected CardLayout            cpCardLayout               = null;
    
    protected ImageFrame            imageFrame                 = null;
    protected ImageImportFrame      imageImportFrame           = null;
    protected boolean               imageFrameWasShowing       = false;
    protected ListSelectionListener workbenchRowChangeListener = null;
    
    protected JFrame                mapFrame                   = null;
    protected JLabel                mapImageLabel              = null;
    
    protected WindowListener        minMaxWindowListener       = null; 
    
    protected CustomDialog          geoRefConvertDlg           = null;
    
    private static class WorkbenchPluginMap extends HashMap<Class<?>, WorkBenchPluginIFace> {}
    
    protected WorkbenchPluginMap    workBenchPlugins        = new WorkbenchPluginMap();
    protected Vector<JComponent>    workBenchPluginSSBtns   = new Vector<JComponent>();
    protected Vector<JComponent>    workBenchPluginFormBtns = new Vector<JComponent>();
    
    /**
     * The currently active Uploader. 
     * static to help prevent multiple simultaneous uploads.
     */
    protected static Uploader       datasetUploader            = null; 
    protected WorkbenchValidator    workbenchValidator         = null;
    protected boolean 		        doIncrementalValidation    = false;
    protected boolean	            doIncrementalMatching      = false;
    protected boolean				doIncrementalEditChecking  = false;
    protected UniquenessChecker		catNumChecker	           = null;
    protected int					catNumCol                  = -1;
    protected AtomicInteger			invalidCellCount		   = new AtomicInteger(0);
    protected AtomicInteger			unmatchedCellCount		   = new AtomicInteger(0);
    protected AtomicInteger			editedCellCount            = new AtomicInteger(0);
    protected CellRenderingAttributes cellRenderAtts           = new CellRenderingAttributes();
    protected boolean				restoreUploadToolPanel	   = false;
    
    //Single thread executor to ensure that rows are not validated concurrently as a result of batch operations
    //protected final ExecutorService validationExecutor		   = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
    protected final Queue<ValidationWorker> validationWorkerQueue = new LinkedList<ValidationWorker>();
    
    // XXX PREF
    protected int                   mapSize                    = 500;
    
    protected boolean               isReadOnly;
        
    protected AtomicInteger         shutdownLock               = new AtomicInteger(0);
    private TableColumnExt          sgrColExt;
    private Taskable                srcTask;
    private JComponent spreadSheetPane = null;
    

    /**
     * Constructs the pane for the spreadsheet.
     * 
     * @param name the name of the pane
     * @param task the owning task
     * @param workbenchArg the workbench to be edited
     * @param showImageView shows image window when first showing the window
     * @param isReadOnly
     */
    public WorkbenchPaneSS(final String    name,
                           final Taskable  task,
                           final Workbench workbenchArg,
                           final boolean   showImageView,
                           final boolean isReadOnly,
                           final boolean isUpdate) throws Exception
    {
        super(isUpdate ? getResourceString("BATCHEDIT") : name, task);

        
        removeAll();
        
        if (workbenchArg == null)
        {
            return;
        }
        this.workbench = workbenchArg;
        
        this.isReadOnly = isReadOnly;
        
        headers.addAll(workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems());
        Collections.sort(headers);
        
        boolean hasOneOrMoreImages = false;
        // pre load all the data
        for (WorkbenchRow wbRow : workbench.getWorkbenchRows())
        {
            for (WorkbenchDataItem wbdi : wbRow.getWorkbenchDataItems())
            {
                wbdi.getCellData();
            }
            
            if (wbRow.getWorkbenchRowImages() != null && wbRow.getWorkbenchRowImages().size() > 0)
            {
                hasOneOrMoreImages = true;
            }
        } 

        model       = new GridTableModel(this);
        spreadSheet = new WorkbenchSpreadSheet(model, this);
        spreadSheet.setReadOnly(isReadOnly);
        model.setSpreadSheet(spreadSheet);

        GradiantLabel titleBar = null;
        if (isUpdate) {
            titleBar = new GradiantLabel("Batch Edit", SwingConstants.LEFT);
            titleBar.setFont(spreadSheet.getFont().deriveFont(spreadSheet.getFont().getSize() + 3.0F));
            titleBar.setTextColor(new Color(0xFFFFFF));
            titleBar.setBGBaseColor(new Color(0xcf0a2c));
            titleBar.setGradiants(new Color(0xfd5875), new Color(0xcf0a2c));
            titleBar.setIcon(IconManager.getIcon(/*Math.random() >= 0.5 ? */"BatchEdit"/* : "SkullBones"*/, IconManager.IconSize.Std24));
        }

        Highlighter simpleStriping = HighlighterFactory.createSimpleStriping();
        GridCellHighlighter hl = new GridCellHighlighter(new GridCellPredicate(GridCellPredicate.AnyPredicate, null, null));
        Short[] errs = {WorkbenchDataItem.VAL_ERROR};
        ColorHighlighter errColorHighlighter = new ColorHighlighter(new GridCellPredicate(GridCellPredicate.ValidationPredicate, errs, null),
        		CellRenderingAttributes.errorBackground, null);
        Short[] newdata = {WorkbenchDataItem.VAL_NEW_DATA};
        ColorHighlighter noDataHighlighter = new ColorHighlighter(new GridCellPredicate(GridCellPredicate.MatchingPredicate, newdata, null),
        		CellRenderingAttributes.newDataBackground, null);
        Short[] multimatch = {WorkbenchDataItem.VAL_MULTIPLE_MATCH};
        ColorHighlighter multiMatchHighlighter = new ColorHighlighter(new GridCellPredicate(GridCellPredicate.MatchingPredicate, multimatch, null),
        		CellRenderingAttributes.multipleMatchBackground, null);
        Short[] edited = {WorkbenchDataItem.VAL_EDIT};
        ColorHighlighter editedHighlighter = new ColorHighlighter(new GridCellPredicate(GridCellPredicate.EditedPredicate, edited, errs),
        		CellRenderingAttributes.editedBackground, null);

        spreadSheet.setHighlighters(simpleStriping, hl, noDataHighlighter, multiMatchHighlighter, editedHighlighter, errColorHighlighter);
        
        //add key mappings for cut, copy, paste
        //XXX Note: these are shortcuts directly to the SpreadSheet cut,copy,paste methods, NOT to the Specify edit menu.
        addRecordKeyMappings(spreadSheet, KeyEvent.VK_C, "Copy", new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                SwingUtilities.invokeLater(new Runnable() {

					/* (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run()
					{
		            	spreadSheet.cutOrCopy(false);
					}
                });
            }
        }, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        addRecordKeyMappings(spreadSheet, KeyEvent.VK_X, "Cut", new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                SwingUtilities.invokeLater(new Runnable() {

					/* (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run()
					{
		            	spreadSheet.cutOrCopy(true);
					}
                });
            }
        }, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        addRecordKeyMappings(spreadSheet, KeyEvent.VK_V, "Paste", new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                SwingUtilities.invokeLater(new Runnable() {

					/* (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run()
					{
		            	spreadSheet.paste();
					}
                });
            }
        }, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        
        findPanel = spreadSheet.getFindReplacePanel();
        UIRegistry.getLaunchFindReplaceAction().setSearchReplacePanel(findPanel);

        spreadSheet.setShowGrid(true);
        JTableHeader header = spreadSheet.getTableHeader();
        header.addMouseListener(new ColumnHeaderListener());
        header.setReorderingAllowed(false); // Turn Off column dragging

        // Put the model in image mode, and never change it.
        // Now we're showing/hiding the image column using JXTable's column hiding features.
        model.setInImageMode(true);
        int imageColIndex = model.getColumnCount() - 1;
        imageColExt = spreadSheet.getColumnExt(imageColIndex);
        imageColExt.setVisible(false);

//	    int sgrColIndex = model.getSgrHeading().getViewOrder();
//	    sgrColExt = spreadSheet.getColumnExt(sgrColIndex);
//	    sgrColExt.setComparator( ((WorkbenchSpreadSheet)spreadSheet).new NumericColumnComparator() );

        int cmpIdx = 0;
	    for (Comparator<String> cmp : ((WorkbenchSpreadSheet )spreadSheet).getComparators())
		{
			if (cmp != null)
			{
				spreadSheet.getColumnExt(cmpIdx++).setComparator(cmp);
			}
		}


        // Start off with the SGR score column hidden
        showHideSgrCol(false);
       
        
        model.addTableModelListener(e -> setChanged(true, e));
        
        spreadSheet.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e)
            {
                UIRegistry.enableCutCopyPaste(true);
                UIRegistry.enableFind(findPanel, true);
            }
            @Override
            public void focusLost(FocusEvent e)
            {
                UIRegistry.enableCutCopyPaste(true);
                UIRegistry.enableFind(findPanel, true);
            }
        });

        //XXX Using the wb ID in the prefname to do pref setting per wb, may result in a bloated prefs file?? 
        doIncrementalValidation = AppPreferences.getLocalPrefs().getBoolean(wbAutoValidatePrefName + "." + workbench.getId(), true);
        doIncrementalMatching = AppPreferences.getLocalPrefs().getBoolean(wbAutoMatchPrefName + "." + workbench.getId(), false);
        doIncrementalEditChecking = AppPreferences.getLocalPrefs().getBoolean(wbAutoEditCheckPrefName + "." + workbench.getId(), true);
        
        if (getIncremental() && workbenchValidator == null) {
        	buildValidator();
        }
        boolean isForBatchEdit = this.isUpdateDataSet(); //I guess this redundant given the new isUpdate arg, but just to be sure...
        if (isForBatchEdit) {
            doIncrementalValidation = true;
            doIncrementalEditChecking = true;
            doIncrementalMatching = false;
        } else {
            doIncrementalEditChecking = false;
        }

        if (isReadOnly)
        {
            saveBtn = null;
        }
        else
        {
            saveBtn = createButton(isForBatchEdit ? getResourceString("WB_BATCH_EDIT_DONE_BTN") : getResourceString("SAVE"));
            saveBtn.setToolTipText(String.format(isForBatchEdit ? getResourceString("WB_BATCH_EDIT_DONE_TT") : getResourceString("WB_SAVE_DATASET_TT"),
                    new Object[] { workbench.getName() }));
            saveBtn.setEnabled(false);
            saveBtn.setFocusTraversalKeysEnabled(false);
            saveBtn.addActionListener(new ActionListener() {
                final boolean uploadAfterSave = isForBatchEdit;
            	public void actionPerformed(ActionEvent ae) {
                    UsageTracker.incrUsageCount("WB.SaveDataSet");
                    if (isForBatchEdit) {
                        if (invalidCellCount.get() > 0) {
                            UIRegistry.showLocalizedMsg(getResourceString("WB_BATCH_EDIT_CORRECT_INVALID_CELLS"));
                            return;
                        }
                    }
                    String msg = isForBatchEdit ? getResourceString("WB_BATCH_EDIT_PREP")
                            : String.format(getResourceString("WB_SAVING"), new Object[] { workbench.getName() });
                    UIRegistry.writeGlassPaneMsg(msg, WorkbenchTask.GLASSPANE_FONT_SIZE);
                    UIRegistry.getStatusBar().setIndeterminate(workbench.getName(), true);
                    final SwingWorker worker = new SwingWorker() {
                        @SuppressWarnings("synthetic-access")
                        @Override
                        public Object construct() {
                            try {
                                if (uploadAfterSave) {
                                    List<SubPaneIFace> badPanes = checkOpenTasksForUpload(uploadAfterSave);
                                    if (badPanes.size() > 0) {
                                        UIRegistry.displayInfoMsgDlgLocalized(String.format(
                                                getResourceString(isForBatchEdit ? "BATCH_EDIT_CLOSE_TABS_MSG" :"WB_UPLOAD_CLOSE_ALL_MSG"),
                                                getListOfBadTasks(badPanes)));
                                        saveBtn.setEnabled(true);
                                        return null;
                                    }
                                }
                                saveObject();
                                if (uploadAfterSave) {
                                    revertBtn.setEnabled(false);
                                	doDatasetUpload();
                                }
                            }
                            catch (Exception ex) {
                                UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchPaneSS.class, ex);
                                log.error(ex);
                                return ex;
                            }
                            return null;
                        }

                        // Runs on the event-dispatching thread.
                        @Override
                        public void finished() {
                            Object retVal = get();
                            if (retVal != null && retVal instanceof Exception) {
                                Exception ex = (Exception) retVal;
                                UIRegistry.getStatusBar().setErrorMessage(
                                        getResourceString("WB_ERROR_SAVING"), ex);
                            }

                            UIRegistry.clearGlassPaneMsg();
                            UIRegistry.getStatusBar().setProgressDone(workbench.getName());
                        }
                    };
                    worker.start();

                }
            });
        }
        
        if (isReadOnly || !isForBatchEdit)
        {
            revertBtn = null;
        }
        else {
            revertBtn = createButton(getResourceString("WB_REVERT"));
            revertBtn.setToolTipText(String.format(getResourceString("WB_REVERT_TT"),
                    new Object[] { workbench.getName() }));
            revertBtn.setEnabled(false);
            revertBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    UsageTracker.incrUsageCount("WB.RevertEdits");

                    UIRegistry.writeGlassPaneMsg(String.format(getResourceString("WB_REVERTING"),
                            new Object[] { workbench.getName() }),
                            WorkbenchTask.GLASSPANE_FONT_SIZE);
                    UIRegistry.getStatusBar().setIndeterminate(workbench.getName(), true);
                    final SwingWorker worker = new SwingWorker() {
                        @SuppressWarnings("synthetic-access")
                        @Override
                        public Object construct() {
                            try {
                                revertRows();
                            } catch (Exception ex) {
                                UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchPaneSS.class, ex);
                                log.error(ex);
                                return ex;
                            }
                            return null;
                        }

                        // Runs on the event-dispatching thread.
                        @Override
                        public void finished() {
                            Object retVal = get();
                            if (retVal != null && retVal instanceof Exception) {
                                Exception ex = (Exception) retVal;
                                UIRegistry.getStatusBar().setErrorMessage(
                                        getResourceString("WB_ERROR_REVERTING"), ex);
                            }

                            UIRegistry.clearGlassPaneMsg();
                            UIRegistry.getStatusBar().setProgressDone(workbench.getName());
                        }
                    };
                    worker.start();

                }
            });
        }

        
        Action delAction = addRecordKeyMappings(spreadSheet, KeyEvent.VK_F3, "DelRow", new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                if (validationWorkerQueue.peek() == null) 
                {	
                	deleteRows();
                }
            }
        }, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        
        if (isReadOnly || isForBatchEdit) {
            deleteRowsBtn = null;
        } else {
            deleteRowsBtn = createIconBtn("DelRec", "WB_DELETE_ROW", delAction);
            selectionSensitiveButtons.add(deleteRowsBtn);
            spreadSheet.setDeleteAction(delAction);
        }
               

        if (isReadOnly)
        {
            clearCellsBtn = null;
        }
        else
        {
            clearCellsBtn = createIconBtn("Eraser", "WB_CLEAR_CELLS", new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    spreadSheet.clearSorter();

                    if (spreadSheet.getCellEditor() != null)
                    {
                        spreadSheet.getCellEditor().stopCellEditing();
                    }
                    int[] rows = spreadSheet.getSelectedRowModelIndexes();
                    int[] cols = spreadSheet.getSelectedColumnModelIndexes();
                    model.clearCells(rows, cols);
                }
            });
            selectionSensitiveButtons.add(clearCellsBtn);
            clearCellsBtn.setVisible(!isForBatchEdit);
        }
        
        Action addAction = addRecordKeyMappings(spreadSheet, KeyEvent.VK_N, "AddRow", new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                if (workbench.getWorkbenchRows().size() < WorkbenchTask.MAX_ROWS)
                {
                    if (validationWorkerQueue.peek() == null) 
                    {	
                    	addRowAfter();
                    }
                }
            }
        }, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        
        if (isReadOnly || isForBatchEdit)
        {
            addRowsBtn = null;
        }
        else
        {
            addRowsBtn = createIconBtn("AddRec", "WB_ADD_ROW", addAction);
            addRowsBtn.setEnabled(true);
            addAction.setEnabled(true); 
        }

        if (isReadOnly || isForBatchEdit)
        {
            carryForwardBtn = null;
        }
        else
        {
            carryForwardBtn = createIconBtn("CarryForward20x20", IconManager.IconSize.NonStd,
                    "WB_CARRYFORWARD", false, new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            UsageTracker.getUsageCount("WBCarryForward");

                            configCarryFoward();
                        }
                    });
            carryForwardBtn.setEnabled(true);
        }
        
        if (!isForBatchEdit) {
	        toggleImageFrameBtn = createIconBtn("CardImage", IconManager.IconSize.NonStd, "WB_SHOW_IMG_WIN", false, new ActionListener()
	        {
	            public void actionPerformed(ActionEvent ae)
	            {
	                toggleImageFrameVisible();
	            }
	        });
	        toggleImageFrameBtn.setEnabled(true);
	        
	        importImagesBtn = createIconBtn("CardImage", IconManager.IconSize.NonStd, "WB_SHOW_IMG_WIN", false, new ActionListener()
	        {
	            public void actionPerformed(ActionEvent ae)
	            {
	                toggleImportImageFrameVisible();
	            }
	        });
	        importImagesBtn.setEnabled(true);
        }
        /*showMapBtn = createIconBtn("ShowMap", IconManager.IconSize.NonStd, "WB_SHOW_MAP", false, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showMapOfSelectedRecords();
            }
        });*/
        // enable or disable along with Google Earth and Geo Ref Convert buttons
        
        if (isReadOnly)
        {
            exportKmlBtn = null;
        }
        else
        {
            exportKmlBtn = createIconBtn("GoogleEarth", IconManager.IconSize.NonStd,
                    "WB_SHOW_IN_GOOGLE_EARTH", false, new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                public void run()
                                {
                                    showRecordsInGoogleEarth();
                                }
                            });
                        }
                    });
        }
        
        // 
        
        readRegisteries();
        
        // enable or disable along with Show Map and Geo Ref Convert buttons
        
        if (isReadOnly)
        {
            geoRefToolBtn = null;
        }
        else
        {
            AppPreferences remotePrefs = AppPreferences.getRemote();
            final String tool = remotePrefs.get("georef_tool", "geolocate");
            String iconName = "GEOLocate20"; //tool.equalsIgnoreCase("geolocate") ? "GeoLocate" : "BioGeoMancer";
            String toolTip = tool.equalsIgnoreCase("geolocate") ? "WB_DO_GEOLOCATE_LOOKUP"
            		: "WB_DO_BIOGEOMANCER_LOOKUP";
            geoRefToolBtn = createIconBtn(iconName, IconManager.IconSize.NonStd,
                    toolTip, false, new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            spreadSheet.clearSorter();

                            if (tool.equalsIgnoreCase("geolocate"))
                            {
                                doGeoRef(new edu.ku.brc.services.geolocate.prototype.GeoCoordGeoLocateProvider(), "WB.GeoLocateRows");
                            }
                            else
                            {
                                doGeoRef(new GeoCoordBGMProvider(), "WB.BioGeomancerRows");
                            }
                        }
                    });
            // only enable it if the workbench has the proper columns in it
            String[] missingColumnsForBG = getMissingButRequiredColumnsForGeoRefTool(tool);
            if (missingColumnsForBG.length > 0)
            {
                geoRefToolBtn.setEnabled(false);
                String ttText = "<p>" + getResourceString("WB_ADDITIONAL_FIELDS_REQD") + ":<ul>";
                for (String reqdField : missingColumnsForBG)
                {
                    ttText += "<li>" + reqdField + "</li>";
                }
                ttText += "</ul>";
                String origTT = geoRefToolBtn.getToolTipText();
                geoRefToolBtn.setToolTipText("<html>" + origTT + ttText);
            }
            else
            {
                geoRefToolBtn.setEnabled(true);
            }
        }
                
        if (isReadOnly)
        {
            convertGeoRefFormatBtn = null;
        }
        else
        {
            convertGeoRefFormatBtn = createIconBtn("ConvertGeoRef", IconManager.IconSize.NonStd,
                    "WB_CONVERT_GEO_FORMAT", false, new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            showGeoRefConvertDialog();
                        }
                    });

            // now enable/disable the geo ref related buttons
            String[] missingGeoRefFields = getMissingGeoRefLatLonFields();
            if (missingGeoRefFields.length > 0)
            {
                convertGeoRefFormatBtn.setEnabled(false);
                exportKmlBtn.setEnabled(false);
                //showMapBtn.setEnabled(false);

                String ttText = "<p>" + getResourceString("WB_ADDITIONAL_FIELDS_REQD") + ":<ul>";
                for (String reqdField : missingGeoRefFields)
                {
                    ttText += "<li>" + reqdField + "</li>";
                }
                ttText += "</ul>";
                String origTT1 = convertGeoRefFormatBtn.getToolTipText();
                convertGeoRefFormatBtn.setToolTipText("<html>" + origTT1 + ttText);
                String origTT2 = exportKmlBtn.getToolTipText();
                exportKmlBtn.setToolTipText("<html>" + origTT2 + ttText);
                //String origTT3 = showMapBtn.getToolTipText();
                //showMapBtn.setToolTipText("<html>" + origTT3 + ttText);
            }
            else
            {
                convertGeoRefFormatBtn.setEnabled(true);
                exportKmlBtn.setEnabled(true);
                //showMapBtn.setEnabled(true);
            }
        }
        
        if (AppContextMgr.isSecurityOn() && !task.getPermissions().canModify())
        {
            exportExcelCsvBtn = null;
        }
        else
        {
            exportExcelCsvBtn = createIconBtn("Export", IconManager.IconSize.NonStd,
                    "WB_EXPORT_DATA", false, new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            doExcelCsvExport();
                        }
                    });
            exportExcelCsvBtn.setEnabled(true);
        }
        
        uploadDatasetBtn = createIconBtn("Upload", IconManager.IconSize.Std24, "WB_UPLOAD_DATA", false, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                doDatasetUpload();
            }
        });
        uploadDatasetBtn.setVisible(!isForBatchEdit && isUploadPermitted() && !UIRegistry.isMobile());
        uploadDatasetBtn.setEnabled(canUpload());
        if (!uploadDatasetBtn.isEnabled())
        {
            uploadDatasetBtn.setToolTipText(getResourceString("WB_UPLOAD_IN_PROGRESS"));
        }
        
        // listen to selection changes to enable/disable certain buttons
        spreadSheet.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    JStatusBar statusBar = UIRegistry.getStatusBar();
                    statusBar.setText("");
                    
                    currentRow = spreadSheet.getSelectedRow();
                    updateBtnUI();
                }
            }
        });
        

        // setup the JFrame to show images attached to WorkbenchRows
        imageFrame = new ImageFrame(mapSize, this, this.workbench, task, isReadOnly);
        
        // setup the JFrame to show images attached to WorkbenchRows
        imageImportFrame = new ImageImportFrame(this, this.workbench);
        
        setupWorkbenchRowChangeListener();
        
        // setup window minimizing/maximizing listener
        JFrame topFrame = (JFrame)UIRegistry.getTopWindow();
        minMaxWindowListener = new WindowAdapter()
        {
            @Override
            public void windowDeiconified(WindowEvent e)
            {
                if (imageFrame != null && imageFrame.isVisible())
                {
                    imageFrame.setExtendedState(Frame.NORMAL);
                }
                if (mapFrame != null && mapFrame.isVisible())
                {
                    mapFrame.setExtendedState(Frame.NORMAL);
                }
            }

            @Override
            public void windowIconified(WindowEvent e)
            {
                if (imageFrame != null && imageFrame.isVisible())
                {
                    imageFrame.setExtendedState(Frame.ICONIFIED);
                }
                if (mapFrame != null && mapFrame.isVisible())
                {
                    mapFrame.setExtendedState(Frame.ICONIFIED);
                }
            }
        };
        topFrame.addWindowListener(minMaxWindowListener);
 
        if (!isReadOnly)
        {
            showHideUploadToolBtn = createIconBtn("ValidateWB", IconManager.IconSize.NonStd, "WB_HIDE_UPLOADTOOLPANEL", false, new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    if (uploadToolPanel.isExpanded())
                    {
                    	hideUploadToolPanel();
                    	showHideUploadToolBtn.setToolTipText(getResourceString("WB_SHOW_UPLOADTOOLPANEL"));
                    } else
                    {
                    	showUploadToolPanel();
                    	showHideUploadToolBtn.setToolTipText(getResourceString("WB_HIDE_UPLOADTOOLPANEL"));
                   }
                }
            });
            showHideUploadToolBtn.setEnabled(true);
        }

        // setup the mapping features
        mapFrame = new JFrame();
        mapFrame.setIconImage( IconManager.getImage("AppIcon").getImage());
        mapFrame.setTitle(getResourceString("WB_GEO_REF_DATA_MAP"));
        mapImageLabel = createLabel("");
        mapImageLabel.setSize(500,500);
        mapFrame.add(mapImageLabel);
        mapFrame.setSize(500,500);
        
        // start putting together the visible UI
        CellConstraints cc = new CellConstraints();

        JComponent[] compsArray = {addRowsBtn, deleteRowsBtn, clearCellsBtn, /*showMapBtn,*/ exportKmlBtn,
                                   geoRefToolBtn, convertGeoRefFormatBtn, exportExcelCsvBtn, uploadDatasetBtn, showHideUploadToolBtn};
        Vector<JComponent> availableComps = new Vector<JComponent>(compsArray.length + workBenchPluginSSBtns.size());
        for (JComponent c : compsArray)
        {
            if (c != null)
            {
                availableComps.add(c);
            }
        }
        for (JComponent c : workBenchPluginSSBtns)
        {
            availableComps.add(c);
        }
        
        PanelBuilder spreadSheetControlBar = new PanelBuilder(new FormLayout("f:p:g,4px,"+createDuplicateJGoodiesDef("p", "4px", availableComps.size())+",4px,", "c:p:g"));
        
        int x = 3;
        for (JComponent c : availableComps)
        {
            spreadSheetControlBar.add(c, cc.xy(x,1));
            x += 2;
        }

        int h = 0;
        Vector<WorkbenchTemplateMappingItem> headers = new Vector<WorkbenchTemplateMappingItem>();
        headers.addAll(workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems());
        Collections.sort(headers);
        for (WorkbenchTemplateMappingItem mi : headers) {
        	//using the workbench data model table. Not the actual specify table the column is mapped to.
        	//This MIGHT be less confusing
        	//System.out.println("setting header renderer for " + mi.getTableName() + "." + mi.getFieldName()); 
        	spreadSheet.getColumnModel().getColumn(h++).setHeaderRenderer(new WbTableHeaderRenderer(mi.getTableName()));
        }
        
        // NOTE: This needs to be done after the creation of the saveBtn. And after the creation of the header renderes.
        initColumnSizes(spreadSheet, saveBtn);

        // Create the Form Pane  -- needs to be done after initColumnSizes - which also sets cell editors for collumns
        if (task instanceof SGRTask) {
            formPane = new SGRFormPane(this, workbench, isReadOnly);
        } else /*if (!isForBatchEdit)*/ {
            formPane = new FormPane(this, workbench, isReadOnly);
        }

        PanelBuilder resultSetPanel = null;
        if (formPane != null) {
            // This panel contains just the ResultSetContoller, it's needed so the RSC gets centered
            PanelBuilder rsPanel = new PanelBuilder(new FormLayout("c:p:g", "c:p:g"));
            FormValidator dummy = new FormValidator(null);
            dummy.setEnabled(true);
            resultsetController = new ResultSetController(dummy, !isReadOnly, !isReadOnly, false, getResourceString("Record"), model.getRowCount(), true);
            resultsetController.addListener(formPane);
            if (!isReadOnly) {
                resultsetController.getDelRecBtn().addActionListener(delAction);
            }
            rsPanel.add(resultsetController.getPanel(), cc.xy(1, 1));

            // This panel is a single row containing the ResultSetContoller and the other controls for the Form Panel
            String colspec = "f:p:g, p, f:p:g, p";
            for (int i = 0; i < workBenchPluginFormBtns.size(); i++) {
                colspec = colspec + ", f:p, p";
            }

            resultSetPanel = new PanelBuilder(new FormLayout(colspec, "c:p:g"));
            // Now put the two panel into the single row panel
            resultSetPanel.add(rsPanel.getPanel(), cc.xy(2, 1));
            if (!isReadOnly) {
                resultSetPanel.add(formPane.getControlPropsBtn(), cc.xy(4, 1));
            }
            int ccx = 6;
            for (JComponent c : workBenchPluginFormBtns) {
                resultSetPanel.add(c, cc.xy(ccx, 1));
                ccx += 2;
            }
        }
        
        // Create the main panel that uses card layout for the form and spreasheet
        mainPanel = new JPanel(cardLayout = new CardLayout());
        
        // Add the Form and Spreadsheet to the CardLayout
        if (isUpdate) {
            spreadSheetPane = new JPanel(new BorderLayout());
            spreadSheetPane.add(titleBar, BorderLayout.NORTH);
            spreadSheetPane.add(spreadSheet.getScrollPane(), BorderLayout.CENTER);
        } else {
            spreadSheetPane = spreadSheet.getScrollPane();
        }
        Border b = new BasicBorders.MarginBorder();
        //spreadSheetPanel.setBorder(new TitledBorder(new BasicBorders.MarginBorder(), "Batch Edit", TitledBorder.LEFT, TitledBorder.TOP, spreadSheet.getFont(), new Color(0x00ff00)));
        //spreadSheetPanel.setBackground(titleBar.getBackground());
        mainPanel.add(spreadSheetPane, PanelType.Spreadsheet.toString());

        // The controllerPane is a CardLayout that switches between the Spreadsheet control bar and the Form Control Bar
        controllerPane = new JPanel(cpCardLayout = new CardLayout());
        controllerPane.add(spreadSheetControlBar.getPanel(), PanelType.Spreadsheet.toString());

        if (!isForBatchEdit) {
            mainPanel.add(formPane.getPane(), PanelType.Form.toString());
            controllerPane.add(resultSetPanel.getPanel(),        PanelType.Form.toString());
        }

        JLabel sep1 = new JLabel(IconManager.getIcon("Separator"));
        JLabel sep2 = new JLabel(IconManager.getIcon("Separator"));
        ssFormSwitcher = createSwitcher();
        ssFormSwitcher.setVisible(!isForBatchEdit);
        sep2.setVisible(ssFormSwitcher.isVisible());
        
        // This works
        setLayout(new BorderLayout());
        
        boolean doDnDImages = AppPreferences.getLocalPrefs().getBoolean("WB_DND_IMAGES", false);
        JComponent[] ctrlCompArray1 = {importImagesBtn, toggleImageFrameBtn, carryForwardBtn, sep1, saveBtn, revertBtn, sep2, ssFormSwitcher};
        JComponent[] ctrlCompArray2 = {toggleImageFrameBtn, carryForwardBtn, sep1, saveBtn, revertBtn, sep2, ssFormSwitcher}; 
        JComponent[] ctrlCompArray  = doDnDImages ? ctrlCompArray1 : ctrlCompArray2;
   
        Vector<Pair<JComponent, Integer>> ctrlComps = new Vector<Pair<JComponent, Integer>>();
        for (JComponent c : ctrlCompArray)
        {
            ctrlComps.add(new Pair<JComponent, Integer>(c, null));
        }
        
        String layoutStr = "";
        int compCount = 0;
        int col = 1;
        int pos = 0;            
        for (Pair<JComponent, Integer> c : ctrlComps)
        {
            JComponent comp = c.getFirst();
            if (comp != null)
            {
                boolean addComp = !(comp == sep1 || comp == sep2) || compCount > 0;
                if (!addComp)
                {
                    c.setFirst(null);
                }
                else
                {
                    if (!StringUtils.isEmpty(layoutStr))
                    {
                        layoutStr += ",";
                        col++;
                        if (pos < ctrlComps.size() - 1)
                        {
                            //this works because we know ssFormSwitcher is last and always non-null.
                            layoutStr += "6px,";
                            col++;
                        }
                    }
                    c.setSecond(col);
                    if (comp == sep1 || comp == sep2) 
                    {
                        layoutStr += "6px";
                        compCount = 0;
                    }
                    else
                    {
                        layoutStr += "p";
                        compCount++;
                    }   
                }
            }
            pos++;
        }
        PanelBuilder    ctrlBtns   = new PanelBuilder(new FormLayout(layoutStr, "c:p:g"));
        for (Pair<JComponent, Integer> c : ctrlComps)
        {
            if (c.getFirst() != null)
            {
                ctrlBtns.add(c.getFirst(), cc.xy(c.getSecond(), 1));
            }
        }
        
        add(mainPanel, BorderLayout.CENTER);
        
        FormLayout      formLayout = new FormLayout("f:p:g,4px,p", "2px,f:p:g,p:g,p:g");
        PanelBuilder    builder    = new PanelBuilder(formLayout);

        builder.add(controllerPane,      cc.xy(1,2));
        builder.add(ctrlBtns.getPanel(), cc.xy(3,2));

        if (!isReadOnly) {
        	uploadToolPanel = new UploadToolPanel(this, UploadToolPanel.EXPANDED);
        	uploadToolPanel.createUI();
        }

        builder.add(uploadToolPanel,     cc.xywh(1, 3, 3, 1));
        builder.add(findPanel,           cc.xywh(1, 4, 3, 1));


        add(builder.getPanel(), BorderLayout.SOUTH);
        
        if (!isForBatchEdit) {
            resultsetController.addListener(new ResultSetControllerListener() {
                public boolean indexAboutToChange(int oldIndex, int newIndex) {
                    return true;
                }

                public void indexChanged(int newIndex) {
                    if (imageFrame != null) {
                        if (newIndex > -1) {
                            int index = spreadSheet.convertRowIndexToModel(newIndex);
                            imageFrame.setRow(workbench.getRow(index));
                        } else {
                            imageFrame.setRow(null);
                        }
                    }
                }

                public void newRecordAdded() {
                    // do nothing
                }
            });
        }
        if (showImageView || hasOneOrMoreImages)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    toggleImageFrameVisible();

                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            final Frame f = (Frame)UIRegistry.get(UIRegistry.FRAME);
                            f.toFront();
                            f.requestFocus();
                        }
                    });
                }
            });
        }
        
        ((WorkbenchTask) ContextMgr.getTaskByClass(WorkbenchTask.class)).opening(this);
        if (ContextMgr.getTaskByClass(SGRTask.class) != null) {
        	((SGRTask) ContextMgr.getTaskByClass(SGRTask.class)).opening(this);
        }
        if (workbenchValidator == null && (isDoIncrementalValidation() || isDoIncrementalMatching())) {
            if (uploadToolPanel != null) {
                uploadToolPanel.turnOffSelections();
            }
            if (isDoIncrementalValidation()) {
                turnOffIncrementalValidation();
            }
            if (isDoIncrementalMatching()) {
                turnOffIncrementalMatching();
            }
            model.fireDataChanged();
        }
    }

    @Override
    public Icon getIcon() {
        if ((getSrcTask() instanceof BatchEditTask || getSrcTask() instanceof QueryTask)
                && isUpdateDataSet()) {
            Taskable beTask = ContextMgr.getTaskByClass(BatchEditTask.class);
            return beTask != null ? beTask.getIcon(Taskable.StdIcon16) : super.getIcon();
        } else {
            return super.getIcon();
        }
    }

    public Taskable getSrcTask() {
        return srcTask;
    }

    public void setSrcTask(Taskable srcTask) {
        this.srcTask = srcTask;
    }

    /**
     * re-load selected rows or all rows if none are selected
     */
    protected void revertRows() {
    	for (WorkbenchRow row : workbench.getWorkbenchRowsAsList()) {
    		restoreOriginalValues(row.getWorkbenchDataItems());
    	}
    	revertBtn.setEnabled(false);
    	saveBtn.setEnabled(false);
        validateRows(0, spreadSheet.getRowCount()-1);
    }
    /**
     * @return
     */
    public List<UploadField> getAutoAssignableFlds() {
    	List <UploadField> result = null;
    	if (workbenchValidator == null) {
    		buildValidator(true);
    	}
    	if (workbenchValidator != null) {	
    		result = workbenchValidator.getUploader().getAutoAssignableFields();
    	}
    	return result;
    }
    
    /**
     * @return
     */
    public boolean isUpdateDataSet() {
    	boolean result = false;
    	if (workbenchValidator == null) {
    		buildValidator(true);
    	}
    	if (workbenchValidator != null) {	
    		result = workbenchValidator.getUploader().isUpdateUpload();
    	}
    	return result;
    }
    /**
     * @param show
     */
    public void showHideSgrCol(boolean show)
    {
	   // sgrColExt.setVisible(show);
    }
    
    /**
     * 
     */
    public void sgrSort()
    {
//        int sgrColIndex = model.getSgrHeading().getViewOrder();
//        spreadSheet.setSortOrder(sgrColIndex, SortOrder.DESCENDING);
    }
    
    /**
     * @return true if automatch or autovalidate is on
     */
    protected boolean getIncremental()
    {
    	return doIncrementalValidation || doIncrementalMatching || doIncrementalEditChecking;
    	
    }
    
    protected void showUploadToolPanel()
    {
    	uploadToolPanel.expand();
    }
    
    protected void hideUploadToolPanel()
    {
    	uploadToolPanel.contract(300);
    }

    protected void hideUploadToolPanelSansAnimation() {
        uploadToolPanel.contract(1);
    }
    /**
	 * @return the doIncrementalValidation
	 */
	public boolean isDoIncrementalValidation() 
	{
		return doIncrementalValidation;
	}

	/**
	 * turns on incremental validation
	 */
	public void turnOnIncrementalValidation() 
	{
		doIncrementalValidation = true;
		
		if (workbenchValidator == null)
		{
			buildValidator();
		}
		//XXX If incremental matching is already turned on it would save lots
		//of time if validateAll could be called without matching all.
		validateAll();
		
		AppPreferences.getLocalPrefs().putBoolean(wbAutoValidatePrefName + "." + workbench.getId(), doIncrementalValidation);
	}

	/**
	 * @return the doIncrementalMatching
	 */
	public boolean isDoIncrementalMatching() 
	{
		return doIncrementalMatching;
	}

	/**
	 * @return
	 */
	public boolean isDoIncrementalEditChecking() {
		return doIncrementalEditChecking;
	}
	
	/**
	 * 
	 */
	public void turnOnIncrementalEditChecking() {
		doIncrementalEditChecking = true;
		if (workbenchValidator == null) {
			buildValidator();
		}
		if (workbenchValidator != null) {
			//XXX If incremental matching is already turned on it would save lots
			//of time if validateAll could be called without matching all.
			validateAll();
			
			AppPreferences.getLocalPrefs().putBoolean(wbAutoEditCheckPrefName + "." + workbench.getId(), doIncrementalEditChecking);
		}
	}

	/**
	 * @param isNext
	 */
	public void goToEditedCell(final boolean isNext) {
    	Pair<Integer, Integer> editedCell = getNextCellWithStat(isNext, WorkbenchDataItem.VAL_EDIT);
    	if (editedCell != null) {
            if (spreadSheet.getCellEditor() != null) {
                spreadSheet.getCellEditor().stopCellEditing();
            }
            int row = editedCell.getFirst();
            int col = editedCell.getSecond();
            spreadSheet.getSelectionModel().setSelectionInterval(row, row);
            spreadSheet.getColumnModel().getSelectionModel().setSelectionInterval(col, col);
            spreadSheet.scrollCellToVisible(row, col);
            //spreadSheet.editCellAt(invalidCell.getFirst(), invalidCell.getSecond());
    	}
	}
	
	/**
	 * 
	 */
	public void turnOffIncrementalEditChecking() {
		boolean savedBlockChanges = blockChanges;
		try
		{
			blockChanges = true;
			shutdownValidators();
			doIncrementalEditChecking = false;
			updateBtnUI();
			//workbenchValidator = null;
			model.fireDataChanged();
			AppPreferences.getLocalPrefs().putBoolean(wbAutoEditCheckPrefName+ "." + workbench.getId(), doIncrementalEditChecking);
		} finally
		{
			blockChanges = savedBlockChanges;
		}
	}

	/**
	 * @param rows
	 * @param col
	 * @return
	 */
	public List<WorkbenchDataItem> getDataItems(final int[] rows, final Integer col) {
		List<WorkbenchDataItem> result = new ArrayList<WorkbenchDataItem>();
		for (int r : rows) {
			result.add(workbench.getRow(r).getItems().get(col.shortValue()));
		}
		return result;
	}
	
	/**
	 * @param items
	 */
	public void restoreOriginalValues(Collection<WorkbenchDataItem> items) {
		for (WorkbenchDataItem item : items) {
			if (item != null && (item.getEditorValidationStatus() & WorkbenchDataItem.VAL_EDIT) != 0) {
				restoreOriginalValue(item);
			}
		}
		updateBtnUI();
	}
	
	/**
	 * @param item
	 */
	protected void restoreOriginalValue(final WorkbenchDataItem item) {
		String exp = UIRegistry.getResourceString("WB_EDITED_CELL_TT");
		String before = StringUtils.substringBefore(exp, "%s");
		String after = StringUtils.substringAfter(exp, "%s");
		String[] originalValues = item.getStatusText().split("\n; ");
        for (String originalVal : originalValues) {
            String originalValue = originalVal;
            if (originalValue.contains(before)) {
                originalValue = StringUtils.replaceOnce(originalValue, before, "");
                originalValue = StringUtils.reverse(StringUtils.replaceOnce(StringUtils.reverse(originalValue), after, ""));
                if (originalValue.startsWith("'") && originalValue.endsWith("'")) {
                    originalValue = originalValue.substring(1, originalValue.length() - 1);
                }
                if (originalValue == null || "empty".equalsIgnoreCase(originalValue)) {
                    originalValue = "";
                }
                item.setCellData(originalValue);
            }
        }
	}
	
	
	/**
	 * @return editedCellCount after decrementing
	 */
	protected Integer decrementEditedCellCount() {
		Integer cnt = editedCellCount.decrementAndGet();
		if (cnt == 0) {
			//it probably better to do this enabling in the pre-existing button-enabling methods
			revertBtn.setEnabled(false);
			saveBtn.setEnabled(false);
		}
		return cnt;
	}
	
	/**
	 * turns on incremental matching
	 */
	public void turnOnIncrementalMatching() 
	{
		doIncrementalMatching = true;
		
		if (workbenchValidator == null)
		{
			buildValidator();
		}
		if (workbenchValidator != null)
		{
			setMatchStatusForUploadTables();
			validateAll();
		
			AppPreferences.getLocalPrefs().putBoolean(wbAutoMatchPrefName + "." + workbench.getId(), doIncrementalMatching);
		}
	}

	/**
	 * Set up match status behavior. Currently this just sets showMatchInfo on agents and trees,
	 * and turns it off for all other tables. This could be customized but would require
	 * some datamodel changes, I think, because using preference might be tricky since the prefs
	 * would need to be checked and modified or cleared whenever the wb structure was modified.  
	 */
	protected void setMatchStatusForUploadTables()
	{
		workbenchValidator.getUploader().setDefaultMatchStatus();
	}
	
	/**
     * Checks the cell for cell editing and stops it.
     */
    public boolean checkCurrentEditState()
    {
        boolean isOK = true;
        if (currentPanelType == PanelType.Spreadsheet)
        {
            if (spreadSheet != null && spreadSheet.getCellEditor() != null)
            {
                int index = spreadSheet.getSelectedRow();
                isOK = spreadSheet.getCellEditor().stopCellEditing();
                if (index >= 0)
                {
                    spreadSheet.setRowSelectionInterval(index, index);
                }
            }
        } else {
            if (formPane != null) {
                formPane.copyDataFromForm();
            }
        }
        return isOK;
    }
    
    
    /**
     * @param isNext
     * @return
     */
    protected Pair<Integer, Integer> getNextCellWithStat(boolean isNext, short stats) {
    	int startRow = spreadSheet.getSelectedRow() >= 0 ? spreadSheet.getSelectedRow() : 0;
    	int startCol = spreadSheet.getSelectedColumn() >= 0 ? spreadSheet.getSelectedColumn() : 0;
    	int increment = isNext ? 1 : -1;
    	int currentRow = startRow;
    	int currentCol = startCol + increment;
    	if (currentCol < 0 || currentCol == spreadSheet.getColumnCount()) {
    		if (currentCol < 0) {
    			currentCol = spreadSheet.getColumnCount() - 1; //XXX what about attachment column?
    		} else {
    			currentCol = 0;
    		}
    		currentRow += increment;
    		if (currentRow < 0) {
    			currentRow = spreadSheet.getRowCount() - 1 ;
    		} else if (currentRow == spreadSheet.getRowCount()) {
    			currentRow = 0;
    		}  
    	}
    	boolean lastRow = false;
    	do {
    		Hashtable<Short, WorkbenchDataItem> rowItems = workbench.getRow(spreadSheet.convertRowIndexToModel(currentRow)).getItems();
    		do {
    	    	WorkbenchDataItem di = rowItems.get(new Short((short )spreadSheet.convertColumnIndexToModel(currentCol)));
        	    if (di != null && (stats & di.getEditorValidationStatus()) != 0) {
    	    		return new Pair<>(currentRow, currentCol);
    	    	}		
    			currentCol += increment;
    			
    		} while (currentCol >= 0 && currentCol < spreadSheet.getColumnCount() && (!lastRow || currentCol != startCol));
	    	if (currentCol < 0) {
	    		currentCol = spreadSheet.getColumnCount() - 1; //XXX what about attachment column?
	    	} else if (currentCol == spreadSheet.getColumnCount()) {
	    		currentCol = 0;
	    	}
    		if (!lastRow) {
    			currentRow += increment;
    			if (currentRow < 0) {
    				currentRow = spreadSheet.getRowCount() - 1 ;
    			} else if (currentRow == spreadSheet.getRowCount()) {
    				currentRow = 0;
    			}
    		}
    		lastRow = !lastRow && currentRow == startRow;
    	} while (currentRow != startRow || lastRow);
    	return null;
    }
    
    /**
     * @param isNext
     */
    public void goToInvalidCell(boolean isNext) {
    	Pair<Integer, Integer> invalidCell = getNextCellWithStat(isNext, WorkbenchDataItem.VAL_ERROR);
    	if (invalidCell != null) {
            if (spreadSheet.getCellEditor() != null) {
                spreadSheet.getCellEditor().stopCellEditing();
            }
            int row = invalidCell.getFirst();
            int col = invalidCell.getSecond();
            spreadSheet.getSelectionModel().setSelectionInterval(row, row);
            spreadSheet.getColumnModel().getSelectionModel().setSelectionInterval(col, col);
            spreadSheet.scrollCellToVisible(row, col);
    	}
    }
    

    /**
     * @param isNext
     */
    public void goToUnmatchedCell(boolean isNext) {
    	Set<Short> stats = new HashSet<Short>();
    	stats.add(WorkbenchDataItem.VAL_MULTIPLE_MATCH);
    	stats.add(WorkbenchDataItem.VAL_NEW_DATA);
    	Pair<Integer, Integer> invalidCell = getNextCellWithStat(isNext,(short)(WorkbenchDataItem.VAL_MULTIPLE_MATCH | WorkbenchDataItem.VAL_NEW_DATA));
    	if (invalidCell != null)
    	{
            if (spreadSheet.getCellEditor() != null)
            {
                spreadSheet.getCellEditor().stopCellEditing();
            }
            spreadSheet.getSelectionModel().setSelectionInterval(invalidCell.getFirst(), invalidCell.getFirst());
            spreadSheet.getColumnModel().getSelectionModel().setSelectionInterval(invalidCell.getSecond(), invalidCell.getSecond());
            spreadSheet.scrollCellToVisible(invalidCell.getFirst(), invalidCell.getSecond());
            //spreadSheet.editCellAt(invalidCell.getFirst(), invalidCell.getSecond());
    	}
    }

    /**
     * Update enaabled state of buttons effected by the spreadsheet selection.
     */
    protected void updateBtnUI()
    {
        boolean enable = spreadSheet.getSelectedRow() > -1;
        for (JButton btn: selectionSensitiveButtons)
        {
           if (btn != null)
           {
               btn.setEnabled(enable);
           }
        }
        enable = workbench.getWorkbenchRows().size() < WorkbenchTask.MAX_ROWS;
        if (!isReadOnly && addRowsBtn != null)
        {
            addRowsBtn.setEnabled(enable);
        }
        if (!isReadOnly)
        {
            resultsetController.getNewRecBtn().setEnabled(enable && !isReadOnly);
        }
        
        uploadToolPanel.updateBtnUI();
        updateUploadBtnState();
}
    /**
     * @return number of invalid cells
     */
    public int getInvalidCellCount()
    {
    	return invalidCellCount.get();
    }
    
    /**
     * @return number of unmatched cells
     */
    public int getUnmatchedCellCount()
    {
    	return unmatchedCellCount.get();
    }
    
    /**
     * @return
     */
    public int getEditedCellCount() {
    	return editedCellCount.get();
    }
    
    /**
     * Adds a Key mappings.
     * @param comp comp
     * @param keyCode keyCode
     * @param actionName actionName
     * @param action action 
     * @return the action
     */
    public Action addRecordKeyMappings(final JComponent comp, final int keyCode, final String actionName, final Action action,
    		int modifiers)
    {
        InputMap  inputMap  = comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = comp.getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke(keyCode, modifiers), actionName);
        actionMap.put(actionName, action);
        
        //UIRegistry.registerAction(actionName, action);
        return action;
    }
    
    /**
     * Setup the row (or selection) listener for the the Image Window. 
     */
    protected void setupWorkbenchRowChangeListener()
    {
        workbenchRowChangeListener = new ListSelectionListener()
        {
            private int previouslySelectedRowIndex = -1;
            
            @SuppressWarnings("synthetic-access")
            public void valueChanged(ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting() || !imageFrame.isVisible())
                {
                    // ignore this until the user quits changing the selection
                    return;
                }
                
                if (spreadSheet.getCellEditor() != null)
                {
                    spreadSheet.getCellEditor().stopCellEditing();
                }
                
                // check to make sure the selection actually changed
                int newSelectionIndex = spreadSheet.getSelectedRow();
                if (newSelectionIndex != previouslySelectedRowIndex)
                {
                    previouslySelectedRowIndex = newSelectionIndex;
                    
                    showCardImageForSelectedRow();
                }
            }
        };
    }
    
    /**
     * Adds a row at the end.
     */
    protected void addRowAtEnd()
    {
        checkCurrentEditState();
        model.appendRow();
        
        resultsetController.setLength(model.getRowCount());
        
        adjustSelectionAfterAdd(model.getRowCount()-1);
        
        updateBtnUI();
    }
    
    /**
     * In form view, adds a row after the current row. 
     * 
     * In spreadsheet view adds getSelectedRowCount() new rows after the last selected row.
     */
    public void addRowAfter()
    {
        spreadSheet.clearSorter();

        checkCurrentEditState();

        int curSelInx = getCurrentIndexFromFormOrSS();
        int rowsToInsert = 1;
        if (currentPanelType == PanelType.Spreadsheet && spreadSheet.getSelectedRowCount() != 0)
        {
            int[] sels = spreadSheet.getSelectedRows();
            rowsToInsert += sels.length - 1;            
            curSelInx = sels[sels.length-1];
        }
        for (int r = 0; r < rowsToInsert && curSelInx < WorkbenchTask.MAX_ROWS-1; r++, curSelInx++)
            model.insertAfterRow(curSelInx);
        
        int count = model.getRowCount();
        resultsetController.setLength(count);

        adjustSelectionAfterAdd(count == 1 ? 0 : curSelInx);

        updateBtnUI();
    }
    
    /**
     * Inserts a Row above the selection.
     */
    protected void insertRowAbove()
    {
        checkCurrentEditState();
        int curSelInx = getCurrentIndexFromFormOrSS();
        model.insertRow(curSelInx);
        
        resultsetController.setLength(model.getRowCount());
        
        adjustSelectionAfterAdd(curSelInx);
        
        updateBtnUI();

    }
    
    /**
     * Adjusts the selection.
     * @param rowIndex the index to be selected
     */
    protected void adjustSelectionAfterAdd(final int rowIndex)
    {
        if (currentPanelType == PanelType.Spreadsheet)
        {
            //log.debug("ss.RowCnt: "+spreadSheet.getRowCount()+" ss.SelRow"+spreadSheet.getSelectedRow()+" model.RowCnt: "+model.getRowCount());
            spreadSheet.scrollToRow(rowIndex);
            spreadSheet.setRowSelectionInterval(rowIndex, rowIndex);
            spreadSheet.setColumnSelectionInterval(0, spreadSheet.getColumnCount()-1);
            spreadSheet.repaint();
            //log.debug("ss.RowCnt: "+spreadSheet.getRowCount()+" ss.SelRow"+spreadSheet.getSelectedRow()+" model.RowCnt: "+model.getRowCount());
            
        } else
        {
            resultsetController.setIndex(rowIndex);
        }
    }
    
    /**
     * @param rows
     * 
     * Checks for invalid/new columns and adjusts validation data structures
     */
    protected void updateValidationStatusForDelete(final int[] rows)
    {
		Vector<WorkbenchRow> wbRows = workbench.getWorkbenchRowsAsList();
		int invalids = 0;
		int news = 0;
		int edits = 0;
    	for (int r : rows) {
    		WorkbenchRow wbRow = wbRows.get(r);
    		for (WorkbenchDataItem wbdi : wbRow.getWorkbenchDataItems()) {
    			short status = (short )wbdi.getEditorValidationStatus();
    			if ((status & WorkbenchDataItem.VAL_ERROR) != 0) {
    				invalids++;
    			} else if ((status & WorkbenchDataItem.VAL_MULTIPLE_MATCH) != 0
    					|| (status & WorkbenchDataItem.VAL_NEW_DATA) != 0) {
    				news++;
    			} else if ((status & WorkbenchDataItem.VAL_EDIT) != 0) {
    				edits++;
    			}
    		}
    	}
    	if (invalids > 0)
    	{
    		invalidCellCount.set(Math.max(0, invalidCellCount.get() - invalids));
    	} 
    	if (news > 0)
    	{
    		unmatchedCellCount.set(Math.max(0, unmatchedCellCount.get() - news));
    	}
    	if (edits > 0) {
    		editedCellCount.set(Math.max(0,  editedCellCount.get() - edits));
    	}
    }
    /**
     * Deletes the Selected Rows. 
     */
    protected void deleteRows()
    {
        spreadSheet.clearSorter();

        checkCurrentEditState();
        int[] rows;
        if (currentPanelType == PanelType.Spreadsheet)
        {
            rows = spreadSheet.getSelectedRowModelIndexes();
            if (rows.length == 0)
            {
                return;
            }
        } else
        {
            rows = new int[1];
            rows[0] = spreadSheet.convertRowIndexToModel(resultsetController.getCurrentIndex());
        }
        
        int firstRow = rows[0];

        resultsetController.setLength(model.getRowCount() - rows.length);

        
//        if (validationWorkerQueue.peek() != null)
//        {
//        	synchronized(validationWorkerQueue)
//        	{
//        		for (int r : rows)
//        		{
//        			validationWorkerQueue.peek().rowDeleted(r);
//        		}
//        	}
//        }

        //Or Just wait until validation is done.
        while (validationWorkerQueue.peek() != null) 
        {
        	//System.out.println("waiting for validation workers to finish");
        	//sit and wait)
        }

        if (isDoIncremental())
        {
        	updateValidationStatusForDelete(rows);
        }
        
        model.deleteRows(rows);

        int rowCount = workbench.getWorkbenchRowsAsList().size();
        
        if (rowCount == 0)
        {
        	spreadSheet.getSelectionModel().clearSelection();        	
        	addRowAtEnd();
        }
        
        if (currentPanelType == PanelType.Spreadsheet)
        {
            if (rowCount > 0)
            {
                if (firstRow >= rowCount)
                {
                    spreadSheet.setRowSelectionInterval(rowCount-1, rowCount-1);
                } else
                {
                    spreadSheet.setRowSelectionInterval(firstRow, firstRow);
                }
                spreadSheet.setColumnSelectionInterval(0, spreadSheet.getColumnCount()-1);
            } else
            {
                spreadSheet.getSelectionModel().clearSelection();
            }
        } else
        {
            int currentRow = resultsetController.getCurrentIndex();
            resultsetController.setLength(rowCount);
            if (currentRow >= rowCount)
            {
                resultsetController.setIndex(rowCount-1);
            } else
            {
                resultsetController.setIndex(currentRow);
            }
        }
        
        updateBtnUI();

    }
    
    /**
     * @return the resultset controller
     */
    public ResultSetController getResultSetController()
    {
        return resultsetController;
    }

    /**
     * Tells the model there is new data.
     */
    public void addRowToSpreadSheet()
    {
        if (spreadSheet != null)
        {
            spreadSheet.addRow();
            model.fireTableStructureChanged();
            
            resultsetController.setLength(model.getRowCount());
            
            setChanged(true);
            
            updateBtnUI();
        }
    }
    
    /**
     * Tells the model there is new data.
     */
    public void newImagesAdded()
    {
       /* if (currentPanelType == PanelType.Form)
        {
            resultsetController.setIndex(model.getRowCount());
        } else
        {
            spreadSheet.setRowSelectionInterval(currentRow, currentRow);
            spreadSheet.setColumnSelectionInterval(0, spreadSheet.getColumnCount()-1);
            spreadSheet.scrollToRow(Math.min(currentRow+4, model.getRowCount()));

        }
        */
    	if (getIncremental())
    	{
    		validateAll();
    	}
        adjustSelectionAfterAdd(model.getRowCount()-1);
    }
    
    /**
     * Tells the GridModel to update itself. 
     */
    public void gridColumnsUpdated()
    {
        model.fireTableStructureChanged();
        
        // the above call results in new TableColumnExt objects for each column, it appears
        
        // re-get the column extension object
        int imageColIndex = model.getColumnCount() - 1;
        imageColExt = spreadSheet.getColumnExt(imageColIndex);
        imageColExt.setVisible(false);
        
        // show the column if the image frame is showing
        if (imageFrame.isVisible())
        {
            imageColExt.setVisible(true);
        }

        int currentRecord = resultsetController.getCurrentIndex();
        spreadSheet.setRowSelectionInterval(currentRecord, currentRecord);
    }
    
    /**
     * Show image for a selected row. 
     */
    public void refreshImagesForSelectedRow()
    {
        if (imageFrame != null)
        {
            imageFrame.refreshImages();
        }
    }
    
    /**
     * Show image for a selected row. 
     */
    public void showCardImageForSelectedRow()
    {
        int selectedIndex = getCurrentIndex();
        if (selectedIndex == -1)
        {
            // no selection
            log.debug("No selection, so removing the card image");
            imageFrame.setRow(null);
            return;
        }

        log.debug("Showing image for row " + selectedIndex);
        int index = spreadSheet.convertRowIndexToModel(selectedIndex);
        
        WorkbenchRow row = workbench.getWorkbenchRowsAsList().get(index);
        imageFrame.setRow(row);
    }
    
    /**
     * Returns the Workbench for the Pane.
     * @return the current workbench
     */
    public Workbench getWorkbench()
    {
        return workbench;
    }

    /**
     * The grid to form switcher.
     * @return The grid to form switcher.
     */
    public DropDownButtonStateful createSwitcher()
    {
        Vector<DropDownMenuInfo> menuItems = new Vector<DropDownMenuInfo>();
        menuItems.add(new DropDownMenuInfo(getResourceString("Form"), 
                IconManager.getImage("EditForm20", IconManager.IconSize.NonStd), 
                getResourceString("WB_SHOW_GRID_VIEW")));
        menuItems.add(new DropDownMenuInfo(getResourceString("Grid"), 
                IconManager.getImage("Spreadsheet20", IconManager.IconSize.NonStd), 
                getResourceString("WB_SHOW_FORM_VIEW")));
        final DropDownButtonStateful switcher = new DropDownButtonStateful(menuItems);
        switcher.setCurrentIndex(1);
        switcher.setToolTipText(getResourceString("SwitchViewsTT"));
        switcher.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                PanelType panel = switcher.getCurrentIndex() == 1 ? PanelType.Spreadsheet : PanelType.Form;
            	showPanel(panel);
            	if (panel == PanelType.Form && getIncremental() && formPane != null) {
            		formPane.updateValidationUI();
            	}
            }
        });
        switcher.validate();
        switcher.doLayout();
        return switcher;
    }
    
    /**
     * Returns the current selected index from a spreadsheet or the form.
     * @return the current selected index from a spreadsheet or the form.
     */
    protected int getCurrentIndex()
    {
        int selectedInx;
        if (currentPanelType == PanelType.Spreadsheet)
        {
            selectedInx = spreadSheet.getSelectedRow();
        } else
        {
            selectedInx = resultsetController.getCurrentIndex();
        }
        
//        if (selectedInx > -1)
//        {
//            selectedInx = spreadSheet.convertRowIndexToModel(selectedInx);
//        }
    
        return selectedInx;
    }
    
    /**
     * Returns the current selected index from a spreadsheet or the form.
     * @return the current selected index from a spreadsheet or the form.
     */
    protected int getCurrentIndexFromFormOrSS()
    {
        int selectedInx = getCurrentIndex();
        
        if (selectedInx == -1)
        {
            return 0;
        }
        
        if (spreadSheet.getRowCount() == 0)
        {
            return 0;
        }
        return selectedInx == -1 ? spreadSheet.getRowCount() : selectedInx;
    }
    
    public int getSelectedIndexFromView()
    {
        return spreadSheet.getSelectedRow();
    }
    
    /**
     * Shows the grid or the form.
     * @param panelType
     */
    public void showPanel(final PanelType panelType)
    {
        checkCurrentEditState();
        
        currentRow = getCurrentIndexFromFormOrSS();
        
        currentPanelType = panelType;
        
        cardLayout.show(mainPanel, currentPanelType.toString());
        cpCardLayout.show(controllerPane, currentPanelType.toString());
        
        switch (currentPanelType) {
            case Spreadsheet:
                if (formPane != null) {
                    formPane.aboutToShowHide(false);
                }
                // Showing Spreadsheet and hiding form
                if (model.getRowCount() > 0) {
                    spreadSheet.setRowSelectionInterval(currentRow, currentRow);
                    spreadSheet.setColumnSelectionInterval(0, spreadSheet.getColumnCount()-1);
                    spreadSheet.scrollToRow(Math.min(currentRow+4, model.getRowCount()));
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run()
                        {            
                            spreadSheet.requestFocus();
                        }
                    });
                }
                // Enable the "Find" action in the Edit menu when a spreadsheet is shown
                UIRegistry.enableFind(findPanel, true);
                ssFormSwitcher.setCurrentIndex(1);
                break;
            case Form:
                // About to Show Form and hiding Spreadsheet
                
                // cancel any editing in a cell in the spreadsheet
                checkCurrentEditState(); 
                
                // Tell the form we are switching and that it is about to be shown
                formPane.aboutToShowHide(true);
                
                // -1 will tell the form to disable
                resultsetController.setIndex(model.getRowCount() > 0 ? currentRow : -1);
                
                // Hide the find/replace panel when you switch to form view
                findPanel.getHideFindPanelAction().hide();
                
                // Disable the ctrl-F from the edit menu
                UIRegistry.disableFindFromEditMenu();
                
//                if(task instanceof SGRTask)
//                    NavBoxMgr.getInstance().closeSplitter();
                
                ssFormSwitcher.setCurrentIndex(0);
                break;
        }
        
             
        JComponent[] comps = { addRowsBtn, deleteRowsBtn, clearCellsBtn};
        for (JComponent c : comps) {
            if (c != null) {
                c.setVisible(currentPanelType == PanelType.Spreadsheet && !isUpdateDataSet());
            }
        }
    }
    
    /**
     * Shows / Hides the Image Window. 
     */
    public void toggleImageFrameVisible()
    {
        if (spreadSheet.getCellEditor() != null)
        {
            spreadSheet.getCellEditor().stopCellEditing();
        }

        boolean isVisible = imageFrame.isVisible();
        
        setImageFrameVisible(!isVisible);
    }
    
    /**
     * Shows / Hides the Image Window. 
     */
    public void toggleImportImageFrameVisible()
    {
        if (spreadSheet.getCellEditor() != null)
        {
            spreadSheet.getCellEditor().stopCellEditing();
        }

        boolean isVisible = imageImportFrame.isVisible();
        
        setImageFrameVisible(!isVisible, imageImportFrame, toggleImageFrameBtn, "WB_SHOW_IMG_WIN", "WB_HIDE_IMG_WIN", "WorkbenchWorkingWithImages");
    }
    
    /**
     * Shows / Hides the Image Window. 
     */
    public void setImageFrameVisible(boolean visible)
    {
        setImageFrameVisible(visible, imageFrame, toggleImageFrameBtn, "WB_SHOW_IMG_WIN", "WB_HIDE_IMG_WIN", "WorkbenchWorkingWithImages");
    }
    
    /**
     * Shows / Hides the Image Window. 
     */
    public void setImageFrameVisible(final boolean visible, 
                                     final JFrame imgFrame, 
                                     final JButton toolBtn,
                                     final String ttHelpVisible,
                                     final String ttHelpHidden,
                                     final String helpContext)
    {
        if (visible == imgFrame.isVisible())
        {
            return;
        }
        
        if (spreadSheet.getCellEditor() != null)
        {
            spreadSheet.getCellEditor().stopCellEditing();
        }

        // and add or remove the ListSelectionListener (to avoid loading images when not visible)
        if (!visible)
        {
            spreadSheet.setTransferHandler(null);

            // hide the image window
            
            // turn off alwaysOnTop for Swing repaint reasons (prevents a lock up)
            if (imgFrame.isAlwaysOnTop())
            {
                imgFrame.setAlwaysOnTop(false);
            }
            // if the image frame is minimized or iconified, set it to fully visible before doing anything else
            if (imgFrame.getState() == Frame.ICONIFIED)
            {
                imgFrame.setState(Frame.NORMAL);
            }
            toolBtn.setToolTipText(getResourceString(ttHelpVisible));

            spreadSheet.getSelectionModel().removeListSelectionListener(workbenchRowChangeListener);
            
            // set the image window and the image column invisible
            imgFrame.setVisible(false);
            imageColExt.setVisible(false);
        }
        else
        {
            spreadSheet.setTransferHandler(new WBImageTransferable());

            // show the image window
            
            UIHelper.positionFrameRelativeToTopFrame(imgFrame);
            
            // when a user hits the "show image" button, for some reason the selection gets nullified
            // so we'll grab it here, then set it at the end of this method

            toolBtn.setToolTipText(getResourceString(ttHelpHidden));

            spreadSheet.getSelectionModel().addListSelectionListener(workbenchRowChangeListener);
            HelpMgr.setHelpID(this, helpContext);
            
            // set the image window and the image column visible
            imgFrame.setVisible(true);
            imageColExt.setVisible(true);

            // if the image frame is minimized or iconified, set it to fully visible before doing anything else
            if (imgFrame.getState() == Frame.ICONIFIED)
            {
                imgFrame.setState(Frame.NORMAL);
            }

            showCardImageForSelectedRow();
            
            // Without this code below the Image Column doesn't get selected
            // when toggling 
            if (currentPanelType == PanelType.Spreadsheet && currentRow != -1)
            {
                spreadSheet.setRowSelectionInterval(currentRow, currentRow);
                spreadSheet.setColumnSelectionInterval(0, spreadSheet.getColumnCount()-1);
                spreadSheet.scrollToRow(Math.min(currentRow+4, model.getRowCount()));
            }
            
            TableColumn column = spreadSheet.getTableHeader().getColumnModel()
					.getColumn(spreadSheet.getTableHeader().getColumnModel()
									.getColumnCount() - 1);
			column.setCellRenderer(new WbCellRenderer());
            spreadSheet.repaint();
        }
    }
    
    /**
     * Loads a new Image into a WB Row.
     * XXX Note this needs to to be refactored so both the WorkbenchTask and this class use the same image load method.
     * 
     * @param row the row of the new card image
     * @return true if the row was set
     */
    protected boolean loadNewImage(final WorkbenchRow row)
    {
        ImageFilter imageFilter = new ImageFilter();
        JFileChooser fileChooser = new JFileChooser(WorkbenchTask.getDefaultDirPath(WorkbenchTask.IMAGES_FILE_PATH));
        fileChooser.setFileFilter(imageFilter);
        
        int          userAction  = fileChooser.showOpenDialog(this);
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        
        localPrefs.put(WorkbenchTask.IMAGES_FILE_PATH, fileChooser.getCurrentDirectory().getAbsolutePath());
        if (userAction == JFileChooser.APPROVE_OPTION)
        {
            String fullPath = fileChooser.getSelectedFile().getAbsolutePath();
            if (imageFilter.isImageFile(fullPath))
            {
                File chosenFile = fileChooser.getSelectedFile();
                row.setCardImage(chosenFile);
                row.setCardImageFullPath(chosenFile.getAbsolutePath());
                if (row.getLoadStatus() != WorkbenchRow.LoadStatus.Successful)
                {
                    if (!WorkbenchTask.showLoadStatus(row, false))
                    {
                        return false;
                    }
                }
                return true;
            }

            
            // turn off alwaysOnTop for Swing repaint reasons (prevents a lock up)
            if (imageFrame != null)
            {
                imageFrame.setAlwaysOnTop(false);
            }
            JOptionPane.showMessageDialog(UIRegistry.getMostRecentWindow(),
                                          String.format(getResourceString("WB_WRONG_IMAGE_TYPE"), 
                                                  new Object[] {FilenameUtils.getExtension(fullPath)}),
                                          UIRegistry.getResourceString("WARNING"), 
                                          JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    /**
     * Shows a dialog that enales the user to convert the lat/lon formats. 
     */
    protected void showGeoRefConvertDialog()
    {
        if (geoRefConvertDlg != null)
        {
        	geoRefConvertDlg.toFront();
        	return;
        }
        
    	UsageTracker.incrUsageCount("WB.ShowGeoRefConverter");
        
        JStatusBar statusBar = UIRegistry.getStatusBar();

        if (!workbench.containsGeoRefData())
        {
            statusBar.setErrorMessage(getResourceString("NoGeoRefColumns"));
            return;
        }

        List<String> outputFormats = new Vector<String>();
        String dddddd = getResourceString("DDDDDD");
        String ddmmmm = getResourceString("DDMMMM");
        String ddmmss = getResourceString("DDMMSS");
        String ddddddnsew = getResourceString("DDDDDDNSEW");
        String ddmmmmnsew = getResourceString("DDMMMMNSEW");
        String ddmmssnsew = getResourceString("DDMMSSNSEW");
        outputFormats.add(dddddd);
        outputFormats.add(ddmmmm);
        outputFormats.add(ddmmss);
        outputFormats.add(ddddddnsew);
        outputFormats.add(ddmmmmnsew);
        outputFormats.add(ddmmssnsew);
        
        final int locTabId = DBTableIdMgr.getInstance().getIdByClassName(Locality.class.getName());
        final int latColIndex = workbench.getColumnIndex(locTabId,"latitude1");
        final int lonColIndex = workbench.getColumnIndex(locTabId, "longitude1");
        final int lat2ColIndex = workbench.getColumnIndex(locTabId,"latitude2");
        final int lon2ColIndex = workbench.getColumnIndex(locTabId, "longitude2");

        JFrame mainFrame = (JFrame)UIRegistry.getTopWindow();
        
        String title       = UIRegistry.getResourceString("GeoRefConv");
        String description = UIRegistry.getResourceString("GeoRefConvDesc");
        final ToggleButtonChooserPanel<String> toggle = new ToggleButtonChooserPanel<String>(outputFormats, description, Type.RadioButton);
        final JCheckBox symbolCkBx = UIHelper.createCheckBox(UIRegistry.getResourceString("GEOREF_USE_SYMBOLS"));
        JPanel pane = new JPanel(new BorderLayout());
        pane.add(toggle, BorderLayout.CENTER);
        pane.add(symbolCkBx, BorderLayout.SOUTH);
        geoRefConvertDlg = new CustomDialog(mainFrame, title, false, CustomDialog.OKCANCEL, pane)
        {
            boolean converted = false;

            @Override
            public void setVisible(boolean visible)
            {
                super.setVisible(visible);
                
                Dimension prefSize = this.getPreferredSize();
                prefSize.width += 60;
                this.setSize(prefSize);
            }

            @Override
            protected void cancelButtonPressed()
            {
            	geoRefConvertDlg = null;
            	super.cancelButtonPressed();
            	if (converted) {
                    int[] selection = spreadSheet.getSelectedRowModelIndexes();
                    if (selection.length == 0) {
                        // if none are selected, map all of them
                        int rowCnt = spreadSheet.getRowCount();
                        selection = new int[rowCnt];
                        for (int i = 0; i < rowCnt; ++i) {
                            selection[i] = spreadSheet.convertRowIndexToModel(i);
                        }
                    }
                    if (selection.length > 0) {
                        validateRows(selection);

                    } else {
                        //validateRows(0, spreadSheet.getRowCount() - 1);
                        validateAll();
                    }
                }
            }
            
            @Override
            protected void okButtonPressed() {
                checkCurrentEditState();
                getOkBtn().setEnabled(false);
                // figure out which rows the user is working with
                int[] selection = spreadSheet.getSelectedRowModelIndexes();
                if (selection.length == 0) {
                    // if none are selected, map all of them
                    int rowCnt = spreadSheet.getRowCount();
                    selection = new int[rowCnt];
                    for (int i = 0; i < rowCnt; ++i) {
                        selection[i] = spreadSheet.convertRowIndexToModel(i);
                    }
                }

                // since Arrays.copyOf() isn't in Java SE 5...
                int[] selRows = new int[selection.length];
                for (int i = 0; i < selection.length; ++i) {
                    selRows[i] = selection[i];
                }


                // don't call super.okButtonPressed() b/c it will close the window
                isCancelled = false;
                btnPressed = OK_BTN;
                LatLonConverter.DEGREES_FORMAT degFmt = symbolCkBx.isSelected() ?
                        LatLonConverter.DEGREES_FORMAT.Symbol :
                        LatLonConverter.DEGREES_FORMAT.None;
                Vector<CellPosition> unconverted = new Vector<CellPosition>();
                switch (toggle.getSelectedIndex()) {
                    case 0: {
                        unconverted.addAll(convertColumnContents(latColIndex, selRows, new GeoRefConverter(), GeoRefFormat.D_PLUS_MINUS.name(),
                                LatLonConverter.LATLON.Latitude, degFmt));
                        unconverted.addAll(convertColumnContents(lonColIndex, selRows, new GeoRefConverter(), GeoRefFormat.D_PLUS_MINUS.name(),
                                LatLonConverter.LATLON.Longitude, degFmt));
                        unconverted.addAll(convertColumnContents(lat2ColIndex, selRows, new GeoRefConverter(), GeoRefFormat.D_PLUS_MINUS.name(),
                                LatLonConverter.LATLON.Latitude, degFmt));
                        unconverted.addAll(convertColumnContents(lon2ColIndex, selRows, new GeoRefConverter(), GeoRefFormat.D_PLUS_MINUS.name(),
                                LatLonConverter.LATLON.Longitude, degFmt));
                        break;
                    }
                    case 1: {
                        unconverted.addAll(convertColumnContents(latColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DM_PLUS_MINUS.name(),
                                LatLonConverter.LATLON.Latitude, degFmt));
                        unconverted.addAll(convertColumnContents(lonColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DM_PLUS_MINUS.name(),
                                LatLonConverter.LATLON.Longitude, degFmt));
                        unconverted.addAll(convertColumnContents(lat2ColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DM_PLUS_MINUS.name(),
                                LatLonConverter.LATLON.Latitude, degFmt));
                        unconverted.addAll(convertColumnContents(lon2ColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DM_PLUS_MINUS.name(),
                                LatLonConverter.LATLON.Longitude, degFmt));
                        break;
                    }
                    case 2: {
                        unconverted.addAll(convertColumnContents(latColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DMS_PLUS_MINUS.name(),
                                LatLonConverter.LATLON.Latitude, degFmt));
                        unconverted.addAll(convertColumnContents(lonColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DMS_PLUS_MINUS.name(),
                                LatLonConverter.LATLON.Longitude, degFmt));
                        unconverted.addAll(convertColumnContents(lat2ColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DMS_PLUS_MINUS.name(),
                                LatLonConverter.LATLON.Latitude, degFmt));
                        unconverted.addAll(convertColumnContents(lon2ColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DMS_PLUS_MINUS.name(),
                                LatLonConverter.LATLON.Longitude, degFmt));
                        break;
                    }
                    case 3: {
                        unconverted.addAll(convertColumnContents(latColIndex, selRows, new GeoRefConverter(), GeoRefFormat.D_NSEW.name(),
                                LatLonConverter.LATLON.Latitude, degFmt));
                        unconverted.addAll(convertColumnContents(lonColIndex, selRows, new GeoRefConverter(), GeoRefFormat.D_NSEW.name(),
                                LatLonConverter.LATLON.Longitude, degFmt));
                        unconverted.addAll(convertColumnContents(lat2ColIndex, selRows, new GeoRefConverter(), GeoRefFormat.D_NSEW.name(),
                                LatLonConverter.LATLON.Latitude, degFmt));
                        unconverted.addAll(convertColumnContents(lon2ColIndex, selRows, new GeoRefConverter(), GeoRefFormat.D_NSEW.name(),
                                LatLonConverter.LATLON.Longitude, degFmt));

                        break;
                    }
                    case 4: {
                        unconverted.addAll(convertColumnContents(latColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DM_NSEW.name(),
                                LatLonConverter.LATLON.Latitude, degFmt));
                        unconverted.addAll(convertColumnContents(lonColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DM_NSEW.name(),
                                LatLonConverter.LATLON.Longitude, degFmt));
                        unconverted.addAll(convertColumnContents(lat2ColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DM_NSEW.name(),
                                LatLonConverter.LATLON.Latitude, degFmt));
                        unconverted.addAll(convertColumnContents(lon2ColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DM_NSEW.name(),
                                LatLonConverter.LATLON.Longitude, degFmt));

                        break;
                    }
                    case 5: {
                        unconverted.addAll(convertColumnContents(latColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DMS_NSEW.name(),
                                LatLonConverter.LATLON.Latitude, degFmt));
                        unconverted.addAll(convertColumnContents(lonColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DMS_NSEW.name(),
                                LatLonConverter.LATLON.Longitude, degFmt));
                        unconverted.addAll(convertColumnContents(lat2ColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DMS_NSEW.name(),
                                LatLonConverter.LATLON.Latitude, degFmt));
                        unconverted.addAll(convertColumnContents(lon2ColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DMS_NSEW.name(),
                                LatLonConverter.LATLON.Longitude, degFmt));

                        break;
                    }
                }
                if (unconverted.size() != 0) {
                    UIRegistry.displayLocalizedStatusBarError("WB_UNCONVERTED_GEOREFS", unconverted.size());
                    final JList<?> unconvertedcells = UIHelper.createList(unconverted);
                    unconvertedcells.addListSelectionListener(new ListSelectionListener() {

                        @Override
                        public void valueChanged(ListSelectionEvent arg0) {
                            CellPosition rowCol = (CellPosition) unconvertedcells.getSelectedValue();
                            spreadSheet.scrollCellToVisible(rowCol.getFirst(), rowCol.getSecond());

                        }

                    });
                    JLabel lbl = UIHelper.createLabel(UIRegistry.getResourceString("WB_UNCONVERTED_GEOREFS_MSG"));
                    JPanel innerPane = new JPanel(new BorderLayout());
                    innerPane.add(lbl, BorderLayout.NORTH);
                    innerPane.add(unconvertedcells, BorderLayout.CENTER);
                    CustomDialog cd = new CustomDialog((Frame) UIRegistry.getTopWindow(), UIRegistry.getResourceString("WB_UNCONVERTED_GEOREFS_TITLE"),
                            false, CustomDialog.OKHELP, innerPane);
                    cd.setHelpContext("UnconvertableGeoCoords");
                    UIHelper.centerAndShow(cd);
                }
                getOkBtn().setEnabled(true);
                converted = true;
            }
        };
        geoRefConvertDlg.setModal(true);
        toggle.setSelectedIndex(0);
        toggle.setOkBtn(geoRefConvertDlg.getOkBtn());
        toggle.createUI();
        geoRefConvertDlg.addWindowListener(new WindowListener(){

			/* (non-Javadoc)
			 * @see java.awt.event.WindowStateListener#windowStateChanged(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosed(WindowEvent e)
			{
			    geoRefConvertDlg = null;
			}

			/* (non-Javadoc)
			 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowActivated(WindowEvent arg0)
			{
				// TODO Auto-generated method stub
				
			}

			/* (non-Javadoc)
			 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				// TODO Auto-generated method stub
				
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
        geoRefConvertDlg.setOkLabel(getResourceString("APPLY"));
        geoRefConvertDlg.setCancelLabel(getResourceString("CLOSE"));
        geoRefConvertDlg.setVisible(true);
    }
    
    /**
     * Show a map for any number of selected records.
     */
    protected void showMapOfSelectedRecords()
    {
        UsageTracker.incrUsageCount("WB.MapRows");
        
        log.debug("Showing map of selected records");
        //showMapBtn.setEnabled(false);
        int[] selection = spreadSheet.getSelectedRowModelIndexes();
        if (selection.length==0)
        {
            // if none are selected, map all of them
            int rowCnt = spreadSheet.getRowCount();
            selection = new int[rowCnt];
            for (int i = 0; i < rowCnt; ++i)
            {
                selection[i]=spreadSheet.convertRowIndexToModel(i);
            }
        }
        
        DBTableIdMgr databaseSchema = WorkbenchTask.getDatabaseSchema(false);
        // build up a list of temporary MapLocationIFace records to feed to the LocalityMapper
        List<MapLocationIFace> mapLocations = new Vector<MapLocationIFace>(selection.length);
        List<WorkbenchRow> rows = workbench.getWorkbenchRowsAsList();
        int localityTableId = databaseSchema.getIdByClassName(Locality.class.getName());
        int lat1Index = workbench.getColumnIndex(localityTableId, "latitude1");
        int lon1Index = workbench.getColumnIndex(localityTableId, "longitude1");
        int lat2Index = workbench.getColumnIndex(localityTableId, "latitude2");
        int lon2Index = workbench.getColumnIndex(localityTableId, "longitude2");
        for (int i = 0; i < selection.length; ++i )
        {
            int index = selection[i];
            
            WorkbenchRow row = rows.get(index);

            String lat1 = row.getData(lat1Index);
            String lon1 = row.getData(lon1Index);
            Double latitude = null;
            Double longitude = null;
            try
            {
                GeoRefConverter converter = new GeoRefConverter();
                lat1 = converter.convert(StringUtils.stripToNull(lat1), GeoRefFormat.D_PLUS_MINUS.name());
                latitude = new Double(lat1);
                lon1 = converter.convert(StringUtils.stripToNull(lon1), GeoRefFormat.D_PLUS_MINUS.name());
                longitude = new Double(lon1);
            }
            catch (Exception e)
            {
                //UsageTracker.incrHandledUsageCount();
                //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchPaneSS.class, e);
                // this could be a number format exception
                // or a null pointer exception if the field was empty
                // either way, we skip this record
                continue;
            }
            
            SimpleMapLocation newLoc = null;
            
            // try to use a bounding box
            if (lat2Index != -1 && lon2Index != -1)
            {
                String lat2 = row.getData((short)lat2Index);
                String lon2 = row.getData((short)lon2Index);
                Double latitude2 = null;
                Double longitude2 = null;
               
                try
                {
                    latitude2 = new Double(lat2);
                    longitude2 = new Double(lon2);
                }
                catch (Exception e)
                {
                    // this could be a number format exception
                    // or a null pointer exception if the field was empty
                    // either way, we'll just treat this record as though it only has lat1 and lon1
                }
                if ((latitude2 == null) ^ (longitude2 == null))
                {
                	latitude2 = null;
                	longitude2 = null;
                }
                newLoc = new SimpleMapLocation(latitude,longitude,latitude2,longitude2);
            }
            else // use just the point
            {
                // we only have lat1 and long2
                newLoc = new SimpleMapLocation(latitude,longitude,null,null);
            }
            
            // add the storage to the list
            mapLocations.add(newLoc);
        }
        
        LocalityMapper mapper = new LocalityMapper(mapLocations);
        mapper.setMaxMapHeight(500);
        mapper.setMaxMapWidth(500);
        mapper.setShowArrows(false);
        mapper.setDotColor(new Color(64, 220, 64));
        MapperListener mapperListener = new MapperListener()
        {
            @SuppressWarnings("synthetic-access")
            public void exceptionOccurred(Exception e)
            {
                String errorMsg = null;
                if (e instanceof ConnectException)
                {
                    errorMsg = getResourceString("WB_MAP_SERVICE_CONNECTION_FAILURE");
                }
                else
                {
                    // in the future, we may want a different message for non-connection exceptions
                    //errorMsg = getResourceString("WB_MAP_SERVICE_CONNECTION_FAILURE");
                	errorMsg = e.getLocalizedMessage();
                }
                JStatusBar statusBar = UIRegistry.getStatusBar();
                statusBar.setErrorMessage(errorMsg,e);
                statusBar.setProgressDone(WorkbenchTask.WORKBENCH);
                showMapBtn.setEnabled(true);
                log.error("Exception while grabbing map from service", e);
            }

            public void mapReceived(Icon map)
            {
                mapImageReceived(map);
            }
        };
        
        //FileCache imageCache = UIRegistry.getLongTermFileCache();
        //imageCache.clear();
        mapper.getMap(mapperListener);
        
        JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setIndeterminate(WorkbenchTask.WORKBENCH, true);
        statusBar.setText(getResourceString("WB_CREATINGMAP"));
    }
    
    /**
     * Notification that the Map was received.
     * @param map icon of the map that was generated
     */
    protected void mapImageReceived(final Icon map)
    {
        JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setProgressDone(WorkbenchTask.WORKBENCH);
        statusBar.setText("");

        if (map != null)
        {
            UIHelper.positionFrameRelativeToTopFrame(mapFrame);
            mapFrame.setVisible(true);
            mapImageLabel.setIcon(map);
            //showMapBtn.setEnabled(true);
            
            // is the map really skinny?
            int ht = map.getIconHeight();
            int wd = map.getIconWidth();
            if (ht < 20 && wd > 100 || ht > 100 && wd < 20)
            {
                statusBar.setWarningMessage("WB_THIN_MAP_WARNING");
            }
        }
    }
    
    /**
     * @param columnIdx
     * @param rowIndex
     * @return
     */
    protected String getLatLonSrc(int columnIdx, int rowIndex)
    {
        WorkbenchTemplateMappingItem map = workbench.getMappingFromColumn((short )columnIdx);
        String result = null;
        if (map.getTableName().equals("locality"))
        {
            if (map.getFieldName().equalsIgnoreCase("latitude1"))
            {
            	result = workbench.getRow(rowIndex).getLat1Text();
            }
            else if (map.getFieldName().equalsIgnoreCase("latitude2"))
            {
            	result = workbench.getRow(rowIndex).getLat2Text();
            }
            else if (map.getFieldName().equalsIgnoreCase("longitude1"))
            {
            	result = workbench.getRow(rowIndex).getLong1Text();
            }
            else if (map.getFieldName().equalsIgnoreCase("longitude2"))
            {
            	result = workbench.getRow(rowIndex).getLong2Text();
            }
            return result ;
        }
        
        return null;
    }

    protected List<CellPosition> convertColumnContents(int columnIndex, int[] rows, GeoRefConverter converter, String outputFormat)
    {
    	return convertColumnContents(columnIndex, rows, converter, outputFormat, LatLonConverter.LATLON.Latitude /*dummy*/,
    			LatLonConverter.DEGREES_FORMAT.None);
    }

    /**
     * Converts the column contents from on format of Lat/Lon to another
     * @param columnIndex the index of the column being converted
     * @param converter the converter to use
     * @param outputFormat the format string
     * 
     * return number of non-blank cells that were NOT converted
     */
    protected List<CellPosition> convertColumnContents(int columnIndex, int[] rows, GeoRefConverter converter, String outputFormat,
    		LatLonConverter.LATLON latOrLon, LatLonConverter.DEGREES_FORMAT degFmt)
    {
        List<CellPosition> unconverted = new Vector<>();
        
        if (columnIndex == -1)
        {
            return unconverted;           
        }
        
        final int[] selectedRows = spreadSheet.getSelectedRows();
        final int[] selectedCols = spreadSheet.getSelectedColumns();
        for (int index = 0; index < rows.length; ++index)
        {
            int rowIndex = rows[index];
            String currentValue = null; 
            //check backup col for original value before any conversions...
            currentValue = getLatLonSrc(columnIndex, rowIndex);
            if (currentValue != null)
            {
            	currentValue = currentValue.replace("  ", " ");
            }
            if (StringUtils.isBlank(currentValue))
            {
                currentValue = (String)model.getValueAt(rowIndex, columnIndex);
            }
            
            if (StringUtils.isBlank(currentValue))
            {
                continue;
            }

            String convertedValue;
            try
            {
                convertedValue = converter.convert(StringUtils.stripToNull(currentValue), outputFormat, latOrLon, degFmt);
                convertedValue = convertedValue.replaceAll("  ", " ").replaceAll("  ", " ");
            }
            catch (Exception e)
            {
                //UsageTracker.incrHandledUsageCount();
                //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchPaneSS.class, e);
                // this value didn't convert correctly
                // it would be nice to highlight that cell, but I don't know how we could do that
                log.warn("Could not convert contents of cell (" + (rowIndex+1) + "," + (columnIndex+1) + ")");
                unconverted.add(new CellPosition(rowIndex, columnIndex));
                continue;
            }

            model.setValueAt(convertedValue, rowIndex, columnIndex, true);
            if (!currentValue.equals(convertedValue))
            {
                setChanged(true);
            }
        }
        
        SwingUtilities.invokeLater(() -> {
            ListSelectionModel selModel = spreadSheet.getSelectionModel();
            for (int rowIndex: selectedRows)
            {
                selModel.addSelectionInterval(rowIndex, rowIndex);
            }
            ListSelectionModel colSelModel = spreadSheet.getColumnModel().getSelectionModel();
            for (int colIndex: selectedCols)
            {
                colSelModel.addSelectionInterval(colIndex, colIndex);
            }
        });
        return unconverted;
    }
    
    /**
     * Export to XLS.
     */
    protected void doExcelCsvExport()
    {
        int[] selection = spreadSheet.getSelectedRowModelIndexes();
        if (selection.length==0)
        {
            // if none are selected, map all of them
            int rowCnt = spreadSheet.getRowCount();
            selection = new int[rowCnt];
            for (int i = 0; i < rowCnt; ++i)
            {
                selection[i] = spreadSheet.convertRowIndexToModel(i);
            }
        }
        
        // put all the selected rows in a List
        List<WorkbenchRow> selectedRows = new Vector<WorkbenchRow>();
        List<WorkbenchRow> rows = workbench.getWorkbenchRowsAsList();
        for (int i = 0; i < selection.length; ++i )
        {
            int index = selection[i];
            WorkbenchRow row = rows.get(index);
            selectedRows.add(row);
        }
        
        CommandAction command = new CommandAction(PluginsTask.PLUGINS, PluginsTask.EXPORT_LIST);
        command.setData(selectedRows);
        command.setProperty("tool", ExportToFile.class);
        
        
        Properties props = new Properties();

        if (!WorkbenchTask.getExportInfo(props, workbench.getName()))
        {
            return;
        }

         ConfigureExternalDataIFace config = ExportFileConfigurationFactory.getConfiguration(props);

        // Could get config to interactively get props or to look them up from prefs or ???
        // for now hard coding stuff...
        
        // add headers. all the time for now.
        config.setFirstRowHasHeaders(true);
        Vector<WorkbenchTemplateMappingItem> colHeads = new Vector<WorkbenchTemplateMappingItem>();
        colHeads.addAll(workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems());
        Collections.sort(colHeads);
        String[] heads = new String[colHeads.size()];
        for (int h = 0; h < colHeads.size(); h++)
        {
            heads[h] = colHeads.get(h).getCaption();
        }
        config.setHeaders(heads);
        
        command.addProperties(config.getProperties());
        
        UsageTracker.incrUsageCount("WB.ExportXLSRowsTool");
        CommandDispatcher.dispatch(command);
    }
    
    /**
     * @return
     */
    protected Pair<WorkbenchTemplateMappingItem, List<WorkbenchTemplateMappingItem>> selectColumnName()
    {
        WorkbenchTemplateMappingItem genus      = null;
        WorkbenchTemplateMappingItem species    = null;
        WorkbenchTemplateMappingItem subspecies = null;
        WorkbenchTemplateMappingItem variety1   = null;
        

        Comparator<WorkbenchTemplateMappingItem> wbmtiComp = new Comparator<WorkbenchTemplateMappingItem>()
        {
            public int compare(WorkbenchTemplateMappingItem wbmti1, WorkbenchTemplateMappingItem wbmti2)
            {
                return wbmti1.getTitle().compareTo(wbmti2.getTitle());
            }
            
        };
        
        Vector<WorkbenchTemplateMappingItem> list = new Vector<WorkbenchTemplateMappingItem>();
        for (WorkbenchTemplateMappingItem item : workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems())
        {
            //item.setUseCaptionForText(true);
        	list.add(item);
            if (item.getFieldName().equals("genus1"))
            {
                genus = item;
                
            } else if (item.getFieldName().equals("species1"))
            {
                species = item;
                
            } else if (item.getFieldName().equals("subspecies1"))
            {
                subspecies = item;
                
            } else if (item.getFieldName().equals("variety1"))
            {
                variety1 = item;
            }
        }
        
        Collections.sort(list, wbmtiComp);
        
        if (genus != null && species != null)
        {
            WorkbenchTemplateMappingItem genusSpecies = new WorkbenchTemplateMappingItem();
            genusSpecies.setCaption(genus.getTitle() + " " + species.getTitle() +
                                    (subspecies != null ? (" " + subspecies.getTitle()) : "") +
                                    (variety1 != null ? (" " + variety1.getTitle()) : ""));
            genusSpecies.setViewOrder((short)-1);
            genusSpecies.setWorkbenchTemplateMappingItemId((int)genus.getViewOrder());
            genusSpecies.setVersion(species.getViewOrder());
            
            if (subspecies != null)
            {
                genusSpecies.setOrigImportColumnIndex(subspecies.getViewOrder());
            }
            if (variety1 != null)
            {
                genusSpecies.setSrcTableId((int)variety1.getViewOrder());
            }
            list.insertElementAt(genusSpecies, 0);
        }
        
        WorkbenchTemplateMappingItem rowItem = new WorkbenchTemplateMappingItem();
        rowItem.setCaption("Row Number"); // I18N
        rowItem.setViewOrder((short)-2);
        list.insertElementAt(rowItem, 0);

        final ToggleButtonChooserPanel<WorkbenchTemplateMappingItem> titlePanel = new ToggleButtonChooserPanel<WorkbenchTemplateMappingItem>(list, 
                "GE_CHOOSE_FIELD_FOR_TITLE_EXPORT", 
               ToggleButtonChooserPanel.Type.RadioButton);
        titlePanel.setUseScrollPane(true);
        titlePanel.createUI();
        
        Vector<WorkbenchTemplateMappingItem> includeList = new Vector<WorkbenchTemplateMappingItem>();
        for (WorkbenchTemplateMappingItem item : workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems())
        {
            includeList.add(item);
        }
        
        Collections.sort(includeList, wbmtiComp);
        
        final ToggleButtonChooserPanel<WorkbenchTemplateMappingItem> inclPanel = new ToggleButtonChooserPanel<WorkbenchTemplateMappingItem>(includeList, 
                "GE_CHOOSE_FIELDS_EXPORT", 
               ToggleButtonChooserPanel.Type.Checkbox);
        inclPanel.setUseScrollPane(true);
        inclPanel.setAddSelectAll(true);
        inclPanel.createUI();
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g,10px,f:p:g", "f:p:g,6px"));
        CellConstraints cc = new CellConstraints();
        pb.add(titlePanel, cc.xy(1,1));
        pb.add(inclPanel, cc.xy(3,1));

        CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), getResourceString("GE_CHOOSE_FIELD_FOR_EXPORT_TITLE"), true, pb.getPanel()){

			/* (non-Javadoc)
			 * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
			 */
			@Override
			protected void okButtonPressed()
			{
				if (titlePanel.getSelectedObject() != null && inclPanel.getSelectedObjects() != null
						&& inclPanel.getSelectedObjects().size() > 0)
				{
					super.okButtonPressed();
				}
				else
				{
					UIRegistry.showLocalizedError("WB_GOOGLE_SETTINGS_INCOMPLETE");
				}
			}
        	
        };
        dlg.setVisible(true);
//        for (WorkbenchTemplateMappingItem item : workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems())
//        {
//            item.setUseCaptionForText(false);
//        }
        
        if (!dlg.isCancelled())
        {
            return new Pair<WorkbenchTemplateMappingItem, List<WorkbenchTemplateMappingItem>>(
                    titlePanel.getSelectedObject(), 
                    inclPanel.getSelectedObjects());
        }
        return null;
    }
    
    /**
     * @param viewOrder
     * @return
     */
    protected WorkbenchTemplateMappingItem getWBMI(final int viewOrder)
    {
        for (WorkbenchTemplateMappingItem item : workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems())
        {
            if (item.getViewOrder().intValue() == viewOrder)
            {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Make a request to the ExportTask to display the selected records in GoogleEarth.
     */
    protected void showRecordsInGoogleEarth()
    {
        UsageTracker.incrUsageCount("WB.GoogleEarthRows");
        
        log.info("Showing map of selected records");
        int[] selection = spreadSheet.getSelectedRowModelIndexes();
        if (selection.length == 0)
        {
            // if none are selected, map all of them
            int rowCnt = spreadSheet.getRowCount();
            selection = new int[rowCnt];
            for (int i = 0; i < rowCnt; ++i)
            {
                selection[i] = spreadSheet.convertRowIndexToModel(i);
            }
        }
        
        Pair<WorkbenchTemplateMappingItem, List<WorkbenchTemplateMappingItem>> configPair = selectColumnName();
        if (configPair == null || 
            configPair.first == null || 
            configPair.second == null || 
            configPair.second.size() == 0)
        {
        	return;
        }
        
        WorkbenchTemplateMappingItem genus      = null;
        WorkbenchTemplateMappingItem species    = null;
        WorkbenchTemplateMappingItem subspecies = null;
        WorkbenchTemplateMappingItem variety    = null;
        
        WorkbenchTemplateMappingItem item = configPair.first;
        if (item.getViewOrder() == -1)
        {
            genus   = getWBMI(item.getWorkbenchTemplateMappingItemId());
            species = getWBMI(item.getVersion());
            if (item.getOrigImportColumnIndex() != null)
            {
                subspecies = getWBMI(item.getOrigImportColumnIndex());
            }
            if (item.getSrcTableId() != null)
            {
                variety = getWBMI(item.getSrcTableId());
            }
        }
        
        // put all the selected rows in a List
        List<LatLonPlacemarkIFace> selectedRows = new Vector<LatLonPlacemarkIFace>();
        List<WorkbenchRow> rows = workbench.getWorkbenchRowsAsList();
        for (int i = 0; i < selection.length; ++i )
        {
            int index = selection[i];
            WorkbenchRow row = rows.get(index);
            
            short  viewOrder = item.getViewOrder();
            String title     = null;
            if (viewOrder == -1 && genus != null && species != null)
            {
                title = row.getData(genus.getViewOrder()) + " " + 
                        row.getData(species.getViewOrder()) +
                        (subspecies != null ? (" " + row.getData(subspecies.getViewOrder())) : "") +
                        (variety != null ? (" " + row.getData(variety.getViewOrder())) : "");
            }
            
            if ((viewOrder == -1 && StringUtils.isEmpty(title)) || viewOrder == -2)
            {
                int visibleRowNumber = spreadSheet.convertRowIndexToView(index);
                title = "Row " + (visibleRowNumber+1);
                
            } else if (viewOrder > -1)
            {
                title = row.getData(item.getViewOrder());
            }
            selectedRows.add(new WorkbenchRowPlacemarkWrapper(row, title, configPair.second));
        }
        
        // get an icon URL that is specific to the current context
        CommandAction command = new CommandAction(PluginsTask.PLUGINS,PluginsTask.EXPORT_LIST);
        command.setData(selectedRows);
        command.setProperty("tool",           GoogleEarthExporter.class);
        command.setProperty("description",    workbench.getRemarks() != null ? workbench.getRemarks() : "");
        
        JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setText(UIRegistry.getResourceString("WB_OPENING_GOOGLE_EARTH"));
        CommandDispatcher.dispatch(command);
     }
    
    /**
     * Checks to see if the template can support BG.
     * @return return whether this template supports BG
     */
    protected boolean isTemplateBGCompatible()
    {
        // look for the locality fields
        int localityTableId = Locality.getClassTableId();
        if (workbench.getColumnIndex(localityTableId, "localityName") == -1 || // I18N
            workbench.getColumnIndex(localityTableId, "latitude1") == -1 ||
            workbench.getColumnIndex(localityTableId, "longitude1") == -1)
        {
            return false;
        }
        
        // look for the geography fields
        int geographyTableId = Geography.getClassTableId();
        if (workbench.getColumnIndex(geographyTableId, "Country") == -1 || // I18N
            workbench.getColumnIndex(geographyTableId, "State") == -1 ||
            workbench.getColumnIndex(geographyTableId, "County") == -1)
        {
            return false;
        }
        return true;
    }
    
    /**
     * @param tableId
     * @param fieldName
     * @return
     */
    private String getTitleForField(final int tableId, final String fieldName)
    {
        String title = DBTableIdMgr.getInstance().getTitleForField(tableId, fieldName);
        return StringUtils.isNotEmpty(title) ? title : fieldName;
    }
    
    /**
     * @param missingCols
     * @param tblId
     * @param fieldNames
     */
    private void checkForGeoFields(final List<String> missingCols, final int tblId, final String...fieldNames)
    {
        for (String fldName : fieldNames)
        {
            if (workbench.getColumnIndex(tblId, fldName) == -1)
            {
                missingCols.add(getTitleForField(tblId, fldName));
            }
        }
    }
    
    /**
     * @return
     */
    /**
     * @param checkAllFields true checks all fields, false checks just Lat, Lon
     * @return
     */
    protected String[] getMissingButRequiredColumnsForGeoRefTool(final boolean checkAllFields, final String tool)
    {
        List<String> missingCols = new Vector<String>();
        
        if (checkAllFields)
        {
            checkForGeoFields(missingCols, Locality.getClassTableId(),  "localityName", "latitude1", "longitude1");
            if ("geolocate".equalsIgnoreCase(tool))
            {
            	checkForGeoFields(missingCols, Geography.getClassTableId(), "country", "state");
            } else
            {
            	checkForGeoFields(missingCols, Geography.getClassTableId(), "country", "state", "county");
            }
        } else
        {
            checkForGeoFields(missingCols, Locality.getClassTableId(),  "latitude1", "longitude1");
        }
        
        // convert to a String[]  (toArray() converts to a Object[])
        String[] reqdFields = new String[missingCols.size()];
        for (int i = 0; i < missingCols.size(); ++i)
        {
            String s = missingCols.get(i);
            reqdFields[i] = s;
        }
        return reqdFields;
    }
    
    
    /**
     * @return
     */
    protected String[] getMissingButRequiredColumnsForGeoRefTool(final String tool)
    {
        return getMissingButRequiredColumnsForGeoRefTool(true, tool);
    }
    
    /**
     * @return
     */
    /**
     * @return
     */
    protected String[] getMissingGeoRefLatLonFields()
    {
        return getMissingButRequiredColumnsForGeoRefTool(false, "");
    }
    
    /**
     * @return the selected rows form the workbench
     */
    protected List<GeoCoordDataIFace> getSelectedRowsFromViewForGeoRef()
    {
        return new Vector<GeoCoordDataIFace>(getSelectedRows());
    }
    
    /**
     * @return
     */
    public List<WorkbenchRow> getSelectedRows()
    {
        // get the indexes into the model for all of the selected rows
        int[] selection = spreadSheet.getSelectedRowModelIndexes();
        if (selection.length == 0)
        {
            // if none are selected, map all of them
            int rowCnt = spreadSheet.getRowCount();
            selection = new int[rowCnt];
            for (int i = 0; i < rowCnt; ++i)
            {
                selection[i] = spreadSheet.convertRowIndexToModel(i);
            }
        }

        // gather all of the WorkbenchRows into a vector
        List<WorkbenchRow> rows         = workbench.getWorkbenchRowsAsList();
        List<WorkbenchRow> selectedRows = new Vector<WorkbenchRow>();
        for (int i: selection)
        {
            selectedRows.add(rows.get(i));
        }
        return selectedRows;
    }
    
    /**
     * @param geoRefService
     * @param trackId
     */
    protected void doGeoRef(final GeoCoordServiceProviderIFace geoRefService,
                            final String trackId) {
        UsageTracker.incrUsageCount(trackId);
        log.info("Performing GeoREflookup of selected records: "+ trackId);

        if (true) {
            List<GeoCoordDataIFace> selectedWBRows = getSelectedRowsFromViewForGeoRef();
            if (selectedWBRows != null) {
                geoRefService.processGeoRefData(selectedWBRows, new GeoCoordProviderListenerIFace() {
                    public void aboutToDisplayResults() {
                        if (imageFrame != null) {
                            imageFrame.setAlwaysOnTop(false);
                        }
                    }
                    
                    public void complete(final List<GeoCoordDataIFace> items, final int itemsUpdated) {
                        if (itemsUpdated > 0) {
                            setChanged(true);
                            int[] selection = spreadSheet.getSelectedRowModelIndexes();  
                            if (selection.length > 0) {
                            	validateRows(selection);
                            } else  {
                            	validateAll();
                            }
                            model.fireDataChanged();
                            spreadSheet.repaint();
                        }
                    }
                }, "WorkbenchSpecialTools"); // last argument is Help Context
            }
            return;
        }
    }

    /**
     *
     * @param changed
     * @param e
     */
    public void setChanged(final boolean changed, final TableModelEvent e) {
        if (!blockChanges) {
            hasChanged = changed;
            saveBtn.setEnabled(hasChanged);
            updateUploadBtnState();
            updateRevertBtnState(e);
        }
    	
    }
    /**
     * Set that there has been a change.
     * 
     * @param changed true or false
     */
    public void setChanged(final boolean changed)
    {
        setChanged(changed, null);
    }
    
    /**
     * @return whether there has been a change.
     */
    public boolean isChanged()
    {
        return hasChanged;
    }

    /**
     * @param wbtmi
     * @return
     */
    public static int getMaxColWidth(WorkbenchTemplateMappingItem wbtmi, boolean isForUpdate) {
    	DBFieldInfo fi = null;
    	int result = WorkbenchDataItem.getMaxWBCellLength();
    	if (wbtmi.getFieldInfo() != null) {
    		fi = wbtmi.getFieldInfo();
    	} else {
            DBTableIdMgr databaseSchema = WorkbenchTask.getDatabaseSchema(isForUpdate);
            DBTableInfo ti = databaseSchema.getInfoById(wbtmi.getSrcTableId());
            if (ti != null) {
            	fi = ti.getFieldByName(wbtmi.getFieldName());
                if (fi != null)  {  
                	wbtmi.setFieldInfo(fi);
                } else {
                    log.error("Can't find field with name ["+wbtmi.getFieldName()+"]");
                }
            } else {
                log.error("Can't find table ["+wbtmi.getSrcTableId()+"]");
            }
    	}
    	if (fi != null && RecordTypeCodeBuilder.getTypeCode(fi) == null && fi.getLength() > 0) {
    		result = Math.min(fi.getLength(), WorkbenchDataItem.getMaxWBCellLength());
    	}
    	return result;
    }
    
    /**
     * @param workbench
     * @return
     */
    public static Integer[] getMaxColWidths(Workbench workbench, boolean isForUpdate) {
        List<WorkbenchTemplateMappingItem> wbtmis = new ArrayList<WorkbenchTemplateMappingItem>();
        wbtmis.addAll(workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems());
        Collections.sort(wbtmis);        
        Integer[] result = new Integer[wbtmis.size()];
        for (int i = 0; i < wbtmis.size(); i++) {
        	result[i] = new Integer(WorkbenchPaneSS.getMaxColWidth(wbtmis.get(i), isForUpdate));
        }
        return result;
    }
    /**
     * Adjust all the column width for the data in the column, this may be handles with JDK 1.6 (6.)
     * @param tableArg the table that should have it's columns adjusted
     */
    private void initColumnSizes(final JTable tableArg, final JButton theSaveBtn) throws Exception {
        TableModel  tblModel    = tableArg.getModel();
        TableColumn column      = null;
        Component   comp        = null;
        int         headerWidth = 0;
        int         cellWidth   = 0;
        
        
        Element uploadDefs = null;
        if (WorkbenchTask.isCustomizedSchema()) {
        	uploadDefs = XMLHelper.readFileToDOM4J(new File(UIRegistry.getAppDataDir() + File.separator + "specify_workbench_upload_def.xml"));
        } else {
        	uploadDefs = XMLHelper.readDOMFromConfigDir("specify_workbench_upload_def.xml");
        }
        
        //UIRegistry.getInstance().hookUpUndoableEditListener(cellEditor);
        
        Vector<WorkbenchTemplateMappingItem> wbtmis = new Vector<WorkbenchTemplateMappingItem>();
        wbtmis.addAll(workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems());
        Collections.sort(wbtmis);
        
        DBTableIdMgr databaseSchema = WorkbenchTask.getDatabaseSchema(isUpdateDataSet());
        
        columnMaxWidths = new Integer[tableArg.getColumnCount()];
        boolean isUpdateable = isUpdateDataSet();
        for (int i = 0; i < wbtmis.size() /*tableArg.getColumnCount()*/; i++)  {
            TableCellRenderer headerRenderer = tableArg.getColumnModel().getColumn(i).getHeaderRenderer();
            WorkbenchTemplateMappingItem wbtmi = wbtmis.elementAt(i);
            
            // Now go retrieve the data length
            int fieldWidth = WorkbenchDataItem.getMaxWBCellLength();
            DBTableInfo ti = databaseSchema.getInfoById(wbtmi.getSrcTableId());
            if (ti != null) {
                DBFieldInfo fi = ti.getFieldByName(wbtmi.getFieldName());
                if (fi != null) {
                    wbtmi.setFieldInfo(fi);
                    //System.out.println(fi.getName()+"  "+fi.getLength()+"  "+fi.getType());
                    if (RecordTypeCodeBuilder.getTypeCode(fi) == null && fi.getLength() > 0) {
                        fieldWidth = Math.min(fi.getLength(), WorkbenchDataItem.getMaxWBCellLength());
                    }
                } else {
                    log.error("Can't find field with name ["+wbtmi.getFieldName()+"]");
                }
            } else {
                log.error("Can't find table ["+wbtmi.getSrcTableId()+"]");
            }
            columnMaxWidths[i] = new Integer(fieldWidth);
            GridCellEditor cellEditor = getCellEditor(wbtmi, fieldWidth, theSaveBtn, uploadDefs);
            cellEditor.setEditable(!isUpdateable || wbtmi.getIsEditable());
            column = tableArg.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                                 null, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = tableArg.getDefaultRenderer(tblModel.getColumnClass(i)).
                                               getTableCellRendererComponent(tableArg, tblModel.getValueAt(0, i), false, false, 0, i);
            
            cellWidth = comp.getPreferredSize().width;
            
            //comp.setBackground(Color.WHITE);
            
            int maxWidth = headerWidth + 10;
            TableModel m = tableArg.getModel();
            FontMetrics fm     = comp.getFontMetrics(comp.getFont());
            for (int row=0;row<tableArg.getModel().getRowCount();row++)
            {
                String text = m.getValueAt(row, i).toString();
                maxWidth = Math.max(maxWidth, fm.stringWidth(text)+10);
                //log.debug(i+" "+maxWidth);
            }

            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
            //log.debug(Math.max(maxWidth, cellWidth));
            //log.debug(Math.min(Math.max(maxWidth, cellWidth), 400));
            column.setPreferredWidth(Math.min(Math.max(maxWidth, cellWidth), 400));
            
            column.setCellEditor(cellEditor);
            WbCellRenderer renderer = new WbCellRenderer();
            renderer.setEditable(!isUpdateable || wbtmi.getIsEditable());
            column.setCellRenderer(renderer);

        }
        //tableArg.setCellEditor(cellEditor);
    }
    
    /**
     * @param tblName
     * @param fldName
     * @param upDefs
     * @return
     */
    protected String getActualTableName(String tblName, String fldName, Element upDefs) {
    	String result = tblName;
    	if (upDefs != null) {
            for (Iterator<?> i = upDefs.elementIterator("field"); i.hasNext();) {
                Element fld = (Element) i.next();
                String tbl = fld.attributeValue("table");
                String fName = fld.attributeValue("name");
                String actualTable = fld.attributeValue("actualtable");        
                if (tblName.equalsIgnoreCase(tbl) && fldName.equalsIgnoreCase(fName) && actualTable != null && !"".equals(actualTable)) {
                	result = actualTable.toLowerCase();
                	break;
                }
            	
            }

    	}
    	return result;
    }

//    private ValComboBoxFromQuery buildQcbxForCellEditor(final String name, final String dataObjFormatterName) {
//        return TypeSearchForQueryFactory.getInstance().createValComboBoxFromQuery(name, 0, dataObjFormatterName, null);
//    }


    /**
     * @param wbtmi
     * @return
     */
    protected GridCellEditor getCellEditor(WorkbenchTemplateMappingItem wbtmi, int fieldWidth, JButton theSaveBtn, Element uploadDefs) {
    	PickListDBAdapterIFace pickList = null;
    	DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoByTableName(getActualTableName(wbtmi.getTableName(), wbtmi.getFieldName(), uploadDefs));
    	if (tblInfo != null) {
    		String fldName = wbtmi.getFieldName();
    		@SuppressWarnings("unchecked")
    		List<Object> flds = uploadDefs.selectNodes("field");
    		for (Object fld : flds) {
    			String table = XMLHelper.getAttr((Element )fld, "table", null);
    			String field = XMLHelper.getAttr((Element )fld, "name", null);
    			if (wbtmi.getTableName().equalsIgnoreCase(table) && wbtmi.getFieldName().equalsIgnoreCase(field)) {
    				fldName = XMLHelper.getAttr((Element )fld, "actualname", fldName);
    				break;
    			}
    		}
//    		if (wbtmi.getFieldName().equalsIgnoreCase(tblInfo.getIdColumnName())) {
//    		    return new GridCellQueryComboEditor(buildQcbxForCellEditor(UploadTable.capitalize(tblInfo.getName()), tblInfo.getDataObjFormatter()),
//                        wbtmi.getCaption(), fieldWidth, theSaveBtn);
//            }
    		DBFieldInfo fldInfo = tblInfo.getFieldByName(fldName);
    		if (fldInfo != null) {
    			if (!StringUtils.isEmpty(fldInfo.getPickListName())) {
    				pickList = PickListDBAdapterFactory.getInstance().create(
    						fldInfo.getPickListName(), false);
    			} else if (RecordTypeCodeBuilder.isTypeCodeField(fldInfo)) {
    				pickList = RecordTypeCodeBuilder.getTypeCode(fldInfo);
                    ((TypeCode)pickList).addEmptyItem();
    			}     		
    		} 
    		if (tblInfo.getTableId() == Preparation.getClassTableId()) {
    			fldName = wbtmi.getFieldName();
    			if (fldName.startsWith("prepType") && StringUtils.isNumeric(fldName.replace("prepType", ""))) {
    				pickList = PickListDBAdapterFactory.getInstance().create("prepType", false);
    			}
    					
    		}
    	} 
     	if (pickList == null) {
    		return new GridCellEditor(new JTextField(), wbtmi.getCaption(), fieldWidth, theSaveBtn);	
    	}
    	JComboBox<PickListItemIFace> comboBox = new JComboBox<PickListItemIFace>(pickList.getList());
    	comboBox.setEditable(true);
    	return new GridCellListEditor(comboBox, wbtmi.getCaption(), fieldWidth, theSaveBtn); 
    }
    /**
     * Carry forward configuration.
     */
    public void configCarryFoward()
    {
        Vector<WorkbenchTemplateMappingItem> items           = new Vector<WorkbenchTemplateMappingItem>();
        Vector<WorkbenchTemplateMappingItem> selectedObjects = new Vector<WorkbenchTemplateMappingItem>();
        items.addAll(workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems());
        
        for (WorkbenchTemplateMappingItem item : items)
        {
            if (item.getCarryForward())
            {
                selectedObjects.add(item);
            }
        }
        
        // turn off alwaysOnTop for Swing repaint reasons (prevents a lock up)
        imageFrame.setAlwaysOnTop(false);
        
        Collections.sort(items);
        ToggleButtonChooserDlg<WorkbenchTemplateMappingItem> dlg = new ToggleButtonChooserDlg<WorkbenchTemplateMappingItem>((Frame)UIRegistry.get(UIRegistry.FRAME),
                                                                        "WB_CARRYFORWARD",
                                                                        "WB_CHOOSE_CARRYFORWARD", 
                                                                        items,
                                                                        CustomDialog.OKCANCELHELP,
                                                                        ToggleButtonChooserPanel.Type.Checkbox);
        
        dlg.setHelpContext(currentPanelType == PanelType.Spreadsheet ? "WorkbenchGridEditingCF" : "WorkbenchFormEditingCF");
        dlg.setAddSelectAll(true);
        dlg.setSelectedObjects(selectedObjects);
        dlg.setModal(true);
        dlg.setAlwaysOnTop(true);
        dlg.setUseScrollPane(true);
        dlg.setVisible(true);  
        
        if (!dlg.isCancelled())
        {
            for (WorkbenchTemplateMappingItem item : items)
            {
                item.setCarryForward(false);
            }
            for (WorkbenchTemplateMappingItem item : dlg.getSelectedObjects())
            {
                item.setCarryForward(true);
            }
            setChanged(true);
        }
    }
    
    /**
     * loads workbench from the database and backs it up (exports to an xls file).
     */
    protected void backupObject()
    {
        WorkbenchBackupMgr.backupWorkbench(workbench);
    }
    
    protected void logDebug(Object toLog)
    {
        if (debugging)
        {
            log.debug(toLog);
        }
    }
    /**
     * Save the Data. 
     */
    public void saveObject()
    {
        if (workbench == null)
        {
            UIRegistry.showError("The workbench is 'null' before save.\nPlease contact Specify support.");
            return;
        }
        //backup current database contents for workbench
        if (!isUpdateDataSet()) {
            logDebug("backupObject(): " + System.nanoTime());
            backupObject();
            logDebug("---------" + System.nanoTime());
        }
        logDebug("checkCurrentEditState: " + System.nanoTime());
        checkCurrentEditState();
        logDebug("---------" + System.nanoTime());
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        
        boolean committed = false; 
        boolean opened = false;
        try
        {
            FormHelper.updateLastEdittedInfo(workbench);
                                    
            logDebug("merging and saving: " + System.nanoTime());
            session.beginTransaction();
            opened = true;
           
            session.saveOrUpdate(workbench);
            session.commit();
            committed = true;
            session.flush();

            model.setWorkbench(workbench);
            if (formPane != null) {
                formPane.setWorkbench(workbench);
            }
            if (imageFrame != null)
            {
                imageFrame.setWorkbench(workbench);
            }

            log.info("Session Saved[ and Flushed " + session.hashCode() + "]");
            logDebug("-------- " + System.nanoTime());

            hasChanged = false;
            String msg = String.format(getResourceString("WB_SAVED"), new Object[] { workbench.getName() });
            UIRegistry.getStatusBar().setText(msg);
        }
        catch (StaleObjectException ex) // was StaleObjectStateException
        {
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchPaneSS.class, ex);
            if (opened && !committed)
            {
                session.rollback();
            }
            //recoverFromStaleObject("UPDATE_DATA_STALE");
            UnhandledExceptionDialog dlg = new UnhandledExceptionDialog(ex);
            dlg.setVisible(true);
            
        }
        catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchPaneSS.class, ex);
            log.error("******* " + ex);
            ex.printStackTrace();
            if (opened && !committed)
            {
                session.rollback();
            }
            UnhandledExceptionDialog dlg = new UnhandledExceptionDialog(ex);
            dlg.setVisible(true);
        }
        finally
        {
            session.close();
            session = null;
        }
        if (saveBtn != null) {
            saveBtn.setEnabled(false);
        }
        if (datasetUploader != null)
        {
            datasetUploader.refresh();
        }
        updateUploadBtnState();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#aboutToShutdown()
     */
    @Override
    public boolean aboutToShutdown()
    {
        super.aboutToShutdown();
        
        for (WorkBenchPluginIFace wbp : workBenchPlugins.values())
        {
            wbp.shutdown();
        }
        
        // Tell it is about to be hidden.
        // this way it can end any editing
        if (formPane != null)
        {
            if (!checkCurrentEditState())
            {
                SubPaneMgr.getInstance().showPane(this);
                // Need to reverify to get the error to display again.
                if (spreadSheet.getCellEditor() != null)
                {
                    spreadSheet.getCellEditor().stopCellEditing();
                }
                return false;
            }
        }
        
        if (datasetUploader != null && !datasetUploader.aboutToShutdown(this, null, false))
        {
            return false;
        }

        if (datasetUploader != null)
        {
            if (datasetUploader.closing(this))
            {
                datasetUploader = null;
                Uploader.unlockApp();
                if (!isUpdateDataSet()) {
                    Uploader.unlockUpload();
                }
            }
        }

        boolean retStatus = true;
        if (hasChanged)
        {

            // turn off alwaysOnTop for Swing repaint reasons (prevents a lock up)
            if (imageFrame != null)
            {
                imageFrame.setAlwaysOnTop(false);
            }
            
            int rv = JOptionPane.CANCEL_OPTION;
            final String wbName = workbench.getName();
                        
            if (this.isUpdateDataSet()) {
	            String msg = String.format(getResourceString("WB_DISCARD_BATCH_EDITS"), getTitle());
	            JFrame topFrame = (JFrame)UIRegistry.getTopWindow();
	
	            rv = JOptionPane.showConfirmDialog(topFrame,
	                                                   msg,
	                                                   getResourceString("WB_DISCARD_BATCH_EDITS_TITLE"),
	                                                   JOptionPane.OK_CANCEL_OPTION);
	            if (rv == JOptionPane.OK_OPTION) {
	            	rv = JOptionPane.NO_OPTION;
	            }
            } else {
	            String msg = String.format(getResourceString("SaveChanges"), getTitle());
	            JFrame topFrame = (JFrame)UIRegistry.getTopWindow();
	
	            rv = JOptionPane.showConfirmDialog(topFrame,
	                                                   msg,
	                                                   getResourceString("SaveChangesTitle"),
	                                                   JOptionPane.YES_NO_CANCEL_OPTION);
            }
            
            if (rv == JOptionPane.YES_OPTION) {
                //GlassPane and Progress bar currently don't show up during shutdown
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        UIRegistry.writeGlassPaneMsg(String.format(
                                getResourceString("WB_SAVING"),
                                new Object[] { workbench.getName() }),
                                WorkbenchTask.GLASSPANE_FONT_SIZE);
                        UIRegistry.getStatusBar().setIndeterminate(wbName, true);
                    }
                });
                
                SwingWorker saver = new SwingWorker()
                {

                    /* (non-Javadoc)
                     * @see edu.ku.brc.helpers.SwingWorker#construct()
                     */
                    @Override
                    public Object construct()
                    {
                        
                        Boolean result = null;
                        try
                        {
                            saveObject();
                            result = new Boolean(true);
                        }
                        catch (Exception ex)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchPaneSS.class, ex);
                            log.error(ex);
                        }
                        return result;
                    }

                    /* (non-Javadoc)
                     * @see edu.ku.brc.helpers.SwingWorker#finished()
                     */
                    @Override
                    public void finished()
                    {
                        UIRegistry.clearGlassPaneMsg();
                        UIRegistry.getStatusBar().setProgressDone(wbName);
                        shutdownLock.decrementAndGet();
                        shutdown();
                    }
                    
                };
                shutdownLock.incrementAndGet();
                saver.start();
                //retStatus = saver.get() != null;
            }
            else if (rv == JOptionPane.CANCEL_OPTION || rv == JOptionPane.CLOSED_OPTION)
            {
                return false;
            }
            else if (rv == JOptionPane.NO_OPTION)
            {
                hasChanged = false; // we do this so we don't get asked a second time
            }
            
        }
              
        if (retStatus)
        {
            ((WorkbenchTask) ContextMgr.getTaskByClass(WorkbenchTask.class)).closing(this);
            if (ContextMgr.getTaskByClass(SGRTask.class) != null) {
            	((SGRTask) ContextMgr.getTaskByClass(SGRTask.class)).closing(this);
            }
            
            if (spreadSheet != null)
            {
                spreadSheet.getSelectionModel().removeListSelectionListener(workbenchRowChangeListener);
            }
            workbenchRowChangeListener = null;
        }
        
        return retStatus;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#shutdown()
     */
    @Override
    public void shutdown()
    {
        //Check to see if background tasks accessing the Workbench are active.
        if (shutdownLock.get() > 0)
        {
            return;
        }
        
        if (spreadSheet == null)
        {
            return;
        }
                
        //--------------------------------------------------------------------------------
        // I really don't know how much of all this is necessary
        // but using the JProfiler it seems things got better when I did certain things.
        //--------------------------------------------------------------------------------
        
        
        UIRegistry.getLaunchFindReplaceAction().setSearchReplacePanel(null);
        UIRegistry.enableFind(null, false);
        
        JFrame topFrame = (JFrame)UIRegistry.getTopWindow();
        
        if (minMaxWindowListener != null)
        {
            topFrame.removeWindowListener(minMaxWindowListener);
            minMaxWindowListener = null;
        }
        
        shutdownValidators();
        
        removeAll();
        if (mainPanel != null)
        {
            mainPanel.removeAll();
        }
        if (controllerPane != null)
        {
            controllerPane.removeAll();
        }
        
        if (spreadSheet != null)
        {
            spreadSheet.setVisible(false);
            spreadSheet.getSelectionModel().removeListSelectionListener(workbenchRowChangeListener);
            spreadSheet.cleanUp();
            workbenchRowChangeListener = null;
        
            for (int i = 0; i < spreadSheet.getColumnCount(); i++) 
            {
                TableColumn column = spreadSheet.getColumnModel().getColumn(i);
                TableCellEditor editor = column.getCellEditor();
                if (editor instanceof GridCellEditor)
                {
                    ((GridCellEditor)editor).cleanUp();
                }
            }
            TableCellEditor editor = spreadSheet.getCellEditor();
            if (editor instanceof GridCellEditor)
            {
                ((GridCellEditor)editor).cleanUp();
            }
        }
        
        if (imageFrame != null)
        {
            imageFrame.cleanUp();
            imageFrame.dispose();
        }
        
        if (mapFrame != null)
        {
            mapFrame.dispose();
        }
        
        if (model != null)
        {
            model.cleanUp();
        }
        
        if (headers != null)
        {
            headers.clear();
        }
        
        if (resultsetController != null) {
            if (formPane != null) {
                resultsetController.removeListener(formPane);
            }
            resultsetController = null;
        }
        
        if (formPane != null) {
            formPane.cleanup();
        }

        formPane    = null;
        findPanel   = null;
        spreadSheet = null;
        workbench   = null;
        model       = null;
        imageColExt = null;
        columns     = null;
        imageFrame  = null;
        headers     = null;
        recordSet   = null;
        mainPanel   = null;
        controllerPane   = null;
        currentPanelType = null;
        cardLayout       = null;
        cpCardLayout     = null;
        uploadToolPanel  = null;
        workBenchPlugins = null;
        
        super.shutdown();
    }

    
    /**
     * 
     */
    protected void shutdownValidators()
    {
        //validationExecutor.shutdownNow();
        if (validationWorkerQueue.peek() != null) {
        	//System.out.println("Shutdown: Cancelling validation worker.");
        	ValidationWorker vw = null;
        	synchronized(validationWorkerQueue) {
        		vw = validationWorkerQueue.peek();
        		validationWorkerQueue.clear();
        	}
        	if (vw != null && !vw.isDone()) {
        		vw.cancel(true);
        	}
        }

    }
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#showingPane(boolean)
     */
    @Override
    public void showingPane(boolean show)
    {
    	//System.out.println("WBPane.showingPane(" + show + ") " + isVisible());
        if (formPane != null) {
            formPane.showingPane(show);
        }
        
        if (show)
        {
            UIRegistry.getLaunchFindReplaceAction().setSearchReplacePanel(findPanel);
            
            if (imageFrameWasShowing)
            {
                toggleImageFrameVisible();
                
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        final Frame f = (Frame)UIRegistry.get(UIRegistry.FRAME);
                        f.toFront();
                        f.requestFocus();
                    }
                });
            }
        }
        else if (isVisible() == show /*fix for #9182*/)
        {
            checkCurrentEditState();
            
            if (imageFrame != null && imageFrame.isVisible())
            {
                imageFrameWasShowing = true;
                toggleImageFrameVisible();
            }
            else
            {
                imageFrameWasShowing = false;
            }
            
            if (mapFrame != null && mapFrame.isVisible())
            {
                mapFrame.setVisible(false);
                
            }
        }
        super.showingPane(show);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#getRecordSet()
     */
    @Override
    public RecordSetIFace getRecordSet()
    {
        if (recordSet == null)
        {
            recordSet = new RecordSet();
            recordSet.initialize();
            recordSet.set(workbench.getName(), Workbench.getClassTableId(), RecordSet.GLOBAL);
            recordSet.addItem(workbench.getWorkbenchId());
        }
        return recordSet;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#getHelpTarget()
     */
    @Override
    public String getHelpTarget()
    {
        return currentPanelType == PanelType.Spreadsheet ? "WorkbenchGridEditing" : "WorkbenchFormEditing";
    }

    protected List<SubPaneIFace> checkOpenTasksForUpload() {
        return checkOpenTasksForUpload(false);
    }
    /**
     * @return  a list of open panes that prohibit uploading.
     */
    protected List<SubPaneIFace> checkOpenTasksForUpload(boolean isBatchEdit) {
        List<SubPaneIFace> result = new LinkedList<SubPaneIFace>();
        for (SubPaneIFace pane : SubPaneMgr.getInstance().getSubPanes()) {
            if (prohibitsUpload(pane, isBatchEdit)) {
                result.add(pane);
            }
        }
        return result;
    }

    /**
     * @return  a list of open panes that need to be closed when uploader is closed.
     */
    protected List<SubPaneIFace> checkOpenTasksForUploadClose()
    {
        List<SubPaneIFace> result = new LinkedList<SubPaneIFace>();
        for (SubPaneIFace pane : SubPaneMgr.getInstance().getSubPanes())
        {
            if (pane instanceof ESResultsSubPane)
            {
                result.add(pane);
            }
        }
        return result;
    }

    /**
     * @param pane
     * @return true if uploads are prohibited while pane is open.
     */
    protected boolean prohibitsUpload(final SubPaneIFace pane, boolean isBatchEdit)
    {
        if (pane.getTask().getClass().equals(DataEntryTask.class)) {
            return true;
        }
        if (pane.getTask().getClass().equals(InteractionsTask.class)) {
            return true;
        }
        if (pane instanceof ESResultsSubPane) {
        	return true;
        }
        if (pane.getTask() instanceof BaseTreeTask) {
            return true;
        }
        if (isBatchEdit && pane instanceof WorkbenchPaneSS && pane != this) {
            return ((WorkbenchPaneSS)pane).uploadToolPanel != null && ((WorkbenchPaneSS)pane).uploadToolPanel.isVisible();
        }
        return false;
    }
    
    /**
     * @param badPanes
     * @return a list of the distinct tasks represented by badPanes 
     */
    protected String getListOfBadTasks(final List<SubPaneIFace> badPanes) {
        Set<Taskable> badTasks = new HashSet<Taskable>();
        for (SubPaneIFace pane : badPanes) {
            badTasks.add(pane.getTask());
        }
        String result = "";
        Iterator<Taskable> badI= badTasks.iterator();
        while (badI.hasNext()) {
            Taskable badTask = badI.next();
            if (!StringUtils.isBlank(result)) {
                if (badI.hasNext()) {
                    result += ", ";
                }
                else {
                    result += " or ";
                }
            }
            if (badTask instanceof ExpressSearchTask) {
                result += UIRegistry.getResourceString("WorkbenchPaneSS.SearchResult");
            } else if (badTask instanceof WorkbenchTask) {
                result += UIRegistry.getResourceString("WorkbenchPaneSS.UploaderPane");
            } else {
            	result += badTask.getTitle();
            }
        }
        return result;
    }
    
    /**
     * builds validator
     */
    protected void buildValidator()
    {
    	buildValidator(false);
    }
    
    /**
     * builds validator
     */
    protected void buildValidator(boolean quietly)
    {
    	try
    	{
    		workbenchValidator = new WorkbenchValidator(this);
        	setMatchStatusForUploadTables();
        	//set up catnum checker
//        	UploadTable cout = workbenchValidator.getUploader().getUploadTableByName("collectionobject");
//        	if (cout != null)
//        	{
//        		for (Vector<UploadField> ufs : cout.getUploadFields())
//        		{
//        			for (UploadField uf : ufs)
//        			{
//        				DBFieldInfo fi = uf.getField() != null ? uf.getField().getFieldInfo() : null;
//        				if (fi != null && fi.getColumn().equalsIgnoreCase("CatalogNumber"))
//        				{
//        					catNumCol = uf.getIndex();
//        					break;
//        				}
//        			}
//        		}
//        		if (catNumCol != -1)
//        		{
//        			catNumChecker = new UniquenessChecker();
//        		}
//        	}
    	} catch (Exception ex)
    	{
    		if (ex instanceof WorkbenchValidator.WorkbenchValidatorException || ex instanceof UploaderException)
    		{
    			WorkbenchValidator.WorkbenchValidatorException wvEx = null;
    			if (ex instanceof WorkbenchValidator.WorkbenchValidatorException)
    			{
    				wvEx = (WorkbenchValidator.WorkbenchValidatorException )ex;
    			} else if (ex.getCause() instanceof WorkbenchValidator.WorkbenchValidatorException)
    			{
    				wvEx = (WorkbenchValidator.WorkbenchValidatorException )ex.getCause();
    			}
    			if (!quietly && wvEx != null && wvEx.getStructureErrors().size() > 0)
    			{
    				Uploader.showStructureErrors(wvEx.getStructureErrors());
    			}
    		}
    		else {
    			ex.printStackTrace();
    		}
    		if (!quietly)
    		{
    			UIRegistry.showLocalizedError("WorkbenchPaneSS.UnableToAutoValidate");
    		}
			workbenchValidator = null;
            if (uploadToolPanel != null && (isDoIncrementalValidation() || isDoIncrementalMatching())) {
                uploadToolPanel.turnOffSelections();
                if (isDoIncrementalValidation()) {
                    turnOffIncrementalValidation();
                }
                if (isDoIncrementalMatching()) {
                    turnOffIncrementalMatching();
                }
                model.fireDataChanged();
            }
    	}
    }
    
    /**
     * 
     */
    public void turnOffIncrementalValidation()
    {
		boolean savedBlockChanges = blockChanges;
		try
		{
			blockChanges = true;
			shutdownValidators();
			doIncrementalValidation = false;
			updateBtnUI();
			//workbenchValidator = null;
			model.fireDataChanged();
			AppPreferences.getLocalPrefs().putBoolean(wbAutoValidatePrefName+ "." + workbench.getId(), doIncrementalValidation);
		} finally
		{
			blockChanges = savedBlockChanges;
		}
    }
 
    
    /**
     * 
     */
    public void turnOffIncrementalMatching()
    {
		boolean savedBlockChanges = blockChanges;
		try
		{
			blockChanges = true;
			shutdownValidators();
			doIncrementalMatching = false;
			//workbenchValidator = null;
			model.fireDataChanged();
			AppPreferences.getLocalPrefs().putBoolean(wbAutoMatchPrefName + "." + workbench.getId(), doIncrementalMatching);
		} finally
		{
			blockChanges = savedBlockChanges;
		}
    }
    
    public void doDatasetUpload()
    {        
        if (datasetUploader != null)
        {
            //the button shouldn't be enabled in this case, but just to be sure:
            log.error("The upload button was enabled but the datasetUploader was not null.");
            return;
        }
        
        List<SubPaneIFace> badPanes = checkOpenTasksForUpload();
        if (badPanes.size() > 0)
        {
        	UIRegistry.displayInfoMsgDlgLocalized(String.format(getResourceString("WB_UPLOAD_CLOSE_ALL_MSG"), getListOfBadTasks(badPanes)));
        	return;
        }
        
        List<String> logins = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getAgentListLoggedIn(AppContextMgr.getInstance().getClassObject(Discipline.class));
        if (logins.size() > 0)
        {
            String loginStr = "";
            for (int l = 0; l < logins.size(); l++)
            {
                if (l > 0)
                {
                    loginStr += ", ";
                }
                loginStr += "'" + logins.get(l) + "'";
            }
            PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, f:p:g, 5dlu", "5dlu, f:p:g, 2dlu, f:p:g, 2dlu, f:p:g, 5dlu"));
            pb.add(new JLabel(UIRegistry.getResourceString("WB_UPLOAD_OTHER_USERS")), new CellConstraints().xy(2, 2));
            pb.add(new JLabel(loginStr), new CellConstraints().xy(2, 4));
            pb.add(new JLabel(UIRegistry.getResourceString("WB_UPLOAD_OTHER_USERS2")), new CellConstraints().xy(2, 6));
            
            CustomDialog dlg = CustomDialog.create(
                    UIRegistry.getResourceString("WB_UPLOAD_DENIED_DLG"),
                    true,
                    CustomDialog.OKCANCELAPPLYHELP,
                    pb.getPanel());
            dlg.setApplyLabel(UIRegistry.getResourceString("WB_UPLOAD_OVERRIDE"));
            dlg.setCloseOnApplyClk(true);
            dlg.createUI();
            
            //Stoopid x-box...
            dlg.getOkBtn().setVisible(false); 
            dlg.setCancelLabel(dlg.getOkBtn().getText());
            //...Stoopid x-box
            
            UIHelper.centerAndShow(dlg);
            dlg.dispose();
            if (dlg.isCancelled())
            {
                return;
            }
            PanelBuilder pb2 = new PanelBuilder(new FormLayout("5dlu, f:p:g, 5dlu", "5dlu, f:p:g, 5dlu"));
            pb2.add(new JLabel(UIRegistry.getResourceString("WB_UPLOAD_CONFIRM_ANNIHILATION")), new CellConstraints().xy(2, 2));
            CustomDialog dlg2 = CustomDialog.create(
                    UIRegistry.getResourceString("WB_UPLOAD_DANGER"),
                    true,
                    CustomDialog.OKCANCELHELP,
                    pb2.getPanel());
            UIHelper.centerAndShow(dlg2);
            dlg2.dispose();
            if (dlg2.isCancelled())
            {
                return;
            }
        }
        
        WorkbenchUploadMapper importMapper = new WorkbenchUploadMapper(workbench
                .getWorkbenchTemplate());
        try
        {
            Vector<UploadMappingDef> maps = importMapper.getImporterMapping();
            DB db = new DB();
            if (!isUpdateDataSet() && Uploader.lockUpload(null, true) != Uploader.LOCKED) {
                return;
            }
            Uploader.lockApp();
            spreadSheet.clearSorter();
            datasetUploader = new Uploader(db, new UploadData(maps, workbench.getWorkbenchRowsAsList()), this, false);
            Vector<UploadMessage> structureErrors = datasetUploader.verifyUploadability();
            if (structureErrors.size() > 0) 
            { 
                Uploader.showStructureErrors(structureErrors);
                uploadDone();
                return;
            }
            if (!datasetUploader.setAdditionalLocks())
            {
            	uploadDone();
            	return;
            }
            
            UploadField[] configs = uploadToolPanel.getConfiguredFields();
            datasetUploader.copyFldConfigs(configs);
            
            uploadPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spreadSheetPane, datasetUploader.getMainPanel());
            int uploadPaneDivider = 10;//spreadSheetPane.getHeight() * (isUpdateDataSet() ? 19/20 : 2/3);
			mainPanel.remove(spreadSheetPane);
            spreadSheetPane.setVisible(true);
			//uploadPane.setOneTouchExpandable(true);

			// Provide minimum sizes for the two components in the split pane
			Dimension minimumSize = isUpdateDataSet() ? new Dimension(0, 0) : new Dimension(200, 200);
			datasetUploader.getMainPanel().setMinimumSize(minimumSize);
			spreadSheetPane.setMinimumSize(minimumSize);
			mainPanel.add(uploadPane, PanelType.Spreadsheet.toString());
			showPanel(PanelType.Spreadsheet);
			mainPanel.validate();
			mainPanel.doLayout();
			ssFormSwitcher.setEnabled(false);
			// next line causes some weird behavior: when an entire row is
			// selected
			// (highlighted), cells in the row will go into edit mode - sort of
			// ?????
			spreadSheet.setEnabled(false);
			setToolBarBtnsEnabled(false);
			boolean hidToolPanel = uploadToolPanel.isExpanded();
            if (hidToolPanel)
            {
            	if (isUpdateDataSet()) {
            	    hideUploadToolPanelSansAnimation();
                } else {
                    hideUploadToolPanel();
                }
            	showHideUploadToolBtn.setToolTipText(getResourceString("WB_SHOW_UPLOADTOOLPANEL"));
            	restoreUploadToolPanel = true;
            }			
            if (imageFrame != null && imageFrame.isVisible())
			{
				imageFrame.setVisible(false);
			}
            if (isUpdateDataSet()) {
                if (hidToolPanel) {
                    Thread.sleep(100); //wait for hide to finish
                }
                uploadPane.setDividerLocation(1.0);
            } else {
                uploadPane.setDividerLocation(.67);
            }
			datasetUploader.startUI();
			if (doIncrementalValidation && invalidCellCount.get() == 0)
			{
				datasetUploader.validateData(true);
            }
		}
        catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchPaneSS.class, ex);
            UIRegistry.getStatusBar().setErrorMessage(ex.getMessage());
            Uploader.unlockApp();
            Uploader.unlockUpload();
            uploadDone();
        }
        finally
        {
            setAllUploadDatasetBtnEnabled(canUpload());
        }
    }

    public void uploadDone() {
        uploadDone(null);
    }
    /**
     * Removes uploader ui and redisplays standard wb ui
     */
    public void uploadDone(final String action) {
        datasetUploader = null;
        Uploader.unlockApp();
        if (!isUpdateDataSet() && !Uploader.unlockUpload()) {
            log.error("unable to unlock upload task semaphore.");
            //inform the user??
        }
        if (uploadPane != null) {
            List<SubPaneIFace> badPanes = checkOpenTasksForUploadClose();
            if (badPanes.size() > 0) {
                for (SubPaneIFace badPane : badPanes) {
                    if (!SubPaneMgr.getInstance().removePane(badPane, true)) {
                        log.error("unable to close " + badPane.getClass().getName() + " after uploader close.");
                    }
                }
            }
        	
        	mainPanel.remove(uploadPane);
            mainPanel.add(spreadSheetPane, PanelType.Spreadsheet.toString());
            showPanel(PanelType.Spreadsheet);
            mainPanel.validate();
            mainPanel.doLayout();
        }
        ssFormSwitcher.setEnabled(true && !isUpdateDataSet());
        spreadSheet.setEnabled(true);
        setToolBarBtnsEnabled(true);
        if (restoreUploadToolPanel) {
        	showUploadToolPanel();
        	showHideUploadToolBtn.setToolTipText(getResourceString("WB_HIDE_UPLOADTOOLPANEL"));
        	restoreUploadToolPanel = false;
        }
        setAllUploadDatasetBtnEnabled(true);

        if (UploadMainPanel.CANCEL_AND_CLOSE_BATCH_UPDATE.equals(action) || (isUpdateDataSet() && UploadMainPanel.CLOSE_UI.equals(action))) {
            saveBtn.setEnabled(true); //this should be safe
            revertBtn.setEnabled(true); //so should this
            for (WorkbenchRow r : workbench.getWorkbenchRows()) {
                r.setUploadStatus(WorkbenchRow.UPLD_NONE);
            }
            //And other stuff to restore state existing prior to opening the upload pane...
            //???
        }
    }
        
    /**
     * @param enabled
     * 
     * Enables buttons on the toolbar.
     * If enabled is false open forms associated with buttons are closed.
     */
    protected void setToolBarBtnsEnabled(boolean enabled)
    {
    	if (deleteRowsBtn != null)
    	{
    		deleteRowsBtn.setEnabled(enabled);
    	}
    	if (clearCellsBtn != null)
    	{
    		clearCellsBtn.setEnabled(enabled);
    	}
    	if (addRowsBtn != null)
    	{
    		addRowsBtn.setEnabled(enabled);
    	}
    	if (carryForwardBtn != null)
    	{
    		carryForwardBtn.setEnabled(enabled);
    	}
    	if (toggleImageFrameBtn != null)
    	{
    		toggleImageFrameBtn.setEnabled(enabled);
    	}
    	boolean missingGeoRefFlds = getMissingGeoRefLatLonFields().length > 0;
    	if (showMapBtn != null)
    	{
    		showMapBtn.setEnabled(enabled && !missingGeoRefFlds);
    	}
    	if (controlPropsBtn != null)
    	{
    		controlPropsBtn.setEnabled(enabled);
    	}
    	if (exportKmlBtn != null)
    	{
    		exportKmlBtn.setEnabled(enabled && !missingGeoRefFlds);
    	}
    	if (geoRefToolBtn != null)
    	{
    		geoRefToolBtn.setEnabled(enabled);
    	}
    	if (convertGeoRefFormatBtn != null)
    	{
    		if (!enabled)
    		{
    			if (this.geoRefConvertDlg != null)
    			{
    				geoRefConvertDlg.setVisible(false);
    			}
    		}
    		convertGeoRefFormatBtn.setEnabled(enabled && !missingGeoRefFlds);
    		
    	}
    	if (exportExcelCsvBtn != null)
    	{
    		exportExcelCsvBtn.setEnabled(enabled);
    	}
    	
        if (showHideUploadToolBtn != null)
        {
        	showHideUploadToolBtn.setEnabled(enabled);
        }

    	for (JComponent btn : workBenchPluginSSBtns)
    	{
    	    btn.setEnabled(enabled);
    	}
    }
    
    /**
     * @param enabled
     */
    protected void setAllUploadDatasetBtnEnabled(boolean enabled)
    {
        for (SubPaneIFace sp : SubPaneMgr.getInstance().getSubPanes())
        {
            if (sp.getClass().equals(WorkbenchPaneSS.class))
            {
                WorkbenchPaneSS wbSS = (WorkbenchPaneSS )sp;
                boolean canEnable = enabled && !wbSS.isChanged();
                wbSS.uploadDatasetBtn.setEnabled(canEnable);
                if (canEnable)
                {
                    wbSS.uploadDatasetBtn.setToolTipText(getResourceString("WB_UPLOAD_DATA"));
                }
                else
                {
                    wbSS.uploadDatasetBtn.setToolTipText(getResourceString("WB_UPLOAD_IN_PROGRESS"));
                }
            }
        }
        
    }
    
    /**
     * Updates uploadDatasetBtn wrt hasChanged
     */
    protected void updateUploadBtnState()
    {
        if (canUpload())
        {
           if (hasChanged)
           {
        	   uploadDatasetBtn.setEnabled(false);
           } else
           {
        	   if (!doIncrementalValidation)
        	   {
        		   uploadDatasetBtn.setEnabled(true);
        	   } else
        	   {
        		   uploadDatasetBtn.setEnabled(invalidCellCount.get() == 0);
        	   }
           }
           if (uploadDatasetBtn.isEnabled())
           {
               uploadDatasetBtn.setToolTipText(getResourceString("WB_UPLOAD_DATA"));
           }
           else if (doIncrementalValidation && invalidCellCount.get() > 0)
           {
        	   uploadDatasetBtn.setToolTipText(getResourceString("WB_UPLOAD_INVALID_DATA_HINT"));
           } else
           {
               uploadDatasetBtn.setToolTipText(getResourceString("WB_UPLOAD_UNSAVED_CHANGES_HINT"));
           }
        }
    }

    /**
     * Updates revertBtn wrt hasChanged
     */
    protected void updateRevertBtnState(final TableModelEvent e) {
    	if (revertBtn != null && revertBtn.isVisible() && hasChanged) {
    		//it would better to use e to check if it really did differ from the value in the db, 
    		//also tracking edited rows and cols would make checking for edits and showing original values easier
    		revertBtn.setEnabled(true);
    	}
    }

     /**
     * @return true if current user has upload privileges
     */
    protected boolean isUploadPermitted()
    {
    	return ContextMgr.getTaskByClass(WorkbenchTask.class).getPermissions().canModify();
    }
    /**
     * @return true if it is OK/possible to perform an upload.
     */
    protected boolean canUpload()
    {
    	return datasetUploader == null && isUploadPermitted() && !UIRegistry.isMobile();
    }
    
    /**
     * @param stats list of cell stats for row
     * @param wbRow 
     * @return list of updated data items
     */
    protected void updateCellStatuses(final List<CellStatusInfo> stats, final WorkbenchRow wbRow) {
    	Hashtable<Short, Short> exceptionalItems = new Hashtable<>();
        Map<WorkbenchDataItem, List<CellStatusInfo>> statMap = new HashMap<>();
		if (stats != null && stats.size() > 0) {
            for (CellStatusInfo issue : stats) {
                for (Integer col : issue.getColumns()) {
                    if (col >= 0) {
                        WorkbenchDataItem wbItem = wbRow.getItems().get(col.shortValue());
                        if (wbItem == null) {
                            //need to force creation of empty wbItem for blank cell
                            wbItem = wbRow.setData("", col.shortValue(), false, true);
                        }
                        if (wbItem != null) {
                            List<CellStatusInfo> statList = statMap.get(wbItem);
                            if (statList == null) {
                                statList = new ArrayList<>();
                                statMap.put(wbItem, statList);
                            }
                            statList.add(issue);
                        }
                    }
                }
            }
        }
        for (WorkbenchDataItem wbItem : wbRow.getWorkbenchDataItems()) {
                int statusCode = WorkbenchDataItem.VAL_OK;
                String statusText = "";
                if (statMap.get(wbItem) != null) {
                    for (CellStatusInfo info : statMap.get(wbItem)) {
                        statusCode = statusCode | info.getStatus();
                        if (!"".equals(statusText)) {
                            statusText += "\n; ";
                        }
                        statusText += info.getStatusText();
                    }
                }
                int originalStatusCode = wbItem.getEditorValidationStatus();
                boolean itemChanged = false;
                synchronized (wbItem) {
                    if (wbItem.getEditorValidationStatus() != statusCode) {
                        itemChanged = true;
                        wbItem.setEditorValidationStatus(statusCode);
                        wbItem.setStatusText("".equals(statusText) ? null : statusText);
                    }
                }
                if (itemChanged) {
                    if ((statusCode & WorkbenchDataItem.VAL_ERROR) != 0 && (originalStatusCode & WorkbenchDataItem.VAL_ERROR) == 0) {
                        invalidCellCount.getAndIncrement();
                    } else if ((statusCode & WorkbenchDataItem.VAL_ERROR) == 0 &&(originalStatusCode & WorkbenchDataItem.VAL_ERROR) != 0) {
                        invalidCellCount.getAndDecrement();
                    }
                    if (((statusCode & WorkbenchDataItem.VAL_MULTIPLE_MATCH) != 0
                            || (statusCode & WorkbenchDataItem.VAL_NEW_DATA) != 0)
                            && (originalStatusCode & WorkbenchDataItem.VAL_MULTIPLE_MATCH) == 0
                            && (originalStatusCode & WorkbenchDataItem.VAL_NEW_DATA) == 0) {
                        unmatchedCellCount.getAndIncrement();
                    } else if ((statusCode & WorkbenchDataItem.VAL_MULTIPLE_MATCH) == 0 && (statusCode & WorkbenchDataItem.VAL_NEW_DATA) == 0
                        && ((originalStatusCode & WorkbenchDataItem.VAL_MULTIPLE_MATCH) != 0
                            || (originalStatusCode & WorkbenchDataItem.VAL_NEW_DATA) != 0)) {
                        unmatchedCellCount.getAndDecrement();
                    }
                    if ((statusCode & WorkbenchDataItem.VAL_EDIT) != 0 && (originalStatusCode & WorkbenchDataItem.VAL_EDIT) == 0) {
                        editedCellCount.getAndIncrement();
                    } else if ((statusCode & WorkbenchDataItem.VAL_EDIT) == 0 && (originalStatusCode & WorkbenchDataItem.VAL_EDIT) != 0) {
                        decrementEditedCellCount();
                    }
                }
        }
    }
    
    /**
     * @param badRow
     * @return
     */
    protected CellStatusInfo createDupCatNumEntryCellStatus(Integer badRow)
    {
    	return new CellStatusInfo(badRow);
    }

    protected int getTotalIssueCount(final Pair<List<UploadTableInvalidValue>, List<Pair<UploadField, Object>>> issues,
                                     final List<UploadTableMatchInfo> matchInfo, final List<Integer> badCats) {
        return issues.getFirst().size() + (matchInfo == null ? 0 : matchInfo.size())
                + (badCats == null ? 0 : badCats.size())
                + issues.getSecond().size();
    }

    protected List<CellStatusInfo> getCellStatusInfosForIssues(final Pair<List<UploadTableInvalidValue>, List<Pair<UploadField, Object>>> issues,
                                     final List<UploadTableMatchInfo> matchInfo, final List<Integer> badCats) {
        List<CellStatusInfo> csis = new ArrayList<>(getTotalIssueCount(issues, matchInfo, badCats));
        for (UploadTableInvalidValue utiv : issues.getFirst()) {
            csis.add(new CellStatusInfo(utiv));
        }
        for (Pair<UploadField, Object> edit : issues.getSecond()) {
            Object origObj = edit.getSecond();
            String origVal =  origObj != null ? "'" + origObj.toString() + "'" : "Empty"; //more needed for dates and other tricky types
            String tip = String.format(getResourceString("WB_EDITED_CELL_TT"), origVal);
            List<Integer> cols = new ArrayList<Integer>();
            cols.add(edit.getFirst().getIndex());
            csis.add(new CellStatusInfo(WorkbenchDataItem.VAL_EDIT, tip, cols));
        }
        if (doIncrementalMatching && matchInfo != null) {
            for (UploadTableMatchInfo utmi : matchInfo) {
                if (utmi.getNumberOfMatches() != 1) { //for now we don't care if a single match exists
                    csis.add(new CellStatusInfo(utmi));
                }
            }
        }
        if (badCats != null) {
            for (Integer badCat : badCats) {
                csis.add(createDupCatNumEntryCellStatus(badCat));
            }
        }
        return csis;
    }
    /**
     * @param editRow
     * @param editCol (use -1 to validate entire row)
     */
    protected void updateRowValidationStatus(int editRow, int editCol, final List<Integer> badCats)
    {
		WorkbenchRow wbRow = workbench.getRow(editRow);
		Pair<List<UploadTableInvalidValue>, List<Pair<UploadField, Object>>> issues = getIncremental()
                ? workbenchValidator.endCellEdit(editRow, editCol, doIncrementalValidation, doIncrementalEditChecking)
				: new Pair<>();
       List<UploadTableMatchInfo> matchInfo = null;
		if (doIncrementalMatching) {
			try {
				//XXX Really should avoid matching invalid columns. But that is tricky with trees.
				matchInfo = workbenchValidator.getUploader().matchData(editRow, editCol, issues.getFirst());
			} catch (Exception ex) {
				//XXX what to do?, some exception might be caused by invalid data - filter out cols in exceptionalItems??
				//Maybe exceptions can be expected in general for a workbench-in-progress?
				//Or maybe we should blow up and force the workbench to close or something similarly drastic???
				ex.printStackTrace();
			}
		}
        updateCellStatuses(getCellStatusInfosForIssues(issues, matchInfo, badCats), wbRow);
    }
    
    /**
     * @param startRow
     * @param endRow
     * return true if validating
     * 
     * Validates all rows between startRow and endRow
     * startRow and endRow are assumed to be model indices, 
     */
    protected boolean validateRows(final int startRow, final int endRow)
    {
    	if (getIncremental())
		{
			validateRows(null, startRow, endRow, true, false);
			return true;
		}
    	return false;
    }
    
    /**
     * @param rows
     * return true if validating
     * 
     * Validates rows 
     * rows assumed to contain model indices, 
     */
    public boolean validateRows(final int[] rows)
    {
    	if (getIncremental())
		{
			validateRows(rows, -1, -1, rows.length <= 17, false);
			//validateRows(rows, -1, -1, true, null);
    		return true;
		}
    	return false;
    }
    
    /**
     * 
     */
    private void readRegisteries()
    {
        HashMap<String, WBPluginInfo> plugins = new HashMap<String, WorkbenchPaneSS.WBPluginInfo>();
        String fileName = "wb_registry.xml";
        
        String path = XMLHelper.getConfigDirPath(fileName);
        readRegistry(path, plugins);
        
        path = AppPreferences.getLocalPrefs().getDirPath() + File.separator + fileName;
        readRegistry(path, plugins);
        
        for (WBPluginInfo wbp : plugins.values())
        {
            String prefName = wbp.getPrefName();
            if (StringUtils.isEmpty(prefName) || AppPreferences.getLocalPrefs().getBoolean(prefName, false))
            {
                createPlugin(wbp.getClassName(), wbp.getIconName(), wbp.getToolTip());
            }
        }
    }
    
    /**
     * @param path
     * @param plugins
     */
    private void readRegistry(final String path, 
                              final HashMap<String, WBPluginInfo> plugins)
    {
        File file = new File(path);
        if (file.exists())
        {
            try
            {
                Element root = XMLHelper.readFileToDOM4J(file);
                if (root != null)
                {
                    for (Iterator<?> i = root.elementIterator("plugin"); i.hasNext();) //$NON-NLS-1$
                    {
                        Element node = (Element) i.next();
                        String  pluginName = node.attributeValue("name"); //$NON-NLS-1$
                        String  className  = node.attributeValue("class"); //$NON-NLS-1$
                        String  iconName   = node.attributeValue("icon"); //$NON-NLS-1$
                        String  toolTip    = node.attributeValue("tooltip"); //$NON-NLS-1$
                        String  prefName   = node.attributeValue("pref"); //$NON-NLS-1$
                        
                        if (StringUtils.isNotEmpty(pluginName) &&
                            StringUtils.isNotEmpty(className) &&
                            StringUtils.isNotEmpty(iconName))
                        {
                            WBPluginInfo wbp = new WBPluginInfo(pluginName, className, iconName, toolTip, prefName);
                            plugins.put(pluginName, wbp);
                            
                        } else
                        {
                            log.error("WBPlugin in error: One of the fields (name, class, icon) is null or empty.");
                        }
                    }
                }
                
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * @param className
     * @param iconName
     * @param tooltipKey
     */
    protected void createPlugin(final String className, final String iconName, final String tooltipKey)
    {
        try
        {
            final Class<?>             wbPluginCls = Class.forName(className);
            final WorkBenchPluginIFace wbPlugin    = (WorkBenchPluginIFace) wbPluginCls.newInstance();
            workBenchPlugins.put(wbPluginCls, wbPlugin);
            
            wbPlugin.setWorkbenchPaneSS(this);
            wbPlugin.setSpreadSheet(spreadSheet);
            wbPlugin.setWorkbench(workbench);

            workBenchPluginSSBtns.addAll(wbPlugin.getSSButtons());
            workBenchPluginFormBtns.addAll(wbPlugin.getFormButtons());
            
//            List<String> missingFields = wbPlugin.getMissingFieldsForPlugin();
//            if (missingFields != null && missingFields.size() > 0)
//            {
//                btn.setEnabled(false);
//                String ttText = "<p>" + getResourceString("WB_ADDITIONAL_FIELDS_REQD") + ":<ul>";
//                for (String reqdField : missingFields)
//                {
//                    ttText += "<li>" + reqdField + "</li>";
//                }
//                ttText += "</ul>";
//                String origTT = btn.getToolTipText();
//                btn.setToolTipText("<html>" + origTT + ttText);
//            }
//            else
//            {
//                btn.setEnabled(true);
//            }
//            
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        } catch (InstantiationException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }
    
    public WorkBenchPluginIFace getPlugin(Class<?> cls)
    {
    	return workBenchPlugins.get(cls);
    }
    
    //----------------------------------------------------------------------------------------
    //--
    //----------------------------------------------------------------------------------------
    private class ValidationWorker extends javax.swing.SwingWorker<Object, Object>
    {
		private final int[] rows;
		private final int startRow;
		private final int endRow;
		private final boolean useGlassPane;
		private AtomicReference<ProgressGlassPane> glassPane = new AtomicReference<>(null);
		private final boolean allowCancel;
		private final AtomicBoolean cancelledByUser = new AtomicBoolean(false);
		
		//Vectors are thread safe?? Right??
		//private final Vector<Integer> deletedRows = new Vector<Integer>();
		
		
    	/**
		 * @param rows
		 * @param startRow
		 * @param endRow
		 */
		public ValidationWorker(int[] rows, int startRow, int endRow, 
				boolean useGlassPane, boolean allowCancel)
		{
			super();
			this.rows = rows;
			this.startRow = startRow;
			this.endRow = endRow;
			this.useGlassPane = useGlassPane;
			this.allowCancel = allowCancel;
            if (useGlassPane)
            {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ValidationWorker.this.glassPane.set(UIRegistry.writeGlassPaneMsg(
                                String.format(getResourceString("WorkbenchPaneSS.Validating"),
                                        new Object[] {isUpdateDataSet() ? "" : workbench.getName()}),
                                WorkbenchTask.GLASSPANE_FONT_SIZE/*, allowCancel*/));
                        if (allowCancel)
                        {
                            log.warn("cancel no longer supported for WB validation");
//                            UIRegistry.displayStatusBarText(getResourceString("WorkbenchPaneSS.CancelValidationHint"));
//                            ValidationWorker.this.glassPane.get().addMouseListener(new MouseListener() {
//
//                                /*
//                                 * (non-Javadoc)
//                                 *
//                                 * @see
//                                 * java.awt.event.MouseListener#mouseClicked(java.awt
//                                 * .event.MouseEvent)
//                                 */
//                                @Override
//                                public void mouseClicked(MouseEvent arg0) {
//                                    if (!arg0.isConsumed())
//                                    {
//                                        if (arg0.getClickCount() == 2)
//                                        {
//                                            SwingUtilities.invokeLater(new Runnable() {
//
//                                                /* (non-Javadoc)
//                                                 * @see java.lang.Runnable#run()
//                                                 */
//                                                @Override
//                                                public void run() {
//                                                    if (UIRegistry.displayConfirmLocalized(
//                                                            "WorkbenchPaneSS.CancelValidationConfirmTitle",
//                                                            "WorkbenchPaneSS.CancelValidationConfirmMsg", "YES", "NO",
//                                                            JOptionPane.QUESTION_MESSAGE))
//                                                    {
//                                                        cancelledByUser.set(true);
//                                                    }
//                                                }
//
//
//                                            });
//                                        }
//                                    }
//                                }
//
//                                /*
//                                 * (non-Javadoc)
//                                 *
//                                 * @see
//                                 * java.awt.event.MouseListener#mouseEntered(java.awt
//                                 * .event.MouseEvent)
//                                 */
//                                @Override
//                                public void mouseEntered(MouseEvent arg0) {
//                                    // TODO Auto-generated method stub
//
//                                }
//
//                                /*
//                                 * (non-Javadoc)
//                                 *
//                                 * @see
//                                 * java.awt.event.MouseListener#mouseExited(java.awt
//                                 * .event.MouseEvent)
//                                 */
//                                @Override
//                                public void mouseExited(MouseEvent arg0) {
//                                    // TODO Auto-generated method stub
//
//                                }
//
//                                /*
//                                 * (non-Javadoc)
//                                 *
//                                 * @see
//                                 * java.awt.event.MouseListener#mousePressed(java.awt
//                                 * .event.MouseEvent)
//                                 */
//                                @Override
//                                public void mousePressed(MouseEvent arg0) {
//                                    // TODO Auto-generated method stub
//
//                                }
//
//                                /*
//                                 * (non-Javadoc)
//                                 *
//                                 * @see
//                                 * java.awt.event.MouseListener#mouseReleased(java.awt
//                                 * .event.MouseEvent)
//                                 */
//                                @Override
//                                public void mouseReleased(MouseEvent arg0) {
//                                    // TODO Auto-generated method stub
//
//                                }
//
//                            });
                        }

                    }
                });
            }
        }

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Object doInBackground() throws Exception {
			boolean checkedCatNums = catNumChecker == null;
			if (rows != null) {
				int count = rows.length;
				int rowCount = 0;
				for (int row : rows) {
					if (cancelledByUser.get()) {
						break;
					}
					//int adjustedRow = adjustRow(row);
					if (row != -1) {

						if (!checkedCatNums) {
							Vector<Integer> badCats = catNumChecker.checkValues(rows);
							updateRowValidationStatus(row, -1, badCats);
							checkedCatNums = true;
						} else {
							updateRowValidationStatus(row, -1, null);
						}
					}
					if (useGlassPane) {
						//System.out.println((int)( (100.0 * ++rowCount) / count));
						if (glassPane.get() != null) {
                            glassPane.get().setProgress((int) ((100.0 * ++rowCount) / count));
                        }
					}
				}
			} else {
				int count = endRow - startRow + 1;
				int rowCount = 0;
				try {
					for (int row = startRow; row <= endRow; row++) {
						if (cancelledByUser.get()) {
							break;
						}
						//int adjustedRow = adjustRow(row);
						if (row != -1) {
							if (!checkedCatNums) {
								Vector<Integer> badCats = catNumChecker.checkValues(rows);
								updateRowValidationStatus(row, -1, badCats);
								checkedCatNums = true;
							} else {
								updateRowValidationStatus(row, -1, null);
							}
						}
						if (useGlassPane) {
							int progress = (int)( (100.0 * ++rowCount) / count);
							//System.out.println(progress);
                            if (glassPane.get() != null) {
                                glassPane.get().setProgress(progress);
                            }
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (!isCancelled())
			{
				//System.out.println("done(): remove current validationWorker");
				validationWorkerQueue.remove(); //remove this worker
			
				if (validationWorkerQueue.peek() != null)
				{
					//System.out.println("done(): executing next validationWorker");
					validationWorkerQueue.peek().execute();
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		protected void done()
		{
			super.done();
			UIRegistry.displayStatusBarText(null);
			if (useGlassPane)
			{
				UIRegistry.clearGlassPaneMsg();
			}
			if (isCancelled())
			{
				//currently cancellation only occurs during shutdown.
				//System.out.println("done(): Clearing validationWorkerQueue");
				validationWorkerQueue.clear();
			}
			if (cancelledByUser.get())
			{
				//XXX need to verify cancel. Inside the doInBackground loop.
				turnOffIncrementalValidation();
				turnOffIncrementalMatching();
				turnOffIncrementalEditChecking();
				uploadToolPanel.uncheckAutoMatching();
				uploadToolPanel.uncheckAutoValidation();
				uploadToolPanel.uncheckAutoEditChecking();
				uploadToolPanel.updateBtnUI();
			}
			
//			System.out.println("done(): remove current validationWorker");
//			validationWorkerQueue.remove(); //remove this worker
//			
//			if (validationWorkerQueue.peek() != null)
//			{
//				System.out.println("done(): executing next validationWorker");
//				validationWorkerQueue.peek().execute();
//			}

			if (validationWorkerQueue.peek() == null)
			{
				SwingUtilities.invokeLater(new Runnable(){

					@Override
					public void run() 
					{
						updateBtnUI();
					}
	    		
				});
			}

			if (isCancelled())
			{
				return;
			}
			
			boolean savedBlockChanges = blockChanges;
			try
			{
				blockChanges = true;
				if (rows == null)
				{
					model.fireTableRowsUpdated(startRow, endRow); //XXX model vs table rows??
				}
				else
				{
					model.fireDataChanged();
				}
			} finally
			{
				blockChanges = savedBlockChanges;
			}
		}			
		
//		public void rowDeleted(int row)
//		{
//			deletedRows.add(row);
//		}
    }
    
    /**
     */
    public void validateAll() {
        //System.out.println("validating all " + spreadSheet.getRowCount() + " rows.");
        if (catNumChecker != null) {
            //apparently this is pretty quick, but it might be necessary to have a glass pane for this step...
            catNumChecker.clear();
            for (int r = 0; r < spreadSheet.getRowCount(); r++) {
                catNumChecker.setValue(r, spreadSheet.getStringAt(r, catNumCol), false);
            }
        }
        validateRows(null, 0, spreadSheet.getRowCount() - 1, false, true);
    }
    
    /**
     * @param rows
     * @param startRow
     * @param endRow
     * @param doSecretly
     * @param allowCancel
     */
    public void validateRows(final int[] rows, final int startRow, final int endRow, boolean doSecretly, final boolean allowCancel) {

        ValidationWorker newWorker = new ValidationWorker(rows, startRow, endRow, !doSecretly, allowCancel);
        validationWorkerQueue.add(newWorker);
        if (validationWorkerQueue.peek() == newWorker) {
            newWorker.execute();
        }
        SwingUtilities.invokeLater(() -> {
            if (addRowsBtn != null) addRowsBtn.setEnabled(false);
            if (deleteRowsBtn != null) deleteRowsBtn.setEnabled(false);
        });
    }
    
	/**
	 * @param col
	 * @return true if the field mapped to col has an incrementing formatter.
	 */
	public UIFieldFormatterIFace getFormatterForCol(int col)
	{
		if (workbenchValidator != null && workbenchValidator.getUploader() != null)
		{
			DBFieldInfo fldInfo = workbenchValidator.getUploader().getFieldInfoForCol(col);
			if (fldInfo != null)
			{
				return fldInfo.getFormatter();
			}
		}
		return null;
	}

    
	/**
	 * @param startRow
	 * @param endRow
	 * @param rows
	 * @param check
	 * @return
	 */
	protected List<Integer> setCatNumValues(int startRow, int endRow, int[] rows, boolean check)
	{
		List<Integer> result = check ? new Vector<Integer>() : null;
		if (rows == null)
		{
			for (int row = startRow; row <= endRow; row++)
			{
				if (check)
				{	
					result.addAll(catNumChecker.setValue(row, spreadSheet.getStringAt(row, catNumCol), check));
				} else
				{
					catNumChecker.setValue(row, spreadSheet.getStringAt(row, catNumCol), check);
				}
			}
		} else
		{
			for (int row : rows)
			{
				if (check)
				{
					result.addAll(catNumChecker.setValue(row, spreadSheet.getStringAt(row, catNumCol), check));
				} else
				{
					catNumChecker.setValue(row, spreadSheet.getStringAt(row, catNumCol), check);
				}
					
			}
		}
		return result;
	}
	
    //------------------------------------------------------------
    // Inner Classes
    //------------------------------------------------------------

    public class GridCellEditor extends DefaultCellEditor implements TableCellEditor//, UndoableTextIFace
    {
        protected JComponent          uiComponent;
        protected int                 length;
        protected LengthInputVerifier verifier;
        protected JButton             ceSaveBtn;
        protected DocumentListener    docListener;
        protected int				  editCol = -1;
        protected int                 editRow = -1;
        //protected UndoManager undoManager = new UndoManager();
        protected boolean             isEditable = true;

        public GridCellEditor(final JTextField textField, final String caption, final int length, final JButton gcSaveBtn) {
            super(textField);
            init(textField, caption, length, gcSaveBtn);
         }
        
        public GridCellEditor(final JComboBox<?> combo, final String caption, final int length, final JButton gcSaveBtn) {
        	super(combo);
        	init(combo, caption, length, gcSaveBtn);
        }

        public boolean isEditable() {
            return isEditable;
        }

        public void setEditable(boolean editable) {
            isEditable = editable;
        }

        protected void init(final JComponent comp, final String caption, final int length, final JButton gcSaveBtn)
        {
           	this.uiComponent = comp;
            this.length    = length;
            this.ceSaveBtn = saveBtn;

            
            //verifier = new LengthInputVerifier(caption, length);
            //uiComponent.setInputVerifier(verifier);

            uiComponent.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//            docListener = new DocumentListener() {
//                public void changedUpdate(DocumentEvent e)
//                {
//                    validateDoc();
//                }
//                
//                public void insertUpdate(DocumentEvent e)
//                {
//                    validateDoc();
//                }
//                
//                public void removeUpdate(DocumentEvent e) 
//                {
//                    validateDoc();
//                }
//            };
            //textField.getDocument().addDocumentListener(docListener);
        }
        
        /**
         * Makes sure the document is the correct length.
         */
        protected void validateDoc()
        {
            if (!verifier.verify(uiComponent))
            {
                ceSaveBtn.setEnabled(false);
            }
        }

        /**
         * clean up after processing stopCellEditing
         */
        protected void endStopCellEditProcessing()
        {
        	editCol = -1;
        	editRow = -1;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.DefaultCellEditor#stopCellEditing()
         */
        @Override
        public boolean stopCellEditing() {
        	boolean result = super.stopCellEditing();
            if (editRow == -1 || editCol == -1) {
            	editRow = -1;
            	editCol = -1;
            	return result; //a 'superfluous' re-call of this method.
            }
        	if (result) {
            	if (verifier != null && !verifier.verify(uiComponent)) {
            		ceSaveBtn.setEnabled(false);
                	editRow = -1;
                	editCol = -1;
            		return false;
            	}
            	if (getIncremental() && workbenchValidator != null) {
            		Vector<Integer> badCats = null;
            		if (catNumChecker != null && editCol == catNumCol) {
            			badCats = catNumChecker.setValue(editRow, ((JTextField )uiComponent).getText(), true);
            		}
            		updateRowValidationStatus(spreadSheet.convertRowIndexToModel(editRow), spreadSheet.convertColumnIndexToModel(editCol), badCats);
            		updateBtnUI();
            	}
            	endStopCellEditProcessing();
            }
        	return result;
        }

        
        
        /* (non-Javadoc)
         * @see javax.swing.DefaultCellEditor#cancelCellEditing()
         */
        @Override
		public void cancelCellEditing()
		{
			editRow = -1;
			editCol = -1;
			super.cancelCellEditing();
		}

		/* (non-Javadoc)
         * @see javax.swing.CellEditor#getCellEditorValue()
         */
        @Override
        public Object getCellEditorValue() 
        {
            return ((JTextField )uiComponent).getText();
        }

        /* (non-Javadoc)
         * @see javax.swing.AbstractCellEditor#isCellEditable(java.util.EventObject)
         */
        @Override
        public boolean isCellEditable(EventObject e)
        {
            return isEditable;
        }

        //
        //          Implementing the CellEditor Interface
        //
        
        /* (non-Javadoc)
         * @see javax.swing.DefaultCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
         */
        @Override
        public Component getTableCellEditorComponent(JTable  table, 
                                                     Object  value,
                                                     boolean isSelected,
                                                     int     row, 
                                                     int     column)
        {
            // This is needed for Mac OS X, not Linux and I am not sure about Windows
            // If the fonts aren't the same then the double click doesn't find the correct
            // storage to insert the cursor
            if (table.getCellRenderer(row, column) instanceof JComponent)
            {
                JComponent jcomp = (JComponent)table.getCellRenderer(row, column);
                Font cellFont = jcomp.getFont();
                Font txtFont  = uiComponent.getFont();
                if (cellFont != txtFont)
                {
                    uiComponent.setFont(cellFont);
                }
            }            
            
            ((JTextField )uiComponent).setText(value != null ? value.toString() : "");
            try
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {            
                        Caret c = ((JTextField )uiComponent).getCaret();

                        // for keyboard
                        c.setVisible(true);
                        c.setSelectionVisible(true);
                        //textField.requestFocus();
                    }
                });
            }
            catch( Exception e )
            {
                // ignore it?
            }
            editCol = column;
            editRow = row;
            return uiComponent;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.UIRegistry.UndoableTextIFace#getUndoManager()
         */
        public UndoManager getUndoManager()
        {
            return null;//undoManager;
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.UIRegistry.UndoableTextIFace#getText()
         */
        public JTextComponent getTextComponent()
        {
            return (JTextComponent )uiComponent;
        }
        
        /**
         * cleans up listeners etc.
         */
        public void cleanUp()
        {
            uiComponent.setInputVerifier(null);
            //textField.getDocument().removeDocumentListener(docListener);
            uiComponent = null;
            verifier  = null;
            ceSaveBtn = null;
        }
     }

     // experimental
//     public class GridCellQueryComboEditor extends GridCellEditor {
//         public ValComboBoxFromQuery getQcbx() {
//             return qcbx;
//         }
//
//         public DataProviderSessionIFace getSession() {
//             return session;
//         }
//
//         public DataModelObjBase getDataObj(final Object id) {
//             return session.get(Locality.class, Integer.valueOf((String)id));
//         }
//
//         ValComboBoxFromQuery qcbx;
//         DataProviderSessionIFace session;
//
//	    public GridCellQueryComboEditor(final ValComboBoxFromQuery qcbx, final String caption,
//                                         final int length, final JButton gcSaveBtn) {
//             super(qcbx.getTextWithQuery().getTextField(), caption, length, gcSaveBtn);
//             this.qcbx = qcbx;
//             session = DataProviderFactory.getInstance().createSession();
//         }
//
//         @Override
//         protected void finalize() throws Throwable {
//             super.finalize();
//             if (session != null && session.isOpen()) {
//                 session.close();
//             }
//         }
//
//         /* (non-Javadoc)
//          * @see edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS.GridCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
//          */
//         @Override
//         public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//             editCol = column;
//             editRow = row;
//
//             System.out.println("GridCellQueryComboEditor.getTableCellEditorComponent");
//             qcbx.setValue(getDataObj(value), value.toString());
//             return uiComponent;
//         }
//
//         /* (non-Javadoc)
//          * @see javax.swing.CellEditor#getCellEditorValue()
//          */
//         @Override
//         public Object getCellEditorValue() {
//             //DataModelObjBase result = (DataModelObjBase)qcbx.getValue();
//             System.out.println("GridCellQueryComboEditor.getCellEditorValue");
//             return qcbx.getTextWithQuery().getTextField().getText();
////             if (result != null) {
////                 return result.getId();
////             } else {
////                 return "";
////             }
//         }
//
//         /* (non-Javadoc)
//          * @see edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS.GridCellEditor#endStopCellEditProcessing()
//          */
//         @Override
//         protected void endStopCellEditProcessing() {
//             //don't do nuthin.
//         }
//     }
    /**
     * @author timo
     *
     *Cell Editor for pick lists (and possibly lookups)
     */
    public class GridCellListEditor extends GridCellEditor
    {        
    	/**
    	 * @param combo
    	 * @param caption
    	 * @param length
    	 * @param gcSaveBtn
    	 */
    	public GridCellListEditor(final JComboBox<?> combo, final String caption, final int length, final JButton gcSaveBtn)
        {
    		super(combo, caption, length, gcSaveBtn);
    		//model = new DefaultComboBoxModel(pickList.getList());
    		//combo.setModel(model);
        }
    	
        /* (non-Javadoc)
         * @see edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS.GridCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
         */
        @Override
        public Component getTableCellEditorComponent(JTable  table, 
                                                     Object  value,
                                                     boolean isSelected,
                                                     int     row, 
                                                     int     column)
        {
            editCol = column;
            editRow = row;
            ((JComboBox<?> )uiComponent).setSelectedItem(value);
            return uiComponent;
        }
   	        
		/* (non-Javadoc)
         * @see javax.swing.CellEditor#getCellEditorValue()
         */
        @Override
        public Object getCellEditorValue() 
        {
            return ((JComboBox<?> )uiComponent).getSelectedItem().toString();
        }

		/* (non-Javadoc)
		 * @see edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS.GridCellEditor#endStopCellEditProcessing()
		 */
		@Override
		protected void endStopCellEditProcessing() 
		{
			//don't do nuthin. 		
		}    	
        
        /**
         * @return the model
         */
        public ComboBoxModel<?> getList()
        {
        	return ((JComboBox<?> )uiComponent).getModel();
        }
        
    }
    
    //------------------------------------------------------------
    // Switches between the Grid View and the Form View
    //------------------------------------------------------------
    class SwitcherAL implements ActionListener
    {
        protected DropDownButtonStateful switcherComp;
        public SwitcherAL(final DropDownButtonStateful switcherComp)
        {
            this.switcherComp = switcherComp;
        }
        public void actionPerformed(ActionEvent ae)
        {
            showPanel(((DropDownButtonStateful)ae.getSource()).getCurrentIndex() == 0 ? PanelType.Spreadsheet : PanelType.Form);
        }
    }
    
    /**
     * @author timo
     *
     *Renderer for workbench cells that checks cells validation status and status text.
     */
    public class WbCellRenderer extends DefaultTableCellRenderer {
		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int tblRow, int tblColumn) {
			JLabel lbl = (JLabel) super.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, tblRow,
					tblColumn);
			
			int          modelRow = spreadSheet.convertRowIndexToModel(tblRow);
			WorkbenchRow wbRow    = workbench.getRow(modelRow);
			String       cardImageFullPath = wbRow.getCardImageFullPath();
			if (cardImageFullPath != null) {
				String filename = FilenameUtils.getBaseName(cardImageFullPath);
				filename = FilenameUtils.getName(cardImageFullPath);
				lbl.setText(filename);
				lbl.setHorizontalAlignment(SwingConstants.CENTER);
			}
//			if (table.getColumnModel().getColumn(tblColumn).getCellEditor() instanceof GridCellQueryComboEditor) {
//                GridCellQueryComboEditor e = ((GridCellQueryComboEditor)table.getColumnModel().getColumn(tblColumn).getCellEditor());
//			    e.getQcbx().setValue(e.getDataObj(value), value.toString());
//			    System.out.println("wbcellrenderer.gettablecellrendercomponent");
//                lbl.setText(e.getQcbx().getTextWithQuery().getTextField().getText());
//            }
			lbl.setEnabled(isEditable);
			return lbl;
		}

		private boolean isEditable = true;

        public void setEditable(boolean editable) {
            isEditable = editable;
        }

        public boolean isEditable() {
            return isEditable;
        }
    }
    
    /**
     * @author timo
     * 
     * Column header renderer that adds icon for the specify table that contains the field the column is mapped to.
     *
     */
    public class WbTableHeaderRenderer implements TableCellRenderer
    {
        protected JLabel iconLbl;
        protected JPanel panel;
        protected DefaultTableCellHeaderRenderer header;
        
        /**
         * 
         */
        public WbTableHeaderRenderer(final String tableName)
        {
            super();
            
            ImageIcon icon = tableName != null ? IconManager.getIcon(tableName, IconManager.IconSize.Std16) : null;
            iconLbl = UIHelper.createLabel(null, icon);
            header  = new DefaultTableCellHeaderRenderer();
            header.setHorizontalTextPosition(JLabel.LEFT);
            header.setEnabled(icon != null);
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("p,1px,f:p:g", "f:p:g"));
            pb.add(iconLbl, cc.xy(1, 1));
            pb.add(header, cc.xy(3, 1));
            panel = pb.getPanel();
            panel.setOpaque(false);
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex)
        {
            header.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
            header.setOpaque(false);
            //header.setBackground(Color.BLUE);
            return panel;
        }
        
        //public void validate() {}
        //public void revalidate() {}
        //protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
        //public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    }

    
    //------------------------------------------------------------
    // TableCellRenderer for showing the proper Icon when there 
    // is an image column (CardImage)
    //------------------------------------------------------------
    public class ImageRenderer extends DefaultTableCellRenderer 
    {
        protected Border selectedBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(99, 130, 191));// Blue-Border
        
        @Override
        public Component getTableCellRendererComponent(JTable table, 
                                                       Object value,
                                                       boolean isSelected, 
                                                       boolean hasFocus, 
                                                       int row, 
                                                       int column) 
        {
            setText("");
            if (value instanceof ImageIcon)
            {
                setIcon((ImageIcon)value);
                this.setHorizontalAlignment(SwingConstants.CENTER);
            } else
            {
                setIcon(null);
            }
            if (isSelected)
            {
                setBackground(table.getSelectionBackground());
            } 
            else
            {
                setBackground(table.getBackground());
            }
            if (hasFocus)
            {
                setBorder(selectedBorder);
            } else
            {
                setBorder(null);
            }
            return this;
        }
    }
    
    public class CellPosition extends Pair<Integer, Integer>
    {
    	public CellPosition(final int row, final int col)
    	{
    		super(row, col);
    	}
    	
    	@Override
    	public String toString()
    	{
    		return UIRegistry.getResourceString("WB_ROW") + ": " + (getFirst()+1) + ", "
    				+ UIRegistry.getResourceString("WB_COLUMN") + ": " + (getSecond()+1);
    	}
    }
    
    public class ColumnHeaderListener extends MouseAdapter
    {
        @Override
        public void mousePressed(MouseEvent evt)
        {
            if (spreadSheet != null && spreadSheet.getCellEditor() != null)
            {  
                spreadSheet.getCellEditor().stopCellEditing();
            }
            /* this code shows how to get what column was clicked on
             * 
            JTable table = ((JTableHeader) evt.getSource()).getTable();
            TableColumnModel colModel = table.getColumnModel();

            // The index of the column whose header was clicked
            int vColIndex = colModel.getColumnIndexAtX(evt.getX());
            int mColIndex = table.convertColumnIndexToModel(vColIndex);

            // Return if not clicked on any column header
            if (vColIndex == -1) { return; }

            // Determine if mouse was clicked between column heads
            Rectangle headerRect = table.getTableHeader().getHeaderRect(vColIndex);
            if (vColIndex == 0)
            {
                headerRect.width -= 3; // Hard-coded constant
            } else
            {
                headerRect.grow(-3, 0); // Hard-coded constant
            }
            if (!headerRect.contains(evt.getX(), evt.getY()))
            {
                // Mouse was clicked between column heads
                // vColIndex is the column head closest to the click

                // vLeftColIndex is the column head to the left of the click
                int vLeftColIndex = vColIndex;
                if (evt.getX() < headerRect.x)
                {
                    vLeftColIndex--;
                }
                spreadSheet.getCellEditor().stopCellEditing();
                System.out.println(vLeftColIndex);
            }*/
        }
    }

    /**
     * @param hasChanged the hasChanged to set
     */
    public void setHasChanged(boolean hasChanged)
    {
        this.hasChanged = hasChanged;
    }

    /**
     * @return the spreadSheet
     */
    public SpreadSheet getSpreadSheet()
    {
        return spreadSheet;
    }
    
    /**
     * @param col
     * @return the max width for column indexed by col.
     */
    public Integer getColumnMaxWidth(int col)
    {
        return this.columnMaxWidths[col];
    }
    
    /**
     * @return
     */
    public CellRenderingAttributes getCellDecorator()
    {
    	return cellRenderAtts;
    }
    
    /**
     * A debugging tool Used to find discrepancies between workbench and specify schemas.
     */
    protected void compareSchemas()
    {
        List<Pair<DBFieldInfo, DBFieldInfo>> badFlds = new LinkedList<Pair<DBFieldInfo, DBFieldInfo>>();
        DBTableIdMgr wbSchema = WorkbenchTask.getDatabaseSchema(false);
        for (DBTableInfo wbTbl : wbSchema.getTables())
        {
            DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoByTableName(wbTbl.getName());
            if (tbl != null)
            {
                for (DBFieldInfo wbFld : wbTbl.getFields())
                {
                    DBFieldInfo fld = tbl.getFieldByName(wbFld.getName());
                    if (fld == null && wbFld.getName().contains("emarks"))
                    {
                        fld = tbl.getFieldByName("remarks");
                    }
                    if (fld == null && wbFld.getName().matches(".*Text[1|2|3|4|5|6|7|8|9][0|1|2|3|4|5|6|7|8|9]*"))
                    {
                        fld = tbl.getFieldByName("text" + wbFld.getName().substring(wbFld.getName().indexOf("Text", wbFld.getName().length()-6)+4));
                    }
                    if (fld == null && wbFld.getName().matches(".*YesNo[1|2|3|4|5|6|7|8|9]"))
                    {
                        fld = tbl.getFieldByName("yesNo" + wbFld.getName().substring(wbFld.getName().length()-1));
                    }
                    if (fld == null && wbFld.getName().matches(".*Number[1|2|3|4|5|6|7|8|9]"))
                    {
                        fld = tbl.getFieldByName("number" + wbFld.getName().substring(wbFld.getName().length()-1));
                    }
                    if (fld != null)
                    {
                        if (fld.getLength() != -1 && fld.getLength() != wbFld.getLength())
                        {
                            badFlds.add(new Pair<DBFieldInfo, DBFieldInfo>(fld, wbFld));
                        }
                    }
                    else
                    {
                        System.out.println("couldn't find field: " + wbTbl.getName() + "." + wbFld.getName());
                    }
                }
            }
            else
            {
                System.out.println("couldn't find table: " + wbTbl.getName());
            }
        }
        if (badFlds.size() > 0)
        {
            System.out.println("Change the lengths for the following fields...");
            for (Pair<DBFieldInfo, DBFieldInfo> badFld : badFlds)
            {
                DBFieldInfo fld = badFld.getFirst();
                DBFieldInfo wbFld = badFld.getSecond();
                System.out.println(fld.getTableInfo().getName() + "." + fld.getName() + "(" + wbFld.getTableInfo().getName() + "." + wbFld.getName() + ") - " + fld.getLength());
            }
        }
        else
        {
            System.out.println("All field lengths are OK.");
        }
    }
    
    public void incShutdownLock()
    {
        shutdownLock.incrementAndGet();
    }
    
    public void decShutdownLock()
    {
        shutdownLock.decrementAndGet();
    }
    
    
    /**
     * @return list tables in the workbench that support attachments
     */
    public List<UploadTable> getAttachableTables()
    {
    	if (workbenchValidator == null)
    	{
    		buildValidator(true);
    	}
    	if (workbenchValidator != null)
    	{
    		return workbenchValidator.getUploader().getAttachableTablesInUse();
    	}
    	return null;
    }

	/**
	 * @return the doIncrementalValidation
	 */
	public boolean isDoIncremental() 
	{
		return getIncremental();
	}
	
	protected List<Integer> getCatNumCol()
	{
		if (workbenchValidator != null)
		{
			
		}
		return null;
	}
	
	public FormPaneWrapper getFormPane()
	{
	    return formPane;
	}
	/**
	 * @author timo
	 *
	 */
	private class CellStatusInfo
	{
		protected final short status;
		protected final String statusText;
		protected final List<Integer> columns;
		
		/**
		 * @param invalidValue
		 */
		public CellStatusInfo(UploadTableInvalidValue invalidValue)
		{
			if (invalidValue.isWarn())
			{
				//XXX this works for now only because isWarn is only true when non-readonly picklists don't contain values
				status = WorkbenchDataItem.VAL_NEW_DATA;
			} else
			{
				status = WorkbenchDataItem.VAL_ERROR;
			}
			statusText = invalidValue.getDescription(); 
			columns = invalidValue.getCols();		
		}
		
		/**
		 * @param matchInfo
		 */
		public CellStatusInfo(UploadTableMatchInfo matchInfo)
		{
			if (matchInfo.isSkipped())
			{
				status = WorkbenchDataItem.VAL_NOT_MATCHED;
			} else
			{
				//Currently, getNumberOfMatches() will never return 1
				status = matchInfo.getNumberOfMatches() == 0 ? WorkbenchDataItem.VAL_NEW_DATA :
					WorkbenchDataItem.VAL_MULTIPLE_MATCH;
			}
			statusText = matchInfo.getDescription(); 
			columns = matchInfo.getColIdxs();			
		}
		
		/**
		 * @param dupCatNumRow
		 */
		public CellStatusInfo(Integer dupCatNumRow)
		{
			status = WorkbenchDataItem.VAL_ERROR;
			statusText = UIRegistry.getResourceString("WorkbenchPaneSS.DupCatNumEntry");
			columns = new Vector<Integer>(1);
			columns.add(catNumCol);
		}
		
		/**
		 * @param status
		 * @param statusText
		 * @param columns
		 */
		public CellStatusInfo(final short status, final String statusText, final List<Integer> columns) {
			this.status = status;
			this.statusText = statusText;
			this.columns = columns;
		}
		/**
		 * @return the status
		 */
		public short getStatus() 
		{
			return status;
		}
		
		/**
		 * @return the statusText
		 */
		public String getStatusText() 
		{
			return statusText;
		}

		/**
		 * @return the columns
		 */
		public List<Integer> getColumns() 
		{
			return columns;
		}
	}

	
	/**
	 * @author timo
	 *
	 */
	private class GridCellPredicate implements HighlightPredicate
	{
		final static public int ValidationPredicate = 0;
		final static public int MatchingPredicate = 1;
		final static public int EditedPredicate = 2;
		final static public int AnyPredicate = 3;
		protected final int activation;
		protected final Short[] conditions;
		protected final Short[] antiConditions;
		
		public GridCellPredicate(int activation, Short[] conditions, Short[] antiConditions)
		{
			this.activation = activation;
			this.conditions = conditions;
			this.antiConditions = antiConditions;
		}

		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.decorator.HighlightPredicate#isHighlighted(java.awt.Component, org.jdesktop.swingx.decorator.ComponentAdapter)
		 */
		@Override
		public boolean isHighlighted(Component arg0, ComponentAdapter arg1) {
			if ((activation == AnyPredicate && !getIncremental())
					|| (activation == ValidationPredicate && !doIncrementalValidation)
					|| (activation == MatchingPredicate && !doIncrementalMatching)
					|| (activation == EditedPredicate && !doIncrementalEditChecking)) {
				return false;
			}
			WorkbenchRow wbRow = workbench.getRow(spreadSheet.convertRowIndexToModel(arg1.row));
			WorkbenchDataItem wbCell = wbRow.getItems().get((short )spreadSheet.convertColumnIndexToModel(arg1.column));
			if (wbCell == null) {
				return false;
			}
			int status = wbCell.getEditorValidationStatus();
			if (activation == AnyPredicate) {
			    //need to do this to get cell borders
				((JLabel )arg0).setToolTipText(null);
				//Seems like a good idea to try to be as efficient as possible
				//but this will need to be recoded as new cell states are added
				return (status & WorkbenchDataItem.VAL_ERROR) != 0
					|| (status & WorkbenchDataItem.VAL_MULTIPLE_MATCH) != 0
					|| (status & WorkbenchDataItem.VAL_NEW_DATA) != 0
					|| (status & WorkbenchDataItem.VAL_NOT_MATCHED) != 0
					|| (status & WorkbenchDataItem.VAL_EDIT) != 0;
			} else {
				if (conditions != null) {
				    boolean result = false;
                    for (Short condition : conditions) {
                        if ((condition & status) != 0) {
                            ((JLabel )arg0).setToolTipText(wbCell.getStatusText());
                            result = true;
                            break;
                        }
                    }
                    if (result && antiConditions != null) {
                        for (Short condition : antiConditions) {
                            if ((condition & status) != 0) {
                                ((JLabel )arg0).setToolTipText(wbCell.getStatusText());
                                result = false;
                                break;
                            }
                        }

                    }
                    return result;
                }
			}
			return false;
		}
	}

	/**
	 * @author timo
	 *
	 */
	private class GridCellHighlighter extends AbstractHighlighter
	{
		public GridCellHighlighter(HighlightPredicate predicate)
		{
			super(predicate);
		}

		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.decorator.AbstractHighlighter#doHighlight(java.awt.Component, org.jdesktop.swingx.decorator.ComponentAdapter)
		 */
		@Override
		protected Component doHighlight(Component arg0, ComponentAdapter arg1) {
			WorkbenchRow wbRow = workbench.getRow(spreadSheet.convertRowIndexToModel(arg1.row));
			WorkbenchDataItem wbCell = wbRow.getItems().get((short )spreadSheet.convertColumnIndexToModel(arg1.column));
			cellRenderAtts.addAttributes((JLabel )arg0, wbCell, doIncrementalValidation, doIncrementalMatching, doIncrementalEditChecking);
			return arg0;
		}
	}
	
    //------------------------------------------------------------------------------------------------------
    //-- 
    //------------------------------------------------------------------------------------------------------

    class WBImageTransferable extends ImageTransferable
    {
        /* (non-Javadoc)
         * @see edu.ku.brc.specify.tasks.subpane.wb.ImageTransferable#processImages(java.util.Vector)
         */
        @Override
        protected void processImages(final Vector<File> fileList)
        {
            
            final SwingWorker worker = new SwingWorker()
            {
                protected boolean isOK = false;
                
                @Override
                public Object construct()
                {
                    // import the images into the Workbench, creating new rows (and saving the WB if it is brand new)
                    isOK = WorkbenchTask.importImages(workbench, fileList, WorkbenchPaneSS.this, false, imageImportFrame.isOneImagePerRow());
                    
                    return null;
                }

                @SuppressWarnings("synthetic-access")
                @Override
                public void finished()
                {
                    UIRegistry.clearGlassPaneMsg();
                    
                    if (isOK)
                    {
                        setChanged(true);

                        setImageFrameVisible(true);

                        // scrolls to the last row
                        newImagesAdded();
                    }
                }
            };
            worker.start();

        }
    }

    //------------------------------------------------------------------------------------------------------
    //-- 
    //------------------------------------------------------------------------------------------------------
	class WBPluginInfo
	{
        private String pluginName;
        private String className;
        private String iconName;
        private String toolTip;
        private String prefName;
        
        /**
         * @param pluginName
         * @param className
         * @param iconName
         * @param toolTip
         * @param prefName
         */
        public WBPluginInfo(final String pluginName, 
                            final String className, 
                            final String iconName, 
                            final String toolTip, 
                            final String prefName)
        {
            super();
            this.pluginName = pluginName;
            this.className = className;
            this.iconName = iconName;
            this.toolTip = toolTip;
            this.prefName = prefName;
        }
        /**
         * @return the pluginName
         */
        public String getPluginName()
        {
            return pluginName;
        }
        /**
         * @return the className
         */
        public String getClassName()
        {
            return className;
        }
        /**
         * @return the toolTip
         */
        public String getToolTip()
        {
            return toolTip;
        }
        /**
         * @return the prefName
         */
        public String getPrefName()
        {
            return prefName;
        }
        /**
         * @return the iconName
         */
        public String getIconName()
        {
            return iconName;
        }
	    
	}
}

