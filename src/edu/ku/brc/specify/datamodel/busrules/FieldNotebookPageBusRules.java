package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.FieldNotebookPage;

public class FieldNotebookPageBusRules extends AttachmentOwnerBaseBusRules
{
    public FieldNotebookPageBusRules()
    {
        super(FieldNotebookPage.class);
    }

    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }
}
