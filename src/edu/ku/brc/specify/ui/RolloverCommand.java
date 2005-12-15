/* Filename:    $RCSfile: RolloverCommand.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui;

import java.awt.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;

import java.awt.dnd.*;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;

import edu.ku.brc.specify.core.NavBoxItemIFace;
import edu.ku.brc.specify.ui.dnd.DataActionEvent;
import edu.ku.brc.specify.ui.dnd.GhostActionable;
import edu.ku.brc.specify.ui.dnd.GhostMotionAdapter;
import edu.ku.brc.specify.ui.dnd.GhostMouseDropAdapter;
import edu.ku.brc.specify.ui.dnd.ShadowFactory;
import edu.ku.brc.specify.ui.dnd.GhostActionableDropManager;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;

/**
 * @author Rod Spears
 *
 *  
 * Creates a panel containing an icon and button with a focus "ring" when the mouse is hovering.
 * This class is used mostly in NavBoxes
 */
public class RolloverCommand extends JPanel implements NavBoxItemIFace, GhostActionable
{
    protected JTextField             txtFld     = null;   
    protected JLabel                 iconLabel;
    protected boolean                isEditing  = false;
    
    protected boolean                isOver     = false;
    protected static Color           focusColor = Color.BLUE;
    protected Vector<ActionListener> actions    = new Vector<ActionListener>();
    
    protected ImageIcon              imgIcon    = null;
    protected String                 label      = "";

    protected BufferedImage          sizeBufImg       = null; 
    protected Dimension              preferredSize    = new Dimension(0,0);
    protected Vector<ActionListener> listeners        = new Vector<ActionListener>();
    protected GhostMouseDropAdapter  mouseDropAdapter = null;
    
    protected Object                 data = null;
    
    // These used for the Ghosting
    protected static final int SHADOW_SIZE = 10;
    protected RenderingHints         hints;
    protected BufferedImage          shadowBuffer = null;
    protected BufferedImage          buffer       = null;;
    protected double                 ratio        = 0.0;
    protected Dimension              prefferedRenderSize = new Dimension(0,0);
    protected boolean                verticalLayout = false;
    
    /**
     * Constructs a UI component with a label and an icon which can be clicked to execute an action
     * @param label the text label for the UI
     * @param imgIcon the icon for the UI
     */
    public RolloverCommand(final String label, final ImageIcon imgIcon)
    {
        setBorder(new EmptyBorder(new Insets(1,1,1,1)));
        setLayout(new BorderLayout());
       
        this.imgIcon = imgIcon;
        this.label   = label;
        
        iconLabel    = new JLabel(imgIcon);        
        
        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
            public void mouseEntered(MouseEvent e) 
            {
                isOver = true;
                repaint();
            }
            public void mouseExited(MouseEvent e) 
            {
                isOver = false;
                repaint();
            }
            public void mouseClicked(MouseEvent e) 
            {
                repaint();
                doAction(null);
            }
          };
        addMouseListener(mouseInputAdapter);
        addMouseMotionListener(mouseInputAdapter);
          
        ActionListener actionListener = new ActionListener() {
              public void actionPerformed(ActionEvent actionEvent) {
                  startEditting();
              }
            };
            
