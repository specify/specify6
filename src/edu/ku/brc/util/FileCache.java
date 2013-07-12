/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

/**
 * Provides for a local file cache of <code>File</code>s, binary data
 * in the form of <code>byte[]</code>s, and web resources or URLs.
 *
 * @code_status Never Complete
 * @author jstewart
 */
public class FileCache implements DataCacheIFace
{
	private static final Logger log             = Logger.getLogger(FileCache.class);
	private static String mappingFileComment    = "edu.ku.brc.util.FileCache Name Mapping File";
	//private static String accessTimeFileComment = "edu.ku.brc.util.FileCache Access Times File";
	private static String defaultPrefix         = "brc-";
	private static String defaultSuffix         = ".cache";
    private static String defaultPath           = System.getProperty("java.io.tmpdir");
    private static long   ONE_DAY_MILLSEC       = 86400000;
    private static long   ONE_MEG               = 1024 * 1024;

	/** Directory to use for cached files and the mapping files. */
	protected File cacheDir;

	/** Name of the mapping file. */
	protected String mappingFilename;
	
	/** Prefix to be added to all cache filenames. */
	protected String prefix;
	
	/** Suffix to be added to all cache filenames. */
	protected String suffix;
	
	/** whether to use the extension of the file. */
	protected boolean isUsingExtensions = false;
	
	/** Hashtable mapping from a "handle" to the name of the cached file it refers to. */
	protected Properties handleToFilenameHash;
	
	/**
	 * Maximum size of the file cache in kilobytes (using 1 kilobyte = 1024 bytes).
	 * This value is only enforced if enforceMaxSize is set to true.
	 */
	protected int maxCacheMB;
	
	/** A boolean determining whether or not to enforce the cache size limit. */
	protected boolean enforceMaxSize;
    
    /** The current total size of the cache, in bytes. */
    protected long totalCacheSize;
    
    /** How long to keep a file in the cache in days */
    protected long maxRententionDays; 

	/**
	 * Creates a FileCache using the default path and mapping file name.
	 * 
	 * @throws IOException if the default cache directory doesn't exist
	 */
	public FileCache() throws IOException
	{
		this(defaultPath, null);
	}

	/**
	 * Creates a FileCache using the default path and the given
	 * name for the mapping file.
	 * 
	 * @param mappingFilename the name of the mapping file, not including path
	 * @throws IOException if the default cache directory doesn't exist
	 */
	public FileCache(final String mappingFilename) throws IOException
	{
		this(defaultPath, mappingFilename);
	}

	/**
	 * Constructs a FileCache having the given cache directory and mapping
	 * filename.
	 * 
	 * @param dir the directory in which to place the cached files and the mapping files
	 * @param mappingFilename the name of the mapping file, not including path
	 * @throws IOException if the given directory doesn't exist
	 */
	public FileCache(final String dir, final String mappingFilename) throws IOException
	{
		this.mappingFilename = mappingFilename;
		init(dir);
	}

	/**
	 * Does all of the work of constructing a FileCache.
	 * 
	 * @param dir the directory in which to place the cached files and the mapping files
	 * @throws IOException if the given directory doesn't exist
	 */
	protected void init(final String dir) throws IOException
	{
		cacheDir = new File(dir);
		if (!cacheDir.exists())
		{
            FileUtils.forceMkdir(cacheDir);
		}
		//log.debug("Creating FileCache using [" + dir + "] directory");

		handleToFilenameHash = new Properties();
		if (mappingFilename != null)
		{
			loadCacheMappingFile();
            calculateTotalCacheSize();
		}
		prefix = defaultPrefix;
		suffix = defaultSuffix;

		enforceMaxSize    = true;
		maxCacheMB        = 30; // 30MB
		maxRententionDays = 5;  // days
		
		purgeFiles();
	}

    /**
     * Purges all contents of the file cache.
     */
    public void clear()
    {
        handleToFilenameHash.clear();
        File[] files = cacheDir.listFiles();
        for(File file : files)
        {
            file.delete();
        }
    }
    
