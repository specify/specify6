/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tools.schemalocale;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Locale;

import javax.swing.BorderFactory;
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

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.SpLocaleContainerItem;
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
public class SchemaLocalizerPanel extends LocalizerBasePanel implements PropertyChangeListener
{
    private static final Logger log = Logger.getLogger(SchemaLocalizerPanel.class);
            
    protected LocalizableIOIFace localizableIO = null;
    
    protected LocalizableContainerIFace currContainer   = null;
    protected boolean                   includeHiddenUI = true;              // Must be set before creatng the panel
    protected boolean                   isDBSchema      = true;
    protected boolean                   useDisciplines  = false;
    
    protected DisciplineBasedPanel      disciplineBasedPanel = null;

    // LocalizableContainerIFace Fields
    protected FieldItemPanel            fieldPanel;
    
    // LocalizableContainerIFace Tables
    protected JList                     tablesList;
    protected JTextArea                 tblDescText   = new JTextArea();
    protected JTextField                tblNameText   = createTextField();
    protected JLabel                    tblDescLbl;
    protected JLabel                    tblNameLbl;
    protected JCheckBox                 tblHideChk  = createCheckBox(getResourceString("SL_TABLE_HIDE_CHK"));
    protected boolean                   hasTableInfoChanged  = false;
    
    protected LocalizableItemIFace      prevTable = null;
    
    protected JStatusBar                statusBar      = null;
    protected JButton                   tblSpellChkBtn = null;
    
    protected Hashtable<String, String>         resHash     = new Hashtable<String, String>();
    protected Hashtable<String, PackageTracker> packageHash = new Hashtable<String, PackageTracker>();
    protected Hashtable<String, Boolean>        nameHash    = new Hashtable<String, Boolean>();
    
    protected PropertyChangeListener            listener    = null;
    

    /**
     * 
     */
    public SchemaLocalizerPanel(final PropertyChangeListener l)
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
        
        CellConstraints cc = new CellConstraints();
        
        // LocalizableContainerIFace Section Layout
        tblSpellChkBtn               = createButton("Spell Check");
        JPanel      tpbbp            = ButtonBarFactory.buildCenteredBar(new JButton[] {tblSpellChkBtn});
        JScrollPane sp               = new JScrollPane(tblDescText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tblDescText.setRows(4);
        tblDescText.setLineWrap(true);
        tblDescText.setWrapStyleWord(true);
        tblDescText.addKeyListener(new LengthWatcher(255));
        tblNameText.addKeyListener(new LengthWatcher(64));
        
        String descStr  = getResourceString("SL_DESC") + ":";
        String nameStr  = getResourceString("SL_NAME") + ":";

        int y = 1;
        PanelBuilder topInner   = new PanelBuilder(new FormLayout("p,2px,f:p:g", 
                                                                  "p,2px," + (includeHiddenUI ? "p,2px," : "") + "p,2px,p" +
                                                                  (useDisciplines ? ",2px,p" : "")
                                                                  ));
        
        topInner.add(tblDescLbl = createLabel(nameStr, SwingConstants.RIGHT), cc.xy(1, y));
        topInner.add(tblNameText, cc.xy(3, y)); y += 2;
        
        if (includeHiddenUI)
        {
            topInner.add(tblHideChk, cc.xy(3, y)); y += 2;
        }
        
        topInner.add(tblNameLbl = createLabel(descStr, SwingConstants.RIGHT), cc.xy(1, y)); 
        topInner.add(sp,    cc.xy(3, y));   y += 2;

        topInner.add(tpbbp, cc.xywh(1, y, 3, 1));

        
        JScrollPane tblsp = new JScrollPane(tablesList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        // LocalizableNameDescIFace
        fieldPanel = new FieldItemPanel(this, includeHiddenUI, true, isDBSchema, this);
        fieldPanel.setStatusBar(statusBar);
        fieldPanel.setLocalizableIO(localizableIO);
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("max(200px;p),4px,f:p:g", 
                                                          "p,4px,t:p,4px,p,4px,f:p:g,4px,p,4px,p"), this);
        pb.addSeparator(getResourceString("SL_TABLES"),   cc.xywh(1, 1, 3, 1));
        pb.add(tblsp,               cc.xy  (1, 3));
        pb.add(topInner.getPanel(), cc.xy  (3, 3));
        pb.addSeparator(getResourceString("SL_FIELDS"),   cc.xywh(1, 5, 3, 1));
        pb.add(fieldPanel,    cc.xywh(1, 7, 3, 1));
        
        if (useDisciplines)
        {
            disciplineBasedPanel = new DisciplineBasedPanel(this);
            pb.add(disciplineBasedPanel,    cc.xywh(1, 9, 3, 1));
        }

        pb.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
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
        
        SchemaI18NService.getInstance().checkCurrentLocaleMenu();
        
        enableUIControls(false);
        
        setIgnoreChanges(false);
    }
    
