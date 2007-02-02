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
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.ui.treetables.TreeNodePopupMenu;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;

/**
 * Task that handles the UI for viewing taxonomy data.
 * 
 * @code_status Beta
 * @author jstewart
 */
public class TaxonTreeTask extends BaseTreeTask<Taxon,TaxonTreeDef,TaxonTreeDefItem>
{
    private static final Logger log = Logger.getLogger(TaxonTreeTask.class);
	public static final String TAXON = "TaxonTree";
	
	public TaxonTreeTask()
	{
        super(TAXON, getResourceString(TAXON));
        treeDefClass = TaxonTreeDef.class;
        icon = IconManager.getIcon(TAXON,IconManager.IconSize.Std24);
        CommandDispatcher.register(TAXON, this);
        initialize();
        
        log.info("\"Taxonomy (the science of classification) is often undervalued as a glorified form of filing - with each species in its prescribed place in an album; but taxonomy is a fundamental and dynamic science, dedicated to exploring the causes of relationships and similarities among organisms. Classifications are theories about the basis of natural order, not dull catalogues compiled only to avoid chaos.\" Stephen Jay Gould (1990, p.98)");
	}
	
	@Override
	protected void createMenus(List<TaxonTreeDef> defs)
	{
		String label    = getResourceString("TaxonMenu");
		String mnemonic = getResourceString("TaxonMnemonic");
		
		JMenu taxMenu = new JMenu(label);
        taxMenu.setMnemonic(mnemonic.charAt(0));
		MenuItemDesc miDesc = new MenuItemDesc(taxMenu, "AdvMenu");
		menuItems.add(miDesc);
		
		for(TaxonTreeDef def: defs)
		{
			// create a JMenuItem for the def
			// attach a listener to the item that starts a new TTV
			final TaxonTreeDef chosenDef = def;
			JMenuItem defMenuItem = new JMenuItem(def.getName());
			defMenuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					showTree(chosenDef);
				}
			});
            taxMenu.add(defMenuItem);
		}
	}

	@Override
	protected TreeTableViewer<Taxon, TaxonTreeDef, TaxonTreeDefItem> showTree(TaxonTreeDef treeDef)
	{
		final TreeTableViewer<Taxon, TaxonTreeDef, TaxonTreeDefItem> ttv =  super.showTree(treeDef);
		
		if(ttv != null)
		{
			final TreeNodePopupMenu popup = ttv.getPopupMenu();
			// install custom popup menu items
			JMenuItem getITIS = new JMenuItem("Get ITIS Info");
			getITIS.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					Taxon taxon = ttv.getSelectedNode(popup.getList());
					System.out.println("Get ITIS info for " + taxon.getFullName());
				}
			});
			popup.add(getITIS);
			
			JMenuItem getCollObjs = new JMenuItem("Associated Collection Objects");
			getCollObjs.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					Taxon taxon = ttv.getSelectedNode(popup.getList());
					System.out.println("Get associated collection objects for " + taxon.getFullName());
					
					Vector<CollectionObject> collObjs = new Vector<CollectionObject>();
					for(Determination deter : taxon.getDeterminations())
					{
						CollectionObject collObj = deter.getCollectionObject();
						collObjs.add(collObj);
						System.out.println("Collection object " + collObj.getName() + " " + collObj.getCatalogNumber() );
					}
				}
			});
			popup.add(getCollObjs);
			
		}

		return ttv;
	}
}
