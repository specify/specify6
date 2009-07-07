/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.HashMap;

import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.subpane.JRConnectionFieldDef;
import edu.ku.brc.specify.tasks.subpane.SpJRIReportConnection;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author Administrator
 *
 *IReport connection for Workbenches.
 */
@SuppressWarnings("unchecked")
public class WBJRIReportConnection extends SpJRIReportConnection
{
	final Workbench workbench;
	
	/**
	 * @param workbench - fully loaded workbench - forceLoad() must have been called.
	 */
	public WBJRIReportConnection(final Workbench workbench)
	{
		super(workbench.getName());
		this.workbench = workbench;
	}
	
	/* (non-Javadoc)
	 * @see it.businesslogic.ireport.IReportConnection#getDescription()
	 */
	@Override
	public String getDescription()
	{
        return UIRegistry.getResourceString("WBJRIReportConnection.WB_IREPORT_CONNECTION");
	}

	/* (non-Javadoc)
	 * @see it.businesslogic.ireport.IReportConnection#getProperties()
	 */
	@Override
	public HashMap getProperties()
	{
        HashMap map = new HashMap();
        for (int i=0; i< fields.size(); ++i)
        {
            map.put("COLUMN_" + i, fields.get(i).getFldTitle());
        }
        return map;
	}

	/* (non-Javadoc)
	 * @see it.businesslogic.ireport.IReportConnection#loadProperties(java.util.HashMap)
	 */
	@Override
	public void loadProperties(HashMap map)
	{
		fields.clear();
		WorkbenchTemplate wbt = workbench.getWorkbenchTemplate();
		for (WorkbenchTemplateMappingItem wbtmi : wbt.getWorkbenchTemplateMappingItems())
		{
			fields.add(new JRConnectionFieldDef(wbtmi.getTableName() + "." + wbtmi.getFieldName(), 
            		wbtmi.getCaption(), String.class));
		}
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.SpJRIReportConnection#getSpObject()
	 */
	@Override
	public DataModelObjBase getSpObject()
	{
		return workbench;
	}
}