    /**
     * @return the statusBar
     */
    public JStatusBar getStatusBar()
    {
        return statusBar;
    }

    /**
     * @param includeHiddenUI tells it to include the checkboxes for hiding tables and fields
     */
    public void setIncludeHiddenUI(boolean includeHiddenUI)
    {
        this.includeHiddenUI = includeHiddenUI;
        
        // XXX For Now
        useDisciplines = this.includeHiddenUI;
    }

    /**
     * @param useDisciplines the useDisciplines to set
     */
    public void setUseDisciplines(boolean useDisciplines)
    {
        this.useDisciplines = useDisciplines;
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
        tblDescText.setEnabled(enable);
        tblNameText.setEnabled(enable);
        
        tblNameLbl.setEnabled(enable);
        tblDescLbl.setEnabled(enable);
        
        tblHideChk.setEnabled(enable);
        
        if (!enable)
        {
            tblSpellChkBtn.setEnabled(false);
            
        } else
        {
            enableSpellCheck();
        }
    }
    
    /**
     * @return the current container
     */
    public LocalizableContainerIFace getCurrentContainer()
    {
        return currContainer;
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
                    
                    
                    if (disciplineBasedPanel != null && currContainer instanceof DisciplineBasedContainer)
                    {
                        disciplineBasedPanel.set((DisciplineBasedContainer)currContainer, jlistItem);
                    }
                    fieldPanel.setContainer(currContainer, jlistItem);

                } else
                {
                    tblDescText.setText("");
                    tblNameText.setText("");
                    
                    fieldPanel.setContainer(null, null);
                    disciplineBasedPanel.set((DisciplineBasedContainer)null, jlistItem);
                    
                    tblHideChk.setSelected(false);
                }

                prevTable = currContainer;
                
            } else
            {
                fieldPanel.setContainer(null, null);
                disciplineBasedPanel.set((DisciplineBasedContainer)null, null);
                
                log.error("jlistItem was null in list");
            }
            enableUIControls(currContainer != null);
        }
        
        hasTableInfoChanged = false;
        setIgnoreChanges(false);
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
        
        fieldPanel.setEnabled(false);
        
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
                fieldPanel.setEnabled(true);
                
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
    protected void getAllDataFromUI()
    {
        getContainerDataFromUI();
        
        fieldPanel.getItemDataFromUI();
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
            
            if (disciplineBasedPanel != null)
            {
                disciplineBasedPanel.getDataFromUI();
            }
            prevTable = null;
        }
        hasTableInfoChanged = false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizerBasePanel#localeChanged(java.lang.String)
     */
    public void localeChanged(final Locale newLocale)
    {
        int tableInx = tablesList.getSelectedIndex();
        int fieldInx = fieldPanel.getSelectedIndex();
        
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
            fieldPanel.setSelectedIndex(fieldInx);
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
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (disciplineBasedPanel != null)
        {
            disciplineBasedPanel.setItem((SpLocaleContainerItem)evt.getNewValue());
        }
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
