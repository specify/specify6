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

import static edu.ku.brc.ui.UICacheManager.getResourceString;
import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.createIconBtn;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

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
import javax.swing.JDialog;
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
import javax.swing.WindowConstants;
import javax.swing.border.Border;
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.StaleObjectException;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
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
import edu.ku.brc.specify.plugins.BioGeoMancer;
import edu.ku.brc.specify.tasks.ExportTask;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.tasks.services.LocalityMapper;
import edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener;
import edu.ku.brc.ui.ChooseFromListDlg;
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
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.ToggleButtonChooserDlg.Type;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.ui.forms.ResultSetController;
import edu.ku.brc.ui.tmanfe.SpreadSheet;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.StringConverter;
import edu.ku.brc.util.GeoRefConverter.GeoRefFormat;

/**
 * Main class that handles the editing of Workbench data. It creates both a spreasheet and a form pane for editing the data.
 * 
 * @author rods
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
    protected String[]              columns;
    protected Vector<WorkbenchTemplateMappingItem> headers = new Vector<WorkbenchTemplateMappingItem>();
    protected boolean               hasChanged             = false;
    protected boolean               blockChanges           = false;
    protected RecordSet             recordSet              = null;
    protected boolean               bgmCompatible          = false;
    
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
    
    protected CardImageFrame        imageFrame                 = null;
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
    public WorkbenchPaneSS(final String name,
                           final Taskable task,
                           final Workbench workbench,
                           final boolean showImageView)
    {
        super(name, task);
        
        removeAll();
        
        if (workbench == null)
        {
            return;
        }
        this.workbench = workbench;
        
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
            
            if (!hasOneOrMoreImages && !showImageView && wbRow.getCardImage() != null)
            {
                hasOneOrMoreImages = true;
            }
        } 
        
        model       = new GridTableModel(workbench, headers);
        spreadSheet = new SpreadSheet(model);
        model.setSpreadSheet(spreadSheet);
        
        findPanel = spreadSheet.getFindReplacePanel();
        UICacheManager.getLaunchFindReplaceAction().setSearchReplacePanel(findPanel);
    
        initColumnSizes(spreadSheet);
        spreadSheet.setShowGrid(true);
        
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e)
            {
                setChanged(true);
            }
        });
        
        saveBtn = new JButton(UICacheManager.getResourceString("Save"));
        saveBtn.setEnabled(false);
        saveBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                
                SaveProgressDialog progressDialog = new SaveProgressDialog(getResourceString("Save"), false, false);
                progressDialog.setAlwaysOnTop(true);
                progressDialog.getProcessProgress().setString("");
                progressDialog.getProcessProgress().setIndeterminate(true);
                UIHelper.centerAndShow(progressDialog);
            }
        });
       
        ActionListener deleteAction = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                deleteRows();
            }
        };
        deleteRowsBtn = createIconBtn("MinusSign", "WB_DELETE_ROW", deleteAction);
        selectionSensativeButtons.add(deleteRowsBtn);
        
        clearCellsBtn = createIconBtn("Eraser", "WB_CLEAR_CELLS", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                model.clearCells(spreadSheet.getSelectedRowModelIndexes(), spreadSheet.getSelectedColumnModelIndexes());
            }
        });
        selectionSensativeButtons.add(clearCellsBtn);
        
        Action addAction = addRecordKeyMappings(spreadSheet, KeyEvent.VK_N, "AddRow", new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                addRowAfter();
            }
        });
        addRowsBtn = createIconBtn("PlusSign", "WB_ADD_ROW", addAction);
        addRowsBtn.setEnabled(true);
        addAction.setEnabled(true); 


        carryForwardBtn = createIconBtn("CarryForward", IconManager.IconSize.Std16, "WB_CARRYFORWARD", true, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                configCarryFoward();
            }
        });
        carryForwardBtn.setEnabled(true);

        toggleImageFrameBtn = createIconBtn("CardImage", IconManager.IconSize.Std16, "WB_SHOW_IMAGES", true, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                toggleImageFrameVisible();
            }
        });
        toggleImageFrameBtn.setEnabled(true);
        
        showMapBtn = createIconBtn("ShowMap", IconManager.IconSize.Std16, "WB_SHOW_MAP", true, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showMapOfSelectedRecords();
            }
        });
        // only enable it if the workbench has geo ref data
        showMapBtn.setEnabled(workbench.containsGeoRefData());

        exportKmlBtn = createIconBtn("GoogleEarth", IconManager.IconSize.Std16, "WB_SHOW_IN_GOOGLE_EARTH", true, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showRecordsInGoogleEarth();
            }
        });
        exportKmlBtn.setEnabled(workbench.containsGeoRefData());
        
        biogeomancerBtn = createIconBtn("BioGeoMancer", IconManager.IconSize.Std16, "WB_DO_BIOGEOMANCER_LOOKUP", true, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                doBioGeomancerLookup();
            }
        });
        selectionSensativeButtons.add(biogeomancerBtn);
        bgmCompatible = isTemplateBGMCompatible();
        
        convertGeoRefFormatBtn = createIconBtn("ConvertGeoRef", IconManager.IconSize.Std16, "WB_CONVERT_GEO_FORMAT", true, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showGeoRefConvertDialog();
            }
        });
        convertGeoRefFormatBtn.setEnabled(true);
        
        exportExcelCsvBtn = createIconBtn("Export", IconManager.IconSize.Std16, "WB_EXPORT_DATA", true, new ActionListener()
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
                    JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
                    statusBar.setText("");
                    
                    currentRow = spreadSheet.getSelectedRow();
                    updateBtnUI();
                }
            }
        });
        
        // setup the JFrame to show images attached to WorkbenchRows
        imageFrame = new CardImageFrame(mapSize);
        imageFrame.installLoadActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                // figure out what row is selected
                int firstRowSelected = spreadSheet.getSelectedRow();
                WorkbenchRow row = workbench.getWorkbenchRowsAsList().get(firstRowSelected);
                // then load a new image for it
                boolean loaded = loadNewCardImage(row);
                if (loaded)
                {
                    showCardImageForSelectedRow();
                    setChanged(true);
                }
            }
        });
        
        imageFrame.installClearActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                // figure out what row is selected
                int firstRowSelected = spreadSheet.getSelectedRow();
                WorkbenchRow row = workbench.getWorkbenchRowsAsList().get(firstRowSelected);
                row.setCardImage((File)null);
            }
        });
        imageFrame.installCloseActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                toggleImageFrameVisible();
            }
        });
        imageFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                toggleImageFrameVisible();
            }
        });
        
        setupWorkbenchRowChangeListener();
                
        // setup the mapping features
        mapFrame = new JFrame();
        mapFrame.setTitle(getResourceString("WB_GEO_REF_DATA_MAP"));
        mapImageLabel = new JLabel();
        mapImageLabel.setSize(500,500);
        mapFrame.add(mapImageLabel);
        mapFrame.setSize(500,500);
        
        // start putting together the visible UI
        CellConstraints cc = new CellConstraints();

        JComponent[] comps      = {addRowsBtn, clearCellsBtn, deleteRowsBtn, showMapBtn, exportKmlBtn, biogeomancerBtn, convertGeoRefFormatBtn, exportExcelCsvBtn};
        PanelBuilder spreadSheetControlBar = new PanelBuilder(new FormLayout("f:p:g,4px,"+createDuplicateJGoodiesDef("p", "4px", comps.length)+",4px,", "p:g"));
        
        spreadSheetControlBar.add(findPanel, cc.xy(1, 1));
        int x = 3;
        for (JComponent c : comps)
        {
            spreadSheetControlBar.add(c, cc.xy(x,1));
            x += 2;
        }
        
        // Create the main panel that uses card layout for the form and spreasheet
        mainPanel = new JPanel(cardLayout = new CardLayout());
        
        // Create the Form Pane
        formPane = new FormPane(this, workbench);
        
        // This panel is a single row containing the ResultSetContoller and the other controls for the Form Panel  
        PanelBuilder outerRSPanel = new PanelBuilder(new FormLayout("f:p:g, p, f:p:g, p", "p"));
        
        // This panel contains just the ResultSetContoller, it's needed so the RSC gets centered
        PanelBuilder rsPanel = new PanelBuilder(new FormLayout("c:p:g", "p"));
        resultsetController  = new ResultSetController(null, true, true, getResourceString("Record"), model.getRowCount());
        resultsetController.addListener(formPane);
        resultsetController.getDelRecBtn().addActionListener(deleteAction);
        rsPanel.add(resultsetController.getPanel(), cc.xy(1,1));
        
        // Now put the two panel into the single row panel
        outerRSPanel.add(rsPanel.getPanel(), cc.xy(2,1));
        outerRSPanel.add(formPane.getControlPropsBtn(), cc.xy(4,1));
        
        // Add the Form and Spreadsheet to the CardLayout
        mainPanel.add(spreadSheet.getScrollPane(), PanelType.Spreadsheet.toString());
        mainPanel.add(formPane.getScrollPane(), PanelType.Form.toString());
        
        // The controllerPane is a CardLayout that switches between the Spreadsheet control bar and the Form Control Bar
        controllerPane = new JPanel(cpCardLayout = new CardLayout());
        controllerPane.add(spreadSheetControlBar.getPanel(), PanelType.Spreadsheet.toString());
        controllerPane.add(outerRSPanel.getPanel(), PanelType.Form.toString());
        
        if (false) // This does work JGoodies is messing something up
        {
            // This is the main layout panel it has two rows (really 3 but the middle is just a spacer)
            // one row for the mainPanel which is a CardLayout for the Form and Spreadsheet
            FormLayout      formLayout = new FormLayout("f:p:g,4px,p,4px,p,4px,p,4px,p", "f:p:g, 5px, p");
            PanelBuilder    builder    = new PanelBuilder(formLayout, this);
    
            builder.add(mainPanel,          cc.xywh(1,1,9,1)); // Row #1
            builder.add(controllerPane,     cc.xy(1,3));       // Row #2
            builder.add(toggleImageFrameBtn, cc.xy(3,3));
            builder.add(carryForwardBtn,    cc.xy(5,3));
            builder.add(saveBtn,            cc.xy(7,3));
            builder.add(createSwitcher(),   cc.xy(9,3));
        } else
        {
            // This works
            setLayout(new BorderLayout());
            FormLayout      formLayout = new FormLayout("f:p:g,4px,p,4px,p,4px,p,4px,p", "f:p:g, 5px, p");
            PanelBuilder    builder    = new PanelBuilder(formLayout);

            add(mainPanel, BorderLayout.CENTER);
            
            builder.add(controllerPane,     cc.xy(1,3));
            builder.add(toggleImageFrameBtn, cc.xy(3,3));
            builder.add(carryForwardBtn,    cc.xy(5,3));
            builder.add(saveBtn,            cc.xy(7,3));
            builder.add(createSwitcher(),   cc.xy(9,3));
            add(builder.getPanel(), BorderLayout.SOUTH);
        }
        
        // See if we need to make the Image Frame visible
        if (showImageView || hasOneOrMoreImages)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    toggleImageFrameVisible();
                    ((Frame)UICacheManager.get(UICacheManager.FRAME)).toFront();
                }
            });
        }
    }
    
    /**
     * Checks the cell for cell editing and stops it.
     */
    protected void checkForCellEditing()
    {
        if (spreadSheet.getCellEditor() != null)
        {
            int index = spreadSheet.getSelectedRow();
            spreadSheet.getCellEditor().stopCellEditing();
            spreadSheet.setRowSelectionInterval(index, index);
        }
    }
    
    protected void updateBtnUI()
    {
        boolean enable = spreadSheet.getSelectedRow() > -1;
        for (JButton btn: selectionSensativeButtons)
        {
            btn.setEnabled(enable);
        }
        
        if (biogeomancerBtn.isEnabled())
        {
            biogeomancerBtn.setEnabled(bgmCompatible);
        }
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
        
        //UICacheManager.registerAction(actionName, action);
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
        checkForCellEditing();
        model.appendRow();
        resultsetController.setLength(model.getRowCount());
        int selInx = model.getRowCount()-1;
        resultsetController.setIndex(selInx);
        spreadSheet.getSelectionModel().setSelectionInterval(selInx, selInx);
        
        updateBtnUI();
    }
    
    /**
     * Adds a row after the selection. 
     */
    protected void addRowAfter()
    {
        checkForCellEditing();
        
        int curSelInx = getCurrentIndexFromFormOrSS();
        model.insertAfterRow(curSelInx);
        resultsetController.setLength(model.getRowCount());
        
        int newInx = curSelInx  + 1;
        resultsetController.setIndex(newInx);
        spreadSheet.getSelectionModel().setSelectionInterval(newInx, newInx);
        
        updateBtnUI();
    }
    
    /**
     * Inserts a Row above the selection. 
     */
    protected void insertRowAbove()
    {
        checkForCellEditing();
        int curSelInx = getCurrentIndexFromFormOrSS();
        model.insertRow(curSelInx);
        resultsetController.setLength(model.getRowCount());
        int newInx = curSelInx == -1 ? model.getRowCount()-1 : curSelInx;
        resultsetController.setIndex(newInx);
        spreadSheet.getSelectionModel().setSelectionInterval(newInx, newInx);
        
        updateBtnUI();

    }
    
    /**
     * Deletes the Selected Rows. 
     */
    protected void deleteRows()
    {
        checkForCellEditing();
        
        int[] rows = spreadSheet.getSelectedRowModelIndexes();
        model.deleteRows(spreadSheet.getSelectedRowModelIndexes());

        resultsetController.setLength(model.getRowCount());
        
        int rowCount = spreadSheet.getRowCount();
        currentRow   = rowCount > 0 ? (rows[0] > rowCount ? rowCount : rows[0]) : -1;
        resultsetController.setIndex(currentRow);
        spreadSheet.getSelectionModel().setSelectionInterval(currentRow, currentRow);
        
        updateBtnUI();

    }
    
    /**
     * Tells the GridModel to update itself. 
     */
    public void gridColumnsUpdated()
    {
        model.fireTableStructureChanged();
    }
    
    /**
     * Show image for a selected row. 
     */
    protected void showCardImageForSelectedRow()
    {
        int firstRowSelected = spreadSheet.getSelectedRow();
        if (firstRowSelected == -1)
        {
            // no selection
            log.debug("No selection, so removing the card image");
            imageFrame.setRow(null);
            return;
        }
        // else

        log.debug("Showing image for row " + firstRowSelected);
        WorkbenchRow row = workbench.getWorkbenchRowsAsList().get(firstRowSelected);
        imageFrame.setRow(row);

        // XXX Change later - Assuming first Row
        WorkbenchDataItem firstColItem = row.getItems().get(0);
        String firstColCellData = (firstColItem != null) ? firstColItem.getCellData() : "";
        imageFrame.setTitle("Row " + (firstRowSelected+1) + ": " + firstColCellData);
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
                                            IconManager.getImage("EditForm", IconManager.IconSize.Std16), 
                                            getResourceString("ShowEditViewTT")));
        menuItems.add(new DropDownMenuInfo(getResourceString("Grid"), 
                                            IconManager.getImage("Spreadsheet", IconManager.IconSize.Std16), 
                                            getResourceString("ShowSpreadsheetTT")));
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
    protected int getCurrentIndexFromFormOrSS()
    {
        int selectedInx = currentPanelType == PanelType.Spreadsheet ? spreadSheet.getSelectedRow() : resultsetController.getCurrentIndex();
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
    
    /**
     * Shows the grid or the form.
     * @param value the panel number
     */
    public void showPanel(final PanelType panelType)
    {
        currentRow = getCurrentIndexFromFormOrSS();
        
        currentPanelType = panelType;
        
        if (imageFrame != null)
        {
            imageFrame.setHelpContext(panelType == PanelType.Spreadsheet ? "OnRampGridImageWindow" : "OnRampFormImageWindow");
        }
        
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
                spreadSheet.setColumnSelectionInterval(0, model.getColumnCount()-1);
                spreadSheet.scrollToRow(Math.min(currentRow+4, model.getRowCount()));
            }

        } else
        {
            // About to Show Form and hiding Spreadsheet
            
            // cancel any editing in a cell in the spreadsheet
            checkForCellEditing(); 
            
            // Tell the form we are switching and that it is about to be shown
            formPane.aboutToShowHide(true);
            
            if (model.getRowCount() > 0)
            {
                resultsetController.setIndex(currentRow);
            }
            
            // Hide the find/replace panel when you switch to form view
            findPanel.getHideFindPanelAction().hide();
            
            // Disable the ctrl-F from the edit menu
            UICacheManager.disableFindFromEditMenu();

        }
        
             
        JComponent[] comps = { addRowsBtn, clearCellsBtn, deleteRowsBtn};
        for (JComponent c : comps)
        {
            // Enable the "Find" action in the Edit menu when a spreadsheet is shown
            if (isSpreadsheet)
            {
                UICacheManager.enableFindinEditMenu(findPanel);
            }
            c.setVisible(isSpreadsheet);
        }
    }
    
    /**
     * Shows / Hides the Image Window. 
     */
    public void toggleImageFrameVisible()
    {
        // We simply have to toggle the visibility
        // and add or remove the ListSelectionListener (to avoid loading images when not visible)
        boolean visible = imageFrame.isVisible();
        if (visible)
        {
            toggleImageFrameBtn.setToolTipText(getResourceString("WB_SHOW_IMAGES"));
            spreadSheet.getSelectionModel().removeListSelectionListener(workbenchRowChangeListener);
            imageFrame.setVisible(false);
            blockChanges = true;
            // get the selection before the changes
            int[] selRows = spreadSheet.getSelectedRows();
            int[] selCols = spreadSheet.getSelectedColumns();
            model.setInImageMode(false);
            // then restore the selection
            for (int selRow: selRows)
            {
                spreadSheet.getSelectionModel().addSelectionInterval(selRow, selRow);
            }
            for (int selCol: selCols)
            {
                spreadSheet.getColumnModel().getSelectionModel().addSelectionInterval(selCol, selCol);
            }

            blockChanges = false;

        }
        else
        {
            // when a user hits the "show image" button, for some reason the selection gets nullified
            // so we'll grab it here, then set it at the end of this method

            toggleImageFrameBtn.setToolTipText(getResourceString("WB_HIDE_IMAGES"));
            spreadSheet.getSelectionModel().addListSelectionListener(workbenchRowChangeListener);
            imageFrame.setHelpContext(currentPanelType == PanelType.Spreadsheet ? "OnRampGridImageWindow" : "OnRampFormImageWindow");
            imageFrame.setVisible(true);
            
            // tell the table model to show the image column
            blockChanges = true;
            
            // get the selection before the changes
            int[] selRows = spreadSheet.getSelectedRows();
            int[] selCols = spreadSheet.getSelectedColumns();
            
            model.setInImageMode(true);
            
            // then restore the selection
            for (int selRow: selRows)
            {
                spreadSheet.getSelectionModel().addSelectionInterval(selRow, selRow);
            }
            for (int selCol: selCols)
            {
                spreadSheet.getColumnModel().getSelectionModel().addSelectionInterval(selCol, selCol);
            }
            blockChanges = false;

            showCardImageForSelectedRow();
            
            TableColumn column = spreadSheet.getTableHeader().getColumnModel().getColumn(spreadSheet.getTableHeader().getColumnModel().getColumnCount()-1);
            column.setCellRenderer(new ImageRenderer());
            spreadSheet.repaint();
        }
    }
    
    /**
     * Loads a new Card Image into a WB Row
     * @param row the row of the new card image
     * @return true if the row was set
     */
    protected boolean loadNewCardImage(final WorkbenchRow row)
    {
        JFileChooser fileChooser = new JFileChooser();
        int          userAction  = fileChooser.showOpenDialog(this);
        if (userAction == JFileChooser.APPROVE_OPTION)
        {
            String chosenFile = fileChooser.getSelectedFile().getAbsolutePath();
            row.setCardImage(chosenFile);
            return true;
        }
        return false;
    }
    
    protected void showGeoRefConvertDialog()
    {
        JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
        
        if (!workbench.containsGeoRefData())
        {
            statusBar.setErrorMessage(getResourceString("NoGeoRefColumns"));
            return;
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

        JFrame mainFrame = (JFrame)UICacheManager.get(UICacheManager.TOPFRAME);
        
        String title = getResourceString("GeoRefConv");
        String description = getResourceString("GeoRefConvDesc");
        ToggleButtonChooserDlg<String> dlg = new ToggleButtonChooserDlg<String>(mainFrame,title,description,outputFormats,null, CustomDialog.OKCANCEL, Type.RadioButton)
        {
            @Override
            protected void okButtonPressed()
            {
                checkForCellEditing();
                
                // don't call super.okButtonPressed() b/c it will close the window
                isCancelled = false;
                btnPressed  = OK_BTN;
                switch( getSelectedIndex() )
                {
                    case 0:
                    {
                        convertColumnContents(latColIndex, new GeoRefConverter(), GeoRefFormat.D_PLUS_MINUS.name());
                        convertColumnContents(lonColIndex, new GeoRefConverter(), GeoRefFormat.D_PLUS_MINUS.name());
                        break;
                    }
                    case 1:
                    {
                        convertColumnContents(latColIndex, new GeoRefConverter(), GeoRefFormat.DM_PLUS_MINUS.name());
                        convertColumnContents(lonColIndex, new GeoRefConverter(), GeoRefFormat.DM_PLUS_MINUS.name());
                        break;
                    }
                    case 2:
                    {
                        convertColumnContents(latColIndex, new GeoRefConverter(), GeoRefFormat.DMS_PLUS_MINUS.name());
                        convertColumnContents(lonColIndex, new GeoRefConverter(), GeoRefFormat.DMS_PLUS_MINUS.name());
                        break;
                    }
                }
            }
        };
        dlg.setModal(false);
        dlg.setSelectedIndex(0);
        dlg.setOkLabel(getResourceString("Apply"));
        dlg.setCancelLabel(getResourceString("Close"));
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setVisible(true);
    }
    
    /**
     * Show a map for any number of selected records.
     */
    protected void showMapOfSelectedRecords()
    {
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
                selection[i]=i;
            }
        }
        
        DBTableIdMgr databaseSchema = WorkbenchTask.getDatabaseSchema();
        // build up a list of temporary Locality records to feed to the LocalityMapper
        List<Locality> fakeLocalityRecords = new Vector<Locality>(selection.length);
        List<WorkbenchRow> rows = workbench.getWorkbenchRowsAsList();
        int localityTableId = databaseSchema.getIdByClassName(Locality.class.getName());
        int lat1Index = workbench.getColumnIndex(localityTableId, "latitude1");
        int lon1Index = workbench.getColumnIndex(localityTableId, "longitude1");
        int lat2Index = workbench.getColumnIndex(localityTableId, "latitude2");
        int lon2Index = workbench.getColumnIndex(localityTableId, "longitude2");
        for (int i = 0; i < selection.length; ++i )
        {
            int index = selection[i];
            
            Locality newLoc = new Locality();
            newLoc.initialize();

            WorkbenchRow row = rows.get(index);

            String lat1 = row.getData(lat1Index);
            String lon1 = row.getData(lon1Index);
            BigDecimal latitude = null;
            BigDecimal longitude = null;
            try
            {
                latitude = new BigDecimal(lat1);
                longitude = new BigDecimal(lon1);
            }
            catch (Exception e)
            {
                // this could be a number format exception
                // or a null pointer exception if the field was empty
                // either way, we skip this record
                continue;
            }
            
            newLoc.setLatitude1(latitude);
            newLoc.setLongitude1(longitude);
            
            if (lat2Index != -1 && lon2Index != -1)
            {
                String lat2 = row.getData((short)lat2Index);
                String lon2 = row.getData((short)lon2Index);
                BigDecimal latitude2 = null;
                BigDecimal longitude2 = null;
                try
                {
                    latitude2 = new BigDecimal(lat2);
                    longitude2 = new BigDecimal(lon2);
                    newLoc.setLatitude2(latitude2);
                    newLoc.setLongitude2(longitude2);
                }
                catch (Exception e)
                {
                    // this could be a number format exception
                    // or a null pointer exception if the field was empty
                    // either way, we'll just treat this record as though it only has lat1 and lon1
                }
            }
            fakeLocalityRecords.add(newLoc);
        }
        
        LocalityMapper mapper = new LocalityMapper(fakeLocalityRecords);
        mapper.setMaxMapHeight(500);
        mapper.setMaxMapWidth(500);
        mapper.setShowArrows(false);
        mapper.setDotColor(new Color(64, 220, 64));
        mapper.setMinAspectRatio(0.5);
        mapper.setMaxAspectRatio(2.0);
        mapper.setEnforceAspectRatios(true);
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
                JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
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
        
        //FileCache imageCache = UICacheManager.getLongTermFileCache();
        //imageCache.clear();
        mapper.getMap(mapperListener);
        
        JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
        statusBar.setIndeterminate(true);
        statusBar.setText(getResourceString("WB_CREATINGMAP"));
    }
    
    /**
     * Notification that the Map was received.
     * @param map icon of the map that was generated
     */
    protected void mapImageReceived(final Icon map)
    {
        if (map != null)
        {
            mapFrame.setVisible(true);
            mapImageLabel.setIcon(map);
            showMapBtn.setEnabled(true);
        }
        JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
        statusBar.setIndeterminate(false);
        statusBar.setText("");
    }
    
    protected void convertColumnContents(int columnIndex, StringConverter converter, String outputFormat)
    {
        int rowCnt = model.getRowCount();
        for (int rowIndex = 0; rowIndex < rowCnt; ++rowIndex)
        {
            String currentValue = (String)model.getValueAt(rowIndex, columnIndex);
            if (StringUtils.isBlank(currentValue))
            {
                continue;
            }

            String convertedValue;
            try
            {
                convertedValue = converter.convert(currentValue, outputFormat);
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
     * Export to CSV.
     */
    protected void doExcelCsvExport()
    {
        int[] selection = spreadSheet.getSelectedRowModelIndexes();
        if (selection.length==0)
        {
            // if none are selected, select all of them
            int rowCnt = spreadSheet.getRowCount();
            selection = new int[rowCnt];
            for (int i = 0; i < rowCnt; ++i)
            {
                selection[i]=i;
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
        
        CommandAction command = new CommandAction(ExportTask.EXPORT,ExportTask.EXPORT_LIST);
        command.setData(selectedRows);
        command.setProperty("exporter", ExportToFile.class);
        
        
        Properties props = new Properties();
        
        Vector<String> list = new Vector<String>();
        list.add("Excel");
        list.add("CSV");
        
        ChooseFromListDlg<String> dlg = new ChooseFromListDlg<String>((Frame)UICacheManager.get(UICacheManager.FRAME), 
                                                                      "File Format?", null,
                                                                      ChooseFromListDlg.OKCANCELHELP,
                                                                      list, "WorkbenchImportCvs"); //XXX I18N
        dlg.setModal(true);
        UIHelper.centerAndShow(dlg);

        String format = dlg.getSelectedObject();

        if (format == "Excel") 
        { 
            props.setProperty("mimetype", ExportFileConfigurationFactory.XLS_MIME_TYPE);
        }
        else if (format == "CSV")
        {
            props.setProperty("mimetype", ExportFileConfigurationFactory.CSV_MIME_TYPE);   
        }
        else
        {
            return;
        }

        FileDialog fileDialog = new FileDialog((Frame) UICacheManager.get(UICacheManager.FRAME),
                getResourceString("CHOOSE_WORKBENCH_EXPORT_FILE"), FileDialog.SAVE);
        UIHelper.centerAndShow(fileDialog);

        String fileName = fileDialog.getFile();
        String path = fileDialog.getDirectory();
        if (StringUtils.isEmpty(fileName)) { return; }
        props.setProperty("fileName", path + File.separator + fileName);

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
        
        props = config.getProperties();
        Enumeration<?> keys = props.propertyNames();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            command.setProperty(key, props.getProperty(key));
        }

        CommandDispatcher.dispatch(command);
    }
    
    /**
     * Make a request to the ExportTask to display the selected records in GoogleEarth.
     */
    protected void showRecordsInGoogleEarth()
    {
        log.debug("Showing map of selected records");
        int[] selection = spreadSheet.getSelectedRowModelIndexes();
        if (selection.length==0)
        {
            // if none are selected, map all of them
            int rowCnt = spreadSheet.getRowCount();
            selection = new int[rowCnt];
            for (int i = 0; i < rowCnt; ++i)
            {
                selection[i]=i;
            }
        }
        
        // put all the selected rows in a List
        List<GoogleEarthPlacemarkIFace> selectedRows = new Vector<GoogleEarthPlacemarkIFace>();
        List<WorkbenchRow> rows = workbench.getWorkbenchRowsAsList();
        for (int i = 0; i < selection.length; ++i )
        {
            int index = selection[i];
            WorkbenchRow row = rows.get(index);
            selectedRows.add(new WorkbenchRowPlacemarkWrapper(row));
        }
        
        // get an icon URL that is specific to the current context
        String discipline = CollectionObjDef.getCurrentCollectionObjDef().getDiscipline();
        String iconUrl = null;
        discipline = discipline.toLowerCase();
        if (discipline.startsWith("fish"))
        {
            iconUrl = getResourceString("WB_GOOGLE_FISH_ICON_URL");
        }
        if (discipline.startsWith("bird"))
        {
            iconUrl = getResourceString("WB_GOOGLE_BIRD_ICON_URL");
        }
        if (discipline.startsWith("insect") || discipline.startsWith("ento"))
        {
            iconUrl = getResourceString("WB_GOOGLE_ENTO_ICON_URL");
        }
        if (discipline.startsWith("plant") || discipline.equals("botany"))
        {
            iconUrl = getResourceString("WB_GOOGLE_PLANT_ICON_URL");
        }
        if (discipline.startsWith("mammal"))
        {
            iconUrl = getResourceString("WB_GOOGLE_MAMMAL_ICON_URL");
        }
        if (discipline.startsWith("herp"))
        {
            iconUrl = getResourceString("WB_GOOGLE_HERP_ICON_URL");
        }
        
        CommandAction command = new CommandAction(ExportTask.EXPORT,ExportTask.EXPORT_LIST);
        command.setData(selectedRows);
        command.setProperty("exporter", GoogleEarthExporter.class);
        if (iconUrl != null)
        {
            command.setProperty("iconURL", iconUrl);
        }
        CommandDispatcher.dispatch(command);
        JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
        statusBar.setText("Opening Google Earth");
    }
    
    /**
     * Checks to see if the template can support BGM.
     * @return return whether this template supports BGM
     */
    protected boolean isTemplateBGMCompatible()
    {
        int localityTableId = DBTableIdMgr.getInstance().getIdByClassName(Locality.class.getName());
        if (workbench.getColumnIndex(localityTableId, "localityName") == -1 ||
            workbench.getColumnIndex(localityTableId, "latitude1") == -1 ||
            workbench.getColumnIndex(localityTableId, "longitude1") == -1)
        {
            return false;
        }
        
        // get the geography data
        int geographyTableId = DBTableIdMgr.getInstance().getIdByClassName(Geography.class.getName());
        if (workbench.getColumnIndex(geographyTableId, "Country") == -1 ||
            workbench.getColumnIndex(geographyTableId, "State") == -1 ||
            workbench.getColumnIndex(geographyTableId, "County") == -1)
        {
            return false;
        }
        
        return true;
    }
    
    /**
     * Use the BioGeomancer web service to lookup georeferences for the selected records.
     */
    protected void doBioGeomancerLookup()
    {
        int selection = spreadSheet.getSelectedRow();
        if (selection == -1)
        {
            // we must have a row selected
            return;
        }
        
        final WorkbenchRow selectedRow = workbench.getWorkbenchRowsAsList().get(selection);
        
        // get an instance of the biogeo service
        JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
        statusBar.setText("");
        
        ((JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR)).setIndeterminate(true);
        final SwingWorker worker = new SwingWorker()
        {
            protected String    data      = null;
            protected Exception exception = null;
            
            @Override
            public Object construct()
            {
                int    localityTableId      = DBTableIdMgr.getInstance().getIdByClassName(Locality.class.getName());
                int    localityNameColIndex = workbench.getColumnIndex(localityTableId, "localityName");
                String localityNameStr      = selectedRow.getData(localityNameColIndex);
                        
                // get the geography data
                int    geographyTableId = DBTableIdMgr.getInstance().getIdByClassName(Geography.class.getName());
                int    countryColIndex  = workbench.getColumnIndex(geographyTableId, "Country");
                int    stateColIndex    = workbench.getColumnIndex(geographyTableId, "State");
                int    countyColIndex   = workbench.getColumnIndex(geographyTableId, "County");
                
                String country = (countryColIndex!=-1) ? selectedRow.getData(countryColIndex) : "";
                String state   = (stateColIndex!=-1) ? selectedRow.getData(stateColIndex) : "";
                String county  = (countyColIndex!=-1) ? selectedRow.getData(countyColIndex) : "";
                
                data      = BioGeoMancer.getBioGeoMancerResponse(selectedRow.getWorkbenchRowId().toString(), country, state, county, localityNameStr);
                exception = BioGeoMancer.getException();
                
                return null;
            }

            //Runs on the event-dispatching thread.
            @Override
            public void finished()
            {
                ((JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR)).setIndeterminate(false);
                if (exception == null)
                {
                    processBGMResults(selectedRow, data);
                    
                } else
                {
                    ((JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR)).setErrorMessage("BioGeomancer service failure", exception);
                }
            }
        };
        worker.start();
    }
    
    /**
     * Processes the BGM results and displays a dialog.
     * @param selectedRow the WorkbenchRow we are working on
     * @param data the results we got back from BGM
     */
    protected void processBGMResults(final WorkbenchRow selectedRow, final String data)
    {
        // show the BGM frame to allow the user to select from the results
        try
        {
            BioGeoMancer bgmService = new BioGeoMancer();
            bgmService.initialize(null, false);
            
            Locality loc = new Locality();
            loc.initialize();
            bgmService.setValue(loc, null);
            
            JDialog dialog = new JDialog();
            dialog.setTitle("BioGeoManacer");
            dialog.setModal(true);
            dialog.setContentPane(bgmService.createBGMPanel(XMLHelper.readStrToDOM4J(data), dialog));
            dialog.pack();
            
            // This must be a Java 6 feature
            //ImageIcon icon = IconManager.getIcon("BioGeoMancer", IconManager.IconSize.Std16);
            //if (icon != null)
            //{
            //    dialog.setIconImage(icon.getImage());
            //}
            UIHelper.centerAndShow(dialog);
            
            loc = (Locality)bgmService.getValue();
            if (loc == null || loc.getLatitude1() == null || loc.getLongitude1() == null)
            {
                // no value was chosen
                // the user probably pressed the "close" button instead of "OK"
                return;
            }
            log.debug("Lat:"+loc.getLatitude1().toString() + " Long:" + loc.getLongitude1().toString());
            
            // get the latitude1 and longitude1 column indices
            int localityTableId = DBTableIdMgr.getInstance().getIdByClassName(Locality.class.getName());
            int latIndex        = workbench.getColumnIndex(localityTableId, "latitude1");
            int lonIndex        = workbench.getColumnIndex(localityTableId, "longitude1");

            selectedRow.setData(loc.getLatitude1().toString(), (short)latIndex);
            selectedRow.setData(loc.getLongitude1().toString(), (short)lonIndex);
            spreadSheet.repaint();
            setChanged(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ((JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR)).setErrorMessage("BioGeomancer service failure", e);
        }

    }
    
    /**
     * @param bgService
     * @throws Exception
     */
    protected void showBioGeomancerDialog(final BioGeoMancer bgService) throws Exception
    {
        JDialog dialog = new JDialog();
        dialog.setModal(true);
        JPanel p = new JPanel(new BorderLayout());
        p.add(bgService.createBGMPanel(XMLHelper.readFileToDOM4J(new File("biogeomancer.xml")), dialog), BorderLayout.CENTER);
        dialog.setContentPane(p);
        dialog.setLocation(0,0);
        dialog.pack();
        
        //ImageIcon icon = IconManager.getIcon("BioGeoMancer", IconManager.IconSize.Std16);
//        if (icon != null)
//        {
//            dialog.setIconImage(icon.getImage());
//        }
        dialog.setVisible(true);
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
    
    
    public boolean isChanged()
    {
        return hasChanged;
    }

    /**
     * Adjust all the column width for the data in the column, this may be handles with JDK 1.6 (6.)
     * @param tableArg the table that should have it's columns adjusted
     */
    private void initColumnSizes(final JTable tableArg) 
    {
        TableModel  tblModel    = tableArg.getModel();
        TableColumn column      = null;
        Component   comp        = null;
        int         headerWidth = 0;
        int         cellWidth   = 0;
        
        TableCellRenderer headerRenderer = tableArg.getTableHeader().getDefaultRenderer();

        GridCellEditor cellEditor = new GridCellEditor(new JTextField());
        //UICacheManager.getInstance().hookUpUndoableEditListener(cellEditor);
        
        for (int i = 0; i < tblModel.getColumnCount(); i++) 
        {
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
            FontMetrics fm     = new JLabel().getFontMetrics(getFont());
            for (int row=0;row<tableArg.getModel().getRowCount();row++)
            {
                String text = m.getValueAt(row, i).toString();
                maxWidth = Math.max(maxWidth, fm.stringWidth(text)+10);
                //System.out.println(i+" "+maxWidth);
            }

            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
            column.setPreferredWidth(Math.max(maxWidth, cellWidth));
            
            column.setCellEditor(cellEditor);
        }
        
        tableArg.setCellEditor(cellEditor);

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
        ToggleButtonChooserDlg<WorkbenchTemplateMappingItem> dlg = new ToggleButtonChooserDlg<WorkbenchTemplateMappingItem>((Frame)UICacheManager.get(UICacheManager.FRAME),
                                                                        "WB_CARRYFORWARD",
                                                                        "WB_CHOOSE_CARRYFORWARD", 
                                                                        items,
                                                                        null,
                                                                        CustomDialog.OKCANCELHELP,
                                                                        ToggleButtonChooserDlg.Type.Checkbox);
        
        dlg.setHelpContext(currentPanelType == PanelType.Spreadsheet ? "OnRampGridEditingCF" : "OnRampFormEditingCF");
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
        
        checkForCellEditing();
        
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            FormHelper.updateLastEdittedInfo(workbench);
            
            // Delete the cached Items
            Vector<WorkbenchRow> deletedItems = workbench.getDeletedRows();
            if (deletedItems != null)
            {
                session.beginTransaction();
                for (Object obj : deletedItems)
                {
                    session.delete(obj);
                }
                deletedItems.clear();
                session.commit();
                session.flush();
            }
            
            session.beginTransaction();
            
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
            
            log.info("Session Saved[ and Flushed "+session.hashCode()+"]");
            
           
            hasChanged = false;
            
            String msg = String.format(getResourceString("WB_SAVED"), new Object[] { workbench.getName()} );
            ((JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR)).setText(msg);


        } catch (StaleObjectException ex) // was StaleObjectStateException
        {
            session.rollback();
            
            // 
            //recoverFromStaleObject("UPDATE_DATA_STALE");
            ((JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR)).setErrorMessage(getResourceString("WB_ERROR_SAVING"), ex);

            
        } catch (Exception ex)
        {
            log.error("******* " + ex);
            ex.printStackTrace();
            session.rollback();
            ((JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR)).setErrorMessage(getResourceString("WB_ERROR_SAVING"), ex);

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
            int rv = JOptionPane.showConfirmDialog(null,
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
        
        if (formPane != null)
        {
            formPane.cleanup();
        }
        
        boolean retStatus = true;
        if (hasChanged)
        {
            int rv = JOptionPane.showConfirmDialog(null,
                                                   getResourceString("SaveChanges"),
                                                   getResourceString("SaveChangesTitle"),
                                                   JOptionPane.YES_NO_CANCEL_OPTION);
            if (rv == JOptionPane.YES_OPTION)
            {
                saveObject();
    
            } else if (rv == JOptionPane.CANCEL_OPTION)
            {
                retStatus = false;
                
            } else if (rv == JOptionPane.NO_OPTION)
            {
                // nothing
            }
        }
        
        if (retStatus)
        {
            ((WorkbenchTask)task).closing(this);
            //UICacheManager.unregisterAction(workbench.getName()+"_AddRow");
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
            if (imageFrameWasShowing)
            {
                toggleImageFrameVisible();
                ((Frame)UICacheManager.get(UICacheManager.FRAME)).toFront();
            }
        }
        else
        {
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
        return currentPanelType == PanelType.Spreadsheet ? "OnRampGridEditing" : "OnRampFormEditing";
    }

    //------------------------------------------------------------
    // Inner Classes
    //------------------------------------------------------------


    class GridCellEditor extends DefaultCellEditor implements TableCellEditor//, UndoableTextIFace
    {
        protected JTextField  textField;
        //protected UndoManager undoManager = new UndoManager();

        public GridCellEditor(final JTextField textField)
        {
            super(textField);
            this.textField = textField;
            textField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setClickCountToStart (1); 
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
                        textField.requestFocus();
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
         * @see edu.ku.brc.ui.UICacheManager.UndoableTextIFace#getUndoManager()
         */
        public UndoManager getUndoManager()
        {
            return null;//undoManager;
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.UICacheManager.UndoableTextIFace#getText()
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
    
    class SaveProgressDialog extends ProgressDialog
    {
        public SaveProgressDialog(final String  title, 
                              final boolean includeBothBars,
                              final boolean includeClose)
        {
            super(title, includeBothBars, includeClose);
            
            setModal(true);
            
            final JDialog dlg = this;
            final SwingWorker worker = new SwingWorker()
            {
                public Object construct()
                {
                    try
                    {
                        saveObject();
                        
                    } catch (Exception ex)
                    {
                        log.error(ex);
                        ((JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR)).setErrorMessage(getResourceString("WB_ERROR_SAVING"), ex);
                    }
                    return null;
                }

                //Runs on the event-dispatching thread.
                public void finished()
                {
                    dlg.setVisible(false);
                    dlg.dispose();
                }
            };
            worker.start();
        }
    }
}

