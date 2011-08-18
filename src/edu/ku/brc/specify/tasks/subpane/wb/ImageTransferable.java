/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.helpers.ImageFilter;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 18, 2011
 *
 */
public abstract class ImageTransferable extends TransferHandler 
{
    private final String MIME_TYPE = "text/uri-list";
    private ImageFilter filter = new ImageFilter();
    /**
     * 
     */
    public ImageTransferable()
    {
        super();
        
    }

    /**
     * @param fileList
     */
    protected abstract void processImages(final Vector<File> fileList);


    /* (non-Javadoc)
     * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
     */
    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
    {
        for (DataFlavor df : transferFlavors)
        {
            if (df.getHumanPresentableName().equals(MIME_TYPE) && df.getRepresentationClass() == String.class)
            {
                return true;
                /*try
                {
                   
                    String uris = (String)trans.getTransferData(df);
                    String[] filePaths = StringUtils.split(uris, "\r\n");
                    for (String path : filePaths)
                    {
                        return filter.isImageFile(path);
                    }
                } catch (UnsupportedFlavorException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }*/
            }
        }
        System.out.println("canImport1: false");
        return false;
    }

    /* (non-Javadoc)
     * @see javax.swing.TransferHandler#canImport(javax.swing.TransferHandler.TransferSupport)
     */
    @Override
    public boolean canImport(TransferSupport support)
    {
        Transferable trans = support.getTransferable();
        DataFlavor[] flavors = trans.getTransferDataFlavors();
        for (DataFlavor df : flavors)
        {
            if (df.getHumanPresentableName().equals(MIME_TYPE) && df.getRepresentationClass() == String.class)
            {
                return true;
                /*try
                {
                    String uris = (String)trans.getTransferData(df);
                    
                    String[] filePaths = StringUtils.split(uris, "\r\n");
                    for (String path : filePaths)
                    {
                        System.out.println("canImport2: "+filter.isImageFile(path));
                        return filter.isImageFile(path);
                    }
                } catch (UnsupportedFlavorException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }*/
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.TransferSupport)
     */
    @Override
    public boolean importData(TransferSupport support)
    {
        Vector<File> fileList = new Vector<File>();
        
        try
        {
            Transferable trans = support.getTransferable();
            DataFlavor[] flavors = trans.getTransferDataFlavors();
            for (DataFlavor df : flavors)
            {
                if (df.getHumanPresentableName().equals("text/uri-list") && df.getRepresentationClass() == String.class)
                {
                    String uris = (String)trans.getTransferData(df);
                    String[] filePaths = StringUtils.split(uris, "\r\n");
                    for (String path : filePaths)
                    {
                        URI uri = URI.create(path);
                        File f = new File(uri);
                        if (filter.isImageFile(path))
                        {
                            fileList.add(f);
                        }
                    }
                    break;
                }
            }
            
        } catch (UnsupportedFlavorException e)
        {
            e.printStackTrace();
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        if (fileList.size() > 0)
        {
            processImages(fileList);
            return true;
        }
        
        return false;
    }
}