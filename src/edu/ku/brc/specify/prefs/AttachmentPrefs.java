/* Copyright (C) 2015, University of Kansas Center for Research
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
import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

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
import edu.ku.brc.util.WebStoreAttachmentException;
import edu.ku.brc.util.WebStoreAttachmentKeyException;
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
    protected static final String ATTACHMENT_KEY      = "attachment.key";
    protected static final String ATTCH_PATH_ID       = "attch_path";
    protected static final String ATTCH_URL_ID        = "attch_url";
    protected static final String ATTCH_KEY_ID        = "attch_key";
    
    protected boolean           isUsingGlobalAttchPrefs = false;
    protected boolean           canEditGlobalAttchPrefs = false;
    
    protected AppPreferences    remotePrefs = AppPreferences.getRemote();
    protected AppPreferences    localPrefs  = AppPreferences.getLocalPrefs();
    protected AppPreferences    globalPrefs = AppPreferences.getGlobalPrefs();
    
    protected ValBrowseBtnPanel pathBrwse;
    protected JLabel            pathLbl;
    protected ValTextField      urlTxt;
    protected JLabel            urlLbl;
    
    protected JLabel    keyLbl;
    protected ValTextField  keyTxt;

    protected boolean           isInitialized       = false;
    protected String            oldAttachmentPath   = null;
    protected String            oldAttachmentURL    = null;
    protected String            oldAttachmentKey    = null;
    
    protected String            cachedAttachmentPath   = null;
    protected String            cachedAttachmentURL    = null;
    protected String            cachedAttachmentKey    = null;
    
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
        keyTxt    = form.getCompById(ATTCH_KEY_ID);
        keyLbl    = form.getLabelFor(ATTCH_KEY_ID);
        
        isInitialized = pathPanel != null && urlPanel != null && pathBrwse != null && pathLbl != null && urlTxt != null && urlLbl != null;
        if (!isInitialized)
        {
            UIRegistry.showError("The form is not setup correctly.");
            return;
        }
        
        isUsingGlobalAttchPrefs = globalPrefs.getBoolean(USE_GLOBAL_PREFS, false);
        canEditGlobalAttchPrefs = localPrefs.getBoolean(EDT_GLOBAL_PREFS, false);
        
        UIRegistry.loadAndPushResourceBundle("preferences");
        pathRB = UIHelper.createRadioButton(UIRegistry.getResourceString("USE_ATTACH_PATH"));
        urlRB  = UIHelper.createRadioButton(UIRegistry.getResourceString("USE_ATTACH_URL"));
        UIRegistry.popResourceBundle();
        
        pathRB.setOpaque(false);
        urlRB.setOpaque(false);
        
        ButtonGroup group = new ButtonGroup();
        group.add(pathRB);
        group.add(urlRB);
        
        CellConstraints cc = new CellConstraints();
        if (pathPanel != null) pathPanel.add(pathRB, cc.xy(1, 1));
        if (urlPanel != null)  urlPanel.add(urlRB,   cc.xy(1, 1));
        
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
                    oldAttachmentURL  = (String)urlTxt.getText().trim();
                    oldAttachmentKey  = (String)keyTxt.getText().trim();

                    globalPrefs.put(ATTACHMENT_PATH, oldAttachmentPath);
                    globalPrefs.put(ATTACHMENT_URL, oldAttachmentURL);
                    globalPrefs.put(ATTACHMENT_KEY, oldAttachmentKey);
                    globalPrefs.putBoolean(ATTACHMENT_USE_PATH, pathRB.isSelected());
                    
                    // Make sure local prefs is set for the type we are using.
                    localPrefs.putBoolean(ATTACHMENT_USE_PATH, pathRB.isSelected()); 
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
                    globalPrefs.remove(ATTACHMENT_PATH);
                    globalPrefs.remove(ATTACHMENT_URL);
                    globalPrefs.remove(ATTACHMENT_KEY);
                    globalPrefs.remove(ATTACHMENT_USE_PATH);
                    try
                    {
                        globalPrefs.flush();
                    } catch (BackingStoreException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
        
        if (!isUsingGlobalAttchPrefs || canEditGlobalAttchPrefs)
        {
            ActionListener al = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    toggleAttachmentsEnabledState(pathRB.isSelected());
                }
            };
            pathRB.addActionListener(al);
            urlRB.addActionListener(al);
        } else
        {
            pathBrwse.setEnabled(false);
            urlTxt.setEnabled(false);
            keyTxt.setEnabled(false);
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
     */
    protected void toggleAttachmentsEnabledState(final boolean isPathSelected)
    {
        if (!isInitialized) return;
        
        
        if (!isUsingGlobalAttchPrefs || canEditGlobalAttchPrefs)
        {
            pathBrwse.setEnabled(isPathSelected);
            pathLbl.setEnabled(isPathSelected);
            urlTxt.setEnabled(!isPathSelected);
            urlLbl.setEnabled(!isPathSelected);
            keyTxt.setEnabled(!isPathSelected);
            keyLbl.setEnabled(!isPathSelected);
            
        } else if (isUsingGlobalAttchPrefs)
        {
            pathBrwse.setEnabled(false);
            pathLbl.setEnabled(false);
            urlTxt.setEnabled(false);
            urlLbl.setEnabled(false);
            keyTxt.setEnabled(false);
            keyLbl.setEnabled(false);
        } else
        {
            pathBrwse.setEnabled(isPathSelected);
            pathLbl.setEnabled(isPathSelected);
            urlTxt.setEnabled(!isPathSelected);
            urlLbl.setEnabled(!isPathSelected);
            keyTxt.setEnabled(!isPathSelected);
            keyLbl.setEnabled(!isPathSelected);
        }
        
        if (isPathSelected)
        {
            String newURL = urlTxt.getText().trim();
            String newKey = keyTxt.getText().trim();
            if (StringUtils.isNotEmpty(newURL))
            {
                cachedAttachmentURL = newURL;
            }
            if (StringUtils.isNotEmpty(newURL))
            {
                cachedAttachmentKey = newKey;
            }
            pathBrwse.setValue(cachedAttachmentPath, null);
            urlTxt.setValue("", null);
            keyTxt.setValue("", null);
        } else
        {
            String newPath = (String)pathBrwse.getValue();
            if (StringUtils.isNotEmpty(newPath))
            {
                cachedAttachmentPath = newPath;
            }
            urlTxt.setValue(cachedAttachmentURL, null);
            keyTxt.setValue(cachedAttachmentKey, null);
            pathBrwse.setValue("", null);
        }
    }
    
    /**
     * @param prefs
     * @return
     */
    private boolean setDataIntoUI()
    {
        AppPreferences prefs = (!isUsingGlobalAttchPrefs || canEditGlobalAttchPrefs) ? localPrefs : globalPrefs;
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
        oldAttachmentKey  = prefs.get(ATTACHMENT_KEY, null);
        
        cachedAttachmentPath = prefs.get(ATTACHMENT_PATH, "");
        cachedAttachmentURL  = prefs.get(ATTACHMENT_URL, "");
        cachedAttachmentKey = prefs.get(ATTACHMENT_KEY, "");
        
        if ((isEmpty(oldAttachmentPath) && isNotEmpty(oldAttachmentURL)) || isUsingGlobalAttchPrefs)
        {
            isUsingPath = false;
        }
        
        pathBrwse.setValue(oldAttachmentPath, null);
        urlTxt.setValue(oldAttachmentURL, null);
        keyTxt.setValue(oldAttachmentKey, null);

        setRadio(isUsingPath);
        
        toggleAttachmentsEnabledState(isUsingPath);
        
        return true;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#isOKToSave()
     */
    @Override
    public boolean isOKToSave()
    {
        if (pathRB.isSelected())
        {
            oldAttachmentPath = (String)pathBrwse.getValue();
            oldAttachmentURL  = "";
            oldAttachmentKey = "";
            if (StringUtils.isNotEmpty(oldAttachmentPath))
            {
                try
                {
                    if (AttachmentUtils.getAttachmentManager() == null)
                    {
                        AttachmentUtils.setAttachmentManager(new FileStoreAttachmentManager(new File(oldAttachmentPath)));
                    } else
                    {
                        AttachmentUtils.getAttachmentManager().setDirectory(new File(oldAttachmentPath));
                    }
                    return true;
                    
                } catch (IOException ex)
                {
                    UIRegistry.showLocalizedError("SystemPrefs.BAD_ATTCH_PATH");
                }
            } else
            {
                UIRegistry.showLocalizedError("SystemPrefs.NOEMPTY_ATTCH");
            }
        } else
        {
            oldAttachmentPath = "";
            oldAttachmentURL  = (String)urlTxt.getText().trim();
            oldAttachmentKey  = (String)keyTxt.getText().trim();
            
            if (StringUtils.isNotEmpty(oldAttachmentURL))
            {
                WebStoreAttachmentMgr webAssetMgr = null;
                try
                {
                    webAssetMgr = new WebStoreAttachmentMgr(oldAttachmentURL, oldAttachmentKey);
                    AttachmentUtils.setAttachmentManager(webAssetMgr);
                    return true;
                } catch (WebStoreAttachmentKeyException e)
                {
                    UIRegistry.showLocalizedError("SystemPrefs.BAD_ATTCH_KEY");
                } catch (WebStoreAttachmentException e)
                {
                    UIRegistry.showLocalizedError("SystemPrefs.BAD_ATTCH_URL");
                }
            } else
            {
                UIRegistry.showLocalizedError("SystemPrefs.NOEMPTY_URL");
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        if (!isInitialized) return;
        
        boolean usingPath = pathRB.isSelected();
        
        if (usingPath || !isUsingGlobalAttchPrefs || canEditGlobalAttchPrefs)
        {
            if (form.getValidator() == null || form.getValidator().hasChanged())
            {
                super.savePrefs(); // Gets data from form
                
                if (usingPath)
                {
                    localPrefs.put(ATTACHMENT_PATH, oldAttachmentPath);
                    localPrefs.put(ATTACHMENT_URL, "");
                    localPrefs.put(ATTACHMENT_KEY, "");
                } else
                {
                    localPrefs.put(ATTACHMENT_URL, oldAttachmentURL);
                    localPrefs.put(ATTACHMENT_KEY, oldAttachmentKey);
                    localPrefs.put(ATTACHMENT_PATH, "");
                }
                localPrefs.putBoolean(ATTACHMENT_USE_PATH, usingPath); 
                
                try
                {
                    localPrefs.flush();
                    
                } catch (BackingStoreException ex) {}
            }
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
