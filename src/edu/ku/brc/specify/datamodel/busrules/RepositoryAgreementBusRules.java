package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.RepositoryAgreement;

public class RepositoryAgreementBusRules extends AttachmentOwnerBaseBusRules
{
    public RepositoryAgreementBusRules()
    {
        super(RepositoryAgreement.class);
    }

    @Override
    public boolean okToDelete(Object dataObj)
    {
        return true;
    }
}
