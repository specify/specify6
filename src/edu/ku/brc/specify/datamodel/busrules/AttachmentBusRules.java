/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.Attachment;

/**
 * @author jstewart
 * @code_status Alpha
 */
public class AttachmentBusRules extends BaseBusRules
{
    public AttachmentBusRules()
    {
        super(Attachment.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToDelete(Object dataObj)
    {
        if (dataObj instanceof Attachment)
        {
            Attachment attach = (Attachment)dataObj;
            Integer id = attach.getId();
            if (id == null)
            {
                return true;
            }
            
            // just check the foreign keys
            boolean noPermits  = super.okToDelete("attachments", "PermitID",              id);
            boolean noAgents   = super.okToDelete("attachments", "AgentID",               id);
            boolean noLocales  = super.okToDelete("attachments", "LocalityID",            id);
            boolean noLoans    = super.okToDelete("attachments", "LoanID",                id);
            boolean noCollObjs = super.okToDelete("attachments", "CollectionObjectID",    id);
            boolean noCollEvts = super.okToDelete("attachments", "CollectingEventID",     id);
            boolean noAccs     = super.okToDelete("attachments", "AccessionID",           id);
            boolean noPreps    = super.okToDelete("attachments", "PreparationID",         id);
            boolean noTax      = super.okToDelete("attachments", "TaxonID",               id);
            boolean noRepos    = super.okToDelete("attachments", "RepositoryAgreementID", id);

            return noPermits &&
                   noAgents &&
                   noLocales &&
                   noLoans &&
                   noCollObjs &&
                   noCollEvts &&
                   noAccs &&
                   noPreps &&
                   noTax &&
                   noRepos;
        }
        
        return true;
    }

}
