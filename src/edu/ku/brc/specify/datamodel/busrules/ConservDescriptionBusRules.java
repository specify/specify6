package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.ConservDescription;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 11, 2008
 *
 */
public class ConservDescriptionBusRules extends AttachmentOwnerBaseBusRules
{
    public ConservDescriptionBusRules()
    {
        super(ConservDescription.class);
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
