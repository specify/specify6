/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttachmentImageAttribute;
import edu.ku.brc.specify.datamodel.Borrow;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.ConservDescription;
import edu.ku.brc.specify.datamodel.ConservEvent;
import edu.ku.brc.specify.datamodel.DNASequence;
import edu.ku.brc.specify.datamodel.DNASequencingRun;
import edu.ku.brc.specify.datamodel.FieldNotebook;
import edu.ku.brc.specify.datamodel.FieldNotebookPage;
import edu.ku.brc.specify.datamodel.FieldNotebookPageSet;
import edu.ku.brc.specify.datamodel.Gift;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.specify.datamodel.RepositoryAgreement;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.tasks.subpane.images.FullImagePane;
import edu.ku.brc.specify.tasks.subpane.images.ImageDataItem;
import edu.ku.brc.specify.tasks.subpane.images.ImageLoader;
import edu.ku.brc.specify.tasks.subpane.images.ImageLoaderListener;
import edu.ku.brc.specify.tasks.subpane.images.ImagesPane;
import edu.ku.brc.specify.tasks.subpane.images.ImagesPane.SearchType;
import edu.ku.brc.specify.utilapps.morphbank.BatchAttachFiles;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.ImageLoaderExector;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jan 25, 2012
 *
 */
public class AttachmentsTask extends BaseTask implements ImageLoaderListener
{
    private static final String  ON_TASKBAR             = "AttachmentsTask.OnTaskbar";
    private static final String  ATTACHMENTS_SEARCH     = "ATTACHMENTS.SEARCH";
    public static final String  ATTACHMENTS             = "ATTACHMENTS";
    public static final String  EXPORT_CMD              = "ATTACHMENTS.EXPORT_CMD";
    
    
    // Data Members
    protected ImagesPane              imagesPane       = null;
    protected NavBox                  actionNavBox     = null;
    protected NavBox                  viewsNavBox      = null;
    protected NavBox                  adminNavBox       = null;

    protected Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    protected ToolBarDropDownBtn      toolBarBtn       = null;
    
    protected AtomicBoolean           isDoingImageSearching = new AtomicBoolean(false);
    private   File                    exportFile = null;
    
