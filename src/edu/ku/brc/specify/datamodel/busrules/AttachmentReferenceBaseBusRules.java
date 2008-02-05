package edu.ku.brc.specify.datamodel.busrules;

import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.AccessionAttachment;
import edu.ku.brc.specify.datamodel.AgentAttachment;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.CollectingEventAttachment;
import edu.ku.brc.specify.datamodel.CollectionObjectAttachment;
import edu.ku.brc.specify.datamodel.ConservDescriptionAttachment;
import edu.ku.brc.specify.datamodel.ConservEventAttachment;
import edu.ku.brc.specify.datamodel.DNASequenceAttachment;
import edu.ku.brc.specify.datamodel.FieldNotebookAttachment;
import edu.ku.brc.specify.datamodel.FieldNotebookPageAttachment;
import edu.ku.brc.specify.datamodel.FieldNotebookPageSetAttachment;
import edu.ku.brc.specify.datamodel.LoanAttachment;
import edu.ku.brc.specify.datamodel.LocalityAttachment;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;
import edu.ku.brc.specify.datamodel.PermitAttachment;
import edu.ku.brc.specify.datamodel.PreparationAttachment;
import edu.ku.brc.specify.datamodel.RepositoryAgreementAttachment;
import edu.ku.brc.specify.datamodel.TaxonAttachment;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.BaseBusRules;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;

public class AttachmentReferenceBaseBusRules extends BaseBusRules
{
    protected static Logger log = Logger.getLogger(AttachmentReferenceBaseBusRules.class);
    
    public AttachmentReferenceBaseBusRules()
    {
        super( AccessionAttachment.class,
               AgentAttachment.class,
               CollectingEventAttachment.class,
               CollectionObjectAttachment.class,
               ConservDescriptionAttachment.class,
               ConservEventAttachment.class,
               DNASequenceAttachment.class,
               FieldNotebookAttachment.class,
               FieldNotebookPageAttachment.class,
               FieldNotebookPageSetAttachment.class,
               LoanAttachment.class,
               LocalityAttachment.class,
               PermitAttachment.class,
               PreparationAttachment.class,
               RepositoryAgreementAttachment.class,
               TaxonAttachment.class );
    }

    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }

    @Override
    public void afterDeleteCommit(Object dataObj)
    {
        ObjectAttachmentIFace<?> attRef = (ObjectAttachmentIFace<?>)dataObj;
        
        Attachment a = attRef.getAttachment();
        System.out.println("afterSaveCommit(): " + a.getOrigFilename());
        
        AttachmentBusRules attachBusRules = new AttachmentBusRules();
        boolean okToDelete = attachBusRules.okToEnableDelete(a);
        
        if (okToDelete)
        {
            boolean userApproved = askUserToApproveDelete(a);
            if (userApproved)
            {
                try
                {
                    AttachmentUtils.getAttachmentManager().deleteAttachmentFiles(a);
                    
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    Attachment aFromDisk = session.load(Attachment.class, a.getId());
                    try
                    {
                        session.beginTransaction();
                        session.delete(aFromDisk);
                        session.commit();
                    }
                    catch (Exception e)
                    {
                        log.error("Failed to delete Attachment record from database", e);
                    }
                }
                catch (IOException e)
                {
                    log.warn("Failed to delete attachment files from disk", e);
                }
            }
        }
        
        super.afterDeleteCommit(dataObj);
    }
    
    /**
     * @param attachment
     * @return
     */
    protected boolean askUserToApproveDelete(Attachment attachment)
    {
        JOptionPane pane = new JOptionPane("Delete the associated files from the attachment storage system?  " + attachment.getOrigFilename()); // I18N
        pane.setOptionType(JOptionPane.YES_NO_OPTION);
        AttachmentManagerIface attachMgr = AttachmentUtils.getAttachmentManager();
        File thumbnail = attachMgr.getThumbnail(attachment);
        
        if (thumbnail != null)
        {
            ImageIcon icon = new ImageIcon(thumbnail.getAbsolutePath());
            pane.setIcon(icon);
        }
        
        JDialog paneDialog = pane.createDialog(UIRegistry.getMostRecentWindow(), "Confirm Deletion");
        paneDialog.setVisible(true);
        Object choice = pane.getValue();
        
        return ((Integer)choice == JOptionPane.YES_OPTION);
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
        if (newParentDataObj instanceof ObjectAttachmentIFace)
        {
            ObjectAttachmentIFace<?> objAtt = (ObjectAttachmentIFace<?>)newParentDataObj;
            objAtt.setAttachment((Attachment)dataObjectFromSearch);
            return objAtt;
        }
        
        return dataObjectFromSearch;
    }
}
