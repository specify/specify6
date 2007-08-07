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
        Transformer transformer = tFactory.newTransformer(new StreamSource("src/edu/ku/brc/specify/tests/uicontrols.xslt"));
        transformer.transform(new StreamSource("UIControls.xml"), new StreamResult(new FileOutputStream("UIControls.html")));
        System.out.println("** The output is written.");
    }
}