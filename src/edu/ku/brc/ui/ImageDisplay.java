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
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.util.FileCache;


/**
 * This class is repsonsible for displaying an image in a confined panel. The sizes that are passed in at constructoin time are
 * the maximum sizes of the display area. The IMafeDisplay will resize the image approriately to fit within this size and it will
 * keep the image proportional.
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ImageDisplay extends JPanel implements GetSetValueIFace
{
    private ImageIcon    imgIcon   = null;
    protected boolean    isError   = false;
    protected String     url;
    protected boolean    isEditMode = true;
    protected ImagePanel imagePanel;
    protected JButton    editBtn;

    protected ImageGetter getter = null;



    /**
     * Constructor
     * @param imgWidth the desired image width
     * @param imgHeight the desired image height
     * @param isEditMode whether it is in browse mode or edit mode
     * @param hasBorder whether it has a border
     */
    public ImageDisplay(final int imgWidth, final int imgHeight, boolean isEditMode, boolean hasBorder)
    {
        super(new BorderLayout());
        setBorder(hasBorder ? new EtchedBorder(EtchedBorder.LOWERED) : BorderFactory.createEmptyBorder());

        this.isEditMode = isEditMode;
        imagePanel = new ImagePanel(imgWidth, imgHeight);
        createUI();
    }

    /**
     * Constructor with ImageIcon
     * @param imgIcon the icon to be displayed
     * @param isEditMode whether it is in browse mode or edit mode
     * @param hasBorder whether it has a border
     */
    public ImageDisplay(final ImageIcon imgIcon, boolean isEditMode, boolean hasBorder)
    {
        super(new BorderLayout());
        setBorder(hasBorder ? new EtchedBorder(EtchedBorder.LOWERED) : BorderFactory.createEmptyBorder());

        this.isEditMode = isEditMode;
        imagePanel = new ImagePanel(imgIcon.getIconWidth(), imgIcon.getIconHeight());
        createUI();
        
        setImage(imgIcon);
    }

    /**
     *
     */
    protected void createUI()
    {
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:p:g" + (isEditMode ? ",2px,p" : ""), "p"));
        CellConstraints cc      = new CellConstraints();

        builder.add(imagePanel, cc.xy(1,1));

        if (isEditMode)
        {
           ImageIcon icon = IconManager.getImage("EditIcon", IconManager.IconSize.NonStd);

           editBtn = new JButton(icon);
           editBtn.setMargin(new Insets(1,1,1,1));
           editBtn.addActionListener(new ActionListener()
           {
               public void actionPerformed(ActionEvent ae)
               {
                   // XXX Need to add a filter for just images

                   JFileChooser chooser = new JFileChooser();

                   int returnVal = chooser.showOpenDialog(UICacheManager.get(UICacheManager.TOPFRAME));
                   if (returnVal == JFileChooser.APPROVE_OPTION)
                   {
                       File file = new File(chooser.getSelectedFile().getAbsolutePath());
                       try
                       {
                           url = file.toURL().toString();
                           loadImage();
                       } catch (MalformedURLException ex)
                       {
                           // XXX FIXME - Do pop up here ?
                       }

                   }
               }
           });
           builder.add(editBtn, cc.xy(3,1));
        }
       add(builder.getPanel(), BorderLayout.CENTER);
    }

    /**
     * @param imgIcon
     */
    public synchronized void setImage(final ImageIcon imgIcon)
    {
        this.imgIcon = imgIcon;

        if (imgIcon != null && imgIcon.getIconWidth() > 0 && imgIcon.getIconHeight() > 0)
        {
            imagePanel.setImage(imgIcon);

        } else
        {
            this.imgIcon = null;
            imagePanel.setImage(imgIcon);
            imagePanel.setNoImage(true);
        }
        imagePanel.repaint();

       doLayout();

    }


    /**
     *
     */
    protected void simpleLoad()
    {
        try
        {
            imagePanel.setNoImage(false); // means it is loading it

            //imgIcon = new ImageIcon(new URL(url));
            Image img = getToolkit().getImage(new URL(url));

            imgIcon = new ImageIcon(img);
            setImage(imgIcon);

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
            imagePanel.setNoImage(false); // means it is loading it
            repaint();

            if (getter != null)
            {
                getter.stop();
            }
            getter = new ImageGetter(url);
            getter.start();

        } else
        {
            imagePanel.setNoImage(true);
            imagePanel.setImage(null);
        }
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
            url = (String)value;
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

    //--------------------------------------------------------------
    //-- Inner Class JPanel for displaying an image
    //--------------------------------------------------------------
    class ImagePanel extends JComponent
    {
        protected ImageIcon imageIcon;
        protected Dimension preferredSize;
        protected String    noImageStr      = getResourceString("noimage");
        protected String    loadingImageStr = getResourceString("loadingimage");

        protected boolean   isNoImage = true;

        public ImagePanel(final int x, final int y)
        {
            this.imageIcon = null;
            preferredSize = new Dimension(x, y);
        }

        public ImagePanel(ImageIcon imageIcon)
        {
            this.imageIcon  = imageIcon;
        }

        public void setImage(final ImageIcon imageIcon)
        {
            this.imageIcon  = imageIcon;
        }

        @Override
        public Dimension getPreferredSize()
        {
            return preferredSize;
        }
        @Override
        public void paint(Graphics g)
        {
           super.paint(g);

           Dimension size = imagePanel.getSize();
           if (imageIcon != null)
           {

               Insets    insets = imagePanel.getInsets();

               int w = size.width - insets.left - insets.right;
               int h = size.height - insets.top - insets.bottom;

               g.setClip(insets.left, insets.top, w, h);
               int x = insets.left;
               int y = insets.top;

               int imgW = imageIcon.getIconWidth();
               int imgH = imageIcon.getIconHeight();

               if (imgW > w || imgH > h)
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
               g.drawString(label, (size.width - fm.stringWidth(label))/2, (size.height-fm.getAscent())/2);
           }

        }

        public void setNoImage(boolean isNoImage)
        {
            this.isNoImage = isNoImage;
        }

    }

    //--------------------------------------------------------------
    //-- Inner Class JPanel for displaying an image
    //--------------------------------------------------------------
    public class ImageGetter implements Runnable
    {

      // Error Code
      public int kNoError      = 0;
      public int kError        = 1;
      public int kHttpError    = 2;
      public int kNotDoneError = 3;
      public int kIOError      = 4;
      public int kURLError     = 5;

      private Thread        thread = null;
      private String        urlStr;


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
           //Thread me = Thread.currentThread();

           try
           {
               imagePanel.setNoImage(false); // means it is loading it

               //

               FileCache fileCache = UICacheManager.getLongTermFileCache();
               if (fileCache != null)
               {
                   File file = fileCache.getCacheFile(urlStr);
                   if (file == null)
                   {
                       String    fileName  = fileCache.cacheWebResource(urlStr);
                       file      = fileCache.getCacheFile(fileName);
                   }
                   Image     img       = getToolkit().getImage(file.toURL());
                   ImageIcon imageIcon = new ImageIcon(img);
    
                   setImage(imageIcon);
               }

               getter = null;

           } catch (Exception e)
           {
               //log.error(e);
               e.printStackTrace();
           }


           stop();
       }
    }


}
