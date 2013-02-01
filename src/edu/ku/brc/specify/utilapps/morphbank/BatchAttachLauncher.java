/**
 * 
 */
package edu.ku.brc.specify.utilapps.morphbank;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.DatabaseLoginListener;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.FileStoreAttachmentManager;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * @author timo
 *
 */
public class BatchAttachLauncher implements DatabaseLoginListener
{
    protected static final Logger log = Logger.getLogger(BatchAttachLauncher.class);

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#cancelled()
     */
    public void cancelled()
    {
        System.exit(0);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#loggedIn(java.awt.Window, java.lang.String, java.lang.String)
     */
    public void loggedIn(Window window, String databaseName, String userName)
    {
        //System.out.println("Yup. You logged in.");

        //snatched from Specify.restartApp...
        AppPreferences.shutdownRemotePrefs();
        
        if (window != null)
        {
            window.setVisible(false);
        }
        
        //moved here because context needs to be set before loading prefs, we need to know the SpecifyUser
        AppContextMgr.CONTEXT_STATUS status = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).setContext(databaseName, userName, true, true, true, false);
       // AppContextMgr.getInstance().
        SpecifyAppPrefs.initialPrefs();
        
        if (status == AppContextMgr.CONTEXT_STATUS.OK)
        {
            if (AppContextMgr.getInstance().getClassObject(Discipline.class) == null)
            {
                return;
            }
            
            int disciplineeId = AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId();
            SchemaI18NService.getInstance().loadWithLocale(SpLocaleContainer.CORE_SCHEMA, disciplineeId, DBTableIdMgr.getInstance(), Locale.getDefault());
        } else if (status == AppContextMgr.CONTEXT_STATUS.Error)
        {
            if (AppContextMgr.getInstance().getClassObject(Collection.class) == null)
            {
                
                // TODO This is really bad because there is a Database Login with no Specify login
                JOptionPane.showMessageDialog(null, 
                                              getResourceString("Specify.LOGIN_USER_MISMATCH"),  //$NON-NLS-1$
                                              getResourceString("Specify.LOGIN_USER_MISMATCH_TITLE"),  //$NON-NLS-1$
                                              JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        
        }
        //...end specify.restartApp snatch
        
        boolean canOpen = true;
        if (AppContextMgr.isSecurityOn())
        {
            PermissionIFace permissions = SecurityMgr.getInstance().getPermission("Task.ExportMappingTask");
            canOpen = permissions.canView();
        }

        Thumbnailer thumb = new Thumbnailer();
        File thumbnailDir = null;
        try
        {
            thumbnailDir = XMLHelper.getConfigDir("thumbnail_generators.xml"); //$NON-NLS-1$
            thumb.registerThumbnailers(thumbnailDir);
        }
        catch (Exception e1)
        {
            throw new RuntimeException("Couldn't find thumbnailer xml ["+(thumbnailDir != null ? thumbnailDir.getAbsolutePath() : "")+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        thumb.setQuality(.5f);
        thumb.setMaxHeight(128);
        thumb.setMaxWidth(128);
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        AttachmentManagerIface attachMgr          = null;
        File                   attachmentLocation = null;
        final File             location           = UIRegistry.getAppDataSubDir("AttachmentStorage", true); //$NON-NLS-1$
            
        try
        {
            String path = localPrefs.get("attachment.path", null);
            attachmentLocation = path != null && !UIRegistry.isMobile() ? new File(path) : location;
            if (!AttachmentUtils.isAttachmentDirMounted(attachmentLocation))
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        UIRegistry.showLocalizedError("AttachmentUtils.LOC_BAD");
                    }
                });
                
            } else
            {
                attachMgr = new FileStoreAttachmentManager(attachmentLocation);
            }
            
            if (path == null)
            {
                localPrefs.put("attachment.path", location.getAbsolutePath());
            }
        }
        catch (IOException e1)
        {
            log.warn("Problems setting the FileStoreAttachmentManager at ["+location+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            // TODO RELEASE -  Instead of exiting we need to disable Attachments
            //throw new RuntimeException("Problems setting the FileStoreAttachmentManager at ["+location+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        AttachmentUtils.setAttachmentManager(attachMgr);
        AttachmentUtils.setThumbnailer(thumb);
        //ActionListener attachmentDisplayer = AttachmentUtils.getAttachmentDisplayer();
        if (canOpen)
        {
        	openBatchAttacher();
        }
        else
        {
            JOptionPane.showMessageDialog(null, getResourceString("SchemaExportLauncher.PERMISSION_DENIED"),
                        getResourceString("SchemaExportLauncher.PERMISSION_DENIED_TITLE"),
                        JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    
    /**
     * Creates BatchAttachFiles object and calls attachFiles()
     */
	protected void openBatchAttacher()
	{
		try
		{
			//Attach images
			//BatchAttachFiles baf = new BatchAttachFiles(CollectionObject.class, new BarCodeFileNameParser(),
			//		new File("/home/timo/TroyImages"));
			
			//For troy+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//Attach 'blank' attachments from list of image file names
			//BatchAttachFiles baf = new BatchAttachFiles(CollectionObject.class, new BarCodeFileNameParser("altCatalogNumber"),
			//		new File("/media/Terror/ConversionsAndFixes/Troy/Troy_directory_listing.txt"));
			//baf.attachFiles();
			//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			
			//for SD State ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			
//			SDStateFileNameParser p = new SDStateFileNameParser();
//			p.setPicType(SDStateFileNameParser.DorsalPic);
//			
//			BatchAttachFiles baf = new BatchAttachFiles(CollectionObject.class, p,
//					new File("/media/Terror/ConversionsAndFixes/sdstate/SpSub31OCT10/PictureFiles/SpecimenPics"));
//			baf.setErrLogName("/media/Terror/ConversionsAndFixes/sdstate/DorsalPicErrors.txt");
//			baf.attachFiles();
//			
//			p.setPicType(SDStateFileNameParser.SpecPic);
//			baf.setErrLogName("/media/Terror/ConversionsAndFixes/sdstate/SpecPicErrors.txt");
//			baf.attachFiles();
//			
//			
//			p.setPicType(SDStateFileNameParser.VentralPic);
//			baf.setErrLogName("/media/Terror/ConversionsAndFixes/sdstate/VertralPicErrors.txt");
//			baf.attachFiles();
			
			
			//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			
			//for Auburn +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//Attach 'blank' attachments from list of image file names
			BatchAttachFiles baf = new BatchAttachFiles(CollectionObject.class, new BarCodeFileNameParser("altCatalogNumber"),
					new File("/media/Terror/ConversionsAndFixes/auburn/ImageFileNames.txt"));
			baf.attachFiles();
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			
			System.exit(0);
		} catch (Exception ex)
		{
			System.out.println(ex.getMessage());
			System.exit(-1);
		}		
	}
}
