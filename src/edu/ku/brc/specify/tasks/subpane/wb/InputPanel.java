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
import java.awt.Component;
import java.awt.Cursor;
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;
import edu.ku.brc.ui.dnd.ShadowFactory;
import edu.ku.brc.ui.validation.ValCheckBox;

/**
 * Simple panel that doies it's own layout of a JLabel (Caption) and a JComponent. It is draggable on a panel (canvas).
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Mar 9, 2007
 *
 */
public class InputPanel extends JPanel implements GhostActionable
{
    public static final DataFlavor INPUTPANEL_FLAVOR = new DataFlavor(InputPanel.class, "InputPanel");
    
    protected static final Cursor  handCursor        = new Cursor(Cursor.HAND_CURSOR);

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

    protected RolloverCommand        itself       = null; // for the mouse adapter

    protected List<DataFlavor>       dropFlavors  = new ArrayList<DataFlavor>();
    protected List<DataFlavor>       dragFlavors  = new ArrayList<DataFlavor>();
    
    protected int                    textFieldOffset = 0;
    
    /**
     * Constructs a panel with label and control.
     * @param wbtmi the mapping template info
     * @param label the label text
     * @param comp the control
     */
    public InputPanel(final WorkbenchTemplateMappingItem wbtmi, 
                      final String label, 
                      final JComponent comp)
    {
        setLayout(null);
        
        this.wbtmi = wbtmi;
        this.comp  = comp;
        
        if (comp instanceof ValCheckBox)
        {
            this.label = new JLabel("   ", SwingConstants.RIGHT);
        } else
        {
            this.label = new JLabel(label+":", SwingConstants.RIGHT);
        }

        this.label.setCursor(handCursor);
        add(this.label);
        add(this.comp);
        
        if (wbtmi.getXCoord() != null && wbtmi.getYCoord() != null && wbtmi.getXCoord() > -1 && wbtmi.getYCoord() > -1)
        {
            setLocation(wbtmi.getXCoord(), wbtmi.getYCoord());
        }
        doLayout();
        
        dragFlavors.add(INPUTPANEL_FLAVOR);
    }
    
    /**
     * Sets a new Component.
     * @param newComp the new component
     */
    public void setComp(JComponent newComp)
    {
        remove(comp);
        comp = newComp;
        add(comp);
        doLayout();
        repaint();
    }
    
    /* (non-Javadoc)
     * @see java.awt.Container#doLayout()
     */
    @Override
    public void doLayout()
    {
        Dimension labelSize = label.getPreferredSize();
        Dimension compSize  = comp.getPreferredSize();
        
        //Rectangle r = new Rectangle(0, (compSize.height - labelSize.height) / 2, labelSize.width, labelSize.height);
        Rectangle r = new Rectangle(0, 0, labelSize.width, compSize.height);
        label.setBounds(r);
        
        r.y      = 0;
        r.x      = r.width + 2;
        r.width  = compSize.width;
        r.height = compSize.height;
        
        comp.setBounds(r);
        
        textFieldOffset = r.x;
        
        setSize(r.x + r.width, r.height);
        setPreferredSize(new Dimension(r.x + r.width, r.height));
    }

    /**
     * Returns the number of pixels that the text field is positioned at horizontally.
     * @return the number of pixels that the text field is positioned at horizontally.
     */
    public int getTextFieldOffset()
    {
        return textFieldOffset;
    }
    
    /**
     * @return returns the control's label with no ":".
     */
    public String getControlTitle()
    {
        return wbtmi.getCaption();
    }
    
    /**
     * @return the comp
     */
    public Component getComp()
    {
        if (comp instanceof JScrollPane)
        {
            return ((JScrollPane)comp).getViewport().getView();
        }
        return comp;
    }
    
    public Component getScrollPane()
    {
        if (comp instanceof JScrollPane)
        {
            return comp;
        }
        return null;
    }
    
    public String getLabelText()
    {
        if (comp instanceof ValCheckBox)
        {
            return ((ValCheckBox)comp).getText();
        }
        // else
        return label.getText();
    }
    
    public void setLabelText(final String text)
    {
        if (comp instanceof ValCheckBox)
        {
            ((ValCheckBox)comp).setText(text);
        } else
        {
            label.setText(text);
        }
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
     * @see java.awt.Component#setLocation(int, int)
     */
    @Override
    public void setLocation(int x, int y)
    {
        super.setLocation(x, y);
    }

    /**
     * Adds an ActionListener.
     * @param al the listener to be added
     */
    public void addActionListener(ActionListener al)
    {
        listeners.add(al);
    }

    /**
     * Removes an ActionListener.
     * @param al the listener to be removed
     */
    public void removeActionListener(ActionListener al)
    {
        listeners.remove(al);
    }

    /**
     * Adds a new "drag" data flavor it's list of data flavors that it supports.
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


    /* (non-Javadoc)
     * @see javax.swing.JComponent#setPreferredSize(java.awt.Dimension)
     */
    @Override
    public void setPreferredSize(Dimension preferredSize)
    {
        buffer       = null;
        shadowBuffer = null;
        super.setPreferredSize(preferredSize);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setBounds(int, int, int, int)
     */
    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        buffer       = null;
        shadowBuffer = null;
        super.setBounds(x, y, width, height);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setBounds(java.awt.Rectangle)
     */
    @Override
    public void setBounds(Rectangle r)
    {
        buffer       = null;
        shadowBuffer = null;
        super.setBounds(r);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setSize(java.awt.Dimension)
     */
    @Override
    public void setSize(Dimension d)
    {
        buffer       = null;
        shadowBuffer = null;
        super.setSize(d);
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
        // no op
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
        mouseDropAdapter = new GhostMouseInputAdapter(UIRegistry.getGlassPane(), "action", this);
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

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setActive(boolean)
     */
    public void setActive(boolean isActive)
    {
        // TODO Auto-generated method stub
        
    }

    /**
     * Returns the width.
     * @return Returns the width
     */
    public int getItemWidth()
    {
        return getSize().width + 10;
    }

    /**
     * Returns the height.
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

