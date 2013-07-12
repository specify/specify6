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
package edu.ku.brc.specify.plugins.ipadexporter;

import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIHelper.createI18NRadioButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;
import static edu.ku.brc.ui.UIRegistry.popResourceBundle;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.util.AttachmentUtils;


/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 26, 2012
 *
 */
public class ImageSetupDlg extends CustomDialog
{
    protected static final String ATTMGR       = "attmgr";
    protected static final String DIRECT       = "direct";
    
    protected static final String IPAD_REMOTE_IMAGE_URL      = "IPAD_REMOTE_IMAGE_URL";
    protected static final String IPAD_REMOTE_IMAGE_URL_TYPE = "IPAD_REMOTE_IMAGE_URL_TYPE";
    
    
    private JRadioButton  useAttchmentMgrRB;
    private JRadioButton  useDirectUrlRB;
    private ValTextField  textfield;
    private JLabel        label;
    private JLabel        statusLbl;
    
    private String        useAttachTitle;  
    private String        useDirectTitle; 
    
    private String        cachedAttMgrURL = null;
    private String        cachedDirectURL = null;
    
    /**
     * @param cloudHelper
     * @throws HeadlessException
     */
    public ImageSetupDlg() throws HeadlessException
    {
        super((Frame)getTopWindow(), "", true, OKCANCEL, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        loadAndPushResourceBundle(iPadDBExporterPlugin.RES_NAME);
        
        setTitle(getResourceString("IMAGE_SRC_TITLE"));
        useAttachTitle = getResourceString("ATTCH_MGR");   
        useDirectTitle = getResourceString("DIR_URL");  

        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,4px,p,4px,p,4px,p,4px,p,4px"));
        
        label             = createLabel(useAttachTitle+":");
        textfield         = new ValTextField(40);
        statusLbl         = createLabel("");
        useAttchmentMgrRB = createI18NRadioButton("USE_ATT_MGR");
        useDirectUrlRB    = createI18NRadioButton("USE_DIR_URL");
        
        contentPanel = pb.getPanel();

        int y = 1;
        pb.add(createI18NLabel("HOW_IMG_ACCESS"), cc.xy(1, y)); y+= 2;
        pb.add(useAttchmentMgrRB,            cc.xyw(1, y, 4)); y+= 2;
        pb.add(useDirectUrlRB,               cc.xyw(1, y, 4)); y+= 2;
        
        PanelBuilder    pbInner = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p"));
        pbInner.add(label,     cc.xy(1, 1)); 
        pbInner.add(textfield, cc.xy(3, 1));
        
        pb.add(pbInner.getPanel(), cc.xyw(1, y, 4)); y+= 2;
        pb.add(statusLbl,          cc.xyw(1, y, 4)); y+= 2;
        
        pb.setDefaultDialogBorder();
        
        KeyAdapter ka = new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                okBtn.setEnabled(isValidInput(textfield.getText()));
            }
        };
        textfield.addKeyListener(ka);
        
        super.createUI();
        
        okBtn.setEnabled(isValidInput(textfield.getText()));
        
        ButtonGroup group = new ButtonGroup();
        group.add(useAttchmentMgrRB);
        group.add(useDirectUrlRB);
        
