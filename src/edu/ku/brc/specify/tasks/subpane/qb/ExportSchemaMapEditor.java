/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpExportSchema;
import edu.ku.brc.specify.datamodel.SpExportSchemaItem;
import edu.ku.brc.specify.datamodel.SpExportSchemaItemMapping;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 */
@SuppressWarnings("serial")
public class ExportSchemaMapEditor extends CustomDialog
{
	protected SpExportSchema exportSchema = null;
	protected SpExportSchemaMapping schemaMapping = null;
	protected JPanel workPane = null;
	protected JPanel backgroundPane;
	protected JPanel rightSidePane;
	
	protected QueryBldrPane  qb;
	protected final Taskable task;
		
    protected JButton                        mapToBtn;
    protected JButton                        unmapBtn;
    protected JButton                        upBtn;
    protected JButton                        downBtn;

	protected JPanel										btnPanel;

	protected Color											btnPanelColor;

	public ExportSchemaMapEditor(final Frame frame, final Taskable task)
	{
	    super(frame, UIRegistry.getResourceString("ExportSchemaMapEditor.Title"), true, OKCANCELHELP, null); //XXX i18n		
	    this.task = task;
	    qb = null;
	    
	    createUI();
	}
	
	protected QueryBldrPane buildQb(final SpQuery query)
	{
        return new QueryBldrPane(query.getName(), task, query, false, exportSchema, schemaMapping);
	}

	/**
     * Creates a new Query Data Object.
     * @param tableInfo the table information
     * @return the query
     */
    protected SpQuery createNewQueryDataObj()
    {
    	DBTableInfo tableInfo = getTableInfo();
    	SpQuery query = new SpQuery();
        query.initialize();
        //XXX testing
        query.setName(exportSchema == null ? "nada" : exportSchema.getSchemaName());
        query.setNamed(false);
        query.setContextTableId((short)tableInfo.getTableId());
        query.setContextName(tableInfo.getShortClassName());
        return query;
    }

	protected DBTableInfo getTableInfo()
	{
		return DBTableIdMgr.getInstance().getInfoById(1);
	}
	
	protected boolean isMapped(final Mapping item)
	{
		return item.getSchemaMap() != null;
	}
	
	/**
	 * @return a list of all the mappings associated with the current discipline.
	 */
	protected Vector<SpExportSchemaMapping> getMappings()
	{
		DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
		try
        {
	        Vector<SpExportSchemaMapping> result = new Vector<SpExportSchemaMapping>();
	        Integer disciplineId = AppContextMgr.getInstance().getClassObject(Discipline.class).getId();
        	List<SpExportSchemaMapping> mappings = session.getDataList(SpExportSchemaMapping.class);
        	for (SpExportSchemaMapping mapping : mappings)
        	{
        		if (mapping.getSpExportSchema().getDiscipline().getId().equals(disciplineId))
        		{
        			mapping.forceLoad();
        			result.add(mapping);
        		}
        	}
        	return result;
        }
        finally
        {
        	session.close();
        }
	}
	
	/**
	 * @return a list model containing available export mappings.
	 */
	protected DefaultListModel getMappingsModel()
	{
		Vector<SpExportSchemaMapping> mappings = getMappings();
		DefaultListModel result = new DefaultListModel();
		for (SpExportSchemaMapping mapping : mappings)
		{
			result.addElement(mapping);
		}
		return result;
	}
	/**
	 * @param mapping the mapping to edit.
	 * 
	 * Loads mapping into the editor, closing currently open mapping if necessary.
	 */
	protected void editMapping(final SpExportSchemaMapping mapping)
	{
		if (closeCurrentMapping())
		{
			DataProviderSessionIFace session = DataProviderFactory
					.getInstance().createSession();
			try
			{
				exportSchema = mapping.getSpExportSchema();
				session.attach(exportSchema);
				exportSchema.forceLoad();
				schemaMapping = mapping;
				if (schemaMapping.getMappings().size() == 0)
				{
					setupQB(createNewQueryDataObj());
				} else
				{
					SpQuery query = schemaMapping.getMappings().iterator()
							.next().getQueryField().getQuery();
					session.attach(query);
					query.forceLoad();
					setupQB(query);
				}
			} finally
			{
				session.close();
			}
		}
	}

