/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttachment;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;
import edu.ku.brc.ui.UIRegistry;


/**
 * @author timo
 *
 */
public class SymbiotaDarwinCoreArchiver 
{
    protected static final Logger     log = Logger.getLogger(SymbiotaDarwinCoreArchiver.class);

	protected final Integer mappingID;
	protected final List<Class<?>> extensions;
	protected File occurrencesFile;
	protected File identificationsFile;
	protected File imagesFile;
	protected File metaFile;

	
	/**
	 * @param mappingID
	 */
	public SymbiotaDarwinCoreArchiver(final Integer mappingID)
	{
		this.mappingID = mappingID;
		this.extensions = getExtensionClasses();
	}
	
	/**
	 * @param archiveDir
	 * @param archiveName
	 * @throws Exception
	 */
	protected void initializeFiles(File archiveDir, String archiveName) throws Exception
	{
		if (!archiveDir.isDirectory()) 
		{
			throw new Exception(UIRegistry.getResourceString("SymbiotaDarwinCoreArchiver.InvalidArchiveDirArg"));
		}
		occurrencesFile = new File(archiveDir.getAbsolutePath() + File.separator + "occurrences.csv");
		identificationsFile = new File(archiveDir.getAbsolutePath() + File.separator + "identifications.csv");
		imagesFile = new File(archiveDir.getAbsolutePath() + File.separator + "images.csv");
	}
	
	/**
	 * @param rec
	 * 
	 * force load extended classes
	 * 
	 * rec must be attached to an open session
	 */
	protected void loadExtensions(Object rec)
	{
		//could use reflection to find methods that load lists of classes in this.extensions but for now...
		((CollectionObject)rec).getDeterminations();
		((CollectionObject)rec).getCollectionObjectAttachments();
	}
	
	/**
	 * @param cls
	 * @param record
	 * @return
	 * @throws Exception
	 */
	protected <T> T getObject(Class<T> cls, RecordSetItem record) throws Exception
	{
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        T result = null;
        try
        {
        	result = session.get(cls, record.getRecordId());
        	loadExtensions(result);
        	if (result == null)
        	{
        		throw new Exception(String.format(UIRegistry.getResourceString("SymbiotaDarwinCoreArchiver.NoMatchingRecordFor"), record.getRecordId()));
        	}
        } finally
        {
        	session.close();
        }
        return result;
	}
	
	protected List<Class<?>> getExtensionClasses()
	{
		List<Class<?>> result = new ArrayList<Class<?>>();
		result.add(Determination.class);
		result.add(CollectionObjectAttachment.class);
		return result;
	}
	
	protected void archiveRecord(RecordSetItem record) throws Exception
	{
		DarwinCoreArchiveFieldMapper mapper = new DarwinCoreArchiveFieldMapper(getObject(CollectionObject.class, record), mappingID, extensions);
		
		
	}
	
	public boolean createArchive(File archiveDir, String archiveName, RecordSet records)
	{
		try 
		{
			if (records.getTableId() != CollectionObject.getClassTableId())
			{
				throw new Exception(UIRegistry.getResourceString("SymbiotaDarwinCoreArchiver.InvalidBaseTable"));
			}
			initializeFiles(archiveDir, archiveName);
			for (RecordSetItem record : records.getRecordSetItems())
			{
				archiveRecord(record);
			}
			return true;
		} catch (Exception ex)
		{
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SymbiotaDarwinCoreArchiver.class, ex);
            log.error(ex);
			return false;
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub

	}

}
