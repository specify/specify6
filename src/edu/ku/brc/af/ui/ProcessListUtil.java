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
            Process        process = Runtime.getRuntime().exec("tasklist.exe /fo csv /nh");
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) 
            {
                if (!line.trim().equals("")) 
                {
                    // keep only the process name
                    line = line.substring(1);
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
    public static List<Integer> getProcessIdWithText(final String text)
    {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        
        List<String> processList = ProcessListUtil.getRunningProcesses();
        for (String line : processList)
        {
            if (StringUtils.contains(line, text))
            {
                String[] toks = StringUtils.split(line, ' ');
                
                if (UIHelper.isWindows())
                {
                    
                } else
                {
                    ids.add(Integer.parseInt(toks[1]));
                }
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
        if (UIHelper.isWindows())
        {
            return false;
        }
        
        // Unix
        try 
        {
            Process process = Runtime.getRuntime().exec("kill " + processId);
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
        List<String> processList = getRunningProcesses();

        StringBuilder sb = new StringBuilder();
        for (String line : processList)
        {
            if (StringUtils.contains(line.toLowerCase(), "mysqld") && StringUtils.contains(line.toLowerCase(), "3337"))
            {
                if (sb.length() > 0) sb.append("\n");
                sb.append(line);
            }
        }
        System.out.println(sb.length() == 0 ? "None" : sb.toString());
    }
}