	/**
	 * @return true if a schema mapping is currently being edited.
	 */
	protected boolean isEditingMapping()
	{
		return workPane != null;
	}
	
	/**
	 * sets up QB for new mapping
	 */
	protected void setupQB(final SpQuery query)
	{
       	qb = buildQb(query);

        CardLayout layout = (CardLayout )rightSidePane.getLayout();
        if (workPane != null)
        {
        	layout.removeLayoutComponent(workPane);
        	rightSidePane.remove(workPane);
        }
        workPane = createWorkPane();
        rightSidePane.add(workPane, "work");	       	
	}
	
	/**
	 * 
	 * Prompts to choose ExportSchema and opens new mapping, closing currently open mapping if necessary.
	 */
	protected void addMapping()
	{
		SpExportSchema selectedSchema = chooseExportSchema();
		if (closeCurrentMapping())
		{
			exportSchema = selectedSchema;
	       	schemaMapping = new SpExportSchemaMapping();
	       	schemaMapping.initialize();
	       	schemaMapping.setSpExportSchema(exportSchema);
	       	setupQB(createNewQueryDataObj());
		}
	}

	/**
	 * @return true if the currently edited mapping is closed.
	 * 
	 *  Closes current mapping. Gets confirmation from user in case of unsaved changes.
	 */
	protected boolean closeCurrentMapping()
	{
		if (!isEditingMapping())
		{
			return true;
		}
		if (!qb.isChanged())
		{
			return true;
		}
		//XXX finish this
		return false;
	}
	
	/**
	 * @return prompts user to choose from list of existing export schemas.
	 */
	protected SpExportSchema chooseExportSchema()
	{
		ChooseFromListDlg<SpExportSchema> dlg = new ChooseFromListDlg<SpExportSchema>((Frame )UIRegistry.getTopWindow(),
				UIRegistry.getResourceString("ExportSchemaMapEditor.ChooseSchemaTitle"), getExportSchemas());		
		UIHelper.centerAndShow(dlg);
		return dlg.getSelectedObject();
	}
	
	/**
	 * @return list of export schemas for the current discipline
	 */
	protected List<SpExportSchema> getExportSchemas()
	{
		DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
		try
        {
			List<SpExportSchema> result = session.getDataList(SpExportSchema.class, "discipline", 
					AppContextMgr.getInstance().getClassObject(Discipline.class));
			//forceLoad here to get it over with.
			//probably there will never be a lot of export schemas or schema items.
			for (SpExportSchema schema : result)
			{
				schema.forceLoad();
			}
			return result;
        }
		finally
		{
			session.close();
        }
	}
	
	/**
	 * Imports an export schema
	 */
	protected void importExportSchema()
	{
		//XXX do this
	}
	
	protected JPanel createWorkPane()
	{
		CellConstraints cc = new CellConstraints();
		
		PanelBuilder rightSideBldr = new PanelBuilder(new FormLayout("f:p:g", "5dlu, p, 2dlu, f:p, 5dlu, p, 2dlu, f:p:g"));
		rightSideBldr.add(UIHelper.createLabel(getResourceString("ExportSchemaMapEdit.ExportSchemaTitle")), cc.xy(1, 2));
		
		String schemaText = exportSchema.getSchemaName();
		if (StringUtils.isNotBlank(exportSchema.getSchemaVersion()))
		{
			schemaText += " (" + getResourceString("ExportSchemaMapEdit.SchemaVersion") + ": " + exportSchema.getSchemaVersion() + ")";
		}
		
		JPanel schemaPane = new JPanel(new BorderLayout());
		schemaPane.add(UIHelper.createLabel(schemaText), BorderLayout.NORTH);
		if (StringUtils.isNotBlank(exportSchema.getDescription()))
		{
			JTextArea ta = new JTextArea(exportSchema.getDescription());
			ta.setLineWrap(true);
			ta.setWrapStyleWord(true);
			schemaPane.add(ta, BorderLayout.CENTER);
		}
		rightSideBldr.add(schemaPane, cc.xy(1, 4));
		rightSideBldr.add(UIHelper.createLabel(getResourceString("ExportSchemaMapEdit.MappingTitle")), cc.xy(1, 6));
		rightSideBldr.add(qb, cc.xy(1, 8));
		
		
		return rightSideBldr.getPanel();
		
	}
	
