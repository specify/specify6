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
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.EditViewCompSwitcherPanel;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace.CreationMode;
import edu.ku.brc.af.ui.forms.validation.ValBrowseBtnPanel;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.AttachmentManagerIface;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * @author jstewart
 * @code_status Alpha
 */
public class AttachmentBusRules extends BaseBusRules
{
    protected Logger log = Logger.getLogger(AttachmentBusRules.class);

	String[] tableNames = {"accessionattachment",          "agentattachment",             "collectingeventattachment",
                          "collectionobjectattachment",    "conservdescriptionattachment","conserveventattachment",
                          "dnasequenceattachment",         "fieldnotebookattachment",     "fieldnotebookpageattachment",
                          "fieldnotebookpagesetattachment","loanattachment",              "localityattachment",
                          "permitattachment",              "preparationattachment",       "repositoryagreementattachment",
                          "taxonattachment"};
    
    static private String BROWSE_DIR_PREF = "AttachmentBrowseDir";
    private ValBrowseBtnPanel browser = null;
    private Component morphbankPanel = null;
    private Component origComp = null;
    private MultiView imageAttributeMultiView = null;
    
    /**
     * 
     */
    public AttachmentBusRules()
    {
        super(Attachment.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null)
        {
            //morphbankPanel = formViewObj.getCompById("morphbankpanel");

            imageAttributeMultiView = formViewObj.getKids() != null && formViewObj.getKids().size() > 0 
            	? formViewObj.getKids().get(0) : null;

            if (imageAttributeMultiView != null)
            {
            	morphbankPanel = imageAttributeMultiView.getCurrentViewAsFormViewObj().getCompById("morphbankpanel");
            }
            
            origComp  = formViewObj.getCompById("origFilename");
            final Component titleComp = formViewObj.getCompById("title");
            
            if (origComp instanceof EditViewCompSwitcherPanel)
            {
                EditViewCompSwitcherPanel evcsp = (EditViewCompSwitcherPanel)origComp;
                browser = (ValBrowseBtnPanel)evcsp.getComp(true);
    			String dir = AppPreferences.getLocalPrefs().get(BROWSE_DIR_PREF, null);
    			if (dir != null)
    			{
    				browser.setCurrentDir(dir);
    			}
           }
            
            if (browser != null)
            {
                if (titleComp instanceof ValTextField)
                {
                    final ValTextField  titleTF = (ValTextField)titleComp;
                    final ValTextField browserTF = browser.getValTextField();
                    
                    browserTF.getDocument().addDocumentListener(new DocumentAdaptor() {
                        @Override
                        protected void changed(DocumentEvent e)
                        {
                            if (formViewObj.getDataObj() != null && ((DataModelObjBase )formViewObj.getDataObj()).getId() == null)
                            {
                            	String filePath = browserTF.getText();
                            	if (!filePath.isEmpty())
                            	{
                            		titleTF.setText(FilenameUtils.getBaseName(browserTF.getText()));
                            		addImageAttributeIfNecessary();
                            
                            	} else
                            	{
                            		if (!titleTF.getText().isEmpty())
                            		{
                            			titleTF.setText(null);
                            		}
                            	}
                            }
                        }
                    });
                    
                    browserTF.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e)
                        {
                            super.focusLost(e);
                            if (formViewObj.getDataObj() != null && ((DataModelObjBase )formViewObj.getDataObj()).getId() == null)
                            {
                            	String filePath = browserTF.getText();
                            	if (titleTF.getText().isEmpty() && !filePath.isEmpty())
                            	{
                            		titleTF.setText(FilenameUtils.getBaseName(filePath));
                            	}
                            }
                        }
                    });
                }
                
