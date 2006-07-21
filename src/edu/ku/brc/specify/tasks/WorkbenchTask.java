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

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.core.NavBox;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.tasks.subpane.WorkbenchPane;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.SubPaneIFace;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UICacheManager;

/**
 * PLaceholder for additional work
 *
 * @author meg
 *
 */
public class WorkbenchTask extends BaseTask
{
	private static final Logger log = Logger.getLogger(WorkbenchTask.class);
	public static final DataFlavor WORKBENCH_FLAVOR = new DataFlavor(WorkbenchTask.class, "Workbench");
	public static final String WORKBENCH = "Workbench";

    protected Vector<ToolBarDropDownBtn> tbList = new Vector<ToolBarDropDownBtn>();
    protected Vector<JComponent>          menus  = new Vector<JComponent>();

	public WorkbenchTask() {
		super(WORKBENCH, getResourceString(WORKBENCH));
		CommandDispatcher.register(WORKBENCH, this);
		NavBox navBox = new NavBox(getResourceString("File"));
		navBox.add(NavBox.createBtn(getResourceString("New_Workbench"), name, IconManager.IconSize.Std16));
		navBox.add(NavBox.createBtn(getResourceString("New_Template"), name, IconManager.IconSize.Std16));
		navBoxes.addElement(navBox);
		//
		navBox = new NavBox(getResourceString("Templates"));
		navBox.add(NavBox.createBtn(getResourceString("Field_Book_Entry"),	name, IconManager.IconSize.Std16, new ImportFieldBookAction()));
		navBox.add(NavBox.createBtn(getResourceString("Label_Entry"), name,	IconManager.IconSize.Std16));
		navBoxes.addElement(navBox);

		navBox = new NavBox(getResourceString("Workbenches"));
		navBox.add(NavBox.createBtn(getResourceString("Lawrence_River"), name,IconManager.IconSize.Std16));
		navBox.add(NavBox.createBtn(getResourceString("Smith_Collection"), name, IconManager.IconSize.Std16));
		navBoxes.addElement(navBox);
	}
		// super(getResourceString("Workbench"));
		// TODO Auto-generated constructor stub
		//NavBox navBox = new NavBox(name);



    protected void importFieldNotebook()
    {
        if (false)
        {
    		JFileChooser jfc = new JFileChooser();
    		FileFilter filter = new FileFilter()
    		{
    			public boolean accept(File f)
    			{
    				if( f.getName().length() < 4 )
    				{
    					return false;
    				}
    				String nameEnd = f.getName().substring(f.getName().length()-4);
    				if( nameEnd.equalsIgnoreCase(".csv") || nameEnd.equalsIgnoreCase(".tab") )
    					return true;
    				return false;
    			}
    			public String getDescription()
    			{
    				return "csv files";
    			}
    		};
    		jfc.setFileFilter(filter);
    		int result = jfc.showOpenDialog(UICacheManager.get(UICacheManager.TOPFRAME));
    		if( result == JFileChooser.APPROVE_OPTION )
    		{
    			File csvFile = jfc.getSelectedFile();
    			log.debug("Importing field notebook: " + csvFile.getAbsolutePath() );
    			UICacheManager.getSubPaneMgr().addPane(new WorkbenchPane("Field Notebook Import", this, jfc.getSelectedFile()));
    		}
        } else
        {
            FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File f, String name) {
                //System.out.println(name);
                String fileName = name;
                if( fileName.length() < 4 )
                {
                    return false;
                }
                String nameEnd = fileName.substring(fileName.length()-4);

                if( nameEnd.equalsIgnoreCase(".csv") || nameEnd.equalsIgnoreCase(".tab") )
                {
                    //System.out.println(name+"["+nameEnd+"]");
                    return true;
                }
                return false;
            }
            };
            FileDialog dialog = new FileDialog((Frame)UICacheManager.get(UICacheManager.TOPFRAME), "Import Data",FileDialog.LOAD);
            dialog.setFilenameFilter(filter);
            dialog.setVisible(true);
            log.info("Importing field notebook: " + dialog.getFile());


            String curFile;
            if ((curFile = dialog.getFile()) != null)
            {
                String filename = dialog.getDirectory() + curFile;

                UICacheManager.getSubPaneMgr().addPane(new WorkbenchPane("Field Notebook Import", this, new File(filename)));
            }

        }
    }

    public class ImportFieldBookAction implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
		{
			importFieldNotebook();
		}
    }

    //-------------------------------------------------------
    // Plugin Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return new WorkbenchPane(name, this,null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
//        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
//        ToolBarDropDownBtn btn = createToolbarButton(name, "workbench.gif", "workbench_hint");
//        list.add(new ToolBarItemDesc(btn));
//        return list;
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        ToolBarDropDownBtn btn = createToolbarButton(name, "workbench.gif", "workbench_hint");

        list.add(new ToolBarItemDesc(btn));
        return list;
//      return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();

        return list;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getTaskClass()
     */
    public Class getTaskClass()
    {
        return this.getClass();
    }


}
