/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.collab;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Nov 28, 2011
 *
 */
public class ShowNetworkPanel extends BaseSubPane
{
    private Vector<CollabNode> collabNodes = new Vector<CollabNode>();
    private AtomicBoolean  doPaint = new AtomicBoolean(false);
    private int rows = 4;
    private int cols = 5;

    protected static RenderingHints hints;
    protected static Random rand = new Random(System.currentTimeMillis());
    
    static
    {
        hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING,               RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS,          RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    /**
     * 
     */
    public ShowNetworkPanel(String name, Taskable task)
    {
        super(name, task);
        
        removeAll();
        
        setOpaque(true);
        setDoubleBuffered(true);
        
    }

    /**
     * @return the hints
     */
    public static RenderingHints getHints()
    {
        return hints;
    }

    public void createUI()
    {
        //synchronized (this)
        //{
            if (collabNodes.size() == 0)
            {
                Dimension size = getSize();
                
                String[] titles = {"KU", "AMNH", "UFL", "ILL", "WISC", 
                                   "UTK", "LSU", "FIELD", "DUKE", "MOBOT", 
                                   "UMICH", "BYU", "UAFB", "OSU", "ANSP", 
                                   "UWASH", "MSU", "BERK", "UMASS", "UCR", 
                        };
                
                int iconW = 64;
                int iconH = 84;
                
                int x = (size.width / 2) - 50;
                int y = (size.height / 2) - 50;
                
                int xGap = (size.width - (cols * iconW)) / (cols+1);
                int yGap = size.height / 5;
                int xc   = xGap;
                int yc   = yGap; 
                
                int inx = 0;
                for (int j=0;j<rows;j++)
                {
                    for (int i=0;i<cols;i++)
                    {
                        //System.out.println(String.format("Init: Src: %d,%d    Dst: %d,%d", x, y, xc, yc));
                        
                        CollabNode lp = new CollabNode(titles[inx], new Point(x, y), new Point(xc, yc));
                        ShowNetworkPanel.this.add(lp);
                        lp.setLocation(x, y);
                        lp.setSize(iconW, iconH);
                        
                        collabNodes.add(lp);
                        lp.setVisible(true);
                        
                        xc += xGap + iconW;
                        inx++;
                    }
                    xc = xGap;
                    yc += yGap;
                }
                
                CollabNode lp = new CollabNode(Integer.toString(inx), new Point(-100, -100), new Point(-200, -200));
                ShowNetworkPanel.this.add(lp);
                lp.setLocation(-100, -100);
                lp.setSize(iconW, iconH);
                collabNodes.add(lp);
                lp.setVisible(false);
                
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        /*try
                        {
                            Thread.sleep(500);
                        } catch (Exception ex) {}
                        UIRegistry.forceTopFrameRepaint();
                        */
                        ShowNetworkPanel.this.process(0.04);
                    }
                });
            }

        //}
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (doPaint.get())
        {
            Graphics2D g2d = (Graphics2D)g;
            g2d.addRenderingHints(hints);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2)); // make the arrow head solid even if dash pattern has been specified
            
            Dimension sz = getSize();
            
            Font f = g.getFont();
            g.setFont(f.deriveFont(32.0f));
            FontMetrics fm = g.getFontMetrics();
            
            String title = "Collaboration Network";
            int len = fm.stringWidth(title);
            int x = (sz.width - len) /2;
            int y = (int)((double)sz.height * 0.10f);
            g.drawString(title, x, y);
            
            int inx = 0;
            for (int j=0;j<rows;j++)
            {
                CollabNode startLP = collabNodes.get(inx);
                CollabNode endLP   = collabNodes.get(inx+cols-1);
                
                int xGap = 32;
                int yGap = 16;
                int x1 = startLP.getLocation().x + (startLP.getSize().width / 2);
                int y1 = startLP.getLocation().y - yGap;
                
                int x2 = endLP.getLocation().x + (startLP.getSize().width) + xGap;
                int y2 = endLP.getLocation().y - yGap;
                
                g2d.drawLine(x1, y1, x2, y2);
                
                y1 = endLP.getLocation().y - yGap;
                
                if (j < (rows - 1))
                {
                    endLP = collabNodes.get(inx+cols);
                    g2d.drawLine(x2, y2, x2, endLP.getLocation().y - yGap);
                }
                
                for (int i=0;i<cols;i++)
                {
                    CollabNode lp = collabNodes.get(inx);
                    
                    x1 = lp.getLocation().x + (startLP.getSize().width / 2);
                   
                    g2d.drawLine(x1, lp.getLocation().y, x1, lp.getLocation().y - yGap);

                    inx++;
                }
            }
        }
    }



    private void process(final double percent)
    {
        //System.out.println(percent+" =============================================");
        SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>()
        {

            @Override
            protected Boolean doInBackground() throws Exception
            {
                for (CollabNode lapTop : collabNodes)
                {
                    lapTop.move(percent);
                }
                return null;
            }

            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#done()
             */
            @Override
            protected void done()
            {
                //UIRegistry.forceTopFrameRepaint();
                if (percent < 1.0)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            ShowNetworkPanel.this.process(percent + 0.04);
                        }
                    });
                } else
                {
                    doPaint.set(true);
                    
                    String[] users = {"mjohnson", "ksmith", "tcollins", "dstevens", "wdavis"};
                    
                    for (CollabNode node : collabNodes)
                    {
                        node.setInitialized(true);
                        
                        node.addUsers(users, rand.nextInt(users.length));
                        node.setNodeActive(rand.nextInt(100) > 33);
                        
                        //System.out.println(lapTop.hashCode()+"  "+lapTop.getTitle() + " " + lapTop.getLocation());
                    }
                }
                UIRegistry.forceTopFrameRepaint();
            }
        };
        worker.execute();
    }
}