    /**
     * Removes all the files that are past the maximum retention time.
     */
    protected synchronized void purgeFiles()
    {
        long maxMilliSeconds = ONE_DAY_MILLSEC * maxRententionDays;
        long currMilliSecs   = System.currentTimeMillis();
        while (true)
        {
            Pair<String, Long> p = findOldestKeyLRU();
            //log.debug("Oldest is: "+(p != null ? p.first : "None"));
            
            if (p == null) break;
            
            boolean isOld = currMilliSecs - p.second > maxMilliSeconds;
            //double  days  = (double)(currMilliSecs - p.second) / (double)ONE_DAY_MILLSEC;
            //log.debug(p.first+" - "+p.second+"; "+ (currMilliSecs - p.second) +" > " + maxMilliSeconds + " = "+isOld+"  Days:"+String.format("%8.4f", days));
            if (isOld || (totalCacheSize / ONE_MEG) > maxCacheMB)
            {
                if (!purgeCacheFile(p.first))
                {
                    break;
                }
            } else
            {
                break;
            }
        }
    }
    
    /**
     * @param key
     */
    public synchronized void clearItem(final String key)
    {
        String filename = (String)handleToFilenameHash.get(key);
        removeCacheItem(filename);
    }
    
	/**
	 * Returns the prefix prepended to all cache files.
	 * 
	 * @see #setPrefix(String)
	 * @return the prefix
	 */
	public String getPrefix()
	{
		return prefix;
	}

	/**
	 * Sets the prefix prepended to all cache files.
	 * 
	 * @see #getPrefix()
	 * @param prefix the prefix
	 */
	public void setPrefix(final String prefix)
	{
		this.prefix = prefix;
	}

	/**
     * @param maxRententionDays the maxRententionDays to set
     */
    public void setMaxRententionDays(long maxRententionDays)
    {
        this.maxRententionDays = maxRententionDays;
    }

    /**
	 * Returns the suffix appended to all cache files.
	 * 
	 * @see #setSuffix(String)
	 * @return the suffix
	 */
	public String getSuffix()
	{
		return suffix;
	}

	/**
	 * Sets the suffix appended to all cache files.
	 * 
	 * @see #getSuffix()
	 * @param suffix the suffix
	 */
	public void setSuffix(final String suffix)
	{
		this.suffix = suffix;
	}

	/**
     * @return the isUsingExtensions
     */
    public boolean isUsingExtensions()
    {
        return isUsingExtensions;
    }

    /**
     * @param isUsingExtensions the isUsingExtensions to set
     */
    public void setUsingExtensions(boolean isUsingExtensions)
    {
        this.isUsingExtensions = isUsingExtensions;
    }

    /**
	 * Get the max cache size.  Only enforced if
	 * <code>enforceMaxSize</code> is set to true.
	 * 
	 * @see #setMaxCacheSize(int)
	 * @return the cache size limit, in megabytes
	 */
	public int getMaxCacheSize()
	{
		return maxCacheMB;
	}

	/**
	 * Set the max cache size.  Also turns on enforcement of the
     * cache size limit.
	 * 
	 * @see #getMaxCacheSize()
	 * @param kilobytes the new cache size limit, in megabytes
	 */
	public void setMaxCacheSize(int megaBytes)
	{
	    boolean doPurge = (maxCacheMB < megaBytes) || (totalCacheSize / ONE_MEG) > megaBytes;
		maxCacheMB          = megaBytes;
        this.enforceMaxSize = true;
        if (doPurge) 
        {
            purgeFiles();
        }
	}

	/**
	 * Sets the flag signalling whether or not to enforce the
	 * cache size limit.
	 * 
	 * @see #getEnforceMaxCacheSize()
	 * @param value the flag value
	 */
	public void setEnforceMaxCacheSize( boolean value)
	{
		enforceMaxSize = value;
	}

