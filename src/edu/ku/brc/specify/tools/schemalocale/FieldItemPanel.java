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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

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

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.PickListIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFormatterListEdtDlg;
import edu.ku.brc.af.ui.weblink.WebLinkConfigDlg;
import edu.ku.brc.af.ui.weblink.WebLinkDef;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 25, 2007
 *
 */
public class FieldItemPanel extends LocalizerBasePanel implements LocalizableIOIFaceListener
{
    private static final Logger log = Logger.getLogger(FieldItemPanel.class);
    
    protected final String SL_NONE     = getResourceString("NONE");
    protected final String SL_FORMAT   = getResourceString("SL_FORMAT");
    protected final String SL_WEBLINK  = getResourceString("SL_WEBLINK");
    protected final String SL_PICKLIST = getResourceString("SL_PICKLIST");

            
    protected LocalizableIOIFace        localizableIO = null;
    protected WebLinkMgr                webLinkMgrCache;
    
    
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
    protected JTextArea        fieldDescText = createTextArea();
    protected JTextField       fieldNameText = createTextField();
    
    protected JLabel           fieldDescLbl;
    protected JLabel           fieldNameLbl;
    protected JLabel           fieldTypeLbl;
    protected JLabel           fieldLengthLbl;
    protected JLabel           fieldTypeTxt;
    protected JLabel           fieldLengthTxt;
    protected JCheckBox        fieldReqChk;
    protected boolean          mustBeRequired;
    
    protected JLabel           formatLbl     = null;
    protected JComboBox        formatSwitcherCombo = null;
    protected JPanel           formatterPanel;
    protected CardLayout       fmtCardLayout;
    
    // Formatting
    protected JComboBox        formatCombo;
    protected JButton          formatMoreBtn;
    protected Hashtable<String, UIFieldFormatterIFace> formatHash = new Hashtable<String, UIFieldFormatterIFace>();
    protected UIFieldFormatterIFace selectedFormatter = null;
    
    // WebLinks
    protected JComboBox        webLinkCombo;
    protected JButton          webLinkMoreBtn;
    protected WebLinkDef       webLinkDefNone = new WebLinkDef(getResourceString("NONE"), null);
    
    // PickList
    protected JLabel           pickListLbl;
    protected JComboBox        pickListCBX;
    protected PickList         pickListNone = new PickList(getResourceString("NONE")); // I18N
    protected JButton          pickListMoreBtn;

    protected JLabel           autoNumLbl;
    protected JComboBox        autoNumberCombo;
    
    protected DefaultListModel fieldsModel   = new DefaultListModel();
    protected JButton          nxtBtn;
    protected JButton          nxtEmptyBtn;
    protected JCheckBox        fieldHideChk  = createCheckBox(getResourceString("SL_FIELD_HIDE_CHK"));

    protected LocalizableItemIFace prevField = null;
    
    protected JStatusBar       statusBar      = null;
    protected JButton          fldSpellChkBtn = null;
    
    protected PropertyChangeListener pcl   = null;
    
    protected List<PickList>   pickLists      = new Vector<PickList>();
    protected DisciplineType   disciplineType = null;
    protected String           disciplineName = null;
    
