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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.init.NumberingSchemeSetupDlg;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 28, 2008
 *
 */
public class CollectionSetupBusRules extends BaseBusRules
{
    protected Collection          collection      = null;
    
    /**
     * @param dataClasses
     */
    public CollectionSetupBusRules()
    {
        super(Collection.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        collection = (Collection)dataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
        
        Collection col = (Collection)dataObj;
        try
        {
            for (AutoNumberingScheme ns : col.getNumberingSchemes())
            {
                session.attach(ns);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(final Object newDataObj)
    {
        super.addChildrenToNewDataObjects(newDataObj);
        
        MultiView disciplineMV = formViewObj.getMVParent().getMultiViewParent();
        MultiView divisionMV   = disciplineMV.getMultiViewParent();
        
        NumberingSchemeSetupDlg dlg;
        if (UIRegistry.getMostRecentWindow() instanceof Dialog)
        {
            dlg = new NumberingSchemeSetupDlg((Dialog)UIRegistry.getMostRecentWindow(), 
                    (Division)divisionMV.getData(), 
                    (Discipline)disciplineMV.getData(), 
                    (Collection)newDataObj);
        } else
        {
            dlg = new NumberingSchemeSetupDlg((Frame)UIRegistry.getMostRecentWindow(), 
                    (Division)divisionMV.getData(), 
                    (Discipline)disciplineMV.getData(), 
                    (Collection)newDataObj);
        }
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            collection.getNumberingSchemes().add(dlg.getNumScheme());
            dlg.getNumScheme().getCollections().add(collection);
            divisionMV.addToBeSavedItem(dlg.getNumScheme());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        JButton btn = formViewObj.getCompById("newNumSchemeBTN");
        if (btn != null)
        {
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    createNewNumScheme();
                }
            });
            btn = formViewObj.getCompById("chooseNumSchemeBTN");
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    //chooseNumScheme();
                }
            });
        }
    }
    
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeDelete(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeDelete(dataObj, session);
    }

    /**
     * 
     */
    protected void createNewNumScheme()
    {
        ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Dialog)UIRegistry.getMostRecentWindow(),
                null,
                "CatAutoNumberingScheme",
                null,
                "Create Numbering Scheme", // I18N ?
                null,
                AutoNumberingScheme.class.getName(),
                "autoNumberingSchemeId",
                true,
                MultiView.HIDE_SAVE_BTN);
        AutoNumberingScheme scheme = new AutoNumberingScheme();
        scheme.initialize();
        scheme.setTableNumber(CollectionObject.getClassTableId());
        dlg.setData(scheme);
        UIHelper.centerAndShow(dlg);
        if (!dlg.isCancelled())
        {
            ValTextField numSchemTxt = formViewObj.getCompById("numScheme");
            numSchemTxt.setText(scheme.getIdentityTitle());
            
            if (collection != null)
            {
                collection.getNumberingSchemes().add(scheme);
                scheme.getCollections().add(collection);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return super.okToEnableDelete(dataObj);
    }

}
