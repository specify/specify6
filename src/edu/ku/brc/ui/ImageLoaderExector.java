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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class ImageLoaderExector
{
    private static ImageLoaderExector instance = new ImageLoaderExector();
    
    protected ExecutorService imgLoaderExecServ = null;
    
    /**
     * Private Constructor.
     */
    public ImageLoaderExector()
    {
        this(2);
    }
    
    /**
     * Private Constructor.
     */
    public ImageLoaderExector(final int queueSize)
    {
        imgLoaderExecServ = Executors.newFixedThreadPool(queueSize);
    }
    
    /**
     * @return the single instance.
     */
    public static ImageLoaderExector getInstance()
    {
        return instance;
    }
    
    /**
     * Requests an immediate shutdown of the processing queue.
     */
    public void shutdown()
    {
        imgLoaderExecServ.shutdownNow();
    }
    
    /**
     * Requests an IMage to be loaded using the Queue.
     * @param imageLoadObj
     */
    public Future<ImageLoaderIFace> loadImage(final ImageLoaderIFace imageLoadObj)
    {
        //System.out.println("loadImage  - "+imageLoadObj.getStatus());

        // create a background thread to do the web service work
        @SuppressWarnings("unused") //$NON-NLS-1$
        Callable<ImageLoaderIFace> imgLoadWorker = new Callable<ImageLoaderIFace>()
        {
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            public ImageLoaderIFace call() throws Exception
            {
                try
                {
                    imageLoadObj.load();
                    imageLoadObj.done();
                    return imageLoadObj;                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                return null;
            }
        };
        return imgLoaderExecServ.submit(imgLoadWorker);
    }
    
}