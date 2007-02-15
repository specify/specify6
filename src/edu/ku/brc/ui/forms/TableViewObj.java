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
package edu.ku.brc.ui.forms;

import static edu.ku.brc.ui.UICacheManager.getResourceString;
import static org.apache.commons.lang.StringUtils.split;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.FormCell;
import edu.ku.brc.ui.forms.persist.FormCellField;
import edu.ku.brc.ui.forms.persist.FormCellLabel;
import edu.ku.brc.ui.forms.persist.FormCellSubView;
import edu.ku.brc.ui.forms.persist.FormViewDef;
import edu.ku.brc.ui.forms.persist.TableViewDef;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.forms.persist.ViewDef;
import edu.ku.brc.ui.validation.FormValidator;
import edu.ku.brc.ui.validation.UIValidatable;
import edu.ku.brc.ui.validation.UIValidator;
import edu.ku.brc.ui.validation.ValidationListener;

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
    protected boolean                       isEditting     = false;
    protected boolean                       formIsInNewDataMode = false; // when this is true it means the form was cleared and new data is expected
    protected MultiView                     mvParent       = null;
    protected View                          view;
    protected AltView                       altView;
    protected ViewDef                       viewDef;
    protected FormViewDef                   formViewDef;
    protected int                           options;
    protected String                        cellName       = null;
    protected Component                     formComp       = null;
    protected List<MultiView>               kids           = new ArrayList<MultiView>();
    protected Vector<AltView>               altViewsList   = null;
    protected TableViewDef                  tableViewDef;
    protected DataObjectGettable            dataGetter      = null;
    
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
    protected List<Object>                  dataObjList     = null;
    protected Object[]                      singleItemArray = new Object[1];
    protected DateWrapper                   scrDateFormat;
    protected boolean                       isLoaded        = false;
    
    protected String                        dataClassName;
    protected String                        dataSetFieldName;

    protected JPanel                        mainComp        = null;
    protected ControlBarPanel               controlPanel    = null;
    protected ResultSetController           rsController    = null;
    protected List<Object>                  list            = null;
    protected boolean                       ignoreSelection = false;
    protected JButton                       saveBtn         = null;
    protected JButton                       validationInfoBtn = null;
    protected boolean                       wasNull         = false;
    protected MenuSwitcherPanel             switcherUI;
    protected JComboBox                     selectorCBX     = null;
    protected int                           mainCompRowInx  = 1;
    
    protected JButton                       editButton      = null;
    protected JButton                       newButton       = null;
    protected JButton                       deleteButton    = null;

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

    /**
     * Constructor with FormView definition.<NOTE: We cannot build the table here because we need all the column
     * information.
     * @param view the view 
     * @param altView the altview
     * @param mvParent the parent
     * @param formValidator the validator
     * @param options the creation options
     */
    public TableViewObj(final View          view,
                        final AltView       altView,
                        final MultiView     mvParent,
                        final FormValidator formValidator,
                        final int           options)
    {
        this.view        = view;
        this.altView     = altView;
        this.mvParent    = mvParent;
        this.options     = options;
        this.viewDef     = altView.getViewDef();
        this.formValidator = formValidator;
        
        businessRules    = view.getBusinessRule();
        dataGetter       = altView.getViewDef().getDataGettable();
        this.formViewDef = (FormViewDef)altView.getViewDef();
        
        MultiView.printCreateOptions("Creating Form "+altView.getName(), options);


        scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");


        AppPreferences.getRemote().addChangeListener("ui.formatting.viewfieldcolor", this);

        boolean createViewSwitcher         = MultiView.isOptionOn(options, MultiView.VIEW_SWITCHER);
        boolean hideSaveBtn                = MultiView.isOptionOn(options, MultiView.HIDE_SAVE_BTN);
        isEditting                         = MultiView.isOptionOn(options, MultiView.IS_EDITTING);
        
        setValidator(formValidator);

        scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");


        AppPreferences.getRemote().addChangeListener("ui.formatting.viewfieldcolor", this);

        boolean addController = mvParent != null && view.getAltViews().size() > 1;

        mainComp = new JPanel(new BorderLayout());
        
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

                altViewsList = new Vector<AltView>();
                
                // Very Special Case until Tables can handle edit mode or be in edit mode.
                // Since they can't, and when a form is for a "new" object then we need to fake out the switcher code 
                // so it thinks we are in edit mode (at this time Tables are ALWAYS in View mode)
                // so we temporarily set the mode of the Table's AltView to Edit create the switcher
                // and then set it back to View.
                boolean overrideViewMode = MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT) || MultiView.isOptionOn(options, MultiView.IS_EDITTING);
                
                AltView.CreationMode tempMode = null;
                //if (isNewObj && altView.getMode() == AltView.CreationMode.View)
                if (overrideViewMode && altView.getMode() == AltView.CreationMode.View)
                                       {
                    tempMode = altView.getMode();
                    altView.setMode(AltView.CreationMode.Edit);
                }
                
                switcherUI = FormViewObj.createMenuSwitcherPanel(mvParent, view, altView, altViewsList);
                
                if (tempMode != null)
                {
                    altView.setMode(tempMode);
                }
                
                if (altViewsList.size() > 0)
                {
                    if (isEditting)
                    {
                        editButton   = UIHelper.createButton("EditForm", getResourceString("EditRecord"), IconManager.IconSize.Std16, true);
                        newButton    = UIHelper.createButton("CreateObj", getResourceString("NewRecord"), IconManager.IconSize.Std16, true);
                        deleteButton = UIHelper.createButton("SmallTrash", getResourceString("DeleteRecord"), IconManager.IconSize.Std16, true);
                        
                        editButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e)
                            {
                                editRow(table.getSelectedRow(), isEditting, false);
                            }
                        });
                        
                        newButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e)
                            {
                                editRow(table.getSelectedRow(), isEditting, true);
                            }
                        });
                        
                        deleteButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e)
                            {
                                deleteRow(table.getSelectedRow());
                            }
                        });
                        
                        PanelBuilder builder = new PanelBuilder(new FormLayout("f:1px:g,p,1px,p,1px,p,1px,p,1px,p", "p"));
                        builder.add(editButton, cc.xy(2,1));
                        builder.add(newButton, cc.xy(4,1));
                        builder.add(deleteButton, cc.xy(6,1));
        
                        
                        comps.add(builder.getPanel());

                        if (saveBtn != null)
                        {
                            // We want it on the left side of other buttons
                            // so wee need to add it before the Save button
                            //addValidationIndicator(comps);
        
                            //addSaveBtn();
    
                            comps.add(saveBtn);
                            saveWasAdded = true;
                        }
                    } else
                    {
                         editButton = UIHelper.createButton("ViewForm", getResourceString("ViewRecord"), IconManager.IconSize.Std16, true);
                         editButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e)
                            {
                                editRow(table.getSelectedRow(), isEditting, false);
                            }
                        });
                         PanelBuilder builder = new PanelBuilder(new FormLayout("f:1px:g,p,10px", "p"));
                         builder.add(editButton, cc.xy(2,1));
                         comps.add(builder.getPanel());
   
                    }
                    updateUI(false);
                    comps.add(switcherUI);
                }
            }
            
            if (!saveWasAdded && altView.getMode() == AltView.CreationMode.Edit)
            {
                if (mvParent != null && mvParent.isTopLevel() && !hideSaveBtn)
                {
                    //addSaveBtn();
                    comps.add(saveBtn);
                }
                JComponent valInfoBtn = FormViewObj.createValidationIndicator(this);
                if (valInfoBtn != null)
                {
                    comps.add(valInfoBtn);
                }
            }
        }

        if (comps.size() > 0 || addController)
        {
            controlPanel = new ControlBarPanel();
            controlPanel.addComponents(comps, false); // false -> right side
            //mainBuilder.add(controlPanel, cc.xy(1, mainCompRowInx+2));
            mainComp.add(controlPanel, BorderLayout.SOUTH);
        }
    }
    
    /**
     * Sizes the table to number of rows using getRowHeight
     * @param table the table to be sized
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
     * @param table the table to be sized
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
        
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);

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
        model    = new ColTableModel();
        table    = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setFocusable(false);
        
        configColumns();
        
        //table.setCellSelectionEnabled(false);
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                
                ListSelectionModel lsm          = (ListSelectionModel)e.getSource();
                updateUI(!lsm.isSelectionEmpty());
                
            }
        });
            
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if ( e.getClickCount() == 2 )
                {
                    int index = table.getSelectedRow();
                    editRow(index, isEditting, false);
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

        tableScroller = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //mainBuilder.add(tableScroller, cc.xy(1, mainCompRowInx));
        mainComp.add(tableScroller, BorderLayout.CENTER);
        
        initColumnSizes(table);

    }
    
    /**
     * Sest the btn enabled/disabled state.
     * @param hasSelection whether something is selected.
     */
    protected void updateUI(final boolean hasSelection)
    {
        if (editButton != null)
        {
            editButton.setEnabled(hasSelection);
        }
        if (newButton != null)
        {
            newButton.setEnabled(true);
        }
        if (deleteButton != null)
        {
            deleteButton.setEnabled(hasSelection);
        }
    }
    
    /**
     * Tells MV and UI that a change has been made. 
     */
    protected void tellMultiViewOfChange()
    {
        if (mvParent != null)
        {
            MultiView root = mvParent;
            while (root.getMultiViewParent() != null)
            {
                root = root.getMultiViewParent();
            }
            formValidator.setHasChanged(true);
            root.dataChanged(null, null, null);
        }
    }
    
    /**
     * Can create a new item or edit an existing it; or view and existing item.
     * @param rowIndex the index tho be editted
     * @param isEdit whether we are editing or view
     * @param isNew hwther the object is new
     */
    protected void editRow(final int rowIndex, final boolean isEdit, final boolean isNew)
    {
        FormDataObjIFace dObj = null;
        if (isNew)
        {
            dObj = FormHelper.createAndNewDataObj(dataClassName);
            
        } else
        {
            dObj = (FormDataObjIFace)dataObjList.get(rowIndex);
            if (dObj == null)
            {
                return;
            }
        }
        
        final ViewBasedDisplayIFace dialog = UIHelper.createDataObjectDialog(altView, mainComp, dObj, MultiView.isOptionOn(options, MultiView.IS_EDITTING), false);
        if (dialog != null)
        {
            if (isEdit)
            {
                dialog.setCloseListener(new PropertyChangeListener()
                {
                    @SuppressWarnings("unchecked")
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        String action = evt.getPropertyName();
                        if (action.equals("OK"))
                        {
                            dialog.getMultiView().getDataFromUI();
                            if (mvParent != null)
                            {
                                tellMultiViewOfChange();
                                
                                Object daObj = dialog.getMultiView().getData();
                                parentDataObj.addReference((FormDataObjIFace)daObj, dataSetFieldName);
                                if (isNew)
                                {
                                    dataObjList.add(daObj);
                                    
                                    Collections.sort((List)dataObjList);                          
                                    if (origDataSet != null)
                                    {
                                        origDataSet.add(daObj);
                                    }
                                }
                                model.fireDataChanged();
                                table.invalidate();
                                
                                JComponent comp = mvParent.getTopLevel();
                                comp.validate();
                                comp.repaint();
                            }
        
                        }
                        else if (action.equals("Cancel"))
                        {
                            log.warn("User clicked Cancel");
                        }
                    }
                });
            }
            dialog.setData(dObj);
            dialog.showDisplay(true);
        }
    }
    
    /**
     * Delets a Row in the table and there an data object
     * @param rowIndex the item to be deleted
     */
    protected void deleteRow(final int rowIndex)
    {
        FormDataObjIFace dObj = (FormDataObjIFace)dataObjList.get(rowIndex);
        if (dObj == null)
        {
            return;
        }
        parentDataObj.removeReference(dObj, dataSetFieldName);
        dataObjList.remove(rowIndex);
        
        model.fireDataChanged();
        table.invalidate();
        
        JComponent comp = mvParent.getTopLevel();
        comp.validate();
        comp.repaint();
        
        tellMultiViewOfChange();
        
        // Delete a child object by caching it in the Top Level MultiView
        if (mvParent != null && !mvParent.isTopLevel())
        {
            mvParent.getTopLevel().addDeletedItem(dObj);
            String delMsg = (businessRules != null) ? businessRules.getDeleteMsg(dObj) : "";
            ((JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR)).setText(delMsg);

            return;
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
     * @see edu.ku.brc.ui.forms.Viewable#getName()
     */
    public String getName()
    {
        return "Table Viewer for Forms"; // this is not retrieved
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getId()
     */
    public int getId()
    {
        return -1; // I think is needed for DnD of the current row, which will be implemented later.
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getType()
     */
    public ViewDef.ViewType getType()
    {
        return tableViewDef.getType();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getUIComponent()
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
     * @see edu.ku.brc.ui.forms.Viewable#isSubform()
     */
    public boolean isSubform()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getCompById(java.lang.String)
     */
    public Component getCompById(final String id)
    {
        return null; //  Not applicable
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getLabelById(java.lang.String)
     */
    public JLabel getLabelFor(final String id)
    {
        return null; //  Not applicable
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getControlMapping()
     */
    public Map<String, Component> getControlMapping()
    {
        return null; //  Not applicable
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getValidator()
     */
    public FormValidator getValidator()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataObj(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void setDataObj(final Object dataObj)
    {
        if (dataObj instanceof List)
        {
            origDataSet = null;
            dataObjList = (List<Object>)dataObj; 
            
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
                    if (newList.get(0) instanceof Comparable<?>)
                    {
                        Collections.sort(newList);
                    }
                }
                dataObjList.addAll(newList);

                
            } else if (dataObj instanceof RecordSetIFace)
            {
                this.dataObj = dataObj;
                /*
                RecordSetIFace recordSet = (RecordSetIFace)dataObj;
                
                DBTableIdMgr.getInClause(recordSet);
                DBTableIdMgr.TableInfo tableInfo = DBTableIdMgr.lookupInfoById(recordSet.getDbTableId());
                
                DataProviderFactory.getInstance().evict(tableInfo.getClassObj());
                
                //DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                
                String sqlStr = DBTableIdMgr.getQueryForTable(recordSet);
                if (StringUtils.isNotBlank(sqlStr))
                {
                    dataObjList =(List<Object>)session.getDataList(sqlStr);
                }
*/
            } else
            {
                // single object
                dataObjList.add(dataObj);
            }
        }
        
        setDataIntoUI();
        
        if (table != null)
        {
            table.tableChanged(new TableModelEvent(model));
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setRecordSet(edu.ku.brc.dbsupport.RecordSetIFace)
     */
    public void setRecordSet(RecordSetIFace recordSet)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataObj()
     */
    public Object getDataObj()
    {
        return dataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setParentDataObj(java.lang.Object)
     */
    public void setParentDataObj(Object parentDataObj)
    {
        this.parentDataObj = (FormDataObjIFace)parentDataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getParentDataObj()
     */
    public Object getParentDataObj()
    {
        return parentDataObj;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataIntoUI()
     */
    public void setDataIntoUI()
    {
        if (dataObjList != null && model != null)
        {
            DataProviderSessionIFace tmpSession = session;
            if (tmpSession == null)
            {
                tmpSession = DataProviderFactory.getInstance().createSession();
                for (Object dObj : dataObjList)
                {
                    tmpSession.attach(dObj);
                }
            }
            isLoaded = true;
    
            for (int i=0;i<dataObjList.size();i++)
            {
                for (int j=0;j<model.getColumnCount();j++)
                {
                    model.getValueAt(i, j);
                }
            }
            
            if (session == null && tmpSession != null)
            {
                tmpSession.close();
                
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataFromUI()
     */
    public void getDataFromUI()
    {
        // Not applicable
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataFromUIComp(java.lang.String)
     */
    public Object getDataFromUIComp(final String name)
    {
        return null; // Not applicable
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataIntoUIComp(java.lang.String, java.lang.Object)
     */
    public void setDataIntoUIComp(final String name, Object data)
    {
        // Not applicable
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getSubView(java.lang.String)
     */
    public MultiView getSubView(final String name)
    {
        return null; // Not applicable
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getFieldIds(java.util.List)
     */
    public void getFieldIds(final List<String> fieldIds)
    {
        // Not applicable
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#aboutToShow(boolean)
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
            
            FormViewObj.addRemoveWithRootMV(mvParent, formValidator, show);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getView()
     */
    public View getView()
    {
        return view;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getViewDef()
     */
    public FormViewDef getViewDef()
    {
        return (FormViewDef)altView.getViewDef();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getAltView()
     */
    public AltView getAltView()
    {
        return altView;
    }
    
    /* (non
     * -Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#hideMultiViewSwitch(boolean)
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
     * @see edu.ku.brc.ui.forms.Viewable#dataHasChanged()
     */
    public void validationWasOK(boolean wasOK)
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setSession(org.hibernate.Session)
     */
    @SuppressWarnings("unchecked")
    public void setSession(final DataProviderSessionIFace session)
    {
        this.session = session;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setCellName(java.lang.String)
     */
    public void setCellName(String cellName)
    {
        this.cellName = cellName;
        this.dataSetFieldName = cellName;
        
        if (parentDataObj == null)
        {
            this.dataClassName = viewDef.getClassName();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setHasNewData(boolean)
     */
    public void setHasNewData(final boolean isNewForm)
    {
        // this gives you the opportunity to adjust your UI
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#registerSaveBtn(javax.swing.JButton)
     */
    public void registerSaveBtn(JButton saveBtnArg)
    {
        this.saveBtn = saveBtnArg;
    }  
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#updateSaveBtn()
     */
    public void updateSaveBtn()
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#focus()
     */
    public void focus()
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#shutdown()
     */
    public void shutdown()
    {
        //if (mvParent != null)
        //{
        //    mvParent.shutdown();
        //    mvParent = null;
        //}
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
     * @param formCell the FormCell def that describe the cell
     * @param label the the label to be added
     */

    public void addLabel(final FormCellLabel formCell, final JLabel label)
    {
        if (skipControls > 0)
        {
            return;
        }
        if (formCell != null && StringUtils.isNotEmpty(formCell.getLabelFor()))
        {
            String fullCompName = appendName(formCell.getLabelFor());
            ColumnInfo colInfo = controlsById.get(fullCompName);
            if (colInfo == null)
            {
                colInfo = new ColumnInfo(getParentClassName(), formCell, fullCompName, null, null);
                controlsById.put(fullCompName, colInfo);
            }
            colInfo.setLabel(formCell.getLabel());
            
        }
     }
    
    /**
     * Gets the current class name for the stack context.
     * @return the parent object class name
     */
    protected String getParentClassName()
    {
        String className = null;
        FormCellSubView formSubView = subViewStack.size() > 0 ? subViewStack.peek() : null;
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
    public void registerControl(final FormCell formCell, final Component control)
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
            
            String     fullId  = appendName(formCell.getId());
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
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#addControlToUI(java.awt.Component, int, int, int, int)
     */
    public void addControlToUI(Component control, int rowInx, int colInx, int colSpan, int rowSpan)
    {
        if (control == null)
        {
            int x = 0;
            x++;
        }
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
        DBTableIdMgr.TableInfo tblInfo = DBTableIdMgr.getByClassName(clsName);
        
        if (tblInfo != null)
        {
            DBTableIdMgr.RelationshipType type = tblInfo.getRelType(subFormCell.getName());
            //String fieldName = subFormCell.getName();
            //log.info(type+"  "+fieldName+" "+clsName);
            
            boolean isSet = type == DBTableIdMgr.RelationshipType.OneToMany || type == DBTableIdMgr.RelationshipType.ManyToMany;
            if (isSet)
            {
                skipControls++;
                
                String     fullCompName = subFormCell.getName();//appendName(subFormCell.getName());
                String     fullId       = appendName(subFormCell.getId());
                ColumnInfo colInfo      = controlsById.get(fullId);
                if (colInfo == null)
                {
                    colInfo = new ColumnInfo(getParentClassName(), subFormCell, fullCompName, null, null);
                    colInfo.setLabel(subFormCell.getDescription());
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
                icon = IconManager.getIcon("ValidationWarning");

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

    protected void setColorOnControls(final int colorType, final Color color)
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
        protected FormCell    formCell;
        protected String      parentClassName;
        protected String      fullCompName;
        protected String      label;
        protected Component   comp;
        protected JScrollPane scrollPane;
        protected String[]    fieldNames;
        protected boolean     isSet;
        protected String      dataObjFormatName = null;
        protected boolean     hasDataObjFormatter = false;

        public ColumnInfo(String       parentClassName,
                          FormCell     formCell, 
                          String       fullCompName, 
                          Component    comp, 
                          JScrollPane  scrollPane)
        {
            if (parentClassName == null)
            {
                int x = 0;
                x++;
            }
            this.formCell       = formCell;
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
            if (StringUtils.isNotEmpty(formCell.getName()))
            {
                DBTableIdMgr.TableInfo tblInfo = DBTableIdMgr.getByClassName(parentClassName);
                if (tblInfo != null)
                {
                    DBTableIdMgr.RelationshipType type = tblInfo.getRelType(formCell.getName());
                    isSet = type == DBTableIdMgr.RelationshipType.ManyToMany || type == DBTableIdMgr.RelationshipType.ManyToOne;
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
            return formCell.getId();
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

        public FormCell getFormCell()
        {
            return formCell;
        }

        public void setFormCell(FormCell formCell)
        {
            this.formCell = formCell;
            checkForDataObjFormatter();
            checkForSet();
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
    public class ColTableModel extends AbstractTableModel
    {
        protected Vector<TableModelListener> listeners = new Vector<TableModelListener>();

        /**
         * @param rowData
         */
        public ColTableModel()
        {
        }

        public int getColumnCount()
        {
            return columnList.size();
        }

        public String getColumnName(int column)
        {
            if (columnList != null)
            {
                String label = columnList.get(column).getLabel();
                return label != null ? label : "";
            }
            log.error("columnList should not be null!");
            return "N/A";
        }

        public int getRowCount()
        {
            return dataObjList == null ? 0 : dataObjList.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int row, int column)
        {
            if (columnList != null && dataObjList != null && dataObjList.size() > 0)
            {
                if (!isLoaded)
                {
                    setDataIntoUI();
                }
                
                ColumnInfo colInfo = columnList.get(column);
                Object     rowObj  = dataObjList.get(row);
                //log.info("["+colInfo.getFullCompName()+"]");
                
                if (session != null)
                {
                    session.attach(rowObj);
                }
                

                /*
                String[] fName      = new String[1];
                String[] fieldNames = colInfo.getFieldNames();
                for (String fldName : fieldNames)
                {
                    fName[0] = fldName;
                    Object[] dataValues = UIHelper.getFieldValues(fName, rowObj, dataGetter);
                    if (dataValues != null && dataValues[0] instanceof Set)
                    {
                       int x = 0;
                       x++;
                    }
                }*/
                
                Object[] dataValues = UIHelper.getFieldValues(new String[] {colInfo.getFullCompName()}, rowObj, dataGetter);
                if (dataValues == null || dataValues[0] == null)
                {
                    return null;
                }
                
                Object dataVal = dataValues[0];
                
                if (dataVal instanceof Agent)
                {
                    int x = 0;
                    x++;
                }
                
                if (dataVal instanceof Preparation)
                {
                    int x = 0;
                    x++;
                }
                String dataObjFormatName = colInfo.getDataObjFormatName();
                if (StringUtils.isNotEmpty(dataObjFormatName))
                {
                    return DataObjFieldFormatMgr.format(dataVal, dataObjFormatName);
                    
                } else if (dataVal instanceof FormDataObjIFace)
                {

                    FormDataObjIFace formObj = (FormDataObjIFace)dataVal;
                    Object val = DataObjFieldFormatMgr.format(dataVal, formObj.getDataClass());
                    if (val != null)
                    {
                        return val;
                    }
                }
                
                if (dataVal instanceof Set)
                {
                    Set<?> objSet = (Set)dataVal;
                    return DataObjFieldFormatMgr.aggregate(objSet, objSet.iterator().next().getClass());
                    
                } else
                {
                    //log.error("No formatter for ["+dataVal+"]");
                }
                
                return dataVal;

                
                //return dataGetter.getFieldValue(rowObj, colInfo.getFullCompName());
            }
            return null;
        }

        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            Object obj = getValueAt(0, columnIndex);
            if (obj != null)
            {
                return obj.getClass();
                
            } else
            {
                return String.class;
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            return;
        }

        public void addTableModelListener(TableModelListener l)
        {
            listeners.add(l);
        }

        public void removeTableModelListener(TableModelListener l)
        {
            listeners.remove(l);
        }
        
        public void fireDataChanged()
        {
            this.fireTableDataChanged();
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
}
