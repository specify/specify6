/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.utilapps;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.*;

public class UIControlTOHTML
{
    public static void main(String[] args) throws TransformerException,
            TransformerConfigurationException, FileNotFoundException, IOException
    {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        if (false)
        {
          Transformer transformer = tFactory.newTransformer(new StreamSource("src/edu/ku/brc/specify/utilapps/uicontrols.xslt"));
          transformer.transform(new StreamSource("UIControls.xml"), new StreamResult(new FileOutputStream("UIControls.html")));
        } else
        {
            Transformer transformer = tFactory.newTransformer(new StreamSource("src/edu/ku/brc/specify/utilapps/specifyschema.xslt"));
            transformer.transform(new StreamSource("config/specify_datamodel.xml"), new StreamResult(new FileOutputStream("SpecifySchema.html")));
        }
        System.out.println("** The output is written.");
        
        
    }
}