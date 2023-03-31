/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This class is responsible for displaying an image.The ImageDisplay will resize the image approriately to fit within its space
 * and it will keep the image proportional.
 * 
 * @code_status Beta
 * @author rods, jstewart
 *
 */
@SuppressWarnings("serial")
public class ImageDisplay extends JPanel implements GetSetValueIFace, ImageLoaderIFace
{
    // Error Code
    public static final int kImageOK       = 0;
    public static final int kError         = 1;
    public static final int kHttpError     = 2;
    public static final int kInterruptedError = 3;
    public static final int kIOError       = 4;
    public static final int kURLError      = 5;
    
    private byte[]         bytes           = null;
    
	protected Image        image           = null;
	protected String       url;
    protected boolean      isEditMode      = true;
    protected boolean      isFullImage     = false;
	protected JButton      editBtn;
    protected String[]     noAttachmentStr      = StringUtils.split(getResourceString("noattachment"), ' ');
    protected String[]     noThumnailStr        = StringUtils.split(getResourceString("nothumbnail"), ' ');
	protected String[]     loadingAttachmentStr = StringUtils.split(getResourceString("loadingattachment"), ' ');
	protected boolean      isNoAttachment       = true;
    protected JFileChooser chooser;
    protected boolean      doShowText      = true;
    
    protected ChangeListener changeListener = null;
    protected JComponent     paintComponent = null;
    private   int            status         = kImageOK;
    private   ArrayList<File> fileCache = new ArrayList<File>();

    //protected AtomicBoolean isLoading       = new AtomicBoolean(false);
    //protected AtomicBoolean stopLoading     = new AtomicBoolean(false);
    protected boolean isLoading   = false;
    protected boolean stopLoading = false;

	/**
	 * Constructor.
	 * @param imgWidth the desired image width
	 * @param imgHeight the desired image height
	 * @param isEditMode whether it is in browse mode or edit mode
	 * @param hasBorder whether it has a border
	 */
	public ImageDisplay(final int imgWidth, final int imgHeight, boolean isEditMode, boolean hasBorder)
	{
		super(new BorderLayout());
		
		setBorder(hasBorder ? new EtchedBorder(EtchedBorder.LOWERED) : BorderFactory.createEmptyBorder());

		setPreferredSize(new Dimension(imgWidth, imgHeight));

		this.isEditMode     = isEditMode;
		this.paintComponent = this;
		createUI();
		
		setDoubleBuffered(true);
	}
	
	/**
	 * @param msg
	 */
	public void setThumbnailMsg(final String msg)
	{
	    noThumnailStr = StringUtils.split(msg, ' ');
	}

	/**
	 * Constructor with ImageIcon.
	 * @param imgIcon the icon to be displayed
	 * @param isEditMode whether it is in browse mode or edit mode
	 * @param hasBorder whether it has a border
	 */
    public ImageDisplay(final ImageIcon imgIcon, boolean isEditMode, boolean hasBorder)
    {
        this(imgIcon.getIconWidth(), imgIcon.getIconHeight(), isEditMode, hasBorder);
        setImage(imgIcon.getImage());
    }

    /**
     * @param imgIcon
     * @param isEditMode
     * @param hasBorder
     */
    public ImageDisplay(final Image imgIcon, boolean isEditMode, boolean hasBorder)
    {
        this(imgIcon.getWidth(null), imgIcon.getHeight(null), isEditMode, hasBorder);
        setImage(imgIcon);
    }

