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

import static edu.ku.brc.ui.UIRegistry.getFormattedResStr;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.UserAndMasterPasswordMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.ui.PasswordStrengthUI;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.validation.ValPasswordField;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionRelType;
import edu.ku.brc.specify.datamodel.CollectionRelationship;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.plugins.CollectionRelPlugin;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostActionableDropManager;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;
import edu.ku.brc.ui.dnd.Trash;
import edu.ku.brc.util.Pair;

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
    private static final Logger log = Logger.getLogger(CollectionRelTask.class);
    
    private static final String  COLRELTSK        = "COLRELTSK";
    private static final String  CR_MANAGECR      = "CR_MANAGECR";
    private static final String  CR_RELTYPES      = "CR_RELTYPES";
    
    private JList leftList;
    private JList relList;
    private JList rightList;
    private EditDeleteAddPanel edaPanel;
    
    
    /**
     * 
     */
    public CollectionRelTask()
    {
        super(COLRELTSK, "COLRELTSK");
        
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
            NavBox navBox = new NavBox(getResourceString("Actions"));
            
            CommandAction cmdAction = new CommandAction(COLRELTSK, CR_MANAGECR);
            NavBoxAction nba = new NavBoxAction(cmdAction);
            NavBoxItemIFace nbi = NavBox.createBtnWithTT(getResourceString(CR_MANAGECR), name, null, IconManager.STD_ICON_SIZE, nba);
            navBox.add(nbi);//NavBox.createBtn(getResourceString("CR_MGR"), name, IconManager.STD_ICON_SIZE));
            
            cmdAction = new CommandAction(COLRELTSK, CR_RELTYPES);
            nba       = new NavBoxAction(cmdAction);
            nbi       = NavBox.createBtnWithTT(getResourceString(CR_RELTYPES), name, null, IconManager.STD_ICON_SIZE, nba);
            
            navBox.add(nbi);//NavBox.createBtn(getResourceString("CR_MGR"), name, IconManager.STD_ICON_SIZE));
            
            navBoxes.add(navBox);
        }
        isShowDefault = true;
    }
    
    /**
     * @return will return a list or empty list, but not NULL
     */
    @SuppressWarnings("unchecked")
    public static List<?> getList(Class<?> clsObj)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            return session.getDataList("FROM "+clsObj.getSimpleName());
            
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
    @SuppressWarnings("unchecked")
    private void createRelMgrUI()
    {
        Vector<CollectionRelType> relObjVec = new Vector<CollectionRelType>((List<CollectionRelType>)getList(CollectionRelType.class));
        Vector<Collection>        colObjVec = new Vector<Collection>((List<Collection>)getList(Collection.class));
        
        leftList  = new JList(fillLDM(colObjVec));
        relList   = new JList(fillLDM(relObjVec));
        rightList = new JList(fillLDM(colObjVec));

        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g,10px,f:p:g,10px,f:p:g", "p,2px,f:p:g,2px,p"));
        
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
               addRel(); 
            }
        };
        ActionListener delAL = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
               delRel(); 
            }
        };
        
        edaPanel = new EditDeleteAddPanel(editAL, addAL, delAL);
        
        
        pb.add(UIHelper.createI18NLabel("LEFT_SIDE", SwingConstants.CENTER),  cc.xy(1, 1));
        pb.add(UIHelper.createI18NLabel("REL", SwingConstants.CENTER),        cc.xy(3, 1));
        pb.add(UIHelper.createI18NLabel("RIGHT_SIDE", SwingConstants.CENTER), cc.xy(5, 1));

        pb.add(UIHelper.createScrollPane(leftList), cc.xy(1, 3));
        pb.add(UIHelper.createScrollPane(relList), cc.xy(3, 3));
        pb.add(edaPanel, cc.xy(3, 5));
        pb.add(UIHelper.createScrollPane(rightList), cc.xy(5, 3));
        pb.setDefaultDialogBorder();
        
        ListSelectionListener lsl = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    updateBtnUI();
                }
            }
        };
        
        ListSelectionListener relLSL = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    CollectionRelType crt = (CollectionRelType)relList.getSelectedValue();
                    if (crt != null)
                    {
                        selectInList(leftList,  crt.getLeftSideCollection());
                        selectInList(rightList, crt.getRightSideCollection());
                    }
                    updateBtnUI();
                }
            }
        };
        leftList.addListSelectionListener(lsl);
        relList.addListSelectionListener(relLSL);
        rightList.addListSelectionListener(lsl);
        
        CustomDialog dlg = new CustomDialog(null, "Editor", true, pb.getPanel());
        dlg.setVisible(true);
    }
    
    /**
     * @param list
     * @param collection
     */
    private void selectInList(final JList list, final Collection collection)
    {
        DefaultListModel model = (DefaultListModel)list.getModel();
        for (int i=0;i<model.size();i++)
        {
            Collection col = (Collection)model.get(i);
            if (collection.getId().equals(col.getId()))
            {
                list.setSelectedIndex(i);
                return;
            }
        }
    }
    
    private void editRel()
    {
        createRelType();
    }
    
    private void addRel()
    {
        createRelType();
    }
    
    private void delRel()
    {
        createRelType();
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
            session.rollback();
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
    private CollectionRelType createRelType()
    {
        final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                "SystemSetup",
                "CollectionRelType",
                null,
                getResourceString("CHG_PWD_TITLE"),
                "OK",
                null,
                null,
                true,
                MultiView.HIDE_SAVE_BTN | MultiView.DONT_ADD_ALL_ALTVIEWS | MultiView.USE_ONLY_CREATION_MODE |
                MultiView.IS_EDITTING);
        //dlg.setWhichBtns(CustomDialog.OK_BTN | CustomDialog.CANCEL_BTN);
        
        dlg.setFormAdjuster(new FormPane.FormPaneAdjusterIFace() {
            @Override
            public void adjustForm(final FormViewObj fvo)
            {
                /*JLabel     leftLbl  = fvo.getLabel("left");
                JLabel     rightLbl = fvo.getLabel("right");
                
                Collection leftCol  = (Collection)leftList.getSelectedValue();
                Collection rightCol = (Collection)rightList.getSelectedValue();
                
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
                rightLbl.setFont(bold);*/
            }
        
        });
        
        CollectionRelType colRelType = new CollectionRelType();
        colRelType.initialize();
        
        dlg.setData(colRelType);
        UIHelper.centerAndShow(dlg);
        
        if (!dlg.isCancelled())
        {
            Collection left  = (Collection)leftList.getSelectedValue();
            Collection right = (Collection)rightList.getSelectedValue();
            if (left != null && right != null)
            {
                colRelType.setLeftSideCollection(left);
                colRelType.setRightSideCollection(right);
                left.getLeftSideRelTypes().add(colRelType);
                right.getRightSideRelTypes().add(colRelType);
                
                if (save(colRelType))
                {
                    ((DefaultListModel)relList.getModel()).addElement(colRelType);
                    return colRelType;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 
     */
    private void updateBtnUI()
    {
        int leftSelectedInx  = leftList.getSelectedIndex();
        int rightSelectedInx = rightList.getSelectedIndex();
        
        boolean isSelected = !relList.isSelectionEmpty();
        edaPanel.getEditBtn().setEnabled(isSelected);
        edaPanel.getDelBtn().setEnabled(isSelected);
        
        edaPanel.getAddBtn().setEnabled(leftSelectedInx != -1 && rightSelectedInx != -1 && leftSelectedInx != rightSelectedInx);
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
        String label = getResourceString(name);
        String localIconName = name;
        String hint = getResourceString("labels_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label, localIconName, hint);

        toolbarItems.add(new ToolBarItemDesc(btn));
        return toolbarItems;
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
