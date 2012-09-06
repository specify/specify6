/* Copyright (C) 2012, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.images;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.tasks.subpane.lm.BlueMarbleFetcher;
import edu.ku.brc.specify.tasks.subpane.lm.BufferedImageFetcherIFace;
import edu.ku.brc.ui.ImageDisplay;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 31, 2012
 *
 */
public class ImageInfoPanel extends JPanel
{
    public static int IMG_SIZE = 300;

    // Image Members
    protected ImageDisplay               imgDisplay;
    protected ImageDataItem              imgDataItem = null;
    
    // Map Members
    protected BufferedImage              blueMarble      = null;
    protected BufferedImageFetcherIFace  blueMarbleListener;
    //protected BufferedImage              renderImage     = null;
    protected ImageIcon                  markerImg;
    
    protected ImageDisplay               blueMarbleDisplay;
    protected BlueMarbleFetcher          blueMarbleFetcher;
    protected CollectionDataFetcher      dataFetcher;
    
    /**
     * 
     */
    public ImageInfoPanel(final CollectionDataFetcher dataFetcher)
    {
        super();
        
        this.dataFetcher = dataFetcher;
        
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        setBackground(Color.WHITE);
        setOpaque(true);
        
        blueMarbleDisplay = new ImageDisplay(IMG_SIZE, IMG_SIZE/2, false, false);
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p,8px,f:p:g,p"), this);
        
        imgDisplay = new ImageDisplay(IMG_SIZE, IMG_SIZE, false, false);
        Dimension s = new Dimension(IMG_SIZE, IMG_SIZE);
        imgDisplay.setSize(s);
        imgDisplay.setPreferredSize(s);
        
        pb.add(imgDisplay, cc.xy(2, 1));
        pb.add(blueMarbleDisplay, cc.xy(2, 4));
        
        imgDisplay.setBackground(Color.WHITE);
        imgDisplay.setOpaque(true);

        setBackground(Color.WHITE);
        setOpaque(true);
        
        blueMarbleFetcher = new BlueMarbleFetcher(IMG_SIZE, IMG_SIZE/2, new BufferedImageFetcherIFace() {
            @Override
            public void imageFetched(BufferedImage image)
            {
                blueMarble = image;
                blueMarbleDisplay.setImage(blueMarble);
                
                ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
                colorConvert.filter(blueMarble, blueMarble);

                // 0.21 R + 0.71 G + 0.07 B
            }

            @Override
            public void error()
            {
            }
        });
        blueMarbleFetcher.init();
        markerImg = blueMarbleFetcher.getMarkerImg();
    }
    
    /**
     * @param imgDataItem the imgDataItem to set
     */
    public void setImgDataItem(final ImageDataItem imgDataItem)
    {
        this.imgDataItem = imgDataItem;
        if (imgDataItem != null)
        {
            ImageIcon img = imgDataItem.getImgIcon();
            if (img == null || img.getIconWidth() == ImageDataItem.STD_ICON_SIZE)
            {
                imgDataItem.loadScaledImage(IMG_SIZE, new ImageLoaderListener()
                {
                    @Override
                    public void imagedLoaded(String imageName,
                                             String mimeType,
                                             boolean doLoadFullImage,
                                             int scale,
                                             boolean isError, 
                                             ImageIcon imgIcon,
                                             File localFile)
                    {
                        imgDisplay.setImage(imgIcon);
                    }
                });
            } else
            {
                //System.out.println(img);
                imgDisplay.setImage(img);
            }
        } else
        {
            imgDisplay.setImage((ImageIcon)null);
        }
        
        if (imgDataItem != null)
        {
            boolean isPointSet = false;
            HashMap<String, Object> map = dataFetcher.getData(imgDataItem.getAttachmentId(), imgDataItem.getTableId());
            if (map != null)
            {
                BigDecimal lat = (BigDecimal)map.get("Latitude1");
                BigDecimal lon = (BigDecimal)map.get("Longitude1");
                if (lat != null && lon != null)
                {
                    BufferedImage bi = blueMarbleFetcher.plotPoint(lat.doubleValue(), lon.doubleValue());
                    blueMarbleDisplay.setImage((Image)null);  
                    blueMarbleDisplay.setImage(bi);  
                    isPointSet = true;
                }
            }
            if (!isPointSet)
            {
                blueMarbleDisplay.setImage(blueMarble);                
            }
        }
    }
    
}