        if (label != null)
        {
            final JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem renameMenuItem = new JMenuItem("Rename"); // XXX Localize
            renameMenuItem.addActionListener(actionListener);
            popupMenu.add(renameMenuItem);
            MouseListener mouseListener = new MouseAdapter() {
                  private void showIfPopupTrigger(MouseEvent mouseEvent) {
                    if (mouseEvent.isPopupTrigger() && popupMenu.getComponentCount() > 0) {
                      popupMenu.show(mouseEvent.getComponent(),
                        mouseEvent.getX(),
                        mouseEvent.getY());
                    }
                  }
                  public void mousePressed(MouseEvent mouseEvent) {
                    showIfPopupTrigger(mouseEvent);
                  }
                  public void mouseReleased(MouseEvent mouseEvent) {
                    showIfPopupTrigger(mouseEvent);
                  }
                };
                //iconLabel.addMouseListener (mouseListener);            
            addMouseListener (mouseListener);        
        }
    }
    
    /**
     * Calculates the preferred size for initial painting and layout
     *
     */
    protected void calcPreferredSize()
    {
        if (sizeBufImg == null)
        {
            Insets ins = getBorder().getBorderInsets(this);
            sizeBufImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = sizeBufImg.createGraphics();
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            Insets insets = getInsets();
            
            if (verticalLayout)
            {
                preferredSize.width  = ins.left + ins.right + insets.left + insets.right + 
                                       Math.max((label != null ? fm.stringWidth(label) : 0), (imgIcon != null ? (imgIcon.getIconWidth() + 2) : 0));
                preferredSize.height = ins.top + ins.bottom + insets.top + insets.bottom + 
                                       (label != null ? fm.getHeight() : 0) + (imgIcon != null ? (imgIcon.getIconHeight() + 2) : 0);
            } else
            {
                preferredSize.width  = ins.left + ins.right + insets.left + insets.right + 
                                       ((label != null ? fm.stringWidth(label) : 0)+2) + (imgIcon != null ? imgIcon.getIconWidth() : 0);
                preferredSize.height = ins.top + ins.bottom + insets.top + insets.bottom + 
                                       (Math.max(fm.getHeight(), (imgIcon != null ? (imgIcon.getIconHeight() + 2) : 0)));
               
            }
        }
    }
    
    /**
     * 
     */
    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize()
    {
        calcPreferredSize();
        return new Dimension(preferredSize);
    }
    
    /**
     * Stops the editting of the name. 
     * It will accept any input that has already been typed, but it will not allow for a zero length string.
     * It sawps out the text field and swpas in the label.
     *
     */
    protected void stopEditting()
    {
        if (txtFld != null || isEditing)
        {
            label = txtFld.getText();
            txtFld.setVisible(false);
            remove(txtFld);
            remove(iconLabel);
            
            txtFld = null;
            
            buffer     = null; // remove the current rendered image so a new one can be created.
            sizeBufImg = null;
            
            invalidate();
            getParent().doLayout();
            getParent().repaint();
            isEditing = false;
        }
    }
    
    /**
     * Start the editing of the name. It swaps out the label with a text field to enable the user to type in a new name.
     *
     */
    protected void startEditting()
    {
        //btn.setVisible(false);
        //remove(btn);
        
        txtFld = new JTextField(label);
        add(iconLabel, BorderLayout.WEST);
        add(txtFld, BorderLayout.CENTER);

        
        txtFld.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (txtFld.getText().length() > 0)
                {
                    stopEditting();
                }
                
            }
          });
        
        txtFld.addFocusListener(new FocusListener() 
        {
            public void focusGained(FocusEvent e) {}
            public void focusLost(FocusEvent e) 
            {
                stopEditting();
            }
            
        });
        
        isEditing = true;
        
        invalidate();
        doLayout();
        repaint();

    }
    
    /**
     * Helper function that paint the component
     * @param g graphics
     */
    protected void paintComp(Graphics g)
    {
        super.paint(g);
        
        if (!isEditing)
        {
            Insets    insets = getInsets();
            Dimension size   = getSize();
            
            if (verticalLayout)
            {
                int y = insets.top;
                
                if (imgIcon != null && imgIcon.getImage() != null)
                {
                    g.drawImage(imgIcon.getImage(), (size.width-imgIcon.getIconWidth())/2, y, 
                                imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);
                    y += imgIcon.getIconHeight() + 1;
                }
                
                if (label != null)
                {
                    g.setFont(getFont());
                    FontMetrics fm = g.getFontMetrics();
                    g.setColor(getForeground());
                    
                    g.drawString(label, (size.width - fm.stringWidth(label))/2, y+fm.getHeight());
                }
          
            } else
            {
                int x = insets.left + 1;
                int y = insets.top;
                
                int xOffset = 0;
                if (imgIcon != null && imgIcon.getImage() != null)
                {
                    if (label == null)
                    {
                        x = (size.width - imgIcon.getIconWidth()) / 2;
                    }
                    g.drawImage(imgIcon.getImage(), x, y + (size.height - imgIcon.getIconHeight())/2, 
                                imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);
                    xOffset = imgIcon.getIconWidth();
                }
           
                if (label != null)
                {
                    g.setFont(getFont());
                    FontMetrics fm = g.getFontMetrics();
                    g.setColor(getForeground());
                    
                    g.drawString(label, x+xOffset+1, y+((size.height-fm.getHeight())/2)+fm.getAscent());
                }
                
            }
             
            
            if (isOver && !this.hasFocus())
            {
                 g.setColor(UIManager.getLookAndFeel() instanceof PlasticLookAndFeel ? PlasticLookAndFeel.getFocusColor() : Color.BLUE);
                 g.drawRect(insets.left, insets.top, size.width-insets.right-insets.left, size.height-insets.bottom-insets.top);
                
            }
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
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
   
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.NavBoxItemIFace#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }

    
    //-----------------------------------------------
    // GhostActionable Interface
    //-----------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.dnd.GhostActionable#doAction(java.lang.Object)
     */
    public void doAction(Object data)
    {
        DataActionEvent ae = new DataActionEvent(this, data);
        for (ActionListener al : listeners)
        {
            al.actionPerformed(ae);
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
     * @return
     */
    public int getItemWidth() 
    {
        return getPreferredSize().width + 10;
    }
    
    /**
     * 
     * @return
     */
    public int getItemHeight() 
    {
        return getPreferredSize().height;
    }
    
    /**
     * 
     *
     */
    private void createRenderingHints() 
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
    
     /**
     * 
     */
    private void initForRendering()
    {
        if (ratio == 0.0)
        {
            this.ratio = 100.0 / 30.0;//(double) image.getWidth() / (double) image.getHeight();
            createRenderingHints();
        }
    }
    
    /**
     * 
     */
    private void renderOffscreen() 
    {
        initForRendering();
        BufferedImage bgBufImg = getBackgroundImageBuffer();
        
        buffer = new BufferedImage(bgBufImg.getWidth(),bgBufImg.getHeight(), BufferedImage.TYPE_INT_ARGB);

        int shadowWidth  = bgBufImg.getWidth() - getItemWidth();
        int shadowHeight = bgBufImg.getHeight() - getItemHeight();

        int left   = (int)(((double)shadowWidth) * 0.5);
        int top    = (int)(((double)shadowHeight)* 0.4);
        int width  = getItemWidth() - 2;
        int height = getItemHeight() - 2;
        
        Graphics2D g2 = (Graphics2D) buffer.createGraphics();
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
    
    /**
     * Returns the BufferedImage of a background shadow. I creates a large rectangle than the orignal image.
     * @return Returns the BufferedImage of a background shadow. I creates a large rectangle than the orignal image.
     */
    private BufferedImage getBackgroundImageBuffer() 
    {
        if (shadowBuffer == null) 
        {
            initForRendering();
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

    public boolean isVerticalLayout()
    {
        return verticalLayout;
    }

    public void setVerticalLayout(boolean verticalLayout)
    {
        this.verticalLayout = verticalLayout;
    }

    
}
