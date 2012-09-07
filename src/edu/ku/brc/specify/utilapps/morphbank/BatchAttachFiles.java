/**
 * 
 */
package edu.ku.brc.specify.utilapps.morphbank;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.GenericLSIDGeneratorFactory;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAttachment;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttachmentOwnerIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttachment;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttachment;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.LocalityAttachment;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonAttachment;
import edu.ku.brc.specify.tools.export.ExportPanel;
import edu.ku.brc.specify.tools.ireportspecify.MainFrameSpecify;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 * Attaches files in a directory to specimens (or other objects) in a Specify database
 */
public class BatchAttachFiles
{
    protected static final String PROGRESS = "PROGRESS";
    
    protected static final Logger log = Logger.getLogger(BatchAttachFiles.class);
    
	protected static String[] exts = {"TIF", "JPG", "PNG", "jpg", "png", "tif", "TIFF", "tiff"};
	protected final Class<?> tblClass;
	protected final Class<?> attachmentClass;
	protected final FileNameParserIFace fnParser;
	protected final File     directory;
	protected List<File>     files;
	protected String         keyName;
	protected List<Pair<String, String>> errors = new Vector<Pair<String, String>>();
	protected DataProviderSessionIFace session;
	protected String errLogName = "errors";
	
	protected HashMap<String, String> mapFileNameToCatNum = null;
	
	//protected List<Integer> attachments = new Vector<Integer>();
	
	/**
	 * @param tblClass
	 * @param fnParser
	 * @param directoryName
	 */
	public BatchAttachFiles(Class<?> tblClass, FileNameParserIFace fnParser, File directory) throws Exception
	{
		super();
		this.tblClass = tblClass;
		this.fnParser = fnParser;
		this.directory = directory;
		attachmentClass = determineAttachmentClass();
		if (directory.isDirectory())
		{
			files = bldFilesFromDir(directory, exts);
		} else
		{
			bldFilesFromList();
		}
	}
	
    public BatchAttachFiles(final Class<?> tblClass,
                            final String keyName,
                            final File directory) throws Exception
    {
        super();
        this.tblClass   = tblClass;
        this.keyName    = keyName;
        this.fnParser   = null;
        this.directory  = directory;
        attachmentClass = determineAttachmentClass();
        if (directory.isDirectory())
        {
            files = bldFilesFromDir(directory, exts);
        }
    }
    