	/**
	 * Gets the flag signalling whether or not to enforce the
	 * cache size limit.
	 * 
	 * @see #setEnforceMaxCacheSize(boolean)
	 * @return the flag value
	 */
	public boolean getEnforceMaxCacheSize()
	{
		return enforceMaxSize;
	}

	/**
	 * Sets the cache path to be used in case one is not supplied during construction.
	 * 
	 * @param defaultPath the default cache path to use if a path is not supplied to a constructor
	 */
	public static void setDefaultPath(String defaultPath)
    {
        FileCache.defaultPath = defaultPath;
    }

    /**
     * Load handleToFilenameHash from filenames stored in the mapping file.
     */
    protected synchronized void loadCacheMappingFile()
	{
		log.debug("Loading old cache mapping data from " + mappingFilename);
		File mappingFile = new File(cacheDir, mappingFilename);
        if (!cacheDir.exists())
        {
            cacheDir.mkdirs();
        }
        
		if (mappingFile.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(mappingFile);
				handleToFilenameHash.loadFromXML(fis);
				fis.close();
				
				File[] files = cacheDir.listFiles();
				if (handleToFilenameHash.size() < files.length-2)
				{
                    HashMap<String, File> fileNames = new HashMap<String, File>();
    				for (File f : files)
    				{
    				    String nm = f.getName();
    				    if (handleToFilenameHash.get(nm) == null && 
                                !nm.endsWith("-times.xml") && 
                                !nm.toLowerCase().endsWith("cache") && 
    				            !nm.equals(mappingFilename))
    				    {
    				        fileNames.put(f.getName(), f);
    				    }
    				    
    				    for (String key : fileNames.keySet())
    				    {
    				        File file = fileNames.get(key);
    				        handleToFilenameHash.setProperty(key, file.getAbsolutePath());
    				    }
    				}
				}
				
				//showFiles();
			}
			catch( IOException e)
			{
				log.warn("Exception while loading old cache mapping data from disk.  Starting with empty cache.",e);
			}
		}
		else
		{
		    handleToFilenameHash.clear();
		    totalCacheSize = 0L;
		    File[] files = cacheDir.listFiles();
		    for (File file : files)
		    {
		        String nm = file.getName();
		        if (!nm.startsWith(".") && !nm.endsWith("xml") && !nm.endsWith("map") && !nm.endsWith("cache"))
		        {
		            totalCacheSize += file.length();
		            handleToFilenameHash.setProperty(nm, file.getAbsolutePath());
		        }
		    }
		    
			log.warn(String.format("Unable to locate old cache mapping file.  Building cache... %d / %d", totalCacheSize, totalCacheSize / ONE_MEG));
		}
	}

	/**
	 * Returns the filename for the access times file.
	 * 
	 * @return the name of the access times file
	 */
	protected String getAccessTimeFilename()
	{
		String accessTimeFilename = mappingFilename;
		int index = mappingFilename.lastIndexOf(".");
		if (index != -1)
		{
			accessTimeFilename = mappingFilename.substring(0, mappingFilename.lastIndexOf("."));			
		}
		accessTimeFilename = accessTimeFilename + "-times.xml";
		return accessTimeFilename;
	}

	/**
	 * Find the 'oldest' file key in the cache file.
	 *
	 * @return the least recently used key and it last access time
	 */
	protected synchronized Pair<String, Long> findOldestKeyLRU()
	{
		String lruKey        = null;
		long   lruAccessTime = Long.MAX_VALUE;
		for( Object k: handleToFilenameHash.keySet())
		{
			String key = (String)k;
            long   time = getLastAccessTime(key);
			if (time < lruAccessTime)
			{
				lruKey = key;
			}
            lruAccessTime = time;
		}
		return lruKey != null ? new Pair<String, Long>(lruKey, lruAccessTime) : null;
	}

	/**
	 * Save the mapping file to the cache directory.  This is required in order for
	 * a cache to 'survive' application shutdown and restart.
	 * 
	 * @throws IOException an error occurred while writing the handleToFilenameHash
	 * 			contents to the mapping file
	 */
	public synchronized void saveCacheMapping() throws IOException
	{
		if (mappingFilename == null)
		{
			throw new RuntimeException("Cache map filename must be set before calling saveCacheMapping()");
		}

		File mappingFile = new File(cacheDir,mappingFilename);
		try
		{
//		    for (Object k : handleToFilenameHash.keySet())
//		    {
//		        System.err.println("["+k+"]["+handleToFilenameHash.getProperty(k.toString())+"]");
//		    }
			handleToFilenameHash.storeToXML(new FileOutputStream(mappingFile), mappingFileComment);
		}
		catch( IOException e)
		{
			log.warn("Exception while saving cache mapping data to disk.  All cache data will be lost.",e);
			throw e;
		}
	}


	/**
	 * Creates a new <code>File</code> in which cached data can be stored.
	 * 
	 * @return a newly created cache file
	 * @throws IOException an I/O error occurred while creating a new cache file object
	 */
	protected synchronized File createCacheFile(final String extension) throws IOException
	{
		return File.createTempFile(prefix, extension == null ? suffix : extension, cacheDir);
	}

	/**
	 * Updates totalCacheSize to be in sync with actual contents of cache.  This method is only
	 * used after loading a cache mapping file from disk.
	 */
	protected synchronized void calculateTotalCacheSize()
	{
		totalCacheSize = 0L;
		for( Object k: handleToFilenameHash.keySet())
		{
			String key = (String)k;
			String filename = handleToFilenameHash.getProperty(key);
			if (filename != null)
			{
				File f = new File(filename);
				totalCacheSize += f.length();
				//System.out.println(String.format("%s - %d", filename, f.length()));
			}
		}
		log.debug(String.format("Cache Size: %d / %d", totalCacheSize, totalCacheSize / ONE_MEG));
	}

    /**
     * Purges the file having the given handle from the cache.
     * 
     * @param key the cache file handle
     * @return true if a file was purged, false otherwise
     */
    protected synchronized boolean purgeCacheFile(final String key)
    {
        if (key == null)
        {
            return false;
        }
        String filename = handleToFilenameHash.getProperty(key);
        if (filename == null)
        {
            return false;
        }

        //log.debug("Purging " + filename + " from cache");
        File file     = new File(filename);
        long filesize = file.length();
        if (!file.delete())
        {
            log.warn("Failed to delete cache file: "+file.getAbsolutePath());
        }
        handleToFilenameHash.remove(key);
        totalCacheSize -= filesize;
        //log.debug("2 - Reducing cach size: "+totalCacheSize+ " by "+filesize);
        return true;
    }
    
	/**
     * Determine which cache file is the least recently used and delete it.  This method
     * is called when the cache exceeds its maximum size.
     * 
	 * @return true if a file was purged, false otherwise
	 */
