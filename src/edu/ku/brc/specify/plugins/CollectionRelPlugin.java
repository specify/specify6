/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.plugins;

import java.util.Properties;
import java.util.Set;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionRelType;
import edu.ku.brc.specify.datamodel.CollectionRelationship;
import edu.ku.brc.ui.forms.validation.ValComboBoxFromQuery;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 3, 2008
 *
 */
public class CollectionRelPlugin extends UIPluginBase
{
    protected boolean              isLeftSide   = false;
    protected CollectionRelType    colRelType   = null;
    protected Collection           leftSideCol  = null;
    protected Collection           rightSideCol = null;
    
    protected CollectionRelationship collectionRel = null;
    protected CollectionObject       otherSide     = null;
    
    protected ValComboBoxFromQuery cbx;
    
    /**
     * 
     */
    public CollectionRelPlugin()
    {
        super();
        
        // Temp
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        colRelType   = (CollectionRelType)tmpSession.load(CollectionRelType.class, 1);
        leftSideCol  = colRelType.getLeftSideCollection();
        rightSideCol = colRelType.getRightSideCollection();
        colRelType.getRelationships().size();
        tmpSession.close();
        
        isLeftSide = AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionId().equals(leftSideCol.getCollectionId());

        
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("p",  "p"), this);
        
        
        int btnOpts = ValComboBoxFromQuery.CREATE_EDIT_BTN | ValComboBoxFromQuery.CREATE_NEW_BTN | ValComboBoxFromQuery.CREATE_SEARCH_BTN;
        cbx = new ValComboBoxFromQuery(DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId()),
                                "catalogNumber",
                                "catalogNumber",
                                "catalogNumber",
                                null,
                                "CatalogNumber",
                                null,
                                "",
                                btnOpts);
        pb.add(cbx, cc.xy(1, 1));
        
        adjustSQLTemplate();
        
        cbx.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                itemSelected();
            }
        });
    }
    
    /**
     * 
     */
    protected void itemSelected()
    {
        CollectionObject newColObj = (CollectionObject)cbx.getValue();
        CollectionObject curColObj = (CollectionObject)data;
        
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        tmpSession.attach(newColObj);
        
        boolean isNew = false;
        if (collectionRel == null)
        {
            collectionRel = new CollectionRelationship();
            collectionRel.initialize();
            collectionRel.setCollectionRelType(colRelType);
            colRelType.getRelationships().add(collectionRel);
            isNew = true;
        }
        
        if (isLeftSide)
        {
            collectionRel.setLeftSide(curColObj);
            collectionRel.setRightSide(newColObj);
            if (isNew)
            {
                curColObj.getLeftSideRels().add(collectionRel);
                newColObj.getRightSideRels().add(collectionRel);
            }
        } else
        {
            collectionRel.setLeftSide(newColObj);
            collectionRel.setRightSide(curColObj);
            if (isNew)
            {
                newColObj.getLeftSideRels().add(collectionRel);
                curColObj.getRightSideRels().add(collectionRel);
            }
        }
        
        tmpSession.close();
    }
    
    /**
     * 
     */
    protected void adjustSQLTemplate()
    {
        StringBuilder sql = new StringBuilder("SELECT %s1 FROM CollectionObject co ");
        sql.append("WHERE co.collectionMemberId = ");
        sql.append(isLeftSide ? rightSideCol.getCollectionId() : leftSideCol.getCollectionId());
        sql.append(" AND %s2");
        //System.out.println(sql.toString());
        cbx.setSqlTemplate(sql.toString());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#initialize(java.util.Properties, boolean)
     */
    @Override
    public void initialize(Properties propertiesArg, boolean isViewModeArg)
    {
        super.initialize(propertiesArg, isViewModeArg);
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(Object value, String defaultValue)
    {
        super.setValue(value, defaultValue);
        
        if (value instanceof CollectionObject)
        {
            otherSide = null;
            
            CollectionObject colObj = (CollectionObject)value;
            
            Set<CollectionRelationship> rels = isLeftSide ? colObj.getLeftSideRels() : colObj.getRightSideRels();

            for (CollectionRelationship colRel : rels)
            {
                if (colRel.getCollectionRelType().equals(colRelType))
                {
                    collectionRel = colRel;
                    otherSide = isLeftSide ? colRel.getRightSide() : colRel.getLeftSide();
                    break;
                }
            }
            
            cbx.setValue(otherSide, null);
        }
    }
    
}
