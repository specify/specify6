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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;

import edu.ku.brc.ui.dnd.DataActionEvent;
import edu.ku.brc.ui.dnd.DndDeletable;
import edu.ku.brc.ui.dnd.DragAndDropLock;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;
import edu.ku.brc.ui.dnd.ShadowFactory;
import edu.ku.brc.ui.dnd.Trash;

/**
 *
 * Creates a panel containing an icon and button with a focus "ring" when the mouse is hovering.
 * This class is used mostly in NavBoxes.<br>
 * <B><I>NOTE:</I></B> doAction is called if the Rbtn is clicked OR something is dropped on it (a Ghost drop).
 
 * @code_status Beta
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class RolloverCommand extends JPanel implements GhostActionable, DndDeletable
{
    protected static Color           transparentWhite = new Color(255, 255, 255, 180);
    protected static Color           focusColor  = Color.BLUE;
    protected static Color           activeColor = new Color(0, 150, 0, 100); 
    protected static Color           hoverColor  = new Color(0, 0, 150, 100);
    protected static Color           dropColor   = new Color(255, 140, 0, 100);
    
    protected static final int       ICON_TEXT_GAP  = 4;
    public static ImageIcon          hoverImg       = null;
    protected static Font            defaultFont    = null;
    protected static int             vertGap        = 0;      // the Old Default was 2
    protected static boolean         useEmptyBorder = false;
    
    protected JTextField             txtFld      = null;
    protected JLabel                 iconLabel;
    protected boolean                isEditing   = false;
    protected PropertyChangeListener pcl         = null;

    protected boolean                isOver      = false;
    protected Vector<ActionListener> actions     = new Vector<ActionListener>();

    protected ImageIcon              imgIcon     = null;
    protected String                 label       = "";

    protected BufferedImage          sizeBufImg       = null;
    protected Dimension              preferredSize    = new Dimension(0,0);
    protected Vector<ActionListener> listeners        = new Vector<ActionListener>();
    protected GhostMouseInputAdapter  mouseDropAdapter = null;

    protected Object                 data = null;

    // These used for the Ghosting
    protected static final int       SHADOW_SIZE = 10;
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
    protected boolean                isActive     = false;
    protected boolean			     isAccented   = false;
    protected Color				     backupColor  = null;
    // DndDeletable
    protected CommandAction          deleteCmdAction = null;

    /**
     * Constructs a UI component with a label and an icon which can be clicked to execute an action.
     */
    protected RolloverCommand()
    {
        // do nothing
    }

    /**
     * Constructs a UI component with a label and an icon which can be clicked to execute an action
     * @param label the text label for the UI
     * @param imgIcon the icon for the UI
     */
    public RolloverCommand(final String label, final ImageIcon imgIcon)
    {

        if (useEmptyBorder)
        {
            setBorder(new EmptyBorder(new Insets(2,2,2,2)));
        }
        
        setLayout(new BorderLayout());

        this.imgIcon = imgIcon;
        this.label   = label;

        iconLabel    = new JLabel(imgIcon);
        itself       = this;

        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
            protected boolean    wasPopUp = false;
            protected Point      downME   = null;
            
            @Override
            public void mouseEntered(MouseEvent e)
            {
                if (isEnabled())
                {
                    isOver = true;
                    repaint();
                    //UIRegistry.displayStatusBarText(itself.getToolTipText());
                }
            }
            @Override
            public void mouseExited(MouseEvent e)
            {
                isOver = false;
                repaint();
                //UIRegistry.displayStatusBarText("");
            }
            @Override
            public void mousePressed(MouseEvent e)
            {
                downME = e.getPoint();
                repaint();
                wasPopUp = e.isPopupTrigger();
                if (popupMenu != null && wasPopUp && itself.isEnabled())
                {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e)
            {
                repaint();
                
                Point pnt = e.getPoint();
                if (pnt != null && downME != null)
                {
                    boolean clicked = Math.abs(pnt.x - downME.x) < 4 && Math.abs(pnt.y - downME.y) < 4;
                    Rectangle r = RolloverCommand.this.getBounds();
                    r.x = 0;
                    r.y = 0;
                    if (!wasPopUp && clicked && RolloverCommand.this.isEnabled() && r.contains(e.getPoint()))
                    {
                        if (!e.isPopupTrigger())
                        {
                            doAction(RolloverCommand.this);
                            
                        } else if (popupMenu != null)
                        {
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            }

          };
        addMouseListener(mouseInputAdapter);
        addMouseMotionListener(mouseInputAdapter);
        
        addFocusListener(new FocusAdapter()
        {
            public void focusLost(FocusEvent e)
            {
                repaint();
            }

        });
    }

    public boolean isAccented() {
    	return isAccented;
    }
    
    public void setIsAccented(boolean val) {
    	if (isAccented != val) {
    		isAccented = val;
    		if (isAccented) {
    			if (backupColor == null) {
    				backupColor = getForeground();
    			}
    			setForeground(getFocusColor());
    		} else {
    			setForeground(backupColor);
    		}
    	}
    }
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setBackground(java.awt.Color)
     */
    @Override
    public void setBackground(Color bg)
    {
        super.setBackground(bg);
        
        if (iconLabel != null)
        {
            iconLabel.setBackground(bg);
        }
    }

    /**
     * Sets the popup menu
     * @param popupMenu the pm
     */
    public void setPopupMenu(final JPopupMenu popupMenu)
    {
        this.popupMenu = popupMenu;
    }

    /**
     * @return the text part of the button
     */
    public String getLabelText()
    {
        return label;
    }
    
    /**
     * @param label
     */
    public void setLabelText(final String label)
    {
        if (label != null && !label.equals(this.label))
        {
            sizeBufImg = null;
        }   
        this.label = label;
        invalidate();
    }

    /**
     * @return the popup menu (right click)
     */
    public JPopupMenu getPopupMenu()
    {
        return popupMenu;
    }

    /**
     * Calculates the preferred size for initial painting and layout
     */
    protected void calcPreferredSize()
    {
        if (sizeBufImg == null)
        {
            Insets ins = getBorder() != null ? getBorder().getBorderInsets(this) : new Insets(0,0,0,0);
            sizeBufImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = sizeBufImg.createGraphics();
            if (defaultFont != null)
            {
                g.setFont(defaultFont);
            } else
            {
                g.setFont(getFont());
            }
            FontMetrics fm     = g.getFontMetrics();

            if (verticalLayout)
            {
                preferredSize.width  = ins.left + ins.right + 
                                       Math.max((label != null ? fm.stringWidth(label) : 0), (imgIcon != null ? (imgIcon.getIconWidth() + 2) : 0));
                preferredSize.height = ins.top + ins.bottom + 
                                       (label != null ? fm.getHeight()+4 : 0) + (imgIcon != null ? (imgIcon.getIconHeight() + vertGap) : 0);
            } else
            {
                preferredSize.width  = ins.left + ins.right + ICON_TEXT_GAP + 
                                       ((label != null ? fm.stringWidth(label) : 0)+2) + (imgIcon != null ? imgIcon.getIconWidth() : 0);
                preferredSize.height = ins.top + ins.bottom + 
                                       (Math.max(fm.getHeight(), (imgIcon != null ? (imgIcon.getIconHeight() + 2) : 0)));
            }
            g.dispose();
        }
    }

    /**
     * @return the defaultFont
     */
    public static Font getDefaultFont()
    {
        return defaultFont;
    }

    /**
     * @param defaultFont the defaultFont to set
     */
    public static void setDefaultFont(Font defaultFont)
    {
        RolloverCommand.defaultFont = defaultFont;
    }

    public static void setVertGap(int vertGap)
    {
        RolloverCommand.vertGap = vertGap;
    }

    public static void setUseEmptyBorder(boolean useEmptyBorder)
    {
        RolloverCommand.useEmptyBorder = useEmptyBorder;
    }

    /**
     *
     */
    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        calcPreferredSize();
        return new Dimension(preferredSize);
    }

    /**
     * Stops the editing of the name.
     * It will accept any input that has already been typed, but it will not allow for a zero length string.
     * It swaps out the text field and swaps in the label.
     *
     */
    protected void stopEditting()
    {
        if (txtFld != null || isEditing)
        {
            String oldLabel = label;
            
            label = txtFld.getText();
            txtFld.setVisible(false);
            remove(txtFld);
            remove(iconLabel);
            
            if (pcl != null)
            {
                pcl.propertyChange(new PropertyChangeEvent(this, "label", oldLabel, label));
                pcl = null;
            }

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
     * @param pclArg the property change listener that is notified of the change.
     */
    public void startEditting(final PropertyChangeListener pclArg)
    {
        this.pcl = pclArg;
        
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
            public void focusGained(FocusEvent e)
            {
                // do nothing
            }
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
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        
        for (int i=0;i<getComponentCount();i++)
        {
            getComponent(i).setEnabled(enabled);
        }
    }

    /**
     * Helper function that paint the component.
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
                    if (defaultFont != null)
                    {
                        g.setFont(defaultFont);
                    }
                    FontMetrics fm = g.getFontMetrics();
                    g.setColor(getForeground());
                    ((Graphics2D)g).setRenderingHints(UIHelper.createTextRenderingHints());
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
                    xOffset = imgIcon.getIconWidth() + ICON_TEXT_GAP;
                }

                if (label != null)
                {
                    if (defaultFont != null)
                    {
                        g.setFont(defaultFont);
                    }
                    FontMetrics fm = g.getFontMetrics();
                    g.setColor(getForeground());
                    ((Graphics2D)g).setRenderingHints(UIHelper.createTextRenderingHints());
                    g.drawString(label, x+xOffset+1, y+((size.height-fm.getHeight())/2)+fm.getAscent());
                }

            }


            if ((isActive || (!DragAndDropLock.isDragAndDropStarted() && isOver)) && !this.hasFocus())
            {
                Color color;
                if (isActive)
                {
                    color = DragAndDropLock.isDragAndDropStarted() && isOver ? dropColor : activeColor;
                } else
                {
                    Color mouseOverColor = dragFlavors.size() > 0 ? activeColor : hoverColor;
                    color = (this.hasFocus() && UIManager.getLookAndFeel() instanceof PlasticLookAndFeel) ? PlasticLookAndFeel.getFocusColor() :mouseOverColor;
                }
                g.setColor(color);
                
                if (!useEmptyBorder)
                {
                    insets.set(1, 1, 1, 1);
                }
                //g.drawRect(insets.left, insets.top, size.width-insets.right-insets.left, size.height-insets.bottom-insets.top);
                Graphics2D g2d = (Graphics2D)g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                //g2d.setRenderingHints(hints);
                RoundRectangle2D.Double rr = new RoundRectangle2D.Double(insets.left, insets.top, size.width-insets.right-insets.left, size.height-insets.bottom-insets.top, 10, 10);
                g2d.draw(rr);
                rr = new RoundRectangle2D.Double(insets.left+1, insets.top+1, size.width-insets.right-insets.left-2, size.height-insets.bottom-insets.top-2, 10, 10);
                g2d.draw(rr);
            }
            
            if (isOver && hoverImg != null && isActive && DragAndDropLock.isDragAndDropStarted())
            {
                int x = size.width  - hoverImg.getIconWidth() -1;
                int y = (size.height - hoverImg.getIconHeight()) / 2;
                g.drawImage(hoverImg.getImage(), x, y, null);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        paintComp(g);
        
        if (!isEnabled())
        {
            Graphics2D g2d = (Graphics2D)g;
            Dimension size = getSize();
            g2d.setPaint(transparentWhite);
            g2d.fillRect(0, 0, size.width, size.height);
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

    
    /**
     * @return the hoverImg
     */
    public static ImageIcon getHoverImg()
    {
        return hoverImg;
    }

    /**
     * @param hoverImg the hoverImg to set
     */
    public static void setHoverImg(final ImageIcon hoverImgArg)
    {
        RolloverCommand.hoverImg = hoverImgArg;
    }

    /**
     * @return the activeColor
     */
    public static Color getActiveColor()
    {
        return activeColor;
    }

    /**
     * @param activeColor the activeColor to set
     */
    public static void setActiveColor(Color activeColor)
    {
        RolloverCommand.activeColor = activeColor;
    }

    /**
     * @return the dropColor
     */
    public static Color getDropColor()
    {
        return dropColor;
    }

    /**
     * @param dropColor the dropColor to set
     */
    public static void setDropColor(Color dropColor)
    {
        RolloverCommand.dropColor = dropColor;
    }

    /**
     * @return the focusColor
     */
    public static Color getFocusColor()
    {
        return focusColor;
    }

    /**
     * @param focusColor the focusColor to set
     */
    public static void setFocusColor(Color focusColor)
    {
        RolloverCommand.focusColor = focusColor;
    }

    /**
     * @return the hoverColor
     */
    public static Color getHoverColor()
    {
        return hoverColor;
    }

    /**
     * @param hoverColor the hoverColor to set
     */
    public static void setHoverColor(Color hoverColor)
    {
        RolloverCommand.hoverColor = hoverColor;
    }

    /**
     * @return
     */
    public JComponent getUIComponent()
    {
        return this;
    }

    /**
     * @return
     */
    public String getTitle()
    {
        return label;
    }

    /**
     * @param toolTip
     */
    public void setToolTip(final String toolTip)
    {
        super.setToolTipText(toolTip);
    }
    
    /**
     * @param icon
     */
    public void setIcon(ImageIcon icon)
    {
        if (icon != null)
        {
            imgIcon = icon;
            if (iconLabel != null)
            {
                iconLabel.setIcon(imgIcon);
            }
            repaint();
        }
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
        DataActionEvent ae = new DataActionEvent(src, this, src != null ? src.getData() : null);
        for (ActionListener al : listeners)
        {
            al.actionPerformed(ae);
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
    public Object getDataForClass(Class<?> classObj)
    {
        return UIHelper.getDataForClass(data, classObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#createMouseDropAdapter()
     */
    public void createMouseInputAdapter()
    {
        mouseDropAdapter = new GhostMouseInputAdapter(UIRegistry.getGlassPane(), "action", this);
        addMouseListener(mouseDropAdapter);
        addMouseMotionListener(mouseDropAdapter);
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
        this.isActive = isActive;
        repaint();
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
        if (imgIcon != null)
        {
            g2.drawImage(imgIcon.getImage(), left + 1, top + (height - imgIcon.getIconHeight())/2,
                        imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);
        }

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.BLACK);
        if (label != null)
        {
            if (imgIcon != null)
            {
                g2.drawString(label, left+1+imgIcon.getIconWidth()+1+ICON_TEXT_GAP, top+((height-fm.getHeight())/2)+fm.getAscent());
            } else
            {
                g2.drawString(label, left+1, top+((height-fm.getHeight())/2)+fm.getAscent());
            }
        }
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

    /**
     * Returns the layout orientation
     * @return Returns the layout orientation
     */
    public boolean isVerticalLayout()
    {
        return verticalLayout;
    }

    /**
     * Set the layout orientation
     * @param verticalLayout true - vertical, false - horizontal
     */
    public void setVerticalLayout(boolean verticalLayout)
    {
        this.verticalLayout = verticalLayout;
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
    	if (!isAccented) {
    		return dragFlavors;
    	} else {
    		List<DataFlavor> result = new ArrayList<DataFlavor>();
    		for (DataFlavor flavor : dragFlavors) {
    			if (flavor != Trash.TRASH_FLAVOR) {
    				result.add(flavor);
    			}
    		}
    		return result;
    	}
    }

    //-----------------------------------------------
    // DndDeletable Interface
    //-----------------------------------------------

    /**
     * Asks the object to send a request for itself to be deleted
     * @return return true if deleted, false if it couldn't be deleted
     */
    public boolean deleteRequest()
    {
        if (deleteCmdAction != null)
        {
            CommandDispatcher.dispatch(deleteCmdAction);
        }
        return true;
    }

    /**
     * Returns an XML string for cahing the entire contents of the object to be serialized
     * @return Returns an XML string for cahing the entire contents of the object to be serialized
     */
    public String toXML()
    {
        return null;
    }

    /**
     * The command that will be dispatched when "deleteRequest" is called
     * @param deleteCmdAction the command that will delete the object from itself container
     */
    public void setDeleteCommandAction(final CommandAction deleteCmdAction)
    {
        if (deleteCmdAction.getSrcObj() == null)
        {
            deleteCmdAction.setSrcObj(this);
        }
        this.deleteCmdAction = deleteCmdAction;
    }

    /**
     * The name of the object being deleted
     * @return The name of the object being deleted
     */
    @Override
    public String getName()
    {
        return label;
    }

    /**
     * An icon that is representative of the item
     * @return An icon that is representative of the item
     */
    public Icon getIcon()
    {
        return imgIcon;
    }

}
