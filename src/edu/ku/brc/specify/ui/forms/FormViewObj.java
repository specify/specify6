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

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.ColorChooser;
import edu.ku.brc.specify.ui.ColorWrapper;
import edu.ku.brc.specify.ui.GetSetValueIFace;
import edu.ku.brc.specify.ui.db.JAutoCompComboBox;
import edu.ku.brc.specify.ui.db.PickListItem;
import edu.ku.brc.specify.ui.forms.persist.FormCell;
import edu.ku.brc.specify.ui.forms.persist.FormCellField;
import edu.ku.brc.specify.ui.forms.persist.FormFormView;
import edu.ku.brc.specify.ui.forms.persist.FormView;
import edu.ku.brc.specify.ui.validation.FormValidator;

/**
 * Implmentation of the FormViewable interface for the ui
 *  
 * @author rods
 *
 */
public class FormViewObj implements FormViewable, ResultSetControllerListener
{
    private static Log log = LogFactory.getLog(FormViewObj.class);
    
    protected FormViewObj                   parent;
    protected FormView                      formViewDef;
    protected Component                     formComp       = null;
    protected List<FormViewObj>             kids           = new ArrayList<FormViewObj>();
    
    protected Map<String, FieldInfo>        controls       = new Hashtable<String, FieldInfo>();
    
    protected FormValidator                 validator      = null;
    protected Object                        dataObj        = null;
    
    protected JPanel                        mainComp       = null;
    protected JPanel                        rsPanel        = null;
    protected ResultSetController           rsController   = null;
    protected java.util.List                list           = null;
    
    protected PanelBuilder                  mainBuilder;
    protected CellConstraints               cc             = new CellConstraints();
    
    // Data Members 
    protected static SimpleDateFormat scrDateFormat = null;


    /**
     * Constructor with FormView definition
     * @param formViewDef the definition of the form
     */
    public FormViewObj(final FormViewObj parent,  final FormView formViewDef, final Object dataObj)
    {
        this.parent      = parent;
        this.formViewDef = formViewDef;
        this.dataObj     = dataObj;
        
        if (scrDateFormat == null)
        {
            scrDateFormat = PrefsCache.getSimpleDateFormat("ui", "formatting", "scrdateformat");
        }
        
        mainBuilder    = new PanelBuilder(new FormLayout("f:p:g", parent == null ? "p,2px,p": "p:g,2px,p"));
        mainComp = mainBuilder.getPanel();
    }
    
    /**
     * Constructor with FormView definition
     * @param formViewDef the definition of the form
     * @param formComp the component of the form
     */
    /*public FormViewObj(final FormViewObj parent, final FormView formViewDef, final JComponent formComp, final Object dataObj)
    {
        this.parent      = parent;
        this.formViewDef = formViewDef;
        this.formComp        = formComp;
        this.dataObj     = dataObj;
    }*/
    

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
     * Adds child to parent
     * @param child the child to be added
     */
    public void addChild(final FormViewObj child)
    {
        kids.add(child);
    }
    
    /**
     * Adds a control by name so it can be looked up later
     * @param name the name of the control (must be unique or it throws a runtime exception
     * @param formCell the FormCell def that describe the cell
     * @param control the control
     */
    public void addControl(final FormCell formCell, final Component control)
    {
        if (formCell != null)
        {
            if (controls.get(formCell.getName()) != null)
            {
                throw new RuntimeException("Two controls have the same name ["+formCell.getName()+"] "+formViewDef.getViewSetName()+" "+formViewDef.getId());
            }
            controls.put(formCell.getName(), new FieldInfo(formCell, control));
        }
    }
    
