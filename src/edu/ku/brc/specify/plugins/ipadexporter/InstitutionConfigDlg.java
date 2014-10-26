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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.prefs.BackingStoreException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.util.FileUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.specify.permission.PermissionService;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.ImageDisplay;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;


/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 26, 2012
 *
 */
public class InstitutionConfigDlg extends CustomDialog
{
    private final String kCloudURLStr = iPadRepositoryHelper.baseURLStr;
    
    private IPadCloudIFace cloudHelper;
    
    private Institution inst;
    private Collection  collection;
    
    // Institution
    private JTextField  instTextField;
    private int         instNameLen;
    private String      instGUID;
    
    // Institution on Cloud
    private Integer cloudInstId = null;
    
    // Curator
    private JTextField    crTextFld;
    private JTextField    cmTextFld;
    private String        curatorPref;
    private String        colMgrPref;
    
    // Cloud URL
    private JTextField    cloudURLTextFld;
    private String        cloudURLPref;

    // URL Data Members
    protected static final String ATTMGR       = "attmgr";
    protected static final String DIRECT       = "direct";
    
    protected static final String IPAD_REMOTE_IMAGE_URL      = "IPAD_REMOTE_IMAGE_URL";
    protected static final String IPAD_REMOTE_IMAGE_URL_TYPE = "IPAD_REMOTE_IMAGE_URL_TYPE";
    protected static final String IPAD_PICTURE_LOCATION      = "IPAD_PICTURE_LOCATION";
    
    
    private JRadioButton  useAttchmentMgrRB;
    private JRadioButton  useDirectUrlRB;
    private ValTextField  urlTextField;
    private JLabel        label;
    private JLabel        statusLbl;
    
    private String        useAttachTitle;  
    private String        useDirectTitle; 
    
    private String        cachedAttMgrURL = null;
    private String        cachedDirectURL = null;
    
    // Picture Selection
    private ImageDisplay   imageView;
    
