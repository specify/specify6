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
package edu.ku.brc.af.ui.forms;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableChildIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellSubViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewLoader;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;

/**
 * A MulitView is a "view" that contains multiple Viewable object that can display the current data object in any of the given views.
 * Typically three views are registered: Form, Table, and Field <BR>
 * <BR>
 * Upon creation the agruement "createWithMode" tells the creation mechanism whether to look for and obey the "View" vs "Edit" modeness.
 * Meaning that if we have a view with subview and they (or some of them) have both a n Edit View and a non-Edit View,
 * all the subview will be created as either view or edit form according to the parent's mode.
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class MultiView extends JPanel
{
    // These are the configuration Options for a View
    public static final int NO_OPTIONS             =   0; // Indicates there are no options
    public static final int RESULTSET_CONTROLLER   =    1; // Add the ResultSet Controller with First,Previous,Next, Last
    public static final int VIEW_SWITCHER          =    2; // Add a View Switch in the bottom right
    public static final int IS_NEW_OBJECT          =    4; // Indicates the form will contain a brand new data object
    public static final int HIDE_SAVE_BTN          =    8; // Hide the Save Button
    public static final int IS_EDITTING            =   16; // Whether the MultiView is in Edit mode.
    public static final int IS_SINGLE_OBJ          =   32; // Whether the data being passed into the MultiView is a Collection of Object or a single Object
    public static final int NO_SCROLLBARS          =   64; // Whether the form should be scrollable
    public static final int ADD_SEARCH_BTN         =  128; // Whether a special search btn should be added
    public static final int DONT_ADD_ALL_ALTVIEWS  =  256; // Whether it is OK to add all the AltView to the Switcher
    public static final int USE_ONLY_CREATION_MODE =  512; // Create only the AltViews that have the same creation mode
    public static final int DONT_USE_EMBEDDED_SEP  = 1024; // use the internal embedded separator
    public static final int NO_MORE_BTN_FOR_SEP    = 2048; // doesn't include the separator's 'more' btn

    // Statics
    private static final Logger log = Logger.getLogger(MultiView.class);

    protected MultiView                    mvParent             = null;
    protected String                       cellName             = null;
    protected ViewIFace                    view;
    protected Hashtable<String, Viewable>  viewMapByName        = new Hashtable<String, Viewable>();
    protected Object                       data                 = null;
    protected Object                       parentDataObj        = null;
    protected JPanel                       cardPanel;
    protected CardLayout                   cardLayout           = new CardLayout();
    protected Viewable                     currentViewable      = null;
    protected FormValidator                currentValidator     = null;
    protected Class<?>                     classToCreate        = null;
    protected FormCellIFace                cell                 = null;
    
    protected PermissionSettings           permissions;
    
    protected boolean                      editable             = false;
    protected AltViewIFace.CreationMode    createWithMode       = AltViewIFace.CreationMode.NONE;
    protected boolean                      ignoreDataChanges    = false;

    protected int                          createOptions        = 0;

    protected List<MultiView>              kids                 = new ArrayList<MultiView>();

    protected List<ViewBasedDisplayIFace>  displayFrames        = null;
    
    protected boolean                      isSelectorForm;
    protected String                       selectorValue        = null;
    protected boolean                      doingSelector        = false; // This keeps us from recursing into the selector forever
    
    protected Vector<Object>               deletedItems         = null;
    protected Vector<Object>               toBeSavedItems       = null;
    protected HashSet<FormDataObjIFace>    busRulesItems        = null;
    
    protected CollapsableSeparator         separator            = null;
    
    /**
     * Constructor - Note that createWithMode can be null and is passed in from parent ALWAYS.
     * So forms that may not have multiple views or do not wish to have Edit/View can pass in null. (See Class description)
     * @param mvParent parent of this MultiView the root MultiView is null
     * @param view the view to create for
     * @param createWithMode how the form should be created (None, Edit or View mode)
     * @param options the options needed for creating the form
     */
    public MultiView(final MultiView mvParent,
                     final String    cellName,
                     final ViewIFace view,
                     final AltViewIFace.CreationMode createWithMode,
                     final int       options)
    {
        this(mvParent, cellName, view, createWithMode, null, options, null, null);
    }
    
    /**
     * Constructor - Note that createWithMode can be null and is passed in from parent ALWAYS.
     * So forms that may not have multiple views or do not wish to have Edit/View can pass in null. (See Class description)
     * @param mvParent parent of this MultiView the root MultiView is null
     * @param view the view to create for
     * @param createWithMode how the form should be created (Noe, Edit or View mode)
     * @param options the options needed for creating the form
     * @param bgColor background color
     */
    public MultiView(final MultiView mvParent,
                     final String    cellName,
                     final ViewIFace view,
                     final AltViewIFace.CreationMode createWithMode,
                     final int       options,
                     final Color     bgColor)
    {
        this(mvParent, cellName, view, createWithMode, null, options, bgColor, null);
    }
    
    /**
     * Constructor - Note that createWithMode can be null and is passed in from parent ALWAYS.
     * So forms that may not have multiple views or do not wish to have Edit/View can pass in null. (See Class description)
     * @param mvParent parent of this MultiView the root MultiView is null
     * @param view the view to create for
     * @param createWithMode how the form should be created (None, Edit or View mode)
     * @param defaultAltViewType suggestion as to whether to use a form or a grid
     * @param options the options needed for creating the form
     */
    public MultiView(final MultiView mvParent,
                     final String    cellName,
                     final ViewIFace view,
                     final AltViewIFace.CreationMode createWithMode,
                     final String    defaultAltViewType,
                     final int       options)
    {
        this(mvParent, cellName, view, createWithMode, defaultAltViewType, options, null, null);
    }
    
    /**
     * Constructor - Note that createWithMode can be null and is passed in from parent ALWAYS.
     * So forms that may not have multiple views or do not wish to have Edit/View can pass in null. (See Class description)
     * @param mvParent parent of this MultiView the root MultiView is null
     * @param view the view to create for
     * @param createWithMode how the form should be created (None, Edit or View mode)
     * @param defaultAltViewType suggestion as to whether to use a form or a grid
     * @param options the options needed for creating the form
     * @param bgColor background color
     * @param cell  the definition of the MultiView when it is a subview
     */
    public MultiView(final MultiView     mvParent,
                     final String        cellName,
                     final ViewIFace     view,
                     final AltViewIFace.CreationMode createWithMode,
                     final String        defaultAltViewType,
                     final int           options,
                     final Color         bgColor,
                     final FormCellIFace cell)
    {
        this.mvParent       = mvParent;
        this.cellName       = cellName;
        this.view           = view;
        this.createWithMode = createWithMode;
        this.createOptions  = options | (createWithMode == AltViewIFace.CreationMode.EDIT ? IS_EDITTING : 0);
        this.cell           = cell;
        
        initializeCardPanel(options);
        
        this.isSelectorForm = StringUtils.isNotEmpty(view.getSelectorName());
        
        if (bgColor != null)
        {
            setBackground(bgColor);
        }
        
        boolean isUsingSwitcher         = MultiView.isOptionOn(createOptions, MultiView.VIEW_SWITCHER);
        boolean isUsingOnlyCreationMode = MultiView.isOptionOn(createOptions, MultiView.USE_ONLY_CREATION_MODE);
        boolean isUsingSelector         = StringUtils.isNotEmpty(view.getSelectorName());
        
        //log.debug("isUsingOnlyCreationMode "+isUsingOnlyCreationMode + " " + createWithMode + "  defaultAltViewType: "+defaultAltViewType);
        
        
        if (mvParent == null && ViewLoader.isDoFieldVerification())
        {
            ViewLoader.clearFieldVerInfo();
        }
        
        if (AppContextMgr.isSecurityOn())
        {
            this.permissions = getPremissionFromView(view, getClassNameFromParentMV(null, mvParent, cellName));
            //PermissionSettings.dumpPermissions("DO."+view.getName(), permissions.getOptions());
        } else
        {
            this.permissions = new PermissionSettings(PermissionSettings.ALL_PERM);
        }

        AltViewIFace defaultAltView = createDefaultViewable(defaultAltViewType);
        
        if (isUsingSwitcher)
        {
            for (AltViewIFace av : view.getAltViews())
            {
                if ((isUsingOnlyCreationMode  && av.getMode() == createWithMode) || 
                    (!isUsingOnlyCreationMode && (av.getMode() == createWithMode || mvParent == null)))
                {
                    // temporarily set this for creation, because it gets asked via getMode()
                    this.createWithMode = av.getMode();
                    
                    //log.debug("CREATING: createWithMode "+createWithMode+"  "+av.getName()+" "+av.getMode());
                    createViewable(av.getName(), cellName);
                    
                } else
                {
                    //log.debug("SKIPPED:  createWithMode "+createWithMode+"  "+av.getName()+" "+av.getMode());
                }
            }
        } else if (isUsingSelector && defaultAltView.getMode() == AltViewIFace.CreationMode.EDIT) // Special situation for selectors in edit mode
        {
            for (AltViewIFace av : view.getAltViews())
            {
                if (av.getMode() == createWithMode && av.getMode() == AltViewIFace.CreationMode.EDIT)
                {
                    // temporarily set this for creation, because it gets asked via getMode()
                    this.createWithMode = av.getMode();
                    
                    //log.debug("CREATING: createWithMode "+createWithMode+"  "+av.getName()+" "+av.getMode());
                    createViewable(av.getName(), cellName);
                }
            }
        } else
        {
            createViewable(defaultAltView.getName(), cellName);
        }
        
        this.createWithMode = createWithMode;
        
        editable = defaultAltView.getMode() == AltViewIFace.CreationMode.EDIT;
        
        showView(defaultAltView.getName());
        
        //log.debug( "*** Parent " + (mvParent != null ? "NOT NULL" : "NULL"));
        if (currentViewable != null)
        {
            currentValidator = currentViewable.getValidator();
        }
        
        if (mvParent == null && ViewLoader.isDoFieldVerification())
        {
            ViewLoader.displayFieldVerInfo();
        }
    }

    /**
     * Constructor - Note that createWithMode can be null and is passed in from parent ALWAYS.
     * So forms that may not have multiple views or do not wish to have Edit/View can pass in null. (See Class description)
     * @param mvParent parent of this MultiView the root MultiView is null
     * @param view the view to create for
     * @param createWithMode how the form should be created (Noe, Edit or View mode)
     * @param options the options needed for creating the form
     */
    public MultiView(final MultiView      mvParent,
                     final String         cellName,
                     final ViewIFace      view,
                     final AltViewIFace   altView,
                     final AltViewIFace.CreationMode createWithMode,
                     final int            options)
    {
        this.mvParent       = mvParent;
        this.cellName       = cellName;
        this.view           = view;
        this.createWithMode = createWithMode;
        this.createOptions  = options | (createWithMode == AltViewIFace.CreationMode.EDIT ? IS_EDITTING : NO_OPTIONS);
        
        initializeCardPanel(options);
        
        if (AppContextMgr.isSecurityOn())
        {
            this.permissions = getPremissionFromView(view, getClassNameFromParentMV(null, mvParent, cellName));
        } else
        {
            this.permissions = new PermissionSettings(PermissionSettings.ALL_PERM);
        }
        
        createViewable(altView != null ? altView : createDefaultViewable(null), cellName);
        showView(altView.getName());
    }
    
    /**
     * @param options
     */
    private void initializeCardPanel(final int options)
    {
        if (!isOptionOn(options, DONT_USE_EMBEDDED_SEP))
        {
            setLayout(new BorderLayout());
            
            String title = view.getTitle();
            if (mvParent != null && StringUtils.isNotEmpty(cellName))
            {
                DBTableInfo parentTblInfo = DBTableIdMgr.getInstance().getByClassName(mvParent.getView().getClassName());
                if (parentTblInfo != null)
                {
                    DBTableChildIFace childInfo = parentTblInfo.getItemByName(cellName);
                    if (childInfo != null)
                    {
                        title = childInfo.getTitle();
                    }
                }
            }
            this.cardPanel = new JPanel(cardLayout);
            this.separator = new CollapsableSeparator(title, !isOptionOn(options, NO_MORE_BTN_FOR_SEP));
            this.separator.setInnerComp(this.cardPanel);
            this.cardPanel.setOpaque(false);
            
            add(this.separator, BorderLayout.NORTH);
            add(this.cardPanel, BorderLayout.CENTER);
        } else
        {
            setLayout(cardLayout);
        } 
    }
    
    /**
     * Get the short class name for the cell's data type.
     * @param dataClass the class of the data that is put into the form
     * @param parent the MultiView parent
     * @param cellName the cell name of the child
     * @return the short class name for the class type of the data for the cell
     */
    public static String getClassNameFromParentMV(final Class<?> dataClass, final MultiView parent, final String cellName)
    {
        String dataClassName = dataClass != null ? dataClass.getSimpleName() : null;
        if (dataClassName == null && parent != null && cellName != null)
        {
            String className = parent.getView().getClassName();
            if (className != null)
            {
                DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(className);
                if (tblInfo != null)
                {
                    DBTableChildIFace childInfo = tblInfo.getItemByName(cellName);
                    if (childInfo != null)
                    {
                        dataClassName = childInfo.getDataClass().getSimpleName();
                    } else
                    {
                        log.debug("Couldn't find cellName["+cellName+"] for table["+tblInfo.getName()+"]");
                    }
                }
            }
        }
        return dataClassName;
    }
    
    /**
     * Get the Permissions for the data type from the views.
     * @param view the view
     * @param shortClassName the short class name if it is already know (null can be passed in)
     * @return the permissions
     */
    public static PermissionSettings getPremissionFromView(final ViewIFace view, final String shortClassName)
    {
        String shortClass = StringUtils.isNotEmpty(shortClassName) ? shortClassName : StringUtils.substringAfterLast(view.getClassName(), ".");
        if (shortClass == null)
        {
            shortClass = view.getClassName();
        }
        return SecurityMgr.getInstance().getPermission("DO."+shortClass.toLowerCase());
    }
    
    /**
     * @return the permissions
     */
    public PermissionSettings getPermissions()
    {
        return permissions;
    }

    /**
     * @return the separator
     */
    public CollapsableSeparator getSeparator()
    {
        return separator;
    }

    /**
     * Sometimes the caller may wish to have the form validated after being initialized. 
     */
    public void preValidate()
    {
        FormViewObj formViewObj = getCurrentViewAsFormViewObj();
        if (formViewObj != null)
        {
            formViewObj.getValidator().setHasChanged(true);
            formViewObj.getValidator().wasValidated(null);
        }
    }
    
    /**
     * @return
     */
    public FormValidator getCurrentValidator()
    {
        if (currentViewable != null)
        {
            return currentViewable.getValidator();
        }
        return null;
    }
    
    /**
     * @return the form's first focusable object.
     */
    public Component getFirstFocusable()
    {
        FormViewObj formView = getCurrentViewAsFormViewObj();
        if (formView != null)
        {
            return formView.getFirstFocusable();
        }
        return null;
    }
    
    /**
     * Returns a Collection of the Viewables
     * @return  a Collection of the Viewables
     */
    public Collection<Viewable> getViewables()
    {
        return viewMapByName.values();
    }

    /**
     * Asks the Viewable to get the data from the UI and transfer the changes (really all the fields) to
     * the DB object.
     */
    public void getDataFromUI()
    {
        for (Enumeration<Viewable> e=viewMapByName.elements();e.hasMoreElements();)
        {
            Viewable viewable = e.nextElement();
            if (viewable.getValidator() != null && viewable.getValidator().hasChanged()) // XXX Not sure why it must have a validator ???
            {
                viewable.getDataFromUI();
                //if (viewable.getValidator() != null && viewable.getValidator().hasChanged())
                //{
                    //if (FormHelper.updateLastEdittedInfo(viewable.getDataObj()))
                    //{
                    //    viewable.setDataIntoUI();
                    //}
                //}
            }
            // XXX For the Demo I can't figure out why a Different session is being checked when I try
            // to save the session in the form.
            if (viewable instanceof RecordSetTableViewObj)
            {
                viewable.getDataFromUI();
            }
        }
    }
    
    /**
     * Asks the first component to be focused. 
     */
    public void focus()
    {
        if (currentViewable != null)
        {
            currentViewable.focus();
        }
    }
    
    /**
     * Asks the immediate SubViews to create data (via the Business Rules).
     */
    public void initSubViews()
    {
        FormViewObj fvo = getCurrentViewAsFormViewObj();
        if (fvo != null)
        {
            fvo.initSubViews();
        }
    }

    /**
     * Returns the View (the definition).
     * @return the View (the definition)
     */
    public ViewIFace getView()
    {
        return view;
    }

    /**
     * Returns all the Multiview children.
     * @return all the Multiview children
     */
    public List<MultiView> getKids()
    {
        return kids;
    }
    
    /**
     * The flags used when the MultiView was created.
     * @return the int bit mask of options
     */
    public int getOptions()
    {
        return createOptions;
    }

    /**
     * Called to tell current view it is about to be shown or hidden.
     * @param show true - shown, false - hidden
     */
    public void aboutToShow(final boolean show)
    {
        if (currentViewable != null)
        {
            currentViewable.aboutToShow(show);
        }
        showDisplayFrames(show);
    }

    /**
     * Tells all the Viewables that have validators that the form is new or old.
     * NOTE: New Forms means that it is an empty form and that the controls should
     * not show validation errors until they have had focus if they are a validator that changes on input
     * and not by the OK button or by focus.
     * @param isNewForm whether the form is handling a new obj
     * @param traverseKids whether the MultiView should traverse into the children MultiViews
     */
    public void setIsNewForm(final boolean isNewForm, final boolean traverseKids)
    {
        for (Enumeration<Viewable> e=viewMapByName.elements();e.hasMoreElements();)
        {
            Viewable viewable = e.nextElement();
            if (viewable.getValidator() != null)
            {
                FormValidator fv = viewable.getValidator();
                fv.setUIValidatorsToNew(isNewForm);
                // rods 09/24/08 added the if statement because all the validators 
                // in the immediate subforms were being set to not be changed
                if (!isNewForm)
                {
                    fv.setUIValidatorsToNotChanged();
                }
                viewable.setHasNewData(isNewForm);
            }
        }
        
        if (traverseKids)
        {
            for (MultiView mv : kids)
            {
                mv.setIsNewForm(isNewForm, traverseKids);
            }
        }
    }
    
    /**
     * Sets all validators to unchanged state.
     */
    public void clearValidators()
    {
        if (currentValidator != null)
        {
            currentValidator.setUIValidatorsToNotChanged();
        }
    }
    
    /**
     * Returns true if any of the validators have changed, false if it has no validators or they haven't changed.
     * @return true if any of the validators have changed, false if it has no validators or they haven't changed.
     */
    public boolean hasChanged()
    {
        if (!ignoreDataChanges)
        {
            if (currentValidator != null)
            {
                //currentValidator.setFirstTime(false);
    
                if (currentValidator.hasChanged())
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns whether this is the top level MultiView, which usually means this is the most outer form.
     * @return whether this is the top level MultiView, which usually means this is the most outer form.
     */
    public boolean isTopLevel()
    {
        return mvParent == null;
    }
    
    /**
     * Returns the Top MultiView.
     * @return the Top MultiView.
     */
    public MultiView getTopLevel()
    {
        if (mvParent == null)
        {
            return this;
        }
        MultiView top = mvParent;
        while (top != null && top.getMultiViewParent() != null)
        {
            top = top.mvParent;
        }
        return top;
    }

    /**
     * Creates the Default Viewable for this view (it chooses the "default" ViewDef.
     * @param defAltViewType should contain values: null, grid, form
     * @return return the default Viewable (ViewDef)
     */
    protected AltViewIFace createDefaultViewable(final String defAltViewType)
    {
        AltViewIFace  altView;
        if (createWithMode != null)
        {
            altView = view.getDefaultAltViewWithMode(createWithMode, defAltViewType);

        } else
        {
            altView = view.getDefaultAltView(createWithMode, defAltViewType);
        }
        return altView;
    }
    
    /**
     * Returns the name of the view for the MultiView.
     * @return the name of the view for the MultiView
     */
    public String getViewName()
    {
        return view.getName();
    }

    /**
     * Adds a viewable to the MultiView.
     * @param viewable the Viewable to be added
     * @param name the name of the view to be added
     * @return true if it was added, false if name conflicts
     */
    protected boolean add(final Viewable viewable, final String name)
    {
        if (viewMapByName.get(name) != null)
        {
            log.error("Adding a Viewable by a name that is already used["+name+"]");
            return false;

        }
        // else
        viewable.setCellName(cellName);
        viewMapByName.put(name, viewable);
        
        // CallapsablePanel
        if (separator != null)
        {
            cardPanel.add(viewable.getUIComponent(), name);
            
            JComponent controllerPanel = viewable.getControllerPanel();
            if (controllerPanel == null)
            {
                controllerPanel = new JPanel(); 
                controllerPanel.setPreferredSize(new Dimension(0,0));
            }
            separator.addToSubPanel(controllerPanel, name);
            
        } else
        {
            add(viewable.getUIComponent(), name);
        }
        return true;
    }
    
    /**
     * @param fvo
     * @param tableInfo
     * @param recordSetItemList
     */
    public void setRecordSetItemList(final FormViewObj              fvo,
                                     final DBTableInfo              tableInfo, 
                                     final List<RecordSetItemIFace> recordSetItemList)
    {
        for (Viewable viewable : viewMapByName.values())
        {
            if (viewable != fvo && viewable instanceof FormViewObj)
            {
                ((FormViewObj)viewable).setRecordSetItemList(tableInfo, recordSetItemList);
            }
        }
    }

    /**
     * Show the AltViewIFace.
     * @param altView show the AltViewIFace
     */
    public void showView(final AltViewIFace altView)
    {
        if (hasChanged())
        {
            if (currentViewable != null && currentViewable.isDataCompleteAndValid(false))
            {
                showView(altView.getName());         
            }
        } else
        {
            showView(altView.getName());
        }
    }

    /**
     * Show a Viewable by name.
     * @param name the registered name of the component (In this case it is the name of the Viewable)
     */
    public void showView(final String name)
    {
        Viewable viewable = viewMapByName.get(name);
        if (viewable == null)
        {
            viewable = createViewable(name, null);
        }
        
        Vector<ViewState> viewStateList = null;
        
        if (viewable != null)
        {
            if (currentViewable != null)
            {
                FormViewObj fvo = getCurrentViewAsFormViewObj();
                if (fvo != null)
                {
                    viewStateList = fvo.collectionViewState();
                }
                
                currentViewable.aboutToShow(false);
                if (currentViewable.getAltView().getMode() == AltViewIFace.CreationMode.EDIT)
                {
                    currentViewable.getDataFromUI();
                }
                
                if (currentValidator != null)
                {
                    adjustValidationTree(this, currentValidator, false);
                }
            }

            currentViewable = viewable;
            currentViewable.aboutToShow(true);
            
            currentViewable.setParentDataObj(parentDataObj);
            currentValidator = currentViewable.getValidator();
            
            createWithMode = currentViewable.getAltView().getMode();
            editable       = createWithMode == AltViewIFace.CreationMode.EDIT;
            
            setData(data, false);
            
            setFormForSelector();
            
            if (viewStateList != null)
            {
                FormViewObj fvo = getCurrentViewAsFormViewObj();
                if (fvo != null)
                {
                    setViewState(viewStateList, createWithMode, 0);
                } 
            }
            
            if (separator != null)
            {
                cardLayout.show(cardPanel, name);
                separator.showSubPanel(name);
                
            } else
            {
                cardLayout.show(this, name);
            }
            
            if (currentValidator != null)
            {
                if (mvParent != null && mvParent.getCurrentValidator() != null)
                {
                    adjustValidationTree(mvParent, mvParent.getCurrentValidator(), true);
                    //mvParent.getCurrentValidator().dumpState(true, 0);
                    
                } else
                {
                    adjustValidationTree(this, currentValidator, true);
                    //currentValidator.dumpState(true, 0);
                }
            }
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    CommandDispatcher.dispatch(new CommandAction("Data_Entry", "ViewWasShown", currentViewable));
                }
            });
            
        } else
        {
            log.error("Viewable was null and shouldn't have been!");
        }
    }

    /**
     *  Create the Viewable from an AltView Name.
     * @param altViewName the name of the AltView
     * @param cellNameArg
     * @return
     */
    public Viewable createViewable(final String altViewName, final String cellNameArg)
    {
        // Find the AltView to create
        List<AltViewIFace> list = view.getAltViews();
        int inx = 0;
        for (AltViewIFace altView : list)
        {
            if (altViewName.equals(altView.getName()))
            {
                return createViewable(altView, cellNameArg);
            }
            inx++;
        }
        
        log.error("Couldn't find Alt View ["+altViewName+"]in AltViewIFace List");
        
        return null;
    }

    /**
     * Create the Viewable from an AltView.
     * @param altView the actual AltView object
     * @param cellNameArg
     * @return the viewable for the altview
     */
    protected Viewable createViewable(final AltViewIFace altView, final String cellNameArg)
    {
        ViewIFace newView = AppContextMgr.getInstance().getView(view.getViewSetName(), altView.getView().getName());
        // 12/14/07 - rods
        // Commented out the line above, I can't figure out why we need to go get a new copy of the View
        // each time this is called
        //ViewIFace newView = view;
        if (newView != null)
        {
            if (false)
            {
                log.debug("--------------------------");
                for (int i=0;i<getComponentCount();i++)
                {
                    Component comp = getComponent(i);
                    if (comp instanceof Viewable)
                    {
                        log.debug(((Viewable)comp).getName());
                    } else
                    {
                        log.debug(comp);
                    }
                }
                log.debug("--------------------------");
            }

            int tmpCreateOptions = createOptions | (createWithMode == AltViewIFace.CreationMode.EDIT ? (IS_EDITTING) : 0);
            //MultiView.printCreateOptions("createViewable", createOptions);
            Viewable viewable = ViewFactory.createFormView(this, newView, altView.getName(), data, tmpCreateOptions, cellNameArg, getBackground());
            if (viewable != null)
            {
                if (add(viewable, altView.getName()))
                {
                    if (viewable instanceof TableViewObj && cell instanceof FormCellSubViewIFace)
                    {
                        ((TableViewObj)viewable).setVisibleRowCount(((FormCellSubViewIFace)cell).getTableRows());
                    }
                    
                    if (mvParent != null)
                    {
                        viewable.getUIComponent().validate();
                        viewable.getUIComponent().doLayout();
                    }
                    
                    //log.debug("Added Viewable["+altView.getName()+"]");
                    
                    validate();
                    invalidate();
                    doLayout();
                    
                    return viewable;
                }
            
            } else
            {
                log.error("The Viewable could not be created for some reason View["+newView+"] AltViewIFace["+altView.getName()+"] Options["+createOptions+"]");
            }

        } else
        {
            log.error("Unable to load form ViewSetName ["+currentViewable.getView().getViewSetName()+"] Name["+altView.getName()+"]");
        }
        
        return null;
    }
    
    /**
     * @param parent
     * @param parentVal
     * @param doAdd
     */
    protected void adjustValidationTree(final MultiView     parent, 
                                        final FormValidator parentVal,
                                        final boolean       doAdd)
    {
        parentVal.clearKids();
        
        if (parent != null)
        {
            for (MultiView kid : parent.kids)
            {
                //log.debug(kid.getView().getName()+" "+ kid.getCurrentView().getAltView().getName());
                
                FormValidator kidVal = kid.getCurrentValidator();
                if (kidVal != null)
                {
                    if (doAdd)
                    {
                        parentVal.add(kid.getCurrentValidator());
                        kidVal.setParent(parentVal);
                    }/* else
                    {
                        parentVal.remove(kid.getCurrentValidator());
                        kidVal.setParent(null);
    //                }*/
                    adjustValidationTree(kid, kidVal, doAdd);
                }
            }
        }
    }
    
    /**
     * 
     */
    protected void addCurrentValidator()
    {
        if (currentViewable != null)
        {
            currentValidator = currentViewable.getValidator();
            if (currentValidator != null)
            {
                if (mvParent != null)
                {
                    FormValidator parentVal = mvParent.getCurrentValidator();
                    if (parentVal != null)
                    {
                        parentVal.add(currentValidator);
                        currentValidator.setParent(parentVal);
                        //parentVal.dumpState(true, 0);
                    }
                    adjustValidationTree(this, currentValidator, true);
                    
                }
                //currentValidator.dumpState(true, 100);
            }
        } else
        {
            log.error("CurrentViewable can't be null");
        }
    }
    
    /**
     * 
     */
    protected void removeCurrentValidator()
    {
        if (currentValidator != null)
        {
            adjustValidationTree(mvParent, currentValidator, false);
            
            FormValidator parentVal = currentValidator.getParent();
            if (parentVal != null)
            {
                parentVal.remove(currentValidator);
            }
            currentValidator.setParent(null);
            // rods - 7/3/08 - Setting this to null is very bad
            // it clears an existing validator when called from ViewBtn
            //currentValidator = null;
        }
    }

    /**
     * Returns the MultiView's mvParent.
     * @return the MultiView's mvParent
     */
    public MultiView getMultiViewParent()
    {
        return mvParent;
    }

    /**
     * Sets the parent MV.
     * @param parent the parent MV
     */
    public void setMultiViewParent(final MultiView parent)
    {
        mvParent = parent;
    }
    
    /**
     * Adds a MultiView as a child of another MultiView
     * @param kidMV the child to be added
     */
    public void addChildMV(final MultiView kidMV)
    {
        kids.add(kidMV);
        kidMV.setMultiViewParent(this);
    }
    
    /**
     * Removes a MultiView as a child of another MultiView
     * @param kidMV the child to be removed
     */
    public void removeChildMV(final MultiView kidMV)
    {
        kids.remove(kidMV);
        kidMV.setMultiViewParent(null);
    }
    
    /**
     * Return whether the MultiView is in Edit Mode.
     * @return whether the MultiView is in Edit Mode
     */
    public boolean isEditable()
    {
        return editable;
    }

    /**
     * Return whether the MultiView's CreateMode (may be null, true or false) meaning don't assume it will always be non-null.
     * @return whether the MultiView's CreateMode (may be null, true or false)
     */
    public AltViewIFace.CreationMode getCreateWithMode()
    {
        return createWithMode;
    }

    /**
     * @param dObj
     */
    public void setDataIntoParent(final Object dObj)
    {
        this.data = dObj;
    }
    
    /**
     * Sets the Data Object into the View.
     * @param data the data object
     */
    public void setData(final Object data)
    {
        setData(data, true);
    }
    
    /**
     * Called only by FormViewObj after a save.
     * @param dObj the new data object when it isn't a collection.
     */
    protected void setJustDataObj(final Object dObj)
    {
        this.data = dObj;
    }

    /**
     * Sets the Data Object into the View.
     * @param data the data object
     */
    protected void setData(final Object data, final boolean doSelector)
    {
        // If this happens it is because there was a major error elsewhere.
        if (currentViewable == null)
        {
            return;
        }
        
        this.data = data;
        
        ignoreDataChanges = true;

        boolean doSetData = true;
        if (doSelector)
        {
            doSetData = setFormForSelector();
        }
        
        if (doSetData)
        {
            currentViewable.setDataObj(data);
        }
        
        if (isTopLevel() && currentViewable != null)
        {
            currentViewable.updateSaveBtn();
        }
        
        if (isTopLevel())
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    if (currentViewable != null)
                    {
                        currentViewable.focus();
                    }
                }
            });
        }
        
        ignoreDataChanges = false;

    }
    
    /**
     * @return
     */
    protected boolean setFormForSelector()
    {
        if (doingSelector)
        {
            return true;
        }
        
        doingSelector = true;
        
        boolean doSetData = true;
        
        boolean cacheIgnoreDataChanges = ignoreDataChanges;
        ignoreDataChanges = true;
        
        selectorValue = null;
        if (isSelectorForm)
        {
            // We the data gets set into the MultiView we need to display the correct
            // AlView with the matching selector value.
            AltViewIFace altView = currentViewable.getAltView();

            // Set the data into the Current view even though this may not be the correct AltViewIFace
            currentViewable.setDataObj(data);
            
            String             selectorName    = altView.getSelectorName();
            DataObjectGettable getter          = currentViewable.getViewDef().getDataGettable();
            Object             selectorDataVal = getter.getFieldValue(currentViewable.getDataObj(), selectorName);
            
            if (selectorDataVal != null)
            {
                String newSelectorValue = selectorDataVal.toString(); // all selector value must be "directly" convertable to string
                
                // Make the Selector value has changed.
                if (selectorValue == null || !selectorValue.equals(newSelectorValue))
                {
                    selectorValue = newSelectorValue;
                    
                    // Find the matching Viewable with the same selectorValue
                    for (AltViewIFace av : view.getAltViews())
                    {
                        //log.debug("["+av.getSelectorName()+"]["+av.getSelectorValue()+"]["+selectorValue+"]");
                        
                        if (StringUtils.isNotEmpty(av.getSelectorName()) && 
                            av.getSelectorValue().equals(selectorValue) &&
                            altView.getMode() == av.getMode())
                        {
                            // Save off the current to see if it changes
                            Viewable oldViewable = currentViewable;
                            
                            showView(av.getName());
                            
                            // Now set the data into the "new" viewable if it changed Viewables
                            if (oldViewable != currentViewable)
                            {
                                currentViewable.setDataObj(data);
                                doSetData = false;
                            }
                            break;
                        }
                    }
                }
            } else
            {
                //AltViewIFace defAltView = view.getDefaultAltViewWithMode(createWithMode, "form");
                //log.debug(defAltView.getName());
                //showView(defAltView.getName());
            }
        }
        
        ignoreDataChanges = cacheIgnoreDataChanges;
        doingSelector     = false;
        
        return doSetData;

    }

    /**
     * Returns  the data object for this form.
     * @return the data object for this form
     */
    public Object getData()
    {
        if (data instanceof Collection<?>)
        {
            return currentViewable.getDataObj();
        }
        return data;
    }

    /**
     * Returns whether all the validation of this form and child forms is OK.
     * @return whether all the validation of this form and child forms is OK
     */
    public boolean isAllValidationOK()
    {
        if (currentValidator != null)
        {
            currentValidator.isFormValid();
        }
        return true;
    }
    
    /**
     * Validates all the forms and subforms.
     */
    public void validateAll()
    {
        if (currentValidator != null)
        {
            currentValidator.validateForm();
        }
    }


    /**
     * Sets the dataObj of the parent, this is need to add new child node from subforms.
     * @param parentDataObj the dataObj of the parent
     */
    public void setParentDataObj(Object parentDataObj)
    {
        this.parentDataObj = parentDataObj;
        
        //if (currentViewable != null)
        //{
            //currentViewable.setParentDataObj(parentDataObj);
        //}
        
        for (Viewable v : viewMapByName.values())
        {
            v.setParentDataObj(parentDataObj);
        }
        
        for (MultiView kidMV : kids)
        {
            kidMV.setParentDataObj(parentDataObj);
        }
    }

    /**
     * Sets into the multiview and viewables the class name that should be used
     * o create new data objects.
     * @param classToCreateArg the class to be created
     */
    public void setClassToCreate(final Class<?> classToCreateArg)
    {
        this.classToCreate = classToCreateArg;
        for (Viewable v : viewMapByName.values())
        {
            v.setClassToCreate(classToCreate);
        }
    }

    /**
     * Returns the dataObj of of the parent.
     * @return the dataObj of of the parent
     */
    public Object getParentDataObj()
    {
        return parentDataObj;
    }

    /**
     * Registers "display" window for display "sub object" information.
     * @param frame the frame to be added
     */
    public void registerDisplayFrame(final ViewBasedDisplayIFace frame)
    {
        if (frame != null)
        {
            if (displayFrames == null)
            {
                displayFrames  = new ArrayList<ViewBasedDisplayIFace>();
            }
            displayFrames.add(frame);
        } else
        {
            log.error("Registering a null frame!");
        }
    }

    /**
     * Unregsters a frame from the MultiView list of sub-frames.
     * @param frame the frame to be unregistered (removed)
     */
    public void unregisterDisplayFrame(final ViewBasedDisplayIFace frame)
    {
        if (displayFrames != null)
        {
            displayFrames.remove(frame);
        }

    }

    /**
     * Shows or hides all the DisplayFrame attached to this MultiView.
     * @param show true - show, false - hide
     */
    public void showDisplayFrames(final boolean show)
    {
        if (displayFrames != null)
        {
            for (ViewBasedDisplayIFace frame : displayFrames)
            {
                frame.showDisplay(show);
            }
        }
    }

    /**
     * Returns the current view.
     * @return the current view
     */
    public Viewable getCurrentView()
    {
        return currentViewable;
    }
    
    /**
     * Returns the current viewable as a FormViewObj if it is one, or return null.
     * @return the current viewable as a FormViewObj if it is one, or return null.
     */
    public FormViewObj getCurrentViewAsFormViewObj()
    {
        return currentViewable instanceof FormViewObj ? (FormViewObj)currentViewable : null;
    }

    /**
     * Returns the options that it was created with.
     * @return the options that it was created with.
     */
    public int getCreateOptions()
    {
        return createOptions;
    }
    
    /**
     * @return the isOKToAddAllAltViews
     */
    public boolean isOKToAddAllAltViews()
    {
        if (AppContextMgr.isSecurityOn() && permissions.isViewOnly())
        {
            return false;
        }
        
        return !MultiView.isOptionOn(createOptions, MultiView.DONT_ADD_ALL_ALTVIEWS);
    }

    /**
     * Adds an item to be deleted to a list.
     * @param deletedItem the item to be deleted.
     */
    public void addDeletedItem(final Object deletedItem)
    {
        if (deletedItems == null)
        {
            deletedItems = new Vector<Object>();
        }
        boolean addToList = true;
        if (deletedItem instanceof FormDataObjIFace && ((FormDataObjIFace)deletedItem).getId() == null)
        {
            addToList = false;
        }
        if (addToList)
        {
            deletedItems.add(deletedItem);
        }
    }
    
    /**
     * Adds an item to be saved to a list.
     * @param toBeSavedItem the item to be saved.
     */
    public void addToBeSavedItem(final Object toBeSavedItem)
    {
        if (toBeSavedItems == null)
        {
            toBeSavedItems = new Vector<Object>();
        }
        
        boolean addToList = true;
        if (!(toBeSavedItem instanceof FormDataObjIFace))// && ((FormDataObjIFace)toBeSavedItem).getId() == null)
        {
            addToList = false;
        }
        if (addToList)
        {
            toBeSavedItems.add(toBeSavedItem);
        }
    }
    
    /**
     * Adds an item to be process by the busrules to a list.
     * @param brItem the item to be busruled.
     */
    public void addBusRuleItem(final Object brItem)
    {
        if (busRulesItems == null)
        {
            busRulesItems = new HashSet<FormDataObjIFace>();
        }
        
        boolean addToList = true;
        if (!(brItem instanceof FormDataObjIFace))
        {
            addToList = false;
        }
        if (addToList)
        {
            busRulesItems.add((FormDataObjIFace)brItem);
        }
        
        System.out.println("addBusRuleItem: "+view.getClassName());
    }
    

    /**
     * Returns a list of items to be deleted, it may return null.
     * @return a list of items to be deleted, it may return null.
     */
    public Vector<Object> getDeletedItems()
    {
        return deletedItems;
    }
    
    /**
     * @return
     */
    public Vector<Object> getToBeSavedItems()
    {
        return toBeSavedItems;
    }
    
    /**
     * @return the busRulesItems
     */
    public HashSet<FormDataObjIFace> getBusRulesItems()
    {
        return busRulesItems;
    }

    /**
     * Clears the list.
     */
    public void clearItemsToBeSaved()
    {
        if (toBeSavedItems != null)
        {
            toBeSavedItems.clear();
        }
    }

    /**
     * Clears the list.
     */
    public void clearItemsToBeDeleted()
    {
        if (deletedItems != null)
        {
            deletedItems.clear();
        }
    }

    /**
     * Clears the list.
     */
    public void clearItemsForBusRules()
    {
        if (busRulesItems != null)
        {
            busRulesItems.clear();
        }
    }

    /**
     * Asks the Viewable to get the data from the UI and transfer the changes (really all the fields) to
     * the DB object.
     */
    public void setSession(final DataProviderSessionIFace session)
    {
        
        for (Enumeration<Viewable> e=viewMapByName.elements();e.hasMoreElements();)
        {
            e.nextElement().setSession(session);
        }
        
        for (MultiView mv : kids)
        {
            mv.setSession(session);
        }
    }
    
    /**
     * Increments to the next number in the series.
     */
    public void updateAutoNumbers()
    {
        FormViewObj formViewObj = getCurrentViewAsFormViewObj();
        if (formViewObj != null)
        {
            //if (formViewObj.getValidator() != null && formViewObj.getValidator().hasChanged())
            //{
                formViewObj.updateAutoNumbers();
            //}
        }
        
        for (MultiView mv : kids)
        {
            mv.updateAutoNumbers();
        }
    }
    
    /**
     * Sets all the resultSetControllers to the proper index.
     */
    public void setViewState(final Vector<ViewState> viewStateList, 
                             final AltViewIFace.CreationMode mode,
                             final int vsInx)
    {
        if (viewStateList != null && vsInx < viewStateList.size())
        {
            ViewState viewState = viewStateList.get(vsInx);
            
            if (vsInx > -1)
            {
                int inx = viewState.getInx();
                if (inx > -1)
                {
                    FormViewObj formViewObj = getCurrentViewAsFormViewObj();
                    if (formViewObj != null && formViewObj.getRsController() != null)
                    {
                        formViewObj.getRsController().setIndex(inx);
                    }
                }
            }
            
            for (MultiView mv : kids)
            {
                if (mv.createWithMode == mode)
                {
                    mv.setViewState(viewStateList, mode, vsInx+1);
                }
            }
        }
    }
    
    /**
     * Collects the current state of each view. This mostly captures the index of the RSC
     * @param viewStateList the list of view states
     * @param mode the mode
     * @param level the level
     */
    public void collectionViewState(final Vector<ViewState> viewStateList, 
                                    final AltViewIFace.CreationMode mode, 
                                    final int level)
    {
        AltViewIFace altView = currentViewable.getAltView();
        
        //String spaces = "                                                          ";
        //log.debug(spaces.substring(0, level)+"Current AltView: "+altView.getName()+"  Num: "+getViewables().size()+"  "+altView.getViewDef().getName());
        
        ViewState viewState = new ViewState(this, currentViewable, altView, altView.getViewDef());
        viewStateList.add(viewState);
        
        FormViewObj formViewObj = getCurrentViewAsFormViewObj();
        if (formViewObj != null && formViewObj.getRsController() != null)
        {
            int curInx = formViewObj.getRsController().getCurrentIndex();
            //log.debug(spaces.substring(0, level)+"  curInx: "+curInx);
            viewState.setInx(curInx);
        }
        
        if (currentViewable.getDataObj() instanceof FormDataObjIFace)
        {
            viewState.setIdentity(((FormDataObjIFace)currentViewable.getDataObj()).getIdentityTitle());
        }
        
        for (MultiView mv : kids)
        {
            if (mv.createWithMode == mode)
            {
                mv.collectionViewState(viewStateList, mode, level+2);
            }
        }
    }
    
    /**
     * Call right before formShutdown is called, but the entire UI is still intact.
     */
    public void aboutToShutdown()
    {
        for (Enumeration<Viewable> e=viewMapByName.elements();e.hasMoreElements();)
        {
            e.nextElement().aboutToShutdown();
        }
        for (MultiView mv : kids)
        {
            mv.aboutToShutdown();
        }
    }

    /**
     * Tells the MultiView the MV that it is being shutdown to be disposed.
     */
    public void shutdown()
    {
        mvParent      = null;
        view          = null;
        data          = null;
        parentDataObj = null;
        currentViewable = null;

        for (Enumeration<Viewable> e=viewMapByName.elements();e.hasMoreElements();)
        {
            e.nextElement().shutdown();
        }

        if (currentValidator != null)
        {
            currentValidator.cleanUp();
        }

        for (MultiView mv : kids)
        {
            mv.shutdown();
        }
        kids.clear();

        shutdownDisplayFrames();
    }
    
    /**
     * Tell the MultiView to shutdown and close all child ViewBasedDisplayIFace windows.
     */
    public void shutdownDisplayFrames()
    {
        if (displayFrames != null)
        {
            for (ViewBasedDisplayIFace vbd : displayFrames)
            {
                if (vbd != null)
                {
                    vbd.shutdown();
                    vbd.dispose();
                } else
                {
                    log.debug("Display frame was null!");
                }
                    
            }
            displayFrames.clear();
        }
    }
    
    /**
     * Helper method to see if an option is turned on.
     * @param options the range of options that can be turned on
     * @param opt the actual option that may be turned on
     * @return true if the opt bit is on
     */
    public static boolean isOptionOn(final int options, final int opt)
    {
        return (options & opt) == opt;
    }
    
    /**
     * Debug Helper method
     * @param msg debug string
     * @param options the options to be printed
     */
    public static void printCreateOptions(final String msg, final int options)
    {
        log.debug(" ");
        log.debug(msg);
        log.debug("RESULTSET_CONTROLLER  ["+((options & MultiView.RESULTSET_CONTROLLER) == MultiView.RESULTSET_CONTROLLER ? "true" : "false")+"]");
        log.debug("IS_NEW_OBJECT         ["+((options & MultiView.IS_NEW_OBJECT) == MultiView.IS_NEW_OBJECT ? "true" : "false")+"]");
        log.debug("VIEW_SWITCHER         ["+((options & MultiView.VIEW_SWITCHER) == MultiView.VIEW_SWITCHER ? "true" : "false")+"]");
        log.debug("HIDE_SAVE_BTN         ["+((options & MultiView.HIDE_SAVE_BTN) == MultiView.HIDE_SAVE_BTN ? "true" : "false")+"]");
        log.debug("IS_EDITTING           ["+((options & MultiView.IS_EDITTING) == MultiView.IS_EDITTING ? "true" : "false")+"]");
        log.debug("IS_SINGLE_OBJ         ["+((options & MultiView.IS_SINGLE_OBJ) == MultiView.IS_SINGLE_OBJ ? "true" : "false")+"]");
        log.debug("NO_SCROLLBARS         ["+((options & MultiView.NO_SCROLLBARS) == MultiView.NO_SCROLLBARS ? "true" : "false")+"]");
        log.debug("ADD_SEARCH_BTN        ["+((options & MultiView.ADD_SEARCH_BTN) == MultiView.ADD_SEARCH_BTN ? "true" : "false")+"]");
        log.debug("DONT_ADD_ALL_ALTVIEWS ["+((options & MultiView.DONT_ADD_ALL_ALTVIEWS) == MultiView.DONT_ADD_ALL_ALTVIEWS ? "true" : "false")+"]");
        log.debug(" ");        
    }
    
    
    class ViewState 
    {
        protected MultiView      mv;
        protected Viewable       viewable;
        protected AltViewIFace   altView;
        protected ViewDefIFace   viewDef;
        protected int            inx;
        protected String         identity = null;
        
        /**
         * @param mv
         * @param altView
         * @param viewDef
         * @param inx
         */
        public ViewState(MultiView mv, Viewable viewable, AltViewIFace altView, ViewDefIFace viewDef)
        {
            super();
            this.mv       = mv;
            this.viewable = viewable;
            this.altView  = altView;
            this.viewDef  = viewDef;
            this.inx      = -1;
            this.identity = null;
        }

        /**
         * @return the identity
         */
        public String getIdentity()
        {
            return identity;
        }

        /**
         * @param identity the identity to set
         */
        public void setIdentity(String identity)
        {
            this.identity = identity;
        }

        /**
         * @return the mv
         */
        public MultiView getMv()
        {
            return mv;
        }

        /**
         * @return the altView
         */
        public AltViewIFace getAltView()
        {
            return altView;
        }

        /**
         * @return the viewDef
         */
        public ViewDefIFace getViewDef()
        {
            return viewDef;
        }

        /**
         * @return the inx
         */
        public int getInx()
        {
            return inx;
        }

        /**
         * @param inx the inx to set
         */
        public void setInx(int inx)
        {
            this.inx = inx;
        }
        
        /**
         * @return the viewable
         */
        public Viewable getViewable()
        {
            return viewable;
        }

        
        
    }
}
