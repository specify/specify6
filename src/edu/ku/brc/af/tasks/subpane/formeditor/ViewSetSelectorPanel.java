/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.af.tasks.subpane.formeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectionType;
import edu.ku.brc.specify.datamodel.SpAppResourceDir;
import edu.ku.brc.specify.datamodel.SpViewSetObj;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.AddRemoveEditPanel;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.VerticalSeparator;
import edu.ku.brc.ui.forms.persist.AltViewIFace;
import edu.ku.brc.ui.forms.persist.FormCell;
import edu.ku.brc.ui.forms.persist.FormCellField;
import edu.ku.brc.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.ui.forms.persist.FormCellIFace;
import edu.ku.brc.ui.forms.persist.FormCellLabel;
import edu.ku.brc.ui.forms.persist.FormRow;
import edu.ku.brc.ui.forms.persist.FormRowIFace;
import edu.ku.brc.ui.forms.persist.FormViewDef;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.forms.persist.ViewDef;
import edu.ku.brc.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.ui.forms.persist.ViewIFace;
import edu.ku.brc.ui.forms.persist.ViewSet;
import edu.ku.brc.ui.forms.persist.ViewSetIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 23, 2007
 *
 */
public class ViewSetSelectorPanel extends JPanel implements PropertyChangeListener
{
    public enum TYPE {SelectedObj, AddControl, DelControl, AddRow, DelRow}

    protected JList        levelsList;
    protected JList        viewSetsList;
    protected JList        viewsList;
    protected JList        altViewsList;
    protected JList        viewDefsList;
    
    protected ViewSetIFace selectedViewSet = null;
    protected ViewIFace    selectedView    = null;
    protected ViewDefIFace selectedViewDef = null;
    protected AltViewIFace selectedAltView = null;
    protected FormViewDef  formViewDef     = null;

    protected FormRow      selectedRow     = null;
    protected FormCell     selectedCell    = null;
    
    protected Vector<Control>                    controls       = null;
    protected Hashtable<String, Control>         controlHash    = new Hashtable<String, Control>();
    protected Hashtable<String, SubControl>      subcontrolHash = new Hashtable<String, SubControl>();
    
    protected EditorPropPanel                    panel;            
    protected JTree                              tree;
    protected Vector<ViewSetIFace>               viewSetVector = new Vector<ViewSetIFace>();
    
    protected JButton                            addBtn;
    protected JButton                            delBtn;
    
    protected JButton                            saveBtn;
    
    protected Hashtable<String, Boolean>         idHash      = new Hashtable<String, Boolean>();
    
    protected BasicFormPreviewPanel              previewPanel;
    