    /**
     * 
     */
    public AttachmentsTask()
    {
        super(ATTACHMENTS, getResourceString(ATTACHMENTS));
        this.iconName = "AttachmentPrefs";
        CommandDispatcher.register(PreferencesDlg.PREFERENCES, this);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    @Override
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            extendedNavBoxes.clear();
            
            // Actions
            RolloverCommand showAllBtn      = (RolloverCommand)addNavBoxItem(actionNavBox,  getResourceString("ATTCH_SHOWALL_ATT"),   this.iconName, null, null);
            RolloverCommand showAllImgsBtn  = (RolloverCommand)addNavBoxItem(actionNavBox,  getResourceString("ATTCH_SHOWALL_IMGS"),   this.iconName, null, null);
            RolloverCommand uploadImagesBtn = (RolloverCommand)addNavBoxItem(actionNavBox, getResourceString("ATTCH_IMPORT_IMGS"),    this.iconName, null, null);
            RolloverCommand uploadIndexBtn  = (RolloverCommand)addNavBoxItem(actionNavBox, getResourceString("ATTCH_IMPORT_IMGSMAP"), this.iconName, null, null);
            
            //RolloverCommand uploadOCRBtn    = (RolloverCommand)addNavBoxItem(actionNavBox, "Import OCR Data", "image", null, null);
            //addNavBoxItem(actionNavBox, "Import OCR Data", "network_node_del", null, null);
            //RolloverCommand showStatusBtn = (RolloverCommand)addNavBoxItem(actionNavBox, "Status",   "InfoIcon", null, null);
            
            //DBTableInfo coTI  = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
            //DBTableInfo geoTI = DBTableIdMgr.getInstance().getInfoById(Geography.getClassTableId());
            //DBTableInfo taxTI = DBTableIdMgr.getInstance().getInfoById(Taxon.getClassTableId());

            // Query Panels
            //RolloverCommand showAllBtn = (RolloverCommand)addNavBoxItem(viewsNavBox, "Show All Images", "image", null, null);
            //RolloverCommand coBtn      = (RolloverCommand)addNavBoxItem(viewsNavBox, coTI.getTitle(), "image", null, null);
            //coBtn.setIcon(coTI.getIcon(IconManager.STD_ICON_SIZE));
            
            //RolloverCommand configOutgoingBtn = (RolloverCommand)addNavBoxItem(viewsNavBox, "Config Outgoing", "notify", null, null);
            //RolloverCommand notiLogBtn        = (RolloverCommand)addNavBoxItem(viewsNavBox, "Review Notifications", "logfile", null, null);
            
            // Admin
            /* RolloverCommand connectBtn    = (RolloverCommand)addNavBoxItem(adminNavBox, "Connect", "connect_net", null, null);
            RolloverCommand disconnectBtn = (RolloverCommand)addNavBoxItem(adminNavBox, "Disconnect", "disconnect_net", null, null);
            RolloverCommand configNetBtn  = (RolloverCommand)addNavBoxItem(adminNavBox, "Config Connection",   "SystemSetup", null, null);

            connectBtn.setEnabled(false);
            */
            
            navBoxes.add(actionNavBox);
            //navBoxes.add(viewsNavBox);
            //navBoxes.add(adminNavBox);

            int[] attachmentTableIDs = {
                    AttachmentImageAttribute.getClassTableId(),
                    Attachment.getClassTableId(),
                    Accession.getClassTableId(),
                    Agent.getClassTableId(),
                    Borrow.getClassTableId(),
                    CollectingEvent.getClassTableId(),
                    CollectionObject.getClassTableId(),
                    ConservDescription.getClassTableId(),
                    ConservEvent.getClassTableId(),
                    DNASequence.getClassTableId(),
                    DNASequencingRun.getClassTableId(),
                    FieldNotebook.getClassTableId(),
                    FieldNotebookPage.getClassTableId(),
                    FieldNotebookPageSet.getClassTableId(),
                    Gift.getClassTableId(),
                    Loan.getClassTableId(),
                    Locality.getClassTableId(),
                    Permit.getClassTableId(),
                    Preparation.getClassTableId(),
                    ReferenceWork.getClassTableId(),
                    RepositoryAgreement.getClassTableId(),
                    Taxon.getClassTableId()};
            for (int tblId : attachmentTableIDs)
            {
                DBTableInfo   tblInfo   = DBTableIdMgr.getInstance().getInfoById(tblId);
                CommandAction cmdAction = new CommandAction(ATTACHMENTS, ATTACHMENTS_SEARCH);
                cmdAction.setProperty("id",    Integer.toString(tblId));
                
                cmdAction.setProperty(NavBoxAction.ORGINATING_TASK, this);
                String serviceName = String.format("Image Search %s", tblInfo.getTitle());
                String tooltip = UIRegistry.getResourceString(ATTACHMENTS);
                if (tblId != Attachment.getClassTableId()) 
                {
                	tooltip = tblInfo.getTitle() + " " + tooltip;
                }
                ContextMgr.registerService(10, serviceName, tblId, cmdAction, this, "AttachmentPrefs", tooltip , false); // the Name gets Hashed
            }

            
            uploadImagesBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    BatchAttachFiles.uploadAttachmentsByFileName();
                }
            });
            
            uploadIndexBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    BatchAttachFiles.attachFileFromIndexFile();
                }
            });
            
            /*uploadOCRBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    uploadOCRData(false);
                }
            });*/
            
