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

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.event.DocumentEvent;

import org.apache.commons.io.FilenameUtils;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.EditViewCompSwitcherPanel;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValBrowseBtnPanel;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.ui.DocumentAdaptor;
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
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null)
        {
            Component origComp  = formViewObj.getCompById("origFilename");
            Component titleComp = formViewObj.getCompById("title");
            
            if (origComp instanceof EditViewCompSwitcherPanel && titleComp instanceof ValTextField)
            {
                final EditViewCompSwitcherPanel evcsp = (EditViewCompSwitcherPanel)origComp;
                
                final ValTextField  titleTF = (ValTextField)titleComp;
                final ValTextField browserTF = ((ValBrowseBtnPanel)evcsp.getComp(true)).getValTextField();
                
                browserTF.getDocument().addDocumentListener(new DocumentAdaptor() {
                    @Override
                    protected void changed(DocumentEvent e)
                    {
                        String filePath = browserTF.getText();
                        if (titleTF.getText().isEmpty() && !filePath.isEmpty())
                        {
                            titleTF.setText(FilenameUtils.getBaseName(browserTF.getText()));
                        }
                    }
                    
                });
                
                browserTF.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e)
                    {
                        super.focusLost(e);
                        
                        String filePath = browserTF.getText();
                        if (titleTF.getText().isEmpty() && !filePath.isEmpty())
                        {
                            titleTF.setText(FilenameUtils.getBaseName(filePath));
                        }
                    }
                });
                
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        reasonList.clear();
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AttachmentBusRules.class, e);
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
