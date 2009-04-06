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
package edu.ku.brc.af.ui.forms;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.setControlSize;
import static edu.ku.brc.ui.UIRegistry.enableActionAndMenu;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.exception.ConstraintViolationException;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.RecordSetFactory;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBInfoBase;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.af.ui.db.JAutoCompComboBox;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.af.ui.forms.MultiView.ViewState;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.FormCell;
import edu.ku.brc.af.ui.forms.persist.FormCellField;
import edu.ku.brc.af.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellLabel;
import edu.ku.brc.af.ui.forms.persist.FormCellSubView;
import edu.ku.brc.af.ui.forms.persist.FormCellSubViewIFace;
import edu.ku.brc.af.ui.forms.persist.FormViewDef;
import edu.ku.brc.af.ui.forms.persist.ViewDef;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.validation.AutoNumberableIFace;
import edu.ku.brc.af.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.FormValidatorInfo;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.af.ui.forms.validation.ValidationListener;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.dbsupport.StaleObjectException;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.ui.ColorChooser;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Orderable;

/**
 * This implements a Form and is "owed" by a MultiView.<br>
 * <br>
 * Implementation of the Viewable interface for the ui and this derived class is for handling Form's Only (not tables).<br>
 * <br>
 * Implements ViewBuilderIFace which the ViewFactory uses while processing the rows, it calls methods in this interface
 * to add labels, controls and subforms to the form.<br>
 * <br>
 * Implements ValidationListener so it can listen to any and all validations so it knows how to show and activate the icon button
 * that enables the user to see what the errors are in a form.<br>
 * <br>
 * Implements ResultSetControllerListener to react to the record control bar for moving forward or backward in a resultset.<br>
 * <br>
 * Implements AppPrefsChangeListener to be notified of changes to the BG Required Field color or the date formatting.
 * 
 *
 * @author rods
 *
 */
