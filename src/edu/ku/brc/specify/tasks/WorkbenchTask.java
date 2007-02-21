/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.subpane.wb.ColumnMapperPanel;
import edu.ku.brc.specify.tasks.subpane.wb.DataFileInfo;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchFormPane;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPane;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.Trash;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.ViewSetMgr;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.FormCell;
import edu.ku.brc.ui.forms.persist.FormCellField;
import edu.ku.brc.ui.forms.persist.FormCellLabel;
import edu.ku.brc.ui.forms.persist.FormRow;
import edu.ku.brc.ui.forms.persist.FormViewDef;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.forms.persist.ViewDef;
import edu.ku.brc.ui.forms.persist.ViewSet;
import edu.ku.brc.ui.forms.persist.FormCellField.FieldType;

/**
 * Placeholder for additional work.
 *
 * @code_status Alpha
 *
 * @author meg
 *
 */
public class WorkbenchTask extends BaseTask
{
	private static final Logger log = Logger.getLogger(WorkbenchTask.class);
    
	public static final DataFlavor WORKBENCH_FLAVOR      = new DataFlavor(WorkbenchTask.class, "Workbench");
    public static final String     WORKBENCH             = "Workbench";
    public static final String     NEW_WORKBENCH         = "New Workbench";
    public static final String     NEW_TEMPLATE          = "New Template";
    public static final String     NEW_TEMPLATE_FILE     = "New Template From File";
    public static final String     EDIT_TEMPLATE         = "Edit Template";
    public static final String     EDIT_WORKBENCH        = "Edit Workbench";
    public static final String     IMPORT_FIELD_NOTEBOOK = "Import Field Note Book";
    
    protected NavBox                      templateNavBox;
    protected NavBox                      workbenchNavBox;
    protected Vector<ToolBarDropDownBtn>  tbList         = new Vector<ToolBarDropDownBtn>();
    protected Vector<JComponent>          menus          = new Vector<JComponent>();

