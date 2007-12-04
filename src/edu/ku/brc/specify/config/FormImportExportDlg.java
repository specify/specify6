/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.config;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpAppResourceDir;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Dec 1, 2007
 *
 */
public class FormImportExportDlg extends CustomDialog
{
    protected JTree tree;

    /**
     * @throws HeadlessException
     */
    public FormImportExportDlg() throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), getResourceString("FORM_IMEX_TITLE"), true, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        tree = new JTree(root);
        
        SpecifyAppContextMgr context = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        for (int i=0;i<context.getSpAppResourceList().size();i++)
        {
            SpAppResourceDir dir = context.getSpAppResourceList().get(i);
            DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(dir.getIdentityTitle());
            newChild.setUserObject(dir);
            root.add(newChild);
            for (SpAppResource appRes : dir.getSpAppResources())
            {
                DefaultMutableTreeNode kid = new DefaultMutableTreeNode(appRes.getIdentityTitle());
                kid.setUserObject(appRes);
                newChild.add(kid);
            }
        }
        /*DefaultMutableTreeNode newChild = new DefaultMutableTreeNode("Back Stop"); // I18N
        newChild.setUserObject(context.get);
        root.add(newChild);
        */
        JScrollPane sp = new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(sp, BorderLayout.CENTER);
        contentPanel = panel;
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        setSize(500,500);
    }

}
