/*
 * Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute, 1345 Jayhawk Boulevard,
 * Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package edu.ku.brc.af.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 * 
 * @code_status Alpha
 * 
 * Jul 24, 2009
 * 
 */
public class ProcessListUtil
{
    private static String  CSV_PATTERN = "\"([^\"]+?)\",?|([^,]+),?|,";
    private static Pattern csvRE       = Pattern.compile(CSV_PATTERN);

    public static List<String> parse(final String line) 
    {
        List<String> list = new ArrayList<String>();
        Matcher m = csvRE.matcher(line);
        // For each field
        while (m.find()) 
        {
            String match = m.group();
            if (match == null)
            {
                break;
            }
            if (match.endsWith(","))  // trim trailing ,
            {
                match = match.substring(0, match.length() - 1);
            }
            if (match.startsWith("\""))  // assume also ends with
            {
                match = match.substring(1, match.length() - 1);
            }
            if (match.length() == 0)
            {
                match = null;
            }
            list.add(match);
        }
        return list;
    }
    
    /**
     * @return
     */
    public static List<List<String>> getRunningProcessesWin() 
    {
        
        List<List<String>> processList = new ArrayList<List<String>>();
        try 
        {
        	boolean        doDebug = true;
            Process        process = Runtime.getRuntime().exec("tasklist.exe /v /nh /FO CSV");
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) 
            {
                if (!line.trim().isEmpty()) 
                {
                	if (doDebug && StringUtils.contains(line, "mysql"))
                	{
	                	String lineStr = StringUtils.replaceChars(line, '\\', '/');
	                	System.out.println("\n["+lineStr+"]");
	                    for (String tok : parse(lineStr))
	                    {
	                    	System.out.print("["+tok+"]");
	                    }
	                    System.out.println();
	                    
                	}
                    processList.add(parse(StringUtils.replaceChars(line, '\\', '/')));
                }
            }
            input.close();
        }
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
        return processList;
    }

    /**
     * @return
     */
    public static List<List<String>> getRunningProcessesUnix() 
    {
        List<List<String>> processes = new ArrayList<List<String>>();
        try 
        {
            Process        process = Runtime.getRuntime().exec("ps aux");
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) 
            {
                if (!line.trim().equals("")) 
                {
                    //processes.add(line);
                }
            }
            input.close();
        }
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
        return processes;
    }
    
    /**
     * Returns a list of the process ids that contain the provided text (like greping for text).
     * @param text the text to be search for
     * @return the list if ids
     */
    public static List<Integer> getProcessIdWithText(final String...text)
    {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        
        List<List<String>> processList = ProcessListUtil.getRunningProcesses();
        for (List<String> line : processList)
        {
        	System.out.println("["+line+"]");
        	int found = 0;
        	for (String field : line)
            {
	            for (int i=0;i<text.length;i++)
	            {
	            	System.out.println("CHK: ["+field.toLowerCase()+"]["+text[i].toLowerCase()+"]");
	                if (StringUtils.contains(field.toLowerCase(), text[i].toLowerCase()))
	                {
	                	System.out.print("FND: ["+field.toLowerCase()+"]["+text[i].toLowerCase()+"]");
	                	found++;
	                }
	            }
            }
        	
        	System.out.println("***: fnd["+found+"] toks["+text.length+"]");
        	if (found == text.length)
        	{
        		ids.add(Integer.parseInt(line.get(1)));
        	}
        }
        return ids;
    }
    
    
    /**
     * @return
     */
    public static List<List<String>> getRunningProcesses() 
    {
        if (UIHelper.isWindows())
        {
            return getRunningProcessesWin();
        }
        return getRunningProcessesUnix();
    }
    
    /**
     * @param processId
     * @return
     */
    public static boolean killProcess(final int processId)
    {
        String cmd;
        if (UIHelper.isWindows())
        {
            cmd = "taskkill /PID " + processId;
        } else
        {
            cmd = "kill " + processId;
        }
        
        try 
        {
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            return process.exitValue() == 0;
        }
        catch (Exception ex) 
        {
            ex.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        //List<String> processList = getRunningProcesses();

        List<Integer> ids;
        if (!UIHelper.isWindows())
        {
            ids = getProcessIdWithText("3337");
        } else
        {
            ids = getProcessIdWithText("_data/bin/mysqld");
        }
        for (Integer id : ids)
        {
            System.out.println("KILLING: "+id);
            //killProcess(id);
        }
        
        
    }
}