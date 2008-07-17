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

import java.awt.Component;
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
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.ui.forms.ViewFactory;
import edu.ku.brc.ui.forms.validation.DataChangeListener;
import edu.ku.brc.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.ui.forms.validation.UIValidatable;
import edu.ku.brc.ui.forms.validation.ValTextField;
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
public class WebLinkButton extends JPanel implements UIPluginable, 
                                                     GetSetValueIFace, 
                                                     ActionListener, 
                                                     UIValidatable,
                                                     DataChangeListener
{
    protected Logger log = Logger.getLogger(WebLinkButton.class);
    
    protected JButton                       launchBtn;
    protected JButton                       editBtn     = null;
    protected ValTextField                  textField        = null;
    
    protected WebLinkDef                    webLinkDef  = null;
    
    protected String                        urlStr;
    protected Properties                    initProps;
    protected Object                        dataObj;
    protected boolean                       usingThisData = true;
    protected boolean                       isTableSpecific;
    
    protected WebLinkDataProviderIFace      provider = null;
    
    protected Hashtable<String, JTextField> textFieldHash = new Hashtable<String, JTextField>();
    protected Hashtable<String, String>     valueHash     = new Hashtable<String, String>();
    
    protected CustomDialog                  promptDialog  = null;
    
    // UIPluginable
    protected String             cellName       = null;
    protected ChangeListener     changeListener = null;
    
    // UIValidatable && UIPluginable
    protected UIValidatable.ErrorType valState  = UIValidatable.ErrorType.Valid;
    protected boolean            isRequired = false;
    protected boolean            isChanged  = false;
    protected boolean            isNew      = false;

    /**
     * 
     */
    public WebLinkButton()
    {
        
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
                UIRegistry.showLocalizedError("WEBLNK_MISSING", wlName);
                launchBtn.setToolTipText(""); //$NON-NLS-1$
                return;
            }
        } else
        {
            int x = 0;
            x++;
        }
        
        isTableSpecific = StringUtils.isNotEmpty(webLinkDef.getTableName());
        
        PanelBuilder    pb     = new PanelBuilder(new FormLayout("p,"+ (isTableSpecific ? "" : "2px,p,") + "f:p:g", "p"), this); //$NON-NLS-1$ //$NON-NLS-2$
        CellConstraints cc     = new CellConstraints();

        pb.add(launchBtn, cc.xy(1,1));
        
        if (!isTableSpecific)
        {
            textField = new ValTextField(10);
            pb.add(textField, cc.xy(3,1));
            
            textField.setRequired(isRequired);
            
            DataChangeNotifier dcn = new DataChangeNotifier(null, textField, null);
            dcn.addDataChangeListener(this);
            textField.getDocument().addDocumentListener(dcn);
            
            if (isViewMode)
            {
                ViewFactory.changeTextFieldUIForDisplay(textField, false);
            }
        }
        
        
        
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
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent ae)
    {
        // Parse the format and build the URL
        String urlString;
        try
        {
            urlString = buildURL(false);
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
            UIRegistry.showLocalizedError("WEBLNK_BAD", "\n"+uri);
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
    protected String buildURL(final boolean forToolTip)
    {
        //if (StringUtils.isNotEmpty(urlStr))
        //{
        //    return urlStr;
        //}
        
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
                        
                    } else if (dataObj instanceof FormDataObjIFace)
                    {
                        Object data = FormHelper.getValue((FormDataObjIFace)dataObj, name);
                        if (data != null)
                        {
                            value = data.toString();
                        } else
                        {
                            backupPrompt.put(name, arg.getTitle() == null ? arg.getName() : arg.getTitle());
                        }
                    } else if (dataObj instanceof String && textField != null)
                    {
                        value = (String)dataObj;
                    }
                    
                    if (value != null)
                    {
                        valueHash.put(name, value);
                    }
                } else
                {
                    valueHash.put(arg.getName(), "??");
                }
            }
            
            if (!forToolTip)
            {
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
     * @see edu.ku.brc.ui.UIPluginable#setCellName(java.lang.String)
     */
    public void setCellName(String cellName)
    {
        this.cellName = cellName;
        usingThisData = StringUtils.isNotEmpty(cellName) && cellName.equals("this"); //$NON-NLS-1$ 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#setChangeListener(javax.swing.event.ChangeListener)
     */
    public void setChangeListener(ChangeListener listener)
    {
        this.changeListener = listener;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#shutdown()
     */
    public void shutdown()
    {
        dataObj = null;
        changeListener = null;
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
            dataObj = value;
            
            if (value instanceof WebLinkDataProviderIFace)
            {
                provider = (WebLinkDataProviderIFace)value;
            }
            String url = buildURL(true);
            int x = 0;
            if (StringUtils.isNotEmpty(url))
            {
                setToolTips();
            }
            setEnabled(isEnabled());
        }
        
        if (dataObj instanceof String && textField != null)
        {
            textField.setText((String)dataObj);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return dataObj;
    }

    
    //-----------------------------------------------------------
    //-- UIValidatable
    //-----------------------------------------------------------
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#cleanUp()
     */
    public void cleanUp()
    {
        changeListener = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getState()
     */
    public ErrorType getState()
    {
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    public Component getValidatableUIComp()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isChanged()
     */
    public boolean isChanged()
    {
        return isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        return valState != UIValidatable.ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isRequired()
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#reset()
     */
    public void reset()
    {
        valState = isRequired ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
        isChanged = false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    public void setAsNew(final boolean isNew)
    {
        this.isNew = isNew;    
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    public void setChanged(boolean isChanged)
    {
        this.isChanged = isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setState(edu.ku.brc.ui.forms.validation.UIValidatable.ErrorType)
     */
    public void setState(ErrorType state)
    {
        this.valState = state;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#validateState()
     */
    public ErrorType validateState()
    {
        // this validates the state
        valState = textField != null ? textField.getState() : UIValidatable.ErrorType.Valid;
        
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getReason()
     */
    public String getReason()
    {
        return null;
    }

    //--------------------------------------------------------
    // DataChangedListener Interface
    //--------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.DataChangeListener#dataChanged(java.lang.String, java.awt.Component, edu.ku.brc.ui.forms.validation.DataChangeNotifier)
     */
    public void dataChanged(String name, Component comp, DataChangeNotifier dcn)
    {
        validateState();
        
        isChanged = true;
        if (changeListener != null)
        {
            changeListener.stateChanged(null);
        }
        
        String text = textField.getText();
        if (dataObj == null && text != null)
        {
            launchBtn.setEnabled(true);
            dataObj = text;
            
        } else if (dataObj != null && text.length() == 0)
        {
            launchBtn.setEnabled(false);
            dataObj = null;
        } else
        {
            dataObj = text;
        }
        
        if (dataObj != null)
        {
            setToolTips();
        }
    }
    
    /**
     * Creates a tool for btn and the textfield.
     */
    protected void setToolTips()
    {
        String url = buildURL(true);
        launchBtn.setToolTipText(url);
        if (textField != null)
        {
            textField.setToolTipText(url);
        }  
    }
    
}
