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
import java.util.Hashtable;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
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
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.config.SpecifySchemaI18NService;
import edu.ku.brc.specify.tools.schemalocale.LocalizerApp.PackageTracker;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 25, 2007
 *
 */
public class SchemaLocalizerPanel extends LocalizerBasePanel
{
    private static final Logger log = Logger.getLogger(SchemaLocalizerPanel.class);
            
    protected LocalizableIOIFace localizableIO = null;
    
    protected LocalizableContainerIFace currContainer   = null;
    protected boolean                   includeHiddenUI = true;              // Must be set before creatng the panel
    
    // LocalizableContainerIFace Fields
    protected JList            tablesList;
    protected JTextArea        tblDescText   = new JTextArea();
    protected JTextField       tblNameText   = new JTextField();
    protected JLabel           tblDescLbl;
    protected JLabel           tblNameLbl;
    protected JCheckBox        tblHideChk  = new JCheckBox(getResourceString("SL_TABLE_HIDE_CHK"));
    protected boolean          hasTableInfoChanged  = false;
    
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
    protected DefaultListModel fieldsModel   = new DefaultListModel();
    protected JButton          nxtBtn;
    protected JButton          nxtEmptyBtn;
    protected JCheckBox        fieldHideChk  = new JCheckBox(getResourceString("SL_FIELD_HIDE_CHK"));
    protected boolean          hasFieldInfoChanged  = false;

    protected LocalizableItemIFace prevTable = null;
    protected LocalizableItemIFace prevField = null;
    
    protected JStatusBar       statusBar      = null;
    protected JButton          tblSpellChkBtn = null;
    protected JButton          fldSpellChkBtn = null;
    
    protected Hashtable<String, String>         resHash     = new Hashtable<String, String>();
    protected Hashtable<String, PackageTracker> packageHash = new Hashtable<String, PackageTracker>();
    protected Hashtable<String, Boolean>        nameHash    = new Hashtable<String, Boolean>();
    
    protected PropertyChangeListener            listener    = null;
    
    protected boolean          isDBSchema = true;

    /**
     * 
     */
    public SchemaLocalizerPanel(PropertyChangeListener l)
    {
        listener = l;
        init();
    }

