/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tools.schemalocale;

import static edu.ku.brc.ui.UIHelper.adjustButtonArray;
import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createList;
import static edu.ku.brc.ui.UIHelper.createTextArea;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.SpLocaleContainerItem;
import edu.ku.brc.specify.tools.schemalocale.LocalizerApp.PackageTracker;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.formatters.DataObjAggregator;
import edu.ku.brc.ui.forms.formatters.DataObjAggregatorDlg;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatDlg;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.ui.forms.formatters.DataObjSwitchFormatter;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.util.ComparatorByStringRepresentation;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 25, 2007
 *
 */
public class SchemaLocalizerPanel extends LocalizerBasePanel implements PropertyChangeListener,
                                                                        LocalizableIOIFaceListener
{
    private static final Logger log = Logger.getLogger(SchemaLocalizerPanel.class);
    
    protected LocalizableIOIFace        localizableIO   = null;
    
    protected DBTableInfo               tableInfo       = null;
    
    protected LocalizableContainerIFace currContainer   = null;
    protected boolean                   includeHiddenUI = true;              // Must be set before creatng the panel
    protected boolean                   isDBSchema      = true;
    protected boolean                   useDisciplines  = false;
    
    protected DisciplineBasedPanel      disciplineBasedPanel = null;

    // used to hold changes to formatters before these are committed to DB
    protected DataObjFieldFormatMgr 	dataObjFieldFormatMgrCache;
    protected UIFieldFormatterMgr		uiFieldFormatterMgrCache;
    
    // LocalizableContainerIFace Fields
    protected FieldItemPanel            fieldPanel;
    
    // LocalizableContainerIFace Tables
    protected JList                     tablesList;
    protected JTextArea                 tblDescText   = createTextArea();
    protected JTextField                tblNameText   = createTextField();
    protected JLabel                    tblDescLbl;
    protected JLabel                    tblNameLbl;
    protected JCheckBox                 tblHideChk  = createCheckBox(getResourceString("SL_TABLE_HIDE_CHK"));
    protected boolean                   hasTableInfoChanged  = false;
    
    protected LocalizableItemIFace      prevTable = null;
    
    protected JStatusBar                statusBar      = null;
    protected JButton                   tblSpellChkBtn = null;
    
    // data obj formatter and aggregator controls
    protected JLabel                    dataObjFmtLbl = null;
    protected JLabel 					aggregatorLbl = null;
    protected JComboBox					dataObjFmtCbo = null;
    protected JComboBox					aggregatorCbo = null;
    protected JButton					dataObjFmtBtn = null;
    protected JButton					aggregatorBtn = null;
    
    protected Hashtable<String, String>         resHash     = new Hashtable<String, String>();
    protected Hashtable<String, PackageTracker> packageHash = new Hashtable<String, PackageTracker>();
    protected Hashtable<String, Boolean>        nameHash    = new Hashtable<String, Boolean>();
    
    protected PropertyChangeListener            listener    = null;
    

    /**
     * 
     */
    public SchemaLocalizerPanel(final PropertyChangeListener l, 
                                final DataObjFieldFormatMgr  dataObjFieldFormatMgrCache,
                                final UIFieldFormatterMgr    uiFieldFormatterMgrCache)
    {
        listener = l;
        this.dataObjFieldFormatMgrCache = dataObjFieldFormatMgrCache;
        this.uiFieldFormatterMgrCache   = uiFieldFormatterMgrCache;
        init();
    }

    /**
     * 
     */
    public void buildUI()
    {
        setIgnoreChanges(true);
        
        tablesList = createList(localizableIO.getContainerDisplayItems());
        
        tablesList.setVisibleRowCount(14);
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
        tblSpellChkBtn               = createButton(getResourceString("SL_SPELL_CHECK"));
        JPanel      tpbbp            = ButtonBarFactory.buildCenteredBar(adjustButtonArray(new JButton[] {tblSpellChkBtn}));
        JScrollPane sp               = new JScrollPane(tblDescText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tblDescText.setRows(4);
        tblDescText.setLineWrap(true);
        tblDescText.setWrapStyleWord(true);
        // setting min and pref sizes to some bogus values so that textarea shrinks with dialog
        tblDescText.setMinimumSize(new Dimension(50, 5));
        tblDescText.setPreferredSize(new Dimension(50, 5));
        tblDescText.addKeyListener(new LengthWatcher(255));
        tblNameText.addKeyListener(new LengthWatcher(64));
        
        // data obj formatter control
        dataObjFmtLbl = createLabel(getResourceString("DOF_DISPLAY_FORMAT")+":", SwingConstants.RIGHT);
        dataObjFmtCbo = createComboBox();
        dataObjFmtBtn = createButton("...");
        fillFormatterCombo();
        addFormatterActionListener();

        // aggregator controls
        aggregatorLbl = createLabel(getResourceString("DOA_AGGREGATION")+":", SwingConstants.RIGHT);
        aggregatorCbo = createComboBox();
        aggregatorBtn = createButton("...");
        fillAggregatorCombo();
        addAggregatorActionListener();
        
        
        String descStr  = getResourceString("SL_DESC") + ":";
        String nameStr  = getResourceString("SL_NAME") + ":";
        
        int y = 1;
        PanelBuilder topInner   = new PanelBuilder(new FormLayout("p,2px,f:p:g",
                                                                  "p,2px," + (includeHiddenUI ? "p,2px," : "") + "p,2px,p" +
                                                                  (useDisciplines ? ",2px,p" : "") +
                                                                  ",6px,p,2px,p"  // formatter & aggregator panel
                                                                  ));
        
        topInner.add(tblDescLbl = createLabel(nameStr, SwingConstants.RIGHT), cc.xy(1, y));
        topInner.add(tblNameText, cc.xy(3, y)); y += 2;
        
        if (includeHiddenUI)
        {
            topInner.add(tblHideChk, cc.xy(3, y)); y += 2;
        }
        
        topInner.add(tblNameLbl = createLabel(descStr, SwingConstants.RIGHT), cc.xy(1, y)); 
        topInner.add(sp,    cc.xy(3, y));   y += 2;

        topInner.add(tpbbp, cc.xywh(1, y, 3, 1)); y += 2;

        // formatter panel
        PanelBuilder fmtPanel = new PanelBuilder(new FormLayout("200px,r:m", "p"));
        fmtPanel.add(dataObjFmtCbo, cc.xy(1, 1));
        fmtPanel.add(dataObjFmtBtn, cc.xy(2, 1));

        // aggregator panel
        PanelBuilder aggPanel = new PanelBuilder(new FormLayout("200px,r:m", "p"));
        aggPanel.add(aggregatorCbo, cc.xy(1, 1));
        aggPanel.add(aggregatorBtn, cc.xy(2, 1));

        topInner.add(dataObjFmtLbl, cc.xy(1, y)); 
        topInner.add(fmtPanel.getPanel(), cc.xy(3, y)); y += 2;
        topInner.add(aggregatorLbl,    cc.xy(1, y)); 
        topInner.add(aggPanel.getPanel(), cc.xy(3, y)); y += 2;
        
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
                setTableInfoChanged(true);
            }
        });

        ActionListener comboAL = new ActionListener() 
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		setHasChanged(true);
                setTableInfoChanged(true);
        	}
        };
        
        dataObjFmtCbo.addActionListener(comboAL);
        aggregatorCbo.addActionListener(comboAL);
        
        DocumentListener dl = new DocumentListener() {
            protected void changed()
            {
                if (!hasTableInfoChanged)
                {
                    setTableInfoChanged(true);
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
        
        //tablesList.setEnabled(false);
        
        SchemaI18NService.getInstance().checkCurrentLocaleMenu();
        
        enableUIControls(false);
        
        setIgnoreChanges(false);
    }
    

    /**
     * @param ignoreChanges
     */
    public void setTableInfoChanged(boolean hasChanged)
    {
        if (!isIgnoreChanges())
        {
            this.hasTableInfoChanged = hasChanged;
        }
    }
    
    /**
     * 
     */
    private void fillFormatterCombo()
    {
        List<DataObjSwitchFormatter> fList;
    	if (tableInfo != null)
    	{
            fList = dataObjFieldFormatMgrCache.getFormatterList(tableInfo.getClassObj());
            // list must be sorted in the same way it's sorted on UIFormatterDlg because selection index is considered equivalent between combo boxes
            Collections.sort(fList, new ComparatorByStringRepresentation<DataObjSwitchFormatter>()); 
    	}
    	else 
    	{
    		fList = new Vector<DataObjSwitchFormatter>(0);
    	}

    	DefaultComboBoxModel model = (DefaultComboBoxModel) dataObjFmtCbo.getModel();
    	model.removeAllElements();

    	if (currContainer == null)
    		return;
    	
    	String fmtName = currContainer.getAggregator();

    	int selectedInx = -1;
        for (DataObjSwitchFormatter format : fList)
        {
        	model.addElement(format);

        	if (fmtName != null && fmtName.equals(format.getName()))
        	{
        		selectedInx = model.getSize() - 1;
        	}
        }
        
        // select format from list that is currently assigned to table
        dataObjFmtCbo.setSelectedIndex(selectedInx);
    }
    
    /**
     * 
     */
    private void fillAggregatorCombo()
    {
    	List<DataObjAggregator> fList;
    	if (tableInfo != null)
    	{
            fList = dataObjFieldFormatMgrCache.getAggregatorList(tableInfo.getClassObj());
            // list must be sorted in the same way it's sorted on UIFormatterDlg because selection index is considered equivalent between combo boxes
            Collections.sort(fList, new ComparatorByStringRepresentation<DataObjAggregator>()); 
    	}
    	else 
    	{
    		fList = new Vector<DataObjAggregator>(0);
    	}
        
        DefaultComboBoxModel model = (DefaultComboBoxModel) aggregatorCbo.getModel();
    	model.removeAllElements();

    	if (currContainer == null) return;
    	
        String aggName = currContainer.getAggregator();

        int selectedInx = -1;
        for (DataObjAggregator aggregator : fList)
        {
        	model.addElement(aggregator);
        	
        	if (aggName != null && aggName.equals(aggregator.getName()))
        	{
        		selectedInx = model.getSize() - 1;
        	}
        }
        
        // select format from list that is currently assigned to table
        aggregatorCbo.setSelectedIndex(selectedInx);
    }

    /**
     * Creates and adds action listeners for data obj formatter ellipsis button
     */
    private void addFormatterActionListener()
    {
        ActionListener dataObjFmtBtnAL = new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
    			Frame frame = (Frame)UIRegistry.getTopWindow(); 
        		DataObjFieldFormatDlg dlg = new DataObjFieldFormatDlg(frame, 
        				tableInfo, dataObjFmtCbo.getSelectedIndex(), 
        				dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache);
        		dlg.setVisible(true);
        		
        		// set combo selection to formatter selected in dialog
        		if (dlg.getBtnPressed() == CustomDialog.OK_BTN)
        		{
        			DataObjSwitchFormatter format = dlg.getSelectedFormatter();
        			
        			// assign selected obj formatter as the default for current table
        			currContainer.setFormat(format.getName());
        			currContainer.setIsUIFormatter(false);
        			
	        		// fill combo again, adding new formatter if it was new and selecting the appropriate one
        			fillFormatterCombo();
        		}
        	}
        };
        dataObjFmtBtn.addActionListener(dataObjFmtBtnAL);
    }
    
    /**
     * Creates and adds action listener for aggregator ellipsis button
     */
    private void addAggregatorActionListener()
    {
        ActionListener aggregatorBtnAL = new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
    			Frame frame = (Frame)UIRegistry.getTopWindow(); 
    			DataObjAggregatorDlg dlg = new DataObjAggregatorDlg(frame, tableInfo, 
    					aggregatorCbo.getSelectedIndex(), 
    					dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache);
        		dlg.setVisible(true);
        		
        		// set combo selection to formatter selected in dialog
        		if (dlg.getBtnPressed() == CustomDialog.OK_BTN)
        		{
        			DataObjAggregator agg = dlg.getSelectedAggregator();
        			
        			// TODO: assign selected aggregator as the default for current table
        			currContainer.setAggregator(agg.getName());
        			
	        		// fill combo again, adding new formatter if it was new and selecting the appropriate one 
        			fillAggregatorCombo();
        		}
        	}
        };
        aggregatorBtn.addActionListener(aggregatorBtnAL);
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

        // formatter and aggregator controls
        dataObjFmtLbl.setEnabled(enable);
        dataObjFmtCbo.setEnabled(enable);
        dataObjFmtBtn.setEnabled(enable);
        aggregatorLbl.setEnabled(enable);
        aggregatorCbo.setEnabled(enable);
        aggregatorBtn.setEnabled(enable);
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
        if (hasTableInfoChanged)
        {
            localizableIO.containerChanged(currContainer);
        }
        
        log.debug("Changed " + hasTableInfoChanged+" " + (currContainer != null ? currContainer.getName() : "null"));
        getAllDataFromUI();
       
        setIgnoreChanges(true);
       
        LocalizableJListItem jlistItem = (LocalizableJListItem)tablesList.getSelectedValue();
        if (jlistItem != null)
        {
            localizableIO.getContainer(jlistItem, this);
        }
        setIgnoreChanges(false);
        setTableInfoChanged(false);
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
        UIRegistry.getStatusBar().setIndeterminate(getClass().getSimpleName(), true);
        
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
                UIRegistry.getStatusBar().setProgressDone(getClass().getSimpleName());
            }
        };
        
        // start the background task
        workerThread.start();
    }

    /**
     * @param tableName
     * @return
     */
    /*public String getContainerDescStr(final LocalizableJListItem listItem)
    {
        LocalizableContainerIFace table = localizableIO.getContainer(listItem);
        if (table != null)
        {
           return getDescStrForCurrLocale(table);
        }
        log.error("Couldn't find table ["+listItem.getName()+"]");
        return null;
    }*/
    
    /**
     * @param tableName
     * @return
     */
    /*public LocalizableStrIFace getContainerDesc(final LocalizableJListItem listItem)
    {
        LocalizableContainerIFace table = localizableIO.getContainer(listItem);
        if (table != null)
        {
           return getDescForCurrLocale(table);
        }
        log.error("Couldn't find table ["+listItem.getName()+"]");
        return null;
    }*/
    
    /**
     * @param tableName
     * @return
     */
    /*public LocalizableStrIFace getContainerNameDesc(final LocalizableJListItem listItem)
    {
        LocalizableContainerIFace table = localizableIO.getContainer(listItem);
        if (table != null)
        {
           return getNameDescForCurrLocale(table);
        }
        log.error("Couldn't find table ["+listItem.getName()+"]");
        return null;
    }*/
    
    /**
     * @param tableName
     * @return
     */
    /*public LocalizableStrIFace getItemDesc(final LocalizableJListItem tableListItem, 
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
    }*/
    
    /**
     * @param tableName
     * @return
     */
    /*public LocalizableStrIFace getItemNameDesc(final LocalizableJListItem tableListItem, 
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
    }*/
    
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
        setTableInfoChanged(false);
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
                statusBar.setErrorMessage(getResourceString("SL_MAX_LENGTH_ERROR")+": "+maxLength);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        tc.setText(tc.getText().substring(0, maxLength));
                    }
                });
                
            }
        }
    }



	public UIFieldFormatterMgr getUiFieldFormatterMgrCache()
	{
		return uiFieldFormatterMgrCache;
	}

	//--------------------------------------------------------------
	// LocalizableIOIFaceListener
    //--------------------------------------------------------------
	
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFaceListener#containterRetrieved(edu.ku.brc.specify.tools.schemalocale.LocalizableContainerIFace)
     */
    public void containterRetrieved(LocalizableContainerIFace container)
    {
        LocalizableJListItem jlistItem = (LocalizableJListItem)tablesList.getSelectedValue();
        
        currContainer = container;
        
        if (currContainer != null)
        {
            tableInfo = DBTableIdMgr.getInstance().getInfoByTableName(currContainer.getName());
            if (currContainer != null)
            {
                
                if (currContainer != null)
                {
                    tblDescText.setText(getDescStrForCurrLocale(currContainer));
                    tblNameText.setText(getNameDescStrForCurrLocale(currContainer));
                    tblHideChk.setSelected(currContainer.getIsHidden());
                    
                    fillFormatterCombo();
                    fillAggregatorCombo();
                    
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
        } else
        {
            UIRegistry.showError("Couldn't load container["+jlistItem.getId()+"]");
        }

    }

}
