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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.ku.brc.specify.ui.forms.persist.FormViewFactory;


/**
 * An XML Helper, this currently is for w3c DOM, probably won't need this since we will be switch to DOM4J
 * 
 * @author Rod Spears
 */
public class XMLHelper
{
    private static boolean _printAll = false; // indicates whether compareDOMS should print all the node info
    final static Logger   log = Logger.getLogger(XMLHelper.class);
    /**
     * Returns the String representation of the Node Type
     * @param aType Node type to get the string description of
     */
	  public static String getNodeTypeStr(int aType)
   {
     String str = "N/A";
     switch (aType) 
     {
       case Node.ATTRIBUTE_NODE:        str = "The node is an Attr."; break;
       case Node.CDATA_SECTION_NODE:    str = "The node is a CDATASection."; break;
       case Node.COMMENT_NODE:          str = "The node is a Comment."; break;
       case Node.DOCUMENT_FRAGMENT_NODE: str = "The node is a DocumentFragment."; break;
       case Node.DOCUMENT_NODE:         str = "The node is a Document."; break;
       case Node.DOCUMENT_TYPE_NODE:    str = "The node is a DocumentType."; break;
       case Node.ELEMENT_NODE:          str = "The node is an Element."; break;
       case Node.ENTITY_NODE:           str = "The node is an Entity."; break;
       case Node.ENTITY_REFERENCE_NODE: str = "The node is an EntityReference."; break;
       case Node.NOTATION_NODE:         str = "The node is a Notation."; break;
       case Node.PROCESSING_INSTRUCTION_NODE: str = "The node is a ProcessingInstruction."; break;
       case Node.TEXT_NODE:             str = "The node is a Text node."; break; 
     }
     return str;
   }

   /** ------------------------------------------------------------
    * Gets the String value of a node. First checks it's value and if
    * that is null then it checks to see if it has a child node and gets
    * the value of the first child.
    * Assumption: That the first child is a #text node, 
    *             delibertly NOT checking the first node's type
    * @param aNode Parent to search (should be the document root)
    * @return Returns the value of the node
    * -------------------------------------------------------------- */
   public static String getNodeValue(Node aNode)
   {
     String value = null;
     if (aNode.getNodeValue() != null)
     {
       value = aNode.getNodeValue() != null ? aNode.getNodeValue().trim() : null;
     }
     else 
     {
       NodeList list = aNode.getChildNodes();
       if (list.getLength() > 0)
       {
           for (int i=0;i<list.getLength();i++)
           {
		         Node child = list.item(i);
		         if (child != null && child.getNodeType() == Node.TEXT_NODE) 
		         {
		            value = child.getNodeValue() != null ? child.getNodeValue().trim() : null;
		         }
           }
       } else {
         //log.info("For Node "+aNode.getNodeName()+" Num Child: "+list.getLength());
       }
     }
     return value;
   }

   /** ------------------------------------------------------------
    * Finds the first node of a given type
    * @param aNode Parent to search (should be the document root)
    * @param aName Name of node to find
    * @return Returns the node of that name or null
    * -------------------------------------------------------------- */
   public static Node findNode(Node aNode, String aName)
   {
       String name = aNode.getNodeName() != null ? aNode.getNodeName().trim() : "";
       if (aName.equalsIgnoreCase(name)) 
       {
           return aNode;
       }

       NodeList list = aNode.getChildNodes();
       if (list != null)
       {
           for (int i = 0; i < list.getLength(); i++)
           {
               Node child = list.item(i);
               if (child != null)
               {
                   Node node = findNode(child, aName);
                   if (node != null)
                   {
                       return node;
                   }
               }
           }
       }
     return null;
   }
   
   /**
    * Find a node in the vector of node names 
    * @param aNode the current parent node
    * @param aNodeNames a list of node names to be search
    * @param aDepth the current index of name in the list to be searched for
    * @return the found node
    */
   protected static Node findNodeWithXPath(Node aNode, Vector aNodeNames, int aDepth)
   {
       if (aDepth == aNodeNames.size())
       {
           return null;
       }
       String pathNodeName = (String)aNodeNames.elementAt(aDepth);
       NodeList list = aNode.getChildNodes();
       if (list != null)
       {
           for (int i=0;i<list.getLength();i++) 
           {
               Node child = list.item(i);
               if (child != null)
               {
                   String nodeName = child.getNodeName();
                   if (pathNodeName.equals(nodeName))
                   {
                       if (aDepth == aNodeNames.size()-1)
                       {
                           return child;
                       }
                       Node node = findNodeWithXPath(child, aNodeNames, aDepth+1);
                       if (node != null) 
                       {
                         return node;
                       }
                   }
               }
           }
       }
       return null;
   }
   
