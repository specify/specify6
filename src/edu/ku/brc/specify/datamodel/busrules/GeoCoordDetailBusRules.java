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
/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.util.Calendar;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.SpecifyUser;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Apr 14, 2011
 *
 */
public class GeoCoordDetailBusRules extends BaseBusRules
{
    private ValComboBoxFromQuery        geoRefDetByQCBX = null;
    private ValFormattedTextFieldSingle geoRefDetDateTF = null;
    
    /**
     * @param dataClasses
     */
    public GeoCoordDetailBusRules()
    {
        super(GeoCoordDetailBusRules.class);
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#initialize(edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null)
        {
            geoRefDetByQCBX = formViewObj.getCompById("1");
            if (geoRefDetByQCBX == null)
            {
                geoRefDetByQCBX = formViewObj.getCompById("geoRefDetBy");
                if (geoRefDetByQCBX == null)
                {
                    return;
                }
            }
            
            geoRefDetDateTF = formViewObj.getCompById("2");
            if (geoRefDetDateTF == null)
            {
                geoRefDetDateTF = formViewObj.getCompById("geoRefDetDate");
                if (geoRefDetDateTF == null)
                {
                    return;
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (geoRefDetByQCBX != null && geoRefDetByQCBX.isEnabled() && isEditMode() && isNewObject())
        {
            // Always get a new copy of the Agent Object
            SpecifyUser spUser    = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
            if (spUser != null)
            {
                Agent userAgent = AppContextMgr.getInstance().getClassObject(Agent.class);
                if (userAgent != null)
                {
                    DataProviderSessionIFace session = null;
                    try
                    {
                        session = DataProviderFactory.getInstance().createSession();
                        userAgent = session.get(Agent.class, userAgent.getId());
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseTreeBusRules.class, ex);
        
                    } finally
                    {
                        if (session != null) session.close();
                    }
                    if (userAgent != null)
                    {
                        geoRefDetByQCBX.setValue(userAgent, null);
                    }
                }
            }
        }
        
        if (geoRefDetDateTF != null && geoRefDetDateTF.isEnabled() && isEditMode() && isNewObject())
        {
            geoRefDetDateTF.setValue(Calendar.getInstance(), null);
        }
    }

}
