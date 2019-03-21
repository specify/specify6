/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import edu.ku.brc.af.auth.SecurityMgr;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import edu.ku.brc.af.auth.JaasContext;
import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.config.SpecifyAppPrefs;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.tools.ireportspecify.MainFrameSpecify;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class CmdAppBase {
    protected static final String SCHEMA_VERSION_FILENAME = "schema_version.xml";

    private static String[] argkeys = {"-u", "-p", "-d", "-m", "-l", "-h", "-o", "-w", "-U", "-P"};
	
    protected List<Pair<String, String>> argList;
	
    protected String userName;
	protected String password;
	protected String dbName;
	protected String outputName;
	protected String hostName;
	protected String workingPath = ".";
	protected Pair<String, String> master;
	protected FileWriter out;
	boolean success = false;
    protected Vector<DatabaseDriverInfo> dbDrivers      = new Vector<DatabaseDriverInfo>();
    protected int dbDriverIdx;
	protected String collectionName;
	protected JaasContext jaasContext;

	/**
	 * @param argkeys
	 */
	public CmdAppBase(String[] akeys) {
		String[] as = new String[argkeys.length + akeys.length];
		for (int ak=0; ak < argkeys.length; ak++) {
			as[ak] = argkeys[ak];
		}
		for (int ak=0; ak < akeys.length; ak++) {
			as[ak + argkeys.length] = akeys[ak];
		}
		argList = buildArgList(as);
		dbDriverIdx = 0;
		hostName = "localhost";
	}
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
     * @return
     */
    public String getDBSchemaVersionFromXML()
    {
        String dbVersion = null;
        Element root;
        try
        {
            root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath(SCHEMA_VERSION_FILENAME)));//$NON-NLS-1$
            if (root != null)
            {
                dbVersion = ((Element)root).getTextTrim();
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return dbVersion;
    }

	/**
	 * @return
	 */
	protected boolean checkVersion() {
        String schemaVersion = getDBSchemaVersionFromXML();
        String appVersion = UIRegistry.getAppVersion();
        String schemaVersionFromDb = BasicSQLUtils.querySingleObj("select SchemaVersion from spversion");
        String appVersionFromDb = BasicSQLUtils.querySingleObj("select AppVersion from spversion");
        return (schemaVersion.equals(schemaVersionFromDb) && appVersion.equals(appVersionFromDb));
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
	 * @param arg
	 * @return "" if arg checks out, else err msg
	 */
	protected String checkArg(Pair<String, String> arg) {
		return "";
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
		System.setProperty(SecurityMgr.factoryName, "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");
		UIRegistry.setAppName("Specify");  //$NON-NLS-1$
        UIRegistry.setDefaultWorkingPath(this.workingPath);
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

	protected boolean needsMasterKey() {
		return !(master != null && master.getFirst() != null && master.getSecond()!= null);
	}
	/**
	 * @return
	 * @throws Exception
	 */
	protected boolean getMaster() throws Exception {
        UserAndMasterPasswordMgr.getInstance().set(userName, password, dbName);
		if (!needsMasterKey()) {
			UserAndMasterPasswordMgr.getInstance().setUserNamePasswordForDB(master.first, master.second);
			return true;
		}
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

	public boolean isSuccess() {
		return success;
	}

	protected DatabaseDriverInfo buildDefaultDriverInfo() {
		DatabaseDriverInfo result = new DatabaseDriverInfo("MySQL", "com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQL5InnoDBDialect", false, "3306");
		result.addFormat(DatabaseDriverInfo.ConnectionType.Opensys, "jdbc:mysql://SERVER:PORT/");
		result.addFormat(DatabaseDriverInfo.ConnectionType.Open, "jdbc:mysql://SERVER:PORT/DATABASE?characterEncoding=UTF-8&autoReconnect=true");
		return result;
	}

	/**
	 * 
	 */
	public void loadDrivers() {
		dbDrivers = DatabaseDriverInfo.getDriversList();
	}
	
    /**
     * @return
     */
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
		boolean isPw = false;
		for (String arg : args) {
			argStr += " " + (isPw ? "********" : arg);
			if (isPw) {
				isPw = false;
			} else if ("-p".equals(arg)) {
				isPw = true;
			}
		}
		return String.format(UIRegistry.getResourceString("ExportCmdLine.LogInitTxt"), argStr);
	}
	
	/**
	 * @return
	 */
	protected String getLogExitText() {
		return String.format(UIRegistry.getResourceString("ExportCmdLine.LogExitTxt"), (success ? "" : "UN-") + "successfully.");
	}
	
	/**
	 * @throws IOException
	 */
	protected void initLog(String[] args) throws IOException {
		out(getLogInitText(args));
	}
	
	/**
	 * @throws IOException
	 */
	protected void flushLog() throws IOException {
		if (out != null) {
			out.flush();
		}
	}
	/**
	 * @throws IOException
	 */
	protected void exitLog() throws IOException {
		out(getLogExitText());
		if (out != null) {
			out.close();
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
			this.jaasContext = new JaasContext();
			jaasLogin();
		}
		return result;
	}

	/**
	 * @return true if ContextManager initializes successfully for collection.
	 */
	protected boolean setContext() {
        Specify.setUpSystemProperties();
        System.setProperty(AppContextMgr.factoryName,  "edu.ku.brc.specify.tools.export.SpecifyExpCmdAppContextMgr");      // Needed by AppContextMgr //$NON-NLS-1$
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

		return true;
	}

	/**
	 * @return
	 */
	protected boolean retrieveCollectionName() {
		return false;
	}

    /**
     * @return
     */
    public boolean jaasLogin()
    {
        if (jaasContext != null)
        {
            return jaasContext.jaasLogin(
					userName,
					password,
					getConnectionStr(),
					dbDrivers.get(dbDriverIdx).getDriverClassName(),
					master.first,
					master.second
			);
        }

        return false;
    }

	/**
	 * @throws Exception
	 */
	protected void setMembers() throws Exception {
		userName = getArg("-u");
		password = getArg("-p");
		if (password == null) password = "";
		dbName = getArg("-d");
		outputName = getArg("-o");
		workingPath = getArg("-w");
		String logFile = getArg("-l");
		if (logFile != null) {
			openLog(logFile);
		}
		String host = getArg("-h");
		if (host != null) {
			hostName = host;
		}
		master = new Pair<>(getArg("-U"), getArg("-P"));
	}

}
