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

import static org.apache.commons.lang.StringUtils.split;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.DropDownButtonStateful;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
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
 * @code_status Alpha
 **
 * @author rods
 *
 */
public class TableViewObj implements Viewable, 
                                     ViewBuilderIFace, 
                                     ValidationListener, 
                                     ResultSetControllerListener, 
                                     AppPrefsChangeListener
{
    protected static final Logger log = Logger.getLogger(TableViewObj.class);
    
    protected static CellConstraints        cc              = new CellConstraints();

    // Data Members
    protected Session                       session        = null;
    protected boolean                       isEditting     = false;
    protected boolean                       formIsInNewDataMode = false; // when this is true it means the form was cleared and new data is expected
    protected MultiView                     mvParent       = null;
    protected View                          view;
    protected AltView                       altView;
    protected FormViewDef                   formViewDef;
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
    
    //protected FormLayout                    formLayout;
    //protected PanelBuilder                  builder;
    
    protected FormValidator                 formValidator   = null;
    protected Object                        parentDataObj   = null;
    protected Object                        dataObj         = null;
    protected Set<Object>                   origDataSet     = null;
    protected List<Object>                  dataObjList     = null;
    protected Object[]                      singleItemArray = new Object[1];
    protected DateWrapper                   scrDateFormat;

    protected JPanel                        mainComp        = null;
    protected ControlBarPanel               controlPanel    = null;
    protected ResultSetController           rsController    = null;
    protected List<Object>                  list            = null;
    protected boolean                       ignoreSelection = false;
    protected JButton                       saveBtn         = null;
    protected JButton                       validationInfoBtn = null;
    protected boolean                       wasNull         = false;
    protected DropDownButtonStateful        switcherUI;
    protected JComboBox                     selectorCBX     = null;
    protected int                           mainCompRowInx  = 1;

    //protected PanelBuilder                  mainBuilder;
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
     * Constructor with FormView definition
     * @param tableViewDef the definition of the form
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
        
        businessRules    = view.getBusinessRule();
        dataGetter       = altView.getViewDef().getDataGettable();
        this.formViewDef = (FormViewDef)altView.getViewDef();
        
        /*
        boolean createResultSetController  = MultiView.isOptionOn(options, MultiView.RESULTSET_CONTROLLER);
        boolean createViewSwitcher         = MultiView.isOptionOn(options, MultiView.VIEW_SWITCHER);
        boolean isNewObject                = MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT);
        boolean hideSaveBtn                = MultiView.isOptionOn(options, MultiView.HIDE_SAVE_BTN);
        */
        
        MultiView.printCreateOptions("Creating Form "+altView.getName(), options);

        // XXX setValidator(formValidator);

        scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");


        AppPreferences.getRemote().addChangeListener("ui.formatting.viewfieldcolor", this);

        // Figure columns
        //formLayout = new FormLayout(formViewDef.getColumnDef(), formViewDef.getRowDef());
        //builder    = new PanelBuilder(formLayout);

        //boolean createResultSetController  = MultiView.isOptionOn(options, MultiView.RESULTSET_CONTROLLER);
        boolean createViewSwitcher         = MultiView.isOptionOn(options, MultiView.VIEW_SWITCHER);
        //boolean isNewObject                = MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT);
        boolean hideSaveBtn                = MultiView.isOptionOn(options, MultiView.HIDE_SAVE_BTN);
        
        MultiView.printCreateOptions("Creating Form "+altView.getName(), options);

        //setValidator(formValidator);

        scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");


        AppPreferences.getRemote().addChangeListener("ui.formatting.viewfieldcolor", this);

        boolean addController = mvParent != null && view.getAltViews().size() > 1;


        //String rowDefs = (mvParent == null ? "p" : "p") + (addController ? ",2px,p" : "");

        //mainBuilder = new PanelBuilder(new FormLayout("f:p:g", rowDefs));
        //mainComp    = mainBuilder.getPanel();
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
                switcherUI   = FormViewObj.createSwitcher(mvParent, view, altView, altViewsList);
                
                if (altViewsList.size() > 0)
                {
                    if (altView.getMode() == AltView.CreationMode.Edit)
                    {
                        // We want it on the left side of other buttons
                        // so wee need to add it before the Save button
                        //addValidationIndicator(comps);
    
                        //addSaveBtn();
                        comps.add(saveBtn);
                        saveWasAdded = true;
    
                    }
                    comps.add(switcherUI);
                }
            }
            
            if (!saveWasAdded && altView.getMode() == AltView.CreationMode.Edit)
            {
                if (mvParent.getMultiViewParent() == null && !hideSaveBtn)
                {
                    //addSaveBtn();
                    comps.add(saveBtn);
                }
                //addValidationIndicator(comps);
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
     * 
     */
    public void buildTable()
    {
        // Now Build the JTable
        model    = new ColTableModel();
        table    = new JTable(model);
        
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
    
    private void initColumnSizes(final JTable tableArg) 
    {
        ColTableModel     tblModel    = (ColTableModel)tableArg.getModel();
        TableColumn       column      = null;
        Component         comp        = null;
        int               headerWidth = 0;
        int               cellWidth   = 0;
        //Object[]          longValues  = model.longValues;
        
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
    
    //-------------------------------------------------
    // Viewable
    //-------------------------------------------------

    public String getName()
    {
        return "XXX";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getId()
     */
    public int getId()
    {
        return -1;//tableViewDef.getId();
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
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getLabelById(java.lang.String)
     */
    public JLabel getLabelFor(final String id)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getControlMapping()
     */
    public Map<String, Component> getControlMapping()
    {
        return null;
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
            
        } else if (dataObj instanceof Set)
        {
            origDataSet = (Set<Object>)dataObj;
            if (dataObjList == null)
            {
                dataObjList = new Vector<Object>();
                
            } else
            {
                dataObjList.clear();                
            }
            dataObjList.addAll(origDataSet);
        }
        
        
        if (table != null)
        {
            table.tableChanged(new TableModelEvent(model));
            //table.repaint();
        }
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
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getParentDataObj()
     */
    public Object getParentDataObj()
    {
        return null;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataIntoUI()
     */
    public void setDataIntoUI()
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataFromUI()
     */
    public void getDataFromUI()
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataFromUIComp(java.lang.String)
     */
    public Object getDataFromUIComp(final String name)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataIntoUIComp(java.lang.String, java.lang.Object)
     */
    public void setDataIntoUIComp(final String name, Object data)
    {
        // do nothing
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getSubView(java.lang.String)
     */
    public MultiView getSubView(final String name)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getFieldIds(java.util.List)
     */
    public void getFieldIds(final List<String> fieldIds)
    {
        /*for (ColumnInfo fieldInfo : controlsById.values())
        {
            if (fieldInfo.getFormCell().getType() == FormCell.CellType.field)
            {
                fieldIds.add(((FormCellField)fieldInfo.getFormCell()).getId());
            }
        }*/

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#aboutToShow(boolean)
     */
    public void aboutToShow(boolean show)
    {
        if (switcherUI != null)
        {
            ignoreSelection = true;
            switcherUI.setCurrentIndex(0);
            ignoreSelection = false;
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
    public void setSession(final Session session)
    {
        this.session = session;
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
            //if (labels.get(fullCompName) != null)
            //{
            //    log.error("****** Two labels have the same id ["+fullCompName+"] "+formViewDef.getName());
            //}
            
            ColumnInfo colInfo = controlsById.get(fullCompName);
            if (colInfo == null)
            {
                colInfo = new ColumnInfo(getParentClassName(), formCell, fullCompName, null, null);
                controlsById.put(fullCompName, colInfo);
            }
            colInfo.setLabel(formCell.getLabel());
            
        }
        //log.info("Label["+label.getText()+"]");
    }
    
    /**
     * Gets the current class name for the stack contexxt.
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
            
            //if (controlsById.get(formCell.getId()) != null)
            //{
            //    log.error("**** Two controls have the same id ["+formCell.getId()+"] "+formViewDef.getName());
            //}

            //if (controlsByName.get(fullCompName) != null)
            //{
            //    log.error("**** Two controls have the same name ["+fullCompName+"] "+formViewDef.getName());
            //}

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
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#addSeparator(java.lang.String, int, int, int)
     */
    public Component createSeparator(String title, int rowInx, int colInx, int colSpan)
    {
        return null;
    }
    
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
        subViewStack.push(subFormCell);
        if (fullObjPath.length() > 0)
        {
            fullObjPath.append(".");
        }
        fullObjPath.append(subFormCell.getName());
        
        String                 clsName = getParentClassName();
        DBTableIdMgr.TableInfo tblInfo = DBTableIdMgr.lookupByClassName(clsName);
        
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
            ImageIcon icon = IconManager.getImage("ValidationValid");
            UIValidatable.ErrorType state = formValidator.getState();

            if (state == UIValidatable.ErrorType.Incomplete)
            {
                icon = IconManager.getImage("ValidationWarning");

            } else if (state == UIValidatable.ErrorType.Error)
            {
                icon = IconManager.getImage("ValidationError");
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
        dataObj = list.get(newIndex);

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
                DBTableIdMgr.TableInfo tblInfo = DBTableIdMgr.lookupByClassName(parentClassName);
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
            if (getFullCompName().startsWith("accessionAuthorizations"))
            {
                int x = 0;
                x++;
            }
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
    public class ColTableModel implements TableModel
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
            String label = columnList.get(column).getLabel();
            return label != null ? label : "";
        }

        public int getRowCount()
        {
            return dataObjList.size();
        }

        public Object getValueAt(int row, int column)
        {
            if (columnList != null && dataObjList != null)
            {
                ColumnInfo colInfo = columnList.get(column);
                Object     rowObj  = dataObjList.get(row);
                //log.info("["+colInfo.getFullCompName()+"]");

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
                String dataObjFormatName = colInfo.getDataObjFormatName();
                
                Object[] dataValues = UIHelper.getFieldValues(new String[] {colInfo.getFullCompName()}, rowObj, dataGetter);
                
                if (colInfo.getFullCompName().equals("accessionAuthorizations"))
                {
                    return DataObjFieldFormatMgr.aggregate((Set)dataValues[0], "AccessionAuthorizations");
                }
                if (colInfo.getFullCompName().equals("accessionAgents"))
                {
                    return DataObjFieldFormatMgr.aggregate((Set)dataValues[0], "AccessionAgents");
                }
                if (colInfo.getFullCompName().equals("permit"))
                {
                    int x = 0;
                    x++;
                }
                
                if (dataValues != null && dataValues[0] != null)
                {
                    Object data = dataValues[0];
                    if (StringUtils.isNotEmpty(dataObjFormatName))
                    {
                        return DataObjFieldFormatMgr.format(data, dataObjFormatName);
                        
                    } else if (data instanceof Set)
                    {
                        
                    }
                    return data;
                }
                
                return null;
                
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
