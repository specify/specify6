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

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Dec 18, 2008
 *
 */
public class InstitutionBusRules extends BaseBusRules
{

    /**
     * @param dataClasses
     */
    public InstitutionBusRules()
    {
        super(Institution.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null && formViewObj.getMVParent().isTopLevel())
        {
            ResultSetController rsc = formViewObj.getRsController();
            if (rsc != null)
            {
                if (rsc.getNewRecBtn() != null) rsc.getNewRecBtn().setVisible(false);
                if (rsc.getDelRecBtn() != null) rsc.getDelRecBtn().setVisible(false);
            }
            
            ValSpinner minPwdLenSpinner = (ValSpinner)formViewObj.getControlById("minimumPwdLength");
            if (minPwdLenSpinner != null)
            {
                
                minPwdLenSpinner.setRange(Institution.MIN_PASSWORD_LEN,  // min
                                          30,                            // max
                                          Institution.MIN_PASSWORD_LEN); // val
            }
        }
    }
    
    /**
     * @param name
     * @return
     */
    private int getNameCount(final String name)
    {
        return BasicSQLUtils.getCountAsInt(String.format("SELECT COUNT(*) FROM institution WHERE Name = '%s'", name));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#isOkToSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean isOkToSave(final Object dataObj, final DataProviderSessionIFace session)
    {
        if (formViewObj != null)
        {
            Component comp = formViewObj.getControlByName("name");
            if (comp instanceof ValTextField)
            {
                Institution inst   = (Institution)formViewObj.getDataObj();
                Integer     instId = inst.getId();
                
                String name = ((ValTextField)comp).getText();
                int cnt = getNameCount(name);
                if (cnt == 0 || (cnt == 1 && instId != null))
                {
                    return true;
                }
               reasonList.add(UIRegistry.getLocalizedMessage("DIVNAME_DUP", name));
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean afterSaveCommit(Object dataObj, DataProviderSessionIFace session)
    {
        AppContextMgr.getInstance().setClassObject(Institution.class, dataObj);
        
        return super.afterSaveCommit(dataObj, session);
    }
    
    
}
