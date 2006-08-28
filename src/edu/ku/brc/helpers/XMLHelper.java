/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.helpers;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


/**
 * An XML Helper, this currently is for w3c DOM, probably won't need this since we will be switch to DOM4J
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 */
public class XMLHelper
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(XMLHelper.class);

   /**
    * Reads a File and return the root element from the DOM
    * @param file the file to be read
    */
    public static org.dom4j.Element readFileToDOM4J(final File file) throws Exception
    {
        return readFileToDOM4J(new FileInputStream(file));
    }

   /**
    * Reads a DOM from a stream
    * @param fileinputStream the stream to be read
    * @return the root element of the DOM
    */
   public static org.dom4j.Element readFileToDOM4J(final FileInputStream fileinputStream) throws Exception
   {
       SAXReader saxReader= new SAXReader();

       saxReader.setValidation(false);
       saxReader.setStripWhitespaceText(true);

       //saxReader.setFeature("http://apache.org/xml/features/validation/schema", false);
       //saxReader.setFeature("http://xml.org/sax/features/validation", false);
       //saxReader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
       //                   (FormViewFactory.class.getResource("../form.xsd")).getPath());

       org.dom4j.Document document = saxReader.read( fileinputStream );
       return document.getRootElement();
   }

   public static org.dom4j.Element readStrToDOM4J(final String data) throws Exception
   {
       SAXReader saxReader= new SAXReader();

       saxReader.setValidation(false);
       saxReader.setStripWhitespaceText(true);

       //saxReader.setFeature("http://apache.org/xml/features/validation/schema", false);
       //saxReader.setFeature("http://xml.org/sax/features/validation", false);
       //saxReader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
       //                   (FormViewFactory.class.getResource("../form.xsd")).getPath());

       org.dom4j.Document document = saxReader.read( new StringReader(data) );
       return document.getRootElement();
   }

   /**
    * Returns full path to file in config directory
    * @param fileName the name of the file to be read
    * @return the path to the file
    */
    public static String getConfigDirPath(final String fileName)
    {
        String path = new File(".").getAbsolutePath();
        if (path.endsWith("."))
        {
            path = path.substring(0, path.length() - 2);
        }
        return path + File.separator + "config" + (fileName != null ? (File.separator + fileName) : "");
    }

    /**
     * Returns File object to file in config directory
     * 
     * @param fileName the name of the file to be read
     * @return the path to the file
     */
    public static File getConfigDir(final String fileName)
    {
        return new File(getConfigDirPath(fileName));
    }

   /**
     * Reads file from the config directory
     * @param fileName the name of the file to be read
     * @return the root DOM lement
     * @throws Exception any file io exceptions
     */
   public static org.dom4j.Element readDOMFromConfigDir(final String fileName) throws Exception
   {
       try
       {
           java.io.File f = new java.io.File(".");
           String cwd = f.getAbsolutePath() +  File.separator + "config" + File.separator + fileName;

           return XMLHelper.readFileToDOM4J(new File(cwd));

       } catch (Exception ex)
       {
           log.error(ex);
       }
       return null;
   }

   /**
    * Get a string attribute value from an element value
    * @param element the element to get the attribute from
    * @param attrName the name of the attribute to get
    * @param defValue the default value if the attribute isn't there
    * @return the attr value or the default value
    */
   public static String getAttr(final Element element, final String attrName, final String defValue)
   {
       String str = element.attributeValue(attrName);
       return str != null ? str : defValue;
   }

   /**
    * Get a int attribute value from an element value
    * @param element the element to get the attribute from
    * @param attrName the name of the attribute to get
    * @param defValue the default value if the attribute isn't there
    * @return the attr value or the default value
    */
   public static int getAttr(final Element element, final String attrName, final int defValue)
   {
       String str = element.attributeValue(attrName);
       return isNotEmpty(str) ? Integer.parseInt(str) : defValue;
   }

   /**
    * Get a int attribute value from an element value
    * @param element the element to get the attribute from
    * @param attrName the name of the attribute to get
    * @param defValue the default value if the attribute isn't there
    * @return the attr value or the default value
    */
   public static boolean getAttr(final Element element, final String attrName, final boolean defValue)
   {
       String str = element.attributeValue(attrName);
       return isNotEmpty(str) ? Boolean.parseBoolean(str.toLowerCase()) : defValue;
   }
   
   /**
    * Returns the contents of a file as a string
    * @param file the file to be read
    * @return the contents as a string
    */
   public static String getContents(final File file) 
   {
       StringBuilder   contents = new StringBuilder();
       BufferedReader input    = null;
       try 
       {
           String eol = System.getProperty("line.separator");
           input = new BufferedReader(new FileReader(file));
           
           String line = null;
           while (( line = input.readLine()) != null)
           {
               contents.append(line);
               contents.append(eol);
           }
       } catch (FileNotFoundException ex) 
       {
           ex.printStackTrace();
         
       } catch (IOException ex)
       {
           ex.printStackTrace();
         
       } finally 
       {
           try 
           {
               if (input!= null) 
               {
                   input.close();
               }
           } catch (IOException ex) 
           {
               ex.printStackTrace();
           }
       }
       return contents.toString();
   }
   
   /**
    * Writes out the contents.
    * @param outFile outFile
    * @param contents contents
    * @throws FileNotFoundException FileNotFoundException
    * @throws IOException IOException
    */
   public static void setContents(final File outFile, final String contents) throws FileNotFoundException, IOException
   {
       if (outFile == null) { throw new IllegalArgumentException("File should not be null."); }
       if (outFile.exists())
       {
           if (!outFile.isFile()) 
           { 
               throw new IllegalArgumentException("Should not be a directory: "+ outFile); 
           }
           if (!outFile.canWrite()) 
           { 
               throw new IllegalArgumentException("File cannot be written: "+ outFile); 
           }
       }

       Writer output = null;
       try
       {
           output = new BufferedWriter(new FileWriter(outFile));
           output.write(contents);
           
       } finally
       {
           if (output != null)
           {
               output.close();
           }
       }
   }
}
