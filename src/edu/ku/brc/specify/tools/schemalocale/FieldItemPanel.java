/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tools.schemalocale;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.PickListIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 25, 2007
 *
 */
public class FieldItemPanel extends LocalizerBasePanel
{
    private static final Logger log = Logger.getLogger(FieldItemPanel.class);
            
    protected LocalizableIOIFace        localizableIO = null;
    
    protected SchemaLocalizerPanel      schemaPanel;
    
    protected LocalizableJListItem      currJListItem   = null;
    protected LocalizableContainerIFace currContainer   = null;
    protected boolean                   includeHiddenUI;              // Must be set before creatng the panel
    protected boolean                   isDBSchema;
    protected boolean                   includeFormatAndAutoNumUI;
    protected DBTableInfo               tableInfo       = null;
    protected DBFieldInfo               fieldInfo       = null;
    protected DBRelationshipInfo        relInfo         = null;
    
    // LocalizableItemIFace Fields
    protected JList            fieldsList;
    protected JTextArea        fieldDescText = new JTextArea();
    protected JTextField       fieldNameText = new JTextField();
    protected JLabel           fieldDescLbl;
    protected JLabel           fieldNameLbl;
    protected JLabel           fieldTypeLbl;
    protected JLabel           fieldLengthLbl;
    protected JLabel           fieldTypeTxt;
    protected JLabel           fieldLengthTxt;
    
    // Formatting
    protected JLabel           formatLbl     = null;
    protected JComboBox        formatCombo;
    protected JTextField       formatTxt;
    protected Hashtable<String, UIFieldFormatterIFace> formatHash = new Hashtable<String, UIFieldFormatterIFace>();
    
    // PickList
    protected JLabel           pickListLbl;
    protected JComboBox        pickListCBX;

    protected JLabel           autoNumLbl;
    protected JComboBox        autoNumberCombo;
    
    protected DefaultListModel fieldsModel   = new DefaultListModel();
    protected JButton          nxtBtn;
    protected JButton          nxtEmptyBtn;
    protected JCheckBox        fieldHideChk  = new JCheckBox(getResourceString("SL_FIELD_HIDE_CHK"));
    protected boolean          hasFieldInfoChanged  = false;

    protected LocalizableItemIFace prevField = null;
    
    protected JStatusBar       statusBar      = null;
    protected JButton          fldSpellChkBtn = null;
    
    protected PropertyChangeListener pcl   = null;
    protected String           noneStr = UIRegistry.getResourceString("None");
    
    protected List<PickList>   pickLists;
    
    /**
     * 
     */
    public FieldItemPanel(final SchemaLocalizerPanel schemaPanel,
                          final boolean              includeHiddenUI, 
                          final boolean              includeFormatAndAutoNumUI, 
                          final boolean              isDBSchema,
                          final PropertyChangeListener pcl)
    {
        this.schemaPanel     = schemaPanel;
        this.includeHiddenUI = includeHiddenUI;
        this.includeFormatAndAutoNumUI = includeFormatAndAutoNumUI;
        this.isDBSchema      = isDBSchema;
        this.pcl             = pcl;
        
        init();
        
        buildUI();
    }

