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

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createList;
import static edu.ku.brc.ui.UIHelper.createRadioButton;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.AddRemoveEditPanel;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.util.ComparatorByStringRepresentation;

/**
 * @author ricardo
 *
 * @code_status Alpha
 *
 *
 */
public class DataObjFieldFormatDlg extends CustomDialog
{
	protected DBTableInfo								tableInfo;
	protected Vector<DataObjSwitchFormatter>			deletedFormats			= new Vector<DataObjSwitchFormatter>();
	protected Set<String>								uniqueTitles			= new HashSet<String>();
	protected int										initialFormatSelectionIndex;
	protected DataObjFieldFormatMgr 					dataObjFieldFormatMgrCache;
	protected UIFieldFormatterMgr 						uiFieldFormatterMgrCache;

	// UI controls
	protected JList										formatList;
	protected AvailableFieldsComponent					availableFieldComp;
	protected DataObjFieldFormatPanelBuilder			fmtSingleEditingPB;
	protected DataObjFieldFormatMultiplePanelBuilder	fmtMultipleEditingPB;
	protected JComboBox									valueFieldCbo;
	protected JRadioButton								singleDisplayBtn;
	protected JRadioButton								multipleDisplayBtn;
	protected JTextField								titleText;
	protected AddRemoveEditPanel						controlPanel;

	/**
	 * @throws HeadlessException
	 */
	public DataObjFieldFormatDlg(final Frame frame, 
								 final DBTableInfo tableInfo, 
								 int initialFormatSelectionIndex,
								 DataObjFieldFormatMgr dataObjFieldFormatMgrCache,
								 UIFieldFormatterMgr   uiFieldFormatterMgrCache) throws HeadlessException
	{
		super(frame, getResourceString("DOF_DLG_TITLE"), true, OKCANCELHELP, null);
		this.tableInfo                   = tableInfo;
		this.initialFormatSelectionIndex = initialFormatSelectionIndex;
		this.dataObjFieldFormatMgrCache  = dataObjFieldFormatMgrCache;
		this.uiFieldFormatterMgrCache    = uiFieldFormatterMgrCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.ku.brc.ui.CustomDialog#createUI()
	 */
	@Override
	public void createUI()
	{
		super.createUI();

		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(new FormLayout(
				"10px,f:200px,10px,f:p:g,10px", "10px," + // empty space on
															// top of panel
						"p," + // table name
						"10px," + // --- separator
						"p," + // help
						"10px," + // --- separator
						"p," + // available formats label,
						"f:p:g," + // available formats list
						"10px," + // --- separator
						"p," + // delete button
						"15px" // --- separator
		)/* , new FormDebugPanel() */);

		// table info
		PanelBuilder tblInfoPB = new PanelBuilder(new FormLayout("p,p", "p")/*
																			 * ,
																			 * new
																			 * FormDebugPanel()
																			 */);
		JLabel tableTitleLbl = createLabel(getResourceString("FFE_TABLE") + ": ");
		JLabel tableTitleValueLbl = createLabel(getResourceString(tableInfo.getTitle()));
		tableTitleValueLbl.setBackground(Color.WHITE);
		tableTitleValueLbl.setOpaque(true);

		tblInfoPB.add(tableTitleLbl, cc.xy(1, 1));
		tblInfoPB.add(tableTitleValueLbl, cc.xy(2, 1));

		JLabel helpLbl = createLabel(getResourceString("DOF_HELP"), SwingConstants.LEFT);

		List<DataObjSwitchFormatter> fmtrs = populateFormatterList();

		ActionListener addAL = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addNewFormatter();
			}
		};

