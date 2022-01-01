/* Copyright (C) 2022, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.*;
import edu.ku.brc.util.AttachmentUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @author jstewart (original author)
 *
 * @code_status Alpha
 *
 * Jun 12, 2008
 *
 */
public class AttachmentReferenceBaseBusRules extends BaseBusRules
{
    protected static Logger log = Logger.getLogger(AttachmentReferenceBaseBusRules.class);
    
    /**
     * 
     */
    public AttachmentReferenceBaseBusRules()
    {
        super( AccessionAttachment.class,
               AgentAttachment.class,
               BorrowAttachment.class,
               CollectingEventAttachment.class,
               CollectingTripAttachment.class,
               CollectionObjectAttachment.class,
               ConservDescriptionAttachment.class,
               ConservEventAttachment.class,
               DeaccessionAttachment.class,
               DNASequenceAttachment.class,
               DNASequencingRunAttachment.class,
               FieldNotebookAttachment.class,
               FieldNotebookPageAttachment.class,
               FieldNotebookPageSetAttachment.class,
               GiftAttachment.class,
               LoanAttachment.class,
               LocalityAttachment.class,
               PermitAttachment.class,
               PreparationAttachment.class,
               ReferenceWorkAttachment.class,
               RepositoryAgreementAttachment.class,
               TaxonAttachment.class );
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterDeleteCommit(java.lang.Object)
     */
    @Override
    public void afterDeleteCommit(Object dataObj)
    {
        reasonList.clear();
        
        ObjectAttachmentIFace<?> attRef = (ObjectAttachmentIFace<?>)dataObj;
        
        Attachment a = attRef.getAttachment();
        //System.out.println("afterSaveCommit(): " + a.getOrigFilename());
        
        AttachmentBusRules attachBusRules = new AttachmentBusRules();
        boolean okToDelete = a.getId() != null && attachBusRules.okToEnableDelete(a);
        
        if (okToDelete)
        {
            try
            {
                AttachmentUtils.getAttachmentManager().deleteAttachmentFiles(a);
                
                ////////////////////////////////////////////////////////////////////////////////////
                // Bug 8433 - Cascade Rules take careof the deletion so this no longer needs to be done.
                ////////////////////////////////////////////////////////////////////////////////////
                /*DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    
                    Attachment aFromDisk = session.load(Attachment.class, a.getId());
                    
                    session.beginTransaction();
                    session.delete(aFromDisk);
                    session.commit();
                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AttachmentReferenceBaseBusRules.class, e);
                    log.error("Failed to delete Attachment record from database", e);
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }*/
            }
            catch (IOException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AttachmentReferenceBaseBusRules.class, e);
                log.warn("Failed to delete attachment files from disk", e);
            }
        }
        
        super.afterDeleteCommit(dataObj);
    }
    
    /**
     * @param cls
     * @return
     */
    private BusinessRulesIFace getBusRuleForClass(final Object dObj)
    {
        BusinessRulesIFace busRule = null;
        DataModelObjBase   brObj   = (DataModelObjBase)dObj;
        DBTableInfo        tblInfo = DBTableIdMgr.getInstance().getInfoById(brObj.getTableId());
        if (tblInfo != null)
        {
            busRule = tblInfo.getBusinessRule();
        }
        return busRule;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeMerge(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
        
        ObjectAttachmentIFace<?> attRef = (ObjectAttachmentIFace<?>)dataObj;
        getBusRuleForClass(attRef.getAttachment()).beforeMerge(attRef.getAttachment(), session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        ObjectAttachmentIFace<?> attRef = (ObjectAttachmentIFace<?>)dataObj;
        getBusRuleForClass(attRef.getAttachment()).beforeSave(attRef.getAttachment(), session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeSaveCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        ObjectAttachmentIFace<?> attRef = (ObjectAttachmentIFace<?>)dataObj;
        return getBusRuleForClass(attRef.getAttachment()).afterSaveCommit(attRef.getAttachment(), session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#doesSearchObjectRequireNewParent()
     */
    public boolean doesSearchObjectRequireNewParent()
    {
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#processSearchObject(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object processSearchObject(final Object newParentDataObj, 
                                      final Object dataObjectFromSearch)
    {
        if (newParentDataObj instanceof ObjectAttachmentIFace<?>)
        {
            ObjectAttachmentIFace<?> objAtt = (ObjectAttachmentIFace<?>)newParentDataObj;
            objAtt.setAttachment((Attachment)dataObjectFromSearch);
            return objAtt;
        }
        
        return dataObjectFromSearch;
    }
}
