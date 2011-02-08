/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.morphbank.mbsvc3.xml.Credentials;
import net.morphbank.mbsvc3.xml.Request;
import net.morphbank.mbsvc3.xml.XmlUtils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.NotImplementedException;
import org.dom4j.Element;
import org.dom4j.Node;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttachmentImageAttribute;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttachment;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;
import edu.ku.brc.specify.plugins.UIPluginBase;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;

/**
 * @author timo
 *
 */
@SuppressWarnings("serial")
public class MorphBankPanel extends UIPluginBase
{

	protected static int kDetailsBtn = 0;
	protected static int kViewMBBtn=1;
	protected static int kPutBtn=2;
	protected static int kGetBtn=3;
	protected static int kNotImageStatus=4;
	protected static int kNotMorphBankedStatus=5;
	protected static int kUpdatesNeededStatus=6;
	protected static int kUpToDateStatus=7;
	protected static int kMorphBankID=8;
	protected static int kURLDisplayError=9;
	protected static int kDetailsTitle=10;
	protected static int kSendingToMorphbank=11;
	protected static int kMoreThanOneImageObjectCreated=12;
	protected static int kNoImageObjectCreated=13;
	protected static int kSuccessfulImageInsert=14;
	protected static int kImageInsertFailed=15;
	protected static int kImageFileUploadFailed=16;
	protected static int kDetailBtnHint=17;
	protected static int kviewMBBtnHint=18;
	protected static int kPutBtnHint=19;
	protected static int kGetBtnHint=20;
	protected static int kSettingsIncompleteHint=21;
	protected static int textKeys = 22;
	
	protected String[] texts;
	
	protected JButton viewMBBtn; //opens web browser 
	protected JButton putBtn; //inserts, updates sp data to mb
	protected JButton getBtn; //gets annotations etc from mb
	protected JButton detailsBtn; //view details - last update, ..
	//protected JLabel morphBankIdLbl;
	protected JLabel morphBankStatusLbl;

	protected CollectionObject colObj = null;
	protected Attachment attachment = null;
	protected ObjectAttachmentIFace<?> attacher = null;
	protected AttachmentImageAttribute imageAttribute = null;
	protected boolean isImage = false;
	protected AtomicBoolean isMorphBanked = new AtomicBoolean(false);
	protected String conceptMapping = null;
	protected Integer conceptMappingId = null;
	protected Integer ownerId = null;
	protected Integer groupId = null;
	protected String baseURL = null;
	protected String imageUploadURL = null;
	protected boolean settingsComplete = false;
	
	public MorphBankPanel()
	{
        super();
		loadAndPushResourceBundle("specify_plugins");
		String[] keys = {"MorphBankPanel.DetailsBtn",
				"MorphBankPanel.ViewMBBtn",
				"MorphBankPanel.PutBtn",
				"MorphBankPanel.GetBtn",
				"MorphBankPanel.NotImageStatus",
				"MorphBankPanel.NotMorphBankedStatus",
				"MorphBankPanel.UpdatesNeededStatus",
				"MorphBankPanel.UpToDateStatus",
				"MorphBankPanel.MorphBankID",
				"MorphBankPanel.URLDisplayError",
				"MorphBankPanel.DetailsTitle",
				"MorphBankPanel.SendingToMorphbank",
				"MorphBankPanel.MoreThanOneImageObjectCreated",
				"MorphBankPanel.NoImageObjectCreated",
				"MorphBankPanel.SuccessfulImageInsert",
				"MorphBankPanel.ImageInsertFailed",
				"MorphBankPanel.ImageFileUploadFailed",
				"MorphBankPanel.DetailBtnHint",
				"MorphBankPanel.ViewMBBtnHint",
				"MorphBankPanel.PutBtnHint",
				"MorphBankPanel.GetBtnHint",
				"MorphBankPanel.SettingsIncompleteBtnHint"
		};

		texts = new String[textKeys];
		for (int k = 0; k < textKeys; k++)
		{
			texts[k] = UIRegistry.getResourceString(keys[k]);
		}
		UIRegistry.popResourceBundle();
		
		AppPreferences prefs = AppPreferences.getRemote();
		String dwcName = prefs.get("morphbank.dwcmapping", null);
		if (dwcName != null)
		{
			conceptMappingId = BasicSQLUtils.querySingleObj("select SpExportSchemaMappingID from spexportschemamapping where MappingName = '" + dwcName + "'");
		}
		ownerId = prefs.getInt("morphbank.userid", null);
		groupId = prefs.getInt("morphbank.groupid", null);
		baseURL = prefs.get("morphbank.baseurl", null);
		imageUploadURL = prefs.get("morphbank.imageposturl", null);
		if (baseURL != null)
		{
			MorphBankTest.MORPHBANK_URL = baseURL;
		}
		if (imageUploadURL != null)
		{
			MorphBankTest.MORPHBANK_IM_POST_URL = imageUploadURL;
		}
		settingsComplete = conceptMappingId != null && ownerId != null && groupId != null && baseURL != null && imageUploadURL != null;
	}

