/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.persistence.Transient;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.StorageTreeDefItem;
import edu.ku.brc.specify.datamodel.busrules.StorageBusRules;
import edu.ku.brc.specify.ui.treetables.TreeNodePopupMenu;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
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
        return (StorageTreeDef )((SpecifyAppContextMgr )AppContextMgr.getInstance()).getTreeDefForClass(Storage.class);
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
        recordSet.set("TTV", CollectionObject.getClassTableId(), RecordSet.GLOBAL);

        Hashtable<Integer, Boolean> duplicateHash = new Hashtable<Integer, Boolean>();
        
        String sql = "SELECT p.CollectionObjectID FROM storage as st INNER JOIN preparation as p ON st.StorageID = p.StorageID " +
                     "WHERE st.StorageID = "+storage.getStorageId()+" AND p.CollectionMemberID = COLMEMID";
        
        Vector<Integer> idList = new Vector<Integer>();
        
        fillListWithIds(sql, idList);
        
        // Get the Collection Objects from the Preparations
        for (Integer id : idList)
        {
            duplicateHash.put(id, true);
        }
        
        sql = "SELECT co.CollectionObjectID FROM storage as st INNER JOIN container as cn ON st.StorageID = cn.StorageID " +
              "INNER JOIN collectionobject co ON co.ContainerID = cn.ContainerID " +
              "WHERE st.StorageID = "+storage.getStorageId()+" AND co.CollectionMemberID = COLMEMID";
        
        fillListWithIds(sql, idList);
        for (Integer id : idList)
        {
            duplicateHash.put(id, true);
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
