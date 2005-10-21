package edu.ku.brc.specify.tests;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;
import edu.ku.brc.specify.config.SpecifyConfig;
import edu.ku.brc.specify.extfilerepos.*;
import edu.ku.brc.specify.extfilerepos.impl.*;

public class ExternalFileRepositoryTest extends TestCase
{
    private static Log log = LogFactory.getLog(SpecifyConfig.class);

    public ExternalFileRepository extFileRepos = null;
    
    public ExternalFileRepositoryTest()
    {
        //System.out.println("In ExternalFileRepositoryTest.");
        
    }
    
    public void setUp()
    {
        log.info("In Setup.");
        try
        {
            SpecifyConfig config = SpecifyConfig.getInstance();
            config.init(null); // do this once
            
        } catch (Exception e)
        {
            log.info("Error with Configuration", e);
        }
        
        extFileRepos = ExternalFileRepository.getInstance();
        
    }
    
    public void testAdd()
    {
        log.info("In testAdd.");
        assertTrue(extFileRepos.put("myfilename", "my description", ExternalFileIFace.MimeTypes.textPlain.getMimeType(), "."));
        log.info("In testAdd.");
    }
    
    public void testUpdate()
    {
        System.out.println("In testUpdate.");
        assertEquals(true, true);
    }
    
    public void testRemove()
    {
        System.out.println("In testRemove.");
        assertTrue(extFileRepos.remove(new Long(1)));
    }
    
}
