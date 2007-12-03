/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.ui.db.PickListItemIFace;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.validation.ValComboBox;

/**
 * @author rodss
 *
 * @code_status Alpha
 *
 * Created Date: Nov 29, 2007
 *
 */
public class DeterminationBusRules extends BaseBusRules
{
    
    protected Determination determination = null;
    
    protected ActionListener detAL           = null;
    protected boolean        ignoreSelection = false;

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeFormFill(edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void beforeFormFill(Viewable viewable)
    {
        determination = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#fillForm(java.lang.Object, edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void afterFillForm(Object dataObj, Viewable viewable)
    {
        determination = null;
        
        if (viewable instanceof FormViewObj)
        {
            FormViewObj formViewObj = (FormViewObj)viewable;
            if (formViewObj.getDataObj() instanceof Determination)
            {
                determination = (Determination)formViewObj.getDataObj();
                
                Component comp     = formViewObj.getControlByName("status");
                if (comp instanceof ValComboBox)
                {
                    if (detAL == null)
                    {
                        detAL = new ActionListener() 
                        {
                            //@Override
                            public void actionPerformed(final ActionEvent e)
                            {
                                SwingUtilities.invokeLater(new Runnable() {
                                     //@Override
                                     public void run()
                                     {
                                         determinationStatusSelected(e);
                                     }
                                 });
                            }
                        };
                    }
                    
                    JComboBox cbx = ((ValComboBox)comp).getComboBox();
                    boolean fnd = false;
                    for (ActionListener al : cbx.getActionListeners())
                    {
                        if (al == detAL)
                        {
                            fnd = true;
                            break;
                        }
                    }
                    if (!fnd)
                    {
                        cbx.addActionListener(detAL);
                    }
                }
            }
        }
    }
    
    protected void determinationStatusSelected(ActionEvent e)
    {
        if (ignoreSelection)
        {
            return;
        }
        
        if (determination != null)
        {
            final JComboBox cbx = (JComboBox)e.getSource();
            
            PickListItemIFace item = (PickListItemIFace)cbx.getSelectedItem();
            if (item != null)
            {
                DeterminationStatus status = (DeterminationStatus)item.getValueObject();
                if (status != null && status.getIsCurrent())
                {
                    CollectionObject colObj = determination.getCollectionObject();
                    if (colObj != null)
                    {
                        for (Determination det : colObj.getDeterminations())
                        {
                            if (det != determination && det.getStatus() != null && det.getStatus().getIsCurrent())
                            {
                                JOptionPane.showMessageDialog(null, "There is already a current Determination."); // I18N 
                                ignoreSelection = true;
                                cbx.setSelectedIndex(-1);
                                ignoreSelection = false;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterDeleteCommit(java.lang.Object)
     */
    @Override
    public void afterDeleteCommit(Object dataObj)
    {
        determination = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#afterSaveCommit(java.lang.Object)
     */
    @Override
    public boolean afterSaveCommit(Object dataObj)
    {
        determination = null;
        return super.afterSaveCommit(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        determination = null;
    }

}
