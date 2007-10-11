/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
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
    
    protected String englishXSLT = "specifyschema.xslt";
    protected String germanXSLT  = "specifyschema_de.xslt";
    
    protected String englishOUT = "SpecifySchema.html";
    protected String germanOUT  = "SpecifySchemaDE.html";
    
    protected void process() throws TransformerException,
                                    TransformerConfigurationException, 
                                    FileNotFoundException, 
                                    IOException
    {
        boolean doGerman    = false;
        String  outFileName = "UIControls.html";
        
        TransformerFactory tFactory = TransformerFactory.newInstance();
        if (false)
        {
           
          Transformer transformer = tFactory.newTransformer(new StreamSource("src/edu/ku/brc/specify/utilapps/uicontrols.xslt"));
          transformer.transform(new StreamSource("UIControls.xml"), new StreamResult(new FileOutputStream("UIControls.html")));
          
        } else
        {
            String xsltFileName = doGerman ? germanXSLT : englishXSLT;
            
            outFileName  = doGerman ? germanOUT : englishOUT;
            
            Transformer transformer = tFactory.newTransformer(new StreamSource("src/edu/ku/brc/specify/utilapps/" + xsltFileName));
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
                ex.printStackTrace();
            }
        }
        System.out.println("** The output file["+outFileName+"] is written.");

    }
    
    public static void main(String[] args) throws TransformerException,
                                                  TransformerConfigurationException, 
                                                  FileNotFoundException, 
                                                  IOException
    {
        UIControlTOHTML uth = new UIControlTOHTML();
        uth.process();
        
    }
}