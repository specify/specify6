/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.ui.weblink;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIPluginable;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.util.AttachmentUtils;

/**
 * 
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 13, 2008
 *
 */
public class WebLinkButton extends JPanel implements UIPluginable, GetSetValueIFace, ActionListener
{
    protected Logger log = Logger.getLogger(WebLinkButton.class);
    
    protected JButton                       launchBtn;
    protected JButton                       editBtn     = null;
    
    protected WebLinkDef                    webLinkDef  = null;
    
    protected String                        urlStr;
    protected Properties                    initProps;
    protected FormDataObjIFace              dataObj;
    
    protected WebLinkDataProviderIFace      provider = null;
    
    protected Hashtable<String, JTextField> textFieldHash = new Hashtable<String, JTextField>();
    protected Hashtable<String, String>     valueHash     = new Hashtable<String, String>();
    
    protected CustomDialog                  promptDialog  = null;
    /**
     * 
     */
    public WebLinkButton()
    {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent ae)
    {
        // Parse the format and build the URL
        String urlString;
        try
        {
            urlString = buildURL();
        }
        catch (Exception e1)
        {
            log.error("Failed to build URL", e1); //$NON-NLS-1$
            return;
        }
        
        if (urlString == null)
        {
            // an error message should have already been put on the status bar
            // just exit
            return;
        }
        
        // Convert to a URI
        URI uri;
        try
        {
            uri = new URI(urlString);
        }
        catch (URISyntaxException e)
        {
            log.error("Bad URL syntax: " + urlString, e); //$NON-NLS-1$
            return;
        }

        // Open the URI
        try
        {
            AttachmentUtils.openURI(uri);
        }
        catch (Exception e)
        {
            log.error("Failed to open URL: " + uri.toString(), e); //$NON-NLS-1$
            return;
        }
    }
    
    /**
     * @return
     */
    private CustomDialog createPromptDlg(final Hashtable<String, String> backupHash)
    {
        if (webLinkDef != null)
        {
            // Start by getting the data needed to build the URL
            // so first see if we need to prompt for data.
            
            int promptCnt = webLinkDef.getPromptCount();
            if (promptCnt > 0 || backupHash.size() > 0)
            {
                textFieldHash.clear();
                
                promptCnt += backupHash.size();
                
                String          rowDef = createDuplicateJGoodiesDef("p", "4px", promptCnt); //$NON-NLS-1$ //$NON-NLS-2$
                PanelBuilder    pb     = new PanelBuilder(new FormLayout("p,2px,f:p:g", rowDef)); //$NON-NLS-1$
                CellConstraints cc     = new CellConstraints();
                
                DocumentListener dl = new DocumentListener()
                {
                    private void checkFields()
                    {
                        boolean enableOK = true;
                        for (JTextField tf : textFieldHash.values())
                        {
                            if (tf.getText().length() == 0)
                            {
                                enableOK = false;
                                break;
                            }
                        }
                        promptDialog.getOkBtn().setEnabled(enableOK);

                    }
                    public void changedUpdate(DocumentEvent e)
                    {
                        checkFields();
                    }
                    public void insertUpdate(DocumentEvent e)
                    {
                        checkFields();
                    }
                    public void removeUpdate(DocumentEvent e)
                    {
                        checkFields();
                    }
                };
                
                int y = 1;
                for (WebLinkDefArg arg : webLinkDef.getArgs())
                {
                    if (arg.isPrompt())
                    {
                        JTextField txtField = createTextField(15);
                        txtField.getDocument().addDocumentListener(dl);
                        textFieldHash.put(arg.getName(), txtField);
                        pb.add(createLabel(arg.getTitle(), SwingConstants.RIGHT), cc.xy(1, y));
                        pb.add(txtField, cc.xy(3, y));
                        y += 2;
                    }
                }
                
                for (String name : backupHash.keySet())
                {
                    JTextField txtField = createTextField(15);
                    txtField.getDocument().addDocumentListener(dl);
                    textFieldHash.put(name, txtField);
                    pb.add(createLabel(backupHash.get(name), SwingConstants.RIGHT), cc.xy(1, y));
                    pb.add(txtField, cc.xy(3, y));
                    y += 2;
                }
                
                pb.setDefaultDialogBorder();
                
                return new CustomDialog((Frame)getTopWindow(), 
                                        getResourceString("WBLK_PROMPT_DATA"),
                                        true,
                                        CustomDialog.OKCANCELHELP, 
                                        pb.getPanel());
            }  
        }
        return null;
    }

