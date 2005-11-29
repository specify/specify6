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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;

import edu.ku.brc.specify.core.NavBoxItemIFace;

/**
 * @author Rod Spears
 *
 *  
 * Creates a panel containing an icon and button with a focus "ring" when the mouse is hovering.
 * This class is used mostly in NavBoxes
 */
public class RolloverCommand extends JPanel implements NavBoxItemIFace
{
    protected JTextField             txtFld     = null;   
    protected JLabel                 iconLabel;
    protected JButton                btn;
    
    protected boolean                isOver     = false;
    protected static Color           focusColor = Color.BLUE;
    protected Vector<ActionListener> actions    = new Vector<ActionListener>();

    
    /**
     * Constructs a UI component with a label and an icon which can be clicked to execute an action
     * @param label the text label for the UI
     * @param imgIcon the icon for the UI
     */
    public RolloverCommand(String label, Icon imgIcon)
    {
        super(new BorderLayout());
        setBorder(new EmptyBorder(new Insets(1,1,1,1)));
        
        iconLabel = new JLabel(imgIcon);        
        btn       = new JButton(label, imgIcon);
        
        btn.setBorder(new EmptyBorder(new Insets(1,1,1,1)));
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setRolloverEnabled(true);

        
        add(btn, BorderLayout.CENTER);

        
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
            }
          };
          addMouseListener(mouseInputAdapter);
          addMouseMotionListener(mouseInputAdapter);
          
          btn.addMouseListener(mouseInputAdapter);
          btn.addMouseMotionListener(mouseInputAdapter);

          ActionListener actionListener = new ActionListener() {
              public void actionPerformed(ActionEvent actionEvent) {
                  startEditting();
              }
            };
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
            btn.addMouseListener (mouseListener);            
          
    }
    
    /**
     * Stops the editting of the name. 
     * It will accept any input that has already been typed, but it will not allow for a zero length string.
     * It sawps out the text field and swpas in the label.
     *
     */
    protected void stopEditting()
    {
        if (txtFld != null)
        {
            btn.setText(txtFld.getText());
            btn.setVisible(true);
            txtFld.setVisible(false);
            remove(txtFld);
            remove(iconLabel);
            add(btn, BorderLayout.CENTER);
            txtFld = null;
            invalidate();
            getParent().doLayout();
            getParent().repaint();
        }
    }
    
    /**
     * Start the editing of the name. It swaps out the label with a text field to enable the user to type in a new name.
     *
     */
    protected void startEditting()
    {
        btn.setVisible(false);
        remove(btn);
        
        txtFld = new JTextField(btn.getText());
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
        
        txtFld.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {}
            
            public void focusLost(FocusEvent e) 
            {
                stopEditting();
            }
            
        });
        
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (isOver && !this.hasFocus())
        {
            Dimension dim = getSize();
            Insets insets = getInsets();
            
            g.setColor(UIManager.getLookAndFeel() instanceof PlasticLookAndFeel ? PlasticLookAndFeel.getFocusColor() : Color.BLUE);
            g.drawRect(insets.left, insets.top, dim.width-insets.right-insets.left, dim.height-insets.bottom-insets.top);
        } /*else if (this.hasFocus())
        {
            Dimension dim = getSize();
            Insets insets = getInsets();
            
            g.setColor(UIManager.getLookAndFeel() instanceof PlasticLookAndFeel ? PlasticLookAndFeel.getFocusColor() : Color.BLUE);
            g.drawRect(insets.left, insets.top, dim.width-insets.right-insets.left, dim.height-insets.bottom-insets.top);
        }*/
    }
    
    /**
     * 
     * @param al
     */
    public void addActionListener(ActionListener al)
    {
        btn.addActionListener(al);
    }
    
    /**
     * 
     * @param al
     */
    public void removeActionListener(ActionListener al)
    {
        btn.removeActionListener(al);
    }
   
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.NavBoxItemIFace#getUIComponent()
     */
    public Component getUIComponent()
    {
        return this;
    }
    
}
