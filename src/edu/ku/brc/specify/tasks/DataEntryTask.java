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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Query;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.plugins.MenuItemDesc;
import edu.ku.brc.af.plugins.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.ui.treetables.TreeDefinitionEditor;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.forms.ViewMgr;
import edu.ku.brc.ui.forms.persist.View;

/**
 * This task controls the data entry forms
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class DataEntryTask extends BaseTask
{
    private static final Logger log = Logger.getLogger(DataEntryTask.class);

    public static final String DATA_ENTRY = "Data_Entry";

    // Data Members
    protected Vector<NavBoxIFace> extendedNavBoxes = new Vector<NavBoxIFace>();


    /**
     * Default Constructor
     *
     */
    public DataEntryTask()
    {
        super(DATA_ENTRY, getResourceString(DATA_ENTRY));
        CommandDispatcher.register(DATA_ENTRY, this);

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false

            // Temporary
            NavBox navBox = new NavBox(getResourceString("Actions"));
            navBox.add(NavBox.createBtn(getResourceString("Series_Processing"), name, IconManager.IconSize.Std16));
            navBoxes.addElement(navBox);

            navBox = new NavBox(getResourceString("CreateAndUpdate"));
            //navBox.add(NavBox.createBtn(title, name, IconManager.IconSize.Std16));
            navBox.add(NavBox.createBtn("Specimen", "ColObj", IconManager.IconSize.Std16));
            navBox.add(NavBox.createBtn("Locality", "Locality", IconManager.IconSize.Std16));
            navBox.add(NavBox.createBtn("Taxon", "Taxon", IconManager.IconSize.Std16,new ShowTreeTableEditorAction(TaxonTreeDef.class,"Taxonomy Editor")));
            navBox.add(NavBox.createBtn("Geography", "Geography", IconManager.IconSize.Std16,new ShowTreeTableEditorAction(GeographyTreeDef.class,"Geography Editor")));
            navBox.add(NavBox.createBtn("Location", "Location", IconManager.IconSize.Std16,new ShowTreeTableEditorAction(LocationTreeDef.class,"Location Editor")));
            navBox.add(NavBox.createBtn("Geologic Time Period", "Geologic Time Period", IconManager.IconSize.Std16,new ShowTreeTableEditorAction(GeologicTimePeriodTreeDef.class,"Geologic Time Period Editor")));
            navBox.add(NavBox.createBtn("Agent", "Agent", IconManager.IconSize.Std16));
            navBox.add(NavBox.createBtn("Address", "Address", IconManager.IconSize.Std16));
            navBoxes.addElement(navBox);

            navBox = new NavBox(getResourceString("Administration Tasks"));
            navBox.add(NavBox.createBtn("Taxon Tree Def Editor","Taxon", IconManager.IconSize.Std16,new ShowTreeDefinitionEditorAction(TaxonTreeDef.class,"Taxonomy Tree Def Editor")));
            navBox.add(NavBox.createBtn("Geography Tree Def Editor","Geography", IconManager.IconSize.Std16,new ShowTreeDefinitionEditorAction(GeographyTreeDef.class,"Geography Tree Def Editor")));
            navBox.add(NavBox.createBtn("Location Tree Def Editor","Location", IconManager.IconSize.Std16,new ShowTreeDefinitionEditorAction(LocationTreeDef.class,"Location Tree Def Editor")));
            navBox.add(NavBox.createBtn("GTP Tree Def Editor","Geologic Time Period", IconManager.IconSize.Std16,new ShowTreeDefinitionEditorAction(GeologicTimePeriodTreeDef.class,"GTP Tree Def Editor")));
            navBoxes.addElement(navBox);
        }
    }

    /**
     * Opens a pane with a view to data
     * @param task the owning Task
     * @param viewSetName the ViewSet Name
     * @param viewName the view's name 
     * @param mode the creation mode (View, Edit)
     * @param data the data to fill in 
     * @param isNewForm indicates that it is a "new" form for entering in new data
     */
    public static void openView(final Taskable task, 
                                final String   viewSetName, 
                                final String   viewName, 
                                final String   mode, 
                                final Object   data,
                                final boolean isNewForm)
    {
        View view = ViewMgr.getView(viewSetName, viewName);
        FormPane formPane = new FormPane(view.getName(), task, viewSetName, viewName, mode, data, isNewForm);
        SubPaneMgr.getInstance().addPane(formPane);
    }

    /**
     * Opens a View and fills it with a single data object
     * @param mode the mode of how it is to be opened (View, Edit) 
     * @param idStr a string that contains the Integer Id (Primary Key) of the object to be shown
     */
    public static void openView(final Taskable task, final View view, final String mode, final String idStr)
    {
        int tableId = DBTableIdMgr.lookupIdByClassName(view.getClassName());

        Query query = DBTableIdMgr.getQueryForTable(tableId, Integer.parseInt(idStr));
        try
        {
            List data = query.list();
            if (data != null && data.size() > 0)
            {
                FormPane formPane = new FormPane(view.getName(), task, view.getViewSetName(), view.getName(), mode, data.get(0), false);
                SubPaneMgr.getInstance().addPane(formPane);

            } else
            {
                // No Data Error
            }

        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
        }
    }



    /**
     * Opens tree Editor with the class of of the tree (type)
     * @param treeableClass class of tree to be editted
     * @param name the name of the tree
     */
    public void openTreeEditor(final Class treeDefClass, final String name)
    {
    	TreeTableViewer ttv = new TreeTableViewer(treeDefClass,name,this);
    	SubPaneMgr.getInstance().addPane(ttv);
    }
    
    public void openTreeDefEditor(final Class treeDefClass, final String name)
    {
    	TreeDefinitionEditor defEditor = new TreeDefinitionEditor(treeDefClass,name,this);
    	SubPaneMgr.getInstance().addPane(defEditor);
    }

    /**
     * Create a form for a recordset
     * @param recordSet the record to create a form for
     */
    protected static FormPane createFormFor(final Taskable task, final String name, final RecordSet recordSet)
    {
        DBTableIdMgr.getInClause(recordSet);

        String defaultFormName = DBTableIdMgr.lookupDefaultFormNameById(recordSet.getTableId());

        Query query = DBTableIdMgr.getQueryForTable(recordSet);
        
        // "null" ViewSet name means it should use the default
        return new FormPane(name, task, null, defaultFormName, null, query.list(), false);

    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getNavBoxes()
     */
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

        RecordSetTask rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);

        extendedNavBoxes.addAll(rsTask.getNavBoxes());

        return extendedNavBoxes;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return new SimpleDescPane(title, this, "This is the Data Entry Pane");
    }

    //-------------------------------------------------------
    // Plugin Interface
    //-------------------------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();

        ToolBarDropDownBtn btn = createToolbarButton(DATA_ENTRY,   "dataentry.gif",    "dataentry_hint");

        list.add(new ToolBarItemDesc(btn));

        return list;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        return list;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#installPrefs()
     */
    public void installPrefs()
    {
       /*Preferences appPrefs = UICacheManager.getAppPrefs();

       String sectionName = appendChildPrefName("UserInterface", "Formatting", "name");
       String sectionName = appPrefs.get("", null);
       if (sectionName == null)
       {

       }


        */
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#removePrefs()
     */
    public void removePrefs()
    {

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getTaskClass()
     */
    public Class getTaskClass()
    {
        return this.getClass();
    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.getAction().equals("Edit"))
        {
            if (cmdAction.getData() instanceof RecordSet)
            {
                addSubPaneToMgr(createFormFor(this, name, (RecordSet)cmdAction.getData()));
                
            } else if (cmdAction.getData() instanceof Object[])
            {
                Object[] dataList = (Object[])cmdAction.getData();
                if (dataList.length != 3)
                {
                    View   view = (View)dataList[0];
                    String mode = (String)dataList[1];
                    String idStr = (String)dataList[2];
                    openView(this, view, mode, idStr);
                    
                } else
                {
                    log.error("The Edit Command was sent with an object Array that was not 3 components!");
                }
            } else
            {
                log.error("The Edit Command was sent that didn't have data that was a RecordSet or an Object Array");
            }
            
        } if (cmdAction.getAction().equals("ShowView"))
        {
            if (cmdAction.getData() instanceof Object[])
            {
                Object[] dataList = (Object[])cmdAction.getData();
                View   view = (View)dataList[0];
                String mode = (String)dataList[1];
                String idStr = (String)dataList[2];
                openView(this, view, mode, idStr);
            }
        }
    }

    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------

    /**
     * @author jds
     */
    class ShowTreeTableEditorAction implements ActionListener
    {
    	protected Class treeDefClass;
    	protected String name;

    	public ShowTreeTableEditorAction(final Class treeDefClass, final String name)
    	{
    		this.treeDefClass = treeDefClass;
    		this.name = name;
    	}
    	public void actionPerformed(ActionEvent e)
    	{
    		openTreeEditor(treeDefClass,name);
    	}
    }

    /**
     * @author jds
     */
    class ShowTreeDefinitionEditorAction implements ActionListener
    {
    	protected Class treeDefClass;
    	protected String name;

    	public ShowTreeDefinitionEditorAction(final Class treeDefClass, final String name)
    	{
    		this.treeDefClass = treeDefClass;
    		this.name = name;
    	}
    	public void actionPerformed(ActionEvent e)
    	{
    		openTreeDefEditor(treeDefClass,name);
    	}
    }


}
