/**
 * 
 */
package edu.ku.brc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class FileUtils
{
    /**
     * Copy the source file to the destination.
     * 
     * @param src the source filename
     * @param dest the destination filename
     * @throws IOException an error occurred while reading the source file or writing the destination file.
     */
    public static void copyFile( File src, File dest ) throws IOException
    {
        FileChannel sourceChannel = new FileInputStream(src).getChannel();
        FileChannel destinationChannel = new FileOutputStream(dest).getChannel();
        sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);

        // or
        //  destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        sourceChannel.close();
        destinationChannel.close();
    }
    
    public static boolean moveFile( File src, File dest ) throws IOException
    {
        copyFile(src,dest);
        boolean delRes = src.delete();
        return delRes;
    }

}
