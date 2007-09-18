package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.ConservEvent;

public class ConservEventBusRules extends AttachmentOwnerBaseBusRules
{
    public ConservEventBusRules()
    {
        super(ConservEvent.class);
    }

    @Override
    public boolean okToDelete(Object dataObj)
    {
        return true;
    }
}
