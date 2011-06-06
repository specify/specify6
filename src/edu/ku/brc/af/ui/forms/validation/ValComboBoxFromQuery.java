/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.af.ui.forms.validation;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.split;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.af.ui.ViewBasedDialogFactoryIFace;
import edu.ku.brc.af.ui.db.JComboBoxFromQuery;
import edu.ku.brc.af.ui.db.TextFieldWithQuery;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace;
import edu.ku.brc.af.ui.db.TextFieldWithQuery.ExternalQueryProviderIFace;
import edu.ku.brc.af.ui.forms.DataGetterForObj;
import edu.ku.brc.af.ui.forms.DataObjectSettable;
import edu.ku.brc.af.ui.forms.DataObjectSettableFactory;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.SessionListenerIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.persist.FormDevHelper;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;


/**
 * This is a Validated Auto Complete combobox that is filled from a database table. It implements GetSetValueFace
 * and the set and get methods expect (and return) the Hibernate Object for the the table. When the user types
 * into the editable combobox it performs a case insensitive search against a single field. The display can be
 * constructed from multiple columns in the database. It is highly recommended that the first column be the same column
 * that is being searched. It is is unclear whether showing more columns than they can search on is a problem, this may
 * need to be addressed laster.<br><br>
 * The search looks like this:<br>
 * select distinct lastName,firstName,AgentID from agent where lower(lastName) like 's%' order by lastName asc

 * @code_status Complete
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValComboBoxFromQuery extends JPanel implements UIValidatable,
                                                            ListSelectionListener,
                                                            GetSetValueIFace,
                                                            AppPrefsChangeListener,
                                                            SessionListenerIFace
{
    protected static final Logger log = Logger.getLogger(ValComboBoxFromQuery.class);

    public static final int CREATE_EDIT_BTN   =  1;
    public static final int CREATE_NEW_BTN    =  2;
    public static final int CREATE_SEARCH_BTN =  4;
    public static final int CREATE_CLONE_BTN  =  8;
    public static final int CREATE_VIEW_BTN   = 16;
    public static final int CREATE_ALL        = 31;
    
    protected enum MODE {Unknown, Editting, NewAndEmpty, NewAndNotEmpty}

    protected static ColorWrapper valTextColor       = null;
    protected static ColorWrapper requiredFieldColor = null;

    protected UIValidatable.ErrorType valState  = UIValidatable.ErrorType.Valid;

    protected String             cellName   = null;
    protected boolean            isRequired = false;
    protected boolean            isChanged  = false;
    protected boolean            isNew      = false;
    protected boolean            hasBeenVisited = false;
    protected Color              bgColor        = null;
    protected boolean            doAdjustQuery  = true;

    protected TextFieldWithQuery textWithQuery;
    protected JButton            searchBtn  = null;
    protected JButton            createBtn  = null;
    protected JButton            editBtn    = null;
    protected JButton            cloneBtn   = null;
    protected DBTableInfo        tableInfo;
    protected String             frameTitle = null;
    protected String             keyName;
    protected String             dataObjFormatterName;
    protected DataGetterForObj   getter   = null;
    protected String[]           fieldNames;
    protected String             helpContext;

    protected FormDataObjIFace   dataObj     = null;
    protected FormDataObjIFace   newDataObj  = null;
    protected MODE               currentMode = MODE.Unknown;
    protected boolean            hasFocus    = false;
    protected boolean            isRestricted;
    protected String             restrictedStr;

    protected ViewBasedDisplayIFace frame          = null;
    protected MultiView             multiView      = null;
    protected String                searchDlgName  = null;  // Overrides what is in the TableInfo
    protected String                displayDlgName = null;  // Overrides what is in the TableInfo

    protected List<FocusListener>   focusListeners = new ArrayList<FocusListener>();
    protected Vector<ListSelectionListener> listSelectionListeners = null;
    
    protected ActionListener defaultSearchAction;
    protected ActionListener defaultEditAction;
    protected ActionListener defaultNewAction;
    protected ActionListener defaultCloneAction;
    
    protected ViewBasedSearchQueryBuilderIFace builder = null;

    protected DataProviderSessionIFace         session;
    
    /**
     * Constructor.
     * @param tableInfo
     * @param keyFieldName
     * @param displayColumn
     * @param keyName
     * @param format
     * @param uiFieldFormatterName
     * @param dataObjFormatterName
     * @param sqlTemplate
     * @param helpContext
     * @param btns
     */
    public ValComboBoxFromQuery(final DBTableInfo tableInfo,
                                final String      keyFieldName,
                                final String      displayColumn,
                                final String      keyName,
                                final String      format,
                                final String      uiFieldFormatterName,
                                final String      dataObjFormatterName,
                                final String      sqlTemplate,
                                final String      helpContext,
                                final int         btns)
    
    {
        if (StringUtils.isEmpty(displayColumn))
        {
            FormDevHelper.showFormDevError("For ValComboBoxFromQuery table["+tableInfo.getName()+"] displayColumn null.");
            return;
        }
        if (StringUtils.isEmpty(format) && StringUtils.isEmpty(uiFieldFormatterName))
        {
            FormDevHelper.showFormDevError("For ValComboBoxFromQuery table["+tableInfo.getName()+"] both format and fieldFormatterName are null.");
            return;
        }
        if (StringUtils.isEmpty(tableInfo.getNewObjDialog()))
        {
            FormDevHelper.showFormDevError("For ValComboBoxFromQuery table["+tableInfo.getName()+"]  New Obj Dialog name (displayInfoDialogName) is null.");
            return;
        }
        
        this.tableInfo             = tableInfo;
        this.keyName               = keyName;
        this.dataObjFormatterName  = dataObjFormatterName != null ? dataObjFormatterName : tableInfo.getDataObjFormatter();
        this.frameTitle            = tableInfo.getTitle();
        this.helpContext           = helpContext;
        
        textWithQuery = new TextFieldWithQuery(tableInfo, 
                                               keyFieldName, 
                                               displayColumn, 
                                               format, 
                                               uiFieldFormatterName,
                                               sqlTemplate);
        restrictedStr = FormHelper.checkForRestrictedValue(tableInfo);
        if (restrictedStr != null)
        {
            isRestricted = true;
            textWithQuery.setEnabled(false);
            
        } else
        {
        
            textWithQuery.addListSelectionListener(this);
            textWithQuery.setAddAddItem(true);
            
            textWithQuery.getTextField().addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e)
                {
                    //log.debug("focusGained");
                    hasFocus = true;
                    super.focusGained(e);
                    validateState();
                    repaint();
                    
                    for (FocusListener l : focusListeners)
                    {
                        l.focusGained(e);
                    }
                }
    
                @Override
                public void focusLost(FocusEvent e)
                {
                    //log.debug("focusLost");
                    hasFocus = false;
                    super.focusLost(e);
                    
                    validateState();
                    repaint();
                    
                    for (FocusListener l : focusListeners)
                    {
                        l.focusLost(e);
                    }
                }
            });
        }
        
        init(tableInfo.getTitle(), btns);
        
        setOpaque(false);
    }
    
    /**
     * @param isReadOnlyMode the isReadOnlyMode to set
     */
    public void setReadOnlyMode()
    {
        if (textWithQuery != null)
        {
            textWithQuery.setReadOnlyMode();
        }
        if (editBtn != null)
        {
            editBtn.setVisible(false);
        }
    }
    
    /**
     * @param doAdjustQuery
     */
    public void setDoAdjustQuery(boolean doAdjustQuery)
    {
        this.doAdjustQuery = doAdjustQuery;
        if (textWithQuery != null)
        {
            textWithQuery.setDoAdjustQuery(doAdjustQuery);
        }
    }
    
    /**
     * @param sqlTemplate the sqlTemplate to set
     */
    public void setSqlTemplate(String sqlTemplate)
    {
        textWithQuery.setSqlTemplate(sqlTemplate);
    }
    
    /**
     * @param externalQueryProvider
     */
    public void setExternalQueryProvider(ExternalQueryProviderIFace externalQueryProvider)
    {
        if (textWithQuery != null)
        {
            textWithQuery.setExternalQueryProvider(externalQueryProvider);
        }
    }
    
    /**
     * @return the cellName
     */
    public String getCellName()
    {
        return cellName;
    }

    /**
     * @return the tableInfo
     */
    public DBTableInfo getTableInfo()
    {
        return tableInfo;
    }

    /**
     * Sets the "cell" name of this control, this is the name of this control in the form.
     * @param cellName the cell name
     */
    public void setCellName(String cellName)
    {
        this.cellName = cellName;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#requestFocus()
     */
    @Override
    public void requestFocus()
    {
        textWithQuery.requestFocus();
    }

    /* (non-Javadoc)ValComboBoxFromQuery
     * @see java.awt.Component#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        boolean isEnabled = enabled;
        if (isRestricted)
        {
            isEnabled = false;
        }
        
        super.setEnabled(isEnabled);
        
        if (textWithQuery != null)
        {
            textWithQuery.setEnabled(isEnabled);
        }
        if (searchBtn != null)
        {
            searchBtn.setEnabled(isEnabled);
        }
        if (editBtn != null)
        {
            editBtn.setEnabled(isEnabled && (dataObj != null || textWithQuery.getSelectedId() != null));
        }
        if (createBtn != null)
        {
            createBtn.setEnabled(isEnabled);
        }
        if (cloneBtn != null)
        {
            cloneBtn.setEnabled(isEnabled && (dataObj != null || textWithQuery.getSelectedId() != null));
        }
        
        // Cheap easy way of setting the Combobox's Text Field to the proper BG Color
        setRequired(isRequired);

    }
    
    /**
     * @param searchDlgName the searchDlgName to set
     */
    public void setSearchDlgName(String searchDlgName)
    {
        this.searchDlgName = searchDlgName;
    }


    /**
     * @return the searchDlgName
     */
    public String getSearchDlgName()
    {
        return searchDlgName;
    }
    /**
     * @param displayDlgName the displayDlgName to set
     */
    public void setDisplayDlgName(String displayDlgName)
    {
        this.displayDlgName = displayDlgName;
    }

    /**
     * @return whether is has a value
     */
    public boolean isNotEmpty()
    {
        return dataObj != null;
    }

    /**
     * Helper to create a button.
     * @param iconName the name of the icon (not localized)
     * @param tooltipKey the name of the tooltip (not localized)
     * @param objTitle the title of one object needed for the Info Button
     * @return the new button
     */
    protected JButton createBtn(final String iconName, final String tooltipKey, final String objTitle)
    {
        JButton btn = new JButton(IconManager.getIcon(iconName, IconManager.IconSize.Std16));
        btn.setOpaque(false);
        btn.setToolTipText(String.format(getResourceString(tooltipKey), new Object[] {objTitle}));
        btn.setFocusable(false);
        btn.setMargin(new Insets(1,1,1,1));
        btn.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        return btn;
    }

    /**
     * Creates the UI for the ComboBox.
     * @param objTitle the title of one object needed for the Info Button
     */
    public void init(final String objTitle,
                     final int    btnMask)
    {
        fieldNames = split(StringUtils.deleteWhitespace(keyName), ",");

        // strip off any table prefixes
        for (int i=0;i<fieldNames.length;i++)
        {
            String fName = fieldNames[i];
            if (fName.indexOf('.') > -1)
            {
                fieldNames[i] = StringUtils.substringAfterLast(fName, ".");
            }
        }
        
        boolean hasSearchDlg     = StringUtils.isNotEmpty(tableInfo.getSearchDialog());
        boolean hasSearchBtn     = (btnMask & CREATE_SEARCH_BTN) != 0;
        boolean hasCloneBtn      = (btnMask & CREATE_CLONE_BTN) != 0;
        boolean hasEditBtn       = (btnMask & CREATE_EDIT_BTN) != 0;
        boolean hasAddBtn        = (btnMask & CREATE_NEW_BTN) != 0;
        final boolean hasViewBtn = (btnMask & CREATE_VIEW_BTN) != 0;

        StringBuilder sb = new StringBuilder("p:g,1px,p,1px,p");
        if (hasSearchDlg)
        {
            sb.append(",1px,p");
        }
        if (hasCloneBtn)
        {
            sb.append(",1px,p");
        }
        PanelBuilder    pb = new PanelBuilder(new FormLayout(sb.toString(), "c:p"), this);
        CellConstraints cc = new CellConstraints();

        pb.add(textWithQuery, cc.xy(1,1));
        
        PermissionSettings perm = AppContextMgr.isSecurityOn() ? tableInfo.getPermissions() : null;
        
        
        int x = 3;
        if (hasEditBtn || hasViewBtn)
        {
            String iconName;
            String ttName;
            if (hasEditBtn && (perm == null || perm.canModify()))
            {
                iconName = "EditIcon";
                ttName   = "EditRecordTT";
                
            } else
            {
                iconName = "InfoIcon";
                ttName   = "ShowRecordInfoTT";
            }
            
            editBtn = createBtn(iconName, ttName, objTitle);
            pb.add(editBtn, cc.xy(x,1));
            x += 2;
        }

        
        if (hasAddBtn && (perm == null || perm.canAdd()))
        {
            createBtn = createBtn("CreateObj", "NewRecordTT", objTitle); 
            pb.add(createBtn, cc.xy(x,1));
            x += 2;
        }

        if (hasCloneBtn && (perm == null || perm.canAdd()))
        {
            cloneBtn = createBtn("CloneObj", "CloneRecordTT", objTitle); 
            pb.add(cloneBtn, cc.xy(x,1));
            x += 2;
        }

        if (hasSearchDlg && hasSearchBtn && (perm == null || perm.canAdd()))
        {
            textWithQuery.setAddAddItem(hasAddBtn); // set to true if there is an add btn
            searchBtn = createBtn("Search", "SearchForRecordTT", objTitle); 
            pb.add(searchBtn, cc.xy(x,1));
            x += 2;
        }

        if (!UIHelper.isMacOS())
        {
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        }

        bgColor = textWithQuery.getTextField().getBackground();
        if (valTextColor == null || requiredFieldColor == null)
        {
            valTextColor       = AppPrefsCache.getColorWrapper("ui.formatting.valtextcolor");
            requiredFieldColor = AppPrefsCache.getColorWrapper("ui.formatting.requiredfieldcolor");
        }
        
        AppPrefsCache.addChangeListener("ui.formatting.requiredfieldcolor", this);

        if (searchBtn != null)
        {
            defaultSearchAction = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    displaySearchDialog();
                }
            };
            searchBtn.addActionListener(defaultSearchAction);
        }

        if (editBtn != null)
        {
            defaultEditAction = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    currentMode = MODE.Editting;
                    textWithQuery.setIgnoreFocusLost(true);
                    createEditFrame(false, false, hasViewBtn);
                }
            };
            editBtn.addActionListener(defaultEditAction);
        }

        if (createBtn != null)
        {
            defaultNewAction = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    //currentMode = dataObj != null ? MODE.NewAndNotEmpty : MODE.NewAndEmpty;
                    currentMode = MODE.NewAndNotEmpty;
                    textWithQuery.setIgnoreFocusLost(true);
                    createEditFrame(true, false, false);
                }
            };
            createBtn.addActionListener(defaultNewAction);
        }
        
        if (cloneBtn != null)
        {
            defaultCloneAction = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    currentMode = MODE.NewAndNotEmpty;
                    textWithQuery.setIgnoreFocusLost(true);
                    createEditFrame(true, true, false);
                }
            };
            cloneBtn.addActionListener(defaultCloneAction);
        }
    }
    
    /**
     * @return the name of the search that used by the search dialog.
     */
    public String getSearchName()
    {
        return UIRegistry.getViewbasedFactory().getSearchName(StringUtils.isNotEmpty(searchDlgName) ? searchDlgName : tableInfo.getSearchDialog());
    }
    
    /**
     * 
     */
    protected void displaySearchDialog()
    {
        String dlgName = StringUtils.isNotEmpty(searchDlgName) ? searchDlgName : tableInfo.getSearchDialog();
        
        ViewBasedSearchDialogIFace dlg = UIRegistry.getViewbasedFactory().createSearchDialog(UIHelper.getWindow(searchBtn), 
                                                                                             dlgName);
        dlg.setMultipleSelection(false);
        if (builder != null)
        {
            dlg.registerQueryBuilder(builder);
        }
        
        dlg.getDialog().setVisible(true);
        if (!dlg.isCancelled())
        {
            Object dlgDataObj = dlg.getSelectedObject();
            
            if (dlgDataObj instanceof FormDataObjIFace)
            {
                DataProviderSessionIFace localSession = null;
                try
                {
                    localSession = DataProviderFactory.getInstance().createSession();
                    localSession.attach(dlgDataObj);
                    setValue(dlgDataObj, null);
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ValComboBoxFromQuery.class, ex);
                    ex.printStackTrace();
                    
                } finally
                {
                    if (localSession != null)
                    {
                        localSession.close();
                    }
                }
            }
           
            valueHasChanged();
            
            notifyListeners(null);
        }
    }
    
    /**
     * @param al
     */
    public void setEditAction(ActionListener al)
    {
        if (editBtn != null)
        {
            removeAllActionListeners(editBtn);
            if (al != null)
            {
                editBtn.addActionListener(al);
            }
        }
    }
    
    /**
     * @param al
     */
    public void setSearchAction(ActionListener al)
    {
        if (searchBtn != null)
        {
            removeAllActionListeners(searchBtn);
            if (al != null)
            {
                searchBtn.addActionListener(al);
            }
        }
    }
    
    /**
     * @param al
     */
    public void setNewAction(final ActionListener al)
    {
        if (createBtn != null)
        {
            removeAllActionListeners(createBtn);
            if (al != null)
            {
                createBtn.addActionListener(al);
            }
        }
    }

    /**
     * @param button
     */
    protected void removeAllActionListeners(final JButton button)
    {
        for (ActionListener al: button.getActionListeners())
        {
            button.removeActionListener(al);
        }
    }
    
    /**
     * @param enabled
     */
    /*public void setEditEnabled(boolean enabled)
    {
        if (editBtn != null)
        { 
            editBtn.setEnabled(enabled);
        }
    }*/
    
    /**
     * 
     */
    protected void valueHasChanged()
    {
        if (frame != null)
        {
            MultiView mv = frame.getMultiView();
            if (mv != null)
            {           
               if (mv.hasChanged())
               {
                   this.setChanged(true);
               }
               
               //!!!!!!!!!!!!!!!!!!!!!!!!!
               // need to make the change listener fire here!
            }
            
            frame.getMultiView().getDataFromUI();
            refreshUIFromData(true);
        }
    }
    
    /**
     * @param comp
     * @param isPartial
     */
    public static void setIsPartial(final Component comp, final boolean isPartial)
    {
        if (comp instanceof ValFormattedTextField)
        {
            ((ValFormattedTextField)comp).setPartialOK(isPartial);
        } else if (comp instanceof ValFormattedTextFieldSingle)
        {
            ((ValFormattedTextFieldSingle)comp).setPartialOK(isPartial);
        }
    }

    /**
     * Creates a Dialog (non-modal) that will display detail information
     * for the object in the text field.
     * @param isNewObject the data object is new
     * @param isCloned the data object is cloned
     * @param isViewOnly the data object is view only even when the form is in edit mode
     */
    protected void createEditFrame(final boolean isNewObject, 
                                   final boolean isCloned,
                                   final boolean isViewOnly)
    {
        boolean canModify = !isViewOnly;
        if (AppContextMgr.isSecurityOn() && tableInfo.getPermissions() != null)
        {
            canModify = tableInfo.getPermissions().canModify() && !isViewOnly;
        }
        
        int options = (isNewObject ? MultiView.IS_NEW_OBJECT : MultiView.NO_OPTIONS) | 
                      MultiView.HIDE_SAVE_BTN | 
                      (canModify ? (MultiView.DONT_ADD_ALL_ALTVIEWS | MultiView.USE_ONLY_CREATION_MODE) : MultiView.NO_OPTIONS);
        
        String dlgName       = StringUtils.isNotEmpty(displayDlgName) ? displayDlgName : tableInfo.getNewObjDialog();
        String closeBtnTitle = getResourceString(canModify ? "SAVE" : "CLOSE"); 
        frame = UIRegistry.getViewbasedFactory().createDisplay(UIHelper.getWindow(this),
                                                                   dlgName,
                                                                   frameTitle,
                                                                   closeBtnTitle,
                                                                   canModify,   // false means View Mode
                                                                   options,
                                                                   helpContext,
                                                                   ViewBasedDialogFactoryIFace.FRAME_TYPE.DIALOG);
        if (frame == null)
        {
            return;
        }
        
        if (isNewObject)
        {
            if (isCloned)
            {
                if (dataObj != null)
                {
                    DataProviderSessionIFace localSession = null;
                    try
                    {
                        localSession = DataProviderFactory.getInstance().createSession();
                        localSession.attach(dataObj);
                        newDataObj = (FormDataObjIFace) dataObj.clone();
                        
                    } catch (CloneNotSupportedException e)
                    {
                        e.printStackTrace();
                        UIRegistry.showError("Clone is not supported for "+(dataObj != null ? dataObj.getClass().getSimpleName() : "Unknow record type"));
                    } finally
                    {
                        if (localSession != null) localSession.close(); 
                    }
                } else
                {
                    UIRegistry.showError("There isn't anything to clone.\nPlease contact customer support about this issue.");
                    return;
                }
            } else
            {
                newDataObj = FormHelper.createAndNewDataObj(tableInfo.getClassObj());
            }
            
            // Now get the setter for an object and set the value they typed into the combobox and place it in
            // the first field name
            Component comp = null;
            DataObjectSettable ds = DataObjectSettableFactory.get(tableInfo.getClassObj().getName(), FormHelper.DATA_OBJ_SETTER);
            if (ds != null)
            {
                //log.info("ID: ["+textWithQuery.getSelectedId()+"]  PrevText["+textWithQuery.getPrevEnteredText()+"] Cached["+textWithQuery.getCachedPrevText()+"]");
                String value = textWithQuery.getSelectedId() == null ? textWithQuery.getPrevEnteredText() : "";
                if (!isCloned)
                {
                    ds.setFieldValue(newDataObj, fieldNames[0], value);
                }
                
                MultiView mv = frame.getMultiView();
                if (mv != null)
                {
                    FormViewObj fvo = mv.getCurrentViewAsFormViewObj();
                    if (fvo != null)
                    {
                        comp = fvo.getControlByName(fieldNames[0]);
                    }
                }
            }
            
            setIsPartial(comp, true);
            frame.setData(newDataObj);
            setIsPartial(comp, false);

        } else
        {
            // Here we set the dataobj to null to 
            // ensure we always load the latest object
            dataObj = null;
            frame.setData(getValue());
        }
        
        //if (multiView != null)
        //{
        //    multiView.registerDisplayFrame(frame);
        //}
        
        frame.showDisplay(true);
        if (frame.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            if (frame.isEditMode())
            {
                if (currentMode == MODE.NewAndEmpty)
                {
                    if (multiView != null)
                    {
                        
                        FormViewObj fvo = frame.getMultiView().getCurrentViewAsFormViewObj();
                        if (fvo != null)
                        {
                            fvo.saveObject();
                            newDataObj = (FormDataObjIFace)fvo.getDataObj();
                        }
                        
                        Object parentDataObj = multiView.getData();
                        if (parentDataObj instanceof FormDataObjIFace)
                        {
                            ((FormDataObjIFace) parentDataObj).addReference(newDataObj, cellName);
                        }
                        else
                        {
                            FormHelper.addToParent(multiView != null ? multiView.getData() : null, newDataObj);
                        }
                    }

                    setValue(newDataObj, null);
                    newDataObj = null;
                } else
                {
                    FormViewObj fvo = frame.getMultiView().getCurrentViewAsFormViewObj();
                    if (fvo != null)
                    {
                        fvo.saveObject();
                        newDataObj = (FormDataObjIFace)fvo.getDataObj();
                        setValue(newDataObj, null);
                        newDataObj = null;
                    } 
                }
                valueHasChanged();
                notifyListeners(null);
            }

            currentMode = MODE.Unknown;

            //if (multiView != null)
            //{
            //    multiView.unregisterDisplayFrame(frame);
            //}
            
        } else if (!textWithQuery.hasId())
        {
            textWithQuery.setText("");
            textWithQuery.setPrevEnteredText("");
        }
        frame.dispose();
        frame = null;
    }

    /**
     * Sets the string that is pre-appended to the title.
     * @param frameTitle the string arg
     */
    public void setFrameTitle(final String frameTitle)
    {
        if (StringUtils.isNotEmpty(frameTitle))
        {
            this.frameTitle = frameTitle;
        }
    }

    /**
     * Sets the MultiView parent into the control.
     * @param multiView parent multiview
     */
    public void setMultiView(final MultiView multiView)
    {
        this.multiView = multiView;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);

        if ((!isNew || hasBeenVisited) && isInError() && textWithQuery != null && textWithQuery.isEnabled())
        {
            UIHelper.drawRoundedRect((Graphics2D)g, valTextColor.getColor(), getSize(), 1);
        }
    }
    
    /**
     * Adds a listener.
     * @param listSelectionListenerArg the listener
     */
    public void addListSelectionListener(ListSelectionListener listSelectionListener)
    {
        if (this.listSelectionListeners == null)
        {
            this.listSelectionListeners = new Vector<ListSelectionListener>();
        }
        this.listSelectionListeners.add(listSelectionListener);
    }

    /**
     * Rmeoves the listener.
     * @param listChangeListener the listChangeListener to set
     */
    public void removeListSelectionListener(ListSelectionListener listSelectionListener)
    {
        if (this.listSelectionListeners != null && listSelectionListener != null)
        {
            this.listSelectionListeners.remove(listSelectionListener);
        }
    }
    
    // Overriding the add/remove of FocusListeners is so we can make sure they get
    // called AFTER the Combobox has had a change to process it's focus listener

    /* (non-Javadoc)
     * @see java.awt.Component#addFocusListener(java.awt.event.FocusListener)
     */
    @Override
    public void addFocusListener(FocusListener l)
    {
        focusListeners.add(l);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#removeFocusListener(java.awt.event.FocusListener)
     */
    @Override
    public void removeFocusListener(FocusListener l)
    {
        focusListeners.remove(l);
    }

    /**
     * @return the textWithQuery
     */
    public TextFieldWithQuery getTextWithQuery()
    {
        return textWithQuery;
    }


    /**
     * Updates the UI from the data value (assume the data has changed but OK if it hasn't).
     * @param useSession indicates it should create a session
     */
    private void refreshUIFromData(final boolean useSession)
    {
        if (this.dataObj != null)
        {
            if (getter == null)
            {
                getter = new DataGetterForObj();
            }

            // NOTE: If there was a formatName defined for this then the value coming
            // in will already be correctly formatted.
            // So just set the value if there is a format name.
            Object newVal = this.dataObj;
            if (isEmpty(dataObjFormatterName))
            {
                Object[] val = UIHelper.getFieldValues(fieldNames, this.dataObj, getter);
                
                UIFieldFormatterIFace uiFieldFormatter = textWithQuery.getUiFieldFormatter();
                if (uiFieldFormatter != null)
                {
                    if (val != null && val.length > 0 && val[0] != null)
                    {
                        newVal = uiFieldFormatter.formatFromUI(val[0]).toString();
                    } else
                    {
                        newVal = null;
                    }
                } else
                {
                    
                    if (StringUtils.isNotEmpty(textWithQuery.getFormat()))
                    {
                        newVal = UIHelper.getFormattedValue(textWithQuery.getFormat(), val);
                    } else
                    {
                        newVal = this.dataObj;
                    }
                }
            } else
            {
                DataProviderSessionIFace localSession = null;
                try
                {
                    localSession = DataProviderFactory.getInstance().createSession();
                    newVal = DataObjFieldFormatMgr.getInstance().format(this.dataObj, dataObjFormatterName);
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ValComboBoxFromQuery.class, ex);
                    ex.printStackTrace();
                } finally
                {
                    if (localSession != null)
                    {
                        localSession.close();
                    }
                }
            }

            if (newVal != null && textWithQuery != null)
            {
                valState = UIValidatable.ErrorType.Valid;
                textWithQuery.setSelectedId(dataObj != null ? dataObj.getId() : null); // needs to be done before and after
                
                final JTextField tf = textWithQuery.getTextField();
                // rods 08/18/08 - doesn't seem to be needed it is already set correctly
                //
                // 02/06/09 - Commented out because it is causing the idList to be cleared
                // If you turn it back on make sure you turn on ignoreDocChange in the TextFieldWithQuery
                //
                // 02/10/09 - rods - Instead of call seTText directly on the TextField, it is called on the TextFieldWithQuery
                // which disables Doc change notifications
                textWithQuery.setText(newVal.toString());
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        tf.setSelectionStart(-1);
                        tf.setSelectionEnd(-1);
                        tf.setCaretPosition(0);                        
                    }
                    
                });

                textWithQuery.setSelectedId(dataObj != null ? dataObj.getId() : null);
                
                if (editBtn != null)
                {
                    editBtn.setEnabled(true);
                }
                if (cloneBtn != null)
                {
                    cloneBtn.setEnabled(false);
                }
            } else
            {
                if (textWithQuery != null)
                {
                    textWithQuery.clearSelection();
                }
                valState = UIValidatable.ErrorType.Incomplete;
            }

        } else
        {
            if (textWithQuery != null)
            {
                textWithQuery.clearSelection();
            }
            valState = UIValidatable.ErrorType.Incomplete;
            if (editBtn != null)
            {
                editBtn.setEnabled(false);
            }
            if (cloneBtn != null)
            {
                cloneBtn.setEnabled(false);
            }
            if (textWithQuery != null && textWithQuery.getTextField() != null)
            {
                textWithQuery.setText("");
                textWithQuery.getTextField().repaint();
            }
        }
        repaint();
    }

    //--------------------------------------------------
    //-- SessionListenerIFace Interface
    //--------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.SessionListenerIFace#setSession(edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void setSession(DataProviderSessionIFace session)
    {
        this.session = session;
    }
    
    //--------------------------------------------------
    //-- UIValidatable Interface
    //--------------------------------------------------


    /* (non-Javadoc)
     * @see edu.kui.brc.ui.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        if (valState == UIValidatable.ErrorType.Incomplete)
        {
            return isRequired;
        }
        return valState != UIValidatable.ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getState()
     */
    public ErrorType getState()
    {
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setState(edu.ku.brc.ui.forms.validation.UIValidatable.ErrorType)
     */
    public void setState(ErrorType state)
    {
        this.valState = state;
    }
    /* (non-Javadoc)
     * @see edu.kui.brc.ui.validation.UIValidatable#isRequired()
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.kui.brc.ui.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(boolean isRequired)
    {
        if (textWithQuery != null && textWithQuery.getTextField() != null)
        {
            textWithQuery.getTextField().setBackground(isRequired && isEnabled() ? requiredFieldColor.getColor() : bgColor);
        }
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isChanged()
     */
    public boolean isChanged()
    {
        return isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    public void setChanged(boolean isChanged)
    {
        this.isChanged = isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    public void setAsNew(boolean isNew)
    {
        this.isNew = isRequired ? isNew : false;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#validate()
     */
    public UIValidatable.ErrorType validateState()
    {
        //log.debug("validateState "+(isRequired && textWithQuery.hasItem()));
        valState = isRequired && !textWithQuery.hasItem() ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
        //log.debug(valState);
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#reset()
     */
    public void reset()
    {
        textWithQuery.clearSelection();
        
        if (textWithQuery.getTextField() != null)
        {
            textWithQuery.setText("");
            textWithQuery.setPrevEnteredText(null);
        }
        valState = isRequired ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
        repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    public Component getValidatableUIComp()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#cleanUp()
     */
    public void cleanUp()
    {
        if (textWithQuery != null && textWithQuery.getTextField() != null)
        {
            UIHelper.removeFocusListeners(textWithQuery.getTextField());
            UIHelper.removeKeyListeners(textWithQuery.getTextField());
        }
        UIHelper.removeFocusListeners(textWithQuery);
        UIHelper.removeFocusListeners(textWithQuery);
        UIHelper.removeKeyListeners(this);
        
        tableInfo = null;
        getter    = null;
        dataObj   = null;
        frame     = null;
        multiView = null;
        textWithQuery  = null;
        
        if (listSelectionListeners != null)
        {
            listSelectionListeners.clear();
        }

        focusListeners.clear();

        textWithQuery = null;
        AppPrefsCache.removeChangeListener("ui.formatting.requiredfieldcolor", this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getReason()
     */
    public String getReason()
    {
        return null;
    }

    /**
     * Registers an interface that can be asked for the Query string and the results info.
     * @param builder the builder object
     */
    public void registerQueryBuilder(final ViewBasedSearchQueryBuilderIFace builderArg)
    {
        this.builder = builderArg;
        this.textWithQuery.setBuilder(builderArg);
    }

    // --------------------------------------------------------
    // ListSelectionListener
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(final ListSelectionEvent e)
    {
        //log.debug("valueChanged: "+(e != null ? ((JMenuItem)e.getSource()).getText() : "null"));
        //log.debug("valueChanged: "+(e != null ? e.getClass().getSimpleName() : "null")+" "+e.getSource().getClass().getSimpleName());
        if (e != null)
        {
            if (e.getSource() instanceof TextFieldWithQuery)
            {
                if (((TextFieldWithQuery)e.getSource()).getTextField().getText().length() == 0)
                {
                    dataObj = null;
                }
            } else
            {
                String itemLabel = null;
                if (e.getSource() instanceof JMenuItem)
                {
                    itemLabel = ((JMenuItem)e.getSource()).getText().toString();
                    this.dataObj = null;
                    getValue();
                    refreshUIFromData(true);
                    
                } else if (e.getSource() instanceof JList)
                {
                    JList listBox = (JList)e.getSource(); 
                    if (listBox.getSelectedIndex() > -1)
                    {
                        itemLabel = listBox.getSelectedValue().toString();
                        this.dataObj = null;
                    } else
                    {
                        return;
                    }
                }
                
                if (itemLabel != null && itemLabel.equals(UIRegistry.getResourceString("TFWQ_ADD_LABEL")))
                {
                    if (defaultNewAction != null)
                    {
                        defaultNewAction.actionPerformed(null);
                    }
                    return;
                }
            }
        }
        
        isChanged = true;
        valueHasChanged();
        validateState();
        
        boolean doEnable = dataObj != null || (textWithQuery != null && textWithQuery.getSelectedId() != null);
        if (editBtn != null)
        {
            editBtn.setEnabled(doEnable);
        }
        if (cloneBtn != null)
        {
            cloneBtn.setEnabled(doEnable);
        }
        notifyListeners(e);
        
        repaint();
    }
    
    /**
     * @param e
     */
    private void notifyListeners(final ListSelectionEvent e)
    {
        if (listSelectionListeners != null)
        {
            for (ListSelectionListener l : listSelectionListeners)
            {
                l.valueChanged(e);
            }
        }
    }

    //--------------------------------------------------------
    // GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(final Object value, final String defaultValue)
    {
        if (isRestricted)
        {
            textWithQuery.setText(restrictedStr);
            return;
        }
        
        if (value == null || value instanceof FormDataObjIFace)
        {
            dataObj = (FormDataObjIFace)value;
            if (dataObj != null)
            {
                if (session != null)
                {
                    try
                    {
                        session.attach(dataObj);
                    } catch (Exception ex)
                    {
                        session.refresh(dataObj);
                    }
                    dataObj.forceLoad();
                }
            }
            refreshUIFromData(false);
            
            if (cloneBtn != null)
            {
                cloneBtn.setEnabled(dataObj != null || textWithQuery.getSelectedId() != null);
            }
            
        } else
        {
            if (Set.class.isAssignableFrom(value.getClass()))
            {
                UIRegistry.showError("The QueryComboBox cannot handle Sets! field name["+cellName+"]");   
            }
            throw new RuntimeException("Data does not extend FormDataObjIFace ["+ value + "] " + value.getClass());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        Integer id = textWithQuery != null ? textWithQuery.getSelectedId() : null;
        if (id == null)
        {
            return dataObj = null;
        }
        
        if (dataObj != null && dataObj.getId().intValue() == id.intValue())
        {
            return dataObj;
        }
        
        DataProviderSessionIFace localSession = null;
        try
        {
            localSession = DataProviderFactory.getInstance().createSession();
            //log.debug(tableInfo.getClassObj()+" " +tableInfo.getIdFieldName()+" " +id);
            List<?> list = localSession.getDataList(tableInfo.getClassObj(), tableInfo.getIdFieldName(), id, DataProviderSessionIFace.CompareType.Restriction);
            if (list.size() != 0)
            {
                dataObj = (FormDataObjIFace)list.get(0);
                dataObj.forceLoad();
                
            } else
            {
                log.error("**** Can't find the Object "+tableInfo.getClassObj()+" with ID: "+id);
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ValComboBoxFromQuery.class, ex);
            ex.printStackTrace();
        } finally 
        {
            if (localSession != null)
            {
                localSession.close();
            }
        }

        return dataObj;
    }

    //-------------------------------------------------
    // AppPrefsChangeListener
    //-------------------------------------------------

    public void preferenceChange(AppPrefsChangeEvent evt)
    {
        if (evt.getKey().equals("ui.formatting.requiredfieldcolor"))
        {
            textWithQuery.setBackground(isRequired && isEnabled() ? requiredFieldColor.getColor() : bgColor);
        }
    }
    
    //-------------------------------------------------
    // Inner Classes
    //-------------------------------------------------
    class ValTextWithQuery extends JComboBoxFromQuery implements DocumentListener
    {
        protected boolean localHasFocus = false;
        
        public ValTextWithQuery(final String tableName,
                                final String idColumn,
                                final String keyColumn,
                                final String displayColumns,
                                final String format)
        {
            super(tableName, idColumn, keyColumn, displayColumns, format);
            
            getTextField().getDocument().addDocumentListener(this);
            getTextField().addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e)
                {
                    log.debug("focusGained");
                    localHasFocus = true;
                    super.focusGained(e);
                }

                @Override
                public void focusLost(FocusEvent e)
                {
                    log.debug("focusLost");
                    localHasFocus = false;
                    super.focusLost(e);
                }
            });
        }
        
        /**
         * Processes Focus Gained
         * @param e key event
         */
        @Override
        protected void tfFocusGained(@SuppressWarnings("unused") FocusEvent e)
        {
            log.debug("tfFocusGained");
            super.tfFocusGained(e);
            
            hasBeenVisited = true;
            
            for (FocusListener l : focusListeners)
            {
                l.focusGained(e);
            }
        }
        
        /**
         * Processes Focus Lost
         * @param e key event
         */
        @Override
        protected void tfFocusLost(@SuppressWarnings("unused") FocusEvent e)
        {
            super.tfFocusLost(e);
            isNew = false; // hasBeenVisited may remove the need for this being set to false - rods
            
            validateState();
            repaint();
            
            for (FocusListener l : focusListeners)
            {
                l.focusLost(e);
            }
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.db.JComboBoxFromQuery#buildSQL(java.lang.String)
         */
        @Override
        protected String buildSQL(final String searchText)
        {
            if (builder != null)
            {
                return builder.buildSQL(searchText, false);
            }
            return super.buildSQL(searchText);
        }
        
        /**
         * 
         */
        protected void documentChanged()
        {
            if (localHasFocus && textWithQuery.hasItem())
            {
                textWithQuery.clearSelection();    
            }
            
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
         */
        public void changedUpdate(DocumentEvent e)
        {
            documentChanged();
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
         */
        public void insertUpdate(DocumentEvent e)
        {
            documentChanged();
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
         */
        public void removeUpdate(DocumentEvent e)
        {
            documentChanged();
        }
    }
}
