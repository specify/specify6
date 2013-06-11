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
package edu.ku.brc.helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Feb 5, 2013
 *
 */
public class ImageMetaDataHelper
{
    /**
     * @param metaData
     * @param cls
     * @param tagId
     * @return
     */
    public static Calendar getExifFileDate(final Metadata metadata, final Class<? extends Directory> cls, final int tagId)
    {
        if (metadata != null)
        {
            Directory directory = metadata.getDirectory(cls);
            if (directory != null)
            {
                Date date = directory.getDate(tagId);
                if (date != null)
                {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    return cal.get(Calendar.YEAR) > 1850 ? cal : null;
                }
            }
        }
        return null;
    }
    
    /**
     * Retrieves the internal 'embedded' date in the image from the EXIF or metadata. It first check for DATE_ORIGINAL and then DATETIME
     * @param file image file to be checked
     * @return the embedded date or the current date.
     */
    public static Calendar getEmbeddedDate(final File file)
    {
        return getEmbeddedDate(file,  Calendar.getInstance());
    }
    
    /**
     * Retrieves the internal 'embedded' date in the image from the EXIF or metadata. It first check for DATE_ORIGINAL and then DATETIME
     * @param file image file to be checked
     * @return the embedded date or the 'last modified date' of the file.
     */
    public static Calendar getEmbeddedDateOrFileDate(final File file)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(file.lastModified());
        return getEmbeddedDate(file, cal);
    }
    
    /**
     * Retrieves the internal 'embedded' date in the image from the EXIF or metadata. It first check for DATE_ORIGINAL and then DATETIME
     * @param file image file to be checked
     * @param defaultDate the date that will be returned if one doesn't exist in the metadata.
     * @return the embedded date or the default date that was passed in.
     */
    public static Calendar getEmbeddedDate(final File file, final Calendar defaultDate)
    {
        try
        {
            BufferedInputStream bufInp = new BufferedInputStream(new FileInputStream(file));
            String mimeType = URLConnection.guessContentTypeFromStream(bufInp);
            bufInp.close();
            
            System.err.println("MimeType: "+mimeType);
            if (mimeType != null && mimeType.startsWith("image"))
            {
                Metadata metadata = ImageMetadataReader.readMetadata(file);
                Calendar fileCreateCal = getExifFileDate(metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (fileCreateCal == null)
                {
                    fileCreateCal = getExifFileDate(metadata, ExifIFD0Directory.class, ExifIFD0Directory.TAG_DATETIME);
                }
            }
        } catch (ImageProcessingException e)
        {
            //e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return defaultDate;
    }

    /**
     * Retrieves the internal Exif metadata and returns it as JSON.
     * @param file the image file
     * @return JSON
     */
    public static String getJSONMetaData(final File file)
    {
        if (file == null || !file.exists()) return null;
        
        StringBuilder sb = new StringBuilder("[");
        
        try
        {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            Calendar fileCreateCal = getExifFileDate(metadata, ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (fileCreateCal == null)
            {
                fileCreateCal = getExifFileDate(metadata, ExifIFD0Directory.class, ExifIFD0Directory.TAG_DATETIME);
            }
            
            int cnt = 0;
            for (Directory directory : metadata.getDirectories())
            {
                if (cnt > 0) sb.append(",\n");
                
                sb.append(String.format("{\"Name\" : \"%s\",\n\"Fields\" : {\n", directory.getName()));
                //System.out.println("\n---------------------------\n" + directory.getName());
                int fCnt = 0;
                for (Tag tag : directory.getTags())
                {
                    if (fCnt > 0) sb.append(",\n");
                    sb.append(String.format("\"%s\" : \"%s\"", tag.getTagName(), tag.getDescription()));
                    //System.out.println(tag);
                    fCnt++;
                }
                sb.append("}}");
                cnt++;
            }
        } catch (ImageProcessingException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        sb.append("]");
        return sb.toString();
    }
}
