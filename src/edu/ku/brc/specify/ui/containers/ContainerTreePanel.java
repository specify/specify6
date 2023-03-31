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
package edu.ku.brc.specify.ui.containers;

import static edu.ku.brc.ui.UIRegistry.getViewbasedFactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrintQuality;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.db.ViewBasedDisplayPanel;
import edu.ku.brc.af.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.EditDeleteAddVertPanel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostGlassPane;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 3, 2010
 *
 */
public class ContainerTreePanel extends JPanel implements CommandListener
{
    private static final String GETSQL = "SELECT pcn.ContainerID FROM container cn LEFT JOIN container pcn ON cn.ParentID = pcn.ContainerID " +
                                         "WHERE cn.ContainerID = ?";
    
    protected final static Color      bgColor      = Color.WHITE;//new Color(245, 245, 245, 255);
    protected final static int        ROW_HEIGHT   = 26;
    protected final static Color      bannerColor  = new Color(30, 144, 255);    // XXX PREF
    protected Component               header = null;
    
    protected Container               rootContainer;
    protected CollectionObject        rootColObj;
    
    protected Container               currContainer;
    protected CollectionObject        currColObj;
    
    protected HashSet<Integer>        colObjIdHash    = new HashSet<Integer>();
    protected HashSet<Integer>        containerIdHash = new HashSet<Integer>();
    
    protected GhostActionableTree     tree;
    protected DefaultTreeModel        model;
    protected JScrollPane             scrollPane;
    
    protected ViewBasedDisplayPanel   containerPanel;
    
    protected boolean                 isViewMode     = false;
    protected EditDeleteAddVertPanel  edaColObjPanel;
    protected EditDeleteAddVertPanel  edaContnrPanel;
    protected JButton                 searchCOBtn;
    protected JButton                 searchCNBtn;
    protected JLabel                  colObjIcon;
    protected JLabel                  containerIcon;
    
    protected JLabel                  colObjAssocIcon;
    protected JLabel                  containerAssocIcon;
    
    protected ContainerTreeRenderer   treeRenderer = null;
    
    
    protected FormViewObj             formViewObj     = null;
    protected ChangeListener          changeListener  = null;
    
    /**
     * @param changeListener
     * @param isViewModeArg
     * @param rootCon
     * @param rootCO
     */
    public ContainerTreePanel(final ChangeListener changeListener,
                              final boolean isViewModeArg,
                              final Container rootCon,
                              final CollectionObject rootCO)
    {
        this.changeListener = changeListener;
        this.isViewMode     = isViewModeArg;
        this.rootContainer  = rootCon;
        this.rootColObj     = rootCO;
        
        CommandDispatcher.register("DATA_ENTRY", this);
        
        createUI();
    }
    