        ActionListener al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                radBtnSelected();
            }
        };
        
        useAttchmentMgrRB.addActionListener(al);
        useDirectUrlRB.addActionListener(al);
        
        popResourceBundle();
        
        // Set Data into form or initialize it
        String typeStr     = AppPreferences.getRemote().get(getRemoteImageURLTypePrefName(), null);
        String imgURLPath  = AppPreferences.getRemote().get(getRemoteImageURLPrefName(), null);
        if (isNotEmpty(typeStr) && isNotEmpty(imgURLPath))
        {
            textfield.setText(imgURLPath);
            
            boolean isAttachMgr = isNotEmpty(typeStr) && typeStr.equals(ATTMGR);
            useAttchmentMgrRB.setSelected(isAttachMgr);
            useDirectUrlRB.setSelected(!isAttachMgr);
        } else
        {
            fillWithDefaultAttMgr();
        }

    }
    
    private void fillWithDefaultAttMgr()
    {
        // getImageAttachmentURL()
        
        AppPreferences global                  = AppPreferences.getRemote();
        boolean        isUsingGlobalAttchPrefs = global.getBoolean("USE_GLOBAL_PREFS", false);
        if (isUsingGlobalAttchPrefs)
        {
            if (isEmpty(cachedAttMgrURL) && 
                !global.getBoolean("ATTACHMENT_USE_PATH", false))
            {
                cachedAttMgrURL = AttachmentUtils.getAttachmentManager().getImageAttachmentURL();
                if (isNotEmpty(cachedAttMgrURL))
                {
                    useAttchmentMgrRB.setSelected(true);
                } else
                {
                    useDirectUrlRB.setSelected(true);
                }
                radBtnSelected();
            }
        }
        textfield.setText(cachedAttMgrURL);
    }
    
    /**
     * 
     */
    private void radBtnSelected()
    {
        boolean isUsingAttMgr = useAttchmentMgrRB.isSelected();
        if (isUsingAttMgr)
        {
            cachedDirectURL = textfield.getText();
            fillWithDefaultAttMgr();
        } else
        {
            cachedAttMgrURL = textfield.getText();
            textfield.setText(cachedDirectURL);
        }
        
        label.setText((isUsingAttMgr ? useAttachTitle : useDirectTitle)+":");
    }
    
    /**
     * @param uri
     * @return
     */
//    private boolean doesWebSiteExists(final String uri)
//    {
//        try
//        {
//            HTTPGetter getter = new HTTPGetter();
//            getter.setThrowingErrors(false);
//            byte[] bytes = getter.doHTTPRequest(uri);
//            return bytes != null && getter.getStatus() == ErrorCode.NoError;
//        } catch (Exception ex) {}
//        return false;
//    }
    
    /**
     * @param title
     * @param webSite
     * @return
     */
    private boolean isValidInput(final String imageURL)
    {
        if (useDirectUrlRB.isSelected())
        {
            if (isNotEmpty(imageURL))
            {
                statusLbl.setText("");
                if (!isValidWebAddr(imageURL))
                {
                    statusLbl.setText("Must be valid website URL.");
                    return false;
                }
                return true;
            }
            return false;
        }
        return true;
    }
    
    /**
     * @return the instUriTF
     */
    public String getImageURL()
    {
        return textfield.getText();
    }

    /**
     * @param uri
     * @return
     */
    private static boolean isValidWebAddr(final String uri)
    {
        final String prefix = "http://";
        String uriStr = uri;
        if (!uriStr.startsWith(prefix))
        {
            uriStr = prefix + uri;
        }
        String pattern = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        		//"^((http[s]?|ftp):\\/)?\\/?([^:\\/\\s]+)((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$";//"\\b(http)://[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|]";
        try
        {
            Pattern patt = Pattern.compile(pattern);
            Matcher matcher = patt.matcher(uriStr);
            System.out.println("URL: "+ matcher.matches()+"  "+uriStr);
            return matcher.matches();
        } catch (RuntimeException e)
        {
        }
        return false;
    }

    /**
     * @param key
     * @return
     */
    private String getPrefName(final String key)
    {
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        return key + "_" + collection.getId();
    }

    /**
     * @return
     */
    protected String getRemoteImageURLPrefName()
    {
        return getPrefName(IPAD_REMOTE_IMAGE_URL);
    }
    

    /**
     * @return return Collection unique Prefname for storing Image URL
     */
    protected String getRemoteImageURLTypePrefName()
    {
        return getPrefName(IPAD_REMOTE_IMAGE_URL_TYPE);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        if (!textfield.getText().isEmpty())
        {
            System.out.println(String.format("%s=%s", getRemoteImageURLTypePrefName(), useAttchmentMgrRB.isSelected() ? ATTMGR : DIRECT));
            System.out.println(String.format("%s=%s", getRemoteImageURLPrefName(), textfield.getText()));
            AppPreferences.getGlobalPrefs().put(getRemoteImageURLTypePrefName(), useAttchmentMgrRB.isSelected() ? ATTMGR : DIRECT);
            AppPreferences.getGlobalPrefs().put(getRemoteImageURLPrefName(),     textfield.getText());
            statusLbl.setText("");
            super.okButtonPressed();   
         }
    }
}
