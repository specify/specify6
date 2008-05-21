/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui.forms;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.ui.DefaultClassActionHandler;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconTray;
import edu.ku.brc.ui.OrderedIconTray;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.forms.persist.AltViewIFace;
import edu.ku.brc.ui.forms.persist.FormViewDef;
import edu.ku.brc.ui.forms.persist.ViewDef;
import edu.ku.brc.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.ui.forms.persist.ViewIFace;
import edu.ku.brc.ui.forms.validation.FormValidator;
import edu.ku.brc.util.Orderable;
import edu.ku.brc.util.OrderableComparator;

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
    protected ViewIFace                     view;
    protected AltViewIFace                  altView;
    protected ViewDefIFace                  viewDef;
    protected String                        cellName;
    protected Vector<AltViewIFace>          altViewsList;
    protected int                           viewOptions;
    protected Class<?>                      classToCreate = null;
    protected boolean                       ignoreChanges = false;
    
    protected FormDataObjIFace              parentDataObj;
    protected Set<Object>                   dataSet;
    protected String                        dataClassName;
    protected String                        dataSetFieldName;

    // UI stuff
    protected boolean                       dataTypeError;
    protected IconTray                      iconTray;
    protected JPanel                        mainComp;
    protected JPanel                        southPanel;
    protected JButton                       viewBtn           = null;
    protected JButton                       editBtn           = null;
    protected JButton                       newBtn            = null;
    protected JButton                       delBtn            = null;
    protected MenuSwitcherPanel             switcherUI;
    protected JButton                       validationInfoBtn = null;
    protected FormValidator                 validator         = null;
    protected FormValidator                 parentValidator   = null;
    protected boolean                       isEditing;
    
    protected BusinessRulesIFace            businessRules;
    
    protected boolean                       orderableDataClass;

    /**
     * Constructor.
     * 
     * @param view the View
     * @param altView the altView
     * @param mvParent the parent MultiView
     * @param options the view options
     */
    public IconViewObj(final ViewIFace     view, 
                       final AltViewIFace altView, 
                       final MultiView    mvParent, 
                       final int          options)
    {
        this.view          = view;
        this.altView       = altView;
        this.mvParent      = mvParent;
        this.viewOptions   = options;
        this.viewDef       = altView.getViewDef();
        this.dataTypeError = false;
        this.businessRules = view.createBusinessRule();
        
        if (businessRules != null)
        {
            businessRules.initialize(this);
        }
        
        isEditing  = MultiView.isOptionOn(options, MultiView.IS_EDITTING) || MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT);
        
        if (isEditing)
        {
            if (mvParent.getMultiViewParent() != null)
            {
                parentValidator = mvParent.getMultiViewParent().getCurrentValidator();
                
                // We need a form validator that always says it's valid
                validator = new FormValidator(null)
                {
                    @Override
                    public boolean isFormValid()
                    {
                        return true;
                    }
                };
            }
        }
        
        try
        {
            Class<?> dataClass = Class.forName(view.getClassName());
            if (Orderable.class.isAssignableFrom(dataClass))
            {
                // this IconViewObj is showing Orderable objects
                // so we should use an OrderedIconTray
                orderableDataClass = true;
            }
        }
        catch (ClassNotFoundException e)
        {
            log.error("Data class of view cannot be found", e);
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
     * @return
     */
    public String getDataClassName()
    {
        return this.dataClassName;
    }
    
    /**
     * 
     */
    protected void initMainComp()
    {
        if (isEditing)
        {
            editBtn = UIHelper.createButton("EditForm", getResourceString("EditRecord"), IconManager.IconSize.Std16, true);
            newBtn  = UIHelper.createButton("CreateObj", getResourceString("NewRecord"), IconManager.IconSize.Std16, true);
            delBtn  = UIHelper.createButton("DeleteRecord", getResourceString("DeleteRecord"), IconManager.IconSize.Std16, true);
            validationInfoBtn = FormViewObj.createValidationIndicator(this);
            
            editBtn.setEnabled(false);
            delBtn.setEnabled(false);

        } else
        {
            viewBtn = UIHelper.createButton("InfoIcon", getResourceString("ShowRecordInfoTT"), IconManager.IconSize.Std16, true);
            viewBtn.setEnabled(false);

        }
        
        altViewsList = new Vector<AltViewIFace>();
        switcherUI   = FormViewObj.createMenuSwitcherPanel(mvParent, view, altView, altViewsList);
        
        if (orderableDataClass && isEditing)
        {
            iconTray = new OrderedIconTray(IconTray.SINGLE_ROW);
            iconTray.addPropertyChangeListener(new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    if (evt.getPropertyName().equalsIgnoreCase("item order"))
                    {
                        rootHasChanged();
                    }
                }
            });
        }
        else // the data isn't Orderable or we're in view only mode
        {
            iconTray = new IconTray(IconTray.SINGLE_ROW);
        }
        
        iconTray.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 1)
                {
                    updateEnableUI();
                    
                } else if (e.getClickCount() > 1)
                {
                    doDoubleClick();
                }
            }
        });
        
         
        if (isEditing)
        {
            addActionListenerToEditButton();
            addActionListenerToNewButton();
            addActionListenerToDeleteButton();
            
            IconViewTransferHandler ivth = new IconViewTransferHandler(this);
            iconTray.setTransferHandler(ivth);
            
        } else
        {
            addActionListenerToViewButton();
        }

        mainComp = new JPanel();
        mainComp.setLayout(new BorderLayout());
        if (mvParent == null)
        {
            mainComp.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        }
        
        int             defCnt  = (isEditing ? 3 : 1) + (switcherUI != null ? 1 : 0) + (validationInfoBtn != null ? 1 : 0);
        String          colDef  = "f:1px:g," + UIHelper.createDuplicateJGoodiesDef("p", "1px", defCnt);
        PanelBuilder    builder = new PanelBuilder(new FormLayout(colDef, "p"));
        CellConstraints cc      = new CellConstraints();
        
        int x = 2;
        if (isEditing)
        {
            builder.add(editBtn, cc.xy(x, 1)); 
            x += 2;
            builder.add(newBtn, cc.xy(x, 1));
            x += 2;
            builder.add(delBtn, cc.xy(x, 1));
            x += 2;
            
            if (validationInfoBtn != null) // is null when genrating form images
            {
                builder.add(validationInfoBtn, cc.xy(x, 1));
                x += 2;
            }

        } else
        {
            builder.add(viewBtn, cc.xy(x, 1));
            x += 2;
        }
        
        if (switcherUI != null)
        {
            builder.add(switcherUI, cc.xy(x, 1));
            x += 2;
        }
        southPanel = builder.getPanel();

        mainComp.add(iconTray,BorderLayout.CENTER);
        mainComp.add(southPanel,BorderLayout.SOUTH);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#checkForChanges()
     */
    public boolean isDataCompleteAndValid()
    {
        return true;
    }
    
    /**
     * Enables the Add,Del,Edit/View buttons
     */
    protected void updateEnableUI()
    {
        boolean enabled = iconTray.getSelectedValue() != null;
        if (editBtn != null)
        {
            editBtn.setEnabled(enabled);
        }
        if (delBtn != null)
        {
            delBtn.setEnabled(enabled);
        }
        if (viewBtn != null)
        {
            viewBtn.setEnabled(enabled);
        } 
    }

    /**
     * @param e mouse event
     */
    protected void doDoubleClick()
    {
        FormDataObjIFace selection = iconTray.getSelectedValue();
        ActionListener listener = DefaultClassActionHandler.getInstance().getDefaultClassActionHandler(selection.getClass());
        if (listener != null)
        {
            listener.actionPerformed(new IconViewActionEvent(selection, 0, "double-click", this));
        }
        else
        {
            ViewBasedDisplayIFace dialog = UIHelper.createDataObjectDialog(altView, mainComp, selection, MultiView.isOptionOn(viewOptions, MultiView.IS_EDITTING), false);
            dialog.setData(selection);
            dialog.showDisplay(true);
            dialog.dispose();
        }
    }
    
    /**
     * 
     */
    protected void addActionListenerToViewButton()
    {
        if (viewBtn != null)
        {
            viewBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    doDoubleClick();
                }
            });
        }
    }
    
    /**
     * 
     */
    protected void addActionListenerToEditButton()
    {
        editBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                FormDataObjIFace selection = iconTray.getSelection();
                if (selection==null)
                {
                    return;
                }
                
                ViewBasedDisplayIFace dialog = UIHelper.createDataObjectDialog(altView, mainComp, selection, MultiView.isOptionOn(viewOptions, MultiView.IS_EDITTING), false);
                dialog.setData(selection);
                dialog.showDisplay(true);
                if (dialog.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
                {
                    dialog.getMultiView().getDataFromUI();
                    rootHasChanged();
                }
                dialog.dispose();
            }
        });
    }
    
    /**
     * @param f
     * @return
     */
    public boolean addRecord(final File f)
    {
        FormDataObjIFace newObject;
        if (classToCreate != null)
        {
            newObject = FormHelper.createAndNewDataObj(classToCreate);
        } else
        {
            newObject = FormHelper.createAndNewDataObj(view.getClassName());
        }

        FileImportProcessor importer = FileImportProcessor.getInstance();
        if (!importer.importFileIntoRecord(newObject, f))
        {
            return false;
        }
        
        parentDataObj.addReference(newObject, dataSetFieldName);
        iconTray.addItem(newObject);
        
        rootHasChanged();
        
        return true;
    }
    
    /**
     * Notifies the MultiView Parent that the Icon Tray has changed. 
     */
    protected void rootHasChanged()
    {
        if (!ignoreChanges)
        {
            if (validator != null)
            {
                validator.setHasChanged(true);
                validator.validateForm();
            }
            
            MultiView realParent = mvParent.getMultiViewParent();
            if (realParent != null)
            {
                realParent.getCurrentValidator().setHasChanged(true);
                realParent.getCurrentValidator().validateForm();
            }
        }
    }
    
    /**
     * 
     */
    protected void addActionListenerToNewButton()
    {
        newBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                FormDataObjIFace newObject;
                if (classToCreate != null)
                {
                    newObject = FormHelper.createAndNewDataObj(classToCreate);
                } else
                {
                    newObject = FormHelper.createAndNewDataObj(view.getClassName());
                }
                
                // get an edit dialog for the object
                ViewBasedDisplayIFace dialog = UIHelper.createDataObjectDialog(altView, mainComp, newObject, true, true);
                if (dialog == null)
                {
                    log.error("Unable to create a dialog for data entry.  [" + newObject.getClass().getName() + "]");
                    return;
                }
                
                if (mvParent != null)
                {
                    mvParent.registerDisplayFrame(dialog);
                }
                
                dialog.setData(newObject);
                dialog.showDisplay(true);
                
                if (dialog.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
                {
                    dialog.getMultiView().getDataFromUI();
                    
                    log.warn("User clicked OK.  Adding " + newObject.getIdentityTitle() + " into " + dataSetFieldName + ".");
                    parentDataObj.addReference(newObject, dataSetFieldName);
                    iconTray.addItem(newObject);

                    rootHasChanged();
                    
                } else if (dialog.getBtnPressed() == ViewBasedDisplayIFace.CANCEL_BTN)
                {
                    if (mvParent.getMultiViewParent() != null && mvParent.getMultiViewParent().getCurrentValidator() != null)
                    {
                        mvParent.getMultiViewParent().getCurrentValidator().validateForm();
                    }
                }
                dialog.dispose();
            }
        });
    }
    
    /**
     * 
     */
    protected void doDelete()
    {
        FormDataObjIFace dataObj = iconTray.getSelection();
        if (dataObj != null)
        {
            Object[] delBtnLabels = {getResourceString("Delete"), getResourceString("Cancel")};
            int rv = JOptionPane.showOptionDialog(null, UIRegistry.getLocalizedMessage("ASK_DELETE", dataObj.getIdentityTitle()),
                                                  getResourceString("Delete"),
                                                  JOptionPane.YES_NO_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE,
                                                  null,
                                                  delBtnLabels,
                                                  delBtnLabels[1]);
            if (rv == JOptionPane.YES_OPTION)
            {
                
                iconTray.removeItem(dataObj);
                parentDataObj.removeReference(dataObj, IconViewObj.this.cellName);
                if (mvParent != null)
                {
                    MultiView topLvl = mvParent.getTopLevel();
                    topLvl.addDeletedItem(dataObj);
                    rootHasChanged();
                }
                iconTray.repaint();
                updateEnableUI();
                
                rootHasChanged();
            }
        }
    }
    
    /**
     * 
     */
    protected void addActionListenerToDeleteButton()
    {
        delBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                doDelete();
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
        if (mainComp == null)
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
            
            if (dataObj != null)
            {
                if (dataObj instanceof List)
                {
                    dataSet.addAll((List)dataObj);
                    
                } else
                {
                    // single object
                    dataSet.add(dataObj);
                }
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
        if (parentDataObj != null && !(parentDataObj instanceof FormDataObjIFace))
        {
            dataTypeError = true;
            return;
        }
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
        ignoreChanges = true;
        
        if (mainComp == null)
        {
            initMainComp();
        }

        iconTray.removeAllItems();
        
        Vector<Object> dataObjects = new Vector<Object>();
        dataObjects.addAll(dataSet);
        if (this.orderableDataClass)
        {
            Vector<Orderable> sortedDataObjects = new Vector<Orderable>();
            for (Object obj : dataObjects)
            {
                if (obj instanceof Orderable)
                {
                    sortedDataObjects.add((Orderable)obj);
                }
            }
            Collections.sort(sortedDataObjects, new OrderableComparator());
            
            dataObjects.clear();
            dataObjects.addAll(sortedDataObjects);
        }
        
        for (Object o: dataObjects)
        {
            if (!(o instanceof FormDataObjIFace))
            {
                log.error("Icon view data set contains non-FormDataObjIFace objects.  Item being ignored.");
                mainComp.removeAll();
                JLabel lbl = createLabel(getResourceString("Error"));
                mainComp.add(lbl);
 
                dataTypeError = true;
                return;
            }
            
            FormDataObjIFace formDataObj = (FormDataObjIFace)o;
            iconTray.addItem(formDataObj);
        }
        ignoreChanges = false;

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
     * @see edu.ku.brc.ui.forms.Viewable#getFieldNames(java.util.List)
     */
    public void getFieldNames(List<String> fieldNames)
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
        
        // Moving this to the MultiView
        /*if (show)
        {
            log.debug("Dispatching a Data_Entry/ViewWasShown command/action");
            CommandDispatcher.dispatch(new CommandAction("Data_Entry", "ViewWasShown", this));
        }*/
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getView()
     */
    public ViewIFace getView()
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
    public AltViewIFace getAltView()
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
            DBTableInfo        parentTI = DBTableIdMgr.getInstance().getByClassName(parentDataObj.getClass().getName());
            DBRelationshipInfo rel      = parentTI.getRelationshipByName(cellName);
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
        mvParent      = null;
        mainComp      = null;
        
        if (businessRules != null)
        {
            businessRules.formShutdown();
            businessRules = null;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getSaveBtn()
     */
    public JComponent getSaveComponent()
    {
        return null;
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

        public AltViewIFace getAltView()
        {
            return icIconViewObj.getAltView();
        }

        public IconViewObj getIconViewObj()
        {
            return icIconViewObj;
        }

        public ViewIFace getView()
        {
            return icIconViewObj.getView();
        }

        public ViewDefIFace getViewDef()
        {
            return icIconViewObj.getViewDef();
        }
    }
}
