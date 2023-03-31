/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.ui.db.TextFieldWithInfo;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.validation.ValTextAreaBrief;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace.CreationMode;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
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
    private static final Logger  log   = Logger.getLogger(DeterminationBusRules.class);
    private static final String DT_ALREADY_DETERMINATION = "DT_ALREADY_DETERMINATION";
    
    protected Determination  determination         = null;

    protected KeyListener    nameChangeKL          = null;
    protected boolean        ignoreSelection       = false;
    protected boolean        checkedBlankUsageItem = false;
    protected boolean        isNewObject           = false;
    protected boolean        isBlockingChange      = false;
    
    protected ChangeListener chkbxCL               = null;
    protected ValCheckBox    isCurrentCheckbox     = null;

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#beforeFormFill()
     */
    @Override
    public void beforeFormFill()
    {
        determination = null;
    }

    boolean prefTaxIsFormatted = true;

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        isBlockingChange = false;
        determination    = null;
        
        if (formViewObj != null && formViewObj.getDataObj() instanceof Determination)
        {
            determination = (Determination)formViewObj.getDataObj();
            
            // if determination exists and is new (no key) then set current true if CO has no other dets
            Component currentComp = formViewObj.getControlByName("isCurrent");
            if (determination != null && currentComp != null)
            {
                if (isNewObject)
                {
                    // It should never be null, but, currently, it does happen.
                    // Also, now with Batch ReIdentify is will always be NULL
                    if (determination.getCollectionObject() != null) 
                    {
                		if (currentComp instanceof ValCheckBox)
                		{
                            if (formViewObj.isCreatingNewObject())
                            {
                    		    // Do this instead of setSelected because
                    		    // this activates the DataChangeListener
                    		    isBlockingChange = true;
                    			((ValCheckBox)currentComp).doClick();
                    			isBlockingChange = false;
                    			
                    			// Well, if it is already checked then we just checked it to the 'off' state,
                    			// so we need to re-check it so it is in the "checked state"
                    			// Note: As stated in the comment above the 'doClick' the easiest way to activate
                    			// all the change listeners is by simulating a mouse click.
                    			// Also keep in mind that the change listener is listening for ActionEvents for the
                    			// checkbox instead of ChangeEvents (ChangeEvents cause to many problems).
                    			if (!((ValCheckBox)currentComp).isSelected())
                    			{
                    			    ((ValCheckBox)currentComp).doClick(); 
                    			}
                			    
                    			Set<Determination> detSet = determination.getCollectionObject().getDeterminations();
                                for (Determination d : detSet)
                                {
                                    if (d != determination)
                                    {
                                        d.setIsCurrent(false);
                                    }
                                }
                			}
                            
                		} else
                		{
                			log.error("IsCurrent not set to true because form control is of unexpected type: " + currentComp.getClass().getName());
                		}
                    }
                } else
                {
                    ((ValCheckBox)currentComp).setValue(determination.getIsCurrent(), null);
                }
            }

            Component activeTax = formViewObj.getControlByName("preferredTaxon");
            if (activeTax != null) {
                if (activeTax instanceof JTextField || activeTax instanceof ValTextAreaBrief) {
                    String prefTaxTxt = "";
                    if (determination != null && determination.getPreferredTaxon() != null) {
                        String formatName = prefTaxIsFormatted ? DBTableIdMgr.getInstance().getInfoById(Taxon.getClassTableId()).getDataObjFormatter() : null;
                        if (formatName != null && !"".equals(formatName)) {
                            prefTaxTxt = DataObjFieldFormatMgr.getInstance().format(determination.getPreferredTaxon(), formatName);
                        } else {
                            log.warn("Taxon data obj formatter not found. Defaulting to full name.");
                            prefTaxTxt = determination.getPreferredTaxon().getFullName();
                        }
                    }
                    activeTax.setFocusable(false);
                    if (activeTax instanceof JTextField) {
                        ((JTextField)activeTax).setText(prefTaxTxt);
                    } else {
                        ((ValTextAreaBrief) activeTax).setText(prefTaxTxt);
                    }
                } else {
                    log.warn("PreferredTaxon control type not supported." + activeTax.getName());
                }
            }

            
            if (formViewObj.getAltView().getMode() != CreationMode.EDIT)
            {
                // when we're not in edit mode, we don't need to setup any listeners since the user can't change anything
                //log.debug("form is not in edit mode: no special listeners will be attached");
                return;
            }

            
            Component nameUsageComp = formViewObj.getControlByName("nameUsage");
            if (nameUsageComp instanceof ValComboBox)
            {
                // XXX this is probably not necessary anymore...
                if (!checkedBlankUsageItem)
                {
                    boolean fnd = false;
                    
                    if (nameUsageComp instanceof ValComboBox)
                    {
                        ValComboBox cbx = (ValComboBox)nameUsageComp;
                        if (cbx.getComboBox().getModel() instanceof PickListDBAdapterIFace)
                        {
                            PickListDBAdapterIFace items = (PickListDBAdapterIFace) cbx.getComboBox().getModel();
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
                        }
                    }
                    checkedBlankUsageItem = true;
                }
                nameUsageComp.setEnabled(true);
            }

            final Component altNameComp = formViewObj.getControlByName("alternateName");
            if (altNameComp != null && determination != null)
            {
                altNameComp.setEnabled(determination.getTaxon() == null);
            }
            
            if (currentComp != null && chkbxCL == null)
            {
                chkbxCL = new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        adjustIsCurrentCheckbox();
                    }
                };
            
                isCurrentCheckbox = (ValCheckBox)currentComp;
                isCurrentCheckbox.addChangeListener(chkbxCL);
            }
        }
        isNewObject = false;
    }
    
    /**
     * 
     */
    private void adjustIsCurrentCheckbox()
    {
        if (!isBlockingChange && formViewObj != null && isCurrentCheckbox != null)
        {
            determination = (Determination)formViewObj.getDataObj();
            if (determination != null && 
                isCurrentCheckbox.isSelected())
            {
                //log.debug("Current: "+determination.getId());
                if (determination.getCollectionObject() != null) //co can be null for batch-re-identify
                {
                	for (Determination d : determination.getCollectionObject().getDeterminations())
                	{
                		if (d != determination)
                		{
                			d.setIsCurrent(false);
                		}
                	}
                }
            }
        }
    }

    /**
     * @param kl
     * @param comp
     * 
     * Adds KeyListener to comp if it is not already a listener.
     * (This method is overkill given the current way listeners are set up.)
     */
    protected void addListenerIfNecessary(final KeyListener kl, final Component comp)
    {
        boolean fnd = false;
        for (KeyListener existingKl : comp.getKeyListeners())
        {
            if (existingKl == kl)
            {
                fnd = true;
                break;
            }
        }
        if (!fnd)
        {
            comp.addKeyListener(kl);
        }
    }
    
    /**
     * @param e
     * @param taxComp
     * @param altNameComp
     * 
     * Disables the taxon field when the alternateName field is non-empty.
     * Enables the taxon field when the alternateNameField is empty.
     */
    protected void nameChanged(KeyEvent e, final ValComboBoxFromQuery taxComp, final Component altNameComp)
    {
        if (e.getSource() != null)
        {
            if (e.getSource().equals(altNameComp))
            {
                taxComp.setEnabled(StringUtils.isBlank(((JTextField )altNameComp).getText()));
            }
            else if (e.getSource().equals(taxComp))
            {
            	taxonChanged(taxComp, altNameComp);
            }
        }        
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterCreateNewObj(java.lang.Object)
     */
    @Override
    public void afterCreateNewObj(Object newDataObj)
    {
        isNewObject = true;
        /*Determination    deter  = (Determination)newDataObj;
        CollectionObject colObj = deter.getCollectionObject();
        if (colObj != null && deter.isCurrentDet())
        {
            for (Determination det : colObj.getDeterminations())
            {
                if (det != deter && det.isCurrentDet())
                {
                    det.setIsCurrent(false);
                }
            }
        }*/
    }

    /**
     * Checks to make sure there is a single 'current' determination.
     * @param colObj the Collection Object
     * @param deter the determination for the CO
     * @return false if there is more than one determination set to current
     */
    protected boolean checkDeterminationStatus(final CollectionObject colObjArg, final Determination deter)
    {
        
    	//Kind of a workaround to fix bug#9327.
    	//The form system is returning the wrong colobj. 
    	//Using the determination's colobj record, which, I think, will always have a value, solves the immediate problem.
    	CollectionObject colObj = deter.getCollectionObject();
        if (colObj == null) {
        	colObj = colObjArg;
        }
        
    	if (deter.isCurrentDet())
        {
            for (Determination det : colObj.getDeterminations())
            {
                boolean isSameDet;
                if (det.getId() != null && deter.getId() != null)
                {
                    isSameDet = det.getId().equals(deter.getId());
                } else
                {
                    isSameDet = det == deter;
                }
                
                if (!isSameDet && det.isCurrentDet())
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
    public STATUS processBusinessRules(final Object parentDataObj,
                                       final Object dataObj,
                                       final boolean isExistingObject)
    {
        STATUS status = super.processBusinessRules(parentDataObj, dataObj, isExistingObject);
        
        if (status == STATUS.OK)
        {
            if (!checkDeterminationStatus((CollectionObject)parentDataObj, (Determination)dataObj))
            {
                reasonList.add(UIRegistry.getResourceString(DT_ALREADY_DETERMINATION));
                status = STATUS.Error;
            }
        }
        return status;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(final Object dataObj)
    {
        STATUS status = super.processBusinessRules(dataObj);
        if (status == STATUS.OK)
        {
            if (formViewObj != null && formViewObj.getMVParent() != null)
            {
                MultiView mv = formViewObj.getMVParent();
                if (!mv.isTopLevel())
                {
                    Object parentDataObj = mv.getMultiViewParent().getData();
                    if (!checkDeterminationStatus((CollectionObject)parentDataObj, (Determination)dataObj))
                    {
                        reasonList.add(UIRegistry.getResourceString(DT_ALREADY_DETERMINATION));
                        status = STATUS.Error;
                    }
                }
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
    public boolean afterSaveCommit(final Object dataObj, final DataProviderSessionIFace session)
    {
        determination = null;
        return super.afterSaveCommit(dataObj, session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        
        determination = null;
        chkbxCL       = null;
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
        super.initialize(viewableArg);
        
        if (formViewObj != null)
        {
            GetSetValueIFace taxonField = (GetSetValueIFace) formViewObj.getControlByName("taxon");
            if (taxonField instanceof ValComboBoxFromQuery)
            {
                final ValComboBoxFromQuery parentCBX = (ValComboBoxFromQuery) taxonField;
                final Component altNameComp = formViewObj.getControlByName("alternateName");

                if (nameChangeKL == null)
                {
                    nameChangeKL = new KeyAdapter()
                    {
                        @Override
                        public void keyTyped(final KeyEvent e)
                        {
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                // @Override
                                public void run()
                                {
                                    nameChanged(e, parentCBX, altNameComp);
                                }
                            });
                        }
                    };
                }
                
                if (parentCBX != null)
                {
                    parentCBX.addListSelectionListener(new ListSelectionListener()
                    {
                        public void valueChanged(ListSelectionEvent e)
                        {
                            if (e == null || !e.getValueIsAdjusting())
                            {
                                taxonChanged(parentCBX, altNameComp);
                            }
                        }
                    });
                    addListenerIfNecessary(nameChangeKL, parentCBX);
                }

                if (altNameComp != null)
                {
                    addListenerIfNecessary(nameChangeKL, altNameComp);
                }
            }
        }
    }

    /**
     * @param taxonComboBox 
     * 
     * Sets text for preferredTaxon control to the selected taxon or it's accepted parent.
     * 
     * If the selected taxon is not accepted, then a dialog pops up to confirm the choice
     * with an option to use the accepted parent instead.
     * 
     * Disables/Enables AlternateName field based on whether taxon is non-null/null.
     */
    protected void taxonChanged(final ValComboBoxFromQuery taxonComboBox, final Component altTaxName)
    {
        Object objInForm = formViewObj.getDataObj();
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
        
        String activeTaxName = null;
        
        // set the tree def for the object being edited by using the parent node's tree def
        if (taxon != null)
        {
        	if (!taxon.getIsAccepted() && AppPreferences.getRemote().getBoolean("Determination.PromptToReplaceSynonym", false))
            {
                PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, f:p:g, 5dlu", "7dlu, c:p, 5dlu, c:p, 10dlu"));
                String msg1 = String.format(UIRegistry.getResourceString("DeterminationBusRule.SynChoiceMsg1"), 
                        taxon.getFullName(), taxon.getAcceptedParent().getFullName());
                String msg2 = String.format(UIRegistry.getResourceString("DeterminationBusRule.SynChoiceMsg2"),
                        taxon.getFullName(), taxon.getAcceptedParent().getFullName());
                CellConstraints cc = new CellConstraints();
                pb.add(UIHelper.createLabel(msg1), cc.xy(2, 2));
                pb.add(UIHelper.createLabel(msg2), cc.xy(2, 4));
                String formTitle = UIRegistry.getResourceString("DeterminationBusRules.SYNONYM_INFORMATION");
                CustomDialog cd = CustomDialog.create(formTitle, true,
                        CustomDialog.OKCANCELHELP, pb.getPanel());
                cd.setModal(true);
                cd.setOkLabel(UIRegistry.getResourceString("DeterminationBusRules.Change"));
                cd.setCancelLabel(UIRegistry.getResourceString("DeterminationBusRules.Keep"));
                cd.setVisible(true);
                if (cd.getBtnPressed() == CustomDialog.OK_BTN)
                {
                    taxon = taxon.getAcceptedParent();
                    taxonComboBox.setValue(taxon, taxon.getFullName());
                }
            }
            if (taxon.getIsAccepted())
            {
                activeTaxName = taxon.getFullName();
            }
            else
            {
                activeTaxName = taxon.getAcceptedParent().getFullName();
            }
        }

        Component activeTax = formViewObj.getControlByName("preferredTaxon");
        if (activeTax != null)
        {
            ((JTextField )activeTax).setText(activeTaxName);
        }

        if (altTaxName != null)
        {
        	altTaxName.setEnabled(taxon == null);
        }
    }

}
