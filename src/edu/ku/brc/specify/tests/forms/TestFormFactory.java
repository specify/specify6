/* Filename:    $RCSfile: FormAltView.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.tests.forms;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.ku.brc.specify.ui.forms.persist.FormView;
import edu.ku.brc.specify.ui.forms.persist.FormViewFactory;

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
