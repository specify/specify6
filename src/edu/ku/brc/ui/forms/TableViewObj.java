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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.jgoodies.forms.builder.PanelBuilder;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.DropDownButtonStateful;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.FormCell;
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
    private static final Logger log = Logger.getLogger(TableViewObj.class);
    
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
    
    protected FormValidator                 formValidator   = null;
    protected Object                        parentDataObj   = null;
    protected Object                        dataObj         = null;
    protected Set<Object>                   origDataSet     = null;
    protected List<Object>                  dataObjList     = null;
    protected Object[]                      singleItemArray = new Object[1];
    protected SimpleDateFormat              scrDateFormat;

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

    protected PanelBuilder                  mainBuilder;
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

        scrDateFormat = AppPrefsCache.getSimpleDateFormat("ui", "formatting", "scrdateformat");


        AppPreferences.getRemote().addChangeListener("ui.formatting.viewfieldcolor", this);
        
    }
    
    /**
     * Builds the main component as a table
     */
    protected void buildTable()
    {
        model = new ColTableModel();
        
        mainComp = new JPanel(new BorderLayout());
        table = new JTable(model);
        
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);

        //for (int i=0;i<_model.getColumnCount();i++) {
        //    TableColumn column = _table.getColumn(_model.getColumnName(i));
        //    column.setCellRenderer(renderer);
        //}

        tableScroller = new JScrollPane(table);
        mainComp.add(tableScroller, BorderLayout.CENTER);  
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
            dataObjList.clear();
            dataObjList.addAll(origDataSet);
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
        // do nothing
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
        log.info("Label["+label.getText()+"]");
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
        
        log.info("RegControl["+formCell.getName()+"]");
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
        log.info("addControlToUI["+control+"]");
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
        String clsName   = getParentClassName();
        String fieldName = subFormCell.getName();
        DBTableIdMgr.TableInfo tblInfo = DBTableIdMgr.lookupByClassName(clsName);
        if (tblInfo != null)
        {
            DBTableIdMgr.RelationshipType type = tblInfo.getRelType(subFormCell.getName());
            log.info(type+"  "+fieldName+" "+clsName);
            
            boolean isSet = type == DBTableIdMgr.RelationshipType.OneToMany || type == DBTableIdMgr.RelationshipType.ManyToMany;
            if (isSet)
            {
                skipControls++;
                
                String fullCompName = subFormCell.getName();//appendName(subFormCell.getName());
                String     fullId  = appendName(subFormCell.getId());
                ColumnInfo colInfo = controlsById.get(fullId);
                if (colInfo == null)
                {
                    colInfo = new ColumnInfo(getParentClassName(), subFormCell, fullCompName, null, null);
                    colInfo.setLabel(subFormCell.getDescription());
                    controlsById.put(fullId, colInfo); 
                }
                columnList.add(colInfo);
                controlsByName.put(fullCompName, colInfo);
            }
            log.info(isSet);
        }
        log.info("Add Name["+fullObjPath.toString()+"]");
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
        log.info("Done Name["+fullObjPath.toString()+"]");
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
            
            checkForSet();
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
            checkForSet();
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
        protected List<String[]> rowData;
        protected Vector<String> methods;


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
            ColumnInfo colInfo = columnList.get(column);
            Object     rowObj  = dataObjList.get(row);
            log.info("["+colInfo.getFullCompName()+"]");
            if (colInfo.getFullCompName().equals("accessionAuthorizations.permit"))
            {
                int x = 0;
                x++;
            }
            if (colInfo.getFullCompName().equals("accessionAuthorizations"))
            {
                int x = 0;
                x++;
            }
            String[] fName = new String[1];
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
            }
            Object[] dataValues = UIHelper.getFieldValues(new String[] {colInfo.getFullCompName()}, rowObj, dataGetter);
            return dataValues != null ? dataValues[0] : null;
            
            //return dataGetter.getFieldValue(rowObj, colInfo.getFullCompName());
        }

        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
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
}
