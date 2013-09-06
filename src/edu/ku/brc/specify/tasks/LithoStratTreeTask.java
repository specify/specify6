/* Copyright (C) 2013, University of Kansas Center for Research
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
import java.util.Vector;

import javax.persistence.Transient;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDefItem;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.busrules.LithoStratBusRules;
import edu.ku.brc.specify.ui.treetables.TreeNodePopupMenu;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIRegistry;

/**
 * Task that handles the UI for viewing litho stratigraphy data.
 * 
 * @code_status Beta
 * @author rods
 */
public class LithoStratTreeTask extends BaseTreeTask<LithoStrat,LithoStratTreeDef,LithoStratTreeDefItem>
{
	public static final String LITHO = "LithoStratTree";
	
	/**
	 * Constructor.
	 */
	public LithoStratTreeTask()
	{
        super(LITHO, getResourceString(LITHO));
        
        treeClass        = LithoStrat.class;
        treeDefClass     = LithoStratTreeDef.class;
        
        commandTypeString = LITHO;
        
        businessRules = new LithoStratBusRules();

        
        initialize();
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTreeTask#getCurrentTreeDef()
     */
    @Transient
    @Override
    protected LithoStratTreeDef getCurrentTreeDef()
    {
        return (LithoStratTreeDef )((SpecifyAppContextMgr )AppContextMgr.getInstance()).getTreeDefForClass(LithoStrat.class);
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTreeTask#isTreeOnByDefault()
     */
    @Override
    public boolean isTreeOnByDefault()
    {
        return Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.paleobotany) ||
               Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.invertpaleo) ||
               Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.vertpaleo);
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.BaseTreeTask#createTreeViewer(java.lang.String, boolean)
	 */
	@Override
	protected TreeTableViewer<LithoStrat, LithoStratTreeDef, LithoStratTreeDefItem> createTreeViewer(
			String titleArg, boolean isEditMode) {
		final TreeTableViewer<LithoStrat, LithoStratTreeDef, LithoStratTreeDefItem> ttv = super.createTreeViewer(titleArg, isEditMode);
		if (ttv != null) {
            final TreeNodePopupMenu popup = ttv.getPopupMenu();
            JMenuItem getCos = new JMenuItem(getResourceString("TTV_ASSOC_COS"));
            getCos.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                	LithoStrat lithoNode = ttv.getSelectedNode(popup.getList());

                    // this call initializes all of the linked objects
                    // it only initializes the immediate links, not objects that are multiple hops away
                    //ttv.initializeNodeAssociations(taxon);
                    
                    if (getColObjCount(lithoNode) == 0)
                    {
                        UIRegistry.displayInfoMsgDlgLocalized("TTV_NO_COS_FOR_NODE", 
                        		DBTableIdMgr.getInstance().getTitleForId(LithoStrat.getClassTableId()));
                        UIRegistry.getStatusBar().setLocalizedText("TTV_NO_COS_FOR_NODE", 
                        		DBTableIdMgr.getInstance().getTitleForId(LithoStrat.getClassTableId()));
                        return;
                    }

                   final RecordSet recordSet = createColObjRSFromLitho(lithoNode);

                    UIRegistry.getStatusBar().setText(getResourceString("TTV_OPENING_CO_FORM"));
                    // This is needed so the StatusBar gets updated
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run()
                        {
                            CommandDispatcher.dispatch(new CommandAction(DataEntryTask.DATA_ENTRY, DataEntryTask.EDIT_DATA, recordSet));
                        }
                    });
                }
            });
            popup.add(getCos, true);
		}
		return ttv;
	}

	/**
	 * @param litho
	 * @return
	 */
	int getColObjCount(LithoStrat litho) {
		String sql = "select count(*) from paleocontext pc inner join "
				+ "collectionobject co on co.PaleoContextID = pc.PaleoContextID "
				+ "where LithoStratID = " + litho.getId() 
				+ " and co.CollectionMemberID = "
				+ AppContextMgr.getInstance().getClassObject(Collection.class).getId();
		return BasicSQLUtils.getCountAsInt(sql);
	}
	
    /**
     * @param taxon
     * @return
     */
    private RecordSet createColObjRSFromLitho(final LithoStrat litho)
    {
        RecordSet recordSet = new RecordSet();
        recordSet.initialize();
        recordSet.set("TTV", CollectionObject.getClassTableId(), RecordSet.GLOBAL);

        fillRecordSet(litho, recordSet);
        
        return recordSet;
    }

    /**
     * @param gtp
     * @param recordSet
     */
    protected void fillRecordSet(final LithoStrat gtp, final RecordSet recordSet)
    {
        // The old way using Hibernate relationships was too slow, 
        // so instead I am using straight SQL it is a lot faster.
        String sql = "SELECT DISTINCT co.CollectionObjectID FROM paleocontext pc "
        		+ "INNER JOIN collectionobject co ON co.PaleoContextID = pc.PaleoContextID " 
                + "WHERE pc.LithoStratID = " + gtp.getId() 
                +  " AND co.CollectionMemberID = COLMEMID";
        
        Vector<Integer> list = new Vector<Integer>();
        
        fillListWithIds(sql, list);
        
        for (Integer id : list)
        {
            recordSet.addItem(id);
        }
    }

    
}
