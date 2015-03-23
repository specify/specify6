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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 13, 2010
 *
 */
public class ResFileCompare
{
    private static final Logger log = Logger.getLogger(ResFileCompare.class);
    
    /**
     * 
     */
    public ResFileCompare()
    {
        super();
    }
    
    @SuppressWarnings("unchecked")
    public void fixPropertiesFiles(final String baseFileName, final String lang, final boolean doBranch)
    {
        System.out.println("-------------------- " + baseFileName + " --------------------");
        File engFile;
        File lngFile;
        
        String engName  = String.format("src/%s_en.properties", baseFileName);
        String langName = String.format("src/%s_%s.properties", baseFileName, lang);

        if (doBranch)
        {
            engFile = new File(String.format("/home/rods/workspace/Specify_6202SF/src/%s_en.properties", baseFileName));
            lngFile = new File(String.format("/home/rods/workspace/Specify_6202SF/src/%s_%s.properties", baseFileName, lang));
        } else
        {
            engFile = new File(engName);
            lngFile = new File(langName);
        }

        try
        {
            List<String> engList    = (List<String>)FileUtils.readLines(engFile, "UTF8");
            List<String> lngListTmp = (List<String>)FileUtils.readLines(lngFile, "UTF8");
            
            int lineCnt = -1;
            HashMap<String, String> transHash = new HashMap<String, String>();
            for (String line : lngListTmp)
            {
                lineCnt++;
                
                if (line.startsWith("#") ||
                    StringUtils.deleteWhitespace(line).length() < 3 ||
                    line.indexOf('=') == -1)
                {
                    continue;
                }
                
                String[] toks = StringUtils.split(line, '=');
                if (toks.length > 1)
                {
                    if (toks.length == 2)
                    {
                        transHash.put(toks[0], toks[1]); 
                        
                    } else
                    {
                        StringBuilder sb = new StringBuilder();
                        for (int i=1;i<toks.length;i++)
                        {
                            sb.append(String.format("%s=", toks[i])); 
                        }
                        sb.setLength(sb.length()-1); // chomp extra '='
                        transHash.put(toks[0], sb.toString()); 
                    }
                } else
                {
                    log.error("Skipping:["+line+"] Line:"+lineCnt);
                }
            }

            log.info(String.format("Lines Eng: %d;  Terms Hash size: %s: %d", engList.size(), lang, transHash.size()));
            
            File dir = new File("translations");
            if (!dir.exists())
            {
                if (!dir.mkdir())
                {
                    log.error("Unable to create directory["+dir.getAbsolutePath()+"]");
                    return;
                }
            }
            
            File        transFile       = new File(dir.getPath()+File.separator+langName.substring(4));
            PrintWriter transFileOutput = new PrintWriter(transFile, "UTF8");
            
            for (String line : engList)
            {
                if (line.startsWith("#") ||
                    StringUtils.deleteWhitespace(line).length() < 3 ||
                    line.indexOf('=') == -1)
                {
                    transFileOutput.println(line);
                    continue;
                }
                
                boolean  doMove = true;
                String[] toks   = StringUtils.split(line, '=');
                if (toks.length > 1)
                {
                    String key   = null;
                    String value = null;
                    if (toks.length == 2)
                    {
                        key   = toks[0]; 
                        value = toks[1];
                        
                    } else
                    {
                        key   = toks[0]; 
                        StringBuilder sb = new StringBuilder();
                        for (int i=1;i<toks.length;i++)
                        {
                            sb.append(String.format("%s=", toks[i])); 
                        }
                        sb.setLength(sb.length()-1); // chomp extra '='
                        value = sb.toString();
                    }
                    
                    if (key != null)
                    {
                        String text = transHash.get(key);
                        transFileOutput.println(String.format("%s=%s", key, text != null ? text : value));
                        
                        if (text == null)
                        {
                            log.info("Adding new term: "+key);
                        }
                        doMove = false;
                    } else
                    {
                        log.info("Adding new term: "+key);
                    }
                }
                
                if (doMove)
                {
                    transFileOutput.println(line);
                }
            }
            
            transFileOutput.flush();
            transFileOutput.close();
            
            log.info(String.format("Write file: %s", transFile.getPath()));
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Save as Ascii.
     */
    public boolean save(final File file, final Vector<String> list)
    {
        FileOutputStream fos = null;
        DataOutputStream dos = null;

        try
        {
            fos = new FileOutputStream(file);
            dos = new DataOutputStream(fos);
            
            for (String line : list)
            {
                String str = line + '\n';
                dos.writeBytes(str);
            }
            dos.flush();
            dos.close();
            return true;
            
        } catch (Exception e)
        {
            System.out.println("e: " + e);
        }
        return false;
    }
    

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        String[] fileNames = {"backuprestore", "common", "expresssearch", "global_views", "masterusrpwd", 
                "preferences", "resources", "specify_plugins", "specifydbsetupwiz", "stats", 
                "system_setup", "views"};
        //String[] fileNamesX = {"preferences",};
        String lang = "pt";
        
        ResFileCompare resFileFix = new ResFileCompare();
        for (String baseFileName : fileNames)
        {
            resFileFix.fixPropertiesFiles(baseFileName, lang, false);
        }
    }
}
