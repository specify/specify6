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

import java.awt.Dialog;

import javax.swing.SwingUtilities;

import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 8, 2008
 *
 */
public class BioStratBusRules extends GeologicTimePeriodBusRules
{

    /**
     * 
     */
    public BioStratBusRules()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
        GeologicTimePeriod gtp = (GeologicTimePeriod)dataObj;
        if (gtp != null)
        {
            gtp.setIsBioStrat(true);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.GeologicTimePeriodBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        GeologicTimePeriod gtp = (GeologicTimePeriod)dataObj;
        if (gtp != null)
        {
            gtp.setIsBioStrat(true);
        }
        super.beforeSave(dataObj, session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        GeologicTimePeriod gtp = (GeologicTimePeriod)dataObj;
        if (gtp != null)
        {
            gtp.setIsBioStrat(true);
        }
        
        final ValCheckBox chkbx = formViewObj.getCompById("isBioStrat");
        if (chkbx != null)
        {
            chkbx.setSelected(true);
            chkbx.setCurrentValue(true);
            
            // I know this is incrediably Lame approach to renaming the dialog
            // but it is the easiest thing to do
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
                    Dialog dlg = UIHelper.getDialog(chkbx);
                    if (dlg != null)
                    {
                        dlg.setTitle(UIRegistry.getResourceString("BIOSTRAT"));
                    }
                }
            });
        }
    }
}
