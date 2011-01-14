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
package edu.ku.brc.specify.tasks.subpane.wb;


import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.ConnectException;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.af.ui.forms.ResultSetControllerListener;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.StaleObjectException;
import edu.ku.brc.helpers.ImageFilter;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.services.biogeomancer.GeoCoordBGMProvider;
import edu.ku.brc.services.biogeomancer.GeoCoordDataIFace;
import edu.ku.brc.services.biogeomancer.GeoCoordGeoLocateProvider;
import edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace;
import edu.ku.brc.services.biogeomancer.GeoCoordServiceProviderIFace;
import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.services.mapping.LocalityMapper;
import edu.ku.brc.services.mapping.SimpleMapLocation;
import edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace;
import edu.ku.brc.services.mapping.LocalityMapper.MapperListener;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.dbsupport.RecordTypeCodeBuilder;
import edu.ku.brc.specify.rstools.ExportFileConfigurationFactory;
import edu.ku.brc.specify.rstools.ExportToFile;
import edu.ku.brc.specify.rstools.GoogleEarthExporter;
import edu.ku.brc.specify.rstools.WorkbenchRowPlacemarkWrapper;
import edu.ku.brc.specify.tasks.DataEntryTask;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.specify.tasks.InteractionsTask;
import edu.ku.brc.specify.tasks.PluginsTask;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.tasks.subpane.ESResultsSubPane;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.DB;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadData;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMappingDef;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTableInvalidValue;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTableMatchInfo;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadToolPanel;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploaderException;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.WorkbenchUploadMapper;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.specify.ui.LengthInputVerifier;
import edu.ku.brc.specify.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DropDownButtonStateful;
import edu.ku.brc.ui.DropDownMenuInfo;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.UnhandledExceptionDialog;
import edu.ku.brc.ui.WorkBenchPluginIFace;
import edu.ku.brc.ui.ToggleButtonChooserPanel.Type;
import edu.ku.brc.ui.dnd.SimpleGlassPane;
import edu.ku.brc.ui.tmanfe.SearchReplacePanel;
import edu.ku.brc.ui.tmanfe.SpreadSheet;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.GeoRefConverter.GeoRefFormat;

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
    
    private enum PanelType {Spreadsheet, Form}
    
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
    
    protected DropDownButtonStateful ssFormSwitcher        = null;  
    protected List<JButton>         selectionSensitiveButtons  = new Vector<JButton>();
    
    protected int                   currentRow                 = 0;
    protected FormPane              formPane;
    protected ResultSetController   resultsetController;
    
    protected CardLayout            cardLayout                 = null;
    protected JPanel                mainPanel;
    protected PanelType             currentPanelType           = PanelType.Spreadsheet;
    
    protected JSplitPane            uploadPane                 = null; 
    
    protected JPanel                controllerPane;
    protected CardLayout            cpCardLayout               = null;
    
    protected ImageFrame            imageFrame                 = null;
    protected boolean               imageFrameWasShowing       = false;
    protected ListSelectionListener workbenchRowChangeListener = null;
    
    protected JFrame                mapFrame                   = null;
    protected JLabel                mapImageLabel              = null;
    
    protected WindowListener        minMaxWindowListener       = null; 
    
    protected CustomDialog          geoRefConvertDlg           = null;
    
    protected Vector<JButton>                        workBenchPluginBtns = new Vector<JButton>();
    protected HashMap<String, WorkBenchPluginIFace>  workBenchPlugins    = new HashMap<String, WorkBenchPluginIFace>();
    
    /**
     * The currently active Uploader. 
     * static to help prevent multiple simultaneous uploads.
     */
    protected static Uploader       datasetUploader            = null; 
    protected WorkbenchValidator    workbenchValidator         = null;
    protected boolean 		        doIncrementalValidation    = AppPreferences.getLocalPrefs().getBoolean(wbAutoValidatePrefName, true);
    protected boolean	            doIncrementalMatching      = AppPreferences.getLocalPrefs().getBoolean(wbAutoMatchPrefName, false);
    protected AtomicInteger			invalidCellCount		   = new AtomicInteger(0);
    protected AtomicInteger			unmatchedCellCount		   = new AtomicInteger(0);
    protected CellRenderingAttributes cellRenderAtts           = new CellRenderingAttributes();
    
    //Single thread executor to ensure that rows are not validated concurrently as a result of batch operations
    //protected final ExecutorService validationExecutor		   = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
    protected final Queue<ValidationWorker> validationWorkerQueue = new LinkedList<ValidationWorker>();
    
    // XXX PREF
    protected int                   mapSize                    = 500;
    
    protected boolean               isReadOnly;
        
    protected AtomicInteger         shutdownLock               = new AtomicInteger(0);
    
    /**
     * Constructs the pane for the spreadsheet.
     * 
     * @param name the name of the pane
     * @param task the owning task
     * @param workbench the workbench to be edited
     * @param showImageView shows image window when first showing the window
     */
    public WorkbenchPaneSS(final String    name,
                           final Taskable  task,
                           final Workbench workbenchArg,
                           final boolean   showImageView,
                           final boolean isReadOnly)
    {
        super(name, task);
        
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
        
        Highlighter simpleStriping = HighlighterFactory.createSimpleStriping();
        GridCellHighlighter hl = new GridCellHighlighter(new GridCellPredicate(GridCellPredicate.AnyPredicate, null));
        Integer[] errs = {WorkbenchDataItem.VAL_ERROR, WorkbenchDataItem.VAL_ERROR_EDIT};
        ColorHighlighter errColorHighlighter = new ColorHighlighter(new GridCellPredicate(GridCellPredicate.ValidationPredicate, errs), 
        		cellRenderAtts.errorBackground, null);
        Integer[] newdata = {WorkbenchDataItem.VAL_NEW_DATA};
        ColorHighlighter noDataHighlighter = new ColorHighlighter(new GridCellPredicate(GridCellPredicate.MatchingPredicate, newdata), 
        		cellRenderAtts.newDataBackground, null);
        Integer[] multimatch = {WorkbenchDataItem.VAL_MULTIPLE_MATCH};
        ColorHighlighter multiMatchHighlighter = new ColorHighlighter(new GridCellPredicate(GridCellPredicate.MatchingPredicate, multimatch), 
        		cellRenderAtts.multipleMatchBackground, null);

        spreadSheet.setHighlighters(simpleStriping, hl, errColorHighlighter, noDataHighlighter, multiMatchHighlighter);
        
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

        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e)
            {
                setChanged(true);
            }
        });
        
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

        if (isReadOnly)
        {
            saveBtn = null;
        }
        else
        {
            saveBtn = createButton(getResourceString("SAVE"));
            saveBtn.setToolTipText(String.format(getResourceString("WB_SAVE_DATASET_TT"),
                    new Object[] { workbench.getName() }));
            saveBtn.setEnabled(false);
            saveBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    UsageTracker.incrUsageCount("WB.SaveDataSet");

                    UIRegistry.writeSimpleGlassPaneMsg(String.format(getResourceString("WB_SAVING"),
                            new Object[] { workbench.getName() }),
                            WorkbenchTask.GLASSPANE_FONT_SIZE);
                    UIRegistry.getStatusBar().setIndeterminate(workbench.getName(), true);
                    final SwingWorker worker = new SwingWorker()
                    {
                        @SuppressWarnings("synthetic-access")
                        @Override
                        public Object construct()
                        {
                            try
                            {
                                saveObject();

                            }
                            catch (Exception ex)
                            {
                                UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchPaneSS.class, ex);
                                log.error(ex);
                                return ex;
                            }
                            return null;
                        }

                        // Runs on the event-dispatching thread.
                        @Override
                        public void finished()
                        {
                            Object retVal = get();
                            if (retVal != null && retVal instanceof Exception)
                            {
                                Exception ex = (Exception) retVal;
                                UIRegistry.getStatusBar().setErrorMessage(
                                        getResourceString("WB_ERROR_SAVING"), ex);
                            }

                            UIRegistry.clearSimpleGlassPaneMsg();
                            UIRegistry.getStatusBar().setProgressDone(workbench.getName());
                        }
                    };
                    worker.start();

                }
            });
        }
        
        // NOTE: This needs to be done after the creation of the saveBtn
        initColumnSizes(spreadSheet, saveBtn);
        
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
        
        if (isReadOnly)
        {
            deleteRowsBtn = null;
        }
        else
        {
            deleteRowsBtn = createIconBtn("DelRec", "WB_DELETE_ROW", delAction);
            selectionSensitiveButtons.add(deleteRowsBtn);
            spreadSheet.setDeleteAction(delAction);
        }
        
        if (!isReadOnly)
        {
        	uploadToolPanel = new UploadToolPanel(this, UploadToolPanel.EXPANDED);

            showHideUploadToolBtn = createIconBtn("Interactions", IconManager.IconSize.NonStd, "WB_HIDE_UPLOADTOOLPANEL", false, new ActionListener()
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
        
        if (isReadOnly)
        {
            addRowsBtn = null;
        }
        else
        {
            addRowsBtn = createIconBtn("AddRec", "WB_ADD_ROW", addAction);
            addRowsBtn.setEnabled(true);
            addAction.setEnabled(true); 
        }

        if (isReadOnly)
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
        
        toggleImageFrameBtn = createIconBtn("CardImage", IconManager.IconSize.NonStd, "WB_SHOW_IMG_WIN", false, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                toggleImageFrameVisible();
            }
        });
        toggleImageFrameBtn.setEnabled(true);
        
        showMapBtn = createIconBtn("ShowMap", IconManager.IconSize.NonStd, "WB_SHOW_MAP", false, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showMapOfSelectedRecords();
            }
        });
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
        
        if (!isReadOnly)
        {
            // Will come from XML
            //createPlugin("edu.ku.brc.specify.plugins.sgr.SGRPluginImpl", "SGR", "WB_SHOW_IN_GOOGLE_EARTH");
        }
        
        
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
                                doGeoRef(new GeoCoordGeoLocateProvider(), "WB.GeoLocateRows");
                            }
                            else
                            {
                                doGeoRef(new GeoCoordBGMProvider(), "WB.BioGeomancerRows");
                            }
                        }
                    });
            // only enable it if the workbench has the proper columns in it
            String[] missingColumnsForBG = getMissingButRequiredColumnsForBioGeomancer();
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
            String[] missingGeoRefFields = getMissingGeoRefFields();
            if (missingGeoRefFields.length > 0)
            {
                convertGeoRefFormatBtn.setEnabled(false);
                exportKmlBtn.setEnabled(false);
                showMapBtn.setEnabled(false);

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
                String origTT3 = showMapBtn.getToolTipText();
                showMapBtn.setToolTipText("<html>" + origTT3 + ttText);
            }
            else
            {
                convertGeoRefFormatBtn.setEnabled(true);
                exportKmlBtn.setEnabled(true);
                showMapBtn.setEnabled(true);
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
        uploadDatasetBtn.setVisible(isUploadPermitted() && !UIRegistry.isMobile());
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
        
        
        for (int c = 0; c < spreadSheet.getTableHeader().getColumnModel().getColumnCount(); c++)
		{
			// TableColumn column =
			// spreadSheet.getTableHeader().getColumnModel().getColumn(spreadSheet.getTableHeader().getColumnModel().getColumnCount()-1);
			TableColumn column = spreadSheet.getTableHeader().getColumnModel().getColumn(c);
			column.setCellRenderer(new WbCellRenderer());
		}

        // setup the JFrame to show images attached to WorkbenchRows
        imageFrame = new ImageFrame(mapSize, this, this.workbench, (WorkbenchTask)task, isReadOnly);
        
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

        JComponent[] compsArray = {addRowsBtn, deleteRowsBtn, clearCellsBtn, showMapBtn, exportKmlBtn, 
                                   geoRefToolBtn, convertGeoRefFormatBtn, exportExcelCsvBtn, uploadDatasetBtn, showHideUploadToolBtn};
        Vector<JComponent> availableComps = new Vector<JComponent>(compsArray.length + workBenchPluginBtns.size());
        for (JComponent c : compsArray)
        {
            if (c != null)
            {
                availableComps.add(c);
            }
        }
        for (JComponent c : workBenchPluginBtns)
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
        
        // Create the Form Pane
        formPane = new FormPane(this, workbench, isReadOnly);
        
        // This panel contains just the ResultSetContoller, it's needed so the RSC gets centered
        PanelBuilder rsPanel = new PanelBuilder(new FormLayout("c:p:g", "c:p:g"));
        FormValidator dummy = new FormValidator(null);
        dummy.setEnabled(true);
        resultsetController  = new ResultSetController(dummy, !isReadOnly, !isReadOnly, false, getResourceString("Record"), model.getRowCount(), true);
        resultsetController.addListener(formPane);
        if (!isReadOnly)
        {
            resultsetController.getDelRecBtn().addActionListener(delAction);
        }
//        else
//        {
//            resultsetController.getDelRecBtn().setVisible(false);
//        }
        rsPanel.add(resultsetController.getPanel(), cc.xy(1,1));
        
        // This panel is a single row containing the ResultSetContoller and the other controls for the Form Panel  
        PanelBuilder resultSetPanel = new PanelBuilder(new FormLayout("f:p:g, p, f:p:g, p", "c:p:g"));
        // Now put the two panel into the single row panel
        resultSetPanel.add(rsPanel.getPanel(), cc.xy(2,1));
        if (!isReadOnly)
        {
            resultSetPanel.add(formPane.getControlPropsBtn(), cc.xy(4,1));
        }
        
        // Create the main panel that uses card layout for the form and spreasheet
        mainPanel = new JPanel(cardLayout = new CardLayout());
        
        // Add the Form and Spreadsheet to the CardLayout
        mainPanel.add(spreadSheet.getScrollPane(), PanelType.Spreadsheet.toString());
        mainPanel.add(formPane.getScrollPane(),    PanelType.Form.toString());
        
        // The controllerPane is a CardLayout that switches between the Spreadsheet control bar and the Form Control Bar
        controllerPane = new JPanel(cpCardLayout = new CardLayout());
        controllerPane.add(spreadSheetControlBar.getPanel(), PanelType.Spreadsheet.toString());
        controllerPane.add(resultSetPanel.getPanel(),        PanelType.Form.toString());
   
        JLabel sep1 = new JLabel(IconManager.getIcon("Separator"));
        JLabel sep2 = new JLabel(IconManager.getIcon("Separator"));
        ssFormSwitcher = createSwitcher();
        
        // This works
        setLayout(new BorderLayout());
        JComponent[] ctrlCompArray = {toggleImageFrameBtn, carryForwardBtn, sep1, saveBtn, sep2, ssFormSwitcher};
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
        
        //PanelBuilder    ctrlBtns   = new PanelBuilder(new FormLayout("p,4px,p,6px,6px,6px,p,7px,6px,p", "c:p:g"));
//        ctrlBtns.add(toggleImageFrameBtn, cc.xy(1,1));
//        ctrlBtns.add(carryForwardBtn,     cc.xy(3,1));
//        ctrlBtns.add(sep1,                cc.xy(5,1));
//        ctrlBtns.add(saveBtn,             cc.xy(7,1));
//        ctrlBtns.add(sep2,                cc.xy(9,1));
//        ctrlBtns.add(ssFormSwitcher,    cc.xy(10,1));
        
        add(mainPanel, BorderLayout.CENTER);
        
        FormLayout      formLayout = new FormLayout("f:p:g,4px,p", "2px,f:p:g,p:g,p:g");
        PanelBuilder    builder    = new PanelBuilder(formLayout);

        builder.add(controllerPane,      cc.xy(1,2));
        builder.add(ctrlBtns.getPanel(), cc.xy(3,2));
        
        builder.add(uploadToolPanel,     cc.xywh(1, 3, 3, 1));
        builder.add(findPanel,           cc.xywh(1, 4, 3, 1));


        add(builder.getPanel(), BorderLayout.SOUTH);
        
        // See if we need to make the Image Frame visible
        // Commenting this out for now because it is so annoying.
        
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
        
        resultsetController.addListener(new ResultSetControllerListener() {
            public boolean indexAboutToChange(int oldIndex, int newIndex)
            {
                return true;
            }
            public void indexChanged(int newIndex)
            {
                if (imageFrame != null)
                {
                    imageFrame.setRow(newIndex > -1 ? workbench.getRow(newIndex) : null);
                }
            }
            public void newRecordAdded()
            {
                // do nothing
            }
        });
        //compareSchemas();
        if (getIncremental())
        {
        	buildValidator();
        }
    }
    
    /**
     * @return true if automatch or autovalidate is on
     */
    protected boolean getIncremental()
    {
    	return doIncrementalValidation || doIncrementalMatching;
    	
    }
    
    protected void showUploadToolPanel()
    {
    	uploadToolPanel.expand();
    }
    
    protected void hideUploadToolPanel()
    {
    	uploadToolPanel.contract();
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
		validateAll(null);
		
		AppPreferences.getLocalPrefs().putBoolean(wbAutoValidatePrefName, doIncrementalValidation);
	}

	/**
	 * @return the doIncrementalMatching
	 */
	public boolean isDoIncrementalMatching() 
	{
		return doIncrementalMatching;
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
		validateAll(null);

		AppPreferences.getLocalPrefs().putBoolean(wbAutoMatchPrefName, doIncrementalMatching);
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
        } else
        {
            if (formPane != null)
            {
                formPane.copyDataFromForm();
            }
        }
        return isOK;
    }
    
    
    /**
     * @param isNext
     * @return
     */
    protected Pair<Integer, Integer> getNextCellWithStat(boolean isNext, Set<Short> stats)
    {
    	if (!isNext)
    	{
    		//System.out.println("goToInvalidCell prev");
    	} else
    	{
    		//System.out.println("goToInvalidCell next");
    	}
    	
    	int startRow = spreadSheet.getSelectedRow() >= 0 ? spreadSheet.getSelectedRow() : 0;
    	int startCol = spreadSheet.getSelectedColumn() >= 0 ? spreadSheet.getSelectedColumn() : 0;
    	int increment = isNext ? 1 : -1;
    	int currentRow = startRow;
    	int currentCol = startCol + increment;
    	if (currentCol < 0 || currentCol == spreadSheet.getColumnCount())
    	{
    		if (currentCol < 0)
    		{
    			currentCol = spreadSheet.getColumnCount() - 1; //XXX what about attachment column?
    		} else
    		{
    			currentCol = 0;
    		}
    		currentRow += increment;
    		if (currentRow < 0)
    		{
    			currentRow = spreadSheet.getRowCount() - 1 ;
    		} else if (currentRow == spreadSheet.getRowCount())
    		{
    			currentRow = 0;
    		}  
    	}
    	boolean lastRow = false;
    	do
    	{
    		Hashtable<Short, WorkbenchDataItem> rowItems = workbench.getRow(spreadSheet.convertRowIndexToModel(currentRow)).getItems();
    		do 
    		{
    	    	WorkbenchDataItem di = rowItems.get(new Short((short )spreadSheet.convertColumnIndexToModel(currentCol)));
        	    if (di != null && stats.contains(new Short((short )di.getEditorValidationStatus())))
    	    	{
    	    		return new Pair<Integer, Integer>(currentRow, currentCol);
    	    	}		
    			currentCol += increment;
    			
    		} while (currentCol >= 0 && currentCol < spreadSheet.getColumnCount() && (!lastRow || currentCol != startCol));
	    	if (currentCol < 0)
	    	{
	    		currentCol = spreadSheet.getColumnCount() - 1; //XXX what about attachment column?
	    	} else if (currentCol == spreadSheet.getColumnCount())
	    	{
	    		currentCol = 0;
	    	}
    		
    		if (!lastRow)
    		{
    			currentRow += increment;
    			if (currentRow < 0)
    			{
    				currentRow = spreadSheet.getRowCount() - 1 ;
    			} else if (currentRow == spreadSheet.getRowCount())
    			{
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
    public void goToInvalidCell(boolean isNext)
    {
    	Set<Short> stats = new HashSet<Short>();
    	stats.add(WorkbenchDataItem.VAL_ERROR);
    	stats.add(WorkbenchDataItem.VAL_ERROR_EDIT);
    	Pair<Integer, Integer> invalidCell = getNextCellWithStat(isNext, stats);
    	if (invalidCell != null)
    	{
            if (spreadSheet.getCellEditor() != null)
            {
                spreadSheet.getCellEditor().stopCellEditing();
            }
            int row = invalidCell.getFirst();
            int col = invalidCell.getSecond();
            spreadSheet.getSelectionModel().setSelectionInterval(row, row);
            spreadSheet.getColumnModel().getSelectionModel().setSelectionInterval(col, col);
            spreadSheet.scrollCellToVisible(row, col);
            //spreadSheet.editCellAt(invalidCell.getFirst(), invalidCell.getSecond());
    	}
    }
    

    /**
     * @param isNext
     */
    public void goToUnmatchedCell(boolean isNext)
    {
    	Set<Short> stats = new HashSet<Short>();
    	stats.add(WorkbenchDataItem.VAL_MULTIPLE_MATCH);
    	stats.add(WorkbenchDataItem.VAL_NEW_DATA);
    	Pair<Integer, Integer> invalidCell = getNextCellWithStat(isNext, stats);
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
        if (!isReadOnly)
        {
            addRowsBtn.setEnabled(enable);
        }
        if (!isReadOnly)
        {
            resultsetController.getNewRecBtn().setEnabled(enable && !isReadOnly);
        }
        
        uploadToolPanel.updateBtnUI();
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
            log.debug("rows "+spreadSheet.getRowCount()+" "+spreadSheet.getSelectedRow());
            spreadSheet.scrollToRow(rowIndex);
            spreadSheet.setRowSelectionInterval(rowIndex, rowIndex);
            spreadSheet.setColumnSelectionInterval(0, spreadSheet.getColumnCount()-1);
            spreadSheet.repaint();
            log.debug("rows "+spreadSheet.getRowCount()+" "+spreadSheet.getSelectedRow());
            
        } else
        {
            resultsetController.setIndex(rowIndex);
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
            rows[0] = resultsetController.getCurrentIndex();
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
        	
        model.deleteRows(rows);

        int rowCount = workbench.getWorkbenchRowsAsList().size();
        
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
            resultsetController.setLength(rowCount);
            if (firstRow >= rowCount)
            {
                resultsetController.setIndex(rowCount-1);
            } else
            {
                resultsetController.setIndex(firstRow);
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
    protected void showCardImageForSelectedRow()
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
        WorkbenchRow row = workbench.getWorkbenchRowsAsList().get(selectedIndex);
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
            public void actionPerformed(ActionEvent ae)
            {
                PanelType panel = switcher.getCurrentIndex() == 1 ? PanelType.Spreadsheet : PanelType.Form;
            	showPanel(panel);
            	
            	//Until auto-validation is hooked up to form view:
//            	if (panel == PanelType.Spreadsheet && doIncrementalValidation)
//            	{
//            		validateAll(null);
//            	}
            	
            	if (panel == PanelType.Form && getIncremental())
            	{
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
            if (selectedInx > -1)
            {
                selectedInx = spreadSheet.convertRowIndexToModel(selectedInx);
            }
        } else
        {
            selectedInx = resultsetController.getCurrentIndex();
        }
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
     * @param value the panel number
     */
    public void showPanel(final PanelType panelType)
    {
        checkCurrentEditState();
        
        currentRow = getCurrentIndexFromFormOrSS();
        
        currentPanelType = panelType;
        
        cardLayout.show(mainPanel, currentPanelType.toString());
        cpCardLayout.show(controllerPane, currentPanelType.toString());
        
        boolean isSpreadsheet = currentPanelType == PanelType.Spreadsheet;
        if (isSpreadsheet)
        {
            formPane.aboutToShowHide(false);
            
            // Showing Spreadsheet and hiding form
            if (model.getRowCount() > 0)
            {
                spreadSheet.setRowSelectionInterval(currentRow, currentRow);
                spreadSheet.setColumnSelectionInterval(0, spreadSheet.getColumnCount()-1);
                spreadSheet.scrollToRow(Math.min(currentRow+4, model.getRowCount()));
                
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {            
                        spreadSheet.requestFocus();
                    }
                });
            }
            // Enable the "Find" action in the Edit menu when a spreadsheet is shown
            UIRegistry.enableFind(findPanel, true);

        } else
        {
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
        }
        
             
        JComponent[] comps = { addRowsBtn, deleteRowsBtn, clearCellsBtn};
        for (JComponent c : comps)
        {
            if (c != null)
            {
                c.setVisible(isSpreadsheet);
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
    public void setImageFrameVisible(boolean visible)
    {
        if (visible == imageFrame.isVisible())
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
            // hide the image window
            
            // turn off alwaysOnTop for Swing repaint reasons (prevents a lock up)
            if (imageFrame.isAlwaysOnTop())
            {
                imageFrame.setAlwaysOnTop(false);
            }
            // if the image frame is minimized or iconified, set it to fully visible before doing anything else
            if (imageFrame.getState() == Frame.ICONIFIED)
            {
                imageFrame.setState(Frame.NORMAL);
            }
            toggleImageFrameBtn.setToolTipText(getResourceString("WB_SHOW_IMG_WIN"));

            spreadSheet.getSelectionModel().removeListSelectionListener(workbenchRowChangeListener);
            
            // set the image window and the image column invisible
            imageFrame.setVisible(false);
            imageColExt.setVisible(false);
        }
        else
        {
            // show the image window
            
            UIHelper.positionFrameRelativeToTopFrame(imageFrame);
            
            // when a user hits the "show image" button, for some reason the selection gets nullified
            // so we'll grab it here, then set it at the end of this method

            toggleImageFrameBtn.setToolTipText(getResourceString("WB_HIDE_IMG_WIN"));
            spreadSheet.getSelectionModel().addListSelectionListener(workbenchRowChangeListener);
            HelpMgr.setHelpID(this, "WorkbenchWorkingWithImages");
            
            // set the image window and the image column visible
            imageFrame.setVisible(true);
            imageColExt.setVisible(true);

            // if the image frame is minimized or iconified, set it to fully visible before doing anything else
            if (imageFrame.getState() == Frame.ICONIFIED)
            {
                imageFrame.setState(Frame.NORMAL);
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
					.getColumn(
							spreadSheet.getTableHeader().getColumnModel()
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
            }
            
            @Override
            protected void okButtonPressed()
            {
                checkCurrentEditState();

                // figure out which rows the user is working with
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
                
                // since Arrays.copyOf() isn't in Java SE 5...
                int[] selRows = new int[selection.length];
                for (int i = 0; i < selection.length; ++i)
                {
                    selRows[i] = selection[i];
                }


                // don't call super.okButtonPressed() b/c it will close the window
                isCancelled = false;
                btnPressed  = OK_BTN;
                LatLonConverter.DEGREES_FORMAT degFmt = symbolCkBx.isSelected() ?
                		LatLonConverter.DEGREES_FORMAT.Symbol :
                		LatLonConverter.DEGREES_FORMAT.None;
                Vector<CellPosition> unconverted = new Vector<CellPosition>();
                switch( toggle.getSelectedIndex() )
                {
                    case 0:
                    {
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
                    case 1:
                    {
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
                    case 2:
                    {
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
                    case 3:
                    {
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
                    case 4:
                    {
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
                    case 5:
                    {
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
                if (unconverted.size() != 0 )
                {
                	UIRegistry.displayLocalizedStatusBarError("WB_UNCONVERTED_GEOREFS", unconverted.size());
                	final JList unconvertedcells = UIHelper.createList(unconverted);
                	unconvertedcells.addListSelectionListener(new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent arg0) {
							CellPosition rowCol = (CellPosition )unconvertedcells.getSelectedValue();
							spreadSheet.scrollCellToVisible(rowCol.getFirst(), rowCol.getSecond());
							
						}
                		
                	});
                	JLabel lbl = UIHelper.createLabel(UIRegistry.getResourceString("WB_UNCONVERTED_GEOREFS_MSG"));
                	JPanel innerPane = new JPanel(new BorderLayout());
                	innerPane.add(lbl, BorderLayout.NORTH);
                	innerPane.add(unconvertedcells, BorderLayout.CENTER);
                	CustomDialog cd = new CustomDialog((Frame )UIRegistry.getTopWindow(), UIRegistry.getResourceString("WB_UNCONVERTED_GEOREFS_TITLE"),
                			false, CustomDialog.OKHELP, innerPane);
                	cd.setHelpContext("UnconvertableGeoCoords");
                	UIHelper.centerAndShow(cd);
                }
            }
        };
        geoRefConvertDlg.setModal(false);
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
        showMapBtn.setEnabled(false);
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
        
        DBTableIdMgr databaseSchema = WorkbenchTask.getDatabaseSchema();
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
            showMapBtn.setEnabled(true);
            
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
        List<CellPosition> unconverted = new Vector<CellPosition>();
        
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
            
            model.setValueAt(convertedValue, rowIndex, columnIndex, !(converter instanceof GeoRefConverter));
            if (!currentValue.equals(convertedValue))
            {
                setChanged(true);
            }
        }
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
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
            }
        });
        return unconverted;
    }
    
    /**
     * Export to XLS .
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

        if (!((WorkbenchTask) task).getExportInfo(props, workbench.getName()))
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
            if (viewOrder == -1)
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
        int localityTableId = DBTableIdMgr.getInstance().getIdByClassName(Locality.class.getName());
        if (workbench.getColumnIndex(localityTableId, "localityName") == -1 || // I18N
            workbench.getColumnIndex(localityTableId, "latitude1") == -1 ||
            workbench.getColumnIndex(localityTableId, "longitude1") == -1)
        {
            return false;
        }
        
        // look for the geography fields
        int geographyTableId = DBTableIdMgr.getInstance().getIdByClassName(Geography.class.getName());
        if (workbench.getColumnIndex(geographyTableId, "Country") == -1 || // I18N
            workbench.getColumnIndex(geographyTableId, "State") == -1 ||
            workbench.getColumnIndex(geographyTableId, "County") == -1)
        {
            return false;
        }
        
        return true;
    }
    
    protected String[] getMissingButRequiredColumnsForBioGeomancer()
    {
        List<String> missingCols = new Vector<String>();
        
        // check the locality fields
        int localityTableId = DBTableIdMgr.getInstance().getIdByClassName(Locality.class.getName());
        
        if (workbench.getColumnIndex(localityTableId, "localityName") == -1)
        {
            missingCols.add("Locality Name");  // i18n
        }
        if (workbench.getColumnIndex(localityTableId, "latitude1") == -1)
        {
            missingCols.add("Latitude 1"); // i18n
        }
        if (workbench.getColumnIndex(localityTableId, "longitude1") == -1)
        {
            missingCols.add("Longitude 1"); // i18n
        }
        
        // check the geography fields
        int geographyTableId = DBTableIdMgr.getInstance().getIdByClassName(Geography.class.getName());

        if (workbench.getColumnIndex(geographyTableId, "country") == -1)
        {
            missingCols.add("Country"); // i18n
        }
        if (workbench.getColumnIndex(geographyTableId, "state") == -1)
        {
            missingCols.add("State"); // i18n
        }
        if (workbench.getColumnIndex(geographyTableId, "county") == -1)
        {
            missingCols.add("County"); // i18n
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
    
    protected String[] getMissingGeoRefFields()
    {
        List<String> missingCols = new Vector<String>();
        
        // check the locality fields
        int localityTableId = DBTableIdMgr.getInstance().getIdByClassName(Locality.class.getName());
        
        if (workbench.getColumnIndex(localityTableId, "latitude1") == -1)
        {
            missingCols.add("Latitude 1"); // i18n
        }
        if (workbench.getColumnIndex(localityTableId, "longitude1") == -1)
        {
            missingCols.add("Longitude 1"); // i18n
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
     * @return the selected rows form the workbench
     */
    protected List<GeoCoordDataIFace> getSelectedRowsFromViewForGeoRef()
    {
        return new Vector<GeoCoordDataIFace>(getSelectedRows());
    }
    
    /**
     * @return
     */
    protected List<WorkbenchRow> getSelectedRows()
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
                            final String trackId)
    {
        UsageTracker.incrUsageCount(trackId);
        log.info("Performing GeoREflookup of selected records: "+ trackId);

        if (true)
        {
            List<GeoCoordDataIFace> selectedWBRows = getSelectedRowsFromViewForGeoRef();
            if (selectedWBRows != null)
            {
                geoRefService.processGeoRefData(selectedWBRows, new GeoCoordProviderListenerIFace()
                {
                    public void aboutToDisplayResults()
                    {
                        if (imageFrame != null)
                        {
                            imageFrame.setAlwaysOnTop(false);
                        }
                    }
                    
                    public void complete(final List<GeoCoordDataIFace> items, final int itemsUpdated)
                    {
                        if (itemsUpdated > 0)
                        {
                            setChanged(true);
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
     * Set that there has been a change.
     * 
     * @param changed true or false
     */
    public void setChanged(final boolean changed)
    {
        if (!blockChanges)
        {
            hasChanged = changed;
            saveBtn.setEnabled(hasChanged);
            updateUploadBtnState();
        }
    }
    
    /**
     * @return whether there has been a change.
     */
    public boolean isChanged()
    {
        return hasChanged;
    }

    /**
     * Adjust all the column width for the data in the column, this may be handles with JDK 1.6 (6.)
     * @param tableArg the table that should have it's columns adjusted
     */
    private void initColumnSizes(final JTable tableArg, final JButton theSaveBtn) 
    {
        TableModel  tblModel    = tableArg.getModel();
        TableColumn column      = null;
        Component   comp        = null;
        int         headerWidth = 0;
        int         cellWidth   = 0;
        
        TableCellRenderer headerRenderer = tableArg.getTableHeader().getDefaultRenderer();

        
        //UIRegistry.getInstance().hookUpUndoableEditListener(cellEditor);
        
        Vector<WorkbenchTemplateMappingItem> wbtmis = new Vector<WorkbenchTemplateMappingItem>();
        wbtmis.addAll(workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems());
        Collections.sort(wbtmis);
        
        DBTableIdMgr databaseSchema = WorkbenchTask.getDatabaseSchema();
        
        columnMaxWidths = new Integer[tableArg.getColumnCount()];
        for (int i = 0; i < tableArg.getColumnCount(); i++) 
        {
            WorkbenchTemplateMappingItem wbtmi = wbtmis.elementAt(i);
            
            // Now go retrieve the data length
            int fieldWidth = WorkbenchDataItem.cellDataLength;
            DBTableInfo ti = databaseSchema.getInfoById(wbtmi.getSrcTableId());
            if (ti != null)
            {
                DBFieldInfo fi = ti.getFieldByName(wbtmi.getFieldName());
                if (fi != null)
                {
                    wbtmi.setFieldInfo(fi);
                    //System.out.println(fi.getName()+"  "+fi.getLength()+"  "+fi.getType());
                    if (RecordTypeCodeBuilder.getTypeCode(fi) == null && fi.getLength() > 0)
                    {
                        fieldWidth = Math.min(fi.getLength(), WorkbenchDataItem.cellDataLength);
                    }
                } else
                {
                    log.error("Can't find field with name ["+wbtmi.getFieldName()+"]");
                }
            } else
            {
                log.error("Can't find table ["+wbtmi.getSrcTableId()+"]");
            }
            columnMaxWidths[i] = new Integer(fieldWidth);
            GridCellEditor cellEditor = getCellEditor(wbtmi, fieldWidth, theSaveBtn);
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
        }
        //tableArg.setCellEditor(cellEditor);
    }
    
    /**
     * @param wbtmi
     * @return
     */
    protected GridCellEditor getCellEditor(WorkbenchTemplateMappingItem wbtmi, int fieldWidth, JButton theSaveBtn)
    {
    	PickListDBAdapterIFace pickList = null;
    	//XXX wbtmi.getFieldInfo() doesn't work -- returned object's pickList is always null???
    	DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoByTableName(wbtmi.getTableName());
    	if (tblInfo != null)
    	{
    		DBFieldInfo fldInfo = tblInfo.getFieldByName(wbtmi.getFieldName());
    		if (fldInfo != null)
    		{
    			if (!StringUtils.isEmpty(fldInfo.getPickListName()))
    			{
    				pickList = PickListDBAdapterFactory.getInstance().create(
    						fldInfo.getPickListName(), false);
    			} else if (RecordTypeCodeBuilder.isTypeCodeField(fldInfo))
    			{
    				pickList = RecordTypeCodeBuilder.getTypeCode(fldInfo);
    			}
    		}
    	}
     	if (pickList == null)
    	{
    		return new GridCellEditor(new JTextField(), wbtmi.getCaption(), fieldWidth, theSaveBtn);	
    	}
    	JComboBox comboBox = new JComboBox(pickList.getList());
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
        WorkbenchBackupMgr.backupWorkbench(workbench, (WorkbenchTask) task);
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
        logDebug("backupObject(): " + System.nanoTime());
        backupObject();
        logDebug("---------" + System.nanoTime());
        
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
            formPane.setWorkbench(workbench);
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
        if (saveBtn != null)
        {
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
        
        if (datasetUploader != null && !datasetUploader.aboutToShutdown(this))
        {
            return false;
        }

        if (datasetUploader != null)
        {
            if (datasetUploader.closing(this))
            {
                datasetUploader = null;
                Uploader.unlockApp();
                Uploader.unlockUpload();
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
            String msg = String.format(getResourceString("SaveChanges"), getTitle());
            JFrame topFrame = (JFrame)UIRegistry.getTopWindow();

            final String wbName = workbench.getName();
            
            int rv = JOptionPane.showConfirmDialog(topFrame,
                                                   msg,
                                                   getResourceString("SaveChangesTitle"),
                                                   JOptionPane.YES_NO_CANCEL_OPTION);
            if (rv == JOptionPane.YES_OPTION)
            {
                //GlassPane and Progress bar currently don't show up during shutdown
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        UIRegistry.writeSimpleGlassPaneMsg(String.format(
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
                        UIRegistry.clearSimpleGlassPaneMsg();
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
            ((WorkbenchTask)task).closing(this);
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
        //Check to see if background tasks accessing the Workbanch are active.
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
        
        if (resultsetController != null)
        {
            resultsetController.removeListener(formPane);
            resultsetController = null;
        }
        
        if (formPane != null)
        {
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
        
        super.shutdown();
    }

    
    /**
     * 
     */
    protected void shutdownValidators()
    {
        //validationExecutor.shutdownNow();
        if (validationWorkerQueue.peek() != null)
        {
        	//System.out.println("Shutdown: Cancelling validation worker.");
        	ValidationWorker vw = null;
        	synchronized(validationWorkerQueue)
        	{
        		vw = validationWorkerQueue.peek();
        		validationWorkerQueue.clear();
        	}
        	if (vw != null && !vw.isDone())
        	{
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
        if (formPane != null)
        {
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
        else
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
    
    /**
     * @return  a list of open panes that prohibit uploading.
     */
    protected List<SubPaneIFace> checkOpenTasksForUpload()
    {
        List<SubPaneIFace> result = new LinkedList<SubPaneIFace>();
        for (SubPaneIFace pane : SubPaneMgr.getInstance().getSubPanes())
        {
            if (prohibitsUpload(pane))
            {
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
    protected boolean prohibitsUpload(final SubPaneIFace pane)
    {
        if (pane.getTask().getClass().equals(DataEntryTask.class))
        {
            return true;
        }
        if (pane.getTask().getClass().equals(InteractionsTask.class))
        {
            return true;
        }
        if (pane instanceof ESResultsSubPane)
        {
        	return true;
        }
        return false;
    }
    
    /**
     * @param badPanes
     * @return a list of the distinct tasks represented by badPanes 
     */
    protected String getListOfBadTasks(final List<SubPaneIFace> badPanes)
    {
        Set<Taskable> badTasks = new HashSet<Taskable>();
        for (SubPaneIFace pane : badPanes)
        {
            badTasks.add(pane.getTask());
        }
        String result = "";
        Iterator<Taskable> badI= badTasks.iterator();
        while (badI.hasNext())
        {
            Taskable badTask = badI.next();
            if (!StringUtils.isBlank(result))
            {
                if (badI.hasNext())
                {
                    result += ", ";
                }
                else
                {
                    result += " or ";
                }
            }
            if (badTask instanceof ExpressSearchTask)
            {
            	result += UIRegistry.getResourceString("WorkbenchPaneSS.SearchResult");
            }
            else
            {
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
    	try
    	{
    		workbenchValidator = new WorkbenchValidator(this);
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
    			if (wvEx != null && wvEx.getStructureErrors().size() > 0)
    			{
    				showStructureErrors(wvEx.getStructureErrors());
    			}
    		}
    		else {
    			ex.printStackTrace();
    		}
    		UIRegistry.showLocalizedError("WorkbenchPaneSS.UnableToAutoValidate");
    		uploadToolPanel.turnOffSelections();
//    		this.autoValidateChk.setSelected(false);
//    		this.autoMatchChk.setSelected(false);
    		turnOffIncrementalValidation();
    		turnOffIncrementalMatching();
			workbenchValidator = null;
			model.fireDataChanged();
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
			workbenchValidator = null;
			model.fireDataChanged();
			AppPreferences.getLocalPrefs().putBoolean(wbAutoValidatePrefName, doIncrementalValidation);
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
			workbenchValidator = null;
			model.fireDataChanged();
			AppPreferences.getLocalPrefs().putBoolean(wbAutoMatchPrefName, doIncrementalMatching);
		} finally
		{
			blockChanges = savedBlockChanges;
		}
    }

    /**
     * @param structureErrors
     * 
     * Display a dialog listing the 'structural' problems with the dataset
     * that prevent uploading.
     */
    protected void showStructureErrors(Vector<UploadMessage> structureErrors)
    {
        JPanel pane = new JPanel(new BorderLayout());
        JLabel lbl = createLabel(getResourceString("WB_UPLOAD_BAD_STRUCTURE_MSG") + ":");
        lbl.setBorder(new EmptyBorder(3, 1, 2, 0));
        pane.add(lbl, BorderLayout.NORTH);
        JPanel lstPane = new JPanel(new BorderLayout());
        JList lst = new JList(structureErrors);
        lst.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        lstPane.setBorder(new EmptyBorder(1, 1, 10, 1));
        lstPane.add(lst, BorderLayout.CENTER);
        pane.add(lstPane, BorderLayout.CENTER);
        CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(),
                getResourceString("WB_UPLOAD_BAD_STRUCTURE_DLG"),
                true,
                CustomDialog.OKHELP,
                pane);
        UIHelper.centerAndShow(dlg);
        dlg.dispose();
    }
    
    protected void doDatasetUpload()
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
            if (!UIRegistry.displayConfirm(UIRegistry.getResourceString("WB_UPLOAD_CLOSE_ALL_TITLE"), 
                    String.format(getResourceString("WB_UPLOAD_CLOSE_ALL_MSG"), getListOfBadTasks(badPanes)), 
                    getResourceString("OK"), getResourceString("CANCEL"), JOptionPane.QUESTION_MESSAGE))
            {
                return;
            }
            for (SubPaneIFace badPane : badPanes)
            {
                if (!SubPaneMgr.getInstance().removePane(badPane, true))
                {
                    return;
                }
            }
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
            
            CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(),
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
            CustomDialog dlg2 = new CustomDialog((Frame)UIRegistry.getTopWindow(),
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
            if (Uploader.lockUpload(null, true) != Uploader.LOCKED)
            {
                return;
            }
            Uploader.lockApp();
            spreadSheet.clearSorter();
            datasetUploader = new Uploader(db, new UploadData(maps, workbench.getWorkbenchRowsAsList()), this, false);
            Vector<UploadMessage> structureErrors = datasetUploader.verifyUploadability();
            if (structureErrors.size() > 0) 
            { 
                showStructureErrors(structureErrors);
                uploadDone();
                return;
            }
            if (!datasetUploader.setAdditionalLocks())
            {
            	uploadDone();
            	return;
            }
            uploadPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spreadSheet
					.getScrollPane(), datasetUploader.getMainPanel());
			mainPanel.remove(spreadSheet.getScrollPane());
			uploadPane.setOneTouchExpandable(true);
			uploadPane.setDividerLocation(spreadSheet.getScrollPane()
					.getHeight() * 2 / 3);
			spreadSheet.getScrollPane().setVisible(true);

			// Provide minimum sizes for the two components in the split pane
			Dimension minimumSize = new Dimension(200, 200);
			datasetUploader.getMainPanel().setMinimumSize(minimumSize);
			spreadSheet.getScrollPane().setMinimumSize(minimumSize);
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
			if (imageFrame != null && imageFrame.isVisible())
			{
				imageFrame.setVisible(false);
			}
			datasetUploader.startUI();
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
    
    /**
     * Removes uploader ui and redisplays standard wb ui
     */
    public void uploadDone()
    {
        datasetUploader = null;
        Uploader.unlockApp();
        if (!Uploader.unlockUpload())
        {
            log.error("unable to unlock upload task semaphore.");
            //inform the user??
        }
        if (uploadPane != null)
        {
            List<SubPaneIFace> badPanes = checkOpenTasksForUploadClose();
            if (badPanes.size() > 0)
            {
                for (SubPaneIFace badPane : badPanes)
                {
                    if (!SubPaneMgr.getInstance().removePane(badPane, true))
                    {
                        log.error("unable to close " + badPane.getClass().getName() + " after uploader close.");
                    }
                }
            }
        	
        	mainPanel.remove(uploadPane);
            mainPanel.add(spreadSheet.getScrollPane(), PanelType.Spreadsheet.toString());
            showPanel(PanelType.Spreadsheet);
            mainPanel.validate();
            mainPanel.doLayout();
        }
        ssFormSwitcher.setEnabled(true);
        spreadSheet.setEnabled(true);
        setToolBarBtnsEnabled(true);
        setAllUploadDatasetBtnEnabled(true);
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
    	boolean missingGeoRefFlds = getMissingGeoRefFields().length > 0;
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
    	
    	for (JButton btn : workBenchPluginBtns)
    	{
    	    btn.setEnabled(enabled);
    	}
    }
    
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
           uploadDatasetBtn.setEnabled(!hasChanged);
           if (uploadDatasetBtn.isEnabled())
           {
               uploadDatasetBtn.setToolTipText(getResourceString("WB_UPLOAD_DATA"));
           }
           else
           {
               uploadDatasetBtn.setToolTipText(getResourceString("WB_UPLOAD_UNSAVED_CHANGES_HINT"));
           }
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
    protected Hashtable<Short, Short> updateCellStatuses(List<CellStatusInfo> stats, final WorkbenchRow wbRow)
    {
    	Hashtable<Short, Short> exceptionalItems = new Hashtable<Short, Short>();
		if (stats != null && stats.size() > 0)
		{
			for (CellStatusInfo issue : stats)
			{
				for (Integer col : issue.getColumns())
				{
					WorkbenchDataItem wbItem = wbRow.getItems().get(col.shortValue());
					if (wbItem == null)
					{
						//need to force creation of empty wbItem for blank cell
						wbItem = wbRow.setData("", col.shortValue(), false, true);
					}
					if (wbItem != null)
					{
						exceptionalItems.put(col.shortValue(), issue.getStatus());
						//WorkbenchDataItems can be updated by GridCellEditor or by background validation initiated at load time or after find/replace ops			
						synchronized(wbItem)
						{
							if (wbItem.getEditorValidationStatus() != issue.getStatus())
							{
								wbItem.setEditorValidationStatus(issue.getStatus());
								wbItem.setStatusText(issue.getStatusText());
								if (issue.getStatus() == WorkbenchDataItem.VAL_ERROR
										|| issue.getStatus() == WorkbenchDataItem.VAL_ERROR_EDIT)
								{
									invalidCellCount.getAndIncrement();
								} else if (issue.getStatus() == WorkbenchDataItem.VAL_MULTIPLE_MATCH
										|| issue.getStatus() == WorkbenchDataItem.VAL_NEW_DATA)
								{
									unmatchedCellCount.getAndIncrement();
								}
								
								//System.out.println("error " + invalidCellCount.get());
							}
						}
					}
					else
					{
						log.error("couldn't find workbench item for col " + col);
					}
				}
			}
		}
		return exceptionalItems;
    }
    
    /**
     * @param editRow
     * @param editCol (use -1 to validate entire row)
     */
    protected void updateRowValidationStatus(int editRow, int editCol)
    {
		WorkbenchRow wbRow = workbench.getRow(editRow);
		List<UploadTableInvalidValue> issues = doIncrementalValidation ? workbenchValidator.endCellEdit(editRow, editCol) 
				: new Vector<UploadTableInvalidValue>();
		List<UploadTableMatchInfo> matchInfo = null;
		
		Hashtable<Short, Short> originalStats = new Hashtable<Short, Short>();
		Hashtable<Short, WorkbenchDataItem> originals = wbRow.getItems();
		for (Map.Entry<Short, WorkbenchDataItem> original : originals.entrySet())
		{
			originalStats.put(original.getKey(), (short )original.getValue().getEditorValidationStatus());
		}
		
		if (doIncrementalMatching)
		{
			try
			{
				//XXX Really should avoid matching invalid columns. But that is tricky with trees.
				matchInfo = workbenchValidator.getUploader().matchData(editRow, editCol, issues);
			} catch (Exception ex)
			{
				//XXX what to do?, some exception might be caused by invalid data - filter out cols in exceptionalItems??
				//Maybe exceptions can be expected in general for a workbench-in-progress?
				//Or maybe we should blow up and force the workbench to close or something similarly drastic???
				ex.printStackTrace();
			}
		}
		List<CellStatusInfo> csis = new Vector<CellStatusInfo>(issues.size() + (matchInfo == null ? 0 : matchInfo.size()));
		for (UploadTableInvalidValue utiv : issues)
		{
				csis.add(new CellStatusInfo(utiv));
		}
		if (doIncrementalMatching && matchInfo != null)
		{
			for (UploadTableMatchInfo utmi : matchInfo)
			{
				if (utmi.getNumberOfMatches() != 1) //for now we don't care if a single match exists
				{
					csis.add(new CellStatusInfo(utmi));
				}
			}
		}
			
		
		Hashtable<Short, Short> exceptionalItems = updateCellStatuses(csis, wbRow);
		for (WorkbenchDataItem wbItem : wbRow.getWorkbenchDataItems())
		{
			Short origstat = originalStats.get(new Short((short )wbItem.getColumnNumber()));
			if (origstat != null)
			{
				if (origstat != wbItem.getEditorValidationStatus() || exceptionalItems.get(wbItem.getColumnNumber()) == null)
				{
					if (origstat == WorkbenchDataItem.VAL_MULTIPLE_MATCH || origstat == WorkbenchDataItem.VAL_NEW_DATA)
					{
						unmatchedCellCount.getAndDecrement();
					} else if (origstat == WorkbenchDataItem.VAL_ERROR || origstat == WorkbenchDataItem.VAL_ERROR_EDIT)
					{
						invalidCellCount.getAndDecrement();
					}
					if (exceptionalItems.get(wbItem.getColumnNumber()) == null)
					{
						//XXX synchronization is not really necessary anymore, right??
						synchronized(wbItem)
						{
							wbItem.setStatusText(null);
							wbItem.setEditorValidationStatus(WorkbenchDataItem.VAL_OK);
						}
					}
				}
			}
			
		}
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
			validateRows(null, startRow, endRow, true, null);
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
			validateRows(rows, -1, -1, rows.length <= 17, null);
			//validateRows(rows, -1, -1, true, null);
    		return true;
		}
    	return false;
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
            
            wbPlugin.setSpreadSheet(spreadSheet);
            wbPlugin.setWorkbench(workbench);
            workBenchPlugins.put(wbPluginCls.getSimpleName(), wbPlugin);
            
            JButton btn = createIconBtn(iconName, IconManager.IconSize.Std20,
                    tooltipKey, false, new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                public void run()
                                {
                                    wbPlugin.process(getSelectedRows());
                                }
                            });
                        }
                    });
            workBenchPluginBtns.add(btn);
            
            List<String> missingFields = wbPlugin.getMissingFieldsForPlugin();
            if (missingFields != null && missingFields.size() > 0)
            {
                btn.setEnabled(false);
                String ttText = "<p>" + getResourceString("WB_ADDITIONAL_FIELDS_REQD") + ":<ul>";
                for (String reqdField : missingFields)
                {
                    ttText += "<li>" + reqdField + "</li>";
                }
                ttText += "</ul>";
                String origTT = btn.getToolTipText();
                btn.setToolTipText("<html>" + origTT + ttText);
            }
            else
            {
                btn.setEnabled(true);
            }
            
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
    
    //----------------------------------------------------------------------------------------
    //--
    //----------------------------------------------------------------------------------------
    private class ValidationWorker extends javax.swing.SwingWorker<Object, Object>
    {
		private final int[] rows;
		private final int startRow;
		private final int endRow;
		private final boolean useGlassPane;
		private SimpleGlassPane glassPane;
		
		//Vectors are thread safe?? Right??
		private final Vector<Integer> deletedRows = new Vector<Integer>();
		
		
    	/**
		 * @param rows
		 * @param startRow
		 * @param endRow
		 */
		public ValidationWorker(int[] rows, int startRow, int endRow, 
				boolean useGlassPane)
		{
			super();
			this.rows = rows;
			this.startRow = startRow;
			this.endRow = endRow;
			this.useGlassPane = useGlassPane;
		}

		/**
		 * @param row
		 * @return row adjusted to account for deletes. Or -1 if the row has been deleted.
		 */
		private int adjustRow(int row)
		{
			int result = row;
			//Not sure what happens if deletedRows is added to during the following loop.
			//Doesn't seem important enough to worry about.
			for (Integer deleted : deletedRows)
			{
				if (deleted == row)
				{
					result = -1;
					break;
					
				} else if (row > deleted)
				{
					result--;
				}
			} 
			return result;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Object doInBackground() throws Exception
		{
			if (useGlassPane)
			{
	            this.glassPane = UIRegistry.writeSimpleGlassPaneMsg(String.format(getResourceString("WorkbenchPaneSS.Validating"), new Object[] {workbench.getName()}), 
	            		WorkbenchTask.GLASSPANE_FONT_SIZE);
			} 			
			if (rows != null)
			{
				int count = rows.length;
				int rowCount = 0;
				for (int row : rows)
				{
					int adjustedRow = adjustRow(row);
					if (adjustedRow != -1)
					{
						updateRowValidationStatus(adjustedRow, -1);
					}
					if (useGlassPane)
					{
						//System.out.println((int)( (100.0 * ++rowCount) / count));
						glassPane.setProgress((int)( (100.0 * ++rowCount) / count));
					}
				}
			} else
			{
				int count = endRow - startRow + 1;
				int rowCount = 0;
				try 
				{
				for (int row = startRow; row <= endRow; row++)
				{
					
					int adjustedRow = adjustRow(row);
					if (adjustedRow != -1)
					{
						updateRowValidationStatus(adjustedRow, -1);
					}
					if (useGlassPane)
					{
						int progress = (int)( (100.0 * ++rowCount) / count);
						//System.out.println(progress);
						glassPane.setProgress(progress);
					}
				}
				} catch (Exception ex)
				{
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
			if (useGlassPane)
			{
				UIRegistry.clearSimpleGlassPaneMsg();
			}
			if (isCancelled())
			{
				//currently cancellation only occurs during shutdown.
				//System.out.println("done(): Clearing validationWorkerQueue");
				validationWorkerQueue.clear();
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
		
		public void rowDeleted(int row)
		{
			deletedRows.add(row);
		}
    }
    
    /**
     * @param glassPane
     */
    public void validateAll(final SimpleGlassPane glassPane)
    {
    	//System.out.println("validating all " + spreadSheet.getRowCount() + " rows.");
    	validateRows(null, 0, spreadSheet.getRowCount()-1, false, glassPane);
    }
    
    /**
     * @param rows
     * @param startRow
     * @param endRow
     * @param isDoInBackground
     */
    protected void validateRows(final int[] rows, final int startRow, final int endRow, boolean doSecretly, final SimpleGlassPane glassPane)
    {
    	
    	ValidationWorker newWorker = new ValidationWorker(rows, startRow, endRow, !doSecretly);
//    	if (doSecretly)
    	{
    		//System.out.println("validateRows(): adding worker to queue");
    		validationWorkerQueue.add(newWorker);
    		if (validationWorkerQueue.peek() == newWorker)
    		{
    			//System.out.println("validateRows(): executing new worker");
    			newWorker.execute();
    		}
    		SwingUtilities.invokeLater(new Runnable(){
    			@Override
    			public void run() {
    				addRowsBtn.setEnabled(false);
    				deleteRowsBtn.setEnabled(false);
    			}
    		});
    	} //else
//    	if (!doSecretly)
//    	{
//    		try
//    		{
//    			//newWorker.execute();
//    			newWorker.get();
//    		} catch (Exception ex)
//    		{
//                UsageTracker.incrHandledUsageCount();
//                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchPaneSS.class, ex);
//                log.error(ex);
//    		}
//    	}
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

        public GridCellEditor(final JTextField textField, final String caption, final int length, final JButton gcSaveBtn)
        {
            super(textField);
            init(textField, caption, length, gcSaveBtn);
         }
        
        public GridCellEditor(final JComboBox combo, final String caption, final int length, final JButton gcSaveBtn)
        {
        	super(combo);
        	init(combo, caption, length, gcSaveBtn);
        }
        
        protected void init(final JComponent comp, final String caption, final int length, final JButton gcSaveBtn)
        {
           	this.uiComponent = comp;
            this.length    = length;
            this.ceSaveBtn = saveBtn;
     
            
            verifier = new LengthInputVerifier(caption, length);
            uiComponent.setInputVerifier(verifier);

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

        /* (non-Javadoc)
         * @see javax.swing.DefaultCellEditor#stopCellEditing()
         */
        @Override
        public boolean stopCellEditing()
        {
        	boolean result = super.stopCellEditing();
            if (editCol == -1 || editRow == -1)
            {
            	editRow = -1;
            	editCol = -1;
            	return result; //a 'superfluous' re-call of this method.
            }
        	if (result)
            {
            	if (!verifier.verify(uiComponent))
            	{
            		ceSaveBtn.setEnabled(false);
                	editRow = -1;
                	editCol = -1;
            		return false;
            	}
            	if (getIncremental() && workbenchValidator != null)
            	{
            		updateRowValidationStatus(spreadSheet.convertRowIndexToModel(editRow), spreadSheet.convertColumnIndexToModel(editCol));
            		updateBtnUI();
            	}
            	editRow = -1;
            	editCol = -1;
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
            return true;
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
    	public GridCellListEditor(final JComboBox combo, final String caption, final int length, final JButton gcSaveBtn)
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
            //return textField;
            //DefaultComboBoxModel model = new DefaultComboBoxModel(pickList.getList());
            //((JComboBox )uiComponent).setModel(model);
            return uiComponent;
        }
   	        
		/* (non-Javadoc)
         * @see javax.swing.CellEditor#getCellEditorValue()
         */
        @Override
        public Object getCellEditorValue() 
        {
            return ((JComboBox )uiComponent).getSelectedItem().toString();
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
    public class WbCellRenderer extends DefaultTableCellRenderer
    {
		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int tblRow, int tblColumn)
		{
			JLabel lbl = (JLabel) super.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, tblRow,
					tblColumn);
			int modelRow = spreadSheet.convertRowIndexToModel(tblRow);
			WorkbenchRow wbRow = workbench.getRow(modelRow);
			String cardImageFullPath = wbRow.getCardImageFullPath();
			if (cardImageFullPath != null)
			{
				String filename = FilenameUtils
						.getBaseName(cardImageFullPath);
				filename = FilenameUtils.getName(cardImageFullPath);
				lbl.setText(filename);
				lbl.setHorizontalAlignment(SwingConstants.CENTER);
			}
			return lbl;
		}
		
		
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
     * @return the hasChanged
     */
    public boolean isHasChanged()
    {
        return hasChanged;
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
     * A debugging tool Used to find discrepancies between workbench and specify schemas.
     */
    protected void compareSchemas()
    {
        List<Pair<DBFieldInfo, DBFieldInfo>> badFlds = new LinkedList<Pair<DBFieldInfo, DBFieldInfo>>();
        DBTableIdMgr wbSchema = WorkbenchTask.getDatabaseSchema();
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
    		buildValidator();
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
			status = WorkbenchDataItem.VAL_ERROR;
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
	private class CellRenderingAttributes
	{
		public Color errorBorder = Color.RED;
		public Color errorForeground = errorBorder;
		public Color errorBackground = new Color(errorBorder.getRed(), errorBorder.getGreen(), errorBorder.getBlue(), 37);
		public Color newDataBorder = Color.YELLOW;
		public Color newDataForeground = Color.YELLOW;
		public Color newDataBackground = new Color(newDataBorder.getRed(), newDataBorder.getGreen(), newDataBorder.getBlue(), 37);
		public Color multipleMatchBorder = Color.ORANGE;
		public Color multipleMatchBackground = new Color(multipleMatchBorder.getRed(), multipleMatchBorder.getGreen(), multipleMatchBorder.getBlue(), 37);
		public Color multipleMatchForeground = multipleMatchBorder;
		public Color notMatchedBorder = newDataBorder;
		public Color notMatchedBackground = null;
		public Color notMatchedForeground = notMatchedBorder;
		
		
		private class Atts
		{
			public String toolTip;
			public Border border;
			public Color background;
			
			public Atts(String toolTip, Border border, Color background)
			{
				this.toolTip = toolTip;
				this.border = border;
				this.background = background;
			}
		}
		
		
		protected Atts getAtts(int wbCellStatus, String statusText)
		{
			LineBorder bdr = null;
			Color bg = null;
			if (doIncrementalValidation && (wbCellStatus == WorkbenchDataItem.VAL_ERROR 
					|| wbCellStatus == WorkbenchDataItem.VAL_ERROR_EDIT))
			{
				bdr = new LineBorder(errorBorder);
				bg = errorBackground;
			} else if (doIncrementalMatching && wbCellStatus == WorkbenchDataItem.VAL_NEW_DATA)
			{
				bdr = new LineBorder(newDataBorder);
				bg = newDataBackground;
			} else if (doIncrementalMatching && wbCellStatus == WorkbenchDataItem.VAL_MULTIPLE_MATCH)
			{
				bdr = new LineBorder(multipleMatchBorder);
				bg = multipleMatchBackground;
			} else if (doIncrementalMatching && wbCellStatus == WorkbenchDataItem.VAL_NOT_MATCHED)
			{
				bdr = new LineBorder(notMatchedBorder);
				bg = notMatchedBackground;
			}
			return new Atts(statusText, bdr, bg);
		}
		
		public CellRenderingAttributes()
		{
			
		}
		
		/**
		 * @param lbl
		 * @param wbCell
		 */
		public void addAttributes(JLabel lbl, final WorkbenchDataItem wbCell)
		{
			if (getIncremental())
			{
				int cellStatus = WorkbenchDataItem.VAL_OK;
				String cellStatusText = null;
				if (wbCell != null)
				{
					// XXX WorkbenchDataItems can be updated by GridCellEditor
					// or by background validation initiated at load time or
					// after find/replace ops
					// but probably not necessary to synchronize here?
					//synchronized (wbCell)
					//{
						cellStatus = wbCell.getEditorValidationStatus();
						cellStatusText = wbCell.getStatusText();
					//}
				} 			
				//currently nothing extra is done for OK cells
				if (cellStatus != WorkbenchDataItem.VAL_NONE && cellStatus != WorkbenchDataItem.VAL_OK)
				{
					Atts atts = getAtts(cellStatus, cellStatusText);
					lbl.setToolTipText(atts.toolTip);
					lbl.setBorder(atts.border);
					//Using ColorHighlighters to set background colors because
					//they do not override the selection color
//					if (atts.background != null)
//					{
//						//lbl.setOpaque(true);
//						lbl.setBackground(atts.background);
//					}
				}
			}
			
		}
	}
	
	private class GridCellPredicate implements HighlightPredicate
	{
		final static public int ValidationPredicate = 0;
		final static public int MatchingPredicate = 1;
		final static public int AnyPredicate = 2;
		protected final int activation;
		protected final Integer[] conditions;
		
		public GridCellPredicate(int activation, Integer[] conditions)
		{
			this.activation = activation;
			this.conditions = conditions;
		}

		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.decorator.HighlightPredicate#isHighlighted(java.awt.Component, org.jdesktop.swingx.decorator.ComponentAdapter)
		 */
		@Override
		public boolean isHighlighted(Component arg0, ComponentAdapter arg1) 
		{
			if ((activation == AnyPredicate && !getIncremental())
					|| (activation == ValidationPredicate && !doIncrementalValidation)
					|| (activation == MatchingPredicate && !doIncrementalMatching))
			{
				return false;
			
			} 
			
			WorkbenchRow wbRow = workbench.getRow(spreadSheet.convertRowIndexToModel(arg1.row));
			WorkbenchDataItem wbCell = wbRow.getItems().get((short )spreadSheet.convertColumnIndexToModel(arg1.column));
			if (wbCell == null)
			{
				return false;
			}
			
			int status = wbCell.getEditorValidationStatus();
			if (activation == AnyPredicate)
			{
				//Seems like a good idea to try to be as efficient as possible
				//but this will need to be recoded as new cell states are added
				return status == WorkbenchDataItem.VAL_ERROR
					|| status == WorkbenchDataItem.VAL_ERROR_EDIT
					|| status == WorkbenchDataItem.VAL_MULTIPLE_MATCH
					|| status == WorkbenchDataItem.VAL_NEW_DATA
					|| status == WorkbenchDataItem.VAL_NOT_MATCHED;
			}
			else {
				for (Integer condition : conditions)
				{
					if (condition == status)
					{
						((JLabel )arg0).setToolTipText(wbCell.getStatusText());
						return true;
					}
				}
			}
			return false;
		}
	}

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
			cellRenderAtts.addAttributes((JLabel )arg0, wbCell);
			return arg0;
		}
		
		
	}
}

