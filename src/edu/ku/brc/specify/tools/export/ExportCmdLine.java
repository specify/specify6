/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.auth.JaasContext;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.tasks.QueryTask;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace;
import edu.ku.brc.specify.tools.ireportspecify.MainFrameSpecify;
import edu.ku.brc.specify.tools.webportal.BuildSearchIndex2;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class ExportCmdLine {

	private static String[] argkeys = {"-u", "-p", "-d", "-m", "-a", "-l", "-h", "-o"};
	
	protected List<Pair<String, String>> argList;
	protected String userName;
	protected String password;
	protected String dbName;
	protected String mapping;
	protected String outputName;
	protected Integer mappingId = null;;
	protected SpExportSchemaMapping theMapping = null;
	protected String action;
	protected String hostName;
	protected Pair<String, String> master;
	protected String collectionName;
	protected JaasContext jaasContext;
	protected FileWriter out;
	boolean success = false;
    protected Vector<DatabaseDriverInfo> dbDrivers      = new Vector<DatabaseDriverInfo>();
    protected int dbDriverIdx;


	/**
	 * @param keys
	 * @return
	 */
	protected List<Pair<String, String>> buildArgList(String[] keys) {
		List<Pair<String, String>> result = new ArrayList<Pair<String,String>>();
		for (String key : keys) {
			result.add(new Pair<String, String>(key, null));
		}
		return result;
	}
	
	/**
	 * @param arg
	 * @return
	 */
	protected String checkArg(Pair<String, String> arg) {
		if (arg.getSecond() == null) {
			if (!arg.getFirst().equals("-l") && !arg.getFirst().equals("-h")) {
				return "missing argument: " + arg.getFirst();
			}
		}
		return "";
	}
	
	/**
	 * @param args
	 */
	protected void readArgs(String[] args) {
		for (Pair<String, String> argPair : argList) {
			argPair.setSecond(readArg(argPair.getFirst(), args));
		}
	}
	
	/**
	 * @return
	 */
	protected String checkArgs() {
		String result = "";
		for (Pair<String, String> arg : argList) {
			String err = checkArg(arg);
			if (StringUtils.isNotBlank(err)) {
				if (result.length() > 0) {
					result += "; ";
				}
				result += err;
			}
		}
		return result;
	}
	
    /**
     * 
     */
    protected void adjustLocaleFromPrefs()
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

    /**
     * @throws Exception
     */
    protected void setupPrefs() throws Exception {
		//Apparently this is correct...
        UIRegistry.setAppName("Specify");  //$NON-NLS-1$
        final AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        adjustLocaleFromPrefs();
        final String iRepPrefDir = localPrefs.getDirPath(); 
        int mark = iRepPrefDir.lastIndexOf(UIRegistry.getAppName(), iRepPrefDir.length());
        final String SpPrefDir = iRepPrefDir.substring(0, mark) + "Specify";
        AppPreferences.getLocalPrefs().flush();
        AppPreferences.getLocalPrefs().setDirPath(SpPrefDir);
        AppPreferences.getLocalPrefs().setProperties(null);
    }
    
	/**
	 * @return
	 * @throws Exception
	 */
	protected boolean hasMasterKey() throws Exception {
        UserAndMasterPasswordMgr.getInstance().set(userName, password, dbName);
        return UserAndMasterPasswordMgr.getInstance().hasMasterUsernameAndPassword();
	}
	
	/**
	 * @return
	 * @throws Exception
	 */
	protected boolean getMaster() throws Exception {
        UserAndMasterPasswordMgr.getInstance().set(userName, password, dbName);
        Pair<String, String> userpw = UserAndMasterPasswordMgr.getInstance().getUserNamePasswordForDB();
		if (userpw != null) {
			if (StringUtils.isNotBlank(userpw.getFirst()) && StringUtils.isNotBlank(userpw.getSecond())) {
				if (master == null) {
					master = new Pair<String, String>(null, null);
				}
				master.setFirst(userpw.getFirst());
				master.setSecond(userpw.getSecond());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return
	 */
	protected boolean goodUser() {
		String userType = BasicSQLUtils.querySingleObj("select UserType from specifyuser where `name` = '" + userName + "'");
		return "manager".equalsIgnoreCase(userType);
	}
	
	/**
	 * @return
	 */
	protected boolean retrieveMappingId() {
		mappingId = BasicSQLUtils.querySingleObj(
			"select SpExportSchemaMappingID from spexportschemamapping "
			+ " where MappingName = '" + mapping + "'");
		return mappingId != null;
	}

	/**
	 * @return
	 */
	protected boolean retrieveCollectionName() {
		collectionName = BasicSQLUtils.querySingleObj("select CollectionName from spexportschemamapping m inner join collection c" +
				" on c.CollectionID = m.CollectionMemberID");
		return collectionName != null;
	}
	
	/**
	 * @return
	 */
	protected SpExportSchemaMapping getTheMapping() {
		if (mappingId != null && theMapping == null) {
			DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
	        try {
	        	theMapping = session.get(SpExportSchemaMapping.class, mappingId);
	        	if (theMapping != null) {
	        		theMapping.forceLoad();
	        	}
	        }
	        finally {
	        	session.close();
	        }
		}
		return theMapping;
	}
	/**
	 * @return
	 */
	protected boolean mappingNeedsRebuild() {
        return ExportPanel.needsToBeRebuilt(getTheMapping());
	}
	
	/**
	 * @return
	 */
	protected MappingUpdateStatus getMappingUpdateStatus() {
		return ExportPanel.retrieveMappingStatus(getTheMapping());
	}
	
	/**
	 * @param st
	 * @return
	 * @throws Exception
	 */
	protected boolean updateMappingCache(MappingUpdateStatus st) throws Exception {
		boolean includeRecordIds = true;
		boolean useBulkLoad = false; 
		String bulkFileDir = null;
		QBDataSourceListenerIFace listener = null; 
		Connection conn = DBConnection.getInstance().getConnection();
		final long cacheRowCount = st.getTotalRecsChanged() - st.getRecsToDelete();
        return ExportPanel.updateInBackground(includeRecordIds, useBulkLoad, bulkFileDir, 
        		getTheMapping(), listener, conn, 
        		cacheRowCount);
	}
	/**
	 * @param argKey
	 * @param args
	 * @return
	 */
	protected String readArg(String argKey, String[] args) {
		for (int k = 0; k < args.length - 1; k+=2) {
			if (args[k].equals(argKey)) {
				return args[k+1];
			}
		}
		return null;
	}
	
	/**
	 * @param argKey
	 * @return
	 */
	protected String getArg(String argKey) {
		for (Pair<String, String> arg : argList) {
			if (arg.getFirst().equals(argKey)) {
				return arg.getSecond();
			}
		}
		return null;
	}
	
	/**
	 * @param success
	 */
	protected void setSuccess(boolean success) {
		this.success = success;
	}
	
	/**
	 * 
	 */
	public ExportCmdLine() {
		argList = buildArgList(argkeys);
		dbDrivers = DatabaseDriverInfo.getDriversList();
		dbDriverIdx = 0;
		hostName = "localhost";
	}
	
    protected String getConnectionStr() {
    	
    	return dbDrivers.get(dbDriverIdx).getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, 
    			hostName, dbName);
    }
    
	/**
	 * @param fileName
	 * @throws Exception
	 */
	protected void openLog(String fileName) throws Exception {
		FileWriter testOut = new FileWriter(new File(fileName), true);
		out = testOut;
	}
	
	/**
	 * @return
	 */
	protected String getTimestamp() {
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
	}
	
	/**
	 * @return
	 */
	protected String getLogInitText(String[] args) {
		String argStr = "";
		for (String arg : args) {
			argStr += " " + arg;
		}
		return "starting with args [" + argStr + "]...";
	}
	
	/**
	 * @return
	 */
	protected String getLogExitText() {
		return "...exiting " + (success ? "" : "UN-") + "successfully.";
	}
	
	/**
	 * @throws IOException
	 */
	protected void initLog(String[] args) throws IOException {
		out(getLogInitText(args));
	}
	
	protected void flushLog() throws IOException {
		out.flush();
	}
	/**
	 * @throws IOException
	 */
	protected void exitLog() throws IOException {
		out(getLogExitText());
		out.close();
	}
	
	/**
	 * @throws Exception
	 */
	protected void setMembers() throws Exception {
		userName = getArg("-u");
		password = getArg("-p");
		dbName = getArg("-d");
		mapping = getArg("-m");
		action = getArg("-a");
		outputName = getArg("-o");
		String logFile = getArg("-l");
		if (logFile != null) {
			openLog(logFile);
		}
		String host = getArg("-h");
		if (host != null) {
			hostName = host;
		}
	}
	
	/**
	 * @param line
	 * @throws IOException
	 */
	protected void out(String line) throws IOException {
		if (out != null) {
			out.append(getTimestamp() + ": " + line + "\n");
			out.flush();
		} else {
			System.out.println(getTimestamp() + ": " + line);
		}
	}
	
	/**
	 * @return
	 */
	protected boolean login() {
		boolean result =  UIHelper.tryLogin(dbDrivers.get(dbDriverIdx).getDriverClassName(), 
                dbDrivers.get(dbDriverIdx).getDialectClassName(),
                dbName, 
                getConnectionStr(), 
                master.getFirst(), 
                master.getSecond());
		if (result) {
	        //this.jaasContext = new JaasContext(); 
			//jaasLogin(); 
		}
		return result;
	}
	
    /**
     * @return
     */
    public boolean jaasLogin()
    {
        if (jaasContext != null)
        {
            return jaasContext.jaasLogin(userName,
            							 password,
            							 getConnectionStr(), 
            							 dbDrivers.get(dbDriverIdx).getDriverClassName(),
            							 master.first,
            							 master.second);
        }

        return false;
    }

	protected boolean setContext() {
        Specify.setUpSystemProperties();
        AppPreferences.shutdownRemotePrefs();
        
        AppContextMgr.CONTEXT_STATUS status = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).
        		setContext(dbName, userName, false, false, true, collectionName, false);
       // AppContextMgr.getInstance().
        SpecifyAppPrefs.initialPrefs();
        
		if (status == AppContextMgr.CONTEXT_STATUS.OK) {
			if (AppContextMgr.getInstance().getClassObject(Discipline.class) == null) {
				return false;
			}

		} else if (status == AppContextMgr.CONTEXT_STATUS.Error) {
			return false;
		}
		// ...end specify.restartApp snatch

		boolean canOpen = true;
		if (AppContextMgr.isSecurityOn()) {
			PermissionIFace permissions = SecurityMgr.getInstance()
					.getPermission("Task.ExportMappingTask");
			canOpen = permissions.canView();
		}
		if (canOpen) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @return
	 * @throws Exception
	 */
	public boolean processAction() throws Exception {
		if ("update".equals(action)) {
			return true; //update is required for any action and, currently, will already be done.
		} else {
			boolean result = false;
			out("performing " + action + "...");
			if ("ExportForWebPortal".equalsIgnoreCase(action)) {
				result = exportForWebPortal();
			} else if ("ExportToTabDelim".equalsIgnoreCase(action)) {
				result = exportToTabDelim();
			} else {
				//really oughta catch this when args are first read...
				throw new Exception("Unrecognized action: " + action);
			}
			if (result) {
				out("..." + action + " completed.");
			}
			return result;
		}
	}
	
	/**
	 * @return
	 * @throws Exception
	 */
	protected boolean exportForWebPortal() throws Exception {
		File f = new File(outputName);
        String zipFile = f.getPath();
        if (!zipFile.toLowerCase().endsWith(".zip"))
        {
             zipFile += ".zip";
        }
        final BuildSearchIndex2 bsi = new BuildSearchIndex2(
                getTheMapping(),
                zipFile,
                collectionName,
                ExportPanel.getAttachmentURL());
    	bsi.connect();
		return bsi.index(null);
	}
	
	/**
	 * @return
	 * @throws Exception
	 */
	protected boolean exportToTabDelim() throws Exception {
		return ExportToMySQLDB.exportRowsToTabDelimitedText(new File(outputName), null,
				ExportPanel.getCacheTableName(getTheMapping().getMappingName()));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExportCmdLine ecl = new ExportCmdLine();
		try {
			try {
				try {
					ecl.setupPrefs();

					ecl.readArgs(args);

					String argErr = ecl.checkArgs();
					if (StringUtils.isNotEmpty(argErr)) {
						ecl.out(argErr); // currently the same as System.out until setMembers() is called.
						System.exit(1);
					}

					ecl.setMembers();
					ecl.initLog(args);

					if (ecl.hasMasterKey()) {
						ecl.out("Master key set");
					} else {
						throw new Exception("Master Key not set");
					}
					if (ecl.getMaster()) {
						ecl.out("Got master");
					} else {
						throw new Exception("Got not the master");
					}
					if (ecl.login()) {
						ecl.out("logged in");
					} else {
						throw new Exception("Login failed.");
					}
                	TaskMgr.register(new QueryTask(), false);
					if (ecl.goodUser()) {
						ecl.out("good user");
					} else {
						throw new Exception("user is not a manager.");
					}
					if (ecl.retrieveMappingId()) {
						ecl.out("got mapping id");
					} else {
						throw new Exception("couldn't find mapping.");
					}
					if (ecl.retrieveCollectionName()) {
						ecl.out("got collection name");
					} else {
						throw new Exception("couldn't find collection name.");
					}
					if (ecl.setContext()) {
						ecl.out("context established");
					} else {
						throw new Exception("unable to establish context.");
					}
					if (!ecl.mappingNeedsRebuild()) {
						ecl.out("mapping can be processed");
					} else {
						throw new Exception("mapping must be rebuilt.");
					}
					MappingUpdateStatus st = ecl.getMappingUpdateStatus();
					if (st != null) {
						ecl.out("Got mapping status: Records deleted=" + st.getRecsToDelete() + ". Records added or updated=" + (st.getTotalRecsChanged() - st.getRecsToDelete()));
					} else {
						throw new Exception("failed to get mapping status.");
					}
					if (st.getTotalRecsChanged() != 0) {
						ecl.out("Updating cache...");
						ecl.flushLog();
						if (ecl.updateMappingCache(st)) {
							ecl.out("...updated cache.");
						} else {
							throw new Exception("failed to update cache.");
						}
					}
					if (!ecl.processAction()) {
						throw new Exception("action not completed.");
					}
					ecl.setSuccess(true);
				} catch (Exception ex) {
					ecl.setSuccess(false);
					ex.printStackTrace();
					ecl.out(ex.getLocalizedMessage());
				}
			} finally {
				ecl.exitLog();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}

}
