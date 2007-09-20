package edu.ku.brc.specify.datamodel.busrules;

import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

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
import edu.ku.brc.ui.UIRegistry;
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
               LoanAttachment.class,
               LocalityAttachment.class,
               PermitAttachment.class,
               PreparationAttachment.class,
               RepositoryAgreementAttachment.class,
               TaxonAttachment.class );
    }

    @Override
    public boolean okToDelete(Object dataObj)
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
        boolean okToDelete = attachBusRules.okToDelete(a);
        
        if (okToDelete)
        {
            boolean userApproved = askUserToApproveDelete(a);
            if (userApproved)
            {
                try
                {
                    AttachmentUtils.getAttachmentManager().deleteAttachmentFiles(a);
                }
                catch (IOException e)
                {
                    log.warn("Failed to delete attachment files from disk", e);
                }
            }
        }
        
        super.afterDeleteCommit(dataObj);
    }
    
    protected boolean askUserToApproveDelete(Attachment attachment)
    {
        JOptionPane pane = new JOptionPane("Delete the associated files from the attachment storage system?");
        pane.setOptionType(JOptionPane.YES_NO_OPTION);
        ImageIcon icon = new ImageIcon(AttachmentUtils.getAttachmentManager().getThumbnail(attachment).getAbsolutePath());
        pane.setIcon(icon);
        
        JDialog paneDialog = pane.createDialog(UIRegistry.getMostRecentWindow(), "Confirm Deletion");
        paneDialog.setVisible(true);
        Object choice = pane.getValue();
        
        return ((Integer)choice == JOptionPane.YES_OPTION);
    }
}