    /**
     * 
     */
    public void buildUI()
    {
        setIgnoreChanges(true);
        
        tablesList = new JList(localizableIO.getContainerDisplayItems());
        fieldsList = new JList(fieldsModel);
        
        tablesList.setVisibleRowCount(10);
        tablesList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    startTableSelected();
                }
            }
            
        });
        
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
        
        // LocalizableContainerIFace Section Layout
        tblSpellChkBtn               = new JButton("Spell Check");
        JPanel      tpbbp            = ButtonBarFactory.buildCenteredBar(new JButton[] {tblSpellChkBtn});
        JScrollPane sp               = new JScrollPane(tblDescText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tblDescText.setRows(8);
        tblDescText.setLineWrap(true);
        tblDescText.setWrapStyleWord(true);
        tblDescText.addKeyListener(new LengthWatcher(255));
        tblNameText.addKeyListener(new LengthWatcher(64));
        
        String descStr  = getResourceString("SL_DESC") + ":";
        String nameStr  = getResourceString("SL_NAME") + ":";
        String labelStr = getResourceString("SL_LABEL") + ":";

        int y = 1;
        PanelBuilder topInner   = new PanelBuilder(new FormLayout("p,2px,f:p:g", 
                                                                  "p,2px,"+(includeHiddenUI ? "p,2px," : "") +"p,2px,p"));
        
        topInner.add(tblDescLbl = new JLabel(nameStr, SwingConstants.RIGHT), cc.xy(1, y));
        topInner.add(tblNameText, cc.xy(3, y)); y += 2;
        
        if (includeHiddenUI)
        {
            topInner.add(tblHideChk, cc.xy(3, y)); y += 2;
        }
        
        topInner.add(tblNameLbl = new JLabel(descStr, SwingConstants.RIGHT), cc.xy(1, y)); 
        topInner.add(sp,    cc.xy(3, y));   y += 2;
        
        topInner.add(tpbbp, cc.xywh(1, y, 3, 1));

        
        JScrollPane tblsp = new JScrollPane(tablesList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane fldsp = new JScrollPane(fieldsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        // LocalizableNameDescIFace
        PanelBuilder inner = new PanelBuilder(new FormLayout("max(200px;p),4px,p,2px,f:p:g", 
                                                             (includeHiddenUI ? "p,2px," : "") + 
                                                             (isDBSchema ? "p,2px,p,2px," : "") + 
                                                             "p,2px,p,2px,p,2px,p,2px,p,2px,f:p:g"));
        y = 1;
        inner.add(fldsp, cc.xywh(1, y, 1, 7+(isDBSchema ? 4 : 0)));
        inner.add(fieldNameLbl = new JLabel(labelStr, SwingConstants.RIGHT), cc.xy(3, y));
        inner.add(fieldNameText, cc.xy(5, y));   y += 2;
        
        if (includeHiddenUI)
        {
            inner.add(fieldHideChk, cc.xy(5, y)); y += 2;
        }
       
        inner.add(fieldDescLbl = new JLabel(descStr, SwingConstants.RIGHT), cc.xy(3, y));
        sp = new JScrollPane(fieldDescText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        inner.add(sp,   cc.xy(5, y));   y += 2;
        fieldDescText.setLineWrap(true);
        fieldDescText.setRows(8);
        fieldDescText.setWrapStyleWord(true);
        
        if (isDBSchema)
        {
            fieldTypeTxt   = new JLabel("");
            fieldLengthTxt = new JLabel("");
            
            inner.add(fieldTypeLbl   = new JLabel(getResourceString("SL_TYPE") + ":", SwingConstants.RIGHT), cc.xy(3, y));
            inner.add(fieldTypeTxt,   cc.xy(5, y));   y += 2;
            inner.add(fieldLengthLbl = new JLabel(getResourceString("SL_LENGTH") + ":", SwingConstants.RIGHT), cc.xy(3, y));
            inner.add(fieldLengthTxt, cc.xy(5, y));   y += 2;
            
            fieldTypeTxt.setBackground(Color.WHITE);
            fieldLengthTxt.setBackground(Color.WHITE);
            fieldTypeTxt.setOpaque(true);
            fieldLengthTxt.setOpaque(true);
        }
        
        
        nxtBtn         = new JButton(getResourceString("NEXT"));
        nxtEmptyBtn    = new JButton(getResourceString("SL_NEXT_EMPTY"));
        fldSpellChkBtn = new JButton(getResourceString("SL_SPELL_CHECK"));
        
        JPanel bbp = ButtonBarFactory.buildCenteredBar(new JButton[] {nxtEmptyBtn, nxtBtn, fldSpellChkBtn});
        inner.add(bbp,   cc.xywh(3, y, 3, 1));

        
        //bbp = ButtonBarFactory.buildCenteredBar(new JButton[] {exitBtn, saveBtn});
        
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("max(200px;p),4px,f:p:g", 
                                                          "p,4px,t:p,4px,p,4px,f:p:g,4px,p,4px,p"), this);
        pb.addSeparator(getResourceString("SL_TABLES"),   cc.xywh(1, 1, 3, 1));
        pb.add(tblsp,               cc.xy  (1, 3));
        pb.add(topInner.getPanel(), cc.xy  (3, 3));
        pb.addSeparator(getResourceString("SL_FIELDS"),   cc.xywh(1, 5, 3, 1));
        pb.add(inner.getPanel(),    cc.xywh(1, 7, 3, 1));
        //pb.add(statusBar,           cc.xywh(1, 11, 3, 1));

        pb.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
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
        
        tblSpellChkBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (checker != null)
                {
                    checker.spellCheck(tblDescText);
                    checker.spellCheck(tblNameText);
                }
            }
            
        });
        
        fieldHideChk.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                setHasChanged(true);
                hasFieldInfoChanged = true;
            }
            
        });
        
        tblHideChk.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                setHasChanged(true);
                hasTableInfoChanged = true;
            }
        });
        
        DocumentListener dl = new DocumentListener() {
            protected void changed()
            {
                if (!hasFieldInfoChanged)
                {
                    hasFieldInfoChanged = true;
                    setHasChanged(true);
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
        
        dl = new DocumentListener() {
            protected void changed()
            {
                if (!hasTableInfoChanged)
                {
                    hasTableInfoChanged = true;
                    setHasChanged(true);
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
        tblNameText.getDocument().addDocumentListener(dl);
        tblDescText.getDocument().addDocumentListener(dl);
        
        //statusBar.setSectionText(0, currLocale.getDisplayName());
        
        tablesList.setEnabled(false);
        
        SpecifySchemaI18NService.getInstance().checkCurrentLocaleMenu();
        
        enableUIControls(false);
        
        setIgnoreChanges(false);
    }
    
    
    /**
     * @param includeHiddenUI tells it to include the checkboxes for hiding tables and fields
     */
    public void setIncludeHiddenUI(boolean includeHiddenUI)
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
    public void setLocalizableIO(LocalizableIOIFace localizableIO)
    {
        this.localizableIO = localizableIO;
    }
    
    
    /**
     * @return the main list of containers/tables
     */
    public JList getContainerList()
    {
        return tablesList;
    }

    /**
     * Enable the controls.
     * @param enable true/false
     */
    protected void enableUIControls(final boolean enable)
    {
        fieldDescText.setEnabled(enable);
        fieldNameText.setEnabled(enable);
        
        tblDescText.setEnabled(enable);
        tblNameText.setEnabled(enable);
        
        fieldNameLbl.setEnabled(enable);
        fieldDescLbl.setEnabled(enable);
        
        tblNameLbl.setEnabled(enable);
        tblDescLbl.setEnabled(enable);
        
        fieldHideChk.setEnabled(enable);
        tblHideChk.setEnabled(enable);
        
        fieldTypeLbl.setEnabled(enable);
        fieldTypeTxt.setEnabled(enable);
        
        fieldLengthLbl.setEnabled(enable);
        fieldLengthTxt.setEnabled(enable);
        
        if (!enable)
        {
            tblSpellChkBtn.setEnabled(false);
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
     * 
     */
    protected void startTableSelected()
    {
       getAllDataFromUI();
       
       setIgnoreChanges(true);
       
        LocalizableJListItem jlistItem = (LocalizableJListItem)tablesList.getSelectedValue();
        if (jlistItem != null)
        {
            currContainer = localizableIO.getContainer(jlistItem);
            if (currContainer != null)
            {
                fillFieldList();
                
                if (currContainer != null)
                {
                    currContainer = (LocalizableContainerIFace)localizableIO.realize(currContainer);
                    tblDescText.setText(getDescStrForCurrLocale(currContainer));
                    tblNameText.setText(getNameDescStrForCurrLocale(currContainer));
                    tblHideChk.setSelected(currContainer.getIsHidden());
                    
                    if (doAutoSpellCheck)
                    {
                        checker.spellCheck(tblNameText);
                        checker.spellCheck(tblDescText);
                    }
                } else
                {
                    tblDescText.setText("");
                    tblNameText.setText("");
                    fieldsList.setSelectedIndex(-1);
                    tblHideChk.setSelected(false);
                }

                prevTable = currContainer;
                
            } else
            {
                log.error("jlistItem was null in list");
            }
            enableUIControls(currContainer != null);
        }
        
        hasTableInfoChanged = false;
        setIgnoreChanges(false);
    }
    
    protected void tableItemRetrieved()
    {
    }
    
    /**
     * @param statusBar the statusBar to set
     */
    public void setStatusBar(JStatusBar statusBar)
    {
        this.statusBar = statusBar;
    }
    
    /**
     * @param item
     * @param srcLocale
     * @param dstLocale
     */
    protected void startLocaleCopy(final Locale srcLocale, final Locale dstLocale)
    {
        enableUIControls(false);
        UIRegistry.getStatusBar().setText(UIRegistry.getResourceString("COPYING_LOCALE")); // XXX I18N (this may not need to be localized)
        tablesList.setEnabled(false);
        fieldsList.setEnabled(false);
        
        if (listener != null)
        {
            listener.propertyChange(new PropertyChangeEvent(this, "copyStart", null, null));
        }
        UIRegistry.getStatusBar().setIndeterminate(true);
        
        SwingWorker workerThread = new SwingWorker()
        {
            @Override
            public Object construct()
            {
                localizableIO.copyLocale(srcLocale, dstLocale);  
                
                return null;
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public void finished()
            {
                UIRegistry.getStatusBar().setText("");
                enableUIControls(true);
                tablesList.setEnabled(true);
                fieldsList.setEnabled(true);
                
                if (listener != null)
                {
                    listener.propertyChange(new PropertyChangeEvent(this, "copyEnd", null, null));
                }
                UIRegistry.getStatusBar().setIndeterminate(false);
            }
        };
        
        // start the background task
        workerThread.start();
    }

    /**
     * @param tableName
     * @return
     */
    public String getContainerDescStr(final LocalizableJListItem listItem)
    {
        LocalizableContainerIFace table = localizableIO.getContainer(listItem);
        if (table != null)
        {
           return getDescStrForCurrLocale(table);
        }
        log.error("Couldn't find table ["+listItem.getName()+"]");
        return null;
    }
    
    /**
     * @param tableName
     * @return
     */
    public LocalizableStrIFace getContainerDesc(final LocalizableJListItem listItem)
    {
        LocalizableContainerIFace table = localizableIO.getContainer(listItem);
        if (table != null)
        {
           return getDescForCurrLocale(table);
        }
        log.error("Couldn't find table ["+listItem.getName()+"]");
        return null;
    }
    
    /**
     * @param tableName
     * @return
     */
    public LocalizableStrIFace getContainerNameDesc(final LocalizableJListItem listItem)
    {
        LocalizableContainerIFace table = localizableIO.getContainer(listItem);
        if (table != null)
        {
           return getNameDescForCurrLocale(table);
        }
        log.error("Couldn't find table ["+listItem.getName()+"]");
        return null;
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
        getContainerDataFromUI();
        getItemDataFromUI();
    }
    
    /**
     * 
     */
    protected void getContainerDataFromUI()
    {
        if (prevTable != null && hasTableInfoChanged)
        {
            prevTable.setIsHidden(tblHideChk.isSelected());
            
            boolean nameChanged = setNameDescStrForCurrLocale(prevTable, tblNameText.getText());
            boolean descChanged = setDescStrForCurrLocale(prevTable,     tblDescText.getText());
            if (nameChanged || descChanged)
            {
                setHasChanged(true);
            }
            prevTable = null;
        }
        hasTableInfoChanged = false;
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
        
        LocalizableJListItem jlistContainerItem = (LocalizableJListItem)tablesList.getSelectedValue();
        if (jlistContainerItem != null)
        {
            LocalizableContainerIFace tbl = localizableIO.getContainer(jlistContainerItem);
            if (tbl != null)
            {
                for (LocalizableJListItem f : localizableIO.getDisplayItems(jlistContainerItem))
                {
                    fieldsModel.addElement(f);
                }
    
                fieldsList.setSelectedIndex(0);
            }
            updateBtns();
        } else
        {
            log.error("jlistItem can't be null");
        }
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
        
        statusBar.setText("");
        
        LocalizableItemIFace fld = getSelectedFieldItem();
        if (fld != null)
        {
            fld = localizableIO.realize(fld);
            fieldDescText.setText(getDescStrForCurrLocale(fld));
            fieldNameText.setText(getNameDescStrForCurrLocale(fld));
            fieldHideChk.setSelected(fld.getIsHidden());
            
            if (isDBSchema)
            {
                DBTableInfo ti = DBTableIdMgr.getInstance().getInfoByTableName(currContainer.getName());
                if (ti != null)
                {
                    DBFieldInfo fi = ti.getFieldByName(fld.getName());
                    if (fi != null)
                    {
                        String ts = fi.getType();
                        String typeStr = ts.indexOf('.') > -1 ? StringUtils.substringAfterLast(fi.getType(), ".") : ts;
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
            fieldDescText.setText("");
            fieldNameText.setText("");
            fieldHideChk.setSelected(false);
            fieldTypeTxt.setText("");
            fieldLengthTxt.setText("");
        }
        
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
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizerBasePanel#localeChanged(java.lang.String)
     */
    public void localeChanged(final Locale newLocale)
    {
        int tableInx = tablesList.getSelectedIndex();
        int fieldInx = fieldsList.getSelectedIndex();
        
        tablesList.getSelectionModel().clearSelection();
        
        Locale oldLocale = SchemaI18NService.getCurrentLocale();
        
        SchemaI18NService.setCurrentLocale(newLocale);
        SchemaI18NService.getInstance().checkCurrentLocaleMenu();
        
        if (!localizableIO.isLocaleInUse(newLocale))
        {
            int rv = JOptionPane.showConfirmDialog(UIRegistry.getTopWindow(),
                                                   getResourceString("SL_WISH_CHANGE_LOCALE"), 
                                                   getResourceString("SL_LOCALE_EMPTY"), 
                                                   JOptionPane.YES_NO_OPTION);
            if (rv == JOptionPane.YES_OPTION)
            {
                Locale localeToCopy = chooseNewLocale(localizableIO.getLocalesInUse());
                if (localeToCopy != null)
                {
                    startLocaleCopy(localeToCopy, newLocale);
                    setHasChanged(true);
                }
            } else
            {
                SchemaI18NService.setCurrentLocale(oldLocale);
                SchemaI18NService.getInstance().checkCurrentLocaleMenu();
            }
        } else
        {
            log.debug("Locale["+newLocale+"] was in use.");
        }
        
        if (tableInx != -1)
        {
            tablesList.setSelectedIndex(tableInx);
        }
        if (fieldInx != -1)
        {
            fieldsList.setSelectedIndex(fieldInx);
        }
        
        statusBar.setSectionText(0, newLocale.getDisplayName());
        
        enableSpellCheck();
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizerBasePanel#enableSpellCheck()
     */
    @Override
    protected void enableSpellCheck()
    {
        boolean ok = SchemaI18NService.getCurrentLocale().getLanguage().equals("en")  && tablesList.isEnabled();
        tblSpellChkBtn.setEnabled(ok && checker != null && spellCheckLoaded);
        fldSpellChkBtn.setEnabled(ok && checker != null && spellCheckLoaded);
    }



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
                statusBar.setErrorMessage("The text has exceed the maximum number of characters: "+maxLength);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        tc.setText(tc.getText().substring(0, maxLength));
                    }
                });
                
            }
        }
    }
}
