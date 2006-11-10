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
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.StaleObjectStateException;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.StaleObjectException;
import edu.ku.brc.ui.ColorChooser;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.DropDownButtonStateful;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.JAutoCompComboBox;
import edu.ku.brc.ui.db.PickListItem;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.FormCell;
import edu.ku.brc.ui.forms.persist.FormCellField;
import edu.ku.brc.ui.forms.persist.FormCellLabel;
import edu.ku.brc.ui.forms.persist.FormCellSubView;
import edu.ku.brc.ui.forms.persist.FormViewDef;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.forms.persist.ViewDef;
import edu.ku.brc.ui.validation.DataChangeNotifier;
import edu.ku.brc.ui.validation.FormValidator;
import edu.ku.brc.ui.validation.FormValidatorInfo;
import edu.ku.brc.ui.validation.UIValidatable;
import edu.ku.brc.ui.validation.UIValidator;
import edu.ku.brc.ui.validation.ValidationListener;

/**
 * This implements a Form and is "owed" by a MultiView.<br>
 * <br>
 * Implmentation of the Viewable interface for the ui and this derived class is for handling Form's Only (not tables).<br>
 * <br>
 * Implements ViewBuilderIFace which the ViewFactory uses while processing the rows, it calls methods in this interface
 * to add labels, controls and subforms to the form.<br>
 * <br>
 * Implements ValidationListener so it can listen to any and all validations so it knows how to show and activate the icon button
 * that enables the user to see what the errors are in a form.<br>
 * <br>
 * Implements ResultSetControllerListener to react to the record control bar for moving forward or backward in a resulset.<br>
 * <br>
 * Implements AppPrefsChangeListener to be notified of changes to the BG Required Field color or the date formatting.
 * 
 * @code_status Beta
 *
 * @author rods
 *
 */
