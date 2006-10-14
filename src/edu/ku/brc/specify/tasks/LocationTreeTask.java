/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.LocationTreeDef;
import edu.ku.brc.specify.datamodel.LocationTreeDefItem;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;

/**
 *
 * @code_status Beta
 * @author jstewart
 */
public class LocationTreeTask extends BaseTreeTask<Location,LocationTreeDef,LocationTreeDefItem>
{
	public static final String LOCATION = "LocationTree";
	
	public LocationTreeTask()
	{
        super(LOCATION, getResourceString(LOCATION));
        treeDefClass = LocationTreeDef.class;
        icon = IconManager.getIcon(LOCATION,IconManager.IconSize.Std24);
        CommandDispatcher.register(LOCATION, this);
        initialize();
	}
	
	@Override
	protected void createMenus(List<LocationTreeDef> defs)
	{
		String label    = getResourceString("LocationMenu");
		String mnemonic = getResourceString("LocationMnemonic");
		
		JMenu locMenu = new JMenu(label);
		locMenu.setMnemonic(mnemonic.charAt(0));
		MenuItemDesc miDesc = new MenuItemDesc(locMenu, "AdvMenu");
		menuItems.add(miDesc);
		
		for(LocationTreeDef def: defs)
		{
			// create a JMenuItem for the def
			// attach a listener to the item that starts a new TTV
			final LocationTreeDef chosenDef = def;
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
