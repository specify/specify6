/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.tools.export.CmdAppBase;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class UploadCmdLine extends CmdAppBase {

	private static String[] myargkeys = {"-b","-c","-x","-k", "-n"};
	
	private String wbId = null;
	private Workbench wb = null;
	private String collection = null; 
	private boolean doCommit = true;
	private boolean doMatch = true;
	private String multipleMatchAction = "skip";
	
	/**
	 * 
	 */
	public UploadCmdLine() {
		super(myargkeys);
	}
	
	/**
	 * @return
	 */
	private boolean getWb() {
		wb = WorkbenchTask.loadWorkbench(Integer.valueOf(wbId), null, true);
		return wb != null;
	}
	
	/**
	 * @return
	 */
	private boolean uploadIt() {
        WorkbenchUploadMapper importMapper = new WorkbenchUploadMapper(wb.getWorkbenchTemplate());
        try {
        	Vector<UploadMappingDef> maps = importMapper.getImporterMapping();
        	DB db = new DB();
        	Uploader u =  new Uploader(db, new UploadData(maps, wb.getWorkbenchRowsAsList()), null, wb, 
				wb.getWorkbenchTemplate().getWorkbenchTemplateMappingItems(), false);
        	return u.uploadItSansUI(doCommit, doMatch, multipleMatchAction);
        } catch (Exception ex) {
        	ex.printStackTrace();
        	return false;
        }
	}
	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tools.export.CmdAppBase#checkArg(edu.ku.brc.util.Pair)
	 */
	@Override
	protected String checkArg(Pair<String, String> arg) {
		if (arg.getFirst().equals("-n")) {
			if (arg.getSecond() != null && !("skip".equalsIgnoreCase(arg.getSecond())  || "new".equalsIgnoreCase(arg.getSecond())
					|| "pick".equalsIgnoreCase(arg.getSecond()))) {
				return "-n must be 'skip', 'pick', or 'new'";
			} else {
				return "";
			}
		} else {
			return super.checkArg(arg);
		}
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tools.export.CmdAppBase#setMembers()
	 */
	@Override
	protected void setMembers() throws Exception {
		super.setMembers();
		wbId = getArg("-b");
		collection = getArg("-c");
		String x = getArg("-x");
		doCommit = x == null || "true".equalsIgnoreCase(x);
		x = getArg("-k");
		doMatch = x == null || "true".equalsIgnoreCase(x);
		x = getArg("-n");
		multipleMatchAction =  x == null ? "skip" : x;
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tools.export.CmdAppBase#retrieveCollectionName()
	 */
	@Override
	protected boolean retrieveCollectionName() {
		collectionName = collection;
		return collectionName != null;
	}

	@Override
	protected String getLogInitText(String[] args) {
		// suppress args in log so passwords are compromised
		return String.format(UIRegistry.getResourceString("ExportCmdLine.LogInitTxt"), "Suppressed");
	}

	@Override
	protected String getTimestamp() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dateFormat.format(new Date());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("pid = " + new File("/proc/self").getCanonicalFile().getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		UploadCmdLine ucl = new UploadCmdLine();
		try {
			try {
				try {
					ucl.readArgs(args);

					String argErr = ucl.checkArgs();
					if (StringUtils.isNotEmpty(argErr)) {
						ucl.out(argErr); // currently the same as System.out until setMembers() is called.
						System.exit(1);
					}

					ucl.setMembers();
					if (!ucl.doCommit) ucl.out("Validating only. Will not commit.");
					ucl.initLog(args);
					ucl.setupPrefs();
					ucl.loadDrivers();

					if (ucl.needsMasterKey()) {
						if (ucl.hasMasterKey()) {
							//ecl.out("Master key set");
							ucl.out(UIRegistry.getResourceString("ExportCmdLine.MasterKeySet"));
						} else {
							//throw new Exception("Master Key not set");
							throw new Exception(UIRegistry.getResourceString("ExportCmdLine.MasterKeyNotSet"));
						}
					}
					if (ucl.getMaster()) {
						//ecl.out("Got master");
						ucl.out(UIRegistry.getResourceString("ExportCmdLine.GotMaster"));
					} else {
						//throw new Exception("Got not the master");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.GotNotMaster"));
					}
					if (ucl.login()) {
						//ecl.out("logged in");
						ucl.out(UIRegistry.getResourceString("ExportCmdLine.LoggedIn"));
					} else {
						//throw new Exception("Login failed.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.LoginFailed"));
					}
					if (ucl.checkVersion()) {
						//ecl.out("Versions OK");
						ucl.out(UIRegistry.getResourceString("ExportCmdLine.VersionsOK"));
					} else {
						//throw new Exception("Schema or application version mismatch.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.VersionsMismatched"));
					}
                	//TaskMgr.register(new WorkbenchTask(), false);
					if (ucl.goodUser()) {
						//ecl.out("good user");
						ucl.out(UIRegistry.getResourceString("ExportCmdLine.GoodUser"));
					} else {
						//throw new Exception("user is not a manager.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.UserNotAMgr"));
					}
					if (ucl.retrieveCollectionName()) {
						//ecl.out("got collection name");
						ucl.out(UIRegistry.getResourceString("ExportCmdLine.GotCollectionName"));
					} else {
						//throw new Exception("couldn't find collection name.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.CouldNotFindCollectionName"));
					}
					if (ucl.setContext()) {
						//ecl.out("context established");
						ucl.out(UIRegistry.getResourceString("ExportCmdLine.ContextEstablished"));
					} else {
						//throw new Exception("unable to establish context.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.CouldNotEstablishContext"));
					}
					if (ucl.getWb()) {
						ucl.out(UIRegistry.getResourceString("UploadCmdLine.LoadedWB"));
					} else {
						throw new Exception(UIRegistry.getResourceString("UploadCmdLine.CouldNotLoadWB"));
					}
					if (ucl.uploadIt()) {
						ucl.out(UIRegistry.getResourceString("UploadCmdLine.Uploaded"));
					} else {
						throw new Exception(UIRegistry.getResourceString("UploadCmdLine.UploadFailed"));
					}
					ucl.setSuccess(true);
				} catch (Exception ex) {
					ucl.setSuccess(false);
					ex.printStackTrace();
					ucl.out(ex.getLocalizedMessage());
				}
			} finally {
				ucl.exitLog();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}

}
