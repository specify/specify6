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
public class FileCache
{
	private static final Logger log = Logger.getLogger(FileCache.class);
	private static String mappingFileComment = "edu.ku.brc.util.FileCache Name Mapping File";
	private static String accessTimeFileComment = "edu.ku.brc.util.FileCache Access Times File";
	private static String defaultPrefix = "brc-";
	private static String defaultSuffix = ".cache";
    private static String defaultPath   = System.getProperty("java.io.tmpdir");

	/** HttpClient used for grabbing web resources. */
	protected HttpClient httpClient;
	
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
	public FileCache(String mappingFilename) throws IOException
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
	public FileCache(String dir, String mappingFilename) throws IOException
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
	protected void init( String dir ) throws IOException
	{
		cacheDir = new File(dir);
		if( !cacheDir.exists() )
		{
            FileUtils.forceMkdir(cacheDir);
		}
		log.debug("Creating FileCache using " + dir + " directory");

		handleToFilenameHash = new Properties();
		handleToAccessTimeHash = new Properties();
		if( mappingFilename != null )
		{
			loadCacheMappingFile();
			loadCacheAccessTimesFile();
			calculateTotalCacheSize();
		}
		httpClient = new HttpClient();
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
	public void setPrefix(String prefix)
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
	public void setSuffix(String suffix)
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
	 * Set the max cache size.  Only enforced if
	 * <code>enforceMaxSize</code> is set to true.
	 * 
	 * @see #getMaxCacheSize()
	 * @param kilobytes the new cache size limit, in kilobytes (using 1 kilobyte = 1000 bytes)
	 */
	public void setMaxCacheSize(int kilobytes)
	{
		maxCacheKb = kilobytes;
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
		log.info("Loading old cache mapping data from " + mappingFilename);
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
		log.info("Loading old cache access times from " + accessTimeFilename);
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
			String value = handleToAccessTimeHash.getProperty(key);
			if( value == null )
			{
				// if we can't find an access time for this key, consider it the oldest
				return key;
			}
			long time = Long.parseLong(value);
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
	synchronized protected File createCacheFile() throws IOException
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
    protected synchronized boolean purgeCacheFile(String key)
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

        log.info("Purging " + filename + " from cache");
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
	protected void cacheNewItem( String key, File item )
	{
		handleToAccessTimeHash.setProperty(key, Long.toString(System.currentTimeMillis()));
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
	protected void removeCacheItem( String filename )
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
	public String cacheData( byte[] data ) throws IOException
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
	public void cacheData( String key, byte[] data ) throws IOException
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
	public String cacheFile( File f ) throws IOException
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
	public void cacheFile( String key, File f ) throws IOException
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
	public String cacheWebResource( String url ) throws HttpException, IOException
	{
		GetMethod get = new GetMethod(url);
		get.setFollowRedirects(true);
		int result = httpClient.executeMethod(get);
		if( result != 200 )
		{
			log.info("Retrieving "+url+" resulted in unexpected code: "+result);
			throw new HttpException("Unexpected HTTP code received: " + result);
		}

		byte[] response = get.getResponseBody();

		cacheData(url, response);
		return url;
	}

	/**
	 * Re-retrieve the web resourced cached under the given key.
	 * 
	 * @param key the handle to the cached resource to be refreshed
	 * @throws HttpException a network error occurred while grabbing the web resource
	 * @throws IOException an error occurred while writing the resource to a cache file
	 */
	public void refreshCachedWebResource( String key ) throws HttpException, IOException
	{
		cacheWebResource(key);
	}

	/**
	 * Retrieve the cached file associated with the given handle.
	 * 
	 * @param key the handle to the cached file
	 * @return the cached File, or null if no such file exists
	 */
	public File getCacheFile( String key )
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
		log.info("Previously cached file '"+filename+"' is missing.  Cleaning up cache map data.");
		handleToFilenameHash.remove(key);
		return null;
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

//	public static void main(String[] args) throws IOException
//	{
//		FileCache fc = new FileCache("AAAATEST-cache-map.xml");
//		fc.setPrefix("AAAATEST");
//		// set max size to 10 KB
//		fc.setMaxCacheSize(1000);
//		fc.setEnforceMaxCacheSize(true);
//
//		log.info("Current cache size: " + fc.getCurrentCacheSize());
//
//		// a little File caching test
//		File fileFile = fc.getCacheFile("kmloutput.kml");
//		if( fileFile == null )
//		{
//			log.info("Cached file not found.");
//			String fileKey = fc.cacheFile(new File("/home/jstewart/Desktop/KML_Samples.kml"));
//			log.info("Cached kmloutput.kml under key value " + fileKey);
//		}
//		else
//		{
//			log.info("Found cached file under " + fileFile.getAbsolutePath());
//		}
//
//		log.info("Current cache size: " + fc.getCurrentCacheSize());
//
//		// a little web resource caching test
//		File urlFile = fc.getCacheFile("http://www.google.com/");
//		if( urlFile == null )
//		{
//			log.info("Cached web resource not found.");
//			String urlKey = fc.cacheWebResource("http://www.google.com/");
//			log.info("Cached http://www.google.com/ under key value " + urlKey);
//		}
//		else
//		{
//			log.info("Found cached web resource under " + urlFile.getAbsolutePath());
//		}
//
//		log.info("Current cache size: " + fc.getCurrentCacheSize());
//
//		// a little data caching test
//		File dataFile = fc.getCacheFile("31a55ff8-763b-4ee6-92e8-485c29f8a937");
//		if( dataFile == null )
//		{
//			log.info("Cached data not found.");
//			Random r = new Random();
//			int count = r.nextInt(100000);
//			StringBuilder sb = new StringBuilder();
//			for( int i = 0; i < count; ++i )
//			{
//				sb.append("X");
//			}
//			String dataKey = fc.cacheData(sb.toString().getBytes());
//			log.info("Cached data bytes under key value " + dataKey);
//		}
//		else
//		{
//			log.info("Found cached data under " + dataFile.getAbsolutePath());
//		}
//
//		log.info("Current cache size: " + fc.getCurrentCacheSize());
//        
//        fc.clear();
//        
//        log.info("Current cache size: " + fc.getCurrentCacheSize());
//
//		fc.saveCacheMapping();
//	}
}
