package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.FieldNotebook;

public class FieldNotebookBusRules extends AttachmentOwnerBaseBusRules
{
    public FieldNotebookBusRules()
    {
        super(FieldNotebook.class);
    }

    @Override
    public boolean okToDelete(Object dataObj)
    {
        return true;
    }
}
