package edu.ku.brc.af.ui.forms.formatters;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.validation.DataChangeListener;
import edu.ku.brc.af.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author Ricardo
 *
 * @code_status Alpha
 *
 */
public class DataObjAggregatorDlg extends CustomDialog implements DataChangeListener
{
    protected Frame                     aggDlgFrame; 
    protected DBTableInfo               tableInfo;
    protected DataObjAggregator         selectedAggregator;
    protected boolean                   newAggregator;
    protected DataObjFieldFormatMgr     dataObjFieldFormatMgrCache;
    protected UIFieldFormatterMgr       uiFieldFormatterMgrCache;
    
    // UI controls
    protected JComboBox                 displayCbo;
    protected JComboBox                 fieldOrderCbo;
    protected ValTextField              nameText;
    protected ValTextField              titleText;
    protected ValTextField              sepText;
    protected ValSpinner                countSpinner; 
    protected ValTextField              endingText;
    
    // listeners
    protected DocumentListener[]        textChangedDL    = new DocumentListener[5];
    protected ListSelectionListener     aggregatorListSL = null;
    protected ItemListener              checkBoxListener = null;
    protected ActionListener            cboAL            = null;
    
    protected boolean                   hasChanged       = false;
    protected boolean                   isNameInError    = false;
    protected FormValidator             validator        = new FormValidator(null);
    protected boolean                   isNew            = false;

    /**
     * @throws HeadlessException
     */
    public DataObjAggregatorDlg(final CustomDialog          parentDlg, 
                                final DBTableInfo           tableInfo, 
                                final DataObjFieldFormatMgr dataObjFieldFormatMgrCache,
                                final UIFieldFormatterMgr   uiFieldFormatterMgrCache,
                                final DataObjAggregator     selectedAggregator) 
        throws HeadlessException
    {
        super(parentDlg, getResourceString("DOA_DLG_TITLE"), true, OKCANCELHELP, null);
        
        this.tableInfo                  = tableInfo;
        this.dataObjFieldFormatMgrCache = dataObjFieldFormatMgrCache;
        this.uiFieldFormatterMgrCache   = uiFieldFormatterMgrCache;
        this.selectedAggregator         = selectedAggregator;
        this.helpContext                = "DOA_EDITOR";
        
        validator.addDataChangeListener(this);
        validator.addEnableRule("DOA_ENDING", "DOA_COUNT.getIntValue() > 0");
    }
    
