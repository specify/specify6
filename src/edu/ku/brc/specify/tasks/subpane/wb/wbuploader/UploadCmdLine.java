/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.tools.export.CmdAppBase;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timo
 *
 */
public class UploadCmdLine extends CmdAppBase {

	private static String[] myargkeys = {"-b","-c"};
	
	private String wbId = null;
	private Workbench wb = null;
	private String collection = null; 
	
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
        	return u.uploadItSansUI();
        } catch (Exception ex) {
        	ex.printStackTrace();
        	return false;
        }
	}
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tools.export.CmdAppBase#setMembers()
	 */
	protected void setMembers() throws Exception {
		super.setMembers();
		wbId = getArg("-b");
		collection = getArg("-c");
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tools.export.CmdAppBase#retrieveCollectionName()
	 */
	@Override
	protected boolean retrieveCollectionName() {
		collectionName = collection;
		return collectionName != null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
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
					ucl.initLog(args);
					ucl.setupPrefs();
					ucl.loadDrivers();
					
					if (ucl.hasMasterKey()) {
						//ecl.out("Master key set");
						ucl.out(UIRegistry.getResourceString("ExportCmdLine.MasterKeySet"));
					} else {
						//throw new Exception("Master Key not set");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.MasterKeyNotSet"));
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
