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

import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAuthorization;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.RepositoryAgreement;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 14, 2009
 *
 */
public class AccessionAuthorizationBusRules extends BaseBusRules
{
    protected ValComboBoxFromQuery permitQCBX = null;
    
    /**
     * 
     */
    public AccessionAuthorizationBusRules()
    {
        super(AccessionAuthorizationBusRules.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(final Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null && formViewObj.isEditing())
        {
            permitQCBX = formViewObj.getCompById("1");
            if (permitQCBX != null)
            {
                permitQCBX.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (!e.getValueIsAdjusting())
                        {
                            FormDataObjIFace parentData = (FormDataObjIFace)formViewObj.getParentDataObj();
                            Set<?>           setOfData  = parentData instanceof Accession ? ((Accession)parentData).getAccessionAuthorizations() :
                                                          ((RepositoryAgreement)parentData).getRepositoryAgreementAuthorizations();
                            
                            Permit permit = (Permit)permitQCBX.getValue();
                            if (countDataObjectById(setOfData, permit) > 1)
                            {
                                UIRegistry.showLocalizedError("ACCAUTH_DUP", permit.getIdentityTitle());
                                
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        permitQCBX.setValue(null, null);
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeDelete(Object dataObj, DataProviderSessionIFace session)
    {
        AccessionAuthorization accAuth = (AccessionAuthorization) dataObj;
        Permit                 permit  = accAuth.getPermit();
        if (permit != null && session != null)
        {
            session.attach(permit);
            removeById(permit.getAccessionAuthorizations(), accAuth);
            accAuth.setPermit(null);
        }
        if (accAuth.getAccession() != null)
        {
            removeById(accAuth.getAccession().getAccessionAuthorizations(), accAuth);
            accAuth.setAccession(null);
        }
    }

}