	/**
	 * Constructor. 
	 */
	public WorkbenchTask() 
    {
		super(WORKBENCH, getResourceString(WORKBENCH));
        
		CommandDispatcher.register(WORKBENCH, this);        
        CommandDispatcher.register(APP_CMD_TYPE, this);

	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            NavBox navBox = new NavBox(getResourceString("Actions"));
            makeDraggableAndDroppableNavBtn(navBox, getResourceString("New_Workbench"),    name, new CommandAction(WORKBENCH, NEW_WORKBENCH),     null, false);// true means make it draggable
            makeDraggableAndDroppableNavBtn(navBox, getResourceString("New_Template"),     name, new CommandAction(WORKBENCH, NEW_TEMPLATE),      null, false);// true means make it draggable
            makeDraggableAndDroppableNavBtn(navBox, getResourceString("New_TemplateFile"), name, new CommandAction(WORKBENCH, NEW_TEMPLATE_FILE), null, false);// true means make it draggable
            navBoxes.addElement(navBox);
            
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            
            templateNavBox = new NavBox(getResourceString("Templates"));           
            List list      = session.getDataList("From WorkbenchTemplate where SpecifyUserID = "+SpecifyUser.getCurrentUser().getSpecifyUserId());
            for (Object obj : list)
            {
                addTemplateToNavBox((WorkbenchTemplate)obj);
            }
            
            //navBox.add(NavBox.createBtn(getResourceString("Field_Book_Entry"),  name, IconManager.IconSize.Std16, new NavBoxAction(WORKBENCH, IMPORT_FIELD_NOTEBOOK)));
            //navBox.add(NavBox.createBtn(getResourceString("Label_Entry"), name, IconManager.IconSize.Std16));
            navBoxes.addElement(templateNavBox);

            workbenchNavBox = new NavBox(getResourceString("Workbenches"));
            list            = session.getDataList("From Workbench where SpecifyUserID = "+SpecifyUser.getCurrentUser().getSpecifyUserId());
            for (Object obj : list)
            {
                addWorkbenchToNavBox((Workbench)obj);
            }
            
            //navBox.add(NavBox.createBtn(getResourceString("Lawrence_River"), name,IconManager.IconSize.Std16));
            //navBox.add(NavBox.createBtn(getResourceString("Smith_Collection"), name, IconManager.IconSize.Std16));
            navBoxes.addElement(workbenchNavBox);
            
            session.close();
        }
    }
    
    /**
     * Adds a WorkbenchTemplate to the Left Pane NavBox
     * @param workbenchTemplate the template to be added
     */
    protected void addTemplateToNavBox(final WorkbenchTemplate workbenchTemplate)
    {
        CommandAction cmd = new CommandAction(WORKBENCH, EDIT_TEMPLATE);
        cmd.setProperty("template", workbenchTemplate);
        RolloverCommand roc = (RolloverCommand)makeDraggableAndDroppableNavBtn(templateNavBox, workbenchTemplate.getName(), name, cmd, 
                                                                               new CommandAction(WORKBENCH, DELETE_CMD_ACT, workbenchTemplate), 
                                                                               true);// true means make it draggable
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
    }
    
    /**
     * Adds a WorkbenchTemplate to the Left Pane NavBox
     * @param workbench the workbench to be added
     */
    protected void addWorkbenchToNavBox(final Workbench workbench)
    {
        CommandAction cmd = new CommandAction(WORKBENCH, EDIT_WORKBENCH);
        cmd.setProperty("workbench", workbench);
        RolloverCommand roc = (RolloverCommand)makeDraggableAndDroppableNavBtn(workbenchNavBox, workbench.getName(), name, cmd, 
                                                                               new CommandAction(WORKBENCH, DELETE_CMD_ACT, workbench), 
                                                                               true);// true means make it draggable
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return starterPane = new WorkbenchPane(name, this,null);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
//        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
//        ToolBarDropDownBtn btn = createToolbarButton(name, "workbench.gif", "workbench_hint");
//        list.add(new ToolBarItemDesc(btn));
//        return list;
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        String label = getResourceString(name);
        String iconName = name;
        String hint = getResourceString("workbench_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label, iconName, hint);

        list.add(new ToolBarItemDesc(btn));
        return list;
//      return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();

        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
    /**
     * Creates a new WorkBenchTemplate from the Column Headers and the Data in a file
     * @return the new WorkbenchTemplate
     */
    protected WorkbenchTemplate createTemplateFromFile()
    {
        // For ease of testing
        File file = null;
        if (true)
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showDialog(UICacheManager.get(UICacheManager.TOPFRAME), getResourceString("CHOOSE_WORKBENCH_IMPORT_FILE")) != JFileChooser.CANCEL_OPTION) // XXX LOCALIZE
            {
                file = chooser.getSelectedFile();
            }  
        } else
        {
            file = new File("/home/rods/Documents/_GuyanaTripX.xls");
        }
        
        WorkbenchTemplate wbt = null;
        
        if (file != null && file.exists())
        {
            DataFileInfo dataFileInfo = new DataFileInfo(file);

            JDialog           dlg    = new JDialog((Frame)null, "Column Mapper", true);
            ColumnMapperPanel mapper = new ColumnMapperPanel(dlg, dataFileInfo);
            dlg.setContentPane(mapper);
            dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dlg.pack();
            UIHelper.centerAndShow(dlg);
             
            if (!mapper.isCancelled())
            {                
                try
                {
                    wbt = mapper.createWorkbenchTemplate();
                    
                    final ViewBasedDisplayDialog editorDlg = new ViewBasedDisplayDialog((Frame)UICacheManager.get(UICacheManager.TOPFRAME),
                            "Global",
                            "WorkbenchTemplate",
                            null,
                            getResourceString("WB_TEMPLATE_INFO"),
                            getResourceString("OK"),
                            null, // className,
                            null, // idFieldName,
                            true, // isEdit,
                            0);
                    editorDlg.setData(wbt);
                    editorDlg.setModal(true);
                    
                    final WorkbenchTemplate workbenchTemplate = wbt;
                    
                    editorDlg.setCloseListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt)
                        {
                            String action = evt.getPropertyName();
                            if (action.equals("OK"))
                            {
                                editorDlg.getMultiView().getDataFromUI();
                                try
                                {
                                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                                    session.beginTransaction();
                                    session.save(workbenchTemplate);
                                    session.commit();
                                    session.flush();
                                    session.close();
                                    
                                    addTemplateToNavBox(workbenchTemplate);
                                    
                                } catch (Exception ex)
                                {
                                    ex.printStackTrace();
                                    // XXX Error Dialog
                                }
                                
                            } else
                            {
                                return;
                            }
                        }
                    });
                    editorDlg.setVisible(true);
                    
                    
                    if (wbt != null)
                    {
                        createWorkbench(dataFileInfo, wbt);
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

        }
        
        return wbt;
    }
    
    /**
     * Creates a transient View and ViewDefs for the Workbench.
     * @param wbt the WorkbenchTemplate that defines how the form should look
     * @return return the new View
     */
    protected View createTransientView(final WorkbenchTemplate wbt)
    {
        View view = new View("Dynamic", "Workbench", "Workbench", Workbench.class.getName(), "", "", false, "");

        Set<WorkbenchTemplateMappingItem>    wbtmiSet  = wbt.getWorkbenchTemplateMappingItems();
        Vector<WorkbenchTemplateMappingItem> wbtmiList = new Vector<WorkbenchTemplateMappingItem>();
        wbtmiList.addAll(wbtmiSet);
        Collections.sort(wbtmiList);

        FormViewDef formViewDef = new FormViewDef(ViewDef.ViewType.form,  "Workbench Form", Workbench.WorkbenchRow.class.getName(), "edu.ku.brc.ui.forms.DataGetterForGrid", "", "");
        formViewDef.setColumnDef("p,2px,p");
        formViewDef.setRowDef(UIHelper.createDuplicateJGoodiesDef("p", "2px", wbtmiList.size()));
        
        AltView formAltView = new AltView(view, "Workbench Form", "Form", AltView.CreationMode.Edit, true, true, formViewDef);
        
        int idCnt = 0;
        for (WorkbenchTemplateMappingItem wbtmi : wbtmiList)
        {
            FormRow       formRow   = new FormRow();
            String        idStr     = Integer.toString(idCnt);
            FormCellLabel cellLabel = new FormCellLabel("", idStr, wbtmi.getCaption(), idStr, null, false, 1);
            formRow.addCell(cellLabel);
            
            String fieldIdStr = Integer.toString(idCnt++);
            
            FormCellField cellField = new FormCellField(FormCell.CellType.field, 
                                                        fieldIdStr,
                                                        fieldIdStr,
                                                        FieldType.text,
                                                        FieldType.dsptextfield, 
                                                        "", // format
                                                        "", // formatName
                                                        "", // uiFieldFormatter
                                                        true, // isRequired
                                                        25, // cols
                                                        1, // rows
                                                        1, // colpsan
                                                        1, // rowspan
                                                        "Changed", // validationType
                                                        "",        // validationRule
                                                        false);    // isEncrypted

            formRow.addCell(cellField);
            formViewDef.addRow(formRow);
        }
        
        ViewDef gridViewDef = null;
        try
        {
            gridViewDef = (ViewDef)formViewDef.clone();
            gridViewDef.setType(ViewDef.ViewType.formtable);
            gridViewDef.setName("Workbench Grid");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        AltView gridAltView = new AltView(view, "Workbench Grid", "Grid", AltView.CreationMode.Edit, true, true, gridViewDef);

        view.addAltView(gridAltView);
        view.addAltView(formAltView);
        
        ViewSetMgr viewSetMgr = SpecifyAppContextMgr.getInstance().getBackstopViewSetMgr();
        
        ViewSet viewSet = viewSetMgr.getViewSet("Dynamic");
        if (viewSet == null)
        {
            viewSet = new ViewSet();
            viewSet.setName("Dynamic");
            viewSetMgr.addViewSet(viewSet);
        }
        
        viewSet.addTransientView(view);
        viewSet.addTransientViewDef(formViewDef);
        viewSet.addTransientViewDef(gridViewDef);
        
        return view;
        
    }
    
    /**
     * Creates a new Workbench Data Object from a definition provided by the WorkbenchTemplate and asks for the Workbench fields via a dialog
     * @param workbenchTemplate the WorkbenchTemplate
     * @return the new Workbench data object
     */
    protected Workbench createNewWorkbenchDataObj(final WorkbenchTemplate workbenchTemplate)
    {
        if (workbenchTemplate != null)
        {
            final Workbench workbench = new Workbench();
            workbench.initialize();
            workbench.setSpecifyUser(SpecifyUser.getCurrentUser());
            workbench.setWorkbenchTemplate(workbenchTemplate);
            
            try
            {
                final ViewBasedDisplayDialog editorDlg = new ViewBasedDisplayDialog((Frame)UICacheManager.get(UICacheManager.TOPFRAME),
                        "Global",
                        "Workbench",
                        null,
                        getResourceString("WB_WORKBENCH_INFO"),
                        getResourceString("OK"),
                        null, // className,
                        null, // idFieldName,
                        true, // isEdit,
                        0);
                
                editorDlg.setData(workbench);
                editorDlg.setModal(true);
                
                editorDlg.setCloseListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        String action = evt.getPropertyName();
                        if (action.equals("OK"))
                        {
                            editorDlg.getMultiView().getDataFromUI();
                            
                        } else
                        {
                            workbench.setWorkbenchTemplate(null);
                        }
                    }
                });
                editorDlg.setVisible(true);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            
            return workbench.getWorkbenchTemplate() == null ? null : workbench;
        }
        return null;
    }
    
    /**
     * Creates a new Workbench Data Object from a definition provided by the WorkbenchTemplate
     * @param dataFileInfo the DataFileInfo Object that contains all the information about the file
     * @param wbt the WorkbenchTemplate
     * @return the new Workbench data object
     */
    protected Workbench createWorkbench(final DataFileInfo dataFileInfo, final WorkbenchTemplate wbt)
    {
        Workbench workbench = createNewWorkbenchDataObj(wbt);
        
        dataFileInfo.loadData(workbench);
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        
        try
        {
            session.beginTransaction();
            session.save(workbench);
            session.commit();
            session.flush();
            
            addWorkbenchToNavBox(workbench);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        createEditorForWorkbench(workbench, session);
        
        session.close();
        
        return workbench;
        
    }
    
    /**
     * Creates the Pane for editing a Workbench.
     * @param workbench the workbench to be edited
     * @param session a session to use to load the workbench (can be null)
     */
    protected void createEditorForWorkbench(final Workbench workbench, final DataProviderSessionIFace session)
    {
        if (workbench != null)
        {
            // Make sure we have a session but use an existing one if it is passed in
            DataProviderSessionIFace tmpSession = session;
            if (session == null)
            {
                tmpSession = DataProviderFactory.getInstance().createSession();
                tmpSession.attach(workbench);
            }
            
            View view = createTransientView(workbench.getWorkbenchTemplate());
    
            WorkbenchFormPane formPane = new WorkbenchFormPane(view.getName(), 
                                                               this, 
                                                               view, 
                                                               "edit", 
                                                               null, 
                                                               MultiView.VIEW_SWITCHER | MultiView.IS_EDITTING | MultiView.RESULTSET_CONTROLLER);
            
            formPane.getViewable().setSession(tmpSession);
    
            formPane.setIcon(getImageIcon());
            
            formPane.getMultiView().setSession(tmpSession);
            formPane.getMultiView().setData(workbench.getWorkbenchRows());
            
            addSubPaneToMgr(formPane);
            
            formPane.getMultiView().setSession(null);
            
            if (session == null && tmpSession != null)
            {
                tmpSession.close();
            }

        }
    }
    
    /**
     * Creates a brand new Workbench from a template with one new row of data.
     * @param workbenchTemplate the template to create the Workbench from
     * @return the new workbench
     */
    protected Workbench createNewWorkbench(final WorkbenchTemplate workbenchTemplate)
    {
        Workbench workbench = null;
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            session.attach(workbenchTemplate);
            
            workbench = createNewWorkbenchDataObj(workbenchTemplate);
            
            for (WorkbenchTemplateMappingItem item : workbenchTemplate.getWorkbenchTemplateMappingItems())
            {
                WorkbenchDataItem wbdi = new WorkbenchDataItem();
                wbdi.initialize();
                wbdi.setCellData("");
                wbdi.setColumnNumber(item.getViewOrder());
                wbdi.setRowNumber(1);
                workbench.addWorkbenchDataItem(wbdi);
            }

            session.beginTransaction();
            session.save(workbench);
            session.commit();
            session.flush();
            
            addWorkbenchToNavBox(workbench);
            
            createEditorForWorkbench(workbench, session);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            session.close();
        }
        
        
        return workbench;
    }
    
    /**
     * Deletes a workbench.
     * @param workbench the workbench to be deleted
     */
    protected void deleteWorkbench(final Workbench workbench)
    {
        for (SubPaneIFace sp : SubPaneMgr.getInstance().getSubPanes())
        {
            MultiView mv = sp.getMultiView();
            if (mv != null)
            {
                Object data = mv.getData();
                if (data != null && data == workbench.getWorkbenchRows())
                {
                    SubPaneMgr.getInstance().removePane(sp);
                    break;
                }
            }
        }
        
        NavBoxItemIFace nbi = getBoxByTitle(workbenchNavBox, workbench.getName());
        if (nbi != null)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            
            try
            {
                session.attach(workbench);
                
                workbench.getWorkbenchTemplate().getWorkbenches().remove(workbench);
                workbench.setWorkbenchTemplate(null);
            
                session.beginTransaction();
                session.delete(workbench);
                session.commit();
                session.flush();
                
                workbenchNavBox.remove(nbi);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                // XXX Error Dialog
                
            } finally 
            {
                session.close();
            }

        }

        log.info("Deleted a Workbench ["+workbench.getName()+"]");

    }
    
    /**
     * Deletes a workbench.
     * @param workbench the workbench to be deleted
     */
    protected void deleteWorkbenchTemplate(final WorkbenchTemplate workbenchTemplate)
    {
        if (workbenchTemplate != null)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                session.attach(workbenchTemplate);
                
                boolean okToDel = true;
                if (workbenchTemplate.getWorkbenches().size() > 0)
                {
                    String msg = String.format(getResourceString("WBT_DEL_MSG"), new Object[] {workbenchTemplate.getWorkbenches().size()});
                    okToDel = UICacheManager.displayConfirm(getResourceString("WBT_DEL_TITLE"), 
                                                            msg, 
                                                            getResourceString("WBT_DELBTN"), 
                                                            getResourceString("Cancel"),
                                                            JOptionPane.QUESTION_MESSAGE);
                }
                
                if (okToDel)
                {
                    NavBoxItemIFace nbi = getBoxByTitle(templateNavBox, workbenchTemplate.getName());
                    if (nbi != null)
                    {
                        templateNavBox.remove(nbi);
                        
                        session.beginTransaction();
                        for (Workbench wb : workbenchTemplate.getWorkbenches())
                        {
                            NavBoxItemIFace wbNBI = getBoxByTitle(workbenchNavBox, wb.getName());
                            workbenchNavBox.remove(wbNBI);
                            session.delete(wb);                                
                        }
                        workbenchTemplate.getWorkbenches().clear();
                        session.delete(workbenchTemplate);
                        session.commit();
                        session.flush();
                        log.info("Deleted a Workbench ["+workbenchTemplate.getName()+"]");
                    }
                } else
                {
                    // XXX Error Dialog needed.
                    log.info("Can't delete workbench template ["+workbenchTemplate.getName()+"]");
                }
                    
                        
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    // XXX Error Dialog
                    
                } finally 
                {
                    session.close();
                }
            }

    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------
    
    /**
     * Processes all Commands of type DATA_ENTRY.
     * @param cmdAction the command to be processed
     */
    protected void processWorkbenchCommands(final CommandAction cmdAction)
    {
        if (cmdAction.isAction(EDIT_TEMPLATE))
        {
            WorkbenchTemplate template = (WorkbenchTemplate)cmdAction.getProperty("template");
            log.info("Trying to edit template "+template.getName());
            
        } else if (cmdAction.isAction(EDIT_WORKBENCH))
        {
            Workbench workbench = (Workbench)cmdAction.getProperty("workbench");
            createEditorForWorkbench(workbench, null);
            
        } else if (cmdAction.isAction(NEW_TEMPLATE))
        {
            log.info("Trying to create template ");
            
        } else if (cmdAction.isAction(NEW_TEMPLATE_FILE))
        {
            createTemplateFromFile();
            
        } else if (cmdAction.isAction(NEW_WORKBENCH))
        {
            WorkbenchTemplate workbenchTemplate = null;
            if (cmdAction.getData() instanceof CommandAction)
            {
                CommandAction srcCommand = (CommandAction)cmdAction.getData();
                workbenchTemplate = (WorkbenchTemplate)srcCommand.getProperty("template");
            }
            if (workbenchTemplate != null)
            {
                createNewWorkbench(workbenchTemplate);
            }
            
        } else if (cmdAction.isAction(DELETE_CMD_ACT))
        {
            if (cmdAction.getData() instanceof Workbench)
            {
                deleteWorkbench((Workbench)cmdAction.getData());
                
            } else if (cmdAction.getData() instanceof WorkbenchTemplate)
            {
                deleteWorkbenchTemplate((WorkbenchTemplate)cmdAction.getData());
            }
            
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(WORKBENCH))
        {
            processWorkbenchCommands(cmdAction);
            
        } else if (cmdAction.isType(APP_CMD_TYPE) && cmdAction.isAction(APP_RESTART_ACT))
        {
            //viewsNavBox.clear();
            //initializeViewsNavBox();
        }
    }
}
