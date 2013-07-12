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
package edu.ku.brc.ui;

/**
 * (Copied from the Web)
 * 
 * @author rod
 * 
 * @code_status Alpha
 * 
 * Dec 13, 2008
 * 
 */
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JPanel;

/**
 * A small extension to JPanel, meant to allow the JPanel to support a tiling image background. The
 * tiled background is correctly drawn inside any Border that the panel might have. Note that
 * JTiledPanel containers are always opaque. If you give the tiling image as null, then JTiledPanel
 * behaves exactly like an opaque JPanel.
 */
public class JTiledPanel extends JPanel
{
    private Image     tileimage;
    private int       tilewidth;
    private int       tileheight;
    private Rectangle rb;

    /**
     * 
     */
    public JTiledPanel()
    {
        super();
        setOpaque(false);
        rb = new Rectangle(0, 0, 1, 1);
    }

    /**
     * @param layout
     */
    public JTiledPanel(LayoutManager layout)
    {
        super(layout);
        setOpaque(false);
        rb = new Rectangle(0, 0, 1, 1);
    }

    /**
     * Create a JTiledPanel with the given image. The tile argument may be null, you can set it
     * later with setTileImage(). Note that a JTiledPanel is always opaque.
     */
    public JTiledPanel(Image tile)
    {
        super();
        setTileImage(tile);
        setOpaque(false);
        rb = new Rectangle(0, 0, 1, 1);
    }

    /**
     * Create a JTiledPanel with the given image and layout manager and double buffering status.
     * Either or both of the first two arguments may be null.
     */
    public JTiledPanel(Image tile, LayoutManager mgr, boolean isDB)
    {
        super(mgr, isDB);
        setTileImage(tile);
        setOpaque(false);
        rb = new Rectangle(0, 0, 1, 1);
    }

    /**
     * Get the current tiling image, or null if there isn't any right now.
     */
    public Image getTileImage()
    {
        return tileimage;
    }

    /**
     * Set the current tiling image. To prevent tiling, call this method with null. Note that this
     * method does NOT call repaint for you; if you want the panel to repaint immediately, you must
     * call repaint() yourself.
     */
    public void setTileImage(Image tile)
    {
        tileimage  = tile;
        tilewidth  = 0;
        tileheight = 0;
    }
    
    /**
     * @return whether it is being tiled.
     */
    public boolean isTiled()
    {
        return tileimage != null;
    }

    /**
     * Paint this component, including the tiled background image, if any.
     */
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        if (tileimage != null && tilewidth <= 0)
        {
            tileheight = tileimage.getHeight(this);
            tilewidth = tileimage.getWidth(this);
        }
        
        if (tileimage != null && tilewidth > 0)
        {
            Color bg = getBackground();
            getBounds(rb);
            rb.x = 0;
            rb.y = 0;

            //Insets riv = getInsets(ri);
            //rb.translate(riv.left, riv.top);
            //rb.width -= (riv.left + riv.right);
            //rb.height -= (riv.top + riv.bottom);
            Shape ccache = g.getClip();
            g.clipRect(rb.x, rb.y, rb.width, rb.height);
            int xp, yp;
            for (yp = rb.y; yp < rb.y + rb.height; yp += tileheight)
            {
                for (xp = rb.x; xp < rb.x + rb.width; xp += tilewidth)
                {
                    g.drawImage(tileimage, xp, yp, bg, this);
                }
            }
            g.setClip(ccache);
        }
    }
}