    /**
     * @param indexFile
     * @return
     */
    public boolean attachFileFromIndexFile(final File indexFile)
    {
        mapFileNameToCatNum = new HashMap<String, String>();
        try
        {
            List<?>      records     = (List<?>)FileUtils.readLines(indexFile);
            for (Object lineObj : records)
            {
                String[] cols = StringUtils.split(lineObj.toString(), '\t');
                if (cols.length == 2)
                {
                    String fileName = cols[1];
                    mapFileNameToCatNum.put(fileName, cols[0]);
                    System.out.println(String.format("%s %s", cols[0], fileName));
                }
            }
            attachFilesByFieldName();
            return true;
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * Does the backup on a SwingWorker Thread.
     * @param isMonthly whether it is a monthly backup
     * @param doSendAppExit requests sending an application exit command when done
     * @return true if the prefs are set up and there were no errors before the SwingWorker thread was started
     */
    public boolean attachFilesByFieldName()//final boolean doCreateRecords)
    {
        if (files == null)
        {
            return false;
        }
        SwingWorker<Integer, Integer> backupWorker = new SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                for (File file : files)
                {
                    String catNum = mapFileNameToCatNum.get(file.getName());
                    if (catNum != null) System.out.println("catNum["+catNum+"]  ["+file.getName()+"]");
                }
                
                DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(tblClass.getSimpleName());
                if (tblInfo != null)
                {
                    UIFieldFormatterIFace fmt = DBTableIdMgr.getFieldFormatterFor(tblClass, keyName);
                    PreparedStatement pStmt = null;
                    try
                    {
                        int    numFiles  = files.size();
                        int    total     = 0;
                        int    one20th   = (int)((double)numFiles / 20.0);
                        int    prevTenth = 0;
                        
                        String sql = String.format("SELECT %s FROM %s WHERE %s = ?", tblInfo.getPrimaryKeyName(), tblInfo.getName(), keyName);
                        //System.out.println(sql);
                        pStmt = DBConnection.getInstance().getConnection().prepareStatement(sql);
                        for (File file : files)
                        {
                            String primaryName = null;
                            if (mapFileNameToCatNum != null)
                            {
                                //System.out.println("file.getName()["+file.getName()+"]");
                                String catNum = mapFileNameToCatNum.get(file.getName());
                                if (catNum != null)
                                {
                                    primaryName = catNum;
                                }
                            } else
                            {
                                primaryName = FilenameUtils.getBaseName(file.getName()); 
                            }
                            if (primaryName != null) System.out.println("["+primaryName+"]");
                            
                            Object value = fmt != null ? fmt.formatFromUI(primaryName) : primaryName;  
                            
                            if (value instanceof String)
                            {
                                pStmt.setString(1, value.toString());
                                ResultSet rs = pStmt.executeQuery();
                                if (rs.next())
                                {
                                    int id = rs.getInt(1);
                                    System.out.println(String.format("%s -> id: %d", value.toString(), id));
                                    attachFileTo(file, id);
                                }
                                rs.close();
                            }
                            
                            total++;
                            if (numFiles < 21)
                            {
                                firePropertyChange(PROGRESS, numFiles, total); 
                            } else if (numFiles % one20th == 0)
                            {
                                int percent = (int)(((double)total / numFiles) * 100.0);
                                if (percent != prevTenth)
                                {
                                    prevTenth = percent;
                                    firePropertyChange(PROGRESS, 100, percent);
                                }
                            }
                            //Thread.currentThread().sleep(2000);
                            //if (cnt == 2) break;
                        }
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        
                    } finally
                    {
                        if (pStmt != null)
                        {
                            try
                            {
                                pStmt.close();
                            } catch (SQLException ex) {}
                        }
                    }
                }
                
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                UIRegistry.clearSimpleGlassPaneMsg();
            }
        };
        
        final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("MySQLBackupService.BACKINGUP", ""), 24);
        glassPane.setProgress(0);
        
        backupWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (PROGRESS.equals(evt.getPropertyName())) 
                        {
                            System.out.println("Progress: "+evt.getNewValue());
                            glassPane.setProgress((Integer)evt.getNewValue());
                        }
                    }
                });
        backupWorker.execute();
        
        return true;
    }
    
    /**
     * 
     */
    public void attachFilesByFieldNameOld()
    {
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(tblClass.getSimpleName());
        if (tblInfo != null)
        {
            UIFieldFormatterIFace fmt = DBTableIdMgr.getFieldFormatterFor(tblClass, keyName);
            PreparedStatement pStmt = null;
            try
            {
                String sql = String.format("SELECT %s FROM %s WHERE %s = ?", tblInfo.getIdFieldName(), tblInfo.getName(), keyName);
                pStmt = DBConnection.getInstance().getConnection().prepareStatement(sql);
                for (File file : files)
                {
                    Object value = FilenameUtils.getBaseName(file.getName());
                    if (fmt != null)
                    {
                        value = fmt.formatFromUI(value);
                    }
                    if (value instanceof String)
                    {
                        pStmt.setString(1, value.toString());
                        ResultSet rs = pStmt.executeQuery();
                        if (rs.next())
                        {
                            int id = rs.getInt(1);
                            attachFileTo(file, id);
                        }
                        rs.close();
                    }
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                
            } finally
            {
                if (pStmt != null)
                {
                    try
                    {
                        pStmt.close();
                    } catch (SQLException ex) {}
                }
            }
        }
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
	public static Vector<File> bldFilesFromDir(File directory, String[] exts)
	{
		Vector<File> result = new Vector<File>();
		Collection<?> fs = FileUtils.listFiles(directory, exts, false);
		for (Object f : fs)
		{
			result.add((File )f);
//			if (files.size() == 10)
//			{
//				System.out.println("!!!!!!!!!Only processing first 10 files!!!!!!!!!");
//				break;
//			}
		}
		return result;
	}
	
	/**
	 * builds 'files' from file containing list of file names 
	 */
	protected void bldFilesFromList() throws IOException
	{
		files = new Vector<File>();
		List<?> fileNames = FileUtils.readLines(directory);
		for (Object f : fileNames)
		{
			files.add(new File((String )f));
//			if (files.size() == 10)
//			{
//				System.out.println("!!!!!!!!!Only processing first 10 files!!!!!!!!!");
//				break;
//			}
		}
	}
	/**
	 * Attach the files in directory.
	 */
	public void attachFiles() throws Exception
	{
		errors.clear();
		//attachments.clear();
		//session = DataProviderFactory.getInstance().createSession();
		try
		{
			for (File f : files)
			{
				attachFile(f);
			}
			if (errors.size() > 0)
			{
				Vector<String> errLines = new Vector<String>();
				for (Pair<String, String> error : errors)
				{
					String errLine = error.getFirst() + ": " + error.getSecond();
					System.out.println(errLine);
					errLines.add(errLine);
				}
				FileUtils.writeLines(new File(errLogName), errLines);
			} else
			{
				System.out.println("All files in the directory were attached.");
			}
		} finally
		{
			//session.close();
		}
	}
	
	/**
	 * @param f
	 * 
	 * Attach f.
	 */
	protected void attachFile(File f)
	{
		//System.out.println("Attaching " + f.getName());
		//System.out.println("attachFile Entry: " + Runtime.getRuntime().freeMemory());
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
		//System.out.println("attachFile Exit: " + Runtime.getRuntime().freeMemory());
	}

	
	/**
	 * @return the errLogName
	 */
	public String getErrLogName() 
	{
		return errLogName;
	}

	/**
	 * @param errLogName the errLogName to set
	 */
	public void setErrLogName(String errLogName) {
		this.errLogName = errLogName;
	}

	/**
	 * @param cls
	 * @return an initialized instance of the appropriate OjbectAttachmentIFace implementation.
	 */
	protected ObjectAttachmentIFace<? extends DataModelObjBase> getAttachmentObject(final Class<?> cls)
	{
		ObjectAttachmentIFace<? extends DataModelObjBase> result = null;
		if (cls.equals(Accession.class))
		{
			result = new AccessionAttachment();
		}
		if (cls.equals(Taxon.class))
		{
			result =  new TaxonAttachment();
		}
		if (cls.equals(Locality.class))
		{
			result =  new LocalityAttachment();
		}
		if (cls.equals(CollectingEvent.class))
		{
			result =  new CollectingEventAttachment();
		}
		if (cls.equals(CollectionObject.class))
		{
			result =  new CollectionObjectAttachment();
		}
		if (result != null)
		{
			((DataModelObjBase )result).initialize();
		}
		return result;
    }

    /**
     * @param f
     * @param attachTo
     * 
     * Attaches f to the object with key attachTo
     */
    @SuppressWarnings("unchecked")
    protected void attachFileTo(final File f, final Integer attachTo)
    {
        attachFileTo(f, attachTo, null);
    }

    /**
     * @param fileToSave
     * @param attachToId
     * 
     * Attaches f to the object with key attachTo
     */
    @SuppressWarnings("unchecked")
    protected void attachFileTo(final File fileToSave, final Integer attachToId, final DataProviderSessionIFace session)
    {
		//System.out.println("Attaching " + f.getName() + " to " + attachTo);
		//System.out.println("attachFileTo Entry: " + Runtime.getRuntime().freeMemory());
		DataProviderSessionIFace localSession = session == null ? DataProviderFactory.getInstance().createSession() : session;
		boolean tblTransactionOpen = false;
		if (localSession != null)
		{
			try
			{
				AttachmentOwnerIFace<?> rec = getAttachmentOwner(localSession, attachToId);
				//session.attach(rec);
				localSession.beginTransaction();
				tblTransactionOpen = true;
				
				Set<ObjectAttachmentIFace<?>> attachees = (Set<ObjectAttachmentIFace<?>>) rec.getAttachmentReferences();
				int        ordinal    = 0;
				Attachment attachment = new Attachment();
				attachment.initialize();
				if (fileToSave.exists())
				{
					attachment.setOrigFilename(fileToSave.getPath());
				} else
				{
					attachment.setOrigFilename(fileToSave.getName());
				}
				
				attachment.setTableId(rec.getAttachmentTableId());
				
				attachment.setTitle(fileToSave.getName());
				ObjectAttachmentIFace<DataModelObjBase> oaif = 
				    (ObjectAttachmentIFace<DataModelObjBase>) getAttachmentObject(rec.getClass());
				//CollectionObjectAttachment oaif = new CollectionObjectAttachment();
				//oaif.initialize();
				oaif.setAttachment(attachment);
				oaif.setObject((DataModelObjBase) rec);
				//oaif.setCollectionObject((CollectionObject )rec);
				oaif.setOrdinal(ordinal);
				//((CollectionObject )rec).getAttachmentReferences().add(oaif);
				
				attachees.add(oaif);

				BusinessRulesIFace busRule = DBTableIdMgr.getInstance()
						.getBusinessRule(rec.getClass());
				if (busRule != null)
				{
					busRule.beforeSave(rec, localSession);
				}
				localSession.saveOrUpdate(rec);
				
				if (busRule != null)
				{
					if (!busRule.beforeSaveCommit(rec, localSession))
					{
						localSession.rollback();
						throw new Exception("Business rules processing failed");
					}
				}
				if (fileToSave.exists())
				{
					AttachmentUtils.getAttachmentManager()
						.setStorageLocationIntoAttachment(oaif.getAttachment(), false);
					oaif.getAttachment().storeFile(false); // false means do not display an error dialog
				}

				localSession.commit();
				//System.out.println("ATTACHED " + f.getName() + " to " + attachTo);
				tblTransactionOpen = false;
				if (busRule != null)
				{
					busRule.afterSaveCommit(rec, localSession);
				}
				
				//this is necessary to prevent memory leak -- no idea why or how -- but the merge
				//prevents out-of-memory crashes that occur after about 6300 records.
				//session.merge(rec);
				
				attachees = null;
				attachment = null;
				rec = null;
				oaif = null;
				
			
			} catch (HibernateException he)
			{
				if (tblTransactionOpen)
				{
					localSession.rollback();
				}
				errors.add(new Pair<String, String>(fileToSave.getName(), he.getLocalizedMessage()));
				
			} catch (Exception ex)
			{
				if (tblTransactionOpen)
				{
					localSession.rollback();
				}
				errors.add(new Pair<String, String>(fileToSave.getName(), ex
						.getLocalizedMessage()));
			} finally
			{
			    if (session == null)
			    {
			        localSession.close();
			    }
				//session.clear();
			}
		} else
		{
			errors.add(new Pair<String, String>(fileToSave.getName(), UIRegistry
					.getResourceString("BatchAttachFiles.UnableToAttach")));
		}
		System.out.println("attachFileTo Exit: " + Runtime.getRuntime().freeMemory());
	}
	
	/**
	 * @param session
	 * @param recId
	 * @return record with key recId
	 */
	protected AttachmentOwnerIFace<?> getAttachmentOwner(DataProviderSessionIFace sessionArg, Integer recId)
	{
		return (AttachmentOwnerIFace<?>)sessionArg.get(tblClass, recId);
	}
	
	/**
	 * @param args
	 */
	    public static void main(String[] args)
	    {
	        log.debug("********* Current ["+(new File(".").getAbsolutePath())+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        // This is for Windows and Exe4J, turn the args into System Properties
	 
	        // Set App Name, MUST be done very first thing!
	        //UIRegistry.setAppName("SchemaExporter");  //$NON-NLS-1$
	        UIRegistry.setAppName("Specify");  //$NON-NLS-1$

	        UIRegistry.setEmbeddedDBPath(UIRegistry.getDefaultEmbeddedDBPath()); // on the local machine
	        
	        for (String s : args)
	        {
	            String[] pairs = s.split("="); //$NON-NLS-1$
	            if (pairs.length == 2)
	            {
	                if (pairs[0].startsWith("-D")) //$NON-NLS-1$
	                {
	                    System.setProperty(pairs[0].substring(2, pairs[0].length()), pairs[1]);
	                } 
	            } else
	            {
	                String symbol = pairs[0].substring(2, pairs[0].length());
	                System.setProperty(symbol, symbol);
	            }
	        }
	        
	        // Now check the System Properties
	        String appDir = System.getProperty("appdir");
	        if (StringUtils.isNotEmpty(appDir))
	        {
	            UIRegistry.setDefaultWorkingPath(appDir);
	        }
	        
	        String appdatadir = System.getProperty("appdatadir");
	        if (StringUtils.isNotEmpty(appdatadir))
	        {
	            UIRegistry.setBaseAppDataDir(appdatadir);
	        }
	        
	        // For Debugging Only 
	        //System.setProperty("mobile", "true");
	        //System.setProperty("embedded", "true");
	        
	        String mobile = System.getProperty("mobile");
	        if (StringUtils.isNotEmpty(mobile))
	        {
	            UIRegistry.setMobile(true);
	        }
	        
	        String embeddedStr = System.getProperty("embedded");
	        if (StringUtils.isNotEmpty(embeddedStr))
	        {
	            UIRegistry.setEmbedded(true);
	        }
	        
	        String embeddeddbdir = System.getProperty("embeddeddbdir");
	        if (StringUtils.isNotEmpty(embeddeddbdir))
	        {
	            UIRegistry.setEmbeddedDBPath(embeddeddbdir);
	        } else
	        {
	            UIRegistry.setEmbeddedDBPath(UIRegistry.getDefaultEmbeddedDBPath()); // on the local machine
	        }
	        
	        // Then set this
	        IconManager.setApplicationClass(Specify.class);
	        IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml")); //$NON-NLS-1$
	        IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml")); //$NON-NLS-1$
	        IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml")); //$NON-NLS-1$

	        
	        
	        System.setProperty(AppContextMgr.factoryName,                   "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr //$NON-NLS-1$
	        System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences //$NON-NLS-1$
	        System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");            // Needed By UIRegistry //$NON-NLS-1$ //$NON-NLS-2$
	        System.setProperty("edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory", "edu.ku.brc.specify.ui.SpecifyDraggableRecordIdentiferFactory"); // Needed By the Form System //$NON-NLS-1$ //$NON-NLS-2$
	        System.setProperty("edu.ku.brc.dbsupport.AuditInterceptor",     "edu.ku.brc.specify.dbsupport.AuditInterceptor");       // Needed By the Form System for updating Lucene and logging transactions //$NON-NLS-1$ //$NON-NLS-2$
	        System.setProperty("edu.ku.brc.dbsupport.DataProvider",         "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set //$NON-NLS-1$ //$NON-NLS-2$
	        System.setProperty("edu.ku.brc.ui.db.PickListDBAdapterFactory", "edu.ku.brc.specify.ui.db.PickListDBAdapterFactory");   // Needed By the Auto Cosmplete UI //$NON-NLS-1$ //$NON-NLS-2$
	        System.setProperty(CustomQueryFactory.factoryName,              "edu.ku.brc.specify.dbsupport.SpecifyCustomQueryFactory"); //$NON-NLS-1$
	        System.setProperty(UIFieldFormatterMgr.factoryName,             "edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr");           // Needed for CatalogNumberign //$NON-NLS-1$
	        System.setProperty(QueryAdjusterForDomain.factoryName,          "edu.ku.brc.specify.dbsupport.SpecifyQueryAdjusterForDomain"); // Needed for ExpressSearch //$NON-NLS-1$
	        System.setProperty(SchemaI18NService.factoryName,               "edu.ku.brc.specify.config.SpecifySchemaI18NService");         // Needed for Localization and Schema //$NON-NLS-1$
	        System.setProperty(WebLinkMgr.factoryName,                      "edu.ku.brc.specify.config.SpecifyWebLinkMgr");                // Needed for WebLnkButton //$NON-NLS-1$
	        System.setProperty(SecurityMgr.factoryName,                     "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");              // Needed for Tree Field Names //$NON-NLS-1$
	        System.setProperty(DBMSUserMgr.factoryName,                     "edu.ku.brc.dbsupport.MySQLDMBSUserMgr");
	        System.setProperty(SchemaUpdateService.factoryName,             "edu.ku.brc.specify.dbsupport.SpecifySchemaUpdateService");   // needed for updating the schema
	        System.setProperty(GenericLSIDGeneratorFactory.factoryName,     "edu.ku.brc.specify.config.SpecifyLSIDGeneratorFactory");
	        
	        final AppPreferences localPrefs = AppPreferences.getLocalPrefs();
	        localPrefs.setDirPath(UIRegistry.getAppDataDir());
	        adjustLocaleFromPrefs();
	    	final String iRepPrefDir = localPrefs.getDirPath(); 
	        int mark = iRepPrefDir.lastIndexOf(UIRegistry.getAppName(), iRepPrefDir.length());
	        final String SpPrefDir = iRepPrefDir.substring(0, mark) + "Specify";
	        HibernateUtil.setListener("post-commit-update", new edu.ku.brc.specify.dbsupport.PostUpdateEventListener()); //$NON-NLS-1$
	        HibernateUtil.setListener("post-commit-insert", new edu.ku.brc.specify.dbsupport.PostInsertEventListener()); //$NON-NLS-1$
	        HibernateUtil.setListener("post-commit-delete", new edu.ku.brc.specify.dbsupport.PostDeleteEventListener()); //$NON-NLS-1$
	        
	        SwingUtilities.invokeLater(new Runnable() {
	            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
	          public void run()
	            {
	                
	                try
	                {
	                    UIHelper.OSTYPE osType = UIHelper.getOSType();
	                    if (osType == UIHelper.OSTYPE.Windows )
	                    {
	                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
	                        PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
	                        
	                    } else if (osType == UIHelper.OSTYPE.Linux )
	                    {
	                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
	                    }
	                }
	                catch (Exception e)
	                {
	                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
	                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExportPanel.class, e);
	                    log.error("Can't change L&F: ", e); //$NON-NLS-1$
	                }
	                
	                DatabaseLoginPanel.MasterPasswordProviderIFace usrPwdProvider = new DatabaseLoginPanel.MasterPasswordProviderIFace()
	                {
	                    @Override
	                    public boolean hasMasterUserAndPwdInfo(final String username, final String password, final String dbName)
	                    {
	                        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password))
	                        {
	                            UserAndMasterPasswordMgr.getInstance().set(username, password, dbName);
	                            boolean result = false;
	                            try
	                            {
	                            	try
	                            	{
	                            		AppPreferences.getLocalPrefs().flush();
	                            		AppPreferences.getLocalPrefs().setDirPath(SpPrefDir);
	                            		AppPreferences.getLocalPrefs().setProperties(null);
	                            		result = UserAndMasterPasswordMgr.getInstance().hasMasterUsernameAndPassword();
	                            	}
	                            	finally
	                            	{
	                            		AppPreferences.getLocalPrefs().flush();
	                            		AppPreferences.getLocalPrefs().setDirPath(iRepPrefDir);
	                            		AppPreferences.getLocalPrefs().setProperties(null);
	                            	}
	                            } catch (Exception e)
	                            {
	                            	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
	                            	edu.ku.brc.exceptions.ExceptionTracker.getInstance()
	    								.capture(MainFrameSpecify.class, e);
	                            	result = false;
	                            }
	                            return result;
	                        }
	                        return false;
	                    }
	                    
	                    @Override
	                    public Pair<String, String> getUserNamePassword(final String username, final String password, final String dbName)
	                    {
	                        UserAndMasterPasswordMgr.getInstance().set(username, password, dbName);
                            
	                        Pair<String, String> result = null;
	                        try
	                        {
	                        	try
	                        	{
	                        		AppPreferences.getLocalPrefs().flush();
	                        		AppPreferences.getLocalPrefs().setDirPath(SpPrefDir);
	                        		AppPreferences.getLocalPrefs().setProperties(null);
	                        		result = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
	                        	}
	                        	finally
	                        	{
	                        		AppPreferences.getLocalPrefs().flush();
	                        		AppPreferences.getLocalPrefs().setDirPath(iRepPrefDir);
	                        		AppPreferences.getLocalPrefs().setProperties(null);
	                        	}
	                        } catch (Exception e)
	                        {
	                        	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
	                        	edu.ku.brc.exceptions.ExceptionTracker.getInstance()
									.capture(MainFrameSpecify.class, e);
	                        	result = null;
	                        }
	                        return result;
	                    }
	                    @Override
	                    public boolean editMasterInfo(final String username, final String dbName, final boolean askFroCredentials)
	                    {
	                        boolean result = false;
	                    	try
	                        {
	                        	try
	                        	{
	                        		AppPreferences.getLocalPrefs().flush();
	                        		AppPreferences.getLocalPrefs()
										.setDirPath(SpPrefDir);
	                        		AppPreferences.getLocalPrefs().setProperties(null);
	                        		result =  UserAndMasterPasswordMgr
										.getInstance()
										.editMasterInfo(username, dbName, askFroCredentials);
	                        	} finally
	                        	{
	                        		AppPreferences.getLocalPrefs().flush();
	                        		AppPreferences.getLocalPrefs().setDirPath(
										iRepPrefDir);
	                        		AppPreferences.getLocalPrefs().setProperties(null);
	                        	}
	                        } catch (Exception e)
	                        {
	                        	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
	                        	edu.ku.brc.exceptions.ExceptionTracker.getInstance()
									.capture(MainFrameSpecify.class, e);
	                        	result = false;
	                        }
	                    	return result;
	                   }
	                };
	                String nameAndTitle = UIRegistry.getResourceString("BatchAttachFiles.AppTitle"); // I18N
	                UIRegistry.setRelease(true);
	                UIHelper.doLogin(usrPwdProvider, true, false, false, new BatchAttachLauncher(), Specify.getLargeIconName(), nameAndTitle, nameAndTitle, "SpecifyWhite32", "login"); // true
																																		// means
																																		// do
																																		// auto
																																		// login
																																		// if
																																		// it
																																		// can,
																																		// second
																																		// bool
																																		// means
																																		// use
																																		// dialog
																																		// instead
																																		// of
																																		// frame
	                
	                localPrefs.load();
	                
	            }
	        });

	       
	    }

    /**
     * 
     */
    protected static void adjustLocaleFromPrefs()
    {
        String language = AppPreferences.getLocalPrefs().get("locale.lang", null); //$NON-NLS-1$
        if (language != null)
        {
            String country  = AppPreferences.getLocalPrefs().get("locale.country", null); //$NON-NLS-1$
            String variant  = AppPreferences.getLocalPrefs().get("locale.var",     null); //$NON-NLS-1$
            
            Locale prefLocale = new Locale(language, country, variant);
            
            Locale.setDefault(prefLocale);
            UIRegistry.setResourceLocale(prefLocale);
        }
        
        try
        {
            ResourceBundle.getBundle("resources", Locale.getDefault()); //$NON-NLS-1$
            
        } catch (MissingResourceException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, ex);
            Locale.setDefault(Locale.ENGLISH);
            UIRegistry.setResourceLocale(Locale.ENGLISH);
        }
        
    }
    
}