    /**
     * @param key
     * @return
     */
    protected JLabel createI18NFormLabelBold(final String key)
    {
        JLabel lbl = createI18NFormLabel(key);
        Font   lblFont = lbl.getFont().deriveFont(Font.BOLD);
        lbl.setFont(lblFont);
        return lbl;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        isNew = StringUtils.isEmpty(selectedAggregator.getName());
        validator.setNewObj(isNew);
        
        CellConstraints cc = new CellConstraints();
        
        // panel for aggregator editing controls
        // display combobox with available data obj formatters
        PanelBuilder displayPB = new PanelBuilder(new FormLayout("f:p:g,min", "p"));

        displayCbo = createComboBox();
        JButton displayDlgBtn = createButton("...");
        displayPB.add(displayCbo,    cc.xy(1, 1));
        displayPB.add(displayDlgBtn, cc.xy(2, 1));
        
        fieldOrderCbo = createComboBox();
        
        ActionListener displayDlgBtnAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // subtract 1 from selected index to account for empty entry at the beginning
                // if selected index is zero, then select "new" entry in the dialog, which is the last one
                
                boolean newFormatter;
                DataObjSwitchFormatter dosf;
                if (displayCbo.getSelectedIndex() == 0)
                {
                    // no formatter selected: create a new one and pass it to the dialog so the user can edit it
                    newFormatter = true;
                    dosf = new DataObjSwitchFormatter(null, null, true, false, tableInfo.getClassObj(), "");
                } else
                {
                    // pass selected formatter to the dialog so the user can edit it
                    newFormatter = false;
                    dosf = (DataObjSwitchFormatter)displayCbo.getSelectedItem();                    
                }
                DataObjFieldFormatDlg dlg = new DataObjFieldFormatDlg(null, 
                                                                      tableInfo, 
                                                                      dataObjFieldFormatMgrCache, 
                                                                      uiFieldFormatterMgrCache,
                                                                      dosf);
                dlg.createUI();

                if (dlg.isEditable())
                {
                    dlg.setVisible(true);
                    
                    // set combo selection to formatter selected in dialog
                    if (dlg.getBtnPressed() == OK_BTN)
                    {
                        selectedAggregator.setFormatName(dosf.getName());
                        if (newFormatter)
                        {
                            // must add new DO formatter and then select it
                            dataObjFieldFormatMgrCache.addFormatter(dosf);
                        }
                        // update the display combo, adding the new formatter and selecting it
                        // if that's the case
                        updateDisplayCombo();
                    }
                }
            }
        };
        displayDlgBtn.addActionListener(displayDlgBtnAL);
        
        // text fields
        titleText    = new ValTextField(10);
        nameText     = new ValTextField(10)
        {
            @Override
            public ErrorType validateState()
            {
                ErrorType state =  super.validateState();
                if (state == ErrorType.Valid)
                {
                    return isNameInError ? ErrorType.Error : ErrorType.Valid;
                }
                return state;
            }
        };
        sepText      = new ValTextField(10);
        endingText   = new ValTextField(10);
        countSpinner = new ValSpinner(0, 10, true, false); 
        
        countSpinner.setValue(0);

        JButton      valBtn       = FormViewObj.createValidationIndicator(this, validator);
        PanelBuilder valInfoBtnPB = new PanelBuilder(new FormLayout("f:p:g,p", "p"));
        valInfoBtnPB.add(valBtn, cc.xy(2, 1));
        validator.setValidationBtn(valBtn);
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("r:p,2px,p,f:p:g", 
                                                          UIHelper.createDuplicateJGoodiesDef("p", "2px", 11)));
        int y = 1;
        
        JLabel       tableTitleLbl      = createI18NFormLabel("FFE_TABLE");
        JLabel       tableTitleValueLbl = createLabel(tableInfo.getTitle());
        tableTitleValueLbl.setBackground(Color.WHITE);
        tableTitleValueLbl.setOpaque(true);
        
        pb.add(tableTitleLbl,      cc.xy(1, y));
        pb.add(tableTitleValueLbl, cc.xyw(3, y, 2));
        y += 2;
        
        pb.addSeparator(" ", cc.xyw(1, y, 3));
        y += 2;
        
        pb.add(createI18NFormLabelBold("DOA_NAME"), cc.xy(1, y)); 
        add(pb, nameText, "DOA_NAME", "DOA_NAME.getText().length() > 0", cc.xyw(3, y, 2)); 
        y += 2;

        pb.add(createI18NFormLabelBold("DOA_TITLE"), cc.xy(1, y)); 
        add(pb, titleText, "DOA_TITLE", "DOA_TITLE.getText().length() > 0", cc.xyw(3, y, 2)); 
        y += 2;

        pb.add(createI18NFormLabelBold("DOA_DISPLAY"), cc.xy(1, y)); 
        pb.add(displayPB.getPanel(), cc.xyw(3, y, 2)); 
        y += 2;
        
        pb.add(createI18NFormLabelBold("DOA_SEP"), cc.xy(1, y)); 
        add(pb, sepText, "DOA_SEP", "DOA_SEP.getText().length() > 0", cc.xyw(3, y, 2)); 
        y += 2;

        pb.add(createI18NFormLabelBold("DOA_COUNT"), cc.xy(1, y)); 
        add(pb, countSpinner, "DOA_COUNT", null, cc.xyw(3, y, 2)); 
        y += 2;

        pb.add(createI18NFormLabel("DOA_ENDING"), cc.xy(1, y));
        add(pb, endingText, "DOA_ENDING", "DOA_ENDING.getText().length() > 0", cc.xyw(3, y, 2)); 
        y += 2;

        pb.add(createI18NFormLabel("DOA_SORT_BY"), cc.xy(1, y)); 
        add(pb, fieldOrderCbo, "DOA_DEFAULT", null, cc.xy(3, y)); 
        y += 2;

        pb.add(valInfoBtnPB.getPanel(), cc.xyw(3, y, 2)); 
        y += 2;

        pb.setBorder(BorderFactory.createEmptyBorder(14, 14, 0, 14));
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        fillWithObjAggregator(selectedAggregator);

        updateUIEnabled();
        
        addComboBoxActionListeners();
        
        if (!isNew)
        {
            ViewFactory.changeTextFieldUIForDisplay(nameText, false);
        }
        
        validator.resetFields();
        validator.setEnabled(true);
        validator.setHasChanged(false);

        pack();
        
        if (isNew)
        {
            nameText.getDocument().addDocumentListener(new DocumentListener() {
                
                public void removeUpdate(DocumentEvent e)  { changed(e); }
                public void insertUpdate(DocumentEvent e)  { changed(e); }
                public void changedUpdate(DocumentEvent e) { changed(e); }
                
                protected void changed(@SuppressWarnings("unused") DocumentEvent e)
                {
                    isNameInError = dataObjFieldFormatMgrCache.getAggregator(nameText.getText()) != null;
                    validator.validateForm();
                    updateUIEnabled();
                }
            });
        }
    }
    
    /**
     * @param pb
     * @param comp
     * @param ident
     * @param valRule
     * @param cc
     */
    protected void add(final PanelBuilder   pb, 
                       final JComponent     comp, 
                       final String         ident,
                       final String         valRule,
                       final CellConstraints cc)
    {
        pb.add(comp, cc);
        if (comp instanceof UIValidatable)
        {
            if (comp instanceof ValTextField)
            {
                validator.hookupTextField((ValTextField)comp, ident, true, UIValidator.Type.Changed, valRule, false);
                
            } else if (comp instanceof ValComboBox)
            {
                DataChangeNotifier dcn = validator.hookupComponent(comp, ident, UIValidator.Type.Changed, valRule, false);
                ((ValComboBox)comp).getComboBox().addActionListener(dcn);
                
            } else if (comp instanceof ValSpinner)
            {
                DataChangeNotifier dcn = validator.hookupComponent(comp, ident, UIValidator.Type.Changed, valRule, false);
                ((ValSpinner)comp).addChangeListener(dcn);
            }
            validator.addUILabel(ident, UIHelper.createI18NLabel(ident));
            
            if (comp instanceof UIValidatable)
            {
                ((UIValidatable)comp).setRequired(true);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#setVisible(boolean)
     */
    public void setVisible(final boolean visible)
    {
        if (visible)
        {
            createUI();
            
            Dimension size = getPreferredSize();
            size.width = Math.max(400, size.width);
            setSize(size);
        }
        super.setVisible(visible);
    }
    
    /**
     * 
     */
    private void addComboBoxActionListeners()
    {
        if (cboAL == null)
        {
            cboAL = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (e.getSource() == displayCbo)
                    {
                        Object item = displayCbo.getSelectedItem();
                        if (item instanceof DataObjSwitchFormatter)
                        {
                            // display format changed
                            DataObjSwitchFormatter fmt = (DataObjSwitchFormatter) item;
                            selectedAggregator.setFormatName(fmt.getName());
                            setHasChanged(true);
                        }
                    }
                    else if (e.getSource() == fieldOrderCbo)
                    {
                        Object item = fieldOrderCbo.getSelectedItem();
                        if (item instanceof DBFieldInfo)
                        {
                            // order by field changed
                            DBFieldInfo fi = (DBFieldInfo) item;
                            selectedAggregator.setOrderFieldName(fi.getName());
                            setHasChanged(true);
                        }
                    }
                }
            };
        }
        
        displayCbo.addActionListener(cboAL);
        fieldOrderCbo.addActionListener(cboAL);
    }
    
    /*
     * Populates the display format combo with available formatters for this table 
     */
    protected void updateDisplayCombo()
    {
        // save selected aggregator because that one will be reset when elements are removed from combo box model
        DataObjAggregator tempAgg = selectedAggregator;
        
        // clear combo box list
        DefaultComboBoxModel cboModel = (DefaultComboBoxModel) displayCbo.getModel();
        cboModel.removeAllElements();
        
        // add formatters to display combo box
        List<DataObjSwitchFormatter> fmts = dataObjFieldFormatMgrCache.getFormatterList(tableInfo.getClassObj());

        // add an empty entry at the beginning so the user can clear the selection if he wants to
        cboModel.addElement(getResourceString("NONE"));

        if (fmts.size() == 0)
        {
            return;
        }

        int selectedFieldIndex = 0;
        for (int i = 0; i < fmts.size(); ++i)
        {
            DataObjSwitchFormatter currentFormat = fmts.get(i);
            cboModel.addElement(currentFormat);
            if (tempAgg != null && 
                currentFormat.getName().equals(tempAgg.getFormatName()))
            {
                // found the selected field
                // current combo index is (i+1) because of empty entry at the beginning
                selectedFieldIndex = i + 1; 
            }
        }
        
        // set selected field
        displayCbo.setSelectedIndex(selectedFieldIndex);
    }
    
    /*
     * Populates the field value combo with fields and leaves the right one selected
     */
    protected void updateFieldValueCombo()
    {
        // clear combo box list
        DefaultComboBoxModel cboModel = (DefaultComboBoxModel) fieldOrderCbo.getModel();
        cboModel.removeAllElements();
        
        // add an empty entry at the beginning so the user can clear the selection if he wants to
        cboModel.addElement(getResourceString("NONE"));
        
        // add fields to combo box
        List<DBFieldInfo> fields             = tableInfo.getFields();
        int               selectedFieldIndex = 0;
        for (int i = 0; i < fields.size(); ++i)
        {
            DBFieldInfo currentField = fields.get(i);
            cboModel.addElement(currentField);
            if (selectedAggregator != null && 
                currentField.getName().equals(selectedAggregator.getOrderFieldName()))
            {
                // found the selected field
                // current combo index is (i+1) because of empty entry at the beginning
                selectedFieldIndex = i + 1;
            }
        }
        
        // set selected field
        fieldOrderCbo.setSelectedIndex(selectedFieldIndex);
    }

    /**
     * @param hasChanged the hasChanged to set
     */
    public void setHasChanged(boolean hasChanged)
    {
        this.hasChanged = hasChanged;
        
        if (this.hasChanged != hasChanged)
        {
            setWindowModified(true);
        }
        updateUIEnabled();
    }

    /*
     * Populates the dialog controls with data from a given formatter
     */
    protected void fillWithObjAggregator(final DataObjAggregator agg)
    {
        selectedAggregator = agg;

        titleText.setText(agg.getTitle());
        nameText.setText(agg.getName());
        sepText.setText(agg.getSeparator());
        endingText.setText(agg.getEnding());
        countSpinner.setValue(agg.getCount() != null ? agg.getCount() : 0);
        
        updateDisplayCombo();
        updateFieldValueCombo();
        updateUIEnabled();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        selectedAggregator.setTitle(titleText.getText());
        selectedAggregator.setName(nameText.getText());
        selectedAggregator.setSeparator(sepText.getText());
        selectedAggregator.setEnding(endingText.getText());
        if (isNew)
        {
            selectedAggregator.setDefault(false);
        }
        selectedAggregator.setCount((Integer)countSpinner.getValue());
        super.okButtonPressed();
    }

    public DataObjAggregator getSelectedAggregator()
    {
        return selectedAggregator;
    }

    /**
     * 
     */
    protected void vaidateForm()
    {
        validator.processFormRules();
        validator.validateForm();
        validator.processFormRules();
    }

    /**
     * 
     */
    protected void updateUIEnabled()
    {
        endingText.setEnabled( ((Integer)countSpinner.getValue()) > 0 );
        
        //isInError = vaidateForm();
        okBtn.setEnabled(hasChanged && validator.isFormValid() && !isNameInError);
    }
    
    /**
     * @return the hasChanged
     */
    public boolean hasChanged()
    {
        return hasChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.DataChangeListener#dataChanged(java.lang.String, java.awt.Component, edu.ku.brc.ui.forms.validation.DataChangeNotifier)
     */
    public void dataChanged(String name, Component comp, DataChangeNotifier dcn)
    {
        vaidateForm();
        setHasChanged(true);
    }


    //-------------------------------------------------------------------------
    protected class DataObjAggregatorDlgDocumentListener implements DocumentListener
    {
        protected JTextField source;
        protected Method     setter;
        
        public DataObjAggregatorDlgDocumentListener(JTextField source, Method setter)
        {
            this.source = source;
            this.setter = setter;
        }
        
        public void removeUpdate(DocumentEvent e)  { changed(e); }
        public void insertUpdate(DocumentEvent e)  { changed(e); }
        public void changedUpdate(DocumentEvent e) { changed(e); }
        
        protected void changed(@SuppressWarnings("unused") DocumentEvent e)
        {
            try
            {
                // equivalent to calling selectedAggregator.setter(source.getText())
                setter.invoke(selectedAggregator, source.getText());
                setHasChanged(true);
            }
            catch (IllegalAccessException iae)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjAggregatorDlg.class, iae);
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjAggregatorDlg.class, iae);
                throw new RuntimeException("Illegal Access Exception: " + iae.getMessage());
            }
            catch (InvocationTargetException ite)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjAggregatorDlg.class, ite);
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjAggregatorDlg.class, ite);
                throw new RuntimeException("Invocation Target Exception: " + ite.getMessage());
            }
        }
    }
}
