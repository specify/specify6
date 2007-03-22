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
package edu.ku.brc.specify.tasks.subpane.wb;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;
import edu.ku.brc.ui.dnd.ShadowFactory;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Mar 9, 2007
 *
 */
public class InputPanel extends JPanel implements GhostActionable
{
    public static final DataFlavor INPUTPANEL_FLAVOR      = new DataFlavor(InputPanel.class, "InputPanel");

    protected WorkbenchTemplateMappingItem wbtmi;
    protected JLabel     label;
    protected JComponent comp;
    
    // Ghosting
    protected BufferedImage          sizeBufImg       = null;
    protected Dimension              preferredSize    = new Dimension(0,0);
    protected Vector<ActionListener> listeners        = new Vector<ActionListener>();
    protected GhostMouseInputAdapter  mouseDropAdapter = null;
    
    protected static final int       SHADOW_SIZE  = 10;
    protected RenderingHints         hints        = null;
    protected BufferedImage          shadowBuffer = null;
    protected BufferedImage          buffer       = null;
    protected double                 ratio        = 0.0;
    protected Dimension              prefferedRenderSize = new Dimension(0,0);
    protected boolean                verticalLayout      = false;

    protected JPopupMenu             popupMenu    = null;
    protected RolloverCommand        itself       = null; // for the mouse adapter

    protected List<DataFlavor>       dropFlavors  = new ArrayList<DataFlavor>();
    protected List<DataFlavor>       dragFlavors  = new ArrayList<DataFlavor>();
    
    public InputPanel(WorkbenchTemplateMappingItem wbtmi, String label, JComponent comp)
    {
        CellConstraints cc         = new CellConstraints();
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:p:g,2px,f:p:g", "p"), this);
        
        this.wbtmi = wbtmi;
        this.label = new JLabel(label, JLabel.RIGHT);
        this.comp  = comp;
        
        builder.add(this.label, cc.xy(1, 1));
        builder.add(comp, cc.xy(3, 1));
        
        if (wbtmi.getXCoord() != null && wbtmi.getYCoord() != null)
        {
            setLocation(wbtmi.getXCoord(), wbtmi.getYCoord());
        }
    }
    
    /**
     * @return the comp
     */
    public JComponent getComp()
    {
        return comp;
    }
    
    /**
     * @return the label
     */
    public JLabel getLabel()
    {
        return label;
    }
    
    /**
     * @return the wbtmi
     */
    public WorkbenchTemplateMappingItem getWbtmi()
    {
        return wbtmi;
    }
    
    
    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    /*@Override
    public void paint(Graphics g)
    {
        if (isEnabled())
        {
            paintComp(g);

        } else
        {
            BufferedImage buf = new BufferedImage(getWidth(),getHeight(), BufferedImage.TYPE_INT_RGB);
            paintComp(buf.getGraphics());

            float[] my_kernel = {
                    0.10f, 0.10f, 0.10f,
                    0.10f, 0.20f, 0.10f,
                    0.10f, 0.10f, 0.10f };

                ConvolveOp op = new ConvolveOp(new Kernel(3,3, my_kernel));
                Image img = op.filter(buf,null);
                g.drawImage(img,0,0,null);
        }
    }*/

    /* (non-Javadoc)
     * @see java.awt.Component#setLocation(int, int)
     */
    @Override
    public void setLocation(int x, int y)
    {
        wbtmi.setXCoord((short)x);
        wbtmi.setYCoord((short)y);
        super.setLocation(x, y);
    }

    /**
     * Adds an ActionListener
     * @param al the listener to be added
     */
    public void addActionListener(ActionListener al)
    {
        listeners.add(al);
    }

    /**
     * Removes an ActionListener
     * @param al the listener to be removed
     */
    public void removeActionListener(ActionListener al)
    {
        listeners.remove(al);
    }

    /**
     * Adds a new "drag" data flavor it's list of data flavors that it supports
     * @param dataFlavor the new data flavor
     */
    public void addDragDataFlavor(final DataFlavor dataFlavor)
    {
        dragFlavors.add(dataFlavor);
    }

    /**
     * Adds a new "drop" data flavor it's list of data flavors that it supports
     * @param dataFlavor the new data flavor
     */
    public void addDropDataFlavor(final DataFlavor dataFlavor)
    {
        dropFlavors.add(dataFlavor);
    }

    //-----------------------------------------------
    // GhostActionable Interface
    // Note: Both GhostActionable and NavBoxItemIFace both have a get/set Data
    //-----------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#doAction(edu.ku.brc.ui.dnd.GhostActionable)
     */
    public void doAction(GhostActionable src)
    {
        // The drop has occurred and now we dispatch the event
        //DataActionEvent ae = new DataActionEvent(src, this, src != null ? src.getData() : null);
        //for (ActionListener al : listeners)
        //{
        //    al.actionPerformed(ae);
        //}
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setData(java.lang.Object)
     */
    public void setData(final Object data)
    {
        if (comp instanceof JTextField)
        {
            ((JTextField)comp).setText(data.toString());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getData()
     */
    public Object getData()
    {
        if (comp instanceof JTextField)
        {
            ((JTextField)comp).getText();
        }
        return "";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataForClass(java.lang.Class)
     */
    public Object getDataForClass(Class<?> classObj)
    {
        return UIHelper.getDataForClass(String.class, classObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#createMouseDropAdapter()
     */
    public void createMouseInputAdapter()
    {
        mouseDropAdapter = new GhostMouseInputAdapter(UICacheManager.getGlassPane(), "action", this);
        mouseDropAdapter.setPaintPositionMode(GhostGlassPane.ImagePaintMode.ABSOLUTE);
        mouseDropAdapter.setDoAnimationOnDrop(false);
        
        addMouseListener(mouseDropAdapter);
        addMouseMotionListener(mouseDropAdapter);
        label.addMouseListener(mouseDropAdapter);
        label.addMouseMotionListener(mouseDropAdapter);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getMouseInputAdapter()
     */
    public GhostMouseInputAdapter getMouseInputAdapter()
    {
        return mouseDropAdapter;
    }

    /**
     * Returns the width
     * @return Returns the width
     */
    public int getItemWidth()
    {
        return getSize().width + 10;
    }

    /**
     * Returns the height
     * @return Returns the height
     */
    public int getItemHeight()
    {
        return getSize().height;
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
            } catch (Exception e)
            {
                // do nothing
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

        Graphics2D g2 = buffer.createGraphics();
        g2.setRenderingHints(hints);

        paint(g2);
        
        g2.setColor(Color.GRAY);
        Rectangle r = getBounds();
        g2.drawRect(0, 0, r.width-1, r.height-1);
        
        g2.dispose();
    }

    /**
     * Returns the buffered image of the control
     * @return Returns the buffered image of the control
     */
    public BufferedImage getBufferedImage()
    {
        if (buffer == null)
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
        if (shadowBuffer == null)
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

