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
package edu.ku.brc.specify.prefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.af.ui.forms.PanelViewable;
import edu.ku.brc.af.ui.forms.validation.ValBrowseBtnPanel;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.FileStoreAttachmentManager;
import edu.ku.brc.util.WebStoreAttachmentMgr;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Nov 8, 2011
 *
 */
public class AttachmentPrefs extends GenericPrefsPanel implements PrefsSavable, PrefsPanelIFace
{
    protected static final String ATTACHMENT_PATH     = "attachment.path";
    protected static final String ATTACHMENT_URL      = "attachment.url";
    protected static final String ATTCH_PATH_ID       = "attch_path";
    protected static final String ATTCH_URL_ID        = "attch_url";
    
    protected static final String ATTACHMENT_USE_PATH = "attachment.use_path";

    
    protected AppPreferences remotePrefs = AppPreferences.getRemote();
    protected AppPreferences localPrefs  = AppPreferences.getLocalPrefs();
    
    protected String oldAttachmentPath   = null;
    protected String oldAttachmentURL    = null;
    
    protected JRadioButton pathRB;
    protected JRadioButton urlRB;


    /**
     * 
     */
    public AttachmentPrefs()
    {
        super();
        
        createForm("Preferences", "Attachments");
        
        PanelViewable pathPanel = form.getCompById("path_panel");
        PanelViewable urlPanel  = form.getCompById("url_panel");
        
        UIRegistry.loadAndPushResourceBundle("preferences");
        pathRB = UIHelper.createRadioButton(UIRegistry.getResourceString("USE_ATTACH_PATH"));
        urlRB = UIHelper.createRadioButton(UIRegistry.getResourceString("USE_ATTACH_URL"));
        UIRegistry.popResourceBundle();
        
        ButtonGroup group = new ButtonGroup();
        group.add(pathRB);
        group.add(urlRB);
        
        CellConstraints cc = new CellConstraints();
        pathPanel.add(pathRB, cc.xy(1, 1));
        urlPanel.add(urlRB,  cc.xy(1, 1));
        
        ValBrowseBtnPanel browse = form.getCompById(ATTCH_PATH_ID);
        if (browse != null)
        {
            oldAttachmentPath = localPrefs.get(ATTACHMENT_PATH, null);
            browse.setValue(oldAttachmentPath, null);
            
            browse.getTextField().addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e)
                {
                    super.focusLost(e);
                    verifyAttachmentPath();
                }
            });
        }
        
        boolean isUsingPath = localPrefs.getBoolean(ATTACHMENT_USE_PATH, true);
        
        toggleAttachmentsEnabledState(isUsingPath, false);
        pathRB.setSelected(isUsingPath);
        
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                toggleAttachmentsEnabledState(pathRB.isSelected(), true);
            }
         };
         pathRB.addActionListener(al);
         urlRB.addActionListener(al);

        oldAttachmentURL = localPrefs.get(ATTACHMENT_URL, "");
        ValTextField urlTF = form.getCompById(ATTCH_URL_ID);
        if (urlTF != null)
        {
            urlTF.setValue(oldAttachmentURL, null);
            urlTF.addFocusListener(new FocusAdapter()
            {
                @Override
                public void focusLost(FocusEvent e)
                {
                    verifyAttachmentPath();
                }
            });
        }
    }
    
    /**
     * @param isSelected
     * @param doClearFields
     */
    protected void toggleAttachmentsEnabledState(final boolean isSelected, final boolean doClearFields)
    {
        //System.out.println("isSelected:"+isSelected);
        
        ValBrowseBtnPanel pathBrwse = form.getCompById(ATTCH_PATH_ID);
        JLabel            pathLbl   = form.getLabelFor(ATTCH_PATH_ID);
        
        ValTextField      urlTxt    = form.getCompById(ATTCH_URL_ID);
        JLabel            urlLbl    = form.getLabelFor(ATTCH_URL_ID); 
        pathBrwse.setEnabled(isSelected);
        pathLbl.setEnabled(isSelected);
        urlTxt.setEnabled(!isSelected);
        urlLbl.setEnabled(!isSelected);
        
        if (doClearFields)
        {
            if (isSelected)
            {
                urlTxt.setValue("", null);
            } else
            {
                pathBrwse.setValue("", null);
            }
        }
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        if (form.getValidator() == null || form.getValidator().hasChanged())
        {
            super.savePrefs();
            
            Boolean usingPath = (Boolean)pathRB.isSelected();
            localPrefs.putBoolean(ATTACHMENT_USE_PATH, usingPath == null ? false : usingPath);  
            verifyAttachmentPath();
            
            try
            {
                localPrefs.flush();
                
            } catch (BackingStoreException ex) {}
        }
    }
    
    /**
     * 
     */
    private void verifyAttachmentPath()
    {
        boolean isUsingPath = pathRB.isSelected();
        
        if (isUsingPath)
        {
            ValBrowseBtnPanel browse = form.getCompById(ATTCH_PATH_ID);
            if (browse != null)
            {
                String newAttachmentPath = browse.getValue().toString();
                if (newAttachmentPath != null && !oldAttachmentPath.equals(newAttachmentPath))
                {
                    if (newAttachmentPath.isEmpty())
                    {
                        UIRegistry.showLocalizedError("SystemPrefs.NOEMPTY_ATTCH");
                        browse.setValue(oldAttachmentPath, oldAttachmentPath);
                        
                    } else if (okChangeAttachmentPath(oldAttachmentPath, newAttachmentPath))
                    {
                        if (!oldAttachmentPath.equals(newAttachmentPath))
                        {
                            localPrefs.put(ATTACHMENT_PATH, newAttachmentPath);
                            try
                            {
                                if (AttachmentUtils.getAttachmentManager() == null)
                                {
                                    AttachmentUtils.setAttachmentManager(new FileStoreAttachmentManager(new File(newAttachmentPath)));
                                } else
                                {
                                    AttachmentUtils.getAttachmentManager().setDirectory(new File(newAttachmentPath));
                                }
                                localPrefs.put(ATTACHMENT_URL, "");
                            } catch (IOException ex)
                            {
                                UIRegistry.showLocalizedError("SystemPrefs.ESA");
                            }
                        }
                        
                    } else
                    {
                        UIRegistry.showLocalizedError("SystemPrefs.DOESNT_EXIST", newAttachmentPath);
                        browse.setValue(oldAttachmentPath, oldAttachmentPath);
                    }
                }
            }
        } else
        {
            ValTextField urlTF = form.getCompById(ATTCH_URL_ID);
            if (urlTF != null)
            {
                String newAttachmentURL = urlTF.getValue().toString();
                
                if (newAttachmentURL != null && !oldAttachmentURL.equals(newAttachmentURL))
                {
                    if (newAttachmentURL.isEmpty())
                    {
                        UIRegistry.showLocalizedError("SystemPrefs.NOEMPTY_ATTCH");
                        urlTF.setValue(oldAttachmentURL, oldAttachmentURL);
                        
                    } else 
                    {
                        localPrefs.put(ATTACHMENT_URL, newAttachmentURL);
                        WebStoreAttachmentMgr webAssetMgr = new WebStoreAttachmentMgr();
                        if (!webAssetMgr.isInitialized())
                        {
                            webAssetMgr = null;
                        } else
                        {
                            localPrefs.put(ATTACHMENT_PATH, "");
                        }
                        AttachmentUtils.setAttachmentManager(webAssetMgr);
                        
                        if (webAssetMgr == null)
                        {
                            UIRegistry.showLocalizedError("SystemPrefs.NOEMPTY_ATTCH");
                            //urlTF.setValue(oldAttachmentURL, oldAttachmentURL);
                            // http://129.237.201.34/web_asset_store.xml
                        }
                    }
                }
            }
        }
    }
    
    
    /**
     * @param oldPath
     * @param newPath
     * @return
     */
    protected boolean okChangeAttachmentPath(final String oldPath, final String newPath)
    {
        /*if (false)
        {
            File  oldDir = new File(oldPath);
            if (oldDir.exists())
            {
                File origDir = new File(oldPath + File.separator + "originals");
                
                boolean doMoveFiles = false;
                int numFiles = origDir.listFiles().length;
                if (numFiles > 0)
                {
                    Object[] options = { getResourceString("SystemPrefs.MV_FILES"),  //$NON-NLS-1$
                                         getResourceString("CANCEL")  //$NON-NLS-1$
                                       };
                    int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                                getLocalizedMessage("SystemPrefs.MV_FILES_MSG"),  //$NON-NLS-1$
                                                                getResourceString("SystemPrefs.ATTCH_TITLE"),  //$NON-NLS-1$
                                                                JOptionPane.YES_NO_OPTION,
                                                                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if (userChoice == JOptionPane.YES_OPTION)
                    {
                        doMoveFiles = true;
                    } else
                    {
                        return false;
                    }
                }
                
                if (doMoveFiles)
                {
                    File newDir = new File(newPath);
                    try
                    {
                        AttachmentUtils.getAttachmentManager().setDirectory(newDir);
                        
                        //File newParentDir = new File(newDir.getParent());
                        for (File file : oldDir.listFiles())
                        {
                            if (file.isDirectory())
                            {
                                FileUtils.copyDirectoryToDirectory(file, newDir);
                                
                            } else if (!file.getName().equals(".") && !file.getName().equals(".."))
                            {
                                FileUtils.copyFileToDirectory(file, newDir);
                            }
                        }
                        //FileUtils.copyDirectoryToDirectory(oldDir, newParentDir);
                        
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SystemPrefs.class, ex);
                        ex.printStackTrace();
                        try
                        {
                            AttachmentUtils.getAttachmentManager().setDirectory(oldDir);
                            
                        } catch (Exception ex2)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SystemPrefs.class, ex);
                            ex2.printStackTrace();
                        }
                        UIRegistry.showLocalizedError("SystemPrefs.NO_MOVE_ATTCH", newDir.getAbsoluteFile());
                        return false;
                    }
                }
            }
        } else
        {*/
            return AttachmentUtils.isAttachmentDirMounted(new File(newPath));
        //}
        
        //return true;
    }


}
