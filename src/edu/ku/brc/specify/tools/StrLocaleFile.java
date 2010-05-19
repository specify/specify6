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
package edu.ku.brc.specify.tools;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.specify.tools.StrLocaleEntry.STATUS;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 15, 2009
 *
 */
public class StrLocaleFile
{
    private static final Logger  log                = Logger.getLogger(StrLocaleFile.class);
            
    protected String                            path;
    protected String                            srcPath;
    protected Vector<StrLocaleEntry>            items      = new Vector<StrLocaleEntry>();
    protected Hashtable<String, StrLocaleEntry> itemHash   = new Hashtable<String, StrLocaleEntry>();
    protected Hashtable<String, String>         chkHash    = new Hashtable<String, String>();
    protected boolean                           isDestination;
    protected Hashtable<String, Integer>        keyToInxMap = new Hashtable<String, Integer>();
    protected Vector<StrLocaleEntry>			keys = new Vector<StrLocaleEntry>();
    
    /**
     * @param path
     */
    public StrLocaleFile(final String  path, 
                         final String  srcPath,
                         final boolean isDestination)
    {
        super();
        this.path          = path;
        this.srcPath       = srcPath;
        this.isDestination = isDestination;
        
        load(path);
    }
    
    /**
     * @return number of actual resources with keys in the file
     */
    public int getNumberOfKeys()
    {
    	return keys.size();
    }
    
    /**
     * @param key
     * @return
     */
    public Integer getInxForKey(final String key)
    {
        return keyToInxMap.get(key);
    }
    
    /**
     * @param path
     */
    @SuppressWarnings("unchecked")
    private void load(final String path)
    {
        try
        {
            File file = new File(path);
            if (file.exists())
            {
                int index = 0;
                int count = 0;
                List<String> lines = (List<String>)FileUtils.readLines(file);
                for (String line : lines)
                {
                    if (line.trim().startsWith("#"))
                    {
                        items.add(new StrLocaleEntry("#", line, null, STATUS.IsComment));
                        
                    } else if (line.indexOf('=') > -1)
                    {
                        int inx = line.indexOf('=');
                        
                        String key   = line.substring(0, inx);
                        String value = line.substring(inx+1, line.length());
                                                
                        if (itemHash.get(key) != null)
                        {
                            log.error("Key '"+key+"' on Line "+count+" is a duplicate.");
                            
                        } else
                        {
                            StrLocaleEntry entry = new StrLocaleEntry(key, value, null, StringUtils.isEmpty(value) ? STATUS.IsNew : STATUS.IsOK);
                            items.add(entry);
                            itemHash.put(key, entry);
                            keyToInxMap.put(key, keys.size());
                            keys.add(entry);
                        }
                        
                        index++;
                        
                    } else
                    {
                        items.add(new StrLocaleEntry(null, null, null, STATUS.IsBlank));
                    }
                    count++;
                }
            }
            
            loadCheckFile(path);
            
            clearEditFlags();
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param path
     */
    @SuppressWarnings("unchecked")
    private void loadCheckFile(final String path)
    {
        try
        {
            chkHash.clear();
            
            File file = new File(path+".orig");
            if (file.exists())
            {
                List<String> lines = (List<String>)FileUtils.readLines(file);
                for (String line : lines)
                {
                    if (!line.trim().startsWith("#") && line.indexOf('=') > -1)
                    {
                        int inx = line.indexOf('=');
                        String key   = line.substring(0, inx);
                        String value = line.substring(inx+1, line.length());
                        chkHash.put(key, value);
                    }
                }
            }
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param key
     * @param value
     * @return
     */
    public boolean isSrcSameAsDest(final String key, final String value)
    {
        String oldValue = chkHash.get(key);
        if (oldValue != null)
        {
            return oldValue.equals(value);
        }
        return false;
    }
    

    /**
     * 
     */
    public void save()
    {
        Vector<String> lines = new Vector<String>();
        for (StrLocaleEntry entry : items)
        {
            if (entry.getKey() == null)
            {
                lines.add("");
            } else if (entry.getKey().equals("#"))
            {
                lines.add(entry.getSrcStr());
            } else
            {
                lines.add(entry.getKey()+ "="+ (entry.getDstStr() == null ? "" : entry.getDstStr()));
            }
        }
        
        try
        {
            FileUtils.writeLines(new File(path), lines);
            
            if (srcPath != null)
            {
                File origFile = new File(srcPath);
                File outFile  = new File(path+".orig");
                FileUtils.copyFile(origFile, outFile);
            }
            
            //clear edited flag for all items
            clearEditFlags();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * clear edited flag for all items
     */
    public void clearEditFlags()
    {
        for (StrLocaleEntry entry : items) 
        {
        	entry.setEdited(false);
        }
    }
    /**
     * @return true if any items have been edited since last save
     */
    public boolean isEdited()
    {
        for (StrLocaleEntry entry : items) 
        {
        	if (entry.isEdited())
        	{
        		return true;
        	}
        }
        return false;
    }
    /**
     * 
     */
    /*private void saveCheckFile()
    {
        Vector<String> lines = new Vector<String>();
        for (StrLocaleEntry entry : items)
        {
            if (entry.isValue())
            {
                lines.add(entry.getKey()+ "="+ (entry.getDstStr() == null ? "" : entry.getDstStr()));
            }
        }
        
        try
        {
            FileUtils.writeLines(new File(path+".chg"), lines);
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }*/
    
    public StrLocaleEntry getKey(final int index)
    {
    	return keys.get(index);
    }
        
    /**
     * @return
     */
    public Hashtable<String, StrLocaleEntry> getItemHash()
    {
        return itemHash;
    }

    /**
     * @return the items
     */
    public Vector<StrLocaleEntry> getItems()
    {
        return items;
    }

    /**
     * @return the chkHash
     */
    public Hashtable<String, String> getChkHash()
    {
        return chkHash;
    }

	/**
	 * @return the path
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * @return the srcPath
	 */
	public String getSrcPath()
	{
		return srcPath;
	}
    
	
    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return FilenameUtils.getName(getPath());
	}

//	public static void main(String[] args)
//    {
//    	try
//    	{
//    		List<String> text = FileUtils.readLines(new File("/home/timo/LanguageCodesLarge.txt"));
//    		List<String> newText = new Vector<String>();
//    		newText.add("<languagecodes>");
//    		Iterator<String> lines = text.iterator();
//    		while (lines.hasNext())
//    		{
//    			String[] codes = lines.next().split("\t");
//    			if (codes.length > 4)
//    			{
//    				String newLine = "<languagecode englishname=\"" + codes[4].trim() + "\" code=\"" + codes[0].trim() + "\"/>";
//    				newText.add(newLine);
//    			}
//    		}
//    		newText.add("</languagecodes>");
//    		FileUtils.writeLines(new File("/home/timo/LanguageCodesLittle.txt"), newText);
//    	} catch (Exception e)
//    	{
//    		e.printStackTrace();
//    		System.exit(1);
//    	}
//    	
//    }
}
