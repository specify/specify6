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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
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
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.ui.forms.ResultSetController;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.ViewDef;
import edu.ku.brc.ui.tmanfe.SpreadSheet;


public class WorkbenchPaneSS extends BaseSubPane
{
    private static final Logger log = Logger.getLogger(WorkbenchPaneSS.class);

    protected SpreadSheet spreadSheet;
    protected Workbench   workbench;
    protected String[]    columns;
    protected Vector<WorkbenchTemplateMappingItem> headers = new Vector<WorkbenchTemplateMappingItem>();
    protected boolean     hasChanged = false;
    
    protected GridTableModel model;
    
    protected JButton     saveBtn       = null;
    protected JButton     deleteRowsBtn = null;
    protected JButton     cellCellsBtn  = null;
    protected JButton     insertRowBtn  = null;
    protected JButton     addRowsBtn    = null;
    
    protected FormPane    formPane;
    
    protected CardLayout  cardLayout      = null;
    protected JPanel      mainPanel;
    
    protected JPanel      controllerPane;
    protected CardLayout  cpCardLayout      = null;

    /**
     * Constructs the pane for the spreadsheet.
     * @param name the name of the pane
     * @param task the owning task
     * @param workbench the workbench to be editted
     */
    public WorkbenchPaneSS(final String name,
                           final Taskable task,
                           final Workbench workbench)
    {
        super(name, task);
        
        this.workbench = workbench;
        
        removeAll();
        
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
        
        deleteRowsBtn = new JButton(UICacheManager.getResourceString("Delete Row(s)"));
        deleteRowsBtn.setEnabled(false);
        deleteRowsBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                model.deleteRows(spreadSheet.getSelectedRows());
            }
        });
        
        cellCellsBtn = new JButton(UICacheManager.getResourceString("Clear Cell(s)"));
        cellCellsBtn.setEnabled(false);
        cellCellsBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                model.clearCells(spreadSheet.getSelectedRows(), spreadSheet.getSelectedColumns());
            }
        });
        
        insertRowBtn = new JButton(UICacheManager.getResourceString("Insert Row"));
        insertRowBtn.setEnabled(false);
        insertRowBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                model.insertRow(spreadSheet.getSelectedRow());
            }
        });

        addRowsBtn = new JButton(UICacheManager.getResourceString("Add Row"));
        //addRowsBtn.setEnabled(false);
        addRowsBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                model.appendRow();
            }
        });

        
        CellConstraints cc = new CellConstraints();

        JComponent[] comps = { addRowsBtn, insertRowBtn, cellCellsBtn, deleteRowsBtn, saveBtn, };
        PanelBuilder controlBar = new PanelBuilder(new FormLayout("f:p:g,2px,"+UIHelper.createDuplicateJGoodiesDef("f:p:g", "2px", comps.length-1)+",2px,p,2px,", "p:g"));

        int x = 3;
        for (JComponent c : comps)
        {
            controlBar.add(c, cc.xy(x,1));
            x += 2;
        }
        
        mainPanel = new JPanel(cardLayout = new CardLayout());
        
        formPane = new FormPane(workbench);
        ResultSetController rsc = new ResultSetController(null, true, true, "XXXX", model.getRowCount());
        rsc.addListener(formPane);
        
        mainPanel.add(((SpreadSheet)spreadSheet).getScrollPane(), "0");
        mainPanel.add(formPane, "1");
        
        controllerPane = new JPanel(cpCardLayout = new CardLayout());
        controllerPane.add(controlBar.getPanel(), "0");
        controllerPane.add(rsc.getPanel(), "1");
        
        FormLayout      formLayout = new FormLayout("f:p:g,5px,p", "fill:p:g, 5px, p");
        PanelBuilder    builder    = new PanelBuilder(formLayout, this);

        builder.add(mainPanel, cc.xywh(1,1,3,1));
        builder.add(controllerPane, cc.xy(1,3));
        builder.add(createSwitcher(), cc.xy(3,3));
    }
    
    /**
     * @return
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
                showPanel(switcher.getCurrentIndex());
            }
        });
        switcher.validate();
        switcher.doLayout();
        
        return switcher;
    }
    
    /**
     * @param value
     */
    public void showPanel(final int panelNum)
    {
        String key = Integer.toString(panelNum);
        cardLayout.show(mainPanel, key);
        cpCardLayout.show(controllerPane, key);
        
        boolean show = panelNum == 0;
            
       JComponent[] comps = { addRowsBtn, insertRowBtn, cellCellsBtn, deleteRowsBtn};
       for (JComponent c : comps)
       {
           c.setVisible(show);
       }
    }
    
    /**
     * Set that there has been a change.
     * @param changed true or false
     */
    protected void setChanged(final boolean changed)
    {
        hasChanged = changed;
        saveBtn.setEnabled(hasChanged);
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

            /*
            if (DEBUG) {
                System.out.println("Initializing width of column "
                                   + i + ". "
                                   + "headerWidth = " + headerWidth
                                   + "; cellWidth = " + cellWidth);
            }*/

            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
            column.setPreferredWidth(Math.max(maxWidth, cellWidth));
            
            //column.setCellEditor(new GridCellEditor());
        }
        
        //tableArg.setCellEditor(new GridCellEditor());

    }
    
    protected void saveObject()
    {

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        
        //log.info("saveObject "+hashCode() + " Session ["+(session != null ? session.hashCode() : "null")+"]");
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
                            CheckboxChooserDlg<BusinessRulesDataItem> dlg = new CheckboxChooserDlg<BusinessRulesDataItem>("Save", "Check the items you would like to have saved.", dataToSaveList);
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
    public boolean aboutToShutdown()
    {
        super.aboutToShutdown();
        
        if (hasChanged)
        {
            
        }
        return true;
    }

    

    
    
    class GridCellEditor extends AbstractCellEditor implements TableCellEditor
    {
        protected JTextField textField = new JTextField();

        public GridCellEditor()
        {

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
            
            showPanel(((DropDownButtonStateful)ae.getSource()).getCurrentIndex());
        }
    }
}

