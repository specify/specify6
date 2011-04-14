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
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace.CreationMode;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.tasks.TreeTaskMgr;
import edu.ku.brc.ui.GetSetValueIFace;

/**
 * A business rules class that handles various safety checking and housekeeping tasks
 * that must be performed when editing {@link Taxon} or
 * {@link TaxonTreeDefItem} objects.
 *
 * @author jstewart
 * 
 * @code_status Beta
 */
public class TaxonBusRules extends BaseTreeBusRules<Taxon, TaxonTreeDef, TaxonTreeDefItem>
{
    protected static final String PARENT = "parent";
    protected static final String RANK = "definitionItem";
    protected static final String HYBRIDPARENT1 = "hybridParent1";
    protected static final String HYBRIDPARENT2 = "hybridParent2";
    protected static final String IS_HYBRID     = "isHybrid";
    
    protected AttachmentOwnerBaseBusRules attachOwnerRules;
    
    /**
     * Constructor.
     */
    public TaxonBusRules()
    {
        super(Taxon.class,TaxonTreeDefItem.class);
        
        attachOwnerRules = new AttachmentOwnerBaseBusRules()
        {
            @Override
            public boolean okToEnableDelete(Object dataObj)
            {
                return false;
            }
        };
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#getNodeClass()
     */
    protected Class<?> getNodeClass()
    {
    	return Taxon.class;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#initialize(edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        Component fishBaseWL = formViewObj.getControlById("WebLink");
        if (fishBaseWL != null && !Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.fish))
        {
            fishBaseWL.setVisible(false);
        }
        
        // TODO: the form system MUST require the hybridParent1 and hybridParent2 widgets to be present if the isHybrid checkbox is present
        final JCheckBox        hybridCheckBox = (JCheckBox)formViewObj.getControlByName(IS_HYBRID);
        final GetSetValueIFace hybrid1Widget  = (GetSetValueIFace)formViewObj.getControlByName(HYBRIDPARENT1);
        final GetSetValueIFace hybrid2Widget  = (GetSetValueIFace)formViewObj.getControlByName(HYBRIDPARENT2);
        
        if (hybridCheckBox != null)
        {
            hybridCheckBox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    if (!hybridCheckBox.isSelected())
                    {
                        hybrid1Widget.setValue(null, null);
                        hybrid2Widget.setValue(null, null);
                    }
                }
            });
        }
        
        TreeTaskMgr.checkLocks();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#getDeleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(Object dataObj)
    {
        return getLocalizedMessage("TAXON_DELETE", ((Taxon)dataObj).getName());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        if (dataObj instanceof Taxon)
        {
            Taxon taxon = (Taxon)dataObj;
            int citCnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM taxoncitation WHERE TaxonID = " + taxon.getId());
            if (citCnt < 1)
            {
                return super.okToDeleteNode(taxon);
            }
        }
        else if (dataObj instanceof TaxonTreeDefItem)
        {
            return okToDeleteDefItem((TaxonTreeDefItem)dataObj);
        }
        
        return false;
    }
    
    /**
     * Handles the {@link #okToEnableDelete(Object)} method in the case that the passed in
     * {@link Object} is an instance of {@link TaxonTreeDefItem}.
     * 
     * @param defItem the {@link TaxonTreeDefItem} being inspected
     * @return true if the passed in item is deletable
     */
    public boolean okToDeleteDefItem(TaxonTreeDefItem defItem)
    {
        reasonList.clear();
        
       // never let the root level be deleted
        if (defItem.getRankId() == 0)
        {
            return false;
        }
        
        // don't let 'used' levels be deleted
        if (!okToDelete("taxon", "TaxonTreeDefItemID", defItem.getId()))
        {
            return false;
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean afterSaveCommit(final Object dataObj, final DataProviderSessionIFace session)
    {
        setLSID((FormDataObjIFace)dataObj);

        return super.afterSaveCommit(dataObj, session);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#getExtraWhereColumns(edu.ku.brc.af.core.db.DBTableInfo)
     */
    @Override
    protected String getExtraWhereColumns(final DBTableInfo tableInfo)
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#getRelatedTableAndColumnNames()
     */
    @Override
    public String[] getRelatedTableAndColumnNames()
    {
        String[] relationships = 
        {
                "determination", "TaxonID",
                "collectingeventattribute", "HostTaxonID",
                
                //allow cascade deletes for citations
                //"taxoncitation", "TaxonID", 
                
                "taxon",         "HybridParent1ID",
                "taxon",         "HybridParent2ID",
                "taxon",         "AcceptedID"
        };

        return relationships;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#getAllRelatedTableAndColumnNames()
     */
    @Override
	public String[] getAllRelatedTableAndColumnNames() 
    {
    	//XXX probably better to use DBTableInfo for this? (trickiness with PreferredTaxonID).
    	String[] relationships = 
        {
                "determination", "TaxonID",
                "determination", "PreferredTaxonID",
                "collectingeventattribute", "HostTaxonID",
                "taxoncitation", "TaxonID", 
                "taxon",         "HybridParent1ID",
                "taxon",         "HybridParent2ID",
                "taxon",         "AcceptedID"
        };

        return relationships;
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        // make sure to handle all of the attachment stuff
        attachOwnerRules.beforeSave(dataObj, session);
        
        super.beforeSave(dataObj, session);
        
        if (dataObj instanceof Taxon)
        {
            Taxon taxon = (Taxon)dataObj;
            beforeSaveTaxon(taxon, session);

            // this might not do anything (if no names need to be changed)
            super.updateFullNamesIfNecessary(taxon, session);
            
            return;
        }
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link Taxon}.  The real work of this method is to
     * update the 'fullname' field of all {@link Taxon} objects effected by the changes
     * to the passed in {@link Taxon}.
     * 
     * @param taxon the {@link Taxon} being saved
     */
    protected void beforeSaveTaxon(Taxon taxon, DataProviderSessionIFace session)
    {
        // if this node is "accepted" then make sure it doesn't point to an accepted parent
        if (taxon.getIsAccepted() == null || taxon.getIsAccepted().booleanValue() == true)
        {
            taxon.setAcceptedTaxon(null);
        }
        
        // if this node isn't a hybrid then make sure it doesn't point at hybrid "parents"
        if (taxon.getIsHybrid() == null || taxon.getIsHybrid().booleanValue() == false)
        {
            taxon.setHybridParent1(null);
            taxon.setHybridParent2(null);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeDeleteCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeDeleteCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        // make sure to handle all of the attachment stuff
        boolean retVal = attachOwnerRules.beforeDeleteCommit(dataObj, session);
        if (retVal == false)
        {
            return retVal;
        }
        
        retVal = super.beforeDeleteCommit(dataObj, session);
        return retVal;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#beforeSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeSaveCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        // make sure to handle all of the attachment stuff
        boolean retVal = attachOwnerRules.beforeSaveCommit(dataObj, session);
        if (retVal == false)
        {
            return retVal;
        }
        
        retVal = super.beforeSaveCommit(dataObj, session);
        return retVal;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#canAccessSynonymy(java.lang.Object)
     */
    @Override
    protected boolean canAccessSynonymy(Taxon dataObj)
    {
        return false;
    }

    
    /* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#canAccessSynonymy(edu.ku.brc.specify.datamodel.Treeable, int)
	 */
	@Override
	protected boolean canAccessSynonymy(Taxon dataObj, int rank)
	{
		return false;
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (formViewObj.getAltView().getMode() == CreationMode.EDIT &&
            formViewObj.getDataObj() instanceof Taxon)
        {
            Taxon nodeInForm = (Taxon)formViewObj.getDataObj();
            
            // TODO: the form system MUST require the hybridParent1 and hybridParent2 widgets to be present if the isHybrid checkbox is present
            JCheckBox hybridCheckBox = (JCheckBox)formViewObj.getControlByName(IS_HYBRID);
            
            Component hybridParent1Comp = formViewObj.getControlByName(HYBRIDPARENT1);
            if (hybridParent1Comp instanceof ValComboBoxFromQuery)
            {
                ValComboBoxFromQuery hybrid1Widget  = (ValComboBoxFromQuery)hybridParent1Comp;
                ValComboBoxFromQuery hybrid2Widget  = (ValComboBoxFromQuery)formViewObj.getControlByName(HYBRIDPARENT2);
                
                if (hybridCheckBox != null && nodeInForm != null)
                {
                    //XXX TaxonSearchBuilder will still allow both hybrid parents to be the same.
                    hybrid1Widget.registerQueryBuilder(new TreeableSearchQueryBuilder(nodeInForm, null, TreeableSearchQueryBuilder.HYBRID_PARENT));
                    hybrid2Widget.registerQueryBuilder(new TreeableSearchQueryBuilder(nodeInForm, null, TreeableSearchQueryBuilder.HYBRID_PARENT));
                }
            }
            //formViewObj.getValidator().setUIValidatorsToNotChanged();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        
        TreeTaskMgr.checkLocks();
    }
}
