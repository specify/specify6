/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.af.prefs;

import static edu.ku.brc.ui.UIHelper.paintBorderGlow;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Vector;

import edu.ku.brc.ui.dnd.SimpleGlassPane;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Dec 23, 2008
 *
 */
public class PrefIndicatorPanel implements SimpleGlassPane.DelegateRenderer
{
    private Vector<Pair<Shape, Boolean>> arrows = new Vector<Pair<Shape, Boolean>>();

    
    /**
     * @param text
     * @param pointSize
     */
    public PrefIndicatorPanel()
    {
        
    }
    
    /**
     * 
     */
    public void clearArrows()
    {
        arrows.clear();
    }
    
    /**
     * @param x
     * @param y
     */
    public void addArrow(final int x, final int y)
    {
        GeneralPath gp = new GeneralPath();
        int sz = 10;
        int[] xpnts = { x, x + sz, x + sz / 4, x + sz / 2, x - sz / 2, x - sz / 4, x - sz, x };
        int[] ypnts = { y, y + (sz * 2), y + (sz * 2), y + (sz * 4), y + (sz * 4), y + (sz * 2),
                y + (sz * 2), y };
        gp.moveTo(xpnts[0], ypnts[0]);
        for (int i = 0; i < xpnts.length; i++)
        {
            gp.lineTo(xpnts[i], ypnts[i]);
        }
        gp.closePath();
        
        arrows.add(new Pair<Shape, Boolean>(gp, false));
    }
    
    /**
     * @param inx
     * @param visible
     */
    public void setIndexVisible(final int inx, final boolean visible)
    {
        Pair<Shape, Boolean> pair = arrows.get(inx);
        pair.second = visible;
    }

    /**
     * @param imgG
     * @param bufImg
     */
    @Override
    public void render(Graphics2D compG, Graphics2D imgG, BufferedImage bufImg)
    {
        imgG.setComposite(AlphaComposite.Src);
        imgG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        imgG.setColor(new Color(0, 0, 0, 0));
        
        for (Pair<Shape, Boolean> shape : arrows)
        {
            if (shape.second)
            {
                imgG.fill(shape.first);
                paintBorderGlow(imgG, shape.first, 6);
            }
        }
    }
}
