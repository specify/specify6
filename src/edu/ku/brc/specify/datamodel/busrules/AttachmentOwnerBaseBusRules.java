package edu.ku.brc.specify.datamodel.busrules;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttachmentOwnerIFace;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;

public abstract class AttachmentOwnerBaseBusRules extends BaseBusRules
{
    protected Logger log = Logger.getLogger(AttachmentOwnerBaseBusRules.class);
    
    public AttachmentOwnerBaseBusRules(Class<?>...classes)
    {
        super(classes);
    }
    
    @Override
    public boolean beforeDeleteCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        boolean retVal = super.beforeDeleteCommit(dataObj, session);
        if (retVal == false)
        {
            return retVal;
        }
        
        if (dataObj instanceof AttachmentOwnerIFace<?>)
        {
            AttachmentOwnerIFace<?> owner = (AttachmentOwnerIFace<?>)dataObj;
            
            // now check to see if the attachments referenced by this owner have no other
            // references in the DB
            
            AttachmentBusRules attachBusRules = new AttachmentBusRules();
            for (ObjectAttachmentIFace<?> attachRef: owner.getAttachmentReferences())
            {
                Attachment attach = attachRef.getAttachment();
                boolean canDelete = attachBusRules.okToDelete(attach);
                
                int option = JOptionPane.showOptionDialog(UIRegistry.getMostRecentWindow(), "Delete the attachment file from disk?", "Confirm file deletion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION);
                
                if (canDelete && option == JOptionPane.YES_OPTION)
                {
                    System.out.println("delete the file from disk: " + attach.getAttachmentLocation());
                    session.delete(attach);
                }
            }
        }
        
        return retVal;
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
    public boolean beforeSaveCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        boolean retVal = super.beforeSaveCommit(dataObj, session);
        if (retVal == false)
        {
            return retVal;
        }
        
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
        
        return true;
    }
}
