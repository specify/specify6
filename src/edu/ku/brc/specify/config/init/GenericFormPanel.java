/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.createLabel;

import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.DataGetterForObj;
import edu.ku.brc.af.ui.forms.DataSetterForObj;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 17, 2008
 *
 */
public class GenericFormPanel extends BaseSetupPanel
{
    protected String[]         fieldsNames = null;
    protected Hashtable<String, JComponent> comps = new Hashtable<String, JComponent>();
    protected FormDataObjIFace dataObj;
    protected DataGetterForObj getter    = null;
    protected DataSetterForObj setter    = null;
    
    /**
     * @param panelName
     * @param nextBtn
     */
    public GenericFormPanel(final String panelName,
                            final JButton nextBtn)
    {
        super(panelName, nextBtn);
    }

    public GenericFormPanel(final String   name,
                            final String   title,
                            final String[] labels,
                            final String[] fields, 
                            final JButton  nextBtn)
    {
        this(null, name, title, labels, fields,  nextBtn);
    }
    
    public GenericFormPanel(final FormDataObjIFace dataObj,
                            final String   name,
                            final String   title,
                            final String[] labels,
                            final String[] fields, 
                            final JButton  nextBtn)
    {
        super(name, nextBtn);
        
        this.dataObj     = dataObj;
        this.fieldsNames = fields;
        
        //DBTableInfo tblInfo = dataObj != null ? DBTableIdMgr.getInstance().getInfoById(dataObj.getTableId()) : null;
        
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,5px," + 
                                               UIHelper.createDuplicateJGoodiesDef("p", "2px", fields.length)+",p:g"), this);
        int row = 1;
        
        builder.add(createLabel(title), cc.xywh(1,row,3,1));row += 2;

        int i = 0;
        for (String fName : fields)
        {
            if (fName.equals("-"))
            {
                builder.addSeparator(labels[i], cc.xyw(1, row, 4));
            } else
            {
                comps.put(fName, createField(builder, labels[i], row));
            }
            row += 2;
            i++;
        }
        updateBtnUI();
    }
    
    public DataGetterForObj getGetter()
    {
        return getter;
    }

    public void setGetter(DataGetterForObj getter)
    {
        this.getter = getter;
    }

    public DataSetterForObj getSetter()
    {
        return setter;
    }

    public void setSetter(DataSetterForObj setter)
    {
        this.setter = setter;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues(java.util.Properties)
     */
    @Override
    public void getValues(final Properties props)
    {
        for (String fName : comps.keySet())
        {
            JComponent comp = comps.get(fName);
            if (comp instanceof JTextField)
            {
                String val = ((JTextField)comp).getText();
                props.put(fName, val);
                
                if (dataObj != null && setter != null)
                {
                    Object dataVal = getter.getFieldValue(dataObj, fName);
                    if (dataVal != null)
                    {
                        if (!dataVal.toString().equals(val))
                        {
                            setter.setFieldValue(dataObj, fName, val);
                        }
                    }
                }
            }
        } 
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(final Properties values)
    {
        for (String fName : comps.keySet())
        {
            JComponent comp = comps.get(fName);
            if (comp instanceof JTextField)
            {
                String val = values.getProperty(fName);
                if (dataObj != null && getter != null)
                {
                    Object dataVal = getter.getFieldValue(dataObj, fName);
                    if (dataVal != null)
                    {
                        val = dataVal.toString();
                    }
                }
                ((JTextField)comp).setText(val);
                
                if (StringUtils.isNotEmpty(val))
                {
                    ((JTextField)comp).setCaretPosition(0);
                }
            } 
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        for (String fName : comps.keySet())
        {
            JComponent comp = comps.get(fName);
            if (comp instanceof JTextField)
            {
                if (StringUtils.isEmpty(((JTextField)comp).getText()))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#updateBtnUI()
     */
    @Override
    public void updateBtnUI()
    {
        if (nextBtn != null)
        {
            nextBtn.setEnabled(isUIValid());
        }
    }

}
