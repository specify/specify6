/**
 * Copyright (C) ${year}  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.util.thumbnails;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The main class used for generating 'thumbnails' of various types of files.
 * Thumbnailer provides a mechanism by which {@link ThumbnailGenerator}s can
 * be plugged in to provide thumbnailing capabilities for other types of files.
 *
 * @code_status Alpha
 * @author jstewart
 */
public class Thumbnailer
{
	/** A map of registered ThumbnailGenerators. */
	protected Hashtable<String, ThumbnailGenerator> mimeTypeToGeneratorMap;
	
    /** The quality factor of the thumbnail output. */
	protected float quality;
	
    /** The max width of the thumbnail output. */
	protected int maxWidth;
	
    /** The max height of the thumbnail output. */
	protected int maxHeight;
	
	public Thumbnailer()
	{
		mimeTypeToGeneratorMap = new Hashtable<String, ThumbnailGenerator>();
		
		
		quality = 1f;
		maxWidth = 32;
		maxHeight = 32;
	}
	
	/**
     * Registers all of the {@link ThumbnailGenerator}s described in the registry file.
     * 
	 * @param generatorRegistryFile the XML file containing the thumbnail generator class names
	 * @throws SAXException the file was not capable of being parsed
	 * @throws IOException the file couldn't be opened
	 * @throws ParserConfigurationException a DOM parser couldn't be created
	 * @throws ClassNotFoundException a class listed in the registry couldn't be located
	 * @throws InstantiationException a class listed in the registry couldn't be instantiated
	 * @throws IllegalAccessException a security violation occured while instantiating a class
	 */
	public void registerThumbnailers(String generatorRegistryFile) throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		Document registry = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(generatorRegistryFile));
		NodeList generatorNodes = registry.getElementsByTagName("generator");
		for(int i = 0; i < generatorNodes.getLength(); ++i )
		{
			Node generatorNode = generatorNodes.item(i);
			Node classNameNode = generatorNode.getAttributes().getNamedItem("class");
			String generatorClassName = classNameNode.getNodeValue();
			ThumbnailGenerator gen = Class.forName(generatorClassName).asSubclass(ThumbnailGenerator.class).newInstance();
			for(String supportedMimeType: gen.getSupportedMimeTypes())
			{
				registerThumbnailGenerator(supportedMimeType,gen);
			}
		}
	}
	
	/**
     * Registers the given {@link ThumbnailGenerator} with the system.
     * 
	 * @param mimeType the MIME type handled by the generator
	 * @param generator the generator
	 * @return the registered generator
	 */
	public ThumbnailGenerator registerThumbnailGenerator(String mimeType, ThumbnailGenerator generator)
	{
		generator.setQuality(quality);
		generator.setMaxHeight(maxHeight);
		generator.setMaxWidth(maxWidth);
		return mimeTypeToGeneratorMap.put(mimeType,generator);
	}
	
	/**
     * Unregisters the {@link ThumbnailGenerator} that handles the given MIME type.
     * 
	 * @param mimeType the MIME type to unregister
	 * @return the unregistered generator, null if one was not registered
	 */
	public ThumbnailGenerator removeGenerator(String mimeType)
	{
		return mimeTypeToGeneratorMap.remove(mimeType);
	}
	
	/**
     * Generates a thumbnail for the given original.
     * 
	 * @param originalFile the original
	 * @param outputFile the output file for the generated thumbnail
	 * @throws IOException an exception occured while generating the thumbnail or no generator was registered for the given MIME type
	 */
	public void generateThumbnail(String originalFile, String outputFile) throws IOException
	{
        // get the system MIME type mapper
        MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();
        
        // get the MIME type of the given original file
		String mimeType = mimeMap.getContentType(originalFile);
        
        // find the appropriate thumbnail generator, if any
		ThumbnailGenerator generator = mimeTypeToGeneratorMap.get(mimeType);
		if(generator!=null)
		{
            generator.generateThumbnail(originalFile, outputFile);
            return;
		}
        
        // if no generator was found, throw an exception
        throw new IOException("No ThumbnailGenerator registered for this MIME type");
	}
	
	/**
     * Sets the maximum width of any visual thumbnails generated.
     * {@link ThumbnailGenerator#setMaxWidth(int)} is called on all registered
     * generators.
     * 
	 * @param maxWidth the maximum width
	 */
	public void setMaxWidth(int maxWidth)
	{
		Enumeration<ThumbnailGenerator> tgs = mimeTypeToGeneratorMap.elements();
		while(tgs.hasMoreElements())
		{
			tgs.nextElement().setMaxWidth(maxWidth);
		}
		
		this.maxWidth = maxWidth;
	}

	/**
     * Sets the maximum height of any visual thumbnails generated.
     * {@link ThumbnailGenerator#setMaxHeight(int)} is called on all registered
     * generators.
     * 
     * @param maxHeight the maximum width
	 */
	public void setMaxHeight(int maxHeight)
	{
		Enumeration<ThumbnailGenerator> tgs = mimeTypeToGeneratorMap.elements();
		while(tgs.hasMoreElements())
		{
			tgs.nextElement().setMaxHeight(maxHeight);
		}
		
		this.maxHeight = maxHeight;
	}

	/**
     * Sets the quality factor of any thumbnails generated.  Note that not
     * all {@link ThumbnailGenerator}s make use of a quality factor.
     * {@link ThumbnailGenerator#setQuality(float)} is called on all registered
     * generators.
     * 
	 * @param percent the quality factor
	 */
	public void setQuality(float percent)
	{
		Enumeration<ThumbnailGenerator> tgs = mimeTypeToGeneratorMap.elements();
		while(tgs.hasMoreElements())
		{
			tgs.nextElement().setQuality(percent);
		}
		
		this.quality = percent;
	}
	
	/**
	 * A simple demonstration of this class, using an image thumbnail generator.
     * Note that this demo only works provided the hard coded file names are valid.
	 *
	 * @param args ignored
	 * @throws Exception all sorts of reasons
	 */
	public static void main(String[] args) throws Exception
	{
		Thumbnailer thumb = new Thumbnailer();
		thumb.registerThumbnailers("config\\thumbnail_generators.xml");
		thumb.setQuality(.5f);
		thumb.setMaxHeight(128);
		thumb.setMaxWidth(128);
		
		String orig      = "C:\\Documents and Settings\\jstewart\\Desktop\\orig.png";
		String thumbnail = "C:\\Documents and Settings\\jstewart\\Desktop\\thumbnail.jpg";
		thumb.generateThumbnail(orig,thumbnail);
		
		String origTxt = "C:\\Documents and Settings\\jstewart\\Desktop\\test.txt";
		thumb.generateThumbnail(origTxt,thumbnail);
	}
}
