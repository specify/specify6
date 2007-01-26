/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui.forms;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.DBTableIdMgr.TableInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr.TableRelationship;
import edu.ku.brc.ui.DefaultClassActionHandler;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconTray;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.FormViewDef;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.forms.persist.ViewDef;
import edu.ku.brc.ui.forms.persist.AltView.CreationMode;
import edu.ku.brc.ui.validation.FormValidator;

/**
 * A Viewable that will display a set of FormDataObjIFace objects in a file
 * browser-like UI.
 *
 * @code_status Beta
 * @author jds
 */
public class IconViewObj implements Viewable
{
    protected static final Logger log = Logger.getLogger(IconViewObj.class);
    
    // Data Members
    protected DataProviderSessionIFace      session;
    protected MultiView                     mvParent;
    protected View                          view;
    protected AltView                       altView;
    protected ViewDef                       viewDef;
    protected String                        cellName;
    protected Vector<AltView>               altViewsList;
    protected int                           viewOptions;
    
    protected FormDataObjIFace              parentDataObj;
    protected Set<Object>                   dataSet;
    protected String                        dataClassName;
    protected String                        dataSetFieldName;

    // UI stuff
    protected IconTray                      iconTray;
    protected JPanel mainComp;
    protected JPanel southPanel;
    protected JButton editButton;
    protected JButton newButton;
    protected JButton deleteButton;
    protected MenuSwitcherPanel             switcherUI;
    protected JButton                       validationInfoBtn;
    
    protected boolean dataTypeError;
    
    protected FormValidator validator;
    
    protected BusinessRulesIFace            businessRules;

    /**
     * Constructor.
     * 
     * @param view the View
     * @param altView the altView
     * @param mvParent the parent MultiView
     * @param options the view options
     */
    public IconViewObj(final View view, final AltView altView, final MultiView mvParent, final int options)
    {
        this.view = view;
        this.altView = altView;
        this.mvParent = mvParent;
        this.viewOptions = options;
        this.viewDef = altView.getViewDef();
        this.dataTypeError = false;
        this.businessRules = view.getBusinessRule();
        
        // we need a form validator that always says it's valid
        validator = new FormValidator(){
            @Override
            public boolean isFormValid()
            {
                return true;
            }
        };
        MultiView root = mvParent;
        while (root.getMultiViewParent() != null)
        {
            root = root.getMultiViewParent();
        }
        validator.setName("IconViewObj validator");
        root.addFormValidator(validator);
    }
    
    public String getDataClassName()
    {
        return this.dataClassName;
    }
    
