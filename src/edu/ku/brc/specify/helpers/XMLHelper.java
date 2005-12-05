/* Filename:    $RCSfile: XMLHelper.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:28 $
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
package edu.ku.brc.specify.helpers;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.dom4j.io.SAXReader;


/**
 * An XML Helper, this currently is for w3c DOM, probably won't need this since we will be switch to DOM4J
 * 
 * @author Rod Spears
 */
public class XMLHelper
{
    // Static Data Members
    final static Logger log = Logger.getLogger(XMLHelper.class);

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
   
   /**
     * Returns full path to file in config directory
     * @param fileName the name of the file to be read
     * @return the path to the file
     */
   public static String getConfigDirPath(final String fileName)
   {
       java.io.File f = new java.io.File(".");
       return f.getAbsolutePath() +  File.separator + "config" + File.separator + fileName;
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
    * Escape String for HTML/XML (code borrowed from DiGIR
    * @param s string to be escaped
    * @return escaped string
    */
  public static final String escapeHTML(String s)
  {
    if (s == null || s.length() == 0) {
      return "";
    }

    StringBuffer sb = new StringBuffer();
    int n = s.length();
    for (int i = 0; i < n; i++)
    {
      char c = s.charAt(i);
      switch (c)
      {
        case '<': sb.append("&lt;"); break;
        case '>': sb.append("&gt;"); break;
        case '"': sb.append("&quot;"); break;
        case '&': sb.append("&amp;"); break;
      //all these should be uncommented so the escaping is complete
/*
        case '': sb.append("&agrave;");break;
        case '': sb.append("&Agrave;");break;
        case '': sb.append("&acirc;");break;
        case '': sb.append("&Acirc;");break;
        case '': sb.append("&auml;");break;
        case '': sb.append("&Auml;");break;
        case '': sb.append("&aring;");break;
        case '': sb.append("&Aring;");break;
        case '': sb.append("&aelig;");break;
        case '': sb.append("&AElig;");break;
        case '': sb.append("&ccedil;");break;
        case '': sb.append("&Ccedil;");break;
        case '': sb.append("&eacute;");break;
        case '': sb.append("&Eacute;");break;
        case '': sb.append("&egrave;");break;
        case '': sb.append("&Egrave;");break;
        case '': sb.append("&ecirc;");break;
        case '': sb.append("&Ecirc;");break;
        case '': sb.append("&euml;");break;
        case '': sb.append("&Euml;");break;
        case '': sb.append("&iuml;");break;
        case '': sb.append("&Iuml;");break;
        case '': sb.append("&ocirc;");break;
        case '': sb.append("&Ocirc;");break;
        case '': sb.append("&ouml;");break;
        case '': sb.append("&Ouml;");break;
        case '': sb.append("&oslash;");break;
        case '': sb.append("&Oslash;");break;
        case '': sb.append("&szlig;");break;
        case '': sb.append("&ugrave;");break;
        case '': sb.append("&Ugrave;");break;         
        case '': sb.append("&ucirc;");break;         
        case '': sb.append("&Ucirc;");break;
        case '': sb.append("&uuml;");break;
        case '': sb.append("&Uuml;");break;
        case '': sb.append("&reg;");break;         
        case '': sb.append("&copy;");break;   
        case '': sb.append("&euro;"); break;
        // be carefull with this one (non-breaking whitee space)
        case ' ': sb.append("&nbsp;");break;
        */         
        default:  sb.append(c); break;
      }
    }

    return sb.toString();
  }

  
}
