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
            boolean noAccession = super.okToDelete("accessionattachment",           "AttachmentID", id);
            boolean noAgent     = super.okToDelete("agentattachment",               "AttachmentID", id);
            boolean noCollEvt   = super.okToDelete("collectingeventattachment",     "AttachmentID", id);
            boolean noCollObj   = super.okToDelete("collectionobjectattachment",    "AttachmentID", id);
            boolean noConsDesc  = super.okToDelete("conservdescriptionattachment",  "AttachmentID", id);
            boolean noConsEvt   = super.okToDelete("conserveventattachment",        "AttachmentID", id);
            boolean noLoan      = super.okToDelete("loanattachment",                "AttachmentID", id);
            boolean noLoc       = super.okToDelete("localityattachment",            "AttachmentID", id);
            boolean noPermit    = super.okToDelete("permitattachment",              "AttachmentID", id);
            boolean noPrep      = super.okToDelete("preparationattachment",         "AttachmentID", id);
            boolean noRepoAg    = super.okToDelete("repositoryagreementattachment", "AttachmentID", id);
            boolean noTaxon     = super.okToDelete("taxonattachment",               "AttachmentID", id);

            return noAccession &&
                   noAgent &&
                   noCollEvt &&
                   noCollObj &&
                   noConsDesc &&
                   noConsEvt &&
                   noLoan &&
                   noLoc &&
                   noPermit &&
                   noPrep &&
                   noRepoAg &&
                   noTaxon;
        }
        
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeSaveCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        boolean retVal = super.beforeSaveCommit(dataObj, session);
        if (retVal == false)
        {
            return retVal;
        }
        
        if (!(dataObj instanceof Attachment))
        {
            return true;
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
        
        attachmentMgr.storeAttachmentFile(attachment, origFile, thumbFile);
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeDeleteCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeDeleteCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        return super.beforeDeleteCommit(dataObj, session);
        
        // TODO: delete the attachment from the storage system
    }
}
