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
import java.util.Hashtable;

import javax.persistence.Transient;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.StorageTreeDefItem;
import edu.ku.brc.specify.datamodel.busrules.StorageBusRules;
import edu.ku.brc.specify.ui.treetables.TreeNodePopupMenu;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

/**
 * Task that handles the UI for viewing storage data.
 * 
 * @code_status Beta
 * @author jstewart
 */
public class StorageTreeTask extends BaseTreeTask<Storage, StorageTreeDef, StorageTreeDefItem>
{
	public static final String STORAGE = "StorageTree";
	
	/**
	 * Constructor.
	 */
	public StorageTreeTask()
	{
        super(STORAGE, getResourceString(STORAGE));
        
        treeClass         = Storage.class;
        treeDefClass      = StorageTreeDef.class;
        icon              = IconManager.getIcon(STORAGE,IconManager.IconSize.Std16);
        
        menuItemText      = getResourceString("StorageMenu");
        menuItemMnemonic  = getResourceString("StorageMnemonic");
        starterPaneText   = getResourceString("StorageStarterPaneText");
        commandTypeString = STORAGE;
        
        businessRules     = new StorageBusRules();
        
        initialize();
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTreeTask#getCurrentTreeDef()
     */
    @Transient
    @Override
    protected StorageTreeDef getCurrentTreeDef()
    {
        return AppContextMgr.getInstance().getClassObject(Discipline.class).getStorageTreeDef();
    }
    
    /**
     * @param ttv
     * @param list
     */
    protected void showPreparations(final TreeTableViewer<Storage, StorageTreeDef, StorageTreeDefItem> ttv,
                                    final JList list)
    {
        Storage storage = ttv.getSelectedNode(list);

        // this call initializes all of the linked objects
        // it only initializes the immediate links, not objects that are multiple hops away
        ttv.initializeNodeAssociations(storage);
        
        if (storage.getPreparations().size() == 0)
        {
            UIRegistry.getStatusBar().setText(getResourceString("TTV_TAXON_NO_COS_FOR_NODE"));
            return;
        }

        RecordSet recordSet = new RecordSet();
        recordSet.initialize();
        recordSet.set(UIRegistry.getResourceString("TTV.showCollectionObjects"), CollectionObject.getClassTableId(), RecordSet.GLOBAL);

        Hashtable<Integer, Boolean> duplicateHash = new Hashtable<Integer, Boolean>();
        
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        Integer    colMemId   = collection != null ? collection.getCollectionId() : null;
        
        // Get the Collection Objects from the Preparations
        for (Preparation prep : storage.getPreparations())
        {
            if (colMemId == null || prep.getCollectionMemberId().equals(colMemId))
            {
                duplicateHash.put(prep.getCollectionObject().getId(), true);
            }
        }
        
        // Get the Collection Objects from the Containers
        for (Container container : storage.getContainers())
        {
            for (CollectionObject co : container.getCollectionObjects())
            {
                if (colMemId == null || co.getCollectionMemberId().equals(colMemId))
                {
                    duplicateHash.put(co.getId(), true);
                }
            }
        }

        for(Integer id : duplicateHash.keySet())
        {
            recordSet.addItem(id);
        }

        final RecordSet rs = recordSet;
        UIRegistry.getStatusBar().setText(getResourceString("TTV_OPENING_CO_FORM"));
        // This is needed so the StatusBar gets updated
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                CommandDispatcher.dispatch(new CommandAction(DataEntryTask.DATA_ENTRY, DataEntryTask.EDIT_DATA, rs));
            }
        });

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTreeTask#createTreeViewer(boolean)
     */
    @Override
    protected TreeTableViewer<Storage, StorageTreeDef, StorageTreeDefItem> createTreeViewer(final String titleArg, boolean isEditMode)
    {
        final TreeTableViewer<Storage, StorageTreeDef, StorageTreeDefItem> ttv = super.createTreeViewer(titleArg, isEditMode);

        if (ttv != null)
        {
            final TreeNodePopupMenu popup = ttv.getPopupMenu();
            // install custom popup menu items

            JMenuItem coMenuItem = new JMenuItem(getResourceString("TTV_ASSOC_COS"));
            coMenuItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    showPreparations(ttv, popup.getList());
                }
            });
            popup.add(coMenuItem, true);
        }
        
        return ttv;
    }    
}
