/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.ui.UIRegistry;

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
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (formViewObj.getDataObj() instanceof Address)
        {
            address = (Address)formViewObj.getDataObj();
            
            if (isEditMode())
            {
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
