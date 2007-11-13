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
import java.util.Locale;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import edu.ku.brc.ui.JStatusBar;

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
            
    protected LocalizableIOIFace localizableIO = null;
    
    protected SchemaLocalizerPanel      schemaPanel;
    
    protected LocalizableJListItem      currJListItem   = null;
    protected LocalizableContainerIFace currContainer   = null;
    protected boolean                   includeHiddenUI = true;              // Must be set before creatng the panel
    protected boolean                   isDBSchema = true;

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

    protected LocalizableItemIFace prevField = null;
    
    protected JStatusBar       statusBar      = null;
    protected JButton          fldSpellChkBtn = null;
    
    protected PropertyChangeListener pcl   = null;
    
    /**
     * 
     */
    public FieldItemPanel(final SchemaLocalizerPanel schemaPanel,
                          final boolean              includeHiddenUI, 
                          final boolean              isDBSchema,
                          final PropertyChangeListener pcl)
    {
        this.schemaPanel     = schemaPanel;
        this.includeHiddenUI = includeHiddenUI;
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
        PanelBuilder pb = new PanelBuilder(new FormLayout("max(200px;p),4px,p,2px,f:p:g", 
                                                             (includeHiddenUI ? "p,2px," : "") + 
                                                             (isDBSchema ? "p,2px,p,2px," : "") + 
                                                             "p,2px,p,2px,p,2px,p,2px,p,2px,f:p:g"), this);
        
        pb.add(fldsp, cc.xywh(1, y, 1, 7+(isDBSchema ? 4 : 0)));
        pb.add(fieldNameLbl = new JLabel(labelStr, SwingConstants.RIGHT), cc.xy(3, y));
        pb.add(fieldNameText, cc.xy(5, y));   y += 2;
        
        if (includeHiddenUI)
        {
            pb.add(fieldHideChk, cc.xy(5, y)); y += 2;
        }
       
        pb.add(fieldDescLbl = new JLabel(descStr, SwingConstants.RIGHT), cc.xy(3, y));
        JScrollPane sp = new JScrollPane(fieldDescText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pb.add(sp,   cc.xy(5, y));   y += 2;
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
        
        
        nxtBtn         = new JButton(getResourceString("NEXT"));
        nxtEmptyBtn    = new JButton(getResourceString("SL_NEXT_EMPTY"));
        fldSpellChkBtn = new JButton(getResourceString("SL_SPELL_CHECK"));
        
        JPanel bbp = ButtonBarFactory.buildCenteredBar(new JButton[] {nxtEmptyBtn, nxtBtn, fldSpellChkBtn});
        pb.add(bbp,   cc.xywh(3, y, 3, 1));
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
        
        SchemaI18NService.getInstance().checkCurrentLocaleMenu();
        
        enableUIControls(false);
        
        setIgnoreChanges(false);
    }
    
    public void setContainer(LocalizableContainerIFace container,
                             LocalizableJListItem      jListContainerItem)
    {
        currContainer = container;
        currJListItem = jListContainerItem;
        
        fillFieldList();
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
                for (LocalizableJListItem f : localizableIO.getDisplayItems(currJListItem))
                {
                    fieldsModel.addElement(f);
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
            
            if (pcl != null)
            {
                pcl.propertyChange(new PropertyChangeEvent(fieldsList, "index", null, fld));
            }
            
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
            enableUIControls(false);
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
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizerBasePanel#enableSpellCheck()
     */
    @Override
    protected void enableSpellCheck()
    {
        boolean ok = SchemaI18NService.getCurrentLocale().getLanguage().equals("en");
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

    @Override
    public void localeChanged(Locale newLocale)
    {
        // mothing
    }
}
