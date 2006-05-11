package edu.ku.brc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;
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
	private static String defaultPrefix = "sp6-";
	private static String defaultSuffix = ".cache";
	
	protected HttpClient httpClient;
	protected File cacheDir;
	protected String mappingFilename;
	protected String prefix;
	protected String suffix;
	protected Properties handleToFilenameHash;
	
	public FileCache() throws IOException
	{
		mappingFilename = null;
		init(System.getProperty("java.io.tmpdir"));
	}
	
	public FileCache(String mappingFilename) throws IOException
	{
		this(System.getProperty("java.io.tmpdir"),mappingFilename);
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
		if( mappingFilename != null )
		{
			loadCacheMappingFile();
		}
		httpClient = new HttpClient();
		prefix = defaultPrefix;
		suffix = defaultSuffix;
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

	protected void loadCacheMappingFile() throws IOException
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
				throw e;
			}
		}
	}

	public void saveCacheMapping() throws IOException
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
	}

	synchronized protected File createCacheFile() throws IOException
	{
		return File.createTempFile(prefix, suffix, cacheDir);
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
		handleToFilenameHash.setProperty(key, f.getAbsolutePath());
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
		handleToFilenameHash.setProperty(key, cachedFile.getAbsolutePath());
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
		
		File f = createCacheFile();
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(response);
		fos.flush();
		fos.close();

		handleToFilenameHash.setProperty(key, f.getAbsolutePath());
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

	public static void main(String[] args) throws IOException
	{
		FileCache fc = new FileCache("sp6-cache-map.xml");

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

		// a little data caching test
		File dataFile = fc.getCacheFile("31a55ff8-763b-4ee6-92e8-485c29f8a937");
		if( dataFile == null )
		{
			log.info("Cached data not found.");
			String testData = "This data was generated for testing purposes only.  Feel free to delete at any time.";
			String dataKey = fc.cacheData(testData.getBytes());
			log.info("Cached data bytes under key value " + dataKey);
		}
		else
		{
			log.info("Found cached data under " + dataFile.getAbsolutePath());
		}
		
		fc.saveCacheMapping();
	}
}