    /**
     * 
     */
    public FieldItemPanel(final SchemaLocalizerPanel schemaPanel,
                          final WebLinkMgr           webLinkMgrCache,
                          final boolean              includeHiddenUI, 
                          final boolean              includeFormatAndAutoNumUI, 
                          final boolean              isDBSchema,
                          final PropertyChangeListener pcl)
    {
        this.schemaPanel     = schemaPanel;
        this.webLinkMgrCache = webLinkMgrCache;
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
        
        fieldsList = createList(fieldsModel);
        
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
        // setting min and pref sizes to some bogus values so that textarea shrinks with dialog
        fieldDescText.setMinimumSize(new Dimension(50, 5));
        fieldDescText.setPreferredSize(new Dimension(50, 5));
        fieldDescText.setColumns(40);
        fieldNameText.addKeyListener(new LengthWatcher(64));

        CellConstraints cc = new CellConstraints();
        
        String descStr  = getResourceString("SL_DESC") + ":";
        String labelStr = getResourceString("SL_LABEL") + ":";

        int y = 1;
        
        JScrollPane fldsp = UIHelper.createScrollPane(fieldsList);
        
        // LocalizableNameDescIFace
        PanelBuilder pb = new PanelBuilder(new FormLayout("max(200px;p),4px,p,2px,p,10px,p,2px,p,f:p:g", 
                                                             (includeHiddenUI ? "p,2px," : "") + 
                                                             (isDBSchema ? "p,2px,p,2px," : "") + 
                                                             (includeFormatAndAutoNumUI ? "p,2px,p,2px," : "") + 
                                                             "p,2px,p,2px,p,2px,p,2px,p,2px,p,2px,f:p:g"), this);
        
        pb.add(fldsp, cc.xywh(1, y, 1, 7+(isDBSchema ? 4 : 0)));
        pb.add(fieldNameLbl = createLabel(labelStr, SwingConstants.RIGHT), cc.xy(3, y));
        pb.add(fieldNameText, cc.xywh(5, y, 6, 1));   y += 2;
        
        if (includeHiddenUI)
        {
            pb.add(fieldHideChk, cc.xy(5, y)); y += 2;
        }
       
        pb.add(fieldDescLbl = createLabel(descStr, SwingConstants.RIGHT), cc.xy(3, y));
        JScrollPane sp = new JScrollPane(fieldDescText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pb.add(sp,   cc.xywh(5, y, 6, 1));   y += 2;
        fieldDescText.setLineWrap(true);
        fieldDescText.setRows(4);
        fieldDescText.setWrapStyleWord(true);
        
        if (isDBSchema)
        {
            fieldTypeTxt   = createLabel("");
            fieldLengthTxt = createLabel("");
            
            pb.add(fieldTypeLbl   = createLabel(getResourceString("SL_TYPE") + ":", SwingConstants.RIGHT), cc.xy(3, y));
            pb.add(fieldTypeTxt,   cc.xy(5, y));  
            
            pb.add(fieldReqChk   = createCheckBox(getResourceString("SL_REQ")),   cc.xy(9, y)); y += 2;
            
            pb.add(fieldLengthLbl = createLabel(getResourceString("SL_LENGTH") + ":", SwingConstants.RIGHT), cc.xy(3, y));
            pb.add(fieldLengthTxt, cc.xy(5, y));   y += 2;
            
            fieldTypeTxt.setBackground(Color.WHITE);
            fieldLengthTxt.setBackground(Color.WHITE);
            fieldTypeTxt.setOpaque(true);
            fieldLengthTxt.setOpaque(true);
        }
        
        if (includeFormatAndAutoNumUI)
        {
            PanelBuilder inner = new PanelBuilder(new FormLayout("p,2px,p", "p"));
            
            formatSwitcherCombo = createComboBox();
            fmtCardLayout       = new CardLayout();
            formatterPanel      = new JPanel(fmtCardLayout);
            pb.add(formatLbl = createLabel(getResourceString("SL_FMTTYPE") + ":", SwingConstants.RIGHT), cc.xy(3, y));
            
            inner.add(formatSwitcherCombo, cc.xy(1,1));   
            inner.add(formatterPanel, cc.xy(3,1));
            
            pb.add(inner.getPanel(), cc.xywh(5, y, 6, 1));   y += 2;
            
            ActionListener switchAL = new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    String item = (String)formatSwitcherCombo.getSelectedItem();
                    if (item != null)
                    {
                        fmtCardLayout.show(formatterPanel, item);
                        webLinkCombo.setEnabled(item.equals(SL_WEBLINK));
                        
                        if (formatSwitcherCombo.getSelectedIndex() == 0)
                        {
                            if (webLinkCombo.getModel().getSize() > 0)
                            {
                                webLinkCombo.setSelectedIndex(0);
                            }
                            if (formatCombo.getModel().getSize() > 0)
                            {
                                formatCombo.setSelectedIndex(0);
                            }
                            if (pickListCBX.getModel().getSize() > 0)
                            {
                                pickListCBX.setSelectedIndex(0);
                            }
                        }
                    }
                }
            };
            formatSwitcherCombo.addActionListener(switchAL);
            
            formatterPanel.add(SL_NONE, new JPanel());

            //--------------------------
            // UIFieldFormatter
            //--------------------------
            inner = new PanelBuilder(new FormLayout("max(p;150px),2px,min", "p"));
            
            formatCombo   = createComboBox(new DefaultComboBoxModel());
            formatMoreBtn = createButton("...");
            
            inner.add(formatCombo,   cc.xy(1, 1));   
            inner.add(formatMoreBtn, cc.xy(3, 1));
            
            formatMoreBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    UIFormatterListEdtDlg dlg = new UIFormatterListEdtDlg((Frame)UIRegistry.getTopWindow(), 
                                                            fieldInfo,
                                                            schemaPanel.getUiFieldFormatterMgrCache());
                    dlg.setVisible(true);
                    if (dlg.isCancelled() && dlg.hasChanged())
                    {
                        schemaPanel.setHasChanged(true);
                    }
                }
            });
            
            formatterPanel.add(SL_FORMAT, inner.getPanel());
            
            ActionListener changed = new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    formHasChanged();
                        
                    boolean hasFormat = formatCombo.getSelectedIndex() > 0;
                    webLinkCombo.setEnabled(!hasFormat);
                    if (hasFormat)
                    {
                        webLinkCombo.setSelectedIndex(0);
                        pickListCBX.setSelectedIndex(0);
                    }
                }
            };
            formatCombo.addActionListener(changed);
            
            //--------------------------
            // WebLinks
            //--------------------------
            webLinkMoreBtn = createButton("...");
            webLinkMoreBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    WebLinkDef       selectedWL = (WebLinkDef)webLinkCombo.getSelectedItem();
                    WebLinkConfigDlg dlg        = webLinkMgrCache.editWebLinks(tableInfo, false);
                    if (dlg.getBtnPressed() == CustomDialog.OK_BTN)
                    {
                        fillWebLinkBox();
                        
                        formHasChanged();
                        
                        if (selectedWL != null && !selectedWL.getName().equals(SL_WEBLINK))
                        {
                            dlg.setWebLink(selectedWL.getName());
                        }
                        if (dlg.getBtnPressed() == CustomDialog.OK_BTN)
                        {
                            setSelectedWebLink(dlg.getSelectedItem());
                        }
                    }
                }
            });
            
            inner = new PanelBuilder(new FormLayout("max(p;150px),2px,min", "p"));
            webLinkCombo = createComboBox();
            DefaultComboBoxModel model = (DefaultComboBoxModel)webLinkCombo.getModel();
            model.addElement(webLinkDefNone);
            webLinkCombo.setSelectedIndex(0);
            
            inner.add(webLinkCombo,   cc.xy(1, 1));   
            inner.add(webLinkMoreBtn, cc.xy(3, 1));
            
            ActionListener wlchanged = new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    formHasChanged();
                    
                    boolean hasWL = webLinkCombo.getSelectedIndex() > 0;
                    webLinkCombo.setEnabled(hasWL);
                    if (hasWL)
                    {
                        formatCombo.setSelectedIndex(formatCombo.getModel().getSize() > 0 ? 0 : -1);
                        pickListCBX.setSelectedIndex(pickListCBX.getModel().getSize() > 0 ? 0 : -1);
                    }
                }
            };
            webLinkCombo.addActionListener(wlchanged);
            
            formatterPanel.add(SL_WEBLINK, inner.getPanel());
        }
        
        //--------------------------
        // PickList
        //--------------------------

        pickListCBX = createComboBox(new DefaultComboBoxModel());
        pickListCBX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                formHasChanged();
                if (formatCombo != null && pickListCBX.getSelectedIndex() > 0)
                {
                    formatCombo.setSelectedIndex(formatCombo.getModel().getSize() > 0 ? 0 : -1);
                    webLinkCombo.setSelectedIndex(webLinkCombo.getModel().getSize() > 0 ? 0 : -1);
                }
            }
        });
        pickListMoreBtn = createButton("...");
        pickListMoreBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                PickListEditorDlg dlg = new PickListEditorDlg(localizableIO.hasUpdatablePickLists() ? null : localizableIO);
                dlg.setVisible(true);
            }
        });
        
        if (includeFormatAndAutoNumUI)
        {
            PanelBuilder inner = new PanelBuilder(new FormLayout("max(p;150px),2px,min", "p"));
            inner.add(pickListCBX,   cc.xy(1, 1));   
            inner.add(pickListMoreBtn, cc.xy(3, 1));
            
            formatterPanel.add(SL_PICKLIST, inner.getPanel());
            
        } else
        {
            pb.add(pickListLbl   = createLabel(SL_PICKLIST + ":", SwingConstants.RIGHT), cc.xy(3, y));
            pb.add(pickListCBX,   cc.xy(5, y));
            pb.add(pickListMoreBtn,   cc.xy(7, y));   y += 2;
        }
        
        nxtBtn         = createButton(getResourceString("SL_NEXT"));
        nxtEmptyBtn    = createButton(getResourceString("SL_NEXT_EMPTY"));
        fldSpellChkBtn = createButton(getResourceString("SL_SPELL_CHECK"));
        
        JPanel bbp = ButtonBarFactory.buildCenteredBar(adjustButtonArray(new JButton[] {nxtEmptyBtn, nxtBtn, fldSpellChkBtn}));
        bbp.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        pb.add(bbp,   cc.xywh(3, y, 8, 1));
        
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
                formHasChanged();
            }
            
        });
        
        fieldReqChk.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                formHasChanged();
            }
            
        });
        
        DocumentListener dl = new DocumentListener() {
            public void changedUpdate(DocumentEvent e)
            {
                formHasChanged();
            }
            public void insertUpdate(DocumentEvent e)
            {
                formHasChanged();
            }
            public void removeUpdate(DocumentEvent e)
            {
                formHasChanged();
            }
        };
        
        fieldNameText.getDocument().addDocumentListener(dl);
        fieldDescText.getDocument().addDocumentListener(dl);
