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
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.createI18NLabel;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.DataGetterForObj;
import edu.ku.brc.af.ui.forms.DataSetterForObj;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

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
    protected Hashtable<String, Boolean> reqHash  = null;
    
    protected CellConstraints       cc = new CellConstraints();
    protected PanelBuilder          builder   = null;
    protected int                   row       = 1;
    protected String[]              labels;
    protected ArrayList<JComponent> compList  = new ArrayList<JComponent>();
    
    /**
     * @param panelName
     * @param helpContext
     * @param nextBtn
     */
    public GenericFormPanel(final String panelName,
                            final String helpContext,
                            final JButton nextBtn,
                            final JButton prevBtn)
    {
        this(panelName, helpContext, nextBtn, prevBtn, false);
    }

    /**
     * @param panelName
     * @param helpContext
     * @param nextBtn
     * @param makeStretchy
     */
    public GenericFormPanel(final String panelName,
                            final String helpContext,
                            final JButton nextBtn,
                            final JButton prevBtn,
                            final boolean makeStretchy)
    {
        super(panelName, helpContext, nextBtn, prevBtn, makeStretchy);
    }

    /**
     * @param name
     * @param title
     * @param helpContext
     * @param labels
     * @param fields
     * @param nextBtn
     * @param makeStretchy
     */
    public GenericFormPanel(final String   name,
                            final String   title,
                            final String   helpContext,
                            final String[] labels,
                            final String[] fields, 
                            final JButton  nextBtn,
                            final JButton  prevBtn,
                            final boolean makeStretchy)
    {
        this(null, name, title, helpContext, labels, fields, (boolean[])null, (Integer[])null, nextBtn, prevBtn, makeStretchy);
    }
    
    /**
     * @param name
     * @param title
     * @param helpContext
     * @param labels
     * @param fields
     * @param required
     * @param nextBtn
     * @param makeStretchy
     */
    public GenericFormPanel(final String   name,
                            final String   title,
                            final String   helpContext,
                            final String[] labels,
                            final String[] fields, 
                            final boolean[] required, 
                            final JButton  nextBtn,
                            final JButton prevBtn,
                            final boolean makeStretchy)
    {
        this(null, name, title, helpContext, labels, fields, required, null, nextBtn, prevBtn, makeStretchy);
        
    }
    
    /**
     * @param name
     * @param title
     * @param helpContext
     * @param labels
     * @param fields
     * @param required
     * @param nextBtn
     * @param makeStretchy
     */
    public GenericFormPanel(final String   name,
                            final String   title,
                            final String   helpContext,
                            final String[] labels,
                            final String[] fields, 
                            final boolean[] required, 
                            final Integer[] lengths, 
                            final JButton  nextBtn,
                            final JButton prevBtn,
                            final boolean makeStretchy)
    {
        this(null, name, title, helpContext, labels, fields, required, lengths, nextBtn, prevBtn, makeStretchy);
        
    }
    

    /**
     * @param dataObj
     * @param name
     * @param title
     * @param helpContext
     * @param labels
     * @param fields
     * @param required
     * @param nextBtn
     * @param makeStretchy
     */
    public GenericFormPanel(final FormDataObjIFace dataObj,
                            final String   name,
                            final String   title,
                            final String   helpContext,
                            final String[] labels,
                            final String[] fields, 
                            final boolean[] required, 
                            final Integer[] lengths,
                            final JButton  nextBtn,
                            final JButton  prevBtn,
                            final boolean makeStretchy)
    {
        super(name, helpContext, nextBtn, prevBtn);
        
        this.dataObj      = dataObj;
        this.fieldsNames  = fields;
        this.makeStretchy = makeStretchy;
        this.labels       = labels;
        
        init(title, fields, required, null, lengths);
    }
    
    /**
     * @param name
     * @param title
     * @param helpContext
     * @param labels
     * @param fields
     * @param types
     * @param nextBtn
     * @param makeStretchy
     */
    public GenericFormPanel(final String   name,
                            final String   title,
                            final String   helpContext,
                            final String[] labels,
                            final String[] fields, 
                            final String[] types, 
                            final JButton  nextBtn,
                            final JButton  prevBtnBtn,
                            final boolean makeStretchy)
    {
        super(name, helpContext, nextBtn, prevBtnBtn);
        
        this.dataObj      = null;
        this.fieldsNames  = fields;
        this.makeStretchy = makeStretchy;
        this.labels       = labels;
        
        init(title, fields, null, types, null);
    }
    
    /**
     * @param name
     * @param title
     * @param helpContext
     * @param labels
     * @param fields
     * @param types
     * @param nextBtn
     * @param makeStretchy
     */
    public GenericFormPanel(final String   name,
                            final String   title,
                            final String   helpContext,
                            final String[] labels,
                            final String[] fields, 
                            final Integer[] lengths, 
                            final JButton  nextBtn,
                            final JButton  prevBtnBtn,
                            final boolean makeStretchy)
    {
        super(name, helpContext, nextBtn, prevBtnBtn);
        
        this.dataObj      = null;
        this.fieldsNames  = fields;
        this.makeStretchy = makeStretchy;
        this.labels       = labels;
        
        init(title, fields, null, null, lengths);
    }
    
    /**
     * @param title
     * @param fields
     * @param required
     */
    protected void init(final String    title, 
                        final String[]  fields, 
                        final boolean[] required, 
                        final String[]  types,
                        final Integer[] lens)
    {
        
        Pair<String, String> rowCol = getRowColDefs();
        
        builder = new PanelBuilder(new FormLayout(rowCol.first, rowCol.second), this);
        row = 1;
        
        builder.add(createI18NLabel(title, SwingConstants.CENTER), cc.xywh(1, row, 4, 1));row += 2;

        if (required != null)
        {
            reqHash = new Hashtable<String, Boolean>();
        }
        
        int i = 0;
        for (String fName : fields)
        {
            if (fName.equals("-"))
            {
                JComponent c = builder.addSeparator(UIRegistry.getResourceString(labels[i]), cc.xyw(1, row, 4));
                compList.add(c);
                
            } else if (fName.equals(" "))
            {
                JLabel lbl = UIHelper.createLabel("");
                builder.add(lbl, cc.xyw(1, row, 4));
                compList.add(lbl);
                
            } else
            {
                JComponent comp;
                if (reqHash != null)
                {
                    reqHash.put(fName, required[i]);
                }
                
                if (types != null && types[i].equals("checkbox"))
                {
                    comp = createCheckBox(builder, labels[i], row);

                } else
                {
                    comp = createField(builder, labels[i], required != null ? required[i] : true, row, lens != null ? lens[i] : null);
                }
                compList.add(comp);
                comps.put(fName, comp);
            }
            row += 2;
            i++;
        }
        updateBtnUI();
    }
    
    /**
     * @return the Row and Column JGoodies definitions
     */
    protected Pair<String, String> getRowColDefs()
    {
        String rowDef = "p,5px" + (fieldsNames.length > 0 ? ","+createDuplicateJGoodiesDef("p", "2px", fieldsNames.length) : "") + getAdditionalRowDefs() + ",p:g";
        return new Pair<String, String>("p,2px,p,f:p:g", rowDef);
    }
    
    /**
     * @return any additional row definitions to be inserted before the final "p:g"
     */
    protected String getAdditionalRowDefs()
    {
        return "";
    }
    
    /**
     * @return
     */
    public DataGetterForObj getGetter()
    {
        return getter;
    }

    /**
     * @param getter
     */
    public void setGetter(DataGetterForObj getter)
    {
        this.getter = getter;
    }

    /**
     * @return
     */
    public DataSetterForObj getSetter()
    {
        return setter;
    }

    /**
     * @param setter
     */
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
            Object val = null;
            JComponent comp = comps.get(fName);
            if (comp instanceof JTextField)
            {
                val = ((JTextField)comp).getText();
                props.put(fName, val);

            } else if (comp instanceof JCheckBox)
            {
                Boolean isChecked = ((JCheckBox)comp).isSelected();
                props.put(fName, isChecked);
            }
            
            if (dataObj != null && setter != null && val != null)
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(final Properties values)
    {
        properties = values;
        
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
        int i = 0;
        for (String fName : comps.keySet())
        {
            JComponent comp = comps.get(fName);
            if (comp instanceof JTextField)
            {
                if ((reqHash == null || reqHash.get(fName)) && StringUtils.isEmpty(((JTextField)comp).getText()))
                {
                    return false;
                }
            }
            i++;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getSummary()
     */
    @Override
    public List<Pair<String, String>> getSummary()
    {
        List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        for (int i=0;i<labels.length;i++)
        {
            JComponent comp = compList.get(i);
            if (comp instanceof JTextField)
            {
                list.add(new Pair<String, String>(UIRegistry.getResourceString(labels[i]), ((JTextField)comp).getText()));
                
            } else if (comp instanceof JCheckBox)
            {
                JCheckBox chkbx = (JCheckBox)comp;
                list.add(new Pair<String, String>(UIRegistry.getResourceString(labels[i]), 
                        UIRegistry.getResourceString(Boolean.toString(chkbx.isSelected()))));
            }
        }
        return list;
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
