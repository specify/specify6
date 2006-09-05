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
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;

/**
 *
 * @code_status Alpha
 * @author jstewart
 * @version %I% %G%
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
		
		JMenu locMenu = new JMenu(label);
		locMenu.setMnemonic(mnemonic.charAt(0));
		MenuItemDesc miDesc = new MenuItemDesc(locMenu, "AdvMenu");
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
			locMenu.add(defMenuItem);
		}
	}
}