		ActionListener delAL = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				deleteSelectedFormatter();
			}
		};

		// control panel
		controlPanel = new AddRemoveEditPanel(addAL, delAL, null);
		controlPanel.getAddBtn().setEnabled(true);

		// title text field
		DocumentListener titleChangedDL = new DocumentListener()
		{
			public void removeUpdate(DocumentEvent e)  { changed(e); }
			public void insertUpdate(DocumentEvent e)  { changed(e); }
			public void changedUpdate(DocumentEvent e) { changed(e); }

			protected void changed(DocumentEvent e)
			{
				DataObjSwitchFormatter fmt = getSelectedFormatter();
				if (fmt != null)
				{
					fmt.setTitle(titleText.getText());
				}
			}
		};

		JLabel titleLbl = createLabel(getResourceString("DOF_TITLE") + ":");
		titleText = createTextField(32);
		titleText.getDocument().addDocumentListener(titleChangedDL);

		// radio buttons (single/multiple/external object display formats
		JLabel typeLbl = createLabel(getResourceString("DOF_TYPE") + ":");
		singleDisplayBtn = createRadioButton(getResourceString("DOF_SINGLE"));
		multipleDisplayBtn = createRadioButton(getResourceString("DOF_MULTIPLE") + ":");
		singleDisplayBtn.setSelected(true);

		ButtonGroup displayTypeGrp = new ButtonGroup();
		displayTypeGrp.add(singleDisplayBtn);
		displayTypeGrp.add(multipleDisplayBtn);
		addDisplayTypeRadioButtonListeners();

		// combo box that lists fields that can be selected when multiple
		// display radio button is selected
		DefaultComboBoxModel cboModel = new DefaultComboBoxModel();
		valueFieldCbo = createComboBox(cboModel);
		addValueFieldsToCombo(null);
		addValueFieldCboAL();

		// little panel to hold multiple display radio button and its combo box
		PanelBuilder multipleDisplayPB = new PanelBuilder(new FormLayout(
				"l:p,f:p:g", "p"));
		multipleDisplayPB.add(multipleDisplayBtn, cc.xy(1, 1));
		multipleDisplayPB.add(valueFieldCbo, cc.xy(2, 1));

		// create field tree that will be re-used in all instances of single switch formatter editing panel
		availableFieldComp = new AvailableFieldsComponent(tableInfo, dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache);
		// format editing panels (dependent on the type for format: single/multiple)
		DataObjSwitchFormatterContainerIface formatterContainer = new DataObjSwitchFormatterListContainer(formatList);
		fmtSingleEditingPB = new DataObjFieldFormatSinglePanelBuilder(tableInfo, availableFieldComp, 
				formatterContainer, getOkBtn(), uiFieldFormatterMgrCache);
		fmtMultipleEditingPB = new DataObjFieldFormatMultiplePanelBuilder(tableInfo, availableFieldComp, 
				formatterContainer, getOkBtn(), uiFieldFormatterMgrCache);

		// panel for radio buttons and display formatting editing panel
		PanelBuilder rightPB = new PanelBuilder(new FormLayout("r:p,4px,f:p:g",
				"p,10px,p,p,10px,f:p:g")/* , new FormDebugPanel() */);

		int yr = 1;
		rightPB.add(titleLbl,  cc.xy(1, yr));
		rightPB.add(titleText, cc.xy(3, yr)); yr += 2;

		rightPB.add(typeLbl, 					  cc.xy(1, yr));
		rightPB.add(singleDisplayBtn,             cc.xy(3, yr)); yr += 1;
		rightPB.add(multipleDisplayPB.getPanel(), cc.xy(3, yr)); yr += 2;
		
		// both panels occupy the same space
		rightPB.add(fmtSingleEditingPB.getPanel(),   cc.xyw(1, yr, 3));
		rightPB.add(fmtMultipleEditingPB.getPanel(), cc.xyw(1, yr, 3));

		// lay out components on main panel
		int y = 2; // leave first row blank
		pb.add(tblInfoPB.getPanel(), cc.xyw(2, y, 3)); y += 2;
		pb.add(helpLbl,              cc.xyw(2, y, 3)); y += 2;

		pb.add(createLabel(getResourceString("DOF_DISPLAY_FORMATS") + ":",
				SwingConstants.LEFT), cc.xy(2, y)); y += 1;
				
		int y2 = y; // align 3rd column with this row
		pb.add(new JScrollPane(formatList), cc.xy(2, y)); y += 2;
		pb.add(controlPanel,                cc.xy(2, y)); y += 2;

		// 2nd column (4th if you count the spaces between them)
		pb.add(rightPB.getPanel(), cc.xywh(4, y2, 1, 4));

		contentPanel = pb.getPanel();
		mainPanel.add(contentPanel, BorderLayout.CENTER);

		// after all is created, set initial selection on format list
		if (fmtrs.size() > 0 && initialFormatSelectionIndex < fmtrs.size())
		{
			formatList.setSelectedIndex(initialFormatSelectionIndex);
		} else
		{
			fillWithObjFormatter(null);
		}

		updateUIEnabled();

		packWithLargestPanel();
	}

	protected List<DataObjSwitchFormatter> populateFormatterList()
	{
		// list of existing formats
		DefaultListModel listModel = new DefaultListModel();

		// add available data object formatters
		List<DataObjSwitchFormatter> fmtrs;
		fmtrs = dataObjFieldFormatMgrCache.getFormatterList(tableInfo.getClassObj());
		Collections.sort(fmtrs,
				new ComparatorByStringRepresentation<DataObjSwitchFormatter>());
		for (DataObjSwitchFormatter format : fmtrs)
		{
			listModel.addElement(format);
			uniqueTitles.add(format.getTitle());
		}

		formatList = createList(listModel);
		formatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		addFormatListSelectionListener();
		addFormatListMouseListener();

		return fmtrs;
	}

	protected void packWithLargestPanel()
	{
		fmtSingleEditingPB.getPanel().setVisible(true);
		fmtMultipleEditingPB.getPanel().setVisible(true);

		pack();

		// restore selection
		setVisibleFormatPanel((singleDisplayBtn.isSelected()) ? singleDisplayBtn : multipleDisplayBtn);
	}

	protected void addDisplayTypeRadioButtonListeners()
	{
		ActionListener displayTypeRadioBtnL = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (e.getSource() instanceof JRadioButton)
				{
					JRadioButton btn = (JRadioButton) e.getSource();
					DataObjSwitchFormatter fmt = getSelectedFormatter();
					if (fmt != null)
					{
						fmt.setSingle(btn == singleDisplayBtn);
					}
					setVisibleFormatPanel(btn);
					updateUIEnabled();
				}
			}
		};

		singleDisplayBtn.addActionListener(displayTypeRadioBtnL);
		multipleDisplayBtn.addActionListener(displayTypeRadioBtnL);
	}

	public void addValueFieldCboAL()
	{
		ActionListener valueFieldCboAL = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				valueFieldChanged();
			}
		};
		valueFieldCbo.addActionListener(valueFieldCboAL);
	}

	/*
	 * Populates the dialog controls with data from a given formatter
	 */
	protected void fillWithObjFormatter(DataObjSwitchFormatter fmt)
	{
		titleText.setText((fmt != null)? fmt.getTitle() : "");

		boolean isSingle = (fmt == null) ? singleDisplayBtn.isSelected() : fmt.isSingle();
		if (isSingle)
		{
			singleDisplayBtn.setSelected(true);
			setVisibleFormatPanel(singleDisplayBtn);
			fmtSingleEditingPB.fillWithObjFormatter(fmt);
		} else
		{
			multipleDisplayBtn.setSelected(true);
			setVisibleFormatPanel(multipleDisplayBtn);
			fmtMultipleEditingPB.fillWithObjFormatter(fmt);
		}

		// update combo even if formatter is single (in this case the combo will
		// be disabled anyway)
		updateValueFieldCombo(fmt);
		updateUIEnabled();
	}

	/**
	 * Populates the field value combo with fields and leaves the right one
	 * selected (for multiple formats)
	 */
	protected void addValueFieldsToCombo(DataObjSwitchFormatter switchFormatter)
	{
		// clear combo box list
		DefaultComboBoxModel cboModel = (DefaultComboBoxModel) valueFieldCbo.getModel();
		cboModel.removeAllElements();

		// add fields to combo box
		List<DBFieldInfo> fields = tableInfo.getFields();
		int selectedFieldIndex = -1;
		for (int i = 0; i < fields.size(); ++i)
		{
			DBFieldInfo currentField = fields.get(i);
			cboModel.addElement(currentField);
			if (switchFormatter != null && 
					currentField.getName().equals(switchFormatter.getFieldName()))
			{
				// found the selected field
				selectedFieldIndex = i;
			}
		}

		// set selected field
		if (selectedFieldIndex != -1)
		{
			valueFieldCbo.setSelectedIndex(selectedFieldIndex);
		}
	}

	/**
	 * Select appropriate field value from combo box
	 */
	protected void updateValueFieldCombo(DataObjSwitchFormatter switchFormatter)
	{
		if (switchFormatter == null || switchFormatter.getFieldName() == null)
			return;
		
		DefaultComboBoxModel cboModel = (DefaultComboBoxModel) valueFieldCbo.getModel();
		cboModel.setSelectedItem(tableInfo.getFieldByName(switchFormatter.getFieldName()));
	}

	private void addFormatListMouseListener()
	{
		MouseAdapter mAdp = new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					int index = formatList.locationToIndex(e.getPoint());
					formatList.ensureIndexIsVisible(index);
					okButtonPressed();
				}
			}
		};

		formatList.addMouseListener(mAdp);
	}

	private void addFormatListSelectionListener()
	{
		ListSelectionListener formatListSL = new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
					return;

				JList theList = (JList) e.getSource();
				if (theList.isSelectionEmpty())
					return;

				Object selValue = theList.getSelectedValue();
				if (selValue instanceof DataObjSwitchFormatter)
				{
					fillWithObjFormatter((DataObjSwitchFormatter) selValue);
				} else
				{
					fillWithObjFormatter(null);
				}

				updateUIEnabled();
			}
		};

		formatList.addListSelectionListener(formatListSL);
	}

	protected void addNewFormatter()
	{
		String newTitle = DataObjFieldFormatMgr.getUniqueName(tableInfo.getTitle(), " ", uniqueTitles);
		
		// formatter name will be set when inserted into hash of formatters by fmt manager
		DataObjSwitchFormatter fmt = new DataObjSwitchFormatter("", newTitle,
				singleDisplayBtn.isSelected(), true, tableInfo.getClassObj(), "");
		DefaultListModel listModel = (DefaultListModel) formatList.getModel();
		listModel.addElement(fmt);
		uniqueTitles.add(newTitle);
		formatList.setSelectedIndex(listModel.getSize() - 1);
	}

	protected void deleteSelectedFormatter()
	{
		DataObjSwitchFormatter selectedFormat = getSelectedFormatter();
		if (selectedFormat == null)
			return;

		deletedFormats.add(selectedFormat);
		uniqueTitles.remove(selectedFormat.getTitle());

		int index = formatList.getSelectedIndex();
		DefaultListModel model = (DefaultListModel) formatList.getModel();
		model.removeElement(selectedFormat);

		if (model.getSize() == 0)
		{
			// formatter list is now empty
			fillWithObjFormatter(null);
			updateUIEnabled();
			return;
		}

		// else
		index = (index >= model.getSize()) ? index - 1 : index;
		formatList.setSelectedIndex(index);
	}

	public void valueFieldChanged()
	{
		if (formatList == null)
			return;

		Object obj = valueFieldCbo.getSelectedItem();
		if (!(obj instanceof DBFieldInfo))
			// shouldn't get here... it should be a DBFieldInfo there... let's just bail out
			return;

		DBFieldInfo field = (DBFieldInfo) obj;
		String fieldValueName = field.getName();
		DataObjSwitchFormatter fmt = getSelectedFormatter();
		if (fmt != null)
		{
			fmt.setFieldName(fieldValueName);
		}
	}

	protected void setVisibleFormatPanel(JRadioButton btn)
	{
		fmtSingleEditingPB.getPanel().setVisible(btn == singleDisplayBtn);
		fmtMultipleEditingPB.getPanel().setVisible(btn == multipleDisplayBtn);
	}

	public DataObjSwitchFormatter getSelectedFormatter()
	{
		// get current formatter
		Object value = formatList.getSelectedValue();
		if (!(value instanceof DataObjSwitchFormatter))
		{
			return null;
		}

		DataObjSwitchFormatter fmt = (DataObjSwitchFormatter) value;
		return fmt;
	}

	/**
	 * 
	 */
	protected void updateUIEnabled()
	{
		valueFieldCbo.setEnabled(multipleDisplayBtn.isSelected());
		controlPanel.getDelBtn().setEnabled(formatList.getSelectedIndex() != -1);

		if (singleDisplayBtn.isSelected())
		{
			fmtSingleEditingPB.enableUIControls();
		} else
		{
			fmtMultipleEditingPB.enableUIControls();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
	 */
	@Override
	protected void okButtonPressed()
	{
		// remove non-system formatters marked for deletion Iterator<UIFieldFormatterIFace>
		Iterator<DataObjSwitchFormatter> it = deletedFormats.iterator(); 
		while (it.hasNext()) 
		{
			dataObjFieldFormatMgrCache.removeFormatter(it.next()); 
		}

		// save formatters
    	DefaultListModel listModel = (DefaultListModel) formatList.getModel();
    	Enumeration<?> elements = listModel.elements();
    	while (elements.hasMoreElements())
    	{
    		Object obj = elements.nextElement();
    		if (obj instanceof DataObjSwitchFormatter)
    		{
           		// add formatter to list of existing ones and save it
    			DataObjSwitchFormatter fmt = (DataObjSwitchFormatter) obj;
   				dataObjFieldFormatMgrCache.addFormatter(fmt);
    		}
    	}

		super.okButtonPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.ku.brc.ui.CustomDialog#cleanUp()
	 */
	@Override
	public void cleanUp()
	{
		// TODO Auto-generated method stub
		super.cleanUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.ku.brc.ui.CustomDialog#getOkBtn()
	 */
	@Override
	public JButton getOkBtn()
	{
		// TODO Auto-generated method stub
		return super.getOkBtn();
	}
}