    protected AddRemoveEditPanel                 viewControlPanel;
    protected AddRemoveEditPanel                 altViewControlPanel;
    protected AddRemoveEditPanel                 viewDefControlPanel;
     
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public ViewSetSelectorPanel(final BasicFormPreviewPanel previewPanel)
    {
        this.previewPanel = previewPanel;
        
        Vector<String> levelsVec = new Vector<String>();
        SpecifyUser    user      = SpecifyUser.getCurrentUser();
        Hashtable<CollectionType, Boolean> usedColTypes = new Hashtable<CollectionType, Boolean>();
        
        levelsVec.add(user.getUserType());

        for (Agent agent : user.getAgents())
        {
            CollectionType ct = agent.getCollectionType();
            if (usedColTypes.get(ct) == null)
            {
                levelsVec.add(ct.getDiscipline());
                usedColTypes.put(ct, true);
            }
        }
        levelsVec.add("BackStop");
        
        addBtn = UIHelper.createIconBtn("PlusSign", "", new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                addControl();
            }
        });
        
        delBtn = UIHelper.createIconBtn("MinusSign", "", new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                delControl(TYPE.SelectedObj);
            }
        });
        
        ActionListener addViewAL = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                addView();
            }
        };
        
        ActionListener delViewAL = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                delView();
            }
        };
        
        viewControlPanel = new AddRemoveEditPanel(addViewAL, delViewAL, null);
        
        ActionListener addAltViewAL = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                addAltView();
            }
        };
        
        ActionListener delAltViewAL = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                delAltView();
            }
        };
        
        altViewControlPanel = new AddRemoveEditPanel(addAltViewAL, delAltViewAL, null);
        
        ActionListener addDefViewAL = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                addViewDef();
            }
        };
        
        ActionListener delDefViewAL = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                delViewDef();
            }
        };
        
        viewDefControlPanel = new AddRemoveEditPanel(addDefViewAL, delDefViewAL, null);
        
        ActionListener saveAL = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                save();
            }
        };
        AddRemoveEditPanel saveControlPanel = new AddRemoveEditPanel(saveAL, null, null);
        saveControlPanel.getAddBtn().setIcon(IconManager.getIcon("Save", IconManager.IconSize.Std16));
        saveControlPanel.getAddBtn().setEnabled(true);
        
        setLayout(new BorderLayout());
        
        levelsList = new JList(levelsVec);
        panel      = new EditorPropPanel(controlHash, subcontrolHash, null, true, this);
        //panel.setFormViewDef(null);
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,4px,p, 6px,10px,6px, p,6px,p, 6px,10px,6px, p,f:p:g", "p,2px,f:p:g,2px,p,10px,p,4px"));
        CellConstraints cc = new CellConstraints();
        
        String[] labels = new String[] {"User Level", "ViewSets", " ", "Views", "AltViews", " ", "View Defs"};
        int x = 1;
        for (String label : labels)
        {
            pb.add(new JLabel(label, SwingConstants.CENTER), cc.xy(x, 1));
            x += 2;
        }
        
        Color vsFGColor = new Color(224, 224, 224);
        Color vsBGColor = new Color(124, 124, 124);
        
        Dimension ps = new Dimension(200, 150);
        
        x = 1;
        JScrollPane sp = new JScrollPane(levelsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pb.add(sp, cc.xy(x, 3));
        x += 2;
        
        viewSetsList = new JList(new DefaultListModel());
        sp = new JScrollPane(viewSetsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setPreferredSize(ps);
        pb.add(sp, cc.xy(x, 3));
        x += 2;
        
        pb.add(new VerticalSeparator(vsFGColor, vsBGColor), cc.xy(x, 3));
        x += 2;
        
        viewsList = new JList(new DefaultListModel());
        sp = new JScrollPane(viewsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setPreferredSize(ps);
        pb.add(sp, cc.xy(x, 3));
        x += 2;
        
        altViewsList = new JList(new DefaultListModel());
        sp = new JScrollPane(altViewsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setPreferredSize(ps);
        pb.add(sp, cc.xy(x, 3));
        x += 2;
        
        pb.add(new VerticalSeparator(vsFGColor, vsBGColor), cc.xy(x, 3));
        x += 2;
        
        viewDefsList = new JList(new DefaultListModel());
        sp = new JScrollPane(viewDefsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setPreferredSize(ps);
        pb.add(sp, cc.xy(x, 3));
        x += 2;
        
        pb.add(saveControlPanel,     cc.xy(1, 5));
        //pb.add(new JLabel(" "),     cc.xy(3, 5));
        //pb.add(new JLabel(" "),     cc.xy(5, 5));
        pb.add(viewControlPanel,    cc.xy(7, 5));
        pb.add(altViewControlPanel, cc.xy(9, 5));
        //pb.add(new JLabel(" "),     cc.xy(11, 5));
        pb.add(viewDefControlPanel, cc.xy(13, 5));
        
        pb.addSeparator("View Def Editor", cc.xywh(1, 7, 13, 1));
        
        add(pb.getPanel(), BorderLayout.NORTH);
        
        pb = new PanelBuilder(new FormLayout("max(250px;p),4px,f:p:g", "t:p"));

        PanelBuilder inner = new PanelBuilder(new FormLayout("max(250px;p)", "t:p,2px,t:p"));
        tree = new JTree();
        ((DefaultTreeModel)tree.getModel()).setRoot(null);
        sp   = new JScrollPane(tree);
        inner.add(sp, cc.xy(1, 1));
        
        PanelBuilder btnPb = new PanelBuilder(new FormLayout("f:p:g,p,2px,p", "p"));
        btnPb.add(delBtn, cc.xy(2,1));
        btnPb.add(addBtn, cc.xy(4,1));
        inner.add(btnPb.getPanel(), cc.xy(1, 3));

        pb.add(inner.getPanel(), cc.xy(1, 1));
        pb.add(panel,            cc.xy(3, 1));
        
        add(pb.getPanel(), BorderLayout.CENTER);
        
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e)
            {
                treeSelected();
            }
        });
        
        levelsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    levelSelected();
                }
            }
        });
        
        viewSetsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    viewSetSelected();
                }
            }
        });
        
        viewsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    viewSelected();
                }
            }
        });
        
        altViewsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    altViewSelected();
                }
            }
        });
        
        viewDefsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    viewDefSelected();
                }
            }
        });
        
        tree.addMouseListener(new MouseAdapter() {
            
            protected void doPopup(MouseEvent e)
            {
                if (e.isPopupTrigger()) 
                {
                    int x = e.getX();
                    int y = e.getY();
                    TreePath path = tree.getPathForLocation(x, y);
                    if (path != null)
                    {
                        tree.setSelectionPath(path);
                    }
                    
                    treeSelected();
                    
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)tree.getSelectionModel().getSelectionPath().getLastPathComponent();
                    
                    if (selectedCell != null || selectedRow != null)
                    {
                        new TreePopupMenu((Component)e.getSource(), e, selectedCell);
                        
                    } else if (selectedNode.getUserObject() instanceof FormViewDef)
                    {
                        new TreePopupMenu((Component)e.getSource(), e);
                    }
                }
            }
            public void mouseReleased(MouseEvent e)
            {
                doPopup(e);
            }

            public void mousePressed(MouseEvent e)
            {
                doPopup(e);
            }
        });
        
        XStream xstream = new XStream();
        xstream.alias("control",       Control.class);
        xstream.useAttributeFor(Control.class, "type");
        xstream.useAttributeFor(Attr.class, "type");
        xstream.useAttributeFor(Attr.class, "name");
        xstream.useAttributeFor(Attr.class, "required");
        xstream.useAttributeFor(Attr.class, "defaultValue");
        xstream.aliasAttribute("default", "defaultValue");
        
        xstream.alias("uicontrols",    Vector.class);
        xstream.alias("attr",          Attr.class);
        xstream.alias("param",         Param.class);
        xstream.alias("subcontrol",    SubControl.class);
        xstream.aliasAttribute("desc", "desc");
        xstream.aliasAttribute(Attr.class, "values", "values");
        xstream.useAttributeFor(SubControl.class, "type");
        
        try
        {
            controls = (Vector<Control>)xstream.fromXML(FileUtils.readFileToString(new File("UIControls.xml")));
            for (Control control : controls)
            {
                controlHash.put(control.getType(), control);
                if (control.getSubcontrols() != null)
                {
                    for (SubControl sc : control.getSubcontrols())
                    {
                        //System.out.println(" ["+sc.getType()+"]  ");
                        subcontrolHash.put(sc.getType(), sc);
                    }
                }
            }
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @return
     */
    protected FormViewDef getViewDefFromSelection()
    {
        if (tree != null && tree.getSelectionModel() != null && tree.getSelectionModel().getSelectionPath() != null)
        {
            TreePath path = tree.getSelectionModel().getSelectionPath();
            for (int i=0;i<path.getPathCount();i++)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getPathComponent(i);
                if (node.getUserObject() instanceof FormViewDef)
                {
                    return (FormViewDef)node.getUserObject();
                }
            }
        }
        return null;
    }
    
    /**
     * 
     */
    protected void updateUIControls()
    {
        addBtn.setEnabled(false);
        delBtn.setEnabled(false);
        
        if (tree.getSelectionModel().getSelectionPath() != null)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getSelectionModel().getSelectionPath().getLastPathComponent();
            Object nodeObj = node.getUserObject();
            
            //System.out.println(nodeObj.getClass().getSimpleName());
            
            if (nodeObj instanceof FormRow)
            {
                addBtn.setEnabled(true);
                delBtn.setEnabled(true);
                
            } else if (nodeObj instanceof FormViewDef)
            {
                addBtn.setEnabled(true);
                delBtn.setEnabled(true);
                
            } else if (nodeObj instanceof FormCell)
            {
                addBtn.setEnabled(true);
                delBtn.setEnabled(true);
   
            }
        }
    }
    
    /**
     * 
     */
    protected void addControl()
    {
        if (selectedCell != null)
        {
            addControlTo(selectedCell);
            
        } else if (selectedRow != null)
        {
            addControlTo(null);
            
        } else
        {
            addRow();
        }
    }
    
    /**
     * @return
     */
    protected List<ControlIFace> getControlsList(final boolean addRow)
    {
        List<ControlIFace> list = new Vector<ControlIFace>();
        for (Control c : controls)
        {
            list.add(c);
            if (c.getSubcontrols() != null && c.getSubcontrols().size() > 0)
            {
                for (SubControl sc : c.getSubcontrols())
                {
                    list.add(sc);
                }
            }
        }
        return list;
    }
    
    /**
     * @param viewDef
     * @param formRow
     * @param selectedCell
     */
    protected void addControlTo(final FormCell selectedCell)
    {
        List<ControlIFace> list = getControlsList(true);

        ToggleButtonChooserDlg<ControlIFace> dlg = new ToggleButtonChooserDlg<ControlIFace>((Frame)UIRegistry.getTopWindow(), "Choose A Control", list, ToggleButtonChooserPanel.Type.RadioButton);
        dlg.setUseScrollPane(true);
        dlg.setVisible(true);
        
        if (!dlg.isCancelled())
        {
            addControl(dlg.getSelectedObject(), selectedCell);
        }
    }
    
    /**
     * @param fcf
     */
    protected void setDefaultDspUIType(final FormCellField fcf)
    {
        FormCellFieldIFace.FieldType dspUiType = null;
        FormCellFieldIFace.FieldType uitype = fcf.getUiType();
        switch (uitype)
        {
            case textarea:
                dspUiType = FormCellFieldIFace.FieldType.dsptextarea;
                break;

            case querycbx:
                dspUiType = FormCellFieldIFace.FieldType.textfieldinfo;
                break;

            case formattedtext:
                dspUiType = FormCellFieldIFace.FieldType.formattedtext;
                break;

            case url:
            case list:
            case image:
            case checkbox:
            case password:
            case plugin:
            case button:
                dspUiType = uitype;
                break;

            case spinner:
            case combobox:
                dspUiType = FormCellFieldIFace.FieldType.dsptextfield;
                break;
            default:
                dspUiType = FormCellFieldIFace.FieldType.dsptextfield;
                break;
            
        } // switch
        fcf.setDspUIType(dspUiType);
    }
    
    /**
     * Tells the tree model that a node hash changed.
     * @param parentNode the parent node
     * @param model the tree model
     * @param updateChildren update kids
     */
    protected void updateTreeNodes(final DefaultMutableTreeNode parentNode, 
                                   final DefaultTreeModel       model,
                                   final boolean                updateChildren)
    {
        model.nodeChanged(parentNode);
        if (updateChildren)
        {
            for (int i=0;i<parentNode.getChildCount();i++)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)parentNode.getChildAt(i);
                updateTreeNodes(node, model, true);
            }
        }
    }
    
    /**
     * Renumbers rows and update the tree nodes.
     * @param rows the rows to be renumbered
     */
    protected void renumberRows(final Vector<FormRowIFace>   rows)
    {
        byte num = 0;
        for (FormRowIFace row : formViewDef.getRows())
        {
            row.setRowNumber(num);
            num++;
        }
    }
    
    /**
     * Adds a new row in the right position (above).
     */
    protected void addRow()
    {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();

        FormRow newRow = new FormRow();
        
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newRow);
        newNode.setUserObject(newRow);
        
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)tree.getSelectionModel().getSelectionPath().getLastPathComponent();
        DefaultMutableTreeNode parentNode   = (DefaultMutableTreeNode)selectedNode.getParent();
        if (parentNode == null)
        {
            parentNode   = (DefaultMutableTreeNode)model.getRoot();
            selectedNode = null;
        }
        
        int position;
        if (selectedRow == null || parentNode.getUserObject() instanceof String)
        {
            formViewDef.getRows().add(newRow);
            position = formViewDef.getRows().size() - 1;
            
        } else
        {
            position = formViewDef.getRows().indexOf(selectedRow);
            formViewDef.getRows().insertElementAt(newRow, (byte)position);
        }
        
        model.insertNodeInto(newNode, parentNode, position);

        renumberRows(formViewDef.getRows());
        updateTreeNodes((DefaultMutableTreeNode)model.getRoot(), model, false);
        
        Object[] newPath = new Object[2];
        newPath[0] = parentNode;
        newPath[1] = newNode;
        
        final TreePath newTreePath = new TreePath(newPath);
        SwingUtilities.invokeLater(new Runnable() {

            public void run()
            {
                tree.setSelectionPath(newTreePath);
            }
        });
        
        previewPanel.rebuild(false);
    }

    
    /**
     * @param selectedControl
     * @param selectedCell
     */
    protected void addControl(final ControlIFace selectedControl, 
                              final FormCell     selectedCell)
    {
        int position = 0;
        if (selectedCell != null)
        {
            position = selectedRow.getCells().indexOf(selectedCell);
        }
        
        if (selectedControl instanceof RowControl)
        {
            addRow();
            return;
        }

        EditorPropPanel panel = new EditorPropPanel(controlHash, subcontrolHash, getAvailableFieldCells(), false, this);
        //panel.setFormViewDef(formViewDef);
        
        FormCell formCell = null;
        boolean  skip     = false;
        if (selectedControl instanceof Control)
        {
            Control  control  = (Control)selectedControl;
            
            if (control.getType().equals("label"))
            {
                formCell = new FormCellLabel();
                formCell.setIdent(Integer.toString(getNextId()));
            }
            
            if (formCell != null)
            {
                formCell.setType(FormCellIFace.CellType.valueOf(control.getType()));
                
                //System.out.println(formCell.getType() + "  "+ formCell.getClass().getSimpleName());
                
                panel.loadView(formCell.getType().toString(), selectedViewDef.getClassName());
                panel.setDataIntoUI(formViewDef, 
                        formCell, 
                        (formViewDef.getRows().size()*2)-1, 
                        formViewDef.getRowDefItem().getNumItems(),
                        selectedRow.getRowNumber(), 
                        (selectedRow.getCells().size()*2)-1, 
                        formViewDef.getColumnDefItem().getNumItems(),
                        position);
            }
            
        } else
        {
            SubControl  subControl  = (SubControl)selectedControl;
            
            FormCellField fcf = null;
            if (subControl.getType().equals("combobox"))
            {
                fcf = new FormCellField(FormCell.CellType.field, Integer.toString(getNextId()), "", 1, 1);
                fcf.setUiType(FormCellFieldIFace.FieldType.combobox);
                setDefaultDspUIType(fcf);
                formCell = fcf;
            }
            //System.out.println("* ["+fcf.getUiType().toString() + "]  "+ fcf.getClass().getSimpleName());
            //SubControl subcontrol = subcontrolHash.get(fcf.getUiType().toString());
            //System.out.println("SC: "+subcontrol);
            
            panel.loadView(fcf.getUiType().toString(), selectedViewDef.getClassName());
            panel.setDataIntoUI(formViewDef, 
                                fcf, 
                                (formViewDef.getRows().size()*2)-1, 
                                formViewDef.getRowDefItem().getNumItems(),
                                selectedRow.getRowNumber(),
                                (selectedRow.getCells().size()*2)-1,
                                formViewDef.getColumnDefItem().getNumItems(),
                                position);
                
        }
        
        if (!skip && formCell != null)
        {
            CustomDialog propDlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), "Create", true, panel);
            propDlg.createUI();
            //panel.getViewDefMultiView().getCurrentView().getValidator().addEnableItem(propDlg.getOkBtn());
            propDlg.pack();
            Rectangle r = propDlg.getBounds();
            r.width += 60;
            r.height += 30;
            propDlg.setBounds(r);
            propDlg.setVisible(true);
            
            if (!propDlg.isCancelled())
            {
                if (selectedControl instanceof Control)
                {
                    panel.getDataFromUI(formCell);
                } else
                {
                    panel.getDataFromUI((FormCellField)formCell);
                }
                
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(formCell);
                newNode.setUserObject(formCell);
                
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)tree.getSelectionModel().getSelectionPath().getLastPathComponent();
                if (!(parentNode.getUserObject() instanceof FormRow))
                {
                    parentNode = (DefaultMutableTreeNode)parentNode.getParent();
                }
                
                TreePath treePath = tree.getSelectionModel().getSelectionPath();
                Object[] path = treePath.getPath();
                
                Object[] newPath = new Object[path.length + (selectedCell == null ? 1 : 0)];
                for (int i=0;i<path.length;i++)
                {
                    newPath[i] = path[i];
                }
                newPath[newPath.length-1] = newNode;

                if (selectedRow.getCells().size() == 0)
                {
                    selectedRow.getCells().add(formCell);
                    ((DefaultTreeModel)tree.getModel()).insertNodeInto(newNode, parentNode, 0);
                    
                } else if (position == selectedRow.getCells().size()-1)
                {
                    //System.out.println("Adding New Cell at position["+(position+1)+"] number of nodes["+nodeParent.getChildCount()+"] rowCells "+selectedRow.getCells().size());
                    selectedRow.getCells().add(formCell);
                    ((DefaultTreeModel)tree.getModel()).insertNodeInto(newNode, parentNode, position+1);
                    
                } else
                {
                    //System.out.println("Adding New Cell at position["+position+"] number of nodes["+nodeParent.getChildCount()+"] rowCells "+selectedRow.getCells().size());
                    selectedRow.getCells().insertElementAt(formCell, position+1);
                    ((DefaultTreeModel)tree.getModel()).insertNodeInto(newNode, parentNode, position+1);
                }
                
                System.out.println("******* ADDING ["+formCell.getIdent()+"]");
                
                idHash.put(formCell.getIdent(), true);
                
                final TreePath newTreePath = new TreePath(newPath);
                SwingUtilities.invokeLater(new Runnable() {

                    public void run()
                    {
                        tree.setSelectionPath(newTreePath);
                    }
                });
                
                previewPanel.rebuild(false);
            }
        }

    }
    
    /**
     * Deletes a ForRow or a FormCell.
     */
    protected void delControl(final TYPE type)
    {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)tree.getSelectionModel().getSelectionPath().getLastPathComponent();
        DefaultMutableTreeNode parentNode   = (DefaultMutableTreeNode)selectedNode.getParent();
        Object                 formObj      = selectedNode.getUserObject();
        
        // For when we select delete row Menu when on a Control
        if (type == TYPE.DelRow && formObj instanceof FormCell)
        {
            selectedNode = parentNode;
            parentNode   = (DefaultMutableTreeNode)parentNode.getParent();
            formObj      = selectedNode.getUserObject();
        }
        
        if (formObj instanceof FormRow)
        {
            FormRow row = (FormRow)formObj;
            ((FormViewDef)parentNode.getUserObject()).getRows().remove(row);
            for (FormCellIFace formCell : row.getCells())
            {
                idHash.remove(formCell.getIdent());
            }
            
        } else if (formObj instanceof FormCell)
        {
            FormCell formCell = (FormCell)formObj;
            ((FormRow)parentNode.getUserObject()).getCells().remove(formCell);
            idHash.remove(formCell.getIdent());
        }
        
        renumberRows(formViewDef.getRows());
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        
        model.removeNodeFromParent(selectedNode);
    }
    
    /**
     * Builds a hash of the ids of the cells in the form.
     * @param fvd
     */
    protected void buildIdHash(final FormViewDef fvd)
    {
        idHash.clear();
        for (FormRowIFace row : fvd.getRows())
        {
            for (FormCellIFace cell : row.getCells())
            {
                idHash.put(cell.getIdent(), true);
            }
        }
    }
    
    /**
     * @return
     */
    protected int getNextId()
    {
        int id = 1;
        while (idHash.get(Integer.toString(id)) != null)
        {
            id++;
        }
        return id;
    }
    
    protected Vector<FormCellLabel> getAvailableLabels()
    {
        Vector<FormCellLabel>      list      = new Vector<FormCellLabel>();
        Hashtable<String, FormCellLabel> labelHash    = new Hashtable<String, FormCellLabel>();
        //Hashtable<String, FormCellLabel> labelForHash = new Hashtable<String, FormCellLabel>();
        // Add all the labels
        for (FormRowIFace row : formViewDef.getRows())
        {
            for (FormCellIFace cell : row.getCells())
            {
                if (cell instanceof FormCellLabel)
                {
                    //labelForHash.put(((FormCellLabel)cell).getLabelFor(), (FormCellLabel)cell);
                    labelHash.put(cell.getIdent(), (FormCellLabel)cell);
                }
            }
        }
        
        // remove the ones in use
        for (FormRowIFace row : formViewDef.getRows())
        {
            for (FormCellIFace cell : row.getCells())
            {
                if (cell instanceof FormCellField)
                {
                    FormCellField fcf   = (FormCellField)cell;
                    FormCellLabel label = labelHash.get(fcf.getIdent());
                    if (label != null)
                    {
                        labelHash.remove(label.getIdent());
                    }
                }
            }
        }
        list.addAll(labelHash.values());
        return list;
    }
    
    /**
     * @return
     */
    protected Vector<FormCellField> getAvailableFieldCells()
    {
        Vector<FormCellField>            list         = new Vector<FormCellField>();
        Hashtable<String, FormCellLabel> labelForHash = new Hashtable<String, FormCellLabel>();
        // Add all the labels
        for (FormRowIFace row : formViewDef.getRows())
        {
            for (FormCellIFace cell : row.getCells())
            {
                if (cell instanceof FormCellLabel)
                {
                    FormCellLabel fcl = (FormCellLabel)cell;
                    if (StringUtils.isNotEmpty(fcl.getLabelFor()))
                    {
                        labelForHash.put(fcl.getLabelFor(), (FormCellLabel)cell);
                    }
                }
            }
        }
        
        // add the ones not in use
        for (FormRowIFace row : formViewDef.getRows())
        {
            for (FormCellIFace cell : row.getCells())
            {
                if (cell instanceof FormCellField)
                {
                    FormCellField fcf   = (FormCellField)cell;
                    FormCellLabel label = labelForHash.get(fcf.getIdent());
                    if (label == null)
                    {
                        list.add(fcf);
                    }
                }
            }
        }
        return list;
    }
    
    /**
     * 
     */
    protected void treeSelected()
    {
        TreePath treePath = tree.getSelectionModel().getSelectionPath();
        if (treePath == null)
        {
            selectedRow  = null;
            selectedCell = null;
            return;
        }
        
        DefaultMutableTreeNode node    = (DefaultMutableTreeNode)treePath.getLastPathComponent();
        Object                 nodeObj = node.getUserObject();
        
        if (nodeObj instanceof FormRow)
        {
            if (nodeObj == selectedRow)
            {
                return;
            }
            selectedRow = (FormRow)nodeObj;
            selectedCell = null;
            
        } else if (nodeObj instanceof FormCell)
        {
            if (nodeObj == selectedCell)
            {
                return;
            }
            selectedCell = (FormCell)nodeObj;
            DefaultMutableTreeNode rowNode = (DefaultMutableTreeNode)node.getParent();
            selectedRow = (FormRow)rowNode.getUserObject();
            
        } else
        {
            selectedRow = null;
            selectedCell = null;
        }
        
        if (treePath != null)
        {
            updateUIControls();
            
            if (treePath.getPathCount() == 1)
            {
                panel.loadView("ViewDefProps", null);
                panel.setData(selectedViewDef);
                
                return;
            }
            
            if (selectedCell != null)
            {
                int col = 0;
                for (FormCellIFace fc : selectedRow.getCells())
                {
                    if (fc == nodeObj)
                    {
                        break;
                    }
                    col++;
                }
                showPropertiesPanel((FormCell)nodeObj, 
                                     formViewDef.getClassName(), 
                                     (formViewDef.getRows().size()*2)-1, 
                                     formViewDef.getRowDefItem().getNumItems(), 
                                     selectedRow.getRowNumber(), 
                                     (selectedRow.getCells().size()*2)-1,
                                     formViewDef.getColumnDefItem().getNumItems(),
                                     col);
            }
        }
    }
    
    /**
     * @param formCell
     */
    protected void showPropertiesPanel(final FormCell formCell, 
                                       final String   dataClassName,
                                       final int      rows,
                                       final int      rowDefs,
                                       final int      rowInx,
                                       final int      cols,
                                       final int      colDefs,
                                       final int      colInx)
    {
        //System.out.println(formCell.getType() + "  "+ formCell.getClass().getSimpleName());
        if (formCell instanceof FormCellField)
        {
            FormCellField fcf = (FormCellField)formCell;
            
            //System.out.println("* ["+fcf.getUiType().toString() + "]  "+ formCell.getClass().getSimpleName());
            //SubControl subcontrol = subcontrolHash.get(fcf.getUiType().toString());
            //System.out.println("SC: "+subcontrol);
            
            panel.loadView(fcf.getUiType().toString(), dataClassName);
            panel.setDataIntoUI(formViewDef, fcf, rows, rowDefs, rowInx, cols, colDefs, colInx);
            
            
        } else
        {
            //System.out.println(formCell.getType() + "  "+ formCell.getClass().getSimpleName());
            //Control control = controlHash.get(formCell.getType().toString());
            //System.out.println("Control: "+control);
            
            if (formCell instanceof FormCellLabel)
            {
                panel.setFieldsNotUsedByLabels(getAvailableFieldCells());
            }
            
            panel.loadView(formCell.getType().toString(), dataClassName);
            panel.setDataIntoUI(formViewDef, formCell, rows, rowDefs, rowInx, cols, colDefs, colInx);
            
            //panel.loadView(formCell.getType().toString());
            //panel.setDataIntoUI(formCell, control);
        }
    }
    
    /**
     * 
     */
    protected void levelSelected()
    {
        selectedViewSet = null;
        selectedView    = null;
        selectedViewDef = null;
        selectedAltView = null;
        
        ((DefaultTreeModel)tree.getModel()).setRoot(null);
        
        DefaultListModel model = (DefaultListModel)viewsList.getModel();
        model.clear();
        
        model = (DefaultListModel)altViewsList.getModel();
        model.clear();
        
        model = (DefaultListModel)viewSetsList.getModel();
        model.clear();
        viewSetVector.clear();
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        for (SpAppResourceDir dir : ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getSpAppResourceList())
        {
            if (dir.getSpAppResourceDirId() != null)
            {
                session.attach(dir);
            }
            //for (SpViewSetObj viewSetObj : dir.getSpViewSets())
            //{
                //model.addElement(viewSetObj.getName());
                for (SpViewSetObj vso : dir.getSpViewSets())
                {
                    try
                    {
                        Element root = XMLHelper.readStrToDOM4J(vso.getDataAsString());
                        ViewSet viewSet = new ViewSet(root, false);
                        viewSetVector.add(viewSet);
                        //System.out.println(viewSet);
                        model.addElement(viewSet.getName());

                    } catch (Exception ex)
                    {
                        //log.error(vso.getName());
                        //log.error(ex);
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }
            //}
        }
        session.close();
    }
    
    protected void viewSetSelected()
    {
        selectedViewSet = null;
        selectedView    = null;
        selectedViewDef = null;
        selectedAltView = null;
        
        ((DefaultTreeModel)tree.getModel()).setRoot(null);
        
        DefaultListModel model;
        
        model = (DefaultListModel)altViewsList.getModel();
        model.clear();
        
        model = (DefaultListModel)viewDefsList.getModel();
        model.clear();
        
        model = (DefaultListModel)viewsList.getModel();
        model.clear();
        
        int inx = viewSetsList.getSelectedIndex();
        if (inx > -1)
        {
            selectedViewSet = (ViewSet)viewSetVector.get(inx);
            
            viewControlPanel.getAddBtn().setEnabled(true);
            viewDefControlPanel.getAddBtn().setEnabled(true);
            
            Vector<String> names = new Vector<String>(selectedViewSet.getViews().keySet());
            Collections.sort(names);
            for (String viewName : names)
            {
                model.addElement(viewName);
            }
            
            model = (DefaultListModel)viewDefsList.getModel();
            names.clear();
            names.addAll(selectedViewSet.getViewDefs().keySet());
            Collections.sort(names);
            for (String viewDefName : names)
            {
                model.addElement(viewDefName);
            }
            
        } else
        {
            viewControlPanel.getAddBtn().setEnabled(false);
            viewDefControlPanel.getAddBtn().setEnabled(false);
        }
    }
    
    /**
     * 
     */
    protected void viewSelected()
    {
        selectedView    = null;
        selectedViewDef = null;
        selectedAltView = null;
        
        viewDefsList.clearSelection();
        
        DefaultListModel model = (DefaultListModel)altViewsList.getModel();
        model.clear();
        
        ((DefaultTreeModel)tree.getModel()).setRoot(null);
        
        if (viewsList.getSelectedIndex() > -1)
        {
            if (selectedViewSet != null)
            {
                String viewName = (String)viewsList.getSelectedValue();
                if (viewName != null)
                {
                    selectedView = selectedViewSet.getViews().get(viewName);
                    
                    for (AltViewIFace altView : selectedView.getAltViews())
                    {
                        model.addElement(altView.getName());
                    }
                }
                altViewControlPanel.getAddBtn().setEnabled(true);
                viewControlPanel.getDelBtn().setEnabled(true);
            }
            
        } else
        {
            altViewControlPanel.getAddBtn().setEnabled(false);
            viewControlPanel.getDelBtn().setEnabled(false);
        }
    }
    
    /**
     * @param view
     * @param viewDef
     * @param mode
     */
    protected void viewDefSelected(final ViewIFace    view, 
                                   final ViewDefIFace viewDef,
                                   final AltViewIFace.CreationMode mode)
    {
        if (viewDef instanceof FormViewDef)
        {
            selectedViewDef = viewDef;
            if (selectedViewDef instanceof FormViewDef)
            {
                formViewDef = (FormViewDef)viewDef;
                
                buildIdHash(formViewDef);
            }
        }
        
        for (AltViewIFace av : view.getAltViews())
        {
            if (av.getViewDef() == viewDef && mode == av.getMode())
            {
                selectedAltView = av;
                break;
            }
        }
        
        buildTreeModel(viewDef);
        
        for (int i=0;i<tree.getRowCount();i++)
        {
            tree.expandRow(i);
        }
        tree.repaint();
        previewPanel.set((View)view);
        previewPanel.setFormViewDef((ViewDef)viewDef);
        previewPanel.rebuild(mode == AltViewIFace.CreationMode.EDIT);
        
        panel.loadView("ViewDefProps", null);
        panel.setData(selectedViewDef);
        
        validate();
        repaint();

    }
    
    /**
     * 
     */
    protected void altViewSelected()
    {
        selectedAltView = null;
        
        ((DefaultTreeModel)tree.getModel()).setRoot(null);

        String altViewName = (String)altViewsList.getSelectedValue();
        if (altViewName != null)
        {
            selectedAltView = (AltViewIFace)selectedView.getAltView(altViewName);
            viewDefSelected(selectedView, selectedAltView.getViewDef(), selectedAltView.getMode());
            
            
            panel.loadView("AltViewProps", null);
            panel.setData(selectedAltView);
            
            validate();
            repaint();

        }
    }
    
    /**
     * 
     */
    protected void viewDefSelected()
    {
        ((DefaultTreeModel)tree.getModel()).setRoot(null);
        
        String viewDefName = (String)viewDefsList.getSelectedValue();
        if (viewDefName != null)
        {
            selectedView    = null;
            selectedAltView = null;
            
            viewsList.clearSelection();

            selectedViewDef = (ViewDefIFace)selectedViewSet.getViewDefs().get(viewDefName);
            if (selectedViewDef != null)
            {
                ViewIFace view = selectedView;
                if (view == null || !view.getClassName().equals(selectedViewDef.getClassName()))
                {
                    view = null;
                    for (ViewIFace v : selectedViewSet.getViews().values())
                    {
                        if (v.getClassName().equals(selectedViewDef.getClassName()))
                        {
                            view = v;
                            break;
                        }
                    }
                }
                
                if (view != null)
                {

                    viewDefSelected(view, selectedViewDef, AltViewIFace.CreationMode.EDIT);
                    
                } else
                {
                    // error
                }
                
            }
            viewDefControlPanel.getDelBtn().setEnabled(true);
            
        } else
        {
            viewDefControlPanel.getDelBtn().setEnabled(false);
        }
    }
    
    /**
     * @param viewDefs
     */
    protected void buildTreeModel(Vector<ViewDefIFace> viewDefs)
    {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("ViewDefs");
        model.setRoot(root);
        
        for (ViewDefIFace vd : viewDefs)
        {
            DefaultMutableTreeNode vdNode = new DefaultMutableTreeNode(vd.getName());
            vdNode.setUserObject(vd);
            if (vd instanceof FormViewDef)
            {
                root.add(vdNode);
                FormViewDef fvd = (FormViewDef)vd;
                for (FormRowIFace r : fvd.getRows())
                {
                    FormRow row = (FormRow)r;
                    DefaultMutableTreeNode rowNode = new DefaultMutableTreeNode(row.getRowNumber());
                    rowNode.setUserObject(row);
                    vdNode.add(rowNode);
                    
                    for (FormCellIFace c : row.getCells())
                    {
                        FormCell cell = (FormCell)c;
                        DefaultMutableTreeNode cellNode = new DefaultMutableTreeNode(cell.toString());
                        cellNode.setUserObject(cell);
                        rowNode.add(cellNode);
                    }
                }
            }
        }
        model.nodeStructureChanged(root);
    }
    
    protected void buildTreeModel(final ViewDefIFace viewDef)
    {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        DefaultMutableTreeNode vdNode = new DefaultMutableTreeNode(viewDef.getName());
        vdNode.setUserObject(viewDef);
        model.setRoot(vdNode);
        if (viewDef instanceof FormViewDef)
        {
            FormViewDef fvd = (FormViewDef)viewDef;
            for (FormRowIFace r : fvd.getRows())
            {
                FormRow row = (FormRow)r;
                DefaultMutableTreeNode rowNode = new DefaultMutableTreeNode(row.getRowNumber());
                rowNode.setUserObject(row);
                vdNode.add(rowNode);
                
                for (FormCellIFace c : row.getCells())
                {
                    FormCell cell = (FormCell)c;
                    DefaultMutableTreeNode cellNode = new DefaultMutableTreeNode(cell.toString());
                    cellNode.setUserObject(cell);
                    rowNode.add(cellNode);
                }
            }
        }
        model.nodeStructureChanged(vdNode);
    }
    
    /**
     * 
     */
    protected void addView()
    {
        /*
        EditorPropPanel panel = new EditorPropPanel(controlHash, subcontrolHash, getAvailableFieldCells(), false, this);
        panel.setFormViewDef(formViewDef);
        panel.loadFormViewDef(formViewDef);

        CustomDialog propDlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), "Create", true, panel);
        propDlg.createUI();
        //panel.getViewDefMultiView().getCurrentView().getValidator().addEnableItem(propDlg.getOkBtn());
        propDlg.pack();
        Rectangle r = propDlg.getBounds();
        r.width += 60;
        r.height += 30;
        propDlg.setBounds(r);
        propDlg.setVisible(true);
        
        if (!propDlg.isCancelled())
        {
            
        }*/
    }
    
    /**
     * 
     */
    protected void delView()
    {
        
    }
    
    /**
     * 
     */
    protected void addAltView()
    {
        
    }
    
    /**
     * 
     */
    protected void delAltView()
    {
        
    }
    
    /**
     * 
     */
    protected void addViewDef()
    {
        List<ControlIFace> list = getControlsList(true);

        ToggleButtonChooserDlg<ControlIFace> dlg = new ToggleButtonChooserDlg<ControlIFace>((Frame)UIRegistry.getTopWindow(), "Choose A Control", list, ToggleButtonChooserPanel.Type.RadioButton);
        dlg.setUseScrollPane(true);
        dlg.setVisible(true);
        
        if (!dlg.isCancelled())
        {
            addControl(dlg.getSelectedObject(), selectedCell);
        }
    }
    
    /**
     * 
     */
    protected void delViewDef()
    {
        
    }
    
    /**
     * 
     */
    protected void save()
    {
        if (selectedViewSet != null)
        {
            File file = new File("viewset.xml");
            StringBuilder sb = new StringBuilder();
            selectedViewSet.toXML(sb);
            
            try
            {
                FileUtils.writeStringToFile(file, sb.toString());

                Element root = XMLHelper.readFileToDOM4J(file);
                ViewSet vs = new ViewSet(root, false);
                for (ViewDefIFace viewDef : vs.getViewDefs().values())
                {
                    if (viewDef instanceof FormViewDef)
                    {
                        FormViewDef fvd = (FormViewDef)viewDef;
                        System.out.println(viewDef.getName()+" " + fvd.getRows().size());
                    }
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            
        }
    }
    
    //---------------------------------------------------------------------
    // Inner Classes
    //---------------------------------------------------------------------
    
    public class TreePopupMenu extends MouseAdapter
    {
        public TreePopupMenu(final Component src, final MouseEvent e, final FormCell formCell)
        {
            menu = new JPopupMenu();
            
            boolean isOnCell = selectedCell != null;
            
            JMenuItem menuItem = new JMenuItem("Add Row");
            menuItem.addActionListener(new MenuActionListener(TYPE.AddRow));
            menu.add(menuItem);
            
            menuItem = new JMenuItem("Delete Row");
            menuItem.addActionListener(new MenuActionListener(TYPE.DelRow));
            menu.add(menuItem);
            
            JMenu addMenu = new JMenu("Add Control");
            menu.add(addMenu);

            for (Object control : getControlsList(true))
            {
                menuItem = new JMenuItem(control.toString());
                menuItem.addActionListener(new MenuActionListener(formCell, control));
                addMenu.add(menuItem);
            }
            
            if (isOnCell)
            {
                menuItem = new JMenuItem("Delete Control");
                menuItem.addActionListener(new MenuActionListener(TYPE.DelControl));
                menu.add(menuItem);
            }

            menu.show(src, e.getX(), e.getY());
        }

        /**
         * Pop up for Adding just a row.
         * @param src
         * @param e
         */
        public TreePopupMenu(final Component src, final MouseEvent e)
        {
            menu = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem("Add Row");
            menu.add(menuItem);
            menuItem.addActionListener(new MenuActionListener(TYPE.AddRow));
            menu.show(src, e.getX(), e.getY());
        }

        public void mousePressed(java.awt.event.MouseEvent e)
        {

            displayMenu(e);
        }

        public void mouseReleased(java.awt.event.MouseEvent e)
        {

            displayMenu(e);
        }

        private void displayMenu(MouseEvent e)
        {
            if (e.isPopupTrigger())
                menu.show(e.getComponent(), e.getX(), e.getY());
        }


        private JPopupMenu menu;
    }
    
    class MenuActionListener implements ActionListener
    {
        protected FormCell formCell;
        protected Object   data;
        protected TYPE     type;  
        
        public MenuActionListener(final TYPE type)
        {
            this.formCell = null;
            this.data     = null;
            this.type     = type;
        }

        public MenuActionListener(final FormCell formCell, 
                                  final Object data)
        {
            this.formCell = formCell;
            this.data     = data;
            this.type     = TYPE.AddControl;
        }

        public void actionPerformed(java.awt.event.ActionEvent e)
        {
            if (type == TYPE.AddRow)
            {
                addRow();
                
            } else if (type == TYPE.DelRow || type == TYPE.DelControl)
            {
                delControl(type);
                
            } else if (data instanceof Control)
            {
                addControl((Control)data, formCell);
                
            } else if (data instanceof SubControl)
            {
                addControl((SubControl)data, formCell);
                
            } else if (data instanceof RowControl)
            {
                addRow();
            }
        }

        /**
         * @return the type
         */
        public TYPE getType()
        {
            return type;
        }
        
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        model.nodeChanged((TreeNode)model.getRoot());
        
        previewPanel.rebuild(selectedAltView.getMode() == AltViewIFace.CreationMode.EDIT);
    }

    class RowControl implements ControlIFace
    {
        /* (non-Javadoc)
         * @see edu.ku.brc.af.tasks.subpane.formeditor.ControlIFace#getDesc()
         */
        public String getDesc()
        {
            return "A Form Row";
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.af.tasks.subpane.formeditor.ControlIFace#getType()
         */
        public String getType()
        {
            return "Row";
        }
        
        public String toString()
        {
            return "New Row";
        }
        
    }
}
