/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.BaseBusRules;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.validation.ValCheckBox;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 25, 2008
 *
 */
public class AddressBusRules extends BaseBusRules
{
    protected Address        address = null;
    protected ActionListener addrAL  = null;
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object, edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void afterFillForm(Object dataObj, Viewable viewableArg)
    {
        super.afterFillForm(dataObj, viewableArg);
        
        if (formViewObj.getDataObj() instanceof Address)
        {
            address = (Address)formViewObj.getDataObj();
            
            Component comp = formViewObj.getControlByName("isPrimary");
            if (comp instanceof ValCheckBox)
            {
                if (addrAL == null)
                {
                    addrAL = new ActionListener() 
                    {
                        //@Override
                        public void actionPerformed(final ActionEvent e)
                        {
                            SwingUtilities.invokeLater(new Runnable() {
                                 //@Override
                                 public void run()
                                 {
                                     primaryAddressSelected(e);
                                 }
                             });
                        }
                    };
                    ((ValCheckBox)comp).addActionListener(addrAL);
                }
            }
        }
    }
    
    /**
     * @param e
     */
    protected void primaryAddressSelected(ActionEvent e)
    {
        if (address != null)
        {
            final JCheckBox cbx = (JCheckBox)e.getSource();
            
            boolean isSelected = cbx.isSelected();
            if (isSelected)
            {
                Agent agent = address.getAgent();
                if (agent != null)
                {
                    for (Address addr : agent.getAddresses())
                    {
                        if (addr.getIsPrimary())
                        {
                            JOptionPane.showMessageDialog(null, UIRegistry.getResourceString("ADDR_HASPRIMARY_ALREADY"));
                            cbx.setSelected(false);
                            break;
                        }
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        
        addrAL = null;
        address = null;
    }


}
