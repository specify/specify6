package edu.ku.brc.specify.datamodel.busrules;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.AccessionAttachment;
import edu.ku.brc.specify.datamodel.AgentAttachment;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.CollectingEventAttachment;
import edu.ku.brc.specify.datamodel.CollectionObjectAttachment;
import edu.ku.brc.specify.datamodel.ConservDescriptionAttachment;
import edu.ku.brc.specify.datamodel.ConservEventAttachment;
import edu.ku.brc.specify.datamodel.LoanAttachment;
import edu.ku.brc.specify.datamodel.LocalityAttachment;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;
import edu.ku.brc.specify.datamodel.PermitAttachment;
import edu.ku.brc.specify.datamodel.PreparationAttachment;
import edu.ku.brc.specify.datamodel.RepositoryAgreementAttachment;
import edu.ku.brc.specify.datamodel.TaxonAttachment;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.thumbnails.Thumbnailer;

public class BaseObjectAttachmentBusRules extends BaseBusRules
{
    protected Logger log = Logger.getLogger(BaseObjectAttachmentBusRules.class);
    
    public BaseObjectAttachmentBusRules()
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
               TaxonAttachment.class
              );
    }
    
    public BaseObjectAttachmentBusRules(Class<? extends ObjectAttachmentIFace<?>>... dataClasses)
    {
        super(dataClasses);
    }

    @Override
    public boolean okToDelete(Object dataObj)
    {
        // it's always ok to delete an ObjectAttachmentIFace since they just serve as links
        // between Attachment records and other DB records.  Delete one of them is simply
        // breaking the link.
        return true;
    }

    @Override
    public void beforeDeleteCommit(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeDeleteCommit(dataObj, session);
    }

    @Override
    public void beforeSaveCommit(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSaveCommit(dataObj, session);
        
        ObjectAttachmentIFace<?> attachmentLink = (ObjectAttachmentIFace)dataObj;
        Attachment attach = attachmentLink.getAttachment();
        if (attach != null && attach.getId() == null)
        {
            log.info("Attachment link created to unsaved Attachment object.  Pushing Attachment file into storage system.");
            
            // Copy the attachment file to the file storage system
            Thumbnailer thumbnailGen = AttachmentUtils.getThumbnailer();
            AttachmentManagerIface attachmentMgr = AttachmentUtils.getAttachmentManager();
            File origFile = new File(attach.getOrigFilename());
            File thumbFile = null;
            
            try
            {
                thumbFile = File.createTempFile("sp6_thumb_", null);
                thumbFile.deleteOnExit();
                thumbnailGen.generateThumbnail(attach.getOrigFilename(), thumbFile.getAbsolutePath());
            }
            catch (IOException e)
            {
                // unable to create thumbnail
                thumbFile = null;
            }
            
            try
            {
                attachmentMgr.storeAttachmentFile(attach, origFile, thumbFile);
            }
            catch (IOException e)
            {
                // exception while saving copying attachments to storage system
                e.printStackTrace();
            }
        }
    }
}
