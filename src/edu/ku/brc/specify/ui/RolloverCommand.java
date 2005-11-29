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

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import  com.jgoodies.looks.plastic.*;
import java.util.*;
import edu.ku.brc.specify.core.NavBoxItemIFace;

/**
 * @author Rod Spears
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RolloverCommand extends JPanel implements NavBoxItemIFace
{
    protected JTextField   txtFld = null;
    
    protected boolean      isOver     = false;
    protected static Color focusColor = Color.BLUE;
    protected Vector<ActionListener> actions = new Vector<ActionListener>();
    protected JLabel       iconLabel;
    //protected JLabel       label;
    protected JButton      btn;

    
    /**
     * 
     * @param aLabel
     * @param aImgIcon
     */
    public RolloverCommand(String aLabel, Icon aImgIcon)
    {
        super(new BorderLayout());
        setBorder(new EmptyBorder(new Insets(1,1,1,1)));
        
        iconLabel = new JLabel(aImgIcon);        
        btn       = new JButton(aLabel, aImgIcon);
        btn.setBorder(new EmptyBorder(new Insets(1,1,1,1)));
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setRolloverEnabled(true);

        
        //add(iconLabel, BorderLayout.WEST);
        add(btn, BorderLayout.CENTER);
        
        //this.setFocusable(true);

        
        //UIManager.getLookAndFeel().
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
    
    /**
     * paints the component
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
    
    public void addActionListener(ActionListener al)
    {
        btn.addActionListener(al);
    }
    
    public void removeActionListener(ActionListener al)
    {
        btn.removeActionListener(al);
    }
   
    // NavBoxItemIFace
    public Component getUIComponent()
    {
        return this;
    }
    
}
