/*
 * Copyright (C) 2008 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

/**
 * (Adapted from JTiledPanel which was taken form the web)
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
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JToolBar;

/**
 * A small extension to JPanel, meant to allow the JPanel to support a tiling image background. The
 * tiled background is correctly drawn inside any Border that the panel might have. Note that
 * JTiledPanel containers are always opaque. If you give the tiling image as null, then JTiledPanel
 * behaves exactly like an opaque JPanel.
 */
public class JTiledToolbar extends JToolBar
{
    private Image     tileimage;
    private int       tilewidth;
    private int       tileheight;
    private Rectangle rb;
    private Insets    ri;

    /**
     * Create a JTiledPanel with the given image. The tile argument may be null, you can set it
     * later with setTileImage(). Note that a JTiledPanel is always opaque.
     */
    public JTiledToolbar(Image tile)
    {
        super();
        setTileImage(tile);
        setOpaque(true);
        rb = new Rectangle(0, 0, 1, 1);
        ri = new Insets(0, 0, 0, 0);
    }

    /**
     * Create a JTiledPanel with the given image and layout manager and double buffering status.
     * Either or both of the first two arguments may be null.
     */
    public JTiledToolbar(Image tile, LayoutManager mgr, boolean isDB)
    {
        super();
        setLayout(mgr);
        setTileImage(tile);
        setOpaque(true);
        rb = new Rectangle(0, 0, 1, 1);
        ri = new Insets(0, 0, 0, 0);
        setDoubleBuffered(isDB);
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
        tileimage = tile;
        tilewidth = 0;
        tileheight = 0;
    }

    /**
     * Paint this component, including the tiled background image, if any.
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        /*
         * getBounds(rb); g.setColor(Color.RED); g.drawLine(0, rb.height/2, rb.width, rb.height/2);
         */

        if (tileimage != null && tilewidth <= 0)
        {
            tileheight = tileimage.getHeight(this);
            tilewidth = tileimage.getWidth(this);
        }
        if (tileimage != null && tilewidth > 0)
        {
            Color bg = getBackground();
            getBounds(rb);
            Insets riv = getInsets(ri);
            rb.translate(riv.left, riv.top);
            rb.width -= (riv.left + riv.right);
            rb.height -= (riv.top + riv.bottom);
            Shape ccache = g.getClip();
            g.clipRect(rb.x, rb.y, rb.width, rb.height);
            int xp, yp;
            for (yp = rb.y; yp < rb.y + rb.height; yp += tileheight)
            {
                for (xp = rb.x; xp < rb.x + rb.width; xp += tilewidth)
                {
                    g.drawImage(tileimage, xp, yp, bg, this);
                    //g.drawImage(tileimage, xp, yp, tilewidth, tileheight, null);
                }
            }
            g.setClip(ccache);
        }
    }
}