//            coBtn.addActionListener(new ActionListener()
//            {
//                @Override
//                public void actionPerformed(ActionEvent e)
//                {
//                    if (starterPane != null) removeSubPaneFromMgr(starterPane);
//                    starterPane = imagesPane = new ImagesPane(getResourceString(ATTACHMENTS), ImagesTask.this, false); 
//                    addSubPaneToMgr(starterPane);
//                }
//            });
            
            showAllBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (starterPane != null) 
                    {
                        removeSubPaneFromMgr(starterPane);
                    }
                    starterPane = imagesPane = new ImagesPane(getResourceString(ATTACHMENTS), AttachmentsTask.this, SearchType.AllAttachments); 
                    addSubPaneToMgr(starterPane);
                }
            });
            
            showAllImgsBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (starterPane != null) 
                    {
                        removeSubPaneFromMgr(starterPane);
                    }
                    starterPane = imagesPane = new ImagesPane(getResourceString(ATTACHMENTS), AttachmentsTask.this, SearchType.AllImages); 
                    addSubPaneToMgr(starterPane);
                }
            });
        }
        isShowDefault = true;
    }
    
    /**
     * @param rs
     */
    private void searchForAttachments(final RecordSetIFace rs)
    {
        if (starterPane != null)
        {
            removeSubPaneFromMgr(starterPane);
        }
        
        starterPane = null;
        imagesPane  = null;
        
        //isDoingImageSearching.set(false);
        
        if (!isDoingImageSearching.get())
        {
            isDoingImageSearching.set(true);
            new ImagesPane(getResourceString(ATTACHMENTS), this, rs);
        }
    }
    
    /**
     * @param imgPane
     */
    public void attachmentSearchDone(final ImagesPane imgPane)
    {
        if (isDoingImageSearching.get())
        {
            isDoingImageSearching.set(false);
            if (imgPane != null)
            {
                starterPane = imagesPane = imgPane; 
                addSubPaneToMgr(starterPane);
            }
        }
    }
    
    /**
     * 
     */
    private void exportAttachment(final CommandAction cmdAction)
    {
        exportFile = null;
        Object data = cmdAction.getData();
        if (data instanceof ImageDataItem)
        {
            ImageDataItem idi = (ImageDataItem)data;
            System.out.println(idi.getImgName());
            
            String origFilePath = BasicSQLUtils.querySingleObj("SELECT OrigFilename FROM attachment WHERE AttachmentID = " + idi.getAttachmentId());
            if (StringUtils.isEmpty(origFilePath))
            {
                origFilePath = FilenameUtils.getName(origFilePath);
            } else
            {
                origFilePath = idi.getTitle();
            }
            String       usrHome = System.getProperty("user.home");
            JFileChooser dlg     = new JFileChooser(usrHome);
            dlg.setSelectedFile(new File(origFilePath));
            int rv = dlg.showSaveDialog((Frame)UIRegistry.getTopWindow());
            if (rv == JFileChooser.APPROVE_OPTION)
            {
                File file = dlg.getSelectedFile();
                if (file != null)
                {
                    String fullPath = file.getAbsolutePath();
                    String oldExt   = FilenameUtils.getExtension(origFilePath);
                    String newExt   = FilenameUtils.getExtension(fullPath);
                    if (StringUtils.isEmpty(newExt) && StringUtils.isNotEmpty(oldExt))
                    {
                        fullPath += "." + oldExt;
                        exportFile = new File(fullPath);
                    } else
                    {
                        exportFile = file;
                    }
                    boolean isOK = true;
                    if (exportFile.exists())
                    {
                        isOK = UIRegistry.displayConfirmLocalized("ATTCH.FILE_EXISTS", "ATTCH.REPLACE_MSG", "ATTCH.REPLACE", "CANCEL", JOptionPane.QUESTION_MESSAGE);
                    }
                    if (isOK)
                    {
                        ImageLoader loader = new ImageLoader(idi.getImgName(), idi.getMimeType(), true, -1, this);
                        ImageLoaderExector.getInstance().loadImage(loader);
                    }
                }
                System.out.println(file.toPath());
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.images.ImageLoaderListener#imagedLoaded(java.lang.String, java.lang.String, boolean, int, boolean, javax.swing.ImageIcon, java.io.File)
     */
    @Override
    public void imageLoaded(final String    imageName,
                             final String    mimeType,
                             final boolean   doLoadFullImage,
                             final int       scale,
                             final boolean   isError,
                             final ImageIcon imgIcon,
                             final File      localFile)
    {
        if (!isError && localFile.exists())
        {
            try
            {
                FileUtils.copyFile(localFile, exportFile);
                UIRegistry.writeTimedSimpleGlassPaneMsg("File exported");
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.images.ImageLoaderListener#imageStopped(java.lang.String)
     */
    @Override
    public void imageStopped(final String imageName, final boolean doLoadFullImage)
    {
    }

    /**
     * @param cmdAction
     */
    private void displayAttachment(final CommandAction cmdAction)
    {
        Object dataObj = cmdAction.getData();
        if (!(dataObj instanceof Attachment) && !(dataObj instanceof ObjectAttachmentIFace<?>))
        {
            throw new IllegalArgumentException("Passed object must be an Attachment or ObjectAttachmentIFace");
        }
        Attachment attachment = (dataObj instanceof Attachment) ? (Attachment)dataObj : ((ObjectAttachmentIFace<?>)dataObj).getAttachment();

        File original = AttachmentUtils.getAttachmentFile(dataObj);
        if (original != null)
        {
            if (AttachmentUtils.isFileDisplayable(original.getName()))
            {
                // Here we are 
                FullImagePane pane = new FullImagePane(attachment.getTitle(), this, original);
                SubPaneMgr.getInstance().addPane(pane);
                return;
            }
        } else {
            UIRegistry.showLocalizedError("noattachment");
            return;
        }
        
        try
        {
            AttachmentUtils.openFile(original);
        } catch (Exception ex)
        {
            UIRegistry.showLocalizedMsg("AttachmentUtils.NEV_TITLE", "AttachmentUtils.NEV_MSG");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#preInitialize()
     */
    @Override
    public void preInitialize()
    {
        CommandDispatcher.register(ATTACHMENTS, this);

        // Create and add the Actions NavBox first so it is at the top at the top
        actionNavBox = new NavBox(getResourceString("Actions"));
        
        //viewsNavBox = new NavBox("Image Search");
        //adminNavBox  = new NavBox("Admin");
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return starterPane = StartUpTask.createFullImageSplashPanel(title, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneRemoved(edu.ku.brc.af.core.SubPaneIFace)
     */
    @Override
    public void subPaneRemoved(final SubPaneIFace subPane)
    {
        super.subPaneRemoved(subPane);
        
        if (subPane == starterPane)
        {
            starterPane = imagesPane = null;
        }
    }

    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getNavBoxes()
     */
    @Override
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);
//        
//        RecordSetTask     rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);
//        List<NavBoxIFace> nbs    = rsTask.getNavBoxes();
//        if (nbs != null)
//        {
//            extendedNavBoxes.addAll(nbs);
//        }

        return extendedNavBoxes;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        String label    = getResourceString(ATTACHMENTS);
        String hint     = getResourceString(ATTACHMENTS);
        toolBarBtn      = createToolbarButton(label, iconName, hint);
        
        toolbarItems = new Vector<ToolBarItemDesc>();
        if (AppPreferences.getRemote().getBoolean(ON_TASKBAR, true))
        {
            toolbarItems.add(new ToolBarItemDesc(toolBarBtn));
        }
        return toolbarItems;
    }
    
    //-------------------------------------------------------
    // SecurityOption Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getAdditionalSecurityOptions()
     */
    /*@Override
    public List<SecurityOptionIFace> getAdditionalSecurityOptions()
    {
        List<SecurityOptionIFace> list = new ArrayList<SecurityOptionIFace>();
        
        SecurityOption secOpt = new SecurityOption(ATTACHMENTS_SECURITY, 
                                                    getResourceString("ATTACHMENTS_TITLE"), 
                                                    securityPrefix,
                                                    new BasicPermisionPanel(ATTACHMENTS_TITLE, 
                                                                            "ATTACHMENTS_VIEW", 
                                                                            "ATTACHMENTS_EDIT"));
        addPerms(secOpt, new boolean[][] 
                   {{true, true, true, false},
                   {false, false, false, false},
                   {false, false, false, false},
                   {false, false, false, false}});
        
        list.add(secOpt);

        return list;
    }*/
    
    //-------------------------------------------------------
    // BaseTask
    //-------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#shutdown()
     */
    @Override
    public void shutdown()
    {
        super.shutdown();
        
        if (imagesPane != null)
        {
            imagesPane.shutdown();
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        menuItems = new Vector<MenuItemDesc>();
        /*
        final String DATA_MENU = "Specify.DATA_MENU";
        
        SecurityMgr secMgr = SecurityMgr.getInstance();
        
        MenuItemDesc mid;
        JMenuItem mi;
        String    menuDesc = getResourceString(ATTACHMENTS_MENU);

        String securityName = buildTaskPermissionName(ATTACHMENTS_SECURITY);
        if (!AppContextMgr.isSecurityOn() || 
            (secMgr.getPermission(securityName) != null && 
             !secMgr.getPermission(securityName).hasNoPerm()))
        {
            mi       = UIHelper.createLocalizedMenuItem(ATTACHMENTS_MENU, ATTACHMENTS_MNU, ATTACHMENTS_TITLE, true, null); 
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            LifeMapperTask.this.requestContext();
                        }
                    });
                }
            });
            mid = new MenuItemDesc(mi, DATA_MENU);
            mid.setPosition(MenuItemDesc.Position.Bottom, menuDesc);
            menuItems.add(mid);
        }
        */
        return menuItems;
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------
    
    /**
     * Processes all Commands of type RECORD_SET.
     * @param cmdAction the command to be processed
     */
    protected void processRecordSetCommands(final CommandAction cmdAction)
    {
        if (cmdAction.isAction("Display"))
        {
            if (cmdAction.getData() instanceof RecordSetIFace)
            {
                RecordSetIFace rs = (RecordSetIFace)cmdAction.getData();
                if (rs.getDbTableId() == Taxon.getClassTableId())
                {
                    // Loop through all the RS ids and get the names
                    //BasicSQLUtils.querySingleObj("SELECT)
                    //((LifeMapperPane)getStarterPane()).doSearchGenusSpecies(searchStr)
                }
            } /*else if (cmdAction.getData() instanceof Pair<?,?>)
            {
                @SuppressWarnings("unchecked")
                Pair<Taxon, RecordSet> p = (Pair<Taxon, RecordSet>)cmdAction.getData();
                getStarterPane(); // make sure it is loaded
                imagesPane.resetWWPanel();
                imagesPane.setDoResetWWPanel(false);
                imagesPane.doSearchGenusSpecies(p.first.getFullName(), true);
                requestContext();
            }*/
        } if (cmdAction.isAction(ATTACHMENTS_SEARCH))
        {
            if (cmdAction.getData() instanceof RecordSetIFace)
            {
                RecordSetIFace rs = (RecordSetIFace)cmdAction.getData();
                searchForAttachments(rs);
            } 
        }
    }
    
    /**
     * @param cmdAction
     */
    protected void prefsChanged(final CommandAction cmdAction)
    {
        reAddToolBarItem(cmdAction, toolBarBtn, ON_TASKBAR);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doProcessAppCommands(edu.ku.brc.ui.CommandAction)
     */
    @Override
    protected void doProcessAppCommands(CommandAction cmdAction)
    {
        super.doProcessAppCommands(cmdAction);
        
        if (cmdAction.isAction(APP_RESTART_ACT))
        {
            isInitialized = false;
            initialize();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        super.doCommand(cmdAction);
        
        if (cmdAction.isType(ATTACHMENTS))
        {
            if (cmdAction.isAction(EXPORT_CMD))
            {
                exportAttachment(cmdAction);
                
            } else if (cmdAction.isAction("DisplayAttachment"))
            {
                displayAttachment(cmdAction);
            } else
            {
                processRecordSetCommands(cmdAction);
            }
        } else if (cmdAction.isType(PreferencesDlg.PREFERENCES))
        {
            prefsChanged(cmdAction);
        }
    }

}
