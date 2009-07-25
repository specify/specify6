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

import java.io.*;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 * 
 * @code_status Alpha
 * 
 *              Jul 24, 2009
 * 
 */
public class ProcessListUtil
{
    public static List<String> getRunningProcessesWin() 
    {
        List<String> processList = new ArrayList<String>();
        try 
        {
            Process        process = Runtime.getRuntime().exec("tasklist.exe /v /nh");
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) 
            {
                if (!line.trim().equals("")) 
                {
                    // keep only the process name
                    //line = line.substring(1);
                    //processes.add(line.substring(0, line.indexOf("\"")));
                    processList.add(line);
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
    public static List<String> getRunningProcessesUnix() 
    {
        List<String> processes = new ArrayList<String>();
        try 
        {
            Process        process = Runtime.getRuntime().exec("ps aux");
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) 
            {
                if (!line.trim().equals("")) 
                {
                    processes.add(line);
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
        
        List<String> processList = ProcessListUtil.getRunningProcesses();
        for (String line : processList)
        {
            //System.out.println(line);
            boolean doCont = false;
            for (int i=0;i<text.length;i++)
            {
                if (!StringUtils.contains(line.toLowerCase(), text[i].toLowerCase()))
                {
                    doCont = true;
                    break;
                }
            }
            if (doCont)
            {
                continue;
            }
            
            String[] toks = StringUtils.split(line, ' ');
            
            if (UIHelper.isWindows())
            {
            	ids.add(Integer.parseInt(toks[1]));
            } else
            {
                ids.add(Integer.parseInt(toks[1]));
            }
        }
        return ids;
    }
    
    
    /**
     * @return
     */
    public static List<String> getRunningProcesses() 
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
        	ids = getProcessIdWithText("mysqld-nt.exe", System.getProperty("user.name"));
        }
        for (Integer id : ids)
        {
            System.out.println(id);
            killProcess(id);
        }
        
        
    }
}