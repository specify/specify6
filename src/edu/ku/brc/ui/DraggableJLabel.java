/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;

import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;
import edu.ku.brc.ui.dnd.ShadowFactory;

public class DraggableJLabel extends JLabel implements GhostActionable
{
    // Static Data Members
    public static final DataFlavor DATAOBJREP_FLAVOR = new DataFlavor(DraggableJLabel.class, "DraggableJLabel");
    
    // These used for the Ghosting
    protected static final int SHADOW_SIZE = 10;
    
    //protected ImageIcon               imgIcon;
    //protected String                  label      = "";
    
    protected BufferedImage           sizeBufImg       = null;

    protected GhostMouseInputAdapter  mouseInputAdapter   = null;
    protected RenderingHints          hints               = null;
    protected BufferedImage           shadowBuffer        = null;
    protected BufferedImage           buffer              = null;
    protected boolean                 generateImgBuf      = true;    
    
    protected Object                  data                = null;

    protected List<DataFlavor>        dropFlavors  = new ArrayList<DataFlavor>();
    protected List<DataFlavor>        dragFlavors  = new ArrayList<DataFlavor>();
    
    
    public DraggableJLabel()
    {
        createMouseInputAdapter();
    }

    public DraggableJLabel(String text)
    {
        super(text);
        createMouseInputAdapter();
    }

    public DraggableJLabel(Icon image)
    {
        super(image);
        createMouseInputAdapter();
    }

    public DraggableJLabel(String text, int horizontalAlignment)
    {
        super(text, horizontalAlignment);
        createMouseInputAdapter();
    }

    public DraggableJLabel(Icon image, int horizontalAlignment)
    {
        super(image, horizontalAlignment);
        createMouseInputAdapter();
    }

    public DraggableJLabel(String text, Icon icon, int horizontalAlignment)
    {
        super(text, icon, horizontalAlignment);
        createMouseInputAdapter();
    }

    /* (non-Javadoc)
     * @see javax.swing.JLabel#setText(java.lang.String)
     */
    public void setText(String label)
    {
        String oldLabel = super.getText();
         super.setText(label);
        if (label == null || oldLabel == null || !oldLabel.equals(label))
        {
            generateImgBuf = true;   
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
        if (src != null)
        {
            
            //Object dataObj = src.getData();

        }
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setActive(boolean)
     */
    public void setActive(boolean isActive)
    {
        // Auto-generated method stub
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
     * Returns the width.
     * @return Returns the width
     */
    public int getItemWidth()
    {
        return getPreferredSize().width + 10;
    }

    /**
     * Returns the height.
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

        
        buffer = new BufferedImage(bgBufImg.getWidth(), bgBufImg.getHeight(), BufferedImage.TYPE_INT_ARGB);

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
        
        g2.translate(left, top);
        super.paint(g2);
        
        /*
        g2.drawImage(imgIcon.getImage(), left + 1, top + (height - imgIcon.getIconHeight())/2,
                     imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.BLACK);
        g2.drawString(label, left+1+imgIcon.getIconWidth()+1, top+((height-fm.getHeight())/2)+fm.getAscent());
        g2.dispose();
        */
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
