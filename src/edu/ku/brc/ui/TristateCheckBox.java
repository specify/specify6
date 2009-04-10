/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;

/**
 * Author: Dr. Heinz M. Kabutz From: http://www.roseindia.net/javatutorials/tristatecheckbox.shtml
 * 
 * Maintenance tip - There were some tricks to getting this code working:
 * 
 * 1. You have to overwite addMouseListener() to do nothing 2. You have to add a mouse event on
 * mousePressed by calling super.addMouseListener() 3. You have to replace the UIActionMap for the
 * keyboard event "pressed" with your own one. 4. You have to remove the UIActionMap for the
 * keyboard event "released". 5. You have to grab focus when the next state is entered, otherwise
 * clicking on the component won't get the focus. 6. You have to make a TristateDecorator as a
 * button model that wraps the original button model and does state management.
 * 
 * I added the rendering of the "dash" for the don't care state.
 */
public class TristateCheckBox extends JCheckBox
{
    private RenderingHints hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    /** This is a type-safe enumerated type */
    public static class State
    {
        private State()
        {
        }
    }

    public static final State       NOT_SELECTED = new State();
    public static final State       SELECTED     = new State();
    public static final State       DONT_CARE    = new State();

    private final TristateDecorator model;

    public TristateCheckBox(String text, Icon icon)
    {
        this(text, icon, NOT_SELECTED);
    }

    public TristateCheckBox(String text, Icon icon, State initial)
    {
        super(text, icon);
        
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        // Add a listener for when the mouse is pressed
        super.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                grabFocus();
                model.nextState();
            }
        });
        // Reset the keyboard action map
        ActionMap map = new ActionMapUIResource();
        map.put("pressed", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                grabFocus();
                model.nextState();
            }
        });
        map.put("released", null);
        SwingUtilities.replaceUIActionMap(this, map);
        // set the model to the adapted model
        model = new TristateDecorator(getModel());
        setModel(model);
        setCheckState(initial);
    }

    public TristateCheckBox(String text, State initial)
    {
        this(text, null, initial);
    }

    public TristateCheckBox(String text)
    {
        this(text, DONT_CARE);
    }

    public TristateCheckBox()
    {
        this(null);
    }

    /** No one may add mouse listeners, not even Swing! */
    public void addMouseListener(MouseListener l)
    {
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (getCheckState() == DONT_CARE)
        {
            Graphics2D g2 = (Graphics2D)g;
            g2.addRenderingHints(hints);
            g2.setColor(Color.BLACK);
            Dimension s = getSize();
            if (UIHelper.isMacOS())
            {
                Stroke origStroke = g2.getStroke();
                g2.setStroke(new BasicStroke(2.0f)); // make the arrow head solid even if dash pattern has been specified
                g2.drawLine(11, s.height/2, 16, s.height/2);
                g2.setStroke(origStroke);
            }
        }
    }
    
    /**
     * Set the new state to either SELECTED, NOT_SELECTED or DONT_CARE. If state == null, it is
     * treated as DONT_CARE.
     */
    public void setCheckState(State state)
    {
        model.setState(state);
    }

    /**
     * Return the current state, which is determined by the selection status of the model.
     */
    public State getCheckState()
    {
        return model.getState();
    }

    public void setSelected(boolean b)
    {
        if (b)
        {
            setCheckState(SELECTED);
        } else
        {
            setCheckState(NOT_SELECTED);
        }
    }

    /**
     * Exactly which Design Pattern is this? Is it an Adapter, a Proxy or a Decorator? In this case,
     * my vote lies with the Decorator, because we are extending functionality and "decorating" the
     * original model with a more powerful model.
     */
    private class TristateDecorator implements ButtonModel
    {
        private final ButtonModel other;

        private TristateDecorator(ButtonModel other)
        {
            this.other = other;
        }

        private void setState(State state)
        {
            if (state == NOT_SELECTED)
            {
                other.setArmed(false);
                setPressed(false);
                setSelected(false);
                
            } else if (state == SELECTED)
            {
                other.setArmed(false);
                setPressed(false);
                setSelected(true);
                
            } else
            { // either "null" or DONT_CARE
                other.setArmed(true);
                setPressed(false);
                setSelected(false);
            }
        }

        /**
         * The current state is embedded in the selection / armed state of the model.
         * 
         * We return the SELECTED state when the checkbox is selected but not armed, DONT_CARE state
         * when the checkbox is selected and armed (grey) and NOT_SELECTED when the checkbox is
         * deselected.
         */
        private State getState()
        {
            if (isSelected() && !isArmed())
            {
                // normal black tick
                return SELECTED;
            } else if (!isSelected() && isArmed())
            {
                // don't care grey tick
                return DONT_CARE;
            } else
            {
                // normal deselected
                return NOT_SELECTED;
            }
        }

        /** We rotate between NOT_SELECTED, SELECTED and DONT_CARE. */
        private void nextState()
        {
            State current = getState();
            if (current == NOT_SELECTED)
            {
                setState(SELECTED);
            } else if (current == SELECTED)
            {
                setState(DONT_CARE);
            } else if (current == DONT_CARE)
            {
                setState(NOT_SELECTED);
            }
        }

        /** Filter: No one may change the armed status except us. */
        public void setArmed(boolean b)
        {
        }

        /**
         * We disable focusing on the component when it is not enabled.
         */
        public void setEnabled(boolean b)
        {
            setFocusable(b);
            other.setEnabled(b);
        }

        /**
         * All these methods simply delegate to the "other" model that is being decorated.
         */
        public boolean isArmed()
        {
            return other.isArmed();
        }

        public boolean isSelected()
        {
            return other.isSelected();
        }

        public boolean isEnabled()
        {
            return other.isEnabled();
        }

        public boolean isPressed()
        {
            return other.isPressed();
        }

        public boolean isRollover()
        {
            return other.isRollover();
        }

        public void setSelected(boolean b)
        {
            other.setSelected(b);
        }

        public void setPressed(boolean b)
        {
            other.setPressed(b);
        }

        public void setRollover(boolean b)
        {
            other.setRollover(b);
        }

        public void setMnemonic(int key)
        {
            other.setMnemonic(key);
        }

        public int getMnemonic()
        {
            return other.getMnemonic();
        }

        public void setActionCommand(String s)
        {
            other.setActionCommand(s);
        }

        public String getActionCommand()
        {
            return other.getActionCommand();
        }

        public void setGroup(ButtonGroup group)
        {
            other.setGroup(group);
        }

        public void addActionListener(ActionListener l)
        {
            other.addActionListener(l);
        }

        public void removeActionListener(ActionListener l)
        {
            other.removeActionListener(l);
        }

        public void addItemListener(ItemListener l)
        {
            other.addItemListener(l);
        }

        public void removeItemListener(ItemListener l)
        {
            other.removeItemListener(l);
        }

        public void addChangeListener(ChangeListener l)
        {
            other.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l)
        {
            other.removeChangeListener(l);
        }

        public Object[] getSelectedObjects()
        {
            return other.getSelectedObjects();
        }
    }
}
