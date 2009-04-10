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
package edu.ku.brc.af.ui.weblink;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.createFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.validation.DataChangeListener;
import edu.ku.brc.af.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.specify.plugins.UIPluginBase;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;
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
public class WebLinkButton extends UIPluginBase implements ActionListener, 
                                                           UIValidatable,
                                                           DataChangeListener
{
    protected Logger log = Logger.getLogger(WebLinkButton.class);
    
    protected JButton                       launchBtn;
    protected JButton                       editBtn     = null;
    protected ValTextField                  textField   = null;
    protected FormViewObj                   fvo         = null;
    
    protected WebLinkDef                    webLinkDef  = null;
    
    protected String                        urlStr;
    protected boolean                       usingThisData = true;
    protected boolean                       isTableSpecific;
    
    protected WebLinkDataProviderIFace      provider = null;
    
    protected Hashtable<String, JTextField> textFieldHash = new Hashtable<String, JTextField>();
    protected Hashtable<String, String>     valueHash     = new Hashtable<String, String>();
    
    protected CustomDialog                  promptDialog  = null;
    
    // UIPluginable
    protected String                        cellName       = null;
    protected ChangeListener                changeListener = null;
    
    // UIValidatable && UIPluginable
    protected UIValidatable.ErrorType       valState  = UIValidatable.ErrorType.Valid;
    protected boolean                       isRequired = false;
    protected boolean                       isChanged  = false;
    protected boolean                       isNew      = false;
    
    protected String                        watchId    = null;
    protected boolean                       isWatchSetUp = false;

    /**
     * 
     */
    public WebLinkButton()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#initialize(java.util.Properties, boolean)
     */
    @Override
    public void initialize(final Properties propertiesArg, final boolean isViewModeArg)
    {
        super.initialize(propertiesArg, isViewModeArg);
        
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
            }
        }
        
        isTableSpecific = webLinkDef != null ? StringUtils.isNotEmpty(webLinkDef.getTableName()) : false;
        
        PanelBuilder    pb     = new PanelBuilder(new FormLayout("p,"+ (isTableSpecific ? "" : "2px,p,") + "f:p:g", "p"), this); //$NON-NLS-1$ //$NON-NLS-2$
        CellConstraints cc     = new CellConstraints();

        pb.add(launchBtn, cc.xy(1,1));
        
        if (!isTableSpecific && !usingThisData)
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
        } else
        {
            watchId = properties.getProperty("watch");
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
     * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        throw new NotImplementedException("isNotEmpty not implement!");
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            log.error("Failed to build URL", e1); //$NON-NLS-1$
            return;
        }
        
        if (StringUtils.isEmpty(urlString))
        {
            // an error message should have already been put on the status bar
            // just exit
            return;
        }
        
        // Convert to a URI
        URI uri;
        try
        {
            uri = new URL(urlString).toURI();
        }
        catch (MalformedURLException e)
        {
            log.error("Bad URL syntax: " + urlString, e); //$NON-NLS-1$
            return;
            
        } catch (URISyntaxException e)
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
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
                
                DocumentAdaptor dla = new DocumentAdaptor()
                {
                    @Override
                    protected void changed(DocumentEvent e)
                    {
                        super.changed(e);
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
                    
                };
                
                int y = 1;
                for (WebLinkDefArg arg : webLinkDef.getArgs())
                {
                    if (arg.isPrompt())
                    {
                        JTextField txtField = createTextField(15);
                        txtField.getDocument().addDocumentListener(dla);
                        textFieldHash.put(arg.getName(), txtField);
                        String label = arg.getTitle();
                        if (StringUtils.isEmpty(label))
                        {
                            label = arg.getName();
                        }
                        pb.add(createFormLabel(label), cc.xy(1, y));
                        pb.add(txtField, cc.xy(3, y));
                        y += 2;
                    }
                }
                
                for (String name : backupHash.keySet())
                {
                    JTextField txtField = createTextField(15);
                    txtField.getDocument().addDocumentListener(dla);
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
        if (webLinkDef != null)
        {
            // Start by getting the data needed to build the URL
            // so first see if we need to prompt for data.
            valueHash.clear();

            int possibleValues = 0;
            int numValues      = 0;
            
            Vector<String> missingList = null; // List of missing args
            
            Hashtable<String, String> backupPrompt = new Hashtable<String, String>();
            for (WebLinkDefArg arg : webLinkDef.getArgs())
            {
                if (!arg.isPrompt())
                {
                    possibleValues++;
                    
                    String name  = arg.getName();
                    String value = ""; //$NON-NLS-1$
                    if (provider != null)
                    {
                        value = provider.getWebLinkData(name);
                        
                    } else if (dataObj instanceof FormDataObjIFace)
                    {
                        Object dataVal = FormHelper.getValue((FormDataObjIFace)dataObj, name);
                        if (dataVal != null)
                        {
                            value = dataVal.toString();
                        } else
                        {
                            backupPrompt.put(name, arg.getTitle() == null ? arg.getName() : arg.getTitle());
                        }
                    } else if (dataObj instanceof String && textField != null)
                    {
                        value = (String)dataObj;
                    }
                    
                    String textFieldValue = null;
                    if (textField != null)
                    {
                        textFieldValue = textField.getText();
                    }
                    
                    if (value != null)
                    {
                        if (StringUtils.isNotEmpty(textFieldValue) && !textFieldValue.equals(value))
                        {
                            value = textFieldValue;
                        }
                        valueHash.put(name, value);
                        numValues++;
                        
                    } else if (StringUtils.isNotEmpty(textFieldValue))
                    {
                        valueHash.put(name, textFieldValue);
                        numValues++;
                    } else
                    {
                        if (missingList == null)
                        {
                            missingList = new Vector<String>();
                        }
                        missingList.add(name);
                    }
                } else
                {
                    valueHash.put(arg.getName(), "??");
                }
            }
            
            if (possibleValues != numValues)
            {
                if (!forToolTip)
                {
                    StringBuilder sb = new StringBuilder();
                    for (String name : missingList)
                    {
                        sb.append(name);
                        sb.append("\n");
                    }
                    UIRegistry.showLocalizedError("WEBLNK_MIS_FLD", "\n"+sb.toString());
                }
                return null;
            }
            
            if (!forToolTip)
            {
                int promptCnt = webLinkDef.getPromptCount();
                if (promptCnt > 0 || backupPrompt.size() > 0)
                {
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
            
            byte[] chars = webLinkDef.getBaseURLStr().getBytes();
            int i = 0;
            for (byte b : webLinkDef.getBaseURLStr().getBytes())
            {
                if (b == '<' || b == '>')
                {
                    chars[i] = '\'';
                }
                i++;
            }
            String url = new String(chars);
            for (String key : valueHash.keySet())
            {
                String val = valueHash.get(key);
                if (val.equals("this"))
                {
                    val = valueHash.get("this");
                }
                try
                {
                    val = URLEncoder.encode(val, "UTF-8");
                } catch (Exception ex) {}
                
                url = StringUtils.replace(url, "'"+key+"'", (val != null ? val : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                //System.out.println("|"+key+"|"+url);
            }
            
            url = StringUtils.replace(url, "AMP", "&"); //$NON-NLS-2$
            return url;
        }
        
        if (textField != null)
        {
            return textField.getText();
        }
        
        return null;
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
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setCellName(java.lang.String)
     */
    @Override
    public void setCellName(String cellName)
    {
        super.setCellName(cellName);
        usingThisData = StringUtils.isNotEmpty(cellName) && cellName.equals("this"); //$NON-NLS-1$ 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setViewable(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void setParent(final FormViewObj parent)
    {
        super.setParent(parent);
        
        if (fvo != null && isTableSpecific && StringUtils.isNotEmpty(watchId))
        {
            Component comp = fvo.getCompById(watchId);
            if (comp instanceof ValTextField)
            {
                textField = (ValTextField)comp;
                DataChangeNotifier dcn = new DataChangeNotifier(null, textField, null);
                dcn.addDataChangeListener(this);
                textField.getDocument().addDocumentListener(dcn);
            }
        }
    }

    //--------------------------------------------------------
    //-- GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        super.setValue(value, defaultValue);
        
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

    //-----------------------------------------------------------
    //-- UIValidatable
    //-----------------------------------------------------------
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#cleanUp()
     */
    public void cleanUp()
    {
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
    public void dataChanged(final String name, final Component comp, final DataChangeNotifier dcn)
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
