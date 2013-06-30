/**
 * 
 */
package edu.ku.brc.specify.utilapps.morphbank;

import static edu.ku.brc.ui.UIRegistry.clearSimpleGlassPaneMsg;
import static edu.ku.brc.ui.UIRegistry.displayConfirmLocalized;
import static edu.ku.brc.ui.UIRegistry.getAppDataDir;
import static edu.ku.brc.ui.UIRegistry.getAppName;
import static edu.ku.brc.ui.UIRegistry.getDefaultEmbeddedDBPath;
import static edu.ku.brc.ui.UIRegistry.getMostRecentWindow;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.setAppName;
import static edu.ku.brc.ui.UIRegistry.setBaseAppDataDir;
import static edu.ku.brc.ui.UIRegistry.setDefaultWorkingPath;
import static edu.ku.brc.ui.UIRegistry.setEmbedded;
import static edu.ku.brc.ui.UIRegistry.setEmbeddedDBPath;
import static edu.ku.brc.ui.UIRegistry.setMobile;
import static edu.ku.brc.ui.UIRegistry.setRelease;
import static edu.ku.brc.ui.UIRegistry.setResourceLocale;
import static edu.ku.brc.ui.UIRegistry.showLocalizedError;
import static edu.ku.brc.ui.UIRegistry.showLocalizedMsg;
import static edu.ku.brc.ui.UIRegistry.writeSimpleGlassPaneMsg;

