/* Filename:    $RCSfile: FormViewObj.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui.forms;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.ColorChooser;
import edu.ku.brc.specify.ui.ColorWrapper;
import edu.ku.brc.specify.ui.DropDownButtonStateful;
import edu.ku.brc.specify.ui.GetSetValueIFace;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.db.JAutoCompComboBox;
import edu.ku.brc.specify.ui.db.PickListItem;
import edu.ku.brc.specify.ui.forms.persist.AltView;
import edu.ku.brc.specify.ui.forms.persist.FormCell;
import edu.ku.brc.specify.ui.forms.persist.FormCellField;
import edu.ku.brc.specify.ui.forms.persist.FormCellLabel;
import edu.ku.brc.specify.ui.forms.persist.FormCellSubView;
import edu.ku.brc.specify.ui.forms.persist.FormViewDef;
import edu.ku.brc.specify.ui.forms.persist.View;
import edu.ku.brc.specify.ui.forms.persist.ViewDef;
import edu.ku.brc.specify.ui.validation.DataChangeNotifier;
import edu.ku.brc.specify.ui.validation.FormValidator;
import edu.ku.brc.specify.ui.validation.FormValidatorInfo;
import edu.ku.brc.specify.ui.validation.UIValidatable;
import edu.ku.brc.specify.ui.validation.UIValidator;
import edu.ku.brc.specify.ui.validation.ValidationListener;

/**
 * Implmentation of the Viewable interface for the ui and this derived class is for handling Form's Only (not tables)
 *
 *
 * @author rods
 *
 */
public class FormViewObj implements Viewable, ValidationListener, ResultSetControllerListener, PreferenceChangeListener
{
    private static Log log = LogFactory.getLog(FormViewObj.class);

    // Static Data Members
    protected static Object[]               formattedValues = new Object[2];
    protected static SimpleDateFormat       scrDateFormat   = null;
    protected static ColorWrapper           viewFieldColor  = null;
    protected static CellConstraints        cc              = new CellConstraints();

    // Data Members
    protected boolean                       isEditting     = false;
    protected boolean                       formIsInNewDataMode = false; // when this is true it means the form was cleared and new data is expected
    protected MultiView                     mvParent       = null;
    protected View                          view;
    protected AltView                       altView;
    protected FormViewDef                   formViewDef;
    protected Component                     formComp       = null;
    protected List<MultiView>               kids           = new ArrayList<MultiView>();
    protected Vector<AltView>               altViewsList   = null; 

    protected Map<String, FieldInfo>        controlsById   = new Hashtable<String, FieldInfo>();
    protected Map<String, FieldInfo>        controlsByName = new Hashtable<String, FieldInfo>();
    protected Map<String, FieldInfo>        labels         = new Hashtable<String, FieldInfo>(); // ID is the Key

    protected FormValidator                 formValidator   = null;
    protected Object                        parentDataObj   = null;
    protected Object                        dataObj         = null;
    protected Set                           origDataSet     = null; 
    protected Object[]                      singleItemArray = new Object[1];

    protected JPanel                        mainComp        = null;
    protected ControlBarPanel               controlPanel    = null;
    protected ResultSetController           rsController    = null;
    protected List<Object>                  list            = null;
    protected boolean                       ignoreSelection = false;
    protected JButton                       saveBtn         = null;
    protected JButton                       validationInfoBtn = null;
    protected boolean                       wasNull         = false;
    protected DropDownButtonStateful        altViewUI;

    protected PanelBuilder                  mainBuilder;

    // Carry Forward
    protected CarryForwardInfo              carryFwdInfo    = null;
    protected boolean                       doCarryForward  = false;
    protected Object                        carryFwdDataObj = null;

