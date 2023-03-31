/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.expresssearch.ExpressResultsTableInfo;
import edu.ku.brc.af.core.expresssearch.ExpressSearchConfigCache;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace;
import edu.ku.brc.af.ui.forms.ViewFactory;
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
import edu.ku.brc.specify.datamodel.busrules.TableSearchResults;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

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
    
    protected ValComboBoxFromQuery   cbx           = null;
    protected JTextField             text         = null;
    protected String                 relName       = null;
    protected Integer                hostCollId    = null;
    protected Discipline             rightsideDiscipline = null;
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

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
        if (cbx != null)
        {
            cbx.setEnabled(enabled);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#initialize(java.util.Properties, boolean)
     */
    @Override
    public void initialize(Properties propertiesArg, boolean isViewModeArg)
    {
        super.initialize(propertiesArg, isViewModeArg);
        
        CellConstraints cc = new CellConstraints();

        String errMsg = null;
        
        relName = propertiesArg.getProperty("relname", null);
        if (StringUtils.isNotEmpty(relName))
        {
            Collection curCollection = AppContextMgr.getInstance().getClassObject(Collection.class);
            
            String sql = String.format("SELECT RightSideCollectionID FROM collectionreltype WHERE Name = \"%s\" AND LeftSideCollectionID = %d", relName,  curCollection.getId());
            System.err.println(sql);
            hostCollId = BasicSQLUtils.getCount(sql);
            System.err.println("hostCollId: "+hostCollId+"   curColId: "+curCollection.getId());
            
            DataProviderSessionIFace session = null;
            try
            {
                session    = DataProviderFactory.getInstance().createSession();
                colRelType = session.getData(CollectionRelType.class, "name", relName, DataProviderSessionIFace.CompareType.Equals);
                if (colRelType != null)
                {
                    leftSideCol  = colRelType.getLeftSideCollection();
                    rightSideCol = colRelType.getRightSideCollection();
                }
                
                if (rightSideCol != null)
                {
                    hostCollId = rightSideCol.getId();
                    System.err.println("2hostCollId: "+hostCollId+"   curColId: "+curCollection.getId());
                    if (hostCollId != null)
                    {
                        rightsideDiscipline = rightSideCol.getDiscipline();
                        if (rightsideDiscipline != null)
                        {
                            taxonTreeDef = rightsideDiscipline.getTaxonTreeDef();
                        } else
                        {
                            errMsg = UIRegistry.getLocalizedMessage("HostTaxonPlugin.ERR_MSG_RSD", relName);
                        }                            
                    } else
                    {
                        errMsg = UIRegistry.getLocalizedMessage("HostTaxonPlugin.ERR_BAD_NM", relName, curCollection.getCollectionName());
                    }
                } else
                {
                    errMsg = UIRegistry.getLocalizedMessage("HostTaxonPlugin.ERR_MSG_RSC", relName);
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                errMsg = UIRegistry.getLocalizedMessage("HostTaxonPlugin.ERR");
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        
            if (hostCollId != null && rightsideDiscipline != null)
            {
                if (isViewMode)
                {
                    PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g",  "p"), this);
                    text = UIHelper.createTextField("");
                    ViewFactory.changeTextFieldUIForDisplay(text, false);
                    pb.add(text, cc.xy(1, 1));
                    
                } else
                {
                    PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g",  "f:p:g"), this);
                    
                    // Switching to Search only for Bug 8393 - rods 6/4/11 (6.3.00 release)
                    //int btnOpts = ValComboBoxFromQuery.CREATE_EDIT_BTN | ValComboBoxFromQuery.CREATE_NEW_BTN | ValComboBoxFromQuery.CREATE_SEARCH_BTN;
                    int btnOpts = ValComboBoxFromQuery.CREATE_SEARCH_BTN;
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
                    
                    cbx.registerQueryBuilder(createSearchQueryBuilder());
                    
                    cbx.addListSelectionListener(new ListSelectionListener() {
                        public void valueChanged(ListSelectionEvent e)
                        {
                            itemSelected();
                        }
                    });
                }
            }
        } else
        {
            errMsg = UIRegistry.getLocalizedMessage("HostTaxonPlugin.ERR_MSG_REL_EMPTY");
        }
        
        if (errMsg != null)
        {
            PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g",  "p"), this);
            JButton btn = UIHelper.createI18NButton("HostTaxonPlugin.ERR_CLICK");
            pb.add(btn, cc.xy(1, 1));
            
            final String dlgErrMsg = errMsg;
            btn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    UIRegistry.showError(dlgErrMsg);
                }
            });
        }
    }
    
    
    /**
     * @return
     */
    protected ViewBasedSearchQueryBuilderIFace createSearchQueryBuilder()
    {
        return new ViewBasedSearchQueryBuilderIFace()
        {
            /* (non-Javadoc)
             * @see edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace#buildSQL(java.lang.String, boolean)
             */
            @Override
            public String buildSQL(String searchText, boolean isForCount)
            {
                String cols = isForCount ? "COUNT(*)" : "tx.fullName, tx.id";
                String sql = String.format("SELECT %s FROM Taxon tx INNER JOIN tx.definition ttd WHERE ttd.id = %d AND LOWER(tx.fullName) LIKE '%c%s%c' ORDER BY tx.fullName", 
                                           cols, taxonTreeDef.getId(), '%',searchText, '%');
                
                //System.out.println("adjustSQLTemplate: "+sql.toString());
                return sql;
            }

            /* (non-Javadoc)
             * @see edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace#buildSQL(java.util.Map, java.util.List)
             */
            @Override
            public String buildSQL(Map<String, Object> dataMap, List<String> fieldNames)
            {
                String orderBy = "";
                String fullName = (String)dataMap.get("taxon.FullName");
                if (StringUtils.isNotEmpty(fullName))
                {
                    fullName = StringUtils.remove(fullName, '#');
                    fullName = StringUtils.remove(fullName, '*');
                    if (StringUtils.isNotEmpty(fullName))
                    {
                        orderBy = "FullName";
                        fullName = String.format("LOWER(FullName) LIKE '%c%s%c'", '%', fullName.toLowerCase(), '%');  
                    }
                }
                
                String common = (String)dataMap.get("taxon.CommonName");
                if (StringUtils.isNotEmpty(common))
                {
                    common = StringUtils.remove(common, '#');
                    common = StringUtils.remove(common, '*');
                    if (StringUtils.isNotEmpty(common))
                    {
                        common = (StringUtils.isNotEmpty(fullName) ? " OR " : "") + String.format("LOWER(CommonName) LIKE '%c%s%c'", '%', common.toLowerCase(), '%');
                        if (StringUtils.isEmpty(orderBy))
                        {
                            orderBy = "CommonName";
                        }
                    }
                }
                
                if ("".equals(orderBy)) {
                	orderBy = "FullName";
                }
                String where = (fullName == null ? "" : fullName) + " " + (common == null ? "" : common);
                if (!"".equals(where.trim())) {
                	where = " AND (" + where + ")";
                } else {
                	where = "";
                }
                String sql = String.format("SELECT TaxonID, FullName, CommonName FROM taxon tx INNER JOIN taxontreedef ttd ON tx.TaxonTreeDefID = ttd.TaxonTreeDefID WHERE ttd.TaxonTreeDefID = %d %s ORDER BY %s", 
                                           taxonTreeDef.getId(), where, orderBy);
                //System.out.println(sql);
                return sql;
            }

            /* (non-Javadoc)
             * @see edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace#createQueryForIdResults()
             */
            @Override
            public QueryForIdResultsIFace createQueryForIdResults()
            {
                ExpressResultsTableInfo esTblInfo = ExpressSearchConfigCache.getTableInfoByName("TaxonSearch");
                return new TableSearchResults(DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId()), esTblInfo.getCaptionInfo()); //true => is HQL
            }
            
        };
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {"hostTaxon"};
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
        
        boolean clear = true;
        if (value instanceof CollectingEventAttribute)
        {
            CollectingEventAttribute cea = (CollectingEventAttribute)value;
            if (cea.getHostTaxon() != null)
            {
                clear = false;
                if (text != null)
                {
                    text.setText(cea.getHostTaxon().getFullName() != null ? cea.getHostTaxon().getFullName() : cea.getHostTaxon().getName());
                    
                } else if (cbx != null)
                {
                    cbx.setValue(cea.getHostTaxon(), null);
                    cbx.getTextWithQuery().setSelectedId(cea.getHostTaxon().getId());
                }
            }
        }
        
        if (clear)
        {
            if (text != null)
            {
                text.setText("");
                
            } else if (cbx != null)
            {
                cbx.setValue(null, null);
            } else
            {
                System.err.println("cbx and text field were null");
            }
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
