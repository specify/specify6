/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;

/**
 * Task that handles the UI for viewing geography data.
 * 
 * @code_status Beta
 * @author jstewart
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
		
		JMenu geogMenu = new JMenu(label);
        geogMenu.setMnemonic(mnemonic.charAt(0));
		MenuItemDesc miDesc = new MenuItemDesc(geogMenu, "AdvMenu");
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
            geogMenu.add(defMenuItem);
		}
	}
}
