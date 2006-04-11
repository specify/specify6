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

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.ColorChooser;
import edu.ku.brc.specify.ui.ColorWrapper;
import edu.ku.brc.specify.ui.GetSetValueIFace;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.db.JAutoCompComboBox;
import edu.ku.brc.specify.ui.db.PickListItem;
import edu.ku.brc.specify.ui.forms.persist.AltView;
import edu.ku.brc.specify.ui.forms.persist.FormCell;
import edu.ku.brc.specify.ui.forms.persist.FormCellField;
import edu.ku.brc.specify.ui.forms.persist.FormCellSubView;
import edu.ku.brc.specify.ui.forms.persist.FormViewDef;
import edu.ku.brc.specify.ui.forms.persist.View;
import edu.ku.brc.specify.ui.forms.persist.ViewDef;
import edu.ku.brc.specify.ui.validation.FormValidator;

/**
 * Implmentation of the Viewable interface for the ui and this derived class is for handling Form's Only (not tables)
 *
 *
 * @author rods
 *
 */
public class FormViewObj implements Viewable, ResultSetControllerListener, PreferenceChangeListener
{
    private static Log log = LogFactory.getLog(FormViewObj.class);

    protected static Object[]               formattedValues = new Object[2];

    protected boolean                       isEditting     = false;
    protected MultiView                     mvParent       = null;
    protected View                          view;
    protected AltView                       altView;
    protected FormViewDef                   formViewDef;
    protected Component                     formComp       = null;
    protected List<MultiView>               kids           = new ArrayList<MultiView>();

    protected Map<String, FieldInfo>        controls       = new Hashtable<String, FieldInfo>();

    protected FormValidator                 validator      = null;
    protected Object                        parentDataObj  = null;
    protected Object                        dataObj        = null;
    protected Set                           origDataSet    = null; 
    protected Object[]                      singleItemArray = new Object[1];

    protected JPanel                        mainComp       = null;
    protected ControlBarPanel               controlPanel   = null;
    protected ResultSetController           rsController   = null;
    protected java.util.List                list           = null;
    protected JComboBox                     altViewUI      = null;
    protected boolean                       ignoreSelection = false;
    protected JButton                       saveBtn         = null;

    protected PanelBuilder                  mainBuilder;
    protected CellConstraints               cc             = new CellConstraints();

    // Data Members
    protected static SimpleDateFormat       scrDateFormat  = null;
    protected static ColorWrapper           viewFieldColor = null;


    /**
     * Constructor with FormView definition
     * @param view the definition of the view
     * @param altView indicates which AltView we will be using
     * @param mvParent the mvParent mulitview
     */
    public FormViewObj(final View        view,
                       final AltView     altView,
                       final MultiView   mvParent)
    {
        this.view        = view;
        this.altView     = altView;
        this.mvParent    = mvParent;

        formViewDef = (FormViewDef)altView.getViewDef();

        if (scrDateFormat == null)
        {
            scrDateFormat = PrefsCache.getSimpleDateFormat("ui", "formatting", "scrdateformat");
        }

        Preferences appsNode = UICacheManager.getAppPrefs();
        Preferences prefNode = appsNode.node("ui/formatting");
        prefNode.addPreferenceChangeListener(this);

        mainBuilder    = new PanelBuilder(new FormLayout("f:p:g", mvParent == null ? "p,2px,p": "p:g,2px,p"));
        mainComp = mainBuilder.getPanel();

        // We will add the switchable UI if we are mvParented to a MultiView and have multiple AltViews
        if (mvParent != null && view.getAltViews().size() > 1)
        {
            controlPanel = new ControlBarPanel();
            mainBuilder.add(controlPanel, cc.xy(1,3));

            // Now we have a Special case that when when there are only two AltViews and
            // they differ only by Edit & View we hide the switching UI unless
            // we are the root MultiView. This way when switching the Root View all the other views switch
            // (This is because they were created that way. It also makes no sense that while in "View" mode
            // you would want to switch an individual subview to a differe "mode" view than the root).

            if (!view.isSpecialViewEdit() || mvParent.getMultiViewParent() == null)
            {
                // loop thru and add the AltViews to the comboxbox and make sure that the
                // this form is always at the top of the list.
                Vector<AltView> altViewsList = new Vector<AltView>();
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
                altViewUI = new JComboBox(altViewsList);
                addMultiViewListener(altViewUI);

                List<JComponent> comps = new ArrayList<JComponent>();
                if (altView.getMode() == AltView.CreationMode.Edit)
                {
                    saveBtn = new JButton(IconManager.getIcon("Save", IconManager.IconSize.Std24));
                    saveBtn.setMargin(new Insets(1,1,1,1));
                    saveBtn.setEnabled(false);
                    saveBtn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae)
                        {
                            saveObject();
                        }
                    });

                    comps.add(saveBtn);
                }
                comps.add(altViewUI);

