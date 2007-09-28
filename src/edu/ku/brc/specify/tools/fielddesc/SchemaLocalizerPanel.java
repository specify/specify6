/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.fielddesc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.tools.fielddesc.LocalizerApp.PackageTracker;
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
    
    protected LocalizableContainerIFace currContainer = null;
    
    // LocalizableContainerIFace Fields
    protected JList            tablesList;
    protected JTextArea        tblDescText   = new JTextArea();
    protected JTextField       tblNameText   = new JTextField();
    protected JLabel           tblDescLbl;
    protected JLabel           tblNameLbl;
    
    
    // LocalizableItemIFace Fields
    protected JList            fieldsList;
    protected JTextArea        fieldDescText = new JTextArea();
    protected JTextField       fieldNameText = new JTextField();
    protected JLabel           fieldDescLbl;
    protected JLabel           fieldNameLbl;
    protected DefaultListModel fieldsModel   = new DefaultListModel();
    protected JButton          nxtBtn;
    protected JButton          nxtEmptyBtn;

    protected LocalizableItemIFace prevTable = null;
    protected LocalizableItemIFace prevField = null;
    
    protected JStatusBar       statusBar      = null;
    protected JButton          tblSpellChkBtn = null;
    protected JButton          fldSpellChkBtn = null;
    
    protected Hashtable<String, String>         resHash     = new Hashtable<String, String>();
    protected Hashtable<String, PackageTracker> packageHash = new Hashtable<String, PackageTracker>();
    protected Hashtable<String, Boolean>        nameHash    = new Hashtable<String, Boolean>();

    /**
     * 
     */
    public SchemaLocalizerPanel()
    {
        init();
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
     * 
     */
    public void buildUI()
    {
        tablesList = new JList(localizableIO.getContainerDisplayItems());
        fieldsList = new JList(fieldsModel);
        
        tablesList.setVisibleRowCount(10);
        tablesList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {

            /* (non-Javadoc)
             * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
             */
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    getAllDataFromUI();
                    
                    LocalizableJListItem jlistItem = (LocalizableJListItem)tablesList.getSelectedValue();
                    if (jlistItem != null)
                    {
                        currContainer = localizableIO.getContainer(jlistItem);
                        
                        fillFieldList();
                        
                        if (currContainer != null)
                        {
                            tblDescText.setText(getDescStrForCurrLocale(currContainer));
                            tblNameText.setText(getNameDescStrForCurrLocale(currContainer));
        
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
                        }
                        
                        boolean ok = currContainer != null;
                        tblDescText.setEnabled(ok);
                        tblNameText.setEnabled(ok);
                        tblNameLbl.setEnabled(ok);
                        tblDescLbl.setEnabled(ok);
    
                        prevTable = currContainer;
                        
                    } else
                    {
                        currContainer = null;
                        log.error("jlistItem was null in list");
                    }
                }
            }
            
        });
        
        fieldsList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {

            /* (non-Javadoc)
             * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
             */
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

        PanelBuilder topInner   = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,4px,p,4px,p"));
        topInner.add(tblDescLbl = new JLabel("Name:", SwingConstants.RIGHT), cc.xy(1, 1));
        topInner.add(tblNameText,        cc.xy(3, 1));
        topInner.add(tblNameLbl = new JLabel("Desc:", SwingConstants.RIGHT), cc.xy(1, 3));
        topInner.add(sp,                 cc.xy(3, 3));
        topInner.add(tpbbp,              cc.xywh(1, 5, 3, 1));

        
        JScrollPane tblsp = new JScrollPane(tablesList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane fldsp = new JScrollPane(fieldsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        // LocalizableNameDescIFace
        PanelBuilder inner = new PanelBuilder(new FormLayout("max(200px;p),4px,p,2px,f:p:g", 
                                                             "p,2px,p,2px,p,2px,f:p:g"));
        inner.add(fldsp, cc.xywh(1, 1, 1, 7));
        inner.add(fieldNameLbl = new JLabel("Label:", SwingConstants.RIGHT), cc.xy(3, 1));
        inner.add(fieldNameText, cc.xy(5, 1));
        
        inner.add(fieldDescLbl = new JLabel("Desc:", SwingConstants.RIGHT), cc.xy(3, 3));
        sp = new JScrollPane(fieldDescText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        inner.add(sp,   cc.xy(5, 3));
        fieldDescText.setLineWrap(true);
        fieldDescText.setRows(8);
        fieldDescText.setWrapStyleWord(true);
        
        nxtBtn         = new JButton("Next");
        nxtEmptyBtn    = new JButton("Next Empty");
        fldSpellChkBtn = new JButton("Spell Check");
        
        JPanel bbp = ButtonBarFactory.buildCenteredBar(new JButton[] {nxtEmptyBtn, nxtBtn, fldSpellChkBtn});
        inner.add(bbp,   cc.xywh(3, 5, 3, 1));

        
        //bbp = ButtonBarFactory.buildCenteredBar(new JButton[] {exitBtn, saveBtn});
        
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("max(200px;p),4px,f:p:g", "p,4px,t:p,4px,p,4px,f:p:g,4px,p,4px,p"), this);
        pb.addSeparator("Tables",   cc.xywh(1, 1, 3, 1));
        pb.add(tblsp,               cc.xy  (1, 3));
        pb.add(topInner.getPanel(), cc.xy  (3, 3));
        pb.addSeparator("Fields",   cc.xywh(1, 5, 3, 1));
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
        
        //statusBar.setSectionText(0, currLocale.getDisplayName());
        
        fieldDescText.setEnabled(false);
        fieldNameText.setEnabled(false);
        
        tblDescText.setEnabled(false);
        tblNameText.setEnabled(false);
        
        fieldNameLbl.setEnabled(false);
        fieldDescLbl.setEnabled(false);
        
        tblNameLbl.setEnabled(false);
        tblDescLbl.setEnabled(false);
        
        checkLocaleMenu(currLocale);
        
        tblSpellChkBtn.setEnabled(checker != null);
        fldSpellChkBtn.setEnabled(checker != null);
    }
    
    /**
     * @param statusBar the statusBar to set
     */
    public void setStatusBar(JStatusBar statusBar)
    {
        this.statusBar = statusBar;
    }

    /**
     * @param srcLocale
     * @param dstLocale
     */
    protected void copy(final Locale srcLocale, final Locale dstLocale)
    {
        for (LocalizableJListItem listItem : localizableIO.getContainerDisplayItems())
        {
            LocalizableContainerIFace table = localizableIO.getContainer(listItem);
            
            copyLocale(table, srcLocale, dstLocale);
            
            for (LocalizableItemIFace field : table.getContainerItems())
            {
                copyLocale(field, srcLocale, dstLocale);
            }
        }
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
                LocalizableItemIFace f = (LocalizableItemIFace)getFieldItem(i);
                if (f != null)
                {
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
    
    protected void getContainerDataFromUI()
    {
        if (prevTable != null)
        {
            boolean nameChanged = setNameDescStrForCurrLocale(prevTable, tblNameText.getText());
            boolean descChanged = setDescStrForCurrLocale(prevTable,     tblDescText.getText());
            if (nameChanged || descChanged)
            {
                setHasChanged(true);
            }
            prevTable = null;
        }
        
    }
    
    protected void getItemDataFromUI()
    {
        if (prevField != null)
        {
            boolean nameChanged = setNameDescStrForCurrLocale(prevField, fieldNameText.getText());
            boolean descChanged = setDescStrForCurrLocale(prevField,     fieldDescText.getText());
            if (nameChanged || descChanged)
            {
                setHasChanged(true);
            }
            prevField = null;
        }
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
            } else
            {
                log.error("fieldsList item was null");
            }
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
                } else
                {
                    log.error("fieldsList item was null");
                }
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
        statusBar.setText("");
        
        LocalizableItemIFace fld  = getSelectedFieldItem();
        if (fld != null)
        {
            fieldDescText.setText(getDescStrForCurrLocale(fld));
            fieldNameText.setText(getNameDescStrForCurrLocale(fld));
            
            if (doAutoSpellCheck)
            {
                checker.spellCheck(fieldDescText);
                checker.spellCheck(fieldNameText);
            }
        } else
        {
            fieldDescText.setText("");
            fieldNameText.setText(""); 
        }
        
        boolean ok = fld != null;
        fieldDescText.setEnabled(ok);
        fieldNameText.setEnabled(ok);
        fieldNameLbl.setEnabled(ok);
        fieldDescLbl.setEnabled(ok);

        prevField = fld;
        
        updateBtns();
    }
    
    /**
     * @param lndi
     * @param localeHash
     */
    public static void checkForLocales(final LocalizableItemIFace lndi, final Hashtable<String, String> localeHash)
    {
        lndi.fillDescs(descsList);
        lndi.fillNames(namesList);
        
        for (LocalizableStrIFace nm : namesList)
        {
            if (nm.getLanguage().equals("de"))
            {
                int x= 0;
                x++;
            }
            localeHash.put(makeLocaleKey(nm.getLanguage(), nm.getCountry(), nm.getVariant()), "X");
        }
        for (LocalizableStrIFace d : descsList)
        {
            if (d.getLanguage().equals("de"))
            {
                int x= 0;
                x++;
            }
            localeHash.put(makeLocaleKey(d.getLanguage(), d.getCountry(), d.getVariant()), "X");
        }
    }
    
    /**
     * @param locale
     * @return
     */
    public Vector<Locale> getLocalesInUse()
    {
        Hashtable<String, String> localeHash = new Hashtable<String, String>();
        for (LocalizableJListItem listItem : localizableIO.getContainerDisplayItems())
        {
            LocalizableContainerIFace table = localizableIO.getContainer(listItem);
            checkForLocales(table, localeHash);
            for (LocalizableItemIFace f : table.getContainerItems())
            {
                checkForLocales(f, localeHash);
            }
        }
        Vector<Locale> inUseLocales = new Vector<Locale>(localeHash.keySet().size()+10);
        for (String key : localeHash.keySet())
        {
            String[] toks = StringUtils.split(key, "_");
            inUseLocales.add(new Locale(toks[0], "", ""));
        }
        return inUseLocales;
    }
    
    /**
     * @param locale
     * @return
     */
    public boolean isLocaleInUse(final Locale locale)
    {
        Hashtable<String, String> localeHash = new Hashtable<String, String>();
        for (LocalizableJListItem listItem : localizableIO.getContainerDisplayItems())
        {
            LocalizableContainerIFace table = localizableIO.getContainer(listItem);
            checkForLocales(table, localeHash);
            for (LocalizableItemIFace f : table.getContainerItems())
            {
                checkForLocales(f, localeHash);
            }
        }
        for (String key : localeHash.keySet())
        {
            System.out.println("In Use: "+key);
        }
        return localeHash.get(makeLocaleKey(locale)) != null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizerBasePanel#localeChanged(java.lang.String)
     */
    public void localeChanged(final Locale newLocale)
    {
        int tableInx = tablesList.getSelectedIndex();
        int fieldInx = fieldsList.getSelectedIndex();
        
        tablesList.getSelectionModel().clearSelection();
        
        Locale oldLocale = currLocale;
        
        currLocale = newLocale;
        checkLocaleMenu(currLocale);
        
        if (!isLocaleInUse(currLocale))
        {
            int rv = JOptionPane.showConfirmDialog(UIRegistry.getTopWindow(),
                    "Do you wish to copy a Locale?", "Locale is Empty", JOptionPane.YES_NO_OPTION);
            if (rv == JOptionPane.YES_OPTION)
            {
                Locale localeToCopy = chooseNewLocale(getLocalesInUse());
                if (localeToCopy != null)
                {
                    copy(localeToCopy, currLocale);
                    setHasChanged(true);
                }
            } else
            {
                currLocale = oldLocale;
                checkLocaleMenu(currLocale);
            }
        } else
        {
            log.debug("Locale["+currLocale.getDisplayName()+"] was in use.");
        }
        
        if (tableInx != -1)
        {
            tablesList.setSelectedIndex(tableInx);
        }
        if (fieldInx != -1)
        {
            fieldsList.setSelectedIndex(fieldInx);
        }
        
        statusBar.setSectionText(0, currLocale.getDisplayName());
        boolean ok = currLocale.getLanguage().equals("en");
        tblSpellChkBtn.setEnabled(ok && checker != null);
        fldSpellChkBtn.setEnabled(ok && checker != null);
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
                        String text = tc.getText();
                        tc.setText(text.substring(0, maxLength));
                    }
                    
                });
                
            }
        }
        
    }
    
    /**
     * @param item
     * @param srcLocale
     * @param dstLocale
     */
    public void copyLocale(LocalizableItemIFace item, final Locale srcLocale, final Locale dstLocale)
    {
        item.fillDescs(descsList);
        item.fillNames(namesList);
        
        LocalizableStrIFace srcName = null;
        for (LocalizableStrIFace nm : namesList)
        {
            if (nm.isLocale(srcLocale))
            {
                srcName = nm;
                break;
            }
        }
        
        if (srcName != null)
        {
            LocalizableStrIFace name = localizableStrFactory.create(srcName.getText(), dstLocale);
            item.addName(name);
        }

        LocalizableStrIFace srcDesc = null;
        for (LocalizableStrIFace d : descsList)
        {
            if (d.isLocale(srcLocale))
            {
                srcDesc = d;
                break;
            }
        }
        
        if (srcDesc != null)
        {
            LocalizableStrIFace desc = localizableStrFactory.create(srcDesc.getText(), dstLocale);
            item.addDesc(desc);
        }       
    }
}
