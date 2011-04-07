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
package edu.ku.brc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * Provides for a local file cache of <code>File</code>s, binary data
 * in the form of <code>byte[]</code>s, and web resources or URLs.
 *
 * @code_status Complete
 * @author jstewart
 */
public class FileCache implements DataCacheIFace
{
	private static final Logger log = Logger.getLogger(FileCache.class);
	private static String mappingFileComment = "edu.ku.brc.util.FileCache Name Mapping File";
	private static String accessTimeFileComment = "edu.ku.brc.util.FileCache Access Times File";
	private static String defaultPrefix = "brc-";
	private static String defaultSuffix = ".cache";
    private static String defaultPath   = System.getProperty("java.io.tmpdir");

	/** Directory to use for cached files and the mapping files. */
	protected File cacheDir;

	/** Name of the mapping file. */
	protected String mappingFilename;
	
	/** Prefix to be added to all cache filenames. */
	protected String prefix;
	
	/** Suffix to be added to all cache filenames. */
	protected String suffix;
	
	/** Hashtable mapping from a "handle" to the name of the cached file it refers to. */
	protected Properties handleToFilenameHash;
	
	/** Hashtable mapping from a "handle" to the last access time of the cached file it refers to. */
	protected Properties handleToAccessTimeHash;
	
	/**
	 * Maximum size of the file cache in kilobytes (using 1 kilobyte = 1000 bytes).
	 * This value is only enforced if enforceMaxSize is set to true.
	 */
	protected int maxCacheKb;
	
	/** A boolean determining whether or not to enforce the cache size limit. */
	protected boolean enforceMaxSize;
	
	/** The current total size of the cache, in bytes. */
	protected long totalCacheSize;

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
		if( !cacheDir.exists() )
		{
            FileUtils.forceMkdir(cacheDir);
		}
		//log.debug("Creating FileCache using " + dir + " directory");

		handleToFilenameHash = new Properties();
		handleToAccessTimeHash = new Properties();
		if( mappingFilename != null )
		{
			loadCacheMappingFile();
			loadCacheAccessTimesFile();
			calculateTotalCacheSize();
		}
		prefix = defaultPrefix;
		suffix = defaultSuffix;

