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
    protected Hashtable<String, JComponent> comps = new Hashtable<String, JComponent>();
    
    
    public GenericFormPanel(final String name,
                            final String   title,
                            final String[] labels,
                            final String[] fields, 
                            final JButton  nextBtn)
    {
        super(name, nextBtn);
        
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,5px," + 
                                               UIHelper.createDuplicateJGoodiesDef("p", "2px", fields.length)+",p:g"), this);
        int row = 1;
        
        builder.add(createLabel(title), cc.xywh(1,row,3,1));row += 2;

        int i = 0;
        for (String fName : fields)
        {
            comps.put((String)fName, createField(builder, labels[i], row));
            row += 2;
            i++;
        }
        updateBtnUI();
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
                props.put(makeName(fName), ((JTextField)comp).getText());
            }
        } 
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    protected void setValues(final Properties values)
    {
        for (Object key : values.keySet())
        {
            String[] fieldNames = StringUtils.split(key.toString(), "_");
            if (fieldNames[0].equals(panelName))
            {
                JComponent comp = comps.get(fieldNames[1]);
                if (comp instanceof JTextField)
                {
                    String val      = values.getProperty(key.toString());
                    ((JTextField)comp).setText(val);
                    if (StringUtils.isNotEmpty(val))
                    {
                        ((JTextField)comp).setCaretPosition(0);
                    }
                } 
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#isUIValid()
     */
    @Override
    protected boolean isUIValid()
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
    protected void updateBtnUI()
    {
        if (nextBtn != null)
        {
            nextBtn.setEnabled(isUIValid());
        }
    }

}