    /**
     * Adds a Key mappings.
     * @param comp comp
     * @param keyCode keyCode
     * @param actionName actionName
     * @param action action 
     * @return the action
     */
    protected Action addRecordKeyMappings(final JComponent comp, final int keyCode, final String actionName, final Action action)
    {
        InputMap  inputMap  = comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = comp.getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke(keyCode, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), actionName);
        actionMap.put(actionName, action);
        
        //UIRegistry.registerAction(actionName, action);
        return action;
    }

	/**
	 * 
	 */
	protected void createUI()
	{
		PanelBuilder tpb = new PanelBuilder(new FormLayout("2dlu, f:p:g, 2dlu, f:p, 2dlu, f:p, 2dlu, f:p, 2dlu, f:p, 2dlu", "2dlu, f:p, 2dlu"));
    	CellConstraints cc = new CellConstraints();
		morphBankStatusLbl = new JLabel();
    	tpb.add(morphBankStatusLbl, cc.xy(2, 2));
    	//morphBankIdLbl = new JLabel();
    	//tpb.add(morphBankIdLbl, cc.xy(4, 1));
    	
		//PanelBuilder bpb = new PanelBuilder(new FormLayout("2dlu, f:p:g, 1dlu, f:p:g, 1dlu, f:p:g, 1dlu, f:p:g, 2dlu", 
		//		"f:p"));

    	detailsBtn = createIconBtn("InfoIcon", IconManager.IconSize.Std24, null, false, new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e)
			{
				showMorphBankDetails();
			}
    		
    	});

    	
//    	detailsBtn = new JButton(texts[kDetailsBtn]);
//		detailsBtn.addActionListener(new ActionListener() {
//
//			/* (non-Javadoc)
//			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//			 */
//			@Override
//			public void actionPerformed(ActionEvent e)
//			{
//				showMorphBankDetails();
//			}
//		});
		
    	tpb.add(detailsBtn, cc.xy(4, 2));
    	
    	//intent is to use morphbank icon here
    	viewMBBtn = createIconBtn("InfoIcon", IconManager.IconSize.Std24, null, false, new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e)
			{
		        SwingUtilities.invokeLater(new Runnable() {
		            public void run()
		            {
		            	viewMorphBankImagePage(getMbImageId());
		            }
		          });
			}
    		
    	});
        
    	
//    	viewMBBtn = new JButton(texts[kViewMBBtn]);
//    	viewMBBtn.addActionListener(new ActionListener() {
//
//			/* (non-Javadoc)
//			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//			 */
//			@Override
//			public void actionPerformed(ActionEvent arg0)
//			{
//		        SwingUtilities.invokeLater(new Runnable() {
//		            public void run()
//		            {
//		            	viewMorphBankImagePage(getMbImageId());
//		            }
//		          });
//				
//			}
//    		
//    	});
        
    	tpb.add(viewMBBtn, cc.xy(6, 2));

    	putBtn = createIconBtn("Green Arrow Up", IconManager.IconSize.Std24, null, false, new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e)
			{
				sendToMorphBank(false);
			}
    		
    	});
    	
