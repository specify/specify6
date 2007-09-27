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
            
    protected Vector<LocalizerContainerIFace>            tables     = new Vector<LocalizerContainerIFace>();
    protected Hashtable<String, LocalizerContainerIFace> tableHash  = new Hashtable<String, LocalizerContainerIFace>();
    
    // LocalizerContainerIFace Fields
    protected JList            tablesList;
    protected JTextArea        tblDescText   = new JTextArea();
    protected JTextField       tblNameText   = new JTextField();
    protected JLabel           tblDescLbl;
    protected JLabel           tblNameLbl;
    
    // LocalizableNameDescIFace Fields
    protected JList            fieldsList;
    protected JTextArea        fieldDescText = new JTextArea();
    protected JTextField       fieldNameText = new JTextField();
    protected JLabel           fieldDescLbl;
    protected JLabel           fieldNameLbl;
    protected DefaultListModel fieldsModel   = new DefaultListModel();
    protected JButton          nxtBtn;
    protected JButton          nxtEmptyBtn;
    
    protected LocalizableNameDescIFace prevTable = null;
    protected LocalizableNameDescIFace prevField = null;
    
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
     * 
     */
    public void buildUI()
    {
        tablesList = new JList(tables);
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
                    fillFieldList();
                    getAllDataFromUI();
                    
                    LocalizerContainerIFace tbl  = (LocalizerContainerIFace)tablesList.getSelectedValue();
                    if (tbl != null)
                    {
                        tblDescText.setText(getDescStrForCurrLocale(tbl));
                        tblNameText.setText(getNameDescStrForCurrLocale(tbl));
    
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
                    
                    boolean ok = tbl != null;
                    tblDescText.setEnabled(ok);
                    tblNameText.setEnabled(ok);
                    tblNameLbl.setEnabled(ok);
                    tblDescLbl.setEnabled(ok);

                    prevTable = tbl;
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
        
        // LocalizerContainerIFace Section Layout
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
                checker.spellCheck(fieldDescText);
            }
            
        });
        
        tblSpellChkBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                checker.spellCheck(tblDescText);
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
    }
    
    /**
     * @param statusBar the statusBar to set
     */
    public void setStatusBar(JStatusBar statusBar)
    {
        this.statusBar = statusBar;
    }

    /**
     * @return the tables
     */
    public Vector<LocalizerContainerIFace> getTables()
    {
        return tables;
    }

    
    /**
     * @param tables the tables to set
     */
    public void setTables(Vector<LocalizerContainerIFace> tables)
    {
        this.tables = tables;
        
        tableHash.clear();
        for (LocalizerContainerIFace table : tables)
        {
            tableHash.put(table.getName(), table);
        }
    }

    /**
     * @param srcLocale
     * @param dstLocale
     */
    protected void copy(final Locale srcLocale, final Locale dstLocale)
    {
        for (LocalizerContainerIFace table : tables)
        {
            table.copyLocale(srcLocale, dstLocale);
            
            for (LocalizableNameDescIFace field : table.getItems())
            {
                field.copyLocale(srcLocale, dstLocale);
            }
        }
    }

    /**
     * @param tableName
     * @return
     */
    public String getTableDescStr(final String tableName)
    {
        LocalizerContainerIFace table = tableHash.get(tableName);
        if (table != null)
        {
           return getDescStrForCurrLocale(table);
        }
        log.error("Couldn't find table ["+tableName+"]");
        return null;
    }
    
    /**
     * @param tableName
     * @return
     */
    public Desc getTableDesc(final String tableName)
    {
        LocalizerContainerIFace table = tableHash.get(tableName);
        if (table != null)
        {
           return getDescForCurrLocale(table);
        }
        log.error("Couldn't find table ["+tableName+"]");
        return null;
    }
    
    /**
     * @param tableName
     * @return
     */
    public Name getTableNameDesc(final String tableName)
    {
        LocalizerContainerIFace table = tableHash.get(tableName);
        if (table != null)
        {
           return getNameDescForCurrLocale(table);
        }
        log.error("Couldn't find table ["+tableName+"]");
        return null;
    }
    
    /**
     * @param tableName
     * @return
     */
    public Desc getFieldDesc(final String tableName, final String fieldName)
    {
        LocalizerContainerIFace table = tableHash.get(tableName);
        if (table != null)
        {
            for (LocalizableNameDescIFace f : table.getItems())
            {
                if (f.getName().equals(fieldName))
                {
                    return getDescForCurrLocale(f);
                }
            }
           
        }
        log.error("Couldn't find table ["+tableName+"]");
        return null;
    }
    
    /**
     * @param tableName
     * @return
     */
    public Name getFieldNameDesc(final String tableName, final String fieldName)
    {
        LocalizerContainerIFace table = tableHash.get(tableName);
        if (table != null)
        {
            for (LocalizableNameDescIFace f : table.getItems())
            {
                if (f.getName().equals(fieldName))
                {
                    return getNameDescForCurrLocale(f);
                }
            }
        }
        log.error("Couldn't find table ["+tableName+"]");
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
                LocalizableNameDescIFace f = (LocalizableNameDescIFace)fieldsModel.get(i);
                if (f != null)
                {
                    Desc  desc = getDescForCurrLocale(f);
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
        getTableDataFromUI();
        getFieldDataFromUI();
    }
    
    protected void getTableDataFromUI()
    {
        if (prevTable != null)
        {
            boolean nameChanged = setNameDescStrForCurrLocale(prevTable, tblNameText.getText());
            boolean descChanged = setDescStrForCurrLocale(prevTable,     tblDescText.getText());
            if (nameChanged || descChanged)
            {
                setHasChanged(true);
            }
        }
        
    }
    
    protected void getFieldDataFromUI()
    {
        if (prevField != null)
        {
            boolean nameChanged = setNameDescStrForCurrLocale(prevField, fieldNameText.getText());
            boolean descChanged = setDescStrForCurrLocale(prevField,     fieldDescText.getText());
            if (nameChanged || descChanged)
            {
                setHasChanged(true);
            }
            
        }
    }
    
    /**
     * 
     */
    protected void next()
    {
        getFieldDataFromUI();
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
        getFieldDataFromUI();
        
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
        
        LocalizerContainerIFace tbl = (LocalizerContainerIFace)tablesList.getSelectedValue();
        if (tbl != null)
        {
            for (LocalizableNameDescIFace f : tbl.getItems())
            {
                fieldsModel.addElement(f);
            }

            fieldsList.setSelectedIndex(0);
        }
        updateBtns();
    }
    
    /**
     * 
     */
    protected void fieldSelected()
    {
        statusBar.setText("");
        
        LocalizableNameDescIFace fld  = (LocalizableNameDescIFace)fieldsList.getSelectedValue();
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
    public static void checkForLocales(final LocalizableNameDescIFace lndi, final Hashtable<String, String> localeHash)
    {
        for (Name nm : lndi.getNames())
        {
            localeHash.put(makeLocaleKey(nm.getLang(), nm.getCountry(), nm.getVariant()), "X");
        }
        for (Desc d : lndi.getDescs())
        {
            localeHash.put(makeLocaleKey(d.getLang(), d.getCountry(), d.getVariant()), "X");
        }
    }
    
    /**
     * @param locale
     * @return
     */
    public Vector<Locale> getLocalesInUse()
    {
        Hashtable<String, String> localeHash = new Hashtable<String, String>();
        for (LocalizerContainerIFace table : getTables())
        {
            checkForLocales(table, localeHash);
            for (LocalizableNameDescIFace f : table.getItems())
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
        for (LocalizerContainerIFace table : getTables())
        {
            checkForLocales(table, localeHash);
            for (LocalizableNameDescIFace f : table.getItems())
            {
                checkForLocales(f, localeHash);
            }
        }
        //for (String key : localeHash.keySet())
        //{
        //    System.out.println("In Use: "+key);
        //}
        return localeHash.get(makeLocaleKey(locale)) != null;
    }

    public void localeChanged(final String newLocaleName)
    {
        int tableInx = tablesList.getSelectedIndex();
        int fieldInx = fieldsList.getSelectedIndex();
        
        tablesList.getSelectionModel().clearSelection();
        
        currLocale = getLocaleByName(newLocaleName);
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
                }
            }
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
        tblSpellChkBtn.setEnabled(ok);
        fldSpellChkBtn.setEnabled(ok);
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
    
}