//	protected synchronized boolean purgeLruCacheFile()
//	{
//	    Pair<String, Long> p = findOldestKeyLRU();
//	    if (p != null)
//	    {
//	        return purgeCacheFile(p.first);
//	    }
//	    return false;
// 	}

	/**
	 * Cache <code>item</code> using the given key for retrieval.
	 * 
	 * @param key the "handle" used to retrieve this cached data item in the future
	 * @param item the File to be cached
	 */
	protected synchronized void cacheNewItem(final String key, final File item)
	{
        //long currentTime = System.currentTimeMillis();
        //log.debug("Caching " + key + " at " + currentTime);
		Object oldValue = handleToFilenameHash.setProperty(key, item.getAbsolutePath());
		if (oldValue != null)
		{
			removeCacheItem((String)oldValue);
		}

		totalCacheSize += item.length();
		
		//long totSize = totalCacheSize / ONE_MEG;
        //log.debug("Just Added New File totSize: "+totSize +" > maxCacheMB: "+maxCacheMB+"   enforceMaxSize: "+enforceMaxSize+"   Bytes to be added:"+item.length());
        
		purgeFiles();
	}
	
	/**
	 * @param fileName
	 * @return
	 */
	public String createCachFileName(final String fileName)
	{
	    return cacheDir.getAbsolutePath() + File.separator + FilenameUtils.getName(fileName);
	}

	/**
	 * Purge the cached item with the given filename.
	 * 
	 * @param filename the name of the cache file to be deleted
	 */
	protected synchronized void removeCacheItem(final String filename)
	{
		File f = new File(filename);
		long size = f.length();
		if (!f.delete())
		{
			log.warn("Failed to delete old cache file: "+f.getAbsolutePath());
		}
		totalCacheSize -= size;
		//log.debug("1 - Reducing cach size: "+totalCacheSize+ " by "+size);
	}

	/**
	 * Cache the given data bytes.
	 * 
	 * @param data binary data to be stored in a cache file
	 * @return a "handle" used to retrieve the cached data in the future
	 * @throws IOException an error occurred while storing the data to disk
	 */
	public String cacheData(final byte[] data) throws IOException
	{
		String key = UUID.randomUUID().toString();
		cacheData(key, data);
		return key;
	}

	/**
	 * Cache the given data bytes under the given handle name.
	 * 
	 * @param key the retrieval handle to cache under
	 * @param data the data to be cached
	 * @throws IOException an error occurred while storing the data to disk
	 */
	public void cacheData(final String key, final byte[] data) throws IOException
	{
		File f = createCacheFile(null);
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(data);
		fos.flush();
		fos.close();

		cacheNewItem(key,f);
	}

	/**
	 * Cache the given file.
	 * 
	 * @param f the file to cache
	 * @return a handle used to retrieve the cached data in the future
	 * @throws IOException an error occurred while storing the data to disk
	 */
	public File cacheFile(final File f) throws IOException
	{
		File cachedFile = cacheFile(f.getName(), f);
		return cachedFile != null && cachedFile.exists() ? cachedFile : f;
	}
	
	/**
	 * 
	 */