    /**
     * @return
     * @throws Exception
     */
    protected String buildURL() throws Exception
    {
        if (StringUtils.isNotEmpty(urlStr))
        {
            return urlStr;
        }
        
        if (webLinkDef != null)
        {
            // Start by getting the data needed to build the URL
            // so first see if we need to prompt for data.
            
            Hashtable<String, String> backupPrompt = new Hashtable<String, String>();
            for (WebLinkDefArg arg : webLinkDef.getArgs())
            {
                if (!arg.isPrompt())
                {
                    String name  = arg.getName();
                    String value = ""; //$NON-NLS-1$
                    if (provider != null)
                    {
                        value = provider.getWebLinkData(name);
                    } else
                    {
                        Object data = FormHelper.getValue(dataObj, name);
                        if (data != null)
                        {
                            value = data.toString();
                        } else
                        {
                            backupPrompt.put(name, arg.getTitle() == null ? arg.getName() : arg.getTitle());
                        }
                    }
                    valueHash.put(name, value);
                }
            }
            
            int promptCnt = webLinkDef.getPromptCount();
            if (promptCnt > 0 || backupPrompt.size() > 0)
            {
                valueHash.clear();
                promptDialog = createPromptDlg(backupPrompt);
                promptDialog.setVisible(true);
                if (!promptDialog.isCancelled())
                {
                    for (String key : textFieldHash.keySet())
                    {
                        valueHash.put(key, textFieldHash.get(key).getText());
                    }
                    
                } else
                {
                    return null;
                }
                promptDialog = null;
            }
            
            String url = webLinkDef.getBaseURLStr();
            for (String key : valueHash.keySet())
            {
                String val = valueHash.get(key);
                url = StringUtils.replace(url, "["+key+"]", (val != null ? val : "")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            url = StringUtils.replace(url, "AMP", "&amp;"); //$NON-NLS-2$
            
            return url;
        }
        
        return null;
    }
    
    /**
     * 
     */
    protected void doEdit()
    {
        boolean doSimpleDialog = true;
        if (webLinkDef != null)
        {
            
        } 
        
        if (doSimpleDialog)
        {
            // simple dialog
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
        
        boolean enbl = (webLinkDef != null || StringUtils.isNotEmpty(urlStr)) && enabled;
        
        launchBtn.setEnabled(enbl);
        if (editBtn != null)
        {
            editBtn.setEnabled(enbl);
        }
    }
    
    //--------------------------------------------------------
    //-- UIPluginable
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#initialize(java.util.Properties, boolean)
     */
    public void initialize(final Properties properties, final boolean isViewMode)
    {
        urlStr = properties.getProperty("url");
        
        String iconName = properties.getProperty("icon"); //$NON-NLS-1$
        ImageIcon icon = null;
        if (StringUtils.isNotEmpty(iconName))
        {
            icon = IconManager.getIcon(iconName, IconManager.STD_ICON_SIZE);
        }
        
        if (icon == null)
        {
            icon = IconManager.getIcon("WebLink", IconManager.STD_ICON_SIZE); //$NON-NLS-1$
        }
        launchBtn = createButton(icon);
        launchBtn.addActionListener(this);
        
        String wlName = properties.getProperty("weblink"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(wlName))
        {
            webLinkDef = WebLinkMgr.getInstance().get(wlName);
            if (webLinkDef != null)
            {
                this.setToolTipText(webLinkDef.getDesc());
            } else
            {
                launchBtn.setToolTipText(""); //$NON-NLS-1$
            }
        }
        
        PanelBuilder    pb     = new PanelBuilder(new FormLayout("p,f:p:g", "p"), this); //$NON-NLS-1$ //$NON-NLS-2$
        CellConstraints cc     = new CellConstraints();
        
        pb.add(launchBtn, cc.xy(1,1));
        
        /*if (!isViewMode)
        {
            editBtn = createButton(IconManager.getIcon("EditIcon"));
            add(editBtn, BorderLayout.EAST);
            
            editBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    doEdit();
                }
            });
        }*/
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#setCellName(java.lang.String)
     */
    public void setCellName(String cellName)
    {
        // ignore
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#setChangeListener(javax.swing.event.ChangeListener)
     */
    public void setChangeListener(ChangeListener listener)
    {
        // ignore
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#shutdown()
     */
    public void shutdown()
    {
        dataObj = null;
    }
    
    //--------------------------------------------------------
    //-- GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        if (value == null)
        {
            this.setEnabled(false);
            dataObj  = null;
            provider = null;
            
        } else
        {
            if (value instanceof FormDataObjIFace)
            {
                dataObj = (FormDataObjIFace)value;
            }
            
            if (value instanceof WebLinkDataProviderIFace)
            {
                provider = (WebLinkDataProviderIFace)value;
            }
            setEnabled(isEnabled());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return dataObj;
    }
}