//    	putBtn = new JButton(texts[kPutBtn]);
//    	putBtn.addActionListener(new ActionListener() {
//
//			/* (non-Javadoc)
//			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//			 */
//			@Override
//			public void actionPerformed(ActionEvent e)
//			{
//				sendToMorphBank(false);
//			}
//    		
//    	});
    	
    	tpb.add(putBtn, cc.xy(8, 2));
    	
    	getBtn = createIconBtn("Green Arrow Down", IconManager.IconSize.Std24, null, false, new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e)
			{
				//nothing yet;
			}
    		
    	});

    	//getBtn = new JButton(texts[kGetBtn]);
    	
    	tpb.add(getBtn, cc.xy(10, 2));

        Color bgColor = getBackground();
        bgColor = new Color(Math.max(bgColor.getRed()-20, 0), 
                            Math.max(bgColor.getGreen()-20, 0), 
                            Math.max(bgColor.getBlue()-20, 0));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bgColor), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
    	
        //setLayout(new BorderLayout());
    	add(tpb.getPanel() /*BorderLayout.CENTER*/);
    	//add(bpb.getPanel(), BorderLayout.SOUTH);    	
	}
	
	/**
	 * 
	 */
	protected void updateUIInfo()
	{
		setupButtons();
		
		morphBankStatusLbl.setText(getStatusText());
		//morphBankIdLbl.setText(getIdText());
	}
	
	protected void setupButtons()
	{
		detailsBtn.setEnabled(isEnabled() && isMorphBanked.get());
		viewMBBtn.setEnabled(isEnabled() && isMorphBanked.get() && settingsComplete);
		putBtn.setEnabled(isEnabled() && isImage && settingsComplete && (!isMorphBanked.get() || updatesToPut()));
		getBtn.setEnabled(settingsComplete && isEnabled() && updatesToGet());
		
		setButtonHints();
	}
	
	/**
	 * 
	 */
	protected void setButtonHints()
	{
		if (!settingsComplete)
		{
			detailsBtn.setToolTipText(texts[kSettingsIncompleteHint]);
			viewMBBtn.setToolTipText(texts[kSettingsIncompleteHint]);
			putBtn.setToolTipText(texts[kSettingsIncompleteHint]);
			getBtn.setToolTipText(texts[kSettingsIncompleteHint]);			
		} else
		{
			detailsBtn.setToolTipText(texts[kDetailBtnHint]);
			viewMBBtn.setToolTipText(texts[kviewMBBtnHint]);
			putBtn.setToolTipText(texts[kPutBtnHint]);
			getBtn.setToolTipText(texts[kGetBtnHint]);			
		}
		
	}
	/**
	 * @return
	 */
	protected String getStatusText()
	{
		if (!isImage)
		{
			return texts[kNotImageStatus];			
		}
		
		if (!isMorphBanked.get())
		{
			return texts[kNotMorphBankedStatus];			
		}
		
		if (updatesToPut() || updatesToGet())
		{
			return getIdText() + " [" + texts[kUpdatesNeededStatus] + "]";	
		}
		return getIdText() + " [" + texts[kUpToDateStatus] + "]";	
	}
	
	/**
	 * @return
	 */
	protected Integer getMbImageId()
	{
		if (isMorphBanked.get())
		{
			return imageAttribute.getMbImageId();
		}
		return null;
	}
	
	/**
	 * @return
	 */
	protected String getIdText()
	{
		String result = texts[kMorphBankID];
		if (isMorphBanked.get())
		{
			result += " " + getMbImageId();
		}
		return result;
	}
	
	/**
	 * @return true if changes have made to specify data since the last send to morphbank
	 */
	protected boolean updatesToPut()
	{
		if (!isMorphBanked.get())
		{
			return false;
		}
		
		AttachmentImageAttribute mbs = attachment.getAttachmentImageAttribute();
		if (mbs.getTimestampLastSend() == null)
		{
			return true;
		}
		
		//XXX yikes. this is hard? need to check timestamps for all relevant related data???
		return mbs.getTimestampLastSend().compareTo(attachment.getTimestampModified()) < 0;
	}
	
	/**
	 * @return true if annotations or other updates to this image are available from morphbank
	 */
	protected boolean updatesToGet()
	{
		return false;
	}
	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.plugins.UIPluginBase#isNotEmpty()
	 */
	@Override
	public boolean isNotEmpty()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#initialize(java.util.Properties, boolean)
	 */
	@Override
	public void initialize(Properties properties, boolean isViewMode)
	{
		super.initialize(properties, isViewMode);
		createUI();
	}


	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
	 */
	@Override
	public void setValue(Object value, String defaultValue)
	{
		colObj = null;
		isImage = false;
		isMorphBanked.set(false);
		attachment = null;
		attacher = null;
		if (value != null && fvo != null && fvo.getParentDataObj() != null)
		{
			if (value instanceof AttachmentImageAttribute)
			{
				if (fvo.getParentDataObj() instanceof CollectionObjectAttachment)
				{
					imageAttribute = (AttachmentImageAttribute )value;
					attachment = imageAttribute.getAttachment();
					attacher = (CollectionObjectAttachment )fvo.getParentDataObj();
					colObj = ((CollectionObjectAttachment )attacher).getCollectionObject();
					if (attachment != null)
					{
						isImage = isImageMimeType(attachment.getMimeType());
						isMorphBanked.set(imageAttribute.getMbImageId() != null);
					}
				}
				else
				{
					throw new NotImplementedException("MorphBankUploadPanel does not support " + value.getClass().getName());
					//System.out.println("MorphBankUploadPanel does not support " + fvo.getDataObj().getClass().getName());
				}
			}
		}
		setEnabled(isImage);
		updateUIInfo();
	}
	
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {"attachment", "collectionObject"};
    }

	/**
	 * @param imageId
	 */
	protected void viewMorphBankImagePage(Integer imageId)
	{
		String url = MorphBankTest.MORPHBANK_URL + "/" + MorphBankTest.MORPHBANK_IMAGE_Q + imageId;
		try
		{
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		} catch (Exception ex)
		{
			UIRegistry.displayStatusBarErrMsg(String.format(texts[kURLDisplayError], url));
		}
	}
	
	/**
	 * 
	 */
	protected void showMorphBankDetails()
	{
//        String viewName = "MorphBankStatus";
//        Frame   parentFrame  = (Frame)UIRegistry.get(UIRegistry.FRAME);
//        String  displayName  = ""; 
//        boolean isEdit       = false;
//        String  closeBtnText = (isEdit) ? getResourceString("SAVE") : getResourceString("CLOSE"); 
//        String  className    = "MorphBankStatus";
//        DBTableInfo nodeTableInfo = DBTableIdMgr.getInstance().getInfoById(MorphBankStatus.getClassTableId());
//        String  idFieldName  = nodeTableInfo.getIdFieldName();
//        int     options      = MultiView.HIDE_SAVE_BTN;
//                
//        // create the form dialog
//        String title = texts[kDetailsTitle]; 
//        ViewBasedDisplayDialog dialog = new ViewBasedDisplayDialog(parentFrame, null, viewName, displayName, title, 
//                                                                   closeBtnText, className, idFieldName, isEdit, options);
//        dialog.setModal(true);
//        dialog.setData(attachment.getMorphBankStatuses().iterator().next());
//        dialog.preCreateUI();
//        dialog.setVisible(true);
		
	}
	
	
	/**
	 * Send image to morphbank. Currently inserts only.
	 */
	protected void sendToMorphBank(final boolean isUpdate)
	{
		if (isUpdate)
		{
			throw new NotImplementedException("MorphBankPanel.sendToMorphbank: updates not implemented");
		}
		
        final ProgressDialog progDlg = new ProgressDialog(texts[kSendingToMorphbank], false, false);
        progDlg.setResizable(false);
        progDlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progDlg.setModal(true);
        progDlg.getProcessProgress().setIndeterminate(true);
        progDlg.setAlwaysOnTop(true);
        //progDlg.setProcess(0,100);
        //progDlg.setProcessPercent(true);
		javax.swing.SwingWorker<Object, Object> sw = new javax.swing.SwingWorker<Object, Object>() {

			protected Integer morphBankImageId = null;
			protected String statusText = null;
			protected Boolean success = false;
			protected Boolean imageFilePosted = false;
			protected Exception killer = null;

			/**
			 * @param morphbankImageId
			 * @param isUpdate
			 * @return
			 */
			protected boolean updateMorphBankStatusRecord(Integer morphbankImageId, boolean isUpdate)
			{
				if (isUpdate)
				{
					throw new NotImplementedException("MorphBankPanel.updateMorphBankStatusRecord: updates not implemented");
				}
				
		        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
		        boolean tblTransactionOpen = false;
		        try
		        {
		        	session.attach(attachment);
		        	AttachmentImageAttribute aia = attachment.getAttachmentImageAttribute();
		        	if (aia == null)
		        	{
		        		aia = new AttachmentImageAttribute();
			        	aia.initialize();
			        	aia.getAttachments().add(attachment);
			        	attachment.setAttachmentImageAttribute(aia);
		        	}
		        	aia.setMbImageId(morphbankImageId);
		        	aia.setTimestampLastSend(new Timestamp(System.currentTimeMillis()));
		            BusinessRulesIFace busRule = DBTableIdMgr.getInstance().getBusinessRule(AttachmentImageAttribute.class);
		            if (busRule != null)
		            {
		                busRule.beforeSave(aia, session);
		            }
		            session.beginTransaction();
		            tblTransactionOpen = true;
		            session.saveOrUpdate(aia);
		            session.saveOrUpdate(attachment);
		            if (busRule != null)
		            {
		                if (!busRule.beforeSaveCommit(aia, session))
		                {
		                	session.rollback();
		                    throw new Exception("Business rules processing failed");
		                }
		            }
		            session.commit();
		            tblTransactionOpen = false;
		            if (busRule != null)
		            {
		                busRule.afterSaveCommit(aia, session);
		            }
		            //session.merge(attachment);
		            //attacher = session.merge(attacher);
		            session.refresh(attacher);
		            imageAttribute = attachment.getAttachmentImageAttribute();
		            imageAttribute.forceLoad();
		        	return true;
		        }
		        catch (Exception ex)
		        {
		            if (tblTransactionOpen)
		            {
		                session.rollback();
		            }
		            killer = ex;
		            return false;
		        }
				finally
				{
					session.close();
				}
			}

			/**
			 * @param response
			 * @return Morphbank image id from response.
			 * response is assumed to have successful statuscode (200), but may not actually contain an image id.			 *
			 */
			private String getMorphbankIdStr(PostXMLSp.PostResponse response)
			{
				//System.out.println(response.getBody());
				String result = null;
				try
				{
					Element responseElement = XMLHelper.readStrToDOM4J(response.getBody());
					for (Object respObj : responseElement
						.selectNodes("response/object"))
					{
						Element object = (Element) respObj;
						if (object.attributeValue("type").equals("Image"))
						{
							Node id = object.selectSingleNode("sourceId/morphbank");
							if (id != null)
							{
								if (result != null)
								{
									statusText = texts[kMoreThanOneImageObjectCreated];
									result = null;
									break;
								} else
								{
									result = id.getText();
								}
							}
						}
					}
				} catch (Exception ex)
				{
		            killer = ex;
					statusText = texts[kNoImageObjectCreated];
				}
				return result;
			}
			
			/**
			 * @return full name of the 
			 */
			private String getImageFileName()
			{
				File imageDir = AttachmentUtils.getAttachmentManager().getOriginal(attachment);
				return imageDir.getAbsolutePath();
			}
			
			/**
			 * @return true if image file is successfully posted to Morphbank.
			 */
			private boolean postImageFile()
			{
				boolean result = false;
				try
				{
					PostMethod post = MorphBankTest.getImagePostRequest(morphBankImageId.toString(), getImageFileName());
					HttpClient httpclient = new HttpClient();
					int postStatus = httpclient.executeMethod(post);
					//System.out.println(post.getResponseBodyAsString());
					if (postStatus == 200)
					{
						result = true;
					}					
				} catch (Exception ex)
				{
		            killer = ex;
				}
				if (!result)
				{
					statusText = texts[kImageFileUploadFailed];
				}
				return result;
			}
			
			/* (non-Javadoc)
			* @see javax.swing.SwingWorker#doInBackground()
			*/
			@Override
			protected Object doInBackground() throws Exception
			{
				try
				{
					Request request = MorphBankTest.createRequestFromImage(
							attacher, conceptMappingId,
							getSubmitterCredentials(), getOwnerCredentials());

					String requestFileName = UIRegistry.getAppDataDir()
							+ File.separator + colObj.getCatalogNumber()
							+ ".xml";
					FileWriter outFile = new FileWriter(requestFileName);
					PrintWriter out = new PrintWriter(outFile);
					XmlUtils.printXml(out, request);
					PostXMLSp poster = new PostXMLSp();
					PostXMLSp.PostResponse response = poster
							.post(requestFileName);
					if (response.getStatusCode() == 200)
					if (true)
					{
						String morphbankIDStr = getMorphbankIdStr(response);
						//String morphbankIDStr = "666";
						
						//System.out.println(morphbankIDStr);
						if (morphbankIDStr != null)
						{
							morphBankImageId = Integer
									.valueOf(morphbankIDStr);
							success = updateMorphBankStatusRecord(
									morphBankImageId, isUpdate);
							if (success)
							{
								imageFilePosted = postImageFile();
							}
						}
						else
						{
							statusText = texts[kNoImageObjectCreated];
						}
					}
				} catch (Exception e)
				{
					killer = e;
					success = false;
				}
				return null;
			}

			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#done()
			 */
			@Override
			protected void done()
			{
				super.done();
				progDlg.setVisible(false);
				if (success && morphBankImageId != null)
				{
					SwingUtilities.invokeLater(new Runnable() {

						/*
						 * (non-Javadoc)
						 * 
						 * @see java.lang.Runnable#run()
						 */
						@Override
						public void run()
						{
							JOptionPane.showMessageDialog(UIRegistry
									.getTopWindow(),
									texts[kSuccessfulImageInsert],
									getResourceString("INFORMATION"),
									JOptionPane.INFORMATION_MESSAGE);
							if (!imageFilePosted)
							{
								JOptionPane.showMessageDialog(UIRegistry
										.getTopWindow(),
										texts[kImageFileUploadFailed],
										getResourceString("INFORMATION"),
										JOptionPane.INFORMATION_MESSAGE);
							}
							setValue(imageAttribute, null);
						}
					});
				} else
				{

					SwingUtilities.invokeLater(new Runnable() {

						/*
						 * (non-Javadoc)
						 * 
						 * @see java.lang.Runnable#run()
						 */
						@Override
						public void run()
						{
							String msg = statusText;
							if (killer != null
									&& killer.getLocalizedMessage() != null)
							{
								if (msg != null)
								{
									msg += " (" + killer.getLocalizedMessage()
											+ ")";
								} else
								{
									msg = killer.getLocalizedMessage();
								}
							}
							UIRegistry.showError(msg);
						}
					});
				}
			}
		};
    	//UIRegistry.writeGlassPaneMsg(texts[kSendingToMorphbank], 24); 
    	sw.execute();
		UIHelper.centerAndShow(progDlg);					
	}

	/**
	 * @return Morphbank Owner credentials
	 */
	protected Credentials getOwnerCredentials()
	{
		Credentials result = new Credentials();
		//Me:
		//result.setUserId(1000338);
		//result.setGroupId(1000339);
		
		//Andy
		//result.setUserId(568231);
		//result.setGroupId(568232);
		
		result.setUserId(ownerId);
		result.setGroupId(groupId);
		return result;
	}
	
	/**
	 * @return Morphbank Submitter credentials
	 */
	protected Credentials getSubmitterCredentials()
	{
		Credentials result = new Credentials();
		//result.setUserId(1000338);
		//result.setGroupId(1000339);
		
		result.setUserId(ownerId);
		result.setGroupId(groupId);
		return result;
	}

	/**
	 * @param mimeType
	 */
	protected boolean isImageMimeType(final String mimeType)
	{
		return mimeType != null && 
			(mimeType.startsWith("image") || mimeType.equals("png"));
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		setupButtons();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.plugins.UIPluginBase#setParent(edu.ku.brc.af.ui.forms.FormViewObj)
	 */
	@Override
	public void setParent(FormViewObj parent)
	{
		super.setParent(parent);
		colObj = null;
		if (fvo != null && fvo.getDataObj() != null)
		{
			if (fvo.getDataObj() instanceof CollectionObject)
			{
				colObj = (CollectionObject )fvo.getDataObj();
			}
			else
			{
				throw new NotImplementedException("MorphBankUploadButton does not support " + fvo.getDataObj().getClass().getName());
				//System.out.println("MorphBankUploadButton does not support " + fvo.getDataObj().getClass().getName());
			}
			
		}
	}


}
