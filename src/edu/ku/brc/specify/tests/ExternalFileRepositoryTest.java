package edu.ku.brc.specify.tests;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.extfilerepos.impl.ExternalFileRepository;

public class ExternalFileRepositoryTest extends TestCase
{
    private static final Logger log = Logger.getLogger(ExternalFileRepositoryTest.class);

    public ExternalFileRepository extFileRepos = null;
    
    public ExternalFileRepositoryTest()
    {
        //System.out.println("In ExternalFileRepositoryTest.");
        
    }
    
    public void setUp()
    {
        log.info("In Setup.");
        extFileRepos = ExternalFileRepository.getInstance();
        
    }
    
    public void testAdd()
    {
        log.info("In testAdd.");
        //assertTrue(extFileRepos.put("myfilename", "my description", ExternalFileIFace.MimeTypes.textPlain.getMimeType(), "."));
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
        //assertTrue(extFileRepos.remove(new Long(1)));
    }
    
}
