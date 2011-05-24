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

import static edu.ku.brc.ui.UIRegistry.getViewbasedFactory;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.ViewBasedDialogFactoryIFace;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionRelType;
import edu.ku.brc.specify.datamodel.CollectionRelationship;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Apr 11, 2011
 *
 */
public class CollectionRelOneToManyPlugin extends UIPluginBase implements UIValidatable
{
    protected static final Logger log = Logger.getLogger(CollectionRelOneToManyPlugin.class);
    
    private static final HashMap<Integer, UIFieldFormatterIFace> catNumFmtHash = new HashMap<Integer, UIFieldFormatterIFace>();
    
    private String[]               headers;

    private boolean                isLeftSide      = true;
    private CollectionRelType      colRelType      = null;
    private Collection             leftSideCol     = null;
    private Collection             rightSideCol    = null;
    private UIFieldFormatterIFace  catNumFormatter = null;
    
    private CollectionObject       currentColObj   = null;
    
    private boolean                  isRequired      = false;
    private boolean                  isChanged       = false;
    
    // UI
    private JTable                   table;
    private ColObjDataModel          model;
    private Vector<CollectionRelationship> colObjs;
    
    private JButton                  searchAddBtn;
    private JButton                  removeBtn;
    private JButton                  infoBtn;
    

