package edu.ku.brc.specify.tools;

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.helpers.XMLHelper.readStrToDOM4J;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

public class AppendHelp
{
    private Hashtable<String, String>  targetToUrlHash = new Hashtable<String, String>();
    private Hashtable<String, String>  urlToTargetHash = new Hashtable<String, String>();
    private Hashtable<String, String>  targetToAnchor  = new Hashtable<String, String>();
    
    /**
     * Reads a DOM from a stream
     * @param fileinputStream the stream to be read
     * @return the root element of the DOM
     */
    public Element readFileToDOM4J(final File file) throws IOException, DocumentException
    {
        SAXReader saxReader= new SAXReader();

        try
        {
            saxReader.setValidation(false);
            saxReader.setStripWhitespaceText(true);
            //saxReader.setIncludeExternalDTDDeclarations(false);
            //saxReader.setIncludeInternalDTDDeclarations(false);
            saxReader.setIgnoreComments(true);
            //saxReader.setXMLFilter(new TransparentFilter(saxReader.getXMLReader()));
            
            EntityResolver entityResolver = new EntityResolver() 
            {
                public InputSource resolveEntity(String publicId, String systemId) 
                {
                    return new InputSource("");
                }
            };
            saxReader.setEntityResolver(entityResolver);
            
            //saxReader.getXMLFilter().setDTDHandler(null);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        org.dom4j.Document document = saxReader.read( new FileInputStream(file) );
        return document.getRootElement();
    }
    
    /**
     * @param file
     * @param element
     * @return
     * @throws IOException
     */
    private String getXMLStr(final File file, final String element) throws IOException
    {
        String text = FileUtils.readFileToString(file);
        
        int inx = text.indexOf(element);
        System.out.println(text.substring(inx));
        return text.substring(inx);
        
    }
    
    /**
     * @return
     */
    private void getMapEntries(final File directory)
    {
        try
        {
            File mapFile = new File("help/SpecifyHelp.jhm");//$NON-NLS-1$
            Element root = readStrToDOM4J(getXMLStr(mapFile, "<map")); //$NON-NLS-1$
            if (root == null)
            {
                return;
            }
            List<?> sections = root.selectNodes("/map/mapID"); //$NON-NLS-1$
            for ( Iterator<?> iter = sections.iterator(); iter.hasNext(); )
            {
                Element section = (Element)iter.next();

                String target = getAttr(section, "target", null); //$NON-NLS-1$
                String url   = getAttr(section, "url", null); //$NON-NLS-1$
                
                if (StringUtils.isNotEmpty(target) && 
                    StringUtils.isNotEmpty(url) &&
                    !url.startsWith("http")) //$NON-NLS-1$
                {
                    targetToUrlHash.put(target, url);
                    urlToTargetHash.put(url, target);
                    
                    String[] toks = StringUtils.split(url, "#");
                    if (toks.length > 1)
                    {
                        targetToAnchor.put(target, toks[1]);
                    }
                }
            }

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param parent
     * @param list
     * @param sb
     */
    private void buildList(final Element parent, 
                           final Vector<TOCItem> list,
                           final StringBuilder sb)
    {
        List<?> sections = parent.selectNodes("tocitem"); //$NON-NLS-1$
        if (sections.size() > 0)
        {
           sb.append("<UL>\n"); 
           for (Iterator<?> iter = sections.iterator(); iter.hasNext(); )
           {
               Element section = (Element)iter.next();

               String target = getAttr(section, "target", null); //$NON-NLS-1$
               String text   = getAttr(section, "text", null); //$NON-NLS-1$
               
               if (StringUtils.isNotEmpty(target) && 
                   StringUtils.isNotEmpty(text))
               {
                   String anchor = targetToAnchor.get(target);
                   list.add(new TOCItem(target, text));
                   sb.append("<LI>");
                   sb.append("<a href=\"#");
                   sb.append(anchor != null ? anchor : target);
                   sb.append("\">");
                   sb.append(text);
                   sb.append("</a>");
                   sb.append("</LI>\n");
               }
               
               buildList(section, list, sb);
           }
           sb.append("</UL>\n"); 
        }
     }

    /**
     * @param directory
     * @return
     */
    private List<TOCItem> getTOCList(final File directory, final StringBuilder sb)
    {
        Vector<TOCItem> list = new Vector<TOCItem>();
        try
        {
            File tocFile = new File("help/SpecifyHelpTOC.xml");
            Element root = readStrToDOM4J(getXMLStr(tocFile, "<toc")); //$NON-NLS-1$
            if (root == null)
            {
                return null;
            }
            Element parent = (Element)root.selectSingleNode("/toc"); //$NON-NLS-1$
            
            //sb.append("<UL>\n");
            buildList(parent, list, sb);
            //sb.append("</UL>\n");

        } catch (Exception ex)
        {
            ex.printStackTrace();
            // XXX FIXME
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    static public String getContents(final File   file, 
                                     final String target,
                                     final String anchor) throws IOException
    {
        if (file.length() < 500)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        
        String contents = FileUtils.readFileToString(file);
        String lower    = contents.toLowerCase();
        
        int sInx = lower.indexOf("<title>");
        int eInx = lower.indexOf("</title>");
        
        //String title = contents.substring(sInx+8, eInx-1);
        sInx = lower.indexOf("<body");
        sInx = lower.indexOf('>', sInx);
        eInx = lower.indexOf("</body>");
        if (eInx == -1)
        {
            eInx = lower.indexOf("</html>");
        }
        if (sInx == -1 || eInx == -1)
        {
            System.out.println(file.getAbsolutePath()+"  "+sInx+"  "+eInx);
            return "";
        }
        String body = contents.substring(sInx+1, eInx-1);
        
        body = StringUtils.replace(body, "src=\"../images", "src=\"help/SpecifyHelp/images");
        body = StringUtils.replace(body, "src=\"../../images", "src=\"help/images");
        body = StringUtils.replace(body, "background=\"../../images", "background=\"help/images");
        
        if (contents.indexOf("topbar.png") == -1)
        {
            sb.append("<HR>");
        }
        sb.append("<a name=\"");
        sb.append(anchor != null ? anchor : target);
        sb.append("\">");
        sb.append(body);
        
        return sb.toString();
    }

    /**
     * 
     */
    @SuppressWarnings({ "unchecked" })
    public AppendHelp()
    {
        super();
        
        StringBuilder sb = new StringBuilder();
        
        String path = "help/SpecifyHelp";
        
        File spHelpDir = new File(path);
        getMapEntries(spHelpDir);
        
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
        //sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        sb.append("<head>\n");
        //sb.append("<base href=\"help/SpecifyHelp/Workbench\">\n");
        sb.append("<style>body { font-family: sans-serif; }</style>\n");
        sb.append("<link href=\"../../main.css\" rel=\"stylesheet\" type=\"text/css\" media=\"screen\"></link>\n");
        sb.append("<title>Specify Help</title>\n");
        sb.append("</head>\n");
        sb.append("<body bgcolor=\"#ffffff\">\n");
        sb.append("<H1>");
        sb.append("Specify Help");
        sb.append("</H1>\n");

        try
        {
            List<TOCItem> tocList = getTOCList(spHelpDir, sb);
    
            Vector<String>             fileNameList = new Vector<String>();
            Hashtable<String, Boolean> fileNameHash = new Hashtable<String, Boolean>();
            for (TOCItem item : tocList)
            {
                String fName = targetToUrlHash.get(item.getTarget());
                if (fName != null)
                {
                    if (fName.indexOf('#') > -1)
                    {
                        String[] toks     = StringUtils.split(fName, "#");
                        item.setFileName("help/" + toks[0]);
                        item.setAnchor(toks[1]);
                        
                    } else
                    {
                        item.setFileName("help/" + fName);
                    }
                    
                    String fullName = item.getFileName();
                    if (fileNameHash.get(fullName) == null)
                    {
                        fileNameList.add(fullName);
                        fileNameHash.put(fullName, false);
                    }
                    //sb.append(getContents(new File(fileName), p.first, toks.length > 1 ? toks[1] : null));
                }
            }
            
            int i = 0;
            for (TOCItem item : tocList)
            {
                if (item.getFileName() != null)
                {
                    System.out.println(item.getTarget() +"  "+item.getFileName());
                    Boolean used = fileNameHash.get(item.getFileName());
                    if (used != null && !used)
                    {
                        if (i > 0)
                        {
                            sb.append("<HR>\n");
                        }
                            
                        sb.append(getContents(new File(item.getFileName()), item.getTarget(), null));
                        fileNameHash.put(item.getFileName(), true);
                        i++;
                    }
                } else
                {
                    System.err.println("File Name is null for target["+item.getTarget()+"]");
                }
            }
            
            Hashtable<String, Boolean> fNameHash = new Hashtable<String, Boolean>();
            for (String url : urlToTargetHash.keySet())
            {
                fNameHash.put(FilenameUtils.getName(url), true);
            }
            
            for (File file : (Collection<File>)FileUtils.listFiles(new File(path), new String[] {"html"}, true))
            {
                try
                {
                    String fName = FilenameUtils.getName(file.getAbsolutePath());
                    if (fNameHash.get(fName) == null)
                    {
                        if (i > 0)
                        {
                            sb.append("<HR>\n");
                        }
                        sb.append(getContents(file, fName, null));
                        i++;
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

            sb.append("</body></html>");
            FileUtils.writeStringToFile(new File("SpecifyHelp.html"), sb.toString());
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
    }
    
    public class TransparentFilter implements XMLFilter
    {
        private XMLReader parent;

        public TransparentFilter(XMLReader parent)
        {
            this.parent = parent;
        }

        public void setParent(XMLReader parent)
        {
            this.parent = parent;
        }

        public XMLReader getParent()
        {
            return this.parent;
        }

        public boolean getFeature(String name) throws SAXNotRecognizedException,
                SAXNotSupportedException
        {
            return parent.getFeature(name);
        }

        public void setFeature(String name, boolean value) throws SAXNotRecognizedException,
                SAXNotSupportedException
        {
            parent.setFeature(name, value);
        }

        public Object getProperty(String name) throws SAXNotRecognizedException,
                SAXNotSupportedException
        {
            return parent.getProperty(name);
        }

        public void setProperty(String name, Object value) throws SAXNotRecognizedException,
                SAXNotSupportedException
        {
            parent.setProperty(name, value);
        }

        public void setEntityResolver(EntityResolver resolver)
        {
            parent.setEntityResolver(resolver);
        }

        public EntityResolver getEntityResolver()
        {
            return parent.getEntityResolver();
        }

        public void setDTDHandler(DTDHandler handler)
        {
            parent.setDTDHandler(handler);
        }

        public DTDHandler getDTDHandler()
        {
            return new DTDHandler() {

                /* (non-Javadoc)
                 * @see org.xml.sax.DTDHandler#notationDecl(java.lang.String, java.lang.String, java.lang.String)
                 */
                @Override
                public void notationDecl(String name, String publicId, String systemId)
                        throws SAXException
                {
                }

                /* (non-Javadoc)
                 * @see org.xml.sax.DTDHandler#unparsedEntityDecl(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
                 */
                @Override
                public void unparsedEntityDecl(String name,
                                               String publicId,
                                               String systemId,
                                               String notationName) throws SAXException
                {
                }
                
            };
        }

        public void setContentHandler(ContentHandler handler)
        {
            parent.setContentHandler(handler);
        }

        public ContentHandler getContentHandler()
        {
            return parent.getContentHandler();
        }

        public void setErrorHandler(ErrorHandler handler)
        {
            parent.setErrorHandler(handler);
        }

        public ErrorHandler getErrorHandler()
        {
            return parent.getErrorHandler();
        }

        public void parse(InputSource input) throws SAXException, IOException
        {
            parent.parse(input);
        }

        public void parse(String systemId) throws SAXException, IOException
        {
            parent.parse(systemId);
        }
    }

    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        @SuppressWarnings("unused")
        AppendHelp appendHelp = new AppendHelp();

    }

    
    class TOCItem 
    {
        protected String fileName;
        protected String target;
        protected String anchor;
        protected String title;
        
        
        /**
         * @param target
         * @param title
         */
        public TOCItem(String target, String title)
        {
            super();
            this.target = target;
            this.title = title;
        }


        /**
         * @return the fileName
         */
        public String getFileName()
        {
            return fileName;
        }
        /**
         * @return the target
         */
        public String getTarget()
        {
            return target;
        }
        /**
         * @return the anchor
         */
        public String getAnchor()
        {
            return anchor;
        }
        /**
         * @return the title
         */
        public String getTitle()
        {
            return title;
        }
        /**
         * @param fileName the fileName to set
         */
        public void setFileName(String fileName)
        {
            this.fileName = fileName;
        }
        /**
         * @param target the target to set
         */
        public void setTarget(String target)
        {
            this.target = target;
        }
        /**
         * @param anchor the anchor to set
         */
        public void setAnchor(String anchor)
        {
            this.anchor = anchor;
        }
        /**
         * @param title the title to set
         */
        public void setTitle(String title)
        {
            this.title = title;
        }
        
        
    }
}