public class FormViewObj implements Viewable, 
                                    ViewBuilderIFace, 
                                    ValidationListener, 
                                    ResultSetControllerListener, 
                                    AppPrefsChangeListener,
                                    BusinessRulesOkDeleteIFace,
                                    PropertyChangeListener
{
    private static final Logger log = Logger.getLogger(FormViewObj.class);
    
    protected enum SAVE_STATE {Initial, NewObjSaveerror, StaleRecovery, SaveOK, Error}
    

    // Static Data Members
    protected static Object[]               formattedValues = new Object[2];
    protected static ColorWrapper           viewFieldColor  = null;
    protected static CellConstraints        cc              = new CellConstraints();
    protected static boolean                useDebugForm    = false;
    public    static final String           STATUSBAR_NAME  = "FormViewObj";

    // Data Members
    protected DataProviderSessionIFace      session        = null;
    protected boolean                       isEditing     = false;
    protected boolean                       isNewlyCreatedDataObj = false;
    protected MultiView                     mvParent       = null;
    protected ViewIFace                     view;
    protected AltViewIFace                  altView;
    protected FormViewDef                   formViewDef;
    protected String                        cellName;
    protected Component                     formComp       = null;
    protected List<MultiView>               kids           = new ArrayList<MultiView>();
    protected Vector<AltViewIFace>          altViewsList   = null;
    protected Class<?>                      classToCreate  = null;

    protected ArrayList<FVOFieldInfo>          compsList      = new ArrayList<FVOFieldInfo>();
    protected Hashtable<String, FVOFieldInfo>  controlsById   = new Hashtable<String, FVOFieldInfo>();
    protected Hashtable<String, FVOFieldInfo>  controlsByName = new Hashtable<String, FVOFieldInfo>();
    protected Hashtable<String, FVOFieldInfo>  labels         = new Hashtable<String, FVOFieldInfo>(); // ID is the Key
    
    protected FormLayout                    formLayout;
    protected PanelBuilder                  builder;
    protected boolean                       isBuildValid     = false;

    protected boolean                       isSkippingAttach = false; // Indicates whether to skip before setting data into the form
    protected Boolean                       isJavaCollection = null;
    
    protected FormValidator                 formValidator   = null;
    protected Object                        parentDataObj   = null;
    protected Object                        dataObj         = null;
    protected Set<Object>                   origDataSet     = null;
    protected Object[]                      singleItemArray = new Object[1];
    protected DateWrapper                   scrDateFormat;
    protected int                           options;

    protected JPanel                        mainComp        = null;
    protected ControlBarPanel               controlPanel    = null;
    protected ResultSetController           rsController    = null;
    protected Vector<Object>                list            = null;
    protected boolean                       ignoreSelection = false;
    protected JComponent                    saveControl     = null;
    protected JButton                       newRecBtn       = null;
    protected JButton                       delRecBtn       = null;
    protected boolean                       wasNull         = false;
    protected MenuSwitcherPanel             switcherUI;
    protected int                           mainCompRowInx  = 1;
    protected boolean                       saveAndNew      = false;  
    protected boolean                       isAutoNumberOn  = true; 
    protected RestrictablePanel             restrictablePanel = null;
    protected JPanel                        sepController   = null;
    
    
    protected String                        searchName      = null;
    protected JButton                       srchRecBtn      = null;
    
    // When creating a new Data Object
    protected Object                        oldDataObj           = null;
    protected boolean                       doSetIntoAndValidate = true;
    
    // Forms that have a Selector
    protected JComboBox                     selectorCBX     = null;
    protected boolean                       isSelectorForm;
    protected boolean                       isShowing       = false;

    protected BusinessRulesIFace            businessRules   = null;
    protected boolean                       hasInitBR       = false;

    protected DraggableRecordIdentifier     draggableRecIdentifier   = null;
    
    // Carry Forward
    protected CarryForwardInfo              carryFwdInfo    = null;
    protected boolean                       doCarryForward  = false;
    protected Object                        carryFwdDataObj = null;
    
    // RecordSet Management
    protected RecordSetIFace                recordSet         = null;
    protected List<RecordSetItemIFace>      recordSetItemList = null;  
    protected DBTableInfo                   tableInfo         = null;
    
    protected Color                         bgColor           = null;
    
    protected Vector<ViewState>             viewStateList     = null;
    
    // Security
    private PermissionSettings              perm = null;

    /**
     * Constructor with FormView definition.
     * @param view the definition of the view
     * @param altView indicates which AltViewIFace we will be using
     * @param mvParent the mvParent multiview
     * @param createResultSetController indicates that a ResultSet Controller should be created
     * @param formValidator the form's formValidator
     * @param options the options needed for creating the form
     * @param bgColor bg color it should use
     */
    public FormViewObj(final ViewIFace     view,
                       final AltViewIFace  altView,
                       final MultiView     mvParent,
                       final FormValidator formValidator,
                       final int           options,
                       final Color         bgColor)
    {
        this(view, altView, mvParent, formValidator, options, null, bgColor);
    }
    
    /**
     * Constructor with FormView definition.
     * @param view the definition of the view
     * @param altView indicates which AltViewIFace we will be using
     * @param mvParent the mvParent mulitview
     * @param formValidator the form's formValidator
     * @param options the options needed for creating the form
     * @param cellName the name of the outer form's cell for this view (subview)
     * @param bgColor bg color it should use
     */
    public FormViewObj(final ViewIFace     view,
                       final AltViewIFace  altView,
                       final MultiView     mvParent,
                       final FormValidator formValidator,
                       final int           options,
                       final String        cellName,
                       final Color         bgColor)
    {
        this.view        = view;
        this.altView     = altView;
        this.mvParent    = mvParent;
        this.cellName    = cellName;
        this.bgColor     = bgColor;

        businessRules    = view.createBusinessRule();
        isEditing        = altView.getMode() == AltViewIFace.CreationMode.EDIT;
        
        boolean addSearch = mvParent != null && MultiView.isOptionOn(mvParent.getOptions(), MultiView.ADD_SEARCH_BTN);
        if (addSearch)
        {
            isEditing = false;
        }
        this.formViewDef = (FormViewDef)altView.getViewDef();
        
        // Figure columns
        try
        {
            JPanel panel = useDebugForm ? new FormDebugPanel() : (restrictablePanel = new RestrictablePanel());
            formLayout = new FormLayout(formViewDef.getColumnDef(), formViewDef.getRowDef());
            builder    = new PanelBuilder(formLayout, panel);
            
        } catch (java.lang.NumberFormatException ex)
        {
            String msg = "Error in row or column definition for form: `"+view.getName() + "`\n" + ex.getMessage();
            UIRegistry.showError(msg);
            return;
        }
        
        mainComp = new JPanel(new BorderLayout());
        mainComp.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        if (mvParent == null)
        {
            builder.getPanel().setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        }
        if (bgColor != null)
        {
            builder.getPanel().setBackground(bgColor);
        }
        
        this.options = options;
        boolean isSingleObj                = MultiView.isOptionOn(options, MultiView.IS_SINGLE_OBJ);
        boolean createResultSetController  = MultiView.isOptionOn(options, MultiView.RESULTSET_CONTROLLER);
        boolean createViewSwitcher         = MultiView.isOptionOn(options, MultiView.VIEW_SWITCHER);
        //boolean isNewObject                = MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT);
        boolean hideSaveBtn                = MultiView.isOptionOn(options, MultiView.HIDE_SAVE_BTN);
        
        isNewlyCreatedDataObj = MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT);
        if (formValidator != null)
        {
            formValidator.setNewObj(isNewlyCreatedDataObj);
        }

        //MultiView.printCreateOptions("Creating Form "+altView.getName(), options);

        setValidator(formValidator);

        scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");

        AppPreferences.getRemote().addChangeListener("ui.formatting.viewfieldcolor", this);

        boolean addController = mvParent != null && view.getAltViews().size() > 1;

        // See if we need to add a Selector ComboBox
        isSelectorForm = StringUtils.isNotEmpty(view.getSelectorName());

        boolean addSelectorCBX = false;
        //log.debug(altView.getName()+"  "+altView.getMode()+"  "+AltViewIFace.CreationMode.EDIT);
        //if (isSelectorForm && isNewObject && altView.getMode() == AltViewIFace.CreationMode.EDIT)
        if (isSelectorForm && altView.getMode() == AltViewIFace.CreationMode.EDIT)
        {
            addSelectorCBX = true;
        }

        List<JComponent> comps = new ArrayList<JComponent>();

        int y = 1;
        // Here we create the JComboBox that enables the user to switch between forms
        // when creating a new object
        if (addSelectorCBX)
        {
            Vector<AltViewIFace> cbxList = new Vector<AltViewIFace>();
            cbxList.add(altView);
            for (AltViewIFace av : view.getAltViews())
            {
                if (av != altView && av.getMode() == AltViewIFace.CreationMode.EDIT)
                {
                    cbxList.add(av);
                }
            }
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            
            p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            selectorCBX = createComboBox(cbxList);
            selectorCBX.setRenderer(new SelectorCellRenderer());
            p.add(selectorCBX, BorderLayout.WEST);
            mainComp.add(p, BorderLayout.NORTH);
            
            if (mvParent != null)
            {
                selectorCBX.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev)
                    {
                        doSelectorWasSelected(mvParent, ev);
                    }
                });
            }
            y += 2;
        }
 
        //
        // We will add the switchable UI if we are parented to a MultiView and have multiple AltViews
        //
        if (addController) // this says we are the "root" form
        {
            boolean saveWasAdded = false;
            
            // We want it on the left side of other buttons
            // so wee need to add it before the Save button
            JComponent valInfoBtn = createValidationIndicator(getUIComponent(), formValidator);
            if (valInfoBtn != null)
            {
                comps.add(valInfoBtn);
            }

            if (createViewSwitcher) // This is passed in outside
            {
                // Now we have a Special case that when when there are only two AltViews and
                // they differ only by Edit & View we hide the switching UI unless we are the root MultiView.
                // This way when switching the Root View all the other views switch
                // (This is because they were created that way. It also makes no sense that while in "View" mode
                // you would want to switch an individual subview to a differe "mode" view than the root).

                altViewsList = new Vector<AltViewIFace>();
                
                // This will return null if it isn't suppose to have a switcher
                switcherUI = createMenuSwitcherPanel(mvParent, view, altView, altViewsList, restrictablePanel);
                
                if (altViewsList.size() > 0)
                {
                    if (altView.getMode() == AltViewIFace.CreationMode.EDIT && mvParent != null && mvParent.isTopLevel())
                    {
                        addSaveBtn();
                        comps.add(saveControl);
                        saveWasAdded = true;
                    }
                    
                    if (switcherUI != null)
                    {
                        comps.add(switcherUI);
                        
                    }
                }
                
                // rods - 07/21/08 for disabling the switcher when the form is invalid 
                if (formValidator != null && switcherUI != null)
                {
                    formValidator.addEnableItem(switcherUI, FormValidator.EnableType.ValidNotNew);
                }
            }
            
            if (!saveWasAdded && altView.getMode() == AltViewIFace.CreationMode.EDIT && mvParent != null && mvParent.isTopLevel() && !hideSaveBtn)
            {
                addSaveBtn();
                comps.add(saveControl);
            }
        }
        
        // This here because the Search mode shouldn't be combined with other modes
        if (altView.getMode() == AltViewIFace.CreationMode.SEARCH)
        {
            if (!hideSaveBtn)
            {
                saveControl = createButton(UIRegistry.getResourceString("SEARCH"), 
                        IconManager.getImage("Search", IconManager.IconSize.Std16));/*
                {
                    public void setEnabled(boolean enabled)
                    {
                        System.err.println("Save: "+enabled);
                        super.setEnabled(enabled);
                    }
                };*/
                saveControl.setOpaque(false);
                comps.add(saveControl);
                
                addSaveActionMap(saveControl);
            }

        }
        
        if (ViewFactory.isFormTransparent())
        {
            builder.getPanel().setOpaque(false);
        }
        mainComp.add(builder.getPanel(), BorderLayout.CENTER);
            
        if (comps.size() > 0 || addController || createResultSetController) 
        {
            controlPanel = new ControlBarPanel(bgColor);
            controlPanel.addComponents(comps, false); // false -> right side
            
            if (ViewFactory.isFormTransparent())
            {
                controlPanel.setOpaque(false);
            }
            
            mainComp.add(controlPanel, BorderLayout.SOUTH);
            
        }

        if (createResultSetController)
        {
            addRSController(addSearch);
            
            if (addSearch)
            {
                DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                if (tblInfo != null)
                {
                    searchName = tblInfo.getSearchDialog();
                    if (StringUtils.isEmpty(searchName))
                    {
                        searchName = ""; // Note not null but empty tells it to disable the search btn
                        
                        log.error("The Search Dialog Name is empty or missing for class["+view.getClassName()+"]");
                    }
                } else
                {
                    log.error("Couldn't find TableInfo for class["+view.getClassName()+"]");
                }
                
                if (rsController.getSearchRecBtn() != null)
                {
                    rsController.getSearchRecBtn().setEnabled(true);
                    rsController.getSearchRecBtn().addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e)
                        {
                            doSearch();
                        }
                    });
                }
            }
            
        } else if (isSingleObj)
        {
            createAddDelSearchPanel();
        }
        
        if (true)
        {
            builder.getPanel().addMouseListener(new MouseAdapter() 
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    showContextMenu(e);
                }

                @Override
                public void mouseReleased(MouseEvent e)
                {
                    showContextMenu(e);

                }
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    //FormViewObj.this.listFieldChanges();
                }
            });
        }
        
        if (rsController != null)
        {
            rsController.setNewObj(isNewlyCreatedDataObj);
        }
        
        isBuildValid = true;
    }
    
    /**
     * Register the KeyBinding short cut for the Save control
     * @param saveComp the save control
     */
    private void addSaveActionMap(final JComponent saveComp)
    {
        
        Action saveAction = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doSave();
            }
        };
        String    ACTION_KEY = "SAVE";
        KeyStroke ctrlS      = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        InputMap  inputMap   = saveComp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        inputMap.put(ctrlS, ACTION_KEY);
        ActionMap actionMap = saveComp.getActionMap();
        actionMap.put(ACTION_KEY, saveAction);
    }
    
    /**
     * @return the isBuildValid
     */
    public boolean isBuildValid()
    {
        return isBuildValid;
    }

    /**
     * Helper method for discovering the type of objects it will hold.
     */
    protected boolean isJavaCollection()
    {
        if (parentDataObj != null)
        {
            if (isJavaCollection == null)
            {
                isJavaCollection = false;
                DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(parentDataObj.getClass().getName());
                if (ti != null)
                {
                    DBRelationshipInfo ri = ti.getRelationshipByName(cellName);
                    if (ri != null)
                    {
                        //log.debug(ri.getType());
                        isJavaCollection = ri.getType() == DBRelationshipInfo.RelationshipType.OneToMany ||
                                           ri.getType() == DBRelationshipInfo.RelationshipType.ManyToMany;
                    }
                }
            }
            return isJavaCollection;
        }
        return false;
    }
    
    /**
     * @return the cellName
     */
    public String getCellName()
    {
        return cellName;
    }

    /**
     * @param mv
     * @param ev
     */
    protected void doSelectorWasSelected(final MultiView mv, final ActionEvent ev)
    {
        if (!ignoreSelection)
        {
            AltViewIFace selectedAV = (AltViewIFace)((JComboBox)ev.getSource()).getSelectedItem();
            
            // Transfer the data from old form to the new form. 
            traverseToGetDataFromForms(mvParent);
            
            Object dObj = mv.getData();
            
            mv.showView(selectedAV.getName());
            
            mv.setData(dObj);
            mv.validateAll();
        }
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setClassToCreate(java.lang.String)
     */
    public void setClassToCreate(final Class<?> classToCreate)
    {
        this.classToCreate = classToCreate;
    }

    /**
     * @return the newRecBtn
     */
    public JButton getNewRecBtn()
    {
        return newRecBtn;
    }

    /**
     * @return the delRecBtn
     */
    public JButton getDelRecBtn()
    {
        return delRecBtn;
    }

    /**
     * Creates a special drop "switcher UI" component for switching between the Viewables in the MultiView.
     * @param mvParentArg the MultiView Parent
     * @param viewArg the View
     * @param altViewArg the AltViewIFace
     * @param altViewsListArg the Vector of AltViewIFace that will contains the ones in the Drop Down
     * @return the special combobox
     */
    public static MenuSwitcherPanel createMenuSwitcherPanel(final MultiView            mvParentArg, 
                                                            final ViewIFace            viewArg, 
                                                            final AltViewIFace         altViewArg, 
                                                            final Vector<AltViewIFace> altViewsListArg,
                                                            final RestrictableUIIFace  restrictableUI)
    {
        if (AppContextMgr.isSecurityOn())
        {
            String shortName = StringUtils.substringAfterLast(viewArg.getClassName(), ".");
            if (shortName == null)
            {
                shortName = viewArg.getClassName();
            }
            
            PermissionSettings perm = SecurityMgr.getInstance().getPermission("DO."+shortName.toLowerCase());
            PermissionSettings.dumpPermissions(mvParentArg.getViewName(), perm.getOptions());
            
            if (perm.hasNoPerm() && restrictableUI != null)
            {
                restrictableUI.setRestricted(true);
            }
            
            AltViewIFace.CreationMode mode = altViewArg.getMode();
            for (AltViewIFace av : viewArg.getAltViews())
            {
                boolean isSecurityModeOK = perm.getOptions() > 0 && ((perm.isViewOnly() && (mode == AltViewIFace.CreationMode.VIEW)) ||
                                                                     !perm.isViewOnly());
                //PermissionSettings.dumpPermissions(viewArg.getClassName()+"  "+isSecurityModeOK+"  "+av.getTitle()+"  "+mode, perm.getOptions());
                if (isSecurityModeOK && (av.getMode() == mode || (mvParentArg.isTopLevel() && mvParentArg.isOKToAddAllAltViews())))
                {
                    altViewsListArg.add(av);
                }
            }
            
        } else 
        {
            // Add all the View if we are at the top level
            // If not, then we are a subform and we should only add the view that belong to our same creation mode.
            if (mvParentArg.isTopLevel() && mvParentArg.isOKToAddAllAltViews())
            {
                altViewsListArg.addAll(viewArg.getAltViews());
                
            } else
            {
                AltViewIFace.CreationMode mode = altViewArg.getMode();
                for (AltViewIFace av : viewArg.getAltViews())
                {
                    if (av.getMode() == mode)
                    {
                        altViewsListArg.add(av);
                    }
                }
            }
        }

        return altViewsListArg.size() > 1 ? new MenuSwitcherPanel(mvParentArg, altViewArg, altViewsListArg) : null;
    }
    
    /**
     * @return the saveAndNew
     */
    public boolean isSaveAndNew()
    {
        return saveAndNew;
    }

    /**
     * @param saveAndNew the saveAndNew to set
     */
    public void setSaveAndNew(boolean saveAndNew)
    {
        this.saveAndNew = saveAndNew;
        if (saveControl instanceof JButton)
        {
            ((JButton)saveControl).setText(getResourceString(saveAndNew ? "SAVE_AND_NEW_BTN" : "Save"));
        }
    }

    /**
     * Adds the Save Button to the form.
     */
    protected void addSaveBtn()
    {
        JButton saveBtn;
        boolean doDebug = false;
        if (!doDebug)
        {
            saveBtn= createButton(UIRegistry.getResourceString("SAVE"));
        } else
        {
            saveBtn = new JButton(UIRegistry.getResourceString("SAVE")) {
                public void setEnabled(boolean enabled)
                {
                    //if (enabled) log.debug("******* "+formValidator.getName()+"  Save: "+enabled);
                    super.setEnabled(enabled);
                }
            };
        }
        saveBtn.setEnabled(false);

        saveBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                if (saveObject() && saveAndNew)
                {
                   createNewDataObject(true);
                }
            }
        });
        saveControl = saveBtn;
        addSaveActionMap(saveControl);
        
        if (formValidator != null)
        {
            formValidator.setSaveComp(saveBtn, FormValidator.EnableType.ValidAndChangedItems);
        }
    }

    /**
     * Returns the CarryForwardInfo Object for the Form.
     * @return the CarryForwardInfo Object for the Form
     */
    private CarryForwardInfo getCarryForwardInfo()
    {
        if (carryFwdInfo == null)
        {
            try
            {
                Class<?> classObj = Class.forName(formViewDef.getClassName());
                carryFwdInfo = new CarryForwardInfo(classObj, this, formViewDef);

            } catch (ClassNotFoundException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormViewObj.class, ex);
                throw new RuntimeException(ex);
            }
        }
        return carryFwdInfo;
    }

    /**
     * Returns whether this form is doing Carry Forward.
     * @return whether this form is doing Carry Forward
     */
    public boolean isDoCarryForward()
    {
        return doCarryForward;
    }

    /**
     * Turns on/off Carry Forward for this form
     * @param doCarryForward true - on, false - off
     */
    public void setDoCarryForward(boolean doCarryForward)
    {
        this.doCarryForward = doCarryForward;
        adjustActionsAndMenus(true);
    }
    
    /**
     * @return returns whether there is any set up information.
     */
    public boolean isCarryForwardConfgured()
    {
        if (carryFwdInfo != null)
        {
            return carryFwdInfo.hasConfiguredFields();
        }
        return false;
    }
    
    /**
     * Adjust the Action and MenuItem for CarryForward.
     * @param isVisible whether is is visible
     */
    private void adjustActionsAndMenus(final boolean isVisible)
    {
        boolean isConfiged = isCarryForwardConfgured() && isVisible;
        enableActionAndMenu("CarryForward", isConfiged, isConfiged);
        
        enableActionAndMenu("ConfigCarryForward", isVisible, null);
        
        boolean doAutoNum = isAutoNumberOn() && isEditing && isVisible;
        enableActionAndMenu("AutoNumbering", doAutoNum, doAutoNum);
        
        enableActionAndMenu("SaveAndNew", isVisible, null);
    }
    
    /**
     * Shows a Dialog to setup Carry Forward. 
     * The hard part is figuring out which fields are candidates for Carry Forward.
     */
    public void configureCarryForward()
    {
        CarryForwardInfo carryForwardInfo = getCarryForwardInfo();
        
        DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
        
        Vector<FVOFieldInfo> itemLabels    = new Vector<FVOFieldInfo>();
        Vector<FVOFieldInfo> selectedItems = new Vector<FVOFieldInfo>(carryForwardInfo.getFieldList());

        // This next section loops through all the UI components in the form that has an ID
        // It checks to make sure that it is a candidate for CF
        Vector<String> ids = new Vector<String>();
        getFieldIds(ids, true); // true means return all the UI components with ids not just the fields
        for (String id : ids)
        {
            FVOFieldInfo fieldInfo = getFieldInfoForId(id);
            String       fieldName = fieldInfo.getFormCell().getName();
            DBFieldInfo  fi        = ti != null ? ti.getFieldByName(fieldName) : null;
            
            fieldInfo.setFieldInfo(fi);
                    
            //log.debug(fieldName);

            // Start by assuming it is OK to be added
            boolean isOK = true;
            if (fieldInfo.getFormCell() instanceof FormCellFieldIFace)
            {
                // Only the ones that are editable.
                FormCellFieldIFace fcf = (FormCellFieldIFace)fieldInfo.getFormCell();
                if (fcf.isReadOnly())
                {
                    isOK = false; 
                } else 
                {
                    DBInfoBase infoBase = fieldInfo.getFieldInfo();
                    if (infoBase instanceof DBFieldInfo)
                    {
                        if (fi.isUnique())
                        {
                            isOK = false;
                            
                        } else if (fi.getFormatter() != null && fi.getFormatter().isIncrementer())
                        {
                            isOK = false;
                        }
                    } else
                    {
                        log.debug("Skipping "+infoBase);
                    }
                }
            } else
            {
                log.debug("Skipping "+fieldInfo.getFormCell());
            }
            
            // At this point we have weeded out any readonly/autoinc "fields" and we need to get a label for the field
            // And weed out any SubViews.
            if (isOK)
            {
                // Check to see if the field has a label
                String label = null;
                FVOFieldInfo labelInfo = getLabelInfoFor(id);
                if (labelInfo != null)
                {
                    if (!(fieldInfo.getFormCell() instanceof FormCellLabel))
                    {
                        label = ((FormCellLabel)labelInfo.getFormCell()).getLabel();
                    }
                }
                
                log.error("Couldn't find field ["+fieldName+"] in ["+ti.getTitle()+"]");
                
                // Now we go get the DBFieldInfo and DBRelationshipInfo and check to make
                // that the field or Relationship is still a candidate for CF
                DBInfoBase infoBase = null;
                if (ti != null)
                {
                    if (fi != null)
                    {
                        infoBase = fi;
                        
                        // Skip any fields that are AutoNumbered
                        if (fieldInfo.getComp() instanceof AutoNumberableIFace)
                        {
                            isOK = !((AutoNumberableIFace)fieldInfo.getComp()).isFormatterAutoNumber();
                        } else
                        {
                            isOK = true;
                        }
                        
                    } else
                    {
                        DBRelationshipInfo ri = ti.getRelationshipByName(fieldName);
                        if (ri != null)
                        {
                            infoBase = ri;
                            
                            // If the field is a OneToMany then it is a s Set
                            // and we need to make sure the items in the set are clonable
                            // if they are not clonable then we can't include this in 
                            // the Carry Forward list
                            Class<?> dataClass = ri.getDataClass();
                            if (ri.getType() == DBRelationshipInfo.RelationshipType.OneToMany)
                            {
                                try
                                {
                                    Method method = dataClass.getMethod("clone", new Class<?>[] {});
                                    // Pretty much every Object has a "clone" method but we need 
                                    // to check to make sure it is implemented by the same class of 
                                    // Object that is in the Set.
                                    isOK = method.getDeclaringClass() == dataClass;
                                    
                                } catch (Exception ex) 
                                {
                                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormViewObj.class, ex);
                                    isOK = false; // this really shouldn't happen
                                }
                            }
                            
                        } else if (fieldInfo.getUiPlugin() != null)
                        {
                            if (StringUtils.isNotEmpty(label))
                            {
                                label = fieldInfo.getUiPlugin().getTitle();
                            }
                            isOK = fieldInfo.getUiPlugin().canCarryForward();
                        } else
                        {
                            log.error("Couldn't find field ["+fieldName+"] in ["+ti.getTitle()+"]");
                            isOK = false;  
                        }
                    }
                    
                    if (isOK)
                    {
                        if (infoBase != null && StringUtils.isEmpty(label))
                        {
                            label = infoBase.getTitle();
                        }
                        fieldInfo.setLabel(label);
                        itemLabels.add(fieldInfo);
                        fieldInfo.setFieldInfo(infoBase);
                    }
                }
            }
        }
        
        Collections.sort(itemLabels, new Comparator<FVOFieldInfo>() {
            @Override
            public int compare(FVOFieldInfo o1, FVOFieldInfo o2)
            {
                if (o1.getLabel() == null || o2.getLabel() == null)
                {
                    return 1;
                }
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });
        
        ToggleButtonChooserDlg<FVOFieldInfo> dlg = new ToggleButtonChooserDlg<FVOFieldInfo>((Frame)UIRegistry.getTopWindow(),
                "CONFIG_CARRY_FORWARD_TITLE", itemLabels);
        dlg.setUseScrollPane(true);
        dlg.setAddSelectAll(true);
        dlg.setSelectedObjects(selectedItems);
        UIHelper.centerAndShow(dlg);
        
        if (!dlg.isCancelled())
        {
            carryForwardInfo.add(dlg.getSelectedObjects());
        }
    }
    
    /**
     * Toggles Carry Forward State (Turning it on and off).
     */
    public void toggleCarryForward()
    {
        setDoCarryForward(!isDoCarryForward());
        
        JCheckBoxMenuItem mi = (JCheckBoxMenuItem)UIRegistry.get("CarryForward");
        if (mi != null)
        {
            mi.setSelected(isDoCarryForward());
        }
    }
    
    /**
     * Shows Parent Form's Context Menu.
     * @param e the mouse event
     */
    protected void showContextMenu(MouseEvent e)
    {
        if (e.isPopupTrigger() && mvParent != null && mvParent.isTopLevel() && isEditing)
        {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem(UIRegistry.getResourceString("CONFIG_CARRY_FORWARD_MENU"));
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ex)
                {
                    configureCarryForward();
                }
            });
            popup.add(menuItem);

            JCheckBoxMenuItem chkMI = new JCheckBoxMenuItem(UIRegistry.getResourceString("CARRY_FORWARD_CHECKED_MENU"));
            chkMI.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ex)
                {
                    toggleCarryForward();
                }
            });
            chkMI.setSelected(isCarryForwardConfgured() && isDoCarryForward());
            chkMI.setEnabled(isCarryForwardConfgured());
            popup.add(chkMI);

            popup.addSeparator();
            chkMI = new JCheckBoxMenuItem(UIRegistry.getAction("AutoNumbering"));
            /*chkMI.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ex)
                {
                    toggleAutoNumberOnOffState();
                }
            });*/
            chkMI.setSelected(isAutoNumberOn);
            popup.add(chkMI);

            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    
    /**
     * Toggles Auto-Numbering mode (turns on or off).
     */
    public void toggleAutoNumberOnOffState()
    {
        isAutoNumberOn = !isAutoNumberOn;
        
        JCheckBoxMenuItem mi = (JCheckBoxMenuItem)UIRegistry.get("AutoNumbering");
        if (mi != null)
        {
            mi.setSelected(isAutoNumberOn);
        }
        
        for (FVOFieldInfo fieldInfo : controlsById.values())
        {
            Component comp = fieldInfo.getComp();
            if (comp instanceof AutoNumberableIFace)
            {
                ((AutoNumberableIFace)comp).setAutoNumberEnabled(isAutoNumberOn);
            }
        }
    }
    
    /**
     * @return the entire list of controls.
     */
    public List<FVOFieldInfo> getComps()
    {
        return compsList;
    }

    /**
     * @return the isAutoNumberOn
     */
    public boolean isAutoNumberOn()
    {
        return isAutoNumberOn;
    }

    /**
     * Creates the JButton that displays the current state of the forms validation
     * @param window
     * @param validator
     * @return
     */
    public static JButton createValidationIndicator(final Component comp, final FormValidator validator)
    {
        if (validator != null)
        {
            JButton validationInfoBtn = new JButton(IconManager.getIcon("ValidationValid"));
            validationInfoBtn.setOpaque(false);
            validationInfoBtn.setToolTipText(getResourceString("SHOW_VALIDATION_INFO_TT"));
            validationInfoBtn.setMargin(new Insets(1,1,1,1));
            validationInfoBtn.setBorder(BorderFactory.createEmptyBorder());
            validationInfoBtn.setFocusable(false);
            validationInfoBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    showValidationInfo(comp instanceof Window ? (Window)comp : UIHelper.getWindow(comp), validator);
                }
            });
            validator.setValidationBtn(validationInfoBtn);
            return validationInfoBtn;
        }
        // else
        return null;
    }

    /**
     * @return the mvParent
     */
    public MultiView getMVParent()
    {
        return mvParent;
    }

    /**
     * Static Helper method for showing Validation info.
     * @param viewable the view to show info for.
     */
    protected static void showValidationInfo(final Window window, final FormValidator validator)
    {
        final FormValidatorInfo formInfo = new FormValidatorInfo(validator);

        PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("p", "p,5px,p"));
        panelBuilder.add(formInfo, cc.xy(1,1));
        
        CustomDialog dialog;
        if (window instanceof JDialog)
        {
            dialog = new CustomDialog((Dialog)window, validator.getName(), true, CustomDialog.OK_BTN, panelBuilder.getPanel())
            {
                @Override
                public void setVisible(final boolean visible)
                {
                    if (!visible)
                    {
                        formInfo.cleanUp();
                    } 
                    super.setVisible(visible);
                }
            };
        } else
        {
            dialog = new CustomDialog((Frame)window, validator.getName(), true, CustomDialog.OK_BTN, panelBuilder.getPanel())
            {
                @Override
                public void setVisible(final boolean visible)
                {
                    if (!visible)
                    {
                        formInfo.cleanUp();
                    } 
                    super.setVisible(visible);
                }
            }; 
        }
        dialog.setOkLabel(getResourceString("CLOSE"));
        UIHelper.centerAndShow(dialog);
        dialog.dispose();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#aboutToShow(boolean)
     */
    public void aboutToShow(final boolean show)
    {
        isShowing = show;
        
        /*if (origDataSet != null && list != null && origDataSet.size() != list.size())
        {
            // XXX Ok here we know new items have been added
            // so we need to resort (maybe) but certainly need to re-adjust the RecordSet controller.
            //
            // Actually check the sizes isn't enough, we need to really know if there was a change in the list
        }*/
        
        if (formValidator != null)
        {
            formValidator.validateForm();
        }
        
        if (switcherUI != null)
        {
            ignoreSelection = true;
            switcherUI.set(altView);
            ignoreSelection = false;
        }
        
        if (selectorCBX != null)
        {
            ignoreSelection = true;
            selectorCBX.setEnabled(true);
            selectorCBX.setSelectedIndex(0);
            ignoreSelection = false;
        }
        
        for (MultiView mv : kids)
        {
            mv.aboutToShow(show);
        }
        
        if (mvParent != null && mvParent.isTopLevel() && isEditing)
        {
            if (show)
            {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run()
                    {
                        //log.error(hashCode()+"  "+show);
                        adjustActionsAndMenus(true);
                    }
                });
            } else
            {
                //log.error(hashCode()+"  "+show);
                adjustActionsAndMenus(false);
            }
        }
        
        // Give all the ResultSetController btns one last chance with all the data there to be enabled
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                updateControllerUI();
            }
        });

        // Moving this to the MultiView
        /*if (show)
        {
            log.debug("Dispatching a Data_Entry/ViewWasShown command/action");
            CommandDispatcher.dispatch(new CommandAction("Data_Entry", "ViewWasShown", this));
        }*/
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getViewDef()
     */
    public FormViewDef getViewDef()
    {
        return formViewDef;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getAltView()
     */
    public AltViewIFace getAltView()
    {
        return altView;
    }
    
    /**
     * Returns the name of the form from the FormView
     * @return the name of the form from the FormView
     */
    public String getName()
    {
        return formViewDef.getName();
    }

    /**
     * Returns the current Data Object, which means the actual object if it is not a list
     * or the current object in the list
     * @return Returns the current Data Object, which means the actual object if it is not a list
     * or the current object in the list
     */
    public Object getCurrentDataObj()
    {
        return dataObj;
    }


    /**
     * Return list of data objects if this is a recordset
     * @return the list of data objects
     */
    public List<?> getDataList()
    {
        return list;
    }

    /**
     * Sets the component into the object
     * @param formComp the UI component that represents this viewable
     */
    public void setFormComp(final JComponent formComp)
    {
        // Remove existing component
        if (this.formComp != null)
        {
            mainComp.remove(this.formComp);
        }
        
        // add new component
        if (MultiView.isOptionOn(options, MultiView.NO_SCROLLBARS))
        {
            if (ViewFactory.isFormTransparent())
            {
                formComp.setOpaque(false);
            }
            this.mainComp.add(formComp, BorderLayout.CENTER);
            this.formComp = formComp;
            
        } else
        {
            JScrollPane scrollPane = new JScrollPane(formComp, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getVerticalScrollBar().setUnitIncrement(10);
            
            scrollPane.setBorder(null);
            this.mainComp.add(scrollPane, BorderLayout.CENTER); 
            this.formComp = scrollPane;
            
            if (ViewFactory.isFormTransparent())
            {
                scrollPane.setOpaque(false);
                scrollPane.getViewport().setOpaque(false);
            }
        }
        
        if (businessRules != null && !hasInitBR)
        {
            businessRules.initialize(this);
            hasInitBR = true;
        }

        // This is needed to make the form layout correctly
        //XXX I hate that I have to do this
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                //mainComp.invalidate();
                //mainComp.validate();
                //mainComp.doLayout();
                UIRegistry.forceTopFrameRepaint();
            }
        });
    }
    
    /**
     * Returns the panel that contains all the controls.
     * @return the panel that contains all the controls
     */
    public JPanel getPanel()
    {
        return builder.getPanel();
    }

    /**
     * Adds child to mvParent
     * @param child the child to be added
     */
    public void addChild(final MultiView child)
    {
        kids.add(child);
    }

    /**
     * Sets the multiview if it is owned or mvParented by it.
     * @param cbx combobox to add a listener to
     */
    public void addMultiViewListener(final JComboBox cbx)
    {
        class MVActionListener implements ActionListener
        {
            protected MultiView mv;
            public MVActionListener(final MultiView mv)
            {
                this.mv = mv;
            }
            public void actionPerformed(ActionEvent ae)
            {
                doSelectorWasSelected(mv, ae);
            }
        }

        if (cbx != null)
        {
            cbx.addActionListener(new MVActionListener(mvParent));
        }
    }

    /**
     * Set the form formValidator and hooks up the root form to listen also.
     * @param formValidator the formValidator
     */
    protected void setValidator(final FormValidator formValidator)
    {
        if (this.formValidator != null)
        {
            this.formValidator.removeValidationListener(this);
        }
        
        this.formValidator = formValidator;

        // If there is a form validator and this is not the "root" form 
        // then add this form as a listener to the validator AND
        // make the root form a listener to this validator.
        if (formValidator != null && mvParent != null)
        {
            formValidator.addValidationListener(this);
        }
    }

    /**
     * Adds new child object to its parent to a Set
     * @param newDataObj the new object to be added to a Set
     */
    protected static void removeFromParent(final MultiView mvParent, 
                                           final Object    parentDataObjArg, 
                                           final String    cellNameArg, 
                                           final Object    oldDataObjArg)
    {
        if (oldDataObjArg != null)
        {
            if (parentDataObjArg != null)
            {
                log.debug("Removing "+oldDataObjArg+" "+oldDataObjArg.getClass().getSimpleName()+" from "+parentDataObjArg);
                
                if (parentDataObjArg instanceof FormDataObjIFace && oldDataObjArg instanceof FormDataObjIFace)
                {
                    boolean addSearch = mvParent != null && MultiView.isOptionOn(mvParent.getOptions(), MultiView.ADD_SEARCH_BTN);
                    
                    ((FormDataObjIFace)parentDataObjArg).removeReference((FormDataObjIFace)oldDataObjArg, cellNameArg, addSearch);
                    if (addSearch && mvParent != null && ((FormDataObjIFace)oldDataObjArg).getId() != null)
                    {
                        mvParent.getTopLevel().addToBeSavedItem(oldDataObjArg);
                    }
                    
                } else
                {
                    throw new RuntimeException("Hmmm, I don't think we soud be here.");
                }
            }
        } else
        {
            throw new RuntimeException("Hmmm,Why are we trying to delete a NULL object?");
        }
    }

    /**
     * Walks the MultiView hierarchy and has them transfer their data from the UI to the DB Object
     * @param parentMV the parent MultiView
     */
    public void traverseToGetDataFromForms()
    {
        traverseToGetDataFromForms(mvParent);
    }

    /**
     * Walks the MultiView hierarchy and has them transfer their data from the UI to the DB Object
     * @param parentMV the parent MultiView
     */
    public static void traverseToGetDataFromForms(final MultiView parentMV)
    {
        for (MultiView mv : parentMV.getKids())
        {
            mv.getDataFromUI();
            traverseToGetDataFromForms(mv);
        }
    }

    /**
     * Sets the parent MultiView and all of its children. The last argument indicates whether
     * it should have the children walk there children (a deep recurse).
     * that the form is new
     * @param parentMV the parent MultiView
     * @param isNewForm whether the form is now in "new data input" mode
     * @param traverseKids whether the MultiView should traverse into the children MultiViews (deep recurse)
     */
    protected void traverseToToSetAsNew(final MultiView parentMV, 
                                        final boolean isNewForm,
                                        final boolean traverseKids)
    {
        // This call just sets all the Viewable for the MV, unless traverseKids is to true
        // then it walks the children MVs
        parentMV.setIsNewForm(isNewForm, traverseKids);
        
        // if traverseKids is true then the kids have already been walked
        // but if it is false then we need to walk the immediate kids
        if (!traverseKids)
        {
            for (MultiView mv : parentMV.getKids())
            {
                mv.setIsNewForm(isNewForm, false);
            }
        }
    }
    
    /**
     * Sets the parent MultiView and all of its children. The last argument indicates whether
     * it should have the children walk there children (a deep recurse).
     * that the form is new
     * @param parentMV the parent MultiView
     * @param isNewForm whether the form is now in "new data input" mode
     * @param traverseKids whether the MultiView should traverse into the children MultiViews (deep recurse)
     */
    protected void traverseToSetModified(final MultiView parentMV)
    {
        if (parentMV != null)
        {
            for (Viewable v : parentMV.getViewables())
            {
                FormValidator fv = v.getValidator();
                if (fv != null && fv.hasChanged())
                {
                    // They might be different because of a previous save or merge
                    boolean doSetCreate = parentMV.getData() instanceof FormDataObjIFace && ((FormDataObjIFace)parentMV.getData()).getId() == null;
                    FormHelper.updateLastEdittedInfo(parentMV.getData(), doSetCreate);
                    if (parentMV.getData() != v.getDataObj())
                    {
                        doSetCreate = v.getDataObj() instanceof FormDataObjIFace && ((FormDataObjIFace)v.getDataObj()).getId() == null;
                        FormHelper.updateLastEdittedInfo(v.getDataObj(), doSetCreate);
                    }
                }
            }
            
            // if traverseKids is true then the kids have already been walked
            for (MultiView mv : parentMV.getKids())
            {
                traverseToSetModified(mv);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#isDataCompleteAndValid(boolean)
     */
    public boolean isDataCompleteAndValid(final boolean throwAwayOnDiscard)
    {
        //log.debug((formValidator != null) +" "+ formValidator.hasChanged() +"  "+mvParent.isTopLevel() +" "+ mvParent.hasChanged());
        
        // Figure out if it is New and whether it has changed or is incomplete
        boolean isNewAndComplete = true;
        if (mvParent != null)
        {
            Object topParentData = mvParent.getTopLevel().getData();
            if (topParentData != null && topParentData instanceof FormDataObjIFace)
            {
                if (((FormDataObjIFace)topParentData).getId() == null)
                {
                    if (formValidator != null && dataObj != null)
                    {
                        isNewAndComplete = formValidator.isFormValid();
                    }
                }
            }
        }
        
        //log.debug("Form     Val: "+(formValidator != null && formValidator.hasChanged()));
        //log.debug("mvParent Val: "+(mvParent != null && mvParent.isTopLevel() && mvParent.hasChanged()));
        
        //if ((formValidator != null && formValidator.hasChanged()) ||
        //    (mvParent != null && mvParent.isTopLevel() && mvParent.hasChanged()))
        if (mvParent != null && mvParent.isTopLevel() && mvParent.hasChanged())
        {
            String title = null;
            if (dataObj != null)
            {
                if (tableInfo == null)
                {
                    tableInfo = DBTableIdMgr.getInstance().getByShortClassName(dataObj.getClass().getSimpleName());
                }
                
                title = tableInfo != null ? tableInfo.getTitle() : null;
                
                if (StringUtils.isEmpty(title))
                {
                    title = UIHelper.makeNamePretty(dataObj.getClass().getSimpleName());
                }
            }
            
            if (StringUtils.isEmpty(title))
            {
                title = "data"; // I18N, not really sure what to put here.
            }
            
            // For the DISCARD
            // Since JOptionPane doesn't have a YES_CANCEL_OPTION 
            // I have to use YES_NO_OPTION and since this is a Discard question
            // the rv has completely different meanings:
            // YES -> Means don't save (Discard) and close dialog (return true)
            // NO  -> Means do nothing so return false
            
            String[] optionLabels;
            int      dlgOptions;
            int      defaultRV;
            if (!isNewAndComplete || (formValidator != null && !formValidator.isFormValid()))
            {
                dlgOptions = JOptionPane.YES_NO_OPTION;
                optionLabels = new String[] {getResourceString("DiscardChangesBtn"), 
                                             getResourceString("CANCEL")};
                defaultRV = JOptionPane.NO_OPTION;
            } else
            {
                dlgOptions = JOptionPane.YES_NO_CANCEL_OPTION;
                optionLabels = new String[] {getResourceString("SaveChangesBtn"), 
                                             getResourceString("DiscardChangesBtn"), 
                                             getResourceString("CANCEL")};
                defaultRV = JOptionPane.CANCEL_OPTION;
            }
            
            int rv = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(),
                        isNewAndComplete ? UIRegistry.getLocalizedMessage("DiscardChanges", title) : UIRegistry.getLocalizedMessage("SaveChanges", title),
                        isNewAndComplete ? getResourceString("DiscardChangesTitle") : getResourceString("SaveChangesTitle"),
                        dlgOptions,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        optionLabels,
                        optionLabels[0]);
        
            if (rv == JOptionPane.CLOSED_OPTION)
            {
                rv = defaultRV;
            }

            if ( dlgOptions == JOptionPane.YES_NO_OPTION)
            {
                if (rv == JOptionPane.YES_OPTION)
                {
                    discardCurrentObject(throwAwayOnDiscard);
                    return true;
                    
                } else if (rv == JOptionPane.NO_OPTION)
                {
                  return false;
                }
                
            } else
            {
                if (rv == JOptionPane.YES_OPTION)
                {
                    return saveObject();
                    
                } else if (rv == JOptionPane.CANCEL_OPTION)
                {
                    return false; 
                    
                } else if (rv == JOptionPane.NO_OPTION)
                {
                    // NO means Discard
                    discardCurrentObject(throwAwayOnDiscard);
                    return true;
                }
            }
            
            
        } else
        {
            return true;
        }
        return isNewAndComplete;
    }
    
    /**
     * Increments to the next number in the series.
     */
    public void updateAutoNumbers()
    {
        if (isAutoNumberOn)
        {
            for (FVOFieldInfo fieldInfo : controlsById.values())
            {
                Component comp = fieldInfo.getComp();
                if (comp instanceof AutoNumberableIFace && comp.isEnabled())
                {
                    ((AutoNumberableIFace)comp).updateAutoNumbers();
                }
            }
        }
    }
    
    /**
     * Returns whether a field with a given name or ID is auto-incremented.
     * @param fieldInfo the field info
     * @return true it is, false it isn't
     */
    public boolean isFieldAutoNumbered(final FVOFieldInfo fieldInfo)
    {
        if (fieldInfo != null)
        {
            if (fieldInfo.getFormCell() instanceof FormCellFieldIFace)
            {
                FormCellFieldIFace fcf = (FormCellFieldIFace)fieldInfo.getFormCell();
                UIFieldFormatterIFace uiff = UIFieldFormatterMgr.getInstance().getFormatter(fcf.getUIFieldFormatterName());
                if (uiff != null)
                {
                    return uiff.getAutoNumber() != null;
                }
            }
        }
        return false;
    }
    
    /**
     * Asks the Business Rules if a SubViewIFace should have a new record created. 
     */
    public void initSubViews()
    {
        if (businessRules != null)
        {
            for (FVOFieldInfo fieldInfo : controlsById.values())
            {
                if (fieldInfo.getFormCell() instanceof FormCellSubViewIFace &&
                    fieldInfo.getComp() instanceof MultiView &&
                    businessRules.shouldCreateSubViewData(fieldInfo.getName()))
                {
                    MultiView   mv = ((MultiView)fieldInfo.getComp());
                    FormViewObj fvo =  mv.getCurrentViewAsFormViewObj();
                    if (fvo != null)
                    {
                        if (fvo.getRsController() != null && 
                            fvo.getRsController().getNewRecBtn() != null)
                        {
                            fvo.getRsController().getNewRecBtn().doClick();
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns whether a field with a given name is auto-incremented.
     * @param name the name of the field
     * @return true it is, false it isn't
     */
    public boolean isFieldAutoNumberedByName(final String name)
    {
        return isFieldAutoNumbered(controlsByName.get(name));
    }

    /**
     * Returns whether a field with a given ID is auto-incremented.
     * @param devName the ID of the field
     * @return true it is, false it isn't
     */
    public boolean isFieldAutoNumberedById(final String id)
    {
        return isFieldAutoNumbered(controlsById.get(id));
    }

    /**
     * Creates a new Record and adds it to the List and dataSet if necessary
     * @param doSetIntoAndValidateArg whether the new data object should be set into the form and validated.
     */
    protected void createNewDataObject(final boolean doSetIntoAndValidateArg)
    {
        //log.debug("createNewDataObject " + this.getView().getName());

        if (!isDataCompleteAndValid(false))
        {
            return;
        }
        
        if (list != null && !list.isEmpty())
        {
            getDataFromUI();
        }
        
        doSetIntoAndValidate = doSetIntoAndValidateArg;
        
        // Check to see if the business rules will be creating the object
        // if so the BR will then call setNewObject
        if (businessRules != null && businessRules.canCreateNewDataObject())
        {
            businessRules.createNewObj(true, null);
            
        } else
        {
            boolean shouldDoCarryForward = doCarryForward && carryFwdDataObj != null && carryFwdInfo != null;
            
            //log.info("createNewDataObject "+hashCode() + " Session ["+(session != null ? session.hashCode() : "null")+"] ");
            FormDataObjIFace obj;
            if (classToCreate != null)
            {
                obj = FormHelper.createAndNewDataObj(classToCreate, !shouldDoCarryForward);
            } else
            {
                obj = FormHelper.createAndNewDataObj(view.getClassName(), !shouldDoCarryForward);
            }
            
            setNewObject(obj);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setNewObject(edu.ku.brc.af.ui.forms.FormDataObjIFace)
     */
    public void setNewObject(final FormDataObjIFace obj)
    {
        if (obj == null)
        {
            return;
        }
        
        oldDataObj = dataObj;
        dataObj = obj;
        
        UIValidator.setIgnoreAllValidation(this, true);
        for (FVOFieldInfo fieldInfo : controlsById.values())
        {
            Component comp = fieldInfo.getComp();
            if (comp != null && comp instanceof UIValidatable)
            {
                ((UIValidatable)comp).reset();
            }
        }
        UIValidator.setIgnoreAllValidation(this, false);
        
        for (FVOFieldInfo fi : controlsByName.values())
        {
            if (fi.getComp() instanceof EditViewCompSwitcherPanel)
            {
                ((EditViewCompSwitcherPanel)fi.getComp()).putIntoEditMode();
            }
        }
        
        boolean shouldDoCarryForward = doCarryForward && carryFwdDataObj != null && carryFwdInfo != null;
        
        // The order needs to be set here because some Sets are TreSets which
        // require the ordinal to be set BEFORE it is added to the TreeSet
        if (obj instanceof Orderable)
        {
            // They really should all be Orderable, 
            // but just in case we check each one.
            int maxOrder = -1;
            for (Object listObj : list)
            {
                if (listObj instanceof Orderable)
                {
                    maxOrder = Math.max(((Orderable)listObj).getOrderIndex(), maxOrder);
                }
            }
            
            ((Orderable)obj).setOrderIndex(maxOrder+1);
        }
        
        boolean isManyToOne  = false;
        if (parentDataObj instanceof FormDataObjIFace)
        {
            boolean isASingleObj = false;
            DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(parentDataObj.getClass().getName());
            if (ti != null)
            {
                DBRelationshipInfo ri = ti.getRelationshipByName(cellName);
                if (ri != null)
                {
                    //log.debug(ri.getType());
                    if (ri.getType() == DBRelationshipInfo.RelationshipType.ManyToOne)
                    {
                        // not sure this is right anymore - rods 03/14/08
                        //isASingleObj = true;
                        doSetIntoAndValidate = true;
                        isManyToOne = true;
                        
                    } else if (ri.getType() == DBRelationshipInfo.RelationshipType.ZeroOrOne)
                    {
                        doSetIntoAndValidate = true;
                    }
                }
            }
            
            if (isASingleObj)
            {
                obj.addReference(((FormDataObjIFace)parentDataObj), cellName);
                doSetIntoAndValidate = true;
                
            } else
            {
                ((FormDataObjIFace)parentDataObj).addReference(obj, cellName);
            }
            
        } else
        {
            FormHelper.addToParent(parentDataObj, obj);
        }

        boolean didCarryForward = false;
        if (shouldDoCarryForward)
        {
            // We don't need a Session when we are not cloning sets.
            if (false)
            {
                carryFwdInfo.carryForward(businessRules, carryFwdDataObj, obj);
                
            } else
            {
                DataProviderSessionIFace sessionLocal = null;
                try
                {
                    sessionLocal = DataProviderFactory.getInstance().createSession();
                    sessionLocal.attach(carryFwdDataObj);
                    carryFwdInfo.carryForward(businessRules, carryFwdDataObj, obj);
                    didCarryForward = true;
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormViewObj.class, ex);
                    ex.printStackTrace();
                    
                } finally
                {
                    if (sessionLocal != null)
                    {
                        sessionLocal.close();
                    }
                }
            }
        }
        
        if (businessRules != null)
        {
            //businessRules.addChildrenToNewDataObjects(obj);
        }
        
        if (carryFwdDataObj == null && oldDataObj != null)
        {
            carryFwdDataObj = oldDataObj;
        }
        
        if (formValidator != null && formValidator.hasChanged())
        {
            formValidator.setHasChanged(false);
        }
        
        dataObj = obj;
       
        if (list != null)
        {
            list.add(obj);
            int len = list.size();
            rsController.setLength(len);
            rsController.setIndex(len-1, false);
            
        } else if (mvParent.getMultiViewParent() != null)
        {
            // NOTE: This is primarily for single objects that are in a sub-form.
            // Not calling setHasNewData because we need to traverse and setHasNewData doesn't
            
            ///////////////////////////////
            // 02/03/09 rods - set second argument to false and that seems to fix several bugs
            // that was causing data to disappear because it was resetting the fields in the parent form.
            
            //traverseToToSetAsNew(mvParent.getMultiViewParent(), false, false); // don't traverse deeper than our immediate children
            indexChanged(-1);
        }
        
        if (recordSetItemList != null)
        {
            RecordSetItemIFace recordSetItem = recordSet.addItem(-1);
            recordSetItemList.add(recordSetItem);
        }
        
        isNewlyCreatedDataObj = true;
        if (rsController != null)
        {
            rsController.setNewObj(isNewlyCreatedDataObj);
        }
        if (formValidator != null)
        {
            formValidator.setNewObj(isNewlyCreatedDataObj);
        }
        
        // Not calling setHasNewData because we need to traverse and setHasNewData doesn't
        traverseToToSetAsNew(mvParent, true, false); // don't traverse deeper than our immediate children
        
        updateControllerUI();

        if (doSetIntoAndValidate)
        {
            if (list == null) // skip doing the setDataIntoUI because changing the index of will do it.
            {
                this.setDataIntoUI();
            }
    
            if (formValidator != null)
            {
                formValidator.validateForm();

                // OK, here we need to figure out if there are any required fields
                // that are empty. If there are than we need to tell the validator and the parent
                // validators that the forms are changed and are incomplete.
                boolean hasEmptyRequiredField = false;
                boolean hasRequiredFields     = false;
                for (FVOFieldInfo fieldInfo : controlsById.values())
                {
                    if (fieldInfo.isOfType(FormCellIFace.CellType.field))
                    {
                        Component comp = fieldInfo.getComp();
                        if (comp instanceof UIValidatable &&
                            comp instanceof GetSetValueIFace)
                        {
                            if (((UIValidatable)comp).isRequired())
                            {
                                hasRequiredFields = true;
                                if (((GetSetValueIFace)comp).getValue() == null)
                                {
                                    hasEmptyRequiredField = true;
                                }
                            }
                        }
                    }
                }
                
                // OK, now tell all the validators up the chain that 
                // things have changed and are incomplete.
                if (hasEmptyRequiredField)
                {
                    formValidator.setFormValidationState(UIValidatable.ErrorType.Incomplete);
                    formValidator.setHasChanged(true);
                    formValidator.updateValidationBtnUIState();
                    
                    if (mvParent != null)
                    {
                        MultiView mvp = mvParent;
                        do
                        {
                            mvp.getCurrentValidator().setFormValidationState(UIValidatable.ErrorType.Incomplete);
                            mvp.getCurrentValidator().setHasChanged(true);
                            mvParent.getTopLevel().getCurrentViewAsFormViewObj().getValidator().updateValidationBtnUIState();
                            mvp = mvp.getMultiViewParent();
                        } while (mvp!= null);
                    }
                    mvParent.getTopLevel().getCurrentViewAsFormViewObj().getValidator().updateValidationBtnUIState();
                    mvParent.getTopLevel().getCurrentValidator().updateSaveUIEnabledState();
                    
                } else if (hasRequiredFields)
                {
                    formValidator.setFormValidationState(UIValidatable.ErrorType.Valid);
                    formValidator.setHasChanged(true);
                    formValidator.updateValidationBtnUIState();
                }
            }
        }
        
        if (isManyToOne)
        {
            mvParent.setDataIntoParent(dataObj);
        }
        
        // Make the save button enabled
        if (didCarryForward && formValidator != null)
        {
            formValidator.setHasChanged(true);
            formValidator.updateSaveUIEnabledState();
            formValidator.validateForm();
            updateControllerUI();
        }
        
        if (mvParent.isTopLevel())
        {
            mvParent.initSubViews();
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    mvParent.focus();
                }
            });
            
        } else
        {
            focusFirstFormControl();
        }

        if (selectorCBX != null)
        {
            selectorCBX.setEnabled(true);
        }
        
        // rods - 09/08/08 - this is to disable the MenuSwitcher when a new item is created
        if (switcherUI != null)
        {
            switcherUI.setEnabled(false);
        }
    }
    
    /**
     * The user tried to update or delete an object that was already changed by someone else. 
     */
    protected void recoverFromStaleObject(final String msgResStr, final String actualMsg)
    {
        JOptionPane.showMessageDialog(null, actualMsg != null ? actualMsg : getResourceString(msgResStr), getResourceString("Error"), JOptionPane.ERROR_MESSAGE);
        reloadDataObj();
    }
    
    /**
     * Reloads a current (non-new) object from the database i nto the the form.
     */
    protected void reloadDataObj()
    {
        if (!isNewlyCreatedDataObj)
        {
            if (mvParent != null && mvParent.isTopLevel())
            {
                collectionViewState();
            }
            
            if (session != null && (mvParent == null || mvParent.isTopLevel()))
            {
                session.close();
            }
            
            session = DataProviderFactory.getInstance().createSession();
            //DataProviderFactory.getInstance().evict(dataObj.getClass()); 
            
            if (list != null && dataObj instanceof FormDataObjIFace)
            {
                int index = list.indexOf(dataObj);
                Integer id = ((FormDataObjIFace)dataObj).getId();
                if (id != null)
                {
                    Class<?> cls = dataObj.getClass();
                    dataObj = session.get(cls, id);
                    
                    if (index > -1)
                    {
                        list.remove(index);
                        list.insertElementAt(dataObj, index);
                    }
                } else
                {
                    // Bail out if the id is null meaning it is a new object
                    return; 
                }
                
            } else
            {
                dataObj = session.get(dataObj.getClass(), FormHelper.getId(dataObj));
            }
            
            if (mvParent != null)
            {
                mvParent.setSession(session);
                mvParent.setData(dataObj);
                
            } else
            {
                setSession(session);
                this.setDataObj(dataObj);
            }
            this.setDataIntoUI();
            
            if (viewStateList != null && viewStateList.size() > 0 && mvParent != null && mvParent.isTopLevel())
            {
                if (mvParent != null)
                {
                    mvParent.setViewState(viewStateList, altView.getMode(), 0);
                }
                viewStateList.clear();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#doSave()
     */
    public boolean doSave()
    {
        return saveObject();
    }
    
    /**
     * @param mv
     * @return
     */
    private BusinessRulesIFace recurseProcessBR(final MultiView mv)
    {
        BusinessRulesIFace busRulesRV = null;
        FormViewObj fvo = mv.getCurrentViewAsFormViewObj();
        if (fvo != null && fvo.getAltView().getMode() == AltViewIFace.CreationMode.EDIT)
        {
            Object             fvoDataObj = fvo.getCurrentDataObj();
            BusinessRulesIFace busRules   = fvo.getBusinessRules();
            if (busRules != null && fvoDataObj != null)
            {
                BusinessRulesIFace.STATUS status = busRules.processBusinessRules(fvoDataObj);
                if (status != BusinessRulesIFace.STATUS.OK && status != BusinessRulesIFace.STATUS.None)
                {
                    busRulesRV = busRules;
                }
            }
        }
        
        for (MultiView childMV : mv.getKids())
        {
            BusinessRulesIFace brInError = recurseProcessBR(childMV);
            if (brInError != null)
            {
                return brInError;
            }
        }
        return busRulesRV;
    }
    
    /**
     * This helper method deletes each item from the list after checking the busniess rules
     * @param localSession a session to use to delete
     * @param deletedItems the list of data objects
     * @throws Exception
     */
    public static void deleteItemsInDelList(final DataProviderSessionIFace localSession,
                                            final Vector<Object> deletedItems) throws Exception
    {
        for (Object obj : deletedItems)
        {
            BusinessRulesIFace delBusRules = DBTableIdMgr.getInstance().getBusinessRule(obj);
            // notify the business rules object that a deletion is going to happen
            Object obj2 = localSession.merge(obj);
            if (delBusRules != null)
            {
                delBusRules.beforeDelete(obj2, localSession);
            }
            localSession.delete(obj2);
        }
        for (Object obj : deletedItems)
        {
            BusinessRulesIFace delBusRules = DBTableIdMgr.getInstance().getBusinessRule(obj);
            // notify the business rules object that a deletion is going to be committed
            if (delBusRules != null)
            {
                if (!delBusRules.beforeDeleteCommit(obj, localSession))
                {
                    throw new Exception("Business rules processing failed");
                }
            }
        }
    }
    
    /**
     * This helper method is used to update/save those items that have been removed from a relationship
     * @param localSession a session to use to delete
     * @param toBeSavedItems the list of data objects
     * @throws Exception
     */
    public static void saveItemsInToBeSavedList(final DataProviderSessionIFace localSession,
                                                final Vector<Object> toBeSavedItems) throws Exception
    {
        for (Object obj : toBeSavedItems)
        {
            obj = localSession.merge(obj);
            localSession.saveOrUpdate(obj);
        }
    }
    
    /**
     * This method enables us to loop when there is a duplicate key
     * @param dataObj the data object to be saved
     * @return the merged object, or null if there was an error.
     */
    protected SAVE_STATE saveToDB(final Object dataObjArg)
    {
        if (dataObjArg == null)
        {
            if (saveControl != null)
            {
                saveControl.setEnabled(false);
            }
            return SAVE_STATE.SaveOK;
        }
        
        SAVE_STATE saveState = SAVE_STATE.Initial;
        
        boolean isDuplicateError = false;
        boolean tryAgain         = false;
        int     numTries         = 0;
        
        Vector<Object> deletedItems   = mvParent != null ? mvParent.getDeletedItems() : null;
        Vector<Object> toBeSavedItems = mvParent != null ? mvParent.getToBeSavedItems() : null;

        Object dObj = null;
        do
        {
            try
            {
                numTries++;
                
                Integer dataObjId = ((FormDataObjIFace)dataObjArg).getId();
                if (dataObjId != null)
                {
                    Integer count = session.getDataCount(dataObjArg.getClass(), "id", dataObjId, DataProviderSessionIFace.CompareType.Equals);
                    if (count == null || count == 0)
                    {
                        UIRegistry.showLocalizedError("FormViewObj.DATA_OBJ_MISSING");
                        setHasNewData(false);
                        removeObject(true);
                        return SAVE_STATE.Error;
                    }
                }
                
                // First get data so business Rules can be checked
                this.getDataFromUI();
                traverseToGetDataFromForms(mvParent);
                
                //log.debug("saveObject checking businessrules for [" + (dataObjArg != null ? dataObjArg.getClass(): "null") + "]");
                //if (businessRules != null && businessRules.processBusinessRules(dataObjArg) == BusinessRulesIFace.STATUS.Error)
                BusinessRulesIFace busRuleInError = recurseProcessBR(mvParent);
                if (busRuleInError != null)
                {
                    UIRegistry.showError(busRuleInError.getMessagesAsString());
                    return null;
                }
                
                // Now update the auto number fields and re-get all the data
                // we can't update the auto number fields before we run the business rules.
                mvParent.updateAutoNumbers();
                
                this.getDataFromUI();
                traverseToGetDataFromForms(mvParent);
                
                // XXX RELEASE - Need to walk the form tree and set them manually
                //FormHelper.updateLastEdittedInfo(dataObjArg);
                traverseToSetModified(getMVParent());
                
                session.beginTransaction();
                
                if (numTries == 1 && deletedItems != null)
                {
                    deleteItemsInDelList(session, deletedItems);
                }
    
                if (numTries == 1 && toBeSavedItems != null)
                {
                    saveItemsInToBeSavedList(session, toBeSavedItems);
                }
    
                if (businessRules != null)
                {
                    businessRules.beforeMerge(dataObjArg, session);
                }
                
                dObj = session.merge(dataObjArg);
                
                if (businessRules != null)
                {
                    businessRules.beforeSave(dObj, session);
                }

                session.saveOrUpdate(dObj);
                if (businessRules != null)
                {
                    if (!businessRules.beforeSaveCommit(dObj, session))
                    {
                        throw new Exception("Business rules processing failed");
                    }
                }
                session.commit();
                session.flush();
                
                if (mvParent != null)
                {
                    mvParent.clearItemsToBeSaved();
                }
                
                if (numTries == 1 && deletedItems != null)
                {
                    // notify the business rules object that a deletion has occurred
                    for (Object obj: deletedItems)
                    {
                        BusinessRulesIFace delBusRules = DBTableIdMgr.getInstance().getBusinessRule(obj);
                        if (delBusRules != null)
                        {
                            delBusRules.afterDeleteCommit(obj);
                        }
                    }
                    deletedItems.clear();
                }
                
                tryAgain = false;
                
                isNewlyCreatedDataObj = false; // shouldn't be needed, but just in case
                if (rsController != null)
                {
                    rsController.setNewObj(isNewlyCreatedDataObj);
                }
                if (formValidator != null)
                {
                    formValidator.setNewObj(isNewlyCreatedDataObj);
                }

                saveState = SAVE_STATE.SaveOK;
                
                dataObj = dObj;
                
                for (FVOFieldInfo fi : controlsByName.values())
                {
                    if (fi.getComp() instanceof EditViewCompSwitcherPanel)
                    {
                        ((EditViewCompSwitcherPanel)fi.getComp()).putIntoViewMode();
                    }
                }
                
            } catch (StaleObjectException e) // was StaleObjectStateException
            {
                session.rollback();
                recoverFromStaleObject("UPDATE_DATA_STALE", null);
                tryAgain = false;
                dObj     = dataObj;
                saveState = SAVE_STATE.StaleRecovery;
                //e.printStackTrace();
                
            } catch (ConstraintViolationException e)
            {
                //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormViewObj.class, e);
                log.error(e);
                log.error(e.getSQLException());
                log.error(e.getSQLException().getSQLState());
                
                // This check here works for MySQL in English "Duplicate entry"
                // we can add other Databases as we go
                // The idea of this code is that if we are certain it failed on a constraint because
                // of a duplicate key error then we will try a couple of more times.
                //
                // The number 5 and 3 below are completely arbitrary, I just choose them
                // because they seemed right.
                //
                String errMsg = e.getSQLException().toString();
                if (StringUtils.isNotEmpty(errMsg) && errMsg.indexOf("Duplicate entry") > -1)
                {
                    isDuplicateError = true;
                }
                
                tryAgain = (isDuplicateError && numTries < 5) || (!isDuplicateError && numTries < 3);

                isDuplicateError = false;
                
                // Ok, we tried a couple of times and have decided to give up.
                if (!tryAgain)
                {
                    session.rollback();
                    
                    recoverFromStaleObject("DUPLICATE_KEY_ERROR", null);
                    dObj      = dataObj;
                    saveState = SAVE_STATE.StaleRecovery;
                }

            }
            catch (org.hibernate.ObjectNotFoundException ex)
            {
                //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormViewObj.class, ex);
                String errMsg = null;
                String msg    = ex.toString();
                if (StringUtils.contains(msg, "No row with the given identifier exists"))
                {
                    int sInx = msg.indexOf('[');
                    int eInx = msg.lastIndexOf(']');
                    if (sInx > -1 && eInx > -1)
                    {
                        msg = msg.substring(sInx+1, eInx);
                        eInx = msg.lastIndexOf('#');
                        if (eInx > -1)
                        {
                            msg = msg.substring(0, eInx);
                            DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(msg);
                            if (ti != null)
                            {
                                errMsg = String.format(getResourceString("FormViewObj.FIELD_STALE_TITLE"), ti.getTitle());
                            }
                        }
                    }
                }
                
                if (errMsg == null)
                {
                    errMsg = getResourceString("FormViewObj.FIELD_STALE");
                }
                
                session.rollback();
                
                recoverFromStaleObject("UNRECOVERABLE_DB_ERROR", errMsg);
                saveState = SAVE_STATE.StaleRecovery;
            }
            catch (Exception e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormViewObj.class, e);
                log.error("******* " + e);
                e.printStackTrace();
                session.rollback();
                
                recoverFromStaleObject("UNRECOVERABLE_DB_ERROR", null);
                saveState = SAVE_STATE.StaleRecovery;
            }
            
        } while (tryAgain);
        
        return saveState;
    }
    
    /**
     * 
     */
    public Vector<ViewState> collectionViewState()
    {
        if (viewStateList == null)
        {
            viewStateList = new Vector<ViewState>();
        }
        
        viewStateList.clear();

        if (mvParent != null)
        {
            mvParent.collectionViewState(viewStateList, altView.getMode(), 2);
        }
        return viewStateList;
    }

    /**
     * Save any changes to the current object
     */
    @SuppressWarnings("unchecked")
    public boolean saveObject()
    {
        /*if (formValidator != null)
        {
            formValidator.wasValidated(null);
            if (!formValidator.isFormValid())
            {
                return false;
            }
        }*/
        
        if (mvParent != null && mvParent.isTopLevel())
        {
            collectionViewState();
        }
        
        if (session != null && (mvParent == null || mvParent.isTopLevel()))
        {
            session.close();
        }
        session = DataProviderFactory.getInstance().createSession();
        setSession(session);
        
        //log.info("saveObject "+hashCode() + " Session ["+(session != null ? session.hashCode() : "null")+"]");

        if (businessRules != null)
        {
            if (!businessRules.isOkToSave(dataObj, session))
            {
                UIRegistry.showLocalizedError(businessRules.getMessagesAsString());
                return false;
            }
        }
        
        Object beforeSaveDataObj = dataObj;
        
        SAVE_STATE saveState = saveToDB(dataObj);
        
        if (saveState == SAVE_STATE.SaveOK)
        {
            if (businessRules != null)
            {
                businessRules.afterSaveCommit(dataObj, session);
            }
            session.refresh(dataObj);
            
            replaceDataObjInList(beforeSaveDataObj, dataObj);
            
            if (origDataSet != null)
            {
                origDataSet.remove(beforeSaveDataObj);
                origDataSet.add(dataObj);
            }
            
            log.info("Session Saved[ and Flushed "+session.hashCode()+"]");
            
            CommandDispatcher.dispatch(new CommandAction("Data_Entry", "SaveBeforeSetData", dataObj));
            
            setDataIntoUI();
            
            formValidator.setHasChanged(false);
            if (mvParent != null)
            {
                mvParent.clearValidators();
                
                if (mvParent.isTopLevel() && beforeSaveDataObj == mvParent.getData())
                {
                    mvParent.setJustDataObj(dataObj);
                }
            }
            
            // Not calling setHasNewData because we need to traverse and setHasNewData doesn't
            traverseToToSetAsNew(mvParent, false, true); // last arg means it should traverse
            updateControllerUI();
            
            //if (doCarryForward)
            //{
                carryFwdDataObj = dataObj;
            //}

            if (saveControl != null)
            {
                saveControl.setEnabled(false);
            }
            
            if (selectorCBX != null)
            {
                selectorCBX.setEnabled(false);
            }
            
            if (session != null && (mvParent == null || mvParent.isTopLevel()))
            {
                session.close();
                session = null;
            }
            
            if (viewStateList != null && viewStateList.size() > 0 && mvParent != null && mvParent.isTopLevel())
            {
                if (mvParent != null)
                {
                    mvParent.setViewState(viewStateList, altView.getMode(), 0);
                }
                viewStateList.clear();
            }
            
            CommandDispatcher.dispatch(new CommandAction("Data_Entry", "Save", dataObj));

            //log.debug("After save");
            //log.debug("Form     Val: "+(formValidator != null && formValidator.hasChanged()));
            //log.debug("mvParent Val: "+(mvParent != null && mvParent.isTopLevel() && mvParent.hasChanged()));
            return true;
        }
        
        return false;
    }
    
    /**
     * @param oldDO
     * @param newDO
     */
    protected void replaceDataObjInList(final Object oldDO, final Object newDO)
    {
        //if (oldDO != null && newDO != null)
        //{
        //    log.debug("Replacing "+oldDO.getClass().getSimpleName()+" "+oldDO.hashCode()+" with "+newDO.getClass().getSimpleName()+" "+newDO.hashCode());
        //}
        if (list != null)
        {
            int index = list.indexOf(oldDO);
            if (index > -1)
            {
                list.remove(oldDO);
                if (oldDO != null)
                {
                    log.error("Removed " + oldDO.getClass().getSimpleName()+" "+oldDO.hashCode() + " from list.");
                }
                list.insertElementAt(newDO, index);
                log.error("list length: "+list.size()+"    inx: "+index); 
            } else
            {
                if (oldDO != null)
                {
                    log.error("************ " + oldDO.getClass().getSimpleName()+" "+oldDO.hashCode() + " couldn't be found in list.");
                }
            }
        }
    }

    /**
     * This adjusts the rsController after an item has been deleted, it gets a new item
     * to fill the form if on exists.
     */
    protected void adjustRSControllerAfterRemove()
    {
        if (rsController != null)
        {
            int currInx = rsController.getCurrentIndex();
            int newLen  = rsController.getLength() - 1;
            int newInx  = Math.min(currInx, newLen-1);
            
            if (list != null)
            {
                list.remove(currInx); // remove from list
            }
            
            if (recordSetItemList != null)
            {
                recordSetItemList.remove(currInx); // remove from list
            }
            
            rsController.setLength(newLen); // set new len for controller
    
            if (newInx > -1 && (list == null || list.size() > 0))
            {
                isEditing = false;
                rsController.setIndex(newInx, mvParent == null); // only send notification about index change top form
                isEditing = true;

                // rods - 07/07/2008 - This has already been at this point when the index changed.
                // and this ends 
                //dataObj = list.get(newInx);
                //setDataObj(dataObj, true); // true means the dataObj is already in the current "list" of data items we are working with
                
                if (formValidator != null)
                {
                    formValidator.validateForm();
                }
                
            } else 
            {
                setDataObj(null, true); // true means the dataObj is already in the current "list" of data items we are working with
            }
            updateControllerUI();
            
            if (newLen == 0 && formValidator != null)
            {
                formValidator.setHasChanged(false);
                formValidator.setFormValidationState(UIValidatable.ErrorType.Valid);
                formValidator.updateValidationBtnUIState(); // requires manual overridew becase the validator is disabled.
            }
            
        } else if (MultiView.isOptionOn(options, MultiView.IS_SINGLE_OBJ))
        {
            setDataObj(null, false); // true means the dataObj is already in the current "list" of data items we are working with
        }
    }

    /**
     * Save any changes to the current object
     */
    protected void askToRemoveObject()
    {
        boolean addSearch = mvParent != null && MultiView.isOptionOn(mvParent.getOptions(), MultiView.ADD_SEARCH_BTN);
        
        Object[] delBtnLabels = {getResourceString(addSearch ? "Remove" : "Delete"), getResourceString("CANCEL")};
        String title = dataObj instanceof FormDataObjIFace ? ((FormDataObjIFace)dataObj).getIdentityTitle() : tableInfo.getTitle();
        
        int rv = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), UIRegistry.getLocalizedMessage(addSearch ? "ASK_REMOVE" : "ASK_DELETE", title),
                                              getResourceString("Delete"),
                                              JOptionPane.YES_NO_OPTION,
                                              JOptionPane.QUESTION_MESSAGE,
                                              null,
                                              delBtnLabels,
                                              delBtnLabels[1]);
        if (rv != JOptionPane.YES_OPTION)
        {
            return;
        }
        
        // We do this because the process of determining whether something can be deleted might take a while.
        
        if (addSearch)
        {
            doDeleteDataObj(dataObj, session, true);
            
        } else if (businessRules != null)
        {
            UIRegistry.getStatusBar().setIndeterminate(STATUSBAR_NAME, true);
            final SwingWorker worker = new SwingWorker()
            {
                public Object construct()
                {
                    businessRules.okToDelete(dataObj, session, FormViewObj.this);
                    return null;
                }

                //Runs on the event-dispatching thread.
                public void finished()
                {
                    UIRegistry.getStatusBar().setProgressDone(STATUSBAR_NAME);
                    if (session != null)
                    {
                        session.close();
                    }
                }
            };
            worker.start();
            
        } else // No Business Rules
        {
            doDeleteDataObj(dataObj, session, true); 
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace#doDeleteDataObj(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, boolean)
     */
    public void doDeleteDataObj(final Object dataObjArg, final DataProviderSessionIFace sessionArg, final boolean doDelete)
    {
        if (doDelete)
        {
            removeObject(false);
            
        } else
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(dataObjArg.getClass().getName());
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(getResourceString("COULDNT_DELETE_OBJ"), ti.getTitle(), ((FormDataObjIFace)dataObjArg).getIdentityTitle()));
            sb.append("\n");
            sb.append(getResourceString("BR_FOUNDINTABLE_LABEL"));
            sb.append("\n");
            for (String s : businessRules.getWarningsAndErrors())
            {
                sb.append("  ");
                sb.append(s);
                sb.append("\n");
            }
            JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                    sb.toString(), 
                    getResourceString("COULDNT_DELETE_OBJ_TITLE"), JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Save any changes to the current object
     */
    protected void removeObject(final boolean doSkipAlreadyDelMsg)
    {
        // This shouldn't happen
        if (session != null)
        {
            session.close();
        }
        
        session = DataProviderFactory.getInstance().createSession();
        setSession(session);
        
        try
        {
            //log.info(hashCode() + " Session ["+(session != null ? session.hashCode() : "null")+"] ");
            if (session == null)
            {
                return;
            }
            
            // rods - 08/21/08 Needed to add this so could remove the other side of the relationship
            // which might have failed from being lazy loaded.
            
            //session.attach(parentDataObj);
            
            // 09/23/08 - Bug 5996 When the dataObj fails to attach it is most likely
            // because it has been changed. Which we don't care about because we are deleting it
            boolean attachFailed = false;
            if (((FormDataObjIFace)dataObj).getId() != null)
            {
                try
                {
                    session.attach(dataObj);
                    
                    if (carryFwdDataObj != null && carryFwdDataObj instanceof FormDataObjIFace)
                    {
                        FormDataObjIFace fdo = (FormDataObjIFace)carryFwdDataObj;
                        if (fdo.getId() != null && fdo.getId().equals(((FormDataObjIFace)dataObj).getId()))
                        {
                            carryFwdDataObj = null;
                            // XXX should we warn the user?
                        }
                    }
                    
                } catch (org.hibernate.HibernateException ex)
                {
                    // we could check the type to make sure it was a "dirty colleciton" error
                    // but for now I am not.
                    attachFailed = true;
                }
            }
            
            removeFromParent(mvParent, parentDataObj, cellName, dataObj);
            
            // Delete a child object by caching it in the Top Level MultiView
            if (mvParent != null && !mvParent.isTopLevel())
            {
                boolean addSearch = mvParent != null && MultiView.isOptionOn(mvParent.getOptions(), MultiView.ADD_SEARCH_BTN);
                
                // We don't delete these type of objects from the database
                // because were added as references only
                if (!addSearch)
                {
                    mvParent.getTopLevel().addDeletedItem(dataObj);
                }
    
                String delMsg = (businessRules != null) ? businessRules.getDeleteMsg(dataObj) : "";
                UIRegistry.getStatusBar().setText(delMsg);
                formValidator.setHasChanged(true);
                formValidator.validateForm();
    
                // We need to turn off the notifications when setting new data.
                formValidator.setIgnoreValidationNotifications(true);
                adjustRSControllerAfterRemove();
                formValidator.setIgnoreValidationNotifications(false);
                
                mvParent.getTopLevel().getCurrentValidator().setHasChanged(true);
                mvParent.getTopLevel().getCurrentValidator().validateForm();
                
                isNewlyCreatedDataObj = false; // shouldn't be needed, but just in case
                if (rsController != null)
                {
                    rsController.setNewObj(isNewlyCreatedDataObj);
                }
                if (formValidator != null)
                {
                    formValidator.setNewObj(isNewlyCreatedDataObj);
                }
                
                if (session != null)
                {
                    session.close();
                    setSession(null);
                }
                
                return;
            }
            
            String delMsg = (businessRules != null) ? businessRules.getDeleteMsg(dataObj) : "";

            boolean doClearObj = true;

            try
            {
                // Clear the items in the "deleted" cache because they will be deleted anyway.
                if (mvParent != null && mvParent.getDeletedItems() != null)
                {
                    mvParent.getDeletedItems().clear();
                }
                
                FormDataObjIFace fdo   = (FormDataObjIFace)dataObj;
                Integer          objId = fdo.getId();
                if (objId != null)
                {
                    if (attachFailed)
                    {
                        session.close();
                        session = DataProviderFactory.getInstance().createSession();
                        setSession(session);
                    }
                    
                    session.evict(dataObj);
                    // Reload the object from the database  to avoid a stale object exception.
                    Object dbDataObj = session.getData(fdo.getDataClass(), "id", objId, DataProviderSessionIFace.CompareType.Equals);
                    if (dbDataObj != null)
                    {
                        session.beginTransaction();
                        if (businessRules != null)
                        {
                            businessRules.beforeDelete(dbDataObj, session);
                        }
                        session.delete(dbDataObj);
                        if (businessRules != null)
                        {
                            if (!businessRules.beforeDeleteCommit(dbDataObj, session))
                            {
                                session.rollback();
                                throw new Exception("Business rules processing failed");
                            }
                        }
                        session.commit();
                        session.flush();
                        if (businessRules != null)
                        {
                            businessRules.afterDeleteCommit(dbDataObj);
                        }
                    } else
                    {
                        doClearObj = true;
                        if (!doSkipAlreadyDelMsg)
                        {
                            UIRegistry.showLocalizedMsg("OBJ_ALREADY_DEL");
                        }
                    }
                    
                    updateAfterRemove(false);
                }
                
            } catch (edu.ku.brc.dbsupport.StaleObjectException e)
            {
                e.printStackTrace();
                doClearObj = false;
                session.rollback();
                recoverFromStaleObject("DELETE_DATA_STALE", null);
                
            } catch (Exception e)
            {
                e.printStackTrace();
                doClearObj = false;
                session.rollback();
                recoverFromStaleObject("DELETE_DATA_STALE", null);
            }
            
            log.debug("Session Flushed["+(session != null ? session.hashCode() : "no session")+"]");

            if (doClearObj)
            {
                adjustRSControllerAfterRemove();
                
                UIRegistry.getStatusBar().setText(delMsg);
            } else
            {
                UIRegistry.getStatusBar().setText(getResourceString("OBJ_NOT_DELETED"));
            }

        } catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormViewObj.class, e);
            log.error("******* " + e);
            e.printStackTrace();
            
        } finally
        {
            if (session != null && (mvParent == null || mvParent.isTopLevel()))
            {
                session.close();
                session = null;
            }
        }
        
        if (saveControl != null && list != null && list.size() == 0)
        {
            saveControl.setEnabled(false);
        }
    }
    
    /**
     * Sets the "newly created object" in the controller and the validator to false
     * and closes and frames that are showing.
     * @param doAdjustRS true calls adjustRSControllerAfterRemove and removes the item from the list
     */
    public void updateAfterRemove(final boolean doAdjustRS)
    {
        isNewlyCreatedDataObj = false; // shouldn't be needed, but just in case
        if (rsController != null)
        {
            rsController.setNewObj(isNewlyCreatedDataObj);
        }
        if (formValidator != null)
        {
            formValidator.setNewObj(isNewlyCreatedDataObj);
        }

        if (mvParent != null)
        {
            mvParent.shutdownDisplayFrames();
        }
        
        if (doAdjustRS)
        {
            // Nees to be put out on GUI thread to prevent lock up
            SwingUtilities.invokeLater(new Runnable() {

                /* (non-Javadoc)
                 * @see java.lang.Runnable#run()
                 */
                @Override
                public void run()
                {
                    adjustRSControllerAfterRemove();
                }
                
            });
        }
    }

    /**
     * Tells this form and all of it's children that it is a "new" form for data entry.
     * @param isNewForm true is new, false is not
     */
    public void setHasNewData(final boolean isNewForm)
    {
        isNewlyCreatedDataObj = isNewForm; // rods - 09/05/08 - added so "+" can be disabled for new forms with data
        updateControllerUI();
    }

    /**
     * Returns the list of MultiView kids (subforms).
     * @return the list of MultiView kids (subforms)
     */
    public List<MultiView> getKids()
    {
        return kids;
    }

    /**
     * Debug method - lists the fields that have changed.
     */
    public void listFieldChanges()
    {
        try
        {
            if (formValidator != null)
            {
                log.debug("=================================== "+formValidator.getDCNs().values().size());
                for (DataChangeNotifier dcn : formValidator.getDCNs().values())
                {
                    FVOFieldInfo fieldInfo = controlsById.get(dcn.getId());
                    if (fieldInfo != null)
                    {
                        log.debug("Changed Field["+fieldInfo.getName()+"]\t["+(dcn.isDataChanged() ? "CHANGED" : "not changed")+"]");
                    } else
                    {
                        log.debug("Field Info is null for dcn.getId()["+dcn.getId()+"]");
                    }
                }
                log.debug("===================================");
            }
        } catch (Exception ex)
        {
            log.error(ex);
        }
    }
    
    /**
     * This will choose the first focusable UI component that doesn't have a value. 
     * BUT! It always chooses a JTextField over anything else.
     * (NOTE: We may want a non-JTextField that is required to override a JTextField that is not.)
     * 
     * @return the focusable first object.
     */
    public Component getFirstFocusable()
    {
        int       insertPos = Integer.MAX_VALUE;
        Component focusable = null;
        Component first     = null;
        for (FVOFieldInfo compFI : compsList)
        {
            Component comp = compFI.getComp();

            if (comp.isEnabled() && comp.isFocusable() && comp instanceof GetSetValueIFace)
            {
                Object val = ((GetSetValueIFace)comp).getValue();
                if (val == null || (val instanceof String && StringUtils.isEmpty((String)val)))
                {
                    if (comp instanceof ValFormattedTextFieldSingle)
                    {
                        ValFormattedTextFieldSingle vtf = (ValFormattedTextFieldSingle)comp;
                        if (vtf.getFormatter() != null && vtf.getFormatter().isIncrementer())
                        {
                            continue;
                        }
                    }
                    boolean override = false;//focusable instanceof JTextField && !(comp instanceof JTextField);
                    
                    if (compFI.getInsertPos() < insertPos || override)
                    {

                        if (comp instanceof UIValidatable)
                        {

                            focusable = ((UIValidatable)comp).getValidatableUIComp();
                        } else
                        {
                            focusable = comp;
                        }
                        
                        if (!override) // keep the same (lower) position as the original
                        {
                            insertPos = compFI.getInsertPos();
                        }
                    }
                }
                
                if (compFI.getInsertPos() == 0)
                {
                    first = comp;
                }
            }
        }
        
        if (focusable instanceof JTextField && !(first instanceof JTextField))
        {
            return focusable;
        }
        
        return first != null ? first : focusable;
    }

    /**
     * Sets the focus to the first control in the form.
     */
    public void focusFirstFormControl()
    {
        Component focusable = getFirstFocusable();
        if (focusable != null)
        {
            focusable.requestFocus();
        }
    }

    /**
     * Adds the the ActionListener to the btns.
     */
    protected void setAddDelListeners(final JButton addBtn, final JButton delBtn)
    {
        if (addBtn != null)
        {
            addBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    UIValidator.setIgnoreAllValidation(this, true);
                    createNewDataObject(false);
                    if (formValidator != null)
                    {
                        formValidator.processFormRules();
                    }
                    UIValidator.setIgnoreAllValidation(this, false);
                    //focusFirstFormControl();
                }
            });
        }

        if (delBtn != null)
        {
            delBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    askToRemoveObject();
                }
            });
        }
    }

    /**
     * Adds the ResultSetController to the panel.
     * @param addSearch indicates it should add a search btn
     */
    protected void addRSController(final boolean addSearch)
    {
        // If the Control panel doesn't exist, then add it
        if (rsController == null && controlPanel != null)
        {
            boolean canAdd = true;
            boolean canDel = true;
            
            if (AppContextMgr.isSecurityOn())
            {
                if (perm == null)
                {
                    if (tableInfo != null)
                    {
                        perm = tableInfo.getPermissions();
                    } else
                    {
                        String shortName = StringUtils.substringAfterLast(view.getClassName(), ".");
                        if (shortName == null)
                        {
                            shortName = view.getClassName();
                        }
                        perm = SecurityMgr.getInstance().getPermission("DO."+shortName.toLowerCase());
                    }
                    //SecurityMgr.dumpPermissions(mvParentArg.getViewName(), perm.getOptions());
                    canAdd = perm.canAdd();
                    canDel = perm.canDelete();
                }
            }
            
            boolean mvHasSeparator = mvParent.getSeparator() != null;
            
            boolean inEditMode = altView.getMode() == AltViewIFace.CreationMode.EDIT;
            rsController = new ResultSetController(formValidator, 
                                                   inEditMode && !addSearch && canAdd, // Add New
                                                   inEditMode && canDel,               // Add Delete
                                                   inEditMode && addSearch && canAdd,  // Add Search
                                                   view.getObjTitle(),                 // Object Title
                                                   0,                                  // current length
                                                   !mvHasSeparator);                    // don't layout the btns
            rsController.getPanel().setBackground(bgColor);
            if (mvParent.isTopLevel())
            {
                rsController.setupGotoListener();
            }
            
            rsController.addListener(this);
            controlPanel.addController(rsController.getPanel());
            controlPanel.setRecordSetController(rsController);
            
            newRecBtn = rsController.getNewRecBtn();
            delRecBtn = rsController.getDelRecBtn();
            
            if (formValidator != null && newRecBtn != null)
            {
                formValidator.addEnableItem(newRecBtn, FormValidator.EnableType.ValidItems);
            }
            
            setAddDelListeners(newRecBtn, delRecBtn);
            
            if (mvHasSeparator)
            {
                addControllerBtnsToSep();
            }
        }
    }
    
    /**
     * Adds all the Control buttons to the separator.
     */
    protected void addControllerBtnsToSep()
    {
        JButton searchBtn = rsController.getSearchRecBtn();
        int cnt = (newRecBtn != null ? 1 : 0) + (delRecBtn != null ? 1 : 0) + (searchBtn != null ? 1 : 0);
        PanelBuilder pb = new PanelBuilder(new FormLayout(UIHelper.createDuplicateJGoodiesDef("p", "2px", cnt), "p"));
        
        int x = 1;
        if (newRecBtn != null)
        {
            pb.add(newRecBtn, cc.xy(x, 1));
            x += 2;
        }
        if (delRecBtn != null)
        {
            pb.add(delRecBtn, cc.xy(x, 1));
            x += 2;
        }
        if (searchBtn != null)
        {
            pb.add(searchBtn, cc.xy(x, 1));
            x += 2;
        }
        
        pb.getPanel().setOpaque(false);
        sepController = pb.getPanel();
    }
    
    /**
     * Creates the extra btns.
     */
    protected void createAddDelSearchPanel()
    {
        if (controlPanel != null && formValidator != null)
        {
            boolean doAddSearch = mvParent != null && MultiView.isOptionOn(mvParent.getOptions(), MultiView.ADD_SEARCH_BTN);
            
            Insets insets = new Insets(1,1,1,1);
            PanelBuilder rowBuilder = new PanelBuilder(new FormLayout("f:p:g,p,2px,p"+ (doAddSearch ? ",2px,p" : ""), "p"));
            
            /*newRecBtn = new JButton("+") {
                public void setEnabled(boolean enable)
                {
                    System.err.println("> "+enable);
                    super.setEnabled(enable);
                }
            };*/
            newRecBtn = UIHelper.createIconBtn("NewRecord", null, null);
            newRecBtn.setToolTipText(ResultSetController.createTooltip("NewRecordTT", view.getObjTitle()));
            newRecBtn.setMargin(insets);
            rowBuilder.add(newRecBtn, cc.xy(2,1));
    
            delRecBtn = UIHelper.createIconBtn("DeleteRecord", null, null);
            delRecBtn.setToolTipText(ResultSetController.createTooltip("RemoveRecordTT", view.getObjTitle()));
            delRecBtn.setMargin(insets);
            rowBuilder.add(delRecBtn, cc.xy(4,1));
            
            if (doAddSearch)
            {
                srchRecBtn = UIHelper.createIconBtn("Search", IconManager.IconSize.Std16, null, null);
                srchRecBtn.setToolTipText(ResultSetController.createTooltip("SearchForRecordTT", view.getObjTitle()));
                srchRecBtn.setMargin(insets);
                srchRecBtn.setOpaque(false);
                rowBuilder.add(srchRecBtn, cc.xy(6,1));
                
                DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                if (tblInfo != null)
                {
                    searchName = tblInfo.getSearchDialog();
                    if (StringUtils.isEmpty(searchName))
                    {
                        searchName = ""; // Note not null but empty tells it to disable the search btn
                    }
                }
                
                srchRecBtn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        doSearch();
                    }
                });
            }
            
            rowBuilder.getPanel().setOpaque(false);
            newRecBtn.setOpaque(false);
            delRecBtn.setOpaque(false);
            // This is the Old way
            //controlPanel.addController(rowBuilder.getPanel());
            
            // This is the new way
            sepController = rowBuilder.getPanel();
            
            // 2/13/09 - rods - The last two checks determine if it is a 'Single' item
            if (formValidator != null && newRecBtn != null && rsController != null && origDataSet != null)
            {
                formValidator.addEnableItem(newRecBtn, FormValidator.EnableType.ValidItems);
            }
            
            setAddDelListeners(newRecBtn, delRecBtn);
        }
            
    }
    
    /**
     * 
     */
    protected void doSearch()
    {
        if (StringUtils.isNotEmpty(searchName))
        {
            ViewBasedSearchDialogIFace dlg = UIRegistry.getViewbasedFactory().createSearchDialog(UIHelper.getWindow(mainComp), searchName);
            dlg.setMultipleSelection(true);
            dlg.getDialog().setModal(true);
            dlg.getDialog().setVisible(true);
            if (!dlg.isCancelled())
            {
                // Some object that are searched for need to have a new parent
                // so we ask them if they want us to create one for them
                // and then we hand it to them.
                // Otherwise we just set the new object into the form.
                List<Object> newDataObjects  = dlg.getSelectedObjects();
                boolean      doSetNewDataObj = true;
                
                if (businessRules != null && newDataObjects != null)
                {
                    if (businessRules.doesSearchObjectRequireNewParent())
                    {
                        createNewDataObject(false);
                        doSetNewDataObj = false;
                    }
                    
                    for (Object dObj : newDataObjects)
                    {
                        if (!businessRules.isOkToAssociateSearchObject(parentDataObj, dObj))
                        {
                            UIRegistry.showLocalizedError(businessRules.getMessagesAsString());
                            return;
                        }
                        businessRules.processSearchObject(!doSetNewDataObj ? dataObj : null, dObj);
                    }

                    // Set the data and validate
                    this.setDataIntoUI();
                    
                    if (formValidator != null)
                    {
                        formValidator.validateForm();
                    }
                }
                
                if (newDataObjects != null && newDataObjects.size() > 0)
                {
                    if (doSetNewDataObj)
                    {
                        boolean doOtherSide = true;
                        
                        DBTableInfo parentTblInfo = DBTableIdMgr.getInstance().getByShortClassName(parentDataObj.getClass().getSimpleName());
                        if (parentTblInfo != null)
                        {
                            DBTableChildIFace ci = parentTblInfo.getItemByName(cellName);
                            if (ci instanceof DBRelationshipInfo)
                            {
                                DBRelationshipInfo ri = (DBRelationshipInfo)ci;
                                doOtherSide = ri.getType() == DBRelationshipInfo.RelationshipType.OneToMany;
                            }
                        }
                        
                        for (Object obj : newDataObjects)
                        {
                            ((FormDataObjIFace)parentDataObj).addReference((FormDataObjIFace)obj, cellName, doOtherSide);
                        }
                        
                        if (list != null && origDataSet != null)
                        {
                            list.addAll(newDataObjects);
                            int len = list.size();
                            rsController.setLength(len);
                            rsController.setIndex(len-1, false);
                            origDataSet.addAll(newDataObjects);
                            
                        } else 
                        {
                            setDataObj(newDataObjects.get(0));
                        }
                        
                        mvParent.getTopLevel().getCurrentValidator().setHasChanged(true);
                        mvParent.getTopLevel().getCurrentValidator().validateForm();
                    }
                }
            }

        } else
        {
            log.error("The search name is empty is there one defined in the display tag for the XML?");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getSaveComponent()
     */
    public JComponent getSaveComponent()
    {
        return saveControl;
    }

    //-------------------------------------------------
    // Viewable
    //-------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getType()
     */
    public ViewDef.ViewType getType()
    {
        return formViewDef.getType();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getUIComponent()
     */
    public Component getUIComponent()
    {
        return mainComp;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#isSubform()
     */
    public boolean isSubform()
    {
        return mvParent != null && mvParent.getMultiViewParent() != null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getCompById(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public <T> T getCompById(final String id)
    {
        FVOFieldInfo fi = controlsById.get(id);
        if (fi != null)
        {
            return (T)fi.getComp();
        }
        // else
        //throw new RuntimeException("Couldn't find FieldInfo for ID["+id+"]");
        //log.error("Couldn't find FieldInfo for ID["+id+"]");
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getLabelById(java.lang.String)
     */
    public JLabel getLabelFor(final String id)
    {
        FVOFieldInfo fi = labels.get(id);
        if (fi != null)
        {
            return (JLabel)fi.getComp();
        }
        // else
        throw new RuntimeException("Couldn't find FieldInfo for ID["+id+"]");
    }

    /**
     * @param id the id of the label to be returned.
     * @return the FieldInfo for a label
     */
    public FVOFieldInfo getLabelInfoFor(final String id)
    {
        for (FVOFieldInfo fi : labels.values())
        {
            String labelForId = ((FormCellLabel)fi.getFormCell()).getLabelFor();
            if (labelForId != null && labelForId.equals(id))
            {
                return fi;
            }
        }
        return null;
    }

    /**
     * Get the label for a field comonent.
     * @param comp the field component
     * @return the label component for a field component
     */
    public JLabel getLabelFor(final Component comp)
    {
        for (FVOFieldInfo fi : controlsById.values())
        {
            if (fi.getComp() == comp)
            {
                return getLabelFor(fi.getId());
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getControlMapping()
     */
    public Map<String, Component> getControlMapping()
    {
        Map<String, Component> map = new Hashtable<String, Component>();
        for (FVOFieldInfo fieldInfo : controlsById.values())
        {
            map.put(fieldInfo.getId(), fieldInfo.getComp());
        }
        return map;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#getControlById(java.lang.String)
     */
    public Component getControlById(String id)
    {
        FVOFieldInfo fi = controlsById.get(id);
        return fi != null ? fi.getComp() : null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.ViewBuilderIFace#fixUpRequiredDerivedLabels()
     */
    public void fixUpRequiredDerivedLabels()
    {
        // NOTE: The forms can contain object that are not in our data model
        DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(formViewDef.getClassName());
        if (ti != null)
        {
            Font        boldFont = null;
            for (String idFor : labels.keySet())
            {
                FVOFieldInfo       labelInfo = labels.get(idFor);
                JLabel             label     = (JLabel)labelInfo.getComp();
                //FormCellLabelIFace lblCell   = (FormCellLabelIFace)labelInfo.getFormCell();
                
                FormViewObj.FVOFieldInfo fieldInfo = controlsById.get(idFor);
                if (fieldInfo == null)
                {
                    UIRegistry.showError("Setting Label -Form control with id["+idFor+"] is not in the form or subform.");
                    continue;
                }
                
                if (fieldInfo.getFormCell() != null && fieldInfo.getFormCell().getType() == FormCellIFace.CellType.field)
                {
                    FormCellField     cell      = (FormCellField)fieldInfo.getFormCell();
                    String            fieldName = fieldInfo.getFormCell().getName();
                    DBTableChildIFace derivedCI = null;
                    
                    if (fieldName.indexOf(".") > -1)
                    {
                        derivedCI = FormHelper.getChildInfoFromPath(fieldName, ti);
                        if (derivedCI == null)
                        {
                            UIRegistry.showError("The name 'path' ["+fieldName+"] was not valid.");
                            continue; 
                        }
                    }
                
                    DBTableChildIFace tblChild = derivedCI != null ? derivedCI : ti.getItemByName(fieldInfo.getFormCell().getName());
                    if (isEditing && (cell.isRequired() || (tblChild != null && tblChild.isRequired())))
                    {
                        if (boldFont == null)
                        {
                            boldFont = label.getFont().deriveFont(Font.BOLD);
                        }
                        label.setFont(boldFont);
                    }
                    
                    /*if (lblCell.isDerived() && tblChild != null)
                    {
                        String title = tblChild.getTitle();
                        if (StringUtils.isNotEmpty(title))
                        {
                            label.setText(title + (StringUtils.isNotEmpty(title) ? ":" : ""));
                        }
                    }*/
                }
            }
            
            /*for (FVOFieldInfo fieldInfo : controlsByName.values())
            {
                if (fieldInfo.getFormCell().getType() == FormCellIFace.CellType.field)
                {
                    FormCellField cell = (FormCellField)fieldInfo.getFormCell();
                    
                    if (cell.getUiType() == FormCellFieldIFace.FieldType.checkbox)
                    {
                        DBFieldInfo fi  = ti.getFieldByName(fieldInfo.getFormCell().getName());
                        JCheckBox   cbx = (JCheckBox)fieldInfo.getComp();
                        
                        if (isEditting && (cell.isRequired() || (fi != null && fi.isRequired())))
                        {
                            if (boldFont == null)
                            {
                                boldFont = cbx.getFont().deriveFont(Font.BOLD);
                            }
                            cbx.setFont(boldFont);
                        }
                        
                        if (cell.isDerived() && fi != null)
                        {
                            String title = fi.getTitle();
                            if (StringUtils.isNotEmpty(title))
                            {
                                //cbx.setText(title);
                            }
                        }
                    }
                }
            }*/
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.ViewBuilderIFace#doneBuilding()
     */
    @Override
    public void doneBuilding()
    {
        for (FVOFieldInfo fi : controlsById.values())
        {
            if (fi.getUiPlugin() != null)
            {
                fi.getUiPlugin().setParent(this);
            }
        }
    }

    /**
     * @param id the id of the control
     * @return the FVOFieldInfo object by ID
     */
    public FVOFieldInfo getFieldInfoForId(final String id)
    {
        return controlsById.get(id);
    }

    /**
     * @param fName the name of the control
     * @return the FVOFieldInfo object by name
     */
    public FVOFieldInfo getFieldInfoForName(final String fName)
    {
        return controlsByName.get(fName);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getValidator()
     */
    public FormValidator getValidator()
    {
        return formValidator;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataObj(java.lang.Object)
     */
    public void setDataObj(final Object dataObj)
    {
        setDataObj(dataObj, false);
    }
    
    /**
     * @param index
     * @return
     */
    protected Object getDataObjectViaRecordSet(final int index)
    {
        Object dObj = null;
        //log.debug("Loading["+recordSetItemList.get(index).getRecordId()+"]");
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        try
        {
            dObj = tmpSession.get(tableInfo.getClassObj(), recordSetItemList.get(index).getRecordId());
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            tmpSession.close();
        }
        return dObj;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setRecordSet(edu.ku.brc.dbsupport.RecordSetIFace)
     */
    public void setRecordSet(final RecordSetIFace recordSet)
    {
        tableInfo = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());
        // XXX Check for Error here
        
        StringBuilder sql = new StringBuilder("SELECT " + tableInfo.getIdColumnName() + " FROM " + tableInfo.getName() + " WHERE " + tableInfo.getIdColumnName() + " IN (");
        int cnt = 0;
        for (RecordSetItemIFace rsi : recordSet.getOrderedItems())
        {
            if (cnt > 0) sql.append(',');
            sql.append(rsi.getRecordId());
            cnt++;
        }
        sql.append(')');
        
        //log.debug(sql.toString());
        
        SQLExecutionListener listener = new SQLExecutionListener()
        {
            public void exectionDone(final SQLExecutionProcessor process, final java.sql.ResultSet resultSet)
            {
                countResultsBack(recordSet, resultSet);
            }
            
            public void executionError(final SQLExecutionProcessor process, final Exception ex)
            {
                // Display dlg with error message
            }
        };
        SQLExecutionProcessor sqlProc = new SQLExecutionProcessor(listener, sql.toString());
        sqlProc.start();
    }
    
    /**
     * @param fvo
     * @param availableIdList
     */
    public void setRecordSetItemList(final DBTableInfo              tableInfo,
                                     final List<RecordSetItemIFace> availableIdList)
    {
        if (availableIdList != null)
        {
            this.tableInfo = tableInfo;
            if (this.recordSetItemList == null)
            {
                recordSetItemList = new Vector<RecordSetItemIFace>(availableIdList.size());
                
            } else
            {
                recordSetItemList.clear();
            }
            
            if (recordSet == null)
            {
                RecordSetIFace rs = RecordSetFactory.getInstance().createRecordSet();
                rs.setName("Temp");
                rs.setDbTableId(tableInfo.getTableId());
                recordSet = rs;
            }
            recordSetItemList.addAll(availableIdList);
            recordSet.addAll(availableIdList);
        }
    }
    
    /**
     * @param rs
     * @param resultSet
     */
    protected void countResultsBack(final RecordSetIFace rs, final java.sql.ResultSet resultSet)
    {
        try
        {
            if (resultSet.next())
            {
                Hashtable<Integer, Boolean> availableIdList = new Hashtable<Integer, Boolean>();
                do
                {
                    availableIdList.put(resultSet.getInt(1), true);
                    
                } while (resultSet.next());
                
                if (availableIdList.size() != rs.getNumItems())
                {
                    UIRegistry.displayLocalizedStatusBarText("FormViewObj.NOT_ALL_RECS_FOUND");
                }
                
                if (recordSet == null)
                {
                    recordSet = RecordSetFactory.getInstance().createRecordSet();
                }
                
                recordSetItemList = new Vector<RecordSetItemIFace>(availableIdList.size());
                for (RecordSetItemIFace rsi : rs.getOrderedItems())
                {
                    if (availableIdList.get(rsi.getRecordId().intValue()) != null)
                    {
                        recordSetItemList.add(rsi);
                        recordSet.addItem(rsi);
                    }
                }
                Object         firstDataObj = getDataObjectViaRecordSet(0);
                Vector<Object> tmpList      = new Vector<Object>(availableIdList.size()+5);
                tmpList.add(firstDataObj);
                for (int i=1;i<recordSetItemList.size();i++)
                {
                    tmpList.add(null); 
                }
                
                if (mvParent != null)
                {
                    mvParent.setData(tmpList);
                    mvParent.setRecordSetItemList(this, tableInfo, recordSetItemList);
                    
                } else
                {
                    setDataObj(tmpList);
                }
                
            } else
            {
                SwingUtilities.invokeLater( new Runnable() {
                    //@Override
                    public void run()
                    {
                        JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                                getResourceString("NO_RECORD_FOUND"), 
                                getResourceString("NO_RECORD_FOUND_TITLE"), JOptionPane.WARNING_MESSAGE);
                    }
                });
            }
        } catch (SQLException ex)
        {
            SwingUtilities.invokeLater( new Runnable() {
                //@Override
                public void run()
                {
                    JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                            getResourceString("ERROR_LOADING_FORM_DATA"), 
                            getResourceString("ERROR_LOADING_FORM_DATA_TITLE"), JOptionPane.WARNING_MESSAGE);
                }
            });
        }

    }
    
    /**
     * Updates the enabled state of the New and delete buttons in the controller
     */
    protected void updateControllerUI()
    {
        if (rsController != null)
        {
            rsController.updateUI();
        }
        
        //log.debug("----------------- "+formViewDef.getName()+"----------------- ");
        if (delRecBtn != null && !isJavaCollection())
        {
            boolean enableDelBtn = dataObj != null && (businessRules == null || businessRules.okToEnableDelete(this.dataObj));
            //log.debug("1----------------- Del "+formViewDef.getName()+"  "+enableDelBtn+"----------------- ");
            
            delRecBtn.setEnabled(enableDelBtn);
        }
        
        boolean enableNewBtn = false;
        if (newRecBtn != null)
        {
            if (formValidator != null && dataObj != null)
            {
                enableNewBtn = formValidator.isFormValid();
                
            } else if (mvParent != null)
            {
                //log.debug("mvParent.getData() "+mvParent.getData() +"  data ["+ (dataObj == null ? "null" : dataObj.getClass().getSimpleName()) + "] parent"+ 
                //          (!mvParent.isTopLevel() ? mvParent.getMultiViewParent().getData().getClass().getSimpleName() : "null"));
                
                if (mvParent.isTopLevel())
                {
                    enableNewBtn = true;
                } else
                {
                    enableNewBtn = mvParent.getMultiViewParent() != null && mvParent.getMultiViewParent().getData() != null;
                }
            }
            
            //log.debug(view.getName()+"  enableNewBtn "+enableNewBtn+"  isNewlyCreatedDataObj "+isNewlyCreatedDataObj()+" ("+(enableNewBtn && (dataObj == null || !isNewlyCreatedDataObj()))+")");
            
            // 03/27/08 - rods - Add isSingle check for SubForms that hold a single Object
            boolean isSingle      = rsController == null && origDataSet == null;
            boolean newBtnEnabled = enableNewBtn && (dataObj == null || (!isNewlyCreatedDataObj() && !isSingle));
            //log.debug("1----------------- Add "+formViewDef.getName()+"  "+newBtnEnabled+"----------------- ");
            
            if (!isJavaCollection())
            {
                newRecBtn.setEnabled(newBtnEnabled);
            }
            
            if (switcherUI != null)
            {
                switcherUI.setEnabled(enableNewBtn);
            }
        }

        if (srchRecBtn != null)
        {
            boolean enable = enableNewBtn && StringUtils.isNotBlank(searchName);
            srchRecBtn.setEnabled(enable);
            if (switcherUI != null)
            {
                switcherUI.setEnabled(enable);
            }
        }
        
        if (rsController != null && rsController.getSearchRecBtn() != null && switcherUI != null)
        {
            switcherUI.setEnabled(true);
        }
    }

    /**
     * Set the datObj into the form but controls 
     * @param dataObj the data object
     * @param alreadyInTheList indicates whether this dataObj is already in the list of data objects we are working with
     */
    @SuppressWarnings("unchecked")
    protected void setDataObj(final Object dataObj, final boolean alreadyInTheList)
    {
        //log.debug("Setting DataObj["+dataObj+"]");
        
        // rods - Added 3/21/08 because switching from the Grid View
        // back to the Form View causes the "+" button to be disabled and this
        // is because it thinks it is a newly created object for some reason
        isNewlyCreatedDataObj = false;
        
        // Convert the Set over to a List so the RecordController can be used
        Object data = dataObj;
        if (!alreadyInTheList)
        {
            if (data instanceof java.util.Set)
            {
                origDataSet = (Set)dataObj;
                List newList = Collections.list(Collections.enumeration(origDataSet));
                data = newList;
                
                if (newList.size() > 0)
                {
                    if (newList.get(0) instanceof Comparable<?>)
                    {
                        Collections.sort(newList);
                    }
                }
            }
        }

        // If there is a formValidator then we set the current object into the formValidator's scripting context
        // then turn off change notification while the form is filled
        if (formValidator != null && dataObj != null)
        {
            formValidator.addRuleObjectMapping("dataObj", dataObj);
        }
        
        if (selectorCBX != null)
        {
            selectorCBX.setEnabled(true);
        }

        boolean isList;
        boolean isVector;
        // if we do have a list then get the first object or null
        if (data instanceof Vector)
        {
            isList   = true;
            isVector = true;
            
        } else if (data instanceof List)
        {
            isList   = true;
            isVector = false;
        } else
        {
            isList   = false;
            isVector = false;
        }
        
        for (FVOFieldInfo fieldInfo : controlsById.values())
        {
            if (fieldInfo.isOfType(FormCellIFace.CellType.subview) ||
                fieldInfo.isOfType(FormCellIFace.CellType.iconview))
            {
                MultiView mv = fieldInfo.getSubView();
                if (mv != null)
                {
                    mv.setParentDataObj(null);
                    
                } else
                {
                    ((SubViewBtn)fieldInfo.getComp()).setParentDataObj(null);
                }
            }
            
            /*if (isEditing &&  fieldInfo.getComp() instanceof EditViewCompSwitcherPanel)
            {
                if (isDataValueNew)
                {
                    ((EditViewCompSwitcherPanel)fieldInfo.getComp()).putIntoEditMode();
                } else
                {
                    ((EditViewCompSwitcherPanel)fieldInfo.getComp()).putIntoViewMode();
                }
            }*/
        }

        // if we do have a list then get the first object or null
        if (isList)
        {
            if (isVector)
            {
                list = (Vector)data;
            } else
            {
                list = new Vector<Object>((List<?>)data);
            }
            
            if (list.size() > 0)
            {
                this.dataObj = list.get(0);
                //log.debug("Getting DO from list "+this.dataObj.getClass().getSimpleName()+" "+this.dataObj.hashCode());
                
            } else
            {
                this.dataObj = null;
            }

            // Now tell the RecordController how many Object we have
            if (rsController != null)
            {
                int len = list.size();
                if (AppContextMgr.isSecurityOn())
                {
                    ensurePermissions();
                    if (perm.hasNoPerm())
                    {
                        len = 0;
                    }
                }
                rsController.setLength(len);
                //updateControllerUI();
            }

            // Set the data from the into the form
            setDataIntoUI();

        } else
        {
            // OK, it is a single data object
            this.dataObj = dataObj;
            
            if (!alreadyInTheList && (this.list != null && this.dataObj != dataObj))
            {
                this.list = null;
            }

            setDataIntoUI();

            // Don't remove the rsController if the data is NULL because the next non-null one may be a list
            // mostly likely it will be
            if (rsController != null)
            {
                if (this.dataObj != null)
                {
                    // I added this 'if' and I have no idea why the call was here in the first place. - rods
                    // was it here for PaleoContext ?????
                    if (!alreadyInTheList)
                    {
                        //controlPanel.setRSCVisibility(!isEditting);
                        rsController.reset(); // rods - 07/07/08 just moved this into the this 'if' statement
                        // it has already caused problems not being n there.
                    }
                    
                } else
                {
                    rsController.clear();
                }
                updateControllerUI();
            }
        }
    }
    
    /**
     * Discards the current data object in the form. It may have been added to the List/Set
     * and need to be removed.
     * @param throwAway indicates whether it should throw or reload the data object.
     */
    public void discardCurrentObject(final boolean throwAway)
    {
        if (parentDataObj instanceof FormDataObjIFace)
        {
            ((FormDataObjIFace)parentDataObj).removeReference((FormDataObjIFace)dataObj, cellName);
        }
        
        if (throwAway || (dataObj instanceof FormDataObjIFace  && ((FormDataObjIFace)dataObj).getId() == null))
        {
            if (list != null)
            {
                list.remove(dataObj);
                int len = list.size();
                rsController.setLength(len);
                rsController.setIndex(len-1, false);
            }
            
            // I am punting here and just removing the last one
            if (recordSetItemList != null)
            {
                recordSetItemList.remove(recordSetItemList.size()-1);
            }
        } else if (dataObj instanceof FormDataObjIFace)
        {
            reloadDataObj();
            
        } else
        {
            setDataIntoUI();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataObj()
     */
    public Object getDataObj()
    {
        //log.debug("getDataObj " + this.getView().getName());
        return dataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setParentDataObj(java.lang.Object)
     */
    public void setParentDataObj(Object parentDataObj)
    {
        this.parentDataObj = parentDataObj;
        //updateControllerUI();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getParentDataObj()
     */
    public Object getParentDataObj()
    {
        return parentDataObj;
    }
    
    /**
     * @param enabled
     */
    public void setFormEnabled(final boolean enabled)
    {
        // Enable the labels
        for (FVOFieldInfo labelFI : labels.values())
        {
            labelFI.getComp().setEnabled(true);
        }

        // Enable the form controls
        for (FVOFieldInfo compFI : controlsById.values())
        {
            compFI.setEnabled(true);
        }
        
        /*if (!enabled)
        {
            for (MultiView kid : kids)
            {
                if (kid.getCurrentViewAsFormViewObj() != null)
                {
                    kid.getCurrentViewAsFormViewObj().setFormEnabled(enabled);
                }
            }
        }*/
    }
    
    /**
     * @return the actual value of isNewlyCreatedDataObj
     */
    public boolean getIsNewlyCreatedDataObj()
    {
        return isNewlyCreatedDataObj;
    }

    /**
     * @return the the top-levels value of isNewlyCreatedDataObj or it's own isNewlyCreatedDataObj if it is the top-level ro one doesn't exist.
     */
    public boolean isNewlyCreatedDataObj()
    {
        return (mvParent != null && mvParent.isTopLevel()) ?  mvParent.getCurrentViewAsFormViewObj().getIsNewlyCreatedDataObj() : isNewlyCreatedDataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataIntoUI()
     */
    public void setDataIntoUI()
    {
        setDataIntoUI(true, false);
    }
    
    /**
     * Makes sure we have gotten the permissions.
     */
    private void ensurePermissions()
    {
        // Make sure we get the right class name, the dataObj is sometimes a "Set<?>"
        if (perm == null)
        {
            Class<?> cls;
            if (classToCreate == null)
            {
                try
                {
                    cls = Class.forName(view.getClassName());
                } catch (Exception ex) 
                {
                    cls = dataObj.getClass();
                }
                
            } else
            {
                cls = classToCreate;
            }
            perm = SecurityMgr.getInstance().getPermission("DO."+cls.getSimpleName().toLowerCase());
        }
    }

    /**
     * 
     * @return true skip session.attach, false do it
     */
    public boolean isSkippingAttach()
    {
        return isSkippingAttach;
    }

    /**
     * Set this to true if you do not want the form to do an attach before filling in the entire form
     * 
     * @param isSkippingAttach true skip session.attach, false do it
     */
    public void setSkippingAttach(final boolean isSkippingAttach)
    {
        this.isSkippingAttach = isSkippingAttach;
        for (FVOFieldInfo fi : controlsById.values())
        {
            if (fi.getFormCell() instanceof FormCellSubView)
            {
                if (fi.getComp() instanceof SubViewBtn)
                {
                    ((SubViewBtn)fi.getComp()).setSkippingAttach(isSkippingAttach);
                    
                } else if (fi.getSubView() != null)
                {
                    if (fi.getSubView().getCurrentView() != null)
                    {
                        fi.getSubView().getCurrentView().setSkippingAttach(isSkippingAttach);
                    } 
                }   
            }
        }
    }

    /**
     * Fill the form with data, indicate whether the form should be reset because the data is new.
     * @param doResetAfterFill tells the form to be reset after filling, as if it was new data.
     * 
     */
    protected void setDataIntoUI(final boolean doResetAfterFill,
                                 final boolean forceCreateSession)
    {
        
        if (dataObj != null)
        {
            if (AppContextMgr.isSecurityOn())
            {
                ensurePermissions();
                if ((isEditing && perm.isViewOnly()) || (!isEditing && !perm.canView()))
                {
                    return;
                }
            }
        }

        if (businessRules != null)
        {
            businessRules.beforeFormFill();
        }
        
        if (!isSkippingAttach && dataObj != null && dataObj instanceof FormDataObjIFace && ((FormDataObjIFace)dataObj).getId() != null)
        {
            if (mvParent == null || mvParent.isTopLevel() || forceCreateSession)
            {
                if (session != null)
                {
                    session.close();
                }
                
                session = DataProviderFactory.getInstance().createSession();
                
                if (session != null && mvParent != null)
                {
                    mvParent.setSession(session);   
                }
                
                try
                {
                    session.attach(dataObj);
                    //((FormDataObjIFace)dataObj).forceLoad();
                }
                catch (HibernateException ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHQLUsageCount();
                    //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormViewObj.class, ex);
                    
                    //Oh No!
                    //take the drastic measures in the unreachable else block below...
                    Object beforeSaveDataObj = dataObj;
                    try
                    {
                        dataObj = session.merge(dataObj);
                    }
                    catch (Exception ex2)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormViewObj.class, ex2);
                        throw new RuntimeException(ex2);
                    }
                    replaceDataObjInList(beforeSaveDataObj, dataObj);
                    if (origDataSet != null)
                    {
                        origDataSet.remove(beforeSaveDataObj);
                        origDataSet.add(dataObj);
                    }
                }
            }
        }
        
        // Now turn off data change notification and then validate the form
        if (formValidator != null)
        {
            formValidator.setDataChangeNotification(false);
            formValidator.setEnabled(dataObj != null);
            
            if (dataObj == null)
            {
                formValidator.setState(UIValidatable.ErrorType.Valid);
            }
            
            // I have always wanted to avoid doing this globally
            // but it is the best approach
           
            UIValidator.setIgnoreAllValidation(this, true);
        }

        boolean weHaveData = true;

        DataObjectGettable dg = formViewDef.getDataGettable();

        // This is a short circut for when we were switch from being enabled to disabled or visa-versus
        // This way we won't need to set the controls enabled or disabled each time we advance to a new record,
        // we only have to do it once
        if (dataObj == null && !wasNull)
        {
            // Disable all the labels
            for (FVOFieldInfo labelFI : labels.values())
            {
                labelFI.getComp().setEnabled(false);
            }

            // Disable all the form controls and set their values to NULL
            for (FVOFieldInfo fieldInfo : controlsById.values())
            {
                fieldInfo.getComp().setEnabled(false);
                
                if (fieldInfo.isOfType(FormCellIFace.CellType.field))
                {
                    setDataIntoUIComp(fieldInfo.getComp(), null, null);
                    //log.debug("Setting ["+fieldInfo.getName()+"] to enabled=false");

                } else if (fieldInfo.isOfType(FormCellIFace.CellType.subview))
                {
                    MultiView mv = fieldInfo.getSubView();
                    if (mv != null)
                    {
                        mv.setData(null);
                    } else
                    {
                        setDataIntoUIComp(fieldInfo.getComp(), null, null);
                    }
                    
                }
            }
            // Disable the ResultSet Controller
            if (rsController != null)
            {
                rsController.clear();
            }
            wasNull    = true;
            weHaveData = false;

        } else if (dataObj != null && wasNull)
        {
            setFormEnabled(true);

            // Enable the ResultSet Controller
            if (rsController != null)
            {
                rsController.reset();
            }
            wasNull = false;
        }

        
        if (draggableRecIdentifier != null && this.dataObj != null && this.dataObj instanceof FormDataObjIFace)
        {
            FormDataObjIFace formDataObj = (FormDataObjIFace)this.dataObj;
            draggableRecIdentifier.setFormDataObj(formDataObj);
        }
        
        if (weHaveData)
        {
            //log.debug("*************************** weHaveData!" + dataObj);
            
            Object[] defaultDataArray = new Object[1]; // needed for setting the default value
            
            // Now we know the we have data, so loop through all the controls
            // and set their values
            for (FVOFieldInfo fieldInfo : controlsById.values())
            {
                Component comp = fieldInfo.getComp();
                
                Object data = null;

                // This is for panels that use in layout but have no data
                if (fieldInfo.isOfType(FormCellIFace.CellType.field))
                {
                    // Do Formatting here
                    FormCellField cellField         = (FormCellField)fieldInfo.getFormCell();
                    String        dataObjFormatName = cellField.getFormatName();
                    String        defaultValue      = cellField.getDefaultValue();
                    
                    // 02/13/08 - ignore means ignore
                    if (cellField.isIgnoreSetGet())
                    {
                        continue;
                    }
                    
                    boolean isTextFieldPerMode = cellField.isTextFieldForMode(altView.getMode());

                    boolean useDataObjFormatName = isTextFieldPerMode && isNotEmpty(dataObjFormatName);
                    //log.debug("["+cellField.getName()+"] useFormatName["+useFormatName+"]  "+comp.getClass().getSimpleName());

                    if (useDataObjFormatName)
                    {
                        if (cellField.getFieldNames().length > 1)
                        {
                            throw new RuntimeException("formatName ["+dataObjFormatName+"] only works on a single value.");
                        }
                        
                        Object[] values = UIHelper.getFieldValues(cellField.getFieldNames(), dataObj, dg);
                        
                        setDataIntoUIComp(comp, DataObjFieldFormatMgr.getInstance().format(values[0], dataObjFormatName), defaultValue);

                    } else
                    {
                        Object[] values;
                        if (((FormCellFieldIFace)fieldInfo.getFormCell()).useThisData())
                        {
                            values = new Object[] {dataObj};
                            
                        } else if (fieldInfo.getFormCell().isIgnoreSetGet())
                        {
                            defaultDataArray[0] = defaultValue;
                            values              = defaultDataArray;
                            
                        } else
                        {
                            values = UIHelper.getFieldValues(cellField, dataObj, dg);
                        }

                        if (values != null && values.length > 0)
                        {
                            String format = cellField.getFormat();
                            if (tableInfo != null && isEmpty(format))
                            {
                                DBFieldInfo fi = tableInfo.getFieldByName(fieldInfo.getFormCell().getName());
                                if (fi != null)
                                {
                                    format = fi.getFormatStr();
                                }
                            }
                            
                            if (isNotEmpty(format))
                            {
                                setDataIntoUIComp(comp, UIHelper.getFormattedValue(values, format), defaultValue);

                            } else
                            {
                                if (cellField.getFieldNames().length > 1)
                                {
                                    throw new RuntimeException("No Format but mulitple fields were specified for["+cellField.getName()+"]");
                                }

                                if (values[0] == null)
                                {
                                    setDataIntoUIComp(comp, isTextFieldPerMode ? "" : null, defaultValue);
                                    
                                } else
                                {
                                    setDataIntoUIComp(comp, isTextFieldPerMode && !(values[0] instanceof Number) ? values[0].toString() : values[0], defaultValue);
                                }

                            }
                        } else
                        {
                            setDataIntoUIComp(comp, null, defaultValue);
                        }
                    }

                } else if (fieldInfo.isOfType(FormCellIFace.CellType.subview))
                {
                    if (fieldInfo.getFormCell().isIgnoreSetGet())
                    {
                        continue;
                    }

                    MultiView mv = fieldInfo.getSubView();
                    if (mv != null)
                    {
                        mv.setParentDataObj(dataObj);
                        
                    } else
                    {
                        ((SubViewBtn)fieldInfo.getComp()).setParentDataObj(dataObj);
                    }
                    
                    try // XXX RELEASE remove try block
                    {
                        Object[] values = UIHelper.getFieldValues(fieldInfo.getFormCell(), dataObj, dg);
                        data = values != null ? values[0] : null;
                        
                    } catch (NullPointerException ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormViewObj.class, ex);
                        
                        log.error("FieldCell["+fieldInfo.getFormCell().getName()+" data["+dataObj+"]");
                        throw new RuntimeException(ex);
                    }
                    
                    if (data != null)
                    {
                        if (((FormCellSubViewIFace)fieldInfo.getFormCell()).isSingleValueFromSet() && data instanceof Set)
                        {
                            Set<?> set = (Set<?>)data;
                            if (set.size() > 0)
                            {
                                data = set.iterator().next();
                            }
                        }
                        
                        if (mv != null)
                        {
                            mv.setData(data);
                            
                        } else
                        {
                            ((SubViewBtn)fieldInfo.getComp()).setValue(data, null);
                        }
                    } else if (fieldInfo.getComp() instanceof MultiView)
                    {
                        ((MultiView)fieldInfo.getComp()).setData(data);
                        
                    } else
                    {
                        ((SubViewBtn)fieldInfo.getComp()).setValue(data, null);
                    }
                }
                
                if (fieldInfo.getComp() instanceof EditViewCompSwitcherPanel)
                {
                    boolean isOK = !(dataObj instanceof FormDataObjIFace) || ((FormDataObjIFace)dataObj).getId() == null;
                    if (isEditing && isOK)
                    {
                        ((EditViewCompSwitcherPanel)fieldInfo.getComp()).putIntoEditMode();
                    } else
                    {
                        ((EditViewCompSwitcherPanel)fieldInfo.getComp()).putIntoViewMode();
                    }
                }
            }
        }
        
        // Adjust the formValidator now that all the data is in the controls
        if (doResetAfterFill && formValidator != null)
        {
            formValidator.reset(MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT));
        }
        
        if (businessRules != null)
        {
            businessRules.afterFillForm(dataObj);
        }


        //if (doResetAfterFill && mvParent != null && mvParent.isTopLevel() && saveControl != null && isEditing)
        //{
        //    saveControl.setEnabled(false);
        //}
        
        // See comment above where I turn this on
        if (formValidator != null)
        {
            UIValidator.setIgnoreAllValidation(this, false);
        }
        
        updateControllerUI();
        
        if (session != null && (mvParent == null || mvParent.isTopLevel() || forceCreateSession))
        {
            session.close();
            session = null;
            
            if (mvParent != null)
            {
                mvParent.setSession(session);  
            }
        }

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataFromUI()
     */
    public void getDataFromUI()
    {
        if (isEditing)
        {
            // This should only happen when the user created a new object
            // in a form in a dialog and then they pressed Cancel without it being saved.
            /*if (isNewlyCreatedDataObj)
            {
                if (parentDataObj instanceof FormDataObjIFace)
                {
                    ((FormDataObjIFace)parentDataObj).addReference((FormDataObjIFace)dataObj, cellName);
                    
                } else
                {
                    FormHelper.addToParent(parentDataObj, dataObj);
                }
                isNewlyCreatedDataObj = false;
            }*/
            
            if (formValidator != null && formValidator.getState() != UIValidatable.ErrorType.Valid)
            {
                if (isNewlyCreatedDataObj)
                {
                    if (list != null)
                    {
                        list.remove(dataObj);
                    }
                    
                    if (origDataSet != null)
                    {
                        origDataSet.remove(dataObj);
                    }
                    formValidator.setFormValidationState(UIValidatable.ErrorType.Valid);
                    formValidator.reset(true);
                    formValidator.validateRoot();
                }
                return;
            }
            
            DataObjectSettable ds = formViewDef.getDataSettable();
            DataObjectGettable dg = formViewDef.getDataGettable();
            if (ds != null)
            {
                
                // Get Data From Selector
                if (selectorCBX != null)
                {
                    String selectorName = altView.getSelectorName();
                    if (StringUtils.isNotEmpty(selectorName))
                    {
                        try
                        {
                            PropertyDescriptor descr = PropertyUtils.getPropertyDescriptor(dataObj, selectorName);
                            Object selectorValObj = UIHelper.convertDataFromString(altView.getSelectorValue(), descr.getPropertyType());
                            
                            FormHelper.setFieldValue(selectorName, dataObj, selectorValObj, dg, ds);
                            
                        } catch (Exception ex)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormViewObj.class, ex);
                            log.error(ex);
                            // XXX TODO Show error dialog here
                        }
                    }
                }
                
                for (FVOFieldInfo fieldInfo : controlsById.values())
                {
                    //String nm = fieldInfo.getFormCell().getName();
                    //if (nm.equals("collection.collectionName"))
                    //{
                    //    int x = 0;
                    //    x++;
                    //}
                    
                    FormCellIFace fc = fieldInfo.getFormCell();
                    boolean isInoreGetSet = fc.isIgnoreSetGet();
                    boolean isReadOnly;
                    boolean useThisData; // meaning the control is using the same data object as what is in the form
                                         // so we don't need to go get the data (skip it)
                    if (fc instanceof FormCellField)
                    {
                        FormCellFieldIFace fcf = (FormCellFieldIFace)fc;
                        isReadOnly    = fcf.isReadOnly();// || fcf.isEditOnCreate();
                        useThisData   = fcf.useThisData();
                        
                    } else
                    {
                        useThisData   = false;
                        isReadOnly    = false;
                    }
                    
                    String id = fieldInfo.getFormCell().getIdent();
                    
                    //log.debug(fieldInfo.getName()+"\t"+fieldInfo.getFormCell().getName()+"\t   hasChanged: "+(!isReadOnly && hasFormControlChanged(id)));
                    
                     if (!isReadOnly && !isInoreGetSet && hasFormControlChanged(id))
                    {
                        // this ends up calling the getData on the GetSetValueIFace 
                        // which enables the control to set data into the data object
                        if (useThisData)
                        {
                            getDataFromUIComp(id); 
                            continue;
                        }
                            
                       //log.debug(fieldInfo.getName()+"  "+fieldInfo.getFormCell().getName() +"  HAS CHANGED!");
                        Object uiData = getDataFromUIComp(id); // if ID is null then we have huge problems
                        // if (uiData != null && dataObj != null) Changed for Bug 4994
                        if (dataObj != null && (uiData != null || !(fieldInfo.getFormCell() instanceof FormCellSubViewIFace)))
                        {
                            //log.info(fieldInfo.getFormCell().getName()+" "+(dataObj != null ? dataObj.getClass().getSimpleName() : "dataObj was null"));
                            FormHelper.setFieldValue(fieldInfo.getFormCell().getName(), dataObj, uiData, dg, ds);
                        }
                    }
                }
            } else
            {
                throw new RuntimeException("Calling getDataFromUI when the DataObjectSettable is null for the form.");
            }
        }
    }

    /**
     * If the control supports UIValidatable interface then it return whether the controls has been changed. If it
     * doesn't then it assumes it has and returns true.
     * @param id the id of the control
     * @return If the control supports UIValidatable interface then it return whether the controls has been changed. If it
     * doesn't then it assumes it has and returns true.
     */
    public boolean hasFormControlChanged(final String id)
    {
        FVOFieldInfo fieldInfo = controlsById.get(id);
        if (fieldInfo != null)
        {
            Component comp = fieldInfo.getComp();
            if (comp != null)
            {
                if (comp instanceof UIValidatable)
                {
                    boolean hasChanged = ((UIValidatable)comp).isChanged();
                    if (!hasChanged && comp instanceof AutoNumberableIFace)
                    {
                        hasChanged = true;
                        return hasChanged;
                    }
                    
                    boolean hasDefaultData = false;
                    if (fieldInfo.getFormCell() instanceof FormCellFieldIFace)
                    {
                        hasDefaultData = StringUtils.isNotEmpty(((FormCellFieldIFace)fieldInfo.getFormCell()).getDefaultValue());
                    }
                    return ((UIValidatable)comp).isChanged() || hasDefaultData;
                }
            }
        }
        return true;
    }
    
    /**
     * @return whether it is in Edit Mode
     */
    public boolean isEditing()
    {
        return isEditing;
    }

    /**
     * @param comp
     * @param isSingleValueFromSet
     * @param isCommand
     * @param id
     * @return
     */
    public static Object getValueFromComponent(final Component comp, 
                                               final boolean isSingleValueFromSet,
                                               final boolean isCommand,
                                               final String   id)
    {
        if (comp != null)
        {
            if (comp instanceof GetSetValueIFace)
            {
                return ((GetSetValueIFace)comp).getValue();

            } else if (comp instanceof MultiView)
            {
                if (isSingleValueFromSet)
                {
                    return ((MultiView)comp).getData();
                } 
                // else
                return null;

            } else if (comp instanceof JTextField)
            {
                return ((JTextField)comp).getText();

            } else if (comp instanceof JComboBox)
            {
                if (comp instanceof JAutoCompComboBox)
                {
                    PickListItemIFace pli = (PickListItemIFace)((JAutoCompComboBox)comp).getSelectedItem();
                    return pli.getValueObject() == null ? pli.getValue() : pli.getValueObject();

                }
                // else
                return ((JComboBox)comp).getSelectedItem().toString();

            } else if (comp instanceof JLabel)
            {
                return ((JLabel)comp).getText();

            } else if (comp instanceof ColorChooser)
            {
                return ColorWrapper.toString(((ColorChooser)comp).getBackground());

            } else if (comp instanceof JList)
            {
                return ((JList)comp).getSelectedValue().toString();

            } else if (comp instanceof JCheckBox)
            {
                return new Boolean(((JCheckBox)comp).isSelected());

            } else if (isCommand)
            {
               // no op
            } else
            {
                log.error("Not sure how to get data from object "+comp);
            }
        } else
        {
            log.error("Component is null in FieldInfo "+id);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataFromUIComp(java.lang.String)
     */
    public Object getDataFromUIComp(final String id)
    {
        FVOFieldInfo fieldInfo = controlsById.get(id);
        if (fieldInfo != null)
        {
            boolean isSingleValueFromSet = false;
            if (fieldInfo.getFormCell() instanceof FormCellSubViewIFace)
            {
                isSingleValueFromSet = ((FormCellSubViewIFace)fieldInfo.getFormCell()).isSingleValueFromSet();
            }
            
            return getValueFromComponent(fieldInfo.getComp(), 
                    isSingleValueFromSet, 
                    fieldInfo.getFormCell().getType() == FormCellIFace.CellType.command,
                    id);
            
        }
        log.error("FieldInfo is null "+id);
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getSubView(java.lang.String)
     */
    public MultiView getSubView(final String name)
    {
        // do linear search because there will never be very many of them
        for (MultiView mv : kids)
        {
            if (mv.getViewName().equals(name))
            {
                return mv;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataIntoUIComp(java.lang.String, java.lang.Object)
     */
    public void setDataIntoUIComp(final String id, Object data)
    {
        setDataIntoUIComp(controlsById.get(id).getComp(), data, null);
    }


    /**
     * Helper class to set data into a component
     * @param comp the component to get the data
     * @param data the data to be set into the component
     */
    public static void setDataIntoUIComp(final Component comp, final Object data, final String defaultValue)
    {
        if (comp instanceof GetSetValueIFace)
        {
            ((GetSetValueIFace)comp).setValue(data, defaultValue);

        } else if (comp instanceof MultiView)
        {
            ((MultiView)comp).setData(data);

        } else if (comp instanceof JTextField)
        {
            JTextField tf = (JTextField)comp;
            tf.setText(data == null ? "" : data.toString());
            tf.setCaretPosition(0);

        } else if (comp instanceof JTextArea)
        {
            //log.debug(name+" - "+comp.getPreferredSize()+comp.getSize());
            ((JTextArea)comp).setText(data == null ? "" : data.toString());

        } else if (comp instanceof JCheckBox)
        {
            //log.debug(name+" - "+comp.getPreferredSize()+comp.getSize());
            if (data != null)
            {
                ((JCheckBox)comp).setSelected((data instanceof Boolean) ? ((Boolean)data).booleanValue() : data.toString().equalsIgnoreCase("true"));
            } else
            {
                ((JCheckBox)comp).setSelected(false);
            }

        } else if (comp instanceof JLabel)
        {
            ((JLabel)comp).setText(data == null ? "" : data.toString());

        } else if (comp instanceof JComboBox)
        {
            setComboboxValue((JComboBox)comp, data);

        } else if (comp instanceof JList)
        {
            setListValue((JList)comp, data);
        }

        // Reset it's state as not being changes,
        // because setting in data will cause the change flag to be set
        if (comp instanceof UIValidatable)
        {
            ((UIValidatable)comp).setChanged(false);
        }
    }

    /**
     * Sets the appropriate index in the combobox for the value
     * @param comboBox the combobox
     * @param data the data value
     */
    protected static void setComboboxValue(final JComboBox comboBox, final Object data)
    {
        ComboBoxModel  model = comboBox.getModel();

        for (int i=0;i<comboBox.getItemCount();i++)
        {
            Object item = model.getElementAt(i);
            if (item instanceof String)
            {
                if (((String)item).equals(data))
                {
                    comboBox.setSelectedIndex(i);
                    break;
                }
            } else if (item.equals(data))
            {
                comboBox.setSelectedIndex(i);
                break;
            }
        }

    }

    /**
     * Sets the appropriate index in the list box
     * @param list the list box
     * @param data the data value
     */
    protected static void setListValue(final JList list, final Object data)
    {

        Iterator<?> iter = null;
        if (data instanceof Set)
        {
            iter = ((Set<?>)data).iterator();

        } else if (data instanceof org.hibernate.collection.PersistentSet)
        {
            iter = ((org.hibernate.collection.PersistentSet)data).iterator();
        }

        if (iter != null)
        {
            DefaultListModel defModel = new DefaultListModel();
            while (iter.hasNext())
            {
                defModel.addElement(iter.next());
            }
            list.setModel(defModel);

        } else
        {
            ListModel  model = list.getModel();
            for (int i=0;i<model.getSize();i++)
            {
                Object item = model.getElementAt(i);
                if (item instanceof String)
                {
                    if (((String)item).equals(data))
                    {
                        list.setSelectedIndex(i);
                        return;
                    }
                } else if (item.equals(data))
                {
                    list.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getControllerPanel()
     */
    @Override
    public JComponent getControllerPanel()
    {
        return sepController == null || sepController.getComponentCount() == 0 ? null : sepController;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getView()
     */
    public ViewIFace getView()
    {
        return view;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#hideMultiViewSwitch(boolean)
     */
    public void hideMultiViewSwitch(boolean hide)
    {
        if (switcherUI != null)
        {
            switcherUI.setVisible(!hide);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#dataHasChanged()
     */
    public void validationWasOK(boolean wasOK)
    {
       if (saveControl != null && (mvParent == null || mvParent.hasChanged()))
       {
           saveControl.setEnabled(wasOK);
       }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setSession(org.hibernate.Session)
     */
    public void setSession(final DataProviderSessionIFace session)
    {
        //log.debug("setSession "+hashCode() + " Session ["+(session != null ? session.hashCode() : "null")+"] ");
        this.session = session;
    }

    /**
     * @return the session
     */
    public DataProviderSessionIFace getSession()
    {
        return session;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setCellName(java.lang.String)
     */
    public void setCellName(final String cellName)
    {
        this.cellName = cellName;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#registerSaveBtn(javax.swing.JButton)
     */
    public void registerSaveBtn(final JButton saveBtnArg)
    {
        this.saveControl = saveBtnArg;
        this.saveControl.setOpaque(false);

        if (formValidator != null)
        {
            formValidator.addEnableItem(saveControl, FormValidator.EnableType.ValidAndChangedItems);
        }

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#updateSaveBtn()
     */
    public void updateSaveBtn()
    {
        if (saveControl != null && formValidator != null)
        {
            if (mvParent == null || mvParent.isAllValidationOK())
            {
                validationWasOK(formValidator.getState() == UIValidatable.ErrorType.Valid);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#focus()
     */
    public void focus()
    {
        focusFirstFormControl();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#aboutToShutdown()
     */
    public void aboutToShutdown()
    {
        if (businessRules != null)
        {
            businessRules.aboutToShutdown();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#shutdown()
     */
    public void shutdown()
    {
        for (Enumeration<FVOFieldInfo> e=controlsById.elements(); e.hasMoreElements();)
        {
            FVOFieldInfo fieldInfo = e.nextElement();
            fieldInfo.shutdown();
        }
        compsList.clear();
        controlsById.clear();
        controlsByName.clear();
        labels.clear();
        
        if (altViewsList != null)
        {
            altViewsList.clear();
        }

        kids.clear();

        mvParent    = null;
        formViewDef = null;
        formComp    = null;
        
        if (formValidator != null)
        {
            formValidator.removeValidationListener(this);
            FormValidator parent = formValidator.getParent();
            if (parent != null)
            {
                parent.remove(formValidator);
            }
            formValidator.setParent(null);
        }
        
        if (businessRules != null)
        {
            businessRules.formShutdown();
            businessRules = null;
        }
    }
    
    //-------------------------------------------------
    // ViewBuilderIFace
    //-------------------------------------------------

    /**
     * Adds a control by name so it can be looked up later.
     * @param formCell the FormCell def that describe the cell
     * @param label the the label to be added
     */
    public void addLabel(final FormCellLabel formCell, final JLabel label)
    {

        if (formCell != null && StringUtils.isNotEmpty(formCell.getLabelFor()))
        {
            if (labels.get(formCell.getLabelFor()) != null)
            {
                String str = "Two labels have the same id ["+formCell.getLabelFor()+"] "+formViewDef.getName();
                UIRegistry.showError(str);
                //throw new RuntimeException("Two labels have the same id ["+formCell.getLabelFor()+"] "+formViewDef.getName());
            }
            labels.put(formCell.getLabelFor(), new FVOFieldInfo(formCell, label, null, labels.size()));
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#registerControl(edu.ku.brc.ui.forms.persist.FormCellIFace, java.awt.Component)
     */
    public void registerControl(final FormCellIFace formCell, final Component control)
    {
        if (formCell != null)
        {
            boolean isThis = formCell.getName().equals("this");
            
            if (controlsById.get(formCell.getIdent()) != null)
            {
                throw new RuntimeException("Two controls have the same id ["+formCell.getIdent()+"] "+formViewDef.getName());
            }

            if (!isThis && controlsByName.get(formCell.getName()) != null)
            {
                throw new RuntimeException("Two controls have the same name ["+formCell.getName()+"] "+formViewDef.getName());
            }

            JScrollPane scrPane;
            Component comp;
            if (control instanceof JScrollPane)
            {
                scrPane = (JScrollPane)control;
                comp = scrPane.getViewport().getView();
            } else
            {
                scrPane = null;
                comp = control;
            }
            
            FVOFieldInfo fieldInfo = new FVOFieldInfo(formCell, comp, scrPane, controlsById.size());
            controlsById.put(formCell.getIdent(), fieldInfo);
            if (!isThis)
            {
                controlsByName.put(formCell.getName(), fieldInfo);
            }
            compsList.add(fieldInfo);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#registerPlugin(edu.ku.brc.ui.forms.persist.FormCellIFace, edu.ku.brc.af.ui.forms.UIPluginable)
     */
    public void registerPlugin(FormCellIFace formCell, UIPluginable uip)
    {
        boolean isThis = formCell.getName().equals("this");
        
        if (controlsById.get(formCell.getIdent()) != null)
        {
            throw new RuntimeException("Two controls have the same id ["+formCell.getIdent()+"] "+formViewDef.getName());
        }

        if (!isThis && controlsByName.get(formCell.getName()) != null)
        {
            throw new RuntimeException("Two controls have the same name ["+formCell.getName()+"] "+formViewDef.getName());
        }
        
        uip.addPropertyChangeListener(this);
        
        FVOFieldInfo fieldInfo = new FVOFieldInfo(formCell, uip, controlsById.size());
        controlsById.put(formCell.getIdent(), fieldInfo);
        if (!isThis)
        {
            controlsByName.put(formCell.getName(), fieldInfo);
            compsList.add(fieldInfo);
        }
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#addControlToUI(java.awt.Component, int, int, int, int)
     */
    public void addControlToUI(Component control, int colInx, int rowInx, int colSpan, int rowSpan)
    {
        builder.add(control, cc.xywh(colInx, rowInx, colSpan, rowSpan));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#createSeparator(java.lang.String)
     */
    public Component createSeparator(String title)
    {
        int        titleAlignment  = builder.isLeftToRight() ? SwingConstants.LEFT : SwingConstants.RIGHT;
        JComponent titledSeparator = builder.getComponentFactory().createSeparator(title, titleAlignment);
        setControlSize(titledSeparator);

        return titledSeparator;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#addRecordIndentifier(java.lang.String, javax.swing.ImageIcon)
     */
    public JComponent createRecordIndentifier(String title, ImageIcon icon)
    {
        PanelBuilder panelBldr = new PanelBuilder(new FormLayout("16px,1px,f:p:g", "p"));
        draggableRecIdentifier = DraggableRecordIdentifierFactory.getInstance().createDraggableRecordIdentifier(icon);
        //draggableRecIdentifier.setLabel(title);
        
        panelBldr.add(draggableRecIdentifier, cc.xy(1, 1));
        panelBldr.addSeparator(title, cc.xy(3, 1));
        
        return panelBldr.getPanel();
    }

    
    /**
     * Adds a control by name so it can be looked up later
     * @param formCell the FormCell def that describe the cell
     * @param subView the subView
     * @param colInx column index
     * @param rowInx row index
     * @param colSpan column span
     * @param rowSpan row span
     * @param addIt add it to the layout
     */
    public void addSubView(final FormCellSubView formCell, final MultiView subView, final int colInx, final int rowInx, final int colSpan, final int rowSpan, final boolean addIt)
    {
        if (formCell != null)
        {
            if (controlsById.get(formCell.getIdent()) != null)
            {
                UIRegistry.showError("Two controls have the same id ["+formCell.getIdent()+"] "+formViewDef.getName() +
                        "\nThe form will not operate correctly. Please fix it before continuing.");
                formCell.setIdent(Long.toString((Calendar.getInstance().getTimeInMillis())));
            }

            if (!formCell.getName().equals("this") && controlsByName.get(formCell.getName()) != null)
            {
                throw new RuntimeException("Two controls have the same Name ["+formCell.getName()+"] "+formViewDef.getName());
            }

            if (addIt)
            {
                builder.add(subView, cc.xywh(colInx, rowInx, colSpan, rowSpan, "fill,fill"));
            }
            
            FVOFieldInfo fi = new FVOFieldInfo(formCell, subView, controlsById.size());
            controlsById.put(formCell.getIdent(), fi);
            controlsByName.put(formCell.getName(), fi);
            kids.add(subView);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#addSubView(edu.ku.brc.ui.forms.persist.FormCellSubView, edu.ku.brc.ui.forms.MultiView, int, int, int, int)
     */
    public void addSubView(final FormCellSubView formCell, final MultiView subView, final int colInx, final int rowInx, final int colSpan, final int rowSpan)
    {
        addSubView(formCell, subView, colInx, rowInx, colSpan, rowSpan, true);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#closeSubView(edu.ku.brc.ui.forms.persist.FormCellSubView)
     */
    public void closeSubView(FormCellSubView formCell)
    {
        // not supported
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#shouldFlatten()
     */
    public boolean shouldFlatten()
    {
        return false;
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#getControlByName(java.lang.String)
     */
    public Component getControlByName(final String name)
    {
        FVOFieldInfo fieldInfo = controlsByName.get(name);
        // If it wasn't found in the immediate form then 
        // recurse through all the SubViews
        if (fieldInfo == null)
        {
            for (MultiView mv : kids)
            {
                if (mv != null)
                {
                    FormViewObj fvo = mv.getCurrentViewAsFormViewObj();
                    if (fvo != null)
                    {
                        Component comp = fvo.getControlByName(name);
                        if (comp != null)
                        {
                            return comp;
                        }
                    }
                }
            }
        } else
        {
            if (fieldInfo.comp instanceof EditViewCompSwitcherPanel)
            {
                return ((EditViewCompSwitcherPanel)fieldInfo.comp).getCurrentComp();
            }
            return fieldInfo.comp;
        }
        return null;
    }

    /**
     * Returns the FormViewObj for the control with the name passed in.
     * @param name the name of the control
     * @return the FormViewObj that the control with "name" belongs to.
     */
    public FormViewObj getFormViewObjForControlName(final String name)
    {
        FVOFieldInfo fieldInfo = controlsByName.get(name);
        // If it wasn't found in the immediate form then 
        // recurse through all the SubViews
        if (fieldInfo == null)
        {
            for (MultiView mv : kids)
            {
                if (mv != null)
                {
                    FormViewObj fvo = mv.getCurrentViewAsFormViewObj();
                    if (fvo != null)
                    {
                        Component comp = fvo.getControlByName(name);
                        if (comp != null)
                        {
                            return fvo;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * If the Control is found and implements UIValidatable it gets set to be changed;
     * and if it has a DataChangeNotifier then that is also set.
     * @param controlName the name of the control
     */
    public void setControlChanged(final String controlName)
    {
        FVOFieldInfo   fieldInfo   = controlsByName.get(controlName);
        Component   comp        = null;
        FormViewObj formViewObj = null;
        
        // If it wasn't found in the immediate form then 
        // recurse through all the SubViews
        if (fieldInfo == null)
        {
            for (MultiView mv : kids)
            {
                if (mv != null)
                {
                    FormViewObj fvo = mv.getCurrentViewAsFormViewObj();
                    if (fvo != null)
                    {
                        comp = fvo.getControlByName(controlName);
                        if (comp != null)
                        {
                            formViewObj = fvo;
                            break;
                        }
                    }
                }
            }
        } else
        {
            comp        = fieldInfo.getComp();
            formViewObj = this;
        }
        
        if (comp != null && formViewObj != null)
        {
            if (comp instanceof UIValidatable)
            {
                ((UIValidatable)comp).setChanged(true);
                
            }
            formViewObj.getValidator().setDataChangeInNotifier(comp);
        }
    }
    
    /**
     * @return the rsController
     */
    public ResultSetController getRsController()
    {
        return rsController;
    }

    /**
     * @return the businessRules
     */
    public BusinessRulesIFace getBusinessRules()
    {
        return businessRules;
    }
    
    /**
     * This hooks up all the labels so double click bring up the usage notes (the description).
     */
    public void addUsageNotes()
    {
        if (tableInfo == null)
        {
            tableInfo = DBTableIdMgr.getInstance().getByClassName(formViewDef.getClassName());
        }
        
        if (tableInfo != null)
        {
            for (String idStr : controlsById.keySet())
            {
                FVOFieldInfo fc  = controlsById.get(idStr);
                if (StringUtils.isNotEmpty(fc.getName()))
                {
                    final DBTableChildIFace tci = tableInfo.getItemByName(fc.getName());
                    if (tci != null && StringUtils.isNotEmpty(tci.getDescription()))
                    {
                        FVOFieldInfo lbl = labels.get(idStr);
                        if (lbl != null)
                        {
                            lbl.getComp().addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent e)
                                {
                                    super.mouseClicked(e);
                                    if (e.getClickCount() == 2)
                                    {
                                        JOptionPane.showMessageDialog(UIRegistry.getMostRecentWindow(),
                                                "<html>"+tci.getDescription(), 
                                                UIRegistry.getResourceString("FormViewObj.UNOTES"), 
                                                JOptionPane.INFORMATION_MESSAGE);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }
    
    //-----------------------------------------------------
    // ValidationListener
    //-----------------------------------------------------

    /* (non-Javadoc)
     * @see ValidationListener#wasValidated(UIValidator)
     */
    public void wasValidated(final UIValidator validator)
    {
        if (formValidator != null)
        {
            formValidator.updateValidationBtnUIState();
            
            // The Validator enabled the Delete Btn
            // Now make sure the Delete Btn should still be enabled
            /*if (delRecBtn != null && 
                delRecBtn.isEnabled() &&
                dataObj != null && 
                (dataObj instanceof FormDataObjIFace && ((FormDataObjIFace)dataObj).getId() == null))
            {
                log.debug("2----------------- "+formViewDef.getName()+"  false----------------- ");
                //delRecBtn.setEnabled(false);
            }*/
        }
    }

    //-------------------------------------------------
    // ResultSetControllerListener
    //-------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexChanged(int)
     */
    public void indexChanged(int newIndex)
    {
        //log.debug("---------------------------------------------------------------------");
        //log.debug("Before setDataIntoUI");
        //log.debug("Form     Val: "+(formValidator != null && formValidator.hasChanged()));
        //log.debug("mvParent Val: "+(mvParent != null && mvParent.isTopLevel() && mvParent.hasChanged()));

        if (formValidator != null && formValidator.hasChanged())
        {
            getDataFromUI();
        }
        
        if (list != null)
        {
            if (list.isEmpty())
            {
                return;
            }
            
            Object listDO = list.get(newIndex);
            if (recordSetItemList != null && listDO == null)
            {
                dataObj = getDataObjectViaRecordSet(newIndex);
                list.remove(newIndex);
                list.insertElementAt(dataObj, newIndex);
                
            } else
            {
                dataObj = listDO;    
            }
        }
        
        /////////////////////////////////////////////////////////////////////////////////
        // NOTE: This needs to be here below the setting of the index call because
        // changing the index will set it to false and we need it to be set
        // to true when leaving this method.
        /////////////////////////////////////////////////////////////////////////////////
        isNewlyCreatedDataObj = false; // shouldn't be needed, but just in case
        if (rsController != null)
        {
            rsController.setNewObj(isNewlyCreatedDataObj);
        }
        
        if (formValidator != null)
        {
            formValidator.setNewObj(isNewlyCreatedDataObj);
        }
        
        //log.debug("Before2 setDataIntoUI");
        //log.debug("Form     Val: "+(formValidator != null && formValidator.hasChanged()));
        //log.debug("mvParent Val: "+(mvParent != null && mvParent.isTopLevel() && mvParent.hasChanged()));

        // rods - 07/22/08 - Trying to force a session to be created.
        // so child Sets can get lazy loaded.
        setDataIntoUI(true, true);
        
        //log.debug("After setDataIntoUI");
        //log.debug("Form     Val: "+(formValidator != null && formValidator.hasChanged()));
        //log.debug("mvParent Val: "+(mvParent != null && mvParent.isTopLevel() && mvParent.hasChanged()));


        if (saveControl != null)
        {
            saveControl.setEnabled(false);
            
        } else if (mvParent != null && mvParent.hasChanged())
        {
            Viewable currView = mvParent.getTopLevel().getCurrentView();
            if (currView != null && currView.getSaveComponent() != null)
            {
                currView.getSaveComponent().setEnabled(false); 
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexAboutToChange(int, int)
     */
    public boolean indexAboutToChange(final int oldIndex, final int newIndex)
    {
        return isDataCompleteAndValid(false);
        
        /*if (formValidator != null && formValidator.hasChanged())
        {
            getDataFromUI();
        }
        return true;
        */
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#newRecordAdded()
     */
    public void newRecordAdded()
    {
        if (mvParent.getMultiViewParent() != null)
        {
            formValidator.setHasChanged(true);
            formValidator.validateRoot();
        }
    }


    //-------------------------------------------------
    // PropertyChangeListener
    //-------------------------------------------------

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        if (pce != null && StringUtils.isNotEmpty(pce.getPropertyName()) && pce.getPropertyName().equals("data"))
        {
            setDataIntoUI(false, false);
        }
    }

    //-------------------------------------------------
    // AppPrefsChangeListener
    //-------------------------------------------------

    protected void setColorOnControls(final int colorType, final Color color)
    {
        for (FVOFieldInfo fieldInfo : controlsById.values())
        {
            if (fieldInfo.isOfType(FormCellIFace.CellType.field))
            {
                FormCellFieldIFace cellField = (FormCellFieldIFace)fieldInfo.getFormCell();
                FormCellField.FieldType uiType = cellField.getUiType();
                //log.debug("["+uiType+"]");

                // XXX maybe check check to see if it is a JTextField component instead
                if (uiType == FormCellFieldIFace.FieldType.dsptextfield || uiType == FormCellFieldIFace.FieldType.dsptextarea)
                {
                    Component comp = fieldInfo.getComp();
                    if (colorType == 0)
                    {
                        if (comp instanceof JScrollPane)
                        {
                            ((JScrollPane)comp).getViewport().getView().setBackground(color);
                        } else
                        {
                            fieldInfo.getComp().setBackground(color);
                        }
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.AppPrefsChangeListener#preferenceChange(edu.ku.brc.af.prefs.AppPrefsChangeEvent)
     */
    public void preferenceChange(AppPrefsChangeEvent evt)
    {
        if (evt.getKey().equals("viewfieldcolor"))
        {
            ColorWrapper viewFieldColorLocal = AppPrefsCache.getColorWrapper("ui", "formatting", "viewfieldcolor");
            setColorOnControls(0, viewFieldColorLocal.getColor());
        }
        //log.debug("Pref: ["+evt.getKey()+"]["+pref.get(evt.getKey(), "XXX")+"]");
    }
    
    /**
     * @return  returns hash table mapping the Ids to the Names
     */
    public Hashtable<String, String> getIdToNameHash()
    {
        Hashtable<String, String> hash = new Hashtable<String, String>();
        for (FVOFieldInfo fieldInfo : controlsById.values())
        {
            hash.put(fieldInfo.getId(), fieldInfo.getName());
        }
        return hash;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getFieldIds(java.util.List)
     */
    public void getFieldIds(final List<String> fieldIds)
    {
        getFieldIds(fieldIds, false);
    }
    
    /**
     * @param fieldIds
     * @param doAll
     */
    public void getFieldIds(final List<String> fieldIds, final boolean doAll)
    {
        for (FVOFieldInfo fieldInfo : controlsById.values())
        {
            if (fieldInfo.isOfType(FormCellIFace.CellType.field) || doAll)
            {
                fieldIds.add(fieldInfo.getFormCell().getIdent());
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getFieldNames(java.util.List)
     */
    public void getFieldNames(final List<String> fieldNames)
    {
        ArrayList<FVOFieldInfo> flds = new ArrayList<FVOFieldInfo>();
        
        for (FVOFieldInfo fieldInfo : controlsByName.values())
        {
            if (fieldInfo.isOfType(FormCellIFace.CellType.field))
            {
                flds.add(fieldInfo);
            }
        }
        
        Collections.sort(flds);
        
        for (FVOFieldInfo fieldInfo : flds)
        {
            fieldNames.add(fieldInfo.getName());
        }
    }
    
    //-------------------------------------------------
    // SelectorCellRenderer
    //-------------------------------------------------

    public class SelectorCellRenderer extends DefaultListCellRenderer
    {
        public SelectorCellRenderer() 
        {
            super();
        }

        public Component getListCellRendererComponent(@SuppressWarnings("hiding")JList   list,
                                                      Object  value,   // value to display
                                                      int     index,      // cell index
                                                      boolean iss,    // is the cell selected
                                                      boolean chf)    // the list and the cell have the focus
        {
            AltViewIFace av = (AltViewIFace)value;
            JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, iss, chf);
            label.setText(av.getTitle());
            return label;
        }
    }
    
    //-------------------------------------------------
    // FieldInfo
    //-------------------------------------------------
    public class FVOFieldInfo implements Comparable<FVOFieldInfo>
    {
        protected FormCellIFace formCell;
        protected MultiView     subView;
        protected Component     comp;
        protected JScrollPane   fieldScrollPane;
        protected Integer       insertPos;
        protected UIPluginable  uiPlugin;
        
        // used by CarryForwardSetup
        protected String        label = null; 
        protected DBInfoBase    fieldInfo;
        
        public FVOFieldInfo(FormCellIFace formCell, Component comp, JScrollPane scrollPane, int insertPos)
        {
            this.comp     = comp;
            this.formCell = formCell;
            this.subView  = null;
            this.fieldScrollPane = scrollPane;
            this.insertPos = insertPos;
            this.uiPlugin  = null;
        }

        public FVOFieldInfo(FormCellIFace formCell, MultiView subView, int insertPos)
        {
            this.formCell = formCell;
            this.subView  = subView;
            this.comp     = subView;
            this.insertPos = insertPos;
            this.uiPlugin  = null;
        }
        
        public FVOFieldInfo(FormCellIFace formCell, UIPluginable uiPlugin, int insertPos)
        {
            this.formCell  = formCell;
            this.subView   = null;
            this.comp      = uiPlugin.getUIComponent();
            this.insertPos = insertPos;
            this.uiPlugin  = uiPlugin;
        }
        
        public boolean isOfType(final FormCell.CellType type)
        {
            return formCell.getType() == type;
        }

        public String getName()
        {
            return formCell.getName();
        }

        public String getId()
        {
            return formCell.getIdent();
        }

        public Component getComp()
        {
            return comp;
        }
        public FormCellIFace getFormCell()
        {
            return formCell;
        }

        public MultiView getSubView()
        {
            return subView;
        }

        public int getInsertPos()
        {
            return insertPos;
        }

        /**
         * @return the uiPlugin
         */
        public UIPluginable getUiPlugin()
        {
            return uiPlugin;
        }

        public void setEnabled(boolean enabled)
        {
            //log.debug(formCell.getName()+"  "+(scrollPane != null ? "has Pane" : "no pane"));
            comp.setEnabled(enabled);
            if (fieldScrollPane != null)
            {
                fieldScrollPane.setEnabled(enabled);
            }
        }
        
        /**
         * Tells it to clean up
         */
        public void shutdown()
        {
            if (comp instanceof UIValidatable)
            {
                ((UIValidatable)comp).cleanUp();
                
            } else if (comp instanceof UIPluginable)
            {
                ((UIPluginable)comp).shutdown();
            }
            formCell   = null;
            subView    = null;
            comp       = null;
            fieldScrollPane = null;
            label      = null;
            fieldInfo  = null;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(FVOFieldInfo o)
        {
            return insertPos.compareTo(o.insertPos);
        }

        public String getLabel()
        {
            return label;
        }

        public void setLabel(String label)
        {
            this.label = label;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return label;
        }

        /**
         * @return the fieldInfo
         */
        public DBInfoBase getFieldInfo()
        {
            return fieldInfo;
        }

        /**
         * @param fieldInfo the fieldInfo to set
         */
        public void setFieldInfo(DBInfoBase fieldInfo)
        {
            this.fieldInfo = fieldInfo;
        }
    }

    /**
     * @return the useDebugForm
     */
    public static boolean isUseDebugForm()
    {
        return useDebugForm;
    }

    /**
     * @param useDebugForm the useDebugForm to set
     */
    public static void setUseDebugForm(boolean useDebugForm)
    {
        FormViewObj.useDebugForm = useDebugForm;
    }

}
