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
package edu.ku.brc.specify.utilapps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class UIControlTOHTML
{
    protected enum Langs {ePT, ePTBZ}
    protected String[] fileExt = {"_pt", "_pt_bz"};
    protected Langs    currentLang = Langs.ePT;
    
    
    protected String englishXSLT = "specifyschema.xslt";
    protected String foreignXSLT  = String.format("specifyschema%s.xslt", fileExt[currentLang.ordinal()]);
    
    protected String englishOUT = "SpecifySchema.html";
    protected String foreignOUT  = String.format("SpecifySchema%s.html", fileExt[currentLang.ordinal()]);
    
    /**
     * @throws TransformerException
     * @throws TransformerConfigurationException
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected void process() throws TransformerException,
                                    TransformerConfigurationException, 
                                    FileNotFoundException, 
                                    IOException
    {
        boolean doUIControls = true;
        boolean doForeign    = false;
        String  outFileName  = "UIControls.html";
        
        TransformerFactory tFactory = TransformerFactory.newInstance();
        if (doUIControls)
        {
           
          Transformer transformer = tFactory.newTransformer(new StreamSource("src/edu/ku/brc/specify/utilapps/uicontrols.xslt"));
          transformer.transform(new StreamSource("UIControls.xml"), new StreamResult(new FileOutputStream("UIControls.html")));
          
        } else
        {
            String xsltFileName = doForeign ? foreignXSLT : englishXSLT;
            
            outFileName  = doForeign ? foreignOUT : englishOUT;
            
            String filePath = "src/edu/ku/brc/specify/utilapps/" + xsltFileName;
            File transFile = new File(filePath);
            if (!transFile.exists())
            {
                System.err.println("File path["+filePath+"] doesn't exist!");
                System.exit(1);
            }
            System.out.println(filePath);
            
            Transformer transformer = tFactory.newTransformer(new StreamSource(filePath));
            try
            {
                // Need to read it in as a string because of the embedded German characters
                String                 xmlStr   = FileUtils.readFileToString(new File("config/specify_datamodel.xml"));
                DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();
                DocumentBuilder        db       = factory.newDocumentBuilder();
                InputSource            inStream = new InputSource();
                inStream.setCharacterStream(new StringReader(xmlStr));
                Document doc1 = db.parse(inStream);
                
                transformer.transform(new DOMSource(doc1), new StreamResult(new FileOutputStream(outFileName)));
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UIControlTOHTML.class, ex);
                ex.printStackTrace();
            }
        }
        System.out.println("** The output file["+outFileName+"] is written.");

    }
    
    /**
     * @param args
     * @throws TransformerException
     * @throws TransformerConfigurationException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void main(String[] args) throws TransformerException,
                                                  TransformerConfigurationException, 
                                                  FileNotFoundException, 
                                                  IOException
    {
        UIControlTOHTML uth = new UIControlTOHTML();
        uth.process();
        
    }
}
