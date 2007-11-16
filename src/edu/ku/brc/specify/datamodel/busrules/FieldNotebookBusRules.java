package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.FieldNotebook;

public class FieldNotebookBusRules extends AttachmentOwnerBaseBusRules
{
    public FieldNotebookBusRules()
    {
        super(FieldNotebook.class);
    }

    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }
}
