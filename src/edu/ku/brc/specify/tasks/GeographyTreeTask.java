/**
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.ku.brc.af.plugins.MenuItemDesc;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;

/**
 *
 * @code_status Alpha
 * @author jstewart
 * @version %I% %G%
 */
public class GeographyTreeTask extends BaseTreeTask<Geography,GeographyTreeDef,GeographyTreeDefItem>
{
	public static final String GEOGRAPHY = "GeographyTree";
	
	public GeographyTreeTask()
	{
        super(GEOGRAPHY, getResourceString(GEOGRAPHY));
        treeDefClass = GeographyTreeDef.class;
        icon = IconManager.getIcon(GEOGRAPHY,IconManager.IconSize.Std24);
        CommandDispatcher.register(GEOGRAPHY, this);
        initialize();
	}
	
	@Override
	protected void createMenus(List<GeographyTreeDef> defs)
	{
		String label    = getResourceString("GeographyMenu");
		String mnemonic = getResourceString("GeographyMnemonic");
		
		JMenu locMenu = new JMenu(label);
		locMenu.setMnemonic(mnemonic.charAt(0));
		MenuItemDesc miDesc = new MenuItemDesc(locMenu, "AdvMenu");
		menuItems.add(miDesc);
		
		for(GeographyTreeDef def: defs)
		{
			// create a JMenuItem for the def
			// attach a listener to the item that starts a new TTV
			final GeographyTreeDef chosenDef = def;
			JMenuItem defMenuItem = new JMenuItem(def.getName());
			defMenuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					showTree(chosenDef);
				}
			});
			locMenu.add(defMenuItem);
		}
	}
}