    /**
     * 
     */
    public void buildUI()
    {
        setIgnoreChanges(true);
        
        fieldsList = new JList(fieldsModel);
        
        fieldsList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    getAllDataFromUI();
                    fieldSelected();
                }
            }
        });
        
        fieldsList.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e)
            {
                super.focusLost(e);
                //lastIndex = fieldsList.getSelectedIndex();
            }
        });
        
        fieldDescText.setRows(5);
        fieldDescText.setLineWrap(true);
        fieldDescText.addKeyListener(new LengthWatcher(255));
        fieldNameText.addKeyListener(new LengthWatcher(64));

        CellConstraints cc = new CellConstraints();
        
        String descStr  = getResourceString("SL_DESC") + ":";
        String labelStr = getResourceString("SL_LABEL") + ":";

        int y = 1;
        
        JScrollPane fldsp = new JScrollPane(fieldsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        // LocalizableNameDescIFace
        PanelBuilder pb = new PanelBuilder(new FormLayout("max(200px;p),4px,p,2px,p,f:p:g", 
                                                             (includeHiddenUI ? "p,2px," : "") + 
                                                             (isDBSchema ? "p,2px,p,2px," : "") + 
                                                             (includeFormatAndAutoNumUI ? "p,2px," : "") + 
                                                             "p,2px,p,2px,p,2px,p,2px,p,2px,p,2px,f:p:g"), this);
        
        pb.add(fldsp, cc.xywh(1, y, 1, 7+(isDBSchema ? 4 : 0)));
        pb.add(fieldNameLbl = new JLabel(labelStr, SwingConstants.RIGHT), cc.xy(3, y));
        pb.add(fieldNameText, cc.xywh(5, y, 2, 1));   y += 2;
        
        if (includeHiddenUI)
        {
            pb.add(fieldHideChk, cc.xy(5, y)); y += 2;
        }
       
        pb.add(fieldDescLbl = new JLabel(descStr, SwingConstants.RIGHT), cc.xy(3, y));
        JScrollPane sp = new JScrollPane(fieldDescText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pb.add(sp,   cc.xywh(5, y, 2, 1));   y += 2;
        fieldDescText.setLineWrap(true);
        fieldDescText.setRows(4);
        fieldDescText.setWrapStyleWord(true);
        
        if (isDBSchema)
        {
            fieldTypeTxt   = new JLabel("");
            fieldLengthTxt = new JLabel("");
            
            pb.add(fieldTypeLbl   = new JLabel(getResourceString("SL_TYPE") + ":", SwingConstants.RIGHT), cc.xy(3, y));
            pb.add(fieldTypeTxt,   cc.xy(5, y));   y += 2;
            pb.add(fieldLengthLbl = new JLabel(getResourceString("SL_LENGTH") + ":", SwingConstants.RIGHT), cc.xy(3, y));
            pb.add(fieldLengthTxt, cc.xy(5, y));   y += 2;
            
            fieldTypeTxt.setBackground(Color.WHITE);
            fieldLengthTxt.setBackground(Color.WHITE);
            fieldTypeTxt.setOpaque(true);
            fieldLengthTxt.setOpaque(true);
        }
        
        if (includeFormatAndAutoNumUI)
        {
            PanelBuilder inner = new PanelBuilder(new FormLayout("max(p;150px),2px,f:p:g", "p"));
            
            formatCombo = new JComboBox(new DefaultComboBoxModel());
            formatTxt   = new JTextField(15);
            
            pb.add(formatLbl = new JLabel(getResourceString("SL_FORMAT") + ":", SwingConstants.RIGHT), cc.xy(3, y));
            
            inner.add(formatCombo,   cc.xy(1, 1));   
            inner.add(formatTxt,     cc.xy(3, 1));
            pb.add(inner.getPanel(), cc.xywh(5, y, 2, 1));   y += 2;
            
            //String[] items = {noneStr, getResourceString("Generic"), getResourceString("External")};
            
            //autoNumberCombo = new JComboBox(items);
            //pb.add(autoNumLbl = new JLabel(getResourceString("SL_AUTONUM") + ":", SwingConstants.RIGHT), cc.xy(3, y));
            //pb.add(autoNumberCombo, cc.xy(5, y));   y += 2;
            
            ActionListener changed = new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    setHasChanged(true);
                    schemaPanel.setHasChanged(true);
                    hasFieldInfoChanged = true;
                    String  fmtName = (String)formatCombo.getSelectedItem();
                    if (StringUtils.isNotEmpty(fmtName)) // should never be empty
                    {
                        formatTxt.setEnabled(fmtName.equals(noneStr));
                    }
                }
            };
            formatCombo.addActionListener(changed);
            //autoNumberCombo.addActionListener(changed);
        }
        
        pickListCBX = new JComboBox(new DefaultComboBoxModel());
        pickListCBX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                setHasChanged(true);
                schemaPanel.setHasChanged(true);
                hasFieldInfoChanged = true;
            }
        });
        
        pb.add(pickListLbl   = new JLabel(getResourceString("SL_PICKLIST") + ":", SwingConstants.RIGHT), cc.xy(3, y));
        pb.add(pickListCBX,   cc.xy(5, y));   y += 2;

        
        nxtBtn         = new JButton(getResourceString("SL_NEXT"));
        nxtEmptyBtn    = new JButton(getResourceString("SL_NEXT_EMPTY"));
        fldSpellChkBtn = new JButton(getResourceString("SL_SPELL_CHECK"));
        
        JPanel bbp = ButtonBarFactory.buildCenteredBar(new JButton[] {nxtEmptyBtn, nxtBtn, fldSpellChkBtn});
        bbp.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        pb.add(bbp,   cc.xywh(3, y, 4, 1));
        
        nxtBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                next();
            }
        });
        nxtEmptyBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                nextEmpty();
            }
        });
        
        fldSpellChkBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (checker != null)
                {
                    checker.spellCheck(fieldDescText);
                    checker.spellCheck(fieldNameText);
                }
            }
            
        });
        
        fieldHideChk.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                setHasChanged(true);
                schemaPanel.setHasChanged(true);
                hasFieldInfoChanged = true;
            }
            
        });
        
        DocumentListener dl = new DocumentListener() {
            protected void changed()
            {
                if (!hasFieldInfoChanged)
                {
                    hasFieldInfoChanged = true;
                    setHasChanged(true);
                    schemaPanel.setHasChanged(true);
                }
            }
            public void changedUpdate(DocumentEvent e)
            {
                changed();
            }
            public void insertUpdate(DocumentEvent e)
            {
                changed();
            }
            public void removeUpdate(DocumentEvent e)
            {
                changed();
            }
        };
        
        fieldNameText.getDocument().addDocumentListener(dl);
        fieldDescText.getDocument().addDocumentListener(dl);
        
        if (formatTxt != null)
        {
            formatTxt.getDocument().addDocumentListener(dl);
        }
        
        SchemaI18NService.getInstance().checkCurrentLocaleMenu();
        
        enableUIControls(false);
        
        setIgnoreChanges(false);
    }
    
    /**
     * 
     */
    protected void formTextChanged()
    {
        setHasChanged(true);
        schemaPanel.setHasChanged(true);
    }
    
    /**
     * Fills the format combobox with the available formatters.
     */
    protected void fillFormatBox()
    {
        if (formatCombo != null)
        {
            ((DefaultComboBoxModel)formatCombo.getModel()).removeAllElements();
            formatCombo.setEnabled(false);
            formatTxt.setEnabled(false);
            formatTxt.setText("");
            
            if (fieldInfo != null)
            {
                if (currContainer instanceof SpLocaleContainer)
                {
                    SpLocaleContainer localeContainer = (SpLocaleContainer)currContainer;
                    
                    if (fieldInfo.getDataClass() == Date.class || 
                        fieldInfo.getDataClass() == Calendar.class)
                    {
                        fillWithDate(localeContainer);
                        
                    } else 
                    {
                        fillWithFieldFormatter();
                    }
                }
            }
        }
    }
    
    /**
     * @param localeContainer
     */
    protected void fillWithDate(final SpLocaleContainer localeContainer)
    {
        formatHash.clear();
        DefaultComboBoxModel cbxModel = (DefaultComboBoxModel)formatCombo.getModel();
        cbxModel.removeAllElements();
        
        DBFieldInfo                 precision = tableInfo.getFieldByName(fieldInfo.getName()+"Precision");
        if (precision != null)
        {
            cbxModel.addElement("Partial Date"); // I18N
        } else
        {
            cbxModel.addElement("Date");
        }
        formatCombo.setSelectedIndex(0);
        formatCombo.setEnabled(false);
        formatTxt.setEnabled(false);
    }
    
    /**
     * @return
     */
    protected UIFieldFormatterIFace fillWithFieldFormatter()
    {
        formatHash.clear();
        DefaultComboBoxModel cbxModel = (DefaultComboBoxModel)formatCombo.getModel();
        cbxModel.removeAllElements();
        
        cbxModel.addElement(noneStr); // Add None
        
        /*if (UIHelper.isClassNumeric(fieldInfo.getDataClass()))
        {
            formatCombo.setEnabled(false);
            formatTxt.setEnabled(false);
            
        } */
        
        boolean              isUIFormatter = false;
        String               formatName    = null;
        LocalizableItemIFace fld           = getSelectedFieldItem();
        if (fld != null)
        {
            formatName    = fld.getFormat();
            isUIFormatter = fld.getIsUIFormatter() == null ? false : fld.getIsUIFormatter();
            
        } else
        {
            return null; // Why did this happen?
        }
        
        int selectedInx = 0; // default to 'None'
        
        UIFieldFormatterIFace       selectedFmt = null;
        List<UIFieldFormatterIFace> fList       = UIFieldFormatterMgr.getFormatterList(tableInfo.getClassObj(), fieldInfo.getName());
        if (fList != null && fList.size() > 0)
        {
            for (UIFieldFormatterIFace fmt : fList)
            {
                log.debug(fmt.getTitle());
                
                cbxModel.addElement(fmt.toString());
                formatHash.put(fmt.toString(), fmt);
                
                if (isUIFormatter && formatName != null && formatName.equals(fmt.toString()))
                {
                    selectedInx = formatHash.size();
                    selectedFmt = fmt;
                }
            }
        }
        
        formatCombo.setSelectedIndex(selectedInx);
        formatCombo.setEnabled(formatHash.size() > 1);
        formatTxt.setEnabled(selectedInx == 0);
        
        formatTxt.setText(selectedInx != 0 ? "" : formatName);
        
        return selectedFmt;
    }
    
    /**
     * @param container
     * @param jListContainerItem
     */
    public void setContainer(final LocalizableContainerIFace container,
                             final LocalizableJListItem      jListContainerItem)
    {
        currContainer = container;
        currJListItem = jListContainerItem;
        
        tableInfo = DBTableIdMgr.getInstance().getInfoByTableName(currContainer.getName());
        
        fillFieldList();
    }
    
    
    /**
     * @param includeHiddenUI tells it to include the checkboxes for hiding tables and fields
     */
    public void setIncludeHiddenUI(final boolean includeHiddenUI)
    {
        this.includeHiddenUI = includeHiddenUI;
    }

    /**
     * @return the localizableIO
     */
    public LocalizableIOIFace getLocalizableIO()
    {
        return localizableIO;
    }

    /**
     * @param localizableIO the localizableIO to set
     */
    public void setLocalizableIO(final LocalizableIOIFace localizableIO)
    {
        this.localizableIO = localizableIO;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        fieldsList.setEnabled(enabled);
    }
    
    /**
     * @return
     */
    public int getSelectedIndex()
    {
        return fieldsList.getSelectedIndex();
    }
    
    /**
     * @param index
     */
    public void setSelectedIndex(final int index)
    {
        fieldsList.setSelectedIndex(index);
    }
    
    /**
     * Enable the controls.
     * @param enable true/false
     */
    protected void enableUIControls(final boolean enable)
    {
        fieldDescText.setEnabled(enable);
        fieldNameText.setEnabled(enable);
        
        fieldNameLbl.setEnabled(enable);
        fieldDescLbl.setEnabled(enable);
        
        fieldHideChk.setEnabled(enable);
        
        fieldTypeLbl.setEnabled(enable);
        fieldTypeTxt.setEnabled(enable);
        
        fieldLengthLbl.setEnabled(enable);
        fieldLengthTxt.setEnabled(enable);
        
        pickListLbl.setEnabled(enable);
        pickListCBX.setEnabled(enable);
        
        if (formatLbl != null)
        {
            formatLbl.setEnabled(enable);
            formatCombo.setEnabled(enable);
            formatTxt.setEnabled(enable);
            
            //autoNumLbl.setEnabled(enable);
            //autoNumberCombo.setEnabled(enable);
        }
        
        if (!enable)
        {
            fldSpellChkBtn.setEnabled(false);
            nxtBtn.setEnabled(false);
            nxtEmptyBtn.setEnabled(false);
            
        } else
        {
            updateBtns();
            checkForMoreEmpties();
            enableSpellCheck();
        }
    }
    
    /**
     * @param statusBar the statusBar to set
     */
    public void setStatusBar(JStatusBar statusBar)
    {
        this.statusBar = statusBar;
    }
    
    /**
     * @param tableName
     * @return
     */
    public LocalizableStrIFace getItemDesc(final LocalizableJListItem tableListItem, 
                                           final LocalizableJListItem fieldListItem)
    {
        LocalizableContainerIFace table = localizableIO.getContainer(tableListItem);
        if (table != null)
        {
            for (LocalizableItemIFace f : table.getContainerItems())
            {
                if (f.getName().equals(fieldListItem.getName()))
                {
                    return getDescForCurrLocale(f);
                }
            }
           
        }
        log.error("Couldn't find table ["+tableListItem.getName()+"]");
        return null;
    }
    
    /**
     * @param tableName
     * @return
     */
    public LocalizableStrIFace getItemNameDesc(final LocalizableJListItem tableListItem, 
                                               final LocalizableJListItem fieldListItem)
    {
        LocalizableContainerIFace table = localizableIO.getContainer(tableListItem);
        if (table != null)
        {
            for (LocalizableItemIFace f : table.getContainerItems())
            {
                if (f.getName().equals(fieldListItem.getName()))
                {
                    return getNameDescForCurrLocale(f);
                }
            }
        }
        log.error("Couldn't find table ["+tableListItem.getName()+"]");
        return null;
    }
    
    /**
     * 
     */
    protected void updateBtns()
    {
        int     inx     = fieldsList.getSelectedIndex();
        boolean enabled = inx < fieldsModel.size() -1;
        nxtBtn.setEnabled(enabled);
        checkForMoreEmpties();
    }
    
    /**
     * 
     */
    protected void checkForMoreEmpties()
    {
        int inx = getNextEmptyIndex(fieldsList.getSelectedIndex());
        nxtEmptyBtn.setEnabled(inx != -1);
    }
    
    /**
     * @param inx
     * @return
     */
    protected int getNextEmptyIndex(final int inx)
    {
        if (inx > -1 && inx < fieldsModel.size())
        {
            for (int i=inx;i<fieldsModel.size();i++)
            {
                LocalizableItemIFace f = getFieldItem(i);
                if (f != null)
                {
                    f = localizableIO.realize(f);
                    LocalizableStrIFace  desc = getDescForCurrLocale(f);
                    if (desc != null && StringUtils.isEmpty(desc.getText()))
                    {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    
    /**
     * 
     */
    protected void getAllDataFromUI()
    {
        getItemDataFromUI();
    }
    
    /**
     * 
     */
    protected void getItemDataFromUI()
    {
        if (prevField != null && hasFieldInfoChanged)
        {
            prevField.setIsHidden(fieldHideChk.isSelected());
            boolean nameChanged = setNameDescStrForCurrLocale(prevField, fieldNameText.getText());
            boolean descChanged = setDescStrForCurrLocale(prevField,     fieldDescText.getText());
            if (nameChanged || descChanged)
            {
                setHasChanged(true);
                schemaPanel.setHasChanged(true);
            }
            
            PickList pl = (PickList)pickListCBX.getSelectedItem();
            prevField.setPickListName(pl != null ? pl.getName() : null);
            
            if (formatCombo != null)
            {
                String  fmtName = (String)formatCombo.getSelectedItem();
                if (StringUtils.isNotEmpty(fmtName)) // should never be empty
                {
                    boolean isNone = fmtName.equals(noneStr);
                    if (!isNone)
                    {
                        prevField.setFormat(formatHash.get(fmtName).getName());
                        prevField.setIsUIFormatter(true);
                    
                    } else
                    {
                        prevField.setIsUIFormatter(false);
                        
                        fmtName = formatTxt.getText();
                        if (StringUtils.isNotEmpty(fmtName)) 
                        {
                            prevField.setFormat(fmtName);     // this should a format string like %s or %5.2f etc
                            
                        } else
                        {
                            prevField.setFormat(null);
                        }
                    }
                } else
                {
                    log.error("We should never get here!");
                }
            }
            prevField = null;
        }
        hasFieldInfoChanged = false;
    }
    
    /**
     * 
     */
    protected void next()
    {
        getItemDataFromUI();
        int inx = fieldsList.getSelectedIndex();
        if (inx < fieldsModel.size()-1)
        {
            inx++;
        }
        fieldsList.setSelectedIndex(inx);
        updateBtns();
    }
    
    /**
     * 
     */
    protected void nextEmpty()
    {
        getItemDataFromUI();
        
        int inx = getNextEmptyIndex(fieldsList.getSelectedIndex()+1);
        if (inx > -1)
        {
            fieldsList.setSelectedIndex(inx);
        }
        updateBtns();
    }
    
    /**
     * 
     */
    protected void fillFieldList()
    {
        fieldsModel.clear();
        
        if (currJListItem != null)
        {
            LocalizableContainerIFace tbl = localizableIO.getContainer(currJListItem);
            if (tbl != null)
            {
                for (LocalizableJListItem fItem : localizableIO.getDisplayItems(currJListItem))
                {
                    fieldsModel.addElement(fItem);
                }
    
                fieldsList.setSelectedIndex(0);
            }
        }
        updateBtns();
    }
    
    /**
     * @return
     */
    protected LocalizableItemIFace getFieldItem(final int index)
    {
        if (currContainer != null)
        {
            LocalizableJListItem jlistFieldItem = (LocalizableJListItem)fieldsModel.get(index);
            if (jlistFieldItem != null)
            {
                return localizableIO.getItem(currContainer, jlistFieldItem);
            }
            log.error("fieldsList item was null");
        } else
        {
            log.error("currContainer was null");
        }

        return null;
    }
    
    /**
     * @return
     */
    protected LocalizableItemIFace getSelectedFieldItem()
    {
        if (currContainer != null)
        {
            if (fieldsList.getSelectedIndex() > -1)
            {
                LocalizableJListItem jlistFieldItem = (LocalizableJListItem)fieldsList.getSelectedValue();
                if (jlistFieldItem != null)
                {
                    return localizableIO.getItem(currContainer, jlistFieldItem);
                }
                log.error("fieldsList item was null");
            }
        } else
        {
            log.error("currContainer was null");
        }

        return null;
    }
    
    /**
     * 
     */
    protected void fieldSelected()
    {
        boolean ignoreChanges = isIgnoreChanges();
        
        setIgnoreChanges(true);
        
        if (statusBar != null)
        {
            statusBar.setText("");
        }
        
        LocalizableItemIFace fld = getSelectedFieldItem();
        if (fld != null)
        {
            enableUIControls(true);
            
            fieldInfo = fld != null ? tableInfo.getFieldByName(fld.getName()) : null;
            relInfo   = fieldInfo == null ? tableInfo.getRelationshipByName(fld.getName()) : null;
            
            if (pcl != null)
            {
                pcl.propertyChange(new PropertyChangeEvent(fieldsList, "index", null, fld));
            }
            
            fld = localizableIO.realize(fld);
            fieldDescText.setText(getDescStrForCurrLocale(fld));
            fieldNameText.setText(getNameDescStrForCurrLocale(fld));
            fieldHideChk.setSelected(fld.getIsHidden());
            
            if (pickLists == null)
            {
                pickLists = localizableIO.getPickLists(Discipline.getCurrentDiscipline().getDiscipline());
            }
            
            DBRelationshipInfo.RelationshipType relType = relInfo != null ? relInfo.getType() : null;
            String                              typeStr = fieldInfo != null ? fieldInfo.getType() : null;
            
            if (pickLists != null)
            {
                int selectedIndex = -1;
                DefaultComboBoxModel plCbxModel = (DefaultComboBoxModel)pickListCBX.getModel();
                
                if (typeStr != null && typeStr.equals("string"))
                {
                    plCbxModel.removeAllElements();
                    int inx = 0;
                    for (PickList pl : pickLists)
                    {
                        if (pl.getType() == PickListIFace.PL_WITH_ITEMS ||
                            pl.getType() == PickListIFace.PL_TABLE_FIELD)
                        {
                            plCbxModel.addElement(pl);
                            if (StringUtils.isNotEmpty(fld.getPickListName()) && fld.getPickListName().equals(pl.getName()))
                            {
                                selectedIndex = inx;
                            }
                        }
                        inx++;
                    }
                } else if (relType != null && relType == DBRelationshipInfo.RelationshipType.ManyToOne)
                {
                    plCbxModel.removeAllElements();
                    int inx = 0;
                    for (PickList pl : pickLists)
                    {
                        if (pl.getType() == PickListIFace.PL_WHOLE_TABLE)
                        {
                            plCbxModel.addElement(pl);
                            if (StringUtils.isNotEmpty(fld.getPickListName()) && fld.getPickListName().equals(pl.getName()))
                            {
                                selectedIndex = inx;
                            }
                        }
                        inx++;
                    }
                }
                pickListCBX.setEnabled((typeStr != null && typeStr.equals("string")) || relType != null);
                pickListCBX.setSelectedIndex(selectedIndex);
            } else
            {
                pickListCBX.setEnabled(false);
            }
            
            if (isDBSchema)
            {
                DBTableInfo ti = DBTableIdMgr.getInstance().getInfoByTableName(currContainer.getName());
                if (ti != null)
                {
                    DBFieldInfo fi = ti.getFieldByName(fld.getName());
                    if (fi != null)
                    {
                        String ts = fi.getType();
                        typeStr = ts.indexOf('.') > -1 ? StringUtils.substringAfterLast(fi.getType(), ".") : ts;
                        fieldTypeTxt.setText(typeStr);
                        
                        String lenStr = fi.getLength() != -1 ? Integer.toString(fi.getLength()) : " ";
                        fieldLengthTxt.setText(lenStr);
                        
                        fieldTypeLbl.setEnabled(true);
                        fieldTypeTxt.setEnabled(true);
                        
                        fieldLengthLbl.setEnabled(StringUtils.isNotEmpty(lenStr));
                        fieldLengthTxt.setEnabled(StringUtils.isNotEmpty(lenStr));

                    } else
                    {
                        DBRelationshipInfo ri = ti.getRelationshipByName(fld.getName());
                        if (ri != null)
                        {
                            fieldTypeTxt.setText(ri.getType().toString());
                            fieldTypeLbl.setEnabled(true);
                            
                            fieldLengthTxt.setText(" ");
                            fieldLengthLbl.setEnabled(false);
                            fieldLengthTxt.setEnabled(false);
                        }
                    }
                }
            }


            if (doAutoSpellCheck)
            {
                checker.spellCheck(fieldDescText);
                checker.spellCheck(fieldNameText);
            }
        } else
        {
            enableUIControls(false);
            fieldDescText.setText("");
            fieldNameText.setText("");
            fieldHideChk.setSelected(false);
            fieldTypeTxt.setText("");
            fieldLengthTxt.setText("");
        }
        
        fillFormatBox();
        
        boolean ok = fld != null;
        fieldDescText.setEnabled(ok);
        fieldNameText.setEnabled(ok);
        fieldNameLbl.setEnabled(ok);
        fieldDescLbl.setEnabled(ok);
        
        setIgnoreChanges(ignoreChanges);

        prevField = fld;
        
        updateBtns();
        
        hasFieldInfoChanged = false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizerBasePanel#enableSpellCheck()
     */
    @Override
    protected void enableSpellCheck()
    {
        boolean ok = SchemaI18NService.getCurrentLocale().getLanguage().equals("en");
        fldSpellChkBtn.setEnabled(ok && checker != null && spellCheckLoaded);
    }

    
    //------------------------------------------------------
    //
    //------------------------------------------------------
    class LengthWatcher extends KeyAdapter
    {
        protected int maxLength;
        
        public LengthWatcher(final int maxLength)
        {
            this.maxLength = maxLength;
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
         */
        @Override
        public void keyReleased(KeyEvent e)
        {
            super.keyReleased(e);
            final JTextComponent tc = (JTextComponent)e.getSource();
            String text = tc.getText();
            if (text.length() > maxLength)
            {
                if (statusBar != null)
                {                
                    statusBar.setErrorMessage("The text has exceed the maximum number of characters: "+maxLength);
                }
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        tc.setText(tc.getText().substring(0, maxLength));
                    }
                });
                
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizerBasePanel#localeChanged(java.util.Locale)
     */
    @Override
    public void localeChanged(Locale newLocale)
    {
        // do nothing
    }
}
