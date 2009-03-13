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

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * This is the configuration window for create a new discipline.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 8, 2009
 *
 */
public class DisciplinePanel extends BaseSetupPanel
{
    protected JTextField         disciplineName;
    protected JComboBox          disciplines;
    
    /**
     * Creates a dialog for entering database name and selecting the appropriate driver.
     */
    public DisciplinePanel(final JButton nextBtn)
    {
        super("DISCIPLINE", nextBtn);
        
        String header = getResourceString("DISP_INFO") + ":";

        CellConstraints cc = new CellConstraints();
        
        
        String rowDef = "p,2px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", 2) + ",p:g";
        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p:g", rowDef), this);
        int row = 1;
        
        builder.add(createLabel(header, SwingConstants.CENTER), cc.xywh(1,row,3,1));row += 2;
        
        Vector<DisciplineType> dispList = new Vector<DisciplineType>();
        for (DisciplineType disciplineType : DisciplineType.getDisciplineList())
        {
            if (disciplineType.getType() == 0)
            {
                dispList.add(disciplineType);
            }
        }
        disciplines = createComboBox(dispList);
        
        // Select Fish as the default
        for (DisciplineType disciplineType : dispList)
        {
            if (disciplineType.getName().equals("fish"))
            {
                disciplines.setSelectedItem(disciplineType);
            }
        }
        
        // Discipline 
        JLabel lbl = createI18NFormLabel("DSP_TYPE", SwingConstants.RIGHT);
        lbl.setFont(bold);
        builder.add(lbl,         cc.xy(1, row));
        builder.add(disciplines, cc.xy(3, row));
        row += 2;
        
        disciplineName     = createField(builder, "DISP_NAME",  true, row);       row += 2;
        
        updateBtnUI();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues()
     */
    @Override
    public void getValues(final Properties props)
    {
        props.put("dispName", disciplineName.getText());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(Properties values)
    {
        disciplineName.setText(values.getProperty("dispName"));
    }

    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    public void updateBtnUI()
    {
        boolean isValid = isUIValid();
        if (nextBtn != null)
        {
            nextBtn.setEnabled(isValid);
        }
    }
    
    /**
     * Checks all the textfields to see if they have text
     * @return true of all fields have text
     */
    public boolean isUIValid()
    {
        if (StringUtils.isEmpty(disciplineName.getText()))
        {
            return false;
        }
        return true;
    }
    
    // Getters 
    
    /**
     * @return
     */
    public String getDisciplineTitle()
    {
        return disciplineName.getText();
    }

    /**
     * @return
     */
    public DisciplineType getDisciplineType()
    {
        return (DisciplineType)disciplines.getSelectedItem();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getSummary()
     */
    @Override
    public List<Pair<String, String>> getSummary()
    {
        List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        list.add(new Pair<String, String>(getResourceString("DSP_TYPE"), disciplines.getSelectedItem().toString()));
        list.add(new Pair<String, String>(getResourceString("DSP_NAME"), disciplineName.getText()));
        return list;
    }
    
    
}
