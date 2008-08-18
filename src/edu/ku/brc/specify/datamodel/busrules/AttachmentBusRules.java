/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.datamodel.busrules;

import java.io.File;
import java.io.IOException;

import edu.ku.brc.af.ui.forms.BaseBusRules;
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
    String[] tableNames = {"accessionattachment",          "agentattachment",             "collectingeventattachment",
                          "collectionobjectattachment",    "conservdescriptionattachment","conserveventattachment",
                          "dnasequenceattachment",         "fieldnotebookattachment",     "fieldnotebookpageattachment",
                          "fieldnotebookpagesetattachment","loanattachment",              "localityattachment",
                          "permitattachment",              "preparationattachment",       "repositoryagreementattachment",
                          "taxonattachment"};
    /**
     * 
     */
    public AttachmentBusRules()
    {
        super(Attachment.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        if (dataObj instanceof Attachment)
        {
            Attachment attach = (Attachment)dataObj;
            Integer id = attach.getId();
            if (id == null)
            {
                return true;
            }
            
            for (String tName : tableNames)
            {
                if (!super.okToDelete(tName, "AttachmentID", id))
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Returns a count of how many times this attachment is attached.
     * @param ids the id of the attachment(s)
     * @return the count of how many times it is attached
     */
    public Integer getTotalCountOfAttachments(final Integer...ids)
    {
        String[] tableColCombos = new String[tableNames.length*2];
        int inx = 0;
        for (String tName : tableNames)
        {
            tableColCombos[inx++] = tName;
            tableColCombos[inx++] = "AttachmentID";
        }
        
        return getTotalCount(tableColCombos, ids);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeSaveCommit(final Object dataObj, final DataProviderSessionIFace session) throws Exception
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
            thumbnailGen.generateThumbnail(attachment.getOrigFilename(), 
                                           thumbFile.getAbsolutePath(),
                                           false);
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
