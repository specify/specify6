/*
* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute, 1345 Jayhawk Boulevard,
 * Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package edu.ku.brc.specify.plugins.imgproc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.HybridBinarizer;

/**
 * @author rods
 * 
 * @code_status Alpha
 * 
 * Sep 12, 2011
 * 
 */
public class BarCodeDecoder
{
    Result               result = null;
    private int          successful;
    private final Config config = new Config();

    /**
     * 
     */
    public BarCodeDecoder()
    {
    }

    /**
     * @return
     */
    public int getSuccessful()
    {
        return successful;
    }

    /**
     * @param uri
     * @return
     * @throws IOException
     */
    public Result decode(final File file) throws IOException
    {
        Hashtable<DecodeHintType, Object> hints = config.getHints();
        BufferedImage image;
        try
        {
            image = ImageIO.read(file);
        } catch (IllegalArgumentException iae)
        {
            throw new FileNotFoundException("Resource not found: " + file.getAbsolutePath());
        }

        if (image == null)
        {
            System.err.println(file.toString() + ": Could not load image");
            return null;
        }
        try
        {
            LuminanceSource source;
            if (config.getCrop() == null)
            {
                source = new BufferedImageLuminanceSource(image);
            } else
            {
                int[] crop = config.getCrop();
                source = new BufferedImageLuminanceSource(image, crop[0], crop[1], crop[2], crop[3]);
            }
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            // if (config.isDumpBlackPoint())
            // {
            // dumpBlackPoint(uri, image, bitmap);
            // }
            result = new MultiFormatReader().decode(bitmap, hints);
            if (config.isBrief())
            {
                System.out.println(file.getAbsolutePath() + ": Success");
            } else
            {
                ParsedResult parsedResult = ResultParser.parseResult(result);
                System.out.println(file.getAbsolutePath() + " (format: "
                        + result.getBarcodeFormat() + ", type: " + parsedResult.getType()
                        + "):\nRaw result:\n" + result.getText() + "\nParsed result:\n"
                        + parsedResult.getDisplayResult());

                System.out.println("Found " + result.getResultPoints().length + " result points.");
                for (int i = 0; i < result.getResultPoints().length; i++)
                {
                    ResultPoint rp = result.getResultPoints()[i];
                    System.out.println("  Point " + i + ": (" + rp.getX() + ',' + rp.getY() + ')');
                }
            }

            return result;

        } catch (NotFoundException nfe)
        {
            System.out.println(file.getAbsolutePath() + ": No barcode found");
            return null;
        }
    }
    
    /**
     * @return
     */
    public String getNumber()
    {
        if (result != null)
        {
            ParsedResult parsedResult = ResultParser.parseResult(result);
            if (parsedResult != null)
            {
                return result.getText();
            }
        }
        return null;
    }
}