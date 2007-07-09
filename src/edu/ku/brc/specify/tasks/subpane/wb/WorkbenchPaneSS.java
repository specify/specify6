/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.tasks.subpane.wb;


import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.createIconBtn;
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
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
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
import org.jdesktop.swingx.table.TableColumnExt;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.StaleObjectException;
import edu.ku.brc.helpers.ImageFilter;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.services.biogeomancer.BioGeomancer;
import edu.ku.brc.services.biogeomancer.BioGeomancerQuerySummaryStruct;
import edu.ku.brc.services.biogeomancer.BioGeomancerResultStruct;
import edu.ku.brc.services.geolocate.client.GeoLocate;
import edu.ku.brc.services.geolocate.client.GeorefResult;
import edu.ku.brc.services.geolocate.client.GeorefResultSet;
import edu.ku.brc.services.mapping.LocalityMapper;
import edu.ku.brc.services.mapping.SimpleMapLocation;
import edu.ku.brc.services.mapping.LocalityMapper.MapLocationIFace;
import edu.ku.brc.services.mapping.LocalityMapper.MapperListener;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.exporters.ExportFileConfigurationFactory;
import edu.ku.brc.specify.exporters.ExportToFile;
import edu.ku.brc.specify.exporters.GoogleEarthExporter;
import edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace;
import edu.ku.brc.specify.exporters.WorkbenchRowPlacemarkWrapper;
import edu.ku.brc.specify.tasks.ExportTask;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.specify.ui.LengthInputVerifier;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DropDownButtonStateful;
import edu.ku.brc.ui.DropDownMenuInfo;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.SearchReplacePanel;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.ToggleButtonChooserDlg.Type;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.ui.forms.ResultSetController;
import edu.ku.brc.ui.forms.ResultSetControllerListener;
import edu.ku.brc.ui.tmanfe.SpreadSheet;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.StringConverter;
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
public class WorkbenchPaneSS extends BaseSubPane
{
    private static final Logger log = Logger.getLogger(WorkbenchPaneSS.class);
    
    private enum PanelType {Spreadsheet, Form}
    
    protected SearchReplacePanel    findPanel              = null;
    protected SpreadSheet           spreadSheet;
    protected Workbench             workbench;
    protected GridTableModel        model;
    protected TableColumnExt        imageColExt;
    protected String[]              columns;
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
    protected JButton               biogeomancerBtn        = null;
    protected JButton               convertGeoRefFormatBtn = null;
    protected JButton               exportExcelCsvBtn      = null;

    protected List<JButton>         selectionSensativeButtons  = new Vector<JButton>();
    
    protected int                   currentRow                 = 0;
    protected FormPane              formPane;
    protected ResultSetController   resultsetController;
    
    protected CardLayout            cardLayout                 = null;
    protected JPanel                mainPanel;
    protected PanelType             currentPanelType           = PanelType.Spreadsheet;
    
    protected JPanel                controllerPane;
    protected CardLayout            cpCardLayout               = null;
    
    protected ImageFrame            imageFrame                 = null;
    protected boolean               imageFrameWasShowing       = false;
    protected ListSelectionListener workbenchRowChangeListener = null;
    
    protected JFrame                mapFrame                   = null;
    protected JLabel                mapImageLabel              = null;
    
    // XXX PREF
    protected int                   mapSize                    = 500;
    
