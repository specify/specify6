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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionRelType;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.EditDeleteAddPanel;
import static edu.ku.brc.ui.UIHelper.*;
import static edu.ku.brc.ui.UIRegistry.*;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jan 7, 2010
 *
 */
public class CollectionRelTask extends BaseTask
{
    //private static final Logger log = Logger.getLogger(CollectionRelTask.class);
    
    private static final String  COLRELTSK        = "COLRELTSK";
    //private static final String  CR_MANAGECR      = "CR_MANAGECR";
    private static final String  CR_RELTYPES      = "CR_RELTYPES";
    private static final String  COLREL_MENU      = "COLREL_MENU";
    private static final String  COLREL_MNU       = "COLREL_MNU";
    private static final String  COLREL_TITLE     = "COLREL_TITLE";
    private static final String  COLREL_SECURITY  = "COLRELEDIT";
    
    
    private JList relList;
    private EditDeleteAddPanel edaPanel;
    
    private Vector<Collection>        colObjVec;
    private Vector<CollectionRelType> relObjVec;
    
    private Collection srcCollection = null;
    private Collection dstCollection = null;
    
    
    /**
     * 
     */
    public CollectionRelTask()
    {
        super(COLRELTSK, getResourceString(COLRELTSK));
        
        iconName = "SystemSetup";
        
        CommandDispatcher.register(COLRELTSK, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    @Override
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            // No Series Processing
            /*NavBox navBox = new NavBox(getResourceString("Actions"));
            
            CommandAction cmdAction = new CommandAction(COLRELTSK, CR_MANAGECR);
            NavBoxAction nba = new NavBoxAction(cmdAction);
            NavBoxItemIFace nbi = NavBox.createBtnWithTT(getResourceString(CR_MANAGECR), name, null, IconManager.STD_ICON_SIZE, nba);
            navBox.add(nbi);//NavBox.createBtn(getResourceString("CR_MGR"), name, IconManager.STD_ICON_SIZE));
            
            cmdAction = new CommandAction(COLRELTSK, CR_RELTYPES);
            nba       = new NavBoxAction(cmdAction);
            nbi       = NavBox.createBtnWithTT(getResourceString(CR_RELTYPES), name, null, IconManager.STD_ICON_SIZE, nba);
            
            navBox.add(nbi);//NavBox.createBtn(getResourceString("CR_MGR"), name, IconManager.STD_ICON_SIZE));
            
            navBoxes.add(navBox);*/
        }
        isShowDefault = true;
    }
    