//	protected void showFiles()
//	{
//        //log.debug(" ");
//        //log.debug("Start --------------------------------------");
//	    for (File f : cacheDir.listFiles())
//	    {
//	        //System.out.println(f.getName());
//	        //if (prefix != null && f.getName().startsWith(prefix))
//	        {
//	            //log.debug(f.getName()+" = "+handleToAccessTimeHash.get(f.getName()));
//	        }
//	    }
//        //log.debug("Done ---------------------------------------");
//	}

	/**
	 * Cache the given file using the given handle for retrieval.
	 * 
	 * @param key a handle used to retrieve the cached data in the future
	 * @param f the file to cache
	 * @throws IOException an error occurred while storing the data to disk
	 */
	public File cacheFile(final String key, final File f) throws IOException
	{
	    String extension = isUsingExtensions ? ("." + FilenameUtils.getExtension(f.getName())) : null;
		File  cachedFile = createCacheFile(extension);
		//log.debug(String.format("Caching Key[%s]  file[%s] -> [%s]", key, f.getName(), cachedFile.getName()));
		
		FileUtils.copyFile(f, cachedFile);
		cacheNewItem(key, cachedFile);
		
		//log.debug("Caching["+cachedFile.getAbsolutePath()+"]");
		return cachedFile;
	}

	/**
	 * Retrieve and cache the web resource located at the given URL using the given key as the
	 * retrieval handle.
	 * 
	 * @param key the handle used for retrieval of the cached resource
	 * @param url the URL to the web resource to cache
	 * @throws HttpException a network error occurred while grabbing the web resource
	 * @throws IOException an error occurred while writing the resource to a cache file
	 */
	public String cacheWebResource(final String url) throws HttpException, IOException
	{
        HttpClient httpClient = new HttpClient();
		GetMethod get = new GetMethod(url);
		get.setFollowRedirects(true);
		int result = httpClient.executeMethod(get);
		if (result != 200)
		{
			//log.debug("Retrieving "+url+" resulted in unexpected code: "+result);
			throw new HttpException("Unexpected HTTP code received: " + result);
		}

		byte[] response = get.getResponseBody();

        if (response.length > 0)
        {
            cacheData(url, response);
            return url;
        }
        
        throw new HttpException("Web request returned zero length response");
	}

	/**
	 * Re-retrieve the web resourced cached under the given key.
	 * 
	 * @param key the handle to the cached resource to be refreshed
	 * @throws HttpException a network error occurred while grabbing the web resource
	 * @throws IOException an error occurred while writing the resource to a cache file
	 */
	public void refreshCachedWebResource(final String key) throws HttpException, IOException
	{
		cacheWebResource(key);
	}

	/**
	 * Retrieve the cached file associated with the given handle.
	 * 
	 * @param key the handle to the cached file
	 * @return the cached File, or null if no such file exists
	 */
	public synchronized File getCacheFile(final String key)
	{
	    //log.debug(String.format("Get [%s]", key));

		String filename = handleToFilenameHash.getProperty(key);
		if (filename == null)
		{
			return null;
		}
		
		File f = new File(filename);
		if (f.exists())
		{
			return f;
		}
		
		// the resource was previously cached, but the cache file is missing
		// cleanup the cache mapping
		//log.debug("Previously cached file '"+filename+"' is missing.  Cleaning up cache map data.");
		handleToFilenameHash.remove(key);
		return null;
	}
    
    /**
     * Returns the last access time of the item cached under the given key.  The
     * time returned is in milliseconds since January 1, 1970 UTC.  This uses
     * {@link System#currentTimeMillis()} internally.  If the given key is not
     * found in the cache, {@value Long#MIN_VALUE} is returned.
     * 
     * @param key the key for the cached item
     * @return the last time the item was accessed
     */
    public synchronized long getLastAccessTime(final String key)
    {
        try
        {
            String filename = (String)handleToFilenameHash.get(key);
            File   file     = new File(filename);
            if (file != null)
            {
                Path p = Paths.get(file.getAbsoluteFile().toURI());
                BasicFileAttributes view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
                //log.debug(key+" -> "+view.lastAccessTime()+"  "+view.lastAccessTime().toMillis());
                //System.out.println(view.creationTime()+" is the same as "+view.lastAccessTime()+"  "+view.lastAccessTime().toMillis());

                return view.lastAccessTime().toMillis();
            }
        } catch (IOException ex) {}
        return Calendar.getInstance().getTimeInMillis();
    }

	/**
	 * Returns the current size (in bytes) of the cache.
	 * 
	 * @return the current size
	 */
	public long getCurrentCacheSize()
	{
		return totalCacheSize;
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.util.DataCacheIFace#shutdown()
     */
    public void shutdown() throws Exception
    {
        saveCacheMapping();
    }
    

    /////////////////////////////////
    // Implementation of DataCacheIFace
    /////////////////////////////////
    
    
//    public static void main(String[] args) throws IOException
//    {
//        FileCache fc = new FileCache();
//        fc.setPrefix("XXX");
//        // set max size to 10 KB
//        fc.setMaxCacheSize(2048);
//        fc.setEnforceMaxCacheSize(true);
//        fc.setMaxRententionDays(1);
//        
//        SimpleDateFormat sf = new SimpleDateFormat("hh:mm:ss");
//        
//        Calendar cal = Calendar.getInstance();
//        System.out.println(sf.format(cal.getTime()));
//
//        //log.debug("Current cache size: " + fc.getCurrentCacheSize());
//        
//        String[] files = {"IMG_0123.jpg", "IMG_9781.jpg", "DSCF0029.jpg", "DSCF0023.jpg", "DSCF0024.jpg", };
//        for (String fName : files)
//        {
//            System.out.println("Caching: "+fName);
//            fc.cacheFile(new File("/Users/rods/Desktop/" + fName));
//            try
//            {
//                Thread.currentThread().sleep(1500);
//            } catch (Exception ex){}
//            
//            cal = Calendar.getInstance();
//            System.out.println(sf.format(cal.getTime()));
//            fc.purgeOldFiles();
//            fc.showFiles();
//        }
//        //log.debug("All Done - Current cache size: " + fc.getCurrentCacheSize());
//    }

//	public static void main(String[] args) throws IOException
//	{
//		FileCache fc = new FileCache("AAAATEST-cache-map.xml");
//		fc.setPrefix("AAAATEST");
//		// set max size to 10 KB
//		fc.setMaxCacheSize(1024);
//		fc.setEnforceMaxCacheSize(true);
//
//		//log.debug("Current cache size: " + fc.getCurrentCacheSize());
//
//		// a little File caching test
//        String filename = "/home/jstewart/Desktop/jds.asc";
//		File fileFile = fc.getCacheFile(filename);
//        String fileKey = null;
//		if (fileFile == null)
//		{
//			//log.debug("Cached file not found.");
//			fileKey = fc.cacheFile(new File(filename));
//			//log.debug("Cached " + filename + " under key value " + fileKey);
//		}
//		else
//		{
//			//log.debug("Found cached file under " + fileFile.getAbsolutePath());
//		}
//
//		//log.debug("Current cache size: " + fc.getCurrentCacheSize());
//
//		// a little web resource caching test
//        String httpUrl = "http://www.google.com/";
//		File urlFile = fc.getCacheFile(httpUrl);
//        String urlKey = null;
//		if (urlFile == null)
//		{
//			//log.debug("Cached web resource not found.");
//			urlKey = fc.cacheWebResource("http://www.google.com/");
//			//log.debug("Cached http://www.google.com/ under key value " + urlKey);
//		}
//		else
//		{
//			//log.debug("Found cached web resource under " + urlFile.getAbsolutePath());
//		}
//
//		//log.debug("Current cache size: " + fc.getCurrentCacheSize());
//
//		// a little data caching test
//		File dataFile = fc.getCacheFile("31a55ff8-763b-4ee6-92e8-485c29f8a937");
//        String dataKey = null;
//		if (dataFile == null)
//		{
//			//log.debug("Cached data not found.");
//			Random r = new Random();
//			int count = r.nextInt(100000);
//			StringBuilder sb = new StringBuilder();
//			for( int i = 0; i < count; ++i)
//			{
//				sb.append("X");
//			}
//			dataKey = fc.cacheData(sb.toString().getBytes());
//			//log.debug("Cached data bytes under key value " + dataKey);
//		}
//		else
//		{
//			//log.debug("Found cached data under " + dataFile.getAbsolutePath());
//		}
//
//		//log.debug("Current cache size: " + fc.getCurrentCacheSize());
//        
//        long fileTime = fc.getLastAccessTime(fileKey);
//        long urlTime  = fc.getLastAccessTime(urlKey);
//        long dataTime = fc.getLastAccessTime(dataKey);
//        
//        //log.debug("File was last accessed at " + fileTime);
//        //log.debug("URL was last accessed at " + urlTime);
//        //log.debug("Data was last accessed at " + dataTime);
//
//        //log.debug("Requesting cached web resource: " + urlKey);
//        
//        fc.getCacheFile(urlKey);
//
//        fileTime = fc.getLastAccessTime(fileKey);
//        urlTime  = fc.getLastAccessTime(urlKey);
//        dataTime = fc.getLastAccessTime(dataKey);
//        
//        //log.debug("File was last accessed at " + fileTime);
//        //log.debug("URL was last accessed at " + urlTime);
//        //log.debug("Data was last accessed at " + dataTime);
//
//        fc.clear();
//        
//        //log.debug("Current cache size: " + fc.getCurrentCacheSize());
//
//		fc.saveCacheMapping();
//	}
}
