package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.Locality;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 11, 2008
 *
 */
public class LocalityBusRules extends AttachmentOwnerBaseBusRules
{
    /**
     * 
     */
    public LocalityBusRules()
    {
        super(Locality.class);
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
