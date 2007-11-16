package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.Locality;

public class LocalityBusRules extends AttachmentOwnerBaseBusRules
{
    public LocalityBusRules()
    {
        super(Locality.class);
    }
    
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }

}
