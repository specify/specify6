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

import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.split;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.af.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellField;
import edu.ku.brc.af.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellLabel;
import edu.ku.brc.af.ui.forms.persist.FormCellLabelIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellSubView;
import edu.ku.brc.af.ui.forms.persist.FormCellSubViewIFace;
import edu.ku.brc.af.ui.forms.persist.FormViewDef;
import edu.ku.brc.af.ui.forms.persist.FormViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.TableViewDef;
import edu.ku.brc.af.ui.forms.persist.ViewDef;
import edu.ku.brc.af.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.af.ui.forms.validation.ValidationListener;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.ui.BiColorTableCellRenderer;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Orderable;

/*
 * The Whole idea of the class is that it converts or translates a form/sybform definition into a series of columns.
 * We refer to this as "flatten" the form structure, because a form and subforms are really a hierarchical tree.
 * And the prcessRows method in the ViewFactory will traverse the rows and when it hits a subform it "goes into" 
 * the subform and processes its rows.<br>
 * <br>
 * So we handle this by creating a "stack" that we puch the subforms onto (which represent our context) and then keep processing
 * the subforms. Most of the time an entire subform will be represented by a single column and the information in the subform
 * will be formatted and aggregated into a single to be represented by that single column.<br>
 * <br>
 * Also, sometimes form refer to a field in a "sub object" meaning the form walks the object hierarchy to get the data. We need to also
 * keep track of subform for this same reason.<br>
 * 
 * @code_status Beta
 *
 * @author rods
 *
 */
