package edu.ku.brc.specify.tests;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;

import edu.ku.brc.helpers.UIHelper;


public class MiscUtilTest extends TestCase
{
    private static final Logger log = Logger.getLogger(MiscUtilTest.class);
    /**
     * Tests the Date formating 
     */
    public void testSimpleDateFormat()
    {
        Calendar calendar = new GregorianCalendar();
        
        calendar.clear(); // clear time components
        
        calendar.set(Calendar.YEAR, 1845);
        calendar.set(Calendar.MONTH, 9);       // Oct
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        
        log.info(formatter.format(calendar.getTime()));
        assertTrue(formatter.format(calendar.getTime()).equals("10/01/1845"));

        int origDate = 18451001;
        
        Date date = UIHelper.convertIntToDate(origDate);
        
        log.info("date:     "+date.getTime());
        log.info("calendar: "+calendar.getTimeInMillis());
        
        assertTrue(date.getTime() == calendar.getTimeInMillis());
        
        int iDate = UIHelper.convertDateToInt(date);
        
        log.info("origDate: "+origDate);
        log.info("iDate:    "+iDate);
        
        assertTrue(iDate == origDate);
        
        log.info("10/01/1845");
        log.info(formatter.format(date));
        assertTrue(formatter.format(date).equals("10/01/1845"));
        
    }
    
    public void testExif()
    {
        try
        {
            //File jpegFile = new File(new URL("http://129.237.201.166/exif_image.jpg").toURI());
            File jpegFile = new File("/var/www/html/exif_image.jpg");
            Metadata metadata = JpegMetadataReader.readMetadata(jpegFile);
            
            Iterator directories = metadata.getDirectoryIterator();
            while (directories.hasNext()) {
                Directory directory = (Directory)directories.next();
                // iterate through tags and print to System.out
                Iterator tags = directory.getTagIterator();
                log.info("*** ["+directory.getName()+"]");
                while (tags.hasNext()) {
                    Tag tag = (Tag)tags.next();
                    // use Tag.toString()
                    String name = tag.getTagName();
                    String desc = tag.getDescription(); // the tag's value
                    
                    log.info("["+name+"]["+desc+"]");
                }
            }
        } catch (JpegProcessingException ex)
        {
            ex.printStackTrace();
            
        } catch (MetadataException ex)
        {
            ex.printStackTrace();
        } //catch (URISyntaxException ex)
        //{
        //    ex.printStackTrace();
        //    
        //} catch (MalformedURLException ex)
        //{
        //    ex.printStackTrace();
        //}
    }
}
