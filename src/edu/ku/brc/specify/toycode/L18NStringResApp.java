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
package edu.ku.brc.specify.toycode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.google.api.translate.Language;
import com.google.api.translate.Translate;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jan 16, 2011
 *
 */
public class L18NStringResApp
{
    private static final String[] langs        = {"es", "pt", "de", "se", "fr", "it"};
    private static final String   RES_PATH     = "/Users/rods/workspace/SpecifyDroidSF/res/";
    private static final String   SRCFILE_NAME = "values/strings.xml";
    private static final String   ENGFILE_NAME = RES_PATH + SRCFILE_NAME;
    
    protected HashMap<String, Pair<String, String>> baseHash = new HashMap<String, Pair<String,String>>();
    
    protected Locale englishLocale = Locale.US;
    protected Locale destLocale    = new Locale("se");

    
    /**
     * 
     */
    public L18NStringResApp()
    {
        super();
    }
    
    /**
     * @param code
     * @return
     */
    private Language getLangFromCode(final String code)
    {
        if (code.equals("es")) return Language.SPANISH;
        if (code.equals("pt")) return Language.PORTUGUESE;
        if (code.equals("de")) return Language.GERMAN;
        if (code.equals("se")) return Language.SWEDISH;
        if (code.equals("fr")) return Language.FRENCH;
        if (code.equals("it")) return Language.ITALIAN;
        
        return Language.ENGLISH;
    }
    
