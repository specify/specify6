/* Filename:    $RCSfile: ToolBarDropDownBtn.java,v $
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

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JWindow;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.metal.MetalComboBoxIcon;

/**
 * Toolbar button derived from DropDownBtn, this provides a way to set menu items
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ColorChooser extends JButton implements AncestorListener 
{
    protected JComponent drop_down_comp;
    protected JComponent visible_comp;
    protected Color      color;
    protected JWindow    popup;
    protected ColorSelectionPanel colorPanel;
    protected ColorChooser itself;
    
     /**
     * Creates a toolbar item with label and icon and their positions.
     * @param label label of the toolbar item
     * @param icon the icon
     * @param textPosition the position of the text as related to the icon
     */
    public ColorChooser(Color color)
    {
        super();
        
        this.color   = color;
        visible_comp = this;
        itself       = this;
        
        setBackground(color);
        setPreferredSize(new Dimension(24,24));
        
        init();
    }
    
    public void paint(Graphics g)
    {
        g.setColor(getBackground());
        Dimension size = getSize();
        Insets insets = getInsets();
        System.out.println(size+"  "+insets);
        //g.fillRect(insets.left,insets.top,size.width-(insets.left+insets.right),size.height-(insets.top+insets.bottom));
        g.fillRect(0,0,size.width,size.height);
        g.setColor(Color.BLACK);
        g.drawRect(0,0,size.width-1,size.height-1);
    }
    
    protected void init()
    {
        drop_down_comp = new ColorSelectionPanel();      
        drop_down_comp.addPropertyChangeListener("selectedColor",new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                itself.hidePopup();
                Color color = (Color)evt.getNewValue();
                System.out.println(color);
                visible_comp.setBackground((Color)evt.getNewValue());
            }
        });

        
        addActionListener(new ActionListener()
                {  public void actionPerformed(ActionEvent ae) { createColorPanelWindow(); } });
            

        addAncestorListener(this);
    }
    
    protected void createColorPanelWindow()
    {
        // build popup window
        popup = new JWindow(getFrame(null));
        popup.getContentPane().add(drop_down_comp);
        popup.addWindowFocusListener(new WindowAdapter() {
            public void windowLostFocus(WindowEvent evt) {
                popup.setVisible(false);
            }
        });
        popup.pack();
        
        // show the popup window
        Point pt = visible_comp.getLocationOnScreen();
        System.out.println("pt = " + pt);
        pt.translate(visible_comp.getWidth()-popup.getWidth(),visible_comp.getHeight());
        System.out.println("pt = " + pt);
        popup.setLocation(pt);
        popup.toFront();
        popup.setVisible(true);
        popup.requestFocusInWindow();
    }

    
    protected Frame getFrame(Component comp) {
        if(comp == null) {
            comp = this;
        }
        if(comp.getParent() instanceof Frame) {
            return (Frame)comp.getParent();
        }
        return getFrame(comp.getParent());
    }
    
    public void ancestorAdded(AncestorEvent event){ 
        hidePopup();
    }
    
    public void ancestorRemoved(AncestorEvent event){ 
        hidePopup();
    }
    
    public void ancestorMoved(AncestorEvent event){ 
        if (event.getSource() != popup) {
            hidePopup();
        }
    }
    
    public void hidePopup() {
        if(popup != null && popup.isVisible()) {
            popup.setVisible(false);
        }
    }

    class ColorSelectionPanel extends JPanel 
    {
        public ColorSelectionPanel() 
        {
            GridBagLayout      gbl = new GridBagLayout();
            GridBagConstraints c   = new GridBagConstraints();
            setLayout(gbl);
            
            ActionListener color_listener = new ActionListener() 
            {
                public void actionPerformed(ActionEvent evt) {
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
                    System.out.println(newColor);
                    if (newColor != null)
                    {
                        selectColor(newColor);
                    }
                }
            };
            
            Color[] colors = new Color[12];
            colors[0] = Color.white;
            colors[1] = Color.black;
            
            colors[2] = Color.blue;
            colors[3] = Color.cyan;
            colors[4] = Color.gray;
            colors[5] = Color.green;
            colors[6] = Color.lightGray;
            colors[7] = Color.magenta;
            colors[8] = Color.orange;
            colors[9] = Color.pink;
            colors[10] = Color.red;
            colors[11] = Color.yellow;
            
            c.gridheight = 1;
            c.gridwidth = 1;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 1.0;
            c.weighty = 1.0;
            
            for(int i=0; i<3; i++) {
                for(int j=0; j<4; j++) {
                    c.gridx=j;
                    c.gridy=i;
                    
                    JButton button = new JButton("");
                    Dimension dim = new Dimension(15,15);
                    button.setSize(dim);
                    button.setPreferredSize(dim);
                    button.setMinimumSize(dim);
                    button.setBackground(colors[j+i*4]);
                    button.setForeground(colors[j+i*4]);
                    button.setOpaque(true);
                    button.setBorderPainted(false);
                    button.setBorder(BorderFactory.createEtchedBorder());
                    gbl.setConstraints(button,c);
                    add(button);
                    button.addActionListener(color_listener);
                }

            }
            JButton button = new JButton("Other...");
            //button.setBorderPainted(false);
            button.setBorder(BorderFactory.createEtchedBorder());
            c.fill = GridBagConstraints.BOTH;
            c.gridx=0;
            c.gridy=4;
            c.gridwidth = GridBagConstraints.REMAINDER;
            gbl.setConstraints(button,c);
            add(button);            
            button.addActionListener(othercolor_listener);
        }
        
        protected Color selectedColor = Color.black;
        public void selectColor(Color newColor) {
            Color oldColor = selectedColor;
            selectedColor = newColor;
            firePropertyChange("selectedColor",oldColor, newColor);
        }

    }


 }
