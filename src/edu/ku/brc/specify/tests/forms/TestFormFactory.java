package edu.ku.brc.specify.tests.forms;

import java.io.FileInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.tests.PreferenceTest;
import junit.framework.TestCase;
import java.util.Vector;

import java.io.*;
import edu.ku.brc.specify.ui.forms.persist.*;

public class TestFormFactory extends TestCase
{
    private static Log log = LogFactory.getLog(TestFormFactory.class);

    
    protected boolean readFile(File aFile)
    {
        SAXReader reader = new SAXReader();
        try
        {
            FileInputStream fileInputStream = new FileInputStream(aFile);
            reader.setValidation(true);
            reader.setFeature("http://apache.org/xml/features/validation/schema", true);
            reader.setFeature("http://xml.org/sax/features/validation", true);
            reader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", (FormViewFactory.class.getResource("../form.xsd")).getPath());
            
            org.dom4j.Document doc = reader.read( fileInputStream );
            if (doc != null)
            {
                Element root = doc.getRootElement();
                FormViewFactory viewFactory = new FormViewFactory();
                Vector<FormView> views = viewFactory.getViews(root); 
                return views.size() > 0;
            }
        } catch (Exception ex)
        {
            //ex.printStackTrace();
            log.info(ex);  
        }
         
        return false;
    }
    
    protected boolean readFile(String aFileName)
    {
        return readFile(new File(aFileName));
    }
    
    public void testReadValidViewFile()
    {
        log.info("Running Test testReadValidViewFile");
        assertTrue(readFile(TestFormFactory.class.getResource("view_valid.xml").getFile()));
    }
    
    public void testDuplicateViewIds()
    {
        log.info("Running Test testDuplicateViewIds");
        assertFalse(readFile(TestFormFactory.class.getResource("duplicate_view_ids.xml").getFile()));
    }
    
    public void testMissingSubViewId()
    {
        log.info("Running Test testMissingSubViewId");
        assertFalse(readFile(TestFormFactory.class.getResource("missing_subview_id.xml").getFile()));
    }
}
