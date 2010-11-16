/* Copyright (C) 2009, University of Kansas Center for Research
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
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

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

    /**
     * 
     */
    public ResFileCompare()
    {
        super();
    }
    
    @SuppressWarnings("unchecked")
    public void compare(final String baseFileName, final String lang, final boolean doBranch)
    {
        boolean doWrite = false;
        System.out.println("-------------------- " + baseFileName + " --------------------");
        File engFile;
        File lngFile;
        
        if (doBranch)
        {
            engFile = new File(String.format("/home/rods/workspace/Specify_6202SF/src/%s_en.properties", baseFileName));
            lngFile = new File(String.format("/home/rods/workspace/Specify_6202SF/src/%s_%s.properties", baseFileName, lang));
        } else
        {
            engFile = new File(String.format("src/%s_en.properties", baseFileName));
            lngFile = new File(String.format("src/%s_%s.properties", baseFileName, lang));
        }
        
        try
        {
            List<String> engListTmp = (List<String>)FileUtils.readLines(engFile, "UTF8");
            List<String> lngListTmp = (List<String>)FileUtils.readLines(lngFile, "UTF8");
            
            Vector<String> engList = new Vector<String>(engListTmp);
            Vector<String> lngList = new Vector<String>(lngListTmp);
            
            System.out.println(String.format("Lines Eng: %d;  Lines %s: %d", engList.size(), lang, lngList.size()));
            
            boolean isOK = true;
            int numLines = Math.min(engList.size(), lngList.size());
            int lineCnt  = 0;
            while (lineCnt < numLines)
            {
                String eStr = engList.get(lineCnt);
                String lStr = lngList.get(lineCnt);
                
                /*if ((StringUtils.isEmpty(eStr) && StringUtils.isNotEmpty(lStr)) ||
                    (StringUtils.isNotEmpty(eStr) && StringUtils.isEmpty(lStr)))
                {
                    System.out.println(String.format("0 - Line: %d [%s][%s]", (lineCnt+1), eStr, lStr));
                    isOK = false;
                    break;
                }*/
                
                int eInx = eStr.indexOf('=');
                int lInx = lStr.indexOf('=');
                if (eInx > -1 && lInx > -1)
                {
                    if (eInx != lInx)
                    {
                        System.out.println(String.format("1 - Line: %d [%s][%s]", (lineCnt+1), eStr, lStr));
                        isOK = false;
                        break;
                        
                    } else
                    {
                        String e = eStr.substring(0, eInx);
                        String l = lStr.substring(0, lInx);
                        if (!e.equals(l))
                        {
                            System.out.println(String.format("2 - Line: %d [%s][%s]  %d / %d", (lineCnt+1), e, l, eInx, lInx));
                            isOK = false;
                            break;
                        }
                    }
                } else if (!eStr.equals(lStr))
                {
                    
                    if (StringUtils.getLevenshteinDistance(eStr, lStr) < 5)
                    {
                        System.out.println(String.format("5 - Line: %d [%s][%s]", (lineCnt+1), eStr, lStr));
                        isOK = false;
                        break;
                        
                    } else
                    {
                        System.out.println(String.format("3 - Line: %d [%s][%s]", (lineCnt+1), eStr, lStr));
                        lngList.insertElementAt(eStr, lineCnt);
                        lineCnt--;
                        numLines++;
                        doWrite = true;
                        break;
                    }
                }
                
                lineCnt++;
                if (lineCnt % 100 == 0)
                {
                    System.out.println(lineCnt);
                }
            }
            
            /*if (isOK && engList.size() > lngList.size())
            {
                while (lineCnt < engList.size())
                {
                    lngList.add(engList.get(lineCnt));
                    lineCnt++;
                }
                doWrite = true;
                
            } else*/ if (engList.size() != lngList.size())
            {
                System.out.println(String.format("4 - File Lengths different: %d  - %d", engList.size(), lngList.size()));
                isOK = false;
            }
            
            System.out.println(String.format("File %s is %s", baseFileName, isOK ? "OK" : "NOT ok ************"));
            //System.out.println(String.format("Lines Eng: %d;  Lines %s: %d", engList.size(), lang, lngList.size()));
            
            if (doWrite)
            {
                //save(lngFile, lngList);
            }
            
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
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
        
        ResFileCompare compare = new ResFileCompare();
        for (String baseFileName : fileNames)
        {
            compare.compare(baseFileName, lang, true);
        }
    }
}
