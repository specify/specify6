package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.ConservEvent;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 11, 2008
 *
 */
public class ConservEventBusRules extends AttachmentOwnerBaseBusRules
{
    /**
     * 
     */
    public ConservEventBusRules()
    {
        super(ConservEvent.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }
}