    /**
     * 
     */
    public CollectionRelOneToManyPlugin()
    {
        super();
        
        colObjs = new Vector<CollectionRelationship>();
        
        DBTableInfo coTI   = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
        DBTableInfo collTI = DBTableIdMgr.getInstance().getInfoById(Collection.getClassTableId());
        headers = new String[] {coTI.getTitle(), collTI.getTitle()};
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
                tmpSession = DataProviderFactory.getInstance().createSession();
                colRelType = tmpSession.getData(CollectionRelType.class, "name", relName, DataProviderSessionIFace.CompareType.Equals);
                if (colRelType != null)
                {
                    leftSideCol  = colRelType.getLeftSideCollection();
                    rightSideCol = colRelType.getRightSideCollection();
                    colRelType.getRelationships().size();
                    rightSideCol.getCollectionId();
                    leftSideCol.getCollectionId();
                    
                    catNumFormatter = getCatNumFormatter(leftSideCol, rightSideCol);
                    
                    Collection currCollection = AppContextMgr.getInstance().getClassObject(Collection.class);
                    isLeftSide = currCollection.getId().equals(leftSideCol.getId());
                    if (!isLeftSide)
                    {
                        // major error
                    }
                    
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
            
            CellConstraints cc   = new CellConstraints();
            
            PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,2px,p", "f:p:g"), this);
            
            model = new ColObjDataModel();
            table = new JTable(model); 
            JScrollPane sp = UIHelper.createScrollPane(table, false);
            pb.add(sp, cc.xy(1,1));
            
            searchAddBtn = UIHelper.createIconBtn("SearchAdd", IconManager.IconSize.Std20,  "", true, createSearchAL());
            removeBtn    = UIHelper.createIconBtn("Eraser",    IconManager.IconSize.NonStd, "", true, createRemoveAL());
            infoBtn      = UIHelper.createIconBtn("InfoIcon",  IconManager.IconSize.Std20, "", true,   createInfoAL());
            
            PanelBuilder rpb = new PanelBuilder(new FormLayout("f:p:g", "p,4px,p,4px,p, f:p:g"));
    
            int y = 1;
            rpb.add(infoBtn,       cc.xy(1,y)); y += 2;
            if (!isViewMode)
            {
                rpb.add(searchAddBtn,  cc.xy(1,y)); y += 2;
                rpb.add(removeBtn,     cc.xy(1,y)); y += 2;
            }                
            pb.add(rpb.getPanel(), cc.xy(3,1));

            
            UIHelper.makeTableHeadersCentered(table, true);
            
            UIHelper.setVisibleRowCount(table, 5);
            
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setCellSelectionEnabled(false);
            table.setColumnSelectionAllowed(false);
            table.setRowSelectionAllowed(true);
            
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    updateBtns();
                }
            });
            updateBtns();
        } else
        {
            System.err.println(propertiesArg);
            log.error("CollectionRelOneToManyPlugin - initialize attribute for 'relname' is null and can't be!");
        }
    }
    
    private void updateBtns()
    {
        int rowInx = table.getSelectedRow();
        boolean selected = rowInx > -1;
        infoBtn.setEnabled(selected);
        removeBtn.setEnabled(selected);
        searchAddBtn.setEnabled(true);
    }
    
    /**
     * @return
     */
    private ActionListener createInfoAL()
    {
        return new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showCollectionObject();
            }
        };
    }
    
    /**
     * @return
     */
    private ActionListener createRemoveAL()
    {
        return new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                removeCollectionObject();
            }
        };
    }
    
    /**
     * @return
     */
    private ActionListener createSearchAL()
    {
        return new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showSearch();
            }
        };
    }
    
    /**
     * 
     */
    private void showSearch()
    {
        ViewBasedSearchDialogIFace srchDlg = getViewbasedFactory().createSearchDialog(null, "CollectionObjectSearch"); //$NON-NLS-1$
        if (srchDlg != null)
        {
            srchDlg.registerQueryBuilder(CollectionRelPlugin.createSearchQueryBuilder(isLeftSide, leftSideCol, rightSideCol));
            
            srchDlg.setTitle(DBTableIdMgr.getInstance().getTitleForId(CollectionObject.getClassTableId()));
            srchDlg.getDialog().setVisible(true);
            if (!srchDlg.isCancelled())
            {
                CollectionObject colObj = (CollectionObject)srchDlg.getSelectedObject();
                if (colObj != null)
                {
                    // Make sure this no one is already pointing at it.
                    if (CollectionRelPlugin.isColObjAlreadyUsed(colRelType.getId(), rightSideCol.getId(), colObj.getCatalogNumber(), catNumFormatter))
                    {
                        return;
                    }
                    
                    if (!existsInCollectionRel(currentColObj.getLeftSideRels(), colObj))
                    {
                        CollectionRelationship collectionRel = new CollectionRelationship();
                        collectionRel.initialize();
                        collectionRel.setCollectionRelType(colRelType);
                        colRelType.getRelationships().add(collectionRel);
                        
                        collectionRel.setLeftSide(currentColObj);
                        collectionRel.setRightSide(colObj);
                        currentColObj.getLeftSideRels().add(collectionRel);
                        
                        colObjs.add(collectionRel);
                        
                        DataProviderSessionIFace tmpSession = null;
                        try
                        {
                            tmpSession = DataProviderFactory.getInstance().createSession();
                            tmpSession.attach(colObj);
                            colObj.getCollection().getId(); // force load
                            colObj.getRightSideRels().add(collectionRel);
                            
                        } catch (Exception ex)
                        {
                            ex.printStackTrace();
                        } finally
                        {
                            if (tmpSession != null) tmpSession.close();
                        }
                        model.fireTableDataChanged();
                        notifyChangeListeners(new ChangeEvent(this));
                    }
                }
            }
        }
    }
    
    /**
     * 
     */
    private void removeCollectionObject()
    {
        int rowInx = table.getSelectedRow();
        if (rowInx > -1)
        {
            CollectionRelationship colRel = colObjs.get(rowInx);
            if (colRel != null)
            {
                CollectionRelPlugin.removeFromCollectionRel(currentColObj.getLeftSideRels(), colRel);
                if (colRel.getId() != null)
                {
                    colRel.setLeftSide(null);
                    colRel.setRightSide(null);
                    fvo.getMVParent().getTopLevel().addDeletedItem(colRel);
                }
                colObjs.remove(rowInx);
                model.fireTableDataChanged();
                notifyChangeListeners(new ChangeEvent(CollectionRelOneToManyPlugin.this));
            }
        }
    }
    
    /**
     * @param collectionRels
     * @param colRelToBeRemoved
     */
    private boolean existsInCollectionRel(final Set<CollectionRelationship> collectionRels, 
                                          final CollectionObject colObj)
    {
        for (CollectionRelationship colRel : collectionRels)
        {
            if (colRel.getRightSide().getId().equals(colObj.getId()))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 
     */
    private void showCollectionObject()
    {
        int rowInx = table.getSelectedRow();
        if (rowInx > -1)
        {
            int     options = MultiView.DONT_USE_EMBEDDED_SEP;
            ViewBasedDisplayIFace dlg = getViewbasedFactory().createDisplay(UIRegistry.getTopWindow(), 
                                                                             "CollectionObject", 
                                                                             DBTableIdMgr.getInstance().getTitleForId(CollectionObject.getClassTableId()), 
                                                                             UIRegistry.getResourceString("CLOSE"), 
                                                                             false, 
                                                                             options, 
                                                                             null, 
                                                                             ViewBasedDialogFactoryIFace.FRAME_TYPE.DIALOG);
            CollectionRelationship colRel   = colObjs.get(rowInx);
            boolean                leftSide = currentColObj.getCollection().getId().equals(leftSideCol.getId());
            CollectionObject       colObj   = leftSide ? colRel.getRightSide() : colRel.getLeftSide();
            dlg.setData(colObj);
            dlg.showDisplay(true);
        }
    }
    
    /**
     * gets the CatNum formatter from the cache.
     * @param id Collection ID
     * @return the formatter or null
     */
    public static UIFieldFormatterIFace getCatNumFormatter(final Integer id)
    {
        if (id != null)
        {
            return catNumFmtHash.get(id);
        }
        return null;
    }
    
    /**
     * @param leftSideCol
     * @param rightSideCol
     * @return
     */
    public static UIFieldFormatterIFace getCatNumFormatter(final Collection leftSideCol, final Collection rightSideCol)
    {
        //AppContextMgr currApContextMgr = AppContextMgr.getInstance();
        if (catNumFmtHash.get(leftSideCol.getId()) == null)
        {
            catNumFmtHash.put(leftSideCol.getId(), UIFieldFormatterMgr.getInstance().getFormatter(leftSideCol.getCatalogNumFormatName()));
        }
        
        // Go get Formatter and then cache it
        UIFieldFormatterIFace catNumFormatter = catNumFmtHash.get(rightSideCol.getId());
        if (catNumFormatter == null)
        {
            /*SpecifyUser          spUser        = currApContextMgr.getClassObject(SpecifyUser.class);
            SpecifyAppContextMgr appContextMgr = new SpecifyAppContextMgr();
            
            Division   div  = AppContextMgr.getInstance().getClassObject(Division.class);
            Discipline disp = AppContextMgr.getInstance().getClassObject(Discipline.class);
            
            AppContextMgr.CONTEXT_STATUS status = appContextMgr.setContext(DBConnection.getInstance().getDatabaseName(), spUser.getName(), true, false, rightSideCol.getCollectionName());
            if (status == AppContextMgr.CONTEXT_STATUS.OK)
            {
                UIFieldFormatterMgr ffMgr = new SpecifyUIFieldFormatterMgr();
                ffMgr.load();
                ffMgr.setAppContextMgr(appContextMgr);
                catNumFormatter = ffMgr.getFormatter(rightSideCol.getCatalogNumFormatName());
                if (catNumFormatter != null)
                {
                    catNumFmtHash.put(rightSideCol.getId(), catNumFormatter);
                }
                ffMgr.setAppContextMgr(null);
                appContextMgr.clear();
            }*/
            // For now use the left side's CatNumFmt for the rightside.
            // The above code sets too many global items (Div, Disp, UserType etc)
            catNumFmtHash.put(rightSideCol.getId(), UIFieldFormatterMgr.getInstance().getFormatter(leftSideCol.getCatalogNumFormatName()));
        }
        return catNumFormatter;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#getValue()
     */
    @Override
    public Object getValue()
    {
        return super.getValue();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(Object value, String defaultValue)
    {
        super.setValue(value, defaultValue);
        
        if (leftSideCol != null)
        {
            if (value != null)
            {
                currentColObj = (CollectionObject)value;
                boolean leftSide = currentColObj.getCollection().getId().equals(leftSideCol.getId());
                
                model.setLeft(leftSide);
                
                //System.out.println("Col: "+currentColObj.getCollection().getCollectionName()+"   Left: "+leftSide+"  "+(currentColObj != null ? currentColObj.getCatalogNumber(): "null"));
                
                Set<CollectionRelationship> collectionRels = leftSide ? currentColObj.getLeftSideRels() : currentColObj.getRightSideRels();
                for (CollectionRelationship cr : collectionRels)
                {
                    cr.getLeftSide().getCollection().getCollectionName(); // force load of collection (only)
                    cr.getRightSide().getCollection().getCollectionName(); // force load of collection (only)
                }
                
                currentColObj.getLeftSideRels().size();
                currentColObj.getRightSideRels().size();
                
                colObjs.clear();
                colObjs.addAll(collectionRels);
                model.fireTableDataChanged();
            }
        } else
        {
            log.error("CollectionRelOneToManyPlugin - leftSide collection is null and it can't be!");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        return colObjs.size() > 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        colObjs.clear();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getReason()
     */
    @Override
    public String getReason()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getState()
     */
    @Override
    public ErrorType getState()
    {
        return ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    @Override
    public Component getValidatableUIComp()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isChanged()
     */
    @Override
    public boolean isChanged()
    {
        return isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isInError()
     */
    @Override
    public boolean isInError()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isRequired()
     */
    @Override
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#reset()
     */
    @Override
    public void reset()
    {
        isChanged = false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    @Override
    public void setAsNew(boolean isNew)
    {
        

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    @Override
    public void setChanged(boolean isChanged)
    {
        this.isChanged = isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setRequired(boolean)
     */
    @Override
    public void setRequired(boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setState(edu.ku.brc.af.ui.forms.validation.UIValidatable.ErrorType)
     */
    @Override
    public void setState(ErrorType state)
    {

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#validateState()
     */
    @Override
    public ErrorType validateState()
    {
        return ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {"rightSideColRels"};
    }
    
    //---------------------------------------------------------------------------------------------------
    //
    //---------------------------------------------------------------------------------------------------
    class ColObjDataModel extends DefaultTableModel
    {
        boolean isLeft = true;
        
        /**
         * 
         */
        public ColObjDataModel()
        {
            super();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            String valueStr = "N/A";
            
            CollectionRelationship colRel = colObjs.get(row);
            if (colRel != null)
            {
                CollectionObject colObj = isLeft ? colRel.getRightSide() : colRel.getLeftSide();
                if (colObj != null)
                {
                    if (column == 0)
                    {
                        valueStr = colObj.getCatalogNumber();
                        if (catNumFormatter != null)
                        {
                            valueStr = (String)catNumFormatter.formatToUI(valueStr);
                        }
                    } else if (column == 1)
                    {
                        valueStr = colObj.getCollection().getCollectionName();
                    }
                }
            }
            return valueStr;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return headers.length;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return headers[column];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return colObjs.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        /**
         * @return the isLeft
         */
        public boolean isLeft()
        {
            return isLeft;
        }

        /**
         * @param isLeft the isLeft to set
         */
        public void setLeft(boolean isLeft)
        {
            this.isLeft = isLeft;
        }
        
        
    }
}
