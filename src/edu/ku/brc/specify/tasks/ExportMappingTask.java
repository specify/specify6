/**
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpExportSchema;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.tasks.subpane.qb.QueryBldrPane;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 * 
 */
public class ExportMappingTask extends QueryTask
{
	protected SpExportSchema exportSchema = null;
	protected SpExportSchemaMapping schemaMapping = null;
	protected final AtomicBoolean addingMapping = new AtomicBoolean(false);
	
	public ExportMappingTask()
	{
		super("ExportMappingTask",
				getResourceString("ExportMappingTask.TaskTitle"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.ku.brc.specify.tasks.QueryTask#addNewQCreators()
	 */
	@Override
	protected void addNewQCreators()
	{
		actionNavBox.add(NavBox.createBtnWithTT(
				getResourceString("ExportMappingTask.NewMapping"), "Query",
				getResourceString("ExportMappingTask.NewMappigTT"),
				IconManager.STD_ICON_SIZE, new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						addMapping();
					}
				}));
		actionNavBox.add(NavBox.createBtnWithTT(
				getResourceString("ExportMappingTask.ImportSchema"), "Query",
				getResourceString("ExportMappingTask.ImportSchemaTT"),
				IconManager.STD_ICON_SIZE, new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						importSchema();
					}
				}));
	}

	/**
	 * @return a list of all the mappings associated with the current
	 *         discipline.
	 */
	protected Vector<SpExportSchemaMapping> getMappings()
	{
		DataProviderSessionIFace session = DataProviderFactory.getInstance()
				.createSession();
		try
		{
			Vector<SpExportSchemaMapping> result = new Vector<SpExportSchemaMapping>();
			Integer disciplineId = AppContextMgr.getInstance().getClassObject(
					Discipline.class).getId();
			List<SpExportSchemaMapping> mappings = session
					.getDataList(SpExportSchemaMapping.class);
			for (SpExportSchemaMapping mapping : mappings)
			{
				if (mapping.getSpExportSchema().getDiscipline().getId().equals(
						disciplineId))
				{
					mapping.forceLoad();
					result.add(mapping);
				}
			}
			return result;
		} finally
		{
			session.close();
		}
	}

	/**
	 * @return prompts user to choose from list of existing export schemas.
	 */
	protected SpExportSchema chooseExportSchema()
	{
		ChooseFromListDlg<SpExportSchema> dlg = new ChooseFromListDlg<SpExportSchema>(
				(Frame) UIRegistry.getTopWindow(),
				UIRegistry
						.getResourceString("ExportSchemaMapEditor.ChooseSchemaTitle"),
				getExportSchemas());
		UIHelper.centerAndShow(dlg);
		return dlg.getSelectedObject();
	}

	/**
	 * @return list of export schemas for the current discipline
	 */
	protected List<SpExportSchema> getExportSchemas()
	{
		DataProviderSessionIFace session = DataProviderFactory.getInstance()
				.createSession();
		try
		{
			List<SpExportSchema> result = session.getDataList(
					SpExportSchema.class, "discipline", AppContextMgr
							.getInstance().getClassObject(Discipline.class));
			// forceLoad here to get it over with.
			// probably there will never be a lot of export schemas or schema
			// items.
			for (SpExportSchema schema : result)
			{
				schema.forceLoad();
			}
			return result;
		} finally
		{
			session.close();
		}
	}

	/**
	 * Creates a new Query Data Object.
	 * 
	 * @param tableInfo
	 *            the table information
	 * @return the query
	 */
	protected SpQuery createNewQueryDataObj()
	{
		DBTableInfo tableInfo = getTableInfo();
		SpQuery query = new SpQuery();
		query.initialize();
		query.setName(exportSchema.getSchemaName());
		query.setNamed(false);
		query.setContextTableId((short) tableInfo.getTableId());
		query.setContextName(tableInfo.getShortClassName());
		return query;
	}

	/**
	 * @param query
	 * @return a QueryBldrPane for query
	 */
	protected QueryBldrPane buildQb(final SpQuery query)
	{
		if (!addingMapping.get())
		{
			//determine schema info from query
			Vector<Object[]> esm = BasicSQLUtils.query("select distinct esm.spexportschemamappingid from spexportschemamapping esm "
					+ "inner join spexportschemaitemmapping esim on esim.spexportschemamappingid = "
					+ "esm.spexportschemamappingid inner join spqueryfield qf on qf.spqueryfieldid = esim.spqueryfieldid "
					+ "where qf.spqueryid = " + query.getId());
			Integer esmId = (Integer )esm.get(0)[0];
			DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
			try
			{
				schemaMapping = session.get(SpExportSchemaMapping.class, esmId);
				schemaMapping.forceLoad();
				exportSchema = schemaMapping.getSpExportSchema();
				exportSchema.forceLoad();
			}
			finally
			{
				session.close();
			}
		}
        return new QueryBldrPane(query.getName(), this, query, false, exportSchema, schemaMapping);
	}

	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getActionNavBoxTitle()
	 */
	@Override
	protected String getActionNavBoxTitle()
	{
		return getResourceString("ExportMappingTask.ActionNavBoxTitle");
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getQueryNavBoxTitle()
	 */
	@Override
	protected String getQueryNavBoxTitle()
	{
		return getResourceString("ExportMappingTask.MappingNavBoxTitle");
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getNewQbPane(edu.ku.brc.specify.datamodel.SpQuery)
	 */
	@Override
	protected QueryBldrPane getNewQbPane(SpQuery query)
	{
		return buildQb(query);
	}

	/**
	 * @return TableInfo for the current exportSchema
	 */
	protected DBTableInfo getTableInfo()
	{
		//XXX will probably need to support schemas that are NOT based on CO
		return DBTableIdMgr.getInstance().getInfoById(1);
	}

	/**
	 * 
	 * Prompts to choose ExportSchema and opens new mapping, closing currently
	 * open mapping if necessary.
	 */
	protected void addMapping()
	{
		UIRegistry.displayErrorDlg("add mapping?");
		if (queryBldrPane == null || queryBldrPane.aboutToShutdown())
		{
			SpExportSchema selectedSchema = chooseExportSchema();
			exportSchema = selectedSchema;
			schemaMapping = new SpExportSchemaMapping();
			schemaMapping.initialize();
			schemaMapping.setSpExportSchema(exportSchema);
			SpQuery query = createNewQueryDataObj();
			if (query != null)
			{
				addingMapping.set(true);
				try
				{
					editQuery(query);
				}
				finally
				{
					addingMapping.set(false);
				}
			}
		}
	}

	protected void importSchema()
	{
		UIRegistry.displayErrorDlg("Import schema?");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.ku.brc.specify.tasks.QueryTask#getQueriesForLoading(edu.ku.brc.dbsupport
	 * .DataProviderSessionIFace)
	 */
	@Override
	protected List<?> getQueriesForLoading(DataProviderSessionIFace session)
	{
		List<SpExportSchema> exportSchemas = getExportSchemas(session);
		Vector<SpQuery> result = new Vector<SpQuery>();
		for (SpExportSchema exportSchema : exportSchemas)
		{
			for (SpExportSchemaMapping mapping : exportSchema
					.getSpExportSchemaMappings())
			{
				result.add(mapping.getMappings().iterator().next()
						.getQueryField().getQuery());
			}
		}
		return result;
	}

	/**
	 * @return list of export schemas for the current discipline
	 */
	protected List<SpExportSchema> getExportSchemas(
			DataProviderSessionIFace session)
	{
		List<SpExportSchema> result = session.getDataList(SpExportSchema.class,
				"discipline", AppContextMgr.getInstance().getClassObject(
						Discipline.class));
		// forceLoad here to get it over with.
		// probably there will never be a lot of export schemas or schema items.
		for (SpExportSchema schema : result)
		{
			schema.forceLoad();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#isLoadableQuery(edu.ku.brc.specify.datamodel.SpQuery)
	 */
	@Override
	protected boolean isLoadableQuery(SpQuery query)
	{
		//assuming query was produced by this.getQueriesForLoading()
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#deleteThisQuery(edu.ku.brc.specify.datamodel.SpQuery, edu.ku.brc.dbsupport.DataProviderSessionIFace)
	 */
	@Override
	protected void deleteThisQuery(SpQuery query,
			DataProviderSessionIFace session) throws Exception
	{
		String hql = "from SpExportSchemaMapping sesm inner join sesm.mappings maps inner join maps.queryField qf inner join qf.query q where q.id = " + query.getId();
		QueryIFace q = session.createQuery(hql, false);
		Object x = q.list().get(0);
		session.delete(((Object[] )x)[0]);
		super.deleteThisQuery(query, session);
		
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getQueryType()
	 */
	@Override
	protected String getQueryType()
	{
		return "ExportMappingTask";
	}

	
}
