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

import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField.FieldType;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.VerticalSeparator;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 27, 2008
 *
 */
public class UIFormatterEditorDlg extends CustomDialog
{
	protected DBFieldInfo               fieldInfo      = null;
	protected UIFieldFormatterIFace     selectedFormat = null;
    
    // used to hold changes to formatters before committing them to DB
    protected DataObjFieldFormatMgr 	dataObjFieldFormatMgrCache;
    protected UIFieldFormatterMgr		uiFieldFormatterMgrCache;
    
    protected UIFieldFormatterFactory   formatFactory;
    protected UIFieldFormatterSampler 	fieldFormatterSampler; 

    protected JLabel	                sampleLabel;
    protected JTextField                nameTF;
    protected JTextField                titleTF;
    protected JCheckBox                 byYearCB;
    protected JButton                   deleteBtn;
    
    // Right-side UI
    protected JTable                    fieldsTbl;
    protected Vector<UIFieldFormatterField> fields;
    protected FieldsTableModel          fieldsModel;
    protected JButton                   orderDwnBtn;
    protected JButton                   orderUpBtn;
    protected EditDeleteAddPanel        fieldsPanel;
    protected JLabel                    fieldTypeLbl;
    protected JComboBox                 fieldTypeCbx;
    protected boolean                   fieldHasChanged             = false;
    protected UIFieldFormatterField     currentField                = null;
    
    // CardLayout for Type Panels
    protected CardLayout                cardLayout                  = new CardLayout();
    protected JPanel                    cardPanel;
    protected JTextField                fieldTxt;
    protected JSpinner                  sizeSpinner;
    protected JComboBox                 sepCbx;
    protected JCheckBox                 isIncChk;
    
    protected ListSelectionListener     fieldsTblSL                 = null;
    protected DocumentListener          formatChangedDL             = null;
    protected boolean                   hasChanged                  = false;
    protected boolean                   isInError                   = false;
    protected boolean                   isNew;
    protected String                    fmtErrMsg                   = null;
    