    /**
     * Constructs the pane for the spreadsheet.
     * 
     * @param name the name of the pane
     * @param task the owning task
     * @param workbench the workbench to be editted
     * @param showImageView shows image window when first showing the window
     */
    public WorkbenchPaneSS(final String    name,
                           final Taskable  task,
                           final Workbench workbenchArg,
                           final boolean   showImageView)
    {
        super(name, task);
        
        removeAll();
        
        if (workbenchArg == null)
        {
            return;
        }
        this.workbench = workbenchArg;
        
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
        
        model       = new GridTableModel(workbench, headers);
        spreadSheet = new SpreadSheet(model);
        model.setSpreadSheet(spreadSheet);
        
        findPanel = spreadSheet.getFindReplacePanel();
        UIRegistry.getLaunchFindReplaceAction().setSearchReplacePanel(findPanel);
        
        spreadSheet.setShowGrid(true);
        spreadSheet.getTableHeader().setReorderingAllowed(false); // Turn Off column dragging

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
            }
            @Override
            public void focusLost(FocusEvent e)
            {
                UIRegistry.enableCutCopyPaste(true);
            }
        });
        
        saveBtn = new JButton(getResourceString("Save"));
        saveBtn.setToolTipText(String.format(getResourceString("WB_SAVE_DATASET_TT"), new Object[] {workbench.getName()}));
        saveBtn.setEnabled(false);
        saveBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                UsageTracker.incrUsageCount("WB.SaveDataSet");
                
                UIRegistry.writeGlassPaneMsg(String.format(getResourceString("WB_SAVING"), new Object[] { workbench.getName()}), WorkbenchTask.GLASSPANE_FONT_SIZE);
                
                final SwingWorker worker = new SwingWorker()
                {
                     @SuppressWarnings("synthetic-access")
                    @Override
                    public Object construct()
                    {
                         try
                         {
                             saveObject();
                             
                         } catch (Exception ex)
                         {
                             log.error(ex);
                             return ex;
                         }
                        return null;
                    }

                    //Runs on the event-dispatching thread.
                    @Override
                    public void finished()
                    {
                        Object retVal = get();
                        if (retVal != null && retVal instanceof Exception)
                        {
                            Exception ex = (Exception)retVal;
                            UIRegistry.getStatusBar().setErrorMessage(getResourceString("WB_ERROR_SAVING"), ex);
                        }
                        
                        UIRegistry.clearGlassPaneMsg();
                    }
                };
                worker.start();

            }
        });

        // NOTE: This needs to be done after the creation of the saveBtn
        initColumnSizes(spreadSheet, saveBtn);

        Action delAction = addRecordKeyMappings(spreadSheet, KeyEvent.VK_F3, "DelRow", new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                deleteRows();
            }
        });
        deleteRowsBtn = createIconBtn("DelRec", "WB_DELETE_ROW", delAction);
        selectionSensativeButtons.add(deleteRowsBtn);
        spreadSheet.setDeleteAction(delAction);

        clearCellsBtn = createIconBtn("Eraser", "WB_CLEAR_CELLS", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                if (spreadSheet.getCellEditor() != null)
                {
                    spreadSheet.getCellEditor().stopCellEditing();
                }
                int[] rows = spreadSheet.getSelectedRowModelIndexes();
                int[] cols = spreadSheet.getSelectedColumnModelIndexes();
                model.clearCells(rows,cols);
            }
        });
        selectionSensativeButtons.add(clearCellsBtn);
        
        Action addAction = addRecordKeyMappings(spreadSheet, KeyEvent.VK_N, "AddRow", new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                if (workbench.getWorkbenchRows().size() < WorkbenchTask.MAX_ROWS)
                {
                    addRowAfter();
                }
            }
        });
        addRowsBtn = createIconBtn("AddRec", "WB_ADD_ROW", addAction);
        addRowsBtn.setEnabled(true);
        addAction.setEnabled(true); 


        carryForwardBtn = createIconBtn("Configure", IconManager.IconSize.NonStd, "WB_CARRYFORWARD", false, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                UsageTracker.getUsageCount("WBCarryForward");

                configCarryFoward();
            }
        });
        carryForwardBtn.setEnabled(true);

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
        
        exportKmlBtn = createIconBtn("GoogleEarth", IconManager.IconSize.NonStd, "WB_SHOW_IN_GOOGLE_EARTH", false, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showRecordsInGoogleEarth();
            }
        });
        // enable or disable along with Show Map and Geo Ref Convert buttons
        
        biogeomancerBtn = createIconBtn("BioGeoMancer", IconManager.IconSize.NonStd, "WB_DO_BIOGEOMANCER_LOOKUP", false, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                AppPreferences remotePrefs = AppPreferences.getRemote();
                String tool = remotePrefs.get("georef_tool", "");
                if (tool.equalsIgnoreCase("geolocate"))
                {
                    doGeoLocateLookup();    
                } else
                {
                    doBioGeomancerLookup();                    
                }
            }
        });
        // only enable it if the workbench has the proper columns in it
        String[] missingColumnsForBG = getMissingButRequiredColumnsForBioGeomancer();
        if (missingColumnsForBG.length > 0)
        {
            biogeomancerBtn.setEnabled(false);
            String ttText = "<p>" + getResourceString("WB_ADDITIONAL_FIELDS_REQD") + ":<ul>";
            for (String reqdField: missingColumnsForBG)
            {
                ttText += "<li>" + reqdField + "</li>";
            }
            ttText += "</ul>";
            String origTT = biogeomancerBtn.getToolTipText();
            biogeomancerBtn.setToolTipText("<html>" + origTT + ttText);
        }
        else
        {
            biogeomancerBtn.setEnabled(true);
        }
        
        convertGeoRefFormatBtn = createIconBtn("ConvertGeoRef", IconManager.IconSize.NonStd, "WB_CONVERT_GEO_FORMAT", false, new ActionListener()
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
            for (String reqdField: missingGeoRefFields)
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
        
        exportExcelCsvBtn = createIconBtn("Export", IconManager.IconSize.NonStd, "WB_EXPORT_DATA", false, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                doExcelCsvExport();
            }
        });
        exportExcelCsvBtn.setEnabled(true);
        
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
        imageFrame = new ImageFrame(mapSize, this, this.workbench);
        
        setupWorkbenchRowChangeListener();
                
        // setup the mapping features
        mapFrame = new JFrame();
        mapFrame.setIconImage( IconManager.getImage("AppIcon").getImage());
        mapFrame.setTitle(getResourceString("WB_GEO_REF_DATA_MAP"));
        mapImageLabel = new JLabel();
        mapImageLabel.setSize(500,500);
        mapFrame.add(mapImageLabel);
        mapFrame.setSize(500,500);
        
        // start putting together the visible UI
        CellConstraints cc = new CellConstraints();

        JComponent[] comps      = {addRowsBtn, deleteRowsBtn, clearCellsBtn, showMapBtn, exportKmlBtn, biogeomancerBtn, convertGeoRefFormatBtn, exportExcelCsvBtn};
        PanelBuilder spreadSheetControlBar = new PanelBuilder(new FormLayout("f:p:g,4px,"+createDuplicateJGoodiesDef("p", "4px", comps.length)+",4px,", "c:p:g"));
        
        int x = 3;
        for (JComponent c : comps)
        {
            spreadSheetControlBar.add(c, cc.xy(x,1));
            x += 2;
        }
        
        //spreadSheetControlBar.getPanel().setBackground(Color.MAGENTA); // DEBUG
        //spreadSheetControlBar.getPanel().setOpaque(true);
        
        // Create the Form Pane
        formPane = new FormPane(this, workbench);
        
        // This panel contains just the ResultSetContoller, it's needed so the RSC gets centered
        PanelBuilder rsPanel = new PanelBuilder(new FormLayout("c:p:g", "c:p:g"));
        resultsetController  = new ResultSetController(null, true, true, getResourceString("Record"), model.getRowCount());
        resultsetController.addListener(formPane);
        resultsetController.getDelRecBtn().addActionListener(delAction);
        rsPanel.add(resultsetController.getPanel(), cc.xy(1,1));
        
        //rsPanel.getPanel().setBackground(Color.YELLOW); // DEBUG
        //rsPanel.getPanel().setOpaque(true);
        
        // This panel is a single row containing the ResultSetContoller and the other controls for the Form Panel  
        PanelBuilder resultSetPanel = new PanelBuilder(new FormLayout("f:p:g, p, f:p:g, p", "c:p:g"));
        // Now put the two panel into the single row panel
        resultSetPanel.add(rsPanel.getPanel(), cc.xy(2,1));
        resultSetPanel.add(formPane.getControlPropsBtn(), cc.xy(4,1));
        
        //resultSetPanel.getPanel().setBackground(Color.ORANGE); // DEBUG
        //resultSetPanel.getPanel().setOpaque(true);
        
        // Create the main panel that uses card layout for the form and spreasheet
        mainPanel = new JPanel(cardLayout = new CardLayout());
        
        // Add the Form and Spreadsheet to the CardLayout
        mainPanel.add(spreadSheet.getScrollPane(), PanelType.Spreadsheet.toString());
        mainPanel.add(formPane.getScrollPane(),    PanelType.Form.toString());
        
        // The controllerPane is a CardLayout that switches between the Spreadsheet control bar and the Form Control Bar
        controllerPane = new JPanel(cpCardLayout = new CardLayout());
        controllerPane.add(spreadSheetControlBar.getPanel(), PanelType.Spreadsheet.toString());
        controllerPane.add(resultSetPanel.getPanel(),        PanelType.Form.toString());
        
        //controllerPane.setBackground(Color.BLUE); // DEBUG
        //controllerPane.setOpaque(true);
        
        JLabel sep1 = new JLabel(IconManager.getIcon("Separator"));
        JLabel sep2 = new JLabel(IconManager.getIcon("Separator"));
        
        // This works
        setLayout(new BorderLayout());
        PanelBuilder    ctrlBtns   = new PanelBuilder(new FormLayout("p,4px,p,6px,6px,6px,p,7px,6px,p", "c:p:g"));
        ctrlBtns.add(toggleImageFrameBtn, cc.xy(1,1));
        ctrlBtns.add(carryForwardBtn,     cc.xy(3,1));
        ctrlBtns.add(sep1,                cc.xy(5,1));
        ctrlBtns.add(saveBtn,             cc.xy(7,1));
        ctrlBtns.add(sep2,                cc.xy(9,1));
        ctrlBtns.add(createSwitcher(),    cc.xy(10,1));
        
        //ctrlBtns.getPanel().setBackground(Color.GREEN); // DEBUG
        //ctrlBtns.getPanel().setOpaque(true);
        
        add(mainPanel, BorderLayout.CENTER);
        
        FormLayout      formLayout = new FormLayout("f:p:g,4px,p", "2px,f:p:g,p:g");
        PanelBuilder    builder    = new PanelBuilder(formLayout);

        builder.add(controllerPane,      cc.xy(1,2));
        builder.add(ctrlBtns.getPanel(), cc.xy(3,2));
        builder.add(findPanel,           cc.xywh(1, 3, 3, 1));


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
    }
    
    /**
     * Checks the cell for cell editing and stops it.
     */
    public boolean checkCurrentEditState()
    {
        boolean isOK = true;
        if (currentPanelType == PanelType.Spreadsheet)
        {
            if (spreadSheet.getCellEditor() != null)
            {
                int index = spreadSheet.getSelectedRow();
                isOK = spreadSheet.getCellEditor().stopCellEditing();
                spreadSheet.setRowSelectionInterval(index, index);
            }
        } else
        {
            formPane.copyDataFromForm();
        }
        return isOK;
    }
    
    /**
     * Update enaabled state of buttons effected by the spreadsheet selection.
     */
    protected void updateBtnUI()
    {
        boolean enable = spreadSheet.getSelectedRow() > -1;
        for (JButton btn: selectionSensativeButtons)
        {
            btn.setEnabled(enable);
        }
        enable = workbench.getWorkbenchRows().size() < WorkbenchTask.MAX_ROWS;
        addRowsBtn.setEnabled(enable);
        resultsetController.getNewRecBtn().setEnabled(enable);
    }
    
    /**
     * Adds a Key mappings.
     * @param comp comp
     * @param keyCode keyCode
     * @param actionName actionName
     * @param action action 
     * @return the action
     */
    protected Action addRecordKeyMappings(final JComponent comp, final int keyCode, final String actionName, final Action action)
    {
        InputMap  inputMap  = comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = comp.getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke(keyCode, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), actionName);
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
     * Adds a row after the selection. 
     */
    public void addRowAfter()
    {
        checkCurrentEditState();
        
        int curSelInx = getCurrentIndexFromFormOrSS();
        model.insertAfterRow(curSelInx);
        
        int count = model.getRowCount();
        resultsetController.setLength(count);
        
        adjustSelectionAfterAdd(count == 1 ? 0 : curSelInx+1);
        
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
                                            IconManager.getImage("EditForm24", IconManager.IconSize.NonStd), 
                                            getResourceString("WB_SHOW_FORM_VIEW")));
        menuItems.add(new DropDownMenuInfo(getResourceString("Grid"), 
                                            IconManager.getImage("Spreadsheet24", IconManager.IconSize.NonStd), 
                                            getResourceString("WB_SHOW_GRID_VIEW")));
        final DropDownButtonStateful switcher = new DropDownButtonStateful(menuItems);
        switcher.setCurrentIndex(1);
        switcher.setToolTipText(getResourceString("SwitchViewsTT"));
        switcher.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                showPanel(switcher.getCurrentIndex() == 1 ? PanelType.Spreadsheet : PanelType.Form);
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
            // Enable the "Find" action in the Edit menu when a spreadsheet is shown
            if (isSpreadsheet)
            {
                UIRegistry.enableFindinEditMenu(findPanel);
            }
            c.setVisible(isSpreadsheet);
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
        if (spreadSheet.getCellEditor() != null)
        {
            spreadSheet.getCellEditor().stopCellEditing();
        }

        // and add or remove the ListSelectionListener (to avoid loading images when not visible)
        if (!visible)
        {
            // hide the image window
            
            // turn off alwaysOnTop for Swing repaint reasons
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
            
            TableColumn column = spreadSheet.getTableHeader().getColumnModel().getColumn(spreadSheet.getTableHeader().getColumnModel().getColumnCount()-1);
            column.setCellRenderer(new DefaultTableCellRenderer()
            {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int tblRow, int tblColumn)
                {
                    JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, tblRow, tblColumn);
                    int modelRow = spreadSheet.convertRowIndexToModel(tblRow);
                    WorkbenchRow wbRow = workbench.getRow(modelRow);
                    String cardImageFullPath = wbRow.getCardImageFullPath();
                    if (cardImageFullPath != null)
                    {
                        String filename = FilenameUtils.getBaseName(cardImageFullPath);
                        filename = FilenameUtils.getName(cardImageFullPath);
                        lbl.setText(filename);
                        lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    }
                    return lbl;
                }

            });
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
            JOptionPane.showMessageDialog(UIRegistry.getMostRecentWindow(), 
                                          String.format(getResourceString("WB_WRONG_IMAGE_TYPE"), 
                                                  new Object[] {FilenameUtils.getExtension(fullPath)}),
                                          UIRegistry.getResourceString("Warning"), 
                                          JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    /**
     * Shows a dialog that enales the user to convert the lat/lon formats. 
     */
    protected void showGeoRefConvertDialog()
    {
        UsageTracker.incrUsageCount("WB.ShowGeoRefConverter");
        
        JStatusBar statusBar = UIRegistry.getStatusBar();

        if (!workbench.containsGeoRefData())
        {
            statusBar.setErrorMessage(getResourceString("NoGeoRefColumns"));
            return;
        }

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
        final int[] selRows = new int[selection.length];
        for (int i = 0; i < selection.length; ++i)
        {
            selRows[i] = selection[i];
        }

        List<String> outputFormats = new Vector<String>();
        String dddddd = getResourceString("DDDDDD");
        String ddmmmm = getResourceString("DDMMMM");
        String ddmmss = getResourceString("DDMMSS");
        outputFormats.add(dddddd);
        outputFormats.add(ddmmmm);
        outputFormats.add(ddmmss);
        
        final int locTabId = DBTableIdMgr.getInstance().getIdByClassName(Locality.class.getName());
        final int latColIndex = workbench.getColumnIndex(locTabId,"latitude1");
        final int lonColIndex = workbench.getColumnIndex(locTabId, "longitude1");
        
        // it's fine if these come back as -1
        // that results in no backups of the original values
        final int lat1TextColIndex = workbench.getColumnIndex(locTabId, "Lat1Text");
        final int long1TextColIndex = workbench.getColumnIndex(locTabId, "Long1Text");

        JFrame mainFrame = (JFrame)UIRegistry.getTopWindow();
        
        String title = "GeoRefConv";
        String description = "GeoRefConvDesc";
        ToggleButtonChooserDlg<String> dlg = new ToggleButtonChooserDlg<String>(mainFrame,title,description,outputFormats,null, CustomDialog.OKCANCEL, Type.RadioButton)
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
            protected void okButtonPressed()
            {
                checkCurrentEditState();
                
                // don't call super.okButtonPressed() b/c it will close the window
                isCancelled = false;
                btnPressed  = OK_BTN;
                switch( getSelectedIndex() )
                {
                    case 0:
                    {
                        convertColumnContents(latColIndex, selRows, new GeoRefConverter(), GeoRefFormat.D_PLUS_MINUS.name(), lat1TextColIndex);
                        convertColumnContents(lonColIndex, selRows, new GeoRefConverter(), GeoRefFormat.D_PLUS_MINUS.name(), long1TextColIndex);
                        break;
                    }
                    case 1:
                    {
                        convertColumnContents(latColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DM_PLUS_MINUS.name(), lat1TextColIndex);
                        convertColumnContents(lonColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DM_PLUS_MINUS.name(), long1TextColIndex);
                        break;
                    }
                    case 2:
                    {
                        convertColumnContents(latColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DMS_PLUS_MINUS.name(), lat1TextColIndex);
                        convertColumnContents(lonColIndex, selRows, new GeoRefConverter(), GeoRefFormat.DMS_PLUS_MINUS.name(), long1TextColIndex);
                        break;
                    }
                }
            }
        };
        dlg.setModal(false);
        dlg.setSelectedIndex(0);
        dlg.setOkLabel(getResourceString("Apply"));
        dlg.setCancelLabel(getResourceString("Close"));
        dlg.setVisible(true);
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
                lat1 = converter.convert(lat1, GeoRefFormat.D_PLUS_MINUS.name());
                latitude = new Double(lat1);
                lon1 = converter.convert(lon1, GeoRefFormat.D_PLUS_MINUS.name());
                longitude = new Double(lon1);
            }
            catch (Exception e)
            {
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
                    newLoc = new SimpleMapLocation(latitude,longitude,latitude2,longitude2);
                }
                catch (Exception e)
                {
                    // this could be a number format exception
                    // or a null pointer exception if the field was empty
                    // either way, we'll just treat this record as though it only has lat1 and lon1
                }
            }
            else // use just the point
            {
                // we only have lat1 and long2
                newLoc = new SimpleMapLocation(latitude,longitude,null,null);
            }
            
            // add the location to the list
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
                    errorMsg = "Error connecting to mapping service";
                }
                else
                {
                    errorMsg = "Failed to get map from service";
                }
                JStatusBar statusBar = UIRegistry.getStatusBar();
                statusBar.setErrorMessage(errorMsg,e);
                statusBar.setIndeterminate(false);
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
        statusBar.setIndeterminate(true);
        statusBar.setText(getResourceString("WB_CREATINGMAP"));
    }
    
    /**
     * Notification that the Map was received.
     * @param map icon of the map that was generated
     */
    protected void mapImageReceived(final Icon map)
    {
        JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setIndeterminate(false);
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
                statusBar.setWarningMessage("Resulting map is thin.  May not display well.");
            }
        }
    }
    
    /**
     * Converts the column contents from on format of Lat/Lon to another
     * @param columnIndex the index of the column being converted
     * @param converter the converter to use
     * @param outputFormat the format string
     * @param backupColIndex the column index of the column to store the original value in, or -1 if none
     */
    protected void convertColumnContents(int columnIndex, int[] rows, StringConverter converter, String outputFormat, int backupColIndex)
    {
        for (int index = 0; index < rows.length; ++index)
        {
            int rowIndex = rows[index];
            String currentValue = (String)model.getValueAt(rowIndex, columnIndex);
            if (StringUtils.isBlank(currentValue))
            {
                continue;
            }

            String convertedValue;
            try
            {
                convertedValue = converter.convert(currentValue, outputFormat);
                
                // if the caller specified a "backup" column index, copy the original value to it
                // if the backup column doesn't already have contents and the converted value is actually
                // different from the current value
                if (backupColIndex != -1 && !convertedValue.equals(currentValue))
                {
                    String currVal = (String)model.getValueAt(rowIndex, backupColIndex);
                    if (currVal == null || StringUtils.isEmpty(currVal))
                    {
                        model.setValueAt(currentValue, rowIndex, backupColIndex);
                    }
                }
            }
            catch (Exception e)
            {
                // this value didn't convert correctly
                // it would be nice to highlight that cell, but I don't know how we could do that
                log.warn("Could not convert contents of cell (" + (rowIndex+1) + "," + (columnIndex+1) + ")");
                continue;
            }
            
            model.setValueAt(convertedValue, rowIndex, columnIndex);
            if (!currentValue.equals(convertedValue))
            {
                setChanged(true);
            }
        }
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
        
        CommandAction command = new CommandAction(ExportTask.EXPORT, ExportTask.EXPORT_LIST);
        command.setData(selectedRows);
        command.setProperty("exporter", ExportToFile.class);
        
        
        Properties props = new Properties();

        if (!((WorkbenchTask) task).getExportInfo(props))
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
            heads[h] = colHeads.get(h).getFieldName();
        }
        config.setHeaders(heads);
        
        command.addProperties(config.getProperties());
        
        UsageTracker.incrUsageCount("WB.ExportXLSRowsTool");
        CommandDispatcher.dispatch(command);
      }
    
    /**
     * Make a request to the ExportTask to display the selected records in GoogleEarth.
     */
    protected void showRecordsInGoogleEarth()
    {
        UsageTracker.incrUsageCount("WB.GoogleEarthRows");
        
        log.info("Showing map of selected records");
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
        
        // put all the selected rows in a List
        List<GoogleEarthPlacemarkIFace> selectedRows = new Vector<GoogleEarthPlacemarkIFace>();
        List<WorkbenchRow> rows = workbench.getWorkbenchRowsAsList();
        for (int i = 0; i < selection.length; ++i )
        {
            int index = selection[i];
            WorkbenchRow row = rows.get(index);
            int visibleRowNumber = spreadSheet.convertRowIndexToView(index);
            selectedRows.add(new WorkbenchRowPlacemarkWrapper(row, "Row " + (visibleRowNumber+1)));
        }
        
//        // get an icon URL that is specific to the current context
//        String discipline = CollectionObjDef.getCurrentCollectionObjDef().getDiscipline();
//        String iconUrl = null;
//        discipline = discipline.toLowerCase();
//        if (discipline.startsWith("fish"))
//        {
//            iconUrl = getResourceString("WB_GOOGLE_FISH_ICON_URL");
//        }
//        if (discipline.startsWith("bird"))
//        {
//            iconUrl = getResourceString("WB_GOOGLE_BIRD_ICON_URL");
//        }
//        if (discipline.startsWith("insect") || discipline.startsWith("ento"))
//        {
//            iconUrl = getResourceString("WB_GOOGLE_ENTO_ICON_URL");
//        }
//        if (discipline.startsWith("plant") || discipline.equals("botany"))
//        {
//            iconUrl = getResourceString("WB_GOOGLE_PLANT_ICON_URL");
//        }
//        if (discipline.startsWith("mammal"))
//        {
//            iconUrl = getResourceString("WB_GOOGLE_MAMMAL_ICON_URL");
//        }
//        if (discipline.startsWith("herp"))
//        {
//            iconUrl = getResourceString("WB_GOOGLE_HERP_ICON_URL");
//        }
        
        CommandAction command = new CommandAction(ExportTask.EXPORT,ExportTask.EXPORT_LIST);
        command.setData(selectedRows);
        command.setProperty("exporter", GoogleEarthExporter.class);
//        if (iconUrl != null)
//        {
//            command.setProperty("iconURL", iconUrl);
//        }
        JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setText("Opening Google Earth");  // XXX I18N
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
        if (workbench.getColumnIndex(localityTableId, "localityName") == -1 ||
            workbench.getColumnIndex(localityTableId, "latitude1") == -1 ||
            workbench.getColumnIndex(localityTableId, "longitude1") == -1)
        {
            return false;
        }
        
        // look for the geography fields
        int geographyTableId = DBTableIdMgr.getInstance().getIdByClassName(Geography.class.getName());
        if (workbench.getColumnIndex(geographyTableId, "Country") == -1 ||
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
            missingCols.add("localityName");
        }
        if (workbench.getColumnIndex(localityTableId, "latitude1") == -1)
        {
            missingCols.add("latitude1");
        }
        if (workbench.getColumnIndex(localityTableId, "longitude1") == -1)
        {
            missingCols.add("longitude1");
        }
        
        // check the geography fields
        int geographyTableId = DBTableIdMgr.getInstance().getIdByClassName(Geography.class.getName());

        if (workbench.getColumnIndex(geographyTableId, "country") == -1)
        {
            missingCols.add("country");
        }
        if (workbench.getColumnIndex(geographyTableId, "state") == -1)
        {
            missingCols.add("state");
        }
        if (workbench.getColumnIndex(geographyTableId, "county") == -1)
        {
            missingCols.add("county");
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
            missingCols.add("latitude1");
        }
        if (workbench.getColumnIndex(localityTableId, "longitude1") == -1)
        {
            missingCols.add("longitude1");
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
    
    protected List<WorkbenchRow> getSelectedRowsFromView()
    {
        // get the indexes into the model for all of the selected rows
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

        // gather all of the WorkbenchRows into a vector
        List<WorkbenchRow> rows = workbench.getWorkbenchRowsAsList();
        final List<WorkbenchRow> selectedRows = new Vector<WorkbenchRow>();
        for (int i: selection)
        {
            selectedRows.add(rows.get(i));
        }
        return selectedRows;
    }
    
    protected void doGeoLocateLookup()
    {
        UsageTracker.incrUsageCount("WB.GeoLocateRows");
        
        log.info("Performing GeoLocate lookup of selected records");
        
        // table IDs and row indexes needed for the GL lookup
        final int    localityNameColIndex = workbench.getColumnIndex(Locality.class, "localityName");
        final int    countryColIndex      = workbench.getColumnIndex(Geography.class, "Country");
        final int    stateColIndex        = workbench.getColumnIndex(Geography.class, "State");
        final int    countyColIndex       = workbench.getColumnIndex(Geography.class, "County");

        //final JStatusBar statusBar = UIRegistry.getStatusBar();
        
        final List<WorkbenchRow> selectedRows = getSelectedRowsFromView();

        // create a progress bar dialog to show the network progress
        final ProgressDialog progressDialog = new ProgressDialog("GEOLocate Progress", false, true); // I18N
        progressDialog.getCloseBtn().setText(getResourceString("Cancel"));
        progressDialog.setModal(true);
        progressDialog.setProcess(0, selectedRows.size());

        // XXX Java 6
        //progressDialog.setIconImage( IconManager.getImage("AppIcon").getImage());

        // create the thread pool for doing the GEOLocate web service requests
        final ExecutorService glExecServ = Executors.newFixedThreadPool(10);
        
        // NOTE:
        // You might think to use a CompletionService to get the completed tasks, as they finish.
        // However, since we want to display the results to the user in the order they appear in the table
        // we don't want a CompletionService.  We can simply wait for each result in order.
        // See "Java Concurrency in Practice" by Brian Goetz, page 129
        // So, instead we keep a List of the Future objects as we schedule the Callable workers.
        final List<Future<Pair<WorkbenchRow,GeorefResultSet>>> runningQueries = new Vector<Future<Pair<WorkbenchRow,GeorefResultSet>>>();
        
        // create the thread pool for pre-caching maps
        final ExecutorService mapGrabExecServ = Executors.newFixedThreadPool(10);
        
        // create individual worker threads to do the GL queries for the rows
        for (WorkbenchRow wbRow: selectedRows)
        {
            final WorkbenchRow row = wbRow;
            
            // create a background thread to do the web service work
            Callable<Pair<WorkbenchRow,GeorefResultSet>> wsClientWorker = new Callable<Pair<WorkbenchRow,GeorefResultSet>>()
            {
                @SuppressWarnings("synthetic-access")
                public Pair<WorkbenchRow,GeorefResultSet> call() throws Exception
                {
                    // get the locality data
                    String localityNameStr      = row.getData(localityNameColIndex);
                            
                    // get the geography data
                    String country = (countryColIndex!=-1) ? row.getData(countryColIndex) : "";
                    String state   = (stateColIndex!=-1) ? row.getData(stateColIndex) : "";
                    String county  = (countyColIndex!=-1) ? row.getData(countyColIndex) : "";
                    
                    // make the web service request
                    log.info("Making call to GEOLocate web service: " + localityNameStr);
                    final GeorefResultSet glResults = GeoLocate.getGeoLocateResults(country, state, county, localityNameStr);

                    // update the progress bar
                    SwingUtilities.invokeLater(new Runnable()
                    {
                       public void run()
                       {
                           int progress = progressDialog.getProcess();
                           progressDialog.setProcess(++progress);
                       }
                    });

                    // if there was at least one result, pre-cache a map for that result
                    if (glResults != null && glResults.getNumResults() > 0)
                    {
                        Runnable mapPreCacheTask = new Runnable()
                        {
                            public void run()
                            {
                                try
                                {
                                    int rowNumber = row.getRowNumber();
                                    log.info("Requesting map of GEOLocate results for workbench row " + rowNumber);
                                    GeoLocate.getMapOfGeographicPoints(glResults.getResultSet(), null);
                                }
                                catch (Exception e)
                                {
                                    log.warn("Failed to pre-cache GEOLocate results map",e);
                                }
                            }
                        };
                        mapGrabExecServ.execute(mapPreCacheTask);
                    }

                    return new Pair<WorkbenchRow,GeorefResultSet>(row,glResults);
                }
            };
            
            runningQueries.add(glExecServ.submit(wsClientWorker));
        }
        
        // shut down the ExecutorService
        // this will run all of the task that have already been submitted
        glExecServ.shutdown();
        
        // this thread simply gets the 'waiting for all results' part off of the Swing thread
        final Thread waitingForExecutors = new Thread(new Runnable()
        {
            public void run()
            {
                // a big list of the query results
                final List<Pair<WorkbenchRow,GeorefResultSet>> glResults = new Vector<Pair<WorkbenchRow,GeorefResultSet>>();
                
                // iterrate over the set of queries, asking for the result
                // this will basically block us right here until all of the queries are completed
                for (Future<Pair<WorkbenchRow,GeorefResultSet>> completedQuery: runningQueries)
                {
                    try
                    {
                        glResults.add(completedQuery.get());
                    }
                    catch (InterruptedException e)
                    {
                        // ignore this query since results were not available
                        System.err.println("Process cancelled by user");
                        mapGrabExecServ.shutdown();
                        return;
                    }
                    catch (ExecutionException e)
                    {
                        // ignore this query since results were not available
                        System.err.println(completedQuery.toString() + " had an execution error");
                    }
                }
                
                // do the UI work to show the results
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        progressDialog.setVisible(false);
                        displayGeoLocateResults(glResults);
                        mapGrabExecServ.shutdown();
                    }
                });
            }
        });
        waitingForExecutors.setName("GEOLocate UI update thread");
        waitingForExecutors.start();
        
        // if the user hits close, stop the worker thread
        progressDialog.getCloseBtn().addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent ae)
            {
                log.debug("Stopping the GEOLocate service worker threads");
                glExecServ.shutdownNow();
                mapGrabExecServ.shutdownNow();
                waitingForExecutors.interrupt();
            }
        });

        // popup the progress dialog
        UIHelper.centerAndShow(progressDialog);
    }
    
    /**
     * Create a dialog to display the set of rows that had at least one result option
     * returned by GEOLocate.  The dialog allows the user to iterate through the
     * records supplied, choosing a result (or not) for each one.
     * 
     * @param rows the set of records containing valid GEOLocate responses with at least one result
     */
    protected void displayGeoLocateResults(List<Pair<WorkbenchRow,GeorefResultSet>> glResults)
    {
        final JStatusBar statusBar = UIRegistry.getStatusBar();
        
        List<Pair<WorkbenchRow,GeorefResultSet>> withResults = new Vector<Pair<WorkbenchRow,GeorefResultSet>>();

        for (Pair<WorkbenchRow,GeorefResultSet> result: glResults)
        {
            if (result.second.getNumResults() > 0)
            {
                withResults.add(result);
            }
        }
        
        if (withResults.size() == 0)
        {
            statusBar.setText(getResourceString("NO_GL_RESULTS"));
            return;
        }
        
        // ask the user if they want to review the results
        // TODO: i18n
        // XXX: i18n
        String message = "GEOLocate returned results for " + withResults.size()
                + " records.  Would you like to view them now?";
        int userChoice = JOptionPane.showConfirmDialog(WorkbenchPaneSS.this, message,
                "Continue?", JOptionPane.YES_NO_OPTION);
        if (userChoice != JOptionPane.OK_OPTION)
        {
            statusBar.setText("GEOLocate process terminated by user");
            return;
        }

        // create the UI for displaying the BG results
        JFrame topFrame = (JFrame)UIRegistry.getTopWindow();
        GeoLocateResultsChooser bgResChooser = new GeoLocateResultsChooser(topFrame,"GEOLocate Results Chooser",withResults);
        
        List<GeorefResult> results = bgResChooser.getResultsChosen();
        
        // get the latitude1 and longitude1 column indices
        int localityTableId = DBTableIdMgr.getInstance().getIdByClassName(Locality.class.getName());
        int latIndex        = workbench.getColumnIndex(localityTableId, "latitude1");
        int lonIndex        = workbench.getColumnIndex(localityTableId, "longitude1");

        for (int i = 0; i < results.size(); ++i)
        {
            WorkbenchRow row = withResults.get(i).first;
            GeorefResult chosenResult = results.get(i);
            
            if (chosenResult != null)
            {
                row.setData(Double.toString(chosenResult.getWGS84Coordinate().getLatitude()), (short)latIndex);
                row.setData(Double.toString(chosenResult.getWGS84Coordinate().getLongitude()), (short)lonIndex);
                
                setChanged(true);
            }
        }
        spreadSheet.repaint();
    }

    /**
     * Use the BioGeomancer web service to lookup georeferences for the selected records.
     */
    protected void doBioGeomancerLookup()
    {
        UsageTracker.incrUsageCount("WB.BioGeomancerRows");
        
        log.info("Performing BioGeomancer lookup of selected records");
        
        // table IDs and row indexes needed for the BG lookup
        final int    localityTableId      = DBTableIdMgr.getInstance().getIdByClassName(Locality.class.getName());
        final int    localityNameColIndex = workbench.getColumnIndex(localityTableId, "localityName");
        final int    geographyTableId = DBTableIdMgr.getInstance().getIdByClassName(Geography.class.getName());
        final int    countryColIndex  = workbench.getColumnIndex(geographyTableId, "Country");
        final int    stateColIndex    = workbench.getColumnIndex(geographyTableId, "State");
        final int    countyColIndex   = workbench.getColumnIndex(geographyTableId, "County");

        final List<WorkbenchRow> selectedRows = getSelectedRowsFromView();
        
        // create a progress bar dialog to show the network progress
        final ProgressDialog progressDialog = new ProgressDialog("BioGeomancer Progress", false, true); // I18N
        progressDialog.getCloseBtn().setText(getResourceString("Cancel"));
        progressDialog.setModal(true);
        progressDialog.setProcess(0, selectedRows.size());
        
        // XXX Java 6
        //progressDialog.setIconImage( IconManager.getImage("AppIcon").getImage());

        // use a SwingWorker thread to do all of the work, and update the GUI when done
        final SwingWorker bgTask = new SwingWorker()
        {
            final JStatusBar statusBar = UIRegistry.getStatusBar();
            protected boolean cancelled = false;
            
            @Override
            public void interrupt()
            {
                super.interrupt();
                cancelled = true;
            }
                        
            @SuppressWarnings("synthetic-access")
            @Override
            public Object construct()
            {
                // TODO: perform the BG web service call ON all rows, storing results in the rows
                
                int progress = 0;

                for (WorkbenchRow row: selectedRows)
                {
                    if (cancelled)
                    {
                        break;
                    }
                    
                    // get the locality data
                    String localityNameStr      = row.getData(localityNameColIndex);
                            
                    // get the geography data
                    String country = (countryColIndex!=-1) ? row.getData(countryColIndex) : "";
                    String state   = (stateColIndex!=-1) ? row.getData(stateColIndex) : "";
                    String county  = (countyColIndex!=-1) ? row.getData(countyColIndex) : "";
                    
                    log.info("Making call to BioGeomancer service: " + localityNameStr);
                    String bgResults;
                    BioGeomancerQuerySummaryStruct bgQuerySummary;
                    try
                    {
                        bgResults = BioGeomancer.getBioGeomancerResponse(row.getWorkbenchRowId().toString(), country, state, county, localityNameStr);
                        bgQuerySummary = BioGeomancer.parseBioGeomancerResponse(bgResults);
                    }
                    catch (IOException ex1)
                    {
                        statusBar.setWarningMessage("Network error while contacting BioGeomancer", ex1);
                        log.error("A network error occurred while contacting the BioGeomancer service", ex1);
                        
                        // update the progress bar UI and move on
                        progressDialog.setProcess(++progress);
                        continue;
                    }
                    catch (Exception ex2)
                    {
                        statusBar.setWarningMessage("Error while parsing BioGeomancer results", ex2);
                        log.warn("Failed to get result count from BioGeomancer respsonse", ex2);
                        
                        // update the progress bar UI and move on
                        progressDialog.setProcess(++progress);
                        continue;
                    }

                    // if there was at least one result, pre-cache a map for that result
                    int resCount = bgQuerySummary.results.length;
                    if (resCount > 0)
                    {
                        final int rowNumber = row.getRowNumber();
                        final BioGeomancerQuerySummaryStruct summaryStruct = bgQuerySummary;
                        // create a thread to go grab the map so it will be cached for later use
                        Thread t = new Thread(new Runnable()
                        {
                            public void run()
                            {
                                try
                                {
                                    log.info("Requesting map of BioGeomancer results for workbench row " + rowNumber);
                                    BioGeomancer.getMapOfQuerySummary(summaryStruct, null);
                                }
                                catch (Exception e)
                                {
                                    log.warn("Failed to pre-cache BioGeomancer results map",e);
                                }
                            }
                        });
                        t.setName("Map Pre-Caching Thread: row " + row.getRowNumber());
                        log.debug("Starting map pre-caching thread");
                        t.start();
                    }
                    
                    // if we got at least one result...
                    if (resCount > 0)
                    {
                        // everything must have worked and returned at least 1 result
                        row.setBioGeomancerResults(bgResults);
                        setChanged(true);
                    }
                    
                    // update the progress bar UI and move on
                    progressDialog.setProcess(++progress);
                }
                
                return null;
            }
        
            @Override
            public void finished()
            {
                if (!cancelled)
                {
                    // hide the progress dialog
                    progressDialog.setVisible(false);

                    // find out how many records actually had results
                    List<WorkbenchRow> rowsWithResults = new Vector<WorkbenchRow>();
                    for (WorkbenchRow row : selectedRows)
                    {
                        if (row.getBioGeomancerResults() != null)
                        {
                            rowsWithResults.add(row);
                        }
                    }

                    // if no records had possible results...
                    int numRecordsWithResults = rowsWithResults.size();
                    if (numRecordsWithResults == 0)
                    {
                        statusBar.setText(getResourceString("NO_BG_RESULTS"));
//                        JOptionPane.showMessageDialog(UIRegistry.getTopWindow(),
//                                getResourceString("NO_BG_RESULTS"),
//                                getResourceString("NO_RESULTS"), JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    // ask the user if they want to review the results
                    // TODO: i18n
                    // XXX: i18n
                    String message = "BioGeomancer returned results for " + numRecordsWithResults
                            + " records.  Would you like to view them now?";
                    int userChoice = JOptionPane.showConfirmDialog(WorkbenchPaneSS.this, message,
                            "Continue?", JOptionPane.YES_NO_OPTION);
                    if (userChoice != JOptionPane.OK_OPTION)
                    {
                        statusBar.setText("BioGeomancer process terminated by user");
                        return;
                    }

                    displayBioGeomancerResults(rowsWithResults);
                }
            }
        };
        
        // if the user hits close, stop the worker thread
        progressDialog.getCloseBtn().addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent ae)
            {
                log.debug("Stopping the BioGeomancer service worker thread");
                bgTask.interrupt();
            }
        });
        
        log.debug("Starting the BioGeomancer service worker thread");
        bgTask.start();
        UIHelper.centerAndShow(progressDialog);
    }
    
    /**
     * Given the BioGeomancer XML response (as a String), determines the
     * number of possible georeference results returned.
     * 
     * @param bgResponse the XML response from BioGeomancer
     * @return the number of possible georeference results
     * @throws Exception if the XML cannot be parsed
     */
    public int getBioGeomancerResultCount(final String bgResponse) throws Exception
    {
        if (bgResponse == null)
        {
            return 0;
        }
        
        try
        {
            return BioGeomancer.getResultsCount(bgResponse);
        }
        catch (Exception e)
        {
            throw new Exception("Unable to determine number of results from BioGeomancer service", e);
        }
    }
    
    /**
     * Create a dialog to display the set of rows that had at least one result option
     * returned by BioGeomancer.  The dialog allows the user to iterate through the
     * records supplied, choosing a result (or not) for each one.
     * 
     * @param rows the set of records containing valid BioGeomancer responses with at least one result
     */
    protected void displayBioGeomancerResults(List<WorkbenchRow> rows)
    {
        // create the UI for displaying the BG results
        JFrame topFrame = (JFrame)UIRegistry.getTopWindow();
        BioGeomancerResultsChooser bgResChooser = new BioGeomancerResultsChooser(topFrame,"BioGeomancer Results Chooser",rows);
        
        List<BioGeomancerResultStruct> results = bgResChooser.getResultsChosen();
        
        // get the latitude1 and longitude1 column indices
        int localityTableId = DBTableIdMgr.getInstance().getIdByClassName(Locality.class.getName());
        int latIndex        = workbench.getColumnIndex(localityTableId, "latitude1");
        int lonIndex        = workbench.getColumnIndex(localityTableId, "longitude1");

        for (int i = 0; i < rows.size(); ++i)
        {
            WorkbenchRow row = rows.get(i);
            BioGeomancerResultStruct userChoice = results.get(i);
            
            if (userChoice != null)
            {
                //System.out.println(userChoice.coordinates);
                
                String[] coords = StringUtils.split(userChoice.coordinates);

                row.setData(coords[1], (short)latIndex);
                row.setData(coords[0], (short)lonIndex);
                
                setChanged(true);
            }
        }
        spreadSheet.repaint();
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
        
        for (int i = 0; i < tableArg.getColumnCount(); i++) 
        {
            WorkbenchTemplateMappingItem wbtmi = wbtmis.elementAt(i);
            GridCellEditor cellEditor = new GridCellEditor(new JTextField(), wbtmi.getCaption(), wbtmi.getDataFieldLength(), theSaveBtn);
            
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
        
        Collections.sort(items);
        ToggleButtonChooserDlg<WorkbenchTemplateMappingItem> dlg = new ToggleButtonChooserDlg<WorkbenchTemplateMappingItem>((Frame)UIRegistry.get(UIRegistry.FRAME),
                                                                        "WB_CARRYFORWARD",
                                                                        "WB_CHOOSE_CARRYFORWARD", 
                                                                        items,
                                                                        null,
                                                                        CustomDialog.OKCANCELHELP,
                                                                        ToggleButtonChooserDlg.Type.Checkbox);
        
        dlg.setHelpContext(currentPanelType == PanelType.Spreadsheet ? "WorkbenchGridEditingCF" : "WorkbenchFormEditingCF");
        dlg.setAddSelectAll(true);
        dlg.setSelectedObjects(selectedObjects);
        dlg.setModal(true);
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
        WorkbenchBackupMgr.backupWorkbench(workbench.getId(), (WorkbenchTask) task);
    }
    
    /**
     * Save the Data. 
     */
    protected void saveObject()
    {
        //backup current database contents for workbench
        backupObject();
        
        checkCurrentEditState();
        
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            FormHelper.updateLastEdittedInfo(workbench);
            
            // Delete the cached Items
            Vector<WorkbenchRow> deletedItems = workbench.getDeletedRows();
            if (deletedItems != null && deletedItems.size() > 0)
            {
                session.beginTransaction();
                for (Object obj : deletedItems)
                {
                    session.delete(obj);
                }
                deletedItems.clear();
                session.commit();
                session.flush();
                session.close();
                
                session = DataProviderFactory.getInstance().createSession();
            }
            
            session.beginTransaction();

            // DEBUG
            /*for (WorkbenchRow row : workbench.getWorkbenchRowsAsList())
            {
                for (WorkbenchDataItem item : row.getWorkbenchDataItems())
                {
                    System.out.println("["+item.getCellData()+"]");
                }
            }*/
            
            Object dObj = session.merge(workbench);
            
            /*// DEBUG
            for (WorkbenchRow row : ((Workbench)dObj).getWorkbenchRowsAsList())
            {
                for (WorkbenchDataItem item : row.getWorkbenchDataItems())
                {
                    System.out.println("["+item.getCellData()+"]");
                }
            }*/
            session.saveOrUpdate(dObj);
            session.commit();
            session.flush();

            workbench = (Workbench)dObj;
            workbench.forceLoad();
            
            model.setWorkbench(workbench);
            formPane.setWorkbench(workbench);
            
            log.info("Session Saved[ and Flushed "+session.hashCode()+"]");
           
            hasChanged = false;
            
            String msg = String.format(getResourceString("WB_SAVED"), new Object[] { workbench.getName()} );
            UIRegistry.getStatusBar().setText(msg);


        } catch (StaleObjectException ex) // was StaleObjectStateException
        {
            session.rollback();
            
            // 
            //recoverFromStaleObject("UPDATE_DATA_STALE");
            UIRegistry.getStatusBar().setErrorMessage(getResourceString("WB_ERROR_SAVING"), ex);

            
        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            session.rollback();
            UIRegistry.getStatusBar().setErrorMessage(getResourceString("WB_ERROR_SAVING"), ex);

        }
        
        if (saveBtn != null)
        {
            saveBtn.setEnabled(false);
        }

        session.close();
        session = null;
        
    }
    
    /**
     * Checks to see if the current item has changed and asks if it should be saved
     * @return true to continue false to stop
     */
    public boolean checkForChanges()
    {
        if (hasChanged)
        {
            JFrame topFrame = (JFrame)UIRegistry.getTopWindow();
            int rv = JOptionPane.showConfirmDialog(topFrame,
                        getResourceString("SaveChanges"),
                        getResourceString("SaveChangesTitle"),
                        JOptionPane.YES_NO_CANCEL_OPTION);

            if (rv == JOptionPane.YES_OPTION)
            {
                saveObject();

            } else if (rv == JOptionPane.CANCEL_OPTION)
            {
                return false;
                
            } else if (rv == JOptionPane.NO_OPTION)
            {
                // Check to see if we are cancelling a new object or a previously saved object
                // if the object is part of this Session then anychanges were already saved.
                // If it is NOT part of this session then some of the object may not have been save.
                
                /* XYZ THIS NEEDS TO BE REWORKED
                if (!session.contains(dataObj))
                {
                    if (businessRules != null)
                    {
                        List<BusinessRulesDataItem> dataToSaveList = businessRules.getStandAloneDataItems(dataObj);
                        if (dataToSaveList.size() > 0)
                        {
                            ToggleButtonChooserDlg<BusinessRulesDataItem> dlg = new ToggleButtonChooserDlg<BusinessRulesDataItem>("Save", "Check the items you would like to have saved.", dataToSaveList);
                            UIHelper.centerAndShow(dlg);
                            dataToSaveList = dlg.getSelectedObjects();
                            for (BusinessRulesDataItem item : dataToSaveList)
                            {
                                item.setChecked(true);
                            }
                            businessRules.saveStandAloneData(dataObj, dataToSaveList);
                        }
                    }
                }*/
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#aboutToShutdown()
     */
    @Override
    public boolean aboutToShutdown()
    {
        super.aboutToShutdown();
        
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
        
        boolean retStatus = true;
        if (hasChanged)
        {
            JFrame topFrame = (JFrame)UIRegistry.getTopWindow();
            int rv = JOptionPane.showConfirmDialog(topFrame,
                                                   getResourceString("SaveChanges"),
                                                   getResourceString("SaveChangesTitle"),
                                                   JOptionPane.YES_NO_CANCEL_OPTION);
            if (rv == JOptionPane.YES_OPTION)
            {
                saveObject();
            }
            else if (rv == JOptionPane.CANCEL_OPTION)
            {
                return false;
            }
            else if (rv == JOptionPane.NO_OPTION)
            {
                // nothing
            }
        }
        
        if (formPane != null)
        {
            formPane.cleanup();
        }
        

        if (retStatus)
        {
            ((WorkbenchTask)task).closing(this);
            //UIRegistry.unregisterAction(workbench.getName()+"_AddRow");
            
            imageFrame.dispose();
            mapFrame.dispose();
        }
        
        return retStatus;
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
            recordSet = new RecordSet(workbench.getName(), Workbench.getClassTableId());
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

    //------------------------------------------------------------
    // Inner Classes
    //------------------------------------------------------------

    class GridCellEditor extends DefaultCellEditor implements TableCellEditor//, UndoableTextIFace
    {
        protected JTextField          textField;
        protected int                 length;
        protected LengthInputVerifier verifier;
        protected JButton             saveBtn;
        
        //protected UndoManager undoManager = new UndoManager();

        public GridCellEditor(final JTextField textField, final String caption, final int length, final JButton saveBtn)
        {
            super(textField);
            this.textField = textField;
            this.length    = length;
            this.saveBtn   = saveBtn;
            
            verifier = new LengthInputVerifier(caption, length);
            textField.setInputVerifier(verifier);

            textField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            
            textField.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e)
                {
                    validateDoc();
                }
                
                public void insertUpdate(DocumentEvent e)
                {
                    validateDoc();
                }
                
                public void removeUpdate(DocumentEvent e) 
                {
                    validateDoc();
                }
            });
        }
        
        /**
         * Makes sure the document is the correct length.
         */
        protected void validateDoc()
        {
            if (!verifier.verify(textField))
            {
                saveBtn.setEnabled(false);
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.DefaultCellEditor#stopCellEditing()
         */
        @Override
        public boolean stopCellEditing()
        {
            if (!verifier.verify(textField))
            {
                saveBtn.setEnabled(false);
                return false;
            }
            return super.stopCellEditing();
        }

        /* (non-Javadoc)
         * @see javax.swing.CellEditor#getCellEditorValue()
         */
        @Override
        public Object getCellEditorValue() 
        {
            return textField.getText();
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
            // location to insert the cursor
            if (table.getCellRenderer(row, column) instanceof JComponent)
            {
                JComponent jcomp = (JComponent)table.getCellRenderer(row, column);
                Font cellFont = jcomp.getFont();
                Font txtFont  = textField.getFont();
                if (cellFont != txtFont)
                {
                    textField.setFont(cellFont);
                }
            }            
            
            textField.setText(value != null ? value.toString() : "");
            try
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {            
                        Caret c = textField.getCaret();

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

            return textField;
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
            return textField;
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
            } else
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
}

