/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.List;
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
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.CollectionObject;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 18, 2008
 *
 */
public class FormatterPickerPanel extends BaseSetupPanel
{
    protected JCheckBox isNumericChk    = createCheckBox(getResourceString("IS_NUM_CHK"));
    protected JComboBox formatterCBX    = createComboBox(new DefaultComboBoxModel());
    protected JLabel    isNumericLbl    = createLabel("");
    protected boolean   doingCatNums;
    protected List<UIFieldFormatterIFace> fmtList;
    
    public FormatterPickerPanel(final JButton nextBtn, 
                                final boolean doingCatNums)
    {
        super("autonumber", nextBtn);
        
        this.doingCatNums = doingCatNums;
        
        formatterCBX.getModel().addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent e)
            {
                int index = formatterCBX.getSelectedIndex();
                if (index > -1)
                {
                    UIFieldFormatterIFace fmt = fmtList.get(index);
                    if (fmt != null)
                    {
                        isNumericLbl.setText(getResourceString(fmt.isNumeric() ? "Yes" : "No"));
                    }
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
        
        fmtList = UIFieldFormatterMgr.getInstance().getFormatterList(doingCatNums ? CollectionObject.class : Accession.class);
        
        if (!doingCatNums)
        {
            ((DefaultComboBoxModel)formatterCBX.getModel()).addElement(getResourceString("NONE"));
        }
        
        for (UIFieldFormatterIFace fmt : fmtList)
        {
            ((DefaultComboBoxModel)formatterCBX.getModel()).addElement(fmt.getName());
        }

        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,4px,p,f:p:g", "p,4px,p,4px,p"), this);
        int y = 1;
        String label = getLocalizedMessage("CHOOSE_FMT", getResourceString(doingCatNums ? "CATALOG" : "ACCESSION"));
        pb.add(createLabel(label), cc.xywh(1, y, 4, 1)); // I18N
        y +=2;
        
        pb.add(createI18NFormLabel("FORMAT", SwingConstants.RIGHT), cc.xy(1, y));// I18N
        pb.add(formatterCBX, cc.xy(3, y));
        y +=2;

        if (doingCatNums)
        {
            pb.add(createI18NFormLabel("IS_NUM", SwingConstants.RIGHT), cc.xy(1, y));// I18N
            pb.add(isNumericLbl, cc.xy(3, y));
            y +=2;
        }

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues()
     */
    @Override
    public void getValues(final Properties props)
    {
        if (doingCatNums || formatterCBX.getSelectedIndex() > 0)
        {
            props.put(doingCatNums ? "catnumfmt" : "accnumfmt", formatterCBX.getSelectedItem());
        } else
        {
            props.remove(doingCatNums ? "catnumfmt" : "accnumfmt");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    public void setValues(Properties values)
    {
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
    }
}
