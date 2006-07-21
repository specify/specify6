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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Toolbar button derived from DropDownBtn, this provides a way to set menu items
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ColorChooser extends JButton implements AncestorListener, GetSetValueIFace
{
    protected JComponent drop_down_comp;
    protected JComponent visible_comp;
    protected Color      color;
    protected JWindow    popup;
    protected ColorSelectionPanel colorPanel;
    protected ColorChooser itself;
    
    
    
     /**
     * Creates a toolbar item with label and icon and their positions.
     * @param color the initial awt Color
     */
    public ColorChooser(Color color)
    {
        super();
        
        this.color   = color;
        visible_comp = this;
        itself       = this;
        
        setBackground(color);
        setPreferredSize(new Dimension(16,16));
        
        init();
    }
    
    public void paint(Graphics g)
    {
        g.setColor(getBackground());
        Dimension size = getSize();
        g.fillRect(0,0,size.width,size.height);
        g.setColor(Color.BLACK);
        g.drawRect(0,0,size.width-1,size.height-1);
    }


    protected void init()
    {
        drop_down_comp = new ColorSelectionPanel();      
        drop_down_comp.addPropertyChangeListener("selectedColor",new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) 
            {
                itself.hidePopup();
                Color color = (Color)evt.getNewValue();
                setValue(color, null);
                //System.out.println(color);
                //visible_comp.setBackground((Color)evt.getNewValue());
            }
        });

        
        addActionListener(new ActionListener()
                {  public void actionPerformed(ActionEvent ae) { createColorPanelWindow(); } });
            

        addAncestorListener(this);
    }
    
    protected void createColorPanelWindow()
    {
        // build popup window
        //System.out.println(getDialog(this));
        
        Dialog parentDlg = getDialog(this);
        if (parentDlg != null)
        {
            popup = new JWindow(parentDlg);
        } else
        {
            popup = new JWindow(getFrame(this));
        }
        
        popup.getContentPane().add(drop_down_comp);
        
        popup.addWindowFocusListener(new WindowAdapter() 
        {
            public void windowLostFocus(WindowEvent evt) 
            {
                popup.setVisible(false);
            }
        });
        popup.pack();
        
        // show the popup window
        Point pt = visible_comp.getLocationOnScreen();
        pt.translate(visible_comp.getWidth()-popup.getWidth(),visible_comp.getHeight());
        popup.setLocation(pt);
        popup.toFront();
        popup.setVisible(true);
        popup.requestFocusInWindow();
    }

    
    protected Frame getFrame(Component comp) 
    {
        if (comp == null) 
        {
            comp = this;
        }
        if(comp.getParent() instanceof Frame) 
        {
            return (Frame)comp.getParent();
        }
        return getFrame(comp.getParent());
    }
    
    protected java.awt.Dialog getDialog(Component comp) 
    {
        if (comp.getParent() instanceof Dialog) 
        {
            return (Dialog)comp.getParent();
        }
        return getDialog(comp.getParent());
    }
    
    public void ancestorAdded(AncestorEvent event)
    { 
        hidePopup();
    }
    
    public void ancestorRemoved(AncestorEvent event)
    { 
        hidePopup();
    }
    
    public void ancestorMoved(AncestorEvent event){ 
        if (event.getSource() != popup)
        {
            hidePopup();
        }
    }
    
    public void hidePopup() 
    {
        if (popup != null && popup.isVisible()) 
        {
            popup.setVisible(false);
        }
    }
    
    //-----------------------------------------------------
    // GetSetValueIFace
    //-----------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        
        Object data = value;
        if (data instanceof String)
        {
            data = (new ColorWrapper((String)data)).getColor();
        }
        
        if (data instanceof Color)
        {
            Color newValue = (Color)data;
            Color oldColor = color;
            color = newValue;
            visible_comp.setBackground(color);
            firePropertyChange("setValue", oldColor, newValue); 
            
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return color;
    }

    //-----------------------------------------------------
    // Inner Classes
    //-----------------------------------------------------

    class ColorSelectionPanel extends JPanel 
    {
        public ColorSelectionPanel() 
        {
            GridBagLayout      gbl = new GridBagLayout();
            GridBagConstraints c   = new GridBagConstraints();
            setLayout(gbl);
            
            ActionListener color_listener = new ActionListener() 
            {
                public void actionPerformed(ActionEvent evt) 
                {
                    hidePopup();
                    selectColor(((JButton)evt.getSource()).getBackground());
                }
            };
            
            ActionListener othercolor_listener = new ActionListener() 
            {
                public void actionPerformed(ActionEvent evt) {
                    
                    Color newColor = JColorChooser.showDialog(
                            itself,
                            "Choose Color",
                            itself.getBackground());
                    if (newColor != null)
                    {
                        selectColor(newColor);
                    }
                }
            };

            c.gridheight = 1;
            c.gridwidth = 1;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 1.0;
            c.weighty = 1.0;
            
            int numCols = 8;
            int numRows = (rgbs.length / 3) / numCols;
                       
            Dimension dim = new Dimension(15,15);
            int inx = 0;
            for(int i=0; i<numRows; i++) {
                for(int j=0; j<numCols; j++) {
                    c.gridx=j;
                    c.gridy=i;
                    
                    if (inx > rgbs.length)
                    {
                        JLabel lbl = new JLabel();
                        gbl.setConstraints(lbl,c);
                        lbl.setSize(dim);
                        lbl.setPreferredSize(dim);
                        lbl.setMinimumSize(dim);
                        add(lbl);
                        
                    } else
                    {
                        JButton button = new JButton("");
                        button.setSize(dim);
                        button.setPreferredSize(dim);
                        button.setMinimumSize(dim);
                        Color color = new Color(rgbs[inx++], rgbs[inx++], rgbs[inx++]);
                        button.setBackground(color);
                        button.setForeground(color);
                        button.setOpaque(true);
                        button.setBorderPainted(false);
                        button.setBorder(BorderFactory.createEtchedBorder());
    
                        gbl.setConstraints(button,c);
                        add(button);
                        button.addActionListener(color_listener);
                    }
                }

            }
            JButton button = new JButton("Other...");
            button.setBorder(BorderFactory.createEtchedBorder());
            c.fill = GridBagConstraints.BOTH;
            c.gridx=0;
            c.gridy=numRows;
            c.gridwidth = GridBagConstraints.REMAINDER;
            gbl.setConstraints(button,c);
            add(button);            
            button.addActionListener(othercolor_listener);
        }
        
        protected Color selectedColor = Color.black;
        public void selectColor(Color newColor) 
        {
            Color oldColor = selectedColor;
            selectedColor = newColor;
            firePropertyChange("selectedColor",oldColor, newColor);
        }

    }
    
    // The 56 Standard Colors
    protected static int[] rgbs = {
                            0,0,0,
                            255,255,255,
                            255,0,0,
                            0,255,0,
                            0,0,255,
                            255,255,0,
                            255,0,255,
                            0,255,255,
                            128,0,0,
                            0,128,0,
                            0,0,128,
                            128,128,0,
                            128,0,128,
                            0,128,128,
                            192,192,192,
                            128,128,128,
                            153,153,255,
                            153,51,102,
                            255,255,204,
                            204,255,255,
                            102,0,102,
                            255,128,128,
                            0,102,204,
                            204,204,255,
                            0,0,128,
                            255,0,255,
                            255,255,0,
                            0,255,255,
                            128,0,128,
                            128,0,0,
                            0,128,128,
                            0,0,255,
                            0,204,255,
                            204,255,255,
                            204,255,204,
                            255,255,153,
                            153,204,255,
                            255,153,204,
                            204,153,255,
                            255,204,153,
                            51,102,255,
                            51,204,204,
                            153,204,0,
                            255,204,0,
                            255,153,0,
                            255,102,0,
                            102,102,153,
                            150,150,150,
                            0,51,102,
                            51,153,102,
                            0,51,0,
                            51,51,0,
                            153,51,0,
                            153,51,102,
                            51,51,153,
                            51,51,51};
  
 }
