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

package edu.ku.brc.af.ui.forms.formatters;

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createRadioButton;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.util.ComparatorByStringRepresentation;

/**
 * 
 * @author ricardo
 * @author rods
 *
 * @code_status Alpha
 *
 *
 */
public class DataObjFieldFormatDlg extends CustomDialog implements ChangeListener
{
    protected DBTableInfo                               tableInfo;
    protected DataObjFieldFormatMgr                     dataObjFieldFormatMgrCache;
    protected UIFieldFormatterMgr                       uiFieldFormatterMgrCache;
    
    protected DataObjSwitchFormatter                    dataObjFormatter;

    // UI controls
    protected AvailableFieldsComponent                  availableFieldComp;
    protected DataObjFieldFormatSinglePanel             fmtSingleEditingPanel;
    protected DataObjFieldFormatMultiplePanel           fmtMultipleEditingPanel;
    protected JComboBox                                 valueFieldCbo;
    protected JRadioButton                              singleDisplayBtn;
    protected JRadioButton                              multipleDisplayBtn;
    protected JTextField                                titleText;
    protected JTextField                                nameText;
    protected EditDeleteAddPanel                        controlPanel;
    
    protected boolean                                   hasChanged = false;
    protected boolean                                   isInError  = false;

    /**
     * @throws HeadlessException
     */
    public DataObjFieldFormatDlg(final CustomDialog           parentDlg, 
                                 final DBTableInfo            tableInfo, 
                                 final DataObjFieldFormatMgr  dataObjFieldFormatMgrCache,
                                 final UIFieldFormatterMgr    uiFieldFormatterMgrCache,
                                 final DataObjSwitchFormatter dataObjFormatter) throws HeadlessException
    {
        super(parentDlg, getResourceString("DOF_DLG_TITLE"), true, OKCANCELHELP, null);
        
        this.tableInfo                   = tableInfo;
        this.dataObjFieldFormatMgrCache  = dataObjFieldFormatMgrCache;
        this.uiFieldFormatterMgrCache    = uiFieldFormatterMgrCache;
        this.dataObjFormatter            = dataObjFormatter;
        this.helpContext                 = "DOF_EDITOR"; 
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

         JLabel titleLbl = createLabel(getResourceString("DOF_TITLE") + ":");
        titleText = createTextField(32);

        JLabel nameLbl = createLabel(getResourceString("DOF_NAME") + ":");
        nameText = createTextField(32);

        // radio buttons (single/multiple/external object display formats
        JLabel typeLbl     = createLabel(getResourceString("DOF_TYPE") + ":");
        singleDisplayBtn   = createRadioButton(getResourceString("DOF_SINGLE"));
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

        CellConstraints cc = new CellConstraints();

        // little panel to hold multiple display radio button and its combo box
        PanelBuilder multipleDisplayPB = new PanelBuilder(new FormLayout("l:p,f:p:g", "p"));
        multipleDisplayPB.add(multipleDisplayBtn, cc.xy(1, 1));
        multipleDisplayPB.add(valueFieldCbo, cc.xy(2, 1));

        // create field tree that will be re-used in all instances of single switch formatter editing panel
        availableFieldComp = new AvailableFieldsComponent(tableInfo, dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache);
        
        // format editing panels (dependent on the type for format: single/multiple)
        DataObjSwitchFormatterContainerIface formatterContainer = new DataObjSwitchFormatterSingleContainer(dataObjFormatter);
        fmtSingleEditingPanel   = new DataObjFieldFormatSinglePanel(tableInfo,   availableFieldComp, formatterContainer, uiFieldFormatterMgrCache, this);
        fmtMultipleEditingPanel = new DataObjFieldFormatMultiplePanel(tableInfo, availableFieldComp, formatterContainer, uiFieldFormatterMgrCache, this);

        // Panel for radio buttons and display formatting editing panel
        PanelBuilder pb = new PanelBuilder(new FormLayout("r:p,4px,f:p:g", "p,2px,p,10px,p,p,10px,f:p:g"));

        int y = 1;
        pb.add(nameLbl,   cc.xy(1, y));
        pb.add(nameText,  cc.xy(3, y)); y += 2;

        pb.add(titleLbl,  cc.xy(1, y));
        pb.add(titleText, cc.xy(3, y)); y += 2;

        pb.add(typeLbl,                       cc.xy(1, y));
        pb.add(singleDisplayBtn,             cc.xy(3, y)); y += 1;
        pb.add(multipleDisplayPB.getPanel(), cc.xy(3, y)); y += 2;
        
        // both panels occupy the same space
        pb.add(fmtSingleEditingPanel,   cc.xyw(1, y, 3));
        pb.add(fmtMultipleEditingPanel, cc.xyw(1, y, 3));

        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // after all is created, set initial selection on format list
        fillWithObjFormatter(dataObjFormatter, true);
        
        // title text field
        DocumentListener nameChangedDL = new DocumentListener()
        {
            public void removeUpdate(DocumentEvent e)  { changed(e); }
            public void insertUpdate(DocumentEvent e)  { changed(e); }
            public void changedUpdate(DocumentEvent e) { changed(e); }

            protected void changed(@SuppressWarnings("unused") DocumentEvent ev)
            {
                String name = nameText.getText();
                
                if (StringUtils.isEmpty(name) || dataObjFieldFormatMgrCache.getDataFormatter(name) != null)
                {
                    isInError = true;
                } else
                {
                    isInError = false;
                }
                
                dataObjFormatter.setTitle(titleText.getText());
                dataObjFormatter.setName(name);
                
                setHasChanged(true);
            }
        };

        //titleText.getDocument().addDocumentListener(nameChangedDL);
        nameText.getDocument().addDocumentListener(nameChangedDL);

        updateUIEnabled();

        packWithLargestPanel();
        
        okBtn.setEnabled(false);
    }