    /**
     * Constructor with FormView definition
     * @param view the definition of the view
     * @param altView indicates which AltView we will be using
     * @param mvParent the mvParent mulitview
     * @param createRecordSetController indicates that a RecordSet Contoller should be created
     * @param formValidator the form's formValidator
     * @param createViewSwitcher can be used to make sure that the multiview switcher is not created
     */
    public FormViewObj(final View          view,
                       final AltView       altView,
                       final MultiView     mvParent,
                       final FormValidator formValidator,
                       final boolean       createRecordSetController,
                       final boolean       createViewSwitcher)
    {
        this.view        = view;
        this.altView     = altView;
        this.mvParent    = mvParent;
        
        this.formViewDef = (FormViewDef)altView.getViewDef();
        
        setValidator(formValidator);
        
        if (scrDateFormat == null)
        {
            scrDateFormat = PrefsCache.getSimpleDateFormat("ui", "formatting", "scrdateformat");
        }

        Preferences appsNode = UICacheManager.getAppPrefs();
        Preferences prefNode = appsNode.node("ui/formatting");
        prefNode.addPreferenceChangeListener(this);

        boolean addController = mvParent != null && view.getAltViews().size() > 1;
        
        String rowDefs = (mvParent == null ? "p" : "p:g") + (addController ? ",2px,p" : "");

        mainBuilder    = new PanelBuilder(new FormLayout("f:p:g", rowDefs));
        mainComp = mainBuilder.getPanel();

        // We will add the switchable UI if we are mvParented to a MultiView and have multiple AltViews
        if (addController)
        {
            controlPanel = new ControlBarPanel();
            mainBuilder.add(controlPanel, cc.xy(1,3));
            
            List<JComponent> comps = new ArrayList<JComponent>();

            // Now we have a Special case that when when there are only two AltViews and
            // they differ only by Edit & View we hide the switching UI unless
            // we are the root MultiView. This way when switching the Root View all the other views switch
            // (This is because they were created that way. It also makes no sense that while in "View" mode
            // you would want to switch an individual subview to a differe "mode" view than the root).

            if (createViewSwitcher && (!view.isSpecialViewEdit() || mvParent.getMultiViewParent() == null))
            {
                
                ImageIcon[] icons  = new ImageIcon[view.getAltViews().size()];
                String[]    labels = new String[view.getAltViews().size()];
                
                // loop thru and add the AltViews to the comboxbox and make sure that the
                // this form is always at the top of the list.
                altViewsList = new Vector<AltView>();
                int inx = 0;
                for (AltView av : view.getAltViews())
                {
                    if (av == altView)
                    {
                        altViewsList.insertElementAt(av, 0);
                    } else
                    {
                        altViewsList.add(av);
                    }
                }
                
                for (AltView av : altViewsList)
                {
                    labels[inx] = av.getLabel();
                    
                    // This is Sort of Temporary until I get it all figured out
                    if (av.getMode() == AltView.CreationMode.Edit)
                    {
                        icons[inx] = IconManager.getImage("EditForm", IconManager.IconSize.Std16);
                        
                    } else if (av.getViewDef().getType() == ViewDef.ViewType.table)
                    {
                        icons[inx] = IconManager.getImage("Speadsheet", IconManager.IconSize.Std16);
                        
                    } else
                    {
                        icons[inx] = IconManager.getImage("ViewForm", IconManager.IconSize.Std16);
                    }
                    inx++;
                }

                
                altViewUI = new DropDownButtonStateful(labels, icons);
                altViewUI.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae)
                    {
                        mvParent.showView(altViewsList.get(altViewUI.getCurrentIndex()));
                    }
                });

