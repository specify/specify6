/* Copyright (C) 2015, University of Kansas Center for Research
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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
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
    protected DataObjFieldFormatSinglePanel             fmtSingleEditingPanel;
    protected DataObjFieldFormatMultiplePanel           fmtMultipleEditingPanel;
    protected JComboBox                                 valueFieldCbo;
    protected JRadioButton                              singleDisplayBtn;
    protected JRadioButton                              multipleDisplayBtn;
    protected JTextField                                titleText;
    protected JTextField                                nameText;
    protected EditDeleteAddPanel                        controlPanel;
    
    protected JPanel                                    customEditor = null;
    
    protected boolean                                   hasChanged   = false;
    protected boolean                                   isInError    = false;
    
    protected boolean                                   isEditable   = true;

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
        
        CellConstraints cc = new CellConstraints();

        if (dataObjFormatter.getFormatters().size() == 1)
        {
            DataObjDataFieldFormatIFace dof = dataObjFormatter.getFormatters().iterator().next();
            if (dof.isCustom())
            {
                if (dof.hasEditor())
                {
                    customEditor = dof.getCustomEditor(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e)
                        {
                            setHasChanged(true);
                        }
                    });
                } else
                {
                    isEditable = false;
                    UIRegistry.showLocalizedMsg("DOF_NO_CST_EDT");
                    return;
                }
            }
        }

        if (customEditor == null)
        {
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
            PanelBuilder multipleDisplayPB = new PanelBuilder(new FormLayout("l:p,f:p:g", "p"));
            multipleDisplayPB.add(multipleDisplayBtn, cc.xy(1, 1));
            multipleDisplayPB.add(valueFieldCbo, cc.xy(2, 1));
    
            // format editing panels (dependent on the type for format: single/multiple)
            DataObjSwitchFormatterContainerIface formatterContainer = new DataObjSwitchFormatterSingleContainer(dataObjFormatter);
            fmtSingleEditingPanel   = new DataObjFieldFormatSinglePanel(tableInfo, formatterContainer, dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache, this, getOkBtn());
            fmtMultipleEditingPanel = new DataObjFieldFormatMultiplePanel(tableInfo, formatterContainer, dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache, this, getOkBtn());
    
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
    
        } else
        {
            PanelBuilder pb = new PanelBuilder(new FormLayout("r:p,4px,f:p:g", "p,6px,p,2px,p,4px,p,10px"));
    
            int y = 1;
            pb.addSeparator(getResourceString("DOF_CST_ED"), cc.xyw(1,y,3)); y += 2;
            
            pb.add(nameLbl,   cc.xy(1, y));
            pb.add(nameText,  cc.xy(3, y)); y += 2;
    
            pb.add(titleLbl,  cc.xy(1, y));
            pb.add(titleText, cc.xy(3, y)); y += 2;

            String labelStr = dataObjFormatter.getFormatters().iterator().next().getLabel();
            if (StringUtils.isNotEmpty(labelStr))
            {
                pb.add(UIHelper.createFormLabel(labelStr),  cc.xy(1, y));
                pb.add(customEditor, cc.xy(3, y)); y += 2;
                
            } else
            {
                pb.add(customEditor, cc.xyw(1, y, 3)); y += 2;
            }

            pb.setDefaultDialogBorder();
            
            contentPanel = pb.getPanel();
        }
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // after all is created, set initial selection on format list
        fillWithObjFormatter(dataObjFormatter, true);
            
        // title text field
        DocumentAdaptor nameChangedDL = new DocumentAdaptor()
        {
            @Override
            protected void changed(DocumentEvent ev)
            {
                String name  = nameText.getText();
                String title = titleText.getText();
                
                isInError = (StringUtils.isEmpty(name) || dataObjFieldFormatMgrCache.getDataFormatter(name) != null);
                
                dataObjFormatter.setName(name);
                dataObjFormatter.setTitle(title);
                
                setHasChanged(true);
            }
        };

        titleText.getDocument().addDocumentListener(nameChangedDL);
        nameText.getDocument().addDocumentListener(nameChangedDL);

        updateUIEnabled();

        packWithLargestPanel();
    }

    /**
     * @return the isEditable
     */
    public boolean isEditable()
    {
        return isEditable;
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

    @Override
    protected void okButtonPressed()
    {
        dataObjFormatter.setTitle(titleText.getText());

        super.okButtonPressed();
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

    /**
     * 
     */
    protected void packWithLargestPanel()
    {
        if (customEditor == null)
        {
            fmtSingleEditingPanel.setVisible(true);
            fmtMultipleEditingPanel.setVisible(true);
        } else 
        {
            customEditor.setVisible(true);
        }

        pack();

        if (customEditor == null)
        {
            // restore selection
            setVisibleFormatPanel((singleDisplayBtn.isSelected()) ? singleDisplayBtn : multipleDisplayBtn);
        }
    }

    /**
     * 
     */
    protected void addDisplayTypeRadioButtonListeners()
    {
        ActionListener displayTypeRadioBtnL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (e.getSource() instanceof JRadioButton)
                {
                    JRadioButton btn = (JRadioButton) e.getSource();

//                    boolean hasChanged = (dataObjFormatter.isSingle() && btn != singleDisplayBtn) ||
//                                         (!dataObjFormatter.isSingle() && btn == singleDisplayBtn);

                    dataObjFormatter.setSingle(btn == singleDisplayBtn);

                    if (btn == singleDisplayBtn) 
                    {
                        // single editing panel selected
                        fmtSingleEditingPanel.setHasChanged(true);
                    }
                    else
                    {
                        // multiple editing panel selected
                        fmtMultipleEditingPanel.setHasChanged(true);
                    }

                    setVisibleFormatPanel(btn);
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

        if (customEditor == null)
        {
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
     * Select appropriate field value from combobox
     * @param switchFormatter
     */
    protected void updateValueFieldCombo(final DataObjSwitchFormatter switchFormatter)
    {
        if (switchFormatter == null || switchFormatter.getFieldName() == null)
        {
            return;
        }
        
        if (customEditor == null)
        {
            DefaultComboBoxModel cboModel = (DefaultComboBoxModel) valueFieldCbo.getModel();
            cboModel.setSelectedItem(tableInfo.getFieldByName(switchFormatter.getFieldName()));
        }
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
        if (customEditor == null)
        {
            fmtSingleEditingPanel.setVisible(btn == singleDisplayBtn);
            fmtMultipleEditingPanel.setVisible(btn == multipleDisplayBtn);
            updateUIEnabled();
        }
    }

    /**
     * 
     */
    protected void updateUIEnabled()
    {
        if (customEditor == null)
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
            okBtn.setEnabled(!isInError && !subPanelInError && 
                    StringUtils.isNotEmpty(nameText.getText()) &&
                    StringUtils.isNotEmpty(titleText.getText()));
        }
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