   /**
    * Find a single node in a DOM tree with an XPath string. 
    * If the path starts with a '/' then the first name MUST match the 
    * the root name of the DOM, if not then the first name is the name of 
    * a child node.
    * @param aRootNode the root node of the search
    * @param aXPath the XPath string of names to be searched
    * @return the node
    */
   public static Node findNodeWithXPath(Node aRootNode, String aXPath)
   {
       Vector nodeNames = new Vector();
       StringTokenizer st = new StringTokenizer(aXPath, "/");
       while (st.hasMoreTokens()) {
           nodeNames.addElement(st.nextToken());
       }
       
       int depth = 0;
       /*if (aXPath.charAt(0) == '/')
       {
           String nodeName = aRootNode.getNodeName() != null ? aRootNode.getNodeName().trim() : "";
           String pathNodeName = (String)nodeNames.elementAt(0);
           if (!nodeName.equals(pathNodeName)) 
           {
             return null;
           }
           depth = 1;
       }*/
       
       return findNodeWithXPath(aRootNode, nodeNames, depth);
   }
  
   /** ------------------------------------------------------------
    * Searches for the Node by the param "aName" and returns its value
    * @param aNode Parent to search 
    * @param aName Name of node to find
    * @return Returns the node's value as a string
    * -------------------------------------------------------------- */
   public static String findNodeValue(Node aNode, String aName)
   {
     String value = null;
     Node node = findNode(aNode, aName);
     if (node != null)
     {
       value = getNodeValue(node);
     }
     return value;
   }

   /** ------------------------------------------------------------
    * Gets an attribute value for the named node.
    * @param aNode Parent to search (should be the document root)
    * @param aName Name of node to find
    * @param aAttr Name of attribute to return
    * @return Returns the attribute's value as a string
    * -------------------------------------------------------------- */
   public static String findAttrValueForNode(Node aNode, String aName, String aAttr)
   {
     String value = null;
     Node node = findNode(aNode, aName);
     if (node != null)
     {
       NamedNodeMap map = node.getAttributes();
       if (map != null)
       {
         Node attrNode = map.getNamedItem(aAttr);
         if (attrNode != null)
         {
           value = getNodeValue(attrNode);
         }
       }
     }
     return value;
   }

   /** ------------------------------------------------------------
    * Gets an attribute value for the node.
    * @param aNode Parent to search (should be the document root)
    * @param aAttr Name of attribute to return
    * @return Returns the attribute's value as a string
    * -------------------------------------------------------------- */
   public static String findAttrValue(Node aNode, String aAttr)
   {
     String value = null;
     if (aNode != null)
     {
       NamedNodeMap map = aNode.getAttributes();
       if (map != null)
       {
         Node attrNode = map.getNamedItem(aAttr);
         if (attrNode != null)
         {
           value = getNodeValue(attrNode);
         }
       }
     }
     return value;
   }
   
   /**
    * Returns an int from a string attribute
    * @param aNode the node
    * @param aAttrName the attr name
    * @return Returns an int from a string attribute
    */
   public static int getIntFromAttr(Node aNode, String aAttrName)
   {
       String attr = findAttrValue(aNode, aAttrName);
       if (attr != null)
       {
           try
           {
               return Integer.parseInt(attr);
           } catch (Exception e){}
       }
       return -1;
   }

   /**
     * Prints a DOM Tree (recursive)
     * @param aNode parent node of tree to be printed
     * @param aLevel indicates the current indentation level
     */
	  public static void printNode(Node aNode, int aLevel)
   {
     if (aNode == null) {
       return;
     }

     String spaces = "";
     for (int i=0;i<aLevel;i++) 
     {
       spaces += "..";
     }

     log.info(spaces+"Name:  " + aNode.getNodeName());
     log.info(spaces+"Type:  " + aNode.getNodeType());
     log.info(spaces+"Value: [" + aNode.getNodeValue()+"]");
     NodeList list = aNode.getChildNodes();
     if (list != null) {
       for (int i=0;i<list.getLength();i++) 
       {
         Node child = list.item(i);
         printNode(child, aLevel+1);
       }
     }
   }

   /**
     * Reads in an XML document and returns a String of the file's contents
     * @param aFileName file name of XML file to be read
     */
	  public static String readXMLFile2Str(String aFileName)
   {
     try 
     {
       String buffer = "";
       FileReader     fileReader = new FileReader(aFileName);
       BufferedReader bufReader  = new BufferedReader(fileReader);
       String line = bufReader.readLine();
       while (line != null) {
         buffer += line + "\n";
         line = bufReader.readLine();
       }
       return buffer;
     } // try
     catch (Exception e) 
     {
    	 log.error("readXMLFile2Str - Exception: ", e);
     }
     return null;
   }

