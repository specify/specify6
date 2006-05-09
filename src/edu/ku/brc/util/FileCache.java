package edu.ku.brc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileCache
{
	private static Log log = LogFactory.getLog(FileCache.class);
	
	private static String cacheMappingFilename = "sp6-cache-mapping.xml";
	private static String mappingFileComment = "edu.ku.brc.util.FileCache Name Mapping File";
	
	private static String defaultPrefix = "sp6-";
	private static String defaultSuffix = ".cache";
	
	protected HttpClient httpClient;
	protected File cacheDir;
	protected String prefix;
	protected String suffix;
	
	protected Properties handleToFilenameHash;
	
	public FileCache() throws IOException
	{
		this(System.getProperty("java.io.tmpdir"));
	}
	
	public FileCache( String dir ) throws IOException
	{
		cacheDir = new File(dir);
		if( !cacheDir.exists() )
		{
			throw new IOException("Requested cache directory must already exist");
		}
		
		log.info("Creating FileCache using " + dir + " directory");
		
		init();
	}
	
	protected void init() throws IOException
	{
		handleToFilenameHash = new Properties();
		File mappingFile = new File(cacheDir,cacheMappingFilename);
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
		httpClient = new HttpClient();
		prefix = defaultPrefix;
		suffix = defaultSuffix;
	}

	public void saveCacheMapping() throws IOException
	{
		File mappingFile = new File(cacheDir,cacheMappingFilename);
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

	synchronized protected File getCacheFile() throws IOException
	{
		return File.createTempFile(prefix, suffix, cacheDir);
	}
	
	public String cacheData( byte[] data ) throws IOException
	{
		File f = getCacheFile();
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(data);
		fos.flush();
		fos.close();
		handleToFilenameHash.setProperty(f.getName(), f.getAbsolutePath());
		return f.getName();
	}
	
	public File cacheFile( File f ) throws IOException
	{
		File cachedFile = getCacheFile();
		copyFile(f,cachedFile);
		return f;
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
		GetMethod get = new GetMethod(url);
		get.setFollowRedirects(true);
		int result = httpClient.executeMethod(get);
		if( result != 200 )
		{
			log.info("Retrieving "+url+" resulted in unexpected code: "+result);
			throw new HttpException("Unexpected HTTP code received: " + result);
		}
		
		byte[] response = get.getResponseBody();
		
		File f = getCacheFile();
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(response);
		fos.flush();
		fos.close();

		handleToFilenameHash.setProperty(url, f.getAbsolutePath());
		
		return url;
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
			return new File(filename);
		}
	}
}
