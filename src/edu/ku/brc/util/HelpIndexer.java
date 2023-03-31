/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;


/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * the indexIt method will create javahelp index entries for all html files contained in
 * topDir (and topDir's subdirectories). jhm is a JavaHelp mapping file that is used to determine 
 * the targets for the index entries. The entries are written to a file named outFileName.
 * 
 * Assumptions:
 * 
 * One target in map file for each html file
 * 
 * map file entries are one liners: <mapID target="Login" url="SpecifyHelp/login.html"/>
 * 
 * html file names are unique within topDir (and all it's sub directories). 
 *
 * span tags are in (exactly) this format: <span class="index">index entry namee</span>
 *  on a single line.
 *  
 *  
 */
public class HelpIndexer
{
    File jhm;
    File topDir;
    String outFileName;
    List<String> mapLines;
    
    public HelpIndexer(final String jhmFile, final String dir, final String outFileName)
    {
        jhm = new File(jhmFile);
        topDir = new File(dir);
        this.outFileName = outFileName;
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public void indexIt()
    {
        if (!jhm.exists())
        {
            System.out.println("jhm file not found.");
            return;
        }
        if (!topDir.isDirectory())
        {
            System.out.println("directory does not exist.");
            return;
        }
        try
        {
          mapLines = FileUtils.readLines(jhm);
        }
        catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(HelpIndexer.class, ex);
            System.out.println("Error reading map file: " + ex.getMessage());
            return;
        }
        
        Vector<String> lines = new Vector<String>();
        String[] ext = {"html"};
        Iterator<File> helpFiles = FileUtils.iterateFiles(topDir, ext, true);
        while (helpFiles.hasNext())
        {
            File file = helpFiles.next();
            System.out.println("Processing "+file.getName());
            processFile(file, lines);
        }
        
        System.out.println();
        System.out.println("all done.");
        
        File outFile = new File(outFileName); 
        try
        {

            //add lines to top of file
            lines.insertElementAt("<?xml version='1.0' encoding='ISO-8859-1'  ?>", 0);
            lines.insertElementAt("<!DOCTYPE index", 1);
            lines.insertElementAt("  PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp Index Version 1.0//EN\"", 2);
            lines.insertElementAt("         \"http://java.sun.com/products/javahelp/index_1_0.dtd\">", 3);
            lines.insertElementAt("<index version=\"1.0\">", 4);
            lines.insertElementAt(" ", 5);
            
            //and bottom...
            lines.add(" ");
            lines.add("</index>");
            
            FileUtils.writeLines(outFile, lines);
        }
        catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(HelpIndexer.class, ex);
            System.out.println("error writing output file: " + ex.getMessage());
        }
    }
    
    protected String getFileTitle(final File file)
    {
        String text;
        try
        {
            text = FileUtils.readFileToString(file);
        }
        catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(HelpIndexer.class, ex);
            System.out.println("error processing file: " + file.getName());
            return null;
        }
        int titlePos = text.toLowerCase().indexOf("<title>", 0);
        if (titlePos < 0)
        {
            return null;
        }
        int endTitlePos = text.toLowerCase().indexOf("</title>", titlePos);
        if (endTitlePos < 0)
        {
            return null;
        }
        return text.substring(titlePos + 7, endTitlePos).trim();
    }
    
    protected String processIndexLine(final String line, final String target)
    {
        String workLine = line.trim();
        int sinx          = workLine.indexOf("<span");
        int startIdxTitle = workLine.indexOf(">", sinx)+1;
        int endIdxTitle = workLine.indexOf("</span>", sinx);
        String idxTitle = workLine.substring(startIdxTitle, endIdxTitle);
        return "<indexitem text=\"" + idxTitle + "\"   target=\"" + target + "\"/>";
    }
    
    protected String getTarget(final File file)
    {
        // this assumes that file names are unique across directories
        // also assumes that map entries are one liners
        int lineNo = 0;
        while (lineNo < mapLines.size() && mapLines.get(lineNo).indexOf(file.getName()) < 0)
        {
            lineNo++;
        }
        
        if (lineNo == mapLines.size())
        {
            System.out.println("target for " + file.getName() + " could not be determined.");
            return null;
        }
        String mapLine = mapLines.get(lineNo);
        
        int targetIdx = mapLine.toLowerCase().indexOf("target=");
        if (targetIdx < 0)
        {
            System.out.println("target for " + file.getName() + " could not be determined.");
            return null;
        }
        int endTargetIdx = mapLine.indexOf("\"", targetIdx + 9);
        if (endTargetIdx < 0)
        {
            System.out.println("target for " + file.getName() + " could not be determined.");
            return null;
        }
        String result = mapLine.substring(targetIdx + 7, endTargetIdx+1).trim();
        //remove enclosing "
        return result.substring(1, result.length()-1);
        
    }
    protected void processFile(final File file, final Vector<String> lines)
    {
        // System.out.println("processing file: " + file.getName());

        LineIterator it;
        try
        {
            it = FileUtils.lineIterator(file, "UTF-8");
        }
        catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(HelpIndexer.class, ex);
            System.out.println("error processing file: " + file.getName());
            return;
        }
        String target = getTarget(file);
        String title = getFileTitle(file);
        boolean removeTitleEntry = false;
        if (title != null)
        {
            String tline = "<indexitem text=\"" + title;
            if (target != null)
            {
            	tline += "\"  target=\"" + target;
            }
            tline +=  "\">";
        	lines.add(tline);
            removeTitleEntry = true;
        }
        if (target != null)
        {
            try
            {
                while (it.hasNext())
                {
                    String line = it.nextLine();
                    //System.out.println(line);
                    if (isIndexLine(line))
                    {
                        System.out.println("indexing " + file.getName() + ": " + line);
                        String indexEntry = processIndexLine(line, target);
                        if (indexEntry != null)
                        {
                            lines.add("     " + indexEntry);
                            removeTitleEntry = false;
                        }
                    }
                }
            }
            finally
            {
                LineIterator.closeQuietly(it);
            }
        }
        if (title != null && !removeTitleEntry)
        {
            lines.add("</indexitem>");
        }
        if (removeTitleEntry)
        {
            lines.remove(lines.size() - 1);
        }
    }
    
    protected boolean isIndexLine(final String line)
    {
        final String token = "<span class=\"index\">";
        String stripped = line.trim();
        if (stripped.length() < token.length())
        {
            return false;
        }
        return stripped.indexOf(token) > -1;
    }
    
    /**
     * @param args
     * args[0] - map file. eg. "/home/timbo/workspace/Specify 6/help/...
     * args[1] - directory containing help files. (it will recurse into sub-directories)
     * args[2] - name of the output file. (already existing files will be overwritten!!) 
     */
    public static void main(String[] args)
    {
        String mapFile;
        String helpFile;
        String output;
        if (args.length == 0)
        {
            mapFile = "help/SpecifyHelp.jhm";
            helpFile = "help/SpecifyHelp/Workbench";
            output = "SpecifyHelpIndex.xml";
        }
        else
        {
            mapFile = args[0];
            helpFile = args[1];
            output = args[2];
        }
        
        HelpIndexer hi = new HelpIndexer(mapFile, helpFile, output);
        hi.indexIt();
    }

}
