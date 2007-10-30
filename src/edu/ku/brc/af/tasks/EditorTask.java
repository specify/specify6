/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.af.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.ui.CustomFrame;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.forms.persist.ViewSetIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 29, 2007
 *
 */
public class EditorTask extends BaseTask
{
    public static final String EDITOR = "FormEditor";
    
    //protected ViewSetSelectorPanel viewSetSelectorPanel;
    protected JTree                tree          = null;

    
    /**
     * 
     */
    public EditorTask()
    {
        super(EDITOR, getResourceString(EDITOR));
        icon = IconManager.getImage("Form", IconManager.IconSize.Std16);
    }
    
    protected JPanel createChooseViewSetPanel()
    {
        PanelBuilder pb = new PanelBuilder(new FormLayout("p", "p,4px,p"));
        CellConstraints cc = new CellConstraints();
        
        Hashtable<String, List<ViewSetIFace>> hash = AppContextMgr.getInstance().getViewSetHash();
        Vector<String> viewSetNames = new Vector<String>(hash.keySet());
        
        //List<ViewSetIFace> viewSetList = ViewSetMgrManager.
        
        JList viewSetList = new JList(viewSetNames);
        
        pb.add(new JLabel("Choose a ViewSet"), cc.xy(1,1));
        pb.add(viewSetList, cc.xy(1,3));
        
        return pb.getPanel();
    }
    
    protected JPanel createViewViewDefEditor()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        return new Vector<MenuItemDesc>();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        SimpleDescPane pane = new SimpleDescPane(name, this, "Editor");
        
        
        pane.removeAll();
        pane.setLayout(new BorderLayout());
        
        /*
        BasicFormPreviewPanel previewPanel = new BasicFormPreviewPanel();
        CustomFrame frame = new CustomFrame("Layout Preview", previewPanel);
        
        viewSetSelectorPanel = new ViewSetSelectorPanel(previewPanel);
        
        frame.setSize(500,500);
        frame.setVisible(true);
        
        pane.add(viewSetSelectorPanel, BorderLayout.CENTER);
        */
        
        /*
        Hashtable<String, List<ViewSetIFace>> hash = AppContextMgr.getInstance().getViewSetHash();
        List<ViewSetIFace> list = hash.values().iterator().next();
        
        SpecifyAppContextMgr spMgr = (SpecifyAppContextMgr) AppContextMgr.getInstance();
        
        for (SpAppResourceDir ad : spMgr.getSpAppResourceList())
        {
            for (SpViewSetObj vso : ad.getSpViewSets())
            {
                System.out.println(vso.getName());
            }
        }
        
        
        // Get the Top Most AppDef
        SpAppResourceDir     appResDef = spMgr.getSpAppResourceList().get(0);
        DataProviderSessionIFace session   = DataProviderFactory.getInstance().createSession();
        if (appResDef.getSpAppResourceDirId() != null)
        {
            session.attach(appResDef);
        }
        Vector<ViewSetIFace> viewSetList = new Vector<ViewSetIFace>();
        for (SpViewSetObj vso : appResDef.getSpViewSets())
        {
            try
            {
                Element root = XMLHelper.readStrToDOM4J(vso.getDataAsString());
                
                ViewSet viewSet = new ViewSet(root);
                
                viewSetList.add(viewSet);

                vso.copyViewSet(viewSet);
                
                session.beginTransaction();
                session.saveOrUpdate(vso);
                session.commit();

            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            
            StringBuffer sb = new StringBuffer();
            for (SpUIViewSet v : vso.getSpViewSets())
            {
                v.toXML(sb);
            }
            System.out.println(sb.toString());
        }
        session.close();

        
        pane.add(new ViewViewSetEditorPanel(list.get(0)), BorderLayout.CENTER);
        */
        return pane;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        ToolBarDropDownBtn      btn  = createToolbarButton("FormEditor", "Form",  getResourceString("FORM_EDITOR_HINT"));
        list.add(new ToolBarItemDesc(btn));
        return list;
    }
    

}
