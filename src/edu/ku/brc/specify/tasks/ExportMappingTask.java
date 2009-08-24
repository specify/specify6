/**
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.helpers.UIFileFilter;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpExportSchema;
import edu.ku.brc.specify.datamodel.SpExportSchemaItem;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.tasks.subpane.qb.QueryBldrPane;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 * 
 */
public class ExportMappingTask extends QueryTask
{
	protected SpExportSchema		exportSchema	= null;
	protected SpExportSchemaMapping	schemaMapping	= null;
	protected final AtomicBoolean	addingMapping	= new AtomicBoolean(false);
	
	protected static final String	DEF_IMP_PREF	= "ExportSchemaMapping.SchemaImportDir";
	
	public ExportMappingTask()
	{
		super("ExportMappingTask",
				getResourceString("ExportMappingTask.TaskTitle"));
	}

	
	
	@Override
	public List<ToolBarItemDesc> getToolBarItems() 
	{
		return null;
	}



	@Override
	public List<MenuItemDesc> getMenuItems() 
	{
	       String menuDesc = "Specify.SYSTEM_MENU";
	        
	        menuItems = new Vector<MenuItemDesc>();
	        
	        if (permissions == null || permissions.canView())
	        {
	            String    menuTitle = "ExportMappingTask.ExMapMenu"; //$NON-NLS-1$
	            String    mneu      = "ExportMappingTask.ExMapMneu"; //$NON-NLS-1$
	            String    desc      = "ExportMappingTask.ExMapDesc"; //$NON-NLS-1$
	            JMenuItem mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
	            mi.addActionListener(new ActionListener()
	            {
	                public void actionPerformed(ActionEvent ae)
	                {
	                    ExportMappingTask.this.requestContext();
	                }
	            });
	            MenuItemDesc rsMI = new MenuItemDesc(mi, menuDesc);
	            rsMI.setPosition(MenuItemDesc.Position.After);
	            menuItems.add(rsMI);
	        }
	        return menuItems;

	}



	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#getIcon(int)
	 */
	@Override
	public ImageIcon getIcon(int size)
	{
        IconManager.IconSize iSize = IconManager.IconSize.Std16;
        if (size != Taskable.StdIcon16)
        {
            for (IconManager.IconSize ic : IconManager.IconSize.values())
            {
                if (ic.size() == size)
                {
                    iSize = ic;
                    break;
                }
            }
        }
        //return IconManager.getIcon("SystemSetup", iSize);
        return IconManager.getIcon("Export16", iSize);
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
		query.setName(exportSchema.getSchemaName() + exportSchema.getVersion());
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

    /**
     * Returns a path from the prefs and if it isn't valid then it return the User's Home Directory.
     * @param prefKey the Preferences key to look up
     * @return the path as a string
     */
    public static  String getDefaultSchemaImportDir()
    {
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        String path = localPrefs.get(DEF_IMP_PREF, null);
        if (path != null)
        {
        	File pathDir = new File(path);
        	if (pathDir.exists() && pathDir.isDirectory())
        	{
        		return path;
        	}
        }
        return UIRegistry.getDefaultWorkingPath() + File.separator + "config";
    }

	protected void importSchema()
	{
        JFileChooser chooser = new JFileChooser(getDefaultSchemaImportDir());
        chooser.setDialogTitle(getResourceString("ExportMappingTask.IMPORT_SCHEMADEF_TITLE"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new UIFileFilter("xsd", getResourceString("ExportMappingTask.SCHEMA_DEFS")));
        
        if (chooser.showOpenDialog(UIRegistry.get(UIRegistry.FRAME)) != JFileChooser.APPROVE_OPTION)
        {
            UIRegistry.getStatusBar().setText("");
            return;
        }

        File file = chooser.getSelectedFile();
        if (file == null)
        {
            UIRegistry.getStatusBar().setText(getResourceString("ExportMappingTask.NO_FILE"));
            return;
        }
        
        String path = chooser.getCurrentDirectory().getPath();
        //String path = FilenameUtils.getPath(file.getPath());
        if (StringUtils.isNotEmpty(path))
        {
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            localPrefs.put(DEF_IMP_PREF, path);
        }
        
		if (importSchemaDefinition(file))
		{
			UIRegistry.displayInfoMsgDlgLocalized("ExportMappingTask.SchemaImportSuccess", (Object[] )null);
		}
		else
		{
			UIRegistry.displayInfoMsgDlgLocalized("ExportMappingTask.SchemaImportFailure", (Object[] )null);
		}
		//importSchemaDefinition(new File("C:/darwincoreWithDiGIRv1.3.xsd"));
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#registerServices()
	 */
	@Override
	protected void registerServices()
	{
		//if the query task is present there is no need to do anything here
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
		session.delete(getMappingForQuery(query, session));
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

	/**
	 * @param query
	 * @param session
	 * @return 
	 * @throws Exception
	 */
	public static SpExportSchemaMapping getMappingForQuery(SpQuery query, DataProviderSessionIFace session) throws Exception
	{
		DataProviderSessionIFace theSession = session;
		boolean createSession = theSession == null;
		try
		{
			if (createSession)
			{
				theSession = DataProviderFactory.getInstance().createSession();
			}
			String hql = "from SpExportSchemaMapping sesm inner join sesm.mappings maps inner join maps.queryField qf inner join qf.query q where q.id = " + query.getId();
			QueryIFace q = theSession.createQuery(hql, false);
			if (q.list().size() == 0)
			{
				return null;
			}
			Object x = q.list().get(0);
			return (SpExportSchemaMapping )((Object[] )x)[0];
		}
		finally
		{
			if (createSession)
			{
				theSession.close();
			}
		}
	}
	
	/**
	 * @param xsdFile
	 * @return true if the schema was imported
	 * 
	 * Reads xsdFile and adds record to SpExportSchema describing the schema 
	 * and adds a record to SpExportSchemaMappings for each concept in the schema.
	 */
	protected boolean importSchemaDefinition(File xsdFile)
	{
		Element xsd = null;
		try
		{
			xsd = XMLHelper.readFileToDOM4J(xsdFile);
		}
		catch (Exception ex)
		{
			UIRegistry.displayErrorDlg(ex.getLocalizedMessage()); //XXX i18n
			return false;
		}
		if (xsd != null)
		{
			PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, p, 2dlu, p, 5dlu", "p, 2dlu, p, 2dlu, p"));
			CellConstraints cc = new CellConstraints();
			JLabel lbl = new JLabel(getResourceString("ExportMappingTask.SchemaDescTitle"));
			lbl.setHorizontalAlignment(SwingConstants.RIGHT);
			pb.add(lbl, cc.xy(2, 1));
			lbl = new JLabel(getResourceString("ExportMappingTask.SchemaTitleTitle"));
			lbl.setHorizontalAlignment(SwingConstants.RIGHT);
			pb.add(lbl, cc.xy(2, 3));
			lbl = new JLabel(getResourceString("ExportMappingTask.SchemaVersionTitle"));
			lbl.setHorizontalAlignment(SwingConstants.RIGHT);
			pb.add(lbl, cc.xy(2, 5));
			JTextField namespace = new JTextField(xsd.attributeValue("targetNamespace"));
			namespace.setEditable(false);
			pb.add(namespace, cc.xy(4, 1));
			JTextField title = new JTextField();
			pb.add(title, cc.xy(4, 3));
			JTextField version = new JTextField();
			pb.add(version, cc.xy(4, 5));
			CustomDialog cd = new CustomDialog((Frame )UIRegistry.get(UIRegistry.FRAME), 
					UIRegistry.getResourceString("ExportMappingTask.SchemaInfoTitle"), true,
					CustomDialog.OKCANCEL, pb.getPanel());
			
			boolean tryAgain = true;
			while (tryAgain)
			{
				UIHelper.centerAndShow(cd);
				if (!cd.isCancelled())
				{
					tryAgain = StringUtils.isBlank(title.getText()) || StringUtils.isBlank(version.getText());
					if (tryAgain)
					{
						UIRegistry.displayInfoMsgDlgLocalized("ExportMappingTask.FillAllFlds", (Object[] )null);
					}
				}
				else
				{
					return false;
				}
			}
			
			boolean doRollback = false;
			DataProviderSessionIFace session = null;
			try 
			{
				SpExportSchema schema = new SpExportSchema();
				schema.initialize();
				//XXX possibly need ui here for user to set version or remarks???
				schema.setSchemaName(title.getText());
				schema.setSchemaVersion(version.getText());
				schema.setDescription(xsd.attributeValue("targetNamespace"));
				schema.setDiscipline(AppContextMgr.getInstance().getClassObject(Discipline.class));
				for (Object itemObj : getNodesForDef(xsd))
				{
					SpExportSchemaItem item = createSchemaItem((Element ) itemObj);
					item.setSpExportSchema(schema);
					schema.getSpExportSchemaItems().add(item);
				}
				session = DataProviderFactory.getInstance().createSession();
				session.beginTransaction();
				doRollback = true;
				session.save(schema);
				session.commit();
				doRollback = false;
				return true;
			}
			catch (Exception ex)
			{
				if (doRollback)
				{
					session.rollback();
				}
				UIRegistry.displayErrorDlg(ex.getLocalizedMessage()); //XXX i18n
			}
			finally
			{
				if (session != null)
				{
					session.close();
				}
			}
		}
		UIRegistry.displayErrorDlg("Unable to import schema"); //XXX i18n
		return false;
	}
	
	@SuppressWarnings("unchecked")
	List<Object> getNodesForDef(final Element xsd)
	{
		List<Object> result = null;
		try
		{
			result = xsd.selectNodes("xsd:element");
		}
		catch (Exception ex)
		{
			System.out.println(ex.getLocalizedMessage());
		}
		if (result == null)
		{
			result = new LinkedList<Object>();
//			for (Object grp : xsd.selectNodes("xs:group"))
//			{
//				for (Object seq : ((Element )grp).selectNodes("xs:sequence"))
//				{
//					for (Object obj : ((Element )seq).selectNodes("xs:element"))
//					{
//						result.add(obj);
//					}
//				}
//			}
			for (Object obj : xsd.selectNodes("xs:element"))
			{
				if (((Element )obj).attributeValue("type", null) != null)
				{
					result.add(obj);
				}
			}
		}
		for (Object obj : result)
		{
			System.out.println(((Element )obj).attributeValue("name"));
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getPopupMenu()
	 */
	@Override
	public JPopupMenu getPopupMenu()
	{
		return null;
	}



	protected JPanel bldSchemaImportPane(String schemaNamespace)
	{
		PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, p, 2dlu, p, 5dlu", "p, 2dlu, p, 2dlu, p"));
		CellConstraints cc = new CellConstraints();
		JLabel lbl = new JLabel("Schema:");
		lbl.setHorizontalAlignment(SwingConstants.RIGHT);
		pb.add(lbl, cc.xy(2, 1));
		lbl = new JLabel("Schema Title:");
		lbl.setHorizontalAlignment(SwingConstants.RIGHT);
		pb.add(lbl, cc.xy(2, 3));
		lbl = new JLabel("Schema Version:");
		lbl.setHorizontalAlignment(SwingConstants.RIGHT);
		pb.add(lbl, cc.xy(2, 5));
		JTextField namespace = new JTextField(schemaNamespace);
		namespace.setEditable(false);
		pb.add(namespace, cc.xy(4, 1));
		JTextField title = new JTextField();
		pb.add(title, cc.xy(4, 3));
		JTextField version = new JTextField();
		pb.add(version, cc.xy(4, 5));
		return pb.getPanel();
	}
	
	protected SpExportSchemaItem createSchemaItem(Element itemElement) throws Exception
	{
		SpExportSchemaItem result = new SpExportSchemaItem();
		result.initialize();
		result.setFieldName(itemElement.attributeValue("name"));
		result.setDataType(itemElement.attributeValue("type"));
		return result;
	}
}