    /**
     * @return will return a list or empty list, but not NULL
     */
    //@SuppressWarnings("unchecked")
    public static List<?> getList(Class<?> clsObj)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            List<?> list = session.getDataList("FROM "+clsObj.getSimpleName());
            for (Object dataObj : list)
            {
                if (dataObj instanceof FormDataObjIFace)
                {
                    ((FormDataObjIFace)dataObj).forceLoad();
                }
            }
            return list;
            
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
        return new ArrayList<Object>();
    }
    
    /**
     * @param list
     * @return
     */
    private DefaultListModel fillLDM(final Vector<?> list)
    {
        DefaultListModel model = new DefaultListModel();
        for (Object item : list)
        {
            model.addElement(item);
        }
        return model;
    }
    
    /**
     * 
     */
    private void showSourceList()
    {
        srcCollection = null;
        
        ChooseFromListDlg<Collection> dlg = new ChooseFromListDlg<Collection>((Frame)getTopWindow(), 
                getResourceString(COLREL_TITLE), getResourceString("COLREL_CHS_SRC"), CustomDialog.OKCANCELHELP, colObjVec);
        dlg.setHelpContext("cr_configure");
        dlg.setOkLabel(getResourceString("NEXT"));
        centerAndShow(dlg);
        if (!dlg.isCancelled())
        {
            srcCollection = dlg.getSelectedObject();
            showDestinationList();
        }
    }
    
    /**
     * 
     */
    private void showDestinationList()
    {
        Vector<Collection> filteredList;
        boolean allowSelfRefColRel = AppPreferences.getLocalPrefs().getBoolean("SELF_COLREL", false);
        if (allowSelfRefColRel)
        {
            filteredList = new Vector<Collection>(colObjVec);
        } else
        {
            filteredList = new Vector<Collection>();
            for (Collection col : colObjVec)
            {
                if (col.getCatalogNumFormatName().equals(srcCollection.getCatalogNumFormatName()) &&
                    !col.getId().equals(srcCollection.getId()))
                {
                    filteredList.add(col);
                }
            }
        }
        
        if (filteredList.size() == 1)
        {
            dstCollection = filteredList.get(0);
            
        } else if (filteredList.size() > 1)
        {
            ChooseFromListDlg<Collection> dlg = new ChooseFromListDlg<Collection>((Frame)getTopWindow(), 
                    getResourceString(COLREL_TITLE), getResourceString("COLREL_CHS_DST"), CustomDialog.OKCANCELHELP, filteredList);
            dlg.setHelpContext("cr_configure"); 
            dlg.setOkLabel(getResourceString("NEXT"));
            centerAndShow(dlg);
            if (!dlg.isCancelled())
            {
                dstCollection = dlg.getSelectedObject();   
            } else
            {
                return;
            }
        } else
        {
            showLocalizedError("COLREL_NO_COL");
            return;
        }
        
        addRel();
    }

    /**
     * 
     */
    @SuppressWarnings("unchecked")
    private void createRelMgrUI()
    {
        colObjVec = new Vector<Collection>((List<Collection>)getList(Collection.class));
        relObjVec = new Vector<CollectionRelType>((List<CollectionRelType>)getList(CollectionRelType.class));
        
        relList   = new JList(fillLDM(relObjVec));

        ActionListener editAL = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
               editRel(); 
            }
        };
        ActionListener addAL = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showSourceList(); 
            }
        };
        ActionListener delAL = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
               delRel(); 
            }
        };
        
        relList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    editRel(); 
                }
            }
        });
        
        edaPanel = new EditDeleteAddPanel(editAL, delAL, addAL);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:MAX(300px;p):g", "p,2px,f:p:g,2px,p"));
        
        pb.add(createI18NLabel("CR_REL", SwingConstants.CENTER),  cc.xy(1, 1));
        pb.add(createScrollPane(relList),                         cc.xy(1, 3));
        pb.add(edaPanel,                                                   cc.xy(1, 5));
        pb.setDefaultDialogBorder();
        
        edaPanel.getAddBtn().setEnabled(true);
        
        ListSelectionListener relLSL = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    updateBtnUI();
                }
            }
        };
        relList.addListSelectionListener(relLSL);
        
        CustomDialog dlg = new CustomDialog((Frame)getTopWindow(), getResourceString(COLREL_TITLE), true, CustomDialog.OK_BTN, pb.getPanel());
        dlg.setOkLabel(getResourceString("CLOSE"));
        dlg.setVisible(true);
    }
    
    /**
     * 
     */
    private void editRel()
    {
        CollectionRelType crt = (CollectionRelType)relList.getSelectedValue();
        if (crt != null)
        {
            createRelType(crt);
        }
    }
    
    /**
     * 
     */
    private void addRel()
    {
        createRelType(null);
    }
    
    /**
     * 
     */
    private void delRel()
    {
        CollectionRelType collectionRel = (CollectionRelType)relList.getSelectedValue();
        if (collectionRel != null)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                CollectionRelType colRelTyp = session.get(CollectionRelType.class, collectionRel.getId());
                
                String key = null;
                if (colRelTyp != null)
                {
                    int cnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM collectionrelationship WHERE CollectionRelTypeID = " +collectionRel.getId());
                    if (cnt == 0)
                    {
                        Collection   rRelCol  = colRelTyp.getRightSideCollection();
                        Collection   lRelCol  = colRelTyp.getLeftSideCollection();
                        Discipline   leftDisp = lRelCol.getDiscipline();
                        TaxonTreeDef leftTaxonTreeDef = leftDisp.getTaxonTreeDef();
                        
                        String sql = String.format("SELECT COUNT(*) FROM collectingeventattribute cea INNER JOIN taxon t ON cea.HostTaxonID = t.TaxonID WHERE cea.DisciplineID = %d AND t.TaxonTreeDefID = %d",
                                         rRelCol.getDiscipline().getId(), leftTaxonTreeDef.getId());
                        cnt = BasicSQLUtils.getCountAsInt(sql);
                        if (cnt > 0)
                        {
                            key = "COLREL_USEDBY_HST";//"Is Used by HostTaxonID";
                        }
                    } else
                    {
                        key = "COLREL_USEDBY_CR";
                    }
                    
                    if (key != null)
                    {
                        showLocalizedError(key);
                    } else
                    {
                        if (BasicSQLUtils.update("DELETE FROM collectionreltype WHERE CollectionRelTypeID = " + colRelTyp.getId()) == 1)
                        {
                            DefaultListModel model = (DefaultListModel)relList.getModel();
                            model.remove(relList.getSelectedIndex());
                        }
                    }
                }

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
    
    /**
     * @param dataObj
     * @return
     */
    private boolean save(final FormDataObjIFace dataObj)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            session.saveOrUpdate(dataObj);
            session.commit();
            
            return true;
            
        } catch (Exception ex)
        {
            if (session != null) session.rollback();
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        return false;
    }
    
    /**
     * @return
     */
    private CollectionRelType createRelType(final CollectionRelType crt)
    {
        boolean isEdit = crt != null;
        
        final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Dialog)getMostRecentWindow(),
                "SystemSetup",
                "CollectionRelType",
                null,
                getResourceString(COLREL_TITLE),
                getResourceString("OK"),
                null,
                null,
                true,
                MultiView.HIDE_SAVE_BTN | 
                MultiView.DONT_ADD_ALL_ALTVIEWS | 
                MultiView.USE_ONLY_CREATION_MODE |
                MultiView.IS_EDITTING);
        
        dlg.setFormAdjuster(new FormPane.FormPaneAdjusterIFace() {
            @Override
            public void adjustForm(final FormViewObj fvo)
            {
                JLabel     leftLbl  = fvo.getLabelById("left");
                JLabel     rightLbl = fvo.getLabelById("right");
                
                Collection leftCol  = crt != null ? crt.getLeftSideCollection() : srcCollection;
                Collection rightCol = crt != null ? crt.getRightSideCollection() : dstCollection;
                
                if (leftCol != null)
                {
                    leftLbl.setText(leftCol.getCollectionName());
                }
                if (rightCol != null)
                {
                    rightLbl.setText(rightCol.getCollectionName());
                }
                
                Font bold = leftLbl.getFont().deriveFont(Font.BOLD).deriveFont(leftLbl.getFont().getSize2D()+2.0f);
                leftLbl.setFont(bold);
                rightLbl.setFont(bold);
            }
        
        });
        
        
        CollectionRelType colRelType;
        if (isEdit)
        {
            colRelType = crt;
            
        } else
        {
            colRelType = new CollectionRelType();
            colRelType.initialize();
        }
        
        dlg.setHelpContext("cr_name");
        dlg.setData(colRelType);
        centerAndShow(dlg);
        
        if (!dlg.isCancelled())
        {
            if (!isEdit) // it's new
            {
                if (srcCollection != null && dstCollection != null)
                {
                    try
                    {
                        DataProviderSessionIFace session = null;
                        try
                        {
                            session = DataProviderFactory.getInstance().createSession();
                            srcCollection  = session.get(Collection.class, srcCollection.getId());
                            dstCollection = session.get(Collection.class, dstCollection.getId());
                            
                            colRelType.setLeftSideCollection(srcCollection);
                            colRelType.setRightSideCollection(dstCollection);
                            srcCollection.getLeftSideRelTypes().add(colRelType);
                            dstCollection.getRightSideRelTypes().add(colRelType);
                            
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
                        
                        if (save(colRelType))
                        {
                            ((DefaultListModel)relList.getModel()).addElement(colRelType);
                            return colRelType;
                        }
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    
                } else  if (save(colRelType))
                {
                    return colRelType;
                }
            } else if (save(colRelType))
            {
                return colRelType;
            }
        }

        return colRelType;
    }
    
    /**
     * 
     */
    private void updateBtnUI()
    {
        boolean isSelected = !relList.isSelectionEmpty();
        edaPanel.getEditBtn().setEnabled(isSelected);
        edaPanel.getDelBtn().setEnabled(isSelected);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        return new BasicPermisionPanel(null, "ENABLE");
    }

    /**
     * @return the permissions array
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {true, true, true, true},
                                {true, true, false, true},
                                {true, true, true, true}};
    }
    
    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        toolbarItems = new Vector<ToolBarItemDesc>();
        /*String label = getResourceString(name);
        String localIconName = name;
        String hint = getResourceString("labels_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label, localIconName, hint);

        toolbarItems.add(new ToolBarItemDesc(btn));
        */
        return toolbarItems;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        final String SYSTEM_MENU = "Specify.SYSTEM_MENU";
        
        SecurityMgr secMgr = SecurityMgr.getInstance();
        
        menuItems = new Vector<MenuItemDesc>();
        
        String securityName = "Task." + COLREL_SECURITY;
        if (!AppContextMgr.isSecurityOn() || 
            (secMgr.getPermission(securityName) != null && 
             !secMgr.getPermission(securityName).hasNoPerm()))
        {
            JMenuItem mi = createLocalizedMenuItem(COLREL_MENU, COLREL_MNU, COLREL_TITLE, true, null); 
            mi.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    createRelMgrUI();
                }
            });
            MenuItemDesc mid = new MenuItemDesc(mi, SYSTEM_MENU);
            mid.setPosition(MenuItemDesc.Position.Bottom);
            menuItems.add(mid);
        }
        
        return menuItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return starterPane = StartUpTask.createFullImageSplashPanel(title, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        super.doCommand(cmdAction);
        
        if (cmdAction.isType(COLRELTSK))
        {
            if (cmdAction.isAction(CR_RELTYPES))
            {
                createRelMgrUI();
            }
            
        } else if (cmdAction.isType(RecordSetTask.RECORD_SET))
        {
            //processRecordSetCommands(cmdAction);
            
        }
    }
}