public class TableViewObj implements Viewable, 
                                     ViewBuilderIFace, 
                                     ValidationListener, 
                                     ResultSetControllerListener, 
                                     AppPrefsChangeListener
{
    private static final Logger log = Logger.getLogger(TableViewObj.class);
    
    protected static CellConstraints        cc              = new CellConstraints();

    // Data Members
    protected DataProviderSessionIFace      session        = null;
    protected boolean                       isEditing      = false;
    protected boolean                       isSavable      = false;  // via the relationship
    protected boolean                       formIsInNewDataMode = false; // when this is true it means the form was cleared and new data is expected
    protected MultiView                     mvParent       = null;
    protected ViewIFace                     view;
    protected AltViewIFace                  altView;
    protected ViewDefIFace                  viewDef;
    protected FormViewDefIFace              formViewDef;
    protected int                           options;
    protected String                        cellName       = null;
    protected List<MultiView>               kids           = new ArrayList<MultiView>();
    protected Vector<AltViewIFace>          altViewsList   = null;
    protected TableViewDef                  tableViewDef;
    protected DataObjectGettable            dataGetter      = null;
    protected Class<?>                      classToCreate   = null;
    protected String                        searchName      = null;
    
    protected Stack<FormCellSubView>        subViewStack    = new Stack<FormCellSubView>();
    protected StringBuilder                 fullObjPath     = new StringBuilder();
    protected int                           skipControls    = 0;
    
    
    protected Hashtable<String, ColumnInfo> controlsByName  = new Hashtable<String, ColumnInfo>();
    protected Hashtable<String, ColumnInfo> controlsById    = new Hashtable<String, ColumnInfo>();
    protected Vector<ColumnInfo>            columnList      = new Vector<ColumnInfo>();
    
    protected FormValidator                 formValidator   = null;
    protected FormDataObjIFace              parentDataObj   = null;
    protected Object                        dataObj         = null;
    protected Set<Object>                   origDataSet     = null;
    protected Vector<Object>                dataObjList     = null;
    protected Vector<Object>                newObjsList     = new Vector<Object>();
    protected Object[]                      singleItemArray = new Object[1];
    protected DateWrapper                   scrDateFormat;
    protected boolean                       isLoaded        = false;
    protected boolean                       isSkippingAttach = false; // Indicates whether to skip before setting data into the form
    protected Boolean                       isRestricted     = null;
    protected String                        restrictedStr    = null;
    
    protected String                        dataClassName;
    protected String                        dataSetFieldName;

    protected RestrictablePanel             mainComp        = null;
    protected ControlBarPanel               controlPanel    = null;
    protected ResultSetController           rsController    = null;
    protected Vector<Object>                list            = null;
    protected boolean                       ignoreSelection = false;
    protected JButton                       saveBtn         = null;
    protected JButton                       validationInfoBtn = null;
    protected boolean                       wasNull         = false;
    protected MenuSwitcherPanel             switcherUI;
    protected JComboBox                     selectorCBX     = null;
    protected int                           mainCompRowInx  = 1;
    protected boolean                       addSearch;
    protected JButton                       searchButton    = null;
    protected JButton                       editButton      = null;
    protected JButton                       newButton       = null;
    protected JButton                       deleteButton    = null;
    protected JPanel                        sepController   = null;

    protected BusinessRulesIFace            businessRules   = null; 

    protected DraggableRecordIdentifier     draggableRecIdentifier   = null;
    
    // Carry Forward
    protected CarryForwardInfo              carryFwdInfo    = null;
    protected boolean                       doCarryForward  = false;
    protected Object                        carryFwdDataObj = null;

    // UI
    protected ColTableModel                 model;
    protected JTable                        table;
    protected JScrollPane                   tableScroller;
    protected JPanel                        orderablePanel;
    protected boolean                       isLoading   = false;
    
    // Reordering
    protected JButton                       orderUpBtn  = null;
    protected JButton                       orderDwnBtn = null;
    protected boolean                       doOrdering  = false;
    
    // Security
    private PermissionSettings              perm = null;
    

    /**
     * Constructor with FormView definition.<NOTE: We cannot build the table here because we need all the column
     * information.
     * @param view the view 
     * @param altView the altview
     * @param mvParent the parent
     * @param formValidator the validator
     * @param options the creation options
     */
    public TableViewObj(final ViewIFace     view,
                        final AltViewIFace  altView,
                        final MultiView     mvParent,
                        final FormValidator formValidator,
                        final int           options,
                        final Color         bgColor)
    {
        this.view        = view;
        this.altView     = altView;
        this.mvParent    = mvParent;
        this.options     = options;
        this.viewDef     = altView.getViewDef();
        this.formValidator = formValidator;
        
        businessRules    = view.createBusinessRule();
        dataGetter       = altView.getViewDef().getDataGettable();
        this.formViewDef = (FormViewDefIFace)altView.getViewDef();
        
        scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");

        AppPreferences.getRemote().addChangeListener("ui.formatting.viewfieldcolor", this);

        boolean createViewSwitcher         = MultiView.isOptionOn(options, MultiView.VIEW_SWITCHER);
        boolean hideSaveBtn                = MultiView.isOptionOn(options, MultiView.HIDE_SAVE_BTN);
        isEditing                          = MultiView.isOptionOn(options, MultiView.IS_EDITTING) && altView.getMode() == AltViewIFace.CreationMode.EDIT;
        
        addSearch = mvParent != null && MultiView.isOptionOn(mvParent.getOptions(), MultiView.ADD_SEARCH_BTN);
        if (addSearch)
        {
            isEditing = false;
        }
        
        setValidator(formValidator);

        boolean addController = mvParent != null && view.getAltViews().size() > 1;

        mainComp = new RestrictablePanel(new BorderLayout());
        mainComp.setBackground(bgColor);
        
        if (mvParent == null)
        {
            mainComp.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        }
        
        List<JComponent> comps = new ArrayList<JComponent>();

        // We will add the switchable UI if we are mvParented to a MultiView and have multiple AltViews
        if (addController)
        {
            boolean saveWasAdded = false;
            
            if (createViewSwitcher)
            {
                // Now we have a Special case that when when there are only two AltViews and
                // they differ only by Edit & View we hide the switching UI unless
                // we are the root MultiView. This way when switching the Root View all the other views switch
                // (This is because they were created that way. It also makes no sense that while in "View" mode
                // you would want to switch an individual subview to a differe "mode" view than the root).

                altViewsList = new Vector<AltViewIFace>();
                
                // Very Special Case until Tables can handle edit mode or be in edit mode.
                // Since they can't, and when a form is for a "new" object then we need to fake out the switcher code 
                // so it thinks we are in edit mode (at this time Tables are ALWAYS in View mode)
                // so we temporarily set the mode of the Table's AltViewIFace to Edit create the switcher
                // and then set it back to View.
                boolean overrideViewMode = MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT) || isEditing;
                
                AltViewIFace.CreationMode tempMode = null;
                //if (isNewObj && altView.getMode() == AltViewIFace.CreationMode.View)
                if (overrideViewMode && altView.getMode() == AltViewIFace.CreationMode.VIEW)
                                       {
                    tempMode = altView.getMode();
                    altView.setMode(AltViewIFace.CreationMode.EDIT);
                }
                
                switcherUI = FormViewObj.createMenuSwitcherPanel(mvParent, view, altView, altViewsList, mainComp);
                
                if (tempMode != null)
                {
                    altView.setMode(tempMode);
                }
                
                if (altViewsList.size() > 0)
                {
                    if (isEditing || addSearch)
                    {
                        String delTTStr = ResultSetController.createTooltip("RemoveRecordTT", view.getObjTitle());
                        deleteButton = UIHelper.createIconBtnTT("DeleteRecord", IconManager.IconSize.Std16, delTTStr, false, new ActionListener() {
                            public void actionPerformed(ActionEvent e)
                            {
                                deleteRow(table.getSelectedRow());
                            }
                        });
                        
                        if (addSearch)
                        {
                            String srchTTStr = ResultSetController.createTooltip("SearchForRecordTT", view.getObjTitle());
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
                            
                            searchButton = UIHelper.createIconBtnTT("Search", IconManager.IconSize.Std16, srchTTStr, false, new ActionListener() {
                                public void actionPerformed(ActionEvent e)
                                {
                                    doSearch();
                                }
                            });
                            searchButton.setEnabled(true);
                            
                        } else
                        {
                            String edtTTStr  = ResultSetController.createTooltip("EditRecordTT",   view.getObjTitle());
                            String newTTStr  = ResultSetController.createTooltip("NewRecordTT",    view.getObjTitle());
                            editButton   = UIHelper.createIconBtnTT("EditForm", IconManager.IconSize.Std16, edtTTStr, false, new ActionListener() {
                                public void actionPerformed(ActionEvent e)
                                {
                                    editRow(table.getSelectedRow(), false);
                                }
                            });
                            newButton    = UIHelper.createIconBtnTT("CreateObj", IconManager.IconSize.Std16, newTTStr, false, new ActionListener() {
                                public void actionPerformed(ActionEvent e)
                                {
                                    editRow(table.getSelectedRow(), true);
                                }
                            });
                        }
                        
                        boolean isAbove = mvParent.getSeparator() != null;  
                        
                        int cnt = (deleteButton != null ? 1 : 0) + (searchButton != null ? 1 : 0) + (editButton != null ? 1 : 0) + (newButton != null ? 1 : 0);
                        
                        PanelBuilder builder = new PanelBuilder(new FormLayout((isAbove ? "1px," : "f:1px:g,") +UIHelper.createDuplicateJGoodiesDef("p", "1px", cnt), "p"));
                        int x = 2;
                        if (newButton != null)
                        {
                            builder.add(newButton, cc.xy(x,1));  
                            x += 2;
                        }
                        
                        if (deleteButton != null)
                        {
                            builder.add(deleteButton, cc.xy(x,1));  
                            x += 2;
                        }
                        
                        if (editButton != null)
                        {
                            builder.add(editButton, cc.xy(x,1));  
                            x += 2;
                        }
                        
                        if (searchButton != null)
                        {
                            builder.add(searchButton, cc.xy(x,1));  
                            x += 2;
                        }
                        
                        builder.getPanel().setBackground(bgColor);
                        
                        if (isAbove)
                        {
                            sepController = builder.getPanel();
                        } else
                        {
                            comps.add(builder.getPanel());
                        }
                        
                        if (saveBtn != null)
                        {
                            comps.add(saveBtn);
                            saveWasAdded = true;
                        }
                        
                    } else
                    {
                         editButton = UIHelper.createButton("InfoIcon", getResourceString("ViewRecord"), IconManager.IconSize.Std16, true);
                         editButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e)
                            {
                                editRow(table.getSelectedRow(), false);
                            }
                        });
                         PanelBuilder builder = new PanelBuilder(new FormLayout("f:1px:g,p,10px", "p"));
                         builder.add(editButton, cc.xy(2,1));
                         if (mvParent.getSeparator() != null)
                         {
                             sepController = builder.getPanel();
                         } else
                         {
                             comps.add(builder.getPanel());
                         }
                         builder.getPanel().setBackground(bgColor);
                    }
                    
                    updateUI(false);
                    
                    if (switcherUI != null)
                    {
                        comps.add(switcherUI);
                    }
                }
            }
            
            if (!saveWasAdded && altView.getMode() == AltViewIFace.CreationMode.EDIT)
            {
                if (mvParent != null && mvParent.isTopLevel() && !hideSaveBtn && saveBtn != null)
                {
                    if (saveBtn != null)
                    {
                        comps.add(saveBtn);
                    }
                }
                JComponent valInfoBtn = FormViewObj.createValidationIndicator(mainComp, getValidator());
                if (valInfoBtn != null)
                {
                    comps.add(valInfoBtn);
                }
            }
        }
        
        if (businessRules != null)
        {
            businessRules.initialize(this);
        }
        
        if (comps.size() > 0 || addController)
        {
            controlPanel = new ControlBarPanel(bgColor);
            controlPanel.addComponents(comps, false); // false -> right side
            //mainBuilder.add(controlPanel, cc.xy(1, mainCompRowInx+2));
            mainComp.add(controlPanel, BorderLayout.SOUTH);
        }
        
        if (AppContextMgr.isSecurityOn())
        {
            setPermsFromTableInfo(view.getClassName());
        } else
        {
            perm = new PermissionSettings(PermissionSettings.ALL_PERM);
        }
    }
    
    /**
     * @param tableInfo
     */
    private void setPermsFromTableInfo(final String className)
    {
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(className);
        if (tableInfo != null)
        {
            perm = SecurityMgr.getInstance().getPermission("DO."+tableInfo.getShortClassName().toLowerCase());
            isRestricted = !perm.canView();
            if (isRestricted)
            {
                restrictedStr = getResourceString("RESTRICTED");
            }
            if (deleteButton != null)
            {
                deleteButton.setVisible(perm.canDelete());
            }
            if (editButton != null)
            {
                editButton.setVisible(perm.canModify());
            }
            if (newButton != null)
            {
                newButton.setVisible(perm.canAdd());
            }
        }
    }
    
    /**
     * @return the newButton
     */
    public JButton getNewButton()
    {
        return newButton;
    }


    /**
     * @return the deleteButton
     */
    public JButton getDeleteButton()
    {
        return deleteButton;
    }

    /**
     * @return the mvParent
     */
    public MultiView getMVParent()
    {
        return mvParent;
    }

    /**
     * 
     */
    private void doSearch()
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
                List<Object>  newDataObjects = dlg.getSelectedObjects();
                boolean doSetNewDataObj = true;
                
                if (businessRules != null && newDataObjects != null)
                {
                    if (businessRules.doesSearchObjectRequireNewParent())
                    {
                        //createNewDataObject(false);
                        //doSetNewDataObj = false;
                        throw new RuntimeException("NOT IMPLMENTED!");
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
                    /*this.setDataIntoUI();
                    
                    if (formValidator != null)
                    {
                        formValidator.validateForm();
                    }*/
                }
                
                if (newDataObjects != null && newDataObjects.size() > 0)
                {
                    if (doSetNewDataObj)
                    {
                        if (dataObjList != null && origDataSet != null)
                        {
                            dataObjList.addAll(newDataObjects);
                            origDataSet.addAll(newDataObjects);
                            
                            for (Object dObj : newDataObjects)
                            {
                                // rods - 09/12/08 - Changed the arg to true from false because 
                                // when adding searched items they weren't getting the parent object
                                // might need to have it true only for search items
                                parentDataObj.addReference((FormDataObjIFace)dObj, dataSetFieldName, true);
                                if (!isSavable && addSearch && mvParent != null && ((FormDataObjIFace)dObj).getId() != null)
                                {
                                    mvParent.getTopLevel().addToBeSavedItem(dObj);
                                }
                            }
                            
                            if (rsController != null)
                            {
                                int len = dataObjList.size();
                                rsController.setLength(len);
                                rsController.setIndex(len-1, false);
                            }
                            model.fireDataChanged();
                            tellMultiViewOfChange();
                            formValidator.validateRoot();
                            
                            newObjsList.addAll(newDataObjects);
                            
                        } else 
                        {
                            setDataObj(newDataObjects.get(0));
                        }
                        
                        // Set the data and validate
                        this.setDataIntoUI();
                        
                        if (formValidator != null)
                        {
                            formValidator.validateForm();
                        }
                    }
                }
            }

        } else
        {
            log.error("The search name is empty is there one defined in the display tag for the XML?");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setClassToCreate(java.lang.String)
     */
    public void setClassToCreate(final Class<?> classToCreate)
    {
        this.classToCreate = classToCreate;
        
        if (perm == null && this.classToCreate != null)
        {
            setPermsFromTableInfo(classToCreate.getName());
        }
    }
    
    /**
     * Sizes the table to number of rows using getRowHeight
     * @param query the table to be sized
     * @param rows the number of rows
     */
    public void setVisibleRowCount(int rows)
    {
        if (table != null)
        {
            table.setPreferredScrollableViewportSize(new Dimension( 
                    table.getPreferredScrollableViewportSize().width, 
                    rows*table.getRowHeight()));
        }
    }
    
    /**
     * Sizes the table to number of rows using the height of actual rows.
     * @param query the table to be sized
     * @param rows the number of rows
     */
    public void setVisibleRowCountForHeight(int rows)
    { 
        if (table != null)
        {
            int height = 0; 
            for(int row=0; row<rows; row++) 
                height += table.getRowHeight(row); 
         
            table.setPreferredScrollableViewportSize(new Dimension( 
                    table.getPreferredScrollableViewportSize().width, 
                    height 
            ));
        }
    }


    /**
     * Sets all the Columns to be center justified this COULD be set up in the table info.
     *
     */
    protected void configColumns()
    {
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        //DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        //renderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        DefaultTableCellRenderer renderer = new BiColorTableCellRenderer(true); // Centered


        TableColumnModel tableColModel = table.getColumnModel();
        for (int i=0;i<tableColModel.getColumnCount();i++)
        {
            tableColModel.getColumn(i).setCellRenderer(renderer);
        }
    }
    
    /**
     * Build the table now that we have all the information we need for the columns.
     */
    protected void buildTable()
    {
        // Now Build the JTable
        model = new ColTableModel();
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setFocusable(false);
        //table.setPreferredScrollableViewportSize(new Dimension(200,table.getRowHeight()*6));
        
        configColumns();
        
        //table.setCellSelectionEnabled(false);
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    updateUI(!lsm.isSelectionEmpty());
                }
            }
        });
            
        table.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e)
            {
                if ( e.getClickCount() == 2 )
                {
                    int index = table.getSelectedRow();
                    editRow(index, false);
                }
            }
        });

        
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        /*
         
        // This is BROKEN!
        table.setCellSelectionEnabled(true);
        
        for (int i=0;i<model.getColumnCount();i++) 
        {
            TableColumn column = table.getColumn(model.getColumnName(i));
            
            //log.info(model.getColumnName(i));
            //column.setCellRenderer(renderer);
            
            ColumnInfo columnInfo = columnList.get(i);
            Component  comp       = columnInfo.getComp();
            
            //column.setCellEditor(new DefaultCellEditor(new JTextField()));
            if (comp instanceof GetSetValueIFace)
            {
                column.setCellEditor(new MyTableCellEditor(columnInfo));
                
            } else if (comp instanceof JTextField)
            {
                column.setCellEditor(new DefaultCellEditor((JTextField)comp));
                
            } else
            {
                log.error("Couldn't figure out DefaultCellEditor for comp ["+comp.getClass().getSimpleName()+"]");
            }
        }
        */
        
        tableScroller = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        orderablePanel = new JPanel(new BorderLayout());
        orderablePanel.add(tableScroller, BorderLayout.CENTER);
        
        mainComp.add(orderablePanel, BorderLayout.CENTER);
        
        initColumnSizes(table);
    }
    
    /**
     * Reorders items from a starting index.
     * @param startInx the starting index.
     */
    protected void reorderItems(final int startInx)
    {
        for (int i=startInx;i<dataObjList.size();i++)
        {
            Object obj = dataObjList.get(i);
            if (obj instanceof Orderable)
            {
                Orderable orderable = (Orderable)obj;
                orderable.setOrderIndex(i);
            }
        }
        model.fireDataChanged();
        tellMultiViewOfChange();
    }
    
    /**
     * Reorders a list item up.
     */
    protected void orderUp()
    {
        int inx = table.getSelectedRow();
        if (inx > -1)
        {
            Object item = dataObjList.get(inx);
            dataObjList.remove(inx);
            dataObjList.insertElementAt(item, inx-1);
            reorderItems(inx-1);
        }
        table.getSelectionModel().setSelectionInterval(inx-1, inx-1);
        tellMultiViewOfChange();
    }
    
    /**
     * Reorders a list item up.
     */
    protected void orderDown()
    {
        int inx = table.getSelectedRow();
        if (inx > -1)
        {
            Object item = dataObjList.get(inx);
            dataObjList.remove(inx);
            dataObjList.insertElementAt(item, inx+1);
            reorderItems(inx);
        }
        table.getSelectionModel().setSelectionInterval(inx+1, inx+1);
    }
    
    /**
     * Sets the button enabled/disabled state.
     * @param hasSelection whether something is selected.
     */
    protected void updateUI(final boolean hasSelection)
    {
        if (editButton != null)
        {
            editButton.setEnabled(hasSelection);
        }
        
        if (deleteButton != null)
        {
            int inx = table != null ? table.getSelectedRow() : -1;
            Object item = inx > -1 ? dataObjList.get(inx) : null;
            deleteButton.setEnabled(hasSelection && (businessRules == null || businessRules.okToEnableDelete(item)));
        }
        
        if (doOrdering && table != null)
        {
            int inx = table.getSelectedRow();
            orderUpBtn.setEnabled(inx > 0);
            orderDwnBtn.setEnabled(inx > -1 && inx < table.getRowCount()-1);
        }
        
        updateUIEnabled();
    }
    
    /**
     * 
     */
    private void updateUIEnabled()
    {
        if (newButton != null)
        {
            newButton.setEnabled(parentDataObj != null);
        }
    }
    
    /**
     * Tells MV and UI that a change has been made. 
     */
    protected void tellMultiViewOfChange()
    {
        if (formValidator != null)
        {
            formValidator.setHasChanged(true);
            //formValidator.validateForm();
            formValidator.wasValidated(null);
        }
    }
    
    /**
     * Can create a new item or edit an existing it; or view and existing item.
     * @param rowIndex the index tho be editted
     * @param isEdit whether we are editing or view
     * @param isNew hwther the object is new
     */
    @SuppressWarnings("unchecked")
    protected void editRow(final int rowIndex, final boolean isNew)
    {
        if (isNew)
        {
            // Check to see if the business rules will be creating the object
            // if so the BR will then call setNewObject
            if (businessRules != null && businessRules.canCreateNewDataObject())
            {
                businessRules.createNewObj(true, null);
                
            } else
            {
                // OK, we need to create it locally
                FormDataObjIFace dObj;
                if (classToCreate != null)
                {
                    dObj = FormHelper.createAndNewDataObj(classToCreate);
                } else
                {
                    dObj = FormHelper.createAndNewDataObj(view.getClassName());
                }
                editRow(dObj, rowIndex, isNew);
            }
        } else
        {
            FormDataObjIFace dObj = (FormDataObjIFace)dataObjList.get(rowIndex);
            if (dObj == null)
            {
                return;
            }
            editRow(dObj, rowIndex, isNew);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setNewObject(edu.ku.brc.af.ui.forms.FormDataObjIFace)
     */
    @Override
    public void setNewObject(FormDataObjIFace newDataObj)
    {
        editRow(newDataObj, table.getSelectedRow(), true);
    }

    /**
     * Can create a new item or edit an existing it; or view and existing item.
     * @param rowIndex the index tho be editted
     * @param isEdit whether we are editing or view
     * @param isNew hwther the object is new
     */
    @SuppressWarnings("unchecked")
    protected void editRow(final FormDataObjIFace dObj, final int rowIndex, final boolean isNew)
    {
        // Add it in here so the Business Rules has a parent object to
        // get state from.
        if (parentDataObj != null && isEditing && isNew)
        {
            parentDataObj.addReference(dObj, dataSetFieldName);
        }
        
        final ViewBasedDisplayIFace dialog = FormHelper.createDataObjectDialog(altView, mainComp, dObj, isEditing, isNew);
        if (dialog != null)
        {
            // Now we need to get the MultiView and add it into the MV tree
            MultiView multiView = dialog.getMultiView();
            
            // Note: The 'real' parent is the parent of the current MultiView
            // this is because the table's MultiView doesn't have a validator.
            MultiView realParent = mvParent.getMultiViewParent();
            if (realParent != null)
            {
                realParent.addChildMV(multiView);
            }
            
            multiView.addCurrentValidator();
            
            if (isNew && multiView.getCurrentViewAsFormViewObj() != null)
            {
                multiView.getCurrentViewAsFormViewObj().getBusinessRules().addChildrenToNewDataObjects(dObj);
            }
            
            dialog.setParentData(parentDataObj);
            
            DataProviderSessionIFace localSession = null;
            try
            {
                if (!isSkippingAttach)
                {
                    if (dObj.getId() != null)
                    {
                        localSession = DataProviderFactory.getInstance().createSession();
                        localSession.attach(dObj);
                    }
                    dialog.setSession(localSession);
                }
                
                System.err.println(dObj);
                dialog.setData(dObj);
                dialog.showDisplay(true);
                
                dialog.setSession(null);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TableViewObj.class, ex);
                
            } finally
            {
                if (localSession != null)
                {
                    localSession.close();
                }
            }
            
            // OK, now unhook everything (MVs and the validators)
            multiView.removeCurrentValidator();
            if (realParent != null)
            {
                realParent.removeChildMV(multiView);
            }
            
            if (isEditing)
            {
                if (dialog.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
                {
                    dialog.getMultiView().getDataFromUI();
                    if (mvParent != null)
                    {
                        tellMultiViewOfChange();
                        
                        Object daObj = dialog.getMultiView().getData();
                        if (isNew)
                        {
                            if (daObj instanceof Orderable)
                            {
                                // They really should all be Orderable, 
                                // but just in case we check each one.
                                int maxOrder = -1;
                                for (Object obj : dataObjList)
                                {
                                    if (obj instanceof Orderable)
                                    {
                                        maxOrder = Math.max(((Orderable)obj).getOrderIndex(), maxOrder);
                                    }
                                }
                                
                                ((Orderable)daObj).setOrderIndex(maxOrder+1);
                                
                                if (orderUpBtn == null)
                                {
                                    addOrderablePanel();
                                }

                            }
                            dataObjList.add(daObj);
                            
                            if (dataObjList != null && dataObjList.size() > 0)
                            {
                                if (dataObjList.get(0) instanceof Comparable<?>)
                                {
                                    Collections.sort((List)dataObjList);
                                }
                            }
                                                 
                            if (origDataSet != null)
                            {
                                origDataSet.add(daObj);
                            }
                            model.setValueAt(daObj, 0, dataObjList.size()-1);
                        }
                        model.fireDataChanged();
                        table.invalidate();
                        table.repaint();
                        
                        JComponent comp = mvParent.getTopLevel();
                        comp.validate();
                        comp.repaint();
                    }
                } else if (dialog.getBtnPressed() == ViewBasedDisplayIFace.CANCEL_BTN)
                {
                    // since it was added in before the dlg was shown we now need to remove.
                    if (parentDataObj != null && isEditing && isNew)
                    {
                        parentDataObj.removeReference(dObj, dataSetFieldName);
                    }
                    
                    if (mvParent.getMultiViewParent() != null && mvParent.getMultiViewParent().getCurrentValidator() != null)
                    {
                        mvParent.getCurrentValidator().setHasChanged(true);
                        mvParent.getMultiViewParent().getCurrentValidator().validateForm();
                    }
                }
            }
            dialog.dispose();
            
        } else if (parentDataObj != null)
        {
            parentDataObj.removeReference(dObj, dataSetFieldName);
        }

    }
    
    /**
     * Creates and adds a re-order panel to the table view.
     */
    protected void addOrderablePanel()
    {
        doOrdering = true;
        
        orderUpBtn = createIconBtn("ReorderUp", "ES_RES_MOVE_UP", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                orderUp();
            }
        });
        orderDwnBtn = createIconBtn("ReorderDown", "ES_RES_MOVE_DOWN", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                orderDown();
            }
        });
        
        PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
        upDownPanel.add(orderUpBtn,       cc.xy(1, 2));
        upDownPanel.add(orderDwnBtn,      cc.xy(1, 4));

        orderablePanel.add(upDownPanel.getPanel(), BorderLayout.EAST);
        orderablePanel.validate();
        orderablePanel.invalidate(); 
        orderablePanel.doLayout();
    }
    
    /**
     * Deletes a Row in the table and there an data object
     * @param rowIndex the item to be deleted
     */
    protected void deleteRow(final int rowIndex)
    {
        FormDataObjIFace dObj = (FormDataObjIFace)dataObjList.get(rowIndex);
        if (dObj != null)
        {
            Object[] delBtnLabels = {getResourceString(addSearch ? "Remove" : "Delete"), getResourceString("CANCEL")};
            int rv = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), UIRegistry.getLocalizedMessage("ASK_DELETE", dObj.getIdentityTitle()),
                                                  getResourceString("Delete"),
                                                  JOptionPane.YES_NO_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE,
                                                  null,
                                                  delBtnLabels,
                                                  delBtnLabels[1]);
            if (rv == JOptionPane.YES_OPTION)
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
                        log.debug(ri.getType());
                    }
                }
                doOtherSide = false;
                parentDataObj.removeReference(dObj, dataSetFieldName, doOtherSide || addSearch);
                if (addSearch && mvParent != null && dObj.getId() != null)
                {
                    mvParent.getTopLevel().addToBeSavedItem(dObj);
                }
                
                // 'addSearch' is used in FormViewObj, but here maybe we need to use 'doOtherSide'
                if (addSearch && mvParent != null && dObj.getId() != null)
                {
                    mvParent.getTopLevel().addToBeSavedItem(dObj);
                }

                dataObjList.remove(rowIndex);
                
                model.fireDataChanged();
                table.invalidate();
                
                JComponent comp = mvParent.getTopLevel();
                comp.validate();
                comp.repaint();
                
                reorderItems(rowIndex);
                
                tellMultiViewOfChange();
                
                table.getSelectionModel().clearSelection();
                updateUI(false);
                
                mvParent.getTopLevel().getCurrentValidator().setHasChanged(true);
                mvParent.getTopLevel().getCurrentValidator().validateForm();
                
                mvParent.getCurrentValidator().setHasChanged(true);
                mvParent.getMultiViewParent().getCurrentValidator().validateForm();
                
                if (!addSearch)
                {
                    // Delete a child object by caching it in the Top Level MultiView
                    if (mvParent != null && !mvParent.isTopLevel())
                    {
                        mvParent.getTopLevel().addDeletedItem(dObj);
                        String delMsg = (businessRules != null) ? businessRules.getDeleteMsg(dObj) : "";
                        UIRegistry.getStatusBar().setText(delMsg);
                    }
                }
            }
        }
    }
    
    /**
     * Adjust all the column width for the data in the column, this may be handles with JDK 1.6 (6.)
     * @param tableArg the table that should have it's columns adjusted
     */
    private void initColumnSizes(final JTable tableArg) 
    {
        ColTableModel     tblModel    = (ColTableModel)tableArg.getModel();
        TableColumn       column      = null;
        Component         comp        = null;
        int               headerWidth = 0;
        int               cellWidth   = 0;
        
        TableCellRenderer headerRenderer = tableArg.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < tblModel.getColumnCount(); i++) {
            column = tableArg.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                                 null, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = tableArg.getDefaultRenderer(tblModel.getColumnClass(i)).
                getTableCellRendererComponent(tableArg, tblModel.getValueAt(0, i), false, false, 0, i);
            
            cellWidth = comp.getPreferredSize().width;

            /*
            if (DEBUG) {
                System.out.println("Initializing width of column "
                                   + i + ". "
                                   + "headerWidth = " + headerWidth
                                   + "; cellWidth = " + cellWidth);
            }*/

            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }
    
    public void refreshDataList()
    {
        dataObjList.clear();
        dataObjList.addAll(origDataSet);
        model.fireDataChanged();
    }
    /**
     * Returns the JTable.
     * @return the JTable.
     */
    public JTable getTable()
    {
        return table;
    }
    

    /**
     * Set the form formValidator and hooks up the root form to listen also.
     * @param formValidator the formValidator
     */
    protected void setValidator(final FormValidator formValidator)
    {
        this.formValidator = formValidator;

        // If there is a form validator and this is not the "root" form 
        // then add this form as a listener to the validator AND
        // make the root form a listener to this validator.
        if (formValidator != null && mvParent != null)
        {
            formValidator.addValidationListener(this);

            //log.debug(formViewDef.getName()+ " formValidator: "+formValidator);
            //registerWithRootMV(true);
        }
    }
    
    //-------------------------------------------------
    // Viewable
    //-------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getControllerPanel()
     */
    @Override
    public JComponent getControllerPanel()
    {
        return sepController;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getName()
     */
    public String getName()
    {
        return "Table Viewer for Forms"; // this is not retrieved
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getId()
     */
    public int getId()
    {
        return -1; // I think is needed for DnD of the current row, which will be implemented later.
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getType()
     */
    public ViewDef.ViewType getType()
    {
        return tableViewDef.getType();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getUIComponent()
     */
    public Component getUIComponent()
    {
        // At this point we should have amodel and everything we need for building the table
        if (model == null)
        {
            buildTable();
        }
        return mainComp;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#isSubform()
     */
    public boolean isSubform()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getCompById(java.lang.String)
     */
    public <T> T getCompById(final String id)
    {
        return null; //  Not applicable
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getLabelById(java.lang.String)
     */
    public JLabel getLabelFor(final String id)
    {
        return null; //  Not applicable
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getControlMapping()
     */
    public Map<String, Component> getControlMapping()
    {
        return null; //  Not applicable
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getValidator()
     */
    public FormValidator getValidator()
    {
        return formValidator;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setDataObj(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void setDataObj(final Object dataObj)
    {
        this.dataObj = dataObj;
        
        if (dataObj instanceof List)
        {
            origDataSet = null;
            if (dataObj instanceof Vector)
            {
                dataObjList = (Vector<Object>)(List<Object>)dataObj;
            } else
            {
                dataObjList = new Vector<Object>((List<Object>)dataObj);
            }
            
            newObjsList.addAll(dataObjList);
            
        } else
        {
            if (dataObjList == null)
            {
                dataObjList = new Vector<Object>();
            } else
            {
                dataObjList.clear(); 
            }
            
            if (dataObj instanceof Set)
            {
                origDataSet = (Set<Object>)dataObj;
                List newList = Collections.list(Collections.enumeration(origDataSet));
                if (newList.size() > 0)
                {
                    Object firstObj = newList.get(0);
                    if (firstObj instanceof Comparable<?>)
                    {
                        Collections.sort(newList);
                    }
                    
                    if (firstObj instanceof Orderable && isEditing && orderUpBtn == null)
                    {
                        addOrderablePanel();
                    }
                }
                dataObjList.addAll(newList);
                newObjsList.addAll(newList);
                
            } else if (dataObj instanceof RecordSetIFace)
            {
                //this.dataObj = dataObj;
                /*
                RecordSetIFace recordSet = (RecordSetIFace)dataObj;
                
                DBTableIdMgr.getInstance().getInClause(recordSet);
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().lookupInfoById(recordSet.getDbTableId());
                
                DataProviderFactory.getInstance().evict(tableInfo.getClassObj());
                
                //DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                
                String sqlStr = DBTableIdMgr.getInstance().getQueryForTable(recordSet);
                if (StringUtils.isNotBlank(sqlStr))
                {
                    dataObjList =(List<Object>)session.getDataList(sqlStr);
                }
*/
            } else
            {
                // single object
                dataObjList.add(dataObj);
                newObjsList.add(dataObj);
            }
        }
        
        setDataIntoUI();
        
        if (table != null)
        {
            table.tableChanged(new TableModelEvent(model));
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setRecordSet(edu.ku.brc.dbsupport.RecordSetIFace)
     */
    public void setRecordSet(RecordSetIFace recordSet)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getDataObj()
     */
    public Object getDataObj()
    {
        return dataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setParentDataObj(java.lang.Object)
     */
    public void setParentDataObj(Object parentDataObj)
    {
        this.parentDataObj = (FormDataObjIFace)parentDataObj;
        updateUIEnabled();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getParentDataObj()
     */
    public Object getParentDataObj()
    {
        return parentDataObj;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setDataIntoUI()
     */
    public void setDataIntoUI()
    {
        if (newObjsList.size() == 0)
        {
            return;
        }
        
        //log.debug(dataObjList.size());
        //if (parentDataObj.getId() != null && dataObjList != null && model != null)
        if (dataObjList != null && model != null)
        {
            DataProviderSessionIFace tmpSession = session;
            try
            {
                // 06/29/09 - rods - Bug 7424 The attach (or merge) causes an exception
                // Without the attach/merge it gets a different exception, but that was solved 
                // by adding a additional items to the forceLoad of the data object 

                if (tmpSession == null && !isSkippingAttach)
                {
                    /*tmpSession = DataProviderFactory.getInstance().createSession();
                    for (Object dObj : newObjsList)
                    {
                        if (dObj != null && dObj instanceof FormDataObjIFace && ((FormDataObjIFace)dObj).getId() != null)
                        {
                            tmpSession.attach(dObj);
                        } else
                        {
                            //log.error("Obj in list is null!");
                        }
                    }
                    */
                }
                isLoaded = true;
        
                for (int i=0;i<dataObjList.size();i++)
                {
                    for (int j=0;j<model.getColumnCount();j++)
                    {
                        model.getValueAt(i, j);
                    }
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TableViewObj.class, ex);
                
            } finally
            {
                if (session == null && tmpSession != null)
                {
                    tmpSession.close();
                    
                }
            }
        }
        newObjsList.clear();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getDataFromUI()
     */
    public void getDataFromUI()
    {
        // Not applicable
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getDataFromUIComp(java.lang.String)
     */
    public Object getDataFromUIComp(final String name)
    {
        return null; // Not applicable
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setDataIntoUIComp(java.lang.String, java.lang.Object)
     */
    public void setDataIntoUIComp(final String name, Object data)
    {
        // Not applicable
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getSubView(java.lang.String)
     */
    public MultiView getSubView(final String name)
    {
        return null; // Not applicable
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getFieldIds(java.util.List)
     */
    public void getFieldIds(final List<String> fieldIds)
    {
        // Not applicable
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getFieldNames(java.util.List)
     */
    public void getFieldNames(List<String> fieldNames)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#aboutToShow(boolean)
     */
    public void aboutToShow(boolean show)
    {
        
        if (origDataSet != null && list != null && origDataSet.size() != list.size())
        {
            // XXX Ok here we know new items have been added
            // so we need to resort (maybe) but certainly need to re-adjust the RecordSet controller.
            //
            // Actually check the sizes isn't enough, we need to really know if there was a change in the list
        }
        
        if (switcherUI != null)
        {
            ignoreSelection = true;
            switcherUI.set(altView);
            ignoreSelection = false;
        }
        
        if (formValidator != null)
        {
            formValidator.validateForm();
        }
        
        // Moving this to the MultiView
        /*if (show)
        {
            log.debug("Dispatching a Data_Entry/ViewWasShown command/action");
            CommandDispatcher.dispatch(new CommandAction("Data_Entry", "ViewWasShown", this));
        }*/
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getView()
     */
    public ViewIFace getView()
    {
        return view;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getViewDef()
     */
    public FormViewDef getViewDef()
    {
        return (FormViewDef)altView.getViewDef();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getAltView()
     */
    public AltViewIFace getAltView()
    {
        return altView;
    }
    
    /* (non
     * -Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#hideMultiViewSwitch(boolean)
     */
    public void hideMultiViewSwitch(boolean hide)
    {
        /*
        if (altViewUI != null)
        {
            altViewUI.setVisible(!hide);
        }
        */
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#dataHasChanged()
     */
    public void validationWasOK(boolean wasOK)
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setSession(org.hibernate.Session)
     */
    @SuppressWarnings("unchecked")
    public void setSession(final DataProviderSessionIFace session)
    {
        this.session = session;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setCellName(java.lang.String)
     */
    public void setCellName(String cellName)
    {
        this.cellName = cellName;
        this.dataSetFieldName = cellName;
        
        if (parentDataObj == null)
        {
            this.dataClassName = viewDef.getClassName();
        }
        
        if (cellName != null)
        {
            DBTableInfo parentTI = DBTableIdMgr.getInstance().getByClassName(mvParent.getMultiViewParent().getView().getClassName());
            if (parentTI != null)
            {
                DBRelationshipInfo rel = parentTI.getRelationshipByName(cellName);
                if (rel != null)
                {
                    isSavable = rel.isSavable();
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setHasNewData(boolean)
     */
    public void setHasNewData(final boolean isNewForm)
    {
        // this gives you the opportunity to adjust your UI
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#registerSaveBtn(javax.swing.JButton)
     */
    public void registerSaveBtn(JButton saveBtnArg)
    {
        this.saveBtn = saveBtnArg;
    }  
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#updateSaveBtn()
     */
    public void updateSaveBtn()
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#focus()
     */
    public void focus()
    {
        // do nothing
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
     * @see edu.ku.brc.af.ui.forms.Viewable#shutdown()
     */
    public void shutdown()
    {
        if (businessRules != null)
        {
            businessRules.formShutdown();
            businessRules = null;
        }
        
        mvParent      = null;
        tableViewDef  = null;
        table         = null;
        tableScroller = null;
        mainComp      = null;
    }
    
    /**
     * Concats a name onto the fullObjPath with a "." separator if necessary.
     * @param cName the component's name
     * @return the new name
     */
    protected String appendName(final String cName)
    {
        return fullObjPath.toString() + (fullObjPath.length() > 0 ? "." : "") + cName;
    }
    
    //-------------------------------------------------
    // ViewBuilderIFace
    //-------------------------------------------------

    /**
     * Adds a control by name so it can be looked up later.
     * @param formCellLabel the FormCell def that describe the cell
     * @param label the the label to be added
     */

    public void addLabel(final FormCellLabel formCellLabel, final JLabel label)
    {
        if (skipControls > 0)
        {
            return;
        }
        if (formCellLabel != null && StringUtils.isNotEmpty(formCellLabel.getLabelFor()))
        {
            String fullCompName = appendName(formCellLabel.getLabelFor());
            ColumnInfo colInfo = controlsById.get(fullCompName);
            if (colInfo == null)
            {
                colInfo = new ColumnInfo(getParentClassName(), formCellLabel, fullCompName, null, null);
                controlsById.put(fullCompName, colInfo);
            }
        }
     }
    
    /**
     * Gets the current class name for the stack context.
     * @return the parent object class name
     */
    protected String getParentClassName()
    {
        String className = null;
        FormCellSubViewIFace formSubView = subViewStack.size() > 0 ? subViewStack.peek() : null;
        if (formSubView != null)
        {
            className = formSubView.getClassDesc();
        } 
        
        if (StringUtils.isEmpty(className))
        {
            className = view.getClassName();
        }
        return className;
    }

    /**
     * Adds a control by name so it can be looked up later.
     * @param formCell the FormCell def that describe the cell
     * @param control the control
     */
    public void registerControl(final FormCellIFace formCell, final Component control)
    {
        if (skipControls > 0)
        {
            return;
        }
        
        if (formCell != null)
        {
            String fullCompName = appendName(formCell.getName());
            
            JScrollPane scrollPane;
            Component comp;
            if (control instanceof JScrollPane)
            {
                scrollPane = (JScrollPane)control;
                comp = scrollPane.getViewport().getView();
                
            } else
            {
                scrollPane = null;
                comp = control;
            }
            
            String     fullId  = appendName(formCell.getIdent());
            ColumnInfo colInfo = controlsById.get(fullId);
            if (colInfo == null)
            {
                colInfo = new ColumnInfo(getParentClassName(), formCell, fullCompName, comp, scrollPane);
                controlsById.put(fullId, colInfo);
                
            } else
            {
                colInfo.setFullCompName(fullCompName);
                colInfo.setFormCell(formCell);
            }
            
            if (StringUtils.isEmpty(colInfo.getLabel()))
            {
                if (control instanceof JCheckBox)
                {
                    String cbxLabel = ((JCheckBox)control).getText();
                    if (StringUtils.isEmpty(cbxLabel))
                    {
                        cbxLabel = " ";
                    }
                    colInfo.setLabel(cbxLabel);
                    
                } else
                {
                    colInfo.setLabel(" ");
                }
            }
            colInfo.setComp(comp);
            colInfo.setScrollPane(scrollPane);
            columnList.add(colInfo);
            controlsByName.put(fullCompName, colInfo);
        }
        
        //log.info("RegControl["+formCell.getName()+"]");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#registerPlugin(edu.ku.brc.ui.forms.persist.FormCellIFace, edu.ku.brc.af.ui.forms.UIPluginable)
     */
    public void registerPlugin(FormCellIFace formCell, UIPluginable uip)
    {
        // for now we can't do anything with a plugin in the table
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#addControlToUI(java.awt.Component, int, int, int, int)
     */
    public void addControlToUI(Component control, int rowInx, int colInx, int colSpan, int rowSpan)
    {
        //log.info("addControlToUI["+control+"]");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#createSeparator(java.lang.String)
     */
    public Component createSeparator(String title)
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#createRecordIndentifier(java.lang.String, javax.swing.ImageIcon)
     */
    public JComponent createRecordIndentifier(String title, ImageIcon icon)
    {
        // not supported
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#addSubView(edu.ku.brc.ui.forms.persist.FormCell, edu.ku.brc.ui.forms.MultiView, int, int, int, int)
     */
    public void addSubView(FormCellSubView subFormCell, MultiView subView, int colInx, int rowInx, int colSpan, int rowSpan)
    {
        // When we are "flattening" the subforms and creating columns we need to be in the "context of" a subform.
        // so push the current subform onto a stack for our current context.
        subViewStack.push(subFormCell);
        if (fullObjPath.length() > 0)
        {
            fullObjPath.append(".");
        }
        fullObjPath.append(subFormCell.getName());
        
        String                 clsName = getParentClassName();
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(clsName);
        
        if (tblInfo != null)
        {
            DBRelationshipInfo.RelationshipType type = tblInfo.getRelType(subFormCell.getName());
            //String fieldName = subFormCell.getName();
            //log.info(type+"  "+fieldName+" "+clsName);
            
            boolean isSet = type == DBRelationshipInfo.RelationshipType.OneToMany || type == DBRelationshipInfo.RelationshipType.ManyToMany;
            if (isSet)
            {
                skipControls++;
                
                String     fullCompName = subFormCell.getName();//appendName(subFormCell.getName());
                String     fullId       = appendName(subFormCell.getIdent());
                ColumnInfo colInfo      = controlsById.get(fullId);
                if (colInfo == null)
                {
                    colInfo = new ColumnInfo(getParentClassName(), subFormCell, fullCompName, null, null);
                    controlsById.put(fullId, colInfo); 
                }
                columnList.add(colInfo);
                controlsByName.put(fullCompName, colInfo);
            }
            //log.info(isSet);
        }
        //log.info("Add Name["+fullObjPath.toString()+"]");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#closeSubView(edu.ku.brc.ui.forms.persist.FormCellSubView)
     */
    public void closeSubView(FormCellSubView subFormCell)
    {
        subViewStack.pop();
        fullObjPath.setLength(fullObjPath.length()-subFormCell.getName().length());
        if (fullObjPath.length() > 1)
        {
            fullObjPath.setLength(fullObjPath.length()-1);
        }
        //log.info("Done Name["+fullObjPath.toString()+"]");
        skipControls--;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#shouldFlatten()
     */
    public boolean shouldFlatten()
    {
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#getControlByName(java.lang.String)
     */
    public Component getControlByName(final String name)
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#getControlById(java.lang.String)
     */
    public Component getControlById(String id)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#fixUpRequiredDerivedLabels()
     */
    public void fixUpRequiredDerivedLabels()
    {
        // NOTE: The forms can contain object that are not in our data model
        /*DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(formViewDef.getClassName());
        if (ti != null)
        {
            for (ColumnInfo colInfo : controlsById.values())
            {
                String fieldName = colInfo.getFormCell().getName();
                
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
            
                DBTableChildIFace  tblChild = derivedCI != null ? derivedCI : ti.getItemByName(fieldName);
                FormCellLabelIFace formCellLabel = colInfo.getFormCellLabel();
                if (formCellLabel != null)
                {
                    if (formCellLabel.isDerived() && tblChild != null)
                    {
                        String title = tblChild.getTitle();
                        if (StringUtils.isNotEmpty(title))
                        {
                            colInfo.setLabel(title);
                        }
                    }
                } else if (colInfo.getLabel().equals("##"))
                {
                    FormCellIFace formCell = colInfo.getFormCell();
                    if (formCell.getType() == FormCellIFace.CellType.field)
                    {
                        DBFieldInfo fi = ti.getFieldByName(formCell.getName());
                        if (fi != null)
                        {
                            colInfo.setLabel(fi.getTitle());
                        }
                    }
                }
            }
        }*/
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.ViewBuilderIFace#doneBuilding()
     */
    @Override
    public void doneBuilding()
    {
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getSaveBtn()
     */
    public JComponent getSaveComponent()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#isDataCompleteAndValid(boolean)
     */
    public boolean isDataCompleteAndValid(final boolean throwAwayOnDiscard)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setSkippingAttach(boolean)
     */
    @Override
    public void setSkippingAttach(boolean isSkippingAttach)
    {
        this.isSkippingAttach = isSkippingAttach;
    }
    
    //-----------------------------------------------------
    // ValidationListener
    //-----------------------------------------------------


    /* (non-Javadoc)
     * @see ValidationListener#wasValidated(UIValidator)
     */
    public void wasValidated(final UIValidator validator)
    {
        if (validationInfoBtn != null)
        {
            ImageIcon icon = IconManager.getIcon("ValidationValid");
            UIValidatable.ErrorType state = formValidator.getState();

            if (state == UIValidatable.ErrorType.Incomplete)
            {
                icon = IconManager.getIcon("ValidationWarning", IconManager.IconSize.Std16);

            } else if (state == UIValidatable.ErrorType.Error)
            {
                icon = IconManager.getIcon("ValidationError");
            }

            validationInfoBtn.setIcon(icon);
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
        dataObj = dataObjList.get(newIndex);

        setDataIntoUI();

        if (saveBtn != null)
        {
            saveBtn.setEnabled(false);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexAboutToChange(int, int)
     */
    public boolean indexAboutToChange(int oldIndex, int newIndex)
    {
        return false;// XXX checkForChanges();

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#newRecordAdded()
     */
    public void newRecordAdded()
    {
        if (mvParent.getMultiViewParent() != null)
        {
            formValidator.setHasChanged(true);
        }
    }


    //-------------------------------------------------
    // AppPrefsChangeListener
    //-------------------------------------------------

    protected void setColorOnControls(@SuppressWarnings("unused") final int colorType, @SuppressWarnings("unused") final Color color)
    {
        /*
        for (ColumnInfo fieldInfo : controlsById.values())
        {
            if (fieldInfo.getFormCell().getType() == FormCell.CellType.field)
            {
                FormCellField cellField = (FormCellField)fieldInfo.getFormCell();
                String uiType = cellField.getUiType();
                //log.debug("["+uiType+"]");

                // XXX maybe check check to see if it is a JTextField component instead
                if (uiType.equals("dsptextfield") || uiType.equals("dsptextarea"))
                {
                    Component comp = fieldInfo.getComp();
                    switch (colorType)
                    {
                        case 0 : {

                            if (comp instanceof JScrollPane)
                            {
                                ((JScrollPane)comp).getViewport().getView().setBackground(color);
                            } else
                            {
                                fieldInfo.getComp().setBackground(color);
                            }
                        } break;

                        //case 1 : {
                        //    if (comp instanceof )
                        //    //fieldInfo.getComp().setBackground(color);
                        //} break;
                    }
                }
            }
        }*/

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


    //-------------------------------------------------
    // ColumnInfo
    //-------------------------------------------------
    
    
    /**
     * This clas has all the info needed for what defines a column in the table.
     * 
     */
    class ColumnInfo
    {
        protected FormCellIFace      formCell;
        protected FormCellLabelIFace formCellLabel;
        protected String             parentClassName;
        protected String             fullCompName;
        protected String             label;
        protected Component          comp;
        protected JScrollPane        scrollPane;
        protected String[]           fieldNames;
        protected boolean            isSet;
        protected String             dataObjFormatName = null;
        protected boolean            hasDataObjFormatter = false;
        
        protected PickListDBAdapterIFace adaptor = null;

        public ColumnInfo(final String        parentClassName,
                          final FormCellIFace formCell, 
                          final String        fullCompName, 
                          final Component     comp, 
                          final JScrollPane   scrollPane)
        {
            if (formCell instanceof FormCellLabelIFace)
            {
                this.formCell      = null;
                this.formCellLabel = (FormCellLabelIFace)formCell;
                this.label         = ((FormCellLabelIFace)formCell).getLabel();
                
            } else
            {
                this.formCell  = formCell;
                if (formCell instanceof FormCellFieldIFace)
                {
                    label = ((FormCellFieldIFace)formCell).getLabel();
                    
                } else if (formCell instanceof FormCellSubView)
                {
                    FormCellSubView subViewDef = (FormCellSubView)formCell;
                    DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(subViewDef.getClassDesc());
                    if (ti != null)
                    {
                        label = ti.getTitle();
                    } else
                    {
                        ti = DBTableIdMgr.getInstance().getByClassName(parentClassName);
                        if (ti != null)
                        {
                            DBTableChildIFace childInfo = ti.getItemByName(subViewDef.getName());
                            if (childInfo != null)
                            {
                                label = childInfo.getTitle();
                            }
                        }
                    }
                }
                this.formCellLabel = null;
            }
            this.parentClassName = parentClassName;
            this.fullCompName   = fullCompName;
            this.comp           = comp;
            this.scrollPane     = scrollPane;
            this.fieldNames     = split(StringUtils.deleteWhitespace(fullCompName), ".");
            this.isSet          = false;

            checkForDataObjFormatter();
            checkForSet();
        }
        
        protected void checkForDataObjFormatter()
        {
            // Check to see if we have a DataObjFormatter for the Column's Object
            if (formCell instanceof FormCellField)
            {
                FormCellField fcf = (FormCellField)formCell;
                if (fcf.getDspUIType().equals("querycbx") || fcf.getDspUIType().equals("textfieldinfo"))
                {
                    dataObjFormatName = fcf.getProperty("name");
                }
            }
        }

        protected void checkForSet()
        {
            String name = formCell != null ? formCell.getName() : formCellLabel.getName();
            if (StringUtils.isNotEmpty(name))
            {
                DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(parentClassName);
                if (tblInfo != null)
                {
                    DBRelationshipInfo.RelationshipType type = tblInfo.getRelType(formCell.getName());
                    isSet = type == DBRelationshipInfo.RelationshipType.ManyToMany || type == DBRelationshipInfo.RelationshipType.ManyToOne;
                }
            }
        }

        public String[] getFieldNames()
        {
            return fieldNames;
        }

        public String getFullCompName()
        {
            return fullCompName;
        }
        
        public void setFullCompName(String fullCompName)
        {
            this.fullCompName = fullCompName;
            fieldNames = split(StringUtils.deleteWhitespace(fullCompName), ".");
        }

        public String getLabel()
        {
            return label;
        }

        public void setLabel(String label)
        {
            this.label = label;
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
        
        public void setComp(Component comp)
        {
            this.comp = comp;
        }

        public void setScrollPane(JScrollPane scrollPane)
        {
            this.scrollPane = scrollPane;
        }

        public FormCellIFace getFormCell()
        {
            return formCell;
        }

        public void setFormCell(final FormCellIFace formCell)
        {
            if (formCell instanceof FormCellLabelIFace)
            {
                this.formCellLabel = (FormCellLabelIFace)formCell;
            } else
            {
                this.formCell = formCell;
            }
            checkForDataObjFormatter();
            checkForSet();
        }

        /**
         * @return the formCellLabel
         */
        public FormCellLabelIFace getFormCellLabel()
        {
            return formCellLabel;
        }

        public String getDataObjFormatName()
        {
            return dataObjFormatName;
        }

        public void setEnabled(boolean enabled)
        {
            //log.debug(formCell.getName()+"  "+(scrollPane != null ? "has Pane" : "no pane"));
            comp.setEnabled(enabled);
            if (scrollPane != null)
            {
                scrollPane.setEnabled(enabled);
            }
        }

        public boolean hasDataObjFormatter()
        {
            return hasDataObjFormatter;
        }

        public void setHasDataObjFormatter(boolean hasDataObjFormatter)
        {
            this.hasDataObjFormatter = hasDataObjFormatter;
        }

        public PickListDBAdapterIFace getAdaptor()
        {
            return adaptor;
        }

        public void setAdaptor(PickListDBAdapterIFace adaptor)
        {
            this.adaptor = adaptor;
        }

        /**
         * Tells it to clean up
         */
        public void shutdown()
        {
            if (comp instanceof UIValidatable)
            {
                ((UIValidatable)comp).cleanUp();
            }
            formCell   = null;
            comp       = null;
            scrollPane = null;
        }
    }
    
    //------------------------------------------------------------------
    //-- Table Model
    //------------------------------------------------------------------
    public class ColTableModel extends DefaultTableModel
    {
        public ColTableModel()
        {
        }

        public int getColumnCount()
        {
            return columnList.size();
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public String getColumnName(int column)
        {
            if (columnList != null)
            {
                String label = columnList.get(column).getLabel();
                return label != null ? label : "xx";
            }
            log.error("columnList should not be null!");
            return "N/A";
        }

        public int getRowCount()
        {
            if (isLoading || (isRestricted != null && isRestricted))
            {
                return 1;
            }
            return dataObjList == null ? 0 : dataObjList.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int row, int column)
        {
            //log.debug(row+","+column+"  isLoaded:"+isLoaded);
            
            if (isLoading)
            {
                return column == 0 && row == 0 ? UIRegistry.getResourceBundle("LOADING") : "";
                
            } else if (isRestricted != null && isRestricted)
            {
                return restrictedStr;
            }

            if (columnList != null && dataObjList != null && dataObjList.size() > 0)
            {
                if (!isLoaded)
                {
                    setDataIntoUI();
                }
                
                ColumnInfo colInfo = columnList.get(column);
                Object     rowObj  = dataObjList.get(row);
                //log.info("["+colInfo.getFullCompName()+"]");
                
                if (session != null && session.isOpen() && rowObj instanceof FormDataObjIFace)
                {
                    session.attach(rowObj);
                }

                Object[] dataValues = UIHelper.getFieldValues(new String[] {colInfo.getFullCompName()}, rowObj, dataGetter);
                if (dataValues == null || dataValues[0] == null)
                {
                    return null;
                }
                
                Object dataVal           = dataValues[0];
                String dataObjFormatName = colInfo.getDataObjFormatName();
                if (StringUtils.isNotEmpty(dataObjFormatName))
                {
                    return DataObjFieldFormatMgr.getInstance().format(dataVal, dataObjFormatName);
                    
                } else if (dataVal instanceof FormDataObjIFace)
                {

                    FormDataObjIFace formObj = (FormDataObjIFace)dataVal;
                    Object val = DataObjFieldFormatMgr.getInstance().format(dataVal, formObj.getDataClass());
                    if (val != null)
                    {
                        return val;
                    }
                }
                
                if (dataVal instanceof Set)
                {
                    Set<?> objSet = (Set<?>)dataVal;
                    if (objSet.size() > 0)
                    {
                        return DataObjFieldFormatMgr.getInstance().aggregate(objSet, objSet.iterator().next().getClass());
                    }
                    return "";
                    
                }
                
                if (colInfo.getFormCell() instanceof FormCellFieldIFace)
                {
                    FormCellFieldIFace fcf = (FormCellFieldIFace)colInfo.getFormCell();
                    String uiFmt = fcf.getUIFieldFormatterName();
                    if (fcf.getDspUIType() == FormCellFieldIFace.FieldType.textpl)
                    {
                        PickListDBAdapterIFace adapter = colInfo.getAdaptor();
                        if (adapter == null)
                        {
                            String pickListName = fcf.getPickListName();
                            if (isNotEmpty(pickListName))
                            {
                                adapter = PickListDBAdapterFactory.getInstance().create(pickListName, false);
                                
                                if (adapter == null || adapter.getPickList() == null)
                                {
                                    throw new RuntimeException("PickList Adapter ["+pickListName+"] cannot be null!");
                                }
                                colInfo.setAdaptor(adapter);
                            }
                        }
                        return getPickListValue(adapter, dataVal);
                        
                    } else if (StringUtils.isNotEmpty(uiFmt))
                    {
                        UIFieldFormatterIFace fmt = UIFieldFormatterMgr.getInstance().getFormatter(uiFmt);
                        if (fmt != null)
                        {
                            dataVal = fmt.formatToUI(dataVal);
                        }
                    }
                }
                return dataVal instanceof Boolean ? getResourceString(dataVal.toString()) : dataVal;
            }
            return null;
        }
        
        /**
         * @param adapter
         * @param value
         * @return
         */
        protected Object getPickListValue(final PickListDBAdapterIFace adapter, final Object value)
        {
            if (value != null && adapter != null)
            {
                if (adapter.isTabledBased())
                {
                    String data = null;
        
                    boolean                   isFormObjIFace = value instanceof FormDataObjIFace;
                    Vector<PickListItemIFace> items          = adapter.getList();
                    
                    for (int i=0;i<items.size();i++)
                    {
                        PickListItemIFace pli    = items.get(i);
                        Object       valObj = pli.getValueObject();
                        
                        if (valObj != null)
                        {
                            if (isFormObjIFace && valObj instanceof FormDataObjIFace)
                            {
                                if (((FormDataObjIFace)value).getId().intValue() == (((FormDataObjIFace)valObj).getId().intValue()))
                                {
                                    data = pli.getTitle();
                                    break;                                
                                }
                            } else if (pli.getValue().equals(value.toString()))
                            {
                                data = pli.getTitle();
                                break;                            
                            }
                        }
                    } 
                    
                    if (data == null)
                    {
                        data = "";
                    }
                    
                    return data;
                }

                for (PickListItemIFace item : adapter.getList())
                {
                    if (item.getValue().equals(value.toString()))
                    {
                        return item.getTitle();
                    }
                }
            }
            return "";
        }

        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            if (isRestricted != null && isRestricted)
            {
                return String.class;
            }
            
            Object obj = getValueAt(0, columnIndex);
            if (obj != null)
            {
                return obj.getClass();
            }
            // else
            return String.class;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex)
        {
        }
        
        public void fireDataChanged()
        {
            fireTableDataChanged();
        }
    }
    
    
    class MyTableCellEditor  extends AbstractCellEditor  implements TableCellEditor
    {
        protected Component        comp;
        protected GetSetValueIFace compGetSet;
        
        public MyTableCellEditor(final ColumnInfo colInfo)
        {
            this.comp       = colInfo.getComp();
            this.compGetSet = (GetSetValueIFace)comp;
        }

        //
        //          Override the implementations of the superclass, forwarding all methods 
        //          from the CellEditor interface to our delegate. 
        //

        /**
         * Forwards the message from the <code>CellEditor</code> to
         * the <code>delegate</code>.
         */
        public Object getCellEditorValue() 
        {
            return compGetSet.getValue();
        }

        /**
         * Forwards the message from the <code>CellEditor</code> to
         * the <code>delegate</code>.
         */
        @Override
        public boolean isCellEditable(EventObject anEvent) 
        { 
            return true; 
        }
        
        //
        //          Implementing the CellEditor Interface
        //
        /** Implements the <code>TableCellEditor</code> interface. */
        public Component getTableCellEditorComponent(JTable tbl, 
                                                     Object value,
                                                     boolean isSelected,
                                                     int row, 
                                                     int column)
        {
            compGetSet.setValue(value, null);
            return comp;
        }
    }
    
    // This class is being used to try to find an exception during the
    // the display of the table (CollectingEvent Collectors)
    /*
    class TestTable extends JTable
    {

        public TestTable(TableModel dm)
        {
            super(dm);
        }

        @Override
        public Object getValueAt(int row, int column)
        {
            //log.debug("row,col "+row+", "+column);
            try
            {
                return super.getValueAt(row, column);
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TableViewObj.class, ex);
                log.debug("row,col "+row+", "+column);
                ex.printStackTrace();
            }
            return null;
        }

    }*/

}
