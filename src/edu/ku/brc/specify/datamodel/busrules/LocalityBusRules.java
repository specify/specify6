package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.validation.ValComboBoxFromQuery;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 11, 2008
 *
 */
public class LocalityBusRules extends AttachmentOwnerBaseBusRules implements ListSelectionListener
{
    protected ValComboBoxFromQuery geographyCBX = null;
    /**
     * 
     */
    public LocalityBusRules()
    {
        super(Locality.class);
        
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
            Component comp = formViewObj.getCompById("4");
            if (comp != null && comp instanceof ValComboBoxFromQuery)
            {
                geographyCBX = (ValComboBoxFromQuery)comp;
                geographyCBX.addListSelectionListener(this);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        
        if (geographyCBX != null)
        {
            geographyCBX.removeListSelectionListener(this);
            geographyCBX = null;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(Object dataObj)
    {
        // TODO Auto-generated method stub
        return super.processBusinessRules(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object, edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void afterFillForm(Object dataObj, Viewable viewable)
    {
        super.afterFillForm(dataObj, viewable);
        
        if (viewable instanceof FormViewObj)
        {
            Locality locality = (Locality)dataObj;
            if (locality  != null)
            {
                boolean   enable   = locality.getGeography() != null && StringUtils.isNotEmpty(locality.getLocalityName());
                Component bgmComp  = formViewObj.getCompById("23");
                if (bgmComp != null)
                {
                    bgmComp.setEnabled(enable);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e)
    {
        if (formViewObj != null)
        {
            /*Geography geography = (Geography)geographyCBX.getValue();
            Locality  locality  = (Locality)formViewObj.getDataObj();
            if (locality  != null)
            {
                locality.setGeography(geography);
            }*/
        }
        
    }
}
