/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.ui.forms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;
import edu.ku.brc.ui.dnd.ShadowFactory;

/**
 * Implements a
 *
 * @code_status Alpha
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class DraggableRecordIdentifier extends JComponent implements GhostActionable
{
    // Static Data Members
    public static final DataFlavor DATAOBJREP_FLAVOR = new DataFlavor(DraggableRecordIdentifier.class, "DraggableRecordIdentifier");
    
    // These used for the Ghosting
    protected static final int SHADOW_SIZE = 10;
    
    protected ImageIcon               imgIcon;
    protected String                  label               = "";
    
    protected BufferedImage           sizeBufImg          = null;
    protected Dimension               preferredSize       = new Dimension(0,0);


    protected GhostMouseInputAdapter  mouseInputAdapter   = null;
    protected RenderingHints          hints               = null;
    protected BufferedImage           shadowBuffer        = null;
    protected BufferedImage           buffer              = null;
    protected boolean                 generateImgBuf      = true;    
    protected Dimension               prefferedRenderSize = new Dimension(0,0);
    protected boolean                 verticalLayout      = false;
    
    protected Color                   textColor           = new Color(0,0,0, 90);
    protected Font                    textFont            = null;
    
    protected Object                  data                = null;
    protected FormDataObjIFace        formDataObj         = null;

    protected List<DataFlavor>        dropFlavors         = new ArrayList<DataFlavor>();
    protected List<DataFlavor>        dragFlavors         = new ArrayList<DataFlavor>();

    /**
     * Constructor.
     * @param icon the icon
     * @param label the label
     */
    public DraggableRecordIdentifier(final ImageIcon icon, final String label)
    {
        
        imgIcon = icon;
        
        createMouseInputAdapter(); // this makes it draggable
        setData(null);
    }
    
    /**
     * Constructor
     */
    public DraggableRecordIdentifier(final ImageIcon icon)
    {
        this(icon, null);
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        
        g.drawImage(imgIcon.getImage(), 0, 0, imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);
    }
    
   /* (non-Javadoc)
    * @see java.awt.Component#getPreferredSize()
    */
   public Dimension getPreferredSize()
   {
       calcPreferredSize();
       return new Dimension(preferredSize);
   }
    
    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        if (label == null || this.label == null || !this.label.equals(label))
        {
            generateImgBuf = true;   
        }
        this.label = label;
    }
    
    /**
     * Calculates the preferred size for initial painting and layout
     *
     */
    protected void calcPreferredSize()
    {
        if (sizeBufImg == null)
        {
            Insets ins = getBorder() != null ? getBorder().getBorderInsets(this) : new Insets(0,0,0,0);
            //Insets ins = new Insets(0,0,0,0);
            sizeBufImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D g = sizeBufImg.createGraphics();
            g.setFont(getFont());
            
            FontMetrics fm     = g.getFontMetrics();
            Insets      insets = getInsets();

            int strWidth  = label != null ? fm.stringWidth(label) : 0;
            int strHeight = label != null ? fm.getHeight() : 0;
            if (verticalLayout)
            {
                preferredSize.width  = ins.left + ins.right + insets.left + insets.right +
                                       Math.max(strWidth, (imgIcon != null ? (imgIcon.getIconWidth() + 2) : 0));
                preferredSize.height = ins.top + ins.bottom + insets.top + insets.bottom +
                                       strHeight + (imgIcon != null ? (imgIcon.getIconHeight() + 2) : 0);
            } else
            {
                preferredSize.width  = ins.left + ins.right + insets.left + insets.right +
                                       (strWidth+2) + (imgIcon != null ? imgIcon.getIconWidth() : 0);
                
                preferredSize.height = ins.top + ins.bottom + insets.top + insets.bottom +
                                       (Math.max(fm.getHeight(), (imgIcon != null ? (imgIcon.getIconHeight() + 2) : 0)));

            }
        }
    }


    /**
     * Returns the FormDataObjIFace.
     * @return the FormDataObjIFace
     */
    public FormDataObjIFace getFormDataObj()
    {
        return formDataObj;
    }

    /**
     * Sets the the FormDataObjIFace.
     * @param formDataObj thee new 
     */
    public void setFormDataObj(FormDataObjIFace formDataObj)
    {
        if (this.formDataObj != formDataObj)
        {
            this.formDataObj = formDataObj;
        }
    }
    
    //-----------------------------------------------
    // GhostActionable Interface
    //-----------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#doAction(edu.ku.brc.ui.dnd.GhostActionable)
     */
    public void doAction(GhostActionable src)
    {

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setData(java.lang.Object)
     */
    public void setData(final Object data)
    {
        this.data = data;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getData()
     */
    public Object getData()
    {
        return data;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataForClass(java.lang.Class)
     */
    public Object getDataForClass(Class classObj)
    {
        return UIHelper.getDataForClass(data, classObj);
    }
   
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#createMouseInputAdapter()
     */
    public void createMouseInputAdapter()
    {
        mouseInputAdapter = new GhostMouseInputAdapter(UICacheManager.getGlassPane(), "action", this);
        addMouseListener(mouseInputAdapter);
        addMouseMotionListener(mouseInputAdapter);
    }
    
    /**
     * Returns the adaptor for tracking mouse drop gestures
     * @return Returns the adaptor for tracking mouse drop gestures
     */
    public GhostMouseInputAdapter getMouseInputAdapter()
    {
        return mouseInputAdapter;
    }
    
    /**
     * Returns the width
     * @return Returns the width
     */
    public int getItemWidth()
    {
        return getPreferredSize().width + 10;
    }

    /**
     * Returns the height
     * @return Returns the height
     */
    public int getItemHeight()
    {
        return getPreferredSize().height;
    }

    /**
     * Initialize rendering hints
     *
     */
    private void createRenderingHints()
    {
        if (hints == null)
        {
            hints = new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                                       RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            Object value = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
            try {
                Field declaredField = RenderingHints.class.getDeclaredField("VALUE_TEXT_ANTIALIAS_LCD_HRGB");
                value = declaredField.get(null);
            } catch (Exception e) {
            }
            hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, value);
        }
    }

    /**
     * Render the control to a buffer
     */
    private void renderOffscreen()
    {
        
        createRenderingHints();
        BufferedImage bgBufImg = getBackgroundImageBuffer();

        buffer = new BufferedImage(bgBufImg.getWidth(),bgBufImg.getHeight(), BufferedImage.TYPE_INT_ARGB);

        int shadowWidth  = bgBufImg.getWidth() - getItemWidth();
        int shadowHeight = bgBufImg.getHeight() - getItemHeight();

        int left   = (int)((shadowWidth) * 0.5);
        int top    = (int)((shadowHeight)* 0.4);
        int width  = getItemWidth() - 2;
        int height = getItemHeight() - 2;

        Graphics2D g2 = buffer.createGraphics();
        g2.setRenderingHints(hints);

        g2.drawImage(bgBufImg, 0, 0, bgBufImg.getWidth(), bgBufImg.getHeight(), null);

        g2.fillRect(left, top, width, height);

        g2.setClip(left, top, width, height);
        g2.drawImage(imgIcon.getImage(), left + 1, top + (height - imgIcon.getIconHeight())/2,
                     imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.BLACK);
        g2.drawString(label, left+1+imgIcon.getIconWidth()+1, top+((height-fm.getHeight())/2)+fm.getAscent());
        g2.dispose();
    }

    /**
     * Returns the buffered image of the control
     * @return Returns the buffered image of the control
     */
    public BufferedImage getBufferedImage()
    {
        if (buffer == null || generateImgBuf)
        {
            renderOffscreen();
        }

        return buffer;
    }

    /**
     * Returns the BufferedImage of a background shadow. I creates a large rectangle than the orignal image.
     * @return Returns the BufferedImage of a background shadow. I creates a large rectangle than the orignal image.
     */
    private BufferedImage getBackgroundImageBuffer()
    {
        if (shadowBuffer == null || generateImgBuf)
        {
            createRenderingHints();
            ShadowFactory factory = new ShadowFactory(SHADOW_SIZE, 0.17f, Color.BLACK);

            BufferedImage image = new BufferedImage(getItemWidth(), getItemHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = image.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2.dispose();

            shadowBuffer = factory.createShadow(image);
        }
        return shadowBuffer;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataFlavor()
     */
    public List<DataFlavor> getDropDataFlavors()
    {
        return dropFlavors;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataFlavor()
     */
    public List<DataFlavor> getDragDataFlavors()
    {
        return dragFlavors;
    }
}
