/**
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIHelper.centerAndShow;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createLocalizedMenuItem;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.XPathException;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.helpers.UIFileFilter;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpExportSchema;
import edu.ku.brc.specify.datamodel.SpExportSchemaItem;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpTaskSemaphore;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgrCallerIFace;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr.USER_ACTION;
import edu.ku.brc.specify.tasks.subpane.qb.QueryBldrPane;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.IconManager;
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
	
	//protected static final String[] unSupportedsubstitutionGroups = {"dwc"
	
	/**
	 * 
	 */
	public ExportMappingTask()
	{
		super("ExportMappingTask", getResStr("TaskTitle"));
	}

    /**
     * @param key
     * @return
     */
    private static String getI18N(final String key)
    {
        return "ExportMappingTask." + key; 
    }
    
    /**
     * @param key
     * @return
     */
    private static String getResStr(final String key)
    {
        return UIRegistry.getResourceString(getI18N(key));
    }
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getToolBarItems()
	 */
	@Override
	public List<ToolBarItemDesc> getToolBarItems() 
	{
		return null;
	}



	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
	 */
	@Override
	public List<MenuItemDesc> getMenuItems() 
	{
	       String menuDesc = "Specify.SYSTEM_MENU";
	        
	        menuItems = new Vector<MenuItemDesc>();
	        
	        if (permissions == null || permissions.canView())
	        {
	            String    menuTitle = getI18N("ExMapMenu"); //$NON-NLS-1$
	            String    mneu      = getI18N("ExMapMneu"); //$NON-NLS-1$
	            String    desc      = getI18N("ExMapDesc"); //$NON-NLS-1$
	            JMenuItem mi        = createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
	            mi.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent ae)
	                {
	                    ExportMappingTask.this.requestContext();
	                }
	            });
	            MenuItemDesc rsMI = new MenuItemDesc(mi, menuDesc);
	            rsMI.setPosition(MenuItemDesc.Position.Bottom);
	            menuItems.add(rsMI);
	        }
	        return menuItems;

	}

	/**
	 * @param map
	 * @return true if everything is OK.
	 * 
	 * Locks map if it is unlocked.
	 * If it is already locked, presents dialog to user with options to cancel or override lock.
	 */
	public static boolean checkMappingLock(final SpExportSchemaMapping map)
	{
		if (map == null)
		{
			return true;
		}
		final TaskSemaphoreMgrCallerIFace callface = new TaskSemaphoreMgrCallerIFace() {

			@Override
			public USER_ACTION resolveConflict(SpTaskSemaphore semaphore,
					boolean previouslyLocked, String prevLockBy) 
			{
				if (previouslyLocked) 
				{
                    int      options      = JOptionPane.YES_NO_OPTION;
                    Object[] optionLabels = new String[] { getResourceString("CANCEL"),  //$NON-NLS-1$
                                                           getResourceString("SpecifyAppContextMgr.OVERRIDE")//$NON-NLS-1$
                                                         };
					int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                            getLocalizedMessage("ExportMappingTask.LockedMsg", map.getMappingName(), prevLockBy),
                            getResStr("LockedDlgTitle"),  //$NON-NLS-1$
                            options,
                            JOptionPane.QUESTION_MESSAGE, null, optionLabels, 0);
					if (userChoice == JOptionPane.YES_OPTION)
					{
						return USER_ACTION.Cancel;
					}
					if (userChoice == JOptionPane.NO_OPTION)
					{
						if (unlockMapping(map))
						{
							//return USER_ACTION.OK;
							// XXX ??? !!!
							return TaskSemaphoreMgr.lock(getLockTitle(map), getLockName(map), null, 
									TaskSemaphoreMgr.SCOPE.Global, false, this, false);
						}
					}
					return USER_ACTION.Cancel;
				}
				return USER_ACTION.OK;
			}
			
		};
		return TaskSemaphoreMgr.lock(getLockTitle(map), getLockName(map), null, 
				TaskSemaphoreMgr.SCOPE.Global, false, callface, false) == TaskSemaphoreMgr.USER_ACTION.OK;
		
	}

	/**
	 * @param map
	 * @return name for semaphore lock for map
	 */
	protected static String getLockName(SpExportSchemaMapping map)
	{
		return "ExportMapping" + (map == null ? "" : map.getId()); //map will probably never be null.				
	}
	
	/**
	 * @param map
	 * @return title for semaphore lock for map
	 */
	protected static String getLockTitle(SpExportSchemaMapping map)
	{
		return map.getMappingName();
	}
	
	/**
	 * @param map
	 * @return true if semaphore for map was unlocked
	 */
	public static boolean unlockMapping(SpExportSchemaMapping map)
	{
		if (map != null && map.getId() != null)
		{
			return TaskSemaphoreMgr.unlock(getLockTitle(map), "ExportMapping" + map.getId().toString(), TaskSemaphoreMgr.SCOPE.Global);
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#checkLock(edu.ku.brc.specify.datamodel.SpQuery)
	 */
	@Override
	protected boolean checkLock(SpQuery query) 
	{
		boolean result = checkMappingLock(query.getMapping());
		return result;
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
				getResStr("NewMapping"), "Query",
				getResStr("NewMappigTT"),
				IconManager.STD_ICON_SIZE, new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						addMapping();
					}
				}));
		actionNavBox.add(NavBox.createBtnWithTT(
				getResStr("ImportSchema"), "Query",
				getResStr("ImportSchemaTT"),
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
		centerAndShow(dlg);
		if (dlg.isCancelled()) return null;
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
			schemaMapping = query.getMapping();
			if (schemaMapping != null)
        	{
        		exportSchema = schemaMapping.getSpExportSchema();
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
		return getResStr("ActionNavBoxTitle");
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getQueryNavBoxTitle()
	 */
	@Override
	protected String getQueryNavBoxTitle()
	{
		return getResStr("MappingNavBoxTitle");
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
			if (selectedSchema != null) 
			{
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
					} finally 
					{
						addingMapping.set(false);
					}
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
        chooser.setDialogTitle(getResStr("IMPORT_SCHEMADEF_TITLE"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new UIFileFilter("xsd", getResStr("SCHEMA_DEFS")));
        
        if (chooser.showOpenDialog(UIRegistry.get(UIRegistry.FRAME)) != JFileChooser.APPROVE_OPTION)
        {
            UIRegistry.getStatusBar().setText("");
            return;
        }

        File file = chooser.getSelectedFile();
        if (file == null)
        {
            UIRegistry.getStatusBar().setText(getResStr("NO_FILE"));
            return;
        }
        
        String path = chooser.getCurrentDirectory().getPath();
        //String path = FilenameUtils.getPath(file.getPath());
        if (StringUtils.isNotEmpty(path))
        {
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            localPrefs.put(DEF_IMP_PREF, path);
        }
        
		if (importSchemaDefinition(file, null, null))
		{
			UIRegistry.displayInfoMsgDlgLocalized(getI18N("SchemaImportSuccess"));
		}
		else
		{
			UIRegistry.displayInfoMsgDlgLocalized(getI18N("SchemaImportFailure"));
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
		for (SpExportSchema spExportSchema : exportSchemas)
		{
			for (SpExportSchemaMapping mapping : spExportSchema
					.getSpExportSchemaMappings())
			{
				if (mapping.getCollectionMemberId().equals(AppContextMgr.getInstance().getClassObject(Collection.class).getId()))
				{
					result.add(mapping.getMappings().iterator().next()
						.getQueryField().getQuery());
				}
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
			
			if (theSession != null)
			{
    			String hql = "from SpExportSchemaMapping sesm inner join sesm.mappings maps inner join maps.queryField qf inner join qf.query q where q.id = " + query.getId()
    				+ " and sesm.collectionMemberId = " + AppContextMgr.getInstance().getClassObject(Collection.class).getId();
    			QueryIFace q = theSession.createQuery(hql, false);
    			if (q.list().size() == 0)
    			{
    				return null;
    			}
    			Object x = q.list().get(0);
    			return (SpExportSchemaMapping )((Object[] )x)[0];
			}
			return null;
		}
		finally
		{
			if (createSession && theSession != null)
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
	public static boolean importSchemaDefinition(File xsdFile, String titleText, String versionText)
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
		String theTitle = titleText;
		String theVersion = versionText;
		if (xsd != null)
		{
			if (titleText == null && versionText == null)
			{
			    ColorWrapper requiredFieldColor = AppPrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
			    
				PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, p, 2dlu, f:p:g", "p, 2dlu, p, 2dlu, p"));
				CellConstraints cc = new CellConstraints();
				pb.add(createI18NFormLabel(getI18N("SchemaDescTitle")),    cc.xy(2, 1));
				pb.add(createI18NFormLabel(getI18N("SchemaTitleTitle")),   cc.xy(2, 3));
				pb.add(createI18NFormLabel(getI18N("SchemaVersionTitle")), cc.xy(2, 5));
				
				JTextField namespace = createTextField(xsd.attributeValue("targetNamespace"));
				namespace.setEditable(false);
				pb.add(namespace, cc.xy(4, 1));
				
				final JTextField title = createTextField(40);
				title.setBackground(requiredFieldColor.getColor());
				pb.add(title, cc.xy(4, 3));
				
				final JTextField version = createTextField();
				version.setBackground(requiredFieldColor.getColor());
				pb.add(version, cc.xy(4, 5));
				
				pb.setDefaultDialogBorder();
				
				final CustomDialog dlg = new CustomDialog(
						(Frame) UIRegistry.get(UIRegistry.FRAME),
						getResStr("SchemaInfoTitle"),
						true, CustomDialog.OKCANCEL, pb.getPanel());

				DocumentListener dl = new DocumentAdaptor()
		        {
		            @Override
		            protected void changed(DocumentEvent e)
		            {
		                dlg.getOkBtn().setEnabled(!title.getText().isEmpty() || !version.getText().isEmpty());
		            }
		        };
		        title.getDocument().addDocumentListener(dl);
		        version.getDocument().addDocumentListener(dl);
		        
				centerAndShow(dlg);
				if (dlg.isCancelled())
				{
				    return false;
				}
				
				theTitle  = title.getText();
				theVersion = version.getText();
			}
			boolean doRollback = false;
			DataProviderSessionIFace session = null;
			try 
			{
				SpExportSchema schema = new SpExportSchema();
				schema.initialize();
				//XXX possibly need ui here for user to set version or remarks???
				schema.setSchemaName(theTitle);
				schema.setSchemaVersion(theVersion);
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
				if (doRollback && session != null)
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
		UIRegistry.displayErrorDlgLocalized(getI18N("MAPPING_ERR"));
		return false;
	}
	
	protected boolean includeGroup(@SuppressWarnings("unused") String substitutionGroupName)
	{
		return true;
	}
	
	protected boolean includeTerm(Element term)
	{
		return term.attributeValue("type", null) != null
			&& includeGroup(term.attributeValue("substitutionGroup", null));
	}
	
	@SuppressWarnings("unchecked")
	protected static List<Object> getNodesForDef(final Element xsd)
	{
		List<Object> result = null;
		try
		{
			result = xsd.selectNodes("xsd:element");
		}
		catch (Exception ex)
		{
			//ignore: must be a newer schema that doesn't use "xsd:element"
		}
		if (result == null)
		{
			result = new LinkedList<Object>();
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
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem mi = new JMenuItem(getResStr("XML_IMPORT_MAPPINGS"));
		popupMenu.add(mi);

		mi.addActionListener(new ActionListener() 
		{
		    @Override
			public void actionPerformed(ActionEvent e) {
				importQueries();
			}
		});

		mi = new JMenuItem(getResStr("XML_EXPORT_MAPPINGS"));
		popupMenu.add(mi);

		mi.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
				exportQueries();
			}
		});
		
		mi = new JMenuItem(getResStr("DELETE_SCHEMATA"));
		popupMenu.add(mi);

		mi.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
				deleteSchemata();
			}
		});

		return popupMenu;
	}


	/**
	 * Presents a list of existing SpExportSchemas.
	 * Deletes selected schema, if not in use.
	 * If in use displays a message listing the SpExportSchemaMappings that use the Schema
	 */
	protected void deleteSchemata()
	{
		DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
		if (session != null)
		{
			try
			{
				List<SpExportSchema> schemas =  session.getDataList(SpExportSchema.class, "discipline",	
						AppContextMgr.getInstance().getClassObject(Discipline.class), DataProviderSessionIFace.CompareType.Equals);
				ChooseFromListDlg<SpExportSchema> dlg = new ChooseFromListDlg<SpExportSchema>((Frame )UIRegistry.getTopWindow(),
						getResStr("DELETE_DLG_TITLE"), 
						getResStr("DELETE_DLG_MSG"), -1, schemas);
				centerAndShow(dlg);
				if (!dlg.isCancelled() && dlg.getSelectedObject() != null)
				{
					SpExportSchema selected = dlg.getSelectedObject();
					if (selected.getSpExportSchemaMappings().size() > 0)
					{
						displayMappings(dlg.getSelectedObject(), selected.getSpExportSchemaMappings().size() > 1 ?
								getResStr("MAPPINGS_PREVENT_DELETE") :
									getResStr("MAPPING_PREVENTS_DELETE"));
					}
					else
					{
						boolean rollback = false;
						try
						{
							session.beginTransaction();
							session.delete(selected);
							session.commit();
							UIRegistry.displayLocalizedStatusBarText(getI18N("SchemaDeleted"));
						}
						catch (Exception ex)
						{
				            if (rollback)
				            {
				            	session.rollback();
				            }
							UsageTracker.incrHandledUsageCount();
				            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExportMappingTask.class, ex);
							UIRegistry.displayStatusBarErrMsg(getResStr("SchemaDeleteError"));
						}
					}
				}
			
			}
			finally
			{
				session.close();
			}
		}
	}

	/**
	 * @param schema
	 * 
	 * Displays a dialog listing the SpExportSchemaMappings based on schema
	 */
	protected void displayMappings(SpExportSchema schema, String msg)
	{
		DefaultListModel model = new DefaultListModel();
		int i = 0;
		for (SpExportSchemaMapping mapping : schema.getSpExportSchemaMappings())
		{
			if (mapping.getCollectionMemberId().equals(AppContextMgr.getInstance().getClassObject(Collection.class).getId()))
			{
				model.add(i++, mapping.getMappingName());
			}
		}
		JList list = new JList(model);
		PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, p:g, 5dlu", "5dlu, p, 3dlu"));
		CellConstraints cc = new CellConstraints();
		pb.add(createLabel(msg), cc.xy(2, 2));
		JPanel pane = new JPanel(new BorderLayout());
		pane.add(pb.getPanel(), BorderLayout.NORTH);
		pane.add(list, BorderLayout.CENTER);
		CustomDialog cd = new CustomDialog((Frame )UIRegistry.getTopWindow(), getResStr("MAPPINGS_TITLE"),
				true, CustomDialog.OK_BTN, pane);
		centerAndShow(cd);
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getTopLevelNodeSelector()
	 */
	@Override
	protected String getTopLevelNodeSelector() 
	{
		return "/spexportschemamappings/query";
	}



	protected JPanel bldSchemaImportPane(String schemaNamespace) // I18N
	{
		PanelBuilder    pb = new PanelBuilder(new FormLayout("5dlu, p, 2dlu, p, 5dlu", "p, 2dlu, p, 2dlu, p"));
		CellConstraints cc = new CellConstraints();
		
		pb.add(createLabel("Schema:", SwingConstants.RIGHT), cc.xy(2, 1));
		pb.add(createLabel("Schema Title:", SwingConstants.RIGHT), cc.xy(2, 3));
		pb.add(createLabel("Schema Version:", SwingConstants.RIGHT), cc.xy(2, 5));
		
		JTextField namespace = createTextField(schemaNamespace);
		ViewFactory.changeTextFieldUIForDisplay(namespace, false);
		pb.add(namespace, cc.xy(4, 1));
		
		pb.add(createTextField(), cc.xy(4, 3));
		
		pb.add(createTextField(), cc.xy(4, 5));
		
		return pb.getPanel();
	}
	
	protected static SpExportSchemaItem createSchemaItem(Element itemElement) throws Exception
	{
		SpExportSchemaItem result = new SpExportSchemaItem();
		result.initialize();
		result.setFieldName(itemElement.attributeValue("name"));
		result.setDataType(itemElement.attributeValue("type"));
		try 
		{
			for (Object docObj : itemElement.selectNodes("xsd:annotation/xsd:documentation"))
			{
				//so if there is more than one docObj earlier objects 
				//are overwritten by later objects
				Element docElem = (Element )docObj;
				result.setDescription(docElem.getText());
			}
		} catch (XPathException ex)
		{
			//newer xsds don't seem to have remarks and definitely don't use "xsd" prefix
			result.setDescription(null);
		}
		return result;
	}



	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getExportDlgMsgi18nKey()
	 */
	@Override
	protected String getExportDlgMsgi18nKey() 
	{
		return getI18N("ChooseMappings");
	}



	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getExportDlgTitlei18nKey()
	 */
	@Override
	protected String getExportDlgTitlei18nKey() 
	{
		return getI18N("EXPORT_QUERIES");
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getExportHelpContext()
	 */
	@Override
	protected String getExportHelpContext() 
	{
		return super.getExportHelpContext();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getExportUsageKey()
	 */
	@Override
	protected String getExportUsageKey() 
	{
		return "ExportMapper.Export";
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getXMLExportFirstLine()
	 */
	@Override
	protected String getXMLExportFirstLine() 
	{
		return "<spexportschemamappings>\n";
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#getXMLExportLastLine()
	 */
	@Override
	protected String getXMLExportLastLine() 
	{
		return "</spexportschemamappings>";
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#toXML(edu.ku.brc.specify.datamodel.SpQuery, java.lang.StringBuilder)
	 */
	@Override
	protected void toXML(SpQuery query, StringBuilder sb) 
	{
		//XXX Don't seem to need this method anymore...
		super.toXML(query, sb);
	}

	/**
	 * @param obj object to be named
	 * @param sql query to return names in use
	 * 
	 * If necessary modifies obj's name to make it unique.
	 */
	protected String uniqueName(String currentName, String sql)
	{
		Vector<Object[]> names = BasicSQLUtils.query(sql);
		HashSet<String> nameSet = new HashSet<String>();
		for (Object[] nm : names)
		{
			nameSet.add((String )nm[0]);
		}
        int    cnt      = 0;
        String objName  = currentName;
        while (nameSet.contains(objName))
        {
            cnt++;
            objName = currentName + cnt;
       }
       return objName;	
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.QueryTask#saveImportedQueries(java.util.List)
	 */
	@Override
	protected boolean saveImportedQueries(List<SpQuery> queriesList) 
	{
		for (SpQuery q : queriesList)
		{
			SpExportSchemaMapping m = q.getMapping();
			//assuming m is not null...
			
			for (SpExportSchema e : m.getSpExportSchemas())
			{
				//XXX - kind of a problem with the export schema names. A unique name is not
				//actually necessary, but if not set, then things could get confusing (more so)
				//for people who use this feature regularly
				e.setSchemaName(uniqueName(e.getSchemaName(), "select SchemaName from spexportschema where DisciplineID = " 
						+ AppContextMgr.getInstance().getClassObject(Discipline.class).getId()));
				e.setDiscipline(AppContextMgr.getInstance().getClassObject(Discipline.class));
				
				//clear link to m to make hibernate happy.
				//This only works because e will always be a new SpExportSchema - no attempt is made to match existing
				//SpExportSchemas (and their contents).
				e.getSpExportSchemaMappings().clear(); 
				
				if (!DataModelObjBase.save(true, e))
				{
					return false;
				}
			}
			
			m.setMappingName(q.getName()); //assuming q already got a unique name and that mapping name==query name always
			if (!DataModelObjBase.save(true, q, m))
			{
				return false;
			}
		}
		return true;
	}
}