    /**
     * @param hasChanged
     */
    public void setHasChanged(boolean hasChanged)
    {
        if (this.hasChanged != hasChanged)
        {
           setWindowModified(hasChanged); 
        }
        this.hasChanged = hasChanged;
        updateUIEnabled();
    }

    /**
     * @return
     */
    protected List<DataObjSwitchFormatter> populateFormatterList()
    {
        // list of existing formats
        DefaultListModel listModel = new DefaultListModel();

        // add available data object formatters
        List<DataObjSwitchFormatter> fmtrs;
        fmtrs = dataObjFieldFormatMgrCache.getFormatterList(tableInfo.getClassObj());
        Collections.sort(fmtrs, new ComparatorByStringRepresentation<DataObjSwitchFormatter>());
        for (DataObjSwitchFormatter format : fmtrs)
        {
            listModel.addElement(format);
            //uniqueTitles.add(format.getTitle());
        }

        return fmtrs;
    }

    protected void packWithLargestPanel()
    {
        fmtSingleEditingPanel.setVisible(true);
        fmtMultipleEditingPanel.setVisible(true);

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
                    
                    dataObjFormatter.setSingle(btn == singleDisplayBtn);
                    dataObjFormatter.clearFields();
                    
                    fmtSingleEditingPanel.fillWithObjFormatter(dataObjFormatter);
                    fmtMultipleEditingPanel.fillWithObjFormatter(dataObjFormatter);
                    
                    setVisibleFormatPanel(btn);
                    fillWithObjFormatter(dataObjFormatter, false);
                    
                    fmtSingleEditingPanel.setHasChanged(true);
                    fmtMultipleEditingPanel.setHasChanged(true);
                    
                    updateUIEnabled();
                }
            }
        };

        singleDisplayBtn.addActionListener(displayTypeRadioBtnL);
        multipleDisplayBtn.addActionListener(displayTypeRadioBtnL);
    }
    
    /**
     * 
     */
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
    protected void fillWithObjFormatter(final DataObjSwitchFormatter fmt, final boolean isFirstTime)
    {
        if (fmt.getTitle() != null)
        {
            ViewFactory.changeTextFieldUIForDisplay(nameText, false);
        }
        
        titleText.setText(fmt.getTitle());
        nameText.setText(fmt.getName());

        boolean isSingle = fmt.isSingle();
        if (isSingle)
        {
            singleDisplayBtn.setSelected(true);
            setVisibleFormatPanel(singleDisplayBtn);
            fmtSingleEditingPanel.fillWithObjFormatter(fmt);
            
        } else
        {
            multipleDisplayBtn.setSelected(true);
            setVisibleFormatPanel(multipleDisplayBtn);
            fmtMultipleEditingPanel.fillWithObjFormatter(fmt);
        }


        // update combo even if formatter is single (in this case the combo will
        // be disabled anyway)
        updateValueFieldCombo(fmt);
        updateUIEnabled();
        
        
        setHasChanged(!isFirstTime);
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


    /**
     * 
     */
    public void valueFieldChanged()
    {
        Object obj = valueFieldCbo.getSelectedItem();
        if (!(obj instanceof DBFieldInfo))
        {
            // shouldn't get here... it should be a DBFieldInfo there... let's just bail out
            return;
        }

        DBFieldInfo field          = (DBFieldInfo) obj;
        String      fieldValueName = field.getName();
        if (dataObjFormatter != null)
        {
            dataObjFormatter.setFieldName(fieldValueName);
            setHasChanged(true);
        }
    }

    /**
     * @param btn
     */
    protected void setVisibleFormatPanel(final JRadioButton btn)
    {
        fmtSingleEditingPanel.setVisible(btn == singleDisplayBtn);
        fmtMultipleEditingPanel.setVisible(btn == multipleDisplayBtn);
        updateUIEnabled();
    }

    /**
     * 
     */
    protected void updateUIEnabled()
    {
        valueFieldCbo.setEnabled(multipleDisplayBtn.isSelected());
        
        boolean subPanelInError = false;
        if (singleDisplayBtn.isSelected())
        {
            subPanelInError = fmtSingleEditingPanel.isInError();
        } else
        {
            subPanelInError = fmtMultipleEditingPanel.isInError();
            fmtMultipleEditingPanel.enableUIControls();
        }
        okBtn.setEnabled(hasChanged && !isInError && !subPanelInError);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.ui.CustomDialog#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        super.cleanUp();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        setHasChanged(true);
    }
    
}