                controlPanel.addComponents(comps, false); // false -> right side

            }
        }

    }
    
    /**
     * Save any changes to the current object
     */
    protected void saveObject()
    {
        try
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
            session.saveOrUpdate(dataObj);
            HibernateUtil.commitTransaction();
            
        } catch (Exception e)
        {
            log.error("******* " + e);
            e.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }
    }

    /**
     * Indicates the form is about to be shown
     */
    public void aboutToShow()
    {
        if (altViewUI != null)
        {
            ignoreSelection = true;
            altViewUI.setSelectedIndex(0);
            ignoreSelection = false;
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
            if (controls.get(formCell.getName()) != null)
            {
                throw new RuntimeException("Two controls have the same name ["+formCell.getName()+"] "+formViewDef.getName());
            }
            controls.put(formCell.getName(), new FieldInfo(formCell, control));
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
            if (controls.get(formCell.getName()) != null)
            {
                throw new RuntimeException("Two controls have the same name ["+formCell.getName()+"] "+formViewDef.getName());
            }

            controls.put(formCell.getName(), new FieldInfo(formCell, subView));
            kids.add(subView);
        }
    }

    /**
     * Sest the form validator
     * @param validator the validator
     */
    public void setValidator(final FormValidator validator)
    {
        this.validator = validator;

        if (validator != null && mvParent != null)
        {
            MultiView root = mvParent;
            while (root.getMultiViewParent() != null)
            {
                root = root.getMultiViewParent();
            }
            root.addFormValidator(validator);
        }
    }
    
    protected void addToParent(final Object newDataObj)
    {
        if (parentDataObj != null)
        {
            String methodName = "add" + newDataObj.getClass().getSimpleName();
            try
            {
                Method method = parentDataObj.getClass().getMethod(methodName, new Class[] {newDataObj.getClass()});
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
     * Creates a new Record and adds it to the List and dataSet if necessary
     */
    protected void createNewRecord()
    {
        try
        {
            Class classObj = Class.forName(view.getClassName());
            Object obj = classObj.newInstance();
            dataObj = obj;
            if (list != null)
            {
                list.add(obj);
                //origDataSet.add(obj);
                rsController.setLength(list.size());
                rsController.setIndex(list.size()-1);
                addToParent(obj);
            }
            this.setDataIntoUI();
            
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
     * Cleanup references
     */
    public void cleanUp()
    {
        controls.clear();
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
     * @see edu.ku.brc.specify.ui.forms.Viewable#getComp(java.lang.String)
     */
    public Component getComp(final String name)
    {
        return controls.get(name).getComp();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getControlMapping()
     */
    public Map<String, Component> getControlMapping()
    {
        Map<String, Component> map = new Hashtable<String, Component>();
        for (FieldInfo fi : controls.values())
        {
            map.put(fi.getName(), fi.getComp());
        }
        return map;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getValidator()
     */
    public FormValidator getValidator()
    {
        return validator;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#setDataObj(java.lang.Object)
     */
    public void setDataObj(final Object dataObj)
    {
        this.parentDataObj = parentDataObj;
        
        Object data = dataObj;
        if (data instanceof java.util.Set)
        {
            origDataSet = (Set)dataObj;
            data = Collections.list(Collections.enumeration(origDataSet));
        }

        if (validator != null)
        {
            validator.addRuleObjectMapping("dataObj", dataObj);
            validator.setDataChangeNotification(false);
        }

        if (data instanceof java.util.List)
        {
            list = (java.util.List)data;
            if (list.size() > 0)
            {
                this.dataObj = list.get(0);
            } else
            {
                this.dataObj = null;
            }

            
            // If the Control panel doesn't exist, then add it
            if (rsController == null)
            {
                rsController = new ResultSetController(validator, altView.getMode() == AltView.CreationMode.Edit, list.size());
                rsController.addListener(this);
                controlPanel.add(rsController);
                
                if (rsController.getNewRecBtn() != null)
                {
                    rsController.getNewRecBtn().addActionListener(new ActionListener()
                            {
                        public void actionPerformed(ActionEvent ae)
                        {
                            createNewRecord();
                        }
                    });
                }
            }
            rsController.setLength(list.size());

            setDataIntoUI();

        } else
        {
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

        if (validator != null)
        {
            validator.setDataChangeNotification(true);
            validator.validateForm();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getDataObj()
     */
    public Object getDataObj()
    {
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
        DataObjectGettable dg = formViewDef.getDataGettable();

        for (FieldInfo fieldInfo : controls.values())
        {
            Component comp = controls.get(fieldInfo.getName()).getComp();

            Object data = null;//dg.getFieldValue(dataObj, fieldInfo.getName());

            if (fieldInfo.getFormCell().isIgnoreSetGet())
            {
                continue;
            }
            System.out.println("["+fieldInfo.getFormCell().getName()+"]["+fieldInfo.getFormCell().getType()+"]");
            if (fieldInfo.getFormCell().getName().equals("agentAddress.agent"))
            {
                int x = 0;
                x++;
            }

            if (fieldInfo.getFormCell().getType() == FormCell.CellType.field)
            {
                // Do Formatting here
                FormCellField cellField        = (FormCellField)fieldInfo.getFormCell();
                String        formatName       = cellField.getFormatName();

                //boolean useFormatName = (cellField.isBothFormatterDefined() && cellField.isTextField()) || isNotEmpty(formatName);
                boolean useFormatName = cellField.isTextField() && (isNotEmpty(cellField.getUiFieldFormatter()) || isNotEmpty(formatName));
                System.out.println("["+cellField.getName()+"] "+useFormatName+"  "+comp.getClass().getSimpleName());

                if (useFormatName)
                {
                    if (cellField.getFieldNames().length > 1)
                    {
                        throw new RuntimeException("formatName ["+formatName+"] only works on a single value.");
                    }
                    Object[] vals = UIHelper.getFieldValues(cellField.getFieldNames(), dataObj, dg);
                    setDataIntoUIComp(fieldInfo.getName(), DataObjFieldFormatMgr.format(vals[0], formatName));

                } else
                {

                    Object[] values = UIHelper.getFieldValues(cellField, dataObj, dg);
                    String   format = cellField.getFormat();
                    if (isNotEmpty(format))
                    {
                        setDataIntoUIComp(fieldInfo.getName(), UIHelper.getFormattedValue(values, cellField.getFormat()));

                    } else
                    {
                        if (cellField.getFieldNames().length > 1)
                        {
                            throw new RuntimeException("No Format but mulitple fields were specified for["+cellField.getName()+"]");
                        }
                        
                        if (cellField.isTextField())
                        {
                            
                            setDataIntoUIComp(fieldInfo.getName(), values != null && values[0] != null ? values[0].toString() : "");
                        } else
                        {
                            setDataIntoUIComp(fieldInfo.getName(), values == null ? null : values[0]);
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
                    fieldInfo.getSubView().setParentDataObj(dataObj);
                }
            }
        }

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getDataFromUI()
     */
    public void getDataFromUI()
    {
        DataObjectSettable ds = formViewDef.getDataSettable();
        if (ds != null)
        {
            for (FieldInfo fieldInfo : controls.values())
            {
                if (fieldInfo.getFormCell().isIgnoreSetGet())
                {
                    continue;
                }
                String name = fieldInfo.getFormCell().getName();
                if (name.equals("isCurrent"))
                {
                    int x = 0;
                    x++;
                }
                Object uiData = getDataFromUIComp(name);
                ds.setFieldValue(dataObj, name, uiData);
            }
        } else
        {
            throw new RuntimeException("Calling getDataFromUI when the DataObjectSettable is null for the form.");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.Viewable#getDataFromUIComp(java.lang.String)
     */
    public Object getDataFromUIComp(final String name)
    {
        FieldInfo fieldInfo = controls.get(name);
        if (fieldInfo != null)
        {
            Component comp = fieldInfo.getComp();
            if (comp != null)
            {
                if (comp instanceof GetSetValueIFace)
                {
                    return ((GetSetValueIFace)comp).getValue();

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
                log.error("Component is null in FieldInfo "+name);
            }
        } else
        {
            log.error("FieldInfo is null "+name);
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
    public void setDataIntoUIComp(final String name, Object data)
    {
        setDataIntoUIComp(controls.get(name).getComp(), data);
    }


    /**
     * Helper class to set data into a component
     * @param comp the component to get the data
     * @param data the data to be set into the component
     */
    public void setDataIntoUIComp(final Component comp, Object data)
    {
        if (comp instanceof GetSetValueIFace)
        {
            ((GetSetValueIFace)comp).setValue(data);

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
    
    public boolean indexAboutToChange(int oldIndex, int newIndex)
    {
        if (validator != null && validator.hasChanged())
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
        for (FieldInfo fieldInfo : controls.values())
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
     * @see edu.ku.brc.specify.ui.forms.Viewable#getFieldNames(java.util.List)
     */
    public void getFieldNames(final List<String> fieldNames)
    {
        for (FieldInfo fieldInfo : controls.values())
        {
            if (fieldInfo.getFormCell().getType() == FormCell.CellType.field)
            {
                fieldNames.add(((FormCellField)fieldInfo.getFormCell()).getName());
            }
        }

    }

    //-------------------------------------------------
    // FieldInfo
    //-------------------------------------------------
    class FieldInfo
    {
        protected FormCell  formCell;
        protected MultiView subView;
        protected Component comp;

        public FieldInfo(FormCell formCell, Component comp)
        {
            this.comp     = comp;
            this.formCell = formCell;
            this.subView  = null;
        }

        public FieldInfo(FormCell formCell, MultiView subView)
        {
            this.formCell = formCell;
            this.subView  = subView;
            this.comp     = subView;
        }

        public String getName()
        {
            return formCell.getName();
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

    }



}
