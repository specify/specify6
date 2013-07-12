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
package edu.ku.brc.specify.plugins.imgproc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;

import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.Trayable;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 12, 2011
 *
 */
public class TrayImageIcon implements Trayable
{
    protected ImageIcon imgIcon;
    protected ImageIcon imgIconFull;
    protected File      file;
    
    /**
     * @param imgIcon
     */
    public TrayImageIcon(final File file)
    {
        super();
        this.file = file;
        
        imgIconFull = new ImageIcon(file.getAbsolutePath());
        
        BufferedImage destImage = null;
        
        int destWidth = 120;
        destImage = new BufferedImage(destWidth, destWidth, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = destImage.createGraphics();
        graphics2D.drawImage(imgIconFull.getImage(), 0, 0, destWidth, destWidth, null);
        graphics2D.dispose();

        try
        {
            int[] hsv = new int[3];
            for (int i=0;i<destWidth;i++)
            {
                Color c = new Color(destImage.getRGB(i, 0));
                rgb2hsv(c.getRed(), c.getGreen(), c.getBlue(), hsv);
                
                System.out.print(c.getRed()+","+c.getGreen()+","+c.getBlue()+"   ");
                System.out.print(hsv[0]+","+hsv[1]+","+hsv[2]+"   ");
                double[] hsvDbl = RGBtoHSV(c.getRed(), c.getGreen(), c.getBlue());
                System.out.println(hsvDbl[0]+","+hsvDbl[1]+","+hsvDbl[2]+"   ");

            }
            System.out.println();
            System.out.println();
            
            rgb2hsv(255, 0, 0, hsv);
            System.out.println(hsv[0]+","+hsv[1]+","+hsv[2]+"   ");
            double[] hsvDbl = RGBtoHSV(255, 0, 0);
            System.out.println(hsvDbl[0]+","+hsvDbl[1]+","+hsvDbl[2]+"   ");
            
            rgb2hsv(0, 255, 0, hsv);
            System.out.println(hsv[0]+","+hsv[1]+","+hsv[2]+"   ");
            hsvDbl = RGBtoHSV(0, 255, 0);
            System.out.println(hsvDbl[0]+","+hsvDbl[1]+","+hsvDbl[2]+"   ");
            
            rgb2hsv(0, 0, 255, hsv);
            System.out.println(hsv[0]+","+hsv[1]+","+hsv[2]+"   ");
            hsvDbl = RGBtoHSV(0, 0, 255);
            System.out.println(hsvDbl[0]+","+hsvDbl[1]+","+hsvDbl[2]+"   ");
            
            imgIcon = GraphicsUtils.scaleImageToIconImage(destImage, 120, 120, true, true);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static double[] RGBtoHSV(double r, double g, double b){

        double h, s, v;

        double min, max, delta;

        min = Math.min(Math.min(r, g), b);
        max = Math.max(Math.max(r, g), b);

        // V
        v = max;

         delta = max - min;

        // S
         if( max != 0 )
            s = delta / max;
         else {
            s = 0;
            h = -1;
            return new double[]{h,s,v};
         }

        // H
         if( r == max )
            h = ( g - b ) / delta; // between yellow & magenta
         else if( g == max )
            h = 2 + ( b - r ) / delta; // between cyan & yellow
         else
            h = 4 + ( r - g ) / delta; // between magenta & cyan

         h *= 60;    // degrees

        if( h < 0 )
            h += 360;

        return new double[]{h,s,v};
    }
    

    private void rgb2hsv(int r, int g, int b, int hsv[])
    {

        int min; // Min. value of RGB
        int max; // Max. value of RGB
        int delMax; // Delta RGB value

        if (r > g)
        {
            min = g;
            max = r;
        } else
        {
            min = r;
            max = g;
        }
        if (b > max)
            max = b;
        if (b < min)
            min = b;

        delMax = max - min;

        float H = 0, S;
        float V = max;

        if (delMax == 0)
        {
            H = 0;
            S = 0;
        } else
        {
            S = delMax / 255f;
            if (r == max)
                H = ((g - b) / (float) delMax) * 60;
            else if (g == max)
                H = (2 + (b - r) / (float) delMax) * 60;
            else if (b == max)
                H = (4 + (r - g) / (float) delMax) * 60;
        }

        hsv[0] = (int) (H);
        hsv[1] = (int) (S * 100);
        hsv[2] = (int) (V * 100);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.Trayable#getIcon()
     */
    @Override
    public javax.swing.ImageIcon getIcon()
    {
        return imgIcon;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.Trayable#getName()
     */
    @Override
    public String getName()
    {
        return file.getName();
    }
    
    /**
     * @return
     */
    public File getFile()
    {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file)
    {
        this.file = file;
    }
}
