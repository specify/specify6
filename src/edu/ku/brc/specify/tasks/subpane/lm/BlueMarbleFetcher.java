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
package edu.ku.brc.specify.tasks.subpane.lm;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import edu.ku.brc.ui.IconManager;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 5, 2012
 *
 */
public class BlueMarbleFetcher
{
    // Map Members
    public String URL_FMT;
    public String BG_URL;
    public String BBOX_STR;
    
    protected int imgWidth;
    protected int imgHeight;
    
    protected BufferedImage              blueMarble      = null;
    protected int                        blueMarbleTries = 0;
    protected String                     blueMarbleURL;
    protected BufferedImageFetcherIFace  blueMarbleListener;
    protected BufferedImageFetcherIFace  pointsMapImageListener;
    protected BufferedImage              renderImage     = null;
    protected ImageIcon                  markerImg;
    
    protected BufferedImageFetcherIFace  fetcherListener;
    
    /**
     * @param fetcherListener
     */
    public BlueMarbleFetcher(BufferedImageFetcherIFace fetcherListener)
    {
        this(450, 225, fetcherListener);
    }


    /**
     * @param imgWidth
     * @param imgHeight
     */
    public BlueMarbleFetcher(int imgWidth, int imgHeight, BufferedImageFetcherIFace  fetcherListener)
    {
        super();
        this.fetcherListener  = fetcherListener;
        this.imgWidth  = imgWidth;
        this.imgHeight = imgHeight;
        
        init();
    }

    /**
     * 
     */
    public void init()
    {
        markerImg = IconManager.getIcon("RedDot6x6");
        
        URL_FMT  = getResourceString("LM_URL_FMT");
        BG_URL   = getResourceString("LM_BG_URL");
        BBOX_STR = getResourceString("LM_BBOX_STR");
        
        blueMarbleURL = BG_URL + String.format("WIDTH=%d&HEIGHT=%d", imgWidth, imgHeight);

        markerImg = IconManager.getIcon("RedDot6x6");

        blueMarbleListener = new BufferedImageFetcherIFace()
        {
            @Override
            public void imageFetched(BufferedImage image)
            {
                blueMarble = image;
                
                if (fetcherListener != null) fetcherListener.imageFetched(blueMarble);
            }
            
            @Override
            public void error()
            {
                blueMarbleTries++;
                if (blueMarbleTries < 5)
                {
                    blueMarbleRetry();
                } else
                {
                    if (fetcherListener != null) fetcherListener.error();
                }
            }
        };
        
        getImageFromWeb(blueMarbleURL, blueMarbleListener);
        
        pointsMapImageListener = new BufferedImageFetcherIFace()
        {
            @Override
            public void imageFetched(final BufferedImage image)
            {
                if (renderImage == null)
                {
                    renderImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
                }
                Graphics2D g2d = renderImage.createGraphics();
                if (g2d != null)
                {
                    g2d.fillRect(0, 0, imgWidth, imgHeight);
                    if (blueMarble != null)
                    {
                        g2d.drawImage(blueMarble, 0, 0, null);
                    }
                    if (image != null)
                    {
                        g2d.drawImage(image, 0, 0, null);
                    }
                    g2d.dispose();
                }
                
                if (fetcherListener != null) fetcherListener.imageFetched(renderImage);
            }
            
            @Override
            public void error()
            {
                if (fetcherListener != null) fetcherListener.error();
            }
        };
    }
    
    private static final double CENTRAL_MERIDIAN_OFFSET = 0.0;
    public static final double W = 300;
    public static final double H = 150;// - 34;

    /**
     * @param lat
     * @param lon
     * @return
     */
    public static double[] toMillerXY(double lat, double lon)
    {
        // y' = 1499/2 - (1499/ (2 * 2.303412543)) * 1.089472895
        double x, y;

        lon = Math.toRadians(lon);
        lat = Math.toRadians(lat);

        x = lon - CENTRAL_MERIDIAN_OFFSET;
        y = 1.25 * Math.log( Math.tan( 0.25 * Math.PI + 0.4 * lat ) );

        x = ( W / 2 ) + ( W / (2 * Math.PI) ) * x;
        y = ( H / 2 ) - ( H / ( 2 * 2.303412543 ) ) * y;

        y -= 2;

        return new double[] {x, y};
    }

    /**
     * @param lat
     * @param lon
     * @return
     */
    public BufferedImage getBlueMarbleImage()
    {
        return plotPoint(Double.MIN_VALUE, Double.MIN_VALUE);
    }

    /**
     * @param lat
     * @param lon
     * @return
     */
    public BufferedImage plotPoint(double lat, double lon)
    {
        if (renderImage == null)
        {
            renderImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D g2d = renderImage.createGraphics();
        if (g2d != null)
        {
            if (blueMarble != null)
            {
                g2d.drawImage(blueMarble, 0, 0, null);
                
                if (lat != Double.MIN_VALUE && lon != Double.MIN_VALUE)
                {
                    double[] pnt = toMillerXY(lat, lon);
                    g2d.setColor(Color.YELLOW);
                    //System.out.println(String.format("%d, %d   %8.5f,%8.5f", (int)Math.round(pnt[0]), (int)Math.round(pnt[1]), lat, lon));
                    g2d.fillArc((int)Math.round(pnt[0]), (int)Math.round(pnt[1]), 4, 4, 0, 360);
                }
            }
            g2d.dispose();
        }
        return renderImage;
    }
    
    /**
     * @return the markerImg
     */
    public ImageIcon getMarkerImg()
    {
        return markerImg;
    }

    /**
     * 
     */
    private void blueMarbleRetry()
    {
        getImageFromWeb(blueMarbleURL, blueMarbleListener);
    }
    
    /**
     * @param urlStr
     * @param listener
     */
    public static void getImageFromWeb(final String                    urlStr, 
                                       final BufferedImageFetcherIFace listener)
    {
        SwingWorker<BufferedImage, BufferedImage> worker = new SwingWorker<BufferedImage, BufferedImage>()
        {
            @Override
            protected BufferedImage doInBackground() throws Exception
            {
                try {
                    URL url = new URL(urlStr);
                    return ImageIO.read(url);
                    
                 } catch (IOException e) {
                     
                 }
                return null;
            }

            @Override
            protected void done()
            {
                try
                {
                    BufferedImage img = get();
                    listener.imageFetched(img);
                    return;
                    
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
                listener.error();
                
                super.done();
            }
        };
        worker.execute();
    }


}
