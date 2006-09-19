/**
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
 *
 * @code_status Alpha
 * @author jstewart
 */
public class Thumbnailer
{
	protected Hashtable<String, ThumbnailGenerator> mimeTypeToGeneratorMap;
	protected MimetypesFileTypeMap mimeMap;
	protected ThumbnailGenerator defaultThumbnailer;
	protected float quality;
	protected int maxWidth;
	protected int maxHeight;
	
	public Thumbnailer()
	{
		mimeTypeToGeneratorMap = new Hashtable<String, ThumbnailGenerator>();
		mimeMap = new MimetypesFileTypeMap();
		
		quality = 1f;
		maxWidth = 64;
		maxHeight = 64;
	}
	
	public void registerThumbnailers(String generatorRegistryFile) throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		Document registry = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(generatorRegistryFile));
		NodeList generatorNodes = registry.getElementsByTagName("generator");
		for(int i = 0; i < generatorNodes.getLength(); ++i )
		{
			Node generatorNode = generatorNodes.item(i);
			Node classNameNode = generatorNode.getAttributes().getNamedItem("class");
			String generatorClassName = classNameNode.getTextContent();
			ThumbnailGenerator gen = Class.forName(generatorClassName).asSubclass(ThumbnailGenerator.class).newInstance();
			for(String supportedMimeType: gen.getSupportedMimeTypes())
			{
				registerThumbnailGenerator(supportedMimeType,gen);
			}
		}
	}
	
	public ThumbnailGenerator registerThumbnailGenerator(String mimeType, ThumbnailGenerator generator)
	{
		generator.setQuality(quality);
		generator.setMaxHeight(maxHeight);
		generator.setMaxWidth(maxWidth);
		return mimeTypeToGeneratorMap.put(mimeType,generator);
	}
	
	public ThumbnailGenerator removeGenerator(String mimeType)
	{
		return mimeTypeToGeneratorMap.remove(mimeType);
	}
	
	public void generateThumbnail(String originalFile, String outputFile) throws IOException
	{
		String mimeType = mimeMap.getContentType(originalFile);
		ThumbnailGenerator generator = mimeTypeToGeneratorMap.get(mimeType);
		if(generator==null)
		{
			generator = defaultThumbnailer;
		}
		
		generator.generateThumbnail(originalFile, outputFile);
	}
	
	public void setMaxWidth(int maxWidth)
	{
		Enumeration<ThumbnailGenerator> tgs = mimeTypeToGeneratorMap.elements();
		while(tgs.hasMoreElements())
		{
			tgs.nextElement().setMaxWidth(maxWidth);
		}
		
		this.maxWidth = maxWidth;
	}

	public void setMaxHeight(int maxHeight)
	{
		Enumeration<ThumbnailGenerator> tgs = mimeTypeToGeneratorMap.elements();
		while(tgs.hasMoreElements())
		{
			tgs.nextElement().setMaxHeight(maxHeight);
		}
		
		this.maxHeight = maxHeight;
	}

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
	 *
	 *
	 * @param args
	 * @throws Exception 
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
