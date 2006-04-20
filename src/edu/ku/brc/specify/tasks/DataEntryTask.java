/* Filename:    $RCSfile: DataEntryTask.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;

import edu.ku.brc.specify.core.ContextMgr;
import edu.ku.brc.specify.core.NavBox;
import edu.ku.brc.specify.core.NavBoxIFace;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.tasks.subpane.DataEntryPane;
import edu.ku.brc.specify.tasks.subpane.FormPane;
import edu.ku.brc.specify.tasks.subpane.SimpleDescPane;
import edu.ku.brc.specify.ui.CommandAction;
import edu.ku.brc.specify.ui.CommandDispatcher;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import edu.ku.brc.specify.ui.TreeTableViewer;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.ui.forms.persist.View;

/**
 * This task controls the data entry forms
 * 
 * @author rods
 * 
 */
public class DataEntryTask extends BaseTask
{
    private static Log log = LogFactory.getLog(DataEntryTask.class);
            
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
            navBox.add(NavBox.createBtn(getResourceString("Series_Processing"), name, IconManager.IconSize.Std16, new DataEntryAction("")));
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
        }
    }
   
    /**
     * Opens a pane with a form
     * @param formName the name of the form to be opened
     */
    public void openView(final String formName)
    {
        DataEntryPane formPane = new DataEntryPane(name, this);
        UICacheManager.getSubPaneMgr().addPane(formPane);
    }
    
    /**
     * Opens a pane with a form
     * @param formName the name of the form to be opened
     */
    public void openView(final String viewSetName, final String viewName, final String mode, final Object data)
    {
        View view = ViewMgr.getView(viewSetName, viewName);
        FormPane formPane = new FormPane(view.getName(), this, viewSetName, viewName, data);
        UICacheManager.getSubPaneMgr().addPane(formPane);
    }
    
    /**
     * Opens a pane with a form
     * @param formName the name of the form to be opened
     */
    public void openView(final View view, final String mode, final String idStr)
    {
        int tableId = DBTableIdMgr.lookupIdByClassName(view.getClassName());
        
        Query query = DBTableIdMgr.getQueryForTable(tableId, Integer.parseInt(idStr));
        try
        {
            List data = query.list();
            if (data != null && data.size() > 0)
            {
                FormPane formPane = new FormPane(view.getName(), this, view.getViewSetName(), view.getName(), data.get(0));
                UICacheManager.getSubPaneMgr().addPane(formPane);
                
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
    

    
    public void openTreeEditor(final Class treeableClass, final String name)
    {
    	TreeTableViewer ttv = new TreeTableViewer(treeableClass,name,this);
    	UICacheManager.getSubPaneMgr().addPane(ttv);
    }
    
    /**
     * Create a form for a recordset
     * @param recordSet the record to create a form for
     */
    protected void createFormFor(final RecordSet recordSet)
    {
        DBTableIdMgr.getInClause(recordSet);

        String defaultFormName = DBTableIdMgr.lookupDefaultFormNameById(recordSet.getTableId());
        
        //FormView formView = ViewMgr.getView("Main Views", tableId);
        
        Query query = DBTableIdMgr.getQueryForTable(recordSet);
        java.util.List list = query.list();
        
        System.out.println(query.toString());
        System.out.println("ResultSet: "+list.size());
        
        // XXX Hard Coded ViewSet Name
        FormPane form = new FormPane(name, this, "Main Views", defaultFormName, query.list()); 
        addSubPaneToMgr(form);
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

    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.getAction().equals("Edit"))
        {
            if (cmdAction.getData() instanceof RecordSet)
            {
                RecordSet recordSet = (RecordSet)cmdAction.getData();
                createFormFor(recordSet);
                
                //UICacheManager.addSubPane(new SimpleDescPane(title, this, "This is where we would be editing the "+recordSet.getItems().size()+" records in the RecordSet."));
            } else if (cmdAction.getData() instanceof Object[])
            {
                Object[] dataList = (Object[])cmdAction.getData();
                View   view = (View)dataList[0];
                String mode = (String)dataList[1];
                String idStr = (String)dataList[2];
                openView(view, mode, idStr);
            }
        } if (cmdAction.getAction().equals("ShowView"))
        {
            if (cmdAction.getData() instanceof Object[])
            {
                Object[] dataList = (Object[])cmdAction.getData();
                View   view = (View)dataList[0];
                String mode = (String)dataList[1];
                String idStr = (String)dataList[2];
                openView(view, mode, idStr);
            }
        }
    }
    
    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------
    
    /**
     * 
     * @author rods
     *
     */
    class DataEntryAction implements ActionListener 
    {
        private String formName;
        
        public DataEntryAction(final String formName)
        {
            this.formName = formName;
        }
        public void actionPerformed(ActionEvent e) 
        {
            openView(formName);
        }
    }
    
    /**
     * @author jds
     */
    class ShowTreeTableEditorAction implements ActionListener
    {
    	protected Class treeableClass;
    	protected String name;
    	
    	public ShowTreeTableEditorAction(final Class treeableClass, final String name)
    	{
    		this.treeableClass = treeableClass;
    		this.name = name;
    	}
    	public void actionPerformed(ActionEvent e)
    	{
    		openTreeEditor(treeableClass,name);
    	}
    }


}
