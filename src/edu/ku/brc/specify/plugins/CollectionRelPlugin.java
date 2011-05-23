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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.ExpressResultsTableInfo;
import edu.ku.brc.af.core.expresssearch.ExpressSearchConfigCache;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.af.ui.db.TextFieldWithInfo;
import edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionRelType;
import edu.ku.brc.specify.datamodel.CollectionRelationship;
import edu.ku.brc.specify.datamodel.busrules.TableSearchResults;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Apr 3, 2008
 *
 */
public class CollectionRelPlugin extends UIPluginBase implements UIValidatable
{
    private final static String CATNUM_NAME    = "catalogNumber";
    private final static String CATNUM_NAMECAP = "CatalogNumber";
    private final static String COLOBJ_NAME    = "CollectionObject";

    private boolean                isLeftSide      = true;
    private CollectionRelType      colRelType      = null;
    private Collection             leftSideCol     = null;
    private Collection             rightSideCol    = null;
    
    private CollectionObject       currentColObj   = null;
    private CollectionRelationship collectionRel   = null;
    private CollectionObject       otherSideColObj = null;
    
    private ValComboBoxFromQuery   cbx             = null;
    private TextFieldWithInfo      textWithInfo    = null;
    private boolean                isRequired      = false;
    private UIFieldFormatterIFace  catNumFormatter = null;

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
    public void initialize(final Properties propertiesArg, final boolean isViewModeArg)
    {
        super.initialize(propertiesArg, isViewModeArg);
        
        String relName = propertiesArg.getProperty("relname");
        if (StringUtils.isNotEmpty(relName))
        {
            DataProviderSessionIFace tmpSession = null;
            try
            {
                tmpSession = DataProviderFactory.getInstance().createSession();
                colRelType = tmpSession.getData(CollectionRelType.class, "name", relName, DataProviderSessionIFace.CompareType.Equals);
                if (colRelType != null)
                {
                    leftSideCol  = colRelType.getLeftSideCollection();
                    rightSideCol = colRelType.getRightSideCollection();
                    colRelType.getRelationships().size();
                    rightSideCol.getCollectionId();
                    leftSideCol.getCollectionId();
                    
                    Collection currCollection = AppContextMgr.getInstance().getClassObject(Collection.class);
                    isLeftSide = currCollection.getId().equals(leftSideCol.getId());
                    
                    catNumFormatter = CollectionRelOneToManyPlugin.getCatNumFormatter(leftSideCol, rightSideCol);
                    
                } else
                {
                    DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(CollectionRelationship.getClassTableId());
                    UIRegistry.showError(String.format("The %s name '%s' doesn't exist (defined in the form for the plugin).", ti.getTitle(), relName));
                    
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
            
            DBTableInfo     coTI = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
            CellConstraints cc   = new CellConstraints();
            PanelBuilder    pb   = new PanelBuilder(new FormLayout("MAX(p;40px)", "p"), this);
            
            if (isViewModeArg || !isLeftSide)
            {
                textWithInfo = new TextFieldWithInfo(coTI.getClassName(),
                                                     CATNUM_NAME,    // id name
                                                     CATNUM_NAME,    // key name
                                                     null,           // format
                                                     CATNUM_NAMECAP, // uiFieldFormatterName
                                                     COLOBJ_NAME,    // dataObjFormatterName
                                                     COLOBJ_NAME,    // displayInfoDialogName
                                                     "");            // objTitle  
                pb.add(textWithInfo, cc.xy(1, 1));
                ViewFactory.changeTextFieldUIForDisplay(textWithInfo.getTextField(), false);
                
            } else
            {
                int btnOpts = ValComboBoxFromQuery.CREATE_VIEW_BTN | ValComboBoxFromQuery.CREATE_SEARCH_BTN;
                cbx = new ValComboBoxFromQuery(coTI,
                                                CATNUM_NAME,
                                                CATNUM_NAME,
                                                CATNUM_NAME,
                                                null,
                                                CATNUM_NAMECAP,
                                                COLOBJ_NAME,
                                                "",
                                                null, // helpContext
                                                btnOpts);
                pb.add(cbx, cc.xy(1, 1));
                
                cbx.setEnabled(colRelType != null);
                
                adjustSQLTemplate();
                
                cbx.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e)
                    {
                        itemSelected();
                        notifyChangeListeners(new ChangeEvent(CollectionRelPlugin.this));
                    }
                });
                
                cbx.registerQueryBuilder(createSearchQueryBuilder(isLeftSide, leftSideCol, rightSideCol));
            }
            
        } else
        {
            // no Relationship name
        }
    }
    
    /**
     * @return
     */
    protected static ViewBasedSearchQueryBuilderIFace createSearchQueryBuilder(final boolean isLeftSide, 
                                                                               final Collection leftSideCol, 
                                                                               final Collection rightSideCol)
    {
        return new ViewBasedSearchQueryBuilderIFace()
        {
            /* (non-Javadoc)
             * @see edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace#buildSQL(java.lang.String, boolean)
             */
            @Override
            public String buildSQL(String searchText, boolean isForCount)
            {
                Collection collection = !isLeftSide ? leftSideCol : rightSideCol;
                String cols = isForCount ? "COUNT(*)" : CATNUM_NAME+", collectionObjectId";
                String sql = String.format("SELECT %s FROM CollectionObject WHERE collectionMemberId = %d AND %s LIKE '%c%s%c' ORDER BY catalogNumber", 
                        cols, collection.getId(), CATNUM_NAME, '%',searchText, '%');
                //System.out.println(sql);
                return sql;
            }

            /* (non-Javadoc)
             * @see edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace#buildSQL(java.util.Map, java.util.List)
             */
            @Override
            public String buildSQL(Map<String, Object> dataMap, List<String> fieldNames)
            {
                
                Collection collection = !isLeftSide ? leftSideCol : rightSideCol;
                String catNum = (String)dataMap.get("CatalogNumber");
                catNum = StringUtils.remove(catNum, '#');
                String sql = String.format("SELECT CollectionObjectId, CatalogNumber FROM collectionobject WHERE CollectionMemberID = %d AND CatalogNumber LIKE '%c%s%c' ORDER BY catalogNumber", 
                        collection.getId(), '%', catNum, '%');
                //System.out.println(sql);
                return sql;
            }

            /* (non-Javadoc)
             * @see edu.ku.brc.af.ui.db.ViewBasedSearchQueryBuilderIFace#createQueryForIdResults()
             */
            @Override
            public QueryForIdResultsIFace createQueryForIdResults()
            {
                ExpressResultsTableInfo esTblInfo = ExpressSearchConfigCache.getTableInfoByName("CollectionObjectSearch");
                return new TableSearchResults(DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId()), esTblInfo.getCaptionInfo()); //true => is HQL
            }
            
        };
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
        return cbx.isNotEmpty();
    }
    
    /**
     * @param collectionRels
     * @param colRelToBeRemoved
     */
    public static  void removeFromCollectionRel(final Set<CollectionRelationship> collectionRels, 
                                                final CollectionRelationship colRelToBeRemoved)
    {
        for (CollectionRelationship colRel : collectionRels)
        {
            if (colRel == colRelToBeRemoved || 
                colRel.getId().equals(colRelToBeRemoved.getId()))
            {
                collectionRels.remove(colRel);
                break;
            }
        }
    }
    
    /**
     * 
     */
    private void itemSelected()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                itemSelectedInternal();
                
            }
        });
    }
    
    /**
     * @param dstCatNum
     * @return
     */
    public static String isColObjAlreadyUsedInDB(final int colRelId, final String dstCatNum)
    {
        String sql = "SELECT src.CatalogNumber FROM collectionobject AS src " +
        		     "Left Join collectionrelationship AS cr ON src.CollectionObjectID = cr.LeftSideCollectionID " +
        		     "Left Join collectionobject AS dst ON cr.RightSideCollectionID = dst.CollectionObjectID " +
        		     "WHERE cr.CollectionRelTypeID = %d AND dst.CatalogNumber = '%s'";
        sql = String.format(sql, colRelId, dstCatNum);
        return BasicSQLUtils.querySingleObj(sql);
    }
    
    /**
     * @param colRelId
     * @param dstId
     * @param dstCatNum
     * @param catNumFormatter
     * @return
     */
    public static boolean isColObjAlreadyUsed(final int colRelId, 
                                              final int dstId, 
                                              final String dstCatNum,
                                              final UIFieldFormatterIFace catNumFormatter)
    {
        final String catNum = isColObjAlreadyUsedInDB(colRelId, dstCatNum);
        if (catNum != null)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    String otherCatNum = dstCatNum;
                    if (StringUtils.isNotEmpty(otherCatNum))
                    {
                        UIFieldFormatterIFace fmt = CollectionRelOneToManyPlugin.getCatNumFormatter(dstId);
                        otherCatNum = fmt != null ? (String)fmt.formatToUI(otherCatNum) : otherCatNum;
                    }
                    UIRegistry.showLocalizedError("COLREL_USED_CO", otherCatNum, catNumFormatter.formatToUI(catNum));
                }
            });
            return true; // Is In Use (an error)
        }
        return false; // Not In Use
    }
    
    /**
     * 
     */
    private void itemSelectedInternal()
    {
        final CollectionObject newOtherSide = (CollectionObject)cbx.getValue();
        if (newOtherSide != null)
        {
            // Make sure this no one is already pointing at it.
            if (isColObjAlreadyUsed(colRelType.getId(), rightSideCol.getId(), newOtherSide.getCatalogNumber(), catNumFormatter))
            {
                cbx.setValue(null, null);
                return;
            }
            
            DataProviderSessionIFace tmpSession = null;
            try
            {
                tmpSession = DataProviderFactory.getInstance().createSession();
                tmpSession.attach(newOtherSide);
                
                if (collectionRel == null && colRelType != null)
                {
                    collectionRel = new CollectionRelationship();
                    collectionRel.initialize();
                    collectionRel.setCollectionRelType(colRelType);
                    colRelType.getRelationships().add(collectionRel);
                }
                
                // Force Load 
                /*if (currentColObj.getId() != null)
                {
                    tmpSession.attach(currentColObj);
                    currentColObj.getLeftSideRels().size();
                    currentColObj.getRightSideRels().size();
                }*/
                
                // Is the other side already hooked up
                // if it is a different ColObj then remove the link.
                if (otherSideColObj != null && !newOtherSide.getId().equals(otherSideColObj.getId()))
                {
                    if (isLeftSide)
                    {
                        removeFromCollectionRel(otherSideColObj.getRightSideRels(), collectionRel);
                        collectionRel.setRightSide(null);
                        
                    } else
                    {
                        removeFromCollectionRel(otherSideColObj.getLeftSideRels(), collectionRel);
                        collectionRel.setLeftSide(null);
                    }
                }
                
                otherSideColObj = newOtherSide;
                //otherSideColObj.getLeftSideRels().size();
                //otherSideColObj.getRightSideRels().size();
                
                if (isLeftSide)
                {
                    collectionRel.setLeftSide(currentColObj);
                    collectionRel.setRightSide(otherSideColObj);
                    
                    if (!currentColObj.getLeftSideRels().contains(collectionRel))
                    {
                        currentColObj.getLeftSideRels().add(collectionRel);
                        otherSideColObj.getRightSideRels().add(collectionRel);
                    }
                } else
                {
                    collectionRel.setLeftSide(otherSideColObj);
                    collectionRel.setRightSide(currentColObj);
                    
                    if (!currentColObj.getLeftSideRels().contains(collectionRel))
                    {
                        otherSideColObj.getLeftSideRels().add(collectionRel);
                        currentColObj.getRightSideRels().add(collectionRel);
                    }
                }

            } catch (Exception ex)
            {
                ex.printStackTrace();
                
            } finally
            {
                if (tmpSession != null) tmpSession.close();
            }
        } else if (dataObj != null && otherSideColObj != null)
        {
            DataProviderSessionIFace tmpSession = null;
            try
            {
                tmpSession = DataProviderFactory.getInstance().createSession();
                otherSideColObj = tmpSession.merge(otherSideColObj);
                
                if (isLeftSide)
                {
                    removeFromCollectionRel(currentColObj.getLeftSideRels(), collectionRel);
                    removeFromCollectionRel(otherSideColObj.getRightSideRels(), collectionRel);
                    
                } else // right side
                {
                    removeFromCollectionRel(currentColObj.getRightSideRels(), collectionRel);
                    removeFromCollectionRel(otherSideColObj.getLeftSideRels(), collectionRel);
                }
                collectionRel.setRightSide(null);
                collectionRel.setLeftSide(null);
                
                if (collectionRel.getId() != null && fvo != null)
                {
                    fvo.getMVParent().getTopLevel().addDeletedItem(collectionRel);
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            } finally
            {
                if (tmpSession != null) tmpSession.close();
            }
        }
    }
    
    /**
     * 
     */
    private void adjustSQLTemplate()
    {
        if (colRelType != null && ((!isLeftSide && rightSideCol != null) || (isLeftSide && leftSideCol != null)))
        {
            StringBuilder sql = new StringBuilder("SELECT %s1 FROM CollectionObject co ");
            sql.append("WHERE co.collectionMemberId = ");
            sql.append(isLeftSide ? rightSideCol.getCollectionId() : leftSideCol.getCollectionId());
            sql.append(" AND %s2");
            //System.out.println(sql.toString());
            cbx.setSqlTemplate(sql.toString());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(final Object value, final String defaultValue)
    {
        super.setValue(value, defaultValue); // sets 'dataObj'
        
        if (value instanceof CollectionObject && colRelType != null)
        {
            currentColObj   = (CollectionObject)value;
            if (currentColObj.getId() != null)
            {
                currentColObj.getLeftSideRels().size();
                currentColObj.getRightSideRels().size();
            }
            otherSideColObj = null;
            
            boolean leftSide = currentColObj.getCollection().getId().equals(leftSideCol.getId());
            
            Set<CollectionRelationship> collectionRels = leftSide ? currentColObj.getLeftSideRels() : currentColObj.getRightSideRels();
            for (CollectionRelationship colRel : collectionRels)
            {
                if (colRel.getCollectionRelType().getId().equals(colRelType.getId()))
                {
                    collectionRel   = colRel;
                    otherSideColObj = leftSide ? colRel.getRightSide() : colRel.getLeftSide();
                    otherSideColObj.getLeftSideRels().size();
                    otherSideColObj.getRightSideRels().size();
                    break;
                }
            }
            
            if (cbx != null)
            {
                cbx.setValue(otherSideColObj, null);
            } else
            {
                textWithInfo.setValue(otherSideColObj, null);
            }
        } else
        {
            currentColObj = null;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#getValue()
     */
    @Override
    public Object getValue()
    {
        return cbx != null ? cbx.getValue() : super.getValue();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {CATNUM_NAME, "rightSide", "leftSide"};
    }

    //---------------------------------------------------------------------------------------------
    //-- edu.ku.brc.af.ui.forms.UIPluginable
    //---------------------------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        if (cbx != null)
        {
            cbx.cleanUp();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getReason()
     */
    @Override
    public String getReason()
    {
        return cbx != null ? cbx.getReason() : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getState()
     */
    @Override
    public ErrorType getState()
    {
        return cbx != null ? cbx.getState() : ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    @Override
    public Component getValidatableUIComp()
    {
        return cbx;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isChanged()
     */
    @Override
    public boolean isChanged()
    {
        return cbx != null ? cbx.isChanged() : false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isInError()
     */
    @Override
    public boolean isInError()
    {
        return cbx != null ? cbx.isInError() : false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isRequired()
     */
    @Override
    public boolean isRequired()
    {
        return this.isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#reset()
     */
    @Override
    public void reset()
    {
        cbx.reset();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    @Override
    public void setAsNew(boolean isNew)
    {
        if (cbx != null)
        {
            cbx.setAsNew(isNew);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    @Override
    public void setChanged(boolean isChanged)
    {
        if (cbx != null)
        {
            cbx.setChanged(isChanged);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setRequired(boolean)
     */
    @Override
    public void setRequired(boolean isRequired)
    {
        if (cbx != null)
        {
            cbx.setRequired(isRequired);
        }
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setState(edu.ku.brc.af.ui.forms.validation.UIValidatable.ErrorType)
     */
    @Override
    public void setState(ErrorType state)
    {
        if (cbx != null)
        {
            cbx.setState(state);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#validateState()
     */
    @Override
    public ErrorType validateState()
    {
        return cbx != null ? cbx.validateState() : ErrorType.Valid;
    }
    
}
