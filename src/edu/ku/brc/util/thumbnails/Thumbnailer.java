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
package edu.ku.brc.util.thumbnails;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.IconEntry;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;

/**
 * The main class used for generating 'thumbnails' of various types of files.
 * Thumbnailer provides a mechanism by which {@link ThumbnailGeneratorIFace}s can
 * be plugged in to provide thumbnailing capabilities for other types of files.
 *
 * @code_status Alpha
 * @author jstewart
 */
public class Thumbnailer
{
    private static HashMap<String, String> availableIcons;

	/** A map of registered ThumbnailGenerators. */
	protected Hashtable<String, ThumbnailGeneratorIFace> mimeTypeToGeneratorMap;
	
    /** The quality factor of the thumbnail output. */
	protected float quality;
	
    /** The max width of the thumbnail output. */
	protected int maxWidth;
	
    /** The max height of the thumbnail output. */
	protected int maxHeight;
	
	static
	{
	    readIconMap();
	}
	
	/**
	 * 
	 */
	public Thumbnailer()
	{
		mimeTypeToGeneratorMap = new Hashtable<String, ThumbnailGeneratorIFace>();
		
		
		quality   = 1f;
		maxWidth  = 100;
		maxHeight = 100;
	}
	
	/**
     * Registers all of the {@link ThumbnailGeneratorIFace}s described in the registry file.
     * 
	 * @param generatorRegistryFile the XML file containing the thumbnail generator class names
	 * @throws SAXException the file was not capable of being parsed
	 * @throws IOException the file couldn't be opened
	 * @throws ParserConfigurationException a DOM parser couldn't be created
	 * @throws ClassNotFoundException a class listed in the registry couldn't be located
	 * @throws InstantiationException a class listed in the registry couldn't be instantiated
	 * @throws IllegalAccessException a security violation occured while instantiating a class
	 */
	public void registerThumbnailers(final File generatorRegistryFile) throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		Document registry = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(generatorRegistryFile);
		NodeList generatorNodes = registry.getElementsByTagName("generator");
		for(int i = 0; i < generatorNodes.getLength(); ++i )
		{
			Node generatorNode = generatorNodes.item(i);
			Node classNameNode = generatorNode.getAttributes().getNamedItem("class");
			String generatorClassName = classNameNode.getNodeValue();
			ThumbnailGeneratorIFace gen = Class.forName(generatorClassName).asSubclass(ThumbnailGeneratorIFace.class).newInstance();
			for(String supportedMimeType: gen.getSupportedMimeTypes())
			{
				registerThumbnailGenerator(supportedMimeType,gen);
			}
		}
	}
	
	/**
	 * 
	 */
	private static void readIconMap()
	{
	    availableIcons = new HashMap<String, String>();
	    
	    File mimeTypeFile = XMLHelper.getConfigDir("mime_icons.xml");
	    if (mimeTypeFile.exists())
	    {
            try
            {
                Document registry = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(mimeTypeFile);
                NodeList mimeNodes = registry.getElementsByTagName("mimetype");
                for(int i = 0; i < mimeNodes.getLength(); ++i )
                {
                    Node     mimeNode     = mimeNodes.item(i);
                    Node     iconNameNode = mimeNode.getAttributes().getNamedItem("icon");
                    String   iconName     = iconNameNode.getNodeValue();
                    NodeList extNodes     = mimeNode.getChildNodes();
                    for(int j = 0; j < extNodes.getLength(); ++j )
                    {
                        Node   extNode = extNodes.item(j);
                        String ext     = extNode.getTextContent().trim();
                        if (StringUtils.isNotEmpty(ext))
                        {
                            availableIcons.put(ext, iconName);
                        }
                    }
                }
            } catch (SAXException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            } catch (ParserConfigurationException e)
            {
                e.printStackTrace();
            }
	    }
	}
	
	/**
     * Registers the given {@link ThumbnailGeneratorIFace} with the system.
     * 
	 * @param mimeType the MIME type handled by the generator
	 * @param generator the generator
	 * @return the registered generator
	 */
	public ThumbnailGeneratorIFace registerThumbnailGenerator(final String mimeType, final ThumbnailGeneratorIFace generator)
	{
		generator.setQuality(quality);
		generator.setMaxHeight(maxHeight);
		generator.setMaxWidth(maxWidth);
		return mimeTypeToGeneratorMap.put(mimeType,generator);
	}
	
	/**
     * Unregisters the {@link ThumbnailGeneratorIFace} that handles the given MIME type.
     * 
	 * @param mimeType the MIME type to unregister
	 * @return the unregistered generator, null if one was not registered
	 */
	public ThumbnailGeneratorIFace removeGenerator(String mimeType)
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
	public void generateThumbnail(final String originalFile, 
	                              final String outputFile,
	                              final boolean doHighQuality) throws IOException
	{
        // get the system MIME type mapper
        MimetypesFileTypeMap mimeMap = AttachmentUtils.getMimeMap();
        
        // get the MIME type of the given original file
		String mimeType = mimeMap.getContentType(originalFile);
        
        // find the appropriate thumbnail generator, if any
		ThumbnailGeneratorIFace generator = mimeTypeToGeneratorMap.get(mimeType);
		if (generator != null)
		{
            if (!generator.generateThumbnail(originalFile, outputFile, doHighQuality))
            {
                UIRegistry.getStatusBar().setLocalizedText("Thumbnailer.THMB_NO_CREATE", originalFile);
            }
            return;
		}
		
		String iconName = null;
		
		String ext = FilenameUtils.getExtension(originalFile);
		if (StringUtils.isNotEmpty(ext))
		{
		    iconName = availableIcons.get(ext);
		}
		
		if (StringUtils.isEmpty(iconName))
		{
		    iconName = "unknown";
		}
		
        IconEntry entry = IconManager.getIconEntryByName(iconName);
        if (entry != null)
        {
            BufferedImage bi = ImageIO.read(entry.getUrl());
            ImageIO.write(bi, "PNG", new FileOutputStream(outputFile));
        }
	}
	
	/**
     * Sets the maximum width of any visual thumbnails generated.
     * {@link ThumbnailGeneratorIFace#setMaxWidth(int)} is called on all registered
     * generators.
     * 
	 * @param maxWidth the maximum width
	 */
	public void setMaxWidth(int maxWidth)
	{
		Enumeration<ThumbnailGeneratorIFace> tgs = mimeTypeToGeneratorMap.elements();
		while(tgs.hasMoreElements())
		{
			tgs.nextElement().setMaxWidth(maxWidth);
		}
		
		this.maxWidth = maxWidth;
	}

	/**
     * Sets the maximum height of any visual thumbnails generated.
     * {@link ThumbnailGeneratorIFace#setMaxHeight(int)} is called on all registered
     * generators.
     * 
     * @param maxHeight the maximum width
	 */
	public void setMaxHeight(int maxHeight)
	{
		Enumeration<ThumbnailGeneratorIFace> tgs = mimeTypeToGeneratorMap.elements();
		while(tgs.hasMoreElements())
		{
			tgs.nextElement().setMaxHeight(maxHeight);
		}
		
		this.maxHeight = maxHeight;
	}

	/**
     * Sets the quality factor of any thumbnails generated.  Note that not
     * all {@link ThumbnailGeneratorIFace}s make use of a quality factor.
     * {@link ThumbnailGeneratorIFace#setQuality(float)} is called on all registered
     * generators.
     * 
	 * @param percent the quality factor
	 */
	public void setQuality(float percent)
	{
		Enumeration<ThumbnailGeneratorIFace> tgs = mimeTypeToGeneratorMap.elements();
		while(tgs.hasMoreElements())
		{
			tgs.nextElement().setQuality(percent);
		}
		
		this.quality = percent;
	}
	
	/**
	 * Get icon name for extension.
	 * @param ext file extension 
	 * @return the icon name for a file extension
	 */
	public static String getIconNameFromExtension(final String ext)
	{
	    return availableIcons.get(ext);
	}
	
	/**
	 * A simple demonstration of this class, using an image thumbnail generator.
     * Note that this demo only works provided the hard coded file names are valid.
	 *
	 * @param args ignored
	 * @throws Exception all sorts of reasons
	 */
	/*public static void main(String[] args) throws Exception
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
	}*/
}
