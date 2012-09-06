/* Copyright (C) 2012, University of Kansas Center for Research
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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import static edu.ku.brc.ui.UIRegistry.getAppDataDir;
import static org.apache.commons.lang.StringUtils.countMatches;
import static org.apache.commons.lang.StringUtils.replaceChars;
import static org.apache.commons.lang.StringUtils.split;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;

import edu.ku.brc.helpers.XMLHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 29, 2012
 *
 */
public class FirstLastVerifier extends LuceneHelperBase
{
    public enum SuffixType {eNone, eJr, eII, eIII};
    
    private static SuffixType suffix;
    
    /**
     * 
     */
    public FirstLastVerifier()
    {
        super();
        FILE_INDEX_DIR = new File(getAppDataDir() + File.separator + "firstlast-index");
        
        buildIndex();
    }
    
    /**
     * 
     */
    protected void buildIndex()
    {
        initLuceneForIndexing(true);
        buildFromFile(true);
        buildFromFile(false);
        
        try
        {
            writer.close();
            writer = null;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * @param isLastName
     */
    protected void buildFromFile(final boolean isLastName)
    {
        File file = XMLHelper.getConfigDir("nameresources" + File.separator + (isLastName ? "last" : "first") + "names.list"); 
        try
        {
            for (String name : (List<String>)FileUtils.readLines(file))
            {
                Document doc = new Document();
                String fld = isLastName ? "last" : "first";
                doc.add(new Field(fld,  name,  Field.Store.YES, Field.Index.ANALYZED));
                
                try
                {
                    writer.addDocument(doc);
                } catch (CorruptIndexException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @param fieldName
     * @param searchText
     * @return
     */
    private boolean search(final String fieldName, final String searchText)
    {
        if (parser == null)
        {
            initLuceneforReading(fieldName);
        }
        try
        {
            String srchTxt  = StringUtils.replace(searchText, ")", "");
            srchTxt = StringUtils.replace(srchTxt, "(", "");
            srchTxt = StringUtils.replace(srchTxt, ":", "");
            srchTxt = StringUtils.replace(srchTxt, "?", "");
            
            Query query = parser.parse(fieldName+":"+srchTxt.toUpperCase());
            int                  hitsPerPage = 10;
            TopScoreDocCollector collector   = TopScoreDocCollector.create(hitsPerPage, true);
            searcher.search(query, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            
            //System.out.println("Hits: "+(hits != null ? hits.length : 0));
            
            return hits.length > 0;
            
        } catch (Exception e)
        {
            System.err.println(e.getMessage());
            //e.printStackTrace();
        }
        return false;
    }
    
    /**
     * @param name
     * @return
     */
    public boolean isFirstName(final String name)
    {
        return search("first", name);
    }


    /**
     * @param name
     * @return
     */
    public boolean isLastName(final String name)
    {
        return search("last", name);
    }
    
    /**
     * 
     */
    public void shutdown()
    {
        if (analyzer != null) analyzer.close();
        if (searcher != null)
        {
            try
            {
                searcher.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * @param strs
     * @return
     */
    private static int indexOfLargest(final String...strs)
    {
        int inx = -1;
        int max = 0;
        for (int i=0;i<strs.length;i++)
        {
            if (strs[i].length() > max)
            {
                max = strs[i].length();
                inx = i;
            }
        }
        return inx;
    }
    
    /**
     * @param nameStr
     * @return
     */
    public static String[] parseName(final String nameStr)
    {
        return parseName(nameStr, true);
    }
    
    /**
     * @param str
     * @param ending
     * @return
     */
    private static String removeSuffix(final String str, final String ending)
    {
        /*System.out.println();
        for (int j=0;j<str.length();j++) System.out.print((j % 10));
        System.out.println();
        System.out.println(str);*/
        
        byte[] s = str.getBytes();
        int    i = str.length() - ending.length() - 1;
        do
        {
           //System.out.println(i+" -> "+(char)s[i]);
           if (s[i] == ' ') 
           {
               i--;
           } else if (s[i] == ',')
           {
               
           } else
           {
               break;
           }
        } while (i > 0);
        
        if (s[i] != ',') i++;
        return str.substring(0, i);
    }
    
    /**
     * @param nameStr
     * @param doFirstLetter
     * @return
     */
    public static String[] parseName(final String nameStr, 
                                     final boolean doFirstLetter)
    {
        suffix = SuffixType.eNone;
    
        int numPeriods    = countMatches(nameStr, ".");
        int numCommas     = countMatches(nameStr, ",");
        int numSemiColons = countMatches(nameStr, ";");
        int numAppr       = countMatches(nameStr, "&");
        int numAnds       = countMatches(nameStr, " and ");
        
        if (numSemiColons > 0 || numCommas > 1 || numAnds > 0 || numAppr > 0) // probably a list of agents
        {
            return null;
        }
        
        String   str    = replaceChars(nameStr, ',', ' ');
        str             = replaceChars(str, ".", "");
        
        String tmp = str.trim().toLowerCase();
        for (SuffixType sufxType : SuffixType.values())
        {
            String sfx = getSuffixStr(sufxType);
            if (sfx != null && tmp.endsWith(sfx.toLowerCase()))
            {
                str = removeSuffix(str, sfx.toLowerCase());
                suffix = sufxType;
                break;
            }
        }
        
        String[] tokens = split(str, " ");
        
        if (tokens.length > 3)
        {
            return null;
        }
        
        String os1 = null;
        String os2 = null;
        String os3 = null;
        
        int bigInx = indexOfLargest(tokens);
        
        if (tokens.length == 3)
        {
            String s1 = tokens[0];
            String s2 = tokens[1];
            String s3 = tokens[2];
            
            if (bigInx == 0)
            {
                os1 = s1;
                if (s2.length() < 3 && s3.length() < 3)
                {
                    if (s2.length() == 1 && s3.length() == 1)
                    {
                        os2 = (StringUtils.isNotEmpty(s2) ? (s2 + ".") : "") + (StringUtils.isNotEmpty(s3) ? (s3 + ".") : "");
                        os3 = null;
                    } else 
                    {
                        os2 = s2;
                    }
                } else  if (s2.length()  > s3.length())
                {
                    os2 = s2;
                    os3 = s3;
                } else
                {
                    os2 = s3;
                    os3 = s2;
                }
            } else if (bigInx == 2)
            {
                os1 = s3;
                if (s1.length() < 3 && s2.length() < 3)
                {
                    os2 = s1;
                    os3 = s2;
                } else if (s1.length()  > s2.length())
                {
                    os2 = s1;
                    os3 = s2;
                } else
                {
                    os2 = s2;
                    os3 = s1;
                }
            } else if (s1.length() < 3 && s3.length() < 3)
            {
                os1 = s3;
                os2 = s1;
                os3 = s2 != null ? s2 : null; 
            } else 
            {
                os1 = s2;
                os2 = s1;
                os3 = s3 != null ? s3 : null; 
            }
        } else if (tokens.length == 2)
        {
            String s1 = tokens[0];
            String s2 = tokens[1];
            
            if (s1.length() > 2 && s2.length() > 2)
            {
                boolean oneComma = numCommas == 1;
                os1 = oneComma ? s1 : s2;
                os2 = oneComma ? s2 : s1;
                
            } else if (s1.length() > 2 && s2.length() == 1)
            {
                os1 = (StringUtils.isNotEmpty(s1) ? (s1 + ".") : "");
                os2 = (StringUtils.isNotEmpty(s2) ? (s2 + ".") : "");
            } else if (s1.length() == 1 && s2.length() > 2)
            {
                os1 = s2;
                os2 = (StringUtils.isNotEmpty(s1) ? (s1 + ".") : "");
            } else
            {
                os1 = bigInx == 0 ? s1 : s2;
                os2 = bigInx == 0 ? s2 : s1;
            }
        } else if (tokens.length == 0)
        {
            return null;
        }
        
        if (doFirstLetter && os2 != null && os2.length() == 2)
        {
            os2 = os2.substring(0,1);
        }
        
        int len = os2 != null ? os2.length() : 0;
        if (numPeriods > 0 && (len == 1 || len == 2) && !os2.endsWith("."))
        {
            os2 = os2.charAt(0) + "." + (len == 2 ? (os2.charAt(1) + ".") : "");
        }
        
        if (os3 != null)
        {
            return new String[] {os1, os2, os3};
        }
        if (os2 != null)
        {
            return new String[] {os1, os2};
        }
        if (os1 != null)
        {
            return new String[] {os1};
        }
        return null;
    }
    
    /**
     * @param name
     * @return name if there is no suffix
     */
    public static String appendSuffixTo(final String name)
    {
        if (hasSuffix())
        {
            return name + ", " + getSuffixStr();
        }
        return name;
    }
    
    /**
     * 
     */
    public static void testLastNames()
    {
        String[] names = {"F. E. Lewis", "Jones, Bill C", "B.R. JOnes", "Jones BR", "Jones B.R.", "BR Jones", "Bill Jones", "Jones, Bill", "Romero A.", "Bill C Jones", "C. Bill Jones", 
                          "C.B. Flemming, Jr.", "C.B. Flemming, Jr", "C.B. Flemming Jr.",
                          "Y. Chang & R.E. Brooks", "C.A. Morse et al.", "Tamrya D. d'Artenay (MVZ)", "Catherine E. Dana, Kristina Yamamoto (MVZ)", };
        int i = 0;
        for (String lastName : names)
        {
            System.out.print(i+" - Input: ["+lastName+"] ");
            String[] nms = parseName(lastName, false);
            if (nms != null)
            {
                for (i=0;i<nms.length;i++) System.out.print("   "+i+" ["+nms[i]+"]");
                if (hasSuffix()) 
                {
                    System.out.print(", "+getSuffixStr());
                }
                System.out.println();
            } else
            {
                System.out.println(" - Can't parse: "+lastName);   
            }
            i++;
        }
    }

    /**
     * @return
     */
    public static boolean hasSuffix()
    {
        return suffix != null && suffix != SuffixType.eNone;
    }
    
    /**
     * @return the suffix
     */
    public static SuffixType getSuffix()
    {
        return suffix;
    }
    
    /**
     * @param sufx
     * @return
     */
    public static String getSuffixStr(final SuffixType sufx)
    {
        if (sufx != null)
        {
            switch (sufx)
            {
                case eJr: return "Jr";
                case eII: return "II";
                case eIII: return "III";
                default:
                    return null;
            }
        }
        return null;
    }
    
    /**
     * @return
     */
    public static String getSuffixStr()
    {
        return getSuffixStr(suffix);
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        if (true)
        {
            testLastNames();
            return;
        }
        FirstLastVerifier flv = new FirstLastVerifier();
        System.out.println(flv.isFirstName("Bill"));
        System.out.println(flv.isLastName("Bill"));
        
        System.out.println(flv.isFirstName("Johnson"));
        System.out.println(flv.isLastName("Johnson"));
        
        try
        {
            if (false)
            {
                for (String nm : new String[] {"firstnames", "lastnames"})
                {
                    File file = new File("/Users/rods/Downloads/"+nm+".txt");
                    try
                    {
                        PrintWriter pw = new PrintWriter("/Users/rods/Downloads/"+nm+".list");
                        for (String line : (List<String>)FileUtils.readLines(file))
                        {
                            String[] toks = StringUtils.split(line, '\t');
                            if (toks != null && toks.length > 0)
                                pw.println(toks[0]);
                        }
                        pw.close();
                        
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            
            Vector<String> lnames = new Vector<String>();
            File file = XMLHelper.getConfigDir("lastnames.list"); 
            if (false)
            {
                for (String name : (List<String>)FileUtils.readLines(file))
                {
                    if (flv.isFirstName(name))
                    {
                        System.out.println(name+" is first.");
                    } else
                    {
                        lnames.add(name);
                    }
                }
                Collections.sort(lnames);
                FileUtils.writeLines(file, lnames);
            }
            
            lnames.clear();
            file = XMLHelper.getConfigDir("firstnames.list");
            for (String name : (List<String>)FileUtils.readLines(file))
            {
                if (flv.isLastName(name))
                {
                    System.out.println(name+" is first.");
                } else
                {
                    lnames.add(name);
                }
            }
            Collections.sort(lnames);
            //FileUtils.writeLines(file, lnames);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
