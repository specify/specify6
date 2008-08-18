/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttachmentOwnerIFace;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;

/**
 * @author jstewart (original author)
 *
 * @code_status Alpha
 *
 * Jun 12, 2008
 *
 */
public abstract class AttachmentOwnerBaseBusRules extends BaseBusRules
{
    protected Logger log = Logger.getLogger(AttachmentOwnerBaseBusRules.class);
    
    /**
     * @param classes
     */
    public AttachmentOwnerBaseBusRules(Class<?>...classes)
    {
        super(classes);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#beforeDeleteCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
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
            
            Set<?>             hashSet        = new HashSet<Object>(owner.getAttachmentReferences());
            AttachmentBusRules attachBusRules = new AttachmentBusRules();
            for (Object attachRefObj : hashSet)
            {
                ObjectAttachmentIFace<?> attachRef = (ObjectAttachmentIFace<?>)attachRefObj;
                
                Attachment attach     = attachRef.getAttachment();
                Integer    totalCount = attachBusRules.getTotalCountOfAttachments(attach.getId());
                
                if (totalCount != null && totalCount == 1)
                {
                    // rods - We have decided that will automatically go ahead and delete
                    // the file on disk. We can make this a preference later if we wish.
                    if (false)
                    {
                        int option = JOptionPane.showOptionDialog(UIRegistry.getMostRecentWindow(), 
                                UIRegistry.getResourceString("AttachmentOwnerBaseBusRules.DEL_FROM_DISK"),  //$NON-NLS-1$
                                UIRegistry.getResourceString("AttachmentOwnerBaseBusRules.DEL_FROM_DISK_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION); // I18N //$NON-NLS-1$
                        
                        if (option == JOptionPane.YES_OPTION)
                        {
                            log.debug("delete the file from disk: " + attach.getAttachmentLocation()); //$NON-NLS-1$
                            session.delete(attach);
                            owner.getAttachmentReferences().remove(attach);
                        }
                    } else
                    {
                        session.delete(attach);
                        owner.getAttachmentReferences().remove(attach);
                    }
                }
            }
        }
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
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
                    a.setStoreFile(true);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#beforeSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
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
                if (a != null && a.isStoreFile())
                {
                    // this is a new Attachment object
                    // we need to store it's file into the storage system
                    try
                    {
                        a.storeFile();
                    }
                    catch (IOException e)
                    {
                        log.error("Unable to store attached file", e); //$NON-NLS-1$
                    }
                }
            }
        }
        
        return true;
    }
}
