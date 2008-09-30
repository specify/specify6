/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

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

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.specify.datamodel.CollectionObject;

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
    protected JCheckBox isNumericChk    = createCheckBox("Is Numeric Only?");// I18N
    protected JComboBox catNumSchemeCBX = createComboBox(new DefaultComboBoxModel());
    protected JLabel    isNumericLbl    = createLabel("");
    
    public CatNumScheme(final JButton nextBtn)
    {
        super("cns", nextBtn);
        
        
        catNumSchemeCBX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                UIFieldFormatterIFace fmt = (UIFieldFormatterIFace)catNumSchemeCBX.getSelectedItem();
                if (fmt != null)
                {
                    isNumericLbl.setText(getResourceString(fmt.isNumeric() ? "Yes" : "No"));
                }
            }
        });
        catNumSchemeCBX.getModel().addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent e)
            {
                UIFieldFormatterIFace fmt = (UIFieldFormatterIFace)catNumSchemeCBX.getSelectedItem();
                if (fmt != null)
                {
                    isNumericLbl.setText(getResourceString(fmt.isNumeric() ? "Yes" : "No"));
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
        
        for (UIFieldFormatterIFace fmt : UIFieldFormatterMgr.getInstance().getFormatterList(CollectionObject.class))
        {
            //System.out.println(fmt.getName()+"  "+fmt.getTitle());
            ((DefaultComboBoxModel)catNumSchemeCBX.getModel()).addElement(fmt);
        }
        

        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,4px,p,f:p:g", "p,4px,p,4px,p"), this);
        pb.add(createLabel("Choose a Catalog Numbering Format:"), cc.xywh(1, 1, 4, 1)); // I18N
        pb.add(createLabel("Format:", SwingConstants.RIGHT), cc.xy(1, 3));// I18N
        pb.add(catNumSchemeCBX, cc.xy(3, 3));
        
        pb.add(createLabel("Is Numeric:", SwingConstants.RIGHT), cc.xy(1, 5));// I18N
        pb.add(isNumericLbl, cc.xy(3, 5));

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues()
     */
    @Override
    public void getValues(final Properties props)
    {
        props.put(makeName("catnumsheme"), catNumSchemeCBX.getSelectedItem().toString());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(Properties values)
    {
        //dbNameTxt.setText(values.getProperty(makeName("dbname")));
        
        //String driverName = values.get(makeName("driver");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#updateBtnUI()
     */
    @Override
    public void updateBtnUI()
    {
        // TODO Auto-generated method stub

    }

}