	/**
	 * @param mapping
	 * @return true if mapping was deleted.
	 */
	protected boolean deleteMappingFromDB(final SpExportSchemaMapping mapping)
	{
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        boolean transOpen = false;
        boolean result = false;
        try
        {
        	session.beginTransaction();
        	transOpen = true;
        	session.delete(mapping.getMappings().iterator().next().getQueryField().getQuery());
        	session.delete(mapping);
        	session.commit();
        	transOpen = false;
        	result = true;
        }
        catch (Exception ex)
        {
            if (transOpen)
            {
            	session.rollback();
            }
        	UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QueryBldrPane.class, ex);
            ex.printStackTrace();
        }
        finally
        {
            session.close();
        }
        return result;
	}
	
	/**
	 * @param mapping the mapping to delete.
	 */
	protected void deleteMapping(final JList mapList)
	{
		//XXX need a name field for SpExportSchemaMapping?
		SpExportSchemaMapping mapping = (SpExportSchemaMapping )mapList.getSelectedValue();
		String mapName = mapping.getMappings().iterator().next().getQueryField().getQuery().getName();
		if (UIRegistry.displayConfirm(getResourceString("ExportSchemaMapEditor.ConfirmMappingDeleteTitle"), 
				String.format(getResourceString("ExportSchemaMapEditor.ConfirmMappingDeleteMsg"), mapName), 
				getResourceString("OK"), getResourceString("Cancel"), JOptionPane.QUESTION_MESSAGE))
		{
			if (deleteMappingFromDB(mapping))
			{
				SwingUtilities.invokeLater(new Runnable() {

					/* (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run()
					{
						((DefaultListModel )mapList.getModel()).removeElementAt(mapList.getSelectedIndex());
					}
				});
			}
		}
	
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.CustomDialog#createUI()
	 */
	@Override
	public void createUI()
	{
		super.createUI();

		
		CellConstraints cc = new CellConstraints();

		final JList mappingList = UIHelper.createList(getMappingsModel());
	
		final JButton editBtn = UIHelper.createButton(UIRegistry.getResourceString("EDIT"));
		editBtn.addActionListener(new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				editMapping((SpExportSchemaMapping )mappingList.getSelectedValue());				
			}
			
		});
	
		JButton addBtn = UIHelper.createButton(UIRegistry.getResourceString("ADD"));
		addBtn.addActionListener(new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				addMapping();				
			}
			
		});
 
		final JButton delBtn = UIHelper.createButton(UIRegistry.getResourceString("DELETE"));
		delBtn.addActionListener(new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				deleteMapping(mappingList);				
			}
			
		});
		
		JButton impBtn = UIHelper.createButton(UIRegistry.getResourceString("ExportSchemaMapEditor.ImportExportSchema"));
		impBtn.addActionListener(new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				importExportSchema();				
			}
			
		});
		
		mappingList.addListSelectionListener(new ListSelectionListener() {

			/* (non-Javadoc)
			 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
			 */
			@Override
			public void valueChanged(ListSelectionEvent arg0)
			{
				Object value = mappingList.getSelectedValue();
				delBtn.setEnabled(mappingList.getModel().getSize() > 0 && value != null && value != schemaMapping);
				editBtn.setEnabled(mappingList.getModel().getSize() > 0 && value != null);				
			}			
		});
		
		PanelBuilder leftSideBldr = new PanelBuilder(new FormLayout("5dlu, f:p:g, 5dlu", "5dlu, p, 2dlu, f:p:g, 5dlu, p"));
		leftSideBldr.add(UIHelper.createLabel(getResourceString("ExportSchemaMapEdit.MappingsListTitle")), cc.xy(2, 2));
		mappingList.setPreferredSize(new Dimension(300, 500));
		leftSideBldr.add(mappingList, cc.xy(2, 4));
		PanelBuilder btnPaneBldr = new PanelBuilder(new FormLayout("f:p:g, 2dlu, f:p:g, 2dlu, f:p:g, 2dlu, f:p:g", "p"));
		btnPaneBldr.add(editBtn, cc.xy(1, 1));
		btnPaneBldr.add(addBtn, cc.xy(3, 1));
		btnPaneBldr.add(delBtn, cc.xy(5, 1));
		btnPaneBldr.add(impBtn, cc.xy(7, 1));
		leftSideBldr.add(btnPaneBldr.getPanel(), cc.xy(2, 6));
						
		backgroundPane = new JPanel();
		CardLayout layout = new CardLayout();
		rightSidePane = new JPanel(layout);
		rightSidePane.setPreferredSize(new Dimension(400, 500));
		
		layout.addLayoutComponent(backgroundPane, "background");

		PanelBuilder mainBldr = new PanelBuilder(new FormLayout("f:p, 5dlu, f:p:g, 10dlu", "f:p:g"));
		
		mainBldr.add(leftSideBldr.getPanel(), cc.xy(1, 1));
		mainBldr.add(rightSidePane, cc.xy(3, 1));
		
		contentPanel = mainBldr.getPanel();

		okBtn.setEnabled(false);

		HelpMgr.registerComponent(helpBtn, helpContext);

		mainPanel.add(contentPanel, BorderLayout.CENTER);
		
		pack();

	}
	
	public class Mapping
	{
		protected SpExportSchemaItem schemaItem;
		protected SpExportSchemaItemMapping schemaMap;
		
		public Mapping(SpExportSchemaItem schemaItem,
				SpExportSchemaItemMapping schemaMap)
		{
			super();
			this.schemaItem = schemaItem;
			this.schemaMap = schemaMap;
		}
		/**
		 * @return the schemaItem
		 */
		public SpExportSchemaItem getSchemaItem()
		{
			return schemaItem;
		}
		/**
		 * @param schemaItem the schemaItem to set
		 */
		public void setSchemaItem(SpExportSchemaItem schemaItem)
		{
			this.schemaItem = schemaItem;
		}
		/**
		 * @return the schemaMap
		 */
		public SpExportSchemaItemMapping getSchemaMap()
		{
			return schemaMap;
		}
		/**
		 * @param schemaMap the schemaMap to set
		 */
		public void setSchemaMap(SpExportSchemaItemMapping schemaMap)
		{
			this.schemaMap = schemaMap;
		}
		
		/**
		 * @return the query field for the mapping
		 */
		public SpQueryField getQueryField()
		{
			if (schemaMap != null)
			{
				return schemaMap.getQueryField();
			}
			
			return null;
		}
		
		/**
		 * @return the FieldInfo for the mapping
		 */
		public String getMappedToText()
		{
			SpQueryField qf = getQueryField();
			if (qf != null)
			{
				DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoById(qf.getContextTableIdent());
				if (tbl != null)
				{
					return tbl.getTitle() + "." + qf.getFieldName();
				}
			}
			return getResourceString("ExportSchemaMapEditor.Unmapped");
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return getMappedToText() + " <===> " + schemaItem;
		}
		
		
	}
	
	public class MappingListRenderer implements ListCellRenderer
	{
		//testing testing testing
		protected SpQueryField dummy;
		
		public MappingListRenderer()
		{
            DataProviderSessionIFace session = DataProviderFactory.getInstance()
            .createSession();
            try
            {
                dummy = session.get(SpQueryField.class, 1);
                dummy.forceLoad();
            }
            finally
            {
                session.close();
            }
			
		}
		/* (non-Javadoc)
		 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
		 */
		@Override
		public Component getListCellRendererComponent(JList arg0, Object arg1,
				int arg2, boolean arg3, boolean arg4)
		{
			Mapping mapping = (Mapping )arg1;
        	//FieldQRI fieldQRI = qb.getFieldQRI(mapping.getQueryField());
        	FieldQRI fieldQRI = qb.getFieldQRI(dummy);
            if (fieldQRI != null)
            {
                return QueryBldrPane.bldQueryFieldPanel(qb, fieldQRI, mapping.getQueryField(), 
                		qb.getColumnDefStr(), null);
            }    
			return new JLabel(mapping.toString());
		}
		
	}
}