    /**
     * @param inputText
     */
    protected String translate(final String inputText)
    {
        if (inputText.isEmpty()) return "";
        //System.out.println("\n"+inputText);
        
        Translate.setHttpReferrer("http://www.specifysoftware.org");

        try
        {
            String text = inputText;
            
            boolean hasSpecialChars = false;
            while (StringUtils.contains(text, "%d") || 
                    StringUtils.contains(text, "%s") || 
                    StringUtils.contains(text, "\\n"))
            {
                text = StringUtils.replace(text, "%d", "99");
                text = StringUtils.replace(text, "%s", "88");
                text = StringUtils.replace(text, "\\n", " 77 ");
                hasSpecialChars = true;
            }
            
            Language lang = getLangFromCode(destLocale.getLanguage());
            //System.out.println(text);
            String newText = Translate.execute(text, Language.ENGLISH, lang);
            
            if (hasSpecialChars)
            {
                while (StringUtils.contains(newText, "77") ||
                       StringUtils.contains(newText, "88") ||
                       StringUtils.contains(newText, "99"))
                {
                    newText = StringUtils.replace(newText, "99", "%d");
                    newText = StringUtils.replace(newText, "88", "%s");
                    newText = StringUtils.replace(newText, " 77 ", " \\n ");
                    newText = StringUtils.replace(newText, "77 ", "\\n ");
                    newText = StringUtils.replace(newText, " 77", " \\n");
                }
            }
            //System.out.println(newText);
            return newText;
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Reads a DOM from a stream
     * @param fileinputStream the stream to be read
     * @return the root element of the DOM
     */
    public static org.dom4j.Document readFileToDOM4J(final FileInputStream fileinputStream) throws Exception
    {
        SAXReader saxReader= new SAXReader();

        saxReader.setValidation(false);
        saxReader.setStripWhitespaceText(true);
        
        saxReader.setFeature("http://xml.org/sax/features/namespaces", false);
        saxReader.getXMLReader().setFeature("http://xml.org/sax/features/namespaces",false);

        //saxReader.setFeature("http://apache.org/xml/features/validation/schema", false);
        //saxReader.setFeature("http://xml.org/sax/features/validation", false);
        //saxReader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
        //                   (FormViewFactory.class.getResource("../form.xsd")).getPath());

        return saxReader.read( fileinputStream );
    }

    
    /**
     * @param file
     */
    public void process(final File fileArg, final boolean doDiffs)
    {
        try
        {
            String dirName = RES_PATH + "values-" + destLocale.getLanguage();
            String path    = dirName + File.separator + fileArg.getName();
            
            File file = fileArg;
            if (doDiffs)
            {
                file = new File(path);
            }
            Document doc  = readFileToDOM4J(new FileInputStream(file));
            Node     root = doc.getRootElement();
            
            for (Object nodeObj : root.selectNodes("/resources/string"))
            {
                Node   node      = (Node)nodeObj;
                String name      = XMLHelper.getAttr((Element)node, "name", null);
                
                if (doDiffs)
                {
                    if (baseHash.get(name) != null)
                    {
                        continue;
                    }
                }
                
                String text      = node.getText();
                String transText = translate(text);
                if (transText != null)
                {
                    node.setText(transText);
                }
                System.out.println(name+"["+text+"]["+transText+"]");
            }
            
            File   dir     = new File(dirName);
            if (!dir.exists())
            {
                dir.mkdir();
            }
            
            FileOutputStream fos    = new FileOutputStream(path);
            OutputFormat     format = OutputFormat.createPrettyPrint();
            XMLWriter        writer = new XMLWriter(fos, format);
            writer.write(doc);
            writer.flush();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * @param file
     */
    protected void moveFile(final File file)
    {
        String dirName = RES_PATH + "values-" + destLocale.getLanguage();
        String srcPath = FilenameUtils.getBaseName(file.getName())+"_"+destLocale.getLanguage()+".xml";
        String path   = dirName + File.separator + file.getName();
        
        try
        {
            FileUtils.copyFile(new File(srcPath), new File(path));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     */
    protected void moveAll()
    {
        File file = new File(ENGFILE_NAME);
        for (String lang : langs)
        {
            System.out.println("Processing "+lang);
            destLocale = new Locale(lang);
            moveFile(file);
        }
    }

    /**
     * @param line
     * @return
     */
    /**
     * @param line
     * @return
     */
    private String getKey(final String line)
    {
        int sInx = line.indexOf("name=");
        if (sInx > -1)
        {
            sInx += 6;
            int eInx = line.indexOf('\"', sInx);
            return line.substring(sInx, eInx);
        }
        return null;
    }
    
    private String getText(final String line)
    {
        int sInx = line.indexOf(">");
        if (sInx > -1)
        {
            sInx++;
            int eInx = line.indexOf('<', sInx);
            return line.substring(sInx, eInx);
        }
        return null;
    }
    
    /**
     * @param file
     */
    protected void copyNewLines(final File file)
    {
        String dirName = RES_PATH + "values-" + destLocale.getLanguage();
        String dstPath = dirName + File.separator + file.getName();
        
        try
        {
            List<String>   srcLines = (List<String>)FileUtils.readLines(file, "UTF8");
            Vector<String> dstLines = new Vector<String>((List<String>)FileUtils.readLines(new File(dstPath), "UTF8"));
            
            int dstCnt = 0;
            for (int i=0;i<srcLines.size();i++)
            {
                
                String srcLine = srcLines.get(i);
                String dstLine = dstLines.get(dstCnt++);
                
                if (StringUtils.contains(srcLine, "<?xml") && StringUtils.contains(dstLine, "<?xml")) continue;
                if (StringUtils.contains(srcLine, "<res") && StringUtils.contains(dstLine, "<res")) continue;
                if (StringUtils.contains(srcLine, "</res") && StringUtils.contains(dstLine, "</res")) continue;

                srcLine = StringUtils.replace(srcLine, "\\ \'", "\\\'");
                srcLine = StringUtils.replace(srcLine, "s \\\'", "s\\\'");
                
                dstLine = StringUtils.replace(dstLine, "\\ \'", "\\\'");
                dstLine = StringUtils.replace(dstLine, "s \\ \'", "s\\\'");
                
                System.out.println("--- ["+srcLine+"]["+dstLine+"] -- ");
                
                if (srcLine.equals(dstLine)) continue;
                
                boolean isSrcComment = StringUtils.contains(srcLine, "<!--"); 
                boolean isDstComment = StringUtils.contains(dstLine, "<!--"); 
                
                String srcKey = !isSrcComment ? getKey(srcLine) : "";
                String dstKey = !isDstComment ? getKey(dstLine) : "";
                
                if (srcKey != null && dstKey != null && srcKey.equals(dstKey) && srcKey.length() > 0) continue;
                
                dstLines.insertElementAt(srcLine, dstCnt++);
            }
            
            System.out.println("----------------------------");
            for (int i=0;i<srcLines.size();i++)
            {
                String srcLine = srcLines.get(i);
                String dstLine = dstLines.get(i);
                
                srcLine = StringUtils.replace(srcLine, "\\ \'", "\\\'");
                srcLine = StringUtils.replace(srcLine, "s \\\'", "s\\\'");
                
                dstLine = StringUtils.replace(dstLine, "\\ \'", "\\\'");
                dstLine = StringUtils.replace(dstLine, "s \\ \'", "s\\\'");
                
                System.out.println("--- ["+srcLine+"] "+srcLine.length()+"  ["+dstLine+"] "+dstLine.length()+" -- ");
            }
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     */
    protected void copyNewLinesAll()
    {
        File file = new File(ENGFILE_NAME);
        for (String lang : langs)
        {
            System.out.println("Copying new lines "+lang);
            destLocale = new Locale(lang);
            copyNewLines(file);
            break;
        }
    }
    
    /**
     * @param file
     * @param dstFile
     * @param hash
     */
    protected void mergeFile(final File file, 
                             final File dstFile,
                             final HashMap<String, Pair<String, String>> hash)
    {
        try
        {
            List<String>   srcLines = (List<String>)FileUtils.readLines(file, "UTF8");
            Vector<String> dstLines = new Vector<String>();
            for (int i=0;i<srcLines.size();i++)
            {
                String line = srcLines.get(i);
                if (StringUtils.contains(line, "<string"))
                {
                    String key = getKey(line);
                    String text = null;
                    if (key != null && hash.get(key) != null)
                    {
                        text = hash.get(key).first;
                        
                    } else
                    {
                        String txt = getText(line);
                        text = translate(txt);
                        System.out.println("["+txt+"]["+text+"]");
                    }
                    line = String.format("    <string name=\"%s\">%s</string>", key, text); 
                }
                if (line.endsWith("\n"))
                {
                    line = StringUtils.chomp(line);
                }
                dstLines.add(line);
            }
            
            /*
            System.out.println("----------");
            for (String s : dstLines)
            {
                System.out.print(s);
            }
            */
            
            FileUtils.writeLines(dstFile, "UTF8", dstLines);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * 
     */
    protected void mergeAll()
    {
        HashMap<String, Pair<String, String>> hash = new HashMap<String, Pair<String,String>>();
        
        File file = new File(ENGFILE_NAME);
        for (String lang : langs)
        {
            hash.clear();
            
            System.out.println("-------------------------------------------------");
            System.out.println("Merge new lines "+lang);
            destLocale = new Locale(lang);
            
            String dirName = RES_PATH + "values-" + destLocale.getLanguage();
            String dstPath = dirName + File.separator + file.getName();
            
            File dstFile = new File(dstPath);
            process(dstFile, hash, 0);
            mergeFile(file, dstFile, hash);
        }
    }

    /**
     * @param file
     * @param hash
     * @param index
     */
    public void process(final File file, 
                        final HashMap<String, Pair<String, String>> hash,
                        final int index)
    {
        try
        {
            Document doc  = readFileToDOM4J(new FileInputStream(file));
            Node     root = doc.getRootElement();
            
            for (Object nodeObj : root.selectNodes("/resources/string"))
            {
                Node   node      = (Node)nodeObj;
                String name      = XMLHelper.getAttr((Element)node, "name", null);
                String text      = node.getText();
                
                Pair<String, String> p = hash.get(name);
                if (p == null)
                {
                    p = new Pair<String, String>();
                    hash.put(name, p);
                }
                if (index == 0)
                {
                    p.first = text;
                } else
                {
                    p.second = text;
                }
            }
            
            if (index == 1)
            {
                FileOutputStream fos = new FileOutputStream(FilenameUtils.getBaseName(file.getName())+"_compare"+destLocale.getLanguage()+".txt");
                PrintWriter pw = new PrintWriter(fos);
                
                Vector<String> keys = new Vector<String>(hash.keySet());
                Collections.sort(keys);
                for (String key : keys)
                {
                    Pair<String, String> p = hash.get(key);
                    pw.println(String.format("\n---- %s -----\n%s\n%s", key, chk(p.first), chk(p.second)));
                }
                pw.flush();
                pw.close();
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * @param str
     * @return
     */
    private String chk(final String str)
    {
        return StringUtils.isEmpty(str) ? "<No Translation>" : str;
    }

    /**
     * 
     */
    public void processAll(final boolean doDiffs)
    {
        if (doDiffs)
        {
            loadBaseFile();
        }
        File file = new File(ENGFILE_NAME);
        for (String lang : langs)
        {
            System.out.println("Processing "+lang);
            destLocale = new Locale(lang);
            process(file, doDiffs);
        }
    }
    
    /**
     * 
     */
    public void loadBaseFile()
    {
        File file = new File(RES_PATH + "../strings.xml");
        baseHash.clear();
        process(file, baseHash, 0);
    }
    
    /**
     * 
     */
    public void checkAll()
    {
        HashMap<String, Pair<String, String>> hash = new HashMap<String, Pair<String,String>>();
        
        File file = new File(ENGFILE_NAME);
        for (String lang : langs)
        {
            hash.clear();
            System.out.println("Processing "+lang);
            destLocale = new Locale(lang);
            process(file, hash, 0);
            
            String fName = FilenameUtils.getBaseName(file.getName())+"_"+destLocale.getLanguage()+".xml";
            process(new File(fName), hash, 1);
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        //for (Locale l : Locale.getAvailableLocales())
        //{
            //System.out.println(l.getDisplayLanguage()+" ["+l.getLanguage()+"]");
        //}

        L18NStringResApp app = new L18NStringResApp();
        app.mergeAll();
        //app.processAll(true);
        app.checkAll();
        //app.moveAll();
    }

}
