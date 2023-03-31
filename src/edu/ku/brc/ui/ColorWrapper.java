/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.awt.Color;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * A class to wrap a Color object so it can be mutable easily 
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class ColorWrapper
{
    private static final Logger log = Logger.getLogger(ColorWrapper.class);
    
    protected Color color;
    
    /**
     * Constructor with color
     * @param color the color to initialize it with
     */
    public ColorWrapper(final Color color)
    {
        if (color == null)
        {
            throw new IllegalArgumentException("ColorWrapper - The Color string is null!");
        }
        
        this.color = color;
    }

    /**
     * Constructor with r, g, b
     * @param r red
     * @param g green
     * @param b blue
     */
    public ColorWrapper(final int r, final int g, final int b)
    {
        setRGB(r, g, b);
    }

    /**
     * Constructor with color
     * @param rgbStr the color to initialize it with
     */
    public ColorWrapper(final String rgbStr)
    {
        setRGB(rgbStr);
    }

    /**
     * Returns the Color 
     * @return the color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * Sets a new Color
     * @param color the new color
     */
    public void setColor(Color color)
    {
        this.color = color;
    }
    
    /**
     * Creates a new color from RGB values
     * @param r red
     * @param g green
     * @param b blue
     */
    public Color setRGB(final int r, final int g, final int b)
    {
        // make sure the values are in range
        int rr = Math.min(Math.max(0, r), 255);
        int gg = Math.min(Math.max(0, g), 255);
        int bb = Math.min(Math.max(0, b), 255);
        color = new Color(rr, gg, bb);
        return color;
    }
    
    /**
     * Parses a comma separated String and creates a color and sets it internally
     * @param rgbStr the string to be parsed ("100, 128, 45")
     */
    public Color setRGB(final String rgbStr)
    {
        if (rgbStr == null)
        {
            throw new IllegalArgumentException("ColorWrapper - The Color string is null!");
        }
        
        String[] rgbVals = StringUtils.split(rgbStr, " ,");
        if (rgbVals.length == 3)
        {
            // We could check for numeric here
            if (StringUtils.isNumeric(rgbVals[0]) &&
                StringUtils.isNumeric(rgbVals[1]) &&
                StringUtils.isNumeric(rgbVals[2]))
            {
                int r = Integer.parseInt(rgbVals[0]);
                int g = Integer.parseInt(rgbVals[1]);
                int b = Integer.parseInt(rgbVals[2]);
                setRGB(r,g,b);
            } else
            {
                setRGB(255,255,255);
                //throw new IllegalArgumentException("ColorWrapper - one of the values is not numeric r["+rgbVals[0]+"] g["+rgbVals[1]+"] b["+rgbVals[2]+"]");
                log.error("ColorWrapper - one of the values is not numeric ["+rgbStr+"] r["+rgbVals[0]+"] g["+rgbVals[1]+"] b["+rgbVals[2]+"]");
            }
        } else if (StringUtils.isNotEmpty(rgbStr))
        {
            throw new IllegalArgumentException("ColorWrapper - The Color string doesn't parse ["+rgbStr+"]");
        }
        return color;
    }
    
    /**
     * Helper to convert a Color into a string "r,g,b"
     * @param c the color
     * @return the comma separated string
     */
    public static String toString(Color c)
    {
        return c.getRed() + ", " +c.getGreen() + ", " +c.getBlue(); 
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return toString(color); 
    }
 
}
