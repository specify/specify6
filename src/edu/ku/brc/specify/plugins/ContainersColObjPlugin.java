/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.plugins;

import java.awt.Component;
import java.util.Properties;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.TextFieldWithInfo;
import edu.ku.brc.af.ui.forms.SessionListenerIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Dec 15, 2010
 *
 */
public class ContainersColObjPlugin extends UIPluginBase implements SessionListenerIFace, UIValidatable
{
    private final static String CATNUM_NAME    = "catalogNumber";
    private final static String CATNUM_NAMECAP = "CatalogNumber";
    private final static String COLOBJ_NAME    = "CollectionObject";

    private ValComboBoxFromQuery  qcbx         = null;
    private TextFieldWithInfo     textWithInfo = null;
    private Set<CollectionObject> coSet        = null;
    private Container             container    = null;
    
    private UIFieldFormatterIFace colObjFmt    = null;
    private String                errorMsg     = null;
    private boolean               isRequired   = false;
    
    /**
     * Constructor.
     */
    public ContainersColObjPlugin()
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
        
        UIRegistry.loadAndPushResourceBundle("specify_plugins");
        errorMsg = UIRegistry.getResourceString("CNTR_CO_INUSE");
        UIRegistry.popResourceBundle();
        
        DBTableInfo coTI = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
        DBFieldInfo catNumFld = coTI.getFieldByColumnName(CATNUM_NAMECAP);
        colObjFmt = catNumFld.getFormatter();
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("MAX(p;100px)",  "p"), this);
        
        if (isViewModeArg)
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
            
        } else
        {
            int btnOpts = ValComboBoxFromQuery.CREATE_EDIT_BTN | ValComboBoxFromQuery.CREATE_SEARCH_BTN;
            qcbx = new ValComboBoxFromQuery(coTI,
                                            CATNUM_NAME,
                                            CATNUM_NAME,
                                            CATNUM_NAME,
                                            null,
                                            CATNUM_NAMECAP,
                                            null,
                                            "",
                                            null, // helpContext
                                            btnOpts);
            pb.add(qcbx, cc.xy(1, 1));
        
            qcbx.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e)
                {
                    itemSelected();
                }
            });
        }
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(Object value, String defaultValue)
    {
        if (value != null && value instanceof Container)
        {
            container = (Container)value;
            coSet     = container.getCollectionObjects();
            
            coSet.size();
            CollectionObject colObj = null;
            if (!coSet.isEmpty())
            {
                colObj = coSet.iterator().next();
            }
            
            if (qcbx != null)
            {
                qcbx.setValue(colObj, null);
                
            } else if (textWithInfo != null)
            {
                textWithInfo.setValue(colObj, null);
            }
        }
    }
    
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#getValue()
     */
    @Override
    public Object getValue()
    {
        CollectionObject colObj = null;
        if (coSet != null && !coSet.isEmpty())
        {
            colObj = coSet.iterator().next();
        }
        
        if (qcbx != null)
        {
            Object qcbxValue = qcbx.getValue();
            if (qcbxValue != null)
            {
                CollectionObject newColObj = (CollectionObject)qcbxValue;
                if (colObj == null || !newColObj.getId().equals(colObj.getId()))
                {
                    coSet.clear();
                    coSet.add(newColObj);
                    newColObj.setContainer(container);
                }
            }
        }
        return container;
    }

    /**
     * 
     */
    protected void itemSelected()
    {
        Integer selectedId = qcbx.getTextWithQuery().getSelectedId();
        if (selectedId != null)
        {
            final String clause = String.format(" FROM collectionobject WHERE CollectionObjectId = %d AND ContainerID IS NOT NULL", selectedId);
            String sql = "SELECT COUNT(*)" + clause;
           //System.err.println("ContainersColObjPlugin: "+sql+" -> "+BasicSQLUtils.getCountAsInt(sql));
            
            if (BasicSQLUtils.getCountAsInt(sql) > 0)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        qcbx.setValue(null, null);
                        
                        String catNumStr = BasicSQLUtils.querySingleObj("SELECT CatalogNumber" + clause);
                        catNumStr = colObjFmt != null ? (String)colObjFmt.formatToUI(catNumStr) : catNumStr;
                        
                        UIRegistry.showError(String.format(errorMsg, catNumStr));
                    }
                });
            }
        } else
        {
            CollectionObject colObj = container.getCollectionObject();
            if (colObj != null)
            {
                fvo.getMVParent().getTopLevel().addToBeSavedItem(colObj);
                colObj.setContainer(null);
                coSet.clear();
            }
        }
        notifyChangeListeners(new ChangeEvent(this));
    }

    /**
     * 
     */
    /*protected void adjustSQLTemplate()
    {
        StringBuilder sql = new StringBuilder("SELECT %s1 FROM CollectionObject co ");
        sql.append("WHERE co.collectionMemberId = ");
        sql.append(isLeftSide ? rightSideCol.getCollectionId() : leftSideCol.getCollectionId());
        sql.append(" AND %s2");
        //System.out.println(sql.toString());
        qcbx.setSqlTemplate(sql.toString());
    }*/

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {CATNUM_NAME};
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.SessionListenerIFace#setSession(edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void setSession(DataProviderSessionIFace session)
    {
        if (qcbx != null)
        {
            qcbx.setSession(session);
        }
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
        if (qcbx != null)
        {
            qcbx.cleanUp();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getReason()
     */
    @Override
    public String getReason()
    {
        return qcbx != null ? qcbx.getReason() : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getState()
     */
    @Override
    public ErrorType getState()
    {
        return qcbx != null ? qcbx.getState() : ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    @Override
    public Component getValidatableUIComp()
    {
        return qcbx;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isChanged()
     */
    @Override
    public boolean isChanged()
    {
        return qcbx != null ? qcbx.isChanged() : false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isInError()
     */
    @Override
    public boolean isInError()
    {
        return qcbx != null ? qcbx.isInError() : false;
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
        qcbx.reset();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    @Override
    public void setAsNew(boolean isNew)
    {
        if (qcbx != null)
        {
            qcbx.setAsNew(isNew);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    @Override
    public void setChanged(boolean isChanged)
    {
        if (qcbx != null)
        {
            qcbx.setChanged(isChanged);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setRequired(boolean)
     */
    @Override
    public void setRequired(boolean isRequired)
    {
        if (qcbx != null)
        {
            qcbx.setRequired(isRequired);
        }
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setState(edu.ku.brc.af.ui.forms.validation.UIValidatable.ErrorType)
     */
    @Override
    public void setState(ErrorType state)
    {
        if (qcbx != null)
        {
            qcbx.setState(state);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#validateState()
     */
    @Override
    public ErrorType validateState()
    {
        return qcbx != null ? qcbx.validateState() : ErrorType.Valid;
    }
    

}
