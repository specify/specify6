package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JTabbedPane;
import javax.swing.event.MouseInputAdapter;

/**
 * Temporary Class until Mustang ships, it places an "X" in the bottom right of the TabbedPane for closing tabs.
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ExtendedTabbedPane extends JTabbedPane
{
    protected static final int CLOSER_SIZE = 5;
    
    protected Rectangle closerRect = new Rectangle();
    protected boolean   isOver     = false;
    protected ExtendedTabbedPane itself;
    
    public ExtendedTabbedPane()
    {
        super();
        init();
    }

    public ExtendedTabbedPane(int tabPlacement)
    {
        super(tabPlacement);
        init();
    }

    public ExtendedTabbedPane(int tabPlacement, int tabLayoutPolicy)
    {
        super(tabPlacement, tabLayoutPolicy);
        init();
    }
    
    protected void init()
    {
        setBackground(Color.WHITE);
        
        itself = this;
        
        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
            public void mouseExited(MouseEvent e) 
            {
                if (!closerRect.contains(e.getPoint()))
                {
                    isOver = false;
                    repaint();
                    //UICacheManager.displayStatusBarText("");
                }
            }
            public void mouseMoved(MouseEvent e)
            {
                if (closerRect.contains(e.getPoint()))
                {
                    isOver = true;
                    repaint();
                    //UICacheManager.displayStatusBarText(itself.getToolTipText());
                    
                } else if (isOver)
                {
                    isOver = false;
                    repaint();
                }
                
            }
            public void mouseClicked(MouseEvent e) 
            {
                if (closerRect.contains(e.getPoint()))
                {
                    closeCurrent();
                }
                
            }
          };
        addMouseListener(mouseInputAdapter);
        addMouseMotionListener(mouseInputAdapter);
        
    }
    
    protected void closeCurrent()
    {
        
        this.remove(this.getSelectedComponent());
    }
    
    protected void drawCloser(final Graphics g, final int x, final int y, final int w, final int h)
    {
        closerRect.setBounds(x, y, w, h);
        
        g.drawLine(x, y, x+w, y+h);
        g.drawLine(x+w, y, x, y+h);
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (this.getTabCount() > 0)
        {
            Dimension s = getSize();
            s.width  -= CLOSER_SIZE + 1;
            s.height -= CLOSER_SIZE + 1;
            
            Color color = getBackground();
            
            int x =  s.width -5;
            int y =  s.height-5;
            if (isOver)
            {
                //g.setColor(color.darker());
                //g.fillRect(x, y, CLOSER_SIZE+1, CLOSER_SIZE+1);
                g.setColor(Color.RED);
                drawCloser(g, x, y, CLOSER_SIZE, CLOSER_SIZE);
                
            } else
            {
                //g.setColor(color);
                //g.fillRect(x, y, CLOSER_SIZE, CLOSER_SIZE);
                g.setColor(color.darker());
                drawCloser(g, x, y, CLOSER_SIZE, CLOSER_SIZE);                
            }
        } else
        {
            closerRect.setBounds(0,0,0,0);
        }
        
        //g.setColor(color.darker().darker());
        //drawCloser(g, x, y, CLOSER_SIZE, CLOSER_SIZE);
    }
}
