package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.Preparation;

public class PreparationBusRules extends AttachmentOwnerBaseBusRules
{
    public PreparationBusRules()
    {
        super(Preparation.class);
    }

    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }
}
