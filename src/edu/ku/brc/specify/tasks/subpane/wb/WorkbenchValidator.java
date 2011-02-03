/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.List;
import java.util.Vector;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.DB;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadData;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMappingDef;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTableInvalidValue;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploaderException;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.WorkbenchUploadMapper;

/**
 * @author timo
 *
 *A class that serves as link between a workbench and the database.
 *It handles validation, lookups, and other tasks necessary to provide
 *feedback about the 'upload' status of a workbench while it is being edited.
 */
public class WorkbenchValidator 
{
	protected final WorkbenchPaneSS wbPane;
	protected final Workbench workbench;
	protected final Uploader uploader;
	
	/**
	 * @param wbPane
	 */
	public WorkbenchValidator(WorkbenchPaneSS wbPane) throws WorkbenchValidatorException, UploaderException
	{
		this.wbPane = wbPane;
		this.workbench = wbPane.getWorkbench();
		this.uploader = createUploader();
	}
	
	public WorkbenchValidator(Workbench wb ) throws WorkbenchValidatorException, UploaderException
	{
		this.wbPane = null;
		this.workbench = wb;
		this.uploader = createUploader();
	}
	/**
	 * @return an uploader for wbPane.
	 */
	protected Uploader createUploader() throws WorkbenchValidatorException, UploaderException
	{
		WorkbenchUploadMapper importMapper = new WorkbenchUploadMapper(workbench
                .getWorkbenchTemplate());
		Vector<UploadMappingDef> maps = importMapper.getImporterMapping();
        try
        {
        	DB db = new DB();
        	Uploader result = new Uploader(db, new UploadData(maps, workbench.getWorkbenchRowsAsList()), wbPane, workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems(), true);
        	Vector<UploadMessage> structureErrors = result.verifyUploadability();
        	if (structureErrors.size() > 0) 
        	{ 
        		throw new WorkbenchValidatorException(structureErrors);
        	}
        	return result;
        } catch (Exception ex)
        {
        	throw new UploaderException(ex);
        }
	}
	
	/**
	 * @param row
	 * @param col
	 * 
	 * Perform validation after a cell is edited.
	 */
	public List<UploadTableInvalidValue> endCellEdit(int row, int col)
	{
		Vector<UploadTableInvalidValue> issues = uploader.validateData(row, col);
//		if (issues.size() > 0)
//		{
//			for (UploadTableInvalidValue i : issues)
//			{
//				System.out.println(i.getMsg());
//			}
//		}
		return issues;
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
	
	public Uploader getUploader()
	{
		return uploader;
	}
}
