package edu.ku.brc.specify.ui;

import java.awt.*;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import javax.swing.*;

import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.ui.dnd.*;
import edu.ku.brc.specify.ui.dnd.GhostMouseDropAdapter;
import edu.ku.brc.specify.datamodel.*;

import edu.ku.brc.specify.ui.dnd.GhostActionable;

public class Trash extends JComponent implements GhostActionable
{
    // These used for the Ghosting
    protected static final int SHADOW_SIZE = 10;
    
    protected ImageIcon              imgIcon;
    protected GhostMouseDropAdapter  mouseDropAdapter = null;
    protected Object                 data         = null;
    protected RenderingHints         hints        = null;
    protected BufferedImage          shadowBuffer = null;
    protected BufferedImage          buffer       = null;;
    protected Dimension              prefferedRenderSize = new Dimension(0,0);
    protected boolean                verticalLayout      = false;

    public Trash()
    {
        imgIcon = new ImageIcon(IconManager.getImagePath("trash.png"));
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
        return new Dimension(imgIcon.getIconWidth(), imgIcon.getIconHeight());
    }
    
    //-----------------------------------------------
    // GhostActionable Interface
    //-----------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.dnd.GhostActionable#doAction(java.lang.Object)
     */
    public void doAction(Object data)
    {
        if (data instanceof RecordSet)
        {
            CommandDispatcher.dispatch(new CommandAction("Record_Set", "Delete", data));

        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.dnd.GhostActionable#setData(java.lang.Object)
     */
    public void setData(final Object data)
    {
        this.data = data;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.dnd.GhostActionable#getData()
     */
    public Object getData()
    {
        return data;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.dnd.GhostActionable#createMouseDropAdapter()
     */
    public void createMouseDropAdapter()
    {
        mouseDropAdapter = new GhostMouseDropAdapter(UICacheManager.getGlassPane(), "action", this);
        addMouseListener(mouseDropAdapter);
        addMouseMotionListener(new GhostMotionAdapter(UICacheManager.getGlassPane()));
    }
    
    /**
     * Returns the adaptor for tracking mouse drop gestures
     * @return Returns the adaptor for tracking mouse drop gestures
     */
    public GhostMouseDropAdapter getMouseDropAdapter()
    {
        return mouseDropAdapter;
    }
    
    /**
     * 
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
     * 
     */
    private void renderOffscreen() 
    {
        createRenderingHints();
        
        buffer = new BufferedImage(imgIcon.getIconWidth(), imgIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = (Graphics2D) buffer.createGraphics();
        g2.setRenderingHints(hints);

        g2.fillRect(0, 0, imgIcon.getIconWidth(), imgIcon.getIconHeight());
                
        g2.setClip(0, 0, imgIcon.getIconWidth(), imgIcon.getIconHeight());
        g2.drawImage(imgIcon.getImage(), 0, 0, imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);
        
        g2.dispose();
    }
    
    /**
     * 
     * @return
     */
    public BufferedImage getBufferedImage() 
    {
        
        if (buffer == null) 
        {
            renderOffscreen();
        }

        return buffer;
    }

}
