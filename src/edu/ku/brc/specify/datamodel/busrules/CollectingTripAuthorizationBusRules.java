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

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.CollectingTrip;
import edu.ku.brc.specify.datamodel.CollectingTripAuthorization;
import edu.ku.brc.ui.UIRegistry;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Set;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 14, 2009
 *
 */
public class CollectingTripAuthorizationBusRules extends BaseBusRules
{
    protected ValComboBoxFromQuery permitQCBX = null;

    /**
     *
     */
    public CollectingTripAuthorizationBusRules()
    {
        super(CollectingTripAuthorizationBusRules.class);
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
            Component comp = formViewObj.getCompById("1");
            if (comp instanceof ValComboBoxFromQuery)
            {
                permitQCBX = (ValComboBoxFromQuery)comp;
                permitQCBX.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (e != null && !e.getValueIsAdjusting())
                        {
                            FormDataObjIFace parentData = (FormDataObjIFace)formViewObj.getParentDataObj();
                            Set<?> setOfData  = ((CollectingTrip)parentData).getCollectingTripAuthorizations();
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
    public Object beforeDelete(Object dataObj, DataProviderSessionIFace session)
    {
        CollectingTripAuthorization ceAuth = (CollectingTripAuthorization) dataObj;
        Permit                 permit  = ceAuth.getPermit();
        if (permit != null && session != null)
        {
            session.attach(permit);
            removeById(permit.getCollectingTripAuthorizations(), ceAuth);
            ceAuth.setPermit(null);
        }
        if (ceAuth.getCollectingTrip() != null)
        {
            removeById(ceAuth.getCollectingTrip().getCollectingTripAuthorizations(), ceAuth);
            ceAuth.setCollectingTrip(null);
        }
        return dataObj;
    }

}
