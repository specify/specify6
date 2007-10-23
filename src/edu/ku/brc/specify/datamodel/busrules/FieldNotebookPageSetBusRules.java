package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.FieldNotebookPageSet;

public class FieldNotebookPageSetBusRules extends AttachmentOwnerBaseBusRules
{
    public FieldNotebookPageSetBusRules()
    {
        super(FieldNotebookPageSet.class);
    }

    @Override
    public boolean okToDelete(Object dataObj)
    {
        return true;
    }
}
