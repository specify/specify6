package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.specify.datamodel.Deaccession;
import edu.ku.brc.specify.datamodel.Gift;

public class DeaccessionBusRules extends BaseBusRules {
    public DeaccessionBusRules() {
        super(DeaccessionBusRules.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(Object dataObj)
    {
        reasonList.clear();

        if (!(dataObj instanceof Deaccession))
        {
            return STATUS.Error;
        }

        STATUS duplicateNumberStatus = isCheckDuplicateNumberOK("deaccessionNumber",
                (FormDataObjIFace)dataObj,
                Deaccession.class,
                "deaccessionId");

        return duplicateNumberStatus;
    }

}