    /**
     * @param parentDlg
     * @param fieldInfo
     * @param selectedFormat
     * @param isNew
     * @param uiFieldFormatterMgrCache
     * @throws HeadlessException
     */
    public UIFormatterEditorDlg(final CustomDialog          parentDlg, 
                                final DBFieldInfo           fieldInfo,
                                final UIFieldFormatterIFace selectedFormat,
                                final boolean               isNew,
                                final boolean               doProcessSamples,
                                final UIFieldFormatterMgr	uiFieldFormatterMgrCache) throws HeadlessException
    {
        super(parentDlg, getResourceString("FFE_DLG_TITLE"), true, OKCANCELHELP, null); //$NON-NLS-1$
        
        this.fieldInfo                   = fieldInfo;
        this.selectedFormat              = selectedFormat;
        this.uiFieldFormatterMgrCache    = uiFieldFormatterMgrCache;
        this.isNew                       = isNew;
        this.fieldFormatterSampler       = doProcessSamples ? new UIFieldFormatterSampler(fieldInfo) : null;
        this.formatFactory               = UIFieldFormatterMgr.getFormatFactory(fieldInfo);
        this.helpContext                 = "UIF_EDITOR";
        
        this.fields                      = selectedFormat.getFields();
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        CellConstraints cc = new CellConstraints();
        
        orderUpBtn = createIconBtn("ReorderUp", "TCGD_MOVE_UP", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                int inx = fieldsTbl.getSelectedRow();
                UIFieldFormatterField item = (UIFieldFormatterField)fields.get(inx);
                
                fields.remove(inx);
                fields.insertElementAt(item, inx-1);
                fieldsTbl.getSelectionModel().setSelectionInterval(inx-1, inx-1);
                updateEnabledState();
                updateUIEnabled();
            }
        });
        orderDwnBtn = createIconBtn("ReorderDown", "TCGD_MOVE_DOWN", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                int inx = fieldsTbl.getSelectedRow();
                UIFieldFormatterField item = (UIFieldFormatterField)fields.get(inx);
               
                fields.remove(inx);
                fields.insertElementAt(item, inx+1);
                fieldsTbl.getSelectionModel().setSelectionInterval(inx+1, inx+1);
                updateEnabledState();
                updateUIEnabled();
            }
        });
        
        // get formatters for field
        List<UIFieldFormatterIFace> fmtrs = new Vector<UIFieldFormatterIFace>(
                uiFieldFormatterMgrCache.getFormatterList(fieldInfo.getTableInfo().getClassObj(), fieldInfo.getName()));
        Collections.sort(fmtrs, new Comparator<UIFieldFormatterIFace>() {
            public int compare(UIFieldFormatterIFace o1, UIFieldFormatterIFace o2)
            {
                return o1.toPattern().compareTo(o2.toPattern());
            }
        });

        // table and field titles
        PanelBuilder tblInfoPB = new PanelBuilder(new FormLayout("r:p,2px,f:p:g", "p,2px,p,2px,p,10px")/*, new FormDebugPanel()*/);

        String typeStr = fieldInfo.getType();
        typeStr = typeStr.indexOf('.') > -1 ? StringUtils.substringAfterLast(fieldInfo.getType(), ".") : typeStr;

        JLabel tableTitleLbl      = createI18NFormLabel("FFE_TABLE");
        JLabel tableTitleValueLbl = createLabel(fieldInfo.getTableInfo().getTitle());
        tableTitleValueLbl.setBackground(Color.WHITE);
        tableTitleValueLbl.setOpaque(true);

        JLabel fieldTitleLbl      = createI18NFormLabel("FFE_FIELD");
        JLabel fieldTitleValueLbl = createLabel(fieldInfo.getTitle());
        fieldTitleValueLbl.setBackground(Color.WHITE);
        fieldTitleValueLbl.setOpaque(true);

        JLabel fieldLengthLbl = createI18NFormLabel("FFE_LENGTH");
        JLabel fieldLengthValueLbl = createLabel(Integer.toString(fieldInfo.getLength()));
        fieldLengthValueLbl.setBackground(Color.WHITE);
        fieldLengthValueLbl.setOpaque(true);
        
        int y = 1;
        tblInfoPB.add(tableTitleLbl,       cc.xy(1, y));
        tblInfoPB.add(tableTitleValueLbl,  cc.xy(3, y)); y += 2;
        tblInfoPB.add(fieldTitleLbl,       cc.xy(1, y));
        tblInfoPB.add(fieldTitleValueLbl,  cc.xy(3, y)); y += 2;
        tblInfoPB.add(fieldLengthLbl,      cc.xy(1, y));
        tblInfoPB.add(fieldLengthValueLbl, cc.xy(3, y)); y += 2;

        // sample panel
        sampleLabel = createLabel("", SwingConstants.LEFT); 
        JPanel samplePanel = new JPanel();
        samplePanel.setBorder(BorderFactory.createTitledBorder(getResourceString("FFE_SAMPLE"))); //$NON-NLS-1$ 
        samplePanel.add(sampleLabel);

        // name text field
        nameTF = createTextField(20);
        
        // title text field
        titleTF = createTextField(20);
        
        byYearCB = createCheckBox(getResourceString("FFE_BY_YEAR_CHECKBOX")); //$NON-NLS-1$ 
        hookByYearCheckBoxListener();
        
        fieldsPanel  = new EditDeleteAddPanel(getSaveAL(), getDelAL(), getAddAL());
        fieldsTbl    = new JTable(fieldsModel = new FieldsTableModel());
        fieldTypeCbx = new JComboBox(FieldType.values()); // I18N
        fieldTxt     = createTextField(20);
        fieldsPanel.getAddBtn().setEnabled(true);
        fieldsPanel.getEditBtn().setIcon(IconManager.getIcon("Green Arrow Up", IconManager.IconSize.Std16));
        UIHelper.makeTableHeadersCentered(fieldsTbl, true);
        
        int width = fieldTypeCbx.getPreferredSize().width;
        
        y = 1;
        PanelBuilder subPB = new PanelBuilder(new FormLayout("r:p,2px,p", "p,4px, p,4px, p,4px, p,4px"));
        
        subPB.add(createI18NFormLabel("FFE_NAME"), cc.xy(1,y));
        subPB.add(nameTF, cc.xy(3, y)); y += 2;
        
        subPB.add(createI18NFormLabel("FFE_TITLE"), cc.xy(1,y));
        subPB.add(titleTF, cc.xy(3, y)); y += 2;
        
        subPB.add(byYearCB, cc.xy(3,y)); y += 2;
        
        // CardLayout for Editor Panels
        
        SpinnerModel retModel = new SpinnerNumberModel(1, //initial value
                1, //min
                10,   //max
                1);               //step
        sizeSpinner = new JSpinner(retModel);
        isIncChk    = new JCheckBox("Is Incrementer");
        
        String colDefs = "f:p:g,p,2px,"+width+"px";
        PanelBuilder numPB = new PanelBuilder(new FormLayout(colDefs, "p,2px,p"));
        numPB.add(createI18NFormLabel("FFE_LENGTH"), cc.xy(2, 1));
        numPB.add(sizeSpinner, cc.xy(4, 1));
        numPB.add(isIncChk,    cc.xy(4, 3));
        
        sepCbx = new JComboBox(new String[] {"-", ".", "/", "` `", "_"});
        PanelBuilder sepPB = new PanelBuilder(new FormLayout(colDefs, "p"));
        sepPB.add(createI18NFormLabel("FFE_SEP"), cc.xy(2, 1));
        sepPB.add(sepCbx, cc.xy(4, 1));

        PanelBuilder txtPB = new PanelBuilder(new FormLayout(colDefs, "p"));
        txtPB.add(createI18NFormLabel("FFE_TEXT"), cc.xy(2, 1));
        txtPB.add(fieldTxt, cc.xy(4, 1));

        cardPanel = new JPanel(cardLayout);
        cardPanel.add("size", numPB.getPanel());
        cardPanel.add("text", txtPB.getPanel());
        cardPanel.add("sep",  sepPB.getPanel());
        cardPanel.add("none", new JLabel(" "));
        
        // formatting key panel
        JPanel keyPanel = new JPanel();
        keyPanel.setLayout(new BoxLayout(keyPanel, BoxLayout.Y_AXIS));
        keyPanel.setBorder(BorderFactory.createTitledBorder(getResourceString("FFE_HELP"))); //$NON-NLS-1$
        // left help body text in a single string resource until we build infrastructure to 
        // localize long texts (in files maybe)
        keyPanel.add(createLabel(formatFactory.getHelpHtml()));
        
        y = 1;
        PanelBuilder leftPB = new PanelBuilder(new FormLayout("f:p:g", "t:p,10px,p,f:p:g"));
        leftPB.add(tblInfoPB.getPanel(), cc.xy(1, y));     y += 2;
        leftPB.add(subPB.getPanel(),     cc.xy(1, y));     y += 2;

        PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
        upDownPanel.add(orderUpBtn,       cc.xy(1, 2));
        upDownPanel.add(orderDwnBtn,      cc.xy(1, 4));

        y = 1;
        PanelBuilder rightPB = new PanelBuilder(new FormLayout("p:g,2px,p,2px,p",  "200px,2px,p,2px,p,2px,p"));
        rightPB.add(createScrollPane(fieldsTbl),     cc.xywh(1, y, 3, 1)); 
        rightPB.add(upDownPanel.getPanel(),          cc.xywh(5, y, 1, 1)); y += 2;    
        rightPB.add(fieldsPanel,                     cc.xywh(1, y, 3, 1)); y += 2;
        rightPB.add(fieldTypeLbl = createI18NFormLabel("FFE_TYPE"), cc.xy(1,y));
        rightPB.add(fieldTypeCbx,                    cc.xy(3, y));         y += 2;
        rightPB.add(cardPanel,                       cc.xyw(1, y, 3));     y += 2;
        
        y = 1;
        PanelBuilder pb = new PanelBuilder(new FormLayout("p:g,10px,p,10px,p:g,10px,p",  "f:p:g,10px,p"));
        Color bg = getBackground();
        pb.add(new VerticalSeparator(bg.darker(), bg.brighter()), cc.xywh(3, 1, 1, 1));
        
        pb.add(leftPB.getPanel(),    cc.xy(1, y));     
        pb.add(rightPB.getPanel(),   cc.xy(5, y));     
        //pb.add(keyPanel,             cc.xy(7, y));     
        y += 2;
        pb.add(samplePanel,          cc.xyw(1, y, 7)); y += 2;  
        
        setByYearSelected(selectedFormat);
        
        nameTF.setEditable(isNew);
        nameTF.setText(selectedFormat.getTitle());
        titleTF.setText(selectedFormat.getName());
        updateSample(); 
        
        hookTextChangeListener(nameTF,  "FFE_NO_NAME");
        hookTextChangeListener(titleTF, "FFE_NO_TITLE");
        
        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        updateUIEnabled();
        
        pack();
        
        enabledEditorUI(false);

        hookFieldsTblSelectionListener();
        
        fieldTypeCbx.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                typeChanged();
            }
        });
        
        fieldTxt.getDocument().addDocumentListener(new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e)
            {
                fieldHasChanged = true;
                updateEnabledState();
                hasChanged      = true;
                updateUIEnabled();

            }
        });
        
        sizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                fieldHasChanged = true;
                updateEnabledState();
                hasChanged      = true;
                updateUIEnabled();

            }
        });
        
        isIncChk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateEntry();
                fieldsTbl.repaint();
            }
        });
        
        fieldTypeCbx.setSelectedIndex(-1);
        fieldHasChanged = false;
        updateEnabledState();
    }
    
    /**
     * 
     */
    private void typeChanged()
    {
        fieldHasChanged = true;
        updateEnabledState();
        hasChanged      = true;
        updateUIEnabled();
        
        String cardKey = "none";
        if (fieldTypeCbx.getSelectedIndex() > -1)
        {
            isIncChk.setVisible(false);
            
            FieldType fieldType = (FieldType)fieldTypeCbx.getSelectedItem();
            switch (fieldType)
            {
                case alphanumeric : 
                case alpha : 
                case anychar : 
                    cardKey = "size";
                    break;
                    
                case constant :
                    cardKey = "text";
                    break;
                    
                case numeric :
                    cardKey = "size";
                    isIncChk.setVisible(true);
                    break;
                    
                case separator : 
                    cardKey = "sep";
                    break;
                    
                case year :
                    cardKey = "none";
                    break;
            }
        }
        cardLayout.show(cardPanel, cardKey);
    }
    
    /**
     * 
     */
    private void unhookFieldsTblSelectionListener() 
    {
        fieldsTbl.getSelectionModel().removeListSelectionListener(fieldsTblSL);
    }

    /**
     * 
     */
    private void hookFieldsTblSelectionListener() 
    {
        if (fieldsTblSL == null) 
        {
            fieldsTblSL = new ListSelectionListener() 
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        if (fieldHasChanged)
                        {
                            Object[] options = { getResourceString("SAVE"), //$NON-NLS-1$ 
                                                 getResourceString("DISCARD") }; //$NON-NLS-1$
                            int retVal = JOptionPane.showOptionDialog(null, getResourceString("FFE_SAVE_CHG"), getResourceString("SaveChangesTitle"), JOptionPane.YES_NO_CANCEL_OPTION, //$NON-NLS-1$ //$NON-NLS-2$
                                                                      JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                            if (retVal == JOptionPane.YES_OPTION)
                            {
                                fieldsPanel.getEditBtn().doClick();
                            }
                        }
                        
                        int inx = fieldsTbl.getSelectedRow();
                        if (inx > -1)
                        {
                            currentField = fields.get(inx);
                            
                            fieldTypeCbx.setSelectedIndex(currentField.getType().ordinal());
                            isIncChk.setSelected(currentField.isIncrementer());
                            fieldTxt.setText(currentField.getValue());
                            sizeSpinner.setValue(Math.max(1, currentField.getSize()));
                            enabledEditorUI(true);
                            
                        } else
                        {
                            fieldTypeCbx.setSelectedIndex(-1);
                            enabledEditorUI(false);
                        }
                        fieldHasChanged = false;
                        updateEnabledState();
                    }
                }
            };
        }
        
        fieldsTbl.getSelectionModel().addListSelectionListener(fieldsTblSL);
    }


    /**
     * @param enable
     */
    protected void enabledEditorUI(final boolean enable)
    {
        fieldTypeCbx.setEnabled(enable);
        fieldTypeLbl.setEnabled(enable);
        fieldTxt.setEnabled(enable);
        sizeSpinner.setEnabled(enable);
    }
    
    /**
     * @param size
     * @param ch
     * @return
     */
    protected String getValueStr(final int size, final char ch)
    {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<size;i++)
        {
            sb.append(ch);
        }
        return sb.toString();
    }
    
    /**
     * Save the Field values.
     * @return action listener
     */
    protected ActionListener getSaveAL()
    {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                saveEntry();
            }
        };
    }
    
    /**
     * 
     */
    protected void updateEntry()
    {
        if (!fields.contains(currentField))
        {
            fields.add(currentField);
        }
        
        FieldType fieldType = (FieldType)fieldTypeCbx.getSelectedItem();
        currentField.setType(fieldType);
        
        int     size     = (Integer)sizeSpinner.getValue();
        boolean isByYear = fieldType == FieldType.year;
        boolean isIncr   = isIncChk.isSelected();
        
        switch (fieldType)
        {
            case alphanumeric :
                currentField.setSize(size);
                currentField.setValue(getValueStr(size, 'A'));
                break;
                
            case alpha : 
                currentField.setSize(size);
                currentField.setValue(getValueStr(size, 'a'));
                break;
                
            case anychar : 
                currentField.setSize(size);
                currentField.setValue(getValueStr(size, '#'));
                break;
                
            case numeric :
                currentField.setSize(size);
                currentField.setValue(getValueStr(size, isIncr ? '#' : 'N'));
                break;
                
            case constant :
                currentField.setSize(fieldTxt.getText().length());
                currentField.setValue(fieldTxt.getText());
                break;

                
            case separator :
                currentField.setSize(1);
                String sepStr = (String)sepCbx.getSelectedItem();
                if (sepStr.startsWith("`"))
                {
                    sepStr = " ";
                }
                currentField.setValue(String.valueOf(sepStr.charAt(0)));
                break;
                
            case year :
                currentField.setValue("YEAR");
                currentField.setSize(4);
                break;
        }
       
        currentField.setByYear(isByYear);
        currentField.setIncrementer(isIncr);
    }
    
    /**
     * 
     */
    protected void saveEntry()
    {
        updateEntry();
        
        fieldTypeCbx.setSelectedIndex(-1);
        fieldTxt.setText("");
        sizeSpinner.setValue(1);
        isIncChk.setSelected(false);
        
        fieldHasChanged = false;
        
        enabledEditorUI(false);
        updateEnabledState();
        fieldsModel.fireChange();
        updateUIEnabled();
    }
    
    /**
     * @return
     */
    protected ActionListener getDelAL()
    {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fieldTypeCbx.setSelectedIndex(-1);
                fieldTxt.setText(""); // DL attached to this field will set fieldHasChanged to true 
                fields.remove(fieldsTbl.getSelectedRow());
                // we must unhook the fields table SL to avoid popping up a dialog asking to save or discard data  
                unhookFieldsTblSelectionListener();
                fieldHasChanged = false;
                fieldsTbl.clearSelection();
                fieldsModel.fireChange();
                enabledEditorUI(false);
                updateUIEnabled();
                hookFieldsTblSelectionListener();
                fieldsPanel.getEditBtn().setEnabled(false);
            }
        };
    }
    
    /**
     * 
     */
    protected void setDataIntoUI()
    {
        if (currentField != null)
        {
            fieldTypeCbx.setSelectedIndex(currentField.getType().ordinal());
            isIncChk.setSelected(currentField.isByYear());
            fieldTxt.setText(currentField.getValue());
            sizeSpinner.setValue(Math.max(1, currentField.getSize()));
        }
    }
    
    /**
     * @return
     */
    protected ActionListener getAddAL()
    {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                currentField = new UIFieldFormatterField();
                setDataIntoUI();
                enabledEditorUI(true);
                fieldHasChanged = false;
                updateUIEnabled();
            }
        };
    }
    
    /**
     * 
     */
    protected void updateEnabledState()
    {
        int inx = fieldsTbl.getSelectedRow();
        boolean isSelected = inx > -1;
        fieldsPanel.getDelBtn().setEnabled(isSelected);
        
        // save Btn
        if (currentField != null)
        {
            fieldsPanel.getEditBtn().setEnabled(fieldHasChanged && (currentField.getType() == FieldType.constant ? !fieldTxt.getText().isEmpty() : true));
        }
        
        orderUpBtn.setEnabled(inx > 0);
        orderDwnBtn.setEnabled(inx > -1 && inx < fields.size()-1);
    }
    
    /**
     * 
     */
    protected void getDataFromUI()
    {
        selectedFormat.setTitle(titleTF.getText());
        selectedFormat.setName(nameTF.getText());
        selectedFormat.setByYear(byYearCB.isSelected());
        selectedFormat.setDefault(false);
        selectedFormat.setDataClass(fieldInfo.getTableInfo().getClassObj());
        selectedFormat.setFieldName(fieldInfo != null ? fieldInfo.getName() : null);
        
        selectedFormat.setIncrementer(false);
        for (UIFieldFormatterField f : fields)
        {
            if (f.isIncrementer())
            {
                selectedFormat.setIncrementer(true);
                break;
            }
        }
        
        if (fields.size() == 1 && fields.get(0).getType() == FieldType.numeric)
        {
            selectedFormat.setType(UIFieldFormatterIFace.FormatterType.numeric);
        } else
        {
            selectedFormat.setType(UIFieldFormatterIFace.FormatterType.generic);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        if (fieldsPanel.getEditBtn().isEnabled())
        {
            int userChoice = JOptionPane.NO_OPTION;
            Object[] options = { getResourceString("Continue"),  //$NON-NLS-1$
                                 getResourceString("CANCEL")  //$NON-NLS-1$
                  };
            loadAndPushResourceBundle("masterusrpwd");

            userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                         getResourceString("UIFEDlg.ITEM_CHG"),  //$NON-NLS-1$
                                                         getResourceString("UIFEDlg.CHG_TITLE"),  //$NON-NLS-1$
                                                         JOptionPane.YES_NO_OPTION,
                                                         JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (userChoice == JOptionPane.NO_OPTION)
            {
                return;
            }
        }
        super.okButtonPressed();
        getDataFromUI();
    }


    /**
     * 
     */
    private void updateSample() 
    {
        sampleLabel.setText(selectedFormat != null ? selectedFormat.getSample() : " "); 
    	resetError();
    }
    
    /**
     * 
     */
    private void resetError() 
    {
        isInError = false;
        sampleLabel.setForeground(Color.black);
    	fmtErrMsg = null;
    }

    /**
     * @param message
     */
    private void setError(final String message, final boolean doClearFmt) 
    {
        selectedFormat = doClearFmt ? null : selectedFormat;
        isInError      = true;
        sampleLabel.setForeground(Color.red);
        sampleLabel.setText(message);
    }

    /**
     * 
     */
    private void hookByYearCheckBoxListener()
    {
    	ItemListener il = new ItemListener()
    	{
    		public void itemStateChanged(ItemEvent e)
    		{
    			if (e.getItem() == byYearCB)
    			{
    				// do nothing if format is new because it will be destroyed when the next key is pressed
    				// the correct byYear state of newly created formatters is corrected when the ok button is pressed
    				if (selectedFormat != null)
    				{
    					selectedFormat.setByYear(e.getStateChange() == ItemEvent.SELECTED);
    				}
    			}
    		}
    	};
    	byYearCB.addItemListener(il);
    }
    
    /**
     * @param txtFld
     */
    private void hookTextChangeListener(final JTextField txtFld, final String errMsgKey)
    {
        txtFld.getDocument().addDocumentListener(new DocumentAdaptor()
        {
            @Override
            protected void changed(DocumentEvent e)
            {
                if (StringUtils.isEmpty(txtFld.getText()))
                {
                    setError(getResourceString(errMsgKey), false); 
                    
                } else if (selectedFormat == null)
                {
                    setError(fmtErrMsg, true); 
                    
                } else if (isInError)
                {
                    updateUIEnabled(); 
                    
                } else
                {
                    updateSample();
                }
                hasChanged = true;
            }
        });
    }

	/**
	 * 
	 */
	private void setByYearSelected(Object obj)
	{
		if (obj instanceof UIFieldFormatterIFace)
		{
			UIFieldFormatterIFace fmt = (UIFieldFormatterIFace) obj;
			byYearCB.setSelected(fmt.getByYear());
		}
	}
	
    /**
     * 
     */
    protected void updateUIEnabled()
    {
        // If we have a field formatter sampler, then we can check if current format 
        // invalidates an existing value in database.
        if (fieldFormatterSampler != null && selectedFormat != null) 
        {
            try 
            {
                fieldFormatterSampler.isValid(selectedFormat);
                resetError();
            }
            catch (UIFieldFormatterInvalidatesExistingValueException e)
            {
                setError(String.format(getResourceString("FFE_FORMAT_INVALIDATES_FIELD_VALUE"), //$NON-NLS-1$ 
                        selectedFormat.getSample(), e.getInvalidatedValue().toString()), false);
            }
        }
        
    	// enable ok button only if currently selected format is valid 
        // by year checkbox is enabled if there's one YEAR and one auto-number (###) in the format
        boolean byYearEnabled = (selectedFormat != null) && (selectedFormat.byYearApplies());
        byYearCB.setEnabled(byYearEnabled);
        
        okBtn.setEnabled(hasChanged && 
                         !isInError && 
                         nameTF.getText().length() > 0 && 
                         titleTF.getText().length() > 0 &&
                         fields.size() > 0);
        
        // create a sample and display it, if there's no error
        // otherwise, leave the sample panel area with the error message, set in setError() method.  
        if (!isInError)
        {
            StringBuilder pattern = new StringBuilder();
            for (UIFieldFormatterField ff : fields)
            {
                pattern.append(ff.getSample());
            }
            sampleLabel.setText(pattern.toString());
        }
    }


    /**
     * @return the selectedFormat
     */
    public UIFieldFormatterIFace getSelectedFormat()
    {
        return selectedFormat;
    }
    
    //-------------------------------------------------
    class FieldsTableModel extends AbstractTableModel
    {
        protected String[] colHeaders = {"Type", "Value", "Size", "Is By Year", "Is Incrementer"};
        
        /**
         * 
         */
        public FieldsTableModel()
        {
            super();
        }
        
        public void fireChange()
        {
            super.fireTableDataChanged();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return super.getColumnClass(columnIndex);
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return colHeaders[column];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return colHeaders.length;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return selectedFormat.getFields().size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            UIFieldFormatterField fld = selectedFormat.getFields().get(rowIndex);
            switch (columnIndex)
            {
                case 0 : return fld.getType();
                case 1 : return fld.getValue();
                case 2 : return fld.getType() == FieldType.separator ? "" : fld.getSize();
                case 3 : return fld.getType() == FieldType.separator ? "" : UIRegistry.getResourceString(fld.isByYear() ? "YES" : "NO");
                case 4 : return fld.getType() == FieldType.separator ? "" : UIRegistry.getResourceString(fld.isIncrementer() ? "YES" : "NO");
            }
            return null;
        }
    }
}
