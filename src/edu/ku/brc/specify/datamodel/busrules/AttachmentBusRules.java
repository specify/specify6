/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.datamodel.busrules;

import java.io.File;
import java.io.IOException;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.thumbnails.Thumbnailer;

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
            boolean noPermits  = super.okToDelete("attachment", "PermitID",              id);
            boolean noAgents   = super.okToDelete("attachment", "AgentID",               id);
            boolean noLocales  = super.okToDelete("attachment", "LocalityID",            id);
            boolean noLoans    = super.okToDelete("attachment", "LoanID",                id);
            boolean noCollObjs = super.okToDelete("attachment", "CollectionObjectID",    id);
            boolean noCollEvts = super.okToDelete("attachment", "CollectingEventID",     id);
            boolean noAccs     = super.okToDelete("attachment", "AccessionID",           id);
            boolean noPreps    = super.okToDelete("attachment", "PreparationID",         id);
            boolean noTax      = super.okToDelete("attachment", "TaxonID",               id);
            boolean noRepos    = super.okToDelete("attachment", "RepositoryAgreementID", id);

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


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSaveCommit(Object dataObj, DataProviderSessionIFace session)
    {
        // TODO Auto-generated method stub
        super.beforeSaveCommit(dataObj, session);
        
        if (!(dataObj instanceof Attachment))
        {
            return;
        }
        
        Attachment attachment = (Attachment)dataObj;
        
        // Copy the attachment file to the file storage system
        Thumbnailer thumbnailGen = AttachmentUtils.getThumbnailer();
        AttachmentManagerIface attachmentMgr = AttachmentUtils.getAttachmentManager();
        File origFile = new File(attachment.getOrigFilename());
        File thumbFile = null;
        
        try
        {
            thumbFile = File.createTempFile("sp6_thumb_", null);
            thumbFile.deleteOnExit();
            thumbnailGen.generateThumbnail(attachment.getOrigFilename(), thumbFile.getAbsolutePath());
        }
        catch (IOException e)
        {
            // unable to create thumbnail
            thumbFile = null;
        }
        
        try
        {
            attachmentMgr.storeAttachmentFile(attachment, origFile, thumbFile);
        }
        catch (IOException e)
        {
            // exception while saving copying attachments to storage system
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeDeleteCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeDeleteCommit(Object dataObj, DataProviderSessionIFace session)
    {
        // TODO Auto-generated method stub
        super.beforeDeleteCommit(dataObj, session);
        
        // TODO: delete the attachment from the storage system
    }
}
