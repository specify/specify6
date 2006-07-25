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

package edu.ku.brc.specify.extfilerepos;

import java.io.File;
import java.sql.Date;

/**
 * Interface representing a single external file in the repository
 
 * @code_status Unknown (auto-generated)
 **
 * @author Rod Spears <rods@ku.edu>
 */

public interface ExternalFileIFace
{
    // Mime TYpe Enumerations
    public enum MimeTypes {
        textPlain("text/plain"),
        textHtml("text/html"),
        
        imageGIF("image/gif"),
        imageJPEG("image/jpeg"),
        imagePNG("image/png"),
        imageTIFF("image/tiff"),
        
        appHTML("application/html"),
        appBin("application/octet-stream"),
        appMacBin("application/mac-binhex40"),
        appPDF("application/pdf"),
        appMSWord("application/msword"),
        appRTF("application/rtf"),
        
        audioBasic("audio/msword"),
        audioWav("audio/x-wav"),
        audioMP4("audio/mp4");
        
        
        MimeTypes(String mimeVal)
        { 
            this.mimeVal = mimeVal;
        }
        
        private String mimeVal;
        public String getMimeType() { return mimeVal; }
        
    }
    
    /**
     * 
     * @return logical name of file
     */
    public String getName();
    
    /**
     * 
     * @return a description of the file
     */
    public String getDescription();
    
    /**
     * 
     * @return the date the file entered
     */
    public Date getDate();
    
    /**
     * 
     * @return the location of the file in the repository
     */
    public File getAsFile();
    
    /**
     * 
     * @return the type of the file
     */
    public String getMimeType();
    
    /**
     * 
     * @return the size of the file in bytes
     */
    public long getFileSize();
    
}
