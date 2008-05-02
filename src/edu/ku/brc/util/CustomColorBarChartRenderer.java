/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.util;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.renderer.category.BarRenderer3D;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 29, 2008
 *
 */
public class CustomColorBarChartRenderer extends BarRenderer3D
{
    private Paint[] colors;
    
    int[] rgb = {142, 32, 166,
                    87,  32, 166,
                    32,  32, 166,
                    32, 87, 166,
                    32, 142, 166,
                    32, 166, 32,
                    87, 166, 32,
                    142, 166, 32,
                    166, 142, 32,
                    166, 87, 32,
                    166, 32, 32};
        
    /**
     * 
     */
    public CustomColorBarChartRenderer()
    {
        colors = new Paint[rgb.length];
        for (int i = 0; i < rgb.length; i++)
        {
            colors[i / 3] = new Color(rgb[i], rgb[i + 1], rgb[i + 2]);
            i += 2;
        }
    }

    /* (non-Javadoc)
     * @see org.jfree.chart.renderer.AbstractRenderer#getItemPaint(int, int)
     */
    public Paint getItemPaint(final int row, final int column)
    {
        return (this.colors[row % colors.length]);
    }
}
