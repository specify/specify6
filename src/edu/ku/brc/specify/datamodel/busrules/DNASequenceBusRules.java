package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.DNASequence;

public class DNASequenceBusRules extends AttachmentOwnerBaseBusRules
{
    public DNASequenceBusRules()
    {
        super(DNASequence.class);
    }

    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }
}