		enforceMaxSize = false;
		maxCacheKb = Integer.MAX_VALUE;
	}

    /**
     * Purges all contents of the file cache.
     */
    public void clear()
    {
        // purge all of the files
        while (purgeLruCacheFile())
        {
            // do nothing
        }
    }
    
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
	 * Get the max cache size.  Only enforced if
	 * <code>enforceMaxSize</code> is set to true.
	 * 
	 * @see #setMaxCacheSize(int)
	 * @return the cache size limit, in kilobytes (using 1 kilobyte = 1000 bytes)
	 */
	public int getMaxCacheSize()
	{
		return maxCacheKb;
	}

	/**
	 * Set the max cache size.  Also turns on enforcement of the
     * cache size limit.
	 * 
	 * @see #getMaxCacheSize()
	 * @param kilobytes the new cache size limit, in kilobytes (using 1 kilobyte = 1000 bytes)
	 */
	public void setMaxCacheSize(int kilobytes)
	{
		maxCacheKb = kilobytes;
        this.enforceMaxSize = true;
	}

	/**
	 * Sets the flag signalling whether or not to enforce the
	 * cache size limit.
	 * 
	 * @see #getEnforceMaxCacheSize()
	 * @param value the flag value
	 */
	public void setEnforceMaxCacheSize( boolean value )
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
		//log.debug("Loading old cache mapping data from " + mappingFilename);
		File mappingFile = new File(cacheDir, mappingFilename);
        if (!cacheDir.exists())
        {
            cacheDir.mkdirs();
        }
        
		if( mappingFile.exists() )
		{
			try
			{
				FileInputStream fis = new FileInputStream(mappingFile);
				handleToFilenameHash.loadFromXML(fis);
				fis.close();
			}
			catch( IOException e )
			{
				log.warn("Exception while loading old cache mapping data from disk.  Starting with empty cache.",e);
			}
		}
		else
		{
			log.warn("Unable to locate old cache mapping file.  Cache will start out empty.");
		}
	}

	/**
	 * Load handleToAccessTimeHash from times stored in the access time file.
	 */
	protected synchronized void loadCacheAccessTimesFile()
	{
		String accessTimeFilename = getAccessTimeFilename();
		//log.debug("Loading old cache access times from " + accessTimeFilename);
		File accessTimesFile = new File(cacheDir,accessTimeFilename);
		if( accessTimesFile.exists() )
		{
			try
			{
				FileInputStream fis = new FileInputStream(accessTimesFile);
				handleToAccessTimeHash.loadFromXML(fis);
				fis.close();
			}
			catch( IOException e )
			{
				log.warn("Exception while loading old cache access times from disk.",e);
			}
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
		if( index != -1 )
		{
			accessTimeFilename = mappingFilename.substring(0, mappingFilename.lastIndexOf("."));			
		}
		accessTimeFilename = accessTimeFilename + "-times.xml";
		return accessTimeFilename;
	}

	/**
	 * Find the key in the cache that corresponds to the least recently used
	 * cache file.
	 *
	 * @return the least recently used key
	 */
	protected String findKeyLRU()
	{
		String lruKey = null;
		long lruAccessTime = Long.MAX_VALUE;
		for( Object k: handleToFilenameHash.keySet() )
		{
			String key = (String)k;
            long time = getLastAccessTime(key);
			if( time < lruAccessTime )
			{
				lruKey = key;
				lruAccessTime = time;
			}
		}

		return lruKey;
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
		if( mappingFilename == null )
		{
			throw new RuntimeException("Cache map filename must be set before calling saveCacheMapping()");
		}

		File mappingFile = new File(cacheDir,mappingFilename);
		try
		{
			handleToFilenameHash.storeToXML(new FileOutputStream(mappingFile), mappingFileComment);
		}
		catch( IOException e )
		{
			log.warn("Exception while saving cache mapping data to disk.  All cache data will be lost.",e);
			throw e;
		}

		saveCacheAccessTimes();
	}

	/**
	 * Save the access times file to the cache directory.  This is not required
	 * in order for a cache to 'survive' application shutdown and restart.  However,
	 * for the cache to correctly delete the least recently used item when the cache
	 * exceeds its maximum size (if set and enforced), the access times file must
	 * also 'survive' application shutdown and restart.
	 * 
	 * @throws IOException an error occurred while writing the handleToAccessTimeHash
	 * 			contents to the mapping file
	 */
	protected synchronized void saveCacheAccessTimes() throws IOException
	{
		String accessTimeFilename = getAccessTimeFilename();
		File accessTimesFile = new File(cacheDir,accessTimeFilename);
		try
		{
			handleToAccessTimeHash.storeToXML(new FileOutputStream(accessTimesFile), accessTimeFileComment);
		}
		catch( IOException e )
		{
			log.warn("Exception while saving cache access times to disk.",e);
			throw e;
		}
	}

	/**
	 * Creates a new <code>File</code> in which cached data can be stored.
	 * 
	 * @return a newly created cache file
	 * @throws IOException an I/O error occurred while creating a new cache file object
	 */
	protected synchronized File createCacheFile() throws IOException
	{
		return File.createTempFile(prefix, suffix, cacheDir);
	}

	/**
	 * Updates totalCacheSize to be in sync with actual contents of cache.  This method is only
	 * used after loading a cache mapping file from disk.
	 */
	protected synchronized void calculateTotalCacheSize()
	{
		totalCacheSize = 0L;
		for( Object k: handleToFilenameHash.keySet() )
		{
			String key = (String)k;
			String filename = handleToFilenameHash.getProperty(key);
			if( filename != null )
			{
				File f = new File(filename);
				totalCacheSize += f.length();
			}
		}
	}

    /**
     * Purges the file having the given handle from the cache.
     * 
     * @param key the cache file handle
     * @return true if a file was purged, false otherwise
     */
    protected synchronized boolean purgeCacheFile(final String key)
    {
        if (key==null)
        {
            return false;
        }
        String filename = handleToFilenameHash.getProperty(key);
        if( filename == null )
        {
            return false;
        }

        //log.debug("Purging " + filename + " from cache");
        File f = new File(filename);
        long filesize = f.length();
        if( !f.delete() )
        {
            log.warn("Failed to delete cache file: "+f.getAbsolutePath());
        }
        handleToFilenameHash.remove(key);
        handleToAccessTimeHash.remove(key);
        totalCacheSize -= filesize;
        return true;
    }
    
	/**
     * Determine which cache file is the least recently used and delete it.  This method
     * is called when the cache exceeds its maximum size.
     * 
	 * @return true if a file was purged, false otherwise
	 */
	protected synchronized boolean purgeLruCacheFile()
	{
		String oldKey = findKeyLRU();
        return purgeCacheFile(oldKey);
 	}

	/**
	 * Cache <code>item</code> using the given key for retrieval.
	 * 
	 * @param key the "handle" used to retrieve this cached data item in the future
	 * @param item the File to be cached
	 */
	protected synchronized void cacheNewItem(final String key, final File item )
	{
        long currentTime = System.currentTimeMillis();
		handleToAccessTimeHash.setProperty(key, Long.toString(currentTime));
        //log.debug("Caching " + key + " at " + currentTime);
		Object oldValue = handleToFilenameHash.setProperty(key, item.getAbsolutePath());
		if( oldValue != null )
		{
			removeCacheItem((String)oldValue);
		}

		totalCacheSize += item.length();

		while( enforceMaxSize && (totalCacheSize > maxCacheKb*1000) )
		{
			purgeLruCacheFile();
		}
	}

	/**
	 * Purge the cached item with the given filename.
	 * 
	 * @param filename the name of the cache file to be deleted
	 */
	protected synchronized void removeCacheItem(final String filename )
	{
		File f = new File(filename);
		long size = f.length();
		if( !f.delete() )
		{
			log.warn("Failed to delete old cache file: "+f.getAbsolutePath());
		}
		totalCacheSize -= size;
	}

	/**
	 * Cache the given data bytes.
	 * 
	 * @param data binary data to be stored in a cache file
	 * @return a "handle" used to retrieve the cached data in the future
	 * @throws IOException an error occurred while storing the data to disk
	 */
	public String cacheData(final byte[] data ) throws IOException
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
	public void cacheData(final String key, final byte[] data ) throws IOException
	{
		File f = createCacheFile();
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
	public String cacheFile(final File f ) throws IOException
	{
		cacheFile(f.getName(),f);
		return f.getName();
	}

	/**
	 * Cache the given file using the given handle for retrieval.
	 * 
	 * @param key a handle used to retrieve the cached data in the future
	 * @param f the file to cache
	 * @throws IOException an error occurred while storing the data to disk
	 */
	public void cacheFile(final String key, final File f ) throws IOException
	{
		File cachedFile = createCacheFile();
		FileUtils.copyFile(f,cachedFile);
		cacheNewItem(key,cachedFile);
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
	public String cacheWebResource(final String url ) throws HttpException, IOException
	{
        HttpClient httpClient = new HttpClient();
		GetMethod get = new GetMethod(url);
		get.setFollowRedirects(true);
		int result = httpClient.executeMethod(get);
		if( result != 200 )
		{
			log.debug("Retrieving "+url+" resulted in unexpected code: "+result);
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
	public void refreshCachedWebResource(final String key ) throws HttpException, IOException
	{
		cacheWebResource(key);
	}

	/**
	 * Retrieve the cached file associated with the given handle.
	 * 
	 * @param key the handle to the cached file
	 * @return the cached File, or null if no such file exists
	 */
	public File getCacheFile(final String key )
	{
		String filename = handleToFilenameHash.getProperty(key);
		if( filename == null )
		{
			return null;
		}
		
		File f = new File(filename);
		if( f.exists() )
		{
			handleToAccessTimeHash.setProperty(key, Long.toString(System.currentTimeMillis()));
			return f;
		}
		
		// the resource was previously cached, but the cache file is missing
		// cleanup the cache mapping
		log.debug("Previously cached file '"+filename+"' is missing.  Cleaning up cache map data.");
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
    public long getLastAccessTime(final String key )
    {
        String accessTimeString = handleToAccessTimeHash.getProperty(key);
        if (accessTimeString == null)
        {
            return Long.MIN_VALUE;
        }
        
        long accessTimeMillis;
        try
        {
            accessTimeMillis = Long.parseLong(accessTimeString);
        }
        catch (NumberFormatException nfe)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FileCache.class, nfe);
            log.error("Unable to parse access time for cache item: " + key, nfe);
            accessTimeMillis = Long.MIN_VALUE;
        }
        return accessTimeMillis;
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
    

//	public static void main(String[] args) throws IOException
//	{
//		FileCache fc = new FileCache("AAAATEST-cache-map.xml");
//		fc.setPrefix("AAAATEST");
//		// set max size to 10 KB
//		fc.setMaxCacheSize(1000);
//		fc.setEnforceMaxCacheSize(true);
//
//		log.debug("Current cache size: " + fc.getCurrentCacheSize());
//
//		// a little File caching test
//        String filename = "/home/jstewart/Desktop/jds.asc";
//		File fileFile = fc.getCacheFile(filename);
//        String fileKey = null;
//		if( fileFile == null )
//		{
//			log.debug("Cached file not found.");
//			fileKey = fc.cacheFile(new File(filename));
//			log.debug("Cached " + filename + " under key value " + fileKey);
//		}
//		else
//		{
//			log.debug("Found cached file under " + fileFile.getAbsolutePath());
//		}
//
//		log.debug("Current cache size: " + fc.getCurrentCacheSize());
//
//		// a little web resource caching test
//        String httpUrl = "http://www.google.com/";
//		File urlFile = fc.getCacheFile(httpUrl);
//        String urlKey = null;
//		if( urlFile == null )
//		{
//			log.debug("Cached web resource not found.");
//			urlKey = fc.cacheWebResource("http://www.google.com/");
//			log.debug("Cached http://www.google.com/ under key value " + urlKey);
//		}
//		else
//		{
//			log.debug("Found cached web resource under " + urlFile.getAbsolutePath());
//		}
//
//		log.debug("Current cache size: " + fc.getCurrentCacheSize());
//
//		// a little data caching test
//		File dataFile = fc.getCacheFile("31a55ff8-763b-4ee6-92e8-485c29f8a937");
//        String dataKey = null;
//		if( dataFile == null )
//		{
//			log.debug("Cached data not found.");
//			Random r = new Random();
//			int count = r.nextInt(100000);
//			StringBuilder sb = new StringBuilder();
//			for( int i = 0; i < count; ++i )
//			{
//				sb.append("X");
//			}
//			dataKey = fc.cacheData(sb.toString().getBytes());
//			log.debug("Cached data bytes under key value " + dataKey);
//		}
//		else
//		{
//			log.debug("Found cached data under " + dataFile.getAbsolutePath());
//		}
//
//		log.debug("Current cache size: " + fc.getCurrentCacheSize());
//        
//        long fileTime = fc.getLastAccessTime(fileKey);
//        long urlTime  = fc.getLastAccessTime(urlKey);
//        long dataTime = fc.getLastAccessTime(dataKey);
//        
//        log.debug("File was last accessed at " + fileTime);
//        log.debug("URL was last accessed at " + urlTime);
//        log.debug("Data was last accessed at " + dataTime);
//
//        log.debug("Requesting cached web resource: " + urlKey);
//        
//        fc.getCacheFile(urlKey);
//
//        fileTime = fc.getLastAccessTime(fileKey);
//        urlTime  = fc.getLastAccessTime(urlKey);
//        dataTime = fc.getLastAccessTime(dataKey);
//        
//        log.debug("File was last accessed at " + fileTime);
//        log.debug("URL was last accessed at " + urlTime);
//        log.debug("Data was last accessed at " + dataTime);
//
//        fc.clear();
//        
//        log.debug("Current cache size: " + fc.getCurrentCacheSize());
//
//		fc.saveCacheMapping();
//	}
}
