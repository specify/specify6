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

import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.createI18NLabel;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.DataGetterForObj;
import edu.ku.brc.af.ui.forms.DataSetterForObj;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
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

    protected PanelBuilder          builder   = null;
    protected int                   row       = 1;
    protected String[]              labels;
    protected ArrayList<JComponent> compList  = new ArrayList<JComponent>();
    
    /**
     * @param panelName
     * @param nextBtn
     */
    public GenericFormPanel(final String panelName,
                            final JButton nextBtn)
    {
        this(panelName, nextBtn, false);
    }

    /**
     * @param panelName
     * @param nextBtn
     * @param makeStretchy
     */
    public GenericFormPanel(final String panelName,
                            final JButton nextBtn,
                            final boolean makeStretchy)
    {
        super(panelName, nextBtn, makeStretchy);
    }

    /**
     * @param name
     * @param title
     * @param labels
     * @param fields
     * @param nextBtn
     */
    public GenericFormPanel(final String   name,
                            final String   title,
                            final String[] labels,
                            final String[] fields, 
                            final JButton  nextBtn,
                            final boolean makeStretchy)
    {
        this(null, name, title, labels, fields, null, nextBtn, makeStretchy);
    }
    
    /**
     * @param name
     * @param title
     * @param labels
     * @param fields
     * @param required
     * @param nextBtn
     */
    public GenericFormPanel(final String   name,
                            final String   title,
                            final String[] labels,
                            final String[] fields, 
                            final boolean[] required, 
                            final JButton  nextBtn,
                            final boolean makeStretchy)
    {
        this(null, name, title, labels, fields, required, nextBtn, makeStretchy);
        
    }
    
    /**
     * @param dataObj
     * @param name
     * @param title
     * @param labels
     * @param fields
     * @param required
     * @param nextBtn
     */
    public GenericFormPanel(final FormDataObjIFace dataObj,
                            final String   name,
                            final String   title,
                            final String[] labels,
                            final String[] fields, 
                            final boolean[] required, 
                            final JButton  nextBtn,
                            final boolean makeStretchy)
    {
        super(name, nextBtn);
        
        this.dataObj      = dataObj;
        this.fieldsNames  = fields;
        this.makeStretchy = makeStretchy;
        this.labels       = labels;
        
        init(title, fields, required);
    }
    
    /**
     * @param title
     * @param fields
     * @param required
     */
    protected void init(final String    title, 
                        final String[]  fields, 
                        final boolean[] required)
    {
        CellConstraints cc = new CellConstraints();
        
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
                
            } else
            {
                if (reqHash != null)
                {
                    reqHash.put(fName, required[i]);
                }
                JComponent comp = createField(builder, labels[i], required != null ? required[i] : true, row);
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
        return new Pair<String, String>("p,2px,p,f:p:g", 
                                        "p,5px," + createDuplicateJGoodiesDef("p", "2px", fieldsNames.length) +
                                        getAdditionalRowDefs() + ",p:g");
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
