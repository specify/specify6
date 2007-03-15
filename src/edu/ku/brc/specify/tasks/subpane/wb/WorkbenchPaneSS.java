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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.StaleObjectException;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.DropDownButtonStateful;
import edu.ku.brc.ui.DropDownMenuInfo;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UICacheManager.UndoableTextIFace;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.ui.forms.ResultSetController;
import edu.ku.brc.ui.forms.ResultSetControllerListener;
import edu.ku.brc.ui.tmanfe.SpreadSheet;

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
public class WorkbenchPaneSS extends BaseSubPane implements ResultSetControllerListener
{
    private static final Logger log = Logger.getLogger(WorkbenchPaneSS.class);
    
    private enum PanelType {Spreadsheet, Form}

    protected SpreadSheet spreadSheet;
    protected Workbench   workbench;
    protected String[]    columns;
    protected Vector<WorkbenchTemplateMappingItem> headers = new Vector<WorkbenchTemplateMappingItem>();
    protected boolean     hasChanged = false;
    
    protected GridTableModel model;
    
    protected JButton     saveBtn         = null;
    protected JButton     deleteRowsBtn   = null;
    protected JButton     cellCellsBtn    = null;
    protected JButton     insertRowBtn    = null;
    protected JButton     addRowsBtn      = null;
    protected JButton     carryForwardBtn = null;
    protected JButton     toggleCardImageBtn = null;
    
    protected int         currentRow      = 0;
    
    
    protected FormPane            formPane;
    protected ResultSetController resultsetController;
    
    
    protected CardLayout  cardLayout        = null;
    protected JPanel      mainPanel;
    protected PanelType   currentPanelType  = PanelType.Spreadsheet;
    
    protected JPanel      controllerPane;
    protected CardLayout  cpCardLayout      = null;
    
    protected JFrame                cardImageFrame             = null;
    protected JLabel                cardImageLabel             = null;
    protected JPanel                noCardImageMessagePanel    = null;
    protected ListSelectionListener workbenchRowChangeListener = null;
    protected boolean               cardFrameWasShowing        = false;
    protected boolean               showingCardImageLabel      = true;
    
    /**
     * Constructs the pane for the spreadsheet.
     * 
     * @param name the name of the pane
     * @param task the owning task
     * @param workbench the workbench to be editted
     */
    public WorkbenchPaneSS(final String name,
                           final Taskable task,
                           final Workbench workbench)
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
        
         // pre load all the data
        for (WorkbenchRow wbRow : workbench.getWorkbenchRows())
        {
            for (WorkbenchDataItem wbdi : wbRow.getWorkbenchDataItems())
            {
                wbdi.getCellData();
            }
        } 

        model       = new GridTableModel(workbench, headers);
        spreadSheet = new SpreadSheet(model);
        model.setSpreadSheet(spreadSheet);
        