    /**
     * @throws HeadlessException
     */
    public InstitutionConfigDlg(final IPadCloudIFace cloudHelper, final Integer cloudInstId) throws HeadlessException
    {
        super((Frame)getTopWindow(), "", true, OKCANCEL, null);
        
        collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        inst       = AppContextMgr.getInstance().getClassObject(Institution.class);
        instGUID   = inst.getGuid();
        
        curatorPref     = "IPAD_CURATOR_NAME_" + collection.getId();
        colMgrPref      = "IPAD_COLMGR_NAME_"  + collection.getId();
        cloudURLPref    = "IPAD_CLOUD_URL_"    + collection.getId();
        
        this.cloudHelper = cloudHelper;
        this.cloudInstId = cloudInstId;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        loadAndPushResourceBundle(iPadDBExporterPlugin.RESOURCE_NAME);
        
        setTitle(getResourceString("IPAD_CONFIG_TITLE"));
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "p,2px,p,10px, p,2px,p,10px, p,2px,p,10px, p,2px,p,10px, p,2px,p,10px"));

        int y = 1;
        pb.addSeparator(getResourceString("INST_INFO"), cc.xy(1, y)); y+= 2;
        pb.add(createInstitutionPanel(), cc.xy(1, y)); y+= 2;
        
        pb.addSeparator(getResourceString("CURATOR_NM"), cc.xy(1, y)); y+= 2;
        pb.add(createCuratorPanel(), cc.xy(1, y)); y+= 2;
        
        pb.addSeparator(getResourceString("CLOUD_URL"), cc.xy(1, y)); y+= 2;
        pb.add(createCloudURLPanel(), cc.xy(1, y)); y+= 2;
        
        pb.addSeparator(getResourceString("IMAGE_SRC_TITLE"), cc.xy(1, y)); y+= 2;
        pb.add(createURLPanel(), cc.xy(1, y)); y+= 2;
        
        pb.addSeparator(getResourceString("PICTURE_TITLE"), cc.xy(1, y));  y+= 2;
        pb.add(createPicturePanel(), cc.xy(1, y)); y+= 2;
        
        popResourceBundle();
        
        updateOKBtn();
        
        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * @return
     */
    private JPanel createCloudURLPanel()
    {
       AppPreferences  remotePrefs = AppPreferences.getRemote();
        
        String cloudURLStr = remotePrefs.get(cloudURLPref, kCloudURLStr);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,4px,p"));
        
        cloudURLTextFld = UIHelper.createTextField(cloudURLStr);
        
        pb.add(UIHelper.createI18NFormLabel("CLOUD_URL_TITLE"), cc.xy(1, 1));
        pb.add(cloudURLTextFld, cc.xy(3, 1));
        
        return pb.getPanel();
    }
    
    /**
     * @return
     */
    private JPanel createPicturePanel()
    {
        AppPreferences  remotePrefs = AppPreferences.getRemote();
        
        String          picturelocation = remotePrefs.get(getRemotePicturePrefName(), "");
        JTextArea       explainTextArea = UIHelper.createTextArea(); 
        
        explainTextArea.setEditable(false);
        explainTextArea.setText(getResourceString("PICTURE_EXPLAIN"));
        explainTextArea.setOpaque(false);
        
        final String thumbnailPicMsg = getResourceString("THUMBNAIL_PIC");
        imageView = new ImageDisplay(200, 200, true, true);
        imageView.setThumbnailMsg(thumbnailPicMsg);

        if (StringUtils.isNotEmpty(picturelocation))
        {
            String fullPath = UIRegistry.getAppDataDir() + File.separator + picturelocation;
            File   urlFile  = new File(fullPath);
            if (urlFile.exists())
            {
                imageView.setValue(urlFile.toURI().toASCIIString(), null);
            }
        }
        
        JButton clearBtn = UIHelper.createI18NButton("CLEAR_PICTURE");
        clearBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                imageView.setValue(null, null);
                imageView.setThumbnailMsg(thumbnailPicMsg);
                imageView.repaint();
            }
        });
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder epb = new PanelBuilder(new FormLayout("f:p:g", "top:p:g,4px,p"));
        epb.add(explainTextArea, cc.xy(1,1));
        
        PanelBuilder bpb = new PanelBuilder(new FormLayout("p", "f:p:g,p"));
        bpb.add(clearBtn, cc.xy(1,2));
        epb.add(bpb.getPanel(), cc.xy(1, 3));
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,8px,f:p:g", "f:p:g"));
        pb.add(imageView, cc.xy(1, 1));
        pb.add(epb.getPanel(), cc.xy(3, 1));

        return pb.getPanel();
    }
    
    /**
     * @return
     */
    private JPanel createInstitutionPanel()
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,2px,p,8px"));
        DBTableInfo     ti = DBTableIdMgr.getInstance().getInfoById(Institution.getClassTableId());
        
        DBFieldInfo fi = ti.getFieldByName("name");
        JLabel nmLabel = UIHelper.createFormLabel(fi.getTitle());
        instNameLen    = fi.getLength();
        instTextField  = new ValTextField();
        
        int y = 1;
        pb.add(nmLabel,       cc.xy(1, y)); 
        pb.add(instTextField, cc.xy(3, y)); y+= 2;
        
        KeyAdapter ka = new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                updateOKBtn();
            }
        };
        instTextField.addKeyListener(ka);
        
        String title = inst != null ? inst.getName() : "";
        instTextField.setText(title);
        return pb.getPanel();
    }
    
    /**
     * @return
     */
    private JPanel createCuratorPanel()
    {
        AppPreferences  remotePrefs = AppPreferences.getRemote();
        
        String          curatorName = remotePrefs.get(curatorPref, "");
        String          colMgrName  = remotePrefs.get(colMgrPref, "");
        
        CellConstraints cc          = new CellConstraints();
        PanelBuilder    pb          = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,4px,p"));
        
        crTextFld = UIHelper.createTextField(curatorName);
        cmTextFld = UIHelper.createTextField(colMgrName);
        
        pb.add(UIHelper.createI18NFormLabel("Curator"), cc.xy(1, 1));
        pb.add(crTextFld, cc.xy(3, 1));
        
        pb.add(UIHelper.createI18NFormLabel("COLMGR"), cc.xy(1, 3));
        pb.add(cmTextFld, cc.xy(3, 3));
        
        KeyAdapter ka = new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                updateOKBtn();
            }
        };
        crTextFld.addKeyListener(ka);
        cmTextFld.addKeyListener(ka);
        return pb.getPanel();
    }
    
    /**
     * Returns the URL of the iPad Cloud Server. 
     */
    public String getCloudURL()
    {
        AppPreferences  remotePrefs = AppPreferences.getRemote();
        return remotePrefs.get(cloudURLPref, kCloudURLStr);
    }
 
    /**
     * @return
     */
    public boolean isInstOK()
    {
        AppPreferences  remotePrefs = AppPreferences.getRemote();
        //String          picturelocation = remotePrefs.get(getRemotePicturePrefName(), "");
        String          curatorName = remotePrefs.get(curatorPref, "");
        String          colMgrName  = remotePrefs.get(colMgrPref, "");
        String          cloudURLStr = remotePrefs.get(cloudURLPref, kCloudURLStr);

        return cloudInstId != null && //StringUtils.isNotEmpty(picturelocation) && 
                                      StringUtils.isNotEmpty(curatorName) && 
                                      StringUtils.isNotEmpty(colMgrName) && 
                                      StringUtils.isNotEmpty(cloudURLStr);
    }
    
    /**
     * @return the instId
     */
    public Integer getInstId()
    {
        return cloudInstId;
    }
    
    /**
     * @param title
     * @param uri
     * @param code
     * @return
     */
    private boolean saveToLocalDB(final String title)
    {
        boolean           isOK  = false;
        Connection        conn  = DBConnection.getInstance().getConnection();
        PreparedStatement pStmt = null;
        try
        {
            String sql = "UPDATE institution SET Name=?, TimestampModified=? WHERE InstitutionID=?";
            pStmt = conn.prepareStatement(sql);
            pStmt.setString(1, title);
            pStmt.setTimestamp(2, new Timestamp(Calendar.getInstance().getTimeInMillis()));
            pStmt.setInt(3, inst.getId());

            isOK =  pStmt.executeUpdate() == 1;
            pStmt.close();
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                inst = session.get(Institution.class, inst.getId());
                AppContextMgr.getInstance().setClassObject(Institution.class, inst);
            }
            catch (Exception e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionService.class, e);
                e.printStackTrace();
            }
            finally
            {
                if (session != null) session.close();
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return isOK;
    }
    
    /**
     * 
     */
    private void updateOKBtn()
    {
        boolean isInstOK     = !instTextField.getText().isEmpty();
        boolean isCurratorOK = !crTextFld.getText().isEmpty() && !cmTextFld.getText().isEmpty();
        boolean isURLOK      = isImageURLValidInput(urlTextField.getText());
        
        getOkBtn().setEnabled(isCurratorOK && isURLOK && isInstOK);
    }
    
    private JPanel createURLPanel()
    {
        //setTitle(getResourceString("IMAGE_SRC_TITLE"));

        useAttachTitle = getResourceString("ATTCH_MGR");   
        useDirectTitle = getResourceString("DIR_URL");  

        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,4px,p,4px,p,4px,p,4px,p,4px"));
        
        label             = createLabel(useAttachTitle+":");
        urlTextField         = new ValTextField(40);
        statusLbl         = createLabel("");
        useAttchmentMgrRB = createI18NRadioButton("USE_ATT_MGR");
        useDirectUrlRB    = createI18NRadioButton("USE_DIR_URL");
        
        int y = 1;
        pb.add(createI18NLabel("HOW_IMG_ACCESS"), cc.xy(1, y)); y+= 2;
        pb.add(useAttchmentMgrRB,            cc.xyw(1, y, 4)); y+= 2;
        pb.add(useDirectUrlRB,               cc.xyw(1, y, 4)); y+= 2;
        
        PanelBuilder    pbInner = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p"));
        pbInner.add(label,     cc.xy(1, 1)); 
        pbInner.add(urlTextField, cc.xy(3, 1));
        
        pb.add(pbInner.getPanel(), cc.xyw(1, y, 4)); y+= 2;
        pb.add(statusLbl,          cc.xyw(1, y, 4)); y+= 2;
        
        pb.setDefaultDialogBorder();
        
        KeyAdapter ka = new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                updateOKBtn();
            }
        };
        urlTextField.addKeyListener(ka);
        
        super.createUI();
        
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
        
        // Set Data into form or initialize it
        String typeStr     = AppPreferences.getRemote().get(getRemoteImageURLTypePrefName(), null);
        String imgURLPath  = AppPreferences.getRemote().get(getRemoteImageURLPrefName(), null);
        
        //System.out.println(String.format("%s=%s", getRemoteImageURLTypePrefName(), typeStr));
        //System.out.println(String.format("%s=%s", getRemoteImageURLPrefName(), imgURLPath));

        if (isNotEmpty(typeStr) && isNotEmpty(imgURLPath))
        {
            urlTextField.setText(imgURLPath);
            
            boolean isAttachMgr = isNotEmpty(typeStr) && typeStr.equals(ATTMGR);
            useAttchmentMgrRB.setSelected(isAttachMgr);
            useDirectUrlRB.setSelected(!isAttachMgr);
        } else
        {
            fillWithDefaultAttMgr();
        }

        return pb.getPanel();
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
        urlTextField.setText(cachedAttMgrURL);
    }
    
    /**
     * 
     */
    private void radBtnSelected()
    {
        boolean isUsingAttMgr = useAttchmentMgrRB.isSelected();
        if (isUsingAttMgr)
        {
            cachedDirectURL = urlTextField.getText();
            fillWithDefaultAttMgr();
        } else
        {
            cachedAttMgrURL = urlTextField.getText();
            urlTextField.setText(cachedDirectURL);
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
     * @param imageURL
     * @return
     */
    private boolean isImageURLValidInput(final String imageURL)
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
        return urlTextField.getText();
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

    /**
     * @return return Collection unique Prefname for storing Image URL
     */
    protected String getRemotePicturePrefName()
    {
        return getPrefName(IPAD_PICTURE_LOCATION);
    }

    /**
     * @return the institution guid
     */
    public String getInstGuid()
    {
        return instGUID;
    }
    
    public static BufferedImage toBufferedImage(final Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
    
    private String copyInstImage()
    {
        // These are form the iPad app
        final int kWidth  = 770;
        final int kHeight = 435;
        
        String filePath = (String)imageView.getValue();
        if (StringUtils.isNotEmpty(filePath))
        {
            try
            {
                File srcFile;
                if (filePath.startsWith("file:"))
                {
                    URL url = new URL(filePath);
                    srcFile = new File(url.toURI());
                } else
                {
                    srcFile  = new File(filePath);
                }
                String baseName = FilenameUtils.getBaseName(filePath);
                String fileName = baseName + ".png";
                File   destFile = new File(UIRegistry.getAppDataDir() + File.separator + fileName);
                
                if (!srcFile.getAbsolutePath().equals(destFile.getAbsolutePath()))
                {
                    ImageIcon img = new ImageIcon(srcFile.getAbsolutePath());
                    if (img.getIconWidth() > kWidth || img.getIconHeight() > kHeight)
                    {
                        Image image = GraphicsUtils.getScaledImage(img, kWidth, kHeight, true);
                        try
                        {
                            ImageIO.write(toBufferedImage(image), "PNG", destFile); //$NON-NLS-1$
                        } catch (Exception ex){}
                    } else
                    {
                        FileUtils.copyFile(srcFile, destFile);
                    }
                }
                return fileName;
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        String nmStr  = instTextField.getText();
        if (nmStr.length() > instNameLen)
        {
            nmStr = nmStr.substring(0, instNameLen);
        }
        
        if (saveToLocalDB(nmStr))
        {
            statusLbl.setText("");
            
            boolean isOK           = false;
            if (!iPadDBExporter.IS_TESTING) // ZZZ  
            {            
                isOK = cloudHelper.updateInstitution(cloudInstId, nmStr, inst.getUri());
            } else 
            {
                isOK = true;
            }
            
            if (isOK)
            {
                //System.out.println(String.format("%s=%s", getRemoteImageURLTypePrefName(), useAttchmentMgrRB.isSelected() ? ATTMGR : DIRECT));
                //System.out.println(String.format("%s=%s", getRemoteImageURLPrefName(), textfield.getText()));
                
                AppPreferences remotePrefs = AppPreferences.getRemote();
                remotePrefs.put(getRemoteImageURLTypePrefName(), useAttchmentMgrRB.isSelected() ? ATTMGR : DIRECT);
                remotePrefs.put(getRemoteImageURLPrefName(),     urlTextField.getText());
                
                remotePrefs.put(curatorPref, crTextFld.getText());
                remotePrefs.put(colMgrPref, cmTextFld.getText());
                remotePrefs.put(cloudURLPref, cloudURLTextFld.getText());

                String fileName = copyInstImage();
                if (fileName != null)
                {
                    remotePrefs.put(getRemotePicturePrefName(), fileName);
                }
                
                try 
                {
                    remotePrefs.flush();
                } catch (BackingStoreException e1){}
                
                statusLbl.setText("");
                
                super.okButtonPressed();
                
                return;
  
            } else
            {
                statusLbl.setText("Unable to save the Institution Information.");
            }
        } else
        {
            statusLbl.setText("Unable to save the Institution Information to local database.");
        }
        okBtn.setEnabled(false);

    }
}
