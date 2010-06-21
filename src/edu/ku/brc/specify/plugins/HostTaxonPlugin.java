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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
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
            //System.err.println(sql);
            hostCollId = BasicSQLUtils.getCount(sql);
            
            if (hostCollId != null)
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session             = DataProviderFactory.getInstance().createSession();
                    Collection rightCol = session.get(Collection.class, hostCollId);
                    if (rightCol != null)
                    {
                        rightsideDiscipline = rightCol.getDiscipline();
                        if (rightsideDiscipline != null)
                        {
                            taxonTreeDef = rightsideDiscipline.getTaxonTreeDef();
                        } else
                        {
                            errMsg = UIRegistry.getLocalizedMessage("HostTaxonPlugin.ERR_MSG_RSD", relName);
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
            } else
            {
                errMsg = UIRegistry.getLocalizedMessage("HostTaxonPlugin.ERR_BAD_NM", relName, curCollection.getCollectionName());
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