        //spreadsheet.setBackground(Color.WHITE);
        initColumnSizes(spreadSheet);
        spreadSheet.setShowGrid(true);
        
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e)
            {
                setChanged(true);
            }
        });
        
        spreadSheet.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    boolean enable = spreadSheet.getSelectedRow() > -1;
                    cellCellsBtn.setEnabled(enable);
                    insertRowBtn.setEnabled(enable);  
                    deleteRowsBtn.setEnabled(enable); 

                    setCurrentRow( spreadSheet.getSelectedRow());
                }
            }
        });
        
        saveBtn = new JButton(UICacheManager.getResourceString("Save"));
        saveBtn.setEnabled(false);
        saveBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                saveObject();
            }
        });
       
        ActionListener deleteAction = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                model.deleteRows(spreadSheet.getSelectedRows());
            }
        };
        deleteRowsBtn = createIconBtn("MinusSign", "WB_DELETE_ROW", deleteAction);
        
        cellCellsBtn = createIconBtn("Eraser", "WB_CLEAR_CELLS", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                model.clearCells(spreadSheet.getSelectedRows(), spreadSheet.getSelectedColumns());
            }
        });
        
        ActionListener insertAction = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                model.insertRow(spreadSheet.getSelectedRow());
                resultsetController.setIndex(getCurrentRow());
            }
        };
        
        insertRowBtn = createIconBtn("InsertSign", "WB_INSERT_ROW", insertAction);

        addRowsBtn = createIconBtn("PlusSign", "WB_ADD_ROW", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                model.appendRow();
                resultsetController.setIndex(getCurrentRow());
            }
        });
        addRowsBtn.setEnabled(true);

        carryForwardBtn = createIconBtn("SystemSetup", IconManager.IconSize.Std16, "WB_CARRYFORWARD", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                configCarryFoward();
            }
        });
        carryForwardBtn.setEnabled(true);

        toggleCardImageBtn = createIconBtn("CardImage", IconManager.IconSize.Std16, "WB_SHOW_CARD", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                toggleCardImageVisible();
            }
        });
        toggleCardImageBtn.setEnabled(true);
        
        // setup the JFrame to show images attached to WorkbenchRows
        cardImageFrame = new JFrame();
        cardImageLabel = new JLabel();
        cardImageLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        cardImageLabel.setSize(500,500);
        cardImageFrame.add(cardImageLabel);
        cardImageFrame.setSize(500,500);
        setupWorkbenchRowChangeListener();
        
        noCardImageMessagePanel = new JPanel();
        noCardImageMessagePanel.setLayout(new BoxLayout(noCardImageMessagePanel,BoxLayout.LINE_AXIS));
        JButton loadImgBtn = new JButton("Load New Image");
        loadImgBtn.addActionListener(new ActionListener()
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
        noCardImageMessagePanel.add(new JLabel("No card image available for the selected row"));
        noCardImageMessagePanel.add(loadImgBtn);
        
        CellConstraints cc = new CellConstraints();

        JComponent[] comps      = {addRowsBtn, insertRowBtn, cellCellsBtn, deleteRowsBtn};
        PanelBuilder controlBar = new PanelBuilder(new FormLayout("f:p:g,2px,"+createDuplicateJGoodiesDef("p", "2px", comps.length)+",2px,", "p:g"));

        int x = 3;
        for (JComponent c : comps)
        {
            controlBar.add(c, cc.xy(x,1));
            x += 2;
        }
        
        mainPanel = new JPanel(cardLayout = new CardLayout());
        
        formPane = new FormPane(this, workbench);
        
        PanelBuilder rsPanel = new PanelBuilder(new FormLayout("c:p:g", "p"));
        resultsetController  = new ResultSetController(null, true, true, "XXXX", model.getRowCount());
        resultsetController.addListener(formPane);
        resultsetController.addListener(this);
        resultsetController.getNewRecBtn().addActionListener(insertAction);
        resultsetController.getDelRecBtn().addActionListener(deleteAction);
        rsPanel.add(resultsetController.getPanel(), cc.xy(1,1));
        
        mainPanel.add(spreadSheet.getScrollPane(), PanelType.Spreadsheet.toString());
        mainPanel.add(formPane, PanelType.Form.toString());
        
        controllerPane = new JPanel(cpCardLayout = new CardLayout());
        controllerPane.add(controlBar.getPanel(), PanelType.Spreadsheet.toString());
        controllerPane.add(rsPanel.getPanel(), PanelType.Form.toString());
        
        FormLayout      formLayout = new FormLayout("f:p:g,4px,p,4px,p,4px,p,4px,p", "fill:p:g, 5px, p");
        PanelBuilder    builder    = new PanelBuilder(formLayout, this);

        builder.add(mainPanel,          cc.xywh(1,1,9,1));
        builder.add(controllerPane,     cc.xy(1,3));
        builder.add(toggleCardImageBtn, cc.xy(3,3));
        builder.add(carryForwardBtn,    cc.xy(5,3));
        builder.add(saveBtn,            cc.xy(7,3));
        builder.add(createSwitcher(),   cc.xy(9,3));
    }

    /**
     * Setup the row (or selection) listener for the the Image Window. 
     */
    protected void setupWorkbenchRowChangeListener()
    {
        workbenchRowChangeListener = new ListSelectionListener()
        {
            @SuppressWarnings("synthetic-access")
            public void valueChanged(ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting() || !cardImageFrame.isVisible())
                {
                    // ignore this until the user quits changing the selection
                    return;
                }
                showCardImageForSelectedRow();
            }
        };
    }
    
    protected void showCardImageForSelectedRow()
    {
        int firstRowSelected = spreadSheet.getSelectedRow();
        if (firstRowSelected == -1)
        {
            // no selection
            log.debug("No selection, so removing the card image");
            
            if (!showingCardImageLabel)
            {
                // swap out the noCardImageMessagePanel for the cardImageLabel
                cardImageFrame.remove(noCardImageMessagePanel);
                cardImageFrame.add(cardImageLabel);
                showingCardImageLabel = true;
            }
            
            cardImageLabel.setIcon(null);
            cardImageLabel.setText("No row selected");
            return;
        }
        // else

        log.debug("Showing image for row " + firstRowSelected);
        WorkbenchRow row = workbench.getWorkbenchRowsAsList().get(firstRowSelected);
        ImageIcon cardImage = row.getCardImage();
        log.debug("\tImage file: " + cardImage);
        cardImageLabel.setIcon(cardImage);
        if (cardImage==null)
        {
            if (showingCardImageLabel)
            {
                // swap out the cardImageLabel for the noCardImageMessagePanel
                cardImageFrame.remove(cardImageLabel);
                cardImageFrame.add(noCardImageMessagePanel);
                showingCardImageLabel = false;
            }
        }
        else
        {
            if (!showingCardImageLabel)
            {
                // swap out the noCardImageMessagePanel for the cardImageLabel
                cardImageFrame.remove(noCardImageMessagePanel);
                cardImageFrame.add(cardImageLabel);
                showingCardImageLabel = true;
            }
            cardImageLabel.setText(null);
        }
        WorkbenchDataItem firstColItem = row.getItems().get(0);
        String firstColCellData = (firstColItem!=null) ? firstColItem.getCellData() : "";
        cardImageFrame.setTitle("Row " + (firstRowSelected+1) + ": " + firstColCellData);
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
        switcher.setToolTipText(getResourceString("SwitchViewsTT"));
        switcher.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                showPanel(switcher.getCurrentIndex() == 0 ? PanelType.Spreadsheet : PanelType.Form);
            }
        });
        switcher.validate();
        switcher.doLayout();
        
        return switcher;
    }
    
    /**
     * Shows the grid or the form.
     * @param value the panel number
     */
    public void showPanel(final PanelType panelType)
    {
        currentPanelType = panelType;
        
        cardLayout.show(mainPanel, currentPanelType.toString());
        cpCardLayout.show(controllerPane, currentPanelType.toString());
        
        
        
       boolean isSpreadsheet = currentPanelType == PanelType.Spreadsheet;
       if (isSpreadsheet)
       {
           // Showing Spreadsheet and hiding form
           setCurrentRow(resultsetController.getCurrentIndex());
           if (model.getRowCount() > 0)
           {
               spreadSheet.setRowSelectionInterval(getCurrentRow(), getCurrentRow());
               spreadSheet.setColumnSelectionInterval(0, model.getColumnCount()-1);
               spreadSheet.scrollToRow(Math.min(getCurrentRow()+4, model.getRowCount()));
           }
           
       } else
       {
           // Showing Form and hiding Spreadsheet
           setCurrentRow(spreadSheet.getSelectedRow());
           if (model.getRowCount() > 0)
           {
               resultsetController.setIndex(getCurrentRow());
           }
       }
            
       JComponent[] comps = { addRowsBtn, insertRowBtn, cellCellsBtn, deleteRowsBtn};
       for (JComponent c : comps)
       {
           c.setVisible(isSpreadsheet);
       }
    }
    
    /**
     * Shows / Hides the Image Window. 
     */
    public void toggleCardImageVisible()
    {
        // we simply have to toggle the visibility
        // and add or remove the ListSelectionListener (to avoid loading images when not visible)
        boolean visible = cardImageFrame.isVisible();
        if (visible)
        {
            spreadSheet.getSelectionModel().removeListSelectionListener(workbenchRowChangeListener);
            cardImageFrame.setVisible(false);
        }
        else
        {
            spreadSheet.getSelectionModel().addListSelectionListener(workbenchRowChangeListener);
            cardImageFrame.setVisible(true);
            int[] selectedRows = spreadSheet.getSelectedRows();
            int first = -1;
            int last = -1;
            if (selectedRows.length!=0)
            {
                first = selectedRows[0];
                last = selectedRows[selectedRows.length-1];
            }
            ListSelectionEvent lse = new ListSelectionEvent(spreadSheet,first,last,false);
            workbenchRowChangeListener.valueChanged(lse);
        }
    }
    
    public boolean loadNewCardImage(WorkbenchRow row)
    {
        JFileChooser fileChooser = new JFileChooser();
        int userAction = fileChooser.showOpenDialog(this);
        if (userAction == JFileChooser.APPROVE_OPTION)
        {
            String chosenFile = fileChooser.getSelectedFile().getAbsolutePath();
            try
            {
                row.setCardImage(chosenFile);
                return true;
            }
            catch (IOException e)
            {
                log.error("Failed to set card image for workbench row", e);
            }
        }
        return false;
    }
    
    /**
     * Set that there has been a change.
     * 
     * @param changed true or false
     */
    public void setChanged(final boolean changed)
    {
        hasChanged = changed;
        saveBtn.setEnabled(hasChanged);
    }
    
    
    /**
     * Returns the currently selected row in the Spreasdsheet or form.
     * @return the currently selected row in the Spreasdsheet or form.
     */
    public int getCurrentRow()
    {
        return currentRow;
    }

    /**
     * Sets the currently selected row in the Spreasdsheet or form.
     * @param currentRow the current row
     */
    public void setCurrentRow(int curRow)
    {
        if (curRow > -1 && this.currentRow != curRow)
        {
            this.currentRow = curRow;
            
            if (currentPanelType == PanelType.Form)
            {
                if (curRow != spreadSheet.getSelectedRow())
                {
                    spreadSheet.setRowSelectionInterval(curRow, curRow);
                }
            } else
            {
                resultsetController.setIndex(curRow);
            }
        }
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

        GridCellEditor cellEditor = new GridCellEditor();
        UICacheManager.getInstance().hookUpUndoableEditListener(cellEditor);
        
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
        
        //tableArg.setCellEditor(new GridCellEditor());

    }
    
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
                "WB_CHOOSE_CARRYFORWARD", items);
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
        }
    }
    
    /**
     * Save the Data. 
     */
    protected void saveObject()
    {

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
            session.saveOrUpdate(dObj);
            session.commit();
            session.flush();

            workbench = (Workbench)dObj;
            
            log.info("Session Saved[ and Flushed "+session.hashCode()+"]");
            
            hasChanged = false;

        } catch (StaleObjectException e) // was StaleObjectStateException
        {
            session.rollback();
            
            // 
            //recoverFromStaleObject("UPDATE_DATA_STALE");
            
        } catch (Exception e)
        {
            log.error("******* " + e);
            e.printStackTrace();
            session.rollback();
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
        
        if (hasChanged)
        {
            //
        }
        return true;
    }

    @Override
    public void showingPane(boolean show)
    {
        if (show)
        {
            if (cardFrameWasShowing)
            {
                toggleCardImageVisible();
            }
        }
        else
        {
            if (cardImageFrame!=null && cardImageFrame.isVisible())
            {
                cardFrameWasShowing = true;
                toggleCardImageVisible();
            }
            else
            {
                cardFrameWasShowing = false;
            }
        }
        super.showingPane(show);
    }

    //------------------------------------------------------------
    // ResultSetControllerListener
    //------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexAboutToChange(int, int)
     */
    public boolean indexAboutToChange(int oldIndex, int newIndex)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexChanged(int)
     */
    public void indexChanged(int newIndex)
    {
        setCurrentRow(newIndex);
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#newRecordAdded()
     */
    public void newRecordAdded()
    {
        // TODO Auto-generated method stub
        
    }



    //------------------------------------------------------------
    // Inner Classes
    //------------------------------------------------------------


    class GridCellEditor extends AbstractCellEditor implements TableCellEditor, UndoableTextIFace
    {
        protected JTextField  textField   = new JTextField();
        protected UndoManager undoManager = new UndoManager();

        public GridCellEditor()
        {
            textField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }

        /* (non-Javadoc)
         * @see javax.swing.CellEditor#getCellEditorValue()
         */
        public Object getCellEditorValue() 
        {
            return textField.getText();
        }

        /* (non-Javadoc)
         * @see javax.swing.AbstractCellEditor#isCellEditable(java.util.EventObject)
         */
        @Override
        public boolean isCellEditable(EventObject anEvent) 
        { 
            return true; 
        }
        
        //
        //          Implementing the CellEditor Interface
        //
        /** Implements the <code>TableCellEditor</code> interface. */
        public Component getTableCellEditorComponent(JTable  tbl, 
                                                     Object  value,
                                                     boolean isSelected,
                                                     int     row, 
                                                     int     column)
        {
            textField.setText(value != null ? value.toString() : "");
            textField.selectAll();
            undoManager.discardAllEdits();
            UICacheManager.getUndoAction().setUndoManager(undoManager);
            UICacheManager.getRedoAction().setUndoManager(undoManager);
            return textField;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.UICacheManager.UndoableTextIFace#getUndoManager()
         */
        public UndoManager getUndoManager()
        {
            return undoManager;
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.UICacheManager.UndoableTextIFace#getText()
         */
        public JTextComponent getTextComponent()
        {
            return textField;
        }
        
     }

    
    class SwitcherAL implements ActionListener
    {
        protected DropDownButtonStateful switcherComp;
        public SwitcherAL(final DropDownButtonStateful switcherComp)
        {
            this.switcherComp = switcherComp;
        }
        public void actionPerformed(ActionEvent ae)
        {
            //log.info("Index: "+switcherComp.getCurrentIndex());
            
            showPanel(((DropDownButtonStateful)ae.getSource()).getCurrentIndex() == 0 ? PanelType.Spreadsheet : PanelType.Form);
        }
    }
}

