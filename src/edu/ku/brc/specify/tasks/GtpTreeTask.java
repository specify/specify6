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
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.busrules.GeologicTimePeriodBusRules;
import edu.ku.brc.specify.ui.treetables.TreeNodePopupMenu;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIRegistry;

/**
 * Task that handles the UI for viewing geologic time period data.
 * 
 * @code_status Beta
 * @author jstewart
 */
public class GtpTreeTask extends BaseTreeTask<GeologicTimePeriod,GeologicTimePeriodTreeDef,GeologicTimePeriodTreeDefItem>
{
	public static final String GTP = "GeoTimePeriodTree";
	
	/**
	 * Constructor.
	 */
	public GtpTreeTask()
	{
        super(GTP, getResourceString(GTP));
        
        treeClass        = GeologicTimePeriod.class;
        treeDefClass     = GeologicTimePeriodTreeDef.class;
        
        commandTypeString = GTP;
        
        businessRules = new GeologicTimePeriodBusRules();
        
        initialize();
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTreeTask#getCurrentTreeDef()
     */
    @Transient
    @Override
    protected GeologicTimePeriodTreeDef getCurrentTreeDef()
    {
        return (GeologicTimePeriodTreeDef )((SpecifyAppContextMgr )AppContextMgr.getInstance()).getTreeDefForClass(GeologicTimePeriod.class);
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
	protected TreeTableViewer<GeologicTimePeriod, GeologicTimePeriodTreeDef, GeologicTimePeriodTreeDefItem> createTreeViewer(
			String titleArg, boolean isEditMode) {
		// TODO Auto-generated method stub
		final TreeTableViewer<GeologicTimePeriod, GeologicTimePeriodTreeDef, GeologicTimePeriodTreeDefItem> ttv = 
				super.createTreeViewer(titleArg, isEditMode);
		if (ttv != null) {
            final TreeNodePopupMenu popup = ttv.getPopupMenu();
            JMenuItem getCos = new JMenuItem(getResourceString("TTV_ASSOC_COS"));
            getCos.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    GeologicTimePeriod gtpNode = ttv.getSelectedNode(popup.getList());

                    // this call initializes all of the linked objects
                    // it only initializes the immediate links, not objects that are multiple hops away
                    //ttv.initializeNodeAssociations(taxon);
                    
                    if (getColObjCount(gtpNode) == 0)
                    {
                        UIRegistry.displayInfoMsgDlgLocalized("TTV_NO_COS_FOR_NODE", 
                        		DBTableIdMgr.getInstance().getTitleForId(GeologicTimePeriod.getClassTableId()));
                        UIRegistry.getStatusBar().setLocalizedText("TTV_NO_COS_FOR_NODE", 
                        		DBTableIdMgr.getInstance().getTitleForId(GeologicTimePeriod.getClassTableId()));
                        return;
                    }

                   final RecordSet recordSet = createColObjRSFromGtp(gtpNode);

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
	 * @param gtp
	 * @return
	 */
	int getColObjCount(GeologicTimePeriod gtp) {
		String pcChild = AppContextMgr.getInstance().getClassObject(Discipline.class).getPaleoContextChildTable();
		String sql = "select count(*) from paleocontext pc inner join ";
		if ("locality".equalsIgnoreCase(pcChild)) {
			sql += "locality loc on loc.PaleoContextID = pc.PaleoContextID "
					+ "inner join collectingevent ce on ce.LocalityID = loc.LocalityID "
					+ "inner join collectionobject co on co.CollectingEventID = ce.CollectingEventID ";
		} else if ("collectingevent".equalsIgnoreCase(pcChild)) {
			sql += "collectingevent ce on ce.PaleoContextID = pc.PaleoContextID "
					+ "inner join collectionobject co on co.CollectingEventID = ce.CollectingEventID ";

		} else {
			sql += "collectionobject co on co.PaleoContextID = pc.PaleoContextID ";
		}
		sql += "where (pc.ChronosStratID = " + gtp.getId() 
				+ " or pc.ChronosStratEndID=" + gtp.getId()
				+ " or pc.BioStratID = " + gtp.getId() + ") and co.CollectionMemberID = "
				+ AppContextMgr.getInstance().getClassObject(Collection.class).getId();
		return BasicSQLUtils.getCountAsInt(sql);
	}
	
    /**
     * @param taxon
     * @return
     */
    private RecordSet createColObjRSFromGtp(final GeologicTimePeriod gtpObj)
    {
        RecordSet recordSet = new RecordSet();
        recordSet.initialize();
        recordSet.set("TTV", CollectionObject.getClassTableId(), RecordSet.GLOBAL);

        fillRecordSet(gtpObj, recordSet);
        
        return recordSet;
    }

    /**
     * @param gtp
     * @param recordSet
     */
    protected void fillRecordSet(final GeologicTimePeriod gtp, final RecordSet recordSet)
    {
        // The old way using Hibernate relationships was too slow, 
        // so instead I am using straight SQL it is a lot faster.
//        String sql = "SELECT DISTINCT co.CollectionObjectID FROM paleocontext pc "
//        		+ "INNER JOIN collectionobject co ON co.PaleoContextID = pc.PaleoContextID " 
//                + "WHERE (pc.ChronosStratID = " + gtp.getId()+ " or pc.BioStratID = " + gtp.getId() 
//                +  ") AND co.CollectionMemberID = COLMEMID";
// 
        
		String pcChild = AppContextMgr.getInstance().getClassObject(Discipline.class).getPaleoContextChildTable();
		String sql = "select DISTINCT co.CollectionObjectID from paleocontext pc inner join ";
		if ("locality".equalsIgnoreCase(pcChild)) {
			sql += "locality loc on loc.PaleoContextID = pc.PaleoContextID "
					+ "inner join collectingevent ce on ce.LocalityID = loc.LocalityID "
					+ "inner join collectionobject co on co.CollectingEventID = ce.CollectingEventID ";
		} else if ("collectingevent".equalsIgnoreCase(pcChild)) {
			sql += "collectingevent ce on ce.PaleoContextID = pc.PaleoContextID "
					+ "inner join collectionobject co on co.CollectingEventID = ce.CollectingEventID ";

		} else {
			sql += "collectionobject co on co.PaleoContextID = pc.PaleoContextID ";
		}
		sql += "where (pc.ChronosStratID = " + gtp.getId() 
				+ " or pc.ChronosStratEndID=" + gtp.getId()
				+ " or pc.BioStratID = " + gtp.getId() + ") and co.CollectionMemberID = "
				+ AppContextMgr.getInstance().getClassObject(Collection.class).getId();
        
        
        Vector<Integer> list = new Vector<Integer>();
        
        fillListWithIds(sql, list);
        
        for (Integer id : list)
        {
            recordSet.addItem(id);
        }
    }

    
}
