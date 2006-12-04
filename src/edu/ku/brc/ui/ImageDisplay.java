/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UICacheManager.getResourceString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.util.FileCache;

/**
 * This class is repsonsible for displaying an image.The ImageDisplay will resize the image approriately to fit within its space
 * and it will keep the image proportional.
 * 
 * @code_status Beta
 * @author rods, jstewart
 *
 */
@SuppressWarnings("serial")
public class ImageDisplay extends JPanel implements GetSetValueIFace
{
	protected ImageIcon imageIcon = null;
	protected boolean isError = false;
	protected String url;
	protected boolean isEditMode = true;
	protected JButton editBtn;
	protected ImageGetter getter = null;
	protected String noImageStr = getResourceString("noimage");
	protected String loadingImageStr = getResourceString("loadingimage");
	protected boolean isNoImage = true;
    protected JFileChooser chooser;

	/**
	 * Constructor
	 * @param imgWidth the desired image width
	 * @param imgHeight the desired image height
	 * @param isEditMode whether it is in browse mode or edit mode
	 * @param hasBorder whether it has a border
	 */
	public ImageDisplay(final int imgWidth, final int imgHeight,
			boolean isEditMode, boolean hasBorder)
	{
		super(new BorderLayout());
		setBorder(hasBorder ? new EtchedBorder(EtchedBorder.LOWERED)
				: BorderFactory.createEmptyBorder());

		setPreferredSize(new Dimension(imgWidth, imgHeight));

		this.isEditMode = isEditMode;
		createUI();
	}

	/**
	 * Constructor with ImageIcon
	 * @param imgIcon the icon to be displayed
	 * @param isEditMode whether it is in browse mode or edit mode
	 * @param hasBorder whether it has a border
	 */
	public ImageDisplay(final ImageIcon imgIcon, boolean isEditMode,
			boolean hasBorder)
	{
		this(imgIcon.getIconWidth(), imgIcon.getIconHeight(), isEditMode,
				hasBorder);
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

		int returnVal = chooser.showOpenDialog(UICacheManager
				.get(UICacheManager.TOPFRAME));
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = new File(chooser.getSelectedFile().getAbsolutePath());
			setImage(new ImageIcon(file.getAbsolutePath()));
            url = file.getAbsolutePath();
			repaint();
		}
        
