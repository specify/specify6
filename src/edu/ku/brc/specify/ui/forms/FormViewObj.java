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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.ComboBoxModel;
import javax.swing.JComponent;
import javax.swing.*;

import edu.ku.brc.specify.ui.forms.persist.FormFormView;
import edu.ku.brc.specify.ui.forms.persist.FormView;
import edu.ku.brc.specify.ui.validation.FormValidator;
import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.prefs.*;

/**
 * Implmentation of the FormViewable interface for the ui
 *  
 * @author rods
 *
 */
public class FormViewObj implements FormViewable
{
    protected FormViewObj                   parent;
    protected FormView                      formViewDef;
    protected Component                     comp      = null;
    protected List<FormViewObj>             kids      = new ArrayList<FormViewObj>();
    
    protected Map<String, Component>        controls  = new Hashtable<String, Component>();
    
    protected FormValidator                 validator = null;
    protected Object                        dataObj   = null;
    
    /**
     * Constructor with FormView definition
     * @param formViewDef the definition of the form
     */
    public FormViewObj(final FormViewObj parent,  final FormView formViewDef, final Object dataObj)
    {
        this.parent      = parent;
        this.formViewDef = formViewDef;
        this.dataObj     = dataObj;
    }
    
    /**
     * Constructor with FormView definition
     * @param formViewDef the definition of the form
     * @param comp the component of the form
     */
    public FormViewObj(final FormViewObj parent, final FormView formViewDef, final JComponent comp, final Object dataObj)
    {
        this.parent      = parent;
        this.formViewDef = formViewDef;
        this.comp        = comp;
        this.dataObj     = dataObj;
    }
    

    /**
     * Sets the component into the object
     * @param comp the UI component that represents this viewable
     */
    public void setComp(JComponent comp)
    {
        this.comp = comp;
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
     * @param control the control
     */
    public void addControl(final String name, final Component control)
    {
        if (name != null && name.length() > 0)
        {
            if (controls.get(name) != null)
            {
                throw new RuntimeException("Two controls have the same name ["+name+"] "+formViewDef.getViewSetName()+" "+formViewDef.getId());
            }
            controls.put(name, control);
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
        comp        = null;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    public void finalize() throws Throwable
    {
        super.finalize();
        cleanUp();
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
        return comp;
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
        return controls.get(name);
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getControlMapping()
     */
    public Map<String, Component> getControlMapping()
    {
        return controls;
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
        this.dataObj = dataObj;
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
            for (String name : controls.keySet())
            {
                Object data = dg.getFieldValue(dataObj, name);
                setDataIntoUIComp(name, data);
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
                for (String name : controls.keySet())
                {
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
        Component comp = controls.get(name);
        if (comp != null)
        {
            if (comp instanceof JTextField)
            {
                return ((JTextField)comp).getText();
                
            } else if(comp instanceof JComboBox)
            {
                return ((JComboBox)comp).getSelectedItem().toString();
                
            } else if (comp instanceof ColorChooser)
            {
                return ColorWrapper.toString(((ColorChooser)comp).getBackground());
                
            } else if (comp instanceof GetSetValueIFace)
            {
                return ((GetSetValueIFace)comp).getValue().toString();
                
            } else
            {
                System.err.println("Not sure how to get data from object "+comp);
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#setDataIntoUIComp(java.lang.String, java.lang.Object)
     */
    public void setDataIntoUIComp(final String name, Object data)
    {
        Component comp = controls.get(name);
        if (comp instanceof JTextField)
        {
            ((JTextField)comp).setText(data == null ? "" : data.toString());
            
        } else if (comp instanceof JComboBox)
        {
            JComboBox      cbx   = (JComboBox)comp;
            ComboBoxModel  model = cbx.getModel();
            for (int i=0;i<cbx.getItemCount();i++)
            {
                Object item = model.getSelectedItem();
                if (item instanceof String)
                {
                    if (((String)item).equals(data))
                    {
                        cbx.setSelectedIndex(i);
                        return;
                    } 
                } else if (item.equals(data))
                {
                    cbx.setSelectedIndex(i);
                    return;
                }
            }
        } else if (comp instanceof GetSetValueIFace)
        {
            ((GetSetValueIFace)comp).setValue(data == null ? "" : data.toString());
        }
        
    }

}
