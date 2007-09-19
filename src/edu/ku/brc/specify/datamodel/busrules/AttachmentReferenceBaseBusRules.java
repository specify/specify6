package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.AccessionAttachment;
import edu.ku.brc.specify.datamodel.AgentAttachment;
import edu.ku.brc.specify.datamodel.CollectingEventAttachment;
import edu.ku.brc.specify.datamodel.CollectionObjectAttachment;
import edu.ku.brc.specify.datamodel.ConservDescriptionAttachment;
import edu.ku.brc.specify.datamodel.ConservEventAttachment;
import edu.ku.brc.specify.datamodel.LoanAttachment;
import edu.ku.brc.specify.datamodel.LocalityAttachment;
import edu.ku.brc.specify.datamodel.PermitAttachment;
import edu.ku.brc.specify.datamodel.PreparationAttachment;
import edu.ku.brc.specify.datamodel.RepositoryAgreementAttachment;
import edu.ku.brc.specify.datamodel.TaxonAttachment;

public class AttachmentReferenceBaseBusRules extends BaseBusRules
{

    public AttachmentReferenceBaseBusRules()
    {
        super( AccessionAttachment.class,
               AgentAttachment.class,
               CollectingEventAttachment.class,
               CollectionObjectAttachment.class,
               ConservDescriptionAttachment.class,
               ConservEventAttachment.class,
               LoanAttachment.class,
               LocalityAttachment.class,
               PermitAttachment.class,
               PreparationAttachment.class,
               RepositoryAgreementAttachment.class,
               TaxonAttachment.class );
    }

    @Override
    public boolean okToDelete(Object dataObj)
    {
        return true;
    }
}
