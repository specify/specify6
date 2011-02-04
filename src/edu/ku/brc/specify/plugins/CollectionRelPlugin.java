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
package edu.ku.brc.specify.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionRelType;
import edu.ku.brc.specify.datamodel.CollectionRelationship;

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
    protected boolean                isLeftSide    = false;
    protected CollectionRelType      colRelType    = null;
    protected Collection             leftSideCol   = null;
    protected Collection             rightSideCol  = null;
    
    protected CollectionRelationship collectionRel = null;
    protected CollectionObject       otherSide     = null;
    
    protected ValComboBoxFromQuery   cbx;
    
    /**
     * 
     */
    public CollectionRelPlugin()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#initialize(java.util.Properties, boolean)
     */
    @Override
    public void initialize(Properties propertiesArg, boolean isViewModeArg)
    {
        super.initialize(propertiesArg, isViewModeArg);
        
        String relName = propertiesArg.getProperty("relname");
        if (StringUtils.isNotEmpty(relName))
        {
            DataProviderSessionIFace tmpSession = null;
            try
            {
                tmpSession   = DataProviderFactory.getInstance().createSession();
                colRelType   = tmpSession.getData(CollectionRelType.class, "name", relName, DataProviderSessionIFace.CompareType.Equals);
                if (colRelType != null)
                {
                    leftSideCol  = colRelType.getLeftSideCollection();
                    rightSideCol = colRelType.getRightSideCollection();
                    colRelType.getRelationships().size();
                    
                    isLeftSide = AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionId().equals(leftSideCol.getCollectionId());
                    
                } else
                {
                    // RelName not found.
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                
            } finally
            {
                if (tmpSession != null)
                {
                    tmpSession.close();
                }
            }
            
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
                                    null, // helpContext
                                    btnOpts);
            pb.add(cbx, cc.xy(1, 1));
            
            adjustSQLTemplate();
            
            cbx.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e)
                {
                    itemSelected();
                }
            });
            
        } else
        {
            // no Relationship name
        }
             
    }
    
    /**
     * @return will return a list or empty list, but not NULL
     */
    @SuppressWarnings("unchecked")
    public static List<CollectionRelationship> getCollectionRelationships()
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            return (List<CollectionRelationship>)session.getDataList("FROM CollectionRelationship");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        return new ArrayList<CollectionRelationship>();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        throw new NotImplementedException("isNotEmpty not implement!");
    }
    
    /**
     * 
     */
    protected void itemSelected()
    {
        CollectionObject newColObj = (CollectionObject)cbx.getValue();
        CollectionObject curColObj = (CollectionObject)dataObj;
        
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
                if (colRel.getCollectionRelType().getId().equals(colRelType.getId()))
                {
                    collectionRel = colRel;
                    otherSide = isLeftSide ? colRel.getRightSide() : colRel.getLeftSide();
                    break;
                }
            }
            
            cbx.setValue(otherSide, null);
        }
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {"catalogNumber", "rightSide", "leftSide"};
    }
    
}
