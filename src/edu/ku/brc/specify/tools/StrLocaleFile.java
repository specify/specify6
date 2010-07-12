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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
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
            
    protected String                            dstPath;
    protected String                            srcPath;
    protected Vector<StrLocaleEntry>            items      = new Vector<StrLocaleEntry>();
    protected Hashtable<String, StrLocaleEntry> itemHash   = new Hashtable<String, StrLocaleEntry>();
    protected Hashtable<String, String>         chkHash    = new Hashtable<String, String>();
    protected boolean                           isDestination;
    protected Hashtable<String, Integer>        keyToInxMap = new Hashtable<String, Integer>();
    protected Vector<StrLocaleEntry>			keys = new Vector<StrLocaleEntry>();
    
    /**
     * @param dstPath
     */
    public StrLocaleFile(final String  dstPath, 
                         final String  srcPath,
                         final boolean isDestination)
    {
        super();
        this.dstPath       = dstPath;
        this.srcPath       = srcPath;
        this.isDestination = isDestination;
        
        load();
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
     * 
     */
    @SuppressWarnings("unchecked")
    private void load()
    {
        char[] dstBytes = new char[2048];
        try
        {
            itemHash.clear();
            
            File file = new File(dstPath);
            if (file.exists())
            {
                int duplicateCnt = 0;
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
                        
                        boolean debug = false;//key.equals("TOP5") && StringUtils.contains(dstPath, "_pt");
                        
                        if (debug)
                        {
                            //System.out.println((byte)value.charAt(12)+"="+value.charAt(12));
                            char spec = 'Ã';
                            int fInx = value.indexOf(spec);
                            if (fInx > -1)
                            {
                                int jj = 0;
                                byte[] bytes = value.getBytes();
                                for (int ii=0;ii<bytes.length;ii++)
                                {
                                    byte b1 = bytes[ii];
                                    short s1 = (short)(b1 < 0 ? (256 + b1) : b1);
                                    if (s1 > 127 && s1 != 195) s1 += 64;
                                    
                                    //System.out.println(ii+"  "+bytes[ii]+" ");
                                    if (s1 == 195)
                                    {
                                        ii++;
                                        b1 = bytes[ii];
                                        s1 = (short)(b1 < 0 ? (256 + b1) : b1);
                                        if (s1 > 127 && s1 != 195) s1 += 64;
                                        
                                        dstBytes[jj++] = (char)s1;
                                    } else
                                    {
                                        dstBytes[jj++] = (char)bytes[ii];
                                    }
                                }
                                System.out.print(value+'=');
                                value = new String(dstBytes, 0, jj);
                                System.out.println(value);
                            }
                            /*for (String k : l10nMappingHash.keySet())
                            {
                                String before = value;
                                int    sInx   = value.indexOf(k);
                                if (sInx > -1)
                                {
                                    for (int i=0;i<value.length();i++)
                                    {
                                        byte b = (byte)value.charAt(i);
                                        System.out.print(b);
                                        if (b < 0) b = (byte)(256 + (int)b);
                                        System.out.println(" ["+value.charAt(i)+"]["+b+"]");
                                    }
                                    //int eInx = sInx + 2;
                                    //value = value.substring(0, sInx-1) + l10nMappingHash.get(k) + (eInx < value.length() ? value.substring(eInx) : "");
                                    //value = StringUtils.replace(value, k, l10nMappingHash.get(k));
                                    //System.out.println("Contains["+k+"] b4["+before+"]["+value+"]["+k+"]["+l10nMappingHash.get(k)+"]");
                                } else
                                {
                                    System.out.println("NOT Contains["+k+"]");
                                }
                            }*/
                        }
                                                
                        if (itemHash.get(key) != null)
                        {
                            log.error("Key '"+key+"' on Line "+count+" is a duplicate.");
                            duplicateCnt++;
                            
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
                log.error(duplicateCnt+" duplicate keys: "+dstPath);
            }
            
            if (isDestination)
            {
                loadCheckFile();
            }
            
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
    private void loadCheckFile()
    {
        try
        {
            chkHash.clear();
            
            File file = new File(dstPath+".orig");
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
    public boolean isSrcSameAsDest(final String key, final String srcValue)
    {
        String origValue = chkHash.get(key);
        if (origValue != null)
        {
            return origValue.equals(srcValue);
        }
        return false;
    }
    
    /**
     * Save as Ascii.
     */
    public boolean save()
    {
        FileOutputStream fos = null;
        DataOutputStream dos = null;

        try
        {
            fos = new FileOutputStream(dstPath);
            dos = new DataOutputStream(fos);//, "UTF-8");
            
            for (StrLocaleEntry entry : items)
            {
                String str = "";
                if (entry.getKey() == null)
                {
                    
                } else if (entry.getKey().equals("#"))
                {
                    str = entry.getSrcStr();
                } else
                {
                    str = entry.getKey()+ "="+ (entry.getDstStr() == null ? "" : entry.getDstStr());
                }
                
                str += '\n';
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
		return dstPath;
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
