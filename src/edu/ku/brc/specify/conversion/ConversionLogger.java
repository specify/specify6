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
package edu.ku.brc.specify.conversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 29, 2009
 *
 */
public class ConversionLogger
{

    
    protected Hashtable<String, String>      printWritersNameHash  = new Hashtable<String, String>();
    protected Hashtable<String, TableWriter> printWritersHash      = new Hashtable<String, TableWriter>();
    
    protected String indexTitle = "Index";
    
    protected File dir;
    
    /**
     * 
     */
    public ConversionLogger()
    {
    }
    
    /**
     * @param name
     * @return
     */
    public boolean initialize(final String name)
    {
        return initialize("conversions", name);
    }
    
    /**
     * @param name
     * @return
     */
    public boolean initialize(final String baseDirName, final String name)
    {
        dir = new File(baseDirName + File.separator + name);
        if (!dir.exists())
        {
            return dir.mkdirs();
        }
        return false;
    }
    
    /**
     * @param indexTitle the indexTitle to set
     */
    public void setIndexTitle(String indexTitle)
    {
        this.indexTitle = indexTitle;
    }

    /**
     * @param tableName
     * @return
     * @throws IOException
     */
    public TableWriter getWriter(final String fileName, final String title)
    {
        
        TableWriter tblWriter = null;
        try
        {
            if (printWritersNameHash.get(fileName) == null)
            {
                String path = dir.getAbsolutePath() + File.separator + StringUtils.replace(fileName, " ", "_");
                tblWriter = new TableWriter(path, title);
                printWritersNameHash.put(fileName, path);
                printWritersHash.put(fileName, tblWriter);
            } else
            {
                System.err.println("Duplicate file name["+fileName+"]");
            }
        } catch (IOException ex) { ex.printStackTrace(); }

        return tblWriter;
    }
    
    /**
     * 
     */
    public File closeAll()
    {
        try 
        {
            String path = dir.getAbsolutePath() + File.separator + "index.html";
            TableWriter indexWriter = new TableWriter(path, indexTitle);
            indexWriter.startTable();
            
            Vector<String> names = new Vector<String>(printWritersHash.keySet());
            
            for (String nm : names)
            {
                System.out.println(nm);
                
                TableWriter tblWriter = printWritersHash.get(nm);
                
                try
                {
                    if (tblWriter.hasLines())
                    {
                        indexWriter.log("<A href=\""+ FilenameUtils.getName(tblWriter.getFileName())+"\">"+tblWriter.getTitle()+"</A>");
                        tblWriter.close();
                        
                    } else
                    {
                        tblWriter.flush();
                        tblWriter.close();
                        
                        File f = new File(tblWriter.getFileName());
                        if (f.exists())
                        {
                            f.delete();
                        }
                    }
                   
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            indexWriter.endTable();
            indexWriter.close();
            
            return new File(path);
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
}
