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

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

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
    protected static final String USE_GLOBAL_PREFS    = "USE_GLOBAL_PREFS";
    protected static final String EDT_GLOBAL_PREFS    = "EDT_GLOBAL_PREFS";
    
    protected static final String ATTACHMENT_USE_PATH = "attachment.use_path";
    protected static final String ATTACHMENT_PATH     = "attachment.path";
    protected static final String ATTACHMENT_URL      = "attachment.url";
    protected static final String ATTCH_PATH_ID       = "attch_path";
    protected static final String ATTCH_URL_ID        = "attch_url";
    
    protected boolean           isUsingGlobalAttchPrefs = false;
    protected boolean           canEditGlobalAttchPrefs = false;
    
    protected AppPreferences    remotePrefs = AppPreferences.getRemote();
    protected AppPreferences    localPrefs  = AppPreferences.getLocalPrefs();
    protected AppPreferences    globalPrefs = AppPreferences.getLocalPrefs();
    
    protected ValBrowseBtnPanel pathBrwse;
    protected JLabel            pathLbl;
    protected ValTextField      urlTxt;
    protected JLabel            urlLbl; 

    protected boolean           isInitialized       = false;
    protected String            oldAttachmentPath   = null;
    protected String            oldAttachmentURL    = null;
    
    protected JRadioButton      pathRB;
    protected JRadioButton      urlRB;

    /**
     * 
     */
    public AttachmentPrefs()
    {
        super();
        
        createForm("Preferences", "Attachments");
        
        PanelViewable pathPanel = form.getCompById("path_panel");
        PanelViewable urlPanel  = form.getCompById("url_panel");
        
        pathBrwse = form.getCompById(ATTCH_PATH_ID);
        pathLbl   = form.getLabelFor(ATTCH_PATH_ID);
        urlTxt    = form.getCompById(ATTCH_URL_ID);
        urlLbl    = form.getLabelFor(ATTCH_URL_ID); 
        
        isInitialized = pathPanel != null && urlPanel != null && pathBrwse != null && pathLbl != null && urlTxt != null && urlLbl != null;
        if (!isInitialized)
        {
            UIRegistry.showError("The form is not setup correctly.");
            return;
        }
        
        isUsingGlobalAttchPrefs = AppPreferences.getGlobalPrefs().getBoolean(USE_GLOBAL_PREFS, false);
        canEditGlobalAttchPrefs = AppPreferences.getGlobalPrefs().getBoolean(EDT_GLOBAL_PREFS, false);
        
        UIRegistry.loadAndPushResourceBundle("preferences");
        pathRB = UIHelper.createRadioButton(UIRegistry.getResourceString("USE_ATTACH_PATH"));
        urlRB  = UIHelper.createRadioButton(UIRegistry.getResourceString("USE_ATTACH_URL"));
        UIRegistry.popResourceBundle();
        
        ButtonGroup group = new ButtonGroup();
        group.add(pathRB);
        group.add(urlRB);
        
        CellConstraints cc = new CellConstraints();
        pathPanel.add(pathRB, cc.xy(1, 1));
        urlPanel.add(urlRB,   cc.xy(1, 1));
        
        JButton saveGGblPrefs  = form.getCompById("SaveGGblPrefs");
        JButton clearGGblPrefs = form.getCompById("ClearGGblPrefs");
        if (saveGGblPrefs != null)
        {
            saveGGblPrefs.setVisible(isUsingGlobalAttchPrefs && canEditGlobalAttchPrefs);
            saveGGblPrefs.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    AppPreferences.getGlobalPrefs().put(ATTACHMENT_PATH, oldAttachmentPath);
                    AppPreferences.getGlobalPrefs().put(ATTACHMENT_URL, oldAttachmentURL);
                    AppPreferences.getGlobalPrefs().putBoolean(ATTACHMENT_USE_PATH, pathRB.isSelected());
                }
            });
        }
        
        if (clearGGblPrefs != null)
        {
            clearGGblPrefs.setVisible(isUsingGlobalAttchPrefs && canEditGlobalAttchPrefs);
            clearGGblPrefs.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    AppPreferences.getGlobalPrefs().remove(ATTACHMENT_PATH);
                    AppPreferences.getGlobalPrefs().remove(ATTACHMENT_URL);
                    AppPreferences.getGlobalPrefs().remove(ATTACHMENT_USE_PATH);
                }
            });
        }
        
        if (!isUsingGlobalAttchPrefs || canEditGlobalAttchPrefs)
        {
            pathBrwse.getTextField().addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e)
                {
                    verifyAttachmentPath();
                    toggleAttachmentsEnabledState(pathRB.isSelected(), true);
                    if (pathRB.isEnabled())
                    {
                        configAttachmentMgr(oldAttachmentPath, pathRB.isSelected());
                    }
                }
            });
            urlTxt.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e)
                {
                    verifyAttachmentPath();
                    toggleAttachmentsEnabledState(pathRB.isSelected(), true);
                    if (urlRB.isEnabled())
                    {
                        configAttachmentMgr(oldAttachmentURL, pathRB.isSelected());
                    }
                }
            });
            
            ActionListener al = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    toggleAttachmentsEnabledState(pathRB.isSelected(), true);
                }
            };
            pathRB.addActionListener(al);
            urlRB.addActionListener(al);
        } else
        {
            pathBrwse.setEnabled(false);
            urlTxt.setEnabled(false);
            pathRB.setEnabled(false);
            urlRB.setEnabled(false);
        }
        
        setDataIntoUI();
    }
    
    /**
     * @param isUsingPath
     */
    private void setRadio(final boolean isUsingPath)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                pathRB.setSelected(isUsingPath);
                urlRB.setSelected(!isUsingPath);
            }
        });
    }
    
    /**
     * @param isPathSelected
     * @param doClearFields
     */
    protected void toggleAttachmentsEnabledState(final boolean isPathSelected, 
                                                 final boolean doClearFields)
    {
        if (!isInitialized) return;
        //System.out.println("isSelected:"+isSelected);
        
        pathBrwse.setEnabled(isPathSelected);
        pathLbl.setEnabled(isPathSelected);
        urlTxt.setEnabled(!isPathSelected);
        urlLbl.setEnabled(!isPathSelected);
        
        if (doClearFields)
        {
            if (isPathSelected)
            {
                urlTxt.setValue("", null);
            } else
            {
                pathBrwse.setValue("", null);
            }
        }
    }
    
    /**
     * @param prefs
     * @return
     */
    private boolean setDataIntoUI()
    {
        AppPreferences prefs = (!isUsingGlobalAttchPrefs || canEditGlobalAttchPrefs) ? AppPreferences.getLocalPrefs() : AppPreferences.getGlobalPrefs();
        return setDataIntoUI(prefs);
    }
    
    /**
     * @param prefs
     * @return
     */
    private boolean setDataIntoUI(final AppPreferences prefs)
    {
        boolean isUsingPath = true;
        
        oldAttachmentPath = prefs.get(ATTACHMENT_PATH, null);
        oldAttachmentURL  = prefs.get(ATTACHMENT_URL, null);
        
        if (isEmpty(oldAttachmentPath) && isNotEmpty(oldAttachmentURL))
        {
            isUsingPath = false;
        }
        pathBrwse.setValue(oldAttachmentPath, null);
        urlTxt.setValue(oldAttachmentURL, null);

        setRadio(isUsingPath);
        
        return true;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        if (!isInitialized) return;
        
        if (!isUsingGlobalAttchPrefs || canEditGlobalAttchPrefs)
        {
            if (form.getValidator() == null || form.getValidator().hasChanged())
            {
                super.savePrefs();
                
                boolean usingPath = pathRB.isSelected();
                localPrefs.putBoolean(ATTACHMENT_USE_PATH, usingPath); 
                
                verifyAttachmentPath();
                
                try
                {
                    localPrefs.flush();
                    
                } catch (BackingStoreException ex) {}
            }
        }
    }
    
    /**
     * @param configStr
     * @param isUsingPath
     */
    private void configAttachmentMgr(final String configStr, final boolean isUsingPath)
    {
        if (isUsingPath)
        {
            localPrefs.put(ATTACHMENT_PATH, configStr);
            try
            {
                if (AttachmentUtils.getAttachmentManager() == null)
                {
                    AttachmentUtils.setAttachmentManager(new FileStoreAttachmentManager(new File(configStr)));
                } else
                {
                    AttachmentUtils.getAttachmentManager().setDirectory(new File(configStr));
                }
                localPrefs.put(ATTACHMENT_URL, "");
                
            } catch (IOException ex)
            {
                UIRegistry.showLocalizedError("SystemPrefs.ESA");
            }
        } else
        {
            localPrefs.put(ATTACHMENT_URL, configStr);
            localPrefs.put(ATTACHMENT_PATH, "");
            
            WebStoreAttachmentMgr webAssetMgr = new WebStoreAttachmentMgr();
            if (!webAssetMgr.isInitialized())
            {
                webAssetMgr = null;
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
    
    /**
     * 
     */
    private void verifyAttachmentPath()
    {
        if (!isInitialized) return;
        
        boolean isUsingPath       = pathRB.isSelected();
        String  newAttachmentPath = pathBrwse.getValue().toString();
        String  newAttachmentURL  = urlTxt.getValue().toString();

        if (isUsingPath)
        {
            if (isNotEmpty(newAttachmentPath))
            {
                if (!oldAttachmentPath.equals(newAttachmentPath))
                {
                    oldAttachmentPath = newAttachmentPath;
                }
            } else
            {
                UIRegistry.showLocalizedError("SystemPrefs.NOEMPTY_ATTCH");
            }
            
            urlTxt.setValue("", null);
        } else
        {
            if (isNotEmpty(newAttachmentURL))
            {
                if (!oldAttachmentURL.equals(newAttachmentURL))
                {
                    oldAttachmentURL = newAttachmentURL;
                }
            } else
            {
                UIRegistry.showLocalizedError("SystemPrefs.NOEMPTY_ATTCH");
            }
            pathBrwse.setValue("", null);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#getHelpContext()
     */
    @Override
    public String getHelpContext()
    {
        return "PrefsAttachments";
    }

}