                if (altView.getMode() == AltView.CreationMode.Edit)
                {
                    saveBtn = new JButton(UICacheManager.getResourceString("Save"), IconManager.getImage("Save"));
                    saveBtn.setMargin(new Insets(1,1,1,1));
                    saveBtn.setEnabled(false);
                    saveBtn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae)
                        {
                            saveObject();
                        }
                    });
                    
                    // We want it on the left side of other buttons
                    // so wee need to add it before the Save button
                    addValidationIndicator(comps);

                    comps.add(saveBtn);

                }
                comps.add(altViewUI);
                
            } else if (altView.getMode() == AltView.CreationMode.Edit)
            {
                addValidationIndicator(comps);
            }
            
            controlPanel.addComponents(comps, false); // false -> right side
        }
        
        if (createRecordSetController)
        {
            addRSController();
        }
    }

    /**
     * Returns the CarryForwardInfo Object for the Form
     * @return the CarryForwardInfo Object for the Form
     */
    public CarryForwardInfo getCarryForwardInfo()
    {
        if (carryFwdInfo == null)
        {
            try
            {
                Class classObj = Class.forName(formViewDef.getClassName());
                carryFwdInfo = new CarryForwardInfo(classObj, this, formViewDef);
                
            } catch (ClassNotFoundException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        return carryFwdInfo;
    }
    
    /**
     * Returns whether this form is doing Carry Forward
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
    }

    /**
     * Creates the JButton that displays the current state of the forms validation
     * @param comps the list of control that will be added to the controlbar
     */
    protected void addValidationIndicator(final List<JComponent> comps)
    {
        if (formValidator != null)
        {
            validationInfoBtn = new JButton(IconManager.getImage("ValidationValid"));
            validationInfoBtn.setMargin(new Insets(1,1,1,1));
            validationInfoBtn.setBorder(BorderFactory.createEmptyBorder());
            validationInfoBtn.setFocusable(false);
            validationInfoBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    showValidationInfo();
                }
            });
            comps.add(validationInfoBtn);
        }
    }
    
    /**
     * 
     */
    protected void showValidationInfo()
    {
        if (true)
        {
            FormValidatorInfo formInfo = new FormValidatorInfo(formValidator.getName(), this);
            
            JDialog dialog = new JDialog();
            dialog.setTitle(formValidator.getName());
            PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("p", "p,5px,p"));
            CellConstraints cc = new CellConstraints();
            panelBuilder.add(formInfo, cc.xy(1,1));
            
            class Closer implements ActionListener
            {
                protected JDialog           parent;
                protected FormValidatorInfo formValInfo;
                
                public Closer(final JDialog parent, final FormValidatorInfo formValInfo)
                {
                    this.parent = parent;
                    this.formValInfo = formValInfo;
                }
                public void actionPerformed(ActionEvent e)
                {
                    parent.setVisible(false);
                    formValInfo.cleanUp();
                    parent.dispose();
                    parent      = null;
                    formValInfo = null;
                }
            }

            JButton closeBtn = new JButton("Close");
            panelBuilder.add(ButtonBarFactory.buildOKBar(closeBtn), cc.xy(1,3));
            closeBtn.addActionListener(new Closer(dialog, formInfo));

            dialog.setAlwaysOnTop(true);
            dialog.setContentPane(panelBuilder.getPanel());
            dialog.pack();
            UIHelper.centerAndShow(dialog);
            
        } else
        {
            FormValidatorInfo formInfo = new FormValidatorInfo(formValidator.getName(), this);
            JFrame frame = new JFrame();
            frame.setContentPane(formInfo);
            frame.pack();
            frame.setSize(frame.getPreferredSize());
            UIHelper.centerAndShow(frame);
        }
        
    }
     

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#aboutToShow(boolean)
     */
    public void aboutToShow(final boolean show)
    {
        if (altViewUI != null)
        {
            ignoreSelection = true;
            altViewUI.setCurrentIndex(0);
            ignoreSelection = false;
        }
        for (MultiView mv : kids)
        {
            mv.aboutToShow(show);
        }
    }

    /**
     * Returns the definition of the form
     * @return the definition of the form
     */
    public FormViewDef getFormView()
    {
        return formViewDef;
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
    public List getDataList()
    {
        return list;
    }

    /**
     * Sets the component into the object
     * @param formComp the UI component that represents this viewable
     */
    public void setFormComp(JComponent formComp)
    {
        // Remove existing component
        if (this.formComp != null)
        {
            mainComp.remove(this.formComp);
        }
        this.formComp = formComp;

        // add new component
        mainBuilder.add(formComp, cc.xy(1,1));
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
     * Adds a control by name so it can be looked up later
     * @param formCell the FormCell def that describe the cell
     * @param control the control
     */
    public void addControl(final FormCell formCell, final Component control)
    {
        if (formCell != null)
        {
            if (controlsById.get(formCell.getId()) != null)
            {
                throw new RuntimeException("Two controls have the same id ["+formCell.getId()+"] "+formViewDef.getName());
            }
            
            if (controlsByName.get(formCell.getName()) != null)
            {
                throw new RuntimeException("Two controls have the same name ["+formCell.getName()+"] "+formViewDef.getName());
            }
            
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
            if (control instanceof MultiView)
            {
                int x = 0;
                x++;
            }
            FieldInfo fieldInfo = new FieldInfo(formCell, comp, scrollPane, controlsById.size());
            controlsById.put(formCell.getId(), fieldInfo);
            controlsByName.put(formCell.getName(), fieldInfo);

        }
    }


    /**
     * Adds a control by name so it can be looked up later
     * @param formCell the FormCell def that describe the cell
     * @param control the control
     */
    public void addLabel(final FormCellLabel formCell, final JLabel label)
    {
        
        if (formCell != null)
        {
            if (labels.get(formCell.getLabelFor()) != null)
            {
                throw new RuntimeException("Two labels have the same id ["+formCell.getLabelFor()+"] "+formViewDef.getName());
            }
            labels.put(formCell.getLabelFor(), new FieldInfo(formCell, label, null, labels.size()));
        }
    }

    /**
     * Sets the multiview if it is owned or mvParented by it
     * @param cbx cobobox to add a listener to
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
                if (!ignoreSelection)
                {
                     mv.showView((AltView)((JComboBox)ae.getSource()).getSelectedItem());
                }
            }
        };

        if (cbx != null)
        {
            cbx.addActionListener(new MVActionListener(mvParent));
        }
    }

    /**
     * Adds a control by name so it can be looked up later
     * @param formCell the FormCell def that describe the cell
     * @param subView the subView
     */
    public void addSubView(final FormCell formCell, final MultiView subView)
    {
        if (formCell != null)
        {
            if (controlsById.get(formCell.getId()) != null)
            {
                throw new RuntimeException("Two controls have the same id ["+formCell.getId()+"] "+formViewDef.getName());
            }
            
            controlsById.put(formCell.getId(), new FieldInfo(formCell, subView, controlsById.size()));
            kids.add(subView);
        }
    }

    /**
     * Se the form formValidator
     * @param formValidator the formValidator
     */
    protected void setValidator(final FormValidator formValidator)
    {
        this.formValidator = formValidator;
        
        if (formValidator != null && mvParent != null)
        {
            formValidator.addValidationListener(this);
            
            //log.info(formViewDef.getName()+ " formValidator: "+formValidator);
    
            if (mvParent != null)
            {
                MultiView root = mvParent;
                while (root.getMultiViewParent() != null)
                {
                    root = root.getMultiViewParent();
                }
                formValidator.addValidationListener(root);
                root.addFormValidator(formValidator);
            }
        }
    }
    
    /**
     * Adds new child object to its parent to a Set
     * @param newDataObj the new object to be added to a Set
     */
    protected void initAndAddToParent(final Object newDataObj)
    {
        if (parentDataObj != null)
        {
            String methodName = "add" + newDataObj.getClass().getSimpleName();
            log.info("Invoking method["+methodName+"]");
            try
            {
                Method method = newDataObj.getClass().getMethod("initialize", new Class[] {});
                method.invoke(newDataObj, new Object[] {});
                
                method = parentDataObj.getClass().getMethod(methodName, new Class[] {newDataObj.getClass()});
                method.invoke(parentDataObj, new Object[] {newDataObj});
                
            } catch (NoSuchMethodException ex)
            {
                ex.printStackTrace();
                
            } catch (IllegalAccessException ex)
            {
                ex.printStackTrace();   
                
            } catch (InvocationTargetException ex)
            {
                ex.printStackTrace();    
            }
        }
    }
    
    /**
     * Adds new child object to its parent to a Set
     * @param newDataObj the new object to be added to a Set
     */
    protected void removeFromParent(final Object oldDataObj)
    {
        if (parentDataObj != null)
        {
            String methodName = "remove" + oldDataObj.getClass().getSimpleName();
            try
            {
                Method method = parentDataObj.getClass().getMethod(methodName, new Class[] {oldDataObj.getClass()});
                method.invoke(parentDataObj, new Object[] {oldDataObj});
                
            } catch (NoSuchMethodException ex)
            {
                ex.printStackTrace();
                
            } catch (IllegalAccessException ex)
            {
                ex.printStackTrace();   
                
            } catch (InvocationTargetException ex)
            {
                ex.printStackTrace();    
            }
        }
    }
    
    /**
     * Walks the MultiView hierarchy and has them transfer their data from the UI to the DB Object
     * @param parentMV the parent MultiView
     */
    protected void traverseToGetDataFromForms(final MultiView parentMV)
    {
        for (MultiView mv : parentMV.getKids())
        {
            mv.getDataFromUI();
            traverseToGetDataFromForms(mv);
        }
    }
    
    /**
     * Walks the MultiView hierarchy and ctells all the Viewables in each MultiView
     * that the form is new
     * @param parentMV the parent MultiView
     * @param isNewForm wheather the form is now in "new data input" mode
     */
    protected void traverseToToSetAsNew(final MultiView parentMV, final boolean isNewForm)
    {
        parentMV.setIsNewForm(isNewForm);
        for (MultiView mv : parentMV.getKids())
        {
            mv.setIsNewForm(isNewForm);
        }
    }
    
    /**
     * Creates a new Record and adds it to the List and dataSet if necessary
     */
    @SuppressWarnings("unchecked")
    protected void createNewRecord()
    {
        log.info("createNewRecord " + this.getView().getName());
        try
        {
            Class  classObj = Class.forName(view.getClassName());
            Object obj      = classObj.newInstance();
            UIHelper.initAndAddToParent(parentDataObj, obj);
            
            if (carryFwdDataObj == null && dataObj != null)
            {
                carryFwdDataObj = dataObj;
            }
            
            if (doCarryForward && carryFwdDataObj != null  && carryFwdInfo != null)
            {
                carryFwdInfo.carryForward(carryFwdDataObj, obj);
            }
            
            dataObj = obj;
                
            if (list != null)
            {
                list.add(obj);
                rsController.setLength(list.size());
                rsController.setIndex(list.size()-1);
            }
            setAsNewForm(true);
            
            this.setDataIntoUI();

            if (formValidator != null)
            {
                formValidator.validateForm();
            }

            
        } catch (ClassNotFoundException ex)
        {
            ex.printStackTrace();
            
        } catch (IllegalAccessException ex)
        {
            ex.printStackTrace();   
            
        } catch (InstantiationException ex)
        {
            ex.printStackTrace();            
        }
    }
    
    
    /**
     * Save any changes to the current object
     */
    protected void saveObject()
    {
        try
        {
            HibernateUtil.beginTransaction();
            
            this.getDataFromUI();
            
            traverseToGetDataFromForms(mvParent);
            
            HibernateUtil.getCurrentSession().saveOrUpdate(dataObj);
            HibernateUtil.commitTransaction();
            
            formIsInNewDataMode = false;
            traverseToToSetAsNew(mvParent, false);
            
            if (doCarryForward)
            {
                carryFwdDataObj = dataObj;
            }

            
        } catch (Exception e)
        {
            log.error("******* " + e);
            e.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }
        saveBtn.setEnabled(false);
    }

    /**
     * Save any changes to the current object
     */
    protected void removeObject()
    {
        try
        {
            removeFromParent(dataObj);
            
            HibernateUtil.beginTransaction();
            HibernateUtil.getCurrentSession().delete(dataObj);
            HibernateUtil.commitTransaction();
            
            if (rsController != null)
            {
                int currInx = rsController.getCurrentIndex();
                list.remove(dataObj);
                rsController.setLength(rsController.getLength()-1);
                
                int newInx = Math.min(currInx, rsController.getLength());
                if (newInx > 0)
                {
                    rsController.setIndex(newInx);
                    dataObj = list.get(newInx);
                    this.setDataIntoUI();
                }
            }
            
        } catch (Exception e)
        {
            log.error("******* " + e);
            e.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }
    }
    
    /**
     * Tells this form and all of it's children that it is a "new" form for data entry
     * @param isNewForm true is new, false is not
     */
    public void setAsNewForm(final boolean isNewForm)
    {
        formIsInNewDataMode = isNewForm;
        traverseToToSetAsNew(mvParent, isNewForm);
    }

    /**
     * Returns the list of MultiView kids (subforms)
     * @return the list of MultiView kids (subforms)
     */
    public List<MultiView> getKids()
    {
        return kids;
    }
    
    public void listFieldChanges()
    {
        if (formValidator != null)
        {
            System.out.println("=================================== "+formValidator.getDCNs().values().size());
            for (DataChangeNotifier dcn : formValidator.getDCNs().values())
            {
                FieldInfo fieldInfo = controlsById.get(dcn.getId());
                if (dcn.isDataChanged())
                {
                    System.out.println("Changed Field["+fieldInfo.getName()+"]");
                }
            }
            System.out.println("===================================");
        }
    }
    
    /**
     * Sets the focus to the first control in the form
     */
    protected void focusFirstFormControl()
    {
        for (FieldInfo compFI : controlsById.values())
        {
            if (compFI.getInsertPos() == 0)
            {
                compFI.getComp().requestFocus();
            }
        }
    }
    
    /**
     * Adds the RecordSetController to the panel 
     */
    protected void addRSController()
    {
        // If the Control panel doesn't exist, then add it
        if (rsController == null)
        {
            boolean inEditMode = altView.getMode() == AltView.CreationMode.Edit;
            rsController = new ResultSetController(formValidator, inEditMode, inEditMode, 0);
            rsController.addListener(this);
            controlPanel.add(rsController);
            
            if (rsController.getNewRecBtn() != null)
            {
                rsController.getNewRecBtn().addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae)
                    {
                        createNewRecord();
                        focusFirstFormControl();
                    }
                });
            }
            
            if (rsController.getDelRecBtn() != null)
            {
                rsController.getDelRecBtn().addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae)
                    {
                        removeObject();
                    }
                });
            }
        } 
    }

    /**
     * Cleanup references
     */
    public void cleanUp()
    {
        controlsById.clear();
        controlsByName.clear();
        labels.clear();
        
        // XXX FIXME for (MultiView fvo : kids)
        //{
        //    fvo.cleanUp();
        //}
        mvParent      = null;
        formViewDef = null;
        formComp    = null;
    }

    //-------------------------------------------------
    // Viewable
    //-------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getType()
     */
    public ViewDef.ViewType getType()
    {
        return formViewDef.getType();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getUIComponent()
     */
    public Component getUIComponent()
    {
        return mainComp;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#isSubform()
     */
    public boolean isSubform()
    {
        return mvParent != null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getCompById(java.lang.String)
     */
    public Component getCompById(final String id)
    {
        FieldInfo fi = controlsById.get(id);
        if (fi != null)
        {
            return fi.getComp();
        } else
        {
            throw new RuntimeException("Couldn't find FieldInfo for ID["+id+"]");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getControlMapping()
     */
    public Map<String, Component> getControlMapping()
    {
        Map<String, Component> map = new Hashtable<String, Component>();
        for (FieldInfo fieldInfo : controlsById.values())
        {
            map.put(fieldInfo.getId(), fieldInfo.getComp());
        }
        return map;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getValidator()
     */
    public FormValidator getValidator()
    {
        return formValidator;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#setDataObj(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void setDataObj(final Object dataObj)
    {
        // We really shouldn't get here.
        // This condition is true only if a new Object (record) was being entered and some how the user was 
        // able to go to a previous or next record before saving or discarding the new info
        if (formIsInNewDataMode)
        {
            setAsNewForm(false);
            throw new RuntimeException("Shouldn't have gotten here! Why wasn't the object saved or discarded?");
        }
        
        // Convert the Set over to a List so the RecordController can be used
        Object data = dataObj;
        if (data instanceof java.util.Set)
        {
            origDataSet = (Set)dataObj;
            data = Collections.list(Collections.enumeration(origDataSet));
        }

        // If there is a formValidator then we set the current object into the formValidator's scripting context
        // then turn off change notification while the form is filled
        if (formValidator != null)
        {
            formValidator.addRuleObjectMapping("dataObj", dataObj);
        }

        // if we do have a list then get the first object or null
        if (data instanceof List)
        {
            list = (List)data;
            if (list.size() > 0)
            {
                this.dataObj = list.get(0);
            } else
            {
                this.dataObj = null;
            }

            // Now tell the RecordController how many Object we have
            if (rsController != null)
            {
                rsController.setLength(list.size());
            }

            // Set the data from the into the form 
            setDataIntoUI();

        } else
        {
            // OK, it is a single data object
            this.dataObj = dataObj;
            this.list    = null;
            
            setDataIntoUI();

            // Don't remove the rsController if the data is NULL because the next non-null one may be a list
            // mostly likely it will be
            if (this.dataObj != null && rsController != null)
            {
                controlPanel.setRSCVisibility(!isEditting);
            }
        }

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getDataObj()
     */
    public Object getDataObj()
    {
        log.info("getDataObj " + this.getView().getName());
        return dataObj;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#setParentDataObj(java.lang.Object)
     */
    public void setParentDataObj(Object parentDataObj)
    {
        this.parentDataObj = parentDataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getParentDataObj()
     */
    public Object getParentDataObj()
    {
        return parentDataObj;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#setDataIntoUI()
     */
    public void setDataIntoUI()
    {
        // Now turn off data change notification and then validate the form
        if (formValidator != null)
        {
            formValidator.setDataChangeNotification(false);
        }
        
        boolean weHaveData = true;
        
        DataObjectGettable dg = formViewDef.getDataGettable();
        
        // This is a short circut for when we were switch from being enabled to disabled or visa-versus
        // This way we won't need to set the controls enabled or disabled each time we advance to a new record,
        // we only have to do it once
        if (dataObj == null && !wasNull)
        {
            // Disable all the labels
            for (FieldInfo labelFI : labels.values())
            {
                labelFI.getComp().setEnabled(false);
            }
            
            // Diable all the form controls and set their values to NULL
            for (FieldInfo fieldInfo : controlsById.values())
            {
                fieldInfo.getComp().setEnabled(false);
                if (fieldInfo.getFormCell().getType() == FormCell.CellType.field)
                {
                    setDataIntoUIComp(fieldInfo.getComp(), null, null);
                    //System.out.println("Setting ["+fieldInfo.getName()+"] to enabled=false");
                    
                } else if (fieldInfo.getFormCell().getType() == FormCell.CellType.subview)
                {
                    fieldInfo.getSubView().setData(null);
                }
            }
            // Disable the RecordSet Controller
            if (rsController != null)
            {
                rsController.setEnabled(false);
            }
            wasNull    = true;
            weHaveData = false;
            
        } else if (dataObj != null && wasNull)
        {
            // Enable the labels
            for (FieldInfo labelFI : labels.values())
            {
                labelFI.getComp().setEnabled(true);
            }
            
            // Enable the formn controls
            for (FieldInfo compFI : controlsById.values())
            {
                compFI.setEnabled(true);
            }
            
            // Enable the RecordSet Controller
            if (rsController != null)
            {
                rsController.setEnabled(true);
            }
            wasNull = false; 
        }


        if (weHaveData)
        {
            // Now we know the we have data, so loop through all the controls 
            // and set their values
            for (FieldInfo fieldInfo : controlsById.values())
            {
                Component comp = fieldInfo.getComp();
    
                Object data = null;
    
                // This is for panels that use in layout but have no data
                if (fieldInfo.getFormCell().isIgnoreSetGet())
                {
                    continue;
                }
    
                if (fieldInfo.getFormCell().getType() == FormCell.CellType.field)
                {
                    // Do Formatting here
                    FormCellField cellField    = (FormCellField)fieldInfo.getFormCell();
                    String        formatName   = cellField.getFormatName();
                    String        defaultValue = cellField.getDefaultValue();
    
                    boolean isTextFieldPerMode = cellField.isTextField(altView.getMode());
                    
                    boolean useFormatName = isTextFieldPerMode && isNotEmpty(formatName);
                    //System.out.println("["+cellField.getName()+"] "+useFormatName+"  "+comp.getClass().getSimpleName());
    
                    if (useFormatName)
                    {
                        if (cellField.getFieldNames().length > 1)
                        {
                            throw new RuntimeException("formatName ["+formatName+"] only works on a single value.");
                        }
                        Object[] vals = UIHelper.getFieldValues(cellField.getFieldNames(), dataObj, dg);
                        setDataIntoUIComp(comp, DataObjFieldFormatMgr.format(vals[0], formatName), defaultValue);
    
                    } else
                    {
    
                        Object[] values = UIHelper.getFieldValues(cellField, dataObj, dg);
                        String   format = cellField.getFormat();
                        if (isNotEmpty(format))
                        {
                            setDataIntoUIComp(comp, UIHelper.getFormattedValue(values, cellField.getFormat()), defaultValue);
    
                        } else
                        {
                            if (cellField.getFieldNames().length > 1)
                            {
                                throw new RuntimeException("No Format but mulitple fields were specified for["+cellField.getName()+"]");
                            }
                            
                            if (isTextFieldPerMode)
                            {
                                setDataIntoUIComp(comp, values != null && values[0] != null ? values[0].toString() : "", defaultValue);
                                
                            } else
                            {
                                setDataIntoUIComp(comp, values == null ? null : values[0], defaultValue);
                            }
                            
                        }
    
                    }
    
                } else if (fieldInfo.getFormCell().getType() == FormCell.CellType.subview)
                {
                    data = dg != null ? dg.getFieldValue(dataObj, fieldInfo.getName()) : null;
                    if (data != null)
                    {
                        if (((FormCellSubView)fieldInfo.getFormCell()).isSingleValueFromSet() && data instanceof Set)
                        {
                            Set set = (Set)data;
                            if (set.size() > 0)
                            {
                                data = set.iterator().next();
                            }
                        }
                        fieldInfo.getSubView().setData(data);
                    }
                    fieldInfo.getSubView().setParentDataObj(dataObj);
                }
            }
        }
        
        System.out.println(formViewDef.getName());
        
        // Adjust the formValidator now that all the data is in the controls
        if (formValidator != null)
        {
            formValidator.setHasChanged(false);
            
            formValidator.resetFields();
            
            formValidator.setDataChangeNotification(true); // this doesn't effect validation notifications
            
            formValidator.validateForm();
            
            //formValidator.resetFields();
            
            this.listFieldChanges();
        }
        
        if (mvParent != null && mvParent.isRoot() && saveBtn != null)
        {
            saveBtn.setEnabled(false);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getDataFromUI()
     */
    public void getDataFromUI()
    {
        DataObjectSettable ds = formViewDef.getDataSettable();
        DataObjectGettable dg = formViewDef.getDataGettable();
        if (ds != null)
        {
            for (FieldInfo fieldInfo : controlsById.values())
            {
                FormCell fc = fieldInfo.getFormCell();
                boolean isReadOnly = false;
                
                if (fc instanceof FormCellField)
                {
                    isReadOnly = ((FormCellField)fieldInfo.getFormCell()).isReadOnly();
                }
                
                if (isReadOnly || fc.isIgnoreSetGet())
                {
                    continue;
                }
                String id   = fieldInfo.getFormCell().getId();
                Object uiData = getDataFromUIComp(id); // if ID is null then we have huge problems
                if (uiData != null)
                {
                    //log.info(fieldInfo.getFormCell().getName());
                    UIHelper.setFieldValue(fieldInfo.getFormCell().getName(), dataObj, uiData, dg, ds);
                }
            }
        } else
        {
            throw new RuntimeException("Calling getDataFromUI when the DataObjectSettable is null for the form.");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getDataFromUIComp(java.lang.String)
     */
    public Object getDataFromUIComp(final String id)
    {
        FieldInfo fieldInfo = controlsById.get(id);
        if (fieldInfo != null)
        {
            Component comp = fieldInfo.getComp();
            if (comp != null)
            {
                if (comp instanceof GetSetValueIFace)
                {
                    return ((GetSetValueIFace)comp).getValue();

                } else if (comp instanceof MultiView)
                {
                    if (((FormCellSubView)fieldInfo.getFormCell()).isSingleValueFromSet())
                    {
                        return ((MultiView)comp).getData();
                    } else
                    {
                        return null;
                    }

                } else if (comp instanceof JTextField)
                {
                    return ((JTextField)comp).getText();

                } else if (comp instanceof JComboBox)
                {
                    if (comp instanceof JAutoCompComboBox)
                    {
                        PickListItem pli = (PickListItem)((JAutoCompComboBox)comp).getSelectedItem();
                        return pli.getValue();

                    } else
                    {
                        return ((JComboBox)comp).getSelectedItem().toString();
                    }

                } else if (comp instanceof JLabel)
                {
                    return ((JLabel)comp).getText();

                } else if (comp instanceof ColorChooser)
                {
                    return ColorWrapper.toString(((ColorChooser)comp).getBackground());

                } else if(comp instanceof JList)
                {
                    return ((JList)comp).getSelectedValue().toString();

                } else if(comp instanceof JCheckBox)
                {
                    return new Boolean(((JCheckBox)comp).isSelected());

               } else
               {
                    log.error("Not sure how to get data from object "+comp);
               }
            } else
            {
                log.error("Component is null in FieldInfo "+id);
            }
        } else
        {
            log.error("FieldInfo is null "+id);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getSubView(java.lang.String)
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
     * @see edu.ku.brc.specify.ui.forms.Viewable#setDataIntoUIComp(java.lang.String, java.lang.Object)
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
    public void setDataIntoUIComp(final Component comp, final Object data, final String defaultValue)
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
            //System.out.println(name+" - "+comp.getPreferredSize()+comp.getSize());
            ((JTextArea)comp).setText(data == null ? "" : data.toString());

        } else if (comp instanceof JCheckBox)
        {
            //System.out.println(name+" - "+comp.getPreferredSize()+comp.getSize());
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
    protected void setComboboxValue(final JComboBox comboBox, final Object data)
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
     * Sets the appropriate ndex in the list box
     * @param list the list box
     * @param data the data value
     */
    protected void setListValue(final JList list, final Object data)
    {

        Iterator iter = null;
        if (data instanceof Set)
        {
            iter = ((Set)data).iterator();

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
     * @see edu.ku.brc.specify.ui.forms.Viewable#getView()
     */
    public View getView()
    {
        return view;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#hideMultiViewSwitch(boolean)
     */
    public void hideMultiViewSwitch(boolean hide)
    {
        if (altViewUI != null)
        {
            altViewUI.setVisible(!hide);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#dataHasChanged()
     */
    public void validationWasOK(boolean wasOK)
    {
       if (saveBtn != null)
       {
           saveBtn.setEnabled(wasOK);
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
        ImageIcon icon = IconManager.getImage("ValidationValid");
        UIValidatable.ErrorType state = formValidator.getState();
        
        //log.info(state);
        
        if (state == UIValidatable.ErrorType.Incomplete)
        {
            icon = IconManager.getImage("ValidationWarning");
            
        } else if (state == UIValidatable.ErrorType.Error)
        {
            icon = IconManager.getImage("ValidationError");
        }
        validationInfoBtn.setIcon(icon);
    }

    //-------------------------------------------------
    // ResultSetControllerListener
    //-------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.ResultSetControllerListener#indexChanged(int)
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
     * @see edu.ku.brc.specify.ui.forms.ResultSetControllerListener#indexAboutToChange(int, int)
     */
    public boolean indexAboutToChange(int oldIndex, int newIndex)
    {
        if (formValidator != null && formValidator.hasChanged())
        {
            getDataFromUI();
        }
        return true;
    }

    //-------------------------------------------------
    // PreferenceChangeListener
    //-------------------------------------------------

    protected void setColorOnControls(final int colorType, final Color color)
    {
        for (FieldInfo fieldInfo : controlsById.values())
        {
            if (fieldInfo.getFormCell().getType() == FormCell.CellType.field)
            {
                FormCellField cellField = (FormCellField)fieldInfo.getFormCell();
                String uiType = cellField.getUiType();
                //log.info("["+uiType+"]");

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

                        /*case 1 : {
                            if (comp instanceof )
                            //fieldInfo.getComp().setBackground(color);
                        } break;*/
                    }
                }
            }
        }

    }

    /* (non-Javadoc)
     * @see java.util.prefs.PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
     */
    public void preferenceChange(PreferenceChangeEvent evt)
    {
        Preferences pref = evt.getNode();

        if (evt.getKey().equals("viewfieldcolor"))
        {
            ColorWrapper viewFieldColor = PrefsCache.getColorWrapper("ui", "formatting", "viewfieldcolor");
            Color vfColor = viewFieldColor.getColor();

            setColorOnControls(0, vfColor);
        }
        log.info("Pref: ["+evt.getKey()+"]["+pref.get(evt.getKey(), "XXX")+"]");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getFieldIds(java.util.List)
     */
    public void getFieldIds(final List<String> fieldIds)
    {
        for (FieldInfo fieldInfo : controlsById.values())
        {
            if (fieldInfo.getFormCell().getType() == FormCell.CellType.field)
            {
                fieldIds.add(((FormCellField)fieldInfo.getFormCell()).getId());
            }
        }

    }

    //-------------------------------------------------
    // FieldInfo
    //-------------------------------------------------
    class FieldInfo
    {
        protected FormCell    formCell;
        protected MultiView   subView;
        protected Component   comp;
        protected JScrollPane scrollPane;
        protected int         insertPos;

        public FieldInfo(FormCell formCell, Component comp, JScrollPane scrollPane, int insertPos)
        {
            this.comp     = comp;
            this.formCell = formCell;
            this.subView  = null;
            this.scrollPane = scrollPane;
            this.insertPos = insertPos;
        }

        public FieldInfo(FormCell formCell, MultiView subView, int insertPos)
        {
            this.formCell = formCell;
            this.subView  = subView;
            this.comp     = subView;
            this.insertPos = insertPos;
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
        public FormCell getFormCell()
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

        public void setEnabled(boolean enabled)
        {
            //log.info(formCell.getName()+"  "+(scrollPane != null ? "has Pane" : "no pane"));
            comp.setEnabled(enabled);
            if (scrollPane != null)
            {
                scrollPane.setEnabled(enabled);
            }
        }

    }



}
