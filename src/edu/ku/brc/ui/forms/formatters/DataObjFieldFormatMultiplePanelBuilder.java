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

package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.UIRegistry;

public class DataObjFieldFormatMultiplePanelBuilder extends DataObjFieldFormatPanelBuilder 
	{

	protected final String ellipsisButtonLabel = "...";

	protected JTable formatSwitchTbl;
	protected JButton addRowBtn;
	protected JButton delRowBtn;
	protected JButton resetRowBtn;

	DataObjFieldFormatMultiplePanelBuilder(DBTableInfo           tableInfo,
					                       JList                 formatList, 
					                       ListSelectionListener formatListSL,
					                       JButton               okButton) 
	{
		super(tableInfo, formatList, formatListSL, okButton);
	}

	protected void buildUI() 
	{
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g",
				"10px,f:130px:g,10px,p,15px")/* , new FormDebugPanel() */);

		formatSwitchTbl = new JTable(new DefaultTableModel());
		formatSwitchTbl.getSelectionModel().addListSelectionListener(new RowListener());
		fillWithObjFormatter(null);

		// tool bar (which hosts the add and delete buttons)
		PanelBuilder deletePB = new PanelBuilder(new FormLayout("p,p,p", "p"));
		addRowBtn = new JButton(getResourceString("Add"));
		delRowBtn = new JButton(getResourceString("Delete"));
		resetRowBtn = new JButton(getResourceString("Reset"));
		deletePB.add(addRowBtn, cc.xy(1, 1));
		deletePB.add(delRowBtn, cc.xy(2, 1));
		deletePB.add(resetRowBtn, cc.xy(3, 1));
		hookToolbarActionListeners();

		// lay out components on main panel
		pb.add(new JScrollPane(formatSwitchTbl), cc.xy(1, 2));
		pb.add(deletePB.getPanel(), cc.xy(1, 4));
		this.mainPanelBuilder = pb;
	}

	public void enableUIControls() 
	{
		DefaultTableModel model = (DefaultTableModel) formatSwitchTbl.getModel();
		delRowBtn.setEnabled(formatSwitchTbl.getSelectedRowCount() > 0);
		resetRowBtn.setEnabled(model.getRowCount() > 0);
		
		if (okButton != null)
		{
			okButton.setEnabled(isValidFormatter());
		}
	}

	public JPanel getPanel() 
	{
		return mainPanelBuilder.getPanel();
	}

	public PanelBuilder getMainPanelBuilder() 
	{
		return mainPanelBuilder;
	}

	protected boolean isValidFormatter()
	{
		// check if there's an empty row in the switch formatter table
		DefaultTableModel model = (DefaultTableModel) formatSwitchTbl.getModel();
		
		if (model.getRowCount() == 0)
		{
			// formatter is not valid if there are no internal formatters attached to it
			return false;
		}
		
		for (int i = 0; i < model.getRowCount(); ++i)
		{
			for (int j = 0; j <= 1; ++j)
			{
				if (StringUtils.isEmpty(model.getValueAt(i, j).toString()))
					return false;
			}
		}
		return true;
	}
	
	public DataObjSwitchFormatter getSwitchFormatter() 
	{
		String formatterName = "";
		String formatterField = "";
		
		// check whether format selected is an existing one
		if (formatList != null && formatList.getSelectedIndex() < formatList.getModel().getSize() - 1)
		{
			// selected entry is not the last one, so it's an existing one
			// however, the internal formatters may have changed
			// so, only keep formatter name and create a new one to replace the existing one
			Object value = formatList.getSelectedValue(); 
			if (value instanceof DataObjSwitchFormatter)
			{
				DataObjSwitchFormatter tmpFormatter = (DataObjSwitchFormatter) value;
				formatterName  = tmpFormatter.getName();
				formatterField = tmpFormatter.getFieldName();
			}
		}
		
		// note: if formatter is a new one, formatterField will be set on the parent dialog class (DataObjFieldFormatDlg)
		
		// return the new formatter that has been composed
		DataObjSwitchFormatter switchFormatter = new DataObjSwitchFormatter(
				formatterName, false, false, 
				tableInfo.getClassObj(), formatterField);
		
		// gather formatters for each table row
		DefaultTableModel model = (DefaultTableModel) formatSwitchTbl.getModel();
		for (int i = 0; i < model.getRowCount(); ++i)
		{
			Object obj   = model.getValueAt(i, 1);
			String value = (String) model.getValueAt(i, 0);
			if (obj instanceof DataObjDataFieldFormat)
			{
				DataObjDataFieldFormat subFormat = (DataObjDataFieldFormat) obj;
				subFormat.setValue(value);
				switchFormatter.add(subFormat);
			}
		}
		
		return switchFormatter;
	}

	private DefaultTableModel getCleanTableModel() 
	{
		DefaultTableModel model = new DefaultTableModel() 
		{
			public boolean isCellEditable(int row, int column) 
			{
				return (column != 1);
			}
		};
		model.addColumn("Field Value");
		model.addColumn("Display Format");
		model.addColumn("");

		return model;
	}

	private void hookToolbarActionListeners() 
	{
		ActionListener addBtnAL = new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				DefaultTableModel model = (DefaultTableModel) formatSwitchTbl.getModel();
				model.addRow(new Object[] {"", new DataObjDataFieldFormat(), ellipsisButtonLabel});
	            enableUIControls();
			}
		};
		addRowBtn.addActionListener(addBtnAL);

		ActionListener delBtnAL = new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				DefaultTableModel model = (DefaultTableModel) formatSwitchTbl.getModel();
				int[] rows = formatSwitchTbl.getSelectedRows();
				// sort rows in reverse order otherwise removing the first rows
				// will mess up with the row numbers
				Integer[] intRows = new Integer[rows.length];
				for (int i = 0; i < rows.length; ++i) {
					intRows[i] = new Integer(rows[i]);
				}
				Arrays.sort(intRows, Collections.reverseOrder());
				for (int currentRow : intRows) {
					model.removeRow(currentRow);
				}
				formatSwitchTbl.clearSelection();
	            enableUIControls();
			}
		};
		delRowBtn.addActionListener(delBtnAL);

		ActionListener resetBtnAL = new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				formatSwitchTbl.setModel(getCleanTableModel());
				setFormatSwitchTblColumnProperties();
	            enableUIControls();
			}
		};
		resetRowBtn.addActionListener(resetBtnAL);
	}

	public void fillWithObjFormatter(DataObjSwitchFormatter switchFormatter) 
	{
		// display each formatter as a table row
		// DefaultTableModel tableModel = (DefaultTableModel)
		// formatSwitch.getModel();
		DefaultTableModel model = getCleanTableModel();

		if (switchFormatter != null) 
		{
			Vector<DataObjDataFieldFormatIFace> formatters;
			formatters = new Vector<DataObjDataFieldFormatIFace>(switchFormatter.getFormatters());
			Collections.sort(formatters,new Comparator<DataObjDataFieldFormatIFace>() 
					{
						public int compare(DataObjDataFieldFormatIFace o1, DataObjDataFieldFormatIFace o2) 
						{
							return o1.getValue().compareTo(o2.getValue());
						}
					});
			for (DataObjDataFieldFormatIFace formatter : formatters) 
			{
				model.addRow(new Object[] { formatter.getValue(), formatter,
						ellipsisButtonLabel });
			}
		}

		formatSwitchTbl.setModel(model);
		setFormatSwitchTblColumnProperties();
	}
	
	private void setFormatSwitchTblColumnProperties()
	{
		// set details of 1st column (field values)
		TableColumn column = formatSwitchTbl.getColumnModel().getColumn(0);
		column.setMinWidth(20);
		column.setMaxWidth(300);
		column.setPreferredWidth(70);
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER);
		column.setCellRenderer(renderer);

		// set details of 3rd column (ellipsis buttons)
		column = formatSwitchTbl.getColumnModel().getColumn(2);
		column.setCellRenderer(new EditDataObjFormatButtonRenderer());
		column.setCellEditor(new EditDataObjFormatButtonEditor(new JCheckBox()));
		column.setMinWidth(20);
		column.setMaxWidth(20);
		column.setPreferredWidth(20);
	}

    private class RowListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) 
        {
            if (event.getValueIsAdjusting()) 
                return;
            
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
		private boolean isPushed;
		private JTable table;
		private int row;

		public EditDataObjFormatButtonEditor(JCheckBox checkBox) 
		{
			super(checkBox);
			button = new JButton();
			button.setOpaque(true);
			button.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent e) 
				{
					fireEditingStopped();
				}
			});
		}

		public Component getTableCellEditorComponent(JTable table,
													 Object value, boolean isSelected, 
													 int row, int column) 
		{
			if (isSelected) 
			{
				button.setForeground(table.getSelectionForeground());
				button.setBackground(table.getSelectionBackground());
			} else 
			{
				button.setForeground(table.getForeground());
				button.setBackground(table.getBackground());
			}
			button.setText(ellipsisButtonLabel);
			isPushed = true;

			this.table = table;
			this.row = row;

			return button;
		}

		public Object getCellEditorValue() 
		{
			if (isPushed) {
				// get formatter object that corresponds to the pressed button
				Object value = table.getValueAt(row, 1);
				if (value instanceof DataObjDataFieldFormatIFace) {
					DataObjDataFieldFormatIFace formatter = (DataObjDataFieldFormatIFace) value;

					// open dialog to edit format
					DataObjFieldFormatSingleDlg dlg;
					dlg = new DataObjFieldFormatSingleDlg((Frame) UIRegistry.getTopWindow(), tableInfo, formatter);
					dlg.setVisible(true);

					// save format back to table row data object
					DataObjDataFieldFormatIFace fmt = dlg.getSwitchFormatter().getSingle();
					table.setValueAt(fmt, row, 1);
					enableUIControls();
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
}
