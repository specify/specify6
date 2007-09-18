package edu.ku.brc.specify.datamodel.busrules;

import java.io.IOException;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttachmentOwnerIFace;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;
import edu.ku.brc.util.AttachmentUtils;

public abstract class AttachmentOwnerBaseBusRules extends BaseBusRules
{
    protected Logger log = Logger.getLogger(AttachmentOwnerBaseBusRules.class);
    
    public AttachmentOwnerBaseBusRules(Class<?>...classes)
    {
        super(classes);
    }
    
    @Override
    public void beforeDeleteCommit(Object dataObj, DataProviderSessionIFace session)
    {
        // TODO Auto-generated method stub
        super.beforeDeleteCommit(dataObj, session);
    }

    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        if (dataObj instanceof AttachmentOwnerIFace<?>)
        {
            AttachmentOwnerIFace<?> owner = (AttachmentOwnerIFace<?>)dataObj;
            for (ObjectAttachmentIFace<?> oa: owner.getAttachmentReferences())
            {
                Attachment a = oa.getAttachment();
                if (a != null && a.getAttachmentLocation() == null)
                {
                    AttachmentUtils.getAttachmentManager().setStorageLocationIntoAttachment(a);
                }
            }
        }
    }

    @Override
    public void beforeSaveCommit(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSaveCommit(dataObj, session);
        
        // walk the set of ObjectAttachmentIFace objects, looking for any that have a new Attachment record
        // that needs to be saved into the Attachment storage system
        
        if (dataObj instanceof AttachmentOwnerIFace<?>)
        {
            AttachmentOwnerIFace<?> owner = (AttachmentOwnerIFace<?>)dataObj;
            for (ObjectAttachmentIFace<?> oa: owner.getAttachmentReferences())
            {
                Attachment a = oa.getAttachment();
                if (a != null && a.getAttachmentId() == null)
                {
                    // this is a new Attachment object
                    // we need to save it's file into the storage system
                    try
                    {
                        a.storeFile();
                    }
                    catch (IOException e)
                    {
                        log.error("Unable to store attached file", e);
                    }
                }
            }
        }
    }
}