        firePropertyChange("imageURL", oldURL, url);
	}

	/**
	 * @param imgIcon
	 */
	public synchronized void setImage(final ImageIcon imgIcon)
	{
		if (imgIcon != null && imgIcon.getIconWidth() > 0
				&& imgIcon.getIconHeight() > 0)
		{
			imageIcon = imgIcon;
			setNoImage(false);
		} else
		{
			imageIcon = null;
			setNoImage(true);
		}
		repaint();

		doLayout();
	}

	/**
	 *
	 */
	protected void simpleLoad()
	{
		try
		{
			setNoImage(false); // means it is loading it

			//imgIcon = new ImageIcon(new URL(url));
			Image img = getToolkit().getImage(new URL(url));

			imageIcon = new ImageIcon(img);
			setImage(imageIcon);

		} catch (Exception e)
		{
			//log.error(e);
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	public void loadImage()
	{
		if (isNotEmpty(url))
		{
			setNoImage(false); // means it is loading it
			repaint();

			if (getter != null)
			{
				getter.stop();
			}

			if (url.startsWith("file"))
			{
				try
				{
					File f = new File(new URI(url));
					ImageIcon icon = new ImageIcon(f.getAbsolutePath());
					setImage(icon);
					repaint();
					return;
				} catch (URISyntaxException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			getter = new ImageGetter(url);
			getter.start();

		} else
		{
			setNoImage(true);
			setImage(null);
		}
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		int w = getWidth();
		int h = getHeight();

		if (imageIcon != null && !isNoImage)
		{
			int imgW = imageIcon.getIconWidth();
			int imgH = imageIcon.getIconHeight();

			if (imgW > w || imgH > h)
			{
				double scaleW = 1.0;
				double scaleH = 1.0;
				double scale = 1.0;

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
			Image image = imageIcon.getImage();
			g.drawImage(image, x, y, imgW, imgH, null);
		} else
		{
			String label = this.isNoImage ? noImageStr : loadingImageStr;
			FontMetrics fm = g.getFontMetrics();
			g.drawString(label, (w - fm.stringWidth(label)) / 2, (h - fm.getAscent()) / 2);
		}
	}

	public void setNoImage(boolean isNoImage)
	{
		this.isNoImage = isNoImage;
		repaint();
	}

	//--------------------------------------------------------------
	//-- GetSetValueIFace
	//--------------------------------------------------------------

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
	 */
	public void setValue(Object value, String defaultValue)
	{
		if (value instanceof String)
		{
			url = (String) value;
			loadImage();
		}
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
	 */
	public Object getValue()
	{
		return url;
	}

	public static void main(String[] args) throws InterruptedException
	{
		ImageIcon icon = new ImageIcon("demo_files/beach.jpg");
		final ImageDisplay id = new ImageDisplay(icon, true, true);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame f = new JFrame();
				PanelBuilder builder = new PanelBuilder(new FormLayout("p,p",
						"p,p,p,p"));
				CellConstraints cc = new CellConstraints();
				builder.add(new JLabel("1 2 3 4 5 6 7 8 9 0"), cc.xywh(1, 1, 1,1));
				builder.add(new JLabel("1 2 3 4 5 6 7 8 9 0"), cc.xywh(1, 2, 1,1));
				builder.add(new JLabel("1 2 3 4 5 6 7 8 9 0"), cc.xywh(1, 3, 1,1));
				builder.add(new JLabel("1 2 3 4 5 6 7 8 9 0"), cc.xywh(1, 4, 1,1));
				builder.add(id, cc.xywh(2, 1, 1, 4));
				f.add(builder.getPanel());
				f.pack();
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setVisible(true);
			}
		});

		Thread.sleep(2000);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				System.out.println("id.setNoImage(true)");
				id.setNoImage(true);
			}
		});

		Thread.sleep(2000);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				System.out.println("id.setNoImage(false)");
				id.setNoImage(false);
			}
		});

		Thread.sleep(2000);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				System.out.println("id.setImage(null)");
				id.setImage(null);
			}
		});
		Thread.sleep(1000);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				System.out.println("id.setValue(\"http://www.google.com/images/logo_sm.gif\",null)");
				id.setValue("http://www.google.com/images/logo_sm.gif", null);
			}
		});
	}

	//--------------------------------------------------------------
	//-- Inner Class JPanel for displaying an image
	//--------------------------------------------------------------
	public class ImageGetter implements Runnable
	{

		// Error Code
		public int kNoError = 0;
		public int kError = 1;
		public int kHttpError = 2;
		public int kNotDoneError = 3;
		public int kIOError = 4;
		public int kURLError = 5;
		private Thread thread = null;
		private String urlStr;

		public ImageGetter(String urlStr)
		{
			this.urlStr = urlStr;
		}

		//----------------------------------------------------------------
		//-- Runnable Interface
		//----------------------------------------------------------------
		public void start()
		{
			if (thread == null)
			{
				thread = new Thread(this);
				//thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}

		/**
		 *
		 *
		 */
		public synchronized void stop()
		{
			if (thread != null)
			{
				thread.interrupt();
			}
			thread = null;
			notifyAll();
		}

		/**
		 *
		 */
		public void run()
		{
			try
			{
				setNoImage(false); // means it is loading it

				FileCache fileCache = UICacheManager.getLongTermFileCache();
				if (fileCache != null)
				{
					File file = fileCache.getCacheFile(urlStr);
					if (file == null)
					{
						String fileName = fileCache.cacheWebResource(urlStr);
						file = fileCache.getCacheFile(fileName);
					}
					Image img = getToolkit().getImage(file.toURL());
					ImageIcon imgIcon = new ImageIcon(img);

					setImage(imgIcon);
				}

				getter = null;

			} catch (Exception e)
			{
				//log.error(e);
				e.printStackTrace();
			}
		}
	}
}
