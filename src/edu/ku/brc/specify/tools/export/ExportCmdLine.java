/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import java.io.File;
import java.sql.Connection;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.tasks.QueryTask;
import edu.ku.brc.specify.tasks.StartUpTask;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace;
import edu.ku.brc.specify.tools.webportal.BuildSearchIndex2;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class ExportCmdLine extends CmdAppBase {


	private static String[] myargkeys = {"-a"};
	
	protected String mapping;
	protected Integer mappingId = null;;
	protected SpExportSchemaMapping theMapping = null;
	protected String action;


	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tools.export.CmdAppBase#checkArg(edu.ku.brc.util.Pair)
	 */
	protected String checkArg(Pair<String, String> arg) {
		if (arg.getSecond() == null) {
			if (arg.getFirst().equals("-o")) {
				if (!"update".equalsIgnoreCase(getArg("-a"))) {
					return String.format(UIRegistry.getResourceString("ExportCmdLine.MissingArgument"), arg.getFirst());
				}
			} else if (!arg.getFirst().equals("-l") && !arg.getFirst().equals("-h") && !arg.getFirst().equals("-w")) {
				return String.format(UIRegistry.getResourceString("ExportCmdLine.MissingArgument"), arg.getFirst());
			}
		}
		return "";
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

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tools.export.CmdAppBase#retrieveCollectionName()
	 */
	protected boolean retrieveCollectionName() {
		collectionName = BasicSQLUtils.querySingleObj("select CollectionName from spexportschemamapping m inner join collection c" +
				" on c.CollectionID = m.CollectionMemberID where m.SpExportSchemaMappingID=" + mappingId);
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
	 * 
	 */
	public ExportCmdLine() {
		super(myargkeys);
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tools.export.CmdAppBase#setMembers()
	 */
	protected void setMembers() throws Exception {
		super.setMembers();
		mapping = getArg("-m");
		action = getArg("-a");
	}
	
	
	/**
	 * @return
	 * @throws Exception
	 */
	public boolean processAction() throws Exception {
		if ("update".equalsIgnoreCase(action)) {
			return true; //update is required for any action and, currently, will already be done.
		} else {
			boolean result = false;
			//out("performing " + action + "...");
			out(String.format(UIRegistry.getResourceString("ExportCmdLine.PerformingAction"), action));
			if ("ExportForWebPortal".equalsIgnoreCase(action)) {
				result = exportForWebPortal();
			} else if ("ExportToTabDelim".equalsIgnoreCase(action)) {
				result = exportToTabDelim();
			} else {
				//really oughta catch this when args are first read...
				//throw new Exception("Unrecognized action: " + action);
				throw new Exception(String.format(UIRegistry.getResourceString("ExportCmdLine.UnrecognizedAction"), action));
			}
			if (result) {
				//out("..." + action + " completed.");
				out(String.format(UIRegistry.getResourceString("ExportCmdLine.ActionCompleted"), action));
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
        StartUpTask.configureAttachmentManager();
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
					ecl.readArgs(args);

					String argErr = ecl.checkArgs();
					if (StringUtils.isNotEmpty(argErr)) {
						ecl.out(argErr); // currently the same as System.out until setMembers() is called.
						System.exit(1);
					}

					ecl.setMembers();
					ecl.initLog(args);
					ecl.setupPrefs();
					ecl.loadDrivers();
					
					if (ecl.hasMasterKey()) {
						//ecl.out("Master key set");
						ecl.out(UIRegistry.getResourceString("ExportCmdLine.MasterKeySet"));
					} else {
						//throw new Exception("Master Key not set");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.MasterKeyNotSet"));
					}
					if (ecl.getMaster()) {
						//ecl.out("Got master");
						ecl.out(UIRegistry.getResourceString("ExportCmdLine.GotMaster"));
					} else {
						//throw new Exception("Got not the master");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.GotNotMaster"));
					}
					if (ecl.login()) {
						//ecl.out("logged in");
						ecl.out(UIRegistry.getResourceString("ExportCmdLine.LoggedIn"));
					} else {
						//throw new Exception("Login failed.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.LoginFailed"));
					}
					if (ecl.checkVersion()) {
						//ecl.out("Versions OK");
						ecl.out(UIRegistry.getResourceString("ExportCmdLine.VersionsOK"));
					} else {
						//throw new Exception("Schema or application version mismatch.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.VersionsMismatched"));
					}
                	TaskMgr.register(new QueryTask(), false);
					if (ecl.goodUser()) {
						//ecl.out("good user");
						ecl.out(UIRegistry.getResourceString("ExportCmdLine.GoodUser"));
					} else {
						//throw new Exception("user is not a manager.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.UserNotAMgr"));
					}
					if (ecl.retrieveMappingId()) {
						//ecl.out("got mapping id");
						ecl.out(UIRegistry.getResourceString("ExportCmdLine.GotMappingId"));
					} else {
						//throw new Exception("couldn't find mapping.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.CouldNotFindMapping"));
					}
					if (ecl.retrieveCollectionName()) {
						//ecl.out("got collection name");
						ecl.out(UIRegistry.getResourceString("ExportCmdLine.GotCollectionName"));
					} else {
						//throw new Exception("couldn't find collection name.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.CouldNotFindCollectionName"));
					}
					if (ecl.setContext()) {
						//ecl.out("context established");
						ecl.out(UIRegistry.getResourceString("ExportCmdLine.ContextEstablished"));
					} else {
						//throw new Exception("unable to establish context.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.CouldNotEstablishContext"));
					}
					if (!ecl.mappingNeedsRebuild()) {
						//ecl.out("mapping can be processed");
						ecl.out(UIRegistry.getResourceString("ExportCmdLine.CanProcessMapping"));
					} else {
						//throw new Exception("mapping must be rebuilt.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.MappingMustBeRebuilt"));
					}
					MappingUpdateStatus st = ecl.getMappingUpdateStatus();
					if (st != null) {
						//ecl.out("Got mapping status: Records deleted=" + st.getRecsToDelete() + ". Records added or updated=" + (st.getTotalRecsChanged() - st.getRecsToDelete()));
						ecl.out(String.format(UIRegistry.getResourceString("ExportCmdLine.MappingStatus"), st.getRecsToDelete(),(st.getTotalRecsChanged() - st.getRecsToDelete())));
						
					} else {
						//throw new Exception("failed to get mapping status.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.FailedToGetMappingStatus"));
					}
					if (st.getTotalRecsChanged() != 0) {
						//ecl.out("Updating cache...");
						ecl.out(UIRegistry.getResourceString("ExportCmdLine.UpdatingCache"));
						ecl.flushLog();
						if (ecl.updateMappingCache(st)) {
							//ecl.out("...updated cache.");
							ecl.out(UIRegistry.getResourceString("ExportCmdLine.UpdatedCache"));
						} else {
							//throw new Exception("failed to update cache.");
							throw new Exception(UIRegistry.getResourceString("ExportCmdLine.FailedToUpdateCache"));
						}
					}
					if (!ecl.processAction()) {
						//throw new Exception("action not completed.");
						throw new Exception(UIRegistry.getResourceString("ExportCmdLine.ActionNotCompleted"));
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
