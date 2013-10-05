/* Copyright (C) 2013, University of Kansas Center for Research
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

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.FormViewDef;
import edu.ku.brc.af.ui.forms.persist.ViewDef;
import edu.ku.brc.af.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DefaultClassActionHandler;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconTray;
import edu.ku.brc.ui.OrderedIconTray;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Orderable;
import edu.ku.brc.util.OrderableComparator;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * A Viewable that will display a set of FormDataObjIFace objects in a file
 * browser-like UI. It now used an Orderable IconTray at all times and disables it when it isn't needed.
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
    protected boolean                       isSkippingAttach = false; // Indicates whether to skip before setting data into the form
    protected boolean                       isAlwaysGetDataFromUI = false;

    protected FormDataObjIFace              parentDataObj;
    protected Set<Object>                   dataSet;
    protected String                        dataClassName;
    protected String                        dataSetFieldName;

    // UI stuff
    protected boolean                       dataTypeError;
    protected OrderedIconTray               iconTray;
    protected RestrictablePanel             mainComp;
    protected JPanel                        southPanel;
    protected JButton                       viewBtn           = null;
    protected JButton                       editBtn           = null;
    protected JButton                       newBtn            = null;
    protected JButton                       delBtn            = null;
    protected JPanel                        sepController     = null;
    
    protected MenuSwitcherPanel             switcherUI;
    protected JButton                       validationInfoBtn = null;
    protected FormValidator                 validator         = null;
    protected FormValidator                 parentValidator   = null;
    protected boolean                       isEditing;
    
    protected BusinessRulesIFace            businessRules;
    protected Class<?>                      dataClass;
    
    protected boolean                       orderableDataClass;
    
    // Security
    private PermissionSettings              perm = null;

    /**
     * Constructor.
     * 
     * @param view the View
     * @param altView the altView
     * @param mvParent the parent MultiView
     * @param options the view options
     * @param cellName the name of the cell when it is a subview
     * @param dataClass the class of the data that is put into the form
     */
    public IconViewObj(final ViewIFace     view, 
                       final AltViewIFace altView, 
                       final MultiView    mvParent, 
                       final int          options,
                       final String       cellName,
                       final Class<?>     dataClass)
    {
        this.view          = view;
        this.altView       = altView;
        this.mvParent      = mvParent;
        this.viewOptions   = options;
        this.viewDef       = altView.getViewDef();
        this.dataTypeError = false;
        this.businessRules = view.createBusinessRule();
        this.cellName      = cellName;
        this.dataClass     = dataClass;
        
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
        
        checkOrderableness();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getMVParent()
     */
    @Override
    public MultiView getMVParent()
    {
        return mvParent;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setClassToCreate(java.lang.String)
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
            String delTTStr = ResultSetController.createTooltip("DeleteRecordTT", view.getObjTitle());
            String edtTTStr = ResultSetController.createTooltip("EditRecordTT",   view.getObjTitle());
            String newTTStr = ResultSetController.createTooltip("NewRecordTT",    view.getObjTitle());

            editBtn = UIHelper.createIconBtnTT("EditForm",     IconManager.IconSize.Std16, edtTTStr, false, null);
            newBtn  = UIHelper.createIconBtnTT("CreateObj",    IconManager.IconSize.Std16, newTTStr, false, null);
            delBtn  = UIHelper.createIconBtnTT("DeleteRecord", IconManager.IconSize.Std16, delTTStr, false, null);
            
            validationInfoBtn = FormViewObj.createValidationIndicator(mainComp, getValidator());
            
            editBtn.setEnabled(false);
            delBtn.setEnabled(false);
            newBtn.setEnabled(true);
            
        } else
        {
            String srchTTStr = ResultSetController.createTooltip("ShowRecordInfoTT", view.getObjTitle());
            viewBtn = UIHelper.createIconBtnTT("InfoIcon", IconManager.IconSize.Std16, srchTTStr, false, null);
            viewBtn.setEnabled(false);
        }
        
        altViewsList = new Vector<AltViewIFace>();
        switcherUI   = FormViewObj.createMenuSwitcherPanel(mvParent, view, altView, altViewsList, mainComp, cellName, dataClass);
        
        Dimension maxSize = Thumbnailer.getInstance().getMaxSize();
        iconTray = new OrderedIconTray(IconTray.SINGLE_ROW, maxSize.width, maxSize.height);
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
        
        iconTray.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    updateEnableUI();
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
        
        mainComp = new RestrictablePanel();
        mainComp.setLayout(new BorderLayout());
        if (mvParent == null)
        {
            mainComp.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        }
        
        boolean isAbove = mvParent.getSeparator() != null;
        
        int             defCnt  = (isEditing ? 3 : 1) + (switcherUI != null ? 1 : 0) + (validationInfoBtn != null ? 1 : 0);
        String          colDef  = (isAbove ? "1px," : "f:1px:g,") + UIHelper.createDuplicateJGoodiesDef("p", "1px", defCnt);
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
        
        if (isAbove)
        {
            sepController = builder.getPanel();
        } else
        {
            southPanel = builder.getPanel();
            mainComp.add(southPanel,BorderLayout.SOUTH);
        }

        mainComp.add(iconTray,BorderLayout.CENTER);
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#isDataCompleteAndValid(boolean)
     */
    public boolean isDataCompleteAndValid(final boolean throwAwayOnDiscard)
    {
        return true;
    }
    
    /**
     * Checks to see if the data class is orderable.
     */
    protected void checkOrderableness()
    {
        try
        {
            Class<?> dataClss = Class.forName(dataClassName != null ? dataClassName : view.getClassName());
            if (Orderable.class.isAssignableFrom(dataClss))
            {
                // this IconViewObj is showing Orderable objects
                // so we should use an OrderedIconTray
                orderableDataClass = true;
                return;
            }
        }
        catch (ClassNotFoundException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IconViewObj.class, e);
            log.error("Data class of view cannot be found", e);
        }
        
        if (iconTray != null)
        {
            iconTray.setOrderable(false);
        }
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
     * 
     */
    protected void doDoubleClick()
    {
        FormDataObjIFace selection = iconTray.getSelectedValue();
        ActionListener listener = DefaultClassActionHandler.getInstance().getDefaultClassActionHandler(selection.getClass());
        if (listener != null)
        {
            CommandAction cmdAction = new CommandAction("ATTACHMENTS", "DisplayAttachment", selection);
            CommandDispatcher.dispatch(cmdAction);
        } else
        {
            ViewBasedDisplayIFace dialog = FormHelper.createDataObjectDialog(mainComp, selection, MultiView.isOptionOn(viewOptions, MultiView.IS_EDITTING), false);
            if (dialog != null)
            {
                dialog.setData(selection);
                dialog.showDisplay(true);
                dialog.dispose();
            }
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
                    editItem();
                }
            });
        }
    }
    
    /**
     * 
     */
    private void editItem()
    {
        FormDataObjIFace selection = iconTray.getSelection();
        if (selection == null)
        {
            return;
        }
        
        ViewBasedDisplayIFace dialog = FormHelper.createDataObjectDialog(mainComp, selection, MultiView.isOptionOn(viewOptions, MultiView.IS_EDITTING), false);
        if (dialog != null)
        {
            dialog.setData(selection);
            dialog.showDisplay(true);
            if (dialog.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
            {
                dialog.getMultiView().getDataFromUI();
                rootHasChanged();
                iconTray.validate();
                iconTray.repaint();
            }
            dialog.dispose();
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
                editItem();
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
            newObject = FormHelper.createAndNewDataObj(classToCreate, businessRules);
        } else
        {
            newObject = FormHelper.createAndNewDataObj(view.getClassName(), businessRules);
        }
        setupAsOrderable(newObject);

        FileImportProcessor importer = FileImportProcessor.getInstance();
        if (!importer.importFileIntoRecord(newObject, newObject.getTableId(), f))
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
            
            if (mvParent != null)
            {
                MultiView realParent = mvParent.getMultiViewParent();
                if (realParent != null && realParent.getCurrentValidator() != null)
                {
                    realParent.getCurrentValidator().setHasChanged(true);
                    realParent.getCurrentValidator().validateForm();
                }
            }
        }
    }
    
    /**
     * 
     */
    protected void createNewDataObject()
    {
        // Check to see if the business rules will be creating the object
        // if so the BR will then call setNewObject
        if (businessRules != null && businessRules.canCreateNewDataObject())
        {
            businessRules.createNewObj(true, null);
            
        } else
        {
            if (businessRules != null && mvParent != null) // Bug 9370
            {
                if (mvParent.getMultiViewParent() != null && mvParent.getMultiViewParent().getData() != null)
                {
                    if (!businessRules.isOkToAddSibling(mvParent.getMultiViewParent().getData()))
                    {
                        return;
                    }
                }
            }

            FormDataObjIFace newObject;
            if (classToCreate != null)
            {
                newObject = FormHelper.createAndNewDataObj(classToCreate, businessRules);
            } else
            {
                newObject = FormHelper.createAndNewDataObj(view.getClassName(), businessRules);
            }
            setNewObject(newObject);
        }
    }
    
    /**
     * @param newDataObj
     */
    public void setupAsOrderable(final FormDataObjIFace newDataObj)
    {
        if (newDataObj instanceof Orderable)
        {
            // They really should all be Orderable, 
            // but just in case we check each one.
            int maxOrder = -1;
            for (Object listObj : dataSet)
            {
                if (listObj instanceof Orderable)
                {
                    maxOrder = Math.max(((Orderable)listObj).getOrderIndex(), maxOrder);
                }
            }
            ((Orderable)newDataObj).setOrderIndex(maxOrder+1);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setNewObject(edu.ku.brc.af.ui.forms.FormDataObjIFace)
     */
    @Override
    public void setNewObject(final FormDataObjIFace newDataObj)
    {
        // get an edit dialog for the object
        ViewBasedDisplayIFace dialog = FormHelper.createDataObjectDialog(mainComp, newDataObj, true, true);
        if (dialog == null)
        {
            log.error("Unable to create a dialog for data entry.  [" + newDataObj.getClass().getName() + "]");
            return;
        }
        
        if (mvParent != null)
        {
            mvParent.registerDisplayFrame(dialog);
        }
        
        setupAsOrderable(newDataObj);
        
        dialog.setData(newDataObj);
        dialog.showDisplay(true);
        
        if (dialog.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            dialog.getMultiView().getDataFromUI();
            
            log.warn("User clicked OK.  Adding " + newDataObj.getIdentityTitle() + " into " + dataSetFieldName + ".");
            parentDataObj.addReference(newDataObj, dataSetFieldName);
            iconTray.addItem(newDataObj);

            rootHasChanged();
            
        } else if (dialog.isCancelled())
        {
            if (mvParent.getMultiViewParent() != null && mvParent.getMultiViewParent().getCurrentValidator() != null)
            {
                mvParent.getMultiViewParent().getCurrentValidator().validateForm();
            }
        }
        dialog.dispose();

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
                createNewDataObject();
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
            Object[] delBtnLabels = {getResourceString("Delete"), getResourceString("CANCEL")};
            int rv = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                    UIRegistry.getLocalizedMessage("ASK_DELETE", dataObj.getIdentityTitle()),
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
     * @see edu.ku.brc.af.ui.forms.Viewable#getName()
     */
    public String getName()
    {
        return "Icon View";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getId()
     */
    public int getId()
    {
        return -1;//tableViewDef.getId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getType()
     */
    public ViewDef.ViewType getType()
    {
        return viewDef.getType();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getUIComponent()
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
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getLabelById(java.lang.String)
     */
    public JLabel getLabelFor(final String id)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getControlMapping()
     */
    public Map<String, Component> getControlMapping()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getValidator()
     */
    public FormValidator getValidator()
    {
        return validator;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setDataObj(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void setDataObj(Object dataObj)
    {
        if (AppContextMgr.isSecurityOn() && dataObj != null)
        {
            if (perm == null)
            {
                String shortClassName = MultiView.getClassNameFromParentMV(dataClass, mvParent, cellName);
                if (StringUtils.isEmpty(shortClassName))
                {
                    if (classToCreate == null)
                    {
                        try
                        {
                            shortClassName = Class.forName(view.getClassName()).getSimpleName();
                            
                        } catch (Exception ex) 
                        {
                            shortClassName = dataObj.getClass().getSimpleName();
                        }
                    } else
                    {
                        shortClassName = classToCreate.getSimpleName();
                    }
                }
                perm = SecurityMgr.getInstance().getPermission("DO."+shortClassName.toLowerCase());            
            }
            
            if ((isEditing && perm.isViewOnly()) || (!isEditing && !perm.canView()))
            {
                return;
            }
        }
        
        if (dataObj instanceof Set)
        {
            dataSet = (Set<Object>)dataObj;
            
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
                    dataSet.addAll((List<Object>)dataObj);
                    
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
        return dataSet;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setParentDataObj(java.lang.Object)
     */
    public void setParentDataObj(Object parentDataObj)
    {
        if (parentDataObj != null && !(parentDataObj instanceof FormDataObjIFace))
        {
            dataTypeError = true;
            return;
        }
        this.parentDataObj = (FormDataObjIFace)parentDataObj;
        
        adjustDataClassName();
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
     * @see edu.ku.brc.af.ui.forms.Viewable#getControllerPanel()
     */
    @Override
    public JComponent getControllerPanel()
    {
        return sepController;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getDataFromUI()
     */
    public void getDataFromUI()
    {
        if (dataTypeError)
        {
            return;
        }
        
        dataSet.clear();
        Set<Object> iconTrayItems = iconTray.getItems();
        for (Object fdo: iconTrayItems)
        {
            dataSet.add(fdo);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getDataFromUIComp(java.lang.String)
     */
    public Object getDataFromUIComp(final String name)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setDataIntoUIComp(java.lang.String, java.lang.Object)
     */
    public void setDataIntoUIComp(final String name, Object data)
    {
        // do nothing
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getSubView(java.lang.String)
     */
    public MultiView getSubView(final String name)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getFieldIds(java.util.List)
     */
    public void getFieldIds(final List<String> fieldIds)
    {
        // do nothing
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
        if (switcherUI != null)
        {
            switcherUI.set(altView);
        }
        
        if (!show)
        {
            iconTray.setSelectedIndex(-1);
            if (mvParent != null)
            {
                mvParent.shutdownDisplayFrames();
            }
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#hideMultiViewSwitch(boolean)
     */
    public void hideMultiViewSwitch(boolean hide)
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#enableMultiViewSwitch(boolean)
     */
    public void enableMultiViewSwitch(boolean enabled)
    {
        if (switcherUI != null)
        {
            switcherUI.setEnabled(enabled);   
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#dataHasChanged()
     */
    public void validationWasOK(boolean wasOK)
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setSession(edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    public void setSession(final DataProviderSessionIFace session)
    {
        this.session = session;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setHasNewData(boolean)
     */
    public void setHasNewData(final boolean isNewForm)
    {
        // this gives you the opportunity to adjust your UI
    }


    /**
     * Uses the parentDataObj to adjust the dataClassName
     */
    protected void adjustDataClassName()
    {
        if (parentDataObj == null)
        {
            this.dataClassName = viewDef.getClassName();
        }
        else
        {
            DBTableInfo parentTI = DBTableIdMgr.getInstance().getByClassName(parentDataObj.getClass().getName());
            if (parentTI != null)
            {
                DBRelationshipInfo rel = parentTI.getRelationshipByName(cellName);
                if (rel != null)
                {
                    this.dataClassName = rel.getClassName();
                }
            }
            // At this point 'this.dataClassName' could be null if 'rel' was null
            // which happens when the IconViewObj is embedded in a subform
            // This doesn't seem to matter
        }
        
        checkOrderableness();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setCellName(java.lang.String)
     */
    public void setCellName(String cellName)
    {
        this.cellName = cellName;
        this.dataSetFieldName = cellName;
        
        adjustDataClassName();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#registerSaveBtn(javax.swing.JButton)
     */
    public void registerSaveBtn(JButton saveBtn)
    {
        // TODO: ???
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
        mvParent      = null;
        mainComp      = null;
        
        if (businessRules != null)
        {
            businessRules.formShutdown();
            businessRules = null;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#getSaveBtn()
     */
    public JComponent getSaveComponent()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.Viewable#setSkippingAttach(boolean)
     */
    @Override
    public void setSkippingAttach(boolean isSkippingAttach)
    {
        this.isSkippingAttach = isSkippingAttach;
    }

    /**
     * @return the isAlwaysGetDataFromUI
     */
    @Override
    public boolean isAlwaysGetDataFromUI()
    {
        return isAlwaysGetDataFromUI;
    }

    /**
     * @param isAlwaysGetDataFromUI the isAlwaysGetDataFromUI to set
     */
    @Override
    public void setAlwaysGetDataFromUI(boolean isAlwaysGetDataFromUI)
    {
        this.isAlwaysGetDataFromUI = isAlwaysGetDataFromUI;
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
