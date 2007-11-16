package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.CollectingEvent;

public class CollectingEventBusRules extends AttachmentOwnerBaseBusRules
{
    public CollectingEventBusRules()
    {
        super(CollectingEvent.class);
    }

    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }
}