    protected void initMainComp()
    {
        editButton   = UIHelper.createButton("EditForm", getResourceString("EditRecord"), IconManager.IconSize.Std16, true);
        newButton    = UIHelper.createButton("CreateObj", getResourceString("NewRecord"), IconManager.IconSize.Std16, true);
        deleteButton = UIHelper.createButton("SmallTrash", getResourceString("DeleteRecord"), IconManager.IconSize.Std16, true);

        altViewsList = new Vector<AltView>();
        switcherUI   = FormViewObj.createMenuSwitcherPanel(mvParent, view, altView, altViewsList);
        
        validationInfoBtn = FormViewObj.createValidationIndicator(this);

        
        iconTray = new IconTray(IconTray.SINGLE_ROW);
        //iconTray = new IconTray(IconTray.MULTIPLE_ROWS);
        
        iconTray.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount()>1)
                {
                    doDoubleClick(e);
                }
            }
        });
        
        addActionListenerToEditButton();
        
        if (altView.getMode() == CreationMode.View)
        {
            newButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
        else
        {
            addActionListenerToNewButton();
            addActionListenerToDeleteButton();
            
            IconViewTransferHandler ivth = new IconViewTransferHandler(this);
            iconTray.setTransferHandler(ivth);
        }

        mainComp = new JPanel();
        mainComp.setLayout(new BorderLayout());
        if (mvParent == null)
        {
            mainComp.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        }
        
        PanelBuilder builder = new PanelBuilder(new FormLayout("f:1px:g,p,1px,p,1px,p,1px,p,1px,p", "p"));
        CellConstraints cc  = new CellConstraints();
        
        builder.add(editButton, cc.xy(2,1));
        builder.add(newButton, cc.xy(4,1));
        builder.add(deleteButton, cc.xy(6,1));
        builder.add(validationInfoBtn, cc.xy(8,1));
        if (switcherUI != null)
        {
            builder.add(switcherUI, cc.xy(10,1));
        }
        southPanel = builder.getPanel();

        mainComp.add(iconTray,BorderLayout.CENTER);
        mainComp.add(southPanel,BorderLayout.SOUTH);
    }

    protected void doDoubleClick(@SuppressWarnings("unused") MouseEvent e)
    {
        FormDataObjIFace selection = iconTray.getSelectedValue();
        ActionListener listener = DefaultClassActionHandler.getInstance().getDefaultClassActionHandler(selection.getClass());
        if (listener!=null)
        {
            listener.actionPerformed(new IconViewActionEvent(selection, 0, "double-click", this));
        }
        else
        {
            ViewBasedDisplayIFace dialog = UIHelper.createDataObjectDialog(altView, mainComp, selection, false);
            dialog.setData(selection);
            dialog.showDisplay(true);
        }
    }
        
    protected void addActionListenerToEditButton()
    {
        editButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                FormDataObjIFace selection = iconTray.getSelection();
                if (selection==null)
                {
                    return;
                }
                
                final ViewBasedDisplayIFace dialog = UIHelper.createDataObjectDialog(altView, mainComp, selection,false);
                dialog.setCloseListener(new PropertyChangeListener()
                {
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        String action = evt.getPropertyName();
                        if (action.equals("OK"))
                        {
                            dialog.getMultiView().getDataFromUI();
                            if (mvParent != null)
                            {
                                MultiView root = mvParent;
                                while (root.getMultiViewParent() != null)
                                {
                                    root = root.getMultiViewParent();
                                }
                                validator.setHasChanged(true);
                                root.dataChanged(null, null, null);
                            }

                        }
                        else if (action.equals("Cancel"))
                        {
                            log.warn("User clicked Cancel");
                        }
                    }
                });
                dialog.setData(selection);
                dialog.showDisplay(true);
            }
        });
    }
    
    public boolean addRecord(File f)
    {
        final FormDataObjIFace newObject = FormHelper.createAndNewDataObj(dataClassName);

        FileImportProcessor importer = FileImportProcessor.getInstance();
        if (!importer.importFileIntoRecord(newObject, f))
        {
            return false;
        }
        
        parentDataObj.addReference(newObject, dataSetFieldName);
        iconTray.addItem(newObject);
        if (mvParent != null)
        {
            MultiView root = mvParent;
            while (root.getMultiViewParent() != null)
            {
                root = root.getMultiViewParent();
            }
            validator.setHasChanged(true);
            root.dataChanged(null, null, null);
        }
        return true;
    }
    
    protected void addActionListenerToNewButton()
    {
        newButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                final FormDataObjIFace newObject = FormHelper.createAndNewDataObj(dataClassName);
                
                // get an edit dialog for the object
                final ViewBasedDisplayIFace dialog = UIHelper.createDataObjectDialog(altView, mainComp, newObject,true);
                dialog.setCloseListener(new PropertyChangeListener()
                {
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        String action = evt.getPropertyName();
                        if (action.equals("OK"))
                        {
                            dialog.getMultiView().getDataFromUI();
                            log.warn("User clicked OK.  Adding " + newObject.getIdentityTitle() + " into " + dataSetFieldName + ".");
                            parentDataObj.addReference(newObject, dataSetFieldName);
                            iconTray.addItem(newObject);
                            if (mvParent != null)
                            {
                                MultiView root = mvParent;
                                while (root.getMultiViewParent() != null)
                                {
                                    root = root.getMultiViewParent();
                                }
                                validator.setHasChanged(true);
                                root.dataChanged(null, null, null);
                            }
                        }
                        else if (action.equals("Cancel"))
                        {
                            // nothing to do
                        }
                        
                        if (mvParent != null)
                        {
                            mvParent.unregisterDisplayFrame(dialog);
                        }
                    }
                });
                if (mvParent != null)
                {
                    mvParent.registerDisplayFrame(dialog);
                }
                dialog.setData(newObject);

                dialog.showDisplay(true);
            }
        });
    }
    
    protected void addActionListenerToDeleteButton()
    {
        deleteButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                FormDataObjIFace selection = iconTray.getSelection();
                if (selection==null)
                {
                    return;
                }
                
                iconTray.removeItem(selection);
                session.deleteOnSaveOrUpdate(selection);
                if (mvParent != null)
                {
                    MultiView root = mvParent;
                    while (root.getMultiViewParent() != null)
                    {
                        root = root.getMultiViewParent();
                    }
                    validator.setHasChanged(true);
                    root.dataChanged(null, null, null);
                }
            }
        });
    }

    //-------------------------------------------------
    // Viewable
    //-------------------------------------------------

    public int getViewOptions()
    {
        return viewOptions;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getName()
     */
    public String getName()
    {
        return "Icon View";
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
        return viewDef.getType();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getUIComponent()
     */
    public synchronized Component getUIComponent()
    {
        if (mainComp==null)
        {
            initMainComp();
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
        return validator;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataObj(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void setDataObj(Object dataObj)
    {
        if (dataObj instanceof Set)
        {
            dataSet = (Set)dataObj;
            
        } else
        {
            if (dataSet == null)
            {
                dataSet = new HashSet<Object>();
            } else
            {
                dataSet.clear();
            }
            
            if (dataObj instanceof List)
            {
                dataSet.addAll((List)dataObj);
                
            } else
            {
                // single object
                dataSet.add(dataObj);
            }
        }
        
        setDataIntoUI();
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
        return dataSet;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setParentDataObj(java.lang.Object)
     */
    public void setParentDataObj(Object parentDataObj)
    {
//        if (!(parentDataObj instanceof FormDataObjIFace))
//        {
//            dataTypeError = true;
//            return;
//        }
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
    public synchronized void setDataIntoUI()
    {
        /*
        if (mvParent == null || mvParent.isRoot())
        {
            if (session != null)
            {
                session.close();
            }
            session = DataProviderFactory.getInstance().createSession();
            
            if (mvParent != null && mvParent.isTopLevel())
            {
                mvParent.setSession(session);   
            }
        } else if (session != null && dataSet != null)
        {
            for (Object o: dataSet)
            {
                session.attach(o);
            }
        }*/
        
        if (mainComp == null)
        {
            initMainComp();
        }

        iconTray.removeAllItems();
        
        for (Object o: dataSet)
        {
            if (!(o instanceof FormDataObjIFace))
            {
                log.error("Icon view data set contains non-FormDataObjIFace objects.  Item being ignored.");
                mainComp.removeAll();
                JLabel lbl = new JLabel(getResourceString("Error"));
                mainComp.add(lbl);
 
                dataTypeError = true;
                return;
            }
            
            iconTray.addItem((FormDataObjIFace)o);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataFromUI()
     */
    public void getDataFromUI()
    {
        if (dataTypeError)
        {
            return;
        }
        
        dataSet.clear();
        Set<FormDataObjIFace> iconTrayItems = iconTray.getItems();
        for (FormDataObjIFace fdo: iconTrayItems)
        {
            dataSet.add(fdo);
        }
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
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#aboutToShow(boolean)
     */
    public void aboutToShow(boolean show)
    {
        if (switcherUI != null)
        {
            switcherUI.set(altView);
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#hideMultiViewSwitch(boolean)
     */
    public void hideMultiViewSwitch(boolean hide)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#dataHasChanged()
     */
    public void validationWasOK(boolean wasOK)
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setSession(edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    public void setSession(final DataProviderSessionIFace session)
    {
        this.session = session;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setHasNewData(boolean)
     */
    public void setHasNewData(final boolean isNewForm)
    {
        // this gives you the opportunity to adjust your UI
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
        else
        {
            TableInfo parentTI = DBTableIdMgr.lookupByClassName(parentDataObj.getClass().getName());
            TableRelationship rel = parentTI.getRelationshipByName(cellName);
            this.dataClassName = rel.getClassName();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#registerSaveBtn(javax.swing.JButton)
     */
    public void registerSaveBtn(JButton saveBtn)
    {
        // TODO: ???
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#shutdown()
     */
    public void shutdown()
    {
        mvParent      = null;
        mainComp      = null;
    }
    
    //-----------------------------------------------------------------------------------------------
    //-- Inner Classes
    //-----------------------------------------------------------------------------------------------
    
    public class IconViewActionEvent extends ActionEvent
    {
        protected IconViewObj icIconViewObj;

        public IconViewActionEvent(Object      source, 
                                   int         id, 
                                   String      command,
                                   IconViewObj icIconViewObj)
        {
            super(source, id, command);
            
            this.icIconViewObj = icIconViewObj;
        }

        public AltView getAltView()
        {
            return icIconViewObj.getAltView();
        }

        public IconViewObj getIconViewObj()
        {
            return icIconViewObj;
        }

        public View getView()
        {
            return icIconViewObj.getView();
        }

        public ViewDef getViewDef()
        {
            return icIconViewObj.getViewDef();
        }
    }
}
