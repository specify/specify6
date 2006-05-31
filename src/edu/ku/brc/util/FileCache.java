package edu.ku.brc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileCache
{
	private static Log log = LogFactory.getLog(FileCache.class);
	private static String mappingFileComment = "edu.ku.brc.util.FileCache Name Mapping File";
	private static String accessTimeFileComment = "edu.ku.brc.util.FileCache Access Times File";
	private static String defaultPrefix = "sp6-";
	private static String defaultSuffix = ".cache";
    private static String defaultPath = System.getProperty("java.io.tmpdir");

	protected HttpClient httpClient;
	protected File cacheDir;
	protected String mappingFilename;
	protected String prefix;
	protected String suffix;
	protected Properties handleToFilenameHash;
	protected Properties handleToAccessTimeHash;
	protected int maxCacheKb;
	protected boolean enforceMaxSize;
	protected long totalCacheSize;

	public FileCache() throws IOException
	{
		this(defaultPath, null);
	}

	public FileCache(String mappingFilename) throws IOException
	{
		this(defaultPath, mappingFilename);
	}

	public FileCache(String dir, String mappingFilename) throws IOException
	{
		this.mappingFilename = mappingFilename;
		init(dir);
	}

	protected void init( String dir ) throws IOException
	{
		cacheDir = new File(dir);
		if( !cacheDir.exists() )
		{
			throw new IOException("Requested cache directory must already exist");
		}
		log.info("Creating FileCache using " + dir + " directory");

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
	 * @return Returns the prefix.
	 */
	public String getPrefix()
	{
		return prefix;
	}

	/**
	 * @param prefix The prefix to set.
	 */
	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	/**
	 * @return Returns the suffix.
	 */
	public String getSuffix()
	{
		return suffix;
	}

	/**
	 * @param suffix The suffix to set.
	 */
	public void setSuffix(String suffix)
	{
		this.suffix = suffix;
	}

	public int getMaxCacheSize()
	{
		return maxCacheKb;
	}

	public void setMaxCacheSize(int kilobytes)
	{
		maxCacheKb = kilobytes;
	}

	public void setEnforceMaxCacheSize( boolean value )
	{
		enforceMaxSize = value;
	}

	public boolean getEnforceMaxCacheSize()
	{
		return enforceMaxSize;
	}

	public static void setDefaultPath(String defaultPath)
    {
        FileCache.defaultPath = defaultPath;
    }

    protected synchronized void loadCacheMappingFile()
	{
		log.info("Loading old cache mapping data from " + mappingFilename);
		File mappingFile = new File(cacheDir,mappingFilename);
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

	synchronized protected File createCacheFile() throws IOException
	{
		return File.createTempFile(prefix, suffix, cacheDir);
	}

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

	protected synchronized void purgeLruCacheFile()
	{
		String oldKey = findKeyLRU();
		String filename = handleToFilenameHash.getProperty(oldKey);
		if( filename == null )
		{
			return;
		}

		log.info("Purging " + filename + " from cache");
		File f = new File(filename);
		long filesize = f.length();
		if( !f.delete() )
		{
			log.warn("Failed to delete cache file: "+f.getAbsolutePath());
		}
		handleToFilenameHash.remove(oldKey);
		handleToAccessTimeHash.remove(oldKey);
		totalCacheSize -= filesize;
	}

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

	public String cacheData( byte[] data ) throws IOException
	{
		String key = UUID.randomUUID().toString();
		cacheData(key, data);
		return key;
	}

	public void cacheData( String key, byte[] data ) throws IOException
	{
		File f = createCacheFile();
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(data);
		fos.flush();
		fos.close();

		cacheNewItem(key,f);
	}

	public String cacheFile( File f ) throws IOException
	{
		cacheFile(f.getName(),f);
		return f.getName();
	}

	public void cacheFile( String key, File f ) throws IOException
	{
		File cachedFile = createCacheFile();
		copyFile(f,cachedFile);
		cacheNewItem(key,cachedFile);
	}

	protected void copyFile( File src, File dest ) throws IOException
	{
		FileChannel sourceChannel = new FileInputStream(src).getChannel();
		FileChannel destinationChannel = new FileOutputStream(dest).getChannel();
		sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);

		// or
		//  destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
		sourceChannel.close();
		destinationChannel.close();
	}

	public String cacheWebResource( String url ) throws HttpException, IOException
	{
		cacheWebResource(url, url);
		return url;
	}

	public void cacheWebResource( String key, String url ) throws HttpException, IOException
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
	}

	public void refreshCachedWebResource( String key ) throws HttpException, IOException
	{
		cacheWebResource(key);
	}

	public File getCacheFile( String key )
	{
		String filename = handleToFilenameHash.getProperty(key);
		if( filename == null )
		{
			return null;
		}

		else
		{
			File f = new File(filename);
			if( f.exists() )
			{
				handleToAccessTimeHash.setProperty(key, Long.toString(System.currentTimeMillis()));
				return f;
			}
			else
			{
				// the resource was previously cached, but the cache file is missing
				// cleanup the cache mapping
				log.info("Previously cached file '"+filename+"' is missing.  Cleaning up cache map data.");
				handleToFilenameHash.remove(key);
				return null;
			}
		}
	}

	public long getCurrentCacheSize()
	{
		return totalCacheSize;
	}

	public static void main(String[] args) throws IOException
	{
		FileCache fc = new FileCache("AAAATEST-cache-map.xml");
		fc.setPrefix("AAAATEST");
		// set max size to 10 KB
		fc.setMaxCacheSize(1000);
		fc.setEnforceMaxCacheSize(true);

		log.info("Current cache size: " + fc.getCurrentCacheSize());

		// a little File caching test
		File fileFile = fc.getCacheFile("kmloutput.kml");
		if( fileFile == null )
		{
			log.info("Cached file not found.");
			String fileKey = fc.cacheFile(new File("C:\\Documents and Settings\\jstewart\\Desktop\\kmloutput.kml"));
			log.info("Cached kmloutput.kml under key value " + fileKey);
		}
		else
		{
			log.info("Found cached file under " + fileFile.getAbsolutePath());
		}

		log.info("Current cache size: " + fc.getCurrentCacheSize());

		// a little web resource caching test
		File urlFile = fc.getCacheFile("http://www.google.com/");
		if( urlFile == null )
		{
			log.info("Cached web resource not found.");
			String urlKey = fc.cacheWebResource("http://www.google.com/");
			log.info("Cached http://www.google.com/ under key value " + urlKey);
		}
		else
		{
			log.info("Found cached web resource under " + urlFile.getAbsolutePath());
		}

		log.info("Current cache size: " + fc.getCurrentCacheSize());

		// a little data caching test
		File dataFile = fc.getCacheFile("31a55ff8-763b-4ee6-92e8-485c29f8a937");
		if( dataFile == null )
		{
			log.info("Cached data not found.");
			Random r = new Random();
			int count = r.nextInt(100000);
			StringBuilder sb = new StringBuilder();
			for( int i = 0; i < count; ++i )
			{
				sb.append("X");
			}
			String dataKey = fc.cacheData(sb.toString().getBytes());
			log.info("Cached data bytes under key value " + dataKey);
		}
		else
		{
			log.info("Found cached data under " + dataFile.getAbsolutePath());
		}

		log.info("Current cache size: " + fc.getCurrentCacheSize());

		fc.saveCacheMapping();
	}
}
