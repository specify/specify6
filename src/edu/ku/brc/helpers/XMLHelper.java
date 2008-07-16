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

import static edu.ku.brc.ui.UIRegistry.getResourceString;
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
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.XMLChecksumUtil;


/**
 * An XML Helper, this currently is for w3c DOM, probably won't need this since we will be switch to DOM4J.
 * 
 * (This Class needs to be moved to somewhere probably to the "ui" package.)
 *
 * @code_status Beta
 *
 * @author rods
 */
public class XMLHelper
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(XMLHelper.class);
    private static final String eol = System.getProperty("line.separator"); //$NON-NLS-1$
    
    private static boolean useChecksum = false;
    
    private static File    configDir     = null;
    private static boolean isEmptyAttrOK = false;

    
   /**
     * @param isEmptyAttrOK the isEmptyAttrOK to set
     */
    public static void setEmptyAttrOK(boolean isEmptyAttrOK)
    {
        XMLHelper.isEmptyAttrOK = isEmptyAttrOK;
    }

/**
     * @param useChecksum the useChecksum to set
     */
    public static void setUseChecksum(boolean useChecksum)
    {
        XMLHelper.useChecksum = useChecksum;
    }

/**
    * Reads a File and return the root element from the DOM
    * @param file the file to be read
    */
    public static org.dom4j.Element readFileToDOM4J(final File file) throws Exception
    {
        if (useChecksum && 
            configDir != null && 
            !file.getName().equals("checksum.ini") &&  //$NON-NLS-1$
            !XMLChecksumUtil.checkSignature(file))
        {
            JOptionPane.showMessageDialog(null, getResourceString("XMLHelper.CHECKSUM_MSG"), getResourceString("XMLHelper.CHECKSUM_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
            System.exit(0);
        }
        
        if (!file.exists())
        {
            log.error("the file ["+file+"] doesn't exist."); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }

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
        if (configDir == null)
        {
        	// Check the working path.
            File cfgDir = new File(UIRegistry.getDefaultWorkingPath() + File.separator + "config"); //$NON-NLS-1$
            log.debug("Checking Working Path["+cfgDir.getAbsolutePath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            if (!cfgDir.exists())
            {
            	// Second check to see if the config dir is a child directory
                String path = new File(".").getAbsolutePath(); //$NON-NLS-1$
                if (path.endsWith(".")) //$NON-NLS-1$
                {
                    path = UIHelper.stripSubDirs(path, 1);
                }
                
                // If not then check the working path.
                cfgDir = new File(path + File.separator + "config"); //$NON-NLS-1$
                log.debug("Checking Working Path["+cfgDir.getAbsolutePath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                if (!cfgDir.exists())
                {
                	throw new RuntimeException("Couldn't find config path["+cfgDir.getAbsolutePath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            configDir = cfgDir;
        }
        
        return configDir.getAbsolutePath() + File.separator + (fileName != null ? fileName : ""); //$NON-NLS-1$
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
   public static org.dom4j.Element readDOMFromConfigDir(final String fileName)
   {
       try
       {
           return XMLHelper.readFileToDOM4J(getConfigDir(fileName));

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
    * Get the value of the XML element with the given name that is a child of the given element.
    * 
    * @param element the parent XML element
    * @param name the name of the child element to get the value for
    * @return the data the child element's value
    */
   public static String getValue(final Element element, final String name)
   {
       Element node = (Element)element.selectSingleNode(name);
       if (node != null)
       {
           String data = node.getTextTrim();
           int inx = data.indexOf("("); //$NON-NLS-1$
           if (inx != -1)
           {
               int einx = data.indexOf(")"); //$NON-NLS-1$
               return data.substring(inx+1, einx);
           }
           return data;
       }
       
       // else
       // Although the name may not have been found it could be because no results came back
       log.debug("****** ["+name+"] was not found."); //$NON-NLS-1$ //$NON-NLS-2$
       return ""; //$NON-NLS-1$
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
           // XXX TODO Replace with FileUtils
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
       if (outFile == null) { throw new IllegalArgumentException("File should not be null."); } //$NON-NLS-1$
       if (outFile.exists())
       {
           if (!outFile.isFile()) 
           { 
               throw new IllegalArgumentException("Should not be a directory: "+ outFile);  //$NON-NLS-1$
           }
           if (!outFile.canWrite()) 
           { 
               throw new IllegalArgumentException("File cannot be written: "+ outFile);  //$NON-NLS-1$
           }
       }

       Writer output = null;
       try
       {
           output = new BufferedWriter(new FileWriter(outFile));
           output.write(contents);
           output.flush();
           
       } finally
       {
           if (output != null)
           {
               output.close();
           }
       }
   }
   
   public static void indent(final StringBuilder sb, final int width)
   {
       for (int i=0;i<width;i++)
       {
           sb.append(' ');
       }
   }
   
   public static void addAttr(final StringBuilder sb, final String name, final String value)
   {
       sb.append(' ');
       sb.append(name);
       sb.append("=\""); //$NON-NLS-1$
       sb.append(value == null ? "" : value); //$NON-NLS-1$
       sb.append('\"');
   }
   
   public static void addNode(final StringBuilder sb, final int indent, final String name, final boolean isEnd)
   {
       indent(sb, indent);
       sb.append('<');
       if (isEnd) sb.append('/');
       sb.append(name);
       sb.append('>');
   }
   
   public static void addNode(final StringBuilder sb, final int indent, final String name, final String value)
   {
       addNode(sb, indent, name, false);
       sb.append(value);
       addNode(sb, 0, name, true);
   }
   
   /**
    * Reads in a file of HTML, primarily from the Help directory and fixes up the images
    * to have an absolute path. Plus it strip everything before and including the 'body' tag and
    * strips the '</body>' to the end.
    * @param file the html file to be read.
    * @return the file as a string
    */
   @SuppressWarnings("deprecation") //$NON-NLS-1$
   public static String fixUpHTML(final File file)
   {
       String path = FilenameUtils.getFullPath(file.getAbsolutePath());
       
       StringBuilder sb = new StringBuilder();
       try
       {
           List<?> lines    = FileUtils.readLines(file);
           boolean fndBegin = false;
           
           for (Object lineObj : lines)
           {
               String line = (String)lineObj;
               if (!fndBegin)
               {
                   if (line.indexOf("<body") > -1) //$NON-NLS-1$
                   {
                       fndBegin = true;
                   }
                   continue;
               }
               int inx = line.indexOf("<img"); //$NON-NLS-1$
               if (inx > -1)
               {
                   inx = line.indexOf("src=\"", inx); //$NON-NLS-1$
                   
                   sb.append(line.substring(0, inx+5));
                   File f = new File(path);
                   sb.append(f.toURL()); // needed for 1.5
                   sb.append(line.substring(inx+5, line.length()));
               } else
               {
                   sb.append(line+"\n"); //$NON-NLS-1$
               }
           }
       } catch (IOException ex)
       {
           log.error(ex);
       }
       return sb.toString();
   }
   
   public static void xmlAttr(final StringBuilder sb, final String attr, final String val)
   {
       if (StringUtils.isNotEmpty(val) || isEmptyAttrOK)
       {
           sb.append(' ');
           sb.append(attr);
           sb.append("=\""); //$NON-NLS-1$
           sb.append(val);
           sb.append('\"');
       }
   }
   
   public static void xmlAttr(final StringBuilder sb, final String attr, final Integer val)
   {
       if (val != null || isEmptyAttrOK)
       {
           xmlAttr(sb, attr, val.toString());
       }
   }
   
   public static void xmlAttr(final StringBuilder sb, final String attr, final Short val)
   {
       if (val != null || isEmptyAttrOK)
       {
           xmlAttr(sb, attr, val.toString());
       }
   }
   
   public static void xmlAttr(final StringBuilder sb, final String attr, final Byte val)
   {
       if (val != null || isEmptyAttrOK)
       {
           xmlAttr(sb, attr, val.toString());
       }
   }
   
   public static void xmlAttr(final StringBuilder sb, final String attr, final Boolean val)
   {
       if (val != null || isEmptyAttrOK)
       {
           xmlAttr(sb, attr, val.toString());
       }
   }
   
   public static void xmlNode(final StringBuilder sb, final String tag, final String val, final boolean useCData)
   {
       if (val != null || isEmptyAttrOK)
       {
           sb.append("<"); //$NON-NLS-1$
           sb.append(tag);
           sb.append(">"); //$NON-NLS-1$
           if (useCData) sb.append("<![CDATA["); //$NON-NLS-1$
           sb.append(val);
           if (useCData) sb.append("]]>"); //$NON-NLS-1$
           sb.append("</"); //$NON-NLS-1$
           sb.append(tag);
           sb.append(">\n"); //$NON-NLS-1$
       }
   }
   
   public static void xmlProps(final StringBuilder sb, final Properties props)
   {
       if (props != null)
       {
           int i = 0;
           for (Object key : props.keySet())
           {
               if (i > 0) sb.append(';');
               sb.append(key.toString());
               sb.append("="); //$NON-NLS-1$
               sb.append(props.getProperty(key.toString()));
               i++;
           }
       }
   }
   
   /*public static void xmlAttr(final StringBuilder sb, final String attr, final Boolean val, final Boolean defaultVal)
   {
       if (val != null || (defaultVal != null && val.equals(defaultVal)))
       {
           xmlAttr(sb, attr, val);
       }
   }
   
   public static void xmlAttr(final StringBuilder sb, final String attr, final String val, final String defaultVal)
   {
       if (val != null || (defaultVal != null && val.equals(defaultVal)))
       {
           xmlAttr(sb, attr, val);
       }
   }
   
   public static void xmlAttr(final StringBuilder sb, final String attr, final Integer val, final Integer defaultVal)
   {
       if (val != null || (defaultVal != null && val.equals(defaultVal)))
       {
           xmlAttr(sb, attr, val);
       }
   }
   public static void xmlAttr(final StringBuilder sb, final String attr, final Integer val, final Short defaultVal)
   {
       if (val != null || (defaultVal != null && val.equals(defaultVal)))
       {
           xmlAttr(sb, attr, val);
       }
   }
   public static void xmlAttr(final StringBuilder sb, final String attr, final Byte val, final Byte defaultVal)
   {
       if (val != null || (defaultVal != null && val.equals(defaultVal)))
       {
           xmlAttr(sb, attr, val);
       }
   }*/


}