import java.awt.FileDialog;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
import edu.ku.brc.af.core.GenericGUIDGeneratorFactory;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.helpers.ImageMetaDataHelper;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.AttachmentOwnerIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;
import edu.ku.brc.specify.tools.export.ExportPanel;
import edu.ku.brc.specify.tools.ireportspecify.MainFrameSpecify;
import edu.ku.brc.ui.ChooseFromListDlg;
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
    protected static final String PROGRESS     = "PROGRESS";
    protected static final String RESTR_PREFIX = "BatchAttachFiles.";
    
    protected static final Logger log = Logger.getLogger(BatchAttachFiles.class);
    protected static final int CN_ERROR_NONE      = 0;
    protected static final int CN_ERROR_INVALID   = 0;
    protected static final int CN_ERROR_NOT_IN_DB = 0;
    
    private enum FileErrorType { eMisingFile, eDuplicateCombo}
    
	protected static final String[] exts = {"TIF", "JPG", "PNG", "jpg", "png", "tif", "TIFF", "tiff"};
	
	protected FileNameParserIFace        fnParser;
	protected File                       directory;
    protected ArrayList<File>            files  = new ArrayList<File>();
    protected ArrayList<Integer>         recIds = new ArrayList<Integer>();
	protected String                     keyName;
	protected List<Pair<String, String>> errors     = new Vector<Pair<String, String>>();
	protected DataProviderSessionIFace   session;
	protected String                     errLogName = "errors";
	
    protected ArrayList<Pair<String, FileErrorType>> errFiles = new ArrayList<Pair<String, FileErrorType>>();
    
    protected HashMap<String, ArrayList<String>> mapFileNameToCatNum = null;
    
    // Data Members for converting FileNames to Cat Nums
    protected Pattern           regExNumericCatNumPattern = Pattern.compile("(?<=[0-9])-(?=[0-9a-zA-Z])|(?<=\\d)(?=\\p{L})|(?<=\\p{L})(?=\\d)");
    protected ArrayList<String> fileNamesInErrList        = new ArrayList<String>();
    protected PreparedStatement pStmtRE                   = null;
	protected int               catNumErrStatus           = CN_ERROR_NONE;
	
    /**
     * @param fnParser
     * @param directory
     * @param isForImagesOnly
     * @throws Exception
     */
    public BatchAttachFiles(final FileNameParserIFace fnParser, 
                            final File directory,
                            final boolean isForImagesOnly) throws Exception
    {
        super();
        this.fnParser  = fnParser;
        this.directory = directory;
        
        if (directory.isDirectory())
        {
            bldFilesFromDir(directory, isForImagesOnly ? exts : null);
        }
    }
    
    /**
     * @param fnParser
     * @param directory
     * @throws Exception
     */
    public BatchAttachFiles(final FileNameParserIFace fnParser, 
                            final List<File> files)
    {
        super();
        
        this.fnParser  = fnParser;
        this.directory = null;
        this.files     = new ArrayList<File>(files);
    }
    
    /**
     * @param indexFile
     * @return
     */
    public boolean attachFileFromIndexFile(final File indexFile)
    {
        if (files != null) files = null;
        
        String  ext          = FilenameUtils.getExtension(indexFile.getName().toLowerCase());
        boolean isTabDelim   = ext.equals("tab");
        boolean isCommaDelim = ext.equals("csv");
        if (ext.equals("txt"))
        {
            try
            {
                int tabCnt   = 0;
                int commaCnt = 0;
                int cnt      = 0;
                for (String line : FileUtils.readLines(indexFile))
                {
                    if (line.indexOf('\t') > 0) tabCnt++;
                    if (line.indexOf(',') > 0) commaCnt++;
                    if (!line.trim().isEmpty()) cnt++;
                    //System.out.println("["+line.trim()+"]");
                }
                if (tabCnt == cnt) 
                {
                    isTabDelim = true;
                } else
                {
                    if (commaCnt == cnt) isCommaDelim = true;
                }
                
                if (!isCommaDelim && !isTabDelim)
                {
                    UIRegistry.showLocalizedError("ATTCH_ERROR_PROCESSING");
                    return false;
                }
            } catch (IOException e)
            {
                UIRegistry.showLocalizedError("ATTCH_ERROR_PROCESSING");
                return false;
            }
        }
        
        mapFileNameToCatNum = new HashMap<String, ArrayList<String>>();
        try
        {
            char delim  = isTabDelim ? '\t' : ',';
            int  errCnt = 0;
            
            ArrayList<File>   dirFiles = new ArrayList<File>();
            List<?> records = (List<?>)FileUtils.readLines(indexFile);
            for (Object lineObj : records)
            {
                String line = lineObj.toString();
                if (line.indexOf(delim) == -1)
                {
                    errCnt++;
                    continue;
                }
                
                String[] cols = StringUtils.split(lineObj.toString(), delim);
                if (cols.length == 2)
                {
                    String fileName = cols[1].trim();
                    File upFile = new File(directory.getAbsolutePath() + File.separator + fileName);
                    if (upFile.exists())
                    {
                        String mappingValue = cols[0].trim();
                        ArrayList<String> catNumList = mapFileNameToCatNum.get(fileName);
                        if (catNumList == null)
                        {
                            catNumList = new ArrayList<String>();
                            mapFileNameToCatNum.put(fileName, catNumList);
                            dirFiles.add(upFile);
                            
                        } else if (catNumList.contains(mappingValue))
                        {
                            errFiles.add(new Pair<String, FileErrorType>(fileName, FileErrorType.eDuplicateCombo));
                            continue;
                        }
                        System.out.println(String.format("%s -> %s",mappingValue, fileName));
                        catNumList.add(mappingValue);
                        
                    } else
                    {
                        errFiles.add(new Pair<String, FileErrorType>(fileName, FileErrorType.eMisingFile));
                    }
                } else
                {
                    errCnt++;
                }
            }
            
            errCnt += errFiles.size();
            if (errCnt == records.size() && errCnt > 0)
            {
                showLocalizedError(isTabDelim? "IMPORT_IMG_BAD_DELIM_TAB" : "IMPORT_IMG_BAD_DELIM_COMMA");
                return false;
            }
            files = dirFiles;
            attachFiles();
            
            return true;
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static FileNameParserIFace chooseFileNameParser(final String helpContext)
    {
        final List<FileNameParserIFace> items = FileNameParserFactory.getInstance().getList();
        String title = getResourceString("BatchAttachFiles.CHOOSE_DEST");
        String msg   = getResourceString("BatchAttachFiles.CHOOSE_DESTMSG");
        ChooseFromListDlg<FileNameParserIFace> dlg = new ChooseFromListDlg<FileNameParserIFace>((Frame)getMostRecentWindow(), title, msg, ChooseFromListDlg.OKCANCELHELP, items);
        dlg.setHelpContext(helpContext);
        //dlg.setHelpContext("NEED_HELP_CONTEXT");
        dlg.createUI();
        
        dlg.getList().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                JList<?> list = (JList<?>)e.getSource();
                int selIndex = list.getSelectedIndex();
                if (selIndex > -1)
                {
                    final FileNameParserIFace fnp = items.get(selIndex);
                    
                    if (fnp.getTableId() == CollectingEvent.getClassTableId() && 
                        fnp.getFieldName().equals("stationFieldNumber"))
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                UIRegistry.showLocalizedError(JOptionPane.WARNING_MESSAGE, "ATTCH_UNIQUE_FIELD", fnp.getFieldTitle());
                            }
                        });
                    }
                }
            }
        });
        
        
        UIHelper.centerAndShow(dlg);
        if (dlg.isNotCancelled())
        {
            return (FileNameParserIFace)dlg.getSelectedObject();
        }
        return null;
    }
    
    /**
     * 
     */
    public static void attachFileFromIndexFile()
    {
        FileNameParserIFace fnParser = chooseFileNameParser("Import_Attachmap");
        if (fnParser != null)
        {
            JFileChooser fileChooser = new JFileChooser("BatchAttachFiles.CH_FILE_MSG");
            // Commented out for now, too dificult for Windows user
            // to operate
            //fileChooser.setFileFilter(new IndexFileFilter());
            
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            //fileChooser.setFileHidingEnabled(true);
    
            File indexFile      = null;
            int    returnVal = fileChooser.showOpenDialog(getTopWindow());
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                indexFile = fileChooser.getSelectedFile();
                try
                {
                    String           path      = FilenameUtils.getFullPath(indexFile.getAbsolutePath());
                    BatchAttachFiles batchFile = new BatchAttachFiles(fnParser, new File(path), false);
                    batchFile.attachFileFromIndexFile(indexFile);
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Ask for what table and field are the destination of the attachments, and then
     * asks for the directory or files.
     */
    public static void uploadAttachmentsByFileName()
    {
        FileNameParserIFace fnParser = chooseFileNameParser("Import_Attach");
        if (fnParser != null)
        {
            int rv = askForDirOrFiles();
            if (rv != JOptionPane.CANCEL_OPTION)
            {
                if (rv == JOptionPane.YES_OPTION)
                {
                    JFileChooser chooser = new JFileChooser("BatchAttachFiles.CH_FILE_MSG");
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            
                    File indexFile      = null;
                    int    returnVal = chooser.showOpenDialog(getTopWindow());
                    if (returnVal == JFileChooser.APPROVE_OPTION)
                    {
                        indexFile = chooser.getSelectedFile();
                        try
                        {
                            BatchAttachFiles batchFile = new BatchAttachFiles(fnParser, indexFile, false);
                            batchFile.attachFiles();
                            
                        } catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                } else
                {
                    String title = "title";
                    FileDialog dialog = new FileDialog((Frame)null, title, FileDialog.LOAD);
                    dialog.setMultipleMode(true);
                    
                    // FILE FILTER!!!!!!!!!
                    UIHelper.centerAndShow(dialog);
                    File[] selectedFiles = dialog.getFiles();
                    if (selectedFiles == null || selectedFiles.length == 0)
                    {
                        return;
                    }
                    ArrayList<File> list = new ArrayList<File>();
                    Collections.addAll(list, selectedFiles);
                    BatchAttachFiles batchFile = new BatchAttachFiles(fnParser, list);
                    batchFile.attachFiles();
                }
            }
        }
    }

    /**
     * @return
     */
    private static int askForDirOrFiles()
    {
        return displayConfirmLocalized("CHOOSE", "BatchAttachFiles.ASK_DIRORFILES_MSG", 
                "BatchAttachFiles.DO_DIRS", "BatchAttachFiles.DO_FILES", "CANCEL", JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * @param tw
     * @param file
     * @param fieldTitle
     * @param mappingFieldVal
     * @param resKey
     */
    private void writeError(final TableWriter tw, 
                            final File   file, 
                            final String fieldTitle, 
                            final String mappingFieldVal, 
                            final String resKey)
    {
        String msgFmt = getResourceString(RESTR_PREFIX + resKey);
        String msg    = String.format(msgFmt, fieldTitle, mappingFieldVal);
        tw.logErrors(file.getName(), msg);
    }
    
    /**
     * 
     */
    public void attachFiles()
    {
        if (files.size() == 0 || fnParser == null)
        {
            showLocalizedError("BatchAttachFiles.IMPORT_NO_FILES");
            return;
        }
        SwingWorker<Integer, Integer> backupWorker = new SwingWorker<Integer, Integer>()
        {
            int    attachedCnt = 0;
            int    totNumFiles = 0;
            
            @Override
            protected Integer doInBackground() throws Exception
            {
                try
                {
                    double percentThreshold = 10.0;
                    int    cnt              = 0;
                    
                    totNumFiles = files.size();
                    
                    if (mapFileNameToCatNum != null)
                    {
                        ArrayList<File> availFiles = new ArrayList<File>(files);
                        files.clear();
                        for (File file : availFiles)
                        {
                            ArrayList<String> catNumList = mapFileNameToCatNum.get(file.getName());
                            if (catNumList == null || catNumList.size() == 0)
                            {
                                totNumFiles--;
                            } else
                            {
                                totNumFiles += catNumList.size()-1;
                                files.add(file);
                            }
                        }
                    }
                    int incr = (int)((double)totNumFiles / percentThreshold);

                    String path = getAppDataDir() + File.separator + "fileupload.html";
                    TableWriter tw = new TableWriter(path, getResourceString("BatchAttachFiles.REPORT_TITLE"));
                    tw.startTable();
                    tw.logHdr(getResourceString("BatchAttachFiles.REPORT_FILE"), getResourceString("BatchAttachFiles.REPORT_REASON"));
                    
                    for (Pair<String, FileErrorType> p : errFiles)
                    {
                        tw.logErrors(p.first, getResourceString(RESTR_PREFIX+(p.second == FileErrorType.eMisingFile ? "FILE_MISSING" : "DUP_COMBO_MISSING")));
                    }

                    ArrayList<String> fieldValueList = new ArrayList<String>(); 

                    String fieldTitle = fnParser.getFieldTitle();
                    for (File file : files)
                    {
                        fieldValueList.clear();
                        if (mapFileNameToCatNum != null)
                        {
                            ArrayList<String> catNumList = mapFileNameToCatNum.get(file.getName());
                            if (catNumList == null || catNumList.size() == 0)
                            {
                                fieldValueList.add(FilenameUtils.getBaseName(file.getName()));
                            } else
                            {
                                fieldValueList.addAll(catNumList);
                            }
                        } else
                        {
                            fieldValueList.add(FilenameUtils.getBaseName(file.getName()));
                        }

                        for (String fieldValue : fieldValueList)
                        {
                            if (fnParser.isNameValid(fieldValue))
                            {
                                Integer id = fnParser.getRecordId(fieldValue);
                                if (id != null)
                                {
                                    //System.out.println(String.format("%s -> id: %d", value.toString(), id));
                                    if (attachFileTo(file, id))
                                    {
                                        attachedCnt++;
                                    } else
                                    {
                                        writeError(tw, file, fieldTitle, fieldValue, "ERR_SAVING");
                                    }
                                } else
                                {
                                    writeError(tw, file, fieldTitle, fieldValue, "VAL_NOT_IN_DB");
                                }
                            } else
                            {
                                writeError(tw, file, fieldTitle, fieldValue, "VAL_NOT_VALID");
                            }
                            
                            cnt++;
                            if (totNumFiles < 21)
                            {
                                firePropertyChange(PROGRESS, totNumFiles, cnt*5); 
                            } else if (cnt % incr == 0)
                            {
                                int percent = (int)(((double)cnt / totNumFiles) * 100.0);
                                firePropertyChange(PROGRESS, 100, percent);
                            }
                        }
                    }
                    tw.close();
                    
                    if (tw.hasLines())
                    {
                        File twFile = new File(path);
                        AttachmentUtils.openFile(twFile);
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    
                }
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                if (BatchAttachFiles.this.fnParser instanceof BaseFileNameParser)
                {
                    ((BaseFileNameParser)BatchAttachFiles.this.fnParser).cleanup();
                }
                clearSimpleGlassPaneMsg();
                
                if (attachedCnt > 0)
                {
                    showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, "BatchAttachFiles.NO_FILES_ATTACHED_TT", "BatchAttachFiles.NUM_FILES_ATTACHED", attachedCnt, totNumFiles);
                } else
                {
                    showLocalizedError("BatchAttachFiles.NO_FILES_ATTACHED");
                }
            }
        };
        
        final SimpleGlassPane glassPane = writeSimpleGlassPaneMsg(getResourceString("BatchAttachFiles.UPLOADING"), 24);
        glassPane.setProgress(0);
        
        backupWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (PROGRESS.equals(evt.getPropertyName())) 
                        {
                            //System.out.println("Progress: "+evt.getNewValue());
                            glassPane.setProgress((Integer)evt.getNewValue());
                        }
                    }
                });
        backupWorker.execute();

    }

	/**
	 * @return the tblClass
	 */
	public Class<?> getTblClass()
	{
		return fnParser.getAttachmentOwnerClass();
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
	 * @param file
	 * @param formatter
	 * @return
	 * @throws SQLException
	 */
	public Integer getRecordIDForFieldValue(final File file, final UIFieldFormatterIFace formatter) throws SQLException
	{
	    catNumErrStatus = CN_ERROR_NONE;
	    
        String  baseName  = FilenameUtils.getBaseName(file.getName());
	    int     fmtLen    = formatter.getLength();
	    boolean isNumeric = formatter.isNumeric();
	    
	    if (isNumeric)
	    {
	        String catNum = null;
	        if (!StringUtils.isNumeric(baseName)) // has trailing letters etc.
	        {
	            String[] tokens = regExNumericCatNumPattern.split(baseName);
	            if (tokens.length > 0)
	            {
	                catNum = (String)formatter.formatFromUI(tokens[0]);
	            } else
	            {
	                catNumErrStatus = CN_ERROR_INVALID;
	                return null;
	            }
	        } else // Just Numeric
	        {
	            catNum = (String)formatter.formatFromUI(baseName);
	        }
	        
	        if (catNum != null)
	        {
	            Integer recId = fnParser.getRecordId(catNum);
                if (recId != null)
                {
                    return recId;
                }
                catNumErrStatus = CN_ERROR_NOT_IN_DB;
                return null;
	        }
	        catNumErrStatus = CN_ERROR_INVALID;
	        
	    } else
	    {
	        String catNum = baseName.length() == fmtLen ? baseName : baseName.substring(0, fmtLen);
            if (formatter.isValid(catNum))
            {
                Integer recId = fnParser.getRecordId(catNum);
                if (recId != null)
                {
                    return recId;
                }
                catNumErrStatus = CN_ERROR_NOT_IN_DB;
            } else
            {
                catNumErrStatus = CN_ERROR_INVALID;
            }
	    }
	    return null;
	}
	
	/**
	 * build a list of files in directory.
	 */
	public void bldFilesFromDir(final File directory, final String[] exts)
	{
	    try
	    {
            files.clear();
            recIds.clear();
    	    fileNamesInErrList.clear();
    		
    		Collection<?> srcFileList  = FileUtils.listFiles(directory, exts, false);
    		for (Object f : srcFileList)
    		{
		        files.add((File)f);
    		}
    		
    		if (files.size() == 0)
    		{
    		    showLocalizedError(JOptionPane.WARNING_MESSAGE, "ATTCH_IMPORT_NO_FILES_SEL");
    		}
    		
		} catch (Exception ex)
		{
		    ex.printStackTrace();
		}
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
     * @param f
     * @param attachTo
     * 
     * Attaches f to the object with key attachTo
     */
    @SuppressWarnings("unchecked")
    protected boolean attachFileTo(final File f, final Integer attachTo)
    {
        return attachFileTo(f, attachTo, null);
    }
    
    /**
     * Attaches f to the object with key attachTo
     * 
     * @param fileToSave
     * @param attachToId
     * @param session
     */
    @SuppressWarnings("unchecked")
    protected boolean attachFileTo(final File    fileToSave, 
                                   final Integer attachToId, 
                                   final DataProviderSessionIFace session)
    {
        boolean isOK = true;
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
				
                attachment.setFileCreatedDate(ImageMetaDataHelper.getEmbeddedDateOrFileDate(fileToSave));
				
				attachment.setTitle(fileToSave.getName());
				//ObjectAttachmentIFace<DataModelObjBase> oaif = (ObjectAttachmentIFace<DataModelObjBase>) getAttachmentObject(rec.getClass());
				
				DataModelObjBase baseObj = (DataModelObjBase)fnParser.getAttachmentJoinClass().newInstance();
				baseObj.initialize();
				
				ObjectAttachmentIFace<DataModelObjBase> oaif = (ObjectAttachmentIFace<DataModelObjBase>)baseObj;
				oaif.setAttachment(attachment);
				oaif.setObject((DataModelObjBase) rec);
				oaif.setOrdinal(ordinal);
				
				attachees.add(oaif);

				BusinessRulesIFace busRule = DBTableIdMgr.getInstance().getBusinessRule(rec.getClass());
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
					AttachmentUtils.getAttachmentManager().setStorageLocationIntoAttachment(oaif.getAttachment(), false);
					oaif.getAttachment().storeFile(false); // false means do not display an error dialog
				} else
				{
				    log.debug(fileToSave.getName()+" doesn't exist.");
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
			    isOK = false;
				if (tblTransactionOpen)
				{
					localSession.rollback();
				}
				errors.add(new Pair<String, String>(fileToSave.getName(), he.getLocalizedMessage()));
				
			} catch (Exception ex)
			{
                isOK = false;
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
			errors.add(new Pair<String, String>(fileToSave.getName(), getResourceString("BatchAttachFiles.UnableToAttach")));
            isOK = false;
		}
		//System.out.println("attachFileTo Exit: " + Runtime.getRuntime().freeMemory());
		return isOK;
	}
	
	/**
	 * @param session
	 * @param recId
	 * @return record with key recId
	 */
	protected AttachmentOwnerIFace<?> getAttachmentOwner(DataProviderSessionIFace sessionArg, Integer recId)
	{
		return (AttachmentOwnerIFace<?>)sessionArg.get(fnParser.getAttachmentOwnerClass(), recId);
	}
	
	/**
	 * @param args
	 */
	    public static void main(String[] args)
	    {
	        log.debug("********* Current ["+(new File(".").getAbsolutePath())+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        // This is for Windows and Exe4J, turn the args into System Properties
	 
	        // Set App Name, MUST be done very first thing!
	        //setAppName("SchemaExporter");  //$NON-NLS-1$
	        setAppName("Specify");  //$NON-NLS-1$

	        setEmbeddedDBPath(getDefaultEmbeddedDBPath()); // on the local machine
	        
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
	            setDefaultWorkingPath(appDir);
	        }
	        
	        String appdatadir = System.getProperty("appdatadir");
	        if (StringUtils.isNotEmpty(appdatadir))
	        {
	            setBaseAppDataDir(appdatadir);
	        }
	        
	        // For Debugging Only 
	        //System.setProperty("mobile", "true");
	        //System.setProperty("embedded", "true");
	        
	        String mobile = System.getProperty("mobile");
	        if (StringUtils.isNotEmpty(mobile))
	        {
	            setMobile(true);
	        }
	        
	        String embeddedStr = System.getProperty("embedded");
	        if (StringUtils.isNotEmpty(embeddedStr))
	        {
	            setEmbedded(true);
	        }
	        
	        String embeddeddbdir = System.getProperty("embeddeddbdir");
	        if (StringUtils.isNotEmpty(embeddeddbdir))
	        {
	            setEmbeddedDBPath(embeddeddbdir);
	        } else
	        {
	            setEmbeddedDBPath(getDefaultEmbeddedDBPath()); // on the local machine
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
	        System.setProperty(GenericGUIDGeneratorFactory.factoryName,     "edu.ku.brc.specify.config.SpecifyGUIDGeneratorFactory");
	        
	        final AppPreferences localPrefs = AppPreferences.getLocalPrefs();
	        localPrefs.setDirPath(getAppDataDir());
	        adjustLocaleFromPrefs();
	    	final String iRepPrefDir = localPrefs.getDirPath(); 
	        int mark = iRepPrefDir.lastIndexOf(getAppName(), iRepPrefDir.length());
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
	                String nameAndTitle = getResourceString("BatchAttachFiles.AppTitle"); // I18N
	                setRelease(true);
	                UIHelper.doLogin(usrPwdProvider, true, false, false, new BatchAttachLauncher(), Specify.getLargeIconName(), nameAndTitle, nameAndTitle, "SpecifyWhite32", "login"); // true
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
            setResourceLocale(prefLocale);
        }
        
        try
        {
            ResourceBundle.getBundle("resources", Locale.getDefault()); //$NON-NLS-1$
            
        } catch (MissingResourceException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(MainFrameSpecify.class, ex);
            Locale.setDefault(Locale.ENGLISH);
            setResourceLocale(Locale.ENGLISH);
        }
        
    }
}
