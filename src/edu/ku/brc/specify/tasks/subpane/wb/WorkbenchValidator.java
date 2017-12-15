/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.DB;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadData;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadField;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMappingDef;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTableInvalidValue;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploaderException;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.WorkbenchUploadMapper;
import edu.ku.brc.ui.UnhandledExceptionDialog;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 *A class that serves as link between a workbench and the database.
 *It handles validation, lookups, and other tasks necessary to provide
 *feedback about the 'upload' status of a workbench while it is being edited.
 */
public class WorkbenchValidator 
{
    protected static final Logger     log = Logger.getLogger(WorkbenchValidator.class);

    protected final WorkbenchPaneSS wbPane;
	protected final Workbench workbench;
	protected final Uploader uploader;
	
	/**
	 * @param wbPane
	 * @throws WorkbenchValidatorException
	 * @throws UploaderException
	 */
	public WorkbenchValidator(WorkbenchPaneSS wbPane) throws WorkbenchValidatorException, UploaderException
	{
		this.wbPane = wbPane;
		this.workbench = wbPane.getWorkbench();
		this.uploader = createUploader();
	}
	
	/**
	 * @param wb
	 * @throws WorkbenchValidatorException
	 * @throws UploaderException
	 */
	public WorkbenchValidator(Workbench wb ) throws WorkbenchValidatorException, UploaderException
	{
		this.wbPane = null;
		this.workbench = wb;
		this.uploader = createUploader();
	}
	/**
	 * @return an uploader for wbPane.
	 */
	protected Uploader createUploader() throws WorkbenchValidatorException, UploaderException {
		WorkbenchUploadMapper importMapper = new WorkbenchUploadMapper(workbench
                .getWorkbenchTemplate());
		Vector<UploadMappingDef> maps = importMapper.getImporterMapping();
        try {
        	DB db = new DB();
        	Uploader result = new Uploader(db, new UploadData(maps, workbench.getWorkbenchRowsAsList()), wbPane, workbench, workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems(), true);
        	Vector<UploadMessage> structureErrors = result.verifyUploadability();
        	if (structureErrors.size() > 0) {
        		throw new WorkbenchValidatorException(structureErrors);
        	}
        	return result;
        } catch (Exception ex) {
        	ex.printStackTrace();
        	throw new UploaderException(ex);
        }
	}
	
	/**
	 * @param row
	 * @param col
	 * 
	 * Perform validation after a cell is edited.
	 */
	public Pair<List<UploadTableInvalidValue>, List<Pair<UploadField, Object>>>  endCellEdit(int row, int col, boolean validate, boolean checkEdits) {
		List<UploadTableInvalidValue> invalids = new ArrayList<UploadTableInvalidValue>();
		List<Pair<UploadField, Object>> edits = new ArrayList<Pair<UploadField, Object>>();
		if (validate) invalids.addAll(uploader.validateData(row, col));
		if (checkEdits) {
			try {
				edits.addAll(uploader.checkChangedData(row, false));
			} catch (Exception ex) {
	            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
	            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchPaneSS.class, ex);
	            log.error("******* " + ex);
	            ex.printStackTrace();
	            UnhandledExceptionDialog dlg = new UnhandledExceptionDialog(ex);
	            dlg.setVisible(true);
			}
		}
		return new Pair<List<UploadTableInvalidValue>, List<Pair<UploadField, Object>>>(invalids, edits);
	}
	
	
	/**
	 * @param row
	 * @param col
	 * 
	 * Get ready - lookup valid values, etc - for a cell edit.
	 */
	public void startCellEdit(int row, int col)
	{
		
	}
	
	/**
	 * @author timo
	 *
	 */
	@SuppressWarnings("serial")
	public class WorkbenchValidatorException extends Exception
	{
		protected final Vector<UploadMessage> structureErrors;
		
		/**
		 * @param structureErrors
		 */
		public WorkbenchValidatorException(Vector<UploadMessage> structureErrors)
		{
			super();
			this.structureErrors = structureErrors;			
		}
		
		/**
		 * @return the structureErrors
		 */
		public Vector<UploadMessage> getStructureErrors()
		{
			return structureErrors;
		}
	}
	
	/**
	 * @return the uploader
	 */
	public Uploader getUploader()
	{
		return uploader;
	}
}