                if (formViewObj.getRsController() != null && formViewObj.getRsController().getNewRecBtn() != null)
                {
                    formViewObj.getRsController().getNewRecBtn().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run()
                                {
                                    browser.getBrowseBtn().doClick();
                                }
                            });
                        }
                    });
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        reasonList.clear();
        if (dataObj instanceof Attachment)
        {
            Attachment attach = (Attachment)dataObj;
            Integer id = attach.getId();
            if (id == null)
            {
                return true;
            }
            
            for (String tName : tableNames)
            {
                if (!super.okToDelete(tName, "AttachmentID", id))
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    protected void addImageAttributeIfNecessary()
    {
    	if (browser != null)
    	{
    		Integer width = null;
    		Integer height = null;
            File file = new File(browser.getValue().toString());
        	String mimeType = file == null ? null : AttachmentUtils.getMimeType(file.getName());
            boolean isImage = mimeType != null && mimeType.startsWith("image");
        	if (isImage)
            {
            	try 
            	{
            		ImageInputStream iis = ImageIO.createImageInputStream(file);
            		Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            
            		if (readers.hasNext()) 
            		{
           				// pick the first available ImageReader
           				ImageReader reader = readers.next();
           				// attach source to the reader
           				reader.setInput(iis, true);

           				// read metadata of first image
           				//                		IIOMetadata metadata = reader.getImageMetadata(0);
                
           				width = reader.getWidth(0);
           				height = reader.getHeight(0);
           			}
           		} catch (IOException ex)
           		{
           			//XXX does this execption necessarily mean the file is bad?
           			//XXX throw or log this exception
           			ex.printStackTrace();
           		}
           	}
           	//MultiView mvobj = formViewObj.getKids().get(0);
            FormViewObj aiafv = imageAttributeMultiView == null ? null
            		: imageAttributeMultiView.getCurrentViewAsFormViewObj();
            if (aiafv != null)
            {
                //hide add/delete buttons. 
            	aiafv.getNewRecBtn().setVisible(false);
                aiafv.getDelRecBtn().setVisible(false);
            	if (isImage)
            	{
            		if (aiafv.getDataObj() == null)
            		{
            			aiafv.getNewRecBtn().doClick();
            		}
            		System.out.println(browser.getValue() + "height " + height + " width " + width);
            		try
            		{
            			aiafv.setDataIntoUIComp("height", height);
            			aiafv.setControlChanged("height");
            			aiafv.setDataIntoUIComp("width", width);
            			aiafv.setControlChanged("width");
            		} catch (Exception e)
            		{
            			log.error("Unable set image attribute data. Controls may be missing from form definition");
            		}
            	} else
            	{
            		if (aiafv.getDataObj() != null)
            		{
            			//delete the imageAttribute rec
            			//XXX suppress "confirm delete" dlg?
            			aiafv.getDelRecBtn().doClick(); 
            		}
            	}
            }
            setupImageAttributeView();
        }
    }
    /**
     * Returns a count of how many times this attachment is attached.
     * @param ids the id of the attachment(s)
     * @return the count of how many times it is attached
     */
    public Integer getTotalCountOfAttachments(final Integer...ids)
    {
        String[] tableColCombos = new String[tableNames.length*2];
        int inx = 0;
        for (String tName : tableNames)
        {
            tableColCombos[inx++] = tName;
            tableColCombos[inx++] = "AttachmentID";
        }
        
        return getTotalCount(tableColCombos, ids);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeSaveCommit(final Object dataObj, final DataProviderSessionIFace session) throws Exception
    {
        boolean retVal = super.beforeSaveCommit(dataObj, session);
        if (retVal == false)
        {
            return retVal;
        }
        
        if (!(dataObj instanceof Attachment))
        {
            return true;
        }
        
        Attachment attachment = (Attachment)dataObj;
        
        // Copy the attachment file to the file storage system
        Thumbnailer thumbnailGen = AttachmentUtils.getThumbnailer();
        AttachmentManagerIface attachmentMgr = AttachmentUtils.getAttachmentManager();
        File origFile = new File(attachment.getOrigFilename());
        File thumbFile = null;
        
        try
        {
            thumbFile = File.createTempFile("sp6_thumb_", null);
            thumbFile.deleteOnExit();
            thumbnailGen.generateThumbnail(attachment.getOrigFilename(), 
                                           thumbFile.getAbsolutePath(),
                                           false);
        }
        catch (IOException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AttachmentBusRules.class, e);
            // unable to create thumbnail
            thumbFile = null;
        }
        
        attachmentMgr.storeAttachmentFile(attachment, origFile, thumbFile);
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#beforeDeleteCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeDeleteCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        return super.beforeDeleteCommit(dataObj, session);
        
        // TODO: delete the attachment from the storage system
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
	 */
	@Override
	public void afterFillForm(Object dataObj)
	{
		super.afterFillForm(dataObj);
        if (morphbankPanel != null)
        {
        	morphbankPanel.setEnabled(formViewObj.getAltView().getMode() != CreationMode.EDIT);
        }
        setupImageAttributeView();
	}

	/**
	 * shows/hides image attribute view based on type of attachment 
	 */
	private void setupImageAttributeView()
	{
        if (origComp != null) 
        {
           	String fileName = origComp instanceof JTextField ? ((JTextField )origComp).getText() :
           		 browser.getValue().toString();
           	if (fileName != null && !fileName.isEmpty())
           	{
           		String mimeType = fileName == null ? null : AttachmentUtils.getMimeType(fileName);
           		boolean isImage = mimeType != null && mimeType.startsWith("image");
           		if (imageAttributeMultiView != null)
           		{
           			imageAttributeMultiView.setVisible(isImage);
           			Dialog dlg = UIHelper.getDialog(imageAttributeMultiView);
           			if (dlg != null)
           			{
           				dlg.pack();
           			}
           		}
           	}
        }

	}
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.BaseBusRules#aboutToShutdown()
	 */
	@Override
	public void aboutToShutdown() 
	{
		super.aboutToShutdown();
		if (browser != null)
		{
			String dir = browser.getCurrentDir();
			if (dir != null)
			{
				AppPreferences.getLocalPrefs().put(BROWSE_DIR_PREF, dir);
			}
			System.out.println(browser.getCurrentDir());
		}
	}
    
    
}
