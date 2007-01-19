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
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;

/**
 * Task that handles the UI for viewing geologic time period data.
 * 
 * @code_status Beta
 * @author jstewart
 */
public class GtpTreeTask extends BaseTreeTask<GeologicTimePeriod,GeologicTimePeriodTreeDef,GeologicTimePeriodTreeDefItem>
{
	public static final String GTP = "GeoTimePeriodTree";
	
	public GtpTreeTask()
	{
        super(GTP, getResourceString(GTP));
        treeDefClass = GeologicTimePeriodTreeDef.class;
        icon = IconManager.getIcon(GTP,IconManager.IconSize.Std24);
        CommandDispatcher.register(GTP, this);
        initialize();
	}
	
	@Override
	protected void createMenus(List<GeologicTimePeriodTreeDef> defs)
	{
		String label    = getResourceString("GeoTimePeriodMenu");
		String mnemonic = getResourceString("GeoTimePeriodMnemonic");
		
		JMenu gtpMenu = new JMenu(label);
        gtpMenu.setMnemonic(mnemonic.charAt(0));
		MenuItemDesc miDesc = new MenuItemDesc(gtpMenu, "AdvMenu");
		menuItems.add(miDesc);
		
		for(GeologicTimePeriodTreeDef def: defs)
		{
			// create a JMenuItem for the def
			// attach a listener to the item that starts a new TTV
			final GeologicTimePeriodTreeDef chosenDef = def;
			JMenuItem defMenuItem = new JMenuItem(def.getName());
			defMenuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					showTree(chosenDef);
				}
			});
            gtpMenu.add(defMenuItem);
		}
	}
}