    /**
     * Adds a control by name so it can be looked up later
     * @param name the name of the control (must be unique or it throws a runtime exception
     * @param formCell the FormCell def that describe the cell
     * @param control the control
     */
    public void addSubView(final FormCell formCell, final FormViewObj subView)
    {
        if (formCell != null)
        {
            if (controls.get(formCell.getName()) != null)
            {
                throw new RuntimeException("Two controls have the same name ["+formCell.getName()+"] "+formViewDef.getViewSetName()+" "+formViewDef.getId());
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
    }

    
    /**
     * Cleanup references
     */
    public void cleanUp()
    {
        controls.clear();
        for (FormViewObj fvo : kids)
        {
            fvo.cleanUp();
        }
        parent      = null;
        formViewDef = null;
        formComp    = null;
    }

    
    /**
     * Adds the ResultSetController to the form 
     */
    protected void addRecordSetController()
    {
        if (rsPanel == null)
        {
            PanelBuilder    builder    = new PanelBuilder(new FormLayout("c:p:g", "p"));
            CellConstraints cc         = new CellConstraints();
            
            rsController = new ResultSetController(list.size());
            rsController.addListener(this);
            builder.add(rsController.getPanel(), cc.xy(1,1));
            rsPanel = builder.getPanel();
        }
        mainBuilder.add(rsPanel, cc.xy(1,3));
    }
    
 
    //-------------------------------------------------
    // FormViewable
    //-------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getId()
     */
    public int getId()
    {
        return formViewDef.getId();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getType()
     */
    public FormView.ViewType getType()
    {
        return formViewDef.getType();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getUIComponent()
     */
    public Component getUIComponent()
    {
        return mainComp;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#isSubform()
     */
    public boolean isSubform()
    {
        return parent != null;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getComp(java.lang.String)
     */
    public Component getComp(final String name)
    {
        return controls.get(name).getComp();
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getControlMapping()
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
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getValidator()
     */
    public FormValidator getValidator()
    {
        return validator;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#setDataObj(java.lang.Object)
     */
    public void setDataObj(final Object dataObj)
    {
        Object data = dataObj;
        if (data instanceof java.util.Set)
        {
            Set dataSet = (Set)dataObj;
            data = Collections.list(Collections.enumeration(dataSet));
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
            }
            if (rsPanel == null)
            {
                addRecordSetController();
            }
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
                // ASSUMPTION! That is mainComp has more than one component it contains rsPanel
                if (mainComp.getComponentCount() > 1)
                {
                    mainComp.remove(rsPanel);
                }
            }
        }
        
        if (validator != null)
        {
            validator.setDataChangeNotification(true);
        }
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getDataObj()
     */
    public Object getDataObj()
    {
        return dataObj;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#setDataIntoUI()
     */
    public void setDataIntoUI()
    {
        DataObjectGettable dg = formViewDef.getDataGettable();
        if (formViewDef instanceof FormFormView)
        {
            for (FieldInfo fieldInfo : controls.values())
            {
                Object data = null;//dg.getFieldValue(dataObj, fieldInfo.getName());
                
                if (fieldInfo.getFormCell().isIgnoreSetGet())
                {
                    continue;
                }
                
                if (fieldInfo.getFormCell().getType() == FormCell.CellType.field)
                {
                    // Do Formatting here
                    FormCellField cellField = (FormCellField)fieldInfo.getFormCell();
                    
                    String format = cellField.getFormat();
                    if (isNotEmpty(format))
                    {
                        boolean  allFieldsNull = true;
                        String[] fields        = StringUtils.split(cellField.getName(), ",");
                        Object[] values        = new Object[fields.length];
                        for (int i=0;i<values.length;i++)
                        {
                            values[i] = dg.getFieldValue(dataObj, fields[i]);
                            if (allFieldsNull && values[i] != null)
                            {
                                allFieldsNull = false;
                            }
                        }
                        
                        if (!allFieldsNull)
                        {
                            if (format.equals("date"))
                            {
                                if (values.length == 1 && (values[0] instanceof Integer))
                                {
                                    int iDate = (Integer)values[0];
                                    if (iDate == 0)
                                    {
                                        data = "";
                                    } else
                                    {
                                        data = scrDateFormat.format(UIHelper.convertIntToDate((Integer)values[0]));
                                    }
                                } else
                                {
                                    data = "";
                                }
                                
                            } else if (values[0] instanceof java.util.Date)
                            {
                                data = scrDateFormat.format((java.util.Date)values[0]);
                                
                            } else
                            {
                                Formatter formatter = new Formatter();
                                formatter.format(format, (Object[])values);
                                data = formatter.toString();
                            }
                        }
                    } else
                    {
                        data = dg != null ? dg.getFieldValue(dataObj, fieldInfo.getName()) : null;
                        if (data instanceof Date)
                        {
                            data = scrDateFormat.format(data);
                        }
                    }
                    setDataIntoUIComp(fieldInfo.getName(), data);
                     
                } else if (fieldInfo.getFormCell().getType() == FormCell.CellType.subview)
                {
                    data = dg != null ? dg.getFieldValue(dataObj, fieldInfo.getName()) : null;
                    if (data != null)
                    {
                        fieldInfo.getSubView().setDataObj(data);
                        fieldInfo.getSubView().setDataIntoUI();
                    }
                }  
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getDataFromUI()
     */
    public void getDataFromUI()
    {
        DataObjectSettable ds = formViewDef.getDataSettable();
        if (ds != null)
        {
            if (formViewDef instanceof FormFormView)
            {
                for (FieldInfo fieldInfo : controls.values())
                {
                    if (fieldInfo.getFormCell().isIgnoreSetGet())
                    {
                        continue;
                    }
                    String name = fieldInfo.getFormCell().getName();
                    Object uiData = getDataFromUIComp(name);
                    ds.setFieldValue(dataObj, name, uiData);
                }
            }
        } else
        {
            throw new RuntimeException("Calling getDataFromUI when the DataObjectSettable is null for the form.");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getDataFromUIComp(java.lang.String)
     */
    public Object getDataFromUIComp(final String name)
    {
        FieldInfo fieldInfo = controls.get(name);
        if (fieldInfo != null)
        {
            Component formComp = fieldInfo.getComp();
            if (formComp != null)
            {
                if (formComp instanceof GetSetValueIFace)
                {
                    return ((GetSetValueIFace)formComp).getValue().toString();
                    
                } else if (formComp instanceof JTextField)
                {
                    return ((JTextField)formComp).getText();
                    
                } else if (formComp instanceof JComboBox)
                {
                    if (formComp instanceof JAutoCompComboBox)
                    {
                        PickListItem pli = (PickListItem)((JAutoCompComboBox)formComp).getSelectedItem();
                        return pli.getValue();
                        
                    } else
                    {
                        return ((JComboBox)formComp).getSelectedItem().toString();
                    }
                    
                } else if (formComp instanceof JLabel)
                {
                    return ((JLabel)formComp).getText();
                    
                } else if (formComp instanceof ColorChooser)
                {
                    return ColorWrapper.toString(((ColorChooser)formComp).getBackground());
                    
                } else if(formComp instanceof JList)
                {
                    return ((JList)formComp).getSelectedValue().toString();
                    
               } else
                {
                    log.error("Not sure how to get data from object "+formComp);
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
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getSubView(java.lang.String)
     */
    public FormViewable getSubView(final String name)
    {
        // do linear search because there will never be very many of them
        for (FormViewObj fvo : kids)
        {
            if (fvo.formViewDef.getName().equals(name))
            {
                return fvo;
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#setDataIntoUIComp(java.lang.String, java.lang.Object)
     */
    public void setDataIntoUIComp(final String name, Object data)
    {
        Component formComp = controls.get(name).getComp();
        if (formComp instanceof GetSetValueIFace)
        {
            ((GetSetValueIFace)formComp).setValue(data == null ? "" : data.toString());
            
        } else if (formComp instanceof JTextField)
        {
            ((JTextField)formComp).setText(data == null ? "" : data.toString());
            
        } else if (formComp instanceof JTextArea)
        {
            //System.out.println(name+" - "+formComp.getPreferredSize()+formComp.getSize());
            ((JTextArea)formComp).setText(data == null ? "" : data.toString());
            
        } else if (formComp instanceof JCheckBox)
        {
            //System.out.println(name+" - "+formComp.getPreferredSize()+formComp.getSize());
            if (data != null)
            {
                ((JCheckBox)formComp).setSelected((data instanceof Boolean) ? ((Boolean)data).booleanValue() : data.toString().equalsIgnoreCase("true"));
            } else
            {
                ((JCheckBox)formComp).setSelected(false);
            }
            
        } else if (formComp instanceof JLabel)
        {
            ((JLabel)formComp).setText(data == null ? "" : data.toString());
            
        } else if (formComp instanceof JComboBox)
        {
            setComboboxValue((JComboBox)formComp, data);
            
        } else if (formComp instanceof JList)
        {
            setListValue((JList)formComp, data);
            

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
    }
    
    //-------------------------------------------------
    // FieldInfo
    //-------------------------------------------------
    class FieldInfo 
    {
        protected FormCell    formCell;
        protected FormViewObj subView;
        protected Component   comp;
        
        public FieldInfo(FormCell formCell, Component comp)
        {
            this.comp     = comp;
            this.formCell = formCell;
            this.subView  = null;
        }
        
        public FieldInfo(FormCell formCell, FormViewObj subView)
        {
            this.formCell = formCell;
            this.subView  = subView;
            this.comp     = subView.getUIComponent();
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

        public FormViewObj getSubView()
        {
            return subView;
        }
        
    }

}