   /**
     * Reads in an XML document and returns the Document node of the DOM tree
     * @param aFileName file name of XML file to be read
     */
	  public static Document readXMLFile2DOM(String aFileName)
   {
       if (aFileName == null)
       {
    	   log.error("***  readXMLFile2DOM file name is null!");
           return null;
       }
       
       if (aFileName.length() == 0)
       {
    	   log.error("***  readXMLFile2DOM file name is null!");
           return null;
       }
       
       File file = new File(aFileName);
       if (file == null || !file.exists() || file.length() == 0)
       {
    	   log.error("***  readXMLFile2DOM - file "+aFileName+" does not exist or is zero length!");
           return null;
       }
       
       try {
         TransformerFactory tFactory = TransformerFactory.newInstance();

         if (tFactory.getFeature(DOMSource.FEATURE) && tFactory.getFeature(DOMResult.FEATURE))
         {
           //Instantiate a DocumentBuilderFactory.
           DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();

           // And setNamespaceAware, which is required when parsing xsl files
           dFactory.setNamespaceAware(true);

           //Use the DocumentBuilderFactory to create a DocumentBuilder.
           DocumentBuilder dBuilder = dFactory.newDocumentBuilder();

           //Use the DocumentBuilder to parse the XML input.
           Document xmlDoc = dBuilder.parse(aFileName);

           //printNode(xmlDoc, 0);

           return xmlDoc;
   
         }
       } // try
       catch (org.xml.sax.SAXParseException e) {
    	   log.error("Tried Reading["+aFileName+"]");
    	   log.error("readXMLFile2DOM - Exception: ", e);
         //String xmlString = readXMLFile2Str(aFileName);
         //log.error("XML Dump " + aFileName);
         //log.error("------------------------------------------");
         //logger.error(xmlString);
         //logger.error("------------------------------------------");
       }
       catch (Exception e) {
    	   log.error("Tried Reading["+aFileName+"]");
    	   log.error("readXMLFile2DOM - Exception: ", e);
       }
       return null;
   }

   /**
     * Convert/Parses an XML string into a DOM tree
     * @param aXMLStr XML string (document)
     */
	  public static Document convertXMLStr2DOM(String aXMLStr)
   {
     try 
     {
	      TransformerFactory tFactory = TransformerFactory.newInstance();

       if (tFactory.getFeature(DOMSource.FEATURE) && tFactory.getFeature(DOMResult.FEATURE))
       {
         //Instantiate a DocumentBuilderFactory.
         DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();

         // And setNamespaceAware, which is required when parsing xsl files
         dFactory.setNamespaceAware(true);

         //Use the DocumentBuilderFactory to create a DocumentBuilder.
         DocumentBuilder dBuilder = dFactory.newDocumentBuilder();

         StringReader strReader = new StringReader(aXMLStr);
         InputSource inpSrc = new InputSource(strReader);

         //Use the DocumentBuilder to parse the XML input.
         Document xmlDoc = dBuilder.parse(inpSrc);

         return xmlDoc;
 
	      }
     } // try
     catch (Exception e) {
    	 log.error("convertXMLStr2DOM - Exception: ", e);
    	 log.error("XML Dump");
    	 log.error("------------------------------------------");
    	 log.error(aXMLStr);
    	 log.error("------------------------------------------");
       //e.printStackTrace();
     }
     return null;
   }
   
   /**
     * Writes an XML DOM to a file
     * @param aFileName The file name to be written
     * @param aDOM THe DOM to be serialized to a file.
     */
	  public static boolean writeXMLToFile(String aFileName, Document aDOM)
   {/*
     try
     {
       File file = new File(aFileName);
       if (file != null)
       {
         file.createNewFile();
         FileOutputStream fileOut = new FileOutputStream(file);
         if (file != null)

         {
           Serializer serializer = SerializerFactory.getSerializer
                                  (OutputPropertiesFactory.getDefaultMethodProperties("xml"));
           //serializer.setOutputStream(fileOut);
           serializer.setOutputStream(System.out);
           //logger.info(aDOM.getNode());
           serializer.asDOMSerializer().serialize(aDOM);
           return true;
         }
       }
     }
     catch (Exception e)
     {
       logger.error("writeXMLToFile - " , e);
     }*/
     return false;
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

}
