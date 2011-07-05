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
import java.io.UnsupportedEncodingException;
import java.util.Collections;
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
    protected Vector<TableWriter>            printWritersList      = new Vector<TableWriter>();
    
    protected String indexTitle = "Index";
    
    protected File dir;
    
    /**
     * 
     */
    public ConversionLogger()
    {
    }
    
    /**
     * @param baseDirName
     * @param dirName
     * @return
     */
    public boolean initialize(final String baseDirName, final String dirName)
    {
        dir = new File(baseDirName + File.separator + dirName);
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
        return getWriter(fileName, title, false);
    }

    /**
     * @param tableName
     * @return
     * @throws IOException
     */
    public TableWriter getWriter(final String fileName, final String title, final String extraStyle)
    {
        return getWriter(fileName, title, extraStyle, false);
    }

    /**
     * @param tableName
     * @return
     * @throws IOException
     */
    public TableWriter getWriter(final String fileName, final String title, final boolean doCenterTitle)
    {
        return getWriter(fileName, title, null, doCenterTitle);
    }

    /**
     * @param tableName
     * @return
     * @throws IOException
     */
    public TableWriter getWriter(final String fileName, final String title, final String extraStyle, final boolean doCenterTitle)
    {
        TableWriter tblWriter = null;
        try
        {
            if (printWritersNameHash.get(fileName) == null)
            {
                String path = dir.getAbsolutePath() + File.separator + StringUtils.replace(fileName, " ", "_");
                tblWriter = new TableWriter(path, title, extraStyle, doCenterTitle);
                printWritersNameHash.put(fileName, path);
                printWritersHash.put(fileName, tblWriter);
                printWritersList.add(tblWriter);
                
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
        return closeAll(false);
    }
    
    /**
     * @param indexWriter
     * @param orderList
     */
    protected void writeIndex(final TableWriter indexWriter, final Vector<TableWriter> orderList)
    {
        indexWriter.startTable();

        for (TableWriter tblWriter : orderList)
        {
            System.out.println(tblWriter.getTitle());
            
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
    }
    
    /**
     * 
     */
    public File closeAll(final boolean byOrderAdded)
    {
        try 
        {
            if (dir == null)
            {
                dir = new File(".");
            }
            String path = dir.getAbsolutePath() + File.separator + "index.html";
            TableWriter indexWriter = new TableWriter(path, indexTitle);
            
            Vector<TableWriter> orderList;
            if (byOrderAdded)
            {
                orderList = printWritersList;
            } else
            {
                orderList = new Vector<TableWriter>();
                Vector<String> names = new Vector<String>(printWritersHash.keySet());
                Collections.sort(names); 
                
                for (String nm : names)
                {
                    orderList.add(printWritersHash.get(nm));
                }
            }
            
            writeIndex(indexWriter, orderList);
            
            indexWriter.close();
            
            return new File(path);
            
        } catch (FileNotFoundException e) 
        {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
}