/*        
        if (formatTxt != null)
        {
            formatTxt.getDocument().addDocumentListener(dl);
        }
*/        
        SchemaI18NService.getInstance().checkCurrentLocaleMenu();
        
        enableUIControls(false);
        
        setIgnoreChanges(false);
    }
    
    /**
     * Sets the this panel as changed and the parent panel as changed.
     */
    protected void formHasChanged()
    {
        if (!isIgnoreChanges())
        {
            setHasChanged(true);
            schemaPanel.setHasChanged(true);
            schemaPanel.setTableInfoChanged(true);
        }
    }
    
    /**
     * @param item
     */
    protected void fillFormatSwticherCBX(final DBTableChildIFace item)
    {
        formatSwitcherCombo.removeAllItems();
        
        formatSwitcherCombo.addItem(SL_NONE);
        
        if (item instanceof DBRelationshipInfo)
        {
            if (((DBRelationshipInfo)item).getType() == DBRelationshipInfo.RelationshipType.ManyToOne)
            {
                formatSwitcherCombo.addItem(SL_PICKLIST);
            }
            
        } else
        {
            DBFieldInfo fi = (DBFieldInfo)item;
            if (fi.getDataClass() == String.class)
            {
                String ts      = fi.getType();
                String typeStr = ts.indexOf('.') > -1 ? StringUtils.substringAfterLast(fi.getType(), ".") : ts;
                if (StringUtils.isNotEmpty(typeStr) && !typeStr.equals("text"))
                {
                    formatSwitcherCombo.addItem(SL_FORMAT);
                    formatSwitcherCombo.addItem(SL_WEBLINK);
                    formatSwitcherCombo.addItem(SL_PICKLIST);
                }
            }
        }
        
        formatSwitcherCombo.setEnabled(formatSwitcherCombo.getModel().getSize() > 1);
    }

    /**
     * @param formatter
     */
    protected void setSelectedFieldFormatter(final UIFieldFormatterIFace formatter)
    {
    	LocalizableItemIFace fld = getSelectedFieldItem();
    	String oldFormat = fld.getFormat();
    	String newFormat = (formatter != null)? formatter.getName() : "";
    	fld.setFormat( newFormat );
    	
    	// first reset combo box in case any formatters have been deleted
    	fillWithFieldFormatter();
    	
		setHasChanged(newFormat.equals(oldFormat));
    	
    }
    
    /**
     * @param webLinkDef
     */
    protected void setSelectedWebLink(final WebLinkDef webLinkDef)
    {
        DefaultComboBoxModel model = (DefaultComboBoxModel)webLinkCombo.getModel();
        model.removeAllElements();
        
        model.addElement(webLinkDefNone);
        
        int fndInx = 0;
        int index  = 0;
        for (WebLinkDef wld : webLinkMgrCache.getWebLinkDefs(null))
        {
            model.addElement(wld);
            if (webLinkDef == wld)
            {
                setHasChanged(true);
                webLinkCombo.setSelectedIndex(index);
                fndInx = index;
                break;
            }
            index++;
        }   
        
        if (fndInx > 0)
        {
            webLinkCombo.setSelectedItem(webLinkDef);
            setHasChanged(true);
        }
        webLinkCombo.setEnabled(webLinkCombo.getModel().getSize() > 1);
    }
    
    /**
     * @param disciplineType the disciplineType to set
     */
    public void setDisciplineType(DisciplineType disciplineType)
    {
        this.disciplineType = disciplineType;
    }

    /**
     * Fills the format Combobox with the available formatters.
     */
    protected void fillWebLinkBox()
    {
        if (webLinkCombo != null)
        {
            DefaultComboBoxModel wlModel = (DefaultComboBoxModel)webLinkCombo.getModel();
            wlModel.removeAllElements();
            //if (wlModel.getSize() == 0)
            {
                wlModel.addElement(webLinkDefNone);
                for (WebLinkDef wld : webLinkMgrCache.getWebLinkDefs(null))
                {
                    wlModel.addElement(wld);
                }
            }
            
            //wbLnkCombo.setEnabled(false);
            webLinkMoreBtn.setEnabled(true);
            //wbLnkCombo.setEnabled(false);
            
            int selInx = webLinkCombo.getModel().getSize() > 0 ? 0 : -1;
            LocalizableItemIFace fld = getSelectedFieldItem();
            if (fld != null)
            {
                String webLinkName = fld.getWebLinkName();
                if (StringUtils.isNotEmpty(webLinkName))
                {
                    DefaultComboBoxModel model = (DefaultComboBoxModel)webLinkCombo.getModel();
                    for (int i=0;i<model.getSize();i++)
                    {
                        WebLinkDef wld = (WebLinkDef)model.getElementAt(i);
                        if (wld.getName().equals(webLinkName))
                        {
                            selInx = i;
                            break;
                        }
                    }
                }
            }
            webLinkCombo.setSelectedIndex(selInx);
        }
    }

    /**
     * Fills the format Combobox with the available formatters.
     */
    protected void fillFormatBox()
    {
        if (formatCombo != null)
        {
            ((DefaultComboBoxModel)formatCombo.getModel()).removeAllElements();
            
            formatCombo.setEnabled(false);
            formatMoreBtn.setEnabled(false);
            
            webLinkCombo.setEnabled(false);
            
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
            cbxModel.addElement(getResourceString("SL_PARTIAL_DATE"));
        } else
        {
            cbxModel.addElement(getResourceString("SL_DATE"));
        }
        formatCombo.setSelectedIndex(0);
        formatCombo.setEnabled(false);
        formatMoreBtn.setEnabled(false);
    }
    
    /**
     * @return
     */
    protected UIFieldFormatterIFace fillWithFieldFormatter()
    {
        formatHash.clear();
        DefaultComboBoxModel cbxModel = (DefaultComboBoxModel)formatCombo.getModel();
        cbxModel.removeAllElements();
        
        cbxModel.addElement(SL_NONE); // Add None
        
        if (fieldInfo.getType().equals("java.lang.String") ||
        	UIHelper.isClassNumeric(fieldInfo.getDataClass()))
        {
            formatCombo.setEnabled(true);
            formatMoreBtn.setEnabled(true);
        }
        
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
        List<UIFieldFormatterIFace> fList       = schemaPanel.getUiFieldFormatterMgrCache().getFormatterList(tableInfo.getClassObj(), fieldInfo.getName());
        // list must be sorted in the same way it's sorted on UIFormatterDlg because selection index is considered equivalent between combo boxes
        Collections.sort(fList, new Comparator<UIFieldFormatterIFace>() {
            public int compare(UIFieldFormatterIFace o1, UIFieldFormatterIFace o2)
            {
                return o1.toPattern().compareTo(o2.toPattern());
            }
        });
        
        if (fList != null && fList.size() > 0)
        {
            for (UIFieldFormatterIFace fmt : fList)
            {
                log.debug(fmt.getTitle());
                
                cbxModel.addElement(fmt);
                
                if (isUIFormatter && formatName != null && formatName.equals(fmt.getName()))
                {
                    selectedInx = cbxModel.getSize() - 1;
                    selectedFmt = fmt;
                }
            }
        }
        
        boolean hasFormat = selectedInx > 0;
        webLinkCombo.setEnabled(hasFormat);
        
        formatCombo.setSelectedIndex(selectedInx);
        
        if (hasFormat)
        {
            
        } else
        {
            
        }
        
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
        
        tableInfo = currContainer == null ? null : DBTableIdMgr.getInstance().getInfoByTableName(currContainer.getName());
        
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
        pickListMoreBtn.setEnabled(localizableIO.hasUpdatablePickLists());
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
        
        fieldReqChk.setEnabled(!mustBeRequired && enable);
        //log.debug("mustBeRequired: "+mustBeRequired+" !mustBeRequired && enable: "+(!mustBeRequired && enable));
        
        fieldLengthLbl.setEnabled(enable);
        fieldLengthTxt.setEnabled(enable);
        
        formatSwitcherCombo.setEnabled(enable);
        
        if (pickListLbl != null)
        {
            pickListLbl.setEnabled(enable);
        }
        pickListCBX.setEnabled(enable);
        
        if (formatLbl != null)
        {
            formatLbl.setEnabled(enable);
            formatCombo.setEnabled(enable);
            formatMoreBtn.setEnabled(enable);
            webLinkCombo.setEnabled(enable);
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
    protected void updateBtns()
    {
        int     inx     = fieldsList.getSelectedIndex();
        boolean enabled = inx != -1 && (inx < (fieldsModel.size() -1));
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
                    //f = localizableIO.realize(f);
                    LocalizableStrIFace  desc = getDescForCurrLocale(f);
                    if (desc == null || StringUtils.isEmpty(desc.getText()))
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
    public boolean getAllDataFromUI()
    {
        return getItemDataFromUI();
    }
    
    /**
     * 
     */
    protected boolean getItemDataFromUI()
    {
        boolean changed = hasChanged;
        
        if (prevField != null && changed)
        {
            prevField.setPickListName(null);
            prevField.setWebLinkName(null);
            prevField.setFormat(null);
            prevField.setIsUIFormatter(false);
            
            prevField.setIsRequired(fieldReqChk.isSelected());
            
            prevField.setIsHidden(fieldHideChk.isSelected());
            boolean nameChanged = setNameDescStrForCurrLocale(prevField, fieldNameText.getText());
            boolean descChanged = setDescStrForCurrLocale(prevField,     fieldDescText.getText());
            if (nameChanged || descChanged)
            {
                formHasChanged();
            }
            
            if (pickListCBX.getSelectedIndex() > 0)
            {
                PickList pl = (PickList)pickListCBX.getSelectedItem();
                prevField.setPickListName(pl != null ? pl.getName() : null);
                
            } else if (formatCombo != null && formatCombo.getSelectedIndex() > 0)
            {
                Object item = formatCombo.getSelectedItem();
                if (item != null) // should never be null
                {
                	// there is only one string in the list... the None string
                	// others are the formatter objects (not only strings with their names) 
                	boolean isNone = item instanceof String; 
                    if (!isNone)
                    {
                    	UIFieldFormatterIFace frmt = (UIFieldFormatterIFace) item;
                        prevField.setFormat(frmt.getName());
                        prevField.setIsUIFormatter(true);
                    
                    } else
                    {
                        prevField.setIsUIFormatter(false);
                        prevField.setFormat(null);
                    }
                } else
                {
                    log.error("We should never get here!");
                }
                    
            } else if (webLinkCombo != null && webLinkCombo.getSelectedIndex() > 0)
            {
                WebLinkDef wld = (WebLinkDef)webLinkCombo.getSelectedItem();
                prevField.setWebLinkName(wld.getName());
            }
            
            prevField = null;
        }
        hasChanged = false;
        
        return changed;
    }
    
    /**
     * 
     */
    protected void next()
    {
        if (getItemDataFromUI())
        {
            formHasChanged();
        }
        
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
        if (getItemDataFromUI())
        {
            formHasChanged();
        }
        
        int inx = getNextEmptyIndex(fieldsList.getSelectedIndex()+1);
        if (inx > -1)
        {
            fieldsList.setSelectedIndex(inx);
            fieldsList.ensureIndexIsVisible(inx);
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
            localizableIO.getContainer(currJListItem, this);
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
        if (fieldsList.getSelectedIndex() > -1)
        {
            if (currContainer != null)
            {
                LocalizableJListItem jlistFieldItem = (LocalizableJListItem)fieldsList.getSelectedValue();
                if (jlistFieldItem != null)
                {
                    return localizableIO.getItem(currContainer, jlistFieldItem);
                }
                log.error("fieldsList item was null");
            } else
            {
                log.error("currContainer was null");
            }
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
            fieldInfo = fld != null ? tableInfo.getFieldByName(fld.getName()) : null;
            relInfo   = fieldInfo == null ? tableInfo.getRelationshipByName(fld.getName()) : null;
            
            fillFormatSwticherCBX(tableInfo.getItemByName(fld.getName()));
            
            if (pcl != null)
            {
                pcl.propertyChange(new PropertyChangeEvent(fieldsList, "index", null, fld));
            }
            
            //fld = localizableIO.realize(fld);
            fieldDescText.setText(getDescStrForCurrLocale(fld));
            fieldNameText.setText(getNameDescStrForCurrLocale(fld));
            fieldHideChk.setSelected(fld.getIsHidden());
            
            String dspName = disciplineType != null ? disciplineType.getName() : null;
            if (AppContextMgr.getInstance().hasContext() && AppContextMgr.getInstance().getClassObject(Discipline.class) != null)
            {
                dspName = AppContextMgr.getInstance().getClassObject(Discipline.class).getName();
            }
            
            if (disciplineName == null || !dspName.equals(disciplineName))
            {
                disciplineName = dspName;
                
                pickLists.clear();
                List<PickList> plList = localizableIO.getPickLists(null);
                if (plList != null)
                {
                    pickLists.addAll(plList);
                }
                
                //for (PickList pl : pickLists) System.out.println("0: "+pl.getName());
                if (disciplineName != null)
                {
                    plList = localizableIO.getPickLists(disciplineName);
                    if (plList != null)
                    {
                        pickLists.addAll(plList);
                    }
                    //for (PickList pl : localizableIO.getPickLists(disciplineName)) System.out.println("1: "+pl.getName());
                }
                
                Collections.sort(pickLists);
            }
            
            if (pickLists != null)
            {
                DBRelationshipInfo.RelationshipType relType = relInfo != null ? relInfo.getType() : null;
                
                String  typeStr  = fieldInfo != null ? fieldInfo.getType() : null;
                boolean isString = typeStr != null && (typeStr.equals("java.lang.String") || typeStr.equals("string"));
                
                int selectedIndex = 0;
                DefaultComboBoxModel plCbxModel = (DefaultComboBoxModel)pickListCBX.getModel();
                
                if (isString)
                {
                    plCbxModel.removeAllElements();
                    plCbxModel.addElement(pickListNone);
                    
                    int inx = 1;
                    for (PickList pl : pickLists)
                    {
                        if (pl.getType() == PickListIFace.PL_WITH_ITEMS ||
                            pl.getType() == PickListIFace.PL_TABLE_FIELD)
                        {
                            plCbxModel.addElement(pl);
                            String plName = fld.getPickListName();
                            if (selectedIndex == 0 && StringUtils.isNotEmpty(plName) && plName.equals(pl.getName()))
                            {
                                selectedIndex = inx;
                            }
                            inx++;
                        }
                    }
                } else if (relType != null && relType == DBRelationshipInfo.RelationshipType.ManyToOne)
                {
                    plCbxModel.removeAllElements();
                    plCbxModel.addElement(pickListNone);
                    int inx = 0;
                    for (PickList pl : pickLists)
                    {
                        if (pl.getType() == PickListIFace.PL_WHOLE_TABLE)
                        {
                            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoByTableName(pl.getTableName());
                            if (relInfo.getDataClass() == tblInfo.getClassObj())
                            {
                                plCbxModel.addElement(pl);
                                String plName = fld.getPickListName();
                                //System.out.println(plName+"  "+pl.getName());
                                if (StringUtils.isNotEmpty(plName) && plName.equals(pl.getName()))
                                {
                                    selectedIndex = inx;
                                }
                                inx++;
                            }
                        }
                    }
                }
                pickListCBX.setEnabled(isString || relType != null);
                pickListCBX.setSelectedIndex(pickListCBX.getModel().getSize() > 0 ? selectedIndex : -1);
                
            } else
            {
                pickListCBX.setEnabled(false);
            }
            
            if (isDBSchema)
            {
                mustBeRequired = true;
                fieldReqChk.setSelected(false);
                
                DBTableInfo ti = DBTableIdMgr.getInstance().getInfoByTableName(currContainer.getName());
                if (ti != null)
                {
                    DBFieldInfo fi = ti.getFieldByName(fld.getName());
                    if (fi != null)
                    {
                        String ts      = fi.getType();
                        String typeStr = ts.indexOf('.') > -1 ? StringUtils.substringAfterLast(fi.getType(), ".") : ts;
                        if (typeStr.equals("Calendar"))
                        {
                            typeStr = "Date";
                        }
                        fieldTypeTxt.setText(typeStr);
                        
                        String lenStr = fi.getLength() != -1 ? Integer.toString(fi.getLength()) : " ";
                        fieldLengthTxt.setText(lenStr);
                        
                        fieldTypeLbl.setEnabled(true);
                        fieldTypeTxt.setEnabled(true);
                        
                        fieldLengthLbl.setEnabled(StringUtils.isNotEmpty(lenStr));
                        fieldLengthTxt.setEnabled(StringUtils.isNotEmpty(lenStr));
                        
                        mustBeRequired = fi.isRequiredInSchema();
                        fieldReqChk.setSelected(fi.isRequired());

                    } else
                    {
                        DBRelationshipInfo ri = ti.getRelationshipByName(fld.getName());
                        if (ri != null)
                        {
                            String title = ri.getType().toString();
                            if (ri.getType() == DBRelationshipInfo.RelationshipType.OneToMany)
                            {
                                title = DBRelationshipInfo.RelationshipType.OneToMany.toString();
                                
                            } else if (ri.getType() == DBRelationshipInfo.RelationshipType.ManyToOne)
                            {
                                title = DBRelationshipInfo.RelationshipType.ManyToOne.toString();
                            }
                            fieldTypeTxt.setText(title +" "+getResourceString("SL_TO")+" "+getNameDescStrForCurrLocale(currContainer));
                            fieldTypeLbl.setEnabled(true);
                            
                            fieldLengthTxt.setText(" ");
                            fieldLengthLbl.setEnabled(false);
                            fieldLengthTxt.setEnabled(false);
                            
                            fieldReqChk.setSelected(ri.isRequired());
                        } else
                        {
                            throw new RuntimeException("couldn't find field or relationship.");
                        }
                    }
                }
            }
            
            if (doAutoSpellCheck)
            {
                checker.spellCheck(fieldDescText);
                checker.spellCheck(fieldNameText);
            }
            
            enableUIControls(true);
            
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
        fillWebLinkBox();
        
        String label = SL_NONE;
        
        if (pickListCBX.getSelectedIndex() > 0)
        {
            label = SL_PICKLIST;
        }
        
        if (formatCombo.getSelectedIndex() > 0)
        {
            label = SL_FORMAT;
        }
        
        if (webLinkCombo.getSelectedIndex() > 0)
        {
            label = SL_WEBLINK;
        }
        
        formatSwitcherCombo.setSelectedItem(label);
        
        boolean ok = fld != null;
        fieldDescText.setEnabled(ok);
        fieldNameText.setEnabled(ok);
        fieldNameLbl.setEnabled(ok);
        fieldDescLbl.setEnabled(ok);
        
        setIgnoreChanges(ignoreChanges);

        prevField = fld;
        
        updateBtns();
        
        hasChanged = false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizerBasePanel#enableSpellCheck()
     */
    @Override
    protected void enableSpellCheck()
    {
        boolean ok = SchemaI18NService.getCurrentLocale().getLanguage().equals("en");
        LocalizableItemIFace fld = getSelectedFieldItem();
        fldSpellChkBtn.setEnabled(fld != null && ok && checker != null && spellCheckLoaded);
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
                    statusBar.setErrorMessage(getResourceString("SL_MAX_LENGTH_ERROR")+": "+maxLength);
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
    
    //--------------------------------------------------------------
    // LocalizableIOIFaceListener
    //--------------------------------------------------------------
    
 
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFaceListener#containterRetrieved(edu.ku.brc.specify.tools.schemalocale.LocalizableContainerIFace)
     */
    public void containterRetrieved(LocalizableContainerIFace container)
    {
        if (container != null)
        {
            for (LocalizableJListItem fItem : localizableIO.getDisplayItems(currJListItem))
            {
                fieldsModel.addElement(fItem);
            }

            fieldsList.setSelectedIndex(0);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFaceListener#realizeComplete(edu.ku.brc.specify.tools.schemalocale.LocalizableItemIFace)
     */
    public void realizeComplete(@SuppressWarnings("unused")LocalizableItemIFace liif)
    {
    }
    
    
}
