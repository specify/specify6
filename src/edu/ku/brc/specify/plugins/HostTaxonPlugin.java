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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.NotImplementedException;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttribute;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionRelType;
import edu.ku.brc.specify.datamodel.CollectionRelationship;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 3, 2008
 *
 */
public class HostTaxonPlugin extends UIPluginBase
{
    protected boolean                isLeftSide    = false;
    protected CollectionRelType      colRelType    = null;
    protected Collection             leftSideCol   = null;
    protected Collection             rightSideCol  = null;
    
    protected CollectionRelationship collectionRel = null;
    protected CollectionObject       otherSide     = null;
    
    protected ValComboBoxFromQuery   cbx;
    protected String                 relName       = null;
    protected Integer                hostCollId    = null;
    protected Discipline             discipline    = null;
    protected TaxonTreeDef           taxonTreeDef  = null;
    protected Taxon                  taxon         = null;
    
    /**
     * 
     */
    public HostTaxonPlugin()
    {
        super();
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
    protected void adjustSQLTemplate()
    {
        StringBuilder sql = new StringBuilder("SELECT %s1 FROM Taxon tx inner join tx.definition ttd WHERE ttd.id = ");
        sql.append(taxonTreeDef.getId());
        sql.append(" AND %s2");
        System.out.println(sql.toString());
        cbx.setSqlTemplate(sql.toString());
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#initialize(java.util.Properties, boolean)
     */
    @Override
    public void initialize(Properties propertiesArg, boolean isViewModeArg)
    {
        super.initialize(propertiesArg, isViewModeArg);
        
        relName = propertiesArg.getProperty("relname", null);
        if (relName != null)
        {
            Collection curCollection = AppContextMgr.getInstance().getClassObject(Collection.class);
            
            String sql = String.format("SELECT RightSideCollectionID FROM collectionreltype WHERE Name = \"%s\" AND LeftSideCollectionID = %d", relName,  curCollection.getId());
            System.err.println(sql);
            hostCollId = BasicSQLUtils.getCount(sql);
            
            if (hostCollId != null)
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session    = DataProviderFactory.getInstance().createSession();
                    Collection rightCol = session.get(Collection.class, hostCollId);
                    //discipline = session.get(Discipline.class, rightCol.getDiscipline().getId());
                    discipline   = rightCol.getDiscipline();
                    taxonTreeDef = discipline.getTaxonTreeDef();
                    
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
            }
        }
        
        if (discipline != null)
        {
            CellConstraints cc = new CellConstraints();
            PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g",  "f:p:g"), this);
            
            int btnOpts = ValComboBoxFromQuery.CREATE_EDIT_BTN | ValComboBoxFromQuery.CREATE_NEW_BTN | ValComboBoxFromQuery.CREATE_SEARCH_BTN;
            cbx = new ValComboBoxFromQuery(DBTableIdMgr.getInstance().getInfoById(Taxon.getClassTableId()),
                                    "fullName",
                                    "fullName",
                                    "fullName",
                                    "%s",
                                    null,
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
        }
    }
    
    /**
     * 
     */
    protected void itemSelected()
    {
        notifyChangeListeners(new ChangeEvent(this));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(Object value, String defaultValue)
    {
        super.setValue(value, defaultValue);
        
        if (value instanceof CollectingEventAttribute)
        {
            CollectingEventAttribute cea = (CollectingEventAttribute)value;
            System.err.println(cea.getHostTaxon());
            if (cea.getHostTaxon() != null)
            {
                cbx.setValue(cea.getHostTaxon(), null);
                cbx.getTextWithQuery().setSelectedId(cea.getHostTaxon().getId());
            }
            //cbx.setValue(cea.getHostTaxon(), null);
        }
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            CollectingEvent ce = session.get(CollectingEvent.class, 9);
            if (ce != null)
            {
                if (ce.getCollectingEventAttribute() != null)
                {
                    System.err.print("["+ce.getCollectingEventAttribute().getId()+"]  ");
                    System.err.println("["+ce.getCollectingEventAttribute().getHostTaxon()+"]");
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ValComboBoxFromQuery.class, ex);
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#getValue()
     */
    @Override
    public Object getValue()
    {
        if (fvo != null)
        {
            Object data = fvo.getCurrentDataObj();
            if (data instanceof CollectingEventAttribute)
            {
                CollectingEventAttribute cea = (CollectingEventAttribute)data;
                //System.err.println(cea.getHostTaxon());
                cea.setHostTaxon((Taxon)cbx.getValue());
            }
        }
        return null;
    }
    
}
