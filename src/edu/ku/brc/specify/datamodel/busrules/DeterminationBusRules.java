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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace.CreationMode;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIHelper;
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
    
    protected Determination  determination         = null;

    protected ActionListener altTaxUsageAL         = null;
    protected boolean        ignoreSelection       = false;
    protected boolean        checkedBlankUsageItem = false;

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

            Component activeTax = formViewObj.getControlByName("activeTaxon");
            if (activeTax != null)
            {
                activeTax.setFocusable(false);
                if (determination != null && determination.getActiveTaxon() != null)
                {
                    ((JTextField )activeTax).setText(determination.getActiveTaxon().getFullName());
                }
                else
                {
                    ((JTextField )activeTax).setText("");
                }
            }

            
            if (formViewObj.getAltView().getMode() != CreationMode.EDIT)
            {
                // when we're not in edit mode, we don't need to setup any listeners since the user can't change anything
                //log.debug("form is not in edit mode: no special listeners will be attached");
                return;
            }

            
            Component altTaxUsageComp     = formViewObj.getControlByName("nameUsage");
            if (altTaxUsageComp instanceof ValComboBox)
            {
                if (!checkedBlankUsageItem)
                {
                    boolean fnd = false;
                    PickListDBAdapterIFace items = (PickListDBAdapterIFace) ((ValComboBox) altTaxUsageComp)
                            .getComboBox().getModel();
                    for (PickListItemIFace item : items.getPickList().getItems())
                    {
                        if (StringUtils.isBlank(item.getValue()))
                        {
                            fnd = true;
                            break;
                        }
                    }
                    if (!fnd)
                    {
                        boolean readOnly = items.getPickList().getReadOnly();
                        if (readOnly)
                        {
                            items.getPickList().setReadOnly(false);
                        }
                        items.addItem("", null);
                        if (readOnly)
                        {
                            items.getPickList().setReadOnly(true);
                        }
                    }
                    checkedBlankUsageItem = true;
                }
                altTaxUsageComp.setEnabled(true);
                if (altTaxUsageAL == null)
                {
                    altTaxUsageAL = new ActionListener()
                    {
                        // @Override
                        public void actionPerformed(final ActionEvent e)
                        {
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                // @Override
                                public void run()
                                {
                                    altTaxUsageSelected(e);
                                }
                            });
                        }
                    };
                }

                JComboBox cbx = ((ValComboBox) altTaxUsageComp).getComboBox();
                boolean fnd = false;
                for (ActionListener al : cbx.getActionListeners())
                {
                    if (al == altTaxUsageAL)
                    {
                        fnd = true;
                        break;
                    }
                }
                if (!fnd)
                {
                    cbx.addActionListener(altTaxUsageAL);
                }
            }

            Component taxComp = formViewObj.getControlByName("taxon");
            Component altTaxComp = formViewObj.getControlByName("alternateName");
            if (taxComp != null)
            {
                if (determination != null)
                {
                    ((ValComboBoxFromQuery) taxComp).getTextWithQuery().getTextField().setEditable(
                            StringUtils.isBlank(determination.getNameUsage()));
                }
            }
            if (altTaxComp != null)
            {
                if (determination != null)
                {
                    ((JTextField) altTaxComp).setEditable(determination.getTaxon() == null);
                }
            }
        }
    }
     
    protected void altTaxUsageSelected(ActionEvent e)
    {
        if (ignoreSelection)
        {
            return;
        }
        
        if (determination != null)
        {
            final JComboBox cbx = (JComboBox)e.getSource();
            
            PickListItemIFace item = (PickListItemIFace)cbx.getSelectedItem();
            Component taxComp = formViewObj.getControlByName("taxon");
            Component altTaxComp = formViewObj.getControlByName("alternateName");
            if (item != null && !StringUtils.isBlank(item.getValue()))
            {
                //clear and disable taxon component
                if (taxComp != null)
                {
                    ((ValComboBoxFromQuery )taxComp).setValue(null, null);
                    ((ValComboBoxFromQuery )taxComp).getTextWithQuery().getTextField().setEditable(false);
                }
                //enable alternateTaxon  component
                if (altTaxComp != null)
                {
                    ((JTextField )altTaxComp).setEditable(true);
                }
            }
            else
            {
                //clear and disable alternate component
                if (altTaxComp != null)
                {
                    
                    ((JTextField )altTaxComp).setEditable(false);
                }
                
                //enable taxon component
                if (taxComp != null)
                {
                    ((ValComboBoxFromQuery )taxComp).getTextWithQuery().getTextField().setEditable(true);                
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
        if (deter.isCurrentDet())
        {
            for (Determination det : colObj.getDeterminations())
            {
                if (det != deter && det.isCurrentDet())
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
                
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        // TODO Auto-generated method stub
        super.initialize(viewableArg);
        
        if (formViewObj != null)
        {
            GetSetValueIFace  taxonField  = (GetSetValueIFace)formViewObj.getControlByName("taxon");
            if (taxonField instanceof ValComboBoxFromQuery)
            {
                final ValComboBoxFromQuery parentCBX = (ValComboBoxFromQuery)taxonField;
                if (parentCBX != null)
                {
                    parentCBX.addListSelectionListener(new ListSelectionListener() {
                        public void valueChanged(ListSelectionEvent e)
                        {
                            if (e == null || !e.getValueIsAdjusting())
                            {
                                    taxonChanged(parentCBX);
                            }
                            }
                        });
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void taxonChanged(final ValComboBoxFromQuery taxonComboBox)
    {
        Object objInForm = formViewObj.getDataObj();
        //log.debug("form data object = " + objInForm);
        if (objInForm == null)
        {
            return;
        }
        
        Taxon formNode = ((Determination )objInForm).getTaxon();

        Taxon taxon = null;
        if (taxonComboBox.getValue() instanceof String)
        {
            // the data is still in the VIEW mode for some reason
            taxonComboBox.getValue();
            taxon = formNode.getParent();
        }
        else
        {
            taxon = (Taxon )taxonComboBox.getValue();
        }
        
        // set the tree def for the object being edited by using the parent node's tree def
        if (taxon != null)
        {
            if (!taxon.getIsAccepted())
            {
                PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, f:p:g, 5dlu", "7dlu, c:p, 2dlu, c:p, 10dlu"));
                String msg1 = String.format(UIRegistry.getResourceString("DeterminationBusRule.SynChoiceMsg1"), 
                        taxon.getFullName(), taxon.getAcceptedParent().getFullName());
                String msg2 = String.format(UIRegistry.getResourceString("DeterminationBusRule.SynChoiceMsg2"),
                        taxon.getAcceptedParent().getFullName());
                CellConstraints cc = new CellConstraints();
                pb.add(UIHelper.createLabel(msg1), cc.xy(2, 2));
                pb.add(UIHelper.createLabel(msg2), cc.xy(2, 4));
                String formTitle = UIRegistry.getResourceString("INFORMATION");
                CustomDialog cd = new CustomDialog((Frame)UIRegistry.getTopWindow(), formTitle, true, 
                        CustomDialog.OKCANCELHELP, pb.getPanel());
                cd.setModal(true);
                cd.setOkLabel(UIRegistry.getResourceString("YES"));
                cd.setCancelLabel(UIRegistry.getResourceString("NO"));
                UIHelper.centerAndShow(cd);
                if (cd.getBtnPressed() == CustomDialog.OK_BTN)
                {
                    taxon = taxon.getAcceptedParent();
                    taxonComboBox.setValue(taxon, taxon.getFullName());
                }
            }
            String activeTaxName;
            if (taxon.getIsAccepted())
            {
                activeTaxName = taxon.getFullName();
            }
            else
            {
                activeTaxName = taxon.getAcceptedParent().getFullName();
            }
            Component activeTax = formViewObj.getControlByName("activeTaxon");
            if (activeTax != null)
            {
                ((JTextField )activeTax).setText(activeTaxName);
            }
        }

    }

}
