/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.datamodel.busrules;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.StaleObjectException;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttachmentOwnerIFace;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
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
    
    
    private HashMap<Class<?>, HashSet<String>> attachHashSet = new HashMap<Class<?>, HashSet<String>>();
    
    /**
     * @param classes
     */
    public AttachmentOwnerBaseBusRules(Class<?>...classes)
    {
        super(classes);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beginSecondaryRuleProcessing()
     */
    @Override
    public void startProcessingBeforeAfterRules()
    {
        attachHashSet.clear();
        attachOwners.clear();
    }
    
    protected void clearOwners()
    {
        attachOwners.clear();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#endSecondaryRuleProcessing()
     */
    @Override
    public void endProcessingBeforeAfterRules()
    {
    }

    /**
     * @param dObj
     * @return
     */
    private HashSet<String> getHashSetForClass(final Object dObj)
    {
        Class<?>        cls  = dObj.getClass();
        HashSet<String> hSet = attachHashSet.get(cls);
        if (hSet == null)
        {
            hSet = new HashSet<String>();
            attachHashSet.put(cls, hSet);
        }
        return hSet;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
        addExtraObjectForProcessing(dataObj);
        
        for (AttachmentOwnerIFace<?> aOwner : attachOwners)
        {
            /*DataModelObjBase dob = (DataModelObjBase)aOwner;
            if (dob.getId() != null)
            {
                try
                {
                    aOwner = (AttachmentOwnerIFace)session.merge(dob);
                } catch (StaleObjectException ex)
                {
                    
                }
            }*/
            
            for (ObjectAttachmentIFace<?> objAtt : aOwner.getAttachmentReferences())
            {
                Attachment a = objAtt.getAttachment();
                if (a != null && a.getAttachmentLocation() == null)
                {
                    AttachmentUtils.getAttachmentManager().setStorageLocationIntoAttachment(a);
                    getHashSetForClass(dataObj).add(a.getAttachmentLocation());
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(final Object dataObj, final DataProviderSessionIFace session)
    {
        clearOwners();
        
        addExtraObjectForProcessing(dataObj);
        
        super.beforeSave(dataObj, session);
        
        for (AttachmentOwnerIFace<?> aOwner : attachOwners)
        {
            for (ObjectAttachmentIFace<?> objAtt : aOwner.getAttachmentReferences())
            {
                Attachment a = objAtt.getAttachment();
                
                if (a != null && a.getAttachmentLocation() != null)
                {
                    if (getHashSetForClass(dataObj).contains(a.getAttachmentLocation()))
                    {
                        a.setStoreFile(true);
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#beforeSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeSaveCommit(final Object dataObj, final DataProviderSessionIFace session) throws Exception
    {
        boolean retVal = super.beforeSaveCommit(dataObj, session);
        if (retVal == false)
        {
            return retVal;
        }
        
        for (AttachmentOwnerIFace<?> aOwner : attachOwners)
        {
            for (ObjectAttachmentIFace<?> oa: aOwner.getAttachmentReferences())
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
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AttachmentOwnerBaseBusRules.class, e);
                        log.error("Unable to store attached file", e); //$NON-NLS-1$
                    }
                }
            }
        }
        return true;
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
                    /*if (false)
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
                    } else*/
                    {
                        session.delete(attach);
                        owner.getAttachmentReferences().remove(attach);
                    }
                }
            }
        }
        
        return retVal;
    }
    

}
