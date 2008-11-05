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

import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rodss
 *
 * @code_status Alpha
 *
 * Created Date: Nov 29, 2007
 *
 */
/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 1, 2008
 *
 */
public class DeterminationBusRules extends BaseBusRules
{
    
    protected Determination determination = null;
    
    protected ActionListener detAL           = null;
    protected boolean        ignoreSelection = false;

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#beforeFormFill()
     */
    @Override
    public void beforeFormFill()
    {
        determination = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        determination = null;
        
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
                
                
                comp.setEnabled(!isSynonymyDet(determination));
            }
        }
    }
    
    /**
     * @param e
     */
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
                if (status != null && DeterminationStatus.isCurrentType(status.getType()))
                {
                    determination.setStatus(status);
                    
                    CollectionObject colObj = determination.getCollectionObject();
                    if (colObj != null)
                    {
                        if (!checkDeterminationStatus(colObj, determination))
                        {
                            ignoreSelection = true;
                            cbx.setSelectedIndex(-1);
                            ignoreSelection = false;
                            JOptionPane.showMessageDialog(null, UIRegistry.getResourceString("DT_ALREADY_DETERMINATION"));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Checks to make sure there is a single 'current' determination.
     * @param colObj the Collection Object
     * @param deter the determination for the CO
     * @return true is ok
     */
    protected boolean checkDeterminationStatus(final CollectionObject colObj, final Determination deter)
    {
        if (deter.isCurrent())
        {
            for (Determination det : colObj.getDeterminations())
            {
                if (det != deter && det.isCurrent())
                {
                    return false;
                }
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object, java.lang.Object, boolean)
     */
    @Override
    public STATUS processBusinessRules(Object parentDataObj,
                                       Object dataObj,
                                       boolean isExistingObject)
    {
        STATUS status = super.processBusinessRules(parentDataObj, dataObj, isExistingObject);
        
        if (status == STATUS.OK)
        {
            if (!checkDeterminationStatus((CollectionObject)parentDataObj, (Determination)dataObj))
            {
                reasonList.add(UIRegistry.getResourceString("DT_ALREADY_DETERMINATION"));
                status = STATUS.Error;
            }
        }
        return status;
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
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        determination = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#addChildrenToNewDataObjects(java.lang.Object)
     */
    @Override
    public void addChildrenToNewDataObjects(final Object newDataObj)
    {
        super.addChildrenToNewDataObjects(newDataObj);

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
//        if (!super.okToEnableDelete(dataObj))
//        {
//            return false;
//        }
//        
//        Determination detObj = (Determination )dataObj;
//        if (detObj.getOldSynonymyDeterminations().size() > 0 || detObj.getNewSynonymyDeterminations().size() > 0)
//        {
//            return false;
//        }

        return super.okToEnableDelete(dataObj);
    }

    /**
     * @param det
     * @return true if det was created or modified as a result of a synonymization
     */
    protected boolean isSynonymyDet(final Determination det)
    {
        if (det.getId() == null)
        {
            return false;
        }
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            return session.createQuery("select id from SpSynonymyDetermination where oldDeterminationId = " 
                    + det.getId() + " or newDeterminationId = " + det.getId(), false).list().size() > 0;
        }
        finally
        {
            session.close();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(Object dataObj,
                           DataProviderSessionIFace session,
                           BusinessRulesOkDeleteIFace deletable)
    {
        if (deletable != null)
        {
            Determination det = (Determination )dataObj;
            boolean doDelete = true;
            if (isSynonymyDet(det))
            {
                doDelete = UIRegistry.displayConfirmLocalized("DeterminationBusRule.SynDetDelTitle", "DeterminationBusRule.SynDetDelMsg", "YES", "CANCEL", JOptionPane.QUESTION_MESSAGE);
            }
            if (doDelete)
            {
                deletable.doDeleteDataObj(dataObj, session, true);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeSaveCommit(Object dataObj, DataProviderSessionIFace session)
            throws Exception
    {
        if (!super.beforeSaveCommit(dataObj, session))
        {
            return false;
        }
        
        if (dataObj == null)
        {
            return true;
        }
        
        if (isSynonymyDet((Determination )dataObj))
        {
            return UIRegistry.displayConfirmLocalized("DeterminationBusRule.SynDetDelTitle", "DeterminationBusRule.SynDetDelMsg", "YES", "CANCEL", JOptionPane.QUESTION_MESSAGE);
        }
        
        return true;
    }

    
}
