/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.plugins.ipadexporter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * @author from the web (http://http://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java)
 *
 * @code_status Alpha
 *
 * May 23, 2012
 *
 */
public class MD5Checksum
{
    /**
     * @param file
     * @return
     * @throws Exception
     */
    public static String getMD5Checksum(final File file) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.reset();
        InputStream   fis = new FileInputStream(file);
        try {
            //ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fis);
            DigestInputStream     digestInputStream = new DigestInputStream(fis, md);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int ch;
            while ((ch = digestInputStream.read()) >= 0) {
              byteArrayOutputStream.write(ch);
            }

            byte[] newInput = byteArrayOutputStream.toByteArray();
            return org.apache.commons.codec.digest.DigestUtils.md5Hex(newInput);
        }
        finally {
            fis.close();
        }
    }
    
    public static void main(String args[]) {
        try {
            System.out.println(getMD5Checksum(new File("/Users/rods/Documents/Specify/ipad_export/taxon_types.png")));
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}