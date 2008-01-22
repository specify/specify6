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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 18, 2008
 *
 */
public class CatNumScheme extends BaseSetupPanel
{
    protected JCheckBox isNumericChk    = new JCheckBox("Is Numeric Only?");
    protected JComboBox catNumSchemeCBX = new JComboBox(new DefaultComboBoxModel());
    protected JLabel    isNumericLbl    = new JLabel("");
    
    public CatNumScheme(final JButton nextBtn)
    {
        super("cns", nextBtn);
        
        
        catNumSchemeCBX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                UIFieldFormatterIFace fmt = (UIFieldFormatterIFace)catNumSchemeCBX.getSelectedItem();
                if (fmt != null)
                {
                    isNumericLbl.setText(fmt.isNumeric() ? "Yes" : "No");
                }
            }
        });
        catNumSchemeCBX.getModel().addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent e)
            {
                UIFieldFormatterIFace fmt = (UIFieldFormatterIFace)catNumSchemeCBX.getSelectedItem();
                if (fmt != null)
                {
                    isNumericLbl.setText(fmt.isNumeric() ? "Yes" : "No");
                }
            }
            public void intervalAdded(ListDataEvent e)
            {
                contentsChanged(e);
            }
            public void intervalRemoved(ListDataEvent e)
            {
                contentsChanged(e);
            }
        });
        
        for (UIFieldFormatterIFace fmt : UIFieldFormatterMgr.getFormatterList(CollectionObject.class))
        {
            System.out.println(fmt.getName()+"  "+fmt.getTitle());
            ((DefaultComboBoxModel)catNumSchemeCBX.getModel()).addElement(fmt);
        }
        

        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,4px,p,f:p:g", "p,4px,p,4px,p"), this);
        pb.add(new JLabel("Choose a Catalog Numbering Format:"), cc.xywh(1, 1, 4, 1));
        pb.add(new JLabel("Format:", SwingConstants.RIGHT), cc.xy(1, 3));
        pb.add(catNumSchemeCBX, cc.xy(3, 3));
        
        pb.add(new JLabel("Is Numeric:", SwingConstants.RIGHT), cc.xy(1, 5));
        pb.add(isNumericLbl, cc.xy(3, 5));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues()
     */
    @Override
    protected void getValues(final Properties props)
    {
        props.put(makeName("catnumsheme"), catNumSchemeCBX.getSelectedItem().toString());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    protected void setValues(Properties values)
    {
        //dbNameTxt.setText(values.getProperty(makeName("dbname")));
        
        //String driverName = values.get(makeName("driver");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#isUIValid()
     */
    @Override
    protected boolean isUIValid()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#updateBtnUI()
     */
    @Override
    protected void updateBtnUI()
    {
        // TODO Auto-generated method stub

    }

}