    /**
	 *
	 */
	protected void createUI()
	{
		this.setLayout(new FormLayout("f:p:g" + (isEditMode ? ",2px,p" : ""),"p"));
		CellConstraints cc = new CellConstraints();

		if (isEditMode)
		{
			ImageIcon icon = IconManager.getImage("EditIcon",
					IconManager.IconSize.NonStd);

			editBtn = new JButton(icon);
			editBtn.setMargin(new Insets(1, 1, 1, 1));
			editBtn.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					selectNewImage();
				}
			});
			add(editBtn, cc.xy(3, 1));
		}
	}

    /**
     * @param isFullImage the isFullImage to set
     */
    public void setFullImage(boolean isFullImage)
    {
        this.isFullImage = isFullImage;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageLoaderIFace#stopLoading()
     */
    @Override
    public void stopLoading()
    {
        stopLoading = true;
    }

	/**
     * @return the changeListener
     */
    public ChangeListener getChangeListener()
    {
        return changeListener;
    }
    
    /**
     * @return
     */
    public boolean isInError()
    {
        return status != kImageOK;
    }

    /**
     * @param changeListener the changeListener to set
     */
    public void setChangeListener(ChangeListener changeListener)
    {
        this.changeListener = changeListener;
    }

    /**
     * @return the isLoading
     */
    public boolean isLoading()
    {
        return isLoading;
    }

    /**
     * @param isLoading the isLoading to set
     */
    public void setLoading(boolean isLoading)
    {
        this.isLoading = isLoading;
    }

    /**
     * 
     */
    protected void selectNewImage()
	{
        String oldURL = this.url;
		synchronized(this)
        {
           if (chooser==null)
           {
            //      XXX Need to add a filter for just images
               chooser = new JFileChooser();
           }
        }

		int returnVal = chooser.showOpenDialog(UIRegistry.getMostRecentWindow());
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = new File(chooser.getSelectedFile().getAbsolutePath());
			try
            {
                setImage(ImageIO.read(file));
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            url = file.getAbsolutePath();
			repaint();
		}
        
        firePropertyChange("imageURL", oldURL, url);
	}

	/**
	 * @param newImage
	 */
    public void setImage(final Image newImage)
    {
        if (newImage != null && 
            newImage.getWidth(null) > 0 &&
            newImage.getHeight(null) > 0)
        {
            image = newImage;
            setNoImage(false);
            status = kImageOK;
        } else
        {
            image = null;
            setNoImage(true);
        }
        notifyOnUIThread(true, true);
        repaint();
        //invalidate();
        //doLayout();
    }
    
    /**
     * @param newImageIcon
     */
    public void setImage(final ImageIcon newImageIcon)
    { 
        if (newImageIcon != null)
        {
            setImage(newImageIcon.getImage());
        } else
        {
            setImage((Image)null);
        }
        notifyOnUIThread(true, true);
        //invalidate();
        //doLayout();
    }
    
    /**
     * 
     */
    private void doLoadFromURL()
    {
        try
        {
            File localFile = getFileFromWeb(url);
            if (localFile != null)
            {
                localFile.deleteOnExit();
                
                setImage(ImageIO.read(localFile));
                status = kImageOK;
                
                if (fileCache.size() > 2)
                {
                    File f = fileCache.get(0);
                    fileCache.remove(0);
                    f.delete();
                }
                fileCache.add(localFile);
                
            } else
            {
                status = kIOError;
            }
    
        } catch (MalformedURLException e)
        {
            status = kURLError;
            e.printStackTrace();
            
        } catch (Exception e)
        {
            status = kError;
            e.printStackTrace();
        }
        done();
    }
    
	/* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageLoaderIFace#done()
     */
    @Override
    public void done()
    {
        if (paintComponent != null && !stopLoading)
        {
            notifyOnUIThread(true, true);
        }
    }

    /**
	 * Checks to see if it is a File URL first.
	 */
	private void startImageLoad()
	{
	    if (stopLoading)
	    {
	        //System.err.println("Skipping load..."+url);
	        status = kError;
            return;
	    }
	    //System.err.println("Starting load... "+url);
	    
	    status = kImageOK;
		if (isNotEmpty(url))
		{
			setNoImage(false); // means it is loading it
			
			if (url.startsWith("file"))
			{
				try
				{
					File f = new File(new URI(url));
					if (f.exists())
					{
					    Image img = ImageIO.read(f);
					    setImage(img);
                        status = kImageOK;
					} else
					{
					    status = kError;
					}
                    done();
					return;
					
				} catch (Exception e)
				{
				    status = kURLError;
				    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
				    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ImageDisplay.class, e);
					e.printStackTrace();
					done();
				}
			} else
			{
			    doLoadFromURL();
			}

		} else
		{
		    status = kURLError;
			setNoImage(true);
			setImage((Image)null);
            done();
		}
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageLoaderIFace#cleanup()
     */
    @Override
    public void cleanup()
    {
        image          = null;
        changeListener = null;
        paintComponent = null;
        fileCache.clear();
        fileCache      = null;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageLoaderIFace#getStatus()
     */
    @Override
    public int getStatus()
    {
        return status;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ImageLoaderIFace#load()
     */
    @Override
    public void load()
    {
        startImageLoad();
    }

    /* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		boolean doScale = true;

		int w = getWidth();
		int h = getHeight();

        Image   dspImg         = image;
		boolean doDisplayImage = (image != null && (!isNoAttachment && status == kImageOK)) || isLoading;
		if (isLoading)
		{
		    doDisplayImage = true;
		    dspImg = IconManager.getImage("Loading").getImage();
		}
		if (doDisplayImage && dspImg != null)
		{
			int imgW = dspImg.getWidth(null);
			int imgH = dspImg.getHeight(null);

			if (doScale && (imgW > w || imgH > h))
			{
				double scaleW = 1.0;
				double scaleH = 1.0;
				double scale  = 1.0;

				if (imgW > w)
				{
					scaleW = (double) w / imgW;
				}
				if (imgH > h)
				{
					scaleH = (double) h / imgH;
				}
				scale = Math.min(scaleW, scaleH);

				imgW = (int) (imgW * scale);
				imgH = (int) (imgH * scale);
			}

			int x = 0;
			int y = 0;

			if (imgW < w)
			{
				x = (w - imgW) / 2;
			}
			if (imgH < h)
			{
				y = (h - imgH) / 2;
			}
			g.drawImage(dspImg, x, y, imgW, imgH, null);
			
		} else if (doShowText)
		{
		    GraphicsUtils.turnOnAntialiasedDrawing(g);
			String[]    label   = this.isNoAttachment ? (isFullImage ? noAttachmentStr : noThumnailStr) : loadingAttachmentStr;
			FontMetrics fm      = g.getFontMetrics();
			int         spacing = 2;
			int         yOffset = (h - (fm.getHeight() + spacing) * label.length) / 2;
			if (yOffset < 0) yOffset = 0;
			int y = yOffset + fm.getHeight();
			for (String str : label)
			{
			    g.drawString(str, (w - fm.stringWidth(str)) / 2, y);
			    y += fm.getHeight() + 2;
			}
		}
	}
	
	/**
     * @param noImageStr the noImageStr to set
     */
    public void setNoImageStr(final String noImageStr)
    {
        this.noAttachmentStr = StringUtils.isNotEmpty(noImageStr) ? StringUtils.split(noImageStr, ' ') : new String[] {};
    }

    /**
	 * @param doRepaint
	 * @param notifyChangeListeners
	 */
	private void notifyOnUIThread(final boolean doRepaint, final boolean notifyChangeListeners)
	{
	    if (!stopLoading)
	    {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    if (doRepaint)
                    {
                        ImageDisplay.this.repaint();
                    }
                    
                    if (notifyChangeListeners && changeListener != null)
                    {
                        changeListener.stateChanged(new ChangeEvent(this));
                    }

                }
            });
	    }
	}

	/**
	 * @param isNoImage
	 */
	public void setNoImage(boolean isNoImage)
	{
		this.isNoAttachment = isNoImage;

	}

	//--------------------------------------------------------------
	//-- GetSetValueIFace
	//--------------------------------------------------------------

	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
	 */
	public void setValue(Object value, String defaultValue)
	{
		if (value instanceof String)
		{
			url = (String) value;
			ImageLoaderExector.getInstance().loadImage(this);
		}
		if (value == null)
		{
		    url = null;
		    image = null;
		    isNoAttachment = true;
		}
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
	 */
	public Object getValue()
	{
		return url;
	}

	/**
     * @return the doShowText
     */
    public boolean isDoShowText()
    {
        return doShowText;
    }
    
    /**
     * @param doShowText the doShowText to set
     */
    public void setDoShowText(boolean doShowText)
    {
        this.doShowText = doShowText;
    }

    /**
     * @param urlStr
     * @param tmpFile
     * @return
     */
    private boolean fillFileFromWeb(final String urlStr, 
                                    final File tmpFile)
    {
        if (bytes == null)
        {
            bytes = new byte[100*1024];
        }
        
        try
        {
            URL urlObj = new URL(urlStr);
            InputStream inpStream = urlObj.openStream();
            if (inpStream != null)
            {
                BufferedInputStream  in  = new BufferedInputStream(inpStream);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmpFile));
                
                do
                {
                    int numBytes = in.read(bytes);
                    if (numBytes == -1)
                    {
                        break;
                    }
                    bos.write(bytes, 0, numBytes);
                    
                } while(true);
                in.close();
                bos.close();
            
                return true;
            }
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * @param fileName
     * @param mimeType
     * @param isThumb
     * @return
     */
    private synchronized File getFileFromWeb(final String urlStr)
    {
        try
        {
            File tmpFile = File.createTempFile("sp6", ".img", null);
            return fillFileFromWeb(urlStr, tmpFile) ? tmpFile : null;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
}
