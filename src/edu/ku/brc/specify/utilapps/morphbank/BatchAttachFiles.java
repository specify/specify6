/**
 * 
 */
package edu.ku.brc.specify.utilapps.morphbank;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttachment;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 * Attaches files in a directory to specimens (or other objects) in a Specify database
 */
public class BatchAttachFiles
{
	protected static String[] exts = {"TIF", "JPG", "PNG"};
	protected final Class<?> tblClass;
	protected final Class<?> attachmentClass;
	protected final FileNameParserIFace fnParser;
	protected final File directory;
	protected List<File> files;
	protected List<Pair<String, String>> errors = new Vector<Pair<String, String>>();
	protected List<Integer> attachments = new Vector<Integer>();
	
	/**
	 * @param tblClass
	 * @param fnParser
	 * @param directoryName
	 */
	public BatchAttachFiles(Class<?> tblClass, FileNameParserIFace fnParser,
			File directory) throws Exception
	{
		super();
		this.tblClass = tblClass;
		this.fnParser = fnParser;
		this.directory = directory;
		attachmentClass = determineAttachmentClass();
		bldFiles();
	}
	
	/**
	 * @return attachment class for the table class
	 * @throws Exception
	 */
	protected Class<?> determineAttachmentClass() throws Exception
	{
		if (tblClass.equals(CollectionObject.class))
		{
			return CollectionObjectAttachment.class;
		} else
		{
			throw new Exception(String.format(UIRegistry.getResourceString("BatchAttachFiles.ClassNotSupported"), tblClass.getName()));
		}
	}
	
	/**
	 * @return the tblClass
	 */
	public Class<?> getTblClass()
	{
		return tblClass;
	}
	/**
	 * @return the fnParser
	 */
	public FileNameParserIFace getFnParser()
	{
		return fnParser;
	}
	/**
	 * @return the directoryName
	 */
	public File getDirectory()
	{
		return directory;
	}
	
	/**
	 * build a list of files in directory.
	 */
	protected void bldFiles()
	{
		files = new Vector<File>();
		Collection<?> fs = FileUtils.listFiles(directory, exts, false);
		for (Object f : fs)
		{
			files.add((File )f);
		}
	}
	
	/**
	 * Attach the files in directory.
	 */
	public void attachFiles() 
	{
		errors.clear();
		attachments.clear();
		for (File f : files)
		{
			attachFile(f);
		}
	}
	
	/**
	 * @param f
	 * 
	 * Attach f.
	 */
	protected void attachFile(File f)
	{
		System.out.println("Attaching " + f.getName());
		List<Integer> ids = fnParser.getRecordIds(f.getName());
		if (ids.size() == 0)
		{
			errors.add(new Pair<String, String>(f.getName(), 
					UIRegistry.getResourceString("BatchAttachFiles.FileNameParseError")));
			return;
		} 
		
		for (Integer id : ids)
		{
			attachFileTo(f, id);
		}
	}
	
	protected void attachFileTo(File f, Integer attachTo)
	{
		System.out.println("Attaching " + f.getName() + " to " + attachTo);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try
		{
			BatchAttachFiles baf = new BatchAttachFiles(CollectionObject.class, new BarCodeFileNameParser(),
					new File("/home/timo/TroyImages"));
			baf.attachFiles();
		} catch (Exception ex)
		{
			System.out.println(ex.getMessage());
			System.exit(-1);
		}
	}
}