public class FormViewObj implements Viewable, 
                                    ViewBuilderIFace, 
                                    ValidationListener, 
                                    ResultSetControllerListener, 
                                    AppPrefsChangeListener
{
    private static final Logger log = Logger.getLogger(FormViewObj.class);

    // Static Data Members
    protected static Object[]               formattedValues = new Object[2];
    protected static ColorWrapper           viewFieldColor  = null;
    protected static CellConstraints        cc              = new CellConstraints();

    // Data Members
    protected DataProviderSessionIFace      session        = null;
    protected boolean                       isEditting     = false;
    protected boolean                       formIsInNewDataMode = false; // when this is true it means the form was cleared and new data is expected
    protected MultiView                     mvParent       = null;
    protected View                          view;
    protected AltView                       altView;
    protected FormViewDef                   formViewDef;
    protected String                        cellName;
    protected Component                     formComp       = null;
    protected List<MultiView>               kids           = new ArrayList<MultiView>();
    protected Vector<AltView>               altViewsList   = null;

    protected Hashtable<String, FieldInfo>  controlsById   = new Hashtable<String, FieldInfo>();
    protected Hashtable<String, FieldInfo>  controlsByName = new Hashtable<String, FieldInfo>();
    protected Hashtable<String, FieldInfo>  labels         = new Hashtable<String, FieldInfo>(); // ID is the Key
    
    protected FormLayout                    formLayout;
    protected PanelBuilder                  builder;

    protected FormValidator                 formValidator   = null;
    protected Object                        parentDataObj   = null;
    protected Object                        dataObj         = null;
    protected Set                           origDataSet     = null;
    protected Object[]                      singleItemArray = new Object[1];
    protected DateWrapper                   scrDateFormat;
    protected int                           options;

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
    protected List<UIValidatable>           defaultValueList = new ArrayList<UIValidatable>();

    protected PanelBuilder                  mainBuilder;
    protected BusinessRulesIFace            businessRules   = null; 

    protected DraggableRecordIdentifier     draggableRecIdentifier   = null;
    
    // Carry Forward
    protected CarryForwardInfo              carryFwdInfo    = null;
    protected boolean                       doCarryForward  = false;
    protected Object                        carryFwdDataObj = null;

    /**
     * Constructor with FormView definition
     * @param view the definition of the view
     * @param altView indicates which AltView we will be using
     * @param mvParent the mvParent mulitview
     * @param createResultSetController indicates that a ResultSet Controller should be created
     * @param formValidator the form's formValidator
     * @param options the options needed for creating the form
     */
    public FormViewObj(final View          view,
                       final AltView       altView,
                       final MultiView     mvParent,
                       final FormValidator formValidator,
                       final int           options)
    {
        this(view, altView, mvParent, formValidator, options, null);
    }
    
    /**
     * Constructor with FormView definition
     * @param view the definition of the view
     * @param altView indicates which AltView we will be using
     * @param mvParent the mvParent mulitview
     * @param createResultSetController indicates that a ResultSet Controller should be created
     * @param formValidator the form's formValidator
     * @param options the options needed for creating the form
     */
    public FormViewObj(final View          view,
                       final AltView       altView,
                       final MultiView     mvParent,
                       final FormValidator formValidator,
                       final int           options,
                       final String        cellName)
    {
        this.view        = view;
        this.altView     = altView;
        this.mvParent    = mvParent;
        this.cellName    = cellName;
        
        businessRules    = view.getBusinessRule();
        isEditting       = altView.getMode() == AltView.CreationMode.Edit;

        this.formViewDef = (FormViewDef)altView.getViewDef();
        
        // Figure columns
        formLayout = new FormLayout(formViewDef.getColumnDef(), formViewDef.getRowDef());
        builder    = new PanelBuilder(formLayout);

        if (mvParent == null)
        {
            builder.getPanel().setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        }
        
        this.options = options;
        boolean createResultSetController  = MultiView.isOptionOn(options, MultiView.RESULTSET_CONTROLLER);
        boolean createViewSwitcher         = MultiView.isOptionOn(options, MultiView.VIEW_SWITCHER);
        boolean isNewObject                = MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT);
        boolean hideSaveBtn                = MultiView.isOptionOn(options, MultiView.HIDE_SAVE_BTN);
        
        formIsInNewDataMode = isNewObject;
        //System.err.println(view.getName()+"  "+formIsInNewDataMode+"  "+options);
        
        MultiView.printCreateOptions("Creating Form "+altView.getName(), options);

        setValidator(formValidator);

        scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");


        AppPreferences.getRemote().addChangeListener("ui.formatting.viewfieldcolor", this);

        boolean addController = mvParent != null && view.getAltViews().size() > 1;

        boolean addExtraRow = addController || altView.getMode() == AltView.CreationMode.Search;
        
        // See if we need to add a Selector ComboBox
        boolean addSelectorCBX = false;
        if (StringUtils.isNotEmpty(view.getSelectorName()) && isNewObject)
        {
            addSelectorCBX = true;
        }

        String rowDefs = (addSelectorCBX ? "t:p," : "") + (mvParent == null ? "t:p" : "t:p:g") + (addExtraRow ? ",2px,t:p" : "");

        mainBuilder = new PanelBuilder(new FormLayout("f:p:g", rowDefs));
        mainComp    = mainBuilder.getPanel();
        mainComp.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        List<JComponent> comps = new ArrayList<JComponent>();

        if (addSelectorCBX)
        {
            mainCompRowInx++;
            
            Vector<String> cbxList = new Vector<String>();
            cbxList.add(altView.getName());
            for (AltView av : view.getAltViews())
            {
                if (av != altView && av.getMode() == AltView.CreationMode.Edit)
                {
                    cbxList.add(av.getName());
                }
            }
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            selectorCBX = new JComboBox(cbxList);
            p.add(selectorCBX, BorderLayout.WEST);
            mainBuilder.add(p, cc.xy(1, 1));
            
            if (mvParent != null)
            {
                selectorCBX.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent ex)
                    {
                        mvParent.showView(((JComboBox)ex.getSource()).getSelectedItem().toString());
                    }
                });
            }
        }
 

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
                switcherUI   = createSwitcher(mvParent, view, altView, altViewsList);
                
                if (altViewsList.size() > 0)
                {
                    if (altView.getMode() == AltView.CreationMode.Edit && mvParent != null && mvParent.isTopLevel())
                    {
                        // We want it on the left side of other buttons
                        // so wee need to add it before the Save button
                        addValidationIndicator(comps);
    
                        addSaveBtn();
                        comps.add(saveBtn);
                        saveWasAdded = true;
    
                    }
                    comps.add(switcherUI);
                }
            }
            
            if (!saveWasAdded && altView.getMode() == AltView.CreationMode.Edit)
            {
                if (mvParent != null && mvParent.isTopLevel() && !hideSaveBtn)
                {
                    addSaveBtn();
                    comps.add(saveBtn);
                }
                addValidationIndicator(comps);
            }
        }

        // This here because the Seach mode shouldn't be combined with other modes
        if (altView.getMode() == AltView.CreationMode.Search)
        {
            if (!hideSaveBtn)
            {
                saveBtn = new JButton(UICacheManager.getResourceString("Search"), IconManager.getImage("Search", IconManager.IconSize.Std16));
                comps.add(saveBtn);
            }

        }

        if (comps.size() > 0 || addController)
        {
            controlPanel = new ControlBarPanel();
            controlPanel.addComponents(comps, false); // false -> right side
            mainBuilder.add(controlPanel, cc.xy(1, mainCompRowInx+2));
        }

        if (createResultSetController)
        {
            addRSController();
        }
    }
    
    
    /**
     * Creates a special drop "switcher UI" component for switching between the Viewables in the MultiView.
     * @param mvParentArg the MultiView Parent
     * @param viewArg the View
     * @param altViewArg the AltView
     * @param altViewsListArg the Vector of AltView that will contains the ones in the Drop Down
     * @return the special combobox
     */
    public static DropDownButtonStateful createSwitcher(final MultiView       mvParentArg, 
                                                        final View            viewArg, 
                                                        final AltView         altViewArg, 
                                                        final Vector<AltView> altViewsListArg)
    {
        DropDownButtonStateful switcher = null;
        
        // Add all the View if we are at the top level
        // If not, then we are a subform and we should only add the view that belong to our same creation mode.
        if (mvParentArg.isTopLevel())
        {
            altViewsListArg.addAll(viewArg.getAltViews());
            
        } else
        {
            AltView.CreationMode mode = altViewArg.getMode();
            for (AltView av : viewArg.getAltViews())
            {
                ViewDef.ViewType type = av.getViewDef().getType();
                if (av.getMode() == mode || type == ViewDef.ViewType.table || type == ViewDef.ViewType.formTable)
                {
                    altViewsListArg.add(av);
                }
            }
        }
        // If we have AltView then we need to build information for the Switcher Control
        if (altViewsListArg.size() > 0)
        {
            ImageIcon[] iconsArray    = new ImageIcon[altViewsListArg.size()];
            String[]    labelsArray   = new String[altViewsListArg.size()];
            String[]    toolTipsArray = new String[altViewsListArg.size()];

            int inx = 0;
            Hashtable<String, Boolean> useLabels = new Hashtable<String, Boolean>();
            for (AltView av : altViewsListArg)
            {
                String selectorName = av.getSelectorName();
                if (StringUtils.isNotEmpty(selectorName))
                {
                    String combinedName = av.getMode().toString() + "_" + selectorName;
                    if (useLabels.get(combinedName) == null)
                    {
                        useLabels.put(combinedName, true);
                        
                    } else
                    {
                        continue;
                    }
                }
                
                labelsArray[inx] = av.getLabel();

                // TODO This is Sort of Temporary until I get it all figured out
                // But somehow we need to externalize this, possible have the AltView Definition
                // define its own icon
                if (av.getMode() == AltView.CreationMode.Edit)
                {
                    iconsArray[inx]    = IconManager.getImage("EditForm", IconManager.IconSize.Std16);
                    toolTipsArray[inx] = getResourceString("ShowEditViewTT");

                } else if (av.getViewDef().getType() == ViewDef.ViewType.table ||
                           av.getViewDef().getType() == ViewDef.ViewType.formTable)
                {
                    iconsArray[inx]    = IconManager.getImage("Spreadsheet", IconManager.IconSize.Std16);
                    toolTipsArray[inx] = getResourceString("ShowSpreadsheetTT");

                } else
                {
                    iconsArray[inx]    = IconManager.getImage("ViewForm", IconManager.IconSize.Std16);
                    toolTipsArray[inx] = getResourceString("ShowViewTT");
                }
                inx++;
            }
            
            class SwitcherAL implements ActionListener
            {
                protected DropDownButtonStateful switcherComp;
                public SwitcherAL(final DropDownButtonStateful switcherComp)
                {
                    this.switcherComp = switcherComp;
                }
                public void actionPerformed(ActionEvent ae)
                {
                    log.info("Index: "+switcherComp.getCurrentIndex());
                    
                    mvParentArg.showView(altViewsListArg.get(switcherComp.getCurrentIndex()));
                }
            }

            switcher = new DropDownButtonStateful(labelsArray, iconsArray, toolTipsArray);
            switcher.setToolTipText(getResourceString("SwitchViewsTT"));
            switcher.addActionListener(new SwitcherAL(switcher));
            switcher.validate();
            switcher.doLayout();

        }
        
        return switcher;
    }
    
    /**
     * 
     */
    protected void addSaveBtn()
    {
        saveBtn = new JButton(UICacheManager.getResourceString("Save"), IconManager.getIcon("Save", IconManager.IconSize.Std16));
        saveBtn.setToolTipText(ResultSetController.createTooltip("SaveRecordTT", view.getObjTitle()));
        saveBtn.setMargin(new Insets(1,1,1,1));
        saveBtn.setEnabled(false);
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                saveObject();
            }
        });
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
            validationInfoBtn.setToolTipText(getResourceString("ShowValidationInfoTT"));
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
            FormValidatorInfo formInfo = new FormValidatorInfo(this);

            JDialog dialog = new JDialog();
            dialog.setTitle(formValidator.getName());
            PanelBuilder panelBuilder = new PanelBuilder(new FormLayout("p", "p,5px,p"));
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
            FormValidatorInfo formInfo = new FormValidatorInfo(this);
            JFrame frame = new JFrame();
            frame.setContentPane(formInfo);
            frame.pack();
            frame.setSize(frame.getPreferredSize());
            UIHelper.centerAndShow(frame);
        }

    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#aboutToShow(boolean)
     */
    public void aboutToShow(final boolean show)
    {
        if (switcherUI != null)
        {
            ignoreSelection = true;
            switcherUI.setCurrentIndex(altViewsList.indexOf(altView));
            ignoreSelection = false;
        }
        
        if (selectorCBX != null)
        {
            ignoreSelection = true;
            selectorCBX.setSelectedIndex(0);
            ignoreSelection = false;
        }
        
        for (MultiView mv : kids)
        {
            mv.aboutToShow(show);
        }
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
    public AltView getAltView()
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
        mainBuilder.add(formComp, cc.xy(1, mainCompRowInx));

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

            // if this isn't the root form then find the root form
            // and make it listen to this validator for changes.
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
    protected void removeFromParent(final Object oldDataObj)
    {
        if (oldDataObj != null)
        {
            if (parentDataObj != null)
            {
                if (parentDataObj instanceof FormDataObjIFace &&
                    oldDataObj instanceof FormDataObjIFace)
                {
                    ((FormDataObjIFace)parentDataObj).removeReference((FormDataObjIFace)oldDataObj, cellName);
                    
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
    protected void traverseToGetDataFromForms(final MultiView parentMV)
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
     * @param isNewForm wheather the form is now in "new data input" mode
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
     * Checks to see if the current item has changed and asks if it should be saved
     * @return true to continue false to stop
     */
    public boolean checkForChanges()
    {
        if (formValidator != null && formValidator.hasChanged() && mvParent != null && mvParent.isTopLevel())
        {
            int rv = JOptionPane.showConfirmDialog(null,
                        getResourceString("SaveChanges"),
                        getResourceString("SaveChangesTitle"),
                        JOptionPane.YES_NO_CANCEL_OPTION);

            if (rv == JOptionPane.YES_OPTION)
            {
                saveObject();

            } else if (rv == JOptionPane.CANCEL_OPTION)
            {
                return false;
                
            } else if (rv == JOptionPane.NO_OPTION)
            {
                // Check to see if we are cancelling a new object or a previously saved object
                // if the object is part of this Session then anychanges were already saved.
                // If it is NOT part of this session then some of the object may not have been save.
                
                /* XYZ THIS NEEDS TO BE REWORKED
                if (!session.contains(dataObj))
                {
                    if (businessRules != null)
                    {
                        List<BusinessRulesDataItem> dataToSaveList = businessRules.getStandAloneDataItems(dataObj);
                        if (dataToSaveList.size() > 0)
                        {
                            CheckboxChooserDlg<BusinessRulesDataItem> dlg = new CheckboxChooserDlg<BusinessRulesDataItem>("Save", "Check the items you would like to have saved.", dataToSaveList);
                            UIHelper.centerAndShow(dlg);
                            dataToSaveList = dlg.getSelectedObjects();
                            for (BusinessRulesDataItem item : dataToSaveList)
                            {
                                item.setChecked(true);
                            }
                            businessRules.saveStandAloneData(dataObj, dataToSaveList);
                        }
                    }
                }*/
            }
        }
        return true;
    }

    /**
     * Creates a new Record and adds it to the List and dataSet if necessary
     */
    protected void createNewDataObject()
    {
        log.debug("createNewDataObject " + this.getView().getName());

        if (!checkForChanges())
        {
            return;
        }

        //log.info("createNewDataObject "+hashCode() + " Session ["+(session != null ? session.hashCode() : "null")+"] ");
        FormDataObjIFace obj = FormHelper.createAndNewDataObj(view.getClassName());
        if (parentDataObj instanceof FormDataObjIFace)
        {
            obj.initialize();
            ((FormDataObjIFace)parentDataObj).addReference(obj, cellName);
            
        } else
        {
            FormHelper.initAndAddToParent(parentDataObj, obj);
        }

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
        
        // Not calling setHasNewData because we need to traverse and setHasNewData doesn't
        formIsInNewDataMode = true;
        traverseToToSetAsNew(mvParent, formIsInNewDataMode, false); // don't traverse deeper than our immediate children
        updateControllerUI();

        this.setDataIntoUI();

        if (formValidator != null)
        {
            formValidator.validateForm();
        }
    }
    
    /**
     * The user tried to update or delete an object that was already changed by someone else. 
     */
    protected void recoverFromStaleObject(final String msgResStr)
    {
        JOptionPane.showMessageDialog(null, getResourceString(msgResStr), getResourceString("Error"), JOptionPane.ERROR_MESSAGE); 
        
        //session.rollback();
        
        session.close();
        
        session = DataProviderFactory.getInstance().createSession();
        //DataProviderFactory.getInstance().evict(dataObj.getClass()); 
        
        if (dataObj instanceof FormDataObjIFace)
        {
            Long id = ((FormDataObjIFace)dataObj).getId();
            Class<?> cls = dataObj.getClass();
            dataObj = session.get(cls, id);
            
            
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
    }

    /**
     * Save any changes to the current object
     */
    protected void saveObject()
    {
        //log.info("saveObject "+hashCode() + " Session ["+(session != null ? session.hashCode() : "null")+"]");
        try
        {
            this.getDataFromUI();

            traverseToGetDataFromForms(mvParent);
            
            if (businessRules != null && businessRules.processBusinessRules(dataObj) == BusinessRulesIFace.STATUS.Error)
            {
                StringBuilder strBuf = new StringBuilder();
                for (String s : businessRules.getWarningsAndErrors())
                {
                    strBuf.append(s);
                    strBuf.append("\n");
                }
                JOptionPane.showMessageDialog(null, strBuf, getResourceString("Error"), JOptionPane.ERROR_MESSAGE); 
                return;
            }
            
            FormHelper.updateLastEdittedInfo(dataObj);
            
            session.beginTransaction();
            session.saveOrUpdate(dataObj);
            session.commit();
            session.flush();
            
            log.info("Session Saved[ and Flushed "+session.hashCode()+"]");
            
            // Not calling setHasNewData because we need to traverse and setHasNewData doesn't
            formIsInNewDataMode = false;
            traverseToToSetAsNew(mvParent, false, true); // last arg means it should traverse
            updateControllerUI();
            
            setDataIntoUI();

            if (doCarryForward)
            {
                carryFwdDataObj = dataObj;
            }


        } catch (StaleObjectException e) // was StaleObjectStateException
        {
            session.rollback();
            recoverFromStaleObject("UPDATE_DATA_STALE");
            
        } catch (Exception e)
        {
            log.error("******* " + e);
            e.printStackTrace();
            session.rollback();
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
            //log.info(hashCode() + " Session ["+(session != null ? session.hashCode() : "null")+"] ");
            if (session == null)
            {
                int x = 0;
                x++;
                return;
            }
            
            removeFromParent(dataObj);
            
            String delMsg = businessRules != null ? businessRules.getDeleteMsg(dataObj) : "";

            boolean doClearObj = true;
            if (mvParent.isTopLevel())
            {
                try
                {
    
                    session.beginTransaction();
                    session.delete(dataObj);
                    session.commit();
                    session.flush();
                    
                } catch (edu.ku.brc.dbsupport.StaleObjectException e)
                {
                    doClearObj = false;
                    session.rollback();
                    recoverFromStaleObject("DELETE_DATA_STALE");
                    
                } catch (StaleObjectStateException e)
                {
                    doClearObj = false;
                    session.rollback();
                    recoverFromStaleObject("DELETE_DATA_STALE");
                }
                
            } else
            {
                session.deleteOnSaveOrUpdate(dataObj);
            }
            
            //mvParent.clearData(true);
            
            log.debug("Session Flushed["+session.hashCode()+"]");

            if (doClearObj)
            {
                if (rsController != null)
                {
                    int currInx = rsController.getCurrentIndex();
                    int newLen  = rsController.getLength() - 1;
                    int newInx  = Math.min(currInx, newLen-1);
                    
                    if (list != null)
                    {
                        list.remove(dataObj); // remove from list
                    }
                    
                    rsController.setLength(newLen); // set new len for controller
    
                    if (newInx > -1 && (list == null || list.size() > 0))
                    {
                        rsController.setIndex(newInx);
                        dataObj = list.get(newInx);
                        
                        setDataObj(dataObj, true); // true means the dataObj is already in the current "list" of data items we are working with
                    } else 
                    {
                        setDataObj(null, true); // true means the dataObj is already in the current "list" of data items we are working with
                    }
                } else
                {
                    setDataObj(null); 
                }
                ((JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR)).setText(delMsg);
            } else
            {
                ((JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR)).setText(getResourceString("OBJ_NOT_DELETED"));
            }


        } catch (Exception e)
        {
            log.error("******* " + e);
            e.printStackTrace();
         }
    }

    /**
     * Tells this form and all of it's children that it is a "new" form for data entry
     * @param isNewForm true is new, false is not
     */
    public void setHasNewData(final boolean isNewForm)
    {
        formIsInNewDataMode = isNewForm;
        updateControllerUI();
    }

    /**
     * Returns the list of MultiView kids (subforms)
     * @return the list of MultiView kids (subforms)
     */
    public List<MultiView> getKids()
    {
        return kids;
    }

    /**
     * Debug method - lists the fields that have changed
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
                    FieldInfo fieldInfo = controlsById.get(dcn.getId());
                    log.debug("Changed Field["+fieldInfo.getName()+"]\t["+(dcn.isDataChanged() ? "CHANGED" : "not changed")+"]");
                }
                log.debug("===================================");
            }
        } catch (Exception ex)
        {
            log.error(ex);
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
     * Adds the ResultSetController to the panel
     */
    protected void addRSController()
    {
        // If the Control panel doesn't exist, then add it
        if (rsController == null)
        {
            boolean inEditMode = altView.getMode() == AltView.CreationMode.Edit;
            rsController = new ResultSetController(formValidator, inEditMode, inEditMode, view.getObjTitle(), 0);
            rsController.addListener(this);
            controlPanel.add(rsController);

            if (rsController.getNewRecBtn() != null)
            {
                rsController.getNewRecBtn().addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae)
                    {
                        createNewDataObject();
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
     * Returns the "Save" Button
     * @return the Save Button
     */
    public JButton getSaveBtn()
    {
        return saveBtn;
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
     * @see edu.ku.brc.ui.forms.Viewable#getLabelById(java.lang.String)
     */
    public JLabel getLabelFor(final String id)
    {
        FieldInfo fi = labels.get(id);
        if (fi != null)
        {
            return (JLabel)fi.getComp();
        } else
        {
            throw new RuntimeException("Couldn't find FieldInfo for ID["+id+"]");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getControlMapping()
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
     * Updates the enabled state of the New and delete buttons in the controller
     */
    protected void updateControllerUI()
    {
        if (rsController != null)
        {
            log.debug("----------------- "+formViewDef.getName()+"----------------- ");
            if (rsController.getDelRecBtn() != null)
            {
                boolean enableDelBtn = dataObj != null && (businessRules == null || businessRules.okToDelete(this.dataObj));// && list != null && list.size() > 0;
                //log.debug(formViewDef.getName()+" Enabling The Del Btn: "+enableDelBtn);
                /*if (!enableDelBtn)
                {
                    //log.debug("  parentDataObj != null    ["+(parentDataObj != null) + "]");
                    //log.debug("  formIsInNewDataMode      ["+(!formIsInNewDataMode)+"]");
                    log.debug("  businessRules != null    ["+(businessRules != null)+"] ");
                    log.debug("  businessRules.okToDelete ["+(businessRules != null && businessRules.okToDelete(this.dataObj))+"]");
                    log.debug("  list != null             ["+(list != null)+"]");
                    log.debug("  list.size() > 0          ["+(list != null && list.size() > 0)+"]");
                }*/
                rsController.getDelRecBtn().setEnabled(enableDelBtn);
            }
            
            if (rsController.getNewRecBtn() != null)
            {
                boolean enableNewBtn = dataObj != null || parentDataObj != null || mvParent.isTopLevel();
                /*if (isEditting)
                {
                    log.debug(formViewDef.getName()+" ["+(dataObj != null) + "] ["+(parentDataObj != null)+"]["+(mvParent.isTopLevel())+"] "+enableNewBtn);
                    log.debug(formViewDef.getName()+" Enabling The New Btn: "+enableNewBtn);
                }*/
                rsController.getNewRecBtn().setEnabled(enableNewBtn);
            }
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
        // We really shouldn't get here.
        // This condition is true only if a new Object (record) was being entered and some how the user was
        // able to go to a previous or next record before saving or discarding the new info
        //if (formIsInNewDataMode)
        //{
        //    setAsNewForm(false);
        //    throw new RuntimeException("Shouldn't have gotten here! Why wasn't the object saved or discarded?");
        //}

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
                    Object firstDataObj = newList.get(0);
                    if (firstDataObj instanceof Comparable<?>)
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
        
        for (FieldInfo fieldInfo : controlsById.values())
        {
            if (fieldInfo.getFormCell().getType() == FormCell.CellType.subview ||
                fieldInfo.getFormCell().getType() == FormCell.CellType.iconview)
            {
                fieldInfo.getSubView().setParentDataObj(null);
            }
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
                updateControllerUI();
            }

            // Set the data from the into the form
            setDataIntoUI();

        } else
        {
            // OK, it is a single data object
            this.dataObj = dataObj;
            
            if (!alreadyInTheList)
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
                    controlPanel.setRSCVisibility(!isEditting);
                    rsController.setEnabled(true);
                    
                } else
                {
                    rsController.setEnabled(false);
                }
                updateControllerUI();
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataObj()
     */
    public Object getDataObj()
    {
        log.debug("getDataObj " + this.getView().getName());
        return dataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setParentDataObj(java.lang.Object)
     */
    public void setParentDataObj(Object parentDataObj)
    {
        this.parentDataObj = parentDataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getParentDataObj()
     */
    public Object getParentDataObj()
    {
        return parentDataObj;
    }
    
    
    /**
     * Updates the display icon as to the current state of the validator attached to the form,  
     * if the form has a validator.
     */
    protected void updateValidationBtnUIState()
    {
        if (validationInfoBtn != null && formValidator != null)
        {
            boolean                 enable = true;
            ImageIcon               icon   = IconManager.getImage("ValidationValid");
            UIValidatable.ErrorType state  = formValidator.getState();

            if (state == UIValidatable.ErrorType.Incomplete)
            {
                icon = IconManager.getImage("ValidationWarning");

            } else if (state == UIValidatable.ErrorType.Error)
            {
                icon = IconManager.getImage("ValidationError");
            } else
            {
                enable = false;
            }
            validationInfoBtn.setEnabled(enable);
            validationInfoBtn.setIcon(icon);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setDataIntoUI()
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
                    //log.debug("Setting ["+fieldInfo.getName()+"] to enabled=false");

                } else if (fieldInfo.getFormCell().getType() == FormCell.CellType.subview)
                {
                    fieldInfo.getSubView().setData(null);
                }
            }
            // Disable the ResultSet Controller
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

            // Enable the ResultSet Controller
            if (rsController != null)
            {
                rsController.setEnabled(true);
            }
            wasNull = false;
        }

        
        if (draggableRecIdentifier != null && this.dataObj != null && this.dataObj instanceof FormDataObjIFace)
        {
            FormDataObjIFace formDataObj = (FormDataObjIFace)this.dataObj;
            draggableRecIdentifier.setFormDataObj(formDataObj);
        }
        
        // This is used to keep track of all the Controls that have had there default value set
        // when there is a validator all the fields get reset back to false to we can't call "setChanged"
        // when we set the data into the control, we wait until after the fact.
        // this clear right here should need to be called but is for insurance
        defaultValueList.clear();
        
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
                    log.info("["+cellField.getName()+"] useFormatName["+useFormatName+"]  "+comp.getClass().getSimpleName());

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
                        if( values != null && values.length > 0 )
                        {
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

                                if (values[0] == null)
                                {
                                    setDataIntoUIComp(comp, isTextFieldPerMode ? "" : null, defaultValue);
                                    
                                } else
                                {
                                    setDataIntoUIComp(comp, isTextFieldPerMode ? values[0].toString() : values[0], defaultValue);
                                }

                            }
                        } else
                        {
                            setDataIntoUIComp(comp, null, defaultValue);
                            if (isEditting && comp instanceof UIValidatable && StringUtils.isNotEmpty(defaultValue))
                            {
                                defaultValueList.add((UIValidatable)comp);
                            }
                        }
                    }

                } else if (fieldInfo.getFormCell().getType() == FormCell.CellType.subview)
                {
                    fieldInfo.getSubView().setParentDataObj(dataObj);
                    
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
                }
            }
        }

        //log.debug(formViewDef.getName());

        // Adjust the formValidator now that all the data is in the controls
        if (formValidator != null)
        {
            formValidator.reset(MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT));
            
            /*
            formValidator.setHasChanged(false);

            formValidator.resetFields();

            formValidator.setDataChangeNotification(true); // this doesn't effect validation notifications

            formValidator.validateForm();

            if (MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT))
            {
                formValidator.setFormValidationState(UIValidatable.ErrorType.Valid); 
            }*/

            listFieldChanges();
            
            updateValidationBtnUIState();
            
        }

        // Now set all the controls with default values as having been changed
        // this is done because "resetFields" has just set them all to false
        for (UIValidatable uiv : defaultValueList)
        {
            uiv.setChanged(true);
        }
        defaultValueList.clear();

        if (mvParent != null && mvParent.isRoot() && saveBtn != null)
        {
            saveBtn.setEnabled(false);
        }
        
        updateControllerUI();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataFromUI()
     */
    public void getDataFromUI()
    {
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
                        
                        // FIXME This needs to be moved out of HibernateUtil!
                        FormHelper.setFieldValue(selectorName, dataObj, selectorValObj, dg, ds);
                        
                    } catch (Exception ex)
                    {
                        log.error(ex);
                        // XXX TODO Show error dialog here
                    }
                }
            }
            
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
                System.out.println(fieldInfo.getName()+"  "+fieldInfo.getFormCell().getName());
                String id = fieldInfo.getFormCell().getId();
                if (hasFormControlChanged(id))
                {
                    Object uiData = getDataFromUIComp(id); // if ID is null then we have huge problems
                    if (uiData != null)
                    {
                        //log.debug(fieldInfo.getFormCell().getName());
                        FormHelper.setFieldValue(fieldInfo.getFormCell().getName(), dataObj, uiData, dg, ds);
                    }
                }
            }
        } else
        {
            throw new RuntimeException("Calling getDataFromUI when the DataObjectSettable is null for the form.");
        }
    }

    /**
     * If the control supports UIValidatable interface then it return whether the controls has been changed. If it
     * doesn't then it assumes it has and returns true.
     * @param id the id of the control
     * @return If the control supports UIValidatable interface then it return whether the controls has been changed. If it
     * doesn't then it assumes it has and returns true.
     */
    protected boolean hasFormControlChanged(final String id)
    {
        FieldInfo fieldInfo = controlsById.get(id);
        if (fieldInfo != null)
        {
            Component comp = fieldInfo.getComp();
            if (comp != null)
            {
                if (comp instanceof UIValidatable)
                {
                    return ((UIValidatable)comp).isChanged();
                }
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getDataFromUIComp(java.lang.String)
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
     * @see edu.ku.brc.ui.forms.Viewable#getView()
     */
    public View getView()
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
       if (saveBtn != null && (mvParent == null || mvParent.hasChanged()))
       {
           saveBtn.setEnabled(wasOK);
       }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setSession(org.hibernate.Session)
     */
    public void setSession(final DataProviderSessionIFace session)
    {
        log.debug("setSession "+hashCode() + " Session ["+(session != null ? session.hashCode() : "null")+"] ");
        this.session = session;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#setCellName(java.lang.String)
     */
    public void setCellName(String cellName)
    {
        this.cellName = cellName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#shutdown()
     */
    public void shutdown()
    {
        for (Enumeration<FieldInfo> e=controlsById.elements(); e.hasMoreElements();)
        {
            e.nextElement().shutdown();
        }
        controlsById.clear();
        controlsByName.clear();
        labels.clear();
        
        if (altViewsList != null)
        {
            altViewsList.clear();
        }

        // XXX FIXME for (MultiView fvo : kids)
        //{
        //    fvo.cleanUp();
        //}
        kids.clear();

        mvParent    = null;
        formViewDef = null;
        formComp    = null;
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
     * Adds a control by name so it can be looked up later.
     * @param formCell the FormCell def that describe the cell
     * @param control the control
     */
    public void registerControl(final FormCell formCell, final Component control)
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
            
            FieldInfo fieldInfo = new FieldInfo(formCell, comp, scrollPane, controlsById.size());
            controlsById.put(formCell.getId(), fieldInfo);
            controlsByName.put(formCell.getName(), fieldInfo);

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
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#addSeparator(java.lang.String, int, int, int)
     */
    public Component createSeparator(String title, int colInx, int rowInx, int colSpan)
    {
        return builder.addSeparator(title, cc.xyw(colInx, rowInx, colSpan));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ViewBuilderIFace#addRecordIndentifier(java.lang.String, javax.swing.ImageIcon)
     */
    public JComponent createRecordIndentifier(String title, ImageIcon icon)
    {
        PanelBuilder panelBldr = new PanelBuilder(new FormLayout("16px,1px,f:p:g", "p"));
        draggableRecIdentifier = DraggableRecordIdentifierFactory.getInstance().createDraggableRecordIdentifier(icon);
        
        panelBldr.add(draggableRecIdentifier, cc.xy(1, 1));
        panelBldr.addSeparator(title, cc.xy(3, 1));
        return panelBldr.getPanel();
    }

    
    /**
     * Adds a control by name so it can be looked up later
     * @param formCell the FormCell def that describe the cell
     * @param subView the subView
     */
    public void addSubView(final FormCellSubView formCell, final MultiView subView, int colInx, int rowInx, int colSpan, int rowSpan)
    {
        if (formCell != null)
        {
            if (controlsById.get(formCell.getId()) != null)
            {
                throw new RuntimeException("Two controls have the same id ["+formCell.getId()+"] "+formViewDef.getName());
            }

            builder.add(subView, cc.xywh(colInx, rowInx, colSpan, rowSpan, "fill,fill"));
            
            controlsById.put(formCell.getId(), new FieldInfo(formCell, subView, controlsById.size()));
            kids.add(subView);
        }
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
    
    //-----------------------------------------------------
    // ValidationListener
    //-----------------------------------------------------


    /* (non-Javadoc)
     * @see ValidationListener#wasValidated(UIValidator)
     */
    public void wasValidated(final UIValidator validator)
    {
        updateValidationBtnUIState();
    }

    //-------------------------------------------------
    // ResultSetControllerListener
    //-------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexChanged(int)
     */
    public void indexChanged(int newIndex)
    {
        if (formValidator != null && formValidator.hasChanged())
        {
            getDataFromUI();
        }
        
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
        return checkForChanges();
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
        }
    }


    //-------------------------------------------------
    // AppPrefsChangeListener
    //-------------------------------------------------

    protected void setColorOnControls(final int colorType, final Color color)
    {
        for (FieldInfo fieldInfo : controlsById.values())
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

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.Viewable#getFieldIds(java.util.List)
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
            subView    = null;
            comp       = null;
            scrollPane = null;
        }

    }
}