    /**
     * 
     */
    private void createUI()
    {
        CellConstraints cc  = new CellConstraints();

        edaColObjPanel = new EditDeleteAddVertPanel(getEditColObjAL(), getDelColObjAL(), getAddColObjAL());
        edaContnrPanel = new EditDeleteAddVertPanel(getEditContainerAL(), getDelContainerAL(), getAddContainerAL());
        
        set(rootContainer,  rootColObj);
        
        scrollPane       = UIHelper.createScrollPane(tree, true);
        
        treeRenderer = new ContainerTreeRenderer(null, !isViewMode, isViewMode);
        treeRenderer.setEditable(true);
        treeRenderer.setLeafIcon(IconManager.getIcon(CollectionObject.class.getSimpleName(), IconManager.IconSize.Std32));
        treeRenderer.setVerticalTextPosition(SwingConstants.CENTER);
        tree.setCellRenderer(treeRenderer);
        
        tree.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(final MouseEvent e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mousePressedOnTree(e);
                    }
                });
            }
        });
        
        colObjAssocIcon    = new JLabel(IconManager.getIcon(CollectionObject.class.getSimpleName(), IconManager.IconSize.Std20));
        containerAssocIcon = new JLabel(IconManager.getIcon(Container.class.getSimpleName(), IconManager.IconSize.Std20));
        
        colObjIcon         = new JLabel(IconManager.getIcon(CollectionObject.class.getSimpleName(), IconManager.IconSize.Std20));
        containerIcon      = new JLabel(IconManager.getIcon(Container.class.getSimpleName(), IconManager.IconSize.Std20));
        
        searchCOBtn        = UIHelper.createIconBtn("Search", IconManager.IconSize.Std20, "", true, null);
        searchCNBtn        = UIHelper.createIconBtn("Search", IconManager.IconSize.Std20, "", true, null);
        
        colObjIcon.setEnabled(false);
        containerIcon.setEnabled(false);
        
        containerPanel = new ViewBasedDisplayPanel(null, "ContainerBrief", Container.class.getName(), true, MultiView.NO_OPTIONS);
        
        // Right Vertical Control Panel
        PanelBuilder rpb = null;
        if (!isViewMode)
        {
            rpb = new PanelBuilder(new FormLayout("f:p:g", "p,2px,p,2px,p,10px,p, 20px,p,2px,p,10px,p,f:p:g"));
    
            PanelBuilder cnCOLblPB = new PanelBuilder(new FormLayout("p,1px,p", "p"));
            cnCOLblPB.add(containerAssocIcon, cc.xy(1,1));
            cnCOLblPB.add(colObjAssocIcon,    cc.xy(3,1));
            containerAssocIcon.setEnabled(false);
            colObjAssocIcon.setEnabled(false);
           
            int y = 1;
            rpb.add(containerIcon,   cc.xy(1,y)); y += 2;
            rpb.add(edaContnrPanel,  cc.xy(1,y)); y += 2;
            rpb.add(searchCNBtn,     cc.xy(1,y)); y += 2;
            
            rpb.addSeparator("",     cc.xy(1,y)); y += 2;
            rpb.add(colObjIcon,      cc.xy(1,y)); y += 2;
            rpb.add(edaColObjPanel,  cc.xy(1,y)); y += 2;
            rpb.add(searchCOBtn,     cc.xy(1,y)); y += 2;
        }
        
        // Main Layout
        PanelBuilder pb = new PanelBuilder(new FormLayout("l:p,8px,f:p:g", "f:p:g,10px,p,2px,p"), this);
        
        if (rpb != null)
        {
            pb.add(rpb.getPanel(), cc.xy(1,1)); 
        }
        pb.add(scrollPane, cc.xy(3,1));
        
        //pb.addSeparator("Container", cc.xyw(1,3,3));
        //pb.add(containerPanel,       cc.xyw(1,5,3));
        //pb.setDefaultDialogBorder();
        
        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
        {
            @Override
            public void valueChanged(TreeSelectionEvent e)
            {
                updateBtnUI();
                
                currContainer = null;
                currColObj    = null;
                
                DefaultMutableTreeNode node = getSelectedTreeNode();
                if (node != null)
                {
                    if (node.getUserObject() instanceof Container)
                    {
                        currContainer = (Container)node.getUserObject();
                        if (containerPanel != null)
                        {
                            containerPanel.getMultiView().setData(currContainer);
                        }
                    } else if (node.getUserObject() instanceof  CollectionObject)
                    {
                        currColObj = (CollectionObject)node.getUserObject();
                    }
                }
            }
        });
        
        GhostGlassPane glassPane = (GhostGlassPane)UIRegistry.get(UIRegistry.GLASSPANE);
        glassPane.add((GhostActionable)tree);
        
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        
        searchCOBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addColObjToContainer(true, false);
            }
        });
        
        searchCNBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addContainer(true);
            }
        });
        
        FormViewObj fvo = containerPanel.getMultiView().getCurrentViewAsFormViewObj();
        if (fvo != null)
        {
            Component comp = fvo.getCompById("nm");
            if (comp instanceof ValTextField)
            {
                final ValTextField nameTF = fvo.getCompById("nm");
                nameTF.getDocument().addDocumentListener(new DocumentAdaptor() {
                    @Override
                    protected void changed(DocumentEvent e)
                    {
                        nameFieldChanged(nameTF.getText());
                        if (changeListener != null) changeListener.stateChanged(null);
                    }
                });
                
                final ValComboBox typeCBX = fvo.getCompById("typ");
                typeCBX.getComboBox().addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        PickListItemIFace pli = (PickListItemIFace)typeCBX.getComboBox().getSelectedItem();
                        typeChanged(pli == null ? -1 : Integer.parseInt(pli.getValue()));
                        if (changeListener != null) changeListener.stateChanged(null);
                    }
                });
            }
        }
    }
    
    /**
     * @return NO_OPTION for discard (continue), YES_OPTION for save, CANCEL_OPTION for Cancel
     */
    private boolean checkForChanges()
    {
        boolean isOK = containerPanel.getMultiView().getCurrentViewAsFormViewObj().isDataCompleteAndValid(true);
        //((JButton)fvo.getSaveComponent()).setEnabled(isOK);

        /*if (formViewObj.getValidator().hasChanged())
        {
            int rv = UIRegistry.askYesNoLocalized("SAVE", "DISCARD", "CANCEL", "The data in the form has not been changed.", "TITLE");
            if (rv == JOptionPane.YES_OPTION)
            {
                ((JButton)formViewObj.getSaveComponent()).doClick();
            }
        }
        return JOptionPane.NO_OPTION;*/
        return isOK;
    }
    
    /**
     * @param e
     */
    private void mousePressedOnTree(final MouseEvent e)
    {
        Point pnt  = e.getLocationOnScreen();
        
        int i = 0;
        for (Rectangle r : treeRenderer.getHitRects())
        {
            //System.out.println(pnt+" "+r+" "+r.contains(pnt));
            if (r.contains(pnt))
            {
                DefaultMutableTreeNode node = getSelectedTreeNode();
                if (node != null)
                {
                    if (node.getUserObject() instanceof Container)
                    {
                        Container cn = (Container)node.getUserObject();
                        if (cn != null)
                        {
                            if (!isViewMode) // Edit Mode
                            {
                                if (cn.getCollectionObject() != null)
                                {
                                    if (i == 0)
                                    {
                                        editColObj(cn.getCollectionObject());
                                    } else
                                    {
                                        delColObj();
                                    }
                                } else
                                {
                                    if (i == 0)
                                    {
                                        addColObjToContainer(true, true);
                                    } else
                                    {
                                        addColObjToContainer(false, true);
                                    }
                                }
                            } else if (cn.getCollectionObject() != null && i == 0)
                            {
                                viewColObj();
                            }
                        }
                    } else if (node.getUserObject() instanceof CollectionObject)
                    {
                        if (isViewMode)
                        {
                            if (i == 0)
                            {
                                viewColObj();
                            }
                        } else
                        {
                            if (i == 0)
                            {
                                editColObj();
                            }
                        }
                    }
                }
                break;
            }
            i++;
        }
    }
    
    /**
     * 
     */
    public void cleanUp()
    {
        GhostGlassPane glassPane = (GhostGlassPane)UIRegistry.get(UIRegistry.GLASSPANE);
        glassPane.remove((GhostActionable)tree);
        
        tree.cleanUp();
    }
    
    /**
     * @param text
     */
    public void nameFieldChanged(final String text)
    {
        if (currContainer != null)
        {
            currContainer.setName(text);
        }
        model.nodeChanged((TreeNode)model.getRoot());
    }
    
    /**
     * @param type
     */
    public void typeChanged(final int type)
    {
        if (currContainer != null)
        {
            currContainer.setType((short)type);
        }
        model.nodeChanged((TreeNode)model.getRoot());
    }
    
    /**
     * @param containerId
     * @return
     */
    private Integer getTopMostParentId(final Integer containerId)
    {
        if (containerId != null)
        {
            PreparedStatement pStmt = null;
            try
            {
                pStmt = DBConnection.getInstance().getConnection().prepareStatement(GETSQL);
                
                Integer pContainerId = containerId;
                do
                {
                    pStmt.setInt(1, pContainerId);
                    pContainerId = null;
                    
                    ResultSet rs = pStmt.executeQuery();
                    if (rs.next())
                    {
                        pContainerId = (Integer)rs.getObject(1);
                    }
                    rs.close();
                    
                } while (pContainerId != null);
            
                return pContainerId;
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            } finally
            {
               try
                {
                    if (pStmt != null) pStmt.close();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    /**
     * 
     */
    public void set(final Container rootCon,
                    final CollectionObject rootCO)
    {
        this.rootContainer = rootCon;
        this.rootColObj    = rootCO;
        
        colObjIdHash.clear();
        containerIdHash.clear();
        
        if (rootCon != null)
        {
            Integer rootParentId = getTopMostParentId(rootCon.getId());
            if (rootParentId != null)
            {
                this.rootContainer = getContainerFromDB(rootParentId);
                this.rootColObj    = rootContainer.getCollectionObject();
            }
        } else if (rootCO ==  null)
        {
            
        }
        
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        rootNode.setUserObject(rootContainer != null ? rootContainer : rootColObj);
        
        if (rootContainer != null)
        {
            loadContainerTree(rootNode, rootContainer, null);
            
        } else if (rootColObj != null)
        {
            loadContainerTree(rootNode, null, rootColObj);
        } else
        {
            rootNode.setUserObject("New Container");
        }
        
        model = new DefaultTreeModel(rootNode);
        if (tree == null)
        {
            tree = new GhostActionableTree(this, model);
            tree.setRowHeight(ROW_HEIGHT);
            tree.setBackground(bgColor);
            tree.setEditable(false);
            tree.setVisibleRowCount(15);
            registerPrintContextMenu(tree);
            
        } else
        {
            tree.setModel(model);
        }
        model.reload();
        
        if (containerPanel != null)
        {
            containerPanel.getMultiView().setData(rootCon);
        }
    }
    
    /**
     * @param parentNode
     * @param container
     * @param colObj
     */
    private void loadContainerTree(final DefaultMutableTreeNode parentNode, 
                                   final Container container, 
                                   final CollectionObject colObj)
    {
        if (container != null)
        {
            container.forceLoad();
            
            int cnt = parentNode.getChildCount();
            Vector<CollectionObject> coKids = new Vector<CollectionObject>(container.getCollectionObjectKids());
            Collections.sort(coKids);
            for (CollectionObject co : coKids)
            {
                cnt = parentNode.getChildCount();
                DefaultMutableTreeNode node = new DefaultMutableTreeNode();
                node.setUserObject(co);
                colObjIdHash.add(co.getId());
                model.insertNodeInto(node, parentNode, cnt);
                loadContainerTree(node, null, co);
            }
            
            Vector<Container> cnKids = new Vector<Container>(container.getChildrenList());
            Collections.sort(cnKids);
            for (Container cn : cnKids)
            {
                cnt = parentNode.getChildCount();
                DefaultMutableTreeNode node = new DefaultMutableTreeNode();
                node.setUserObject(cn);
                containerIdHash.add(cn.getId());
                model.insertNodeInto(node, parentNode, cnt);
                loadContainerTree(node, cn, null);
            }
            
        } else if (colObj != null)
        {
            if (colObj.getContainer() != null)
            {
                loadContainerTree(parentNode, colObj.getContainer(), null);
            }
        } else
        {
            // error
        }
    }
    
    /**
     * 
     */
    private DefaultMutableTreeNode getSelectedTreeNode()
    {
        boolean isNodeSelected = tree.getSelectionCount() > -1;
        if (isNodeSelected)
        {
            TreePath path = tree.getSelectionModel().getSelectionPath();
            if (path != null)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getPathComponent(path.getPathCount()-1);
                if (node != null)
                {
                    return node;
                }
            }
        }
        return null;
    }
    
    /**
     * @param rs
     */
    public void addRecordSet(final RecordSetIFace rs)
    {
        if (!checkForChanges())
        {
            return;
        }

        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            ArrayList<Integer>          inUseIds            = new ArrayList<Integer>();
            ArrayList<CollectionObject> alreadyHasContainer = new ArrayList<CollectionObject>();
            ArrayList<CollectionObject> alreadyContained    = new ArrayList<CollectionObject>();
            
            DefaultMutableTreeNode parentNode = getSelectedTreeNode();
            if (parentNode == null)
            {
                if (tree.getRowCount() == 1)
                {
                    parentNode = (DefaultMutableTreeNode)model.getRoot();
                } else
                {
                    return;
                }
            }
                    
            for (RecordSetItemIFace rsi : rs.getItems())
            {
                Integer coId = rsi.getRecordId();
                if (!colObjIdHash.contains(coId))
                {
                    if (coId != null)
                    {
                        CollectionObject dbCO = session.get(CollectionObject.class, coId);
                        if (dbCO != null)
                        {
                            if (dbCO.getContainer() == null)
                            {
                                if (dbCO.getContainerOwner() == null)
                                {
                                    addColObjAsChild(parentNode, dbCO);
                                } else
                                {
                                    alreadyContained.add(dbCO);
                                }
                            } else
                            {
                                alreadyHasContainer.add(dbCO);
                            }
                        }
                    }
                } else
                {
                    inUseIds.add(coId); 
                }
            }
            
            if (inUseIds.size() > 0 || alreadyHasContainer.size() > 0 || alreadyContained.size() > 0)
            {
                UIRegistry.showError("These COs were not added.");
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ContainerTreePanel.class, ex);

        } finally
        {
            try
            {
                if (session != null) session.close();
            } catch (Exception ex) {}
        }
    }
    
    /**
     * 
     */
    private void updateBtnUI()
    {
        containerAssocIcon.setEnabled(false);
        colObjAssocIcon.setEnabled(false);
        colObjIcon.setEnabled(false);
        containerIcon.setEnabled(false);
        
        if (tree != null)
        {
            boolean isNodeSelected = tree.getSelectionCount() > 0;
            if (isNodeSelected)
            {
                TreePath path = tree.getSelectionModel().getSelectionPath();
                if (path != null && path.getPathCount() > 0)
                {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getPathComponent(path.getPathCount()-1);
                    if (node != null)
                    {
                        Object dataObj = node.getUserObject();
                        if (dataObj != null)
                        {
                            boolean isContainer = dataObj instanceof Container;
                            
                            if (isContainer)
                            {
                                int[] selInxs = tree.getSelectionRows();
                                Container cntr = (Container)dataObj;
                                
                                edaContnrPanel.getEditBtn().setEnabled(selInxs[0] > 0);
                                if (!isViewMode)
                                {
                                    edaContnrPanel.getAddBtn().setEnabled(true);
                                    edaContnrPanel.getDelBtn().setEnabled(selInxs[0] > 0);
                                } else
                                {
                                    edaContnrPanel.getEditBtn().setEnabled(selInxs[0] > -1);
                                }

                                if (!isViewMode)
                                {
                                    colObjAssocIcon.setEnabled(true);
                                    containerAssocIcon.setEnabled(true);
                                } else
                                {
                                    containerAssocIcon.setEnabled(cntr.getCollectionObject() != null);
                                    colObjAssocIcon.setEnabled(cntr.getCollectionObject() != null);
                                }
                                colObjIcon.setEnabled(!isViewMode);
                                containerIcon.setEnabled(true);
                                
                                if (!isViewMode)
                                {
                                    edaColObjPanel.getAddBtn().setEnabled(true);
                                    edaColObjPanel.getDelBtn().setEnabled(false);
                                }
                                edaColObjPanel.getEditBtn().setEnabled(false);
                                
                                searchCOBtn.setEnabled(true);
                                searchCNBtn.setEnabled(true);
                                
                            } else
                            {
                                edaContnrPanel.setEnabled(false);
                                containerIcon.setEnabled(false);
                                
                                colObjIcon.setEnabled(true);
                                edaColObjPanel.getEditBtn().setEnabled(true);
                                if (!isViewMode)
                                {
                                    edaColObjPanel.getDelBtn().setEnabled(true);
                                    edaColObjPanel.getAddBtn().setEnabled(false);
                                    searchCOBtn.setEnabled(false);
                                    searchCNBtn.setEnabled(false);                                    
                                }
                                
                                containerAssocIcon.setEnabled(false);
                                colObjAssocIcon.setEnabled(false);
                            }
                        }
                    }
                }
            } else
            {
                edaColObjPanel.setEnabled(false);
                edaContnrPanel.setEnabled(false);
                colObjIcon.setEnabled(false);
                containerIcon.setEnabled(false);  
                searchCOBtn.setEnabled(false);
                searchCNBtn.setEnabled(false);
                
                containerAssocIcon.setEnabled(false);
                colObjAssocIcon.setEnabled(false);
            }
        }
    }
    /**
     * 
     */
    private void editColObj()
    {
        if (checkForChanges())
        {
            DefaultMutableTreeNode node = getSelectedTreeNode();
            if (node != null && node.getUserObject() instanceof CollectionObject)
            {
                CollectionObject co = editColObj((CollectionObject)node.getUserObject());
                if (co != null)
                {
                    node.setUserObject(co);
                }
            }
        }
    }

    /**
     * 
     */
    private void delColObj()
    {
        if (checkForChanges())
        {
            DefaultMutableTreeNode node = getSelectedTreeNode();
            if (node != null)
            {
                if (node.getUserObject() instanceof Container)
                {
                    Container cn = (Container)node.getUserObject();
                    CollectionObject co = cn.getCollectionObject();
                    if (cn != null)
                    {
                        cn.getCollectionObjects().clear();
                        co.setContainer(null);
                        model.nodeChanged(node);
                    }
                } else
                {
                    CollectionObject co = (CollectionObject)node.getUserObject();
                    colObjIdHash.remove(co.getId());
                    model.removeNodeFromParent(node);
                }
            }
        }
    }
    
    /**
     * @param colObj
     */
    private Container getContainerFromDB(final Integer containerId)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            Container container = session.get(Container.class, containerId);
            container.forceLoad();
            
            return container;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ContainerTreePanel.class, ex);

        } finally
        {
            try
            {
                if (session != null) session.close();
            } catch (Exception ex) {}
        }
        return null;
    }
    

    /**
     * @param colObj
     */
    private FormDataObjIFace forceLoad(final FormDataObjIFace fdo)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            FormDataObjIFace mergedObj = (FormDataObjIFace)session.merge(fdo);
            mergedObj.forceLoad();
            return mergedObj;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ContainerTreePanel.class, ex);

        } finally
        {
            try
            {
                if (session != null) session.close();
            } catch (Exception ex) {}
        }
        return null;
    }
    
    /**
     * @param colObj
     */
    private void saveColObj(final MultiView mv, final CollectionObject colObj)
    {
        mv.getDataFromUI();
        
        FormViewObj fvo = mv.getCurrentViewAsFormViewObj();
        if (fvo != null)
        {
            fvo.saveObject();
        }
    }
    
    /**
     * @param container
     */
    private CollectionObject editColObj(final CollectionObject colObj)
    {
        final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                null,
                "CollectionObject",
                null,
                "CollectionObject",
                "Save",
                null,
                null,
                true,
                MultiView.HIDE_SAVE_BTN | MultiView.DONT_ADD_ALL_ALTVIEWS | MultiView.USE_ONLY_CREATION_MODE |
                MultiView.IS_EDITTING)
        {
            @Override
            protected void okButtonPressed()
            {
                MultiView mvParent = getMultiView();
                saveColObj(mvParent, (CollectionObject)mvParent.getCurrentViewAsFormViewObj().getDataObj());
                
                super.okButtonPressed();
            }
        };
        //dlg.setHelpContext("CHANGE_PWD");
        
        Object dataObj = null;
        dlg.setWhichBtns(CustomDialog.OK_BTN | CustomDialog.CANCEL_BTN);
        if (colObj == null)
        {
            dlg.getMultiView().getCurrentViewAsFormViewObj().createNewObjectByAdding();
            dataObj = dlg.getMultiView().getCurrentViewAsFormViewObj().getCurrentDataObj();
        } else
        {
            dataObj = colObj;
        }
        dlg.getMultiView().getCurrentViewAsFormViewObj().setDataObj(dataObj);
        
        UIHelper.centerAndShow(dlg);
        if (dlg.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            return (CollectionObject)dlg.getMultiView().getCurrentViewAsFormViewObj().getDataObj();
        }
        return null;
    }
    
    /**
     * @param container
     */
    private Container createContainer(final Container parentContainer)
    {
        return createOrEditContainer(null, parentContainer);
    }
    
    /**
     * @param container
     */
    private Container createOrEditContainer(final Container container, final Container parentContainer)
    {
        final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                null,
                "ContainerBrief",
                null,
                "Container",
                "Save",
                null,
                null,
                true,
                MultiView.HIDE_SAVE_BTN | MultiView.DONT_ADD_ALL_ALTVIEWS | MultiView.USE_ONLY_CREATION_MODE |
                MultiView.IS_EDITTING)
        {
            @Override
            protected void okButtonPressed()
            {
                MultiView mvParent = getMultiView();
                
                mvParent.getDataFromUI();
                
                FormViewObj fvo = mvParent.getCurrentViewAsFormViewObj();
                if (fvo != null)
                {
                    if (parentContainer != null)
                    {
                        Container newContainer = (Container)fvo.getDataObj();
                        newContainer.setParent(parentContainer);
                        parentContainer.getChildren().add(newContainer);
                    }
                    
                    fvo.saveObject();
                }
                super.okButtonPressed();
            }
        };
        
        dlg.setWhichBtns(CustomDialog.OK_BTN | CustomDialog.CANCEL_BTN);
        if (container == null)
        {
            dlg.getMultiView().getCurrentViewAsFormViewObj().createNewObjectByAdding();
            Object dataObj = dlg.getMultiView().getCurrentViewAsFormViewObj().getCurrentDataObj();
            dlg.getMultiView().getCurrentViewAsFormViewObj().setDataObj(dataObj);
        } else
        {
            dlg.getMultiView().setData(container);
        }
        
        UIHelper.centerAndShow(dlg);
        if (dlg.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            return (Container)dlg.getMultiView().getCurrentViewAsFormViewObj().getDataObj();
        }
        return null;
    }
    
 
    /**
     * @return
     */
    private Object searchForDataObj(final Class<?> cls)
    {
        try
        {
            String title = cls.getSimpleName()+"Search";
            
            ViewBasedSearchDialogIFace srchDlg = getViewbasedFactory().createSearchDialog(null, title); //$NON-NLS-1$
            if (srchDlg != null)
            {
                //srchDlg.setTitle(title);
                UIHelper.centerAndShow(srchDlg.getDialog());
                if (!srchDlg.isCancelled())
                {
                    return forceLoad((FormDataObjIFace)srchDlg.getSelectedObject());
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyAppContextMgr.class, ex);
            // it's ok 
            // we get when it can't find the search dialog
            // xxx error dialog "Unable to retrieve default search dialog"
        }        
        return null;
    }

    /**
     * @param doAddBySearch
     * @param doAssociate
     */
    private void addColObjToContainer(final boolean doAddBySearch, final boolean doAssociate)
    {
        if (checkForChanges())
        {
            DefaultMutableTreeNode parentNode = getSelectedTreeNode();
            if (parentNode != null)
            {
                Container container = (Container)parentNode.getUserObject();
                
                CollectionObject co = doAddBySearch ? (CollectionObject)searchForDataObj(CollectionObject.class) : editColObj(null);
                if (co != null)
                {
                    if (doAssociate)
                    {
                        co.setContainer(container);
                        container.getCollectionObjects().add(co);
                        
                        saveObjs(co, container);
                        
                        model.nodeChanged(parentNode);
                        
                    } else
                    {
                        addColObjAsChild(parentNode, co);
                    }
                }
            }
        }
    }

    /**
     * 
     */
    private void addColObjAsChild(final DefaultMutableTreeNode parentNodeArg, 
                                  final CollectionObject colObj)
    {
        DefaultMutableTreeNode parentNode = parentNodeArg == null ? getSelectedTreeNode() : parentNodeArg;
        if (parentNode != null)
        {
            if (colObj != null)
            {
                Container container = (Container)parentNode.getUserObject();

                colObj.setContainerOwner(container);
                container.getCollectionObjectKids().add(colObj);
                
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode();
                newNode.setUserObject(colObj);
                colObjIdHash.add(colObj.getId());
                int inx = parentNode.getChildCount();
                model.insertNodeInto(newNode, parentNode, inx);
                model.nodesWereInserted(parentNode, new int[] {inx});
                model.nodeChanged(parentNode);
                model.reload();
                tree.restoreTree();
                
                expandToNode(newNode);
            }
        }
    }
    
    /**
     * 
     */
    private void editContainer()
    {
        if (checkForChanges())
        {
            DefaultMutableTreeNode node = getSelectedTreeNode();
            if (node != null && node.getUserObject() instanceof Container)
            {
                Container container = (Container)node.getUserObject();
                createOrEditContainer(container, null);
                model.nodeChanged(node);
            }
        }
    }
    
    /**
     * @param node
     * @param containersToBeDeleted
     * @param colObjsToBeUpdated
     */
    private void recursePrune(final DefaultMutableTreeNode node, 
                              final ArrayList<Container> containersToBeDeleted,
                              final ArrayList<CollectionObject> colObjsToBeUpdated)
    {
        if (node.getUserObject() instanceof Container)
        {
            Container cn = (Container)node.getUserObject();
            if (cn.getId() != null) 
            {
                containersToBeDeleted.add(cn);
                containerIdHash.remove(cn.getId());
                
                CollectionObject co = cn.getCollectionObject();
                if (co != null)
                {
                    cn.getCollectionObjects().clear();
                    co.setContainer(null);
                    colObjsToBeUpdated.add(cn.getCollectionObject());
                    colObjIdHash.remove(co.getId());
                }
                
                for (int i=0;i<node.getChildCount();i++)
                {
                    recursePrune((DefaultMutableTreeNode)node.getChildAt(i), containersToBeDeleted, colObjsToBeUpdated);
                }
                cn.getChildren().clear();
                
                for (CollectionObject coKid : cn.getCollectionObjectKids())
                {
                    coKid.setContainerOwner(null);
                    colObjsToBeUpdated.add(coKid);
                    colObjIdHash.remove(coKid.getId());
                }
                cn.getCollectionObjectKids().clear();
            }
        } else
        {
            CollectionObject co = (CollectionObject)node.getUserObject();
            co.setContainer(null);
            co.setContainerOwner(null);
            colObjsToBeUpdated.add(co);
            colObjIdHash.remove(co.getId());
        }
        node.setUserObject(null);
        model.removeNodeFromParent(node);
    }

    /**
     * 
     */
    private void delContainer()
    {
        if (checkForChanges())
        {
            DefaultMutableTreeNode node = getSelectedTreeNode();
            if (node != null)
            {
                ArrayList<Container>        containersToBeDeleted = new ArrayList<Container>();
                ArrayList<CollectionObject> colObjsToBeUpdated    = new ArrayList<CollectionObject>();
                
                recursePrune(node, containersToBeDeleted, colObjsToBeUpdated);
                
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    
                    session.beginTransaction();
                    for (CollectionObject co : colObjsToBeUpdated)
                    {
                        CollectionObject mergedCO = session.merge(co);
                        session.update(mergedCO);
                    }
                    
                    for (Container cn : containersToBeDeleted)
                    {
                        session.delete(cn);
                    }
                    
                    session.commit();
                    
                } catch (Exception ex)
                {
                    session.rollback();
                    
                    ex.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ContainerTreePanel.class, ex);
    
                } finally
                {
                    try
                    {
                        if (session != null) session.close();
                    } catch (Exception ex) {}
                }
            }
        }
    }
    
    /**
     * @param objs
     * @return
     */
    private boolean saveObjs(Object...objs)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            session.beginTransaction();
            for (Object obj : objs)
            {
                session.saveOrUpdate(obj);
            }
            session.commit();
            return true;
            
        } catch (Exception ex)
        {
            session.rollback();
            
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ContainerTreePanel.class, ex);

        } finally
        {
            try
            {
                if (session != null) session.close();
            } catch (Exception ex) {}
        }
        return false;
    }
    
    /**
     * @param bottomNode
     */
    private void expandToNode(final DefaultMutableTreeNode bottomNode)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (model != null && bottomNode != null && bottomNode.getParent() != null)
                {
                    TreeNode[] path = model.getPathToRoot(bottomNode);
                    if (path != null && path.length > 0)
                    {
                        tree.expandPath(new TreePath(path));
                    }
                }
            }
        });   
    }
    
    /**
     * 
     */
    private void addContainer(final boolean doSearch)
    {
        if (checkForChanges())
        {
            DefaultMutableTreeNode parentNode = getSelectedTreeNode();
            if (parentNode != null)
            {
                Container parentContainer = (Container)parentNode.getUserObject();
                Container newContainer    = doSearch ? (Container)searchForDataObj(Container.class) : createContainer(parentContainer);
                if (newContainer != null)
                {
                    if (doSearch)
                    {
                        // check here to see if they are already parented.
                        newContainer.setParent(parentContainer);
                        parentContainer.getChildren().add(newContainer);
                        saveObjs(newContainer, parentContainer);
                    }
                    
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode();
                    newNode.setUserObject(newContainer);
                    if (newContainer.getId() != null)
                    {
                        containerIdHash.add(newContainer.getId());
                    }
                    
                    int inx = parentNode.getChildCount();
                    model.insertNodeInto(newNode, parentNode, inx);
                    model.nodesWereInserted(parentNode, new int[] {inx});
                    model.nodeChanged(parentNode);
                    model.reload();
                    tree.restoreTree();
                    
                    expandToNode(newNode); // invokedLater
                }
            }
        }
    }
    
    /**
     * 
     */
    private void viewContainer()
    {
        DefaultMutableTreeNode node = getSelectedTreeNode();
        if (node != null && node.getUserObject() instanceof Container)
        {
            String title = DBTableIdMgr.getInstance().getTitleForId(Container.getClassTableId());
            ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                    null,
                    "Container",
                    null,
                    title,
                    "Close",
                    null,
                    null,
                    false,
                    MultiView.HIDE_SAVE_BTN);
            
            dlg.setWhichBtns(CustomDialog.OK_BTN);
            dlg.getMultiView().getCurrentViewAsFormViewObj().setDataObj(node.getUserObject());
            
            UIHelper.centerAndShow(dlg);
                
        }
    }
    
    /**
     * @param doAssociated
     */
    private void viewColObj()
    {
        Object dataObj = null;
        DefaultMutableTreeNode node = getSelectedTreeNode();
        if (node != null)
        {
            if (node.getUserObject() instanceof Container)
            {
                Container cn = (Container)node.getUserObject();
                dataObj = cn.getCollectionObject();
                
            } else if (node.getUserObject() instanceof CollectionObject)
            {
                dataObj = node.getUserObject();
            }
        }
        
        if (dataObj != null)
        {
            String title = DBTableIdMgr.getInstance().getTitleForId(CollectionObject.getClassTableId());
            ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                    null,
                    "CollectionObject",
                    null,
                    title,
                    "Close",
                    null,
                    null,
                    false,
                    MultiView.HIDE_SAVE_BTN);
            
            dlg.setWhichBtns(CustomDialog.OK_BTN);
            dlg.getMultiView().getCurrentViewAsFormViewObj().setDataObj(dataObj);
            
            UIHelper.centerAndShow(dlg);
        }
    }
    
    /**
     * @param fvo
     */
    public void setFVO(final FormViewObj fvo)
    {
        formViewObj = fvo;
    }
    
    /**
     * @return actionlistener
     */
    private ActionListener getEditColObjAL()
    {
        ActionListener al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!isViewMode)
                {
                    editColObj();
                } else
                {
                    viewColObj();
                }
            }
        };
        return al;
    }
    
    /**
     * @return actionlistener
     */
    private ActionListener getDelColObjAL()
    {
        if (isViewMode) return null;
        
        ActionListener al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                delColObj();
            }
        };
        return al;
    }
    
    /**
     * @return actionlistener
     */
    private ActionListener getAddColObjAL()
    {
        if (isViewMode) return null;
        
        ActionListener al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addColObjToContainer(false, false); // Do Add but don't associate, make it a child
            }
        };
        return al;
    }

    /**
     * @return actionlistener
     */
    private ActionListener getEditContainerAL()
    {
        ActionListener al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!isViewMode)
                {
                    editContainer();
                } else
                {
                    viewContainer();
                }
            }
        };
        return al;
    }
    
    /**
     * @return actionlistener
     */
    private ActionListener getDelContainerAL()
    {
        if (isViewMode) return null;
        
        ActionListener al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                delContainer();
            }
        };
        return al;
    }
    
    /**
     * @return actionlistener
     */
    private ActionListener getAddContainerAL()
    {
        if (isViewMode) return null;
        
        ActionListener al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addContainer(false);
            }
        };
        return al;
    }
    
    /**
     * @param actionableTree
     */
    public void print(final JTree actionableTree)
    {
        /*if (true)
        {
            PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
            aset.add(OrientationRequested.LANDSCAPE);
            aset.add(new Copies(2));
            aset.add(new JobName("My job", null));

            // Create a print job 
            PrinterJob pj = PrinterJob.getPrinterJob();       
            pj.setPrintable(this);
            // locate a print service that can handle the request 
            PrintService[] services =
                PrinterJob.lookupPrintServices();

            if (services.length > 0) {
                System.out.println("selected printer " + services[0].getName());
                try {
                    pj.setPrintService(services[0]);
                    pj.pageDialog(aset);
                    if(pj.printDialog(aset)) {
                        pj.print(aset);
                    }
                } catch (PrinterException pe) { 
                    System.err.println(pe);
                }
            }
            return;
        }*/
        

        DefaultMutableTreeNode clonedTree = GhostActionableTree.makeDeepCopy((DefaultMutableTreeNode)actionableTree.getModel().getRoot());
        GhostActionableTree    printTree  = new GhostActionableTree(this, new DefaultTreeModel(clonedTree));
        printTree.setRowHeight(ROW_HEIGHT);
        //printTree.setEditable(false);
        //printTree.setVisibleRowCount(15);
        ContainerTreeRenderer renderer = new ContainerTreeRenderer(null, false, false);
        renderer.setBGColor(Color.WHITE);
        renderer.setFont(getFont().deriveFont(8.0f));
        //renderer.setLeafIcon(IconManager.getIcon(CollectionObject.class.getSimpleName(), IconManager.IconSize.Std32));
        //renderer.setVerticalTextPosition(SwingConstants.CENTER);
        printTree.setCellRenderer(renderer);
        
        for (int row =0;row<printTree.getRowCount();row++)
        {
            printTree.expandRow(row);
        }
        
        PrintablePanel p = new PrintablePanel(new BorderLayout(), printTree);
        p.add(printTree, BorderLayout.CENTER);
        
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        //PrinterResolution        pr   = new PrinterResolution(300, 300, PrinterResolution.DPI);
        //MediaPrintableArea       mpa  = new MediaPrintableArea(8,21, 210-16, 296-42, MediaPrintableArea.MM);

        //aset.add( MediaSizeName.IS);
        //aset.add( pr );
        //aset.add( mpa );
        aset.add( new Copies(1) );
        aset.add(OrientationRequested.PORTRAIT );
        aset.add(PrintQuality.HIGH);
        
        PrinterJob job        = PrinterJob.getPrinterJob();
        /*PageFormat pageFormat = job.defaultPage();
        Paper      paper      = pageFormat.getPaper();
        paper.setSize(pageFormat.getWidth(), pageFormat.getHeight());
        paper.setImageableArea(
            0,
            0,
            pageFormat.getWidth(),
            pageFormat.getHeight()
            );
        //aset.add( Fidelity.FIDELITY_TRUE );
        pageFormat.setPaper(paper);

        Book book = new Book();
        book.append(p, pageFormat, 1);
        job.setPageable(book);*/
        
        job.setPrintable(p);
        boolean ok = job.printDialog();
        if (ok)
        {
            try
            {
                job.print(aset);
                
            } catch (PrinterException ex)
            {
                ex.printStackTrace();
                /* The job did not successfully complete */
            }
        }
    }
    
    /**
     * @param actionTree
     */
    private void registerPrintContextMenu(final JTree actionTree)
    {
        actionTree.addMouseListener(new MouseAdapter() 
        {
            private void displayMenu(MouseEvent e)
            {
                if (e.isPopupTrigger())
                {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem printMenu = new JMenuItem(UIRegistry.getResourceString("Print"));
                    menu.add(printMenu);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                    printMenu.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ev)
                        {
                            print(actionTree);
                        }
                    });
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                displayMenu(e);
            }
            @Override
            public void mousePressed(MouseEvent e)
            {
                super.mousePressed(e);
                displayMenu(e);
            }
            @Override
            public void mouseReleased(MouseEvent e)
            {
                super.mouseReleased(e);
                displayMenu(e);
            }
            
        });
    }

    
    //-------------------------------------------------------------------------------------------
    //--
    //-------------------------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isAction("Save") && cmdAction.getData() instanceof Container)
        {
            System.out.println(cmdAction);
        }
    }
    
    //-------------------------------------------------------------------------------------------
    //--
    //-------------------------------------------------------------------------------------------
    class PrintablePanel extends JPanel implements Printable
    {
        protected GhostActionableTree gTree;
        /**
         * @param layout
         */
        public PrintablePanel(LayoutManager layout, GhostActionableTree tree)
        {
            super(layout);
            gTree = tree;
        }
        
        /* (non-Javadoc)
         * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
         */
        public int print(Graphics g, PageFormat pageFormat, int index) throws PrinterException
        {
            Graphics2D g2 = (Graphics2D) g;
            if (index > 0)
            {
                return Printable.NO_SUCH_PAGE;
            }
            
            int w = (int)pageFormat.getWidth();
            int h = (int)pageFormat.getHeight();
            
            //System.out.println(pf.getPaper().getImageableWidth()+", "+pf.getPaper().getImageableHeight());
            double imgWidth  = pageFormat.getImageableWidth()* 1.5;
            double imgHeight = pageFormat.getImageableHeight()* 1.5;
            int    imgW      = (int)imgWidth;
            int    imgH      = (int)imgHeight;

            
            setBounds(0, 0, imgW, imgH);
            Image     fullSizeImg = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
            Graphics2D  fsG       = (Graphics2D)fullSizeImg.getGraphics();
            
            //fsG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            gTree.setBounds(0, 0, imgW, imgH);
            gTree.printAll(fsG);
            fsG.dispose();
            
            Graphics2D g2d = (Graphics2D)g;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            
            g2.drawImage(fullSizeImg, 0, 0, w, h, 0, 0, imgW, imgH, null);
            
            return Printable.PAGE_EXISTS;
        }
    }
    
    //-------------------------------------------------------------------------------------------
    //--
    //-------------------------------------------------------------------------------------------
    class ParentNodeInfo
    {
        boolean  hasColObj;
        String   containerName;
        String   colObjName;
        Integer  type;
        
        /**
         * @param hasColObj
         * @param containerName
         * @param colObjName
         */
        public ParentNodeInfo(boolean hasColObj, 
                              String containerName, 
                              String colObjName,
                              int    type)
        {
            super();
            this.hasColObj     = hasColObj;
            this.containerName = containerName;
            this.colObjName    = colObjName;
            this.type          = type;
        }
        
        /**
         * @return
         */
        public String getTitle()
        {
            if (StringUtils.isNotEmpty(containerName) && StringUtils.isNotEmpty(colObjName))
            {
                return String.format("%s - %s", containerName, colObjName);
                
            } else if (StringUtils.isNotEmpty(containerName))
            {
                return containerName;
                
            } else if (StringUtils.isNotEmpty(colObjName))
            {
                return containerName;
            }
            return "";
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return getTitle();
        }
    }

}
