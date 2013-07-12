/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms.formatters;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * 
 * @author Ricardo
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class DataObjFieldFormatMultiplePanel extends DataObjFieldFormatPanel implements ChangeListener
{

    protected static final String FIELD_VALUE_COL;
    protected static final String DISPLAY_FORMAT_COL;
    protected static final String ELLIPSIS_BUTTON_COL = "";

    protected final String       ellipsisButtonLabel = "...";

    protected JTable             formatSwitchTbl;
    protected EditDeleteAddPanel controlPanel;
    
    static 
    {
        FIELD_VALUE_COL     = getResourceString("DOF_FIELD_VALUE");
        DISPLAY_FORMAT_COL  = getResourceString("DOF_DISPLAY_FORMAT");
    }
    
    /**
     * @param tableInfo
     * @param availableFieldsComp
     * @param formatContainer
     * @param okButton
     * @param uiFieldFormatterMgrCache
     */
    public DataObjFieldFormatMultiplePanel(final DBTableInfo                          tableInfo,
                                           final DataObjSwitchFormatterContainerIface formatContainer,
                                           final DataObjFieldFormatMgr                dataObjFieldFormatMgrCache,
                                           final UIFieldFormatterMgr                  uiFieldFormatterMgrCache,    
                                           final ChangeListener                       listener,
                                           final JButton                              okButton)
    {
        super(tableInfo, formatContainer, dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache, listener, okButton);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatPanelBuilder#buildUI()
     */
    protected void buildUI() 
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "10px,f:130px:g,10px,p,15px"), this);

        formatSwitchTbl = new JTable(new DefaultTableModel());
        formatSwitchTbl.getSelectionModel().addListSelectionListener(new RowListener());
        addTableModelListener((DefaultTableModel) formatSwitchTbl.getModel());
        fillWithObjFormatter(null);

        // tool bar to host the add and delete buttons
        createToolbar();

        // lay out components on main panel
        JScrollPane sp = UIHelper.createScrollPane(formatSwitchTbl);
        // set minimum and preferred sizes so that table shrinks with the dialog
        sp.setMinimumSize(new Dimension(50, 5));
        sp.setPreferredSize(new Dimension(50, 5));

        pb.add(sp, cc.xy(1, 2));
        pb.add(controlPanel, cc.xy(1, 4));
        this.mainPanelBuilder = pb;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatPanelBuilder#enableUIControls()
     */
    public void enableUIControls() 
    {
        okButton.setEnabled(!isInError());
        controlPanel.getDelBtn().setEnabled(formatSwitchTbl.getSelectedRowCount() > 0);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatPanelBuilder#isInError()
     */
    @Override
    public boolean isInError()
    {
        return super.isInError() || !isValidFormatter();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatPanelBuilder#getMainPanelBuilder()
     */
    public PanelBuilder getMainPanelBuilder() 
    {
        return mainPanelBuilder;
    }

    /**
     * @param model
     */
    protected void addTableModelListener(DefaultTableModel model)
    {
        TableModelListener tml = new TableModelListener()
        {
            public void tableChanged(TableModelEvent e)
            {
                int               row        = e.getFirstRow();
                int               column     = e.getColumn();
                DefaultTableModel localModel = (DefaultTableModel) e.getSource();
                String            columnName = localModel.getColumnName(column);
                
                if (columnName.equals(FIELD_VALUE_COL))
                {
                    int                    formatColumn = formatSwitchTbl.getColumnModel().getColumnIndex(DISPLAY_FORMAT_COL);
                    DataObjDataFieldFormat format       = (DataObjDataFieldFormat) localModel.getValueAt(row, formatColumn);
                    String                 value        = (String) localModel.getValueAt(row, column);
                    format.setValue(value);
                    enableUIControls();
                }
            }
        };
        model.addTableModelListener(tml);        
    }
    
    /**
     * @return
     */
    protected boolean isValidFormatter()
    {
        // check if there's an empty row in the switch formatter table
        DefaultTableModel model = (DefaultTableModel) formatSwitchTbl.getModel();
        
        if (model.getRowCount() == 0)
        {
            return false; // formatter is not valid if there are no internal formatters attached to it
        }
     
        Set<String> valueSet = new HashSet<String>();
        // check if there are valid values in each row
        for (int i = 0; i < model.getRowCount(); ++i)
        {
            for (int j = 0; j <= 1; ++j)
            {
                Object obj = model.getValueAt(i, j);
                String value = obj.toString(); 
                if (obj == null || StringUtils.isEmpty(value))
                {
                    return false;
                }
                // also check if field value is unique (column 0)
                if (j == 0)
                {
                    // value already in the set: so it's not unique
                    if (valueSet.contains(value))
                        return false;
                    
                    valueSet.add(value);
                }
            }
        }
        return true;
    }
    
    /**
     * @return
     */
    private DefaultTableModel getCleanTableModel() 
    {
        DefaultTableModel model = new DefaultTableModel() 
        {
            public boolean isCellEditable(int row, int column) 
            {
                return (column != 1);
            }
        };
        model.addColumn(FIELD_VALUE_COL);
        model.addColumn(DISPLAY_FORMAT_COL);
        model.addColumn(ELLIPSIS_BUTTON_COL);

        addTableModelListener(model);
        return model;
    }

    private void createToolbar() 
    {
        ActionListener addAL = new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                DefaultTableModel      model = (DefaultTableModel) formatSwitchTbl.getModel();
                DataObjSwitchFormatter fmt   = formatContainer.getSelectedFormatter();
                DataObjDataFieldFormat fld   = new DataObjDataFieldFormat();
                fmt.add(fld);
                model.addRow(new Object[] {fld.getValue(), fld, ellipsisButtonLabel});
                setHasChanged(true);
                enableUIControls();
            }
        };

        ActionListener delAL = new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                int                    formatColumn = formatSwitchTbl.getColumn(DISPLAY_FORMAT_COL).getModelIndex();
                DefaultTableModel      model        = (DefaultTableModel) formatSwitchTbl.getModel();
                DataObjSwitchFormatter fmt          = formatContainer.getSelectedFormatter();
                int[]                  rows         = formatSwitchTbl.getSelectedRows();
                // sort rows in reverse order otherwise removing the first rows
                // will mess up with the row numbers
                Integer[] intRows = new Integer[rows.length];
                for (int i = 0; i < rows.length; ++i) 
                {
                    intRows[i] = new Integer(rows[i]);
                }
                Arrays.sort(intRows, Collections.reverseOrder());
                for (int currentRow : intRows) 
                {
                    fmt.remove((DataObjDataFieldFormatIFace) model.getValueAt(currentRow, formatColumn));
                    model.removeRow(currentRow);
                }
                formatSwitchTbl.clearSelection();
                setHasChanged(true);
                enableUIControls();
            }
        };

        controlPanel = new EditDeleteAddPanel(null, delAL, addAL);
        controlPanel.getAddBtn().setEnabled(true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatPanelBuilder#fillWithObjFormatter(edu.ku.brc.ui.forms.formatters.DataObjSwitchFormatter)
     */
    public void fillWithObjFormatter(final DataObjSwitchFormatter switchFormatter) 
    {
        // display each formatter as a table row
        // DefaultTableModel tableModel = (DefaultTableModel)
        // formatSwitch.getModel();
        DefaultTableModel model = getCleanTableModel();

        if (switchFormatter != null) 
        {
            Vector<DataObjDataFieldFormatIFace> formatters = new Vector<DataObjDataFieldFormatIFace>(switchFormatter.getFormatters());
            for (DataObjDataFieldFormatIFace formatter : formatters) 
            {
                model.addRow(new Object[] { formatter.getValue(), formatter, ellipsisButtonLabel });
            }
        }

        formatSwitchTbl.setModel(model);
        setFormatSwitchTblColumnProperties();
    }
    
    /**
     * 
     */
    private void setFormatSwitchTblColumnProperties()
    {
        // set details of 1st column (field values)
        TableColumnModel model = formatSwitchTbl.getColumnModel();
        TableColumn column = model.getColumn(model.getColumnIndex(FIELD_VALUE_COL));
        column.setMinWidth(20);
        column.setMaxWidth(300);
        column.setPreferredWidth(70);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        column.setCellRenderer(renderer);

        // set details of 3rd column (ellipsis buttons)
        column = model.getColumn(model.getColumnIndex(ELLIPSIS_BUTTON_COL));
        column.setCellRenderer(new EditDataObjFormatButtonRenderer());
        column.setCellEditor(new EditDataObjFormatButtonEditor(createCheckBox()));
        column.setMinWidth(20);
        column.setMaxWidth(20);
        column.setPreferredWidth(20);
    }

    private class RowListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) 
        {
            if (event.getValueIsAdjusting())
            {
                return;
            }
            
            enableUIControls();
        }
    }

    /*
     * Table cell renderer that renders ellipsis button that opens format editor
     */
    protected class EditDataObjFormatButtonRenderer extends JButton implements TableCellRenderer 
    {
        public EditDataObjFormatButtonRenderer() 
        {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, 
                                                       boolean isSelected, boolean hasFocus, 
                                                       int row, int column) 
        {
            if (isSelected) 
            {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else 
            {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            setText(ellipsisButtonLabel);
            return this;
        }
    }

    /*
     * Table cell editor that forwards events to rendered ellipsis buttons on
     * the table
     */
    protected class EditDataObjFormatButtonEditor extends DefaultCellEditor 
    {
        protected JButton button;
        private boolean   isPushed;
        private JTable    table;
        private int       row;

        public EditDataObjFormatButtonEditor(final JCheckBox checkBox) 
        {
            super(checkBox);
            
            button = createButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() 
            {
                public void actionPerformed(ActionEvent e) 
                {
                    fireEditingStopped();
                }
            });
        }

        public Component getTableCellEditorComponent(JTable tableArg,
                                                     Object value, 
                                                     boolean isSelected, 
                                                     int rowArg, 
                                                     int column) 
        {
            if (isSelected) 
            {
                button.setForeground(tableArg.getSelectionForeground());
                button.setBackground(tableArg.getSelectionBackground());
            } else 
            {
                button.setForeground(tableArg.getForeground());
                button.setBackground(tableArg.getBackground());
            }
            button.setText(ellipsisButtonLabel);
            isPushed = true;

            this.table = tableArg;
            this.row = rowArg;

            return button;
        }

        public Object getCellEditorValue() 
        {
            if (isPushed) 
            {
                // get formatter object that corresponds to the pressed button
                int    formatCol = table.getColumn(DISPLAY_FORMAT_COL).getModelIndex();
                Object fieldObj  = table.getValueAt(row, formatCol);
                if (fieldObj instanceof DataObjDataFieldFormatIFace) 
                {
                    DataObjDataFieldFormatIFace formatter = (DataObjDataFieldFormatIFace) fieldObj;

                    // open dialog to edit format
                    DataObjFieldFormatSingleDlg dlg = new DataObjFieldFormatSingleDlg((Frame) UIRegistry.getTopWindow(), 
                                                                                       tableInfo, 
                                                                                       formatter,
                                                                                       dataObjFieldFormatMgrCache,
                                                                                       uiFieldFormatterMgrCache);
                    dlg.setVisible(true);

                    // save format back to table row data object
                    if (dlg.getBtnPressed() == CustomDialog.OK_BTN)
                    {
                        
                        DataObjSwitchFormatter fmt = formatContainer.getSelectedFormatter();
                        DataObjDataFieldFormatIFace field = dlg.getSingleFormatter();
                        fmt.set(row, field);
                        table.setValueAt(field, row, formatCol);
                        
                        // get field value from table and assign it to newly created formatter
                        int    valueCol = table.getColumn(FIELD_VALUE_COL).getModelIndex();
                        String value    = (String) table.getValueAt(row, valueCol);
                        field.setValue(value);
                        
                        setHasChanged(true);
                        // update ok button based on results
                        enableUIControls();
                    }
                }
            }
            isPushed = false;
            return new String(ellipsisButtonLabel);
        }

        public boolean stopCellEditing() 
        {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() 
        {
            super.fireEditingStopped();
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        listener.stateChanged(e);
    }
    
    
}
